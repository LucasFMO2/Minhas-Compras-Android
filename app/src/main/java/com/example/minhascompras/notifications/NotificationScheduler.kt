package com.example.minhascompras.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de agendamento de notificações usando WorkManager.
 * 
 * Responsável por agendar, reagendar e cancelar todos os workers de notificação.
 */
object NotificationScheduler {
    
    private const val TAG = "NotificationScheduler"
    
    /**
     * Agenda ou reagenda o lembrete diário.
     * 
     * @param context Contexto da aplicação
     * @param hour Hora do dia (0-23)
     * @param minute Minuto da hora (0-59)
     * @param enabled Se true, agenda o worker; se false, cancela
     */
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int, enabled: Boolean) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(DailyReminderWorker.WORK_NAME)
            Log.d(TAG, "Lembrete diário cancelado")
            return
        }
        
        // Calcular delay inicial até o próximo horário configurado
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // Se o horário já passou hoje, agendar para amanhã
            if (timeInMillis <= currentTime) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        val initialDelay = calendar.timeInMillis - currentTime
        
        // Criar constraints
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        // Criar work request
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DailyReminderWorker.TAG)
            .build()
        
        // Agendar work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.d(TAG, "Lembrete diário agendado para $hour:$minute (delay inicial: ${initialDelay / 1000 / 60} minutos)")
    }
    
    /**
     * Agenda ou reagenda a verificação de itens pendentes.
     * 
     * @param context Contexto da aplicação
     * @param enabled Se true, agenda o worker; se false, cancela
     */
    fun schedulePendingItemsCheck(context: Context, enabled: Boolean) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(PendingItemsWorker.WORK_NAME)
            Log.d(TAG, "Verificação de itens pendentes cancelada")
            return
        }
        
        // Criar constraints
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        // Criar work request (executa uma vez por dia)
        val workRequest = PeriodicWorkRequestBuilder<PendingItemsWorker>(
            1, TimeUnit.DAYS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag(PendingItemsWorker.TAG)
            .build()
        
        // Agendar work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PendingItemsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Log.d(TAG, "Verificação de itens pendentes agendada (1x por dia)")
    }
    
    /**
     * Cancela todos os workers de notificação.
     */
    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(DailyReminderWorker.WORK_NAME)
            cancelUniqueWork(PendingItemsWorker.WORK_NAME)
        }
        Log.d(TAG, "Todas as notificações canceladas")
    }
}

