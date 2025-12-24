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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HistoryViewModel(
    private val repository: ItemCompraRepository,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: ShoppingListRepository? = null
) : ViewModel() {
    init {
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "HistoryViewModel.kt:init",
            message = "HistoryViewModel initialized",
            data = mapOf(
                "shoppingListRepositoryNotNull" to (shoppingListRepository != null)
            ),
            hypothesisId = "A"
        )
        // #endregion
    }
    // Histórico: APENAS listas arquivadas, sem filtros
    val historyLists: StateFlow<List<ShoppingListHistory>> = 
        if (shoppingListRepository != null) {
            shoppingListRepository.getArchivedLists()
                .map { archivedLists ->
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:historyLists.map",
                        message = "getArchivedLists returned",
                        data = mapOf(
                            "archivedListsCount" to archivedLists.size,
                            "archivedListIds" to archivedLists.map { it.id },
                            "archivedListNames" to archivedLists.map { it.nome }
                        ),
                        hypothesisId = "A"
                    )
                    // #endregion
                    
                    // Converter todas as listas arquivadas em ShoppingListHistory
                    val historyList = archivedLists.map { archivedList ->
                        ShoppingListHistory(
                            id = -archivedList.id, // ID negativo para diferenciar
                            listId = archivedList.id,
                            listName = archivedList.nome,
                            completionDate = archivedList.dataCriacao
                        )
                    }.sortedByDescending { it.completionDate }
                    
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:historyLists.map",
                        message = "converted to ShoppingListHistory",
                        data = mapOf(
                            "historyListCount" to historyList.size,
                            "historyListIds" to historyList.map { it.id }
                        ),
                        hypothesisId = "A"
                    )
                    // #endregion
                    
                    historyList
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        } else {
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "HistoryViewModel.kt:historyLists",
                message = "shoppingListRepository is null",
                data = emptyMap(),
                hypothesisId = "A"
            )
            // #endregion
            MutableStateFlow(emptyList<ShoppingListHistory>()).asStateFlow()
        }

    fun getHistoryListWithItems(@Suppress("UNUSED_PARAMETER") historyId: Long): StateFlow<ShoppingListHistoryWithItems?> {
        // Listas arquivadas não têm itens salvos no histórico
        return MutableStateFlow<ShoppingListHistoryWithItems?>(null).asStateFlow()
    }

    fun deleteHistory(historyId: Long) {
        viewModelScope.launch {
            // ID negativo: lista arquivada
            val listId = -historyId
            
            // 1. Deletar o histórico associado (se existir)
            // O histórico contém os itens salvos quando a lista foi arquivada
            val realHistoryList = repository.getHistoryLists(listId).first()
            realHistoryList.firstOrNull { it.listId == listId }?.let { history ->
                repository.deleteHistory(history.id)
            }
            
            // 2. Deletar a lista arquivada
            // Os itens da lista são deletados automaticamente via CASCADE
            shoppingListRepository?.let { repo ->
                val list = repo.getListByIdSync(listId)
                if (list != null) {
                    repo.deleteList(listId)
                }
            }
        }
    }

    // Estado para controlar se está reutilizando (para desabilitar botões na UI)
    private val _isReusing = MutableStateFlow(false)
    val isReusing: StateFlow<Boolean> = _isReusing.asStateFlow()
    
    // Mutex para garantir que apenas uma operação de reutilização ocorra por vez
    private val reuseMutex = Mutex()
    
    fun reuseHistoryList(historyId: Long, onComplete: () -> Unit = {}) {
        // #region agent log
        com.example.minhascompras.utils.DebugLogger.log(
            location = "HistoryViewModel.kt:reuseHistoryList",
            message = "reuseHistoryList called",
            data = mapOf(
                "historyId" to historyId,
                "isReusing" to _isReusing.value
            ),
            hypothesisId = "REUSE"
        )
        // #endregion
        
        // Se já está reutilizando, ignorar nova chamada (verificação rápida antes do mutex)
        if (_isReusing.value) {
            // #region agent log
            com.example.minhascompras.utils.DebugLogger.log(
                location = "HistoryViewModel.kt:reuseHistoryList",
                message = "already reusing, ignoring duplicate call",
                data = mapOf("historyId" to historyId),
                hypothesisId = "REUSE"
            )
            // #endregion
            return
        }
        
        viewModelScope.launch {
            // Usar mutex para garantir atomicidade
            reuseMutex.withLock {
                // Verificação dupla dentro do mutex
                if (_isReusing.value) {
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "already reusing (inside mutex), ignoring",
                        data = mapOf("historyId" to historyId),
                        hypothesisId = "REUSE"
                    )
                    // #endregion
                    return@withLock
                }
                
                // Marcar como reutilizando ANTES de qualquer operação
                _isReusing.value = true
                
                try {
                    // ID negativo: lista arquivada
                    val listId = -historyId
                    
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "converted historyId to listId",
                        data = mapOf(
                            "historyId" to historyId,
                            "listId" to listId
                        ),
                        hypothesisId = "REUSE"
                    )
                    // #endregion

                    // Verificar se há histórico real (ShoppingListHistory) para esta lista
                    // Quando archiveCurrentList é chamado, cria um ShoppingListHistory com os itens
                    val realHistoryList = repository.getHistoryLists(listId).first()
                    val realHistory = realHistoryList.firstOrNull { it.listId == listId }
                    
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "checked for real history",
                        data = mapOf(
                            "listId" to listId,
                            "realHistoryListSize" to realHistoryList.size,
                            "realHistoryFound" to (realHistory != null),
                            "realHistoryId" to (realHistory?.id ?: -1)
                        ),
                        hypothesisId = "REUSE"
                    )
                    // #endregion

                    if (realHistory != null) {
                        // Há histórico com itens: copiar itens de volta para a lista arquivada
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "HistoryViewModel.kt:reuseHistoryList",
                            message = "calling repository.reuseHistoryList",
                            data = mapOf(
                                "realHistoryId" to realHistory.id,
                                "listId" to listId
                            ),
                            hypothesisId = "REUSE"
                        )
                        // #endregion
                        
                        repository.reuseHistoryList(realHistory.id, listId)
                        
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "HistoryViewModel.kt:reuseHistoryList",
                            message = "repository.reuseHistoryList completed",
                            data = mapOf("listId" to listId),
                            hypothesisId = "REUSE"
                        )
                        // #endregion

                        // Desarquivar a lista arquivada
                        shoppingListRepository?.let { repo ->
                            val list = repo.getListByIdSync(listId)
                            // #region agent log
                            com.example.minhascompras.utils.DebugLogger.log(
                                location = "HistoryViewModel.kt:reuseHistoryList",
                                message = "checking list to unarchive",
                                data = mapOf(
                                    "listId" to listId,
                                    "listFound" to (list != null),
                                    "isArchived" to (list?.isArchived ?: false)
                                ),
                                hypothesisId = "REUSE"
                            )
                            // #endregion
                            
                            if (list != null && list.isArchived) {
                                repo.updateList(list.copy(isArchived = false))
                                
                                // #region agent log
                                com.example.minhascompras.utils.DebugLogger.log(
                                    location = "HistoryViewModel.kt:reuseHistoryList",
                                    message = "list unarchived",
                                    data = mapOf("listId" to listId),
                                    hypothesisId = "REUSE"
                                )
                                // #endregion
                            }
                        }

                        // Selecionar a lista arquivada como ativa
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "HistoryViewModel.kt:reuseHistoryList",
                            message = "setting active list",
                            data = mapOf("listId" to listId),
                            hypothesisId = "REUSE"
                        )
                        // #endregion
                        
                        shoppingListPreferencesManager.setActiveListId(listId)
                        
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "HistoryViewModel.kt:reuseHistoryList",
                            message = "active list set",
                            data = mapOf("listId" to listId),
                            hypothesisId = "REUSE"
                        )
                        // #endregion

                        // NÃO deletar o histórico - manter para reutilização futura
                    } else {
                        // Não há histórico: apenas desarquivar e selecionar a lista
                        // (não há itens para copiar - lista foi arquivada sem itens ou histórico foi deletado)
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "HistoryViewModel.kt:reuseHistoryList",
                            message = "no real history, just unarchiving",
                            data = mapOf("listId" to listId),
                            hypothesisId = "REUSE"
                        )
                        // #endregion
                        
                        shoppingListRepository?.let { repo ->
                            val list = repo.getListByIdSync(listId)
                            if (list != null && list.isArchived) {
                                repo.updateList(list.copy(isArchived = false))
                                shoppingListPreferencesManager.setActiveListId(listId)
                            }
                        }
                    }
                    
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "reuse completed successfully",
                        data = mapOf("historyId" to historyId),
                        hypothesisId = "REUSE"
                    )
                    // #endregion
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "reuse cancelled",
                        data = mapOf(
                            "historyId" to historyId,
                            "error" to (e.message ?: "unknown")
                        ),
                        hypothesisId = "REUSE"
                    )
                    // #endregion
                    throw e // Re-throw cancellation exceptions
                } catch (e: Exception) {
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "error during reuse",
                        data = mapOf(
                            "historyId" to historyId,
                            "error" to (e.message ?: "unknown"),
                            "errorType" to e.javaClass.simpleName
                        ),
                        hypothesisId = "REUSE"
                    )
                    // #endregion
                    // Não re-throw outras exceções para não quebrar o app
                } finally {
                    _isReusing.value = false
                    // #region agent log
                    com.example.minhascompras.utils.DebugLogger.log(
                        location = "HistoryViewModel.kt:reuseHistoryList",
                        message = "reuse completed, isReusing reset to false",
                        data = mapOf("historyId" to historyId),
                        hypothesisId = "REUSE"
                    )
                    // #endregion
                    
                    // Chamar callback de conclusão após a operação terminar
                    onComplete()
                }
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

