# US-GAP-001 — Understand evidence-supported wardrobe capability gaps

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAP-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-028](../../product-backlog.md#pbi-028--explain-evidence-supported-wardrobe-capability-gaps) |
| Domain | Wardrobe Gaps |
| Theme | Wardrobe Intelligence |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Actionable Wardrobe Needs |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to understand underserved wardrobe capabilities, so that I can decide whether to improve information, use owned clothing differently or consider an addition.

## 2. Value

Make underserved needs actionable without manufacturing product demand.

## 3. Scope

Current defensible Coverage-to-gap evidence, capability/bottleneck explanation, useful characteristics and no-gap/insufficient/unavailable/outdated alternatives.

## 4. Preconditions

- The user can access relevant wardrobe/common-need context; a completed assessment or existing gap is not required to request analysis.

## 5. Acceptance Criteria

### AC-01 — Use defensible need evidence

Consider only positive-priority needs with NeedCoverage below 1 as potential gap evidence, using current sufficient Coverage. Surface an evidence-supported capability/bottleneck rather than equating every absent product with a gap.

### AC-02 — Explain capability before product

Each surfaced gap identifies the need, supporting coverage/context and useful garment characteristics; a named product is a possible response, not a mandatory definition or universal checklist.

### AC-03 — Handle honest no-gap and insufficient states

A defensible no-important-gap result is meaningful. Insufficient evidence withholds unsupported gap/0% claims; unavailable retrieval is distinct from no gap and does not manufacture shopping demand.

### AC-04 — Retain supported gaps without candidates

If no useful/evaluable candidate exists, keep the supported capability explanation and disclose the candidate limitation. Offer optional related candidate/utility exploration only where supported.

### AC-05 — Keep currentness clear

Relevant Coverage/wardrobe/need/context/rule changes refresh gap evidence or mark it outdated. Failed reassessment cannot restore current gap status or invent a new bottleneck.

### AC-06 — Preserve user control and privacy

The user can continue wardrobe/context/insight work without shopping or a purchase. Viewing creates no ownership or wear; unauthorized access grants no private evidence and accepted personal state survives failed analysis.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-026. Useful sequencing: PBI-027; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Per-need readout is [US-COV-002](../coverage/US-COV-002-inspect-per-need-coverage-evidence.md); candidate information [US-SHOP-001](../shopping/US-SHOP-001-explore-supported-hypothetical-candidates.md); Multiplier [US-MULT-001](../multiplier/US-MULT-001-evaluate-incremental-candidate-outfit-utility.md). No universal footwear purchase, catalog-missing-item rule or new importance threshold is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-028](../../product-backlog.md#pbi-028--explain-evidence-supported-wardrobe-capability-gaps); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-015, BR-022, BR-024, CAP-08 |
| Product | [PRD](../../../02-product/PRD.md): JRN-06, FEAT-GAP-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-GAP-001–FR-GAP-005; DATA-ANL-002; ERR-ANL-001, ERR-ANL-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAP-001, BRULE-GAP-002, BRULE-COV-002, BRULE-COV-005, BRULE-COV-006 |
| Use Cases | [UC-020](../../../03-requirements/use-cases/UC-020-view-wardrobe-gaps.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-020](../../../03-requirements/activity-diagrams/AD-020-view-wardrobe-gaps.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Capability-first meaning and absence handling are settled. Exact explanation/characteristic examples must be supported by the evaluated evidence, not invented generic sales advice.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
