# Script rápido para reinstalar o app após mudanças
# Uso: .\reinstalar-app.ps1

$SDK_PATH = "C:\Users\nerdd\AppData\Local\Android\Sdk"
$ADB_PATH = "$SDK_PATH\platform-tools\adb.exe"
$APP_PACKAGE = "com.example.minhascompras"
$APP_ACTIVITY = "$APP_PACKAGE/.MainActivity"

# Verificar se emulador está rodando
$devices = & $ADB_PATH devices | Select-String "device$"
if (-not $devices) {
    Write-Host "Erro: Nenhum emulador/dispositivo conectado!" -ForegroundColor Red
    Write-Host "Execute primeiro: .\rodar-emulador.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "Reinstalando app..." -ForegroundColor Cyan

# Reinstalar (mais rápido que rebuild completo)
& .\gradlew.bat installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ App reinstalado!" -ForegroundColor Green
    
    # Reiniciar o app
    & $ADB_PATH shell am force-stop $APP_PACKAGE
    Start-Sleep -Milliseconds 500
    & $ADB_PATH shell am start -n $APP_ACTIVITY
    
    Write-Host "✓ App reiniciado!" -ForegroundColor Green
} else {
    Write-Host "Erro na reinstalação!" -ForegroundColor Red
    exit 1
}

