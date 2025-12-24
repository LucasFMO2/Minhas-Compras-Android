# Script para criar release no GitHub
# Uso: .\criar-release-github.ps1

$ErrorActionPreference = "Continue"
$version = "v2.10.8"
$tag = "v2.10.8"
$repo = "Lucasfmo1/Minhas-Compras-Android"
$apkPath = "app-release-v2.10.8.apk"

Write-Host "🚀 Criando release $version no GitHub..." -ForegroundColor Cyan
Write-Host ""

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ APK não encontrado: $apkPath" -ForegroundColor Red
    Write-Host "   Certifique-se de que o arquivo está na pasta raiz do projeto." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ APK encontrado: $apkPath" -ForegroundColor Green
$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "   Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Gray
Write-Host ""

# Verificar se gh CLI está disponível
$ghAvailable = $false
try {
    $ghVersion = gh --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        $ghAvailable = $true
        Write-Host "✅ GitHub CLI encontrado" -ForegroundColor Green
    }
} catch {
    $ghAvailable = $false
}

if (-not $ghAvailable) {
    Write-Host "⚠️  GitHub CLI não encontrado" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📝 INSTRUÇÕES PARA CRIAR A RELEASE MANUALMENTE:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Acesse: https://github.com/$repo/releases/new" -ForegroundColor White
    Write-Host ""
    Write-Host "2. Preencha os campos:" -ForegroundColor Yellow
    Write-Host "   • Tag: Selecione v2.10.8 no dropdown (ou digite v2.10.8)" -ForegroundColor White
    Write-Host "   • Título: Release v2.10.8" -ForegroundColor White
    Write-Host "   • Descrição: Copie o conteúdo do arquivo RELEASE_NOTES_v2.10.8.md" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Para anexar o APK:" -ForegroundColor Yellow
    Write-Host "   • Clique na area Attach binaries by dropping them here or selecting them" -ForegroundColor White
    Write-Host "   • OU arraste o arquivo app-release-v2.10.8.apk para essa area" -ForegroundColor White
    Write-Host "   • OU clique em selecting them e navegue ate o arquivo" -ForegroundColor White
    Write-Host ""
    Write-Host '4. Clique no botao Publish release (verde, no final da pagina)' -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📍 Localização do APK:" -ForegroundColor Cyan
    $fullPath = (Resolve-Path $apkPath).Path
    Write-Host "   $fullPath" -ForegroundColor White
    Write-Host ""
    exit 0
}

# Verificar autenticação
Write-Host "🔐 Verificando autenticação..." -ForegroundColor Cyan
try {
    $authStatus = gh auth status 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Não autenticado no GitHub CLI" -ForegroundColor Red
        Write-Host "   Execute: gh auth login" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Autenticado no GitHub" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erro ao verificar autenticação" -ForegroundColor Red
    exit 1
}

# Criar release com gh CLI
Write-Host "📝 Criando release com anexo do APK..." -ForegroundColor Cyan
Write-Host ""

$releaseNotes = @"
## Release v2.10.8

✨ **Atualizações e Melhorias:**
- 🎨 **Melhorias na interface** - Componentes de UI aprimorados (ItemCompraCard, StatisticCard)
- 📱 **Ajustes na tela de lista** - Melhorias na experiência do usuário na tela principal
- 📐 **Responsividade aprimorada** - Melhor adaptação para diferentes tamanhos de tela
- 🔧 **Otimizações gerais** - Melhorias de performance e estabilidade
"@

# Criar release
Write-Host "Executando: gh release create $tag --title 'Release $version' --notes '...' $apkPath" -ForegroundColor Gray
Write-Host ""

gh release create $tag `
    --title "Release $version" `
    --notes $releaseNotes `
    $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Release criada com sucesso!" -ForegroundColor Green
    Write-Host "🔗 URL: https://github.com/$repo/releases/tag/$tag" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📱 O APK está disponível para download na página da release!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erro ao criar release" -ForegroundColor Red
    Write-Host "   Verifique se a tag v2.10.8 já existe e se você tem permissões" -ForegroundColor Yellow
    exit 1
}

