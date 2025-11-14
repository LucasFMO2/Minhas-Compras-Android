package com.example.minhascompras.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ItemCompra
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCompraCard(
    item: ItemCompra,
    onToggleComprado: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    var showContextMenu by remember { mutableStateOf(false) }
    val contextMenuState = remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        contextMenuState.value = true
                    }
                )
            },
        color = if (item.comprado) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Checkbox simples e direto
                Checkbox(
                    checked = item.comprado,
                    onCheckedChange = { onToggleComprado() },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Conteúdo principal
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Nome do item
                    Text(
                        text = item.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (item.comprado) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (item.comprado) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (item.comprado) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Informações adicionais (quantidade e preço) - apenas texto simples
                    if (item.quantidade > 1 || (item.preco != null && item.preco > 0)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.quantidade > 1) {
                                Text(
                                    text = "${item.quantidade}x",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            
                            if (item.preco != null && item.preco > 0) {
                                Text(
                                    text = formatador.format(item.preco * item.quantidade),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Sincronizar estado do menu
            LaunchedEffect(contextMenuState.value) {
                showContextMenu = contextMenuState.value
            }
            
            // Menu de contexto (aparece no clique longo)
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { 
                    showContextMenu = false
                    contextMenuState.value = false
                }
            ) {
                DropdownMenuItem(
                    text = { Text("Editar") },
                    onClick = {
                        onEdit()
                        showContextMenu = false
                        contextMenuState.value = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Excluir") },
                    onClick = {
                        onDelete()
                        showContextMenu = false
                        contextMenuState.value = false
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
        }
    }
}

