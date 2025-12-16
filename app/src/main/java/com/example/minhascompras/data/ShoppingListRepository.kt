package com.example.minhascompras.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShoppingListRepository(
    private val shoppingListDao: ShoppingListDao
) {
    val allLists: Flow<List<ShoppingList>> = shoppingListDao.getAllLists()

    fun getListById(listId: Long): Flow<ShoppingList?> {
        return shoppingListDao.getListById(listId)
    }

    suspend fun getListByIdSync(listId: Long): ShoppingList? {
        return shoppingListDao.getListByIdSync(listId)
    }

    fun getDefaultList(): Flow<ShoppingList?> {
        return shoppingListDao.getDefaultList()
    }

    suspend fun getDefaultListSync(): ShoppingList? {
        return shoppingListDao.getDefaultListSync()
    }

    suspend fun insert(list: ShoppingList): Long {
        return shoppingListDao.insert(list)
    }

    suspend fun update(list: ShoppingList) {
        shoppingListDao.update(list)
    }

    suspend fun delete(list: ShoppingList) {
        // Não há mais lista padrão, então qualquer lista pode ser deletada
        shoppingListDao.delete(list)
    }

    suspend fun deleteById(listId: Long) {
        // Não há mais lista padrão, então qualquer lista pode ser deletada
        shoppingListDao.deleteById(listId)
    }

    suspend fun getListCount(): Int {
        return shoppingListDao.getListCount()
    }

    suspend fun getNonDefaultListCount(): Int {
        return shoppingListDao.getNonDefaultListCount()
    }

    suspend fun getItemCountForList(listId: Long): Int {
        return shoppingListDao.getItemCountForList(listId)
    }

    suspend fun ensureDefaultListExists() {
        // Não criar lista padrão - removido para permitir que usuários criem suas próprias listas
        // Este método é mantido para compatibilidade, mas não faz nada
    }
}

