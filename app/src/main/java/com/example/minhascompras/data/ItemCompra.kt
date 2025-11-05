package com.example.minhascompras.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itens_compra")
data class ItemCompra(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val quantidade: Int = 1,
    val comprado: Boolean = false,
    val dataCriacao: Long = System.currentTimeMillis()
)

