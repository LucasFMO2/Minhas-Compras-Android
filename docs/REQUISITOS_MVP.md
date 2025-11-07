# 📋 Requisitos do MVP - Minhas Compras

**Versão:** 3.0 MVP  
**Prazo:** 3 semanas  
**Data de Criação:** Novembro 2024

---

## 📊 Status Atual do Projeto

### ✅ Recursos Já Implementados (v2.3)

#### **Funcionalidades Core**
- ✅ Adicionar itens com nome, quantidade e preço
- ✅ Editar itens existentes
- ✅ Deletar itens individuais
- ✅ Marcar itens como comprado/pendente
- ✅ Deletar todos os itens comprados
- ✅ Persistência local com Room Database
- ✅ Estatísticas básicas (total, pendentes, comprados)
- ✅ Estatísticas de preços (total geral, pendentes, comprados)
- ✅ Barra de progresso visual
- ✅ Interface Material Design 3
- ✅ Animações suaves
- ✅ Estado vazio com call-to-action

#### **Arquitetura**
- ✅ MVVM com ViewModel
- ✅ Room Database com DAO
- ✅ Repository Pattern
- ✅ Kotlin Coroutines
- ✅ Jetpack Compose
- ✅ StateFlow para reatividade

---

## 🎯 Escopo do MVP (3 Semanas)

### **Semana 1: Funcionalidades Essenciais**

#### 1.1 Busca e Filtros ⏱️ 2-3 dias
**Prioridade:** ALTA  
**Complexidade:** MÉDIA

**Requisitos:**
- [ ] Campo de busca na tela principal
- [ ] Busca em tempo real por nome do item
- [ ] Filtros: Todos / Pendentes / Comprados
- [ ] Indicador visual do filtro ativo
- [ ] Limpar busca/filtro facilmente

**Critérios de Aceitação:**
- Busca funciona enquanto o usuário digita
- Filtros atualizam a lista instantaneamente
- Busca e filtros funcionam em conjunto
- Performance mantida com muitos itens

---

#### 1.2 Ordenação de Itens ⏱️ 1-2 dias
**Prioridade:** MÉDIA  
**Complexidade:** BAIXA

**Requisitos:**
- [ ] Menu de ordenação (dropdown ou bottom sheet)
- [ ] Opções: Nome (A-Z), Data (mais recente), Preço (menor-maior)
- [ ] Indicador visual da ordenação atual
- [ ] Persistir preferência de ordenação

**Critérios de Aceitação:**
- Ordenação funciona em todos os filtros
- Performance mantida com muitos itens
- UI intuitiva e acessível

---

#### 1.3 Categorias Básicas ⏱️ 3-4 dias
**Prioridade:** ALTA  
**Complexidade:** MÉDIA-ALTA

**Requisitos:**
- [ ] Adicionar campo "categoria" ao modelo ItemCompra
- [ ] Lista pré-definida de categorias (Frutas, Laticínios, Carnes, Padaria, Limpeza, Bebidas, etc.)
- [ ] Seleção de categoria no dialog de adicionar/editar
- [ ] Exibir categoria no card do item (badge/chip)
- [ ] Filtro por categoria (opcional - se sobrar tempo)

**Categorias Sugeridas:**
- Frutas e Verduras
- Laticínios
- Carnes e Aves
- Padaria
- Limpeza
- Higiene
- Bebidas
- Grãos e Cereais
- Outros

**Critérios de Aceitação:**
- Categoria é obrigatória ao adicionar item
- Migração de dados existentes (categoria padrão "Outros")
- Visual consistente com Material Design 3

---

### **Semana 2: Melhorias de UX e Funcionalidades**

#### 2.1 Modo Escuro/Claro ⏱️ 1-2 dias
**Prioridade:** MÉDIA  
**Complexidade:** BAIXA

**Requisitos:**
- [ ] Toggle de tema no TopBar ou menu de configurações
- [ ] Suporte a tema claro e escuro
- [ ] Persistir preferência do usuário
- [ ] Transição suave entre temas
- [ ] Seguir preferência do sistema (opcional)

**Critérios de Aceitação:**
- Todos os componentes respeitam o tema
- Cores contrastantes para acessibilidade
- Preferência persiste após fechar o app

---

#### 2.2 Gestos e Ações Rápidas ⏱️ 2-3 dias
**Prioridade:** MÉDIA  
**Complexidade:** MÉDIA

**Requisitos:**
- [ ] Swipe para direita: marcar como comprado
- [ ] Swipe para esquerda: deletar item
- [ ] Feedback visual durante o swipe
- [ ] Snackbar com ação "Desfazer" após deletar
- [ ] Long press para menu de ações rápidas (opcional)

**Critérios de Aceitação:**
- Gestos funcionam de forma intuitiva
- Feedback visual claro
- Desfazer funciona corretamente

---

#### 2.3 Melhorias no Dialog ⏱️ 1 dia
**Prioridade:** BAIXA  
**Complexidade:** BAIXA

**Requisitos:**
- [ ] Autocompletar baseado em itens anteriores
- [ ] Sugestões de itens frequentes
- [ ] Validação melhorada de campos
- [ ] Feedback visual de erros

**Critérios de Aceitação:**
- Autocompletar acelera a entrada de dados
- Sugestões são relevantes
- Validação previne erros

---

### **Semana 3: Backup e Polimento**

#### 3.1 Backup e Restauração ⏱️ 3-4 dias
**Prioridade:** ALTA  
**Complexidade:** MÉDIA-ALTA

**Requisitos:**
- [ ] Menu de configurações/sobre
- [ ] Opção "Exportar dados" (JSON)
- [ ] Opção "Importar dados" (JSON)
- [ ] Compartilhar lista via texto/WhatsApp
- [ ] Dialog de confirmação para importação
- [ ] Validação de arquivo JSON

**Critérios de Aceitação:**
- Exportação gera arquivo JSON válido
- Importação valida e trata erros
- Compartilhamento funciona em apps externos
- Backup não corrompe dados existentes

---

#### 3.2 Histórico de Compras ⏱️ 2-3 dias
**Prioridade:** MÉDIA  
**Complexidade:** MÉDIA

**Requisitos:**
- [ ] Salvar lista quando todos os itens forem comprados
- [ ] Tela de histórico com listas anteriores
- [ ] Visualizar lista histórica
- [ ] Reutilizar lista histórica (criar nova lista baseada nela)
- [ ] Deletar histórico

**Critérios de Aceitação:**
- Histórico salva automaticamente
- Reutilização cria nova lista independente
- Performance mantida com muitos históricos

---

#### 3.3 Testes e Correções ⏱️ 2-3 dias
**Prioridade:** ALTA  
**Complexidade:** VARIÁVEL

**Requisitos:**
- [ ] Testes unitários básicos (ViewModel)
- [ ] Testes de UI básicos (Compose Testing)
- [ ] Correção de bugs encontrados
- [ ] Melhorias de performance
- [ ] Ajustes de acessibilidade
- [ ] Revisão de código

**Critérios de Aceitação:**
- Cobertura mínima de testes (30-40%)
- App funciona sem crashes
- Performance aceitável

---

## 📝 Recursos Adicionais Importantes (Fora do MVP)

### **Pós-MVP (v3.1+)**

#### 🔄 Múltiplas Listas
- Criar e gerenciar múltiplas listas
- Navegação entre listas
- Renomear/deletar listas

#### 📱 Widget
- Widget na tela inicial
- Adicionar itens rapidamente
- Ver progresso sem abrir app

#### 🔔 Notificações
- Lembrete para ir às compras
- Notificação quando lista estiver pronta

#### 📊 Estatísticas Avançadas
- Gráficos de gastos
- Histórico de compras por período
- Itens mais comprados

#### 🤝 Compartilhamento Colaborativo
- Compartilhar lista com outras pessoas
- Edição colaborativa em tempo real
- Sincronização via Firebase

#### 📷 Scanner de Código de Barras
- Adicionar item escaneando código
- Buscar preços online

---

## 🗓️ Cronograma Sugerido (3 Semanas)

### **Semana 1 (Dias 1-5)**
- **Dia 1-2:** Busca e Filtros
- **Dia 3:** Ordenação
- **Dia 4-5:** Categorias (modelo + UI)

### **Semana 2 (Dias 6-10)**
- **Dia 6:** Modo Escuro/Claro
- **Dia 7-8:** Gestos (swipe)
- **Dia 9:** Melhorias no Dialog
- **Dia 10:** Buffer/testes

### **Semana 3 (Dias 11-15)**
- **Dia 11-13:** Backup e Restauração
- **Dia 14:** Histórico de Compras
- **Dia 15:** Testes finais e polimento

---

## 🎯 Critérios de Sucesso do MVP

### **Funcionalidades Obrigatórias**
- ✅ Busca e filtros funcionando
- ✅ Categorias implementadas
- ✅ Modo escuro/claro
- ✅ Backup/restauração básico
- ✅ Gestos básicos (swipe)

### **Qualidade**
- ✅ Sem crashes críticos
- ✅ Performance aceitável (< 100ms para operações)
- ✅ UI responsiva e intuitiva
- ✅ Acessibilidade básica

### **Documentação**
- ✅ README atualizado
- ✅ CHANGELOG atualizado
- ✅ Comentários no código crítico

---

## 🔧 Considerações Técnicas

### **Migrações do Room**
- Criar migração adequada para adicionar categoria
- Não usar `fallbackToDestructiveMigration()` em produção
- Testar migração com dados existentes

### **Performance**
- Usar índices no Room para busca
- LazyColumn com keys adequadas
- Debounce na busca (300ms)

### **Acessibilidade**
- Content descriptions em todos os ícones
- Labels adequados para campos
- Suporte a TalkBack

### **Testes**
- Testes unitários para ViewModel
- Testes de UI para componentes principais
- Testes de integração para fluxos críticos

---

## 📦 Entregáveis do MVP

1. **APK de Release** (v3.0)
2. **Código-fonte** no GitHub
3. **Release Notes** detalhadas
4. **Documentação** atualizada
5. **Testes** básicos implementados

---

## 🚀 Próximos Passos Após MVP

1. Coletar feedback de usuários
2. Priorizar features baseado em uso
3. Planejar v3.1 com melhorias incrementais
4. Considerar publicação na Play Store

---

**Nota:** Este documento é um guia flexível. Prioridades podem ser ajustadas conforme necessidade e complexidade encontrada durante o desenvolvimento.

