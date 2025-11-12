# Script para obter detalhes completos do projeto Supabase
$token = "sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9"
$projectId = "wkpmrmmhkhjbjfcuwakk"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Obtendo detalhes do projeto..." -ForegroundColor Yellow

try {
    # Obter detalhes do projeto específico
    $project = Invoke-RestMethod -Uri "https://api.supabase.com/v1/projects/$projectId" -Method Get -Headers $headers
    
    Write-Host "`nDetalhes do Projeto:" -ForegroundColor Green
    Write-Host "===================" -ForegroundColor Green
    Write-Host "Nome: $($project.name)" -ForegroundColor Cyan
    Write-Host "ID: $($project.id)" -ForegroundColor Cyan
    Write-Host "Reference: $($project.ref)" -ForegroundColor Cyan
    Write-Host "Region: $($project.region)" -ForegroundColor Cyan
    
    if ($project.ref) {
        Write-Host "`n✅ Project Reference: $($project.ref)" -ForegroundColor Green
        return $project.ref
    } else {
        # Tentar extrair da URL do projeto
        if ($project.api_keys) {
            Write-Host "`nTentando obter informações adicionais..." -ForegroundColor Yellow
        }
        
        # O reference pode estar no formato da URL
        # Geralmente é algo como: https://[ref].supabase.co
        Write-Host "`n⚠️ Reference ID não encontrado diretamente." -ForegroundColor Yellow
        Write-Host "O Reference ID geralmente está na URL do projeto." -ForegroundColor Yellow
        Write-Host "Exemplo: Se a URL é https://abc123.supabase.co, o ref é 'abc123'" -ForegroundColor Yellow
    }
} catch {
    Write-Host "`n❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host $_.Exception.Response -ForegroundColor Red
}

