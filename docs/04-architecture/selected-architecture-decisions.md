# CapsuleAI — Selected Architecture Decisions

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Selected Architecture Decisions |
| Version | 0.1.6 |
| Status | Baseline Draft |
| Decision State | ADA-001–018 Selected; architecture refinement AR-01–AR-08 approved 2026-11-06 (Section 8) |
| Selection Date | 2026-10-10 |
| Selected By | Project Owner / Backend Developer |
| Source Analysis | [Architecture Decision Analysis](architecture-decision-analysis.md), v0.2 analyzed recommendations |
| Requirements / Driver Inputs | [Quality Attribute Analysis](quality-attribute-analysis.md) and [ASR](ASR.md); owning requirements remain authoritative |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 17.2–17.3 and 46 |
| Authoritative Location | `docs/04-architecture/selected-architecture-decisions.md` |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-10 | Recorded explicit human acceptance of all 18 current ADA recommendations exactly as analyzed; no override, deferral, supersession or scope change. |
| 0.1.1 | 2026-10-10 | Navigation/status synchronization after ADD v0.1 creation; Logical View is next. All selected options, bases, impacts and selection date/human remain unchanged. |
| 0.1.2 | 2026-10-10 | Linked Logical View v0.1 and advanced current navigation to Implementation View; all selections and historical revision entries remain unchanged. |
| 0.1.3 | 2026-10-10 | Linked Implementation View v0.1 and recorded explicit human acceptance of the Logical decomposition; review precedes Deployment View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |
| 0.1.4 | 2026-10-11 | Linked Deployment View v0.1 and recorded explicit human acceptance of both Logical and Implementation Views; Deployment View review precedes Data View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |
| 0.1.5 | 2026-10-11 | Linked Data View v0.1 and recorded explicit human acceptance of all three preceding views; Data View review precedes ADRs. Navigation/status only; substantive baseline and historical revisions remain unchanged. |
| 0.1.6 | 2026-10-20 | Synchronized process navigation with the 2026-10-20 workflow: Test Strategy and Engineering Baseline precede Sprint delivery; detailed design is JIT for selected Sprint work; standalone ADRs are optional for significant future evolution. Substantive product, architecture and quality baselines remain unchanged. |
| 0.1.7 | 2026-11-06 | Added Section 8, the Product Owner-approved refinement AR-01–AR-08 (one PostgreSQL schema per persistence owner, no cross-context joins, persistence access or foreign keys, published contracts, enforcement). It decides the schema/FK/access detail that ADA-016 left downstream. ADA-001–018 selections, bases and impacts, and every earlier revision, remain unchanged. |

## 2. Purpose and Selection Authority

**All current recommended options from ADA-001–018 were selected without override or deferral by the Project Owner / Backend Developer on 2026-10-10.** ADA-001–014 select their recommended Option A; ADA-015–018 select their recommended Option B. All eight Critical Before ADD choices and all ten Important choices are Selected; the major-choice selection gate is satisfied.

This record is the authority for what was selected. [ADA](architecture-decision-analysis.md) retains the alternatives, trade-offs, importance, confidence and evidence gaps; its subsequent status/navigation synchronization does not change the v0.2 analyzed recommendations. ADA and this selected record already preserve the current decision baseline and durable rationale/history; ADD and the architecture views realize the selected directions. Standalone ADRs are optional only for significant future evolution, not a prerequisite to implementation or one per ADA.

Selected decisions do not imply approval of this Baseline Draft artifact, implemented conformance or passed benchmarks. Exact contexts, packages, classes, schemas, APIs and event payloads remain downstream. Business/product requirements and solution-neutral ASRs retain their own authority.

## 3. Selection Summary

Each selected option below reproduces the source ADA **Recommendation**, including its Option label. The full per-decision recommendation controls where ADA's compact analytical summary uses shorter wording.

| ADA ID | Decision Problem | Selected Option | Backend Impact | Status |
| --- | --- | --- | --- | --- |
| ADA-001 | Business-core architecture style | Option A — Modular monolith for the business core | Keep one coordinated business core and explicit domain boundaries. | Selected |
| ADA-002 | Backend application runtime | Option A — Java with Spring Boot for the business application | Use Java/Spring Boot; retain application-level authority checks. | Selected |
| ADA-003 | Authoritative persistence model and database | Option A — One relational primary database: PostgreSQL | Use PostgreSQL as the primary relational authority. | Selected |
| ADA-004 | AI/CV execution boundary | Option A — Separate private Python analysis runtime | Keep analysis private and separate from confirmed-state authority. | Selected |
| ADA-005 | Backend-to-AI interaction | Option A — Bounded synchronous complete-result interaction | Await a bounded complete result; preserve truthful fallback. | Selected |
| ADA-006 | External integration boundaries | Option A — Narrow explicit ports/adapters for real dependencies | Isolate provider semantics behind narrow business ports/adapters. | Selected |
| ADA-007 | Private image persistence and access | Option A — Private object storage with application-controlled authorized delivery | Store media privately and authorize delivery through the application. | Selected |
| ADA-008 | Session, refresh and reset authority | Option A — Primary database as persistent session/reset authority | Persist session/reset authority alongside related account state. | Selected |
| ADA-009 | Logical-action idempotency and accepted effects | Option A — Database-backed logical-action coordination and coherent effects | Coordinate accepted logical actions durably and coherently. | Selected |
| ADA-010 | Advice and wardrobe-intelligence computation | Option A — Request-time evaluation from authoritative inputs with shared semantics | Evaluate current inputs with shared validity/identity and truthful results. | Selected |
| ADA-011 | Dedicated cache strategy | Option A — No dedicated cache initially | Start without a dedicated cache and measure complete operations. | Selected |
| ADA-012 | Messaging and background coordination | Option A — Explicit core coordination with no broker initially | Coordinate core effects explicitly; make required later work recoverable. | Selected |
| ADA-013 | Initial deployment and resource separation | Option A — Small controlled deployment with supervised core/AI processes and durable resources | Use supervised separate processes and durable resources; verify placement. | Selected |
| ADA-014 | Protected observability and validation evidence | Option A — Minimal protected structured logs, metrics and health/recovery evidence | Expose protected outcome, timing, health and recovery evidence. | Selected |
| ADA-015 | Internal Module Architecture | Option B — Pragmatic Clean Architecture inside each business module | Keep domain/application dependencies inward with useful ports/adapters. | Selected |
| ADA-016 | Context Data Ownership | Option B — One PostgreSQL database initially with explicit context-owned logical persistence boundaries | Own persistence logically per context within one PostgreSQL. | Selected |
| ADA-017 | Inter-Context Communication | Option B — Explicit published module contracts, choosing synchronous local interaction or event-based interaction according to business semantics | Choose local calls/events through public contracts by business semantics. | Selected |
| ADA-018 | Context Model Isolation and Anti-Corruption | Option B — Published contracts + Anti-Corruption Layer translation at meaningful semantic boundaries | Keep models private and translate meanings at real semantic boundaries. | Selected |

## ADA-001 — Business-core architecture style

**Selected Option:** Option A — Modular monolith for the business core

**Selection Basis:** Accepted the [ADA-001 recommendation](architecture-decision-analysis.md). Local coordination and one shared interpretation of domain rules fit the MVP's authority and integrity pressures without distributing business-state ownership.

**Backend Developer Impact:** Design explicit domain responsibilities in one deployable business core. Keep boundaries suitable for later justified extraction; core releases remain coupled and boundary discipline remains ongoing work.

**Status:** Selected

## ADA-002 — Backend application runtime

**Selected Option:** Option A — Java with Spring Boot for the business application

**Selection Basis:** Accepted the [ADA-002 recommendation](architecture-decision-analysis.md). The analyzed runtime supports transaction-oriented business work, SQL integration and operating evidence. Framework defaults do not establish CapsuleAI session or ownership correctness.

**Backend Developer Impact:** Use Java with Spring Boot for the business application and integrate the private AI runtime explicitly. Preserve domain tests and application-level usable-session checks; supported JDK/framework versions, libraries and build tooling remain downstream.

**Status:** Selected

## ADA-003 — Authoritative persistence model and database

**Selected Option:** Option A — One relational primary database: PostgreSQL

**Selection Basis:** Accepted the [ADA-003 recommendation](architecture-decision-analysis.md). Related business and session state need coordinated authoritative changes; one relational store reduces consistency surfaces while supporting justified variable garment information.

**Backend Developer Impact:** Realize the primary authority in PostgreSQL. Design transactions and concurrency deliberately; media operations are separate recoverable effects. Tables, ORM, indexes, isolation and database version remain Data View/detailed design decisions.

**Status:** Selected

## ADA-004 — AI/CV execution boundary

**Selected Option:** Option A — Separate private Python analysis runtime

**Selection Basis:** Accepted the [ADA-004 recommendation](architecture-decision-analysis.md). A private analysis process separates model execution and dependency changes from authoritative wardrobe acceptance. Its extra communication and resource costs still require verification.

**Backend Developer Impact:** Use a separate private Python runtime with purpose-minimal inputs and proposal/preview outputs. AI must not independently write confirmed business state. Models, licenses, inference framework and compute sizing remain downstream.

**Status:** Selected

## ADA-005 — Backend-to-AI interaction

**Selected Option:** Option A — Bounded synchronous complete-result interaction

**Selection Basis:** Accepted the [ADA-005 recommendation](architecture-decision-analysis.md). A bounded complete-result interaction gives a clear completion boundary with fewer job states. Quick acceptance alone cannot satisfy the existing full-result processing measure.

**Backend Developer Impact:** Design bounded waiting and truthful pending/failure/manual continuation, with safe late-output handling. Completion includes both preview and reviewable proposals; it never overwrites confirmed data. Protocol and numeric timeouts remain downstream.

**Status:** Selected

## ADA-006 — External integration boundaries

**Selected Option:** Option A — Narrow explicit ports/adapters for real dependencies

**Selection Basis:** Accepted the [ADA-006 recommendation](architecture-decision-analysis.md). Real dependency boundaries need consistent business outcomes, failure handling and minimal disclosure. Small adapters isolate provider semantics without a speculative integration framework.

**Backend Developer Impact:** Keep provider DTOs, SDKs and errors in adapters, with ACL translation where meanings differ. Preserve UC-006 as location/weather acquisition authority; consuming assessments do not become separate acquisition goals. Exact providers and contracts remain downstream.

**Status:** Selected

## ADA-007 — Private image persistence and access

**Selected Option:** Option A — Private object storage with application-controlled authorized delivery

**Selection Basis:** Accepted the [ADA-007 recommendation](architecture-decision-analysis.md). Separate media storage avoids concentrating image volume in transactional authority. Access and physical purge still depend on application authority and explicit cross-resource lifecycle handling.

**Backend Developer Impact:** Use private object storage for images/previews with current account/session-authorized delivery. Design recoverable association and cleanup, including applicable copies/versions; object existence or an unrevocable bearer link is insufficient. Provider and access mechanics remain downstream.

**Status:** Selected

## ADA-008 — Session, refresh and reset authority

**Selected Option:** Option A — Primary database as persistent session/reset authority

**Selection Basis:** Accepted the [ADA-008 recommendation](architecture-decision-analysis.md). Single-success refresh/reset transitions and revocation need persistent coordinated authority. Offline JWT validity alone cannot enforce the existing usable-session policies.

**Backend Developer Impact:** Persist session, refresh and reset authority in PostgreSQL and require usable account/session context for protected operations. Preserve the different logout, refresh-reuse and reset effects. Token representation, keys and concurrency mechanisms remain downstream.

**Status:** Selected

## ADA-009 — Logical-action idempotency and accepted effects

**Selected Option:** Option A — Database-backed logical-action coordination and coherent effects

**Selection Basis:** Accepted the [ADA-009 recommendation](architecture-decision-analysis.md). Durable action identity/outcome supports safe retry after uncertain completion or restart. Coherent accepted effects must preserve explicit repeated Wear intentions and survivor semantics.

**Backend Developer Impact:** Coordinate logical actions and authoritative effects in the database, using local ACID where correctness requires it and owner operations through public boundaries. Keep new same-outfit/day Wear reports distinct from retries. Review cross-context transaction coupling for future extraction; external effects need recoverable coordination.

**Status:** Selected

## ADA-010 — Advice and wardrobe-intelligence computation

**Selected Option:** Option A — Request-time evaluation from authoritative inputs with shared semantics

**Selection Basis:** Accepted the [ADA-010 recommendation](architecture-decision-analysis.md). Request-time evaluation reduces stored-result invalidation surfaces while keeping evidence close to the request. Exact intelligence still needs coherent current inputs, complete relevant sets and consistent validity/identity semantics.

**Backend Developer Impact:** Obtain authoritative inputs through published owner contracts and validate the evaluated basis. Apply validity before ranking; exact Multiplier compares complete current/expanded sets on the same basis, never the displayed daily subset. Preserve Exact, Evaluated Zero, Incomplete, Unavailable and Outdated meanings; algorithms and optimizations remain downstream.

**Status:** Selected

## ADA-011 — Dedicated cache strategy

**Selected Option:** Option A — No dedicated cache initially

**Selection Basis:** Accepted the [ADA-011 recommendation](architecture-decision-analysis.md). No current evidence establishes a dedicated cache bottleneck. Starting from authority and direct computation avoids another correctness, invalidation and private-data lifecycle surface.

**Backend Developer Impact:** Exclude Redis or another dedicated cache from the initial required architecture. Profile complete representative operations before revisiting acceleration, with ownership/basis/invalidation safeguards. Existing provider-evidence reuse under its freshness rule remains available.

**Status:** Selected

## ADA-012 — Messaging and background coordination

**Selected Option:** Option A — Explicit core coordination with no broker initially

**Selection Basis:** Accepted the [ADA-012 recommendation](architecture-decision-analysis.md). The MVP has no independent event-consumer or sustained queue requirement. Direct acceptance coordination can coexist with recoverable housekeeping and semantically appropriate local events.

**Backend Developer Impact:** Start without a broker; required cleanup must be durable/resumable rather than depend on in-memory signals. Keep optional measurement failure independent of core acceptance. Any future Integration Event is a separately designed published contract, not an unchanged local event payload.

**Status:** Selected

## ADA-013 — Initial deployment and resource separation

**Selected Option:** Option A — Small controlled deployment with supervised core/AI processes and durable resources

**Selection Basis:** Accepted the [ADA-013 recommendation](architecture-decision-analysis.md). A small operating footprint fits current needs while preserving process control and recovery obligations. Separate core/AI processes do not determine host placement or demonstrate adequate capacity.

**Backend Developer Impact:** Design controlled core/AI process supervision and authoritative/media durability across restart. Deployment View resolves placement and resources from evidence; co-location is conditional and retains shared-host failure/contending-resource risks. Hosting, containers and CPU/GPU allocation remain downstream.

**Status:** Selected

## ADA-014 — Protected observability and validation evidence

**Selected Option:** Option A — Minimal protected structured logs, metrics and health/recovery evidence

**Selection Basis:** Accepted the [ADA-014 recommendation](architecture-decision-analysis.md). Existing obligations require inspectable acceptance, complete-operation timing, assessment basis and recovery evidence. Focused structured instrumentation supports this without a full telemetry platform.

**Backend Developer Impact:** Allocate protected logs, metrics, health/recovery evidence and necessary correlation. Preserve distinct purposes/lifecycles for functional history, user-linked measurement and controlled AI evaluation; optional measurement failure cannot undo acceptance. Fields, tooling, retention realization and vendor remain downstream.

**Status:** Selected

## ADA-015 — Internal Module Architecture

**Selected Option:** Option B — Pragmatic Clean Architecture inside each business module

**Selection Basis:** Accepted the [ADA-015 recommendation](architecture-decision-analysis.md). Dependency isolation protects shared domain meaning and independent tests from framework/provider changes. The analyzed approach remains small enough for simple behavior and explicit transaction coordination.

**Backend Developer Impact:** Use pragmatic Clean Architecture inside each business module, with adapters implementing needed ports and configuration at the edge. Keep Spring/JPA, HTTP and provider types outside the domain/application core. Avoid interfaces for every class, generic repositories and empty layers; exact packages and mappings remain downstream.

**Status:** Selected

## ADA-016 — Context Data Ownership

**Selected Option:** Option B — One PostgreSQL database initially with explicit context-owned logical persistence boundaries

**Selection Basis:** Accepted the [ADA-016 recommendation](architecture-decision-analysis.md). Privacy, accepted-state and same-basis obligations need explicit mutation/lifecycle owners. Those logical boundaries are compatible with one physical database and useful local transactions.

**Backend Developer Impact:** Another context must not bypass an owner through private repository/entity/table/schema access by default. Collaborate through published owner boundaries; any justified exception needs purpose, invariant/lifecycle impact, coupling and review evidence in the owning design/ADR. Final contexts, schemas and FK/access rules remain downstream.

**Status:** Selected

## ADA-017 — Inter-Context Communication

**Selected Option:** Option B — Explicit published module contracts, choosing synchronous local interaction or event-based interaction according to business semantics

**Selection Basis:** Accepted the [ADA-017 recommendation](architecture-decision-analysis.md). Immediate exclusion/revocation, coherent accepted effects and current assessments require deliberate collaboration semantics. Independent reactions can use events where delayed effects are permitted or immediate obligations remain preserved.

**Backend Developer Impact:** Use narrow published contracts for authoritative information and coordinated acceptance. An event does not automatically mean asynchronous or durable delivery. Do not introduce internal HTTP or a broker to imitate future services; exact contracts, events, transaction phases and sequences remain downstream.

**Status:** Selected

## ADA-018 — Context Model Isolation and Anti-Corruption

**Selected Option:** Option B — Published contracts + Anti-Corruption Layer translation at meaningful semantic boundaries

**Selection Basis:** Accepted the [ADA-018 recommendation](architecture-decision-analysis.md). Confirmed/proposed, current/historical/hypothetical and optional-commerce meanings need explicit protection across boundaries. Focused translation reduces model coupling without duplicating authoritative stores.

**Backend Developer Impact:** Keep internal/context/provider models private; publish needed language and translate into consuming meanings where appropriate. Ports define dependencies, adapters realize access and ACLs protect interpretation; ACL does not imply a snapshot/projection. Preserve required minimal historical snapshots separately, and avoid one universal domain model or independently drifting validity/identity rules.

**Status:** Selected

## 4. Selected Architecture Direction

The business core is a **Modular Monolith** using **Java with Spring Boot**. Its boundaries follow domain responsibilities and support selective future service extraction when real drivers justify it. Core releases are currently coupled. Extraction would require renewed contract, authorization, consistency, data-migration, failure and operating design; healthy boundaries do not make it automatic or cost-free.

Inside each business module, **pragmatic Clean Architecture** keeps domain/application dependencies inward, with needed ports and outer adapters. Contexts own their persisted data logically, publish explicit contracts and keep internal models private. Synchronous local calls and event-based interaction follow business semantics; focused ACL translation protects meaning where models differ. An ACL neither selects a copied-state store nor replaces necessary minimal historical snapshots.

**One PostgreSQL database** supplies primary business, session/reset and logical-action authority. Public owner operations may participate in deliberately coordinated local ACID transactions while preserving ownership. **Private object storage** holds media under application-authorized access and recoverable lifecycle coordination. Database transactions do not make media or email effects atomic.

A **separate private Python runtime** supplies analysis proposals/previews through **bounded synchronous complete-result interaction**; user-confirmed state remains authoritative. Advice/intelligence starts from request-time authoritative inputs with shared validity/identity semantics and truthful completion/currentness. Required later lifecycle work remains durable/resumable. Protected structured logs, metrics and health/recovery evidence support a small controlled deployment, with actual resource placement resolved from evidence.

There is **no dedicated cache or broker initially**. These selections add no API gateway, service discovery, service mesh, Kubernetes, physical database-per-service, distributed Saga, universal Outbox or distributed-tracing platform. Possible future remote adapters/Integration Events require new evidence and design rather than simulated microservices today. Final module/context names remain an ADD/views responsibility.

## 5. Traceability and Realization

All rows trace to the corresponding stable entries in [ADA Sections 5/10](architecture-decision-analysis.md), which retain ASR, QA and requirement evidence. ADD consumes the complete selection set; the destinations below indicate principal later responsibilities, not new artifact creation or fixed contracts.

| Selected ADA IDs | Source Analysis | Principal Downstream Realization |
| --- | --- | --- |
| ADA-001, ADA-015–018 | Corresponding ADA analyses and boundary-policy interactions in Section 7 | ADD; Logical, Implementation and Data Views for domain responsibilities, dependency direction, ownership, contracts and isolation. |
| ADA-002 | ADA-002 | ADD / Implementation View; later engineering baseline for supported runtime versions and tooling. |
| ADA-003, ADA-016 | ADA-003 / ADA-016 | Data View and later detailed data design; Implementation/Deployment implications for authority and durability. |
| ADA-004/005/007/013 | Corresponding ADA analyses | Implementation and Deployment Views; Data View for authoritative/media ownership and lifecycle; later integration contracts and sequences as needed. |
| ADA-006/017/018 | Corresponding ADA analyses | Logical / Implementation Views; later API/service-contract and semantic translation design. |
| ADA-008/009 | ADA-008 / ADA-009 | Implementation / Data Views; later necessary sequence/state/security design and verification. |
| ADA-010/011/012 | Corresponding ADA analyses | Logical / Implementation / Data Views; later performance, exactness and reliability detailed design. |
| ADA-014 | ADA-014 | Implementation / Deployment Views; later Test Strategy and engineering evidence mechanisms. |

Workflow Section 46 ordering is **ADD + four views → Test Strategy → Engineering Baseline → Backlog Refinement → Sprint Planning → Sprint execution: selected-Story JIT Detailed Design → Automated Test + Implementation → PR / CI / Review → Done**. ADA and this selected record already retain current decision rationale/history. Optional future ADRs link to these authorities and affected views; they do not duplicate each current ADA decision.

## 6. Backend Developer Handoff

- **Selected direction:** Use the combined directions above as ADD inputs. Selection is complete; confidence/evidence gaps still require design and verification rather than another pending selection gate.
- **Preserve during realization:** Owner authority, private-data lifecycle, coherent accepted effects and retries, validity before ranking, consistent outfit identity, exact same-basis intelligence, truthful missing/outdated states, purpose-minimal integration and inspectable evidence. Public contracts and inward dependencies must preserve these obligations.
- **Still intentionally unspecified:** Final context/module map, packages/classes/interfaces, schemas/tables/FKs/ORM/indexes, transaction/concurrency mechanisms, API/event payloads, protocols/numeric timeouts, runtime/model versions, providers, resource placement and tooling. Downstream realization detail is not a Deferred ADA decision.
- **Next step:** [ADD](ADD.md) realizes the selected inputs. **Current handoff: Test Strategy → Engineering Baseline → Backlog Refinement → Sprint Planning.** [Logical View v0.1](views/logical-view.puml), [Implementation View v0.1](views/implementation-view.puml), [Deployment View v0.1](views/deployment-view.puml) and [Data View v0.1 — Baseline Draft](views/data-view.puml) already exist. During Sprint execution, selected Stories receive only necessary JIT Detailed Design, with automated tests/implementation and PR/CI/review through Done. ADA and Selected Architecture Decisions already preserve the baseline; standalone ADRs are optional for significant future evolution. Follow Workflow Sections 25–33 and 46; no whole-MVP detailed-design gate is required. [Delivery Decisions](../06-scrum/delivery-decisions.md), DD-001–006, still govern product ordering/refinement and later Sprint commitment.

Known non-blocking source issues remain recorded in ADA Section 3.4: the incomplete FR-MET-004 benchmark cross-reference and absent focused UC diagram files. Existing explicit SRS benchmark authorities and the master diagram/textual specifications/activities remain usable; this selection neither repairs those sources nor infers missing content.

## 7. Status and Next Step

**Version 0.1.7 — Baseline Draft; ADA-001–018 Selected; AR-01–AR-08 approved refinement (Section 8).** No override, deferred decision or superseded decision is recorded; Section 8 refines ADA-016–018 without replacing them. No business/product/requirement, ASR obligation, QA measure, backlog/story or Sprint commitment changes.

**Current handoff: Test Strategy → Engineering Baseline → Backlog Refinement → Sprint Planning.** [Logical View v0.1](views/logical-view.puml), [Implementation View v0.1](views/implementation-view.puml), [Deployment View v0.1](views/deployment-view.puml) and [Data View v0.1 — Baseline Draft](views/data-view.puml) already exist. During Sprint execution, selected Stories receive only necessary JIT Detailed Design, with automated tests/implementation and PR/CI/review through Done. ADA and Selected Architecture Decisions already preserve the baseline; standalone ADRs are optional for significant future evolution. Follow Workflow Sections 25–33 and 46; no whole-MVP detailed-design gate is required. All 18 selections remain unchanged; Data View approval remains pending.

## 8. Approved Architecture Refinement AR-01–AR-08 — Context-Owned Schemas

### 8.1 Refinement Record

| Field | Value |
| --- | --- |
| Refinement IDs | AR-01–AR-08 |
| Refines | ADA-016 (primary), ADA-017 and ADA-018; consistent with ADA-001, ADA-003, ADA-009, ADA-012 and ADA-015 |
| Approved By | Product Owner |
| Approval Date | 2026-11-06 |
| Status | Approved; realized for the implemented Identity module (Flyway V6) and enforced by automated tests (Section 8.5) |
| Nature | A stricter, explicit realization of the selected options. ADA-016 selected logical, context-owned persistence inside one PostgreSQL database and left "final contexts, schemas and FK/access rules" downstream. ADD Section 9.4 likewise left "exact schema/FK/joins/access rules" downstream "rather than a universal cross-context FK ban". This refinement decides those details. It changes no selected option, selection basis, backend impact or historical revision above, and it was not in force when ADA-016 was selected. |

### 8.2 Rules

| ID | Rule | Consequence for code, SQL and migrations |
| --- | --- | --- |
| AR-01 | **One PostgreSQL schema per persistence owner.** Every business module that persists its own state owns exactly one dedicated schema inside the single CapsuleAI database (ADA-003 unchanged). | The schema name is the module name in snake_case (Section 8.3). Tables are named `<schema>_<name>` and are always referenced as `<schema>.<schema>_<name>`. A module's schema is created by the migration of the first story that persists its state; no empty or speculative schema or table is created in advance. Flyway's default schema holds only Flyway's own `flyway_schema_history`. |
| AR-02 | **No cross-context SQL joins.** A query joins only tables of its own owner's schema, even though every schema is in the same database. | Information owned elsewhere is obtained through the owner's published API and combined in application code, or through a deliberately designed published read contract. |
| AR-03 | **No direct cross-context persistence access.** A module does not query, modify or depend on another owner's tables, table layout, JDBC repositories, persistence models or private domain models. No database view, function, trigger or rule bypasses an owner. | Only a module's own persistence adapter (`adapter.out.persistence`) issues SQL against its schema. A repository never becomes a generic repository for another context's data. |
| AR-04 | **No cross-context foreign keys.** Foreign keys stay inside one owner's schema. | A reference to another owner's entity (for example `wardrobe.wardrobe_garment.owner_account_id`) is a plain identifier. Its validity comes from the owner's published contract at the time of the operation, not from the database. Section 8.4 states the lifecycle consequences. |
| AR-05 | **Published module APIs.** Inter-context collaboration uses the owner's `api` package. | Authenticated requests reuse Identity's `AuthenticatedAccount` principal, which the security chain verifies against the persistent session; no per-request account lookup is added. A contract such as an account-existence lookup is created only when a real use case needs it. |
| AR-06 | **No internal HTTP.** Modules call each other's published Java APIs in-process (ADA-001, ADA-017). | HTTP remains for real external boundaries (mobile clients, providers). |
| AR-07 | **Deliberate transactional consistency.** One database still allows local ACID transactions, but a module never joins another owner's transaction by touching its tables. | A transaction spanning owners is coordinated explicitly through the owners' published operations. Its invariant and participants are documented with their service-extraction coupling (ADA-009, ADD Section 9.2), and partial failure is never hidden. The business requirement decides whether owners communicate by synchronous call or by event (ADA-017); this rule does not. |
| AR-08 | **Automated enforcement.** The rules are verified by build-time tests (Section 8.5), not only by naming. | The application uses one database role with access to every schema. **Database privileges therefore do not enforce context isolation at runtime**; code ownership, published contracts and the tests do. Dedicated per-owner roles and data sources are not selected; they would add configuration and transaction complexity without a current requirement. |

### 8.3 Persistence Owners and Schemas

The owners are the five logical contexts of the accepted [Logical View](views/logical-view.puml) and the Product Measurement supporting capability. The [Implementation View](views/implementation-view.puml) gives Product Measurement its own measurement persistence and lifecycle. No other context is introduced.

| Owner (package) | Schema / table prefix | Owned state (Data View) | Published contracts | May depend on (published APIs only) | Status |
| --- | --- | --- | --- | --- | --- |
| Identity (`identity`) | `identity` / `identity_` | Account, credentials, persistent sessions, refresh tokens, password-reset interactions | `AuthenticatedAccount`, `SessionAuthority` (implemented) | None | **Implemented:** `identity.identity_account`, `identity.identity_session`, `identity.identity_refresh_token` (moved by V6) |
| Personal Context (`personalcontext`) | `personal_context` / `personal_context_` | Profile, declared preferences and needs, selected location, translated environmental evidence | Declared, current and limited context | Identity | Not implemented; no schema yet |
| Wardrobe (`wardrobe`) | `wardrobe` / `wardrobe_` | Confirmed garments and their properties, Materials, media associations, garment action outcomes, minimal removed-garment snapshots | Confirmed properties and current ownership | Identity | Not implemented; Sprint 2 (US-GAR-001) creates the schema |
| Outfit Advice and Use (`outfitadviceanduse`) | `outfit_advice_and_use` / `outfit_advice_and_use_` | Feedback, Wear events, history, effective evidence, advice action outcomes | Advice, feedback, Wear and reported use; Outfit Evaluation Semantics | Identity, Wardrobe, Personal Context | Not implemented |
| Wardrobe Intelligence (`wardrobeintelligence`) | `wardrobe_intelligence` / `wardrobe_intelligence_` | Only justified owned source/action/work state; no result store mandated | Coverage, Gaps, candidate utility, shopping | Identity, Wardrobe, Personal Context, Outfit Evaluation Semantics | Not implemented |
| Product Measurement (`productmeasurement`, supporting) | `product_measurement` / `product_measurement_` | Purpose-limited, user-linked measurement evidence with its own retention | Measurement intake (local events) | Identity; receives facts from other owners | Not implemented |

### 8.4 Cross-Owner Lifecycle Consistency

Because AR-04 forbids cross-schema foreign keys, the database neither prevents nor cascades a reference to another owner's entity. Consistency is an application responsibility:

- **Creation and use:** an owner stores another owner's identifier only as the result of that owner's published contract in the same operation, for example the authenticated account from `AuthenticatedAccount`. The identifier alone does not prove the referenced entity still exists.
- **Removal or deletion:** no account-deletion capability is selected today. A future one must be an explicit, durable and resumable application process (ADA-012). Identity publishes the lifecycle fact or operation. Every owner removes or de-identifies its own data through its own application services and persistence, within the applicable retention rules (DATA-RET-001–003, ADD Section 9.3). Cross-schema `ON DELETE CASCADE` is not available, and one owner must not imitate it by deleting another owner's rows.
- **Verification:** where a real use case needs to confirm another owner's entity, that owner publishes a narrow contract for it (AR-05).

### 8.5 Enforcement and Its Limits

| Check | Verifies |
| --- | --- |
| `ArchitectureDependencyTest` (ArchUnit) | Modules use each other only through `api`; published APIs expose no private domain or application model; Spring JDBC stays in persistence adapters and configuration; no dependency cycle between modules; domain and application code stay framework-free; every module package is registered with its schema. Deliberate-violation fixtures prove that each rule fails when broken. |
| `PersistenceOwnershipSqlTest` | Production SQL (Java string literals and text blocks) names only its own module's schema and always schema-qualifies owner tables. Every migration after V6 changes exactly one owner schema, so it cannot create a cross-schema foreign key, view or join. It is a focused scanner, not a SQL parser; its conventions are listed in the test. |
| `DatabaseSchemaOwnershipIT` | On the fully migrated database, through the PostgreSQL catalog: only registered owner schemas exist besides Flyway's default schema; that schema holds only `flyway_schema_history`; every table is in its owner's schema; no foreign key or view crosses schemas; owner schemas hold no routines. |
| `IdentitySchemaRelocationIT` | V6 on a realistic Sprint 1 database: every row, index and constraint survives, the adapters work on the upgraded data, the history remains valid, and a restart applies nothing. |

These checks run in `mvnw clean verify`, and the CI report guard requires them. They detect violations at build time; they do not restrict a running application, because the single database role can access every schema (AR-08).

### 8.6 Realization for the Implemented Identity Module

V1–V5 created the Identity tables unqualified in Flyway's default schema. They have been applied to persistent databases and are never edited. The forward migration `V6__move_identity_tables_into_identity_schema.sql` creates the `identity` schema and moves the three tables with `ALTER TABLE ${flyway:defaultSchema}.<table> SET SCHEMA identity`:

- It keeps every row, index, constraint and foreign key, copies nothing, and runs in one transaction.
- Flyway's configuration and history location are unchanged, so V1–V5 are not re-applied.
- All Identity SQL is now schema-qualified.
- V6 needs the migrating role to own the three tables (a requirement of `SET SCHEMA`) and to hold `CREATE` on the database. This is true wherever the same role ran V1–V5; otherwise V6 fails without changing anything.
