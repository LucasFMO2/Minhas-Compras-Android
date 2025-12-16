package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ThemeMode
import com.example.minhascompras.data.ThemePreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themePreferencesManager: ThemePreferencesManager) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themePreferencesManager.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferencesManager.setThemeMode(mode)
        }
    }

    fun toggleThemeMode() {
        viewModelScope.launch {
            val currentMode = themeMode.value
            val nextMode = when (currentMode) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.SYSTEM
                ThemeMode.SYSTEM -> ThemeMode.LIGHT
            }
            themePreferencesManager.setThemeMode(nextMode)
        }
    }
}

class ThemeViewModelFactory(
    private val themePreferencesManager: ThemePreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(themePreferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

