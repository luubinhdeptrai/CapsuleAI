# US-GAR-001 — Add an owned garment manually

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-001 |
| Version | 0.5 |
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

Input, validation, confirmation, authorized persistence and accepted-value preservation for all 13 applicable properties: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. The [source catalogue](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md) owns full lists/applicability. Category and one dominant Color plus explicit confirmation are the minimum; every other applicable field is optional. Images and AI are independent future routes. Preserve cancellation, failed/uncertain acceptance, safe retries and account isolation. Later editing remains PBI-008; no current property is deferred to enrichment.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

## 4. Preconditions

- I have authenticated access authorized for my wardrobe.
- Existing garments, an image, successful automated analysis and completed optional personalization are not prerequisites.

## 5. Acceptance Criteria

### AC-01 — Create and preserve an explicitly confirmed garment

**Given** authorized manual entry with supported Category, one approved dominant Color and any supplied valid applicable optional properties

**When** I review and explicitly confirm them and creation is accepted

**Then** exactly one owned garment is acknowledged, all accepted applicable values are durably preserved and available for rereading, and recommendation eligibility remains not evaluated.

### AC-02 — Continue independently of image and AI

**Given** I choose manual entry without an image, including while image analysis is unavailable

**When** I review and confirm the minimum and accepted creation completes

**Then** the garment is saved through the same manual outcome without requiring image capture, upload, preview, AI proposals or successful analysis.

### AC-03 — Review and correct before confirmation

**Given** I have entered category/color and any optional subtype, Brand or Price but have not submitted confirmed creation

**When** I review the values and change an entered value

**Then** the review reflects the latest supported values and permits explicit confirmation of that revised minimum. Merely entering or changing a draft does not establish a confirmed owned garment.

### AC-04 — Validate the complete approved classification and vocabularies

**Given** I enter any of the eight approved Categories

**When** I supply Subtype, Color or another controlled property

**Then** use the complete source lists: every category subtype, exactly ten Dresses/Jumpsuits choices, all Colors/Materials/Patterns/named Styles and category details. Validate parent/applicability; reject unsupported selections with correction/clear guidance, without silent replacement, invented fallback or Etc value. Color is exactly one dominant selection, including Colorful.

### AC-05 — Correct a missing or unconfirmed minimum

**Given** Primary Category or Dominant Color is missing or not confirmed

**When** I attempt to submit the garment

**Then** CapsuleAI identifies the missing confirmation/information and retains the draft for correction. No authoritative garment is created, and existing wardrobe information remains unchanged until accepted creation.

### AC-06 — Support all applicable properties while keeping optional values optional

**Given** I confirm valid Category and Color

**When** I omit or provide the other applicable properties

**Then** omission does not block saving. Support all 13 properties in this Sprint: optional Subtype, zero or multiple distinct Materials, one optional Pattern, zero or multiple distinct Styles, applicable Fit/Neckline/Sleeve Length/Length/Silhouette, Brand text and purchase Price. Material duplicates add no semantic material; no percentages/proportions are fabricated. Price uses decimal amount plus explicit ISO 4217 currency together or is cleared; missing Price is unknown, not zero. Preserve all accepted values without float calculations, image/location inference or currency conversion.

### AC-07 — Apply category-specific detail validation

**Given** I supply category-specific information or change Category before confirmation

**When** CapsuleAI validates my entry

**Then** Fit, Neckline and Sleeve Length apply only to Tops; Length uses the separate Tops/Pants lists and Silhouette applies only to Pants. Other categories receive no such fields. Invalid/inapplicable supplied information is identified for correction or clearing rather than silently discarded; applicable optional omission remains valid.

### AC-08 — Keep eligibility unevaluated without rejecting the garment

**Given** my garment satisfies saving requirements

**When** creation is accepted or its outcome is inspected

**Then** no readiness predicate is evaluated or required. Do not label the garment ready, invalid, unsuitable or ineligible because a future engine policy is undefined or optional values are missing. Preserve normal wardrobe access and truthful missing-value information; no mandatory readiness enum/column is prescribed.

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

**When** I review/correct the minimum, confirm, or encounter missing-field/save/guidance that eligibility is not evaluated

**Then** the fields, correction and confirmation actions and result states remain operable and understandable through meaningful labels/states beyond color alone. Guidance distinguishes draft, saved ownership, failed/unconfirmed save and eligibility under a future approved policy.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-GAR-001–BRULE-GAR-006 preserve authorized ownership, entered-and-confirmed authority, the Saveable minimum, eligibility under a future approved policy and bounded vocabularies.
- Confirmation establishes canonical information; it does not prove physical composition or make unknown compatibility information valid. All applicable optional fields remain supported; eligibility is not evaluated and no valid-outfit guarantee follows from saving.
- The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-004's usable authenticated access; [US-AUTH-002](../auth/US-AUTH-002-log-in.md) and [US-AUTH-003](../auth/US-AUTH-003-resume-session.md) supply access and continuity where applicable.
- PBI-007's [US-WAR-001](../wardrobe/US-WAR-001-browse-current-wardrobe.md) and [US-WAR-002](../wardrobe/US-WAR-002-inspect-owned-garment.md) are useful inspection paths after acceptance; creating the minimum is not conditional on completing those whole stories.
- PBI-003 provides useful common interaction support, not an additional hard capability dependency. PBI-008 edits accepted properties later; all applicable creation fields are included here; PBI-012/PBI-013 add photo/AI routes. None is a prerequisite to manual creation.

- Related drafts now document [US-GAR-002](../garment/US-GAR-002-correct-and-enrich-garment-information.md) enrichment, [US-GAR-003](../garment/US-GAR-003-add-photo-supported-garment.md) photo-supported input and [US-GAR-004](../garment/US-GAR-004-review-ai-assisted-garment-proposals.md) assistance. None becomes a prerequisite to this minimum-confirmed imageless outcome.

## 8. Out of Scope

All 13 applicable properties are included in manual creation. Later editing remains PBI-008. Photo capture/selection, image validation and photo-supported entry are PBI-012; AI proposals, confidence and processed preview are PBI-013. General browsing/detail are PBI-007, measurement is PBI-009, and search/removal belong to PBI-015/PBI-016. No new quota, mandatory image/AI, inventory sorting, analytics event or implementation design is added.

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

**2026-11-07 Product Owner decisions.** Style accepts zero, one or several distinct approved values (OPEN-S2-08). Brand: at most 100 Unicode code points, blank means unspecified, accepted text kept exactly (OPEN-S2-01B). Price: zero is valid, negative invalid, at most 12 integer digits, fractional digits limited to the currency's ISO 4217 minor unit and rejected, never rounded, beyond it; valid upper-case ISO 4217 currency (OPEN-S2-01A; PRD OPQ-013 resolved). The Tops subtype is spelled Tanks & Camis (`TANKS_AND_CAMIS`). No image field is introduced; eligibility stays `NOT_EVALUATED`. See the [decision implementation notes](../../sprints/sprint-02/decision-implementation-notes.md).

**2026-11-05 product revision:** [PRD Section 34.1](../../../02-product/PRD.md#341-approved-taxonomy-revision-and-open-follow-up-decisions) records the final 13-property/vocabulary approval and resolved eligibility deferral; only genuine validation/contract details remain open. Resolve applicable validation before dependent implementation; these questions do not make optional fields mandatory. Historical refinement notes below are subject to this revision. Sprint 2 selection is recorded in the [Sprint plan](../../sprints/sprint-02/sprint-plan.md); source status remains Baseline Draft.

**2026-10-21 refinement draft.** Retain manual entry, review, explicit confirmation and safe acceptance as one outcome. AC-12 makes repeated delivery/retry of one logical addition explicit without prescribing a deduplication mechanism (DATA-INT-004). Verify an imageless minimum-only saved entry, retained UNKNOWN/readiness meaning, cancellation, denied ownership and interrupted acceptance. Confirm mobile Add/acknowledgment/inspection feasibility and Developers' complexity assessment (Q-02/Q-04/Q-05). Other rich entry, images and AI remain later capabilities; the later approved optional subtype/Brand/Price extension is included here.

Question details, owners and conditional planning blockers are centralized in the [focused refinement review](../../backlog-refinement-review.md#6-open-questions-for-product-owner-and-team). These notes do not change story status, assign estimates or authorize implementation. Necessary implementation detail remains JIT for a selected story. The [full story catalogue](../README.md) now accounts for all 41 PBIs; those additional drafts change none of this story's acceptance boundaries.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; document approval is not claimed; Sprint 2 selection is recorded separately in its plan.
