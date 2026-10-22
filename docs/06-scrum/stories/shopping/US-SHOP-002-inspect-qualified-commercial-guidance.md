# US-SHOP-002 — Inspect credible and qualified commercial guidance

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-SHOP-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-032](../../product-backlog.md#pbi-032--show-qualified-commercial-guidance-when-credible) |
| Domain | Strategic Shopping |
| Theme | Strategic Shopping |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Better-Informed Additions |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want available commercial guidance to show its evidence and limits, so that I can consider a candidate without mistaking estimates for guarantees.

## 2. Value

Support informed consideration of additions without inventing merchant facts or making commerce necessary.

## 3. Scope

Conditional supported price/material/durability/retailer/destination information with source/time and 24-hour price/availability treatment, independent of outfit utility.

## 4. Preconditions

- Authorized candidate advice is available; optional credible commercial information is not a prerequisite to core candidate utility.

## 5. Acceptance Criteria

### AC-01 — Show supported optional facts only

Show optional price ranges, material, durability/longevity, retailer or destination information only where credible and available. Qualify estimates and uncertain composition; do not fabricate merchant partnerships, stock or guarantees.

### AC-02 — Preserve provenance

External commercial values retain identifiable source and retrieval/last-checked time so the user can understand their basis.

### AC-03 — Apply commercial currentness

Price/availability at exactly 24 hours old may be current informational guidance; older information must be refreshed, explicitly stale/last checked, or omitted. Failed refresh is not a current guaranteed merchant fact.

### AC-04 — Retain independent utility

Missing/stale commercial values or a destination failure do not convert an otherwise current supported Multiplier to zero or remove its supported candidate/gap/reasons/previews. Utility changes only when its own relevant basis changes.

### AC-05 — Keep absence honest

When credible optional information is unavailable, omit/qualify it without manufacturing prices, longevity, scarcity or candidate invalidity. Users may continue without purchase, retailer contract or affiliate participation.

### AC-06 — Preserve private control

Reading optional guidance changes no ownership/history or transaction state and gives providers/destinations no unrestricted wardrobe/profile/history access.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-029.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Candidate identity is [US-SHOP-001](../shopping/US-SHOP-001-explore-supported-hypothetical-candidates.md), Multiplier [US-MULT-001](../multiplier/US-MULT-001-evaluate-incremental-candidate-outfit-utility.md) and external action [US-SHOP-003](../shopping/US-SHOP-003-choose-optional-external-shopping-navigation.md). No live inventory guarantee, native commerce, source contract or new credibility score is introduced.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-032](../../product-backlog.md#pbi-032--show-qualified-commercial-guidance-when-credible); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-020, BR-024, CAP-10 |
| Product | [PRD](../../../02-product/PRD.md): JRN-07, FEAT-SHOP-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-SHOP-003, FR-SHOP-004, FR-SHOP-006, FR-SHOP-008; DATA-ANL-003; ERR-SHOP-002; COM-003 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-SHOP-001–BRULE-SHOP-003, BRULE-MULT-008 |
| Use Cases | [UC-021](../../../03-requirements/use-cases/UC-021-view-shopping-recommendations.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-021](../../../03-requirements/activity-diagrams/AD-021-view-shopping-recommendations.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q04: identify credible sources/evidence available for optional fields. No merchant/provider selection is made; unavailable optional guidance does not block supported utility.

Question ownership and impact: [catalogue questions CAT-Q04](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
