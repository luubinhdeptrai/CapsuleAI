# US-MOB-001 — Use a coherent Vietnamese mobile journey foundation

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Enabler Story |
| Story ID | US-MOB-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-003](../../product-backlog.md#pbi-003--usable-vietnamese-mobile-journey-foundation) |
| Domain | Mobile Experience |
| Theme | Foundation & Access |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Establish the shared supported mobile entry, navigation and accessible state meanings through which delivered wardrobe capabilities become usable.

## 2. Value

Give the Vietnam-first audience a coherent, understandable route through wardrobe value.

## 3. Scope

Shared Today/Wardrobe/Insights/Profile responsibilities, prominent Add Garment, applicable detail/return paths, Vietnamese semantic labels and accessible interaction. Connect destinations as their capabilities arrive.

## 4. Preconditions

- Identify the capabilities actually available and the supported Android/iOS validation environment; backend-only tests do not establish this outcome.

## 5. Acceptance Criteria

### AC-01 — Reach delivered value

On Android 10+ and iOS 15+, users can reach the applicable Today, Wardrobe, Insights and Profile responsibilities and Add/detail/return routes for delivered capabilities. Navigation names do not imply that a future feature is implemented.

### AC-02 — Enter progressively

Account entry can continue without body/gender, device-location permission, Display Name or a first garment. Optional setup and recommended garment addition are explained without a quota; available later setup routes preserve that distinction.

### AC-03 — Understand state

Vietnamese loading, empty, no-match, pending, confirmed, failed and applicable limited/outdated/hypothetical states have correct meanings and useful source-supported actions; unavailable data is not presented as empty or complete.

### AC-04 — Operate accessibly

Applicable interactive elements have meaningful accessible names/roles/states and remain operable with TalkBack/VoiceOver and text scaling up to 200%; primary touch targets follow approximately 48 dp Android/44 pt iOS minima.

### AC-05 — Preserve semantic meaning

Labels communicate important states beyond color/images; imageless garments remain identifiable, Vietnamese text/diacritics round-trip accurately, and translated labels do not redefine canonical domain values. Later-language compatibility adds no second MVP UI language.

### AC-06 — Demonstrate actual integration

Representative delivered entry/navigation/recovery journeys are verified on the supported client with the real collaborating core where required. Record substituted boundaries and limitations; an inaccessible previous-account view is not exposed during navigation or account change.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: None.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Feature-specific flows remain in their product stories. PBI-036/PBI-037 extend accessibility/usability evidence. No mobile framework, pixel layout, offline guarantee or future feature completion is selected here.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-003](../../product-backlog.md#pbi-003--usable-vietnamese-mobile-journey-foundation); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-023, BR-022 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; Sections 7 and 21 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-011; UI-001–UI-009; LOC-001–LOC-005; NFR-ACC-001, NFR-ACC-002, NFR-USE-001; NFR-SEC-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-PROF-001 |
| Use Cases | [UC-001](../../../03-requirements/use-cases/UC-001-register-account.md); [UC-002](../../../03-requirements/use-cases/UC-002-log-in.md); [UC-005](../../../03-requirements/use-cases/UC-005-update-personalization-profile.md); [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); [UC-008](../../../03-requirements/use-cases/UC-008-view-wardrobe.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

Developers must establish mobile delivery skills/toolchain and Android/iOS verification access, with PO/BA on coherent handoffs (focused review Q-02). This draft does not settle a framework or claim mobile implementation exists.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
