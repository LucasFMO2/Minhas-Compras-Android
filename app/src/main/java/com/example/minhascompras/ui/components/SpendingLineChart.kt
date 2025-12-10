package com.example.minhascompras.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.SpendingDataPoint
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.marker.rememberMarker
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpendingLineChart(
    spendingData: List<SpendingDataPoint>,
    modifier: Modifier = Modifier
) {
    if (spendingData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum dado de gasto disponível para o período selecionado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale("pt", "BR")) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }
    
    // Converter SpendingDataPoint para FloatEntry do Vico
    val chartEntries = remember(spendingData) {
        spendingData.mapIndexed { index, dataPoint ->
            FloatEntry(x = index.toFloat(), y = dataPoint.amount.toFloat())
        }
    }
    
    val model = remember(chartEntries) {
        entryModelOf(chartEntries)
    }

    // Estado do scroll do gráfico
    val chartScrollState = rememberChartScrollState()

    // Formata o valor do eixo X (índice para data)
    val bottomAxisValueFormatter = remember(spendingData, dateFormat) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.toInt()
            if (index in spendingData.indices) {
                dateFormat.format(Date(spendingData[index].date))
            } else {
                ""
            }
        }
    }

    // Formata o valor do eixo Y (moeda)
    val startAxisValueFormatter = remember(currencyFormat) {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            currencyFormat.format(value.toDouble())
        }
    }

    // Configuração do marcador (tooltip) para exibir detalhes ao tocar no gráfico
    val marker = rememberMarker(
        label = textComponent {
            color = MaterialTheme.colorScheme.onSurface
            background = roundedCornerShape(
                cornerSize = 4.dp,
                color = MaterialTheme.colorScheme.surface
            )
            padding = dimensionsOf(horizontal = 8.dp, vertical = 4.dp)
            typeface = Typeface.MONOSPACE
            shadow = com.patrykandpatrick.vico.core.marker.Marker.Label.Text.Shadow(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 4f,
                dx = 0f,
                dy = 2f
            )
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
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            chart = lineChart(
                lines = listOf(
                    lineComponent(
                        color = MaterialTheme.colorScheme.primary,
                        thickness = 3.dp,
                        smooth = true // Linha suave
                    )
                ),
                spacing = 0.dp
            ),
            model = model,
            startAxis = rememberStartAxis(
                title = "Total Gasto",
                titleComponent = textComponent {
                    color = MaterialTheme.colorScheme.onSurface
                    typeface = Typeface.DEFAULT_BOLD
                },
                valueFormatter = startAxisValueFormatter,
                guideline = axisGuidelineComponent(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            ),
            bottomAxis = rememberBottomAxis(
                title = "Data",
                titleComponent = textComponent {
                    color = MaterialTheme.colorScheme.onSurface
                    typeface = Typeface.DEFAULT_BOLD
                },
                valueFormatter = bottomAxisValueFormatter,
                labelRotationDegrees = 45f, // Rotaciona as labels para evitar sobreposição
                guideline = axisGuidelineComponent(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
            ),
            marker = marker,
            chartScrollState = chartScrollState,
            isZoomEnabled = true // Habilita o pinch-to-zoom
        )
    }
}

