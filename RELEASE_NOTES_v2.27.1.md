# Release v2.27.1 - Correção do Filtro de Semanas

## 🐛 Correções Implementadas

### Problema Resolvido
Corrigido o problema no filtro de semanas nas estatísticas, que estava causando cálculos incorretos e inconsistências na exibição de dados.

### Alterações Técnicas

#### 1. Adicionado DateUtils.kt com funções centralizadas
- Criado novo arquivo utilitário para manipulação de datas
- Implementadas funções centralizadas para cálculo de períodos
- Padronizada a manipulação de datas em todo o aplicativo

#### 2. Simplificado cálculo do período anterior em StatisticsScreen.kt
- Refatorada a lógica de cálculo de períodos
- Utilizadas as novas funções centralizadas do DateUtils
- Melhorada a precisão nos cálculos de períodos anteriores

#### 3. Atualizado PeriodFilterChips.kt para usar função centralizada
- Migrada a lógica de filtragem para usar as funções do DateUtils
- Melhorada a consistência na aplicação de filtros de período
- Reduzida a duplicação de código

#### 4. Implementado validações robustas em StatisticsViewModel.kt
- Adicionadas validações mais rigorosas para períodos de data
- Melhorado o tratamento de edge cases
- Implementado fallback para valores padrão quando necessário

#### 5. Corrigidos erros de contentPadding em ListaComprasScreen.kt
- Resolvido problema de layout na tela de lista de compras
- Ajustado o espaçamento interno para melhor experiência visual
- Melhorada a responsividade da interface

## 📱 Instalação

### Como instalar o APK
1. Baixe o arquivo `app-release-v2.27.1.apk` deste release
2. No seu dispositivo Android, vá para Configurações > Segurança
3. Ative "Fontes desconhecidas" ou "Instalar apps desconhecidos"
4. Abra o arquivo APK baixado e siga as instruções de instalação

### Requisitos
- Android 5.0 (API level 21) ou superior
- Espaço de armazenamento: aproximadamente 15MB

## 🔍 Verificação

Após a instalação, verifique se:
- O filtro de semanas nas estatísticas está funcionando corretamente
- Os períodos são calculados com precisão
- A interface da lista de compras está sem erros de layout

---

**Versão:** v2.27.1  
**Data:** 12/12/2025  
**Tipo:** Correção de Bugs  
**APK:** app-release-v2.27.1.apk (13.7MB)