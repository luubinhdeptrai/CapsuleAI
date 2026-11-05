# US-GAR-002 — Correct and enrich authoritative garment information

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-002 |
| Version | 0.2 |
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

The eight categories and exact ten Dresses/Jumpsuits subtypes follow [PRD Sections 13.2/13.5](../../../02-product/PRD.md#132-controlled-initial-garment-subtypes). Optional subtype must match its category. Brand text and purchase Price (decimal amount plus explicit ISO 4217 currency) follow FR-GAR-012/013 and BRULE-GAR-010; neither is mandatory for saving or readiness. Price cannot be inferred from a photograph; any future Brand proposal requires explicit acceptance and cannot overwrite confirmed information. This adds no Brand-recognition feature. Future editing supports revising or clearing these values; category changes require review of incompatible subtypes. The bounded optional subtype/Brand/Price creation fields now belong to US-GAR-001; other enrichment and editing remain this story. The established pattern/climate checks continue for TOPS, PANTS, SKIRTS, OUTERWEAR and SHOES. Applicable layering remains required for TOPS/OUTERWEAR; bulk remains conditional on the layered check. DRESSES_JUMPSUITS, BAGS and HEADWEAR need an explicit product decision on composition and required compatibility inputs (PRD OPQ-012). Until resolved, retain and expose these saveable garments, explain that the relevant evaluation policy is not yet defined, and do not assert readiness or fabricate missing attributes. This limitation is distinct from a user omitting known required information. Brand and Price neither establish nor block readiness.

Supported descriptor review during entry and later edit; explicit acceptance, rule-specific readiness guidance, preserved user authority and affected-advice currentness.

## 4. Preconditions

- The user has authorized access to an existing current owned garment, or to its applicable review before confirmation.

## 5. Acceptance Criteria

### AC-01 — Review bounded information

Present accepted/proposed/unknown information distinctly and allow the supported subtype, layering, bulk, fit, silhouette, color/palette, pattern/noise, climate, style/occasion and material information defined in SRS Section 4.10 to be reviewed and corrected.

The optional-property and classification rules in Section 3 apply to this accepted outcome, including preservation of valid supplied values and correction/clearing of invalid category/subtype or incomplete Price pairs.

### AC-02 — Apply supported consistency

Reject unsupported values or inconsistent revisions without replacing accepted data. Bulk and Visual Noise use 1–5 bounds; SOLID implies NONE density. OTHER/UNKNOWN is accepted only where the source vocabulary defines it.

### AC-03 — Keep saving and readiness separate

Confirmed Primary Category and Dominant Color remain the saveable minimum. For TOPS/PANTS/SKIRTS/OUTERWEAR/SHOES under the established composition, readiness needs common pattern/climate information and applicable TOPS/OUTERWEAR layering, with bulk only for the layered checks requiring it. Missing required information blocks only the relevant evaluation; optional descriptors are not universal save/readiness gates.

### AC-04 — Preserve uncertainty

Unknown descriptors stay unknown unless justified evidence supports derivation. Material can be corrected or left unknown without a verified composition/durability claim; derivation cannot silently replace user-confirmed values.

### AC-05 — Confirm or cancel

Explicit confirmation establishes the revised authoritative profile and readiness. Cancellation or a known rejected/failed edit retains the prior accepted profile; later AI output cannot silently overwrite confirmed corrections.

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

PRD OPQ-012–014 track unresolved category policy and optional-field validation. No missing policy authorizes fabricated readiness or silent user-value changes.

No new behavior decision is required by the current sources. Supported controls, contracts and concurrent-edit mechanics remain selected-story JIT detail; Developers still assess delivery feasibility and size.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
