# Release v2.11.4 - Ações na TopBar com Seleção de Itens

## 🎯 Melhoria de UX e Padrão Material Design

Esta versão implementa um padrão mais intuitivo e profissional: ao fazer **clique longo** em um item, ele é selecionado e as ações de **Editar** e **Excluir** aparecem diretamente na TopBar, seguindo o padrão de apps como Gmail e Google Drive.

### ✨ Principais Melhorias

#### 📱 Ações na TopBar
- **Seleção de item** - Clique longo em qualquer item para selecioná-lo
- **Ações visíveis** - Botões de Editar e Excluir aparecem na TopBar quando um item está selecionado
- **Título dinâmico** - TopBar mostra o nome do item selecionado
- **Botão Cancelar** - Fácil de deselecionar o item

#### 🎨 Feedback Visual
- **Destaque do item** - Item selecionado recebe cor de destaque visual
- **Interface limpa** - Sem menus flutuantes, tudo na TopBar
- **Transição suave** - TopBar muda dinamicamente entre modo normal e modo de seleção

#### 🚀 Experiência do Usuário
- **Mais intuitivo** - Padrão familiar de apps Android modernos
- **Mais acessível** - Botões grandes e fáceis de tocar na TopBar
- **Mais profissional** - Segue as diretrizes do Material Design
- **Mantém funcionalidades** - Swipe e checkbox continuam funcionando normalmente

### 📝 Detalhes Técnicos
- Adicionado estado `itemSelecionado` para controlar seleção
- TopBar condicional: mostra ações diferentes baseado no estado de seleção
- Removido menu flutuante (DropdownMenu) do ItemCompraCard
- Adicionado parâmetro `isSelected` para destacar item visualmente
- Item é desmarcado automaticamente após ações ou fechamento de diálogos

### 🔄 Compatibilidade
- Mantém todas as funcionalidades anteriores
- Swipe para marcar/deletar continua funcionando
- Checkbox para marcar como comprado continua funcionando
- Compatível com versões anteriores do Android

### 🎨 Design
- Interface ainda mais profissional
- Segue padrões do Material Design 3
- Melhor experiência de uso
- Feedback visual claro e intuitivo

---

**Versão:** 2.11.4  
**Version Code:** 47

