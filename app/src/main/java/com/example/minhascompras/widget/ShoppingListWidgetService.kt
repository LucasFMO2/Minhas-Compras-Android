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
        android.util.Log.d("ShoppingListWidget", "Thread atual: ${Thread.currentThread().name}")
        
        // Evitar múltiplos carregamentos simultâneos
        if (isLoading) {
            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId já está carregando dados, ignorando chamada")
            return
        }
        
        isLoading = true
        
        // Carregar dados de forma síncrona para garantir disponibilidade imediata
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
                // Buscar apenas itens pendentes (não comprados) de forma SÍNCRONA
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: buscando itens pendentes da lista $listId de forma síncrona")
                
                // Usar runBlocking para garantir que os dados sejam carregados sincronamente
                val resultado = runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        itemDao.getItensByListAndStatus(listId, false).first()
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro ao buscar itens síncrona para widget $appWidgetId", e)
                        emptyList()
                    }
                }
                
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: encontrados ${resultado.size} itens no banco")
                
                // Log detalhado dos itens encontrados
                resultado.forEachIndexed { index, item ->
                    android.util.Log.d("ShoppingListWidget", "Item $index: ${item.nome} (ID: ${item.id}, Comprado: ${item.comprado})")
                }
                
                // Atualizar items imediatamente (já estamos na thread correta)
                val oldSize = items.size
                items = resultado
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId itens atualizados: ${items.size} (antigo: $oldSize)")
                
                // Verificar se getCount retornará o valor correto
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId getCount() retornará: ${getCount()}")
                
                // Iniciar carregamento assíncrono em background para atualizações futuras
                dataLoadScope.launch {
                    try {
                        val asyncResult = itemDao.getItensByListAndStatus(listId, false).first()
                        android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: carregamento assíncrono concluído com ${asyncResult.size} itens")
                        
                        // Se houver diferença, atualizar na thread principal
                        if (asyncResult.size != items.size || asyncResult.any { newItem -> items.none { it.id == newItem.id } }) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                items = asyncResult
                                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId itens atualizados via carregamento assíncrono: ${items.size}")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro no carregamento assíncrono para widget $appWidgetId", e)
                    }
                }
            } else {
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId não configurado")
                items = emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ShoppingListWidget", "Erro geral ao buscar itens para widget $appWidgetId", e)
            items = emptyList()
        } finally {
            isLoading = false
            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId carregamento finalizado. isLoading: $isLoading")
        }
    }

    override fun onDestroy() {
        android.util.Log.d("ShoppingListWidget", "onDestroy chamado para widget $appWidgetId")
        // Cancelar todas as coroutines em andamento
        dataLoadJob.cancel()
        // Limpeza se necessário
        items = emptyList()
    }

    override fun getCount(): Int {
        val count = items.size
        android.util.Log.d("ShoppingListWidget", "getCount() chamado para widget $appWidgetId, retornando: $count")
        android.util.Log.d("ShoppingListWidget", "Itens atuais na lista: ${items.map { "${it.nome}(ID:${it.id})" }}")
        return count
    }

    override fun getViewAt(position: Int): RemoteViews? {
        android.util.Log.d("ShoppingListWidget", "getViewAt chamado para posição $position no widget $appWidgetId")
        android.util.Log.d("ShoppingListWidget", "Total de itens disponíveis: ${items.size}")
        
        if (position >= items.size) {
            android.util.Log.w("ShoppingListWidget", "Posição $position maior que o tamanho da lista (${items.size})")
            return null
        }

        val item = items[position]
        android.util.Log.d("ShoppingListWidget", "Criando view para item na posição $position: ${item.nome} (ID: ${item.id})")
        
        // Usar layout de item apropriado baseado no tamanho do widget
        val itemLayoutResId = if (isSmallWidget) {
            R.layout.widget_item_small
        } else {
            R.layout.widget_item
        }
        val views = RemoteViews(context.packageName, itemLayoutResId)

        // Configurar nome do item
        views.setTextViewText(R.id.widget_item_name, item.nome)
        android.util.Log.d("ShoppingListWidget", "TextView configurado com: ${item.nome}")

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

