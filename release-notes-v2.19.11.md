# Release Notes v2.19.11

## Correções

### 🐛 Bug Fix: Seletor de Horário do Lembrete Diário
- **Problema:** O seletor de horário do lembrete diário se movia sozinho até 21:58 e travava, impedindo a seleção de outros horários
- **Causa:** A função `calculateCenterItem` usava truncamento em vez de arredondamento, causando imprecisão na detecção do item central do viewport
- **Solução:**
  - Alterado de `.toInt()` para `.roundToInt()` para arredondamento correto
  - Adicionada proteção contra loops infinitos nos LaunchedEffects de detecção de scroll
  - Alterado scroll inicial de `scrollToItem` para `animateScrollToItem` para movimento mais suave
- **Resultado:** O seletor de horário agora funciona corretamente, permitindo seleção de qualquer horário sem movimento automático ou travamento

## Detalhes Técnicos

### Arquivos Modificados
- `app/src/main/java/com/example/minhascompras/ui/screens/SettingsScreen.kt`
  - Função `calculateCenterItem`: Arredondamento correto
  - LaunchedEffects de detecção de scroll: Proteção contra loops
  - Scroll inicial: Movimento mais suave

### Versão
- **Version Code:** 97
- **Version Name:** 2.19.11

---

**Data:** 26 de Dezembro de 2025
