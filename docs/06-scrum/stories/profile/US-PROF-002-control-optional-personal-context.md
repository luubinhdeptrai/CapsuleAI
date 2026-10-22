# US-PROF-002 — Control optional body and gender context

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-PROF-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-010](../../product-backlog.md#pbi-010--set-and-revise-preferences-through-progressive-onboarding) |
| Domain | Personalization Profile |
| Theme | Personalization & Context |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Actionable Wardrobe Needs |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to omit, revise or remove optional body/gender information, so that I control sensitive context without losing wardrobe access or clothing choices.

## 2. Value

Give first advice a relevant personal basis while keeping optional information and wardrobe setup under user control.

## 3. Scope

Optional-context lifecycle during initial/later profile use and its effect as current soft personalization input.

## 4. Preconditions

- The user has authorized access to their profile; body/gender information need not already exist.

## 5. Acceptance Criteria

### AC-01 — Omit without penalty

The user can skip body/gender and continue authenticated entry without garment-category restrictions or fabricated inferred personal attributes.

### AC-02 — Supply or revise optionally

The user can submit supported optional-context changes through initial or later profile use; only accepted information becomes current. The story does not require body measurements or inferred gender.

### AC-03 — Remove current input

After accepted removal, the information is represented as absent and stops affecting future personalization; it is not silently replaced by inferred values.

### AC-04 — Keep influence soft

With or without this context, all four supported garment categories remain eligible under the same garment/validity rules. Body/gender cannot override hard validity, owned wardrobe or explicit preferences.

### AC-05 — Handle canceled or uncertain revision

Canceling before submission and known rejection/failure preserve accepted context. Uncertain acceptance remains unconfirmed pending review/safe retry; expired/revoked or other-account access grants no private read or mutation.

### AC-06 — Preserve purpose

Optional information is private and used only for supported purposes, with no MVP personal-data AI training/improvement or unrestricted sharing. Removing it does not imply an invented account deletion/export workflow.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-004. Useful sequencing: PBI-003, PBI-006; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Style/common needs are [US-PROF-001](../profile/US-PROF-001-set-and-revise-style-and-common-needs.md); exact ranking calculation is [US-PERS-002](../personalization/US-PERS-002-adapt-valid-ranking-from-feedback-and-wear.md). No new body taxonomy, measurement capture, inferred identity or physical-deletion deadline for optional profile fields is invented.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-010](../../product-backlog.md#pbi-010--set-and-revise-preferences-through-progressive-onboarding); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-010, BR-012, CAP-01, CAP-06 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01, FEAT-PROF-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-PROF-005–FR-PROF-007; FR-AUTH-011; FR-PERS-010; DATA-AUTH-004; NFR-PRIV-001, NFR-PRIV-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-PROF-001, BRULE-AUTH-007, BRULE-OUT-008 |
| Use Cases | [UC-005](../../../03-requirements/use-cases/UC-005-update-personalization-profile.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

No unresolved policy is identified. The source allows supported optional information without prescribing a new body/gender taxonomy; do not create one in AC.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
