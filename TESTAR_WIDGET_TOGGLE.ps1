# Script para testar a funcionalidade de toggle do widget
Write-Host "=== INICIANDO TESTE DO WIDGET TOGGLE ===" -ForegroundColor Green

# Limpar builds anteriores
Write-Host "Limpando builds anteriores..." -ForegroundColor Yellow
./gradlew clean

# Compilar o app com os logs de debugging
Write-Host "Compilando app com logs de debugging..." -ForegroundColor Yellow
./gradlew assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Falha na compilação!" -ForegroundColor Red
    exit 1
}

Write-Host "Build concluído com sucesso!" -ForegroundColor Green

# Instalar o APK
Write-Host "Instalando APK no emulador/dispositivo..." -ForegroundColor Yellow
adb install -r app/build/outputs/apk/debug/app-debug.apk

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Falha na instalação!" -ForegroundColor Red
    exit 1
}

Write-Host "APK instalado com sucesso!" -ForegroundColor Green

# Instruções para o teste
Write-Host "=== INSTRUÇÕES PARA O TESTE ===" -ForegroundColor Cyan
Write-Host "1. Adicione o widget à tela inicial" -ForegroundColor White
Write-Host "2. Configure o widget com uma lista de compras" -ForegroundColor White
Write-Host "3. Adicione alguns itens à lista" -ForegroundColor White
Write-Host "4. Tente marcar/desmarcar itens no widget" -ForegroundColor White
Write-Host "5. Monitore os logs com:" -ForegroundColor Yellow
Write-Host "   adb logcat -s ShoppingListWidget" -ForegroundColor Gray
Write-Host ""
Write-Host "=== LOGS ESPERADOS ===" -ForegroundColor Cyan
Write-Host "Procure por estas mensagens nos logs:" -ForegroundColor White
Write-Host "- '!!! VALIDAÇÃO REQUEST CODE:' (para detectar conflitos)" -ForegroundColor Gray
Write-Host "- '!!! CONFLITO DETECTADO:' (se houver conflito de request code)" -ForegroundColor Red
Write-Host "- '!!! VALIDAÇÃO CRÍTICA:' (para verificar o fluxo do onReceive)" -ForegroundColor Yellow
Write-Host "- '!!! VALIDAÇÃO ANTES:' (estado do item antes da alteração)" -ForegroundColor Gray
Write-Host "- '!!! ALTERAÇÃO:' (detalhes da alteração do status)" -ForegroundColor Yellow
Write-Host "- '!!! VALIDAÇÃO DEPOIS:' (confirmação da alteração no banco)" -ForegroundColor Gray
Write-Host "- '!!! ERRO CRÍTICO:' (se o status no banco não confere)" -ForegroundColor Red
Write-Host ""
Write-Host "Para monitorar logs em tempo real:" -ForegroundColor Green
Write-Host "adb logcat -s ShoppingListWidget | Select-String '!!!'" -ForegroundColor Gray

Write-Host "=== TESTE PRONTO ===" -ForegroundColor Green