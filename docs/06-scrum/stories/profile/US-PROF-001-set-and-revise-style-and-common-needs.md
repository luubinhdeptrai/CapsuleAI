# US-PROF-001 — Set and revise style preferences and common needs

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-PROF-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-010](../../product-backlog.md#pbi-010--set-and-revise-preferences-through-progressive-onboarding) |
| Domain | Personalization Profile |
| Theme | Personalization & Context |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Actionable Wardrobe Needs |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to set and revise my styles and common clothing needs, so that advice and Coverage use my declared priorities.

## 2. Value

Give first advice a relevant personal basis while keeping optional information and wardrobe setup under user control.

## 3. Scope

Initial progressive preference setup and later revision; controlled styles, six common needs, priority weights, separate current occasion and source-supported accepted/canceled/error outcomes.

## 4. Preconditions

- The user has usable authorized access to their personalization profile; initial unset information is permitted.

## 5. Acceptance Criteria

### AC-01 — Set or revisit preferences

Show current or unset styles/common needs and support submission of permitted choices in onboarding or later profile revision. Unset data is absent context, not an inferred persona or mandatory wardrobe checklist.

### AC-02 — Use canonical choices

Accept the controlled style vocabulary and six need dimensions/mapping in SRS Sections 4.10/3.14.1, with Not Relevant=0, Low=1, Medium=2 and High=3. Unsupported values are explained for correction without saving invented replacements.

### AC-03 — Separate common and current context

A current outfit request occasion may differ from ongoing needs and does not automatically overwrite their priorities; accepted common-priority changes remain available to subsequent contextual Coverage.

### AC-04 — Preserve progressive entry

Follow the source progression from account entry through style/common needs, optional personal context, location and recommended first garment to Today/Wardrobe. Omitted optional context or no first garment does not block authenticated entry; later completion is available.

### AC-05 — Reflect accepted changes

Accepted styles/priorities become the user's current context for relevant later ranking and assessment. Affected prior assessments are refreshed or marked outdated; Not Relevant needs are not silently replaced by positive defaults.

### AC-06 — Cancel or recover safely

Leaving an unsubmitted revision or known rejection/failure keeps the last accepted profile. An uncertain update is not called saved; the user can review actual context or retry, and unusable/wrong-account access cannot change the profile.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-004. Useful sequencing: PBI-003, PBI-006; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Optional body/gender lifecycle is [US-PROF-002](../profile/US-PROF-002-control-optional-personal-context.md); actual location/weather acquisition is [US-CTX-001](../context/US-CTX-001-choose-location-and-recover-limited-context.md). Ranking and Coverage computation remain their owning stories. No new persona capsules or category restrictions are added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-010](../../product-backlog.md#pbi-010--set-and-revise-preferences-through-progressive-onboarding); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-010, BR-012, CAP-01, CAP-06 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01, FEAT-PROF-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-PROF-001–FR-PROF-004, FR-PROF-008; FR-AUTH-011; DATA-AUTH-003; UI-002; FR-ANL-014 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-PROF-002, BRULE-GAR-004, BRULE-COV-001, BRULE-COV-002, BRULE-COV-006 |
| Use Cases | [UC-005](../../../03-requirements/use-cases/UC-005-update-personalization-profile.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The choices and priority policy are established. Initial defaults/presentation must not invent preferences; mobile handoff feasibility remains focused review Q-02.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
