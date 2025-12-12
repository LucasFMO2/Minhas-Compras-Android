# Release Notes - v2.27.0

## 🐛 Correção: Estatísticas de Semana

Esta versão corrige um problema crítico nas estatísticas quando o filtro "Semana" era selecionado, garantindo que os dados sejam exibidos corretamente.

### ✨ Principais Mudanças

#### 📊 Correção nas Estatísticas de Semana
- **Alinhamento correto do período**: O período de semana agora está corretamente alinhado com o início da semana (segunda-feira)
- **Cálculo do período anterior**: Correção no cálculo do período anterior para comparação, garantindo que não haja sobreposição
- **Validação de períodos**: Adicionada validação robusta para garantir que todos os períodos sejam válidos antes de processar
- **Consistência entre componentes**: Unificação da lógica de cálculo de período entre diferentes componentes

### 🛠️ Melhorias Técnicas

#### Estatísticas
- **Alinhamento de semana**: Cálculo correto do início da semana atual (segunda-feira à meia-noite)
- **Validação de períodos**: Verificação de períodos inválidos antes de processar dados
- **Tratamento de erros**: Melhor tratamento de erros para evitar crashes
- **Cálculo do período anterior**: Lógica corrigida para calcular corretamente a semana anterior para comparação

### 📱 Detalhes da Versão

- **Versão**: 2.27.0
- **Version Code**: 77
- **Android mínimo**: 7.0 (API 24)
- **Android alvo**: 14 (API 34)

### 🔐 Segurança

- APK assinado digitalmente
- Keystore configurado para releases futuras

### 📝 Notas de Migração

- **Sem mudanças de dados**: Esta atualização não requer migração de banco de dados
- **Compatibilidade total**: Funciona perfeitamente com versões anteriores
- **Correção de bug**: Usuários que usavam o filtro "Semana" nas estatísticas terão a funcionalidade corrigida

### 🐛 Correções

- ✅ Correção no cálculo do período de semana nas estatísticas
- ✅ Correção no alinhamento do início da semana (segunda-feira)
- ✅ Correção no cálculo do período anterior para comparação
- ✅ Validação de períodos inválidos para evitar erros

### 🚀 Próximas Versões

- Templates de listas pré-configuradas (planejado)
- Sugestões inteligentes de categorias
- Compartilhamento de listas específicas
- Sincronização entre dispositivos (planejado)

---

**Data de Release**: 10/12/2025

**Desenvolvido com ❤️ para facilitar suas compras**

