package com.example.minhascompras

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import java.lang.Thread.UncaughtExceptionHandler

class MinhasComprasApplication : Application() {
    
    companion object {
        const val FCM_CHANNEL_ID = "fcm_notifications"
        const val FCM_CHANNEL_NAME = "Notificações do App"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Criar canal de notificação para FCM (necessário para Android 8.0+)
        createNotificationChannel()
        
        // Configurar handler global para exceções não capturadas
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("MinhasComprasApp", "Exceção não capturada no thread: ${thread.name}", exception)
            exception.printStackTrace()
            
            // Chamar o handler padrão para manter o comportamento normal
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        Log.d("MinhasComprasApp", "Application inicializada com sucesso")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                FCM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificações push do Firebase Cloud Messaging"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("MinhasComprasApp", "Canal de notificação FCM criado")
        }
    }
}

