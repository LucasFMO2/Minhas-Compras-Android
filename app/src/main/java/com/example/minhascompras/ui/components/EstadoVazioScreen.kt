package com.example.minhascompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.utils.ResponsiveUtils

@Composable
fun EstadoVazioScreen(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getMediumSpacing()),
            modifier = Modifier.padding(ResponsiveUtils.getHorizontalPadding())
        ) {
            // Ícone com fundo circular destacado
            val iconContainerSize = if (ResponsiveUtils.isSmallScreen()) 120.dp else 160.dp
            val iconSize = if (ResponsiveUtils.isSmallScreen()) 60.dp else 80.dp
            
            Box(
                modifier = Modifier
                    .size(iconContainerSize)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Ícone de carrinho de compras vazio",
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
            
            // Mensagens
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSmallSpacing())
            ) {
                Text(
                    text = "Sua lista está vazia!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = ResponsiveUtils.getTitleFontSize() * 1.2f
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Comece adicionando itens à sua lista de compras e organize suas compras de forma simples e eficiente",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = ResponsiveUtils.getBodyFontSize()
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = ResponsiveUtils.getHorizontalPadding())
                )
            }
            
            // Botão de ação destacado
            FilledTonalButton(
                onClick = onAddClick,
                modifier = Modifier
                    .padding(top = ResponsiveUtils.getSpacing())
                    .height(ResponsiveUtils.getButtonHeight()),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = ResponsiveUtils.getButtonPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ResponsiveUtils.getIconSize())
                )
                Spacer(modifier = Modifier.width(ResponsiveUtils.getSmallSpacing()))
                Text(
                    if (ResponsiveUtils.isSmallScreen()) "Adicionar Item" else "Adicionar Primeiro Item",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = ResponsiveUtils.getBodyFontSize()
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

