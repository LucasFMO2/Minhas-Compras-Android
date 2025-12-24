# Como Coletar Logs de Debug para Task 11

## Método 1: Via Arquivo do Dispositivo (Recomendado)

1. **Conectar dispositivo/emulador:**
   ```bash
   adb devices
   ```

2. **Copiar arquivo de log:**
   ```bash
   adb pull /storage/emulated/0/Android/data/com.example.minhascompras/files/debug.log .cursor/debug.log
   ```

## Método 2: Via Logcat (Alternativo)

1. **Capturar logs do logcat:**
   ```bash
   adb logcat -d -s DebugLogger:* > .cursor/debug.logcat.txt
   ```

2. **Ou em tempo real enquanto reproduz:**
   ```bash
   adb logcat -s DebugLogger:* > .cursor/debug.logcat.txt
   ```
   (Pressione Ctrl+C após reproduzir o issue)

## Método 3: Script Automatizado

Execute o script PowerShell:
```powershell
.\capture-debug-logs.ps1
```

## Localização dos Logs no Dispositivo

- **Caminho completo:** `/storage/emulated/0/Android/data/com.example.minhascompras/files/debug.log`
- **Alternativa:** Use Android Studio → Device File Explorer → Navegar até o caminho acima

## Verificar se o App Está Gerando Logs

Execute o app e verifique no logcat:
```bash
adb logcat | grep DebugLogger
```

Você deve ver linhas como:
```
DebugLogger: [A] ShoppingListViewModel.kt:init: init started - ...
```

## Após Coletar os Logs

Após copiar o arquivo `.cursor/debug.log`, informe que os logs foram coletados para análise.

