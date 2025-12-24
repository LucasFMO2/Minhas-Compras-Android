package com.example.minhascompras.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.NotificationPreferencesManager
import kotlinx.coroutines.flow.first

/**
 * Worker responsável por enviar lembretes diários ao usuário.
 * 
 * Este worker é executado uma vez por dia no horário configurado pelo usuário.
 * Verifica quantos itens estão na lista e envia uma notificação lembrando o usuário.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val TAG = "DailyReminderWorker"
        const val WORK_NAME = "daily_reminder_work"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val preferencesManager = NotificationPreferencesManager(applicationContext)
            
            // Verificar se a notificação está habilitada
            val isEnabled = preferencesManager.isDailyReminderEnabled().first()
            if (!isEnabled) {
                Log.d(TAG, "Lembrete diário desabilitado, ignorando")
                return Result.success()
            }
            
            // Obter quantidade de itens na lista
            val database = AppDatabase.getDatabase(applicationContext)
            val itemCount = database.itemCompraDao().getAllItens().first().size
            
            Log.d(TAG, "Enviando lembrete diário. Itens na lista: $itemCount")
            
            // Enviar notificação
            NotificationHelper.showDailyReminderNotification(applicationContext, itemCount)
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar lembrete diário", e)
            Result.failure()
        }
    }
}

