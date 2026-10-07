# US-GAR-001 — Add an owned garment manually

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Version | 0.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-006](../../product-backlog.md#pbi-006--add-a-confirmed-garment-manually) |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Trustworthy Wardrobe Understanding](../../product-goal.md#4-desired-product-outcomes) |

## 1. Story

As an authenticated user, I want to enter and confirm an owned garment manually, so that I can build a trustworthy wardrobe even without an image or available AI assistance.

## 2. User Value

A minimum-only manual entry provides owned inventory immediately. Review, confirmation and safe save recovery remain together because entered information has no owned-garment value until creation is accepted.

## 3. Scope

Imageless manual entry of the supported Primary Category and Dominant Color, review/correction of entered information, explicit confirmation, cancellation, accepted creation and safe failed/uncertain-save handling. Explain applicable Recommendation-Readiness after acceptance without implementing rich-descriptor enrichment.

## 4. Preconditions

- I have authenticated access authorized for my wardrobe.
- Existing garments, an image, successful automated analysis and completed optional personalization are not prerequisites.

## 5. Acceptance Criteria

### AC-01 — Create a minimum-confirmed manual garment

**Given** I have authorized access and choose imageless manual entry

**When** I enter a supported Primary Category and Dominant Color, review them, explicitly confirm the minimum and submit, and creation is accepted

**Then** CapsuleAI acknowledges one confirmed owned garment with the accepted values, makes it available in my current wardrobe, and explains applicable readiness separately from successful saving.

### AC-02 — Continue independently of image and AI

**Given** I choose manual entry without an image, including while image analysis is unavailable

**When** I review and confirm the minimum and accepted creation completes

**Then** the garment is saved through the same manual outcome without requiring image capture, upload, preview, AI proposals or successful analysis.

### AC-03 — Review and correct before confirmation

**Given** I have entered category/color but have not submitted confirmed creation

**When** I review the values and change either value

**Then** the review reflects the latest supported values and permits explicit confirmation of that revised minimum. Merely entering or changing a draft does not establish a confirmed owned garment.

### AC-04 — Use the established classification and color choices

**Given** I enter the manual minimum

**When** I select category/color information

**Then** Primary Category is restricted to TOP, BOTTOM, OUTERWEAR and FOOTWEAR; Dominant Color uses the bounded SRS color-family vocabulary. OTHER/UNKNOWN subtype fallbacks do not create a fifth primary category or make a subtype universally mandatory.

### AC-05 — Correct a missing or unconfirmed minimum

**Given** Primary Category or Dominant Color is missing or not confirmed

**When** I attempt to submit the garment

**Then** CapsuleAI identifies the missing confirmation/information and retains the draft for correction. No authoritative garment is created, and existing wardrobe information remains unchanged until accepted creation.

### AC-06 — Save without rich descriptors

**Given** I explicitly confirm the minimum but omit subtype or other rich descriptors

**When** creation is accepted

**Then** the entry remains Saveable and owned. Missing subtype, Fit, Material, Silhouette, Secondary Colors, precise HEX/HSL display, Style Tags or Occasion Tags does not become a universal save blocker.

### AC-07 — Explain missing common compatibility information

**Given** the accepted garment meets the minimum but a required common compatibility field, such as Dominant Color, Pattern Type or Climate/Season Suitability, is UNKNOWN or unavailable

**When** CapsuleAI presents the post-save readiness explanation

**Then** it identifies the missing common information and explains that decisions requiring it cannot treat the garment as ready. The owned entry is retained; missing values are not fabricated and accepted saving is not presented as failed.

### AC-08 — Explain applicable layering and bulk requirements

**Given** the accepted garment lacks applicable Layering Level for TOP/OUTERWEAR or Bulk Index needed for a layered compatibility decision

**When** CapsuleAI explains readiness

**Then** it identifies the applicable missing information for the affected decision. Layering is applied to TOP/OUTERWEAR and Bulk Index only where layered compatibility requires it; unavailable required information cannot pass that check, while bulk is not a universal saving/readiness gate.

### AC-09 — Retain user authority

**Given** my manual creation is accepted with entered-and-confirmed values

**When** I inspect the accepted result or later automated information becomes available

**Then** the accepted values remain authoritative for my garment. Provenance reflects how those values were established where relevant, and automated output cannot silently replace them; this story does not require automated analysis.

### AC-10 — Cancel unconfirmed work

**Given** I am reviewing an entry and have not submitted confirmed creation

**When** I cancel or leave the preconfirmation work

**Then** no confirmed wardrobe entry is created from that draft and my previously accepted garments remain unchanged.

### AC-11 — Handle a known failed save

**Given** I submitted the confirmed minimum but creation is known not to have been accepted

**When** saving fails

**Then** CapsuleAI distinguishes failure from successful creation, retains useful entered work where practical, and offers a safe continuation/retry. No confirmed garment is claimed and existing accepted garments remain unchanged.

### AC-12 — Resolve an uncertain save before retry

**Given** I submitted creation but interrupted communication leaves acceptance unconfirmed

**When** I review the outcome or continue the intended addition

**Then** CapsuleAI distinguishes pending/unconfirmed acceptance from known failure and success, preserves useful work where practical, and lets me establish whether creation was accepted before safe retry. One accepted addition does not become unexplained duplicate entries; uncertainty does not establish definite unchanged inventory.

### AC-13 — Respect authorized ownership during entry

**Given** access expires, is revoked, or is not authorized for the affected wardrobe before or during the interaction

**When** I attempt to continue protected entry or review an interrupted submission

**Then** CapsuleAI pauses protected use, explains the need for usable authorized access, and exposes no other user's wardrobe. It does not label inaccessible data empty or an interrupted submission accepted; the goal can resume after valid authentication with outcome review where acceptance is uncertain.

### AC-14 — Understand and operate manual confirmation

**Given** I use the Vietnamese manual-entry interaction with a screen reader or text scaled up to 200%

**When** I review/correct the minimum, confirm, or encounter missing-field/save/readiness guidance

**Then** the fields, correction and confirmation actions and result states remain operable and understandable through meaningful labels/states beyond color alone. Guidance distinguishes draft, saved ownership, failed/unconfirmed save and applicable readiness.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-GAR-001–BRULE-GAR-006 preserve authorized ownership, entered-and-confirmed authority, the Saveable minimum, applicable readiness and bounded vocabularies.
- Confirmation establishes canonical information; it does not prove physical composition or make unknown compatibility information valid. Rich optional fields stay optional; readiness is not a guarantee of a valid outfit.
- The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-004's usable authenticated access; [US-AUTH-002](../auth/US-AUTH-002-log-in.md) and [US-AUTH-003](../auth/US-AUTH-003-resume-session.md) supply access and continuity where applicable.
- PBI-007's [US-WAR-001](../wardrobe/US-WAR-001-browse-current-wardrobe.md) and [US-WAR-002](../wardrobe/US-WAR-002-inspect-owned-garment.md) are useful inspection paths after acceptance; creating the minimum is not conditional on completing those whole stories.
- PBI-003 provides useful common interaction support, not an additional hard capability dependency. PBI-008 enriches descriptors later; PBI-012/PBI-013 add photo/AI routes. None is a prerequisite to manual creation.

## 8. Out of Scope

Rich descriptor entry/enrichment and later editing are PBI-008. Photo capture/selection, image validation and photo-supported entry are PBI-012; AI proposals, confidence and processed preview are PBI-013. General browsing/detail are PBI-007, measurement is PBI-009, and search/removal belong to PBI-015/PBI-016. No new quota, mandatory image/AI, inventory sorting, analytics event or implementation design is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-006](../../product-backlog.md#pbi-006--add-a-confirmed-garment-manually) |
| Business | [BRD](../../../01-business/BRD.md): BG-01; BR-002, BR-003, BR-004, BR-005; CAP-03, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-02; FEAT-AI-002, FEAT-GAR-001 (manual minimum and authority) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AI-003, FR-AI-008, FR-AI-009, FR-AI-010; FR-GAR-001, FR-GAR-002, FR-GAR-003, FR-GAR-004, FR-GAR-005, FR-GAR-006, FR-GAR-011; DATA-GAR-001, DATA-GAR-005; UI-004, UI-006; ERR-GAR-001, ERR-GAR-002; COM-002; NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001; BRULE-GAR-001, BRULE-GAR-002, BRULE-GAR-003, BRULE-GAR-004, BRULE-GAR-005, BRULE-GAR-006 |
| Use Cases | [UC-007 — Add Garment](../../../03-requirements/use-cases/UC-007-add-garment.md), manual alternative and shared confirmation/save flows only |
| Activity Diagrams | [AD-007 — Add Garment](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml), manual branch and shared confirmation/recovery only |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.
