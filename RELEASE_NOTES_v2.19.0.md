# 🚀 Release v2.19.0 - Notificações Inteligentes

## 🎉 Nova Funcionalidade Principal

### 📱 Sistema de Notificações Inteligentes

Esta versão introduz um sistema completo de notificações inteligentes que ajuda você a não esquecer suas compras e mantém o engajamento com o app!

#### ✨ Funcionalidades Implementadas

- **🔔 Lembrete Diário Configurável**
  - Receba um lembrete diário no horário que você escolher
  - Personalize o horário (HH:MM) nas configurações
  - Notificação mostra quantos itens você tem na lista

- **🎉 Notificação de Conclusão de Lista**
  - Receba uma notificação quando completar todos os itens da lista
  - Celebra sua conquista e sugere criar uma nova lista
  - Pode ser habilitada/desabilitada nas configurações

- **⏰ Notificação de Itens Pendentes**
  - Alerta sobre itens que estão pendentes há vários dias
  - Threshold configurável (padrão: 7 dias)
  - Ajuda a não esquecer itens esquecidos na lista

- **⚙️ Configurações Completas**
  - Nova seção "Notificações" na tela de Configurações
  - Toggles para habilitar/desabilitar cada tipo de notificação
  - Seletores para personalizar horários e dias

#### 🛠️ Detalhes Técnicos

- **WorkManager**: 2.9.0 - Para agendamento de notificações recorrentes
- **3 Canais de Notificação**: Lembretes, Conclusão, Itens Pendentes
- **Token FCM**: Salvo localmente para futuras integrações
- **Compatibilidade**: Android 7.0+ (API 24+)

#### 📋 Arquivos Criados

- `FCMTokenManager.kt` - Gerenciamento de tokens FCM
- `NotificationPreferencesManager.kt` - Gerenciamento de preferências
- `NotificationHelper.kt` - Helper para criar notificações
- `DailyReminderWorker.kt` - Worker para lembretes diários
- `PendingItemsWorker.kt` - Worker para itens pendentes
- `NotificationScheduler.kt` - Agendamento de workers

#### 📋 Arquivos Modificados

- `SettingsScreen.kt` - UI de configurações de notificações
- `ItemCompraRepository.kt` - Verificação de conclusão de lista
- `ListaComprasViewModel.kt` - Integração com notificações
- `MainActivity.kt` - Inicialização de workers
- `MinhasComprasApplication.kt` - Criação de canais de notificação
- `MyFirebaseMessagingService.kt` - Salvamento de token FCM
- `build.gradle.kts` - Dependência WorkManager
- `gradle/libs.versions.toml` - Versão WorkManager

#### 🧪 Como Usar

1. Abra o app e vá em **Configurações**
2. Role até a seção **"Notificações"**
3. Configure cada tipo de notificação:
   - **Lembrete Diário**: Ative e escolha o horário
   - **Conclusão**: Ative para receber quando completar lista
   - **Itens Pendentes**: Ative e configure quantos dias

#### 📝 Notas

- As notificações funcionam mesmo com o app em segundo plano
- WorkManager garante que as notificações sejam agendadas corretamente
- Todas as configurações são salvas localmente usando DataStore

---

**Versão:** 2.19.0  
**Version Code:** 86  
**Data:** 24 de Dezembro de 2025

