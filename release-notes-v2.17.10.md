## Release v2.17.10

### 🐛 Correção Importante

**Correção do Problema de Reutilização de Listas Arquivadas**

- **Problema corrigido**: Ao reutilizar uma lista arquivada do histórico, a lista original agora volta a aparecer corretamente em "listas ativas"
- **Implementação**: Adicionada lógica para desarquivar automaticamente a lista original quando uma lista arquivada é reutilizada
- **Comportamento**: Quando você reutiliza uma lista do histórico que foi arquivada, o sistema agora:
  1. Copia os itens para a lista ativa
  2. Desarquiva automaticamente a lista original
  3. Remove o histórico após a reutilização

### 📋 Detalhes Técnicos

- **Version Code**: 76
- **Version Name**: 2.17.10
- **Target SDK**: 34
- **Min SDK**: 24

### 🔧 Mudanças Implementadas

1. **ItemCompraRepository.reuseHistoryList()**
   - Adicionada verificação se o histórico tem `listId` associado
   - Implementada lógica para desarquivar a lista original quando necessário
   - A lista original agora volta a aparecer em "listas ativas" após reutilização

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

### ⚠️ Nota Importante

Esta correção resolve um problema onde listas arquivadas desapareciam permanentemente ao serem reutilizadas. Agora, ao reutilizar uma lista do histórico, ela volta a aparecer corretamente na lista de listas ativas.

