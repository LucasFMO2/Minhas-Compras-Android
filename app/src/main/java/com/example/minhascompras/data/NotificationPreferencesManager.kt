package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Gerenciador de preferências de notificações inteligentes.
 * 
 * Gerencia as configurações do usuário para diferentes tipos de notificações:
 * - Lembretes diários
 * - Notificações de conclusão de lista
 * - Lembretes de itens pendentes
 */
class NotificationPreferencesManager(private val context: Context) {
    
    companion object {
        private const val DATASTORE_NAME = "notification_preferences"
        
        // Chaves para preferências de lembrete diário
        private val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        private val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        private val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        
        // Chaves para preferências de conclusão de lista
        private val COMPLETION_NOTIFICATION_ENABLED = booleanPreferencesKey("completion_notification_enabled")
        
        // Chaves para preferências de itens pendentes
        private val PENDING_ITEMS_NOTIFICATION_ENABLED = booleanPreferencesKey("pending_items_notification_enabled")
        private val PENDING_ITEMS_DAYS_THRESHOLD = intPreferencesKey("pending_items_days_threshold")
        
        // Valores padrão
        const val DEFAULT_DAILY_REMINDER_ENABLED = false
        const val DEFAULT_DAILY_REMINDER_HOUR = 9 // 9:00 AM
        const val DEFAULT_DAILY_REMINDER_MINUTE = 0
        const val DEFAULT_COMPLETION_NOTIFICATION_ENABLED = true
        const val DEFAULT_PENDING_ITEMS_NOTIFICATION_ENABLED = true
        const val DEFAULT_PENDING_ITEMS_DAYS_THRESHOLD = 7 // 7 dias
        
        private val Context.notificationPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
            name = DATASTORE_NAME
        )
    }
    
    // ===== Lembrete Diário =====
    
    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.notificationPrefsDataStore.edit { preferences ->
            preferences[DAILY_REMINDER_ENABLED] = enabled
        }
    }
    
    fun isDailyReminderEnabled(): Flow<Boolean> {
        return context.notificationPrefsDataStore.data.map { preferences ->
            preferences[DAILY_REMINDER_ENABLED] ?: DEFAULT_DAILY_REMINDER_ENABLED
        }
    }
    
    suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        context.notificationPrefsDataStore.edit { preferences ->
            preferences[DAILY_REMINDER_HOUR] = hour
            preferences[DAILY_REMINDER_MINUTE] = minute
        }
    }
    
    fun getDailyReminderHour(): Flow<Int> {
        return context.notificationPrefsDataStore.data.map { preferences ->
            preferences[DAILY_REMINDER_HOUR] ?: DEFAULT_DAILY_REMINDER_HOUR
        }
    }
    
    fun getDailyReminderMinute(): Flow<Int> {
        return context.notificationPrefsDataStore.data.map { preferences ->
            preferences[DAILY_REMINDER_MINUTE] ?: DEFAULT_DAILY_REMINDER_MINUTE
        }
    }
    
    // ===== Notificação de Conclusão =====
    
    suspend fun setCompletionNotificationEnabled(enabled: Boolean) {
        context.notificationPrefsDataStore.edit { preferences ->
            preferences[COMPLETION_NOTIFICATION_ENABLED] = enabled
        }
    }
    
    fun isCompletionNotificationEnabled(): Flow<Boolean> {
        return context.notificationPrefsDataStore.data.map { preferences ->
            preferences[COMPLETION_NOTIFICATION_ENABLED] ?: DEFAULT_COMPLETION_NOTIFICATION_ENABLED
        }
    }
    
    // ===== Notificação de Itens Pendentes =====
    
    suspend fun setPendingItemsNotificationEnabled(enabled: Boolean) {
        context.notificationPrefsDataStore.edit { preferences ->
            preferences[PENDING_ITEMS_NOTIFICATION_ENABLED] = enabled
        }
    }
    
    fun isPendingItemsNotificationEnabled(): Flow<Boolean> {
        return context.notificationPrefsDataStore.data.map { preferences ->
            preferences[PENDING_ITEMS_NOTIFICATION_ENABLED] ?: DEFAULT_PENDING_ITEMS_NOTIFICATION_ENABLED
        }
    }
    
    suspend fun setPendingItemsDaysThreshold(days: Int) {
        context.notificationPrefsDataStore.edit { preferences ->
            preferences[PENDING_ITEMS_DAYS_THRESHOLD] = days
        }
    }
    
    fun getPendingItemsDaysThreshold(): Flow<Int> {
        return context.notificationPrefsDataStore.data.map { preferences ->
            preferences[PENDING_ITEMS_DAYS_THRESHOLD] ?: DEFAULT_PENDING_ITEMS_DAYS_THRESHOLD
        }
    }
}

