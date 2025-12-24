# Script para capturar logs de debug da Task 11
Write-Host "Capturando logs de debug..." -ForegroundColor Cyan

# Caminho do arquivo de log no dispositivo
$deviceLogPath = "/storage/emulated/0/Android/data/com.example.minhascompras/files/debug.log"
$localLogPath = ".cursor/debug.log"

# Tentar copiar do dispositivo
Write-Host "Tentando copiar log do dispositivo..." -ForegroundColor Yellow
$pullResult = adb pull $deviceLogPath $localLogPath 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Log copiado com sucesso!" -ForegroundColor Green
    Write-Host "Arquivo: $localLogPath" -ForegroundColor Green
} else {
    Write-Host "Não foi possível copiar do dispositivo. Tentando capturar via logcat..." -ForegroundColor Yellow
    
    # Capturar logs do logcat filtrando por DebugLogger
    Write-Host "Capturando últimos logs do logcat (DebugLogger)..." -ForegroundColor Yellow
    $logcatOutput = adb logcat -d -s DebugLogger:* 2>&1
    
    if ($LASTEXITCODE -eq 0 -and $logcatOutput) {
        # Criar diretório se não existir
        New-Item -ItemType Directory -Force -Path ".cursor" | Out-Null
        
        # Converter logs do logcat para formato NDJSON básico
        $ndjsonLines = @()
        $logcatOutput -split "`n" | ForEach-Object {
            if ($_ -match "DebugLogger.*\[([^\]]+)\]\s+([^:]+):\s+(.+?)\s+-\s+(.+)") {
                $hypothesisId = $matches[1]
                $location = $matches[2]
                $message = $matches[3]
                $dataStr = $matches[4]
                
                $ndjsonLines += @{
                    id = "log_$(Get-Date -Format 'yyyyMMddHHmmss')_$(New-Guid).ToString().Substring(0,8)"
                    timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
                    location = $location
                    message = $message
                    data = $dataStr
                    sessionId = "debug-session"
                    runId = "run1"
                    hypothesisId = $hypothesisId
                } | ConvertTo-Json -Compress
            }
        }
        
        if ($ndjsonLines.Count -gt 0) {
            $ndjsonLines | Out-File -FilePath $localLogPath -Encoding UTF8
            Write-Host "Logs capturados via logcat e convertidos!" -ForegroundColor Green
            Write-Host "Total de entradas: $($ndjsonLines.Count)" -ForegroundColor Green
        } else {
            Write-Host "Nenhum log encontrado no logcat. Certifique-se de que o app foi executado." -ForegroundColor Red
        }
    } else {
        Write-Host "Erro ao capturar logs do logcat. Certifique-se de que:" -ForegroundColor Red
        Write-Host "1. O dispositivo/emulador está conectado (adb devices)" -ForegroundColor Red
        Write-Host "2. O app foi executado após a instrumentação" -ForegroundColor Red
        Write-Host "3. Você executou o cenário de teste" -ForegroundColor Red
    }
}

