package com.example.minhascompras.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ItemCompra

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCompraCard(
    item: ItemCompra,
    onToggleComprado: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.comprado) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.comprado) 1.dp else 3.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícone visual
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.comprado) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (item.comprado) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nome,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (item.comprado) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        color = if (item.comprado) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (item.quantidade > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Qtd: ${item.quantidade}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Checkbox e botão deletar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedContent(
                    targetState = item.comprado,
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                    label = "checkbox"
                ) { comprado ->
                    Checkbox(
                        checked = comprado,
                        onCheckedChange = { onToggleComprado() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

