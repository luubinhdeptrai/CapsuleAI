# CapsuleAI — Focused Backlog Refinement Review

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

**Final scope clarification — 2026-11-05:** All 13 applicable creation/validation/persistence properties are now in US-GAR-001/PBI-006, with complete detail retrieval in US-WAR-002/PBI-007 and identifiable collection summaries in US-WAR-001. PBI-008 retains later editing only. Every source-listed vocabulary is approved; engine eligibility/composition policy is deferred for all categories and never blocks saving/browsing/detail. Existing story identities, parent PBIs, global order and four-story Sprint 2 selection remain.

**Current continuation — 2026-11-10:** Sections 1–9 preserve the dated initial refinement review. Section 10 records the approved PR #4 synchronization and current user-supplied Sprint 3 candidates; earlier foundation-only/readiness statements are authoring-time evidence, not a claim about current implemented scope.

## 1. Refinement Overview

| Field | Value |
| --- | --- |
| Date | 2026-10-21 |
| Repository snapshot | `D:\CapsuleAI`; branch `main`; inspected commit `94cc582602633763b02c87f8a154e088a8ce4f4e` |
| Horizon | PBI-001–PBI-007; detailed review of the eight existing stories under PBI-004–PBI-007 |
| Participants / decision status | Codex, acting as AI refinement facilitator/advisor through repository inspection. No PO or Developers meeting, consensus, approval or commitment is claimed. |
| Artifact status | Working review draft; advisory assessments only |
| Changes | Clarified foundation evidence, capability boundaries, acceptance wording, traceability and current handoff in existing Scrum artifacts. |
| Outstanding decisions | Q-01–Q-05 below: foundation closure, mobile feasibility, recovery delivery, privacy evidence and story selectability. |

The purpose is to prepare understandable, feasible candidates for future planning discussion. All 41 PBIs retain their global order, IDs, types, estimates, hard dependencies and refinement states. The eight stories retain their IDs, parents, AC identifiers and **Baseline Draft** status. No Sprint scope, estimate, new story or implementation is authorized.

### Sources inspected

| Source | Inspection relevance |
| --- | --- |
| [Canonical workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), complete; especially Sections 28–33, 38–40 and 46 | Refinement versus Planning/JIT design, ownership, evidence and artifact boundaries. |
| [Product Goal](product-goal.md), complete; [Product Backlog](product-backlog.md), complete | Goal contributions, all 41 ordered items, dependencies and refinement horizon. PBI-008–PBI-011 used only for adjacent boundaries. |
| [Definition of Done](definition-of-done.md); [Delivery Decisions](delivery-decisions.md) | Common/triggered completion criteria; DD-001–DD-006, especially existing decomposition, human accountability and no automatic Sprint selection. |
| [Story index](stories/README.md) and all eight linked story files, complete | User value, existing AC, alternatives, scope, dependencies and traceability. |
| [BRD](../01-business/BRD.md), goals/capabilities, garment authority, BR-002–BR-006 and BR-021–BR-023; [PRD](../02-product/PRD.md), Sections 6–8, 10.1, 12.2, 13–14, 23 and relevant quality expectations | Private participation and trustworthy wardrobe understanding; progressive entry; manual minimum; browsing/detail and honest states. |
| [SRS](../03-requirements/SRS.md), Sections 2, 3.1, manual/garment/wardrobe parts of 3.4–3.6, relevant data/interface/quality/localization/error and verification sections | Exact access/session policy, ownership, accepted-state integrity, supported vocabulary, mobile constraints and observable recovery. |
| [Business Rules](../03-requirements/business-rules.md), AUTH, relevant GAR and HIST rules | Access and revocation, confirmed/saveable versus ready, unknown information and reported-use meaning. |
| [UC-001](../03-requirements/use-cases/UC-001-register-account.md), [UC-002](../03-requirements/use-cases/UC-002-log-in.md), [UC-003](../03-requirements/use-cases/UC-003-log-out.md), [UC-004](../03-requirements/use-cases/UC-004-reset-password.md), [UC-007](../03-requirements/use-cases/UC-007-add-garment.md), [UC-008](../03-requirements/use-cases/UC-008-view-wardrobe.md); [AD-004](../03-requirements/activity-diagrams/AD-004-reset-password.puml), [AD-007](../03-requirements/activity-diagrams/AD-007-add-garment.puml) | Normal, alternative, cancellation, authorization and uncertain-outcome paths relevant to the horizon. |
| [Selected Architecture Decisions](../04-architecture/selected-architecture-decisions.md), relevant ADA analysis and [ADD](../04-architecture/ADD.md); [Logical](../04-architecture/views/logical-view.puml), [Implementation](../04-architecture/views/implementation-view.puml), [Deployment](../04-architecture/views/deployment-view.puml) and [Data](../04-architecture/views/data-view.puml) Views | Existing ownership, persistent session authority, integration/consistency constraints and intentionally unresolved mobile/provider details. No architecture reselection. |
| [Test Strategy](../07-testing/test-strategy.md), especially Sections 8–9, 11, 13–14, 17–19 and 21–22 | Appropriate evidence across policy, real persistence, security boundaries, integrations and supported clients. |
| [Backend README](../../backend/README.md), current production/test source, [POM](../../backend/pom.xml), Wrapper configuration, [Compose](../../compose.yaml), application configuration, foundation migration and [CI workflow](../../.github/workflows/backend-ci.yml) | Distinguish merged infrastructure and documented verification from absent product/mobile/AI implementation. Private `.env` contents were not needed. |

## 2. PBI Refinement Matrix

“Proposed State” preserves the existing backlog working state. Candidate assessments in Section 7 are separate advisory judgments.

| PBI | Initial State | Proposed State | Refinement Result | Open Questions |
| --- | --- | --- | --- | --- |
| PBI-001 | Refinable Soon | Unchanged — Refinable Soon | Credit the merged backend foundation; identify remaining client/integration readiness and a bounded completion/evidence decision. No duplicate infrastructure work or automatic Done. | Q-01, Q-02 |
| PBI-002 | Refinable Soon | Unchanged — Refinable Soon | Apply protection to each delivered operation from first use; distinguish immediate evidence from future lifecycle work and broader verification. Completion boundary needs review. | Q-04 |
| PBI-003 | Refinable Soon | Unchanged — Refinable Soon | Clarify Vietnamese entry/navigation, accessible states and Add/return routes; future destinations are not completed features. Backend CI supplies no mobile proof. | Q-02 |
| PBI-004 | Ready for Story Refinement | Unchanged — Ready for Story Refinement | Preserve four access outcomes; clarify normalized-email contention and session-authority denial. Expand primary traces to existing continuity/rotation/input rules. | Q-02, Q-04, Q-05 |
| PBI-005 | Ready for Story Refinement | Unchanged — Ready for Story Refinement | Preserve one complete recovery outcome and all-session effects; make delivery/opening feasibility visible without predesigning contracts. | Q-02, Q-03, Q-04, Q-05 |
| PBI-006 | Ready for Story Refinement | Unchanged — Ready for Story Refinement | Preserve one minimum-confirmed manual-add outcome; clarify same-action retries and accepted-but-unready meaning. AI, images and rich enrichment remain unnecessary. | Q-02, Q-04, Q-05 |
| PBI-007 | Ready for Story Refinement | Unchanged — Ready for Story Refinement | Retain browsing/detail as two outcomes; empty browsing needs no existing garment, and unavailable Wear evidence cannot gate detail or become zero use. | Q-02, Q-04, Q-05 |

## 3. User Story Refinement Matrix

| Story ID | Parent PBI | Findings | Improvements | Remaining Gaps |
| --- | --- | --- | --- | --- |
| US-AUTH-001 | PBI-004 | Coherent account-creation value; required/optional input, exact passwords, duplicate identity, cancellation and uncertainty already covered. | AC-06 explicitly covers concurrent equivalent-email submissions; add transport/accessibility and DoD/test evidence traces. | Mobile/private-entry evidence and Developers' selectability assessment: Q-02/Q-04/Q-05. |
| US-AUTH-002 | PBI-004 | Coherent credential access; wrong account, incomplete input, optional setup and uncertain login already covered. | Preserve AC; improve transport/accessibility/evidence trace and correct-account handoff test focus. | Shared mobile/privacy feasibility and sizing: Q-02/Q-04/Q-05. |
| US-AUTH-003 | PBI-004 | Continuity includes risk-sensitive rotation, absolute expiry, consumed-token reuse and interrupted responses. | AC-06 explicitly denies unusable-session access despite an unexpired token; link existing ADA-008 and verification obligations. | Developers must assess lifecycle/concurrency feasibility and size: Q-05; shared Q-02/Q-04. |
| US-AUTH-004 | PBI-004 | Current-session logout is distinct from separate-session preservation and completed reset. | AC-01 explicitly denies protected operations before token expiry after revocation; add FR-AUTH-009/error/transport and authority traces. | Shared mobile/privacy evidence and sizing: Q-02/Q-04/Q-05. |
| US-AUTH-005 | PBI-005 | Request-only work would not recover access; the existing story coherently includes validity/replacement, completion and return to login. | AC-05 makes revoked-session protected denial explicit; add existing authorization/disclosure/error and authority traces. | Delivery/opening feasibility Q-03; complexity/evidence Q-05; shared Q-02/Q-04. |
| US-GAR-001 | PBI-006 | Confirmation, minimum input, cancellation, denied access and uncertain save belong to one usable creation outcome. | AC-12 explicitly bounds repeated delivery/retry to one logical addition; add DATA-INT-001/004 and authorization/privacy traces. | Mobile acceptance/inspection and integrity evidence; Developers' sizing: Q-02/Q-04/Q-05. |
| US-WAR-001 | PBI-007 | Empty/populated value is coherent; loading/unavailable/unauthorized are already distinct from empty. | Preserve AC; add explicit security/privacy/transport/accessibility and completion/evidence trace; clarify fixture/handoff discussion. | Supported-client and cross-account evidence, sizing: Q-02/Q-04/Q-05. |
| US-WAR-002 | PBI-007 | Accepted accepted profile / unevaluated eligibility inspection remains useful without Wear reporting or rich fields; existing AC-06/07 correctly qualify use evidence. | Preserve AC; strengthen security/privacy/transport/accessibility and evidence trace; retain conditional use-information boundary. | Supported-client, current-ownership and return-path evidence, sizing: Q-02/Q-04/Q-05. |

## 4. Detailed Findings

### 4.1 Value and scope remain aligned

PBI-001–PBI-005 enable and protect participation in the Product Goal. PBI-006/PBI-007 deliver **Trustworthy Wardrobe Understanding**: confirmed possessions can be created and inspected before recommendation, AI or shopping capabilities exist. BR-021/BR-023 and CAP-01 protect entry; BG-01, BR-002/BR-004/BR-005 and CAP-03/CAP-04 anchor manual ownership and inspection.

The existing role/motivation statements and most AC are already observable and comprehensive. No unsupported normal, failure, cancellation or recovery flow was added. PBI-008 owns rich enrichment/editing; PBI-009 owns measurement; PBI-010/PBI-011 own preferences/location. None becomes a prerequisite to authenticated entry, minimum manual saving or basic inspection.

### 4.2 PBI-001: credit evidence without overclaiming completion

| Category | Evidence / refinement implication |
| --- | --- |
| Already established | `backend/pom.xml`, committed Wrapper, development Compose/configuration, bootstrap and six ownership package descriptions; foundation migration; smoke/dependency/PostgreSQL tests and CI report enforcement. Reuse this foundation. |
| Verification available | `backend/README.md`, Sections 11–13, documents 2 harness + 2 architecture + 1 database cases and the successful baseline main-branch CI run. The database case uses real PostgreSQL and checks migration execution. These are infrastructure checks, not product AC coverage. |
| Not established by that evidence | No product endpoints, account/session behavior, garment operations, mobile client or Python/provider integration is implemented. A future mobile/AI integration needs appropriate capability and evidence when relevant, not compulsory implementation of all runtimes now. |
| Ongoing responsibility | Changed-behavior automation, integration/security checks, review and inspectable CI evidence belong to each delivered story's DoD. They do not create an endlessly open foundation task or require a full-MVP test campaign before early value. |
| Other PBI ownership | PBI-003 owns shared mobile experience; PBI-002 and owning stories apply privacy; PBI-039 owns complete-loop regression and PBI-040 operational readiness. Do not transfer those whole outcomes into PBI-001. |
| Remaining team review | Q-01/Q-02: identify the remaining bounded foundation capability and evidence. README Section 9 also records a local-password fallback caveat; explicit private configuration is required by its setup instructions. The configuration was not changed or credited as production hardening. |

This refinement inspected current source and existing recorded evidence; it did not rerun builds, CI, mobile tests or provider checks. Neither the five smoke cases nor a merged infrastructure PR establishes PBI-001 as Done.

### 4.3 Capability dependencies and cross-client delivery

The hard-dependency semantics in `docs/06-scrum/product-backlog.md`, Sections 5–7, remain unchanged.

| Item | Actual hard capability input | Useful sequencing / boundary |
| --- | --- | --- |
| PBI-001/PBI-002/PBI-003 | None listed | Shared foundation/trust/usability work supports early increments; no new artificial chain is introduced. |
| PBI-004 | PBI-002 | PBI-003 supplies useful mobile entry. Existing-account login does not require rerunning registration; logout needs a current session, not every sibling story completed first. |
| PBI-005 | PBI-004 | Email delivery is an external business dependency. Existing accounts can recover; the normal login story supplies return to access. |
| PBI-006 | PBI-004 | PBI-007 offers useful post-save inspection. Creating an accepted minimum is not conditional on completing general browsing/detail or AI/photo/enrichment. |
| PBI-007 | PBI-004 | PBI-006 supplies useful populated examples/Add destination, not a hard gate to empty browsing. Detail needs an actual current owned garment. Wear reporting is conditional evidence, not a profile-inspection prerequisite. |

These are end-to-end user stories, not backend-task substitutes. Backend authorization tests alone cannot establish supported mobile behavior, accessible interaction or the complete user outcome (Test Strategy Section 13). The team must discuss usable handoffs and verification access under Q-02 without requiring a full future profile, weather or Wear feature.

### 4.4 Acceptance and traceability clarifications

Five existing criteria were clarified without adding or renumbering AC:

- `stories/auth/US-AUTH-001-register-account.md`, AC-06: concurrent equivalent-email registration must still produce at most one account. Sources: FR-AUTH-016, DATA-AUTH-002 and Test Strategy Section 8.
- `stories/auth/US-AUTH-003-resume-session.md`, AC-06; `stories/auth/US-AUTH-004-log-out.md`, AC-01; `stories/auth/US-AUTH-005-recover-account-access.md`, AC-05: persistent session/account authority must permit protected use in addition to token validity. Current logout, affected-session refresh reuse and completed-reset account-wide revocation retain distinct effects. Sources: FR-AUTH-007/009/014, NFR-SEC-002, BRULE-AUTH-006/007, selected ADA-008, ADD Section 9.1 and Test Strategy Section 9.
- `stories/garment/US-GAR-001-add-garment-manually.md`, AC-12: repeated delivery/retry of one logical addition cannot produce multiple accepted wardrobe effects. Sources: FR-AI-010, DATA-INT-004, COM-002 and AD-007. No retry key, transaction or deduplication design is prescribed.

Each story's Section 9 now links the existing DoD/Test Strategy and applicable missing transport, ownership/privacy or accessible-state references. These were obligations already expressed in source requirements and story behavior; they do not defer protection to PBI-041 or accessibility to PBI-036. The registration/login/browse/detail stories retain existing cancellation, invalid, empty/unavailable or uncertain-state criteria as applicable; no extra criteria were invented to enlarge those stories.

For later verification, use normalized-email/password boundary examples, two synthetic accounts and separate sessions, controlled expiry instants, imageless minimum-only garments and interrupted-action examples. Real session/persistence/security integration is necessary where mocks cannot establish the source obligation. Device/Vietnamese evidence applies to delivered interactions; future AI/performance/full-loop campaigns remain with their owning capabilities and triggered DoD criteria.

### 4.5 INVEST diagnostic and unresolved source matters

| Diagnostic | Finding across the eight stories |
| --- | --- |
| Independent | Each has a distinct user outcome with explicit capability inputs. Independence does not remove necessary authentication, owned-garment or external delivery inputs. No layer-based story split is justified. |
| Negotiable | Layout, technical collaboration and implementation detail remain negotiable within fixed source behavior. Password, expiry, revocation, user authority and privacy rules are not silently negotiable. |
| Valuable | Registration, credential access, continuity, logout, recovered access, confirmed addition, browsing and selected-garment understanding each provide a meaningful outcome. |
| Estimable | Behavior is detailed enough for Developers to investigate/supply estimates; no team estimate or feasibility agreement exists. Q-02/Q-03/Q-05 identify the material unknowns. |
| Small | Size is unproven for every story. Continuity and recovery carry particular lifecycle/concurrency/integration risk; manual acceptance has integrity risk. AC count alone does not justify splitting them. |
| Testable | Existing observable AC plus the clarifications above give source-linked outcomes. The runnable product/client/provider fixtures and evidence are not yet implemented; testability on paper is not executed verification. |

No blocking contradiction in the canonical product behavior was found within this horizon. The backlog's old “next: initial Stories/DoD/later architecture” navigation was stale and is now historical; the existing stories, architecture, Test Strategy and merged foundation are acknowledged. Older dated handoffs in other documents do not restart completed preparation. Those sources remain unchanged under this task's edit boundary.

Some UC specifications reference absent focused diagram files; their textual specifications and the existing master/AD sources remain usable. This editorial limitation does not require new diagrams or block the current refinement.

## 5. Proposed Story Changes

**Recommendation for team review:** retain the eight existing identities and DD-003 decomposition. No split, merge, re-ID, new story or layer-based PBI is proposed or applied. Scope and authority remain with the existing sources and owners.

If Developers' Q-05 assessment shows a story cannot form a feasible Sprint-sized outcome, bring a concrete alternative to PO/BA/team review with preserved references and an independently useful result. An email-request-only story, refresh-token-only task, or separate backend/database/UI “story” is not the default remedy. A bounded Spike is an option only for a demonstrated major unknown; none is created or selected here.

PO/team review is required for any proposed change to PBI-001/PBI-002 completion boundaries, ordering, scope or decomposition. Pure implementation detail remains Developers' responsibility inside selected-story JIT work; this review creates no extra PO sign-off gate for every task.

## 6. Open Questions for Product Owner and Team

The order below reflects potential effect on candidate selection. A conditional blocker applies only to affected work; it does not prevent discussion of the backlog or imply that the whole project is blocked.

### Q-02 — Supported mobile delivery and verification path

**Affected:** PBI-001/PBI-003 and all eight stories. **Owner:** Developers, with PO/BA on usable outcome boundaries.

What credible mobile delivery/contributor and Android/iOS verification path supports these end-to-end outcomes, including optional-setup entry, Add/acceptance, browsing/detail return and account isolation?

**Evidence / significance:** The current backend has no client implementation; Deployment View explicitly leaves the mobile framework open. BR-023, UI-001–UI-004, LOC-001 and NFR-ACC-001/002 require supported, Vietnamese, accessible interactions; Test Strategy Section 13 rejects backend-only evidence as complete journey proof.

**Recommended approach:** establish enough feasibility, available skills/device access and bounded handoff expectations for candidate discussion. Use lightweight investigation if needed; do not design all screens/contracts or choose a framework in this review. **Planning effect:** blocks selection of an affected mobile outcome if no feasible delivery/verification path can be established; not a demand to implement every mobile feature before Planning.

### Q-04 — First private-operation evidence and PBI-002 boundary

**Affected:** PBI-002; PBI-004–PBI-007 and all eight stories. **Owner:** shared PO/Developers discussion; Developers own verification approach.

Which first delivered protected operations and representative account/session scenarios will demonstrate the bounded PBI-002 outcome, and how will its completion boundary remain distinct from future feature-specific protections?

**Evidence / significance:** NFR-SEC-001 calls for zero successful unauthorized access in agreed scenarios; FR-AUTH-008/009, NFR-SEC-002/003, NFR-PRIV-001/002 and DoD common/triggered criteria apply immediately. Images/history and their removal/retention belong to later actual capabilities, not an invented up-front implementation campaign.

**Recommended approach:** agree relevant signed-out, wrong-account, switched-account, revoked-session and disclosure scenarios for the operations actually delivered; retain ongoing feature/lifecycle evidence in owning work. Do not waive a privacy rule or treat missing features as verified. **Planning effect:** clarification needed before calling PBI-002 complete; selection is blocked where a material protection/evidence risk is not understood, not by the absence of a full future-MVP matrix.

### Q-03 — Recovery delivery and opening feasibility

**Affected:** PBI-005 / US-AUTH-005. **Owner:** Developers; PO/BA clarify any discovered user-outcome conflict.

Can the team exercise necessary recovery email delivery and open/complete the issued interaction on the supported client, then return to credential access, including delivery failure and replaced/expired instructions?

**Evidence / significance:** SI-003, FR-AUTH-003/004/017/018, UC-004/AD-004 and selected ADA-006 require real boundary semantics; no recovery provider integration exists. Mocked delivery alone cannot establish the whole restored-access outcome.

**Recommended approach:** establish provider/test-destination access and enough opening/return feasibility; use controlled substitutes for failure permutations and identify substituted boundaries. Leave final provider contract, interaction transport and implementation to necessary JIT design. **Planning effect:** unresolved material feasibility blocks selecting this recovery outcome; it does not block independent access/manual-wardrobe discussions.

### Q-05 — Story size, risk and evidence feasibility

**Affected:** all eight stories, especially US-AUTH-003, US-AUTH-005 and US-GAR-001. **Owner:** Developers for technical feasibility/estimation; shared PO/BA discussion if a scope/decomposition proposal is needed.

Are the existing complete outcomes selectable at a responsible size once mobile/privacy/recovery feasibility is understood, or does a specific major unknown need bounded investigation?

**Evidence / significance:** All items remain Not Estimated; no team capacity or estimation evidence is recorded. Test Strategy Sections 8–9/14 require meaningful session/acceptance contention, controlled time and committed outcomes, while DD-003 preserves coherent user outcomes.

**Recommended approach:** discuss the actual candidate's risks and lowest useful verification levels; identify likely work, without final schemas, endpoints or full behavior diagrams. Retain the existing decomposition unless evidence justifies a reviewable alternative. **Planning effect:** unresolved major feasibility/size uncertainty blocks selecting the affected story; minor implementation detail is resolved JIT after selection. No estimate or Sprint commitment is invented here.

### Q-01 — Bounded PBI-001 completion after the merged foundation

**Affected:** PBI-001; coordination with PBI-003. **Owner:** PO for backlog outcome/scope, Developers for capability/evidence.

What remaining reproducible delivery/verification capability is actually needed to satisfy PBI-001's connected-mobile outcome, and which evidence will distinguish that finite completion from ongoing story testing?

**Evidence / significance:** Backend README Sections 11–15 and the current source establish infrastructure but no mobile/AI/product capability. PBI-001 names a broader delivery outcome; the DoD, PBI-003, PBI-039 and PBI-040 retain distinct responsibilities.

**Recommended approach:** credit existing work, identify only necessary remaining foundation gaps and agree a bounded completion/evidence statement in the existing backlog. If that requires changed scope/decomposition, propose it explicitly instead of silently closing the PBI or creating duplicate tasks. Future AI support is needed when relevant, not as an unconditional gate to manual wardrobe value. **Planning effect:** blocks a justified completion claim or selecting an unbounded remainder of PBI-001; does not itself require feature work to wait for full-MVP infrastructure.

## 7. Candidate Readiness Assessment

These are advisory discussion assessments, not state changes, formal Ready approvals or Sprint assignments.

| Candidate | Assessment | Basis / next clarification |
| --- | --- | --- |
| PBI-001 | Needs additional clarification | Remaining bounded capability and evidence: Q-01/Q-02. |
| PBI-002 | Needs additional clarification | First-operation evidence and finite PBI boundary: Q-04. |
| PBI-003 | Needs additional clarification | Mobile delivery and verification feasibility: Q-02. |
| PBI-004 | Needs additional clarification | Four behavior slices are clear; continuity risk/selectability and shared delivery evidence need Developers' input. |
| PBI-005 | Needs additional clarification | Recovery provider/opening feasibility and end-to-end size/evidence: Q-03/Q-05. |
| PBI-006/PBI-007 | Clear enough for planning discussion | Manual-add/browse/detail boundaries are clear; shared mobile/privacy/selectability questions still apply before selection. |
| US-AUTH-001, US-AUTH-002, US-AUTH-004 | Clear enough for planning discussion | Clear access outcomes and source policies; Q-02/Q-04/Q-05 remain for actual team feasibility/selection. |
| US-AUTH-003, US-AUTH-005 | Needs additional clarification | Lifecycle/concurrency/integration risk: Q-05; recovery also Q-03. |
| US-GAR-001, US-WAR-001, US-WAR-002 | Clear enough for planning discussion | Minimum ownership, browse and detail behavior is testable; Q-02/Q-04/Q-05 still apply to evidence and selection. |

No candidate is conclusively labeled **Blocked by an unresolved decision** from repository evidence alone. Section 6 defines when an unresolved feasibility/protection decision would block selection of affected work. This distinction avoids both an automatic Ready declaration and an unsupported project-wide blocker.

## 8. Recommended Next Actions

- **Product Owner decisions:** review this draft and the current order; resolve the PBI-001/PBI-002 outcome/evidence boundaries with Developers. Any change to scope, decomposition or ordering needs an explicit supported decision; none is assumed.
- **Development-team refinement:** establish mobile and recovery feasibility, inspect relevant quality risks and discuss candidate sizing/verification under Q-02–Q-05. Use a bounded investigation only if a demonstrated major unknown prevents responsible selection.
- **Requirement clarification:** verify the five AC wording changes against the linked sources. No new product policy answer is currently needed; escalate an actual contradiction to PO/BA rather than changing source rules or inventing a flow.
- **Preparation for Sprint Planning:** once the affected candidates are sufficiently understood, conduct Planning through the established process using actual team capacity and a coherent goal. This review supplies no Sprint Goal, scope or backlog. Necessary API/data/transaction/sequence/state detail follows selection, inside Sprint execution.

The next activity is **PO/Developers review and targeted closure of refinement questions**, then authorized Sprint Planning for sufficiently understood candidates. Do not restart the system-wide baseline or design the whole MVP.

## 9. Files Changed

| File | Reason |
| --- | --- |
| [product-backlog.md](product-backlog.md) | PBI-001–PBI-007 evidence/boundary/trace clarifications; current refinement/handoff navigation; v0.2, same states/order/estimates. |
| [US-AUTH-001-register-account.md](stories/auth/US-AUTH-001-register-account.md) | Concurrent normalized-email AC clarification, trace/evidence and open refinement notes. |
| [US-AUTH-002-log-in.md](stories/auth/US-AUTH-002-log-in.md) | Preserve AC; strengthen transport/accessibility/evidence trace and focused feasibility notes. |
| [US-AUTH-003-resume-session.md](stories/auth/US-AUTH-003-resume-session.md) | Unusable-authority denial clarification, existing architecture/evidence trace and lifecycle-risk notes. |
| [US-AUTH-004-log-out.md](stories/auth/US-AUTH-004-log-out.md) | Completed-revocation denial clarification, source/evidence trace and isolation notes. |
| [US-AUTH-005-recover-account-access.md](stories/auth/US-AUTH-005-recover-account-access.md) | All-session protected denial clarification, source/evidence trace and provider/selectability questions. |
| [US-GAR-001-add-garment-manually.md](stories/garment/US-GAR-001-add-garment-manually.md) | Same-logical-addition retry clarification, integrity/privacy traces and mobile/acceptance evidence notes. |
| [US-WAR-001-browse-current-wardrobe.md](stories/wardrobe/US-WAR-001-browse-current-wardrobe.md) | Preserve AC; strengthen ownership/privacy/evidence trace and browse/handoff discussion. |
| [US-WAR-002-inspect-owned-garment.md](stories/wardrobe/US-WAR-002-inspect-owned-garment.md) | Preserve AC; strengthen ownership/privacy/evidence trace and conditional use-information boundary. |
| [backlog-refinement-review.md](backlog-refinement-review.md) | This single new working review: findings, recommendations, open questions and advisory assessments. |

The eight story versions advance to v0.2 while retaining Baseline Draft status. Story identities/parents and index entries are unchanged, so `stories/README.md` requires no edit. No source requirement, architecture, DoD, delivery decision, code, configuration, Sprint artifact or lower-horizon PBI detail is changed. No commit, push or PR is created.

Repository validation passed: `git diff --check`; all 41 ordered backlog rows, states and dependency sections preserved; PBI-008–PBI-041 detail unchanged; all eight story identities/parents and AC identifier lists preserved. All 176 local links/anchors checked resolve, and referenced requirement IDs were checked against their canonical catalogues. Exactly these ten authorized refinement files are changed; existing files outside the allowed scope are unchanged.

## 10. PR #4 Synchronization and Sprint 3 Candidate Impact

### 10.1 Authority and Change Impact

The inspected branch was `sprint_03`, initially clean, at merge `9e10cbb574f57e2492d9c1c5fc415f505fb4735f` (PR #4). Its merged commits are `648638d` and `cd49c79`; the first-parent diff contains the eight authoritative files named below. PR #4 / active DD-007 is the approved later scope and feedback authority. Source approval is distinct from artifact Draft status, story readiness, Sprint commitment, working implementation and passing verification.

| Approved change | Source authority | Downstream impact / synchronization |
|---|---|---|
| Community inspiration and bounded actions | BRD Section 16; DD-007; BR-025/026 | FEAT-COM-001/002; FR-COM-001–009; BRULE-COM-001–003; UC/AD-024–026; PBI-042/043; US-COM-001–003; private/shared projection and creator lifecycle open. |
| Single-thread AI Chat | BRD Section 16; DD-007; BR-027 | FEAT-CHAT-001; FR-CHAT-001–007; BRULE-CHAT-001/002; UC/AD-027; PBI-044; US-CHAT-001; thread retention/context/integration open. |
| Generic-model Try-On | BRD Section 16 (including Save/Wear entry); DD-007; BR-028 | FEAT-TRY-001; FR-TRY-001–005; BRULE-TRY-001/002; UC/AD-028; PBI-045; US-TRY-001. Preserve current-valid-owned Wear rules; model/provider/lifecycle and unowned prerequisite UX remain open. |
| Internal catalog search and confirmed addition | BRD Section 16; DD-007; BR-029/030 | FEAT-CAT-001/002; FR-CAT-001–008; BRULE-CAT-001/002; UC/AD-029/030; PBI-046/047; US-CAT-001/002. US-WAR-003 remains owned search; catalog source/rights and allocation open. |
| Bulk photo import | BRD Section 16; DD-007; BR-031 | FEAT-BULK-001; FR-BULK-001–006; BRULE-BULK-001/002; UC/AD-031; PBI-048; US-BULK-001. Upload alone is not ownership; limits/processing/atomicity/resumption remain open. |
| Stored feedback/Wear without prescribed ranking | SRS Sections 3.10–3.12; BRULE-PERS-001–006; DD-007 | PRD, PBI-020–025/029 notes, US-PERS/Wear/Analytics/Measurement/Privacy, UC-011/013–018, AD-013/014/016/017, QA/ASR/ADA/ADD/views and Test Strategy updated. No fixed coefficients, ranking-decay bands, penalties, boost or outfit/day preference cap. |
| AI-only Dislike / typed target identity | FR-PERS-001/002/014; UC-013; BRULE-PERS-001 | Like on AI Today/Chat and Community; Dislike only on AI Today/Chat suggestions. Matching outfit constituents do not redirect feedback across source kinds. Revise/clear, rejection, uncertain-state and negative-test paths are explicit. |
| Garment authority and implemented eligibility | Source garment catalogue; PRD OPQ-013/015/016/017/018; current code/OpenAPI | Exactly 13 properties and all eight categories, full vocabularies, distinct Material/Style sets, approved Brand/Price validation, confirmation and `NOT_EVALUATED` remain. US-GAR-002 edits only; US-GAR-003 single-photo manual confirmation; US-GAR-004 AI proposals and US-BULK-001 retain separate scope. |
| Sharing/privacy contradictions | SRS introductory scope, NFR-PRIV-001 and Section 8.3; DD-007 | Permit only approved bounded shared projection/audience, never private Wardrobe inventory/source media/Price/profile/history. No comments, messaging, entire-wardrobe sharing, personal avatars or AR/3D. Existing no-training, removed-data deletion and measurement retention remain. |

### 10.2 Backend Impact Categories and Evidence

This is a static compatibility review against inspected source/contracts/tests, not a passing runtime-test report.

| Category | Finding | Evidence / action |
|---|---|---|
| A — implemented behavior incorrect | No specific PR #4 incompatibility was demonstrated in the implemented scope. No immediate Java/SQL/API correction is justified by this review. | The new matching/feed/chat/preview/catalog/batch modules are not implemented; removed ranking coefficients/decay are absent from their nonexistent engine, so no old engine is patched. This is not a guarantee that all backend code is defect-free. |
| B — approved future capability | Community, AI Chat, generic preview, catalog discovery/confirmed ingestion and batch import; typed feedback and future engine policy remain delivery work. | New source-linked Draft PBIs/Stories/UCs/Activities and architecture impact/questions are provided. No new endpoints, tables, provider or production feature is implemented. |
| C — existing behavior remains valid | Registration/login, JWT plus persistent usable session, refresh rotation, logout, recovery, confirmed garment creation, cursor wardrobe browsing and full garment detail. | Preserve current `docs/05-api/openapi.yaml`, all Java/tests/configuration/CI and applied Flyway V1–V8 unchanged. No contract or migration churn for documentation scope. |

Inspected examples include `GarmentProfile` (13 fields, Materials/Styles sets and category applicability), `Brand` (exact accepted text / 100 code points), `PurchasePrice` (exact decimal/currency, known zero, approved precision), `GarmentResponse` (`NOT_EVALUATED`, unavailable future Wear evidence), `WardrobeGarmentController` (owner-derived create/page/detail), V8 owned persistence and the property authority itself. `ApprovedGarmentVocabularyTest` compares every source vocabulary directly; `AddGarmentApiIT`, `CurrentWardrobePageIT`, `GarmentDetailApiIT` and `WardrobeAccessAfterPasswordResetIT` contain relevant contract, retry, owner and reset-preserves-wardrobe cases. Their existence is inspection evidence, not a new executed pass. Identity and Wardrobe contain implementation; Personal Context, Outfit Advice and Use, Wardrobe Intelligence and Product Measurement currently have package boundaries only.

### 10.3 Sprint 3 Candidate Assessment

The latest user instruction supplies exactly four candidates. No canonical Sprint 3 plan/selection artifact was found in the inspected checkout; a branch named `sprint_03` does not establish a Sprint Goal or selected capacity. Existing Sprint 1–2 scope, plans, decision notes and verification history are preserved. Four Sprint 2 guides receive only broken-navigation repairs; one pre-existing table row is split into its intended third cell without changing its text or meaning. The following is an advisory impact assessment, not a new Sprint plan.

| Candidate | Existing useful dependency / protected scope | Refinement / integration risk | PO / Developer review recommendation |
|---|---|---|---|
| US-GAR-002 | Implemented PBI-006 creation and current full detail establish the same 13-property model. This story edits an existing owned garment only. | Category changes must correct/clear inapplicable supplied values; optional clearing, Materials/Styles sets, Brand/Price, accepted-versus-uncertain edit and concurrent/stale selections need coherent JIT design. Future consumers refresh only when they exist. No readiness/compatibility gate. | Keep candidate scope; review edit conflict/retry and client evidence proportionally before selecting. No Community/Catalog/Bulk dependency or feature is added. |
| US-GAR-003 | Implemented confirmed manual addition; one-photo manual review, image permission/validation and private media association. | Media access, staged association/cleanup, failure/retry and actual supported device evidence are additional integration work. Future Chat/Bulk can reuse this boundary but do not enter the candidate. | Keep single-photo manual-confirmation scope; establish necessary media feasibility and lifecycle before selection, then design exact contracts/storage mechanics JIT. No AI extraction or batch queue is added. |
| US-PROF-001 | Existing authenticated identity; controlled private style/common-needs vocabulary and current request occasion remain distinct. | Persistence/authorized revision and limited/unset context require implementation; consumers may not yet exist. Community creator display is a separately approved projection, not permission to publish private preferences. | Keep private preference scope; verify useful standalone setup/revision and future public contract meaning without implementing Community/Chat. |
| US-MEAS-001 | Existing wardrobe outcomes supply early producers; measurement is purpose-limited and cannot block core acceptance. | Clarify actual accepted-action definitions, source hooks, safe retry and separate 90-day user-linked measurement lifecycle. New-feature producers remain future integration, not a dependency on their full implementation. | Keep minimum delivered-wardrobe evidence scope; resolve relevant CAT-Q07 interpretation and actual producer feasibility, with exact event/schema/lifecycle mechanics JIT. No new capability is selected to generate metrics. |

The expansion adds product-wide privacy/media/typed-target/integration considerations, but no evidence establishes a new hard dependency of these four candidates on completion of all new capabilities. Relative priority among the new PBIs is unassigned. Effort/capacity, estimates, dates and Sprint Goal are not known and are not fabricated; PO and Developers must assess actual readiness and capacity during authorized Planning. Any eventual scope negotiation must be explicit.

### 10.4 Traceability and Open Decisions

The seven new business outcomes extend existing BG-01–BG-04 without replacing prior IDs. Every new capability has product/system/rule ownership and linked Draft delivery/interaction coverage:

| Business / capability | Product / system | Rule / interaction | Backlog / stories | Architecture / verification |
|---|---|---|---|---|
| BR-025 / CAP-11 | FEAT-COM-001; FR-COM; DATA-COM-001 | BRULE-COM-002, BRULE-COM-003; UC/AD-024 | PBI-042; US-COM-001 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |
| BR-026 / CAP-11 | FEAT-COM-002; FR-COM; DATA-COM-001 | BRULE-COM-001, BRULE-COM-002; UC/AD-025, UC/AD-026 | PBI-043; US-COM-002, US-COM-003 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |
| BR-027 / CAP-12 | FEAT-CHAT-001; FR-CHAT; DATA-CHAT-001 | BRULE-CHAT-001, BRULE-CHAT-002; UC/AD-027 | PBI-044; US-CHAT-001 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |
| BR-028 / CAP-13 | FEAT-TRY-001; FR-TRY; DATA-TRY-001 | BRULE-TRY-001, BRULE-TRY-002; UC/AD-028 | PBI-045; US-TRY-001 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |
| BR-029 / CAP-14 | FEAT-CAT-001; FR-CAT; DATA-CAT-001 | BRULE-CAT-001; UC/AD-029 | PBI-046; US-CAT-001 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |
| BR-030 / CAP-14 | FEAT-CAT-002; FR-CAT; DATA-CAT-001 | BRULE-CAT-001, BRULE-CAT-002; UC/AD-030 | PBI-047; US-CAT-002 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |
| BR-031 / CAP-15 | FEAT-BULK-001; FR-BULK; DATA-BULK-001 | BRULE-BULK-001, BRULE-BULK-002; UC/AD-031 | PBI-048; US-BULK-001 with numbered source-linked AC | ADD Section 15 / four views; Test Strategy Section 15.1; implementation/tests pending actual selection. |

Unresolved **product-policy / allocation decisions** are recorded once with options, trade-offs, explicitly unapproved recommendations, owners and impacts in [PRD MVP-Q01–Q10](../02-product/PRD.md#342-mvp-expansion-open-decisions). These do not reopen the approved feature/action scope. Story/AC → requirements/rules/UC/Activity → architecture-impact/planned-verification links exist; implementation/test-result links for new features are intentionally pending, not missing delivered evidence. Exact endpoints, physical schemas, transactions and needed sequence/state diagrams are JIT implementation details after actual selection, not additional upfront approval paperwork.

### 10.5 Verification Record

Validation completed **2026-11-10** against the final source files. These checks establish documentation consistency, not complete feature implementation or a new backend/CI test pass.

| Check | Actual result |
|---|---|
| Change scope | 64 existing files modified; 25 new source files (8 Draft Stories, 8 Draft Use Cases, 9 Activity Diagrams). All changes are documentation/source diagrams. The unrelated concurrently added transactions/concurrency document is unchanged and excluded. |
| Actual Markdown navigation | 3413 local links/anchors checked across canonical source documentation, including new artifacts; **0 broken**. Fenced examples and inline code are excluded from navigable links. Missing focused projections now use the actual master; four historical guides have navigation-only repairs. |
| Markdown / references | **0** inconsistent table headers/column counts; **0** missing explicit scoped requirement/delivery references in changed/new active documents. |
| Stable source identity | Previous business/system/rule IDs preserved; every modified existing Story keeps its original numbered AC IDs. All original 41 ordered PBI ID/order pairs are unchanged; seven new PBIs remain unranked. |
| Current catalogue | 48 PBIs (47 mapped plus PBI-001 explained disposition); 56 Stories; 31 detailed Use Cases; 21 Activities. SRS has 306 distinct defining IDs, including 187 functional IDs; 76 Business Rules, including retained superseded history IDs. New Draft Stories contain 46 numbered source-linked AC. |
| Architectural preservation | All 18 selected options, selection bases and developer-impact statements are unchanged; AR-01–08 rule rows are unchanged. New allocation/provider/processing choices are explicitly pending, not selected. |
| Superseded policy scan | **0 positive active fixed ranking mandates** found in the targeted scan for contribution values, aging/decay, outfit/day preference caps and normalization. Remaining mentions explain negation/supersession, unrelated normalization, or preserved historical records. Community Dislike is explicitly rejected; AI Today/Chat Dislike is supported. |
| Formal UML | **26/26 PlantUML sources rendered to SVG successfully**, renderer exit 0, no syntax-error output; final source hashes match rendered inputs. Includes all new/affected Activities, master and four views. |
| Business / product Mermaid | **10/10 embedded Mermaid diagrams rendered successfully** (7 BRD, 3 PRD), exit 0, final source hashes match. No rendering issue remains. Generated images remain in temporary validation output, outside the repository. |
| Git / encoding | `git diff --check` passed; no replacement-character corruption in changed/new files. No commit, push, merge, rebase or reset was performed. |
| Implemented scope protection | All backend Java, tests, build/configuration, CI, OpenAPI and applied V1–V8 migrations are byte-identical to the initial snapshot. The garment-property authority is byte-identical. Historical Sprint scope/evidence is unchanged; only exact guide navigation/table-format repairs are allowed by the comparison. |
| Compilation / runtime tests | **Not run**: no backend/API/migration/configuration/test implementation changed. Relevant existing test source was inspected; no new passing compilation, runtime, mobile, provider, benchmark or CI result is claimed. |
| Remaining work | No documentation-validation blocker remains. MVP-Q01–Q10 and existing relevant catalogue questions remain refinement work before affected Draft stories can be Ready/selected. New feature implementation and actual test-result links remain future delivery work. |

### 10.6 Complete File Change Register

The following lists every file changed by this synchronization: **64 existing files modified and 25 new source artifacts**. Seven PBIs are entries in the existing backlog, not seven additional files. New files are eight Draft Stories, eight Draft Use Cases and nine Activity sources (the eight new goals plus typed feedback AD-013). Diagram renders and validation helpers stay outside the repository. An unrelated concurrently added transactions/concurrency learning document is preserved and excluded from this change count.

| Group | File | Change / reason |
|---|---|---|
| Architecture | [docs/04-architecture/ADD.md](../04-architecture/ADD.md) | Modified: Remove old ranking realization; register approved capability impact separately from unselected allocation/integration. |
| Architecture | [docs/04-architecture/ASR.md](../04-architecture/ASR.md) | Modified: Update existing privacy/accepted-state pressures and current navigation; preserve driver/ASR identities and unrelated measures. |
| Architecture | [docs/04-architecture/architecture-decision-analysis.md](../04-architecture/architecture-decision-analysis.md) | Modified: Correct obsolete ranking-cap realization claim and navigation; preserve analyzed/selected architectural options. |
| Architecture | [docs/04-architecture/quality-attribute-analysis.md](../04-architecture/quality-attribute-analysis.md) | Modified: Update affected scenario meaning/verification after scope and feedback change; preserve classifications and unrelated measures. |
| Architecture | [docs/04-architecture/selected-architecture-decisions.md](../04-architecture/selected-architecture-decisions.md) | Modified: Current implementation/navigation status only; every selected option, basis, developer impact and AR-01–08 rule remains. |
| Architecture | [docs/04-architecture/views/data-view.puml](../04-architecture/views/data-view.puml) | Modified: Show new required concerns as allocation pending, remove obsolete aging and preserve existing accepted module/runtime/store choices. |
| Architecture | [docs/04-architecture/views/deployment-view.puml](../04-architecture/views/deployment-view.puml) | Modified: Show new required concerns as allocation pending, remove obsolete aging and preserve existing accepted module/runtime/store choices. |
| Architecture | [docs/04-architecture/views/implementation-view.puml](../04-architecture/views/implementation-view.puml) | Modified: Show new required concerns as allocation pending, remove obsolete aging and preserve existing accepted module/runtime/store choices. |
| Architecture | [docs/04-architecture/views/logical-view.puml](../04-architecture/views/logical-view.puml) | Modified: Show new required concerns as allocation pending, remove obsolete aging and preserve existing accepted module/runtime/store choices. |
| Business | [docs/01-business/BRD.md](../01-business/BRD.md) | Modified: Elaborate BR-025–031 / CAP-11–15, conceptual concepts/context, bounded scope and stored-feedback meaning. |
| Business Rules | [docs/03-requirements/business-rules.md](../03-requirements/business-rules.md) | Modified: Eleven extension invariants; typed feedback, stored history/time and no prescribed ranking weights/decay/caps. |
| Process navigation | [Initial files/CapsuleAI_Scrum_Development_Workflow.md](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) | Modified: Dated current-position navigation only; no process/order/architecture/Sprint selection change. |
| Product | [docs/02-product/PRD.md](../02-product/PRD.md) | Modified: Seven new features, five journeys, states/privacy, corrected feedback criteria and sole MVP-Q01–Q10 decision table. |
| Product Backlog / Scrum | [docs/06-scrum/definition-of-done.md](definition-of-done.md) | Modified: Navigation/counts only; shared completion criteria are unchanged. |
| Product Backlog / Scrum | [docs/06-scrum/delivery-decisions.md](delivery-decisions.md) | Modified: Retain DD-007 authority; connect the new Draft chain and distinguish original handoffs from current implementation/refinement. |
| Product Backlog / Scrum | [docs/06-scrum/product-backlog.md](product-backlog.md) | Modified: Preserve Orders 1–41, correct affected outcomes/dependencies, add unranked PBI-042–048 and source coverage. |
| Product Backlog / Scrum | [docs/06-scrum/product-goal.md](product-goal.md) | Modified: Retain the single Product Goal; align feedback, extension traceability and current backlog navigation. |
| Requirements | [docs/03-requirements/SRS.md](../03-requirements/SRS.md) | Modified: 35 capability FRs plus FR-PERS-014 and five DATA requirements; repair scope/privacy, traceability and old policy references. |
| Sprint candidate review / change impact | [docs/06-scrum/backlog-refinement-review.md](backlog-refinement-review.md) | Modified: Change matrix, backend categories, four-candidate Sprint 3 advice, verification and complete change register. |
| Sprint historical navigation | [docs/06-scrum/sprints/sprint-02/implementation-guides/US-AUTH-005-guide.md](sprints/sprint-02/implementation-guides/US-AUTH-005-guide.md) | Modified: Only obsolete diagram/DoD links repaired; AUTH guide table formatting preserves existing wording. No historical scope/evidence change. |
| Sprint historical navigation | [docs/06-scrum/sprints/sprint-02/implementation-guides/US-GAR-001-guide.md](sprints/sprint-02/implementation-guides/US-GAR-001-guide.md) | Modified: Only obsolete diagram/DoD links repaired; AUTH guide table formatting preserves existing wording. No historical scope/evidence change. |
| Sprint historical navigation | [docs/06-scrum/sprints/sprint-02/implementation-guides/US-WAR-001-guide.md](sprints/sprint-02/implementation-guides/US-WAR-001-guide.md) | Modified: Only obsolete diagram/DoD links repaired; AUTH guide table formatting preserves existing wording. No historical scope/evidence change. |
| Sprint historical navigation | [docs/06-scrum/sprints/sprint-02/implementation-guides/US-WAR-002-guide.md](sprints/sprint-02/implementation-guides/US-WAR-002-guide.md) | Modified: Only obsolete diagram/DoD links repaired; AUTH guide table formatting preserves existing wording. No historical scope/evidence change. |
| Testing | [docs/07-testing/test-strategy.md](../07-testing/test-strategy.md) | Modified: Replace obsolete ranking fixtures; add proportional planned sharing/target/Chat/preview/catalog/batch regressions. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-013-provide-outfit-feedback.puml](../03-requirements/activity-diagrams/AD-013-provide-outfit-feedback.puml) | New: Different permitted feedback branches by typed source, rejection and accepted/uncertain state. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-014-record-wear-event.puml](../03-requirements/activity-diagrams/AD-014-record-wear-event.puml) | Modified: Remove ranking increment/decay effects while retaining logical action, original-time, correction/removal and survivor branches. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-016-correct-wear-event.puml](../03-requirements/activity-diagrams/AD-016-correct-wear-event.puml) | Modified: Remove ranking increment/decay effects while retaining logical action, original-time, correction/removal and survivor branches. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-017-remove-wear-event.puml](../03-requirements/activity-diagrams/AD-017-remove-wear-event.puml) | Modified: Remove ranking increment/decay effects while retaining logical action, original-time, correction/removal and survivor branches. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-024-browse-community-outfit-inspiration.puml](../03-requirements/activity-diagrams/AD-024-browse-community-outfit-inspiration.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-025-share-selected-outfit.puml](../03-requirements/activity-diagrams/AD-025-share-selected-outfit.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-026-duplicate-shared-outfit.puml](../03-requirements/activity-diagrams/AD-026-duplicate-shared-outfit.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-027-consult-ai-chat-stylist.puml](../03-requirements/activity-diagrams/AD-027-consult-ai-chat-stylist.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-028-preview-generic-model-try-on.puml](../03-requirements/activity-diagrams/AD-028-preview-generic-model-try-on.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-029-search-internal-garment-catalog.puml](../03-requirements/activity-diagrams/AD-029-search-internal-garment-catalog.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-030-confirm-catalog-item-into-closet.puml](../03-requirements/activity-diagrams/AD-030-confirm-catalog-item-into-closet.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/activity-diagrams/AD-031-review-and-confirm-bulk-import.puml](../03-requirements/activity-diagrams/AD-031-review-and-confirm-bulk-import.puml) | New: Formal activity for an approved new actor goal; source-linked decisions, recovery and open-policy boundaries. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-001-register-account.md](../03-requirements/use-cases/UC-001-register-account.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-002-log-in.md](../03-requirements/use-cases/UC-002-log-in.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-003-log-out.md](../03-requirements/use-cases/UC-003-log-out.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-004-reset-password.md](../03-requirements/use-cases/UC-004-reset-password.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-005-update-personalization-profile.md](../03-requirements/use-cases/UC-005-update-personalization-profile.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-006-set-location-context.md](../03-requirements/use-cases/UC-006-set-location-context.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-007-add-garment.md](../03-requirements/use-cases/UC-007-add-garment.md) | Modified: Clarify confirmed garment / distinct entry or owned-search boundaries; use actual master diagram where focused projection is absent. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-008-view-wardrobe.md](../03-requirements/use-cases/UC-008-view-wardrobe.md) | Modified: Clarify confirmed garment / distinct entry or owned-search boundaries; use actual master diagram where focused projection is absent. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-009-edit-garment.md](../03-requirements/use-cases/UC-009-edit-garment.md) | Modified: Clarify confirmed garment / distinct entry or owned-search boundaries; use actual master diagram where focused projection is absent. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-010-remove-garment.md](../03-requirements/use-cases/UC-010-remove-garment.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-011-get-outfit-recommendations.md](../03-requirements/use-cases/UC-011-get-outfit-recommendations.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-013-provide-outfit-feedback.md](../03-requirements/use-cases/UC-013-provide-outfit-feedback.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-014-record-wear-event.md](../03-requirements/use-cases/UC-014-record-wear-event.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-015-view-wear-history.md](../03-requirements/use-cases/UC-015-view-wear-history.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-016-correct-wear-event.md](../03-requirements/use-cases/UC-016-correct-wear-event.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-017-remove-wear-event.md](../03-requirements/use-cases/UC-017-remove-wear-event.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-018-view-wardrobe-utilization.md](../03-requirements/use-cases/UC-018-view-wardrobe-utilization.md) | Modified: Propagate supported feedback/stored Wear meaning, original-local history, removal and source links without obsolete ranking mandates. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-021-view-shopping-recommendations.md](../03-requirements/use-cases/UC-021-view-shopping-recommendations.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-023-open-external-shopping-link.md](../03-requirements/use-cases/UC-023-open-external-shopping-link.md) | Modified: Replace missing duplicate focused-diagram navigation with the actual existing master Use Case Diagram. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-024-browse-community-outfit-inspiration.md](../03-requirements/use-cases/UC-024-browse-community-outfit-inspiration.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-025-share-selected-outfit.md](../03-requirements/use-cases/UC-025-share-selected-outfit.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-026-duplicate-shared-outfit.md](../03-requirements/use-cases/UC-026-duplicate-shared-outfit.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-027-consult-ai-chat-stylist.md](../03-requirements/use-cases/UC-027-consult-ai-chat-stylist.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-028-preview-generic-model-try-on.md](../03-requirements/use-cases/UC-028-preview-generic-model-try-on.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-029-search-internal-garment-catalog.md](../03-requirements/use-cases/UC-029-search-internal-garment-catalog.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-030-confirm-catalog-item-into-closet.md](../03-requirements/use-cases/UC-030-confirm-catalog-item-into-closet.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/UC-031-review-and-confirm-bulk-import.md](../03-requirements/use-cases/UC-031-review-and-confirm-bulk-import.md) | New: Draft independent user-goal specification with alternatives, failures and requirement/story/diagram traceability. |
| Use Cases / Diagrams | [docs/03-requirements/use-cases/use-case-diagram.puml](../03-requirements/use-cases/use-case-diagram.puml) | Modified: Add eight independent actor goals with user associations; preserve valid existing UML extension and avoid invented include/extend links. |
| User Stories | [docs/06-scrum/stories/README.md](stories/README.md) | Modified: 56-story / 48-PBI coverage, new Draft links and retirement of obsolete CAT-Q03 boost question. |
| User Stories | [docs/06-scrum/stories/analytics/US-ANL-001-understand-reported-wardrobe-utilization.md](stories/analytics/US-ANL-001-understand-reported-wardrobe-utilization.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/bulk-import/US-BULK-001-review-and-confirm-bulk-garment-import.md](stories/bulk-import/US-BULK-001-review-and-confirm-bulk-garment-import.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/catalog/US-CAT-001-search-internal-garment-catalog.md](stories/catalog/US-CAT-001-search-internal-garment-catalog.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/catalog/US-CAT-002-confirm-catalog-item-into-owned-closet.md](stories/catalog/US-CAT-002-confirm-catalog-item-into-owned-closet.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/chat/US-CHAT-001-consult-single-thread-ai-chat-stylist.md](stories/chat/US-CHAT-001-consult-single-thread-ai-chat-stylist.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/community/US-COM-001-browse-and-interact-with-outfit-inspiration.md](stories/community/US-COM-001-browse-and-interact-with-outfit-inspiration.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/community/US-COM-002-explicitly-share-a-selected-outfit.md](stories/community/US-COM-002-explicitly-share-a-selected-outfit.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/community/US-COM-003-duplicate-shared-inspiration-as-own-outfit-draft.md](stories/community/US-COM-003-duplicate-shared-inspiration-as-own-outfit-draft.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/garment/US-GAR-002-correct-and-enrich-garment-information.md](stories/garment/US-GAR-002-correct-and-enrich-garment-information.md) | Modified: Preserve all existing AC IDs; edits-only scope, multiple Styles, approved Brand/Price, NOT_EVALUATED and corrected sources. |
| User Stories | [docs/06-scrum/stories/garment/US-GAR-003-add-photo-supported-garment.md](stories/garment/US-GAR-003-add-photo-supported-garment.md) | Modified: Preserve all AC IDs; single-photo manual confirmation, 13-property authority and NOT_EVALUATED; separate AI/Bulk. |
| User Stories | [docs/06-scrum/stories/garment/US-GAR-004-review-ai-assisted-garment-proposals.md](stories/garment/US-GAR-004-review-ai-assisted-garment-proposals.md) | Modified: Keep existing outcome scope; clarify new-capability boundaries, source meaning or minimum delivered-producer evidence. |
| User Stories | [docs/06-scrum/stories/measurement/US-MEAS-001-capture-minimum-wardrobe-use-evidence.md](stories/measurement/US-MEAS-001-capture-minimum-wardrobe-use-evidence.md) | Modified: Keep existing outcome scope; clarify new-capability boundaries, source meaning or minimum delivered-producer evidence. |
| User Stories | [docs/06-scrum/stories/measurement/US-MEAS-002-assemble-product-goal-value-loop-evidence.md](stories/measurement/US-MEAS-002-assemble-product-goal-value-loop-evidence.md) | Modified: Keep existing outcome scope; clarify new-capability boundaries, source meaning or minimum delivered-producer evidence. |
| User Stories | [docs/06-scrum/stories/personalization/US-PERS-001-express-revise-or-clear-outfit-feedback.md](stories/personalization/US-PERS-001-express-revise-or-clear-outfit-feedback.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md](stories/personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/privacy/US-PRIV-002-verify-mvp-authorization-privacy-and-retention.md](stories/privacy/US-PRIV-002-verify-mvp-authorization-privacy-and-retention.md) | Modified: Permit only approved sharing projection; preserve private data, no training and separate functional/measurement lifecycle. |
| User Stories | [docs/06-scrum/stories/profile/US-PROF-001-set-and-revise-style-and-common-needs.md](stories/profile/US-PROF-001-set-and-revise-style-and-common-needs.md) | Modified: Keep existing outcome scope; clarify new-capability boundaries, source meaning or minimum delivered-producer evidence. |
| User Stories | [docs/06-scrum/stories/quality/US-QUAL-008-verify-connected-domain-value-loop.md](stories/quality/US-QUAL-008-verify-connected-domain-value-loop.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/try-on/US-TRY-001-preview-selected-look-on-generic-model.md](stories/try-on/US-TRY-001-preview-selected-look-on-generic-model.md) | New: Draft cohesive user outcome, numbered source-linked AC, parent PBI, dependencies, exclusions and unresolved refinement. |
| User Stories | [docs/06-scrum/stories/wardrobe/US-WAR-003-find-garments-with-search-and-filters.md](stories/wardrobe/US-WAR-003-find-garments-with-search-and-filters.md) | Modified: Keep existing outcome scope; clarify new-capability boundaries, source meaning or minimum delivered-producer evidence. |
| User Stories | [docs/06-scrum/stories/wear/US-WEAR-001-report-an-intentional-wear-event.md](stories/wear/US-WEAR-001-report-an-intentional-wear-event.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/wear/US-WEAR-002-review-distinct-wear-history.md](stories/wear/US-WEAR-002-review-distinct-wear-history.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/wear/US-WEAR-003-correct-one-wear-event.md](stories/wear/US-WEAR-003-correct-one-wear-event.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |
| User Stories | [docs/06-scrum/stories/wear/US-WEAR-004-withdraw-one-wear-event.md](stories/wear/US-WEAR-004-withdraw-one-wear-event.md) | Modified: Retain Story/AC identity; replace superseded ranking policy with typed stored feedback, distinct event/history and safe mutation meaning. |

### 10.7 Final Assessment and Next Refinement

**Repository documentation is synchronized with approved PR #4 scope.** Source-linked coverage exists across business/product/system/rule/interaction/backlog/story/AC/architecture-impact/planned-verification layers. Prior stable IDs, original ordered PBI positions, implemented contracts, selected architecture and historical Sprint scope remain intact. No code, API, migration, new provider/owner choice, backlog priority, Sprint estimate/date/Goal or new implementation scope is selected by this documentation task.

The new features are **Draft requirements, not implemented software or Ready stories**. MVP-Q01–Q10 remain explicitly unresolved product/feasibility/allocation questions for their owners, with unapproved recommendations. Resolve only relevant questions before affected selection, then use selected-story JIT detailed design and automated test/implementation. The four user-supplied Sprint 3 candidates retain their existing scope; implementation/test-result links for new features remain pending actual delivery, not fabricated evidence.
