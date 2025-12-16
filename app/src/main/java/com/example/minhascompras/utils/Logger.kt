package com.example.minhascompras.utils

import android.util.Log

/**
 * Sistema de logging que desabilita logs em builds de release
 * para melhorar performance e segurança
 */
object Logger {
    private const val DEFAULT_TAG = "MinhasCompras"
    
    // Flag para controlar logs (pode ser configurada via ProGuard ou build config)
    private const val ENABLE_LOGS = false // Desabilitado por padrão em release
    
    @JvmStatic
    fun d(tag: String = DEFAULT_TAG, message: String) {
        // Logs desabilitados em release para melhor performance
        // Para habilitar em debug, usar android.util.Log diretamente
    }
    
    @JvmStatic
    fun e(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        // Logs desabilitados em release para melhor performance
        // Para habilitar em debug, usar android.util.Log diretamente
    }
    
    @JvmStatic
    fun i(tag: String = DEFAULT_TAG, message: String) {
        // Logs desabilitados em release para melhor performance
        // Para habilitar em debug, usar android.util.Log diretamente
    }
    
    @JvmStatic
    fun w(tag: String = DEFAULT_TAG, message: String) {
        // Logs desabilitados em release para melhor performance
        // Para habilitar em debug, usar android.util.Log diretamente
    }
}

