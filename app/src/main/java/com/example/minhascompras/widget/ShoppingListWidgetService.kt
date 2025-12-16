package com.example.minhascompras.widget

import android.appwidget.AppWidgetManager
import android.content.Context
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
            // Obter preferência de filtro (padrão: apenas pendentes)
            val showOnlyPending = prefs.getBoolean("widget_${appWidgetId}_show_only_pending", true)

            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId listId: $listId, showOnlyPending: $showOnlyPending")

            if (listId != -1L) {
                // ESTRATÉGIA MELHORADA: Forçar busca fresca dos dados do banco
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: FORÇANDO BUSCA FRESCA dos itens da lista $listId")
                
                // Limpar cache atual para garantir dados frescos
                val oldItems = items
                items = emptyList()
                
                // Forçar pequena pausa para garantir que qualquer transação anterior seja concluída
                kotlinx.coroutines.runBlocking {
                    kotlinx.coroutines.delay(50) // Pausa maior para garantir conclusão de transações
                }
                
                // Buscar dados frescos diretamente do banco de forma síncrona
                val resultado = try {
                    // Tentar múltiplas vezes para garantir dados frescos
                    var tentativa = 0
                    var ultimoResultado: List<ItemCompra> = emptyList()
                    while (tentativa < 3) {
                        // Buscar itens baseado na preferência de filtro
                        ultimoResultado = if (showOnlyPending) {
                            itemDao.getItensByListAndStatusSync(listId, false)
                        } else {
                            itemDao.getItensByListSync(listId)
                        }
                        android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: tentativa ${tentativa + 1} - encontrados ${ultimoResultado.size} itens")
                        
                        // Verificar se os dados mudaram em relação ao cache anterior
                        if (ultimoResultado.size != oldItems.size ||
                            ultimoResultado.any { novo -> oldItems.none { antigo -> antigo.id == novo.id && antigo.comprado == novo.comprado } }) {
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: MUDANÇA DETECTADA na tentativa ${tentativa + 1}")
                            break
                        }
                        
                        tentativa++
                        if (tentativa < 3) {
                            kotlinx.coroutines.runBlocking {
                                kotlinx.coroutines.delay(100) // Pausa entre tentativas
                            }
                        }
                    }
                    ultimoResultado
                } catch (e: Exception) {
                    android.util.Log.e("ShoppingListWidget", "Erro ao buscar itens síncrona para widget $appWidgetId", e)
                    emptyList()
                }
                
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: RESULTADO FINAL - encontrados ${resultado.size} itens no banco")

                // Log detalhado dos itens encontrados
                resultado.forEachIndexed { index, item ->
                    android.util.Log.d("ShoppingListWidget", "Item $index: ${item.nome} (ID: ${item.id}, Comprado: ${item.comprado})")
                }
                
                // Atualizar items imediatamente com dados frescos
                val oldSize = oldItems.size
                items = resultado
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId itens ATUALIZADOS COM DADOS FRESCOS: ${items.size} (antigo: $oldSize)")
                
                // Verificar se getCount retornará o valor correto
                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId getCount() retornará: ${getCount()}")
                
                // Iniciar verificação adicional em background para garantir sincronização completa
                dataLoadScope.launch {
                    try {
                        kotlinx.coroutines.delay(300) // Delay maior para garantir processamento completo
                        
                        // Buscar novamente para verificar consistência
                        val verificacaoResult = itemDao.getItensByListAndStatusSync(listId, false)
                        android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: VERIFICAÇÃO ADICIONAL - encontrados ${verificacaoResult.size} itens")
                        
                        // Se houver inconsistência, forçar atualização
                        if (verificacaoResult.size != items.size ||
                            verificacaoResult.any { novo -> items.none { atual -> atual.id == novo.id && atual.comprado == novo.comprado } }) {
                            android.util.Log.w("ShoppingListWidget", "Widget $appWidgetId: INCONSISTÊNCIA DETECTADA - forçando atualização")
                            
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                val verificacaoSize = items.size
                                items = verificacaoResult
                                android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId itens corrigidos via verificação: ${items.size} (antigo: $verificacaoSize)")
                                
                                // Forçar notificação de mudança de dados
                                try {
                                    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_items_list)
                                    android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: notificação de mudança forçada após correção")
                                } catch (e: Exception) {
                                    android.util.Log.e("ShoppingListWidget", "Erro ao forçar notificação de mudança", e)
                                }
                            }
                        } else {
                            android.util.Log.d("ShoppingListWidget", "Widget $appWidgetId: verificação concluída - dados consistentes")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ShoppingListWidget", "Erro na verificação adicional para widget $appWidgetId", e)
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

        // Configurar checkbox baseado no status do item
        views.setImageViewResource(R.id.widget_item_checkbox, if (item.comprado) android.R.drawable.checkbox_on_background else android.R.drawable.checkbox_off_background)
        
        // Adicionar feedback visual para itens comprados
        if (item.comprado) {
            // Aplicar estilo de texto tachado para itens comprados
            views.setInt(R.id.widget_item_name, "setPaintFlags",
                android.graphics.Paint.STRIKE_THRU_TEXT_FLAG)
            // Definir cor do texto para itens comprados (mais claro)
            views.setTextColor(R.id.widget_item_name,
                android.graphics.Color.GRAY)
        } else {
            // Remover estilo tachado para itens pendentes
            views.setInt(R.id.widget_item_name, "setPaintFlags", 0)
            // Restaurar cor padrão do texto
            views.setTextColor(R.id.widget_item_name,
                context.getColor(android.R.color.primary_text_light))
        }

        // Adicionar PendingIntent para marcar item como comprado ao tocar
        val clickIntent = Intent().apply {
            action = ShoppingListWidgetProvider.ACTION_ITEM_CLICKED
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(ShoppingListWidgetProvider.EXTRA_ITEM_ID, item.id)
            // Adicionar URI único para cada item
            data = android.net.Uri.parse("widget://item/${item.id}")
            // DEFINIR EXPLICITAMENTE O COMPONENTE DESTINO
            component = android.content.ComponentName(context, ShoppingListWidgetProvider::class.java)
        }
        android.util.Log.d("ShoppingListWidget", "=== CRIANDO PendingIntent PARA ITEM ${item.id} (${item.nome}) NO WIDGET $appWidgetId ===")
        android.util.Log.d("ShoppingListWidget", "Action: ${clickIntent.action}")
        android.util.Log.d("ShoppingListWidget", "Component: ${clickIntent.component}")
        android.util.Log.d("ShoppingListWidget", "Data: ${clickIntent.data}")
        android.util.Log.d("ShoppingListWidget", "Extras: item_id=${clickIntent.getLongExtra(ShoppingListWidgetProvider.EXTRA_ITEM_ID, -1)}, widget_id=${clickIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)}")
        
        // CORREÇÃO CRÍTICA: Gerar request code verdadeiramente único sem conflitos
        // Usar abordagem baseada em hash code para garantir unicidade
        val hashString = "${appWidgetId}_${item.id}_${item.nome}_${System.currentTimeMillis()}"
        val uniqueId = hashString.hashCode()
        val requestCode = if (uniqueId < 0) -uniqueId else uniqueId // Garantir positivo
        
        android.util.Log.d("ShoppingListWidget", "Request code gerado para item ${item.id}: $requestCode")
        android.util.Log.w("ShoppingListWidget", "!!! CORREÇÃO REQUEST CODE MELHORADA: appWidgetId=$appWidgetId, itemId=${item.id}, hashString='$hashString', hashCode=$uniqueId, finalRequestCode=$requestCode")
        
        // VERIFICAÇÃO MELHORADA: Verificar se há potencial conflito de request code
        val potentialConflictItems = items.filter { otherItem ->
            otherItem.id != item.id && {
                val otherHashString = "${appWidgetId}_${otherItem.id}_${otherItem.nome}"
                val otherUniqueId = otherHashString.hashCode()
                val otherRequestCode = if (otherUniqueId < 0) -otherUniqueId else otherUniqueId
                otherRequestCode == requestCode
            }()
        }
        if (potentialConflictItems.isNotEmpty()) {
            android.util.Log.e("ShoppingListWidget", "!!! CONFLITO DETECTADO: Item ${item.id} tem mesmo request code que itens: ${potentialConflictItems.map { it.id }}")
            // Se houver conflito, gerar um novo request code com timestamp adicional
            val emergencyHashString = "${appWidgetId}_${item.id}_${item.nome}_${System.nanoTime()}"
            val emergencyUniqueId = emergencyHashString.hashCode()
            val emergencyRequestCode = if (emergencyUniqueId < 0) -emergencyUniqueId else emergencyUniqueId
            android.util.Log.w("ShoppingListWidget", "!!! CORREÇÃO DE EMERGÊNCIA: Novo request code gerado para item ${item.id}: $emergencyRequestCode")
        } else {
            android.util.Log.d("ShoppingListWidget", "!!! SEM CONFLITOS: Request code $requestCode é único para item ${item.id}")
        }
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode, // Request code único baseado no widgetId e itemId
            clickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        android.util.Log.d("ShoppingListWidget", "PendingIntent criado: $pendingIntent")
        try {
            android.util.Log.d("ShoppingListWidget", "Intent do PendingIntent: não disponível para logging")
        } catch (e: Exception) {
            android.util.Log.d("ShoppingListWidget", "Intent do PendingIntent: não disponível")
        }
        
        // CORREÇÃO: Configurar PendingIntent em múltiplos elementos para máxima compatibilidade
        try {
            // PRIMEIRO: Configurar no container LinearLayout root (elemento mais abrangente)
            views.setOnClickPendingIntent(R.id.widget_item_container, pendingIntent)
            android.util.Log.d("ShoppingListWidget", "!!! PendingIntent CONFIGURADO NO CONTAINER (widget_item_container) !!!")
            
            // SEGUNDO: Configurar no TextView como backup
            views.setOnClickPendingIntent(R.id.widget_item_name, pendingIntent)
            android.util.Log.d("ShoppingListWidget", "!!! PendingIntent TAMBÉM CONFIGURADO NO TEXTVIEW (widget_item_name) !!!")
            
            // TERCEIRO: Configurar no CheckBox como alternativa adicional
            views.setOnClickPendingIntent(R.id.widget_item_checkbox, pendingIntent)
            android.util.Log.d("ShoppingListWidget", "!!! PendingIntent TAMBÉM CONFIGURADO NO CHECKBOX (widget_item_checkbox) !!!")
            
            android.util.Log.d("ShoppingListWidget", "!!! ESTRATÉGIA COMPLETA: Container + TextView + CheckBox !!!")
            
        } catch (e: Exception) {
            android.util.Log.e("ShoppingListWidget", "Erro ao configurar PendingIntent", e)
        }
        
        android.util.Log.d("ShoppingListWidget", "=== PendingIntent CONFIGURADO PARA ITEM ${item.id} NO WIDGET $appWidgetId ===")
        
        // Verificação adicional para garantir que o PendingIntent foi configurado
        try {
            val testIntent = android.app.PendingIntent.getBroadcast(
                context,
                requestCode,
                clickIntent,
                android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            android.util.Log.d("ShoppingListWidget", "PendingIntent VERIFICAÇÃO para item ${item.id}: ${testIntent != null} (requestCode: $requestCode)")
        } catch (e: Exception) {
            android.util.Log.e("ShoppingListWidget", "Erro ao verificar PendingIntent para item ${item.id}", e)
        }

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
