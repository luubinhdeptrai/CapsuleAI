# US-MULT-002 — Inspect newly enabled hypothetical outfits

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-MULT-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-031](../../product-backlog.md#pbi-031--preview-newly-enabled-hypothetical-outfits) |
| Domain | Wardrobe Multiplier |
| Theme | Strategic Shopping |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Better-Informed Additions |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to inspect the new combinations behind a candidate's Multiplier, so that incremental utility is understandable before I consider an addition.

## 2. Value

Make incremental candidate utility understandable through concrete combinations with owned clothing.

## 3. Scope

Same-assessment candidate-bearing preview inspection, clear hypothetical ownership, zero/non-exact/currentness constraints and independent preview retrieval recovery.

## 4. Preconditions

- An authorized candidate assessment with established status/basis is identifiable; a positive exact count and previews are not assumed available.

## 5. Acceptance Criteria

### AC-01 — Show genuinely new combinations

For a current completed Exact positive assessment, previews come from its newly enabled valid set, contain and identify the hypothetical unowned candidate, and use the same context/rule/uniqueness basis as the count.

### AC-02 — Keep current and hypothetical separate

Owned constituents remain identifiable while the candidate/previews are labeled hypothetical rather than daily owned advice. Preview viewing creates no owned garment, Wear Event, purchase or verified physical-wear evidence.

### AC-03 — Honor evaluated zero

A complete Evaluated Zero result shows no invented newly enabled outfits; preserve its established +0 meaning and allow return without purchase.

### AC-04 — Suppress unsupported current previews

Incomplete, Unavailable or Outdated assessment cannot present previews as current exact newly enabled evidence. Changed wardrobe/candidate/context/rules require current reevaluation; a failed refresh cannot restore current status.

### AC-05 — Recover preview retrieval independently

If only preview retrieval fails while the count/basis remains established and current, explain missing previews and retain that count appropriately. Offer preview retry/return without converting a known positive count to +0 or claiming evaluation completion from missing data.

### AC-06 — Respect optional use and privacy

Users may decline previews or return to core advice without shopping. Private assessment/garment information remains authorized and failure changes no accepted wardrobe/history.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-030.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Evaluation/status/count authority is [US-MULT-001](../multiplier/US-MULT-001-evaluate-incremental-candidate-outfit-utility.md); external navigation [US-SHOP-003](../shopping/US-SHOP-003-choose-optional-external-shopping-navigation.md). No generated ownership, mandatory preview count, selection algorithm or new shopping interaction is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-031](../../product-backlog.md#pbi-031--preview-newly-enabled-hypothetical-outfits); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-017, BR-022, CAP-09, CAP-10 |
| Product | [PRD](../../../02-product/PRD.md): JRN-07, FEAT-MULT-001, FEAT-SHOP-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-MULT-005, FR-MULT-006, FR-MULT-007, FR-MULT-008; FR-SHOP-002; DATA-OUT-003; DATA-ANL-004; DATA-INT-003; UI-008; ERR-ANL-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-MULT-004–BRULE-MULT-009, BRULE-OUT-002, BRULE-SHOP-002 |
| Use Cases | [UC-022](../../../03-requirements/use-cases/UC-022-evaluate-candidate-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-022](../../../03-requirements/activity-diagrams/AD-022-evaluate-candidate-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Same-basis preview and count semantics are established; exact visual presentation and retrieval realization remain JIT detail. No arbitrary preview quota is implied.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
