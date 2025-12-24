package com.example.minhascompras.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.minhascompras.MainActivity
import com.example.minhascompras.R

/**
 * Helper para criar e gerenciar notificações do app.
 * 
 * Centraliza a lógica de criação de notificações para diferentes tipos:
 * - Lembretes diários
 * - Conclusão de lista
 * - Itens pendentes
 */
object NotificationHelper {
    
    // IDs dos canais de notificação
    const val CHANNEL_ID_REMINDERS = "reminders_channel"
    const val CHANNEL_ID_COMPLETION = "completion_channel"
    const val CHANNEL_ID_PENDING_ITEMS = "pending_items_channel"
    
    // IDs das notificações
    const val NOTIFICATION_ID_DAILY_REMINDER = 100
    const val NOTIFICATION_ID_COMPLETION = 101
    const val NOTIFICATION_ID_PENDING_ITEMS = 102
    
    /**
     * Cria todos os canais de notificação necessários.
     * Deve ser chamado na inicialização do app.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para lembretes diários
            val remindersChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Lembretes Diários",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações de lembretes diários sobre suas compras"
                enableVibration(true)
            }
            
            // Canal para conclusão de lista
            val completionChannel = NotificationChannel(
                CHANNEL_ID_COMPLETION,
                "Conclusão de Lista",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações quando você completa uma lista de compras"
                enableVibration(true)
                enableLights(true)
            }
            
            // Canal para itens pendentes
            val pendingItemsChannel = NotificationChannel(
                CHANNEL_ID_PENDING_ITEMS,
                "Itens Pendentes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lembretes sobre itens pendentes há vários dias"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannels(
                listOf(remindersChannel, completionChannel, pendingItemsChannel)
            )
        }
    }
    
    /**
     * Cria e exibe uma notificação de lembrete diário.
     */
    fun showDailyReminderNotification(context: Context, itemCount: Int) {
        val title = "Lembrete de Compras"
        val message = when {
            itemCount == 0 -> "Que tal adicionar alguns itens à sua lista de compras?"
            itemCount == 1 -> "Você tem 1 item na sua lista de compras"
            else -> "Você tem $itemCount itens na sua lista de compras"
        }
        
        showNotification(
            context = context,
            channelId = CHANNEL_ID_REMINDERS,
            notificationId = NOTIFICATION_ID_DAILY_REMINDER,
            title = title,
            message = message,
            icon = R.drawable.ic_launcher_foreground
        )
    }
    
    /**
     * Cria e exibe uma notificação de conclusão de lista.
     */
    fun showCompletionNotification(context: Context) {
        val title = "🎉 Parabéns!"
        val message = "Você completou sua lista de compras! Que tal começar uma nova?"
        
        showNotification(
            context = context,
            channelId = CHANNEL_ID_COMPLETION,
            notificationId = NOTIFICATION_ID_COMPLETION,
            title = title,
            message = message,
            icon = R.drawable.ic_launcher_foreground
        )
    }
    
    /**
     * Cria e exibe uma notificação de itens pendentes.
     */
    fun showPendingItemsNotification(context: Context, itemCount: Int, daysThreshold: Int) {
        val title = "Itens Pendentes"
        val message = when {
            itemCount == 1 -> "Você tem 1 item pendente há mais de $daysThreshold dias"
            else -> "Você tem $itemCount itens pendentes há mais de $daysThreshold dias"
        }
        
        showNotification(
            context = context,
            channelId = CHANNEL_ID_PENDING_ITEMS,
            notificationId = NOTIFICATION_ID_PENDING_ITEMS,
            title = title,
            message = message,
            icon = R.drawable.ic_launcher_foreground
        )
    }
    
    /**
     * Função genérica para criar e exibir uma notificação.
     */
    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        icon: Int
    ) {
        // Criar intent para abrir o app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Construir notificação
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        // Exibir notificação
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}

