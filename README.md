# 📱 Minha Lista de Compras

Um aplicativo Android moderno e intuitivo para gerenciar sua lista de compras, desenvolvido com Jetpack Compose e Material Design 3.

## ✨ Funcionalidades

- ✅ **Gerenciamento completo de itens**
  - Adicionar novos itens
  - Editar itens existentes
  - Remover itens
  - Marcar como comprado/pendente

- 📊 **Estatísticas em tempo real**
  - Total de itens
  - Itens comprados
  - Itens pendentes

- 🎨 **Interface moderna**
  - Material Design 3
  - Interface responsiva
  - Animações suaves
  - Tema adaptativo

- 📝 **Informações detalhadas**
  - Nome do item
  - Quantidade
  - Categoria
  - Status de compra

## 🚀 Tecnologias

- **Kotlin** - Linguagem principal
- **Jetpack Compose** - Framework de UI
- **Material Design 3** - Sistema de design
- **Android SDK 24+** - Compatibilidade

## 📱 Compatibilidade

- **Android 7.0+** (API 24+)
- **Target SDK 36** (Android 14)

## 🏗️ Estrutura do Projeto

```
app/
├── src/main/java/com/example/minhalistadecompras/
│   ├── data/
│   │   └── ItemCompra.kt          # Modelo de dados
│   ├── ui/theme/                  # Tema e cores
│   └── MainActivity.kt            # Activity principal
├── src/main/res/
│   └── values/
│       └── strings.xml            # Strings em português
└── build.gradle.kts               # Configurações do módulo
```

## 🎯 Como Usar

1. **Adicionar item**: Toque no botão + (flutuante)
2. **Editar item**: Toque no ícone de edição no item
3. **Remover item**: Toque no ícone de lixeira no item
4. **Marcar como comprado**: Toque no checkbox do item

## 📋 Dados de Exemplo

O aplicativo inclui itens de exemplo para demonstração:
- Leite, Pão, Ovos, Arroz, Feijão, Banana, Maçã, Frango

## 🔮 Próximas Funcionalidades

- [ ] Persistência de dados com Room Database
- [ ] Categorias personalizáveis
- [ ] Filtros e busca
- [ ] Ordenação de itens
- [ ] Compartilhamento de listas
- [ ] Notificações e lembretes
- [ ] Backup e sincronização
- [ ] Temas personalizáveis

## 📄 Licença

Este projeto é privado e proprietário.

---

**Versão**: 1.0.0  
**Última atualização**: Dezembro 2024
