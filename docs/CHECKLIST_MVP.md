# ✅ Checklist do MVP - Minhas Compras v3.0

**Prazo:** 3 semanas | **Status:** 🟡 Em Planejamento

---

## 📅 Semana 1: Funcionalidades Essenciais

### 🔍 Busca e Filtros (2-3 dias)
- [ ] Adicionar campo de busca na tela principal
- [ ] Implementar busca em tempo real
- [ ] Criar filtros: Todos / Pendentes / Comprados
- [ ] Adicionar indicador visual do filtro ativo
- [ ] Implementar botão de limpar busca
- [ ] Testar performance com muitos itens
- [ ] Adicionar debounce na busca (300ms)

### 📊 Ordenação (1-2 dias)
- [ ] Criar menu de ordenação
- [ ] Implementar ordenação por nome (A-Z)
- [ ] Implementar ordenação por data (mais recente)
- [ ] Implementar ordenação por preço (menor-maior)
- [ ] Adicionar indicador visual da ordenação
- [ ] Persistir preferência de ordenação
- [ ] Testar ordenação com filtros

### 🏷️ Categorias (3-4 dias)
- [ ] Adicionar campo `categoria` ao modelo `ItemCompra`
- [ ] Criar migração do Room (versão 3)
- [ ] Criar lista de categorias pré-definidas
- [ ] Adicionar seleção de categoria no dialog
- [ ] Exibir categoria no card do item (badge/chip)
- [ ] Atualizar ViewModel para categorias
- [ ] Migrar dados existentes (categoria padrão)
- [ ] Testar migração sem perda de dados
- [ ] (Opcional) Filtro por categoria

---

## 📅 Semana 2: Melhorias de UX

### 🌙 Modo Escuro/Claro (1-2 dias)
- [ ] Configurar tema claro e escuro
- [ ] Adicionar toggle de tema na UI
- [ ] Persistir preferência do tema
- [ ] Testar todos os componentes com ambos os temas
- [ ] Ajustar cores para contraste adequado
- [ ] (Opcional) Seguir preferência do sistema

### 👆 Gestos e Ações Rápidas (2-3 dias)
- [ ] Implementar swipe para direita (marcar comprado)
- [ ] Implementar swipe para esquerda (deletar)
- [ ] Adicionar feedback visual durante swipe
- [ ] Implementar Snackbar com "Desfazer"
- [ ] Testar gestos em diferentes dispositivos
- [ ] Ajustar sensibilidade dos gestos
- [ ] (Opcional) Long press para menu

### 💬 Melhorias no Dialog (1 dia)
- [ ] Implementar autocompletar de itens
- [ ] Adicionar sugestões de itens frequentes
- [ ] Melhorar validação de campos
- [ ] Adicionar feedback visual de erros
- [ ] Testar usabilidade

---

## 📅 Semana 3: Backup e Polimento

### 💾 Backup e Restauração (3-4 dias)
- [ ] Criar tela/menu de configurações
- [ ] Implementar exportação para JSON
- [ ] Implementar importação de JSON
- [ ] Adicionar validação de arquivo JSON
- [ ] Implementar compartilhamento de lista (texto)
- [ ] Adicionar dialog de confirmação para importação
- [ ] Testar backup/restauração
- [ ] Tratar erros de importação

### 📜 Histórico de Compras (2-3 dias)
- [ ] Criar modelo `ListaHistorico`
- [ ] Adicionar DAO para histórico
- [ ] Implementar salvamento automático quando lista completa
- [ ] Criar tela de histórico
- [ ] Implementar visualização de lista histórica
- [ ] Implementar reutilização de lista histórica
- [ ] Adicionar opção de deletar histórico
- [ ] Testar fluxo completo

### 🧪 Testes e Correções (2-3 dias)
- [ ] Escrever testes unitários (ViewModel)
- [ ] Escrever testes de UI (Compose Testing)
- [ ] Corrigir bugs encontrados
- [ ] Otimizar performance
- [ ] Revisar acessibilidade
- [ ] Revisar código
- [ ] Atualizar documentação

---

## 🎯 Tarefas Finais

### 📦 Preparação para Release
- [ ] Atualizar versionCode e versionName
- [ ] Atualizar README.md
- [ ] Atualizar CHANGELOG.md
- [ ] Gerar APK de release
- [ ] Testar APK em dispositivo físico
- [ ] Criar release no GitHub
- [ ] Atualizar documentação

---

## 📊 Progresso Geral

**Semana 1:** ⬜ 0/3 tarefas principais  
**Semana 2:** ⬜ 0/3 tarefas principais  
**Semana 3:** ⬜ 0/3 tarefas principais  

**Total:** ⬜ 0/9 tarefas principais concluídas

---

## 🔴 Bloqueadores e Riscos

### Riscos Identificados
- ⚠️ Migração do Room pode ser complexa
- ⚠️ Gestos podem não funcionar bem em todos os dispositivos
- ⚠️ Backup/restauração pode ter problemas de formato

### Mitigações
- ✅ Testar migração em ambiente isolado primeiro
- ✅ Usar bibliotecas testadas para gestos
- ✅ Validar JSON com schema antes de importar

---

## 📝 Notas

- Marque as tarefas conforme forem concluídas
- Adicione notas sobre problemas encontrados
- Ajuste o cronograma conforme necessário
- Priorize funcionalidades críticas primeiro

---

**Última atualização:** [Data]  
**Responsável:** [Nome]

