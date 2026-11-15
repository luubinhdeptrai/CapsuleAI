# US-PERS-001 — Express, revise or clear outfit Like or Dislike

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-PERS-001 |
| Version | 0.2 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-020](../../product-backlog.md#pbi-020--express-revise-or-clear-outfit-likedislike) |
| Domain | Behavioral Personalization |
| Theme | Wear & Learning |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

**2026-11-09 synchronization:** Approved PR #4 / DD-007 is propagated; existing Story/AC IDs and scope boundaries are retained. This revision makes no Sprint selection or implementation claim.

## 1. Story

As an authenticated user, I want to set, change or clear my preference for an exact outfit, so that future advice can use my current feedback.

## 2. Value

Give users control over meaningful preference evidence separately from reported wear.

## 3. Scope

Like on AI Today/Chat outfit suggestions and Community posts; Dislike only on AI Today/Chat suggestions, never Community. Preserve typed target/source identity, one effective state, revision/clearing and safe failure/uncertainty without Wear or prescribed ranking effects.

## 4. Preconditions

- The authorized recommendation/exact outfit target is identifiable; an existing feedback state is optional.

## 5. Acceptance Criteria

### AC-01 — Set the supported target state

Accepted Like applies to an identified AI Today/Chat outfit suggestion or Community post; Dislike applies only to AI Today/Chat suggestions. Preserve affected user, target kind/origin and identifiable source reference. The same garment set in an AI suggestion and a Community post does not alias their feedback. Unidentified/inaccessible targets receive no unrelated feedback; Community Dislike is neither offered nor accepted and changes no state. FR-PERS-001/002/014 govern the target policy.

### AC-02 — Revise or clear supported feedback

On an AI target, changing Like to Dislike or vice versa leaves one accepted effective state. On Community, Like can be applied or cleared, never changed to Dislike. Clearing leaves no effective preference for that target and never deletes Wear history; retries/failures cannot leave contradictory effective states.

### AC-03 — Store user-expressed meaning

Store accepted Like/Dislike state and applicable action-time information for history and future Matching Engine use until revised/cleared/superseded through user action, subject to functional retention/removal. No fixed numeric contribution, ranking decay, recency penalty, boost, relative strength or outfit/day increment cap is prescribed. Dislike does not automatically ban garments/categories.

### AC-04 — Handle failure and uncertainty

Known failed/rejected updates retain the prior effective state. Uncertain outcomes remain unconfirmed for review/safe retry without simultaneously contradictory states or a false acceptance claim.

### AC-05 — Keep wear and validity separate

Preference actions/views create no Wear Event. Future influence remains soft and cannot admit an invalid outfit or guarantee changed advice when no useful valid alternative exists.

### AC-06 — Enforce private purpose

Only authorized affected-user access may inspect/change feedback; secrets and unrelated private contents are not exposed. Measurement failure does not undo accepted feedback or imply model-training consent.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- A supported authorized target from its owning capability: PBI-017 for AI Today, PBI-042 for Community, or PBI-044 for AI Chat. These target sources are alternatives by slice, not a gate requiring every feature first.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Future engine evidence consumption is [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md); Wear reporting is [US-WEAR-001](../wear/US-WEAR-001-report-an-intentional-wear-event.md). No reason survey, permanent garment ban, similarity algorithm or feedback endpoint design is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-020](../../product-backlog.md#pbi-020--express-revise-or-clear-outfit-likedislike); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-011, BR-021, CAP-06 |
| Product | [PRD](../../../02-product/PRD.md): JRN-05, FEAT-PERS-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-PERS-001–FR-PERS-005, FR-PERS-013; DATA-OUT-001; DATA-WEAR-001; COM-002; FR-MET-007 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-PERS-001, BRULE-PERS-003, BRULE-PERS-006, BRULE-OUT-008 |
| Use Cases | [UC-013](../../../03-requirements/use-cases/UC-013-provide-outfit-feedback.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-013](../../../03-requirements/activity-diagrams/AD-013-provide-outfit-feedback.puml): typed target, supported actions and accepted/rejected state. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

No new feedback policy decision is needed. Target/lifecycle integration mechanics are selected-story JIT details; planned tests distinguish typed targets, supported actions and current accepted state without asserting a fixed ranking effect. MVP-Q10 tracks any future engine policy.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
