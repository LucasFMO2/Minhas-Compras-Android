# 📋 Taskmaster - Sistema de Gerenciamento de Tarefas

Sistema de gerenciamento de tarefas integrado ao projeto Minhas Compras.

## 📁 Estrutura

```
.taskmaster/
├── config.json      # Configurações do Taskmaster
├── tasks.json       # Todas as tarefas do projeto
└── README.md        # Este arquivo
```

## 🎯 Uso

### Visualizar Tarefas

As tarefas estão organizadas em sprints (semanas) no arquivo `tasks.json`.

### Status das Tarefas

- ⏳ **Pendente** - Ainda não iniciada
- 🔄 **Em Progresso** - Sendo trabalhada
- 👀 **Em Revisão** - Aguardando revisão
- ✅ **Concluída** - Finalizada
- 🚫 **Bloqueada** - Bloqueada por dependência

### Prioridades

- 🔴 **Alta** - Crítica para o MVP
- 🟡 **Média** - Importante mas não crítica
- 🔵 **Baixa** - Desejável mas pode ser adiada

## 📊 Progresso do MVP

**Total de Tarefas:** 9  
**Tarefas Concluídas:** 0  
**Tarefas em Progresso:** 0  
**Tarefas Pendentes:** 9

### Por Sprint

- **Sprint 1 (Semana 1):** 0/3 tarefas
- **Sprint 2 (Semana 2):** 0/3 tarefas
- **Sprint 3 (Semana 3):** 0/3 tarefas

## 🔧 Manutenção

Para atualizar o progresso:

1. Edite o arquivo `tasks.json`
2. Atualize o status da tarefa
3. Marque itens do checklist como concluídos
4. Atualize os metadados no final do arquivo

## 📝 Formato das Tarefas

Cada tarefa contém:

- `id`: Identificador único
- `title`: Título da tarefa
- `description`: Descrição detalhada
- `priority`: Prioridade (high/medium/low)
- `status`: Status atual
- `estimatedHours`: Estimativa de horas
- `tags`: Tags para categorização
- `dependencies`: IDs de tarefas dependentes
- `checklist`: Lista de sub-tarefas

## 🚀 Próximos Passos

1. Revisar tarefas da Sprint 1
2. Iniciar primeira tarefa (Busca e Filtros)
3. Atualizar status conforme progresso
4. Manter checklist atualizado

---

**Última Atualização:** 07/11/2024

