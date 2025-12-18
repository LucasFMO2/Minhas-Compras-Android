# Guia Prático de Reversão para v2.22.0

## 🚀 Comandos Prontos para Execução

### Etapa 1: Backup e Preparação
```bash
# 1.1 Backup completo do projeto
cd /c/Users/nerdd/Desktop/
cp -r Minhas-Compras-Android Minhas-Compras-Android-backup-$(date +%Y%m%d-%H%M%S)

# 1.2 Entrar no diretório do projeto
cd Minhas-Compras-Android

# 1.3 Verificar estado atual do Git
git status
git log --oneline -5

# 1.4 Listar todas as tags disponíveis
git tag --list | sort -V | tail -10
```

### Etapa 2: Identificação da Versão 2.22.0
```bash
# 2.1 Procurar por tag v2.22.0
git tag --list | grep "v2.22.0"

# 2.2 Se não encontrar tag, procurar commits com "2.22.0"
git log --oneline --grep="2.22.0" --all

# 2.3 Procurar commits próximos à data do release (09/12/2025)
git log --oneline --since="2025-12-01" --until="2025-12-15" --all

# 2.4 Verificar informações do commit/tag encontrado
git show v2.22.0  # ou git show <hash-do-commit>
```

### Etapa 3: Reversão do Código
```bash
# 3.1 Salvar alterações não commitadas (se houver)
git stash push -m "Alterações antes da reversão para v2.22.0"

# 3.2 Limpar working directory
git clean -fd
git reset --hard HEAD

# 3.3 Fazer checkout da versão 2.22.0
# OPÇÃO A: Se existir tag
git checkout v2.22.0

# OPÇÃO B: Se não houver tag (substituir <hash> pelo commit correto)
git checkout <hash-do-commit-da-v2.22.0>

# 3.4 Criar branch para trabalhar
git checkout -b revert-to-v2.22.0

# 3.5 Verificar se está na versão correta
git log --oneline -1
git status
```

### Etapa 4: Ajuste das Configurações
```bash
# 4.1 Verificar configurações atuais
cat app/build.gradle.kts | grep -A 5 -B 5 "versionCode\|versionName"

# 4.2 Editar arquivo de configurações
notepad++ app/build.gradle.kts  # ou usar seu editor preferido

# 4.3 Verificar se as configurações estão corretas
grep -n "versionCode\|versionName" app/build.gradle.kts
# Deve mostrar:
# versionCode = 72
# versionName = "2.22.0"
```

### Etapa 5: Verificação de Dependências
```bash
# 5.1 Verificar dependências críticas
grep -n "vico\|firebase\|room" app/build.gradle.kts

# 5.2 Verificar se Vico Charts está presente (essencial para v2.22.0)
grep -A 3 -B 1 "vico" app/build.gradle.kts

# 5.3 Verificar versão do Firebase
grep -A 1 -B 1 "firebase-bom" app/build.gradle.kts
```

### Etapa 6: Build e Testes
```bash
# 6.1 Limpar build anterior
./gradlew clean

# 6.2 Build de debug
./gradlew assembleDebug

# 6.3 Verificar se APK foi gerado
ls -la app/build/outputs/apk/debug/

# 6.4 Build de release (se necessário)
./gradlew assembleRelease

# 6.5 Verificar APK de release
ls -la app/build/outputs/apk/release/
```

### Etapa 7: Validação
```bash
# 7.1 Verificar informações do APK gerado
./gradlew app:info

# 7.2 Comparar com APK original (se disponível)
# Comparar tamanho:
ls -lh app-release-v2.22.0.apk
ls -lh app/build/outputs/apk/release/app-release.apk

# 7.3 Rodar testes unitários
./gradlew test

# 7.4 Rodar testes instrumentados (se tiver emulador/dispositivo)
./gradlew connectedAndroidTest
```

## 🔧 Scripts Automatizados

### Script de Backup Completo
```bash
#!/bin/bash
# backup-completo.sh
DATA=$(date +%Y%m%d-%H%M%S)
BACKUP_DIR="/c/Users/nerdd/Desktop/Minhas-Compras-Android-backup-$DATA"

echo "Criando backup em: $BACKUP_DIR"
cp -r /c/Users/nerdd/Desktop/Minhas-Compras-Android "$BACKUP_DIR"

# Backup do banco de dados se houver
if [ -d "app/src/main/assets/databases" ]; then
    cp -r app/src/main/assets/databases "$BACKUP_DIR/backup-databases-$DATA"
fi

echo "Backup concluído com sucesso!"
```

### Script de Verificação de Versão
```bash
#!/bin/bash
# verificar-versao.sh
echo "=== Verificando versão atual ==="
grep -n "versionCode\|versionName" app/build.gradle.kts

echo -e "\n=== Verificando dependências críticas ==="
grep -n "vico\|firebase-bom" app/build.gradle.kts

echo -e "\n=== Verificando migrações do banco ==="
grep -n "version.*=" app/src/main/java/com/example/minhascompras/data/AppDatabase.kt

echo -e "\n=== Informações do Git ==="
git log --oneline -1
git branch --show-current
```

## 📋 Checklist de Execução Rápida

### Antes de Começar
- [ ] Backup completo realizado
- [ ] Tag/commit da v2.22.0 identificado
- [ ] Ambiente de desenvolvimento pronto

### Durante a Execução
- [ ] Checkout da versão correta
- [ ] Configurações ajustadas (versionCode=72, versionName="2.22.0")
- [ ] Dependências verificadas
- [ ] Build sem erros
- [ ] APK gerado com sucesso

### Validação Final
- [ ] App abre sem crashes
- [ ] Estatísticas avançadas funcionando
- [ ] Total a Pagar com comportamento correto
- [ ] Migrações de banco aplicando
- [ ] APK comparável com original

## 🚨 Comandos de Emergência

### Se precisar restaurar backup
```bash
# Parar processo atual (Ctrl+C)
cd /c/Users/nerdd/Desktop/

# Restaurar backup mais recente
ls -la | grep "Minhas-Compras-Android-backup"
cp -r Minhas-Compras-Android-backup-MAIS-RECENTE/* Minhas-Compras-Android/

# Voltar ao branch original
cd Minhas-Compras-Android
git checkout main
git branch -D revert-to-v2.22.0
```

### Se o build falhar
```bash
# Verificar erros de build
./gradlew clean
./gradlew assembleDebug --stacktrace

# Verificar dependências
./gradlew dependencies

# Limpar cache do Gradle
./gradlew clean
rm -rf .gradle
./gradlew assembleDebug
```

### Se o app crashar
```bash
# Verificar logs com adb
adb logcat | grep "minhascompras"

# Instalar APK de debug
adb install app/build/outputs/apk/debug/app-debug.apk

# Testar com banco limpo
adb shell pm clear com.example.minhascompras
```

## 📊 Validação de Features da v2.22.0

### Estatísticas Avançadas
1. Abrir o app
2. Ir para tela de estatísticas
3. Verificar se os gráficos aparecem:
   - Gráfico de linha (gastos no tempo)
   - Gráfico de pizza (categorias)
   - Gráfico de barras (comparação)
4. Testar filtros de período

### Total a Pagar Fixo
1. Criar lista com itens
2. Marcar alguns itens como comprados
3. Verificar se o total NÃO diminui
4. Verificar se a barra sempre aparece

### Migrações de Banco
1. Instalar app com dados existentes
2. Verificar se dados são migrados corretamente
3. Testar criação de novas listas
4. Verificar histórico de compras

## 📝 Notas Finais

1. **Paciência**: O processo pode levar tempo, especialmente os builds
2. **Validação**: Teste cada funcionalidade antes de considerar concluído
3. **Backup**: Mantenha o backup até ter certeza que tudo funciona
4. **Documentação**: Anote qualquer problema encontrado e solução aplicada

---

**Importante**: Execute os comandos na ordem apresentada. Não pule etapas de validação!