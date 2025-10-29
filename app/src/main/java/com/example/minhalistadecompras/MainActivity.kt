package com.example.minhalistadecompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.minhalistadecompras.R
import com.example.minhalistadecompras.data.ItemCompra
import com.example.minhalistadecompras.ui.theme.MinhaListaDeComprasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MinhaListaDeComprasTheme {
                ListaComprasApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaComprasApp() {
    var listaItens by remember { mutableStateOf(ItemCompra.createSampleItems()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemEditando by remember { mutableStateOf<ItemCompra?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    val totalItens = listaItens.size
                    val comprados = listaItens.count { it.comprado }
                    val pendentes = totalItens - comprados
                    
                    Text(
                        text = stringResource(R.string.total_items, totalItens),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    itemEditando = null
                    showDialog = true 
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Estatísticas
            EstatisticasCard(
                totalItens = listaItens.size,
                comprados = listaItens.count { it.comprado },
                pendentes = listaItens.count { !it.comprado }
            )
            
            // Lista de itens
            if (listaItens.isEmpty()) {
                ListaVazia()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaItens) { item ->
                        ItemCompraCard(
                            item = item,
                            onToggleComprado = { 
                                listaItens = listaItens.map { 
                                    if (it.id == item.id) it.copy(comprado = !it.comprado) 
                                    else it 
                                }
                            },
                            onEditar = { 
                                itemEditando = item
                                showDialog = true 
                            },
                            onRemover = { 
                                listaItens = listaItens.filter { it.id != item.id }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog para adicionar/editar item
    if (showDialog) {
        DialogAdicionarItem(
            item = itemEditando,
            onDismiss = { 
                showDialog = false
                itemEditando = null
            },
            onConfirmar = { novoItem ->
                if (itemEditando != null) {
                    // Editando item existente
                    listaItens = listaItens.map { 
                        if (it.id == itemEditando!!.id) novoItem.copy(id = it.id)
                        else it 
                    }
                } else {
                    // Adicionando novo item
                    val novoId = (listaItens.maxOfOrNull { it.id } ?: 0) + 1
                    listaItens = listaItens + novoItem.copy(id = novoId)
                }
                showDialog = false
                itemEditando = null
            }
        )
    }
}

@Composable
fun EstatisticasCard(
    totalItens: Int,
    comprados: Int,
    pendentes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstatisticaItem(
                label = stringResource(R.string.total_items, totalItens),
                icon = Icons.Default.List,
                color = MaterialTheme.colorScheme.primary
            )
            EstatisticaItem(
                label = stringResource(R.string.comprados_count, comprados),
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary
            )
            EstatisticaItem(
                label = stringResource(R.string.pendentes_count, pendentes),
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EstatisticaItem(
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun ListaVazia() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_list),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCompraCard(
    item: ItemCompra,
    onToggleComprado: () -> Unit,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.comprado,
                onCheckedChange = { onToggleComprado() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (item.comprado) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.comprado) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Row {
                    Text(
                        text = "Qtd: ${item.quantidade}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (item.categoria.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${item.categoria}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row {
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onRemover) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAdicionarItem(
    item: ItemCompra?,
    onDismiss: () -> Unit,
    onConfirmar: (ItemCompra) -> Unit
) {
    var nome by remember { mutableStateOf(item?.nome ?: "") }
    var quantidade by remember { mutableStateOf(item?.quantidade?.toString() ?: "1") }
    var categoria by remember { mutableStateOf(item?.categoria ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (item != null) stringResource(R.string.edit) else stringResource(R.string.add_item)
            ) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text(stringResource(R.string.item_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { quantidade = it },
                    label = { Text(stringResource(R.string.quantity_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text(stringResource(R.string.category_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nome.isNotBlank()) {
                        onConfirmar(
                            ItemCompra(
                                id = item?.id ?: 0,
                                nome = nome.trim(),
                                quantidade = quantidade.toIntOrNull() ?: 1,
                                categoria = categoria.trim(),
                                comprado = item?.comprado ?: false
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ListaComprasPreview() {
    MinhaListaDeComprasTheme {
        ListaComprasApp()
    }
}