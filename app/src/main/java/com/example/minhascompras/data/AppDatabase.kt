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
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemCompraDao(): ItemCompraDao
    abstract fun shoppingListDao(): ShoppingListDao
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
                // 1. Criar tabela shopping_lists
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS shopping_lists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nome TEXT NOT NULL,
                        dataCriacao INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                // 2. Inserir lista padrão "Minhas Compras" com ID 1
                // Usar timestamp atual do SQLite no momento da execução
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO shopping_lists (id, nome, dataCriacao, isDefault)
                    VALUES (1, 'Minhas Compras', (strftime('%s', 'now') * 1000), 1)
                    """.trimIndent()
                )

                // 3. Adicionar coluna listId na tabela itens_compra
                // Primeiro, verificar se a coluna já existe (para evitar erro em re-execução)
                try {
                    database.execSQL(
                        "ALTER TABLE itens_compra ADD COLUMN listId INTEGER NOT NULL DEFAULT 1"
                    )
                } catch (e: Exception) {
                    // Coluna já existe, ignorar erro
                    android.util.Log.d("AppDatabase", "Coluna listId já existe na tabela itens_compra")
                }

                // 4. Atualizar todos os itens existentes para usar listId = 1 (lista padrão)
                database.execSQL(
                    "UPDATE itens_compra SET listId = 1 WHERE listId IS NULL OR listId = 0"
                )

                // 5. Criar índice para melhor performance nas consultas por lista
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_itens_compra_listId ON itens_compra(listId)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar coluna listId na tabela shopping_list_history
                try {
                    database.execSQL(
                        "ALTER TABLE shopping_list_history ADD COLUMN listId INTEGER"
                    )
                } catch (e: Exception) {
                    // Coluna já existe, ignorar erro
                    android.util.Log.d("AppDatabase", "Coluna listId já existe na tabela shopping_list_history")
                }
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adicionar coluna isArchived na tabela shopping_lists
                try {
                    database.execSQL(
                        "ALTER TABLE shopping_lists ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0"
                    )
                } catch (e: Exception) {
                    // Coluna já existe, ignorar erro
                    android.util.Log.d("AppDatabase", "Coluna isArchived já existe na tabela shopping_lists")
                }
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remover a lista padrão "Minhas Compras" se existir
                database.execSQL(
                    "DELETE FROM shopping_lists WHERE isDefault = 1 AND nome = 'Minhas Compras'"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "compras_database"
                    )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .fallbackToDestructiveMigration() // Fallback seguro em caso de erro de migração
                    .build()
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Erro ao criar banco de dados", e)
                    // Tentar criar sem migrações como último recurso
                    try {
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "compras_database"
                        )
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

