# CapsuleAI — Architecture Design Document

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Architecture Design Document (ADD) |
| Version | 0.1.4 |
| Status | Baseline Draft |
| Last Updated | 2026-10-11 |
| Owner | Architect / Tech Lead, with Backend, AI, Mobile and QA input; Product/BA for requirement implications |
| Architecture Decision Baseline | ADA-001–018 Selected; no override or deferral |
| Selection Authority | [Selected Architecture Decisions](selected-architecture-decisions.md), v0.1; Project Owner / Backend Developer, 2026-10-10 |
| Quality / Requirement Inputs | [Quality Attribute Analysis](quality-attribute-analysis.md) v0.1.2; [ASR](ASR.md) v0.1.2; [SRS](../03-requirements/SRS.md) v0.3; [Business Rules](../03-requirements/business-rules.md) v0.1.2 |
| Business / Product Inputs | [BRD](../01-business/BRD.md) v0.3; [PRD](../02-product/PRD.md) v0.2 |
| Analysis Input | [Architecture Decision Analysis](architecture-decision-analysis.md), v0.2.1; unchanged v0.2 analyzed options |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 17.3–28 and 46 |
| Authoritative Location | `docs/04-architecture/ADD.md` |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-10 | Initial integrated architecture baseline realizing all 18 selected directions against seven existing drivers, nine ASRs and source-linked QA scenarios; four views and detailed design remain downstream. |
| 0.1.1 | 2026-10-10 | Linked Logical View v0.1 and advanced current navigation to Implementation View; architectural tactics, selected directions, source scenarios and historical revision remain unchanged. |
| 0.1.2 | 2026-10-10 | Linked Implementation View v0.1 and recorded explicit human acceptance of the Logical decomposition; review precedes Deployment View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |
| 0.1.3 | 2026-10-11 | Linked Deployment View v0.1 and recorded explicit human acceptance of both Logical and Implementation Views; Deployment View review precedes Data View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |
| 0.1.4 | 2026-10-11 | Linked Data View v0.1 and recorded explicit human acceptance of all three preceding views; Data View review precedes ADRs. Navigation/status only; substantive baseline and historical revisions remain unchanged. |

Input versions identify the baselines re-read for authoring. Subsequent navigation-only revisions do not change those obligations or selections. Baseline Draft records pending artifact review, not software conformance or benchmark success.

## 2. Introduction

This ADD explains **how the selected architecture addresses CapsuleAI's significant requirements and quality drivers**. It connects authoritative needs to responsibility boundaries, runtime collaboration, coordination tactics and evidence that later design/testing must establish. The architecture covers the complete connected three-pillar MVP, including Coverage/Gaps and Wardrobe Multiplier, beyond the current eight-story refinement batch.

| Authority | Contribution / Boundary |
| --- | --- |
| BRD / PRD | Business value, product scope and resolved behavior; architectural realization adapts to them. |
| SRS / Business Rules / Use Cases / Activity Diagrams | Observable obligations, invariants and interaction/failure meaning; exact behavior remains in those sources. |
| QA analysis / ASR | Source-owned measurable scenarios, seven drivers and nine architecture-significant requirement clusters. |
| Selected Architecture Decisions | Controls the chosen directions; all ADA-001–018 remain Selected. |
| ADA | Supplies existing comparisons, accepted trade-offs, confidence and evidence gaps; this ADD does not repeat option analysis. |
| ADD / four primary views | This baseline integrates responsibilities/tactics. Logical, Implementation, Deployment and Data Views provide the canonical detailed representations in workflow order. |
| Later ADRs / detailed design / Test Strategy | Preserve consequential rationale after views, realize necessary contracts/data/behavior, then define executable verification. |

The current repository contains documentation, with no implementation/configuration baseline establishing executable behavior. Historical proposal/implementation notes and the Food Delivery ADD inform methodology only. They do not add CapsuleAI domains, infrastructure or targets. Creating this draft claims neither artifact approval nor completed software validation.

## 3. Architecture Scope

### 3.1 System and Runtime Boundaries

The architecture connects private mobile use to one coordinated business core. Analysis assists confirmed wardrobe creation; owned-wardrobe advice and hypothetical shopping utility remain distinct purposes.

| Boundary | Architectural Responsibility | Authority / Collaboration |
| --- | --- | --- |
| Android/iOS mobile clients | Capture/selection and permission alternatives; Vietnamese review, actions, accessible results and recovery. | Act under the current account/session. Present accepted versus pending/uncertain outcomes and clear protected content on account switch/logout; transport/UI framework detail remains downstream. |
| Java / Spring Boot business core | Coordinate authorized product operations, confirmed truth, business invariants, intelligence and recovery through domain-oriented boundaries. | One Modular Monolith with pragmatic Clean Architecture, published owner contracts and controlled local transaction participation. |
| Separate private Python AI runtime | Process purpose-minimal selected-image input into usable preview and reviewable attribute proposals/uncertainty. | Bounded synchronous complete-result interaction; no independent authoritative wardrobe/account/business-state writes. |
| One PostgreSQL primary database | Persist authoritative business/account/session/reset and logical-action state under logical context ownership. | Access through owner persistence adapters/public operations; shared physical storage grants no unrestricted cross-context access. |
| Private object storage | Retain original/processed garment media with authorized delivery and applicable lifecycle handling. | Storage existence is not user authority. Association, access and cleanup are coordinated recoverable effects, separate from database atomicity. |
| External weather / email recovery / shopping boundaries | Supply selected environmental evidence, necessary recovery delivery and optional chosen navigation/credible commercial information. | Narrow ports/adapters and semantic translation; each dependency retains its own completion, disclosure and failure meaning. Providers are unspecified. |

### 3.2 Business Responsibility Obligations

These are cohesive **responsibility concerns, not a duplicate module/context list**. [Logical View v0.1](views/logical-view.puml), [Implementation View v0.1](views/implementation-view.puml) and [Deployment View v0.1](views/deployment-view.puml) are human-accepted baselines; [Data View v0.1 — Baseline Draft](views/data-view.puml) is available for review.

| Responsibility Concern | Obligation to Allocate |
| --- | --- |
| Account access and personal context | Authorized identity/session/recovery; optional preferences and common needs distinct from the current request occasion; removed optional context stops effective use. |
| Wardrobe and garment authority | Saveable/readiness distinctions; confirmed/corrected profiles, current ownership, browsing and maintenance; private media; minimal removed-item historical meaning. |
| Styling and shared evaluation meaning | Current eligible inputs, canonical outfit identity, hard validity, personalized ranking, Shuffle invariants and explainable results. |
| Feedback, reported use and history | Exact-outfit feedback, distinct logical Wear intentions, original-local-day/absolute-time meanings, survivor effects, utilization and effective behavioral evidence. |
| Wardrobe intelligence and strategic shopping | Contextual weighted Coverage, capability-first Gaps, complete same-basis candidate utility and hypothetical previews; credible optional commerce and independent measurement. |

### 3.3 Scope Limits and Downstream Detail

Architecture owns the authority, dependency, acceptance, failure and evidence boundaries required to make these responsibilities coherent. SRS scope exclusions remain: no native commerce, automatic purchase/wear verification, social/public wardrobes, additional garment categories, consumer web/desktop, AR/3D/video or guaranteed offline use. Lightweight personalization does not require an advanced learned model.

Final context/module names, exact packages/classes/DTOs, endpoints, event payloads, database tables/indexes/schemas/FKs/ORM, locking/isolation, numeric dependency timeout/retry configuration, providers and deployment machines/containers are unresolved realization details. The four view artifacts, ADRs, detailed API/data/sequence/state design, Test Strategy, engineering baseline and Sprint artifacts are not created here.

## 4. Design Constraints

Requirements constrain observable meaning; selections constrain its architectural realization. The sources below remain authoritative for their exact criteria. The ADD introduces no competing thresholds or new product features.

| Established Constraint | Governing Source | Architectural Consequence |
| --- | --- | --- |
| Connected Vietnam-first Android 10+ / iOS 15+; Vietnamese initial UI | SRS Sections 1.2/2.3; LOC-001–005; PRD OPQ-002 | Preserve permission/manual alternatives, canonical meanings independent of display labels, account-safe client state and accessible outcomes. Mobile framework remains unspecified. |
| Selected system/runtime/data direction | ADA-001–004/007/011–013/015–018; selected record | Modular business core, private separate Python analysis, one PostgreSQL authority and private media; inward dependencies, logical owners and public semantic contracts; no dedicated cache or broker initially. |
| Persistent usable access and distinct revocation | FR-AUTH-004/007/009/010/013/014/017/018; DATA-AUTH-005; ADA-008 | Valid JWT plus usable account/session authority. Access lasts 15 minutes, session at most 30 days across consumed refresh rotation; reset is 30-minute single-success-use with replacement. Logout is current-session-only; reuse affects its session; completed reset revokes all account Refresh Sessions. |
| Confirmed truth and bounded garment domain | DATA-INT-001/002; BRULE-GAR-001–003/009; BRULE-OUT-001/008/009 | TOP/BOTTOM/OUTERWEAR/FOOTWEAR; confirmed category/color is Saveable without an image. Evaluation-specific readiness and hard validity remain separate from ranking and saving. |
| Qualified image input and complete processing | SRS Section 3.4.1; NFR-PERF-001; ADA-004/005 | Supported JPEG/JPG, PNG, HEIC/HEIF; maximum 15 MB, shortest dimension ≥512 pixels. Full processing includes usable preview plus reviewable proposals; user confirmation remains separate. |
| Complete-operation timing and distinct workload meanings | NFR-PERF-001/002; NFR-TEST-003; NFR-SCA-001/002; SRS Section 12.3 | p95 ≤5 seconds for post-transfer processing; p95 <3 seconds for first complete daily result with exactly 100 confirmed garments. Use reference hardware/network and 5 warm-ups then ≥100 measured runs. The 20-user integrity workload is separate, not a timing guarantee. |
| Necessary private use and lifecycle | NFR-PRIV-001/002; DATA-RET-001–003; SRS Section 12.1; ADA-007/012/014 | Immediate effective removal; applicable physical deletion within 30 days; user-linked measurement no longer linked beyond its 90-day limit. Necessary minimal functional history is distinct. MVP personal data is excluded from model training/improvement. |
| Environmental and commercial evidence | FR-WEATHER-003–005; SI-002; FR-SHOP-001/003/006; SRS Section 12.2 | Weather age ≤30 minutes is current; an external request delays assessment at most 2 seconds. Price/availability guidance is current through 24 hours inclusive. Preserve source/time, reduced-context meaning and utility independent of navigation. |
| Exact contextual intelligence and reported behavior | FR-ANL-010–015; FR-MULT-002–008; FR-WEAR-003/004/006–010; FR-PERS-012/013 | Positive-priority Coverage sufficiency/weights; complete same-basis unique-set utility; input/rule currentness. Retry identity differs from a new same-outfit/day intention; accepted survivor/time effects stay coherent. |
| Recoverable operation and trustworthy presentation/evidence | NFR-REL-003; NFR-SUP/TEST; NFR-USE/ACC; SRS Sections 12.3/12.4 | Restore core service within ≤5 minutes after a recoverable validation restart without silent accepted-state corruption; no production SLA/RPO is inferred. Preserve truthful labels, accessibility and protected evidence. |

The five-second processing target is a validation measure, not a chosen universal AI timeout. Weather's two-second delay boundary is an existing product rule. No general API/Multiplier latency, wardrobe maximum, additional retention limit or production availability target is added.

## 5. Architectural Drivers

The seven driver names/IDs below come directly from [ASR Section 4](ASR.md). AD-01–AD-07 are drivers, distinct from three-digit Activity Diagram IDs and later ADRs. Their nine ASRs remain requirement pressures; the response column states this ADD's selected tactics.

| Driver / ASR | Source | Architectural Pressure | ADD Response |
| --- | --- | --- | --- |
| AD-01 — Protect private authority and purpose-limited information; ASR-SEC-001/002 | ASR Section 4; BR-021; NFR-SEC-*; NFR-PRIV-*; DATA-RET-001–003; QA-SEC-01–03; ASR-SEC-001/002 | Account/session boundaries, permitted use, lifecycle and protected external/operating evidence. | Persistent account/session authority, current authorized media access and purpose/lifecycle owners (§§6/9.1/9.3/9.5). |
| AD-02 — Preserve coherent accepted state and logical actions; ASR-REL-001 | ASR Section 4; DATA-INT-001–004; DATA-WEAR-002–004; BRULE-WEAR/PERS; QA-REL-01/02, QA-SCA-01; ASR-REL-001 | Mutation acceptance, safe retry, concurrency, surviving evidence and time interpretation. | Durable logical-action acceptance and deliberate local transactions/concurrency through public owner operations; survivor/time effects (§§6/9.2). |
| AD-03 — Preserve trustworthy shared validity and exact intelligence; ASR-CON-001 | ASR Section 4; BR-009/014–017/022; NFR-REL-002; DATA-OUT-001–003, DATA-ANL-004; QA-CON-01; ASR-CON-001 | Consistent domain meaning, complete same-basis assessment, identity and currentness. | Shared validity/identity meaning; authoritative request-time inputs, complete same-basis assessment and change-aware result states (§§6/9.7). |
| AD-04 — Complete expensive operations within established timing; ASR-PERF-001 | ASR Section 4; NFR-PERF-001/002; NFR-SCA-001; NFR-TEST-003; QA-PERF-01/02; ASR-PERF-001 | End-to-end coordination and resource use without weakening result integrity. | Private supervised AI boundary, bounded full-result interaction, efficient local evaluation and protected whole-operation timing (§§6/9.5/9.9). |
| AD-05 — Preserve bounded dependency semantics and independent core value; ASR-INT-001 | ASR Section 4; SI-001–004; NFR-INT-001; NFR-AVL-001; QA-INT-01, QA-AVL-01; ASR-INT-001 | Minimal exchange, bounded environmental acquisition, failure propagation and optional enrichment. | Narrow provider ports/adapters/ACL, bounded weather acquisition and explicit optional-effect isolation (§§6/9.6). |
| AD-06 — Preserve recoverable and inspectable operation; ASR-REL-002/TEST-001 | ASR Section 4; NFR-REL-003; NFR-TEST-001–003; NFR-SUP-001/002; AI-REQ-010/011/014; QA-REL-03, QA-TEST-01, QA-SUP-01; ASR-REL-002/TEST-001 | Restoration, accepted-state survival, controlled operation and protected reproducible evidence. | Durable accepted state, resumable required work, controlled process recovery and protected operating/benchmark evidence (§§6/9.9). |
| AD-07 — Preserve understandable and accessible outcome meaning; ASR-USE-001 | ASR Section 4; NFR-USE/ACC; LOC-001–005; NFR-MNT-001; QA-USE-01/02, QA-MNT-01; ASR-USE-001 | Meaningful explanations/state information across contributors and labels independent of canonical semantics. | Canonical semantic language plus explanations/limitations carried to Vietnamese accessible mobile actions (§§6/9.8). |

Reliability and Security retain Critical significance; Performance, Interoperability, Conceptual Integrity, Usability and Testability are High. Availability, Scalability, Maintainability, Flexibility, Supportability and Manageability remain Medium; Reusability remains Low. This design does not upgrade classifications to justify extra infrastructure. §12 maps every formal ASR to its selected decisions and verification responsibility.

## 6. Quality Attribute Requirements and Architectural Tactics

Fifteen existing scenarios are included because they constrain authority, coordination, execution boundaries, dependency behavior or cross-contributor evidence. Their six source fields are reproduced from [QA analysis](quality-attribute-analysis.md) without changed measures; only “Source of Stimulus” is relabeled **Stimulus Source**. Artifact descriptions retain the source's logical scope. The added tactics show how the selected architecture must respond, not that it already passes.

QA-MNT-01 (display-label regression) and QA-USE-01 (representative-user task study) remain linked source obligations in §§9.8/12/13, rather than duplicate scenario tables here. All 14 classifications remain authoritative in QA Section 5. No omitted table waives its requirement or completion evidence.

### 6.1 QA-PERF-01 — Reviewable garment processing

Sources: SRS NFR-PERF-001, NFR-TEST-003, Section 3.4.1 and Section 12.3; UC-007; AD-007; PBI-013/035. Scenario body: [QA Section 6.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Authenticated User submitting a supported garment image. |
| Stimulus | Analysis request is accepted after image transfer completes. |
| Environment | SRS reference Android 10+ mid-range device with ≥6 GB RAM, or iOS 15+ iPhone 11-class or newer; stable Wi-Fi/4G-class network with download ≥20 Mbps, upload ≥5 Mbps and RTT ≤100 ms; supported image under Section 3.4.1. |
| Artifact | Garment-analysis operation through availability of its review result. |
| Response | Make both a usable processed preview and reviewable proposals available; retain user confirmation as authority. |
| Response Measure | p95 ≤5 seconds from accepted post-transfer analysis request to both outputs. Use 5 warm-ups then ≥100 measured executions; retain legitimate slow runs and record device/network/environment. Upload time and user review time are outside this interval. |
| Architectural Tactics | ADA-004/005/007/013/014; ASR-PERF-001. Enforce qualified-input bounds, isolate supervised Python analysis and await a bounded complete result. Correlate the post-transfer start through availability of both outputs across core/AI/media handling; resource limits and timeout realization follow views/design. Failure remains explicit/manual, not completed processing. |

### 6.2 QA-PERF-02 — Complete daily outfit result

Sources: SRS NFR-PERF-002, NFR-SCA-001, NFR-TEST-003 and Section 12.3; UC-011; AD-011; PBI-017/035. Scenario body: [QA Section 6.2](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Authenticated User requesting daily outfits. |
| Stimulus | Outfit request is accepted with required context available. |
| Environment | The reference devices/network specified in QA-PERF-01; nominal wardrobe manifest contains exactly 100 confirmed garments with applicable eligibility and readiness identified. This is separate from the 20-user functional workload. |
| Artifact | Current-wardrobe recommendation operation. |
| Response | Evaluate eligible inputs, apply hard validity before ranking and make the first complete recommendation result set available, retaining actual fewer/no-valid outcomes where appropriate. |
| Response Measure | p95 <3 seconds from accepted request to the first complete result set. Use 5 warm-ups then ≥100 measured executions, retain legitimate slow runs and record conditions. No silent omission of eligible inputs, duplicate padding or invalid alternatives. |
| Architectural Tactics | ADA-001/003/010/011/014/016/017; ASR-PERF-001 / ASR-CON-001. Obtain coherent authoritative inputs through published local owners; filter applicable readiness/validity before personalized ranking. Profile complete computation and data access without a dedicated cache, eligible-input truncation or duplicate padding; collect full-result timing and manifest evidence. |

### 6.3 QA-AVL-01 — Optional dependency failure preserves value

Sources: SRS NFR-AVL-001, FR-SHOP-006–008, FR-MET-007, ERR-SHOP-003 and ERR-MET-001; BRULE-SHOP-002/003; UC-023; PBI-009/033/038. Scenario body: [QA Section 7.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Optional external shopping destination or measurement dependency. |
| Stimulus | A selected navigation cannot proceed, or measurement becomes unavailable during an otherwise usable core action; exercise each failure independently. |
| Environment | Authorized connected use with available wardrobe data and a current supported candidate assessment; required product inputs remain valid. |
| Artifact | Candidate/gap/utility presentation and independently usable core-action paths. |
| Response | Explain affected unavailability, retain otherwise valid candidate information, gap, multiplier, reasons and previews where available, and preserve the core action's actual outcome. |
| Response Measure | Each exercised optional failure leaves the independent core path usable. Destination failure alone produces no +0, ownership, Wear or transaction state; unavailable measurement neither blocks/reverses the core action nor claims successfully recorded evidence. No offline or destination-uptime claim. |
| Architectural Tactics | ADA-006/012/014/017/018; ASR-INT-001. Keep optional navigation/measurement effects outside required core acceptance. Adapters preserve their actual failure meaning, while published outcomes retain valid independent utility and reasons. Neither unavailable telemetry nor external navigation may manufacture a purchase, Wear or numeric utility state. |

### 6.4 QA-REL-01 — Manual continuation and authoritative correction

Sources: SRS NFR-REL-001, FR-AI-003/007/008/010, AI-REQ-007, DATA-INT-001 and ERR-AI-001/002; BRULE-GAR-001–003; UC-007/AD-007; US-GAR-001 AC-01/02/09/12. Scenario body: [QA Section 8.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | User and unavailable, failed or uncertain garment-analysis capability. |
| Stimulus | Image assistance cannot provide useful information; the User supplies and confirms the manual minimum. Later automated output disagrees with the accepted values. |
| Environment | Authorized connected entry, exercising all agreed representative AI-failure cases; manual saving capability remains usable. |
| Artifact | Garment confirmation and authoritative wardrobe profile. |
| Response | Permit imageless manual review/confirmation, acknowledge accepted creation separately from draft/failure, explain applicable readiness and retain confirmed/corrected values after later analysis. |
| Response Measure | Manual creation succeeds in every agreed representative AI-failure case when the confirmed minimum is accepted. An unconfirmed/canceled attempt establishes no garment; retry of an accepted addition creates no unexplained duplicate; later analysis silently overwrites no authoritative value. |
| Architectural Tactics | ADA-003/004/005/009/015/018; ASR-REL-001. Place canonical acceptance under user confirmation, with a usable imageless/manual path independent of AI. Persist the accepted logical addition coherently; keep proposals/late analysis outside that authority. Expose draft, failed, uncertain and accepted meaning to prevent retry duplication or automated overwrite. |

### 6.5 QA-REL-02 — Wear retries, correction and survivor effects

Sources: SRS FR-WEAR-003/004/006–010, FR-PERS-012/013, DATA-WEAR-003/004, DATA-INT-004 and ERR-WEAR-001/002; BRULE-WEAR-002–006, BRULE-PERS-003/005/006; UC-014/016/017 and AD-014/016/017; Business Rules Section 15, B11–B14/B19/B20. Scenario body: [QA Section 8.2](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Authenticated User reporting and repairing Wear Events; interrupted communication. |
| Stimulus | Retry a logical report after an uncertain response, initiate a separate same-outfit/day report, then correct or remove the selected accepted report. |
| Environment | Controlled valid owned outfit and known accepted timestamps; original event-local date/time is preserved even after device-timezone change. Exercise accepted, rejected, canceled and uncertain mutations. |
| Artifact | Wear history, utilization, recency and effective normalized ranking evidence. |
| Response | Resolve the original intention without duplication, preserve separate intentional reports, and update only accepted correction/removal effects using surviving report memberships and absolute anchors. |
| Response Measure | One logical retry retains one event; a new explicit report remains distinct. Same exact outfit/original day supplies at most one +2 increment before prescribed decay. Accepted correction/removal recomputes the latest surviving anchor and elapsed age/recency; no survivors means no group Wear contribution. Invalid future/cross-original-day corrections and canceled/known failed mutations preserve accepted state; uncertainty makes no false completion/absence claim. Underlying Outfit/Garments and unrelated reports remain intact. |
| Architectural Tactics | ADA-003/009/010/016/017; ASR-REL-001. Persist the logical action and accepted event effects through public owner operations and deliberate local coordination. Recalculate affected surviving memberships, absolute anchors and effective consumers before claiming the accepted result; derive coherent state or coordinate required changes. Keep original-local-day grouping distinct from elapsed absolute-time aging. |

### 6.6 QA-REL-03 — Recoverable restart

Sources: SRS NFR-REL-003 and Section 12.3; NFR-AVL-001; PBI-038/040; DoD Section 5 operational trigger. Scenario body: [QA Section 8.3](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Recoverable CapsuleAI application/service interruption in validation. |
| Stimulus | A recoverable application/service restart occurs after authoritative user information was accepted. |
| Environment | Agreed validation environment with known accepted account, wardrobe and relevant operation state; exercise applicable optional-dependency fallback. |
| Artifact | Core authorized access, wardrobe and advice service behavior. |
| Response | Restore core operation with previously accepted authoritative information intact and applicable optional-dependency fallbacks available. |
| Response Measure | Core service is restored within ≤5 minutes after the recoverable restart; controlled accepted-state checks show no silent corruption and applicable fallback checks pass. This is neither a disaster-recovery/RPO requirement nor a production uptime SLA. |
| Architectural Tactics | ADA-003/007/008/009/012/013/014; ASR-REL-002. Preserve account/session/business authority and necessary recovery work durably, supervise core/AI processes and restore usable authorization before protected readiness. Resume applicable media/lifecycle work after restart and inspect accepted outcomes/fallbacks. Deployment placement and operating procedures remain downstream. |

### 6.7 QA-SEC-01 — User and account isolation

Sources: SRS FR-AUTH-008/009, NFR-SEC-001–003, DATA-GAR-001 and COM-001; BRULE-AUTH-001; UC-002/003/008; US-AUTH-002 AC-06, US-AUTH-004 AC-04, US-WAR-001 AC-09, US-WAR-002 AC-11. Scenario body: [QA Section 9.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Unauthenticated requester or authenticated User attempting another account's protected information. |
| Stimulus | Attempt protected wardrobe/image access, including after logout or switching accounts on a device. |
| Environment | Agreed unauthenticated and two-account validation fixtures, with prior protected content displayed and current-session authority known. |
| Artifact | Personal operations, garment images and protected client-visible results. |
| Response | Deny unauthorized access, expose only the current authorized account's content and give appropriate renewed-access guidance without disclosing secrets or labeling inaccessible data empty. |
| Response Measure | Zero successful unauthorized cross-user wardrobe/image accesses in the agreed scenarios. Account switch/logout exposes no preceding-user protected content; credential/token/recovery secrets appear in no unauthorized output or ordinary measurement. |
| Architectural Tactics | ADA-002/006/007/008/016/017; ASR-SEC-001. Gate protected operations and media delivery on valid access plus current usable account/session ownership; owner contracts enforce authorization without exposing private internals. Mobile results are account-scoped and prior protected presentation is cleared on logout/account change. Private object storage alone does not establish access correctness. |

### 6.8 QA-SEC-02 — Session and recovery boundaries

Sources: SRS Section 3.1.1, FR-AUTH-004/007/010/013/014/017/018, DATA-AUTH-005 and SI-003; BRULE-AUTH-004–006; UC-002/003/004; AD-004; US-AUTH-003 AC-02–08, US-AUTH-004 AC-01/02/06 and US-AUTH-005 AC-01–09/13–15. Scenario body: [QA Section 9.2](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Returning User, repeated refresh credential, or User initiating/completing email recovery. |
| Stimulus | Renew at access expiry, reuse a consumed Refresh Token, log out of a selected session, or issue/complete a reset interaction; exercise these policy branches independently. |
| Environment | Known account and separate device/session fixtures with controlled issuance/expiry time and successful, failed or uncertain email/reset outcomes. |
| Artifact | Account access, Refresh Sessions and single-use reset validity. |
| Response | Apply the established lifetime/rotation/revocation boundaries, require authentication where authority is unusable, and distinguish initiation/delivery from actual completed reset. |
| Response Measure | Expired 15-minute access alone grants no protected use. Rotation consumes the current refresh credential and does not extend the maximum 30-day session; reuse revokes the affected session. Logout targets only the current session. Reset permits one success before its 30-minute expiry; successful replacement invalidates earlier unused interactions. Completed reset revokes all account Refresh Sessions and requires the new password; unaccepted delivery/reset does not claim completion. Registered/unregistered initiation responses are equivalent and account-safe. |
| Architectural Tactics | ADA-003/006/008/009/017; ASR-SEC-001. Coordinate session/refresh/reset validity and related account changes in PostgreSQL through explicit acceptance boundaries. Consume single-use credentials coherently under concurrency and enforce revocation at protected use; a valid JWT signature cannot bypass unusable authority. Treat email initiation/delivery as separate from completed reset. |

### 6.9 QA-SEC-03 — Purpose limitation and data lifecycle

Sources: SRS NFR-PRIV-001/002, SI-001–004, COM-003, FR-PROF-007, FR-WAR-005, FR-WEAR-007, FR-MET-006, DATA-RET-001–003 and Section 12.1; BRULE-AUTH-007, BRULE-GAR-009, BRULE-WEAR-005, BRULE-HIST-002/003; UC-005/010/017/023; PBI-002/009/016/023/041. Scenario body: [QA Section 9.3](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | User accepting personal-data removal; product measurement and external interactions using private information. |
| Stimulus | Remove optional profile information, a garment or an individual Wear Event; inspect personal-data uses and measurement at its retention boundary. |
| Environment | Authorized controlled data-lifecycle fixtures and inspectable processing/exchange evidence, with known acceptance and retention times. |
| Artifact | Current personalization/ownership/evidence, minimal history, removed personal data, user-linked measurement and external disclosures. |
| Response | Stop effective use at the applicable accepted boundary, preserve only necessary functional history, enforce deletion/measurement limits and purpose-minimal processing/exchange. |
| Response Measure | Removed optional context stops future personalization use; accepted garment/event removal immediately excludes the applicable current/effective inputs. Applicable removed event data and original removed-garment images/nonessential data are physically deleted within 30 days. User-linked measurement is deleted or aggregated/de-identified within 90 days with older retained measurement no longer user-linked. Necessary unremoved history/feedback is not erased solely by ranking expiry. MVP personal data enters no AI training/improvement. Selected-image analysis remains purpose-limited under SI-001; weather, recovery and shopping exchanges contain only necessary selected location, recovery-delivery information or explicitly chosen navigation, with no unrelated private wardrobe/profile/history. |
| Architectural Tactics | ADA-004/006/007/009/012/014/016/018; ASR-SEC-002. Withdraw accepted data from effective owner contracts/consumers immediately, preserve only justified minimal functional history and track restart-surviving physical deletion/measurement expiry. Include applicable media versions/copies and derived uses in lifecycle ownership; keep provider disclosure and benchmark/measurement purposes minimal and separate. |

### 6.10 QA-SCA-01 — Concurrent functional integrity

Sources: SRS NFR-SCA-002 and Section 12.3; FR-AUTH-008, DATA-INT-004; PBI-038. Scenario body: [QA Section 10.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Concurrently active authenticated Users. |
| Stimulus | Exercise core access, wardrobe/recommendation and applicable mutation/retry operations concurrently. |
| Environment | Agreed validation environment with 20 concurrently active authenticated users and known ownership, accepted-state and logical-action fixtures. |
| Artifact | Core operations and each user's accepted personal state. |
| Response | Keep core operations functional and preserve authorization, isolation, accepted state and logical-action outcomes under concurrent use. |
| Response Measure | At 20 active users, no cross-user leakage, broken authorization, corrupted accepted state or accidental duplicate logical actions are caused solely by concurrency; exercised core operations remain functional. No p95-at-20-users, throughput or higher-concurrency target is inferred. |
| Architectural Tactics | ADA-003/008/009/010/016/017; ASR-REL-001. Design database concurrency and owner-level coordination around accepted-action, authorization and assessment-basis invariants. Protect private inputs and durable action identity across competing operations; no event timing or unrestricted cross-owner access may bypass coherent effects. Exact isolation/locking is later design; this workload selects no distributed scaling infrastructure. |

### 6.11 QA-INT-01 — Weather freshness and bounded fallback

Sources: SRS FR-WEATHER-003–005, SI-002, ERR-WEATHER-001 and Section 12.2; BRULE-PROF-003, BRULE-OUT-006; UC-006/AD-006 and consuming UC-011/019/022; PBI-011. Scenario body: [QA Section 14.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Weather Information Provider during selected-context acquisition. |
| Stimulus | Supply stale information or fail to return usable current weather within the allowed external-request assessment delay. |
| Environment | Optional consented location or independently selected manual city, authorized connected assessment and sufficient remaining information for defensible reduced-context use. |
| Artifact | Weather-context exchange and affected recommendation/Coverage/Multiplier interpretation. |
| Response | Request only necessary selected location, check actual evidence timestamp, attempt refresh of stale weather and disclose unavailable environment when usable current information is absent by the boundary. Preserve all remaining hard validity. |
| Response Measure | Weather at age ≤30 minutes is current; older evidence is refreshed before current use. The external request delays assessment by at most 2 seconds; without usable current information by then, reduced-context behavior skips only unavailable environmental filtering. No invented weather, blanket outfit rejection or unrelated wardrobe/profile/history disclosure; changed relevant basis refreshes or marks affected advice Outdated. |
| Architectural Tactics | ADA-006/010/017/018; ASR-INT-001. Acquire context through the UC-006-governed boundary, translate provider evidence with actual source/time and apply the existing freshness/wait policy. Consumers obtain the resulting public context rather than each becoming an acquisition goal. Explicit limited-context outcomes preserve remaining validity and make changed evaluation basis visible. |

### 6.12 QA-CON-01 — Consistent valid sets and honest utility

Sources: SRS FR-OUT-003/007/009, FR-ANL-010–015, FR-GAP-004, FR-MULT-002–008, DATA-OUT-001, DATA-ANL-004 and NFR-REL-002; BRULE-OUT-008/009, BRULE-COV-002–006, BRULE-MULT-001–009; Business Rules Section 15 B05/B07–B09/B16/B17; UC-019/020/022; AD-019/020/022; PBI-026/028/030/031/039. Scenario body: [QA Section 15.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | User requesting Coverage or candidate utility, including after a relevant input/rule change. |
| Stimulus | Evaluate controlled valid-set fixtures, vary information sufficiency/completion, reorder constituents or change the previously evaluated basis. |
| Environment | Authorized current wardrobe and hypothetical candidate with explicit readiness, priorities, context and relevant rule version; fixtures include complete, zero, partial, unavailable and changed-basis conditions. |
| Artifact | Valid outfit identity, Coverage/Gaps and Multiplier count/state/previews. |
| Response | Use identity-set uniqueness and applicable hard validity consistently, apply Coverage sufficiency/weighted meaning, and claim exact candidate utility only from complete same-basis sets. |
| Response Measure | Permuting the same constituent identities adds no outfit. Existing complete 41-current/58-expanded fixture gives +17; identical completed sets give Evaluated Zero/+0. Missing/partial/unavailable evaluation yields no exact +N or false +0, and relevant changes yield Outdated until current evaluation is established. Existing weighted Coverage fixture B09 gives 50; missing positive priorities/required ready roles yields no fabricated 0% or gap. Previews contain the hypothetical candidate and share the count's basis; assessment/viewing changes neither ownership nor Wear history. |
| Architectural Tactics | ADA-009/010/015–018; ASR-CON-001. Assign accountable shared validity/identity semantics and use authorized current owner inputs with coherent basis checks. Establish Coverage sufficiency and complete unique current/expanded sets before exact utility, associating count/reasons/previews with that basis. Detect relevant changes, withhold current exact claims and prevent candidate evaluation from mutating ownership/history. |

### 6.13 QA-USE-02 — Accessible actions and state meaning

Sources: SRS NFR-ACC-001/002 and Section 12.4; LOC-001; PBI-036; current stories' accessibility AC, including US-GAR-001 AC-14 and US-WAR-002 AC-12. Scenario body: [QA Section 16.2](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | User relying on a screen reader or enlarged text. |
| Stimulus | Operate applicable core journeys and interpret confidence/readiness, non-exact utility, hypothetical and removed states. |
| Environment | Supported Android/iOS; Android TalkBack and iOS VoiceOver; text scaling up to 200%. |
| Artifact | Vietnamese actions, interactive semantics, explanations and state presentation. |
| Response | Provide meaningful accessible names/roles/states and labels beyond color or image alone, retaining operable primary actions and accurate outcomes. |
| Response Measure | Applicable core journeys are operable under both screen readers and at text scaling up to 200%; primary-control targets meet the approximately 48 dp Android/44 pt iOS minimum criteria. Confidence, readiness, Incomplete/Unavailable/Outdated, hypothetical and Removed from wardrobe remain understandable without color/image-only cues. These checks make no certification claim. |
| Architectural Tactics | ADA-005/006/010/014/015/018; ASR-USE-001. Carry stable canonical meanings, limitations and recovery choices through published outcomes/ACLs to localized mobile presentation. Mobile supplies accessible action semantics, labels and operability rather than inferring state from image/color alone. Integrated client/core checks must establish the source accessibility criteria. |

### 6.14 QA-TEST-01 — Reproducible garment-assistance evaluation

Sources: SRS Section 8.1, AI-REQ-010/011/014, NFR-TEST-002 and NFR-USE-002; PBI-014; DoD Section 5 AI trigger. Scenario body: [QA Section 17.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | QA/AI contributors executing the established acceptance evaluation. |
| Stimulus | Evaluate garment assistance against the locked qualified-image benchmark and inspect its evidence. |
| Environment | Locked set of ≥200 qualified images with ≥50 each for TOP, BOTTOM, OUTERWEAR and FOOTWEAR; human category and controlled dominant-color-family ground truth. Two independent annotators should label with disagreement adjudicated; the locked corpus is excluded from training/tuning after lock. |
| Artifact | Garment-assistance results, benchmark/label manifests and independent preview review. |
| Response | Produce inspectable results tied to qualified inputs and the evaluated implementation, reporting category and dominant-color correctness separately from preview usability and richer attributes. |
| Response Measure | Evidence establishes the required set size/balance and evaluation isolation. Category accuracy is ≥90% overall and ≥85% within each primary-category group; dominant-color accuracy is ≥90% overall, reported separately. Preview review checks identifiable garment, preserved major regions, non-obstructive background and sufficient correction/confirmation information under SRS Section 3.4.1; no preview percentage or equal-accuracy rich-attribute claim is introduced. |
| Architectural Tactics | ADA-004/005/014/015/018; ASR-TEST-001. Keep domain/provider boundaries testable and analysis output tied to qualified input and evaluated implementation. Provide protected benchmark manifests/results and separate category, color and preview evidence. Use the locked controlled corpus; MVP personal information remains excluded from training/improvement, and confidence is not measured accuracy. |

### 6.15 QA-SUP-01 — Diagnose an unavailable operation safely

Sources: SRS NFR-SUP-001/002, NFR-TEST-002, NFR-SEC-003, FR-WAR-008, FR-MET-006 and ERR-NET-001; UC-008; US-WAR-001 AC-03/05/09 and US-WAR-002 AC-07/08/11; PBI-040. Scenario body: [QA Section 18.1](quality-attribute-analysis.md).

| Field | Description |
| --- | --- |
| Stimulus Source | Failed/interrupted wardrobe operation encountered by a User and inspected by authorized validation/support contributors. |
| Stimulus | Retrieval fails, or interruption leaves an operation outcome unconfirmed; inspect the state and its applicable recovery/evidence. |
| Environment | Controlled accepted inventory and authorized test/operating context; unavailable retrieval, unusable access and genuinely empty inventory are separate cases. |
| Artifact | Operation-state presentation, recovery choices and permitted diagnostic/validation evidence. |
| Response | Identify the actual pending/failed/unavailable/unconfirmed condition, preserve accepted state, provide applicable retry/outcome-review/renewed-access guidance and support protected inspection. |
| Response Measure | Each case is distinguishable from confirmed success and genuine empty inventory; an uncertain result claims neither success nor definite absence. Applicable recovery is available and accepted inventory remains intact. Evidence distinguishes attempted/accepted outcomes and required timing information without exposing credential/token/recovery secrets or requiring raw images/sensitive profiles in ordinary engagement diagnostics. No resolution-time SLA is added. |
| Architectural Tactics | ADA-008/009/012/013/014/017/018; ASR-TEST-001 / ASR-REL-002 / ASR-USE-001. Preserve durable accepted-action outcomes and public pending/failed/uncertain semantics for recovery. Protected structured operation/timing evidence permits diagnosis without secret/content leakage. Readiness distinguishes unavailable authority, unavailable retrieval and actual empty inventory. |

## 7. Selected Architectural Decisions Realization

All ADA-001–018 remain Selected in the [selection authority](selected-architecture-decisions.md). The shorthand below summarizes realization; exact selected Option wording and analytical alternatives remain in the owning selection/ADA files. Every row constrains later views rather than creating those artifacts now.

| ADA | Selected Direction | ADD Architectural Realization | Principal Downstream View |
| --- | --- | --- | --- |
| ADA-001 | Modular Monolith business core | One coordinated application with domain-oriented responsibilities, local owner collaboration and coupled core releases. | Logical / Implementation |
| ADA-002 | Java / Spring Boot business runtime | Framework/transport/configuration at the edge; application acceptance coordinates domain policies and private integrations. | Implementation |
| ADA-003 | PostgreSQL primary relational authority | Persist related accepted/session/action information coherently; deliberate concurrency and logical owner access. | Data / Implementation / Deployment |
| ADA-004 | Private separate Python analysis | Purpose-minimal proposal/preview execution isolated from authoritative wardrobe mutation. | Implementation / Deployment |
| ADA-005 | Bounded synchronous complete AI result | Await usable preview plus proposals, disclose noncompletion and retain independent manual continuation. | Implementation / Deployment |
| ADA-006 | Narrow external ports/adapters | Express CapsuleAI outcomes at application boundaries; keep provider types/errors and meaningful ACL translation outside core policy. | Logical / Implementation |
| ADA-007 | Private object storage / authorized delivery | Separate media resource with current-account/session-controlled access and recoverable association/purge. | Deployment / Data / Implementation |
| ADA-008 | Database-backed session/reset authority | Coordinate refresh/reset/account validity and apply current usable authority to protected operations. | Data / Implementation |
| ADA-009 | Durable logical-action / coherent effects | Persist accepted identity/outcome and preserve retry versus new intention; local ACID through owner boundaries where needed. | Logical / Implementation / Data |
| ADA-010 | Authoritative request-time intelligence | Shared validity/identity, coherent inputs, completion/currentness evidence and full same-basis utility; no copied state as a shortcut. | Logical / Implementation / Data |
| ADA-011 | No dedicated cache initially | Authority/direct evaluation first; profile complete operations before evidence-driven acceleration. | Implementation / Data |
| ADA-012 | Explicit core coordination; no broker initially | Coordinate required immediate effects and durable resumable later work; local events only where semantics fit. | Logical / Implementation |
| ADA-013 | Small controlled supervised deployment | Separate managed core/AI processes and durable resources; placement/capacity remain evidence-driven. | Deployment |
| ADA-014 | Protected structured operating evidence | Minimal outcome/timing/assessment-basis, health and recovery evidence with authorized access and purpose/lifecycle control. | Implementation / Deployment |
| ADA-015 | Pragmatic Clean Architecture per module | Domain/application dependencies point inward; needed ports, outer adapters and edge wiring without interface-per-class ceremony. | Logical / Implementation |
| ADA-016 | One PostgreSQL; context-owned logical persistence | Allocate mutation/lifecycle owners and public operations; no default cross-context repository/entity/table/schema bypass. | Logical / Data / Implementation |
| ADA-017 | Published contracts; semantic call/event choice | Immediate authority/current results use deliberate local coordination; independent reactions may be events with explicit delivery/recovery semantics. | Logical / Implementation |
| ADA-018 | Published language / meaningful ACL translation | Keep internal/provider models private, translate different meanings and retain shared rule semantics without a universal entity model. | Logical / Implementation / Data |

### 7.1 Dependency and Acceptance Shape

Inbound adapters translate an authorized user/module request into application orchestration. Application responsibilities depend on domain meaning and needed ports; outbound adapters implement persistence, private analysis, provider and neighboring-owner boundaries. Configuration assembles concrete implementations at the edge. Spring/JPA/HTTP/SDK/provider types remain outside domain/application decisions. No maximal layer count, generic repository or mapper for every object is required.

Acceptance is explicit: owners validate the intention and authorized current basis, coordinate required authoritative effects, establish the durable outcome, then expose its actual accepted or nonaccepted meaning. Other responsibilities receive permitted information/operations through published contracts. Optional measurement may react independently; private media/email and required later purge need recoverable coordination. A synchronous local call alone does not prove coherent multi-call reads under concurrent changes.

### 7.2 Coherent Runtime Collaboration

The mobile/core boundary carries confirmed user intentions and truthful state/explanation information. The core/private-AI boundary carries assistance and its completion evidence; it cannot replace confirmation. Core/persistence/media boundaries distinguish database acceptance from external resource outcomes. Advice/intelligence consume authorized owner information under a coherent evaluation basis, then return valid recommendations or defensible assessment states.

These responsibilities are allocated by meaning and ownership. Healthy boundaries support later selective extraction, while present correctness uses local collaboration and transactions. A context need not map one-to-one to a future service. §8.5 records only conditional evolution, not an infrastructure plan.

## 8. Architectural Representation

These summaries define the responsibilities of the four canonical views. **Data View review → ADRs** is the current handoff. [Logical View v0.1](views/logical-view.puml), [Implementation View v0.1](views/implementation-view.puml) and [Deployment View v0.1](views/deployment-view.puml) are human-accepted baselines in the current Data View instruction. [Data View v0.1 — Baseline Draft](views/data-view.puml) is available for review. Then follow ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation.

### 8.1 Logical View — Summary / Intended Responsibility

Allocate the cohesive responsibility concerns in §3.2 to domain-first owners. Show authority, dependencies, public collaborations and the semantic ownership of validity/identity, accepted-action effects, personal lifecycle and evaluated context. Distinguish current ownership from history and hypothetical candidates, and environmental acquisition from its consumers. Identify immediate/coherent interactions versus independent reactions, including any invariant coordinated across owners.

[Logical View v0.1](views/logical-view.puml), governed by Workflow Section 21, is the canonical decomposition accepted by the human for this handoff on 2026-10-10. This ADD retains its responsibility obligations and does not duplicate that owner map. [Implementation View v0.1](views/implementation-view.puml) is the accepted source realization with unchanged boundaries. [Deployment View v0.1](views/deployment-view.puml) provides the human-accepted runtime representation (Workflow Section 23).

### 8.2 Implementation View — Summary / Intended Responsibility

Map the Logical View's responsibilities into the Java business core's modules/components and private Python integration. Represent pragmatic domain/application/port/adapter/configuration roles, published contracts, private internals and enforceable dependency direction. Locate framework/persistence/provider translation at the edge; include deliberate transaction/event-phase and recovery responsibilities without universal shared entities.

Workflow Section 22 governs the human-accepted [Implementation View v0.1](views/implementation-view.puml), which records source modules, public contracts and inward ports/adapters against the accepted Logical View. Exact package naming, classes/interfaces, DTOs, libraries, framework/JDK versions and enforcement tooling remain review/detailed design/engineering work.

### 8.3 Deployment View — Summary / Intended Responsibility

Represent mobile clients, the Spring Boot business process, private Python analysis process, PostgreSQL authority, private media and genuine external dependencies. Distinguish software responsibility, process, resource and deployment node; show protected access/trust boundaries, process supervision, durable-resource survival and optional-dependency behavior.

Resolve placement from measured timing/resource/recovery evidence. Separate core/AI processes do not require separate hosts; co-location remains conditional and carries contention/shared-failure risk. Workflow Section 23 governs the human-accepted [Deployment View v0.1](views/deployment-view.puml). Providers, machine/container counts, CPU/GPU, network/protocol and operating configuration remain unresolved; Kubernetes is not selected.

### 8.4 Data View — Summary / Intended Responsibility

Show major business/session/action/media/evaluation information, logical owners, authority and necessary relationships/lifecycle. Include account/session/reset validity, confirmed garment information, exact outfit identity, individual Wear records and effective survivor evidence, optional personal context, Coverage/candidate basis and minimal history. Separate authoritative state, media, permitted derived information and user-linked measurement.

Represent permitted cross-owner references/operations, effective exclusion versus physical deletion, necessary copies/versions and recoverable media association. One PostgreSQL remains compatible with logical ownership; schema names, tables, ORM, indexes, exact FK/joins/access exceptions and isolation are not finalized. Workflow Section 24 governs [Data View v0.1 — Baseline Draft](views/data-view.puml), now available for logical information/ownership/lifecycle review; no DDL or snapshot/projection store is selected here.

### 8.5 Conditional Architecture Evolution

The selected modularity preserves useful boundaries now. Future extraction needs real drivers, renewed analysis/selection and verification of existing guarantees; it is neither automatic nor a commitment to one service per module.

| Present Boundary | Possible Later Realization, Only if Justified |
| --- | --- |
| Published local contract / port | Remote adapter with separately designed authorization, compatibility and timeout/failure behavior. |
| Appropriate local domain/application event | A distinct Integration Event contract and deliberate reliable publication, delivery and consumer idempotency. |
| Logical data owner inside one PostgreSQL | Physical owner separation, possibly database-per-service, with explicit migration/reference/lifecycle design. |
| Coordinated local owner transaction | Local service transaction plus necessary cross-service coordination; compensation/Saga only if real semantics require it. |
| Required distributed publication, if a real boundary emerges | Outbox or another evidenced mechanism; not a universal current default. |
| Protected local/core-AI evidence | Expanded correlation/tracing if independently operated services require it; no platform/vendor selected. |

Incremental migration may use appropriate Strangler or Branch-by-Abstraction techniques after a justified extraction design. No broker, internal HTTP, API gateway, service discovery/mesh, distributed Saga or universal Outbox is introduced to simulate that future today.

## 9. Cross-Cutting Concerns

Each concern identifies the governing obligation, selected tactic, affected boundary and unresolved realization. These are architecture-wide coordination responsibilities, not new user goals or finalized context names.

### 9.1 Authentication, Authorization and Session Lifecycle

**Basis:** ASR-SEC-001; FR-AUTH-004/007/008/009/013/014/017/018, DATA-AUTH-005; BRULE-AUTH-001/004–006; UC-002–004. **Selections:** ADA-002/003/006/008/009/016/017.

The application establishes usable account/session authority for every protected operation, then the owning responsibility enforces access to the affected personal information, including media. A signed, unexpired JWT cannot authorize an unusable/revoked session. PostgreSQL coordinates refresh consumption/reuse, reset single-success use/replacement and account credential/session effects. Keep current-session logout, affected-session reuse revocation and completed-reset account-wide Refresh Session revocation distinct; another device is not logged out merely by current-session logout.

Mobile clears preceding-account protected presentation and distinguishes renewed-access needs from empty inventory. Recovery initiation/delivery never implies password change; registered/unregistered initial responses remain equivalent. The authority boundary spans core, owners, client and delivery adapter. Keys, secret/token representation, session-check implementation, concurrent transition protection and detailed recovery contracts remain downstream.

### 9.2 Accepted State, Idempotency, Transactions and Concurrency

**Basis:** ASR-REL-001; DATA-INT-001–004, FR-WEAR-003/004/006–010/012, FR-PERS-012/013; BRULE-WEAR-002–006 and BRULE-PERS-003/005/006; UC-007/009/010/013/014/016/017. **Selections:** ADA-003/008/009/010/012/016/017.

Durable logical-action identity/outcome is coordinated with authoritative mutation in PostgreSQL so a lost response or restart can be reviewed/retried without an unexplained second effect. A new explicit Wear intention is distinct even for the same outfit/day; account + outfit + date is not its duplicate identity. Cancellation, rejection and known failure preserve accepted state; an uncertain response establishes neither completion nor absence.

Application coordination invokes public owner operations and uses local ACID where current invariants require it. Acceptance includes coherent effective consumers, through authoritative derived reads or required coordinated changes, before the operation is represented as accepted. If a transaction touches multiple logical owners, later views document its invariant, participants and extraction coupling. Shared storage does not permit bypassing owner internals; media/email effects remain outside database atomicity.

For Wear repair, preserve the event's original local day/timezone context and reject future/cross-original-day corrections. Recompute affected outfit/day memberships, latest surviving accepted absolute anchors, elapsed-time age, recency and effective history/utilization/ranking. Grouping uses the original local date; aging uses completed elapsed 24-hour periods. Separate intentional events remain in history, while one exact-outfit/original-day group contributes at most one +2 Wear increment before prescribed decay. No survivors means no group contribution. Feedback remains outfit-targeted; removed/expired influence does not erase unrelated events or automatically clear functional feedback.

Concurrency protection must cover authority transitions, retries, removal effects and multi-owner assessment reads. PostgreSQL transaction support alone does not establish every business invariant; [official isolation guidance](https://www.postgresql.org/docs/18/transaction-iso.html) describes concurrency limits. Exact action keys, transaction participation, isolation/locking, retry-evidence representation/retention and conflict responses remain Data/Implementation View and detailed design decisions.

### 9.3 Privacy, Purpose and Data Lifecycle

**Basis:** ASR-SEC-002; NFR-PRIV-001/002, FR-PROF-007, FR-WAR-005, FR-WEAR-007, FR-MET-006, DATA-HIST-001/002, DATA-RET-001–003; SRS Section 12.1. **Selections:** ADA-004/006/007/009/012/014/016/018.

Assign lifecycle accountability with logical ownership, including derived consumers and necessary copies. Accepted optional-profile removal stops future personalization use; accepted garment/event removal immediately withdraws applicable current/effective inputs. Required physical deletion is later resumable work, not a reason to delay effective withdrawal. Applicable removed event data and removed-garment original images/nonessential personal data are deleted within 30 days, including relevant retained media copies/versions.

Minimal meaningful history may explain a removed garment as **Removed from wardrobe**, without restoring ownership or retaining original images indefinitely. Necessary unremoved history/feedback has a functional purpose distinct from both ranking's 90-day influence window and user-linked measurement's 90-day maximum. Older retained measurement must be deleted or aggregated/de-identified so it is no longer user-linked.

Core/AI/provider/evidence boundaries use only necessary authorized information. MVP personal images, profiles, Wear, feedback and corrections never enter model training/improvement. Navigation grants no unrestricted disclosure consent. Copy inventories, access enforcement, deletion tracking/scheduling and evidence retention realization remain downstream; no account-deletion/export feature or legal-certification claim is added.

### 9.4 Ownership, Published Collaboration, Events and Model Isolation

**Basis:** ASR-REL-001/CON-001/INT-001/SEC-002; DATA-INT, DATA-ANL-004 and SI-001–004. **Selections:** ADA-001/006/009/012/015–018.

One physical PostgreSQL contains logically owned persistence. Another context must not import an owner's private repository/entity or directly use its internal table/schema representation by default. Public owner contracts return permitted meaning, not mutable internal models or unrestricted persistence access. Any justified exception documents purpose, invariant/lifecycle impact, coupling and review evidence in its owning design/ADR; convenience is not an implicit exception. Exact schema/FK/joins/access rules remain downstream, rather than a universal cross-context FK ban.

[PostgreSQL schema guidance](https://www.postgresql.org/docs/18/ddl-schemas.html) distinguishes namespace organization from privilege-controlled access. Logical ownership also needs source dependency discipline and authorized public contracts; schema names or a broadly privileged application identity alone do not enforce it.

Immediate revocation/exclusion, coherent acceptance and current evaluation need deliberate local collaboration. Events fit facts/independent reactions only when delayed effects are permitted or an explicit visibility/recovery mechanism preserves immediate obligations. A **local Domain/Application Event is distinct from a future Integration Event**, and an event is not automatically asynchronous. [Spring event delivery](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html) defaults to synchronous listeners; [transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html) can target transaction phases. Those documented mechanics do not supply this design's required durable recovery; restart-surviving work must be explicit. Listener configuration/phases and event types are not selected here.

Ports define interaction dependencies, adapters implement access/transport, and ACL translation protects different meanings. Publish narrow language for current confirmed information, analysis proposals, environmental/commercial evidence and hypothetical/historical results. Do not share one universal domain/persistence model or independently duplicate validity/identity rules. **ACL is not a snapshot/projection**; required minimal historical snapshots serve a separate functional purpose. Final context language/contracts, translation placement and dependency enforcement follow the views/design.

### 9.5 AI Assistance, User Confirmation and Private Media

**Basis:** ASR-REL-001/PERF-001/INT-001/SEC-002; SI-001, NFR-REL-001, DATA-INT-001, AI-REQ-007; SRS Section 3.4.1; UC-007/AD-007. **Selections:** ADA-004–007/009/013/015/018.

The private Python runtime analyzes the selected qualified image and returns both a usable preview and reviewable proposals/uncertainty. The core awaits a bounded complete result; pending/timeout/failure remains explicit and manual entry stays independently usable. Confidence/provenance and Saveable/readiness meanings stay distinct. User confirmation/correction, not AI completion, establishes the canonical profile. Late or retried output cannot overwrite accepted values or create an owned garment.

Private object storage is a media resource reached through an application-controlled authorized delivery boundary. Object existence, a completed upload or a long-lived bearer link does not establish current access authority. Database acceptance and media operations need recoverable association/cleanup; no single cross-resource atomic transaction is claimed. Imageless manual creation requires neither successful media upload nor analysis.

The boundary spans mobile permissions/review, core authority, private analysis and storage. Transport/payloads, image access method, numeric timeout/resource limits, model/framework/license/version, provider and draft/media cleanup mechanics remain unresolved. Their realized overhead still counts within the source-defined full-result interval where applicable.

### 9.6 External Integration and Failure Isolation

**Basis:** ASR-INT-001; SI-002–004, COM-003, FR-WEATHER-003–005, FR-SHOP-001/003/006–008, FR-MET-007; UC-004/006/021–023; SRS Section 12.2. **Selections:** ADA-006/010/012/014/017/018.

Application ports express CapsuleAI success, incompleteness and unavailability; provider SDKs/DTOs/errors remain adapter concerns, with ACL where interpretation differs. Consumers do not duplicate acquisition authority. UC-006 governs selected-location/environmental acquisition; recommendation/Coverage/Multiplier consume its permitted context. Existing current provider evidence may be reused under the established freshness rule without introducing a dedicated cache.

Weather uses its actual applicable timestamp: age ≤30 minutes is current; older evidence is refreshed before current use. The external request may delay an assessment at most 2 seconds. Without usable current information then, disclose unavailable environment and skip only its unavailable environmental filtering, preserving remaining hard validity and sufficient-input gates. Never invent weather or reject every outfit solely because acquisition failed.

Recovery email receives only necessary email/delivery information and does not confirm reset success. Candidate information needs explicit user-provided support or an identifiable configured credible source, without creating a new capture/import goal. Optional price/availability retains source/last-checked time, current through 24 hours inclusive; older evidence is refreshed, explicitly stale or omitted. Shopping failure affects navigation only; supported utility/reasons/previews remain usable where available. Neither returning from a destination nor measurement acceptance creates ownership, verified purchase or Wear.

Exact providers/SDKs, contract shapes, retry/failure translation and acquired-evidence storage remain downstream. No general offline mode, retailer guarantee or additional commerce workflow is introduced.

### 9.7 Wardrobe Intelligence, Validity, Exactness and Currentness

**Basis:** ASR-CON-001; FR-OUT-003/007/009/013–017, FR-ANL-010–015, FR-GAP-001–005, FR-MULT-002–008, DATA-OUT-001–003, DATA-ANL-004; BRULE-OUT/COV/MULT. **Selections:** ADA-009/010/011/015–018.

Request-time assessment obtains authorized current inputs through published owner contracts and establishes a coherent evaluation basis despite concurrent mutation. Assign an accountable semantic owner/public meaning for garment eligibility/readiness, hard validity and garment-identity-set uniqueness across Daily, Shuffle, feedback/Wear targets, Coverage and Multiplier. This requires consistent rules, not one universal entity library or one algorithm for every consumer.

Daily advice filters hard constraints before soft preferences, including declared context and effective age-weighted behavioral evidence. Shuffle changes the chosen slot while retaining other constituents/context and validating the resulting outfit. A completed smaller/no-valid choice set is honest; invalid alternatives, padding and silent omission are unacceptable.

Coverage applies the authoritative positive-priority weighted needs and sufficient-data gates. Missing necessary priorities/ready roles cannot become 0% or a fabricated Gap. A Gap is an underserved capability/bottleneck supported by the evaluated context, not a mandatory named product.

Multiplier completes the relevant current and hypothetical expanded unique valid sets on the same context/rule/identity basis. Their incremental set difference establishes +N, not the displayed daily recommendation subset. Exact/fully evaluated +0 require completion/readiness/uniqueness evidence. **Exact, Evaluated Zero, Incomplete, Unavailable and Outdated** remain separate meanings; missing data, timeout or partial computation cannot become +0. Candidate-bearing previews, count and explanation share the same evaluated basis, and assessment/viewing changes no ownership/Wear state.

Relevant wardrobe, confirmed/candidate attributes, context or rule changes require reevaluation or explicit Outdated status; no arbitrary TTL establishes truth. Weather freshness follows its separate source policy. Exact basis/version representation, concurrency/currentness checks, enumeration/ranking optimizations and any justified later derived storage remain downstream. Profile cost without introducing a dedicated cache or quietly weakening exactness.

### 9.8 Truthful, Localized and Accessible Outcomes

**Basis:** ASR-USE-001; NFR-USE/ACC/MNT/SUP, LOC-001–005; QA-USE-01/02, QA-MNT-01 and QA-SUP-01. **Selections:** ADA-005/006/010/014/015/018.

Published outcomes carry canonical meaning, applicable evidence/limitations and available recovery so mobile does not infer acceptance or currentness from a count, image or technical transport success. Keep draft/accepted/uncertain, Saveable/readiness, confidence/provenance, inaccessible/unavailable/empty and current/historical/hypothetical states distinguishable. No backend failure is labeled empty inventory, evaluated zero or completed mutation.

Separate controlled semantic values from Vietnamese display labels. A label change cannot silently modify confirmed values, vocabulary meaning or domain behavior; QA-MNT-01 remains the before/after regression obligation. Mobile supplies meaningful accessible names/roles/states, text scaling and primary-action operability under QA-USE-02. Core/ACLs preserve the information needed to explain states beyond color/images; accessibility remains integrated client/core evidence.

QA-USE-01 and SRS Section 12.4 retain the representative-user study: at least 5 participants total, ≥80% unaided completion target per evaluated core task, zero unresolved Critical usability blockers and target average SUS ≥68 as an aid. This ADD defines semantic support rather than a test plan or UI layout. Exact response/localization structures and executable accessibility/usability checks remain downstream.

### 9.9 Protected Evidence, Required Later Work and Recovery

**Basis:** ASR-TEST-001/REL-002; NFR-TEST-001–003, NFR-SUP-001/002, NFR-REL-003, AI-REQ-010/011/014; QA-TEST-01/SUP-01/REL-03; DoD Sections 4–6. **Selections:** ADA-003/007–009/012–015/017.

Protected structured logs/metrics and health/recovery evidence distinguish attempted from accepted actions, source-defined timing boundaries and assessed basis/completeness. Collect only necessary correlation and evidence; ordinary measurement needs no raw private images/sensitive profiles or credential/token/recovery secrets. Functional history, user-linked measurement, operating evidence and the locked qualified-image evaluation remain different purposes/lifecycles. Optional measurement failure neither blocks/reverses acceptance nor pretends evidence was recorded.

Required physical deletion/retention work is tracked durably enough to resume and retry safely after restart and demonstrate its deadline. Owner responsibility follows effective removal through applicable copies/resources and actual physical completion. In-memory timers/events alone are insufficient. Scheduler/work representation, cleanup retry rules, safe external dispatch and evidence-retention configuration remain downstream; no broker or universal Outbox is required.

Supervise the separate core/AI processes and preserve accepted authority/media resources across recoverable restart. Restore authorized core access and applicable fallback before claiming usable readiness; optional AI/weather/shopping failure does not by itself disable manual wardrobe paths. Missing authoritative access/data cannot be bypassed with stale authorization. The ≤5-minute validation restoration measure is not a production uptime/disaster-recovery promise.

Later detailed design/Test Strategy will identify controllable clocks, provider fixtures, concurrent operations and inspection points to establish §§6/12 evidence. Locked AI quality results remain separate from preview review and timing; full-operation measurements retain legitimate slow runs. Exact log/event/metric shapes, health interfaces, storage/access controls, recovery commands and harness/CI tooling remain downstream.

## 10. Architecture Risks and Evidence Gaps

The risks below are source-supported uncertainties, principally ADA confidence/evidence gaps and Section 3.4 source issues. ADD-R labels identify risks within this document only; they create no new requirement, ASR or architecture decision. Selection remains complete.

| Risk ID | Risk | Related ASR / ADA | Current Mitigation | Validation / Resolution Point |
| --- | --- | --- | --- | --- |
| ADD-R01 | Java/runtime and contributor suitability is unverified; Clean/boundary discipline may erode. | ASR-TEST-001/CON-001; ADA-002/015–018 | Keep meaningful inward dependencies, public contracts and private internals; avoid ceremonial layers. | Logical/Implementation View review of representative acceptance/evaluation responsibilities; engineering baseline and later domain/contract evidence. |
| ADD-R02 | Model packaging, license, quality and compute demands are unverified. | ASR-PERF-001/INT-001/TEST-001; ADA-004/005/013 | Private proposal-only boundary, bounded full result and independent manual path. | Implementation/Deployment feasibility and actual model/resource evidence; later locked QA-TEST-01 and full QA-PERF-01 validation. |
| ADD-R03 | AI waiting/communication/media overhead or shared-host contention may miss complete-operation timing. | ASR-PERF-001/REL-002; ADA-004/005/007/013/014 | Supervised separate processes, explicit full interval and evidence-driven placement; co-location is conditional. | Deployment resource/placement review and later processing/restart evidence; do not replace completion with quick acceptance. |
| ADD-R04 | Request-time exact intelligence cost is unknown; concurrent basis changes can invalidate apparently complete advice. | ASR-CON-001/PERF-001/REL-001; ADA-009/010/011/017 | Shared semantics, public authoritative inputs and explicit basis/completion checks; truthful non-exact states. | Logical/Data/Implementation basis design and controlled QA-CON-01 fixtures; daily QA-PERF-02 profiling. No new Multiplier latency target. |
| ADD-R05 | Private media authorization/revocation, copy/version purge, volume and provider cost are unresolved. | ASR-SEC-001/002, ASR-REL-002; ADA-007/012/013 | Application-authorized delivery, recoverable association/purge and lifecycle ownership. | Deployment/Data View and provider/access feasibility; later QA-SEC-01/03 and interruption/deletion evidence. |
| ADD-R06 | Cross-owner local transactions and shared-rule/public-model boundaries may become hidden coupling. | ASR-REL-001/CON-001/SEC-002; ADA-001/009/015–018 | Document owner participation/invariants; permit justified local coordination without private persistence bypass. | Logical/Implementation/Data Views and boundary/semantic regression evidence; extraction requires renewed consistency/migration analysis. |
| ADD-R07 | Concurrency and uncertain outcomes may break credential consumption, retry identity or survivor coherence. | ASR-SEC-001/REL-001; ADA-003/008/009/016/017 | Persistent authority/actions and deliberate coherent acceptance/currentness. | Data/Implementation concurrency design; later QA-SEC-02, QA-REL-02 and separate 20-user QA-SCA-01 evidence. |
| ADD-R08 | Restart-surviving cleanup and protected operating/evaluation evidence remain unrealized. | ASR-SEC-002/REL-002/TEST-001; ADA-012–014 | Durable work responsibility, process/resource supervision and purpose-separated evidence. | Deployment/Implementation/Data Views, detailed recovery/lifecycle design and later QA-REL-03/SUP-01/SEC-03 evidence. |
| ADD-R09 | FR-MET-004 has an incomplete benchmark cross-reference; focused UC diagram links name absent files. | ADA Section 3.4; ASR-PERF-001 and interaction evidence | Use explicit SRS Sections 3.4.1/8.1.1/12.3 and stable NFR/AI IDs; use the master diagram, 23 UC specifications and 12 available activities. | Source-owner correction remains outside this ADD task; do not infer missing models or change requirements. |

No blocking contradiction between current requirements and the selected set was found. Medium confidence is a feasibility/design evidence gap, not a Deferred ADA status. Actual adverse evidence that makes a selected direction untenable must return to its owning analysis/explicit human selection, without silent option substitution.

## 11. Major Trade-offs

These are accepted costs of the selected design, not another alternatives round. Full comparisons remain in ADA.

| Selected Direction | Benefit Preserved | Accepted Cost / Design Obligation |
| --- | --- | --- |
| Modular core and logical owners | Local coherent authority and simpler operation with visible business boundaries. | Coupled core deployment, ongoing boundary enforcement and possible future physical extraction/migration work. |
| Pragmatic Clean Architecture / published language / ACL | Testable policy and private framework/provider models with explicit semantic collaboration. | Useful mapping/port discipline and contributor learning; keep granularity proportional to actual boundaries. |
| One PostgreSQL / useful local ACID | Durable related state and practical immediate invariant coordination. | Deliberate concurrency/transaction design; cross-owner participation is extraction coupling to document. |
| Separate Python / bounded synchronous result | Model/dependency isolation and clear complete-result semantics. | Communication, supervision, private image access and request/resource waiting; full timing/capacity still needs evidence. |
| Request-time intelligence / no dedicated cache | Close relationship to authoritative input and fewer stored-result invalidation surfaces. | Compute/data-access cost and concurrent basis validation; optimize only from evidence without losing exactness. |
| Explicit coordination / no broker | Fewer transport/operating surfaces and direct immediate effects. | Required durable resumable work remains explicit; less independent asynchronous scaling is accepted initially. |
| Private object storage | Media volume/lifecycle separated from transactional data. | Cross-resource association, authorization/revocation and actual copy/version purge are recoverable design work. |
| Minimal protected evidence | Inspectable acceptance, quality/timing and recovery without a full platform. | Instrumentation, privacy/access/lifecycle discipline and later reproducible validation remain necessary. |

The small deployment retains resource/shared-failure risks until placement is evidenced. No accepted trade-off permits weaker privacy, invalid personalized choices, false exactness, late effective removal or untruthful completion.

## 12. Traceability

This matrix connects existing pressures to selected tactics and later evidence. Every formal ASR and included QA scenario is covered; §7 supplies one realization row for each selected ADA. Source IDs and detailed obligations stay in the linked [SRS](../03-requirements/SRS.md), [rules](../03-requirements/business-rules.md), [QA analysis](quality-attribute-analysis.md) and [ASR](ASR.md). These destinations allocate verification responsibility, not executed tests or newly created artifacts.

| Source Requirement / QA | ASR | Selected ADA | ADD Tactic / Section | Downstream Verification |
| --- | --- | --- | --- | --- |
| FR-AUTH-008/009, NFR-SEC-001/002; QA-SEC-01 | ASR-SEC-001 | ADA-002/007/008/016/017 | Usable account/session and owner authorization; account-safe mobile/media access (§§6/9.1/9.5). | Later protected-access/media/account-switch integration and security checks. |
| FR-AUTH-004/007/013/014/017/018, DATA-AUTH-005; QA-SEC-02 | ASR-SEC-001 | ADA-003/006/008/009/017 | Persistent coordinated single-success/session/reset transitions (§§6/9.1/9.2). | Controlled expiry, rotation/reuse, separate-session, reset replacement/consumption and recovery-failure evidence. |
| NFR-PRIV-001/002, DATA-RET-001–003; QA-SEC-03 | ASR-SEC-002 | ADA-004/006/007/009/012/014/016/018 | Immediate effective withdrawal, purpose-minimal exchanges and recoverable physical lifecycle (§§6/9.3/9.9). | Later lifecycle/copy/version purge, retention and disclosure inspection; model-data-purpose evidence. |
| DATA-INT-001/004, NFR-REL-001; QA-REL-01 | ASR-REL-001 | ADA-003/004/005/009/015/018 | Confirmation authority, manual independence and durable accepted addition (§§6/9.2/9.5). | Controlled AI failure/late output, imageless creation, interrupted confirmation and retry fixtures. |
| FR-WEAR-003/004/006–010, FR-PERS-012/013; QA-REL-02 | ASR-REL-001 | ADA-003/009/010/016/017 | Distinct intentions, original-local-day/absolute aging and coherent survivor effects (§§6/9.2). | Later retry/new-intention/correction/removal and rule-boundary regressions, including surviving-anchor fixtures. |
| NFR-SCA-002, DATA-INT-004; QA-SCA-01 | ASR-REL-001 | ADA-003/008/009/010/016/017 | Deliberate authority/action/basis concurrency design (§§6/9.2/9.7). | Separate 20-user functional integrity evidence; no inferred p95-at-20-users target. |
| FR-OUT-003/009, FR-ANL-010–015, FR-MULT-002–008, DATA-ANL-004; QA-CON-01 | ASR-CON-001 | ADA-001/009/010/011/015–018 | Shared identity/validity, weighted sufficiency and complete same-basis hypothetical set difference (§§6/9.7). | Domain/basis/currentness fixtures, existing weighted 50 and 41→58/+17 cases, plus zero/non-exact/no-side-effect evidence. |
| NFR-PERF-001, NFR-TEST-003; QA-PERF-01 | ASR-PERF-001 | ADA-004/005/007/013/014 | Private supervised analysis, bounded complete output and full-interval timing (§§6/9.5/9.9). | Later full processing benchmark under source image/device/network/sampling conditions. |
| NFR-PERF-002, NFR-SCA-001, NFR-TEST-003; QA-PERF-02 | ASR-PERF-001 | ADA-001/003/010/011/014/016/017 | Request-time coherent local inputs and complete valid ranked result (§§6/9.7/9.9). | Later exactly-100-garment complete daily-result benchmark; profile cost without silent eligible-input omission. |
| FR-WEATHER-003–005, SI-002; QA-INT-01 | ASR-INT-001 | ADA-006/010/017/018 | UC-006 acquisition, provider timestamp translation and bounded reduced-context fallback (§§6/9.6). | Provider-contract freshness/wait-boundary, minimal-disclosure and affected-basis checks. |
| FR-SHOP-006–008, FR-MET-007, NFR-AVL-001; QA-AVL-01 | ASR-INT-001 | ADA-006/012/014/017/018 | Isolated optional navigation/measurement semantics (§§6/9.6/9.9). | Independent destination/measurement-failure cases preserving core acceptance and available utility. |
| NFR-REL-003; QA-REL-03 | ASR-REL-002 | ADA-003/007/008/009/012/013/014 | Durable accepted state/work, supervised recovery and usable readiness (§§6/9.9). | Later recoverable restart/restoration timing, accepted-state and applicable-fallback evidence. |
| NFR-ACC-001/002, LOC-001–005; QA-USE-02; linked QA-USE-01 / NFR-USE-001 | ASR-USE-001 | ADA-005/006/010/014/015/018 | Published truthful meanings and integrated localized accessible mobile actions (§§6/9.8). | Later TalkBack/VoiceOver, enlarged-text/action/state checks and representative-user study under SRS Section 12.4. |
| NFR-MNT-001, LOC-002/003; linked QA-MNT-01 | ASR-USE-001, ASR-CON-001 | ADA-015/018 | Canonical meanings independent of labels; focused translation (§9.8). | Before/after confirmed-value and domain-outcome regression after label changes. |
| AI-REQ-010/011/014, NFR-TEST-002; QA-TEST-01 | ASR-TEST-001 | ADA-004/005/014/015/018 | Controlled qualified benchmark and purpose-separated inspectable results (§§6/9.9). | Later locked corpus/category/color evidence and separate criterion-based preview review. |
| NFR-SUP-001/002, NFR-TEST-002, NFR-SEC-003; QA-SUP-01 | ASR-TEST-001, ASR-REL-002, ASR-USE-001 | ADA-008/009/012/013/014/017/018 | Durable outcome review, truthful failure/empty distinctions and protected evidence (§§6/9.8/9.9). | Later interruption/recovery/diagnostic inspection without secret or unnecessary private-content disclosure. |

Workflow realization remains ADD → Logical View → Implementation View → Deployment View → Data View → ADRs → necessary Detailed API/Data/Sequence/State Design → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation. Later ADRs link consequential selected direction, ASR, source ADA and affected views. Test Strategy defines executable coverage after design; applicable evidence remains required through implementation and the project-wide DoD.

## 13. Backend Developer Handoff

- **Preserve the selected architecture:** Java/Spring Boot Modular Monolith, pragmatic inward Clean dependencies, one PostgreSQL authority with logical owners/public contracts, private Python analysis and authorized private media. Keep meaningful ports/ACLs and request-time coherent intelligence; no dedicated cache or broker initially.
- **Understand before the views:** Read the governing ASRs and relevant SRS/rules/UC/activity acceptance and failure behavior. Logical ownership must allocate shared validity/identity, authority, survivor effects and lifecycle; current local ACID is useful, and any cross-owner invariant/transaction needs visible participation and extraction coupling.
- **Resolve downstream detail in order:** Logical View establishes the domain decomposition/collaboration. Implementation, Deployment and Data Views resolve source/runtime/persistence responsibilities, followed by necessary detailed contracts/algorithms/concurrency, schema/API/event design and evidence mechanisms. No final module/package/schema/provider/protocol choice is inferred here.
- **Avoid premature implementation:** This task commits no code or Sprint work and introduces no internal HTTP, Redis, broker, Saga/universal Outbox, copied-state/ACL projection, Kubernetes or distributed platform. Future extraction needs real evidence and renewed design, not a simulated service topology.
- **Keep delivery authority and evidence intact:** [Delivery Decisions](../06-scrum/delivery-decisions.md), DD-001–006, preserve 41 ordered PBIs, PBI-001–007's first refinement horizon and eight existing stories for PBI-004–007. No estimate, split/merge, order or Sprint selection changes. Applicable AC plus [DoD](../06-scrum/definition-of-done.md) remain completion criteria; a document audit does not prove working software.

**Next: Data View review → ADRs.** Review [Data View v0.1 — Baseline Draft](views/data-view.puml) against all three accepted views, authoritative requirements/rules and this ADD's selected constraints/tactics and Sections 10/12 evidence responsibilities. Do not reopen a selected ADA to fill an unresolved detailed data decision.

## 14. Status and Next Step

**Version 0.1.4 — Baseline Draft.** This navigation revision links the Data View and records explicit human acceptance of Logical, Implementation and Deployment Views on 2026-10-11. ADD review still involves Architect/Tech Lead with Backend, AI, Mobile, QA and relevant Product/BA input. All 18 selections, seven drivers, nine ASRs, 15 source scenarios and two linked validation obligations remain unchanged; no business/product/requirement change.

**Data View review → ADRs** is the current handoff. [Logical View v0.1](views/logical-view.puml), [Implementation View v0.1](views/implementation-view.puml) and [Deployment View v0.1](views/deployment-view.puml) are human-accepted baselines in the current Data View instruction. [Data View v0.1 — Baseline Draft](views/data-view.puml) is available for review. Then follow ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation.

All four architecture views now exist separately. Data View review, ADRs, API/database schemas, sequence/state diagrams, Test Strategy, engineering baseline, Sprint artifacts and code remain downstream. Known source issues and feasibility gaps remain in Section 10; Data View approval, benchmark success and software conformance are not claimed.
