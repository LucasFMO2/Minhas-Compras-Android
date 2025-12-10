package com.example.minhascompras.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.DailyReminderScheduler
import com.example.minhascompras.data.NotificationPreferencesManager
import com.example.minhascompras.data.PendingItemsScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationPreferencesManager: NotificationPreferencesManager,
    private val context: Context
) : ViewModel() {
    
    val dailyReminderEnabled: StateFlow<Boolean> = notificationPreferencesManager.dailyReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val dailyReminderHour: StateFlow<Int> = notificationPreferencesManager.dailyReminderHour
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 9
        )
    
    val dailyReminderMinute: StateFlow<Int> = notificationPreferencesManager.dailyReminderMinute
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    val purchaseCompleteEnabled: StateFlow<Boolean> = notificationPreferencesManager.purchaseCompleteEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val pendingItemsEnabled: StateFlow<Boolean> = notificationPreferencesManager.pendingItemsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val pendingItemsDays: StateFlow<Int> = notificationPreferencesManager.pendingItemsDays
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 7
        )

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferencesManager.setDailyReminderEnabled(enabled)
            updateSchedulers()
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            notificationPreferencesManager.setDailyReminderTime(hour, minute)
            updateSchedulers()
        }
    }

    fun setPurchaseCompleteEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferencesManager.setPurchaseCompleteEnabled(enabled)
        }
    }

    fun setPendingItemsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferencesManager.setPendingItemsEnabled(enabled)
            updateSchedulers()
        }
    }

    fun setPendingItemsDays(days: Int) {
        viewModelScope.launch {
            notificationPreferencesManager.setPendingItemsDays(days)
            updateSchedulers()
        }
    }

    private suspend fun updateSchedulers() {
        val dailyScheduler = DailyReminderScheduler(context)
        dailyScheduler.updateSchedule()
        
        val pendingScheduler = PendingItemsScheduler(context)
        pendingScheduler.updateSchedule()
    }
}

class NotificationViewModelFactory(
    private val notificationPreferencesManager: NotificationPreferencesManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(notificationPreferencesManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

