# Script para fazer push do código para a nova conta do GitHub
# Execute este script após resolver a suspensão da conta ou obter um token de acesso

Write-Host "🚀 Fazendo push do código para o novo repositório..." -ForegroundColor Cyan
Write-Host ""

# Verificar se há um token de acesso pessoal
$useToken = Read-Host "Deseja usar um Personal Access Token? (S/N)"
if ($useToken -eq "S" -or $useToken -eq "s") {
    $token = Read-Host "Cole seu Personal Access Token aqui" -AsSecureString
    $tokenPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($token)
    )
    
    if (-not [string]::IsNullOrWhiteSpace($tokenPlain)) {
        # Configurar URL com token
        $remoteUrl = "https://$tokenPlain@github.com/roseanerosafmo-sketch/Minhas-Compras-Android.git"
        git remote set-url origin $remoteUrl
        Write-Host "✅ Remote configurado com token" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "📤 Fazendo push do código..." -ForegroundColor Yellow

# Fazer push da branch main
git push -u origin main

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Push da branch main concluído!" -ForegroundColor Green
    
    # Fazer push das tags
    Write-Host ""
    Write-Host "📦 Enviando tags..." -ForegroundColor Yellow
    git push origin --tags
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Todas as tags foram enviadas!" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "🎉 Deploy concluído com sucesso!" -ForegroundColor Green
    Write-Host "🔗 Repositório: https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "❌ Erro ao fazer push" -ForegroundColor Red
    Write-Host ""
    Write-Host "Possíveis causas:" -ForegroundColor Yellow
    Write-Host "1. Conta ainda está suspensa - Resolva em https://support.github.com" -ForegroundColor White
    Write-Host "2. Token inválido ou expirado - Gere um novo em https://github.com/settings/tokens" -ForegroundColor White
    Write-Host "3. Repositório não existe - Crie em https://github.com/new" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 Dica: Se a conta estiver suspensa, você precisará:" -ForegroundColor Cyan
    Write-Host "   - Acessar https://support.github.com" -ForegroundColor White
    Write-Host "   - Entrar em contato com o suporte" -ForegroundColor White
    Write-Host "   - Aguardar a reativação da conta" -ForegroundColor White
}

Write-Host ""
Read-Host "Pressione ENTER para sair"

