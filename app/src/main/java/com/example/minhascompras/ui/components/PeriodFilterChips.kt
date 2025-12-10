package com.example.minhascompras.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minhascompras.ui.viewmodel.Period
import com.example.minhascompras.ui.viewmodel.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodFilterChips(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chip Semana
            FilterChip(
                selected = selectedPeriod.type == PeriodType.WEEK,
                onClick = {
                    onPeriodSelected(
                        Period(
                            type = PeriodType.WEEK,
                            startDate = getDefaultPeriodStart(PeriodType.WEEK),
                            endDate = System.currentTimeMillis()
                        )
                    )
                },
                label = { Text("Semana") }
            )

            // Chip Mês
            FilterChip(
                selected = selectedPeriod.type == PeriodType.MONTH,
                onClick = {
                    onPeriodSelected(
                        Period(
                            type = PeriodType.MONTH,
                            startDate = getDefaultPeriodStart(PeriodType.MONTH),
                            endDate = System.currentTimeMillis()
                        )
                    )
                },
                label = { Text("Mês") }
            )

            // Chip 3 Meses
            FilterChip(
                selected = selectedPeriod.type == PeriodType.THREE_MONTHS,
                onClick = {
                    onPeriodSelected(
                        Period(
                            type = PeriodType.THREE_MONTHS,
                            startDate = getDefaultPeriodStart(PeriodType.THREE_MONTHS),
                            endDate = System.currentTimeMillis()
                        )
                    )
                },
                label = { Text("3 Meses") }
            )

            // Chip Ano
            FilterChip(
                selected = selectedPeriod.type == PeriodType.YEAR,
                onClick = {
                    onPeriodSelected(
                        Period(
                            type = PeriodType.YEAR,
                            startDate = getDefaultPeriodStart(PeriodType.YEAR),
                            endDate = System.currentTimeMillis()
                        )
                    )
                },
                label = { Text("Ano") }
            )

            // Chip Personalizado
            FilterChip(
                selected = selectedPeriod.type == PeriodType.CUSTOM,
                onClick = {
                    // Abrir DatePicker para data inicial
                    customStartDate = selectedPeriod.startDate
                    customEndDate = selectedPeriod.endDate
                    showStartDatePicker = true
                },
                label = { Text("Personalizado") }
            )
        }

        // Exibir período selecionado se for personalizado
        if (selectedPeriod.type == PeriodType.CUSTOM) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "De: ${dateFormat.format(selectedPeriod.startDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Até: ${dateFormat.format(selectedPeriod.endDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // DatePicker para data inicial
    if (showStartDatePicker) {
        val startDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customStartDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDatePickerState.selectedDateMillis?.let {
                            customStartDate = it
                            showStartDatePicker = false
                            showEndDatePicker = true
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    // DatePicker para data final
    if (showEndDatePicker) {
        val endDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customEndDate ?: System.currentTimeMillis(),
            initialDisplayedMonthMillis = customStartDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDatePickerState.selectedDateMillis?.let { endDate ->
                            customStartDate?.let { startDate ->
                                if (endDate >= startDate) {
                                    customEndDate = endDate
                                    onPeriodSelected(
                                        Period(
                                            type = PeriodType.CUSTOM,
                                            startDate = startDate,
                                            endDate = endDate
                                        )
                                    )
                                    showEndDatePicker = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}

private fun getDefaultPeriodStart(type: PeriodType): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return when (type) {
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
        PeriodType.CUSTOM -> System.currentTimeMillis()
    }
}

