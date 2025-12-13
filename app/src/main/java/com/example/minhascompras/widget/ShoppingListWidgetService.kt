package com.example.minhascompras.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.minhascompras.R
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.ItemCompra
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job

/**
 * RemoteViewsService para fornecer dados à ListView do widget.
 * 
 * Este serviço é responsável por criar a RemoteViewsFactory que popula
 * a lista de itens pendentes no widget.
 */
class ShoppingListWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val isSmallWidget = intent.getBooleanExtra("is_small_widget", false)
        return ShoppingListWidgetFactory(applicationContext, appWidgetId, isSmallWidget)
    }
}

/**
 * Factory que cria RemoteViews para cada item da lista.
 */
class ShoppingListWidgetFactory(
    private val context: android.content.Context,
    private val appWidgetId: Int,
    private val isSmallWidget: Boolean = false
) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<ItemCompra> = emptyList()

    override fun onCreate() {
        // Inicialização se necessário
    }

    // Adicionar um CoroutineScope persistente para a factory
    private val dataLoadJob = Job()
    private val dataLoadScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob(dataLoadJob) + kotlinx.coroutines.Dispatchers.IO
    )
    
    // Variável para controlar se há carregamento em andamento
    private var isLoading = false
    
    override fun onDataSetChanged() {
        android.util.Log.d("ShoppingListWidget", "onDataSetChanged chamado para widget $appWidgetId")
        
        // Evitar múltiplos carregamentos simultâneos
        if (isLoading) {
            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId já está carregando dados, ignorando chamada")
            return
        }
        
        isLoading = true
        
        // Buscar itens do banco de dados de forma assíncrona sem bloquear
        dataLoadScope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val itemDao = database.itemCompraDao()

                // Obter lista associada ao widget
                val prefs = context.getSharedPreferences(
                    ShoppingListWidgetProvider.WIDGET_PREFS_NAME,
                    android.content.Context.MODE_PRIVATE
                )
                val listId = prefs.getLong("widget_${appWidgetId}_list_id", -1L)

                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId listId: $listId")

                if (listId != -1L) {
                    // Buscar apenas itens pendentes (não comprados) de forma assíncrona
                    try {
                        val itensFlow = itemDao.getItensByListAndStatus(listId, false)
                        val resultado = itensFlow.first()
                        android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: encontrados ${resultado.size} itens no banco")
                        
                        // Atualizar items na thread principal
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            items = resultado
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId itens atualizados: ${items.size}")
                        }
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        android.util.Log.w("ShoppingListWidget", "Operação cancelada para widget $appWidgetId")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            items = emptyList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro ao buscar itens para widget $appWidgetId", e)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            items = emptyList()
                        }
                    }
                } else {
                    android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId não configurado")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        items = emptyList()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ShoppingListWidget", "Erro geral ao buscar itens para widget $appWidgetId", e)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    items = emptyList()
                }
            } finally {
                isLoading = false
            }
        }
    }

    override fun onDestroy() {
        android.util.Log.d("ShoppingListWidget", "onDestroy chamado para widget $appWidgetId")
        // Cancelar todas as coroutines em andamento
        dataLoadJob.cancel()
        // Limpeza se necessário
        items = emptyList()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position >= items.size) return null

        val item = items[position]
        // Usar layout de item apropriado baseado no tamanho do widget
        val itemLayoutResId = if (isSmallWidget) {
            R.layout.widget_item_small
        } else {
            R.layout.widget_item
        }
        val views = RemoteViews(context.packageName, itemLayoutResId)

        // Configurar nome do item
        views.setTextViewText(R.id.widget_item_name, item.nome)

        // Checkbox sempre desmarcado (apenas itens pendentes são exibidos)
        views.setBoolean(R.id.widget_item_checkbox, "setChecked", false)

        // Adicionar PendingIntent para marcar item como comprado ao tocar
        val clickIntent = Intent().apply {
            action = ShoppingListWidgetProvider.ACTION_ITEM_CLICKED
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(ShoppingListWidgetProvider.EXTRA_ITEM_ID, item.id)
            // Adicionar URI único para cada item
            data = android.net.Uri.parse("widget://item/${item.id}")
        }
        android.util.Log.d("ShoppingListWidget", "Criando PendingIntent para item ${item.id} (${item.nome}) no widget $appWidgetId")
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            item.id.toInt(), // Request code único por item
            clickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_item_name, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_item_checkbox, pendingIntent)
        android.util.Log.d("ShoppingListWidget", "PendingIntent configurado para item ${item.id} no widget $appWidgetId")

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        // Retornar null para usar o layout padrão de loading
        return null
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position < items.size) items[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}

