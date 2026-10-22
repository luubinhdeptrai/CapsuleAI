# US-SHOP-003 — Choose optional external shopping navigation

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-SHOP-003 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-033](../../product-backlog.md#pbi-033--navigate-optionally-to-an-external-shopping-destination) |
| Domain | Strategic Shopping |
| Theme | Strategic Shopping |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Better-Informed Additions |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user interested in a candidate, I want to explicitly open an available external destination and return, so that I can explore it while retaining independent wardrobe advice.

## 2. Value

Let interested users act on credible candidate advice while retaining wardrobe value on return or failure.

## 3. Scope

Optional explicitly external handoff, decline/cancel, established versus uncertain opening, failure/return context and no inferred commerce/ownership.

## 4. Preconditions

- Authorized candidate advice and credible available destination information identify an offered action; external opening is not guaranteed.

## 5. Acceptance Criteria

### AC-01 — Require explicit external choice

Identify the action as external and navigate only when the user explicitly chooses an offered available destination. Candidate viewing or declining/canceling before handoff performs no external action or completed-opening claim.

### AC-02 — Distinguish navigation outcomes

When opening can be established, distinguish it from the attempt. Known handoff failure and uncertain opening are disclosed without claiming completed opening, purchase or sale; offer return/retry where appropriate.

### AC-03 — Retain advice after failure or return

Known absent/unavailable destination or normal return preserves otherwise valid candidate/gap/Multiplier/reasons/previews where available. Destination failure alone does not imply +0, candidate invalidity or a transaction outcome.

### AC-04 — Check changed context

Relevant assessment changes during navigation/return trigger reevaluation or outdated status; returning alone does not establish that prior counts/previews remain current.

### AC-05 — Avoid product side effects

Opening/returning creates no confirmed garment, Wear Event, purchase verification or retailer transaction in CapsuleAI. Core styling/insights remain usable without navigation, purchase or affiliate participation.

### AC-06 — Limit disclosure and measurement

Only chosen navigation is handed off; unrelated private wardrobe/profile/history is not disclosed. Measurement distinguishes attempt, established opening and uncertainty from candidate views/sales and cannot block otherwise usable navigation.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-029. Useful sequencing: PBI-030, PBI-031, PBI-032; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

UC-023 remains an optional extension of UC-021. No cart, checkout, order, fulfillment, verified sales attribution or automatic ingestion is added; commercial guidance/exact Multiplier is not a new mandatory link gate.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-033](../../product-backlog.md#pbi-033--navigate-optionally-to-an-external-shopping-destination); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-018, BR-019, BR-024, CAP-10 |
| Product | [PRD](../../../02-product/PRD.md): JRN-07, FEAT-SHOP-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-SHOP-005–FR-SHOP-008; FR-MET-003, FR-MET-007; SI-004; COM-003; DATA-INT-003; ERR-SHOP-003 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-SHOP-002, BRULE-SHOP-003, BRULE-AUTH-007 |
| Use Cases | [UC-023](../../../03-requirements/use-cases/UC-023-open-external-shopping-link.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q04 applies to credible destination information; actual platform handoff/observable opening evidence remains JIT. Do not require the client to claim an opening it cannot establish.

Question ownership and impact: [catalogue questions CAT-Q04](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
