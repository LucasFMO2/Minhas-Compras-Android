# 🚀 Deploy para GitHub - Minha Lista de Compras

## Passo 1: Criar Repositório no GitHub

1. Acesse [github.com](https://github.com) e faça login
2. Clique no botão **"New"** ou **"+"** → **"New repository"**
3. Configure:
   - **Repository name**: `minha-lista-de-compras`
   - **Description**: `Aplicativo Android para gerenciar lista de compras`
   - **Visibility**: ✅ **Private** (marcar como privado)
   - **Initialize**: ❌ **NÃO** marcar nenhuma opção
4. Clique em **"Create repository"**

## Passo 2: Executar Comandos

Após criar o repositório, execute os comandos abaixo (substitua `SEU_USUARIO` pelo seu username):

```bash
# Conectar ao repositório remoto
git remote add origin https://github.com/SEU_USUARIO/minha-lista-de-compras.git

# Enviar código e tags
git push -u origin main
git push origin v1.0.0
```

## Passo 3: Criar Release no GitHub

1. No GitHub, vá para o seu repositório
2. Clique em **"Releases"** (lado direito)
3. Clique em **"Create a new release"**
4. Configure:
   - **Tag version**: `v1.0.0`
   - **Release title**: `🚀 Minha Lista de Compras v1.0.0`
   - **Description**: Cole o conteúdo do arquivo `docs/CHANGELOG.md`
   - **Set as the latest release**: ✅ Marcar
5. Clique em **"Publish release"**

## ✅ Status Atual

- ✅ Repositório Git inicializado
- ✅ .gitignore criado
- ✅ README.md criado
- ✅ Commit inicial feito
- ✅ Tag v1.0.0 criada
- ✅ Documentação completa em docs/CHANGELOG.md

**Próximo**: Execute os comandos acima após criar o repositório no GitHub!
