package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.updatePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_preferences")

class UpdatePreferencesManager(private val context: Context) {
    companion object {
        private val LAST_CHECK_KEY = longPreferencesKey("last_update_check")
        private val LAST_SUCCESSFUL_CHECK_KEY = longPreferencesKey("last_successful_check")
        private val LAST_VERSION_CHECKED_KEY = intPreferencesKey("last_version_checked")
        private val LAST_APP_USE_KEY = longPreferencesKey("last_app_use")
        private val CONSECUTIVE_FAILURES_KEY = intPreferencesKey("consecutive_failures")
        private val UPDATE_AVAILABLE_KEY = booleanPreferencesKey("update_available")
        
        // Intervalos adaptativos (em milissegundos)
        private const val MIN_CHECK_INTERVAL_MS = 30 * 60 * 1000L // 30 minutos (quando há atualização disponível)
        private const val NORMAL_CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L // 6 horas (verificação normal)
        private const val MAX_CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 horas (após falhas)
        private const val APP_USE_CHECK_INTERVAL_MS = 2 * 60 * 60 * 1000L // Verificar após 2h de uso do app
    }

    val lastCheckTime: Flow<Long> = context.updatePreferencesDataStore.data.map { preferences ->
        preferences[LAST_CHECK_KEY] ?: 0L
    }

    suspend fun setLastCheckTime(time: Long = System.currentTimeMillis()) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[LAST_CHECK_KEY] = time
        }
    }
    
    suspend fun setLastSuccessfulCheck(time: Long = System.currentTimeMillis()) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[LAST_SUCCESSFUL_CHECK_KEY] = time
            preferences[CONSECUTIVE_FAILURES_KEY] = 0
        }
    }
    
    suspend fun recordCheckFailure() {
        context.updatePreferencesDataStore.edit { preferences ->
            val failures = (preferences[CONSECUTIVE_FAILURES_KEY] ?: 0) + 1
            preferences[CONSECUTIVE_FAILURES_KEY] = failures
        }
    }
    
    suspend fun setLastVersionChecked(versionCode: Int) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[LAST_VERSION_CHECKED_KEY] = versionCode
        }
    }
    
    suspend fun getLastVersionChecked(): Int {
        return context.updatePreferencesDataStore.data
            .map { it[LAST_VERSION_CHECKED_KEY] ?: 0 }
            .first()
    }
    
    suspend fun setUpdateAvailable(available: Boolean) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[UPDATE_AVAILABLE_KEY] = available
        }
    }
    
    suspend fun isUpdateAvailable(): Boolean {
        return context.updatePreferencesDataStore.data
            .map { it[UPDATE_AVAILABLE_KEY] ?: false }
            .first()
    }
    
    suspend fun updateLastAppUse() {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[LAST_APP_USE_KEY] = System.currentTimeMillis()
        }
    }
    
    suspend fun getLastAppUse(): Long {
        return context.updatePreferencesDataStore.data
            .map { it[LAST_APP_USE_KEY] ?: 0L }
            .first()
    }
    
    suspend fun getConsecutiveFailures(): Int {
        return context.updatePreferencesDataStore.data
            .map { it[CONSECUTIVE_FAILURES_KEY] ?: 0 }
            .first()
    }

    /**
     * Verifica se deve fazer verificação de atualização com lógica inteligente:
     * - Intervalos adaptativos baseados em sucesso/falha
     * - Considera se há atualização disponível
     * - Considera uso do app
     * - Considera falhas consecutivas
     */
    suspend fun shouldCheckForUpdate(force: Boolean = false): Boolean {
        if (force) return true
        
        val lastCheckValue = context.updatePreferencesDataStore.data
            .map { it[LAST_CHECK_KEY] ?: 0L }
            .first()
        val now = System.currentTimeMillis()
        val timeSinceLastCheck = now - lastCheckValue
        
        // Se nunca verificou, deve verificar
        if (lastCheckValue == 0L) return true
        
        // Verificar se há atualização disponível (verificar mais frequentemente)
        val hasUpdate = isUpdateAvailable()
        if (hasUpdate && timeSinceLastCheck >= MIN_CHECK_INTERVAL_MS) {
            return true
        }
        
        // Verificar falhas consecutivas para ajustar intervalo
        val failures = getConsecutiveFailures()
        val interval = when {
            failures >= 3 -> MAX_CHECK_INTERVAL_MS // Após 3 falhas, verificar apenas 1x por dia
            failures >= 1 -> NORMAL_CHECK_INTERVAL_MS * 2 // Após 1 falha, verificar a cada 12h
            hasUpdate -> MIN_CHECK_INTERVAL_MS // Se há atualização, verificar a cada 30min
            else -> NORMAL_CHECK_INTERVAL_MS // Normal: a cada 6h
        }
        
        // Verificar se passou tempo suficiente
        if (timeSinceLastCheck >= interval) {
            return true
        }
        
        // Verificar se app foi usado recentemente e passou tempo desde última verificação
        val lastAppUse = getLastAppUse()
        if (lastAppUse > 0 && (now - lastAppUse) < APP_USE_CHECK_INTERVAL_MS) {
            // Se app foi usado recentemente e passou pelo menos 2h desde última verificação
            if (timeSinceLastCheck >= APP_USE_CHECK_INTERVAL_MS) {
                return true
            }
        }
        
        return false
    }
}

