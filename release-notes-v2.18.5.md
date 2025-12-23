## Release v2.18.5 - Correção de Reutilização de Lista Arquivada

### 🐛 Correção Crítica

Esta versão corrige um problema crítico onde ao reutilizar uma lista arquivada, os itens desapareciam completamente da lista.

### 🔧 Problema Identificado e Solução

**Problema Anterior:**
- Ao reutilizar lista arquivada, os itens desapareciam porque eram deletados ao arquivar mas não eram copiados de volta
- Lista arquivada era "excluída" do histórico em vez de voltar para listas ativas

**Solução Implementada:**
- `reuseHistoryList()` agora copia os itens de volta para a lista arquivada (não para lista ativa atual)
- Lista arquivada é desarquivada e selecionada como ativa
- Histórico é mantido para reutilização futura
- Funcionalidade de reutilização funciona corretamente

### ✅ Melhorias

- **Reutilização Funcional**: Listas arquivadas recuperam seus itens corretamente
- **Experiência Consistente**: Usuários podem reutilizar listas arquivadas sem perder dados
- **Histórico Preservado**: Lista permanece no histórico após reutilização
- **Compatibilidade**: Mantém funcionamento com listas arquivadas de versões anteriores

### 📋 Detalhes Técnicos

- **Version Code**: 83
- **Version Name**: 2.18.6
- **Target SDK**: 34
- **Min SDK**: 24

### 🔄 Compatibilidade

Esta versão é compatível com todas as versões anteriores. Usuários podem atualizar sem perder dados existentes.

### 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

---

**Data de Release**: Dezembro 2024
**Compatibilidade**: Android 7.0+ (API 24+)
