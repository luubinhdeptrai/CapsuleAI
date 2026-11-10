# US-PERS-002 — Adapt valid-outfit ranking from feedback and reported use

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-PERS-002 |
| Version | 0.2 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-024](../../product-backlog.md#pbi-024--adapt-future-valid-outfit-ranking-to-feedback-and-reported-use) |
| Domain | Behavioral Personalization |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

**2026-11-09 synchronization:** Approved PR #4 / DD-007 is propagated; existing Story/AC IDs and scope boundaries are retained. This revision makes no Sprint selection or implementation claim.

## 1. Story

As an authenticated user, I want my accepted feedback and reported wear to affect future valid choices, so that advice becomes more relevant and varied.

## 2. Value

Make repeated use of CapsuleAI more personally relevant and help rediscover owned clothing.

## 3. Scope

Source-faithful stored feedback/Wear evidence for future Matching Engine use, with revisions/clearing and corrections/removals reflected after hard validity. Exact engine strategy is unresolved (MVP-Q10); no prescribed weights, ranking aging, penalties, boosts or outfit/day increment cap.

## 4. Preconditions

- Valid candidates and available accepted explicit/behavioral evidence are identifiable; no minimum history quota is required.

## 5. Acceptance Criteria

### AC-01 — Keep hard validity dominant

Rank only already-valid outfits. Declared style/current occasion work without history, optional body/gender stay soft, and no useful valid alternative means a changed result is not guaranteed.

### AC-02 — Preserve distinct evidence types

Current supported Like/Dislike state and distinct user-reported Wear Events remain available with correct user, typed feedback target or outfit, timestamps and available context. No numeric contribution or strength comparison between Like and Wear is an acceptance condition.

### AC-03 — Preserve timestamps without mandated aging

Persist accepted action-time information and original Wear local/timezone context faithfully. No fixed ranking decay, elapsed-day influence bands or expiry of displayed feedback is required; state changes only through its approved lifecycle.

### AC-04 — Keep history identity distinct from grouping

Retain each legitimate intentional Wear Event, including repeated same-outfit/local-day reports. One logical action retried creates one event. Outfit/day grouping may serve history presentation/counting and never merges/deletes events or imposes a preference-increment cap.

### AC-05 — Defer recency strategy

No mandatory recency penalty or recency band applies. A future explicitly approved engine strategy may use stored evidence, but its tests must be source-linked and cannot override validity or user control.

### AC-06 — Defer overlooked-item ranking strategy

No mandatory or default overlooked-garment boost or evidence-age anchor is specified. Recorded-use indicators retain their honest history meaning and do not prove a garment was not worn. Former CAT-Q03 boost-anchor work is superseded by DD-007.

### AC-07 — Refresh stored evidence after user changes

Accepted feedback revision/clearing, optional context removal and Wear correction/removal update applicable stored state and surviving history for engine consumption. Unrelated reports and independent feedback remain. Do not calculate a mandated age, increment or penalty.

### AC-08 — Separate functional evidence and measurement retention

Necessary unremoved Wear history and functional feedback state follow their own lifecycle. User-linked measurement still expires or becomes non-user-linked within 90 days, and applicable removed garment/Wear data has its existing 30-day deletion obligation. Neither is a ranking-decay window.

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

[MVP-Q10](../../../02-product/PRD.md#342-mvp-expansion-open-decisions): PO/BA with engine Developers/QA must approve any future Matching Engine strategy and its proportional acceptance evidence. This Draft does not claim a ranking algorithm is specified or implemented. CAT-Q03's former optional-boost anchor is superseded; logical-action identity, typed targets, validity and retention are settled requirements, not reopened questions.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
