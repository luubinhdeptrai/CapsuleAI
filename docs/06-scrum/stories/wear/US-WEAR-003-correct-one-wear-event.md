# US-WEAR-003 — Correct one Wear Event within its original local day

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-WEAR-003 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-022](../../product-backlog.md#pbi-022--correct-one-reported-wear-event-within-its-original-local-day) |
| Domain | Wear History |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to correct one reported event's supported information, so that my history is accurate without changing unrelated reports.

## 2. Value

Repair inaccurate reported use without changing unrelated history.

## 3. Scope

Individual applicable outfit/context/local-time correction, original-day/non-future bounds and coherent effective history/utilization/evidence.

## 4. Preconditions

- The user has authorized access to an identified existing effective report; changed/removed targets are handled.

## 5. Acceptance Criteria

### AC-01 — Target one existing report

Show the selected event's accepted outfit/context and original event-local time. A supported correction changes only intended applicable fields on that event, creates no new report and restores no removed garment ownership. The supported replacement-outfit choices require CAT-Q06 clarification.

### AC-02 — Validate corrected time

Accept a corrected local time only when it is not future and stays within the original event-local calendar day. Future or cross-original-day proposals are rejected with correction guidance; travel/current device timezone does not relax that boundary.

### AC-03 — Preserve unaffected meaning

An outfit-only/context-only correction retains unchanged time information; accepted time correction updates the corresponding absolute instant while preserving original local-day meaning and unrelated events.

### AC-04 — Recompute effective consequences

Accepted correction updates history/utilization and affected exact-outfit/original-day memberships, latest surviving accepted absolute anchors, elapsed aging/decay and exact-outfit recency. A moved latest timestamp can move the group anchor; normalization remains distinct from event identity.

### AC-05 — Cancel or recover safely

Canceling pending correction or known invalid/rejected/failed work retains the prior event/evidence. An uncertain result requires outcome review/safe retry and is neither accepted nor definitely unchanged solely from interruption.

### AC-06 — Handle changed target and access

A removed/changed or unauthorized target receives no correction to another record. Unusable access pauses protected use, retains accepted data and offers the existing authentication/history return paths.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-021.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

New reporting is [US-WEAR-001](../wear/US-WEAR-001-report-an-intentional-wear-event.md); individual withdrawal [US-WEAR-004](../wear/US-WEAR-004-withdraw-one-wear-event.md); ranking application [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md). No cross-day relocation, backdated new creation, bulk edit or ownership restoration is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-022](../../product-backlog.md#pbi-022--correct-one-reported-wear-event-within-its-original-local-day); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-006, BR-011, CAP-06, CAP-07 |
| Product | [PRD](../../../02-product/PRD.md): JRN-05, FEAT-PERS-002, FEAT-ANL-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WEAR-006, FR-WEAR-010, FR-WEAR-012; FR-ANL-007; DATA-WEAR-004; UI-007; ERR-WEAR-002; LOC-005; COM-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-WEAR-003, BRULE-WEAR-004, BRULE-WEAR-006, BRULE-PERS-005, BRULE-PERS-006 |
| Use Cases | [UC-016](../../../03-requirements/use-cases/UC-016-correct-wear-event.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-016](../../../03-requirements/activity-diagrams/AD-016-correct-wear-event.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q06: PO/BA with Developers must clarify which current/historical outfit choices are supported when correcting an existing report. The source fixes applicable-field and original-day/time limits but does not justify inventing replacement eligibility or a new outfit-capture flow.

Question ownership and impact: [catalogue questions CAT-Q06](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
