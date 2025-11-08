package com.example.minhascompras.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ItemCompraRepository(private val itemCompraDao: ItemCompraDao) {
    val allItens: Flow<List<ItemCompra>> = itemCompraDao.getAllItens()

    suspend fun getAllItensList(): List<ItemCompra> {
        return itemCompraDao.getAllItens().first()
    }

    fun getFilteredItens(
        searchQuery: String, 
        filterStatus: FilterStatus,
        sortOrder: SortOrder
    ): Flow<List<ItemCompra>> {
        val normalizedQuery = searchQuery.trim().lowercase()
        
        val baseFlow = when (filterStatus) {
            FilterStatus.ALL -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getAllItens()
                } else {
                    itemCompraDao.searchItens(searchQuery)
                }
            }
            FilterStatus.PENDING -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByStatus(comprado = false)
                } else {
                    itemCompraDao.searchItensByStatus(searchQuery, comprado = false)
                }
            }
            FilterStatus.PURCHASED -> {
                if (normalizedQuery.isEmpty()) {
                    itemCompraDao.getItensByStatus(comprado = true)
                } else {
                    itemCompraDao.searchItensByStatus(searchQuery, comprado = true)
                }
            }
        }
        
        return baseFlow.map { items -> applySortOrder(items, sortOrder) }
    }
    
    private fun applySortOrder(items: List<ItemCompra>, sortOrder: SortOrder): List<ItemCompra> {
        // Primeiro, separar itens comprados e não comprados
        val comprados = items.filter { it.comprado }
        val naoComprados = items.filter { !it.comprado }
        
        // Aplicar ordenação em cada grupo
        val sortedNaoComprados = when (sortOrder) {
            SortOrder.BY_NAME_ASC -> naoComprados.sortedBy { it.nome.lowercase() }
            SortOrder.BY_NAME_DESC -> naoComprados.sortedByDescending { it.nome.lowercase() }
            SortOrder.BY_DATE_ASC -> naoComprados.sortedBy { it.dataCriacao }
            SortOrder.BY_DATE_DESC -> naoComprados.sortedByDescending { it.dataCriacao }
            SortOrder.BY_PRICE_ASC -> naoComprados.sortedWith(compareBy(nullsLast()) { it.preco })
            SortOrder.BY_PRICE_DESC -> naoComprados.sortedWith(compareByDescending(nullsLast()) { it.preco })
        }
        
        val sortedComprados = when (sortOrder) {
            SortOrder.BY_NAME_ASC -> comprados.sortedBy { it.nome.lowercase() }
            SortOrder.BY_NAME_DESC -> comprados.sortedByDescending { it.nome.lowercase() }
            SortOrder.BY_DATE_ASC -> comprados.sortedBy { it.dataCriacao }
            SortOrder.BY_DATE_DESC -> comprados.sortedByDescending { it.dataCriacao }
            SortOrder.BY_PRICE_ASC -> comprados.sortedWith(compareBy(nullsLast()) { it.preco })
            SortOrder.BY_PRICE_DESC -> comprados.sortedWith(compareByDescending(nullsLast()) { it.preco })
        }
        
        // Retornar não comprados primeiro, depois comprados
        return sortedNaoComprados + sortedComprados
    }

    suspend fun insert(item: ItemCompra): Long {
        return itemCompraDao.insert(item)
    }

    suspend fun update(item: ItemCompra) {
        itemCompraDao.update(item)
    }

    suspend fun delete(item: ItemCompra) {
        itemCompraDao.delete(item)
    }

    suspend fun deleteComprados() {
        itemCompraDao.deleteComprados()
    }

    suspend fun replaceAllItems(items: List<ItemCompra>) {
        itemCompraDao.replaceAllItems(items)
    }
}

