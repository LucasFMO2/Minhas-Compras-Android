# Release Notes v2.27.2

## Data de Lançamento
12 de Dezembro de 2024

## Resumo
Versão de correção crítica que resolve o problema de conflito de assinaturas entre APKs, garantindo compatibilidade com instalações anteriores e estabilidade do aplicativo.

## Principais Correções

### 🔧 Correção de Conflito de Assinaturas
- **Problema**: Conflito de assinaturas impedia instalação/atualização do APK v2.27.1
- **Solução**: Gerado novo APK v2.27.2 com assinatura compatível
- **Impacto**: Usuários agora podem instalar/atualizar o aplicativo sem conflitos

### 📦 APK Funcional
- APK v2.27.2 gerado e testado com sucesso
- Compatibilidade garantida com instalações anteriores
- Processo de instalação validado em dispositivo real

## Documentação Adicionada

### 📚 Guias e Documentação
- `SOLUCAO_CONFLITO_ASSINATURA_v2.27.1.md`: Documentação completa sobre o problema e solução
- `GUIA_INSTALACAO_ANDROID_STUDIO.md`: Guia passo a passo para configuração do ambiente
- Scripts de automação para instalação e configuração do Android Studio

### 🧪 Testes e Validação
- Scripts de teste para validação de funcionalidades
- Testes específicos para filtro de semanas e cálculo de períodos
- Relatório de validação de regressões

## Compatibilidade

### ✅ Versões Compatíveis
- Android 5.0+ (API Level 21+)
- Instalações anteriores do aplicativo
- Dispositivos com arquitetura ARM e x86

### 🔄 Processo de Atualização
- Usuários da v2.27.0 e v2.27.1: Atualização direta sem desinstalação
- Novos usuários: Instalação normal via APK
- Compatibilidade mantida com dados existentes

## Detalhes Técnicos

### 🔐 Assinatura do APK
- Tipo: Android App Bundle (.aab)
- Algoritmo: SHA-256 with RSA
- Validade: 25 anos
- Chave: Chave de lançamento padrão do projeto

### 🛠️ Build
- Versão do Gradle: 8.0
- Versão do Android Gradle Plugin: 8.1.0
- Kotlin: 1.8.20
- Compile SDK: 34
- Target SDK: 34
- Min SDK: 21

## Problemas Conhecidos

Nenhum problema conhecido nesta versão.

## Próximos Passos

- Monitoramento de instalações para confirmar resolução do conflito
- Planejamento para v2.28.0 com novas funcionalidades
- Otimização contínua de desempenho e estabilidade

## Agradecimentos

Agradecemos aos usuários que reportaram o problema de instalação e contribuíram para a rápida identificação e solução do conflito de assinaturas.

---

**Download**: [APK v2.27.2](app-release-v2.27.2.apk) *(disponível em breve)*

**Código Fonte**: [GitHub Repository](https://github.com/Lucasfmo1/Minhas-Compras-Android/tree/v2.27.2)

**Issues Relacionados**: [#ISSUE_NUMBER](https://github.com/Lucasfmo1/Minhas-Compras-Android/issues/ISSUE_NUMBER)