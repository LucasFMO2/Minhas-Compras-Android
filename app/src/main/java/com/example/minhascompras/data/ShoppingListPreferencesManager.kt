package com.example.minhascompras.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.minhascompras.utils.DebugLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.shoppingListDataStore: DataStore<Preferences> by preferencesDataStore(name = "shopping_list_preferences")

class ShoppingListPreferencesManager(private val context: Context) {
    companion object {
        private val ACTIVE_LIST_ID_KEY = longPreferencesKey("active_list_id")
    }

    val activeListId: Flow<Long?> = context.shoppingListDataStore.data.map { preferences ->
        val listId = preferences[ACTIVE_LIST_ID_KEY]
        
        // #region agent log
        DebugLogger.log(
            location = "ShoppingListPreferencesManager.kt:activeListId",
            message = "activeListId read from DataStore",
            data = mapOf("listId" to listId),
            hypothesisId = "A"
        )
        // #endregion
        
        listId
    }

    suspend fun setActiveListId(listId: Long) {
        // #region agent log
        DebugLogger.log(
            location = "ShoppingListPreferencesManager.kt:setActiveListId",
            message = "setActiveListId called",
            data = mapOf("listId" to listId),
            hypothesisId = "A"
        )
        // #endregion
        
        context.shoppingListDataStore.edit { preferences ->
            preferences[ACTIVE_LIST_ID_KEY] = listId
        }
        
        // #region agent log
        DebugLogger.log(
            location = "ShoppingListPreferencesManager.kt:setActiveListId",
            message = "activeListId saved to DataStore",
            data = mapOf("listId" to listId),
            hypothesisId = "A"
        )
        // #endregion
    }

    suspend fun clearActiveListId() {
        context.shoppingListDataStore.edit { preferences ->
            preferences.remove(ACTIVE_LIST_ID_KEY)
        }
    }
}

