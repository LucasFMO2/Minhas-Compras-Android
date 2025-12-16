# Script para gerar APK de release
# Uso: .\gerar-apk.ps1

Write-Host "🔨 Gerando APK de release..." -ForegroundColor Cyan

# Limpa o build anterior
Write-Host "🧹 Limpando build anterior..." -ForegroundColor Yellow
.\gradlew clean

# Gera o APK de release
Write-Host "📦 Gerando APK..." -ForegroundColor Yellow
.\gradlew assembleRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ APK gerado com sucesso!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📱 Localização do APK:" -ForegroundColor Cyan
    $apkPath = "app\build\outputs\apk\release\app-release.apk"
    if (Test-Path $apkPath) {
        $fullPath = Resolve-Path $apkPath
        Write-Host $fullPath -ForegroundColor White
        Write-Host ""
        Write-Host "💡 Dica: Você pode compartilhar este arquivo APK para instalar em outros dispositivos Android." -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Erro ao gerar APK. Verifique os logs acima." -ForegroundColor Red
}

