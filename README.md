# SmartPark — Parking Solvers

Sistema de gestão e monitoramento do estacionamento para Institutos Federais (Projeto Final de Programação de Dispositivos Móveis — PDIM — BCC — 2026-2).

**Equipe:** Caio Emanuel, Lucas Nunes, Luiza Bueno e Michel Amancio  
**Cliente (Product Owner):** Prof. Fábio Santos

---

## Onde Localizar o APK Executável do App

O instalador Android (`.apk`) pronto para testar no celular ou no emulador fica disponível em **`5.Implantação/SmartPark-v1.0.apk`**:

- **Download direto pelo GitHub:** [`5.Implantação/SmartPark-v1.0.apk`](5.Implanta%C3%A7%C3%A3o/SmartPark-v1.0.apk) (abra o arquivo e clique em **Download / Download raw file**).
- **Caminho após compilar localmente:** `3.Implementacao/app/build/outputs/apk/debug/app-debug.apk`.
- **Requisitos para instalar:** dispositivo ou emulador com **Android 7.0 (API 24) ou superior**. Ao abrir o `.apk` no celular, autorize *"Instalar apps desconhecidos"* para o navegador e toque em **Instalar**.

---

## Funcionalidades Principais

- **Controle de acesso por 3 níveis (`usuario` no Room):** Administração (`ADMIN`), Portaria (`PORTARIA`) e Servidor/Docente (`SERVIDOR`), com senhas em hash SHA-256 + salt, proteção contra força bruta (`LoginAttemptService`: 5 falhas = bloqueio de 15 min) e edição de perfil em **Meus dados**.
- **Cadastro de servidores e veículos permanentes/temporários:** validação de placas brasileiras padrão antigo (`AAA-1234`) e Mercosul (`ABC1D23`), exibição com o componente visual `PlacaMercosulChip` e controle automático de vigência temporária.
- **Portaria e visitantes externos (`visitante`):** consulta instantânea de placas, registro de entradas e saídas de servidores e ficha completa de visitantes de outros campi, empresas e convidados (CPF/RG, instituição de origem, destino, motivo e baixa de saída).
- **Vagas para carregamento de veículos elétricos (expansível):** inicia configurada com **2 vagas de carregamento elétrico** no banco local (`configuracao_estacionamento`), com bloqueio automático na portaria/visitantes quando lotadas e tela para a **Administração expandir facilmente** para mais estações de recarga sem alterar código.
- **Painel de ocupação:** lotação em tempo real (servidores + visitantes), card dedicado de carregamento elétrico, vagas exclusivas (PCD, Idoso e Oficial) e média diária por turno filtrada por *Hoje*, *7 dias* ou *Tudo*.

---

## Estrutura do Repositório

Organizado segundo o processo ágil **SpinOff** e o padrão do repositório de referência (`C4103M/carToon`):

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
│   └── diagrama/                   # Modelos de Análise, ER do Room v4 e Sequência
├── 3.Implementacao/                # Submódulo Git -> parking-solvers-implementacao (Kotlin, Compose, Room v4, JUnit)
├── 4.Teste/
│   ├── Massa de Testes/            # Scripts SQLite (Limpeza, Massa de Testes e Conferência)
│   └── SPARK - Roteiro de Testes.md
├── 5.Implantação/
│   ├── SmartPark-v1.0.apk          # Executável Android pronto para instalação
│   ├── SPARK - Guia de Implantacao.md
│   └── SPARK - Manual do Usuario.md
├── 6.Gerenciamento de Projeto/
│   ├── Atas/                       # Atas de Reunião e Revisões de Sprint (I, II e III)
│   ├── SPARK - Checklist de Verificacao.md
│   ├── SPARK - Planejamento e Controle do Projeto.md
│   └── SPARK - Relatorio de Codigo.md
├── .gitmodules
├── LICENSE
└── README.md
```

---

## Contas Iniciais no Banco Local Room (`usuario`)

| Nível (`PerfilUsuario`) | Login (ou Prontuário) | Senha Inicial | Permissões |
| :--- | :--- | :---: | :--- |
| **Administração (`ADMIN`)** | `admin@campus.edu.br` | `admin123` | Painel (com expansão de vagas), Portaria, Visitantes, Veículos e Gestão |
| **Portaria (`PORTARIA`)** | `portaria@campus.edu.br` | `portaria123` | Portaria, Visitantes, Veículos e Painel |
| **Servidor (`SERVIDOR`)** | `ana.souza@campus.edu.br` ou `BP102401` | `123456` | Meus Veículos + Meus Dados |
| **Servidor (`SERVIDOR`)** | `carlos.lima@campus.edu.br` ou `BP102402` | `123456` | Meus Veículos + Meus Dados |
| **Servidor (`SERVIDOR`)** | `fernanda.oliveira@campus.edu.br` ou `BP102403` | `123456` | Meus Veículos + Meus Dados |

---

## Como Clonar, Compilar e Testar

Como `3.Implementacao` é um submódulo Git, clone usando `--recurse-submodules`:

```powershell
git clone --recurse-submodules https://github.com/Mix994/parking-solvers.git
```

Depois:
1. Abra a pasta `3.Implementacao/` no **Android Studio**, ou
2. Execute via terminal dentro de `3.Implementacao/`:

```powershell
cd "3.Implementacao"
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
