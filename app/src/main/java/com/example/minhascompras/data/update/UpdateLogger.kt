package com.example.minhascompras.data.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * Sistema de logging especializado para auditoria de atualizações
 * Registra todas as etapas do processo de atualização para análise e debugging
 */
class UpdateLogger(private val context: Context) {
    
    companion object {
        private const val LOG_DIR = "update_logs"
        private const val LOG_FILE_PREFIX = "update_log_"
        private const val LOG_FILE_EXTENSION = ".json"
        private const val MAX_LOG_FILES = 10
        private const val MAX_LOG_ENTRIES = 1000
        private const val MAX_MEMORY_LOGS = 100
        
        // Níveis de log
        private const val LEVEL_DEBUG = 0
        private const val LEVEL_INFO = 1
        private const val LEVEL_WARNING = 2
        private const val LEVEL_ERROR = 3
        private const val LEVEL_CRITICAL = 4
        
        // Categorias de log
        const val CATEGORY_BACKUP = "backup"
        const val CATEGORY_INTEGRITY = "integrity"
        const val CATEGORY_DOWNLOAD = "download"
        const val CATEGORY_INSTALLATION = "installation"
        const val CATEGORY_ROLLBACK = "rollback"
        const val CATEGORY_MIGRATION = "migration"
        const val CATEGORY_NETWORK = "network"
        const val CATEGORY_SECURITY = "security"
        const val CATEGORY_PERFORMANCE = "performance"
        const val CATEGORY_USER_ACTION = "user_action"
    }
    
    private val logDir = File(context.filesDir, LOG_DIR)
    private val memoryLogs = ConcurrentLinkedQueue<UpdateLogEntry>()
    private val logSequence = AtomicLong(0)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    init {
        // Criar diretório de logs se não existir
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        
        // Limpar logs antigos ao inicializar
        GlobalScope.launch(Dispatchers.IO) {
            cleanupOldLogs()
        }
    }
    
    /**
     * Registra log de debug
     */
    suspend fun d(category: String, message: String, details: Map<String, Any> = emptyMap()) {
        log(LEVEL_DEBUG, category, message, details)
    }
    
    /**
     * Registra log de informação
     */
    suspend fun i(category: String, message: String, details: Map<String, Any> = emptyMap()) {
        log(LEVEL_INFO, category, message, details)
    }
    
    /**
     * Registra log de aviso
     */
    suspend fun w(category: String, message: String, details: Map<String, Any> = emptyMap()) {
        log(LEVEL_WARNING, category, message, details)
    }
    
    /**
     * Registra log de erro
     */
    suspend fun e(category: String, message: String, error: Throwable? = null, details: Map<String, Any> = emptyMap()) {
        val errorDetails = if (error != null) {
            details + mapOf(
                "error_type" to error::class.java.simpleName,
                "error_message" to error.message,
                "stack_trace" to error.stackTraceToString()
            )
        } else {
            details
        }
        log(LEVEL_ERROR, category, message, errorDetails as Map<String, Any>)
    }
    
    /**
     * Registra log crítico
     */
    suspend fun c(category: String, message: String, error: Throwable? = null, details: Map<String, Any> = emptyMap()) {
        val errorDetails = if (error != null) {
            details + mapOf(
                "error_type" to error::class.java.simpleName,
                "error_message" to error.message,
                "stack_trace" to error.stackTraceToString()
            )
        } else {
            details
        }
        log(LEVEL_CRITICAL, category, message, errorDetails as Map<String, Any>)
    }
    
    /**
     * Registra início de uma operação
     */
    suspend fun logOperationStart(
        operation: String,
        category: String,
        details: Map<String, Any> = emptyMap()
    ): String {
        val operationId = generateOperationId(operation)
        val startDetails = details + mapOf(
            "operation_id" to operationId,
            "operation_type" to "start",
            "operation_name" to operation
        )
        
        log(LEVEL_INFO, category, "Iniciando operação: $operation", startDetails)
        return operationId
    }
    
    /**
     * Registra fim de uma operação
     */
    suspend fun logOperationEnd(
        operationId: String,
        operation: String,
        category: String,
        success: Boolean,
        duration: Long,
        details: Map<String, Any> = emptyMap()
    ) {
        val endDetails = details + mapOf(
            "operation_id" to operationId,
            "operation_type" to "end",
            "operation_name" to operation,
            "success" to success,
            "duration_ms" to duration
        )
        
        val level = if (success) LEVEL_INFO else LEVEL_ERROR
        val message = "Operação ${if (success) "concluída" else "falhou"}: $operation"
        log(level, category, message, endDetails)
    }
    
    /**
     * Registra métricas de performance
     */
    suspend fun logPerformance(
        operation: String,
        duration: Long,
        details: Map<String, Any> = emptyMap()
    ) {
        val performanceDetails = details + mapOf(
            "operation" to operation,
            "duration_ms" to duration,
            "performance_category" to "timing"
        )
        
        log(LEVEL_INFO, CATEGORY_PERFORMANCE, "Performance: $operation", performanceDetails)
    }
    
    /**
     * Registra eventos de rede
     */
    suspend fun logNetworkEvent(
        event: String,
        url: String,
        statusCode: Int? = null,
        duration: Long? = null,
        bytesTransferred: Long? = null,
        error: Throwable? = null
    ) {
        val networkDetails = mutableMapOf<String, Any>(
            "network_event" to event,
            "url" to url
        )
        
        statusCode?.let { networkDetails["status_code"] = it }
        duration?.let { networkDetails["duration_ms"] = it }
        bytesTransferred?.let { networkDetails["bytes_transferred"] = it }
        error?.let {
            networkDetails["error_type"] = it::class.java.simpleName
            networkDetails["error_message"] = it.message ?: ""
        }
        
        val level = when {
            error != null -> LEVEL_ERROR
            statusCode != null && statusCode >= 400 -> LEVEL_WARNING
            else -> LEVEL_INFO
        }
        
        log(level, CATEGORY_NETWORK, "Network: $event", networkDetails)
    }
    
    /**
     * Registra eventos de segurança
     */
    suspend fun logSecurityEvent(
        event: String,
        severity: SecuritySeverity,
        details: Map<String, Any> = emptyMap()
    ) {
        val securityDetails = details + mapOf(
            "security_event" to event,
            "severity" to severity.name,
            "security_category" to "audit"
        )
        
        val level = when (severity) {
            SecuritySeverity.LOW -> LEVEL_INFO
            SecuritySeverity.MEDIUM -> LEVEL_WARNING
            SecuritySeverity.HIGH -> LEVEL_ERROR
            SecuritySeverity.CRITICAL -> LEVEL_CRITICAL
        }
        
        log(level, CATEGORY_SECURITY, "Security: $event", securityDetails)
    }
    
    /**
     * Registra ações do usuário
     */
    suspend fun logUserAction(
        action: String,
        details: Map<String, Any> = emptyMap()
    ) {
        val userActionDetails = details + mapOf(
            "user_action" to action,
            "timestamp_user" to System.currentTimeMillis()
        )
        
        log(LEVEL_INFO, CATEGORY_USER_ACTION, "User: $action", userActionDetails)
    }
    
    /**
     * Função principal de logging
     */
    private suspend fun log(
        level: Int,
        category: String,
        message: String,
        details: Map<String, Any>
    ) = withContext(Dispatchers.IO) {
        
        try {
            val timestamp = System.currentTimeMillis()
            val sequence = logSequence.incrementAndGet()
            
            // Criar entrada de log
            val logEntry = UpdateLogEntry(
                timestamp = timestamp,
                sequence = sequence,
                level = level,
                category = category,
                message = message,
                details = details,
                threadName = Thread.currentThread().name,
                appVersion = getAppVersion(),
                deviceInfo = getDeviceInfo()
            )
            
            // Adicionar aos logs em memória
            memoryLogs.offer(logEntry)
            
            // Manter apenas os logs mais recentes em memória
            while (memoryLogs.size > MAX_MEMORY_LOGS) {
                memoryLogs.poll()
            }
            
            // Escrever para arquivo
            writeLogToFile(logEntry)
            
            // Tamb enviar para o Logger principal baseado no nível
            when (level) {
                LEVEL_DEBUG -> Logger.d("UpdateLogger", "[$category] $message")
                LEVEL_INFO -> Logger.i("UpdateLogger", "[$category] $message")
                LEVEL_WARNING -> Logger.w("UpdateLogger", "[$category] $message")
                LEVEL_ERROR, LEVEL_CRITICAL -> Logger.e("UpdateLogger", "[$category] $message")
            }
            
        } catch (e: Exception) {
            Logger.e("UpdateLogger", "Erro ao registrar log", e)
        }
    }
    
    /**
     * Escreve entrada de log para arquivo
     */
    private suspend fun writeLogToFile(logEntry: UpdateLogEntry) = withContext(Dispatchers.IO) {
        try {
            val logFile = getCurrentLogFile()
            
            // Converter entrada para JSON
            val json = logEntry.toJson()
            
            // Anexar ao arquivo
            logFile.appendText("$json\n")
            
            // Verificar se precisa rotacionar o arquivo
            if (logFile.length() > 5 * 1024 * 1024) { // 5MB
                rotateLogFile()
            }
            
        } catch (e: Exception) {
            Logger.e("UpdateLogger", "Erro ao escrever log para arquivo", e)
        }
    }
    
    /**
     * Obtém o arquivo de log atual
     */
    private fun getCurrentLogFile(): File {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fileName = "$LOG_FILE_PREFIX$today$LOG_FILE_EXTENSION"
        return File(logDir, fileName)
    }
    
    /**
     * Rotaciona arquivo de log quando fica muito grande
     */
    private suspend fun rotateLogFile() = withContext(Dispatchers.IO) {
        try {
            val currentFile = getCurrentLogFile()
            val timestamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
            val rotatedFileName = "${currentFile.nameWithoutExtension}_$timestamp$LOG_FILE_EXTENSION"
            val rotatedFile = File(logDir, rotatedFileName)
            
            currentFile.renameTo(rotatedFile)
            
            // Limpar logs antigos se necessário
            cleanupOldLogs()
            
        } catch (e: Exception) {
            Logger.e("UpdateLogger", "Erro ao rotacionar arquivo de log", e)
        }
    }
    
    /**
     * Limpa logs antigos
     */
    private suspend fun cleanupOldLogs() = withContext(Dispatchers.IO) {
        try {
            val logFiles = logDir.listFiles { file ->
                file.isFile && file.name.startsWith(LOG_FILE_PREFIX) && file.name.endsWith(LOG_FILE_EXTENSION)
            }?.sortedByDescending { it.lastModified() }
            
            if (logFiles != null && logFiles.size > MAX_LOG_FILES) {
                val filesToDelete = logFiles.drop(MAX_LOG_FILES)
                filesToDelete.forEach { file ->
                    if (file.delete()) {
                        Logger.d("UpdateLogger", "Arquivo de log antigo removido: ${file.name}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Logger.e("UpdateLogger", "Erro ao limpar logs antigos", e)
        }
    }
    
    /**
     * Gera ID único para operação
     */
    private fun generateOperationId(operation: String): String {
        val timestamp = System.currentTimeMillis()
        val hash = operation.hashCode().toString(16).uppercase()
        return "OP_${timestamp}_$hash"
    }
    
    /**
     * Obtém versão atual do aplicativo
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Obtém informações do dispositivo
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "android_version" to Build.VERSION.RELEASE,
            "api_level" to Build.VERSION.SDK_INT.toString(),
            "architecture" to if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "unknown"
        )
    }
    
    /**
     * Obtém logs recentes da memória
     */
    suspend fun getRecentLogs(limit: Int = 50): List<UpdateLogEntry> {
        return memoryLogs.toList().takeLast(limit)
    }
    
    /**
     * Obtém logs de uma categoria específica
     */
    suspend fun getLogsByCategory(category: String, limit: Int = 100): List<UpdateLogEntry> {
        return memoryLogs.filter { it.category == category }.toList().takeLast(limit)
    }
    
    /**
     * Obtém logs de um nível específico ou superior
     */
    suspend fun getLogsByMinLevel(minLevel: Int, limit: Int = 100): List<UpdateLogEntry> {
        return memoryLogs.filter { it.level >= minLevel }.toList().takeLast(limit)
    }
    
    /**
     * Exporta logs para arquivo JSON
     */
    suspend fun exportLogs(
        startTime: Long? = null,
        endTime: Long? = null,
        categories: List<String>? = null
    ): File? = withContext(Dispatchers.IO) {
        
        try {
            val filteredLogs = memoryLogs.filter { log ->
                val timeMatch = (startTime?.let { log.timestamp >= it } ?: true) &&
                                  (endTime?.let { log.timestamp <= it } ?: true)
                val categoryMatch = categories?.let { log.category in it } ?: true
                
                timeMatch && categoryMatch
            }
            
            val exportData = JSONObject().apply {
                put("export_timestamp", System.currentTimeMillis())
                put("app_version", getAppVersion())
                put("total_logs", filteredLogs.size)
                put("logs", JSONArray().apply {
                    filteredLogs.forEach { log ->
                        put(log.toJson())
                    }
                })
            }
            
            val exportFile = File(logDir, "export_${System.currentTimeMillis()}.json")
            exportFile.writeText(exportData.toString(2))
            
            Logger.i("UpdateLogger", "Logs exportados: ${exportFile.absolutePath}")
            exportFile
            
        } catch (e: Exception) {
            Logger.e("UpdateLogger", "Erro ao exportar logs", e)
            null
        }
    }
    
    /**
     * Limpa todos os logs
     */
    suspend fun clearAllLogs() = withContext(Dispatchers.IO) {
        try {
            memoryLogs.clear()
            
            logDir.listFiles { file ->
                file.isFile && file.name.startsWith(LOG_FILE_PREFIX)
            }?.forEach { file ->
                if (file.delete()) {
                    Logger.d("UpdateLogger", "Arquivo de log removido: ${file.name}")
                }
            }
            
            Logger.i("UpdateLogger", "Todos os logs foram limpos")
            
        } catch (e: Exception) {
            Logger.e("UpdateLogger", "Erro ao limpar logs", e)
        }
    }
}

/**
 * Níveis de severidade de segurança
 */
enum class SecuritySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Entrada de log de atualização
 */
data class UpdateLogEntry(
    val timestamp: Long,
    val sequence: Long,
    val level: Int,
    val category: String,
    val message: String,
    val details: Map<String, Any>,
    val threadName: String,
    val appVersion: String,
    val deviceInfo: Map<String, String>
) {
    /**
     * Converte entrada para JSON
     */
    fun toJson(): String {
        val json = JSONObject().apply {
            put("timestamp", timestamp)
            put("sequence", sequence)
            put("level", getLevelName())
            put("category", category)
            put("message", message)
            put("thread", threadName)
            put("app_version", appVersion)
            
            // Adicionar device info
            put("device", JSONObject().apply {
                deviceInfo.forEach { (key, value) ->
                    put(key, value)
                }
            })
            
            // Adicionar details
            put("details", JSONObject().apply {
                details.forEach { (key, value) ->
                    when (value) {
                        is String -> put(key, value)
                        is Number -> put(key, value)
                        is Boolean -> put(key, value)
                        is List<*> -> put(key, JSONArray(value))
                        is Map<*, *> -> {
                            val mapValue = value as Map<String, Any>
                            put(key, JSONObject().apply {
                                mapValue.forEach { (k, v) ->
                                    when (v) {
                                        is String -> put(k, v)
                                        is Number -> put(k, v)
                                        is Boolean -> put(k, v)
                                        else -> put(k, v.toString())
                                    }
                                }
                            })
                        }
                        else -> put(key, value.toString())
                    }
                }
            })
        }
        
        return json.toString()
    }
    
    /**
     * Obtém nome do nível
     */
    private fun getLevelName(): String {
        return when (level) {
            0 -> "DEBUG"
            1 -> "INFO"
            2 -> "WARNING"
            3 -> "ERROR"
            4 -> "CRITICAL"
            else -> "UNKNOWN"
        }
    }
    
    /**
     * Formata timestamp para exibição
     */
    fun getFormattedTimestamp(): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Verifica se é um erro
     */
    fun isError(): Boolean {
        return level >= 3
    }
    
    /**
     * Verifica se é crítico
     */
    fun isCritical(): Boolean {
        return level >= 4
    }
}