package com.example.minhascompras

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.minhascompras.MinhasComprasApplication.Companion.CHANNEL_ID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Serviço de mensageria do Firebase Cloud Messaging (FCM).
 * 
 * Este serviço é responsável por:
 * - Receber tokens de registro FCM
 * - Processar mensagens recebidas do Firebase
 * 
 * O token é crucial para identificar o dispositivo e enviar notificações push.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessaging"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MyFirebaseMessagingService inicializado")
    }

    /**
     * Chamado quando um novo token FCM é gerado.
     * 
     * Este método é invocado:
     * - Na primeira vez que o app é instalado
     * - Quando o token é atualizado pelo Firebase
     * - Quando o app é reinstalado
     * 
     * @param token O novo token de registro FCM do dispositivo
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Novo token FCM recebido: $token")
        
        // TODO: Em uma implementação futura, este token deve ser enviado para o backend
        // para permitir o envio de notificações push para este dispositivo específico.
    }

    /**
     * Chamado quando uma mensagem é recebida.
     * 
     * Este método é invocado quando o app está em primeiro plano.
     * Quando o app está em segundo plano ou fechado, o Firebase
     * exibe automaticamente a notificação.
     * 
     * @param remoteMessage A mensagem recebida do Firebase
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensagem recebida do Firebase")
        
        // Verifica se a mensagem contém dados de notificação
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Notificação"
            val body = notification.body ?: ""
            
            Log.d(TAG, "Título da notificação: $title")
            Log.d(TAG, "Corpo da notificação: $body")
            
            // Exibe a notificação local
            showNotification(title, body)
        } ?: run {
            Log.d(TAG, "Mensagem recebida sem dados de notificação")
        }
    }

    /**
     * Exibe uma notificação local usando NotificationManagerCompat.
     * 
     * Esta função usa o canal de notificação criado na MinhasComprasApplication
     * e configura um PendingIntent para abrir a MainActivity ao tocar na notificação.
     * 
     * @param title Título da notificação
     * @param body Corpo da notificação
     */
    private fun showNotification(title: String, body: String) {
        // Cria um Intent para abrir a MainActivity quando a notificação for clicada
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Constrói a notificação usando NotificationCompat.Builder
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ícone do app
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // Remove a notificação ao tocar
            .setContentIntent(pendingIntent) // Abre MainActivity ao tocar
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Som, vibração, etc.
        
        // Exibe a notificação usando NotificationManagerCompat
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        
        Log.d(TAG, "Notificação exibida: $title - $body")
    }
}

