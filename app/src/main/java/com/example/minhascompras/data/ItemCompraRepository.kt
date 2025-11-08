package com.example.minhascompras.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ItemCompraRepository(private val itemCompraDao: ItemCompraDao) {
    val allItens: Flow<List<ItemCompra>> = itemCompraDao.getAllItens()

    suspend fun getAllItensList(): List<ItemCompra> {
        return itemCompraDao.getAllItens().first()
    }

    fun getFilteredItens(searchQuery: String, filterStatus: FilterStatus): Flow<List<ItemCompra>> {
        val normalizedQuery = searchQuery.trim().lowercase()
        
        return when (filterStatus) {
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

