# Script simples para criar release no GitHub
$token = $env:GITHUB_TOKEN
if (-not $token) {
    Write-Host "ERRO: Variável de ambiente GITHUB_TOKEN não encontrada!" -ForegroundColor Red
    Write-Host "Execute: `$env:GITHUB_TOKEN = 'seu_token_aqui'" -ForegroundColor Yellow
    exit 1
}

Write-Host "=== Criando Release v2.28.9 ===" -ForegroundColor Cyan

# Configuracoes
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.9"
$releaseName = "Release v2.28.9"
$apkPath = "app-release-v2.28.9.apk"
$notesPath = "RELEASE_NOTES_v2.28.9.md"

Write-Host "Verificando APK..." -ForegroundColor Yellow
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado!" -ForegroundColor Red
    exit 1
}

Write-Host "Lendo notas de release..." -ForegroundColor Yellow
$releaseNotes = ""
if (Test-Path $notesPath) {
    $releaseNotes = Get-Content $notesPath -Raw
}

$headers = @{
    Authorization = "token $token"
    Accept = "application/vnd.github.v3+json"
}

$releaseData = @{
    tag_name = $tagName
    name = $releaseName
    body = $releaseNotes
    draft = $false
    prerelease = $false
} | ConvertTo-Json

try {
    Write-Host "Criando release no GitHub..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/releases" -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    $release = $response | ConvertFrom-Json
    
    Write-Host "Release criado! ID: $($release.id)" -ForegroundColor Green
    
    Write-Host "Fazendo upload do APK..." -ForegroundColor Yellow
    $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
    $apkFileName = Split-Path $apkPath -Leaf
    
    $uploadHeaders = @{
        Authorization = "token $token"
        Content-Type = "application/vnd.android.package-archive"
    }
    
    $uploadUrl = $release.upload_url.Replace("{?name,label}","?name=$apkFileName&label=$apkFileName")
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host "" 
    Write-Host "Release criado com sucesso!" -ForegroundColor Green
    Write-Host "APK uploaded: $apkFileName" -ForegroundColor Green
    Write-Host "URL: $($release.html_url)" -ForegroundColor Cyan
    
} catch {
    Write-Host "ERRO:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Verifique as permissoes do token" -ForegroundColor Yellow
    }
}

Write-Host "Processo concluido!" -ForegroundColor Cyan