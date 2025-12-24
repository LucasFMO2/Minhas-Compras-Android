package com.example.minhascompras.data

import android.content.Context
import com.example.minhascompras.data.supabase.AuthService
import com.example.minhascompras.data.supabase.SupabaseSyncService
import com.example.minhascompras.notifications.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ItemCompraRepository(
    private val itemCompraDao: ItemCompraDao,
    private val historyDao: HistoryDao,
    private val shoppingListDao: ShoppingListDao? = null
) {
    private val syncService = SupabaseSyncService(itemCompraDao, historyDao)
    // AuthService inicializado de forma lazy para evitar crashes na inicialização
    private val authService: AuthService by lazy { 
        try {
            AuthService.getInstance()
        } catch (e: Exception) {
            android.util.Log.e("ItemCompraRepository", "Erro ao inicializar AuthService", e)
            // Retornar uma instância mesmo em caso de erro
            AuthService.getInstance()
        }
    }
    val allItens: Flow<List<ItemCompra>> = itemCompraDao.getAllItens()

    suspend fun getAllItensList(): List<ItemCompra> {
        return itemCompraDao.getAllItens().first()
    }

    fun getAllItensByList(listId: Long): Flow<List<ItemCompra>> {
        return itemCompraDao.getItensByList(listId)
    }

    suspend fun getAllItensListByList(listId: Long): List<ItemCompra> {
        return itemCompraDao.getItensByList(listId).first()
    }

    fun getFilteredItens(
        searchQuery: String, 
        filterStatus: FilterStatus,
        sortOrder: SortOrder
    ): Flow<List<ItemCompra>> {
        val normalizedQuery = searchQuery.trim()
        
        val baseFlow = when (filterStatus) {
            FilterStatus.ALL -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getAllItens()
                } else {
                    itemCompraDao.searchItens(normalizedQuery)
                }
            }
            FilterStatus.PENDING -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByStatus(comprado = false)
                } else {
                    itemCompraDao.searchItensByStatus(normalizedQuery, comprado = false)
                }
            }
            FilterStatus.PURCHASED -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByStatus(comprado = true)
                } else {
                    itemCompraDao.searchItensByStatus(normalizedQuery, comprado = true)
                }
            }
        }
        
        return baseFlow.map { items -> applySortOrder(items, sortOrder) }
    }

    fun getFilteredItensByList(
        listId: Long,
        searchQuery: String, 
        filterStatus: FilterStatus,
        sortOrder: SortOrder
    ): Flow<List<ItemCompra>> {
        val normalizedQuery = searchQuery.trim()
        
        val baseFlow = when (filterStatus) {
            FilterStatus.ALL -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByList(listId)
                } else {
                    itemCompraDao.searchItensByList(listId, normalizedQuery)
                }
            }
            FilterStatus.PENDING -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByListAndStatus(listId, comprado = false)
                } else {
                    itemCompraDao.searchItensByListAndStatus(listId, normalizedQuery, comprado = false)
                }
            }
            FilterStatus.PURCHASED -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByListAndStatus(listId, comprado = true)
                } else {
                    itemCompraDao.searchItensByListAndStatus(listId, normalizedQuery, comprado = true)
                }
            }
        }
        
        return baseFlow.map { items -> applySortOrder(items, sortOrder) }
    }
    
    private fun applySortOrder(items: List<ItemCompra>, sortOrder: SortOrder): List<ItemCompra> {
        // Primeiro, separar itens comprados e não comprados
        val comprados = items.filter { it.comprado }
        val naoComprados = items.filter { !it.comprado }
        
        // Aplicar ordenação em cada grupo
        val sortedNaoComprados = when (sortOrder) {
            SortOrder.BY_NAME_ASC -> naoComprados.sortedBy { it.nome.lowercase() }
            SortOrder.BY_NAME_DESC -> naoComprados.sortedByDescending { it.nome.lowercase() }
            SortOrder.BY_DATE_ASC -> naoComprados.sortedBy { it.dataCriacao }
            SortOrder.BY_DATE_DESC -> naoComprados.sortedByDescending { it.dataCriacao }
            SortOrder.BY_PRICE_ASC -> naoComprados.sortedWith(compareBy(nullsLast()) { it.preco })
            SortOrder.BY_PRICE_DESC -> naoComprados.sortedWith(compareByDescending(nullsLast()) { it.preco })
        }
        
        val sortedComprados = when (sortOrder) {
            SortOrder.BY_NAME_ASC -> comprados.sortedBy { it.nome.lowercase() }
            SortOrder.BY_NAME_DESC -> comprados.sortedByDescending { it.nome.lowercase() }
            SortOrder.BY_DATE_ASC -> comprados.sortedBy { it.dataCriacao }
            SortOrder.BY_DATE_DESC -> comprados.sortedByDescending { it.dataCriacao }
            SortOrder.BY_PRICE_ASC -> comprados.sortedWith(compareBy(nullsLast()) { it.preco })
            SortOrder.BY_PRICE_DESC -> comprados.sortedWith(compareByDescending(nullsLast()) { it.preco })
        }
        
        // Retornar não comprados primeiro, depois comprados
        return sortedNaoComprados + sortedComprados
    }

    suspend fun insert(item: ItemCompra): Long {
        val id = itemCompraDao.insert(item)
        // Sincronizar com Supabase se disponível e usuário autenticado
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            val insertedItem = item.copy(id = id)
            syncService.syncItemToSupabase(insertedItem, userId).onFailure {
                // Log do erro, mas não falha a operação local
            }
        }
        return id
    }

    suspend fun update(item: ItemCompra) {
        itemCompraDao.update(item)
        // Sincronizar com Supabase se disponível e usuário autenticado
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            syncService.syncItemToSupabase(item, userId).onFailure {
                // Log do erro, mas não falha a operação local
            }
        }
    }
    
    /**
     * Verifica se todos os itens de uma lista foram comprados e envia notificação se necessário.
     * 
     * @param context Contexto da aplicação (para acessar NotificationPreferencesManager)
     * @param listId ID da lista a verificar (null para lista padrão)
     */
    suspend fun checkAndNotifyCompletion(context: Context, listId: Long?) {
        if (listId == null) return
        
        try {
            val allItems = itemCompraDao.getItensByList(listId).first()
            if (allItems.isNotEmpty() && allItems.all { it.comprado }) {
                // Todos os itens foram comprados - verificar se notificação está habilitada
                val notificationPrefsManager = NotificationPreferencesManager(context)
                val isEnabled = notificationPrefsManager.isCompletionNotificationEnabled().first()
                
                if (isEnabled) {
                    NotificationHelper.showCompletionNotification(context)
                }
            }
        } catch (e: Exception) {
            // Ignorar erros silenciosamente
            android.util.Log.e("ItemCompraRepository", "Erro ao verificar conclusão", e)
        }
    }

    suspend fun delete(item: ItemCompra) {
        itemCompraDao.delete(item)
        // Sincronizar com Supabase se disponível e usuário autenticado
        if (syncService.isAvailable() && authService.isAuthenticated() && item.id > 0) {
            val userId = authService.getCurrentUserId()
            syncService.deleteItemFromSupabase(item.id, userId).onFailure {
                // Log do erro, mas não falha a operação local
            }
        }
    }

    suspend fun deleteComprados() {
        // Obter itens comprados antes de deletar para sincronizar
        val comprados = itemCompraDao.getAllItens().first().filter { it.comprado }
        itemCompraDao.deleteComprados()
        // Sincronizar deleções com Supabase
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            comprados.forEach { item ->
                if (item.id > 0) {
                    syncService.deleteItemFromSupabase(item.id, userId).onFailure {
                        // Log do erro, mas não falha a operação local
                    }
                }
            }
        }
    }

    suspend fun deleteCompradosByList(listId: Long) {
        // Obter itens comprados da lista antes de deletar para sincronizar
        val comprados = itemCompraDao.getItensByList(listId).first().filter { it.comprado }
        itemCompraDao.deleteCompradosByList(listId)
        // Sincronizar deleções com Supabase
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            comprados.forEach { item ->
                if (item.id > 0) {
                    syncService.deleteItemFromSupabase(item.id, userId).onFailure {
                        // Log do erro, mas não falha a operação local
                    }
                }
            }
        }
    }

    suspend fun deleteAll() {
        // Obter todos os itens antes de deletar para sincronizar
        val todosItens = itemCompraDao.getAllItens().first()
        itemCompraDao.deleteAll()
        // Sincronizar deleções com Supabase
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            todosItens.forEach { item ->
                if (item.id > 0) {
                    syncService.deleteItemFromSupabase(item.id, userId).onFailure {
                        // Log do erro, mas não falha a operação local
                    }
                }
            }
        }
    }

    suspend fun deleteAllByList(listId: Long) {
        // Obter todos os itens da lista antes de deletar para sincronizar
        val todosItens = itemCompraDao.getItensByList(listId).first()
        itemCompraDao.deleteAllByList(listId)
        // Sincronizar deleções com Supabase
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            todosItens.forEach { item ->
                if (item.id > 0) {
                    syncService.deleteItemFromSupabase(item.id, userId).onFailure {
                        // Log do erro, mas não falha a operação local
                    }
                }
            }
        }
    }

    suspend fun replaceAllItems(items: List<ItemCompra>) {
        itemCompraDao.replaceAllItems(items)
    }

    // Funções de histórico
    suspend fun archiveCurrentList(listId: Long, listName: String) {
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:archiveCurrentList",
            message = "function called",
            data = mapOf("listId" to listId, "listName" to listName),
            hypothesisId = "F"
        )
        // #endregion
        
        val items = itemCompraDao.getItensByList(listId).first()
        
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:archiveCurrentList",
            message = "items retrieved",
            data = mapOf("itemCount" to items.size),
            hypothesisId = "F"
        )
        // #endregion
        
        if (items.isEmpty()) {
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "ItemCompraRepository.kt:archiveCurrentList",
                message = "no items to archive, returning",
                data = emptyMap(),
                hypothesisId = "F"
            )
            // #endregion
            return
        }
        
        val history = ShoppingListHistory(
            completionDate = System.currentTimeMillis(),
            listName = listName,
            listId = listId // Associar histórico à lista específica
        )
        
        val historyItems = items.map { item ->
            HistoryItem(
                parentListId = 0, // Será atualizado na transação
                nome = item.nome,
                quantidade = item.quantidade,
                preco = item.preco,
                categoria = item.categoria
            )
        }
        
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:archiveCurrentList",
            message = "inserting history with items",
            data = mapOf("historyItemsCount" to historyItems.size),
            hypothesisId = "F"
        )
        // #endregion
        
        historyDao.insertHistoryWithItems(history, historyItems)
        
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:archiveCurrentList",
            message = "history inserted, deleting items",
            data = mapOf("listId" to listId),
            hypothesisId = "F"
        )
        // #endregion
        
        // Sincronizar histórico com Supabase se disponível
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            syncService.syncHistoryToSupabase(history, userId).onFailure {
                // Log do erro, mas não falha a operação local
            }
        }
        
        // Limpar a lista atual
        itemCompraDao.deleteAllByList(listId)
        
        // Marcar a lista como arquivada
        shoppingListDao?.let { dao ->
            val list = dao.getListByIdSync(listId)
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "ItemCompraRepository.kt:archiveCurrentList",
                message = "before marking as archived",
                data = mapOf(
                    "listId" to listId,
                    "listFound" to (list != null),
                    "currentIsArchived" to (list?.isArchived ?: false)
                ),
                hypothesisId = "C"
            )
            // #endregion
            
            if (list != null) {
                val archivedList = list.copy(isArchived = true)
                dao.update(archivedList)
                
                // #region agent log
                com.example.minhascompras.utils.DebugLogger.log(
                    location = "ItemCompraRepository.kt:archiveCurrentList",
                    message = "list marked as archived",
                    data = mapOf(
                        "listId" to listId,
                        "listName" to list.nome,
                        "isArchived" to true
                    ),
                    hypothesisId = "C"
                )
                // #endregion
            } else {
                // #region agent log
                com.example.minhascompras.utils.DebugLogger.log(
                    location = "ItemCompraRepository.kt:archiveCurrentList",
                    message = "list not found, cannot mark as archived",
                    data = mapOf("listId" to listId),
                    hypothesisId = "C"
                )
                // #endregion
            }
        } ?: run {
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "ItemCompraRepository.kt:archiveCurrentList",
                message = "shoppingListDao is null, cannot mark as archived",
                data = mapOf("listId" to listId),
                hypothesisId = "C"
            )
            // #endregion
        }
        
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:archiveCurrentList",
            message = "archive completed",
            data = mapOf("listId" to listId),
            hypothesisId = "F"
        )
        // #endregion
    }

    fun getHistoryLists(): Flow<List<ShoppingListHistory>> {
        return historyDao.getAllHistoryLists()
    }

    /**
     * Obtém histórico de listas, opcionalmente filtrado por listId.
     * Se listId for null, retorna histórico de todas as listas.
     * Se listId for fornecido, retorna apenas histórico daquela lista específica.
     */
    fun getHistoryLists(listId: Long?): Flow<List<ShoppingListHistory>> {
        return if (listId == null) {
            historyDao.getAllHistoryLists()
        } else {
            historyDao.getHistoryListsByListId(listId)
        }
    }

    fun getHistoryListWithItems(historyId: Long): Flow<ShoppingListHistoryWithItems?> {
        return historyDao.getHistoryListWithItems(historyId)
    }

    suspend fun deleteHistory(historyId: Long) {
        historyDao.deleteHistoryById(historyId)
    }

    suspend fun reuseHistoryList(historyId: Long, listId: Long) {
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:reuseHistoryList",
            message = "function called",
            data = mapOf(
                "historyId" to historyId,
                "listId" to listId
            ),
            hypothesisId = "REUSE"
        )
        // #endregion
        
        val historyWithItems = historyDao.getHistoryListWithItems(historyId).first()
        
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "ItemCompraRepository.kt:reuseHistoryList",
            message = "historyWithItems retrieved",
            data = mapOf(
                "historyId" to historyId,
                "historyWithItemsNotNull" to (historyWithItems != null),
                "itemsCount" to (historyWithItems?.items?.size ?: 0)
            ),
            hypothesisId = "REUSE"
        )
        // #endregion
        
        if (historyWithItems != null) {
            // Verificar se já existem itens nesta lista antes de inserir
            val existingItems = itemCompraDao.getItensByList(listId).first()
            
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "ItemCompraRepository.kt:reuseHistoryList",
                message = "checked existing items",
                data = mapOf(
                    "listId" to listId,
                    "existingItemsCount" to existingItems.size
                ),
                hypothesisId = "REUSE"
            )
            // #endregion
            
            // Se já existem itens, deletar antes de inserir novos (evitar duplicação)
            if (existingItems.isNotEmpty()) {
                // #region agent log
                com.example.minhascompras.utils.DebugLogger.log(
                    location = "ItemCompraRepository.kt:reuseHistoryList",
                    message = "deleting existing items to avoid duplication",
                    data = mapOf(
                        "listId" to listId,
                        "existingItemsCount" to existingItems.size
                    ),
                    hypothesisId = "REUSE"
                )
                // #endregion
                itemCompraDao.deleteAllByList(listId)
            }
            
            val items = historyWithItems.items.map { historyItem ->
                ItemCompra(
                    nome = historyItem.nome,
                    quantidade = historyItem.quantidade,
                    preco = historyItem.preco,
                    categoria = historyItem.categoria,
                    comprado = false, // Resetar para não comprado
                    dataCriacao = System.currentTimeMillis(),
                    listId = listId
                )
            }
            
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "ItemCompraRepository.kt:reuseHistoryList",
                message = "inserting items",
                data = mapOf(
                    "listId" to listId,
                    "itemsCount" to items.size
                ),
                hypothesisId = "REUSE"
            )
            // #endregion
            
            itemCompraDao.insertAll(items)
            
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "ItemCompraRepository.kt:reuseHistoryList",
                message = "items inserted",
                data = mapOf(
                    "listId" to listId,
                    "itemsCount" to items.size
                ),
                hypothesisId = "REUSE"
            )
            // #endregion
            
            // Desarquivar a lista original se o histórico tiver listId associado
            historyWithItems.history.listId?.let { originalListId ->
                shoppingListDao?.let { dao ->
                    val originalList = dao.getListByIdSync(originalListId)
                    if (originalList != null && originalList.isArchived) {
                        val unarchivedList = originalList.copy(isArchived = false)
                        dao.update(unarchivedList)
                    }
                }
            }

            // NÃO deletar o histórico após reutilizar - manter para reutilização futura
            // historyDao.deleteHistoryById(historyId)
        }
    }

    /**
     * Sincroniza todos os dados locais para o Supabase
     */
    suspend fun syncToSupabase(): Result<Unit> {
        if (!syncService.isAvailable() || !authService.isAuthenticated()) {
            return Result.failure(Exception("Supabase não disponível ou usuário não autenticado"))
        }
        val userId = authService.getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))
        return syncService.syncAllItemsToSupabase(userId)
    }

    /**
     * Sincroniza dados do Supabase para o local
     */
    suspend fun syncFromSupabase(): Result<Unit> {
        if (!syncService.isAvailable() || !authService.isAuthenticated()) {
            return Result.failure(Exception("Supabase não disponível ou usuário não autenticado"))
        }
        val userId = authService.getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado"))
        return syncService.syncItemsFromSupabase(userId)
    }

    /**
     * Verifica se a sincronização está disponível
     */
    fun isSyncAvailable(): Boolean {
        return syncService.isAvailable() && authService.isAuthenticated()
    }
}

