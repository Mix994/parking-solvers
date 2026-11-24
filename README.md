# SmartPark — Parking Solvers

Sistema de gestão e monitoramento do estacionamento para Institutos Federais (Projeto Final de Programação de Dispositivos Móveis — PDIM — BCC — 2026-2).

**Equipe:** Caio Emanuel, Lucas Nunes, Luiza Bueno e Michel Amancio  
**Cliente (Product Owner):** Prof. Fábio Santos

---

## Estrutura do Repositório

Organizado segundo o processo ágil **SpinOff** e o padrão de engenharia de software do repositório de referência:

```text
.
├── .github/
│   ├── ISSUE_TEMPLATE/             # Templates de issues (feature, bugfix, refactor)
│   └── pull_request_template.md    # Template padrão de Pull Request
├── 1.Requisitos/
│   ├── Casos de Uso/               # Especificação de Casos de Uso (SPARK-US01 a US07)
│   ├── Prototipo/                  # Fluxo e especificação visual das telas
│   ├── SPARK - TAP.md              # Termo de Abertura do Projeto (v1.0)
│   └── SPARK - Visao.md            # Documento de Visão, Necessidades e Suposições
├── 2.Analise e Design/
│   ├── adr/                        # Architecture Decision Records (ADR-0001 a ADR-0003)
│   └── diagrama/                   # Modelos de Análise, ER do Room v3 e Sequência
├── 3.Implementacao/                # Submódulo Git -> parking-solvers-implementacao (Kotlin, Compose, Room v3, JUnit)
├── 4.Teste/
│   ├── Massa de Testes/            # Scripts SQLite (Limpeza, Massa de Testes e Conferência)
│   └── SPARK - Roteiro de Testes.md
├── 5.Implantação/
│   ├── SPARK - Guia de Implantacao.md
│   └── SPARK - Manual do Usuario.md
├── 6.Gerenciamento de Projeto/
│   ├── Atas/                       # Atas de Reunião e Revisões de Sprint (I, II e III)
│   ├── SPARK - Checklist de Verificacao.md
│   ├── SPARK - Planejamento e Controle do Projeto.md
│   └── SPARK - Relatorio de Codigo.md
├── LICENSE
└── README.md
```

---

## Contas Iniciais no Banco Local Room (`usuario`)

| Nível (`PerfilUsuario`) | Login (ou Prontuário) | Senha Inicial | Permissões |
| :--- | :--- | :---: | :--- |
| **Administração (`ADMIN`)** | `admin@campus.edu.br` | `admin123` | Painel, Portaria, Visitantes, Veículos e Gestão (Servidores + Contas) |
| **Portaria (`PORTARIA`)** | `portaria@campus.edu.br` | `portaria123` | Portaria, Visitantes, Veículos e Painel |
| **Servidor (`SERVIDOR`)** | `ana.souza@campus.edu.br` ou `BP102401` | `123456` | Meus Veículos + Meus Dados |
| **Servidor (`SERVIDOR`)** | `carlos.lima@campus.edu.br` ou `BP102402` | `123456` | Meus Veículos + Meus Dados |
| **Servidor (`SERVIDOR`)** | `fernanda.oliveira@campus.edu.br` ou `BP102403` | `123456` | Meus Veículos + Meus Dados |

---

## Como Clonar, Abrir, Compilar e Testar

Como `3.Implementacao` é um submódulo Git, clone usando `--recurse-submodules`:

```powershell
git clone --recurse-submodules https://github.com/Mix994/parking-solvers.git
```

Depois:
1. Abra a pasta `3.Implementacao/` no **Android Studio**.
2. Ou execute via terminal dentro de `3.Implementacao/`:

```powershell
cd "3.Implementacao"
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```
