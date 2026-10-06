# UC-007 — Add Garment

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-007 |
| Use Case Name | Add Garment |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

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
6. CapsuleAI presents the reviewed values and identifies whether the Saveable minimum is confirmed and which applicable compatibility information remains missing.
7. The User explicitly confirms the supported Primary Category and Dominant Color and submits the garment profile.
8. CapsuleAI acknowledges accepted creation, shows the confirmed entry in the current wardrobe and explains applicable Recommendation-Readiness or additional-detail needs.

## 7. Alternative Flows

### A1 — Imageless manual entry

At Main Step 3:

1. The User chooses manual entry without supplying an image or waiting for analysis.
2. CapsuleAI accepts manual garment information, including Primary Category and Dominant Color and optional supported descriptors.
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

### A4 — Saveable but not Recommendation-Ready

At Main Step 6:

1. The reviewed profile meets the confirmed category/color minimum but lacks an applicable compatibility field.
2. The User confirms saving at Main Step 7 without completing every rich descriptor.
3. CapsuleAI retains owned Saveable status and explains that decisions requiring missing/UNKNOWN information cannot use it as ready.

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

### E3 — Missing confirmed Saveable minimum

At Main Step 7:

1. CapsuleAI identifies missing/unconfirmed Primary Category or Dominant Color and retains the draft for correction.
2. The User supplies/reviews the needed information and resumes at Main Step 5; no authoritative entry is created yet.

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
- Readiness is explained separately from Saveable ownership; an imageless manual entry remains accessible.

## 10. Minimal / Failure Postconditions

- Canceled/unconfirmed/rejected work creates no confirmed wardrobe entry and does not alter existing garments.
- Known failed saving is not reported as successful; an uncertain outcome must be reviewed before safe retry.
- Missing compatibility information is not fabricated and cannot justify an unsupported ready/valid-outfit claim.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-001` | User-confirmed information establishes authority. |
| `BRULE-GAR-002` | Confirmed Primary Category and Dominant Color permit saving; manual entry needs no image/analysis. |
| `BRULE-GAR-003` | Readiness is evaluation-specific and distinct from Saveable status. |
| `BRULE-GAR-004` | Four primary categories and bounded subtypes preserve MVP classification. |
| `BRULE-GAR-005` | Supported rich descriptors use the approved values/scales. |
| `BRULE-GAR-006` | Unknown fields and SOLID/NONE consistency remain meaningful. |
| `BRULE-GAR-007` | Evidence-guarded High Confidence, Needs Review and Uncertain never replace confirmation. |
| `BRULE-GAR-008` | Provenance and confidence differ; material inference is not physical verification. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-02` |
| [Product Features](../../02-product/PRD.md) | `FEAT-AI-001`, `FEAT-AI-002`, `FEAT-GAR-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-AI-001`, `FR-AI-002`, `FR-AI-003`, `FR-AI-004`, `FR-AI-005`, `FR-AI-006`, `FR-AI-007`, `FR-AI-008`, `FR-AI-009`, `FR-AI-010`, `FR-AI-011`, `FR-AI-012`, `FR-GAR-001`, `FR-GAR-002`, `FR-GAR-003`, `FR-GAR-004`, `FR-GAR-005`, `FR-GAR-006`, `FR-GAR-007`, `FR-GAR-008`, `FR-GAR-009`, `FR-GAR-010`, `FR-GAR-011`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-001`, `DATA-GAR-002`, `DATA-GAR-003`, `DATA-GAR-004`, `DATA-GAR-005`, `DATA-INT-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AI-001`, `ERR-AI-002`, `ERR-GAR-001`, `ERR-GAR-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-004`, `UI-005`, `UI-006`, `SI-001`, `HW-001`, `HW-002`, `COM-002`, `COM-001` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PERF-001`, `NFR-REL-001`, `NFR-USE-002`, `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002`, `NFR-TEST-003` |
| [Software Requirements — AI Behavior](../SRS.md) | `AI-REQ-001`, `AI-REQ-002`, `AI-REQ-003`, `AI-REQ-004`, `AI-REQ-005`, `AI-REQ-006`, `AI-REQ-007`, `AI-REQ-008`, `AI-REQ-009`, `AI-REQ-012`, `AI-REQ-013` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-001`, `BRULE-GAR-002`, `BRULE-GAR-003`, `BRULE-GAR-004`, `BRULE-GAR-005`, `BRULE-GAR-006`, `BRULE-GAR-007`, `BRULE-GAR-008`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-001`, `BR-002`, `BR-003`, `BR-004`, `BR-005`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-02`, `CAP-03`, `CAP-04` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- Image formats are JPEG/JPG, PNG and HEIC/HEIF, with maximum 15 MB and shortest dimension at least 512 pixels. A plain/white background is not required.
- A usable preview identifies the garment, preserves major regions and supports review despite non-obstructive background; preview quality and attribute certainty are distinct.
- Default confidence states and evidence guards follow BRULE-GAR-007; understandable Vietnamese labels are primary. Even High Confidence requires confirmation.
- Common readiness includes category/color, usable pattern and climate suitability; TOP/OUTERWEAR need applicable layering, and bulk is required only for applicable layered compatibility. Rich color/shape/material/context detail is supported without universal saving gates.
- NFR-PERF-001 targets p95 ≤5 seconds after image transfer and accepted analysis through usable preview plus reviewable proposals, under the reference conditions in SRS Sections 2.3/12.3. Pending work is not a saved garment.

- Images, confirmed/entered attributes and corrections support garment functionality and authorized validation; personal garment data is not used for MVP AI training/improvement. Future training needs separate explicit opt-in and approved product/privacy change.
- Formal timing validation uses the SRS reference devices/network, 5 warm-ups then ≥100 measured executions and no exclusion of legitimate slow runs. The p95 ≤5-second start/end boundary is unchanged.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-008 — View Wardrobe](UC-008-view-wardrobe.md): Inspect the confirmed entry.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Enrich or correct an existing garment later.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Use only applicable ready owned garments in daily advice.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-007-add-garment.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.

## 17. Activity Diagram

[Activity Diagram](../activity-diagrams/AD-007-add-garment.puml)

This diagram visualizes the established main, alternative, and failure flows of this Use Case; the textual specification remains the normative behavioral source.
