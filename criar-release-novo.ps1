# Script simples para criar release v2.16.0 no GitHub
$ErrorActionPreference = "Stop"

$version = "v2.16.0"
$tag = "v2.16.0"
$owner = "mfc46224-jpg"
$repoName = "Minhas-Compras-Android"
$apkPath = "app-release-v2.16.0.apk"

Write-Host "Criando release $version no GitHub..." -ForegroundColor Cyan

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "APK nao encontrado: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "APK encontrado: $apkPath" -ForegroundColor Green

# Extrair token do remote do git
$remoteUrl = git remote get-url origin
$token = $null

if ($remoteUrl -match 'ghp_[A-Za-z0-9]+') {
    $token = $matches[0]
    Write-Host "Token encontrado no remote do git" -ForegroundColor Green
} else {
    Write-Host "Token do GitHub nao encontrado" -ForegroundColor Red
    exit 1
}

# Criar release via API
$releaseBody = @{
    tag_name = $tag
    name = "Release $version"
    body = "Versao 2.16.0 do aplicativo Minhas Compras Android

Esta versao inclui:
- Melhorias de performance
- Correcao de bugs
- Interface otimizada

Download do APK disponivel abaixo."
    draft = $false
    prerelease = $false
} | ConvertTo-Json -Depth 10

$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
}

$releaseUrl = "https://api.github.com/repos/$owner/$repoName/releases"
$response = Invoke-RestMethod -Uri $releaseUrl -Method Post -Headers $headers -Body $releaseBody -ContentType "application/json"

Write-Host "Release criada com sucesso!" -ForegroundColor Green
Write-Host "URL: $($response.html_url)" -ForegroundColor Cyan

# Upload do APK
$uploadUrl = $response.upload_url -replace '\{\?name,label\}', "?name=app-release-v2.16.0.apk"
$apkBytes = [System.IO.File]::ReadAllBytes($apkPath)

$uploadHeaders = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
    "Content-Type" = "application/vnd.android.package-archive"
}

$uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes

Write-Host "APK anexado com sucesso!" -ForegroundColor Green
Write-Host "Release completa criada!" -ForegroundColor Green
Write-Host "Acesse: $($response.html_url)" -ForegroundColor Cyan