# Script para rodar o app Android no emulador
# Uso: .\rodar-emulador.ps1 [nome-do-avd] [--logs] [--rebuild]

param(
    [string]$AvdName = "",
    [switch]$Logs = $false,
    [switch]$Rebuild = $true,
    [switch]$Help = $false
)

# Cores para output
function Write-ColorOutput {
    param(
        [string]$ForegroundColor,
        [string]$Message
    )
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    Write-Output $Message
    $host.UI.RawUI.ForegroundColor = $fc
}

function Show-Help {
    $helpText = @"
========================================
  Script de Emulador Android - Minhas Compras
========================================

Uso:
  .\rodar-emulador.ps1 [opções]

Opções:
  -AvdName <nome>    Nome específico do AVD para iniciar
  -Logs              Mostrar logs do app após iniciar
  -Rebuild           Recompilar o app antes de instalar (padrão: true)
  -Help              Mostrar esta ajuda

Exemplos:
  .\rodar-emulador.ps1
  .\rodar-emulador.ps1 -AvdName "Pixel_5_API_33"
  .\rodar-emulador.ps1 -Logs
  .\rodar-emulador.ps1 -AvdName "Pixel_5_API_33" -Logs -Rebuild

========================================
"@
    Write-ColorOutput -ForegroundColor Cyan -Message $helpText
}

if ($Help) {
    Show-Help
    exit 0
}

# Configurações
$SDK_PATH = "C:\Users\nerdd\AppData\Local\Android\Sdk"
$EMULATOR_PATH = "$SDK_PATH\emulator\emulator.exe"
$ADB_PATH = "$SDK_PATH\platform-tools\adb.exe"
$APP_PACKAGE = "com.example.minhascompras"
$APP_ACTIVITY = "$APP_PACKAGE/.MainActivity"

# Verificar se SDK existe
if (-not (Test-Path $SDK_PATH)) {
    Write-ColorOutput -ForegroundColor Red -Message "Erro: SDK do Android não encontrado em $SDK_PATH"
    Write-ColorOutput -ForegroundColor Yellow -Message "Verifique o caminho em local.properties"
    exit 1
}

# Adicionar ao PATH se necessário
$env:PATH = "$SDK_PATH\emulator;$SDK_PATH\platform-tools;$SDK_PATH\tools;$env:PATH"

# Função para verificar se emulador está rodando
function Test-EmulatorRunning {
    $devices = & $ADB_PATH devices | Select-String "device$"
    return $null -ne $devices
}

# Função para listar AVDs disponíveis
function Get-AvailableAvds {
    if (-not (Test-Path $EMULATOR_PATH)) {
        Write-ColorOutput -ForegroundColor Red -Message "Erro: emulator.exe não encontrado em $EMULATOR_PATH"
        return @()
    }
    
    $avds = & $EMULATOR_PATH -list-avds
    if ($avds) {
        return $avds -split "`r?`n" | Where-Object { $_.Trim() -ne "" }
    }
    return @()
}

# Função para iniciar emulador
function Start-Emulator {
    param([string]$Avd)
    
    Write-ColorOutput -ForegroundColor Cyan -Message "Iniciando emulador: $Avd"
    Write-ColorOutput -ForegroundColor Yellow -Message "Isso pode levar alguns minutos na primeira vez..."
    
    # Iniciar em background
    $process = Start-Process -FilePath $EMULATOR_PATH -ArgumentList "-avd", $Avd -PassThru -WindowStyle Minimized
    
    # Aguardar o emulador inicializar
    Write-ColorOutput -ForegroundColor Cyan -Message "Aguardando emulador inicializar..."
    $maxWait = 120 # 2 minutos
    $waited = 0
    
    while (-not (Test-EmulatorRunning) -and $waited -lt $maxWait) {
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Progress -Activity "Aguardando emulador" -Status "Aguardando dispositivo..." -PercentComplete (($waited / $maxWait) * 100)
    }
    
    Write-Progress -Activity "Aguardando emulador" -Completed
    
    if (-not (Test-EmulatorRunning)) {
        Write-ColorOutput -ForegroundColor Red -Message "Erro: Emulador não iniciou a tempo"
        exit 1
    }
    
    # Aguardar mais um pouco para o sistema ficar totalmente pronto
    Write-ColorOutput -ForegroundColor Yellow -Message "Aguardando sistema ficar pronto..."
    Start-Sleep -Seconds 10
    
    Write-ColorOutput -ForegroundColor Green -Message "✓ Emulador pronto!"
    return $process
}

# Função para compilar e instalar
function Install-App {
    param([bool]$Rebuild = $true)
    
    if ($Rebuild) {
        Write-ColorOutput -ForegroundColor Cyan -Message "Compilando o app..."
        & .\gradlew.bat assembleDebug
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput -ForegroundColor Red -Message "Erro na compilação!"
            exit 1
        }
        Write-ColorOutput -ForegroundColor Green -Message "✓ Compilação concluída"
    }
    
    Write-ColorOutput -ForegroundColor Cyan -Message "Instalando o app no emulador..."
    & .\gradlew.bat installDebug
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput -ForegroundColor Red -Message "Erro na instalação!"
        exit 1
    }
    Write-ColorOutput -ForegroundColor Green -Message "✓ App instalado"
}

# Função para iniciar o app
function Start-App {
    Write-ColorOutput -ForegroundColor Cyan -Message "Iniciando o app..."
    & $ADB_PATH shell am start -n $APP_ACTIVITY
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput -ForegroundColor Green -Message "✓ App iniciado!"
    } else {
        Write-ColorOutput -ForegroundColor Yellow -Message "Aviso: Pode ser que o app já esteja rodando"
    }
}

# Função para mostrar logs
function Show-Logs {
    Write-ColorOutput -ForegroundColor Cyan -Message "`nMostrando logs do app (Ctrl+C para parar)..."
    Write-ColorOutput -ForegroundColor Yellow -Message "Filtro: $APP_PACKAGE`n"
    & $ADB_PATH logcat | Select-String $APP_PACKAGE
}

# ============================================
# EXECUÇÃO PRINCIPAL
# ============================================

Write-ColorOutput -ForegroundColor Cyan -Message "========================================"
Write-ColorOutput -ForegroundColor Cyan -Message "  Minhas Compras - Emulador Android"
Write-ColorOutput -ForegroundColor Cyan -Message "========================================`n"

# Verificar se emulador já está rodando
if (Test-EmulatorRunning) {
    Write-ColorOutput -ForegroundColor Green -Message "✓ Emulador já está rodando"
    $device = & $ADB_PATH devices | Select-String "device$"
    Write-ColorOutput -ForegroundColor Cyan -Message "Dispositivo: $device"
} else {
    # Listar AVDs disponíveis
    $avds = Get-AvailableAvds
    
    if ($avds.Count -eq 0) {
        Write-ColorOutput -ForegroundColor Red -Message "Erro: Nenhum AVD encontrado!"
        Write-ColorOutput -ForegroundColor Yellow -Message "Crie um AVD no Android Studio: Tools -> Device Manager -> Create Device"
        exit 1
    }
    
    # Escolher AVD
    if ($AvdName -eq "") {
        Write-ColorOutput -ForegroundColor Cyan -Message "`nAVDs disponíveis:"
        for ($i = 0; $i -lt $avds.Count; $i++) {
            Write-ColorOutput -ForegroundColor White -Message "  [$i] $($avds[$i])"
        }
        
        if ($avds.Count -eq 1) {
            $AvdName = $avds[0]
            Write-ColorOutput -ForegroundColor Cyan -Message "`nUsando o único AVD disponível: $AvdName"
        } else {
            $choice = Read-Host "`nEscolha o número do AVD (ou Enter para o primeiro)"
            if ($choice -eq "") {
                $AvdName = $avds[0]
            } else {
                $index = [int]$choice
                if ($index -ge 0 -and $index -lt $avds.Count) {
                    $AvdName = $avds[$index]
                } else {
                    Write-ColorOutput -ForegroundColor Red -Message "Escolha inválida!"
                    exit 1
                }
            }
        }
    } else {
        # Verificar se o AVD especificado existe
        if ($AvdName -notin $avds) {
            Write-ColorOutput -ForegroundColor Red -Message "Erro: AVD '$AvdName' não encontrado!"
            Write-ColorOutput -ForegroundColor Cyan -Message "AVDs disponíveis:"
            $avds | ForEach-Object { Write-ColorOutput -ForegroundColor White -Message "  - $_" }
            exit 1
        }
    }
    
    # Iniciar emulador
    $emulatorProcess = Start-Emulator -Avd $AvdName
}

Write-Host ""

# Compilar e instalar
Install-App -Rebuild $Rebuild

# Iniciar app
Start-App

# Mostrar logs se solicitado
if ($Logs) {
    Show-Logs
} else {
    Write-ColorOutput -ForegroundColor Cyan -Message "`n========================================"
    Write-ColorOutput -ForegroundColor Green -Message "✓ App rodando no emulador!"
    Write-ColorOutput -ForegroundColor Cyan -Message "========================================"
    Write-ColorOutput -ForegroundColor Yellow -Message "`nDicas:"
    Write-ColorOutput -ForegroundColor White -Message "  - Para ver logs: .\rodar-emulador.ps1 -Logs"
    Write-ColorOutput -ForegroundColor White -Message "  - Para reinstalar após mudanças: .\rodar-emulador.ps1 -Rebuild:`$false"
    Write-ColorOutput -ForegroundColor White -Message "  - Para escolher AVD: .\rodar-emulador.ps1 -AvdName 'NomeDoAVD'"
    Write-Host ""
}

