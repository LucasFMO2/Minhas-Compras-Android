package com.example.minhascompras.data

import com.example.minhascompras.data.supabase.AuthService
import com.example.minhascompras.data.supabase.SupabaseSyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ItemCompraRepository(
    private val itemCompraDao: ItemCompraDao,
    private val historyDao: HistoryDao
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

    suspend fun replaceAllItems(items: List<ItemCompra>) {
        itemCompraDao.replaceAllItems(items)
    }

    // Funções de histórico
    suspend fun archiveCurrentList(items: List<ItemCompra>) {
        if (items.isEmpty()) return
        
        val history = ShoppingListHistory(
            completionDate = System.currentTimeMillis(),
            listName = "Lista de Compras"
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
        
        historyDao.insertHistoryWithItems(history, historyItems)
        
        // Sincronizar histórico com Supabase se disponível
        if (syncService.isAvailable() && authService.isAuthenticated()) {
            val userId = authService.getCurrentUserId()
            syncService.syncHistoryToSupabase(history, userId).onFailure {
                // Log do erro, mas não falha a operação local
            }
        }
        
        // Limpar a lista atual
        itemCompraDao.deleteAll()
    }

    fun getHistoryLists(): Flow<List<ShoppingListHistory>> {
        return historyDao.getAllHistoryLists()
    }

    fun getHistoryListWithItems(historyId: Long): Flow<ShoppingListHistoryWithItems?> {
        return historyDao.getHistoryListWithItems(historyId)
    }

    suspend fun deleteHistory(historyId: Long) {
        historyDao.deleteHistoryById(historyId)
    }

    suspend fun reuseHistoryList(historyId: Long) {
        val historyWithItems = historyDao.getHistoryListWithItems(historyId).first()
        if (historyWithItems != null) {
            val items = historyWithItems.items.map { historyItem ->
                ItemCompra(
                    nome = historyItem.nome,
                    quantidade = historyItem.quantidade,
                    preco = historyItem.preco,
                    categoria = historyItem.categoria,
                    comprado = false, // Resetar para não comprado
                    dataCriacao = System.currentTimeMillis()
                )
            }
            itemCompraDao.insertAll(items)
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

