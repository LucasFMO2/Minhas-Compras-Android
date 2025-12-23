## Release v2.18.3 - Seleção Automática de Lista

### ✨ Melhoria de UX

Esta versão corrige um problema de experiência do usuário onde a lista recém-criada não era selecionada automaticamente.

### 🔧 Correção Aplicada

**Problema Identificado:**
- Ao criar uma nova lista, o usuário precisava selecioná-la manualmente no drawer
- A lista criada não era automaticamente definida como lista ativa
- Isso criava uma experiência confusa, especialmente para novos usuários

**Solução:**
- A lista recém-criada agora é automaticamente selecionada como lista ativa
- O ID da lista é salvo diretamente no DataStore após a criação
- A UI atualiza imediatamente para mostrar a lista recém-criada como ativa
- Usuário pode começar a adicionar itens imediatamente após criar a lista

### ✅ Melhorias

- **UX Aprimorada**: Lista recém-criada é selecionada automaticamente
- **Fluxo Mais Intuitivo**: Usuário pode começar a usar a lista imediatamente
- **Performance**: Seleção direta no DataStore, sem verificações desnecessárias
- **Feedback Imediato**: UI atualiza instantaneamente após criar a lista

### 📋 Detalhes Técnicos

- **Version Code**: 80
- **Version Name**: 2.18.3
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024  
**Compatibilidade**: Android 7.0+ (API 24+)
