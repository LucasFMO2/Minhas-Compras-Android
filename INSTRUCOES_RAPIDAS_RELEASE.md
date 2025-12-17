# 🚀 Instruções Rápidas de Release - Minhas Compras

## 📋 Resumo do Processo Completo

Este documento resume os passos essenciais do [`GUIA_COMPLETO_DEVOPS_ANDROID.md`](GUIA_COMPLETO_DEVOPS_ANDROID.md) para lançamentos rápidos.

---

## ⚡ Script Automatizado (Recomendado)

### Uso Básico
```powershell
# Release completo
.\script-release-automatico.ps1 -Versao "2.28.11" -Codigo "89" -Titulo "Nova Funcionalidade X"

# Simulação (Dry Run)
.\script-release-automatico.ps1 -Versao "2.28.11" -Codigo "89" -Titulo "Nova Funcionalidade X" -DryRun

# Pular testes (apenas para emergências)
.\script-release-automatico.ps1 -Versao "2.28.11" -Codigo "89" -Titulo "Hotfix Crítico" -SkipTests
```

### O que o script faz automaticamente:
- ✅ Atualiza versão no `build.gradle.kts`
- ✅ Cria branch de release
- ✅ Executa testes e lint
- ✅ Gera APK assinado
- ✅ Verifica integridade do APK
- ✅ Cria notas de release
- ✅ Faz commit das mudanças
- ✅ Cria tag Git

---

## 🛠️ Processo Manual (Passo a Passo)

### 1️⃣ Preparação
```bash
# Atualizar versão em app/build.gradle.kts
versionCode = 89
versionName = "2.28.11"

# Criar branch de release
git checkout -b release/v2.28.11
```

### 2️⃣ Build e Testes
```bash
# Limpar e buildar
./gradlew clean
./gradlew test
./gradlew lint
./gradlew assembleRelease

# Verificar APK
ls -lh app/build/outputs/apk/release/app-release.apk
```

### 3️⃣ Preparação dos Arquivos
```bash
# Copiar APK com nome padrão
cp app/build/outputs/apk/release/app-release.apk app-release-v2.28.11.apk

# Criar notas de release (template em RELEASE_NOTES_v2.28.11.md)
# Usar o template do guia completo
```

### 4️⃣ Commit e Tag
```bash
# Adicionar arquivos
git add app/build.gradle.kts
git add app-release-v2.28.11.apk
git add RELEASE_NOTES_v2.28.11.md

# Commit
git commit -m "Release v2.28.11 - Nova Funcionalidade X"

# Criar tag
git tag -a v2.28.11 -m "Release v2.28.11 - Nova Funcionalidade X"
```

### 5️⃣ Publicação
```bash
# Push do branch e tag
git push origin release/v2.28.11
git push origin v2.28.11

# Ou usar GitHub CLI
gh release create v2.28.11 --title "Release v2.28.11" --notes-file RELEASE_NOTES_v2.28.11.md app-release-v2.28.11.apk
```

---

## 📱 Publicação Manual no GitHub

1. Acessar: https://github.com/Lucasfmo1/Minhas-Compras-Android/releases/new
2. Selecionar tag: `v2.28.11`
3. Título: `Release v2.28.11`
4. Descrição: Copiar conteúdo de `RELEASE_NOTES_v2.28.11.md`
5. Anexar APK: `app-release-v2.28.11.apk`
6. Publicar release

---

## 🔍 Validações Essenciais

### Antes de Publicar
- [ ] APK assinado corretamente
- [ ] Tamanho < 15MB
- [ ] Testes passando
- [ ] Notas de release revisadas
- [ ] Branch atualizado

### Após Publicar
- [ ] Download testado
- [ ] Link funcionando
- [ ] Monitoramento ativado
- [ ] Comunicação enviada

---

## 🚨 Emergências - Hotfix

### Fluxo Rápido
```bash
# Branch de hotfix
git checkout -b hotfix/v2.28.11.1

# Atualizar versão
versionCode = 90
versionName = "2.28.11.1"

# Build rápido
./gradlew assembleRelease

# Publicar
cp app/build/outputs/apk/release/app-release.apk app-release-v2.28.11.1.apk
git add .
git commit -m "Hotfix v2.28.11.1 - Correção crítica"
git tag -a v2.28.11.1 -m "Hotfix v2.28.11.1"
git push origin v2.28.11.1
gh release create v2.28.11.1 app-release-v2.28.11.1.apk
```

---

## 📊 Monitoramento Pós-Lançamento

### Ferramentas
- **Firebase Crashlytics**: Já configurado no projeto
- **GitHub Analytics**: Downloads e engajamento
- **Issues**: Feedback dos usuários

### Métricas Chave
- **Taxa de crashes**: < 1%
- **Downloads**: Acompanhar crescimento
- **Issues novas**: Priorizar críticas

### Alertas
```bash
# Verificar releases recentes
gh release list --limit 5

# Monitorar issues
gh issue list --state open --limit 10
```

---

## 🎯 Templates Rápidos

### Notas de Release (Mínimo)
```markdown
# Release v2.28.11 - Título da Versão

## ✨ Novidades
- [Funcionalidade principal]

## 🐛 Correções
- [Bug corrigido]

## 📥 Instalação
1. Baixe o APK: app-release-v2.28.11.apk
2. Instale e aproveite!

---
**⭐ Avalie o app se gostar!**
```

### Comunicação Rápida
```
🚀 Nova versão do Minhas Compras v2.28.11!

✨ [Principal novidade]
🐛 [Correção importante]

Download: [link]

#MinhasCompras #Android
```

---

## 📁 Arquivos Importantes

- `GUIA_COMPLETO_DEVOPS_ANDROID.md` - Guia completo
- `script-release-automatico.ps1` - Script automatizado
- `keystore/release.jks` - Assinatura do APK
- `app/build.gradle.kts` - Configuração de versão

---

## 🔗 Links Úteis

- **Repositório**: https://github.com/Lucasfmo1/Minhas-Compras-Android
- **Releases**: https://github.com/Lucasfmo1/Minhas-Compras-Android/releases
- **Issues**: https://github.com/Lucasfmo1/Minhas-Compras-Android/issues
- **Guia Completo**: [GUIA_COMPLETO_DEVOPS_ANDROID.md](GUIA_COMPLETO_DEVOPS_ANDROID.md)

---

## ⚡ Dicas de Produtividade

### Para Lançamentos Frequentes
1. Use o script automatizado sempre que possível
2. Mantenha o template de notas atualizado
3. Teste em múltiplos dispositivos
4. Monitore métricas após cada release

### Para Economizar Tempo
1. Configure atalhos para comandos frequentes
2. Use GitHub CLI para automação
3. Mantenha dependências atualizadas
4. Documente mudanças durante o desenvolvimento

---

## 🎉 Conclusão

Este guia rápido complementa o documento completo, fornecendo:
- ✅ Processo automatizado via script
- ✅ Passos manuais essenciais
- ✅ Templates para comunicação
- ✅ Fluxo de emergência
- ✅ Monitoramento essencial

Para detalhes completos, sempre consulte o [`GUIA_COMPLETO_DEVOPS_ANDROID.md`](GUIA_COMPLETO_DEVOPS_ANDROID.md).

---

**📞 Suporte**: Abra uma issue para dúvidas ou sugestões.