package com.example.minhascompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.PeriodComparison
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PeriodComparisonBarChart(
    comparison: PeriodComparison?,
    modifier: Modifier = Modifier
) {
    if (comparison == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum dado de comparação disponível para os períodos selecionados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    
    // Cores: verde para aumento, vermelho para diminuição
    val currentColor = if (comparison.difference >= 0) {
        Color(0xFF22C55E) // Verde para aumento
    } else {
        Color(0xFFEF4444) // Vermelho para diminuição
    }
    val previousColor = Color(0xFF9CA3AF) // Cinza para período anterior

    Column(modifier = modifier.fillMaxWidth()) {
        // Comparação visual usando cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            // Período Atual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = currentColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Período Atual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(comparison.currentSpending),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = currentColor
                    )
                }
            }
            
            // Período Anterior
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = previousColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Período Anterior",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(comparison.previousSpending),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = previousColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Diferença percentual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (comparison.difference >= 0) {
                        Color(0xFF22C55E).copy(alpha = 0.1f)
                    } else {
                        Color(0xFFEF4444).copy(alpha = 0.1f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Variação:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (comparison.difference >= 0) {
                            "+${currencyFormat.format(comparison.difference)} (${String.format(Locale("pt", "BR"), "%.1f", comparison.differencePercentage)}%)"
                        } else {
                            "${currencyFormat.format(comparison.difference)} (${String.format(Locale("pt", "BR"), "%.1f", comparison.differencePercentage)}%)"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (comparison.difference >= 0) {
                            Color(0xFF22C55E) // Verde para aumento
                        } else {
                            Color(0xFFEF4444) // Vermelho para diminuição
                        }
                    )
                }
            }
        }
    }
}
