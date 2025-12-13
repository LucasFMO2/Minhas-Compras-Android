# Minhas Compras v2.28.8 - Release Notes

## Correções Importantes

### 🐛 Correção de Cliques no Widget

Resolvemos dois problemas críticos que estavam impedindo o funcionamento correto dos cliques nos itens do widget:

1. **Conflito no Request Code do PendingIntent**
   - Corrigido o problema onde múltiplos itens poderiam ter o mesmo request code
   - Implementada nova fórmula: `(appWidgetId * 1000 + (item.id % 1000)).toInt()` 
   - Garante unicidade dos request codes para cada item do widget

2. **Layout sem Clique Habilitado**
   - Adicionadas propriedades `android:clickable="true"` e `android:focusable="true"` 
   - Corrigido nos arquivos `widget_item.xml` e `widget_item_small.xml`
   - Agora os itens do widget respondem corretamente aos toques

### 📱 Arquivos Modificados

- `app/src/main/java/com/example/minhascompras/widget/ShoppingListWidgetService.kt`
  - Correção na geração de request codes únicos para PendingIntent

- `app/src/main/res/layout/widget_item.xml`
  - Adicionadas propriedades de clique e foco

- `app/src/main/res/layout/widget_item_small.xml`
  - Adicionadas propriedades de clique e foco

## Detalhes Técnicos

- Versão: 2.28.8
- VersionCode: 86
- Build: Release assinado
- APK: `MinhasCompras-v2.28.8-code86.apk`

## Testes Realizados

- Validação do funcionamento dos cliques em diferentes tamanhos de widget
- Verificação da resposta correta ao marcar itens como comprados
- Testes de compatibilidade com dispositivos Android variados

---

Obrigado por usar o Minhas Compras! Esta versão corrige os problemas reportados com o widget e melhora a experiência do usuário.