## Release v2.19.1 - TimePicker Moderno com Colunas Roláveis

### ✨ Nova Funcionalidade

Esta versão introduz um **TimePicker moderno e intuitivo** com colunas roláveis para seleção de horário do lembrete diário nas configurações de notificações.

### 🎨 Melhorias na Interface

**Novo TimePicker com Colunas Roláveis:**
- **Seleção Visual Intuitiva**: Duas colunas verticais roláveis para horas (0-23) e minutos (0-59)
- **Destaque Visual**: Item selecionado destacado com fundo arredondado e texto em negrito
- **Scroll Suave**: Navegação fluida através das opções de horário
- **Auto-Ajuste Inteligente**: Quando você para de rolar, o seletor ajusta automaticamente para o item mais próximo
- **Design Moderno**: Interface alinhada com Material Design 3 e tema do aplicativo
- **Botão de Confirmação**: Botão circular com ícone de checkmark para confirmar a seleção

### 🎯 Experiência do Usuário

**Antes:**
- Seleção de horário usando botões de incremento/decremento (▲▼)
- Interface menos intuitiva e visualmente menos atraente

**Agora:**
- Seleção por scroll direto nas colunas de horas e minutos
- Visual moderno e profissional
- Experiência similar ao TimePicker nativo do Android
- Mais rápido e intuitivo para selecionar horários

### 📱 Como Usar

1. Vá em **Configurações** → **Notificações**
2. Ative o **Lembrete Diário** (se ainda não estiver ativo)
3. Toque no card de **Horário** para abrir o seletor
4. Role as colunas para selecionar a hora e minuto desejados
5. Toque em **Confirmar** para salvar

### ✅ Melhorias Técnicas

- Componente `ScrollableTimePicker` reutilizável criado
- Uso de `LazyColumn` para performance otimizada
- Sincronização automática entre scroll e seleção
- Suporte completo a temas claro e escuro
- Acessibilidade mantida com descrições adequadas

### 📋 Detalhes Técnicos

- **Version Code**: 87
- **Version Name**: 2.19.1
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes. As configurações de notificação existentes serão preservadas.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)

