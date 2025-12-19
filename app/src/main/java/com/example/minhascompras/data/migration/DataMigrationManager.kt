package com.example.minhascompras.data.migration

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.update.UpdateLogger
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Gerenciador de migrações de dados para compatibilidade com v2.16.0 e versões futuras
 * Implementa estratégias seguras de migração com rollback automático
 */
class DataMigrationManager(private val context: Context) {
    
    companion object {
        // Versões suportadas
        private const val MIN_SUPPORTED_VERSION = 2
        private const val CURRENT_VERSION = 6 // Incrementado para v2.18.0
        private const val V2_16_0_VERSION = 5 // Versão do banco na v2.16.0
        
        // Nomes de tabelas
        private const val TABLE_ITENS_COMPRA = "itens_compra"
        private const val TABLE_SHOPPING_LISTS = "shopping_lists"
        private const val TABLE_SHOPPING_LIST_HISTORY = "shopping_list_history"
        private const val TABLE_HISTORY_ITEMS = "history_items"
        
        // Preferências
        private const val PREFS_NAME = "minhas_compras_prefs"
        private const val PREF_MIGRATION_BACKUP = "migration_backup_created"
        private const val PREF_LAST_MIGRATION = "last_migration_version"
    }
    
    private val logger = UpdateLogger(context)
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Executa migração necessária baseada na versão atual do banco
     * @return MigrationResult com resultado da operação
     */
    suspend fun migrateIfNeeded(): MigrationResult = withContext(Dispatchers.IO) {
        
        val operationId = logger.logOperationStart(
            "database_migration_check",
            "migration",
            mapOf("current_version" to CURRENT_VERSION)
        )
        
        try {
            Logger.i("DataMigrationManager", "Verificando necessidade de migração de dados")
            
            // Verificar versão atual do banco
            val currentDbVersion = getCurrentDatabaseVersion()
            Logger.i("DataMigrationManager", "Versão atual do banco: $currentDbVersion")
            
            when {
                currentDbVersion == 0 -> {
                    // Banco novo, sem migração necessária
                    logger.logOperationEnd(
                        operationId, "database_migration_check", "migration", 
                        true, 0, mapOf("result" to "new_database")
                    )
                    MigrationResult(
                        success = true,
                        fromVersion = 0,
                        toVersion = CURRENT_VERSION,
                        migrationType = MigrationType.NEW_DATABASE,
                        message = "Banco de dados novo criado"
                    )
                }
                
                currentDbVersion == CURRENT_VERSION -> {
                    // Já está na versão mais recente
                    logger.logOperationEnd(
                        operationId, "database_migration_check", "migration", 
                        true, 0, mapOf("result" to "already_latest")
                    )
                    MigrationResult(
                        success = true,
                        fromVersion = currentDbVersion,
                        toVersion = CURRENT_VERSION,
                        migrationType = MigrationType.NO_MIGRATION,
                        message = "Banco já está na versão mais recente"
                    )
                }
                
                currentDbVersion < MIN_SUPPORTED_VERSION -> {
                    // Versão muito antiga, não suportada
                    val error = "Versão do banco ($currentDbVersion) não é suportada. Mínima suportada: $MIN_SUPPORTED_VERSION"
                    logger.e("migration", error)
                    logger.logOperationEnd(
                        operationId, "database_migration_check", "migration", 
                        false, 0, mapOf("error" to error)
                    )
                    MigrationResult(
                        success = false,
                        fromVersion = currentDbVersion,
                        toVersion = CURRENT_VERSION,
                        migrationType = MigrationType.UNSUPPORTED,
                        message = error
                    )
                }
                
                else -> {
                    // Precisa migrar
                    performMigration(currentDbVersion, CURRENT_VERSION, operationId)
                }
            }
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro durante verificação de migração", e)
            logger.e("migration", "Erro na verificação de migração", e)
            logger.logOperationEnd(
                operationId, "database_migration_check", "migration",
                false, 0, mapOf("error" to (e.message ?: "Unknown error"))
            )
            
            MigrationResult(
                success = false,
                fromVersion = 0,
                toVersion = CURRENT_VERSION,
                migrationType = MigrationType.ERROR,
                message = "Erro durante verificação: ${e.message}",
                error = e
            )
        }
    }
    
    /**
     * Executa migração específica de v2.16.0 para v2.18.0
     */
    suspend fun migrateFromV2_16_0(): MigrationResult = withContext(Dispatchers.IO) {
        
        val operationId = logger.logOperationStart(
            "migrate_from_v2_16_0",
            "migration",
            mapOf("target_version" to CURRENT_VERSION)
        )
        
        try {
            Logger.i("DataMigrationManager", "Iniciando migração específica de v2.16.0 para v2.18.0")
            
            // Criar backup antes da migração
            val backupCreated = createMigrationBackup()
            if (!backupCreated) {
                val error = "Falha ao criar backup antes da migração"
                logger.e("migration", error)
                return@withContext MigrationResult(
                    success = false,
                    fromVersion = V2_16_0_VERSION,
                    toVersion = CURRENT_VERSION,
                    migrationType = MigrationType.ERROR,
                    message = error
                )
            }
            
            // Executar migrações incrementais
            val migrations = listOf(
                MIGRATION_5_6 // v2.16.0 (v5) para v2.18.0 (v6)
            )
            
            val startTime = System.currentTimeMillis()
            
            // Aplicar migrações usando Room
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "compras_database"
            )
            .addMigrations(*migrations.toTypedArray())
            .build()
            
            // Verificar se migração foi bem-sucedida
            val newVersion = getCurrentDatabaseVersion()
            val success = newVersion == CURRENT_VERSION
            
            val duration = System.currentTimeMillis() - startTime
            
            if (success) {
                Logger.i("DataMigrationManager", "Migração v2.16.0 → v2.18.0 concluída com sucesso")
                logger.logOperationEnd(
                    operationId, "migrate_from_v2_16_0", "migration", 
                    true, duration, mapOf("new_version" to newVersion)
                )
                
                // Limpar backup após migração bem-sucedida
                cleanupMigrationBackup()
                
                MigrationResult(
                    success = true,
                    fromVersion = V2_16_0_VERSION,
                    toVersion = CURRENT_VERSION,
                    migrationType = MigrationType.INCREMENTAL,
                    message = "Migração de v2.16.0 para v2.18.0 concluída",
                    duration = duration
                )
            } else {
                val error = "Migração falhou: versão esperada $CURRENT_VERSION, obtida $newVersion"
                Logger.e("DataMigrationManager", error)
                logger.e("migration", error)
                
                // Tentar rollback do backup
                rollbackFromMigrationBackup()
                
                MigrationResult(
                    success = false,
                    fromVersion = V2_16_0_VERSION,
                    toVersion = CURRENT_VERSION,
                    migrationType = MigrationType.ERROR,
                    message = error
                )
            }
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro durante migração de v2.16.0", e)
            logger.e("migration", "Erro na migração de v2.16.0", e)
            
            // Tentar rollback em caso de erro
            rollbackFromMigrationBackup()
            
            MigrationResult(
                success = false,
                fromVersion = V2_16_0_VERSION,
                toVersion = CURRENT_VERSION,
                migrationType = MigrationType.ERROR,
                message = "Erro durante migração: ${e.message}",
                error = e
            )
        }
    }
    
    /**
     * Realiza migração incremental entre versões
     */
    private suspend fun performMigration(
        fromVersion: Int,
        toVersion: Int,
        operationId: String
    ): MigrationResult = withContext(Dispatchers.IO) {
        
        try {
            Logger.i("DataMigrationManager", "Iniciando migração de $fromVersion para $toVersion")
            
            // Criar backup antes da migração
            val backupCreated = createMigrationBackup()
            if (!backupCreated) {
                val error = "Falha ao criar backup antes da migração"
                logger.e("migration", error)
                return@withContext MigrationResult(
                    success = false,
                    fromVersion = fromVersion,
                    toVersion = toVersion,
                    migrationType = MigrationType.ERROR,
                    message = error
                )
            }
            
            val startTime = System.currentTimeMillis()
            
            // Determinar migrações necessárias
            val migrations = getRequiredMigrations(fromVersion, toVersion)
            
            if (migrations.isEmpty()) {
                MigrationResult(
                    success = true,
                    fromVersion = fromVersion,
                    toVersion = toVersion,
                    migrationType = MigrationType.NO_MIGRATION,
                    message = "Nenhuma migração necessária"
                )
            } else {
                // Aplicar migrações
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "compras_database"
                )
                .addMigrations(*migrations.toTypedArray())
                .build()
                
                // Verificar resultado
                val finalVersion = getCurrentDatabaseVersion()
                val success = finalVersion == toVersion
                val duration = System.currentTimeMillis() - startTime
                
                if (success) {
                    Logger.i("DataMigrationManager", "Migração concluída com sucesso")
                    logger.logOperationEnd(
                        operationId, "database_migration", "migration", 
                        true, duration, mapOf("final_version" to finalVersion)
                    )
                    
                    // Limpar backup
                    cleanupMigrationBackup()
                    
                    MigrationResult(
                        success = true,
                        fromVersion = fromVersion,
                        toVersion = toVersion,
                        migrationType = MigrationType.INCREMENTAL,
                        message = "Migração incremental concluída com sucesso",
                        duration = duration,
                        migrationsApplied = migrations.size
                    )
                } else {
                    val error = "Migração falhou: versão esperada $toVersion, obtida $finalVersion"
                    Logger.e("DataMigrationManager", error)
                    
                    // Rollback
                    rollbackFromMigrationBackup()
                    
                    MigrationResult(
                        success = false,
                        fromVersion = fromVersion,
                        toVersion = toVersion,
                        migrationType = MigrationType.ERROR,
                        message = error
                    )
                }
            }
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro durante migração", e)
            logger.e("migration", "Erro durante migração", e)
            
            // Rollback em caso de erro
            rollbackFromMigrationBackup()
            
            MigrationResult(
                success = false,
                fromVersion = fromVersion,
                toVersion = toVersion,
                migrationType = MigrationType.ERROR,
                message = "Erro durante migração: ${e.message}",
                error = e
            )
        }
    }
    
    /**
     * Obtém versão atual do banco de dados
     */
    private fun getCurrentDatabaseVersion(): Int {
        return try {
            val dbFile = context.getDatabasePath("compras_database")
            if (!dbFile.exists()) {
                return 0
            }
            
            // Tentar ler versão do banco usando Room
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "compras_database"
            ).fallbackToDestructiveMigration()
            .build()
            
            val version = db.openHelper.readableDatabase.version
            db.close()
            version
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro ao obter versão do banco", e)
            0
        }
    }
    
    /**
     * Obtém migrações necessárias entre versões
     */
    private fun getRequiredMigrations(fromVersion: Int, toVersion: Int): List<Migration> {
        val migrations = mutableListOf<Migration>()
        
        // Adicionar migrações baseadas nas versões
        if (fromVersion < 6 && toVersion >= 6) {
            migrations.add(MIGRATION_5_6)
        }
        
        return migrations
    }
    
    /**
     * Cria backup antes da migração
     */
    private suspend fun createMigrationBackup(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d("DataMigrationManager", "Criando backup antes da migração")
            
            val dbFile = context.getDatabasePath("compras_database")
            if (!dbFile.exists()) {
                return@withContext true // Não há o que backupar
            }
            
            val backupDir = File(context.filesDir, "migration_backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDir, "backup_${timestamp}.db")
            
            // Copiar arquivo do banco
            dbFile.copyTo(backupFile, overwrite = true)
            
            // Salvar informações do backup
            preferences.edit()
                .putLong(PREF_MIGRATION_BACKUP, timestamp)
                .putInt(PREF_LAST_MIGRATION, getCurrentDatabaseVersion())
                .apply()
            
            Logger.i("DataMigrationManager", "Backup de migração criado: ${backupFile.name}")
            true
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro ao criar backup de migração", e)
            false
        }
    }
    
    /**
     * Limpa backup após migração bem-sucedida
     */
    private suspend fun cleanupMigrationBackup() = withContext(Dispatchers.IO) {
        try {
            val backupTimestamp = preferences.getLong(PREF_MIGRATION_BACKUP, 0)
            if (backupTimestamp > 0) {
                val backupDir = File(context.filesDir, "migration_backups")
                val backupFile = File(backupDir, "backup_${backupTimestamp}.db")
                
                if (backupFile.delete()) {
                    Logger.d("DataMigrationManager", "Backup de migração removido: ${backupFile.name}")
                }
                
                preferences.edit()
                    .remove(PREF_MIGRATION_BACKUP)
                    .apply()
            }
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro ao limpar backup de migração", e)
        }
    }
    
    /**
     * Restaura backup em caso de falha na migração
     */
    private suspend fun rollbackFromMigrationBackup() = withContext(Dispatchers.IO) {
        try {
            Logger.w("DataMigrationManager", "Iniciando rollback de migração")
            
            val backupTimestamp = preferences.getLong(PREF_MIGRATION_BACKUP, 0)
            if (backupTimestamp == 0L) {
                Logger.e("DataMigrationManager", "Nenhum backup de migração encontrado")
                return@withContext
            }
            
            val backupDir = File(context.filesDir, "migration_backups")
            val backupFile = File(backupDir, "backup_${backupTimestamp}.db")
            val dbFile = context.getDatabasePath("compras_database")
            
            if (!backupFile.exists()) {
                Logger.e("DataMigrationManager", "Arquivo de backup não encontrado")
                return@withContext
            }
            
            // Fechar conexões existentes
            // Nota: Isso pode precisar ser ajustado baseado na implementação
            
            // Restaurar backup
            backupFile.copyTo(dbFile, overwrite = true)
            
            Logger.i("DataMigrationManager", "Rollback de migração concluído com sucesso")
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro durante rollback de migração", e)
        }
    }
    
    /**
     * Verifica integridade dos dados pós-migração
     */
    suspend fun verifyDataIntegrity(): DataIntegrityResult = withContext(Dispatchers.IO) {
        try {
            Logger.d("DataMigrationManager", "Verificando integridade dos dados pós-migração")
            
            val issues = mutableListOf<String>()
            
            // Verificar tabelas essenciais
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "compras_database"
            ).fallbackToDestructiveMigration()
            .build()
            
            // Verificar se tabelas existem e têm dados válidos
            try {
                val itemDao = db.itemCompraDao()
                val itemCount = itemDao.getAllItemsCount()
                if (itemCount < 0) {
                    issues.add("Contagem de itens inválida: $itemCount")
                }
                
                val historyDao = db.historyDao()
                val historyCount = historyDao.getAllHistoryCount()
                if (historyCount < 0) {
                    issues.add("Contagem de histórico inválida: $historyCount")
                }
                
            } catch (e: Exception) {
                issues.add("Erro ao acessar dados: ${e.message}")
            }
            
            db.close()
            
            val isValid = issues.isEmpty()
            
            DataIntegrityResult(
                isValid = isValid,
                issues = issues,
                message = if (isValid) "Integridade verificada com sucesso" 
                          else "Problemas encontrados: ${issues.joinToString(", ")}"
            )
            
        } catch (e: Exception) {
            Logger.e("DataMigrationManager", "Erro na verificação de integridade", e)
            DataIntegrityResult(
                isValid = false,
                issues = listOf("Erro na verificação: ${e.message}"),
                message = "Erro durante verificação de integridade"
            )
        }
    }
    
    /**
     * Migração de v5 para v6 (v2.16.0 para v2.18.0)
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            Logger.i("DataMigrationManager", "Executando migração 5→6")
            
            try {
                // Adicionar novas colunas ou tabelas para v2.18.0
                // Exemplo: Adicionar coluna de otimização
                database.execSQL(
                    """
                    ALTER TABLE $TABLE_ITENS_COMPRA 
                    ADD COLUMN last_updated INTEGER DEFAULT 0
                    """.trimIndent()
                )
                
                // Adicionar índices para performance
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS 
                    index_itens_compra_last_updated 
                    ON $TABLE_ITENS_COMPRA(last_updated)
                    """.trimIndent()
                )
                
                // Atualizar dados existentes se necessário
                database.execSQL(
                    """
                    UPDATE $TABLE_ITENS_COMPRA 
                    SET last_updated = strftime('%s', 'now')
                    WHERE last_updated = 0
                    """.trimIndent()
                )
                
                Logger.i("DataMigrationManager", "Migração 5→6 concluída com sucesso")
                
            } catch (e: Exception) {
                Logger.e("DataMigrationManager", "Erro na migração 5→6", e)
                throw e
            }
        }
    }
}

/**
 * Tipos de migração
 */
enum class MigrationType {
    NEW_DATABASE,
    NO_MIGRATION,
    INCREMENTAL,
    DESTRUCTIVE,
    ROLLBACK,
    UNSUPPORTED,
    ERROR
}

/**
 * Resultado de uma operação de migração
 */
data class MigrationResult(
    val success: Boolean,
    val fromVersion: Int,
    val toVersion: Int,
    val migrationType: MigrationType,
    val message: String,
    val duration: Long = 0,
    val migrationsApplied: Int = 0,
    val error: Exception? = null
) {
    /**
     * Formata duração para exibição
     */
    fun getFormattedDuration(): String {
        return "${duration}ms"
    }
    
    /**
     * Retorna resumo formatado
     */
    fun getSummary(): String {
        return if (success) {
            "Migração $fromVersion→$toVersion concluída (${getFormattedDuration()})"
        } else {
            "Migração $fromVersion→$toVersion falhou: $message"
        }
    }
}

/**
 * Resultado da verificação de integridade de dados
 */
data class DataIntegrityResult(
    val isValid: Boolean,
    val issues: List<String>,
    val message: String
) {
    /**
     * Retorna contagem de problemas
     */
    fun getIssueCount(): Int = issues.size
    
    /**
     * Verifica se há problemas críticos
     */
    fun hasCriticalIssues(): Boolean = issues.any { 
        it.contains("erro", ignoreCase = true) || 
        it.contains("inválid", ignoreCase = true) 
    }
}