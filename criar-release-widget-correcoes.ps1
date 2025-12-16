# Script para criar release v2.28.10 com correções do widget
Write-Host "=== Criador de Release v2.28.10 - Correções do Widget ===" -ForegroundColor Cyan

Write-Host "Este script irá:" -ForegroundColor Yellow
Write-Host "1. Gerar APK com as correções do widget" -ForegroundColor White
Write-Host "2. Criar release no GitHub" -ForegroundColor White
Write-Host "3. Fazer upload do APK" -ForegroundColor White
Write-Host ""

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"
$releaseName = "Release v2.28.10 - Correções do Widget"
$versionCode = "88"
$apkPath = "app/build/outputs/apk/release/MinhasCompras-v2.28.10-code88.apk"
$notesPath = "RELEASE_NOTES_v2.28.10.md"

# Criar release notes
$releaseNotes = @"
# Release v2.28.10 - Correções do Widget

## Correções Implementadas

### 1. Correção do Conflito de Request Code no PendingIntent
- Implementado sistema de geração de request codes verdadeiramente únicos usando hash baseado em múltiplos parâmetros
- Adicionada verificação de conflitos e geração de códigos de emergência
- Melhorada a estratégia de configuração do PendingIntent em múltiplos elementos do item

### 2. Melhorias nos Logs de Validação
- Adicionados logs detalhados para debugging do processo de toggle de itens
- Implementada validação crítica antes e após as operações do banco
- Adicionada verificação de existência do widget antes do processamento
- Implementados logs de debugging detalhado para todos os intents recebidos

### 3. Melhorias no Fluxo do onReceive()
- Implementada validação de segurança antes do processamento de actions
- Adicionada verificação de existência do widget antes de processar cliques
- Melhorado o fluxo de processamento com validações em múltiplos pontos
- Implementado sistema de retry para atualizações que falham

## Detalhes Técnicos

- Versão: 2.28.10
- Código: 88
- Data: $(Get-Date -Format "dd/MM/yyyy")
- Componentes afetados: Widget Provider e Widget Service

## Testes Realizados

- Teste de toggle de itens no widget
- Teste de conflito de request codes
- Teste de validação de segurança
- Teste de fluxo completo do onReceive()

## Instalação

1. Baixe o APK deste release
2. Instale no seu dispositivo Android
3. Adicione o widget à tela inicial
4. Teste as funcionalidades corrigidas

---

**Observações Importantes:**
- Esta versão corrige problemas reportados com o não funcionamento do clique em itens do widget
- As melhorias nos logs ajudarão em futuros debuggings
- O sistema de validação agora é mais robusto e seguro
"@

# Salvar release notes
$releaseNotes | Out-File -FilePath $notesPath -Encoding UTF8
Write-Host "Release notes criados: $notesPath" -ForegroundColor Green
Write-Host ""

# Gerar APK
Write-Host "🔨 Gerando APK de release..." -ForegroundColor Cyan

# Limpar build anterior
Write-Host "🧹 Limpando build anterior..." -ForegroundColor Yellow
& .\gradlew clean

# Gerar o APK de release
Write-Host "📦 Gerando APK v2.28.10..." -ForegroundColor Yellow
& .\gradlew assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro ao gerar APK. Verifique os logs acima." -ForegroundColor Red
    exit 1
}

# Verificar APK gerado
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ ERRO: APK não encontrado em $apkPath" -ForegroundColor Red
    Write-Host "Verificando arquivos gerados..." -ForegroundColor Yellow
    Get-ChildItem -Path "app/build/outputs/apk/release/" -Filter "*.apk" | ForEach-Object {
        Write-Host "Encontrado: $($_.FullName)" -ForegroundColor White
    }
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "✅ APK gerado com sucesso!" -ForegroundColor Green
Write-Host "📍 Localização: $apkPath" -ForegroundColor Cyan
Write-Host "📏 Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
Write-Host ""

# Perguntar se deseja criar release no GitHub
$resposta = Read-Host "Deseja criar release no GitHub agora? (S/N)"
if ($resposta -notmatch "^[Ss]$") {
    Write-Host "APK gerado com sucesso! Você pode fazer upload manualmente." -ForegroundColor Green
    Write-Host "Arquivo: $apkPath" -ForegroundColor Cyan
    exit 0
}

# Solicitar token
Write-Host ""
Write-Host "Para criar release no GitHub, precisamos do Personal Access Token" -ForegroundColor Yellow
$token = Read-Host "Digite seu Personal Access Token do GitHub" -AsSecureString
$tokenPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($token))

Write-Host ""
Write-Host "Criando release no GitHub..." -ForegroundColor Yellow

# Headers
$headers = @{
    "Authorization" = "token $tokenPlain"
    "Accept" = "application/vnd.github.v3+json"
}

# Criar release
$releaseData = @{
    tag_name = $tagName
    name = $releaseName
    body = $releaseNotes
    draft = $false
    prerelease = $false
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/releases" -Method Post -Headers $headers -Body $releaseData -ContentType "application/json"
    $release = $response | ConvertFrom-Json
    
    Write-Host "Release criado! ID: $($release.id)" -ForegroundColor Green
    
    # Upload APK
    Write-Host "Fazendo upload do APK..." -ForegroundColor Yellow
    $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
    $apkFileName = Split-Path $apkPath -Leaf
    
    $uploadHeaders = @{
        "Authorization" = "token $tokenPlain"
        "Content-Type" = "application/vnd.android.package-archive"
    }
    
    $uploadUrl = $release.upload_url.Replace("{?name,label}","?name=$apkFileName&label=$apkFileName")
    
    $uploadResponse = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
    
    Write-Host ""
    Write-Host "✅ SUCESSO!" -ForegroundColor Green
    Write-Host "Release v2.28.10 criado e APK publicado!" -ForegroundColor Green
    Write-Host "URL: $($release.html_url)" -ForegroundColor Cyan
    Write-Host "APK: $apkFileName" -ForegroundColor Cyan
    Write-Host "Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
    
} catch {
    Write-Host "❌ ERRO:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Verifique as permissões do token" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Processo concluído!" -ForegroundColor Green
Write-Host "APK gerado: $apkPath" -ForegroundColor Cyan