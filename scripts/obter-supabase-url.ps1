# Script para obter a URL do projeto Supabase e extrair o reference ID
$token = "sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9"
$projectId = "wkpmrmmhkhjbjfcuwakk"

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Obtendo informações do projeto..." -ForegroundColor Yellow

try {
    # Tentar obter informações do projeto
    $project = Invoke-RestMethod -Uri "https://api.supabase.com/v1/projects/$projectId" -Method Get -Headers $headers
    
    Write-Host "`nInformações do Projeto:" -ForegroundColor Green
    Write-Host "===================" -ForegroundColor Green
    
    # Exibir todas as propriedades disponíveis
    $project | ConvertTo-Json -Depth 10 | Write-Host
    
    # Tentar encontrar a URL ou reference
    if ($project.ref) {
        Write-Host "`n✅ Reference ID encontrado: $($project.ref)" -ForegroundColor Green
        return $project.ref
    }
    
    # Tentar extrair de outras propriedades
    if ($project.connection_pooling) {
        Write-Host "`nConnection Pooling: $($project.connection_pooling)" -ForegroundColor Cyan
    }
    
    # O reference geralmente está na URL do projeto
    # Formato: https://[reference].supabase.co
    Write-Host "`n⚠️ Para obter o Reference ID:" -ForegroundColor Yellow
    Write-Host "1. Acesse https://app.supabase.com" -ForegroundColor White
    Write-Host "2. Selecione o projeto: $($project.name)" -ForegroundColor White
    Write-Host "3. Vá em Settings > API" -ForegroundColor White
    Write-Host "4. A URL do projeto será algo como: https://[REFERENCE].supabase.co" -ForegroundColor White
    Write-Host "5. O [REFERENCE] é o que você precisa!" -ForegroundColor White
    
} catch {
    Write-Host "`n❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Resposta: $responseBody" -ForegroundColor Red
    }
}

