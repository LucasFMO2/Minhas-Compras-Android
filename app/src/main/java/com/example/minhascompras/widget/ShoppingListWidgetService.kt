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

    override fun onDataSetChanged() {
        // Buscar itens do banco de dados quando os dados mudam
        // Usar corrotina para evitar bloqueio da thread principal
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val itemDao = database.itemCompraDao()

                // Obter lista associada ao widget
                val prefs = context.getSharedPreferences(
                    ShoppingListWidgetProvider.WIDGET_PREFS_NAME,
                    android.content.Context.MODE_PRIVATE
                )
                val listId = prefs.getLong("widget_${appWidgetId}_list_id", -1L)

                if (listId != -1L) {
                    // Buscar apenas itens pendentes (não comprados)
                    items = itemDao.getItensByListAndStatus(listId, false).first()
                } else {
                    items = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("ShoppingListWidget", "Erro ao buscar itens", e)
                items = emptyList()
            }
        }
    }

    override fun onDestroy() {
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
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            item.id.toInt(), // Request code único por item
            clickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_item_name, pendingIntent)
        views.setOnClickPendingIntent(R.id.widget_item_checkbox, pendingIntent)

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

