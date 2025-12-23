package com.example.minhascompras.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "itens_compra",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class ItemCompra(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val quantidade: Int = 1,
    val preco: Double? = null,
    val comprado: Boolean = false,
    val categoria: String = ItemCategory.OUTROS.displayName,
    val dataCriacao: Long = System.currentTimeMillis(),
    val listId: Long? = null // ID da lista (nullable - item pode não ter lista associada)
)

