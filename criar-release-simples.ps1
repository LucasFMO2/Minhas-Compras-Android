# Script simples para criar release no GitHub
Write-Host "=== Criador de Release GitHub ===" -ForegroundColor Cyan

# Abrir diretamente a página de release no navegador
$url = "https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android/releases/new"
Start-Process $url

Write-Host ""
Write-Host "Pagina de release aberta no navegador!" -ForegroundColor Green
Write-Host ""
Write-Host "Informacoes para preencher:" -ForegroundColor Yellow
Write-Host "- Tag: v2.28.9" -ForegroundColor White
Write-Host "- Title: Release v2.28.9" -ForegroundColor White
Write-Host "- APK: app/build/outputs/apk/release/MinhasCompras-v2.28.9-code87.apk" -ForegroundColor White
Write-Host "- Notes: Use o arquivo RELEASE_NOTES_v2.28.9.md" -ForegroundColor White
Write-Host ""

# Abrir arquivo de notas
if (Test-Path "RELEASE_NOTES_v2.28.9.md") {
    Write-Host "Abrindo arquivo de notas..." -ForegroundColor Cyan
    Start-Process "notepad.exe" "RELEASE_NOTES_v2.28.9.md"
}

Write-Host "Complete o release no navegador e o APK estara disponivel!" -ForegroundColor Green
