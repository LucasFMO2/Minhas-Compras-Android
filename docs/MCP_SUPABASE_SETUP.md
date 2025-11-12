# Configuração do MCP Supabase no Cursor

O servidor MCP do Supabase foi configurado no arquivo `c:\Users\nerdd\.cursor\mcp.json`.

## Configuração Atual

O token de acesso pessoal já foi configurado:
- **Token**: `sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9`

## ⚠️ Ação Necessária: Configurar SUPABASE_PROJECT_REF

Para que o MCP do Supabase funcione completamente, você precisa configurar o `SUPABASE_PROJECT_REF`.

### Como Obter o SUPABASE_PROJECT_REF:

1. Acesse o painel do Supabase: https://app.supabase.com
2. Selecione seu projeto
3. O `project_ref` pode ser encontrado de duas formas:
   - **Na URL do projeto**: Se a URL do seu projeto é `https://abc123xyz.supabase.co`, então o `project_ref` é `abc123xyz`
   - **Nas configurações**: Vá em **Settings > General > Reference ID**

### Como Configurar:

**Opção 1: Substituir diretamente no arquivo**

Edite o arquivo `c:\Users\nerdd\.cursor\mcp.json` e substitua `${SUPABASE_PROJECT_REF}` pelo valor real:

```json
"url": "https://mcp.supabase.com/mcp?project_ref=seu-project-ref-aqui",
"env": {
  "SUPABASE_PROJECT_REF": "seu-project-ref-aqui",
  "SUPABASE_ACCESS_TOKEN": "sbp_ab7d4165b84bdcf2c3d90d72bccd3797897eb2e9"
}
```

**Opção 2: Usar variável de ambiente do sistema**

1. Configure a variável de ambiente `SUPABASE_PROJECT_REF` no Windows:
   - Abra "Variáveis de Ambiente" nas configurações do Windows
   - Adicione `SUPABASE_PROJECT_REF` com o valor do seu project reference
2. Reinicie o Cursor para que a variável seja carregada

## Reiniciar o Cursor

Após configurar o `SUPABASE_PROJECT_REF`, **reinicie o Cursor** para que as mudanças tenham efeito.

## Verificação

Após reiniciar, o servidor MCP do Supabase deve aparecer na lista de servidores MCP disponíveis no Cursor.

## Recursos

- [Documentação do MCP Supabase](https://supabase.com/docs/guides/mcp)
- [Painel do Supabase](https://app.supabase.com)

