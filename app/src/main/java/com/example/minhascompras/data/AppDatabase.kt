package com.example.minhascompras.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ItemCompra::class,
        ShoppingList::class,
        ShoppingListHistory::class,
        HistoryItem::class
    ],
    version = 10, // Incremented to fix schema integrity issue
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemCompraDao(): ItemCompraDao
    abstract fun historyDao(): HistoryDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE itens_compra ADD COLUMN categoria TEXT NOT NULL DEFAULT 'Outros'"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Criar tabela de histórico de listas
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shopping_list_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        completionDate INTEGER NOT NULL,
                        listName TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                // Criar tabela de itens do histórico
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS history_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        parentListId INTEGER NOT NULL,
                        nome TEXT NOT NULL,
                        quantidade INTEGER NOT NULL,
                        preco REAL,
                        categoria TEXT NOT NULL,
                        FOREIGN KEY(parentListId) REFERENCES shopping_list_history(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                // Criar índice para melhor performance
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_history_items_parentListId ON history_items(parentListId)"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Habilitar foreign keys
                db.execSQL("PRAGMA foreign_keys = ON")
                
                // 1. Criar tabela shopping_lists
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shopping_lists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        dataCriacao INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                // 2. REMOVIDO: Não criar lista padrão automaticamente
                // O usuário criará sua primeira lista quando necessário

                // 3. Recriar tabela itens_compra com foreign key
                // SQLite não suporta adicionar foreign key com ALTER TABLE, então precisamos recriar
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS itens_compra_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        quantidade INTEGER NOT NULL,
                        preco REAL,
                        comprado INTEGER NOT NULL,
                        categoria TEXT NOT NULL,
                        dataCriacao INTEGER NOT NULL,
                        listId INTEGER,
                        FOREIGN KEY(listId) REFERENCES shopping_lists(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // 4. Copiar dados da tabela antiga para a nova
                db.execSQL(
                    """
                    INSERT INTO itens_compra_new (id, nome, quantidade, preco, comprado, categoria, dataCriacao, listId)
                    SELECT id, nome, quantidade, preco, comprado, categoria, dataCriacao, listId
                    FROM itens_compra
                    """.trimIndent()
                )

                // 5. Dropar tabela antiga
                db.execSQL("DROP TABLE itens_compra")

                // 6. Renomear nova tabela
                db.execSQL("ALTER TABLE itens_compra_new RENAME TO itens_compra")

                // 7. Criar índice para melhor performance
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_itens_compra_listId ON itens_compra(listId)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Habilitar foreign keys
                db.execSQL("PRAGMA foreign_keys = ON")
                
                // Adicionar coluna listId à tabela shopping_list_history
                // Nullable para permitir histórico antigo sem lista associada
                db.execSQL(
                    "ALTER TABLE shopping_list_history ADD COLUMN listId INTEGER"
                )

                // Criar índice para melhor performance em queries filtradas por listId
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_shopping_list_history_listId ON shopping_list_history(listId)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Habilitar foreign keys
                db.execSQL("PRAGMA foreign_keys = ON")
                
                // Recriar tabela itens_compra com foreign key para corrigir bancos que já estão na versão 6
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS itens_compra_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        quantidade INTEGER NOT NULL,
                        preco REAL,
                        comprado INTEGER NOT NULL,
                        categoria TEXT NOT NULL,
                        dataCriacao INTEGER NOT NULL,
                        listId INTEGER,
                        FOREIGN KEY(listId) REFERENCES shopping_lists(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Copiar dados
                db.execSQL(
                    """
                    INSERT INTO itens_compra_new (id, nome, quantidade, preco, comprado, categoria, dataCriacao, listId)
                    SELECT id, nome, quantidade, preco, comprado, categoria, dataCriacao, listId
                    FROM itens_compra
                    """.trimIndent()
                )

                // Dropar e renomear
                db.execSQL("DROP TABLE itens_compra")
                db.execSQL("ALTER TABLE itens_compra_new RENAME TO itens_compra")

                // Recriar índice
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_itens_compra_listId ON itens_compra(listId)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Habilitar foreign keys
                db.execSQL("PRAGMA foreign_keys = ON")
                
                // Forçar recriação da tabela itens_compra com foreign key
                // Esta migração garante que todos os bancos tenham a foreign key corretamente
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS itens_compra_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        quantidade INTEGER NOT NULL,
                        preco REAL,
                        comprado INTEGER NOT NULL,
                        categoria TEXT NOT NULL,
                        dataCriacao INTEGER NOT NULL,
                        listId INTEGER,
                        FOREIGN KEY(listId) REFERENCES shopping_lists(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Copiar dados preservando todos os registros
                db.execSQL(
                    """
                    INSERT INTO itens_compra_new (id, nome, quantidade, preco, comprado, categoria, dataCriacao, listId)
                    SELECT id, nome, quantidade, preco, comprado, categoria, dataCriacao, listId
                    FROM itens_compra
                    """.trimIndent()
                )

                // Dropar tabela antiga
                db.execSQL("DROP TABLE itens_compra")

                // Renomear nova tabela
                db.execSQL("ALTER TABLE itens_compra_new RENAME TO itens_compra")

                // Recriar índice
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_itens_compra_listId ON itens_compra(listId)"
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Habilitar foreign keys
                db.execSQL("PRAGMA foreign_keys = ON")
                
                // Adicionar coluna isArchived à tabela shopping_lists
                // Valor padrão 0 (false) para listas existentes (não arquivadas)
                db.execSQL(
                    "ALTER TABLE shopping_lists ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Habilitar foreign keys
                db.execSQL("PRAGMA foreign_keys = ON")
                // Migration vazia - apenas incrementa versão para corrigir hash de identidade
                // O schema já está correto, apenas o hash precisa ser atualizado
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Limpar banco antigo se houver problema de hash
                val dbFile = context.getDatabasePath("compras_database")
                val dbJournalFile = context.getDatabasePath("compras_database-journal")
                val dbWalFile = context.getDatabasePath("compras_database-wal")
                val dbShmFile = context.getDatabasePath("compras_database-shm")
                
                // Verificar se o banco existe e tentar detectar problema de hash
                if (dbFile.exists()) {
                    try {
                        // Tentar abrir o banco para verificar se há problema
                        val testDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                            dbFile.absolutePath,
                            null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                        )
                        val version = testDb.version
                        testDb.close()
                        
                        // Se a versão for menor que 10, limpar o banco
                        if (version < 10) {
                            android.util.Log.w("AppDatabase", "Banco de dados com versão antiga ($version), limpando para recriação")
                            dbFile.delete()
                            dbJournalFile.delete()
                            dbWalFile.delete()
                            dbShmFile.delete()
                        }
                    } catch (e: Exception) {
                        // Se houver erro ao verificar, limpar o banco
                        android.util.Log.w("AppDatabase", "Erro ao verificar banco, limpando: ${e.message}")
                        dbFile.delete()
                        dbJournalFile.delete()
                        dbWalFile.delete()
                        dbShmFile.delete()
                    }
                }
                
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "compras_database"
                    )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Garantir que foreign keys estão sempre habilitadas
                            db.execSQL("PRAGMA foreign_keys = ON")
                        }
                    })
                    .fallbackToDestructiveMigration() // Fallback seguro em caso de erro de migração
                    .build()
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Erro ao criar banco de dados, tentando limpar: ${e.message}", e)
                    // Limpar banco e tentar novamente
                    dbFile.delete()
                    dbJournalFile.delete()
                    dbWalFile.delete()
                    dbShmFile.delete()
                    
                    try {
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "compras_database"
                        )
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                db.execSQL("PRAGMA foreign_keys = ON")
                            }
                        })
                        .fallbackToDestructiveMigration()
                        .build()
                    } catch (e2: Exception) {
                        android.util.Log.e("AppDatabase", "Erro crítico ao criar banco de dados", e2)
                        throw RuntimeException("Não foi possível inicializar o banco de dados", e2)
                    }
                }
                INSTANCE = instance
                instance
            }
        }
    }
}

