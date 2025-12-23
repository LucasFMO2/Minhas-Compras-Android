package com.example.minhascompras.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val dataCriacao: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

