# Instruções para Criar Release no GitHub

## ⚠️ Problema
O link para o APK está dando erro 404 porque a release ainda não foi criada no GitHub. Apenas a tag `v2.10.1` foi criada.

## ✅ Solução - Criar Release Manualmente

### Opção 1: Via Interface Web do GitHub (Mais Fácil)

1. **Acesse a página de criação de release:**
   ```
   https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/new
   ```

2. **Preencha os campos:**
   - **Tag:** Selecione `v2.10.1` (já existe)
   - **Release title:** `Release v2.10.1: Sistema de atualização em tempo real`
   - **Description:** Cole o texto abaixo:

   ```markdown
   ## 🚀 Sistema de Atualização em Tempo Real

   ### ✨ Novidades da v2.10.1:

   - 🚀 **Sistema de atualização em tempo real** - Busca todas as releases do GitHub e encontra a versão mais recente automaticamente
   - ⚡ **Verificação em tempo real** - Cache busting para garantir verificação sempre atualizada
   - 🔧 **Cálculo automático de versionCode** - Suporte automático para versões futuras sem atualização manual
   - 📊 **Melhorias de performance** - Sistema de atualização mais eficiente e confiável

   ### 📦 Mudanças Técnicas:

   - Modificado UpdateManager para buscar todas as releases ao invés de apenas /latest
   - Implementada lógica para encontrar a release mais recente comparando versionCodes
   - Criada lógica automática para calcular versionCode a partir do versionName
   - Adicionado cache busting para garantir verificação em tempo real
   ```

3. **Anexe o APK:**
   - Clique em "Attach binaries by dropping them here or selecting them"
   - Selecione o arquivo: `app-release-v2.10.1.apk` (na raiz do projeto)

4. **Publique:**
   - Clique em "Publish release"

### Opção 2: Via Script PowerShell (Requer Token)

1. **Crie um Personal Access Token no GitHub:**
   - Acesse: https://github.com/settings/tokens
   - Clique em "Generate new token (classic)"
   - Dê um nome (ex: "Release Token")
   - Marque a permissão: `repo` (acesso completo ao repositório)
   - Clique em "Generate token"
   - **Copie o token** (você só verá ele uma vez!)

2. **Configure o token e execute o script:**
   ```powershell
   $env:GITHUB_TOKEN = 'seu_token_aqui'
   .\criar-release-github.ps1
   ```

### Opção 3: Via GitHub CLI (Se Instalado)

```bash
gh release create v2.10.1 \
  --title "Release v2.10.1: Sistema de atualização em tempo real" \
  --notes "## 🚀 Sistema de Atualização em Tempo Real

### ✨ Novidades da v2.10.1:

- 🚀 **Sistema de atualização em tempo real** - Busca todas as releases do GitHub e encontra a versão mais recente automaticamente
- ⚡ **Verificação em tempo real** - Cache busting para garantir verificação sempre atualizada
- 🔧 **Cálculo automático de versionCode** - Suporte automático para versões futuras sem atualização manual
- 📊 **Melhorias de performance** - Sistema de atualização mais eficiente e confiável" \
  app-release-v2.10.1.apk
```

## 📋 Após Criar a Release

Depois de criar a release, o link no README.md funcionará automaticamente:
```
https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v2.10.1/app-release-v2.10.1.apk
```

## ✅ Verificação

Após criar a release, verifique se está funcionando:
- Acesse: https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases
- Você deve ver a release v2.10.1 com o APK anexado
- O link de download deve funcionar

