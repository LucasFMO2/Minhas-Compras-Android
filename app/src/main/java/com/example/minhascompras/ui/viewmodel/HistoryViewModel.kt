package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.ShoppingList
import com.example.minhascompras.data.ShoppingListHistory
import com.example.minhascompras.data.ShoppingListHistoryWithItems
import com.example.minhascompras.data.ShoppingListPreferencesManager
import com.example.minhascompras.data.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: ItemCompraRepository,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: ShoppingListRepository? = null
) : ViewModel() {
    // Filtro por lista: null = todas as listas, Long = lista específica
    private val _filterListId = MutableStateFlow<Long?>(null)
    val filterListId: StateFlow<Long?> = _filterListId.asStateFlow()

    // Nome da lista filtrada (para exibir na UI)
    val filteredListName: StateFlow<String?> = combine(_filterListId) { filterIds ->
        filterIds.first()
    }.flatMapLatest { listId ->
        if (listId == null || shoppingListRepository == null) {
            MutableStateFlow<String?>(null).asStateFlow()
        } else {
            shoppingListRepository.getListById(listId)
                .map { it?.nome }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Todas as listas disponíveis (para filtro)
    val allLists: StateFlow<List<ShoppingList>> = 
        if (shoppingListRepository != null) {
            shoppingListRepository.allLists
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        } else {
            MutableStateFlow(emptyList<ShoppingList>()).asStateFlow()
        }

    // Inicializar com lista ativa por padrão
    init {
        viewModelScope.launch {
            val activeListId = shoppingListPreferencesManager.activeListId.first()
            _filterListId.value = activeListId
        }
    }

    // Histórico reativo ao filtro
    val historyLists: StateFlow<List<ShoppingListHistory>> = combine(_filterListId) { filterIds ->
        filterIds.first()
    }.flatMapLatest { listId ->
        repository.getHistoryLists(listId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Filtra histórico por lista específica ou mostra todas as listas
     * @param listId ID da lista para filtrar, ou null para mostrar todas
     */
    fun filterByList(listId: Long?) {
        _filterListId.value = listId
    }

    /**
     * Filtra histórico pela lista ativa
     */
    fun filterByActiveList() {
        viewModelScope.launch {
            val activeListId = shoppingListPreferencesManager.activeListId.first()
            _filterListId.value = activeListId
        }
    }

    /**
     * Obtém o nome da lista pelo ID (retorna Flow para uso reativo)
     */
    fun getListName(listId: Long?): StateFlow<String?> {
        return if (listId == null || shoppingListRepository == null) {
            MutableStateFlow<String?>(null).asStateFlow()
        } else {
            shoppingListRepository.getListById(listId)
                .map { it?.nome }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = null
                )
        }
    }

    fun getHistoryListWithItems(historyId: Long): StateFlow<ShoppingListHistoryWithItems?> {
        return repository.getHistoryListWithItems(historyId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun deleteHistory(historyId: Long) {
        viewModelScope.launch {
            repository.deleteHistory(historyId)
        }
    }

    fun reuseHistoryList(historyId: Long) {
        viewModelScope.launch {
            val activeListId = shoppingListPreferencesManager.activeListId.first()
            if (activeListId != null) {
                repository.reuseHistoryList(historyId, activeListId)
            } else {
                // Opcional: mostrar mensagem para usuário criar uma lista primeiro
                // Por enquanto, apenas não faz nada se não houver lista ativa
            }
        }
    }
}

class HistoryViewModelFactory(
    private val repository: ItemCompraRepository,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: ShoppingListRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository, shoppingListPreferencesManager, shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

