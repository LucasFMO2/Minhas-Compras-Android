# Script para Deploy no GitHub - Minha Lista de Compras
# Execute este script após criar o repositório no GitHub

Write-Host "🚀 Deploy para GitHub - Minha Lista de Compras" -ForegroundColor Green
Write-Host ""

# Solicitar o username do GitHub
$username = Read-Host "Digite seu username do GitHub"

if ([string]::IsNullOrWhiteSpace($username)) {
    Write-Host "❌ Username não pode ser vazio!" -ForegroundColor Red
    exit 1
}

# URL do repositório
$repoUrl = "https://github.com/$username/minha-lista-de-compras.git"

Write-Host "📡 Conectando ao repositório: $repoUrl" -ForegroundColor Yellow

try {
    # Adicionar remote origin
    Write-Host "🔗 Adicionando remote origin..." -ForegroundColor Yellow
    git remote add origin $repoUrl
    
    # Enviar código para o GitHub
    Write-Host "📤 Enviando código para o GitHub..." -ForegroundColor Yellow
    git push -u origin main
    
    # Enviar tags
    Write-Host "🏷️ Enviando tags..." -ForegroundColor Yellow
    git push origin v1.0.0
    
    Write-Host ""
    Write-Host "✅ Deploy concluído com sucesso!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Próximos passos:" -ForegroundColor Cyan
    Write-Host "1. Acesse: https://github.com/$username/minha-lista-de-compras" -ForegroundColor White
    Write-Host "2. Vá em 'Releases' e clique em 'Create a new release'" -ForegroundColor White
    Write-Host "3. Selecione a tag 'v1.0.0'" -ForegroundColor White
    Write-Host "4. Título: '🚀 Minha Lista de Compras v1.0.0'" -ForegroundColor White
    Write-Host "5. Descrição: Cole o conteúdo do arquivo 'docs/CHANGELOG.md'" -ForegroundColor White
    Write-Host "6. Marque 'Set as the latest release' e publique" -ForegroundColor White
    Write-Host ""
    Write-Host "🎉 Seu repositório privado está pronto!" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Erro durante o deploy: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 Soluções possíveis:" -ForegroundColor Yellow
    Write-Host "1. Verifique se o repositório foi criado no GitHub" -ForegroundColor White
    Write-Host "2. Verifique suas credenciais do Git" -ForegroundColor White
    Write-Host "3. Execute: git config --global user.name 'Seu Nome'" -ForegroundColor White
    Write-Host "4. Execute: git config --global user.email 'seu@email.com'" -ForegroundColor White
}
