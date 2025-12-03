@echo off
setlocal enabledelayedexpansion
echo === Completando Reversao para v2.16.0 ===
echo.

REM Configurar Git para nao usar pager
set GIT_PAGER=
set PAGER=

echo 1. Mudando para branch main...
git --no-pager checkout main 2>&1
if errorlevel 1 (
    echo ERRO: Nao foi possivel mudar para main
    pause
    exit /b 1
)
echo OK: Mudou para main
echo.

echo 2. Fazendo merge da branch revert-to-v2.16.0...
git --no-pager merge revert-to-v2.16.0 -m "revert: Voltar para versao estavel 2.16.0 devido a problemas de instalacao" 2>&1
if errorlevel 1 (
    echo ERRO: Merge falhou
    pause
    exit /b 1
)
echo OK: Merge concluido
echo.

echo 3. Adicionando documento de analise...
git --no-pager add ANALISE_REVERSAO_v2.16.0.md 2>&1
git --no-pager commit -m "docs: Adicionar analise da reversao para v2.16.0" 2>&1
echo OK: Documento adicionado
echo.

echo 4. Verificando versao...
findstr /C:"versionCode" /C:"versionName" app\build.gradle.kts
echo.

echo 5. Fazendo push para GitHub...
echo    Push para main...
git --no-pager push origin main 2>&1
if errorlevel 1 (
    echo AVISO: Push para main pode ter falhado - verifique manualmente
)
echo.

echo    Push para revert-to-v2.16.0...
git --no-pager push origin revert-to-v2.16.0 2>&1
if errorlevel 1 (
    echo AVISO: Push para revert-to-v2.16.0 pode ter falhado - verifique manualmente
)
echo.

echo === Concluido! ===
echo.
echo Verifique o GitHub para confirmar que o push foi bem-sucedido:
echo https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android
pause

