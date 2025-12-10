package com.example.minhascompras.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.minhascompras.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Gerencia notificações de compra concluída.
 * 
 * Detecta quando todos os itens de uma lista são marcados como comprados
 * e dispara uma notificação uma única vez por conclusão.
 */
class PurchaseCompleteNotifier(private val context: Context) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    companion object {
        private const val NOTIFICATION_ID = 2002
        private const val PENDING_INTENT_REQUEST_CODE = 2002
    }

    /**
     * Verifica se todos os itens de uma lista estão comprados e dispara notificação se necessário.
     * 
     * @param listId ID da lista a verificar
     * @param listName Nome da lista (para exibir na notificação)
     */
    suspend fun checkAndNotifyIfComplete(listId: Long, listName: String) {
        try {
            val preferencesManager = NotificationPreferencesManager(context)
            val isEnabled = preferencesManager.isPurchaseCompleteEnabled()
            
            if (!isEnabled) {
                Log.d("PurchaseCompleteNotifier", "Notificação de compra concluída está desabilitada")
                return
            }

            val database = AppDatabase.getDatabase(context)
            val totalItems = database.itemCompraDao().getItensByList(listId).first().size
            val pendingItems = database.itemCompraDao().countPendingItemsByList(listId)
            
            // Se não há itens na lista, não notificar
            if (totalItems == 0) {
                return
            }

            // Se todos os itens estão comprados (nenhum pendente)
            if (pendingItems == 0 && totalItems > 0) {
                showNotification(listName, totalItems)
                Log.d("PurchaseCompleteNotifier", "Notificação de compra concluída exibida para lista: $listName")
            }
        } catch (e: Exception) {
            Log.e("PurchaseCompleteNotifier", "Erro ao verificar compra concluída", e)
        }
    }

    /**
     * Versão assíncrona que não bloqueia a thread chamadora.
     */
    fun checkAndNotifyIfCompleteAsync(listId: Long, listName: String) {
        scope.launch {
            checkAndNotifyIfComplete(listId, listName)
        }
    }

    private fun showNotification(listName: String, itemCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = when {
            itemCount == 1 -> "Parabéns! Você completou sua lista '$listName' com 1 item!"
            else -> "Parabéns! Você completou sua lista '$listName' com $itemCount itens!"
        }

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannelManager.CHANNEL_PURCHASE_COMPLETE
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lista de Compras Concluída!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

