# ⚡ Guia Rápido: Reversão para v2.16.0

## 🚨 **LEITURA OBRIGATÓRIA**
Execute este guia apenas após ler o [`PROCEDIMENTO_REVERSAO_v2.16.0.md`](PROCEDIMENTO_REVERSAO_v2.16.0.md) completo.

---

## 🛡️ **BACKUP OBRIGATÓRIO (NÃO PULE!)**

```bash
# 1. Verificar alterações
git status

# 2. Se houver alterações, faça backup:
git stash push -m "Backup antes de revert v2.16.0"

# 3. Sincronizar
git fetch origin --all
```

---

## 🔄 **DOIS CAMINHOS**

### 🛡️ **CAMINHO 1: SEGURO (RECOMENDADO)**
```bash
# 1. Fazer checkout do main
git checkout main

# 2. Reverter commits desde 2.16.0
git revert --no-edit 2.16.0..HEAD

# 3. Push normal
git push origin main
```

### ⚠️ **CAMINHO 2: DESTRUTIVO (CUIDADO!)**
```bash
# 1. Checkout do main
git checkout main

# 2. Reset para tag 2.16.0
git reset --hard 2.16.0

# 3. Push forçado
git push origin main --force-with-lease
```

---

## ✅ **VERIFICAÇÃO**

```bash
# Verificar estado
git log --oneline -5
git status

# Verificar versão
cat app/build.gradle.kts | grep -E "(versionCode|versionName)"
```

---

## 📞 **EM CASO DE PROBLEMAS**

```bash
# Restaurar backup
git stash pop

# Reset seguro para remoto
git reset --hard origin/main

# Verificar histórico
git reflog --oneline -10
```

---

**⚠️ LEMBRE-SE:** Comunique sua equipe antes de qualquer operação!