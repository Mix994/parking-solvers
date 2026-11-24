# ADR-0001: Persistência local offline-first com Room (SQLite)

Status: Aceita  
Data: 2026-11-20

## Contexto
A portaria de um Instituto Federal precisa consultar placas e registrar entradas/saídas mesmo quando há oscilação de Wi-Fi na guarita, e os requisitos técnicos da disciplina PDIM pedem persistência local com **Room** em Kotlin + Jetpack Compose.

## Decisão
Adotar banco local SQLite gerenciado pelo **Room** (`SmartParkDatabase`, versão 3) com 5 tabelas relacionais (`usuario`, `servidor`, `veiculo`, `registro_movimentacao`, `visitante`), integridade referencial via chaves estrangeiras (`ForeignKey`), índices únicos em `usuario.login`, `servidor.prontuario` e `veiculo.placa`, e carga automática de dados fictícios (`DadosIniciais`) na primeira execução.

## Consequências
- Resposta instantânea nas consultas da portaria sem latência de rede.
- Fluxos reativos (`Flow`) mantêm listas e painel sincronizados automaticamente.
- Facilidade para demonstração e avaliação via `.apk` sem infraestrutura externa obrigatória.

## Gatilho para revisar
Caso vários dispositivos na guarita precisem compartilhar movimentações simultaneamente em tempo real, adicionar uma camada de sincronização remota sobre os mesmos repositórios.
