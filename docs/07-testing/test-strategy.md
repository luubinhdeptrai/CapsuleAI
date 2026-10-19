# CapsuleAI — Test Strategy

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Project-wide Test Strategy |
| Version | 0.1 |
| Status | Baseline Draft |
| Date | 2026-10-20 |
| Ownership | Developers / Tech Lead with QA; AI and Mobile contributors own their relevant verification evidence; Product/BA clarify source behavior |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 26–33, 40 and 46 |
| Completion Authority | [Definition of Done](../06-scrum/definition-of-done.md) |
| Authoritative Location | `docs/07-testing/test-strategy.md` |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-20 | Established the verification approach from current requirements, stories, quality obligations and selected architecture. No implementation, infrastructure configuration or executed verification is included. |

## 2. Purpose and Workflow Position

Define how CapsuleAI will obtain repeatable evidence that delivered behavior satisfies its requirements and protects existing behavior when code changes. A developer should be able to choose the appropriate test level, dependency setup and evidence for a selected Story without designing the entire MVP first.

The [workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 28/30–33/46, places this strategy after the system-wide requirements/backlog/architecture baseline and before **Pre-Sprint Engineering Baseline**. Refinement prepares feasible, testable candidates; Sprint Planning selects work. Necessary Just-In-Time (JIT) Detailed Design, automated tests and implementation then evolve together inside Sprint execution. **Architecture early, Detailed Design late.** This document selects neither Sprint scope nor an implementation schedule.

## 3. Scope and Authority

The verification horizon is the connected MVP in [BRD](../01-business/BRD.md) and [PRD](../02-product/PRD.md): private access, confirmed wardrobe information, valid personalized advice, reported use, Coverage/Gaps, and hypothetical candidate utility with optional external navigation. Initial stories cover only part of that horizon. Later capabilities receive executable tests as their work is selected and implemented; this strategy creates no new stories.

The [SRS](../03-requirements/SRS.md) owns precise behavior and numerical acceptance conditions; [Business Rules](../03-requirements/business-rules.md) own invariants; stories own their AC. QA/ASR identify significant evidence needs. [Selected Architecture Decisions](../04-architecture/selected-architecture-decisions.md), ADD and views constrain realization. Tests must expose discrepancies against these sources rather than preserve accidental implementation behavior. Report contradictions to the owning roles before changing the baseline.

At authoring, the repository contains documentation without a source, build, test or CI implementation baseline. Tooling below is a recommendation for later engineering work. Endpoints, DTOs, physical schemas, transaction mechanisms and detailed diagrams remain conditional selected-story JIT work. Separate baseline ADRs are not required; ADA/Selected Decisions retain decision authority. This strategy establishes no product feature, provider, runtime version or quality threshold.

## 4. Testing Principles

- **Test observable behavior and risk.** Assert business outcomes, invariants, accepted state and meaningful boundary interactions. Avoid coupling tests to private methods or incidental call order.
- **Keep the feedback loop short.** Many independent fast tests should detect mistakes before expensive environments are needed. Failures must identify the behavior, input and expected outcome.
- **Make execution deterministic.** Control time, randomness, data and dependency responses; no test relies on another test running first.
- **Use real technology where compatibility matters.** PostgreSQL transactions and constraints need PostgreSQL evidence. A mock cannot establish that an adapter, migration or required integration works.
- **Control external dependencies.** Ordinary suites use local fakes/stubs and do not call Internet providers. Test doubles replace boundaries; they should not replace the very policy being verified.
- **Develop tests with implementation.** Use small design/test/implementation/refactoring steps. Selective TDD is useful; testing is continuous through integration, review and CI.
- **Protect privacy in fixtures and evidence.** Use synthetic accounts and approved nonpersonal image fixtures. Keep credentials, tokens, recovery secrets and unnecessary personal contents out of reports/logs.
- **Use automation for regression.** Change code → run the automated regression suite → detect broken existing behavior. Manual exploration and Postman aid investigation; repeatable regression belongs in automated checks.
- **Treat coverage as diagnostic information.** Missing high-risk behavior matters more than an arbitrary percentage. This strategy establishes no coverage target.

These principles implement Workflow Section 28, [DoD](../06-scrum/definition-of-done.md) Sections 4–6 and SRS NFR-TEST-001–003; they do not replace their acceptance conditions.

## 5. Test Portfolio and Developer Mental Model

Use the least expensive level that can establish the behavior, adding higher-level evidence for risks it cannot establish. Many fast domain/application tests, fewer infrastructure integration and API/module tests, and a small number of critical E2E journeys give quicker diagnosis and less fragile setup than checking every permutation through the UI. No fixed numerical distribution is imposed.

| Question / Boundary | Test Level | Typical Setup | What It Establishes |
| --- | --- | --- | --- |
| Is this domain rule correct? | Domain unit | Real policy and explicit values | Invariants, calculations and boundary outcomes |
| Does the use case coordinate the right outcome? | Application / use case | Real application/domain with port fakes or mocks | Acceptance, rejection, failure and permitted effects |
| Does the PostgreSQL or provider adapter behave correctly? | Integration / contract | Real adapter with disposable PostgreSQL or controlled protocol stub | Mapping, constraints, transactions and boundary compatibility |
| Does the HTTP/security boundary preserve the contract? | API / web | Focused web slice; selected full application checks | Validation, JSON, access enforcement and outcome translation |
| Are source dependencies allowed? | Architecture | Compiled application classes | Practical automated dependency restrictions |
| Can contributors deliver a critical user outcome together? | Selective E2E | Integrated client/core and required real components in a controlled environment | Journey-level integration and user-visible outcomes |

A test level describes its responsibility, not its filename or a requirement for a separate build module. A focused API test can be cheaper than a broad integration test; the portfolio is guidance, not a mandatory cost ordering for every test. Use simple suite classification in Section 18.

## 6. Domain Unit Testing

Run real domain policies without Spring, database, HTTP, external providers or Docker. For each changed rule, cover representative success, rejection, equality/edge boundaries and unknown information. Use parameterized cases where they clarify the same policy. Expected results must be derived independently from the rule, not by calling the production calculation again.

| Rule Area | Direct Verification Focus | Authority |
| --- | --- | --- |
| Garment truth and readiness | Confirmed category/color is Saveable without image/AI; required unknown information prevents only the evaluation needing it; layering/bulk are conditional; confidence never substitutes for confirmation. | BRULE-GAR-001–008 |
| Outfit validity and identity | Exact supported composition, eligible owned constituents, layering, bulk equality, environmental bands/fallback, severe-pattern/noise count, and permutation-invariant identity. Strong personalization cannot admit an invalid outfit. | BRULE-OUT-001–009 |
| Shuffle | Only the selected slot changes; other identities/context stay fixed; when no alternative exists, retain selection; no inferred Like/Dislike/Wear. | BRULE-OUT-010 |
| Feedback and Wear | Effective outfit-targeted feedback, reported Wear strength, retry versus new intention, one outfit/original-day ranking increment, survivor-anchor recalculation and elapsed-time aging. History count remains distinct from ranking normalization. | BRULE-PERS-001–006, BRULE-WEAR-001–006, BRULE-HIST-001–003 |
| Coverage and gaps | Positive-priority weighting, sufficient ready roles, zero versus insufficient information, mapped-occasion deduplication and capability gaps supported by evidence. | BRULE-COV-001–006, BRULE-GAP-001/002 |
| Wardrobe Multiplier | Complete same-basis unique-set difference; candidate-bearing previews; Exact/Evaluated Zero/Incomplete/Unavailable/Outdated; no ownership or Wear from hypothetical evaluation. | BRULE-MULT-001–009, BRULE-SHOP-002 |

Reuse the controlled fixtures in [Business Rules Sections 14–15](../03-requirements/business-rules.md): weighted Coverage 50, complete 41→58 sets giving +17, equal bulk, permutation identity, and survivor/aging boundaries. Formula-only fixtures do not prove enumeration: add small explicit garment/outfit sets with independently known members. Vary one relevant predicate while keeping others valid; do not accidentally test rejection for the wrong reason. For ranking, use controlled alternatives and equal conditions to verify the required influence; avoid asserting a universal exact ranking order unsupported by the rules.

## 7. Application / Use Case Testing

Instantiate the real application use case with real domain behavior and fake/mock persistence, clock, provider or neighboring public-contract ports. A **fake** is a small controllable implementation, a **stub** supplies chosen responses, and a **mock** can also verify significant interactions. These tests normally need no Spring context. Keep doubles consistent with the published contract; they are not persistence/concurrency proof.

Derive cases from the [Use Cases](../03-requirements/use-cases/), relevant [Activities](../03-requirements/activity-diagrams/) and selected Story AC. Check the returned outcome and state/effects: correct owner, explicit confirmation, accepted versus rejected/pending/uncertain work, relevant retry review, and independent optional effects. Check an interaction when it is part of the behavior, such as no AI/media request for imageless manual creation or no mutation after denied authorization. Avoid verifying every internal call.

For example, US-GAR-001 needs imageless confirmation, retained Saveable-but-unready information, cancellation, known failure and uncertain-save handling. Fake dependencies can expose those branches. Real PostgreSQL acceptance/retry tests and integrated mobile confirmation are separate evidence. For Wear and intelligence, test orchestration through published owners, coherent survivor effects, shared evaluation policy, complete same-basis inputs and truthful changed-basis outcomes. Optional measurement failure must not reverse accepted business work (ADD Sections 9.2/9.7/9.9).

## 8. Integration and PostgreSQL Testing

### 8.1 Database Compatibility and Lifecycle

Recommend **Testcontainers PostgreSQL** for database integration tests: it exercises a real disposable PostgreSQL instance, fitting the selected relational authority and exposing database-specific constraint/transaction behavior. It requires a supported container runtime for these suites, not for domain/application tests. [Official PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/) and [runtime requirements](https://java.testcontainers.org/supported_docker_environment/) describe that capability. Engineering Baseline will choose compatible, pinned versions and lifecycle configuration; this document configures nothing.

Use an isolated test database per run or worker, with explicit cleanup between cases. Sharing a container within a run can reduce startup cost if each test's data remains independent. Use disposable resources and allocated ports; never point tests at production or rely on a developer's persistent database. Parallel workers must have separate databases/schemas or demonstrably isolated data/reset behavior. Test namespaces are harness choices, not a new product schema policy. Do not silently skip a required integration suite when its runtime is missing.

If Flyway or another migration approach is adopted, apply the actual versioned application migrations. Test fresh initialization and relevant supported upgrades with representative existing data when schema changes occur. ORM-generated test schemas alone cannot verify migrations. H2 or another incompatible in-memory substitute must not be used as evidence for PostgreSQL semantics. ORM, migration tooling, database version and physical schema remain unresolved in the [Implementation](../04-architecture/views/implementation-view.puml) and [Data](../04-architecture/views/data-view.puml) Views.

### 8.2 What Database Tests Must Establish

| Concern | Verification Approach |
| --- | --- |
| Persistence adapters | Exercise real save/read/update/query behavior with deterministic fixtures; flush/reload or use a fresh access context where applicable so an in-memory object does not hide mapping defects. |
| Integrity | Verify the designed uniqueness, ownership associations, supported values and relationships against real constraints. Include concurrent normalized-email creation and logical-action/credential-consumption conflicts where relevant. Do not invent table/FK policies from conceptual data boxes. |
| Transactions | Verify accepted effects are coherent and failed work leaves the appropriate prior state. Exercise actual commit/rollback boundaries and independent connections; a test wrapped in automatic rollback cannot alone prove production commit behavior. |
| Concurrency | Coordinate competing operations explicitly and inspect committed outcomes: refresh/reset single success, retry deduplication, Wear survivor effects, removal versus new use, and assessment-basis changes. Assertions follow source semantics, with conflict responses/mechanisms resolved JIT. |
| Durability and required work | Verify committed accepted state and required association/deletion/retention work survive applicable restart/interruption and resume safely. In-memory fakes or timers cannot establish durability. |

Rollback-based cleanup is suitable for isolated repository cases; committed, multi-connection and restart tests need deliberate reset/disposable data. Verify both the resulting state and forbidden extra effects. PostgreSQL ACID does not make email/media effects atomic; test their recoverable coordination separately. [ADD Sections 9.1–9.4/9.9](../04-architecture/ADD.md), ADA-003/008/009/012/016–018 and DATA-INT-* govern these checks.

## 9. API and Security Testing

### 9.1 HTTP Boundary

For each introduced/changed API, verify request validation, serialized JSON meaning, success/error translation, authentication, affected-information authorization and controller-to-application integration. Check malformed/unsupported input and dependency failure without silently converting them to success, empty inventory or zero utility. Exact routes, DTOs and status contracts are selected-story JIT work; tests then follow the affected OpenAPI contract under Workflow Section 27.

Use a focused Spring web test facility appropriate to the selected stack: MockMvc for servlet MVC, or WebTestClient for WebFlux where that stack is chosen. Neither web stack nor both stacks are mandated here. Add selected full application/API checks with real persistence/security where slices cannot establish the integration. Spring documents the distinction between [focused test facilities and full application tests](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html).

### 9.2 Required Authentication and Authorization Scenarios

This matrix identifies verification obligations from [SRS Section 3.1.1](../03-requirements/SRS.md), FR-AUTH-001–018, DATA-AUTH-001/002/005, NFR-SEC-001–003, [BRULE-AUTH-001–007](../03-requirements/business-rules.md), UC-001–004, current authentication stories and ADA-008. It is not a new security policy.

| Area | Required Behavior to Exercise |
| --- | --- |
| Registration and identity | Missing fields/mismatched confirmation; optional setup does not gate access; trim/case-insensitive email identity without provider-alias merging; duplicate identity creates no second account. Password boundaries 11/12/128/129, spaces/Unicode preserved exactly, no mandatory character-class mix; test an approved denylist only if adopted. |
| Login | Correct account and exact password succeed; incomplete/wrong credentials grant no protected access. Account switch does not expose the prior user's content; failed access does not alter accepted personal information. |
| JWT plus persistent authority | Missing, malformed, invalid-signature and expired tokens are rejected; a valid unexpired token still requires usable authorized account/session state. Verify 15-minute access expiry and rejection after session revocation even while its JWT remains unexpired. No usable authority means no protected bypass. |
| Refresh | Successful renewal consumes the current credential and issues replacement access/refresh credentials. Rotation never extends the original maximum 30-day session. Reuse of a consumed rotated token revokes that affected session, including its newer refresh credential; another separate session remains distinct. |
| Logout | Current-session revocation ends its protected access/renewal; another separate session remains usable under its own validity. Accepted wardrobe/profile/history remains. Unconfirmed logout is not claimed complete and protected client content stays hidden. |
| Recovery initiation/delivery | Known/unknown email initial responses are equivalent and non-disclosing. Only necessary delivery information is sent. Request, issuance or email delivery alone does not change the password or perform completion-based session revocation. |
| Reset validity/replacement | Interaction expires at 30 minutes and permits one successful use. Successfully issuing a replacement invalidates all prior unused interactions for that account. Recheck at submission; an earlier valid opening does not extend validity. Invalid/expired/consumed/replaced/rejected attempts cannot change credentials. |
| Completed reset | Policy-valid new password becomes authoritative; reset interaction is consumed; all account Refresh Sessions are revoked and renewed authentication uses the new password. Preserve accepted personal state and other accounts. Exercise concurrent completion and interrupted-response/single-success behavior. |
| Ownership, media and disclosure | Exercise unauthenticated and cross-account reads/writes for affected protected operations, including images, feedback/history and assessments as delivered. Zero successful unauthorized accesses in agreed scenarios. No credential, token or recovery secret in unauthorized output, logs or measurement. |

Use clock-controlled boundaries just before, at and after expiry. Interrupted refresh must preserve the existing consumed-token reuse policy; do not invent a replay exception to make retry convenient. Current-session logout, affected-session reuse revocation and completed-reset account-wide revocation remain distinct.

### 9.3 Evidence Across Layers

| Layer | Security Responsibility |
| --- | --- |
| Unit policy | Identity/password rules, expiry and allowed lifecycle decisions using controlled time. |
| Application/security | Correct account/session targeting, denied-operation effects, recovery privacy and failure orchestration using boundary doubles. |
| API security | Real configured security chain and owner checks; denied/authorized outcomes and safe response contents. Focused identity helpers are useful for authorization cases. |
| PostgreSQL/session integration | Persisted revocation, atomic consumption/replacement, absolute lifetime and race/restart outcomes with real commits. |
| Critical security journeys | Integrated renewal, logout/account switching and completed recovery across client/core/persistence with controlled email delivery. |

Mock-user or mock-JWT helpers do not establish signature validation, expiry or real persistent session enforcement. Spring's [JWT test helper](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/oauth2.html) explicitly supplies a mock JWT. Include representative requests using signed test tokens and the actual validation/session path; do not disable filters or substitute every authority check merely to pass tests. Test keys/secrets remain disposable and isolated from deployed credentials. This testing reference selects no social login or external identity provider.

## 10. Architecture Testing

Recommend a small **ArchUnit** suite to check dependencies in compiled Java classes, using the actual package structure established later. Its [user guide](https://www.archunit.org/userguide/html/000_Index.html) documents dependency/layer/cycle checks. Rule authority remains [Selected Decisions ADA-015–018](../04-architecture/selected-architecture-decisions.md), [ADD Section 9.4](../04-architecture/ADD.md) and the [Implementation View](../04-architecture/views/implementation-view.puml).

- Domain code must not import Spring, persistence, HTTP/JSON or provider/adapter models. Application code depends inward on domain, its needed ports and published contract language rather than infrastructure implementations.
- Cross-module imports use the supplier's published `api`; private domain/application/port/adapter/repository/entity models must not leak. Public contracts stay framework-free and expose no private persistence models. Check allowed collaboration directions and prohibited context cycles against the view.
- Keep root bootstrap and module configuration at the outer wiring boundary. Each module's configuration may wire its own private implementations; root assembly delegates through configuration. A blanket prohibition that rejects the view's legitimate wiring would be incorrect.
- Preserve the published Outfit Evaluation Semantics within Outfit Advice and Use. Wardrobe Intelligence consumes caller-input policy meaning, not private entities or Daily's ranked subset. Dependency checks support that structure; semantic regression establishes shared validity and exactness.

Review justified ownership exceptions through the owning design and established change process; do not add broad allowlists to silence failures. Static tests cannot prove absence of unauthorized SQL/table access, correct transactions, runtime authorization or semantic compatibility. Pair them with integration/security tests and review. No additional module framework, build split, internal HTTP, separate databases, cache or broker is introduced to enable testing.

## 11. External Dependency and Contract Testing

Ordinary domain/application suites use controlled local doubles. Adapter tests exercise the real adapter against a local protocol stub or an appropriate isolated resource, including serialization, translation, failure and minimal disclosure. When a real provider/contract is chosen, add compatible provider/sandbox checks under explicit isolated credentials and controlled execution; keep Internet-dependent checks out of ordinary deterministic regression. A fake or emulator result alone cannot prove the actual required integration works.

| Existing Boundary | Controlled Cases and Later Integration Evidence |
| --- | --- |
| Private Python analysis | Complete preview/proposals, partial/malformed/unsupported output, uncertainty, delayed/failed communication and late results; see Section 12. Verify real core/Python compatibility when that contract is introduced. |
| Weather | Actual timestamp/location translation, exactly 30-minute versus older evidence, refresh and the 2-second maximum assessment delay; disclose unavailable context with remaining validity preserved. UC-006 remains acquisition authority. Verify no unrelated wardrobe/profile/history is sent. |
| Private object storage | Correct media association, denied access, unavailable/missing media, interrupted association/purge and applicable copies/versions. Real provider checks must establish private delivery and deletion behavior; an object key/existing object is not authorization. Manual imageless use remains independent. |
| Recovery email | Necessary recipient/instructions only, delivery success/failure/delay, no secret leakage or account disclosure through initiation. Use a controlled mailbox/delivery substitute in regression; real adapter compatibility is separate from reset completion. |
| Optional commercial evidence/navigation | Credible candidate source, source/time preservation, price/availability current through 24 hours inclusive and stale qualification/omission afterward. Missing destination affects navigation only; supported utility remains. Explicit client handoff creates no purchase, ownership or Wear and sends no unrelated private data. |

Applicable adapter cases include connection failure, timeout, malformed/partial payload and provider rejection where the chosen interface supports them. Select numeric waiting/retry mechanisms only during relevant JIT design; the five-second processing benchmark is not a universal AI timeout. Optional weather/shopping/measurement failure must not disable otherwise usable core paths; necessary authority/data failure must not be bypassed. Sources: SI-001–004, SRS Section 12.2, ASR-INT-001 and ADD Sections 9.5/9.6/9.9.

## 12. Python AI Boundary and Model Evaluation

Keep four evidence types separate; none substitutes for another.

| Evidence | Responsibility / Approach |
| --- | --- |
| Backend software correctness | Backend/QA verify contract parsing, bounded complete-result handling, unsupported values, missing evidence, failure/manual continuity, confidence/provenance distinction and no authoritative save before confirmation. Controlled outputs cover default 0.60/0.85 confidence boundaries and guards against unjustified High Confidence. |
| Real core/Python integration | Backend/AI verify the agreed versioned contract and actual preview-plus-proposal exchange, including safe late output after user confirmation/correction. Python has no independent confirmed-state/database authority. |
| Recognition quality | AI/QA execute the locked SRS Section 8.1.1 benchmark and retain dataset/label/model/version evidence. At least 200 qualified images, at least 50 in each supported category; category accuracy at least 90% overall and 85% per category; dominant-color accuracy at least 90% overall, reported separately. Two independent annotators should label, with adjudication; no training/tuning on the corpus after lock. These floors are not extended to every rich attribute. |
| Preview usability and processing time | Criterion-based preview review follows SRS Section 3.4.1 (identifiable garment, preserved major regions, non-obstructive background, sufficient review information), without an invented preview percentage. Actual full-operation timing follows Section 19; Java tests returning canned predictions cannot prove model quality, preview usability or real processing latency. |

All proposals, including High Confidence, require user confirmation; unknown attributes remain unknown and later AI output cannot overwrite accepted values. Synthetic or appropriately approved nonpersonal images support software tests. The locked evaluation corpus has its separate controlled purpose and isolation; personal wardrobe images, profiles, feedback, corrections and Wear Events must not enter MVP AI training/improvement. Sources: AI-REQ-001–014, NFR-PRIV-002, QA-TEST-01 and ADD Section 9.5.

## 13. Selective End-to-End Testing

Keep a small stable set of integrated journeys as delivered capabilities become available. A backend-only HTTP journey verifies the backend path; it does not establish mobile behavior, accessible interaction or the complete user outcome. Full journey evidence includes the relevant supported client, real core and PostgreSQL, plus actual required integrations where applicable. Controlled substitutes isolate provider failures and avoid uncontrolled Internet dependencies; label which boundaries are substituted.

| Candidate Journey, Not Sprint Selection | Critical Outcome | Existing Interaction Sources |
| --- | --- | --- |
| Private access and returning use | Register/login, renew, logout, account switch and recover without exposing the wrong account or losing accepted information. Controlled email supports recovery; security permutations remain in lower layers. | UC-001–004; AD-004; US-AUTH-001–005 |
| Trustworthy wardrobe creation/inspection | Confirm an imageless manual entry, browse and inspect its accepted profile/readiness. Later assisted entry adds correction/manual fallback and late-output protection. | UC-007/008; AD-007; US-GAR-001, US-WAR-001/002 |
| Use owned clothing and report wear | Receive valid choices, Shuffle one slot, report Wear, then correct/remove one report with coherent history and personalization meaning. | UC-011/012/014–017; AD-011/012/014/016/017 |
| Understand and evaluate improvement | View contextual Coverage/Gaps, evaluate a candidate, inspect hypothetical new outfits and optionally return from external navigation without creating ownership/Wear. Include a truthful non-exact outcome. | UC-019–023; AD-019–022 |

These are candidate coverage groups, not a requirement to combine everything into four long fragile scripts. Choose the smallest journeys that expose real integration risk; fixture setup may bypass irrelevant UI steps. Assert business outcomes, avoid arbitrary sleeps and brittle visual selectors, and keep the exhaustive validity, expiry and data permutations in lower layers. Human exploration, device accessibility and preview review complement automation under SRS Sections 3.4.1/12.4 and the DoD.

## 14. Test Data, Time and Isolation

Use small explicit fixtures with stable identities and readable builders/factories where repetition warrants them. Each test creates its own state and obtains its expected result from requirements. Factories should expose relevant values rather than hide scenario-critical defaults. Use synthetic accounts, local delivery destinations, disposable secrets and approved nonpersonal media; no production database exports or real-user wardrobes in ordinary test runs.

| Fixture Concern | Required Control |
| --- | --- |
| Ownership/security | At least two synthetic accounts and separate sessions allow owner/other-user/unauthenticated and per-session/account-wide effects to be distinguished. Assertions include forbidden disclosure/mutation. |
| Garment/evaluation states | Saveable-but-unready, removed/historical, current owned and hypothetical candidates; valid/invalid layered outfits; no, one, two and sufficient distinct options; zero versus missing/incomplete/unavailable/outdated evidence. |
| Clock and expiry | Supply a controllable clock at policy boundaries. Fix evaluation instants and test just before/at/after access, session, reset, weather, commercial and retention boundaries; never wait days in real time. |
| Event time | Preserve original event-local date/timezone for history/grouping; calculate age/recency from absolute instants and completed 24-hour periods. Include travel, same-original-day correction, future/cross-day rejection, latest-survivor changes and 30/31, 60/61, 90/91-day decay boundaries. Do not invent an overlooked-garment age anchor. |
| Repetition and randomness | Distinguish repeated delivery of one logical intention from a new explicit report on the same outfit/day. Use fixed/random seeds with failure reporting when sampling is useful; permutation or duplicate inputs must not inflate unique outfits. |
| Database and parallelism | Fresh data per test/worker, explicit cleanup, no dependence on execution order or shared mutable fixture state. Reset mutable doubles and static state. Use the transaction/disposable-resource approach from Section 8. |
| Concurrency and asynchronous work | Use explicit barriers/signals and bounded completion checks rather than timing guesses. Exercise independent real transactions where races matter. Control provider timing/failure and inspect committed accepted outcomes. |

Policy tests with an advanced clock establish deadline decisions; they do not alone establish that deployed cleanup runs, deletes applicable media copies/versions, or resumes after restart. Add real adapter/operational evidence for those responsibilities. The source distinctions are BRULE-PERS-003–006, BRULE-WEAR-001–006, BRULE-HIST-002/003, DATA-RET-001–003 and ADD Sections 9.2/9.3/9.9.

## 15. Regression Strategy

During a change, run focused tests first, then the broader affected suite, then PR/CI regression. Preserve checks for previously delivered behavior; a defect fix normally adds a focused regression reproducing the requirement violation before the correction. When a requirement legitimately changes, update the owning source and affected tests together. Do not rewrite assertions to accept a failure without establishing the intended behavior.

Select affected checks by risk and dependency impact: shared validity/identity changes affect Daily, Shuffle, Wear targets, Coverage and Multiplier; session changes affect all protected paths; persistence changes affect migration/integrity; provider changes affect contracts/fallback. Representative higher-level regression verifies those shared integrations without duplicating every rule case at every level. Required broader checks remain part of CI/DoD even if local execution was focused.

Postman and manual sessions remain useful for debugging, exploration and inspecting unfamiliar behavior. A manually successful request does not provide repeatable regression or establish Done. Record necessary human/device/model-quality evidence alongside automation, with tested version, conditions and limits (DoD Sections 4–6).

## 16. Selective TDD

Use **RED → GREEN → REFACTOR** where it improves clarity: write a failing behavior test, implement enough to satisfy it, then improve the design while the relevant suite stays green. Good candidates include garment readiness, hard validity, unique-set calculations, Coverage, Wear normalization/aging, and security-sensitive lifecycle decisions. A failing test must fail for the intended missing behavior, not broken setup.

Strict TDD is not required for every configuration class, DTO, mapping, framework integration or trivial accessor. Integration-first investigation can be appropriate for an unfamiliar adapter; then automate the learned contract. Testing still accompanies implementation whichever order is useful. Keep JIT design, tests, implementation and refactoring in one small feedback loop under Workflow Sections 26/28/33.

## 17. Acceptance Criteria Traceability

Maintain **Requirement / Business Rule → User Story → AC → automated test or required evidence → delivered implementation/version**. Use meaningful test names and existing Story/AC/source IDs in test metadata/comments or PR evidence where useful. A compact PR mapping is sufficient; no separate test catalogue or new requirement identifier system is required. One AC may need several levels, and one scenario may support several AC; no one-method-per-AC quota applies.

| Existing Source / AC Example | Intended Evidence, Not Executed Tests |
| --- | --- |
| FR-AUTH-013/014; BRULE-AUTH-006; [US-AUTH-003](../06-scrum/stories/auth/US-AUTH-003-resume-session.md) AC-03/05 | Rotation/reuse policy cases, real PostgreSQL consumption/revocation races, and API checks showing affected/newer credentials denied while a separate session remains distinct. |
| FR-AUTH-017/018; BRULE-AUTH-004; [US-AUTH-005](../06-scrum/stories/auth/US-AUTH-005-recover-account-access.md) AC-07–09 | Replacement and submission-time validity cases, persistent single-success checks, controlled delivery and clear invalid/expired guidance. |
| FR-AI-003/008; DATA-INT-001/004; [US-GAR-001](../06-scrum/stories/garment/US-GAR-001-add-garment-manually.md) AC-01/02/09/12 | Application confirmation/manual-independence checks, real accepted-addition retry persistence, and integrated retained values/readiness/uncertain-outcome presentation. |
| FR-WAR-002; BRULE-HIST-001; [US-WAR-002](../06-scrum/stories/wardrobe/US-WAR-002-inspect-owned-garment.md) AC-07 | Controlled unavailable-use response with retained profile inspection and no fabricated zero/history; consumer-facing evidence where UI meaning is involved. |
| BRULE-MULT-002–009; UC-022 / PBI-030–031 | Domain exact/non-exact fixtures and integrated complete same-basis count/preview evidence when those capabilities are implemented. Bind to actual Story/AC after later refinement/selection; do not invent stories now. |

For each selected Story, identify positive, boundary, negative/failure and authorization evidence relevant to its AC, plus triggered quality obligations. Link tests/reports/manual results to the implemented revision and environment. Mark evidence unavailable/not run/inapplicable truthfully with reasons; a planned test is not a passing test. Existing [Story Index](../06-scrum/stories/README.md), [Product Backlog](../06-scrum/product-backlog.md) and DoD remain authoritative for scope, ordering and completion.

## 18. Suite Classification and CI Expectations

Use a small number of suite groups; resolve directory/tag/build-task names during Engineering Baseline. CI provider, build tool and plugin configuration are not selected here.

| Suite Group | Contents | Expected Cadence |
| --- | --- | --- |
| Fast | Domain/application tests without infrastructure | Focused runs during coding; full fast suite before commit and on every software PR. |
| Architecture | Compiled-source dependency rules | Before PR and on every software PR; focused local runs after package/dependency changes. |
| Integration / API | PostgreSQL/migrations, adapters/contracts and web/security tests; a web slice need not start a database | Affected checks during coding and before PR; required regression in CI for software PRs, including the real persistence/security paths affected. Start with the delivered suite rather than a complicated change-selection system. |
| Critical journeys / expensive validation | Small integrated E2E set, real runtime/provider compatibility, formal performance/recovery/concurrency, ML and device evidence | Stable relevant journey checks before integration; heavier checks on relevant changes and agreed scheduled/pre-release runs. Triggered DoD evidence must exist before the affected work is Done; scheduling does not waive it. |

Conceptually, future CI verifies **reproducible build → fast tests + architecture checks + required integration/API/contract checks → other applicable verification → reported pass/fail**. Independent checks may run in parallel once isolation is established. Engineering Baseline must provide an inspectable entry point and reports, with compatible container support for required PostgreSQL tests. No pipeline or build configuration is created by this strategy.

Required checks must pass on the revision integrated into the Increment. Report test counts, failures and meaningful skips/exclusions; zero discovered tests or missing infrastructure must not masquerade as successful required verification. Retain useful failure diagnostics and environment/version information without secrets/private contents. Local results do not establish CI success, and rerunning until green does not resolve an unexplained failure. Full benchmarks and human evidence can be linked from a controlled run rather than forced into every PR job, provided the applicable DoD and evidence basis remain satisfied.

## 19. Quality Attribute Verification

Use the [QA scenarios](../04-architecture/quality-attribute-analysis.md), [ASR](../04-architecture/ASR.md) and [ADD Sections 6/12](../04-architecture/ADD.md) to select evidence, retaining the [SRS](../03-requirements/SRS.md) as acceptance authority. Develop executable harnesses with the relevant selected work; do not require whole-MVP performance or detailed test plans before Sprint delivery.

| Quality Obligation / Source | Verification Direction and Existing Boundary |
| --- | --- |
| Garment processing — QA-PERF-01; NFR-PERF-001 | Measure accepted analysis after image transfer through availability of **both** usable preview and reviewable proposals: p95 ≤5 seconds. Include actual core/AI/media/result overhead; upload and user review are outside the interval. Quick job acceptance or a stubbed model is not full-operation evidence. |
| Daily advice — QA-PERF-02; NFR-PERF-002, NFR-SCA-001 | Measure accepted request with required context to first complete result set: p95 <3 seconds with exactly 100 confirmed garments. Record eligibility/readiness and preserve eligible inputs; 100 is neither a quota nor a maximum. |
| Formal timing procedure — NFR-TEST-003; SRS Section 12.3 | Per operation, 5 warm-ups then at least 100 measured runs, p95 from measured runs, legitimate slow runs retained. Record software/model versions, workload and specified reference device/network/environment. Use the source reference configuration, not a laptop microbenchmark as acceptance proof. |
| Concurrent integrity — QA-SCA-01; NFR-SCA-002 | Exercise 20 concurrently active authenticated users with known ownership, actions and accepted-state expectations. Verify functioning core operations without leakage, broken access, corruption or accidental duplicates. Separate from nominal timing; no p95-at-20-users target is inferred. |
| Recoverable restart — QA-REL-03; NFR-REL-003 | In the agreed validation environment, demonstrate core restoration ≤5 minutes after recoverable application/service restart, accepted state intact and applicable fallbacks usable. Inspect resumed required work. This is not a production availability/disaster-recovery SLA. |
| Accepted-state and domain integrity — QA-REL-01/02, QA-CON-01; ASR-REL-001/CON-001 | Controlled confirmation/failure/retry, Wear survivor/time and complete same-basis calculation fixtures; real transaction/race evidence and integrated currentness/limitation explanations. Source-owned zero and non-exact meanings must remain distinguishable. |
| Privacy/security/lifecycle — QA-SEC-01–03; ASR-SEC-001/002 | Section 9 access/session evidence; purpose-minimal exchanges and no MVP personal-data training. Immediate effective garment/event exclusion; applicable physical deletion within 30 days; user-linked measurement maximum 90 days. Verify actual required lifecycle execution/copies and restart recovery, separately from retained necessary history and ranking expiry. |
| Dependency continuity — QA-AVL-01, QA-INT-01; ASR-INT-001 | Inject separate AI/weather/shopping/measurement failures and preserve the applicable independent paths. Verify weather age ≤30 minutes and at most 2-second assessment delay; commercial currentness through 24 hours inclusive, without turning destination failure into zero utility. |
| AI acceptance — QA-TEST-01; ASR-TEST-001 | Locked corpus/labels and isolated category/color evaluation, distinct preview review and complete-operation timing under Section 12. |
| Localization/accessibility/usability — QA-MNT-01, QA-USE-01/02; ASR-USE-001 | Automate canonical-value/label mapping and state contracts; Mobile/QA supply supported Android/iOS, Vietnamese, TalkBack/VoiceOver, enlarged-text and task-study evidence. Apply all SRS Section 12.4 criteria, including its participant/task/SUS and accessibility conditions; backend tests alone cannot establish these outcomes. |
| Protected diagnostics — QA-SUP-01; NFR-SUP-001/002, NFR-TEST-001/002 | Inject pending/failure/uncertainty and inspect truthful operation/result-basis evidence and recovery choices. Check secret/private-content exclusion; optional measurement failure neither changes acceptance nor falsely claims recorded evidence. |

Preserve the reference hardware/network and supported OS conditions in SRS Sections 2.3/12.3, and image qualification/input criteria in Section 3.4.1. Record actual conditions and limitations with results. No general API/Multiplier latency, extra load target, coverage percentage or invented preview-quality threshold is introduced. Performance investigations may guide optimization, but cannot weaken exactness, authority or privacy. Existing evidence is reusable only while its behavior and validation basis remain valid, as required by DoD Section 5.

## 20. Tooling Direction

The following is a minimal recommended direction, not an installed toolset or architecture reselection. Supported JDK/Spring Boot, build tool, test library majors, PostgreSQL image and web stack remain Engineering Baseline decisions. Choose compatible pinned versions using current official documentation; do not assume example imports, module names or old container tags fit the eventual versions.

| Candidate | Responsibility and Fit | Initial Baseline or Later |
| --- | --- | --- |
| JUnit Jupiter | Java test execution, lifecycle and parameterized rule cases. JUnit 5 is a candidate; choose the Jupiter major supported by the selected Spring Boot line rather than fixing a major independently. | Establish compatible runner/discovery and smoke execution in Engineering Baseline. |
| AssertJ | Readable assertions for policy values, collections and outcomes; helps explain failed business expectations. | Initial assertion support where supplied by the compatible Boot testing dependencies. |
| Mockito | Mock/stub genuine application ports and verify meaningful boundary effects. Use plain fakes when they express state more clearly. | Initial support; no mock for every domain object. |
| Spring Boot Test / appropriate test dependencies | Focused slices and selected full application wiring/integration. Boot supplies coordinated testing support; exact modules depend on the selected version. | Initial compatible foundation, then add focused facilities as delivered boundaries require. |
| MockMvc or WebTestClient; Spring Security test support | HTTP validation/security contracts for the actual MVC or WebFlux stack; mock identity for focused authorization, real validation/session paths for authentication evidence. | Enable the facility matching the selected web stack in the baseline; grow security cases with selected Stories. Do not adopt both stacks for testing. |
| Testcontainers PostgreSQL | Real isolated database semantics for persistence, migrations and transactions. Docker-compatible runtime availability is a practical setup cost justified by the selected authority. | Initial integration-test capability and isolated database smoke check; feature fixtures/races arrive with selected work. |
| ArchUnit | Practical compiled-class dependency rules protecting the selected modular/Clean boundaries. | Initial small suite as package boundaries materialize; extend only for source-supported rules. |
| Local protocol-stub / provider-test facility | Real adapter serialization/failure checks without Internet dependence. The exact facility depends on the eventual protocol/provider. | Defer choice until the relevant adapter needs it; no extra integration framework now. |
| JaCoCo | Optional coverage reports to find untested risk; coverage cannot demonstrate assertion quality or requirement satisfaction. | Defer until useful; no percentage gate or reporting stack required by this strategy. |
| Mobile E2E, load and AI evaluation harnesses | Device journeys, full-operation measurement and locked-model evidence require their own suitable execution environments. | Defer tool choice to relevant work; preserve Section 19 obligations throughout delivery. |

The [Spring Boot testing guide](https://docs.spring.io/spring-boot/reference/testing/index.html) and [test dependency guide](https://docs.spring.io/spring-boot/reference/testing/test-scope-dependencies.html) support the coordinated Java testing direction. Testcontainers and ArchUnit references are linked in Sections 8/10; [JaCoCo documentation](https://www.jacoco.org/jacoco/trunk/doc/) describes the optional coverage tool. These references explain capabilities, not CapsuleAI version selections. No tool/configuration is installed by this document.

## 21. Definition of Done Integration and Evidence Ownership

The [project DoD](../06-scrum/definition-of-done.md), Sections 3–8, is the completion authority. This strategy explains how to obtain its verification evidence; it creates no competing checklist or additional approval gate. A Story is not Done solely because it works manually in Postman or its backend task is complete.

Developers provide and maintain meaningful changed/affected-behavior automation, real integration evidence, required CI results and review links. Backend/AI/Mobile contributors verify their actual collaborating boundaries; QA helps select risks and evaluate evidence, while Product/BA clarify intended behavior and AC. Roles may be combined in the small team; evidence ownership cannot disappear between contributors.

Apply the DoD's actual triggers: contracts/consumers when API changes; migrations and supported-transition integrity when schemas change; security/privacy/lifecycle for affected data; AI/domain/quality evidence for changed recommendations; appropriate integration/staging and device evidence for runnable/user-facing changes. Update architecture documents only when architecture truth changes and requirements only when requirement truth changes. An untouched source needs no ceremonial edit.

Record scope, tested revision, environment, inputs, outcome and relevant limitations through existing PR/work-item/report links. A failed required check, incomplete integration, violated required behavior or missing triggered evidence prevents Done under the DoD regardless of defect label. Later cross-MVP quality PBIs extend verification, rather than defer each delivered capability's obligations. No new Product Owner sign-off or quality waiver is inferred here.

## 22. Developer Workflow

1. **During refinement:** read candidate Story/AC, SRS/rules, relevant interactions and architecture. Identify testability, dependency and evidence risks; investigate only enough for responsible selection. Raise a bounded Spike for a major unknown where needed.
2. **During Sprint Planning:** select feasible work and an initial plan through the established Scrum process. Identify likely test/integration tasks without requiring final endpoints, schemas or full behavior diagrams.
3. **For a selected Story:** perform only necessary JIT design, identify the lowest useful test levels and source-linked fixtures, then write tests and implementation in small steps. Use selective TDD where useful.
4. **While coding:** run focused tests, implement/refactor, and run affected fast/integration/API checks. Add real adapter/transaction evidence for risks mocks cannot establish. Keep affected contracts/design synchronized.
5. **Before and during PR:** run broader relevant regression and architecture checks, explain AC/evidence links, inspect CI and resolve findings. Verify actual integration and triggered quality/device/model evidence before claiming Done.
6. **Through the Increment:** integrate usable work, retain valid evidence, review results and adapt the backlog/affected documents through their owners. Incomplete work does not become Done at Sprint end.

This is Workflow Section 33's continuous **JIT Design → Automated Test + Implementation → Refactor → applicable Integration/API Verification → PR/CI/Review → AC + DoD → Working Increment**, with short feedback loops, not a final testing phase.

## 23. Risks, Low-Value Tests and Flaky-Test Policy

**Test behavior and risk, not lines of code for their own sake.** Avoid isolated tests for trivial getters/setters, behavior-free records, framework internals or private methods. Test simple mappings when they carry meaningful validation, provenance, ownership or contract risk. Avoid mock tests that merely repeat each production statement and copying the same exhaustive rule matrix into every layer. Test the real policy directly, then verify the risky integration at its boundary.

| Risk / Anti-Pattern | Response |
| --- | --- |
| Everything is a full Spring/E2E test | Move policy and orchestration permutations into fast tests; retain higher-level cases for integration risk. |
| Everything is mocked | Add actual PostgreSQL, configured security and real adapter/contract evidence where correctness depends on those boundaries. |
| H2, automatic test DDL or rollback hides PostgreSQL/commit defects | Use the intended database/migrations and committed multi-connection cases from Section 8. |
| AI canned output is called accuracy or latency proof | Keep backend, real integration, ML, preview and formal timing evidence distinct. |
| CI/container/device capacity is unavailable | Expose missing required execution and resolve the environment gap. Do not mark unexecuted checks passed or waive applicable DoD obligations. |
| Sensitive fixtures/reports or evaluation-set leakage | Use privacy-safe inputs and protected evidence, enforce purpose separation and locked-corpus exclusion from training/tuning. |
| Broad architecture exceptions or duplicated rules | Check against the accepted views/public semantics; document legitimate exceptions in the owning design instead of weakening checks silently. |

A randomly failing test is a defect in the test, environment or system. Preserve the failing input/seed, revision, logs and relevant scheduling/environment conditions; investigate races, shared state, uncontrolled clocks, external calls or incorrect expectations. Fix the cause using deterministic setup and synchronization. Do not add blind sleeps, weaken assertions or routinely rerun until green.

If temporary isolation of a flaky test is necessary, make its exclusion visible with an owner, reason and tracked repair; retain suitable alternative protection for the risk. A quarantined/skipped test is not a passing test. Isolation cannot waive required AC, security/integrity evidence or another DoD condition; unresolved required behavior still blocks Done. Keep this within existing issue/PR evidence rather than creating a separate governance system.

## 24. Deferred Details and Known Source Issues

| Resolution Point | Detail Intentionally Deferred |
| --- | --- |
| Pre-Sprint Engineering Baseline | Supported versions/build choice, test execution/discovery, source/suite layout, container lifecycle, migration foundation if adopted, minimal architecture checks, CI entry points and safe environment conventions. Establish infrastructure/smoke verification without implementing product features. |
| Selected-story JIT design/execution | Exact API/AI contracts, schema/migrations, transaction/concurrency mechanisms, clocks/fixtures, adapter protocol tests and conditional Sequence/State models. Design only what that selected outcome and risk require. |
| Relevant integrated/release verification | Specific device/E2E, performance/load/restart and AI evaluation harnesses, provider sandbox execution and reproducible scenario plans. Applicable evidence accompanies affected delivery under the DoD; a later plan is not an exemption. |

No whole-MVP API/Data/Sequence/State design gate is needed to use this strategy. API contracts, schemas, diagrams and tests remain living artifacts; update the affected truth and preserve stable source links as implementation/validated understanding evolves.

Two existing non-blocking editorial issues are recorded in [ADA Section 3.4](../04-architecture/architecture-decision-analysis.md) and [ASR Section 11](../04-architecture/ASR.md): FR-MET-004 contains an incomplete benchmark cross-reference, and focused UC diagram links reference absent files. Use explicit SRS Sections 3.4.1/8.1.1/12.3 with stable NFR/AI IDs, and the existing master UC diagram, 23 textual specifications and 12 activities. No missing model content is assumed. This strategy does not repair unrelated source files or reinterpret their requirements.

## 25. Traceability and References

These are the repository baselines inspected for this strategy. Their statuses and approvals remain their own; a strategy reference establishes no implementation conformance. Detailed test cases will link back to these sources rather than copy their catalogues.

| Authority / Current Baseline | Contribution / Strategy Coverage |
| --- | --- |
| [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), revised 2026-10-20; Sections 26–34, 40–43 and 46 | Strategy before Engineering Baseline, selection before JIT detail, testing alongside implementation and affected-truth propagation; Sections 2–5, 15–18, 21–24/26. |
| [BRD v0.3](../01-business/BRD.md); [PRD v0.2](../02-product/PRD.md) | Business value, connected MVP, privacy/manual continuity, trustworthy outcomes and exclusions; Sections 3–7, 11–13/19. |
| [SRS v0.3](../03-requirements/SRS.md), especially Sections 3–9/12; [Business Rules v0.1.2](../03-requirements/business-rules.md), Sections 2–15 | Precise behavior, invariants, boundaries, lifecycle and acceptance measures; Sections 6–9, 11–14/17/19. |
| [Master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml); [23 UC specifications](../03-requirements/use-cases/); [12 Activity Diagrams](../03-requirements/activity-diagrams/) | Authorized interaction, alternate/failure/retry paths and candidate critical journeys; Sections 7/9/11/13/17. |
| [Product Goal v0.1.1](../06-scrum/product-goal.md); [Product Backlog v0.1](../06-scrum/product-backlog.md); [eight initial Stories/AC v0.1](../06-scrum/stories/README.md) | Incremental value, 41-PBI horizon and existing behavioral slices; Sections 3/7/13/17/22. No estimates, reordering or Sprint selection. |
| [DoD v0.1.8](../06-scrum/definition-of-done.md); [Delivery Decisions v0.1.9](../06-scrum/delivery-decisions.md), DD-001–006 | Common/conditional completion evidence, review/CI and current handoff; Sections 2/15/17–18/21–24/26. |
| [Quality Attribute Analysis v0.1.8](../04-architecture/quality-attribute-analysis.md); [ASR v0.1.8](../04-architecture/ASR.md) | Source-owned scenarios and nine significant requirement clusters; Sections 8–14/18–19/23–24. |
| [ADA v0.2.7](../04-architecture/architecture-decision-analysis.md); [Selected Architecture Decisions v0.1.6](../04-architecture/selected-architecture-decisions.md) | Analysis/trade-offs and all 18 selected directions, with unresolved implementation evidence; Sections 3/8–12/19–20/24. |
| [ADD v0.1.5](../04-architecture/ADD.md), Sections 6–10/12; [Logical View v0.1](../04-architecture/views/logical-view.puml); [Implementation View v0.1](../04-architecture/views/implementation-view.puml); [Deployment View v0.1](../04-architecture/views/deployment-view.puml); [Data View v0.1](../04-architecture/views/data-view.puml) | Accepted owner/public-contract direction, inward dependencies, runtime/media authority, logical persistence and realization risks; Sections 7–14/19–20/23. Data concepts are not physical tables. |

Official tooling capability references are linked beside their recommendations in Sections 8–10/20 and were checked on 2026-10-20. Recheck the relevant version's official documentation when implementing the engineering baseline; these links select no versions.

## 26. Status and Next Step

**Version 0.1 — Baseline Draft.** This document defines the proposed testing approach for team review. No tests, infrastructure, CI, feature implementation, passed benchmark or verified architecture conformance is claimed. Product requirements, Business Rules, all architecture selections/views, Product Backlog order and Sprint scope remain unchanged.

The subsequent execution order remains **Pre-Sprint Engineering Baseline → ongoing Backlog Refinement → Sprint Planning → selected-story JIT Design with Automated Test + Implementation → Integration/PR/CI/Review → Done Increment → Sprint Review/Retrospective and adaptation**. This strategy performs none of those subsequent activities.

**Next Step: Pre-Sprint Engineering Baseline.**
