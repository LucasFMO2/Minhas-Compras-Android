package com.example.minhascompras.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Gerenciador centralizado para criação de canais de notificação.
 * 
 * Este manager cria todos os canais de notificação necessários para o app,
 * incluindo canais para lembretes diários, notificações de compra concluída
 * e lembretes de itens pendentes.
 */
object NotificationChannelManager {
    
    // IDs dos canais de notificação
    const val CHANNEL_DAILY_REMINDER = "daily_reminder_channel"
    const val CHANNEL_PURCHASE_COMPLETE = "purchase_complete_channel"
    const val CHANNEL_PENDING_ITEMS = "pending_items_channel"
    
    // Nomes dos canais (exibidos nas configurações do sistema)
    private const val CHANNEL_DAILY_REMINDER_NAME = "Lembrete Diário"
    private const val CHANNEL_PURCHASE_COMPLETE_NAME = "Compra Concluída"
    private const val CHANNEL_PENDING_ITEMS_NAME = "Itens Pendentes"
    
    // Descrições dos canais
    private const val CHANNEL_DAILY_REMINDER_DESC = "Notificações de lembrete diário para fazer compras"
    private const val CHANNEL_PURCHASE_COMPLETE_DESC = "Notificações quando todos os itens da lista forem marcados como comprados"
    private const val CHANNEL_PENDING_ITEMS_DESC = "Lembretes sobre itens que estão pendentes há muito tempo"
    
    /**
     * Cria todos os canais de notificação necessários para o app.
     * Deve ser chamado durante a inicialização da Application.
     * 
     * @param context Contexto da aplicação
     */
    fun createAllChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            createDailyReminderChannel(notificationManager)
            createPurchaseCompleteChannel(notificationManager)
            createPendingItemsChannel(notificationManager)
            
            Log.d("NotificationChannelManager", "Todos os canais de notificação foram criados")
        } else {
            Log.d("NotificationChannelManager", "Canais de notificação não são necessários (Android < 8.0)")
        }
    }
    
    /**
     * Cria o canal para lembretes diários.
     * Usa IMPORTANCE_DEFAULT para garantir que as notificações sejam exibidas.
     */
    private fun createDailyReminderChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                CHANNEL_DAILY_REMINDER_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DAILY_REMINDER_DESC
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationChannelManager", "Canal 'Lembrete Diário' criado")
        }
    }
    
    /**
     * Cria o canal para notificações de compra concluída.
     * Usa IMPORTANCE_DEFAULT para garantir visibilidade.
     */
    private fun createPurchaseCompleteChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_PURCHASE_COMPLETE,
                CHANNEL_PURCHASE_COMPLETE_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_PURCHASE_COMPLETE_DESC
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationChannelManager", "Canal 'Compra Concluída' criado")
        }
    }
    
    /**
     * Cria o canal para lembretes de itens pendentes.
     * Usa IMPORTANCE_LOW para ser menos intrusivo, já que pode ser frequente.
     */
    private fun createPendingItemsChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_PENDING_ITEMS,
                CHANNEL_PENDING_ITEMS_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_PENDING_ITEMS_DESC
                enableVibration(false)
                enableLights(false)
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationChannelManager", "Canal 'Itens Pendentes' criado")
        }
    }
}

