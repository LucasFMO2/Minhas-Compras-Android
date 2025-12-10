package com.example.minhascompras.data

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

/**
 * Gerencia o agendamento da verificação de itens pendentes.
 * 
 * Agenda o Worker para executar diariamente e verificar itens
 * que estão pendentes há mais tempo que o configurado.
 */
class PendingItemsScheduler(private val context: Context) {
    
    companion object {
        private const val WORK_NAME = "pending_items_work"
        // Executar diariamente
        private const val REPEAT_INTERVAL_HOURS = 24L
    }

    /**
     * Agenda a verificação de itens pendentes para executar diariamente.
     */
    fun schedulePendingItemsCheck() {
        try {
            val workRequest = OneTimeWorkRequestBuilder<PendingItemsWorker>()
                .setInitialDelay(REPEAT_INTERVAL_HOURS, TimeUnit.HOURS)
                .addTag("pending_items")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d("PendingItemsScheduler", "Verificação de itens pendentes agendada")
        } catch (e: Exception) {
            Log.e("PendingItemsScheduler", "Erro ao agendar verificação de itens pendentes", e)
        }
    }

    /**
     * Cancela o agendamento da verificação de itens pendentes.
     */
    fun cancelPendingItemsCheck() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d("PendingItemsScheduler", "Verificação de itens pendentes cancelada")
        } catch (e: Exception) {
            Log.e("PendingItemsScheduler", "Erro ao cancelar verificação de itens pendentes", e)
        }
    }

    /**
     * Atualiza o agendamento baseado nas preferências do usuário.
     */
    suspend fun updateSchedule() {
        val preferencesManager = NotificationPreferencesManager(context)
        val isEnabled = preferencesManager.isPendingItemsEnabled()
        
        if (isEnabled) {
            schedulePendingItemsCheck()
        } else {
            cancelPendingItemsCheck()
        }
    }
}

