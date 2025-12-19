package com.example.minhascompras.data.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.minhascompras.MainActivity
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Gerenciador de rollback automático para falhas em atualizações
 * Coordena o processo de restauração do estado anterior do aplicativo
 */
class RollbackManager(private val context: Context) {
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "rollback_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Atualizações do App"
        private const val NOTIFICATION_ID_ROLLBACK_SUCCESS = 3001
        private const val NOTIFICATION_ID_ROLLBACK_FAILED = 3002
        private const val NOTIFICATION_ID_ROLLBACK_PROGRESS = 3003
        
        // Tempos limite para operações
        private const val ROLLBACK_TIMEOUT_MS = 60000L // 1 minuto
        private const val VERIFICATION_DELAY_MS = 5000L // 5 segundos para verificação
        private const val MAX_ROLLBACK_ATTEMPTS = 3
        
        // Códigos de resultado
        private const val RESULT_SUCCESS = 0
        private const val RESULT_FAILURE = 1
        private const val RESULT_PARTIAL = 2
    }
    
    private val backupManager = BackupManager(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Executa rollback automático completo
     * @param reason Motivo do rollback
     * @param specificBackup Backup específico para restaurar (opcional)
     * @return RollbackResult com resultado da operação
     */
    suspend fun executeRollback(
        reason: RollbackReason,
        specificBackup: BackupInfo? = null
    ): RollbackResult = withContext(Dispatchers.IO) {
        
        Logger.w("RollbackManager", "Iniciando rollback automático. Motivo: ${reason.description}")
        
        val startTime = System.currentTimeMillis()
        var attempts = 0
        var lastError: Exception? = null
        
        // Notificar início do rollback
        showRollbackProgressNotification("Iniciando restauração...")
        
        try {
            // 1. Verificar disponibilidade de backups
            val availableBackups = if (specificBackup != null) {
                listOf(specificBackup)
            } else {
                backupManager.listBackups()
            }
            
            if (availableBackups.isEmpty()) {
                Logger.e("RollbackManager", "Nenhum backup disponível para rollback")
                showRollbackFailedNotification("Nenhum backup disponível")
                return@withContext RollbackResult(
                    success = false,
                    reason = reason,
                    attempts = attempts,
                    duration = System.currentTimeMillis() - startTime,
                    errorMessage = "Nenhum backup disponível"
                )
            }
            
            // 2. Tentar rollback com backups disponíveis
            for (backup in availableBackups.take(MAX_ROLLBACK_ATTEMPTS)) {
                attempts++
                
                try {
                    Logger.i("RollbackManager", "Tentativa $attempts/$MAX_ROLLBACK_ATTEMPTS com backup: ${backup.fileName}")
                    
                    updateRollbackProgressNotification("Restaurando backup ${attempts}/${MAX_ROLLBACK_ATTEMPTS}...")
                    
                    // Executar restauração
                    val restoreSuccess = backupManager.restoreBackup(backup)
                    
                    if (restoreSuccess) {
                        // Aguardar um pouco para verificação
                        delay(VERIFICATION_DELAY_MS)
                        
                        // Verificar se restauração foi bem-sucedida
                        if (verifyRollbackSuccess()) {
                            Logger.i("RollbackManager", "Rollback concluído com sucesso na tentativa $attempts")
                            
                            val duration = System.currentTimeMillis() - startTime
                            
                            // Limpar backups antigos se necessário
                            cleanupAfterSuccessfulRollback(backup)
                            
                            // Notificar sucesso
                            showRollbackSuccessNotification(backup, attempts, duration)
                            
                            return@withContext RollbackResult(
                                success = true,
                                reason = reason,
                                attempts = attempts,
                                duration = duration,
                                restoredBackup = backup
                            )
                        } else {
                            Logger.w("RollbackManager", "Verificação pós-rollback falhou na tentativa $attempts")
                        }
                    } else {
                        Logger.w("RollbackManager", "Restauração falhou na tentativa $attempts")
                    }
                    
                } catch (e: Exception) {
                    lastError = e
                    Logger.e("RollbackManager", "Erro na tentativa $attempts de rollback", e)
                }
                
                // Pequena pausa entre tentativas
                if (attempts < availableBackups.size && attempts < MAX_ROLLBACK_ATTEMPTS) {
                    delay(2000)
                }
            }
            
            // Todas as tentativas falharam
            val duration = System.currentTimeMillis() - startTime
            val errorMessage = lastError?.message ?: "Todas as tentativas de rollback falharam"
            
            Logger.e("RollbackManager", "Rollback falhou após $attempts tentativas")
            showRollbackFailedNotification(errorMessage)
            
            RollbackResult(
                success = false,
                reason = reason,
                attempts = attempts,
                duration = duration,
                errorMessage = errorMessage,
                lastError = lastError
            )
            
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Erro crítico durante rollback", e)
            
            val duration = System.currentTimeMillis() - startTime
            showRollbackFailedNotification("Erro crítico: ${e.message}")
            
            RollbackResult(
                success = false,
                reason = reason,
                attempts = attempts,
                duration = duration,
                errorMessage = "Erro crítico durante rollback",
                lastError = e
            )
        }
    }
    
    /**
     * Verifica se o rollback foi bem-sucedido
     */
    private suspend fun verifyRollbackSuccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d("RollbackManager", "Verificando sucesso do rollback...")
            
            // 1. Verificar se o aplicativo pode ser inicializado
            val packageManager = context.packageManager
            val packageInfo = try {
                packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: Exception) {
                Logger.e("RollbackManager", "Não foi possível obter informações do pacote", e)
                return@withContext false
            }
            
            // 2. Verificar se o banco de dados está acessível
            val databaseAccessible = try {
                // Tentar acessar o banco de dados
                val dbFile = context.getDatabasePath("compras_database")
                dbFile.exists() && dbFile.canRead()
            } catch (e: Exception) {
                Logger.e("RollbackManager", "Banco de dados não acessível", e)
                false
            }
            
            // 3. Verificar se as preferências estão acessíveis
            val preferencesAccessible = try {
                val prefs = context.getSharedPreferences("minhas_compras_prefs", Context.MODE_PRIVATE)
                prefs.getBoolean("rollback_verification", false) // Teste de leitura/escrita
                true
            } catch (e: Exception) {
                Logger.e("RollbackManager", "Preferências não acessíveis", e)
                false
            }
            
            val success = packageInfo != null && databaseAccessible && preferencesAccessible
            
            Logger.i("RollbackManager", "Verificação de rollback: ${if (success) "Sucesso" else "Falha"}")
            Logger.d("RollbackManager", "Package info: ${packageInfo != null}")
            Logger.d("RollbackManager", "Database accessible: $databaseAccessible")
            Logger.d("RollbackManager", "Preferences accessible: $preferencesAccessible")
            
            success
            
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Erro na verificação de rollback", e)
            false
        }
    }
    
    /**
     * Limpeza pós-rollback bem-sucedido
     */
    private suspend fun cleanupAfterSuccessfulRollback(restoredBackup: BackupInfo) {
        try {
            Logger.d("RollbackManager", "Realizando limpeza pós-rollback...")
            
            // 1. Manter apenas o backup restaurado e mais um recente
            val backups = backupManager.listBackups()
            val backupsToDelete = backups.filter { it.fileName != restoredBackup.fileName }
                .drop(1) // Manter o mais recente após o restaurado
            
            for (backup in backupsToDelete) {
                backupManager.deleteBackup(backup)
                Logger.d("RollbackManager", "Removido backup pós-rollback: ${backup.fileName}")
            }
            
            // 2. Limpar arquivos temporários de atualização
            cleanupTempUpdateFiles()
            
            Logger.i("RollbackManager", "Limpeza pós-rollback concluída")
            
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Erro na limpeza pós-rollback", e)
        }
    }
    
    /**
     * Limpa arquivos temporários de atualização
     */
    private suspend fun cleanupTempUpdateFiles() {
        try {
            val updateDir = File(context.getExternalFilesDir(null), "updates")
            if (updateDir.exists()) {
                updateDir.listFiles { file ->
                    file.isFile && (file.name.endsWith(".apk") || file.name.endsWith(".tmp"))
                }?.forEach { file ->
                    if (file.delete()) {
                        Logger.d("RollbackManager", "Arquivo temporário removido: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Erro ao limpar arquivos temporários", e)
        }
    }
    
    /**
     * Verifica se rollback é necessário baseado no estado do aplicativo
     */
    suspend fun shouldRollback(): RollbackAssessment = withContext(Dispatchers.IO) {
        try {
            Logger.d("RollbackManager", "Avaliando necessidade de rollback...")
            
            val issues = mutableListOf<RollbackIssue>()
            
            // 1. Verificar se o aplicativo está respondendo
            val appResponsive = isAppResponsive()
            if (!appResponsive) {
                issues.add(RollbackIssue.APP_NOT_RESPONDING)
            }
            
            // 2. Verificar integridade do banco de dados
            val databaseIntegrity = checkDatabaseIntegrity()
            if (!databaseIntegrity) {
                issues.add(RollbackIssue.DATABASE_CORRUPTED)
            }
            
            // 3. Verificar se há falhas críticas nas preferências
            val preferencesIntegrity = checkPreferencesIntegrity()
            if (!preferencesIntegrity) {
                issues.add(RollbackIssue.PREFERENCES_CORRUPTED)
            }
            
            // 4. Verificar se há atualização em andamento que falhou
            val updateInProgress = isUpdateInProgress()
            if (updateInProgress) {
                issues.add(RollbackIssue.UPDATE_FAILED)
            }
            
            val shouldRollback = issues.isNotEmpty()
            val reason = when {
                issues.contains(RollbackIssue.UPDATE_FAILED) -> RollbackReason.UPDATE_FAILED
                issues.contains(RollbackIssue.APP_NOT_RESPONDING) -> RollbackReason.APP_CRASH
                issues.contains(RollbackIssue.DATABASE_CORRUPTED) -> RollbackReason.DATA_CORRUPTION
                issues.contains(RollbackIssue.PREFERENCES_CORRUPTED) -> RollbackReason.SETTINGS_CORRUPTED
                else -> RollbackReason.UNKNOWN
            }
            
            Logger.i("RollbackManager", "Avaliação de rollback: ${if (shouldRollback) "Necessário" else "Não necessário"}")
            if (shouldRollback) {
                Logger.w("RollbackManager", "Issues detectadas: ${issues.joinToString(", ")}")
            }
            
            RollbackAssessment(
                shouldRollback = shouldRollback,
                reason = reason,
                issues = issues
            )
            
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Erro na avaliação de rollback", e)
            RollbackAssessment(
                shouldRollback = true,
                reason = RollbackReason.UNKNOWN,
                issues = listOf(RollbackIssue.ASSESSMENT_ERROR)
            )
        }
    }
    
    /**
     * Verifica se o aplicativo está responsivo
     */
    private suspend fun isAppResponsive(): Boolean {
        return try {
            // Tentar operações básicas do aplicativo
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(context.packageName, 0)
            appInfo != null
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Aplicativo não responsivo", e)
            false
        }
    }
    
    /**
     * Verifica integridade do banco de dados
     */
    private suspend fun checkDatabaseIntegrity(): Boolean {
        return try {
            val dbFile = context.getDatabasePath("compras_database")
            dbFile.exists() && dbFile.canRead() && dbFile.length() > 0
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Banco de dados corrompido", e)
            false
        }
    }
    
    /**
     * Verifica integridade das preferências
     */
    private suspend fun checkPreferencesIntegrity(): Boolean {
        return try {
            val prefs = context.getSharedPreferences("minhas_compras_prefs", Context.MODE_PRIVATE)
            prefs.getBoolean("integrity_check", true) // Teste de acesso
            true
        } catch (e: Exception) {
            Logger.e("RollbackManager", "Preferências corrompidas", e)
            false
        }
    }
    
    /**
     * Verifica se há atualização em andamento que falhou
     */
    private suspend fun isUpdateInProgress(): Boolean {
        return try {
            val updateDir = File(context.getExternalFilesDir(null), "updates")
            updateDir.exists() && updateDir.listFiles { file ->
                file.isFile && file.name.endsWith(".tmp")
            }?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Cria canal de notificação para rollback
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações sobre atualizações e rollback do aplicativo"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Mostra notificação de progresso do rollback
     */
    private fun showRollbackProgressNotification(message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Restaurando Aplicativo")
            .setContentText(message)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ROLLBACK_PROGRESS, notification)
    }
    
    /**
     * Atualiza notificação de progresso
     */
    private fun updateRollbackProgressNotification(message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Restaurando Aplicativo")
            .setContentText(message)
            .setProgress(100, 50, true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ROLLBACK_PROGRESS, notification)
    }
    
    /**
     * Mostra notificação de sucesso do rollback
     */
    private fun showRollbackSuccessNotification(
        backup: BackupInfo,
        attempts: Int,
        duration: Long
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val message = "Restaurado para versão ${backup.appVersion} em ${attempts} tentativas"
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Aplicativo Restaurado")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$message\n\nBackup: ${backup.getFormattedTimestamp()}\nDuração: ${duration}ms"
            ))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ROLLBACK_SUCCESS, notification)
        
        // Limpar notificação de progresso
        notificationManager.cancel(NOTIFICATION_ID_ROLLBACK_PROGRESS)
    }
    
    /**
     * Mostra notificação de falha do rollback
     */
    private fun showRollbackFailedNotification(errorMessage: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Falha na Restauração")
            .setContentText("Não foi possível restaurar o aplicativo")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Erro: $errorMessage\n\nPor favor, reinstale o aplicativo manualmente."
            ))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_ROLLBACK_FAILED, notification)
        
        // Limpar notificação de progresso
        notificationManager.cancel(NOTIFICATION_ID_ROLLBACK_PROGRESS)
    }
}

/**
 * Motivos para rollback
 */
enum class RollbackReason(val description: String) {
    UPDATE_FAILED("Falha na atualização"),
    APP_CRASH("Aplicativo travando"),
    DATA_CORRUPTION("Dados corrompidos"),
    SETTINGS_CORRUPTED("Configurações corrompidas"),
    USER_REQUESTED("Solicitado pelo usuário"),
    UNKNOWN("Motivo desconhecido")
}

/**
 * Issues que podem requerer rollback
 */
enum class RollbackIssue {
    APP_NOT_RESPONDING,
    DATABASE_CORRUPTED,
    PREFERENCES_CORRUPTED,
    UPDATE_FAILED,
    ASSESSMENT_ERROR
}

/**
 * Resultado de uma operação de rollback
 */
data class RollbackResult(
    val success: Boolean,
    val reason: RollbackReason,
    val attempts: Int,
    val duration: Long,
    val errorMessage: String? = null,
    val lastError: Exception? = null,
    val restoredBackup: BackupInfo? = null
) {
    /**
     * Formata a duração para exibição
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        return "${seconds}s"
    }
    
    /**
     * Retorna mensagem formatada do resultado
     */
    fun getResultMessage(): String {
        return if (success) {
            "Rollback concluído com sucesso em $attempts tentativas (${getFormattedDuration()})"
        } else {
            "Rollback falhou após $attempts tentativas: ${errorMessage ?: "Erro desconhecido"}"
        }
    }
}

/**
 * Avaliação da necessidade de rollback
 */
data class RollbackAssessment(
    val shouldRollback: Boolean,
    val reason: RollbackReason,
    val issues: List<RollbackIssue>
) {
    /**
     * Retorna descrição dos issues
     */
    fun getIssuesDescription(): String {
        return issues.joinToString(", ") { issue ->
            when (issue) {
                RollbackIssue.APP_NOT_RESPONDING -> "App não respondendo"
                RollbackIssue.DATABASE_CORRUPTED -> "Banco corrompido"
                RollbackIssue.PREFERENCES_CORRUPTED -> "Preferências corrompidas"
                RollbackIssue.UPDATE_FAILED -> "Atualização falhou"
                RollbackIssue.ASSESSMENT_ERROR -> "Erro na avaliação"
            }
        }
    }
}