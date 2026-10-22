# US-PERS-001 — Express, revise or clear outfit Like or Dislike

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-PERS-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-020](../../product-backlog.md#pbi-020--express-revise-or-clear-outfit-likedislike) |
| Domain | Behavioral Personalization |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to set, change or clear my preference for an exact outfit, so that future advice can use my current feedback.

## 2. Value

Give users control over meaningful preference evidence separately from reported wear.

## 3. Scope

Identified exact-target feedback, one effective state, accepted revision/clearing and safe failure/uncertainty without reported wear.

## 4. Preconditions

- The authorized recommendation/exact outfit target is identifiable; an existing feedback state is optional.

## 5. Acceptance Criteria

### AC-01 — Set the correct target state

Accepted Like or Dislike is shown for the identified exact garment-identity combination and user. Permuting the same constituents does not create a different outfit target; an unidentified/inaccessible target receives no unrelated feedback.

### AC-02 — Revise or clear

Changing Like to Dislike or vice versa leaves only the accepted effective state; explicit clearing leaves no effective Like/Dislike and updates applicable evidence without deleting Wear history.

### AC-03 — Preserve source signal meaning

Like is conceptual base +1 and Dislike -2 for that outfit target; state persists until changed/cleared/superseded by user action. Aging its ranking influence does not automatically clear the displayed state or permanently ban constituent garments/categories.

### AC-04 — Handle failure and uncertainty

Known failed/rejected updates retain the prior effective state. Uncertain outcomes remain unconfirmed for review/safe retry without simultaneously contradictory states or a false acceptance claim.

### AC-05 — Keep wear and validity separate

Preference actions/views create no Wear Event. Future influence remains soft and cannot admit an invalid outfit or guarantee changed advice when no useful valid alternative exists.

### AC-06 — Enforce private purpose

Only authorized affected-user access may inspect/change feedback; secrets and unrelated private contents are not exposed. Measurement failure does not undo accepted feedback or imply model-training consent.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-017.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Age-aware ranking is [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md); Wear reporting is [US-WEAR-001](../wear/US-WEAR-001-report-an-intentional-wear-event.md). No reason survey, permanent garment ban, similarity algorithm or feedback endpoint design is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-020](../../product-backlog.md#pbi-020--express-revise-or-clear-outfit-likedislike); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-011, BR-021, CAP-06 |
| Product | [PRD](../../../02-product/PRD.md): JRN-05, FEAT-PERS-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-PERS-001–FR-PERS-005, FR-PERS-013; DATA-OUT-001; DATA-WEAR-001; COM-002; FR-MET-007 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-PERS-001, BRULE-PERS-003, BRULE-PERS-006, BRULE-OUT-008 |
| Use Cases | [UC-013](../../../03-requirements/use-cases/UC-013-provide-outfit-feedback.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

No new feedback policy decision is needed. Target/lifecycle integration mechanics are selected-story JIT details; planned tests must distinguish accepted state from aged ranking influence.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
