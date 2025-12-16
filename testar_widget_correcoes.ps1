# Script para testar as correções do widget
Write-Host "Iniciando teste das correções do widget..." -ForegroundColor Green

# Compilar o aplicativo
Write-Host "Compilando o aplicativo..." -ForegroundColor Yellow
./gradlew assembleDebug
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Falha na compilação" -ForegroundColor Red
    exit 1
}

Write-Host "Aplicativo compilado com sucesso!" -ForegroundColor Green

# Instalar o APK
Write-Host "Instalando o aplicativo..." -ForegroundColor Yellow
adb install -r app/build/outputs/apk/debug/app-debug.apk
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Falha na instalação" -ForegroundColor Red
    exit 1
}

Write-Host "Aplicativo instalado com sucesso!" -ForegroundColor Green

# Limpar dados anteriores do widget
Write-Host "Limpando dados anteriores do widget..." -ForegroundColor Yellow
adb shell pm clear com.example.minhascompras

# Iniciar o aplicativo
Write-Host "Iniciando o aplicativo..." -ForegroundColor Yellow
adb shell am start -n com.example.minhascompras/.MainActivity

Write-Host "Aguardando inicialização do aplicativo..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Instruções para teste manual
Write-Host "`n=== INSTRUÇÕES PARA TESTE MANUAL ===" -ForegroundColor Cyan
Write-Host "1. Adicione múltiplos itens (3-5) pelo aplicativo principal" -ForegroundColor White
Write-Host "2. Verifique se todos os itens aparecem no widget" -ForegroundColor White
Write-Host "3. Adicione um item diretamente pelo widget" -ForegroundColor White
Write-Host "4. Verifique se o item aparece no aplicativo principal" -ForegroundColor White
Write-Host "5. Marque alguns itens como comprados pelo widget" -ForegroundColor White
Write-Host "6. Verifique se os itens são marcados como comprados no app principal" -ForegroundColor White
Write-Host "7. Adicione itens pelo app principal e verifique se aparecem no widget" -ForegroundColor White
Write-Host "`n=== PROBLEMAS ESPERADOS ===" -ForegroundColor Yellow
Write-Host "Se os problemas foram corrigidos:" -ForegroundColor Green
Write-Host "- O widget deve exibir TODOS os itens (não apenas um)" -ForegroundColor Green
Write-Host "- Itens adicionados pelo app devem aparecer no widget" -ForegroundColor Green
Write-Host "- Itens adicionados pelo widget devem aparecer no app" -ForegroundColor Green
Write-Host "`n=== COMANDOS ÚTEIS ===" -ForegroundColor Cyan
Write-Host "Para ver logs do widget:" -ForegroundColor White
Write-Host "adb logcat -s ShoppingListWidget" -ForegroundColor Gray
Write-Host "`nPara reiniciar o widget:" -ForegroundColor White
Write-Host "adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE" -ForegroundColor Gray
Write-Host "`nTeste concluído! Verifique o comportamento do widget." -ForegroundColor Green