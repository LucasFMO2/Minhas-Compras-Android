package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    companion object {
        private val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
    }

    val sortOrder: Flow<SortOrder> = context.dataStore.data.map { preferences ->
        val sortOrderString = preferences[SORT_ORDER_KEY] ?: SortOrder.BY_DATE_DESC.name
        try {
            SortOrder.valueOf(sortOrderString)
        } catch (e: IllegalArgumentException) {
            SortOrder.BY_DATE_DESC
        }
    }

    suspend fun setSortOrder(sortOrder: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.name
        }
    }
}

