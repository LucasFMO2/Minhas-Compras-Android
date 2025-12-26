# Script para criar release v2.19.13 no GitHub
Write-Host "=== Criador de Release v2.19.13 ===" -ForegroundColor Cyan

# Verificar se o APK existe
$apkPath = "app-release-v2.19.13.apk"
if (-Not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado em $apkPath" -ForegroundColor Red
    exit 1
}

# Verificar se as release notes existem
$notesPath = "RELEASE_NOTES_v2.19.13.md"
if (-Not (Test-Path $notesPath)) {
    Write-Host "ERRO: Release notes nao encontradas em $notesPath" -ForegroundColor Red
    exit 1
}

Write-Host "OK: APK encontrado: $apkPath" -ForegroundColor Green
Write-Host "OK: Release notes encontradas: $notesPath" -ForegroundColor Green

# Abrir diretamente a pagina de release no navegador
$url = "https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android/releases/new"
Write-Host ""
Write-Host "Abrindo pagina de release no navegador..." -ForegroundColor Cyan
Start-Process $url

Write-Host ""
Write-Host "=== INFORMACOES PARA PREENCHER NA PAGINA DO GITHUB ===" -ForegroundColor Yellow
Write-Host ""
Write-Host "Tag: v2.19.13" -ForegroundColor White
Write-Host "Title: Release v2.19.13 - Interface Simplificada" -ForegroundColor White  
Write-Host "APK: app-release-v2.19.13.apk" -ForegroundColor White
Write-Host "Notes: Copie o conteudo do arquivo RELEASE_NOTES_v2.19.13.md" -ForegroundColor White
Write-Host "Marcar como: Release Publica OK" -ForegroundColor White
Write-Host ""

# Abrir arquivo de release notes para facilitar a copia
Write-Host "Abrindo arquivo de release notes para copiar o conteudo..." -ForegroundColor Cyan
Start-Process "notepad.exe" $notesPath

Write-Host ""
Write-Host "=== PROXIMOS PASSOS ===" -ForegroundColor Green
Write-Host "1. Preencha as informacoes na pagina do GitHub" -ForegroundColor White
Write-Host "2. Marque como 'Release Publica'" -ForegroundColor White  
Write-Host "3. Clique em 'Publish release'" -ForegroundColor White
Write-Host ""
Write-Host "A release v2.19.13 estara disponivel para download publico!" -ForegroundColor Green