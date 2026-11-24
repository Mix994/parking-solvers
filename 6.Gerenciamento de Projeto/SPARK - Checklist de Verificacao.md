# Checklist de Verificação do Projeto — SmartPark

| # | Critério Avaliado | Status | Evidência |
| :-: | :--- | :---: | :--- |
| 1 | Aplicativo em **Kotlin** e **Jetpack Compose** | OK | `3.Implementacao/app/src/main/java/...` |
| 2 | Persistência de dados com **Room** (5 tabelas + FKs + índices únicos) | OK | `SmartParkDatabase.kt` (v3: `usuario`, `servidor`, `veiculo`, `registro_movimentacao`, `visitante`) |
| 3 | Autenticação por níveis de usuário (`ADMIN`, `PORTARIA`, `SERVIDOR`) | OK | `AuthService.kt`, `LoginAttemptService.kt`, `UsuarioService.kt` |
| 4 | Registro de visitantes externos com origem, documento, destino e saída | OK | `visitante/` (`VisitanteService.kt`, `VisitantesScreen.kt`) |
| 5 | Validação de formulários e telas (placa, CPF/RG, e-mail, telefone, senhas) | OK | `ValidadorPlaca.kt`, `ValidadorFormulario.kt` + erros inline |
| 6 | Conceitos da disciplina: strings, regex, coleções, funções, POO e exceções | OK | `compartilhado/exceptions/*`, `CalculadoraOcupacao.kt`, `VeiculoService.kt` |
| 7 | Dados fictícios pré-carregados para testes e demonstração | OK | `DadosIniciais.kt` + `4.Teste/Massa de Testes/*.sql` |
| 8 | Suíte de testes unitários automatizados passando | OK | `.\gradlew.bat testDebugUnitTest` (11 classes de teste) |
| 9 | APK compilado e instalável sem erros | OK | `3.Implementacao/app/build/outputs/apk/debug/app-debug.apk` |
| 10 | Artefatos SpinOff completos (TAP, Visão, UCs, ADRs, Atas, Manual e Relatório) | OK | Pastas `1.Requisitos` a `6.Gerenciamento de Projeto` |
