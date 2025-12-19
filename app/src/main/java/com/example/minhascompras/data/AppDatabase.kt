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
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemCompraDao(): ItemCompraDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE itens_compra ADD COLUMN categoria TEXT NOT NULL DEFAULT 'Outros'"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Criar tabela de shopping lists PRIMEIRO
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shopping_lists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        dataCriacao INTEGER NOT NULL,
                        isAtiva INTEGER NOT NULL DEFAULT 1,
                        ordem INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                
                // Inserir lista padrão com id=1
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO shopping_lists (id, nome, dataCriacao, isAtiva, ordem)
                    VALUES (1, 'Lista de Compras Padrão', ${System.currentTimeMillis()}, 1, 0)
                    """.trimIndent()
                )
                
                // Criar tabela de histórico de listas
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shopping_list_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        completionDate INTEGER NOT NULL,
                        listName TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                // Criar tabela de itens do histórico
                database.execSQL(
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
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_history_items_parentListId ON history_items(parentListId)"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("AppDatabase", "=== INICIANDO MIGRAÇÃO 4_5 ===")
                try {
                    // Garantir que a tabela shopping_lists exista
                    android.util.Log.d("AppDatabase", "Garantindo existência da tabela shopping_lists")
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS shopping_lists (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            nome TEXT NOT NULL,
                            dataCriacao INTEGER NOT NULL,
                            isAtiva INTEGER NOT NULL DEFAULT 1,
                            ordem INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
                    )
                    android.util.Log.d("AppDatabase", "Tabela shopping_lists garantida")
                    
                    // Inserir lista padrão com id=1 se não existir
                    android.util.Log.d("AppDatabase", "Inserindo lista padrão com id=1")
                    database.execSQL(
                        """
                        INSERT OR IGNORE INTO shopping_lists (id, nome, dataCriacao, isAtiva, ordem)
                        VALUES (1, 'Lista de Compras Padrão', ${System.currentTimeMillis()}, 1, 0)
                        """.trimIndent()
                    )
                    android.util.Log.d("AppDatabase", "Lista padrão inserida com sucesso")
                    
                    // Verificar se a lista padrão foi inserida
                    val cursor = database.query("SELECT id FROM shopping_lists WHERE id = 1")
                    val hasDefaultList = cursor.count > 0
                    cursor.close()
                    android.util.Log.d("AppDatabase", "Lista padrão existe após migração 4_5: $hasDefaultList")
                    
                    if (!hasDefaultList) {
                        android.util.Log.w("AppDatabase", "Lista padrão não encontrada após INSERT OR IGNORE, tentando inserção direta")
                        database.execSQL(
                            """
                            INSERT INTO shopping_lists (id, nome, dataCriacao, isAtiva, ordem)
                            VALUES (1, 'Lista de Compras Padrão', ${System.currentTimeMillis()}, 1, 0)
                            """.trimIndent()
                        )
                        android.util.Log.d("AppDatabase", "Lista padrão inserida com inserção direta")
                    }
                    
                    android.util.Log.d("AppDatabase", "=== MIGRAÇÃO 4_5 CONCLUÍDA COM SUCESSO ===")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "ERRO NA MIGRAÇÃO 4_5: ${e.message}", e)
                    android.util.Log.e("AppDatabase", "Stack trace: ${e.stackTraceToString()}")
                    android.util.Log.e("AppDatabase", "=== MIGRAÇÃO 4_5 FALHOU ===")
                    throw e
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.d("AppDatabase", "=== INICIANDO MIGRAÇÃO 5_6 ===")
                try {
                    // Migração para v2.18.0 - Adicionar colunas de otimização
                    android.util.Log.d("AppDatabase", "Adicionando coluna last_updated")
                    database.execSQL(
                        "ALTER TABLE itens_compra ADD COLUMN last_updated INTEGER DEFAULT 0"
                    )
                    android.util.Log.d("AppDatabase", "Coluna last_updated adicionada com sucesso")
                    
                    // Adicionar índice para performance
                    android.util.Log.d("AppDatabase", "Criando índice para last_updated")
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_itens_compra_last_updated ON itens_compra(last_updated)"
                    )
                    android.util.Log.d("AppDatabase", "Índice criado com sucesso")
                    
                    // Atualizar registros existentes
                    android.util.Log.d("AppDatabase", "Atualizando registros existentes")
                    database.execSQL(
                        "UPDATE itens_compra SET last_updated = strftime('%s','now') WHERE last_updated = 0"
                    )
                    android.util.Log.d("AppDatabase", "Registros atualizados com sucesso")
                    
                    android.util.Log.d("AppDatabase", "=== MIGRAÇÃO 5_6 CONCLUÍDA COM SUCESSO ===")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "ERRO NA MIGRAÇÃO 5_6: ${e.message}", e)
                    android.util.Log.e("AppDatabase", "Stack trace: ${e.stackTraceToString()}")
                    android.util.Log.e("AppDatabase", "=== MIGRAÇÃO 5_6 FALHOU ===")
                    throw e
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                android.util.Log.d("AppDatabase", "=== INICIANDO CRIAÇÃO DO BANCO DE DADOS ===")
                val instance = try {
                    android.util.Log.d("AppDatabase", "Criando Room Database Builder")
                    val builder = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "compras_database"
                    )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration() // Fallback seguro em caso de erro de migração
                    
                    android.util.Log.d("AppDatabase", "Adicionando callback para diagnóstico")
                    builder.addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            android.util.Log.d("AppDatabase", "Banco de dados criado pela primeira vez")
                            // Garantir que a tabela shopping_lists tenha o registro padrão
                            try {
                                android.util.Log.d("AppDatabase", "Garantindo lista padrão em onCreate")
                                db.execSQL(
                                    """
                                    INSERT OR IGNORE INTO shopping_lists (id, nome, dataCriacao, isAtiva, ordem)
                                    VALUES (1, 'Lista de Compras Padrão', ${System.currentTimeMillis()}, 1, 0)
                                    """.trimIndent()
                                )
                                android.util.Log.d("AppDatabase", "Lista padrão garantida em onCreate")
                            } catch (e: Exception) {
                                android.util.Log.e("AppDatabase", "Erro ao garantir lista padrão em onCreate", e)
                            }
                        }
                        
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            android.util.Log.d("AppDatabase", "Banco de dados aberto")
                            // Verificar se as tabelas existem
                            try {
                                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
                                val tables = mutableListOf<String>()
                                while (cursor.moveToNext()) {
                                    tables.add(cursor.getString(0))
                                }
                                cursor.close()
                                android.util.Log.d("AppDatabase", "Tabelas encontradas: $tables")
                                
                                // Verificar estrutura da tabela itens_compra
                                val itemCursor = db.query("PRAGMA table_info(itens_compra)")
                                val columns = mutableListOf<String>()
                                while (itemCursor.moveToNext()) {
                                    val columnName = itemCursor.getString(1)
                                    val columnType = itemCursor.getString(2)
                                    columns.add("$columnName ($columnType)")
                                }
                                itemCursor.close()
                                android.util.Log.d("AppDatabase", "Colunas da tabela itens_compra: $columns")
                                
                                // Verificar se a tabela shopping_lists existe e tem o registro padrão
                                if (tables.contains("shopping_lists")) {
                                    android.util.Log.d("AppDatabase", "Verificando lista padrão em onOpen")
                                    val listCursor = db.query("SELECT id FROM shopping_lists WHERE id = 1")
                                    val hasDefaultList = listCursor.count > 0
                                    listCursor.close()
                                    android.util.Log.d("AppDatabase", "Lista padrão existe em onOpen: $hasDefaultList")
                                    
                                    if (!hasDefaultList) {
                                        android.util.Log.w("AppDatabase", "Lista padrão não encontrada em onOpen, criando...")
                                        db.execSQL(
                                            """
                                            INSERT INTO shopping_lists (id, nome, dataCriacao, isAtiva, ordem)
                                            VALUES (1, 'Lista de Compras Padrão', ${System.currentTimeMillis()}, 1, 0)
                                            """.trimIndent()
                                        )
                                        android.util.Log.d("AppDatabase", "Lista padrão criada em onOpen")
                                    }
                                }
                                
                            } catch (e: Exception) {
                                android.util.Log.e("AppDatabase", "Erro ao verificar estrutura do banco", e)
                            }
                        }
                    })
                    
                    android.util.Log.d("AppDatabase", "Construindo banco de dados")
                    val db = builder.build()
                    android.util.Log.d("AppDatabase", "Banco de dados construído com sucesso")
                    db
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Erro ao criar banco de dados", e)
                    android.util.Log.e("AppDatabase", "Stack trace: ${e.stackTraceToString()}")
                    // Tentar criar sem migrações como último recurso
                    try {
                        android.util.Log.d("AppDatabase", "Tentando criar banco com fallback destructive")
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "compras_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                    } catch (e2: Exception) {
                        android.util.Log.e("AppDatabase", "Erro crítico ao criar banco de dados", e2)
                        android.util.Log.e("AppDatabase", "Stack trace: ${e2.stackTraceToString()}")
                        throw RuntimeException("Não foi possível inicializar o banco de dados", e2)
                    }
                }
                INSTANCE = instance
                android.util.Log.d("AppDatabase", "=== BANCO DE DADOS INICIALIZADO COM SUCESSO ===")
                instance
            }
        }
    }
}

