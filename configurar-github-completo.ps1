# Script completo para configurar conexão com GitHub LucasFMO2
# Gerencia criação de repositório, autenticação e push inicial

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "CONFIGURACAO COMPLETA GITHUB" -ForegroundColor Cyan
Write-Host "Conta: LucasFMO2" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar branch atual
Write-Host "[1/6] Verificando branch atual..." -ForegroundColor Yellow
$currentBranch = git branch --show-current
Write-Host "  Branch atual: $currentBranch" -ForegroundColor White

# 2. Verificar se há commits
Write-Host ""
Write-Host "[2/6] Verificando commits locais..." -ForegroundColor Yellow
$commitCount = (git rev-list --count HEAD 2>$null)
if ($commitCount -gt 0) {
    Write-Host "  Total de commits: $commitCount" -ForegroundColor Green
} else {
    Write-Host "  [AVISO] Nenhum commit encontrado!" -ForegroundColor Yellow
    Write-Host "  Você precisa fazer pelo menos um commit antes do push." -ForegroundColor Yellow
}

# 3. Verificar status do repositório remoto
Write-Host ""
Write-Host "[3/6] Verificando conexão com GitHub..." -ForegroundColor Yellow
$remoteTest = git ls-remote origin 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] Repositório existe e está acessível!" -ForegroundColor Green
    $repoExists = $true
} else {
    $errorMsg = $remoteTest -join "`n"
    
    if ($errorMsg -match "suspended") {
        Write-Host "  [ERRO] Conta suspensa no GitHub!" -ForegroundColor Red
        Write-Host "  Acesse: https://support.github.com para resolver" -ForegroundColor Yellow
        $repoExists = $false
        $accountSuspended = $true
    } elseif ($errorMsg -match "404" -or $errorMsg -match "not found") {
        Write-Host "  [AVISO] Repositório não encontrado. Será criado." -ForegroundColor Yellow
        $repoExists = $false
        $accountSuspended = $false
    } elseif ($errorMsg -match "403" -or $errorMsg -match "authentication") {
        Write-Host "  [AVISO] Problema de autenticação. Configurando..." -ForegroundColor Yellow
        $repoExists = $false
        $accountSuspended = $false
    } else {
        Write-Host "  [AVISO] Erro desconhecido: $errorMsg" -ForegroundColor Yellow
        $repoExists = $false
        $accountSuspended = $false
    }
}

# 4. Configurar autenticação
Write-Host ""
Write-Host "[4/6] Configurando autenticação..." -ForegroundColor Yellow

if ($accountSuspended) {
    Write-Host "  [ERRO] Não é possível continuar com conta suspensa." -ForegroundColor Red
    Write-Host "  Resolva a suspensão primeiro em: https://support.github.com" -ForegroundColor Yellow
    Read-Host "`nPressione ENTER para sair"
    exit 1
}

Write-Host "  Para criar o repositório e fazer push, você precisa de um Personal Access Token." -ForegroundColor Cyan
Write-Host "  Crie um token em: https://github.com/settings/tokens" -ForegroundColor Cyan
Write-Host "  Permissões necessárias: repo (acesso completo aos repositórios)" -ForegroundColor Cyan
Write-Host ""

$useToken = Read-Host "Deseja configurar um token agora? (S/N)"

if ($useToken -ne "S" -and $useToken -ne "s") {
    Write-Host "  [INFO] Token não configurado. Configure depois usando:" -ForegroundColor Yellow
    Write-Host "    git remote set-url origin https://SEU_TOKEN@github.com/LucasFMO2/Minhas-Compras-Android.git" -ForegroundColor White
    Read-Host "`nPressione ENTER para sair"
    exit 0
}

Write-Host ""
Write-Host "  Cole seu Personal Access Token:" -ForegroundColor Yellow
$token = Read-Host "Token" -AsSecureString
$tokenPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($token)
)

if ([string]::IsNullOrWhiteSpace($tokenPlain)) {
    Write-Host "  [ERRO] Token vazio!" -ForegroundColor Red
    Read-Host "`nPressione ENTER para sair"
    exit 1
}

# Configurar remote com token
$remoteUrlWithToken = "https://$tokenPlain@github.com/LucasFMO2/Minhas-Compras-Android.git"
git remote set-url origin $remoteUrlWithToken
Write-Host "  [OK] Remote configurado com token!" -ForegroundColor Green

# 5. Criar repositório se não existir
Write-Host ""
Write-Host "[5/6] Verificando/Criando repositório no GitHub..." -ForegroundColor Yellow

if (-not $repoExists) {
    Write-Host "  Criando repositório via API do GitHub..." -ForegroundColor Cyan
    
    # Preparar dados para criar repositório
    $repoData = @{
        name = "Minhas-Compras-Android"
        description = "Aplicativo Android para gerenciamento de compras"
        private = $false
        auto_init = $false
    } | ConvertTo-Json
    
    # Headers para API
    $headers = @{
        "Authorization" = "token $tokenPlain"
        "Accept" = "application/vnd.github.v3+json"
        "User-Agent" = "PowerShell"
    }
    
    try {
        $createResponse = Invoke-RestMethod -Uri "https://api.github.com/user/repos" `
            -Method Post `
            -Headers $headers `
            -Body $repoData `
            -ContentType "application/json"
        
        Write-Host "  [OK] Repositório criado com sucesso!" -ForegroundColor Green
        Write-Host "  URL: $($createResponse.html_url)" -ForegroundColor Cyan
        $repoExists = $true
    } catch {
        $errorDetails = $_.Exception.Response
        if ($errorDetails.StatusCode -eq 422) {
            Write-Host "  [AVISO] Repositório pode já existir ou nome inválido" -ForegroundColor Yellow
            Write-Host "  Tentando verificar novamente..." -ForegroundColor Yellow
        } elseif ($errorDetails.StatusCode -eq 401) {
            Write-Host "  [ERRO] Token inválido ou sem permissões!" -ForegroundColor Red
            Write-Host "  Verifique se o token tem permissão 'repo'" -ForegroundColor Yellow
        } else {
            Write-Host "  [AVISO] Erro ao criar repositório: $($_.Exception.Message)" -ForegroundColor Yellow
            Write-Host "  Você pode criar manualmente em: https://github.com/new" -ForegroundColor Cyan
        }
        
        # Tentar verificar se o repositório existe agora
        $verifyTest = git ls-remote origin 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  [OK] Repositório está acessível agora!" -ForegroundColor Green
            $repoExists = $true
        }
    }
} else {
    Write-Host "  [OK] Repositório já existe!" -ForegroundColor Green
}

# 6. Fazer push inicial
Write-Host ""
Write-Host "[6/6] Preparando push inicial..." -ForegroundColor Yellow

if (-not $repoExists) {
    Write-Host "  [AVISO] Repositório não está acessível. Crie manualmente em:" -ForegroundColor Yellow
    Write-Host "    https://github.com/new" -ForegroundColor Cyan
    Write-Host "    Nome: Minhas-Compras-Android" -ForegroundColor White
    Read-Host "`nPressione ENTER após criar o repositório para continuar"
    
    # Verificar novamente
    $finalTest = git ls-remote origin 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERRO] Ainda não foi possível conectar ao repositório." -ForegroundColor Red
        Read-Host "`nPressione ENTER para sair"
        exit 1
    }
}

# Verificar se há algo para fazer push
$status = git status --porcelain
$hasChanges = $status -ne ""

if ($hasChanges) {
    Write-Host "  [AVISO] Há alterações não commitadas!" -ForegroundColor Yellow
    $commitChanges = Read-Host "  Deseja fazer commit antes do push? (S/N)"
    
    if ($commitChanges -eq "S" -or $commitChanges -eq "s") {
        Write-Host "  Fazendo commit..." -ForegroundColor Cyan
        git add .
        $commitMessage = Read-Host "  Mensagem do commit (ou Enter para padrão)"
        if ([string]::IsNullOrWhiteSpace($commitMessage)) {
            $commitMessage = "chore: Configurar conexão com GitHub LucasFMO2"
        }
        git commit -m $commitMessage
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  [AVISO] Nenhuma alteração para commitar" -ForegroundColor Yellow
        } else {
            Write-Host "  [OK] Commit realizado!" -ForegroundColor Green
        }
    }
}

# Fazer push
Write-Host ""
Write-Host "  Fazendo push da branch '$currentBranch'..." -ForegroundColor Cyan
git push -u origin $currentBranch

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "  [OK] Push realizado com sucesso!" -ForegroundColor Green
    
    # Perguntar sobre push de outras branches
    $otherBranches = git branch -r --format="%(refname:short)" | Where-Object { $_ -notmatch "origin/$currentBranch" -and $_ -notmatch "HEAD" }
    if ($otherBranches) {
        Write-Host ""
        Write-Host "  Há outras branches locais. Deseja fazer push delas também?" -ForegroundColor Yellow
        $pushOthers = Read-Host "  (S/N)"
        
        if ($pushOthers -eq "S" -or $pushOthers -eq "s") {
            git push -u origin --all
            Write-Host "  [OK] Todas as branches foram enviadas!" -ForegroundColor Green
        }
    }
    
    # Perguntar sobre tags
    $hasTags = (git tag -l).Count -gt 0
    if ($hasTags) {
        Write-Host ""
        Write-Host "  Há tags locais. Deseja fazer push das tags?" -ForegroundColor Yellow
        $pushTags = Read-Host "  (S/N)"
        
        if ($pushTags -eq "S" -or $pushTags -eq "s") {
            git push origin --tags
            Write-Host "  [OK] Todas as tags foram enviadas!" -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "CONFIGURACAO CONCLUIDA COM SUCESSO!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Repositório: https://github.com/LucasFMO2/Minhas-Compras-Android" -ForegroundColor Cyan
    Write-Host "Branch atual: $currentBranch" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "  [ERRO] Falha ao fazer push!" -ForegroundColor Red
    Write-Host "  Verifique:" -ForegroundColor Yellow
    Write-Host "    1. Se o repositório existe no GitHub" -ForegroundColor White
    Write-Host "    2. Se o token tem permissões adequadas" -ForegroundColor White
    Write-Host "    3. Se há commits para fazer push" -ForegroundColor White
}

Write-Host ""
Read-Host "Pressione ENTER para sair"

