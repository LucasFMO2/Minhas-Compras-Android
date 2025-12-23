package com.example.minhascompras.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("""
        SELECT * FROM shopping_lists 
        WHERE isArchived = 0
        ORDER BY isDefault DESC, dataCriacao DESC
    """)
    fun getAllLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun getListById(id: Long): Flow<ShoppingList?>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getListByIdSync(id: Long): ShoppingList?

    @Query("SELECT * FROM shopping_lists WHERE isDefault = 1 LIMIT 1")
    fun getDefaultList(): Flow<ShoppingList?>

    @Query("SELECT * FROM shopping_lists WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultListSync(): ShoppingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingList): Long

    @Update
    suspend fun update(list: ShoppingList)

    @Delete
    suspend fun delete(list: ShoppingList)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM shopping_lists")
    fun getListCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM itens_compra 
        WHERE listId = :listId
    """)
    fun getItemCountForList(listId: Long): Flow<Int>
}

