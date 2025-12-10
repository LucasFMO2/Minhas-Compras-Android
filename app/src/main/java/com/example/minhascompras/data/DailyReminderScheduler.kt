package com.example.minhascompras.data

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Gerencia o agendamento do lembrete diário usando WorkManager.
 * 
 * Este scheduler é responsável por:
 * - Agendar o trabalho para o horário exato configurado pelo usuário
 * - Cancelar o trabalho quando necessário
 * - Garantir que apenas uma instância do trabalho esteja agendada
 */
class DailyReminderScheduler(private val context: Context) {
    
    companion object {
        private const val WORK_NAME = "daily_reminder_work"
    }

    /**
     * Calcula o delay em milissegundos até o próximo horário configurado.
     * 
     * Se o horário já passou hoje, agenda para amanhã no mesmo horário.
     * Se ainda não passou, agenda para hoje no horário configurado.
     */
    private fun calculateDelayUntilNextTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Definir o horário alvo para hoje
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val targetTime = calendar.timeInMillis
        
        // Se o horário já passou hoje, agendar para amanhã
        val delay = if (targetTime <= now) {
            // Adicionar 24 horas
            targetTime + TimeUnit.HOURS.toMillis(24) - now
        } else {
            targetTime - now
        }
        
        return delay
    }

    /**
     * Agenda o próximo lembrete diário para o horário exato configurado.
     * 
     * Usa OneTimeWorkRequest com delay calculado até o próximo horário.
     * O Worker reagendará após executar.
     */
    suspend fun scheduleNextReminder() {
        try {
            val preferencesManager = NotificationPreferencesManager(context)
            val hour = preferencesManager.getDailyReminderHour()
            val minute = preferencesManager.getDailyReminderMinute()
            
            val delayMillis = calculateDelayUntilNextTime(hour, minute)
            val delayHours = TimeUnit.MILLISECONDS.toHours(delayMillis)
            val delayMinutes = TimeUnit.MILLISECONDS.toMinutes(delayMillis) % 60
            
            Log.d("DailyReminderScheduler", "Agendando lembrete para $hour:$minute (delay: ${delayHours}h ${delayMinutes}m)")
            
            val workRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            Log.d("DailyReminderScheduler", "Lembrete diário agendado com sucesso para $hour:$minute")
        } catch (e: Exception) {
            Log.e("DailyReminderScheduler", "Erro ao agendar lembrete diário", e)
        }
    }

    /**
     * Agenda o lembrete diário para ser executado periodicamente.
     * 
     * @deprecated Use scheduleNextReminder() para agendamento preciso no horário exato.
     */
    @Deprecated("Use scheduleNextReminder() para agendamento preciso")
    fun scheduleDailyReminder() {
        runBlocking {
            scheduleNextReminder()
        }
    }

    /**
     * Cancela o agendamento do lembrete diário.
     */
    fun cancelDailyReminder() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d("DailyReminderScheduler", "Lembrete diário cancelado")
        } catch (e: Exception) {
            Log.e("DailyReminderScheduler", "Erro ao cancelar lembrete diário", e)
        }
    }

    /**
     * Atualiza o agendamento do lembrete diário.
     * 
     * Se o lembrete estiver habilitado, agenda o trabalho para o horário exato.
     * Se estiver desabilitado, cancela o trabalho.
     */
    suspend fun updateSchedule() {
        val preferencesManager = NotificationPreferencesManager(context)
        val isEnabled = preferencesManager.isDailyReminderEnabled()
        
        if (isEnabled) {
            scheduleNextReminder()
        } else {
            cancelDailyReminder()
        }
    }
}

