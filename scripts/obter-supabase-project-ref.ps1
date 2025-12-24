# Script para obter o SUPABASE_PROJECT_REF usando o token de acesso pessoal
# Token: sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9

$token = "sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "Tentando obter projetos do Supabase..." -ForegroundColor Yellow

try {
    # Tentar obter a lista de projetos
    $response = Invoke-RestMethod -Uri "https://api.supabase.com/v1/projects" -Method Get -Headers $headers
    
    if ($response) {
        Write-Host "`nProjetos encontrados:" -ForegroundColor Green
        Write-Host "===================" -ForegroundColor Green
        
        foreach ($project in $response) {
            Write-Host "`nNome: $($project.name)" -ForegroundColor Cyan
            Write-Host "ID: $($project.id)" -ForegroundColor Cyan
            Write-Host "Reference ID: $($project.ref)" -ForegroundColor Cyan
            Write-Host "URL: $($project.organization_id)" -ForegroundColor Cyan
            Write-Host "---" -ForegroundColor Gray
        }
        
        if ($response.Count -gt 0) {
            $firstProject = $response[0]
            Write-Host "`n✅ Project Reference encontrado: $($firstProject.ref)" -ForegroundColor Green
            Write-Host "`nUse este valor no arquivo mcp.json:" -ForegroundColor Yellow
            Write-Host "  SUPABASE_PROJECT_REF=$($firstProject.ref)" -ForegroundColor White
        }
    } else {
        Write-Host "Nenhum projeto encontrado." -ForegroundColor Red
    }
} catch {
    Write-Host "`n❌ Erro ao obter projetos:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host "`nTentando método alternativo..." -ForegroundColor Yellow
    
    # Método alternativo: tentar obter informações do usuário
    try {
        $userResponse = Invoke-RestMethod -Uri "https://api.supabase.com/v1/user" -Method Get -Headers $headers
        Write-Host "Usuário autenticado: $($userResponse.email)" -ForegroundColor Green
        Write-Host "`nPor favor, acesse https://app.supabase.com e obtenha o Project Reference manualmente." -ForegroundColor Yellow
    } catch {
        Write-Host "Não foi possível autenticar. Verifique o token." -ForegroundColor Red
    }
}

Write-Host "`nPara obter o Project Reference manualmente:" -ForegroundColor Yellow
Write-Host "1. Acesse https://app.supabase.com" -ForegroundColor White
Write-Host "2. Selecione seu projeto" -ForegroundColor White
Write-Host "3. Vá em Settings > General" -ForegroundColor White
Write-Host "4. Copie o 'Reference ID'" -ForegroundColor White

