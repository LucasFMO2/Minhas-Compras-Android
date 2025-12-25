## Release v2.19.2 - Correção de Alinhamento do TimePicker

### 🐛 Correção de Bug

Esta versão corrige um problema de alinhamento visual no TimePicker onde os itens selecionados nas colunas de horas e minutos não ficavam alinhados na mesma linha horizontal.

### 🔧 Problema Identificado e Solução

**Problema Anterior:**
- Itens selecionados nas colunas de horas e minutos não ficavam na mesma linha horizontal
- Visual desalinhado quando comparando horas e minutos
- Experiência visual inconsistente

**Solução Implementada:**
- Cálculo preciso do offset de centralização (`centerOffsetPx`)
- Uso de `scrollOffset` no `animateScrollToItem()` para garantir centralização
- Ambas as colunas usam o mesmo offset de centralização
- Lógica aprimorada de detecção após scroll para selecionar o item mais próximo do centro
- Centralização explícita após seleção para garantir alinhamento perfeito

### ✅ Melhorias Técnicas

- **Alinhamento Perfeito**: Itens selecionados agora ficam sempre na mesma linha horizontal
- **Centralização Consistente**: Ambas as colunas usam o mesmo cálculo de offset
- **Experiência Visual Aprimorada**: Interface mais profissional e polida
- **Comportamento Previsível**: Scroll sempre centraliza o item selecionado

### 🎯 Detalhes da Implementação

- Cálculo do offset: `centerOffsetPx = (containerHeight / 2) - (itemHeight / 2)`
- Uso de `animateScrollToItem()` com parâmetro `scrollOffset` para centralização
- Detecção inteligente do item mais próximo do centro após scroll do usuário
- Sincronização automática entre scroll e seleção

### 📋 Detalhes Técnicos

- **Version Code**: 88
- **Version Name**: 2.19.2
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes. As configurações de notificação existentes serão preservadas.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)

