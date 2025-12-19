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
        android.util.Log.d("ItemCompraRepository", "=== INSERT INICIADO ===")
        android.util.Log.d("ItemCompraRepository", "Item a ser inserido: $item")
        
        return try {
            android.util.Log.d("ItemCompraRepository", "Iniciando inserção no DAO")
            val id = itemCompraDao.insert(item)
            android.util.Log.d("ItemCompraRepository", "Item inserido no DAO com ID: $id")
            
            // Sincronizar com Supabase se disponível e usuário autenticado
            android.util.Log.d("ItemCompraRepository", "Verificando sincronização com Supabase")
            android.util.Log.d("ItemCompraRepository", "SyncService disponível: ${syncService.isAvailable()}")
            android.util.Log.d("ItemCompraRepository", "Usuário autenticado: ${authService.isAuthenticated()}")
            
            if (syncService.isAvailable() && authService.isAuthenticated()) {
                android.util.Log.d("ItemCompraRepository", "Iniciando sincronização com Supabase")
                val userId = authService.getCurrentUserId()
                android.util.Log.d("ItemCompraRepository", "UserID: $userId")
                val insertedItem = item.copy(id = id)
                syncService.syncItemToSupabase(insertedItem, userId).onFailure { exception ->
                    android.util.Log.w("ItemCompraRepository", "Erro na sincronização com Supabase (não crítico)", exception)
                    // Log do erro, mas não falha a operação local
                }
                android.util.Log.d("ItemCompraRepository", "Sincronização com Supabase concluída")
            } else {
                android.util.Log.d("ItemCompraRepository", "Pulando sincronização com Supabase (não disponível ou usuário não autenticado)")
            }
            
            android.util.Log.d("ItemCompraRepository", "=== INSERT CONCLUÍDO COM SUCESSO ===")
            return id
        } catch (e: Exception) {
            android.util.Log.e("ItemCompraRepository", "ERRO CRÍTICO AO INSERIR ITEM: ${e.message}", e)
            android.util.Log.e("ItemCompraRepository", "Stack trace: ${e.stackTraceToString()}")
            android.util.Log.e("ItemCompraRepository", "=== INSERT FALHOU ===")
            throw e // Propagar exceção para tratamento superior
        }
    }

    suspend fun update(item: ItemCompra) {
        android.util.Log.d("ItemCompraRepository", "=== UPDATE INICIADO ===")
        android.util.Log.d("ItemCompraRepository", "Item a ser atualizado: $item")
        
        try {
            android.util.Log.d("ItemCompraRepository", "Iniciando atualização no DAO")
            itemCompraDao.update(item)
            android.util.Log.d("ItemCompraRepository", "Item atualizado no DAO com sucesso")
            
            // Sincronizar com Supabase se disponível e usuário autenticado
            android.util.Log.d("ItemCompraRepository", "Verificando sincronização com Supabase")
            if (syncService.isAvailable() && authService.isAuthenticated()) {
                android.util.Log.d("ItemCompraRepository", "Iniciando sincronização com Supabase")
                val userId = authService.getCurrentUserId()
                syncService.syncItemToSupabase(item, userId).onFailure { exception ->
                    android.util.Log.w("ItemCompraRepository", "Erro na sincronização com Supabase (não crítico)", exception)
                    // Log do erro, mas não falha a operação local
                }
                android.util.Log.d("ItemCompraRepository", "Sincronização com Supabase concluída")
            } else {
                android.util.Log.d("ItemCompraRepository", "Pulando sincronização com Supabase (não disponível ou usuário não autenticado)")
            }
            
            android.util.Log.d("ItemCompraRepository", "=== UPDATE CONCLUÍDO COM SUCESSO ===")
        } catch (e: Exception) {
            android.util.Log.e("ItemCompraRepository", "ERRO CRÍTICO AO ATUALIZAR ITEM: ${e.message}", e)
            android.util.Log.e("ItemCompraRepository", "Stack trace: ${e.stackTraceToString()}")
            android.util.Log.e("ItemCompraRepository", "=== UPDATE FALHOU ===")
            throw e // Propagar exceção para tratamento superior
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

