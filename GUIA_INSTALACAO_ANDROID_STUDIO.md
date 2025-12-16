# 📱 Guia de Instalação do Android Studio

Este guia fornece instruções completas para instalar ou atualizar o Android Studio para a última versão.

## 🎯 Requisitos do Sistema

### Mínimos
- **RAM**: 4 GB (8 GB recomendado)
- **Espaço em Disco**: 10 GB de espaço livre
- **Resolução**: 1280 x 800 pixels
- **Sistema Operacional**: Windows 10/11 (64-bit)

### Recomendados
- **RAM**: 16 GB ou mais
- **Espaço em Disco**: 20 GB ou mais
- **Processador**: Multi-core com suporte a virtualização

## 📥 Método 1: Download Manual (Recomendado)

### Passo 1: Baixar o Android Studio

1. Acesse o site oficial: https://developer.android.com/studio
2. Clique no botão **"Download Android Studio"**
3. Aceite os termos e condições
4. O download iniciará automaticamente (arquivo `.exe` de ~1 GB)

### Passo 2: Instalar o Android Studio

1. **Execute o instalador** (`android-studio-*.exe`)
2. **Siga o assistente de instalação**:
   - Clique em "Next" na tela de boas-vindas
   - Escolha os componentes (mantenha os padrões recomendados)
   - Escolha o diretório de instalação (ou mantenha o padrão)
   - Clique em "Install"
3. **Aguarde a instalação** (pode levar alguns minutos)
4. **Conclua a instalação** e clique em "Finish"

### Passo 3: Configuração Inicial

1. **Inicie o Android Studio** pela primeira vez
2. **Escolha o tipo de instalação**:
   - Selecione **"Standard"** (recomendado para iniciantes)
   - Ou **"Custom"** se quiser personalizar
3. **Aguarde o download dos componentes**:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device (AVD)
   - Emulador Android
   - Ferramentas de build
   - **Isso pode levar 10-30 minutos dependendo da conexão**
4. **Conclua a configuração** e clique em "Finish"

## 🔄 Método 2: Atualizar Versão Existente

### Opção A: Atualização Automática (Mais Fácil)

1. Abra o Android Studio
2. Vá em **Help → Check for Updates**
3. Se houver atualização disponível:
   - Clique em **"Update and Restart"**
   - O Android Studio será atualizado automaticamente
   - Reinicie quando solicitado

### Opção B: Download Manual da Nova Versão

1. Baixe a nova versão do site oficial
2. Execute o instalador
3. O instalador detectará a instalação existente
4. Escolha **"Update"** ou **"Repair"**
5. Siga as instruções do assistente

## ⚙️ Configuração Pós-Instalação

### 1. Configurar Otimizações do Projeto

Após instalar o Android Studio, execute o script de configuração:

```powershell
.\CONFIGURAR_ANDROID_STUDIO.ps1
```

Este script irá:
- Otimizar as configurações de memória do Android Studio
- Configurar o Gradle para melhor performance
- Ajustar as configurações do compilador

### 2. Verificar Versão Instalada

Para verificar a versão do Android Studio instalada:

1. Abra o Android Studio
2. Vá em **Help → About**
3. A versão será exibida (ex: "Android Studio Hedgehog | 2023.1.1")

### 3. Configurar SDK Manager

1. Abra o Android Studio
2. Vá em **Tools → SDK Manager**
3. Na aba **"SDK Platforms"**, certifique-se de ter:
   - ✅ Android 14.0 (API 34) - **Requerido para este projeto**
   - ✅ Android 13.0 (API 33) - Recomendado
4. Na aba **"SDK Tools"**, certifique-se de ter:
   - ✅ Android SDK Build-Tools
   - ✅ Android SDK Platform-Tools
   - ✅ Android Emulator
   - ✅ Google Play services

### 4. Configurar AVD (Android Virtual Device)

Para testar o app em um emulador:

1. Vá em **Tools → Device Manager**
2. Clique em **"Create Device"**
3. Escolha um dispositivo (ex: Pixel 7)
4. Escolha uma imagem do sistema (ex: Android 14.0)
5. Clique em **"Finish"**

## 🔧 Solução de Problemas

### Problema: "SDK not found"

**Solução:**
1. Abra **File → Settings → Appearance & Behavior → System Settings → Android SDK**
2. Verifique o caminho do SDK (geralmente: `C:\Users\SeuUsuario\AppData\Local\Android\Sdk`)
3. Se necessário, baixe os componentes faltantes via SDK Manager

### Problema: "Gradle sync failed"

**Solução:**
1. Vá em **File → Invalidate Caches / Restart**
2. Escolha **"Invalidate and Restart"**
3. Aguarde a sincronização do Gradle novamente

### Problema: "Emulator not starting"

**Solução:**
1. Verifique se a virtualização está habilitada no BIOS
2. Certifique-se de ter instalado o Android Emulator via SDK Manager
3. Tente criar um novo AVD

### Problema: "Out of memory"

**Solução:**
1. Execute o script `CONFIGURAR_ANDROID_STUDIO.ps1`
2. Ou configure manualmente:
   - **Help → Edit Custom VM Options**
   - Aumente `-Xmx` para `4096m` ou `8192m` (se tiver RAM suficiente)

## 📚 Recursos Adicionais

- **Documentação Oficial**: https://developer.android.com/studio/intro
- **Release Notes**: https://developer.android.com/studio/releases
- **Fórum de Suporte**: https://developer.android.com/studio/intro/studio-config

## ✅ Verificação Final

Após a instalação, verifique se tudo está funcionando:

1. ✅ Android Studio abre sem erros
2. ✅ SDK Manager mostra os componentes instalados
3. ✅ Gradle sincroniza sem erros
4. ✅ Emulador inicia corretamente (se configurado)
5. ✅ Projeto abre e compila sem erros

## 🎉 Próximos Passos

1. Abra este projeto no Android Studio: **File → Open → Selecione a pasta do projeto**
2. Aguarde a sincronização do Gradle
3. Execute o app: **Run → Run 'app'** ou pressione `Shift+F10`

---

**Última atualização**: Dezembro 2024
**Versão do Android Studio recomendada**: Hedgehog (2023.1.1) ou superior
**Versão do AGP usada no projeto**: 8.13.0

