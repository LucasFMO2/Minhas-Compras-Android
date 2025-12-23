## Release v2.18.1 - Correção de Bug Crítico

### 🐛 Correção Crítica de Crash

Esta versão corrige um bug crítico que causava crash do aplicativo quando o usuário tentava criar a primeira lista.

### 🔧 Correção Aplicada

**Problema Identificado:**
- O aplicativo crashava ao clicar no botão "Criar Primeira Lista" na tela de boas-vindas
- O crash ocorria devido a uma chamada desnecessária para fechar o drawer de navegação
- A chamada `scope.launch { drawerState.close() }` estava sendo executada em um contexto onde o drawer não estava disponível

**Solução:**
- Removida a chamada problemática que tentava fechar o drawer desnecessariamente
- O botão "Criar Primeira Lista" agora abre o diálogo de criação sem tentar manipular o drawer
- Melhorada a estabilidade do fluxo de criação de primeira lista

### ✅ Melhorias

- **Estabilidade**: Aplicativo não crasha mais ao criar a primeira lista
- **Experiência do Usuário**: Fluxo de criação de lista mais suave e confiável
- **Robustez**: Código mais seguro e sem operações desnecessárias

### 📋 Detalhes Técnicos

- **Version Code**: 78
- **Version Name**: 2.18.1
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)

