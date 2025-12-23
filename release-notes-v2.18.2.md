## Release v2.18.2 - Correção Crítica de Banco de Dados

### 🐛 Correção Crítica de Crash

Esta versão corrige um bug crítico que causava crash do aplicativo devido a um problema de integridade do banco de dados Room.

### 🔧 Correção Aplicada

**Problema Identificado:**
- O aplicativo crashava com erro: `Room cannot verify the data integrity`
- O erro ocorria porque o schema do banco de dados foi alterado, mas a versão do banco não foi atualizada
- O Room detectou incompatibilidade entre o hash de identidade esperado e o encontrado

**Solução:**
- Versão do banco de dados incrementada de 9 para 10
- Migration 9->10 criada para atualizar o hash de identidade do schema
- O banco de dados agora será migrado corretamente sem perda de dados

### ✅ Melhorias

- **Estabilidade**: Aplicativo não crasha mais ao abrir devido a problemas de banco de dados
- **Migração Segura**: Dados existentes são preservados durante a migração
- **Integridade**: Schema do banco de dados agora está sincronizado com a versão do código

### 📋 Detalhes Técnicos

- **Version Code**: 79
- **Version Name**: 2.18.2
- **Database Version**: 10
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados. A migration 9->10 será executada automaticamente na primeira abertura do app.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)

