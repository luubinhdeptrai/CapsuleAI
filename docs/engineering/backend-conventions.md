# CapsuleAI — Backend Conventions

Rules that apply to all backend code and every sprint implementation guide from Sprint 2 onward. Sprint 1 follows them: rule 1 from the start, rule 2 since migration V6 moved its tables into the `identity` schema. Each rule states what to do, why, and how to tell when it does not apply.

## 1. Controllers group a cohesive API

**Rule.** Put related endpoints in the same controller when they share a **bounded context**, a **resource responsibility** and a **common API base path**. Do not create one controller class per endpoint or per user story.

A controller represents one cohesive API responsibility. The class-level `@RequestMapping` names the base path once; each endpoint is a handler method.

| API group (example)                  | Controller               | Base path (illustrative)     |
| ------------------------------------ | ------------------------ | ---------------------------- |
| Registration and session lifecycle   | `AuthController`         | `/api/v1/auth`               |
| Wardrobe item operations             | `WardrobeItemController` | `/api/v1/wardrobe/items`     |
| Outfit advice operations             | `OutfitAdviceController` | `/api/v1/outfit-advice`      |

Only `AuthController` exists today. The other paths are examples; the real ones come from the OpenAPI contract when those stories are designed.

**Keep the controller thin.** Grouping endpoints does not merge anything below the web adapter:

- Each handler translates HTTP to one command, calls **its own** application service, and translates the result back. No business rules, no transactions, no repository calls in the controller.
- Application services, domain rules and persistence adapters stay separate per use case.
- Who may call a handler is decided by the module's security configuration, not by annotations scattered across controllers.
- Error translation stays in the module's `@RestControllerAdvice`, scoped to its web package.

**Do not over-group.** One module may own several resources. Give each resource its own controller even if they live in the same module and package, e.g. a future profile resource does not join `AuthController` just because both belong to Identity. Split a controller when its handlers no longer share a resource or base path, or when it starts to need services that only some handlers use.

**In implementation guides.** The first story that introduces an API group creates its controller (`NEW`). Later stories in the same group **modify** that controller and add one handler (`MODIFY`, complete resulting file), rather than adding a new class. Name the controller after the resource, not the story.

**Why.** One class per API keeps that surface reviewable in one place and prevents duplicate base paths and request mappings from drifting apart. Thin handlers that delegate to separate services keep the use cases as independent as they were with one class each.

**Reference.** Sprint 1 applies this in [shared architecture §3.2 and TD-27](../06-scrum/sprints/sprint-01/implementation-guides/shared-architecture.md#32-final-package-map).

## 2. Each persistence owner has its own PostgreSQL schema

**Rule.** CapsuleAI has one PostgreSQL database. Inside it, every business module that persists state owns exactly one schema, and only that module's persistence adapter reads or writes it. This is the Product Owner-approved architecture refinement AR-01–AR-08 ([selected architecture decisions §8](../04-architecture/selected-architecture-decisions.md#8-approved-architecture-refinement-ar-01ar-08--context-owned-schemas)), which realizes ADA-016–018.

| Module package | Schema | Table names | State |
| --- | --- | --- | --- |
| `identity` | `identity` | `identity_*` | Implemented (moved there by V6) |
| `personalcontext` | `personal_context` | `personal_context_*` | Not yet persisted |
| `wardrobe` | `wardrobe` | `wardrobe_*` | Not yet persisted (Sprint 2, US-GAR-001) |
| `outfitadviceanduse` | `outfit_advice_and_use` | `outfit_advice_and_use_*` | Not yet persisted |
| `wardrobeintelligence` | `wardrobe_intelligence` | `wardrobe_intelligence_*` | Not yet persisted |
| `productmeasurement` | `product_measurement` | `product_measurement_*` | Not yet persisted |

**Do:**

- Write every table reference schema-qualified, in SQL and in migrations: `FROM identity.identity_session s`. Never rely on `search_path`.
- Keep each SQL statement in one string literal or text block, with lower-case, unquoted identifiers. Never assemble a table name from fragments in production code.
- Join freely inside your own schema; foreign keys inside your own schema are fine (`identity.identity_session → identity.identity_account`).
- Store another module's identifier as a plain value without a foreign key, for example `owner_account_id UUID NOT NULL`. Take it from that module's published contract, such as `@AuthenticationPrincipal AuthenticatedAccount`.
- Get information owned by another module by calling its published `api` in-process. If no contract fits a real use case, the owner adds a narrow one.
- In the first story that persists a module's state, create its schema in that story's migration (`CREATE SCHEMA IF NOT EXISTS wardrobe;`), with only the tables that story needs.
- Keep one owner per migration file: a migration changes exactly one schema.

**Do not:**

- Join, query, insert into, update or delete another module's tables.
- Create a foreign key, view, function, trigger or rule that crosses schemas.
- Import another module's repository, persistence adapter, configuration, domain or application classes; only its `api` package is public.
- Put business tables in Flyway's default schema (`public`), which holds only `flyway_schema_history`.
- Change Flyway's `schemas`, `default-schema` or `table` settings. That would move the schema history and make Flyway re-apply V1–V5 on existing databases.
- Edit an applied migration. Change the schema with a new forward migration.
- Add internal HTTP calls, a message broker or a second database to "isolate" modules.

**Lifecycle across owners.** Without cross-schema foreign keys the database neither prevents nor cascades a stale reference. A future deletion, for example of an account, is an explicit, durable application process: each owner removes or de-identifies its own data through its own services ([selected architecture decisions §8.4](../04-architecture/selected-architecture-decisions.md#84-cross-owner-lifecycle-consistency)).

**Transactions.** A local ACID transaction may span several owners only through their published operations, never by one module writing another's tables. Document the invariant and participants in the story's design, together with what it would mean for a later service extraction.

**Enforcement.** `mvnw clean verify` runs, and CI requires:

- `ArchitectureDependencyTest`: `api`-only dependencies between modules; no private model in an `api`; Spring JDBC only in `adapter.out` and `configuration`; no module cycles; every module package registered.
- `PersistenceOwnershipSqlTest`: production SQL uses only its own schema, always qualified; every migration after V6 changes exactly one owner schema.
- `DatabaseSchemaOwnershipIT`: on the migrated database, tables sit in their owner's schema; no foreign key or view crosses schemas; `public` holds only Flyway's history.

The module list and schema names live in `backend/src/test/java/com/capsuleai/architecture/BusinessModule.java`; change them only together with the architecture documents.

**Limit.** The application connects with one database role that can access every schema. The database does not stop cross-schema SQL at runtime; the tests above do, at build time.

**In implementation guides.** Show every SQL statement and migration with its schema qualification, include the new or changed tables in the story's schema test, and state which owner each migration belongs to.
