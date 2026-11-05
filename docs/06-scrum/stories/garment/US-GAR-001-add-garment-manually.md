# US-GAR-001 — Add an owned garment manually

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-001 |
| Version | 0.4 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Revision note | 2026-10-21: first focused refinement; clarified source-backed acceptance/evidence and traceability; identity, scope and Baseline Draft status preserved. 2026-10-22: linked related full-backlog drafts and explicit identity/domain metadata; existing outcome and AC retained. |
| Parent PBI | [PBI-006](../../product-backlog.md#pbi-006--add-a-confirmed-garment-manually) |
| Domain | Garment |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Trustworthy Wardrobe Understanding](../../product-goal.md#4-desired-product-outcomes) |
| Product revision | 2026-11-05: Approved eight-category taxonomy and optional subtype/Brand/purchase Price synchronized. Existing story/AC IDs, parent PBI and Sprint 2 order retained; no implementation claimed. |

## 1. Story

As an authenticated user, I want to enter and confirm an owned garment manually, so that I can build a trustworthy wardrobe even without an image or available AI assistance.

## 2. User Value

A minimum-only manual entry provides owned inventory immediately. Review, confirmation and safe save recovery remain together because entered information has no owned-garment value until creation is accepted.

## 3. Scope

Imageless manual entry of the supported Primary Category and Dominant Color with optional subtype, Brand and purchase Price, review/correction of entered information, explicit confirmation, cancellation, accepted creation and safe failed/uncertain-save handling. Explain applicable Recommendation-Readiness after acceptance with the bounded optional fields approved for this story; other rich enrichment and later editing remain PBI-008.

## 4. Preconditions

- I have authenticated access authorized for my wardrobe.
- Existing garments, an image, successful automated analysis and completed optional personalization are not prerequisites.

## 5. Acceptance Criteria

### AC-01 — Create a minimum-confirmed manual garment

**Given** I have authorized access and choose imageless manual entry

**When** I enter a supported Primary Category and Dominant Color, review them, explicitly confirm the minimum and submit, and creation is accepted

**Then** CapsuleAI acknowledges one confirmed owned garment with the accepted minimum and any supplied valid subtype, Brand and purchase Price, makes it available in my current wardrobe, and explains applicable readiness separately from successful saving.

### AC-02 — Continue independently of image and AI

**Given** I choose manual entry without an image, including while image analysis is unavailable

**When** I review and confirm the minimum and accepted creation completes

**Then** the garment is saved through the same manual outcome without requiring image capture, upload, preview, AI proposals or successful analysis.

### AC-03 — Review and correct before confirmation

**Given** I have entered category/color and any optional subtype, Brand or Price but have not submitted confirmed creation

**When** I review the values and change an entered value

**Then** the review reflects the latest supported values and permits explicit confirmation of that revised minimum. Merely entering or changing a draft does not establish a confirmed owned garment.

### AC-04 — Use the established classification and color choices

**Given** I enter the manual minimum

**When** I select category/color information

**Then** Primary Category is restricted to TOPS, DRESSES_JUMPSUITS, PANTS, SKIRTS, OUTERWEAR, SHOES, BAGS, HEADWEAR. Dominant Color keeps the bounded SRS vocabulary. Optional subtype must match its category; DRESSES_JUMPSUITS offers exactly the ten display/code pairs in PRD Section 13.2. OTHER/UNKNOWN is accepted only where defined. Reject an unsupported pair with correction/clear guidance, not a silent category change; omission remains valid for all eight categories.

### AC-05 — Correct a missing or unconfirmed minimum

**Given** Primary Category or Dominant Color is missing or not confirmed

**When** I attempt to submit the garment

**Then** CapsuleAI identifies the missing confirmation/information and retains the draft for correction. No authoritative garment is created, and existing wardrobe information remains unchanged until accepted creation.

### AC-06 — Save without rich descriptors

**Given** I explicitly confirm the minimum but omit subtype, Brand, Price or other rich descriptors

**When** creation is accepted

**Then** the entry remains Saveable and owned. Missing subtype, Brand, Price, Pattern, Fit, Material, Silhouette, Secondary Colors, precise HEX/HSL display, Style Tags or Occasion Tags does not become a universal save blocker.

Supplied Brand is user-entered text, not an enum. Supplied Price means user-reported purchase price and requires both a decimal amount and explicit ISO 4217 currency; an incomplete pair is corrected or cleared before confirmation. No location default, fabricated amount or floating-point monetary calculation is allowed. Preserve accepted values; absent Price is unknown, not zero (FR-GAR-012/013; BRULE-GAR-010).

### AC-07 — Explain missing common compatibility information

**Given** the accepted garment meets the minimum but for TOPS, PANTS, SKIRTS, OUTERWEAR or SHOES a required common compatibility field, such as Dominant Color, Pattern Type or Climate/Season Suitability, is UNKNOWN or unavailable

**When** CapsuleAI presents the post-save readiness explanation

**Then** it identifies the missing common information and explains that decisions requiring it cannot treat the garment as ready. The owned entry is retained; missing values are not fabricated and accepted saving is not presented as failed.

The established pattern/climate checks continue for TOPS, PANTS, SKIRTS, OUTERWEAR and SHOES. Applicable layering remains required for TOPS/OUTERWEAR; bulk remains conditional on the layered check. DRESSES_JUMPSUITS, BAGS and HEADWEAR need an explicit product decision on composition and required compatibility inputs (PRD OPQ-012). Until resolved, retain and expose these saveable garments, explain that the relevant evaluation policy is not yet defined, and do not assert readiness or fabricate missing attributes. This limitation is distinct from a user omitting known required information. Brand and Price neither establish nor block readiness.

### AC-08 — Explain applicable layering and bulk requirements

**Given** the accepted garment lacks applicable Layering Level for TOPS/OUTERWEAR or Bulk Index needed for a layered compatibility decision

**When** CapsuleAI explains readiness

**Then** it identifies the applicable missing information for the affected decision. Layering is applied to TOPS/OUTERWEAR and Bulk Index only where layered compatibility requires it; unavailable required information cannot pass that check, while bulk is not a universal saving/readiness gate.

### AC-09 — Retain user authority

**Given** my manual creation is accepted with entered-and-confirmed values

**When** I inspect the accepted result or later automated information becomes available

**Then** the accepted values remain authoritative for my garment. Provenance reflects how those values were established where relevant, and automated output cannot silently replace them; this includes Brand and Price. Price is never inferred from a photograph and a suggested Brand is never accepted automatically; this story does not require automated analysis.

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

**Then** CapsuleAI distinguishes pending/unconfirmed acceptance from known failure and success, preserves useful work where practical, and lets me establish whether creation was accepted before safe retry. Repeated delivery or retry of the same intended addition has at most one accepted wardrobe effect; uncertainty does not establish definite unchanged inventory.

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

- Related drafts now document [US-GAR-002](../garment/US-GAR-002-correct-and-enrich-garment-information.md) enrichment, [US-GAR-003](../garment/US-GAR-003-add-photo-supported-garment.md) photo-supported input and [US-GAR-004](../garment/US-GAR-004-review-ai-assisted-garment-proposals.md) assistance. None becomes a prerequisite to this minimum-confirmed imageless outcome.

## 8. Out of Scope

Optional subtype, Brand and purchase Price entry are included here by the approved revision. Other rich descriptor entry/enrichment and later editing remain PBI-008. Photo capture/selection, image validation and photo-supported entry are PBI-012; AI proposals, confidence and processed preview are PBI-013. General browsing/detail are PBI-007, measurement is PBI-009, and search/removal belong to PBI-015/PBI-016. No new quota, mandatory image/AI, inventory sorting, analytics event or implementation design is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-006](../../product-backlog.md#pbi-006--add-a-confirmed-garment-manually) |
| Business | [BRD](../../../01-business/BRD.md): BG-01; BR-002, BR-003, BR-004, BR-005; CAP-03, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-02; FEAT-AI-002, FEAT-GAR-001 (manual minimum and authority) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AI-003, FR-AI-008, FR-AI-009, FR-AI-010; FR-GAR-001, FR-GAR-002, FR-GAR-003, FR-GAR-004, FR-GAR-005, FR-GAR-006, FR-GAR-011, FR-AUTH-008; DATA-GAR-001, DATA-GAR-005; DATA-INT-001, DATA-INT-004; UI-004, UI-006; ERR-GAR-001, ERR-GAR-002; COM-001, COM-002; NFR-SEC-001, NFR-SEC-002, NFR-PRIV-001; NFR-ACC-001, NFR-ACC-002; LOC-001; FR-GAR-012, FR-GAR-013, DATA-GAR-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001; BRULE-GAR-001, BRULE-GAR-002, BRULE-GAR-003, BRULE-GAR-004, BRULE-GAR-005, BRULE-GAR-006; BRULE-GAR-010 |
| Use Cases | [UC-007 — Add Garment](../../../03-requirements/use-cases/UC-007-add-garment.md), manual alternative and shared confirmation/save flows only |
| Activity Diagrams | [AD-007 — Add Garment](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml), manual branch and shared confirmation/recovery only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), selected-story evidence rather than foundation smoke tests alone. |

## 10. Open Refinement Notes

**2026-11-05 product revision:** [PRD Section 34.1](../../../02-product/PRD.md#341-approved-taxonomy-revision-and-open-follow-up-decisions) records open category-readiness/composition, optional-field validation and subtype-vocabulary questions. Resolve applicable validation before dependent implementation; these questions do not make optional fields mandatory. Historical refinement notes below are subject to this revision. Sprint 2 selection is recorded in the [Sprint plan](../../sprints/sprint-02/sprint-plan.md); source status remains Baseline Draft.

**2026-10-21 refinement draft.** Retain manual entry, review, explicit confirmation and safe acceptance as one outcome. AC-12 makes repeated delivery/retry of one logical addition explicit without prescribing a deduplication mechanism (DATA-INT-004). Verify an imageless minimum-only saved entry, retained UNKNOWN/readiness meaning, cancellation, denied ownership and interrupted acceptance. Confirm mobile Add/acknowledgment/inspection feasibility and Developers' complexity assessment (Q-02/Q-04/Q-05). Other rich entry, images and AI remain later capabilities; the later approved optional subtype/Brand/Price extension is included here.

Question details, owners and conditional planning blockers are centralized in the [focused refinement review](../../backlog-refinement-review.md#6-open-questions-for-product-owner-and-team). These notes do not change story status, assign estimates or authorize implementation. Necessary implementation detail remains JIT for a selected story. The [full story catalogue](../README.md) now accounts for all 41 PBIs; those additional drafts change none of this story's acceptance boundaries.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; document approval is not claimed; Sprint 2 selection is recorded separately in its plan.
