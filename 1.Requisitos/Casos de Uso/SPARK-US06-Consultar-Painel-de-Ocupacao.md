# SPARK-US06 — Consultar Painel de Ocupação

- **Atores:** Administração (`ADMIN`), Portaria (`PORTARIA`).
- **Local no App:** Aba **Painel** (`DashboardScreen`).

## Fluxo Principal
1. O usuário seleciona o filtro de período (*Hoje*, *7 dias* ou *Tudo*).
2. O painel exibe em destaque o total de veículos no pátio agora (`servidores + visitantes externos`) e o percentual sobre a capacidade total (60 vagas).
3. Exibe cards de vagas exclusivas ocupadas agora, visitantes externos no campus e no período, servidores ativos e veículos temporários vigentes.
4. Exibe barras de movimentação por turno (*Manhã*, *Tarde*, *Noite*) com média diária de entradas e quadro de uso por tipo de vaga.
