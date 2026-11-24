# SPARK-US02 — Manter Servidores e Docentes

- **Atores:** Administração (`ADMIN`).
- **Local no App:** Aba **Gestão > Servidores** (`ServidoresScreen`).

## Fluxo Principal
1. O administrador visualiza a lista de servidores ativos e inativos.
2. Toca no botão `+` para abrir o formulário de novo servidor.
3. Informa nome completo, prontuário/SIAPE (mín. 4 caracteres), e-mail institucional, telefone e setor.
4. O sistema valida todos os campos via `ServidorService`, verifica unicidade do prontuário e grava na tabela `servidor`.
5. O administrador pode inativar ou reativar servidores a qualquer momento.

## Exceções
- Campos inválidos -> `ValidacaoException` com mensagens inline por campo.
- Prontuário duplicado -> `ConflitoException`.
