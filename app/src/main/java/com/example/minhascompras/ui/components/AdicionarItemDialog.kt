package com.example.minhascompras.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    modifier: Modifier = Modifier,
    itemNamesByFrequency: List<String> = emptyList(),
    onItemNameChanged: (String) -> Unit = {},
    onLoadItemSuggestions: () -> Unit = {},
    onItemSelected: (String) -> Unit = {},
    onClearForm: () -> Unit = {}
) {
    var nomeItem by remember { mutableStateOf(itemEdicao?.nome ?: "") }
    var quantidade by remember { mutableStateOf(itemEdicao?.quantidade?.toString() ?: "1") }
    var preco by remember { mutableStateOf(itemEdicao?.preco?.toString() ?: "") }
    var categoriaSelecionada by remember { mutableStateOf(itemEdicao?.categoria ?: ItemCategory.OUTROS.displayName) }
    var expanded by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    // Estados para validação
    var nomeError by remember { mutableStateOf(false) }
    var quantidadeError by remember { mutableStateOf(false) }
    var precoError by remember { mutableStateOf(false) }
    var categoriaError by remember { mutableStateOf(false) }
    
    LaunchedEffect(itemEdicao) {
        nomeItem = itemEdicao?.nome ?: ""
        quantidade = itemEdicao?.quantidade?.toString() ?: "1"
        preco = itemEdicao?.preco?.toString() ?: ""
        categoriaSelecionada = itemEdicao?.categoria ?: ItemCategory.OUTROS.displayName
    }
    
    LaunchedEffect(nomeItem) {
        onItemNameChanged(nomeItem)
        if (nomeItem.length >= 2) {
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }
    
    // Carregar sugestões quando o item é selecionado
    LaunchedEffect(nomeItem) {
        if (nomeItem.isNotEmpty()) {
            onItemSelected(nomeItem)
        }
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
                verticalArrangement = Arrangement.spacedBy(ResponsiveUtils.getMediumSpacing()),
                modifier = modifier.fillMaxWidth()
            ) {
                // Campo Nome com Autocompletar
                ExposedDropdownMenuBox(
                    expanded = showSuggestions && itemNamesByFrequency.isNotEmpty(),
                    onExpandedChange = {
                        if (it) onLoadItemSuggestions()
                        showSuggestions = it
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nomeItem,
                        onValueChange = {
                            nomeItem = it
                            showSuggestions = it.length >= 2
                        },
                        label = {
                            Text(
                                "Nome do item *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            )
                        },
                        placeholder = {
                            Text(
                                "Ex: Leite, Pão, Arroz...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = ResponsiveUtils.getBodyFontSize()
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Ícone de carrinho de compras",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (showSuggestions && itemNamesByFrequency.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (nomeError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            focusedLabelColor = if (nomeError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (nomeError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { showSuggestions = false })
                    )
                    
                    // Dropdown com sugestões
                    ExposedDropdownMenu(
                        expanded = showSuggestions && itemNamesByFrequency.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false },
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        val filteredSuggestions = itemNamesByFrequency.filter {
                            nome -> nome.lowercase().contains(nomeItem.lowercase())
                        }.take(5)
                        
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        suggestion,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = ResponsiveUtils.getBodyFontSize()
                                        )
                                    )
                                },
                                onClick = {
                                    nomeItem = suggestion
                                    showSuggestions = false
                                    onItemSelected(suggestion)
                                }
                            )
                        }
                    }
                }
                
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
                        label = { 
                            Text(
                                "Categoria *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            ) 
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        supportingText = {
                            Text(
                                "Selecione uma categoria",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            )
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        ItemCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        category.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = ResponsiveUtils.getBodyFontSize()
                                        )
                                    ) 
                                },
                                onClick = {
                                    categoriaSelecionada = category.displayName
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Quantidade e Preço em linha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ResponsiveUtils.getSpacing())
                ) {
                    OutlinedTextField(
                        value = quantidade,
                        onValueChange = { if (it.all { char -> char.isDigit() && it.length <= 3 }) quantidade = it },
                        label = { 
                            Text(
                                "Quantidade",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            ) 
                        },
                        placeholder = { 
                            Text(
                                "1",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = ResponsiveUtils.getBodyFontSize()
                                )
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        supportingText = {
                            Text(
                                "Digite a quantidade",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            )
                        },
                        shape = MaterialTheme.shapes.medium
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
                        label = { 
                            Text(
                                "Preço (R$)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            ) 
                        },
                        placeholder = { 
                            Text(
                                "0.00",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = ResponsiveUtils.getBodyFontSize()
                                )
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        supportingText = {
                            Text(
                                "Opcional",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = ResponsiveUtils.getLabelFontSize()
                                )
                            )
                        },
                        shape = MaterialTheme.shapes.medium
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
                contentPadding = ResponsiveUtils.getButtonPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    if (itemEdicao != null) "Salvar" else "Adicionar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = ResponsiveUtils.getBodyFontSize()
                    ),
                    fontWeight = FontWeight.SemiBold
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

