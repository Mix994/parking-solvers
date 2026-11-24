# SPARK-US05 — Registrar e Controlar Visitantes Externos

- **Atores:** Portaria (`PORTARIA`), Administração (`ADMIN`).
- **Local no App:** Aba **Visitantes** (`VisitantesScreen`).

## Fluxo Principal
1. O operador acessa a aba **Visitantes** (ou vem diretamente da consulta de placa não cadastrada na Portaria).
2. Toca no botão `+` para abrir o cadastro de visitante externo.
3. Preenche nome completo, CPF ou RG, instituição/cidade/empresa de origem, telefone, placa, modelo, cor, setor/pessoa visitada, motivo da visita, tipo de vaga e observação opcional.
4. O sistema valida todos os campos via `VisitanteService`, impede duplicidade de placa já presente no campus e grava na tabela `visitante` com turno automático e nome do operador.
5. O visitante aparece em **No campus agora** com `PlacaMercosulChip` e botão **Dar baixa na saída**.
6. Ao dar baixa, o registro recebe `dataHoraSaidaMillis` e passa para **Visitas encerradas**.
7. O campo de busca filtra visitantes em tempo real por nome, instituição de origem, placa ou setor visitado.
