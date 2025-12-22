# 🔐 Automação de Criação de Conta no GitHub

Este script automatiza o preenchimento do formulário de registro do GitHub.

## ⚠️ Importante

O script **NÃO** pode completar todo o processo automaticamente porque:
- O GitHub usa captcha (proteção anti-bot)
- É necessário verificar o email manualmente
- Algumas etapas requerem interação humana

O script **apenas preenche o formulário** automaticamente. Você precisará:
1. Resolver o captcha manualmente
2. Verificar o email enviado para `lucasggfdd-166@yahoo.com`
3. Clicar no link de verificação no email

## 📋 Dados da Conta

- **Email:** lucasggfdd-166@yahoo.com
- **Username:** LucasFMO3
- **Senha:** 30N06n86*

## 🚀 Como Usar

### Opção 1: Usando o Script PowerShell (Recomendado para Windows)

1. Abra o PowerShell no diretório do projeto
2. Execute:
```powershell
.\criar-conta-github.ps1
```

O script irá:
- Verificar se Python está instalado
- Instalar as dependências necessárias
- Executar a automação

### Opção 2: Execução Manual

1. **Instalar dependências:**
```powershell
pip install -r requirements-github-automation.txt
```

2. **Executar o script:**
```powershell
python criar-conta-github.py
```

## 📦 Requisitos

- **Python 3.7+** instalado
- **Google Chrome** instalado
- **Conexão com internet** (para baixar o ChromeDriver automaticamente)

## 🔧 Dependências

- `selenium` - Para automação web
- `webdriver-manager` - Para gerenciar o ChromeDriver automaticamente

## 📝 O que o Script Faz

1. ✅ Abre o navegador Chrome
2. ✅ Acessa a página de registro do GitHub
3. ✅ Preenche o campo de email
4. ✅ Preenche o campo de senha
5. ✅ Preenche o campo de username
6. ✅ Aguarda você resolver o captcha
7. ✅ Mantém o navegador aberto para você completar o processo

## ⚡ Passos Após a Execução

1. **Resolva o captcha** se aparecer na tela
2. **Complete o processo de verificação** no navegador
3. **Verifique seu email** (`lucasggfdd-166@yahoo.com`)
4. **Clique no link de verificação** enviado pelo GitHub
5. **Complete o cadastro** no GitHub

## 🐛 Solução de Problemas

### Erro: "ChromeDriver não encontrado"
- O script usa `webdriver-manager` que baixa automaticamente o ChromeDriver
- Certifique-se de ter conexão com internet
- Verifique se o Google Chrome está instalado

### Erro: "Python não encontrado"
- Instale o Python de: https://www.python.org/downloads/
- Certifique-se de marcar "Add Python to PATH" durante a instalação

### Erro: "pip não encontrado"
- Reinstale o Python com a opção "Add Python to PATH"
- Ou instale pip manualmente

### O navegador não abre
- Verifique se o Google Chrome está instalado
- Tente executar o script como administrador
- Verifique se há atualizações pendentes do Chrome

## 📌 Notas

- O script mantém o navegador aberto após o preenchimento
- Você pode fechar o navegador manualmente quando terminar
- Os dados da conta estão hardcoded no script por segurança
- Para usar com outros dados, edite as variáveis no início do arquivo `criar-conta-github.py`

## 🔒 Segurança

⚠️ **IMPORTANTE:** Este script contém credenciais sensíveis. Não compartilhe este arquivo publicamente ou faça commit em repositórios públicos.

---

**Status:** Pronto para uso
**Próximo passo:** Execute o script e complete a verificação manualmente

