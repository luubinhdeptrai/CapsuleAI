# US-COV-001 — See a defensible contextual Wardrobe Coverage Score

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-COV-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-026](../../product-backlog.md#pbi-026--see-a-defensible-personalized-wardrobe-coverage-score) |
| Domain | Wardrobe Coverage |
| Theme | Wardrobe Intelligence |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Actionable Wardrobe Needs |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want a score grounded in my common clothing needs and current wardrobe, so that I understand supported and underserved everyday capabilities.

## 2. Value

Help users understand how their owned wardrobe supports their relevant everyday needs.

## 3. Scope

Completed contextual Coverage, sufficient-data gate, unique-valid need support, source weighted formula, concise evidence and honest noncurrent/non-numeric states.

## 4. Preconditions

- Authorized current wardrobe/profile/context are accessible; positive needs and ready roles are assessment conditions, not gates to opening Coverage.

## 5. Acceptance Criteria

### AC-01 — Identify the context

Show the current confirmed eligible wardrobe, relevant declared style/environment, six common needs and mapped occasions, priority choices and limitations used for one Personalized Everyday Capsule.

### AC-02 — Respect sufficient data

A numeric score requires a positive-priority need and at least one applicable ready TOP, BOTTOM and FOOTWEAR, plus OUTERWEAR only when required by the evaluated need/environment and defensible required inputs. All-zero priorities or a missing required ready role produce insufficient information, not 0%.

### AC-03 — Count full valid support

Use hard-valid distinct garment-identity combinations for each need's mapped occasions; permutation or occurrence in several mapped occasions does not double-count a combination for that need. The small displayed daily result subset is not the complete support count.

### AC-04 — Apply the established formula

NeedCoverage is min(valid distinct outfits for need / 3, 1). Overall score is 100 times the sum of positive priority weight times NeedCoverage divided by the sum of positive weights. Weights are 0/1/2/3; zero-priority needs contribute nothing to the denominator or gaps.

### AC-05 — Verify controlled values

Per-need 0/1/2/3/4 valid outfits yield 0%/approximately 33%/approximately 67%/100%/100%. With only Everyday weight 3 and one valid outfit plus Work weight 1 and at least three, the contextual score is 50.

### AC-06 — Separate evaluated zero

With sufficient described ready inputs but no valid combinations, completed evaluated zero is legitimate and explained. Missing/failed/unavailable evaluation is not completed zero or proof of a mandatory missing product.

### AC-07 — Honor context and changes

Consume disclosed missing-weather fallback only where remaining sufficiency/validity is defensible. Relevant ownership/attributes, priorities, context or rule changes trigger reevaluation or outdated status; removed garments and failed refresh cannot support a current score.

### AC-08 — Keep interpretation bounded

The score describes contextual coverage, not universal completeness, future-need certainty, purchase necessity or reported physical wear. Loading/unavailable/private-access failure offers appropriate recovery without changing accepted wardrobe or inventing a number.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-017, PBI-010.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Detailed per-need inspection is [US-COV-002](../coverage/US-COV-002-inspect-per-need-coverage-evidence.md); capability bottlenecks [US-GAP-001](../gaps/US-GAP-001-understand-supported-capability-gaps.md). No garment quota, daily-result proxy, new score formula or mandatory temperature-based outerwear rule is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-026](../../product-backlog.md#pbi-026--see-a-defensible-personalized-wardrobe-coverage-score); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-03, BG-04, BR-014, BR-010, BR-022, CAP-07 |
| Product | [PRD](../../../02-product/PRD.md): JRN-06, FEAT-ANL-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-ANL-008–FR-ANL-015; DATA-ANL-001; DATA-OUT-001; DATA-INT-002; ERR-ANL-001, ERR-ANL-002; NFR-REL-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-COV-001–BRULE-COV-006, BRULE-OUT-001–BRULE-OUT-009 |
| Use Cases | [UC-019](../../../03-requirements/use-cases/UC-019-view-wardrobe-coverage.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-019](../../../03-requirements/activity-diagrams/AD-019-view-wardrobe-coverage.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Formula, sufficiency and mapping are established. Developers need independent small explicit outfit-set fixtures as well as arithmetic examples; enumeration/runtime design is deferred to selected-story JIT work.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
