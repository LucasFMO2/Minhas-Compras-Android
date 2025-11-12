package com.example.minhascompras.data.supabase

import com.example.minhascompras.data.ShoppingListHistory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de dados para Supabase (com campos adicionais)
 */
@Serializable
data class ShoppingListHistorySupabase(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("completion_date")
    val completionDate: Long,
    @SerialName("list_name")
    val listName: String = "Lista de Compras"
)

/**
 * Converte ShoppingListHistory (Room) para ShoppingListHistorySupabase
 */
fun ShoppingListHistory.toSupabase(userId: String? = null): ShoppingListHistorySupabase {
    return ShoppingListHistorySupabase(
        id = if (id > 0) id else null,
        userId = userId,
        completionDate = completionDate,
        listName = listName
    )
}

/**
 * Converte ShoppingListHistorySupabase para ShoppingListHistory (Room)
 */
fun ShoppingListHistorySupabase.toRoom(): ShoppingListHistory {
    return ShoppingListHistory(
        id = id ?: 0L,
        completionDate = completionDate,
        listName = listName
    )
}

