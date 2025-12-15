# Script para testar a funcionalidade de toggle no widget
# Autor: Assistente de Desenvolvimento
# Data: $(Get-Date -Format "dd/MM/yyyy HH:mm")

Write-Host "=== INICIANDO TESTES DO WIDGET TOGGLE ===" -ForegroundColor Green
Write-Host ""

# Função para executar comandos com tratamento de erro
function Execute-Command {
    param(
        [string]$Command,
        [string]$Description
    )
    
    Write-Host "Executando: $Description" -ForegroundColor Yellow
    try {
        Invoke-Expression $Command
        Write-Host "✅ Sucesso: $Description" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "❌ Erro: $Description" -ForegroundColor Red
        Write-Host "Detalhes: $_" -ForegroundColor Red
        return $false
    }
}

# 1. Limpar e compilar o projeto
Write-Host "=== ETAPA 1: COMPILAÇÃO ===" -ForegroundColor Cyan
$success = Execute-Command "./gradlew clean" "Limpar projeto"
if (-not $success) {
    Write-Host "Falha na limpeza, abortando..." -ForegroundColor Red
    exit 1
}

$success = Execute-Command "./gradlew assembleDebug" "Compilar versão debug"
if (-not $success) {
    Write-Host "Falha na compilação, abortando..." -ForegroundColor Red
    exit 1
}

Write-Host ""

# 2. Instalar o APK
Write-Host "=== ETAPA 2: INSTALAÇÃO ===" -ForegroundColor Cyan
$apkPath = "app/build/outputs/apk/debug/app-debug.apk"
if (Test-Path $apkPath) {
    $success = Execute-Command "adb install -r $apkPath" "Instalar APK"
    if (-not $success) {
        Write-Host "Falha na instalação, tentando desinstalar primeiro..." -ForegroundColor Yellow
        Execute-Command "adb uninstall com.example.minhascompras" "Desinstalar app"
        $success = Execute-Command "adb install $apkPath" "Reinstalar APK"
        if (-not $success) {
            Write-Host "Falha na reinstalação, abortando..." -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "❌ APK não encontrado em: $apkPath" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 3. Iniciar o aplicativo
Write-Host "=== ETAPA 3: INICIALIZAÇÃO ===" -ForegroundColor Cyan
$success = Execute-Command "adb shell am start -n com.example.minhascompras/.MainActivity" "Iniciar aplicativo"
Write-Host ""

# 4. Aguardar inicialização
Write-Host "Aguardando inicialização do aplicativo..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 5. Verificar logs do widget
Write-Host "=== ETAPA 4: VERIFICAÇÃO DE LOGS ===" -ForegroundColor Cyan
Write-Host "Monitorando logs do widget (pressione Ctrl+C para parar)..." -ForegroundColor Yellow
Write-Host ""

# Filtrar logs relevantes do widget
adb logcat -s "ShoppingListWidget" | ForEach-Object {
    $timestamp = Get-Date -Format "HH:mm:ss"
    $message = $_
    
    # Destacar mensagens importantes
    if ($message -match "toggleItemStatus|toggleFilter|ACTION_ITEM_CLICKED") {
        Write-Host "[$timestamp] 🔄 $message" -ForegroundColor Green
    } elseif ($message -match "ERROR|FALHA") {
        Write-Host "[$timestamp] ❌ $message" -ForegroundColor Red
    } elseif ($message -match "MUDANÇA DETECTADA|Widget.*atualizado") {
        Write-Host "[$timestamp] ✅ $message" -ForegroundColor Cyan
    } else {
        Write-Host "[$timestamp] ℹ️  $message" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "=== TESTES CONCLUÍDOS ===" -ForegroundColor Green
Write-Host "Verifique manualmente:" -ForegroundColor Yellow
Write-Host "1. Toque em itens para alternar status" -ForegroundColor White
Write-Host "2. Use o botão de filtro para alternar visualização" -ForegroundColor White
Write-Host "3. Verifique sincronização com o app principal" -ForegroundColor White
Write-Host ""