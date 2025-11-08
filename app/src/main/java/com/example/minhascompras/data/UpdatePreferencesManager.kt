package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.updatePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_preferences")

class UpdatePreferencesManager(private val context: Context) {
    companion object {
        private val LAST_CHECK_KEY = longPreferencesKey("last_update_check")
        private const val CHECK_INTERVAL_MS = 1 * 60 * 60 * 1000L // 1 hora (para testes, pode ser ajustado)
    }

    val lastCheckTime: Flow<Long> = context.updatePreferencesDataStore.data.map { preferences ->
        preferences[LAST_CHECK_KEY] ?: 0L
    }

    suspend fun setLastCheckTime(time: Long = System.currentTimeMillis()) {
        context.updatePreferencesDataStore.edit { preferences ->
            preferences[LAST_CHECK_KEY] = time
        }
    }

    suspend fun shouldCheckForUpdate(): Boolean {
        val lastCheckValue = context.updatePreferencesDataStore.data
            .map { it[LAST_CHECK_KEY] ?: 0L }
            .first()
        val now = System.currentTimeMillis()
        return (now - lastCheckValue) >= CHECK_INTERVAL_MS
    }
}

