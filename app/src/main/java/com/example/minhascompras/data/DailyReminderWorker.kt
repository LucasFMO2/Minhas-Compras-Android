package com.example.minhascompras.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.minhascompras.MainActivity
import java.util.Calendar

/**
 * Worker que executa o lembrete diário de compras.
 * 
 * Este worker é executado no horário exato configurado pelo usuário e:
 * 1. Verifica se o lembrete diário está habilitado nas preferências
 * 2. Verifica se está no horário correto (com tolerância de ±30 minutos)
 * 3. Conta quantos itens estão pendentes
 * 4. Exibe uma notificação se houver itens pendentes
 * 5. Reagenda para o próximo dia no mesmo horário
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("DailyReminderWorker", "Executando lembrete diário")
            
            // Verificar se o lembrete está habilitado
            val preferencesManager = NotificationPreferencesManager(applicationContext)
            val isEnabled = preferencesManager.isDailyReminderEnabled()
            
            if (!isEnabled) {
                Log.d("DailyReminderWorker", "Lembrete diário está desabilitado, pulando notificação")
                // Não reagendar se estiver desabilitado
                return Result.success()
            }

            // Verificar se está no horário correto (com tolerância de ±30 minutos)
            val configuredHour = preferencesManager.getDailyReminderHour()
            val configuredMinute = preferencesManager.getDailyReminderMinute()
            
            if (!isWithinTimeWindow(configuredHour, configuredMinute)) {
                Log.d("DailyReminderWorker", "Não está no horário configurado ($configuredHour:$configuredMinute), mas reagendando mesmo assim")
                // Reagendar mesmo assim, pois pode ter sido executado com pequeno atraso
            }

            // Contar itens pendentes
            val database = AppDatabase.getDatabase(applicationContext)
            val pendingCount = database.itemCompraDao().countPendingItems()
            
            if (pendingCount == 0) {
                Log.d("DailyReminderWorker", "Nenhum item pendente, não exibindo notificação")
                // Reagendar mesmo sem itens pendentes
                rescheduleNextReminder()
                return Result.success()
            }

            // Criar e exibir notificação
            showNotification(pendingCount)
            
            Log.d("DailyReminderWorker", "Notificação de lembrete diário exibida com sucesso")
            
            // Reagendar para o próximo dia no mesmo horário
            rescheduleNextReminder()
            
            Result.success()
            
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "Erro ao executar lembrete diário", e)
            // Tentar reagendar mesmo em caso de erro
            try {
                rescheduleNextReminder()
            } catch (e2: Exception) {
                Log.e("DailyReminderWorker", "Erro ao reagendar após falha", e2)
            }
            // Retornar success mesmo em caso de erro para não marcar o trabalho como falho
            Result.success()
        }
    }

    /**
     * Verifica se o horário atual está dentro da janela de tolerância do horário configurado.
     * Tolerância: ±30 minutos.
     */
    private fun isWithinTimeWindow(configuredHour: Int, configuredMinute: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        val currentTotalMinutes = currentHour * 60 + currentMinute
        val configuredTotalMinutes = configuredHour * 60 + configuredMinute
        
        val difference = Math.abs(currentTotalMinutes - configuredTotalMinutes)
        
        // Tolerância de 30 minutos
        return difference <= 30
    }

    /**
     * Reagenda o próximo lembrete para o dia seguinte no mesmo horário.
     */
    private suspend fun rescheduleNextReminder() {
        try {
            val scheduler = DailyReminderScheduler(applicationContext)
            scheduler.scheduleNextReminder()
            Log.d("DailyReminderWorker", "Próximo lembrete reagendado com sucesso")
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "Erro ao reagendar próximo lembrete", e)
        }
    }

    private fun showNotification(pendingCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent para abrir o app quando a notificação for tocada
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Criar mensagem personalizada baseada na quantidade de itens
        val message = when {
            pendingCount == 1 -> "Você tem 1 item pendente na sua lista"
            else -> "Você tem $pendingCount itens pendentes na sua lista"
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannelManager.CHANNEL_DAILY_REMINDER
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lembrete de Compras")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val PENDING_INTENT_REQUEST_CODE = 2001
    }
}

