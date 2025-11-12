package com.example.minhascompras.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.functions.Functions

/**
 * Configuração do Supabase para o aplicativo.
 * 
 * Para usar o Supabase, você precisa:
 * 1. Obter a URL do seu projeto no painel do Supabase: https://app.supabase.com
 * 2. Obter a chave anônima (anon key) do seu projeto
 * 3. Substituir os valores abaixo com suas credenciais
 * 
 * Token de acesso pessoal: sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9
 * (Este token é usado para autenticação na CLI, não no cliente Android)
 */
object SupabaseConfig {
    // URL do projeto Supabase
    private const val SUPABASE_URL = "https://wkpmrmmhkhjbjfcuwakk.supabase.co"
    
    // Chave anônima do projeto (pode ser exposta no cliente)
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndrcG1ybW1oa2hqYmpmY3V3YWtrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI5NTcwODMsImV4cCI6MjA3ODUzMzA4M30.VSPA1u9HIU39z5oMJgiAaK9Z2Jd0-XMKDn0sJtyPBX0"
    
    /**
     * Cria e retorna uma instância do cliente Supabase configurada.
     * 
     * @return Cliente Supabase configurado com todos os módulos necessários
     */
    fun createClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Realtime)
            install(Storage)
            install(Auth)
            install(Functions)
        }
    }
    
    /**
     * Verifica se a configuração do Supabase está completa.
     * 
     * @return true se a URL e a chave foram configuradas corretamente
     */
    fun isConfigured(): Boolean {
        return SUPABASE_URL != "https://seu-projeto.supabase.co" &&
               SUPABASE_ANON_KEY != "sua-chave-anonima-aqui" &&
               SUPABASE_URL.isNotBlank() &&
               SUPABASE_ANON_KEY.isNotBlank()
    }
}

