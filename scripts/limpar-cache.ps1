# Script para limpar cache do Gradle e do Android Studio
Write-Host "🧹 Limpando cache do Gradle e builds..." -ForegroundColor Cyan

# Remove diretórios de cache e build
$directories = @(
    ".gradle",
    "build",
    "app/build",
    ".idea/caches",
    ".idea/modules.xml"
)

foreach ($dir in $directories) {
    if (Test-Path $dir) {
        Write-Host "Removendo: $dir" -ForegroundColor Yellow
        Remove-Item -Recurse -Force $dir -ErrorAction SilentlyContinue
    }
}

Write-Host "✅ Cache limpo com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Cyan
Write-Host "1. Abra o Android Studio" -ForegroundColor White
Write-Host "2. File → Invalidate Caches... → Invalidate and Restart" -ForegroundColor White
Write-Host "3. Após reiniciar: File → Sync Project with Gradle Files" -ForegroundColor White

