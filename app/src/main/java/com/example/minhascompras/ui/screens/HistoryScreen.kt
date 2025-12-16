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
    val historyLists by viewModel.historyLists.collectAsState()
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
                            contentDescription = "Voltar"
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
                        "Nenhum histórico encontrado",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Listas completas serão arquivadas automaticamente aqui",
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
                        onDelete = { viewModel.deleteHistory(history.id) },
                        onReuse = { 
                            viewModel.reuseHistoryList(history.id)
                            onReuseList(history.id)
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
                    Text(
                        text = dateFormat.format(Date(history.completionDate)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = ResponsiveUtils.getLabelFontSize()
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(ResponsiveUtils.getButtonHeight()),
                    contentPadding = ResponsiveUtils.getButtonPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reutilizar lista",
                        modifier = Modifier.size(
                            if (ResponsiveUtils.isSmallScreen()) 16.dp else 18.dp
                        )
                    )
                    Spacer(modifier = Modifier.width(
                        if (ResponsiveUtils.isSmallScreen()) 6.dp else 8.dp
                    ))
                    Text(
                        "Reutilizar",
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

