# Roteiro de Testes — SmartPark

## 1. Testes Automatizados (JUnit 4)
Executados via `.\gradlew.bat testDebugUnitTest` dentro de `3.Implementacao/`:

| Classe de Teste | Cenários Cobertos |
| :--- | :--- |
| `LoginAttemptServiceTest` | 4 falhas sem bloqueio, bloqueio na 5ª falha, normalização de login, reset no sucesso, desbloqueio após 15 min |
| `AuthServiceTest` | Login ADMIN/PORTARIA/SERVIDOR, login por prontuário, senha incorreta, bloqueio após 5 tentativas, conta inativa |
| `SenhaHasherTest` | Salt aleatório único, validação de senha correta/incorreta, rejeição de hash malformado |
| `UsuarioServiceTest` | Cadastro, login duplicado, confirmação de senha divergente, bloqueio de auto-inativação, edição de "Meus dados" |
| `VisitanteServiceTest` | Entrada/saída de visitante externo, normalização de placa, validação de campos/documento, bloqueio de duplicidade no campus |
| `MovimentacaoServiceTest` | Entrada autorizada, bloqueio de duplicidade no pátio, bloqueio de placa não cadastrada, liberação excepcional, saída |
| `ServidorServiceTest` | Cadastro, normalização de prontuário/e-mail, prontuário duplicado, ativar/inativar |
| `VeiculoAutorizacaoTest` | Permanente ativo, temporário vigente, temporário expirado, servidor inativo |
| `CalculadoraOcupacaoTest` | Lotação por turno, vagas exclusivas, filtro por período e soma de visitantes externos |
| `ValidadorPlacaTest` e `ValidadorFormularioTest` | Placa antiga e Mercosul, e-mail, telefone, documento (CPF/RG), período temporário e turnos |

---

## 2. Roteiro de Testes Manuais (Emulador / Celular)

| CT | Caso de Uso | Passos | Resultado Esperado |
| :---: | :---: | :--- | :--- |
| **CT01** | US01 | Tentar entrar com `admin@campus.edu.br` e senha errada 5 vezes | Na 6ª tentativa exibe bloqueio temporário por 15 minutos |
| **CT02** | US01 | Entrar com `BP102401` e senha `123456` | Abre a visão **Meus veículos** da Prof.ª Ana Beatriz Souza |
| **CT03** | US01 | Tocar no ícone de perfil na barra superior e alterar nome em **Meus dados** | Nome atualiza imediatamente na barra superior sem deslogar |
| **CT04** | US03 | Como Servidor, cadastrar placa temporária `QWE9R87` válida por `3d` | Exibe `PlacaMercosulChip` com badges **Temporário** e **Válido** |
| **CT05** | US04 | Como Portaria (`portaria@campus.edu.br` / `portaria123`), consultar `EXP9988` | Exibe card vermelho **Temporário Expirado** |
| **CT06** | US05 | Como Portaria, consultar placa nova `XYZ4K55` -> tocar em **Registrar como visitante externo** | Abre cadastro de visitante com `XYZ4K55` preenchida; ao salvar aparece em **No campus agora** e soma no **Painel** |
| **CT07** | US05 | Na aba **Visitantes**, tocar em **Dar baixa na saída** para `IFG8B24` | Visitante migra para **Visitas encerradas** e contador do pátio diminui |
| **CT08** | US06 | Como Administração (`admin@campus.edu.br` / `admin123`), alternar **Hoje / 7 dias / Tudo** no **Painel** | Barras de turno e contadores por tipo de vaga atualizam imediatamente |
| **CT09** | US07 | Em **Gestão > Contas de acesso**, tentar inativar a própria conta | Botão de inativar não é exibido para `(você)` e o serviço bloqueia auto-inativação |
