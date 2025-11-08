package com.example.minhascompras.data

enum class FilterStatus(val displayName: String) {
    ALL("Todos"),
    PENDING("Pendentes"),
    PURCHASED("Comprados");

    companion object {
        fun fromDisplayName(displayName: String): FilterStatus {
            return values().find { it.displayName == displayName } ?: ALL
        }
    }
}

