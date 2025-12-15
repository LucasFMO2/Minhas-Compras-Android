# Comandos Rápidos para Migração

## Pré-requisitos
- Ter acesso administrador ao repositório atual
- Ter conta GitHub pessoal criada
- Git instalado e configurado

## Comandos Essenciais

### 1. Backup do Repositório
```bash
git bundle create minhas-compras-backup.bundle --all
```

### 2. Configurar Nova Conta
```bash
# Substitua com seus dados reais
git config --global user.name "Seu Nome Completo"
git config --global user.email "seu-email-pessoal@exemplo.com"
```

### 3. Atualizar Remote
```bash
# Remover remote antigo
git remote remove origin

# Adicionar novo remote (substitua seu-usuario-github)
git remote add origin https://github.com/seu-usuario-github/Minhas-Compras-Android.git

# Verificar
git remote -v
```

### 4. Migrar Tudo
```bash
# Push do branch principal
git push -u origin main

# Push de todos os branches
git push --all origin

# Push de todas as tags
git push --tags origin
```

### 5. Verificação
```bash
# Verificar branches remotos
git branch -r

# Verificar tags
git tag -l

# Testar conexão
git fetch origin
```

### 6. Limpeza (Opcional)
```bash
# Remover old-origin se não for mais necessário
git remote remove old-origin
```

## Transferência Direta (Alternativa)

Se você for administrador do repositório:

1. Vá para: https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android/settings
2. Role até "Danger Zone"
3. Clique em "Transfer ownership"
4. Digite `seu-usuario-github/Minhas-Compras-Android`
5. Confirme a transferência

## Configurar SSH (Recomendado)

```bash
# Gerar nova chave SSH
ssh-keygen -t ed25519 -C "seu-email-pessoal@exemplo.com"

# Adicionar ao ssh-agent
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# Copiar chave pública
cat ~/.ssh/id_ed25519.pub

# Configurar remote para SSH
git remote set-url origin git@github.com:seu-usuario-github/Minhas-Compras-Android.git
```

## Validação Final

```bash
# Testar commit e push
echo "Teste de migração" > teste.txt
git add teste.txt
git commit -m "Teste de migração"
git push origin main

# Limpar teste
git reset HEAD~1
rm teste.txt
```

## Arquivos para Atualizar

Após a migração, verifique se estes arquivos precisam de atualização:
- `README.md` (links do repositório)
- Scripts de deploy
- Configurações de CI/CD
- Documentação

## Emergência

Se precisar restaurar:
```bash
# Clonar do backup
git clone minhas-compras-backup.bundle repositorio-restaurado

# Voltar para remote antigo
git remote set-url origin https://github.com/roseanerosafmo-sketch/Minhas-Compras-Android.git