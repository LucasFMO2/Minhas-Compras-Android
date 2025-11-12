# Script completo para configurar o MCP do Supabase no Cursor
# Este script tenta obter o project_ref automaticamente e atualiza a configuração

$token = "sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9"
$mcpConfigPath = "$env:USERPROFILE\.cursor\mcp.json"

Write-Host "=== Configuração do MCP Supabase ===" -ForegroundColor Cyan
Write-Host ""

# Tentar obter projetos
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

$projectRef = $null

try {
    Write-Host "1. Obtendo lista de projetos..." -ForegroundColor Yellow
    $projects = Invoke-RestMethod -Uri "https://api.supabase.com/v1/projects" -Method Get -Headers $headers
    
    if ($projects -and $projects.Count -gt 0) {
        $project = $projects[0]
        Write-Host "   ✅ Projeto encontrado: $($project.name)" -ForegroundColor Green
        
        # Tentar obter o reference ID
        if ($project.ref) {
            $projectRef = $project.ref
            Write-Host "   ✅ Reference ID: $projectRef" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️ Reference ID não encontrado na API" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "   ⚠️ Não foi possível obter projetos automaticamente" -ForegroundColor Yellow
    Write-Host "   Erro: $($_.Exception.Message)" -ForegroundColor Red
}

# Se não encontrou o reference, pedir ao usuário
if (-not $projectRef) {
    Write-Host ""
    Write-Host "2. Reference ID não encontrado automaticamente." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   Para obter o Reference ID manualmente:" -ForegroundColor White
    Write-Host "   1. Acesse https://app.supabase.com" -ForegroundColor Cyan
    Write-Host "   2. Selecione seu projeto" -ForegroundColor Cyan
    Write-Host "   3. Vá em Settings > API" -ForegroundColor Cyan
    Write-Host "   4. A URL do projeto será: https://[REFERENCE].supabase.co" -ForegroundColor Cyan
    Write-Host "   5. O [REFERENCE] é o que você precisa!" -ForegroundColor Cyan
    Write-Host ""
    $projectRef = Read-Host "   Digite o Reference ID (ou pressione Enter para pular)"
}

# Atualizar configuração do MCP
if ($projectRef -and $projectRef -ne "") {
    Write-Host ""
    Write-Host "3. Atualizando configuração do MCP..." -ForegroundColor Yellow
    
    try {
        # Ler configuração atual
        $config = Get-Content $mcpConfigPath -Raw | ConvertFrom-Json
        
        # Atualizar configuração do Supabase
        if ($config.mcpServers.supabase) {
            $config.mcpServers.supabase.url = "https://mcp.supabase.com/mcp?project_ref=$projectRef"
            $config.mcpServers.supabase.headers.Authorization = "Bearer $token"
            if ($config.mcpServers.supabase.env) {
                $config.mcpServers.supabase.env.SUPABASE_PROJECT_REF = $projectRef
                $config.mcpServers.supabase.env.SUPABASE_ACCESS_TOKEN = $token
            } else {
                $config.mcpServers.supabase | Add-Member -MemberType NoteProperty -Name "env" -Value @{
                    SUPABASE_PROJECT_REF = $projectRef
                    SUPABASE_ACCESS_TOKEN = $token
                }
            }
            
            # Salvar configuração
            $config | ConvertTo-Json -Depth 10 | Set-Content $mcpConfigPath
            
            Write-Host "   ✅ Configuração atualizada com sucesso!" -ForegroundColor Green
            Write-Host ""
            Write-Host "4. Próximos passos:" -ForegroundColor Yellow
            Write-Host "   - Reinicie o Cursor para carregar a nova configuração" -ForegroundColor White
            Write-Host "   - O servidor MCP do Supabase estará disponível após o reinício" -ForegroundColor White
        } else {
            Write-Host "   ⚠️ Configuração do Supabase não encontrada no arquivo" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   ❌ Erro ao atualizar configuração: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host ""
    Write-Host "⚠️ Reference ID não fornecido. A configuração não foi atualizada." -ForegroundColor Yellow
    Write-Host "   Você pode atualizar manualmente o arquivo: $mcpConfigPath" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Configuração concluída ===" -ForegroundColor Cyan

