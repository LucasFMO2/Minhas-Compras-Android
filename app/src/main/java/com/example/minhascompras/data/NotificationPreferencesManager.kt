package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notificationPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

/**
 * Gerencia as preferências de notificações do usuário.
 * 
 * Armazena configurações como:
 * - Se o lembrete diário está habilitado
 * - Horário do lembrete diário (hora e minuto)
 * - Outras configurações de notificações (futuras)
 */
class NotificationPreferencesManager(private val context: Context) {
    companion object {
        // Chaves de preferências
        private val DAILY_REMINDER_ENABLED_KEY = booleanPreferencesKey("daily_reminder_enabled")
        private val DAILY_REMINDER_HOUR_KEY = intPreferencesKey("daily_reminder_hour")
        private val DAILY_REMINDER_MINUTE_KEY = intPreferencesKey("daily_reminder_minute")
        private val PURCHASE_COMPLETE_ENABLED_KEY = booleanPreferencesKey("purchase_complete_enabled")
        private val PENDING_ITEMS_ENABLED_KEY = booleanPreferencesKey("pending_items_enabled")
        private val PENDING_ITEMS_DAYS_KEY = intPreferencesKey("pending_items_days")
        
        // Valores padrão
        private const val DEFAULT_HOUR = 9 // 9:00 AM
        private const val DEFAULT_MINUTE = 0
        private const val DEFAULT_PENDING_ITEMS_DAYS = 7
    }

    /**
     * Flow que emite se o lembrete diário está habilitado.
     */
    val dailyReminderEnabled: Flow<Boolean> = context.notificationPreferencesDataStore.data.map { preferences ->
        preferences[DAILY_REMINDER_ENABLED_KEY] ?: false
    }

    /**
     * Flow que emite a hora configurada para o lembrete diário (0-23).
     */
    val dailyReminderHour: Flow<Int> = context.notificationPreferencesDataStore.data.map { preferences ->
        preferences[DAILY_REMINDER_HOUR_KEY] ?: DEFAULT_HOUR
    }

    /**
     * Flow que emite o minuto configurado para o lembrete diário (0-59).
     */
    val dailyReminderMinute: Flow<Int> = context.notificationPreferencesDataStore.data.map { preferences ->
        preferences[DAILY_REMINDER_MINUTE_KEY] ?: DEFAULT_MINUTE
    }

    /**
     * Habilita ou desabilita o lembrete diário.
     */
    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.notificationPreferencesDataStore.edit { preferences ->
            preferences[DAILY_REMINDER_ENABLED_KEY] = enabled
        }
    }

    /**
     * Define o horário do lembrete diário.
     * 
     * @param hour Hora do dia (0-23)
     * @param minute Minuto (0-59)
     */
    suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        require(hour in 0..23) { "Hora deve estar entre 0 e 23" }
        require(minute in 0..59) { "Minuto deve estar entre 0 e 59" }
        
        context.notificationPreferencesDataStore.edit { preferences ->
            preferences[DAILY_REMINDER_HOUR_KEY] = hour
            preferences[DAILY_REMINDER_MINUTE_KEY] = minute
        }
    }

    /**
     * Obtém o valor atual de se o lembrete diário está habilitado (suspending).
     */
    suspend fun isDailyReminderEnabled(): Boolean {
        return dailyReminderEnabled.first()
    }

    /**
     * Obtém a hora atual configurada para o lembrete diário (suspending).
     */
    suspend fun getDailyReminderHour(): Int {
        return dailyReminderHour.first()
    }

    /**
     * Obtém o minuto atual configurado para o lembrete diário (suspending).
     */
    suspend fun getDailyReminderMinute(): Int {
        return dailyReminderMinute.first()
    }

    /**
     * Flow que emite se a notificação de compra concluída está habilitada.
     */
    val purchaseCompleteEnabled: Flow<Boolean> = context.notificationPreferencesDataStore.data.map { preferences ->
        preferences[PURCHASE_COMPLETE_ENABLED_KEY] ?: true // Padrão: habilitado
    }

    /**
     * Flow que emite se a notificação de itens pendentes está habilitada.
     */
    val pendingItemsEnabled: Flow<Boolean> = context.notificationPreferencesDataStore.data.map { preferences ->
        preferences[PENDING_ITEMS_ENABLED_KEY] ?: true // Padrão: habilitado
    }

    /**
     * Flow que emite o número de dias para considerar itens como pendentes há muito tempo.
     */
    val pendingItemsDays: Flow<Int> = context.notificationPreferencesDataStore.data.map { preferences ->
        preferences[PENDING_ITEMS_DAYS_KEY] ?: DEFAULT_PENDING_ITEMS_DAYS
    }

    /**
     * Habilita ou desabilita a notificação de compra concluída.
     */
    suspend fun setPurchaseCompleteEnabled(enabled: Boolean) {
        context.notificationPreferencesDataStore.edit { preferences ->
            preferences[PURCHASE_COMPLETE_ENABLED_KEY] = enabled
        }
    }

    /**
     * Habilita ou desabilita a notificação de itens pendentes.
     */
    suspend fun setPendingItemsEnabled(enabled: Boolean) {
        context.notificationPreferencesDataStore.edit { preferences ->
            preferences[PENDING_ITEMS_ENABLED_KEY] = enabled
        }
    }

    /**
     * Define o número de dias para considerar itens como pendentes há muito tempo.
     */
    suspend fun setPendingItemsDays(days: Int) {
        require(days > 0) { "Dias deve ser maior que 0" }
        context.notificationPreferencesDataStore.edit { preferences ->
            preferences[PENDING_ITEMS_DAYS_KEY] = days
        }
    }

    /**
     * Obtém se a notificação de compra concluída está habilitada (suspending).
     */
    suspend fun isPurchaseCompleteEnabled(): Boolean {
        return purchaseCompleteEnabled.first()
    }

    /**
     * Obtém se a notificação de itens pendentes está habilitada (suspending).
     */
    suspend fun isPendingItemsEnabled(): Boolean {
        return pendingItemsEnabled.first()
    }

    /**
     * Obtém o número de dias configurado para itens pendentes (suspending).
     */
    suspend fun getPendingItemsDays(): Int {
        return pendingItemsDays.first()
    }
}

