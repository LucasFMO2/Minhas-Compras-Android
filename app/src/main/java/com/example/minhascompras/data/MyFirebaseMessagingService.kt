package com.example.minhascompras.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.minhascompras.MainActivity
import com.example.minhascompras.MinhasComprasApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = MinhasComprasApplication.FCM_CHANNEL_ID
        private const val NOTIFICATION_ID = 1000
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Novo token FCM: $token")
        // TODO: Enviar token para o backend quando implementado
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Mensagem recebida de: ${remoteMessage.from}")
        
        // Verificar se a mensagem contém dados
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Dados da mensagem: ${remoteMessage.data}")
        }
        
        // Verificar se a mensagem contém notificação
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Título: ${notification.title}")
            Log.d(TAG, "Corpo: ${notification.body}")
            
            // Exibir notificação local
            showNotification(
                title = notification.title ?: "Nova notificação",
                body = notification.body ?: ""
            )
        }
    }
    
    private fun showNotification(title: String, body: String) {
        // O canal de notificação já é criado na Application class
        
        // Intent para abrir a MainActivity quando a notificação for tocada
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir a notificação
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Exibir a notificação
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}

