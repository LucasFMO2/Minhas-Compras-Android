# Script para completar a reversão no GitHub
Write-Host "=== Completando Reversão para v2.16.0 ===" -ForegroundColor Cyan

# Configurar Git para não usar pager
$env:GIT_PAGER = ''

# 1. Verificar branch atual
Write-Host "`n1. Verificando branch atual..." -ForegroundColor Yellow
$currentBranch = git rev-parse --abbrev-ref HEAD
Write-Host "Branch atual: $currentBranch" -ForegroundColor Green

# 2. Mudar para main
Write-Host "`n2. Mudando para branch main..." -ForegroundColor Yellow
git checkout main
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Não foi possível mudar para main" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Mudou para main" -ForegroundColor Green

# 3. Fazer merge
Write-Host "`n3. Fazendo merge da branch revert-to-v2.16.0..." -ForegroundColor Yellow
git merge revert-to-v2.16.0 -m "revert: Voltar para versão estável 2.16.0 devido a problemas de instalação"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO: Merge falhou" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Merge concluído" -ForegroundColor Green

# 4. Adicionar documento de análise
Write-Host "`n4. Adicionando documento de análise..." -ForegroundColor Yellow
git add ANALISE_REVERSAO_v2.16.0.md
git commit -m "docs: Adicionar análise da reversão para v2.16.0"
if ($LASTEXITCODE -ne 0) {
    Write-Host "AVISO: Commit do documento pode ter falhado (arquivo já commitado?)" -ForegroundColor Yellow
}
Write-Host "✅ Documento adicionado" -ForegroundColor Green

# 5. Verificar versão
Write-Host "`n5. Verificando versão..." -ForegroundColor Yellow
$versionInfo = Select-String -Path "app\build.gradle.kts" -Pattern "versionCode|versionName"
Write-Host $versionInfo -ForegroundColor Cyan

# 6. Mostrar status
Write-Host "`n6. Status do repositório:" -ForegroundColor Yellow
git status --short

Write-Host "`n=== Próximos Passos ===" -ForegroundColor Cyan
Write-Host "Execute os seguintes comandos para fazer push:" -ForegroundColor Yellow
Write-Host "  git push origin main" -ForegroundColor White
Write-Host "  git push origin revert-to-v2.16.0" -ForegroundColor White

Write-Host "`n✅ Reversão local concluída!" -ForegroundColor Green

