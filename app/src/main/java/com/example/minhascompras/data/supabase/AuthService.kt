package com.example.minhascompras.data.supabase

import com.example.minhascompras.data.SupabaseConfig
import com.example.minhascompras.utils.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Serviço de autenticação usando Supabase Auth
 */
class AuthService {
    // TEMPORARIAMENTE DESABILITADO: Supabase desabilitado para evitar crashes
    private val supabase: SupabaseClient? = null
    // private val supabase: SupabaseClient? = if (SupabaseConfig.isConfigured()) {
    //     SupabaseConfig.createClient()
    // } else {
    //     null
    // }

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        // TEMPORARIAMENTE DESABILITADO: Verificação de sessão desabilitada
        // Verificar se há usuário logado ao inicializar
        // if (supabase != null) {
        //     try {
        //         val session = supabase.auth.currentSessionOrNull()
        //         _currentUser.value = session?.user
        //     } catch (e: Exception) {
        //         Logger.e(TAG, "Erro ao verificar usuário atual", e)
        //     }
        // }
    }

    /**
     * Registra um novo usuário com email e senha
     */
    suspend fun signUp(email: String, password: String): Result<UserInfo> {
        if (supabase == null) {
            return Result.failure(Exception("Supabase não configurado"))
        }

        // TEMPORARIAMENTE DESABILITADO: Aguardando correção da API do Supabase
        // TODO: Corrigir sintaxe da API do Supabase e reativar
        Logger.d(TAG, "Autenticação Supabase temporariamente desabilitada")
        return Result.failure(Exception("Supabase temporariamente desabilitado"))
    }

    /**
     * Faz login com email e senha
     */
    suspend fun signIn(email: String, password: String): Result<UserInfo> {
        if (supabase == null) {
            return Result.failure(Exception("Supabase não configurado"))
        }

        // TEMPORARIAMENTE DESABILITADO: Aguardando correção da API do Supabase
        // TODO: Corrigir sintaxe da API do Supabase e reativar
        Logger.d(TAG, "Autenticação Supabase temporariamente desabilitada")
        return Result.failure(Exception("Supabase temporariamente desabilitado"))
    }

    /**
     * Faz logout
     */
    suspend fun signOut(): Result<Unit> {
        // TEMPORARIAMENTE DESABILITADO: Aguardando correção da API do Supabase
        _currentUser.value = null
        Logger.d(TAG, "Logout (Supabase temporariamente desabilitado)")
        return Result.success(Unit)
    }

    /**
     * Obtém o ID do usuário atual
     */
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }

    /**
     * Verifica se há um usuário autenticado
     */
    fun isAuthenticated(): Boolean {
        return _currentUser.value != null
    }

    /**
     * Verifica se o serviço está disponível
     */
    fun isAvailable(): Boolean {
        // TEMPORARIAMENTE DESABILITADO: Sempre retorna false
        return false
        // return supabase != null && SupabaseConfig.isConfigured()
    }

    companion object {
        private const val TAG = "AuthService"
        
        // Instância singleton
        @Volatile
        private var INSTANCE: AuthService? = null

        fun getInstance(): AuthService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthService().also { INSTANCE = it }
            }
        }
    }
}

