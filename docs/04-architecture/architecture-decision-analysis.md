# CapsuleAI — Architecture Decision Analysis

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Architecture Decision Analysis |
| Version | 0.2.4 |
| Status | Baseline Draft |
| Decision State | ADA-001–018 Selected; authority: [Selected Architecture Decisions](selected-architecture-decisions.md) |
| Last Updated | 2026-10-10 |
| Ownership | Architect / Tech Lead with Developer input; review with AI, Mobile, QA and Product/BA where relevant |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 17.2–17.3, 18, 39–43 and 46 |
| Requirements Baseline | BRD v0.3; PRD v0.2; SRS v0.3; Business Rules v0.1.2 |
| Authoritative Location | `docs/04-architecture/architecture-decision-analysis.md` |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-08 | Initial comparison of 14 architecture decision problems across the whole MVP; recommendations await human selection. No product requirement, QA measure or ASR content changes. |
| 0.2 | 2026-10-10 | Added ADA-015–018 for internal module architecture, context data ownership, inter-context communication and model isolation/ACL; reconciled existing recommendations, dependencies, deferrals and human-review gates around service-extractable modularity. Corrected the Business Analysis source location. No product, QA or ASR change; all 18 recommendations remain proposed. |
| 0.2.1 | 2026-10-10 | Synchronized current decision statuses and handoff with explicit human acceptance of all 18 v0.2 recommendations in Selected Architecture Decisions v0.1. Analytical options, trade-offs, confidence, importance and historical revision entries remain unchanged. |
| 0.2.2 | 2026-10-10 | Current navigation acknowledges ADD v0.1 and advances to Logical View; analyses, selected statuses, importance/confidence and historical revision entries remain unchanged. |
| 0.2.3 | 2026-10-10 | Linked Logical View v0.1 and advanced current navigation to Implementation View; analytical content, selected statuses and historical revision entries remain unchanged. |
| 0.2.4 | 2026-10-10 | Linked Implementation View v0.1 and recorded explicit human acceptance of the Logical decomposition; review precedes Deployment View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |

## 2. Purpose

Expose the major choices between required architectural capability and a coherent design. The analysis covers the complete three-pillar mobile MVP: confirmed wardrobe, valid personalized styling and reported behavior, and contextual Coverage/Gaps plus hypothetical candidate utility. The first eight stories are implementation/refinement context, not the architecture boundary.

The selected business-core direction is a modular monolith with **service-extractable modularity**, recorded in [Selected Architecture Decisions](selected-architecture-decisions.md). ADA-001/015–018 supply the unchanged analysis of that coherent direction; no implementation baseline is established. System-level modularity, internal dependency direction, logical data ownership, interaction semantics and model isolation answer different questions. Preparing boundaries for possible extraction does not establish microservices as current product scope.

| Artifact | Responsibility |
| --- | --- |
| ASR | What architecture must be capable of satisfying; requirements and pressures, not solutions. |
| Architecture Decision Analysis (ADA) | Problems, credible alternatives, trade-offs, recommendation and confidence. |
| Selected Architecture Decisions | Explicit human selection or justified bounded deferral of analyzed options. |
| ADD and its views | Architecture structure and realization given ASRs and explicitly selected directions. |
| ADR | Durable rationale, alternatives and consequences for consequential selected decisions, recorded later in this workflow. |

**Recommendation is not selection.** The Project Owner / Backend Developer explicitly accepted every current ADA-001–018 recommendation on 2026-10-10; all are now **Selected**, without override or deferral. The separate [selection record](selected-architecture-decisions.md) governs the chosen options. The analytical comparisons and conditional consequences below retain their v0.2 wording, importance, confidence and evidence gaps; selection does not prove conformance or benchmarks. Detailed providers, versions and topology remain downstream. A material new alternative still requires analysis and explicit selection.

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
| [Implementation Summary](../../Initial%20files/CapsuleAI_Implementation_Summary.md), proposal/official outline under Initial files and [Business Analysis.docx](../../Business%20Analysis.docx) at the repository root | Historical/exploratory context. Their technology preferences, commercial/reward ideas and old thresholds do not establish current scope or constraints. |
| [Quality-attribute learning reference](../../T%C3%A0i%20li%E1%BB%87u%20tham%20kh%E1%BA%A3o%20cho%20Architecture/14-Thu%E1%BB%99c-t%C3%ADnh-ch%E1%BA%A5t-l%C6%B0%E1%BB%A3ng.md), ASR_FoodDelivery.md and ADD_FoodDelivery.md in the same reference directory | Methodology and artifact-boundary/structure reference only. No Food Delivery stack, event architecture or deployment decision is adopted. |

Existing technical capability claims retain their **2026-10-08** Context7/official-documentation checks. The v0.2 expansion checked Spring event/transaction behavior and PostgreSQL schemas/privileges through Context7 on **2026-10-10**, and consulted primary Clean Architecture/DDD explanations linked in ADA-015/018. These references explain capabilities and concepts; CapsuleAI-specific judgments derive from current ASRs and requirements. They do not create product requirements, prove benchmark results, or select versions/providers.

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

All 18 entries are Selected through the separate human [selection record](selected-architecture-decisions.md); the recommendation/importance/confidence columns retain the v0.2 analytical summary. Full recommended Option wording is recorded in Section 5 and the selection record. Persistence model and technology are one problem; avoiding a dedicated cache/broker is a positive architecture recommendation, not an omitted analysis.

| ID | Decision Problem | Importance | Recommendation | Confidence | Status |
| --- | --- | --- | --- | --- | --- |
| ADA-001 | Business-core architecture style | Critical Before ADD | Modular monolith for the business core | High | Selected |
| ADA-002 | Backend application runtime | Important Before ADD | Java with Spring Boot for the business application | Medium | Selected |
| ADA-003 | Authoritative persistence model and database | Critical Before ADD | One relational primary database: PostgreSQL | High | Selected |
| ADA-004 | AI/CV execution boundary | Critical Before ADD | Separate private Python analysis runtime | Medium | Selected |
| ADA-005 | Backend-to-AI interaction | Important Before ADD | Bounded synchronous complete-result interaction | Medium | Selected |
| ADA-006 | External integration boundaries | Important Before ADD | Narrow explicit ports/adapters for real dependencies | High | Selected |
| ADA-007 | Private image persistence and access | Important Before ADD | Private object storage with application-controlled authorized delivery | Medium | Selected |
| ADA-008 | Session, refresh and reset authority | Critical Before ADD | Primary database as persistent session/reset authority | High | Selected |
| ADA-009 | Logical-action idempotency and accepted effects | Critical Before ADD | Database-backed logical-action coordination and coherent effects | High | Selected |
| ADA-010 | Advice and wardrobe-intelligence computation | Critical Before ADD | Request-time evaluation from authoritative inputs with shared semantics | Medium | Selected |
| ADA-011 | Dedicated cache strategy | Important Before ADD | No dedicated cache initially | High | Selected |
| ADA-012 | Messaging and background coordination | Important Before ADD | Explicit core coordination with no broker initially | High | Selected |
| ADA-013 | Initial deployment and resource separation | Important Before ADD | Small controlled deployment with supervised core/AI processes and durable resources | Medium | Selected |
| ADA-014 | Protected observability and validation evidence | Important Before ADD | Minimal protected structured logs, metrics and health/recovery evidence | High | Selected |
| ADA-015 | Internal Module Architecture | Important Before ADD | Pragmatic Clean Architecture inside each business module | Medium | Selected |
| ADA-016 | Context Data Ownership | Critical Before ADD | One PostgreSQL database initially with explicit context-owned logical persistence boundaries | High | Selected |
| ADA-017 | Inter-Context Communication | Critical Before ADD | Explicit published module contracts; synchronous local or event-based interaction follows business semantics | High | Selected |
| ADA-018 | Context Model Isolation and Anti-Corruption | Important Before ADD | Published contracts + Anti-Corruption Layer translation at meaningful semantic boundaries | High | Selected |

## 5. Major Architecture Decision Analyses

The following comparisons retain the v0.2 preselection analysis, including conditional wording and evidence gaps. Only Decision Status fields are synchronized here; exact selections are authoritative in [Selected Architecture Decisions](selected-architecture-decisions.md).

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

Selected

#### Consequence If Selected

ADD would describe one coordinated business core with explicit responsibilities, plus any separately selected analysis boundary. Releases of the core would remain coupled and boundary discipline would become ongoing work.

#### Service-Extractable Modularity

The proposed modular monolith favors local consistency, simpler operation, lower distributed-system complexity and cohesive business-rule realization. ADA-015–018 make its boundaries deliberate: cohesive business responsibility, accountable ownership, private internals, published contracts, controlled dependencies, context-owned logical data and explicit synchronous/asynchronous semantics.

Determine boundaries from the business domain and cohesive responsibilities, then establish ownership/contracts. Only afterward use a future extraction thought experiment to test whether those boundaries remain sensible. A bounded context is a semantic/model boundary; a module is a local implementation boundary. Their mapping belongs to the Logical/Implementation Views, not a pre-invented list of future services. A possible future service may realize one or more healthy responsibilities; **one module does not imply one future microservice**, and some modules may remain local permanently.

Reconsider extraction only when evidence shows materially different independent scaling/resource profiles, measurable independent-deployment value, separate teams with independent domain ownership, release coupling as a demonstrated bottleneck, or diverging failure-isolation/availability requirements. Review affected ASRs and ADA dependencies before human selection and design changes. No migration date, extraction obligation or new availability target is set.

**Microservices are not an MVP requirement.** Prepare ownership and contracts rather than introducing internal HTTP, brokers, API Gateway, service mesh, service discovery, separate database servers, Saga or Kubernetes for a hypothetical future.

#### Deferred Details

ADA-015–018 analyze internal dependency, ownership, communication and model-isolation policies now. Exact bounded-context/module maps, public interfaces, packages and dependency-enforcement realization belong in ADD and Logical/Implementation Views; consequential rationale belongs in later ADRs.

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

Selected

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

Selected

#### Consequence If Selected

ADD would use a single primary authority for related business/session state, with separate media storage if ADA-007 is selected. Cross-resource image work would need explicit recoverable coordination; database transactions would not make object-storage operations atomic.

#### Relationship to Context Ownership

ADA-003 compares the primary persistence model/database family; **ADA-016** analyzes logical ownership inside the proposed shared model. One PostgreSQL database does not imply that every module may freely access every table. Owned mutations, lifecycle and permitted reads remain behind published boundaries; exact schemas/tables are downstream. Local transactions can coordinate owner operations without permitting direct cross-context repository/entity/schema access.

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

Selected

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

Selected

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

Selected

#### Consequence If Selected

ADD would separate provider translation from business outcome decisions. UC-006 would remain location/weather acquisition authority; consuming assessments would not become new independent external acquisition goals. SDK use would remain possible within an adapter.

#### Semantic Translation and Anti-Corruption

An adapter also acts as an **Anti-Corruption Layer** when provider meaning differs from CapsuleAI's. The port defines the needed interaction, the adapter realizes access/transport, and the ACL translates semantics; they are related responsibilities, not synonyms. Illustrative translations include weather responses to CapsuleAI environmental evidence (possibly named WeatherSnapshot), Python analysis responses to GarmentAnalysisProposal, optional shopping responses to qualified commercial evidence, and object-storage behavior to a private media abstraction.

External DTOs, SDK types, provider exceptions and vocabulary stay at the boundary rather than spreading through domain/application decisions. Translation must preserve confidence/provenance, currentness, absence/failure and access/lifecycle meaning. **ADA-018** analyzes internal and external model isolation, including why ACL does not imply a stored snapshot. These names are examples, not final classes or additional integration scope.

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

Selected

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

Selected

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

Selected

#### Consequence If Selected

ADD would make accepted identity/outcome and coherent consumer state explicit. A new same-outfit/day report would remain a separate event; normalization would cap only ranking contribution using the latest surviving accepted absolute anchor.

#### Transaction Boundary and Future Extraction Coupling

The current proposed modular monolith may legitimately use local PostgreSQL ACID transactions where correctness requires them. Do not avoid useful local transactions to imitate microservices. ADA-016 assigns data owners; ADA-017 governs explicit collaboration. An application coordinator can invoke owned operations through public contracts in a deliberately shared local transaction, without directly importing another owner's repository/entity or treating all tables as public.

A transaction that changes multiple contexts' owned state creates **future extraction coupling**. Later design must identify the participating owners, required invariant, acceptance boundary, concurrency/failure behavior and why their coordinated commit is needed. This is a design trade-off, not a ban or a finalized transaction map. Some effects can instead be coherent authoritative derived reads; exact realization stays downstream. Database atomicity does not include object storage or email.

If those responsibilities are later extracted, the same business guarantees may require redesigned local service transactions, reliable events, an Outbox, eventual consistency where permitted, or Saga/compensation where justified. Those are potential future mechanisms, not current selections. Immediate removal/revocation and truthful accepted outcomes cannot become eventual merely to ease extraction. Re-analysis may conclude that tightly coupled responsibilities should remain together.

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

Evaluate current inputs through consistent validity/identity responsibilities and establish completion/basis before claiming a result. It reduces stored-result invalidation surfaces and keeps evidence close to the request. Computation can be expensive, especially full hypothetical sets, and concurrent changes still require basis validation. Shared semantics does not mean Daily and Multiplier must use the same algorithm or compute every possible outfit in the Daily path. Under ADA-016–018, authoritative inputs would normally arrive through published local owner contracts, with coherent basis checks. Consistent validity/identity needs explicit semantic ownership, not unrestricted table access, a universal entity library or independently drifting copies of the rules.

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

Selected

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

Selected

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

Keep required acceptance and current effects directly coordinated, using durable recoverable tracking for housekeeping where needed. This minimizes transport/state surfaces while permitting scheduled cleanup and independent measurement handling. Developers must still design resumption, retry and inspectable deadline completion; an ephemeral timer or fire-and-forget callback alone is insufficient for required lifecycle work. Direct coordination may include local domain/application events where their semantics fit ADA-017; explicit acceptance and recovery remain the governing design.

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

Selected

#### Consequence If Selected

ADD would include durable/resumable required housekeeping and clear optional-effect failure boundaries, while omitting a broker dependency. Necessary deletion could not rely solely on in-memory events.

#### Domain Events and Future Integration Events

A Domain Event expresses a business fact in its owning model; an application event can coordinate a local reaction. Neither implies asynchronous delivery, durable publication or an independently deployed consumer. In-memory events may serve permitted local reactions while required acceptance remains coherent and deadline work remains durably recoverable. Domain meaning must not depend on a Spring event class or broker type (ADA-015).

A future **Integration Event** is a deliberately published cross-service contract, not the same internal event object moved onto a wire. It may require event identity, schema/versioning, serialization, backward compatibility, delivery/failure semantics, consumer idempotency and replay rules. ADA-017/018 keep transport separate from published meaning; a Java/Spring event object cannot be assumed to become an unchanged Kafka payload.

Kafka, RabbitMQ or another broker becomes a candidate only if independent service communication or measured operating needs justify it. An Outbox is a possible future reliable-publication mechanism, not a component selected here. No-broker operation today still needs recoverable required work; if a concrete current guarantee later requires stronger publication, compare that mechanism on its own evidence rather than adopting Outbox everywhere.

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

Selected

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

Selected

#### Consequence If Selected

ADD would allocate privacy-safe operating/validation evidence responsibilities. Functional history, ordinary user-linked measurement and controlled AI benchmark evidence would retain different purposes/lifecycles; optional measurement failure would not undo core acceptance.

#### Deferred Details

Event/metric fields, log access/retention, correlation realization, health interfaces, instrumentation libraries, monitoring vendor and executable harness/reporting belong in detailed design, Test Strategy and engineering preparation.

### ADA-015 — Internal Module Architecture

#### Problem

How should each business module organize its internals so domain rules and application decisions remain independent of Spring, persistence, HTTP, message transport, provider SDKs, object storage and AI runtime details?

#### Why This Decision Is Needed Now

**Important Before ADD.** The shared validity, authority and evidence obligations need a dependency direction before the Implementation View can establish module internals. ASR-CON-001 and ASR-TEST-001 justify isolation; they do not mandate a textbook layering scheme or make every folder a Critical architectural choice. A bounded deferral must preserve inward dependencies and private internals as explicit open design constraints, with Architect/Backend ownership and resolution before the dependent Implementation View.

#### Related ASRs

ASR-CON-001, ASR-TEST-001, ASR-INT-001, ASR-REL-001, ASR-USE-001.

#### Relevant Quality Attributes

Reliability — Critical; Conceptual Integrity, Testability and Interoperability — High; Maintainability and Flexibility — Medium. No change-cost target or High Maintainability classification is inferred.

#### Relevant Constraints

DATA-INT-001–004, NFR-REL-002, NFR-TEST-001/002 and LOC-002/003; BRULE-GAR-001/003, BRULE-OUT-008/009 and BRULE-MULT-004–009. Business meaning spans the full MVP and must survive dependency failure, label changes and accepted mutations. ADA-002's Java/Spring recommendation remains proposed; the dependency principle is useful independently of runtime selection.

#### Evaluation Criteria

Inspectable domain behavior without infrastructure; clear ownership and dependency direction; controlled provider/model translation; coherent transaction orchestration; contributor learning and mapping cost; sufficient structure without speculative abstractions.

#### Option A — Traditional Layered Architecture

Within each module, use presentation/controller, service and repository layers. This is familiar, supports local transactions and can be economical for simple behavior. Disciplined implementations can isolate rules and satisfy the ASRs. If services directly consume persistence entities, HTTP or provider types, domain tests and changes become coupled to infrastructure. Horizontal layers across the entire core would also weaken ADA-001's module boundaries; this option compares internal organization, not removal of modularity.

#### Option B — Pragmatic Clean Architecture inside each business module

Keep domain meaning inward, application orchestration around it and inbound/outbound adapters outside those contracts. Configuration assembles concrete implementations at the edge. Domain decisions remain testable independently of framework and provider details; application ports expose only genuine boundaries. The costs are maintaining useful translations, learning dependency inversion and ensuring transaction coordination remains explicit.

The dependency rule follows [Robert C. Martin's Clean Architecture explanation](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html). CapsuleAI's proposed application of that rule is deliberately small; the source does not mandate this project's packages.

| Conceptual Role | Responsibility / Dependency Direction |
| --- | --- |
| Domain | Confirmed authority, validity, identity and other rules belonging to the module; independent of application orchestration and outer implementation details. |
| Application, including needed ports | Coordinates owned use cases and acceptance; depends on domain meaning and narrow boundary contracts, not adapter implementations. |
| Inbound adapters | Translate user/module entry into application operations; depend inward. |
| Outbound adapters | Implement needed persistence, external/provider or neighboring-module ports; depend inward. |
| Configuration | Wires concrete implementations outside the domain/application core. |

This is a conceptual responsibility shape, not a finalized folder tree. Domain/application logic must not import JPA entities, Spring controllers, RestClient/WebClient, AWS/S3 SDK types, Python AI DTOs or provider-specific DTOs. Persistence entities and transport types remain adapter concerns. Framework transaction realization can surround application acceptance without moving transaction-dependent business policy into controllers or repositories.

Do not create an interface for every class, a generic repository unrelated to domain needs, excessive mapper chains or empty ceremonial layers. Keep simple behavior simple and place actual domain decisions where their responsibility belongs; do not make the domain anemic to fit a diagram.

#### Option C — Framework-centric feature organization without a strict inward dependency rule

Group each feature's framework components, services and persistence access together. This can be the smallest readable solution for a limited team and straightforward behavior, with fewer mappings and direct use of framework facilities. It remains credible with focused tests and disciplined private module contracts. CapsuleAI's richer shared semantics would depend more heavily on conventions and infrastructure-aware tests, and provider/model changes could spread into business decisions.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Domain/infrastructure isolation | Possible with extra discipline | Explicit inward rule | Convention-dependent |
| Controlled boundary/model changes | Moderate; avoid entity leakage | Strong at meaningful ports/translation | More direct framework coupling |
| Initial development effort | Familiar, modest | Moderate; keep only useful boundaries | Lowest for simple features |
| Rule/evidence testing | Good if rules isolated | Strong independently of adapters | More framework-coupled |
| Possible later extraction | Depends on existing seams | Core can survive changed adapters | More infrastructure disentangling |

#### Recommendation

Option B — Pragmatic Clean Architecture inside each business module

#### Why Recommended

ADA-001 governs the business-core organization/deployment direction; this choice governs dependencies inside its modules. They complement each other. The substantial domain fixtures and provider distinctions justify an inward rule, while Medium Maintainability/Flexibility and Low Reusability argue against elaborate frameworks. ADA-006 defines external seams and ADA-018 governs semantic translation at those seams.

#### Recommendation Confidence

Medium — the source-derived isolation benefit is clear, but contributor proficiency, final domain boundaries and the smallest useful mapping/port granularity are unverified. Review representative confirmation and assessment responsibilities before fixing the Implementation View; do not infer team familiarity.

#### Decision Status

Selected

#### Consequence If Selected

ADD/Implementation View would allocate domain/application responsibilities and inward adapter dependencies within domain-oriented modules. A later justified extraction could retain much of the domain/application core while changing a local port/facade adapter to HTTP/gRPC, or local event delivery to broker integration. Extraction would still require contract, authorization, consistency, data migration, failure and operating redesign; it is neither automatic nor cost-free.

#### Deferred Details

Exact bounded contexts, module/package names, source folders, Java classes/interfaces, mapper count, transaction wiring and dependency-enforcement tooling follow ADD/Logical/Implementation Views and detailed design. No framework extension or new library is selected.

### ADA-016 — Context Data Ownership

#### Problem

How should bounded contexts/modules own and access persisted business information while sharing ADA-003's proposed primary PostgreSQL database?

#### Why This Decision Is Needed Now

**Critical Before ADD.** ADD must assign authoritative mutation, privacy/lifecycle and current-state responsibilities before coherent interactions can be designed. An unspecified free-for-all store would undermine those boundaries. The Critical choice is the ownership policy, not exact context decomposition, schema names or access tooling.

#### Related ASRs

ASR-SEC-001, ASR-SEC-002, ASR-REL-001, ASR-CON-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Security and Reliability — Critical; Conceptual Integrity and Testability — High; Maintainability, Flexibility and Manageability — Medium.

#### Relevant Constraints

SRS Sections 4.1–4.9, especially DATA-AUTH-005, DATA-GAR-001/003/005, DATA-WEAR-003/004, DATA-INT-001–004 and DATA-RET-001–003; BRULE-GAR-009, BRULE-WEAR-005/006 and BRULE-HIST-002/003. Logical information domains and Use Case packages are requirement groupings, not a final bounded-context map. Necessary minimal historical snapshots remain supported; current authority and retained history have different purposes.

#### Evaluation Criteria

Unambiguous authority and lifecycle ownership; coherent accepted changes; private persistence models; usable current reads through contracts; local integrity and recovery; enforcement/change cost; future extraction coupling.

#### Option A — Shared database with unrestricted cross-module table access

Any module reads or updates any useful table. Queries and multi-object local transactions are initially convenient, and one store remains economical. It is credible for a small conventional application where all information intentionally belongs to one responsibility. In CapsuleAI's proposed modular core, authority, retention and representation changes would have hidden consumers/writers. This weakens module ownership and increases the risk that removed data remains influential or provider proposals bypass confirmation.

#### Option B — Shared PostgreSQL with explicit context-owned persistence boundaries

Use one physical primary database initially, with an accountable context owning each business persistence model and its mutations/lifecycle. Other contexts request permitted information or operations through published contracts, ports/facades or semantically appropriate events. Repository/entity ownership, table groups, schemas or privileges may later help realize the boundary; none is finalized here.

PostgreSQL schemas can organize logical object groups, but are not inherently isolated databases. Access depends on schema/object privileges; a single broadly privileged application identity does not automatically enforce module discipline. See the [PostgreSQL schema and privilege documentation](https://www.postgresql.org/docs/18/ddl-schemas.html). Implementation dependency controls and authorized public contracts still matter; this reference selects no database version or schema policy.

One PostgreSQL database does not permit another context to import an owner's repository, persistence entity, internal tables or private schema representation. A justified exception must identify the owning/consuming responsibilities, access purpose, affected invariant/lifecycle, coupling, evidence and review point in the owning design/ADR. Convenience is not an implicit exception.

Owner operations may participate in a deliberately coordinated local ACID transaction through their public boundaries (ADA-009/017). Physical sharing permits useful transactions without surrendering logical ownership; it does not make media/email resources atomic.

#### Option C — Physically separate database per context immediately

Provides stronger physical isolation and independent storage/migration operation, useful if security, recovery, organizational autonomy or workloads warrant it. It can be justified even without microservices. Here it adds credentials, recovery/deletion surfaces and cross-database consistency work to immediate exclusions and accepted effects. No present requirement establishes independent database operation, so the added burden is not offset by current evidence.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Authority/lifecycle responsibility | Hidden cross-module writers | Explicit owner and contracts | Strong physical boundary; coordination still needed |
| Local coherent acceptance | Convenient, tightly coupled | Supported through owner operations | Harder across stores |
| Persistence change isolation | Weak | Good with enforced ownership | Strong physically |
| Initial operation/recovery | One database | One database plus boundary discipline | Multiple recovery/access surfaces |
| Future extraction | Hidden data dependencies | Visible logical ownership/coupling | Data separation already present; workflow redesign remains |

#### Recommendation

Option B — One PostgreSQL database initially with explicit context-owned logical persistence boundaries

#### Why Recommended

This separates two decision questions: ADA-003 compares the primary persistence model/database family; ADA-016 compares ownership inside that proposed shared model. It preserves local coordination and manageable operation while making authority and lifecycle accountable. If ADA-003 is overridden, revisit physical realization without discarding the ownership analysis.

#### Recommendation Confidence

High — current privacy, accepted-state and same-basis obligations support explicit owners, while no driver presently requires physical database separation. Final context boundaries and enforcement strength still require design/evidence.

#### Decision Status

Selected

#### Consequence If Selected

ADD/Logical/Data Views would assign ownership and permitted contracts without unrestricted cross-context persistence access. Today that is logical ownership inside one proposed database; future justified extraction may make it physical ownership, possibly Database-per-Service. It does not imply one module per service or separate database servers today.

#### Cross-Context Foreign Keys

Within-context relational integrity may use ordinary foreign keys. Cross-context foreign keys are an architectural trade-off: they can enforce useful current integrity but also couple migration/deletion, schema changes and possible physical extraction. They are not absolutely banned or approved wholesale here. Data View/detailed data design must record which integrity belongs within an owner, which dependencies cross boundaries and how accepted removal/minimal history remain correct. Their existence never authorizes another module's direct persistence access.

#### Deferred Details

Exact context map, PostgreSQL schemas/table groups, roles/permissions, tables, repositories, cross-schema foreign-key policy, cross-context joins/exceptions, migrations and transaction participation follow the views and detailed design. Possible physical separation requires new evidence and renewed analysis/selection.

### ADA-017 — Inter-Context Communication

#### Problem

How should modules communicate inside the proposed modular monolith while preserving ownership, business outcome/consistency semantics and a realistic extraction path?

#### Why This Decision Is Needed Now

**Critical Before ADD.** Accepted removal, session revocation, Wear survivor effects and current exact assessments cannot be coherent if their interactions are arbitrarily asynchronous or bypass owners. The communication/acceptance policy shapes ADD collaboration; exact interfaces and transports remain downstream.

#### Related ASRs

ASR-REL-001, ASR-SEC-001, ASR-SEC-002, ASR-CON-001, ASR-INT-001, ASR-PERF-001, ASR-REL-002, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Conceptual Integrity, Interoperability, Performance and Testability — High; Maintainability, Flexibility and Manageability — Medium.

#### Relevant Constraints

DATA-INT-001–004, DATA-WEAR-003/004, FR-WEAR-010, FR-PERS-013, DATA-RET-001/002 and FR-MULT-008; BRULE-GAR-009, BRULE-WEAR-005/006, BRULE-PERS-006 and BRULE-MULT-004–009; UC-010/014/016/017/019/022 and their available activities. Optional measurement failure does not change core acceptance (FR-MET-007); physical deletion can follow its deadline while effective exclusion is immediate.

#### Evaluation Criteria

Owned public interactions; immediate invariant preservation; truthful accepted/failed/uncertain outcomes; same-basis current reads; explicit transaction and failure behavior; event durability/idempotency where required; local operating simplicity.

#### Option A — Direct internal access

Modules freely invoke neighboring internal services, repositories or entities. This is quick and offers efficient local calls and joins; it can work when the responsibilities truly form one module. Across declared contexts it makes private implementation a de facto public API, hides authority and transaction dependencies and permits lifecycle/currentness shortcuts. A shared database does not eliminate these risks.

#### Option B — Explicit published module contracts with semantic choice of synchronous or event-based communication

Use a narrow local contract/facade/port when the caller needs current authoritative information, a result or coordinated acceptance before completing its operation. Ownership and authorization apply through that boundary. Use domain/application events for facts and reactions whose semantics fit an event model; asynchronous work is appropriate only when delayed effects are permitted or a deliberate visibility/recovery mechanism preserves every immediate obligation.

Business semantics determine interaction style, then technology/transport. An event is not automatically asynchronous: delivery may be synchronous within an explicitly coordinated local operation. For example, Spring application listeners are synchronous by default, while transaction-bound listeners can target commit phases. Neither API choice establishes durable delivery after restart. See [Spring application events](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html) and [transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html). These are feasibility examples, not chosen event classes/configuration.

| Existing Semantic Need | Proposed Local Interaction Direction |
| --- | --- |
| Recommendation/Coverage/Multiplier needs current wardrobe/context | Published authorized read contracts, with coherent evaluated basis/currentness checks; not a new copied-state store. |
| Accept garment removal or Wear correction/removal | Explicitly coordinate owned mutation and effective consumers before claiming the required accepted outcome, using coherent derived reads or required coordinated changes. |
| Logout/reset affects protected use | Preserve the relevant usable-authority checks and revocation boundary; a later optional listener cannot authorize stale access. |
| Optional measurement reaction | May be independent/event-based; failure cannot undo or fabricate core acceptance. |
| Required physical deletion by its deadline | Recoverable, idempotent tracked work as needed under ADA-012; an in-memory signal alone is insufficient. |

These examples allocate interaction principles, not final modules or runtime sequences. A contract must not return mutable private entities or hand the caller unrestricted repository/schema access (ADA-016/018). A successful local call alone does not prove coherent multi-call reads under concurrency; assessment basis and transaction/concurrency design remain explicit ADA-009/010 responsibilities.

#### Option C — Simulate distributed services now

Use internal HTTP or broker hops between modules in the same application/deployment to mimic possible future services. Explicit wire contracts can be useful if an independently deployed consumer or real process boundary already requires them. Used solely within this local business core, they add serialization, latency, retries, private-data flow and partial failure without demonstrated scaling/deployment benefit. This comparison does not reject ADA-004's genuinely analyzed separate AI process.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Ownership/contract clarity | Weak across contexts | Explicit and controlled | Explicit wire interface, extra failure semantics |
| Immediate accepted effects | Possible, hidden coupling | Deliberate local coordination | More partial-failure/consistency work |
| Current-state reads | Fast, private persistence exposed | Local authorized contract; coherent basis required | Added remote/wire cost |
| Events/recovery | Ad hoc | Semantics and durability made explicit | Transport does not solve application obligations |
| MVP operation/evolution | Simple now, harder boundary recovery | Local simplicity with visible seams | Distributed complexity before a driver |

#### Recommendation

Option B — Explicit published module contracts, choosing synchronous local interaction or event-based interaction according to business semantics

#### Why Recommended

It makes acceptance and ownership reviewable while retaining local speed and useful transactions. Removal must immediately stop current influence; replacing that invariant with eventual consistency merely to prepare for microservices would contradict existing sources. ADA-016 prevents the shared-database shortcut, ADA-018 protects meanings and ADA-012 keeps recoverable later work explicit.

#### Recommendation Confidence

High — the current immediate/coherent effects and bounded optional reactions provide clear selection criteria. Actual contracts, transaction participation, dependency cycles and concurrent-basis handling still need design and tests.

#### Decision Status

Selected

#### Consequence If Selected

ADD/Logical/Implementation Views would describe published interactions, controlled dependency direction and deliberately coordinated immediate versus later work. Future extraction could replace a local Java contract/port/facade with an HTTP/REST/gRPC adapter, or an appropriate local event with a separately designed Integration Event and broker delivery. Remote timeout, authorization, compatibility, reliable publication and consistency would require fresh analysis; current guarantees remain binding.

#### Deferred Details

Exact contracts, DTOs, call graphs, domain/application event classes, listener execution/transaction phases, timeout/retry rules, any reliable publication mechanism and detailed sequences follow downstream design. No internal HTTP, Kafka/RabbitMQ, Saga or Outbox is selected.

### ADA-018 — Context Model Isolation and Anti-Corruption

#### Problem

How can CapsuleAI prevent neighboring context internals, external provider models and infrastructure DTOs from becoming a universal domain model throughout the application?

#### Why This Decision Is Needed Now

**Important Before ADD.** Conceptual Integrity and Interoperability need a model-isolation direction alongside public boundaries. The semantic policy normally precedes ADD/Implementation View; final translations/classes are not Critical choices. A safe deferral must retain private internal models and explicit public language, with Architect/Backend ownership and resolution before affected contracts/Implementation View are baselined.

#### Related ASRs

ASR-CON-001, ASR-INT-001, ASR-SEC-002, ASR-REL-001, ASR-USE-001, ASR-TEST-001.

#### Relevant Quality Attributes

Reliability and Security — Critical; Conceptual Integrity, Interoperability, Usability and Testability — High; Maintainability and Flexibility — Medium.

#### Relevant Constraints

DATA-GAR-003, DATA-OUT-001–003, DATA-ANL-003/004, DATA-HIST-001/002, DATA-INT-001–003, SI-001–004 and LOC-002/003; BRULE-GAR-001/007/008, BRULE-HIST-002, BRULE-MULT-004–009 and BRULE-SHOP-001–003. AI proposal versus confirmed truth, current versus historical/hypothetical information, and qualified commercial evidence must retain different meanings.

#### Evaluation Criteria

Private internal models; narrow published meanings and necessary disclosure; consistent identity/validity despite distinct representations; provider/context translation tests; explicit ownership of shared semantics; mapping cost without speculative stored copies.

#### Option A — Share internal entities/models directly

Let a recommendation responsibility import a wardrobe owner's internal Garment/JPA entity, or propagate provider DTOs into business decisions. This avoids mappings and can fit a deliberately unified small responsibility. Across declared boundaries it ties consumers to persistence fields, mutability and provider vocabulary; unconfirmed, historical or hypothetical information can be mistaken for current authoritative ownership. A later internal representation change becomes a broad integration change.

#### Option B — Published contracts plus Anti-Corruption Layer translation

Keep each context's internal domain/persistence model private. Publish only the meanings required by permitted consumers; translate between that contract and the consumer's model when semantics differ. An ACL protects interpretation, while a port defines the interaction dependency and an adapter implements access/transport. One small adapter may perform both access and semantic translation, but those responsibilities are distinct.

[Eric Evans's DDD Reference](https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf), Anticorruption Layer and Published Language, describes model translation across boundaries; [Martin Fowler's bounded-context explanation](https://martinfowler.com/bliki/BoundedContext.html) supports explicit models and relationships. Applying these ideas to CapsuleAI is an architecture recommendation, not evidence that any context map has been approved.

Illustratively, an owned internal Garment might yield a narrower recommendation-facing garment representation carrying relevant identity, confirmed attributes, readiness and authority/currentness evidence. Names such as RecommendationGarment, GarmentAnalysisProposal and WeatherSnapshot describe possible meanings only; no DTO/class is prescribed.

| Boundary Example | Meaning Protected by Translation |
| --- | --- |
| Wardrobe-owned model to a recommendation-facing contract | Relevant current owned information; no internal persistence entity/repository or unrelated profile fields exposed. |
| Python AI response to a garment-analysis proposal | Preview/proposal, confidence and provenance remain assistance; no automatic confirmation or provider-specific DTO leakage. |
| Weather response to CapsuleAI environmental evidence | Source/time, actual usable context and unavailable/stale meaning; WeatherSnapshot as an example name does not require a stored projection. |
| Shopping provider model to commercial evidence | Credible source/last-checked time, qualified optional values and external navigation; no invented purchase or ownership. |
| Object-storage SDK to media abstraction | Private authorized access, lifecycle and copy/version outcomes; object existence is not user authority. |

The cost is maintaining focused mappings and contract fixtures. Small intentional duplication of boundary representations can be preferable to coupling every context to one object. Do not duplicate canonical outfit identity/validity rules independently: assign an explicit semantic owner/published capability or deliberately governed narrow shared rule responsibility in later design (ADA-010/015). Shared meaning does not require a universal User/Garment/Wear entity library.

#### Option C — Shared universal domain model/common library

A common package of User, Garment, Wear and other models reduces mapping and can keep compatible simple representations aligned. A small stable shared concept can be justified where ownership and change control are clear. Making every context depend on the full common model creates coordinated releases, persistence/provider leakage and conflicting purposes for the same object. Shared vocabulary or identity alone does not justify a universal domain package.

#### Comparative Trade-off Matrix

| Criterion | Option A | Option B | Option C |
| --- | --- | --- | --- |
| Internal/provider isolation | Weak across meaningful boundaries | Explicit narrow contracts/translation | Weak if the model is universal |
| Semantic preservation | Dependent on imported model assumptions | Focused and testable | One model may conflate different purposes |
| Mapping effort | Lowest initially | Small intentional boundary cost | Low initially, broad change coordination |
| Shared validity/identity | Reuse can leak internals | Governed meaning with private representations | Consistency possible, ownership easily blurred |
| Future extraction/change | Broad representation dependencies | Visible contract/translation seams | Common model couples services/releases |

#### Recommendation

Option B — Published contracts + Anti-Corruption Layer translation at meaningful semantic boundaries

#### Why Recommended

The actual differences in authority, evidence and lifecycle justify explicit language at provider and neighboring boundaries. Avoid unnecessary mappers when meanings already align; translate real semantic differences and preserve required evidence. This complements ADA-006's ports/adapters and ADA-015's inward dependencies without defining new modules or exposing every model as shared.

#### Recommendation Confidence

High — confirmed/proposed, current/historical/hypothetical and optional commerce distinctions are established sources. Exact translation granularity and domain ownership still need the views and contract evidence.

#### Decision Status

Selected

#### ACL Is Not a Snapshot or Projection

An ACL translates meaning; it does not require storage, replay or a copied read model. Under ADA-010/016/017, current authoritative information should normally come through direct published local contracts, with coherent basis checks. No Recommendation wardrobe/behavior snapshot stores or user snapshots everywhere are introduced.

This does not remove the **existing required minimal historical snapshots** (DATA-HIST-001/002 and BRULE-HIST-002). Preserving past meaning is a different purpose from maintaining a copied current read model.

Future extraction might justify projections when remote-call latency, autonomy, read/change imbalance, availability needs or measured chatty interactions warrant local copies and eventual consistency is acceptable for the affected behavior. Such a proposal must address reliable publication (possibly Outbox), delivery, idempotent consumers, ordering, replay, rebuild, ownership/privacy and stale-state semantics. Immediate current exclusions still need a solution; a projection cannot weaken them.

#### Consequence If Selected

ADD/views would separate owned internal models, published language and necessary translations. Contract/semantic regressions would protect authority, identity, evidence, missing/outdated states and necessary disclosure. Model isolation would reduce avoidable extraction coupling without requiring duplicate authoritative stores.

#### Deferred Details

Exact DTO/class names, public language, context map, mapper placement, shared-rule governance, any snapshot/projection tables, integration-event schemas and contract/version details follow ADD/views/ADRs and detailed design. Projections, CQRS, Event Sourcing and distributed mechanisms need evidence and separate re-analysis when justified.

## 6. Decisions That Do NOT Need to Be Made Yet

These are **Defer to Detailed Design / ADR** concerns unless new evidence makes a major boundary change necessary. Deferral is not permission to omit required behavior.

| Deferred Concern | Why not selected here / later responsibility |
| --- | --- |
| Mobile implementation framework | Supported platforms, accessible Vietnamese outcomes and canonical meanings are fixed; framework is open. Mobile/Architect contributors should compare compatibility and integration feasibility before the dependent Implementation View baseline. No React Native preference or supported-version assumption is made. |
| Exact bounded-context decomposition, module names and package structure | ADA-001/015–018 analyze organization, dependencies, ownership, communication and isolation policies; ADD/Logical/Implementation Views define the domain-first map and actual source organization after human selection. No one-module/one-service mapping is assumed. |
| Exact Java classes/interfaces and boundary DTOs | Implementation View/detailed design choose the smallest useful public contracts, ports and translations; no textbook folder layout or universal domain library is prescribed. |
| Exact inference model/framework, licenses and CPU/GPU | AI contributors must establish packaging, quality and full-result feasibility. If findings overturn ADA-004/005/013, revisit analysis/selection before dependent design. |
| Exact PostgreSQL schemas, tables, types, indexes, ORM, isolation/locking and cross-schema FK policy | ADA-003/016 distinguish primary persistence from logical ownership. Data View/detailed design realize integrity, controlled exceptions and transaction participation; no schema name, table or blanket cross-context FK ban is defined here. |
| Exact local contracts, HTTP/gRPC payloads, errors and retry/action keys | Views and detailed API/service design realize current behavior, inward dependencies, published language and durable logical-action semantics after selection. Future remote transport is not a current internal-module requirement. |
| Exact domain/application-event classes and integration-event schemas | ADA-012/017 analyze semantics; views/detailed design decide any needed local events. Future integration identities, schema evolution, delivery/idempotency and replay need evidence before a wire contract is designed. |
| Outbox implementation, Saga design and snapshot/projection tables | Not adopted for hypothetical extraction or because an ACL exists. Required historical snapshots remain governed by SRS/rules; copied current read models and reliable distributed workflows need justified current or future re-analysis. |
| Enumeration/ranking optimizations and derived caches | Profile relevant workloads and preserve identity, basis and completeness. No arbitrary cut-off or Multiplier latency is added. |
| Object-storage provider, image delivery and deletion mechanisms | Views/design must realize private effective access and applicable physical purge. Necessary copy/version control is a required design concern even though provider is open. |
| Kafka/RabbitMQ or other broker, CQRS and Event Sourcing | No current need selects these mechanisms. Reconsider only when concrete semantic/workload evidence warrants their coordination cost; a local Domain Event is not a selected Integration Event platform. |
| API Gateway, service discovery, service mesh and Kubernetes | No speculative distributed topology is selected. A future extraction proposal must demonstrate relevant operating/availability drivers before re-analysis, selection and views/design. |
| Distributed tracing vendor/platform, local log fields/retention and correlation realization | ADA-014 remains minimal protected evidence. Detailed design/Test Strategy/engineering preparation define needed observation; no ordinary diagnostic store may become an indefinite user-linked measurement copy. |
| Host counts/sizes, network layout and operating commands | Deployment View resolves resources and recovery from evidence. No cloud provider, production SLA or mandatory multi-host topology is set. |
| Detailed sequences/states, executable test strategy and CI tooling | Follow their downstream stages; this analysis does not create them or claim software verification. |

A major alternative that affects authoritative state, security, exactness or runtime boundaries cannot be silently relegated to implementation. Bring such evidence back to the relevant ADA and explicit selection record.

## 7. Cross-Decision Interactions

These v0.2 analytical interactions and conditional evolution possibilities remain unchanged. All current recommended options are now Selected in the [selection record](selected-architecture-decisions.md); this section selects no additional infrastructure.

| Interaction | Consequence for human review and later design |
| --- | --- |
| ADA-001 ↔ ADA-015 ↔ ADA-016 ↔ ADA-017 ↔ ADA-018 | Together establish the proposed service-extractable modularity: domain-first responsibility boundaries, inward dependencies, owned data, public semantic interactions and protected models. They preserve a local business core rather than select a distributed system. |
| ADA-003 ↔ ADA-016 | Database family and ownership are separate choices. One PostgreSQL database permits useful coordinated local transactions, not unrestricted cross-context persistence access; physical enforcement/schema policy follows Data View. |
| ADA-006 ↔ ADA-018 | Ports/adapters isolate actual external dependencies; an ACL protects meaning when models differ. An adapter may implement both, but neither requires a copied-state projection. |
| ADA-009 ↔ ADA-016/017 | Durable action identity, owned mutation contracts and coordinated accepted effects determine transaction boundaries. Multi-context local commits can be useful while documenting future extraction coupling; media/email still need recoverable cross-resource handling. |
| ADA-012 ↔ ADA-017/018 | Local domain/application events must fit permitted effect timing. Future Integration Events need explicit published semantics, identity/version/delivery and consumer recovery; no broker or automatic event-object-to-payload conversion is implied. |
| ADA-015 ↔ ADA-006/018 | Clean Architecture supplies inward dependency direction; ports/adapters supply access seams and ACL supplies semantic translation. Avoid ceremonial interfaces/mappings where no real boundary exists. |
| ADA-010 ↔ ADA-016/017/018 | Current owner contracts and explicit semantic ownership preserve validity/identity and evaluated basis across distinct models. No universal entity library, unrestricted joins or default wardrobe/behavior projection stores. |
| ADA-001 ↔ ADA-003/008/009 | Modular local coordination relies on a coherent authoritative persistence direction. Selecting distributed business services changes mutation/session/recovery costs and requires renewed analysis; it is not a harmless code-organization substitution. |
| ADA-002 ↔ ADA-004/005/013 | Core runtime, model compatibility, analysis communication and resources must fit together. Python in another process does not require separate hosts; co-location does not remove resource contention. Review complete processing evidence and contributor effort as a set. |
| ADA-003 ↔ ADA-007/008/009/012 | Primary transactions can coordinate sensitive business state, but media/email and physical cleanup remain cross-resource responsibilities. No cache/broker recommendation eliminates durable recovery or lifecycle deadlines. |
| ADA-010 ↔ ADA-009/011/014 | Accepted corrections/removals and current evaluation basis govern derived reuse. Benchmark optimization cannot replace full Multiplier evidence with displayed daily choices or a cached prior count. |
| ADA-006 ↔ ADA-005/007/014 | Boundaries must retain truthful dependency outcomes, minimal disclosure, authorized private media and inspectable complete timing. A provider adapter cannot excuse a bearer-access or lifecycle mismatch. |
| ADA-013 ↔ ADA-014 and all Critical decisions | Recovery evidence needs durable accepted state and repeatable operation. Minimal deployment is conditional on actual resources; monitoring tools alone do not prove resilience. |
| All choices ↔ ASR-USE-001 | Backend outcomes must retain confidence/provenance, Saveable/readiness, uncertain mutation and non-exact assessment meaning. Mobile accessibility/usability remains integrated work, not a framework side effect. |

If the human overrides an option, evaluate these dependent recommendations rather than mixing incompatible assumptions. No individual High-confidence recommendation makes the whole set selected.

### 7.1 Conditional Evolution Map

This map tests boundary quality; it is not a module decomposition, migration commitment or deployment plan. Extract only where a real driver justifies the added consistency, security and operating work; cohesive contexts need not map one-to-one to modules or services.

| Proposed Local Boundary / Responsibility | Possible Future Realization if Extraction Is Justified |
| --- | --- |
| Cohesive context/module with private internals | Candidate service boundary, possibly grouping responsibilities that must remain coordinated. |
| Public Java module contract / local port or facade | Published HTTP/REST/gRPC contract and remote adapter with explicit failure/authorization semantics. |
| Domain/application fact and permitted in-memory reaction | Separately designed Integration Event; broker delivery such as Kafka/RabbitMQ only if justified. |
| Context-owned logical data in shared primary persistence | Physical service-owned data, possibly Database-per-Service, with deliberate migration/lifecycle handling. |
| Local ACID acceptance / multi-context coordinated workflow | Local service transactions; cross-service guarantees may need reliable events or Saga/compensation where business semantics permit. |
| Needed reliable cross-service event publication | Outbox or another evidenced mechanism; not selected simply for modularity. |
| Protected local evidence and necessary core/AI correlation | Cross-service correlation/observability if independent runtimes require it; no tracing vendor selected. |
| Deliberate module boundaries and inward adapters | Incremental extraction, potentially Strangler or Branch-by-Abstraction techniques, with explicit data/contract/consistency transition work. |

## 8. Human Selection and Historical Review Guidance

**Current state — 2026-10-10:** the Project Owner / Backend Developer accepted all 18 current recommendations exactly as analyzed. ADA-001–018 are **Selected**, with no override, deferral or supersession. [Selected Architecture Decisions](selected-architecture-decisions.md) records the exact choices; [ADD](ADD.md) realizes them. [Logical View v0.1](views/logical-view.puml) is the human-accepted canonical decomposition; [Implementation View v0.1 — Baseline Draft](views/implementation-view.puml) proposes its source realization for review. Deployment View follows review. All eight Critical and ten Important choices remain selected; evidence gaps remain realization/verification work.

### Historical v0.2 Review Guidance

The following text preserves the review guidance written before this human selection. References to pending selection or a future record describe that historical state, not the current handoff.

**Not yet selected.** Section 4 is the compact proposed set: ADA-001–014 recommend Option A and ADA-015–018 recommend Option B; each letter's meaning differs by problem. Option letters indicate presentation order, not a scoring or automatic-selection rule. The option analyses explain when credible alternatives could be preferable.

Resolve **ADA-001/003/004/008/009/010/016/017 (Critical Before ADD)** explicitly. The added Critical choices establish ownership and interaction/acceptance policies, not exact schemas or a finalized context map. Review Important decisions together with their dependencies; any safe deferral must name the responsible role, known boundary/impact, evidence needed and resolution point before dependent architecture/design. Do not defer a Critical decision merely because it is difficult.

Review ADA-001/015–018 as one boundary philosophy, with ADA-003/006/009/012 supplying persistence, external integration, acceptance and event/recovery implications. Clean Architecture is Important because dependency direction can be bounded without fixing packages; ACL/model isolation is Important because public semantics can be bounded without final DTOs. Neither classification upgrades Maintainability/Flexibility from Medium or creates another ASR.

| New Analysis | Review / Safe Important Deferral Boundary | Responsible Role and Resolution Point |
| --- | --- | --- |
| ADA-015 | Evaluate contributor understanding and a representative domain/application boundary; if deferred, explicitly bound dependency isolation and avoid assuming framework entities as domain contracts. | Architect/Tech Lead with Backend; resolve before dependent Implementation View baseline. |
| ADA-016 | Select an ownership policy before ADD; logical sharing, owner operations and exceptions must fit ADA-003/009/017. Exact decomposition/schema/FK design follows views. | Architect/Tech Lead with Backend/Data and privacy review; Critical policy blocks ADD if unresolved. |
| ADA-017 | Select public-contract and immediate/later-effect policy before ADD; check concurrency/basis, retry and recovery against UC-010/014/016/017/022. | Architect/Tech Lead with Backend/QA; Critical collaboration policy blocks ADD if unresolved. |
| ADA-018 | Establish public language/model-isolation direction; a bounded deferral identifies private models and real translation points without choosing DTOs or projection stores. | Architect/Tech Lead with Backend/AI/Mobile as relevant; resolve before affected Implementation View/contracts. |

These are criteria for a future human-recorded deferral, not deferrals granted by this analysis.

The most material evidence gaps are runtime/team suitability (ADA-002), actual model packaging/quality/resource demand (ADA-004/005/013), computation cost (ADA-010), and private media access/deletion/cost feasibility (ADA-007). The new boundary policies also need contributor proficiency, representative contract/translation reviews and final domain responsibility evidence; these gaps principally limit ADA-015 confidence and later realization detail. Selection does not claim that required benchmarks have passed. Later design and Test Strategy establish how to demonstrate them.

The future `selected-architecture-decisions.md` should record ADA ID, selected analyzed option, selection basis/override reason, Backend Impact and status (Selected, Deferred or Superseded), plus selecting human/review date and bounded-deferral details where applicable. It should link to these analyses, not repeat them, and is not an ADR replacement. That artifact is deliberately **not created here**.

## 9. Backend Developer Takeaways

- The selected business-core direction is a modular monolith, governed by [Selected Architecture Decisions](selected-architecture-decisions.md); there is no implemented deployment baseline. Service-extractable module boundaries start with domain responsibilities rather than a list of future services.
- Pragmatic Clean Architecture governs inward dependencies inside modules; use meaningful ports/adapters and keep Spring/JPA/provider/transport details outside domain decisions. No interface-per-class or empty-layer ceremony.
- Each context logically owns its persisted information even in one PostgreSQL database. Reach another owner through public contracts, not its private repositories, entities, tables or schemas.
- Synchronous versus asynchronous interaction follows business semantics. Immediate exclusion/revocation and accepted effects stay coherent; local events can be useful without requiring a broker.
- ACL protects neighboring/provider meanings through focused translation. It does not automatically create a snapshot/projection; required minimal historical snapshots have a separate purpose.
- Local ACID transactions remain useful. Coordinated cross-context transactions create future extraction coupling to review; Kafka/RabbitMQ, Outbox and Saga are potential later mechanisms, not present requirements.

- The selected modular core and relational authority reduce coordination surfaces; they still require deliberate transaction, ownership and concurrency design.
- Framework JWT validation does not replace current-session/account authority. Reset, logout and refresh reuse have different effects.
- A durable logical-action identity protects retries; it must not collapse new same-outfit/day Wear intentions. Original local grouping and absolute elapsed aging remain separate.
- AI is selected as a private analysis boundary; user confirmation remains authoritative. Synchronous interaction fits the short complete-result target provisionally, subject to measurement.
- Request-time intelligence avoids premature invalidation machinery. Shared validity/identity is necessary, while different consumers may use different algorithms. Exact Multiplier needs complete same-basis sets.
- Redis, a broker and orchestration add correctness/operating work; measured need can justify them later. Required cleanup and recovery exist even without those services.
- Private media and operating evidence need purpose, authority and lifecycle design. Object existence, a deletion marker or a successful telemetry response cannot establish business acceptance.
- Product ordering remains PO-accountable. Developers contribute feasibility and operating implications; the separate selection record fixes architecture directions, while this draft commits no Sprint work.

## 10. Traceability

This matrix maps each existing formal ASR to decision problems. It adds no requirements and leaves the ASR's own detailed source/scenario traceability authoritative.

| Formal ASR | Relevant ADA Problems | Main Existing Evidence Anchors |
| --- | --- | --- |
| ASR-SEC-001 | ADA-001/002/003/006/007/008/011/016/017 | QA-SEC-01/02; FR-AUTH-004/007/009/013/014/017/018; DATA-AUTH-005 |
| ASR-SEC-002 | ADA-003/004/006/007/009/011/012/013/014/016/017/018 | QA-SEC-03; NFR-PRIV-001/002; DATA-RET-001–003; SI/COM purpose boundaries |
| ASR-REL-001 | ADA-001/002/003/004/005/007/008/009/010/011/012/015/016/017/018 | QA-REL-01/02, QA-SCA-01; DATA-INT-001–004; BRULE-WEAR/PERS |
| ASR-CON-001 | ADA-001/002/003/009/010/011/014/015/016/017/018 | QA-CON-01; BRULE-OUT/COV/MULT; DATA-OUT-001/002; DATA-ANL-004 |
| ASR-PERF-001 | ADA-002/004/005/007/010/011/013/014/017 | QA-PERF-01/02; NFR-PERF-001/002; NFR-TEST-003; SRS Section 12.3 |
| ASR-INT-001 | ADA-004/005/006/012/013/015/017/018 | QA-INT-01, QA-AVL-01; SI-001–004; UC-004/006/007/023 |
| ASR-REL-002 | ADA-001/003/004/007/008/009/012/013/014/016/017 | QA-REL-03, QA-SUP-01; NFR-REL-003; applicable DoD operational criteria |
| ASR-USE-001 | ADA-005/006/010/014/015/018; outcome semantics across all choices | QA-USE-01/02, QA-MNT-01; NFR-USE/ACC; LOC-001–005 |
| ASR-TEST-001 | ADA-001/002/004/005/006/008/009/010/012/013/014/015/016/017/018 | QA-TEST-01, QA-SUP-01; NFR-TEST-001–003; AI-REQ-010/011/014; DoD Sections 4–6 |

For all 14 QA classifications, use QA analysis Section 5; the per-ADA tables name the attributes relevant to that comparison. Availability/Scalability and other Medium attributes remain real obligations without automatically requiring redundancy or distributed infrastructure. Reusability is Low and supplies no separate reusable-platform decision.

The inverse mapping below makes the new decision problems reviewable without creating new ASRs or QA scenarios.

| New ADA | Existing Formal ASRs | Existing QA / Requirement / Interaction Evidence |
| --- | --- | --- |
| ADA-015 | ASR-CON-001, ASR-TEST-001, ASR-INT-001, ASR-REL-001, ASR-USE-001 | QA-CON-01, QA-TEST-01, QA-REL-01/02, QA-INT-01, QA-MNT-01; DATA-INT-001–004, NFR-REL-002, NFR-TEST-001/002, LOC-002/003; UC-007/011/019/022 and corresponding activities; DoD Sections 4–6. |
| ADA-016 | ASR-SEC-001/002, ASR-REL-001/002, ASR-CON-001, ASR-TEST-001 | QA-SEC-01–03, QA-REL-02/03, QA-CON-01, QA-SCA-01; DATA-AUTH-005, DATA-GAR and DATA-WEAR information domains (SRS Sections 4.3/4.5), DATA-INT-001–004, DATA-RET-001–003; UC-010/014/016/017 and AD-014/016/017. |
| ADA-017 | ASR-REL-001/002, ASR-SEC-001/002, ASR-CON-001, ASR-INT-001, ASR-PERF-001, ASR-TEST-001 | QA-REL-01–03, QA-SEC-01–03, QA-CON-01, QA-INT-01, QA-PERF-01/02, QA-SCA-01; DATA-INT and DATA-WEAR families, FR-WEAR-010, FR-PERS-013, FR-MULT-008, DATA-RET-001/002, FR-MET-007; UC-010/014/016/017/019/022 and available activities. |
| ADA-018 | ASR-CON-001, ASR-INT-001, ASR-SEC-002, ASR-REL-001, ASR-USE-001, ASR-TEST-001 | QA-CON-01, QA-INT-01, QA-SEC-03, QA-REL-01/02, QA-MNT-01, QA-USE-02; DATA-GAR-003, DATA-OUT-001–003, DATA-ANL-003/004, DATA-HIST-001/002, SI-001–004, LOC-002/003; UC-006/007/011/015/021–023 and available activities. |

Reliability/Security remain Critical; Conceptual Integrity/Interoperability/Testability and other High concerns remain High; Maintainability/Flexibility/Manageability remain Medium. Shared semantic preservation, isolation and evidence support these recommendations; no independent microservices, generic-reuse or change-effort requirement is inferred.

## 11. Status and Next Step

**Version 0.2.4 — Baseline Draft. ADA-001–018 Selected.** Explicit human selection on 2026-10-10 remains in [Selected Architecture Decisions](selected-architecture-decisions.md). Current navigation acknowledges ADD, the human-accepted Logical decomposition and [Implementation View v0.1 — Baseline Draft](views/implementation-view.puml). Analytical recommendations, trade-offs, importance, confidence and evidence gaps remain unchanged; no additional approval, software or benchmark claim.

**Implementation View review → Deployment View** is the current handoff. [Logical View v0.1](views/logical-view.puml) is the human-accepted canonical decomposition; [Implementation View v0.1 — Baseline Draft](views/implementation-view.puml) proposes its source realization for review. Then follow Deployment View → Data View → ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation. All Critical/Important recommendations remain Selected without override or deferral.

This revision synchronizes current navigation only; ADD, Logical and Implementation Views exist separately. Historical revisions and preselection guidance remain visible; all ADA analytical content/statuses are unchanged. No product requirement, QA scenario or ASR obligation changes. Deployment/Data Views, ADRs, detailed design, Test Strategy, Sprint artifacts and code remain downstream.
