# Script para conectar o repositório à conta GitHub LucasFMO2
# Execute este script para configurar a conexão com o GitHub

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CONFIGURACAO GITHUB - LucasFMO2" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar se o repositório remoto está configurado
Write-Host "Verificando configuração do remote..." -ForegroundColor Yellow
$remoteUrl = git remote get-url origin 2>$null

if ($remoteUrl) {
    Write-Host "[OK] Remote configurado: $remoteUrl" -ForegroundColor Green
} else {
    Write-Host "[AVISO] Remote não encontrado. Configurando..." -ForegroundColor Yellow
    git remote add origin https://github.com/LucasFMO2/Minhas-Compras-Android.git
    Write-Host "[OK] Remote configurado!" -ForegroundColor Green
}

Write-Host ""

# Configurar usuário Git
Write-Host "Configurando usuário Git..." -ForegroundColor Yellow
git config user.name "LucasFMO2"
git config user.email "LucasFMO2@users.noreply.github.com"
Write-Host "[OK] Usuário Git configurado: LucasFMO2" -ForegroundColor Green

Write-Host ""

# Verificar se o repositório existe no GitHub
Write-Host "Verificando se o repositório existe no GitHub..." -ForegroundColor Yellow
Write-Host "URL: https://github.com/LucasFMO2/Minhas-Compras-Android" -ForegroundColor Cyan
Write-Host ""

# Perguntar sobre autenticação
Write-Host "Para fazer push, você precisará de um Personal Access Token (PAT)." -ForegroundColor Yellow
Write-Host "Crie um token em: https://github.com/settings/tokens" -ForegroundColor Cyan
Write-Host ""

$useToken = Read-Host "Deseja configurar um token agora? (S/N)"

if ($useToken -eq "S" -or $useToken -eq "s") {
    Write-Host ""
    Write-Host "Cole seu Personal Access Token abaixo:" -ForegroundColor Yellow
    Write-Host "(O token será oculto enquanto você digita)" -ForegroundColor Gray
    $token = Read-Host "Token" -AsSecureString
    
    $tokenPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($token)
    )
    
    if (-not [string]::IsNullOrWhiteSpace($tokenPlain)) {
        # Configurar URL com token
        $remoteUrlWithToken = "https://$tokenPlain@github.com/LucasFMO2/Minhas-Compras-Android.git"
        git remote set-url origin $remoteUrlWithToken
        Write-Host ""
        Write-Host "[OK] Remote configurado com token!" -ForegroundColor Green
        
        # Testar conexão
        Write-Host ""
        Write-Host "Testando conexão..." -ForegroundColor Yellow
        $testResult = git ls-remote origin 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[OK] Conexão estabelecida com sucesso!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Você pode agora fazer push usando:" -ForegroundColor Cyan
            Write-Host "  git push -u origin main" -ForegroundColor White
        } else {
            Write-Host "[AVISO] Não foi possível conectar ao repositório." -ForegroundColor Yellow
            Write-Host "Possíveis causas:" -ForegroundColor Yellow
            Write-Host "  1. O repositório não existe ainda - Crie em: https://github.com/new" -ForegroundColor White
            Write-Host "  2. Token inválido ou sem permissões adequadas" -ForegroundColor White
            Write-Host "  3. Repositório privado sem acesso" -ForegroundColor White
        }
    } else {
        Write-Host "[ERRO] Token vazio!" -ForegroundColor Red
    }
} else {
    Write-Host ""
    Write-Host "[INFO] Token não configurado. Você pode configurar depois usando:" -ForegroundColor Yellow
    Write-Host "  git remote set-url origin https://SEU_TOKEN@github.com/LucasFMO2/Minhas-Compras-Android.git" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CONFIGURACAO CONCLUIDA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Informações da conta:" -ForegroundColor Cyan
Write-Host "  Usuário: LucasFMO2" -ForegroundColor White
Write-Host "  Repositório: https://github.com/LucasFMO2/Minhas-Compras-Android" -ForegroundColor White
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Cyan
Write-Host "  1. Certifique-se de que o repositório existe no GitHub" -ForegroundColor White
Write-Host "  2. Se não existir, crie em: https://github.com/new" -ForegroundColor White
Write-Host "  3. Gere um Personal Access Token em: https://github.com/settings/tokens" -ForegroundColor White
Write-Host "  4. Configure o token usando este script novamente ou manualmente" -ForegroundColor White
Write-Host ""

Read-Host "Pressione ENTER para sair"

