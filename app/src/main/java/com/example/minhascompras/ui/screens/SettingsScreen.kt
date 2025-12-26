package com.example.minhascompras.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.minhascompras.data.ThemeMode
import com.example.minhascompras.data.NotificationPreferencesManager
import com.example.minhascompras.notifications.NotificationScheduler
import com.example.minhascompras.ui.utils.ResponsiveUtils
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
import com.example.minhascompras.ui.viewmodel.ThemeViewModel
import com.example.minhascompras.ui.viewmodel.UpdateViewModel
import com.example.minhascompras.ui.viewmodel.UpdateState
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import com.example.minhascompras.data.ItemCompra
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Componente de seleção de horário estilo carrossel circular com scroll vertical.
 * Design inspirado no iOS picker com navegação infinita e indicador visual.
 */
@Composable
fun ScrollableTimePicker(
    selectedHour: Int,
    selectedMinute: Int,
    onHourSelected: (Int) -> Unit,
    onMinuteSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hourListState = rememberLazyListState()
    val minuteListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current
    val itemHeightPx = with(density) { 48.dp.toPx() }
    val viewportHeightPx = with(density) { 192.dp.toPx() }
    val itemCenterOffset = ((viewportHeightPx - itemHeightPx) / 2).toInt()

    // Constantes para carrossel circular
    val HOURS_CYCLE_SIZE = 24
    val MINUTES_CYCLE_SIZE = 60
    val TOTAL_HOUR_ITEMS = HOURS_CYCLE_SIZE * 3  // 3 ciclos para navegação infinita
    val TOTAL_MINUTE_ITEMS = MINUTES_CYCLE_SIZE * 3

    // Funções para converter índice circular em valor real
    fun getHourFromIndex(index: Int): Int = index % HOURS_CYCLE_SIZE
    fun getMinuteFromIndex(index: Int): Int = index % MINUTES_CYCLE_SIZE

    // Funções para converter valor real em índice do meio do carrossel
    fun getHourIndex(hour: Int): Int = HOURS_CYCLE_SIZE + hour  // Meio do segundo ciclo
    fun getMinuteIndex(minute: Int): Int = MINUTES_CYCLE_SIZE + minute

    // Scroll inicial para o horário selecionado (posição no meio do carrossel)
    LaunchedEffect(Unit) {
        hourListState.scrollToItem(getHourIndex(selectedHour), itemCenterOffset)
        minuteListState.scrollToItem(getMinuteIndex(selectedMinute), itemCenterOffset)
    }
    
    // Detectar item no centro após parar de rolar (horas)
    LaunchedEffect(hourListState.isScrollInProgress) {
        snapshotFlow {
            hourListState.isScrollInProgress to
            (hourListState.firstVisibleItemIndex to hourListState.firstVisibleItemScrollOffset)
        }.collect { (isScrolling, indexOffset) ->
            if (!isScrolling) {
                delay(50) // Pequeno delay para garantir que parou
                val (index, offset) = indexOffset
                val centerItemIndex = calculateCenterItem(index, offset, itemHeightPx, viewportHeightPx / 2)
                val actualHour = getHourFromIndex(centerItemIndex)

                if (actualHour != selectedHour) {
                    onHourSelected(actualHour)
                    // Snap suave para o centro
                    coroutineScope.launch {
                        hourListState.animateScrollToItem(centerItemIndex, itemCenterOffset)
                    }
                }
            }
        }
    }

    // Detectar item no centro após parar de rolar (minutos)
    LaunchedEffect(minuteListState.isScrollInProgress) {
        snapshotFlow {
            minuteListState.isScrollInProgress to
            (minuteListState.firstVisibleItemIndex to minuteListState.firstVisibleItemScrollOffset)
        }.collect { (isScrolling, indexOffset) ->
            if (!isScrolling) {
                delay(50) // Pequeno delay para garantir que parou
                val (index, offset) = indexOffset
                val centerItemIndex = calculateCenterItem(index, offset, itemHeightPx, viewportHeightPx / 2)
                val actualMinute = getMinuteFromIndex(centerItemIndex)

                if (actualMinute != selectedMinute) {
                    onMinuteSelected(actualMinute)
                    // Snap suave para o centro
                    coroutineScope.launch {
                        minuteListState.animateScrollToItem(centerItemIndex, itemCenterOffset)
                    }
                }
            }
        }
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(192.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coluna de Horas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            LazyColumn(
                state = hourListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(TOTAL_HOUR_ITEMS) { index ->
                    val hour = getHourFromIndex(index)
                    TimePickerItem(
                        value = hour,
                        isSelected = hour == selectedHour,
                        onSelected = {
                            onHourSelected(hour)
                            coroutineScope.launch {
                                hourListState.animateScrollToItem(index, itemCenterOffset)
                            }
                        }
                    )
                }
            }
            
            // Background fixo para item selecionado (horas) - alinhado com os dois pontos
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            )
            
            // Indicador de seleção no centro (borda)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .align(Alignment.Center)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
        
        // Separador ":"
        Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Coluna de Minutos
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            LazyColumn(
                state = minuteListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(TOTAL_MINUTE_ITEMS) { index ->
                    val minute = getMinuteFromIndex(index)
                    TimePickerItem(
                        value = minute,
                        isSelected = minute == selectedMinute,
                        onSelected = {
                            onMinuteSelected(minute)
                            coroutineScope.launch {
                                minuteListState.animateScrollToItem(index, itemCenterOffset)
                            }
                        }
                    )
                }
            }
            
            // Background fixo para item selecionado (minutos) - alinhado com os dois pontos
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            )
            
            // Indicador de seleção no centro (borda)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
                    .align(Alignment.Center)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

/**
 * Item individual do seletor de horário
 */
@Composable
private fun TimePickerItem(
    value: Int,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(0.8f)
            .clickable(onClick = onSelected),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%02d", value),
            style = if (isSelected) {
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }
        )
    }
}

/**
 * Calcula qual item está no centro do viewport
 */
private fun calculateCenterItem(
    firstVisibleIndex: Int,
    scrollOffset: Int,
    itemHeight: Float,
    centerOffset: Float
): Int {
    val adjustedOffset = scrollOffset + centerOffset
    return (firstVisibleIndex + (adjustedOffset / itemHeight).toInt()).coerceAtLeast(0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ListaComprasViewModel,
    themeViewModel: ThemeViewModel,
    updateViewModel: UpdateViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExportSuccess by remember { mutableStateOf(false) }
    var showExportError by remember { mutableStateOf(false) }
    var exportErrorMessage by remember { mutableStateOf("") }
    var showImportConfirmation by remember { mutableStateOf(false) }
    var showImportSuccess by remember { mutableStateOf(false) }
    var showImportError by remember { mutableStateOf(false) }
    var importErrorMessage by remember { mutableStateOf("") }
    var pendingImportItems by remember { mutableStateOf<List<ItemCompra>?>(null) }
    var shareText by remember { mutableStateOf("") }
    var showUpToDateDialog by remember { mutableStateOf(false) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val itens = viewModel.getAllItensForExport()
                    val json = Json { prettyPrint = true }
                    val jsonString = json.encodeToString(ListSerializer(ItemCompra.serializer()), itens)
                    
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    showExportSuccess = true
                } catch (e: Exception) {
                    exportErrorMessage = e.message ?: "Erro ao exportar dados"
                    showExportError = true
                }
            }
        }
    }

    val exportFileName = remember {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        "minhas_compras_${dateFormat.format(Date())}.json"
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(it)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            reader.readText()
                        }
                    } ?: throw Exception("Não foi possível ler o arquivo")

                    val json = Json { 
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                    val itens = json.decodeFromString(ListSerializer(ItemCompra.serializer()), jsonString)
                    
                    if (itens.isNotEmpty()) {
                        pendingImportItems = itens
                        showImportConfirmation = true
                    } else {
                        importErrorMessage = "O arquivo está vazio ou não contém dados válidos"
                        showImportError = true
                    }
                } catch (e: kotlinx.serialization.SerializationException) {
                    importErrorMessage = "Erro ao ler o arquivo JSON. Verifique se o arquivo está no formato correto."
                    showImportError = true
                } catch (e: Exception) {
                    importErrorMessage = e.message ?: "Erro ao importar dados"
                    showImportError = true
                }
            }
        }
    }

    LaunchedEffect(showExportSuccess) {
        if (showExportSuccess) {
            kotlinx.coroutines.delay(2000)
            showExportSuccess = false
        }
    }

    LaunchedEffect(showExportError) {
        if (showExportError) {
            kotlinx.coroutines.delay(3000)
            showExportError = false
        }
    }

    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = { showExportSuccess = false },
            title = { Text("Sucesso") },
            text = { Text("Dados exportados com sucesso!") },
            confirmButton = {
                TextButton(onClick = { showExportSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showExportError) {
        AlertDialog(
            onDismissRequest = { showExportError = false },
            title = { Text("Erro") },
            text = { Text(exportErrorMessage) },
            confirmButton = {
                TextButton(onClick = { showExportError = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Diálogo de confirmação de importação
    if (showImportConfirmation && pendingImportItems != null) {
        AlertDialog(
            onDismissRequest = { 
                showImportConfirmation = false
                pendingImportItems = null
            },
            title = { Text("Confirmar Importação") },
            text = { 
                pendingImportItems?.let { items ->
                    Text("Todos os itens atuais serão substituídos por ${items.size} item(ns) do arquivo. Deseja continuar?")
                } ?: Text("Erro: dados de importação não disponíveis")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportItems?.let { items ->
                            scope.launch {
                                try {
                                    viewModel.importItens(items)
                                    showImportSuccess = true
                                    showImportConfirmation = false
                                    pendingImportItems = null
                                } catch (e: Exception) {
                                    importErrorMessage = e.message ?: "Erro ao importar dados"
                                    showImportError = true
                                    showImportConfirmation = false
                                    pendingImportItems = null
                                }
                            }
                        }
                    }
                ) {
                    Text("Importar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showImportConfirmation = false
                        pendingImportItems = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showImportSuccess) {
        AlertDialog(
            onDismissRequest = { showImportSuccess = false },
            title = { Text("Sucesso") },
            text = { Text("Dados importados com sucesso!") },
            confirmButton = {
                TextButton(onClick = { showImportSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showImportError) {
        AlertDialog(
            onDismissRequest = { showImportError = false },
            title = { Text("Erro") },
            text = { Text(importErrorMessage) },
            confirmButton = {
                TextButton(onClick = { showImportError = false }) {
                    Text("OK")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(
                                if (ResponsiveUtils.isSmallScreen()) 18.dp else 20.dp
                            )
                        )
                        Text(
                            "Configurações",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = ResponsiveUtils.getTitleFontSize()
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(ResponsiveUtils.getHorizontalPadding()),
            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSectionSpacing())
        ) {
            // Seção de Tema
            Text(
                "Aparência",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = ResponsiveUtils.getTitleFontSize()
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = ResponsiveUtils.getSmallSpacing())
            )

            val themeMode by themeViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { themeViewModel.toggleThemeMode() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Modo de Tema",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            when (themeMode) {
                                ThemeMode.LIGHT -> "Tema Claro"
                                ThemeMode.DARK -> "Tema Escuro"
                                ThemeMode.SYSTEM -> "Seguir Sistema"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = when (themeMode) {
                            ThemeMode.LIGHT -> Icons.Default.LightMode
                            ThemeMode.DARK -> Icons.Default.DarkMode
                            ThemeMode.SYSTEM -> Icons.Default.PhoneAndroid
                        },
                        contentDescription = "Modo de Tema",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Seção de Atualizações
            Text(
                "Atualizações",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = ResponsiveUtils.getTitleFontSize()
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = ResponsiveUtils.getSmallSpacing())
            )

            val updateState by updateViewModel.updateState.collectAsState()
            val currentVersion = updateViewModel.getCurrentVersionName()

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    when (updateState) {
                        is UpdateState.Checking, is UpdateState.Downloading -> {
                            // Não fazer nada durante verificação ou download
                        }
                        is UpdateState.UpToDate -> {
                            // Mostrar o diálogo apenas quando o usuário clicar no card
                            showUpToDateDialog = true
                        }
                        is UpdateState.Error -> {
                            // Se for erro retryable, tentar novamente
                            if ((updateState as UpdateState.Error).isRetryable) {
                                updateViewModel.checkForUpdate(showNotification = false)
                            }
                        }
                        else -> {
                            // Verificação manual - forçar verificação mesmo se muito recente
                            updateViewModel.checkForUpdate(showNotification = false, force = true)
                        }
                    }
                },
                enabled = updateState !is UpdateState.Checking && updateState !is UpdateState.Downloading,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Verificar Atualizações",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column {
                            Text(
                                when (val state = updateState) {
                                    is UpdateState.Idle -> "Versão atual: $currentVersion"
                                    is UpdateState.Checking -> "Verificando atualizações..."
                                    is UpdateState.UpToDate -> "Você está na versão mais recente! ✓"
                                    is UpdateState.UpdateAvailable -> "Nova versão disponível: ${state.updateInfo.versionName}"
                                    is UpdateState.Downloading -> {
                                        if (state.totalBytes > 0) {
                                            val downloadedMB = state.downloadedBytes / (1024.0 * 1024.0)
                                            val totalMB = state.totalBytes / (1024.0 * 1024.0)
                                            "Baixando: ${state.progress}% (${String.format("%.1f", downloadedMB)} MB / ${String.format("%.1f", totalMB)} MB)"
                                        } else {
                                            "Baixando: ${state.progress}%"
                                        }
                                    }
                                    is UpdateState.DownloadComplete -> "Download concluído! Toque para instalar"
                                    is UpdateState.Error -> state.message
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (updateState) {
                                    is UpdateState.UpToDate -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            // Mostrar tamanho do arquivo quando disponível
                            val availableState = updateState
                            if (availableState is UpdateState.UpdateAvailable) {
                                val fileSizeMB = availableState.updateInfo.fileSize / (1024.0 * 1024.0)
                                if (fileSizeMB > 0) {
                                    Text(
                                        "Tamanho: ${String.format("%.1f", fileSizeMB)} MB",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        if (updateState is UpdateState.Downloading) {
                            IconButton(
                                onClick = { updateViewModel.cancelDownload() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar download",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Icon(
                            imageVector = when (updateState) {
                                is UpdateState.Checking -> Icons.Default.Refresh
                                is UpdateState.UpToDate -> Icons.Default.CheckCircle
                                is UpdateState.UpdateAvailable -> Icons.Default.Add
                                is UpdateState.DownloadComplete -> Icons.Default.Settings
                                is UpdateState.Error -> Icons.Default.Refresh
                                else -> Icons.Default.Settings
                            },
                            contentDescription = "Atualizações",
                            tint = when (updateState) {
                                is UpdateState.UpToDate -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }

            // Diálogos de atualização
            // Renderizar diálogo UpToDate apenas quando o usuário clicar no card
            if (showUpToDateDialog && updateState is UpdateState.UpToDate) {
                AlertDialog(
                    onDismissRequest = { 
                        showUpToDateDialog = false
                        updateViewModel.resetState()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { 
                        Text("Você já está na versão mais recente!")
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Não há atualizações disponíveis no momento. Você já está usando a versão mais recente do aplicativo.")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Versão atual instalada: $currentVersion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            showUpToDateDialog = false
                            updateViewModel.resetState()
                        }) {
                            Text("Entendi")
                        }
                    }
                )
            }
            
            when (val state = updateState) {
                // UpToDate é tratado acima separadamente
                is UpdateState.UpToDate -> {
                    // Já renderizado acima
                }
                is UpdateState.UpdateAvailable -> {
                    // Verificar se a versão é realmente maior que a atual
                    val currentVersionCode = updateViewModel.getCurrentVersionCode()
                    val currentVersionName = updateViewModel.getCurrentVersionName()
                    val canDownload = state.updateInfo.versionCode > currentVersionCode
                    
                    AlertDialog(
                        onDismissRequest = { updateViewModel.resetState() },
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
                                    } else {
                                        updateViewModel.resetState()
                                    }
                                },
                                enabled = canDownload
                            ) {
                                Text(if (canDownload) "Baixar e Instalar" else "Fechar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateViewModel.resetState() }) {
                                Text("Depois")
                            }
                        }
                    )
                }
                is UpdateState.DownloadComplete -> {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Download Concluído") },
                        text = { Text("A atualização foi baixada. Deseja instalar agora?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    updateViewModel.installUpdate(state.apkFile)
                                }
                            ) {
                                Text("Instalar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateViewModel.resetState() }) {
                                Text("Depois")
                            }
                        }
                    )
                }
                is UpdateState.Error -> {
                    AlertDialog(
                        onDismissRequest = { updateViewModel.resetState() },
                        title = { Text("Erro") },
                        text = { Text(state.message) },
                        confirmButton = {
                            if (state.isRetryable) {
                                Button(
                                    onClick = {
                                        updateViewModel.checkForUpdate(showNotification = false)
                                    }
                                ) {
                                    Text("Tentar Novamente")
                                }
                            } else {
                                TextButton(onClick = { updateViewModel.resetState() }) {
                                    Text("OK")
                                }
                            }
                        },
                        dismissButton = {
                            if (state.isRetryable) {
                                TextButton(onClick = { updateViewModel.resetState() }) {
                                    Text("Cancelar")
                                }
                            }
                        }
                    )
                }
                else -> {}
            }

            // Barra de progresso durante download com informações detalhadas
            val downloadingState = updateState
            if (downloadingState is UpdateState.Downloading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { downloadingState.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${downloadingState.progress}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (downloadingState.totalBytes > 0) {
                            Text(
                                "${String.format("%.1f", downloadingState.downloadedBytes / (1024.0 * 1024.0))} MB / ${String.format("%.1f", downloadingState.totalBytes / (1024.0 * 1024.0))} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "${String.format("%.1f", downloadingState.downloadedBytes / (1024.0 * 1024.0))} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Seção de Notificações
            Text(
                "Notificações",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = ResponsiveUtils.getTitleFontSize()
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = ResponsiveUtils.getSmallSpacing())
            )

            val notificationPrefsManager = remember { NotificationPreferencesManager(context) }
            val dailyReminderEnabled by notificationPrefsManager.isDailyReminderEnabled().collectAsState(initial = false)
            val dailyReminderHour by notificationPrefsManager.getDailyReminderHour().collectAsState(initial = 9)
            val dailyReminderMinute by notificationPrefsManager.getDailyReminderMinute().collectAsState(initial = 0)
            val completionEnabled by notificationPrefsManager.isCompletionNotificationEnabled().collectAsState(initial = true)
            val pendingItemsEnabled by notificationPrefsManager.isPendingItemsNotificationEnabled().collectAsState(initial = true)
            val pendingItemsDays by notificationPrefsManager.getPendingItemsDaysThreshold().collectAsState(initial = 7)
            
            var showTimePicker by remember { mutableStateOf(false) }
            var showDaysPicker by remember { mutableStateOf(false) }

            // Lembrete Diário
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Lembrete Diário",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Receba um lembrete diário sobre suas compras",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = dailyReminderEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    notificationPrefsManager.setDailyReminderEnabled(enabled)
                                    if (enabled) {
                                        NotificationScheduler.scheduleDailyReminder(
                                            context,
                                            dailyReminderHour,
                                            dailyReminderMinute,
                                            true
                                        )
                                    } else {
                                        NotificationScheduler.scheduleDailyReminder(
                                            context,
                                            0,
                                            0,
                                            false
                                        )
                                    }
                                }
                            }
                        )
                    }
                    if (dailyReminderEnabled) {
                        Card(
                            onClick = { showTimePicker = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Horário: ${String.format("%02d:%02d", dailyReminderHour, dailyReminderMinute)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Alterar horário",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Notificação de Conclusão
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        val newValue = !completionEnabled
                        notificationPrefsManager.setCompletionNotificationEnabled(newValue)
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Notificação de Conclusão",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Notificar quando você completar uma lista",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = completionEnabled,
                        onCheckedChange = {
                            scope.launch {
                                notificationPrefsManager.setCompletionNotificationEnabled(it)
                            }
                        }
                    )
                }
            }

            // Notificação de Itens Pendentes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Itens Pendentes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Alertar sobre itens pendentes há vários dias",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = pendingItemsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    notificationPrefsManager.setPendingItemsNotificationEnabled(enabled)
                                    NotificationScheduler.schedulePendingItemsCheck(context, enabled)
                                }
                            }
                        )
                    }
                    if (pendingItemsEnabled) {
                        Card(
                            onClick = { showDaysPicker = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Text(
                                    "Alertar após: $pendingItemsDays dias",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Alterar dias",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Time Picker Dialog - Melhorado com preview
            if (showTimePicker) {
                var selectedHour by remember { mutableStateOf(dailyReminderHour) }
                var selectedMinute by remember { mutableStateOf(dailyReminderMinute) }

                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = {
                        Text(
                            "Selecionar Horário do Lembrete",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Preview do horário selecionado
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Horário selecionado: ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                )
                            }

                            // Seletor de horário
                            ScrollableTimePicker(
                                selectedHour = selectedHour,
                                selectedMinute = selectedMinute,
                                onHourSelected = { selectedHour = it },
                                onMinuteSelected = { selectedMinute = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    notificationPrefsManager.setDailyReminderTime(selectedHour, selectedMinute)
                                    NotificationScheduler.scheduleDailyReminder(
                                        context,
                                        selectedHour,
                                        selectedMinute,
                                        dailyReminderEnabled
                                    )
                                    showTimePicker = false
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp) // Bordas mais arredondadas
                )
            }

            // Days Picker Dialog
            if (showDaysPicker) {
                var selectedDays by remember { mutableStateOf(pendingItemsDays) }
                
                AlertDialog(
                    onDismissRequest = { showDaysPicker = false },
                    title = { Text("Dias para Alerta") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                        ) {
                            Text("Alertar sobre itens pendentes há mais de:")
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { 
                                    if (selectedDays > 1) selectedDays-- 
                                }) {
                                    Text("−", style = MaterialTheme.typography.displayLarge)
                                }
                                Text(
                                    "$selectedDays dias",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                IconButton(onClick = { 
                                    if (selectedDays < 30) selectedDays++ 
                                }) {
                                    Text("+", style = MaterialTheme.typography.displayLarge)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch {
                                notificationPrefsManager.setPendingItemsDaysThreshold(selectedDays)
                                showDaysPicker = false
                            }
                        }) {
                            Text("Confirmar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDaysPicker = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Text(
                "Backup e Restauração",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = ResponsiveUtils.getTitleFontSize()
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = ResponsiveUtils.getSmallSpacing())
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { exportLauncher.launch(exportFileName) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Exportar Dados",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Salvar sua lista de compras em um arquivo JSON",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Exportar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { importLauncher.launch("application/json") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Importar Dados",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Restaurar sua lista de compras de um arquivo JSON",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Importar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        try {
                            shareText = viewModel.getShareableText()
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Lista"))
                        } catch (e: Exception) {
                            // Tratar erro silenciosamente ou mostrar mensagem
                        }
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
                shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Compartilhar Lista",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Compartilhar sua lista de compras como texto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartilhar"
                    )
                }
            }
        }
    }
}

