# US-ANL-001 — Understand reported wardrobe utilization

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-ANL-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-025](../../product-backlog.md#pbi-025--understand-reported-wardrobe-utilization-and-overlooked-items) |
| Domain | Wardrobe Utilization |
| Theme | Wardrobe Intelligence |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want explained frequency, recency and supported variety/overlooked-item insights, so that I can understand reported use of my owned clothing.

## 2. Value

Show useful patterns in owned-clothing use while acknowledging incomplete reporting.

## 3. Scope

Derived recorded-use indicators and evidence/limitations, separate current/historical ownership, effective changes and links to wardrobe/history.

## 4. Preconditions

- The user has authorized wardrobe/history access; having reported events is not required to open insights.

## 5. Acceptance Criteria

### AC-01 — Use effective reported evidence

Derive available frequency/recency and supported variety/overlooked indicators from accepted effective Wear Events, and explain the relevant evidence and measure basis. Reporting periods/denominators not fixed upstream require CAT-Q02 clarification before exact statistical AC can be completed.

### AC-02 — Count intentional reports honestly

Legitimate intentional same-outfit/day events count as separate reported events; logical-action retries do not inflate counts, and normalized/aged ranking increments are not substituted for historical frequency.

### AC-03 — Explain missing reporting

A garment with no recorded use is labeled that way rather than never physically worn. Empty history shows no Wear Events without invented frequency/diversity/recency measures or purchase pressure.

### AC-04 — Reflect corrected and removed evidence

Accepted event corrections/removals affect effective indicators and retain unrelated reports. Necessary unremoved history beyond 90-day ranking influence is not automatically truncated.

### AC-05 — Distinguish current and historical garments

Removed historical references are labeled Removed from wardrobe and do not become current inventory. Minimal retained snapshots do not justify retaining original removed images indefinitely.

### AC-06 — Recover and inspect safely

Unavailable/loading insight is not no-use or no-events evidence; offer retry/return to available wardrobe/history. Viewing changes no report or ownership and denies unauthorized access without exposing another user's data.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-021, PBI-007. Useful sequencing: PBI-022, PBI-023; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Chronological event inspection is [US-WEAR-002](../wear/US-WEAR-002-review-distinct-wear-history.md); ranking boost is [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md). No verified physical-wear score, new measurement period, forecasting, automatic wear detection or purchase obligation is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-025](../../product-backlog.md#pbi-025--understand-reported-wardrobe-utilization-and-overlooked-items); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-03, BR-006, BR-024, CAP-07 |
| Product | [PRD](../../../02-product/PRD.md): JRN-05, FEAT-ANL-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-ANL-001–FR-ANL-007; DATA-HIST-001, DATA-HIST-002; DATA-WEAR-004; DATA-RET-002; LOC-005 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-HIST-001–BRULE-HIST-003, BRULE-WEAR-006, BRULE-PERS-005 |
| Use Cases | [UC-018](../../../03-requirements/use-cases/UC-018-view-wardrobe-utilization.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q02 covers reporting periods, denominators and variety/overlooked indicator definitions; CAT-Q03 covers the optional boost's evidence anchor where reused. Do not infer an unrecorded-wear duration or creation-date origin from absent history.

Question ownership and impact: [catalogue questions CAT-Q02, CAT-Q03](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
