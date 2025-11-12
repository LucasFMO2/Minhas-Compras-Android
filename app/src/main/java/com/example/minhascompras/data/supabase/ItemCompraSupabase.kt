package com.example.minhascompras.data.supabase

import com.example.minhascompras.data.ItemCompra
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de dados para Supabase (com campos adicionais para sincronização)
 */
@Serializable
data class ItemCompraSupabase(
    val id: Long? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val nome: String,
    val quantidade: Int = 1,
    val preco: Double? = null,
    val comprado: Boolean = false,
    val categoria: String = "Outros",
    @SerialName("data_criacao")
    val dataCriacao: Long = System.currentTimeMillis(),
    @SerialName("data_atualizacao")
    val dataAtualizacao: Long = System.currentTimeMillis(),
    @SerialName("sync_status")
    val syncStatus: String = "synced"
)

/**
 * Converte ItemCompra (Room) para ItemCompraSupabase
 */
fun ItemCompra.toSupabase(userId: String? = null): ItemCompraSupabase {
    return ItemCompraSupabase(
        id = if (id > 0) id else null, // null para novos itens
        userId = userId,
        nome = nome,
        quantidade = quantidade,
        preco = preco,
        comprado = comprado,
        categoria = categoria,
        dataCriacao = dataCriacao,
        dataAtualizacao = System.currentTimeMillis(),
        syncStatus = "synced"
    )
}

/**
 * Converte ItemCompraSupabase para ItemCompra (Room)
 */
fun ItemCompraSupabase.toRoom(): ItemCompra {
    return ItemCompra(
        id = id ?: 0L,
        nome = nome,
        quantidade = quantidade,
        preco = preco,
        comprado = comprado,
        categoria = categoria,
        dataCriacao = dataCriacao
    )
}

