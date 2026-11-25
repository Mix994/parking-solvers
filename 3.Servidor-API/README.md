# SmartPark API — Servidor Backend (`Spring Boot + Kotlin + PostgreSQL`)

Servidor REST central do sistema **SmartPark** (`Parking Solvers`), responsável por persistência, autenticação, autorização, regras de negócio, controle de concorrência de vagas e métricas de ocupação consumidas pelo app Android em [`../3.Implementacao`](../3.Implementacao).

---

## Tecnologias Utilizadas, Como e Onde Foram Aplicadas

| Tecnologia | Como foi utilizada | Onde está no projeto |
| :--- | :--- | :--- |
| **Kotlin 2.1 + Java 21 + Gradle Kotlin DSL** | Linguagem e build do servidor, mantendo paridade com os modelos e regras do app Android. | `build.gradle.kts`, `settings.gradle.kts`, `src/main/kotlin/` |
| **Spring Boot 3.5 (`starter-web`)** | Exposição dos controladores REST JSON e mapeamento centralizado de exceções de domínio para status HTTP (`400`, `401`, `403`, `404`, `409`, `422`, `429`, `500`). | `**/controller/*.kt`, `compartilhado/ApiExceptionHandler.kt`, `compartilhado/Exceptions.kt` |
| **Spring Data JPA + Hibernate (`starter-data-jpa`)** | Entidades JPA mapeadas às 6 tabelas do esquema com `ddl-auto: validate` (garante alinhamento exato com o SQL do Flyway) e lock pessimista (`@Lock(PESSIMISTIC_WRITE)`) em `ConfiguracaoEstacionamentoRepository` para serializar entradas concorrentes em vagas elétricas. | `**/model/*.kt`, `**/repository/*.kt`, `src/main/resources/application.yml` |
| **PostgreSQL 16** | Banco de dados relacional centralizado, com timestamps em `TIMESTAMPTZ` (UTC), chaves estrangeiras, índices de busca e `CHECK` constraints para enums e linha única de configuração. | `docker-compose.yml` e `src/main/resources/db/migration/V1__schema.sql` |
| **Flyway** | Controle de versão e aplicação automática do esquema SQL ao iniciar a aplicação. | `src/main/resources/db/migration/V1__schema.sql` |
| **Spring Security + JJWT 0.12 + BCrypt** | Segurança stateless (`SessionCreationPolicy.STATELESS`), filtro `JwtAuthFilter` para tokens `Bearer`, geração/validação de `accessToken` (60 min) e `refreshToken` (7 dias) em `JwtService`, hash BCrypt para senhas, controle de acesso por perfil (`@PreAuthorize`) e proteção contra força bruta em `LoginAttemptService` (5 falhas = bloqueio de 15 min). | `auth/security/SecurityConfig.kt`, `auth/security/JwtService.kt`, `auth/service/AuthService.kt`, `auth/service/LoginAttemptService.kt` |
| **SpringDoc OpenAPI 2.8 (Swagger UI)** | Documentação interativa da API em `/swagger-ui.html` e especificação OpenAPI em `/v3/api-docs`, configurada com esquema `bearerAuth` (JWT). Habilitada em `dev` e desabilitada por padrão em `prod`. | `config/OpenApiConfig.kt`, `src/main/resources/application-prod.yml` |
| **Spring Boot Actuator** | Expõe `/actuator/health` e `/actuator/info` para monitoramento, Docker `HEALTHCHECK` e checagens de nuvem. | `src/main/resources/application.yml`, `Dockerfile` |
| **Docker (multi-stage) + Docker Compose** | `Dockerfile` compila o `bootJar` com `eclipse-temurin:21-jdk-alpine` e empacota o runtime em `eclipse-temurin:21-jre-alpine` sob usuário não-root (`smartpark`); `docker-compose.yml` sobe o `postgres:16-alpine` (porta `5433`) e, via `--profile full`, também a API containerizada. | `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `.env.example` |
| **JUnit 5 + Mockito + Testcontainers** | Testes unitários de cada serviço/regra de negócio + teste de integração ponta a ponta (`ApiIntegracaoTest`) contra um container `postgres:16-alpine` real, validando Flyway, Hibernate `validate`, JWT, permissões e fluxo completo de endpoints. | `src/test/kotlin/com/parkingsolvers/smartpark/` e `src/test/resources/docker-java.properties` |

---

## Estrutura de Pacotes

```text
3.Servidor-API/
├── Dockerfile                        # Build multi-stage (JDK 21 -> JRE 21 Alpine não-root + HEALTHCHECK)
├── docker-compose.yml                # PostgreSQL 16 (porta 5433) + serviço opcional `api` (--profile full)
├── .env.example                      # Modelo de variáveis de ambiente para produção
├── build.gradle.kts                  # Dependências Spring Boot, JPA, Security, JJWT, Flyway, OpenAPI, Testcontainers
└── src/
    ├── main/
    │   ├── kotlin/com/parkingsolvers/smartpark/
    │   │   ├── SmartParkApiApplication.kt
    │   │   ├── auth/                 # AuthController, AuthService, LoginAttemptService, JwtService, JwtAuthFilter, SecurityConfig
    │   │   ├── usuario/              # CRUD de contas, /usuarios/me, alteração de perfil/ativo
    │   │   ├── servidor/             # CRUD de servidores e busca por prontuário
    │   │   ├── veiculo/              # CRUD de veículos permanentes/temporários e consulta de autorização por placa
    │   │   ├── movimentacao/         # Entrada/saída de veículos, controle de duplicidade e vagas elétricas
    │   │   ├── visitante/            # Entrada/saída de visitantes externos e controle de vagas elétricas
    │   │   ├── dashboard/            # CalculadoraOcupacao, DashboardService, ConfiguracaoEstacionamento (capacidades)
    │   │   ├── compartilhado/        # Exceções de domínio, ApiExceptionHandler, ValidadorFormulario, ValidadorPlaca, RelogioNegocio
    │   │   ├── config/               # AppConfig (Clock UTC), OpenApiConfig (Swagger JWT), AdminInicialProd
    │   │   └── dev/                  # AdminInicialDev (cria admin/admin123 apenas no perfil dev)
    │   └── resources/
    │       ├── application.yml       # Configuração base (perfil padrão: dev, banco localhost:5433)
    │       ├── application-prod.yml  # Configuração de produção (exige env vars, desliga Swagger por padrão)
    │       └── db/migration/
    │           └── V1__schema.sql    # Esquema inicial PostgreSQL (6 tabelas, índices, FKs e configuração padrão)
    └── test/
        ├── kotlin/com/parkingsolvers/smartpark/
        │   ├── ApiIntegracaoTest.kt  # Integração ponta a ponta com PostgreSQL 16 (Testcontainers)
        │   ├── auth/                 # Testes de autenticação, JWT e bloqueio de tentativas
        │   ├── compartilhado/        # Testes de mapeamento de exceções para status HTTP
        │   ├── usuario/              # Testes de cadastro/validações de usuários e servidores
        │   ├── veiculo/              # Testes de autorização de veículos e fluxo de visitantes
        │   └── dashboard/            # Testes de movimentações, cálculo de ocupação e configuração
        └── resources/
            └── docker-java.properties # Compatibilidade do Testcontainers com Docker Engine 29+ (api.version=1.44)
```

---

## Principais Endpoints REST

| Domínio | Endpoints | Acesso |
| :--- | :--- | :--- |
| **Autenticação** | `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me` | Login/refresh públicos; `/me` autenticado |
| **Servidores** | `GET /servidores`, `GET /servidores/{id}`, `POST /servidores`, `PATCH /servidores/{id}/ativo` | Leitura: `ADMIN`/`PORTARIA` (ou próprio servidor por id); escrita: `ADMIN` |
| **Usuários** | `GET/PUT /usuarios/me`, `GET/POST /usuarios`, `GET /usuarios/{id}`, `PATCH /usuarios/{id}/ativo`, `PATCH /usuarios/{id}/perfil` | `/me`: qualquer autenticado; demais: `ADMIN` |
| **Veículos** | `GET /veiculos`, `GET /veiculos/servidor/{id}`, `GET /veiculos/placa/{placa}`, `POST /veiculos`, `PATCH /veiculos/{id}/ativo` | Consulta: `ADMIN`/`PORTARIA`; gestão: `ADMIN` ou `SERVIDOR` (próprios veículos) |
| **Movimentações** | `GET /movimentacoes`, `GET /movimentacoes/no-estacionamento`, `POST /movimentacoes/entrada`, `POST /movimentacoes/{id}/saida` | `ADMIN` e `PORTARIA` |
| **Visitantes** | `GET /visitantes`, `GET /visitantes/no-campus`, `POST /visitantes`, `POST /visitantes/{id}/saida` | `ADMIN` e `PORTARIA` |
| **Dashboard e Config** | `GET /dashboard/ocupacao?periodo=HOJE\|ULTIMOS_7_DIAS\|TUDO`, `GET/PUT /configuracao-estacionamento` | Leitura: qualquer autenticado; `PUT`: `ADMIN` |
| **Operação / Docs** | `GET /actuator/health`, `GET /swagger-ui.html`, `GET /v3/api-docs` | Público em `dev` (Swagger desligado por padrão em `prod`) |

---

## 1. Desenvolvimento Local

Suba o PostgreSQL com Docker (porta `5433` para não conflitar com PostgreSQL local na `5432`):

```bash
docker compose up -d
```

Rode a API localmente (perfil padrão `dev`, cria `admin` / `admin123` se o banco estiver vazio):

```bash
./gradlew bootRun          # Linux / macOS
.\gradlew.bat bootRun      # Windows
```

- Saúde: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Conectar o app Android (Emulador ou celular via USB)
No emulador Android 17 (API 37) ou celular físico por USB, habilite o túnel local:

```bash
adb reverse tcp:8080 tcp:8080
```

O app em modo `debug` usa `http://127.0.0.1:8080/` por padrão.

---

## 2. Subir Tudo via Docker (API + PostgreSQL)

```bash
docker compose --profile full up -d --build
```

---

## 3. Deploy em Produção (AWS, Render, Railway ou VPS)

1. Crie um banco **PostgreSQL 16** gerenciado (ex.: Amazon RDS, Render/Railway Postgres ou container com volume persistente).
2. Configure o deploy da pasta `3.Servidor-API` usando o `Dockerfile` multi-stage.
3. Defina as variáveis de ambiente listadas em `.env.example` (obrigatórias em `SPRING_PROFILES_ACTIVE=prod`):
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL`, `DB_USER`, `DB_PASSWORD`
   - `JWT_SECRET` (mínimo 32 caracteres aleatórios)
   - `ADMIN_INITIAL_LOGIN` e `ADMIN_INITIAL_PASSWORD` (mínimo 8 caracteres; usadas apenas no primeiro boot quando a tabela `usuario` está vazia).
4. Garanta **HTTPS** na frente da API (AWS App Runner, Render e Railway fornecem automaticamente; em EC2/VPS use Caddy ou Nginx + Let's Encrypt).
5. Configure o healthcheck em `/actuator/health`.

### Gerar o app Android apontando para produção

```bash
cd ../3.Implementacao
./gradlew assembleRelease -PAPI_BASE_URL=https://sua-api.seudominio.com/
```

*(Em `release`, o `network_security_config.xml` bloqueia HTTP sem criptografia — apenas HTTPS é aceito.)*

---

## 4. Backup e Restauração do Banco

```bash
# Backup
docker compose exec -T postgres pg_dump -U smartpark smartpark > backup_smartpark.sql

# Restauração
docker compose exec -T postgres psql -U smartpark smartpark < backup_smartpark.sql
```

---

## 5. Testes

```bash
./gradlew test          # Linux / macOS
.\gradlew.bat test      # Windows
```

Executa os testes unitários e o teste de integração ponta a ponta (`ApiIntegracaoTest`) contra PostgreSQL 16 real via Testcontainers (requer Docker em execução).
