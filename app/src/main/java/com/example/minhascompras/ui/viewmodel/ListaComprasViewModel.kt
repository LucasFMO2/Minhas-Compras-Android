package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.ItemCompraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListaComprasViewModel(private val repository: ItemCompraRepository) : ViewModel() {
    val itens: StateFlow<List<ItemCompra>> = repository.allItens
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun inserirItem(nome: String, quantidade: Int = 1, preco: Double? = null) {
        if (nome.isNotBlank()) {
            viewModelScope.launch {
                repository.insert(ItemCompra(nome = nome.trim(), quantidade = quantidade, preco = preco))
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
            repository.delete(item)
        }
    }

    fun toggleComprado(item: ItemCompra) {
        viewModelScope.launch {
            repository.update(item.copy(comprado = !item.comprado))
        }
    }

    fun deletarComprados() {
        viewModelScope.launch {
            repository.deleteComprados()
        }
    }
}

class ListaComprasViewModelFactory(private val repository: ItemCompraRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListaComprasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListaComprasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

