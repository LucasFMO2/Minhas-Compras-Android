package com.example.minhascompras.ui.components

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
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.SpendingDataPoint
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
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

    Column(modifier = modifier.fillMaxWidth()) {
        Chart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                ),
                spacing = 0.dp
            ),
            model = model,
            startAxis = rememberStartAxis(
                valueFormatter = startAxisValueFormatter
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = bottomAxisValueFormatter,
                labelRotationDegrees = 45f
            )
        )
    }
}
