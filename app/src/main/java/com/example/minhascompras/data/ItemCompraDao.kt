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
    suspend fun insert(item: ItemCompra): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemCompra>)

    @Update
    suspend fun update(item: ItemCompra)

    @Delete
    suspend fun delete(item: ItemCompra)

    @Query("DELETE FROM itens_compra WHERE comprado = 1")
    suspend fun deleteComprados()

    @Query("DELETE FROM itens_compra")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAllItems(items: List<ItemCompra>) {
        deleteAll()
        insertAll(items)
    }
}

