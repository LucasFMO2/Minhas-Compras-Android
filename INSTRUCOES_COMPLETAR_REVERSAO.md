# 📋 Instruções para Completar a Reversão no GitHub

## ✅ Status Atual

- **Branch Atual**: `revert-to-v2.16.0`
- **Versão**: 2.16.0 (versionCode: 66) ✅
- **Build**: Testado e funcionando ✅
- **APK**: Gerado com sucesso ✅
- **Merge na main**: ⏳ Pendente
- **Push para GitHub**: ⏳ Pendente

## 🚀 Comandos para Executar Manualmente

Como o terminal está apresentando problemas com pagers, execute estes comandos **um por vez** no terminal do Android Studio ou PowerShell:

### Passo 1: Mudar para branch main
```powershell
git checkout main
```

### Passo 2: Fazer merge da branch de reversão
```powershell
git merge revert-to-v2.16.0 -m "revert: Voltar para versão estável 2.16.0 devido a problemas de instalação"
```

### Passo 3: Adicionar documento de análise
```powershell
git add ANALISE_REVERSAO_v2.16.0.md
git commit -m "docs: Adicionar análise da reversão para v2.16.0"
```

### Passo 4: Verificar versão
```powershell
Select-String -Path "app\build.gradle.kts" -Pattern "versionCode|versionName"
```
**Deve mostrar**: `versionCode = 66` e `versionName = "2.16.0"`

### Passo 5: Verificar status
```powershell
git status
```
**Deve mostrar**: "nothing to commit, working tree clean"

### Passo 6: Push para GitHub
```powershell
git push origin main
git push origin revert-to-v2.16.0
```

## ⚠️ Se Encontrar Problemas

### Se o merge falhar com conflitos:
```powershell
# Ver conflitos
git status

# Se houver conflitos, resolva manualmente e depois:
git add .
git commit -m "revert: Voltar para versão estável 2.16.0"
```

### Se o push falhar:
```powershell
# Verificar se está conectado ao remoto
git remote -v

# Se necessário, fazer force push (CUIDADO - apenas se tiver certeza!)
# git push origin main --force
```

## ✅ Verificação Final

Após o push, verifique no GitHub:
1. Acesse: https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android
2. Verifique que a branch `main` está na versão 2.16.0
3. Verifique que o arquivo `app/build.gradle.kts` mostra `versionName = "2.16.0"`

## 📝 Arquivos Criados

- ✅ `ANALISE_REVERSAO_v2.16.0.md` - Análise completa da reversão
- ✅ `completar-reversao.ps1` - Script PowerShell (alternativa)
- ✅ `INSTRUCOES_COMPLETAR_REVERSAO.md` - Este arquivo

---

**Última atualização**: 02/12/2025  
**Status**: Aguardando merge e push manual

