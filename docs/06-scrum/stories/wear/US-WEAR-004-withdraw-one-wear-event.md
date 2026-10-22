# US-WEAR-004 — Withdraw one Wear Event and its effective influence

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-WEAR-004 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-023](../../product-backlog.md#pbi-023--withdraw-one-wear-event-and-remove-its-effective-influence) |
| Domain | Wear History |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to withdraw one report, so that it no longer affects my history or advice while other reports and my clothing remain intact.

## 2. Value

Let users withdraw a report while preserving other events, outfits and garments.

## 3. Scope

Explicit event-specific removal/cancellation, immediate effective exclusion, survivor recalculation and applicable 30-day personal-data deletion.

## 4. Preconditions

- The user has authorized access to the intended existing effective Wear Event.

## 5. Acceptance Criteria

### AC-01 — Remove the intended report only

Identify the target and explain that only this report is being removed. Accepted explicit removal excludes it immediately from effective history/utilization/recency/personalization, without deleting the underlying Outfit/Garments, independent feedback or unrelated reports.

### AC-02 — Retain surviving events

If other same-exact-outfit/original-day events survive, retain each in history and recompute that group's latest surviving accepted absolute anchor and its single aged Wear contribution. Removing the latest event moves the anchor to the survivor.

### AC-03 — Remove exhausted contribution

When the last effective event in a group is withdrawn, that group's Wear contribution ceases. Recompute exact-outfit recency from remaining effective events; do not clear unrelated groups or feedback.

### AC-04 — Meet physical deletion policy

Physically delete applicable personal event data within 30 days of accepted removal. Necessary unrelated history and underlying objects remain; immediate exclusion is not postponed until physical deletion.

### AC-05 — Cancel or resolve uncertain removal

Canceling before acceptance or a known rejection/failure leaves the accepted event/effects intact. An uncertain response is not proof of removal or unchanged state; review the selected event/history before safe retry.

### AC-06 — Handle changed target and authority

An already removed/changed selection yields actual state guidance without removing another report, restoring an event or multiplying removal effects. Wrong-account/unusable-session access discloses or mutates no private event.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-021.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Garment removal is [US-GAR-005](../garment/US-GAR-005-remove-current-garment-preserve-minimal-history.md); privacy assurance [US-PRIV-002](../privacy/US-PRIV-002-verify-mvp-authorization-privacy-and-retention.md). No all-day/all-outfit deletion, account deletion, restore feature or erasure of underlying outfits/garments is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-023](../../product-backlog.md#pbi-023--withdraw-one-wear-event-and-remove-its-effective-influence); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-006, BR-011, BR-021, CAP-06, CAP-07 |
| Product | [PRD](../../../02-product/PRD.md): JRN-05, FEAT-PERS-002, FEAT-ANL-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WEAR-007, FR-WEAR-008, FR-WEAR-010, FR-WEAR-012; DATA-RET-001; DATA-WEAR-004; FR-ANL-007; ERR-WEAR-002; COM-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-WEAR-005, BRULE-WEAR-006, BRULE-PERS-005, BRULE-PERS-006, BRULE-HIST-003 |
| Use Cases | [UC-017](../../../03-requirements/use-cases/UC-017-remove-wear-event.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-017](../../../03-requirements/activity-diagrams/AD-017-remove-wear-event.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The exclusion, survivor and 30-day policies are established. Durable deletion coordination and replay/concurrency handling remain JIT design with appropriate real integration evidence.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
