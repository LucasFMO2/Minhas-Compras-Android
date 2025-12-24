## Release v2.18.6 - Correção de Múltiplos Cliques no Botão Reutilizar

### 🐛 Correção Crítica

Esta versão corrige um problema onde ao clicar no botão "Reutilizar" de uma lista arquivada, era necessário clicar 3 vezes ou mais para a lista ser ativada, e ainda apareciam 3 itens duplicados.

### 🔧 Problema Identificado e Solução

**Problema Anterior:**
- Botão "Reutilizar" não era desabilitado durante a operação, permitindo múltiplos cliques
- Race condition entre verificação de job ativo e atribuição do job
- Navegação acontecia antes da operação terminar
- Múltiplas inserções de itens duplicados na lista

**Solução Implementada:**
- Adicionado estado `isReusing` para controlar o estado de loading na UI
- Implementado `Mutex` para garantir atomicidade e prevenir race conditions
- Botão agora é desabilitado durante a operação com indicador visual de carregamento
- Navegação acontece apenas após a operação completar (via callback)
- Padrão double-checked locking para garantir thread-safety

### ✅ Melhorias

- **Proteção contra Cliques Múltiplos**: Botão desabilitado durante operação
- **Feedback Visual**: Indicador de carregamento e texto "Carregando..." durante processamento
- **Thread-Safety**: Mutex garante que apenas uma operação ocorra por vez
- **Experiência Consistente**: Lista é ativada corretamente com um único clique
- **Sem Duplicação**: Itens não são mais inseridos múltiplas vezes

### 📋 Detalhes Técnicos

- **Version Code**: 83
- **Version Name**: 2.18.6
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024
**Compatibilidade**: Android 7.0+ (API 24+)

