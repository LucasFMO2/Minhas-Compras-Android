# Script simplificado para criar release
$ErrorActionPreference = "Continue"

$tag = "v2.10.8"
$apkPath = "app-release-v2.10.8.apk"
$notesFile = "release-notes-temp.txt"

Write-Host "🚀 Criando release $tag no GitHub..." -ForegroundColor Cyan

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK não encontrado: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ APK encontrado: $apkPath" -ForegroundColor Green

# Criar release
$env:GIT_PAGER = ""
$env:PAGER = ""

gh release create $tag --title "Release v2.10.8" --notes-file $notesFile $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Release criada com sucesso!" -ForegroundColor Green
    Write-Host "🔗 URL: https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/tag/$tag" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erro ao criar release" -ForegroundColor Red
    exit 1
}

