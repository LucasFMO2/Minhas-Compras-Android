# Script para criar release no GitHub com APK anexado
# Requer: GitHub Personal Access Token com permissão de repo

param(
    [string]$GitHubToken = $env:GITHUB_TOKEN,
    [string]$Tag = "v2.10.1",
    [string]$ReleaseName = "Release v2.10.1: Sistema de atualização em tempo real",
    [string]$ApkFile = "app-release-v2.10.1.apk",
    [string]$Repo = "nerddescoladofmo-cmyk/Minhas-Compras-Android"
)

if (-not $GitHubToken) {
    Write-Host "Erro: Token do GitHub não encontrado!" -ForegroundColor Red
    Write-Host "Configure a variável de ambiente GITHUB_TOKEN ou passe como parâmetro:" -ForegroundColor Yellow
    Write-Host "  `$env:GITHUB_TOKEN = 'seu_token_aqui'" -ForegroundColor Yellow
    Write-Host "  .\criar-release-github.ps1" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $ApkFile)) {
    Write-Host "Erro: Arquivo APK não encontrado: $ApkFile" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = "token $GitHubToken"
    "Accept" = "application/vnd.github.v3+json"
}

# Criar release
$releaseBody = @{
    tag_name = $Tag
    name = $ReleaseName
    body = @"
## 🚀 Sistema de Atualização em Tempo Real

### ✨ Novidades da v2.10.1:

- 🚀 **Sistema de atualização em tempo real** - Busca todas as releases do GitHub e encontra a versão mais recente automaticamente
- ⚡ **Verificação em tempo real** - Cache busting para garantir verificação sempre atualizada
- 🔧 **Cálculo automático de versionCode** - Suporte automático para versões futuras sem atualização manual
- 📊 **Melhorias de performance** - Sistema de atualização mais eficiente e confiável

### 📦 Mudanças Técnicas:

- Modificado UpdateManager para buscar todas as releases ao invés de apenas /latest
- Implementada lógica para encontrar a release mais recente comparando versionCodes
- Criada lógica automática para calcular versionCode a partir do versionName
- Adicionado cache busting para garantir verificação em tempo real
"@
    draft = $false
    prerelease = $false
} | ConvertTo-Json

Write-Host "Criando release $Tag..." -ForegroundColor Cyan
try {
    $releaseResponse = Invoke-RestMethod -Uri "https://api.github.com/repos/$Repo/releases" -Method Post -Headers $headers -Body $releaseBody -ContentType "application/json"
    $releaseId = $releaseResponse.id
    Write-Host "Release criada com sucesso! ID: $releaseId" -ForegroundColor Green
} catch {
    Write-Host "Erro ao criar release: $_" -ForegroundColor Red
    exit 1
}

# Upload do APK
Write-Host "Fazendo upload do APK..." -ForegroundColor Cyan
$uploadUrl = "https://uploads.github.com/repos/$Repo/releases/$releaseId/assets?name=$ApkFile"

try {
    $fileContent = [System.IO.File]::ReadAllBytes((Resolve-Path $ApkFile))
    $uploadHeaders = @{
        "Authorization" = "token $GitHubToken"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $fileContent
    Write-Host "APK enviado com sucesso!" -ForegroundColor Green
    Write-Host "Release disponível em: $($releaseResponse.html_url)" -ForegroundColor Green
} catch {
    Write-Host "Erro ao fazer upload do APK: $_" -ForegroundColor Red
    Write-Host "Release criada, mas o APK não foi anexado. Você pode anexar manualmente em:" -ForegroundColor Yellow
    Write-Host "  $($releaseResponse.html_url)" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n✅ Release criada com sucesso!" -ForegroundColor Green
Write-Host "URL: $($releaseResponse.html_url)" -ForegroundColor Cyan

