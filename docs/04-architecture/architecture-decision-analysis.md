# CapsuleAI — Architecture Decision Analysis

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Architecture Decision Analysis |
| Version | 0.1 |
| Status | Baseline Draft |
| Decision State | All recommendations Proposed — Awaiting Selection |
| Last Updated | 2026-10-08 |
| Ownership | Architect / Tech Lead with Developer input; review with AI, Mobile, QA and Product/BA where relevant |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 17.2–17.3, 18, 39–43 and 46 |
| Requirements Baseline | BRD v0.3; PRD v0.2; SRS v0.3; Business Rules v0.1.2 |
| Authoritative Location | `docs/04-architecture/architecture-decision-analysis.md` |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-08 | Initial comparison of 14 architecture decision problems across the whole MVP; recommendations await human selection. No product requirement, QA measure or ASR content changes. |

## 2. Purpose

Expose the major choices between required architectural capability and a coherent design. The analysis covers the complete three-pillar mobile MVP: confirmed wardrobe, valid personalized styling and reported behavior, and contextual Coverage/Gaps plus hypothetical candidate utility. The first eight stories are implementation/refinement context, not the architecture boundary.

| Artifact | Responsibility |
| --- | --- |
| ASR | What architecture must be capable of satisfying; requirements and pressures, not solutions. |
| Architecture Decision Analysis (ADA) | Problems, credible alternatives, trade-offs, recommendation and confidence. |
| Selected Architecture Decisions | Explicit human selection or justified bounded deferral of analyzed options. |
| ADD and its views | Architecture structure and realization given ASRs and explicitly selected directions. |
| ADR | Durable rationale, alternatives and consequences for consequential selected decisions, recorded later in this workflow. |

**Recommendation is not selection.** Every ADA entry is **Proposed — Awaiting Selection**. No runtime, database, AI process, storage provider or topology is established by this document. Conditional consequences describe what ADD would inherit if the option were selected. Selection can accept a recommendation, choose another analyzed option, or defer a non-blocking decision with an owner, boundary and resolution point. A material new alternative requires analysis before selection.

## 3. Inputs and Decision Method

### 3.1 Source Authority and Review Scope

The current workflow was read first, followed by the complete relevant repository: all current business/product/requirements artifacts, all interaction specifications/activities, architecture analyses and Scrum material. Before drafting this artifact, ASR and QA analysis were reopened in full, with the architecture-significant SRS sections, relevant rules/UC flows, all 12 activities and DoD criteria reopened. Current canonical sources prevail over historical stack suggestions.

| Source | Use in this analysis |
| --- | --- |
| [ASR](ASR.md), Sections 4–12 | Seven drivers, nine formal ASRs, significant functional areas, hard constraints and cross-cutting concerns; primary decision-problem input. Driver AD-01–07 IDs differ from Activity Diagram AD-004 etc. |
| [Quality Attribute Analysis](quality-attribute-analysis.md), Sections 5–24 | All 14 significance classifications, 17 scenarios, tensions and constraint interpretation; primary evaluation input. |
| [BRD](../01-business/BRD.md); [PRD](../02-product/PRD.md) | Three pillars, BG/BR/CAP meanings, settled scope and product decisions. |
| [SRS](../03-requirements/SRS.md), especially Sections 2.3/2.4, 3–9 and 12 | Normative functional/data/interface/security/privacy/quality boundaries and acceptance conditions. |
| [Business Rules](../03-requirements/business-rules.md), all 11 families and Sections 14–15 | Precedence, confirmed authority, readiness, validity/identity, Wear/time/survivors, Coverage, exactness and optional commerce. |
| [Master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml); [all 23 UC specifications](../03-requirements/use-cases/) | User goals, ownership, accepted mutations, dependency and failure semantics. Particularly UC-004/006/007/011–014/016/017/019–023. |
| [All 12 Activity Diagrams](../03-requirements/activity-diagrams/) | AD-004/006/007/011/012/014/016/017/019/020/021/022 visualize the normative UC behavior; they do not add requirements. |
| [Product Goal](../06-scrum/product-goal.md); [Product Backlog](../06-scrum/product-backlog.md); [all eight stories and index](../06-scrum/stories/README.md) | Whole 41-PBI horizon and concrete near-term AC; existing order/refinement boundaries are preserved. |
| [DoD](../06-scrum/definition-of-done.md), Sections 4–6; [Delivery Decisions](../06-scrum/delivery-decisions.md), DD-001–006 | Integrated conditional evidence, source synchronization and preparation/handoff; no Sprint commitment. |
| [Implementation Summary](../../Initial%20files/CapsuleAI_Implementation_Summary.md), proposal/official outline and Business Analysis.docx under Initial files | Historical/exploratory context. Their technology preferences, commercial/reward ideas and old thresholds do not establish current scope or constraints. |
| [Quality-attribute learning reference](../../T%C3%A0i%20li%E1%BB%87u%20tham%20kh%E1%BA%A3o%20cho%20Architecture/14-Thu%E1%BB%99c-t%C3%ADnh-ch%E1%BA%A5t-l%C6%B0%E1%BB%A3ng.md), ASR_FoodDelivery.md and ADD_FoodDelivery.md in the same reference directory | Methodology and artifact-boundary/structure reference only. No Food Delivery stack, event architecture or deployment decision is adopted. |

Technical capability claims were checked on **2026-10-08** through Context7 and the linked official documentation. Those sources explain option feasibility; they do not create product requirements, prove benchmark results, or select versions/providers.

### 3.2 Evaluation Principles and Importance

Problems arise from ASRs, cross-quality tensions and imposed constraints. Prefer sufficient simplicity; assess coherence, privacy, latency, testability and operating/change cost before adding distributed infrastructure. No weighted scoring model is defined by the sources, so matrices use qualitative source-based judgments, not invented points. Team capacity, current technical proficiency, deployment budget, image volumes and measured model/computation cost are unknown. The human's Backend Developer learning goal informs explanation, not an assumed technology preference.

| Importance | Gate meaning |
| --- | --- |
| Critical Before ADD | Resolve explicitly before an ADD baseline; the major structure/authority cannot be coherent without it. |
| Important Before ADD | Normally resolve before ADD/views. A deferral is possible only with a stable abstraction, known impact, responsible owner and resolution before dependent design. |
| Defer to Detailed Design / ADR | No pre-ADD option selection is needed; Section 6 identifies later responsibilities. |

Confidence measures strength of the recommendation from present evidence, not approval or proof of satisfaction. Even High-confidence options require correct design and validation.

### 3.3 Acceptance Boundaries Used in the Comparisons

The following preserve source meaning rather than establish new architecture targets.

| Boundary | Existing authority and interpretation |
| --- | --- |
| Complete image processing | NFR-PERF-001: p95 ≤5 seconds after image transfer/accepted analysis until both usable processed preview and reviewable proposals. Job acceptance is not completion. |
| Complete daily advice | NFR-PERF-002: p95 <3 seconds after accepted request with required context available, exactly 100 confirmed garments, eligible inputs preserved. This is not a wardrobe maximum. |
| Timing evidence | NFR-TEST-003 / SRS Section 12.3: 5 warm-ups, ≥100 measured executions per operation, legitimate slow runs retained, reference device/network/environment recorded. |
| Concurrent integrity | NFR-SCA-002: 20 active authenticated users, functional integrity. It is separate from nominal p95; no combined load/latency or mass-scale target is inferred. |
| Recovery | NFR-REL-003: core restored ≤5 minutes after a recoverable application/service restart in the agreed validation environment, accepted state intact and applicable fallback available. No production uptime/disaster-recovery/RPO commitment. |
| Exact intelligence | BRULE-MULT-004–009: complete current/expanded unique-valid sets on one relevant context/rule basis; Exact and Evaluated Zero require all prerequisites. No Multiplier latency or arbitrary assessment TTL. |
| Lifecycle and purpose | DATA-RET-001–003: immediate effective withdrawal; applicable physical deletion within 30 days; user-linked measurement maximum 90 days. Necessary minimal functional history and ranking expiry remain distinct. No personal-data AI training in MVP. |
| Mobile meaning | Android 10+/iOS 15+, Vietnamese UI, English documentation; LOC-001–005 and NFR-ACC-001/002 retain canonical meaning and integrated accessibility evidence. A backend runtime cannot alone establish mobile usability. |

### 3.4 Source Issues and Remaining Evidence

No blocking contradiction was found in current requirements. **FR-MET-004 contains an incomplete benchmark cross-reference**; explicit SRS Sections 3.4.1/8.1.1/12.3 and stable NFR/AI IDs remain the acceptance authority. **Focused UC diagram links under `use-cases/diagrams/` refer to absent files**; the master diagram, 23 textual specifications and 12 activities supply available interaction evidence. No missing content is inferred or repaired.

No current root README, legacy standalone PRD or older referenced Food Delivery BRD was available; current canonical CapsuleAI documents and available architecture references were used. Historical “option B”/stack suggestions are not current selected decisions. Older artifact-specific next-step statements preserve their original handoff; the current reusable order and developer handoff are maintained in Workflow Section 46, DD-006 and ASR Section 13.

## 4. Decision Summary

All 14 entries retain the full status shown below. Persistence model and technology are one problem; avoiding a dedicated cache/broker is a positive architecture recommendation, not an omitted analysis.

| ID | Decision Problem | Importance | Recommendation | Confidence | Status |
| --- | --- | --- | --- | --- | --- |
| ADA-001 | Business-core architecture style | Critical Before ADD | Modular monolith for the business core | High | Proposed — Awaiting Selection |
| ADA-002 | Backend application runtime | Important Before ADD | Java with Spring Boot for the business application | Medium | Proposed — Awaiting Selection |
| ADA-003 | Authoritative persistence model and database | Critical Before ADD | One relational primary database: PostgreSQL | High | Proposed — Awaiting Selection |
| ADA-004 | AI/CV execution boundary | Critical Before ADD | Separate private Python analysis runtime | Medium | Proposed — Awaiting Selection |
| ADA-005 | Backend-to-AI interaction | Important Before ADD | Bounded synchronous complete-result interaction | Medium | Proposed — Awaiting Selection |
| ADA-006 | External integration boundaries | Important Before ADD | Narrow explicit ports/adapters for real dependencies | High | Proposed — Awaiting Selection |
| ADA-007 | Private image persistence and access | Important Before ADD | Private object storage with application-controlled authorized delivery | Medium | Proposed — Awaiting Selection |
| ADA-008 | Session, refresh and reset authority | Critical Before ADD | Primary database as persistent session/reset authority | High | Proposed — Awaiting Selection |
| ADA-009 | Logical-action idempotency and accepted effects | Critical Before ADD | Database-backed logical-action coordination and coherent effects | High | Proposed — Awaiting Selection |
| ADA-010 | Advice and wardrobe-intelligence computation | Critical Before ADD | Request-time evaluation from authoritative inputs with shared semantics | Medium | Proposed — Awaiting Selection |
| ADA-011 | Dedicated cache strategy | Important Before ADD | No dedicated cache initially | High | Proposed — Awaiting Selection |
| ADA-012 | Messaging and background coordination | Important Before ADD | Explicit core coordination with no broker initially | High | Proposed — Awaiting Selection |
| ADA-013 | Initial deployment and resource separation | Important Before ADD | Small controlled deployment with supervised core/AI processes and durable resources | Medium | Proposed — Awaiting Selection |
| ADA-014 | Protected observability and validation evidence | Important Before ADD | Minimal protected structured logs, metrics and health/recovery evidence | High | Proposed — Awaiting Selection |

## 5. Major Architecture Decision Analyses

### ADA-001 — Business-core architecture style

#### Problem

How should the business core coordinate private access, confirmed wardrobe state, reported behavior and exact intelligence without making each accepted change a distributed consistency problem?

#### Why This Decision Is Needed Now

**Critical Before ADD.** ADD needs a responsibility and coordination direction for the complete MVP, including the later Coverage and Multiplier capabilities. The eight current stories do not define the architecture horizon.

#### Related ASRs

ASR-REL-001, ASR-CON-001, ASR-SEC-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Conceptual Integrity and Testability — High; Maintainability and Scalability — Medium; Reusability — Low.

#### Relevant Constraints

SRS DATA-INT-001–004, FR-WEAR-009/010, FR-PERS-013 and NFR-SCA-002; BRULE-WEAR-002 and BRULE-MULT-004–009. No independent business-service scaling, organizational ownership split or cross-product reuse obligation is established.

#### Evaluation Criteria

Coherent accepted changes; consistent validity/identity; testable responsibilities; manageable recovery; justified operational complexity. These qualitative criteria carry no numerical weights.

#### Option A — Modular monolith for the business core

Keep the business application together with explicit internal responsibility boundaries. This supports local coordination and one shared interpretation of domain rules while allowing focused tests and changes. It risks becoming a conventional monolith if boundaries are unenforced; Developers must maintain dependency discipline. Business-core deployment remains coupled. A separately executed AI capability can coexist with this option without distributing authoritative business state.

#### Option B — Conventional layered monolith

Organize primarily by presentation, application and persistence layers. It offers a simple deployment and local transactions with familiar development practices. Cross-capability rules can become scattered through broad services and layers, increasing change/test costs and risking divergent assessment meaning. It can satisfy the ASRs with discipline; its disadvantage is weaker visibility of domain responsibilities, not an inherent correctness failure.

#### Option C — Microservices for business capabilities

Use independently deployed business services with explicit contracts. Independent evolution and resource allocation can be valuable when actual ownership/scale demands emerge. Here Wear effects, revocation and changed assessment bases would cross more boundaries, requiring failure handling, coordination and operating evidence. A 20-user integrity scenario alone does not justify this cost; distributed partial acceptance creates additional ASR-REL-001 risk.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Accepted-state coordination | Strong: predominantly local | Strong: local, broad responsibilities | More difficult across services |
| Domain/change visibility | Good with enforced boundaries | Moderate; easier scattering | Good boundaries, harder shared semantics |
| MVP operation/recovery | Good: small core footprint | Good: small footprint | Higher deployment and diagnosis cost |

#### Recommendation

Option A — Modular monolith for the business core

#### Why Recommended

It preserves local consistency while making the full value loop's responsibilities visible. The need for consistent domain meaning justifies internal structure; independent business services lack a current driver.

#### Recommendation Confidence

High — the integrity pressures and absence of independent business-service requirements support this direction. Exact responsibility boundaries remain a design question.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would describe one coordinated business core with explicit responsibilities, plus any separately selected analysis boundary. Releases of the core would remain coupled and boundary discipline would become ongoing work.

#### Deferred Details

Module/component maps, internal interfaces, layering conventions and dependency enforcement belong in ADD and Logical/Implementation Views; consequential rationale belongs in later ADRs.

### ADA-002 — Backend application runtime

#### Problem

Which application runtime can support the core's authorization, coordinated mutations, deterministic rules and protected evidence with acceptable development and operating effort?

#### Why This Decision Is Needed Now

**Important Before ADD.** A runtime direction normally informs ADD and the Implementation View. The historical Java/Spring Boot suggestion warrants comparison, but current sources do not mandate it or establish the team's proficiency.

#### Related ASRs

ASR-SEC-001, ASR-REL-001, ASR-CON-001, ASR-PERF-001, ASR-TEST-001.

#### Relevant Quality Attributes

Security and Reliability — Critical; Conceptual Integrity, Performance and Testability — High; Maintainability and Manageability — Medium.

#### Relevant Constraints

SRS Section 3.1.1 mandates JWT/session policy, not a framework; NFR-PERF-001/002 retain complete-operation timing. ASR Section 7 explicitly leaves Java/Spring Boot and Python open.

#### Evaluation Criteria

Clarity of domain/transaction responsibilities; usable security and persistence integration; reproducible testing/operation; measured performance; contributor experience and maintenance effort. No experience, staffing or budget advantage is assumed.

#### Option A — Java with Spring Boot for the business application

Offers established SQL integration and operational instrumentation; Spring Security can validate JWTs. This is useful for transaction-oriented business work and explicit contracts. The framework does not automatically enforce CapsuleAI's usable-session/revocation or ownership policies. A Java core plus Python analysis adds a second runtime and learning burden; familiarity and measured end-to-end performance must be reviewed. These capabilities are documented in the [Spring Boot SQL reference](https://docs.spring.io/spring-boot/reference/data/sql.html), [Actuator reference](https://docs.spring.io/spring-boot/reference/actuator/index.html) and [Spring Security JWT reference](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

#### Option B — Python for the business application

Can support the same domain and persistence obligations and may reduce language boundaries if analysis is also Python. It can be a strong choice with relevant team experience. Authorization, transactional coordination, dependency isolation and disciplined contracts still need explicit realization; sharing a language does not require sharing one process. No specific Python framework is evaluated or selected here, and no intrinsic performance inferiority is claimed.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B |
| --- | --- | --- |
| Core integration direction | Good: established SQL/security/operating support | Good, conditional on framework/integration choice |
| Language footprint with Python AI | Two runtimes | Potentially one language; process separation remains possible |
| Team suitability | Unknown; historical candidate only | Unknown; no current proficiency evidence |
| Latency fit | Requires actual complete-operation evidence | Requires actual complete-operation evidence |

#### Recommendation

Option A — Java with Spring Boot for the business application

#### Why Recommended

It is a credible transaction-oriented starting point for the proposed core and gives the Backend Developer a concrete runtime to review. This recommendation is conditional on feasibility and learning cost; historical mention is context rather than selection authority.

#### Recommendation Confidence

Medium — current team experience, runtime versions and representative benchmarks are unknown. Strong Python experience or unsuitable Java/AI operating cost could support selecting Option B.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would inherit a Java core and explicit AI/runtime integration responsibilities. Developers would need application-level usable-session checks, domain tests and operating discipline beyond framework defaults.

#### Deferred Details

Supported framework/JDK versions, dependencies, persistence access library, security configuration, package layout and build tooling follow selection in the Implementation View and engineering baseline.

### ADA-003 — Authoritative persistence model and database

#### Problem

How should account/session, wardrobe, Wear and assessment-related information be persisted so accepted multi-object changes and lifecycle obligations remain coherent and queryable?

#### Why This Decision Is Needed Now

**Critical Before ADD.** Persistence direction determines the local coordination available to ADD. Model and database are compared together because separate analyses would duplicate the same integrity trade-off.

#### Related ASRs

ASR-REL-001, ASR-SEC-001, ASR-SEC-002, ASR-CON-001, ASR-REL-002.

#### Relevant Quality Attributes

Reliability and Security — Critical; Conceptual Integrity and Testability — High; Maintainability, Scalability and Manageability — Medium.

#### Relevant Constraints

SRS DATA-AUTH-005, DATA-WEAR-002–004, DATA-INT-001–004 and DATA-RET-001–003; BRULE-PERS-005/006 and BRULE-HIST-002/003. Rich optional garment descriptors do not mandate a document database; no current multiple-database requirement exists.

#### Evaluation Criteria

Transactional acceptance across related state; ownership/uniqueness enforcement; history and survivor queries; purpose/retention separation; recovery and operational burden; flexibility without inconsistent meanings.

#### Option A — One relational primary database: PostgreSQL

Provides local transactions for related authoritative state and relational queries/constraints useful for ownership, unique account identity and surviving reports. JSON support can accommodate justified variable information without a second database. Application transaction/isolation design is still essential; PostgreSQL does not automatically solve all concurrent business invariants. Schema evolution and disciplined modeling are costs. See [application consistency](https://www.postgresql.org/docs/18/applevel-consistency.html) and [JSON data support](https://www.postgresql.org/docs/18/datatype-json.html); these documentation versions do not select a project database version.

#### Option B — One document primary database: MongoDB

Can model rich garment information naturally and supports multi-document transactions in supported replica-set or sharded configurations. It is a credible integrity-capable alternative. Related session, event and survivor state still requires coordinated modeling/transactions; it cannot safely be assumed to fit one document or be atomic through unrelated writes. Required deployment configuration and cross-document queries add considerations. See [MongoDB transactions](https://www.mongodb.com/docs/manual/core/transactions) and [transaction deployment considerations](https://www.mongodb.com/docs/manual/core/transactions-production-consideration).

#### Option C — Relational plus document authoritative stores

Allows specialized storage of different information shapes. It also adds two recovery/lifecycle surfaces and coordination across stores when accepted edits/removals affect consumers. No demonstrated need offsets duplicated operating work or cross-store acceptance risks. It could be justified later by a specific workload that one store cannot handle, rather than by AI or flexible descriptors alone.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Related acceptance | Strong: local transaction direction | Good: explicit multi-document coordination | Higher cross-store coordination risk |
| Variable garment information | Good: relational plus justified JSON | Strong document fit | Strong specialization, extra ownership complexity |
| Survivor/lifecycle reasoning | Good in one authority | Good with deliberate query/model design | More complex across authorities |
| MVP operating footprint | One primary store | One primary store, transaction-capable setup | Two store families |

#### Recommendation

Option A — One relational primary database: PostgreSQL

#### Why Recommended

The strongest pressures concern related authoritative state and its coherent effects, rather than unconstrained document shape. One relational authority offers a clear coordination and query direction while retaining room for justified flexible fields.

#### Recommendation Confidence

High — the source-derived consistency needs strongly support one relational authority. Actual schema, query performance and recovery behavior still require design and verification.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would use a single primary authority for related business/session state, with separate media storage if ADA-007 is selected. Cross-resource image work would need explicit recoverable coordination; database transactions would not make object-storage operations atomic.

#### Deferred Details

Tables, indexes, SQL types, ORM, transaction isolation/locking, migration strategy, hosting/version and backup/restore realization belong in Data/Deployment Views, detailed design and ADRs.

### ADA-004 — AI/CV execution boundary

#### Problem

Where should image/garment analysis execute so model dependencies and failure remain bounded while confirmation and private business authority stay protected?

#### Why This Decision Is Needed Now

**Critical Before ADD.** ADD must distinguish the analysis responsibility from authoritative wardrobe acceptance and account for its resource/failure boundary. Python is a candidate supported by historical exploration, not a current requirement.

#### Related ASRs

ASR-REL-001, ASR-SEC-002, ASR-PERF-001, ASR-INT-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Performance, Interoperability and Testability — High; Availability and Manageability — Medium.

#### Relevant Constraints

SRS SI-001, AI-REQ-001–003/007/010/011/014, NFR-PRIV-002 and NFR-REL-001; UC-007/AD-007. Both usable preview and reviewable proposals complete processing; manual saving and confirmed authority remain independent of AI success.

#### Evaluation Criteria

Compatible model execution; bounded resource/failure effects; complete-result latency; private necessary-purpose processing; independent model validation; deployment/contract cost.

#### Option A — Separate private Python analysis runtime

Keep model execution in a private analysis process and the business application's confirmed-state authority outside it. This isolates dependency/version changes and provides a place for model validation. It adds communication, process supervision and private image-access coordination; two runtimes do not guarantee faster results or eliminate a shared host's resource/failure risks. Analysis would provide proposals and previews rather than directly mutate authoritative user state.

#### Option B — In-process analysis in the business runtime

Avoids a process hop and can simplify deployment when selected models genuinely support the core runtime. Model loading/resource contention and failures become more tightly coupled to core availability; compatible inference packaging and maintenance must be demonstrated. This remains credible if the actual model toolchain fits, rather than being rejected because historical work mentioned Python.

#### Option C — Managed external analysis service

Can reduce local model hosting work and provide managed capacity. It introduces vendor behavior, network, cost and disclosure dependencies. Model/output suitability and complete-result latency remain uncertain; contractual data use/retention must satisfy no personal-data training and necessary-purpose limits. Generic fashion labels or background removal alone cannot establish required combined quality.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Model dependency isolation | Good process boundary | Weaker shared runtime boundary | Good locally, vendor dependency |
| Private-data control | Good with controlled processing/access | Good with controlled core processing | Conditional on provider terms/behavior |
| Core operation during AI failure | Good with independent manual path | Needs careful resource containment | Good fallback, external failure exposure |
| MVP complexity | Moderate: two runtimes | Lower if model compatibility proven | Moderate: vendor assurance and integration |

#### Recommendation

Option A — Separate private Python analysis runtime

#### Why Recommended

It offers a practical separation between model evolution and business authority while preserving manual continuity. Its cost is justified provisionally by model dependency/resource differences, not by a demand for microservices.

#### Recommendation Confidence

Medium — no current model/package compatibility, licensing, compute sizing or full processing benchmark proves this boundary. A compatible in-process model or assured managed provider could change the trade-off.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would inherit a private analysis boundary, purpose-minimal inputs, no independent authoritative business-state writes and explicit failure/operating responsibility. Hosting separation would still be decided through ADA-013.

#### Deferred Details

Models and licenses, inference framework, CPU/GPU need, service contract, transport/security realization, image-access method and model-version management follow ADD/views and detailed contract design.

### ADA-005 — Backend-to-AI interaction

#### Problem

How should accepted image analysis deliver the complete review result within its actual timing boundary while exposing pending, failed and uncertain outcomes truthfully?

#### Why This Decision Is Needed Now

**Important Before ADD.** Interaction direction affects ADD runtime reasoning if ADA-004 is selected. Accepting or queuing work is not completion and must not be presented as satisfaction of NFR-PERF-001.

#### Related ASRs

ASR-PERF-001, ASR-INT-001, ASR-REL-001, ASR-USE-001, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability — Critical; Performance, Interoperability, Usability and Testability — High; Availability and Manageability — Medium.

#### Relevant Constraints

NFR-PERF-001: p95 ≤5 seconds from accepted post-transfer analysis until usable preview AND reviewable proposals, under SRS Sections 2.3/12.3; 5 warm-ups and ≥100 measured runs. FR-AI-003/005/009/010 and UC-007 preserve manual, draft and uncertain outcomes.

#### Evaluation Criteria

End-to-end completion latency including coordination/wait; failure isolation; resource control; truthful pending/completion states; cancellation/late-result authority; operational and client-state complexity.

#### Option A — Bounded synchronous complete-result interaction

Request analysis and await its complete usable result through a bounded interaction. It has a clear completion boundary and fewer job/status states to reconcile. Slow work can consume request resources and uncertain communication needs recovery; bounded resource use and measured model capacity are essential. Timeout does not mean successful processing or prevent manual entry. The SRS p95 is a validation target, not a universal hard timeout selected here.

#### Option B — Asynchronous durable job interaction

Accept work, execute later and make completed results available through job state. It can aid durable resumption and workload control, but adds job ownership, cancellation, result retention and retrieval coordination. Queue wait and retrieval through availability of both outputs still count toward the same ≤5-second measure. A quick acceptance response is not a substitute; no sustained long-running-analysis requirement currently justifies the extra states.

#### Option C — Bounded hybrid interaction

Attempt immediate completion while retaining a durable pending-job route for qualifying work. It combines responsiveness and possible resumption but inherits both interaction models and their ownership/late-result problems. It is credible when evidence shows interruption or resource patterns needing continuation; that evidence is not currently available.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Completion measure clarity | Strong: await both outputs | Good if queue/retrieval included | Good with explicit shared boundary |
| Slow-work control | Requires bounded resources | Good scheduling; wait still counts | Good potential, more policies |
| Pending/result-state cost | Lower | Higher | Highest |
| Current justification | Good initial fit | Unproven durable-job need | Unproven combined need |

#### Recommendation

Option A — Bounded synchronous complete-result interaction

#### Why Recommended

The current short complete-result requirement favors a direct bounded path before adding durable analysis jobs. Feasibility must be established with real processing, network and resource evidence; protocol choice alone proves nothing.

#### Recommendation Confidence

Medium — model duration, interruption frequency and resource contention are unmeasured. If a durable-job model is later selected, it must preserve the same full-result benchmark.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would describe bounded waiting, pending/failure/manual continuation and safe handling of late output. Neither automated completion nor retry would overwrite confirmed data.

#### Deferred Details

Transport, endpoint/payload shapes, numeric dependency timeouts, connection/resource limits, retry/cancellation policies and detailed runtime sequences follow views/contracts; benchmark execution follows Test Strategy.

### ADA-006 — External integration boundaries

#### Problem

How can weather, recovery delivery, AI and optional commerce retain CapsuleAI's meaning and minimal disclosure despite provider-specific behavior?

#### Why This Decision Is Needed Now

**Important Before ADD.** ADD needs a stable boundary between business outcomes and dependency details. Missing weather, failed delivery and unavailable navigation have different consequences.

#### Related ASRs

ASR-INT-001, ASR-SEC-001, ASR-SEC-002, ASR-USE-001, ASR-TEST-001.

#### Relevant Quality Attributes

Security — Critical; Interoperability, Usability and Testability — High; Availability, Maintainability and Flexibility — Medium.

#### Relevant Constraints

SI-001–004 and COM-003; weather age ≤30 minutes, external-request assessment delay ≤2 seconds; optional commercial source/time and inclusive 24-hour currentness; UC-004/006/023 and AD-004/006. No retailer partnership, transaction or new acquisition goal is mandated.

#### Evaluation Criteria

Preserved outcome/provenance/time meaning; purpose-minimal exchange; bounded fallback; controlled provider substitution and test doubles; modest implementation effort.

#### Option A — Narrow explicit ports/adapters for real dependencies

Let application responsibilities depend on CapsuleAI outcome semantics and isolate provider translation in small adapters. Provider SDKs can still be used inside adapters. This supports tests for bounded failure and minimal disclosure without spreading provider terms. The cost is maintaining a small contract/translation layer; avoid universal plugin frameworks or speculative providers.

#### Option B — Direct provider SDK use throughout application logic

Reduces initial wrappers and may suit a tiny single integration. Here provider response/error/timestamp semantics would become dispersed across business paths, making consistent fallback and minimal disclosure harder to review. It can satisfy requirements with careful tests, but duplicated policies and provider replacement have greater maintenance risk.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B |
| --- | --- | --- |
| Outcome/currentness consistency | Strong: one translation boundary | Moderate: discipline across call sites |
| Failure/privacy tests | Good: focused contracts and fakes | More coupled to provider behavior |
| Initial code surface | Small extra adapters | Lower initially |
| Change cost | Contained at actual dependencies | Broader application impact |

#### Recommendation

Option A — Narrow explicit ports/adapters for real dependencies

#### Why Recommended

The explicit outcome and disclosure policies justify a modest integration seam. It serves existing integrations rather than hypothetical extensibility.

#### Recommendation Confidence

High — the boundaries are concrete and shared across current journeys.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would separate provider translation from business outcome decisions. UC-006 would remain location/weather acquisition authority; consuming assessments would not become new independent external acquisition goals. SDK use would remain possible within an adapter.

#### Deferred Details

Interface signatures, chosen providers/SDKs, payloads, exception mappings, contract fixtures and retry configuration follow Implementation View and detailed API/service design.

### ADA-007 — Private image persistence and access

#### Problem

Where should drafts, garment images and usable previews reside so private access, accepted-state recovery and physical deletion can be enforced without burdening every business-state operation?

#### Why This Decision Is Needed Now

**Important Before ADD.** ADD needs a media persistence/access direction and its relationship to primary authority. Image ownership cannot be inferred from a storage object's existence or a long-lived download link.

#### Related ASRs

ASR-SEC-001, ASR-SEC-002, ASR-REL-001, ASR-REL-002, ASR-PERF-001.

#### Relevant Quality Attributes

Security and Reliability — Critical; Performance and Interoperability — High; Manageability and Supportability — Medium.

#### Relevant Constraints

SRS DATA-GAR-004, DATA-HIST-001, DATA-RET-002, NFR-SEC-001/002 and Section 3.4.1; manual garments need no image. Removed original images/nonessential data require physical deletion within 30 days; protected content cannot remain exposed through logout/account switching.

#### Evaluation Criteria

Private authorized delivery/revocation; recoverable association with confirmed state; deletion of applicable copies; image workload separation; operating cost and orphan cleanup.

#### Option A — Private object storage with application-controlled authorized delivery

Separates media volume from transactional business data and allows explicit image lifecycle handling. It adds a storage/access dependency and recoverable coordination for draft, confirmation and removal; object upload is not atomic with a primary-database transaction. Provider/versioning behavior matters. For example, S3 presigned URLs are bearer access reusable until expiry, so logout does not automatically revoke them; versioned deletion can leave prior data behind. An authorized delivery design must satisfy effective access rules, and physical purge must cover applicable versions/copies. See [presigned URL behavior](https://docs.aws.amazon.com/AmazonS3/latest/userguide/using-presigned-url.html) and [versioned deletion](https://docs.aws.amazon.com/AmazonS3/latest/userguide/DeletingObjectVersions.html). AWS is an example, not a selected provider.

#### Option B — Images as blobs in the primary database

Can simplify association and local transactional coordination of image data with state. It concentrates image I/O, storage growth and recovery work in the primary authority, potentially increasing contention and maintenance burden. It is credible for small measured volumes; security/deletion still require application policy and inspection of retained copies.

#### Option C — Durable private filesystem

Can keep initial media handling inexpensive and simple on a controlled environment. Durable placement, authorized delivery, cleanup, restore and later relocation become team responsibilities; ephemeral runtime disk is unsuitable for accepted media. It can work if operating needs are demonstrated, but host coupling and lifecycle evidence require care.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Primary-state/image coordination | Explicit recoverable cross-resource work | Strong local coordination potential | Explicit DB/file coordination |
| Media workload separation | Good | Limited | Good, host-coupled |
| Lifecycle/access correctness | Needs application and storage policy | Needs application and database policy | Needs application and filesystem policy |
| Operating burden | Provider dependency and copy/version control | Primary capacity/recovery burden | Team-managed durability and cleanup |

#### Recommendation

Option A — Private object storage with application-controlled authorized delivery

#### Why Recommended

It separates media work from authoritative transactions while making access and deletion responsibilities explicit. Convenience links or logical deletion alone cannot satisfy the private lifecycle.

#### Recommendation Confidence

Medium — expected image volume, provider cost and the authorized-delivery realization remain unknown. The selection review must account for revocation and copy/version purge, not only storage pricing.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would inherit a separate private media resource and recoverable association/cleanup responsibility. Access would follow current account/session authority; simple unrevocable bearer links would not by themselves establish conformance.

#### Deferred Details

Provider, buckets/paths, versioning, upload/delivery realization, draft retention, cleanup scheduling and storage security configuration belong in views and detailed design; no new retention number is set.

### ADA-008 — Session, refresh and reset authority

#### Problem

Where should usable session and single-use recovery authority live so logout, refresh reuse, reset replacement and completed reset remain effective across concurrent requests and restart?

#### Why This Decision Is Needed Now

**Critical Before ADD.** ADD cannot describe protected access coherently without an authority/coordination direction. JWT validation alone cannot enforce all current session policies.

#### Related ASRs

ASR-SEC-001, ASR-REL-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Security and Reliability — Critical; Testability and Interoperability — High; Manageability and Scalability — Medium.

#### Relevant Constraints

SRS Section 3.1.1, FR-AUTH-004/007/009/010/013/014/017/018 and DATA-AUTH-005; BRULE-AUTH-004–006. Access lasts 15 minutes, session maximum 30 days; reset lasts 30 minutes and permits one success. Reset revokes all account Refresh Sessions; logout/reuse target their specified session.

#### Evaluation Criteria

Atomic consumption/rotation/replacement; coordinated password and revocation change; correct protected access after revocation; restart durability; failure-safe authorization; operational footprint.

#### Option A — Primary database as persistent session/reset authority

Keep related account, session and reset validity in the proposed primary authority. Local coordination can support single-success consumption and revocation with credential changes. Protected operations still need a valid JWT and usable authorized session/account context; the database does not provide that check automatically. This adds authoritative access lookups and requires careful concurrent transition design, but avoids another correctness-critical store.

#### Option B — Dedicated durable Redis authority

Can provide low-latency state access and coordinated operations when deliberately configured. Redis supports persistence; it is not inherently memory-only. Durability, eviction, replica/failover behavior and password-state coordination must be designed and verified. An evictable cache configuration is inappropriate as the sole unexplained source of session truth, and a second authority complicates reset consistency. See [Redis persistence](https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/) and [eviction behavior](https://redis.io/docs/latest/develop/reference/eviction/).

#### Option C — Primary authority plus Redis acceleration

Retains durable state while potentially reducing repeated reads. It introduces invalidation/version coordination: stale authorization after logout/reuse/reset cannot be accepted merely until cache or JWT expiry. No measured authority-read bottleneck currently offsets the extra correctness and operating cost.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Credential/session/reset coherence | Strong local direction | Requires cross-store coordination | Strong authority, harder cache coherence |
| Restart durability | Good with verified DB operation | Conditional on explicit persistence/configuration | Good primary authority |
| Revocation on protected use | Explicit authoritative check required | Explicit authoritative check required | Freshness/invalidation proof required |
| Initial operating footprint | One primary authority | Additional critical state service | Additional cache plus coordination |

#### Recommendation

Option A — Primary database as persistent session/reset authority

#### Why Recommended

It aligns related sensitive transitions with the proposed transactional authority. Current requirements favor correctness and recovery over unproven lookup acceleration.

#### Recommendation Confidence

High — clear state-coordination needs support one persistent authority; lookup cost must still be measured.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would coordinate account/reset/session transitions in one authority and require usable authority for protected access. It would not rely on offline JWT signature validity until token expiry after session revocation.

#### Deferred Details

Credential protection, token signing/keys, storage representation, locking/isolation, session-check implementation, reset-delivery coordination and detailed endpoint contracts follow design.

### ADA-009 — Logical-action idempotency and accepted effects

#### Problem

How should one logical mutation survive retry or uncertain communication while separate Wear intentions and their current effects remain distinct and coherent?

#### Why This Decision Is Needed Now

**Critical Before ADD.** Acceptance boundaries influence ADD across garment confirmation, Wear repair and access/recovery, not just endpoint implementation. Retrying after restart cannot depend solely on an in-memory record.

#### Related ASRs

ASR-REL-001, ASR-SEC-002, ASR-CON-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Conceptual Integrity and Testability — High; Scalability and Maintainability — Medium.

#### Relevant Constraints

DATA-INT-004, DATA-WEAR-003/004, FR-WEAR-009/010 and FR-PERS-013; BRULE-WEAR-002–006 and BRULE-PERS-005/006; UC-007/014/016/017 and AD-007/014/016/017. User + outfit + day is not logical-action identity.

#### Evaluation Criteria

Durable linkage between intention and accepted outcome; coherent current consumers; intentional repetition; concurrent retries/restarts; truthful uncertainty; external side-effect recovery.

#### Option A — Database-backed logical-action coordination and coherent effects

Coordinate durable action identity/outcome with authoritative mutations in the primary store. Subsequent consumers must see the accepted truth and survivor effects, either through coherent derived reads or coordinated maintained state. This supports retry review without collapsing explicit repeats. It adds concurrency/transaction design and necessary evidence. External media/email effects require recoverable coordination rather than a claim of one transaction across all resources.

#### Option B — Application-memory or cache-only duplicate tracking

Can recognize immediate repetitions cheaply. Process restart, eviction, expiry or a missed link between tracking and actual acceptance can cause duplicates or false rejection. It is insufficient as sole authority for the required recoverable logical action; persistent safeguards would effectively move it toward Option A.

#### Option C — Asynchronous events with independently updated authoritative projections

Can decouple downstream work but creates intervals when accepted removals/corrections have not reached current advice or behavioral evidence. Durable ordering, replay, duplicate protection and visibility gates would be needed to satisfy immediate/coherent effects. A broker alone does not solve those obligations; the current MVP supplies no compensating need for this complexity.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Retry after restart | Strong durable direction | Weak as sole authority | Possible with durable identity/replay design |
| Current accepted effects | Good: coherent reads/updates | Not established by deduplication | Needs projection visibility gates |
| Separate intentional repeats | Explicit intention semantics | Risky if short-cut keying | Explicit intention semantics still needed |
| Implementation/operation cost | Moderate, local coordination | Low initially, correctness gaps | High coordination and evidence burden |

#### Recommendation

Option A — Database-backed logical-action coordination and coherent effects

#### Why Recommended

The durable acceptance link addresses the actual problem. It preserves individual reports, current exclusions and survivor recalculation without requiring event sourcing or distributed projections.

#### Recommendation Confidence

High — the relevant failure and retry flows directly support durable coordination.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would make accepted identity/outcome and coherent consumer state explicit. A new same-outfit/day report would remain a separate event; normalization would cap only ranking contribution using the latest surviving accepted absolute anchor.

#### Deferred Details

Client/action identity mechanism, request fields, reuse conflicts, transaction boundaries, locks, representation/retention of retry evidence and external-side-effect recovery protocols follow detailed design.

### ADA-010 — Advice and wardrobe-intelligence computation

#### Problem

How should Daily/Shuffle, Coverage/Gaps and candidate utility execute from a defensible current basis while preserving shared validity and complete exact evaluation?

#### Why This Decision Is Needed Now

**Critical Before ADD.** ADD needs a computation/currentness direction for CapsuleAI's differentiating capabilities. A stored result or top-ranked daily subset does not establish an exact Multiplier.

#### Related ASRs

ASR-CON-001, ASR-REL-001, ASR-PERF-001, ASR-USE-001, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability — Critical; Conceptual Integrity, Performance, Usability and Testability — High; Scalability and Maintainability — Medium.

#### Relevant Constraints

FR-OUT-003/007/009/014–017, FR-ANL-011–015, FR-MULT-002–008 and DATA-ANL-004; BRULE-COV-003–006 and BRULE-MULT-001–009. Daily p95 <3 seconds uses exactly 100 confirmed garments; no Multiplier latency, arbitrary assessment TTL or wardrobe maximum is established.

#### Evaluation Criteria

One validity/identity meaning; coherent basis despite concurrent change; completion evidence for exact claims; honest non-exact outcomes; measured computation cost; maintainable change propagation.

#### Option A — Request-time evaluation from authoritative inputs with shared semantics

Evaluate current inputs through consistent validity/identity responsibilities and establish completion/basis before claiming a result. It reduces stored-result invalidation surfaces and keeps evidence close to the request. Computation can be expensive, especially full hypothetical sets, and concurrent changes still require basis validation. Shared semantics does not mean Daily and Multiplier must use the same algorithm or compute every possible outfit in the Daily path.

#### Option B — Broad precomputation of advice and intelligence

Can reduce read-time work when results are ready. Changes in garments, needs, weather, candidate information and rules require correct invalidation/recomputation; hypothetical candidate space increases cost. In-progress rebuilds cannot remain silently current. It is credible for measured recurring workloads but adds considerable materialization and scheduling responsibility before such a workload is established.

#### Option C — Hybrid current computation plus basis-validated derived reuse

Can reuse expensive valid work while validating its relevant basis and recomputing changed parts. It may be the eventual performance choice. It adds completeness/currentness bookkeeping, invalidation and cache-isolation tests; stored evidence must establish full sets where exactness requires them. A fixed TTL cannot substitute for changed-input/rule detection.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Current-basis reasoning | Good; explicit concurrent-change check | More invalidation/rebuild work | Good if every reused basis is validated |
| Complete exact evidence | Good if full evaluation completes | Good only with complete current materialization | Good only with complete validated evidence |
| Read-time cost | Potentially higher; benchmark needed | Potentially lower; rebuild cost shifts | Potentially lower; bookkeeping cost |
| Initial complexity | Lower | Higher | Moderate to higher |

#### Recommendation

Option A — Request-time evaluation from authoritative inputs with shared semantics

#### Why Recommended

It is the clearest baseline before measured cost justifies reuse/precomputation. Validity precedes ranking; full same-basis current/expanded identity sets support exact Multiplier claims. Incomplete or unavailable work cannot become +0.

#### Recommendation Confidence

Medium — combinatorial cost and the daily benchmark are not yet measured. If profiling demands Option C, selection must include basis/completeness safeguards rather than truncation.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would define coherent evaluated inputs, shared semantic responsibility and truthful Exact/Evaluated Zero/Incomplete/Unavailable/Outdated outcomes. Coverage insufficiency would remain distinct from zero, and Gap would remain an underserved capability rather than a compulsory product.

#### Deferred Details

Enumeration/ranking algorithms, optimization, basis/version realization, derived-state representation, result storage, scheduling and contract shapes follow ADD/views and detailed design.

### ADA-011 — Dedicated cache strategy

#### Problem

Does the initial architecture need another cache resource to satisfy current timing without undermining user isolation, revocation or input/rule-based currentness?

#### Why This Decision Is Needed Now

**Important Before ADD.** ADD should distinguish required authoritative state from optional acceleration. Historical Redis suggestions and the 20-user integrity workload are not evidence of a cache bottleneck.

#### Related ASRs

ASR-PERF-001, ASR-CON-001, ASR-SEC-001, ASR-SEC-002, ASR-REL-001.

#### Relevant Quality Attributes

Security and Reliability — Critical; Performance and Conceptual Integrity — High; Scalability, Manageability and Maintainability — Medium.

#### Relevant Constraints

NFR-PERF-002, NFR-SCA-002, FR-WEATHER-003–005, FR-MULT-008 and DATA-RET-001–003. Assessment currentness is relevant-input/rule based; permitted weather evidence reuse has its own inclusive 30-minute boundary.

#### Evaluation Criteria

Demonstrated expensive repeated work; safe authority/currentness; account isolation and deletion; recoverability; invalidation and operating cost.

#### Option A — No dedicated cache initially

Use the authoritative store and direct computation first, then profile representative complete operations. This avoids another invalidation/retention surface. It does not prohibit provider-evidence reuse under its existing freshness rule, ordinary database mechanisms or later justified optimization. The risk is insufficient measured performance; this direction must be revisited if the actual benchmark fails.

#### Option B — Application-local derived cache

Can accelerate repeated work without a separate service. Account/basis isolation, bounded private-data lifecycle and restart behavior still need design; multiple application instances can hold inconsistent copies. It cannot become sole session or accepted-action authority.

#### Option C — Redis distributed cache

Can support shared acceleration across processes when a real repeated workload exists. It adds deployment, eviction, authorization-currentness and private-copy lifecycle work. Configured persistence does not make derived cached data authoritative or automatically coherent. No measured shared-cache need is currently present.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Initial correctness surface | Smallest | Extra local invalidation/copy surface | Extra distributed invalidation/copy surface |
| Acceleration opportunity | Unproven until profile | Good for local repeated work | Good for shared repeated work |
| Operating cost | Lowest additional footprint | Low service cost, memory management | Additional service/configuration |
| Current justification | Good | Conditional on measured reuse | Weak without shared bottleneck |

#### Recommendation

Option A — No dedicated cache initially

#### Why Recommended

Current sources specify outcomes, not a cache. Starting without one makes it easier to determine which work needs acceleration without sacrificing exactness or revocation.

#### Recommendation Confidence

High — no current evidence requires a dedicated cache; confidence is in this initial direction, not a claim that performance already passes.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would not depend on Redis for the initial authority or evaluation path. Reconsideration would require measured benefit and explicit ownership/basis/invalidation/lifecycle safeguards.

#### Deferred Details

Cache keys, eviction, capacity, derived reuse strategy and optional cache technology/configuration belong to later evidence-driven design and ADR.

### ADA-012 — Messaging and background coordination

#### Problem

Does coordinating accepted changes, optional measurement and required cleanup need an event broker, or can a simpler mechanism preserve the required outcomes?

#### Why This Decision Is Needed Now

**Important Before ADD.** ADD must identify synchronous acceptance versus recoverable later work without assuming that all effects should be messages. No broker does not mean retention/deletion work may be lost after restart.

#### Related ASRs

ASR-REL-001, ASR-SEC-002, ASR-INT-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Interoperability and Testability — High; Manageability, Maintainability and Availability — Medium.

#### Relevant Constraints

FR-MET-007, DATA-RET-001–003, DATA-INT-004 and BRULE-PERS-006; applicable 30-day physical deletion and 90-day user-linked measurement deadlines. Optional measurement failure preserves actual core acceptance.

#### Evaluation Criteria

Coherent acceptance; recoverable deadline work; safe retries; independent optional effects; demonstrated producer/consumer needs; operating complexity.

#### Option A — Explicit core coordination with no broker initially

Keep required acceptance and current effects directly coordinated, using durable recoverable tracking for housekeeping where needed. This minimizes transport/state surfaces while permitting scheduled cleanup and independent measurement handling. Developers must still design resumption, retry and inspectable deadline completion; an ephemeral timer or fire-and-forget callback alone is insufficient for required lifecycle work.

#### Option B — In-process events as the main effect-coordination mechanism

Can decouple local listeners and make optional reactions convenient. Uncontrolled ordering, hidden listeners or lost events after restart can undermine coherent current effects and cleanup. It is credible as a local implementation detail inside an already safe acceptance/recovery design; it is a weaker primary coordination direction.

#### Option C — Durable message broker

Can support independently operating consumers and sustained asynchronous workloads. It adds message ownership, delivery/retry/order, duplicate handling, deployment and monitoring responsibilities. Delivery guarantees do not eliminate application logical-action identity or make related state changes automatically atomic. No current requirement for independent business consumers justifies a broker such as Kafka or RabbitMQ.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Core acceptance visibility | Good: explicit | Moderate: listener/order discipline | Needs cross-consumer visibility rules |
| Required cleanup recovery | Good with durable tracking | Weak without durable backing | Good transport potential; application recovery still required |
| Independent consumers | Limited; current need unproven | Local only | Strong if genuinely needed |
| MVP operation | Lowest additional infrastructure | Low infrastructure, hidden coupling risk | Highest infrastructure/coordination |

#### Recommendation

Option A — Explicit core coordination with no broker initially

#### Why Recommended

It addresses coherent state and recoverable lifecycle work without creating distributed consumers solely for decoupling. Optional effects can remain independent without a messaging platform.

#### Recommendation Confidence

High — the current MVP has no independent event-consumer or sustained queue requirement.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would include durable/resumable required housekeeping and clear optional-effect failure boundaries, while omitting a broker dependency. Necessary deletion could not rely solely on in-memory events.

#### Deferred Details

Scheduler/work-record realization, cleanup retries, local event usage, safe external-effect dispatch and any later broker/transport choice follow design and consequential ADRs.

### ADA-013 — Initial deployment and resource separation

#### Problem

How much runtime separation is needed to keep the core recoverable and prevent analysis resource use from invalidating complete-operation performance, without inventing production availability requirements?

#### Why This Decision Is Needed Now

**Important Before ADD.** ADD needs a feasible operating direction for the proposed core, AI and durable resources. The final placement belongs to Deployment View; no host/provider/GPU inventory is chosen here.

#### Related ASRs

ASR-REL-002, ASR-PERF-001, ASR-INT-001, ASR-SEC-002, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Performance, Interoperability and Testability — High; Availability, Scalability, Manageability and Supportability — Medium.

#### Relevant Constraints

NFR-REL-003: recoverable core restoration ≤5 minutes in the agreed validation environment, accepted state intact; NFR-PERF-001/002 and NFR-SCA-002 retain separate checks. No production uptime SLA, disaster-recovery/RPO, provider or orchestration requirement exists.

#### Evaluation Criteria

Durable state outside ephemeral execution; bounded resource contention; private dependency reachability; repeatable restart evidence; cost/operating skill; ability to increase separation if measured need appears.

#### Option A — Small controlled deployment with supervised core/AI processes and durable resources

Use a minimal operating footprint with independently managed application and analysis processes where ADA-004 is selected. Co-location is a candidate only if representative resources satisfy timing and integrity; it has a shared host failure domain and potential CPU/memory contention. Durable authoritative/media resources must survive application restart. This direction avoids claiming high availability while requiring controlled configuration and recovery evidence.

#### Option B — Core and AI on independently resourced hosts/services

Provides stronger resource and restart separation, potentially useful for measured analysis demands. It adds network/private-access, deployment and diagnosis work and ongoing cost. It is credible if co-location cannot meet existing requirements or actual model resource needs warrant it, rather than because two processes automatically require two hosts.

#### Option C — Orchestrated multi-instance deployment

Can aid managed scheduling/replacement at meaningful operating scale. It adds orchestration skill, configuration, state/authorization/cache coordination and resource planning. It does not by itself prove ≤5-minute recovery or timing; Kubernetes or similar infrastructure lacks a current MVP justification.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Resource isolation | Conditional; demonstrate capacity limits | Stronger independent resources | Configurable, more management |
| Failure isolation | Shared-host risk remains | Improved runtime host separation | Potential resilience; needs state design |
| Operating/cost burden | Lowest candidate footprint | Moderate | Highest |
| Source-driven need | Good initial direction if feasible | Conditional on resource/recovery evidence | Unproven |

#### Recommendation

Option A — Small controlled deployment with supervised core/AI processes and durable resources

#### Why Recommended

It matches the bounded recovery and validation needs without introducing an uptime or mass-scale commitment. Placement remains contingent on demonstrated resources; separation can increase without splitting business authority.

#### Recommendation Confidence

Medium — environment capacity, model memory/compute demand, cost and operating experience are unknown. If co-location is infeasible, select Option B rather than weakening timing or integrity.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would inherit a minimal deployment direction and explicit durability, resource and recovery responsibilities. Deployment View would resolve placement against evidence; same-host operation would retain a documented shared failure risk.

#### Deferred Details

Provider, machine counts/sizes, co-location, container usage, CPU/GPU, network layout, process supervisor, secrets, backup/restore and operating commands belong in Deployment View/engineering preparation.

### ADA-014 — Protected observability and validation evidence

#### Problem

What operating/evaluation information should the architecture make available so real acceptance, exactness, complete timing and restart behavior can be inspected without exposing private contents?

#### Why This Decision Is Needed Now

**Important Before ADD.** ADD must enable evidence collection rather than leave verification until after implementation. This is an internal operating direction, not a new analytics or administration product.

#### Related ASRs

ASR-TEST-001, ASR-REL-002, ASR-PERF-001, ASR-CON-001, ASR-SEC-002, ASR-USE-001.

#### Relevant Quality Attributes

Security and Reliability — Critical; Testability, Performance, Conceptual Integrity and Usability — High; Supportability and Manageability — Medium.

#### Relevant Constraints

NFR-TEST-001–003, NFR-SUP-001/002, NFR-SEC-003, AI-REQ-010/011/014 and FR-MET-006/007; SRS Sections 3.4.1/8.1.1/12.3. Locked qualified corpus ≥200 images, ≥50 per category; category ≥90% overall/≥85% each, color ≥90% separately; preview review has no percentage target.

#### Evaluation Criteria

Attributable attempted/accepted/failed/uncertain outcomes; complete timing boundaries; current/completion basis evidence; reproducible controlled benchmark; protected access/minimal content; useful operating cost.

#### Option A — Minimal protected structured logs, metrics and health/recovery evidence

Provide structured operation/outcome and timing evidence with necessary correlation across selected runtime boundaries, plus inspectable assessment and benchmark basis. It supports focused diagnosis and validation without a full telemetry platform. Instrumentation and controlled access/retention remain work; raw private images, sensitive profiles and secrets do not belong in ordinary engagement diagnostics. Correlation does not require a distributed-tracing vendor. Spring Boot's [observability support](https://docs.spring.io/spring-boot/reference/actuator/observability.html) is one implementation capability, not a substitute for defining CapsuleAI evidence.

#### Option B — Ad hoc text logging and manual inspection

Has low setup cost and may help local development. Inconsistent timestamps/outcome names make accepted-versus-attempted actions and complete-operation timing difficult to establish reliably; secret/content leakage is harder to control. Alone it is a weak architecture evidence direction for exact assessments and prescribed benchmarks.

#### Option C — Full tracing/telemetry platform from the outset

Can improve cross-service investigation at large distributed scale. It adds collection, storage, private-data governance and operating/vendor cost, and still needs correct operation/basis instrumentation. Current runtime complexity does not justify making such a platform a prerequisite.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Required evidence fit | Strong if designed around source measures | Weak as sole approach | Good potential, domain instrumentation still needed |
| Privacy/control surface | Bounded, deliberate fields/retention | Harder consistency review | Larger collection/storage surface |
| Two-runtime diagnosis | Good with necessary correlation | Manual and fragile | Strong tools, higher cost |
| Initial operating cost | Moderate and focused | Low setup, higher investigation effort | High |

#### Recommendation

Option A — Minimal protected structured logs, metrics and health/recovery evidence

#### Why Recommended

It supplies the current validation and support obligations with a bounded operating surface. A platform's sophistication cannot compensate for measuring job acceptance instead of complete results or recording partial sets as exact.

#### Recommendation Confidence

High — current sources explicitly require inspectable outcomes and evidence, while a tracing platform/vendor remains unnecessary.

#### Decision Status

Proposed — Awaiting Selection

#### Consequence If Selected

ADD would allocate privacy-safe operating/validation evidence responsibilities. Functional history, ordinary user-linked measurement and controlled AI benchmark evidence would retain different purposes/lifecycles; optional measurement failure would not undo core acceptance.

#### Deferred Details

Event/metric fields, log access/retention, correlation realization, health interfaces, instrumentation libraries, monitoring vendor and executable harness/reporting belong in detailed design, Test Strategy and engineering preparation.

## 6. Decisions That Do NOT Need to Be Made Yet

These are **Defer to Detailed Design / ADR** concerns unless new evidence makes a major boundary change necessary. Deferral is not permission to omit required behavior.

| Deferred Concern | Why not selected here / later responsibility |
| --- | --- |
| Mobile implementation framework | Supported platforms, accessible Vietnamese outcomes and canonical meanings are fixed; framework is open. Mobile/Architect contributors should compare compatibility and integration feasibility before the dependent Implementation View baseline. No React Native preference or supported-version assumption is made. |
| Exact core modules/classes/packages | ADA-001 concerns organization direction; Logical/Implementation Views define actual responsibilities/dependencies after selection. |
| Exact inference model/framework, licenses and CPU/GPU | AI contributors must establish packaging, quality and full-result feasibility. If findings overturn ADA-004/005/013, revisit analysis/selection before dependent design. |
| Database tables, types, indexes, ORM and isolation/locking details | Data View and detailed data design implement the selected authority and prove invariants; no schema is created here. |
| Endpoint DTOs, transport payloads, error contracts and retry/action keys | Detailed API/service design realizes current behavior and durable logical-action semantics after ADD/views. |
| Enumeration/ranking optimizations and derived caches | Profile relevant workloads and preserve identity, basis and completeness. No arbitrary cut-off or Multiplier latency is added. |
| Object-storage provider, image delivery and deletion mechanisms | Views/design must realize private effective access and applicable physical purge. Necessary copy/version control is a required design concern even though provider is open. |
| Broker technology, CQRS, event sourcing and orchestration | No current need establishes these mechanisms. Reconsider only when a concrete requirement or operating evidence warrants their coordination cost. |
| Monitoring vendor, log fields/retention and tracing implementation | Detailed design/Test Strategy/engineering preparation define protected evidence. No ordinary diagnostic store may become an indefinite user-linked measurement copy. |
| Host counts/sizes, network layout and operating commands | Deployment View resolves resources and recovery from evidence. No cloud provider, production SLA or mandatory multi-host topology is set. |
| Detailed sequences/states, executable test strategy and CI tooling | Follow their downstream stages; this analysis does not create them or claim software verification. |

A major alternative that affects authoritative state, security, exactness or runtime boundaries cannot be silently relegated to implementation. Bring such evidence back to the relevant ADA and explicit selection record.

## 7. Cross-Decision Interactions

| Interaction | Consequence for human review and later design |
| --- | --- |
| ADA-001 ↔ ADA-003/008/009 | Modular local coordination relies on a coherent authoritative persistence direction. Selecting distributed business services changes mutation/session/recovery costs and requires renewed analysis; it is not a harmless code-organization substitution. |
| ADA-002 ↔ ADA-004/005/013 | Core runtime, model compatibility, analysis communication and resources must fit together. Python in another process does not require separate hosts; co-location does not remove resource contention. Review complete processing evidence and contributor effort as a set. |
| ADA-003 ↔ ADA-007/008/009/012 | Primary transactions can coordinate sensitive business state, but media/email and physical cleanup remain cross-resource responsibilities. No cache/broker recommendation eliminates durable recovery or lifecycle deadlines. |
| ADA-010 ↔ ADA-009/011/014 | Accepted corrections/removals and current evaluation basis govern derived reuse. Benchmark optimization cannot replace full Multiplier evidence with displayed daily choices or a cached prior count. |
| ADA-006 ↔ ADA-005/007/014 | Boundaries must retain truthful dependency outcomes, minimal disclosure, authorized private media and inspectable complete timing. A provider adapter cannot excuse a bearer-access or lifecycle mismatch. |
| ADA-013 ↔ ADA-014 and all Critical decisions | Recovery evidence needs durable accepted state and repeatable operation. Minimal deployment is conditional on actual resources; monitoring tools alone do not prove resilience. |
| All choices ↔ ASR-USE-001 | Backend outcomes must retain confidence/provenance, Saveable/readiness, uncertain mutation and non-exact assessment meaning. Mobile accessibility/usability remains integrated work, not a framework side effect. |

If the human overrides an option, evaluate these dependent recommendations rather than mixing incompatible assumptions. No individual High-confidence recommendation makes the whole set selected.

## 8. Proposed Decision Set for Human Review

**Not yet selected.** Section 4 is the compact proposed set: ADA-001–014 each recommend Option A, whose meaning differs for each problem. Option letters indicate presentation order, not a scoring or automatic-selection rule. The option analyses explain when credible alternatives could be preferable.

Resolve **ADA-001/003/004/008/009/010 (Critical Before ADD)** explicitly. Review Important decisions together with their dependencies; any safe deferral must name the responsible role, known boundary/impact, evidence needed and resolution point before dependent architecture/design. Do not defer a Critical decision merely because it is difficult.

The most material evidence gaps are runtime/team suitability (ADA-002), actual model packaging/quality/resource demand (ADA-004/005/013), computation cost (ADA-010), and private media access/deletion/cost feasibility (ADA-007). Selection does not claim that required benchmarks have passed. Later design and Test Strategy establish how to demonstrate them.

The future `selected-architecture-decisions.md` should record ADA ID, selected analyzed option, selection basis/override reason, Backend Impact and status (Selected, Deferred or Superseded), plus selecting human/review date and bounded-deferral details where applicable. It should link to these analyses, not repeat them, and is not an ADR replacement. That artifact is deliberately **not created here**.

## 9. Backend Developer Takeaways

- The proposed modular core and relational authority reduce coordination surfaces; they still require deliberate transaction, ownership and concurrency design.
- Framework JWT validation does not replace current-session/account authority. Reset, logout and refresh reuse have different effects.
- A durable logical-action identity protects retries; it must not collapse new same-outfit/day Wear intentions. Original local grouping and absolute elapsed aging remain separate.
- AI is proposed as a private analysis boundary; user confirmation remains authoritative. Synchronous interaction fits the short complete-result target provisionally, subject to measurement.
- Request-time intelligence avoids premature invalidation machinery. Shared validity/identity is necessary, while different consumers may use different algorithms. Exact Multiplier needs complete same-basis sets.
- Redis, a broker and orchestration add correctness/operating work; measured need can justify them later. Required cleanup and recovery exist even without those services.
- Private media and operating evidence need purpose, authority and lifecycle design. Object existence, a deletion marker or a successful telemetry response cannot establish business acceptance.
- Product ordering remains PO-accountable. Developers contribute feasibility and operating implications; this draft commits no Sprint work or technology choice.

## 10. Traceability

This matrix maps each existing formal ASR to decision problems. It adds no requirements and leaves the ASR's own detailed source/scenario traceability authoritative.

| Formal ASR | Relevant ADA Problems | Main Existing Evidence Anchors |
| --- | --- | --- |
| ASR-SEC-001 | ADA-001/002/003/006/007/008/011 | QA-SEC-01/02; FR-AUTH-004/007/009/013/014/017/018; DATA-AUTH-005 |
| ASR-SEC-002 | ADA-003/004/006/007/009/011/012/013/014 | QA-SEC-03; NFR-PRIV-001/002; DATA-RET-001–003; SI/COM purpose boundaries |
| ASR-REL-001 | ADA-001/002/003/004/005/007/008/009/010/011/012 | QA-REL-01/02, QA-SCA-01; DATA-INT-001–004; BRULE-WEAR/PERS |
| ASR-CON-001 | ADA-001/002/003/009/010/011/014 | QA-CON-01; BRULE-OUT/COV/MULT; DATA-OUT-001/002; DATA-ANL-004 |
| ASR-PERF-001 | ADA-002/004/005/007/010/011/013/014 | QA-PERF-01/02; NFR-PERF-001/002; NFR-TEST-003; SRS Section 12.3 |
| ASR-INT-001 | ADA-004/005/006/012/013 | QA-INT-01, QA-AVL-01; SI-001–004; UC-004/006/007/023 |
| ASR-REL-002 | ADA-001/003/004/007/008/009/012/013/014 | QA-REL-03, QA-SUP-01; NFR-REL-003; applicable DoD operational criteria |
| ASR-USE-001 | ADA-005/006/010/014; outcome semantics across all choices | QA-USE-01/02, QA-MNT-01; NFR-USE/ACC; LOC-001–005 |
| ASR-TEST-001 | ADA-001/002/004/005/006/008/009/010/012/013/014 | QA-TEST-01, QA-SUP-01; NFR-TEST-001–003; AI-REQ-010/011/014; DoD Sections 4–6 |

For all 14 QA classifications, use QA analysis Section 5; the per-ADA tables name the attributes relevant to that comparison. Availability/Scalability and other Medium attributes remain real obligations without automatically requiring redundancy or distributed infrastructure. Reusability is Low and supplies no separate reusable-platform decision.

## 11. Status and Next Step

**Version 0.1 — Baseline Draft. All recommendations Proposed — Awaiting Selection.** Prepared for human Architect/Tech Lead review with Developers and relevant AI/Mobile/QA/Product contributors. No preference, approval, software implementation or passed benchmark is inferred.

Next: **human review → Selected Architecture Decisions**. ADD begins only after the Critical decisions are explicitly selected and any Important deferrals have a safe documented boundary. Then follow Workflow Section 46: ADD → Logical View → Implementation View → Deployment View → Data View → ADRs → Detailed API/Data/Sequence/State Design → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation.

This task creates the analysis and updates current process handoffs only. It creates no selected decision record, ADD/views, ADR, detailed contract/schema/sequence/state artifact, Test Strategy, Sprint artifact or implementation code.
