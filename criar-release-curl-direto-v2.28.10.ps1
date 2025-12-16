# Script para criar release v2.28.10 usando curl diretamente
Write-Host "=== Criador de Release v2.28.10 (cURL Direto) - Correções do Widget ===" -ForegroundColor Cyan

# Configurações
$repoOwner = "roseanerosafmo-sketch"
$repoName = "Minhas-Compras-Android"
$tagName = "v2.28.10"
$releaseName = "Release v2.28.10 - Correções do Widget"
$apkPath = "app-release-v2.28.10.apk"
$notesPath = "RELEASE_NOTES_v2.28.10.md"
$token = $env:GITHUB_TOKEN
if (-not $token) {
    Write-Host "ERRO: Variável de ambiente GITHUB_TOKEN não encontrada!" -ForegroundColor Red
    Write-Host "Execute: `$env:GITHUB_TOKEN = 'seu_token_aqui'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Criando release v2.28.10 usando cURL..." -ForegroundColor Yellow
Write-Host ""

# Verificar se o APK existe
if (-not (Test-Path $apkPath)) {
    Write-Host "ERRO: APK não encontrado em $apkPath" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "✅ APK encontrado: $apkPath ($([math]::Round($apkSize, 2)) MB)" -ForegroundColor Green

# Ler notas de release
$releaseNotes = ""
if (Test-Path $notesPath) {
    $releaseNotes = Get-Content $notesPath -Raw
    Write-Host "✅ Notas de release encontradas: $notesPath" -ForegroundColor Green
} else {
    $releaseNotes = "Release v2.28.10 - Correções do Widget"
    Write-Host "⚠️ Usando notas de release padrão" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Criando release..." -ForegroundColor Green
Write-Host "- Repositório: $repoOwner/$repoName" -ForegroundColor White
Write-Host "- Tag: $tagName" -ForegroundColor White
Write-Host "- APK: $apkPath" -ForegroundColor White
Write-Host ""

# Escapar as notas para JSON
$releaseNotesJson = $releaseNotes -replace "`"", "\`"" -replace "`n", "\n" -replace "`r", "\r"

# Criar JSON para a release
$jsonRelease = "{
    `"tag_name`": `"$tagName`",
    `"name`": `"$releaseName`",
    `"body`": `"$releaseNotesJson`",
    `"draft`": false,
    `"prerelease`": false,
    `"target_commitish`": `"main`"
}"

Write-Host "JSON da release:" -ForegroundColor Gray
Write-Host $jsonRelease -ForegroundColor Gray
Write-Host ""

try {
    # Criar release usando curl
    Write-Host "Enviando release para a API..." -ForegroundColor Yellow
    
    $curlCommand = "curl -X POST `"https://api.github.com/repos/$repoOwner/$repoName/releases`" `
        `-H `"Authorization: token $token`" `
        `-H `"Accept: application/vnd.github.v3+json`" `
        `-H `"Content-Type: application/json`" `
        `-d `"$jsonRelease`""

    Write-Host "Executando: $curlCommand" -ForegroundColor Gray
    
    $response = cmd /c $curlCommand 2>&1
    Write-Host "Resposta: $response" -ForegroundColor Gray
    
    # Tentar extrair ID da release
    if ($response -match '"id":\s*(\d+)') {
        $releaseId = $matches[1]
        Write-Host "Release criado com ID: $releaseId" -ForegroundColor Green
        
        # Upload do APK
        Write-Host "Fazendo upload do APK..." -ForegroundColor Yellow
        
        $uploadCommand = "curl -X POST `"https://uploads.github.com/repos/$repoOwner/$repoName/releases/$releaseId/assets?name=app-release-v2.28.10.apk`" `
            `-H `"Authorization: token $token`" `
            `-H `"Content-Type: application/vnd.android.package-archive`" `
            `--data-binary `@"$apkPath`""
        
        Write-Host "Executando upload: $uploadCommand" -ForegroundColor Gray
        $uploadResponse = cmd /c $uploadCommand 2>&1
        Write-Host "Resposta upload: $uploadResponse" -ForegroundColor Gray
        
        Write-Host ""
        Write-Host "✅ Release v2.28.10 criado com sucesso!" -ForegroundColor Green
        Write-Host "📦 APK uploaded: app-release-v2.28.10.apk" -ForegroundColor Green
        Write-Host "📏 Tamanho: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "URL do Release: https://github.com/$repoOwner/$repoName/releases/tag/$tagName" -ForegroundColor Cyan
    } else {
        Write-Host "❌ Erro ao criar release:" -ForegroundColor Red
        Write-Host $response -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro ao executar curl:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Write-Host ""
Write-Host "Processo concluído!" -ForegroundColor Green