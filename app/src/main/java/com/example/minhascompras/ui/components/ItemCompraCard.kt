package com.example.minhascompras.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.ui.utils.ResponsiveUtils
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
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.comprado) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ResponsiveUtils.getCardElevation()
        ),
        shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveUtils.getCardPadding()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing())
        ) {
            // Checkbox com animação
            AnimatedContent(
                targetState = item.comprado,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(200)
                    ) togetherWith fadeOut(animationSpec = tween(200)) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(200)
                    )
                },
                label = "checkbox"
            ) { comprado ->
                Box(
                    modifier = Modifier
                        .size(ResponsiveUtils.getMinimumTouchTarget())
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (comprado) {
                        IconButton(
                            onClick = { onToggleComprado() },
                            modifier = Modifier.size(ResponsiveUtils.getMinimumTouchTarget())
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Item comprado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(ResponsiveUtils.getIconSize())
                            )
                        }
                    } else {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggleComprado() },
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
            
            // Conteúdo principal
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = ResponsiveUtils.getSmallSpacing()),
                verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
            ) {
                // Nome do item
                Text(
                    text = item.nome,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = ResponsiveUtils.getBodyFontSize()
                    ),
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.comprado) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (item.comprado) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Informações adicionais (quantidade e preço)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.quantidade > 1) {
                        Surface(
                            shape = RoundedCornerShape(ResponsiveUtils.getSmallSpacing()),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "×${item.quantidade}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = ResponsiveUtils.getLabelFontSize()
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    
                    if (item.preco != null && item.preco > 0) {
                        Surface(
                            shape = RoundedCornerShape(ResponsiveUtils.getSmallSpacing()),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatador.format(item.preco),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = ResponsiveUtils.getLabelFontSize()
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Botões de ação
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
            ) {
                val buttonSize = ResponsiveUtils.getMinimumTouchTarget()
                val iconButtonSize = if (ResponsiveUtils.isSmallScreen()) 18.dp else 20.dp
                
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(buttonSize),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(iconButtonSize)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(buttonSize),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        modifier = Modifier.size(iconButtonSize)
                    )
                }
            }
        }
    }
}

