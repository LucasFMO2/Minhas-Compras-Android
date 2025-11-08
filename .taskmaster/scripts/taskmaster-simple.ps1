# Taskmaster Helper Script - Versao Simplificada
param(
    [Parameter(Position=0)]
    [string]$Command = "help",
    [Parameter(Position=1)]
    [string]$TaskId = "",
    [Parameter()]
    [switch]$WithSubtasks = $false
)

$TasksFile = Join-Path $PSScriptRoot "..\tasks\tasks.json"

function Get-Tasks {
    return Get-Content $TasksFile -Raw | ConvertFrom-Json
}

function Show-Status {
    $tasks = Get-Tasks
    $meta = $tasks.metadata
    
    Write-Host ""
    Write-Host "Status do MVP - Minhas Compras v3.0" -ForegroundColor Cyan
    Write-Host "====================================" -ForegroundColor Cyan
    Write-Host "Total de Tarefas: $($meta.totalTasks)"
    Write-Host "Concluidas: $($meta.completedTasks)" -ForegroundColor Green
    Write-Host "Em Progresso: $($meta.inProgressTasks)" -ForegroundColor Yellow
    Write-Host "Pendentes: $($meta.pendingTasks)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Horas Estimadas: $($meta.totalEstimatedHours)h"
    Write-Host ""
}

function Show-Tasks {
    param([bool]$ShowSubtasks = $false)
    
    $tasks = Get-Tasks
    
    foreach ($sprint in $tasks.sprints) {
        Write-Host ""
        Write-Host "$($sprint.name)" -ForegroundColor Cyan
        Write-Host ("-" * 50) -ForegroundColor Gray
        
        foreach ($task in $sprint.tasks) {
            $status = $task.status
            $priority = $task.priority
            
            $statusText = switch ($status) {
                "pending" { "[PENDENTE]" }
                "in_progress" { "[EM PROGRESSO]" }
                "completed" { "[CONCLUIDA]" }
                default { "[$status]" }
            }
            
            $color = switch ($priority) {
                "high" { "Red" }
                "medium" { "Yellow" }
                "low" { "Blue" }
                default { "White" }
            }
            
            Write-Host "  $statusText [$($task.id)] $($task.title)" -ForegroundColor $color
            Write-Host "     Prioridade: $priority | Horas: $($task.estimatedHours)h | Status: $status" -ForegroundColor Gray
            
            if ($ShowSubtasks -and $task.checklist.Count -gt 0) {
                Write-Host "     Subtarefas:" -ForegroundColor Cyan
                foreach ($subtask in $task.checklist) {
                    Write-Host "       - $subtask" -ForegroundColor White
                }
            }
        }
    }
}

function Show-TaskDetail {
    param($TaskId)
    
    $tasks = Get-Tasks
    
    foreach ($sprint in $tasks.sprints) {
        foreach ($task in $sprint.tasks) {
            if ($task.id -eq $TaskId) {
                Write-Host ""
                Write-Host "Detalhes da Tarefa" -ForegroundColor Cyan
                Write-Host ("-" * 50) -ForegroundColor Gray
                Write-Host "ID: $($task.id)"
                Write-Host "Titulo: $($task.title)" -ForegroundColor Yellow
                Write-Host "Descricao: $($task.description)"
                Write-Host "Status: $($task.status)"
                Write-Host "Prioridade: $($task.priority)"
                Write-Host "Horas Estimadas: $($task.estimatedHours)h"
                
                if ($task.checklist.Count -gt 0) {
                    Write-Host ""
                    Write-Host "Checklist:" -ForegroundColor Cyan
                    foreach ($item in $task.checklist) {
                        Write-Host "  - $item"
                    }
                }
                return
            }
        }
    }
    
    Write-Host "Tarefa $TaskId nao encontrada" -ForegroundColor Red
}

switch ($Command.ToLower()) {
    "status" { Show-Status }
    "list" { Show-Tasks -ShowSubtasks $WithSubtasks }
    "show" { 
        if ($TaskId) {
            Show-TaskDetail -TaskId $TaskId
        } else {
            Write-Host "Especifique o ID da tarefa" -ForegroundColor Red
        }
    }
    default {
        Write-Host ""
        Write-Host "Taskmaster - Gerenciador de Tarefas" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Comandos disponiveis:" -ForegroundColor Yellow
        Write-Host "  status                    - Mostra status geral do projeto"
        Write-Host "  list [--with-subtasks]    - Lista todas as tarefas (opcao: com subtarefas)"
        Write-Host "  show [task-id]            - Mostra detalhes de uma tarefa"
        Write-Host ""
        Write-Host "Exemplos:" -ForegroundColor Yellow
        Write-Host "  .\taskmaster-simple.ps1 status"
        Write-Host "  .\taskmaster-simple.ps1 list"
        Write-Host "  .\taskmaster-simple.ps1 show task-1-1"
    }
}

