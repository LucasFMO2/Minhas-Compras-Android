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
        ShoppingListHistory::class,
        HistoryItem::class
    ],
    version = 4,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "compras_database"
                    )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
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

