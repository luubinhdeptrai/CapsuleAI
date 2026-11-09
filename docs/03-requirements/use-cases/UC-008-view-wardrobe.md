# UC-008 — View Wardrobe

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-008 |
| Use Case Name | View Wardrobe |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.4 |
| Status | Baseline Draft |
| Product revision | 2026-11-05: Approved taxonomy and optional Brand/purchase Price synchronized; goals, alternate/failure paths and ownership/retry semantics preserved. |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Browse and find current owned garments, inspect their confirmed details and unevaluated recommendation eligibility and understand available reported-use information without changing wardrobe ownership.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Find accurate possessions, including imageless/incomplete entries. |

## 4. Preconditions

- The User has authenticated access authorized for the wardrobe; an empty wardrobe is permitted.

## 5. Trigger

The User opens the wardrobe or requests garment search/detail.

## 6. Main Success Scenario

1. The User requests the current wardrobe.
2. CapsuleAI distinguishes loading from loaded data and presents identifiable current confirmed garments, including entries without images.
3. The User browses or enters descriptor search and category/color filter conditions.
4. CapsuleAI shows active conditions and the matching current garments.
5. The User selects a garment to inspect.
6. CapsuleAI presents all available applicable accepted properties from the source catalogue, including multiple distinct Materials, one dominant Color, category-specific details, Brand and original purchase Price with currency; available image/provenance remain separate. Missing optional values are not fabricated and eligibility is not evaluated. Collection summaries need only sufficient identifying information, not the complete detail payload.
7. The User returns to browsing or chooses a separate supported maintenance/insight action.

## 7. Alternative Flows

### A1 — Unfiltered browsing/reset

At Main Step 3:

1. The User supplies no conditions or clears existing search/filter conditions.
2. CapsuleAI restores the unfiltered current wardrobe; continue at Main Step 5 if the User selects an item.

### A2 — No matching results

At Main Step 4:

1. CapsuleAI explains that the current conditions match no garments, without calling the wardrobe empty.
2. The User adjusts or resets conditions and resumes at Main Step 3.

### A3 — Genuinely empty wardrobe

At Main Step 2:

1. CapsuleAI explains the confirmed empty state and offers Add Garment.
2. The User may start UC-007 or leave the wardrobe without a fixed garment-quota requirement.

### A4 — Imageless, minimum-only or no-recorded-use entry

At Main Step 6:

1. CapsuleAI keeps the entry identifiable without an image, explains missing eligibility under a future approved policy fields and labels missing reports as no recorded use.
2. The User can inspect/enrich the garment without an inference that it has never been physically worn.

### A5 — Large wardrobe

At Main Step 2 (PO decision 2026-11-07, FR-WAR-001):

1. CapsuleAI presents the first page of current garments, newest first, and indicates whether more are available.
2. The User asks for more; CapsuleAI continues after the last presented garment without repeating or omitting garments that remain current. Garments added after browsing began appear when the User starts again from the first page.

## 8. Exception / Failure Flows

### E1 — Wardrobe/search/detail retrieval fails

At Main Step 2:

1. CapsuleAI identifies unavailable data and offers retry or return to usable prior context.
2. It does not display an empty wardrobe/no matches as a substitute for a failed retrieval; accepted inventory is unchanged.

### E2 — Selected garment is no longer current

At Main Step 6:

1. CapsuleAI explains the changed/removed item and refreshes the current wardrobe where possible.
2. The User returns to current browsing; historical snapshots remain separate and do not restore ownership.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Displayed inventory/details match the available current confirmed wardrobe and visible search/filter conditions.
- Unevaluated eligibility and recorded-use limitations are understandable, and no item is added/edited/removed by viewing.

## 10. Minimal / Failure Postconditions

- Failed browsing/search/detail does not delete or change accepted garments.
- Unavailable access/data is distinct from a true empty or no-match result; removed items are not restored as current.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-001` | Confirmed garment values govern displayed current information. |
| `BRULE-GAR-010` | Optional Brand and purchase Price; supplied values, missing information, currency and confirmed authority. |
| `BRULE-GAR-003` | Show that recommendation eligibility is not evaluated, without hiding any saveable entry or labelling it ready or unready. |
| `BRULE-HIST-001` | Usage indicators are reported evidence, not verified physical behavior. |
| `BRULE-HIST-002` | Historical removed snapshots do not enter current inventory. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-03` |
| [Product Features](../../02-product/PRD.md) | `FEAT-WAR-001`, `FEAT-WAR-003`, `FEAT-GAR-001`, `FEAT-ANL-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WAR-001`, `FR-WAR-002`, `FR-WAR-007`, `FR-WAR-008`, `FR-GAR-004`, `FR-ANL-004`, `FR-AUTH-008`; `FR-GAR-001`, `FR-GAR-002`, `FR-GAR-012`, `FR-GAR-013` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-001`, `DATA-GAR-004`, `DATA-GAR-005`, `DATA-HIST-002`, `DATA-HIST-001`, `DATA-RET-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-001`, `UI-003`, `UI-004`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-001`, `BRULE-GAR-003`, `BRULE-HIST-001`, `BRULE-HIST-002`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-003`, `BR-005`, `BR-006`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-03`, `CAP-04`, `CAP-07` |

## 13. Special Requirements / Constraints

Exactly 13 fashion properties are approved: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. Category and one dominant Color plus explicit user confirmation form the saving minimum. All other applicable properties are optional. Material supports zero or multiple distinct selections; Pattern is single and optional. Fit, Neckline and Sleeve Length apply only to Tops; Length uses separate Tops/Pants vocabularies; Silhouette applies only to Pants. Every supplied controlled value must match the source vocabulary and category; correct or clear invalid/inapplicable input rather than silently substitute or discard it. Unknown optional information is absent, not an invented UNKNOWN/OTHER/Etc value. Preserve real listed fallbacks such as Other, Other Materials and Colorful only where defined.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

The [property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md) owns every vocabulary label; all applicable properties belong to Sprint 2 manual creation and detail. Metadata/provenance/media are separate. Price is the original amount paid with decimal amount and explicit currency; Brand is text. No future AI output silently overwrites accepted values.

- No image, no recorded use, unevaluated eligibility, loading and unavailable data remain distinct meanings.
- Viewing is confined to the current authorized wardrobe. Historical references retain only necessary meaning and do not expose indefinitely retained original removed-garment images.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-007 — Add Garment](UC-007-add-garment.md): Add an owned garment.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Confirm edits to a selected current garment.
- [UC-010 — Remove Garment](UC-010-remove-garment.md): Deliberately remove a current garment.
- [UC-018 — View Wardrobe Utilization](UC-018-view-wardrobe-utilization.md): Inspect reported utilization in more detail.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

PRD OPQ-012 defers readiness/composition to engine development; OPQ-013 (Brand/Price validation) and OPQ-014 are resolved, and OPQ-016–018 (multiple Styles, Tanks & Camis, paged overview) were decided on 2026-11-07. Earlier issue review below predates this revision.

The earlier review recorded no Use Case-specific blocker. The current revision must resolve applicable optional-field validation before dependent implementation; unresolved recommendation policy does not prevent minimum saving or inspection.

## 16. Focused Use Case Diagram

Focused diagram is not present in this checkout; see the [master Use Case Diagram](use-case-diagram.puml) and this textual specification

The master diagram retains the actor/goal boundary; the textual specification and linked existing Activity Diagram, where present, define workflow behavior. This revision creates no replacement focused diagram.
