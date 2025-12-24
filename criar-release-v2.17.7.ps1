# Script para criar release pública v2.17.7 com APK específico
# Carrega variáveis de ambiente do arquivo .env automaticamente
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$loadEnvPath = Join-Path $scriptPath "load-env.ps1"

if (Test-Path $loadEnvPath) {
    . $loadEnvPath
    if (Test-Path (Join-Path $scriptPath ".env")) {
        Load-EnvFile -EnvFile (Join-Path $scriptPath ".env") | Out-Null
    }
}

# O token deve ser fornecido via variável de ambiente GITHUB_TOKEN ou será solicitado
if ($env:GITHUB_TOKEN) {
    $token = $env:GITHUB_TOKEN
} else {
    Write-Host "Token GitHub nao encontrado na variavel de ambiente GITHUB_TOKEN" -ForegroundColor Yellow
    Write-Host "Por favor, defina no arquivo .env ou forneca o token quando solicitado" -ForegroundColor Yellow
    $token = Read-Host "Cole seu Personal Access Token do GitHub" -AsSecureString
    $token = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($token)
    )
}
$apkPath = "app-release-v2.17.7.apk"
$apkName = "Minhas-Compras-v2.17.7.apk"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CRIANDO RELEASE PUBLICA v2.17.7" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "[ERRO] APK nao encontrado: $apkPath" -ForegroundColor Red
    exit 1
}

$apkInfo = Get-Item $apkPath
Write-Host "[OK] APK encontrado:" -ForegroundColor Green
Write-Host "  Nome: $($apkInfo.Name)" -ForegroundColor White
Write-Host "  Tamanho: $([math]::Round($apkInfo.Length/1MB,2)) MB" -ForegroundColor White
Write-Host ""

# Ler release notes do arquivo
$releaseNotesPath = "release-notes-v2.17.7.md"
if (Test-Path $releaseNotesPath) {
    $releaseBody = Get-Content $releaseNotesPath -Raw
    Write-Host "[OK] Release notes carregadas de $releaseNotesPath" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Release notes nao encontradas, usando texto padrao" -ForegroundColor Yellow
    $releaseBody = @"
## Release v2.17.7

### Melhorias
- Sistema de arquivamento atualizado para múltiplas listas
- Filtro de histórico por lista específica ou todas as listas
- Interface aprimorada com FilterChips para navegação

### Instalacao
Baixe o APK abaixo e instale no seu dispositivo Android.

### Informacoes Tecnicas
- Version Code: 73
- Version Name: 2.17.7
- Target SDK: 34
- Min SDK: 24
"@
}

Write-Host ""

# Headers para API
$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
    "User-Agent" = "PowerShell"
}

# Dados da release
$releaseData = @{
    tag_name = "v2.17.7"
    name = "Release v2.17.7 - Sistema de Arquivamento Atualizado"
    body = $releaseBody
    draft = $false
    prerelease = $false
} | ConvertTo-Json

# Criar release
Write-Host "[1/2] Criando release no GitHub..." -ForegroundColor Yellow
try {
    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/LucasFMO2/Minhas-Compras-Android/releases" `
        -Method Post `
        -Headers $headers `
        -Body $releaseData `
        -ContentType "application/json"
    
    Write-Host "[OK] Release criada com sucesso!" -ForegroundColor Green
    Write-Host "  URL: $($release.html_url)" -ForegroundColor Cyan
    Write-Host "  ID: $($release.id)" -ForegroundColor Gray
    Write-Host ""
    
    $releaseId = $release.id
} catch {
    Write-Host "[ERRO] Falha ao criar release: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Detalhes: $responseBody" -ForegroundColor Yellow
    }
    exit 1
}

# Fazer upload do APK
Write-Host "[2/2] Enviando APK..." -ForegroundColor Yellow
$fileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $apkPath))
$uploadUrl = "https://uploads.github.com/repos/LucasFMO2/Minhas-Compras-Android/releases/$releaseId/assets?name=$apkName"

$uploadHeaders = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
    "Content-Type" = "application/vnd.android.package-archive"
}

try {
    $asset = Invoke-RestMethod -Uri $uploadUrl `
        -Method Post `
        -Headers $uploadHeaders `
        -Body $fileBytes `
        -ContentType "application/vnd.android.package-archive"
    
    Write-Host "[OK] APK enviado com sucesso!" -ForegroundColor Green
    Write-Host "  Download: $($asset.browser_download_url)" -ForegroundColor Cyan
    Write-Host "  Tamanho: $([math]::Round($asset.size/1MB,2)) MB" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "[ERRO] Falha ao enviar APK: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Detalhes: $responseBody" -ForegroundColor Yellow
    }
    exit 1
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "RELEASE CRIADA COM SUCESSO!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Release: $($release.html_url)" -ForegroundColor Cyan
Write-Host "APK: $($asset.browser_download_url)" -ForegroundColor Cyan
Write-Host ""

