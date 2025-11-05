package com.example.minhascompras.data

import kotlinx.coroutines.flow.Flow

class ItemCompraRepository(private val itemCompraDao: ItemCompraDao) {
    val allItens: Flow<List<ItemCompra>> = itemCompraDao.getAllItens()

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
}

