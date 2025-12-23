package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.FilterStatus
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.ShoppingListPreferencesManager
import com.example.minhascompras.data.SortOrder
import com.example.minhascompras.data.UserPreferencesManager
import com.example.minhascompras.utils.DebugLogger
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListaComprasViewModel(
    private val repository: ItemCompraRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: com.example.minhascompras.data.ShoppingListRepository? = null
) : ViewModel() {
    // StateFlows para busca e filtro
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)
    val filterStatus: StateFlow<FilterStatus> = _filterStatus.asStateFlow()

    // Estado para undo de exclusão
    private val _lastDeletedItem = MutableStateFlow<ItemCompra?>(null)
    val lastDeletedItem: StateFlow<ItemCompra?> = _lastDeletedItem.asStateFlow()

    private val _isArchiving = MutableStateFlow(false)
    val isArchiving: StateFlow<Boolean> = _isArchiving.asStateFlow()

    sealed interface UiMessage {
        val message: String

        data class Success(override val message: String) : UiMessage
        data class Info(override val message: String) : UiMessage
        data class Error(override val message: String) : UiMessage
    }

    private val _uiMessages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 1)
    val uiMessages: SharedFlow<UiMessage> = _uiMessages.asSharedFlow()

    // SortOrder do DataStore
    val sortOrder: StateFlow<SortOrder> = userPreferencesManager.sortOrder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortOrder.BY_DATE_DESC
        )

    // ID da lista ativa (do DataStore)
    val activeListId: StateFlow<Long?> = shoppingListPreferencesManager.activeListId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Lista completa de itens da lista ativa (sem filtro) para estatísticas
    val allItens: StateFlow<List<ItemCompra>> = activeListId
        .flatMapLatest { activeId ->
            repository.getAllItensByList(activeId ?: 1L) // Fallback para lista padrão se null
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combine search query (com debounce), filter status, sort order e activeListId para criar um flow reativo
    val itens: StateFlow<List<ItemCompra>> = combine(
        _searchQuery.debounce(300L),
        _filterStatus,
        sortOrder,
        activeListId
    ) { values ->
        val query = values[0] as String
        val filter = values[1] as FilterStatus
        val sort = values[2] as SortOrder
        val activeId = values[3] as? Long
        val listId = activeId // Não usar fallback - se null, retornar lista vazia
        
        // #region agent log
        DebugLogger.log(
            location = "ListaComprasViewModel.kt:itens",
            message = "combine triggered",
            data = mapOf(
                "activeId" to activeId,
                "listId" to listId,
                "query" to query,
                "filter" to filter.toString()
            ),
            hypothesisId = "B"
        )
        // #endregion
        
        Triple(query, filter, sort) to listId
    }.flatMapLatest { (params, listId) ->
        // #region agent log
        DebugLogger.log(
            location = "ListaComprasViewModel.kt:itens",
            message = "flatMapLatest with listId",
            data = mapOf("listId" to listId),
            hypothesisId = "B"
        )
        // #endregion
        
        // Se não houver lista ativa, retornar lista vazia
        if (listId == null) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            repository.getFilteredItensByList(listId, params.first, params.second, params.third)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterStatusChanged(filter: FilterStatus) {
        _filterStatus.value = filter
    }

    fun setSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            userPreferencesManager.setSortOrder(sortOrder)
        }
    }

    fun inserirItem(nome: String, quantidade: Int = 1, preco: Double? = null, categoria: String = "Outros") {
        if (nome.isNotBlank()) {
            viewModelScope.launch {
                // VALIDAÇÃO: Verificar se há lista ativa
                val currentListId = activeListId.value
                if (currentListId == null) {
                    // Não há lista ativa - mostrar mensagem de erro
                    _uiMessages.emit(
                        UiMessage.Error("Você precisa criar ou selecionar uma lista antes de adicionar itens.")
                    )
                    return@launch
                }
                
                // Verificar se a lista ainda existe
                val list = shoppingListRepository?.getListByIdSync(currentListId)
                if (list == null) {
                    // Lista foi deletada - limpar preferência e mostrar erro
                    shoppingListPreferencesManager.clearActiveListId()
                    _uiMessages.emit(
                        UiMessage.Error("A lista selecionada não existe mais. Por favor, selecione outra lista.")
                    )
                    return@launch
                }
                
                // Lista válida - inserir item
                repository.insert(ItemCompra(
                    nome = nome.trim(),
                    quantidade = quantidade,
                    preco = preco,
                    categoria = categoria,
                    listId = currentListId
                ))
            }
        }
    }

    fun atualizarItem(item: ItemCompra) {
        viewModelScope.launch {
            repository.update(item)
        }
    }

    fun deletarItem(item: ItemCompra) {
        viewModelScope.launch {
            _lastDeletedItem.value = item
            repository.delete(item)
        }
    }

    fun undoDeleteItem() {
        val item = _lastDeletedItem.value
        if (item != null) {
            viewModelScope.launch {
                repository.insert(item)
                _lastDeletedItem.value = null
            }
        }
    }

    fun toggleComprado(item: ItemCompra) {
        viewModelScope.launch {
            repository.update(item.copy(comprado = !item.comprado))
        }
    }

    fun arquivarLista() {
        viewModelScope.launch {
            if (_isArchiving.value) return@launch
            _isArchiving.value = true
            try {
                val currentListId = activeListId.value
                if (currentListId == null) {
                    _uiMessages.emit(UiMessage.Error("Você precisa ter uma lista ativa para arquivar."))
                    _isArchiving.value = false
                    return@launch
                }
                
                // #region agent log
                DebugLogger.log(
                    location = "ListaComprasViewModel.kt:arquivarLista",
                    message = "arquivarLista called",
                    data = mapOf("currentListId" to currentListId),
                    hypothesisId = "F"
                )
                // #endregion
                
                val itensAtuais = allItens.value
                
                // #region agent log
                DebugLogger.log(
                    location = "ListaComprasViewModel.kt:arquivarLista",
                    message = "itensAtuais retrieved",
                    data = mapOf("itemCount" to itensAtuais.size),
                    hypothesisId = "F"
                )
                // #endregion
                
                if (itensAtuais.isNotEmpty()) {
                    // Obter nome real da lista atual
                    val listName = try {
                        shoppingListRepository?.getListByIdSync(currentListId)?.nome 
                            ?: "Minhas Compras"
                    } catch (e: Exception) {
                        // #region agent log
                        DebugLogger.log(
                            location = "ListaComprasViewModel.kt:arquivarLista",
                            message = "error getting list name",
                            data = mapOf("error" to (e.message ?: "unknown")),
                            hypothesisId = "F"
                        )
                        // #endregion
                        "Minhas Compras"
                    }
                    
                    // #region agent log
                    DebugLogger.log(
                        location = "ListaComprasViewModel.kt:arquivarLista",
                        message = "calling archiveCurrentList",
                        data = mapOf(
                            "listId" to currentListId,
                            "listName" to listName,
                            "itemCount" to itensAtuais.size
                        ),
                        hypothesisId = "F"
                    )
                    // #endregion
                    
                    repository.archiveCurrentList(currentListId, listName)
                    
                    // #region agent log
                    DebugLogger.log(
                        location = "ListaComprasViewModel.kt:arquivarLista",
                        message = "archiveCurrentList completed",
                        data = mapOf("listId" to currentListId),
                        hypothesisId = "F"
                    )
                    // #endregion
                    
                    _uiMessages.emit(UiMessage.Success("Lista arquivada com sucesso!"))
                } else {
                    // #region agent log
                    DebugLogger.log(
                        location = "ListaComprasViewModel.kt:arquivarLista",
                        message = "no items to archive",
                        data = emptyMap(),
                        hypothesisId = "F"
                    )
                    // #endregion
                    
                    _uiMessages.emit(UiMessage.Info("Não há itens para arquivar."))
                }
            } catch (e: Exception) {
                Logger.e("ListaComprasViewModel", "Erro ao arquivar lista manualmente", e)
                
                // #region agent log
                DebugLogger.log(
                    location = "ListaComprasViewModel.kt:arquivarLista",
                    message = "archive error",
                    data = mapOf(
                        "error" to (e.message ?: "unknown"),
                        "errorType" to (e.javaClass.simpleName)
                    ),
                    hypothesisId = "F"
                )
                // #endregion
                
                _uiMessages.emit(UiMessage.Error("Erro ao arquivar lista. Tente novamente."))
            } finally {
                _isArchiving.value = false
            }
        }
    }

    fun deletarComprados() {
        viewModelScope.launch {
            val currentListId = activeListId.value
            if (currentListId == null) {
                _uiMessages.emit(UiMessage.Error("Você precisa ter uma lista ativa para deletar itens."))
                return@launch
            }
            repository.deleteCompradosByList(currentListId)
        }
    }

    fun deletarTodos() {
        viewModelScope.launch {
            val currentListId = activeListId.value
            if (currentListId == null) {
                _uiMessages.emit(UiMessage.Error("Você precisa ter uma lista ativa para deletar itens."))
                return@launch
            }
            repository.deleteAllByList(currentListId)
        }
    }

    suspend fun getAllItensForExport(): List<ItemCompra> {
        val currentListId = activeListId.value
        return if (currentListId == null) {
            emptyList()
        } else {
            repository.getAllItensListByList(currentListId)
        }
    }

    suspend fun importItens(items: List<ItemCompra>) {
        val currentListId = activeListId.value
        if (currentListId == null) {
            throw IllegalStateException("Você precisa ter uma lista ativa para importar itens.")
        }
        val itemsWithListId = items.map { it.copy(listId = currentListId) }
        repository.replaceAllItems(itemsWithListId)
    }

    suspend fun getShareableText(): String {
        val currentListId = activeListId.value
        if (currentListId == null) {
            return "Nenhuma lista selecionada"
        }
        val itens = repository.getAllItensListByList(currentListId)
        if (itens.isEmpty()) {
            return "Lista de Compras vazia"
        }

        val formatador = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        val builder = StringBuilder()
        builder.append("🛒 Minhas Compras\n")
        builder.append("=".repeat(30)).append("\n\n")

        val pendentes = itens.filter { !it.comprado }
        val comprados = itens.filter { it.comprado }

        if (pendentes.isNotEmpty()) {
            builder.append("📋 PENDENTES:\n")
            pendentes.forEach { item ->
                builder.append("  □ ${item.nome}")
                if (item.quantidade > 1) {
                    builder.append(" (${item.quantidade}x)")
                }
                if (item.preco != null && item.preco > 0) {
                    builder.append(" - ${formatador.format(item.preco * item.quantidade)}")
                }
                if (item.categoria.isNotEmpty() && item.categoria != "Outros") {
                    builder.append(" [${item.categoria}]")
                }
                builder.append("\n")
            }
            builder.append("\n")
        }

        if (comprados.isNotEmpty()) {
            builder.append("✅ COMPRADOS:\n")
            comprados.forEach { item ->
                builder.append("  ☑ ${item.nome}")
                if (item.quantidade > 1) {
                    builder.append(" (${item.quantidade}x)")
                }
                if (item.preco != null && item.preco > 0) {
                    builder.append(" - ${formatador.format(item.preco * item.quantidade)}")
                }
                if (item.categoria.isNotEmpty() && item.categoria != "Outros") {
                    builder.append(" [${item.categoria}]")
                }
                builder.append("\n")
            }
            builder.append("\n")
        }

        val totalGeral = itens.sumOf { (it.preco ?: 0.0) * it.quantidade }
        val totalPendentes = pendentes.sumOf { (it.preco ?: 0.0) * it.quantidade }
        
        builder.append("=".repeat(30)).append("\n")
        builder.append("Total de itens: ${itens.size}\n")
        builder.append("Pendentes: ${pendentes.size}\n")
        builder.append("Comprados: ${comprados.size}\n")
        
        if (totalGeral > 0) {
            builder.append("\n💰 Total Geral: ${formatador.format(totalGeral)}\n")
            if (totalPendentes > 0) {
                builder.append("💰 Total Pendente: ${formatador.format(totalPendentes)}\n")
            }
        }

        return builder.toString()
    }
}

class ListaComprasViewModelFactory(
    private val repository: ItemCompraRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: com.example.minhascompras.data.ShoppingListRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaComprasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListaComprasViewModel(repository, userPreferencesManager, shoppingListPreferencesManager, shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

