package com.example.minhascompras.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.minhascompras.R
import com.example.minhascompras.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/**
 * Widget Provider para exibir lista de compras na tela inicial do Android.
 * 
 * Este widget permite acesso rápido às funcionalidades principais sem abrir o app.
 */
class ShoppingListWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Atualizar todos os widgets
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Limpar preferências quando widget é removido
        for (appWidgetId in appWidgetIds) {
            val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove("widget_${appWidgetId}_list_id").apply()
        }
    }

    override fun onEnabled(context: Context) {
        // Widget habilitado
    }

    override fun onDisabled(context: Context) {
        // Todos os widgets foram removidos
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_ITEM_CLICKED -> {
                // Marcar item como comprado
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && itemId != -1L) {
                    markItemAsPurchased(context, appWidgetId, itemId)
                }
            }
            ACTION_ADD_ITEM -> {
                // Abrir app para adicionar item
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                val listId = intent.getLongExtra(EXTRA_LIST_ID, -1L)

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && listId != -1L) {
                    openAppToAddItem(context, listId)
                }
            }
            ACTION_UPDATE_WIDGET -> {
                // Atualizar todos os widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, ShoppingListWidgetProvider::class.java)
                )
                for (widgetId in widgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
            }
        }
    }

    private fun markItemAsPurchased(context: Context, appWidgetId: Int, itemId: Long) {
        // Atualizar item no banco de dados de forma assíncrona
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val itemDao = database.itemCompraDao()

                // Buscar item atual de forma assíncrona
                val allItems = itemDao.getAllItens().first()
                val item = allItems.find { it.id == itemId }

                if (item != null && !item.comprado) {
                    // Marcar como comprado
                    val updatedItem = item.copy(comprado = true)
                    itemDao.update(updatedItem)

                    // Atualizar widget
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                android.util.Log.e("ShoppingListWidget", "Erro ao marcar item como comprado", e)
            }
        }
    }

    private fun openAppToAddItem(context: Context, listId: Long) {
        val intent = Intent(context, com.example.minhascompras.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_add_dialog", true)
            putExtra("list_id", listId)
        }
        context.startActivity(intent)
    }

    companion object {
        const val WIDGET_PREFS_NAME = "ShoppingListWidgetPrefs"
        const val ACTION_ITEM_CLICKED = "com.example.minhascompras.widget.ACTION_ITEM_CLICKED"
        const val ACTION_ADD_ITEM = "com.example.minhascompras.widget.ACTION_ADD_ITEM"
        const val ACTION_UPDATE_WIDGET = "com.example.minhascompras.widget.ACTION_UPDATE_WIDGET"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_LIST_ID = "extra_list_id"

        /**
         * Atualiza todos os widgets instalados.
         * Deve ser chamado quando os dados mudam no app.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(
                    context,
                    ShoppingListWidgetProvider::class.java
                )
            )
            if (widgetIds.isNotEmpty()) {
                for (widgetId in widgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId)
                }
            }
        }

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            android.util.Log.d("ShoppingListWidget", "Atualizando widget $appWidgetId")
            
            // Determinar qual layout usar baseado no tamanho do widget
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            
            // Calcular tamanho em células (cada célula = ~70dp)
            val widthCells = (minWidth + 30) / 70
            val heightCells = (minHeight + 30) / 70
            
            // Escolher layout baseado no tamanho
            val layoutResId = when {
                widthCells <= 2 && heightCells <= 1 -> R.layout.widget_layout_small // 2x1 ou menor
                widthCells >= 4 && heightCells >= 4 -> R.layout.widget_layout_large // 4x4 ou maior
                else -> R.layout.widget_layout_medium // 4x2 (padrão)
            }
            
            // Criar RemoteViews com layout apropriado
            val views = RemoteViews(context.packageName, layoutResId)

            // Obter lista associada ao widget
            val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
            val listId = prefs.getLong("widget_${appWidgetId}_list_id", -1L)
            
            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId listId: $listId")

            if (listId != -1L) {
                // Usar CoroutineScope para operações assíncronas
                val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                widgetScope.launch {
                    try {
                        val database = AppDatabase.getDatabase(context)
                        val itemDao = database.itemCompraDao()
                        val shoppingListDao = database.shoppingListDao()

                        // Verificar se a lista ainda existe
                        val list = shoppingListDao.getListByIdSync(listId)
                        if (list == null) {
                            android.util.Log.w("ShoppingListWidget", "Lista $listId não encontrada")
                            // Lista não existe mais, mostrar mensagem para reconfigurar
                            views.setTextViewText(R.id.widget_list_name, "Lista removida")
                            views.setTextViewText(R.id.widget_progress_text, "Reconfigure o widget")
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                            return@launch
                        }

                        val listName = list.nome
                        android.util.Log.d("ShoppingListWidget", "Lista nome: $listName")

                        // Buscar itens pendentes
                        val pendingItems = itemDao.getItensByListAndStatus(listId, false).first()
                        android.util.Log.d("ShoppingListWidget", "Itens pendentes: ${pendingItems.size}")

                        // Calcular progresso
                        val totalItems = itemDao.getItensByList(listId).first()
                        val completedCount = totalItems.count { it.comprado }
                        val progressText = "$completedCount/${totalItems.size} itens"

                        // Atualizar views com dados
                        views.setTextViewText(R.id.widget_list_name, listName)
                        views.setTextViewText(R.id.widget_progress_text, progressText)

                        // Conectar RemoteViewsService ao ListView
                        val serviceIntent = Intent(context, ShoppingListWidgetService::class.java)
                        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        serviceIntent.putExtra("is_small_widget", layoutResId == R.layout.widget_layout_small)
                        serviceIntent.data = android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))
                        views.setRemoteAdapter(R.id.widget_items_list, serviceIntent)

                        // Configurar empty view (será exibido quando não houver itens)
                        views.setEmptyView(R.id.widget_items_list, R.id.widget_empty_view)

                        // Configurar PendingIntent para o botão "Adicionar Item"
                        val addItemIntent = Intent(context, ShoppingListWidgetProvider::class.java).apply {
                            action = ACTION_ADD_ITEM
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            putExtra(EXTRA_LIST_ID, listId)
                        }
                        val addItemPendingIntent = android.app.PendingIntent.getBroadcast(
                            context,
                            appWidgetId,
                            addItemIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_add_item_button, addItemPendingIntent)

                        // Configurar PendingIntent para abrir o app ao tocar no cabeçalho
                        val openAppIntent = Intent(context, com.example.minhascompras.MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("list_id", listId)
                        }
                        val openAppPendingIntent = android.app.PendingIntent.getActivity(
                            context,
                            appWidgetId + 1000, // Request code diferente
                            openAppIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_list_name, openAppPendingIntent)

                        // Notificar que os dados mudaram para atualizar a lista PRIMEIRO
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_items_list)
                        
                        // Atualizar widget DEPOIS de notificar sobre mudança dos dados
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro ao atualizar widget $appWidgetId", e)
                        // Em caso de erro, mostrar mensagem genérica
                        views.setTextViewText(R.id.widget_list_name, "Erro ao carregar")
                        views.setTextViewText(R.id.widget_progress_text, "Toque para abrir app")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } else {
                // Widget não configurado ainda
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId não configurado")
                views.setTextViewText(R.id.widget_list_name, "Configurar Widget")
                views.setTextViewText(R.id.widget_progress_text, "Toque para configurar")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

