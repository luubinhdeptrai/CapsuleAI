# US-GAR-003 — Add a photo-supported garment with manual confirmation

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-003 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-012](../../product-backlog.md#pbi-012--add-a-photo-supported-garment-with-manual-confirmation) |
| Domain | Garment |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated owner, I want to attach a permitted garment photo and confirm its information manually, so that the wardrobe is visually recognizable without depending on successful AI.

## 2. Value

Make owned clothing recognizable visually while preserving manual entry independently of automated analysis.

## 3. Scope

Camera/library alternatives, supported input validation, replacement/cancellation and a useful photo-supported confirmed garment; imageless manual continuity remains available.

## 4. Preconditions

- The user has authorized wardrobe access. Only the chosen image route needs its applicable permission; analysis success is not required.

## 5. Acceptance Criteria

### AC-01 — Choose a permitted route

Offer capture or photo-library selection with lighting/visibility guidance. Camera denial/unavailability does not remove permitted library or imageless manual entry; library input does not require camera permission.

### AC-02 — Validate supported input

Accept supported JPEG/JPG, PNG and HEIC/HEIF at or below 15 MB with shortest dimension at least 512 pixels. Explain unsupported or out-of-limit input, including 511-pixel and above-15-MB cases, with replacement/manual continuation. A non-plain background alone does not invalidate input.

### AC-03 — Review or cancel selection

Show the selected input, allow replacement or cancellation before confirmation, and keep image/proposal work distinct from a confirmed wardrobe addition.

### AC-04 — Confirm a useful garment

After explicit confirmation of the saveable category/dominant-color minimum, associate the available image with the correct owned garment. An image alone creates no confirmed item; richer fields and AI success are not required to save.

### AC-05 — Preserve authority and readiness

The user's confirmed information remains authoritative; unknown applicable compatibility information retains its meaning and can leave an accepted owned item unready rather than failed or fabricated.

### AC-06 — Recover safely

Canceled preconfirmation work creates no confirmed item. Known failed saves are not success; interrupted/uncertain acceptance permits review and safe same-action retry without duplicate accepted additions. Missing media does not hide otherwise available accepted garment information.

### AC-07 — Protect private media

Only authorized ownership/session access permits the relevant image and garment operations; denied access creates no wardrobe mutation or private disclosure. Optional measurement failure does not reverse accepted creation.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-006.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Imageless manual entry is [US-GAR-001](../garment/US-GAR-001-add-garment-manually.md); processed preview/proposals and confidence are [US-GAR-004](../garment/US-GAR-004-review-ai-assisted-garment-proposals.md). No upload-only product outcome, mandatory white background, new format, AR/3D or image-delivery design is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-012](../../product-backlog.md#pbi-012--add-a-photo-supported-garment-with-manual-confirmation); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-01, BR-001, BR-004, CAP-02, CAP-03 |
| Product | [PRD](../../../02-product/PRD.md): JRN-02, FEAT-AI-001, FEAT-AI-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AI-001–FR-AI-004, FR-AI-008–FR-AI-012; DATA-GAR-004; HW-001, HW-002; DATA-INT-004; ERR-AI-001; COM-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-001–BRULE-GAR-003, BRULE-AUTH-001 |
| Use Cases | [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The input boundaries are fixed by SRS Section 3.4.1. Permission/media realization and actual device evidence remain delivery work; no new image quota or technical save design is specified.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
