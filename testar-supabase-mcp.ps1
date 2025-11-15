# Script para testar a conexão com o MCP Supabase
Write-Host "🔍 Testando conexão com MCP Supabase..." -ForegroundColor Cyan
Write-Host ""

$projectRef = "wkpmrmmhkhjbjfcuwakk"
$accessToken = "sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9"
$mcpUrl = "https://mcp.supabase.com/mcp?project_ref=$projectRef"

Write-Host "📋 Configuração:" -ForegroundColor Yellow
Write-Host "  Project Ref: $projectRef" -ForegroundColor Gray
Write-Host "  MCP URL: $mcpUrl" -ForegroundColor Gray
Write-Host "  Token: $($accessToken.Substring(0, 20))..." -ForegroundColor Gray
Write-Host ""

# Testar conexão HTTP básica
Write-Host "🌐 Testando conexão HTTP..." -ForegroundColor Cyan
try {
    $headers = @{
        "Authorization" = "Bearer $accessToken"
        "Content-Type" = "application/json"
    }
    
    # Tentar fazer uma requisição de teste
    $body = @{
        jsonrpc = "2.0"
        id = 1
        method = "initialize"
        params = @{
            protocolVersion = "2024-11-05"
            capabilities = @{}
            clientInfo = @{
                name = "test-client"
                version = "1.0.0"
            }
        }
    } | ConvertTo-Json -Depth 10
    
    $response = Invoke-RestMethod -Uri $mcpUrl -Method Post -Headers $headers -Body $body -ErrorAction Stop
    
    Write-Host "✅ Conexão HTTP bem-sucedida!" -ForegroundColor Green
    Write-Host "  Resposta recebida do servidor MCP" -ForegroundColor Gray
    
    if ($response.result) {
        Write-Host "  ✅ Servidor MCP respondeu corretamente" -ForegroundColor Green
        if ($response.result.capabilities) {
            Write-Host "  📦 Recursos disponíveis:" -ForegroundColor Yellow
            $response.result.capabilities | ConvertTo-Json -Depth 5 | Write-Host -ForegroundColor Gray
        }
    }
    
} catch {
    Write-Host "❌ Erro na conexão:" -ForegroundColor Red
    Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "  Status Code: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host "  ⚠️  Token de acesso inválido ou expirado" -ForegroundColor Yellow
        } elseif ($statusCode -eq 404) {
            Write-Host "  ⚠️  URL do MCP não encontrada. Verifique o project_ref" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "📝 Verificando arquivo mcp.json..." -ForegroundColor Cyan
$mcpJsonPath = "$env:USERPROFILE\.cursor\mcp.json"
if (Test-Path $mcpJsonPath) {
    $config = Get-Content $mcpJsonPath -Raw | ConvertFrom-Json
    if ($config.mcpServers.supabase) {
        Write-Host "✅ Configuração encontrada no mcp.json" -ForegroundColor Green
        Write-Host "  URL: $($config.mcpServers.supabase.url)" -ForegroundColor Gray
        Write-Host "  Project Ref: $($config.mcpServers.supabase.env.SUPABASE_PROJECT_REF)" -ForegroundColor Gray
    } else {
        Write-Host "❌ Configuração do Supabase não encontrada no mcp.json" -ForegroundColor Red
    }
} else {
    Write-Host "❌ Arquivo mcp.json não encontrado em $mcpJsonPath" -ForegroundColor Red
}

Write-Host ""
Write-Host "💡 Nota: Para usar o MCP Supabase no Cursor, você precisa:" -ForegroundColor Yellow
Write-Host "  1. Reiniciar o Cursor após configurar o mcp.json" -ForegroundColor Gray
Write-Host "  2. Verificar se o servidor MCP aparece na lista de servidores conectados" -ForegroundColor Gray
Write-Host "  3. As ferramentas do Supabase estarão disponíveis quando o servidor estiver ativo" -ForegroundColor Gray
































