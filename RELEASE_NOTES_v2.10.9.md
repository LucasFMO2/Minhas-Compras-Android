## Release v2.10.9

✨ **Correções de Bugs e Melhorias:**

### 🐛 **Correções de Bugs:**
- 🔧 **Correção na busca** - Busca case-insensitive agora funciona corretamente
- 🔧 **Correção no arquivamento automático** - Prevenção de loop infinito ao arquivar lista automaticamente
- 🔧 **Melhorias de segurança** - Remoção de usos inseguros de `!!` (non-null assertion)
- 🔧 **Validação de preço** - Prevenção de múltiplos separadores decimais no campo de preço

### 🛡️ **Melhorias de Segurança:**
- ✅ Sistema de logging otimizado - Logs de debug desabilitados em builds de release
- ✅ Tratamento seguro de null - Uso de safe calls (`?.let`) em vez de `!!`
- ✅ Validação melhorada - Prevenção de valores inválidos em campos de entrada

### 📝 **Outras Melhorias:**
- 📋 Configuração de backup otimizada
- 🧹 Limpeza de código e comentários

---

**Versão:** 2.10.9  
**Version Code:** 23  
**Data:** $(Get-Date -Format "dd/MM/yyyy")

