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

class HistoryViewModel(
    private val repository: ItemCompraRepository,
    private val shoppingListPreferencesManager: ShoppingListPreferencesManager,
    private val shoppingListRepository: ShoppingListRepository? = null
) : ViewModel() {
    // Histórico: APENAS listas arquivadas, sem filtros
    val historyLists: StateFlow<List<ShoppingListHistory>> = 
        if (shoppingListRepository != null) {
            shoppingListRepository.getArchivedLists()
                .map { archivedLists ->
                    // Converter todas as listas arquivadas em ShoppingListHistory
                    archivedLists.map { archivedList ->
                        ShoppingListHistory(
                            id = -archivedList.id, // ID negativo para diferenciar
                            listId = archivedList.id,
                            listName = archivedList.nome,
                            completionDate = archivedList.dataCriacao
                        )
                    }.sortedByDescending { it.completionDate }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        } else {
            MutableStateFlow(emptyList<ShoppingListHistory>()).asStateFlow()
        }

    fun getHistoryListWithItems(@Suppress("UNUSED_PARAMETER") historyId: Long): StateFlow<ShoppingListHistoryWithItems?> {
        // Listas arquivadas não têm itens salvos no histórico
        return MutableStateFlow<ShoppingListHistoryWithItems?>(null).asStateFlow()
    }

    fun deleteHistory(historyId: Long) {
        viewModelScope.launch {
            // Sempre desarquivar a lista (ID sempre negativo)
            val listId = -historyId
            shoppingListRepository?.let { repo ->
                val list = repo.getListByIdSync(listId)
                if (list != null && list.isArchived) {
                    repo.updateList(list.copy(isArchived = false))
                }
            }
        }
    }

    fun reuseHistoryList(historyId: Long) {
        viewModelScope.launch {
            // ID negativo: lista arquivada
            val listId = -historyId

            // Verificar se há histórico real (ShoppingListHistory) para esta lista
            // Quando archiveCurrentList é chamado, cria um ShoppingListHistory com os itens
            val realHistoryList = repository.getHistoryLists(listId).first()
            val realHistory = realHistoryList.firstOrNull { it.listId == listId }

            if (realHistory != null) {
                // Há histórico com itens: copiar itens de volta para a lista arquivada
                repository.reuseHistoryList(realHistory.id, listId)

                // Desarquivar a lista arquivada
                shoppingListRepository?.let { repo ->
                    val list = repo.getListByIdSync(listId)
                    if (list != null && list.isArchived) {
                        repo.updateList(list.copy(isArchived = false))
                    }
                }

                // Selecionar a lista arquivada como ativa
                shoppingListPreferencesManager.setActiveListId(listId)

                // NÃO deletar o histórico - manter para reutilização futura
            } else {
                // Não há histórico: apenas desarquivar e selecionar a lista
                // (não há itens para copiar - lista foi arquivada sem itens ou histórico foi deletado)
                shoppingListRepository?.let { repo ->
                    val list = repo.getListByIdSync(listId)
                    if (list != null && list.isArchived) {
                        repo.updateList(list.copy(isArchived = false))
                        shoppingListPreferencesManager.setActiveListId(listId)
                    }
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

