# Taskmaster Helper Script
# Utilitário para gerenciar tarefas do projeto

param(
    [Parameter(Position=0)]
    [string]$Command = "status",
    
    [Parameter(Position=1)]
    [string]$TaskId = "",
    
    [Parameter()]
    [string]$Status = "",
    
    [Parameter()]
    [string]$Sprint = ""
)

$TasksFile = Join-Path $PSScriptRoot "..\tasks.json"
$ConfigFile = Join-Path $PSScriptRoot "..\config.json"

function Get-Tasks {
    return Get-Content $TasksFile | ConvertFrom-Json
}

function Save-Tasks {
    param($Tasks)
    $Tasks | ConvertTo-Json -Depth 10 | Set-Content $TasksFile
}

function Show-Status {
    $tasks = Get-Tasks
    $metadata = $tasks.metadata
    
    Write-Host "`n📊 Status do MVP - Minhas Compras v3.0" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Total de Tarefas: $($metadata.totalTasks)" -ForegroundColor White
    Write-Host "Concluídas: $($metadata.completedTasks)" -ForegroundColor Green
    Write-Host "Em Progresso: $($metadata.inProgressTasks)" -ForegroundColor Yellow
    Write-Host "Pendentes: $($metadata.pendingTasks)" -ForegroundColor Gray
    Write-Host "`nHoras Estimadas: $($metadata.totalEstimatedHours)h" -ForegroundColor White
    
    Write-Host "`n📅 Por Sprint:" -ForegroundColor Cyan
    foreach ($sprint in $tasks.sprints) {
        $completed = ($sprint.tasks | Where-Object { $_.status -eq "completed" }).Count
        $total = $sprint.tasks.Count
        Write-Host "  $($sprint.name): $completed/$total tarefas" -ForegroundColor White
    }
}

function Show-Tasks {
    param($SprintFilter = "")
    
    $tasks = Get-Tasks
    
    foreach ($sprint in $tasks.sprints) {
        if ($SprintFilter -and $sprint.id -ne $SprintFilter) {
            continue
        }
        
        Write-Host "`n📋 $($sprint.name)" -ForegroundColor Cyan
        Write-Host ("-" * 50) -ForegroundColor Gray
        
        foreach ($task in $sprint.tasks) {
            $statusIcon = switch ($task.status) {
                "pending" { "⏳" }
                "in_progress" { "🔄" }
                "review" { "👀" }
                "completed" { "✅" }
                "blocked" { "🚫" }
                default { "❓" }
            }
            
            $priorityColor = switch ($task.priority) {
                "high" { "Red" }
                "medium" { "Yellow" }
                "low" { "Blue" }
                default { "White" }
            }
            
            Write-Host "  $statusIcon [$($task.id)] $($task.title)" -ForegroundColor $priorityColor
            Write-Host "     Prioridade: $($task.priority) | Horas: $($task.estimatedHours)h" -ForegroundColor Gray
        }
    }
}

function Update-TaskStatus {
    param($TaskId, $NewStatus)
    
    $tasks = Get-Tasks
    $found = $false
    
    foreach ($sprint in $tasks.sprints) {
        foreach ($task in $sprint.tasks) {
            if ($task.id -eq $TaskId) {
                $task.status = $NewStatus
                $found = $true
                Write-Host "✅ Tarefa $TaskId atualizada para: $NewStatus" -ForegroundColor Green
                break
            }
        }
        if ($found) { break }
    }
    
    if (-not $found) {
        Write-Host "❌ Tarefa $TaskId não encontrada" -ForegroundColor Red
        return
    }
    
    # Atualizar metadados
    $completed = 0
    $inProgress = 0
    $pending = 0
    
    foreach ($sprint in $tasks.sprints) {
        foreach ($task in $sprint.tasks) {
            switch ($task.status) {
                "completed" { $completed++ }
                "in_progress" { $inProgress++ }
                default { $pending++ }
            }
        }
    }
    
    $tasks.metadata.completedTasks = $completed
    $tasks.metadata.inProgressTasks = $inProgress
    $tasks.metadata.pendingTasks = $pending
    
    Save-Tasks $tasks
}

function Show-TaskDetail {
    param($TaskId)
    
    $tasks = Get-Tasks
    
    foreach ($sprint in $tasks.sprints) {
        foreach ($task in $sprint.tasks) {
            if ($task.id -eq $TaskId) {
                Write-Host "`n📋 Detalhes da Tarefa" -ForegroundColor Cyan
                Write-Host ("-" * 50) -ForegroundColor Gray
                Write-Host "ID: $($task.id)" -ForegroundColor White
                Write-Host "Título: $($task.title)" -ForegroundColor Yellow
                Write-Host "Descrição: $($task.description)" -ForegroundColor White
                Write-Host "Status: $($task.status)" -ForegroundColor $(if ($task.status -eq "completed") { "Green" } else { "Yellow" })
                Write-Host "Prioridade: $($task.priority)" -ForegroundColor $(switch ($task.priority) { "high" { "Red" } "medium" { "Yellow" } "low" { "Blue" } })
                Write-Host "Horas Estimadas: $($task.estimatedHours)h" -ForegroundColor White
                $tagsStr = $task.tags -join ", "
                Write-Host "Tags: $tagsStr" -ForegroundColor Gray
                
                if ($task.checklist.Count -gt 0) {
                    Write-Host "`n📝 Checklist:" -ForegroundColor Cyan
                    foreach ($item in $task.checklist) {
                        Write-Host "  - $item" -ForegroundColor White
                    }
                }
                
                return
            }
        }
    }
    
    Write-Host "❌ Tarefa $TaskId não encontrada" -ForegroundColor Red
}

# Main command handler
switch ($Command.ToLower()) {
    "status" {
        Show-Status
    }
    "list" {
        Show-Tasks -SprintFilter $Sprint
    }
    "show" {
        if ($TaskId) {
            Show-TaskDetail -TaskId $TaskId
        } else {
            Write-Host "❌ Especifique o ID da tarefa" -ForegroundColor Red
        }
    }
    "start" {
        if ($TaskId) {
            Update-TaskStatus -TaskId $TaskId -NewStatus "in_progress"
        } else {
            Write-Host "❌ Especifique o ID da tarefa" -ForegroundColor Red
        }
    }
    "complete" {
        if ($TaskId) {
            Update-TaskStatus -TaskId $TaskId -NewStatus "completed"
        } else {
            Write-Host "❌ Especifique o ID da tarefa" -ForegroundColor Red
        }
    }
    "update" {
        if ($TaskId -and $Status) {
            Update-TaskStatus -TaskId $TaskId -NewStatus $Status
        } else {
            Write-Host "❌ Use: taskmaster update <taskId> <status>" -ForegroundColor Red
        }
    }
    default {
        Write-Host "`n📋 Taskmaster - Gerenciador de Tarefas" -ForegroundColor Cyan
        Write-Host "`nComandos disponíveis:" -ForegroundColor Yellow
        Write-Host "  status              - Mostra status geral do projeto"
        Write-Host "  list [sprint-id]     - Lista todas as tarefas"
        Write-Host "  show [task-id]       - Mostra detalhes de uma tarefa"
        Write-Host "  start [task-id]      - Marca tarefa como em progresso"
        Write-Host "  complete [task-id]   - Marca tarefa como concluida"
        Write-Host "  update [task-id] [status] - Atualiza status da tarefa"
        Write-Host "`nExemplos:" -ForegroundColor Yellow
        Write-Host "  .\taskmaster.ps1 status"
        Write-Host "  .\taskmaster.ps1 list"
        Write-Host "  .\taskmaster.ps1 show task-1-1"
        Write-Host "  .\taskmaster.ps1 start task-1-1"
        Write-Host "  .\taskmaster.ps1 complete task-1-1"
    }
}

