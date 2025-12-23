package com.example.minhascompras.data

import com.example.minhascompras.utils.DebugLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ShoppingListRepository(
    private val shoppingListDao: ShoppingListDao
) {
    val allLists: Flow<List<ShoppingList>> = shoppingListDao.getAllLists()

    suspend fun getAllListsList(): List<ShoppingList> {
        return shoppingListDao.getAllLists().first()
    }

    fun getListById(id: Long): Flow<ShoppingList?> {
        return shoppingListDao.getListById(id)
    }

    suspend fun getListByIdSync(id: Long): ShoppingList? {
        return shoppingListDao.getListByIdSync(id)
    }

    fun getDefaultList(): Flow<ShoppingList?> {
        return shoppingListDao.getDefaultList()
    }

    suspend fun getDefaultListSync(): ShoppingList? {
        return shoppingListDao.getDefaultListSync()
    }

    suspend fun createList(nome: String): Long {
        val newList = ShoppingList(
            nome = nome,
            dataCriacao = System.currentTimeMillis(),
            isDefault = false
        )
        return shoppingListDao.insert(newList)
    }

    suspend fun updateList(list: ShoppingList) {
        shoppingListDao.update(list)
    }

    suspend fun deleteList(id: Long): Result<Unit> {
        val list = shoppingListDao.getListByIdSync(id)
        
        // Proteção: não permitir deletar lista padrão
        if (list?.isDefault == true) {
            return Result.failure(
                IllegalStateException("Não é possível deletar a lista padrão")
            )
        }
        
        shoppingListDao.deleteById(id)
        return Result.success(Unit)
    }

    suspend fun deleteList(list: ShoppingList): Result<Unit> {
        // Proteção: não permitir deletar lista padrão
        if (list.isDefault) {
            return Result.failure(
                IllegalStateException("Não é possível deletar a lista padrão")
            )
        }
        
        shoppingListDao.delete(list)
        return Result.success(Unit)
    }

    fun getListCount(): Flow<Int> {
        return shoppingListDao.getListCount()
    }

    fun getItemCountForList(listId: Long): Flow<Int> {
        return shoppingListDao.getItemCountForList(listId)
    }

    /**
     * Garante que sempre exista uma lista padrão.
     * Se não existir, cria a lista "Minhas Compras" com ID 1.
     * Se existir lista com ID 1 mas não for padrão, atualiza para padrão.
     */
    suspend fun ensureDefaultListExists() {
        // #region agent log
        DebugLogger.log(
            location = "ShoppingListRepository.kt:ensureDefaultListExists",
            message = "function called",
            data = emptyMap(),
            hypothesisId = "D"
        )
        // #endregion
        
        val defaultList = shoppingListDao.getDefaultListSync()
        
        // #region agent log
        DebugLogger.log(
            location = "ShoppingListRepository.kt:ensureDefaultListExists",
            message = "defaultList checked",
            data = mapOf("defaultListFound" to (defaultList != null)),
            hypothesisId = "D"
        )
        // #endregion
        
        if (defaultList == null) {
            // Verificar se existe lista com ID 1 (pode ter sido criada sem ser padrão)
            val listWithId1 = shoppingListDao.getListByIdSync(1)
            
            // #region agent log
            DebugLogger.log(
                location = "ShoppingListRepository.kt:ensureDefaultListExists",
                message = "listWithId1 checked",
                data = mapOf("listWithId1Found" to (listWithId1 != null)),
                hypothesisId = "D"
            )
            // #endregion
            
            if (listWithId1 != null) {
                // Se existe lista com ID 1 mas não é padrão, atualizar para padrão
                val updatedList = listWithId1.copy(isDefault = true)
                shoppingListDao.update(updatedList)
                
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListRepository.kt:ensureDefaultListExists",
                    message = "listWithId1 updated to default",
                    data = mapOf("listId" to listWithId1.id),
                    hypothesisId = "D"
                )
                // #endregion
            } else {
                // Criar nova lista padrão com ID 1
                val newDefaultList = ShoppingList(
                    id = 1,
                    nome = "Minhas Compras",
                    dataCriacao = System.currentTimeMillis(),
                    isDefault = true
                )
                shoppingListDao.insert(newDefaultList)
                
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListRepository.kt:ensureDefaultListExists",
                    message = "new default list created",
                    data = mapOf("listId" to 1L),
                    hypothesisId = "D"
                )
                // #endregion
            }
        }
    }
}

