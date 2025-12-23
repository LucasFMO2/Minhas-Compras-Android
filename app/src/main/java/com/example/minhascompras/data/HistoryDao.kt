package com.example.minhascompras.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM shopping_list_history ORDER BY completionDate DESC")
    fun getAllHistoryLists(): Flow<List<ShoppingListHistory>>

    @Transaction
    @Query("SELECT * FROM shopping_list_history WHERE id = :historyId")
    fun getHistoryListWithItems(historyId: Long): Flow<ShoppingListHistoryWithItems?>

    @Transaction
    @Query("SELECT * FROM shopping_list_history ORDER BY completionDate DESC")
    fun getAllHistoryListsWithItems(): Flow<List<ShoppingListHistoryWithItems>>

    // Queries filtradas por listId
    @Query("SELECT * FROM shopping_list_history WHERE listId = :listId ORDER BY completionDate DESC")
    fun getHistoryListsByListId(listId: Long): Flow<List<ShoppingListHistory>>

    @Transaction
    @Query("SELECT * FROM shopping_list_history WHERE listId = :listId ORDER BY completionDate DESC")
    fun getAllHistoryListsWithItemsByListId(listId: Long): Flow<List<ShoppingListHistoryWithItems>>

    @Transaction
    @Query("SELECT * FROM shopping_list_history WHERE id = :historyId AND listId = :listId")
    fun getHistoryListWithItemsByListId(historyId: Long, listId: Long): Flow<ShoppingListHistoryWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ShoppingListHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItems(items: List<HistoryItem>)

    @Delete
    suspend fun deleteHistory(history: ShoppingListHistory)

    @Query("DELETE FROM shopping_list_history WHERE id = :historyId")
    suspend fun deleteHistoryById(historyId: Long)

    @Transaction
    suspend fun insertHistoryWithItems(
        history: ShoppingListHistory,
        items: List<HistoryItem>
    ): Long {
        val historyId = insertHistory(history)
        val itemsWithParentId = items.map { it.copy(parentListId = historyId) }
        insertHistoryItems(itemsWithParentId)
        return historyId
    }
}

