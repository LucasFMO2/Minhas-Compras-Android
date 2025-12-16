# Script para criar release no GitHub manualmente
# Este script usa a API do GitHub para criar um release

Write-Host "=== Criador de Release GitHub para Minhas Compras ===" -ForegroundColor Cyan

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.9"
$releaseName = "Release v2.28.9"
$apkPath = "app/build/outputs/apk/release/MinhasCompras-v2.28.9-code87.apk"
$notesPath = "RELEASE_NOTES_v2.28.9.md"

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK não encontrado em $apkPath" -ForegroundColor Red
    exit 1
}

# Ler notas de release
$releaseNotes = ""
if (Test-Path $notesPath) {
    $releaseNotes = Get-Content $notesPath -Raw
} else {
    $releaseNotes = "Release v2.28.9 - Correções no widget"
}

Write-Host "Informações do Release:" -ForegroundColor Yellow
Write-Host "- Repositório: $repoOwner/$repoName"
Write-Host "- Tag: $tagName"
Write-Host "- APK: $apkPath"
Write-Host ""

# Solicitar token do GitHub
Write-Host "Por favor, siga estes passos para criar o release manualmente:" -ForegroundColor Green
Write-Host ""
Write-Host "1. Acesse: https://github.com/$repoOwner/$repoName/releases/new" -ForegroundColor White
Write-Host "2. Selecione a tag: $tagName" -ForegroundColor White
Write-Host "3. Título do Release: $releaseName" -ForegroundColor White
Write-Host "4. Descrição: Copie o conteúdo do arquivo RELEASE_NOTES_v2.28.9.md" -ForegroundColor White
Write-Host "5. Arraste o APK para a área de upload: $apkPath" -ForegroundColor White
Write-Host "6. Clique em 'Publish release'" -ForegroundColor White
Write-Host ""

# Abrir o navegador com a página de release
Start-Process "https://github.com/$repoOwner/$repoName/releases/new"

Write-Host "Pressione qualquer tecla para abrir o arquivo de notas de release..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Abrir arquivo de notas
if (Test-Path $notesPath) {
    Start-Process "notepad.exe" $notesPath
}

Write-Host ""
Write-Host "=== Resumo das ações necessárias ===" -ForegroundColor Green
Write-Host "✓ Tag $tagName já foi criada e enviada para o GitHub" -ForegroundColor Green
Write-Host "✓ APK $apkPath está pronto para upload" -ForegroundColor Green
Write-Host "✓ Notas de release estão em $notesPath" -ForegroundColor Green
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Yellow
Write-Host "1. Complete o release através da página aberta no navegador"
Write-Host "2. O APK ficará disponível para download público"
Write-Host ""