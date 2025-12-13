package com.example.minhascompras.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.ShoppingListRepository
import com.example.minhascompras.ui.theme.MinhasComprasTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Activity de configuração do widget.
 * 
 * Permite ao usuário selecionar qual lista de compras o widget deve exibir.
 */
class WidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obter o ID do widget
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // Se não há widget ID válido, finalizar
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Inicializar repositório
        val database = AppDatabase.getDatabase(applicationContext)
        val shoppingListRepository = ShoppingListRepository(database.shoppingListDao())

        setContent {
            MinhasComprasTheme {
                WidgetConfigureScreen(
                    appWidgetId = appWidgetId,
                    shoppingListRepository = shoppingListRepository,
                    onListSelected = { listId ->
                        saveWidgetConfiguration(listId)
                        finishConfiguration()
                    },
                    onCancel = {
                        finishConfiguration(cancel = true)
                    }
                )
            }
        }
    }

    private fun saveWidgetConfiguration(listId: Long) {
        val prefs = getSharedPreferences(ShoppingListWidgetProvider.WIDGET_PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
            .putLong("widget_${appWidgetId}_list_id", listId)
            .apply()
    }

    private fun finishConfiguration(cancel: Boolean = false) {
        if (cancel) {
            setResult(RESULT_CANCELED)
        } else {
            // Acionar atualização do widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            ShoppingListWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
        }
        finish()
    }
}

@Composable
fun WidgetConfigureScreen(
    appWidgetId: Int,
    shoppingListRepository: ShoppingListRepository,
    onListSelected: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var lists by remember { mutableStateOf<List<com.example.minhascompras.data.ShoppingList>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Carregar listas
    LaunchedEffect(Unit) {
        try {
            lists = runBlocking {
                shoppingListRepository.allLists.first()
            }
            isLoading = false
        } catch (e: Exception) {
            android.util.Log.e("WidgetConfigure", "Erro ao carregar listas", e)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecionar Lista para Widget") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancelar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (lists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Nenhuma lista encontrada",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Crie uma lista no app primeiro",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onCancel) {
                            Text("Voltar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Escolha a lista de compras que este widget deve exibir:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(lists) { list ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onListSelected(list.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = list.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (list.isDefault) {
                                        Text(
                                            text = "Lista padrão",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

