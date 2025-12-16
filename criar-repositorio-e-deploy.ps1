# Script para Criar Repositório no GitHub e Fazer Deploy
# Minha Lista de Compras

Write-Host "🚀 Criando Repositório no GitHub e Fazendo Deploy" -ForegroundColor Green
Write-Host ""

# Configurar Git se necessário
Write-Host "⚙️ Configurando Git..." -ForegroundColor Yellow
git config --global user.name "Desenvolvedor Naruto RPG"
git config --global user.email "dev@minhalistadecompras.com"

# Abrir GitHub para criar repositório
Write-Host "🌐 Abrindo GitHub para criar repositório..." -ForegroundColor Yellow
Write-Host ""
Write-Host "📋 INSTRUÇÕES:" -ForegroundColor Cyan
Write-Host "1. O GitHub será aberto automaticamente" -ForegroundColor White
Write-Host "2. Clique em 'New repository' ou '+' → 'New repository'" -ForegroundColor White
Write-Host "3. Configure:" -ForegroundColor White
Write-Host "   - Repository name: minha-lista-de-compras" -ForegroundColor Gray
Write-Host "   - Description: Aplicativo Android para gerenciar lista de compras" -ForegroundColor Gray
Write-Host "   - Visibility: ✅ Private (marcar como privado)" -ForegroundColor Gray
Write-Host "   - Initialize: ❌ NÃO marcar nenhuma opção" -ForegroundColor Gray
Write-Host "4. Clique em 'Create repository'" -ForegroundColor White
Write-Host "5. Volte aqui e pressione ENTER para continuar" -ForegroundColor White
Write-Host ""

# Abrir GitHub
Start-Process "https://github.com/new"

# Aguardar usuário criar o repositório
Read-Host "Pressione ENTER após criar o repositório no GitHub"

# Solicitar username do GitHub
$username = Read-Host "Digite seu username do GitHub"

if ([string]::IsNullOrWhiteSpace($username)) {
    Write-Host "❌ Username não pode ser vazio!" -ForegroundColor Red
    exit 1
}

# URL do repositório
$repoUrl = "https://github.com/$username/minha-lista-de-compras.git"

Write-Host ""
Write-Host "📡 Conectando ao repositório: $repoUrl" -ForegroundColor Yellow

try {
    # Verificar se remote já existe
    $existingRemote = git remote get-url origin 2>$null
    if ($existingRemote) {
        Write-Host "🔄 Removendo remote existente..." -ForegroundColor Yellow
        git remote remove origin
    }
    
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
    Write-Host "🎉 Repositório criado e código enviado!" -ForegroundColor Green
    Write-Host "🔗 Acesse: https://github.com/$username/minha-lista-de-compras" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 Próximos passos para Release:" -ForegroundColor Cyan
    Write-Host "1. Acesse: https://github.com/$username/minha-lista-de-compras" -ForegroundColor White
    Write-Host "2. Clique em 'Releases' (lado direito)" -ForegroundColor White
    Write-Host "3. Clique em 'Create a new release'" -ForegroundColor White
    Write-Host "4. Configure:" -ForegroundColor White
    Write-Host "   - Tag version: v1.0.0" -ForegroundColor Gray
    Write-Host "   - Release title: 🚀 Minha Lista de Compras v1.0.0" -ForegroundColor Gray
    Write-Host "   - Description: Cole o conteúdo do arquivo 'docs/CHANGELOG.md'" -ForegroundColor Gray
    Write-Host "   - Set as the latest release: ✅ Marcar" -ForegroundColor Gray
    Write-Host "5. Clique em 'Publish release'" -ForegroundColor White
    Write-Host ""
    Write-Host "🎯 Sua tag v1.0.0 ficará visível na página inicial!" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Erro durante o deploy: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 Soluções possíveis:" -ForegroundColor Yellow
    Write-Host "1. Verifique se o repositório foi criado no GitHub" -ForegroundColor White
    Write-Host "2. Verifique suas credenciais do Git" -ForegroundColor White
    Write-Host "3. Execute: git config --global user.name Seu Nome" -ForegroundColor White
    Write-Host "4. Execute: git config --global user.email seu@email.com" -ForegroundColor White
    Write-Host "5. Verifique se você tem permissão para criar repositórios privados" -ForegroundColor White
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")