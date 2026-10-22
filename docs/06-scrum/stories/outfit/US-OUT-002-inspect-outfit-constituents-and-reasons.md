# US-OUT-002 — Inspect outfit constituents and explanation

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-OUT-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-018](../../product-backlog.md#pbi-018--inspect-outfit-constituents-and-explanation) |
| Domain | Outfit Advice |
| Theme | Outfit Decision Support |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to inspect the garments and reasons behind an outfit, so that I can judge the recommendation before taking a separate action.

## 2. Value

Help users judge why a proposed outfit is suitable before choosing a separate action.

## 3. Scope

Full detail for a displayed outfit, garment-detail access, actual assessed context/reasons, current/historical/hypothetical distinction and appropriate return/recovery.

## 4. Preconditions

- An identified outfit and its available assessment context can be selected under authorized access; unavailable/changed detail is handled.

## 5. Acceptance Criteria

### AC-01 — Identify constituents

Detail identifies the assessed garments and their available visuals/information, including imageless entries, and offers the applicable owned-garment detail and return paths.

### AC-02 — Explain actual reasoning

Reasons match accepted garment information and the style/occasion/environmental and relevant preference basis actually evaluated; missing context and interpretation limits are disclosed instead of verified personal claims.

### AC-03 — Separate outfit contexts

Current owned advice, historical reported selections and hypothetical previews are visibly distinct. A historical snapshot or candidate does not establish current eligibility or permit Wear as an owned outfit.

### AC-04 — Keep actions explicit

Where delivered and applicable to current owned advice, offer separate Shuffle, Like/Dislike and Wear entry points. Inspecting/returning alone changes none of those states.

### AC-05 — Handle changed or unavailable information

If a constituent is no longer current, the basis changed or detail retrieval fails, explain the actual limitation and offer return/refresh/retry. Failed retrieval is not proof of no garments and failed refresh is not current advice.

### AC-06 — Retain private state

Denied/unusable access exposes no other-account detail, and inspection failures do not mutate accepted wardrobe, feedback or history.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-017, PBI-007.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Concise result-list reasons belong to [US-OUT-001](../outfit/US-OUT-001-receive-valid-declared-context-outfits.md); actual interactions remain their owning stories. No internal weights, rule execution trace or new feedback action is required.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-018](../../product-backlog.md#pbi-018--inspect-outfit-constituents-and-explanation); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-007, BR-022, CAP-04, CAP-05 |
| Product | [PRD](../../../02-product/PRD.md): JRN-04, FEAT-OUT-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-OUT-010–FR-OUT-013; DATA-OUT-002, DATA-OUT-003; UI-004, UI-008; ERR-ANL-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-OUT-002, BRULE-OUT-008, BRULE-HIST-002, BRULE-MULT-001 |
| Use Cases | [UC-011](../../../03-requirements/use-cases/UC-011-get-outfit-recommendations.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-011](../../../03-requirements/activity-diagrams/AD-011-get-outfit-recommendations.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Exact presentation and applicable interaction handoffs remain UX/JIT detail. Future feedback/Wear implementation is not a new hard gate to reading supported outfit reasons.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
