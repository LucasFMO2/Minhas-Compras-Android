# Script simplificado para criar release v2.17.0 no GitHub
$ErrorActionPreference = "Continue"
$version = "v2.17.0"
$tag = "v2.17.0"
$repo = "mfc46224-jpg/Minhas-Compras-Android"
$apkPath = "app-release-v2.17.0.apk"

Write-Host "Criando release $version no GitHub..." -ForegroundColor Cyan

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host "APK encontrado: $apkPath" -ForegroundColor Green
$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Gray

# Verificar GitHub CLI
try {
    $ghVersion = gh --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "GitHub CLI encontrado" -ForegroundColor Green
        
        # Verificar autenticacao
        $authStatus = gh auth status 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "ERRO: Nao autenticado no GitHub CLI" -ForegroundColor Red
            Write-Host "Execute: gh auth login" -ForegroundColor Yellow
            exit 1
        }
        
        Write-Host "Autenticado no GitHub" -ForegroundColor Green
        
        # Criar release
        $releaseNotes = "Release v2.17.0 - Melhorias na interface e responsividade"
        
        gh release create $tag --title "Release $version" --notes $releaseNotes $apkPath
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Release criada com sucesso!" -ForegroundColor Green
            Write-Host "URL: https://github.com/$repo/releases/tag/$tag" -ForegroundColor Cyan
        } else {
            Write-Host "ERRO ao criar release" -ForegroundColor Red
            exit 1
        }
    } else {
        throw "GitHub CLI nao disponivel"
    }
} catch {
    Write-Host "GitHub CLI nao disponivel ou erro na autenticacao" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "INSTRUCOES PARA CRIAR RELEASE MANUALMENTE:" -ForegroundColor Cyan
    Write-Host "1. Acesse: https://github.com/$repo/releases/new" -ForegroundColor White
    Write-Host "2. Tag: v2.17.0" -ForegroundColor White
    Write-Host "3. Titulo: Release v2.17.0" -ForegroundColor White
    Write-Host "4. Anexe o arquivo: $apkPath" -ForegroundColor White
    Write-Host "5. Clique em Publish release" -ForegroundColor White
}