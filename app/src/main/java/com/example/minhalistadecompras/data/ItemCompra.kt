package com.example.minhalistadecompras.data

import androidx.compose.runtime.Immutable

@Immutable
data class ItemCompra(
    val id: Long,
    val nome: String,
    val quantidade: Int = 1,
    val comprado: Boolean = false,
    val categoria: String = "Outros"
) {
    companion object {
        fun createSampleItems(): List<ItemCompra> = listOf(
            ItemCompra(1, "Leite", 2, false, "Laticínios"),
            ItemCompra(2, "Pão", 1, false, "Padaria"),
            ItemCompra(3, "Ovos", 12, true, "Laticínios"),
            ItemCompra(4, "Arroz", 1, false, "Grãos"),
            ItemCompra(5, "Feijão", 2, false, "Grãos"),
            ItemCompra(6, "Banana", 1, true, "Frutas"),
            ItemCompra(7, "Maçã", 6, false, "Frutas"),
            ItemCompra(8, "Frango", 1, false, "Carnes")
        )
    }
}
