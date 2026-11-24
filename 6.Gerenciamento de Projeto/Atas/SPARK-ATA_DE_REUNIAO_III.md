# Ata de Reunião III — Revisão Final / Produto Final (Etapa 3)

- **Data:** 23/11/2026 (entrega prevista: 27/11/2026)
- **Equipe (Parking Solvers):** Caio Emanuel, Lucas Nunes, Luiza Bueno, Michel Amancio
- **Objetivo:** Revisar o produto final completo (usuários com níveis de acesso, proteção contra força bruta, visitantes externos, design visual e testes automatizados) e estruturar o repositório Git no padrão SpinOff.

## Entregas Concluídas no Sprint 3
- Separação arquitetural `auth/` vs `usuario/` com tabela `usuario` no Room, hash SHA-256 + salt (`SenhaHasher`), bloqueio contra força bruta (`LoginAttemptService`) e modal global **Meus dados**.
- Novo módulo completo `visitante/` no Room v3 para registrar visitantes externos de outros campi/instituições, integrado à Portaria e ao Painel de Ocupação.
- Novo design institucional com `PlacaMercosulChip`, `SmartParkCard`, paleta `VerdeCampus` e 5 abas organizadas para o perfil Administração.
- Suíte de 11 arquivos de testes unitários JUnit cobrindo todos os domínios do aplicativo.

## Contribuição Individual
- **Caio Emanuel:** Arquitetura modular baseada no CarToon, `LoginAttemptService`, `MovimentacaoService` e testes de serviço.
- **Lucas Nunes:** `CalculadoraOcupacao` (com filtro de período e visitantes), `VisitanteService` e scripts SQLite da Massa de Testes.
- **Luiza Bueno:** Design visual Compose (`PlacaMercosulChip`, `SmartParkCard`, `LoginScreen`, `VisitantesScreen`, `DashboardScreen` e `MeusDadosDialog`).
- **Michel Amancio:** Integração Room v3 (`AppContainer`, `DadosIniciais`), `UsuarioService`, gestão de contas e consolidação do repositório Git e relatórios SpinOff.
