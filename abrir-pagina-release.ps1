# Script para abrir a página de criação de release no GitHub
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"

Write-Host "Abrindo página de criação de release..." -ForegroundColor Cyan
Write-Host "Repositório: $repoOwner/$repoName" -ForegroundColor White
Write-Host "Tag: $tagName" -ForegroundColor White
Write-Host ""

$url = "https://github.com/$repoOwner/$repoName/releases/new?tag=$tagName&title=Release%20$tagName"

Write-Host "URL: $url" -ForegroundColor Yellow
Write-Host ""
Write-Host "Abrindo no navegador padrão..." -ForegroundColor Green

# Abrir no navegador padrão
Start-Process $url

Write-Host ""
Write-Host "✅ Página aberta!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Use o arquivo 'instrucoes-release-manual-v2.28.10.md' para seguir os passos" -ForegroundColor Cyan
Write-Host "📦 O APK está pronto: app-release-v2.28.10.apk (13.13 MB)" -ForegroundColor Cyan