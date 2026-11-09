# Sprint 2 Backend Implementation Guides

| Field | Value |
| --- | --- |
| Sprint | Sprint 2 — Recover Access and Establish a Private Wardrobe |
| Stories | US-AUTH-005, US-GAR-001, US-WAR-001, US-WAR-002 |
| Repository baseline | **Stage 0**: branch `sprint_02` at `90d3237` (which contains the schema-ownership correction `17f32de`) plus the Product Owner decision updates of 2026-11-07 to the requirement documents (§3) |
| Audience | Backend developers implementing the Sprint 2 stories by hand, plus reviewers and the Scrum Team |
| Package | This README, [shared-architecture.md](shared-architecture.md) and four story guides |
| Status | Proposed implementation documentation for Stages 1–4, revised on 2026-11-07 to incorporate every Product Owner decision of that day (§17). Stage 0 is real, committed code. Nothing of Stages 1–4 is in `backend/` yet (⏳ BACKEND IMPLEMENTATION PENDING). |

---

## 1. Purpose of this package

These guides let a developer implement the four selected Sprint 2 stories from the current repository without reconstructing requirements, architecture or tests. Each story guide is self-contained. It contains:

- The original applicable requirements, quoted verbatim with their source, section and link.
- The engineering interpretation, kept separate from those quotations.
- The just-in-time technical design.
- Every file to add or modify, in order, with complete code and exact BEFORE/AFTER anchors.
- Complete automated tests, commands, manual checks, the AC and requirement-to-test matrices, and the checklists.

They follow the guide standard in the Sprint plan ([sprint-plan.md §14.2](../sprint-plan.md#142-standard-for-subsequent-self-contained-implementation-guides)) and the structure of the [Sprint 1 guides](../../sprint-01/implementation-guides/README.md).

## 2. Verification status — read this first

| Statement | True? |
| --- | --- |
| The Product Owner decisions of 2026-11-07 are incorporated into these guides and the requirement documents | **Yes** (§17, [decision implementation notes](../decision-implementation-notes.md)). |
| The proposed code in the story guides was compiled and its automated tests executed | **Yes, by the guide author on 2026-11-07 in an isolated scratch clone of this repository**, never in this working tree. Each guide's code is taken mechanically from that clone's stage commits; nothing was retyped. Earlier test counts were not reused: every stage was rebuilt and re-run after the decisions were incorporated. |
| Stage 0 passes | Yes, in the scratch clone on the commit that equals `sprint_02` at `90d3237` plus the 2026-11-07 documentation updates: **161 unit and 124 integration tests** (0 failures, errors or skips), and the CI guard found all 27 required reports. |
| The full four-stage result passed `mvnw clean verify` | Yes, in the scratch clone: **461 unit tests and 258 integration tests**, with 0 failures, 0 errors and 0 skipped, on PostgreSQL 17.11 and Mailpit via Testcontainers. |
| Each intermediate stage compiles and passes on its own | Yes, in the scratch clone: every final stage commit passed `clean verify` and its own CI guard ([§13](#13-expected-outcome-of-each-stage)). |
| The CI report guard passes | Yes, in the scratch clone: all 69 required reports after Stage 4 are present, with none failed or skipped. |
| Applying each guide literally reproduces the tested code | Yes: each guide's [ADD] files and [MODIFY] BEFORE→AFTER changes, applied to its starting stage, give exactly the next stage commit. Every BEFORE block occurs exactly once. This was checked mechanically for all four guides. |
| The OpenAPI file of every stage is structurally valid | Yes: it parses as YAML, every `$ref` resolves, its version is the stage's version, and no stage contains a merge-conflict marker. `IdentityOpenApiContractIT` compares it with the handlers at every stage. |
| The Mermaid and PlantUML diagrams were rendered | **No.** Their syntax was reviewed by hand only. |
| The timing evidence proves constant time | **No.** `PasswordResetTimingIT` shows that, under normal load, both paths take at least the minimum and that their medians differ by less than half of it. That is evidence, not a constant-time guarantee (OPEN-S2-05). |
| The code has been applied to this repository | **No.** |
| Hosted GitHub Actions CI has run it | **No.** The proposed `sprint_02` pull-request trigger exists only in the US-AUTH-005 guide until applied. |
| Any Sprint 2 story is Done | **No.** Done requires the Definition of Done evidence on the integrated revision, including client evidence (accessibility, client states) and the external evidence marked ⚠️ in §17. |
| It has been deployed | **No.** Recovery delivery was exercised against Mailpit, not against a production email provider; no HTTPS link domain exists yet. |

Your own build is the only evidence for your revision. Run the commands in each guide and read the reports.

## 3. Repository baseline

**Stage 0** is the state every guide starts from: branch `sprint_02` at commit `90d3237`. It contains the architecture-conformance change for the Product Owner-approved refinement AR-01–AR-08 ([selected architecture decisions §8](../../../../04-architecture/selected-architecture-decisions.md#8-approved-architecture-refinement-ar-01ar-08--context-owned-schemas)), committed as `17f32de`. That change:

- Adds `V6__move_identity_tables_into_identity_schema.sql`, which moves the Sprint 1 tables, with all their data, into the `identity` schema.
- Schema-qualifies all Identity SQL.
- Adds the architecture checks `PersistenceOwnershipSqlTest`, `DatabaseSchemaOwnershipIT`, `IdentitySchemaRelocationIT` and a stronger `ArchitectureDependencyTest`.
- Adds the persistence rule to [backend conventions §2](../../../../engineering/backend-conventions.md#2-each-persistence-owner-has-its-own-postgresql-schema).

`90d3237` itself only corrected diagrams in two guides; this revision adopts those diagrams (with one correction, see [US-AUTH-005 guide §10.9](US-AUTH-005-guide.md#109-sequence-diagrams)).

The verbatim quotations in this package come from the working tree **with** the Product Owner decision updates of 2026-11-07 (PRD, SRS, Business Rules, UC-004/007/008, stories, sprint plan, architecture decisions, test strategy and the garment-property authority). Those updates change requirement text only, no code. Commit them, together with this package, before starting US-AUTH-005.

The Sprint plan's own baseline assessment ([§3](../sprint-plan.md#3-current-repository-and-sprint-1-baseline-assessment)) was written at commit `529ec99`, before the Sprint 1 fixes (`aa243a8`), CORS (`ab9afb9`) and this correction. Where the two differ, this table reflects Stage 0.

| Area | Baseline state |
| --- | --- |
| Build | Java 21, Spring Boot 4.1.1, Maven Wrapper, single module `backend` |
| Identity | Sprint 1 complete in code: registration, login, JWT access token plus persistent session authority, refresh rotation with reuse detection, logout; CORS configuration (`CorsProperties`, `corsConfigurationSource`) |
| Wardrobe | Only `package-info.java`: no garment code, table or API |
| Other modules | `personalcontext`, `wardrobeintelligence`, `outfitadviceanduse`, `productmeasurement`: documentation boundaries only |
| Migrations | V1–V5 (foundation and Identity) and V6 (moves the Identity tables into the `identity` schema). **Never edit them.** Sprint 2 adds V7 (Identity, password reset) and V8 (Wardrobe, which creates the `wardrobe` schema). |
| Database layout | One PostgreSQL database. Schema `identity` holds `identity_account`, `identity_session` and `identity_refresh_token`. Flyway's default schema `public` holds only `flyway_schema_history`. No other schema exists yet. |
| API contract | `docs/05-api/openapi.yaml` v0.4.1, Identity only. The Sprint 2 additions (0.5.0–0.8.0) are specified in the story guides and are applied with them; they are deliberately **not** in the executable baseline yet |
| Tests | JUnit 6, AssertJ, Mockito, Testcontainers 2 (PostgreSQL), ArchUnit. `IdentityOpenApiContractIT` checks every `/api/` handler against OpenAPI. `ArchitectureDependencyTest`, `PersistenceOwnershipSqlTest` and `DatabaseSchemaOwnershipIT` enforce module and schema ownership. `IsolatedMigrationDatabase` gives upgrade tests their own database. |
| CI | `.github/workflows/backend-ci.yml`: `clean verify` plus a guard that requires named surefire/failsafe reports |
| Garment authority | [Initial files/Image to Properties Extraction.md](../../../../../Initial%20files/Image%20to%20Properties%20Extraction.md), approved 2026-11-05 (13 properties, every vocabulary); 2026-11-07 decisions applied (multiple Styles, Brand and Price rules, "Tanks & Camis") |

## 4. Sprint Goal and selected stories

**Source:** [sprint-plan.md — 2. Sprint Goal](../sprint-plan.md#2-sprint-goal) · lines 28–34 at Stage 0

> ## 2. Sprint Goal
>
> > Enable secure recovery of access to a trustworthy private wardrobe by delivering email password reset, confirmed manual garment creation, current-wardrobe browsing and garment inspection through verified backend APIs.
>
> The Goal connects restored private participation with the first useful **Understand My Wardrobe** capability in the [Product Goal](../../../product-goal.md), Sections 3–5. Recovery preserves access to the same account; manual confirmation establishes trustworthy possessions; browsing/detail make that information inspectable.
>
> **[REQ]** Backend success means correct-account recovery and authorized creation/read outcomes, truthful failure/unevaluated-eligibility states, preserved accepted data and inspectable automated integration evidence. Full mobile journeys and whole-story Done require the additional evidence identified in Sections 6/11. All four selected stories contribute to this Goal; none is optional, deferred or a stretch item.

**Source:** [sprint-plan.md — 5. Selected Sprint 2 Backlog](../sprint-plan.md#5-selected-sprint-2-backlog) · lines 96–107 at Stage 0

> ## 5. Selected Sprint 2 Backlog
>
> Implementation priority below is the recommended sequence within the selected scope, not a change to the Product Backlog's global order.
>
> | Priority | Story / exact title | Parent PBI | User value / backend objective | Expected deliverables | Dependencies | Verification / backend acceptance |
> | --- | --- | --- | --- | --- | --- | --- |
> | 1 | [US-AUTH-005 — Recover account access through email](../../../stories/auth/US-AUTH-005-recover-account-access.md) | PBI-005 | Restore correct-account access through completed email recovery, not merely instruction delivery. | Account-safe initiation, real delivery boundary, reset validity/replacement/consumption, accepted password change and account-wide session revocation; affected API/migrations/tests. | Existing account/login/session authority from PBI-004; Email Delivery Service and usable instruction-opening contract. | AC-01–AC-16 mapping below; atomic reset effects, old authority denied, new-password login, actual delivery evidence and no disclosure. |
> | 2 | [US-GAR-001 — Add an owned garment manually](../../../stories/garment/US-GAR-001-add-garment-manually.md) | PBI-006 | Establish one confirmed owned garment without image/AI dependency. | Eight-category confirmed minimum with optional valid subtype/Brand/purchase Price, owner-associated persistence, authority/provenance and explanation that eligibility has not been evaluated, durable safe retry, protected API/migrations/tests. | PBI-004 usable access; Wardrobe ownership and private-data protections. Recovery completion is not a technical prerequisite. | AC-01–AC-14; accepted minimum-only entry survives reread, no unconfirmed/duplicate effect, bounded values and owner isolation. |
> | 3 | [US-WAR-001 — Browse my current wardrobe](../../../stories/wardrobe/US-WAR-001-browse-current-wardrobe.md) | PBI-007 | Recognize current possessions and distinguish genuine emptiness from unavailable access/data. | Authorized read-only collection, identifiable imageless/minimum-only summaries, current-ownership filtering, truthful empty/failure contract and tests. | PBI-004. Manual creation provides useful populated fixtures, not a hard gate to empty browsing. | AC-01–AC-11; only the current account's confirmed active entries; successful empty result distinct from failure; no read side effects. |
> | 4 | [US-WAR-002 — Inspect an owned garment](../../../stories/wardrobe/US-WAR-002-inspect-owned-garment.md) | PBI-007 | Understand the accepted profile, eligibility under a future approved policy and limits of use evidence. | Owner-scoped detail, available accepted information/provenance, conditional readiness, explicit unavailable use evidence, changed-current-state/failure semantics and tests. | PBI-004 and an actual current owned garment. Browse/add are useful routes; Wear/analytics are not prerequisites. | AC-01–AC-12; correct current garment, retained imageless/incomplete detail, no fabricated usage/readiness or read mutation. |
>
> **[REQ]** Keep account recovery one coherent request-to-restored-access story and keep browsing/detail as distinct outcomes (DD-003). Applicable privacy is included as work within these four rows; it is not a fifth backlog item.

## 5. Implementation order and guide links

**Source:** [sprint-plan.md — 8. Dependencies and Recommended Implementation Sequence](../sprint-plan.md#8-dependencies-and-recommended-implementation-sequence) · lines 252–267 at Stage 0

> ## 8. Dependencies and Recommended Implementation Sequence
>
> Retain the requested order: **US-AUTH-005 → US-GAR-001 → US-WAR-001 → US-WAR-002**.
>
> | Step | Implementation focus | Actual dependency / reuse |
> | --- | --- | --- |
> | Initial bounded checks, with the first story | Reproduce the relevant baseline behavior later, reconcile the contract/CI/security findings in Section 3 and establish recovery delivery/opening feasibility. | Supporting work for affected integration, not a new preparation phase or a whole-Sprint design gate. No claim that all issues are already fixed. |
> | 1 — Recovery | Story-specific JIT design, automated tests and implementation of initiation/delivery/valid completion/revocation. | Existing account/login/session/password capabilities; no garment prerequisite. Verify revocation with the existing security probe initially, then real wardrobe operations as they arrive. |
> | 2 — Manual addition | Establish confirmed garment ownership, all 13 applicable properties and durable safe acceptance. | Existing usable access and Wardrobe module boundary; recovery is an independent capability, not a technical gate. |
> | 3 — Browsing | Reuse accepted Wardrobe state and accepted properties for owner-scoped current summaries. | Authentication is hard input. Empty collection behavior can be developed independently; manual creation supplies real populated cross-story evidence. |
> | 4 — Detail | Reuse the same accepted model and stable garment identification. | A current owned garment is the actual input. Browse provides selection/return; a particular creation method and future Wear/analytics do not gate detail. |
> | Continuous integration | Connect accepted creation → list → detail; reset → old authority denied → new login → same wardrobe retained. | Required combined evidence; testing/review begin with each story, not only at this final cross-story check. |
>
> Each story uses **Selected Story → JIT Detailed Design → Automated Tests + Implementation → Refactor → Integration Verification → PR / CI / Review**. Finish only the necessary detail for the current story before dependent code; do not first design all four APIs/schemas/sequences.
>
> An email dependency problem does not create a technical dependency blocking independent Wardrobe work. It remains a visible recovery risk with an owner; all four selected stories remain in scope. This plan creates neither replacement work nor a reduced-scope fallback.

| Order | Guide | Starts from | Leaves | New API | Migration | OpenAPI |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | [US-AUTH-005 — Recover account access through email](US-AUTH-005-guide.md) | Stage 0 | Stage 1 | `POST /api/v1/auth/password-reset/request` (202), `/check` (204), `/complete` (204); 429 when limited | V7 | 0.5.0 |
| 2 | [US-GAR-001 — Add an owned garment manually](US-GAR-001-guide.md) | Stage 1 | Stage 2 | `POST /api/v1/wardrobe/garments` (201/200) | V8 | 0.6.0 |
| 3 | [US-WAR-001 — Browse my current wardrobe](US-WAR-001-guide.md) | Stage 2 | Stage 3 | `GET /api/v1/wardrobe/garments?limit=&cursor=` (cursor pages) | — | 0.7.0 |
| 4 | [US-WAR-002 — Inspect an owned garment](US-WAR-002-guide.md) | Stage 3 | Stage 4 | `GET /api/v1/wardrobe/garments/{garmentId}` | — | 0.8.0 |

A guide's [MODIFY] anchors and BEFORE blocks refer to the state its "Starts from" column names. Apply the guides in this order. If you change the order (for example, Wardrobe before recovery), shared files such as `openapi.yaml`, `backend-ci.yml`, `backend/README.md` and `IdentitySecurityConfiguration` will have different BEFORE text. [shared-architecture.md §21](shared-architecture.md#21-shared-file-ownership-matrix) lists every such file.

## 6. Story dependency graph

```mermaid
flowchart LR
    s1[Sprint 1<br/>US-AUTH-001..004<br/>accounts, login, sessions] --> a5[US-AUTH-005<br/>email recovery<br/>V7, schema identity]
    s1 --> g1[US-GAR-001<br/>manual garment<br/>V8, schema wardrobe]
    g1 --> w1[US-WAR-001<br/>browse current wardrobe]
    g1 --> w2[US-WAR-002<br/>inspect owned garment]
    w1 -. selection / return .-> w2
    a5 -. cross-story evidence:<br/>reset revokes wardrobe access .-> x[WardrobeAccessAfterPasswordResetIT]
    w2 --> x
    classDef story fill:#eef,stroke:#447
    class a5,g1,w1,w2 story
```

Solid arrows are technical dependencies (code or schema reused). Dotted arrows are integration evidence. Recovery is **not** a technical prerequisite of the Wardrobe stories (Sprint plan §8); the order exists so each guide's BEFORE text is exact.

## 7. How to read a story guide

| Section | Content |
| --- | --- |
| Header, 1–2 | Story overview; the baseline the guide starts from |
| 3–8 | **Verbatim** BRD, PRD, SRS, Business Rules, the complete story with every AC, use cases/activity diagrams and architecture decisions, each with a source line (`Source: [file](link) — section · lines … at Stage 0`) |
| 9 | Engineering interpretation: responsibilities, what each AC means for the backend, contradictions and their treatment |
| 10 | Just-in-time design: architecture, model, API, data, transactions, concurrency, security, sequences, decisions with alternatives |
| 11 | File change matrix (every file, action, purpose, step) |
| 12 | Production steps `P1…Pn`. Each step gives action, path, purpose, requirement refs, dependencies, explanation, and either the complete file ([ADD]) or numbered BEFORE/AFTER changes with a unique anchor ([MODIFY]) |
| 13 | Test steps `T1…Tn` with complete test code, then commands |
| 14–18 | Manual verification, AC and requirement-to-test matrices, Definition of Done, troubleshooting, checklists and known limitations |

Labels: **[REQ]** requirement, **[ARCH]** selected architecture decision, **[TECH]** proposed technical decision (traceable to a `TD-S2-nn` entry in [shared-architecture.md §22](shared-architecture.md#22-technical-decisions-product-owner-decisions-and-contradictions)), **[OPEN]** unresolved (`OPEN-S2-nn`; all were decided on 2026-11-07 and keep their ids). **[ADD]**, **[MODIFY]** and **[REMOVE]** mark file actions. No step in this package removes a file.

Decision status markers: ✅ **APPROVED AND INCORPORATED INTO GUIDES** · ⏳ **BACKEND IMPLEMENTATION PENDING** · ⚠️ **EXTERNAL VERIFICATION PENDING** · 🟡 **DEFERRED BY APPROVED DECISION**.

## 8. Architecture summary

- **Modular monolith, pragmatic clean architecture** ([ARCH] ADA-015): `domain` and `application` are framework-free, `ports` are interfaces, `adapter.in.web` and `adapter.out.*` hold Spring code, and `configuration` wires beans. `ArchitectureDependencyTest` enforces this.
- **Identity** owns accounts, credentials, sessions and recovery. **Wardrobe** owns garments. Wardrobe sees Identity only through `identity.api.AuthenticatedAccount` (ADA-018) and stores the owner id without a foreign key into Identity tables (ADA-016).
- **Security:** a default-deny Spring Security chain. A JWT is accepted only while its persistent session is usable; recovery endpoints are public; every `/api/v1/wardrobe/**` call is protected and owner-scoped.
- **Persistence:** PostgreSQL through `JdbcClient` and Flyway; transactions are explicit through a port (`IdentityTransactions`, `WardrobeTransactions`).
- **One schema per owner** ([ARCH] AR-01–AR-08):
  - Identity's tables live in `identity.*` and Wardrobe's in `wardrobe.*`; every statement is schema-qualified.
  - No module joins, reads, writes or references another owner's tables by foreign key.
  - Each migration changes one owner schema.
  - Build-time tests enforce this. The single database role does not, at runtime.
- **Recovery:** a 256-bit secret, stored only as a SHA-256 hash, valid 30 minutes, used once, and replaced by a newer request. Completion changes the password and revokes every active session atomically. SMTP delivery is asynchronous after commit, with the approved Vietnamese message. The link is a verified HTTPS App Link / Universal Link in production (custom scheme only locally). Attempts are limited per email and per client before the account lookup (429), a successful request lasts at least a minimum time, and ended interactions are deleted after 30 days.
- **Wardrobe:** the 13 approved properties as enums whose codes are constant names with exact labels, category applicability in both Java and V8 `CHECK` constraints, Materials and Styles as distinct sets, exact Brand and the approved Price rules, idempotent creation by `Idempotency-Key`, a constant `NOT_EVALUATED` eligibility, `removed_at` for current versus removed, and an overview in keyset (cursor) pages.

Full detail: [shared-architecture.md](shared-architecture.md).

## 9. Prerequisites

| Tool | Version / note |
| --- | --- |
| JDK | 21 (Temurin or equivalent); `java -version` |
| Maven | Use the wrapper `backend\mvnw.cmd` (no separate install) |
| Docker | Docker Desktop running. Testcontainers starts PostgreSQL 17.11 and Mailpit `axllent/mailpit:v1.27.11` for integration tests |
| PostgreSQL for local runs | Compose service `postgres` (see `backend/README.md` §8) |
| Mailpit for local runs | Compose service `mailpit` added by US-AUTH-005 (SMTP `127.0.0.1:1025`, UI <http://localhost:8025>) |
| Shell | PowerShell; every command in the guides is PowerShell |
| Secrets | Database password and `CAPSULE_JWT_SIGNING_SECRET` set in the launching terminal ([backend README](../../../../../backend/README.md) §9). Optional SMTP credentials via `CAPSULE_MAIL_*`. **Never commit or paste real values.** |

## 10. Terminology

| Term | Meaning in these guides |
| --- | --- |
| Stage 0 | `sprint_02` at `90d3237` (§3); the starting point of US-AUTH-005 |
| Stage 1–4 | The repository state after applying guides 1…n in order |
| Owner schema | The PostgreSQL schema a module owns (`identity`, `wardrobe`, …); its tables are named `<schema>_<name>` |
| Recovery interaction | One password-reset issuance: a secret delivered by email, valid 30 minutes and used once (`identity.identity_password_reset` row) |
| Instruction link | `capsuleai.identity.password-reset.instruction-link` plus `#token=<secret>`: a verified HTTPS address in production, a custom scheme only in local development (OPEN-S2-02) |
| Attempt window | A fixed 15-minute window, aligned to the epoch, in which recovery attempts are counted (TD-S2-26) |
| Ended interaction | A reset interaction that was consumed, replaced or reached its expiry; deleted 30 days later (TD-S2-28) |
| Cursor | The opaque `nextCursor` of an overview page, sent back as `cursor` to continue (TD-S2-29) |
| Active session | A persistent session that is not revoked and not expired; revoked by a completed reset |
| Current garment | `removed_at IS NULL`; a removed garment is "no longer current" |
| Logical addition | One intended garment creation, identified by the client's `Idempotency-Key` |
| Controlled value | A vocabulary value: a stable code (enum constant) plus its exact approved label |
| Saveable minimum | Confirmed Category plus dominant Color (BRULE-GAR-002) |
| `NOT_EVALUATED` | The only recommendation-eligibility value in Sprint 2 (FR-GAR-004) |
| `USER_ENTERED` | Provenance of every manual garment |

## 11. Per-story workflow

For each story, in order (Sprint plan §8: Selected Story → JIT Design → Tests + Implementation → Refactor → Integration Verification → PR / CI / Review):

1. Create a story branch from the integration branch (`sprint_02`), for example `feature/US-AUTH-005-password-recovery`.
2. Read guide sections 1–10. Raise any disagreement about the [TECH]/[OPEN] items before coding.
3. Apply the production steps `P1…Pn` in order. For each [MODIFY], find the unique anchor first; if the BEFORE text does not match, stop and reconcile (an earlier guide was not applied, or was applied differently).
4. Apply the test steps `T1…Tn`.
5. Run the guide's unit, focused integration and full `clean verify` commands; inspect `target/surefire-reports` and `target/failsafe-reports`.
6. Do the guide's manual verification.
7. Open a pull request into `sprint_02` (§15); wait for hosted CI and review.
8. Record AC evidence (guide §15) and the outstanding client/PO evidence. Do **not** mark the story Done until the Definition of Done is met.

## 12. Testing approach

| Level | Where | Runs with | Typical tools |
| --- | --- | --- | --- |
| Domain and application unit | `*Test` | `mvnw test` (Surefire), no Docker | JUnit 6, AssertJ, in-memory port fakes, `MutableClock` |
| Adapter unit | e.g. `SmtpPasswordResetInstructionSenderTest`, `AsyncPasswordResetInstructionSenderTest` | Surefire | Hand-written recording fakes (no Mockito needed; see [shared-architecture.md §19](shared-architecture.md#19-testing-architecture)) |
| PostgreSQL integration | `*SchemaIT`, `Jdbc*RepositoryIT` | `mvnw verify` (Failsafe), Docker | Testcontainers PostgreSQL 17.11 |
| HTTP and security integration | `*ApiIT` | Failsafe | Full Spring context, MockMvc, real filter chain |
| SMTP integration | `SmtpPasswordResetInstructionSenderIT` | Failsafe | Testcontainers Mailpit and its HTTP API |
| Concurrency | `Concurrent*IT`, `PasswordResetLoginRaceIT` | Failsafe | `CountDownLatch`/`CyclicBarrier` gates, no sleeps |
| Failure | `PasswordResetFailureIT`, `WardrobeFailureIT` | Failsafe | Test-only port wrappers that fail on demand |
| Limits, timing, retention | `PasswordResetRateLimitIT`, `PasswordResetTimingIT`, `PasswordResetCleanupIT`, `JdbcPasswordResetAttemptLimiterIT` | Failsafe | Own contexts with low limits or a short minimum; `FOR UPDATE SKIP LOCKED` exercised from a second transaction |
| Query plan | `CurrentWardrobePageIT` | Failsafe | `EXPLAIN` of the keyset query |
| Contract | `IdentityOpenApiContractIT` | Failsafe | OpenAPI YAML compared with handlers and records |
| Cross-story | `WardrobeAccessAfterPasswordResetIT` | Failsafe | Recovery followed by every Wardrobe operation |
| Architecture | `ArchitectureDependencyTest` | Surefire | ArchUnit |
| SQL ownership | `PersistenceOwnershipSqlTest` | Surefire | Focused scanner over SQL strings and migrations |
| Schema ownership and upgrades | `DatabaseSchemaOwnershipIT`, `IdentitySchemaRelocationIT`, `*SchemaIT` upgrade cases | Failsafe | PostgreSQL catalog queries; `IsolatedMigrationDatabase` (a separate database per upgrade test) |

Time is controlled by the application's single `Clock` bean, replaced in tests by `MutableClock`, so expiry boundaries are exact and no test sleeps.

```powershell
Set-Location 'D:\CapsuleAI\backend'
.\mvnw.cmd test                                                     # unit tests only
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify        # everything, as CI runs it
```

## 13. Expected outcome of each stage

The author ran `mvnw --batch-mode --no-transfer-progress clean verify` and then that stage's own CI report guard on 2026-11-07, after the Product Owner decisions were incorporated, on each final stage commit of the scratch clone. These are the results:

| Stage | After | Unit tests (Surefire) | Integration tests (Failsafe) | Build | CI report guard (executed) |
| --- | --- | --- | --- | --- | --- |
| 0 | Schema-ownership correction (`90d3237`) | 161 passed | 124 passed | SUCCESS | 27 of 27 reports present and clean |
| 1 | US-AUTH-005 | 232 passed | 172 passed | SUCCESS | 49 of 49 |
| 2 | US-GAR-001 | 413 passed | 220 passed | SUCCESS | 61 of 61 |
| 3 | US-WAR-001 | 454 passed | 239 passed | SUCCESS | 66 of 66 |
| 4 | US-WAR-002 | 461 passed | 258 passed | SUCCESS | 69 of 69 |

Every row reports 0 failures, 0 errors and 0 skipped. The jump at Stage 2 comes mostly from parameterized cases: `GarmentEntryValidatorTest` alone runs 117 (for example, one per approved subtype and one per category with several Styles), and `PurchasePriceTest` runs 29. At Stage 3, `WardrobePageRequestTest` and `WardrobeCursorTest` add 35 parameterized and plain cases.

Your counts should match these exactly if the guides were applied without deviation. A different count means a step was skipped or changed: compare against the guide's test step list.

## 14. Continuous integration

Each guide adds its new test classes to the `$required` map in `.github/workflows/backend-ci.yml`, with each class's minimum number of test cases. The guard fails the job when a named report is missing, has fewer test cases than required, or reports any failure, error or skip. A renamed, emptied or skipped class therefore cannot disappear silently. The map grows from 20 entries (`dc4ddf3`) to 27 (Stage 0), 49 (Stage 1), 61 (Stage 2), 66 (Stage 3) and **69** (Stage 4).

**Triggers (REVIEW-CI option A, TD-S2-30).** The baseline runs for pushes to `main` and pull requests targeting `main`. The US-AUTH-005 guide's `backend-ci.yml` step adds pull requests targeting `sprint_02`, so every later story PR gets hosted evidence; the `main` triggers and every guard rule stay unchanged. Until that step is applied, this repository's real workflow is unchanged, and a story PR into `sprint_02` runs no hosted CI.

The workflow needs no new secret. Integration tests use Testcontainers (PostgreSQL, Mailpit) and the existing test-only configuration (`application-test.properties`, whose fixed signing key is a published test value, not a credential).

## 15. Branching and pull-request workflow

| Item | Convention |
| --- | --- |
| Integration branch | `sprint_02` (current) |
| Story branches | One per story, created from `sprint_02` after the previous story is merged, e.g. `feature/US-GAR-001-manual-garment` |
| Commits | Small, buildable commits. Each production step plus its tests may be one commit; keep migrations in their own commit |
| PR | One per story into `sprint_02`. The description lists AC evidence, test reports, the OpenAPI version and outstanding client/PO evidence |
| Review | At least one reviewer; check the migration, owner isolation, error codes and that no secret is committed |
| Release | `sprint_02` → `main` PR after all four stories; hosted CI must be green there |

This package itself is not committed, pushed or opened as a pull request.

## 16. Known limitations of the Sprint 2 increment

- Recovery is verified against Mailpit only. A real provider, sender domain with SPF/DKIM/DMARC, TLS and delivery evidence are required before release or Done (OPEN-S2-09, ⚠️).
- No HTTPS link domain with Android/iOS association files exists yet; locally a custom scheme stands in (OPEN-S2-02, ⚠️).
- Timing is hardened and measured, not constant; a database failure during issuance remains a truthful 503 that only a registered address can produce (accepted residual risk, OPEN-S2-05).
- Limits use fixed windows and the servlet's client address; the reverse-proxy settings of each deployment decide how trustworthy that address is.
- No compromised-password denylist (🟡 deferred by OPEN-S2-06).
- No images, editing, removal, search/filter, user-selectable sorting or recorded use; `removed_at` is set only by test fixtures.
- Client-side evidence (Vietnamese UI, accessibility, loading/draft states, "load more") is outside these backend guides.
- Schema ownership is enforced by build-time tests, not by database privileges: the application's single database role can still reach every schema at runtime. The Product Owner accepted this explicitly (REVIEW-DB-ROLE option A); it is not hard isolation (AR-08).

## 17. Product Owner decisions of 2026-11-07

No decision of the previous revision is still open. Each keeps its id; the status shows what is left.

| ID | Decision | Affects | Status |
| --- | --- | --- | --- |
| OPEN-S2-01A | Price rules exactly as TD-S2-11 | US-GAR-001 | ✅ ⏳ |
| OPEN-S2-01B | Brand rules (TD-S2-12) | US-GAR-001 | ✅ ⏳ |
| OPEN-S2-02 | Verified HTTPS link (App Links / Universal Links); custom scheme for local development only | US-AUTH-005 | ✅ ⏳ ⚠️ |
| OPEN-S2-03 | Vietnamese email approved with minor proofreading | US-AUTH-005 | ✅ ⏳ |
| OPEN-S2-04 | Per-address and per-client limits, check and complete protected, Identity-owned | US-AUTH-005 | ✅ ⏳ |
| OPEN-S2-05 | Truthful 503, timing hardening with measured tests, documented residual risk | US-AUTH-005 | ✅ ⏳ |
| OPEN-S2-06 | Denylist deferred; Sprint 1 password policy for registration and reset | US-AUTH-005 | ✅ 🟡 |
| OPEN-S2-07 | 30-day retention with tested automated cleanup | US-AUTH-005 | ✅ ⏳ |
| OPEN-S2-08 | Multiple Styles like Materials | US-GAR-001, US-WAR-002 | ✅ ⏳ |
| OPEN-S2-09 | Provider-neutral SMTP; real provider evidence before release | US-AUTH-005 | ✅ ⏳ ⚠️ |
| OPEN-S2-10 | Cursor pagination in Sprint 2 | US-WAR-001 | ✅ ⏳ |
| OPEN-S2-11 | PRD FEAT-WAR-001 readiness wording removed | US-WAR-001/002 | ✅ |
| OPEN-S2-12 | SRS "SOLID density" removed | US-GAR-001 | ✅ |
| REVIEW-CI | Hosted CI for PRs into `sprint_02` | All | ✅ ⏳ |
| REVIEW-DB-ROLE | Single database role with build-time checks; not hard isolation | All | ✅ |
| REVIEW-TAXONOMY | "Tanks & Camis" / `TANKS_AND_CAMIS` | US-GAR-001 | ✅ ⏳ |

Details: [shared-architecture.md §22](shared-architecture.md#22-technical-decisions-product-owner-decisions-and-contradictions). File-by-file mapping: [decision implementation notes](../decision-implementation-notes.md).

## 18. Package contents

| File | Purpose |
| --- | --- |
| [README.md](README.md) | This overview |
| [shared-architecture.md](shared-architecture.md) | Cross-story architecture, data model, migrations, API conventions, security, testing, shared-file ownership, decision log |
| [US-AUTH-005-guide.md](US-AUTH-005-guide.md) | Stage 0 → 1: email password recovery |
| [US-GAR-001-guide.md](US-GAR-001-guide.md) | Stage 1 → 2: manual garment creation |
| [US-WAR-001-guide.md](US-WAR-001-guide.md) | Stage 2 → 3: current-wardrobe overview |
| [US-WAR-002-guide.md](US-WAR-002-guide.md) | Stage 3 → 4: garment detail and cross-story evidence |
| [../decision-implementation-notes.md](../decision-implementation-notes.md) | Where each Product Owner decision of 2026-11-07 is realized (outside this folder) |

## 19. Authoritative sources

| Source | Path |
| --- | --- |
| Business Requirements | [docs/01-business/BRD.md](../../../../01-business/BRD.md) |
| Product Requirements | [docs/02-product/PRD.md](../../../../02-product/PRD.md) |
| Software Requirements | [docs/03-requirements/SRS.md](../../../../03-requirements/SRS.md) |
| Business Rules | [docs/03-requirements/business-rules.md](../../../../03-requirements/business-rules.md) |
| Use cases | [UC-004](../../../../03-requirements/use-cases/UC-004-reset-password.md), [UC-007](../../../../03-requirements/use-cases/UC-007-add-garment.md), [UC-008](../../../../03-requirements/use-cases/UC-008-view-wardrobe.md) |
| Architecture decisions | [docs/04-architecture/selected-architecture-decisions.md](../../../../04-architecture/selected-architecture-decisions.md) |
| API contract | [docs/05-api/openapi.yaml](../../../../05-api/openapi.yaml) |
| Sprint plan | [sprint-plan.md](../sprint-plan.md) |
| Definition of Done | [definition-of-done.md](../../../definition-of-done.md) |
| Garment-property authority | [Initial files/Image to Properties Extraction.md](../../../../../Initial%20files/Image%20to%20Properties%20Extraction.md) |
| Backend README | [backend/README.md](../../../../../backend/README.md) |

## 20. Final package checklist

- [ ] Stage 0 (`90d3237`) and the 2026-11-07 documentation updates committed first; `V1`–`V6` unchanged afterwards
- [ ] All four guides applied in order; every [MODIFY] anchor matched
- [ ] `clean verify` green locally after each stage; reports inspected
- [ ] CI guard passes with 69 required reports after Stage 4
- [ ] Hosted CI green on the story pull requests into `sprint_02` and on the `sprint_02` → `main` pull request
- [ ] Manual verification of each guide done
- [ ] External evidence marked ⚠️ collected: HTTPS link domain and app association, real email provider delivery (§17)
- [ ] Client evidence collected for the AC parts marked "client"
- [ ] Definition of Done reviewed per story, and no story marked Done before then
