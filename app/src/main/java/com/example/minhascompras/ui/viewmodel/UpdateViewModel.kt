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
import com.example.minhascompras.utils.NetworkUtils
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
    
    fun checkForUpdate(showNotification: Boolean = false, force: Boolean = false) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            try {
                // Verificar conexão antes de tentar
                if (!NetworkUtils.isConnected(context)) {
                    Logger.w("UpdateViewModel", "No internet connection available")
                    _updateState.value = UpdateState.Error("Sem conexão com a internet. Verifique sua conexão e tente novamente.", isRetryable = true)
                    preferencesManager.recordCheckFailure()
                    return@launch
                }
                
                // Verificar se deve fazer verificação (a menos que seja forçado)
                if (!force && !preferencesManager.shouldCheckForUpdate(force = false)) {
                    Logger.d("UpdateViewModel", "Skipping check - too soon since last check")
                    // Verificar cache se houver atualização disponível
                    val hasCachedUpdate = preferencesManager.isUpdateAvailable()
                    val lastVersionChecked = preferencesManager.getLastVersionChecked()
                    val currentVersionCode = getCurrentVersionCode()
                    
                    if (hasCachedUpdate && lastVersionChecked > currentVersionCode) {
                        // Há atualização em cache, mas precisamos buscar os detalhes
                        Logger.d("UpdateViewModel", "Using cached update info")
                        // Continuar com a verificação mesmo assim para obter detalhes atualizados
                    } else {
                        _updateState.value = UpdateState.UpToDate
                        return@launch
                    }
                }
                
                val currentVersionCode = getCurrentVersionCode()
                val currentVersionName = getCurrentVersionName()
                
                Logger.d("UpdateViewModel", "Checking for update...")
                Logger.d("UpdateViewModel", "Current version: $currentVersionName (code: $currentVersionCode)")
                Logger.d("UpdateViewModel", "WiFi connected: ${NetworkUtils.isWiFiConnected(context)}")
                
                val updateInfo = updateManager.checkForUpdate(currentVersionCode)
                
                // Salvar timestamp da verificação
                preferencesManager.setLastCheckTime()
                
                if (updateInfo != null) {
                    Logger.d("UpdateViewModel", "Update found: ${updateInfo.versionName} (code: ${updateInfo.versionCode})")
                    
                    // Salvar informações no cache
                    preferencesManager.setLastSuccessfulCheck()
                    preferencesManager.setLastVersionChecked(updateInfo.versionCode)
                    preferencesManager.setUpdateAvailable(true)
                    
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
                    
                    // Salvar informações no cache
                    preferencesManager.setLastSuccessfulCheck()
                    preferencesManager.setLastVersionChecked(currentVersionCode)
                    preferencesManager.setUpdateAvailable(false)
                    
                    // Garantir que o estado UpToDate seja definido para mostrar o diálogo
                    // Usar um pequeno delay para garantir que a mudança de Checking para UpToDate seja observada
                    delay(100) // Pequeno delay para garantir que a UI tenha tempo de processar a mudança
                    _updateState.value = UpdateState.UpToDate
                    Logger.d("UpdateViewModel", "State set to UpToDate - dialog should appear")
                }
            } catch (e: java.net.SocketTimeoutException) {
                Logger.e("UpdateViewModel", "Timeout checking for update", e)
                preferencesManager.recordCheckFailure()
                _updateState.value = UpdateState.Error("Tempo de conexão esgotado. Verifique sua internet e tente novamente.", isRetryable = true)
            } catch (e: java.net.UnknownHostException) {
                Logger.e("UpdateViewModel", "Network error checking for update", e)
                preferencesManager.recordCheckFailure()
                _updateState.value = UpdateState.Error("Sem conexão com a internet. Verifique sua conexão e tente novamente.", isRetryable = true)
            } catch (e: Exception) {
                Logger.e("UpdateViewModel", "Error checking for update", e)
                preferencesManager.recordCheckFailure()
                _updateState.value = UpdateState.Error("Erro ao verificar atualizações: ${e.message ?: "Erro desconhecido"}", isRetryable = true)
            }
        }
    }
    
    fun checkForUpdateInBackground() {
        viewModelScope.launch {
            try {
                // Verificar conexão antes de tentar
                if (!NetworkUtils.isConnected(context)) {
                    Logger.d("UpdateViewModel", "Background check - No internet connection")
                    return@launch
                }
                
                // Verificar preferencialmente em WiFi (mas não bloquear se não estiver)
                val isWiFi = NetworkUtils.isWiFiConnected(context)
                Logger.d("UpdateViewModel", "Background check - WiFi connected: $isWiFi")
                
                // Verificar se deve fazer a verificação (evitar verificar toda vez)
                val shouldCheck = preferencesManager.shouldCheckForUpdate(force = false)
                Logger.d("UpdateViewModel", "Background check - should check: $shouldCheck")
                
                if (shouldCheck) {
                    val currentVersionCode = getCurrentVersionCode()
                    val currentVersionName = getCurrentVersionName()
                    
                    Logger.d("UpdateViewModel", "Background check - Current version: $currentVersionName (code: $currentVersionCode)")
                    
                    val updateInfo = updateManager.checkForUpdate(currentVersionCode)
                    
                    // Salvar timestamp da verificação
                    preferencesManager.setLastCheckTime()
                    
                    if (updateInfo != null) {
                        Logger.d("UpdateViewModel", "Background check - Update found: ${updateInfo.versionName}")
                        
                        // Salvar informações no cache
                        preferencesManager.setLastSuccessfulCheck()
                        preferencesManager.setLastVersionChecked(updateInfo.versionCode)
                        preferencesManager.setUpdateAvailable(true)
                        
                        // Mostrar notificação apenas se estiver em WiFi ou se for atualização crítica
                        if (isWiFi || isCriticalUpdate(updateInfo)) {
                            notificationManager.showUpdateAvailableNotification(
                                updateInfo.versionName,
                                updateInfo.releaseNotes
                            )
                        }
                    } else {
                        Logger.d("UpdateViewModel", "Background check - No update available")
                        
                        // Salvar informações no cache
                        preferencesManager.setLastSuccessfulCheck()
                        preferencesManager.setLastVersionChecked(currentVersionCode)
                        preferencesManager.setUpdateAvailable(false)
                    }
                }
            } catch (e: Exception) {
                Logger.e("UpdateViewModel", "Error in background check", e)
                preferencesManager.recordCheckFailure()
            }
        }
    }
    
    /**
     * Verifica se uma atualização é crítica (baseado em palavras-chave nas release notes)
     */
    private fun isCriticalUpdate(updateInfo: UpdateInfo): Boolean {
        val criticalKeywords = listOf("crítico", "critical", "urgente", "urgent", "segurança", "security", "bug", "crash", "fix")
        val releaseNotesLower = updateInfo.releaseNotes.lowercase()
        return criticalKeywords.any { releaseNotesLower.contains(it) }
    }
    
    fun downloadUpdate(updateInfo: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            // Validação: verificar se a versão é maior que a atual antes de iniciar o download
            val currentVersionCode = getCurrentVersionCode()
            val currentVersionName = getCurrentVersionName()
            
            if (updateInfo.versionCode <= currentVersionCode) {
                Logger.w("UpdateViewModel", "Tentativa de baixar versão igual ou inferior. Atual: $currentVersionName ($currentVersionCode), Tentativa: ${updateInfo.versionName} (${updateInfo.versionCode})")
                _updateState.value = UpdateState.Error(
                    "Não é possível baixar esta versão. Você já está usando a versão ${updateInfo.versionName} ou uma versão mais recente (${currentVersionName}).",
                    isRetryable = false
                )
                return@launch
            }
            
            // Verificar conexão antes de iniciar download
            if (!NetworkUtils.isConnected(context)) {
                Logger.w("UpdateViewModel", "No internet connection for download")
                _updateState.value = UpdateState.Error("Sem conexão com a internet. Conecte-se e tente novamente.", isRetryable = true)
                return@launch
            }
            
            // Avisar se não estiver em WiFi (mas permitir download se necessário)
            val isWiFi = NetworkUtils.isWiFiConnected(context)
            if (!isWiFi) {
                Logger.w("UpdateViewModel", "Download iniciado sem WiFi - usando dados móveis")
            }
            
            _updateState.value = UpdateState.Downloading(0, 0, 0)
            
            try {
                val totalBytes = updateInfo.fileSize ?: 0L
                val apkFile = updateManager.downloadUpdate(updateInfo, currentVersionCode) { progress ->
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
    
    /**
     * Força o estado para UpToDate para mostrar o diálogo de aviso
     * quando o usuário já está na última versão.
     * Usa um pequeno delay para garantir que a mudança de estado seja observada.
     */
    fun showUpToDateDialog() {
        viewModelScope.launch {
            // Se já está em UpToDate, forçar uma mudança temporária para disparar recomposição
            if (_updateState.value is UpdateState.UpToDate) {
                _updateState.value = UpdateState.Idle
                delay(50) // Pequeno delay para garantir que a mudança seja observada
            }
            _updateState.value = UpdateState.UpToDate
            Logger.d("UpdateViewModel", "showUpToDateDialog called - state set to UpToDate")
        }
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

