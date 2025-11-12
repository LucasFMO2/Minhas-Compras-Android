package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.supabase.AuthService
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUser: UserInfo? = null
)

data class SyncUiState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val syncError: String? = null,
    val isSyncAvailable: Boolean = false
)

class AuthViewModel(
    private val repository: ItemCompraRepository
) : ViewModel() {
    private val authService = AuthService.getInstance()

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _syncState = MutableStateFlow(SyncUiState())
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    init {
        // Observar estado de autenticação
        viewModelScope.launch {
            authService.currentUser.collect { user ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = user != null,
                    currentUser = user
                )
                _syncState.value = _syncState.value.copy(
                    isSyncAvailable = repository.isSyncAvailable()
                )
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            authService.signUp(email, password)
                .onSuccess { user ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                    _syncState.value = _syncState.value.copy(
                        isSyncAvailable = repository.isSyncAvailable()
                    )
                }
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao registrar"
                    )
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            authService.signIn(email, password)
                .onSuccess { user ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                    _syncState.value = _syncState.value.copy(
                        isSyncAvailable = repository.isSyncAvailable()
                    )
                    // Sincronizar dados após login
                    syncFromSupabase()
                }
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao fazer login"
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            authService.signOut()
                .onSuccess {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        currentUser = null
                    )
                    _syncState.value = _syncState.value.copy(
                        isSyncAvailable = false
                    )
                }
                .onFailure { e ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro ao fazer logout"
                    )
                }
        }
    }

    fun syncToSupabase() {
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null)
            repository.syncToSupabase()
                .onSuccess {
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
                .onFailure { e ->
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        syncError = e.message ?: "Erro ao sincronizar"
                    )
                }
        }
    }

    fun syncFromSupabase() {
        viewModelScope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null)
            repository.syncFromSupabase()
                .onSuccess {
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
                .onFailure { e ->
                    _syncState.value = _syncState.value.copy(
                        isSyncing = false,
                        syncError = e.message ?: "Erro ao sincronizar"
                    )
                }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
        _syncState.value = _syncState.value.copy(syncError = null)
    }
}

class AuthViewModelFactory(
    private val repository: ItemCompraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

