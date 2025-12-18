package com.example.minhascompras.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val dataCriacao: Long = System.currentTimeMillis(),
    val isAtiva: Boolean = true,
    val ordem: Int = 0 // Para ordenação personalizada das listas
)