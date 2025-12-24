## Release v2.18.7 - Correção de Exclusão de Lista Arquivada

### 🐛 Correção Crítica

Esta versão corrige um problema crítico onde ao clicar no botão "Excluir" de uma lista arquivada, a lista era **desarquivada** e aparecia novamente nas listas ativas, em vez de ser **deletada**.

### 🔧 Problema Identificado e Solução

**Problema Anterior:**
- Botão "Excluir" em listas arquivadas estava desarquivando a lista em vez de deletar
- Lista arquivada aparecia novamente nas listas ativas após "exclusão"
- Apenas os itens eram removidos, mas a lista permanecia

**Solução Implementada:**
- Função `deleteHistory()` agora deleta completamente a lista arquivada
- Remove o histórico associado (ShoppingListHistory + HistoryItems via CASCADE)
- Remove a lista arquivada (ShoppingList + ItemCompra via CASCADE)
- Lista arquivada desaparece completamente do histórico e do app

### ✅ Melhorias

- **Exclusão Funcional**: Listas arquivadas são completamente removidas quando excluídas
- **Sem Regressão**: Lista excluída não reaparece nas listas ativas
- **Limpeza Completa**: Tanto lista quanto histórico são removidos
- **Experiência Consistente**: Usuários podem excluir listas arquivadas sem preocupações

### 📋 Detalhes Técnicos

- **Version Code**: 84
- **Version Name**: 2.18.7
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024
**Compatibilidade**: Android 7.0+ (API 24+)

