package com.example.minhascompras.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListHistory::class,
            parentColumns = ["id"],
            childColumns = ["parentListId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentListId"])]
)
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentListId: Long,
    val nome: String,
    val quantidade: Int = 1,
    val preco: Double? = null,
    val categoria: String = ItemCategory.OUTROS.displayName
)

