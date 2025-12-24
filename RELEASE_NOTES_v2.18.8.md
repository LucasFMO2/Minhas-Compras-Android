# 🚀 Release v2.18.8 - Firebase Cloud Messaging

## 🎉 Nova Funcionalidade Principal

### 📱 Firebase Cloud Messaging (FCM) - Notificações Push

Esta versão introduz suporte completo para notificações push em tempo real usando Firebase Cloud Messaging!

#### ✨ Funcionalidades Implementadas

- **🔔 Notificações Push em Tempo Real**
  - Recebimento de notificações push do Firebase
  - Funciona mesmo com o app em segundo plano ou fechado
  - Notificações aparecem na barra de status

- **🔐 Solicitação Automática de Permissão**
  - Solicitação automática de permissão de notificações (Android 13+)
  - Experiência do usuário otimizada

- **📲 Integração Completa**
  - Token FCM registrado automaticamente
  - Canal de notificação configurado
  - PendingIntent para abrir o app ao tocar na notificação

#### 🛠️ Detalhes Técnicos

- **Firebase BOM**: 33.7.0
- **Firebase Messaging KTX**: Integrado
- **Google Services Plugin**: 4.4.2
- **Compatibilidade**: Android 7.0+ (API 24+)

#### 📋 Arquivos Modificados

- `app/google-services.json` - Configuração do Firebase
- `app/src/main/java/com/example/minhascompras/MyFirebaseMessagingService.kt` - Serviço FCM
- `app/src/main/java/com/example/minhascompras/MinhasComprasApplication.kt` - Canal de notificação
- `app/src/main/java/com/example/minhascompras/MainActivity.kt` - Solicitação de permissão
- `app/src/main/AndroidManifest.xml` - Registro do serviço FCM
- `build.gradle.kts` - Plugin Google Services
- `app/build.gradle.kts` - Dependências Firebase
- `gradle/libs.versions.toml` - Versões Firebase

#### 🧪 Como Testar

1. Instale o app em um dispositivo Android
2. Na primeira abertura, permita notificações quando solicitado
3. Acesse o Firebase Console e envie uma notificação de teste
4. Verifique se a notificação aparece corretamente

#### 📝 Notas

- Esta versão requer conexão com a internet para receber notificações
- Notificações funcionam em Android 7.0+ (API 24+)
- Permissão de notificação é obrigatória em Android 13+ (API 33+)

---

**Versão:** 2.18.8  
**Version Code:** 85  
**Data:** 24 de Dezembro de 2025

