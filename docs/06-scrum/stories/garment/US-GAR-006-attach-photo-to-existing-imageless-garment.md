# US-GAR-006 — Attach a photo to an existing imageless garment

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-006 |
| Version | 0.1 |
| Status | Draft (formalized 2026-11-17; acceptance criteria documented under delegated refinement) |
| Product | CapsuleAI |
| Parent PBI | [PBI-049](../../product-backlog.md#pbi-049--attach-a-photo-to-an-existing-imageless-garment) |
| Domain | Garment |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding |
| Sprint | Selected for Sprint 4 by the Product Owner ([Sprint 4 plan](../../sprints/sprint-04/sprint-plan.md#65-us-gar-006--attach-a-photo-to-an-existing-garment-provisional)) |
| Authority | The Product Owner selected this outcome for Sprint 4 and, on 2026-11-16, approved its solution decisions S4-GAR-04, S4-GAR-05, S4-GAR-06 and S4-INT-01–03. The detailed acceptance criteria below were formalized on 2026-11-17 from the plan's six provisional expectations under the Product Owner's delegation ([OPEN-S4-09](../../sprints/sprint-04/implementation-guides/open-questions.md#open-s4-09--provisional-attachment-acceptance-criteria)). **They have not been separately approved by the Product Owner**; PO acceptance is part of refinement before Done. |
| Revision note | 2026-11-17: initial formal story, created during the Sprint 4 documentation synchronization. Before this date the story existed only as provisional plan text (Sprint 4 plan §6.5). No implementation, estimate or Done status is claimed. |

## 1. Story

As an authenticated owner, I want to attach a permitted photo to an existing garment that currently has no photo, so that I can make my wardrobe visually recognizable without recreating that garment.

## 2. Value

Owners who first saved garments manually, or whose photo was not available at the time, can add the missing picture later. They keep the garment's confirmed information, creation time and history, and avoid deleting and re-entering it.

## 3. Scope

- One photo for one existing garment of the owner that is **current** (not removed) and **has no photo**.
- The photo is staged through the existing private photo intake ([US-GAR-003](US-GAR-003-add-photo-supported-garment.md)): the same supported formats, size and dimension limits, normalization and private storage.
- The owner reviews the staged photo and the target garment and explicitly confirms the attachment.
- The attachment changes only the garment's photo. All 13 confirmed properties, the creation time and the creation identity stay unchanged.

## 4. Preconditions

- The owner is authenticated with a usable session.
- The selected garment is the owner's current garment without a photo.
- The owner has a staged photo in the **ready** state that has not expired and has not been used for another garment. Staged photos expire after 24 hours, as for photo-supported creation.

## 5. Acceptance Criteria

Each criterion below corresponds to one provisional expectation of the Sprint 4 plan. The original provisional name is shown in brackets.

### AC-01 — Attach only to an eligible garment [Eligibility]

Only the authenticated owner's current garment without a photo can receive a photo. A garment of another account cannot be found or changed. A removed garment answers that it is no longer current. A garment that already has a photo is refused, and its photo is never replaced. Attaching is never offered as a way to create a garment.

### AC-02 — Confirm or cancel deliberately [Confirm / cancel]

The owner sees the staged photo and the target garment before confirming. Only an explicit confirmation attaches the photo; the request also carries the garment revision the owner reviewed. Cancelling before confirmation leaves the garment unchanged. The staged photo then follows the normal pending lifecycle and expires after 24 hours; reviewing it does not extend that time.

### AC-03 — Attach atomically without changing confirmed information [Atomicity / authority]

An accepted attachment happens as one indivisible change: the staged photo is claimed, linked to exactly this garment and the garment's revision advances by one. Category, Color and all other confirmed properties, the creation time and the creation identity do not change. If any check fails, nothing is attached and the staged photo is not consumed.

### AC-04 — Recover from retries and uncertainty [Retry / uncertainty]

When the owner repeats the same confirmation (the same garment and the same photo) after a lost response, the outcome is the same attached garment, without a second revision or a duplicate association. A different photo is never attached over an existing one. If another accepted change happened first (an edit, a removal or another attachment), the owner is told about the conflict and can review the current garment; the other change is not overwritten or undone.

### AC-05 — Keep photos private and report unavailability truthfully [Privacy / availability]

The attached photo is delivered only through the application to its authenticated owner; there is no public link. If the stored photo becomes unavailable after an accepted attachment, the owner sees a truthful photo limitation, and the garment's other information stays readable.

### AC-06 — Stay consistent under cleanup and competing changes [Cleanup / races]

Unclaimed, cancelled or expired staged photos are removed by the existing pending-photo cleanup. An attached photo is never removed by that cleanup. Competing attachments, an attachment against a removal, and an attachment against an edit each have one coherent outcome. A rejected attachment leaves no partial association. When the garment is later removed, its attached photo is deleted within the 30-day removal deadline of [US-GAR-005](US-GAR-005-remove-current-garment-preserve-minimal-history.md).

## 6. Validation, Concurrency, Retry, Security and Recovery

| Concern | Expected behavior |
| --- | --- |
| Validation | Confirmation, the reviewed garment revision and the staged-photo identifier are required. A missing or malformed value is reported field by field before any garment or photo is looked up. |
| Photo validity | The photo was validated when it was staged (format, 15 MB limit, 512-pixel shortest side, normalization). Attachment reuses that validated, normalized photo; it does not upload, decode or analyze it again. |
| Concurrency | The garment is protected before the photo, the same order used by removal and bulk import (S4-INT-01). Two photos for one garment: one wins, the other is told the garment already has a photo. One photo for two garments: one wins, the other is told the photo is no longer usable. |
| Retry | The natural outcome identifies the action: the same garment-and-photo pair already attached answers with the current garment and no new revision. No separate receipt is stored (S4-INT-02). |
| Security | The owner comes from the authenticated session. Another account's garment or staged photo is indistinguishable from an unknown one. No photo bytes or identifiers are written to logs. |
| Recovery | After a lost response, repeat the same confirmation or review the garment. After a conflict, reread the garment and the staged photo. A temporary failure changes nothing and can be retried. |

## 7. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria above. Relevant automated, real integration, concurrency, client and other triggered evidence must establish the outcome; drafted AC or backend checks alone do not establish Done. The proposed backend evidence is in the [US-GAR-006 implementation guide](../../sprints/sprint-04/implementation-guides/US-GAR-006-guide.md), §13 and §15.

## 8. Dependencies

- Parent PBI capability inputs: PBI-007 (an owned current garment and its detail) and PBI-012 (private photo staging, delivered in Sprint 3).
- Useful sequencing: [US-GAR-005](US-GAR-005-remove-current-garment-preserve-minimal-history.md), whose lock order and removal cleanup this story reuses. This is not a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 9. Out of Scope

Replacing or removing a garment's photo, several photos per garment, AI analysis or automatic changes to garment properties from the photo, creating a garment, and bulk attachment. Photo-supported creation is [US-GAR-003](US-GAR-003-add-photo-supported-garment.md); bulk import is [US-BULK-001](../bulk-import/US-BULK-001-review-and-confirm-bulk-garment-import.md).

## 10. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-049](../../product-backlog.md#pbi-049--attach-a-photo-to-an-existing-imageless-garment) |
| Business | [BRD](../../../01-business/BRD.md): BR-005, BR-001, BR-021, CAP-03 (supporting context; no attachment-specific business requirement) |
| Product | [PRD](../../../02-product/PRD.md#142-feat-war-002--edit-and-remove-garments): JRN-03, FEAT-WAR-002 behavior 7 and AC-08–AC-09 (added 2026-11-17) |
| SRS | [SRS](../../../03-requirements/SRS.md#36-wardrobe-management): FR-WAR-009 (added 2026-11-17); DATA-GAR-004; FR-AI-012; DATA-INT-004; NFR-PRIV-001, NFR-PRIV-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md#brule-gar-011--existing-garment-photo-attachment): BRULE-GAR-011 (added 2026-11-17), BRULE-GAR-001, BRULE-GAR-009, BRULE-AUTH-001, BRULE-AUTH-007 |
| Use Cases | [UC-009](../../../03-requirements/use-cases/UC-009-edit-garment.md), alternative flow A4 and exception flow E5 (added 2026-11-17) |
| Activity Diagrams | No dedicated activity is required; UC-009 governs the behavior. |
| Sprint 4 decisions | [S4-GAR-04, S4-GAR-05, S4-GAR-06, S4-INT-01–03](../../sprints/sprint-04/sprint-plan.md#13-consolidated-decision-register) (approved 2026-11-16); delegated details OPEN-S4-07–10, OPEN-S4-31, OPEN-S4-32 |
| Completion / Verification | [Definition of Done](../../definition-of-done.md); [Test Strategy](../../../07-testing/test-strategy.md) |

## 11. Open Refinement Notes

- **PO acceptance of the formalized AC.** AC-01–AC-06 restate the plan's six provisional expectations as testable criteria under delegation. The Product Owner should accept or amend them during refinement.
- **Client evidence.** The mobile flow (offer attachment only for imageless garments, preview and confirm the target, cancel, show conflicts and uncertain outcomes) is not yet evidenced.
- **Storage evidence.** The deletion of an attached photo after removal depends on the storage-versioning evidence required for US-GAR-005 (OPEN-S4-05).

## 12. Status

**Draft.** Formalized on 2026-11-17 for traceability. The implementation is proposed in the Sprint 4 guide and has not been applied to the repository. No approval of the detailed AC, readiness, estimate or implemented outcome is claimed.
