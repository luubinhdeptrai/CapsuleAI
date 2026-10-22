# US-QUAL-005 — Validate core-task usability with representative users

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Quality Story |
| Story ID | US-QUAL-005 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-037](../../product-backlog.md#pbi-037--validate-core-task-usability-with-representative-users) |
| Domain | Quality Verification |
| Theme | Quality / Security / Operations |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Evaluate whether target users understand and complete the established core tasks without avoidable facilitator assistance, then address evidenced barriers.

## 2. Value

Check that the target audience can understand wardrobe value and make decisions without avoidable assistance.

## 3. Scope

Six source core-task groups, representative participants, unaided completion, critical-blocker acceptance and supporting SUS evidence.

## 4. Preconditions

- Representative implemented outcomes are available for the evaluated task groups; research preparation can precede complete execution.

## 5. Acceptance Criteria

### AC-01 — Cover the prescribed task groups

Evaluate Register/Log In; Add Garment; outfit recommendations with Shuffle; Wear reporting and correction/removal; Coverage/Gaps; and Candidate/Multiplier. Record what was actually exercised, not a claim of unimplemented task coverage.

### AC-02 — Recruit the source sample

Use at least five users total across current personas where practical, including the primary audience; do not turn this into five per persona or invent participant answers.

### AC-03 — Measure unaided completion

For each evaluated representative core task, record actual completion without facilitator intervention and compare with the source target of at least 80%; report task-level denominators and barriers instead of hiding failures in an overall average.

### AC-04 — Assess blockers and SUS

Acceptance requires zero unresolved Critical usability blockers in evaluated core journeys. Record average SUS against the target of at least 68 as a validation aid, not a business KPI or invented certification.

### AC-05 — Separate preview and recognition

Where garment-assistance tasks use a preview, assess identifiable garment, preserved major regions, non-obstructive background and sufficient review information independently of category/color accuracy; no preview percentage is invented.

### AC-06 — Use findings responsibly

Retain privacy-safe participant/task conditions, observed limitations and evidence of required remediation/revalidation. Failed required behavior is not reported as successful usability and does not silently change product scope, thresholds, backlog order or Sprint assignments.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-006, PBI-019, PBI-022, PBI-023, PBI-026, PBI-030.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

This validates established requirements rather than discovering a replacement product. No growth/conversion target, additional task group, new study sample rule or separate test-plan artifact is required.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-037](../../product-backlog.md#pbi-037--validate-core-task-usability-with-representative-users); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-01, BG-02, BR-022, BR-023 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01–JRN-07; Section 25 |
| SRS | [SRS](../../../03-requirements/SRS.md): NFR-USE-001, NFR-USE-002; NFR-ACC-001, NFR-ACC-002; SRS Section 12.4 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-007, BRULE-HIST-001, BRULE-GAP-002 |
| Use Cases | [UC-001](../../../03-requirements/use-cases/UC-001-register-account.md); [UC-002](../../../03-requirements/use-cases/UC-002-log-in.md); [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); [UC-011](../../../03-requirements/use-cases/UC-011-get-outfit-recommendations.md); [UC-012](../../../03-requirements/use-cases/UC-012-shuffle-garment-slot.md); [UC-014](../../../03-requirements/use-cases/UC-014-record-wear-event.md); [UC-016](../../../03-requirements/use-cases/UC-016-correct-wear-event.md); [UC-017](../../../03-requirements/use-cases/UC-017-remove-wear-event.md); [UC-019](../../../03-requirements/use-cases/UC-019-view-wardrobe-coverage.md); [UC-020](../../../03-requirements/use-cases/UC-020-view-wardrobe-gaps.md); [UC-022](../../../03-requirements/use-cases/UC-022-evaluate-candidate-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Task groups, sample floor and thresholds are established. PO/QA/Developers arrange participants, task material and actual evaluated scope; no interview result or team agreement is claimed here.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
