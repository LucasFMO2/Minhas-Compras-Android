package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.minhascompras.data.FilterStatus
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.PurchaseCompleteNotifier
import com.example.minhascompras.data.ShoppingList
import com.example.minhascompras.data.ShoppingListPreferencesManager
import com.example.minhascompras.data.ShoppingListRepository
import com.example.minhascompras.data.SortOrder
import com.example.minhascompras.data.UserPreferencesManager
import com.example.minhascompras.utils.Logger
import com.example.minhascompras.widget.ShoppingListWidgetProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListaComprasViewModel(
    private val repository: ItemCompraRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListViewModel: ShoppingListViewModel? = null,
    private val context: Context? = null
) : ViewModel() {
    
    private val purchaseCompleteNotifier = context?.let { PurchaseCompleteNotifier(it) }
    
    // Observar quantidade de listas não-padrão
    val hasUserCreatedLists: StateFlow<Boolean> = shoppingListViewModel?.nonDefaultListCount
        ?.map { it > 0 }
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        ) ?: MutableStateFlow(false).asStateFlow()
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

    // ID da lista ativa (nullable - pode ser null se não houver listas)
    val activeListId: StateFlow<Long?> = shoppingListPreferencesManager.activeListId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // SortOrder do DataStore
    val sortOrder: StateFlow<SortOrder> = userPreferencesManager.sortOrder
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortOrder.BY_DATE_DESC
        )

    // Lista completa de itens (sem filtro) para estatísticas da lista ativa
    val allItens: StateFlow<List<ItemCompra>> = activeListId.flatMapLatest { listId ->
        if (listId != null) {
            repository.getItensByList(listId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combine activeListId, search query (com debounce), filter status e sort order para criar um flow reativo
    val itens: StateFlow<List<ItemCompra>> = combine(
        activeListId,
        _searchQuery.debounce(300L),
        _filterStatus,
        sortOrder
    ) { listId, query, filter, sort ->
        listId to Triple(query, filter, sort)
    }.flatMapLatest { (listId, triple) ->
        val (query, filter, sort) = triple
        if (listId != null) {
            repository.getFilteredItensByList(listId, query, filter, sort)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
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
                // Verificar se há listas criadas pelo usuário
                val allLists = shoppingListRepository.allLists.first()
                val availableLists = allLists.filter { !it.isArchived }
                if (availableLists.isEmpty()) {
                    _uiMessages.emit(UiMessage.Error("Crie uma lista de compras antes de adicionar produtos"))
                    return@launch
                }
                
                val listId = activeListId.value
                if (listId == null) {
                    // Se não houver lista ativa, usar a primeira lista disponível
                    val firstList = availableLists.first()
                    shoppingListViewModel?.setActiveList(firstList.id)
                    repository.insert(
                        ItemCompra(
                            nome = nome.trim(),
                            quantidade = quantidade,
                            preco = preco,
                            categoria = categoria,
                            listId = firstList.id
                        )
                    )
                    return@launch
                }
                
                // Verificar se a lista ativa ainda existe
                val activeList = shoppingListRepository.getListByIdSync(listId)
                if (activeList == null || activeList.isArchived) {
                    // Se a lista não existe mais ou foi arquivada, usar a primeira lista disponível
                    val firstList = availableLists.firstOrNull { it.id != listId } ?: availableLists.first()
                    shoppingListViewModel?.setActiveList(firstList.id)
                    repository.insert(
                        ItemCompra(
                            nome = nome.trim(),
                            quantidade = quantidade,
                            preco = preco,
                            categoria = categoria,
                            listId = firstList.id
                        )
                    )
                    return@launch
                }
                
                repository.insert(
                    ItemCompra(
                        nome = nome.trim(),
                        quantidade = quantidade,
                        preco = preco,
                        categoria = categoria,
                        listId = listId
                    )
                )
                
                // Atualizar widgets após inserir item
                context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
            }
        }
    }

    fun atualizarItem(item: ItemCompra) {
        viewModelScope.launch {
            repository.update(item)
            // Atualizar widgets após atualizar item
            context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
        }
    }

    fun deletarItem(item: ItemCompra) {
        viewModelScope.launch {
            _lastDeletedItem.value = item
            repository.delete(item)
            // Atualizar widgets após deletar item
            context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
        }
    }

    fun undoDeleteItem() {
        val item = _lastDeletedItem.value
        if (item != null) {
            viewModelScope.launch {
                repository.insert(item)
                _lastDeletedItem.value = null
                // Atualizar widgets após restaurar item
                context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
            }
        }
    }

    fun toggleComprado(item: ItemCompra) {
        viewModelScope.launch {
            repository.update(item.copy(comprado = !item.comprado))
            
            // Verificar se a lista foi completada após marcar item como comprado
            if (item.comprado.not()) { // Se estava não comprado e agora está comprado
                val activeListId = activeListId.value ?: item.listId
                val activeList = shoppingListRepository.getListByIdSync(activeListId)
                purchaseCompleteNotifier?.checkAndNotifyIfCompleteAsync(
                    activeListId,
                    activeList?.nome ?: "Lista de Compras"
                )
            }
            
            // Atualizar widgets após marcar item como comprado
            context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
        }
    }

    fun arquivarLista() {
        viewModelScope.launch {
            if (_isArchiving.value) return@launch
            _isArchiving.value = true
            try {
                val itensAtuais = allItens.value
                if (itensAtuais.isNotEmpty()) {
                    val listId = activeListId.value
                    if (listId == null) {
                        _uiMessages.emit(UiMessage.Error("Nenhuma lista ativa selecionada"))
                        return@launch
                    }
                    val list = shoppingListRepository.getListByIdSync(listId)
                    
                    // Não há mais lista padrão, então qualquer lista pode ser arquivada
                    
                    val listName = list?.nome ?: "Lista de Compras"
                    
                    // Arquivar os itens
                    repository.archiveCurrentList(itensAtuais, listId, listName)
                    
                    // Marcar a lista como arquivada
                    if (list != null) {
                        shoppingListRepository.update(list.copy(isArchived = true))
                        
                        // Se a lista arquivada era a ativa, tentar usar a primeira lista disponível
                        if (listId == activeListId.value) {
                            val allLists = shoppingListRepository.allLists.first()
                            val firstList = allLists.firstOrNull { !it.isArchived && it.id != listId }
                            if (firstList != null) {
                                shoppingListViewModel?.setActiveList(firstList.id)
                            } else {
                                shoppingListViewModel?.let { vm ->
                                    viewModelScope.launch {
                                        shoppingListPreferencesManager.setActiveListId(null)
                                    }
                                }
                            }
                        }
                    }
                    
                    _uiMessages.emit(UiMessage.Success("Lista arquivada com sucesso!"))
                } else {
                    _uiMessages.emit(UiMessage.Info("Não há itens para arquivar."))
                }
            } catch (e: Exception) {
                Logger.e("ListaComprasViewModel", "Erro ao arquivar lista manualmente", e)
                _uiMessages.emit(UiMessage.Error("Erro ao arquivar lista. Tente novamente."))
            } finally {
                _isArchiving.value = false
            }
        }
    }

    fun deletarComprados() {
        viewModelScope.launch {
            val listId = activeListId.value
            if (listId != null) {
                repository.deleteCompradosByList(listId)
                // Atualizar widgets após deletar itens comprados
                context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
            }
        }
    }

    fun deletarTodos() {
        viewModelScope.launch {
            val listId = activeListId.value
            if (listId != null) {
                repository.deleteAllByList(listId)
                // Atualizar widgets após deletar todos os itens
                context?.let { ShoppingListWidgetProvider.updateAllWidgets(it) }
            }
        }
    }

    suspend fun getAllItensForExport(): List<ItemCompra> {
        val listId = activeListId.value
        return if (listId != null) {
            repository.getAllItensListByList(listId)
        } else {
            emptyList()
        }
    }

    suspend fun importItens(items: List<ItemCompra>) {
        val listId = activeListId.value
        if (listId != null) {
            // Atualizar todos os itens para a lista ativa antes de importar
            val itemsWithListId = items.map { it.copy(listId = listId) }
            repository.replaceAllItems(itemsWithListId)
        }
    }

    suspend fun getShareableText(): String {
        val listId = activeListId.value
        val itens = if (listId != null) {
            repository.getAllItensListByList(listId)
        } else {
            emptyList()
        }
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
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListViewModel: ShoppingListViewModel? = null,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaComprasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListaComprasViewModel(repository, userPreferencesManager, shoppingListPreferencesManager, shoppingListRepository, shoppingListViewModel, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

