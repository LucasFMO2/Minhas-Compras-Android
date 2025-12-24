# Função helper para carregar variáveis de ambiente do arquivo .env
function Load-EnvFile {
    param(
        [string]$EnvFile = ".env"
    )
    
    if (-not (Test-Path $EnvFile)) {
        Write-Warning "Arquivo .env não encontrado em: $EnvFile"
        return $false
    }
    
    Write-Host "Carregando variáveis de ambiente de: $EnvFile" -ForegroundColor Gray
    
    Get-Content $EnvFile | ForEach-Object {
        # Ignorar linhas vazias e comentários
        if ($_ -match '^\s*#|^\s*$') {
            return
        }
        
        # Processar linhas no formato KEY=VALUE
        if ($_ -match '^\s*([^#=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            
            # Remover aspas se existirem
            if ($value -match '^["''](.*)["'']$') {
                $value = $matches[1]
            }
            
            # Definir variável de ambiente
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Set-Variable -Name "env:$key" -Value $value -Scope Global -Force
            
            Write-Host "  [OK] $key configurado" -ForegroundColor DarkGray
        }
    }
    
    Write-Host "Variáveis de ambiente carregadas com sucesso!" -ForegroundColor Green
    return $true
}

# Auto-carregar se o script for executado diretamente
if ($MyInvocation.InvocationName -ne '.') {
    Load-EnvFile
}

