# US-QUAL-001 — Validate garment assistance on the locked benchmark

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Quality Story |
| Story ID | US-QUAL-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-014](../../product-backlog.md#pbi-014--validate-garment-assistance-quality-on-a-locked-benchmark) |
| Domain | Quality Verification |
| Theme | Quality / Security / Operations |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding; Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Establish reproducible, independent evidence that garment assistance meets the existing recognition floors and preview criteria without compromising user authority or manual continuity.

## 2. Value

Establish defensible assistance quality and reduce misleading confidence or burdensome corrections.

## 3. Scope

Locked non-personal corpus, qualification/ground truth, separate category/color/preview evidence and guarded confidence claims.

## 4. Preconditions

- The assisted-entry behavior under evaluation is available for execution; corpus preparation can begin earlier without claiming a completed benchmark.

## 5. Acceptance Criteria

### AC-01 — Lock representative qualified inputs

Use at least 200 qualified images, including at least 50 for each TOP, BOTTOM, OUTERWEAR and FOOTWEAR. Qualification follows SRS Section 3.4.1 and does not require a plain/white background. Keep the locked set out of training/tuning after lock.

### AC-02 — Establish ground truth

Retain supported-category and controlled dominant-color-family labels, qualification decisions and labeling/adjudication evidence. Follow the SRS protocol: two independent annotators should label the benchmark, with disagreements resolved by adjudication.

### AC-03 — Measure category independently

Report primary-category recognition accuracy of at least 90% overall and at least 85% within each of the four category groups, with inspectable denominators, predictions and evaluated revision/model basis.

### AC-04 — Measure color separately

Report at least 90% overall dominant-color-family accuracy on the same locked qualified set. Category accuracy, color accuracy and preview usability are separate results; floors are not extended to every rich attribute.

### AC-05 — Assess usable previews

Review identifiable garment, preserved major regions, non-obstructive background and sufficient correction/confirmation information. Do not invent a preview-success percentage or substitute recognition accuracy for preview usability.

### AC-06 — Verify confidence safeguards

Check 0.60/0.85 default-state boundaries and source evidence/validation guards, including materially conflicting or unavailable evidence; an unvalidated high raw score cannot establish High Confidence or confirmation.

### AC-07 — Preserve continuity and evidence

Agreed representative analysis-failure/uncertainty cases retain manual creation and user authority. Missing, failed or incomplete benchmark evidence is reported honestly rather than as passing quality; use no personal-data training/improvement inputs.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-013.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Processing latency is PBI-035/[US-QUAL-002](../quality/US-QUAL-002-verify-garment-processing-performance.md). Model architecture/training implementation, an invented rich-attribute accuracy floor and extra benchmark documents are not required by this story.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-014](../../product-backlog.md#pbi-014--validate-garment-assistance-quality-on-a-locked-benchmark); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-001, BR-002, MET-Q01, MET-Q05 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-AI-001, FEAT-AI-002, FEAT-GAR-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): AI-REQ-004, AI-REQ-010, AI-REQ-011, AI-REQ-014; NFR-USE-002, NFR-REL-001; FR-MET-004; SRS Sections 3.4.1 and 8.1 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-001, BRULE-GAR-007, BRULE-GAR-008 |
| Use Cases | [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q05 covers required High Confidence validation evidence. FR-MET-004 has a broken prose cross-reference; use the explicit SRS Sections 8.1/12.3 rather than guessing a different benchmark. Actual corpus/annotator resources are delivery feasibility, not measured results.

Question ownership and impact: [catalogue questions CAT-Q05](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
