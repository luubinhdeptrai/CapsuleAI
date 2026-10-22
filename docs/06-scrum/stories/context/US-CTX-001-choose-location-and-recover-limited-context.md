# US-CTX-001 — Choose environmental context with safe fallback

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-CTX-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-011](../../product-backlog.md#pbi-011--choose-environmental-context-with-manual-and-limited-context-recovery) |
| Domain | Environmental Context |
| Theme | Personalization & Context |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Easier Outfit Decisions; Actionable Wardrobe Needs |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want to choose location through permission or manual selection and understand missing weather, so that advice can be relevant without mandatory device access.

## 2. Value

Improve context relevance without requiring device-location permission or a successful weather provider.

## 3. Scope

Device/manual/skip selection, actual location/weather basis, freshness, bounded acquisition, context change and limited-context recovery.

## 4. Preconditions

- The user has authorized access to personal context; device permission and available weather are not prerequisites to opening setup.

## 5. Acceptance Criteria

### AC-01 — Offer permission-independent choices

Explain location purpose and allow consented device access, independent manual city/location, or skip. Denial/device failure retains manual and skip choices and does not deny authenticated entry.

### AC-02 — Identify actual evidence

Show the selected location and available environmental information with the applicable retrieval/observation timestamp actually used; a selected city alone is not a known forecast.

### AC-03 — Respect weather freshness

Weather age exactly 30 minutes is current; older information is refreshed before current use. An external weather request may delay an assessment by at most 2 seconds.

### AC-04 — Disclose bounded fallback

If usable current weather is not obtained within that boundary, disclose unavailable environmental context and skip only unavailable environmental hard filtering where appropriate. Remaining validity still applies; no invented forecast or blanket rejection of all outfits.

### AC-05 — Handle accepted changes

Accepted location/environment changes inform later relevant advice and trigger reevaluation or an explicit outdated indication for affected results. Failed refresh never makes prior advice current for a new selection.

### AC-06 — Protect accepted context

Known rejected/failed changes retain the last accepted selection; uncertain changes require outcome review rather than a saved claim. Wrong-account/unusable access cannot inspect or change private context.

### AC-07 — Minimize external exchange

Weather retrieval receives only the selected location necessary for its purpose, not unrelated wardrobe/profile/history. Continuous background tracking and permission as a prerequisite are not introduced.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-004.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Recommendation, Coverage and Multiplier consume this context but do not acquire weather separately. No provider, location precision, polling schedule or new timeout is selected.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-011](../../product-backlog.md#pbi-011--choose-environmental-context-with-manual-and-limited-context-recovery); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-007, BR-013, CAP-01, CAP-05 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01, JRN-04, FEAT-PROF-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WEATHER-001–FR-WEATHER-005; SI-002, HW-003; ERR-WEATHER-001; COM-003 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-PROF-003, BRULE-OUT-006, BRULE-COV-006, BRULE-MULT-008 |
| Use Cases | [UC-006](../../../03-requirements/use-cases/UC-006-set-location-context.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-006](../../../03-requirements/activity-diagrams/AD-006-set-location-context.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Provider and supported-city realization details remain JIT. Developers establish relevant delivery/test access before selecting dependent work; no new weather policy is needed.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
