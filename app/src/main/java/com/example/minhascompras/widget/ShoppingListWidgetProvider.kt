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
import kotlinx.coroutines.withContext

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
        android.util.Log.d("ShoppingListWidget", "=== BROADCAST RECEBIDO ===")
        android.util.Log.d("ShoppingListWidget", "Action: ${intent.action}")
        android.util.Log.d("ShoppingListWidget", "Component: ${intent.component}")
        android.util.Log.d("ShoppingListWidget", "Data: ${intent.data}")
        android.util.Log.d("ShoppingListWidget", "Extras: ${intent.extras}")
        android.util.Log.d("ShoppingListWidget", "Flags: ${intent.flags}")
        android.util.Log.d("ShoppingListWidget", "Package: ${intent.`package`}")
        android.util.Log.d("ShoppingListWidget", "Scheme: ${intent.scheme}")
        
        // ADICIONADO: Log mais detalhado para debugging
        android.util.Log.d("ShoppingListWidget", "=== DEBUGGING DETALHADO DO INTENT ===")
        android.util.Log.d("ShoppingListWidget", "Intent toString: ${intent.toString()}")
        android.util.Log.d("ShoppingListWidget", "Intent toURI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}")
        android.util.Log.d("ShoppingListWidget", "Intent type: ${intent.type}")
        android.util.Log.d("ShoppingListWidget", "Intent categories: ${intent.categories}")
        android.util.Log.d("ShoppingListWidget", "Intent source: ${intent.`package`}")
        android.util.Log.d("ShoppingListWidget", "Intent extras bundle: ${intent.extras?.javaClass}")
        
        // Log detalhado para ACTION_ITEM_CLICKED
        if (intent.action == ACTION_ITEM_CLICKED) {
            val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)
            val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            android.util.Log.d("ShoppingListWidget", "!!! ACTION_ITEM_CLICKED DETECTADO !!!")
            android.util.Log.d("ShoppingListWidget", "Item ID: $itemId, Widget ID: $widgetId")
        } else {
            android.util.Log.d("ShoppingListWidget", "Action recebido NÃO é ACTION_ITEM_CLICKED: ${intent.action}")
        }
        
        super.onReceive(context, intent)

        // Adicionar validação de segurança
        if (intent.action == null || !intent.action!!.startsWith("com.example.minhascompras.widget.") &&
            intent.action != "android.appwidget.action.APPWIDGET_UPDATE") {
            android.util.Log.w("ShoppingListWidget", "Action não autorizada ignorada: ${intent.action}")
            return
        }

        android.util.Log.d("ShoppingListWidget", "Action autorizada processada: ${intent.action}")
        
        when (intent.action) {
            ACTION_ITEM_CLICKED -> {
                // Alternar status do item (toggle)
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && itemId != -1L) {
                    toggleItemStatus(context, appWidgetId, itemId)
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
            ACTION_TOGGLE_FILTER -> {
                // Alternar filtro entre mostrar apenas pendentes ou todos
                val appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    toggleFilter(context, appWidgetId)
                }
            }
        }
    }

    private fun toggleItemStatus(context: Context, appWidgetId: Int, itemId: Long) {
        android.util.Log.d("ShoppingListWidget", "=== INÍCIO: toggleItemStatus ===")
        android.util.Log.d("ShoppingListWidget", "Tentando alternar status do item $itemId no widget $appWidgetId")
        android.util.Log.d("ShoppingListWidget", "Thread atual: ${Thread.currentThread().name}")
        
        // Usar um CoroutineScope com SupervisorJob para garantir controle sobre a execução
        val scope = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.SupervisorJob() +
            kotlinx.coroutines.Dispatchers.IO
        )
        
        scope.launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val itemDao = database.itemCompraDao()

                // Buscar item específico
                val item = itemDao.getItemById(itemId)

                android.util.Log.d("ShoppingListWidget", "Item encontrado: ${item?.nome}, status atual: ${item?.comprado}")

                if (item != null) {
                    // Alternar status (toggle)
                    val newStatus = !item.comprado
                    itemDao.updateItemStatus(itemId, newStatus)
                    android.util.Log.d("ShoppingListWidget", "Item ${item.nome} status alterado para: $newStatus")
                    
                    // Mudar para thread principal para atualizações de UI
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        try {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            
                            // ESTRATÉGIA OTIMIZADA: Atualização eficiente sem delays desnecessários
                            
                            // PRIMEIRO: Notificar mudança de dados imediatamente
                            android.util.Log.d("ShoppingListWidget", "Notificando mudança de dados para widget $appWidgetId")
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_items_list)
                            
                            // SEGUNDO: Atualizar widget principal (progresso, contadores, etc.)
                            android.util.Log.d("ShoppingListWidget", "Atualizando widget $appWidgetId após alternar status")
                            updateAppWidget(context, appWidgetManager, appWidgetId)
                            
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId atualizado com sucesso após alternar status")
                            android.util.Log.d("ShoppingListWidget", "=== FIM: toggleItemStatus ===")
                        } catch (e: Exception) {
                            android.util.Log.e("ShoppingListWidget", "Erro ao atualizar widget após alternar status", e)
                        }
                    }
                } else {
                    android.util.Log.w("ShoppingListWidget", "Item $itemId não encontrado")
                    android.util.Log.d("ShoppingListWidget", "=== FIM: toggleItemStatus (item não encontrado) ===")
                }
            } catch (e: Exception) {
                android.util.Log.e("ShoppingListWidget", "Erro ao alternar status do item", e)
                android.util.Log.d("ShoppingListWidget", "=== FIM: toggleItemStatus (com erro) ===")
            }
        }
    }

    private fun toggleFilter(context: Context, appWidgetId: Int) {
        android.util.Log.d("ShoppingListWidget", "=== INÍCIO: toggleFilter ===")
        android.util.Log.d("ShoppingListWidget", "Tentando alternar filtro no widget $appWidgetId")
        
        val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        val currentFilter = prefs.getBoolean("widget_${appWidgetId}_show_only_pending", true)
        val newFilter = !currentFilter
        
        // Salvar nova preferência de filtro
        prefs.edit().putBoolean("widget_${appWidgetId}_show_only_pending", newFilter).apply()
        
        android.util.Log.d("ShoppingListWidget", "Filtro alternado de $currentFilter para $newFilter no widget $appWidgetId")
        
        // Atualizar widget com novo filtro
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAppWidget(context, appWidgetManager, appWidgetId)
        
        android.util.Log.d("ShoppingListWidget", "=== FIM: toggleFilter ===")
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
        const val ACTION_TOGGLE_FILTER = "com.example.minhascompras.widget.ACTION_TOGGLE_FILTER"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_LIST_ID = "extra_list_id"

        /**
         * Atualiza todos os widgets instalados.
         * Deve ser chamado quando os dados mudam no app.
         */
        fun updateAllWidgets(context: Context) {
            android.util.Log.d("ShoppingListWidget", "updateAllWidgets chamado")
            android.util.Log.d("ShoppingListWidget", "Thread atual: ${Thread.currentThread().name}")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(
                    context,
                    ShoppingListWidgetProvider::class.java
                )
            )
            android.util.Log.d("ShoppingListWidget", "Encontrados ${widgetIds.size} widgets para atualizar: ${widgetIds.contentToString()}")
            
            if (widgetIds.isNotEmpty()) {
                // Usar CoroutineScope para garantir que as atualizações sejam sequenciais
                val scope = kotlinx.coroutines.CoroutineScope(
                    kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main
                )
                
                scope.launch {
                    try {
                        // PRIMEIRO: Notificar mudança de dados para todos os widgets
                        for (widgetId in widgetIds) {
                            android.util.Log.d("ShoppingListWidget", "Notificando mudança de dados para widget $widgetId")
                            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_items_list)
                            
                            // Pequena pausa para garantir que a notificação seja processada
                            kotlinx.coroutines.delay(100)
                        }
                        
                        // SEGUNDO: Atualizar cada widget individualmente
                        for (widgetId in widgetIds) {
                            android.util.Log.d("ShoppingListWidget", "Atualizando widget $widgetId")
                            updateAppWidget(context, appWidgetManager, widgetId)
                            
                            // Pequena pausa entre atualizações
                            kotlinx.coroutines.delay(50)
                        }
                        
                        // TERCEIRO: Forçar notificação final para garantir sincronização
                        for (widgetId in widgetIds) {
                            android.util.Log.d("ShoppingListWidget", "Notificação final para widget $widgetId")
                            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_items_list)
                        }
                        
                        android.util.Log.d("ShoppingListWidget", "Todos os widgets atualizados com sucesso")
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro durante atualização dos widgets", e)
                    }
                }
            } else {
                android.util.Log.w("ShoppingListWidget", "Nenhum widget encontrado para atualizar")
            }
        }

        /**
         * Força a atualização imediata dos dados do widget.
         * Método mais agressivo para garantir sincronização.
         */
        fun forceRefreshWidgets(context: Context) {
            android.util.Log.d("ShoppingListWidget", "forceRefreshWidgets chamado")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(
                    context,
                    ShoppingListWidgetProvider::class.java
                )
            )
            
            if (widgetIds.isNotEmpty()) {
                val scope = kotlinx.coroutines.CoroutineScope(
                    kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main
                )
                
                scope.launch {
                    try {
                        for (widgetId in widgetIds) {
                            android.util.Log.d("ShoppingListWidget", "Forçando atualização completa do widget $widgetId")
                            
                            // Forçar notificação múltiplas vezes com pausas
                            repeat(3) {
                                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_items_list)
                                kotlinx.coroutines.delay(200) // Pausa maior para garantir processamento
                            }
                            
                            // Atualizar widget principal
                            updateAppWidget(context, appWidgetManager, widgetId)
                            
                            // Notificação final
                            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_items_list)
                            
                            kotlinx.coroutines.delay(100)
                        }
                        
                        android.util.Log.d("ShoppingListWidget", "Forçamento de atualização concluído para todos os widgets")
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro ao forçar atualização dos widgets", e)
                    }
                }
            }
        }

        /**
         * Força a atualização completa do widget com verificação de dados.
         * Método para garantir sincronização completa quando necessário.
         */
        fun refreshWidgetWithDataVerification(context: Context) {
            android.util.Log.d("ShoppingListWidget", "refreshWidgetWithDataVerification chamado")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(
                    context,
                    ShoppingListWidgetProvider::class.java
                )
            )
            
            if (widgetIds.isNotEmpty()) {
                val scope = kotlinx.coroutines.CoroutineScope(
                    kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main
                )
                
                scope.launch {
                    try {
                        for (widgetId in widgetIds) {
                            android.util.Log.d("ShoppingListWidget", "Iniciando verificação completa para widget $widgetId")
                            
                            // PRIMEIRO: Forçar múltiplas notificações para limpar qualquer cache
                            repeat(3) {
                                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_items_list)
                                kotlinx.coroutines.delay(150)
                            }
                            
                            // SEGUNDO: Atualizar widget principal
                            updateAppWidget(context, appWidgetManager, widgetId)
                            
                            // Pequena pausa
                            kotlinx.coroutines.delay(200)
                            
                            // TERCEIRO: Notificação final
                            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_items_list)
                            
                            android.util.Log.d("ShoppingListWidget", "Verificação completa concluída para widget $widgetId")
                        }
                        
                        android.util.Log.d("ShoppingListWidget", "Verificação completa concluída para todos os widgets")
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro na verificação completa dos widgets", e)
                    }
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
                            withContext(Dispatchers.Main) {
                                views.setTextViewText(R.id.widget_list_name, "Lista removida")
                                views.setTextViewText(R.id.widget_progress_text, "Reconfigure o widget")
                                appWidgetManager.updateAppWidget(appWidgetId, views)
                            }
                            return@launch
                        }

                        val listName = list.nome
                        android.util.Log.d("ShoppingListWidget", "Lista nome: $listName")

                        // Buscar itens pendentes de forma síncrona para garantir dados atualizados
                        android.util.Log.d("ShoppingListWidget", "Buscando itens pendentes da lista $listId")
                        val pendingItems = itemDao.getItensByListAndStatus(listId, false).first()
                        android.util.Log.d("ShoppingListWidget", "Itens pendentes encontrados: ${pendingItems.size}")
                        
                        // Log detalhado dos itens pendentes
                        pendingItems.forEachIndexed { index, item ->
                            android.util.Log.d("ShoppingListWidget", "Item pendente $index: ${item.nome} (ID: ${item.id})")
                        }

                        // Calcular progresso
                        val totalItems = itemDao.getItensByList(listId).first()
                        val completedCount = totalItems.count { it.comprado }
                        val progressText = "$completedCount/${totalItems.size} itens"

                        // Mudar para thread principal para atualizações de UI
                        withContext(Dispatchers.Main) {
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

                            // Configurar PendingIntent para o botão de filtro
                            val filterIntent = Intent(context, ShoppingListWidgetProvider::class.java).apply {
                                action = ACTION_TOGGLE_FILTER
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            val filterPendingIntent = android.app.PendingIntent.getBroadcast(
                                context,
                                appWidgetId + 2000, // Request code diferente
                                filterIntent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(R.id.widget_filter_button, filterPendingIntent)

                            // Obter estado atual do filtro para atualizar texto do botão
                            val showOnlyPending = prefs.getBoolean("widget_${appWidgetId}_show_only_pending", true)
                            val filterText = if (showOnlyPending) "Pendentes" else "Todos"
                            views.setTextViewText(R.id.widget_filter_button, filterText)

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

                            // PRIMEIRO: Notificar que os dados mudaram para atualizar a lista
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: chamando notifyAppWidgetViewDataChanged")
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_items_list)
                            
                            // Pequena pausa para garantir processamento da notificação
                            kotlinx.coroutines.delay(100)
                            
                            // SEGUNDO: Atualizar widget com os dados atualizados
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: chamando updateAppWidget")
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                            
                            // Pequena pausa para garantir processamento da atualização
                            kotlinx.coroutines.delay(100)
                            
                            // TERCEIRO: Notificar novamente para garantir sincronização completa
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: notificação final para garantir sincronização")
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_items_list)
                        }
                        
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro ao atualizar widget $appWidgetId", e)
                        // Em caso de erro, mostrar mensagem genérica
                        withContext(Dispatchers.Main) {
                            views.setTextViewText(R.id.widget_list_name, "Erro ao carregar")
                            views.setTextViewText(R.id.widget_progress_text, "Toque para abrir app")
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
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
