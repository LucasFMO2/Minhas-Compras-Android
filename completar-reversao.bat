@echo off
echo === Completando Reversao para v2.16.0 ===
echo.

echo 1. Mudando para branch main...
git checkout main
if errorlevel 1 (
    echo ERRO: Nao foi possivel mudar para main
    pause
    exit /b 1
)
echo OK: Mudou para main
echo.

echo 2. Fazendo merge da branch revert-to-v2.16.0...
git merge revert-to-v2.16.0 -m "revert: Voltar para versao estavel 2.16.0 devido a problemas de instalacao"
if errorlevel 1 (
    echo ERRO: Merge falhou
    pause
    exit /b 1
)
echo OK: Merge concluido
echo.

echo 3. Adicionando documento de analise...
git add ANALISE_REVERSAO_v2.16.0.md
git commit -m "docs: Adicionar analise da reversao para v2.16.0"
echo OK: Documento adicionado
echo.

echo 4. Verificando versao...
findstr /C:"versionCode" /C:"versionName" app\build.gradle.kts
echo.

echo 5. Fazendo push para GitHub...
echo    Push para main...
git push origin main
if errorlevel 1 (
    echo AVISO: Push para main pode ter falhado
)
echo.

echo    Push para revert-to-v2.16.0...
git push origin revert-to-v2.16.0
if errorlevel 1 (
    echo AVISO: Push para revert-to-v2.16.0 pode ter falhado
)
echo.

echo === Concluido! ===
pause

