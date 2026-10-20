# CapsuleAI — Manual Engineering Baseline Setup Guide

| Field | Value |
| --- | --- |
| Version / status | 0.1 — Learning Guide, Baseline Draft |
| Inspection date | 2026-10-20 |
| Audience | Backend developer learning to establish and explain the foundation independently |
| Execution status | Instructions only. No application, container, migration, test or CI setup was performed while writing this guide. |
| Process authority | [Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 26–34, 40–43 and 46 |
| Verification authorities | [Test Strategy](../07-testing/test-strategy.md) and [Definition of Done](../06-scrum/definition-of-done.md) |

## Part 1 — Orientation

### Purpose and current position

Build the foundation **yourself**, one checkpoint at a time. Learn what each component does, connect it to the next component, verify the behavior, and commit only work you understand.

CapsuleAI already has requirements, Business Rules, Use Cases/Activities, an ordered backlog, eight initial stories, QA/ASR, selected architecture decisions, ADD, four architecture views and a Test Strategy. These remain living baselines. The next preparation activity is **Pre-Sprint Engineering Baseline**; writing or reading this guide does not complete it.

Relevant sources are [ADA](../04-architecture/architecture-decision-analysis.md), [Selected Decisions](../04-architecture/selected-architecture-decisions.md), [ADD](../04-architecture/ADD.md), the [Logical](../04-architecture/views/logical-view.puml), [Implementation](../04-architecture/views/implementation-view.puml), [Deployment](../04-architecture/views/deployment-view.puml) and [Data](../04-architecture/views/data-view.puml) Views, [QA Analysis](../04-architecture/quality-attribute-analysis.md), [ASR](../04-architecture/ASR.md), [Delivery Decisions](../06-scrum/delivery-decisions.md), [Product Backlog](../06-scrum/product-backlog.md) and [Stories/AC](../06-scrum/stories/README.md). Re-read affected sources when implementing a checkpoint; repository truth overrides this dated tutorial.

### Scope and what you will build

Establish one runnable Java backend, a minimal module ownership skeleton, safe local configuration, a development PostgreSQL service, a migration foundation, basic test execution, PostgreSQL integration smoke verification, initial dependency checks and one CI job.

| Component | Role and interaction |
| --- | --- |
| Maven Wrapper + POM | Select the build distribution, compile Java, resolve compatible dependencies, run tests and package the executable application. |
| Spring Boot application | Start one business-core process. Later modules live inside it; no business endpoints are created here. |
| Compose PostgreSQL | Supply a persistent development database. The host-run backend reaches its published localhost port. |
| Flyway | Apply reviewed migration files through the backend connection and record their execution. |
| Jupiter / AssertJ / Mockito | Run independent fast tests, express expected outcomes and control dependency boundaries. |
| Testcontainers PostgreSQL | Start a separate temporary integration database; Spring receives its connection details and runs the same migrations. |
| ArchUnit | Inspect compiled application dependencies against the selected module/Clean boundaries. |
| GitHub Actions | Run the committed build/verification in a fresh hosted environment and retain inspectable results. |

This is short preparation, not Sprint 0. Leave authentication/JWT implementation, garments, outfits, Wear, Coverage/Multiplier, feature repositories/schemas, API contracts, Sequence/State models, deployment and real provider integrations to selected Sprint work. Preserve the private Python/media boundaries, but defer their skeletons until needed: runtime/model/provider and mobile framework choices are unresolved, and these backend checks do not depend on them. This deferral establishes no claim that the full product or all team disciplines are ready.

### Prerequisites and learning method

Use Windows PowerShell and VS Code. Commands identify their working directory. Create/edit files manually in VS Code using UTF-8; XML, Java, YAML and properties blocks are **file contents**, not terminal commands. Check `$LASTEXITCODE` after native tools and stop when a command fails.

For each checkpoint: **understand → implement manually → verify → explain → review the diff → commit → continue**. First occurrences should be manual. Ask AI to explain a failure or review a small change after you can describe it yourself.

Before committing, inspect `git status --short`, `git diff`, and intended staged paths. Stage explicit files, inspect `git diff --cached --check` and `git diff --cached --name-only`, then commit. Avoid `git add .` when secrets/generated files may be present. Use a working branch and PR; never push directly to `main`. Suggested commit messages are not executed history.

## Part 2 — Repository Audit and Decisions

### Observed state, not inferred readiness

Read-only inspection found the following on 2026-10-20. Repeat Checkpoint 1 because files/tools can change.

| Item | Observed evidence | Still to establish manually |
| --- | --- | --- |
| Repository | `D:\CapsuleAI`, branch `main`, clean initial Git status; 71 existing documentation/reference files | Working branch and small reviewed foundation commits |
| Technical files | No root/backend setup README, `backend`, `mobile`, `ai-service`, POM, Wrapper, application config, Compose/Dockerfile, migrations, tests, `.env.example`, `.gitignore`, scripts or Actions workflow | Scoped files below; reuse any that appear after inspection |
| Java / compiler | Temurin OpenJDK **21.0.12**, `javac 21.0.12`; `JAVA_HOME` and Maven point to that JDK | Repeat version agreement; do not reinstall a working JDK |
| Maven | **3.9.16**, running on Java 21 | Wrapper pinned to that distribution |
| Docker | Client/server **29.4.0**; server reports **linux** | Actual PostgreSQL/Testcontainers startup, not established by `docker info` |
| Compose | **v5.1.2**, command `docker compose` available | Validate and run the project's service |
| Host SQL client | `psql` was not found on the inspected PATH; GUI tooling was not verified | Use an existing client if available; Checkpoint 6 verifies the required host JDBC connection |
| Git / PowerShell | Git **2.45.1.windows.1**; Windows PowerShell **5.1** | GitHub remote/access, Actions availability and review policy |
| VS Code | Java extension pack, language support, debugger, test runner and Maven extension installed | Import the Maven project and align its runtime |

Tool availability is observed; no setup checkpoint, test, architecture conformance or pipeline is claimed passed. Older artifact handoffs are historical; Workflow Section 46 and DD-006 govern current timing.

### Selected constraints versus tutorial recommendations

**Already selected:** Java/Spring Boot Modular Monolith, pragmatic Clean dependencies, published owner contracts, one PostgreSQL authority with logical ownership, private separate Python analysis, authorized private media, no dedicated cache/broker initially (ADA-001–018). Shared storage grants no cross-owner repository/entity access. No microservices, gateway, Kubernetes or distributed coordination is needed here.

**Engineering recommendations, not adopted decisions:** use the reference combination below for coherent examples. The student/Tech Lead should adopt or adjust it before Checkpoint 2 and record choices briefly in the backend README/POM. Routine compatible tooling needs no new ADR. Changes to selected architecture follow the existing analysis/human-selection process.

| Recommendation | Compatibility basis / handling |
| --- | --- |
| Java 21, Maven 3.9.16 | Already installed. Boot 4.1.1 supports Java 21 and Maven ≥3.6.3. Set compiler release 21. |
| Spring Boot **4.1.1**, executable JAR, servlet MVC | Stable version returned by Spring Initializr during inspection; simple blocking JDBC foundation. MVC is a recommendation, not an existing web-stack selection. |
| Boot-managed libraries | Published 4.1.1 BOM: Jupiter **6.0.3**, AssertJ **3.27.7**, Mockito **5.23.0**, Testcontainers **2.0.5**, Flyway **12.4.0**, PostgreSQL JDBC **42.7.13**, Surefire/Failsafe **3.5.6**. Inherit these versions. |
| PostgreSQL **17.11-bookworm** image | Explicit available official 17.x tag; use it consistently for development/integration smoke tests. Version adoption remains engineering work. |
| Flyway + JDBC | Versioned executable migrations and real PostgreSQL smoke verification without selecting an ORM or designing business tables. Use Boot's Flyway starter plus Flyway's PostgreSQL module. |
| ArchUnit **1.5.1**, plain Jupiter tests | Explicit library pin, without an additional JUnit engine or module framework. |
| One Maven project in `backend`, package root `com.capsuleai` | Minimal Implementation View translation; no Maven submodule per context. Keep the six owner names from that view. |
| GitHub-hosted `ubuntu-24.04` | Hosted image includes Docker; the job must still check it and execute container tests. No deployment. |

Compatibility references: [Spring system requirements](https://docs.spring.io/spring-boot/4.1/system-requirements.html), [Boot 4.1.1 BOM](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom), [PostgreSQL image](https://hub.docker.com/_/postgres), [Flyway PostgreSQL support](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database), and [ArchUnit releases](https://github.com/TNG/ArchUnit/releases). These are documentation/artifact checks, not an executed combination. Recheck supported/security updates when implementing; a Boot major change requires revisiting artifact names/imports.

This Jupiter 6 line still uses `org.junit.jupiter` imports. The Test Strategy left the major open; do not force an older BOM to match a tutorial. Testcontainers 2 uses `testcontainers-postgresql` and `org.testcontainers.postgresql.PostgreSQLContainer`; older 1.x examples differ.

### Choices requiring human input at their proper point

- Before Checkpoint 2: confirm versions, package root, MVC and single-project layout. Adapt every affected example consistently if choosing alternatives.
- Before Checkpoints 5–6: confirm the development port/names and Flyway adoption. Development credentials are not production security design.
- Before Checkpoint 10: verify the intended GitHub remote, Actions permission and review/merge policy with its owner. No credentials or remote URL are assumed.
- Defer ORM, feature schemas/transactions, security implementation, AI/mobile toolchains, provider vendors, production roles/secrets and hosting. None is needed to verify this backend foundation.

## Part 3 — Setup Roadmap

| Checkpoint | Objective | Dependencies | Success Evidence |
| --- | --- | --- | --- |
| 1 | Audit repository/tools and choose a branch | Existing repository | Inventory/version agreement; no overwritten work |
| 2 | Start Boot/Maven application; generate Wrapper | 1, adopted recommendations | Compile/package; identified running app and local port |
| 3 | Establish module ownership/source conventions | 2, Logical/Implementation Views | Six-owner map; application still compiles |
| 4 | Establish configuration/secret conventions | 2 | Local profile understood; real `.env` ignored before creation |
| 5 | Run local development PostgreSQL | 4, Docker Linux engine | Healthy container; actual SQL connectivity |
| 6 | Connect Boot and apply minimal migration | 2, 4–5 | Successful V1 in Flyway history; unchanged on restart |
| 7 | Establish fast Jupiter/AssertJ/Mockito tests | 2–3 | Two harness cases execute without Docker |
| 8 | Establish disposable PostgreSQL integration testing | 6–7 | Failsafe IT uses real PostgreSQL and migration |
| 9 | Establish architecture rules | 3, 7 | Nonempty production import; rule detects a deliberate test fixture violation |
| 10 | Run verification in GitHub Actions | 8–9, GitHub access | Nonempty fast/architecture/integration results on PR revision |
| 11 | Reproduce/review integrated foundation | All above | Local/CI evidence and remaining scope/gaps explicit |

Configuration precedes database connection to prevent accidental secret commits. Basic tests precede container/architecture tests. CI follows equivalent local verification.

## Part 4 — Manual Setup Checkpoints

### Checkpoint 1 — Repository and Environment Audit

**Objective**

Establish the actual starting state and a safe working branch before creating anything.

**Why It Matters**

A wrong JDK, unavailable Docker engine or existing setup can invalidate later instructions. Inspection prevents duplicate projects and accidental overwrites (Workflow Sections 30/49; Selected Decision ADA-002).

**Prerequisites**

The existing repository is accessible. No installation is assumed necessary.

**Key Concepts**

`JAVA_HOME` points to a JDK directory; `PATH` decides which executable runs. Maven reports its own Java runtime. Docker CLI availability does not establish daemon/container readiness. A Git branch isolates the student's upcoming work.

**Manual Instructions**

1. Open the existing repository, inspect its status and re-read the workflow/Test Strategy. Keep unrelated work intact.
2. Run the audit below. Compare with Part 2, noting changed/missing components and runtime disagreement.
3. If prerequisites are missing, install only the required supported component using its official instructions. Reopen the terminal after environment changes. Docker Desktop must use Linux containers; resolve WSL/virtualization requirements through [Docker's Windows instructions](https://docs.docker.com/desktop/setup/install/windows-install/).
4. Once existing work is accounted for, create `chore/engineering-baseline`. If that branch already exists, inspect and switch to it instead of recreating it.

**Commands / Configuration**

Run in PowerShell from the existing repository root:

```powershell
Set-Location 'D:\CapsuleAI'
git rev-parse --show-toplevel
git status --short
git branch --show-current
git remote
Get-ChildItem -Force
rg --files --hidden -g '!.git/**'
Get-Command java,javac,mvn,docker,git,code
java --version
javac --version
$env:JAVA_HOME
Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe')
mvn --version
docker --version
docker compose version
docker info --format '{{.ServerVersion}} {{.OSType}}'
git --version
code --list-extensions --show-versions | Select-String 'redhat.java|vscjava'
```

If `rg` is unavailable, use `Get-ChildItem -Recurse -File -Force` and omit `.git` contents from the inventory; installing a search tool is not a prerequisite. List configuration filenames; never dump all environment variables or secret files into shared evidence.

After reviewing the status:

```powershell
git switch -c chore/engineering-baseline
```

**Expected Results**

Java/compiler/Maven agree on Java 21, Docker reports a reachable Linux engine, Compose works, Java editor tools are available, and the inventory matches the files actually present. A new branch exists; no product file was changed by the audit.

**Verification**

Repeat `git branch --show-current` and `git status --short`. Explain any existing technical files before applying later examples. Record tool versions and gaps in brief local notes; redact sensitive information.

**Common Problems**

- Wrong Java: inspect `Get-Command java`, `JAVA_HOME`, `mvn --version`, and VS Code's **Java: Configure Java Runtime**. Align them; do not reinstall blindly.
- Docker client works but `docker info` fails: start Docker Desktop, wait for its engine, inspect `docker context show`, and check Linux-container mode.
- Existing changes: preserve them and identify their owner/purpose before creating overlapping files. Do not reset, clean or stash work blindly.

**Understanding Check**

1. Why can `java --version` and `mvn --version` disagree?
2. What does `docker info` establish, and what does it leave unverified?
3. Which files would make you adapt this tutorial rather than create another backend?

**Git Commit**

No commit is needed for an audit alone. If deliberately adding a nonsecret audit note to the later backend README, include it with the foundation commit.

**Completion Criteria**

- [ ] Repository/work ownership is understood; working branch is correct.
- [ ] JDK/Maven agreement, Docker engine and editor prerequisites are checked.
- [ ] Existing versus missing setup is recorded without exposed secrets.

**Next Checkpoint**

Checkpoint 2 — Spring Boot and Maven foundation.

### Checkpoint 2 — Spring Boot and Maven Foundation

**Objective**

Compile, package and start one empty backend reproducibly, using a project-owned Maven Wrapper.

**Why It Matters**

ADA-002 selects Java/Spring Boot; build versions remain engineering choices. The Wrapper gives another developer and CI the same Maven distribution while the POM describes dependencies/plugins. Neither replaces a suitable JDK.

**Prerequisites**

Checkpoint 1; adopt the reference choices in Part 2. If an existing backend now exists, inspect and extend it instead of generating another project.

**Key Concepts**

A Maven parent provides dependency/plugin management, not every dependency automatically. `dependencies` supply libraries; `build/plugins` perform build tasks. Boot's entry point starts its application context and embedded server. Keep it above module packages for component scanning.

**Manual Instructions**

1. Create `backend/src/main/java/com/capsuleai` and `backend/src/main/resources` in VS Code. Create the small files below manually; do not generate another repository or nested `backend/backend` project.
2. Add the root ignore/line-ending rules before staging generated files. Merge with any existing rules.
3. Generate the Wrapper from inside `backend`, inspect its scripts/properties, then use it instead of global `mvn` for project commands.
4. Compile/package, start the application in the foreground, inspect its log and verify the listening port from a second terminal. Stop it with Ctrl+C before trying the packaged JAR.
5. Add a brief `backend/README.md` recording adopted versions, commands and directory assumptions. Do not copy requirements or architecture documents into it.

**Commands / Configuration**

Create `backend/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath/>
    </parent>
    <groupId>com.capsuleai</groupId>
    <artifactId>capsuleai-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

The parent pins Boot's compatible library/plugin versions; `<java.version>` selects release 21. Test scope keeps test libraries outside the runtime JAR. The Boot plugin repackages the ordinary JAR into an executable one. No JPA, Security, AI/provider or distributed-system dependency is needed yet.

Create `backend/src/main/java/com/capsuleai/CapsuleAiApplication.java`:

```java
package com.capsuleai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CapsuleAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CapsuleAiApplication.class, args);
    }
}
```

`@SpringBootApplication` enables Boot configuration and scanning. `main` starts the application; it contains no domain policy.

Create root `.gitignore` with `backend/target/`. Create root `.gitattributes`:

```gitattributes
* text=auto
/backend/mvnw text eol=lf
/backend/mvnw.cmd text eol=crlf
```

This preserves the Linux wrapper's line endings for CI. From `D:\CapsuleAI\backend`:

```powershell
Set-Location 'D:\CapsuleAI\backend'
mvn org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper '-Dmaven=3.9.16' '-Dtype=only-script'
.\mvnw.cmd --version
.\mvnw.cmd compile
.\mvnw.cmd package
.\mvnw.cmd spring-boot:run
```

The [only-script Wrapper](https://maven.apache.org/wrapper/) generates `mvnw`, `mvnw.cmd` and `.mvn/wrapper/maven-wrapper.properties`; commit these, not downloaded distributions. A first run needs dependency/distribution network access.

| Global command / subsequent Wrapper equivalent | Meaning |
| --- | --- |
| `mvn compile` / `.\mvnw.cmd compile` | Compile main sources. |
| `mvn test` / `.\mvnw.cmd test` | Compile sources/tests and run Surefire tests. No tests exist yet at this checkpoint. |
| `mvn package` / `.\mvnw.cmd package` | Run preceding lifecycle phases, including tests, then package. |
| `mvn verify` / `.\mvnw.cmd verify` | Run through packaging and verification, including Failsafe once configured in Checkpoint 8. |
| `mvn spring-boot:run` / `.\mvnw.cmd spring-boot:run` | Invoke a plugin goal to run the app; not a replacement for verification. |

Maven phases execute earlier phases in order; goals are tasks bound to phases or invoked directly. See the [Maven lifecycle guide](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

**Expected Results**

The Wrapper reports Maven 3.9.16/Java 21; compilation and packaging succeed. Logs identify `CapsuleAiApplication` and its embedded server. No business endpoint exists; a root HTTP 404 is unsurprising. A package success before tests exist is build evidence only.

**Verification**

In a second PowerShell terminal:

```powershell
Test-NetConnection -ComputerName localhost -Port 8080
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object LocalAddress,OwningProcess
```

Correlate the listener with the application log/process. Stop the foreground app with Ctrl+C, then in `backend` verify the packaged entry point:

```powershell
java -jar .\target\capsuleai-backend-0.0.1-SNAPSHOT.jar
```

Confirm startup, then stop it. Do not leave two instances competing for port 8080.

**Common Problems**

- Invalid target release: compare Wrapper Java runtime with `<java.version>`.
- Parent/dependency resolution: inspect the first repository/TLS/proxy error and approved Maven settings; do not disable certificate verification.
- Port in use: identify its owner; stop your earlier foreground app or explicitly choose a free development port, without killing an unknown process.
- `no main manifest attribute`: confirm the Boot plugin and run `package`, then use the executable JAR rather than its `.original` file.

**Understanding Check**

1. What does the Wrapper pin, and what does it leave to your machine?
2. Why does `package` include tests but still not establish an integration test ran?
3. Why omit versions on dependencies managed by the Boot parent?

**Git Commit**

Suggested: `chore: initialize backend build foundation`. Stage the POM, Wrapper files, main source, README, `.gitignore` and `.gitattributes` explicitly; exclude `target`.

**Completion Criteria**

- [ ] Wrapper version/runtime is verified and its scripts/properties are tracked.
- [ ] Source compiles; executable JAR and plugin startup both work.
- [ ] No product feature/dependency was added; build outputs are ignored.

**Next Checkpoint**

Checkpoint 3 — Module and Clean Architecture skeleton.

### Checkpoint 3 — Modular Monolith and Clean Architecture Skeleton

**Objective**

Translate the actual Logical/Implementation Views into understandable source ownership without implementing use cases.

**Why It Matters**

**Modular Monolith** describes boundaries within one application; **Clean Architecture** describes dependency direction inside those modules. Maven submodules and independent deployments are separate decisions. ADA-001/015–018 and the existing views require meaningful boundaries, not extra processes or interfaces for every class.

**Prerequisites**

Checkpoint 2; re-read the [Implementation View](../04-architecture/views/implementation-view.puml), especially its source mapping, public contracts and wiring notes.

**Key Concepts**

| Package role inside an owner | Responsibility / allowed direction |
| --- | --- |
| `api` | Narrow framework-free language published to other owners; no private persistence entities. |
| `domain` | Invariants and policy, independent of Spring, persistence, HTTP and adapters. |
| `application` | Coordinate use cases using domain, needed ports and public contract language. |
| `application.ports` | Describe required persistence/integration/neighbor outcomes. |
| `adapter.in` | Translate incoming HTTP/event/client intentions into application work. |
| `adapter.out` | Implement ports for owned persistence, providers or local public contracts. |
| `configuration` | Outer framework wiring of that module's implementations; root bootstrap delegates assembly. |

Application depends inward; adapters implement needed ports. A caller must not import another owner's private repository, entity or service simply because Java declares it `public`. Root/module configuration legitimately wires components; it does not hold domain policy.

**Manual Instructions**

1. Keep `CapsuleAiApplication` in `com.capsuleai`, above the modules.
2. Create the six owner packages below. For now, track only a useful `package-info.java` per owner describing its responsibility and public-boundary convention.
3. Record the role conventions in the existing backend README. Add role packages/classes only when actual work needs them; Git does not track empty folders.
4. Check for accidental owner renaming, technical-layer modules or copied historical `auth/catalog/recommendation` layouts. The accepted view's names control this example.

**Commands / Configuration**

| Logical owner | Java package below `com.capsuleai` | Scope to preserve |
| --- | --- | --- |
| Identity | `identity` | Account/session/reset authority |
| Personal Context | `personalcontext` | Preferences, needs, selected location/weather context |
| Wardrobe | `wardrobe` | Confirmed garments, readiness, analysis/media lifecycle |
| Outfit Advice and Use | `outfitadviceanduse` | Advice, feedback, Wear/history; published evaluation semantics |
| Wardrobe Intelligence | `wardrobeintelligence` | Coverage/Gaps, hypothetical utility and optional shopping |
| Product Measurement | `productmeasurement` | Supporting minimal measurement with its own lifecycle |

Example file `backend/src/main/java/com/capsuleai/identity/package-info.java`:

```java
/**
 * Owns account, session and recovery authority.
 * Collaborators use the published api; implementation remains private.
 * See docs/04-architecture/views/implementation-view.puml.
 */
package com.capsuleai.identity;
```

Write the equivalent short ownership description for each other package. These are source documentation, not new entities. Future role paths such as `wardrobe/application/ports` are conventions, not a request to create placeholder services/repositories now.

From `backend`:

```powershell
.\mvnw.cmd compile
Get-ChildItem .\src\main\java\com\capsuleai -Recurse -File
```

**Expected Results**

One backend still compiles. Six owner descriptions match the views; no feature class or second Boot application has appeared. Published local contracts preserve a future extraction option, but remote authorization, consistency, failure and data-migration design would still be needed if extraction is justified later.

**Verification**

Compare the owner/role map to the Implementation View and inspect the diff. Confirm there are no cross-owner private imports, speculative shared entities or internal HTTP/broker dependencies. Compilation is not proof of architecture conformance; Checkpoint 9 adds limited automated checks.

**Common Problems**

- Empty folders disappear from Git: expected; track useful ownership metadata instead of `.gitkeep` files for every imagined layer.
- Beans are not found later: inspect package root and configuration rather than moving business logic into bootstrap.
- Shared database mistaken for shared models: preserve logical ownership and collaborate through explicit public contracts.

**Understanding Check**

1. Why does one logical module not require a separate Maven project?
2. What is the difference between a port, adapter and published API?
3. Why is service extraction still work even with clean boundaries?

**Git Commit**

Suggested: `chore: document backend module ownership and boundaries`.

**Completion Criteria**

- [ ] Six owners and role/dependency conventions match the views.
- [ ] Only useful ownership metadata was added; compilation still succeeds.
- [ ] No business use case, persistence model or distributed infrastructure was introduced.

**Next Checkpoint**

Checkpoint 4 — Configuration and secrets before database setup.

### Checkpoint 4 — Configuration and Secret Management

**Objective**

Make local startup reproducible and keep credentials outside committed files before introducing PostgreSQL.

**Why It Matters**

Configuration describes the environment; secrets grant access. DoD privacy/security conditions and ADD Section 9.3 apply to source, logs and evidence. An ignored secret file is safer than removing a password after it has entered Git history.

**Prerequisites**

Checkpoint 2; inspect existing ignore/configuration files before merging these examples.

**Key Concepts**

Boot loads `application.properties` and active-profile variants. Environment variables can override properties. Compose uses `.env` for its own interpolation; Spring Boot does **not** automatically treat that file as application configuration. We will explicitly supply Spring's password through the current process environment in Checkpoint 6. Profiles are configuration choices, not security boundaries. See [Boot external configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) and [profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html).

**Manual Instructions**

1. Extend root `.gitignore` before creating real local credentials.
2. Commit a blank-password `.env.example`. Copy it only if `.env` does not exist, then edit the real `.env` privately in VS Code. Set a local-only password; do not put it in shared notes/screenshots.
3. Create the common/local properties below. Bind this host-run foundation to loopback.
4. Start with the local profile, inspect the active-profile/name/server log, then stop it.

**Commands / Configuration**

Root `.gitignore`, preserving the earlier entry:

```gitignore
backend/target/
.env
.env.*
!.env.example
*.log
```

Root `.env.example` (no real password):

```dotenv
POSTGRES_DB=capsuleai_dev
POSTGRES_USER=capsuleai_dev
POSTGRES_PASSWORD=
POSTGRES_HOST_PORT=5433
```

From the repository root:

```powershell
if (-not (Test-Path -LiteralPath '.\.env')) {
    Copy-Item -LiteralPath '.\.env.example' -Destination '.\.env'
}
git check-ignore -v .env
git ls-files -- .env
git status --short
```

`check-ignore` should identify the rule; `git ls-files -- .env` must return nothing. Ignore rules do not untrack an already committed file. If credentials are already tracked, stop sharing the change, rotate exposed credentials and coordinate remediation; do not rewrite history blindly.

`backend/src/main/resources/application.properties`:

```properties
spring.application.name=capsuleai-backend
server.address=127.0.0.1
server.port=8080
```

`backend/src/main/resources/application-local.properties` initially:

```properties
# Local datasource properties are added in Checkpoint 6.
server.port=8080
```

From `backend`:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

No password belongs in these tracked files. Hostnames/ports/database names are ordinary configuration; a password is sensitive. Keep `.env` in the repository root for the Compose commands used below. Shell variables override `.env` interpolation; stale `POSTGRES_*` variables can therefore change Compose behavior.

**Expected Results**

The app log identifies profile `local`, app/server configuration is as intended, and `.env` is ignored/untracked. No database connection is expected yet. Stop the app before continuing.

**Verification**

Verify localhost port 8080 as in Checkpoint 2. Inspect only intended staged paths and confirm that the real `.env` is absent. Never publish `docker compose config`, environment dumps or debug logs containing resolved credentials.

**Common Problems**

- Profile ignored: quote the complete `-Dspring-boot.run.profiles=local` argument in PowerShell and run from `backend`.
- `.env.example` hidden: keep the exception after `.env.*`.
- Password with `$`, `#` or spaces: follow [Compose interpolation/quoting rules](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/); a simple generated alphanumeric local password avoids this tutorial's parsing distractions.

**Understanding Check**

1. Why does `.env` not configure Spring automatically?
2. Why is an ignore rule insufficient for a file already tracked?
3. What is safe to commit in an example file?

**Git Commit**

Suggested: `chore: establish local configuration and secret conventions`. Stage `.gitignore`, `.env.example` and the properties files explicitly.

**Completion Criteria**

- [ ] `.env` is ignored and untracked; example/config files contain no real secrets.
- [ ] Local-profile startup and loopback binding are observed.
- [ ] Compose interpolation and Spring environment configuration are understood separately.

**Next Checkpoint**

Checkpoint 5 — PostgreSQL with Docker Compose.

### Checkpoint 5 — PostgreSQL and Docker Compose

**Objective**

Run one local PostgreSQL service with persistent development data and verified connectivity.

**Why It Matters**

ADA-003 selects PostgreSQL. Compose describes repeatable service setup; a named volume preserves data when a container is replaced. This local service is distinct from the disposable test database introduced later.

**Prerequisites**

Checkpoint 4; Docker's Linux engine is running, `.env` contains a local password, and host port 5433 is available or deliberately changed.

**Key Concepts**

An image is the packaged software; a container is a running instance; a volume holds data outside that instance. Services share a Compose network and can resolve service names. Port publishing connects the Windows host to the service. A healthcheck establishes readiness, not that the application's credentials work.

**Manual Instructions**

1. Create root `compose.yaml` below, preserving any existing services if setup has since appeared.
2. Validate without printing resolved credentials; start only PostgreSQL.
3. Inspect health/port, execute SQL and verify the host path. Use an existing SQL client when available; do not install another merely to duplicate a working tool.
4. Keep this database for local development. Do not use its credentials/volume as an integration-test fixture or production configuration.

**Commands / Configuration**

```yaml
name: capsuleai-dev
services:
  postgres:
    image: postgres:17.11-bookworm
    environment:
      POSTGRES_DB: ${POSTGRES_DB:?Set POSTGRES_DB in .env}
      POSTGRES_USER: ${POSTGRES_USER:?Set POSTGRES_USER in .env}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?Set a local password in .env}
    ports:
      - "127.0.0.1:${POSTGRES_HOST_PORT:-5433}:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}"]
      interval: 5s
      timeout: 3s
      retries: 10
volumes:
  pgdata:
```

Required-variable expressions reject empty configuration; `$$` defers healthcheck expansion to the container. Health timings are local harness settings, not new product quality targets. The [official PostgreSQL image](https://hub.docker.com/_/postgres) uses `/var/lib/postgresql/data` for 17.x; PostgreSQL 18+ changes its recommended layout, so do not switch majors without reviewing volume/upgrade instructions. Initial user/database/password variables initialize an **empty** data directory; changing `.env` does not alter credentials in an existing volume.

From the repository root:

```powershell
Get-NetTCPConnection -LocalPort 5433 -State Listen -ErrorAction SilentlyContinue
docker compose --env-file .env config --quiet
docker compose --env-file .env up -d --wait --wait-timeout 60 postgres
docker compose ps
docker compose port postgres 5432
docker compose exec postgres pg_isready -U capsuleai_dev -d capsuleai_dev
docker compose exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT current_database(), current_user, version();'
Test-NetConnection -ComputerName localhost -Port 5433
```

Use the names/port actually chosen in `.env` if they differ. For an existing host `psql` client, verify TCP authentication without placing the password on the command line:

```powershell
psql -h localhost -p 5433 -U capsuleai_dev -d capsuleai_dev -W -c 'SELECT 1;'
```

Type the password at its prompt. Otherwise connect with an existing GUI client using the same host/port/database/user; if no client exists, retain the container SQL/host TCP evidence and complete real host JDBC authentication in Checkpoint 6. A successful TCP probe alone is not a successful database login.

| Backend execution location | PostgreSQL address | Reason |
| --- | --- | --- |
| Windows host, this guide | `jdbc:postgresql://localhost:5433/capsuleai_dev` | Use the published host port. |
| Future backend container on this Compose network | `jdbc:postgresql://postgres:5432/capsuleai_dev` | `postgres` is the service DNS name; `localhost` would mean the backend container itself. |

The second row explains networking; it does not ask you to containerize the backend. A future container's server binding also needs its own explicit configuration; host loopback defaults are intentional here.

**Expected Results**

Compose reports a healthy PostgreSQL container, SQL identifies the intended database/user/version, and the host port is reachable. The local database initially contains no CapsuleAI business tables. Local initialization credentials are development-only, not a selected production privilege model.

**Verification**

Stop/start your service without deleting its volume, then repeat readiness/SQL checks:

```powershell
docker compose stop postgres
docker compose start postgres
docker compose exec postgres pg_isready -U capsuleai_dev -d capsuleai_dev
```

If startup is still pending, wait for the healthcheck; inspect targeted logs locally. Actual migration persistence is checked next.

**Common Problems**

- Port conflict: inspect the listener and choose a free `POSTGRES_HOST_PORT`; update the backend URL consistently.
- Authentication fails after editing `.env`: existing volume credentials did not change. Diagnose the original initialization; do not delete the volume blindly.
- `postgres` cannot resolve from Windows: use localhost/published port, not Compose DNS.
- Health remains unhealthy: inspect `docker compose logs --tail 50 postgres` locally and redact credentials before sharing.

**Understanding Check**

1. Which data survives container replacement, and why?
2. Why are host port 5433 and service port 5432 different?
3. Why does successful `pg_isready` not prove JDBC authentication?

**Git Commit**

Suggested: `chore: add local PostgreSQL environment`. Commit `compose.yaml` and nonsecret README changes, never `.env` or database contents.

**Completion Criteria**

- [ ] Compose validates; database becomes healthy; real SQL succeeds.
- [ ] Host/container addresses and volume lifecycle are understood.
- [ ] Host authentication is verified now or explicitly pending mandatory Checkpoint 6 JDBC verification.

**Next Checkpoint**

Checkpoint 6 — JDBC and migration foundation.

### Checkpoint 6 — Database Migration Foundation

**Objective**

Connect the host-run backend to PostgreSQL and establish versioned migration execution without business tables.

**Why It Matters**

Migration files make database changes reviewable and reproducible. Flyway records applied versions/checksums, preventing silent drift. The Test Strategy Section 8 and DoD schema-change criterion require real migration/integrity evidence. The Data View is logical ownership guidance, not permission to build all feature tables now.

**Prerequisites**

Checkpoints 2/4/5 and deliberate Flyway adoption. The local database is healthy; real password/names match its initialization.

**Key Concepts**

`V1__foundation.sql` means version 1, two underscores, readable description. Flyway applies pending versions in order and records checksums in `flyway_schema_history`. Do not edit a migration already applied to shared environments; add a later migration. This is versioned migration execution, not Flyway's separate baseline/repair operation.

**Manual Instructions**

1. Add the dependencies below inside the existing POM's `<dependencies>`; keep Boot's versions. The Flyway starter already brings JDBC infrastructure, so do not add an ORM/JPA dependency.
2. Add common Flyway settings and local datasource properties. Require the password instead of supplying a committed/default password.
3. Create one technical V1 migration containing a harmless SQL statement. Flyway's history is the only new persistent structure; no Account/Garment/Outfit/Wear table is needed.
4. Start with the local profile and an explicitly supplied password, inspect the migration result, then restart and confirm it is not applied twice.

**Commands / Configuration**

Add these POM dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Boot 4 uses the Flyway starter for its auto-configuration; Flyway's [PostgreSQL module](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database) supplies database-specific support, and the JDBC driver carries connections. This does not require the Flyway Maven plugin. See [Boot database initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html).

Append to common `application.properties`:

```properties
spring.flyway.locations=classpath:db/migration
spring.flyway.clean-disabled=true
```

Append to `application-local.properties`:

```properties
spring.datasource.url=${CAPSULE_DB_URL:jdbc:postgresql://localhost:5433/capsuleai_dev}
spring.datasource.username=${CAPSULE_DB_USER:capsuleai_dev}
spring.datasource.password=${CAPSULE_DB_PASSWORD}
```

Create `backend/src/main/resources/db/migration/V1__foundation.sql`:

```sql
-- Verify versioned migration execution; no product schema is defined here.
SELECT 1;
```

From `backend`, enter the **same** local password stored privately in root `.env`:

```powershell
$dbPassword = Read-Host 'Local database password' -AsSecureString
$env:CAPSULE_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbPassword).Password
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

For changed names/port, set nonsecret `CAPSULE_DB_URL`/`CAPSULE_DB_USER` in that terminal. Do not print the password variable. The conversion supplies the plaintext value required by the local child process; it is not a production secrets solution. After stopping the application and completing restart verification, clear it:

```powershell
$env:CAPSULE_DB_PASSWORD = $null
$dbPassword = $null
```

After adding JDBC, ordinary startup needs an actual datasource: use this local-profile command or, for tests later, Testcontainers connection details. Do not fix missing database configuration by adding H2 or disabling migrations.

**Expected Results**

Boot opens a real PostgreSQL connection and Flyway applies V1 successfully. On the next startup, V1 is already applied with no duplicate history row. No business schema is designed; default migration-history placement does not establish future context schemas or production roles.

**Verification**

From the repository root in a second terminal:

```powershell
docker compose exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;'
```

Confirm one successful version `1` row, stop/start Boot with the same explicit environment and repeat. Observe successful application startup and port 8080. This provides the host JDBC authentication evidence left pending if no host SQL client existed in Checkpoint 5.

**Common Problems**

- Unresolved `CAPSULE_DB_PASSWORD`: set it in the terminal launching Maven; another terminal does not inherit later changes.
- Unsupported database/missing driver: inspect the PostgreSQL support module, runtime driver and resolved Boot-managed versions before changing the database.
- Checksum mismatch: identify the modified applied migration. Do not run repair, enable baseline-on-migrate or delete data simply to hide it; preserve shared history and create a proper forward change.
- Wrong connection: check profile, host/port/database and environment overrides without dumping credentials.

**Understanding Check**

1. Why does V1 run once rather than every application startup?
2. What is the consequence of changing an applied shared migration?
3. Why is this foundation not a complete physical data design?

**Git Commit**

Suggested: `chore: establish JDBC and migration foundation`. Commit POM, properties, migration and nonsecret README guidance.

**Completion Criteria**

- [ ] Host-run Boot authenticates to the intended PostgreSQL instance.
- [ ] V1 is recorded successful and not reapplied on restart.
- [ ] No business tables, ORM selection or destructive reset was introduced.

**Next Checkpoint**

Checkpoint 7 — Fast automated tests.

### Checkpoint 7 — Basic Automated Testing Infrastructure

**Objective**

Execute two small test-only examples demonstrating Jupiter, readable assertions and fake/mock boundaries without Spring or a database.

**Why It Matters**

Test Strategy Sections 4–7 place fast independent tests underneath expensive integration tests. Boot's test starter supplies the managed testing libraries, but test discovery and real execution must still be verified.

**Prerequisites**

Checkpoint 2; use the established module conventions. No product feature or running PostgreSQL is required.

**Key Concepts**

**Arrange** inputs/dependencies; **Act** on the behavior; **Assert** the result. Jupiter's `@Test` identifies a test method. AssertJ explains expected outcomes fluently. A fake provides controlled behavior; Mockito can stub a port and verify a significant interaction. No `@SpringBootTest` is needed to instantiate ordinary Java objects.

**Manual Instructions**

1. Create the test below under `src/test/java`, not main sources. Its helper types are intentionally test-only teaching fixtures; do not promote them into a product module.
2. Configure Surefire to reject an empty suite. Boot's parent manages its plugin version.
3. Run the focused test, then all fast tests. Read the test count and reports.
4. Temporarily make one expected value wrong, observe a meaningful assertion/build failure, then restore the intended assertion and rerun. This checks the harness, not any product rule.

**Commands / Configuration**

Create `backend/src/test/java/com/capsuleai/foundation/HarnessSmokeTest.java`:

```java
package com.capsuleai.foundation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HarnessSmokeTest {
    interface LabelPort { String load(); }

    record LabelReader(LabelPort port) {
        String read() { return "CapsuleAI:" + port.load(); }
    }

    @Test
    void readsThroughAFakePort() {
        LabelReader reader = new LabelReader(() -> "local"); // Arrange
        String result = reader.read();                      // Act
        assertThat(result).isEqualTo("CapsuleAI:local");     // Assert
    }

    @Test
    void readsThroughAMockedPort() {
        LabelPort port = mock(LabelPort.class);
        when(port.load()).thenReturn("ci");
        LabelReader reader = new LabelReader(port);

        assertThat(reader.read()).isEqualTo("CapsuleAI:ci");
        verify(port).load();
    }
}
```

The fake is a lambda implementing the small port. `when` chooses the mock response; `verify` checks the port was actually used. In real application tests, replace genuine external ports, not all domain objects. These temporary harness examples prove tooling only; future tests must exercise real requirements rather than accumulate trivial framework/arithmetic checks.

Add alongside the existing Boot plugin inside `<build><plugins>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <failIfNoTests>true</failIfNoTests>
    </configuration>
</plugin>
```

From `backend`:

```powershell
.\mvnw.cmd '-Dtest=HarnessSmokeTest' test
.\mvnw.cmd test
Get-Content .\target\surefire-reports\*.txt
```

Surefire discovers conventional names such as `*Test`/`*Tests`. Keep Jupiter imports; do not import JUnit 4's `org.junit.Test`. `target/surefire-reports` contains text/XML evidence. See [Surefire discovery](https://maven.apache.org/surefire/maven-surefire-plugin/examples/inclusion-exclusion.html) and [Boot testing dependencies](https://docs.spring.io/spring-boot/reference/testing/test-scope-dependencies.html).

**Expected Results**

Exactly two harness tests execute and pass when restored. No Spring application context or database starts. Docker is not needed for this suite.

**Verification**

Require a nonzero test count, zero failures/errors and an observed failure during the temporary wrong-expectation exercise. Run the restored full suite before committing. Merely seeing BUILD SUCCESS is insufficient if the expected tests were not discovered.

**Common Problems**

- Zero tests: inspect `src/test/java`, filename, imports and Surefire discovery/configuration; do not disable `failIfNoTests`.
- IDE succeeds but Maven fails: Maven is the shared baseline; check runtime, imported POM and actual discovered classes.
- Mockito agent warning on Java 21: distinguish a warning from a test failure. If the JVM disallows attachment, follow the selected [Mockito version's agent instructions](https://javadoc.io/doc/org.mockito/mockito-core/5.23.0/org.mockito/org/mockito/Mockito.html); do not randomly downgrade dependencies or suppress failures.
- Database connection attempted: an unintended Spring-context test was added. Keep fast tests ordinary Java; move genuine startup/persistence checks to the integration suite.

**Understanding Check**

1. Which lines are Arrange, Act and Assert?
2. What does a mock prove here that a real PostgreSQL test must prove elsewhere?
3. Why should these tests work with PostgreSQL stopped?

**Git Commit**

Suggested: `test: establish fast automated testing infrastructure`.

**Completion Criteria**

- [ ] Two tests are discovered with inspectable reports.
- [ ] A deliberate assertion error makes the command fail; restored expectations pass.
- [ ] No Spring/database/Docker dependency or production teaching feature was introduced.

**Next Checkpoint**

Checkpoint 8 — Disposable PostgreSQL integration testing.

### Checkpoint 8 — PostgreSQL Integration Testing with Testcontainers

**Objective**

Run an integration smoke test against disposable real PostgreSQL, with Spring's actual JDBC/Flyway configuration.

**Why It Matters**

PostgreSQL SQL/constraints/transactions can differ from H2. A substitute passing its own semantics cannot establish selected-database compatibility. Test Strategy Sections 8/18 require isolated real dependency evidence and visible failure when required infrastructure is missing.

**Prerequisites**

Checkpoints 6–7; Docker Desktop's Linux engine is reachable. The selected versions use Testcontainers 2.x. First execution may download images/dependencies.

**Key Concepts**

`@Testcontainers` integrates container lifecycle with Jupiter; a static `@Container` runs once for the test class and is stopped afterward. `@ServiceConnection` supplies Boot JDBC/Flyway connection details using the container's actual mapped address. This database does not use Compose's volume, port or `.env`. Fakes/unit tests remain cheaper and do not start containers.

**Manual Instructions**

1. Add test-scoped dependencies and Failsafe configuration below.
2. Create the IT with the 2.x container import. Do not add a fixed exposed host port, enable reusable persistent test containers or skip tests when Docker is absent.
3. Stop the development PostgreSQL service, leaving Docker itself running. This helps demonstrate that the IT uses its own database.
4. Run `verify` and inspect both test reports; rerun to establish repeatable clean setup.

**Commands / Configuration**

Add inside POM `<dependencies>` (versions inherited):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

Add inside `<build><plugins>`, keeping Surefire:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <configuration>
        <failIfNoTests>true</failIfNoTests>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

`*Test` belongs to Surefire's fast phase; `*IT` belongs to Failsafe. Invoke **verify**, not just `integration-test`: Failsafe records execution results and its verify goal enforces them after cleanup. See [Failsafe usage](https://maven.apache.org/surefire/maven-failsafe-plugin/usage.html).

Create `backend/src/test/java/com/capsuleai/foundation/DatabaseSmokeIT.java`:

```java
package com.capsuleai.foundation;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DatabaseSmokeIT {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17.11-bookworm")
                    .withDatabaseName("capsuleai_test");

    @Autowired JdbcTemplate jdbc;
    @Autowired Flyway flyway;

    @Test
    void connectsAndAppliesFoundationMigration() {
        assertThat(jdbc.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE version = '1' AND success",
                Long.class)).isEqualTo(1L);
        assertThat(flyway.info().pending()).isEmpty();
    }
}
```

The test profile avoids `application-local.properties`; no separate test properties file is needed. `NONE` starts the actual Spring context without an HTTP server. Assertions check real SQL and the actual V1 history; no product table/repository is introduced. Container-local synthetic credentials/ports are provided by the library, not copied from a real environment. See [Boot service connections](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html) and the [Testcontainers PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/).

From the repository root, then `backend`:

```powershell
docker compose stop postgres
Set-Location .\backend
$env:SPRING_PROFILES_ACTIVE = $null
$env:CAPSULE_DB_PASSWORD = $null
docker info --format '{{.ServerVersion}} {{.OSType}}'
.\mvnw.cmd '-Dit.test=DatabaseSmokeIT' verify
Get-Content .\target\surefire-reports\*.txt
Get-Content .\target\failsafe-reports\*.txt
.\mvnw.cmd verify
```

**Expected Results**

The two fast harness tests and one database IT execute. The IT starts its own PostgreSQL container, applies the real V1 and succeeds while the Compose service is stopped. Required Docker failure produces a failed build, not a skipped green check.

**Verification**

Inspect Failsafe XML/text results for one non-skipped IT with zero failures/errors, plus startup/migration evidence. Repeat the run. A class-scoped container isolates this smoke test; later test methods still need independent fixtures/cleanup if sharing a container. Future transaction/race cases need real commits/connections under Test Strategy Section 8, not rollback alone.

**Common Problems**

- Docker cannot be found: inspect Desktop readiness, Linux mode, active Docker context and compatible daemon/API; do not point tests at the development database.
- Image pull fails: check registry connectivity/proxy and the pinned tag. Do not disable container cleanup or assertions to continue.
- Old `PostgreSQLContainer<?>` example fails: use the 2.x artifact/import/class shown here consistently.
- Migration not found: confirm `src/main/resources/db/migration`, V1 naming and Flyway starter/module; inspect the actual test profile/connection details.
- Only fast tests execute: confirm `DatabaseSmokeIT`, Failsafe's two goals and the `verify` phase. `test` alone intentionally excludes ITs.

**Understanding Check**

1. How does Boot find the temporary database without a committed test URL/password?
2. Why can this test succeed while Compose PostgreSQL is stopped?
3. Why is `mvn test` insufficient for full baseline verification?

**Git Commit**

Suggested: `test: establish PostgreSQL integration smoke verification`.

**Completion Criteria**

- [ ] Nonempty fast and integration reports show the expected checks actually ran.
- [ ] Disposable PostgreSQL connectivity and real migration execution are established.
- [ ] No H2 substitution, persistent test volume, feature repository or silent Docker skip exists.

**Next Checkpoint**

Checkpoint 9 — Architecture dependency checks.

### Checkpoint 9 — Architecture Testing with ArchUnit

**Objective**

Run small source-dependency checks and prove the rule detects a forbidden dependency, without creating product classes merely to populate packages.

**Why It Matters**

Compilation permits imports that violate modular ownership. The Implementation View and ADA-015–018 require inward dependencies, public contracts and private owner models. Automated checks catch some violations earlier than review; they do not establish runtime SQL ownership, authorization or behavior.

**Prerequisites**

Checkpoints 3/7; re-read the actual view's wiring/boundary rules. Use plain Jupiter with the ArchUnit library to avoid an extra test-engine dependency.

**Key Concepts**

ArchUnit imports compiled class dependencies, then evaluates rules. Test fixtures must be excluded from production checks. Empty domain/application packages cannot prove a dependency boundary works; verify a nonempty root import and a deliberate test-only violation separately. `allowEmptyShould(true)` below accommodates intentionally absent feature packages, not an unknown/wrong package root.

**Manual Instructions**

1. Add the library dependency and the two test-source files below.
2. Run the architecture test. It checks actual production imports and separately expects a known fixture violation; the invalid fixture is never a production component.
3. Explain which packages are still empty. As real classes arrive, add expected-package presence checks and strengthen rules for application/API dependencies and allowed collaboration directions from the view. Do not create classes just to satisfy a count.
4. When a genuine production dependency violates a rule, correct the dependency or review a justified source-supported exception; do not broadly allowlist private imports.

**Commands / Configuration**

Add to POM dependencies:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit</artifactId>
    <version>1.5.1</version>
    <scope>test</scope>
</dependency>
```

Create `backend/src/test/java/com/capsuleai/architecturefixture/domain/InvalidDomainFixture.java`:

```java
package com.capsuleai.architecturefixture.domain;

import org.springframework.context.ApplicationContext;

// Deliberately invalid TEST fixture for the dependency rule, never production code.
public final class InvalidDomainFixture {
    ApplicationContext forbiddenDependency;
}
```

Create `backend/src/test/java/com/capsuleai/architecture/ArchitectureDependencyTest.java`:

```java
package com.capsuleai.architecture;

import com.capsuleai.architecturefixture.domain.InvalidDomainFixture;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureDependencyTest {
    private static ArchRule domainRule() {
        return noClasses().that().resideInAPackage("com.capsuleai..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "org.hibernate..",
                        "java.sql..", "javax.sql..", "java.net.http..",
                        "com.fasterxml..", "tools.jackson..",
                        "com.capsuleai..application..", "com.capsuleai..adapter..")
                .allowEmptyShould(true);
    }

    @Test
    void productionDependenciesRespectInitialBoundaries() {
        JavaClasses production = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.capsuleai");
        assertThat(production).extracting(JavaClass::getName)
                .contains("com.capsuleai.CapsuleAiApplication");
        domainRule().check(production);

        for (String owner : new String[] { "identity", "personalcontext", "wardrobe",
                "outfitadviceanduse", "wardrobeintelligence", "productmeasurement" }) {
            String base = "com.capsuleai." + owner;
            noClasses().that().resideOutsideOfPackage(base + "..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            base + ".domain..", base + ".application..", base + ".adapter..")
                    .allowEmptyShould(true).check(production);
        }
    }

    @Test
    void domainRuleDetectsTheDeliberateTestFixtureViolation() {
        JavaClasses fixture = new ClassFileImporter().importClasses(InvalidDomainFixture.class);
        assertThat(domainRule().evaluate(fixture).hasViolation()).isTrue();
    }
}
```

The owner rule blocks another owner's private application/ports/domain/adapters, including repositories/entities kept there. It permits each owner to wire its own internals and root bootstrap to reference module configuration. It is an initial subset, not a full public-API exposure/cycle proof. Keep future persistence models inside their documented private roles and extend checks when real structure exists. The domain rule excludes framework/adapter dependencies without banning legitimate outer configuration.

From `backend`:

```powershell
.\mvnw.cmd '-Dtest=ArchitectureDependencyTest' test
.\mvnw.cmd test
Get-Content .\target\surefire-reports\*.txt
```

The [ArchUnit user guide](https://www.archunit.org/userguide/html/000_Index.html) explains import filtering, package patterns and empty rule selections. Here `..` matches package segments; adjust the root consistently if the team adopted another package name.

**Expected Results**

Two architecture tests execute; together with Checkpoint 7, the fast suite now has four tests. Production import includes the actual entry point. The test fixture produces a rule violation as expected, so the detector check succeeds. Initial empty feature roles remain explicitly unproven, not falsely reported as implemented conformance.

**Verification**

Read the detector's failure report while learning, for example by inspecting `evaluate(fixture).getFailureReport()` in the debugger. Calling `check(fixture)` instead would throw an assertion failure; the test deliberately evaluates and asserts that violation. Do not add the invalid fixture to main sources. Confirm production import excludes tests and includes the intended root.

**Common Problems**

- Empty import: inspect compiled output, package root and import filter; do not globally disable empty-selection safeguards.
- Fixture contaminates production checks: confirm its test-source location and `DO_NOT_INCLUDE_TESTS`.
- Legitimate wiring fails: compare the rule to the view's root/module configuration exception before changing code or rule.
- Wrong module names: use the six actual view names rather than the workflow's historical illustrative packages.

**Understanding Check**

1. What does a passing rule over an empty domain package prove?
2. Why is the invalid fixture test green when it detects a violation?
3. Which ownership/security properties cannot be established by static dependency tests?

**Git Commit**

Suggested: `test: add initial architecture dependency checks`.

**Completion Criteria**

- [ ] Both architecture tests execute; actual application import is nonempty.
- [ ] Detector finds the deliberate test-only violation.
- [ ] Empty roles, supported wiring and limitations are explicit; no placeholder product class was created.

**Next Checkpoint**

Checkpoint 10 — GitHub Actions verification.

### Checkpoint 10 — GitHub Actions CI Baseline

**Objective**

Run the committed build, fast tests, architecture checks and disposable PostgreSQL IT on the intended GitHub repository's PR revision.

**Why It Matters**

Local success can depend on an IDE, stale output or a developer database. CI repeats the shared verification command in a fresh environment. Test Strategy Section 18 and DoD Sections 4–6 require actual configured checks/evidence, not a green badge with no discovered tests.

**Prerequisites**

Checkpoints 8–9 pass locally; the owner has confirmed the intended GitHub remote, Actions availability and review policy. Never add deployment credentials or change a remote blindly.

**Key Concepts**

Checkout obtains source; Java setup selects the runtime; Maven caching saves dependency downloads, not test results. A hosted Linux job can use Docker/Testcontainers without a separate Compose database service. Workflow files live at repository root even though Maven runs in `backend`.

**Manual Instructions**

1. Manually create `.github/workflows/backend-ci.yml` below at the repository root.
2. Review official action versions/runner requirements before adopting them. The shown references were available during authoring; follow any repository policy requiring reviewed immutable commit-SHA pins.
3. Commit the workflow, then push **the working branch**, not `main`, to the verified remote and open a PR through GitHub.
4. Inspect checkout/runtime/build/test-count/report steps. Deliberately fail and restore one test on the learning branch to observe a red then corrected run; never merge the deliberate failure.

**Commands / Configuration**

```yaml
name: Backend verification
on:
  push:
    branches: [main, chore/engineering-baseline]
  pull_request:
    branches: [main]
permissions:
  contents: read
jobs:
  verify:
    runs-on: ubuntu-24.04
    timeout-minutes: 15
    defaults:
      run:
        working-directory: backend
        shell: pwsh
    steps:
      - uses: actions/checkout@v7
        with:
          persist-credentials: false
      - uses: actions/setup-java@v6
        with:
          distribution: temurin
          java-version: '21'
          cache: maven
          cache-dependency-path: backend/pom.xml
      - name: Verify Docker availability
        run: docker info --format '{{.ServerVersion}} {{.OSType}}'
      - name: Build and run required verification
        run: bash ./mvnw --batch-mode --no-transfer-progress clean verify
      - name: Require expected test reports
        run: |
          $required = @{
            'surefire-reports/TEST-com.capsuleai.foundation.HarnessSmokeTest.xml' = 2
            'surefire-reports/TEST-com.capsuleai.architecture.ArchitectureDependencyTest.xml' = 2
            'failsafe-reports/TEST-com.capsuleai.foundation.DatabaseSmokeIT.xml' = 1
          }
          foreach ($entry in $required.GetEnumerator()) {
            $path = Join-Path 'target' $entry.Key
            if (-not (Test-Path -LiteralPath $path)) { throw "Missing report: $path" }
            [xml]$report = Get-Content -LiteralPath $path -Raw -Encoding UTF8
            $suite = $report.testsuite
            if ([int]$suite.tests -lt $entry.Value -or
                [int]$suite.failures -ne 0 -or [int]$suite.errors -ne 0 -or
                [int]$suite.skipped -ne 0) {
              throw "Required checks missing, failed or skipped: $path"
            }
            Write-Host "$($suite.name): $($suite.tests) tests, none failed/skipped"
          }
      - name: Retain test reports
        if: always()
        uses: actions/upload-artifact@v7
        with:
          name: backend-test-reports
          path: |
            backend/target/surefire-reports/
            backend/target/failsafe-reports/
          if-no-files-found: error
```

`run` blocks use PowerShell even on Linux. Linux launches the Unix Wrapper through `bash`; the Windows equivalent is:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

The report-check statements are also valid in local PowerShell from `backend`. Required counts describe these particular foundation examples, not a product coverage quota. Adapt filenames/counts deliberately when replacing the teaching harness with real verification; do not delete the discovery guard to make a missing suite green.

The 15-minute job timeout bounds a stuck CI job, not a product performance requirement. `working-directory` applies to `run`; action cache/artifact paths remain relative to repository root. Do not add a GitHub PostgreSQL service or start development Compose: the IT supplies its own database. Use ordinary `pull_request`, not a privileged trigger for untrusted PR code.

References: official [checkout](https://github.com/actions/checkout), [setup-java](https://github.com/actions/setup-java), [upload-artifact](https://github.com/actions/upload-artifact) and [Ubuntu 24.04 runner inventory](https://github.com/actions/runner-images/blob/main/images/ubuntu/Ubuntu2404-Readme.md). The inventory lists Docker/PowerShell; the job's preflight and actual IT establish availability for the execution. Self-hosted runners need their own verified runtime compatibility.

After reviewing staged files and committing, from the repository root, only if `origin` is the intended repository:

```powershell
git push -u origin chore/engineering-baseline
```

**Expected Results**

A workflow appears for the branch/PR, uses Java 21 and the Wrapper, runs four fast/architecture tests plus one database IT, and retains reports. No deployment or product implementation occurs. No CI success is assumed before inspecting the actual run.

**Verification**

Confirm the run belongs to the intended commit/PR revision. Inspect nonzero report counts, zero failures/skips, container/migration execution and the deliberately failing learning run. A failed build remains failed even if report upload succeeds. Arrange required merge checks through the repository's existing review policy; a job existing alone does not enforce branch protection.

**Common Problems**

- Workflow absent: inspect root `.github/workflows`, branch/PR triggers, Actions permissions and whether the workflow was pushed.
- Wrapper permission/line endings: `bash ./mvnw` avoids executable-bit dependence; ensure its LF rule is committed. Windows local commands use `.cmd`.
- Docker unavailable: inspect the chosen runner's actual engine; never turn the required IT into a silent skip.
- Green build but missing checks: inspect report paths/discovery, Surefire/Failsafe and flags. Do not use `skipTests`, `skipITs`, failure-ignore or automatic rerun-to-green as a gate.
- Reports expose private contents later: use privacy-safe fixtures and protected evidence; do not upload secrets/debug environment dumps.

**Understanding Check**

1. Why does caching dependencies not establish tests passed?
2. Why does this job not need the development Compose database?
3. What catches an accidentally deleted architecture test class?

**Git Commit**

Suggested: `ci: establish backend verification workflow`. Use the working branch and review process.

**Completion Criteria**

- [ ] Intended PR revision has an inspected successful build with all expected suites executed.
- [ ] Missing/skipped/failing required checks prevent success; failure evidence is inspectable.
- [ ] No deployment, broad write permissions or committed real secret was introduced.

**Next Checkpoint**

Checkpoint 11 — Integrated baseline verification and handoff.

### Checkpoint 11 — Final Engineering Baseline Verification

**Objective**

Reproduce the whole backend foundation, retain evidence and identify remaining gaps before moving to refinement/Planning.

**Why It Matters**

Files being present means configured; observed reproducible behavior means verified for the stated scope. Workflow Section 30 does not mark Enabler PBIs Done automatically, and the DoD requires inspectable evidence rather than isolated setup tasks.

**Prerequisites**

All earlier checkpoints completed by the student, including inspected CI results. Do not mark this checkpoint complete because this guide exists.

**Key Concepts**

A clean build removes stale generated output and reruns required suites. A temporary test database must work independently of the persistent development service. Local and CI evidence must identify the tested revision/environment; foundation checks do not prove product requirements, full architecture conformance or SRS performance/ML targets.

**Manual Instructions**

1. Stop any foreground application; stop only your development PostgreSQL service. Leave Docker running.
2. Run clean verification from `backend` and inspect all three expected report files using Checkpoint 10's report-check block.
3. Restore development PostgreSQL and start Boot with the local profile/password as in Checkpoint 6. Verify startup, listener, real JDBC connection and existing migration history. Stop Boot and clear the password variable afterward.
4. Review source/module layout, staged files and secret exclusions. Reproduce from a fresh checkout/worktree or CI rather than depending only on the current IDE's state; do not overwrite the existing repository or create another project inside it.
5. Add concise commands, version choices, actual results/links and limitations to the backend README/PR. Use Part 5's checklist and normal review process.

**Commands / Configuration**

From the repository root:

```powershell
docker compose stop postgres
Set-Location .\backend
$env:SPRING_PROFILES_ACTIVE = $null
$env:CAPSULE_DB_PASSWORD = $null
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
Get-Content .\target\surefire-reports\*.txt
Get-Content .\target\failsafe-reports\*.txt
```

Expected classes are `HarnessSmokeTest` (2 cases), `ArchitectureDependencyTest` (2) and `DatabaseSmokeIT` (1), all non-skipped. Inspect a current successful CI run for the same intended change; a previous unrelated green revision is not evidence.

Restore development PostgreSQL from the root:

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env up -d --wait --wait-timeout 60 postgres
git status --short
git check-ignore -v .env
git ls-files -- .env
git diff --check
git diff --cached --check
git diff --cached --name-only
```

Repeat Checkpoint 6's local-profile/password startup and SQL history check. Review intended tracked text files for hard-coded credentials or tokens privately; a filename/regex scan alone does not establish secret absence. Keep real `.env`, build output and private logs out of commits.

**Expected Results**

Both clean automated verification and configured local startup work, the development database retains V1, and the required CI checks execute for the intended revision. Evidence and limitations are recorded. Future product code, schemas, providers and team-specific toolchains remain unimplemented.

**Verification**

Complete every applicable Part 5 item using observed results. Record a failed/unexecuted item honestly and fix it before claiming baseline readiness. Peer review should cover module alignment, configuration, migration/test isolation and actual CI discovery, not just file presence.

**Common Problems**

- Old reports appear convincing: run `clean verify`; check timestamps/revision and actual class counts.
- IDE-only success: reproduce through the committed Wrapper and inspect CI's runtime/dependencies.
- Local test accidentally uses development PostgreSQL: keep it stopped, verify the service connection and remove local profile/URL overrides from tests.
- Foundation verification mistaken for feature completion: link PBI/Story evidence through normal scope/DoD review; this exercise selects no Sprint work or completed story.

**Understanding Check**

1. What evidence distinguishes configured from verified for each major component?
2. Which product/architecture properties remain unverified after these smoke checks?
3. Why must Sprint Planning precede story-specific detailed design and implementation?

**Git Commit**

If recording the verified setup/evidence: `docs: record verified backend foundation and setup commands`. Do not commit invented success results or secrets.

**Completion Criteria**

- [ ] Integrated local and CI evidence is reproducible and tied to the intended change.
- [ ] Part 5's applicable readiness items are complete; unresolved gaps are explicit.
- [ ] No product feature, Sprint scope, architecture selection or backlog order changed.

**Next Checkpoint**

No further setup checkpoint. Use normal review/handoff, then Backlog Refinement and Sprint Planning when authorized. The student performs those future activities; this guide does not.

## Part 5 — Engineering Baseline Readiness Checklist

Leave these unchecked until **you** have the evidence. Retain a concise version/runtime summary, exact verification command, tested commit, relevant reports/log excerpts, CI run/PR link and known limitations in existing README/PR evidence. Do not create an evidence document per command or commit screenshots containing secrets.

- [ ] Java/compiler/Wrapper/IDE runtime agreement verified; chosen versions documented.
- [ ] One backend builds from committed sources using the Wrapper.
- [ ] Plugin and packaged-JAR startup verified; local profile/name/listener understood.
- [ ] Module ownership matches the six actual view owners; no speculative feature classes/shared entities.
- [ ] Local `.env` ignored/untracked; committed configuration/example files contain no real secrets.
- [ ] Compose service healthy; actual PostgreSQL SQL connection established; host JDBC authentication verified.
- [ ] V1 executes once in development, persists across restart and executes freshly in the temporary test database.
- [ ] Jupiter/AssertJ fake and Mockito cases actually run; no Spring/Docker required for fast checks.
- [ ] Testcontainers starts/uses disposable PostgreSQL with the real migration, independently of development Compose.
- [ ] Initial ArchUnit checks run against a nonempty production import and detect the invalid test fixture.
- [ ] Empty feature roles and untested architecture/runtime properties remain explicit.
- [ ] Clean verification discovers all expected suites with no missing/skipped required checks.
- [ ] Current PR/CI revision runs build, fast, architecture and integration verification; reports/failures are inspectable.
- [ ] Review includes secret hygiene and no feature/schema/API/Sprint-scope expansion.
- [ ] Documented commands can be followed by another developer; remaining AI/mobile/provider/production choices are assigned to their proper later work.

Foundation readiness enables responsible refinement/Planning; it is neither a Done product Increment nor proof of business/security/performance acceptance. The [DoD](../06-scrum/definition-of-done.md) remains the completion authority. Relevant product tests, real integrations, device evidence and source-defined QA obligations grow with delivered Stories. Nothing here postpones their applicable constraints to a final hardening Sprint.

## Part 6 — Focused Troubleshooting

When verification fails: **stop → read the first useful error → identify the failing layer → inspect relevant configuration → apply a targeted correction → rerun the affected check → rerun required broader verification**. Do not continue just because files were created.

| Failing component | Inspect / targeted diagnosis |
| --- | --- |
| Java / compiler / editor | Compare `java --version`, `javac --version`, `.\mvnw.cmd --version`, `JAVA_HOME`, command locations and VS Code runtime. Reopen the terminal after configuration changes. |
| Maven resolution | Read the first artifact/TLS/proxy error. Run `.\mvnw.cmd dependency:tree` to inspect actual resolved dependencies; use `.\mvnw.cmd help:effective-pom` locally for parent/plugin configuration. Do not share unreviewed diagnostics/settings or bypass TLS. |
| Docker daemon / Testcontainers | `docker context show`, `docker info`, Linux engine readiness, supported API/runtime and image pull access. Use current compatible libraries rather than forcing an old Docker API or disabling cleanup. |
| Compose / port / credential | Run root `docker compose --env-file .env config --quiet`, `docker compose ps`, `docker compose port postgres 5432`; inspect the actual listener and initial volume credentials. Localhost/service DNS differ. |
| Boot startup | Identify whether the first cause is profile/placeholder, database authentication, missing driver/module, migration or port binding. Check the active profile and explicit environment; avoid a full environment dump. |
| Flyway | Inspect the applied history, filenames/checksums and actual target database. Applied shared migrations need forward changes. Repair/baseline switches are not ordinary remedies for an unexplained mismatch. |
| Test discovery | Verify source path, class suffix, Jupiter imports, Surefire/Failsafe goals and requested lifecycle phase. Inspect XML counts; never remove discovery guards to get green. |
| Architecture rule | Check actual package names, compiled output/import filters, intentional empty roles and wiring allowance. Reproduce a violation with the test-only fixture before blaming production code. |
| CI | Start with the first failed step and tested revision; compare runner/JDK/Wrapper, paths, Docker preflight and reports with local verification. Required failures remain failures even if artifacts upload. |

For shared diagnosis, provide the exact nonsecret command, tool versions, working directory, expected/actual outcome and minimal redacted error chain. Never publish actual password variables, `.env`, credential-bearing URLs, full Docker container inspection or unreviewed Maven debug output.

**Destructive troubleshooting boundary:** removing a database volume erases its data; a migration clean/reset can destroy schema contents. Neither is a routine repair in this guide. Before any intentional disposable-development reset, identify the exact environment/volume, understand what will be lost and preserve needed data. Never apply a reset to shared/unknown resources. No destructive command is required to complete these checkpoints.

## Part 7 — Learning Review

You should now be able to explain how source, dependencies, runtime configuration, a durable development database, repeatable migrations, isolated tests, architecture rules and CI form one engineering foundation.

1. Trace `clean verify`: which phases/plugins run fast tests, package the application, execute ITs and enforce their results?
2. Explain why there is one business-core process and six logical owners without six Maven projects.
3. Trace a future use case through application ports, adapters and public neighbor contracts; identify prohibited private imports and legitimate configuration wiring.
4. Explain which data survives a Compose restart versus an IT container's lifecycle.
5. Explain why `.env`, Spring profiles and process environment variables are separate mechanisms.
6. Distinguish a fast fake/mock test, real PostgreSQL IT, static dependency check and selective user journey.
7. Explain why an empty rule selection, skipped IT or zero-test green build is insufficient evidence.
8. Identify what must change when a future Story adds a schema/API/security boundary, and what can remain unchanged.

Do not seek a perfect setup before beginning delivery. Keep a small reproducible foundation, then evolve useful tests/contracts/designs with selected work. Reuse the [Test Strategy](../07-testing/test-strategy.md), stable source IDs and existing PR/Story evidence rather than multiplying documentation.

## Part 8 — Next Step

**Conditional handoff, not a claim of completion:** once the student has established and verified the Engineering Baseline, follow the canonical workflow:

```text
Engineering Baseline completed and verified
        ↓
Backlog Refinement
        ↓
Sprint Planning
        ↓
Selected-story Just-In-Time / Just-Enough Detailed Design
        ↓
Automated Test + Implementation
        ↓
Refactor / Integration Verification / PR / CI / Review
        ↓
Done Increment under Story AC + project DoD
```

Refinement clarifies feasibility/testability; Planning selects work before full detailed design. No Story/PBI is selected by this guide, and no Sprint Goal, implementation commitment or baseline approval is invented. ADA/Selected Decisions and ADD/views retain architecture authority; separate baseline ADRs and whole-MVP API/Data/Sequence/State design are not prerequisites.

**Begin with Checkpoint 1 — Repository and Environment Audit.**
