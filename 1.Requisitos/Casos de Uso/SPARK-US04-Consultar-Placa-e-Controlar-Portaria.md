# SPARK-US04 — Consultar Placa e Controlar Portaria

- **Atores:** Portaria (`PORTARIA`), Administração (`ADMIN`).
- **Local no App:** Aba **Portaria** (`PortariaScreen`).

## Fluxo Principal
1. O operador digita a placa no campo de busca (validado em tempo real).
2. Toca em consultar e visualiza o card com `PlacaMercosulChip` em destaque e o status de autorização:
   - `AUTORIZADO_PERMANENTE`, `AUTORIZADO_TEMPORARIO` (verde)
   - `TEMPORARIO_EXPIRADO`, `TEMPORARIO_FORA_VIGENCIA`, `VEICULO_INATIVO`, `SERVIDOR_INATIVO`, `NAO_CADASTRADO` (vermelho)
3. Seleciona o tipo de vaga (`Comum`, `Exclusiva PCD`, `Exclusiva Idoso`, `Viatura / Oficial`) e toca em **Liberar entrada autorizada**.
4. Se o veículo já estiver no pátio, o card oferece diretamente **Dar baixa na saída**.
5. Caso a placa não esteja cadastrada, o operador pode tocar em **Registrar como visitante externo** (abrindo `SPARK-US05` com a placa preenchida) ou **Liberar entrada excepcional**.
6. Na seção **No estacionamento agora**, pode dar baixa em qualquer saída, que migra automaticamente para **Últimas saídas**.
