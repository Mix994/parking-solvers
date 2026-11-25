# SmartPark API (Spring Boot + Kotlin + PostgreSQL)

Servidor central do aplicativo **SmartPark** (`3.Implementacao`), substituindo o banco local Room.

## 1. Desenvolvimento local

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

## 2. Subir tudo via Docker (API + PostgreSQL)

```bash
docker compose --profile full up -d --build
```

## 3. Deploy em produção (Render, Railway ou VPS)

1. Crie um banco **PostgreSQL 16** gerenciado (ou container dedicado com volume persistente).
2. Configure o deploy da pasta `3.Servidor-API` usando o `Dockerfile` multi-stage.
3. Defina as variáveis de ambiente listadas em `.env.example` (obrigatórias em `SPRING_PROFILES_ACTIVE=prod`):
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DB_URL`, `DB_USER`, `DB_PASSWORD`
   - `JWT_SECRET` (mínimo 32 caracteres aleatórios)
   - `ADMIN_INITIAL_LOGIN` e `ADMIN_INITIAL_PASSWORD` (usadas apenas no primeiro boot quando o banco ainda não tem usuários; remova depois ou mantenha seguras).
4. Garanta **HTTPS** na frente da API (Render/Railway fornecem automaticamente; em VPS use Caddy ou Nginx + Let's Encrypt).
5. Configure healthcheck em `/actuator/health`.

### Gerar o app Android apontando para produção

```bash
cd ../3.Implementacao
./gradlew assembleRelease -PAPI_BASE_URL=https://sua-api.seudominio.com/
```

*(Em `release`, o `network_security_config.xml` bloqueia HTTP sem criptografia — apenas HTTPS é aceito.)*

## 4. Backup e restauração do banco

```bash
# Backup
docker compose exec -T postgres pg_dump -U smartpark smartpark > backup_smartpark.sql

# Restauração
docker compose exec -T postgres psql -U smartpark smartpark < backup_smartpark.sql
```

## 5. Testes

```bash
./gradlew test
```

Inclui testes unitários e o teste de integração ponta a ponta (`ApiIntegracaoTest`) contra PostgreSQL 16 real via Testcontainers.
