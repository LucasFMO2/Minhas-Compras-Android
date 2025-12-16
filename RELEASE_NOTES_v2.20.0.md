# Release Notes - v2.20.0

## 🎉 Mudança Importante: Remoção da Lista Padrão Fixa

Esta versão remove a lista padrão "Minhas Compras" que era criada automaticamente. Agora **você tem controle total** sobre suas listas de compras!

### ✨ Principais Mudanças

#### 🗑️ Remoção da Lista Padrão
- **Sem lista fixa**: A lista "Minhas Compras" não é mais criada automaticamente
- **Controle total**: Você decide quando e quais listas criar
- **Organização personalizada**: Crie listas com nomes que façam sentido para você
- **Migração automática**: Se você tinha a lista padrão, ela será removida automaticamente na atualização

#### 🔄 Melhorias no Fluxo
- **Experiência mais limpa**: Sem listas pré-criadas que você não precisa
- **Flexibilidade**: Crie quantas listas quiser, quando quiser
- **Organização**: Cada lista pode ter um propósito específico (Supermercado, Farmácia, etc.)

### 🛠️ Melhorias Técnicas

- **Nova migração de banco**: Migração 7→8 remove automaticamente a lista padrão existente
- **Lógica atualizada**: Sistema agora lida com ausência de lista padrão de forma elegante
- **Fallback inteligente**: Se não houver lista ativa, o sistema tenta usar a primeira lista disponível
- **Validações aprimoradas**: Melhor tratamento de casos onde não há listas criadas

### 📱 Detalhes da Versão

- **Versão**: 2.20.0
- **Version Code**: 70
- **Android mínimo**: 7.0 (API 24)
- **Android alvo**: 14 (API 34)

### 🔐 Segurança

- APK assinado digitalmente
- Keystore configurado para releases futuras

### 📝 Notas de Migração

- **Migração automática**: Se você tinha a lista padrão "Minhas Compras", ela será removida automaticamente
- **Dados preservados**: Todos os seus produtos e outras listas serão mantidos
- **Primeira execução**: Na primeira vez após atualizar, você precisará criar uma lista antes de adicionar produtos
- **Compatibilidade**: Funciona perfeitamente com versões anteriores

### ⚠️ Importante

- **Crie uma lista primeiro**: Lembre-se de criar uma lista de compras antes de adicionar produtos
- **Sem lista padrão**: Não há mais uma lista criada automaticamente para você
- **Organize suas listas**: Use nomes descritivos para suas listas (ex: "Supermercado", "Farmácia", "Feira")

### 🐛 Correções

- Remoção de verificações desnecessárias relacionadas à lista padrão
- Melhorias na lógica de fallback quando não há lista ativa
- Correções na interface para lidar com ausência de lista selecionada

### 🚀 Próximas Versões

- Templates de listas pré-configuradas (planejado)
- Sugestões inteligentes de categorias
- Compartilhamento de listas específicas
- Sincronização entre dispositivos (planejado)

---

**Data de Release**: 09/12/2025

**Desenvolvido com ❤️ para facilitar suas compras**

