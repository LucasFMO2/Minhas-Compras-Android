# Release Notes v2.28.2

## Correções

### 🐛 Correção do Widget - Lista de Compras
- **Problema**: Widget não exibia os itens das listas, ficando travado em "carregando..."
- **Causa**: Uso inadequado de `runBlocking` em contextos de widget e falta de atualização automática
- **Solução**: 
  - Removido uso de `runBlocking` no ShoppingListWidgetProvider e ShoppingListWidgetService
  - Implementado CoroutineScope dedicado com Dispatchers.IO para operações assíncronas
  - Adicionada verificação de lista válida antes de carregar itens
  - Implementada atualização automática do widget quando itens são modificados no app
  - Adicionados logs detalhados para diagnóstico de problemas

### 🔧 Melhorias Técnicas
- Refatoração completa do sistema de widgets para melhor performance
- Implementado sistema de atualização automática do widget via ItemCompraRepository
- Adicionada verificação de integridade de listas configuradas no widget
- Melhorada tratamento de erros e logs para debugging

### 📋 Funcionalidades
- Widget agora atualiza automaticamente quando itens são adicionados/removidos
- Verificação automática se lista configurada ainda existe
- Logs detalhados para facilitar diagnóstico de problemas
- Melhorias na performance de carregamento do widget

---

## Instalação
1. Baixe o arquivo `MinhasCompras-v2.28.2-code81.apk`
2. Instale o APK no seu dispositivo Android
3. O widget agora deve exibir corretamente os itens das listas e atualizar automaticamente

## Observações
- Esta é uma versão de correção focada em resolver o problema do widget
- Todas as outras funcionalidades permanecem inalteradas desde a v2.28.1
- O widget agora se atualiza automaticamente quando você faz alterações no app