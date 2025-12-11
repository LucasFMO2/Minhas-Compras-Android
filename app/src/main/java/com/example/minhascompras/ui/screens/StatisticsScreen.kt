package com.example.minhascompras.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import java.io.File
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.components.CategoryPieChart
import com.example.minhascompras.ui.components.PeriodComparisonBarChart
import com.example.minhascompras.ui.components.PeriodFilterChips
import com.example.minhascompras.ui.components.SpendingLineChart
import com.example.minhascompras.ui.components.TopItemsList
import com.example.minhascompras.ui.utils.ResponsiveUtils
import com.example.minhascompras.ui.viewmodel.Period
import com.example.minhascompras.ui.viewmodel.PeriodType
import com.example.minhascompras.ui.viewmodel.StatisticsViewModel
import kotlinx.coroutines.flow.combine

// Helper data class para combine
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// Helper function para debug logs
private fun debugLog(location: String, message: String, data: Map<String, Any?> = emptyMap(), hypothesisId: String = "A") {
    try {
        Log.d("DebugLog", "[$hypothesisId] $location: $message - $data")
        val logFile = File("c:\\Users\\nerdd\\Desktop\\Minhas-Compras-Android\\.cursor\\debug.log")
        val logDir = logFile.parentFile
        if (logDir != null && !logDir.exists()) {
            logDir.mkdirs()
        }
        val dataEntries = data.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
        val logLine = """{"id":"log_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().hashCode()}","timestamp":${System.currentTimeMillis()},"location":"$location","message":"$message","sessionId":"debug-session","runId":"run1","hypothesisId":"$hypothesisId","data":{$dataEntries}}"""
        logFile.appendText("$logLine\n")
    } catch (e: Exception) {
        Log.e("DebugLog", "Failed to write debug log: ${e.message}", e)
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    
    // #region agent log
    androidx.compose.runtime.LaunchedEffect(selectedPeriod.type, selectedPeriod.startDate, selectedPeriod.endDate) {
        debugLog("StatisticsScreen.kt:62", "selectedPeriod changed", mapOf("type" to selectedPeriod.type.toString(), "startDate" to selectedPeriod.startDate, "endDate" to selectedPeriod.endDate), "A")
    }
    // #endregion
    
    // Estados para os dados
    var spendingOverTime by remember { mutableStateOf<List<com.example.minhascompras.ui.viewmodel.SpendingDataPoint>>(emptyList()) }
    var categoryBreakdown by remember { mutableStateOf<List<com.example.minhascompras.ui.viewmodel.CategoryBreakdown>>(emptyList()) }
    var topItems by remember { mutableStateOf<List<com.example.minhascompras.ui.viewmodel.TopItem>>(emptyList()) }
    var periodComparison by remember { mutableStateOf<com.example.minhascompras.ui.viewmodel.PeriodComparison?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Calcular período anterior para comparação
    val previousPeriod = remember(selectedPeriod.type, selectedPeriod.startDate, selectedPeriod.endDate) {
        // #region agent log
        debugLog("StatisticsScreen.kt:80", "previousPeriod calculation start", mapOf("type" to selectedPeriod.type.toString(), "startDate" to selectedPeriod.startDate, "endDate" to selectedPeriod.endDate), "A")
        // #endregion
        try {
            val result = when (selectedPeriod.type) {
                PeriodType.WEEK -> {
                    // #region agent log
                    debugLog("StatisticsScreen.kt:88", "WEEK period calculation start", mapOf("startDate" to selectedPeriod.startDate, "endDate" to selectedPeriod.endDate), "A")
                    // #endregion
                    try {
                        // Calcular início da semana anterior (1 semana antes do startDate atual)
                        val calendar = java.util.Calendar.getInstance().apply {
                            timeInMillis = selectedPeriod.startDate
                            add(java.util.Calendar.WEEK_OF_YEAR, -1)
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        val prevStart = calendar.timeInMillis
                        
                        // Calcular fim da semana anterior (1 milissegundo antes do startDate atual)
                        // Isso garante que não há sobreposição entre os períodos
                        val prevEnd = if (selectedPeriod.startDate > 1) {
                            selectedPeriod.startDate - 1
                        } else {
                            // Fallback: se startDate for muito pequeno, usar a mesma duração da semana atual
                            val weekDuration = selectedPeriod.endDate - selectedPeriod.startDate
                            if (weekDuration > 0 && weekDuration < 8L * 24 * 60 * 60 * 1000) { // Máximo 8 dias
                                prevStart + weekDuration
                            } else {
                                // Duração padrão de 7 dias
                                prevStart + (7L * 24 * 60 * 60 * 1000) - 1
                            }
                        }
                        
                        // Validar que prevStart < prevEnd
                        val finalPrevEnd = if (prevStart < prevEnd) {
                            prevEnd
                        } else {
                            // Se prevEnd for menor que prevStart, ajustar para 1 milissegundo após prevStart
                            prevStart + 1
                        }
                        
                        // #region agent log
                        debugLog("StatisticsScreen.kt:116", "previousPeriod WEEK calculated", mapOf("prevStart" to prevStart, "prevEnd" to finalPrevEnd, "prevStartLessThanPrevEnd" to (prevStart < finalPrevEnd)), "A")
                        // #endregion
                        
                        val period = Period(PeriodType.WEEK, prevStart, finalPrevEnd)
                        // #region agent log
                        debugLog("StatisticsScreen.kt:128", "previousPeriod WEEK result", mapOf("periodStart" to period.startDate, "periodEnd" to period.endDate), "A")
                        // #endregion
                        period
                    } catch (e: Exception) {
                        // #region agent log
                        debugLog("StatisticsScreen.kt:140", "Exception in WEEK calculation", mapOf("exception" to e.message, "stackTrace" to e.stackTraceToString()), "B")
                        // #endregion
                        // Em caso de erro, retornar um período válido como fallback
                        val fallbackStart = selectedPeriod.startDate - (7L * 24 * 60 * 60 * 1000)
                        val fallbackEnd = selectedPeriod.startDate - 1
                        Period(PeriodType.WEEK, fallbackStart, fallbackEnd)
                    }
                }
                PeriodType.MONTH -> {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = selectedPeriod.startDate
                    calendar.add(java.util.Calendar.MONTH, -1)
                    val prevEnd = calendar.timeInMillis
                    calendar.add(java.util.Calendar.MONTH, -1)
                    val prevStart = calendar.timeInMillis
                    Period(PeriodType.MONTH, prevStart, prevEnd)
                }
                PeriodType.THREE_MONTHS -> {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = selectedPeriod.startDate
                    calendar.add(java.util.Calendar.MONTH, -3)
                    val prevEnd = calendar.timeInMillis
                    calendar.add(java.util.Calendar.MONTH, -3)
                    val prevStart = calendar.timeInMillis
                    Period(PeriodType.THREE_MONTHS, prevStart, prevEnd)
                }
                PeriodType.YEAR -> {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = selectedPeriod.startDate
                    calendar.add(java.util.Calendar.YEAR, -1)
                    val prevEnd = calendar.timeInMillis
                    calendar.add(java.util.Calendar.YEAR, -1)
                    val prevStart = calendar.timeInMillis
                    Period(PeriodType.YEAR, prevStart, prevEnd)
                }
                PeriodType.CUSTOM -> {
                    val daysDiff = (selectedPeriod.endDate - selectedPeriod.startDate) / (1000 * 60 * 60 * 24)
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = selectedPeriod.endDate
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysDiff.toInt())
                    val prevEnd = calendar.timeInMillis
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysDiff.toInt())
                    val prevStart = calendar.timeInMillis
                    Period(PeriodType.CUSTOM, prevStart, prevEnd)
                }
            }
            result
        } catch (e: Exception) {
            throw e
        }
    }

    // Validar previousPeriod antes de usar
    val validPreviousPeriod = remember(previousPeriod) {
        // Garantir que prevStart < prevEnd e que ambos são válidos
        if (previousPeriod.startDate < previousPeriod.endDate && 
            previousPeriod.startDate > 0 && 
            previousPeriod.endDate > 0) {
            previousPeriod
        } else {
            // Se inválido, criar um período padrão (1 semana antes do período atual)
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = selectedPeriod.startDate
                add(java.util.Calendar.WEEK_OF_YEAR, -1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val fallbackStart = calendar.timeInMillis
            val fallbackEnd = selectedPeriod.startDate - 1
            Period(previousPeriod.type, fallbackStart, fallbackEnd)
        }
    }

    // Carregar dados quando o período mudar (usando keys para evitar recálculos desnecessários)
    LaunchedEffect(selectedPeriod.startDate, selectedPeriod.endDate, validPreviousPeriod.startDate, validPreviousPeriod.endDate) {
        // #region agent log
        debugLog("StatisticsScreen.kt:203", "LaunchedEffect triggered", mapOf("selectedStart" to selectedPeriod.startDate, "selectedEnd" to selectedPeriod.endDate, "prevStart" to validPreviousPeriod.startDate, "prevEnd" to validPreviousPeriod.endDate), "C")
        // #endregion
        isLoading = true
        try {
            // #region agent log
            debugLog("StatisticsScreen.kt:207", "Starting combine flows", mapOf(), "C")
            // #endregion
            kotlinx.coroutines.flow.combine(
                viewModel.getSpendingOverTime(selectedPeriod),
                viewModel.getCategoryBreakdown(selectedPeriod),
                viewModel.getTopItems(20, selectedPeriod),
                viewModel.getPeriodComparison(selectedPeriod, validPreviousPeriod)
            ) { spending, categories, top, comparison ->
                // #region agent log
                debugLog("StatisticsScreen.kt:213", "combine collected values", mapOf("spendingSize" to spending.size, "categoriesSize" to categories.size, "topSize" to top.size, "comparisonNotNull" to (comparison != null)), "C")
                // #endregion
                Quadruple(spending, categories, top, comparison)
            }.collect { result ->
                // #region agent log
                debugLog("StatisticsScreen.kt:219", "Updating state with collected data", mapOf("spendingSize" to result.first.size, "categoriesSize" to result.second.size, "topSize" to result.third.size), "C")
                // #endregion
                spendingOverTime = result.first
                categoryBreakdown = result.second
                topItems = result.third
                periodComparison = result.fourth
                isLoading = false
                // #region agent log
                debugLog("StatisticsScreen.kt:225", "Data update complete, isLoading=false", mapOf(), "C")
                // #endregion
            }
        } catch (e: Exception) {
            // #region agent log
            debugLog("StatisticsScreen.kt:229", "Exception in LaunchedEffect", mapOf("exception" to e.message, "stackTrace" to e.stackTraceToString()), "B")
            // #endregion
            isLoading = false
            // Não relançar exceção para evitar crash
        }
    }
    
    // Estados derivados para evitar recomposições desnecessárias
    val hasData = remember(spendingOverTime, categoryBreakdown, topItems) {
        spendingOverTime.isNotEmpty() || categoryBreakdown.isNotEmpty() || topItems.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Estatísticas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = ResponsiveUtils.getTitleFontSize()
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Filtros de período
                item {
                    PeriodFilterChips(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { period ->
                            viewModel.setPeriod(period)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Gráfico de linha (gastos ao longo do tempo)
                item {
                    SpendingLineChart(
                        spendingData = spendingOverTime,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Gráfico de pizza (gastos por categoria)
                item {
                    CategoryPieChart(
                        categoryData = categoryBreakdown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Gráfico de barras (comparação de períodos)
                item {
                    PeriodComparisonBarChart(
                        comparison = periodComparison,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Lista de itens mais comprados
                item {
                    TopItemsList(
                        topItems = topItems,
                        limit = 20,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Espaçamento final
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

