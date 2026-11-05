# US-GAR-004 — Review AI-assisted garment proposals before confirmation

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-004 |
| Version | 0.3 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-013](../../product-backlog.md#pbi-013--review-ai-assisted-garment-proposals-before-confirmation) |
| Domain | Garment |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |
| Product revision | 2026-11-05: Synchronized approved garment taxonomy and optional-property authority; no new Sprint selection or AI implementation. |

## 1. Story

As an authenticated owner, I want to inspect and correct assisted garment proposals, so that automation reduces entry effort while I control the final wardrobe information.

## 2. Value

Reduce repetitive garment entry while keeping the final wardrobe representation under user authority.

## 3. Scope

Use all applicable properties and complete approved vocabularies from the [source authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). Exactly 13 fashion properties are approved: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. Category and one dominant Color plus explicit user confirmation form the saving minimum. All other applicable properties are optional. Material supports zero or multiple distinct selections; Pattern is single and optional. Fit, Neckline and Sleeve Length apply only to Tops; Length uses separate Tops/Pants vocabularies; Silhouette applies only to Pants. Every supplied controlled value must match the source vocabulary and category; correct or clear invalid/inapplicable input rather than silently substitute or discard it. Unknown optional information is absent, not an invented UNKNOWN/OTHER/Etc value. Preserve real listed fallbacks such as Other, Other Materials and Colorful only where defined.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

All applicable property input is already included in Sprint 2 manual creation; this story retains only its separate future editing, photo or AI-assisted outcome. Future suggestions require explicit acceptance and cannot overwrite confirmed values; Price never comes from appearance. Metadata/media are separate, not extra properties.

## 4. Preconditions

- The user has authorized access to a supported selected garment image; usable analysis and a confirmed entry are outcomes, not assumed inputs.

## 5. Acceptance Criteria

### AC-01 — Keep analysis pending

While analysis is pending, show processing and no completed wardrobe addition. When available, present both usable processed preview and reviewable supported proposals; preview quality and prediction certainty are distinct.

The optional-property and classification rules in Section 3 apply to this accepted outcome, including preservation of valid supplied values and correction/clearing of invalid category/subtype or incomplete Price pairs.

### AC-02 — Qualify the preview

A usable preview retains an identifiable garment and its major regions, with background that does not materially obstruct correction/confirmation. Unusable processing offers retry/replacement/manual continuation rather than blocking otherwise usable manual input.

### AC-03 — Apply guarded confidence

Default evidence-based states are High Confidence at >=0.85, Needs Review at >=0.60 and <0.85, and Uncertain below 0.60. Missing/materially conflicting evidence, unreliable image inference or unmet required attribute/model validation prohibit High Confidence; validated calibration may be stricter only.

### AC-04 — Use understandable uncertainty

Localized semantic states are primary rather than raw scores. Needs Review prompts checking; Uncertain explains unreliable identification and permits correction/manual information. No fabricated confidence, material certainty or missing descriptor is introduced.

### AC-05 — Separate provenance and authority

Distinguish AI Suggested, User Confirmed, User Corrected and User Entered. Even a High Confidence proposal is not authoritative until explicit user confirmation; accepted corrections cannot be silently overwritten by late analysis.

### AC-06 — Confirm the minimum

The user can accept, correct or manually supply supported values, then save after confirming category/dominant color. Optional rich descriptors, an AI score and recommendation readiness are not extra save gates; no preconfirmation cancellation creates an owned item.

### AC-07 — Handle failures and uncertainty

Unavailable/failed analysis retains manual continuity and established confirmed information. Failed versus uncertain save outcomes remain distinct; review/safe retry does not duplicate one accepted addition, and late/canceled work does not claim a new acceptance.

### AC-08 — Preserve private use

Analysis/media exchanges and evidence remain authorized and purpose-limited. User confirmation/correction is not permission for MVP personal-data model training/improvement.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-012. Useful sequencing: PBI-008; this does not add a hard dependency.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Photo-supported manual input is [US-GAR-003](../garment/US-GAR-003-add-photo-supported-garment.md); rich enrichment is [US-GAR-002](../garment/US-GAR-002-correct-and-enrich-garment-information.md); locked quality verification is [US-QUAL-001](../quality/US-QUAL-001-validate-locked-garment-assistance-benchmark.md). No model architecture, algorithm, training pipeline or new confidence-quality percentage is selected.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-013](../../product-backlog.md#pbi-013--review-ai-assisted-garment-proposals-before-confirmation); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-01, BR-001, BR-002, BR-004, CAP-02, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-02, FEAT-AI-001, FEAT-AI-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AI-005–FR-AI-010; AI-REQ-001–AI-REQ-009, AI-REQ-012, AI-REQ-013; UI-005; SI-001; ERR-AI-001, ERR-AI-002; NFR-USE-002, NFR-REL-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-001, BRULE-GAR-007, BRULE-GAR-008 |
| Use Cases | [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

PRD OPQ-015 resolves property/vocabulary scope and eligibility deferral. OPQ-013 retains genuine validation details; optional absence never authorizes fabricated values or eligibility claims.

CAT-Q05: AI/QA with PO/BA must establish evidence for the required High Confidence attribute/model quality and any stricter calibration. No High Confidence claim is justified by an unvalidated raw score.

Question ownership and impact: [catalogue questions CAT-Q05](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
