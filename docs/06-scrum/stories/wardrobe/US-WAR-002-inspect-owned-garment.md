# US-WAR-002 — Inspect an owned garment

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Version | 0.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-007](../../product-backlog.md#pbi-007--browse-current-wardrobe-and-inspect-owned-garments) |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Trustworthy Wardrobe Understanding](../../product-goal.md#4-desired-product-outcomes) |

## 1. Story

As an authenticated user, I want to inspect an owned garment's accepted information and applicable readiness, so that I can understand what CapsuleAI knows about that garment and the limits of advice based on it.

## 2. User Value

Inspection explains one possession rather than the collection as a whole. Accepted profile, readiness and qualified use information belong together because they establish what can be trusted about the selected garment.

## 3. Scope

Read-only detail for a selected current confirmed garment, including available image/profile/provenance/uncertainty, evaluation-specific readiness and supported recorded-use information when that capability is available. Includes loading, retrieval failure, changed ownership, authorization and return to browsing.

## 4. Preconditions

- I have authenticated access authorized for the selected garment's wardrobe.
- Successful current detail requires a selected confirmed active owned garment. Missing images/rich fields do not disqualify it; a changed or no-longer-current selection is handled within the story.
- Wear reporting, utilization analytics and completed optional personalization are not prerequisites to inspect accepted garment information.

## 5. Acceptance Criteria

### AC-01 — Inspect the correct accepted profile

**Given** I select a current confirmed garment and authorized detail retrieval succeeds

**When** CapsuleAI presents the detail

**Then** it identifies the selected garment and displays its current accepted category, dominant color and available supported profile information, with the appropriate available image. Applicable useful provenance/uncertainty distinguishes how information was established from its certainty; a proposal is not silently presented in place of accepted user values.

### AC-02 — Inspect an imageless or incomplete entry

**Given** the selected current garment has no image or has only the confirmed Saveable minimum

**When** detail retrieval succeeds

**Then** it remains identifiable and inspectable through accepted information. Missing image, unavailable descriptors and missing readiness retain their distinct meanings; no image, material, color or other descriptor is fabricated to make the profile look complete.

### AC-03 — Explain common readiness information

**Given** a saved garment has UNKNOWN/unavailable information needed for a common compatibility check

**When** I inspect its readiness guidance

**Then** CapsuleAI explains the missing required information for that check. Primary Category, Dominant Color, Pattern Type and Climate/Season Suitability are common readiness requirements; the garment remains owned/Saveable, while unavailable required information does not establish readiness for decisions needing it.

### AC-04 — Apply layering and bulk only where required

**Given** the selected garment's category and contemplated compatibility decision determine whether Layering Level or Bulk Index is required

**When** CapsuleAI explains applicable readiness

**Then** TOP/OUTERWEAR require applicable Layering Level in addition to common information, and layered compatibility requires Bulk Index where bulk matters. Missing required information is identified for the affected decision; bulk is not presented as universally required to save or use every garment.

### AC-05 — Keep optional information and validity distinct

**Given** the garment satisfies the applicable readiness information but lacks subtype or optional rich descriptors

**When** I inspect its readiness guidance

**Then** missing strongly recommended subtype or non-universal Fit, Material, Silhouette, Secondary Colors, precise HEX/HSL display, Style Tags or Occasion Tags does not become a universal readiness blocker. Any ready indication concerns applicable information only and does not guarantee that every outfit containing the garment is valid.

### AC-06 — Qualify available reported-use information

**Given** supported reported-use information is available for this garment

**When** it is displayed in detail

**Then** indicators reflect accepted effective Wear Events and their reporting limits. No recorded events is described as no recorded use rather than proof of never having physically worn the garment; information is not fabricated from missing reports. This presentation consumes available evidence and does not implement reporting or utilization analytics.

### AC-07 — Keep unavailable use evidence distinct

**Given** reported-use capabilities/evidence are not yet available or cannot be retrieved, but accepted garment detail is available

**When** I inspect the garment

**Then** CapsuleAI retains usable profile/readiness inspection and does not manufacture zero use, last-worn information or a no-recorded-events conclusion from unavailable evidence. Availability of future reporting/analytics is not a gate to viewing this garment.

### AC-08 — Distinguish detail loading and retrieval failure

**Given** detail retrieval is pending or fails

**When** I review the selected-garment outcome

**Then** CapsuleAI distinguishes loading from unavailable detail, offers applicable retry or return to usable prior context on failure, and does not call the wardrobe empty or the selected garment deleted merely because retrieval failed. Accepted wardrobe information remains unchanged.

### AC-09 — Handle a selection that is no longer current

**Given** the garment changed or was removed from current ownership after selection

**When** CapsuleAI establishes the changed current state during inspection

**Then** it explains the changed/no-longer-current item and refreshes the current wardrobe where possible, allowing return to browsing. A minimal historical snapshot stays separate and does not restore current ownership; this story does not perform removal or implement historical views.

### AC-10 — Inspect and return without mutation

**Given** usable detail is presented

**When** I inspect it, return to browsing or leave without a separate maintenance action

**Then** CapsuleAI offers the return path and inspection alone creates no addition, edit, removal, Wear Event or preference feedback. Accepted values and ownership remain unchanged by viewing.

### AC-11 — Protect detail when authorized access is lost

**Given** access expires, is revoked or is not authorized for the affected garment

**When** I open or continue inspecting it

**Then** CapsuleAI pauses protected use, explains the need for usable authorized access and exposes no other account's garment information. It does not present inaccessible detail as an empty wardrobe; inspection can resume for the correct account after valid authentication.

### AC-12 — Understand the detail and its limits

**Given** I use the Vietnamese detail interaction with a screen reader or text scaled up to 200%

**When** I inspect accepted information/readiness, qualified use information, failure guidance or the return action

**Then** the information, state meanings and actions remain operable and understandable through meaningful accessible names/roles/states beyond image or color alone. Saved ownership, missing readiness, uncertain descriptors and no-recorded/unavailable use evidence remain distinguishable.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-GAR-001–BRULE-GAR-003 preserve authorized ownership, authoritative accepted values and evaluation-specific readiness; BRULE-GAR-006 prohibits invented compatibility information.
- BRULE-HIST-001 limits use indicators to reported evidence; BRULE-HIST-002 prevents historical snapshots from becoming current owned detail. Confirmation is distinct from verified physical material composition.
- The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-004's authenticated access; [US-AUTH-002](../auth/US-AUTH-002-log-in.md) and [US-AUTH-003](../auth/US-AUTH-003-resume-session.md) supply access/continuity where applicable.
- [US-WAR-001](US-WAR-001-browse-current-wardrobe.md) supplies the collection selection/return route. The actual detail input is a current owned garment; no single creation method is a hard dependency.
- PBI-006 / [US-GAR-001](../garment/US-GAR-001-add-garment-manually.md) supply useful minimum-only/imageless examples. PBI-008 supplies later enrichment; neither is a new mandatory completion gate for already available detail.
- Reported-use presentation integrates with available evidence as its owning capabilities arrive (PBI-021/PBI-025); those capabilities are not hard prerequisites to profile/readiness inspection.

## 8. Out of Scope

Collection browsing is US-WAR-001. Rich descriptor entry/enrichment and editing are PBI-008; search/filtering is PBI-015; removal and retention enforcement are PBI-016. Photo/AI entry/proposal review remain PBI-012/PBI-013. Wear reporting/history and utilization calculations/insights remain their owning PBIs, including PBI-021/PBI-025. No new reporting action, analytics event, restore action, image generation, recommendation algorithm or implementation design is introduced.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-007](../../product-backlog.md#pbi-007--browse-current-wardrobe-and-inspect-owned-garments) |
| Business | [BRD](../../../01-business/BRD.md): BR-003, BR-005, BR-006, BR-021, BR-022; CAP-03, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-03; FEAT-WAR-001, FEAT-GAR-001 (accepted profile/readiness); FEAT-ANL-001, available indicators only |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WAR-001, FR-WAR-002, FR-GAR-004, FR-GAR-005, FR-GAR-006, FR-AUTH-008; DATA-GAR-001, DATA-GAR-003, DATA-GAR-004, DATA-GAR-005; UI-004; ERR-GAR-002, ERR-AUTH-001, ERR-NET-001; NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-007; BRULE-GAR-001, BRULE-GAR-002, BRULE-GAR-003, BRULE-GAR-006; BRULE-HIST-001, BRULE-HIST-002 |
| Use Cases | [UC-008 — View Wardrobe](../../../03-requirements/use-cases/UC-008-view-wardrobe.md), selected detail/readiness/use/return and failure branches only |
| Activity Diagrams | No current dedicated Activity Diagram for UC-008; the textual specification governs this slice. |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

