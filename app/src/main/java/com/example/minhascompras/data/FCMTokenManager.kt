package com.example.minhascompras.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Gerenciador de tokens FCM (Firebase Cloud Messaging).
 * 
 * Responsável por armazenar e recuperar o token FCM do dispositivo.
 * O token é salvo usando DataStore para persistência.
 */
class FCMTokenManager(private val context: Context) {
    
    companion object {
        private const val TAG = "FCMTokenManager"
        private const val DATASTORE_NAME = "fcm_preferences"
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
        private val FCM_TOKEN_TIMESTAMP_KEY = stringPreferencesKey("fcm_token_timestamp")
        
        private val Context.fcmDataStore: DataStore<Preferences> by preferencesDataStore(
            name = DATASTORE_NAME
        )
    }
    
    /**
     * Salva o token FCM no DataStore.
     * 
     * @param token Token FCM a ser salvo
     */
    suspend fun saveToken(token: String) {
        try {
            context.fcmDataStore.edit { preferences ->
                preferences[FCM_TOKEN_KEY] = token
                preferences[FCM_TOKEN_TIMESTAMP_KEY] = System.currentTimeMillis().toString()
            }
            Log.d(TAG, "Token FCM salvo com sucesso: ${token.take(20)}...")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar token FCM", e)
        }
    }
    
    /**
     * Recupera o token FCM salvo no DataStore.
     * 
     * @return Flow com o token FCM ou null se não houver token salvo
     */
    fun getToken(): Flow<String?> {
        return context.fcmDataStore.data.map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }
    }
    
    /**
     * Recupera o token FCM de forma síncrona (para uso em Workers).
     * 
     * @return Token FCM ou null se não houver token salvo
     */
    suspend fun getTokenSync(): String? {
        return try {
            var token: String? = null
            context.fcmDataStore.data.collect { preferences ->
                token = preferences[FCM_TOKEN_KEY]
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao recuperar token FCM", e)
            null
        }
    }
    
    /**
     * Verifica se há um token FCM salvo.
     * 
     * @return Flow com true se houver token, false caso contrário
     */
    fun hasToken(): Flow<Boolean> {
        return getToken().map { it != null }
    }
    
    /**
     * Remove o token FCM salvo (útil para logout ou reset).
     */
    suspend fun clearToken() {
        try {
            context.fcmDataStore.edit { preferences ->
                preferences.remove(FCM_TOKEN_KEY)
                preferences.remove(FCM_TOKEN_TIMESTAMP_KEY)
            }
            Log.d(TAG, "Token FCM removido com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao remover token FCM", e)
        }
    }
}

