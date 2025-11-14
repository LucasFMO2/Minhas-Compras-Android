# Release v2.11.3 - Interface Mais Limpa com Clique Longo

## 🎨 Melhoria de Interface e UX

Esta versão remove os ícones de editar e excluir da tela principal, deixando a interface ainda mais limpa e minimalista. Agora você pode usar **clique longo** para acessar essas opções.

### ✨ Principais Melhorias

#### 🖱️ Clique Longo para Ações
- **Menu de contexto** - Clique longo em qualquer item abre um menu com opções
- **Opções disponíveis** - Editar e Excluir acessíveis via menu de contexto
- **Interface mais limpa** - Removidos os ícones de editar e excluir dos cards
- **Mais espaço na tela** - Mais conteúdo visível sem os botões

#### 🎯 Experiência do Usuário
- **Interface minimalista** - Cards ainda mais limpos e focados no conteúdo
- **Padrão familiar** - Clique longo é um padrão comum em apps Android
- **Feedback visual** - Menu aparece no ponto do toque
- **Mantém funcionalidades** - Swipe e checkbox continuam funcionando normalmente

### 📝 Detalhes Técnicos
- Removidos ícones de editar e excluir do `ItemCompraCard`
- Adicionada detecção de clique longo usando `pointerInput` e `detectTapGestures`
- Implementado `DropdownMenu` como menu de contexto
- Gerenciamento de estado thread-safe com `LaunchedEffect`

### 🔄 Compatibilidade
- Mantém todas as funcionalidades anteriores
- Swipe para marcar/deletar continua funcionando
- Checkbox para marcar como comprado continua funcionando
- Compatível com versões anteriores do Android

### 🎨 Design
- Interface ainda mais minimalista
- Foco total no conteúdo dos itens
- Melhor aproveitamento do espaço na tela
- Experiência mais fluida e intuitiva

---

**Versão:** 2.11.3  
**Version Code:** 46

