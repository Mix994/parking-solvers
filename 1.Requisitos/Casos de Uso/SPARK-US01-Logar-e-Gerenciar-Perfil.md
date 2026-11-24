# SPARK-US01 — Logar no Sistema e Gerenciar Perfil ("Meus Dados")

- **Atores:** Administração (`ADMIN`), Portaria (`PORTARIA`), Servidor / Docente (`SERVIDOR`).
- **Pré-condição:** Conta cadastrada e ativa na tabela `usuario` do Room.

## Fluxo Principal (Login)
1. O usuário informa seu e-mail institucional, login ou prontuário e a senha.
2. O sistema valida o preenchimento dos campos obrigatórios.
3. O sistema verifica em `LoginAttemptService` se a conta não está temporariamente bloqueada.
4. O sistema busca o usuário no Room e valida a senha com `SenhaHasher.verificar`.
5. O sistema verifica se a conta (e o servidor vinculado, no perfil `SERVIDOR`) está ativa.
6. O sistema libera o `AppShell` com as abas permitidas para o nível do usuário.

## Fluxo Alternativo — "Meus Dados"
1. A partir do botão de perfil na barra superior, qualquer usuário autenticado abre `MeusDadosDialog`.
2. Pode atualizar nome, login e opcionalmente definir uma nova senha (com confirmação).
3. O sistema persiste no Room e sincroniza a sessão em memória sem deslogar.

## Regras e Exceções
- **Credenciais inválidas:** lança `CredenciaisInvalidasException` e incrementa o contador de falhas.
- **Força bruta:** após 5 tentativas falhas consecutivas, bloqueia o login por 15 minutos (`BloqueioTentativasException`).
- **Conta ou servidor inativo:** bloqueia acesso com `RecursoInativoException`.
