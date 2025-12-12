# Script para atualizar Android Studio automaticamente
# Autor: Assistente AI
# Data: 2024

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Atualizador Automatico Android Studio" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Funcao para verificar versao atual do Android Studio
function Get-AndroidStudioVersion {
    $studioExe = $null
    
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
Write-Host "[VERIFICANDO] Verificando instalacao atual..." -ForegroundColor Cyan
$currentInstall = Get-AndroidStudioVersion

if (-not $currentInstall.Installed) {
    Write-Host "[ERRO] Android Studio nao encontrado!" -ForegroundColor Red
    Write-Host "   Por favor, instale o Android Studio primeiro." -ForegroundColor Yellow
    Write-Host "   Execute: .\instalar-android-studio.ps1" -ForegroundColor White
    exit 1
}

Write-Host "[OK] Android Studio encontrado!" -ForegroundColor Green
Write-Host "   Localizacao: $($currentInstall.Path)" -ForegroundColor Gray
Write-Host "   Versao atual: $($currentInstall.Version)" -ForegroundColor Gray
Write-Host ""

# Verificar se o Android Studio esta em execucao
Write-Host "[VERIFICANDO] Verificando se o Android Studio esta em execucao..." -ForegroundColor Cyan
$studioProcess = Get-Process -Name "studio64" -ErrorAction SilentlyContinue

if ($studioProcess) {
    Write-Host "[AVISO] Android Studio esta em execucao!" -ForegroundColor Yellow
    Write-Host "   Para atualizar, e necessario fechar o Android Studio primeiro." -ForegroundColor White
    Write-Host ""
    
    $closeStudio = Read-Host "Deseja fechar o Android Studio agora? (S/N)"
    if ($closeStudio -eq "S" -or $closeStudio -eq "s" -or $closeStudio -eq "Y" -or $closeStudio -eq "y") {
        Write-Host "[FECHANDO] Fechando Android Studio..." -ForegroundColor Cyan
        $studioProcess | Stop-Process -Force
        Start-Sleep -Seconds 3
        Write-Host "[OK] Android Studio fechado!" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Por favor, feche o Android Studio manualmente e execute este script novamente." -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host "[OK] Android Studio nao esta em execucao." -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INSTRUCOES PARA ATUALIZACAO AUTOMATICA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[PASSO 1] Abra o Android Studio" -ForegroundColor Yellow
Write-Host "   Executando Android Studio..." -ForegroundColor White

# Tentar abrir o Android Studio
try {
    Start-Process $currentInstall.Path
    Write-Host "[OK] Android Studio iniciado!" -ForegroundColor Green
    Write-Host ""
    Write-Host "[AGUARDE] Aguarde o Android Studio carregar completamente..." -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "[ERRO] Nao foi possivel abrir o Android Studio automaticamente." -ForegroundColor Red
    Write-Host "   Por favor, abra manualmente: $($currentInstall.Path)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[PASSO 2] Verificar atualizacoes" -ForegroundColor Yellow
Write-Host "   No Android Studio, siga estes passos:" -ForegroundColor White
Write-Host ""
Write-Host "   1. Vá no menu: Help -> Check for Updates" -ForegroundColor Cyan
Write-Host "      (Ou pressione: Ctrl+Alt+S para abrir Settings)" -ForegroundColor Gray
Write-Host "      (Depois vá em: Appearance & Behavior -> System Settings -> Updates)" -ForegroundColor Gray
Write-Host ""
Write-Host "   2. O Android Studio verificara automaticamente se ha atualizacoes disponiveis" -ForegroundColor White
Write-Host ""
Write-Host "   3. Se houver atualizacao:" -ForegroundColor White
Write-Host "      - Clique em 'Update and Restart'" -ForegroundColor Cyan
Write-Host "      - O Android Studio sera atualizado e reiniciado automaticamente" -ForegroundColor Gray
Write-Host ""
Write-Host "   4. Se nao houver atualizacao:" -ForegroundColor White
Write-Host "      - Voce ja esta com a versao mais recente!" -ForegroundColor Green
Write-Host ""

Write-Host "[PASSO 3] Apos a atualizacao" -ForegroundColor Yellow
Write-Host "   Execute o script de configuracao para otimizar:" -ForegroundColor White
Write-Host "   .\CONFIGURAR_ANDROID_STUDIO.ps1" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  CONFIGURACAO DE ATUALIZACOES AUTOMATICAS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[DICA] Configure atualizacoes automaticas:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   1. Abra: File -> Settings (ou Ctrl+Alt+S)" -ForegroundColor White
Write-Host "   2. Vá em: Appearance & Behavior -> System Settings -> Updates" -ForegroundColor White
Write-Host "   3. Configure:" -ForegroundColor White
Write-Host "      - Update channel: 'Stable' (recomendado)" -ForegroundColor Cyan
Write-Host "      - Automatically check for updates: Marque esta opcao" -ForegroundColor Cyan
Write-Host "      - Show balloon when updates are available: Marque esta opcao" -ForegroundColor Cyan
Write-Host ""
Write-Host "   Assim, o Android Studio verificara atualizacoes automaticamente!" -ForegroundColor Green
Write-Host ""

Write-Host "[INFO] Versao mais recente disponivel:" -ForegroundColor Cyan
Write-Host "   Site oficial: https://developer.android.com/studio" -ForegroundColor White
Write-Host "   Ultima versao: Android Studio Iguana (2024.1.1) ou superior" -ForegroundColor Gray
Write-Host ""

Write-Host "[OK] Instrucoes concluidas!" -ForegroundColor Green
Write-Host "   Siga os passos acima para atualizar o Android Studio." -ForegroundColor White
Write-Host ""

