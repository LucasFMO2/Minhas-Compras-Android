package com.example.minhascompras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.FilterStatus
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.data.SortOrder
import com.example.minhascompras.ui.components.AdicionarItemDialog
import com.example.minhascompras.ui.components.EstadoVazioScreen
import com.example.minhascompras.ui.components.ItemCompraCard
import com.example.minhascompras.ui.components.StatisticCard
import com.example.minhascompras.ui.utils.ResponsiveUtils
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val itens by viewModel.itens.collectAsState() // Lista filtrada para exibição
    val allItens by viewModel.allItens.collectAsState() // Lista completa para estatísticas
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var itemParaEditar by remember { mutableStateOf<ItemCompra?>(null) }

    val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    // Usar allItens para estatísticas (lista completa, sem filtro)
    val totalItens = allItens.size
    val itensComprados = allItens.count { it.comprado }
    val itensPendentes = totalItens - itensComprados
    val temItensComprados = itensComprados > 0
    val progresso = if (totalItens > 0) itensComprados.toFloat() / totalItens else 0f
    
    // Calcular totais de preços usando allItens (lista completa)
    val totalGeral = allItens.sumOf { (it.preco ?: 0.0) * it.quantidade }
    val totalComprados = allItens.filter { it.comprado }.sumOf { (it.preco ?: 0.0) * it.quantidade }
    val totalPendentes = allItens.filter { !it.comprado }.sumOf { (it.preco ?: 0.0) * it.quantidade }
    val temPrecos = allItens.any { it.preco != null && it.preco > 0 }
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val lastDeletedItem by viewModel.lastDeletedItem.collectAsState()
    val isArchiving by viewModel.isArchiving.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val isSmallScreen = ResponsiveUtils.isSmallScreen()
    val contentHorizontalPadding = if (isSmallScreen) 8.dp else ResponsiveUtils.getHorizontalPadding()

    // Mostrar Snackbar quando um item for deletado
    LaunchedEffect(lastDeletedItem) {
        if (lastDeletedItem != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Item deletado",
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDeleteItem()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collectLatest { uiMessage ->
            val duration = when (uiMessage) {
                is ListaComprasViewModel.UiMessage.Success -> SnackbarDuration.Short
                is ListaComprasViewModel.UiMessage.Info -> SnackbarDuration.Short
                is ListaComprasViewModel.UiMessage.Error -> SnackbarDuration.Long
            }
            val actionLabel = if (uiMessage is ListaComprasViewModel.UiMessage.Error) "OK" else null
            snackbarHostState.showSnackbar(
                message = uiMessage.message,
                actionLabel = actionLabel,
                withDismissAction = uiMessage is ListaComprasViewModel.UiMessage.Error,
                duration = duration
            )
        }
    }

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
                            contentDescription = "Ícone de carrinho de compras",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Minhas Compras",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Menu de Ordenação
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ordenar"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nome (A-Z)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_NAME_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == SortOrder.BY_NAME_ASC) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nome (Z-A)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_NAME_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == SortOrder.BY_NAME_DESC) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Data (Mais Recente)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_DATE_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == SortOrder.BY_DATE_DESC) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Data (Mais Antiga)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_DATE_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == SortOrder.BY_DATE_ASC) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Preço (Menor)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_PRICE_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == SortOrder.BY_PRICE_ASC) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Preço (Maior)") },
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_PRICE_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOrder == SortOrder.BY_PRICE_DESC) {
                                        Icon(Icons.Default.Check, contentDescription = "Selecionado", modifier = Modifier.size(16.dp))
                                    }
                                }
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Histórico"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações"
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
                        contentDescription = "Adicionar item"
                    )
                },
                text = { Text("Adicionar") }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Verificar se a lista completa está vazia ou se apenas o filtro não retornou resultados
            val listaCompletaVazia = allItens.isEmpty()
            val filtroSemResultados = !listaCompletaVazia && itens.isEmpty()
            
            if (listaCompletaVazia) {
                // Lista completamente vazia - mostrar tela de estado vazio padrão
                EstadoVazioScreen(
                    onAddClick = { showDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (filtroSemResultados) {
                // Filtro ativo mas sem resultados - mostrar mensagem específica
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing()),
                        modifier = Modifier.padding(ResponsiveUtils.getHorizontalPadding())
                    ) {
                        val emptyIconSize = if (ResponsiveUtils.isSmallScreen()) 60.dp else 80.dp
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Nenhum item encontrado",
                            modifier = Modifier.size(emptyIconSize),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = when (filterStatus) {
                                FilterStatus.PURCHASED -> "Nenhum item comprado ainda"
                                FilterStatus.PENDING -> "Nenhum item pendente"
                                else -> "Nenhum item encontrado"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (filterStatus) {
                                FilterStatus.PURCHASED -> "Marque itens como comprados para vê-los aqui"
                                FilterStatus.PENDING -> "Todos os itens foram marcados como comprados"
                                else -> "Tente ajustar sua busca ou filtro"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (filterStatus != FilterStatus.ALL) {
                            TextButton(
                                onClick = { viewModel.onFilterStatusChanged(FilterStatus.ALL) }
                            ) {
                                Text("Ver todos os itens")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = contentHorizontalPadding)
                        .padding(top = ResponsiveUtils.getVerticalPadding())
                ) {
                    // Barra de busca
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = ResponsiveUtils.getSmallSpacing()),
                        placeholder = { Text("Pesquisar itens...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    // Chips de filtro
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = ResponsiveUtils.getSmallSpacing()),
                        horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing()),
                        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
                    ) {
                        FilterChip(
                            selected = filterStatus == FilterStatus.ALL,
                            onClick = { viewModel.onFilterStatusChanged(FilterStatus.ALL) },
                            label = {
                                Text(
                                    FilterStatus.ALL.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.wrapContentWidth()
                        )
                        FilterChip(
                            selected = filterStatus == FilterStatus.PENDING,
                            onClick = { viewModel.onFilterStatusChanged(FilterStatus.PENDING) },
                            label = {
                                Text(
                                    FilterStatus.PENDING.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.wrapContentWidth()
                        )
                        FilterChip(
                            selected = filterStatus == FilterStatus.PURCHASED,
                            onClick = { viewModel.onFilterStatusChanged(FilterStatus.PURCHASED) },
                            label = {
                                Text(
                                    FilterStatus.PURCHASED.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(ResponsiveUtils.getSmallSpacing()))
                    
                    // Estatísticas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getStatisticCardSpacing())
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
                        Spacer(modifier = Modifier.height(ResponsiveUtils.getSpacing()))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getStatisticCardSpacing())
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
                        Spacer(modifier = Modifier.height(ResponsiveUtils.getSpacing()))
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

                    // Botão arquivar lista
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = { showArchiveDialog = true },
                            enabled = allItens.isNotEmpty() && !isArchiving
                        ) {
                            if (isArchiving) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Arquivando...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Arquivar lista",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Arquivar Lista")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                                        contentDescription = "Deletar itens comprados",
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
                        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing()),
                        contentPadding = PaddingValues(
                            bottom = if (isSmallScreen) 100.dp else 120.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = itens,
                            key = { it.id }
                        ) { item ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            // Swipe left - deletar
                                            viewModel.deletarItem(item)
                                            false // Deixa o composable animar
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            // Swipe right - marcar como comprado
                                            viewModel.toggleComprado(item)
                                            false // Deixa o composable animar
                                        }
                                        else -> false
                                    }
                                }
                            )
                            
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val currentValue = dismissState.currentValue
                                    val color = when (currentValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                                    }
                                    val icon = when (currentValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        SwipeToDismissBoxValue.Settled -> null
                                    }
                                    val alignment = when (currentValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color),
                                        contentAlignment = alignment
                                    ) {
                                        if (icon != null) {
                                            val iconTint = when (currentValue) {
                                                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimary
                                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onError
                                                SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurface
                                            }
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = when (currentValue) {
                                                    SwipeToDismissBoxValue.StartToEnd -> "Marcar como comprado"
                                                    SwipeToDismissBoxValue.EndToStart -> "Deletar item"
                                                    SwipeToDismissBoxValue.Settled -> null
                                                },
                                                tint = iconTint,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
    }

    if (showDialog || itemParaEditar != null) {
        AdicionarItemDialog(
            onDismiss = { 
                showDialog = false
                itemParaEditar = null
            },
            onConfirm = { nome, quantidade, preco, categoria ->
                itemParaEditar?.let { item ->
                    // Editar item existente
                    viewModel.atualizarItem(
                        item.copy(
                            nome = nome,
                            quantidade = quantidade,
                            preco = preco,
                            categoria = categoria
                        )
                    )
                    itemParaEditar = null
                } ?: run {
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

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Arquivar lista atual") },
            text = { Text("Isso move todos os itens para o histórico e limpa a lista atual. Deseja continuar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.arquivarLista()
                        showArchiveDialog = false
                    }
                ) {
                    Text("Arquivar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

