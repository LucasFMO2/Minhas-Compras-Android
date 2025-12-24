# Script para criar release v2.16.0 no GitHub
$ErrorActionPreference = "Stop"

$version = "v2.16.0"
$tag = "v2.16.0"
$owner = "Lucasfmo1"
$repoName = "Minhas-Compras-Android"
$apkPath = "app-release-v2.16.0.apk"
$notesFile = "release-notes-v2.16.0.md"

Write-Host "🚀 Criando release $version no GitHub..." -ForegroundColor Cyan
Write-Host ""

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK não encontrado: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ APK encontrado: $apkPath" -ForegroundColor Green
$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "   Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Gray
Write-Host ""

# Verificar se o arquivo de notas existe
if (-not (Test-Path $notesFile)) {
    Write-Host "⚠️  Arquivo de notas não encontrado: $notesFile" -ForegroundColor Yellow
    $releaseNotes = "# Release v2.16.0 - Versão Estável`n`nVersão estável com correção do diálogo de atualização."
} else {
    $releaseNotes = Get-Content $notesFile -Raw
    Write-Host "✅ Notas de release encontradas" -ForegroundColor Green
}

Write-Host ""

# Extrair token do remote do git
$remoteUrl = git remote get-url origin
$token = $null

if ($remoteUrl -match 'ghp_[A-Za-z0-9]+') {
    $token = $matches[0]
    Write-Host "✅ Token encontrado no remote do git" -ForegroundColor Green
} elseif ($env:GITHUB_TOKEN) {
    $token = $env:GITHUB_TOKEN
    Write-Host "✅ Token encontrado na variável de ambiente" -ForegroundColor Green
} else {
    Write-Host "❌ Token do GitHub não encontrado" -ForegroundColor Red
    Write-Host ""
    Write-Host "📝 Para criar a release manualmente:" -ForegroundColor Yellow
    Write-Host "   1. Acesse: https://github.com/$owner/$repoName/releases/new" -ForegroundColor White
    Write-Host "   2. Selecione a tag: $tag" -ForegroundColor White
    Write-Host "   3. Título: Release $version - Versão Estável" -ForegroundColor White
    Write-Host "   4. Descrição: Copie o conteúdo de $notesFile" -ForegroundColor White
    Write-Host "   5. Anexe o arquivo: $apkPath" -ForegroundColor White
    Write-Host "   6. Clique em 'Publish release'" -ForegroundColor White
    Write-Host ""
    Write-Host "📍 Localização do APK:" -ForegroundColor Cyan
    $fullPath = (Resolve-Path $apkPath).Path
    Write-Host "   $fullPath" -ForegroundColor White
    exit 1
}

# Criar release via API
Write-Host "📝 Criando release via API do GitHub..." -ForegroundColor Cyan
Write-Host ""

$releaseBody = @{
    tag_name = $tag
    name = "Release $version - Versão Estável"
    body = $releaseNotes
    draft = $false
    prerelease = $false
} | ConvertTo-Json -Depth 10

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Accept" = "application/vnd.github.v3+json"
    }
    
    $releaseUrl = "https://api.github.com/repos/$owner/$repoName/releases"
    Write-Host "📤 Enviando requisição para criar release..." -ForegroundColor Yellow
    
    $response = Invoke-RestMethod -Uri $releaseUrl -Method Post -Headers $headers -Body $releaseBody -ContentType "application/json"
    
    Write-Host "✅ Release criada com sucesso!" -ForegroundColor Green
    Write-Host "🔗 URL: $($response.html_url)" -ForegroundColor Cyan
    Write-Host ""
    
    # Upload do APK
    Write-Host "📤 Fazendo upload do APK..." -ForegroundColor Yellow
    
    $uploadUrl = $response.upload_url -replace '\{\?name,label\}', "?name=app-release-v2.16.0.apk"
    
    $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
    
    $uploadHeaders = @{
        "Authorization" = "Bearer $token"
        "Accept" = "application/vnd.github.v3+json"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host "✅ APK anexado com sucesso!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🎉 Release completa criada!" -ForegroundColor Green
    Write-Host "🔗 Acesse: $($response.html_url)" -ForegroundColor Cyan
    Write-Host ""
    
} catch {
    Write-Host "❌ Erro ao criar release: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Detalhes: $responseBody" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "📝 Para criar a release manualmente:" -ForegroundColor Yellow
    Write-Host "   1. Acesse: https://github.com/$owner/$repoName/releases/new" -ForegroundColor White
    Write-Host "   2. Selecione a tag: $tag" -ForegroundColor White
    Write-Host "   3. Título: Release $version - Versão Estável" -ForegroundColor White
    Write-Host "   4. Descrição: Copie o conteúdo de $notesFile" -ForegroundColor White
    Write-Host "   5. Anexe o arquivo: $apkPath" -ForegroundColor White
    Write-Host "   6. Clique em 'Publish release'" -ForegroundColor White
    exit 1
}

Write-Host ""
Read-Host "Pressione ENTER para sair"

