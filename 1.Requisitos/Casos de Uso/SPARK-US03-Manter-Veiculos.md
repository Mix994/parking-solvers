# SPARK-US03 — Manter Veículos Permanentes e Temporários

- **Atores:** Servidor / Docente (`SERVIDOR`), Portaria (`PORTARIA`), Administração (`ADMIN`).
- **Local no App:** Aba **Veículos** (`VeiculosScreen`).

## Fluxo Principal
1. No perfil `SERVIDOR`, a lista exibe apenas os veículos vinculados ao servidor logado; nos perfis `PORTARIA` e `ADMIN`, exibe todos os veículos do campus.
2. Ao tocar em `+`, informa placa (`AAA-1234` ou Mercosul `ABC1D23`), modelo, cor e tipo de cadastro (*Permanente* ou *Temporário*).
3. Para veículos *Temporários*, escolhe a validade (`1d`, `3d`, `7d` ou `15d` a partir de hoje).
4. O sistema valida e normaliza a placa, garante unicidade e salva na tabela `veiculo`.
5. O usuário pode inativar ou reativar veículos cadastrados.

## Exceções
- Servidor inativo -> `RecursoInativoException`.
- Placa inválida ou campos obrigatórios vazios -> `ValidacaoException`.
- Placa já existente -> `ConflitoException`.
