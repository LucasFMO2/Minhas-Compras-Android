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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.SpendingDataPoint
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
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
    // Filtrar e validar dados para evitar NaN que causa crash na biblioteca Vico
    val (chartEntries, validDataPoints) = remember(spendingData) {
        val validPoints = mutableListOf<SpendingDataPoint>()
        val entries = spendingData
            .mapIndexedNotNull { index, dataPoint ->
                val amount = dataPoint.amount
                // Validar que o valor não é NaN, não é infinito e é um número válido
                if (amount.isNaN() || amount.isInfinite() || amount < 0) {
                    null // Ignorar valores inválidos
                } else {
                    validPoints.add(dataPoint)
                    FloatEntry(x = (validPoints.size - 1).toFloat(), y = amount.toFloat())
                }
            }
        Pair(entries, validPoints)
    }
    
    // Se após filtrar não houver dados válidos, mostrar mensagem
    if (chartEntries.isEmpty()) {
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
    
    // Validar que todos os entries são válidos antes de criar o modelo
    val validatedEntries = remember(chartEntries) {
        chartEntries.filter { entry ->
            val xValid = !entry.x.isNaN() && !entry.x.isInfinite() && entry.x >= 0
            val yValid = !entry.y.isNaN() && !entry.y.isInfinite() && entry.y >= 0
            xValid && yValid
        }
    }
    
    // Se após validação não houver dados válidos, mostrar mensagem
    // A biblioteca Vico precisa de pelo menos 2 pontos para calcular o eixo corretamente
    // Com apenas 1 ponto, pode gerar NaN no cálculo do eixo
    if (validatedEntries.isEmpty() || validatedEntries.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (validatedEntries.size == 1) {
                    "Dados insuficientes para exibir gráfico. Adicione mais compras no período."
                } else {
                    "Nenhum dado de gasto disponível para o período selecionado."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    // Garantir que os valores Y têm um range válido
    val maxY = validatedEntries.maxOfOrNull { it.y } ?: 0f
    val minY = validatedEntries.minOfOrNull { it.y } ?: 0f
    
    // Se todos os valores são zero ou inválidos, mostrar mensagem
    if (maxY <= 0f && minY <= 0f) {
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
    
    // Garantir que os valores X são sequenciais e começam em 0
    // E garantir que os valores Y são todos válidos e positivos
    val normalizedEntries = remember(validatedEntries) {
        validatedEntries
            .mapIndexed { index, entry ->
                // Garantir que Y é sempre positivo e válido
                val validY = if (entry.y.isNaN() || entry.y.isInfinite() || entry.y < 0) {
                    0f
                } else {
                    entry.y
                }
                FloatEntry(x = index.toFloat(), y = validY)
            }
            .filter { entry ->
                // Filtrar novamente para garantir que não há NaN
                !entry.x.isNaN() && !entry.x.isInfinite() && 
                !entry.y.isNaN() && !entry.y.isInfinite() &&
                entry.x >= 0 && entry.y >= 0
            }
    }
    
    // Verificar novamente após normalização
    if (normalizedEntries.isEmpty() || normalizedEntries.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Dados insuficientes para exibir gráfico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    val model = remember(normalizedEntries) {
        try {
            // Verificar se há algum problema com o range que pode causar NaN no cálculo do eixo
            val maxX = normalizedEntries.maxOfOrNull { it.x } ?: 0f
            val minX = normalizedEntries.minOfOrNull { it.x } ?: 0f
            val maxY = normalizedEntries.maxOfOrNull { it.y } ?: 0f
            val minY = normalizedEntries.minOfOrNull { it.y } ?: 0f
            val xRange = maxX - minX
            val yRange = maxY - minY
            
            // Range muito pequeno pode causar divisão por zero na biblioteca Vico
            // Adicionar um mínimo de range para evitar problemas
            val minValidRange = 0.01f
            if (yRange < minValidRange && yRange > 0f) {
                // Ajustar o range mínimo para evitar divisão por zero
                val adjustedEntries = normalizedEntries.map { entry ->
                    FloatEntry(x = entry.x, y = entry.y + minValidRange)
                }
                entryModelOf(adjustedEntries)
            } else if (xRange.isNaN() || xRange.isInfinite() || yRange.isNaN() || yRange.isInfinite()) {
                entryModelOf(emptyList())
            } else {
                entryModelOf(normalizedEntries)
            }
        } catch (e: Exception) {
            // Se houver erro ao criar o modelo, retornar modelo vazio
            entryModelOf(emptyList())
        }
    }
    
    // Atualizar validDataPoints para corresponder aos normalizedEntries
    val normalizedValidDataPoints = remember(validDataPoints, normalizedEntries) {
        if (normalizedEntries.size == validDataPoints.size) {
            validDataPoints
        } else {
            // Se o tamanho mudou, usar apenas os pontos correspondentes
            validDataPoints.take(normalizedEntries.size)
        }
    }

    // Formata o valor do eixo X (índice para data)
    // Usar normalizedValidDataPoints que corresponde aos normalizedEntries
    val bottomAxisValueFormatter = remember(normalizedValidDataPoints, dateFormat) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            try {
                // Validar que o valor não é NaN antes de converter
                if (value.isNaN() || value.isInfinite()) {
                    ""
                } else {
                    val index = value.toInt()
                    if (index >= 0 && index < normalizedValidDataPoints.size) {
                        dateFormat.format(Date(normalizedValidDataPoints[index].date))
                    } else {
                        ""
                    }
                }
            } catch (e: Exception) {
                ""
            }
        }
    }
    
    // Formata o valor do eixo Y (moeda) com validação
    val startAxisValueFormatter = remember(currencyFormat) {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            try {
                // Validar que o valor não é NaN antes de formatar
                if (value.isNaN() || value.isInfinite()) {
                    currencyFormat.format(0.0)
                } else {
                    currencyFormat.format(value.toDouble())
                }
            } catch (e: Exception) {
                currencyFormat.format(0.0)
            }
        }
    }


    // Validar modelo antes de renderizar
    // Garantir que há pelo menos 2 pontos e que o range Y é válido
    val (canRender, errorMsg) = remember(normalizedEntries, model) {
        try {
            // Verificar se o modelo tem dados válidos
            val entriesCount = normalizedEntries.size
            if (entriesCount < 2) {
                return@remember Pair(false, "Dados insuficientes para exibir gráfico. Adicione mais compras no período.")
            }
            
            // Verificar que todos os valores são válidos
            val allValid = normalizedEntries.all { entry ->
                val xValid = !entry.x.isNaN() && !entry.x.isInfinite() && entry.x >= 0
                val yValid = !entry.y.isNaN() && !entry.y.isInfinite() && entry.y >= 0
                xValid && yValid
            }
            
            if (!allValid) {
                return@remember Pair(false, "Dados inválidos detectados. Tente selecionar outro período.")
            }
            
            // Verificar que há um range válido nos valores Y
            val maxY = normalizedEntries.maxOfOrNull { it.y } ?: 0f
            val minY = normalizedEntries.minOfOrNull { it.y } ?: 0f
            val range = maxY - minY
            
            // Se o range for muito pequeno ou zero, pode causar problemas no cálculo do eixo
            if (range <= 0f || range.isNaN() || range.isInfinite()) {
                return@remember Pair(false, "Dados insuficientes para exibir gráfico.")
            }
            
            Pair(true, null)
        } catch (e: Exception) {
            Pair(false, "Erro ao validar dados do gráfico.")
        }
    }
    
    if (!canRender) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMsg ?: "Erro ao exibir gráfico. Tente selecionar outro período.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }
    
    // Verificação final antes de renderizar - garantir que o modelo não está vazio
    val finalModel = remember(model, normalizedEntries) {
        if (normalizedEntries.isEmpty()) {
            entryModelOf(emptyList())
        } else {
            model
        }
    }
    
    if (normalizedEntries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Dados insuficientes para exibir gráfico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
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
            model = finalModel,
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
