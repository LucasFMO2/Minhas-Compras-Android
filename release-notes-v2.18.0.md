## Release v2.18.0

### 🎯 Mudança Principal: Remoção da Lista Padrão Fixa

**Nova Experiência do Usuário - Criação de Listas Personalizadas**

Esta versão traz uma mudança significativa na experiência inicial do aplicativo. Agora, os usuários têm controle total sobre suas listas desde o primeiro acesso.

### ✨ Novidades

#### 1. **Sem Lista Padrão Automática**
- **Antes**: O app criava automaticamente uma lista "Minhas Compras" no primeiro acesso
- **Agora**: O usuário precisa criar sua primeira lista manualmente
- **Benefício**: Maior flexibilidade e personalização desde o início

#### 2. **Criação de Lista Obrigatória para Adicionar Itens**
- **Nova regra**: É necessário ter uma lista ativa para adicionar itens
- **Interface**: Tela de boas-vindas quando não há lista, com botão para criar a primeira lista
- **Feedback visual**: FAB (botão de adicionar) desabilitado quando não há lista ativa
- **Mensagens informativas**: Snackbars explicando a necessidade de criar uma lista

#### 3. **Melhorias na UX**
- **Estado vazio aprimorado**: Tela dedicada quando não há lista criada
- **Validações inteligentes**: Sistema valida se a lista existe antes de permitir operações
- **Mensagens de erro claras**: Feedback específico quando ações requerem lista ativa

### 🔧 Mudanças Técnicas

#### Banco de Dados
- **Migration atualizada**: Removida criação automática da lista padrão na migration 4_5
- **listId nullable**: Itens agora podem existir sem lista associada (preparação para futuras funcionalidades)
- **Foreign keys ajustadas**: Suporte para listas opcionais

#### ViewModels
- **ShoppingListViewModel**: Removida lógica de criação automática de lista padrão
- **ListaComprasViewModel**: Validação de lista ativa antes de todas as operações
- **HistoryViewModel**: Validação para reutilização de histórico

#### Interface
- **ListaComprasScreen**: Nova tela de estado vazio quando não há lista
- **FAB inteligente**: Desabilitado e com feedback quando não há lista ativa
- **Validações em tempo real**: Verificação de lista antes de permitir ações

### 📋 Detalhes Técnicos

- **Version Code**: 77
- **Version Name**: 2.18.0
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Migração para Usuários Existentes

- **Usuários com lista padrão existente**: Continuarão funcionando normalmente
- **Novos usuários**: Verão a nova experiência sem lista padrão
- **Sem perda de dados**: Todas as listas e itens existentes são preservados

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

### ⚠️ Nota Importante

Esta é uma mudança significativa na experiência do usuário. Usuários novos precisarão criar uma lista antes de começar a adicionar itens. Isso oferece maior controle e personalização desde o primeiro uso.

### 🐛 Correções

- Corrigido problema onde itens poderiam ser adicionados sem lista ativa
- Melhorada validação de lista em todas as operações
- Corrigido comportamento do FAB quando não há lista

### 🎨 Melhorias de Interface

- Nova tela de boas-vindas quando não há lista
- Feedback visual aprimorado para ações que requerem lista
- Mensagens de erro mais claras e informativas

---

**Data de Release**: Dezembro 2024
**Compatibilidade**: Android 7.0+ (API 24+)

