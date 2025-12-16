# Script para criar repositório no GitHub via API
Write-Host "🚀 Criando repositório no GitHub via API..." -ForegroundColor Green

# Solicitar token do GitHub
Write-Host ""
Write-Host "📋 Para criar o repositório, você precisa de um Personal Access Token do GitHub:" -ForegroundColor Yellow
Write-Host "1. Acesse: https://github.com/settings/tokens" -ForegroundColor White
Write-Host "2. Clique em 'Generate new token' → 'Generate new token (classic)'" -ForegroundColor White
Write-Host "3. Dê um nome: 'Minha Lista de Compras'" -ForegroundColor White
Write-Host "4. Selecione scopes: 'repo' (Full control of private repositories)" -ForegroundColor White
Write-Host "5. Clique em 'Generate token'" -ForegroundColor White
Write-Host "6. Copie o token gerado" -ForegroundColor White
Write-Host ""

$token = Read-Host "Cole seu Personal Access Token aqui"

if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host "❌ Token não pode ser vazio!" -ForegroundColor Red
    exit 1
}

# Configurações do repositório
$repoName = "minha-lista-de-compras"
$description = "Aplicativo Android para gerenciar lista de compras"
$isPrivate = $true

# Headers para a API
$headers = @{
    "Authorization" = "token $token"
    "Accept" = "application/vnd.github.v3+json"
    "User-Agent" = "PowerShell-Script"
}

# Dados do repositório
$repoData = @{
    name = $repoName
    description = $description
    private = $isPrivate
    auto_init = $false
} | ConvertTo-Json

Write-Host "📡 Criando repositório '$repoName'..." -ForegroundColor Yellow

try {
    # Criar repositório via API
    $response = Invoke-RestMethod -Uri "https://api.github.com/user/repos" -Method POST -Headers $headers -Body $repoData -ContentType "application/json"
    
    Write-Host "✅ Repositório criado com sucesso!" -ForegroundColor Green
    Write-Host "🔗 URL: $($response.html_url)" -ForegroundColor Cyan
    
    # Agora fazer o deploy
    Write-Host ""
    Write-Host "📤 Fazendo deploy do código..." -ForegroundColor Yellow
    
    # Configurar Git
    git config --global user.name "Desenvolvedor Naruto RPG"
    git config --global user.email "dev@minhalistadecompras.com"
    
    # Adicionar remote
    git remote add origin $response.clone_url
    
    # Push do código
    git push -u origin main
    
    # Push das tags
    git push origin v1.0.0
    
    Write-Host ""
    Write-Host "🎉 Deploy concluído com sucesso!" -ForegroundColor Green
    Write-Host "🔗 Acesse: $($response.html_url)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 Próximos passos:" -ForegroundColor Yellow
    Write-Host "1. Vá em 'Releases' → 'Create a new release'" -ForegroundColor White
    Write-Host "2. Tag: v1.0.0" -ForegroundColor White
    Write-Host "3. Título: 🚀 Minha Lista de Compras v1.0.0" -ForegroundColor White
    Write-Host "4. Descrição: Cole o conteúdo de docs/CHANGELOG.md" -ForegroundColor White
    Write-Host "5. Marque 'Set as the latest release' e publique" -ForegroundColor White
    
} catch {
    Write-Host "❌ Erro ao criar repositório: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "🔧 Token inválido ou expirado. Gere um novo token." -ForegroundColor Yellow
    } elseif ($_.Exception.Response.StatusCode -eq 422) {
        Write-Host "🔧 Repositório já existe ou nome inválido." -ForegroundColor Yellow
    }
}

Write-Host ""
Read-Host "Pressione ENTER para sair"
