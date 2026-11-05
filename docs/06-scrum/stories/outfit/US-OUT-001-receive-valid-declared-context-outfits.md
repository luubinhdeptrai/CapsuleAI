# US-OUT-001 — Receive valid outfits for my declared context

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-OUT-001 |
| Version | 0.3 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-017](../../product-backlog.md#pbi-017--receive-valid-daily-outfits-relevant-to-declared-context) |
| Domain | Outfit Advice |
| Theme | Outfit Decision Support |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |
| Product revision | 2026-11-05: Clarified category versus retained outfit roles following approved taxonomy expansion; existing validity and calculation rules preserved. |

## 1. Story

As an authenticated user, I want distinct valid outfits from my current wardrobe for the context I choose, so that daily outfit decisions require less effort.

## 2. Value

Reduce daily decision effort with distinct wearable combinations of current owned garments.

## 3. Scope

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

Initial owned-wardrobe advice, applicable hard filtering before declared-context soft ranking, concise reasons and truthful limited/zero/insufficient/unavailable/outdated results.

## 4. Preconditions

- The user has authorized wardrobe/context access; enough garments evaluated under a future approved policy and useful choices are assessment conditions, not access quotas.

## 5. Acceptance Criteria

### AC-01 — Identify the assessment basis

Identify the current wardrobe, declared style/current occasion and available relevant personalization/environment actually used. Missing history/body/gender is not a gate, and request occasion does not overwrite common needs.

### AC-02 — Preserve confirmed ownership before future evaluation

Use authorized confirmed active current owned garments; exclude drafts, removed inventory and hypothetical candidates. Eligibility/composition policy is deferred; saving or optional absence cannot determine it.

### AC-03 — Defer composition policy

**Superseded policy; AC identity retained.** The former fixed composition/category mapping is no longer an approved algorithm. Define composition during future recommendation-engine development under the approved property model; no current implementation or test of old roles is required.

### AC-04 — Defer property-based compatibility policy

**Superseded policy; AC identity retained.** Former removed-property pattern/environment/layer checks are no longer active. Request weather/occasion remain contextual inputs; future engine policy must define compatibility without hidden garment fields or unsupported valid-outfit claims.

### AC-05 — Rank only valid choices

Hard-invalid combinations remain excluded even with strong positive preferences. Soft color harmony, declared style/current occasion and relevant available soft context affect valid choices; body/gender cannot prohibit supported categories. Initial advice needs no behavioral history.

### AC-06 — Offer actual distinct variety

With at least three valid distinct garment-identity combinations, present at least three. With only one or two, present the actual set and explain; permutation is the same outfit, not a padded option.

### AC-07 — Distinguish completed zero and insufficient data

A complete supported assessment with no valid combination explains no-valid results. Missing evidence required by a future evaluation policy/information is insufficient assessment instead; neither outcome erases owned data, imposes a wardrobe quota or fabricates a capability gap.

### AC-08 — Disclose missing weather

Consume location/weather through its established acquisition boundary. Weather older than 30 minutes must not be used as current; absent usable weather within the 2-second delay boundary permits only disclosed reduced-context advice with remaining validity preserved.

### AC-09 — Handle failed or changed basis

Loading/unavailable results offer retry or return and are not a completed zero. Relevant wardrobe/context/rule changes trigger reevaluation or outdated status; a failed refresh is not fresh advice.

### AC-10 — Keep viewing separate

Requests/views and selection for inspection create no Like/Dislike, Wear Event, ownership or purchase. Authorized account/session boundaries apply to every returned constituent and explanation.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-008, PBI-010. Useful sequencing: PBI-011; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Detailed inspection is [US-OUT-002](../outfit/US-OUT-002-inspect-outfit-constituents-and-reasons.md); Shuffle is [US-OUT-003](../outfit/US-OUT-003-shuffle-one-garment-slot.md); behavioral ranking is [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md). No enumeration/ranking algorithm, learned model, mandatory outerwear temperature or new latency policy is selected.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-017](../../product-backlog.md#pbi-017--receive-valid-daily-outfits-relevant-to-declared-context); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-02, BR-007, BR-008, BR-009, BR-010, BR-012, BR-022, CAP-05 |
| Product | [PRD](../../../02-product/PRD.md): JRN-04, FEAT-OUT-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-OUT-001–FR-OUT-009, FR-OUT-012, FR-OUT-013; FR-PERS-006, FR-PERS-010, FR-PERS-011; DATA-OUT-001, DATA-OUT-002; ERR-OUT-001, ERR-OUT-002; NFR-PERF-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-003, BRULE-OUT-001–BRULE-OUT-009, BRULE-PROF-002 |
| Use Cases | [UC-011](../../../03-requirements/use-cases/UC-011-get-outfit-recommendations.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-011](../../../03-requirements/activity-diagrams/AD-011-get-outfit-recommendations.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Validity and initial ranking semantics are established for the retained separated-garment template; PRD OPQ-012 remains open for newly supported categories. Developers must assess feasible candidate generation and test evidence without prescribing full algorithms/contracts before Sprint selection; exact soft ordering is not a new universal AC.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
