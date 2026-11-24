# Relatório de Código — SmartPark (Parking Solvers)

> Formato dos relatórios semanais de PDIM: **etapas numeradas**, **blocos de código**, **teste sugerido**, **"O que aconteceu?"** e **espaço para print de tela**.

---

## Etapa 1 — Banco de Dados Room v3 e Autenticação por Níveis de Acesso

A tabela `usuario` armazena os 3 níveis (`ADMIN`, `PORTARIA`, `SERVIDOR`) com senhas protegidas por SHA-256 + salt (`SenhaHasher`) e bloqueio contra força bruta (`LoginAttemptService`: 5 falhas consecutivas bloqueiam o acesso por 15 minutos).

### Bloco de Código (`AuthService.kt`)
```kotlin
if (loginAttemptService.estaBloqueado(loginLimpo)) {
    throw BloqueioTentativasException()
}

val usuario = localizarUsuarioParaLogin(loginLimpo)
if (usuario == null || !SenhaHasher.verificar(senhaEntrada, usuario.senhaHash)) {
    loginAttemptService.registrarFalha(loginLimpo)
    throw CredenciaisInvalidasException()
}

if (!usuario.ativo) {
    throw RecursoInativoException("Esta conta está inativa. Procure a Administração.")
}
```

### Teste Sugerido
1. Abrir o app e tocar em **Administração** na lista de contas locais para preencher `admin@campus.edu.br` / `admin123`.
2. Alterar a senha para `errada` e clicar em **Entrar** 5 vezes; na 6ª tentativa, verificar a mensagem de bloqueio por 15 minutos.
3. Entrar com `BP102401` / `123456` (login pelo prontuário) e verificar o isolamento da visão **Meus veículos**.

### O que aconteceu?
O `LoginAttemptService` bloqueou a conta após a 5ª falha e o `AuthService` localizou o usuário da Prof.ª Ana tanto pelo e-mail quanto pelo prontuário `BP102401`, abrindo somente os veículos vinculados ao seu `servidorId`.

> **[INSERIR PRINT 1: Tela de Login com contas locais e validação de autenticação]**

---

## Etapa 2 — Consulta de Placas com `PlacaMercosulChip` e Controle de Portaria

A consulta valida placas no padrão antigo (`AAA-1234`) e Mercosul (`ABC1D23`), exibe a mini-placa brasileira `PlacaMercosulChip` em destaque e adapta os botões de ação conforme o estado atual do veículo.

### Bloco de Código (`ValidadorPlaca.kt`)
```kotlin
object ValidadorPlaca {
    private val PADRAO_PLACA = Regex("^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$")

    fun ehValida(placa: String?): Boolean =
        !placa.isNullOrBlank() && PADRAO_PLACA.matches(placa.trim())

    fun validarENormalizar(placa: String?): String {
        if (!ehValida(placa)) throw PlacaInvalidaException()
        return placa!!.trim().uppercase().replace("-", "")
    }
}
```

### Teste Sugerido
1. Entrar como **Portaria** (`portaria@campus.edu.br` / `portaria123`).
2. Consultar `TMP4F56` (temporário vigente) e liberar a entrada.
3. Consultar `EXP9988` (temporário expirado) e verificar o bloqueio vermelho.
4. Dar baixa na saída de `TMP4F56` e conferir a lista **Últimas saídas**.

### O que aconteceu?
`TMP4F56` exibiu card verde com `PlacaMercosulChip` e entrou no pátio; `EXP9988` exibiu **Temporário Expirado**. Ao dar baixa na saída, o registro foi atualizado no Room e movido automaticamente para **Últimas saídas**.

> **[INSERIR PRINT 2: Tela da Portaria com PlacaMercosulChip e controle de pátio]**

---

## Etapa 3 — Registro e Controle de Visitantes Externos

Quando chega um visitante de outro campus, fornecedor ou convidado, a portaria registra nome, CPF/RG, instituição de origem, veículo, setor visitado e motivo da visita na tabela `visitante`.

### Bloco de Código (`VisitanteService.kt`)
```kotlin
val placaNormalizada = ValidadorPlaca.validarENormalizar(request.placa)
if (visitanteRepository.buscarNoCampusPorPlaca(placaNormalizada) != null) {
    throw ConflitoException(
        "O visitante com placa ${Formatadores.formatarPlaca(placaNormalizada)} já está registrado no campus."
    )
}
val entidade = VisitanteMapper.paraEntidade(
    request = request,
    placaNormalizada = placaNormalizada,
    turno = TurnoUtils.determinarTurno(agoraMillis),
    registradoPor = registradoPor.ifBlank { "Portaria" },
    entradaMillis = agoraMillis
)
```

### Teste Sugerido
1. Na Portaria, consultar uma placa desconhecida `XYZ4K55` e tocar em **Registrar como visitante externo**.
2. Preencher os dados do visitante e tocar em **Registrar entrada**.
3. Na aba **Visitantes**, conferir o visitante em **No campus agora** e depois tocar em **Dar baixa na saída**.

### O que aconteceu?
O app redirecionou para a aba **Visitantes** com a placa `XYZ4K55` pré-preenchida, validou todos os campos obrigatórios, gravou na tabela `visitante` e contabilizou o visitante no painel de ocupação até o momento da baixa de saída.

> **[INSERIR PRINT 3: Tela de Visitantes Externos com registro ativo e histórico]**

---

## Etapa 4 — Painel de Ocupação Consolidado (Servidores + Visitantes)

A `CalculadoraOcupacao` combina os registros abertos e históricos das tabelas `registro_movimentacao` e `visitante`, exibindo lotação atual, uso de vagas exclusivas e média diária por turno filtrada por **Hoje**, **7 dias** ou **Tudo**.

### Teste Sugerido
1. Entrar como **Administração** (`admin@campus.edu.br` / `admin123`).
2. Conferir o banner principal de ocupação (detalhando servidores e visitantes externos no pátio).
3. Alternar entre **Hoje**, **7 dias** e **Tudo** e observar as barras por turno e a tabela de vagas exclusivas.

### O que aconteceu?
Os fluxos `Flow.combine` do `DashboardService` recalcularam em tempo real os indicadores ao alternar o período ou ao registrar novas entradas e saídas.

> **[INSERIR PRINT 4: Painel de Ocupação e aba Gestão (Servidores / Contas de acesso)]**
