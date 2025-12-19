# Minhas Compras v2.18.1 - Correção de Crash

## 🐛 Correções

- **Correção crítica**: Resolvido problema que causava o fechamento do aplicativo ao adicionar ou editar itens
- **Melhoria na estabilidade**: Implementado tratamento robusto de exceções em todas as camadas do aplicativo
- **Logs detalhados**: Adicionado sistema de logs completo para facilitar diagnóstico de problemas futuros
- **Validação de dados**: Implementada validação adicional antes de inserir itens no banco de dados
- **Integridade do banco**: Melhorada verificação e criação da lista padrão (shopping_lists)

## 🔧 Detalhes Técnicos

- **Tratamento de exceções**: ViewModel, Repository e DAO agora capturam e tratam exceções adequadamente
- **Validação de entrada**: Verificação de nome, quantidade e preço antes de processar
- **Integridade referencial**: Garantia da existência do registro padrão em shopping_lists (id=1)
- **Logs de diagnóstico**: Logs detalhados em todo o fluxo de adição/edição de itens
- **Feedback ao usuário**: Mensagens de erro via Snackbar quando aplicável

## 📱 Compatibilidade

- **Versão mínima**: Android 7.0 (API 24)
- **Versão alvo**: Android 14 (API 34)
- **Arquitetura**: ARM64, ARM32, x86, x86_64

---

**Esta versão foca em melhorar a estabilidade e resolver o problema reportado de crash ao adicionar itens.**