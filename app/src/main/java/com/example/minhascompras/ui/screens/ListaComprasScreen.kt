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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.BarChart
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
import com.example.minhascompras.ui.viewmodel.ShoppingListViewModel
import com.example.minhascompras.ui.viewmodel.UpdateViewModel
import com.example.minhascompras.ui.viewmodel.UpdateState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    updateViewModel: UpdateViewModel? = null,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    initialShowDialog: Boolean = false,
    modifier: Modifier = Modifier
) {
    val itens by viewModel.itens.collectAsState() // Lista filtrada para exibição
    val allItens by viewModel.allItens.collectAsState() // Lista completa para estatísticas
    var showDialog by remember { mutableStateOf(initialShowDialog) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    var itemParaEditar by remember { mutableStateOf<ItemCompra?>(null) }
    var itemParaDeletar by remember { mutableStateOf<ItemCompra?>(null) }
    var itemSelecionado by remember { mutableStateOf<ItemCompra?>(null) }

    // Usar allItens para estatísticas (lista completa, sem filtro)
    val totalItens = allItens.size
    val itensComprados = allItens.count { it.comprado }
    val temItensComprados = itensComprados > 0
    
    // Calcular total a pagar (todos os itens - valor fixo)
    val totalAPagar = remember(allItens) {
        allItens
            .sumOf { (it.preco ?: 0.0) * it.quantidade }
    }
    val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val lastDeletedItem by viewModel.lastDeletedItem.collectAsState()
    val isArchiving by viewModel.isArchiving.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) } // Pode remover depois
    var showActionMenu by remember { mutableStateOf(false) } // Pode remover depois
    var searchExpanded by remember { mutableStateOf(false) }
    var showSortSubMenu by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSmallScreen = ResponsiveUtils.isSmallScreen()
    
    // Estados do ShoppingListViewModel
    val allLists by shoppingListViewModel.allLists.collectAsState()
    val activeList by shoppingListViewModel.activeList.collectAsState()
    val activeListId by shoppingListViewModel.activeListId.collectAsState()
    val nonDefaultListCount by shoppingListViewModel.nonDefaultListCount.collectAsState()
    val hasUserCreatedLists = nonDefaultListCount > 0
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showRenameListDialog by remember { mutableStateOf(false) }
    var showDeleteListDialog by remember { mutableStateOf(false) }
    var showNoListDialog by remember { mutableStateOf(false) }
    var listToRename by remember { mutableStateOf<com.example.minhascompras.data.ShoppingList?>(null) }
    var listToDelete by remember { mutableStateOf<com.example.minhascompras.data.ShoppingList?>(null) }

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

    // Observar estado de atualização e mostrar diálogo automaticamente
    val updateState by updateViewModel?.updateState?.collectAsState() ?: remember { mutableStateOf<UpdateState?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(updateState) {
        if (updateState is UpdateState.UpdateAvailable) {
            showUpdateDialog = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Cabeçalho do drawer com botão de fechar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        IconButton(
                            onClick = { scope.launch { drawerState.close() } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar menu"
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Seção de Listas
                    Text(
                        text = "Listas de Compras",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Lista de listas disponíveis
                    allLists.forEach { list ->
                        var showMenu by remember { mutableStateOf(false) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavigationDrawerItem(
                                icon = {
                                    if (list.id == activeListId) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Lista ativa",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = null
                                        )
                                    }
                                },
                                label = { 
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            list.nome,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = if (list.id == activeListId) FontWeight.Bold else FontWeight.Normal,
                                            color = if (list.id == activeListId) 
                                                MaterialTheme.colorScheme.primary 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                        if (list.id == activeListId) {
                                            Surface(
                                                shape = MaterialTheme.shapes.extraSmall,
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(6.dp)
                                            ) {}
                                        }
                                    }
                                },
                                selected = list.id == activeListId,
                                onClick = {
                                    shoppingListViewModel.setActiveList(list.id)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Menu de opções (apenas se não for lista padrão)
                            if (!list.isDefault) {
                                Box {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opções",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Renomear") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                listToRename = list
                                                showRenameListDialog = true
                                                showMenu = false
                                                scope.launch { drawerState.close() }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Deletar") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                listToDelete = list
                                                showDeleteListDialog = true
                                                showMenu = false
                                                scope.launch { drawerState.close() }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Botão para criar nova lista
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = { Text("Criar Nova Lista") },
                        selected = false,
                        onClick = {
                            showCreateListDialog = true
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Buscar
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        label = { Text("Buscar") },
                        selected = searchExpanded,
                        onClick = {
                            searchExpanded = !searchExpanded
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    // Histórico
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null
                            )
                        },
                        label = { Text("Histórico") },
                        selected = false,
                        onClick = {
                            onNavigateToHistory()
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null
                            )
                        },
                        label = { Text("Estatísticas") },
                        selected = false,
                        onClick = {
                            onNavigateToStatistics()
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Ordenar (com submenu)
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = null
                            )
                            
                        },
                        label = { Text("Ordenar") },
                        selected = showSortSubMenu,
                        onClick = {
                            showSortSubMenu = !showSortSubMenu
                        }
                    )
                    
                    // Submenu de ordenação
                    if (showSortSubMenu) {
                        Column(modifier = Modifier.padding(start = 48.dp)) {
                            NavigationDrawerItem(
                                label = { Text("Nome (A-Z)") },
                                selected = sortOrder == SortOrder.BY_NAME_ASC,
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_NAME_ASC)
                                    showSortSubMenu = false
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Nome (Z-A)") },
                                selected = sortOrder == SortOrder.BY_NAME_DESC,
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_NAME_DESC)
                                    showSortSubMenu = false
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Data (Mais Recente)") },
                                selected = sortOrder == SortOrder.BY_DATE_DESC,
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_DATE_DESC)
                                    showSortSubMenu = false
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Data (Mais Antiga)") },
                                selected = sortOrder == SortOrder.BY_DATE_ASC,
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_DATE_ASC)
                                    showSortSubMenu = false
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Preço (Menor)") },
                                selected = sortOrder == SortOrder.BY_PRICE_ASC,
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_PRICE_ASC)
                                    showSortSubMenu = false
                                    scope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Preço (Maior)") },
                                selected = sortOrder == SortOrder.BY_PRICE_DESC,
                                onClick = {
                                    viewModel.setSortOrder(SortOrder.BY_PRICE_DESC)
                                    showSortSubMenu = false
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Arquivar Lista
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null
                            )
                        },
                        label = { Text("Arquivar Lista") },
                        selected = false,
                        onClick = {
                            if (allItens.isNotEmpty() && !isArchiving) {
                                showArchiveDialog = true
                                scope.launch { drawerState.close() }
                            }
                        }
                    )
                    
                    // Limpar Comprados
                    if (temItensComprados) {
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            label = { Text("Limpar Comprados") },
                            selected = false,
                            onClick = {
                                showDeleteDialog = true
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Configurações
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        },
                        label = { Text("Configurações") },
                        selected = false,
                        onClick = {
                            onNavigateToSettings()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
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
                                    activeList?.nome ?: "Nenhuma lista selecionada",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = ResponsiveUtils.getTitleFontSize()
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Indicador visual da lista ativa
                                if (activeList != null) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier
                                            .size(8.dp)
                                    ) {}
                                }
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
                            }
                            // Modo normal - sem botões extras, tudo está na barra abaixo
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    
                    // Barra de ações fixa abaixo da TopBar (sem barra, apenas ícones)
                    if (itemSelecionado == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Limpar (Excluir Todos) - PRIMEIRO
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                enabled = allItens.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir Todos",
                                    tint = if (allItens.isNotEmpty()) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Arquivar Lista - SEGUNDO
                            IconButton(
                                onClick = {
                                    if (allItens.isNotEmpty() && !isArchiving) {
                                        showArchiveDialog = true
                                    }
                                },
                                enabled = allItens.isNotEmpty() && !isArchiving,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = "Arquivar Lista",
                                    tint = if (allItens.isNotEmpty() && !isArchiving) 
                                        MaterialTheme.colorScheme.onSurface 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Ordenar (com dropdown) - TERCEIRO
                            Box(modifier = Modifier.weight(1f)) {
                                IconButton(
                                    onClick = { showSortDropdown = !showSortDropdown }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = "Ordenar",
                                        tint = if (showSortDropdown) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = showSortDropdown,
                                    onDismissRequest = { showSortDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Nome (A-Z)") },
                                        onClick = {
                                            viewModel.setSortOrder(SortOrder.BY_NAME_ASC)
                                            showSortDropdown = false
                                        },
                                        leadingIcon = {
                                            if (sortOrder == SortOrder.BY_NAME_ASC) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Nome (Z-A)") },
                                        onClick = {
                                            viewModel.setSortOrder(SortOrder.BY_NAME_DESC)
                                            showSortDropdown = false
                                        },
                                        leadingIcon = {
                                            if (sortOrder == SortOrder.BY_NAME_DESC) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Data (Mais Recente)") },
                                        onClick = {
                                            viewModel.setSortOrder(SortOrder.BY_DATE_DESC)
                                            showSortDropdown = false
                                        },
                                        leadingIcon = {
                                            if (sortOrder == SortOrder.BY_DATE_DESC) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Data (Mais Antiga)") },
                                        onClick = {
                                            viewModel.setSortOrder(SortOrder.BY_DATE_ASC)
                                            showSortDropdown = false
                                        },
                                        leadingIcon = {
                                            if (sortOrder == SortOrder.BY_DATE_ASC) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Preço (Menor)") },
                                        onClick = {
                                            viewModel.setSortOrder(SortOrder.BY_PRICE_ASC)
                                            showSortDropdown = false
                                        },
                                        leadingIcon = {
                                            if (sortOrder == SortOrder.BY_PRICE_ASC) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Preço (Maior)") },
                                        onClick = {
                                            viewModel.setSortOrder(SortOrder.BY_PRICE_DESC)
                                            showSortDropdown = false
                                        },
                                        leadingIcon = {
                                            if (sortOrder == SortOrder.BY_PRICE_DESC) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                }
                            }
                            
                            // Histórico - QUARTO
                            IconButton(
                                onClick = { onNavigateToHistory() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Histórico",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Buscar - ÚLTIMO
                            IconButton(
                                onClick = { searchExpanded = !searchExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = if (searchExpanded) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (hasUserCreatedLists) {
                        showDialog = true
                    } else {
                        showNoListDialog = true
                    }
                },
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
            if (allItens.isNotEmpty()) {
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total a Pagar:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatador.format(totalAPagar),
                                style = MaterialTheme.typography.bodySmall,
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
            
            // Verificar se não há listas criadas pelo usuário
            val naoTemListas = !hasUserCreatedLists
            
            if (listaCompletaVazia) {
                // Lista completamente vazia - verificar se precisa criar lista primeiro
                if (naoTemListas) {
                    // Não há listas criadas - mostrar opção de criar primeira lista
                    EstadoVazioScreen(
                        onAddClick = { showCreateListDialog = true },
                        showCreateList = true,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Há listas mas estão vazias - mostrar opção de adicionar item
                    EstadoVazioScreen(
                        onAddClick = { showDialog = true },
                        showCreateList = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
            text = { Text("Deseja deletar TODOS os itens da lista? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletarTodos()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Deletar Todos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
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

    // Diálogo de atualização disponível (mostrado automaticamente)
    if (showUpdateDialog && updateState is UpdateState.UpdateAvailable && updateViewModel != null) {
        val state = updateState as UpdateState.UpdateAvailable
        val currentVersionCode = updateViewModel.getCurrentVersionCode()
        val currentVersionName = updateViewModel.getCurrentVersionName()
        val canDownload = state.updateInfo.versionCode > currentVersionCode
        
        AlertDialog(
            onDismissRequest = { 
                showUpdateDialog = false
                updateViewModel.resetState()
            },
            title = { Text("Atualização Disponível") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nova versão: ${state.updateInfo.versionName}")
                    if (!canDownload) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "⚠️ Você já está usando a versão ${currentVersionName} ou uma versão mais recente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (state.updateInfo.fileSize > 0) {
                        Text(
                            "Tamanho: ${String.format("%.1f", state.updateInfo.fileSize / (1024.0 * 1024.0))} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.updateInfo.releaseNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Novidades:",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.updateInfo.releaseNotes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (canDownload) {
                            updateViewModel.downloadUpdate(state.updateInfo)
                            showUpdateDialog = false
                        } else {
                            showUpdateDialog = false
                            updateViewModel.resetState()
                        }
                    },
                    enabled = canDownload
                ) {
                    Text(if (canDownload) "Baixar e Instalar" else "Fechar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showUpdateDialog = false
                    updateViewModel.resetState()
                }) {
                    Text("Depois")
                }
            }
        )
    }
    
    // Diálogo para avisar que precisa criar uma lista primeiro
    if (showNoListDialog) {
        AlertDialog(
            onDismissRequest = { showNoListDialog = false },
            title = { Text("Crie uma lista primeiro") },
            text = {
                Text("Você precisa criar uma lista de compras antes de adicionar produtos.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoListDialog = false
                        showCreateListDialog = true
                    }
                ) {
                    Text("Criar Lista")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoListDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para criar nova lista
    if (showCreateListDialog) {
        var newListName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { 
                showCreateListDialog = false
                newListName = ""
            },
            title = { Text("Criar Nova Lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nome da lista") },
                    placeholder = { Text("Ex: Supermercado, Farmácia...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            shoppingListViewModel.createList(newListName.trim())
                            showCreateListDialog = false
                            newListName = ""
                        }
                    },
                    enabled = newListName.isNotBlank()
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCreateListDialog = false
                    newListName = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Dialog para renomear lista
    if (showRenameListDialog && listToRename != null) {
        var newListName by remember(listToRename) { 
            mutableStateOf(listToRename?.nome ?: "") 
        }
        
        AlertDialog(
            onDismissRequest = { 
                showRenameListDialog = false
                listToRename = null
                newListName = ""
            },
            title = { Text("Renomear Lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nome da lista") },
                    placeholder = { Text("Ex: Supermercado, Farmácia...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newListName.isNotBlank() && listToRename != null) {
                            shoppingListViewModel.renameList(
                                listToRename!!.id,
                                newListName.trim()
                            )
                            showRenameListDialog = false
                            listToRename = null
                            newListName = ""
                        }
                    },
                    enabled = newListName.isNotBlank() && newListName.trim() != listToRename?.nome
                ) {
                    Text("Renomear")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRenameListDialog = false
                    listToRename = null
                    newListName = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Dialog para deletar lista
    if (showDeleteListDialog && listToDelete != null) {
        val list = listToDelete!!
        var itemCount by remember { mutableStateOf(0) }
        
        LaunchedEffect(list.id) {
            itemCount = shoppingListViewModel.getItemCountForList(list.id)
        }
        
        AlertDialog(
            onDismissRequest = { 
                showDeleteListDialog = false
                listToDelete = null
            },
            title = { Text("Deletar Lista") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Deseja deletar a lista \"${list.nome}\"?")
                    if (itemCount > 0) {
                        Text(
                            "⚠️ Esta lista contém $itemCount ${if (itemCount == 1) "item" else "itens"}. Todos serão deletados permanentemente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        "Esta ação não pode ser desfeita.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        shoppingListViewModel.deleteList(list.id)
                        showDeleteListDialog = false
                        listToDelete = null
                    }
                ) {
                    Text(
                        "Deletar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteListDialog = false
                    listToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de download concluído (mostrado automaticamente)
    if (updateState is UpdateState.DownloadComplete && updateViewModel != null) {
        val state = updateState as UpdateState.DownloadComplete
        
        AlertDialog(
            onDismissRequest = { 
                // Não permitir fechar sem ação explícita
            },
            title = { Text("Download Concluído") },
            text = { 
                Text("A atualização foi baixada com sucesso. Deseja instalar agora?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateViewModel.installUpdate(state.apkFile)
                    }
                ) {
                    Text("Instalar Agora")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    updateViewModel.resetState()
                }) {
                    Text("Depois")
                }
            }
        )
    }
}

