# Script para ver logs do app em tempo real
# Uso: .\ver-logs.ps1 [filtro]

param(
    [string]$Filter = "com.example.minhascompras"
)

$SDK_PATH = "C:\Users\nerdd\AppData\Local\Android\Sdk"
$ADB_PATH = "$SDK_PATH\platform-tools\adb.exe"

# Verificar se emulador está rodando
$devices = & $ADB_PATH devices | Select-String "device$"
if (-not $devices) {
    Write-Host "Erro: Nenhum emulador/dispositivo conectado!" -ForegroundColor Red
    exit 1
}

Write-Host "Mostrando logs do app (Ctrl+C para parar)..." -ForegroundColor Cyan
Write-Host "Filtro: $Filter`n" -ForegroundColor Yellow

# Mostrar logs filtrados
& $ADB_PATH logcat | Select-String $Filter

