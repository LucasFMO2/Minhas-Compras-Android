## Release v2.19.3 - Correção Crítica do TimePicker

### 🐛 Correção de Bug Crítico

Esta versão corrige um bug crítico no TimePicker onde os números rolavam automaticamente até 22:58 e não permitiam scroll manual, tornando o componente completamente inutilizável.

### 🔧 Problema Identificado e Solução

**Problema Crítico:**
- Ao abrir o seletor de horário, os números rolavam automaticamente até pararem em 22:58
- Não era possível fazer scroll manual para frente ou para trás
- Componente ficava completamente travado e inutilizável
- Loop infinito causado por LaunchedEffect disparando repetidamente

**Causa Raiz:**
- LaunchedEffect estava sendo disparado repetidamente criando um loop infinito
- `animateScrollToItem` era chamado mesmo quando o item já estava selecionado
- Cálculo incorreto não considerava o espaçador superior de 90dp
- Falta de flags para prevenir re-disparos durante scrolls programáticos

**Solução Implementada:**
- **Flags Anti-Loop**: Adicionadas `isScrollingToHour` e `isScrollingToMinute` para evitar loops infinitos
- **Remoção de Scroll Desnecessário**: Removido `animateScrollToItem` quando item já está selecionado
- **Cálculo Corrigido**: Considera espaçador superior de 90dp no cálculo de posição
- **Lógica Aprimorada**: Verifica item anterior, atual e próximo para escolher o mais próximo do centro
- **Delays Ajustados**: Aumentado delay para 200ms para evitar detecções prematuras
- **Verificações de Segurança**: Múltiplas checagens antes de processar scroll

### ✅ Melhorias Técnicas

- **Eliminação de Loops Infinitos**: Flags previnem re-disparos de LaunchedEffect
- **Scroll Manual Funcional**: Usuário pode rolar livremente sem interferência
- **Centralização Correta**: Item selecionado é centralizado corretamente após scroll
- **Performance Otimizada**: Menos chamadas desnecessárias de animateScrollToItem
- **Comportamento Previsível**: TimePicker funciona de forma consistente e confiável

### 🎯 Detalhes da Implementação

- Flags `isScrollingToHour` e `isScrollingToMinute` usando `remember { mutableStateOf(false) }`
- Cálculo de posição: `itemTopPosition = spacerHeightPx + offset`
- Verificação tripla: anterior, atual e próximo item para escolher o mais próximo do centro
- Delay de 200ms para detecção após scroll e 300ms para aguardar animações
- Verificações duplas antes de processar para evitar condições de corrida

### 📋 Detalhes Técnicos

- **Version Code**: 89
- **Version Name**: 2.19.3
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes. As configurações de notificação existentes serão preservadas.

### ⚠️ Importante

Esta é uma correção crítica. Recomendamos atualizar imediatamente se você estava enfrentando problemas ao selecionar horário do lembrete diário.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)

