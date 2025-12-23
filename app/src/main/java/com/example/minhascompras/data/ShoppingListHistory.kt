package com.example.minhascompras.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list_history")
data class ShoppingListHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val completionDate: Long = System.currentTimeMillis(),
    val listName: String = "Lista de Compras",
    @ColumnInfo(name = "listId")
    val listId: Long? = null // ID da lista de compras associada (nullable para histórico antigo)
)

