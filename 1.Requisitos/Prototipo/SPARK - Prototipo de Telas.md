# Protótipo e Direção Visual — SmartPark

## 1. Direção Estética (Sistema de Guarita Institucional)
- **Paleta:** `VerdeCampus` (`#0F6A3B`), `ArdosiaAsfalto` (`#141C1A`), `PedraPatio` (`#F2F4F2`), `AzulMercosul` (`#003399`), `OcreSinal` (`#B45309`) e `CarmimBloqueio` (`#B91C1C`).
- **Elemento assinatura:** `PlacaMercosulChip` — mini-placa brasileira com faixa azul `BRASIL` (Mercosul) ou faixa cinza `BR` (padrão antigo) e tipografia monoespaçada de alto contraste.
- **Superfícies:** `SmartParkCard` com borda fina `1.dp` (`BordaSutil`) e sem sombras pesadas.

## 2. Mapa de Telas por Nível de Acesso
1. **Login (`LoginScreen`)**: banner `VerdeCampus`, formulário de e-mail/prontuário + senha, botão *Criar nova conta* e lista clicável de contas locais para demonstração.
2. **Painel (`DashboardScreen`)**: filtro *Hoje / 7 dias / Tudo*, card principal de lotação em `VerdeCampus` separando servidores e visitantes externos, métricas de vagas exclusivas e barras por turno.
3. **Portaria (`PortariaScreen`)**: busca de placa com validação instantânea, card de autorização com `PlacaMercosulChip`, atalho *Registrar como visitante externo*, pátio ativo e últimas saídas.
4. **Visitantes (`VisitantesScreen`)**: busca por nome/origem/placa, cards de visitantes no campus com instituição e destino, botão *Dar baixa na saída*, visitas encerradas e modal completo de cadastro.
5. **Veículos (`VeiculosScreen`)**: cards com `PlacaMercosulChip`, badges de tipo e vigência, inativação/reativação e modal de cadastro permanente/temporário.
6. **Gestão (`GestaoAdminScreen`)**: abas internas **Servidores** e **Contas de acesso** exclusivas do nível `ADMIN`.
7. **Meus dados (`MeusDadosDialog`)**: modal global na barra superior para edição de nome, login e senha.
