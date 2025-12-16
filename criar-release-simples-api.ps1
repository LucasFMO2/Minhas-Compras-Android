# Script simples para criar release no GitHub via API
Write-Host "=== Criador de Release GitHub API ===" -ForegroundColor Cyan

Write-Host "Este script precisa de um Personal Access Token do GitHub" -ForegroundColor Yellow
Write-Host "1. Vá para: https://github.com/settings/tokens" -ForegroundColor White
Write-Host "2. Clique em 'Generate new token'" -ForegroundColor White
Write-Host "3. Selecione permissão 'repo'" -ForegroundColor White
Write-Host "4. Copie o token gerado" -ForegroundColor White
Write-Host ""

# Solicitar token
$token = Read-Host "Digite seu Personal Access Token do GitHub" -AsSecureString
$tokenPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($token))

Write-Host ""
Write-Host "Token recebido!" -ForegroundColor Green
Write-Host "Criando release..." -ForegroundColor Yellow

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.9"
$releaseName = "Release v2.28.9"
$apkPath = "app/build/outputs/apk/release/MinhasCompras-v2.28.9-code87.apk"
$notesPath = "RELEASE_NOTES_v2.28.9.md"

# Verificar APK
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK não encontrado!" -ForegroundColor Red
    exit 1
}

# Ler notas
$releaseNotes = ""
if (Test-Path $notesPath) {
    $releaseNotes = Get-Content $notesPath -Raw
}

# Headers
$headers = @{
    "Authorization" = "token $tokenPlain"
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
    $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/releases" -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    $release = $response | ConvertFrom-Json
    
    Write-Host "Release criado! ID: $($release.id)" -ForegroundColor Green
    
    # Upload APK
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
    Write-Host "✅ SUCESSO!" -ForegroundColor Green
    Write-Host "Release criado e APK publicado!" -ForegroundColor Green
    Write-Host "URL: $($release.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ ERRO:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Verifique as permissões do token" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")