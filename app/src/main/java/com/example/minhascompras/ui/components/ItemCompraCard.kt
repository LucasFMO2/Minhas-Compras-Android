package com.example.minhascompras.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.comprado) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.comprado) 
                ResponsiveUtils.getCardElevation() 
            else 
                ResponsiveUtils.getCardElevation()
        ),
        shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ResponsiveUtils.getCardPadding()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing())
            ) {
                // Ícone visual
                val iconSize = if (ResponsiveUtils.isSmallScreen()) 36.dp else if (ResponsiveUtils.isMediumScreen()) 40.dp else 48.dp
                Box(
                    modifier = Modifier
                        .size(iconSize)
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
                        contentDescription = "Ícone de item de compra",
                        modifier = Modifier.size(if (ResponsiveUtils.isSmallScreen()) 18.dp else ResponsiveUtils.getIconSize()),
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = ResponsiveUtils.getBodyFontSize()
                        ),
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
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(ResponsiveUtils.getSmallSpacing()))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
                    ) {
                        if (item.quantidade > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Qtd: ${item.quantidade}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = ResponsiveUtils.getLabelFontSize()
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(
                                        horizontal = if (ResponsiveUtils.isSmallScreen()) 6.dp else 8.dp,
                                        vertical = if (ResponsiveUtils.isSmallScreen()) 3.dp else 4.dp
                                    )
                                )
                            }
                        }
                        if (item.preco != null && item.preco > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = formatador.format(item.preco),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = ResponsiveUtils.getLabelFontSize()
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(
                                        horizontal = if (ResponsiveUtils.isSmallScreen()) 6.dp else 8.dp,
                                        vertical = if (ResponsiveUtils.isSmallScreen()) 3.dp else 4.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Checkbox e botões de ação
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
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
                
                val buttonSize = ResponsiveUtils.getMinimumTouchTarget()
                val iconButtonSize = if (ResponsiveUtils.isSmallScreen()) 18.dp else 20.dp
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(buttonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(iconButtonSize)
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(buttonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(iconButtonSize)
                    )
                }
            }
        }
    }
}

