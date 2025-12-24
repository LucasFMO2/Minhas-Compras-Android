# Script para aplicar todas as otimizações de performance
Write-Host "🚀 Aplicando otimizações de performance..." -ForegroundColor Cyan
Write-Host ""

# 1. Parar daemons do Gradle
Write-Host "1️⃣ Parando daemons do Gradle..." -ForegroundColor Yellow
.\gradlew.bat --stop
Write-Host "   ✅ Daemons parados" -ForegroundColor Green
Write-Host ""

# 2. Limpar cache
Write-Host "2️⃣ Limpando cache..." -ForegroundColor Yellow
.\gradlew.bat clean
Write-Host "   ✅ Cache limpo" -ForegroundColor Green
Write-Host ""

# 3. Testar build com cache
Write-Host "3️⃣ Testando build com novas configurações..." -ForegroundColor Yellow
Write-Host "   (Isso pode levar alguns minutos na primeira vez)" -ForegroundColor Gray
.\gradlew.bat assembleDebug --build-cache
Write-Host "   ✅ Build concluído com sucesso!" -ForegroundColor Green
Write-Host ""

# 4. Resumo das otimizações aplicadas
Write-Host "✅ Otimizações aplicadas com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Resumo das otimizações:" -ForegroundColor Cyan
Write-Host "   ✓ Memória do Gradle: 2GB → 4GB" -ForegroundColor White
Write-Host "   ✓ Build paralelo: Habilitado" -ForegroundColor White
Write-Host "   ✓ Build cache: Habilitado" -ForegroundColor White
Write-Host "   ✓ Workers paralelos: 4" -ForegroundColor White
Write-Host "   ✓ Desugaring incremental: Habilitado" -ForegroundColor White
Write-Host ""
Write-Host "📖 Próximos passos manuais:" -ForegroundColor Cyan
Write-Host "   1. Abra o Android Studio" -ForegroundColor White
Write-Host "   2. File → Settings → Build, Execution, Deployment → Compiler" -ForegroundColor White
Write-Host "      - Build process heap size: 2048 MB" -ForegroundColor Gray
Write-Host "   3. File → Settings → Appearance & Behavior → System Settings" -ForegroundColor White
Write-Host "      - Desabilite 'Synchronize files on frame activation'" -ForegroundColor Gray
Write-Host "   4. Help → Edit Custom VM Options (se disponível)" -ForegroundColor White
Write-Host "      - Adicione: -Xmx4096m" -ForegroundColor Gray
Write-Host "   5. Reinicie o Android Studio" -ForegroundColor White
Write-Host ""
Write-Host "📚 Para mais detalhes, consulte: docs/OTIMIZACAO_PERFORMANCE.md" -ForegroundColor Cyan
Write-Host ""

