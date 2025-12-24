# Release v2.17.7

## 🎉 Novidades

### ✨ Sistema de Arquivamento Atualizado para Múltiplas Listas

#### 🗂️ Filtro de Histórico por Lista
- **Filtro "Lista Ativa"**: Visualize apenas o histórico da lista de compras ativa atual
- **Filtro "Todas"**: Veja histórico completo de todas as suas listas de compras
- **Navegação Intuitiva**: Fácil alternância entre os filtros através de chips na barra superior
- **Nome da Lista**: O filtro mostra dinamicamente o nome da lista ativa selecionada

#### 🎯 Melhorias na Experiência
- Histórico agora é automaticamente filtrado pela lista ativa ao abrir a tela
- Cada lista mantém seu próprio histórico separado
- Interface mais organizada e clara para gerenciar múltiplas listas

## 🔧 Mudanças Técnicas

- Versão: **2.17.7** (versionCode: 73)
- Sistema de arquivamento agora trabalha corretamente com múltiplas listas
- Repository atualizado com métodos de filtro por `listId`
- ViewModel atualizado para suportar filtros reativos
- UI aprimorada com FilterChips para seleção de filtro

## 📝 Notas

Esta atualização melhora significativamente o gerenciamento de histórico quando você usa múltiplas listas de compras. Agora você pode facilmente visualizar o histórico específico de cada lista ou ver tudo junto quando necessário.

### Como Usar

1. **Ver Histórico da Lista Ativa**: O app já vem configurado para mostrar apenas a lista ativa por padrão
2. **Ver Todas as Listas**: Toque no chip "Todas" na barra superior para ver histórico completo
3. **Voltar para Lista Ativa**: Toque no chip com o nome da lista (ex: "Minhas Compras") para filtrar novamente

## 🐛 Correções

- Correção no sistema de arquivamento para funcionar corretamente com múltiplas listas
- Histórico agora associa corretamente cada arquivamento à sua lista de origem

## 📦 Instalação

Baixe o APK abaixo e instale no seu dispositivo Android.

### Informações Técnicas
- Version Code: 73
- Version Name: 2.17.7
- Target SDK: 34
- Min SDK: 24

### Links
- Ver código-fonte: https://github.com/LucasFMO2/Minhas-Compras-Android
- Reportar problema: https://github.com/LucasFMO2/Minhas-Compras-Android/issues

