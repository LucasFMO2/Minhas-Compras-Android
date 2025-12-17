# Script Automatizado de Release - Minhas Compras Android
# Baseado no GUIA_COMPLETO_DEVOPS_ANDROID.md
# Uso: .\script-release-automatico.ps1 -Versao "2.28.11" -Codigo "89" -Titulo "Nova Funcionalidade X"

param(
    [Parameter(Mandatory=$true)]
    [string]$Versao,
    
    [Parameter(Mandatory=$true)]
    [string]$Codigo,
    
    [Parameter(Mandatory=$true)]
    [string]$Titulo,
    
    [Parameter(Mandatory=$false)]
    [string]$Descricao = "",
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipTests,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

# Cores para output
$Colors = @{
    Success = "Green"
    Warning = "Yellow"
    Error = "Red"
    Info = "Cyan"
    Gray = "Gray"
}

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Colors[$Color]
}

function Write-Section {
    param([string]$Title)
    Write-Host "`n" + "="*60 -ForegroundColor $Colors.Info
    Write-Host "  $Title" -ForegroundColor $Colors.Info
    Write-Host "="*60 + "`n" -ForegroundColor $Colors.Info
}

function Test-Command {
    param([string]$Command)
    try {
        $null = Get-Command $Command -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Get-ProjectRoot {
    $currentDir = Get-Location
    while ($currentDir -ne $null) {
        if (Test-Path "$currentDir\app\build.gradle.kts") {
            return $currentDir
        }
        $currentDir = Split-Path $currentDir -Parent
    }
    throw "Não foi possível encontrar o diretório raiz do projeto Android"
}

# Início do script
Write-ColorOutput "🚀 Script Automatizado de Release - Minhas Compras Android" "Info"
Write-ColorOutput "Versão: $Versao | Código: $Codigo | Título: $Titulo" "Gray"

try {
    $projectRoot = Get-ProjectRoot
    Set-Location $projectRoot
    Write-ColorOutput "📁 Diretório do projeto: $projectRoot" "Success"
} catch {
    Write-ColorOutput "❌ Erro: $_" "Error"
    exit 1
}

# Verificação de dependências
Write-Section "🔍 Verificação de Dependências"

$dependencies = @("git", "adb", "keytool")
$missingDeps = @()

foreach ($dep in $dependencies) {
    if (-not (Test-Command $dep)) {
        $missingDeps += $dep
    } else {
        Write-ColorOutput "✅ $dep encontrado" "Success"
    }
}

if ($missingDeps.Count -gt 0) {
    Write-ColorOutput "❌ Dependências faltando: $($missingDeps -join ', ')" "Error"
    Write-ColorOutput "   Instale as dependências faltantes e execute novamente" "Warning"
    exit 1
}

# Verificação do Gradle
if (Test-Path ".\gradlew.bat") {
    Write-ColorOutput "✅ Gradle Wrapper encontrado" "Success"
} else {
    Write-ColorOutput "❌ Gradle Wrapper não encontrado" "Error"
    exit 1
}

# Backup do estado atual
Write-Section "💾 Backup do Estado Atual"

$backupDir = "backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

# Backup do build.gradle.kts
if (Test-Path "app\build.gradle.kts") {
    Copy-Item "app\build.gradle.kts" "$backupDir\build.gradle.kts" -Force
    Write-ColorOutput "✅ Backup de app\build.gradle.kts criado" "Success"
}

# Backup do branch atual
try {
    $currentBranch = git rev-parse --abbrev-ref HEAD
    Write-ColorOutput "📂 Branch atual: $currentBranch" "Info"
} catch {
    Write-ColorOutput "❌ Erro ao obter branch atual" "Error"
    exit 1
}

# Atualização da versão
Write-Section "📝 Atualização da Versão"

$buildGradlePath = "app\build.gradle.kts"
$buildGradleContent = Get-Content $buildGradlePath -Raw

# Atualizar versionCode
$versionCodePattern = '(?m)^\s*versionCode\s*=\s*\d+'
$newVersionCode = "versionCode = $Codigo"
$buildGradleContent = $buildGradleContent -replace $versionCodePattern, $newVersionCode

# Atualizar versionName
$versionNamePattern = '(?m)^\s*versionName\s*=\s*"[^"]*"'
$newVersionName = "versionName = `"$Versao`""
$buildGradleContent = $buildGradleContent -replace $versionNamePattern, $newVersionName

if (-not $DryRun) {
    Set-Content $buildGradlePath $buildGradleContent -NoNewline
    Write-ColorOutput "✅ Versão atualizada para $Versao (código $Codigo)" "Success"
} else {
    Write-ColorOutput "🔍 [DRY RUN] Versão seria atualizada para $Versao (código $Codigo)" "Warning"
}

# Criação do branch de release
Write-Section "🌿 Criação do Branch de Release"

$releaseBranch = "release/v$Versao"
Write-ColorOutput "📂 Criando branch: $releaseBranch" "Info"

try {
    if (-not $DryRun) {
        git checkout -b $releaseBranch
        Write-ColorOutput "✅ Branch $releaseBranch criado com sucesso" "Success"
    } else {
        Write-ColorOutput "🔍 [DRY RUN] Branch $releaseBranch seria criado" "Warning"
    }
} catch {
    Write-ColorOutput "❌ Erro ao criar branch: $_" "Error"
    exit 1
}

# Execução de testes
if (-not $SkipTests) {
    Write-Section "🧪 Execução de Testes"
    
    Write-ColorOutput "🔄 Executando testes unitários..." "Info"
    if (-not $DryRun) {
        $testResult = .\gradlew.bat test
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ Testes unitários aprovados" "Success"
        } else {
            Write-ColorOutput "❌ Testes unitários falharam" "Error"
            Write-ColorOutput "   Execute os testes manualmente para mais detalhes" "Warning"
            exit 1
        }
    } else {
        Write-ColorOutput "🔍 [DRY RUN] Testes unitários seriam executados" "Warning"
    }
    
    Write-ColorOutput "🔄 Executando Lint analysis..." "Info"
    if (-not $DryRun) {
        $lintResult = .\gradlew.bat lint
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ Lint analysis aprovado" "Success"
        } else {
            Write-ColorOutput "⚠️ Lint encontrou warnings (continuando...)" "Warning"
        }
    } else {
        Write-ColorOutput "🔍 [DRY RUN] Lint analysis seria executado" "Warning"
    }
} else {
    Write-ColorOutput "⏭️ Testes pulados (-SkipTests)" "Warning"
}

# Build do APK
Write-Section "🔨 Build do APK de Release"

Write-ColorOutput "🔄 Limpando builds anteriores..." "Info"
if (-not $DryRun) {
    .\gradlew.bat clean
    Write-ColorOutput "✅ Limpeza concluída" "Success"
} else {
    Write-ColorOutput "🔍 [DRY RUN] Limpeza seria executada" "Warning"
}

Write-ColorOutput "🔄 Gerando APK de release..." "Info"
if (-not $DryRun) {
    $buildResult = .\gradlew.bat assembleRelease
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✅ APK gerado com sucesso" "Success"
    } else {
        Write-ColorOutput "❌ Falha no build do APK" "Error"
        exit 1
    }
} else {
    Write-ColorOutput "🔍 [DRY RUN] APK seria gerado" "Warning"
}

# Verificação do APK
Write-Section "✅ Verificação do APK"

$apkPath = "app\build\outputs\apk\release\app-release.apk"
$targetApkName = "app-release-v$Versao.apk"

if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-ColorOutput "✅ APK encontrado: $apkPath" "Success"
    Write-ColorOutput "📏 Tamanho: $([math]::Round($apkSize, 2)) MB" "Info"
    
    if (-not $DryRun) {
        Copy-Item $apkPath $targetApkName -Force
        Write-ColorOutput "✅ APK copiado para: $targetApkName" "Success"
    } else {
        Write-ColorOutput "🔍 [DRY RUN] APK seria copiado para: $targetApkName" "Warning"
    }
} else {
    Write-ColorOutput "❌ APK não encontrado em: $apkPath" "Error"
    exit 1
}

# Verificação de assinatura
Write-ColorOutput "🔐 Verificando assinatura do APK..." "Info"
if (-not $DryRun) {
    try {
        $signCheck = & keytool -printcert -jarfile $apkPath
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ APK corretamente assinado" "Success"
        } else {
            Write-ColorOutput "❌ Problema na assinatura do APK" "Error"
            exit 1
        }
    } catch {
        Write-ColorOutput "❌ Erro ao verificar assinatura: $_" "Error"
        exit 1
    }
} else {
    Write-ColorOutput "🔍 [DRY RUN] Assinatura seria verificada" "Warning"
}

# Criação de notas de release
Write-Section "📝 Criação de Notas de Release"

$releaseNotesPath = "RELEASE_NOTES_v$Versao.md"
$releaseNotesContent = @"
# Release v$Versao - $Titulo

## ✨ Novidades

### 🎯 $Titulo
$Descricao

## 🐛 Correções de Bugs

- ✅ **Correção crítica**: Descrição da correção
- 🔧 **Melhoria de estabilidade**: Descrição da melhoria

## 🚀 Melhorias de Performance

- ⚡ **Inicialização**: Melhorias na performance de inicialização
- 📊 **Memória**: Otimizações no consumo de memória

## 📱 Compatibilidade

- **Android Mínimo**: 7.0 (API 24)
- **Android Recomendado**: 12.0 (API 31) ou superior

## 📥 Instalação

1. Faça download do arquivo `app-release-v$Versao.apk`
2. Permita instalação de fontes desconhecidas nas configurações
3. Toque no arquivo APK e siga as instruções

## 🔗 Links Importantes

- **Repositório**: https://github.com/Lucasfmo1/Minhas-Compras-Android
- **Issues**: Reporte problemas em: https://github.com/Lucasfmo1/Minhas-Compras-Android/issues

---

**⭐ Se o app está ajudando você, considere dar uma estrela no repositório!**
"@

if (-not $DryRun) {
    Set-Content $releaseNotesPath $releaseNotesContent -Encoding UTF8
    Write-ColorOutput "✅ Notas de release criadas: $releaseNotesPath" "Success"
} else {
    Write-ColorOutput "🔍 [DRY RUN] Notas de release seriam criadas: $releaseNotesPath" "Warning"
}

# Commit das mudanças
Write-Section "📝 Commit das Mudanças"

if (-not $DryRun) {
    try {
        git add app\build.gradle.kts
        git add $releaseNotesPath
        git add $targetApkName
        git commit -m "Release v$Versao - $Titulo

- Versão: $Versao
- Código: $Codigo
- APK: $targetApkName"
        Write-ColorOutput "✅ Mudanças commitadas" "Success"
    } catch {
        Write-ColorOutput "❌ Erro no commit: $_" "Error"
        exit 1
    }
} else {
    Write-ColorOutput "🔍 [DRY RUN] Mudanças seriam commitadas" "Warning"
}

# Criação da tag
Write-Section "🏷️ Criação da Tag"

$tagName = "v$Versao"
if (-not $DryRun) {
    try {
        git tag -a $tagName -m "Release v$Versao - $Titulo"
        Write-ColorOutput "✅ Tag $tagName criada" "Success"
    } catch {
        Write-ColorOutput "❌ Erro ao criar tag: $_" "Error"
        exit 1
    }
} else {
    Write-ColorOutput "🔍 [DRY RUN] Tag $tagName seria criada" "Warning"
}

# Resumo do processo
Write-Section "📋 Resumo do Processo"

Write-ColorOutput "📊 Informações do Release:" "Info"
Write-ColorOutput "   Versão: $Versao" "Gray"
Write-ColorOutput "   Código: $Codigo" "Gray"
Write-ColorOutput "   Título: $Titulo" "Gray"
Write-ColorOutput "   Branch: $releaseBranch" "Gray"
Write-ColorOutput "   Tag: $tagName" "Gray"
Write-ColorOutput "   APK: $targetApkName" "Gray"
Write-ColorOutput "   Notas: $releaseNotesPath" "Gray"

if (-not $DryRun) {
    Write-ColorOutput "`n✅ Processo de release concluído com sucesso!" "Success"
    Write-ColorOutput "`n🚀 Próximos passos manuais:" "Info"
    Write-ColorOutput "1. Push do branch: git push origin $releaseBranch" "Gray"
    Write-ColorOutput "2. Push da tag: git push origin $tagName" "Gray"
    Write-ColorOutput "3. Criar release no GitHub:" "Gray"
    Write-ColorOutput "   - Acesse: https://github.com/Lucasfmo1/Minhas-Compras-Android/releases/new" "Gray"
    Write-ColorOutput "   - Selecione a tag: $tagName" "Gray"
    Write-ColorOutput "   - Anexe o APK: $targetApkName" "Gray"
    Write-ColorOutput "   - Use as notas: $releaseNotesPath" "Gray"
} else {
    Write-ColorOutput "`n🔍 [DRY RUN] Processo simulado concluído" "Warning"
    Write-ColorOutput "   Execute sem -DryRun para realizar o release real" "Gray"
}

Write-ColorOutput "`n🎉 Script concluído!" "Success"