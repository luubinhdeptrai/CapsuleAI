# CapsuleAI Backend

## 1. Project overview

CapsuleAI helps users understand their wardrobe, choose outfits and evaluate wardrobe improvements. This backend is its Java/Spring Boot business core, organized as one **Modular Monolith**.

The **Pre-Sprint Engineering Baseline is implemented and merged into `main`** through [PR #1](https://github.com/luubinhdeptrai/CapsuleAI/pull/1). It provides the application/build foundation, six documented module boundaries, PostgreSQL configuration, Flyway migrations, automated smoke/dependency tests and GitHub Actions verification.

**Sprint 1 Identity authentication is implemented** (US-AUTH-001–004): registration, login, persistent login sessions with HS256 JWT access tokens, refresh-token rotation with reuse detection and current-session logout, under `/api/v1/auth` ([OpenAPI contract](../docs/05-api/openapi.yaml)). **Sprint 2 adds email password recovery** (US-AUTH-005) under `/api/v1/auth/password-reset/*`, delivered through SMTP. Garments, outfits, Wear/history, Coverage/Multiplier, shopping and other real provider integrations remain selected-story work. This is not a production deployment or a completed product Increment; story completion follows the Definition of Done.

## 2. Technology stack

Versions come from [pom.xml](pom.xml), [Wrapper configuration](.mvn/wrapper/maven-wrapper.properties), [Compose](../compose.yaml) and Boot's [4.1.1 dependency BOM](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom).

| Technology | Adopted version | Purpose |
| --- | --- | --- |
| Java | 21 | Compilation/runtime |
| Spring Boot / servlet MVC | 4.1.1 | Application configuration and embedded HTTP server |
| Spring Security (OAuth2 resource server, JOSE, crypto) | 7.1.1 | Bearer JWT authentication and password hashing |
| Spring Boot Mail (Jakarta Mail) | Managed by Boot 4.1.1 | SMTP delivery of password-reset instructions |
| Mailpit | `axllent/mailpit:v1.27.11` image | Local SMTP sink (Compose) and real-SMTP integration tests |
| Maven / Maven Wrapper | 3.9.16 / 3.3.4 | Reproducible build; Wrapper uses `only-script` |
| PostgreSQL | `17.11-bookworm` image | Development and integration-test databases |
| Flyway / PostgreSQL JDBC | 12.4.0 / 42.7.13 | Migrations and connectivity |
| JUnit Jupiter | 6.0.3 | Test execution |
| AssertJ / Mockito | 3.27.7 / 5.23.0 | Assertions and test doubles |
| Testcontainers | 2.0.5 | Disposable real PostgreSQL |
| ArchUnit | 1.5.1 | Compiled-class dependency checks |
| Surefire / Failsafe | 3.5.6 | Fast/architecture and integration test runners |
| Docker Compose / GitHub Actions | Installed Compose CLI / committed workflow | Local database lifecycle and hosted verification |

Boot manages library/plugin versions except the explicit ArchUnit pin. Keep managed versions compatible when upgrading. JPA/ORM, cache and broker dependencies are not included.

## 3. Architecture overview

One Spring Boot application and one Maven project contain six logical business owners. **Clean Architecture** keeps domain policy independent of infrastructure. Application use cases coordinate domain behavior through necessary **ports**; outer **adapters** implement database, transport or provider access.

The [Implementation View](../docs/04-architecture/views/implementation-view.puml) defines the following role conventions. Identity contains the Sprint 1 roles; the other owners still contain only `package-info.java` documentation. Add roles/classes when actual selected work requires them.

| Role inside an owner | Responsibility |
| --- | --- |
| `api` | Narrow, framework-free contract published to other owners |
| `domain` | Business invariants; no Spring, persistence, HTTP/JSON or provider dependencies |
| `application` / `application.ports` | Use-case coordination and required persistence/integration/neighbor outcomes; dependencies point inward |
| `adapter.in` / `adapter.out` | Incoming translation and implementations of required ports |
| `configuration` | Outer framework wiring of that owner's private implementations |

Cross-module business collaboration uses the supplier's published `api`, not private domain/application/repository/entity types. This local Java contract differs from an HTTP controller. Module configuration wires its own internals; root bootstrap delegates assembly. One PostgreSQL database does not grant unrestricted access to another owner's persistence.

**One schema per owner (AR-01–AR-08):**

- Inside the single database, each owner that persists state has its own PostgreSQL schema, named after its package (`identity`, `wardrobe`, `personal_context`, …).
- Its tables are named `<schema>_<name>` and are always written schema-qualified, for example `identity.identity_session`.
- No owner joins, reads, writes or references another owner's tables by foreign key; it calls that owner's published `api`.
- Flyway's default schema (`public`) holds only `flyway_schema_history`.

The application uses one database role, so these rules are enforced by tests at build time (Section 11), not by database privileges. The rules and the owner list are in [backend conventions §2](../docs/engineering/backend-conventions.md#2-each-persistence-owner-has-its-own-postgresql-schema).

A separate private Python analysis runtime and authorized private object storage are selected architectural boundaries, with integration deferred to relevant stories. No internal HTTP, business microservices, dedicated cache, broker, gateway or Kubernetes is required initially. Future extraction still needs contract, authorization, consistency and data-migration design.

See [Selected Decisions](../docs/04-architecture/selected-architecture-decisions.md), [ADD](../docs/04-architecture/ADD.md) and [Logical View](../docs/04-architecture/views/logical-view.puml) for authoritative detail.

## 4. Business modules

These responsibilities are documented boundaries. So far only Identity's registration and login-session lifecycle are implemented.

| Owner / package below `com.capsuleai` | Responsibility | PostgreSQL schema |
| --- | --- | --- |
| Identity — `identity` | Account identity, authentication/session/reset authority and recovery lifecycle | `identity` (in use) |
| Personal Context — `personalcontext` | Declared preferences/needs, selected location and weather/context meaning | `personal_context` (not yet created) |
| Wardrobe — `wardrobe` | Confirmed garments, membership, unevaluated recommendation eligibility, confirmation/correction and analysis/media lifecycle | `wardrobe` (not yet created) |
| Outfit Advice and Use — `outfitadviceanduse` | Advice, Shuffle, feedback, Wear/history and published outfit-evaluation semantics | `outfit_advice_and_use` (not yet created) |
| Wardrobe Intelligence — `wardrobeintelligence` | Coverage/Gaps, hypothetical utility, result states and shopping support | `wardrobe_intelligence` (not yet created) |
| Product Measurement — `productmeasurement` | Purpose-limited measurement/lifecycle, independent of core acceptance | `product_measurement` (not yet created) |

`CapsuleAiApplication` remains at the parent package `com.capsuleai` for component scanning. Package documentation and Java `public` visibility do not enforce architectural boundaries by themselves.

## 5. Project structure

System-wide documentation stays outside `backend`. This tree shows actual engineering files; `target` is generated.

```text
CapsuleAI/
├── .env.example                         # Nonsecret template; real .env is ignored
├── .gitignore / .gitattributes           # Exclusions and Wrapper line endings
├── compose.yaml                         # Development PostgreSQL
├── .github/workflows/backend-ci.yml
├── docs/                                # Requirements, architecture, Scrum, testing, setup guide
├── Initial files/                       # Canonical Scrum workflow and historical sources
└── backend/
    ├── README.md
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    ├── .mvn/wrapper/maven-wrapper.properties
    ├── src/main/
    │   ├── java/com/capsuleai/
    │   │   ├── CapsuleAiApplication.java
    │   │   ├── identity/                    # Sprint 1: api, domain, application(.ports), adapter.in/out/jwt, configuration
    │   │   ├── personalcontext/package-info.java
    │   │   ├── wardrobe/package-info.java
    │   │   ├── outfitadviceanduse/package-info.java
    │   │   ├── wardrobeintelligence/package-info.java
    │   │   └── productmeasurement/package-info.java
    │   └── resources/
    │       ├── application.properties
    │       ├── application-local.properties
    │       └── db/migration/                # V1 foundation; V2–V5 Identity tables
    ├── src/test/java/com/capsuleai/
    │   ├── foundation/                      # HarnessSmokeTest, DatabaseSmokeIT
    │   ├── architecture/ArchitectureDependencyTest.java
    │   ├── architecturefixture/             # Deliberate violations for the ArchUnit detectors
    │   └── identity/                        # Unit, PostgreSQL, security-chain and contract tests
    ├── src/test/resources/application-test.properties   # Disposable test-only signing key
    └── target/                          # Ignored classes, JAR and reports
```

Do not create empty role directories or placeholder services merely to reproduce a diagram.

## 6. Prerequisites

Use **Windows PowerShell**, adapting `D:\CapsuleAI` below to your checkout.

- JDK **21**; align `JAVA_HOME`, compiler, Maven runtime and the editor's Java runtime.
- Docker Desktop with a reachable **Linux-container engine** and `docker compose`.
- Git; VS Code with Java language/debug/test/Maven extensions is recommended.
- Network access for initial Maven/dependency/image downloads.

Global Maven is optional: use the committed Wrapper. Host `psql` is optional; the commands below use PostgreSQL's container client.

From repository root:

```powershell
Set-Location 'D:\CapsuleAI'
java --version
javac --version
.\backend\mvnw.cmd --version
docker info --format '{{.ServerVersion}} {{.OSType}}'
docker compose version
git status --short
```

Java/compiler/Wrapper should agree on Java 21; Docker should report Linux. Run **Maven project commands from `backend`** and **Compose commands from root**. Stop on failure and inspect the first useful error; check `$LASTEXITCODE` after native commands.

## 7. Local development setup

Verify secret exclusions and create private configuration only if absent:

```powershell
Set-Location 'D:\CapsuleAI'
git check-ignore -v .env
if (-not (Test-Path -LiteralPath '.\.env')) {
    Copy-Item -LiteralPath '.\.env.example' -Destination '.\.env'
}
git ls-files -- .env
```

The first command should identify an ignore rule; the last must print nothing. Edit `.env` privately and set a development password. Preserve existing credentials/data if it already exists.

The committed template contains:

```dotenv
POSTGRES_DB=capsuleai_dev
POSTGRES_USER=capsuleai_dev
POSTGRES_PASSWORD=
POSTGRES_HOST_PORT=5433
```

Compose rejects an empty password. Do not commit real credentials or publish resolved configuration/screenshots. For special dotenv characters, follow [Compose interpolation/quoting rules](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/).

**Compose's `.env` does not configure Spring Boot automatically.** Section 9 supplies Spring's separate `CAPSULE_DB_*` variables. Changed database names/users/ports must agree across both configurations. Existing shell `POSTGRES_*` variables can override Compose interpolation.

## 8. Running PostgreSQL

From root:

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env config --quiet
docker compose --env-file .env up -d --wait --wait-timeout 60 postgres
docker compose --env-file .env ps
docker compose --env-file .env port postgres 5432
```

Quiet validation avoids displaying resolved credentials. Startup waits for service health and leaves PostgreSQL running in the background.

With the example database/user names:

```powershell
docker compose --env-file .env exec postgres pg_isready -U capsuleai_dev -d capsuleai_dev
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT current_database(), current_user, version();'
Test-NetConnection -ComputerName localhost -Port 5433
```

Adapt names/port if changed. The host-run backend connects through `localhost:5433` to container port `5432`. Service DNS `postgres` applies inside the Compose network. Container readiness/SQL and TCP reachability do not establish host JDBC authentication; Boot startup checks that separately.

The named `pgdata` volume preserves development data at PostgreSQL 17's `/var/lib/postgresql/data`. Initialization variables apply to an empty data directory: editing `.env` does not update an existing database's password.

Stop without deleting data:

```powershell
docker compose --env-file .env stop postgres
```

Repeat the startup command for later sessions. Do not remove volumes/reinitialize data to bypass an unexplained authentication or migration problem.

## 9. Running Spring Boot

Start PostgreSQL first. In the **same terminal that will launch Boot**, set the connection and enter the actual database password:

```powershell
Set-Location 'D:\CapsuleAI\backend'
$env:CAPSULE_DB_URL = 'jdbc:postgresql://localhost:5433/capsuleai_dev'
$env:CAPSULE_DB_USER = 'capsuleai_dev'
$dbPassword = Read-Host 'Local database password' -AsSecureString
$env:CAPSULE_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbPassword).Password
```

Adapt URL/user to the private Compose configuration. The prompt avoids a password literal in shell history; JDBC still receives a plaintext child-process value.

**Configuration caveat:** current `application-local.properties` includes an instructional literal password fallback. Always supply `CAPSULE_DB_PASSWORD` explicitly; the fallback is not your configured database credential.

Startup also **requires an access-token signing secret** (Base64 of at least 32 random bytes) and deliberately has no default. In the same terminal:

```powershell
$secretBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($secretBytes)
$env:CAPSULE_JWT_SIGNING_SECRET = [Convert]::ToBase64String($secretBytes)
```

A new secret on each start only invalidates earlier access tokens, which is fine for development. Never commit or share the value.

**Password-reset email (US-AUTH-005).** The `local` profile sends reset instructions to the Mailpit service in [Compose](../compose.yaml) (SMTP `127.0.0.1:1025`); nothing leaves the machine. Start it from the repository root and read received mail at <http://localhost:8025>:

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env up -d mailpit
```

To use a real SMTP provider instead, set its values in the launching terminal before starting Boot (prompt for the password as for the database password). `CAPSULE_PASSWORD_RESET_LINK` is the link the mobile app opens; the reset secret is appended as `#token=…`. Outside local development it must be an `https` address on the verified domain that the Android App Links and iOS Universal Links of the app are associated with (OPEN-S2-02); a custom scheme such as `capsuleai://` starts only where `capsuleai.identity.password-reset.development-custom-scheme-allowed=true` (the `local` profile sets it):

```powershell
$env:CAPSULE_MAIL_HOST = 'smtp.example.com'
$env:CAPSULE_MAIL_PORT = '587'
$env:CAPSULE_MAIL_USERNAME = 'apikey-or-user'
$mailPassword = Read-Host 'SMTP password' -AsSecureString
$env:CAPSULE_MAIL_PASSWORD = [System.Net.NetworkCredential]::new('', $mailPassword).Password
$env:CAPSULE_MAIL_SMTP_AUTH = 'true'
$env:CAPSULE_MAIL_STARTTLS = 'true'
$env:CAPSULE_MAIL_FROM = 'no-reply@your-domain.example'
$env:CAPSULE_PASSWORD_RESET_LINK = 'capsuleai://reset-password'
```

**Recovery protection and retention (US-AUTH-005).** `capsuleai.identity.password-reset.protection.*` sets the attempt limits: requests per normalized email (default 3), requests per client address (20), and link checks/completions per client address (30), each per 15-minute window. It also sets the minimum response time of a successful request (250 ms). These are conservative technical defaults (TD-S2-26), counted in PostgreSQL, so they hold across restarts and instances. The client address is the servlet `remoteAddr`. Behind a reverse proxy, enable Spring Boot's forwarded-header support (`server.forward-headers-strategy=native`) and restrict `server.tomcat.remoteip.internal-proxies` to the real proxy; otherwise every user shares the proxy's address. A scheduled Identity job deletes reset interactions 30 days after they end (`capsuleai.identity.password-reset.cleanup.*`).

**Before release (OPEN-S2-09).** Mailpit proves the SMTP adapter, not delivery. A release requires:

- A real production-capable provider.
- A sender domain with SPF, DKIM and DMARC.
- STARTTLS or SMTPS (`CAPSULE_MAIL_STARTTLS=true` or the provider's TLS port).
- Recorded evidence that a reset email reached a real mailbox.

Choose **one** startup method in that terminal.

**Maven plugin:**

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

**Executable JAR alternative:**

```powershell
.\mvnw.cmd package
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed; inspect the Maven output.' }
java -jar .\target\capsuleai-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Expect logs for `CapsuleAiApplication`, profile `local`, PostgreSQL/Flyway and **127.0.0.1:8080**. No-profile startup lacks a datasource URL for this JDBC application.

From a second terminal:

```powershell
Test-NetConnection -ComputerName localhost -Port 8080
Get-NetTCPConnection -LocalPort 8080 -State Listen |
    Select-Object LocalAddress, OwningProcess
```

Correlate the listener with the application's logged Java PID. `GET /` returning **401** with an `AUTHENTICATION_REQUIRED` problem body is expected: every route except registration, login and refresh requires a bearer access token, and no health endpoint exists. The [OpenAPI contract](../docs/05-api/openapi.yaml) lists the available operations.

Stop Boot with **Ctrl+C** before switching methods. Clear the launching terminal's temporary settings when finished:

```powershell
$env:CAPSULE_DB_PASSWORD = $null
$dbPassword = $null
$env:CAPSULE_DB_URL = $null
$env:CAPSULE_DB_USER = $null
$env:CAPSULE_JWT_SIGNING_SECRET = $null
$secretBytes = $null
$env:CAPSULE_CORS_ALLOWED_ORIGINS = $null
$env:CAPSULE_MAIL_PASSWORD = $null
$mailPassword = $null
```

See [Boot profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html) and [Maven plugin options](https://docs.spring.io/spring-boot/maven-plugin/run.html).

### Browser access (CORS)

A browser lets a page read responses from another origin only if the server grants that origin. The backend grants **only the exact origins in `capsuleai.cors.allowed-origins`**, which `CAPSULE_CORS_ALLOWED_ORIGINS` sets as a comma-separated list:

| Configuration | Allowed browser origins |
| --- | --- |
| Base configuration (any deployment) | None until `CAPSULE_CORS_ALLOWED_ORIGINS` lists them |
| `local` profile | `https://editor.swagger.io`, unless `CAPSULE_CORS_ALLOWED_ORIGINS` replaces the list |

Write each entry exactly as the browser sends it, `scheme://host[:port]`: no path, trailing slash, wildcard or default port (`:443`, `:80`). Startup fails on anything else. Native Android/iOS apps send no `Origin` header and need no entry.

A granted origin may use `GET` and `POST` with the `Authorization`, `Content-Type` and `Accept` request headers; browsers may cache a granted preflight for 30 minutes. Credentials mode (cookies) stays off: send the access token as `Authorization: Bearer …`. CORS only lets the page *read* responses. Every request is still authenticated exactly as from any other client, and an unlisted origin gets `403 Invalid CORS request` with no grant.

To also allow a browser frontend during development, list its real dev-server origin next to the editor in the launching terminal, then start Boot (`5173` is only an example):

```powershell
$env:CAPSULE_CORS_ALLOWED_ORIGINS = 'https://editor.swagger.io,http://localhost:5173'
```

In production, set only the deployed web client's origins, for example `https://app.example.com`. Do not list the Swagger Editor or development origins there.

Check a preflight from a second terminal. Expect `200`, `Access-Control-Allow-Origin: https://editor.swagger.io` and `Access-Control-Allow-Methods: GET,POST`; an unlisted origin returns `403` without those headers:

```powershell
curl.exe -i -X OPTIONS http://localhost:8080/api/v1/auth/login `
    -H 'Origin: https://editor.swagger.io' `
    -H 'Access-Control-Request-Method: POST' `
    -H 'Access-Control-Request-Headers: content-type'
```

**Swagger Editor.** Open `https://editor.swagger.io` in a browser on the machine that runs Boot (it listens on `127.0.0.1` only) and paste the [OpenAPI contract](../docs/05-api/openapi.yaml), whose server is `http://localhost:8080`. Use **Try it out** to register and log in, then **Authorize** with the returned `accessToken` for logout. Chrome and Edge ask once whether the editor may access apps on this device ([Local Network Access](https://developer.chrome.com/blog/local-network-access)). Allow it; a denied prompt blocks requests in the browser before they reach the backend.

## 10. Database migrations

The Boot Flyway starter, PostgreSQL Flyway module and JDBC driver apply real migrations on configured startup. Common properties select `classpath:db/migration` and disable Flyway clean.

`V1__foundation.sql` establishes migration plumbing. V2–V5 create and evolve the Identity tables `identity_account`, `identity_session` and `identity_refresh_token` in Flyway's default schema. `V6__move_identity_tables_into_identity_schema.sql` moves those three tables, with all their rows, indexes and constraints, into the `identity` schema (AR-01); it copies and drops nothing. `V7__create_identity_password_reset.sql` (US-AUTH-005) adds `identity.identity_password_reset`, its attempt counters `identity.identity_password_reset_attempt` and the `PASSWORD_RESET` session-revocation reason. Flyway maintains `flyway_schema_history`, which stays in the default schema. After successful startup, inspect it from root:

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;'
```

Expect successful rows for versions `1` to `7`. Restart Boot using the same settings and confirm each remains applied once. List the Identity tables in their schema:

```powershell
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c '\dt identity.*'
```

Query them schema-qualified, for example `SELECT count(*) FROM identity.identity_account;`. An unqualified `identity_account` no longer resolves.

**Privileges V6 needs.** `ALTER TABLE … SET SCHEMA` may only be run by the owner of the table, and `CREATE SCHEMA` needs the `CREATE` privilege on the database. Both hold when the role that runs Flyway is the role that ran V1–V5, as in every current environment. If a deployment migrates with a different role, V6 fails before changing anything (it is one transaction). Grant the privileges, or run Flyway as the owning role, and start again.

Future files follow `V<version>__<description>.sql` with **two underscores**:

- Add a new migration for selected-story changes; do not edit an already applied shared migration.
- Each migration after V6 changes exactly one owner schema and qualifies every table.
- The first story that persists a module's state creates that module's schema (`CREATE SCHEMA IF NOT EXISTS wardrobe;`).
- Do not change Flyway's `schemas`, `default-schema` or `table` settings: the schema history would move, and Flyway would try to re-apply V1–V5 on existing databases.
- Diagnose checksum mismatches rather than automatically cleaning, repairing or resetting data.

## 11. Automated testing

| Suite | Purpose | Infrastructure |
| --- | --- | --- |
| Fast foundation — `HarnessSmokeTest` | Test-only behavior with a fake, Mockito stub/call verification and AssertJ assertions | No Spring, database or Docker |
| Architecture — `ArchitectureDependencyTest` | Framework-free domain/application/API; modules use each other only through `api`; no private model in an `api`; Spring JDBC only in persistence adapters/configuration; no module cycles; every module registered in `BusinessModule`; independent inbound/outbound adapters; deliberate-violation detectors for each rule | Compiled classes; no Docker/database |
| SQL ownership — `PersistenceOwnershipSqlTest` | Production SQL uses only its own module's schema, always qualified; each migration after V6 changes one owner schema; detectors for cross-context joins, unqualified tables and cross-schema foreign keys | Source files; no Docker/database |
| Identity unit — `identity/**/*Test` | Email, password, display-name, session and rotation policies; use cases with in-memory fakes; JWT converter and error translation; CORS origin validation (`CorsPropertiesTest`) | No database or Docker |
| Integration — `DatabaseSmokeIT` | Boot wiring, real SQL and no pending migration | Docker; disposable PostgreSQL |
| Schema ownership — `DatabaseSchemaOwnershipIT` | On the migrated database: only owner schemas besides `public`; `public` holds only Flyway's history; tables in their owner's schema; no cross-schema foreign key or view | Docker; PostgreSQL catalog |
| Identity relocation — `IdentitySchemaRelocationIT` | V6 on a realistic V1–V5 database with accepted accounts, sessions and refresh tokens: rows, indexes and constraints survive, adapters work, history stays valid, a restart applies nothing; fresh databases migrate completely | Docker; a separate database per test (`IsolatedMigrationDatabase`) |
| Identity integration — `identity/**/*IT` | Schema constraints; registration/login/logout/refresh APIs; security chain; cross-origin browser access (`CorsSecurityIT`); concurrency; session-authority outage (`SessionAuthorityOutageIT`); OpenAPI contract (`IdentityOpenApiContractIT`) | Docker; real application, filter chain and PostgreSQL |

JUnit Jupiter runs the tests; AssertJ expresses outcomes and Mockito controls selected dependencies.

From `backend`, run the fast unit/architecture tests:

```powershell
Set-Location 'D:\CapsuleAI\backend'
.\mvnw.cmd test
```

Expected: zero failures/errors/skips. Focus one class for diagnosis:

```powershell
.\mvnw.cmd '-Dtest=HarnessSmokeTest' test
.\mvnw.cmd '-Dtest=ArchitectureDependencyTest' test
```

For the complete suite, leave Docker running:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

Expected: every Surefire and Failsafe suite passes with zero failures, errors or skips; read the actual counts from the reports. To select one IT:

```powershell
.\mvnw.cmd '-Dit.test=DatabaseSmokeIT' verify
```

This still runs fast tests; `it.test` selects Failsafe's integration class.

`DatabaseSmokeIT` uses `@SpringBootTest(NONE)`, profile `test` and a Testcontainers 2.x PostgreSQL container with `@ServiceConnection`. Identity ITs extend `IdentityIntegrationTestSupport`: the full application and security chain through MockMvc, Testcontainers PostgreSQL, a controllable clock and the disposable signing key in `application-test.properties`. Boot receives temporary connection details; development Compose, private .env credentials and a fixed host port are unnecessary. Docker must remain running even if the development database is stopped.

Inspect fresh reports:

```powershell
Get-Content .\target\surefire-reports\*.txt
Get-Content .\target\failsafe-reports\*.txt
```

Surefire runs `*Test` classes during `test`. Failsafe runs `*IT` during `integration-test` and enforces results during `verify`. Thus `test`/`package` omit the database IT; do not stop verification at `integration-test`. Both plugins reject an entirely empty suite; CI additionally requires the named reports described in Section 13.

The invalid architecture fixtures belong in **test sources**. Each detector passes when its expected violation is found. The rules tolerate empty feature roles. They check dependencies, SQL text and database structure, not runtime authorization, and the single database role can still reach every schema at runtime. Extend them with real code.

Mockito can print a Java 21 self-attachment warning while tests still pass. Consult its version-specific agent guidance when maintaining the toolchain; do not skip tests to hide infrastructure failures.

## 12. Maven commands

Run project commands from `backend` using the Wrapper.

| Command | Current effect |
| --- | --- |
| `.\mvnw.cmd compile` | Compile production Java and copy resources |
| `.\mvnw.cmd test` | Compile tests and run the fast unit/architecture suites; no Docker |
| `.\mvnw.cmd package` | Run preceding phases and package the executable JAR; no database IT |
| `.\mvnw.cmd verify` | Include packaging, Failsafe IT and result enforcement |
| `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify` | Remove stale target output and repeat complete unit + integration verification |
| `.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'` | Start the configured development application; not build/test verification |

Lifecycle phases include earlier phases. Packaging success does not prove IT execution. Linux CI uses `bash ./mvnw` for the same pinned distribution. See [Failsafe usage](https://maven.apache.org/surefire/maven-failsafe-plugin/usage.html).

## 13. Continuous integration

Root [backend-ci.yml](../.github/workflows/backend-ci.yml) runs for pushes to `main` and PRs targeting `main`.

The `verify` job uses Ubuntu 24.04, Temurin 21 and PowerShell run steps. It checks out source, caches Maven dependencies using the backend POM, checks Docker, then runs `bash ./mvnw --batch-mode --no-transfer-progress clean verify` from `backend`.

A guard requires a named set of Surefire/Failsafe XML reports (foundation, architecture and the Sprint 1 Identity suites, including `SessionAuthorityOutageIT` and `IdentityOpenApiContractIT`), each with at least its stated test count and no failures/errors/skips. Reports upload as `backend-test-reports` even after failure; upload success cannot erase a failed build. Paths remain relative to the repository root. Permissions are read-only, checkout credentials are not persisted and the job timeout is 15 minutes. Testcontainers supplies PostgreSQL; CI does not start persistent Compose.

**Baseline evidence:** [PR #1](https://github.com/luubinhdeptrai/CapsuleAI/pull/1) merged at `f0a6480`. Its [main-branch run](https://github.com/luubinhdeptrai/CapsuleAI/actions/runs/39603004558) completed successfully through Docker/build/report guard/upload, inspected on 2026-10-21. Check the latest relevant revision for later changes. Hosted CI does not verify a developer's private local credentials.

## 14. Development workflow

1. Inspect existing work, update your baseline through the normal Git process and use a scoped branch. Do not push directly to `main`.
2. Refine candidate Story/AC, feasibility and dependencies; Sprint Planning selects work **before** full implementation-level design.
3. For selected work, use **JIT / Just-Enough Design → Automated Test + Implementation → Refactor → Integration Verification → PR / CI / Review**. Selective TDD is useful; strict TDD is not mandatory.
4. Preserve ownership/private models and update affected contracts, migrations, tests and source documents when their truth changes.
5. For code/configuration changes, run focused checks and full `clean verify` before review; inspect actual counts and current CI.
6. Stage explicit intended paths and open a PR linked to relevant PBI/Story IDs. Resolve required CI/review findings before completion.

Before a commit, from root:

```powershell
Set-Location 'D:\CapsuleAI'
git status --short
git diff --check
git diff --cached --check
git diff --cached --name-only
```

Stage with `git add -- <actual-intended-path>` before inspecting the staged change. Exclude private .env, generated target output and secrets. Story AC plus the [Definition of Done](../docs/06-scrum/definition-of-done.md) govern completion; foundation completion does not mark product stories Done.

## 15. Current limitations and next steps

Production source contains Identity's Sprint 1 authentication: registration, login, JWT plus persistent session authority on every protected request, refresh rotation and current-session logout. Sprint 2 adds email password recovery with account-wide session revocation. Wardrobe and all other business endpoints and tables, Python/media/provider integrations and production deployment remain unimplemented; recovery has been verified against Mailpit, not against a chosen production email provider, and real delivery evidence is still required before release.

Architecture tests protect module boundaries, SQL ownership and schema placement at build time; database privileges do not separate schemas at runtime. The local-password fallback noted in Section 9 remains a configuration caveat; explicitly supply private credentials and check setup on each actual environment.

After normal foundation handoff, follow **Backlog Refinement → Sprint Planning → selected-story JIT design and automated implementation**. Add only needed APIs/data detail/interaction/state models. Do not predesign the whole MVP or duplicate baseline decisions into ADR files; ADA/Selected Decisions already record them.

## 16. Troubleshooting

| Symptom | First check |
| --- | --- |
| Wrapper fails / Java mismatch | Compare Java/compiler/Wrapper and JAVA_HOME; align editor runtime. First downloads need network access. |
| Datasource URL missing | Activate `local` and supply settings in the launching terminal; .env configures Compose. |
| Startup: signing-secret not configured / too short | Set `CAPSULE_JWT_SIGNING_SECRET` in the launching terminal (Section 9): Base64 of at least 32 bytes. |
| Protected request returns 503 `TEMPORARILY_UNAVAILABLE` | PostgreSQL is unreachable, so session authority cannot be checked; the token was not found invalid. Check the database (Section 8) and retry. |
| Password authentication fails | Check intended user/database/port and private password against original initialization; editing .env does not update an existing volume. |
| Docker/Testcontainers unavailable | Check Linux engine and `docker info`; do not substitute the development database or skip the required IT. |
| Port 8080/5433 occupied | Identify its owner; stop your own earlier instance or consistently change database port/application URL. Do not kill unknown processes. |
| Integration tests did not run | `test`/`package` intentionally omit ITs; use `verify` and inspect Failsafe. |
| Invalid architecture fixture | Intentional test-only violation; its detector must find it. Production import excludes tests. |
| Root HTTP 401 | Expected: unknown routes are protected by default. |
| Startup: `password-reset.sender-address` or `instruction-link is not configured` | Run with the `local` profile (it has Mailpit defaults) or set `CAPSULE_MAIL_FROM` and `CAPSULE_PASSWORD_RESET_LINK` (Section 9). |
| Forgot Password answers 202 but no email arrives | Expected for an unregistered email. Otherwise check Mailpit at <http://localhost:8025>, then the log line "Password-reset instructions could not be handed to the mail server" and the `CAPSULE_MAIL_*` settings. The response is deliberately the same either way. |
| Startup: `capsuleai.cors.allowed-origins entry … is not an exact origin` | Fix that entry in `CAPSULE_CORS_ALLOWED_ORIGINS`: `scheme://host[:port]` only (Section 9, Browser access). |
| Browser console: blocked by CORS policy; preflight `403` | The page's origin is not listed exactly (scheme, host and port must match), or it asked for a method/header other than `GET`/`POST` and `Authorization`/`Content-Type`/`Accept`. Fix the list and restart Boot. |
| Browser: `Failed to fetch` and the backend logs nothing | Not CORS. Check that Boot runs on the server URL's port, that the browser runs on the same machine, and that the Local Network Access prompt was allowed (site settings). Use Chrome, Edge or Firefox if a browser blocks `http://localhost` from an HTTPS page as mixed content. |
| Preflight succeeds but the request returns 401 | Expected without a usable access token: log in again or renew, then **Authorize** with the new `accessToken`. CORS never authenticates a request. |
| Flyway checksum mismatch | Inspect the applied change and use a reviewed forward migration; do not reset/repair blindly. |
| `relation "identity_account" does not exist` | Since V6 the Identity tables are in the `identity` schema. Write `identity.identity_account` (all SQL is schema-qualified). |
| `PersistenceOwnershipSqlTest` fails | Read the message. It names the file and line, and either the qualified table name to use or the other module whose table was used. In the second case, use that module's published `api` instead of its table. |

Share the nonsecret command, directory and minimal error. Do not publish environment dumps, resolved Compose configuration or sensitive logs.

## 17. Authoritative references

- [Engineering Baseline Setup Guide](../docs/engineering/engineering-baseline-setup-guide.md) — detailed checkpoint learning/troubleshooting.
- [Selected Decisions](../docs/04-architecture/selected-architecture-decisions.md), [Decision Analysis](../docs/04-architecture/architecture-decision-analysis.md) and [ADD](../docs/04-architecture/ADD.md) — architecture selections/rationale/realization.
- [Logical](../docs/04-architecture/views/logical-view.puml), [Implementation](../docs/04-architecture/views/implementation-view.puml), [Deployment](../docs/04-architecture/views/deployment-view.puml) and [Data](../docs/04-architecture/views/data-view.puml) Views — canonical boundaries.
- [Test Strategy](../docs/07-testing/test-strategy.md) and [Definition of Done](../docs/06-scrum/definition-of-done.md) — verification/completion.
- [Scrum Development Workflow](../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) — process authority.

This README describes the current executable foundation; requirements, architecture and workflow retain their own authority.
