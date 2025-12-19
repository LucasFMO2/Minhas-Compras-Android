# Estratégia de Distribuição v2.18.0 - Minhas Compras Android

## 📋 Overview

Este documento descreve a estratégia completa de distribuição da versão 2.18.0 do aplicativo Minhas Compras, cobrindo Google Play Store, GitHub Releases e canais alternativos, com foco em atualização incremental e rollback automático.

## 🎯 Objetivos da Distribuição

### Objetivos Principais
- **Alcançar 95% de taxa de sucesso de atualização**
- **Reduzir tamanho de downloads em até 90% com patches incrementais**
- **Manter compatibilidade 100% com dados da v2.16.0**
- **Zero corrupção de dados com rollback automático**
- **Tempo médio de rollback < 30 segundos**

### KPIs de Sucesso
- **Taxa de adoção**: > 80% em 30 dias
- **Taxa de falha**: < 5% em todas as instalações
- **Tempo de atualização**: < 2 minutos (WiFi)
- **Taxa de rollback**: < 1% (apenas em falhas reais)
- **Satisfação do usuário**: > 4.5/5.0

## 🚀 Google Play Store

### Configuração de Release

#### 1. Preparação do AAB
```bash
# Gerar Android App Bundle
./gradlew bundleRelease

# Verificar AAB gerado
ls -lh app/build/outputs/bundle/release/app-release.aab
```

#### 2. Metadados da Release
```json
{
  "version": {
    "name": "2.18.0",
    "code": 69,
    "release_notes": "Sistema avançado de atualização com rollback automático",
    "whats_new": [
      "Atualização incremental (redução de 90% no download)",
      "Backup automático com criptografia AES-128",
      "Rollback inteligente em caso de falha",
      "Verificação SHA-256 de integridade",
      "Logging completo para auditoria",
      "Migração segura desde v2.16.0"
    ]
  },
  "compatibility": {
    "min_android_version": "7.0 (API 24)",
    "target_android_version": "14 (API 34)",
    "direct_upgrade_from": "2.16.0+",
    "preserves_data": true
  },
  "features": {
    "incremental_updates": true,
    "automatic_backup": true,
    "rollback_support": true,
    "integrity_verification": true,
    "secure_migration": true
  }
}
```

#### 3. Estratégia de Rollout

##### Fase 1: Beta Interno (5%)
- **Duração**: 3 dias
- **Público**: Equipe interna + beta testers
- **Monitoramento**: Tempo real
- **Critérios de sucesso**: 
  - Taxa de instalação > 90%
  - Zero crashes relacionados à atualização
  - Backup funcionando em 100% dos casos

##### Fase 2: Beta Fechado (20%)
- **Duração**: 5 dias
- **Público**: Usuários selecionados
- **Monitoramento**: Dashboards + alertas
- **Critérios de sucesso**:
  - Taxa de sucesso > 95%
  - Taxa de rollback < 0.5%
  - Performance mantida

##### Fase 3: Produção Parcial (50%)
- **Duração**: 7 dias
- **Público**: 50% dos usuários aleatórios
- **Monitoramento**: Completo com analytics
- **Critérios de sucesso**:
  - Estabilidade mantida
  - Feedback positivo > 90%
  - Métricas dentro do esperado

##### Fase 4: Produção Completo (100%)
- **Duração**: Contínuo
- **Público**: Todos os usuários
- **Monitoramento**: Contínuo com alertas automáticas

#### 4. Configuração de Play Console

##### Store Listing
```json
{
  "app_title": "Minhas Compras",
  "short_description": "Lista de compras inteligente com backup automático",
  "full_description": "Organize suas compras com sistema avançado de atualização incremental e rollback automático. Totalmente compatível com dados da versão 2.16.0.",
  "release_notes": {
    "pt-BR": "Sistema avançado de atualização com rollback automático e backup criptografado.",
    "en-US": "Advanced update system with automatic rollback and encrypted backup."
  },
  "category": "Shopping",
  "content_rating": "Everyone",
  "tags": ["shopping", "list", "backup", "update", "rollback"]
}
```

##### Política de Privacidade
- **Dados coletados**: Apenas logs de atualização (anonimizados)
- **Uso dos dados**: Melhorar sistema de atualização e rollback
- **Compartilhamento**: Nunca compartilhados com terceiros
- **Retenção**: Logs por 90 dias, backups por 30 dias

### 5. Sistema de Atualização In-App

#### Configuração do Play Core
```kotlin
// No Application class
private val updateManager = UpdateManager.getInstance()

// Verificar atualizações
private fun checkForUpdates() {
    updateManager.getAppUpdateInfo().addOnSuccessListener { info ->
        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            // Mostrar diálogo de atualização
            showUpdateDialog(info)
        }
    }
}

// Atualização flexível
private fun startFlexibleUpdate(info: AppUpdateInfo) {
    updateManager.startUpdateFlowForResult(
        info,
        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
    )
}
```

#### 6. Monitoramento e Analytics

##### Métricas Essenciais
```kotlin
// Eventos de atualização
analytics.logEvent("update_started", mapOf(
    "version" to "2.18.0",
    "update_type" to "incremental",
    "network_type" to "wifi"
))

analytics.logEvent("update_completed", mapOf(
    "duration_ms" to duration,
    "success" to true,
    "rollback_available" to true
))

analytics.logEvent("rollback_executed", mapOf(
    "reason" to "update_failed",
    "restore_time_ms" to restoreTime,
    "backup_age_hours" to backupAge
))
```

## 🐙 GitHub Releases

### Estrutura de Release

#### 1. Assets do Release
```
v2.18.0/
├── app-release-v2.18.0.apk          # APK principal (13MB)
├── app-release-v2.18.0.aab          # Android App Bundle (12MB)
├── patch_v2.18.0.patch              # Patch incremental (1.3MB)
├── patch_v2.18.0.patch.gz           # Patch compactado (400KB)
├── checksums.txt                     # Checksums SHA-256
├── RELEASE_NOTES_v2.18.0.md        # Notas detalhadas
├── RELEASE_REPORT_v2.18.0.md        # Relatório técnico
└── UPDATE_GUIDE_v2.18.0.md         # Guia de atualização
```

#### 2. Metadados do Release
```yaml
name: Release v2.18.0
tag_name: v2.18.0
target_commitish: main
draft: false
prerelease: false

body: |
  ## Release v2.18.0
  
  **Atualizações e Melhorias:**
  - 🚀 Sistema de atualização incremental (redução de 90% no download)
  - 🛡️ Backup automático com criptografia AES-128
  - 🔄 Rollback inteligente em caso de falha
  - ✅ Verificação SHA-256 de integridade
  - 📊 Logging completo para auditoria
  - 🔄 Migração segura desde v2.16.0
  
  **APK Information:**
  - Versão: v2.18.0
  - versionCode: 69
  - Tamanho: 13 MB
  - Build: Release
  - Assinatura: Keystore MinhasCompras
  - Compatibilidade: Android 7.0+ (API 24+)
  
  **📥 Download:**
  - [APK Completo](app-release-v2.18.0.apk) (13 MB)
  - [Patch Incremental](patch_v2.18.0.patch.gz) (400 KB)
  - [Android App Bundle](app-release-v2.18.0.aab) (12 MB)
  
  **🔐 Security & Performance:**
  - ✅ Verificação de assinatura digital
  - ✅ Validação SHA-256
  - ✅ Backup criptografado
  - ✅ Atualização incremental (bsdiff)
  - ✅ Rollback automático
  - ✅ Logging completo
  
  **⚠️ Important Notes:**
  - Esta versão requer Android 7.0 ou superior
  - Atualização direta desde v2.16.0 mantém todos os dados
  - Rollback automático se atualização falhar
  - Backup criado antes de qualquer atualização
```

#### 3. Automação com GitHub Actions
```yaml
# .github/workflows/release.yml
name: Release v2.18.0

on:
  push:
    tags:
      - 'v2.18.0'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
          
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
        
      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          
      - name: Build Release
        run: ./gradlew assembleRelease bundleRelease
        
      - name: Generate Checksums
        run: |
          sha256sum app/build/outputs/apk/release/app-release.apk > checksums.txt
          sha256sum app/build/outputs/bundle/release/app-release.aab >> checksums.txt
          
      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: |
            app/build/outputs/apk/release/app-release.apk
            app/build/outputs/bundle/release/app-release.aab
            checksums.txt
          draft: false
          prerelease: false
```

## 📱 Canais Alternativos

### 1. Download Direto

#### Servidor Web
```nginx
# Configuração do servidor
server {
    listen 443 ssl http2;
    server_name updates.minhascompras.com;
    
    location /releases/ {
        alias /var/www/minhascompras/releases/;
        
        # Forçar HTTPS
        if ($scheme != "https") {
            return 301 https://$server_name$request_uri;
        }
        
        # Headers de segurança
        add_header X-Content-Type-Options nosniff;
        add_header X-Frame-Options DENY;
        add_header X-XSS-Protection "1; mode=block";
        
        # Cache para APKs
        location ~*\.apk$ {
            expires 30d;
            add_header Cache-Control "public, immutable";
        }
        
        # Cache para patches
        location ~*\.patch(\.gz)?$ {
            expires 7d;
            add_header Cache-Control "public";
        }
    }
    
    # SSL
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
}
```

#### API de Verificação de Atualizações
```kotlin
// UpdateAPI.kt
class UpdateAPI {
    private const val BASE_URL = "https://api.minhascompras.com/v1"
    
    suspend fun checkForUpdates(currentVersion: Int): UpdateResponse {
        return withContext(Dispatchers.IO) {
            val url = "$BASE_URL/updates/check?version=$currentVersion"
            val response = httpClient.get(url)
            
            if (response.status.isSuccess()) {
                response.body<UpdateResponse>()
            } else {
                throw UpdateException("Failed to check updates")
            }
        }
    }
    
    suspend fun downloadPatch(patchInfo: PatchInfo): File {
        val url = "$BASE_URL/patches/${patchInfo.fileName}"
        return httpClient.downloadFile(url)
    }
}

data class UpdateResponse(
    val hasUpdate: Boolean,
    val updateInfo: UpdateInfo?,
    val patchInfo: PatchInfo?
)
```

### 2. Distribuição Enterprise

#### MDM (Mobile Device Management)
```xml
<!-- Android Enterprise Configuration -->
<managed-configuration>
    <applications>
        <application package="com.example.minhascompras">
            <install-type>silent</install-type>
            <update-policy>auto</update-policy>
            <backup-policy>enabled</backup-policy>
            <rollback-policy>enabled</rollback-policy>
        </application>
    </applications>
    
    <security-policies>
        <allow-unknown-sources>false</allow-unknown-sources>
        <require-integrity-check>true</require-integrity-check>
        <enforce-backup>true</enforce-backup>
    </security-policies>
</managed-configuration>
```

#### Sideloading Controlado
```kotlin
// EnterpriseInstallManager.kt
class EnterpriseInstallManager {
    fun validateEnterpriseInstall(apkFile: File): ValidationResult {
        // Verificar assinatura enterprise
        val signatureValid = verifyEnterpriseSignature(apkFile)
        
        // Verificar certificado corporativo
        val certificateValid = verifyEnterpriseCertificate(apkFile)
        
        // Verificar permissões
        val permissionsValid = validatePermissions(apkFile)
        
        return ValidationResult(
            isValid = signatureValid && certificateValid && permissionsValid,
            issues = listOfNotNull(
                if (!signatureValid) "Assinatura inválida" else null,
                if (!certificateValid) "Certificado não autorizado" else null,
                if (!permissionsValid) "Permissões inadequadas" else null
            )
        )
    }
}
```

### 3. Canais de Comunidade

#### F-Droid
```xml
<!-- fdroiddata/repo.xml -->
<repo>
    <name>Minhas Compras</name>
    <description>Lista de compras com sistema avançado de atualização</description>
    <version>2.18.0</version>
    <url>https://f-droid.minhascompras.com/repo</url>
    
    <application>
        <id>com.example.minhascompras</id>
        <name>Minhas Compras</name>
        <version code="69">2.18.0</version>
        <versioncode>69</versioncode>
        <description>Organizador de compras com backup automático e atualização incremental</description>
        
        <uses-permission>
            <uses-permission name="android.permission.INTERNET"/>
            <uses-permission name="android.permission.ACCESS_NETWORK_STATE"/>
            <uses-permission name="android.permission.REQUEST_INSTALL_PACKAGES"/>
        </uses-permission>
        
        <srcurl>https://github.com/Lucasfmo1/Minhas-Compras-Android</srcurl>
    </application>
</repo>
```

#### APKPure e Outros
```json
{
  "distribution": {
    "enabled": true,
    "channels": ["apkpure", "apkmirror", "uptodown"],
    "verification_required": true,
    "checksum_provided": true
  },
  "monitoring": {
    "track_downloads": true,
    "track_installations": true,
    "track_errors": true
  }
}
```

## 📊 Sistema de Monitoramento

### 1. Métricas em Tempo Real

#### Dashboard de Atualizações
```typescript
interface UpdateDashboard {
  // Métricas gerais
  totalUpdates: number;
  successfulUpdates: number;
  failedUpdates: number;
  rollbackExecutions: number;
  
  // Performance
  averageUpdateTime: number;
  averageRollbackTime: number;
  patchEfficiency: number;
  
  // Por versão
  versionMetrics: {
    [version: string]: {
      installs: number;
      successRate: number;
      rollbackRate: number;
      averageTime: number;
    }
  };
  
  // Por canal
  channelMetrics: {
    google_play: ChannelMetrics;
    github: ChannelMetrics;
    direct: ChannelMetrics;
    enterprise: ChannelMetrics;
  };
}
```

#### Alertas Automáticas
```kotlin
class AlertManager {
    fun setupAlerts() {
        // Taxa de falha > 5%
        monitorFailureRate { rate ->
            if (rate > 0.05) {
                sendAlert("ALTA_TAXA_FALHA", mapOf(
                    "rate" to rate,
                    "threshold" to 0.05
                ))
            }
        }
        
        // Tempo de atualização > 5 minutos
        monitorUpdateTime { time ->
            if (time > 300000) { // 5 minutos
                sendAlert("TEMPO_ATUALIZACAO_ALTO", mapOf(
                    "time_ms" to time
                ))
            }
        }
        
        // Taxa de rollback > 2%
        monitorRollbackRate { rate ->
            if (rate > 0.02) {
                sendAlert("ALTA_TAXA_ROLLBACK", mapOf(
                    "rate" to rate,
                    "threshold" to 0.02
                ))
            }
        }
    }
}
```

### 2. Analytics de Usuário

#### Eventos Personalizados
```kotlin
// Eventos de atualização
analytics.logEvent("update_flow_started", mapOf(
    "version_from" to "2.16.0",
    "version_to" to "2.18.0",
    "update_type" to "incremental",
    "network_type" to "wifi",
    "battery_level" to 85,
    "available_space_mb" to 512
))

analytics.logEvent("backup_created", mapOf(
    "backup_size_mb" to 2.5,
    "components_count" to 3,
    "encryption_enabled" to true,
    "duration_ms" to 1500
))

analytics.logEvent("patch_applied", mapOf(
    "patch_size_mb" to 1.3,
    "compression_ratio" to 0.9,
    "apply_duration_ms" to 8000,
    "success" to true
))

analytics.logEvent("rollback_executed", mapOf(
    "reason" to "integrity_check_failed",
    "backup_age_hours" to 24,
    "restore_duration_ms" to 25000,
    "data_loss" to false
))
```

### 3. Relatórios Automáticos

#### Relatório Diário
```json
{
  "date": "2024-12-19",
  "summary": {
    "total_updates": 1250,
    "successful_updates": 1187,
    "failed_updates": 63,
    "rollback_executions": 12,
    "success_rate": 0.9496,
    "rollback_rate": 0.0096
  },
  "performance": {
    "average_update_time_ms": 85000,
    "average_rollback_time_ms": 22000,
    "patch_efficiency": 0.89,
    "data_loss_incidents": 0
  },
  "channels": {
    "google_play": {
      "updates": 980,
      "success_rate": 0.965
    },
    "github": {
      "updates": 180,
      "success_rate": 0.944
    },
    "direct": {
      "updates": 90,
      "success_rate": 0.878
    }
  },
  "errors": [
    {
      "type": "network_timeout",
      "count": 15,
      "affected_channels": ["direct", "github"]
    },
    {
      "type": "integrity_check_failed",
      "count": 8,
      "affected_versions": ["2.17.1"]
    }
  ]
}
```

## 🔧 Configuração de Infraestrutura

### 1. CDN para Distribuição

#### CloudFlare Configuração
```javascript
// CDN Configuration
const cdnConfig = {
  zones: ['minhascompras.com'],
  
  cacheRules: [
    {
      name: 'APK Files',
      target: 'file',
      match: ['*.apk'],
      cacheTtl: 2592000, // 30 dias
      browserCacheTtl: 2592000,
      edgeCacheTtl: 2592000
    },
    {
      name: 'Patch Files',
      target: 'file',
      match: ['*.patch*'],
      cacheTtl: 604800, // 7 dias
      browserCacheTtl: 604800,
      edgeCacheTtl: 604800
    }
  ],
  
  security: {
    level: 'high',
    ssl: 'strict',
    hotlinkProtection: true
  }
};
```

#### Balanceamento de Carga
```nginx
upstream update_servers {
    server update1.minhascompras.com:443 max_fails=3 fail_timeout=30s;
    server update2.minhascompras.com:443 max_fails=3 fail_timeout=30s;
    server update3.minhascompras.com:443 max_fails=3 fail_timeout=30s;
}

server {
    listen 443 ssl http2;
    server_name updates.minhascompras.com;
    
    location /releases/ {
        proxy_pass https://update_servers;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        
        # Health checks
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
    }
}
```

### 2. Sistema de Backup dos Downloads

#### Redundância Geográfica
```python
# backup_downloads.py
import boto3
import requests
from datetime import datetime

def backup_to_s3(apk_path, version):
    s3 = boto3.client('s3')
    
    # Upload para S3 com redundância
    s3.upload_file(
        apk_path,
        f'releases/v{version}/app-release-v{version}.apk',
        ExtraArgs={
            'StorageClass': 'STANDARD_IA', # Infrequent Access
            'ServerSideEncryption': 'AES256',
            'Metadata': {
                'version': version,
                'upload_date': datetime.now().isoformat(),
                'source': 'build_server'
            }
        }
    )
    
    # Replicar para outras regiões
    replicate_to_regions(apk_path, version, ['eu-west-1', 'ap-southeast-1'])

def replicate_to_regions(apk_path, version, regions):
    for region in regions:
        s3_region = boto3.client('s3', region_name=region)
        s3_region.upload_file(
            apk_path,
            f'releases/v{version}/app-release-v{version}.apk',
            ExtraArgs={'StorageClass': 'STANDARD'}
        )
```

## 📋 Checklist de Distribuição

### Pré-Lançamento
- [ ] Build de release gerado e assinado
- [ ] Checksums SHA-256 calculados
- [ ] Patch incremental criado e testado
- [ ] Notas de release preparadas
- [ ] Metadados configurados em todos os canais
- [ ] Sistema de monitoramento ativo
- [ ] Alertas configuradas
- [ ] CDN configurado e testado
- [ ] Backup dos servidores atualizado
- [ ] Documentação atualizada
- [ ] Equipe de suporte treinada

### Lançamento Google Play
- [ ] AAB enviado para Google Play Console
- [ ] Release notes preenchidas
- [ ] Configuração de rollout definida
- [ ] Review enviado para aprovação
- [ ] Monitoramento ativo durante rollout
- [ ] Planos de contingência preparados
- [ ] Comunicação com usuários preparada

### Lançamento GitHub
- [ ] Tag criada e推送ada
- [ ] Release criado com todos os assets
- [ ] Checksums incluídos no release
- [ ] Links de download testados
- [ ] Páginas de documentação atualizadas
- [ ] Issues relacionados marcados

### Lançamento Canais Alternativos
- [ ] APK disponível no servidor web
- [ ] Patch disponível para download
- [ ] API de verificação funcional
- [ ] Configuração enterprise preparada
- [ ] F-Droid metadata atualizado
- [ ] Links de alternativos funcionando

### Pós-Lançamento
- [ ] Monitoramento 24/7 ativo por 7 dias
- [ ] Métricas coletadas e analisadas
- [ ] Feedback dos usuários monitorado
- [ ] Bugs corrigidos em tempo hábil
- [ ] Performance otimizada conforme necessário
- [ ] Comunicação de problemas transparente
- [ ] Relatórios de sucesso gerados

## 🚨 Plano de Contingência

### Falhas Comuns

#### Problema: Download Lento
```kotlin
class SlowDownloadHandler {
    fun handleSlowDownload() {
        // Verificar largura de banda
        val bandwidth = NetworkMonitor.getCurrentBandwidth()
        
        if (bandwidth < 1_000_000) { // < 1 Mbps
            // Oferecer download via torrent
            showTorrentOption()
            
            // Sugerir download em horários de baixo tráfego
            showOffHoursOption()
        }
        
        // Ativar download com retomada
        enableResumableDownload()
    }
}
```

#### Problema: Falha de Integridade
```kotlin
class IntegrityFailureHandler {
    fun handleIntegrityFailure() {
        // Registrar falha detalhadamente
        updateLogger.logSecurityEvent(
            "integrity_check_failed",
            SecuritySeverity.HIGH,
            mapOf(
                "checksum_mismatch" to true,
                "corruption_detected" to true
            )
        )
        
        // Oferecer download completo
        showFullDownloadOption()
        
        // Verificar se há mirror disponível
        checkMirrorAvailability()
    }
}
```

#### Problema: Rollback Necessário
```kotlin
class RollbackHandler {
    fun handleRollback(reason: RollbackReason) {
        // Notificar usuário proativamente
        showRollbackNotification(reason)
        
        // Executar rollback automático
        val result = rollbackManager.executeRollback(reason)
        
        if (result.success) {
            // Coletar informações para debugging
            collectRollbackData(result)
            
            // Oferecer suporte
            showSupportOption()
        }
    }
}
```

### Comunicação de Crises

#### Template de Comunicação
```markdown
## 🚨 Problema na Atualização v2.18.0

**Status**: Investigando  
**Início**: 2024-12-19 14:30 UTC  
**Impacto**: Usuários tentando atualizar  
**Severidade**: Média  

### O que aconteceu
Estamos investigando problemas na atualização v2.18.0 para alguns usuários.

### Sintomas
- Download pode ficar lento ou falhar
- Verificação de integridade pode falhar
- Aplicativo pode solicitar rollback automático

### O que fazer
1. **Aguarde**: Estamos trabalhando na correção
2. **Use WiFi**: Para downloads mais estáveis
3. **Espaço**: Garanta 50MB livres
4. **Backup**: Seus dados estão seguros

### Alternativas
- **Download completo**: [Link direto](https://minhascompras.com/app-v2.18.0.apk)
- **Versão anterior**: [v2.17.1](https://minhascompras.com/app-v2.17.1.apk)

### Próximos passos
- Estamos aplicando correção
- Nova versão em breve
- Monitoramento 24/7 ativo

Pedimos desculpas pelo inconveniente.

---
**Equipe Minhas Compras**  
suporte@minhascompras.com  
[Status Page](https://status.minhascompras.com)
```

---

## 📞 Suporte e Documentação

### Canais de Suporte
- **Email**: suporte@minhascompras.com
- **Formulário**: https://minhascompras.com/support
- **FAQ**: https://minhascompras.com/faq
- **Status**: https://status.minhascompras.com
- **Comunidade**: https://github.com/Lucasfmo1/Minhas-Compras-Android/discussions

### Documentação Técnica
- **API Docs**: https://docs.minhascompras.com/api
- **Update Guide**: https://docs.minhascompras.com/update-guide
- **Troubleshooting**: https://docs.minhascompras.com/troubleshooting
- **Enterprise Guide**: https://docs.minhascompras.com/enterprise

### Recursos para Desenvolvedores
- **GitHub**: https://github.com/Lucasfmo1/Minhas-Compras-Android
- **Wiki**: https://github.com/Lucasfmo1/Minhas-Compras-Android/wiki
- **Issues**: https://github.com/Lucasfmo1/Minhas-Compras-Android/issues
- **Discord**: https://discord.gg/minhascompras

---

**Versão**: v2.18.0  
**Data**: 2024-12-19  
**Responsável**: Equipe de Distribuição  
**Status**: ✅ Estratégia Completa