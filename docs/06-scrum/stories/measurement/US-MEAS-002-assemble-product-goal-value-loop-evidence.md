# US-MEAS-002 — Assemble Product Goal evidence across the value loop

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Enabler Story |
| Story ID | US-MEAS-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-034](../../product-backlog.md#pbi-034--complete-product-goal-evidence-across-the-value-loop) |
| Domain | Product Measurement |
| Theme | Quality / Security / Operations |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Extend existing privacy-respecting measurement to the connected value loop and assemble interpretable evidence for the established Product Goal directions.

## 2. Value

Allow the team to evaluate the documented outcome directions rather than assume product usefulness.

## 3. Scope

Coverage/gap/candidate/preview/link evidence, existing digitization/selection/feedback/Wear/utilization/repeat-use directions and honest stage/limitations, reusing early measurement.

## 4. Preconditions

- PBI-009's measurement foundation is available; each producer/validation source is integrated when its capability and established measurement stage exist.

## 5. Acceptance Criteria

### AC-01 — Extend rather than recreate capture

Coverage/gap/candidate detail and hypothetical-preview views plus external-link attempts/established openings are distinguishable, building on existing garment/advice/feedback/Wear evidence rather than a second measurement authority.

### AC-02 — Preserve outcome meanings

Exposure/attempt is not accepted action; Wear creation/correction/removal are separate, retries do not inflate intentional-event counts, and normalized ranking increments do not replace history counts. Navigation is not verified sale or ownership.

### AC-03 — Map existing outcome directions

Evidence maps to Product Goal Section 7 and BRD MET-E01–MET-E07/MET-Q01–MET-Q07 and optional MET-C01 at their established stages, including digitization/corrections, selection effort, reported utilization and insight/preview engagement.

### AC-04 — Interpret benefits honestly

Distinguish captured interaction data from user-reported effort and controlled quality validation. An exposure count alone proves neither reduced decision time nor purchase quality; unavailable/not-yet-observed cohort evidence and limitations are explicit, with no invented growth/revenue threshold.

### AC-05 — Maintain protected lifecycle

Minimize ordinary measurement contents and remove or aggregate/de-identify user-linked engagement data within 90 days. Retained aggregates must no longer be user-linked; functional history and 30-day removed-data policies remain separate.

### AC-06 — Preserve core acceptance

Measurement failure cannot block or change an otherwise usable core action. Evidence remains inspectable for its tested revision/basis without raw private photos, sensitive profiles or secrets; missing required evidence is not reported as established success.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-009.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Early measurement is [US-MEAS-001](../measurement/US-MEAS-001-capture-minimum-wardrobe-use-evidence.md). No analytics dashboard/administrator, vendor/schema, new KPI target, verified referral revenue or commercial-model approval is created.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-034](../../product-backlog.md#pbi-034--complete-product-goal-evidence-across-the-value-loop); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-019, BR-021 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-MET-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-MET-001–FR-MET-007; DATA-RET-003; NFR-TEST-002; NFR-SUP-002; ERR-MET-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-WEAR-006, BRULE-HIST-003, BRULE-SHOP-002, BRULE-AUTH-007 |
| Use Cases | [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); [UC-011](../../../03-requirements/use-cases/UC-011-get-outfit-recommendations.md); [UC-013](../../../03-requirements/use-cases/UC-013-provide-outfit-feedback.md); [UC-014](../../../03-requirements/use-cases/UC-014-record-wear-event.md); [UC-018](../../../03-requirements/use-cases/UC-018-view-wardrobe-utilization.md); [UC-019](../../../03-requirements/use-cases/UC-019-view-wardrobe-coverage.md); [UC-020](../../../03-requirements/use-cases/UC-020-view-wardrobe-gaps.md); [UC-021](../../../03-requirements/use-cases/UC-021-view-shopping-recommendations.md); [UC-022](../../../03-requirements/use-cases/UC-022-evaluate-candidate-garment.md); [UC-023](../../../03-requirements/use-cases/UC-023-open-external-shopping-link.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q07: operational metric definitions, cohort/reporting windows and interpretation evidence need PO/BA with Developers/QA review. Use explicit SRS Sections 8.1/12.3 for FR-MET-004's damaged benchmark reference; no replacement target is inferred.

Question ownership and impact: [catalogue questions CAT-Q07](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
