# Release Notes - v2.19.0

## 🎉 Nova Funcionalidade: Criação Obrigatória de Lista

Esta versão introduz uma melhoria importante no fluxo de uso do aplicativo: **agora é obrigatório criar uma lista de compras antes de adicionar produtos**!

### ✨ Principais Novidades

#### 📋 Criação Obrigatória de Lista
- **Validação inteligente**: O sistema agora exige que você crie pelo menos uma lista antes de adicionar produtos
- **Diálogo informativo**: Quando você tenta adicionar um produto sem ter criado uma lista, um diálogo aparece explicando a necessidade
- **Criação rápida**: O diálogo oferece a opção de criar uma lista diretamente, facilitando o fluxo
- **Proteção de dados**: Garante que todos os produtos sejam associados a listas criadas pelo usuário

#### 🔄 Melhorias no Fluxo
- **Experiência guiada**: Usuários novos são orientados a criar uma lista primeiro
- **Organização melhorada**: Força uma estrutura mais organizada desde o início
- **Validação em múltiplas camadas**: A validação ocorre tanto na interface quanto no backend

### 🛠️ Melhorias Técnicas

- **Novo método no DAO**: Adicionado `getNonDefaultListCount()` para contar listas criadas pelo usuário
- **StateFlow reativo**: Novo `nonDefaultListCount` no `ShoppingListViewModel` para observar listas em tempo real
- **Validação no ViewModel**: Método `inserirItem()` agora valida a existência de listas antes de inserir
- **Interface responsiva**: A interface se adapta automaticamente ao estado das listas

### 📱 Detalhes da Versão

- **Versão**: 2.19.0
- **Version Code**: 69
- **Android mínimo**: 7.0 (API 24)
- **Android alvo**: 14 (API 34)

### 🔐 Segurança

- APK assinado digitalmente
- Keystore configurado para releases futuras

### 📝 Notas de Migração

- **Dados preservados**: Todas as listas e produtos existentes serão mantidos
- **Compatibilidade**: Funciona perfeitamente com versões anteriores
- **Lista padrão**: A lista padrão "Minhas Compras" continua existindo, mas não conta como lista criada pelo usuário para validação

### 🐛 Correções

- Melhorias na validação de criação de listas
- Correções menores na interface

### 🚀 Próximas Versões

- Melhorias na experiência de criação de listas
- Templates de listas pré-configuradas
- Sugestões inteligentes de categorias
- Sincronização entre dispositivos (planejado)

---

**Data de Release**: 07/01/2025

**Desenvolvido com ❤️ para facilitar suas compras**

