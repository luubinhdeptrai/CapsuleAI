# UC-007 — Add Garment

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-007 |
| Use Case Name | Add Garment |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.4 |
| Status | Baseline Draft |
| Product revision | 2026-11-05: Approved taxonomy and optional Brand/purchase Price synchronized; goals, alternate/failure paths and ownership/retry semantics preserved. |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Create a trustworthy digital representation of an owned garment through image assistance or imageless manual entry, with the User confirming the authoritative profile and understanding its readiness.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Save owned clothing despite analysis failure or incomplete optional descriptors. |

## 4. Preconditions

- The User has authenticated access authorized for the wardrobe.

## 5. Trigger

The User chooses Add Garment.

## 6. Main Success Scenario

1. The User requests a new garment entry.
2. CapsuleAI offers permitted camera/gallery input or imageless manual entry, with guidance on lighting, visibility and confusing backgrounds.
3. The User chooses image assistance, captures or selects a permitted image, and submits it.
4. CapsuleAI validates the image, displays pending analysis, then presents an available usable processed preview and reviewable attribute proposals with understandable confidence/provenance.
5. The User reviews the proposed information, corrects it when necessary, and supplies missing information needed for the intended profile.
6. CapsuleAI presents all supplied applicable property values, validates source vocabulary and category applicability (including distinct Materials and one dominant Color), and checks confirmation of the Category/Color minimum. Invalid/inapplicable input or an incomplete Price pair returns for correction or clearing; optional omissions remain valid.
7. The User explicitly confirms the supported Primary Category and Dominant Color and submits the garment profile.
8. CapsuleAI acknowledges one accepted owned garment with every supplied valid applicable value preserved for rereading; recommendation eligibility is not evaluated.

## 7. Alternative Flows

### A1 — Imageless manual entry

At Main Step 3:

1. The User chooses manual entry without supplying an image or waiting for analysis.
2. CapsuleAI offers all 13 applicable properties from the source catalogue. Category and one approved dominant Color are required; Subtype, multiple distinct Materials, Pattern, Style, applicable category details, Brand and purchase Price are optional. Image/AI success is not required.
3. Resume at Main Step 5 for review and explicit confirmation; analysis success is not required.

### A2 — Camera/gallery choice or image replacement

At Main Step 3:

1. The User uses an available permitted image route, declines camera access in favor of gallery/manual entry, or replaces the selected image before confirmation.
2. CapsuleAI keeps the work unconfirmed; resume at Main Step 3 for a replacement image or take the manual alternative.

### A3 — Needs Review or Uncertain proposals

At Main Step 5:

1. CapsuleAI prompts checking for Needs Review and correction/manual supply for Uncertain information; High Confidence still requires review.
2. The User corrects or enters supported values, leaving unknown optional material/descriptors qualified or unknown.
3. Resume at Main Step 6; certainty is not a mandatory saving gate once the confirmed minimum is supplied.

### A4 — Save with optional information absent

At Main Step 6:

1. The reviewed profile has valid confirmed Category and Color but omits some or all optional properties.
2. The User confirms saving without completing them.
3. CapsuleAI retains one owned garment, preserves every supplied accepted value and truthfully reports missing information. Eligibility is not evaluated; optional absence does not establish invalidity or unsuitability.

### A5 — Cancel before confirmation

At Main Step 7:

1. The User cancels preconfirmation work.
2. CapsuleAI creates no confirmed garment from the draft and leaves existing wardrobe information unchanged.

## 8. Exception / Failure Flows

### E1 — Invalid or unsuitable image

At Main Step 4:

1. CapsuleAI explains an unsupported format, file over 15 MB, shortest dimension below 512 pixels or unsuitable image without adding a garment.
2. The User supplies a replacement and resumes at Main Step 3, or takes the imageless manual alternative.

### E2 — Unavailable/failed analysis or unusable preview

At Main Step 4:

1. CapsuleAI distinguishes analysis/preview failure from successful garment addition and explains retry/replacement/manual choices.
2. The User may retry image assistance or continue manually, including using helpful original-image information; no successful analysis is required to confirm the minimum.

### E3 — Missing confirmed minimum or invalid supplied information

At Main Step 7:

1. CapsuleAI identifies missing/unconfirmed Primary Category or Dominant Color and retains the draft for correction.
2. The User supplies/reviews the needed information and resumes at Main Step 5; no authoritative entry is created yet.

A supplied subtype outside its parent category or an incomplete Price amount/currency pair follows the same correction path before acceptance. The User can clear optional information and continue with the confirmed minimum; valid supplied values are retained with the accepted addition.

### E4 — Save failed or outcome uncertain

At Main Step 8:

1. CapsuleAI distinguishes a known failed save from a pending/unconfirmed outcome and preserves useful entered work where practical.
2. The User reviews whether creation was accepted before retrying; one accepted addition cannot become unexplained duplicate entries.

### E5 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- One accepted garment belongs to the User's active wardrobe; confirmed/corrected/entered-and-confirmed values are authoritative.
- Applicable confidence/provenance and unknown states remain distinct; later automated output cannot silently replace accepted values.
- Recommendation eligibility is reported as not evaluated, separately from Saveable ownership; an imageless manual entry remains accessible.

## 10. Minimal / Failure Postconditions

- Canceled/unconfirmed/rejected work creates no confirmed wardrobe entry and does not alter existing garments.
- Known failed saving is not reported as successful; an uncertain outcome must be reviewed before safe retry.
- No compatibility information is requested or fabricated, and no ready/unready or valid-outfit claim is made (PRD OPQ-015; FEAT-WAR-001 updated 2026-11-07).
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-001` | User-confirmed information establishes authority. |
| `BRULE-GAR-010` | Optional Brand and purchase Price; supplied values, missing information, currency and confirmed authority. |
| `BRULE-GAR-002` | Confirmed Primary Category and Dominant Color permit saving; manual entry needs no image/analysis. |
| `BRULE-GAR-003` | Readiness is evaluation-specific and distinct from Saveable status. |
| `BRULE-GAR-004` | Eight primary categories and category-valid optional subtypes preserve approved classification. |
| `BRULE-GAR-005` | Supported rich descriptors use the approved values/scales. |
| `BRULE-GAR-006` | Unknown fields and SOLID/NONE consistency remain meaningful. |
| `BRULE-GAR-007` | Evidence-guarded High Confidence, Needs Review and Uncertain never replace confirmation. |
| `BRULE-GAR-008` | Provenance and confidence differ; material inference is not physical verification. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-02` |
| [Product Features](../../02-product/PRD.md) | `FEAT-AI-001`, `FEAT-AI-002`, `FEAT-GAR-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-AI-001`, `FR-AI-002`, `FR-AI-003`, `FR-AI-004`, `FR-AI-005`, `FR-AI-006`, `FR-AI-007`, `FR-AI-008`, `FR-AI-009`, `FR-AI-010`, `FR-AI-011`, `FR-AI-012`, `FR-GAR-001`, `FR-GAR-002`, `FR-GAR-003`, `FR-GAR-004`, `FR-GAR-005`, `FR-GAR-006`, `FR-GAR-007`, `FR-GAR-008`, `FR-GAR-009`, `FR-GAR-010`, `FR-GAR-011`, `FR-AUTH-008`; `FR-GAR-012`, `FR-GAR-013` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-001`, `DATA-GAR-002`, `DATA-GAR-003`, `DATA-GAR-004`, `DATA-GAR-005`, `DATA-INT-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AI-001`, `ERR-AI-002`, `ERR-GAR-001`, `ERR-GAR-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-004`, `UI-005`, `UI-006`, `SI-001`, `HW-001`, `HW-002`, `COM-002`, `COM-001` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PERF-001`, `NFR-REL-001`, `NFR-USE-002`, `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002`, `NFR-TEST-003` |
| [Software Requirements — AI Behavior](../SRS.md) | `AI-REQ-001`, `AI-REQ-002`, `AI-REQ-003`, `AI-REQ-004`, `AI-REQ-005`, `AI-REQ-006`, `AI-REQ-007`, `AI-REQ-008`, `AI-REQ-009`, `AI-REQ-012`, `AI-REQ-013` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-001`, `BRULE-GAR-002`, `BRULE-GAR-003`, `BRULE-GAR-004`, `BRULE-GAR-005`, `BRULE-GAR-006`, `BRULE-GAR-007`, `BRULE-GAR-008`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-02`, `CAP-03`, `CAP-04` |

## 13. Special Requirements / Constraints

Exactly 13 fashion properties are approved: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. Category and one dominant Color plus explicit user confirmation form the saving minimum. All other applicable properties are optional. Material and Style each support zero or multiple distinct selections; Pattern is single and optional. Fit, Neckline and Sleeve Length apply only to Tops; Length uses separate Tops/Pants vocabularies; Silhouette applies only to Pants. Every supplied controlled value must match the source vocabulary and category; correct or clear invalid/inapplicable input rather than silently substitute or discard it. Unknown optional information is absent, not an invented UNKNOWN/OTHER/Etc value. Preserve real listed fallbacks such as Other, Other Materials and Colorful only where defined.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without prescribing a new physical field. Preserve the implemented `recommendationEligibility: NOT_EVALUATED` response.

The [property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md) owns every vocabulary label; all applicable properties belong to Sprint 2 manual creation and detail. Metadata/provenance/media are separate. Price is the original amount paid with decimal amount and explicit currency; Brand is text. No future AI output silently overwrites accepted values.

- Image formats are JPEG/JPG, PNG and HEIC/HEIF, with maximum 15 MB and shortest dimension at least 512 pixels. A plain/white background is not required.
- A usable preview identifies the garment, preserves major regions and supports review despite non-obstructive background; preview quality and attribute certainty are distinct.
- NFR-PERF-001 targets p95 ≤5 seconds after image transfer and accepted analysis through usable preview plus reviewable proposals, under the reference conditions in SRS Sections 2.3/12.3. Pending work is not a saved garment.
- Images, confirmed/entered attributes and corrections support garment functionality and authorized validation; personal garment data is not used for MVP AI training/improvement. Future training needs separate explicit opt-in and approved product/privacy change.
- Formal timing validation uses the SRS reference devices/network, 5 warm-ups then ≥100 measured executions and no exclusion of legitimate slow runs. The p95 ≤5-second start/end boundary is unchanged.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-008 — View Wardrobe](UC-008-view-wardrobe.md): Inspect the confirmed entry.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Enrich or correct an existing garment later.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Use only applicable owned garments evaluated under a future approved policy in daily advice.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

Chat photo attachment routes into this explicit review/confirmation goal. Catalog-to-Closet (UC-030) and bulk queue/review (UC-031) have separate actor goals and Draft stories; those entry routes do not enlarge US-GAR-003 into AI extraction or bulk import. Materials and Styles are distinct sets; Brand/Price use the approved validation in BRULE-GAR-010.

## 15. Open Issues

PRD OPQ-012 defers readiness/composition to engine development; OPQ-013 (Brand/Price validation) and OPQ-014 are resolved, and OPQ-016–018 (multiple Styles, Tanks & Camis, paged overview) were decided on 2026-11-07. Earlier issue review below predates this revision.

The earlier review recorded no Use Case-specific blocker. The current revision must resolve applicable optional-field validation before dependent implementation; unresolved recommendation policy does not prevent minimum saving or inspection.

## 16. Focused Use Case Diagram

Focused diagram is not present in this checkout; see the [master Use Case Diagram](use-case-diagram.puml) and this textual specification

The master diagram retains the actor/goal boundary; the textual specification and linked existing Activity Diagram, where present, define workflow behavior. This revision creates no replacement focused diagram.

## 17. Activity Diagram

[Activity Diagram](../activity-diagrams/AD-007-add-garment.puml)

This diagram visualizes the established main, alternative, and failure flows of this Use Case; the textual specification remains the normative behavioral source.
