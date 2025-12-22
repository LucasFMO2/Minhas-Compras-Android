# Guia para Criar Release Pública v2.17.2 no GitHub

## ⚠️ Problema com Token

O token fornecido retornou erro 403 (conta suspensa), então a release precisa ser criada manualmente via interface web do GitHub.

## 📋 Passos para Criar a Release

### 1. Acesse o GitHub
1. Vá para: https://github.com/guimaraesneura-web/Minhas-Compras-Android
2. Faça login na sua conta do GitHub

### 2. Criar Nova Release
1. Clique na aba **"Releases"** (ou acesse: https://github.com/guimaraesneura-web/Minhas-Compras-Android/releases)
2. Clique no botão **"Draft a new release"** ou **"Create a new release"**

### 3. Preencher Informações da Release

**Tag version:**
- Selecione: `v2.17.2` (a tag já foi criada e enviada)

**Release title:**
```
v2.17.2
```

**Description (copie o conteúdo abaixo):**

```markdown
# Release v2.17.2

## 🎉 Novidades

### ✨ Melhorias na Interface
- **Ícones na Barra de Status**: Adicionados ícones CreditCard na bottom bar para melhor identificação visual dos valores "Total" e "A Pagar"
- Melhorias na responsividade e organização visual da interface

## 📦 Instalação

Baixe o APK e instale no seu dispositivo Android.

## 🔧 Mudanças Técnicas

- Versão revertida para 2.17.2 (versionCode: 68)
- Atualizações nas telas: ListaComprasScreen, SettingsScreen, HistoryScreen
- Melhorias na apresentação de informações na barra inferior

## 📝 Notas

Esta versão inclui melhorias visuais importantes na apresentação dos totais na barra de status, facilitando a identificação rápida dos valores.
```

### 4. Anexar o APK

1. Na seção **"Attach binaries"**, clique em **"Choose your files"**
2. Selecione o arquivo: `app-release-v2.17.2.apk`
   - Localização: `C:\Users\nerdd\Desktop\Minhas-Compras-Android\app-release-v2.17.2.apk`

### 5. Publicar a Release

1. Certifique-se de que **"Set as the latest release"** está marcado (se desejar)
2. **NÃO** marque "Set as a pre-release" (queremos uma release pública)
3. Clique no botão **"Publish release"**

## ✅ Verificação

Após publicar, a release estará disponível em:
https://github.com/guimaraesneura-web/Minhas-Compras-Android/releases/tag/v2.17.2

## 🔑 Sobre o Token

Se você precisar criar releases automaticamente no futuro, será necessário:
1. Gerar um novo token do GitHub com permissões adequadas
2. Ou resolver o problema de suspensão da conta atual

Para gerar um novo token:
- Acesse: https://github.com/settings/tokens
- Clique em "Generate new token (classic)"
- Selecione as permissões: `repo` (para criar releases)
- Copie o token gerado

