package com.example.minhascompras.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCompraDao {
    @Query("SELECT * FROM itens_compra ORDER BY comprado ASC, dataCriacao DESC")
    fun getAllItens(): Flow<List<ItemCompra>>

    @Query("SELECT * FROM itens_compra WHERE listId = :listId ORDER BY comprado ASC, dataCriacao DESC")
    fun getItensByList(listId: Long): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE LOWER(nome) LIKE '%' || LOWER(:searchQuery) || '%'
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun searchItens(searchQuery: String): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE listId = :listId
        AND LOWER(nome) LIKE '%' || LOWER(:searchQuery) || '%'
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun searchItensByList(listId: Long, searchQuery: String): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE LOWER(nome) LIKE '%' || LOWER(:searchQuery) || '%'
        AND comprado = :comprado
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun searchItensByStatus(searchQuery: String, comprado: Boolean): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE listId = :listId
        AND LOWER(nome) LIKE '%' || LOWER(:searchQuery) || '%'
        AND comprado = :comprado
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun searchItensByListAndStatus(listId: Long, searchQuery: String, comprado: Boolean): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra 
        WHERE comprado = :comprado
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun getItensByStatus(comprado: Boolean): Flow<List<ItemCompra>>

    @Query("""
        SELECT * FROM itens_compra
        WHERE listId = :listId
        AND comprado = :comprado
        ORDER BY comprado ASC, dataCriacao DESC
    """)
    fun getItensByListAndStatus(listId: Long, comprado: Boolean): Flow<List<ItemCompra>>

    @Query("SELECT * FROM itens_compra WHERE id = :id")
    suspend fun getItemById(id: Long): ItemCompra?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemCompra): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemCompra>)

    @Update
    suspend fun update(item: ItemCompra)

    @Delete
    suspend fun delete(item: ItemCompra)

    @Query("DELETE FROM itens_compra WHERE comprado = 1")
    suspend fun deleteComprados()

    @Query("DELETE FROM itens_compra WHERE listId = :listId AND comprado = 1")
    suspend fun deleteCompradosByList(listId: Long)

    @Query("DELETE FROM itens_compra")
    suspend fun deleteAll()

    @Query("DELETE FROM itens_compra WHERE listId = :listId")
    suspend fun deleteAllByList(listId: Long)

    @Query("SELECT COUNT(*) FROM itens_compra WHERE comprado = 0")
    suspend fun countPendingItems(): Int

    @Query("""
        SELECT COUNT(*) FROM itens_compra 
        WHERE comprado = 0 
        AND listId = :listId
    """)
    suspend fun countPendingItemsByList(listId: Long): Int

    @Query("""
        SELECT * FROM itens_compra 
        WHERE comprado = 0 
        AND dataCriacao < :cutoffTime
        ORDER BY dataCriacao ASC
    """)
    suspend fun getPendingItemsOlderThan(cutoffTime: Long): List<ItemCompra>

    @Transaction
    suspend fun replaceAllItems(items: List<ItemCompra>) {
        deleteAll()
        insertAll(items)
    }
}

