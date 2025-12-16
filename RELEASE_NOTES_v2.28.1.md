# Release Notes v2.28.1

## Correções

### 🐛 Correção de Bug no Widget
- **Problema**: Widget ficava travado em "carregando..." e não exibia os itens da lista selecionada
- **Causa**: Uso incorreto de coroutines e funções suspend na implementação do widget
- **Solução**: 
  - Corrigido o uso de `runBlocking` para chamadas de funções suspend fora de contexto de coroutine
  - Ajustado o método `getListByIdSync()` no ShoppingListWidgetProvider
  - Removido uso incorreto de `CoroutineScope.launch` no ShoppingListWidgetService
  - Garantido que todas as operações de banco de dados no widget sejam executadas de forma síncrona

### 🔧 Melhorias Técnicas
- Refatoração do código do widget para melhor tratamento de coroutines
- Melhorada a sincronização de dados entre o app e o widget
- Otimizado o carregamento de itens no widget para evitar travamentos

---

## Instalação
1. Baixe o arquivo `app-release-v2.28.1.apk`
2. Instale o APK no seu dispositivo Android
3. O widget agora deve carregar corretamente os itens da lista selecionada

## Observações
- Esta é uma versão de correção focada em resolver o problema do widget
- Todas as outras funcionalidades permanecem inalteradas desde a v2.28.0