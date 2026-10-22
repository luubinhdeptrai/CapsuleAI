# US-QUAL-004 — Verify accessible Vietnamese journeys on supported devices

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Quality Story |
| Story ID | US-QUAL-004 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-036](../../product-backlog.md#pbi-036--verify-accessible-vietnamese-journeys-on-supported-devices) |
| Domain | Quality Verification |
| Theme | Quality / Security / Operations |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Demonstrate that delivered core journeys remain understandable and operable with Vietnamese content, assistive technology and enlarged text.

## 2. Value

Make the connected experience operable for users relying on assistive technology or enlarged text.

## 3. Scope

Progressive connected-journey accessibility/localization evidence across supported Android/iOS, supplementing each increment's existing obligations.

## 4. Preconditions

- PBI-003's shared mobile experience and the representative capabilities under evaluation are actually available.

## 5. Acceptance Criteria

### AC-01 — Exercise actual supported clients

Verify applicable delivered core journeys with Android TalkBack and iOS VoiceOver under supported Android 10+/iOS 15+ conditions, recording devices, revision, journey scope and limitations.

### AC-02 — Verify interactive semantics

Applicable primary controls expose meaningful accessible names, roles and states and remain discoverable/operable; touch targets follow approximately 48 dp Android and 44 pt iOS minima.

### AC-03 — Verify enlarged text

At scaling up to 200%, core journeys remain operable and primary actions accessible rather than obscured or unusable.

### AC-04 — Preserve semantic states

Confidence/readiness, insufficient/unavailable/outdated, removed and hypothetical states have understandable labels beyond color/images. Imageless garments remain identifiable and a failed operation cannot be mistaken for success/empty results.

### AC-05 — Preserve Vietnamese meaning

Vietnamese labels/recovery/explanations and applicable input preserve diacritics and canonical category/style/occasion/confidence/provenance meanings; original event-local date/time remains correct after travel and valid corrections.

### AC-06 — Keep evidence and remediation honest

Retain actual findings and verification of required fixes in existing work/review evidence. Unverified or failing required behavior is not a passing result; later cross-journey checks do not postpone accessibility in the owning stories or imply certification.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-003.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

No new UI language, certification claim, product capability, layout system or accessibility threshold is introduced; full representative-user study is [US-QUAL-005](../quality/US-QUAL-005-validate-representative-core-task-usability.md).

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-036](../../product-backlog.md#pbi-036--verify-accessible-vietnamese-journeys-on-supported-devices); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-023, BR-022 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01–JRN-07; Sections 21 and 25 |
| SRS | [SRS](../../../03-requirements/SRS.md): NFR-ACC-001, NFR-ACC-002; LOC-001–LOC-005; UI-003–UI-009; SRS Section 12.4 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-007, BRULE-HIST-002, BRULE-WEAR-003 |
| Use Cases | [UC-001](../../../03-requirements/use-cases/UC-001-register-account.md); [UC-002](../../../03-requirements/use-cases/UC-002-log-in.md); [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); [UC-011](../../../03-requirements/use-cases/UC-011-get-outfit-recommendations.md); [UC-012](../../../03-requirements/use-cases/UC-012-shuffle-garment-slot.md); [UC-014](../../../03-requirements/use-cases/UC-014-record-wear-event.md); [UC-016](../../../03-requirements/use-cases/UC-016-correct-wear-event.md); [UC-017](../../../03-requirements/use-cases/UC-017-remove-wear-event.md); [UC-019](../../../03-requirements/use-cases/UC-019-view-wardrobe-coverage.md); [UC-020](../../../03-requirements/use-cases/UC-020-view-wardrobe-gaps.md); [UC-022](../../../03-requirements/use-cases/UC-022-evaluate-candidate-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The source criteria are fixed. Developers/QA must arrange actual supported devices/assistive-technology access; backend-only or mock UI evidence cannot establish this outcome.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
