package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ShoppingList
import com.example.minhascompras.data.ShoppingListPreferencesManager
import com.example.minhascompras.data.ShoppingListRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val repository: ShoppingListRepository,
    private val preferencesManager: ShoppingListPreferencesManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    sealed interface UiMessage {
        val message: String

        data class Success(override val message: String) : UiMessage
        data class Info(override val message: String) : UiMessage
        data class Error(override val message: String) : UiMessage
    }

    private val _uiMessages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 1)
    val uiMessages: SharedFlow<UiMessage> = _uiMessages.asSharedFlow()

    // Todas as listas
    val allLists: StateFlow<List<ShoppingList>> = repository.allLists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ID da lista ativa (do DataStore)
    val activeListId: StateFlow<Long?> = preferencesManager.activeListId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Lista ativa completa (derivada de allLists e activeListId)
    val activeList: StateFlow<ShoppingList?> = combine(
        allLists,
        activeListId
    ) { lists, activeId ->
        if (activeId != null) {
            lists.find { it.id == activeId }
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            try {
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:init",
                    message = "init started",
                    data = emptyMap(),
                    hypothesisId = "D"
                )
                // #endregion
                
                // REMOVIDO: Não criar lista padrão automaticamente
                // O usuário criará sua primeira lista quando necessário
                
                // Verificar se há lista ativa salva e se ela ainda existe
                val currentActiveId = preferencesManager.activeListId.first()
                
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:init",
                    message = "currentActiveId checked from Flow",
                    data = mapOf("currentActiveId" to currentActiveId),
                    hypothesisId = "A"
                )
                // #endregion
                
                if (currentActiveId != null) {
                    // Verificar se a lista ainda existe
                    val list = repository.getListByIdSync(currentActiveId)
                    if (list == null) {
                        // Lista foi deletada, limpar preferência
                        preferencesManager.clearActiveListId()
                        
                        // #region agent log
                        DebugLogger.log(
                            location = "ShoppingListViewModel.kt:init",
                            message = "activeListId cleared - list no longer exists",
                            data = mapOf("listId" to currentActiveId),
                            hypothesisId = "A"
                        )
                        // #endregion
                    }
                }
                // Se não houver lista ativa, deixar null (usuário criará quando necessário)
            } catch (e: Exception) {
                Logger.e("ShoppingListViewModel", "Erro na inicialização", e)
                _uiMessages.emit(UiMessage.Error("Erro ao inicializar listas. Tente novamente."))
                
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:init",
                    message = "init error",
                    data = mapOf("error" to (e.message ?: "unknown")),
                    hypothesisId = "D"
                )
                // #endregion
            }
        }
    }

    fun setActiveList(listId: Long) {
        viewModelScope.launch {
            try {
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:setActiveList",
                    message = "setActiveList called",
                    data = mapOf("listId" to listId),
                    hypothesisId = "A"
                )
                // #endregion
                
                val list = repository.getListByIdSync(listId)
                
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:setActiveList",
                    message = "list retrieved",
                    data = mapOf(
                        "listId" to listId,
                        "listFound" to (list != null),
                        "listName" to (list?.nome ?: null)
                    ),
                    hypothesisId = "A"
                )
                // #endregion
                
                if (list != null) {
                    preferencesManager.setActiveListId(listId)
                    
                    // #region agent log
                    DebugLogger.log(
                        location = "ShoppingListViewModel.kt:setActiveList",
                        message = "activeListId saved",
                        data = mapOf("listId" to listId, "listName" to list.nome),
                        hypothesisId = "A"
                    )
                    // #endregion
                    
                    _uiMessages.emit(UiMessage.Success("Lista '${list.nome}' selecionada"))
                } else {
                    _uiMessages.emit(UiMessage.Error("Lista não encontrada"))
                }
            } catch (e: Exception) {
                Logger.e("ShoppingListViewModel", "Erro ao definir lista ativa", e)
                _uiMessages.emit(UiMessage.Error("Erro ao selecionar lista. Tente novamente."))
                
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:setActiveList",
                    message = "setActiveList error",
                    data = mapOf("error" to (e.message ?: "unknown")),
                    hypothesisId = "A"
                )
                // #endregion
            }
        }
    }

    fun createList(name: String) {
        // #region agent log
        DebugLogger.log(
            location = "ShoppingListViewModel.kt:194",
            message = "createList called",
            data = mapOf(
                "name" to name,
                "nameIsBlank" to name.isBlank(),
                "repositoryNotNull" to (repository != null)
            ),
            hypothesisId = "C"
        )
        // #endregion
        if (name.isBlank()) {
            viewModelScope.launch {
                _uiMessages.emit(UiMessage.Error("O nome da lista não pode estar vazio"))
            }
            return
        }

        viewModelScope.launch {
            try {
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:207",
                    message = "Starting createList coroutine",
                    data = mapOf("name" to name.trim()),
                    hypothesisId = "C"
                )
                // #endregion
                _isLoading.value = true
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:214",
                    message = "Calling repository.createList",
                    data = mapOf("name" to name.trim()),
                    hypothesisId = "C"
                )
                // #endregion
                val newListId = repository.createList(name.trim())
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:220",
                    message = "repository.createList completed",
                    data = mapOf("name" to name.trim(), "newListId" to newListId),
                    hypothesisId = "C"
                )
                // #endregion
                
                // Selecionar automaticamente a lista recém-criada
                preferencesManager.setActiveListId(newListId)
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:230",
                    message = "New list automatically selected",
                    data = mapOf("listId" to newListId),
                    hypothesisId = "C"
                )
                // #endregion
                
                _uiMessages.emit(UiMessage.Success("Lista '${name.trim()}' criada com sucesso"))
            } catch (e: Exception) {
                // #region agent log
                DebugLogger.log(
                    location = "ShoppingListViewModel.kt:227",
                    message = "Exception in createList",
                    data = mapOf(
                        "error" to (e.message ?: "unknown"),
                        "exceptionType" to e.javaClass.simpleName,
                        "stackTrace" to e.stackTraceToString()
                    ),
                    hypothesisId = "C"
                )
                // #endregion
                Logger.e("ShoppingListViewModel", "Erro ao criar lista", e)
                _uiMessages.emit(UiMessage.Error("Erro ao criar lista. Tente novamente."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renameList(id: Long, newName: String) {
        if (newName.isBlank()) {
            viewModelScope.launch {
                _uiMessages.emit(UiMessage.Error("O nome da lista não pode estar vazio"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val list = repository.getListByIdSync(id)
                if (list == null) {
                    _uiMessages.emit(UiMessage.Error("Lista não encontrada"))
                    return@launch
                }

                if (list.nome == newName.trim()) {
                    _uiMessages.emit(UiMessage.Info("O nome não foi alterado"))
                    return@launch
                }

                _isLoading.value = true
                val updatedList = list.copy(nome = newName.trim())
                repository.updateList(updatedList)
                _uiMessages.emit(UiMessage.Success("Lista renomeada para '${newName.trim()}'"))
            } catch (e: Exception) {
                Logger.e("ShoppingListViewModel", "Erro ao renomear lista", e)
                _uiMessages.emit(UiMessage.Error("Erro ao renomear lista. Tente novamente."))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteList(id: Long) {
        viewModelScope.launch {
            try {
                val list = repository.getListByIdSync(id)
                if (list == null) {
                    _uiMessages.emit(UiMessage.Error("Lista não encontrada"))
                    return@launch
                }

                // Obter contagem de itens antes de deletar
                val itemCount = repository.getItemCountForList(id).first()

                _isLoading.value = true
                val result = repository.deleteList(id)
                
                result.fold(
                    onSuccess = {
                        val message = if (itemCount > 0) {
                            "Lista '${list.nome}' e seus $itemCount item(ns) foram deletados"
                        } else {
                            "Lista '${list.nome}' foi deletada"
                        }
                        _uiMessages.emit(UiMessage.Success(message))
                    },
                    onFailure = { exception ->
                        _uiMessages.emit(UiMessage.Error(exception.message ?: "Erro ao deletar lista"))
                    }
                )
            } catch (e: Exception) {
                Logger.e("ShoppingListViewModel", "Erro ao deletar lista", e)
                _uiMessages.emit(UiMessage.Error("Erro ao deletar lista. Tente novamente."))
            } finally {
                _isLoading.value = false
            }
        }
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

