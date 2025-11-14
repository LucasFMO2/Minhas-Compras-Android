# Release v2.11.5 - BottomBar com Total a Pagar

## 💰 Nova Funcionalidade de Totalização

Esta versão adiciona uma **BottomBar** que exibe o **Total a Pagar** dos itens pendentes, facilitando o controle financeiro durante as compras.

### ✨ Principais Melhorias

#### 💵 BottomBar com Total a Pagar
- **Cálculo automático** - Soma automaticamente os preços dos itens não comprados
- **Atualização em tempo real** - Recalcula quando itens são adicionados, removidos ou marcados como comprados
- **Formatação em reais** - Valor formatado como moeda brasileira (R$)
- **Posicionamento estratégico** - Aparece abaixo do botão "+" na parte inferior da tela

#### 🎯 Funcionalidades
- **Visibilidade inteligente** - Só aparece quando há itens pendentes com preço
- **Cálculo preciso** - Considera quantidade de cada item (preço × quantidade)
- **Design limpo** - Interface minimalista que não interfere na experiência

#### 🎨 Design
- **Material Design 3** - Segue as diretrizes do Material Design
- **Elevação visual** - Sombra sutil para destacar a barra
- **Cores do tema** - Adapta-se automaticamente ao tema claro/escuro
- **Tipografia clara** - Texto "Total a Pagar" e valor em destaque

### 📝 Detalhes Técnicos
- Adicionado cálculo `totalAPagar` usando `remember` para otimização
- Filtra apenas itens não comprados (`!it.comprado`)
- Soma considera quantidade: `(preco ?: 0.0) * quantidade`
- Formatação usando `NumberFormat.getCurrencyInstance(Locale("pt", "BR"))`
- BottomBar condicional: só aparece quando `allItens.isNotEmpty() && totalAPagar > 0`

### 🔄 Compatibilidade
- Mantém todas as funcionalidades anteriores
- Não interfere com outras funcionalidades
- Compatível com versões anteriores do Android

### 💡 Benefícios
- **Controle financeiro** - Usuário vê o total a pagar sem precisar calcular manualmente
- **Experiência melhorada** - Informação importante sempre visível
- **Praticidade** - Facilita o planejamento de compras

---

**Versão:** 2.11.5  
**Version Code:** 48

