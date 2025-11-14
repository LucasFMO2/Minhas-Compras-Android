package com.example.minhascompras.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
    var itemParaDeletar by remember { mutableStateOf<ItemCompra?>(null) }
    var itemSelecionado by remember { mutableStateOf<ItemCompra?>(null) }

    // Usar allItens para estatísticas (lista completa, sem filtro)
    val totalItens = allItens.size
    val itensComprados = allItens.count { it.comprado }
    val temItensComprados = itensComprados > 0
    
    // Calcular total a pagar (itens não comprados)
    val totalAPagar = remember(allItens) {
        allItens
            .filter { !it.comprado }
            .sumOf { (it.preco ?: 0.0) * it.quantidade }
    }
    val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val lastDeletedItem by viewModel.lastDeletedItem.collectAsState()
    val isArchiving by viewModel.isArchiving.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var searchExpanded by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val isSmallScreen = ResponsiveUtils.isSmallScreen()

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
                        horizontalArrangement = Arrangement.spacedBy(
                            if (ResponsiveUtils.isSmallScreen()) 6.dp else 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ícone de carrinho de compras",
                            modifier = Modifier.size(
                                if (ResponsiveUtils.isSmallScreen()) 20.dp else 24.dp
                            )
                        )
                        Text(
                            itemSelecionado?.nome ?: "Minhas Compras",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = ResponsiveUtils.getTitleFontSize()
                            ),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    if (itemSelecionado != null) {
                        // Modo de seleção - mostrar ações do item selecionado
                        IconButton(onClick = { itemSelecionado = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar"
                            )
                        }
                        IconButton(
                            onClick = {
                                itemParaEditar = itemSelecionado
                                itemSelecionado = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }
                        IconButton(
                            onClick = {
                                itemParaDeletar = itemSelecionado
                                itemSelecionado = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // Modo normal - mostrar botões padrão
                        // Botão de busca
                        IconButton(onClick = { searchExpanded = !searchExpanded }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        }
                        
                        // Menu de ações
                        Box {
                            IconButton(onClick = { showActionMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Mais opções"
                                )
                            }
                            DropdownMenu(
                                expanded = showActionMenu,
                                onDismissRequest = { showActionMenu = false }
                            ) {
                                // Menu de Ordenação
                                DropdownMenuItem(
                                    text = { Text("Ordenar") },
                                    onClick = {
                                        showSortMenu = true
                                        showActionMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null
                                        )
                                    }
                                )
                                
                                HorizontalDivider()
                                
                                // Arquivar lista
                                DropdownMenuItem(
                                    text = { Text("Arquivar Lista") },
                                    onClick = {
                                        showArchiveDialog = true
                                        showActionMenu = false
                                    },
                                    enabled = allItens.isNotEmpty() && !isArchiving,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    }
                                )
                                
                                // Deletar comprados
                                if (temItensComprados) {
                                    DropdownMenuItem(
                                        text = { Text("Limpar Comprados") },
                                        onClick = {
                                            showDeleteDialog = true
                                            showActionMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                                
                                HorizontalDivider()
                                
                                // Configurações
                                DropdownMenuItem(
                                    text = { Text("Configurações") },
                                    onClick = {
                                        onNavigateToSettings()
                                        showActionMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                        
                        // Menu de Ordenação (separado)
                        Box {
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar item"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (allItens.isNotEmpty() && totalAPagar > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shadowElevation = 12.dp
                ) {
                    Column {
                        // Borda superior azul
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.primary,
                            thickness = 2.dp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total a Pagar",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatador.format(totalAPagar),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
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
                        .padding(horizontal = if (isSmallScreen) 12.dp else 16.dp)
                        .padding(top = 8.dp)
                ) {
                    // Barra de busca simplificada - aparece quando expandida ou quando há busca ativa
                    if (searchExpanded || searchQuery.isNotEmpty()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = ResponsiveUtils.getSmallSpacing()),
                            placeholder = { 
                                Text(
                                    "Pesquisar...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = ResponsiveUtils.getBodyFontSize()
                                    )
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        viewModel.onSearchQueryChanged("")
                                        searchExpanded = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Limpar busca",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                    
                    // Chips de filtro - apenas quando necessário e de forma mais discreta
                    if (filterStatus != FilterStatus.ALL || searchQuery.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = filterStatus == FilterStatus.ALL,
                                onClick = { viewModel.onFilterStatusChanged(FilterStatus.ALL) },
                                label = {
                                    Text(
                                        FilterStatus.ALL.displayName,
                                        style = MaterialTheme.typography.labelSmall,
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
                                        style = MaterialTheme.typography.labelSmall,
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
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }

                    // Lista de itens - interface limpa e focada
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 4.dp,
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
                                            // Swipe left - deletar (mostrar confirmação)
                                            itemParaDeletar = item
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
                                    onDelete = { itemParaDeletar = item },
                                    onEdit = { itemParaEditar = item },
                                    onSelect = { itemSelecionado = item },
                                    isSelected = itemSelecionado?.id == item.id
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
                itemSelecionado = null
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
                    itemSelecionado = null
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

    // Diálogo de confirmação para deletar item individual
    itemParaDeletar?.let { item ->
        AlertDialog(
            onDismissRequest = { 
                itemParaDeletar = null
                itemSelecionado = null
            },
            title = { Text("Confirmar exclusão") },
            text = { Text("Deseja deletar \"${item.nome}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletarItem(item)
                        itemParaDeletar = null
                        itemSelecionado = null
                    }
                ) {
                    Text("Deletar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    itemParaDeletar = null
                    itemSelecionado = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

