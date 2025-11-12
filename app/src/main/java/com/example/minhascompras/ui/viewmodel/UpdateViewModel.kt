package com.example.minhascompras.ui.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.UpdateInfo
import com.example.minhascompras.data.UpdateManager
import com.example.minhascompras.data.UpdateNotificationManager
import com.example.minhascompras.data.UpdatePreferencesManager
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState() // Nova versão mais recente já instalada
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState()
    data class Downloading(val progress: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateState()
    data class DownloadComplete(val apkFile: File) : UpdateState()
    data class Error(val message: String, val isRetryable: Boolean = false) : UpdateState()
}

class UpdateViewModel(private val context: Context) : ViewModel() {
    private val updateManager = UpdateManager(context)
    private val notificationManager = UpdateNotificationManager(context)
    private val preferencesManager = UpdatePreferencesManager(context)
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private var downloadJob: Job? = null
    
    fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }
    
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "0.0"
        }
    }
    
    fun checkForUpdate(showNotification: Boolean = false) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                val currentVersionCode = getCurrentVersionCode()
                val currentVersionName = getCurrentVersionName()
                
                Logger.d("UpdateViewModel", "Checking for update...")
                Logger.d("UpdateViewModel", "Current version: $currentVersionName (code: $currentVersionCode)")
                
                val updateInfo = updateManager.checkForUpdate(currentVersionCode)
                
                // Salvar timestamp da verificação
                preferencesManager.setLastCheckTime()
                
                if (updateInfo != null) {
                    Logger.d("UpdateViewModel", "Update found: ${updateInfo.versionName} (code: ${updateInfo.versionCode})")
                    _updateState.value = UpdateState.UpdateAvailable(updateInfo)
                    // Mostrar notificação se solicitado
                    if (showNotification) {
                        notificationManager.showUpdateAvailableNotification(
                            updateInfo.versionName,
                            updateInfo.releaseNotes
                        )
                    }
                } else {
                    Logger.d("UpdateViewModel", "No update available - already up to date")
                    _updateState.value = UpdateState.UpToDate
                }
            } catch (e: java.net.SocketTimeoutException) {
                Logger.e("UpdateViewModel", "Timeout checking for update", e)
                _updateState.value = UpdateState.Error("Tempo de conexão esgotado. Verifique sua internet e tente novamente.", isRetryable = true)
            } catch (e: java.net.UnknownHostException) {
                Logger.e("UpdateViewModel", "Network error checking for update", e)
                _updateState.value = UpdateState.Error("Sem conexão com a internet. Verifique sua conexão e tente novamente.", isRetryable = true)
            } catch (e: Exception) {
                Logger.e("UpdateViewModel", "Error checking for update", e)
                _updateState.value = UpdateState.Error("Erro ao verificar atualizações: ${e.message ?: "Erro desconhecido"}", isRetryable = true)
            }
        }
    }
    
    fun checkForUpdateInBackground() {
        viewModelScope.launch {
            try {
                // Verificar se deve fazer a verificação (evitar verificar toda vez)
                val shouldCheck = preferencesManager.shouldCheckForUpdate()
                Logger.d("UpdateViewModel", "Background check - should check: $shouldCheck")
                
                if (shouldCheck) {
                    val currentVersionCode = getCurrentVersionCode()
                    val currentVersionName = getCurrentVersionName()
                    
                    Logger.d("UpdateViewModel", "Background check - Current version: $currentVersionName (code: $currentVersionCode)")
                    
                    val updateInfo = updateManager.checkForUpdate(currentVersionCode)
                    
                    // Salvar timestamp da verificação
                    preferencesManager.setLastCheckTime()
                    
                    // Se há atualização disponível, mostrar notificação
                    if (updateInfo != null) {
                        Logger.d("UpdateViewModel", "Background check - Update found: ${updateInfo.versionName}")
                        notificationManager.showUpdateAvailableNotification(
                            updateInfo.versionName,
                            updateInfo.releaseNotes
                        )
                    } else {
                        Logger.d("UpdateViewModel", "Background check - No update available")
                    }
                }
            } catch (e: Exception) {
                Logger.e("UpdateViewModel", "Error in background check", e)
            }
        }
    }
    
    fun downloadUpdate(updateInfo: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(0, 0, 0)
            
            try {
                val totalBytes = updateInfo.fileSize ?: 0L
                val apkFile = updateManager.downloadUpdate(updateInfo) { progress ->
                    // Calcular bytes baixados baseado no progresso apenas se totalBytes for conhecido
                    val downloadedBytes = if (totalBytes > 0) {
                        (totalBytes * progress / 100)
                    } else {
                        0L
                    }
                    _updateState.value = UpdateState.Downloading(progress, downloadedBytes, totalBytes)
                    
                    // Atualizar notificação de progresso a cada 5%
                    if (progress % 5 == 0 || progress == 100) {
                        notificationManager.showDownloadProgressNotification(progress, updateInfo.versionName)
                    }
                }
                
                if (apkFile != null) {
                    _updateState.value = UpdateState.DownloadComplete(apkFile)
                    // Mostrar notificação de download completo
                    notificationManager.showDownloadCompleteNotification(updateInfo.versionName)
                } else {
                    _updateState.value = UpdateState.Error("Erro ao baixar a atualização. O download pode ter sido cancelado ou não há espaço suficiente.", isRetryable = true)
                }
            } catch (e: Exception) {
                Logger.e("UpdateViewModel", "Error downloading update", e)
                _updateState.value = UpdateState.Error("Erro ao baixar: ${e.message ?: "Erro desconhecido"}", isRetryable = true)
            }
        }
    }
    
    fun cancelDownload() {
        downloadJob?.cancel()
        updateManager.cancelDownload()
        // Aguardar um pouco para garantir que o cancelamento foi processado
        viewModelScope.launch {
            delay(100)
            _updateState.value = UpdateState.Idle
        }
    }
    
    fun installUpdate(apkFile: File) {
        updateManager.installApk(apkFile)
        _updateState.value = UpdateState.Idle
    }
    
    fun resetState() {
        _updateState.value = UpdateState.Idle
    }
}

class UpdateViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdateViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

