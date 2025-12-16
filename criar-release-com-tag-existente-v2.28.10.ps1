# Script para criar release v2.28.10 no GitHub usando tag existente
Write-Host "=== Criador de Release v2.28.10 (Tag Existente) - Correções do Widget ===" -ForegroundColor Cyan

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"
$releaseName = "Release v2.28.10 - Correções do Widget"
$apkPath = "app-release-v2.28.10.apk"
$notesPath = "RELEASE_NOTES_v2.28.10.md"
$token = $env:GITHUB_TOKEN
if (-not $token) {
    Write-Host "ERRO: Variável de ambiente GITHUB_TOKEN não encontrada!" -ForegroundColor Red
    Write-Host "Execute: `$env:GITHUB_TOKEN = 'seu_token_aqui'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Criando release v2.28.10 usando tag existente..." -ForegroundColor Yellow
Write-Host ""

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK não encontrado em $apkPath" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "✅ APK encontrado: $apkPath ($([math]::Round($apkSize, 2)) MB)" -ForegroundColor Green

# Ler notas de release
$releaseNotes = ""
if (Test-Path $notesPath) {
    $releaseNotes = Get-Content $notesPath -Raw
    Write-Host "✅ Notas de release encontradas: $notesPath" -ForegroundColor Green
} else {
    $releaseNotes = "Release v2.28.10 - Correções do Widget"
    Write-Host "⚠️ Usando notas de release padrão" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Criando release..." -ForegroundColor Green
Write-Host "- Repositório: $repoOwner/$repoName" -ForegroundColor White
Write-Host "- Tag: $tagName (existente)" -ForegroundColor White
Write-Host "- APK: $apkPath" -ForegroundColor White
Write-Host ""

# Preparar headers
$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
}

# Criar release usando tag existente
$releaseData = @{
    tag_name = $tagName
    name = $releaseName
    body = $releaseNotes
    draft = $false
    prerelease = $false
} | ConvertTo-Json

try {
    # Criar release
    Write-Host "Enviando release para a API..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/releases" -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    $release = $response | ConvertFrom-Json
    
    Write-Host "Release criado com ID: $($release.id)" -ForegroundColor Green
    
    # Upload do APK
    Write-Host "Fazendo upload do APK..." -ForegroundColor Yellow
    $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
    $apkFileName = Split-Path $apkPath -Leaf
    
    $uploadHeaders = @{
        "Authorization" = "token $token"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadUrl = $release.upload_url.Replace("{?name,label}","?name=$apkFileName&label=$apkFileName")
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host ""
    Write-Host "✅ Release v2.28.10 criado com sucesso!" -ForegroundColor Green
    Write-Host "📦 APK uploaded: $apkFileName" -ForegroundColor Green
    Write-Host "📏 Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "URL do Release: $($release.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Erro ao criar release:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Verifique se seu token tem permissão 'repo'" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Processo concluído!" -ForegroundColor Green