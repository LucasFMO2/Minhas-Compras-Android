package com.example.minhascompras.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ThemeMode
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
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Configurações",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção de Tema
            Text(
                "Aparência",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val themeMode by themeViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { themeViewModel.toggleThemeMode() }
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
                            style = MaterialTheme.typography.titleMedium,
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
                            ThemeMode.LIGHT -> Icons.Default.ShoppingCart
                            ThemeMode.DARK -> Icons.Default.Settings
                            ThemeMode.SYSTEM -> Icons.Default.Add
                        },
                        contentDescription = "Modo de Tema",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seção de Atualizações
            Text(
                "Atualizações",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
                            // Se já está atualizado, apenas resetar o estado para permitir nova verificação
                            updateViewModel.resetState()
                        }
                        is UpdateState.Error -> {
                            // Se for erro retryable, tentar novamente
                            if ((updateState as UpdateState.Error).isRetryable) {
                                updateViewModel.checkForUpdate(showNotification = false)
                            }
                        }
                        else -> {
                            updateViewModel.checkForUpdate(showNotification = false)
                        }
                    }
                },
                enabled = updateState !is UpdateState.Checking && updateState !is UpdateState.Downloading
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
                            style = MaterialTheme.typography.titleMedium,
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

            // Diálogo de versão atualizada
            if (updateState is UpdateState.UpToDate) {
                AlertDialog(
                    onDismissRequest = { updateViewModel.resetState() },
                    title = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Você está atualizado!")
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Parabéns! Você já está usando a versão mais recente do aplicativo.")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Versão atual: $currentVersion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { updateViewModel.resetState() }) {
                            Text("OK")
                        }
                    }
                )
            }

            // Diálogo de atualização disponível
            when (val state = updateState) {
                is UpdateState.UpdateAvailable -> {
                    AlertDialog(
                        onDismissRequest = { updateViewModel.resetState() },
                        title = { Text("Atualização Disponível") },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Nova versão: ${state.updateInfo.versionName}")
                                if (state.updateInfo.fileSize > 0) {
                                    val fileSizeMB = state.updateInfo.fileSize / (1024.0 * 1024.0)
                                    Text(
                                        "Tamanho: ${String.format("%.1f", fileSizeMB)} MB",
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
                                    updateViewModel.downloadUpdate(state.updateInfo)
                                }
                            ) {
                                Text("Baixar e Instalar")
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Backup e Restauração",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { exportLauncher.launch(exportFileName) }
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
                            style = MaterialTheme.typography.titleMedium,
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
                onClick = { importLauncher.launch("application/json") }
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
                            style = MaterialTheme.typography.titleMedium,
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
                }
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
                            style = MaterialTheme.typography.titleMedium,
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

