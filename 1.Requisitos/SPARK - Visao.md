# Documento de Visão — SmartPark

## 1. Posicionamento do Problema

| Elemento | Descrição |
| :--- | :--- |
| **O problema de** | conferência lenta de placas na guarita, dificuldade para registrar trocas temporárias de veículos, ausência de controle de visitantes externos e falta de indicadores de ocupação por turno e vagas exclusivas |
| **Afeta** | a portaria, os docentes/servidores e a administração do campus |
| **Cujo impacto é** | filas no acesso, planilhas desatualizadas, credenciais físicas perdidas ou desbotadas e gestão sem dados reais do estacionamento |
| **Uma boa solução seria** | um aplicativo Android nativo com persistência local Room que autentica os três níveis de acesso, valida placas (padrão antigo e Mercosul), gerencia veículos permanentes/temporários, registra visitantes externos e exibe um painel de ocupação por turno e tipo de vaga |

## 2. Matriz de Perfis e Permissões

| Caso de Uso | Descrição | ADMIN | PORTARIA | SERVIDOR |
| :--- | :--- | :---: | :---: | :---: |
| **SPARK-US01** | Logar no Sistema e Editar "Meus Dados" | Sim | Sim | Sim |
| **SPARK-US02** | Manter Servidores e Docentes | Sim | Não | Não |
| **SPARK-US03** | Manter Veículos Permanentes e Temporários | Sim | Sim | Somente os próprios |
| **SPARK-US04** | Consultar Placa e Controlar Entradas/Saídas | Sim | Sim | Não |
| **SPARK-US05** | Registrar e Controlar Visitantes Externos | Sim | Sim | Não |
| **SPARK-US06** | Consultar Painel de Ocupação por Turno | Sim | Sim | Não |
| **SPARK-US07** | Manter Contas e Níveis de Acesso | Sim | Não | Não |

## 3. Necessidades do Cliente e Suposições Registradas
- **S01 (Capacidade padrão):** 60 vagas totais, sendo 10 vagas exclusivas (PCD, Idoso e Viaturas Oficiais).
- **S02 (Turnos automáticos):** Manhã (`06h00–11h59`), Tarde (`12h00–17h59`), Noite (`18h00–05h59`).
- **S03 (Placas):** aceita `AAA-1234` e Mercosul `ABC1D23`, armazenadas normalizadas (7 caracteres maiúsculos sem hífen).
- **S04 (Segurança de login):** senhas gravadas com hash SHA-256 + salt aleatório; 5 falhas consecutivas bloqueiam o login por 15 minutos (`LoginAttemptService`).
- **S05 (Visitantes externos):** visitantes de outros campi, empresas e convidados possuem tabela e fluxo dedicados (`visitante`) contabilizados no painel de ocupação.
