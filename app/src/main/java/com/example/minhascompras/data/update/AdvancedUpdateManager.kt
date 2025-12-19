package com.example.minhascompras.data.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.minhascompras.data.UpdateInfo
import com.example.minhascompras.data.migration.DataMigrationManager
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Gerenciador avançado de atualizações que integra todos os componentes
 * Coordena backup, verificação de integridade, patch, rollback e migração
 */
class AdvancedUpdateManager(private val context: Context) {
    
    companion object {
        private const val MAX_UPDATE_ATTEMPTS = 3
        private const val UPDATE_TIMEOUT_MS = 300000L // 5 minutos
        private const val VERIFICATION_DELAY_MS = 3000L // 3 segundos
        private const val MIN_BATTERY_LEVEL = 30 // 30% bateria mínima
        private const val MIN_WIFI_CONNECTION = true // Requer WiFi para grandes atualizações
    }
    
    private val backupManager = BackupManager(context)
    private val integrityChecker = IntegrityChecker(context)
    private val rollbackManager = RollbackManager(context)
    private val updateLogger = UpdateLogger(context)
    private val migrationManager = DataMigrationManager(context)
    private val patchManager = PatchManager(context)
    
    private val basicUpdateManager = com.example.minhascompras.data.UpdateManager(context)
    
    /**
     * Executa atualização completa com todas as verificações
     * @param updateInfo Informações da atualização
     * @param onProgress Callback de progresso
     * @return AdvancedUpdateResult com resultado detalhado
     */
    suspend fun performAdvancedUpdate(
        updateInfo: UpdateInfo,
        onProgress: (UpdateProgress) -> Unit = {}
    ): AdvancedUpdateResult = withContext(Dispatchers.IO) {
        
        val operationId = updateLogger.logOperationStart(
            "advanced_update",
            "update",
            mapOf(
                "target_version" to updateInfo.versionName,
                "target_version_code" to updateInfo.versionCode,
                "download_url" to updateInfo.downloadUrl,
                "file_size" to updateInfo.fileSize
            )
        )
        
        val startTime = System.currentTimeMillis()
        var attempts = 0
        
        try {
            updateLogger.i("update", "Iniciando atualização avançada para v${updateInfo.versionName}")
            
            // 1. Verificar pré-requisitos
            val prereqResult = checkPrerequisites(updateInfo)
            if (!prereqResult.satisfied) {
                updateLogger.e("update", "Pré-requisitos não satisfeitos", null, mapOf(
                    "missing_requirements" to prereqResult.missingRequirements.joinToString(", ")
                ))
                return@withContext AdvancedUpdateResult(
                    success = false,
                    reason = UpdateFailureReason.PREREQUISITES_NOT_MET,
                    message = "Pré-requisitos não satisfeitos: ${prereqResult.missingRequirements.joinToString(", ")}",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            onProgress(UpdateProgress(5, "Verificando pré-requisitos..."))
            
            // 2. Criar backup completo
            onProgress(UpdateProgress(10, "Criando backup..."))
            val backupInfo = backupManager.createBackup()
            
            if (backupInfo == null) {
                updateLogger.e("update", "Falha ao criar backup")
                return@withContext AdvancedUpdateResult(
                    success = false,
                    reason = UpdateFailureReason.BACKUP_FAILED,
                    message = "Falha ao criar backup dos dados",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            updateLogger.i("backup", "Backup criado com sucesso", mapOf(
                "backup_file" to backupInfo.fileName,
                "backup_size" to backupInfo.size
            ))
            
            onProgress(UpdateProgress(15, "Backup criado"))
            
            // 3. Verificar se atualização incremental está disponível
            val currentVersionCode = getCurrentVersionCode()
            val isIncrementalAvailable = patchManager.isIncrementalUpdateAvailable(
                currentVersionCode, 
                updateInfo.versionCode
            )
            
            onProgress(UpdateProgress(20, if (isIncrementalAvailable) "Preparando atualização incremental..." else "Preparando download completo..."))
            
            // 4. Tentar atualização (com retentativas)
            while (attempts < MAX_UPDATE_ATTEMPTS) {
                attempts++
                
                try {
                    updateLogger.i("update", "Tentativa $attempts/$MAX_UPDATE_ATTEMPTS")
                    
                    val updateResult = if (isIncrementalAvailable) {
                        performIncrementalUpdate(updateInfo, onProgress)
                    } else {
                        performFullUpdate(updateInfo, onProgress)
                    }
                    
                    if (updateResult.success) {
                        // 5. Verificar pós-atualização
                        onProgress(UpdateProgress(85, "Verificando instalação..."))
                        delay(VERIFICATION_DELAY_MS)
                        
                        val verificationResult = verifyUpdateSuccess(updateInfo)
                        
                        if (verificationResult.success) {
                            // 6. Executar migrações se necessário
                            onProgress(UpdateProgress(90, "Migrando dados..."))
                            val migrationResult = migrationManager.migrateIfNeeded()
                            
                            if (!migrationResult.success) {
                                updateLogger.e("migration", "Falha na migração pós-atualização")
                                
                                // Tentar rollback
                                val rollbackResult = rollbackManager.executeRollback(
                                    RollbackReason.DATA_CORRUPTION,
                                    backupInfo
                                )
                                
                                return@withContext AdvancedUpdateResult(
                                    success = false,
                                    reason = UpdateFailureReason.MIGRATION_FAILED,
                                    message = "Falha na migração de dados: ${migrationResult.message}",
                                    duration = System.currentTimeMillis() - startTime,
                                    rollbackResult = rollbackResult
                                )
                            }
                            
                            // 7. Limpeza pós-atualização
                            onProgress(UpdateProgress(95, "Finalizando..."))
                            cleanupAfterSuccessfulUpdate(backupInfo)
                            
                            onProgress(UpdateProgress(100, "Atualização concluída!"))
                            
                            val duration = System.currentTimeMillis() - startTime
                            
                            updateLogger.logOperationEnd(
                                operationId, "advanced_update", "update",
                                true, duration, mapOf(
                                    "final_version" to updateInfo.versionName,
                                    "attempts" to attempts,
                                    "incremental" to isIncrementalAvailable
                                )
                            )
                            
                            return@withContext AdvancedUpdateResult(
                                success = true,
                                updateInfo = updateInfo,
                                backupInfo = backupInfo,
                                isIncremental = isIncrementalAvailable,
                                attempts = attempts,
                                duration = duration,
                                migrationResult = migrationResult
                            )
                        } else {
                            updateLogger.e("update", "Verificação pós-atualização falhou", null, mapOf(
                                "verification_errors" to verificationResult.errors.joinToString(", ")
                            ))
                            
                            // Tentar rollback
                            val rollbackResult = rollbackManager.executeRollback(
                                RollbackReason.UPDATE_FAILED,
                                backupInfo
                            )
                            
                            return@withContext AdvancedUpdateResult(
                                success = false,
                                reason = UpdateFailureReason.VERIFICATION_FAILED,
                                message = "Verificação pós-atualização falhou: ${verificationResult.errors.joinToString(", ")}",
                                duration = System.currentTimeMillis() - startTime,
                                rollbackResult = rollbackResult
                            )
                        }
                    } else {
                        updateLogger.w("update", "Tentativa $attempts falhou: ${updateResult.errorMessage}")
                    }
                    
                } catch (e: Exception) {
                    updateLogger.e("update", "Erro na tentativa $attempts", e)
                }
                
                if (attempts < MAX_UPDATE_ATTEMPTS) {
                    // Pausa entre tentativas
                    delay((2000 * attempts).toLong())
                }
            }
            
            // Todas as tentativas falharam
            val duration = System.currentTimeMillis() - startTime
            
            // Tentar rollback
            val rollbackResult = rollbackManager.executeRollback(
                RollbackReason.UPDATE_FAILED,
                backupInfo
            )
            
            updateLogger.e("update", "Todas as tentativas de atualização falharam", null, mapOf(
                "total_attempts" to attempts,
                "rollback_success" to rollbackResult.success
            ))
            
            AdvancedUpdateResult(
                success = false,
                reason = UpdateFailureReason.ALL_ATTEMPTS_FAILED,
                message = "Todas as $attempts tentativas falharam",
                duration = duration,
                attempts = attempts,
                rollbackResult = rollbackResult
            )
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            updateLogger.e("update", "Erro crítico durante atualização", e)
            
            AdvancedUpdateResult(
                success = false,
                reason = UpdateFailureReason.CRITICAL_ERROR,
                message = "Erro crítico: ${e.message}",
                duration = duration,
                error = e
            )
        }
    }
    
    /**
     * Executa atualização incremental
     */
    private suspend fun performIncrementalUpdate(
        updateInfo: UpdateInfo,
        onProgress: (UpdateProgress) -> Unit
    ): UpdateResult = withContext(Dispatchers.IO) {
        
        try {
            updateLogger.i("update", "Iniciando atualização incremental")
            
            // Criar informações do patch (em produção, viriam do servidor)
            val patchInfo = PatchInfo(
                fileName = "patch_${updateInfo.versionName}.patch",
                patchUrl = updateInfo.downloadUrl.replace(".apk", ".patch"),
                patchSize = updateInfo.fileSize / 10, // Estimativa: 10% do tamanho
                checksumSha256 = "", // Viria do servidor
                originalChecksumSha256 = "", // Verificado localmente
                fromVersionCode = getCurrentVersionCode(),
                toVersionCode = updateInfo.versionCode,
                type = "bsdiff",
                isCompressed = true
            )
            
            val currentApk = getCurrentApkFile()
            if (currentApk == null) {
                return@withContext UpdateResult(
                    success = false,
                    errorMessage = "Não foi possível localizar APK atual"
                )
            }
            
            // Aplicar patch
            val patchResult = patchManager.applyPatch(
                currentApk,
                patchInfo
            ) { progress ->
                onProgress(UpdateProgress(20 + progress / 2, "Aplicando patch..."))
            }
            
            if (patchResult.success) {
                UpdateResult(
                    success = true,
                    updatedFile = patchResult.patchedFile,
                    isIncremental = true,
                    compressionRatio = patchResult.compressionRatio
                )
            } else {
                UpdateResult(
                    success = false,
                    errorMessage = patchResult.error
                )
            }
            
        } catch (e: Exception) {
            updateLogger.e("update", "Erro na atualização incremental", e)
            UpdateResult(
                success = false,
                errorMessage = "Erro na atualização incremental: ${e.message}"
            )
        }
    }
    
    /**
     * Executa atualização completa (download do APK)
     */
    private suspend fun performFullUpdate(
        updateInfo: UpdateInfo,
        onProgress: (UpdateProgress) -> Unit
    ): UpdateResult = withContext(Dispatchers.IO) {
        
        try {
            updateLogger.i("update", "Iniciando atualização completa")
            
            val currentVersionCode = getCurrentVersionCode()
            
            // Baixar APK usando UpdateManager existente
            val downloadedFile = basicUpdateManager.downloadUpdate(
                updateInfo,
                currentVersionCode
            ) { progress ->
                onProgress(UpdateProgress(20 + progress / 2, "Baixando atualização..."))
            }
            
            if (downloadedFile == null) {
                return@withContext UpdateResult(
                    success = false,
                    errorMessage = "Falha no download da atualização"
                )
            }
            
            // Verificar integridade do APK baixado
            onProgress(UpdateProgress(70, "Verificando integridade..."))
            val integrityResult = integrityChecker.verifyApkIntegrity(downloadedFile)
            
            if (!integrityResult.isValid) {
                downloadedFile.delete()
                return@withContext UpdateResult(
                    success = false,
                    errorMessage = "APK baixado falhou na verificação de integridade: ${integrityResult.errorMessage}"
                )
            }
            
            UpdateResult(
                success = true,
                updatedFile = downloadedFile,
                isIncremental = false
            )
            
        } catch (e: Exception) {
            updateLogger.e("update", "Erro na atualização completa", e)
            UpdateResult(
                success = false,
                errorMessage = "Erro na atualização completa: ${e.message}"
            )
        }
    }
    
    /**
     * Verifica pré-requisitos para atualização
     */
    private suspend fun checkPrerequisites(updateInfo: UpdateInfo): PrerequisitesResult = withContext(Dispatchers.IO) {
        val missingRequirements = mutableListOf<String>()
        
        // Verificar espaço disponível
        val requiredSpace = updateInfo.fileSize + (50 * 1024 * 1024) // APK + 50MB extras
        val availableSpace = getAvailableSpace()
        
        if (availableSpace < requiredSpace) {
            missingRequirements.add("Espaço insuficiente (necessário: ${requiredSpace / 1024 / 1024}MB, disponível: ${availableSpace / 1024 / 1024}MB)")
        }
        
        // Verificar nível de bateria (se disponível)
        val batteryLevel = getBatteryLevel()
        if (batteryLevel > 0 && batteryLevel < MIN_BATTERY_LEVEL) {
            missingRequirements.add("Bateria baixa (mínimo: $MIN_BATTERY_LEVEL%, atual: $batteryLevel%)")
        }
        
        // Verificar conexão WiFi para atualizações grandes
        if (updateInfo.fileSize > 20 * 1024 * 1024 && MIN_WIFI_CONNECTION) {
            val isWifi = isWifiConnected()
            if (!isWifi) {
                missingRequirements.add("Atualizações grandes requerem conexão WiFi")
            }
        }
        
        // Verificar versão compatível
        val currentVersion = getCurrentVersionCode()
        if (currentVersion >= updateInfo.versionCode) {
            missingRequirements.add("Versão atual ($currentVersion) já é igual ou superior à desejada (${updateInfo.versionCode})")
        }
        
        PrerequisitesResult(
            satisfied = missingRequirements.isEmpty(),
            missingRequirements = missingRequirements,
            availableSpace = availableSpace,
            batteryLevel = batteryLevel,
            isWifiConnected = isWifiConnected()
        )
    }
    
    /**
     * Verifica sucesso da atualização
     */
    private suspend fun verifyUpdateSuccess(updateInfo: UpdateInfo): VerificationResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        
        try {
            // Verificar se o novo APK está instalado
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            
            val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            
            if (currentVersionCode != updateInfo.versionCode) {
                errors.add("Versão instalada ($currentVersionCode) não corresponde à esperada (${updateInfo.versionCode})")
            }
            
            // Verificar se o aplicativo pode ser iniciado
            val appInfo = packageManager.getApplicationInfo(context.packageName, 0)
            if (appInfo == null) {
                errors.add("Não foi possível obter informações do aplicativo")
            }
            
            // Verificar integridade do banco de dados
            val integrityResult = migrationManager.verifyDataIntegrity()
            if (!integrityResult.isValid) {
                errors.addAll(integrityResult.issues)
            }
            
            VerificationResult(
                success = errors.isEmpty(),
                errors = errors,
                installedVersionCode = currentVersionCode,
                expectedVersionCode = updateInfo.versionCode
            )
            
        } catch (e: Exception) {
            updateLogger.e("update", "Erro na verificação de atualização", e)
            VerificationResult(
                success = false,
                errors = listOf("Erro na verificação: ${e.message}")
            )
        }
    }
    
    /**
     * Limpeza pós-atualização bem-sucedida
     */
    private suspend fun cleanupAfterSuccessfulUpdate(backupInfo: BackupInfo) = withContext(Dispatchers.IO) {
        try {
            updateLogger.i("update", "Iniciando limpeza pós-atualização")
            
            // Manter backup por um tempo como segurança
            // backupManager.deleteBackup(backupInfo) // Não deletar imediatamente
            
            // Limpar arquivos temporários
            cleanupTempFiles()
            
            // Limpar caches
            cleanupCaches()
            
            updateLogger.i("update", "Limpeza pós-atualização concluída")
            
        } catch (e: Exception) {
            updateLogger.e("update", "Erro na limpeza pós-atualização", e)
        }
    }
    
    /**
     * Obtém código da versão atual
     */
    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Obtém arquivo APK atual
     */
    private fun getCurrentApkFile(): File? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            File(applicationInfo.sourceDir)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Obtém espaço disponível
     */
    private fun getAvailableSpace(): Long {
        return try {
            val downloadDir = context.getExternalFilesDir(null)
            val stat = android.os.StatFs(downloadDir?.absolutePath ?: context.filesDir.absolutePath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stat.availableBytes
            } else {
                @Suppress("DEPRECATION")
                stat.availableBlocks.toLong() * stat.blockSize
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Obtém nível de bateria
     */
    private fun getBatteryLevel(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? android.os.BatteryManager
            batteryManager?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        } catch (e: Exception) {
            -1
        }
    }
    
    /**
     * Verifica se está conectado ao WiFi
     */
    private fun isWifiConnected(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetworkInfo
            activeNetwork?.type == android.net.ConnectivityManager.TYPE_WIFI
        } catch (e: Exception) {
            true // Assumir WiFi se não for possível verificar
        }
    }
    
    /**
     * Limpa arquivos temporários
     */
    private suspend fun cleanupTempFiles() = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "temp_updates")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.delete()) {
                        updateLogger.d("update", "Arquivo temporário removido: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            updateLogger.e("update", "Erro ao limpar arquivos temporários", e)
        }
    }
    
    /**
     * Limpa caches
     */
    private suspend fun cleanupCaches() = withContext(Dispatchers.IO) {
        try {
            // Limpar cache do aplicativo
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    updateLogger.d("update", "Cache removido: ${file.name}")
                }
            }
        } catch (e: Exception) {
            updateLogger.e("update", "Erro ao limpar caches", e)
        }
    }
}

/**
 * Progresso da atualização
 */
data class UpdateProgress(
    val percentage: Int,
    val message: String
)

/**
 * Resultado de verificação de pré-requisitos
 */
data class PrerequisitesResult(
    val satisfied: Boolean,
    val missingRequirements: List<String>,
    val availableSpace: Long,
    val batteryLevel: Int,
    val isWifiConnected: Boolean
)

/**
 * Resultado da verificação de atualização
 */
data class VerificationResult(
    val success: Boolean,
    val errors: List<String>,
    val installedVersionCode: Int = 0,
    val expectedVersionCode: Int = 0
)

/**
 * Resultado da atualização
 */
data class UpdateResult(
    val success: Boolean,
    val updatedFile: File? = null,
    val isIncremental: Boolean = false,
    val compressionRatio: Double = 0.0,
    val errorMessage: String? = null
)

/**
 * Razões para falha de atualização
 */
enum class UpdateFailureReason {
    PREREQUISITES_NOT_MET,
    BACKUP_FAILED,
    DOWNLOAD_FAILED,
    INTEGRITY_CHECK_FAILED,
    VERIFICATION_FAILED,
    MIGRATION_FAILED,
    ALL_ATTEMPTS_FAILED,
    CRITICAL_ERROR
}

/**
 * Resultado completo da atualização avançada
 */
data class AdvancedUpdateResult(
    val success: Boolean,
    val reason: UpdateFailureReason? = null,
    val message: String? = null,
    val updateInfo: UpdateInfo? = null,
    val backupInfo: BackupInfo? = null,
    val isIncremental: Boolean = false,
    val attempts: Int = 0,
    val duration: Long = 0,
    val migrationResult: com.example.minhascompras.data.migration.MigrationResult? = null,
    val rollbackResult: com.example.minhascompras.data.update.RollbackResult? = null,
    val error: Exception? = null
) {
    /**
     * Formata duração para exibição
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        return "${seconds}s"
    }
    
    /**
     * Retorna resumo formatado
     */
    fun getSummary(): String {
        return if (success) {
            "Atualização concluída com sucesso em $attempts tentativas (${getFormattedDuration()})"
        } else {
            "Atualização falhou: ${message ?: "Erro desconhecido"}"
        }
    }
}