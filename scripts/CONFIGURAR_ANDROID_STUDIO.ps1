# Script para configurar automaticamente o Android Studio
Write-Host "Configurando Android Studio automaticamente..." -ForegroundColor Cyan
Write-Host ""

# Localizar arquivo studio64.vmoptions do Android Studio
$studioPaths = @(
    "$env:LOCALAPPDATA\Google\AndroidStudio*\bin\studio64.vmoptions",
    "$env:APPDATA\Google\AndroidStudio*\bin\studio64.vmoptions",
    "$env:ProgramFiles\Android\Android Studio\bin\studio64.vmoptions"
)

$vmOptionsFile = $null
foreach ($path in $studioPaths) {
    $files = Get-ChildItem -Path $path -ErrorAction SilentlyContinue
    if ($files) {
        $vmOptionsFile = $files[0].FullName
        break
    }
}

if ($vmOptionsFile) {
    Write-Host "Arquivo encontrado: $vmOptionsFile" -ForegroundColor Green
    
    # Ler conteudo atual
    $content = Get-Content $vmOptionsFile -ErrorAction SilentlyContinue
    
    # Configuracoes otimizadas
    $optimizedSettings = @(
        "-Xms2048m",
        "-Xmx4096m",
        "-XX:ReservedCodeCacheSize=1024m",
        "-XX:+UseG1GC",
        "-XX:SoftRefLRUPolicyMSPerMB=50",
        "-ea",
        "-Dsun.io.useCanonCaches=false",
        "-Djdk.http.auth.tunneling.disabledSchemes=",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:-OmitStackTraceInFastThrow"
    )
    
    # Verificar se ja esta configurado
    $needsUpdate = $false
    foreach ($setting in $optimizedSettings) {
        $key = $setting.Split('=')[0]
        if (-not ($content -match [regex]::Escape($key))) {
            $needsUpdate = $true
            break
        }
    }
    
    if ($needsUpdate) {
        Write-Host "Atualizando configuracoes..." -ForegroundColor Yellow
        
        # Backup do arquivo original
        $backupFile = "$vmOptionsFile.backup"
        Copy-Item $vmOptionsFile $backupFile -Force
        Write-Host "   Backup criado: $backupFile" -ForegroundColor Gray
        
        # Adicionar configuracoes se nao existirem
        $newContent = $content | Where-Object { $_ -notmatch "^-Xm[sx]" -and $_ -notmatch "^-XX:ReservedCodeCacheSize" -and $_ -notmatch "^-XX:\+UseG1GC" -and $_ -notmatch "^-XX:SoftRefLRUPolicyMSPerMB" }
        $newContent += $optimizedSettings
        
        $newContent | Set-Content $vmOptionsFile -Encoding UTF8
        Write-Host "   Configuracoes aplicadas!" -ForegroundColor Green
    } else {
        Write-Host "   Configuracoes ja estao otimizadas" -ForegroundColor Cyan
    }
} else {
    Write-Host "Arquivo studio64.vmoptions nao encontrado automaticamente" -ForegroundColor Yellow
    Write-Host "   Voce precisara configurar manualmente:" -ForegroundColor White
    Write-Host "   1. Abra Android Studio" -ForegroundColor Gray
    Write-Host "   2. Help -> Edit Custom VM Options" -ForegroundColor Gray
    Write-Host "   3. Adicione as configuracoes do arquivo OTIMIZACOES_APLICADAS.md" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Arquivos de configuracao do projeto atualizados:" -ForegroundColor Cyan
Write-Host "   .idea/compiler.xml - Build heap size: 2048 MB" -ForegroundColor White
Write-Host "   .idea/gradle.xml - Configuracoes do Gradle" -ForegroundColor White
Write-Host "   gradle.properties - Otimizacoes do Gradle" -ForegroundColor White
Write-Host ""
Write-Host "Proximos passos:" -ForegroundColor Yellow
Write-Host "   1. Reinicie o Android Studio para aplicar as mudancas" -ForegroundColor White
Write-Host "   2. File -> Settings -> Build, Execution, Deployment -> Compiler" -ForegroundColor White
Write-Host "      Verifique: Build process heap size = 2048 MB" -ForegroundColor Gray
Write-Host "   3. File -> Settings -> Appearance & Behavior -> System Settings" -ForegroundColor White
Write-Host "      Desabilite: Synchronize files on frame activation" -ForegroundColor Gray
Write-Host ""
