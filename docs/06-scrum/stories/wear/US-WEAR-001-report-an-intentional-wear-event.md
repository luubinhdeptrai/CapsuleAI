# US-WEAR-001 — Report a distinct intentional Wear Event

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-WEAR-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-021](../../product-backlog.md#pbi-021--report-intentional-wear-and-review-distinct-wear-events) |
| Domain | Wear History |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to explicitly report one wearing instance of a valid owned outfit, so that my recorded use reflects what I intended to report.

## 2. Value

Make reported use visible and trustworthy enough to support greater value from owned clothing.

## 3. Scope

Explicit current-time report, logical-intention identity, accepted event acknowledgment and safe retry, original event-local time/context and effective downstream meaning.

## 4. Preconditions

- An authorized outfit is identified; current ownership/readiness/validity is checked when a new report is accepted.

## 5. Acceptance Criteria

### AC-01 — Accept only eligible current wear

A successful explicit Wear This Today action on a currently valid owned outfit creates one distinct user-reported Wear Event. A removed/historical, hypothetical, unauthorized or invalid combination cannot become eligible through reporting.

### AC-02 — Preserve accepted time and context

Default time to accepted current action time; retain the absolute timestamp, associated timezone/UTC offset and original local date/time plus available outfit/occasion/recommendation context. Missing optional context is not invented.

### AC-03 — Deduplicate one intention

Retry, repeated delivery or reprocessing of the same logical initiation yields the same single accepted event, with no extra historical/measurement count. An uncertain response cannot prove acceptance or absence; history review/safe retry resolves it.

### AC-04 — Allow separate same-day intentions

A new explicit initiation creates a separate event even for the same exact outfit and original local day, and a different outfit report retains earlier reports. User+outfit+date alone is not duplicate identity.

### AC-05 — Acknowledge effects honestly

Accepted reports are available for history and applicable utilization/recency/evidence. Distinct history is retained while the same exact-outfit/original-day group supplies at most one +2 Wear preference increment before decay, anchored at its latest surviving accepted timestamp.

### AC-06 — Handle changed/failed acceptance

A changed outfit that no longer satisfies current validity gets no new accepted report. Known failed/rejected creation creates no event; existing reports/feedback/ownership stay unchanged and uncertainty is not called success.

### AC-07 — Avoid inferred or verified wear

Views, Like/Dislike, Shuffle, candidate evaluation or link opening do not create this event. A report is not verified physical wear or permission to override hard validity, share data or train MVP models.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-017.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Full history inspection is [US-WEAR-002](../wear/US-WEAR-002-review-distinct-wear-history.md); correction/removal are [US-WEAR-003](../wear/US-WEAR-003-correct-one-wear-event.md)/004; ranking application is [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md). No backdated creation, planned wear, automatic wear detection or technical deduplication window is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-021](../../product-backlog.md#pbi-021--report-intentional-wear-and-review-distinct-wear-events); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-03, BR-006, BR-011, CAP-06, CAP-07 |
| Product | [PRD](../../../02-product/PRD.md): JRN-05, FEAT-PERS-002, FEAT-ANL-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WEAR-001–FR-WEAR-005, FR-WEAR-009–FR-WEAR-011; DATA-WEAR-002, DATA-WEAR-003; DATA-INT-004; FR-MET-005; ERR-WEAR-001; COM-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-WEAR-001–BRULE-WEAR-003, BRULE-WEAR-006, BRULE-PERS-002, BRULE-PERS-005, BRULE-PERS-006 |
| Use Cases | [UC-014](../../../03-requirements/use-cases/UC-014-record-wear-event.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-014](../../../03-requirements/activity-diagrams/AD-014-record-wear-event.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Logical-action identity, accepted current time and same-day repeat rules are settled. Developers still need real acceptance/contention and client evidence; no implementation key or duplicate-time window is chosen here.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
