# Plano de Migração do Repositório Git

## Objetivo
Mover o repositório `Minhas-Compras-Android` da organização `roseanerosafmo-sketch` para a conta pessoal `seu-usuario-github`.

## Situação Atual
- **Repositório atual**: `https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android.git`
- **Remote origin**: Configurado para a organização roseanerosafmo-sketch
- **Remote old-origin**: Aponta para `https://github.com/Lucasfmo1/Minhas-Compras-Android.git`
- **Destino**: `https://github.com/seu-usuario-github/Minhas-Compras-Android.git`

## Passos Detalhados

### 1. Fazer Backup do Repositório Atual
```bash
# Criar um backup completo do repositório local
git bundle create minhas-compras-backup.bundle --all
```

### 2. Criar Novo Repositório na Conta Pessoal
1. Acessar GitHub.com e fazer login na conta pessoal `seu-usuario-github`
2. Criar novo repositório chamado `Minhas-Compras-Android`
3. **Importante**: Não inicializar com README, .gitignore ou license (vamos migrar tudo do repositório atual)
4. Configurar como repositório privado ou público conforme necessário

### 3. Configurar Credenciais Git para Conta Pessoal
```bash
# Verificar configuração atual
git config --global user.name
git config --global user.email

# Configurar novas credenciais (substituir com seus dados)
git config --global user.name "Seu Nome Completo"
git config --global user.email "seu-email-pessoal@exemplo.com"

# Limpar credenciais antigas (Windows)
# Abrir Gerenciador de Credenciais do Windows e remover entradas github.com
```

### 4. Atualizar Remote do Repositório Local
```bash
# Remover remote antigo
git remote remove origin

# Adicionar novo remote apontando para sua conta pessoal
git remote add origin https://github.com/seu-usuario-github/Minhas-Compras-Android.git

# Verificar configuração
git remote -v
```

### 5. Fazer Push de Todo o Código para Novo Repositório
```bash
# Fazer push do branch principal
git push -u origin main

# Fazer push de todos os branches
git push --all origin

# Fazer push de todas as tags
git push --tags origin
```

### 6. Verificar Migração Completa
```bash
# Verificar branches no novo repositório
git branch -r

# Verificar tags
git tag -l

# Verificar se tudo está sincronizado
git status
```

### 7. Testar Autenticação e Acesso
```bash
# Testar conexão com o novo repositório
git fetch origin

# Tentar fazer um novo commit e push para testar
echo "Teste de migração" > teste.txt
git add teste.txt
git commit -m "Teste de migração para novo repositório"
git push origin main
git reset HEAD~1  # Remover o teste
```

### 8. Configurações Adicionais

#### Se Usar GitHub Desktop
1. Remover repositório atual do GitHub Desktop
2. Adicionar novo repositório clonando do novo URL
3. Configurar conta correta

#### Se Usar VS Code com Git Integration
1. Atualizar configuração de usuários
2. Limpar cache de credenciais se necessário

#### Se Usar SSH (Recomendado)
```bash
# Gerar nova chave SSH para conta pessoal
ssh-keygen -t ed25519 -C "seu-email-pessoal@exemplo.com"

# Adicionar chave ao ssh-agent
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# Copiar chave pública para GitHub
cat ~/.ssh/id_ed25519.pub

# Configurar remote para usar SSH
git remote set-url origin git@github.com:seu-usuario-github/Minhas-Compras-Android.git
```

### 9. Limpeza e Arquivamento
```bash
# Remover arquivo de teste criado anteriormente
rm teste.txt

# Opcional: remover old-origin se não for mais necessário
git remote remove old-origin

# Opcional: adicionar remote antigo como referência
git remote add old-origin https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android.git
```

## Verificação Final

### Checklist de Verificação
- [ ] Novo repositório criado em `seu-usuario-github/Minhas-Compras-Android`
- [ ] Todos os branches migrados
- [ ] Todas as tags migradas
- [ ] Histórico de commits preservado
- [ ] Configuração Git atualizada
- [ ] Autenticação funcionando
- [ ] Push/pull testados com sucesso
- [ ] Arquivos de configuração atualizados (se necessário)

### Arquivos que Podem Precisar de Atualização
- `README.md` (links para o repositório)
- Scripts de deploy que usam o URL antigo
- Configurações de CI/CD
- Documentação que referencia o repositório antigo

## Considerações Importantes

1. **Permissões**: Certifique-se de que você tem direitos administrativos sobre o repositório atual para transferi-lo
2. **Colaboradores**: Se houver outros colaboradores, eles precisarão atualizar seus remotes
3. **Integrações**: Webhooks, GitHub Actions, e outras integrações precisarão ser reconfiguradas
4. **Issues e PRs**: Se usar transferência de repositório, issues e PRs são migrados automaticamente

## Alternativa: Transferência Direta do Repositório

Se você tiver permissões de administrador no repositório atual:
1. Vá para Settings do repositório
2. Clique em "Transfer ownership"
3. Digite o nome do seu repositório pessoal
4. Confirme a transferência

Esta abordagem migra automaticamente:
- Todo o histórico de commits
- Issues e Pull Requests
- Wiki e Pages
- Releases
- Estrelas e observadores

## Comandos de Emergência

Se algo der errado:
```bash
# Restaurar do backup
git clone minhas-compras-backup.bundle repositorio-restaurado

# Voltar para remote antigo
git remote set-url origin https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android.git