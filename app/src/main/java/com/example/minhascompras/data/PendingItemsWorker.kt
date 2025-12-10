package com.example.minhascompras.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.minhascompras.MainActivity
import kotlinx.coroutines.flow.first

/**
 * Worker que verifica itens pendentes há mais de X dias e dispara notificação.
 * 
 * Este worker executa periodicamente e verifica itens que estão pendentes
 * há mais tempo que o configurado pelo usuário (padrão: 7 dias).
 */
class PendingItemsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("PendingItemsWorker", "Executando verificação de itens pendentes")
            
            val preferencesManager = NotificationPreferencesManager(applicationContext)
            val isEnabled = preferencesManager.isPendingItemsEnabled()
            
            if (!isEnabled) {
                Log.d("PendingItemsWorker", "Notificação de itens pendentes está desabilitada")
                return Result.success()
            }

            val daysThreshold = preferencesManager.getPendingItemsDays()
            val cutoffTime = System.currentTimeMillis() - (daysThreshold * 24 * 60 * 60 * 1000L)
            
            val database = AppDatabase.getDatabase(applicationContext)
            val oldPendingItems = database.itemCompraDao().getPendingItemsOlderThan(cutoffTime)
            
            if (oldPendingItems.isEmpty()) {
                Log.d("PendingItemsWorker", "Nenhum item pendente há mais de $daysThreshold dias")
                return Result.success()
            }

            // Agrupar por lista para melhor organização
            val itemsByList = oldPendingItems.groupBy { it.listId }
            val databaseLists = database.shoppingListDao().getAllLists().first()
            val listMap = databaseLists.associateBy { it.id }
            
            showNotification(oldPendingItems.size, itemsByList, listMap)
            
            Log.d("PendingItemsWorker", "Notificação de itens pendentes exibida: ${oldPendingItems.size} itens")
            
            // Reagendar para o próximo dia
            rescheduleNextCheck()
            
            Result.success()
            
        } catch (e: Exception) {
            Log.e("PendingItemsWorker", "Erro ao verificar itens pendentes", e)
            Result.success() // Retornar success para não marcar como falho
        }
    }

    private fun showNotification(
        totalItems: Int,
        itemsByList: Map<Long, List<ItemCompra>>,
        listMap: Map<Long, ShoppingList>
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Criar mensagem detalhada
        val listCount = itemsByList.size
        val message = when {
            totalItems == 1 -> "Você tem 1 item pendente há muito tempo"
            listCount == 1 -> {
                val listName = listMap[itemsByList.keys.first()]?.nome ?: "sua lista"
                "Você tem $totalItems itens pendentes há muito tempo em '$listName'"
            }
            else -> "Você tem $totalItems itens pendentes há muito tempo em $listCount listas"
        }

        val bigText = buildString {
            append(message)
            if (itemsByList.size <= 3) {
                append("\n\n")
                itemsByList.forEach { (listId, items) ->
                    val listName = listMap[listId]?.nome ?: "Lista $listId"
                    append("• $listName: ${items.size} ${if (items.size == 1) "item" else "itens"}\n")
                }
            }
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannelManager.CHANNEL_PENDING_ITEMS
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Itens Pendentes há Muito Tempo")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Reagenda a próxima verificação para o dia seguinte.
     */
    private suspend fun rescheduleNextCheck() {
        try {
            val scheduler = PendingItemsScheduler(applicationContext)
            scheduler.schedulePendingItemsCheck()
            Log.d("PendingItemsWorker", "Próxima verificação reagendada")
        } catch (e: Exception) {
            Log.e("PendingItemsWorker", "Erro ao reagendar próxima verificação", e)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 2003
        private const val PENDING_INTENT_REQUEST_CODE = 2003
    }
}

