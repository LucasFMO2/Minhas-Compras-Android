package com.example.minhascompras.ui.utils

import java.util.Calendar

/**
 * Utilitários para cálculo de datas e períodos
 */
object DateUtils {
    
    /**
     * Calcula o início da semana (segunda-feira 00:00:00) para um timestamp dado
     * @param timestamp Timestamp de referência
     * @return Timestamp correspondente ao início da semana
     */
    fun getStartOfWeek(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = when (dayOfWeek) {
                Calendar.SUNDAY -> 6
                else -> dayOfWeek - Calendar.MONDAY
            }
            add(Calendar.DAY_OF_YEAR, -daysFromMonday)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Calcula o início da semana anterior a partir de um timestamp
     * @param timestamp Timestamp de referência
     * @return Timestamp correspondente ao início da semana anterior
     */
    fun getPreviousWeekStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        return getStartOfWeek(calendar.timeInMillis)
    }
    
    /**
     * Valida se um período é válido
     * @param startDate Timestamp de início do período
     * @param endDate Timestamp de fim do período
     * @return true se o período for válido, false caso contrário
     */
    fun isValidPeriod(startDate: Long, endDate: Long): Boolean {
        return startDate > 0 && 
               endDate > 0 && 
               startDate < endDate &&
               endDate <= System.currentTimeMillis()
    }
}