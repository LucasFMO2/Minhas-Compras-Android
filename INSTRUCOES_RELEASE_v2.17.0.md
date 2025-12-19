# Instruções para Criar Release v2.17.0 no GitHub

## Status Atual
- ✅ Código enviado para o GitHub (branch: revertido-para-v2.16.0)
- ✅ Tag v2.17.0 criada e enviada
- ✅ APK gerado: app-release-v2.17.0.apk (13 MB)
- ❌ Release público ainda não criado

## Instruções Manuais

### 1. Acessar o GitHub
Abra o navegador e acesse:
```
https://github.com/mfc46224-jpg/Minhas-Compras-Android/releases/new
```

### 2. Preencher Informações do Release

#### Tag:
- Selecione `v2.17.0` no dropdown
- Ou digite `v2.17.0` manualmente

#### Título:
```
Release v2.17.0
```

#### Descrição:
```
## Release v2.17.0

**Atualizações e Melhorias:**
- **Melhorias na Bottom Bar** - Removido elemento 'Pago' e mantido apenas 'Total' e 'A Pagar'
- **Formatação consistente** - Aplicada formatação bodySmall + FontWeight.Medium na Bottom Bar
- **Cálculo automático em tempo real** - Implementado no diálogo AdicionarItem
- **Correção de quebra de linha** - Aplicado maxLines e overflow no valor total do diálogo
- **Padronização monetária** - Formatação monetária consistente em todo o app
- **Melhorias de responsividade** - Interface mais adaptável a diferentes telas

**APK Information:**
- Versão: v2.17.0
- Tamanho: 13 MB
- Build: Release
```

### 3. Anexar o APK

#### Localização do APK:
```
C:\Users\nerdd\Desktop\Minhas-Compras-Android\app-release-v2.17.0.apk
```

#### Para anexar:
1. Clique na área "Attach binaries by dropping them here or selecting them"
2. OU arraste o arquivo `app-release-v2.17.0.apk` para essa área
3. OU clique em "selecting them" e navegue até o arquivo

### 4. Publicar
Clique no botão verde **"Publish release"** no final da página

## Verificação Final

Após publicar, o release estará disponível em:
```
https://github.com/mfc46224-jpg/Minhas-Compras-Android/releases/tag/v2.17.0
```

O APK estará disponível para download nessa página.

## Resumo das Mudanças

### Bottom Bar:
- Removido elemento "Pago"
- Mantido apenas "Total" e "A Pagar"
- Aplicada formatação consistente (bodySmall + FontWeight.Medium)

### Diálogo AdicionarItem:
- Implementado cálculo automático em tempo real
- Corrigida quebra de linha no valor total com maxLines e overflow

### Interface Geral:
- Padronizada formatação monetária em todo o app
- Melhorias de responsividade para diferentes tamanhos de tela