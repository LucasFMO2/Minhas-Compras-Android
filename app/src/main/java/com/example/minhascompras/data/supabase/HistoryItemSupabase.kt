package com.example.minhascompras.data.supabase

import com.example.minhascompras.data.HistoryItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de dados para Supabase
 */
@Serializable
data class HistoryItemSupabase(
    val id: Long? = null,
    @SerialName("parent_list_id")
    val parentListId: Long,
    val nome: String,
    val quantidade: Int = 1,
    val preco: Double? = null,
    val categoria: String = "Outros"
)

/**
 * Converte HistoryItem (Room) para HistoryItemSupabase
 */
fun HistoryItem.toSupabase(): HistoryItemSupabase {
    return HistoryItemSupabase(
        id = if (id > 0) id else null,
        parentListId = parentListId,
        nome = nome,
        quantidade = quantidade,
        preco = preco,
        categoria = categoria
    )
}

/**
 * Converte HistoryItemSupabase para HistoryItem (Room)
 */
fun HistoryItemSupabase.toRoom(): HistoryItem {
    return HistoryItem(
        id = id ?: 0L,
        parentListId = parentListId,
        nome = nome,
        quantidade = quantidade,
        preco = preco,
        categoria = categoria
    )
}

