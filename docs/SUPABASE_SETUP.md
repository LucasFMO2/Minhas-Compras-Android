# Configuração do Supabase

Este documento descreve como configurar e usar o Supabase no projeto Minhas Compras.

## Instalação

As dependências do Supabase já foram adicionadas ao projeto:

- `postgrest-kt`: Cliente para interagir com o banco de dados PostgreSQL
- `realtime-kt`: Suporte para atualizações em tempo real
- `storage-kt`: Gerenciamento de arquivos e storage
- `gotrue-kt`: Autenticação de usuários
- `functions-kt`: Execução de funções serverless

## Configuração

### 1. Obter Credenciais do Supabase

1. Acesse o painel do Supabase: https://app.supabase.com
2. Selecione seu projeto (ou crie um novo)
3. Vá em **Settings > API**
4. Copie os seguintes valores:
   - **Project URL**: A URL do seu projeto (ex: `https://seu-projeto-id.supabase.co`)
   - **anon/public key**: A chave anônima para uso no cliente

### 2. Configurar no Código

Edite o arquivo `app/src/main/java/com/example/minhascompras/data/SupabaseConfig.kt`:

```kotlin
private const val SUPABASE_URL = "https://seu-projeto-id.supabase.co"
private const val SUPABASE_ANON_KEY = "sua-chave-anonima-aqui"
```

Substitua os valores com suas credenciais reais.

### 3. Token de Acesso Pessoal

O token de acesso pessoal (`sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9`) foi configurado no arquivo `local.properties`.

**Nota**: Este token é usado para autenticação na CLI do Supabase, não no cliente Android. O cliente Android usa a chave anônima (anon key).

## Uso Básico

### Inicializar o Cliente

```kotlin
import com.example.minhascompras.data.SupabaseConfig

// Verificar se está configurado
if (SupabaseConfig.isConfigured()) {
    val supabase = SupabaseConfig.createClient()
    
    // Usar o cliente
    // Exemplo: supabase.postgrest.from("tabela").select()
}
```

### Exemplo: Consultar Dados

```kotlin
val supabase = SupabaseConfig.createClient()

// Consultar dados de uma tabela
val result = supabase.postgrest
    .from("minhas_compras")
    .select()
    .decodeList<ItemCompra>()
```

### Exemplo: Inserir Dados

```kotlin
val supabase = SupabaseConfig.createClient()

// Inserir um novo item
supabase.postgrest
    .from("minhas_compras")
    .insert(ItemCompra(...))
```

### Exemplo: Autenticação

```kotlin
val supabase = SupabaseConfig.createClient()

// Fazer login
supabase.auth.signInWith(Email) {
    email = "usuario@example.com"
    password = "senha123"
}

// Fazer logout
supabase.auth.signOut()
```

## Segurança

⚠️ **IMPORTANTE**: 

- A chave anônima (anon key) pode ser exposta no código do cliente Android
- Use Row Level Security (RLS) no Supabase para proteger seus dados
- Configure políticas de segurança adequadas no painel do Supabase
- Nunca use a chave de serviço (service role key) no cliente Android

## Recursos Adicionais

- [Documentação do Supabase Kotlin](https://github.com/supabase/supabase-kt)
- [Guia de Autenticação](https://supabase.com/docs/guides/auth)
- [Row Level Security](https://supabase.com/docs/guides/auth/row-level-security)

