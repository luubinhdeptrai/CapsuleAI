# CapsuleAI — Quality Attribute Analysis

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Quality Attribute Analysis |
| Version | 0.1.5 |
| Status | Baseline Draft |
| Last Updated | 2026-10-10 |
| Ownership | Software Architect / Tech Lead; review with Developers, QA and Product |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 16–19, 39–43 and 46 |
| Method Reference | [14 Quality Attributes — architecture learning material](../../T%C3%A0i%20li%E1%BB%87u%20tham%20kh%E1%BA%A3o%20cho%20Architecture/14-Thu%E1%BB%99c-t%C3%ADnh-ch%E1%BA%A5t-l%C6%B0%E1%BB%A3ng.md) |
| Requirements Baseline | BRD v0.3; PRD v0.2; SRS v0.3; Business Rules v0.1.2 |
| Authoritative Location | `docs/04-architecture/quality-attribute-analysis.md` |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-08 | Initial analysis of all 14 workflow attributes, source-linked scenarios, constraints and preliminary ASR candidates; no change to upstream scope, requirements or delivery decisions. |
| 0.1.1 | 2026-10-08 | Synchronized current architecture-process navigation with ADA and explicit human selection before ADD; all classifications, scenarios, thresholds and analysis remain unchanged. |
| 0.1.2 | 2026-10-10 | Synchronized current navigation with completed human selection of all 18 ADA recommendations and ADD next; all classifications, scenarios, thresholds and analysis remain unchanged. |
| 0.1.3 | 2026-10-10 | Current navigation acknowledges ADD v0.1 and Logical View next; classifications, source scenarios, thresholds and analysis remain unchanged. |
| 0.1.4 | 2026-10-10 | Linked Logical View v0.1 and advanced current navigation to Implementation View; classifications, scenarios, measures and historical revision entries remain unchanged. |
| 0.1.5 | 2026-10-10 | Linked Implementation View v0.1 and recorded explicit human acceptance of the Logical decomposition; review precedes Deployment View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |

## 2. Purpose

Identify the qualities that should shape CapsuleAI's later architecture reasoning. The analysis spans the complete private, Vietnam-first mobile MVP: confirmed wardrobe information, valid personalized advice, reported use, contextual Coverage and Gaps, and hypothetical candidate utility with optional shopping navigation.

This artifact explains architectural pressures and practical Backend Developer implications. It prepares formal ASR identification without selecting system structures, technologies, interface designs or operating mechanisms. Significance and candidate strength are analysis judgments for review; they do not record approval, working software or passed acceptance tests.

## 3. Analysis Method

The workflow was read first. The complete architecture reference was then read and used for terminology, the distinction among functional requirements, quality requirements and constraints, and the six-part scenario model. Current BRD/PRD/SRS/Business Rules, the master Use Case Diagram and all 23 specifications, all 12 Activity Diagrams, the Product Goal, all 41 PBIs, DoD, Delivery Decisions and all eight stories plus their index were read before drafting. Other repository material was inspected for relevant constraints; historical directions retain the treatment in Section 21.

The taxonomy matches Workflow Section 16 exactly. The reference groups **Supportability and Testability** as system qualities; **Availability, Interoperability, Manageability, Performance, Reliability, Scalability and Security** as runtime qualities; **Conceptual Integrity, Flexibility, Maintainability and Reusability** as design qualities; and **Usability** as a user quality. Privacy is analyzed under Security; Accessibility and localization under Usability, with their effects on other attributes retained. These concerns do not add taxonomy members.

Each assessment considers stakeholder value, current acceptance obligations, failure consequences and the breadth of architectural impact. The workflow's example priorities are illustrative; the judgments here derive from CapsuleAI's current sources.

| Significance | Interpretation in this analysis |
| --- | --- |
| Critical | Failure compromises private participation or authoritative state across the value loop; architecture must address the concern from the outset. |
| High | Explicit acceptance or shared domain/integration concerns strongly influence responsibilities, resource handling or verifiability. |
| Medium | A real but bounded MVP obligation or change pressure needs attention without justifying extensive architecture on its own. |
| Low | Limited current demand; avoid speculative infrastructure or abstractions. |

Concrete scenarios use exactly **Source of Stimulus → Stimulus → Environment → Artifact → Response → Response Measure**, following reference slides 14–15 and 21. QA-* scenario labels identify analysis examples only; they are neither new requirements nor ASR IDs. Numerical acceptance measures come exclusively from current sources. Qualitative measures use inspectable pass/fail outcomes from those sources, without inventing effort, throughput or recovery targets.

Detailed scenarios are included where they add distinct evidence. Shared scenarios are referenced across attributes instead of repeated. Architectural implications identify responsibilities, coordination, consistent information, resources and validation concerns from reference slides 18–22; tactics and technology selection remain later work.

## 4. Requirement and Constraint Context

| Type | CapsuleAI example | Architecture-analysis treatment |
| --- | --- | --- |
| Functional Requirement | FR-OUT-001–FR-OUT-009 define valid owned outfit choices, ranking and honest limited results. | Identify necessary behavior and its interactions. A feature name alone does not select architecture. |
| Quality Attribute Requirement | NFR-PERF-002 requires outfit-result p95 <3 seconds under the prescribed workload. | Analyze how resource use and coordination could affect the acceptance measure. |
| Constraint | PRD OPQ-008 and SRS Sections 2.4/3.1.1 establish JWT access/refresh authentication and session policy; Section 2.3 establishes supported mobile platforms. | Preserve already established limitations while leaving their detailed realization open. |

Source prefixes do not determine architectural significance by themselves. Functional/data rules such as logical Wear identity, authoritative corrections and same-basis Multiplier exactness create Reliability and Conceptual Integrity pressures. They retain their original requirement identity. JWT is a mandated direction; authorization and secret protection express required behavior and Security qualities, while revocation implementation remains open.

The full MVP is the analysis horizon. The first eight stories refine access, recovery, manual creation and wardrobe inspection; they do not replace later styling, Wear, Coverage or candidate obligations. The backlog order, first refinement horizon, estimates and Sprint selection remain unchanged. DoD Sections 4–5 require applicable quality evidence with each increment; later quality PBIs broaden verification rather than postpone protection.

Current numerical boundaries distinguish supported operation from test configuration. The exactly-100-garment timing workload is neither an onboarding quota nor a wardrobe maximum. The 20-user workload tests functional integrity separately. The recoverable-restart target is a validation condition, not a production uptime commitment. No Multiplier latency target, general API latency, larger-wardrobe threshold or deployment topology is established.

## 5. Quality Attribute Summary

“Strong,” “Possible” and “Unlikely” indicate preliminary potential for formal ASR selection. They neither assign ASR IDs nor require a separate ASR for every row. Scenario references point to the owning attribute section.

| QA | Significance | Primary Drivers | Scenario Needed? | Likely ASR Candidate? |
| --- | --- | --- | --- | --- |
| Performance | High | NFR-PERF-001/002; NFR-TEST-003; PBI-035 | Yes: QA-PERF-01/02 | Strong |
| Availability | Medium | NFR-AVL-001; ERR-DEP-001; optional dependencies | Yes: QA-AVL-01; shared QA-REL-01/03, QA-INT-01 | Strong |
| Reliability | Critical | DATA-INT-*; Wear identity/effects; NFR-REL-001–003 | Yes: QA-REL-01–03; shared QA-CON-01 | Strong |
| Security | Critical | NFR-SEC-*; NFR-PRIV-*; session policy; DATA-RET-* | Yes: QA-SEC-01–03 | Strong |
| Scalability | Medium | NFR-SCA-001/002; bounded current validation | Yes: QA-SCA-01; shared QA-PERF-02 | Possible |
| Maintainability | Medium | NFR-MNT-001; LOC-002/003; DoD change propagation | Yes: QA-MNT-01 | Possible |
| Flexibility | Medium | LOC-003; BR-023; conditional information and future opportunities | No separate scenario; QA-MNT-01 and dependency scenarios cover current obligations | Possible |
| Reusability | Low | Shared outfit meaning; no separate reuse product requirement | No separate scenario; QA-CON-01 covers consistency | Unlikely |
| Interoperability | High | NFR-INT-001; SI-001–004; weather/recovery/shopping semantics | Yes: QA-INT-01; shared QA-SEC-02, QA-AVL-01 | Strong |
| Conceptual Integrity | High | User authority; validity/ranking; outfit identity; Coverage/Multiplier basis | Yes: QA-CON-01; shared QA-REL-01/02 | Strong |
| Usability | High | NFR-USE-*; NFR-ACC-*; LOC-*; core task groups | Yes: QA-USE-01/02 | Strong |
| Testability | High | NFR-TEST-*; locked AI benchmark; DoD integration evidence | Yes: QA-TEST-01; shared timing and domain scenarios | Strong |
| Supportability | Medium | NFR-SUP-001/002; truthful recovery; private evidence | Yes: QA-SUP-01 | Possible |
| Manageability | Medium | PBI-001/040; NFR-REL-003; DoD operational criteria | Shared QA-REL-03 and QA-SUP-01; no separate numeric scenario | Possible |

## 6. Performance

**Definition and drivers.** Complete trustworthy processing and advice within their specified response intervals. NFR-PERF-001/002 and SRS Sections 2.3/12.3 refine BRD MET-Q02/MET-Q03; FEAT-AI-001 and FEAT-OUT-001 support BG-01/BG-02. UC-007/AD-007 and UC-011/AD-011 establish what must be available at completion; PBI-035 provides the delivery evidence route.

**Risks and architectural implications.** Image processing, external waiting and valid-outfit computation can dominate latency. Measuring only a preview or the first partial outfit would falsely shorten the interval. Later design must account for the complete operation, resource demand and dependency delay while preserving validity and eligible inputs. No enumeration, caching or communication strategy is selected, and the daily result target is not applied to complete Multiplier evaluation.

**Backend Developer Relevance.** Eventually assess query/computation efficiency, dependency latency and operation boundaries. Preserve honest result states and capture reproducible timing evidence; do not meet latency by truncating valid inputs or hiding slow runs.

**Preliminary Significance: High.** Explicit user-facing limits constrain different expensive paths. Distinct timing scenarios are warranted.

### 6.1 QA-PERF-01 — Reviewable garment processing

Sources: SRS NFR-PERF-001, NFR-TEST-003, Section 3.4.1 and Section 12.3; UC-007; AD-007; PBI-013/035.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Authenticated User submitting a supported garment image. |
| Stimulus | Analysis request is accepted after image transfer completes. |
| Environment | SRS reference Android 10+ mid-range device with ≥6 GB RAM, or iOS 15+ iPhone 11-class or newer; stable Wi-Fi/4G-class network with download ≥20 Mbps, upload ≥5 Mbps and RTT ≤100 ms; supported image under Section 3.4.1. |
| Artifact | Garment-analysis operation through availability of its review result. |
| Response | Make both a usable processed preview and reviewable proposals available; retain user confirmation as authority. |
| Response Measure | p95 ≤5 seconds from accepted post-transfer analysis request to both outputs. Use 5 warm-ups then ≥100 measured executions; retain legitimate slow runs and record device/network/environment. Upload time and user review time are outside this interval. |

### 6.2 QA-PERF-02 — Complete daily outfit result

Sources: SRS NFR-PERF-002, NFR-SCA-001, NFR-TEST-003 and Section 12.3; UC-011; AD-011; PBI-017/035.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Authenticated User requesting daily outfits. |
| Stimulus | Outfit request is accepted with required context available. |
| Environment | The reference devices/network specified in QA-PERF-01; nominal wardrobe manifest contains exactly 100 confirmed garments with applicable eligibility and readiness identified. This is separate from the 20-user functional workload. |
| Artifact | Current-wardrobe recommendation operation. |
| Response | Evaluate eligible inputs, apply hard validity before ranking and make the first complete recommendation result set available, retaining actual fewer/no-valid outcomes where appropriate. |
| Response Measure | p95 <3 seconds from accepted request to the first complete result set. Use 5 warm-ups then ≥100 measured executions, retain legitimate slow runs and record conditions. No silent omission of eligible inputs, duplicate padding or invalid alternatives. |

## 7. Availability

**Definition and drivers.** Keep authorized, otherwise usable product paths accessible when optional capabilities fail. NFR-AVL-001, FR-MET-007, ERR-DEP-001 and ERR-MET-001 preserve core value; BR-024 and Product Goal Section 6 keep utility independent of commerce. UC-006/011/021/023 distinguish missing environmental information and optional navigation from total product loss.

**Risks and architectural implications.** Treating weather, retail or measurement as universal dependencies could unnecessarily block wardrobe value. Later design needs explicit dependency scope and failure containment. Availability does not bypass mandatory access/network or insufficient assessment evidence. No uptime percentage, offline guarantee or redundant deployment requirement follows.

**Backend Developer Relevance.** Identify which operations actually need each dependency and preserve the specified continuation when it fails. Required authentication and acceptance evidence remain gates; an optional provider failure must not change a successful action into failure.

**Preliminary Significance: Medium.** Strong bounded continuity obligations merit ASR consideration, while production availability guarantees remain unspecified. QA-AVL-01 is distinct; manual continuity, bounded weather and restart are covered in QA-REL-01, QA-INT-01 and QA-REL-03.

### 7.1 QA-AVL-01 — Optional dependency failure preserves value

Sources: SRS NFR-AVL-001, FR-SHOP-006–008, FR-MET-007, ERR-SHOP-003 and ERR-MET-001; BRULE-SHOP-002/003; UC-023; PBI-009/033/038.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Optional external shopping destination or measurement dependency. |
| Stimulus | A selected navigation cannot proceed, or measurement becomes unavailable during an otherwise usable core action; exercise each failure independently. |
| Environment | Authorized connected use with available wardrobe data and a current supported candidate assessment; required product inputs remain valid. |
| Artifact | Candidate/gap/utility presentation and independently usable core-action paths. |
| Response | Explain affected unavailability, retain otherwise valid candidate information, gap, multiplier, reasons and previews where available, and preserve the core action's actual outcome. |
| Response Measure | Each exercised optional failure leaves the independent core path usable. Destination failure alone produces no +0, ownership, Wear or transaction state; unavailable measurement neither blocks/reverses the core action nor claims successfully recorded evidence. No offline or destination-uptime claim. |

## 8. Reliability

**Definition and drivers.** Preserve correct authoritative state and trustworthy outcomes during uncertainty, retries, mutation and recoverable faults. DATA-INT-001–004, NFR-REL-001–003, COM-002, ERR-NET-001 and the Wear rules apply across the loop. UC-007/014/016/017 and their Activity Diagrams expose acceptance boundaries and survivor effects; PBI-016/021–024/038/039 carry these concerns beyond initial stories.

**Risks and architectural implications.** Retrying an accepted action could create duplicates; collapsing all same-day reports could erase intentional history. Accepted removal/correction could leave stale utilization, personalization or assessments. An uncertain response must not assert either acceptance or definite absence. Architecture must establish coherent acceptance, safe action identity, consistent derived effects and recoverability. Transaction boundaries and coordination need later reasoning, without selecting transactions, queues or storage mechanisms here.

**Backend Developer Relevance.** Eventually design and verify mutation boundaries, safe retries and derived-state changes. Distinguish event identity from ranking normalization, original event-local grouping from absolute elapsed aging, and known rejection from uncertain communication. Retain accepted state across restart; use QA-CON-01 for assessment integrity.

**Preliminary Significance: Critical.** Incorrect state propagates into every later recommendation and insight. The following scenarios cover distinct continuity, behavioral-integrity and recovery pressures.

### 8.1 QA-REL-01 — Manual continuation and authoritative correction

Sources: SRS NFR-REL-001, FR-AI-003/007/008/010, AI-REQ-007, DATA-INT-001 and ERR-AI-001/002; BRULE-GAR-001–003; UC-007/AD-007; US-GAR-001 AC-01/02/09/12.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | User and unavailable, failed or uncertain garment-analysis capability. |
| Stimulus | Image assistance cannot provide useful information; the User supplies and confirms the manual minimum. Later automated output disagrees with the accepted values. |
| Environment | Authorized connected entry, exercising all agreed representative AI-failure cases; manual saving capability remains usable. |
| Artifact | Garment confirmation and authoritative wardrobe profile. |
| Response | Permit imageless manual review/confirmation, acknowledge accepted creation separately from draft/failure, explain applicable readiness and retain confirmed/corrected values after later analysis. |
| Response Measure | Manual creation succeeds in every agreed representative AI-failure case when the confirmed minimum is accepted. An unconfirmed/canceled attempt establishes no garment; retry of an accepted addition creates no unexplained duplicate; later analysis silently overwrites no authoritative value. |

### 8.2 QA-REL-02 — Wear retries, correction and survivor effects

Sources: SRS FR-WEAR-003/004/006–010, FR-PERS-012/013, DATA-WEAR-003/004, DATA-INT-004 and ERR-WEAR-001/002; BRULE-WEAR-002–006, BRULE-PERS-003/005/006; UC-014/016/017 and AD-014/016/017; Business Rules Section 15, B11–B14/B19/B20.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Authenticated User reporting and repairing Wear Events; interrupted communication. |
| Stimulus | Retry a logical report after an uncertain response, initiate a separate same-outfit/day report, then correct or remove the selected accepted report. |
| Environment | Controlled valid owned outfit and known accepted timestamps; original event-local date/time is preserved even after device-timezone change. Exercise accepted, rejected, canceled and uncertain mutations. |
| Artifact | Wear history, utilization, recency and effective normalized ranking evidence. |
| Response | Resolve the original intention without duplication, preserve separate intentional reports, and update only accepted correction/removal effects using surviving report memberships and absolute anchors. |
| Response Measure | One logical retry retains one event; a new explicit report remains distinct. Same exact outfit/original day supplies at most one +2 increment before prescribed decay. Accepted correction/removal recomputes the latest surviving anchor and elapsed age/recency; no survivors means no group Wear contribution. Invalid future/cross-original-day corrections and canceled/known failed mutations preserve accepted state; uncertainty makes no false completion/absence claim. Underlying Outfit/Garments and unrelated reports remain intact. |

### 8.3 QA-REL-03 — Recoverable restart

Sources: SRS NFR-REL-003 and Section 12.3; NFR-AVL-001; PBI-038/040; DoD Section 5 operational trigger.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Recoverable CapsuleAI application/service interruption in validation. |
| Stimulus | A recoverable application/service restart occurs after authoritative user information was accepted. |
| Environment | Agreed validation environment with known accepted account, wardrobe and relevant operation state; exercise applicable optional-dependency fallback. |
| Artifact | Core authorized access, wardrobe and advice service behavior. |
| Response | Restore core operation with previously accepted authoritative information intact and applicable optional-dependency fallbacks available. |
| Response Measure | Core service is restored within ≤5 minutes after the recoverable restart; controlled accepted-state checks show no silent corruption and applicable fallback checks pass. This is neither a disaster-recovery/RPO requirement nor a production uptime SLA. |

## 9. Security

**Definition and drivers.** Prevent unauthorized access, disclosure or alteration and enforce purpose-limited private use. Privacy belongs here within the 14-QA taxonomy. BR-021; NFR-SEC-001–003; NFR-PRIV-001/002; COM-001/003; DATA-RET-001–003; BRULE-AUTH-007 and PBI-002/041 cover access, secrets, external exchange and lifecycle. Optional body/gender/location remains user-controlled.

**Risks and architectural implications.** Protected content can leak through operations, images, previous-account state, recovery, diagnostic evidence or retained copies. Refresh/recovery state can be inconsistent with revocation policy. Training, measurement and functional history have different permitted purposes and retention. Architecture needs explicit authority, secret protection, private-data flow and lifecycle enforcement boundaries. JWT/session policy is established; signing, storage, key management and revocation realization are open.

**Backend Developer Relevance.** Eventually enforce authorization for affected objects and sessions, protect secrets in outputs/evidence and account switching, and verify minimal disclosures and lifecycle effects. Confirmation/correction/feedback is not training consent. A retained historical snapshot cannot justify keeping original removed images indefinitely.

**Preliminary Significance: Critical.** Private participation is a prerequisite for all product value. Access isolation, policy enforcement and private lifecycle deserve distinct scenarios.

### 9.1 QA-SEC-01 — User and account isolation

Sources: SRS FR-AUTH-008/009, NFR-SEC-001–003, DATA-GAR-001 and COM-001; BRULE-AUTH-001; UC-002/003/008; US-AUTH-002 AC-06, US-AUTH-004 AC-04, US-WAR-001 AC-09, US-WAR-002 AC-11.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Unauthenticated requester or authenticated User attempting another account's protected information. |
| Stimulus | Attempt protected wardrobe/image access, including after logout or switching accounts on a device. |
| Environment | Agreed unauthenticated and two-account validation fixtures, with prior protected content displayed and current-session authority known. |
| Artifact | Personal operations, garment images and protected client-visible results. |
| Response | Deny unauthorized access, expose only the current authorized account's content and give appropriate renewed-access guidance without disclosing secrets or labeling inaccessible data empty. |
| Response Measure | Zero successful unauthorized cross-user wardrobe/image accesses in the agreed scenarios. Account switch/logout exposes no preceding-user protected content; credential/token/recovery secrets appear in no unauthorized output or ordinary measurement. |

### 9.2 QA-SEC-02 — Session and recovery boundaries

Sources: SRS Section 3.1.1, FR-AUTH-004/007/010/013/014/017/018, DATA-AUTH-005 and SI-003; BRULE-AUTH-004–006; UC-002/003/004; AD-004; US-AUTH-003 AC-02–08, US-AUTH-004 AC-01/02/06 and US-AUTH-005 AC-01–09/13–15.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Returning User, repeated refresh credential, or User initiating/completing email recovery. |
| Stimulus | Renew at access expiry, reuse a consumed Refresh Token, log out of a selected session, or issue/complete a reset interaction; exercise these policy branches independently. |
| Environment | Known account and separate device/session fixtures with controlled issuance/expiry time and successful, failed or uncertain email/reset outcomes. |
| Artifact | Account access, Refresh Sessions and single-use reset validity. |
| Response | Apply the established lifetime/rotation/revocation boundaries, require authentication where authority is unusable, and distinguish initiation/delivery from actual completed reset. |
| Response Measure | Expired 15-minute access alone grants no protected use. Rotation consumes the current refresh credential and does not extend the maximum 30-day session; reuse revokes the affected session. Logout targets only the current session. Reset permits one success before its 30-minute expiry; successful replacement invalidates earlier unused interactions. Completed reset revokes all account Refresh Sessions and requires the new password; unaccepted delivery/reset does not claim completion. Registered/unregistered initiation responses are equivalent and account-safe. |

### 9.3 QA-SEC-03 — Purpose limitation and data lifecycle

Sources: SRS NFR-PRIV-001/002, SI-001–004, COM-003, FR-PROF-007, FR-WAR-005, FR-WEAR-007, FR-MET-006, DATA-RET-001–003 and Section 12.1; BRULE-AUTH-007, BRULE-GAR-009, BRULE-WEAR-005, BRULE-HIST-002/003; UC-005/010/017/023; PBI-002/009/016/023/041.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | User accepting personal-data removal; product measurement and external interactions using private information. |
| Stimulus | Remove optional profile information, a garment or an individual Wear Event; inspect personal-data uses and measurement at its retention boundary. |
| Environment | Authorized controlled data-lifecycle fixtures and inspectable processing/exchange evidence, with known acceptance and retention times. |
| Artifact | Current personalization/ownership/evidence, minimal history, removed personal data, user-linked measurement and external disclosures. |
| Response | Stop effective use at the applicable accepted boundary, preserve only necessary functional history, enforce deletion/measurement limits and purpose-minimal processing/exchange. |
| Response Measure | Removed optional context stops future personalization use; accepted garment/event removal immediately excludes the applicable current/effective inputs. Applicable removed event data and original removed-garment images/nonessential data are physically deleted within 30 days. User-linked measurement is deleted or aggregated/de-identified within 90 days with older retained measurement no longer user-linked. Necessary unremoved history/feedback is not erased solely by ranking expiry. MVP personal data enters no AI training/improvement. Selected-image analysis remains purpose-limited under SI-001; weather, recovery and shopping exchanges contain only necessary selected location, recovery-delivery information or explicitly chosen navigation, with no unrelated private wardrobe/profile/history. |

## 10. Scalability

**Definition and drivers.** Preserve correct core behavior as the specified wardrobe workload and concurrent use increase. NFR-SCA-001/002 and SRS Section 12.3 establish the current acceptance envelope; PBI-035/038 provide evidence routes. BR-005/007/009 prohibit an arbitrary wardrobe maximum or omission of eligible inputs.

**Risks and architectural implications.** Shared resources may cause cross-user interference, duplicate acceptance or incomplete calculations. Combinatorial evaluation can grow faster than garment count, especially for exact hypothetical sets. Later design must consider contention and resource demand without assuming elastic cloud deployment or a mass-market throughput requirement.

**Backend Developer Relevance.** Eventually test concurrent ownership and mutation behavior as well as computation/resource limits. Keep nominal timing, functional concurrency and exact Multiplier completeness separate; report limits truthfully instead of changing domain meaning.

**Preliminary Significance: Medium.** Current capacity evidence is bounded; no additional volume or throughput SLA exists. QA-SCA-01 covers concurrency, while QA-PERF-02 covers nominal wardrobe behavior.

### 10.1 QA-SCA-01 — Concurrent functional integrity

Sources: SRS NFR-SCA-002 and Section 12.3; FR-AUTH-008, DATA-INT-004; PBI-038.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Concurrently active authenticated Users. |
| Stimulus | Exercise core access, wardrobe/recommendation and applicable mutation/retry operations concurrently. |
| Environment | Agreed validation environment with 20 concurrently active authenticated users and known ownership, accepted-state and logical-action fixtures. |
| Artifact | Core operations and each user's accepted personal state. |
| Response | Keep core operations functional and preserve authorization, isolation, accepted state and logical-action outcomes under concurrent use. |
| Response Measure | At 20 active users, no cross-user leakage, broken authorization, corrupted accepted state or accidental duplicate logical actions are caused solely by concurrency; exercised core operations remain functional. No p95-at-20-users, throughput or higher-concurrency target is inferred. |

## 11. Maintainability

**Definition and drivers.** Make controlled changes without silently changing established meaning or breaking affected behavior. NFR-MNT-001, LOC-002/003, BRULE-GAR-004–008 and DoD Sections 4–5 support stable vocabularies, user authority and traceable evolution. PBI-001/003/039/040 identify delivery and regression needs.

**Risks and architectural implications.** Duplicated semantic interpretations or localized labels used as identity can turn wording changes into incompatible data/rules. Later design should make the effect of changes identifiable and verifiable, including relevant rule/version freshness. This does not prescribe a module layout or set a change-effort target.

**Backend Developer Relevance.** Eventually preserve canonical meanings independently of display text, assess changed-rule/data/consumer impact, and update affected regression and documentation evidence. Do not edit unrelated artifacts simply to satisfy a checklist.

**Preliminary Significance: Medium.** The explicit change requirement is bounded semantic preservation; wider maintenance cost is not quantified. QA-MNT-01 makes that obligation testable.

### 11.1 QA-MNT-01 — Labels change without domain drift

Sources: SRS NFR-MNT-001, LOC-002/003; PRD OPQ-002/003/005; BRULE-GAR-004/007/008; PBI-003/036.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Authorized product/content maintenance change. |
| Stimulus | Revise a Vietnamese display label for an established classification, style/occasion, confidence or provenance meaning. |
| Environment | Controlled baseline fixtures with confirmed data and expected classification, readiness and evaluation outcomes; canonical meanings remain unchanged. |
| Artifact | Vocabulary interpretation, localized presentation and affected confirmed information/domain behavior. |
| Response | Apply the label change while preserving the same canonical meaning and accepted data; keep affected regression evidence inspectable. |
| Response Measure | Before/after fixtures retain their confirmed values and domain outcomes; only the intended display wording changes. Label changes introduce no new vocabulary meaning or silent data migration. LOC-003 supports later label-language additions without making another language an MVP requirement. No elapsed-time or number-of-files change target is added. |

## 12. Flexibility

**Definition and drivers.** Accommodate justified changes in labels, relevant context and dependencies without redefining core business meaning. BR-023, PRD Sections 25/28, LOC-003 and the conditional SI/SHOP requirements provide bounded readiness and adaptation pressures. Future categories, markets, integrations and learned ranking remain opportunities requiring approval.

**Risks and architectural implications.** Rigid dependency assumptions can impede legitimate evolution; speculative extension frameworks can complicate the current product. Architecture should later assess replaceability and controlled evolution at actual points of variation while respecting current taxonomy and authority. No dynamic rule engine, plugin system, provider-swap SLA or additional MVP feature is required.

**Backend Developer Relevance.** Eventually identify genuine variation in evidence, labels and dependency outcomes while retaining stable domain obligations. Existing profile/context revisions are functional behavior, not evidence for unlimited configuration or a backend-owned product roadmap.

**Preliminary Significance: Medium.** Future adaptability matters but lacks an approved replacement/change-effort acceptance measure. No separate detailed scenario is warranted: QA-MNT-01 covers explicit semantic adaptation and QA-AVL-01/QA-INT-01 cover current conditional dependency behavior. Broader change scenarios need approved future scope before becoming acceptance commitments.

## 13. Reusability

**Definition and drivers.** Enable useful behavior to serve another context without unnecessary duplication. CapsuleAI currently requires consistent validity and identity across Daily Outfits, Shuffle, Coverage and Multiplier (FR-OUT-003, DATA-OUT-001 and FR-MULT-002). That consistency supports Conceptual Integrity; it is not an explicit requirement for an independently reusable product component.

**Risks and architectural implications.** General-purpose libraries, cross-product services or public SDKs could add cost without an approved consumer. Later design may assess common responsibilities inside CapsuleAI, but external reuse should not determine boundaries now. There is no multi-product, multi-tenant platform or public reuse contract in the MVP.

**Backend Developer Relevance.** Eventually avoid divergent copies of rules where shared meaning is required. Prefer abstractions justified by actual uses; do not build speculative reusable frameworks or expose internal behavior as a product API for reuse.

**Preliminary Significance: Low.** No independent reuse obligation or measurable reuse target exists. No separate scenario is warranted; QA-CON-01 verifies the actual cross-context consistency requirement. A standalone ASR is unlikely without a later approved consumer.

## 14. Interoperability

**Definition and drivers.** Exchange information with logical capabilities and external participants while preserving required meaning. NFR-INT-001 and SI-001–004 cover garment analysis, weather, recovery email and optional shopping. The master Use Case Diagram associates email with UC-004, device location/weather with UC-006 and external navigation with UC-023; other assessments consume context. Garment analysis can be internal or external under SRS Section 5.2.

**Risks and architectural implications.** Provider responses may lose timestamp, provenance or uncertainty; delivery may be mistaken for reset completion, and navigation for purchase. Missing commercial data can be incorrectly treated as zero utility. Architecture needs explicit exchange/outcome and purpose-minimal information boundaries; no provider, transport payload or internal AI service boundary is selected.

**Backend Developer Relevance.** Eventually verify dependency success/failure semantics, relevant timestamps, incomplete information and disclosure limits through contract/integration evidence. A Spring Boot–Python interaction appears in historical material, but the current analysis requires logical capability outcomes without mandating that stack.

**Preliminary Significance: High.** Several dependencies have explicit semantic and timing boundaries affecting multiple journeys. QA-INT-01 covers weather; QA-SEC-02 covers recovery outcomes and QA-AVL-01 covers navigation/measurement independence. Commercial guidance retains source/last-checked time and the existing inclusive 24-hour currentness boundary (FR-SHOP-003), without a new integration or merchant guarantee.

### 14.1 QA-INT-01 — Weather freshness and bounded fallback

Sources: SRS FR-WEATHER-003–005, SI-002, ERR-WEATHER-001 and Section 12.2; BRULE-PROF-003, BRULE-OUT-006; UC-006/AD-006 and consuming UC-011/019/022; PBI-011.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Weather Information Provider during selected-context acquisition. |
| Stimulus | Supply stale information or fail to return usable current weather within the allowed external-request assessment delay. |
| Environment | Optional consented location or independently selected manual city, authorized connected assessment and sufficient remaining information for defensible reduced-context use. |
| Artifact | Weather-context exchange and affected recommendation/Coverage/Multiplier interpretation. |
| Response | Request only necessary selected location, check actual evidence timestamp, attempt refresh of stale weather and disclose unavailable environment when usable current information is absent by the boundary. Preserve all remaining hard validity. |
| Response Measure | Weather at age ≤30 minutes is current; older evidence is refreshed before current use. The external request delays assessment by at most 2 seconds; without usable current information by then, reduced-context behavior skips only unavailable environmental filtering. No invented weather, blanket outfit rejection or unrelated wardrobe/profile/history disclosure; changed relevant basis refreshes or marks affected advice Outdated. |

## 15. Conceptual Integrity

**Definition and drivers.** Keep one coherent meaning for information, rules and results throughout the product. BR-002/009/014–017/022/024; DATA-OUT-001–003; DATA-ANL-001–004; NFR-REL-002; and BRULE-COV/MULT require consistent authority, outfit identity, validity, sufficient evidence and assessment basis. UC-011/019–022 and AD-011/019–022 expose the connected interpretation.

**Risks and architectural implications.** Separate implementations could count identity permutations differently, conflate Saveable with Ready, use only displayed daily choices as complete sets, or mix historical/hypothetical ownership. Architecture needs coherent responsibility and information interpretation across consumers, including relevant rule versions and changed-basis effects. This is an analytical need for shared semantics, not a decision to share a particular class, service or store.

**Backend Developer Relevance.** Eventually use consistent canonical meanings and explainable assessment states across queries, computations and consumers. Keep hard validity separate from ranking, reported history separate from effective evidence, and candidate utility separate from commerce.

**Preliminary Significance: High.** Inconsistent semantics undermine the product's differentiating intelligence across several capabilities. QA-CON-01 covers assessment coherence; QA-REL-01/02 cover garment authority and behavioral meaning.

### 15.1 QA-CON-01 — Consistent valid sets and honest utility

Sources: SRS FR-OUT-003/007/009, FR-ANL-010–015, FR-GAP-004, FR-MULT-002–008, DATA-OUT-001, DATA-ANL-004 and NFR-REL-002; BRULE-OUT-008/009, BRULE-COV-002–006, BRULE-MULT-001–009; Business Rules Section 15 B05/B07–B09/B16/B17; UC-019/020/022; AD-019/020/022; PBI-026/028/030/031/039.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | User requesting Coverage or candidate utility, including after a relevant input/rule change. |
| Stimulus | Evaluate controlled valid-set fixtures, vary information sufficiency/completion, reorder constituents or change the previously evaluated basis. |
| Environment | Authorized current wardrobe and hypothetical candidate with explicit readiness, priorities, context and relevant rule version; fixtures include complete, zero, partial, unavailable and changed-basis conditions. |
| Artifact | Valid outfit identity, Coverage/Gaps and Multiplier count/state/previews. |
| Response | Use identity-set uniqueness and applicable hard validity consistently, apply Coverage sufficiency/weighted meaning, and claim exact candidate utility only from complete same-basis sets. |
| Response Measure | Permuting the same constituent identities adds no outfit. Existing complete 41-current/58-expanded fixture gives +17; identical completed sets give Evaluated Zero/+0. Missing/partial/unavailable evaluation yields no exact +N or false +0, and relevant changes yield Outdated until current evaluation is established. Existing weighted Coverage fixture B09 gives 50; missing positive priorities/required ready roles yields no fabricated 0% or gap. Previews contain the hypothetical candidate and share the count's basis; assessment/viewing changes neither ownership nor Wear history. |

## 16. Usability

**Definition and drivers.** Let the target audience understand and operate trustworthy wardrobe journeys. Accessibility and localization are included here. NFR-USE-001/002, NFR-ACC-001/002 and LOC-001–005 implement BR-022/023 and the three personas. SRS Section 12.4, PBI-003/036/037 and all current story AC for accessible guidance cover both successful and limited outcomes.

**Risks and architectural implications.** Users may confuse confidence with truth, an unready garment with an unsaved one, a failed retrieval with emptiness, or a hypothetical preview with ownership. Image/color-only meaning and inaccessible primary actions can block participation. Later design must support correct state, limitation and explanation information for mobile presentation, preserving Vietnamese semantics, accessible action meaning and original event-local time. Backend behavior alone cannot establish mobile accessibility or representative-user usability.

**Backend Developer Relevance.** Eventually supply honest outcomes, accepted values, missing-input reasons and assessment basis that the mobile experience can explain. Preserve diacritics and time meaning. Work with mobile/UX/QA on integrated evidence; do not expose implementation traces as product explanations.

**Preliminary Significance: High.** Explicit core-task and accessibility acceptance affects participation across the loop. QA-USE-01/02 separate representative-user evidence from accessibility checks. NFR-USE-002 also requires criterion-based preview review; prediction accuracy alone cannot satisfy it.

### 16.1 QA-USE-01 — Understand and complete core tasks

Sources: SRS NFR-USE-001, LOC-001 and Section 12.4; PRD Sections 21–25; PBI-037.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Representative Users from the current personas. |
| Stimulus | Attempt the prescribed core tasks without facilitator intervention, including interpreting relevant results/limitations. |
| Environment | Vietnamese supported mobile experience; at least 5 participants total across personas where practical; representative implemented tasks and known fixture states. |
| Artifact | Register/Log In; Add Garment; Get Outfits with Shuffle; Record and correct/remove Wear; Coverage and Gaps; Candidate/Multiplier evaluation. |
| Response | Provide understandable actions, confirmation/readiness, explanations and recovery so Users can complete the relevant task and interpret its outcome. |
| Response Measure | Target ≥80% unaided completion for each evaluated representative core task; zero unresolved Critical usability blockers for acceptance; target average SUS ≥68 as a validation aid. Record per-task results and findings, not an invented growth KPI or 5-participant-per-persona requirement. |

### 16.2 QA-USE-02 — Accessible actions and state meaning

Sources: SRS NFR-ACC-001/002 and Section 12.4; LOC-001; PBI-036; current stories' accessibility AC, including US-GAR-001 AC-14 and US-WAR-002 AC-12.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | User relying on a screen reader or enlarged text. |
| Stimulus | Operate applicable core journeys and interpret confidence/readiness, non-exact utility, hypothetical and removed states. |
| Environment | Supported Android/iOS; Android TalkBack and iOS VoiceOver; text scaling up to 200%. |
| Artifact | Vietnamese actions, interactive semantics, explanations and state presentation. |
| Response | Provide meaningful accessible names/roles/states and labels beyond color or image alone, retaining operable primary actions and accurate outcomes. |
| Response Measure | Applicable core journeys are operable under both screen readers and at text scaling up to 200%; primary-control targets meet the approximately 48 dp Android/44 pt iOS minimum criteria. Confidence, readiness, Incomplete/Unavailable/Outdated, hypothetical and Removed from wardrobe remain understandable without color/image-only cues. These checks make no certification claim. |

## 17. Testability

**Definition and drivers.** Make required outcomes and their evidence controllable, inspectable and reproducible. NFR-TEST-001–003, AI-REQ-010/011/014, FR-MET-004 and SRS Sections 8.1/12.3 define domain, benchmark and timing evidence. PBI-001/014/035/039 and DoD Sections 4–6 require meaningful automated and integration checks with protected evidence.

**Risks and architectural implications.** Hidden assessment basis, uncontrolled clocks/provider states or contaminated evaluation data can make correctness claims unverifiable. Mocks alone cannot establish an actual required integration. Architecture needs observable accepted outcomes and separable evaluation evidence for domain correctness, prediction quality, preview usability and timing. No public test endpoint, test-only user actor, immutable audit store or particular testing framework is required.

**Backend Developer Relevance.** Eventually build deterministic domain fixtures, controlled time/failure cases and relevant unit/integration/contract checks. Preserve sample manifests and operation boundaries; distinguish a documentation audit from executed software verification. Evidence must avoid unnecessary private content.

**Preliminary Significance: High.** Interacting domain rules and explicit numerical acceptance require credible evidence. QA-TEST-01 addresses AI acceptance reproducibility; QA-PERF-01/02 and QA-CON-01 already supply timing and inspectable-domain cases without duplication.

### 17.1 QA-TEST-01 — Reproducible garment-assistance evaluation

Sources: SRS Section 8.1, AI-REQ-010/011/014, NFR-TEST-002 and NFR-USE-002; PBI-014; DoD Section 5 AI trigger.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | QA/AI contributors executing the established acceptance evaluation. |
| Stimulus | Evaluate garment assistance against the locked qualified-image benchmark and inspect its evidence. |
| Environment | Locked set of ≥200 qualified images with ≥50 each for TOP, BOTTOM, OUTERWEAR and FOOTWEAR; human category and controlled dominant-color-family ground truth. Two independent annotators should label with disagreement adjudicated; the locked corpus is excluded from training/tuning after lock. |
| Artifact | Garment-assistance results, benchmark/label manifests and independent preview review. |
| Response | Produce inspectable results tied to qualified inputs and the evaluated implementation, reporting category and dominant-color correctness separately from preview usability and richer attributes. |
| Response Measure | Evidence establishes the required set size/balance and evaluation isolation. Category accuracy is ≥90% overall and ≥85% within each primary-category group; dominant-color accuracy is ≥90% overall, reported separately. Preview review checks identifiable garment, preserved major regions, non-obstructive background and sufficient correction/confirmation information under SRS Section 3.4.1; no preview percentage or equal-accuracy rich-attribute claim is introduced. |

## 18. Supportability

**Definition and drivers.** Provide useful information and recovery choices to understand and resolve problems without exposing private contents. NFR-SUP-001/002, NFR-TEST-002, NFR-SEC-003 and ERR-* distinguish failed/pending/unavailable from accepted/empty/zero. PBI-040 and DoD Section 6 add inspectable protected operational evidence.

**Risks and architectural implications.** False empty/zero messages can hide failures; excessively detailed diagnostics can leak images, secrets or sensitive profiles. Later design needs attributable operation/outcome evidence and useful recovery paths with purpose-minimal content. No logging vendor, support console or diagnostic-time target is selected.

**Backend Developer Relevance.** Eventually make failures and actual outcomes diagnosable, preserving timing/context evidence where required and avoiding secret or unnecessary private-content output. Product explanations should describe user-relevant limitations; operational evidence serves authorized investigation.

**Preliminary Significance: Medium.** Concrete inspectability and recovery obligations exist, but no support-service SLA is defined. QA-SUP-01 covers the distinction; recovery timing is already QA-REL-03.

### 18.1 QA-SUP-01 — Diagnose an unavailable operation safely

Sources: SRS NFR-SUP-001/002, NFR-TEST-002, NFR-SEC-003, FR-WAR-008, FR-MET-006 and ERR-NET-001; UC-008; US-WAR-001 AC-03/05/09 and US-WAR-002 AC-07/08/11; PBI-040.

| Element | CapsuleAI Scenario |
| --- | --- |
| Source of Stimulus | Failed/interrupted wardrobe operation encountered by a User and inspected by authorized validation/support contributors. |
| Stimulus | Retrieval fails, or interruption leaves an operation outcome unconfirmed; inspect the state and its applicable recovery/evidence. |
| Environment | Controlled accepted inventory and authorized test/operating context; unavailable retrieval, unusable access and genuinely empty inventory are separate cases. |
| Artifact | Operation-state presentation, recovery choices and permitted diagnostic/validation evidence. |
| Response | Identify the actual pending/failed/unavailable/unconfirmed condition, preserve accepted state, provide applicable retry/outcome-review/renewed-access guidance and support protected inspection. |
| Response Measure | Each case is distinguishable from confirmed success and genuine empty inventory; an uncertain result claims neither success nor definite absence. Applicable recovery is available and accepted inventory remains intact. Evidence distinguishes attempted/accepted outcomes and required timing information without exposing credential/token/recovery secrets or requiring raw images/sensitive profiles in ordinary engagement diagnostics. No resolution-time SLA is added. |

## 19. Manageability

**Definition and drivers.** Enable controlled configuration, repeatable deployment and verification of operating health/recovery. PBI-001/040, NFR-SUP-002, NFR-REL-003, SRS Section 8.6 and DoD Section 5's operational trigger provide the current basis. The reference's administrator concept does not introduce an Admin actor or management product into CapsuleAI.

**Risks and architectural implications.** Uncontrolled environments can invalidate benchmark evidence, expose secrets or make recovery difficult to reproduce. Later architecture and engineering preparation need responsibility for protected configuration, deployment/recovery and necessary visibility. Topology, management tools, backup mechanisms and monitoring products remain open; there is no production operational SLA beyond established validation conditions.

**Backend Developer Relevance.** Eventually keep environment settings and secrets controlled, make runnable versions/dependency state inspectable and contribute reproducible deployment/recovery checks. Do not embed environment-dependent changes that silently alter confirmed data or rule meaning.

**Preliminary Significance: Medium.** Real enabler outcomes affect delivery, while their mechanisms are deliberately unselected. QA-REL-03 and QA-SUP-01 provide current recovery/inspection measures. No separate scenario is added because a new deployment-time, backup or operator-console target would be unsupported; PBI-040's repeatable outcome will be refined at the appropriate workflow stage.

## 20. Cross-Quality Trade-offs

These tensions inform ASR identification → Architecture Decision Analysis → explicit human selection → ADD/views and later ADR rationale. Current obligations remain in force; the analysis selects no resolution.

| Potential tension | CapsuleAI consequence to examine later |
| --- | --- |
| Performance vs Reliability / Conceptual Integrity | Fast daily advice and complete exact Multiplier evidence have different completion boundaries. Resource limits must not produce a fabricated exact count, omit eligible inputs or bypass hard validity. |
| Performance vs Maintainability | Optimizing expensive evaluation may make rules harder to change or verify. Any future specialization must preserve shared semantics, rule-version meaning and valid benchmark evidence. |
| Security / Privacy vs Usability | Session expiry/revocation and minimal disclosure can interrupt continuity. Recovery must remain understandable without exposing prior-user data or relaxing authorization. |
| Reliability vs implementation complexity | Safe logical retries, intentional repeats and coherent survivor effects need precise acceptance/coordination. Added mechanisms must be justified by these outcomes rather than complexity for its own sake. |
| Availability vs Security / result honesty | Optional failures should preserve defensible core use, but unavailable authority or required evidence cannot be bypassed. Reduced context needs explicit limitations. |
| Flexibility vs Conceptual Integrity | Future labels/dependencies/markets can vary; canonical ownership, readiness, validity and utility must retain agreed meaning or change through approved sources. |
| Supportability / Testability vs Privacy | Reproducible timing, fault and domain evidence needs enough context for inspection while avoiding secret output, raw-image engagement payloads and indefinite user-linked measurement. |
| Scalability vs exact evaluation cost | More garments/concurrent requests increase computation and contention. Current 100-garment timing and 20-user functional checks do not justify arbitrary truncation or establish a larger-scale target. |

## 21. Architectural Constraints Identified

The following are existing imposed boundaries or mandated policies. Their related qualities are analyzed above; the table does not convert every quality measure into a technology constraint.

| Established constraint / policy | Authoritative source | Effect and remaining freedom |
| --- | --- | --- |
| Connected Vietnam-first mobile MVP; Android 10+ and iOS 15+ | BR-023; PRD Sections 2/26/27; SRS Sections 1.2/2.3 | Preserve supported platforms and permission alternatives. No consumer web/desktop, guaranteed offline mode or client framework is mandated. Reference hardware/network is validation configuration, not a user purchase minimum. |
| Vietnamese initial UI, English documentation and canonical meanings independent of labels | PRD OPQ-002; LOC-001–005; NFR-MNT-001 | Support current localization and later label additions without a second MVP language or changed confirmed data. |
| Email/password access with mandated JWT access/refresh direction and session/recovery policy | PRD OPQ-008; SRS Sections 2.4/3.1.1; FR-AUTH-* | Preserve 15-minute access, maximum 30-day session, rotation/reuse and revocation boundaries, 30-minute single-use/replacement reset, normalized unique email and exact 12–128-character password policy. No social/guest MVP access; mechanisms, keys and storage remain open. |
| Four-category, confirmed-authority and validity-first domain | SRS Sections 3.5.1/3.7.1/4.10; BRULE-GAR/OUT | TOP/BOTTOM/OUTERWEAR/FOOTWEAR, approved bounded vocabularies, manual Saveable minimum and applicable readiness constrain supported behavior. Domain rules are behavioral obligations, not a mandated database or internal module map. |
| Preserved event identity/time and assessment meaning | FR-WEAR-*; FR-PERS-*; FR-ANL-*; FR-MULT-*; DATA-OUT-*, DATA-ANL-* and DATA-WEAR-* | Keep intentional reports, safe logical retries, original local grouping/absolute aging, contextual Coverage and complete same-basis incremental utility. No new event architecture, enumeration strategy or arbitrary assessment TTL is prescribed. |
| Purpose-limited private information; no MVP personal-data AI training; minimal external exchange | NFR-PRIV-001/002; SI-002/003; COM-003; BRULE-AUTH-007 | Constrain permitted processing/exchange. Future personal-data training requires separate explicit opt-in and approved product/privacy change; current corrections/feedback do not authorize it. |
| Immediate effective removal, 30-day applicable physical deletion and 90-day user-linked measurement maximum | DATA-RET-001–003; FR-WAR-005; FR-WEAR-007; FR-MET-006; SRS Section 12.1 | Preserve necessary minimal history separately from removed images/nonessential data and measurement. Enforcement technology remains open; no account-delete/export product or statutory certification is added. |
| Logical external interactions and bounded evidence policies | SI-001–004; FR-WEATHER-003–005; FR-SHOP-001/003; SRS Section 12.2 | Analysis may be internal/external; weather age ≤30 minutes and wait ≤2 seconds; credible commercial price/availability up to 24 hours before refresh/stale qualification/omission. No provider or mandatory retailer contract. |
| Commerce remains optional external navigation | BR-024; FR-SHOP-005–008; UC-023 extension of UC-021 | No native cart/checkout/payment/order/fulfillment, verified purchase or automatic ownership/Wear from navigation. |
| Workflow ownership, evolving documentation and artifact order | Workflow Sections 39–43/46; DD-001–006; DoD | Architecture follows quality analysis and ASR identification; contracts, test strategy and engineering preparation precede later Sprint readiness/planning. Product ordering remains PO-accountable; no Sprint commitment or developer-owned product ranking. |

**Historical technology directions.** The [Implementation Summary](../../Initial%20files/CapsuleAI_Implementation_Summary.md), Sections 6 and 15.1, proposes React Native, Java/Spring Boot, MongoDB, AWS S3 and a Python AI/CV route, including an older “option B” selection. The [official outline](../../Initial%20files/%C4%90%E1%BB%80%20C%C6%AF%C6%A0NG%20%C4%90%E1%BB%92%20%C3%81N%202_%20H%E1%BB%86%20TH%E1%BB%90NG%20QU%E1%BA%A2N%20L%C3%9D%20V%C3%80%20G%E1%BB%A2I%20%C3%9D%20PH%E1%BB%90I%20%C4%90%E1%BB%92%20TH%C3%94NG%20MINH.md), Section 4.3, contains similar projected technology and older hardware direction. These remain historical inputs under SRS Section 1.5, PRD Sections 25/29 and current backlog authority; they are not promoted to hard current stack, AI-service topology, GPU, database or cloud constraints. JWT is different because current PRD/SRS explicitly preserve it. No code, CI configuration or later current architecture decision artifact was present to establish additional choices.

The historical proposal's mass-adoption goals, general API latency, tap-count and older operating-system/recognition values are not used as current acceptance thresholds. Current SRS measures govern. Business Analysis notes contain exploratory promotion ideas and preparation questions without a current architecture constraint.

## 22. Preliminary ASR Candidate Summary

These are concerns to examine in formal ASR identification, without ASR IDs or chosen solutions. Promotion should consider cross-cutting impact, consequence and validation evidence; several related qualities may support one driver.

| Concern for next-step examination | Candidate strength | Sources / scenario evidence | Why it may affect architecture |
| --- | --- | --- | --- |
| Private authority, session/recovery enforcement and lifecycle | Strong | NFR-SEC/PRIV, DATA-RET; QA-SEC-01–03 | Spans personal operations, secret/session state, external disclosure and retained information. |
| Accepted-state coherence, logical retries and effective behavioral history | Strong | DATA-INT-004, DATA-WEAR-003/004, BRULE-WEAR/PERS; QA-REL-01/02 | Requires coordinated interpretation of accepted actions and their derived consumers. |
| Consistent validity, identity and current exact intelligence | Strong | NFR-REL-002, DATA-OUT/ANL, BRULE-COV/MULT; QA-CON-01 | Links daily advice, Coverage/Gaps and candidate evaluation through one defensible basis. |
| Complete operation latency and reproducible evidence | Strong | NFR-PERF-001/002, NFR-TEST-003; QA-PERF-01/02 | Constrains resource/coordination reasoning on image and daily-result paths. |
| Dependency semantics, bounded weather and independent core continuation | Strong | NFR-INT-001, NFR-AVL-001, SI-*; QA-INT-01, QA-AVL-01, QA-SEC-02 | Affects interaction boundaries and failure propagation without fixing providers. |
| Recoverable core operation with accepted information intact | Strong | NFR-REL-003; QA-REL-03 | Requires later operating/recovery reasoning and verifiable restoration. |
| Explainable, accessible mobile outcomes | Strong | NFR-USE/ACC, LOC-*; QA-USE-01/02 | Influences information/action semantics across mobile and connected capabilities; integrated evidence is necessary. |
| Inspectable domain and AI acceptance | Strong | NFR-TEST-001/002, AI-REQ-010/011/014; QA-CON-01, QA-TEST-01 | Makes architectural outcomes testable independently of confidence or marketing claims. |
| Concurrent integrity and controlled operation | Possible | NFR-SCA-002, PBI-040; QA-SCA-01, QA-SUP-01 | Bounded workload and operational evidence need design attention, without proving larger-scale infrastructure necessity. |
| Semantic maintenance and bounded evolution | Possible | NFR-MNT-001, LOC-003; QA-MNT-01 | Could shape change responsibility and stable interpretation; no change-cost/SLA is established. |
| Independent cross-product reuse | Unlikely | No current consumer or reuse acceptance requirement | Shared domain consistency matters, but a reusable platform/product boundary lacks a source driver. |

## 23. Backend Developer Takeaways

- Preserve the correct user's accepted information through access changes, retries and recovery. Session/logout/reset boundaries differ and already have precise policies.
- Keep Saveable, Recommendation-Ready and valid outfit decisions separate. Unknown evidence cannot pass a required check; personalization never overrides hard validity.
- Distinguish logical Wear actions, intentional historical reports and normalized ranking groups. Recompute survivor effects using original local grouping and absolute elapsed-time meaning.
- Treat Coverage and Multiplier as evidence-based assessments. Exact utility needs complete consistent sets; incomplete/unavailable/outdated is never a fabricated zero.
- Measure complete operations under existing SRS conditions. Keep nominal p95, concurrent integrity and restart recovery separate; do not introduce general API or Multiplier timing promises.
- Make required dependency outcomes and diagnostic evidence inspectable with minimal private content. Work with mobile/AI/QA on real integration, accessibility and validation evidence under the DoD.
- Bring feasibility and implementation concerns into later architecture/refinement work. Database, topology, communication, caching and JWT realization follow ASR → Architecture Decision Analysis → Selected Architecture Decisions → ADD/views → ADRs; backlog order and product-scope decisions remain with their responsible roles.

## 24. Traceability

Detailed scenario source lines and attribute drivers are the primary traceability. The following source map gives onboarding readers the full evidence scope; it does not create a second requirement hierarchy.

| Source | Role in this analysis |
| --- | --- |
| [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 16–19, 39–43 and 46 | Exact 14-QA taxonomy, significance from actual requirements, ASR/ADA/Selected Architecture Decisions/ADD boundaries, ownership, evolution and next-step order. |
| [Architecture reference](../../T%C3%A0i%20li%E1%BB%87u%20tham%20kh%E1%BA%A3o%20cho%20Architecture/14-Thu%E1%BB%99c-t%C3%ADnh-ch%E1%BA%A5t-l%C6%B0%E1%BB%A3ng.md), slides 4–16 and 18–22 | QA definition/classification, requirement/constraint distinctions, six-part scenarios and architecture reasoning categories; methodology only. |
| [BRD v0.3](../01-business/BRD.md) | BG-01–04; BR-001–024; CAP-01–10; personas, trust, scope, metrics, risks and dependencies. Highest-ranked drivers particularly reach BR-002/004/009/011/014–017/021–024 and MET-Q01–Q07. |
| [PRD v0.2](../02-product/PRD.md), Sections 21–29 and 34 | Complete FEAT/JRN experience, state/explanation/privacy/quality expectations and resolved OPQ-001–010. |
| [SRS v0.3](../03-requirements/SRS.md), Sections 2.3/2.4, 3–9 and 12 | Authoritative functional/data/interface/quality obligations, numerical measures, operating configuration, AI acceptance and failure boundaries. |
| [Business Rules v0.1.2](../03-requirements/business-rules.md), Sections 1.6, 2–15 | All 11 rule families: authority, applicable readiness, validity/ranking, Wear/time/effects, history, Coverage/Gaps, complete hypothetical utility and optional commercial guidance. |
| [Master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml) and all 23 [Use Case Specifications](../03-requirements/use-cases/) | Actors/goals, interaction and failure outcomes. UC-001–006 inform private access/context; UC-007–010 authority/current ownership; UC-011–018 advice and behavioral integrity; UC-019–023 explainable intelligence and optional navigation. |
| All 12 [Activity Diagrams](../03-requirements/activity-diagrams/) | AD-004/006/007/011/012/014/016/017/019/020/021/022 project established complex flows, especially dependency boundaries, accepted mutations, state truth and assessment exactness. |
| [Product Goal v0.1.1](../06-scrum/product-goal.md), Sections 3–8 | Trustworthy understanding, easier decisions, owned-garment value, actionable needs and informed additions; no new business KPI. |
| [Product Backlog v0.1](../06-scrum/product-backlog.md), all 41 PBIs | Whole MVP horizon and evidence routes. PBI-001–003/009/014/034–041 enable/protect the loop; owning product PBIs retain their applicable quality obligations. |
| [Story index](../06-scrum/stories/README.md) and all eight current stories | US-AUTH-001–005, US-GAR-001, US-WAR-001/002 provide concrete near-term access, manual entry, inspection, uncertainty and accessibility AC without narrowing the MVP horizon. |
| [Definition of Done v0.1](../06-scrum/definition-of-done.md), Sections 4–6/11 | Common and triggered completion conditions, actual integration/CI/test evidence, protected diagnostics and scope-aware quality application. |
| [Delivery Decisions v0.1](../06-scrum/delivery-decisions.md), DD-001–006 | Preserve PO backlog responsibility, current refinement/story boundaries, future Sprint planning and QA → ASR → architecture preparation sequence. |

**Source interpretation and issues.** No blocking contradiction in current quality obligations was found. FR-MET-004 contains an incomplete benchmark cross-reference, already noted in Product Backlog Section 11. Recognition/preview evidence uses explicit SRS Section 8.1/3.4.1; timing uses Section 12.3 and NFR-TEST-003. The editorial defect does not remove those acceptance criteria. Historical directions that conflict with normalized scope/thresholds remain historical under Section 21; no upstream artifact is repaired or overridden here.

Older artifacts' “next artifact” statements describe their own handoff point. Current progression follows Workflow Section 46 and DD-006: QA → ASR → ADA → explicit human selection → ADD/views → ADRs → detailed design and delivery preparation. Initial artifact-specific handoffs do not restart completed preparation. All sources remain drafts where so marked; resolved business/product decisions do not imply formal artifact approval.

## 25. Status

**Version 0.1.5 — Baseline Draft**, prepared for Architect/Tech Lead review with Developers, QA and Product. All 14 attributes are analyzed with discriminating significance; 17 concrete scenarios use the reference's six elements and current acceptance measures. Privacy and Accessibility retain their places within the established taxonomy. This QA analysis itself selects no architecture solution; the separate record linked below governs selections. ADD, the accepted Logical View and proposed Implementation View exist separately; Deployment/Data Views and detailed design remain downstream, and no upstream requirement, backlog/story boundary, estimate or Sprint commitment changes.

**Original v0.1 handoff:** formal ASR identification and architectural-driver selection (Workflow Section 17 and Section 46, Step 14). The preliminary candidates and source/scenario evidence above preserve that analysis stage.

**Current navigation:** [ASR](ASR.md) and [Architecture Decision Analysis](architecture-decision-analysis.md) drafts exist. [Selected Architecture Decisions](selected-architecture-decisions.md) records all ADA-001–018 as Selected by explicit human acceptance on 2026-10-10; the gate remains complete. [ADD](ADD.md) includes source scenarios and selected tactics. **Implementation View review → Deployment View** is the current handoff. [Logical View v0.1](views/logical-view.puml) is the human-accepted canonical decomposition; [Implementation View v0.1 — Baseline Draft](views/implementation-view.puml) proposes its source realization for review. Then follow Deployment View → Data View → ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation. This v0.1.5 navigation revision changes no QA classification, scenario body or acceptance measure.
