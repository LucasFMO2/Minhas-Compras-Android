package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.HistoryItem
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.ShoppingListHistoryWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class PeriodType {
    WEEK,
    MONTH,
    THREE_MONTHS,
    YEAR,
    CUSTOM
}

data class Period(
    val type: PeriodType,
    val startDate: Long,
    val endDate: Long
)

data class SpendingDataPoint(
    val date: Long,
    val amount: Double
)

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val itemCount: Int
)

data class TopItem(
    val name: String,
    val frequency: Int,
    val lastPrice: Double?,
    val lastPurchaseDate: Long?
)

data class PeriodComparison(
    val currentPeriod: Period,
    val previousPeriod: Period,
    val currentSpending: Double,
    val previousSpending: Double,
    val difference: Double,
    val differencePercentage: Double
)

class StatisticsViewModel(
    private val repository: ItemCompraRepository
) : ViewModel() {
    
    // Cache em memória para dados calculados
    private val spendingOverTimeCache = mutableMapOf<String, List<SpendingDataPoint>>()
    private val categoryBreakdownCache = mutableMapOf<String, List<CategoryBreakdown>>()
    private val topItemsCache = mutableMapOf<String, List<TopItem>>()
    private val periodComparisonCache = mutableMapOf<String, PeriodComparison>()
    
    // Chave de cache baseada no período
    private fun getCacheKey(period: Period): String = "${period.startDate}_${period.endDate}"
    private fun getComparisonCacheKey(current: Period, previous: Period): String = 
        "${current.startDate}_${current.endDate}_${previous.startDate}_${previous.endDate}"
    
    private val _selectedPeriod = kotlinx.coroutines.flow.MutableStateFlow<Period>(
        getDefaultPeriod(PeriodType.MONTH)
    )
    
    // Debounce para mudanças de período (300ms) para evitar recálculos muito frequentes
    val selectedPeriod: StateFlow<Period> = _selectedPeriod
        .debounce(300L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = getDefaultPeriod(PeriodType.MONTH)
        )
    
    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
    }
    
    fun setPeriodType(type: PeriodType) {
        _selectedPeriod.value = getDefaultPeriod(type)
    }
    
    fun setCustomPeriod(startDate: Long, endDate: Long) {
        _selectedPeriod.value = Period(PeriodType.CUSTOM, startDate, endDate)
    }
    
    /**
     * Limpa o cache quando necessário (ex: quando novos dados são adicionados)
     */
    fun clearCache() {
        spendingOverTimeCache.clear()
        categoryBreakdownCache.clear()
        topItemsCache.clear()
        periodComparisonCache.clear()
    }
    
    /**
     * Obtém gastos ao longo do tempo agrupados por dia/semana/mês conforme o período
     * Com cache para evitar recálculos desnecessários
     */
    fun getSpendingOverTime(period: Period): Flow<List<SpendingDataPoint>> {
        val cacheKey = getCacheKey(period)
        
        // Verificar cache primeiro
        spendingOverTimeCache[cacheKey]?.let { cached ->
            return kotlinx.coroutines.flow.flowOf(cached)
        }
        
        return repository.getHistoryByDateRange(period.startDate, period.endDate)
            .map { historyLists ->
                val groupedData = when (period.type) {
                    PeriodType.WEEK -> groupByDay(historyLists)
                    PeriodType.MONTH, PeriodType.THREE_MONTHS -> groupByWeek(historyLists)
                    PeriodType.YEAR -> groupByMonth(historyLists)
                    PeriodType.CUSTOM -> {
                        val daysDiff = (period.endDate - period.startDate) / (1000 * 60 * 60 * 24)
                        when {
                            daysDiff <= 30 -> groupByDay(historyLists)
                            daysDiff <= 90 -> groupByWeek(historyLists)
                            else -> groupByMonth(historyLists)
                        }
                    }
                }
                val result = groupedData.sortedBy { it.date }
                // Armazenar no cache
                spendingOverTimeCache[cacheKey] = result
                result
            }
    }
    
    /**
     * Obtém distribuição de gastos por categoria
     * Com cache para evitar recálculos desnecessários
     */
    fun getCategoryBreakdown(period: Period): Flow<List<CategoryBreakdown>> {
        val cacheKey = getCacheKey(period)
        
        // Verificar cache primeiro
        categoryBreakdownCache[cacheKey]?.let { cached ->
            return kotlinx.coroutines.flow.flowOf(cached)
        }
        
        return repository.getHistoryByDateRange(period.startDate, period.endDate)
            .map { historyLists ->
                val categoryMap = mutableMapOf<String, Pair<Double, Int>>()
                var totalAmount = 0.0
                
                historyLists.forEach { historyWithItems ->
                    historyWithItems.items.forEach { item ->
                        val amount = (item.preco ?: 0.0) * item.quantidade
                        totalAmount += amount
                        val current = categoryMap.getOrDefault(item.categoria, 0.0 to 0)
                        categoryMap[item.categoria] = (current.first + amount) to (current.second + item.quantidade)
                    }
                }
                
                val result = categoryMap.map { (category, data) ->
                    CategoryBreakdown(
                        category = category,
                        amount = data.first,
                        percentage = if (totalAmount > 0) (data.first / totalAmount) * 100 else 0.0,
                        itemCount = data.second
                    )
                }.sortedByDescending { it.amount }
                
                // Armazenar no cache
                categoryBreakdownCache[cacheKey] = result
                result
            }
    }
    
    /**
     * Obtém top N itens mais comprados
     * Com cache para evitar recálculos desnecessários
     */
    fun getTopItems(limit: Int, period: Period): Flow<List<TopItem>> {
        val cacheKey = "${getCacheKey(period)}_$limit"
        
        // Verificar cache primeiro
        topItemsCache[cacheKey]?.let { cached ->
            return kotlinx.coroutines.flow.flowOf(cached)
        }
        
        return repository.getHistoryByDateRange(period.startDate, period.endDate)
            .map { historyLists ->
                val itemMap = mutableMapOf<String, MutableList<Pair<Double?, Long>>>()
                
                historyLists.forEach { historyWithItems ->
                    historyWithItems.items.forEach { item ->
                        if (!itemMap.containsKey(item.nome)) {
                            itemMap[item.nome] = mutableListOf()
                        }
                        itemMap[item.nome]?.add(
                            item.preco to historyWithItems.history.completionDate
                        )
                    }
                }
                
                val result = itemMap.map { (name, purchases) ->
                    val lastPurchase = purchases.maxByOrNull { it.second }
                    TopItem(
                        name = name,
                        frequency = purchases.size,
                        lastPrice = lastPurchase?.first,
                        lastPurchaseDate = lastPurchase?.second
                    )
                }
                    .sortedByDescending { it.frequency }
                    .take(limit)
                
                // Armazenar no cache
                topItemsCache[cacheKey] = result
                result
            }
    }
    
    /**
     * Compara gastos entre dois períodos
     * Com cache para evitar recálculos desnecessários
     */
    fun getPeriodComparison(
        currentPeriod: Period,
        previousPeriod: Period
    ): Flow<PeriodComparison> {
        val cacheKey = getComparisonCacheKey(currentPeriod, previousPeriod)
        
        // Verificar cache primeiro
        periodComparisonCache[cacheKey]?.let { cached ->
            return kotlinx.coroutines.flow.flowOf(cached)
        }
        
        return combine(
            repository.getTotalSpending(currentPeriod.startDate, currentPeriod.endDate),
            repository.getTotalSpending(previousPeriod.startDate, previousPeriod.endDate)
        ) { currentSpending, previousSpending ->
            val difference = currentSpending - previousSpending
            val differencePercentage = if (previousSpending > 0) {
                (difference / previousSpending) * 100
            } else {
                if (currentSpending > 0) 100.0 else 0.0
            }
            
            val result = PeriodComparison(
                currentPeriod = currentPeriod,
                previousPeriod = previousPeriod,
                currentSpending = currentSpending,
                previousSpending = previousSpending,
                difference = difference,
                differencePercentage = differencePercentage
            )
            
            // Armazenar no cache
            periodComparisonCache[cacheKey] = result
            result
        }
    }
    
    /**
     * Obtém total de gastos no período
     */
    fun getTotalSpending(period: Period): Flow<Double> {
        return repository.getTotalSpending(period.startDate, period.endDate)
    }
    
    // Funções auxiliares para agrupamento
    private fun groupByDay(historyLists: List<ShoppingListHistoryWithItems>): List<SpendingDataPoint> {
        val dayMap = mutableMapOf<Long, Double>()
        
        historyLists.forEach { historyWithItems ->
            val day = getStartOfDay(historyWithItems.history.completionDate)
            val total = historyWithItems.items.sumOf { (it.preco ?: 0.0) * it.quantidade }
            dayMap[day] = dayMap.getOrDefault(day, 0.0) + total
        }
        
        return dayMap.map { (date, amount) -> SpendingDataPoint(date, amount) }
    }
    
    private fun groupByWeek(historyLists: List<ShoppingListHistoryWithItems>): List<SpendingDataPoint> {
        val weekMap = mutableMapOf<Long, Double>()
        
        historyLists.forEach { historyWithItems ->
            val week = getStartOfWeek(historyWithItems.history.completionDate)
            val total = historyWithItems.items.sumOf { (it.preco ?: 0.0) * it.quantidade }
            weekMap[week] = weekMap.getOrDefault(week, 0.0) + total
        }
        
        return weekMap.map { (date, amount) -> SpendingDataPoint(date, amount) }
    }
    
    private fun groupByMonth(historyLists: List<ShoppingListHistoryWithItems>): List<SpendingDataPoint> {
        val monthMap = mutableMapOf<Long, Double>()
        
        historyLists.forEach { historyWithItems ->
            val month = getStartOfMonth(historyWithItems.history.completionDate)
            val total = historyWithItems.items.sumOf { (it.preco ?: 0.0) * it.quantidade }
            monthMap[month] = monthMap.getOrDefault(month, 0.0) + total
        }
        
        return monthMap.map { (date, amount) -> SpendingDataPoint(date, amount) }
    }
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    private fun getStartOfWeek(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Segunda-feira como início da semana
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    private fun getStartOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    private fun getDefaultPeriod(type: PeriodType): Period {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val startDate = when (type) {
            PeriodType.WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.timeInMillis
            }
            PeriodType.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            PeriodType.THREE_MONTHS -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }
            PeriodType.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            PeriodType.CUSTOM -> endDate // Será definido pelo usuário
        }
        
        return Period(type, startDate, endDate)
    }
}

class StatisticsViewModelFactory(
    private val repository: ItemCompraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

