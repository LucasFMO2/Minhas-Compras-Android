package com.example.minhascompras

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.example.minhascompras.notifications.NotificationHelper
import java.lang.Thread.UncaughtExceptionHandler

class MinhasComprasApplication : Application() {
    
    companion object {
        const val CHANNEL_ID = "fcm_default_channel"
        const val CHANNEL_NAME = "Notificações FCM"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar handler global para exceções não capturadas
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("MinhasComprasApp", "Exceção não capturada no thread: ${thread.name}", exception)
            exception.printStackTrace()
            
            // Chamar o handler padrão para manter o comportamento normal
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        // Criar canal de notificação para Android 8.0+
        createNotificationChannel()
        
        // Criar canais de notificação inteligentes
        NotificationHelper.createNotificationChannels(this)
        
        Log.d("MinhasComprasApp", "Application inicializada com sucesso")
    }
    
    /**
     * Cria o canal de notificação para Firebase Cloud Messaging.
     * 
     * Este canal deve ser criado antes de qualquer notificação ser exibida,
     * por isso é criado no onCreate() da Application.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificações do Firebase Cloud Messaging"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("MinhasComprasApp", "Canal de notificação criado: $CHANNEL_NAME")
        }
    }
}

