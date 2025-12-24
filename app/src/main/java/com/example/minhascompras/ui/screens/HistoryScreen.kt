package com.example.minhascompras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ShoppingListHistory
import com.example.minhascompras.ui.utils.ResponsiveUtils
import com.example.minhascompras.ui.viewmodel.HistoryViewModel
import com.example.minhascompras.utils.DebugLogger
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onReuseList: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // #region agent log
    androidx.compose.runtime.LaunchedEffect(Unit) {
        DebugLogger.log(
            location = "HistoryScreen.kt:HistoryScreen",
            message = "HistoryScreen composable entered",
            data = emptyMap(),
            hypothesisId = "D"
        )
    }
    // #endregion
    
    val historyLists by viewModel.historyLists.collectAsState()
    val isReusing by viewModel.isReusing.collectAsState()
    
    // #region agent log
    androidx.compose.runtime.LaunchedEffect(historyLists.size) {
        DebugLogger.log(
            location = "HistoryScreen.kt:HistoryScreen",
            message = "historyLists state updated",
            data = mapOf(
                "historyListsCount" to historyLists.size,
                "historyListIds" to historyLists.map { it.id },
                "historyListNames" to historyLists.map { it.listName }
            ),
            hypothesisId = "D"
        )
    }
    // #endregion
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Histórico de Compras",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = ResponsiveUtils.getTitleFontSize()
                        )
                    )
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
        },
        modifier = modifier
    ) { paddingValues ->
        if (historyLists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Nenhuma lista arquivada",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Listas arquivadas aparecerão aqui",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(ResponsiveUtils.getHorizontalPadding()),
                verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing())
            ) {
                items(
                    items = historyLists,
                    key = { it.id }
                ) { history ->
                    HistoryItemCard(
                        history = history,
                        dateFormat = dateFormat,
                        showListName = true, // Sempre mostrar nome da lista
                        isReusing = isReusing,
                        onDelete = { viewModel.deleteHistory(history.id) },
                        onReuse = { 
                            // Navegar apenas após a operação completar (via callback)
                            viewModel.reuseHistoryList(history.id) {
                                onReuseList(history.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    history: ShoppingListHistory,
    dateFormat: SimpleDateFormat,
    @Suppress("UNUSED_PARAMETER") showListName: Boolean = false,
    isReusing: Boolean = false,
    onDelete: () -> Unit,
    onReuse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius()),
        elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveUtils.getCardPadding()),
            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = history.listName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = ResponsiveUtils.getBodyFontSize()
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(ResponsiveUtils.getSmallSpacing()))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormat.format(Date(history.completionDate)),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = ResponsiveUtils.getLabelFontSize()
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(ResponsiveUtils.getMinimumTouchTarget()),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        modifier = Modifier.size(
                            if (ResponsiveUtils.isSmallScreen()) 18.dp else 20.dp
                        )
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onReuse,
                    enabled = !isReusing,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.height(ResponsiveUtils.getButtonHeight()),
                    contentPadding = ResponsiveUtils.getButtonPadding()
                ) {
                    if (isReusing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(
                                if (ResponsiveUtils.isSmallScreen()) 14.dp else 16.dp
                            ),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reutilizar lista",
                            modifier = Modifier.size(
                                if (ResponsiveUtils.isSmallScreen()) 16.dp else 18.dp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(
                        if (ResponsiveUtils.isSmallScreen()) 6.dp else 8.dp
                    ))
                    Text(
                        if (isReusing) "Carregando..." else "Reutilizar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = ResponsiveUtils.getBodyFontSize()
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

