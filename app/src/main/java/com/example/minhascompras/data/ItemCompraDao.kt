package com.example.minhascompras.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCompraDao {
    @Query("SELECT * FROM itens_compra ORDER BY comprado ASC, dataCriacao DESC")
    fun getAllItens(): Flow<List<ItemCompra>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemCompra): Long

    @Update
    suspend fun update(item: ItemCompra)

    @Delete
    suspend fun delete(item: ItemCompra)

    @Query("DELETE FROM itens_compra WHERE comprado = 1")
    suspend fun deleteComprados()
}

