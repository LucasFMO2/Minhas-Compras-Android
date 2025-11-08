package com.example.minhascompras.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
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
                Text("Todos os itens atuais serão substituídos por ${pendingImportItems!!.size} item(ns) do arquivo. Deseja continuar?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.importItens(pendingImportItems!!)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

