# UC-009 — Edit Garment

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-009 |
| Use Case Name | Edit Garment |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.4 |
| Status | Baseline Draft |
| Product revision | 2026-11-05: Approved taxonomy and optional Brand/purchase Price synchronized; goals, alternate/failure paths and ownership/retry semantics preserved. |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Correct or enrich supported information on a current owned garment, making explicitly accepted User changes authoritative and keeping dependent advice aligned with the new state.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Maintain trustworthy clothing information without losing accepted values on cancel/failure. |

## 4. Preconditions

- The User has authenticated access authorized for an existing current owned garment.

## 5. Trigger

The User chooses to edit that garment.

## 6. Main Success Scenario

1. The User requests editing for the selected garment.
2. CapsuleAI presents its accepted profile and supported editable information, with relevant unknown/uncertain states.
3. The User revises intended values using the supported vocabularies, including all applicable source-approved properties and multiple distinct Materials, or clears optional information. A category change requires correction or clearing of an incompatible subtype. Price, if supplied, includes both decimal amount and explicit ISO 4217 currency; incomplete pairs return for correction before acceptance.
4. CapsuleAI presents the proposed revision and identifies missing required or incompatible values and eligibility under a future approved policy implications.
5. The User explicitly confirms the intended supported changes.
6. CapsuleAI acknowledges the accepted revision and shows the new authoritative accepted profile / unevaluated eligibility.
7. CapsuleAI refreshes affected advice/assessments or identifies them as requiring reevaluation rather than presenting prior results as current.

## 7. Alternative Flows

### A1 — Leave optional descriptors unknown

At Main Step 3:

1. The User changes only intended fields and leaves unknown optional Brand/Price/material/shape/color/context detail unknown.
2. Resume at Main Step 4; completing every rich descriptor is not required for a supported edit.

### A2 — Add previously unknown optional information

1. The User supplies intended applicable properties from the approved source list.
2. CapsuleAI validates vocabulary/applicability and permits confirmation without requiring every optional field.
3. Accepted edits preserve user authority; eligibility is not evaluated and no compatibility descriptor is derived.

### A3 — Cancel before acceptance

At Main Step 5:

1. The User cancels the proposed edit.
2. CapsuleAI retains the previously accepted profile and dependent state.

## 8. Exception / Failure Flows

### E1 — Invalid or unconfirmed required information

At Main Step 4:

1. CapsuleAI explains unsupported values, inconsistent descriptors or missing confirmed minimum information and does not accept the revision.
2. The User corrects the proposal and resumes at Main Step 3.

### E2 — Edit failed or acceptance uncertain

At Main Step 6:

1. CapsuleAI distinguishes known failure from unconfirmed acceptance, preserving useful entered work where practical.
2. The prior profile stays authoritative after known failure; the User reviews the actual outcome before retrying an uncertain update.

### E3 — Garment is no longer current/authorized

At Main Step 5:

1. CapsuleAI refuses an edit to information outside the User's current authorized ownership and explains the unavailable selection.
2. The User returns to current wardrobe inspection; no historical snapshot is silently restored as owned.

### E4 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Accepted revised values are canonical and cannot be silently replaced by later automated output.
- Current accepted profile / unevaluated eligibility reflects the accepted revision; affected recommendations, Coverage and Multiplier refresh or clearly become outdated.

## 10. Minimal / Failure Postconditions

- Canceled/rejected/known failed revisions leave the accepted garment unchanged.
- An uncertain revision is not claimed saved or freshly evaluated until its actual outcome is established.
- Other garments and historical reports are not edited by this action.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-001` | User confirmation/correction establishes authoritative values. |
| `BRULE-GAR-010` | Optional Brand and purchase Price; supplied values, missing information, currency and confirmed authority. |
| `BRULE-GAR-002` | The confirmed Saveable minimum remains meaningful. |
| `BRULE-GAR-003` | Edits can change eligibility under a future approved policy without universal descriptor requirements. |
| `BRULE-GAR-004` | Classification/context vocabulary is bounded. |
| `BRULE-GAR-005` | Editable descriptors retain approved values/scales. |
| `BRULE-GAR-006` | Consistency and unknown handling prevent invented compatibility. |
| `BRULE-GAR-008` | Material uncertainty/provenance is not verification. |
| `BRULE-GAR-009` | Only accepted edits change state and invalidate affected advice. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-03` |
| [Product Features](../../02-product/PRD.md) | `FEAT-WAR-002`, `FEAT-GAR-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WAR-003`, `FR-WAR-006`, `FR-GAR-002`, `FR-GAR-003`, `FR-GAR-004`, `FR-GAR-005`, `FR-GAR-006`, `FR-GAR-007`, `FR-GAR-008`, `FR-GAR-009`, `FR-GAR-010`, `FR-GAR-011`, `FR-AUTH-008`; `FR-GAR-001`, `FR-GAR-012`, `FR-GAR-013` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-002`, `DATA-GAR-003`, `DATA-GAR-005`, `DATA-INT-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-GAR-001`, `ERR-GAR-002`, `ERR-ANL-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-004`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-001`, `BRULE-GAR-002`, `BRULE-GAR-003`, `BRULE-GAR-004`, `BRULE-GAR-005`, `BRULE-GAR-006`, `BRULE-GAR-008`, `BRULE-GAR-009`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-002`, `BR-003`, `BR-004`, `BR-005`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-03`, `CAP-04` |

## 13. Special Requirements / Constraints

Exactly 13 fashion properties are approved: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. Category and one dominant Color plus explicit user confirmation form the saving minimum. All other applicable properties are optional. Material and Style each support zero or multiple distinct selections; Pattern is single and optional. Fit, Neckline and Sleeve Length apply only to Tops; Length uses separate Tops/Pants vocabularies; Silhouette applies only to Pants. Every supplied controlled value must match the source vocabulary and category; correct or clear invalid/inapplicable input rather than silently substitute or discard it. Unknown optional information is absent, not an invented UNKNOWN/OTHER/Etc value. Preserve real listed fallbacks such as Other, Other Materials and Colorful only where defined.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without prescribing a new physical field. Preserve the implemented `recommendationEligibility: NOT_EVALUATED` response.

The [property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md) owns every vocabulary label; all applicable properties belong to Sprint 2 manual creation and detail. Metadata/provenance/media are separate. Price is the original amount paid with decimal amount and explicit currency; Brand is text. No future AI output silently overwrites accepted values.

- The User can correct supported dimensions without re-running analysis. Automated proposals cannot silently overwrite confirmed edits.
- A failed assessment refresh is not presented as a current result for the revised profile.
- Confirmed corrections support accurate private functionality, without MVP personal-data AI training. Last accepted values remain authoritative after known failure.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-008 — View Wardrobe](UC-008-view-wardrobe.md): Inspect the current accepted profile.
- [UC-010 — Remove Garment](UC-010-remove-garment.md): Remove ownership instead of changing descriptors.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Refresh affected current advice.
- [UC-019 — View Wardrobe Coverage](UC-019-view-wardrobe-coverage.md): Refresh affected Coverage.
- [UC-022 — Evaluate Candidate Garment](UC-022-evaluate-candidate-garment.md): Reevaluate affected candidate utility.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

PRD OPQ-012–014 now track unresolved category readiness/composition and optional-field/subtype policies. Earlier issue review below predates this revision.

The earlier review recorded no Use Case-specific blocker. The current revision must resolve applicable optional-field validation before dependent implementation; unresolved recommendation policy does not prevent minimum saving or inspection.

## 16. Focused Use Case Diagram

Focused diagram is not present in this checkout; see the [master Use Case Diagram](use-case-diagram.puml) and this textual specification

The master diagram retains the actor/goal boundary; the textual specification and linked existing Activity Diagram, where present, define workflow behavior. This revision creates no replacement focused diagram.
