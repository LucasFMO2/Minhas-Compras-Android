package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.ShoppingListHistory
import com.example.minhascompras.data.ShoppingListHistoryWithItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: ItemCompraRepository
) : ViewModel() {
    val historyLists: StateFlow<List<ShoppingListHistory>> = repository.getHistoryLists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
            repository.reuseHistoryList(historyId)
        }
    }
}

class HistoryViewModelFactory(
    private val repository: ItemCompraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

