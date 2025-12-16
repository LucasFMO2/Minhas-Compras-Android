# Diagrama de Migração do Repositório

## Fluxo de Migração

```mermaid
graph TD
    A[Repositório Atual<br/>roseanerosafmo-sketch/Minhas-Compras-Android] --> B[Backup Local<br/>git bundle]
    A --> C[Novo Repositório<br/>seu-usuario-github/Minhas-Compras-Android]
    
    B --> D[Configurar Credenciais<br/>Nova conta pessoal]
    D --> E[Atualizar Remote<br/>git remote set-url]
    E --> F[Push Completo<br/>branches + tags + histórico]
    
    F --> G[Verificação<br/>Testar autenticação]
    G --> H[Limpeza<br/>Remover referências antigas]
    
    H --> I[Repositório Migrado<br/>Funcionando na nova conta]
    
    style A fill:#ffcccc
    style I fill:#ccffcc
    style C fill:#ccccff
```

## Comparação: Migração Manual vs Transferência Direta

```mermaid
graph LR
    subgraph "Migração Manual"
        A1[Criar novo repo] --> A2[Configurar credenciais]
        A2 --> A3[Push manual]
        A3 --> A4[Reconfigurar integrações]
    end
    
    subgraph "Transferência Direta"
        B1[Settings → Transfer] --> B2[Confirmar transferência]
        B2 --> B3[Integrações migradas]
    end
    
    A1 --> B1
    A4 --> B3
    
    style A1 fill:#ffcccc
    style A2 fill:#ffcccc
    style A3 fill:#ffcccc
    style A4 fill:#ffcccc
    style B1 fill:#ccffcc
    style B2 fill:#ccffcc
    style B3 fill:#ccffcc
```

## Arquitetura Atual vs Futura

```mermaid
graph TB
    subgraph "Situação Atual"
        A1[Desenvolvedor Local] --> A2[Remote: roseanerosafmo-sketch]
        A2 --> A3[GitHub: Organização]
    end
    
    subgraph "Situação Futura"
        B1[Desenvolvedor Local] --> B2[Remote: seu-usuario-github]
        B2 --> B3[GitHub: Conta Pessoal]
    end
    
    A1 -.->|Migração| B1
    A2 -.->|Atualização| B2
    A3 -.->|Transferência| B3
    
    style A2 fill:#ffcccc
    style B2 fill:#ccffcc
```

## Fluxo de Autenticação

```mermaid
sequenceDiagram
    participant Dev as Desenvolvedor
    participant Git as Git Local
    participant GH as GitHub
    participant Cred as Gerenciador de Credenciais
    
    Dev->>Cred: Remover credenciais antigas
    Dev->>Git: git config --global user.email
    Dev->>Git: git remote set-url origin
    Dev->>Git: git push
    
    Git->>Cred: Solicitar autenticação
    Cred->>GH: Apresentar credenciais novas
    GH-->>Cred: Token de acesso
    Cred-->>Git: Autenticação bem-sucedida
    Git-->>Dev: Push concluído com sucesso