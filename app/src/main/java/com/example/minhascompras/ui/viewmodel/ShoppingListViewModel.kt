package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ShoppingList
import com.example.minhascompras.data.ShoppingListPreferencesManager
import com.example.minhascompras.data.ShoppingListRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val repository: ShoppingListRepository,
    private val preferencesManager: ShoppingListPreferencesManager
) : ViewModel() {

    // Lista de todas as listas de compras
    val allLists: StateFlow<List<ShoppingList>> = repository.allLists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Quantidade de listas não-padrão (criadas pelo usuário)
    val nonDefaultListCount: StateFlow<Int> = allLists
        .map { lists -> lists.count { !it.isDefault && !it.isArchived } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // ID da lista ativa atual (nullable - pode ser null se não houver listas)
    val activeListId: StateFlow<Long?> = preferencesManager.activeListId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Lista ativa atual
    private val _activeList = MutableStateFlow<ShoppingList?>(null)
    val activeList: StateFlow<ShoppingList?> = _activeList.asStateFlow()

    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    sealed interface UiMessage {
        val message: String
        data class Success(override val message: String) : UiMessage
        data class Error(override val message: String) : UiMessage
        data class Info(override val message: String) : UiMessage
    }

    private val _uiMessages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 1)
    val uiMessages: SharedFlow<UiMessage> = _uiMessages.asSharedFlow()

    init {
        // Carregar a lista ativa
        viewModelScope.launch {
            loadActiveList()
        }

        // Observar mudanças no ID da lista ativa
        viewModelScope.launch {
            activeListId.collect { listId ->
                if (listId != null) {
                    loadListById(listId)
                } else {
                    // Se não houver lista ativa, tentar usar a primeira lista disponível
                    val firstList = allLists.value.firstOrNull { !it.isArchived }
                    if (firstList != null) {
                        setActiveList(firstList.id)
                    } else {
                        _activeList.value = null
                    }
                }
            }
        }
    }

    private suspend fun loadActiveList() {
        val listId = activeListId.value
        if (listId != null) {
            loadListById(listId)
        } else {
            // Se não houver lista ativa, tentar usar a primeira lista disponível
            val firstList = allLists.value.firstOrNull { !it.isArchived }
            if (firstList != null) {
                setActiveList(firstList.id)
            } else {
                _activeList.value = null
            }
        }
    }

    private suspend fun loadListById(listId: Long) {
        val list = repository.getListByIdSync(listId)
        _activeList.value = list
        if (list == null) {
            // Se a lista ativa não existe mais, tentar usar a primeira lista disponível
            val firstList = allLists.value.firstOrNull { !it.isArchived }
            if (firstList != null) {
                setActiveList(firstList.id)
            } else {
                _activeList.value = null
                preferencesManager.setActiveListId(null)
            }
        }
    }

    fun setActiveList(listId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getListByIdSync(listId)
                if (list != null) {
                    preferencesManager.setActiveListId(listId)
                    _activeList.value = list
                    _uiMessages.emit(UiMessage.Success("Lista '${list.nome}' selecionada"))
                } else {
                    _uiMessages.emit(UiMessage.Error("Lista não encontrada"))
                }
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("Erro ao selecionar lista"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createList(nome: String) {
        if (nome.isBlank()) {
            viewModelScope.launch {
                _uiMessages.emit(UiMessage.Error("Nome da lista não pode ser vazio"))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newList = ShoppingList(
                    nome = nome.trim(),
                    dataCriacao = System.currentTimeMillis(),
                    isDefault = false
                )
                val newListId = repository.insert(newList)
                _uiMessages.emit(UiMessage.Success("Lista '${nome.trim()}' criada"))
                // Opcionalmente, selecionar a nova lista
                setActiveList(newListId)
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("Erro ao criar lista"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renameList(listId: Long, newName: String) {
        if (newName.isBlank()) {
            viewModelScope.launch {
                _uiMessages.emit(UiMessage.Error("Nome da lista não pode ser vazio"))
            }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getListByIdSync(listId)
                if (list != null) {
                    val updatedList = list.copy(nome = newName.trim())
                    repository.update(updatedList)
                    _uiMessages.emit(UiMessage.Success("Lista renomeada para '${newName.trim()}'"))
                    // Atualizar lista ativa se for a mesma
                    if (listId == activeListId.value) {
                        _activeList.value = updatedList
                    }
                } else {
                    _uiMessages.emit(UiMessage.Error("Lista não encontrada"))
                }
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("Erro ao renomear lista"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getListByIdSync(listId)
                // Não há mais lista padrão, então qualquer lista pode ser deletada

                // Verificar quantidade de itens
                val itemCount = repository.getItemCountForList(listId)
                
                repository.deleteById(listId)
                _uiMessages.emit(
                    UiMessage.Success(
                        if (itemCount > 0) {
                            "Lista deletada com $itemCount ${if (itemCount == 1) "item" else "itens"}"
                        } else {
                            "Lista deletada"
                        }
                    )
                )

                // Se estava na lista deletada, tentar usar a primeira lista disponível
                if (listId == activeListId.value) {
                    val firstList = allLists.value.firstOrNull { !it.isArchived && it.id != listId }
                    if (firstList != null) {
                        setActiveList(firstList.id)
                    } else {
                        // Não há mais listas, limpar lista ativa
                        preferencesManager.setActiveListId(null)
                        _activeList.value = null
                    }
                }
            } catch (e: IllegalStateException) {
                _uiMessages.emit(UiMessage.Error(e.message ?: "Erro ao deletar lista"))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("Erro ao deletar lista"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getListCount(): Int {
        return repository.getListCount()
    }

    suspend fun getItemCountForList(listId: Long): Int {
        return repository.getItemCountForList(listId)
    }
}

class ShoppingListViewModelFactory(
    private val repository: ShoppingListRepository,
    private val preferencesManager: ShoppingListPreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(repository, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


