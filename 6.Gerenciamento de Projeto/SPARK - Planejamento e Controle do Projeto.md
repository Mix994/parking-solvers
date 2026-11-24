# Planejamento e Controle do Projeto — SmartPark (SpinOff)

## 1. Backlog do Produto (Histórias de Usuário Priorizadas)

| ID | Prioridade | História de Usuário | Sprint | Status |
| :--- | :---: | :--- | :---: | :---: |
| **US01** | Alta | Como usuário, quero autenticar com login e senha (com proteção contra força bruta) e editar meus dados cadastrais. | Sprint 3 | Concluída |
| **US02** | Alta | Como administrador, quero cadastrar e ativar/inativar servidores e docentes com validação dos dados. | Sprint 2 | Concluída |
| **US03** | Alta | Como servidor, quero cadastrar veículos permanentes e temporários com prazo de validade. | Sprint 2 | Concluída |
| **US04** | Alta | Como porteiro, quero consultar placas rapidamente e registrar entradas/saídas por tipo de vaga. | Sprint 2 / 3 | Concluída |
| **US05** | Alta | Como porteiro, quero registrar visitantes externos com documento, instituição de origem, destino e saída. | Sprint 3 | Concluída |
| **US06** | Média | Como administrador, quero acompanhar a ocupação por turno e vagas exclusivas filtrada por período. | Sprint 3 | Concluída |
| **US07** | Média | Como administrador, quero gerenciar contas e níveis de acesso (`ADMIN`, `PORTARIA`, `SERVIDOR`). | Sprint 3 | Concluída |

## 2. Quadro de Tarefas por Sprint

### Sprint 1 — Planejamento (Entrega: 20/11)
- [x] TAP 0.1 (13/11) e evolução para TAP v1.0.
- [x] Documento de Visão, Casos de Uso (`SPARK-US01` a `US07`) e Protótipo.
- [x] ADRs e diagramas de arquitetura (`2.Analise e Design`).

### Sprint 2 — Primeiro Incremento (Entrega: 20/11)
- [x] Setup Android Studio (Kotlin + Compose + Room + KSP).
- [x] Domínios `servidor`, `veiculo` e `movimentacao` com validações e exceções.
- [x] Carga inicial fictícia (`DadosIniciais`) e primeiros testes unitários.

### Sprint 3 — Produto Final (Entrega: 27/11)
- [x] Banco Room v3 com tabelas `usuario` e `visitante`.
- [x] Separação `auth/` vs `usuario/`, `LoginAttemptService`, `SenhaHasher` e modal **Meus dados**.
- [x] Módulo completo de **Visitantes Externos** integrado à Portaria e ao Painel.
- [x] Design institucional com `PlacaMercosulChip` e `SmartParkCard`.
- [x] Suíte de testes unitários estilo CarToon, Massa de Testes SQLite, Guias e Relatório de Código.
