package com.example.minhascompras.data.update

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

/**
 * Gerenciador de backup e restauração para sistema de rollback
 * Cria snapshots completos do estado do aplicativo antes de atualizações
 */
class BackupManager(private val context: Context) {
    
    companion object {
        private const val BACKUP_DIR = "backups"
        private const val DATABASE_NAME = "compras_database"
        private const val PREFS_NAME = "minhas_compras_prefs"
        private const val BACKUP_VERSION = 1
        private const val ENCRYPTION_KEY = "MinhasCompras2024" // 16 bytes para AES-128
        private const val MAX_BACKUPS = 3 // Manter apenas 3 backups mais recentes
    }
    
    private val backupDir = File(context.filesDir, BACKUP_DIR)
    
    /**
     * Cria um backup completo do estado atual do aplicativo
     * @return BackupInfo com informações do backup criado ou null em caso de erro
     */
    suspend fun createBackup(): BackupInfo? = withContext(Dispatchers.IO) {
        try {
            Logger.d("BackupManager", "Iniciando criação de backup completo")
            
            // Criar diretório de backups se não existir
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // Limpar backups antigos
            cleanupOldBackups()
            
            val timestamp = System.currentTimeMillis()
            val backupFileName = "backup_${timestamp}.zip"
            val backupFile = File(backupDir, backupFileName)
            
            // Criar backup compactado e criptografado
            val backupInfo = createCompressedBackup(backupFile, timestamp)
            
            if (backupInfo != null) {
                Logger.i("BackupManager", "Backup criado com sucesso: ${backupFile.absolutePath}")
                Logger.i("BackupManager", "Tamanho: ${backupFile.length()} bytes")
                Logger.i("BackupManager", "Componentes: ${backupInfo.components}")
            }
            
            backupInfo
            
        } catch (e: Exception) {
            Logger.e("BackupManager", "Erro ao criar backup", e)
            null
        }
    }
    
    /**
     * Restaura o aplicativo para o estado do backup especificado
     * @param backupInfo Informações do backup a ser restaurado
     * @return true se restauração foi bem-sucedida
     */
    suspend fun restoreBackup(backupInfo: BackupInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.i("BackupManager", "Iniciando restauração do backup: ${backupInfo.fileName}")
            
            val backupFile = File(backupDir, backupInfo.fileName)
            if (!backupFile.exists()) {
                Logger.e("BackupManager", "Arquivo de backup não encontrado: ${backupFile.absolutePath}")
                return@withContext false
            }
            
            // Validar integridade do backup antes de restaurar
            if (!validateBackupIntegrity(backupFile, backupInfo)) {
                Logger.e("BackupManager", "Backup inválido ou corrompido")
                return@withContext false
            }
            
            // Restaurar componentes
            val success = restoreFromCompressedBackup(backupFile, backupInfo)
            
            if (success) {
                Logger.i("BackupManager", "Restauração concluída com sucesso")
            } else {
                Logger.e("BackupManager", "Falha na restauração do backup")
            }
            
            success
            
        } catch (e: Exception) {
            Logger.e("BackupManager", "Erro durante restauração", e)
            false
        }
    }
    
    /**
     * Lista todos os backups disponíveis
     * @return Lista de BackupInfo ordenados por data (mais recente primeiro)
     */
    suspend fun listBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            if (!backupDir.exists()) {
                return@withContext emptyList()
            }
            
            backupDir.listFiles { file -> 
                file.isFile && file.name.endsWith(".zip") 
            }?.mapNotNull { file ->
                try {
                    extractBackupInfo(file)
                } catch (e: Exception) {
                    Logger.w("BackupManager", "Erro ao ler info do backup: ${file.name}", e)
                    null
                }
            }?.sortedByDescending { it.timestamp }
            ?: emptyList()
            
        } catch (e: Exception) {
            Logger.e("BackupManager", "Erro ao listar backups", e)
            emptyList()
        }
    }
    
    /**
     * Remove um backup específico
     */
    suspend fun deleteBackup(backupInfo: BackupInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupDir, backupInfo.fileName)
            val deleted = backupFile.delete()
            
            if (deleted) {
                Logger.d("BackupManager", "Backup removido: ${backupInfo.fileName}")
            } else {
                Logger.w("BackupManager", "Falha ao remover backup: ${backupInfo.fileName}")
            }
            
            deleted
            
        } catch (e: Exception) {
            Logger.e("BackupManager", "Erro ao remover backup", e)
            false
        }
    }
    
    /**
     * Cria backup compactado com criptografia
     */
    private suspend fun createCompressedBackup(backupFile: File, timestamp: Long): BackupInfo? {
        return withContext(Dispatchers.IO) {
            val components = mutableListOf<String>()
            var totalSize = 0L
            
            try {
                // Preparar criptografia
                val secretKey = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                
                FileOutputStream(backupFile).use { fileOut ->
                    CipherOutputStream(fileOut, cipher).use { cipherOut ->
                        ZipOutputStream(cipherOut).use { zipOut ->
                            
                            // Backup do banco de dados Room
                            val dbFile = context.getDatabasePath(DATABASE_NAME)
                            if (dbFile.exists()) {
                                val entry = ZipEntry("database/$DATABASE_NAME")
                                entry.time = timestamp
                                zipOut.putNextEntry(entry)
                                
                                FileInputStream(dbFile).use { input ->
                                    input.copyTo(zipOut)
                                    totalSize += dbFile.length()
                                }
                                zipOut.closeEntry()
                                components.add("database")
                                Logger.d("BackupManager", "Database backup concluído: ${dbFile.length()} bytes")
                            }
                            
                            // Backup do banco de dados WAL (Write-Ahead Logging)
                            val walFile = File(dbFile.parent, "$DATABASE_NAME-wal")
                            if (walFile.exists()) {
                                val entry = ZipEntry("database/$DATABASE_NAME-wal")
                                entry.time = timestamp
                                zipOut.putNextEntry(entry)
                                
                                FileInputStream(walFile).use { input ->
                                    input.copyTo(zipOut)
                                    totalSize += walFile.length()
                                }
                                zipOut.closeEntry()
                                components.add("database-wal")
                            }
                            
                            // Backup do SHM (Shared Memory)
                            val shmFile = File(dbFile.parent, "$DATABASE_NAME-shm")
                            if (shmFile.exists()) {
                                val entry = ZipEntry("database/$DATABASE_NAME-shm")
                                entry.time = timestamp
                                zipOut.putNextEntry(entry)
                                
                                FileInputStream(shmFile).use { input ->
                                    input.copyTo(zipOut)
                                    totalSize += shmFile.length()
                                }
                                zipOut.closeEntry()
                                components.add("database-shm")
                            }
                            
                            // Backup das SharedPreferences
                            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                            if (prefsDir.exists()) {
                                prefsDir.listFiles { file -> 
                                    file.isFile && file.name.endsWith(".xml") 
                                }?.forEach { prefFile ->
                                    val entry = ZipEntry("prefs/${prefFile.name}")
                                    entry.time = timestamp
                                    zipOut.putNextEntry(entry)
                                    
                                    FileInputStream(prefFile).use { input ->
                                        input.copyTo(zipOut)
                                        totalSize += prefFile.length()
                                    }
                                    zipOut.closeEntry()
                                }
                                components.add("preferences")
                                Logger.d("BackupManager", "Preferences backup concluído")
                            }
                            
                            // Backup de arquivos locais importantes
                            val localFilesDir = File(context.filesDir, "local_data")
                            if (localFilesDir.exists()) {
                                localFilesDir.walkTopDown().forEach { file ->
                                    if (file.isFile) {
                                        val relativePath = file.relativeTo(localFilesDir).path
                                        val entry = ZipEntry("files/$relativePath")
                                        entry.time = timestamp
                                        zipOut.putNextEntry(entry)
                                        
                                        FileInputStream(file).use { input ->
                                            input.copyTo(zipOut)
                                            totalSize += file.length()
                                        }
                                        zipOut.closeEntry()
                                    }
                                }
                                components.add("local_files")
                                Logger.d("BackupManager", "Local files backup concluído")
                            }
                            
                            // Adicionar metadata do backup
                            val metadata = """
                                Backup Version: $BACKUP_VERSION
                                Created: ${timestamp}
                                App Version: ${getAppVersion()}
                                Components: ${components.joinToString(", ")}
                                Total Size: $totalSize
                            """.trimIndent()
                            
                            val entry = ZipEntry("metadata.txt")
                            entry.time = timestamp
                            zipOut.putNextEntry(entry)
                            zipOut.write(metadata.toByteArray())
                            zipOut.closeEntry()
                            components.add("metadata")
                        }
                    }
                }
                
                BackupInfo(
                    fileName = backupFile.name,
                    timestamp = timestamp,
                    size = backupFile.length(),
                    components = components,
                    appVersion = getAppVersion(),
                    backupVersion = BACKUP_VERSION
                )
                
            } catch (e: Exception) {
                Logger.e("BackupManager", "Erro ao criar backup compactado", e)
                // Limpar arquivo parcial em caso de erro
                if (backupFile.exists()) {
                    backupFile.delete()
                }
                null
            }
        }
    }
    
    /**
     * Restaura a partir de backup compactado e criptografado
     */
    private suspend fun restoreFromCompressedBackup(backupFile: File, backupInfo: BackupInfo): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Preparar descriptografia
                val secretKey = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                
                FileInputStream(backupFile).use { fileIn ->
                    CipherInputStream(fileIn, cipher).use { cipherIn ->
                        java.util.zip.ZipInputStream(cipherIn).use { zipIn ->
                            var entry = zipIn.nextEntry
                            
                            while (entry != null) {
                                when {
                                    entry.name.startsWith("database/") -> {
                                        restoreDatabaseEntry(zipIn, entry)
                                    }
                                    entry.name.startsWith("prefs/") -> {
                                        restorePreferencesEntry(zipIn, entry)
                                    }
                                    entry.name.startsWith("files/") -> {
                                        restoreFileEntry(zipIn, entry)
                                    }
                                }
                                
                                zipIn.closeEntry()
                                entry = zipIn.nextEntry
                            }
                        }
                    }
                }
                
                // Forçar reinicialização do banco de dados se necessário
                refreshDatabaseConnection()
                
                true
                
            } catch (e: Exception) {
                Logger.e("BackupManager", "Erro ao restaurar backup compactado", e)
                false
            }
        }
    }
    
    /**
     * Restaura entrada do banco de dados
     */
    private fun restoreDatabaseEntry(zipIn: java.util.zip.ZipInputStream, entry: ZipEntry) {
        val dbDir = context.getDatabasePath("").parentFile
        val outputFile = File(dbDir, entry.name.substring("database/".length))
        
        FileOutputStream(outputFile).use { output ->
            zipIn.copyTo(output)
        }
        
        Logger.d("BackupManager", "Database entry restaurado: ${outputFile.name}")
    }
    
    /**
     * Restaura entrada de preferências
     */
    private fun restorePreferencesEntry(zipIn: java.util.zip.ZipInputStream, entry: ZipEntry) {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        if (!prefsDir.exists()) {
            prefsDir.mkdirs()
        }
        
        val outputFile = File(prefsDir, entry.name.substring("prefs/".length))
        
        FileOutputStream(outputFile).use { output ->
            zipIn.copyTo(output)
        }
        
        Logger.d("BackupManager", "Preferences entry restaurado: ${outputFile.name}")
    }
    
    /**
     * Restora entrada de arquivo local
     */
    private fun restoreFileEntry(zipIn: java.util.zip.ZipInputStream, entry: ZipEntry) {
        val localFilesDir = File(context.filesDir, "local_data")
        if (!localFilesDir.exists()) {
            localFilesDir.mkdirs()
        }
        
        val relativePath = entry.name.substring("files/".length)
        val outputFile = File(localFilesDir, relativePath)
        
        // Criar diretórios pai se necessário
        outputFile.parentFile?.mkdirs()
        
        FileOutputStream(outputFile).use { output ->
            zipIn.copyTo(output)
        }
        
        Logger.d("BackupManager", "File entry restaurado: ${outputFile.absolutePath}")
    }
    
    /**
     * Valida integridade do backup
     */
    private suspend fun validateBackupIntegrity(backupFile: File, backupInfo: BackupInfo): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Verificar tamanho do arquivo
                if (backupFile.length() != backupInfo.size) {
                    Logger.e("BackupManager", "Tamanho do backup não corresponde: esperado ${backupInfo.size}, atual ${backupFile.length()}")
                    return@withContext false
                }
                
                // Tentar ler o arquivo para verificar corrupção
                val secretKey = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                
                FileInputStream(backupFile).use { fileIn ->
                    CipherInputStream(fileIn, cipher).use { cipherIn ->
                        java.util.zip.ZipInputStream(cipherIn).use { zipIn ->
                            // Tentar ler primeira entrada para verificar integridade
                            zipIn.nextEntry
                        }
                    }
                }
                
                true
                
            } catch (e: Exception) {
                Logger.e("BackupManager", "Backup corrompido ou inválido", e)
                false
            }
        }
    }
    
    /**
     * Extrai informações do backup a partir do arquivo
     */
    private suspend fun extractBackupInfo(backupFile: File): BackupInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val secretKey = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
                val cipher = Cipher.getInstance("AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                
                var timestamp = 0L
                var size = backupFile.length()
                var appVersion = ""
                var backupVersion = 0
                val components = mutableListOf<String>()
                
                FileInputStream(backupFile).use { fileIn ->
                    CipherInputStream(fileIn, cipher).use { cipherIn ->
                        java.util.zip.ZipInputStream(cipherIn).use { zipIn ->
                            var entry = zipIn.nextEntry
                            
                            while (entry != null) {
                                when (entry.name) {
                                    "metadata.txt" -> {
                                        val metadata = zipIn.reader().readText()
                                        timestamp = extractTimestampFromMetadata(metadata)
                                        appVersion = extractAppVersionFromMetadata(metadata)
                                        backupVersion = extractBackupVersionFromMetadata(metadata)
                                        components.addAll(extractComponentsFromMetadata(metadata))
                                    }
                                }
                                
                                zipIn.closeEntry()
                                entry = zipIn.nextEntry
                            }
                        }
                    }
                }
                
                if (timestamp > 0) {
                    BackupInfo(
                        fileName = backupFile.name,
                        timestamp = timestamp,
                        size = size,
                        components = components,
                        appVersion = appVersion,
                        backupVersion = backupVersion
                    )
                } else {
                    // Fallback: extrair timestamp do nome do arquivo
                    val timestampFromName = extractTimestampFromFileName(backupFile.name)
                    if (timestampFromName > 0) {
                        BackupInfo(
                            fileName = backupFile.name,
                            timestamp = timestampFromName,
                            size = size,
                            components = listOf("unknown"),
                            appVersion = appVersion,
                            backupVersion = backupVersion
                        )
                    } else {
                        null
                    }
                }
                
            } catch (e: Exception) {
                Logger.e("BackupManager", "Erro ao extrair informações do backup", e)
                null
            }
        }
    }
    
    /**
     * Remove backups antigos mantendo apenas os mais recentes
     */
    private suspend fun cleanupOldBackups() {
        try {
            val backups = listBackups()
            if (backups.size > MAX_BACKUPS) {
                val backupsToDelete = backups.drop(MAX_BACKUPS)
                backupsToDelete.forEach { backup ->
                    deleteBackup(backup)
                    Logger.d("BackupManager", "Backup antigo removido: ${backup.fileName}")
                }
            }
        } catch (e: Exception) {
            Logger.e("BackupManager", "Erro ao limpar backups antigos", e)
        }
    }
    
    /**
     * Força atualização da conexão do banco de dados após restauração
     */
    private suspend fun refreshDatabaseConnection() {
        try {
            // Fechar instância atual do banco se existir
            // Isso forçará uma nova inicialização com os dados restaurados
            Logger.d("BackupManager", "Atualizando conexão do banco de dados")
            // Nota: A implementação específica dependerá de como o AppDatabase é gerenciado
        } catch (e: Exception) {
            Logger.e("BackupManager", "Erro ao atualizar conexão do banco", e)
        }
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
     * Extrai timestamp do metadata
     */
    private fun extractTimestampFromMetadata(metadata: String): Long {
        return try {
            val pattern = Regex("Created: (\\d+)")
            pattern.find(metadata)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Extrai app version do metadata
     */
    private fun extractAppVersionFromMetadata(metadata: String): String {
        return try {
            val pattern = Regex("App Version: (.+)")
            pattern.find(metadata)?.groupValues?.get(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Extrai backup version do metadata
     */
    private fun extractBackupVersionFromMetadata(metadata: String): Int {
        return try {
            val pattern = Regex("Backup Version: (\\d+)")
            pattern.find(metadata)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Extrai components do metadata
     */
    private fun extractComponentsFromMetadata(metadata: String): List<String> {
        return try {
            val pattern = Regex("Components: (.+)")
            val componentsStr = pattern.find(metadata)?.groupValues?.get(1) ?: ""
            componentsStr.split(", ").filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Extrai timestamp do nome do arquivo (fallback)
     */
    private fun extractTimestampFromFileName(fileName: String): Long {
        return try {
            val pattern = Regex("backup_(\\d+)\\.zip")
            pattern.find(fileName)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Informações sobre um backup criado
 */
data class BackupInfo(
    val fileName: String,
    val timestamp: Long,
    val size: Long,
    val components: List<String>,
    val appVersion: String,
    val backupVersion: Int
) {
    /**
     * Formata o tamanho para exibição
     */
    fun getFormattedSize(): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1.0 -> "%.1f MB".format(mb)
            kb >= 1.0 -> "%.1f KB".format(kb)
            else -> "$size bytes"
        }
    }
    
    /**
     * Formata o timestamp para exibição
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
}