# US-TRY-001 — Preview a selected look on a generic model

## Document Control

| Field | Value |
|---|---|
| Artifact | User Story |
| Story ID | US-TRY-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-045](../../product-backlog.md) — unranked refinement pool, Section 6.1 |
| Domain / Theme | try-on / Approved MVP extension |
| Product Goal Contribution | [Product Goal](../../product-goal.md): Inspect an illustration of the selected look before a separate Save or Wear action. |
| Revision note | 2026-11-09: source-linked Draft from approved merged PR #4 / DD-007; no Ready, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated CapsuleAI user, I want to preview an identified outfit on a generic model and choose Save look or Wear Today, so that I can inspect inspiration before a separate action while keeping ownership and reported wear truthful.

## 2. Value

Inspect an illustration of the selected look before a separate Save or Wear action.

## 3. Scope

Generic-model result tied to the selection; Save look and Wear Today entry, plus unavailable/failed recovery. Wear still validates current owned outfit and one logical action; no selfie avatar or AR/3D.

## 4. Preconditions

An accessible identified selected outfit; preview method, acceptable fidelity and purpose-minimal inputs are approved before Ready. These preconditions do not imply a new provider, UI tab, algorithm or product-policy default.

## 5. Acceptance Criteria

### AC-01 — Owned and shared selection; excluded modes absent

Given the authorized source and applicable approved policy, the system shall accept an identified accessible selected outfit as generic-model preview input, preserving whether constituents are owned or unowned; no selfie/personal-avatar input or AR/3D simulation is required or introduced.

**Source / verification:** FR-TRY-001; verify owned and shared selection; excluded modes absent.

### AC-02 — Selection change and result basis correspondence

Given the authorized source and applicable approved policy, the system shall associate an available generic-model result with the outfit actually previewed, distinguish it from a current suitability/fit guarantee and not present an older selection's preview as the new selection.

**Source / verification:** FR-TRY-002; verify selection change and result basis correspondence.

### AC-03 — Save from preview; no Wear/ownership side effect

Given the authorized source and applicable approved policy, the system shall offer Save look from an available preview as a separate explicit action under the same saved-look semantics as FR-COM-006, without creating a Wear Event or owned garment.

**Source / verification:** FR-TRY-003; verify Save from preview; no Wear/ownership side effect.

### AC-04 — Owned report versus unowned/invalid denial; retry versus new intention

Given the authorized source and applicable approved policy, the system shall offer Wear Today entry from preview and accept a report only after existing current-valid-owned-outfit requirements FR-WEAR-001/009/011 are satisfied; unowned/shared/candidate looks shall not bypass those rules or silently create ownership.

**Source / verification:** FR-TRY-004; verify owned report versus unowned/invalid denial; retry versus new intention.

### AC-05 — Failure/retry and independent action outcome

Given the authorized source and applicable approved policy, the system shall distinguish preview pending, failed, unavailable and available results, retaining the selected-look context for retry/return and not fabricating a successful preview or reversing independent accepted actions.

**Source / verification:** FR-TRY-005; verify failure/retry and independent action outcome.

### AC-06 — Accepted, canceled and uncertain actions

Cancellation or known rejection/failure does not apply the requested mutation. An interrupted response does not prove success or failure: retain an unconfirmed outcome, allow outcome review/safe same-action retry and avoid duplicate accepted effects. A read, preview, Like, Follow, Save or draft copy alone never creates a confirmed Garment or Wear Event. An expired/revoked/wrong-account operation reveals no private state or unauthorized mutation. Optional measurement failure cannot reverse a valid accepted action.

## 6. Quality and Verification

Apply [shared quality/completion](../README.md#2-shared-quality-and-completion), the [Definition of Done](../../definition-of-done.md) and [Test Strategy](../../../07-testing/test-strategy.md) proportionally. Use known authorized/denied fixtures, missing information and dependency failure, plus real persistence/integration/client evidence when delivered. Vietnamese Android/iOS accessibility and truthful states apply. Protect secrets/private source data; no MVP personal-data training. New tests are planned obligations, not executed or implemented evidence.

## 7. Dependencies

Identifiable selected look from an available source and preview feasibility. Saved-look policy and PBI-021 collaborate on Save and current-valid-owned Wear; unowned preview itself does not require fabricated ownership. Collaborations do not establish priority or a serial schedule. Existing module/public-contract constraints remain authoritative.

## 8. Out of Scope

Comments, messaging, entire-wardrobe sharing, multithread chat history, personalized avatars, AR/3D, automatic ownership/wear/purchase verification and native commerce remain excluded. This story does not implement neighboring capabilities merely to expose an entry point. Full API/data/sequence/state/provider/transaction design follows actual Sprint selection only where justified by complexity and risk.

## 9. Traceability

| Source | References |
|---|---|
| Authority | [DD-007](../../delivery-decisions.md); merged PR #4 (`9e10cbb`) |
| Parent PBI | [PBI-045](../../product-backlog.md) — Section 6.1; no relative order selected |
| Business | [BRD](../../../01-business/BRD.md): BR-028, BR-021, BR-022; CAP-13; BG-02, BG-03 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-TRY-001; JRN-10 |
| Software requirements | [SRS](../../../03-requirements/SRS.md): FR-TRY-001, FR-TRY-002, FR-TRY-003, FR-TRY-004, FR-TRY-005; DATA-TRY-001; FR-PERS-014 where feedback applies; NFR-PRIV-001/002; DATA-INT-004; relevant ERR/LOC/UI requirements |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-TRY-001/002; BRULE-COM-002; BRULE-WEAR-001/002; BRULE-AUTH-001/007 |
| Use Case | [UC-028](../../../03-requirements/use-cases/UC-028-preview-generic-model-try-on.md) |
| Activity Diagram | [AD-028](../../../03-requirements/activity-diagrams/AD-028-preview-generic-model-try-on.puml) |
| Architecture / verification | [ADD Section 15](../../../04-architecture/ADD.md#15-approved-pr-4-capability-impact); [Test Strategy](../../../07-testing/test-strategy.md) — implementation/test evidence pending selection |

## 10. Open Refinement Notes

MVP-Q02, MVP-Q04, MVP-Q05, MVP-Q09 are maintained in [PRD Section 34.2](../../../02-product/PRD.md#342-mvp-expansion-open-decisions). Resolve only relevant approved-scope details and feasibility before Ready. Options/recommendations are unapproved. No new numeric rule, source agreement, provider, retention period, estimate or Sprint commitment is inferred. JIT mechanics remain with selected work.

## 11. Status

**Draft.** PO/BA, Developers and QA review remains pending. Approved MVP scope does not mean this story is Ready, selected, implemented or Done.
