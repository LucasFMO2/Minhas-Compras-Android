# Script para verificar se o MCP Supabase está instalado e configurado

$mcpConfigPath = "$env:USERPROFILE\.cursor\mcp.json"

Write-Host "=== Verificação da Instalação do MCP Supabase ===" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $mcpConfigPath)) {
    Write-Host "❌ Arquivo de configuração MCP não encontrado em:" -ForegroundColor Red
    Write-Host "   $mcpConfigPath" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Arquivo de configuração encontrado" -ForegroundColor Green
Write-Host ""

try {
    $config = Get-Content $mcpConfigPath -Raw | ConvertFrom-Json
    
    if (-not $config.mcpServers) {
        Write-Host "❌ Seção mcpServers não encontrada" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Servidores MCP configurados:" -ForegroundColor Yellow
    foreach ($serverName in $config.mcpServers.PSObject.Properties.Name) {
        Write-Host "  - $serverName" -ForegroundColor Cyan
    }
    Write-Host ""
    
    if ($config.mcpServers.supabase) {
        $supabase = $config.mcpServers.supabase
        Write-Host "✅ Servidor Supabase encontrado na configuração" -ForegroundColor Green
        Write-Host ""
        Write-Host "Detalhes da configuração:" -ForegroundColor Yellow
        Write-Host "  Tipo: $($supabase.type)" -ForegroundColor White
        Write-Host "  URL: $($supabase.url)" -ForegroundColor White
        
        if ($supabase.headers.Authorization) {
            $tokenPreview = $supabase.headers.Authorization.Substring(0, [Math]::Min(20, $supabase.headers.Authorization.Length)) + "..."
            Write-Host "  Token: $tokenPreview" -ForegroundColor White
        }
        
        Write-Host ""
        
        # Verificar se o project_ref está configurado
        if ($supabase.url -match '\$\{SUPABASE_PROJECT_REF\}') {
            Write-Host "⚠️  ATENÇÃO: SUPABASE_PROJECT_REF não está configurado!" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Para completar a instalação:" -ForegroundColor Yellow
            Write-Host "1. Acesse https://app.supabase.com" -ForegroundColor White
            Write-Host "2. Selecione seu projeto" -ForegroundColor White
            Write-Host "3. Vá em Settings > API" -ForegroundColor White
            Write-Host "4. A URL do projeto será: https://[REFERENCE].supabase.co" -ForegroundColor White
            Write-Host "5. O [REFERENCE] é o SUPABASE_PROJECT_REF que você precisa" -ForegroundColor White
            Write-Host ""
            Write-Host "Depois, execute:" -ForegroundColor Yellow
            Write-Host "  powershell -ExecutionPolicy Bypass -File configurar-supabase-mcp.ps1" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "Status: ⚠️  Instalado mas NÃO configurado completamente" -ForegroundColor Yellow
        } else {
            Write-Host "✅ SUPABASE_PROJECT_REF está configurado" -ForegroundColor Green
            Write-Host ""
            Write-Host "Status: ✅ Instalado e configurado!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Próximos passos:" -ForegroundColor Yellow
            Write-Host "1. Reinicie o Cursor para carregar a configuração" -ForegroundColor White
            Write-Host "2. O servidor MCP Supabase estará disponível após o reinício" -ForegroundColor White
        }
    } else {
        Write-Host "❌ Servidor Supabase NÃO encontrado na configuração" -ForegroundColor Red
        Write-Host "Status: ❌ NÃO instalado" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Erro ao ler configuração: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Status: ❌ Erro na configuração" -ForegroundColor Red
}

Write-Host ""

