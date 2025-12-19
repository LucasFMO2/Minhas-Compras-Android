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
                // Garantir que a tabela shopping_lists exista
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
                
                // Inserir lista padrão com id=1 se não existir
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO shopping_lists (id, nome, dataCriacao, isAtiva, ordem)
                    VALUES (1, 'Lista de Compras Padrão', ${System.currentTimeMillis()}, 1, 0)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migração para v2.18.0 - Adicionar colunas de otimização
                database.execSQL(
                    "ALTER TABLE itens_compra ADD COLUMN last_updated INTEGER DEFAULT 0"
                )
                
                // Adicionar índice para performance
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_itens_compra_last_updated ON itens_compra(last_updated)"
                )
                
                // Atualizar registros existentes
                database.execSQL(
                    "UPDATE itens_compra SET last_updated = strftime('%s','now') WHERE last_updated = 0"
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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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

