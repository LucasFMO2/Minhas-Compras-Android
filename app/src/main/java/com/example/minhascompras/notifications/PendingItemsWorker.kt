package com.example.minhascompras.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.NotificationPreferencesManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker responsável por verificar itens pendentes há muito tempo.
 * 
 * Este worker é executado periodicamente (diariamente) e verifica se há itens
 * que estão pendentes há mais de X dias (configurável pelo usuário).
 * Se encontrar, envia uma notificação alertando o usuário.
 */
class PendingItemsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val TAG = "PendingItemsWorker"
        const val WORK_NAME = "pending_items_work"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val preferencesManager = NotificationPreferencesManager(applicationContext)
            
            // Verificar se a notificação está habilitada
            val isEnabled = preferencesManager.isPendingItemsNotificationEnabled().first()
            if (!isEnabled) {
                Log.d(TAG, "Notificação de itens pendentes desabilitada, ignorando")
                return Result.success()
            }
            
            // Obter threshold de dias
            val daysThreshold = preferencesManager.getPendingItemsDaysThreshold().first()
            val thresholdMillis = TimeUnit.DAYS.toMillis(daysThreshold.toLong())
            val currentTime = System.currentTimeMillis()
            
            // Obter itens pendentes antigos
            val database = AppDatabase.getDatabase(applicationContext)
            val allItems = database.itemCompraDao().getAllItens().first()
            val oldPendingItems = allItems.filter { item ->
                !item.comprado && (currentTime - item.dataCriacao) > thresholdMillis
            }
            
            Log.d(TAG, "Verificando itens pendentes. Threshold: $daysThreshold dias. Encontrados: ${oldPendingItems.size}")
            
            // Enviar notificação se houver itens pendentes antigos
            if (oldPendingItems.isNotEmpty()) {
                NotificationHelper.showPendingItemsNotification(
                    applicationContext,
                    oldPendingItems.size,
                    daysThreshold
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar itens pendentes", e)
            Result.failure()
        }
    }
}

