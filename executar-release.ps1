# Script para executar release com token
$token = $env:GITHUB_TOKEN
if (-not $token) {
    Write-Host "ERRO: Variável de ambiente GITHUB_TOKEN não encontrada!" -ForegroundColor Red
    Write-Host "Execute: `$env:GITHUB_TOKEN = 'seu_token_aqui'" -ForegroundColor Yellow
    exit 1
}

# Executar o script principal
& .\criar-release-simples-api.ps1