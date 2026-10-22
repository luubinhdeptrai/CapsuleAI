# US-QUAL-002 — Verify garment-processing performance

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Quality Story |
| Story ID | US-QUAL-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-035](../../product-backlog.md#pbi-035--validate-image-and-daily-outfit-performance-against-the-srs) |
| Domain | Quality Verification |
| Theme | Quality / Security / Operations |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding; Easier Outfit Decisions; Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Demonstrate that the complete assisted processing operation meets the existing processing-time target under the SRS reference conditions.

## 2. Value

Protect low-effort digitization and fast daily decisions with repeatable measured evidence.

## 3. Scope

Processing timing boundary, representative eligible input and usable preview plus proposals, prescribed sampling/p95 evidence and honest failure reporting.

## 4. Preconditions

- The evaluated PBI-013 assisted path is runnable. Parent PBI-035 also needs PBI-017 for its separate outfit-performance slice; this timing exercise does not require that path to run.

## 5. Acceptance Criteria

### AC-01 — Use the exact interval

Measure from accepted analysis request after image transfer is complete until both usable processed preview and reviewable proposals are available. Upload time is not included, and preview alone does not end the interval.

### AC-02 — Use reference conditions

Use and record the SRS Section 12.3 reference devices/network/environment: Android 10+ modern mid-range hardware with at least 6 GB RAM; iOS 15+ iPhone 11-class or newer; stable Wi-Fi/4G-class connectivity with download at least 20 Mbps, upload at least 5 Mbps and RTT at most 100 ms, plus the evaluated software/environment.

### AC-03 — Apply prescribed sampling

Run 5 warm-ups followed by at least 100 measured executions per evaluated operation; calculate p95 from the measured runs and retain legitimate slow runs rather than censoring them to pass.

### AC-04 — Meet the target with a complete result

Processing p95 is at most 5 seconds for the defined complete operation under reference conditions. Record failures and unavailable/unusable outcomes honestly; a failed or incomplete result cannot count as successful fast completion.

### AC-05 — Keep evidence reproducible and bounded

Retain inspectable input qualification, durations/start-end basis, sample counts and configuration using non-personal/disposable evidence. No new production uptime, universal AI timeout or proof of recognition accuracy is inferred from latency.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-013, PBI-017.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Recognition/preview quality evidence is [US-QUAL-001](../quality/US-QUAL-001-validate-locked-garment-assistance-benchmark.md); outfit performance [US-QUAL-003](../quality/US-QUAL-003-verify-daily-outfit-performance.md). No optimization tactic, new hardware floor, Multiplier latency or additional arbitrary workload is selected.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-035](../../product-backlog.md#pbi-035--validate-image-and-daily-outfit-performance-against-the-srs); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-001, MET-Q02 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-AI-001, FEAT-MET-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): NFR-PERF-001, NFR-TEST-003; FR-MET-004; SRS Sections 2.3, 3.4.1 and 12.3 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-001, BRULE-GAR-007 |
| Use Cases | [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The reference/measurement policy is established; Developers/QA arrange actual devices/network/environment and evidence before claiming a measured result. No benchmark has run as part of this documentation task.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
