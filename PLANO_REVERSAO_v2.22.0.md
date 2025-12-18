# Plano Detalhado de Reversão para Versão 2.22.0

## 📋 Análise do Estado Atual

### Versão Atual
- **Versão atual**: 2.25.0 (versionCode: 75)
- **Versão alvo**: 2.22.0 (versionCode: 72)
- **Database version atual**: 8
- **APK disponível**: app-release-v2.22.0.apk

### Principais Diferenças Identificadas
1. **Estatísticas Avançadas**: Introduzidas na v2.22.0 (gráficos Vico Charts)
2. **Total a Pagar Fixo**: Melhoria no comportamento do cálculo
3. **Migrações de DB**: Versão 8 do banco de dados
4. **Dependências**: Biblioteca Vico Charts para gráficos

## 🎯 Objetivo

Restaurar completamente o projeto para o estado exato da versão 2.22.0, garantindo:
- Código fonte idêntico
- Configurações de build corretas
- Dependências compatíveis
- Banco de dados funcional
- APK compilável e funcional

## 📋 Plano de Ação Detalhado

### Fase 1: Preparação e Backup

#### 1.1 Backup Completo do Projeto
```bash
# Criar backup completo do projeto atual
cp -r /c/Users/nerdd/Desktop/Minhas-Compras-Android /c/Users/nerdd/Desktop/Minhas-Compras-Android-backup-$(date +%Y%m%d-%H%M%S)

# Backup específico do banco de dados se houver dados importantes
cp -r app/src/main/assets/databases /c/Users/nerdd/Desktop/backup-databases-$(date +%Y%m%d-%H%M%S)
```

#### 1.2 Identificação da Tag/Commit da v2.22.0
```bash
# Listar todas as tags para encontrar a v2.22.0
git tag --list | grep v2.22

# Se não houver tag, procurar por commit com a versão
git log --oneline --grep="2.22.0" --all

# Verificar informações da tag/commit
git show v2.22.0  # ou git show <hash-do-commit>
```

### Fase 2: Reversão do Código Fonte

#### 2.1 Preparação para Reversão
```bash
# Salvar alterações não commitadas (se houver)
git stash push -m "Alterações antes da reversão para v2.22.0"

# Limpar working directory
git clean -fd
git reset --hard HEAD
```

#### 2.2 Reversão para a Versão Alvo
```bash
# Se existir tag:
git checkout v2.22.0

# Se não houver tag, usar o commit:
git checkout <hash-do-commit-da-v2.22.0>

# Criar branch a partir da versão alvo
git checkout -b revert-to-v2.22.0
```

### Fase 3: Ajustes de Configuração

#### 3.1 Configurações de Build
Verificar e ajustar `app/build.gradle.kts`:
```kotlin
defaultConfig {
    applicationId = "com.example.minhascompras"
    minSdk = 24
    targetSdk = 34
    versionCode = 72  // Alterar para 72
    versionName = "2.22.0"  // Alterar para 2.22.0
}
```

#### 3.2 Verificação de Dependências
Verificar se as dependências na v2.22.0 estão corretas:
- Vico Charts: 1.13.1
- Firebase BOM: 33.7.0
- Compose BOM: versão compatível
- Room: versão compatível

### Fase 4: Banco de Dados

#### 4.1 Análise das Migrações Necessárias
- **Versão atual do DB**: 8
- **Versão na v2.22.0**: 8 (conforme AppDatabase.kt)
- **Ação**: Manter migrações existentes (MIGRATION_7_8)

#### 4.2 Preparação do Banco de Dados
```kotlin
// Em AppDatabase.kt, garantir que todas as migrações até a v8 estejam presentes
.addMigrations(
    MIGRATION_2_3, 
    MIGRATION_3_4, 
    MIGRATION_4_5, 
    MIGRATION_5_6, 
    MIGRATION_6_7, 
    MIGRATION_7_8
)
```

### Fase 5: Build e Testes

#### 5.1 Limpeza e Build
```bash
# Limpar build anterior
./gradlew clean

# Build do projeto
./gradlew assembleDebug

# Build de release (se necessário)
./gradlew assembleRelease
```

#### 5.2 Testes Funcionais
- Testar funcionalidades básicas do app
- Verificar estatísticas avançadas (gráficos)
- Validar comportamento do "Total a Pagar"
- Testar migrações de banco de dados

#### 5.3 Testes Específicos da v2.22.0
1. **Estatísticas Avançadas**:
   - Gráfico de gastos ao longo do tempo
   - Gráfico de pizza por categoria
   - Comparação entre períodos
   - Top itens mais comprados

2. **Total a Pagar**:
   - Valor fixo mostrando todos os itens
   - Não diminui ao marcar itens como comprados
   - Barra sempre visível quando há itens

### Fase 6: Validação Final

#### 6.1 Comparações
- Comparar APK gerado com `app-release-v2.22.0.apk`
- Validar versionCode e versionName
- Verificar assinatura digital

#### 6.2 Testes de Aceitação
- Instalação e funcionamento básico
- Performance das estatísticas
- Comportamento do Total a Pagar
- Migrações de dados existentes

## ⚠️ Riscos e Mitigações

### Riscos Identificados

1. **Perda de Dados do Usuário**
   - **Risco**: Migrações reversas podem corromper dados
   - **Mitigação**: Backup completo do banco de dados antes da reversão

2. **Dependências Incompatíveis**
   - **Risco**: Versões de bibliotecas podem ter mudado
   - **Mitigação**: Verificar arquivo de lock ou usar versões exatas da v2.22.0

3. **Build Failures**
   - **Risco**: Configurações de build podem estar incompatíveis
   - **Mitigação**: Comparar com build.gradle.kts da v2.22.0

4. **Funcionalidades Perdidas**
   - **Risco**: Features pós-v2.22.0 serão perdidas
   - **Mitigação**: Documentar claramente o que será perdido

### Planos de Contingência

1. **Se o build falhar**:
   - Reverter para backup
   - Comparar dependências com a v2.22.0
   - Verificar configurações de build

2. **Se o app crashar**:
   - Analisar logs
   - Verificar migrações de banco de dados
   - Testar com banco de dados limpo

3. **Se dados forem corrompidos**:
   - Restaurar do backup
   - Implementar migração de recuperação
   - Oferecer processo de reimportação

## 📝 Checklist Final

### Antes da Reversão
- [ ] Backup completo do projeto
- [ ] Backup do banco de dados
- [ ] Identificação da tag/commit da v2.22.0
- [ ] Documentação de alterações pós-v2.22.0

### Durante a Reversão
- [ ] Checkout da versão correta
- [ ] Ajuste de versionCode/versionName
- [ ] Verificação de dependências
- [ ] Build bem-sucedido

### Após a Reversão
- [ ] Testes funcionais completos
- [ ] Validação de features da v2.22.0
- [ ] Comparação com APK original
- [ ] Documentação do processo

## 🔧 Comandos Essenciais

```bash
# Backup completo
cp -r Minhas-Compras-Android Minhas-Compras-Android-backup-$(date +%Y%m%d)

# Listar tags
git tag --list | grep v2.22

# Reversão
git checkout v2.22.0
git checkout -b revert-to-v2.22.0

# Build
./gradlew clean
./gradlew assembleDebug

# Testes
./gradlew test
./gradlew connectedAndroidTest
```

## 📚 Referências

- [Release Notes v2.22.0](RELEASE_NOTES_v2.22.0.md)
- [APK Original](app-release-v2.22.0.apk)
- [Configurações de Build](app/build.gradle.kts)
- [Migrações do Banco](app/src/main/java/com/example/minhascompras/data/AppDatabase.kt)

---

**Importante**: Este plano deve ser executado cuidadosamente, com validação em cada etapa. Qualquer anomalia deve ser investigada antes de prosseguir para a próxima fase.