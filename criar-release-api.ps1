# Script para criar release no GitHub usando API
Write-Host "=== Criador de Release GitHub via API ===" -ForegroundColor Cyan

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.9"
$releaseName = "Release v2.28.9"
$apkPath = "app/build/outputs/apk/release/MinhasCompras-v2.28.9-code87.apk"
$notesPath = "RELEASE_NOTES_v2.28.9.md"

Write-Host "Este script cria o release usando a API do GitHub" -ForegroundColor Yellow
Write-Host ""
Write-Host "Para usar este script, você precisa:" -ForegroundColor White
Write-Host "1. Gerar um Personal Access Token no GitHub" -ForegroundColor White
Write-Host "2. Configurar o token com permissão 'repo'" -ForegroundColor White
Write-Host ""

# Solicitar token
$token = Read-Host "Digite seu Personal Access Token do GitHub" -AsSecureString
$tokenPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($token))

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

Write-Host ""
Write-Host "Criando release..." -ForegroundColor Green
Write-Host "- Repositório: $repoOwner/$repoName" -ForegroundColor White
Write-Host "- Tag: $tagName" -ForegroundColor White
Write-Host "- APK: $apkPath" -ForegroundColor White
Write-Host ""

# Preparar headers
$headers = @{
    "Authorization" = "token $tokenPlain"
    "Accept" = "application/vnd.github.v3+json"
}

# Criar release sem arquivo primeiro
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
        "Authorization" = "token $tokenPlain"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadUrl = $release.upload_url.Replace("{?name,label}","?name=$apkFileName&label=$apkFileName")
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host ""
    Write-Host "✅ Release criado com sucesso!" -ForegroundColor Green
    Write-Host "📦 APK uploaded: $apkFileName" -ForegroundColor Green
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
Write-Host "Pressione qualquer tecla para sair..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")