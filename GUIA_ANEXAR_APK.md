# 📎 Guia Detalhado: Como Anexar o APK na Release do GitHub

## 🎯 Passo a Passo Visual

### 1️⃣ Acesse a página de criação de release
**URL:** https://github.com/Lucasfmo1/Minhas-Compras-Android/releases/new**

### 2️⃣ Preencha os campos básicos

- **Tag:** Selecione `v2.10.8` no dropdown (ou digite `v2.10.8`)
- **Título:** `Release v2.10.8`
- **Descrição:** Copie e cole o conteúdo do arquivo `RELEASE_NOTES_v2.10.8.md`

### 3️⃣ Anexar o APK - 3 FORMAS DIFERENTES

#### ✅ **Opção 1: Arrastar e Soltar (Drag & Drop)**
1. Abra o Windows Explorer (pasta do projeto)
2. Localize o arquivo `app-release-v2.10.8.apk`
3. **Arraste** o arquivo para a área que diz:
   ```
   Attach binaries by dropping them here or selecting them
   ```
4. Solte o arquivo quando aparecer uma área destacada

#### ✅ **Opção 2: Clicar e Selecionar**
1. Na página da release, procure pela área que diz:
   ```
   Attach binaries by dropping them here or selecting them
   ```
2. Clique em **"selecting them"** (ou na área toda)
3. Uma janela de seleção de arquivos abrirá
4. Navegue até: `C:\Users\nerdd\AndroidStudioProjects\minhascompras2\`
5. Selecione o arquivo `app-release-v2.10.8.apk`
6. Clique em **"Abrir"** ou **"Open"**

#### ✅ **Opção 3: Usar o botão de upload**
1. Procure por um botão ou link que diz **"Upload"** ou **"Choose files"**
2. Clique nele
3. Selecione o arquivo `app-release-v2.10.8.apk`
4. Confirme

### 4️⃣ Verificar se o APK foi anexado

Após anexar, você verá:
- O nome do arquivo aparecendo na lista de anexos
- O tamanho do arquivo (~6.7 MB)
- Uma opção para remover (se quiser trocar)

### 5️⃣ Publicar a Release

1. Role até o final da página
2. Clique no botão verde **"Publish release"**
3. Aguarde alguns segundos
4. Você será redirecionado para a página da release criada

## 📍 Localização do Arquivo APK

**Caminho completo:**
```
C:\Users\nerdd\AndroidStudioProjects\minhascompras2\app-release-v2.10.8.apk
```

## 🔍 Dicas

- Se não conseguir arrastar, tente a **Opção 2** (clicar e selecionar)
- Certifique-se de que o arquivo não está aberto em outro programa
- O arquivo deve ter aproximadamente **6.7 MB**
- Se aparecer algum erro, tente fazer upload novamente

## ✅ Após Publicar

A release estará disponível em:
- https://github.com/Lucasfmo1/Minhas-Compras-Android/releases

E o link de download no README funcionará automaticamente!

