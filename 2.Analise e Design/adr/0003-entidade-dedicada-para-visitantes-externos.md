# ADR-0003: Entidade dedicada para visitantes externos (`visitante`)

Status: Aceita  
Data: 2026-11-23

## Contexto
Além de docentes e servidores do próprio campus, o Instituto Federal recebe diariamente visitantes externos (docentes de outros campi em bancas, técnicos de manutenção, fornecedores, órgãos públicos e palestrantes) que precisam registrar identificação documental, instituição de origem, motivo da visita e setor visitado — atributos que não pertencem a `Servidor` nem a `Veiculo`.

## Decisão
Criar o domínio independente `visitante/` com tabela própria `visitante` no Room v3, contendo nome, documento (CPF/RG), instituição de origem, telefone, placa/modelo/cor do veículo, motivo da visita, setor de destino, tipo de vaga, turno automático, operador responsável e timestamps de entrada/saída. Integrar os visitantes ativos e históricos na `CalculadoraOcupacao` do Dashboard e oferecer atalho direto da `PortariaScreen` quando uma placa não é encontrada.

## Consequências
- `Servidor` e `Veiculo` permanecem enxutos e focados no quadro permanente/temporário do campus.
- A portaria obtém rastreabilidade completa de quem visitou o campus, de onde veio e que setor visitou.
- O painel de ocupação reflete a lotação real do estacionamento (`servidores + visitantes externos`).
