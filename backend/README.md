# CapsuleAI Backend

## 1. Project overview

CapsuleAI helps users understand their wardrobe, choose outfits and evaluate wardrobe improvements. This backend is its Java/Spring Boot business core, organized as one **Modular Monolith**.

The **Pre-Sprint Engineering Baseline is implemented and merged into `main`** through [PR #1](https://github.com/luubinhdeptrai/CapsuleAI/pull/1). It provides the application/build foundation, six documented module boundaries, PostgreSQL configuration, Flyway migrations, automated smoke/dependency tests and GitHub Actions verification.

Product features are not implemented yet: registration/login/JWT, garments, outfits, Wear/history, Coverage/Multiplier, shopping and real provider integrations remain selected-story work. There are no product HTTP endpoints or business tables. This foundation is not a production deployment or a completed product Increment.

## 2. Technology stack

Versions come from [pom.xml](pom.xml), [Wrapper configuration](.mvn/wrapper/maven-wrapper.properties), [Compose](../compose.yaml) and Boot's [4.1.1 dependency BOM](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom).

| Technology | Adopted version | Purpose |
| --- | --- | --- |
| Java | 21 | Compilation/runtime |
| Spring Boot / servlet MVC | 4.1.1 | Application configuration and embedded HTTP server |
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

The [Implementation View](../docs/04-architecture/views/implementation-view.puml) defines the following role conventions. Currently each owner contains only useful `package-info.java` documentation; add roles/classes when actual selected work requires them.

| Role inside an owner | Responsibility |
| --- | --- |
| `api` | Narrow, framework-free contract published to other owners |
| `domain` | Business invariants; no Spring, persistence, HTTP/JSON or provider dependencies |
| `application` / `application.ports` | Use-case coordination and required persistence/integration/neighbor outcomes; dependencies point inward |
| `adapter.in` / `adapter.out` | Incoming translation and implementations of required ports |
| `configuration` | Outer framework wiring of that owner's private implementations |

Cross-module business collaboration uses the supplier's published `api`, not private domain/application/repository/entity types. This local Java contract differs from an HTTP controller. Module configuration wires its own internals; root bootstrap delegates assembly. One PostgreSQL database does not grant unrestricted access to another owner's persistence.

A separate private Python analysis runtime and authorized private object storage are selected architectural boundaries, with integration deferred to relevant stories. No internal HTTP, business microservices, dedicated cache, broker, gateway or Kubernetes is required initially. Future extraction still needs contract, authorization, consistency and data-migration design.

See [Selected Decisions](../docs/04-architecture/selected-architecture-decisions.md), [ADD](../docs/04-architecture/ADD.md) and [Logical View](../docs/04-architecture/views/logical-view.puml) for authoritative detail.

## 4. Business modules

These responsibilities are documented boundaries, not implemented product capabilities.

| Owner / package below `com.capsuleai` | Responsibility |
| --- | --- |
| Identity — `identity` | Account identity, authentication/session/reset authority and recovery lifecycle |
| Personal Context — `personalcontext` | Declared preferences/needs, selected location and weather/context meaning |
| Wardrobe — `wardrobe` | Confirmed garments, membership/readiness, confirmation/correction and analysis/media lifecycle |
| Outfit Advice and Use — `outfitadviceanduse` | Advice, Shuffle, feedback, Wear/history and published outfit-evaluation semantics |
| Wardrobe Intelligence — `wardrobeintelligence` | Coverage/Gaps, hypothetical utility, result states and shopping support |
| Product Measurement — `productmeasurement` | Purpose-limited measurement/lifecycle, independent of core acceptance |

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
    │   │   ├── identity/package-info.java
    │   │   ├── personalcontext/package-info.java
    │   │   ├── wardrobe/package-info.java
    │   │   ├── outfitadviceanduse/package-info.java
    │   │   ├── wardrobeintelligence/package-info.java
    │   │   └── productmeasurement/package-info.java
    │   └── resources/
    │       ├── application.properties
    │       ├── application-local.properties
    │       └── db/migration/V1__foundation.sql
    ├── src/test/java/com/capsuleai/
    │   ├── foundation/HarnessSmokeTest.java
    │   ├── foundation/DatabaseSmokeIT.java
    │   ├── architecture/ArchitectureDependencyTest.java
    │   └── architecturefixture/domain/InvalidDomainFixture.java
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

Correlate the listener with the application's logged Java PID. `GET /` returning **404** is expected: no product or health endpoint has been added.

Stop Boot with **Ctrl+C** before switching methods. Clear the launching terminal's temporary settings when finished:

```powershell
$env:CAPSULE_DB_PASSWORD = $null
$dbPassword = $null
$env:CAPSULE_DB_URL = $null
$env:CAPSULE_DB_USER = $null
```

See [Boot profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html) and [Maven plugin options](https://docs.spring.io/spring-boot/maven-plugin/run.html).

## 10. Database migrations

The Boot Flyway starter, PostgreSQL Flyway module and JDBC driver apply real migrations on configured startup. Common properties select `classpath:db/migration` and disable Flyway clean.

`V1__foundation.sql` runs `SELECT 1;` and establishes migration plumbing; it creates no business tables. Flyway maintains `flyway_schema_history`. After successful startup, inspect it from root:

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;'
```

Expect one successful version `1` row. Restart Boot using the same settings and confirm V1 remains applied once.

Future files follow `V<version>__<description>.sql` with **two underscores**. Add a new migration for selected-story changes; do not edit an already applied shared migration. Business schemas/ORM/transactions remain story-specific design. Diagnose checksum mismatches rather than automatically cleaning, repairing or resetting data.

## 11. Automated testing

| Suite / current class | Cases | Purpose | Infrastructure |
| --- | ---: | --- | --- |
| Fast foundation — `HarnessSmokeTest` | 2 | Test-only behavior with a fake, Mockito stub/call verification and AssertJ assertions | No Spring, database or Docker |
| Architecture — `ArchitectureDependencyTest` | 2 | Production import/initial dependency rules and intentional fixture violation detection | Compiled classes; no Docker/database |
| Integration — `DatabaseSmokeIT` | 1 | Boot wiring, real SQL, V1 history and no pending migration | Docker; disposable PostgreSQL |

JUnit Jupiter runs the tests; AssertJ expresses outcomes and Mockito controls selected dependencies. These smoke cases verify infrastructure, not product requirement coverage.

From `backend`, run fast/architecture tests:

```powershell
Set-Location 'D:\CapsuleAI\backend'
.\mvnw.cmd test
```

Expected: **4 tests**, zero failures/errors/skips. Focus either class for diagnosis:

```powershell
.\mvnw.cmd '-Dtest=HarnessSmokeTest' test
.\mvnw.cmd '-Dtest=ArchitectureDependencyTest' test
```

Each focused command runs **2 cases**. For the complete suite, leave Docker running:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

Expected: **4 Surefire cases + 1 Failsafe IT**, all non-skipped. To select the existing IT:

```powershell
.\mvnw.cmd '-Dit.test=DatabaseSmokeIT' verify
```

This still runs fast tests; `it.test` selects Failsafe's integration class.

`DatabaseSmokeIT` uses `@SpringBootTest(NONE)`, profile `test` and a Testcontainers 2.x PostgreSQL container with `@ServiceConnection`. Boot receives its temporary connection details; development Compose, private .env credentials and a fixed host port are unnecessary. Docker must remain running even if the development database is stopped.

Inspect fresh reports:

```powershell
Get-Content .\target\surefire-reports\*.txt
Get-Content .\target\failsafe-reports\*.txt
```

Surefire runs `*Test` classes during `test`. Failsafe runs `*IT` during `integration-test` and enforces results during `verify`. Thus `test`/`package` omit the database IT; do not stop verification at `integration-test`. Both plugins reject an entirely empty suite; CI additionally checks the three named reports.

The invalid architecture fixture belongs in **test sources**. Its detector passes when the expected violation is found. Initial rules tolerate empty feature roles and do not prove full API/cycle restrictions, Clean conformance, SQL ownership or runtime authorization. Extend useful checks with real code.

Mockito can print a Java 21 self-attachment warning while tests still pass. Consult its version-specific agent guidance when maintaining the toolchain; do not skip tests to hide infrastructure failures.

## 12. Maven commands

Run project commands from `backend` using the Wrapper.

| Command | Current effect |
| --- | --- |
| `.\mvnw.cmd compile` | Compile production Java and copy resources |
| `.\mvnw.cmd test` | Compile tests and run 4 fast/architecture cases |
| `.\mvnw.cmd package` | Run preceding phases and package the executable JAR; no database IT |
| `.\mvnw.cmd verify` | Include packaging, Failsafe IT and result enforcement |
| `.\mvnw.cmd --batch-mode --no-transfer-progress clean verify` | Remove stale target output and repeat complete 5-case verification |
| `.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'` | Start the configured development application; not build/test verification |

Lifecycle phases include earlier phases. Packaging success does not prove IT execution. Linux CI uses `bash ./mvnw` for the same pinned distribution. See [Failsafe usage](https://maven.apache.org/surefire/maven-failsafe-plugin/usage.html).

## 13. Continuous integration

Root [backend-ci.yml](../.github/workflows/backend-ci.yml) runs for pushes to `main`/`chore/engineering-baseline` and PRs targeting `main`.

The `verify` job uses Ubuntu 24.04, Temurin 21 and PowerShell run steps. It checks out source, caches Maven dependencies using the backend POM, checks Docker, then runs `bash ./mvnw --batch-mode --no-transfer-progress clean verify` from `backend`.

A guard requires harness/architecture/IT XML reports with minimum counts **2/2/1** and no failures/errors/skips. Reports upload as `backend-test-reports` even after failure; upload success cannot erase a failed build. Paths remain relative to the repository root. Permissions are read-only, checkout credentials are not persisted and the job timeout is 15 minutes. Testcontainers supplies PostgreSQL; CI does not start persistent Compose.

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

Production source currently contains bootstrap and module ownership documentation. Business endpoints, session/security enforcement, feature tables, domain behavior, Python/media/provider integrations and production deployment remain unimplemented.

Architecture tests provide initial static protection. The local-password fallback noted in Section 9 remains a configuration caveat; explicitly supply private credentials and check setup on each actual environment.

After normal foundation handoff, follow **Backlog Refinement → Sprint Planning → selected-story JIT design and automated implementation**. Add only needed APIs/data detail/interaction/state models. Do not predesign the whole MVP or duplicate baseline decisions into ADR files; ADA/Selected Decisions already record them.

## 16. Troubleshooting

| Symptom | First check |
| --- | --- |
| Wrapper fails / Java mismatch | Compare Java/compiler/Wrapper and JAVA_HOME; align editor runtime. First downloads need network access. |
| Datasource URL missing | Activate `local` and supply settings in the launching terminal; .env configures Compose. |
| Password authentication fails | Check intended user/database/port and private password against original initialization; editing .env does not update an existing volume. |
| Docker/Testcontainers unavailable | Check Linux engine and `docker info`; do not substitute the development database or skip the required IT. |
| Port 8080/5433 occupied | Identify its owner; stop your own earlier instance or consistently change database port/application URL. Do not kill unknown processes. |
| Only 4 tests ran | `test`/`package` intentionally omit the IT; use `verify` and inspect Failsafe. |
| Invalid architecture fixture | Intentional test-only violation; its detector must find it. Production import excludes tests. |
| Root HTTP 404 | Expected until a product endpoint is implemented. |
| Flyway checksum mismatch | Inspect the applied change and use a reviewed forward migration; do not reset/repair blindly. |

Share the nonsecret command, directory and minimal error. Do not publish environment dumps, resolved Compose configuration or sensitive logs.

## 17. Authoritative references

- [Engineering Baseline Setup Guide](../docs/engineering/engineering-baseline-setup-guide.md) — detailed checkpoint learning/troubleshooting.
- [Selected Decisions](../docs/04-architecture/selected-architecture-decisions.md), [Decision Analysis](../docs/04-architecture/architecture-decision-analysis.md) and [ADD](../docs/04-architecture/ADD.md) — architecture selections/rationale/realization.
- [Logical](../docs/04-architecture/views/logical-view.puml), [Implementation](../docs/04-architecture/views/implementation-view.puml), [Deployment](../docs/04-architecture/views/deployment-view.puml) and [Data](../docs/04-architecture/views/data-view.puml) Views — canonical boundaries.
- [Test Strategy](../docs/07-testing/test-strategy.md) and [Definition of Done](../docs/06-scrum/definition-of-done.md) — verification/completion.
- [Scrum Development Workflow](../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) — process authority.

This README describes the current executable foundation; requirements, architecture and workflow retain their own authority.
