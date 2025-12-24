## Release v2.17.9

### 🐛 Correção Crítica

**Correção do Sistema de Arquivamento de Listas**

- **Problema corrigido**: Listas arquivadas agora desaparecem corretamente da visualização de "listas ativas" após o arquivamento
- **Implementação**: Adicionado campo `isArchived` na entidade `ShoppingList` para rastrear o estado de arquivamento
- **Migração de banco**: Atualização automática do banco de dados (versão 8 → 9) para adicionar suporte ao novo campo
- **Filtro de listas**: Query `getAllLists()` agora filtra automaticamente listas arquivadas (`WHERE isArchived = 0`)

### 📋 Detalhes Técnicos

- **Version Code**: 75
- **Version Name**: 2.17.9
- **Target SDK**: 34
- **Min SDK**: 24
- **Migração de Banco**: Versão 8 → 9 (adiciona coluna `isArchived`)

### 🔧 Mudanças Implementadas

1. **Entidade ShoppingList**
   - Adicionado campo `isArchived: Boolean = false`

2. **ItemCompraRepository**
   - Modificado `archiveCurrentList()` para marcar lista como arquivada após mover itens para histórico
   - Adicionado `ShoppingListDao` como dependência para permitir atualização da lista

3. **ShoppingListDao**
   - Query `getAllLists()` atualizada para filtrar listas arquivadas

4. **AppDatabase**
   - Nova migração `MIGRATION_8_9` para adicionar coluna `isArchived`

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

### ⚠️ Nota Importante

Esta atualização inclui uma migração automática do banco de dados. O processo é transparente e não requer ação do usuário. Todas as listas existentes serão preservadas e marcadas como não arquivadas por padrão.

