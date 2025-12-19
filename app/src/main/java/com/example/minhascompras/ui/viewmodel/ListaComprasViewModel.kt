package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.FilterStatus
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.SortOrder
import com.example.minhascompras.data.UserPreferencesManager
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
    private val userPreferencesManager: UserPreferencesManager
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

    // Lista completa de itens (sem filtro) para estatísticas
    val allItens: StateFlow<List<ItemCompra>> = repository.allItens
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Combine search query (com debounce), filter status e sort order para criar um flow reativo
    val itens: StateFlow<List<ItemCompra>> = combine(
        _searchQuery.debounce(300L),
        _filterStatus,
        sortOrder
    ) { query, filter, sort ->
        Triple(query, filter, sort)
    }.flatMapLatest { (query, filter, sort) ->
        repository.getFilteredItens(query, filter, sort)
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
        android.util.Log.d("ListaComprasViewModel", "=== INSERIR ITEM INICIADO ===")
        android.util.Log.d("ListaComprasViewModel", "Parâmetros: nome=$nome, quantidade=$quantidade, preco=$preco, categoria=$categoria")
        
        if (nome.isNotBlank()) {
            viewModelScope.launch {
                try {
                    android.util.Log.d("ListaComprasViewModel", "Criando objeto ItemCompra")
                    val item = ItemCompra(nome = nome.trim(), quantidade = quantidade, preco = preco, categoria = categoria)
                    android.util.Log.d("ListaComprasViewModel", "Item criado: $item")
                    
                    android.util.Log.d("ListaComprasViewModel", "Iniciando inserção no repositório")
                    val id = repository.insert(item)
                    android.util.Log.d("ListaComprasViewModel", "Item inserido com sucesso, ID: $id")
                    android.util.Log.d("ListaComprasViewModel", "=== INSERIR ITEM CONCLUÍDO COM SUCESSO ===")
                } catch (e: Exception) {
                    android.util.Log.e("ListaComprasViewModel", "ERRO AO INSERIR ITEM: ${e.message}", e)
                    android.util.Log.e("ListaComprasViewModel", "Stack trace: ${e.stackTraceToString()}")
                    android.util.Log.e("ListaComprasViewModel", "=== INSERIR ITEM FALHOU ===")
                    _uiMessages.emit(UiMessage.Error("Erro ao adicionar item: ${e.message}"))
                }
            }
        } else {
            android.util.Log.w("ListaComprasViewModel", "Nome do item está em branco, ignorando inserção")
        }
    }

    fun atualizarItem(item: ItemCompra) {
        android.util.Log.d("ListaComprasViewModel", "=== ATUALIZAR ITEM INICIADO ===")
        android.util.Log.d("ListaComprasViewModel", "Item a ser atualizado: $item")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("ListaComprasViewModel", "Iniciando atualização no repositório")
                repository.update(item)
                android.util.Log.d("ListaComprasViewModel", "Item atualizado com sucesso")
                android.util.Log.d("ListaComprasViewModel", "=== ATUALIZAR ITEM CONCLUÍDO COM SUCESSO ===")
            } catch (e: Exception) {
                android.util.Log.e("ListaComprasViewModel", "ERRO AO ATUALIZAR ITEM: ${e.message}", e)
                android.util.Log.e("ListaComprasViewModel", "Stack trace: ${e.stackTraceToString()}")
                android.util.Log.e("ListaComprasViewModel", "=== ATUALIZAR ITEM FALHOU ===")
                _uiMessages.emit(UiMessage.Error("Erro ao atualizar item: ${e.message}"))
            }
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
                val itensAtuais = allItens.value
                if (itensAtuais.isNotEmpty()) {
                    repository.archiveCurrentList(itensAtuais)
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
            repository.deleteComprados()
        }
    }

    fun deletarTodos() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    suspend fun getAllItensForExport(): List<ItemCompra> {
        return repository.getAllItensList()
    }

    suspend fun importItens(items: List<ItemCompra>) {
        repository.replaceAllItems(items)
    }

    suspend fun getShareableText(): String {
        val itens = repository.getAllItensList()
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
    private val userPreferencesManager: UserPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaComprasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListaComprasViewModel(repository, userPreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

