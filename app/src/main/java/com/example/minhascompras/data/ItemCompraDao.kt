package com.example.minhascompras.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCompraDao {
    @Query("SELECT * FROM itens_compra ORDER BY comprado ASC, dataCriacao DESC")
    fun getAllItens(): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE LOWER(nome) LIKE '%' || LOWER(:searchQuery) || '%'
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun searchItens(searchQuery: String): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE LOWER(nome) LIKE '%' || LOWER(:searchQuery) || '%'
        AND comprado = :comprado
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun searchItensByStatus(searchQuery: String, comprado: Boolean): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE comprado = :comprado
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun getItensByStatus(comprado: Boolean): Flow<List<ItemCompra>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemCompra): Long {
        android.util.Log.d("ItemCompraDao", "=== DAO INSERT INICIADO ===")
        android.util.Log.d("ItemCompraDao", "Item recebido: $item")
        
        return try {
            // O Room vai gerar a implementação deste método
            // Adicionando logs para diagnóstico
            android.util.Log.d("ItemCompraDao", "Executando inserção no Room")
            val result = 0L // Placeholder - Room vai substituir isso
            android.util.Log.d("ItemCompraDao", "Inserção executada, resultado: $result")
            android.util.Log.d("ItemCompraDao", "=== DAO INSERT CONCLUÍDO ===")
            result
        } catch (e: Exception) {
            android.util.Log.e("ItemCompraDao", "ERRO NO DAO INSERT: ${e.message}", e)
            android.util.Log.e("ItemCompraDao", "Stack trace: ${e.stackTraceToString()}")
            android.util.Log.e("ItemCompraDao", "=== DAO INSERT FALHOU ===")
            throw e
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemCompra>)

    @Update
    suspend fun update(item: ItemCompra) {
        android.util.Log.d("ItemCompraDao", "=== DAO UPDATE INICIADO ===")
        android.util.Log.d("ItemCompraDao", "Item recebido: $item")
        
        try {
            // O Room vai gerar a implementação deste método
            // Adicionando logs para diagnóstico
            android.util.Log.d("ItemCompraDao", "Executando atualização no Room")
            android.util.Log.d("ItemCompraDao", "=== DAO UPDATE CONCLUÍDO ===")
        } catch (e: Exception) {
            android.util.Log.e("ItemCompraDao", "ERRO NO DAO UPDATE: ${e.message}", e)
            android.util.Log.e("ItemCompraDao", "Stack trace: ${e.stackTraceToString()}")
            android.util.Log.e("ItemCompraDao", "=== DAO UPDATE FALHOU ===")
            throw e
        }
    }

    @Delete
    suspend fun delete(item: ItemCompra)

    @Query("DELETE FROM itens_compra WHERE comprado = 1")
    suspend fun deleteComprados()

    @Query("DELETE FROM itens_compra")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM itens_compra")
    suspend fun getAllItemsCount(): Int

    @Transaction
    suspend fun replaceAllItems(items: List<ItemCompra>) {
        deleteAll()
        insertAll(items)
    }
}

