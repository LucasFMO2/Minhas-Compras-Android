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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.marker.rememberMarker
import com.patrykandpatrick.vico.compose.component.shape.shader.columnShader
import com.patrykandpatrick.vico.compose.component.shape.shader.roundedCornerShape
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.NumberFormat
import java.text.SimpleDateFormat
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
    val dateFormat = remember { SimpleDateFormat("MMM/yyyy", Locale("pt", "BR")) }
    
    // Criar duas séries de barras: período atual e período anterior
    val currentPeriodEntry = FloatEntry(x = 0f, y = comparison.currentSpending.toFloat())
    val previousPeriodEntry = FloatEntry(x = 0f, y = comparison.previousSpending.toFloat())
    
    val model = remember(comparison) {
        entryModelOf(
            listOf(currentPeriodEntry),
            listOf(previousPeriodEntry)
        )
    }

    // Cores: verde para aumento, vermelho para diminuição
    val currentColor = if (comparison.difference >= 0) {
        Color(0xFF22C55E) // Verde para aumento
    } else {
        Color(0xFFEF4444) // Vermelho para diminuição
    }
    val previousColor = Color(0xFF9CA3AF) // Cinza para período anterior

    // Formata o valor do eixo Y (moeda)
    val startAxisValueFormatter = remember(currencyFormat) {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            currencyFormat.format(value.toDouble())
        }
    }

    // Formata o valor do eixo X (períodos)
    val bottomAxisValueFormatter = remember(dateFormat, comparison) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            when (value.toInt()) {
                0 -> "Período Atual"
                1 -> "Período Anterior"
                else -> ""
            }
        }
    }

    // Configuração do marcador (tooltip)
    val marker = rememberMarker(
        label = textComponent {
            color = MaterialTheme.colorScheme.onSurface
            background = roundedCornerShape(
                cornerSize = 4.dp,
                color = MaterialTheme.colorScheme.surface
            )
            padding = dimensionsOf(horizontal = 8.dp, vertical = 4.dp)
        },
        indicator = lineComponent(
            color = MaterialTheme.colorScheme.primary,
            thickness = 2.dp
        ),
        guideline = axisGuidelineComponent(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Gráfico de barras
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            chart = columnChart(
                columns = listOf(
                    columnShader(
                        brush = DynamicShader.verticalGradient(
                            arrayOf(
                                currentColor.copy(alpha = 0.8f),
                                currentColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = roundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    ),
                    columnShader(
                        brush = DynamicShader.verticalGradient(
                            arrayOf(
                                previousColor.copy(alpha = 0.8f),
                                previousColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = roundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
                ),
                spacing = 8.dp,
                innerSpacing = 4.dp
            ),
            model = model,
            startAxis = rememberStartAxis(
                title = "Gastos Totais",
                titleComponent = textComponent {
                    color = MaterialTheme.colorScheme.onSurface
                },
                valueFormatter = startAxisValueFormatter,
                guideline = axisGuidelineComponent(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            ),
            bottomAxis = rememberBottomAxis(
                title = "Períodos",
                titleComponent = textComponent {
                    color = MaterialTheme.colorScheme.onSurface
                },
                valueFormatter = bottomAxisValueFormatter,
                guideline = axisGuidelineComponent(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            ),
            marker = marker
        )
        
        // Informações de comparação
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            // Legenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround
            ) {
                LegendItem(
                    color = currentColor,
                    label = "Período Atual"
                )
                LegendItem(
                    color = previousColor,
                    label = "Período Anterior"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Diferença percentual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Variação:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (comparison.difference >= 0) {
                        "+${currencyFormat.format(comparison.difference)} (${String.format(Locale("pt", "BR"), "%.1f", comparison.differencePercentage)}%)"
                    } else {
                        "${currencyFormat.format(comparison.difference)} (${String.format(Locale("pt", "BR"), "%.1f", comparison.differencePercentage)}%)"
                    },
                    style = MaterialTheme.typography.bodyLarge,
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

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

