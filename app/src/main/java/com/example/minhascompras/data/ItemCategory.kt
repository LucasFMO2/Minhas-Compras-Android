package com.example.minhascompras.data

enum class ItemCategory(val displayName: String) {
    FRUTAS_E_VERDURAS("Frutas e Verduras"),
    LATICINIOS("Laticínios"),
    CARNES_E_AVES("Carnes e Aves"),
    PADARIA("Padaria"),
    LIMPEZA("Limpeza"),
    HIGIENE("Higiene"),
    BEBIDAS("Bebidas"),
    GRAOS_E_CEREAIS("Grãos e Cereais"),
    OUTROS("Outros");

    companion object {
        fun fromDisplayName(displayName: String): ItemCategory {
            return values().find { it.displayName == displayName } ?: OUTROS
        }

        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}

