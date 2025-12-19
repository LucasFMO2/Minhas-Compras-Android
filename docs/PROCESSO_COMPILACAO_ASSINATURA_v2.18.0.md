# Processo de Compilação e Assinatura - Minhas Compras v2.18.0

## 📋 Overview

Este documento descreve o processo completo de compilação, assinatura e distribuição do aplicativo Minhas Compras versão 2.18.0, incluindo o sistema avançado de atualização incremental e rollback automático.

## 🔧 Pré-requisitos

### Ambiente de Desenvolvimento
- **Java**: JDK 11 ou superior
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+
- **Android Studio**: Giraffe (2022.3.1) ou superior

### Ferramentas Necessárias
```bash
# Verificar instalação
java -version
gradle --version
adb version

# Variáveis de ambiente obrigatórias
export ANDROID_HOME="/path/to/android/sdk"
export JAVA_HOME="/path/to/java/jdk"
```

### Keystore de Assinatura
- **Arquivo**: `keystore/release.jks`
- **Senha**: `minhascompras`
- **Alias**: `minhascompras`
- **Senha da Chave**: `minhascompras`

## 🏗️ Configuração do Build

### build.gradle.kts
```kotlin
android {
    namespace = "com.example.minhascompras"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.minhascompras"
        minSdk = 24
        targetSdk = 34
        versionCode = 69
        versionName = "2.18.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootProject.projectDir}/keystore/release.jks")
            storePassword = "minhascompras"
            keyAlias = "minhascompras"
            keyPassword = "minhascompras"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Configurações de Otimização
```kotlin
// gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.workers.max=4
android.useAndroidX=true
android.nonTransitiveRClass=true
```

## 🔐 Processo de Assinatura

### 1. Geração da Keystore (se necessário)
```bash
# Gerar nova keystore
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias minhascompras

# Informações para a keystore:
# Store password: minhascompras
# Key password: minhascompras
# Alias: minhascompras
# CN: Minhas Compras
# OU: Development
# O: Development Team
# L: City
# S: State
# C: BR
```

### 2. Verificação da Assinatura
```bash
# Verificar APK assinado
${ANDROID_HOME}/build-tools/34.0.0/apksigner verify -v app-release-v2.18.0.apk

# Verificar detalhes da assinatura
${ANDROID_HOME}/build-tools/34.0.0/apksigner verify -verbose -print-certs app-release-v2.18.0.apk
```

### 3. Alinhamento do APK
```bash
# Alinhar APK para otimização
${ANDROID_HOME}/build-tools/34.0.0/zipalign -v 4 app-release-unsigned.apk app-release-aligned.apk

# Assinar APK alinhado
${ANDROID_HOME}/build-tools/34.0.0/apksigner sign -ks keystore/release.jks -alias minhascompras -storepass minhascompras -keypass minhascompras app-release-aligned.apk app-release-v2.18.0.apk
```

## 🚀 Processo de Build

### Build Manual
```bash
# Limpar builds anteriores
./gradlew clean

# Build de debug
./gradlew assembleDebug

# Build de release
./gradlew assembleRelease

# Gerar AAB para Google Play
./gradlew bundleRelease
```

### Build Automatizado (PowerShell)
```powershell
# Executar script completo
.\scripts\release-v2.18.0.ps1 -BuildType release -DeployToGitHub

# Com opções personalizadas
.\scripts\release-v2.18.0.ps1 -BuildType release -SkipTests:$false -DeployToPlayStore:$true
```

### Verificação do Build
```bash
# Verificar informações do APK
${ANDROID_HOME}/build-tools/34.0.0/aapt dump badging app-release-v2.18.0.apk

# Verificar permissões
${ANDROID_HOME}/build-tools/34.0.0/aapt dump permissions app-release-v2.18.0.apk

# Verificar recursos
${ANDROID_HOME}/build-tools/34.0.0/aapt dump resources app-release-v2.18.0.apk
```

## 📦 Estrutura de Arquivos Gerados

### APK de Release
```
app/build/outputs/apk/release/
├── app-release.apk                 # APK principal
├── app-release-v2.18.0.apk       # APK renomeado
└── output-metadata.json            # Metadados do build
```

### Android App Bundle (AAB)
```
app/build/outputs/bundle/release/
├── app-release.aab                 # Bundle para Google Play
└── output-metadata.json            # Metadados do build
```

### Patches para Atualização Incremental
```
patches/
├── patch_v2.18.0.patch          # Patch incremental
├── patch_v2.18.0.zip           # Patch compactado
└── metadata.json                  # Metadados do patch
```

## 🔍 Validação de Integridade

### Checksum SHA-256
```bash
# Gerar checksum
sha256sum app-release-v2.18.0.apk

# Verificar checksum
echo "hash_esperado" | sha256sum -c -
```

### Verificação de Assinatura Digital
```bash
# Verificar assinatura completa
jarsigner -verify -verbose -certs app-release-v2.18.0.apk

# Verificar cadeia de certificados
keytool -printcert -jarfile app-release-v2.18.0.apk
```

### Análise do APK
```bash
# Informações completas do pacote
${ANDROID_HOME}/build-tools/34.0.0/aapt dump badging app-release-v2.18.0.apk | grep package

# Versão e código de versão
${ANDROID_HOME}/build-tools/34.0.0/aapt dump badging app-release-v2.18.0.apk | grep -E "versionName|versionCode"
```

## 🚀 Distribuição

### Google Play Store

#### Upload do AAB
1. Acessar [Google Play Console](https://play.google.com/console)
2. Selecionar app "Minhas Compras"
3. Ir para "Produção" → "Criar nova release"
4. Fazer upload do arquivo `app-release-v2.18.0.aab`
5. Preencher release notes:
```
**Atualizações e Melhorias:**
- Sistema de atualização avançado com rollback automático
- Backup criptografado dos dados do usuário
- Verificação de integridade SHA-256
- Atualização incremental (redução de 90% no download)
- Sistema de logging para auditoria
- Migração segura compatível com v2.16.0
```

#### Configuração de Rollout
```json
{
  "rollout": {
    "percentage": 5,
    "stages": [5, 20, 50, 100],
    "duration": "7 dias por estágio"
  }
}
```

### GitHub Releases

#### Upload Automático
```bash
# Criar release com GitHub CLI
gh release create v2.18.0 \
  --title "Release v2.18.0" \
  --notes-file "releases/v2.18.0/RELEASE_NOTES_v2.18.0.md" \
  app-release-v2.18.0.apk
```

#### Estrutura do Release
```
releases/v2.18.0/
├── app-release-v2.18.0.apk       # APK principal
├── RELEASE_NOTES_v2.18.0.md       # Notas de release
├── RELEASE_REPORT_v2.18.0.md       # Relatório completo
├── patch_v2.18.0.patch           # Patch incremental
└── checksums.txt                 # Checksums de todos os arquivos
```

### Canais Alternativos

#### Download Direto
```bash
# Servir APK via HTTP
python -m http.server 8000

# URL de download
https://seu-dominio.com/app-release-v2.18.0.apk
```

#### Distribuição Enterprise
```bash
# Assinar com certificado enterprise
jarsigner -keystore enterprise.keystore -storepass senha -keypass senha app-release-v2.18.0.apk

# Distribuir via MDM (Mobile Device Management)
# Upload para sistema de gerenciamento da empresa
```

## 🔄 Sistema de Atualização Incremental

### Geração de Patches
```bash
# Criar patch usando bsdiff
bsdiff old.apk new.apk patch_v2.18.0.patch

# Compactar patch
gzip patch_v2.18.0.patch -c > patch_v2.18.0.patch.gz
```

### Metadados do Patch
```json
{
  "version": "2.18.0",
  "from_version": "2.16.0",
  "patch_type": "bsdiff",
  "compression": "gzip",
  "original_size": 13631488,
  "patch_size": 1363149,
  "compression_ratio": 0.9,
  "checksum_sha256": "abc123...",
  "original_checksum": "def456..."
}
```

### Aplicação do Patch
```kotlin
// No aplicativo
val patchManager = PatchManager(context)
val patchInfo = PatchInfo(
    fileName = "patch_v2.18.0.patch",
    patchUrl = "https://api.example.com/patches/patch_v2.18.0.patch",
    // ... outros parâmetros
)

val result = patchManager.applyPatch(currentApk, patchInfo) { progress ->
    updateProgressBar(progress)
}
```

## 🛡️ Sistema de Backup e Rollback

### Backup Automático
```kotlin
val backupManager = BackupManager(context)
val backupInfo = backupManager.createBackup()

// Componentes do backup:
// - Banco de dados Room (compras_database)
// - SharedPreferences
// - Arquivos locais
// - Metadados do app
```

### Rollback Automático
```kotlin
val rollbackManager = RollbackManager(context)
val assessment = rollbackManager.shouldRollback()

if (assessment.shouldRollback) {
    val result = rollbackManager.executeRollback(assessment.reason)
    // Notificar usuário sobre rollback
}
```

### Verificação de Integridade
```kotlin
val integrityChecker = IntegrityChecker(context)
val result = integrityChecker.verifyApkIntegrity(apkFile)

// Verificações realizadas:
// - Checksum SHA-256
// - Assinatura digital
// - Informações do pacote
// - Certificado
// - Estrutura do APK
```

## 📊 Monitoramento e Logging

### Sistema de Logging
```kotlin
val updateLogger = UpdateLogger(context)

// Log de operações
updateLogger.logOperationStart("download", "network")
updateLogger.logOperationEnd(operationId, "download", "network", true, duration)

// Log de segurança
updateLogger.logSecurityEvent("signature_verified", SecuritySeverity.HIGH)

// Log de performance
updateLogger.logPerformance("patch_application", duration)
```

### Métricas de Sucesso
- **Taxa de sucesso de atualização**: > 95%
- **Tempo médio de rollback**: < 30 segundos
- **Redução com patches**: até 90%
- **Taxa de corrupção zero**: 0%
- **Compatibilidade v2.16.0**: 100%

## 🔧 Troubleshooting

### Problemas Comuns

#### Build Falha
```bash
# Limpar completamente
./gradlew clean
./gradlew build --refresh-keys

# Verificar dependências
./gradlew dependencies
```

#### Assinatura Falha
```bash
# Verificar keystore
keytool -list -v -keystore keystore/release.jks

# Verificar aliases
keytool -list -keystore keystore/release.jks -alias minhascompras
```

#### Upload Falha
```bash
# Verificar tamanho do arquivo
ls -lh app-release-v2.18.0.apk

# Verificar permissões
chmod 644 app-release-v2.18.0.apk
```

#### Atualização Falha
```bash
# Verificar logs
adb logcat | grep "UpdateManager"

# Verificar espaço disponível
adb shell df -h

# Verificar rede
adb shell ping -c 3 api.github.com
```

### Logs de Depuração

### Android Logcat
```bash
# Logs de atualização
adb logcat | grep -E "UpdateManager|BackupManager|RollbackManager"

# Logs de erro
adb logcat | grep -E "ERROR|FATAL" | grep "com.example.minhascompras"

# Logs de sistema
adb logcat | grep -E "PackageManager|Install"
```

### Logs do Aplicativo
```kotlin
// No código
Logger.d("UpdateManager", "Mensagem de debug")
Logger.i("UpdateManager", "Mensagem de info")
Logger.w("UpdateManager", "Mensagem de warning")
Logger.e("UpdateManager", "Mensagem de erro", exception)
```

## 📋 Checklist Final

### Antes do Release
- [ ] Versão incrementada (versionCode: 69, versionName: 2.18.0)
- [ ] Testes unitários passando
- [ ] Testes de instrumentação passando
- [ ] Assinatura verificada
- [ ] Checksum SHA-256 gerado
- [ ] Release notes preparadas
- [ ] Patch incremental gerado
- [ ] Backup automatizado testado
- [ ] Rollback automático testado
- [ ] Compatibilidade v2.16.0 verificada

### Pós-Release
- [ ] APK assinado e validado
- [ ] Release no GitHub criado
- [ ] AAB enviado para Google Play
- [ ] Configuração de rollout definida
- [ ] Monitoramento configurado
- [ ] Documentação atualizada
- [ ] Suporte preparado

## 📞 Suporte

### Contato
- **Desenvolvimento**: [dev@minhascompras.com](mailto:dev@minhascompras.com)
- **Suporte**: [suporte@minhascompras.com](mailto:suporte@minhascompras.com)
- **Issues**: [GitHub Issues](https://github.com/Lucasfmo1/Minhas-Compras-Android/issues)

### Documentação
- **API Docs**: [docs/api](./api/)
- **User Guide**: [docs/user-guide](./user-guide/)
- **Developer Guide**: [docs/developer](./developer/)

---

**Versão**: v2.18.0  
**Data**: 2024-12-19  
**Responsável**: Equipe de Desenvolvimento  
**Status**: ✅ Produção Ready