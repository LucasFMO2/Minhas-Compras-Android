package com.example.minhascompras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.ui.components.AdicionarItemDialog
import com.example.minhascompras.ui.components.EstadoVazioScreen
import com.example.minhascompras.ui.components.ItemCompraCard
import com.example.minhascompras.ui.components.StatisticCard
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel,
    modifier: Modifier = Modifier
) {
    val itens by viewModel.itens.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemParaEditar by remember { mutableStateOf<ItemCompra?>(null) }

    val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val totalItens = itens.size
    val itensComprados = itens.count { it.comprado }
    val itensPendentes = totalItens - itensComprados
    val temItensComprados = itensComprados > 0
    val progresso = if (totalItens > 0) itensComprados.toFloat() / totalItens else 0f
    
    // Calcular totais de preços
    val totalGeral = itens.sumOf { (it.preco ?: 0.0) * it.quantidade }
    val totalComprados = itens.filter { it.comprado }.sumOf { (it.preco ?: 0.0) * it.quantidade }
    val totalPendentes = itens.filter { !it.comprado }.sumOf { (it.preco ?: 0.0) * it.quantidade }
    val temPrecos = itens.any { it.preco != null && it.preco > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Minhas Compras",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = { Text("Adicionar") }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (itens.isEmpty()) {
                EstadoVazioScreen(
                    onAddClick = { showDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                ) {
                    // Estatísticas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticCard(
                            label = "Total",
                            value = totalItens.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatisticCard(
                            label = "Pendentes",
                            value = itensPendentes.toString(),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        StatisticCard(
                            label = "Comprados",
                            value = itensComprados.toString(),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Estatísticas de preços (se houver preços)
                    if (temPrecos) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatisticCard(
                                label = "Total Geral",
                                value = formatador.format(totalGeral),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatisticCard(
                                label = "Pendentes",
                                value = formatador.format(totalPendentes),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            StatisticCard(
                                label = "Comprados",
                                value = formatador.format(totalComprados),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Barra de progresso
                    if (totalItens > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Progresso",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${(progresso * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progresso },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    // Botão deletar comprados
                    AnimatedVisibility(
                        visible = temItensComprados,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Limpar Comprados")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de itens
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = itens,
                            key = { it.id }
                        ) { item ->
                            ItemCompraCard(
                                item = item,
                                onToggleComprado = { viewModel.toggleComprado(item) },
                                onDelete = { viewModel.deletarItem(item) },
                                onEdit = { itemParaEditar = item }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog || itemParaEditar != null) {
        AdicionarItemDialog(
            onDismiss = { 
                showDialog = false
                itemParaEditar = null
            },
            onConfirm = { nome, quantidade, preco, categoria ->
                if (itemParaEditar != null) {
                    // Editar item existente
                    viewModel.atualizarItem(
                        itemParaEditar!!.copy(
                            nome = nome,
                            quantidade = quantidade,
                            preco = preco,
                            categoria = categoria
                        )
                    )
                    itemParaEditar = null
                } else {
                    // Adicionar novo item
                    viewModel.inserirItem(nome, quantidade, preco, categoria)
                    showDialog = false
                }
            },
            itemEdicao = itemParaEditar
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar") },
            text = { Text("Deseja deletar todos os itens comprados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletarComprados()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sim")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Não")
                }
            }
        )
    }
}

