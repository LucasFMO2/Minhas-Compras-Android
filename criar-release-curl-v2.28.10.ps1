# Script para criar release v2.28.10 usando curl
Write-Host "=== Criando Release v2.28.10 com curl ===" -ForegroundColor Cyan

# Configuracoes
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"
$releaseName = "Release v2.28.10 - Correcoes do Widget"
$apkPath = "app-release-v2.28.10.apk"
$notesPath = "RELEASE_NOTES_v2.28.10.md"

# Verificar APK
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "APK encontrado: $apkPath ($([math]::Round($apkSize, 2)) MB)" -ForegroundColor Green

# Ler notas
$releaseNotes = Get-Content $notesPath -Raw
Write-Host "Notas carregadas" -ForegroundColor Green

# Instrucoes
Write-Host ""
Write-Host "Para criar a release manualmente:" -ForegroundColor Yellow
Write-Host "1. Abra: https://github.com/$repoOwner/$repoName/releases/new" -ForegroundColor White
Write-Host "2. Preencha:" -ForegroundColor White
Write-Host "   - Tag: v2.28.10" -ForegroundColor Gray
Write-Host "   - Title: Release v2.28.10 - Correcoes do Widget" -ForegroundColor Gray
Write-Host "   - Body: Cole o conteudo de RELEASE_NOTES_v2.28.10.md" -ForegroundColor Gray
Write-Host "3. Anexe o APK: app-release-v2.28.10.apk" -ForegroundColor White
Write-Host "4. Publique a release" -ForegroundColor White
Write-Host ""

# Abrir pagina automaticamente
$url = "https://github.com/$repoOwner/$repoName/releases/new?tag=$tagName&title=Release%20$tagName"
Write-Host "Abrindo pagina..." -ForegroundColor Green
Start-Process $url

Write-Host ""
Write-Host "Pagina aberta no navegador!" -ForegroundColor Cyan
Write-Host "Use as instrucoes acima para completar a release." -ForegroundColor Yellow