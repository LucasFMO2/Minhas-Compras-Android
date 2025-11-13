package com.example.minhascompras.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ItemCategory
import com.example.minhascompras.data.ItemCompra
import com.example.minhascompras.ui.utils.ResponsiveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Double?, String) -> Unit,
    itemEdicao: ItemCompra? = null,
    modifier: Modifier = Modifier
) {
    var nomeItem by remember { mutableStateOf(itemEdicao?.nome ?: "") }
    var quantidade by remember { mutableStateOf(itemEdicao?.quantidade?.toString() ?: "1") }
    var preco by remember { mutableStateOf(itemEdicao?.preco?.toString() ?: "") }
    var categoriaSelecionada by remember { mutableStateOf(itemEdicao?.categoria ?: ItemCategory.OUTROS.displayName) }
    var expanded by remember { mutableStateOf(false) }
    
    LaunchedEffect(itemEdicao) {
        nomeItem = itemEdicao?.nome ?: ""
        quantidade = itemEdicao?.quantidade?.toString() ?: "1"
        preco = itemEdicao?.preco?.toString() ?: ""
        categoriaSelecionada = itemEdicao?.categoria ?: ItemCategory.OUTROS.displayName
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Ícone de carrinho de compras",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(
                    if (ResponsiveUtils.isSmallScreen()) 28.dp else 32.dp
                )
            )
        },
        title = { 
            Text(
                if (itemEdicao != null) "Editar Item" else "Adicionar Item",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = ResponsiveUtils.getTitleFontSize()
                )
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    if (ResponsiveUtils.isSmallScreen()) 16.dp else 20.dp
                ),
                modifier = modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nomeItem,
                    onValueChange = { nomeItem = it },
                    label = { Text("Nome do item") },
                    placeholder = { Text("Ex: Leite, Pão, Arroz...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Ícone de carrinho de compras",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                // Dropdown de Categoria
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = categoriaSelecionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        supportingText = {
                            Text(
                                "Selecione uma categoria",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ItemCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    categoriaSelecionada = category.displayName
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        if (ResponsiveUtils.isSmallScreen()) 8.dp else 12.dp
                    )
                ) {
                    OutlinedTextField(
                        value = quantidade,
                        onValueChange = { if (it.all { char -> char.isDigit() && it.length <= 3 }) quantidade = it },
                        label = { Text("Quantidade") },
                        placeholder = { Text("1") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        supportingText = {
                            Text(
                                "Digite a quantidade",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                    OutlinedTextField(
                        value = preco,
                        onValueChange = { newValue ->
                            // Permitir apenas dígitos, um ponto ou vírgula
                            val filtered = newValue.filter { char -> char.isDigit() || char == '.' || char == ',' }
                            // Garantir apenas um separador decimal
                            val withSingleDecimal = if (filtered.count { it == '.' || it == ',' } > 1) {
                                val firstDecimalIndex = filtered.indexOfFirst { it == '.' || it == ',' }
                                filtered.substring(0, firstDecimalIndex + 1) + filtered.substring(firstDecimalIndex + 1).filter { it.isDigit() }
                            } else {
                                filtered
                            }
                            preco = withSingleDecimal.replace(',', '.')
                        },
                        label = { Text("Preço (R$)") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        supportingText = {
                            Text(
                                "Opcional",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantidade.toIntOrNull()?.takeIf { it > 0 } ?: 1
                    val precoValue = preco.toDoubleOrNull()?.takeIf { it >= 0 }
                    if (nomeItem.isNotBlank() && categoriaSelecionada.isNotBlank()) {
                        onConfirm(nomeItem.trim(), qty, precoValue, categoriaSelecionada)
                        if (itemEdicao == null) {
                            nomeItem = ""
                            quantidade = "1"
                            preco = ""
                            categoriaSelecionada = ItemCategory.OUTROS.displayName
                        }
                    }
                },
                enabled = nomeItem.isNotBlank() && categoriaSelecionada.isNotBlank(),
                modifier = Modifier.height(ResponsiveUtils.getButtonHeight()),
                contentPadding = ResponsiveUtils.getButtonPadding()
            ) {
                Text(
                    if (itemEdicao != null) "Salvar" else "Adicionar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = ResponsiveUtils.getBodyFontSize()
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

