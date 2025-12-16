package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.shoppingListDataStore: DataStore<Preferences> by preferencesDataStore(name = "shopping_list_preferences")

class ShoppingListPreferencesManager(private val context: Context) {
    companion object {
        private val ACTIVE_LIST_ID_KEY = longPreferencesKey("active_list_id")
    }

    val activeListId: Flow<Long?> = context.shoppingListDataStore.data.map { preferences ->
        preferences[ACTIVE_LIST_ID_KEY]
    }

    suspend fun setActiveListId(listId: Long?) {
        context.shoppingListDataStore.edit { preferences ->
            if (listId != null) {
                preferences[ACTIVE_LIST_ID_KEY] = listId
            } else {
                preferences.remove(ACTIVE_LIST_ID_KEY)
            }
        }
    }
}


