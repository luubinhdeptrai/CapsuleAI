# US-PERS-002 — Adapt valid-outfit ranking from feedback and reported use

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-PERS-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-024](../../product-backlog.md#pbi-024--adapt-future-valid-outfit-ranking-to-feedback-and-reported-use) |
| Domain | Behavioral Personalization |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want my accepted feedback and reported wear to affect future valid choices, so that advice becomes more relevant and varied.

## 2. Value

Make repeated use of CapsuleAI more personally relevant and help rediscover owned clothing.

## 3. Scope

Lightweight effective-evidence contribution, elapsed-time aging, same-outfit/day Wear normalization, recency/diversity and survivor/context revisions after hard validity.

## 4. Preconditions

- Valid candidates and available accepted explicit/behavioral evidence are identifiable; no minimum history quota is required.

## 5. Acceptance Criteria

### AC-01 — Keep hard validity dominant

Rank only already-valid outfits. Declared style/current occasion work without history, optional body/gender stay soft, and no useful valid alternative means a changed result is not guaranteed.

### AC-02 — Apply base contributions

In controlled equal-condition cases, Like contributes +1, Dislike -2 and an effective Wear increment +2 before decay; Wear is stronger positive evidence than Like, while Dislike is not a permanent garment/category ban.

### AC-03 — Age by absolute elapsed time

Use completed elapsed 24-hour periods between evaluation and the applicable evidence anchor: 0–30 days gives 100%, 31–60 gives 50%, 61–90 gives 25%, and beyond 90 gives zero. At 30 days 23h59m influence is 100%; exactly 31/61/91 days gives 50%/25%/0%. Device timezone changes alone do not change age.

### AC-04 — Normalize Wear without merging history

For the same user/exact outfit/original local day, surviving reports supply at most one +2 increment before decay, anchored at the latest surviving accepted absolute timestamp. Different outfits/days remain distinct; multiple intentional reports remain separate history events.

### AC-05 — Apply bounded soft recency

For exact-outfit latest effective wear, 0–2 completed elapsed days gives a strong soft diversity penalty, 3–7 mild and beyond 7 none. These never invalidate an outfit; numerical penalty magnitudes are not invented.

### AC-06 — Bound optional overlooked boost

A ready garment with no recorded wear for at least 14 completed elapsed days may receive a small soft boost only with an established evidence anchor and without overriding validity, strong explicit negative feedback or context relevance. Do not invent garment-creation time as the anchor; CAT-Q03 remains open.

### AC-07 — Recompute current evidence

Accepted feedback revision/clearing, context revision and Wear correction/removal recompute affected groups, latest surviving anchors, decay and recency. No surviving report means no group Wear contribution; unrelated accepted reports remain.

### AC-08 — Separate aging and retention

Expired ranking influence does not erase necessary history or clear effective Like/Dislike state. Removed optional context stops current use; removal/deletion and user-linked measurement retention retain their separate policies.

### AC-09 — Handle missing evidence honestly

With missing/unavailable history use available explicit context and disclose limitations, without fabricated signals, invalid choices or an MVP personal-data training dependency.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-020, PBI-021. Useful sequencing: PBI-022, PBI-023; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Feedback/event capture and repair remain their owning stories; no complete ranking algorithm, extra numeric weights, mandatory learned model or fixed universal result order is specified.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-024](../../product-backlog.md#pbi-024--adapt-future-valid-outfit-ranking-to-feedback-and-reported-use); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-009, BR-011, BR-012, CAP-06 |
| Product | [PRD](../../../02-product/PRD.md): JRN-04, JRN-05, FEAT-PERS-003 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-PERS-006–FR-PERS-013; DATA-WEAR-001, DATA-WEAR-003, DATA-WEAR-004; FR-ANL-007; DATA-RET-001–DATA-RET-003 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-OUT-008, BRULE-PERS-001–BRULE-PERS-006, BRULE-HIST-003 |
| Use Cases | [UC-011](../../../03-requirements/use-cases/UC-011-get-outfit-recommendations.md); [UC-013](../../../03-requirements/use-cases/UC-013-provide-outfit-feedback.md); [UC-014](../../../03-requirements/use-cases/UC-014-record-wear-event.md); [UC-016](../../../03-requirements/use-cases/UC-016-correct-wear-event.md); [UC-017](../../../03-requirements/use-cases/UC-017-remove-wear-event.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-011](../../../03-requirements/activity-diagrams/AD-011-get-outfit-recommendations.puml); [AD-014](../../../03-requirements/activity-diagrams/AD-014-record-wear-event.puml); [AD-016](../../../03-requirements/activity-diagrams/AD-016-correct-wear-event.puml); [AD-017](../../../03-requirements/activity-diagrams/AD-017-remove-wear-event.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q03: PO/BA with Developers must establish a supported evidence anchor before applying the optional >=14-day no-recorded-wear boost. No created-at origin or numerical boost magnitude is authorized. Required contribution/decay/recency rules are already specified.

Question ownership and impact: [catalogue questions CAT-Q03](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
