package com.example.minhascompras.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itens_compra")
data class ItemCompra(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val quantidade: Int = 1,
    val preco: Double? = null,
    val comprado: Boolean = false,
    val categoria: String = ItemCategory.OUTROS.displayName,
    val dataCriacao: Long = System.currentTimeMillis()
)

