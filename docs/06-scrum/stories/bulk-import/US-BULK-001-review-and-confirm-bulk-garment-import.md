# US-BULK-001 — Review and confirm bulk garment import

## Document Control

| Field | Value |
|---|---|
| Artifact | User Story |
| Story ID | US-BULK-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-048](../../product-backlog.md) — unranked refinement pool, Section 6.1 |
| Domain / Theme | bulk-import / Approved MVP extension |
| Product Goal Contribution | [Product Goal](../../product-goal.md): Reduce repeated entry work while keeping each wardrobe addition deliberate and trustworthy. |
| Revision note | 2026-11-09: source-linked Draft from approved merged PR #4 / DD-007; no Ready, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated CapsuleAI user, I want to select multiple photos and review a batch queue before saving accepted garments, so that I can reduce repetitive entry while retaining confirmation authority and truthful per-item results.

## 2. Value

Reduce repeated entry work while keeping each wardrobe addition deliberate and trustworthy.

## 3. Scope

Select multiple photos → queue → per-item review/correction → explicit confirmation/save. Preserve individual failed/uncertain outcomes, accepted results and item-level retry identity. Upload or AI completion alone is never ownership.

## 4. Preconditions

Authorized photo/wardrobe access and applicable selection permissions; batch policy/processing feasibility approved before Ready. These preconditions do not imply a new provider, UI tab, algorithm or product-policy default.

## 5. Acceptance Criteria

### AC-01 — Multiphoto queue correspondence and invalid item

Given the authorized source and applicable approved policy, the system shall allow selection of multiple garment photos into an identifiable import batch queue under applicable photo permissions and input-validation rules.

**Source / verification:** FR-BULK-001; verify multiphoto queue correspondence and invalid item.

### AC-02 — Mixed item outcomes and no upload-only ownership

Given the authorized source and applicable approved policy, the system shall show each queued item's pending, reviewable, accepted, failed or uncertain outcome independently, without representing an uploaded photo as a confirmed owned garment.

**Source / verification:** FR-BULK-002; verify mixed item outcomes and no upload-only ownership.

### AC-03 — Review/correction and unknown optional information

Given the authorized source and applicable approved policy, the system shall allow each reviewable item's applicable approved garment profile to be reviewed/corrected, supporting all 13 properties, optional distinct Materials/Styles, category applicability and user authority.

**Source / verification:** FR-BULK-003; verify review/correction and unknown optional information.

### AC-04 — Confirmed versus uploaded/proposed entries

Given the authorized source and applicable approved policy, the system shall save only entries explicitly confirmed by the user with Category and one Color, retaining all accepted optional information and truthful unevaluated recommendation eligibility; upload or AI completion alone shall not confirm an item.

**Source / verification:** FR-BULK-004; verify confirmed versus uploaded/proposed entries.

### AC-05 — Partial failure, uncertain acknowledgment and accepted survivors

Given the authorized source and applicable approved policy, the system shall distinguish each failed or uncertain item from accepted additions, preserve known accepted outcomes and offer correction/outcome review/safe retry without falsely claiming the whole batch succeeded.

**Source / verification:** FR-BULK-005; verify partial failure, uncertain acknowledgment and accepted survivors.

### AC-06 — Item retry versus separate intent; cross-account/cross-item checks

Given the authorized source and applicable approved policy, the system shall preserve one accepted garment per logical confirmed item action across retries/reprocessing, while permitting separately intentional additions, enforcing affected-user/media authorization and preventing cross-item association errors.

**Source / verification:** FR-BULK-006; verify item retry versus separate intent; cross-account/cross-item checks.

### AC-07 — Accepted, canceled and uncertain actions

Cancellation or known rejection/failure does not apply the requested mutation. An interrupted response does not prove success or failure: retain an unconfirmed outcome, allow outcome review/safe same-action retry and avoid duplicate accepted effects. A read, preview, Like, Follow, Save or draft copy alone never creates a confirmed Garment or Wear Event. An expired/revoked/wrong-account operation reveals no private state or unauthorized mutation. Optional measurement failure cannot reverse a valid accepted action.

## 6. Quality and Verification

Apply [shared quality/completion](../README.md#2-shared-quality-and-completion), the [Definition of Done](../../definition-of-done.md) and [Test Strategy](../../../07-testing/test-strategy.md) proportionally. Use known authorized/denied fixtures, missing information and dependency failure, plus real persistence/integration/client evidence when delivered. Vietnamese Android/iOS accessibility and truthful states apply. Protect secrets/private source data; no MVP personal-data training. New tests are planned obligations, not executed or implemented evidence.

## 7. Dependencies

PBI-012 photo-supported review and PBI-006 confirmed authority. AI assistance PBI-013 is conditional; queue topology, limits/resumption and save atomicity remain unselected. Collaborations do not establish priority or a serial schedule. Existing module/public-contract constraints remain authoritative.

## 8. Out of Scope

Comments, messaging, entire-wardrobe sharing, multithread chat history, personalized avatars, AR/3D, automatic ownership/wear/purchase verification and native commerce remain excluded. This story does not implement neighboring capabilities merely to expose an entry point. Full API/data/sequence/state/provider/transaction design follows actual Sprint selection only where justified by complexity and risk.

## 9. Traceability

| Source | References |
|---|---|
| Authority | [DD-007](../../delivery-decisions.md); merged PR #4 (`9e10cbb`) |
| Parent PBI | [PBI-048](../../product-backlog.md) — Section 6.1; no relative order selected |
| Business | [BRD](../../../01-business/BRD.md): BR-031, BR-021, BR-022; CAP-15; BG-01 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-BULK-001; JRN-12 |
| Software requirements | [SRS](../../../03-requirements/SRS.md): FR-BULK-001, FR-BULK-002, FR-BULK-003, FR-BULK-004, FR-BULK-005, FR-BULK-006; DATA-BULK-001; FR-PERS-014 where feedback applies; NFR-PRIV-001/002; DATA-INT-004; relevant ERR/LOC/UI requirements |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-BULK-001/002; BRULE-GAR-001/002/005/006/010; BRULE-AUTH-001/007 |
| Use Case | [UC-031](../../../03-requirements/use-cases/UC-031-review-and-confirm-bulk-import.md) |
| Activity Diagram | [AD-031](../../../03-requirements/activity-diagrams/AD-031-review-and-confirm-bulk-import.puml) |
| Architecture / verification | [ADD Section 15](../../../04-architecture/ADD.md#15-approved-pr-4-capability-impact); [Test Strategy](../../../07-testing/test-strategy.md) — implementation/test evidence pending selection |

## 10. Open Refinement Notes

MVP-Q08, MVP-Q09 are maintained in [PRD Section 34.2](../../../02-product/PRD.md#342-mvp-expansion-open-decisions). Resolve only relevant approved-scope details and feasibility before Ready. Options/recommendations are unapproved. No new numeric rule, source agreement, provider, retention period, estimate or Sprint commitment is inferred. JIT mechanics remain with selected work.

## 11. Status

**Draft.** PO/BA, Developers and QA review remains pending. Approved MVP scope does not mean this story is Ready, selected, implemented or Done.
