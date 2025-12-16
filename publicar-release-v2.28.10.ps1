# Script para publicar release v2.28.10 no GitHub
param(
    [string]$Token = ""
)

Write-Host "=== Publicador de Release v2.28.10 ===" -ForegroundColor Cyan

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"
$releaseName = "Release v2.28.10 - Correções do Widget"
$apkPath = "app-release-v2.28.10.apk"
$notesPath = "RELEASE_NOTES_v2.28.10.md"

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

# Obter token
if ([string]::IsNullOrEmpty($Token)) {
    Write-Host ""
    Write-Host "🔑 Para publicar a release, precisamos do Personal Access Token do GitHub" -ForegroundColor Yellow
    Write-Host "   1. Vá para: https://github.com/settings/tokens" -ForegroundColor White
    Write-Host "   2. Clique em 'Generate new token (classic)'" -ForegroundColor White
    Write-Host "   3. Selecione permissão 'repo'" -ForegroundColor White
    Write-Host "   4. Copie o token gerado" -ForegroundColor White
    Write-Host ""
    
    $Token = Read-Host "Digite seu Personal Access Token do GitHub"
    
    if ([string]::IsNullOrEmpty($Token)) {
        Write-Host "❌ Token não fornecido. Abortando." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "🚀 Publicando release v2.28.10..." -ForegroundColor Green
Write-Host "- Repositório: $repoOwner/$repoName" -ForegroundColor White
Write-Host "- Tag: $tagName" -ForegroundColor White
Write-Host "- APK: $apkPath" -ForegroundColor White
Write-Host ""

# Preparar headers
$headers = @{
    "Authorization" = "token $Token"
    "Accept" = "application/vnd.github.v3+json"
}

# Criar release
$releaseData = @{
    tag_name = $tagName
    name = $releaseName
    body = $releaseNotes
    draft = $false
    prerelease = $false
} | ConvertTo-Json

try {
    Write-Host "📝 Criando release..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/releases" -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    $release = $response | ConvertFrom-Json
    
    Write-Host "✅ Release criado com ID: $($release.id)" -ForegroundColor Green
    
    # Upload do APK
    Write-Host "📦 Fazendo upload do APK..." -ForegroundColor Yellow
    $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
    $apkFileName = Split-Path $apkPath -Leaf
    
    $uploadHeaders = @{
        "Authorization" = "token $Token"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadUrl = $release.upload_url.Replace("{?name,label}","?name=$apkFileName&label=$apkFileName")
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host ""
    Write-Host "🎉 RELEASE PUBLICADA COM SUCESSO!" -ForegroundColor Green
    Write-Host "📦 APK: $apkFileName" -ForegroundColor Green
    Write-Host "📏 Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🔗 URL da Release: $($release.html_url)" -ForegroundColor Cyan
    Write-Host "📱 Download do APK: $($release.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ Erro ao publicar release:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Verifique se:" -ForegroundColor Yellow
        Write-Host "  • Seu token tem permissao 'repo'" -ForegroundColor White
        Write-Host "  • A tag v2.28.10 nao existe no repositorio" -ForegroundColor White
        Write-Host "  • Voce tem permissao de escrita no repositorio" -ForegroundColor White
    }
    exit 1
}

Write-Host ""
Write-Host "✅ Processo concluido com sucesso!" -ForegroundColor Green