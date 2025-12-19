# Script para criar release v2.18.1 - Correcao de Crash
# Este script compila o aplicativo, gera o APK assinado e prepara para deploy

Write-Host "=== CRIANDO RELEASE v2.18.1 - CORRECAO DE CRASH ===" -ForegroundColor Green
Write-Host "Data: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Yellow

# Limpar builds anteriores
Write-Host "Limpando builds anteriores..." -ForegroundColor Cyan
Remove-Item -Path "app/build/outputs/apk/release/*" -Force -ErrorAction SilentlyContinue

# Compilar o aplicativo
Write-Host "Compilando o aplicativo..." -ForegroundColor Cyan
try {
    .\gradlew assembleRelease
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO NA COMPILACAO!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Compilacao concluida com sucesso!" -ForegroundColor Green
} catch {
    Write-Host "ERRO CRITICO NA COMPILACAO: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Verificar se o APK foi gerado
$apkPath = "app/build/outputs/apk/release/app-release.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado em: $apkPath" -ForegroundColor Red
    exit 1
}

# Obter informacoes do APK
$apkInfo = Get-Item $apkPath
$apkSize = [math]::Round($apkInfo.Length / 1MB, 2)
$apkDate = $apkInfo.LastWriteTime

Write-Host "APK GERADO:" -ForegroundColor Green
Write-Host "   Caminho: $apkPath" -ForegroundColor White
Write-Host "   Tamanho: $apkSize MB" -ForegroundColor White
Write-Host "   Data: $apkDate" -ForegroundColor White

# Copiar APK com nome da versao
$versionedApk = "app-release-v2.18.1.apk"
$versionedPath = "app/build/outputs/apk/release/$versionedApk"

Copy-Item $apkPath $versionedPath -Force
Write-Host "APK copiado como: $versionedApk" -ForegroundColor Green

# Criar arquivo de checksum
$checksumPath = "app/build/outputs/apk/release/app-release-v2.18.1.sha256"
try {
    $hash = Get-FileHash $versionedPath -Algorithm SHA256
    $hash.Hash | Out-File $checksumPath -Encoding UTF8
    Write-Host "Checksum SHA256 gerado: $checksumPath" -ForegroundColor Green
    Write-Host "   Hash: $($hash.Hash)" -ForegroundColor Gray
} catch {
    Write-Host "AVISO: Erro ao gerar checksum: $($_.Exception.Message)" -ForegroundColor Yellow
}

# Resumo
Write-Host ""
Write-Host "=== RESUMO DO RELEASE v2.18.1 ===" -ForegroundColor Green
Write-Host "Aplicativo: Minhas Compras" -ForegroundColor White
Write-Host "Versao: 2.18.1" -ForegroundColor White
Write-Host "APK: $versionedApk" -ForegroundColor White
Write-Host "Tamanho: $apkSize MB" -ForegroundColor White
Write-Host "Assinado: Sim" -ForegroundColor White
Write-Host "Notas: Correcao de crash ao adicionar itens" -ForegroundColor White
Write-Host ""

Write-Host "PROXIMOS PASSOS:" -ForegroundColor Yellow
Write-Host "1. Testar o APK em um dispositivo/emulador" -ForegroundColor White
Write-Host "2. Enviar para o GitHub" -ForegroundColor White
Write-Host "3. Criar tag no GitHub: v2.18.1" -ForegroundColor White
Write-Host "4. Criar release no GitHub" -ForegroundColor White
Write-Host ""

Write-Host "Release v2.18.1 preparado com sucesso!" -ForegroundColor Green