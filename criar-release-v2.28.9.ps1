# Script para criar release v2.28.9 no GitHub
$token = $env:GITHUB_TOKEN
if (-not $token) {
    Write-Host "ERRO: Variável de ambiente GITHUB_TOKEN não encontrada!" -ForegroundColor Red
    Write-Host "Execute: `$env:GITHUB_TOKEN = 'seu_token_aqui'" -ForegroundColor Yellow
    exit 1
}

Write-Host "=== Criador de Release GitHub v2.28.9 ===" -ForegroundColor Cyan
Write-Host "Token fornecido diretamente!" -ForegroundColor Green
Write-Host ""

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.9"
$releaseName = "Release v2.28.9"
$apkPath = "app-release-v2.28.9.apk"
$notesPath = "RELEASE_NOTES_v2.28.9.md"

Write-Host "Criando release..." -ForegroundColor Yellow

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
    "Authorization" = "token $token"
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
        "Authorization" = "token $token"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadUrl = $release.upload_url.Replace("{?name,label}","?name=$apkFileName&label=$apkFileName")
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host ""
    Write-Host "✅ Release criado com sucesso!" -ForegroundColor Green
    Write-Host "📦 APK uploaded: $apkFileName" -ForegroundColor Green
    Write-Host "URL: $($release.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ ERRO:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host 'Status: ' $_.Exception.Response.StatusCode -ForegroundColor Red
        Write-Host 'Verifique as permissoes do token' -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host 'Processo concluido!' -ForegroundColor Cyan