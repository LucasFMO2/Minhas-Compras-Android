# Script para instalar/atualizar Android Studio para a ultima versao
# Autor: Assistente AI
# Data: 2024

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Instalador/Atualizador Android Studio" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se esta executando como administrador
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "[AVISO] Execute como Administrador para instalacao completa" -ForegroundColor Yellow
    Write-Host "   Clique com botao direito -> Executar como Administrador" -ForegroundColor Gray
    Write-Host ""
}

# Função para verificar versão atual do Android Studio
function Get-AndroidStudioVersion {
    $studioExe = $null
    
    # Procurar em locais comuns
    $searchPaths = @(
        "$env:LOCALAPPDATA\Programs\Android\Android Studio\bin\studio64.exe",
        "$env:ProgramFiles\Android\Android Studio\bin\studio64.exe",
        "$env:ProgramFiles(x86)\Android\Android Studio\bin\studio64.exe"
    )
    
    foreach ($path in $searchPaths) {
        if (Test-Path $path) {
            $studioExe = $path
            break
        }
    }
    
    if ($studioExe) {
        $versionInfo = (Get-Item $studioExe).VersionInfo
        return @{
            Installed = $true
            Path = $studioExe
            Version = $versionInfo.FileVersion
            ProductVersion = $versionInfo.ProductVersion
        }
    }
    
    return @{ Installed = $false }
}

# Verificar instalacao atual
Write-Host "[VERIFICANDO] Verificando instalacao atual do Android Studio..." -ForegroundColor Cyan
$currentInstall = Get-AndroidStudioVersion

if ($currentInstall.Installed) {
    Write-Host "[OK] Android Studio encontrado!" -ForegroundColor Green
    Write-Host "   Localizacao: $($currentInstall.Path)" -ForegroundColor Gray
    Write-Host "   Versao: $($currentInstall.Version)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "[INFO] Para atualizar:" -ForegroundColor Yellow
    Write-Host "   1. Abra o Android Studio" -ForegroundColor White
    Write-Host "   2. Va em Help -> Check for Updates" -ForegroundColor White
    Write-Host "   3. Ou baixe manualmente de: https://developer.android.com/studio" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host "[ERRO] Android Studio nao encontrado" -ForegroundColor Red
    Write-Host ""
}

# Informacoes sobre download
Write-Host "[DOWNLOAD] Informacoes de Download:" -ForegroundColor Cyan
Write-Host "   Site oficial: https://developer.android.com/studio" -ForegroundColor White
Write-Host "   Download direto: https://redirector.gvt1.com/edgedl/android/studio/install/2024.1.1.15/android-studio-2024.1.1.15-windows.exe" -ForegroundColor Gray
Write-Host ""

# Perguntar se deseja abrir o site de download
$openDownload = Read-Host "Deseja abrir o site de download no navegador? (S/N)"
if ($openDownload -eq "S" -or $openDownload -eq "s" -or $openDownload -eq "Y" -or $openDownload -eq "y") {
    Write-Host "[ABRINDO] Abrindo site de download..." -ForegroundColor Cyan
    Start-Process "https://developer.android.com/studio"
}

Write-Host ""
Write-Host "[INSTRUCOES] Instrucoes de Instalacao:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Baixe o instalador do Android Studio" -ForegroundColor Yellow
Write-Host "   - Acesse: https://developer.android.com/studio" -ForegroundColor White
Write-Host "   - Clique em 'Download Android Studio'" -ForegroundColor White
Write-Host ""
Write-Host "2. Execute o instalador" -ForegroundColor Yellow
Write-Host "   - Execute o arquivo .exe baixado" -ForegroundColor White
Write-Host "   - Siga o assistente de instalacao" -ForegroundColor White
Write-Host "   - Mantenha as configuracoes padrao recomendadas" -ForegroundColor White
Write-Host ""
Write-Host "3. Configure o Android Studio" -ForegroundColor Yellow
Write-Host "   - Na primeira execucao, escolha 'Standard' installation" -ForegroundColor White
Write-Host "   - Aguarde o download dos componentes (SDK, emulador, etc.)" -ForegroundColor White
Write-Host "   - Isso pode levar alguns minutos" -ForegroundColor White
Write-Host ""
Write-Host "4. Apos a instalacao, execute o script de configuracao:" -ForegroundColor Yellow
Write-Host "   .\CONFIGURAR_ANDROID_STUDIO.ps1" -ForegroundColor Green
Write-Host ""

# Verificar requisitos do sistema
Write-Host "[REQUISITOS] Verificando Requisitos do Sistema:" -ForegroundColor Cyan
Write-Host ""

# Verificar RAM
$ram = (Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB
Write-Host "   RAM: $([math]::Round($ram, 2)) GB" -ForegroundColor $(if ($ram -ge 8) { "Green" } else { "Yellow" })
if ($ram -lt 8) {
    Write-Host "   [AVISO] Recomendado: 8 GB ou mais" -ForegroundColor Yellow
}

# Verificar espaco em disco
$drive = (Get-PSDrive -PSProvider FileSystem | Where-Object { $_.Root -eq (Split-Path $env:USERPROFILE -Qualifier) + "\" })
$freeSpace = $drive.Free / 1GB
Write-Host "   Espaco Livre: $([math]::Round($freeSpace, 2)) GB" -ForegroundColor $(if ($freeSpace -ge 10) { "Green" } else { "Yellow" })
if ($freeSpace -lt 10) {
    Write-Host "   [AVISO] Recomendado: 10 GB ou mais de espaco livre" -ForegroundColor Yellow
}

# Verificar Java/JDK
Write-Host ""
Write-Host "[JAVA] Verificando Java/JDK..." -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion) {
        Write-Host "   [OK] Java encontrado: $javaVersion" -ForegroundColor Green
    }
} catch {
    Write-Host "   [AVISO] Java nao encontrado no PATH" -ForegroundColor Yellow
    Write-Host "   [INFO] O Android Studio inclui o JDK, mas voce pode instalar separadamente" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[OK] Verificacao concluida!" -ForegroundColor Green
Write-Host ""
Write-Host "[PROXIMOS PASSOS] Proximos Passos:" -ForegroundColor Cyan
Write-Host "   1. Baixe e instale o Android Studio" -ForegroundColor White
Write-Host "   2. Execute: .\CONFIGURAR_ANDROID_STUDIO.ps1" -ForegroundColor White
Write-Host "   3. Abra o projeto no Android Studio" -ForegroundColor White
Write-Host "   4. Aguarde a sincronizacao do Gradle" -ForegroundColor White
Write-Host ""
Write-Host "[DICA] O Android Studio pode verificar atualizacoes automaticamente" -ForegroundColor Yellow
Write-Host "   Va em: Help -> Check for Updates" -ForegroundColor Gray
Write-Host ""

