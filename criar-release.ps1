# Script para criar release no GitHub via API
$ErrorActionPreference = "Stop"

$version = "v2.10.8"
$tag = "v2.10.8"
$repo = "nerddescoladofmo-cmyk/Minhas-Compras-Android"
$apkPath = "app-release-v2.10.8.apk"
$owner = "nerddescoladofmo-cmyk"
$repoName = "Minhas-Compras-Android"

Write-Host "🚀 Criando release $version no GitHub..." -ForegroundColor Cyan

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK não encontrado: $apkPath" -ForegroundColor Red
    exit 1
}

# Tentar usar GitHub CLI primeiro
$ghAvailable = $false
try {
    $null = gh --version 2>$null
    $ghStatus = gh auth status 2>&1
    if ($LASTEXITCODE -eq 0) {
        $ghAvailable = $true
    }
} catch {
    $ghAvailable = $false
}

if ($ghAvailable) {
    Write-Host "✅ GitHub CLI encontrado, criando release..." -ForegroundColor Green
    
    $releaseNotes = @"
## Release v2.10.8

✨ **Atualizações e Melhorias:**
- 🎨 **Melhorias na interface** - Componentes de UI aprimorados (ItemCompraCard, StatisticCard)
- 📱 **Ajustes na tela de lista** - Melhorias na experiência do usuário na tela principal
- 📐 **Responsividade aprimorada** - Melhor adaptação para diferentes tamanhos de tela
- 🔧 **Otimizações gerais** - Melhorias de performance e estabilidade
"@
    
    # Criar release
    gh release create $tag `
        --title "Release $version" `
        --notes $releaseNotes `
        $apkPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Release criada com sucesso!" -ForegroundColor Green
        Write-Host "🔗 URL: https://github.com/$repo/releases/tag/$tag" -ForegroundColor Cyan
        exit 0
    } else {
        Write-Host "❌ Erro ao criar release com gh CLI" -ForegroundColor Red
    }
}

# Se GitHub CLI não funcionou, tentar via API
Write-Host "⚠️  GitHub CLI não disponível ou falhou. Tentando via API..." -ForegroundColor Yellow

# Verificar se há token do GitHub
$token = $env:GITHUB_TOKEN
if (-not $token) {
    Write-Host "❌ GITHUB_TOKEN não encontrado nas variáveis de ambiente" -ForegroundColor Red
    Write-Host "📝 Para criar a release manualmente:" -ForegroundColor Yellow
    Write-Host "   1. Acesse: https://github.com/$repo/releases/new" -ForegroundColor White
    Write-Host "   2. Selecione a tag: $tag" -ForegroundColor White
    Write-Host "   3. Título: Release $version" -ForegroundColor White
    Write-Host "   4. Anexe o arquivo: $apkPath" -ForegroundColor White
    Write-Host "   5. Publique a release" -ForegroundColor White
    exit 1
}

$releaseBody = @{
    tag_name = $tag
    name = "Release $version"
    body = @"
## Release v2.10.8

✨ **Atualizações e Melhorias:**
- 🎨 **Melhorias na interface** - Componentes de UI aprimorados (ItemCompraCard, StatisticCard)
- 📱 **Ajustes na tela de lista** - Melhorias na experiência do usuário na tela principal
- 📐 **Responsividade aprimorada** - Melhor adaptação para diferentes tamanhos de tela
- 🔧 **Otimizações gerais** - Melhorias de performance e estabilidade
"@
    draft = $false
    prerelease = $false
} | ConvertTo-Json

try {
    $headers = @{
        "Authorization" = "token $token"
        "Accept" = "application/vnd.github.v3+json"
    }
    
    $releaseUrl = "https://api.github.com/repos/$owner/$repoName/releases"
    $response = Invoke-RestMethod -Uri $releaseUrl -Method Post -Headers $headers -Body $releaseBody -ContentType "application/json"
    
    Write-Host "✅ Release criada via API!" -ForegroundColor Green
    Write-Host "🔗 URL: $($response.html_url)" -ForegroundColor Cyan
    
    # Upload do APK
    Write-Host "📤 Fazendo upload do APK..." -ForegroundColor Yellow
    $uploadUrl = $response.upload_url -replace '\{\?name,label\}', "?name=app-release-v2.10.8.apk"
    
    $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
    $apkBase64 = [System.Convert]::ToBase64String($apkBytes)
    
    $uploadHeaders = @{
        "Authorization" = "token $token"
        "Accept" = "application/vnd.github.v3+json"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host "✅ APK anexado com sucesso!" -ForegroundColor Green
    Write-Host "🔗 Release: $($response.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Erro ao criar release via API: $_" -ForegroundColor Red
    Write-Host "📝 Crie a release manualmente em: https://github.com/$repo/releases/new" -ForegroundColor Yellow
    exit 1
}

