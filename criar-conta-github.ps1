# Script PowerShell para criar conta no GitHub
# Este script instala as dependencias e executa a automacao

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AUTOMACAO DE CRIACAO DE CONTA GITHUB" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se Python esta instalado
Write-Host "Verificando instalacao do Python..." -ForegroundColor Yellow
try {
    $pythonVersion = python --version 2>&1
    Write-Host "[OK] Python encontrado: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Python nao encontrado!" -ForegroundColor Red
    Write-Host "Por favor, instale o Python de: https://www.python.org/downloads/" -ForegroundColor Yellow
    exit 1
}

# Verificar se pip esta instalado
Write-Host "Verificando instalacao do pip..." -ForegroundColor Yellow
try {
    $pipVersion = pip --version 2>&1
    Write-Host "[OK] pip encontrado: $pipVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] pip nao encontrado!" -ForegroundColor Red
    exit 1
}

# Instalar dependencias
Write-Host ""
Write-Host "Instalando dependencias..." -ForegroundColor Yellow
pip install -r requirements-github-automation.txt

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Erro ao instalar dependencias!" -ForegroundColor Red
    Write-Host "Tente instalar manualmente:" -ForegroundColor Yellow
    Write-Host "  pip install selenium webdriver-manager" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Dependencias instaladas com sucesso!" -ForegroundColor Green
Write-Host ""

# Verificar se o Chrome esta instalado
Write-Host "Verificando instalacao do Google Chrome..." -ForegroundColor Yellow
$chromePath = Get-Command chrome -ErrorAction SilentlyContinue
if (-not $chromePath) {
    $chromePath = Test-Path "C:\Program Files\Google\Chrome\Application\chrome.exe"
    if (-not $chromePath) {
        $chromePath = Test-Path "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
    }
}

if ($chromePath) {
    Write-Host "[OK] Chrome encontrado!" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Chrome nao encontrado!" -ForegroundColor Yellow
    Write-Host "Por favor, instale o Google Chrome de: https://www.google.com/chrome/" -ForegroundColor Yellow
    Write-Host "Ou o script tentara usar o ChromeDriver automaticamente." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Iniciando automacao..." -ForegroundColor Cyan
Write-Host ""

# Executar o script Python
python criar-conta-github.py

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERRO] Erro ao executar o script!" -ForegroundColor Red
    Write-Host "Verifique se todas as dependencias foram instaladas corretamente." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "[OK] Processo concluido!" -ForegroundColor Green
