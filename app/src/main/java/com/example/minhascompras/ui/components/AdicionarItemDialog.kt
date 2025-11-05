package com.example.minhascompras.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AdicionarItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var nomeItem by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("1") }

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
                "Adicionar Item",
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
                OutlinedTextField(
                    value = quantidade,
                    onValueChange = { if (it.all { char -> char.isDigit() && it.length <= 3 }) quantidade = it },
                    label = { Text("Quantidade") },
                    placeholder = { Text("1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    supportingText = {
                        Text(
                            "Digite a quantidade desejada",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantidade.toIntOrNull()?.takeIf { it > 0 } ?: 1
                    if (nomeItem.isNotBlank()) {
                        onConfirm(nomeItem.trim(), qty)
                        nomeItem = ""
                        quantidade = "1"
                    }
                },
                enabled = nomeItem.isNotBlank()
            ) {
                Text("Adicionar")
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

