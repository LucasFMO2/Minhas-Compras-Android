# Script para gerar APK de release
# Uso: .\gerar-apk.ps1

Write-Host "Gerando APK de release..."

# Limpa o build anterior
Write-Host "Limpando build anterior..."
.\gradlew clean

# Gera o APK de release
Write-Host "Gerando APK..."
.\gradlew assembleRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "APK gerado com sucesso!"
    Write-Host ""
    Write-Host "Localizacao do APK:"
    $apkPath = "app\build\outputs\apk\release\app-release.apk"
    if (Test-Path $apkPath) {
        $fullPath = Resolve-Path $apkPath
        Write-Host $fullPath
        Write-Host ""
        
        # Copia o APK para o diretório raiz
        Write-Host "Copiando APK para o diretório raiz..."
        Copy-Item $apkPath -Destination ".\" -Force
        
        # Renomeia o APK para incluir a versão
        $version = "2.17.2"  # Versão atual do app
        $newApkName = "app-release-v$version.apk"
        Rename-Item "app-release.apk" $newApkName -Force
        
        Write-Host "APK gerado e renomeado para: $newApkName"
        Write-Host "Dica: Voce pode compartilhar este arquivo APK para instalar no seu dispositivo Android."
    }
} else {
    Write-Host "Erro ao gerar APK. Verifique os logs acima."
}

