# Script para deploy v2.18.1 no GitHub
# Este script envia o APK para o GitHub, cria tag e release

Write-Host "=== DEPLOY GITHUB v2.18.1 ===" -ForegroundColor Green
Write-Host "Data: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor Yellow

# Configuracoes
$apkPath = "app/build/outputs/apk/release/app-release-v2.18.1.apk"
$releaseNotesPath = "RELEASE_NOTES_v2.18.1.md"
$tagName = "v2.18.1"
$releaseTitle = "Release v2.18.1 - Correcao de Crash"

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK nao encontrado em: $apkPath" -ForegroundColor Red
    exit 1
}

# Verificar se as notas de release existem
if (-not (Test-Path $releaseNotesPath)) {
    Write-Host "ERRO: Arquivo de notas nao encontrado: $releaseNotesPath" -ForegroundColor Red
    exit 1
}

Write-Host "Arquivos encontrados:" -ForegroundColor Green
Write-Host "  APK: $apkPath" -ForegroundColor White
Write-Host "  Notas: $releaseNotesPath" -ForegroundColor White

# Inicializar Git
Write-Host "Inicializando repositório Git..." -ForegroundColor Cyan
try {
    git status
    git add .
    git commit -m "Release v2.18.1 - Correcao de crash ao adicionar itens

- Correcao critica: Resolvido problema que causava o fechamento do aplicativo ao adicionar ou editar itens
- Melhoria na estabilidade: Implementado tratamento robusto de excecoes em todas as camadas
- Logs detalhados: Adicionado sistema de logs completo para facilitar diagnóstico
- Validacao de dados: Implementada validacao adicional antes de inserir itens
- Integridade do banco: Melhorada verificacao da lista padrao

Changes:
- Atualizada versao para 2.18.1
- Adicionado logs detalhados em ViewModel, Repository e DAO
- Melhorada validacao de dados no dialogo de adicao
- Garantida integridade da tabela shopping_lists
- Tratamento robusto de excecoes com feedback ao usuario"
    
    Write-Host "Commit realizado com sucesso!" -ForegroundColor Green
} catch {
    Write-Host "ERRO NO COMMIT: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Criar tag
Write-Host "Criando tag $tagName..." -ForegroundColor Cyan
try {
    git tag -a $tagName -m $releaseTitle
    Write-Host "Tag $tagName criada com sucesso!" -ForegroundColor Green
} catch {
    Write-Host "ERRO AO CRIAR TAG: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Enviar para GitHub (usando push com tags)
Write-Host "Enviando para GitHub..." -ForegroundColor Cyan
try {
    git push origin main --tags
    Write-Host "Push realizado com sucesso!" -ForegroundColor Green
} catch {
    Write-Host "ERRO NO PUSH: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Criar release no GitHub usando CLI
Write-Host "Criando release no GitHub..." -ForegroundColor Cyan
try {
    # Verificar se o CLI do GitHub está instalado
    $ghVersion = gh --version 2>$null
    if ($ghVersion) {
        Write-Host "GitHub CLI detectado: $ghVersion" -ForegroundColor Green
        
        # Criar release
        $releaseCommand = "gh release create $tagName --title ""$releaseTitle"" --notes-file ""$releaseNotesPath"" ""$apkPath"""
        Write-Host "Executando: $releaseCommand" -ForegroundColor Gray
        
        Invoke-Expression $releaseCommand
        Write-Host "Release criado com sucesso no GitHub!" -ForegroundColor Green
    } else {
        Write-Host "AVISO: GitHub CLI nao encontrado. Release precisa ser criado manualmente." -ForegroundColor Yellow
        Write-Host "URL para criar release: https://github.com/SEU_REPOSITORIO/releases/new" -ForegroundColor White
    }
} catch {
    Write-Host "ERRO AO CRIAR RELEASE: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "AVISO: Release pode ser criado manualmente no GitHub" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== DEPLOY CONCLUIDO ===" -ForegroundColor Green
Write-Host "Versao: 2.18.1" -ForegroundColor White
Write-Host "Tag: $tagName" -ForegroundColor White
Write-Host "APK: $(Split-Path $apkPath -Leaf)" -ForegroundColor White
Write-Host "Status: Deploy realizado com sucesso!" -ForegroundColor Green