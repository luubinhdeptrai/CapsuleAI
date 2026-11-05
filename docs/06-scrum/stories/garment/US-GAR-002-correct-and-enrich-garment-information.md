# US-GAR-002 — Correct and enrich authoritative garment information

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-002 |
| Version | 0.3 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-008](../../product-backlog.md#pbi-008--correct-and-enrich-garment-information-with-readiness-guidance) |
| Domain | Garment |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |
| Product revision | 2026-11-05: Synchronized approved garment taxonomy and optional-property authority; no new Sprint selection or AI implementation. |

## 1. Story

As an authenticated owner, I want to review, enrich and explicitly confirm supported garment information, so that my wardrobe and advice reflect trustworthy descriptions.

## 2. Value

Keep garment knowledge trustworthy and help users supply the information needed for defensible advice.

## 3. Scope

Use all applicable properties and complete approved vocabularies from the [source authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). Exactly 13 fashion properties are approved: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. Category and one dominant Color plus explicit user confirmation form the saving minimum. All other applicable properties are optional. Material supports zero or multiple distinct selections; Pattern is single and optional. Fit, Neckline and Sleeve Length apply only to Tops; Length uses separate Tops/Pants vocabularies; Silhouette applies only to Pants. Every supplied controlled value must match the source vocabulary and category; correct or clear invalid/inapplicable input rather than silently substitute or discard it. Unknown optional information is absent, not an invented UNKNOWN/OTHER/Etc value. Preserve real listed fallbacks such as Other, Other Materials and Colorful only where defined.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

All applicable property input is already included in Sprint 2 manual creation; this story retains only its separate future editing, photo or AI-assisted outcome. Future suggestions require explicit acceptance and cannot overwrite confirmed values; Price never comes from appearance. Metadata/media are separate, not extra properties.

## 4. Preconditions

- The user has authorized access to an existing current owned garment, or to its applicable review before confirmation.

## 5. Acceptance Criteria

### AC-01 — Review bounded information

Present accepted/proposed/missing information distinctly and support all applicable approved properties for correction or clearing, using the complete source lists: Category, Subtype, one Color, distinct Materials, Pattern, Style, category-specific details, Brand and purchase Price.

The optional-property and classification rules in Section 3 apply to this accepted outcome, including preservation of valid supplied values and correction/clearing of invalid category/subtype or incomplete Price pairs.

### AC-02 — Apply supported consistency

Reject unsupported or inapplicable values without changing accepted information. Use the complete source lists, require one approved Color, preserve distinct Materials, and never silently substitute a value or accept Etc as Style.

### AC-03 — Preserve saving validity and unevaluated eligibility

Confirmed Category and one Color remain the saving minimum. Other applicable values are optional. An accepted edit can revise or clear optional values and revalidate category-specific applicability without executing a readiness gate, claiming suitability or reintroducing removed properties.

### AC-04 — Preserve uncertainty

Unknown properties stay absent unless the user explicitly supplies/accepts supported information; no automatic cross-property derivation is approved. Material can be corrected or left unknown without a verified composition/durability claim; derivation cannot silently replace user-confirmed values.

### AC-05 — Confirm or cancel

Explicit confirmation establishes the revised authoritative profile; recommendation eligibility remains not evaluated. Cancellation or a known rejected/failed edit retains the prior accepted profile; later AI output cannot silently overwrite confirmed corrections.

### AC-06 — Refresh dependent meaning

After a relevant accepted revision, detail uses the new profile and affected outfit/Coverage/Multiplier advice is refreshed or marked outdated. Failed reassessment does not make old advice current.

### AC-07 — Resolve uncertain acceptance

If communication leaves acceptance uncertain, show an unconfirmed outcome and allow review before safe retry, preserving useful entered work where practical. Do not claim either saved or definitely unchanged state solely from the interrupted response.

### AC-08 — Protect the selected item

An expired/revoked or wrong-account request cannot inspect/edit another user's garment. A changed/removed selection is explained without editing another item or restoring historical ownership.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-006.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Minimum-only creation is [US-GAR-001](../garment/US-GAR-001-add-garment-manually.md); photo/AI routes are [US-GAR-003](../garment/US-GAR-003-add-photo-supported-garment.md)/004; removal is [US-GAR-005](../garment/US-GAR-005-remove-current-garment-preserve-minimal-history.md). No new descriptors, universal completeness requirement or recommendation algorithm is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-008](../../product-backlog.md#pbi-008--correct-and-enrich-garment-information-with-readiness-guidance); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-003, BR-002, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-02, JRN-03, FEAT-GAR-001, FEAT-WAR-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-GAR-001–FR-GAR-011; FR-WAR-003, FR-WAR-006; DATA-GAR-002, DATA-GAR-003, DATA-GAR-005; DATA-INT-001; ERR-GAR-001, ERR-GAR-002; COM-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-001–BRULE-GAR-006, BRULE-GAR-008, BRULE-GAR-009 |
| Use Cases | [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); [UC-009](../../../03-requirements/use-cases/UC-009-edit-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

PRD OPQ-015 resolves property/vocabulary scope and eligibility deferral. OPQ-013 retains genuine validation details; optional absence never authorizes fabricated values or eligibility claims.

No new behavior decision is required by the current sources. Supported controls, contracts and concurrent-edit mechanics remain selected-story JIT detail; Developers still assess delivery feasibility and size.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
