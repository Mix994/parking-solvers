# Termo de Abertura do Projeto (TAP v1.0) — SmartPark

| Campo | Descrição |
| :--- | :--- |
| **Projeto** | SmartPark — Sistema de Gestão e Monitoramento do Estacionamento |
| **Disciplina** | Programação de Dispositivos Móveis (PDIM) — BCC — 2026-2 |
| **Equipe** | Parking Solvers |
| **Versão / Data** | 1.0 — 20/11/2026 (evolução do TAP 0.1 de 13/11/2026) |
| **Cliente (Product Owner)** | Prof. Fábio Santos |
| **Scrum Master** | Michel Amancio |

## 1. Componentes da Equipe

| Nome | Papel no SpinOff |
| :--- | :--- |
| Caio Emanuel | Time de Desenvolvimento |
| Lucas Nunes | Time de Desenvolvimento |
| Luiza Bueno | Time de Desenvolvimento |
| Michel Amancio | Scrum Master e Time de Desenvolvimento |

## 2. Descrição da Ideia e Recorte
O **SmartPark** é um aplicativo Android desenvolvido em **Kotlin**, **Jetpack Compose** e **Room** para gerir e monitorar o estacionamento de um campus do Instituto Federal, substituindo a planilha compartilhada e as credenciais físicas (adesivos/cartões).

O escopo cobre:
1. Autenticação local por níveis de acesso (`ADMIN`, `PORTARIA`, `SERVIDOR`) com proteção contra força bruta.
2. Cadastro e gestão de servidores/docentes e de seus veículos permanentes e temporários (com vigência).
3. Consulta rápida de placas pela portaria com registro de entrada e saída por tipo de vaga e turno.
4. Registro completo de **visitantes externos** de outras unidades, empresas parceiras ou comunidade externa.
5. Painel de ocupação em tempo real e filtrado por período (*Hoje*, *7 dias*, *Tudo*) com uso de vagas exclusivas.

## 3. Cronograma de Entregas (Moodle)

| Etapa | Data | Entrega |
| :--- | :---: | :--- |
| Ideia | 13/11/2026 | TAP 0.1 — componentes da equipe e descrição da ideia |
| Etapa 1 e 2 | 20/11/2026 | Planejamento SpinOff + primeiro incremento executável e testado |
| Etapa 3 | 27/11/2026 | Produto final completo, testado, checklist e relatório de código |
| Apresentação | 04/12/2026 | Demonstração ao vivo + documento Visão e relatório final |
