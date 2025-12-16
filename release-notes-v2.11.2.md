# Release v2.11.2 - Confirmação ao Deletar Itens

## 🔒 Melhoria de Segurança e UX

Esta versão adiciona confirmação antes de deletar itens individuais, prevenindo exclusões acidentais.

### ✨ Principais Melhorias

#### 🛡️ Confirmação de Exclusão
- **Diálogo de confirmação** - Agora pede confirmação antes de deletar um item individual
- **Previne exclusões acidentais** - Protege contra toques acidentais no botão de deletar
- **Funciona em ambos os métodos** - Confirmação tanto no swipe quanto no botão de deletar
- **Mensagem clara** - Mostra o nome do item que será deletado

#### 🎯 Experiência do Usuário
- **Feedback visual** - Diálogo de confirmação com nome do item
- **Opção de cancelar** - Usuário pode cancelar a exclusão facilmente
- **Mantém funcionalidade de desfazer** - Snackbar com opção "Desfazer" continua funcionando após confirmação

### 📝 Detalhes Técnicos
- Adicionado estado `itemParaDeletar` para controlar o diálogo
- Modificado comportamento do swipe para esquerda
- Modificado callback `onDelete` do `ItemCompraCard`
- Novo `AlertDialog` de confirmação seguindo o padrão do Material Design 3

### 🔄 Compatibilidade
- Mantém todas as funcionalidades anteriores
- Não altera o comportamento de deletar todos os itens comprados (já tinha confirmação)
- Compatível com versões anteriores do Android

---

**Versão:** 2.11.2  
**Version Code:** 45

