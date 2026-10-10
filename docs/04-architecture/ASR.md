# CapsuleAI — Architecturally Significant Requirements

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Formal Architecturally Significant Requirements (ASR) |
| Version | 0.1.5 |
| Status | Baseline Draft |
| Product / Horizon | CapsuleAI — complete approved MVP scope |
| Ownership | Architect / Tech Lead; review with Developers, QA and Product/BA |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 17–19, 39–43 and 46 |
| Primary Analysis Input | [Quality Attribute Analysis](quality-attribute-analysis.md), v0.1 — Baseline Draft |
| Revision Note | v0.1: initial selective ASR baseline. v0.1.1: current handoff/navigation now includes decision analysis and human selection before ADD; seven drivers, nine ASRs, sources, constraints and acceptance measures are unchanged. v0.1.2 (2026-10-10): current navigation acknowledges completed selection and ADD next; ASR/driver/constraint content remains unchanged. v0.1.3 (2026-10-10): ADD now exists and Logical View is next; historical requirement-source discussion is explicitly framed, without changing ASR/driver/constraint meaning. v0.1.4 (2026-10-10): linked Logical View v0.1 and advanced current navigation to Implementation View; requirement content remains unchanged. v0.1.5 (2026-10-10): Linked Implementation View v0.1 and recorded explicit human acceptance of the Logical decomposition; review precedes Deployment View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |

## 2. Purpose

This document identifies requirement clusters that materially influence CapsuleAI's later architecture: responsibilities and boundaries, runtime coordination, authoritative information, integration, resource use, recovery and inspectable evidence. An ASR states what the architecture must be capable of satisfying. Architecture Decision Analysis compares solutions; explicit human selection precedes ADD/views describing their realization. Later ADRs preserve consequential rationale.

The analysis covers the connected AI Digital Closet, Context-Aware Styling, and Wardrobe Intelligence & Strategic Shopping MVP. The complete 41-PBI backlog defines the delivery horizon; the eight currently refined stories describe the first batch. They do not narrow architectural analysis to PBI-004–PBI-007.

| Artifact | Responsibility and relationship to this ASR baseline |
| --- | --- |
| BRD | Business goals, value, scope and trust expectations. |
| PRD | Product behavior, journeys, feature boundaries and resolved product decisions. |
| SRS / Business Rules | Authoritative software obligations, measures and domain invariants. ASRs select clusters without replacing their detail. |
| Quality Attribute Analysis | Analysis of all 14 attributes, their significance, 17 scenarios and preliminary candidates; the immediate analytical input. Scenario bodies remain there. |
| ASR | Selective architecture-driving requirements, drivers, rationale and source/evidence links. |
| Architecture Decision Analysis | Comparative options and proposed recommendations against these drivers; no automatic selection. |
| Selected Architecture Decisions | Explicit human selection or safe bounded deferral before ADD. |
| ADD / architecture views | Later responsibility, runtime, implementation, deployment and data design given these drivers and explicitly selected directions. |
| ADR | Later consequential solution decisions, alternatives, rationale and consequences. |

Source-defined policies remain binding even when they are not standalone ASRs. Current canonical CapsuleAI documents override historical proposals and reference examples. Baseline Draft does not claim artifact approval or implemented satisfaction.

## 3. ASR Selection Method

The process authority was read first, followed by the entire current QA analysis. The BRD, PRD, SRS, Business Rules, master Use Case Diagram, all 23 Use Case specifications, all 12 Activity Diagrams, Product Goal, all 41 PBIs, all eight stories and their index, DoD and Delivery Decisions were read completely. Historical CapsuleAI documents and Business Analysis notes were inspected for context. Relevant source passages were rechecked before each formal ASR was finalized.

Selection tests whether a requirement cluster has substantial cross-cutting effect, high consequence if violated, meaningful state/security/integration/resource/recovery pressure, or costly later change. An SRS ID, acceptance criterion, user importance or High/Critical QA classification alone is insufficient. Overlapping concerns are grouped by coherent architectural pressure; distinct authority and data-lifecycle pressures justify splitting the private-data candidate.

The result is **7 Architectural Drivers and 9 formal ASRs**, using dominant QA concern families. Supporting QA classifications reproduce the current analysis exactly. Privacy remains within Security; accessibility/localization remains within Usability. Section 10 uses the primary QA's existing significance, not a new delivery-priority scale. Section 9 records candidate dispositions.

The [quality-attribute learning reference](../../T%C3%A0i%20li%E1%BB%87u%20tham%20kh%E1%BA%A3o%20cho%20Architecture/14-Thu%E1%BB%99c-t%C3%ADnh-ch%E1%BA%A5t-l%C6%B0%E1%BB%A3ng.md) informs requirement/QA/constraint distinctions and architectural reasoning. [ASR_FoodDelivery.md](../../T%C3%A0i%20li%E1%BB%87u%20tham%20kh%E1%BA%A3o%20cho%20Architecture/ASR_FoodDelivery.md) was read completely for structural ideas: drivers, significant functional areas, constraints, cross-cutting concerns and exclusions. Its later implementation state, domain, technologies, tactics, thresholds and artifact metadata supply no CapsuleAI requirement or architecture decision.

## 4. Architectural Drivers

Driver IDs AD-01–AD-07 identify analysis pressures. They differ from existing three-digit **Activity Diagram** IDs such as AD-004 and from future ADR IDs. The Source / Evidence column references current CapsuleAI authorities; the linked formal ASRs define selected obligations.

| ID | Architectural Driver | Source / Evidence | Architectural Pressure |
| --- | --- | --- | --- |
| AD-01 | Protect private authority and purpose-limited information | BR-021; NFR-SEC-*; NFR-PRIV-*; DATA-RET-001–003; QA-SEC-01–03; ASR-SEC-001/002 | Account/session boundaries, permitted use, lifecycle and protected external/operating evidence. |
| AD-02 | Preserve coherent accepted state and logical actions | DATA-INT-001–004; DATA-WEAR-002–004; BRULE-WEAR/PERS; QA-REL-01/02, QA-SCA-01; ASR-REL-001 | Mutation acceptance, safe retry, concurrency, surviving evidence and time interpretation. |
| AD-03 | Preserve trustworthy shared validity and exact intelligence | BR-009/014–017/022; NFR-REL-002; DATA-OUT-001–003, DATA-ANL-004; QA-CON-01; ASR-CON-001 | Consistent domain meaning, complete same-basis assessment, identity and currentness. |
| AD-04 | Complete expensive operations within established timing | NFR-PERF-001/002; NFR-SCA-001; NFR-TEST-003; QA-PERF-01/02; ASR-PERF-001 | End-to-end coordination and resource use without weakening result integrity. |
| AD-05 | Preserve bounded dependency semantics and independent core value | SI-001–004; NFR-INT-001; NFR-AVL-001; QA-INT-01, QA-AVL-01; ASR-INT-001 | Minimal exchange, bounded environmental acquisition, failure propagation and optional enrichment. |
| AD-06 | Preserve recoverable and inspectable operation | NFR-REL-003; NFR-TEST-001–003; NFR-SUP-001/002; AI-REQ-010/011/014; QA-REL-03, QA-TEST-01, QA-SUP-01; ASR-REL-002/TEST-001 | Restoration, accepted-state survival, controlled operation and protected reproducible evidence. |
| AD-07 | Preserve understandable and accessible outcome meaning | NFR-USE/ACC; LOC-001–005; NFR-MNT-001; QA-USE-01/02, QA-MNT-01; ASR-USE-001 | Meaningful explanations/state information across contributors and labels independent of canonical semantics. |

Drivers summarize pressures; they are neither modules nor mandates for particular architectural tactics. Detailed SRS measures and rules govern trade-offs. For example, resource pressure cannot justify an incomplete Multiplier labeled exact, and dependency continuity cannot bypass unavailable authorization.

## 5. Formal Architecturally Significant Requirements

The following nine entries are selected requirement clusters. Their normative statements preserve source obligations; the rationale and pressure are architecture analysis. Scenario IDs link to the existing QA analysis, and UC/Activity/PBI/story identifiers resolve through Section 11.

### ASR-SEC-001 — Private authority and session recovery

#### Requirement

CapsuleAI shall authorize every protected personal operation for the affected account and preserve isolation through account switching, logout, renewal and recovery. Access shall require a valid, unexpired JWT Access Token and usable authorized account/session context. Renewal, rotated-token reuse, current-session logout and completed password reset shall enforce their distinct validity and revocation boundaries. Uncertain access or recovery shall not establish protected authority or falsely establish reset completion.

#### Why Architecturally Significant

Authority spans every private capability, client-visible content, images and sensitive session/recovery state. Enforcing separate-session and account-wide effects requires coherent runtime coordination; a defect can expose another user's information or permit invalid renewal.

#### Supporting Quality Attributes

Security — Critical (primary); Reliability — Critical; Interoperability — High; Usability — High.

#### Sources

- [SRS](../03-requirements/SRS.md): FR-AUTH-003/004/006–010/013/014/017/018; DATA-AUTH-005; NFR-SEC-001–003; ERR-AUTH-001–003.
- [Business Rules](../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-004–006.
- [QA analysis](quality-attribute-analysis.md): QA-SEC-01/02.
- UC-001–004; AD-004; PBI-002/004/005/041. Current stories: US-AUTH-001–005, especially US-AUTH-003 AC-02–08, US-AUTH-004 AC-01/02/04/06 and US-AUTH-005 AC-01–09/13–15.

#### Affected Functional Areas

Authentication/session/recovery and every authorized wardrobe, profile, outfit, feedback, history, intelligence and candidate interaction.

#### Validation / Evidence

Use QA-SEC-01/02: zero successful unauthorized cross-user wardrobe/image accesses in agreed scenarios; no preceding-account content or unauthorized secret output. Check 15-minute access expiry, the absolute maximum 30-day session across rotations, consumption on each successful refresh, affected-session revocation on reuse, current-session-only logout and all-account Refresh Session revocation on completed reset. Reset has a 30-minute lifetime and one successful use; successful replacement invalidates all earlier unused interactions. Compare known/unknown-email initiation responses and test failed/uncertain delivery and completion separately.

#### Architectural Pressure

Later design must account for where authority is established and checked, how session/reset changes take effect, and how protected content and interrupted actions stay associated with the correct account.

#### Not Decided Here

Token keys, storage, revocation realization and runtime boundaries remain for ADD/ADR. The existing JWT direction is a constraint; no new immediate-token-blacklist mechanism or additional authentication method is selected.

### ASR-SEC-002 — Purpose-limited data and lifecycle

#### Requirement

CapsuleAI shall constrain personal-data processing and external disclosure to necessary authorized product purposes and respect omission, correction, removal and permission controls. MVP personal images, profiles, feedback, corrections and Wear Events shall not enter AI training/improvement, public sharing, unrestricted third-party sharing or sale. Accepted removal shall stop applicable effective use immediately and enforce the established physical-deletion and measurement-retention boundaries while preserving necessary minimal history and unrelated functional information.

#### Why Architecturally Significant

Permitted use and lifecycle span collection, analysis, derived evidence, historical representations, external exchanges and operating/validation evidence. Removing one current item without resurrecting it through another representation requires ownership and lifecycle responsibilities across the system.

#### Supporting Quality Attributes

Security — Critical (primary); Reliability — Critical; Conceptual Integrity — High; Interoperability — High; Testability — High.

#### Sources

- [SRS](../03-requirements/SRS.md): NFR-PRIV-001/002; FR-PROF-007, FR-WAR-005, FR-WEAR-007, FR-MET-006; DATA-RET-001–003; SI-002/003, COM-003; Section 12.1.
- [Business Rules](../03-requirements/business-rules.md): BRULE-AUTH-007, BRULE-GAR-009, BRULE-WEAR-005, BRULE-HIST-002/003.
- [QA analysis](quality-attribute-analysis.md): QA-SEC-03.
- UC-005/010/017/023; AD-017; PBI-002/009/016/023/034/041. Current recovery disclosure example: US-AUTH-005 AC-03; no current story refines the later removal PBIs.

#### Affected Functional Areas

Private wardrobe/profile/history, AI assistance, personalization, removal, measurement and external interactions.

#### Validation / Evidence

Apply QA-SEC-03 and SRS Section 12.1. Removed optional context stops future personalization use; accepted garment/event removal immediately excludes applicable current/effective inputs. Physically delete applicable removed event data and original removed-garment images/nonessential personal data within 30 days. Delete or aggregate/de-identify user-linked measurement within 90 days, leaving older measurement no longer user-linked. Inspect purpose-minimal exchanges and absence of MVP personal data in model training. Ranking expiry alone must not erase necessary unremoved history or functional feedback.

#### Architectural Pressure

Later design must reason about necessary copies and derived consumers, the difference between effective withdrawal and physical deletion, preserved minimal historical meaning, and inspectable retention enforcement.

#### Not Decided Here

Persistence, deletion scheduling, storage providers and evidence mechanisms remain open. No account deletion/export feature, legal certification or new training-consent flow is created.

### ASR-REL-001 — Coherent accepted state and logical retry

#### Requirement

CapsuleAI shall preserve accepted user-confirmed state through cancellation, failure, retry and concurrent use. One logical mutation shall not be duplicated by repeated delivery or uncertain communication; separate explicit Wear intentions shall remain separate history events, including the same outfit/day. Accepted garment, feedback and Wear changes shall update affected current assessments or effective history/utilization/recency/personalization without altering unrelated records. Original event-local grouping and absolute elapsed-time aging shall retain their distinct meanings.

#### Why Architecturally Significant

Mutation acceptance connects authoritative information with multiple derived consumers. Logical-action identity, historical event identity and daily preference grouping differ, requiring coordinated data interpretation and runtime behavior. Incorrect handling would corrupt both personal history and future advice.

#### Supporting Quality Attributes

Reliability — Critical (primary); Security — Critical; Conceptual Integrity — High; Scalability — Medium; Testability — High.

#### Sources

- [SRS](../03-requirements/SRS.md): DATA-INT-001–004, DATA-WEAR-002–004; FR-WEAR-003/004/006–010/012; FR-PERS-012/013; COM-002, ERR-NET-001; NFR-SCA-002.
- [Business Rules](../03-requirements/business-rules.md): BRULE-GAR-001/009, BRULE-WEAR-002–006, BRULE-PERS-003/005/006; Section 15, B11–B14/B19/B20.
- [QA analysis](quality-attribute-analysis.md): QA-REL-01/02, QA-SCA-01.
- UC-007/009/010/013–018; AD-007/014/016/017; PBI-006/008/016/020–025/038/039. Current manual-save evidence: US-GAR-001 AC-09–12.

#### Affected Functional Areas

Garment confirmation/maintenance, feedback, Wear reporting/repair/withdrawal, history/utilization, personalization and dependent assessments.

#### Validation / Evidence

Use QA-REL-01/02 and rule fixtures to distinguish one retried action from a new intentional event. Confirm no automated overwrite of accepted values. Check the same exact outfit/original-local-day cap of one +2 Wear increment before prescribed decay, latest surviving accepted absolute anchor, original-day/non-future correction and survivor effects. Retain separate history and feedback beyond ranking expiry. Exercise known failure, cancellation and uncertainty without false completion or absence. QA-SCA-01 adds integrity at 20 active authenticated users, separately from timing; no cross-user leakage, corrupted accepted state or duplicate logical action caused solely by concurrency.

#### Architectural Pressure

Later design must reason about acceptance boundaries, coordinated consumer updates, logical identity, time interpretation and outcome review after lost responses.

#### Not Decided Here

Transaction realization, persistence, deduplication mechanisms and communication style remain open. This requirement selects no distributed event architecture or user/outfit/date deduplication key.

### ASR-CON-001 — Consistent validity and exact wardrobe intelligence

#### Requirement

CapsuleAI shall preserve consistent confirmed authority, applicable readiness, hard-validity and constituent-identity-set meanings across daily recommendations, Shuffle, feedback/Wear targets, Coverage/Gaps and hypothetical candidate evaluation. Hard validity shall precede soft personalization. Coverage shall retain its contextual, positive-priority-weighted meaning and sufficient-data gate. Wardrobe Multiplier shall count incremental unique valid outfits from complete current and expanded sets under the same context/rule basis. Relevant input/rule changes shall require reevaluation or explicit outdated status.

#### Why Architecturally Significant

The product pillars share domain invariants and assessment evidence. Divergent eligibility, uniqueness or evaluation bases would produce conflicting advice and unreliable exact counts. These obligations affect responsibility allocation, assessment coordination, information ownership and change propagation.

#### Supporting Quality Attributes

Conceptual Integrity — High (primary); Reliability — Critical; Testability — High; Usability — High; Maintainability — Medium; Flexibility — Medium.

#### Sources

- [SRS](../03-requirements/SRS.md): FR-GAR-003/011; FR-OUT-001/003/005/009/013–017; FR-ANL-011–015; FR-GAP-001–005; FR-MULT-002–008; DATA-OUT-001–003, DATA-ANL-004; NFR-REL-002; Sections 3.7.1/3.14.1/3.16.1.
- [Business Rules](../03-requirements/business-rules.md): BRULE-GAR-003, BRULE-OUT-008–010, BRULE-COV-001–006, BRULE-GAP-001/002, BRULE-MULT-001–009.
- [QA analysis](quality-attribute-analysis.md): QA-CON-01; related authority/effects QA-REL-01/02 and semantic-change QA-MNT-01.
- UC-011/012/014/019–022; AD-011/012/019/020/021/022; PBI-017–019/026–031/039. Near-term readiness examples: US-GAR-001 AC-06–09, US-WAR-002 AC-03–05.

#### Affected Functional Areas

Garment readiness, daily outfits/Shuffle, exact-combination behavioral targets, Coverage/Gaps, candidate utility and hypothetical previews.

#### Validation / Evidence

Use QA-CON-01 and Business Rules Section 15 fixtures. The same garment set in another order adds no outfit; soft preference cannot rescue invalid combinations. Shuffle retains other constituents/context. Existing weighted Coverage fixture B09 gives 50, while insufficient data yields no false 0% or gap. The complete 41-current/58-expanded fixture gives +17; identical completed sets give Evaluated Zero/+0. Test Exact, Evaluated Zero, Incomplete, Unavailable and Outdated separately, and verify same-basis candidate-bearing previews and no ownership/Wear side effect. These numbers are fixtures, not new service targets.

#### Architectural Pressure

Later design must reason about shared domain meaning, full evaluation versus displayed subsets, evidence completeness, changed-basis detection and synchronized explanations.

#### Not Decided Here

No module map, enumeration/ranking architecture, cache or precomputation strategy is selected. No arbitrary Multiplier TTL, latency target or approximation permission is introduced.

### ASR-PERF-001 — Complete-operation latency

#### Requirement

CapsuleAI shall meet the existing latency requirements for complete reviewable garment processing and the first complete daily-outfit result under the established reference conditions. It shall preserve required outputs, eligible-input completeness, confirmed authority and hard-validity semantics while meeting those boundaries, with reproducible timing evidence.

#### Why Architecturally Significant

Image preparation/inference and outfit evaluation can consume significant processing resources and require coordinated work before a usable result exists. The required completion boundaries affect runtime collaboration, resource allocation and later performance decisions across contributors.

#### Supporting Quality Attributes

Performance — High (primary); Reliability — Critical; Conceptual Integrity — High; Testability — High; Scalability — Medium.

#### Sources

- [SRS](../03-requirements/SRS.md): NFR-PERF-001/002, NFR-SCA-001, NFR-TEST-003, FR-MET-004; Sections 2.3/3.4.1/12.3.
- [Business Rules](../03-requirements/business-rules.md): BRULE-GAR-001/003, BRULE-OUT-008/009 constrain authority, readiness and complete valid output.
- [QA analysis](quality-attribute-analysis.md): QA-PERF-01/02.
- UC-007/011; AD-007/011; PBI-013/017/035. No current story implements either measured assisted-entry or daily-outfit path.

#### Affected Functional Areas

AI-assisted garment processing through preview/proposals; daily outfit evaluation through the first complete result.

#### Validation / Evidence

QA-PERF-01 requires p95 ≤5 seconds from accepted analysis after image transfer to both usable processed preview and reviewable proposals. QA-PERF-02 requires p95 <3 seconds from accepted request with required context available to the first complete daily result, with exactly 100 confirmed garments. Apply 5 warm-ups followed by ≥100 measured executions per operation; retain legitimate slow runs and record device/network/environment. Use SRS Section 12.3 reference hardware/network and Section 3.4.1 image conditions. These are acceptance conditions, not universal device guarantees. Functional integrity at 20 active users is separate.

#### Architectural Pressure

Later design must assess total operation cost, contention and completion across cooperating responsibilities while preserving result meaning. Slow or partial work cannot be relabeled complete to pass a benchmark.

#### Not Decided Here

No cache, precomputation, queue, parallelization, AI communication style or algorithm architecture is selected. No general API, Multiplier, larger-wardrobe or p95-at-20-users threshold is added.

### ASR-INT-001 — Bounded dependency outcomes and core continuity

#### Requirement

CapsuleAI shall preserve the logical meaning of analysis, weather, email-recovery and external-shopping outcomes, using purpose-minimal exchanges. It shall retain manual entry and independent authorized core paths when optional dependencies fail, subject to usable access/network and sufficient valid inputs. Weather shall follow its established freshness and bounded-wait policy; commercial information shall retain credible source/time qualification. Dependency failure shall not fabricate success, empty inventory, zero utility, ownership, Wear or a transaction outcome.

#### Why Architecturally Significant

Dependencies introduce different authority, completion and failure boundaries. Weather affects validity context; email delivery enables recovery without completing it; image assistance cannot establish garment truth; shopping navigation cannot establish acquisition. Those differences influence runtime integration and failure propagation across the value loop.

#### Supporting Quality Attributes

Interoperability — High (primary); Reliability — Critical; Security — Critical; Availability — Medium; Usability — High.

#### Sources

- [SRS](../03-requirements/SRS.md): SI-001–004, HW-003, COM-003; FR-WEATHER-003–005, FR-SHOP-003/006–008, FR-MET-007; NFR-INT-001, NFR-AVL-001, NFR-REL-001; ERR-DEP-001; Section 12.2.
- [Business Rules](../03-requirements/business-rules.md): BRULE-PROF-003, BRULE-OUT-006, BRULE-SHOP-001–003.
- [QA analysis](quality-attribute-analysis.md): QA-INT-01, QA-AVL-01; analysis continuity QA-REL-01 and recovery QA-SEC-02.
- UC-004/006/007/011/019/021–023; AD-004/006/007/021/022; PBI-005/009/011/013/032/033. Current delivery-failure evidence: US-AUTH-005 AC-03/13; manual alternative US-GAR-001 AC-02.

#### Affected Functional Areas

Environmental context and its assessment consumers; assisted entry; recovery email; optional commercial guidance/navigation and measurement.

#### Validation / Evidence

Use QA-INT-01/QA-AVL-01 with isolated dependency failures. Weather age ≤30 minutes is current; older evidence needs refresh. An external weather request delays assessment at most 2 seconds; absent usable current evidence then means disclosed unavailable environment, skipping only unavailable environmental filtering and retaining remaining validity. Commercial price/availability is current up to 24 hours inclusive; older information is refreshed, explicitly stale/last checked or omitted. Check purpose-minimal exchanges, manual continuity, failed email without reset completion, retained valid utility after destination failure and unchanged core-action outcomes when measurement fails.

#### Architectural Pressure

Later design must distinguish acquisition from consumption, bounded waits from result freshness, and required authority/evidence from optional enrichment. UC-006 acquires environmental context; consumers do not require a new external request for every assessment.

#### Not Decided Here

Providers, internal/external analysis placement, communication style, timeout realization and failure-isolation mechanisms remain open. No retailer contract, background-tracking schedule or general offline guarantee is established.

### ASR-REL-002 — Recoverable core operation

#### Requirement

In the agreed validation environment, CapsuleAI shall restore core service within 5 minutes after a recoverable application/service restart, retaining previously accepted authoritative user information and applicable optional-dependency fallbacks. Operating and recovery behavior shall remain inspectable through protected evidence and truthful outcomes, with usable authorization restored before protected use.

#### Why Architecturally Significant

Restoration requires coordinated operating responsibilities and preservation of accepted state across interruption. The acceptance boundary influences later runtime/deployment and recovery reasoning even though the current sources select no topology or recovery tactic.

#### Supporting Quality Attributes

Reliability — Critical (primary); Availability — Medium; Supportability — Medium; Manageability — Medium; Testability — High; Security — Critical.

#### Sources

- [SRS](../03-requirements/SRS.md): NFR-REL-003, NFR-AVL-001, NFR-SUP-001/002; ERR-DEP-001, ERR-NET-001; Sections 8.6/12.3.
- [Business Rules](../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-GAR-001, BRULE-WEAR-002 constrain restored authority/state.
- [QA analysis](quality-attribute-analysis.md): QA-REL-03; protected diagnostic context QA-SUP-01.
- UC-007/008/011 failure/minimal postconditions; AD-007/011; PBI-001/038/040; [DoD](../06-scrum/definition-of-done.md) Section 5 operational trigger. Current retrieval/access recovery examples: US-WAR-001 AC-05/09.

#### Affected Functional Areas

Core authorized access, wardrobe and advice operation; retained accepted information and dependency fallback.

#### Validation / Evidence

Execute QA-REL-03 with controlled accepted state and restart-to-restoration timing ≤5 minutes. Check preserved authoritative information, usable protected operations and applicable fallback after restoration. QA-SUP-01 supplies protected inspection and honest failed/unconfirmed-state checks. PBI-040/DoD require repeatable deployment, protected configuration and applicable operating evidence; no executed recovery result is claimed here.

#### Architectural Pressure

Later design must identify what constitutes core restoration, how accepted state survives the recoverable interruption and how readiness, dependency failures and recovery results can be verified.

#### Not Decided Here

Deployment topology, monitoring tools, backup/recovery tactics and operating platform remain open. The validation restart target adds no production uptime SLA, disaster-recovery objective or RPO.

### ASR-USE-001 — Truthful, accessible and localized outcomes

#### Requirement

CapsuleAI shall provide the Vietnamese mobile experience with understandable, accessible actions, explanations, state distinctions and recovery choices. Connected capabilities shall supply truthful meaning for draft/accepted authority, Saveable/readiness, confidence/provenance, current/historical/hypothetical information and complete/insufficient/unavailable/outdated results. Display-label changes and later language additions shall preserve canonical semantics and existing confirmed information.

#### Why Architecturally Significant

State meaning originates across product capabilities and must reach mobile presentation without losing evidence or limitations. Accessibility and explanation therefore influence information responsibilities and interactions across contributors, beyond visual layout alone. Unclear outcomes can cause unsafe retries, unsupported decisions or loss of user control.

#### Supporting Quality Attributes

Usability — High (primary); Conceptual Integrity — High; Reliability — Critical; Maintainability — Medium; Flexibility — Medium; Supportability — Medium.

#### Sources

- [SRS](../03-requirements/SRS.md): NFR-USE-001/002, NFR-ACC-001/002, NFR-MNT-001, NFR-SUP-001; LOC-001–005; FR-OUT-010/011; Sections 8.2/12.4.
- [Business Rules](../03-requirements/business-rules.md): BRULE-GAR-007/008, BRULE-HIST-001, BRULE-SHOP-001 and BRULE-MULT-004–009.
- [QA analysis](quality-attribute-analysis.md): QA-USE-01/02, QA-MNT-01; related failure meaning QA-SUP-01.
- UC-007/008/011/015/019–023; AD-007/011/019/021/022; PBI-003/018/036/037. Current examples: US-GAR-001 AC-14 and US-WAR-002 AC-07/12; accessibility AC in all eight stories.

#### Affected Functional Areas

Mobile actions and explanations across entry, confirmation, recommendations, Wear/history, Coverage/Gaps and candidate advice.

#### Validation / Evidence

Reuse QA-USE-01/02: at least 5 users total across personas where practical; target ≥80% unaided completion for each evaluated core task, zero unresolved Critical usability blockers for acceptance and average SUS ≥68 as an aid. Validate applicable core journeys with TalkBack/VoiceOver, text scaling up to 200%, meaningful names/roles/states and approximately 48 dp Android/44 pt iOS primary-control minimum targets. QA-MNT-01 checks unchanged confirmed values and domain outcomes after intended label changes. Missing evidence must remain distinguishable from a completed empty/zero result.

#### Architectural Pressure

Later design must preserve sufficient explanation/state information across boundaries, accessible outcome semantics and separation of canonical meaning from translated labels.

#### Not Decided Here

UI framework, internal response shapes and localization realization remain open. No second MVP language or certification is introduced; integrated validation follows existing SRS/DoD conditions.

### ASR-TEST-001 — Inspectable domain, AI and operating evidence

#### Requirement

CapsuleAI shall make authorized evaluated inputs/context, limitations, results, mutation outcomes and specified timing evidence inspectable so domain correctness, AI acceptance and recovery can be demonstrated. Recognition validation shall use the established locked qualified-image benchmark with separate category, dominant-color and preview evidence. Validation/diagnostic evidence shall protect secrets and private information; optional measurement failure shall preserve the core action's actual outcome.

#### Why Architecturally Significant

Important behavior depends on information not established by a visible count or confidence label alone: full evaluation basis, accepted versus attempted actions, benchmark isolation and complete timing boundaries. Responsibility allocation and runtime information must enable inspection without introducing unauthorized data access.

#### Supporting Quality Attributes

Testability — High (primary); Reliability — Critical; Security — Critical; Conceptual Integrity — High; Performance — High; Supportability — Medium; Manageability — Medium.

#### Sources

- [SRS](../03-requirements/SRS.md): NFR-TEST-001–003, NFR-SUP-002, NFR-SEC-003; FR-MET-004–007; AI-REQ-010/011/014; Sections 3.4.1/8.1.1/12.
- [Business Rules](../03-requirements/business-rules.md): BRULE-GAR-007/008, BRULE-MULT-004; Section 15 controlled fixtures.
- [QA analysis](quality-attribute-analysis.md): QA-TEST-01, QA-SUP-01; related QA-CON-01, QA-PERF-01/02 and QA-REL-03.
- UC-007/008/011/019/022; AD-007/011/019/022; PBI-001/014/035/038–041; [DoD](../06-scrum/definition-of-done.md) Sections 4–6. Current acceptance-distinction examples: US-GAR-001 AC-11/12, US-WAR-001 AC-03/05/09.

#### Affected Functional Areas

Domain assessments, garment-assistance acceptance, mutation/recovery validation, performance measurement and protected operating evidence.

#### Validation / Evidence

QA-TEST-01 requires ≥200 qualified locked images with ≥50 in each primary category, excluded from training/tuning after lock. Category accuracy is ≥90% overall and ≥85% within each category; dominant-color accuracy is ≥90% overall, reported separately. Follow the source labeling protocol: two independent annotators should label, with disagreements adjudicated. Preview usability is criterion-based; no percentage or universal rich-attribute accuracy is added. Inspect QA-CON-01 fixtures and existing timing/restart evidence. QA-SUP-01 distinguishes attempted, accepted, failed and uncertain outcomes while protecting secrets and avoiding raw private images/sensitive profiles in ordinary engagement diagnostics.

#### Architectural Pressure

Later design must expose enough authorized evidence to verify claims, reproduce validation and diagnose failure while keeping validation, functional history and ordinary measurement purposes distinct.

#### Not Decided Here

No public test interface, analytics product, logging/monitoring vendor, CI platform or test-harness design is selected. Detailed Test Strategy and executable evidence follow later; this document claims no software test pass.
## 6. Architecturally Significant Functional Areas

These are source-derived behavior groups, not proposed services, bounded contexts, modules or deployables. Shared authorization, lifecycle and evidence ASRs apply across them; related UC IDs do not imply UML include relationships.

| Functional Area | Significant Behavior / Why Architecture Cares | Related Use Cases / Activities | Main Formal ASRs |
| --- | --- | --- | --- |
| Authentication / Session / Recovery | Private authority, separate sessions, rotation/reuse, reset replacement/consumption and completion-based revocation coordinate access and sensitive state. | UC-001–004; AD-004 | ASR-SEC-001, ASR-REL-001, ASR-INT-001 |
| Garment Digitization and User Confirmation | Image/manual alternatives, explicit canonical confirmation, confidence/provenance, Saveable versus readiness, later automated disagreement and dependent changes constrain authority and completion. Routine browsing/editing matters here only where authority, ownership or uncertain outcomes are affected. | UC-007–010; AD-007 | ASR-REL-001, ASR-CON-001, ASR-PERF-001, ASR-SEC-002 |
| Outfit Recommendation and Shuffle | Current owned inputs, validity before ranking, distinct identity sets, actual available choice, fixed-slot/context replacement and changed-basis detection must stay coherent. | UC-011/012; AD-011/012 | ASR-CON-001, ASR-PERF-001, ASR-INT-001, ASR-USE-001 |
| Wear Events and Personalization | Explicit intentions versus retries, individual correction/removal, original local meaning, absolute aging, survivor effects and distinct history/ranking evidence create substantial state coordination. | UC-013–018; AD-014/016/017 | ASR-REL-001, ASR-SEC-002, ASR-CON-001 |
| Wardrobe Coverage / Gaps | Positive-priority contextual assessment, sufficient-data gate, unique valid need evidence, weighted score and capability-first bottlenecks require shared domain meaning and honest currentness. | UC-019/020; AD-019/020 | ASR-CON-001, ASR-USE-001, ASR-TEST-001 |
| Candidate Evaluation / Wardrobe Multiplier | Complete same-basis set comparison, incremental utility, exact/non-exact states and candidate-bearing hypothetical previews must preserve ownership and assessment evidence. | UC-021/022; AD-021/022 | ASR-CON-001, ASR-INT-001, ASR-TEST-001 |
| External Integrations | Selected-image analysis, optional device/location/weather, recovery email and optional shopping handoff have different completion, disclosure and fallback boundaries. Analysis is a logical capability, not a mandated external actor/service. | UC-004/006/007/023; AD-004/006/007 | ASR-INT-001, ASR-SEC-001/002 |
| Privacy / Retention / Measurement and Operation | Immediate effective withdrawal, bounded physical deletion, necessary minimal history and separate engagement retention must be provable alongside outcome/timing evidence and recoverable operation. This does not introduce an analytics/admin user goal. | UC-005/010/017/023; relevant failure paths across UCs; AD-017 | ASR-SEC-002, ASR-REL-002, ASR-TEST-001 |

## 7. Architectural Constraints

An **ASR** is a selected requirement cluster with substantial architectural influence. A **Quality Attribute** classifies a testable property and its significance. A **Constraint** is an already imposed product/design boundary or mandated policy limiting later choices. A policy may support an ASR without becoming a separate ASR or selecting its realization. Detailed domain rules remain behavioral authority; the table summarizes imposed boundaries rather than a data/implementation design.

| Established Constraint / Policy | Current Authoritative Source | Boundary Retained for Later Design |
| --- | --- | --- |
| Connected Vietnam-first mobile MVP | BR-023; PRD Sections 2/26/27; SRS Sections 1.2/2.3 | Android 10+ and iOS 15+; documented permission alternatives. No consumer web/desktop or guaranteed offline scope. Reference hardware/network defines validation, not user purchase minima. |
| Vietnamese initial UI; English documentation | PRD OPQ-002; LOC-001–005; NFR-MNT-001 | Canonical category/subtype, style/occasion, confidence and provenance meanings remain independent of display labels; later language readiness adds no second MVP UI language. |
| Email/password with JWT access/refresh direction | PRD OPQ-008; SRS Sections 2.4/3.1.1; FR-AUTH-009/010/013/014 | 15-minute access, maximum 30-day session, consumed refresh rotation and affected-session reuse response. Storage, keys and revocation mechanisms remain undecided. |
| Existing account/recovery policy | FR-AUTH-004/007/015–018; DATA-AUTH-002/005; BRULE-AUTH-002–006 | Separate current-session logout versus completed-reset all-Refresh-Session revocation; 30-minute single-success-use reset with replacement; normalized unique email and exact 12–128-character password meaning. No social, OTP or guest MVP access. |
| Bounded garment and outfit domain | SRS Sections 3.5.1/3.7.1/4.10; BRULE-GAR/OUT | TOP, BOTTOM, OUTERWEAR and FOOTWEAR; approved vocabularies; confirmed category/color Saveable minimum with imageless manual creation; evaluation-specific readiness; hard validity before soft preference. Outfit composition remains one TOP/BOTTOM/FOOTWEAR plus zero or one OUTERWEAR. |
| Supported garment-image input | SRS Section 3.4.1; FR-AI-001/002/004; UC-007 | JPEG/JPG, PNG and HEIC/HEIF; maximum 15 MB; shortest dimension ≥512 pixels. Preview usability and recognition accuracy differ; no mandatory white background or image-saving gate. |
| Private, necessary-purpose processing | NFR-PRIV-001/002; BRULE-AUTH-007; SI-002/003, COM-003 | No MVP personal-data AI training/improvement, public wardrobes, user-to-user sharing, unrestricted disclosure or sale. Future training needs separate explicit opt-in and approved product/privacy change. |
| Existing lifecycle boundaries | FR-WAR-005, FR-WEAR-007, FR-MET-006; DATA-RET-001–003; SRS Section 12.1 | Immediate effective garment/event exclusion; 30-day applicable physical deletion; separate maximum 90-day user-linked measurement before deletion/aggregation/de-identification; necessary minimal history distinct from ranking expiry. |
| Existing environmental/commercial evidence policies | FR-WEATHER-003–005; FR-SHOP-001/003; SRS Section 12.2 | Weather current at age ≤30 minutes and external-request assessment delay ≤2 seconds; credible candidate evidence; commercial source/time and inclusive 24-hour price/availability currentness. No mandated provider/retailer contract. |
| Strategic Shopping stays advisory | BR-024; FR-SHOP-005–008; UC-023 | Optional explicit external navigation; no native cart/checkout/payment/orders/fulfillment, verified purchase or automatic garment/Wear creation. |
| Current MVP exclusions and preparation sequence | BRD Section 16; PRD scope/decision baseline; SRS Section 1.2; Workflow Sections 39–43/46; DD-001–006 | No social/public wardrobe or AR/3D scope. ADD/views/ADRs/contracts/Test Strategy and engineering preparation follow their workflow stages; no Sprint commitment is inferred from stories or this ASR draft. |

Session/lifecycle/evidence limits above are existing policies, not newly chosen tactics. Accepted event identity/time, contextual Coverage and complete same-basis Multiplier semantics are preserved through ASR-REL-001/CON-001 and their source rules.

**Historical requirement-source boundary:** the following paragraph preserves the preselection analysis. Current solution choices are governed separately by [Selected Architecture Decisions](selected-architecture-decisions.md) and [ADD](ADD.md); they do not turn those technologies into new product requirements or ASRs.

React Native, Java/Spring Boot, Python AI services, MongoDB/PostgreSQL, AWS/S3 and Redis are **not established current architectural constraints**. Historical or reference mentions do not mandate them. The Implementation Summary's older “option B” and projected stack remain historical under SRS Section 1.5 and the normalized PRD/QA source interpretation. JWT differs because the current PRD/SRS explicitly retain that direction. No current implementation or ADD/ADR establishes further style, database, cache, messaging, provider or deployment choices.

## 8. Cross-Cutting Architectural Concerns

These are requirement-level implications of the formal ASRs, not an implementation checklist.

| Concern | Meaning Across CapsuleAI | Relevant ASRs |
| --- | --- | --- |
| Authorization and account isolation | Personal operations, images and displayed content remain associated with usable authorized access for the correct account through switching/recovery. | ASR-SEC-001 |
| Authoritative user-confirmed state | Proposals, drafts and accepted corrections differ; later automation cannot silently supersede accepted values. | ASR-REL-001, ASR-CON-001 |
| Safe retry and uncertain outcomes | Known failure, pending and uncertain acceptance differ. Outcome review/retry preserves one logical action while allowing separate deliberate Wear reports. | ASR-REL-001, ASR-SEC-001, ASR-INT-001 |
| Data lifecycle and retention | Effective withdrawal, physical deletion, minimal functional history and user-linked measurement have separate responsibilities and deadlines. | ASR-SEC-002, ASR-REL-001 |
| Time semantics | Original event-local date governs history/grouping; absolute elapsed periods govern behavioral age/recency; policy-driven expiry and evidence timestamps retain their own meanings. | ASR-REL-001, ASR-SEC-001, ASR-INT-001 |
| Shared validity, identity and assessment basis | Daily advice, Shuffle and intelligence preserve agreed eligibility/identity. Complete same-basis evaluation and relevant change detection govern current/exact claims. | ASR-CON-001, ASR-PERF-001 |
| Dependency failure semantics | Missing optional enrichment does not destroy accepted information; necessary access/evidence cannot be bypassed. Reduced context preserves remaining hard validity. | ASR-INT-001, ASR-REL-002 |
| Inspectable validation and operation | Inputs, actual acceptance, result basis, timing and restoration remain verifiable through authorized privacy-safe evidence. | ASR-TEST-001, ASR-REL-002 |
| Localization and accessible state meaning | Labels, screen-reader semantics and explanations preserve canonical meaning and distinguish limitations beyond visual cues. | ASR-USE-001, ASR-CON-001 |

The QA analysis's Section 20 trade-offs remain relevant. Performance cannot weaken exactness or confirmed authority; recoverability and usability cannot relax authorization; support/validation evidence cannot exceed necessary-purpose privacy boundaries.

## 9. Non-ASR / Deferred Concerns

The QA analysis's preliminary candidate list was re-evaluated rather than mechanically converted. This disposition table explains promotion, splitting and merging; merged concerns remain binding within their selected ASRs.

| Preliminary Concern | Disposition / Reason |
| --- | --- |
| Private authority, session/recovery and lifecycle | Split into ASR-SEC-001 and ASR-SEC-002: access validity/revocation and permitted data use/deletion impose distinct pressures. |
| Accepted-state coherence, logical retries and behavioral history | Selected as ASR-REL-001, including concrete concurrent integrity. |
| Consistent validity, identity and current exact intelligence | Selected as ASR-CON-001 across capabilities sharing the same domain/evaluation meaning. |
| Complete-operation latency and evidence | Selected as ASR-PERF-001 for the two existing timing boundaries; reproducible evidence also informs ASR-TEST-001. |
| Dependency semantics and bounded continuation | Selected as ASR-INT-001; Availability supports the same integration/fallback pressure rather than a standalone uptime ASR. |
| Recoverable core operation | Selected as ASR-REL-002 using the existing recoverable-restart acceptance condition. |
| Explainable, accessible mobile outcomes | Selected as ASR-USE-001 for cross-capability outcome/explanation semantics. |
| Inspectable domain and AI acceptance | Selected as ASR-TEST-001; it enables evidence for other ASRs without duplicating all scenarios. |
| Concurrent integrity and controlled operation | No separate scale/management ASR: 20-user integrity is incorporated in ASR-REL-001/SEC-001; operating/recovery evidence in ASR-REL-002/TEST-001. No larger-scale source target exists. |
| Semantic maintenance and bounded evolution | No separate change-cost/flexibility ASR: canonical interpretation and changed-basis correctness sit in ASR-CON-001; labels/localization in ASR-USE-001. No modification-time/SLA target exists. |
| Independent cross-product reuse | Not selected: Reusability is Low and no external reuse consumer or reusable-platform acceptance requirement exists. Shared CapsuleAI semantics remain required. |

Routine display-name/profile editing, collection/detail presentation and search/filter mechanics do not independently select architecture; their authority, privacy, uncertainty, state meaning and accessibility obligations remain covered. User importance alone does not justify another ASR.

Speculative markets/languages, new garment categories, personal-data training, retail/advertising/reward platforms and larger-scale throughput remain outside current ASR scope unless approved sources change. No additional latency, production uptime, disaster recovery, resolution-time or reuse target is inferred. Possible databases, caches, brokers, architectural styles and topology are future design options, not deferred requirements. Architecture review may later choose justified mechanisms without inventing product obligations.

## 10. ASR Summary Matrix

Significance below reproduces the **primary QA** classification. Supporting attributes retain their own current classifications in Section 5; for example, a High Conceptual Integrity ASR may also support Critical Reliability. This is not backlog order or implementation priority.

| ASR | Primary QA | Supporting QAs | Significance | Main Functional Areas | Key Evidence |
| --- | --- | --- | --- | --- | --- |
| ASR-SEC-001 | Security | Reliability, Interoperability, Usability | Critical | Private authority, sessions/recovery, protected operations | QA-SEC-01/02; zero unauthorized accesses; session/reset boundary tests |
| ASR-SEC-002 | Security | Reliability, Conceptual Integrity, Interoperability, Testability | Critical | Private data use/lifecycle, minimal exchange, measurement | QA-SEC-03; immediate exclusion, 30-day deletion, 90-day measurement |
| ASR-REL-001 | Reliability | Security, Conceptual Integrity, Scalability, Testability | Critical | Confirmation, mutations, Wear/history/personalization | QA-REL-01/02, QA-SCA-01; retry versus intention, survivor/time fixtures, 20-user integrity |
| ASR-CON-001 | Conceptual Integrity | Reliability, Testability, Usability, Maintainability, Flexibility | High | Validity/Shuffle, Coverage/Gaps, exact candidate utility | QA-CON-01; identity/basis/state fixtures; existing 50 Coverage and +17 Multiplier examples |
| ASR-PERF-001 | Performance | Reliability, Conceptual Integrity, Testability, Scalability | High | Reviewable processing and complete daily result | QA-PERF-01/02; p95 ≤5 s and <3 s under SRS conditions |
| ASR-INT-001 | Interoperability | Reliability, Security, Availability, Usability | High | Analysis, weather, email, shopping and optional measurement | QA-INT-01, QA-AVL-01; 30-minute/2-second weather, qualified commercial evidence, isolated failure |
| ASR-REL-002 | Reliability | Availability, Supportability, Manageability, Testability, Security | Critical | Recoverable core operation | QA-REL-03, QA-SUP-01; ≤5-minute restart restoration and protected operating evidence |
| ASR-USE-001 | Usability | Conceptual Integrity, Reliability, Maintainability, Flexibility, Supportability | High | Accessible Vietnamese actions, explanations and state meaning | QA-USE-01/02, QA-MNT-01; SRS Section 12.4 and label-change regressions |
| ASR-TEST-001 | Testability | Reliability, Security, Conceptual Integrity, Performance, Supportability, Manageability | High | Domain/AI acceptance, timing and operating inspection | QA-TEST-01, QA-SUP-01; locked benchmark, authorized fixture/results and protected evidence |

## 11. Traceability

Section 5 is the detailed source-to-ASR evidence. The representative mappings below support navigation without duplicating the SRS catalogue. Ranges/slash groups refer to existing identifiers in the named family. ASR/driver IDs are analysis identities; upstream IDs remain unchanged.

| Current Requirement / Rule / QA / Interaction Anchors | Formal ASR |
| --- | --- |
| FR-AUTH-004/007–010/013/014/017/018; DATA-AUTH-005; NFR-SEC-001–003; BRULE-AUTH-001/004–006; QA-SEC-01/02; UC-002–004, AD-004 | ASR-SEC-001 |
| NFR-PRIV-001/002; FR-WAR-005, FR-WEAR-007, FR-MET-006; DATA-RET-001–003; BRULE-AUTH-007, BRULE-HIST-002/003; QA-SEC-03; UC-005/010/017/023 | ASR-SEC-002 |
| DATA-INT-001–004, DATA-WEAR-002–004; FR-WEAR-010, FR-PERS-012/013; NFR-SCA-002; BRULE-WEAR-002–006, BRULE-PERS-003/005/006; QA-REL-01/02, QA-SCA-01; UC-007/014/016/017 | ASR-REL-001 |
| FR-OUT-003/009/014–017, FR-ANL-011–015, FR-MULT-002–008; DATA-OUT-001–003, DATA-ANL-004; BRULE-OUT-008–010, BRULE-COV-001–006, BRULE-MULT-001–009; QA-CON-01; UC-011/012/019/020/022 | ASR-CON-001 |
| NFR-PERF-001/002, NFR-SCA-001, NFR-TEST-003; QA-PERF-01/02; UC-007/011, AD-007/011; PBI-035 | ASR-PERF-001 |
| SI-001–004, FR-WEATHER-003–005, FR-SHOP-003/006–008, FR-MET-007; BRULE-PROF-003, BRULE-OUT-006, BRULE-SHOP-001–003; QA-INT-01, QA-AVL-01; UC-004/006/007/023 | ASR-INT-001 |
| NFR-REL-003, NFR-AVL-001, NFR-SUP-001/002; QA-REL-03, QA-SUP-01; core UC failure postconditions; PBI-038/040; DoD operational trigger | ASR-REL-002 |
| NFR-USE-001/002, NFR-ACC-001/002, NFR-MNT-001, LOC-001–005; BRULE-GAR-007/008, BRULE-HIST-001; QA-USE-01/02, QA-MNT-01; UC-007/008/011/019/022 | ASR-USE-001 |
| NFR-TEST-001–003, NFR-SUP-002, AI-REQ-010/011/014, FR-MET-004–007; BRULE-GAR-007, BRULE-MULT-004; QA-TEST-01, QA-SUP-01; UC-007/019/022; PBI-014/039 | ASR-TEST-001 |

### 11.1 Source Navigation and Authority

| Source Read | Role |
| --- | --- |
| [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 16–19/39–43/46 | ASR purpose/ownership, source boundaries, evolution and downstream sequence. |
| [Quality Attribute Analysis v0.1](quality-attribute-analysis.md), especially Sections 5/20–24 | Primary direct input; all 14 classifications, 17 scenario bodies, trade-offs, constraints and preliminary candidates. |
| [BRD v0.3](../01-business/BRD.md); [PRD v0.2](../02-product/PRD.md) | BG-01–04, BR-001–024, CAP-01–10; all journeys/features and settled product/MVP boundaries. |
| [SRS v0.3](../03-requirements/SRS.md) | Normative requirement IDs, exact measures, data/interfaces/failures and acceptance conditions. |
| [Business Rules v0.1.2](../03-requirements/business-rules.md) | All 11 rule families; precedence, invariant meaning and controlled fixtures. |
| [Master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml); [all 23 UC specifications](../03-requirements/use-cases/) | Actors/goals and normative user interaction outcomes. |
| [All 12 Activity Diagrams](../03-requirements/activity-diagrams/) | AD-004/006/007/011/012/014/016/017/019/020/021/022 visualize the corresponding UC flows; no new behavior authority. |
| [Product Goal v0.1.1](../06-scrum/product-goal.md); [Product Backlog v0.1](../06-scrum/product-backlog.md) | Whole-MVP value and all 41 delivery/evidence routes; ordering and refinement remain in Scrum artifacts. |
| [Story index and all eight current stories](../06-scrum/stories/README.md) | US-AUTH-001–005, US-GAR-001, US-WAR-001/002 supply concrete current AC examples, not the whole ASR horizon. |
| [DoD v0.1](../06-scrum/definition-of-done.md); [Delivery Decisions v0.1](../06-scrum/delivery-decisions.md), DD-001–006 | Integrated completion evidence, change propagation, preparation order and absence of Sprint commitment. |
| [Implementation Summary](../../Initial%20files/CapsuleAI_Implementation_Summary.md); [proposal](../../Initial%20files/DA%202%20Proposal.md); official outline under Initial files; Business Analysis.docx | Historical/exploratory context only; their older technology, thresholds, quotas and suggested priorities do not override the canonical baseline. |
| Quality-attribute learning reference and ASR_FoodDelivery.md, linked in Section 3 | Methodology/structure only; no CapsuleAI requirement evidence. |

### 11.2 Source Interpretation and Issues

No blocking contradiction was found in the current baseline. **FR-MET-004 has an incomplete benchmark cross-reference**, already noted in Product Backlog Section 11 and QA analysis Section 24. ASR-PERF-001/TEST-001 use explicit SRS Sections 3.4.1/8.1.1/12.3 and stable NFR/AI requirement IDs. The editorial defect neither removes nor weakens existing acceptance measures; upstream sources remain unchanged.

**Focused Use Case Diagram references do not match the current inventory.** SRS Section 14 and the UC specifications refer to focused diagrams under `use-cases/diagrams/`, but those files are absent. The existing master Use Case Diagram, complete 23 UC specifications and 12 Activity Diagrams supply this analysis's interaction evidence. No missing diagram content is assumed, and no additional diagram artifact is created.

Older documents' “next artifact” statements record their own handoff point. Current progression follows Workflow Section 46 and DD-006: ASR → Architecture Decision Analysis → explicit human Selected Architecture Decisions → ADD/views → ADRs, followed by detailed design and delivery preparation. Earlier handoffs do not restart product/requirements work. The Food Delivery example's later architecture and different documentation responsibilities do not alter CapsuleAI's source ownership or placement of QA scenarios.

## 12. Backend Developer Takeaways

- Enforce authorization/ownership wherever private information or action is involved; preserve the distinct logout, refresh-reuse and completed-reset boundaries.
- Design later mutation and persistence interactions around accepted authority, safe logical retry and coordinated effects. Do not merge separate Wear reports just because outfit/day match.
- Keep original local grouping, absolute elapsed-time aging, minimal history and measurement retention distinct.
- Preserve hard validity and exact identity before ranking. Coverage/Multiplier claims need defensible context, completeness and changed-basis evidence; displayed recommendation subsets cannot establish exact utility.
- Preserve bounded dependency outcomes and truthful recovery. Performance measures apply to complete operations under the SRS workload, separately from concurrency and restart checks.
- Ensure later design permits protected, reproducible evidence with Mobile/AI/QA contributors. Technology, topology, contracts and data realization are still pending.

## 13. Status and Next Step

**Version 0.1.5 — Baseline Draft**, prepared for Architect/Tech Lead review with Developers, QA and Product/BA. This artifact selects seven drivers and nine traceable ASRs across the full MVP; it claims no approval, implemented satisfaction or executed benchmark. Upstream requirements, rules, Scrum artifacts and QA scenarios retain their authority.

The preparation sequence remains **Architecture Decision Analysis → Selected Architecture Decisions → ADD** (Workflow Sections 17.2–17.3 and 46, Steps 15–17). [Selected Architecture Decisions](selected-architecture-decisions.md) records the Project Owner / Backend Developer's 2026-10-10 acceptance of all 18 current [ADA](architecture-decision-analysis.md) recommendations exactly as analyzed. All are Selected, with no override or deferral; the selection gate is satisfied. The selected record supplies solution choices separately from these requirement pressures.

[ADD](ADD.md) realizes the selected directions against these pressures. **Implementation View review → Deployment View** is the current handoff. [Logical View v0.1](views/logical-view.puml) is the human-accepted canonical decomposition; [Implementation View v0.1 — Baseline Draft](views/implementation-view.puml) proposes its source realization for review. Then follow Deployment View → Data View → ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation. Deployment/Data Views remain Steps 20–21, with ADRs at Step 22. Version 0.1.5 changes navigation/status only; all seven drivers, nine ASRs, constraints, traceability and acceptance measures remain unchanged.
