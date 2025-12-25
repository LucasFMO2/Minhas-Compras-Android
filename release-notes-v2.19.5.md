## Release v2.19.5 - Correção de Alinhamento e Fluidez do TimePicker

### 🐛 Correção de Bugs

Esta versão corrige problemas críticos de alinhamento e fluidez no TimePicker que afetavam a usabilidade e precisão da configuração de lembretes diários.

### 🔧 Problemas Identificados e Soluções

**Problemas Corrigidos:**
- **Desalinhamento Vertical**: O horário selecionado não ficava alinhado com o separador ":" (dois pontos)
- **Interação Não Fluida**: Scroll apresentava ajustes bruscos e não suaves
- **Precisão Afetada**: Dificuldade em selecionar o horário exato desejado
- **Alinhamento Visual**: Separador ":" não estava na mesma linha dos números selecionados

**Causas Identificadas:**
- Separador ":" não estava alinhado verticalmente com o centro do viewport
- Cálculo de scroll offset impreciso não garantia centralização perfeita
- Detecção de fim de scroll muito rápida (150ms) causava ajustes prematuros
- Falta de threshold mínimo para evitar ajustes desnecessários quando já próximo do centro
- Múltiplos LaunchedEffect causando conflitos de sincronização

**Soluções Implementadas:**
- **Alinhamento do Separador**: Separador ":" agora envolto em Box com `height(240.dp)` e `contentAlignment = Alignment.Center` para alinhamento perfeito com o centro do viewport
- **Cálculo Preciso de Scroll Offset**: Uso de `viewportHeightPx` e cálculo correto de `viewportCenterPx` para garantir centralização exata
- **Detecção Melhorada**: Delay aumentado para 200ms para dar mais tempo ao scroll natural
- **Threshold Inteligente**: Adicionado threshold de 5px (`minDistance > 5`) para evitar ajustes desnecessários quando já próximo do centro
- **Cálculo de Posição Aprimorado**: Uso explícito de `currentItemTop` e `currentItemCenter` para cálculos mais precisos
- **Sincronização Otimizada**: Delays aumentados para 200ms nas animações para maior suavidade

### ✅ Melhorias Técnicas

- **Alinhamento Perfeito**: Separador ":" agora está sempre alinhado com os números selecionados
- **Scroll Mais Suave**: Interação fluida sem ajustes bruscos ou saltos visuais
- **Precisão Aprimorada**: Seleção de horário mais precisa e confiável
- **Performance Otimizada**: Menos ajustes desnecessários durante o scroll
- **Experiência do Usuário**: Interface mais polida e profissional

### 🎯 Detalhes da Implementação

- Separador ":" envolvido em `Box` com altura fixa de 240.dp e alinhamento central
- Cálculo de `scrollOffsetPx` usando `viewportCenterPx - itemCenterOffsetPx`
- Delay de 200ms para detecção de fim de scroll (antes 150ms)
- Threshold de 5px para evitar ajustes quando já próximo do centro
- Cálculo de posição usando `currentItemTop` e `currentItemCenter` explicitamente
- Verificações de `prevItemTop` e `nextItemTop` para itens adjacentes

### 📋 Detalhes Técnicos

- **Version Code**: 91
- **Version Name**: 2.19.5
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes. As configurações de notificação existentes serão preservadas.

### ⚠️ Importante

Esta é uma correção importante que melhora significativamente a experiência de uso do seletor de horário. Recomendamos atualizar para uma experiência mais fluida e precisa.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)

