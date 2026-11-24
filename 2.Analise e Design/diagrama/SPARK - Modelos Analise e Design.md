# Modelos de Análise e Design — SmartPark (Room v3)

## 1. Modelo Entidade-Relacionamento (Banco Local Room)

```mermaid
erDiagram
    SERVIDOR ||--o| USUARIO : "possui conta (opcional)"
    SERVIDOR ||--o{ VEICULO : "possui"
    VEICULO ||--o{ REGISTRO_MOVIMENTACAO : "registra"

    USUARIO {
        long id PK
        string nome
        string login UK
        string senha_hash
        string perfil "ADMIN | PORTARIA | SERVIDOR"
        long servidor_id FK "nullable"
        boolean ativo
    }

    SERVIDOR {
        long id PK
        string nome
        string prontuario UK
        string email
        string telefone
        string setor
        boolean ativo
    }

    VEICULO {
        long id PK
        long servidor_id FK
        string placa UK
        string modelo
        string cor
        string tipo_cadastro "PERMANENTE | TEMPORARIO"
        long validade_inicio_millis "nullable"
        long validade_fim_millis "nullable"
        boolean ativo
    }

    REGISTRO_MOVIMENTACAO {
        long id PK
        long veiculo_id FK "nullable"
        string placa_registrada
        boolean autorizado
        long data_hora_entrada_millis
        long data_hora_saida_millis "nullable"
        string turno "MANHA | TARDE | NOITE"
        string tipo_vaga "COMUM | EXCLUSIVA_PCD | EXCLUSIVA_IDOSO | EXCLUSIVA_OFICIAL"
        string observacao
    }

    VISITANTE {
        long id PK
        string nome
        string documento
        string instituicao_origem
        string telefone
        string placa
        string modelo_veiculo
        string cor_veiculo
        string motivo_visita
        string setor_destino
        string tipo_vaga
        string turno
        string registrado_por
        long data_hora_entrada_millis
        long data_hora_saida_millis "nullable"
        string observacao
    }
```

---

## 2. Arquitetura em Camadas por Domínio

```mermaid
flowchart TD
    UI["Camada UI (Jetpack Compose + ViewModels)"]
    SRV["Camada Service (Regras de Negócio + Validações + Exceções)"]
    REPO["Camada Repository + DAOs (Room)"]
    DB[("SQLite / SmartParkDatabase v3")]

    UI --> SRV
    SRV --> REPO
    REPO --> DB
```

---

## 3. Diagrama de Sequência — Consulta de Placa e Redirecionamento para Visitante

```mermaid
sequenceDiagram
    actor Porteiro
    participant PortariaScreen
    participant VeiculoService
    participant VisitanteService
    participant RoomDB

    Porteiro->>PortariaScreen: Digita placa "IFG8B24" e toca em Consultar
    PortariaScreen->>VeiculoService: consultarPlaca("IFG8B24")
    VeiculoService->>RoomDB: buscarPorPlaca("IFG8B24")
    RoomDB-->>VeiculoService: null (NAO_CADASTRADO)
    VeiculoService-->>PortariaScreen: ResultadoConsultaPlaca(NAO_CADASTRADO)
    Porteiro->>PortariaScreen: Toca em "Registrar como visitante externo"
    PortariaScreen->>VisitanteService: registrarEntrada(VisitanteRequest)
    VisitanteService->>RoomDB: inserir(Visitante)
    RoomDB-->>VisitanteService: id gerado
    VisitanteService-->>Porteiro: Visitante exibido em "No campus agora" e somado no Painel
```
