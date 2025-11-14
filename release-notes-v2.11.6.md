# Release v2.11.6 - Correção de Padding na BottomBar

## 🔧 Correção de UX

Esta versão corrige o problema de sobreposição da BottomBar com os botões de navegação do sistema Android.

### ✨ Correção Implementada

#### 📱 Ajuste de Padding
- **Respeita botões do sistema** - BottomBar agora não sobrepõe os botões de navegação do Android
- **Padding automático** - Usa `navigationBarsPadding()` para ajuste automático
- **Melhor visualização** - Conteúdo totalmente visível e acessível
- **Compatibilidade** - Funciona em todos os dispositivos Android

### 📝 Detalhes Técnicos
- Adicionado `navigationBarsPadding()` no modifier da BottomBar
- Import adicionado: `androidx.compose.foundation.layout.navigationBarsPadding`
- Padding aplicado automaticamente baseado nos WindowInsets do sistema

### 🔄 Compatibilidade
- Mantém todas as funcionalidades anteriores
- Correção de bug de UX
- Compatível com versões anteriores do Android

### 💡 Benefícios
- **Melhor experiência** - BottomBar não interfere mais com os botões do sistema
- **Visualização completa** - Todo o conteúdo está visível
- **Profissionalismo** - Interface respeita os padrões do Android

---

**Versão:** 2.11.6  
**Version Code:** 49

