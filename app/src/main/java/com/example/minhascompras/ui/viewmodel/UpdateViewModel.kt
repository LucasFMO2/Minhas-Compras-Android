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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class DownloadComplete(val apkFile: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class UpdateViewModel(private val context: Context) : ViewModel() {
    private val updateManager = UpdateManager(context)
    private val notificationManager = UpdateNotificationManager(context)
    private val preferencesManager = UpdatePreferencesManager(context)
    
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
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
            val currentVersionCode = getCurrentVersionCode()
            val updateInfo = updateManager.checkForUpdate(currentVersionCode)
            
            // Salvar timestamp da verificação
            preferencesManager.setLastCheckTime()
            
            if (updateInfo != null) {
                _updateState.value = UpdateState.UpdateAvailable(updateInfo)
                // Mostrar notificação se solicitado
                if (showNotification) {
                    notificationManager.showUpdateAvailableNotification(
                        updateInfo.versionName,
                        updateInfo.releaseNotes
                    )
                }
            } else {
                _updateState.value = UpdateState.Error("Você já está na versão mais recente!")
            }
        }
    }
    
    fun checkForUpdateInBackground() {
        viewModelScope.launch {
            // Verificar se deve fazer a verificação (evitar verificar toda vez)
            if (preferencesManager.shouldCheckForUpdate()) {
                val currentVersionCode = getCurrentVersionCode()
                val updateInfo = updateManager.checkForUpdate(currentVersionCode)
                
                // Salvar timestamp da verificação
                preferencesManager.setLastCheckTime()
                
                // Se há atualização disponível, mostrar notificação
                if (updateInfo != null) {
                    notificationManager.showUpdateAvailableNotification(
                        updateInfo.versionName,
                        updateInfo.releaseNotes
                    )
                }
            }
        }
    }
    
    fun downloadUpdate(updateInfo: UpdateInfo) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(0)
            
            val apkFile = updateManager.downloadUpdate(updateInfo) { progress ->
                _updateState.value = UpdateState.Downloading(progress)
            }
            
            if (apkFile != null) {
                _updateState.value = UpdateState.DownloadComplete(apkFile)
            } else {
                _updateState.value = UpdateState.Error("Erro ao baixar a atualização")
            }
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

