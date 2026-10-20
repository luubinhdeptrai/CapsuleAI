# CapsuleAI Backend

The **CapsuleAI backend** is the Java/Spring Boot business core of the CapsuleAI product. It is designed as **one Modular Monolith** with six business owners and pragmatic **Clean Architecture** within each owner. Business modules share a process but not unrestricted access to each other's implementations or persisted data.

> **Current stage:** Pre-Sprint Engineering Baseline, **Checkpoint 3 — Module and Clean Architecture Skeleton**. This README documents the intended foundation. It does **not** claim that PostgreSQL, migrations, feature APIs, automated test suites, CI, or deployment are implemented or verified.
>
> **Source of truth:** The repository's selected architecture decisions, ADD, and architecture views take precedence over this onboarding summary. Detailed API contracts and feature implementations are designed just in time for selected Sprint stories.

## 1. Scope and boundaries

- **In scope now:** one Spring Boot application, one Maven project, project-owned Maven Wrapper, a minimal module ownership skeleton, and reproducible local build/start commands.
- **Out of scope now:** registration/login/JWT, garments, outfits, Wear, coverage, shopping, real provider clients, HTTP endpoints, physical business schemas, and production deployment.
- **Future collaborators:** a separate private Python AI runtime, authorized private object storage, and mobile clients, when their related stories require implementation.
- **Not in the initial architecture:** independent business microservices, a message broker, dedicated cache, gateway, service mesh, or Kubernetes.

## 2. Prerequisites and reference tooling

| Tool | Reference used by the Engineering Baseline guide | Notes |
| --- | --- | --- |
| JDK | Java 21 | Verify both `java --version` and the Java reported by Maven. |
| Spring Boot | 4.1.1 | Confirm the actual version in `pom.xml`; do not assume this README sets it. |
| Maven | 3.9.16 through the Wrapper | Prefer `mvnw.cmd` / `mvnw` to an unrelated global Maven installation. |
| Editor | VS Code with Java/Maven extensions | Development on Windows uses PowerShell. |
| Git | Current Git installation | Work on a reviewed branch, not directly on `main`. |
| Docker Desktop | Linux-container mode | Needed by later PostgreSQL and Testcontainers checkpoints; not needed for this source skeleton. |

The versions above are **guide reference choices**, not proof of the versions currently installed or verified on every developer's machine. The actual `pom.xml` and `.mvn/wrapper/maven-wrapper.properties` govern the Java build and Maven distribution choices.

## 3. Repository and backend layout

Run backend Maven commands from `CapsuleAI/backend`, not from the Git repository root.

```text
CapsuleAI/
├── docs/                          # System-wide documentation (keep outside backend)
├── Initial files/                 # Original workflow/reference material, if present
├── backend/
│   ├── README.md                   # This document
│   ├── pom.xml                     # Maven project definition
│   ├── mvnw, mvnw.cmd              # Maven Wrapper launchers
│   ├── .mvn/wrapper/               # Wrapper configuration
│   └── src/main/java/com/capsuleai/
│       ├── CapsuleAiApplication.java
│       ├── identity/package-info.java
│       ├── personalcontext/package-info.java
│       ├── wardrobe/package-info.java
│       ├── outfitadviceanduse/package-info.java
│       ├── wardrobeintelligence/package-info.java
│       └── productmeasurement/package-info.java
└── .gitignore / .gitattributes     # Shared Git conventions
```

The tree shows **expected files by Checkpoint 3**. Inspect your repository before creating a missing file; it does not imply everything above was independently verified. Later checkpoints add configuration, migrations, tests, Docker Compose at repository root, and a GitHub Actions workflow.

`CapsuleAiApplication` remains in the parent package `com.capsuleai` so normal Spring component scanning can discover future components below it. Each business owner is a **Java package boundary**, not a separate Maven module, JPMS module, Spring Boot executable, or microservice.

## 4. Business modules and ownership

| Module / package | Ownership |
| --- | --- |
| `identity` | Account identity, authentication/session/reset authority, recovery lifecycle. |
| `personalcontext` | User preferences and needs; selected location and contextual/weather meaning. |
| `wardrobe` | Confirmed garments, membership, readiness, analysis and media lifecycle. |
| `outfitadviceanduse` | Advice, shuffle, feedback, Wear/history, and shared outfit-evaluation semantics. |
| `wardrobeintelligence` | Coverage, gaps, hypothetical item utility, and optional shopping support. |
| `productmeasurement` | Independent, supporting measurement evidence and its own lifecycle/retention. |

Each module currently has a root `package-info.java` that **documents** its ownership and collaboration policy. These are not implementations, and Javadoc comments do not enforce Java visibility or architecture constraints. Architectural dependency tests are introduced later.

### Package roles (create only when needed)

```text
<owner>/
├── api/                 # Narrow framework-free published contract for other owners
├── domain/              # Domain models and business rules; no Spring/SQL/HTTP/provider dependencies
├── application/         # Use cases and coordination
│   └── ports/           # Required persistence, neighbor and external outcomes
├── adapter/
│   ├── in/              # Incoming transport/event adapters (e.g., REST controllers)
│   └── out/             # Implementations for owned persistence, local neighbors and providers
└── configuration/       # Outer Spring wiring / dependency injection
```

**This tree is a naming convention, not an instruction to create empty directories or placeholder classes during Checkpoint 3.** Add individual classes, contracts, and roles only when a selected story justifies them.

### Dependency rules

1. **Cross-module calls:** import only another owner's published `api` surface; never import its private `domain`, `application`, `ports`, `adapter`, repository, or persistence entity classes.
2. **Published contracts:** communicate in narrow owner-controlled contract types, not JPA entities, HTTP DTOs from another module, or concrete persistence implementations.
3. **Domain:** business policy must not depend on Spring, database libraries, HTTP, JSON serializers, or provider-specific models.
4. **Application:** orchestrate domain logic through explicit ports and published contracts; avoid reaching into infrastructure implementations.
5. **Adapters/configuration:** implement boundary translation and dependency injection. Module configuration owns its assembly; root bootstrap should not accumulate domain policy or wire another owner's private implementation directly.
6. **One PostgreSQL database later:** logical persistence ownership still applies; shared PostgreSQL does not authorize cross-module access to another owner's tables or repositories.

The published `api/` package is a **local Java module collaboration contract**, **not** an HTTP controller layer. HTTP entry points, when required, are incoming adapters.

## 5. Build and run locally

All commands in this section are from **Windows PowerShell**. Stop if a command fails; do not infer that the next command succeeded.

```powershell
Set-Location 'D:\CapsuleAI\backend'

# Check the project-owned Maven distribution and selected Java runtime.
.\mvnw.cmd --version

# Compile Java sources.
.\mvnw.cmd compile

# Run standard Maven unit-test phase (may report no tests at this stage).
.\mvnw.cmd test

# Compile, run preceding phases and package the application.
.\mvnw.cmd package

# Start through the Spring Boot Maven plugin.
.\mvnw.cmd spring-boot:run
```

The application startup should show the Boot main class and the configured embedded HTTP server. The reference baseline expects port **8080**; confirm from your actual logs/configuration. Since **no endpoints are implemented**, `GET /` returning `404` can be normal.

While the app runs, use **another PowerShell terminal** to verify the listener:

```powershell
Test-NetConnection -ComputerName localhost -Port 8080
Get-NetTCPConnection -LocalPort 8080 -State Listen |
    Select-Object LocalAddress, OwningProcess
```

Ensure the port belongs to your application, not an unrelated process. Stop the app with **Ctrl+C**. To verify the packaged entry point instead (assuming the reference `pom.xml` artifact/version is unchanged):

```powershell
Set-Location 'D:\CapsuleAI\backend'
java -jar .\target\capsuleai-backend-0.0.1-SNAPSHOT.jar
```

Stop it with **Ctrl+C**; do not run two instances on the same port. If your POM's artifact/version differs, inspect `target/` for the actual generated JAR.

### Maven command meanings

| Command | Meaning |
| --- | --- |
| `compile` | Compile production code. |
| `test` | Run discovered fast tests in the standard `test` phase; no tests may exist yet. |
| `package` | Run preceding lifecycle phases and build an executable JAR with the Boot plugin. |
| `verify` | Run later verification phases; **Failsafe integration tests are only included once configured** in a later checkpoint. |
| `spring-boot:run` | Run the development app through a plugin goal, not as a substitute for build verification. |

A successful build **does not prove that meaningful automated tests ran**. Inspect test counts/reports once the testing checkpoints are implemented.

## 6. Configuration, database and tests: planned, not yet implemented

The following are subsequent Engineering Baseline checkpoints. Do not create them ahead of the guide or confuse planned tooling with a working capability.

| Checkpoint | Capability and intended evidence |
| --- | --- |
| 4 | Safe profiles, environment-variable configuration, `.gitignore`, and untracked local `.env`. |
| 5 | Docker Compose PostgreSQL; health, SQL and host connectivity. |
| 6 | JDBC/Flyway migration smoke verification against real local PostgreSQL. |
| 7 | Fast JUnit Jupiter/AssertJ/Mockito test discovery and execution. |
| 8 | Disposable PostgreSQL Testcontainers integration smoke tests via Maven Failsafe. |
| 9 | ArchUnit checks for selected Clean/module dependency rules. |
| 10 | GitHub Actions CI running nonempty required verification suites. |
| 11 | Integrated local/CI engineering baseline review and evidence. |

**Security rule:** do not commit credentials, tokens, or a real `.env`. The later guide will introduce a **nonsecret** `.env.example`. `compose.yaml` will be at the Git repository root, not inside `backend/`, unless a documented repo decision changes that convention.

After Checkpoint 8, follow the guide's `verify` workflow for full configured local Maven test execution; before then, a `verify` command may succeed without running a database integration suite.

## 7. Day-to-day contribution workflow

1. Create or use a scoped working branch (e.g., `chore/engineering-baseline`); avoid committing directly to `main`.
2. Follow the current Engineering Baseline guide checkpoint-by-checkpoint: **understand → implement → verify → inspect diff → commit**.
3. Preserve module ownership and use only published cross-module contracts when feature work begins.
4. Check intended changes before commit:

   ```powershell
   git status --short
   git diff --check
   git add -- backend/README.md backend/src/main/java/com/capsuleai/identity/package-info.java
   git diff --cached --check
   git diff --cached --name-only
   ```

   Add remaining intended `package-info.java` files explicitly; the example stages **only one** of the six. Keep `backend/target/` and secrets out of Git.
5. Commit verified changes and open a Pull Request for review when appropriate. No deployment is part of Checkpoint 3.

The guide suggests this Checkpoint 3 commit message:

```text
chore: document backend module ownership and boundaries
```

## 8. Troubleshooting and learning checks

| Symptom | First check |
| --- | --- |
| Maven Wrapper doesn't start | JDK availability, network access, `.mvn/wrapper/maven-wrapper.properties`. |
| Java release/compiler mismatch | `java --version`, `.\mvnw.cmd --version`, `JAVA_HOME`. |
| Maven compilation fails | The first meaningful compile error, package names and paths. |
| Port 8080 already occupied | Stop a previous application or identify the listener owner; don't kill an unknown process. |
| No packages visible after clone | Ensure the six `package-info.java` files are committed; Git does not track empty folders. |
| `404` at `/` | Expected until an HTTP endpoint is deliberately implemented. |
| Module rule seems unenforced | `package-info.java` documents intent; dependency checks are added in Checkpoint 9. |

Before moving on, you should be able to explain:

- Why six business modules can share **one** Maven project and Boot process.
- How `api/` differs from `application/ports/` and `adapter/in/` HTTP endpoints.
- Why Java package visibility and `package-info.java` do not automatically enforce module boundaries.
- Why extraction to microservices would still require new consistency, failure-handling and authorization design.

## 9. Canonical references

Treat the current repository versions of these files as authoritative over this README:

- [Engineering Baseline Setup Guide](../docs/engineering/engineering-baseline-setup-guide.md)
- [Selected Architecture Decisions](../docs/04-architecture/selected-architecture-decisions.md)
- [Architecture Decision Analysis](../docs/04-architecture/architecture-decision-analysis.md)
- [Architecture Design Document (ADD)](../docs/04-architecture/ADD.md)
- [Implementation View](../docs/04-architecture/views/implementation-view.puml)
- [Logical View](../docs/04-architecture/views/logical-view.puml)
- [Test Strategy](../docs/07-testing/test-strategy.md)
- [Definition of Done](../docs/06-scrum/definition-of-done.md)
- [Scrum Development Workflow](../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md)

If the actual repository has different paths, update links **without moving the source documents into `backend/`**.
