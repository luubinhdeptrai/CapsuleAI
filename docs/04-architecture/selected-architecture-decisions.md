# CapsuleAI — Selected Architecture Decisions

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Selected Architecture Decisions |
| Version | 0.1.1 |
| Status | Baseline Draft |
| Decision State | ADA-001–018 Selected |
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

## 2. Purpose and Selection Authority

**All current recommended options from ADA-001–018 were selected without override or deferral by the Project Owner / Backend Developer on 2026-10-10.** ADA-001–014 select their recommended Option A; ADA-015–018 select their recommended Option B. All eight Critical Before ADD choices and all ten Important choices are Selected; the major-choice selection gate is satisfied.

This record is the authority for what was selected. [ADA](architecture-decision-analysis.md) retains the alternatives, trade-offs, importance, confidence and evidence gaps; its subsequent status/navigation synchronization does not change the v0.2 analyzed recommendations. ADD and the architecture views realize these directions, and later ADRs preserve consequential durable rationale.

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

Workflow Section 46 ordering remains: **ADD → Logical View → Implementation View → Deployment View → Data View → ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation**. Consequential ADRs link analysis, selection and affected views; this record does not replace them.

## 6. Backend Developer Handoff

- **Selected direction:** Use the combined directions above as ADD inputs. Selection is complete; confidence/evidence gaps still require design and verification rather than another pending selection gate.
- **Preserve during realization:** Owner authority, private-data lifecycle, coherent accepted effects and retries, validity before ranking, consistent outfit identity, exact same-basis intelligence, truthful missing/outdated states, purpose-minimal integration and inspectable evidence. Public contracts and inward dependencies must preserve these obligations.
- **Still intentionally unspecified:** Final context/module map, packages/classes/interfaces, schemas/tables/FKs/ORM/indexes, transaction/concurrency mechanisms, API/event payloads, protocols/numeric timeouts, runtime/model versions, providers, resource placement and tooling. Downstream realization detail is not a Deferred ADA decision.
- **Next step:** [ADD v0.1](ADD.md) now realizes these selected inputs; create the **Logical View** next, then follow Section 5's ordered views/design workflow. [Delivery Decisions](../06-scrum/delivery-decisions.md), DD-001–006, continue to govern product ordering/refinement and later Sprint commitment.

Known non-blocking source issues remain recorded in ADA Section 3.4: the incomplete FR-MET-004 benchmark cross-reference and absent focused UC diagram files. Existing explicit SRS benchmark authorities and the master diagram/textual specifications/activities remain usable; this selection neither repairs those sources nor infers missing content.

## 7. Status and Next Step

**Version 0.1.1 — Baseline Draft; ADA-001–018 Selected.** No override, deferred decision or superseded decision is recorded. No business/product/requirement, ASR obligation, QA measure, backlog/story or Sprint commitment changes.

**ADD → Logical View.** [ADD v0.1 — Baseline Draft](ADD.md) now exists. This revision synchronizes current navigation only and leaves all 18 selections unchanged; no separate architecture view, ADR, detailed design, Test Strategy, engineering baseline, Sprint artifact or implementation is created.
