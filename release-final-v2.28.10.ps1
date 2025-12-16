# Script simples para publicar release v2.28.10
Write-Host "=== Release v2.28.10 - Correcoes do Widget ===" -ForegroundColor Cyan

# Configuracoes
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"
$releaseName = "Release v2.28.10 - Correcoes do Widget"
$apkPath = "app-release-v2.28.10.apk"

# Verificar APK
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "APK encontrado: $apkPath ($([math]::Round($apkSize, 2)) MB)" -ForegroundColor Green

# Solicitar token
Write-Host ""
Write-Host "Para criar a release, voce precisa de um Personal Access Token do GitHub" -ForegroundColor Yellow
Write-Host "1. Va para: https://github.com/settings/tokens" -ForegroundColor White
Write-Host "2. Crie um token com permissao 'repo'" -ForegroundColor White
Write-Host ""

$token = Read-Host "Digite seu token (ou pressione Enter para abrir pagina manual)"

if ([string]::IsNullOrEmpty($token)) {
    Write-Host "Abrindo pagina manual..." -ForegroundColor Yellow
    $url = "https://github.com/$repoOwner/$repoName/releases/new?tag=$tagName&title=Release%20$tagName"
    Start-Process $url
    Write-Host "Use o arquivo RELEASE_NOTES_v2.28.10.md para as notas" -ForegroundColor Cyan
    exit 0
}

# Criar release com token
Write-Host "Criando release..." -ForegroundColor Green

$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
}

$releaseNotes = Get-Content "RELEASE_NOTES_v2.28.10.md" -Raw

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
    
    Write-Host "Release criado com ID: $($release.id)" -ForegroundColor Green
    
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
    Write-Host "SUCESSO! Release v2.28.10 publicada!" -ForegroundColor Green
    Write-Host "APK: $apkFileName" -ForegroundColor Green
    Write-Host "URL: $($release.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "ERRO ao criar release:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}