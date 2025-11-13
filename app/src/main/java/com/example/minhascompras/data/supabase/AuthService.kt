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
    // private val supabase: SupabaseClient? = try {
    //     if (SupabaseConfig.isConfigured()) {
    //         SupabaseConfig.createClient()
    //     } else {
    //         null
    //     }
    // } catch (e: Exception) {
    //     Logger.e(TAG, "Erro ao criar cliente Supabase", e)
    //     null
    // }

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    init {
        // TEMPORARIAMENTE DESABILITADO: Supabase desabilitado para evitar crashes
        // Verificar se há usuário logado ao inicializar
        // if (supabase != null) {
        //     try {
        //         val session = supabase.auth.currentSessionOrNull()
        //         _currentUser.value = session?.user
        //     } catch (e: Exception) {
        //         Logger.e(TAG, "Erro ao verificar usuário atual", e)
        //         // Não falha a inicialização se houver erro
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

        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Obter sessão após registro
            val session = supabase.auth.currentSessionOrNull()
            val user = session?.user
            _currentUser.value = user
            Logger.d(TAG, "Usuário registrado: ${user?.email}")
            Result.success(user ?: throw Exception("Usuário não retornado"))
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao registrar usuário", e)
            Result.failure(e)
        }
    }

    /**
     * Faz login com email e senha
     */
    suspend fun signIn(email: String, password: String): Result<UserInfo> {
        if (supabase == null) {
            return Result.failure(Exception("Supabase não configurado"))
        }

        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // Obter sessão após login
            val session = supabase.auth.currentSessionOrNull()
            val user = session?.user
            _currentUser.value = user
            Logger.d(TAG, "Usuário logado: ${user?.email}")
            Result.success(user ?: throw Exception("Usuário não retornado"))
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao fazer login", e)
            Result.failure(e)
        }
    }

    /**
     * Faz logout
     */
    suspend fun signOut(): Result<Unit> {
        if (supabase == null) {
            _currentUser.value = null
            return Result.success(Unit)
        }

        return try {
            supabase.auth.signOut()
            _currentUser.value = null
            Logger.d(TAG, "Usuário deslogado")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Erro ao fazer logout", e)
            // Mesmo com erro, limpa o estado local
            _currentUser.value = null
            Result.failure(e)
        }
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
        return supabase != null && SupabaseConfig.isConfigured()
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

