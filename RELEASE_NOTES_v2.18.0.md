# Release Notes - v2.18.0

## 🎉 Nova Funcionalidade: Arquivamento de Listas

Esta versão introduz uma melhoria importante na organização das listas de compras: **listas arquivadas agora desaparecem automaticamente do drawer**!

### ✨ Principais Novidades

#### 📦 Arquivamento Inteligente
- **Listas arquivadas ocultas**: Quando você arquiva uma lista, ela desaparece automaticamente do menu lateral (drawer)
- **Organização melhorada**: Mantenha apenas listas ativas visíveis, reduzindo desordem na interface
- **Proteção da lista padrão**: A lista "Minhas Compras" não pode ser arquivada, garantindo que você sempre tenha uma lista disponível

#### 🔄 Comportamento Automático
- **Mudança automática**: Se a lista ativa for arquivada, o app muda automaticamente para a lista padrão
- **Histórico preservado**: Itens arquivados continuam disponíveis no histórico, como antes
- **Transição suave**: A experiência é fluida e intuitiva

### 🛠️ Melhorias Técnicas

- **Migração de banco de dados**: Nova migração (versão 6→7) adiciona campo `isArchived` na tabela de listas
- **Filtragem otimizada**: Queries do banco filtram automaticamente listas arquivadas
- **Performance mantida**: Nenhum impacto negativo na performance do app

### 📱 Detalhes da Versão

- **Versão**: 2.18.0
- **Version Code**: 68
- **Android mínimo**: 7.0 (API 24)
- **Android alvo**: 14 (API 34)

### 🔐 Segurança

- APK assinado digitalmente
- Keystore configurado para releases futuras

### 📝 Notas de Migração

- **Dados preservados**: Todas as listas existentes serão mantidas (nenhuma será arquivada automaticamente)
- **Migração automática**: O banco de dados será atualizado automaticamente ao atualizar o app
- **Sem perda de dados**: A migração é segura e reversível
- **Compatibilidade**: Funciona perfeitamente com versões anteriores

### 🐛 Correções

- Melhorias na lógica de arquivamento
- Correções menores na interface

### 🚀 Próximas Versões

- Restaurar listas arquivadas (planejado)
- Filtros avançados por lista
- Compartilhamento de listas específicas
- Sincronização entre dispositivos (planejado)

---

**Data de Release**: 07/01/2025

**Desenvolvido com ❤️ para facilitar suas compras**

