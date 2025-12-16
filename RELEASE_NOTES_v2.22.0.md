# Release Notes - v2.22.0

## 🎉 Nova Funcionalidade: Estatísticas Avançadas + Melhoria no Total a Pagar

Esta versão traz uma nova funcionalidade completa de estatísticas avançadas e uma melhoria importante no comportamento do "Total a Pagar", tornando-o mais útil e consistente durante as compras.

### ✨ Principais Mudanças

#### 📊 Estatísticas Avançadas (NOVO!)
- **Gráfico de gastos ao longo do tempo**: Visualize seus gastos em linha do tempo (diário, semanal, mensal)
- **Gráfico de pizza por categoria**: Veja a distribuição dos seus gastos por categoria
- **Comparação entre períodos**: Compare gastos entre diferentes períodos (semana, mês, 3 meses, ano)
- **Top itens mais comprados**: Lista dos itens que você mais compra com frequência e último preço
- **Filtros de período**: Escolha entre períodos pré-definidos ou crie um período personalizado
- **Performance otimizada**: Cache inteligente e debounce para carregamento rápido mesmo com muitos dados

#### 💰 Total a Pagar Fixo
- **Valor fixo**: O "Total a Pagar" agora mostra o total de TODOS os itens (comprados e não comprados)
- **Não diminui**: O valor não diminui quando você marca itens como comprados
- **Visibilidade constante**: A barra sempre aparece quando há itens na lista, mesmo que todos estejam comprados
- **Referência útil**: Permite ver o total original da lista enquanto faz as compras

#### 🔄 Comportamento Anterior vs. Novo
- **Antes**: Mostrava apenas itens não comprados e sumia quando todos estavam comprados
- **Agora**: Mostra o total completo e sempre visível quando há itens

### 🛠️ Melhorias Técnicas

#### Estatísticas Avançadas
- **Biblioteca Vico Charts**: Integração com biblioteca nativa Compose para gráficos performáticos
- **ViewModel otimizado**: Cache em memória e debounce para melhor performance
- **Queries otimizadas**: Consultas ao banco de dados otimizadas para estatísticas
- **Componentes reutilizáveis**: Gráficos modulares e reutilizáveis (linha, pizza, barras)
- **Filtros de período**: Sistema flexível de seleção de períodos (pré-definidos e personalizados)

#### Total a Pagar
- **Cálculo atualizado**: Removido filtro que excluía itens comprados do cálculo
- **Exibição melhorada**: Barra sempre visível quando há itens na lista
- **Experiência consistente**: Usuário sempre vê o total, independente do status dos itens

### 📱 Detalhes da Versão

- **Versão**: 2.22.0
- **Version Code**: 72
- **Android mínimo**: 7.0 (API 24)
- **Android alvo**: 14 (API 34)

### 🔐 Segurança

- APK assinado digitalmente
- Keystore configurado para releases futuras

### 📝 Notas de Migração

- **Sem mudanças de dados**: Esta atualização não requer migração de banco de dados
- **Compatibilidade total**: Funciona perfeitamente com versões anteriores
- **Comportamento melhorado**: Usuários terão uma experiência mais consistente

### 🐛 Correções

- Correção no comportamento do "Total a Pagar" para ser mais útil durante as compras
- Correção no sistema OTA de atualizações (URL do repositório GitHub)

### 🚀 Próximas Versões

- Templates de listas pré-configuradas (planejado)
- Sugestões inteligentes de categorias
- Compartilhamento de listas específicas
- Sincronização entre dispositivos (planejado)

---

**Data de Release**: 09/12/2025

**Desenvolvido com ❤️ para facilitar suas compras**

