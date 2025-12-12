package com.example.minhascompras.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import com.example.minhascompras.ui.utils.DateUtils
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    
    // Estados para os dados
    var spendingOverTime by remember { mutableStateOf<List<com.example.minhascompras.ui.viewmodel.SpendingDataPoint>>(emptyList()) }
    var categoryBreakdown by remember { mutableStateOf<List<com.example.minhascompras.ui.viewmodel.CategoryBreakdown>>(emptyList()) }
    var topItems by remember { mutableStateOf<List<com.example.minhascompras.ui.viewmodel.TopItem>>(emptyList()) }
    var periodComparison by remember { mutableStateOf<com.example.minhascompras.ui.viewmodel.PeriodComparison?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Calcular período anterior para comparação
    val previousPeriod = remember(selectedPeriod.type, selectedPeriod.startDate, selectedPeriod.endDate) {
        try {
            val result = when (selectedPeriod.type) {
                PeriodType.WEEK -> {
                    val prevStart = DateUtils.getPreviousWeekStart(selectedPeriod.startDate)
                    val prevEnd = selectedPeriod.startDate - 1
                    Period(PeriodType.WEEK, prevStart, prevEnd)
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
    val validPreviousPeriod = remember(previousPeriod, selectedPeriod) {
        // Garantir que prevStart < prevEnd e que ambos são válidos
        val isValid = previousPeriod.startDate < previousPeriod.endDate && 
            previousPeriod.startDate > 0 && 
            previousPeriod.endDate > 0 &&
            previousPeriod.endDate <= selectedPeriod.startDate // O período anterior deve terminar antes do período atual começar
        
        if (isValid) {
            previousPeriod
        } else {
            // Se inválido, criar um período padrão (1 semana antes do período atual)
            // Para WEEK, garantir alinhamento com segunda-feira
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = selectedPeriod.startDate
                if (selectedPeriod.type == PeriodType.WEEK) {
                    // Alinhar com segunda-feira antes de subtrair
                    val dayOfWeek = get(java.util.Calendar.DAY_OF_WEEK)
                    val daysFromMonday = when (dayOfWeek) {
                        java.util.Calendar.SUNDAY -> 6
                        else -> dayOfWeek - java.util.Calendar.MONDAY
                    }
                    add(java.util.Calendar.DAY_OF_YEAR, -daysFromMonday)
                }
                add(java.util.Calendar.WEEK_OF_YEAR, -1)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val fallbackStart = calendar.timeInMillis
            val fallbackEnd = if (selectedPeriod.startDate > 1) {
                selectedPeriod.startDate - 1
            } else {
                fallbackStart + (7L * 24 * 60 * 60 * 1000) - 1
            }
            Period(previousPeriod.type, fallbackStart, fallbackEnd)
        }
    }

    // Carregar dados quando o período mudar (usando keys para evitar recálculos desnecessários)
    LaunchedEffect(selectedPeriod.startDate, selectedPeriod.endDate, validPreviousPeriod.startDate, validPreviousPeriod.endDate) {
        isLoading = true
        try {
            kotlinx.coroutines.flow.combine(
                viewModel.getSpendingOverTime(selectedPeriod),
                viewModel.getCategoryBreakdown(selectedPeriod),
                viewModel.getTopItems(20, selectedPeriod),
                viewModel.getPeriodComparison(selectedPeriod, validPreviousPeriod)
            ) { spending, categories, top, comparison ->
                Quadruple(spending, categories, top, comparison)
            }.collect { result ->
                spendingOverTime = result.first
                categoryBreakdown = result.second
                topItems = result.third
                periodComparison = result.fourth
                isLoading = false
            }
        } catch (e: Exception) {
            isLoading = false
            // Limpar dados em caso de erro para evitar exibir dados incorretos
            spendingOverTime = emptyList()
            categoryBreakdown = emptyList()
            topItems = emptyList()
            periodComparison = null
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

