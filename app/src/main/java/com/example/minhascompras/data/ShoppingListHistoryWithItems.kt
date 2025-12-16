package com.example.minhascompras.data

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingListHistoryWithItems(
    @Embedded
    val history: ShoppingListHistory,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentListId"
    )
    val items: List<HistoryItem>
)

