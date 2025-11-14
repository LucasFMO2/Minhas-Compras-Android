package com.example.minhascompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.utils.ResponsiveUtils

@Composable
fun StatisticCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    icon: ImageVector? = null
) {
    val defaultIcon = when {
        label.contains("Total", ignoreCase = true) -> Icons.Default.ShoppingCart
        label.contains("Comprado", ignoreCase = true) -> Icons.Default.CheckCircle
        label.contains("Pendente", ignoreCase = true) -> Icons.Default.Add
        else -> null
    }
    val displayIcon = icon ?: defaultIcon
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(ResponsiveUtils.getCardCornerRadius()),
        elevation = CardDefaults.cardElevation(defaultElevation = ResponsiveUtils.getCardElevation())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ResponsiveUtils.getStatisticCardHorizontalPadding(),
                    vertical = ResponsiveUtils.getVerticalPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
        ) {
            // Ícone com fundo circular
            if (displayIcon != null) {
                Box(
                    modifier = Modifier
                        .size(if (ResponsiveUtils.isSmallScreen()) 32.dp else 40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = displayIcon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(
                            if (ResponsiveUtils.isSmallScreen()) 18.dp else 24.dp
                        )
                    )
                }
            }
            
            // Valor
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Label
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

