package com.example.minhascompras.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.util.*

object DebugLogger {
    // Caminho do arquivo de log (será definido via init)
    private var logFilePath: String? = null
    
    fun init(context: Context) {
        // Tentar usar caminho do workspace se disponível via system property
        val workspacePath = System.getProperty("debug.log.path")
        logFilePath = if (workspacePath != null) {
            File(workspacePath).absolutePath
        } else {
            // Usar caminho fixo do workspace do Cursor
            // c:\Users\nerdd\Desktop\Minhas-Compras-Android\.cursor\debug.log
            val workspaceLogPath = File("C:\\Users\\nerdd\\Desktop\\Minhas-Compras-Android\\.cursor\\debug.log")
            if (workspaceLogPath.parentFile?.exists() == true || workspaceLogPath.parentFile?.mkdirs() == true) {
                workspaceLogPath.absolutePath
            } else {
                // Fallback: usar diretório de arquivos externos do app
                File(context.getExternalFilesDir(null), "debug.log").absolutePath
            }
        }
    }
    
    private fun getLogFilePath(): String {
        return logFilePath ?: throw IllegalStateException("DebugLogger not initialized. Call init() first.")
    }
    
    fun log(
        location: String,
        message: String,
        data: Map<String, Any?> = emptyMap(),
        sessionId: String = "debug-session",
        runId: String = "run1",
        hypothesisId: String? = null
    ) {
        try {
            val logFile = File(getLogFilePath())
            logFile.parentFile?.mkdirs()
            
            // Construir JSON manualmente para evitar dependências extras
            val jsonBuilder = StringBuilder()
            jsonBuilder.append("{")
            jsonBuilder.append("\"id\":\"log_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}\",")
            jsonBuilder.append("\"timestamp\":${System.currentTimeMillis()},")
            jsonBuilder.append("\"location\":${escapeJson(location)},")
            jsonBuilder.append("\"message\":${escapeJson(message)},")
            jsonBuilder.append("\"data\":{")
            data.entries.forEachIndexed { index, entry ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("${escapeJson(entry.key)}:")
                when (val value = entry.value) {
                    null -> jsonBuilder.append("null")
                    is String -> jsonBuilder.append(escapeJson(value))
                    is Number, is Boolean -> jsonBuilder.append(value)
                    else -> jsonBuilder.append(escapeJson(value.toString()))
                }
            }
            jsonBuilder.append("},")
            jsonBuilder.append("\"sessionId\":${escapeJson(sessionId)},")
            jsonBuilder.append("\"runId\":${escapeJson(runId)}")
            hypothesisId?.let {
                jsonBuilder.append(",\"hypothesisId\":${escapeJson(it)}")
            }
            jsonBuilder.append("}\n")
            
            FileWriter(logFile, true).use { writer ->
                writer.append(jsonBuilder.toString())
            }
        } catch (e: Exception) {
            Log.e("DebugLogger", "Erro ao escrever log", e)
        }
        
        // Também logar no Logcat para facilitar debugging
        Log.d("DebugLogger", "[$hypothesisId] $location: $message - ${data.entries.joinToString { "${it.key}=${it.value}" }}")
    }
    
    private fun escapeJson(str: String): String {
        return "\"" + str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\""
    }
}

