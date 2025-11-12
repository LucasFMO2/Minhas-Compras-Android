# Integração do Supabase - Minhas Compras

Este documento descreve a integração completa do Supabase no aplicativo Minhas Compras.

## ✅ O que foi implementado

### 1. Configuração do Supabase
- ✅ Credenciais configuradas no `SupabaseConfig.kt`
- ✅ Cliente Supabase inicializado com todos os módulos necessários
- ✅ Verificação de configuração implementada

### 2. Banco de Dados
- ✅ Tabelas criadas no Supabase:
  - `itens_compra` - Itens da lista de compras
  - `shopping_list_history` - Histórico de listas completadas
  - `history_items` - Itens do histórico
- ✅ Índices criados para otimização
- ✅ Row Level Security (RLS) configurado
- ✅ Políticas de segurança implementadas (usuários só veem seus próprios dados)

### 3. Modelos de Dados
- ✅ `ItemCompraSupabase` - Modelo para Supabase com campos adicionais
- ✅ `ShoppingListHistorySupabase` - Modelo de histórico
- ✅ `HistoryItemSupabase` - Modelo de itens do histórico
- ✅ Funções de conversão entre modelos Room e Supabase

### 4. Sincronização Híbrida
- ✅ `SupabaseSyncService` - Serviço de sincronização
- ✅ Sincronização automática após operações locais (insert, update, delete)
- ✅ Sincronização manual (push/pull)
- ✅ Estratégia: Room como fonte de verdade local, Supabase como backup remoto
- ✅ Sincronização não bloqueia operações locais (falhas são silenciosas)

### 5. Autenticação
- ✅ `AuthService` - Serviço de autenticação
- ✅ Registro de usuários (sign up)
- ✅ Login (sign in)
- ✅ Logout (sign out)
- ✅ Verificação de sessão atual
- ✅ `AuthViewModel` - ViewModel para gerenciar estado de autenticação

### 6. Integração no Repositório
- ✅ `ItemCompraRepository` atualizado com sincronização automática
- ✅ Métodos de sincronização manual (`syncToSupabase`, `syncFromSupabase`)
- ✅ Verificação de disponibilidade de sincronização

## 📋 Estrutura de Arquivos

```
app/src/main/java/com/example/minhascompras/
├── data/
│   ├── SupabaseConfig.kt                    # Configuração do Supabase
│   └── supabase/
│       ├── ItemCompraSupabase.kt            # Modelo de dados para Supabase
│       ├── ShoppingListHistorySupabase.kt   # Modelo de histórico
│       ├── HistoryItemSupabase.kt           # Modelo de itens do histórico
│       ├── SupabaseSyncService.kt           # Serviço de sincronização
│       └── AuthService.kt                   # Serviço de autenticação
├── ui/
│   └── viewmodel/
│       └── AuthViewModel.kt                 # ViewModel de autenticação
└── ...
```

## 🔐 Segurança

### Row Level Security (RLS)
Todas as tabelas têm RLS habilitado com políticas que garantem:
- Usuários só podem ver seus próprios dados
- Usuários só podem inserir dados para si mesmos
- Usuários só podem atualizar seus próprios dados
- Usuários só podem deletar seus próprios dados

### Políticas Implementadas
- `itens_compra`: Políticas para SELECT, INSERT, UPDATE, DELETE
- `shopping_list_history`: Políticas para SELECT, INSERT, DELETE
- `history_items`: Políticas baseadas no parent_list_id do usuário

## 🚀 Como Usar

### Autenticação

```kotlin
val authViewModel = AuthViewModel(repository)

// Registrar novo usuário
authViewModel.signUp("usuario@email.com", "senha123")

// Fazer login
authViewModel.signIn("usuario@email.com", "senha123")

// Fazer logout
authViewModel.signOut()
```

### Sincronização

A sincronização acontece automaticamente após operações locais. Para sincronização manual:

```kotlin
// Sincronizar dados locais para o Supabase
repository.syncToSupabase()

// Sincronizar dados do Supabase para o local
repository.syncFromSupabase()
```

### Verificar Status

```kotlin
// Verificar se sincronização está disponível
val isAvailable = repository.isSyncAvailable()

// Verificar estado de autenticação
val authState = authViewModel.authState.value
val isAuthenticated = authState.isAuthenticated
```

## 📊 Fluxo de Sincronização

1. **Operação Local** (insert/update/delete)
   - Operação é executada no Room (local)
   - Se usuário estiver autenticado e Supabase disponível:
     - Sincronização automática em background
     - Falhas não bloqueiam a operação local

2. **Sincronização Manual (Push)**
   - Todos os itens locais são enviados para Supabase
   - Itens remotos do usuário são substituídos

3. **Sincronização Manual (Pull)**
   - Itens do Supabase são baixados
   - Itens locais são substituídos pelos remotos

## 🔄 Próximos Passos (Opcional)

### Funcionalidades Futuras
- [ ] Sincronização em tempo real usando Supabase Realtime
- [ ] Resolução de conflitos (última modificação vence)
- [ ] Sincronização incremental (apenas mudanças)
- [ ] Sincronização automática periódica
- [ ] UI de autenticação na tela de configurações
- [ ] Indicador de status de sincronização
- [ ] Histórico de sincronizações

### Melhorias de Segurança
- [ ] Refresh token automático
- [ ] Validação de email antes de permitir login
- [ ] Recuperação de senha
- [ ] Autenticação com OAuth (Google, Apple, etc.)

## 📝 Notas Importantes

1. **Offline First**: O app funciona completamente offline. A sincronização é opcional e não bloqueia operações.

2. **Falhas Silenciosas**: Erros de sincronização não são mostrados ao usuário para não interromper o fluxo. Logs são registrados para debug.

3. **Dados Locais**: Os dados locais (Room) são sempre a fonte de verdade. O Supabase serve como backup e sincronização entre dispositivos.

4. **Autenticação Opcional**: O app funciona sem autenticação, mas a sincronização só está disponível para usuários autenticados.

## 🐛 Troubleshooting

### Sincronização não funciona
- Verifique se o Supabase está configurado corretamente
- Verifique se o usuário está autenticado
- Verifique os logs para erros específicos

### Erro de autenticação
- Verifique se o email está correto
- Verifique se a senha atende aos requisitos mínimos
- Verifique se o email foi confirmado (se necessário)

### Dados não aparecem após sincronização
- Verifique as políticas RLS no Supabase
- Verifique se o user_id está correto
- Verifique os logs para erros de permissão

## 📚 Referências

- [Documentação Supabase Kotlin](https://github.com/supabase/supabase-kt)
- [Row Level Security](https://supabase.com/docs/guides/auth/row-level-security)
- [Autenticação Supabase](https://supabase.com/docs/guides/auth)

