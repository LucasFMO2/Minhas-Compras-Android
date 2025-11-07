# 🛒 Minhas Compras

Um aplicativo Android moderno e intuitivo para gerenciar sua lista de compras, desenvolvido com Kotlin e Jetpack Compose.

## 📥 Download

### 🆕 Versão 2.4 (Mais Recente)

**[⬇️ Baixar APK v2.4](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v2.4/MinhasCompras-v2.4-release.apk)**

✨ **Novidades da v2.4:**
- 🏷️ Sistema de categorias para organizar itens (Frutas e Verduras, Laticínios, Carnes, etc.)
- 📊 Migração automática do banco de dados preservando dados existentes
- 🎯 Dropdown de categorias no dialog de adicionar/editar item
- ✨ Melhor organização e classificação dos itens de compra

### 📦 Versões Anteriores

**Versão 2.3:**
**[⬇️ Baixar APK v2.3](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v2.3/MinhasCompras-v2.3-release.apk)**
- 🔧 Correção do botão "+ Adicionar" sobrepondo itens da lista
- 📱 Melhor experiência de navegação na lista de compras
- ✨ Interface mais polida e funcional

### 📦 Versões Anteriores

**Versão 2.2:**
**[⬇️ Baixar APK v2.2](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v2.2/MinhasCompras-v2.2-release.apk)**
- 📐 Ajuste de padding nos cards de estatísticas
- 💰 Valores monetários cabem melhor sem quebrar
- 🎨 Melhor aproveitamento do espaço nos cards

**Versão 2.1:**
**[⬇️ Baixar APK v2.1](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v2.1/MinhasCompras-v2.1-release.apk)**
- 📱 Responsividade aprimorada nos cards de estatísticas
- 💰 Valores monetários completos (sem cortes)
- 🎨 Interface otimizada para diferentes tamanhos de tela

**Versão 2.0:**
**[⬇️ Baixar APK v2.0](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v2.0/MinhasCompras-v2.0-release.apk)**
- 💰 Adicione preços aos itens
- ✏️ Edite itens da lista
- 📊 Estatísticas de preços e totais

**Versão 1.0:**

**[⬇️ Baixar APK v1.0](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/download/v1.0/MinhasCompras-v1.0-release.apk)**

📱 **Instalação:** Baixe o APK e instale no seu dispositivo Android. Certifique-se de permitir instalação de fontes desconhecidas nas configurações de segurança.

🔗 **[Ver todas as releases](https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases)**

## ✨ Funcionalidades

- ✅ **Adicionar itens** - Crie itens com nome e quantidade
- 🎯 **Marcar como comprado** - Marque itens como comprados com um simples toque
- 📊 **Estatísticas** - Visualize total de itens, pendentes e comprados
- 📈 **Barra de progresso** - Acompanhe seu progresso de compras
- 🗑️ **Deletar itens** - Remova itens individuais ou limpe todos os comprados
- 💾 **Persistência local** - Seus dados são salvos localmente no dispositivo
- 🎨 **Interface moderna** - Design Material 3 com animações suaves

## 🛠️ Tecnologias

- **Kotlin** - Linguagem de programação
- **Jetpack Compose** - Framework de UI declarativa
- **Room Database** - Persistência de dados local
- **ViewModel** - Gerenciamento de estado
- **Material Design 3** - Design system moderno
- **Coroutines** - Programação assíncrona

## 📱 Requisitos

- Android 7.0 (API 24) ou superior
- Android Studio Hedgehog ou superior

## 🚀 Como usar

1. Clone o repositório:
```bash
git clone https://github.com/nerddescoladofmo-cmyk/Minhas-Compras-Android.git
```

2. Abra o projeto no Android Studio

3. Sincronize o Gradle e aguarde o download das dependências

4. Execute o app em um emulador ou dispositivo físico

## 📦 Estrutura do Projeto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/minhascompras/
│   │   │   ├── data/           # Modelos, DAO e Database
│   │   │   ├── ui/
│   │   │   │   ├── components/ # Componentes reutilizáveis
│   │   │   │   ├── screens/    # Telas da aplicação
│   │   │   │   ├── theme/      # Temas e cores
│   │   │   │   └── viewmodel/  # ViewModels
│   │   │   └── MainActivity.kt
│   │   └── res/                # Recursos (strings, imagens, etc)
```

## 🎨 Componentes Principais

- **ListaComprasScreen** - Tela principal com lista de itens
- **ItemCompraCard** - Card individual para cada item
- **AdicionarItemDialog** - Diálogo para adicionar novos itens
- **EstadoVazioScreen** - Tela exibida quando não há itens
- **StatisticCard** - Card de estatísticas

## 📄 Licença

Este projeto está disponível para uso pessoal e educacional.

## 👨‍💻 Desenvolvido por

Projeto desenvolvido como exemplo de aplicativo Android moderno com as melhores práticas.

---

⭐ Se este projeto foi útil para você, considere dar uma estrela!
