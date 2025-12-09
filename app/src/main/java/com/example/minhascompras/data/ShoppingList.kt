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
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
) {
    companion object {
        const val DEFAULT_LIST_NAME = "Minhas Compras"
        const val DEFAULT_LIST_ID = 1L // ID fixo para a lista padrão
    }
}

