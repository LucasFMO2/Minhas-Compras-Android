# Notas de Lançamento - Minhas Compras v2.28.5

## Correções de Bugs do Widget

Esta versão contém correções importantes para resolver problemas identificados no funcionamento do widget do aplicativo:

### 🔧 Correções Implementadas

1. **Substituição do runBlocking por abordagem assíncrona**
   - Removido o uso de `runBlocking` no `ShoppingListWidgetService`
   - Implementada solução assíncrona adequada para melhor performance e responsividade
   - Evita bloqueios da thread principal durante operações do widget

2. **Resolução da race condition na marcação de itens**
   - Corrigida a condição de corrida que ocorria ao marcar itens como comprados
   - Implementada sincronização adequada para evitar estados inconsistentes
   - Garante que a marcação de itens seja processada corretamente

3. **Melhoria na sincronização de dados entre banco e widget**
   - Otimizada a comunicação entre o banco de dados e o widget
   - Reduzida a latência na atualização de informações do widget
   - Implementada atualização mais eficiente e confiável dos dados

4. **Adição do método getItemById() no DAO**
   - Implementado novo método `getItemById()` no `ItemCompraDao`
   - Permite acesso direto e eficiente a itens específicos pelo ID
   - Melhora o desempenho das operações do widget que dependem de itens específicos

### 🎯 Benefícios

- Melhor desempenho e responsividade do widget
- Menor consumo de recursos do dispositivo
- Sincronização mais confiável entre o aplicativo e o widget
- Eliminação de travamentos e lentidão ao usar o widget

---

**Versão:** 2.28.5  
**Código da Versão:** 83  
**Data de Lançamento:** 13/12/2023