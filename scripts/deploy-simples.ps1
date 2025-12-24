# Script Simples para Deploy no GitHub
Write-Host "🚀 Deploy para GitHub - Minha Lista de Compras" -ForegroundColor Green
Write-Host ""

# Configurar Git
Write-Host "⚙️ Configurando Git..." -ForegroundColor Yellow
git config --global user.name "Desenvolvedor Naruto RPG"
git config --global user.email "dev@minhalistadecompras.com"

# Abrir GitHub
Write-Host "🌐 Abrindo GitHub..." -ForegroundColor Yellow
Write-Host "Crie um repositório privado chamado: minha-lista-de-compras" -ForegroundColor Cyan
Start-Process "https://github.com/new"

# Aguardar
Read-Host "Pressione ENTER após criar o repositório"

# Solicitar username
$username = Read-Host "Digite seu username do GitHub"

if ([string]::IsNullOrWhiteSpace($username)) {
    Write-Host "❌ Username não pode ser vazio!" -ForegroundColor Red
    exit 1
}

# Deploy
$repoUrl = "https://github.com/$username/minha-lista-de-compras.git"

Write-Host "📡 Conectando ao repositório..." -ForegroundColor Yellow

try {
    # Remover remote existente se houver
    git remote remove origin 2>$null
    
    # Adicionar remote
    git remote add origin $repoUrl
    
    # Push
    Write-Host "📤 Enviando código..." -ForegroundColor Yellow
    git push -u origin main
    
    Write-Host "🏷️ Enviando tags..." -ForegroundColor Yellow
    git push origin v1.0.0
    
    Write-Host ""
    Write-Host "✅ Deploy concluído!" -ForegroundColor Green
    Write-Host "🔗 Acesse: https://github.com/$username/minha-lista-de-compras" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 Para criar Release:" -ForegroundColor Yellow
    Write-Host "1. Vá em Releases → Create a new release" -ForegroundColor White
    Write-Host "2. Tag: v1.0.0" -ForegroundColor White
    Write-Host "3. Título: 🚀 Minha Lista de Compras v1.0.0" -ForegroundColor White
    Write-Host "4. Descrição: Cole o conteúdo de docs/CHANGELOG.md" -ForegroundColor White
    Write-Host "5. Marque 'Set as the latest release'" -ForegroundColor White
    Write-Host "6. Publish release" -ForegroundColor White
    
} catch {
    Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
}

Read-Host "Pressione ENTER para sair"
