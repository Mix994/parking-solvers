# SPARK-US07 — Manter Contas e Níveis de Acesso

- **Atores:** Administração (`ADMIN`).
- **Local no App:** Aba **Gestão > Contas de acesso** (`UsuariosScreen`).

## Fluxo Principal
1. O administrador visualiza todas as contas gravadas na tabela `usuario`, podendo filtrar por nível (`Todos`, `Administração`, `Portaria`, `Servidor / Docente`).
2. Toca em `+` para criar uma nova conta escolhendo nível de acesso, nome, login, senha e confirmação de senha (com vínculo obrigatório a um servidor ativo caso o nível seja `SERVIDOR`).
3. Pode inativar ou reativar contas existentes.

## Regras e Exceções
- Não é permitido inativar ou rebaixar a própria conta atualmente logada (`RegraDeNegocioException`).
- Login duplicado ou servidor já vinculado a outra conta lançam `ConflitoException`.
