package com.example.minhascompras.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.CategoryBreakdown
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.pie.pieChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.NumberFormat
import java.util.Locale

// Mapa de cores para categorias
private val categoryColors = mapOf(
    "Frutas e Verduras" to Color(0xFF4CAF50),
    "Laticínios" to Color(0xFF2196F3),
    "Carnes e Aves" to Color(0xFFF44336),
    "Padaria" to Color(0xFFFFC107),
    "Limpeza" to Color(0xFF00BCD4),
    "Higiene" to Color(0xFFFF9800),
    "Bebidas" to Color(0xFF9C27B0),
    "Grãos e Cereais" to Color(0xFF795548),
    "Outros" to Color(0xFF9E9E9E)
)

@Composable
fun CategoryPieChart(
    categoryData: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    if (categoryData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum dado de gasto por categoria disponível para o período selecionado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    
    // Converter CategoryBreakdown para FloatEntry do Vico
    val chartEntries = remember(categoryData) {
        categoryData.map { breakdown ->
            breakdown.amount.toFloat()
        }
    }
    
    val model = remember(chartEntries) {
        entryModelOf(chartEntries)
    }

    // Criar lista de cores para cada fatia
    val colors = remember(categoryData) {
        categoryData.map { breakdown ->
            categoryColors[breakdown.category] ?: categoryColors["Outros"]!!
        }
    }

    // Criar slices do gráfico de pizza
    val slices = remember(colors, categoryData) {
        colors.mapIndexed { index, color ->
            PieChart.Slice(
                color = fromBrush(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(color, color)
                    )
                ),
                labelText = if (categoryData[index].percentage > 5) {
                    "${categoryData[index].percentage.toInt()}%"
                } else {
                    ""
                }
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Gráfico de pizza
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            chart = pieChart(slices = slices),
            model = model
        )
        
        // Legenda customizada
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            categoryData.forEachIndexed { index, breakdown ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(colors[index])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = breakdown.category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${currencyFormat.format(breakdown.amount)} (${breakdown.percentage.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

