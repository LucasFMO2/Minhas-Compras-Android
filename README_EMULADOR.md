# Guia de Uso do Emulador Android

Scripts PowerShell para facilitar o desenvolvimento e teste do app no emulador Android.

## Scripts Disponíveis

### 1. `rodar-emulador.ps1` - Script Principal

Script completo que:
- Verifica se há emulador rodando
- Inicia um emulador se necessário
- Compila e instala o app
- Inicia o app automaticamente

**Uso básico:**
```powershell
.\rodar-emulador.ps1
```

**Opções:**
```powershell
# Escolher um AVD específico
.\rodar-emulador.ps1 -AvdName "Pixel_5_API_33"

# Mostrar logs após iniciar
.\rodar-emulador.ps1 -Logs

# Não recompilar (apenas reinstalar)
.\rodar-emulador.ps1 -Rebuild:$false

# Combinar opções
.\rodar-emulador.ps1 -AvdName "Pixel_5_API_33" -Logs

# Ver ajuda
.\rodar-emulador.ps1 -Help
```

### 2. `reinstalar-app.ps1` - Reinstalação Rápida

Reinstala o app rapidamente após fazer mudanças no código (sem rebuild completo).

**Uso:**
```powershell
.\reinstalar-app.ps1
```

**Quando usar:**
- Após fazer mudanças no código
- Quando o app já está compilado e só precisa ser reinstalado
- Para reiniciar o app rapidamente

### 3. `ver-logs.ps1` - Visualizar Logs

Mostra os logs do app em tempo real.

**Uso:**
```powershell
# Logs do app (padrão)
.\ver-logs.ps1

# Logs com filtro personalizado
.\ver-logs.ps1 -Filter "erro"
```

## Workflow Recomendado

### Primeira vez / Iniciar sessão de desenvolvimento:

```powershell
# 1. Iniciar emulador e rodar o app
.\rodar-emulador.ps1
```

### Durante o desenvolvimento:

```powershell
# 2. Após fazer mudanças no código, reinstalar rapidamente
.\reinstalar-app.ps1

# 3. Se precisar ver logs para debug
.\ver-logs.ps1
```

## Comandos ADB Úteis

Se precisar usar comandos ADB diretamente:

```powershell
# Ver dispositivos conectados
adb devices

# Desinstalar o app
adb uninstall com.example.minhascompras

# Limpar dados do app (reset completo)
adb shell pm clear com.example.minhascompras

# Reiniciar o app
adb shell am force-stop com.example.minhascompras
adb shell am start -n com.example.minhascompras/.MainActivity

# Tirar screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png screenshot.png

# Ver informações do dispositivo
adb shell getprop ro.build.version.release
adb shell getprop ro.product.model
```

## Solução de Problemas

### Emulador não inicia
- Verifique se há AVDs criados no Android Studio
- Certifique-se de que o caminho do SDK está correto em `local.properties`
- Tente iniciar manualmente pelo Android Studio primeiro

### App não instala
- Verifique se o emulador está totalmente inicializado (aguarde alguns segundos)
- Tente desinstalar o app primeiro: `adb uninstall com.example.minhascompras`
- Verifique se há erros de compilação: `.\gradlew.bat assembleDebug`

### Logs não aparecem
- Certifique-se de que o app está rodando
- Tente limpar os logs: `adb logcat -c` e depois `.\ver-logs.ps1`

## Configuração

Os scripts usam automaticamente:
- SDK Path: `C:\Users\nerdd\AppData\Local\Android\Sdk` (de `local.properties`)
- Package: `com.example.minhascompras`
- Activity: `com.example.minhascompras/.MainActivity`

Se precisar alterar, edite os scripts diretamente.

