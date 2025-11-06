package com.example.minhascompras.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.data.ItemCompra

@Composable
fun AdicionarItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Double?) -> Unit,
    itemEdicao: ItemCompra? = null,
    modifier: Modifier = Modifier
) {
    var nomeItem by remember { mutableStateOf(itemEdicao?.nome ?: "") }
    var quantidade by remember { mutableStateOf(itemEdicao?.quantidade?.toString() ?: "1") }
    var preco by remember { mutableStateOf(itemEdicao?.preco?.toString() ?: "") }
    
    LaunchedEffect(itemEdicao) {
        nomeItem = itemEdicao?.nome ?: ""
        quantidade = itemEdicao?.quantidade?.toString() ?: "1"
        preco = itemEdicao?.preco?.toString() ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { 
            Text(
                if (itemEdicao != null) "Editar Item" else "Adicionar Item",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() || char == '.' || char == ',' }
                            preco = filtered.replace(',', '.')
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
                    if (nomeItem.isNotBlank()) {
                        onConfirm(nomeItem.trim(), qty, precoValue)
                        if (itemEdicao == null) {
                            nomeItem = ""
                            quantidade = "1"
                            preco = ""
                        }
                    }
                },
                enabled = nomeItem.isNotBlank()
            ) {
                Text(if (itemEdicao != null) "Salvar" else "Adicionar")
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

