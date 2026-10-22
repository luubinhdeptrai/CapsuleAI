# US-COV-002 — Inspect per-need Coverage evidence and revision routes

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-COV-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-027](../../product-backlog.md#pbi-027--explore-per-need-coverage-evidence-and-useful-input-revisions) |
| Domain | Wardrobe Coverage |
| Theme | Wardrobe Intelligence |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Actionable Wardrobe Needs; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to inspect how a need is supported and what information I can revise, so that the contextual score leads to useful understanding.

## 2. Value

Turn the contextual score into understandable information about covered and underserved needs.

## 3. Scope

Read-only need detail from an established assessment, mapped occasions, priority, distinct support/limitations and existing revision handoffs.

## 4. Preconditions

- An authorized Coverage basis or its explicit limited/outdated state is identifiable; a positive score or gap is not required.

## 5. Acceptance Criteria

### AC-01 — Inspect the same assessment

A selected need shows its mapped occasions, priority, valid-distinct-outfit support and NeedCoverage from the same evaluated basis as the score, not a second inconsistent calculation or displayed daily subset.

### AC-02 — Explain all priority states

Show Not Relevant as weight zero and excluded from the score denominator/gap consideration; covered and underserved positive-priority needs retain their appropriate supporting evidence.

### AC-03 — Explain limitations

Insufficient/unavailable/outdated details preserve the parent assessment's actual state. A missing detail response is not zero support, a refreshed score or a proven purchase need.

### AC-04 — Offer supported revisions

Where relevant, offer existing style/common-priority, garment-readiness or location/context revision paths. Inspection creates no edit itself; accepted later changes require refreshed evidence or an outdated indication.

### AC-05 — Preserve useful nonshopping outcomes

A covered/no-important-gap need remains useful information; the user can return to Coverage/wardrobe without choosing a candidate or retailer. Viewing creates no ownership or Wear.

### AC-06 — Recover privately

On changed basis, failed retrieval or unusable/unauthorized access, explain and allow appropriate return/retry/authentication without exposing another account or treating old evidence as current.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-026.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Assessment/calculation is [US-COV-001](../coverage/US-COV-001-see-contextual-wardrobe-coverage.md); bottleneck/characteristic gap advice is [US-GAP-001](../gaps/US-GAP-001-understand-supported-capability-gaps.md); actual profile/garment/context edits remain their owning stories.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-027](../../product-backlog.md#pbi-027--explore-per-need-coverage-evidence-and-useful-input-revisions); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-014, BR-022, BR-024, CAP-07, CAP-08 |
| Product | [PRD](../../../02-product/PRD.md): JRN-06, FEAT-ANL-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-ANL-009, FR-ANL-010, FR-ANL-012, FR-ANL-014, FR-ANL-015; DATA-ANL-001; ERR-ANL-001, ERR-ANL-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-COV-001–BRULE-COV-006, BRULE-GAP-002 |
| Use Cases | [UC-019](../../../03-requirements/use-cases/UC-019-view-wardrobe-coverage.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-019](../../../03-requirements/activity-diagrams/AD-019-view-wardrobe-coverage.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Need meaning and revision responsibilities are established. Detail presentation/handoffs can be refined without creating a new profile-edit or calculation capability.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
