# CapsuleAI — Business Rules

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## 1. Introduction

| Field | Value |
|---|---|
| Version / Status | 0.3 / Baseline Draft |
| Product revision | 2026-11-05: final 13-property/source-vocabulary approval; all applicable properties included in Sprint 2; obsolete attributes and eligibility/composition predicates superseded with IDs retained. |
| Last Updated | 2026-11-05 |
| Primary Owner | Business Analyst / Product Owner |
| Reviewers | Engineering / QA |
| Business Source | [BRD v0.5](../01-business/BRD.md) |
| Product Source | [PRD v0.4](../02-product/PRD.md) |
| Software Requirements Source | [SRS v0.5](SRS.md) |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) |
| Authoritative Location | `docs/03-requirements/business-rules.md` |

| Version | Date | Revision |
|---|---|---|
| 0.1 | 2026-10-06 | Initial extraction of reusable MVP domain rules, vocabularies, formulas, precedence, decision tables, boundary cases, and source traceability. Temporal rules and boundary examples define deterministic evidence interpretation. |
| 0.1.1 | 2026-10-06 | Defined completed elapsed 24-hour periods for behavioral age/recency and the latest surviving Wear Event as the normalized Wear-group evidence anchor. No other rule semantics changed. |
| 0.1.2 | 2026-10-06 | Defined purpose-limited private data use, removal/measurement retention, weather and commercial freshness, and navigation-independent utility; normalized current rule presentation and preserved established domain semantics. |
| 0.3 | 2026-11-05 | Final source-owned 13-property model, complete vocabularies, multiple Materials, category applicability, deferred eligibility and full applicable Sprint 2 property scope; existing IDs retained. |
| 0.4 | 2026-11-07 | Product Owner decisions: multiple Styles, Brand/Price validation, recovery rate limits, active-session-only revocation on reset, 30-day reset-record retention. Existing IDs retained. |

### 1.1 Purpose

This specification defines reusable CapsuleAI domain invariants and decisions for Product, Engineering, QA, and subsequent behavioral modeling. It consolidates the rules for garment saving and unevaluated eligibility, plus future outfit validity, effective behavioral evidence, contextual coverage, candidate utility, and assessment states.

### 1.2 Scope

Rules cover private account identity, user-controlled context, confirmed garment information, current owned outfits, Shuffle, feedback, user-reported Wear Events, history/utilization, the Personalized Everyday Capsule, capability-based gaps, and hypothetical strategic shopping. The MVP supports TOPS, DRESSES_JUMPSUITS, PANTS, SKIRTS, OUTERWEAR, SHOES, BAGS, HEADWEAR.

Observable interaction sequences and quality acceptance remain in the SRS. This document does not specify implementation mechanisms, physical data structures, interfaces, model design, ranking algorithms, or downstream artifacts. SRS recognition benchmarks, image limits, password/token lifetimes, and performance criteria retain their existing authority; only reusable domain policies are extracted here.

### 1.3 Source Documents and Authority

All four current source documents listed in document control were read. Business intent follows the BRD; product behavior follows the PRD; required software behavior and quality acceptance follow the SRS. The workflow governs artifact ownership, order, and change propagation. Other current material and historical proposals provide context only when consistent with these sources; illustrative workflow rules do not override current CapsuleAI domain decisions.

Each rule cites defining SRS requirements. Section 16 maps those sources to FEAT-*, BR-*, and relevant CAP-* identifiers. Absolute elapsed-time interpretation and latest-surviving Wear-group anchors are defined directly in the personalization/Wear rules and examples; contribution values, age bands and event semantics retain their established meaning.

### 1.4 Rule Identification Scheme

Identifiers use `BRULE-<family>-<three digits>`. Families are AUTH, PROF, GAR, OUT, PERS, WEAR, HIST, COV, GAP, MULT, and SHOP. Shuffle uses the OUT family. Identifiers remain stable after assignment; refinements preserve source links. BRULE-* identifies domain rules and must not be confused with upstream BR-* business requirements.


### 1.5 Rule Conventions

- **Must / only when** expresses a mandatory domain condition; **may** identifies permitted optional behavior.
- **Applicable** means required by the particular evaluation; it does not make every supported descriptor universally mandatory.
- **Unknown / unavailable** is absence of usable evidence, not a numeric zero or a fabricated compatibility value. UNKNOWN is an explicit vocabulary value only where defined.
- **Current** denotes the active assessment basis. Historical snapshots and hypothetical candidates do not establish current ownership.
- Decision tables use **Yes**, **No**, and **—** (irrelevant to that row). Required predicates must be established from authoritative or justified information; an unestablished required check cannot be treated as a successful check.
- **CompletedElapsedDays** counts fully elapsed 24-hour periods between the absolute EvaluationInstant and the applicable EvidenceAnchorInstant, as defined in BRULE-PERS-003. Original event-local calendar dates govern Wear grouping/history; absolute timestamps govern behavioral age and elapsed-day recency. Device timezone changes do not alter elapsed age.
- Examples are controlled fixtures, not universal wardrobe targets or product claims. Temporal examples use completed elapsed days and the latest surviving accepted Wear Event anchor for a normalized group.
- Source columns identify specification coverage, not implemented behavior or passed verification. Local rule sources and the consolidated traceability matrix must stay synchronized.

### 1.6 Rule Precedence

Apply the following order within the relevant domain operation. Steps 1–6 constrain validity; ranking starts only after all applicable hard checks succeed.

| Priority | Governing Boundary | Consequence |
|---|---|---|
| 1 | User authorization / ownership boundaries | Only the authorized user's information may be used or changed. |
| 2 | Current ownership and active state | Removed items and unowned candidates cannot enter current daily outfits. |
| 3 | Authoritative confirmed information | User-confirmed/corrected/entered-and-confirmed values take precedence over automated proposals. |
| 4 | Garment saving and eligibility | Category/one Color plus confirmation save; eligibility is not evaluated and optional omissions do not imply unsuitability. |
| 5 | Future outfit validity | Composition/compatibility/eligibility policy is deferred; authorization and identity remain active boundaries. |
| 6 | Available environmental/contextual hard validity | Enforce applicable environmental compatibility; unavailable weather follows the explicit fallback. |
| 7 | Soft personalization / ranking | Rank already-valid choices using available preferences and evidence. |
| 8 | Diversity / recency adjustments | Apply soft penalties or the permitted boost within the preceding boundaries. |

Soft ranking cannot override authorization, confirmed ownership or a future approved hard rule. No previous property-based compatibility predicate is active; do not infer eligibility from optional absence or undefined policy.

Candidate evaluation is a separately identified hypothetical scope: the candidate may join expanded hypothetical sets, while every owned constituent remains subject to the same applicable rules. History preserves prior meaning without restoring current eligibility. See BRULE-OUT-002, BRULE-OUT-008, BRULE-HIST-002, and BRULE-MULT-001.

## 2. Account and Identity Rules

### BRULE-AUTH-001 — Authorized Personal Operations

**Rule:** Personal wardrobe, profile, feedback, history, and assessment operations require authentication and authorization for the affected user's information. Unavailable authorized access is not an empty wardrobe and does not change personal state.

**Applies To:** all personal domain operations.

**Source:** FR-AUTH-008, ERR-AUTH-001, DATA-GAR-001.

### BRULE-AUTH-002 — Normalized Email Identity

**Rule:** Email identity is compared case-insensitively after trimming surrounding whitespace. Each normalized identity maps to at most one account. Provider-specific alias transformations are not applied; duplicate registration creates no second account and offers sign-in/recovery guidance.

**Applies To:** registration, login, account recovery.

**Source:** FR-AUTH-002, FR-AUTH-016, DATA-AUTH-002.

**Boundary:** ` Person@Example.com ` and `person@example.com` identify the same account; provider alias strings are not automatically merged.

### BRULE-AUTH-003 — Password Value Preservation

**Rule:** A password has 12–128 characters and may contain letters, numbers, symbols, spaces, and Unicode without mandatory character-class mixtures. Its value must not be silently trimmed, transformed, or normalized. A small approved denylist is permitted; an external breached-password service is not an MVP dependency.

**Applies To:** registration, login, password reset.

**Source:** FR-AUTH-001, FR-AUTH-002, FR-AUTH-015, DATA-AUTH-001.

### BRULE-AUTH-004 — Password-Reset Interaction Validity

**Rule:** A password-reset interaction is associated with its account, has a 30-minute lifetime, and permits one successful use. Expired or consumed interactions cannot change credentials. Successfully issuing a new password-reset interaction invalidates all earlier unused password-reset interactions for that account. A consumed, replaced or expired interaction is deleted 30 days after it became terminal; a usable interaction is never deleted (DATA-AUTH-006).

**Applies To:** password recovery and repeated recovery requests.

**Source:** FR-AUTH-017, FR-AUTH-018, DATA-AUTH-005, ERR-AUTH-002.

### BRULE-AUTH-005 — Completed Reset and Recovery Privacy

**Rule:** A successful reset sets a policy-valid password, consumes the reset interaction, revokes every currently active session of the account (already expired or revoked sessions keep their historical state and reason), and requires renewed authentication with the new password; no pre-reset session credential remains usable. Failed recovery must not be reported as completed. Initial recovery responses are equivalent for registered and unregistered email identities and do not disclose account existence. Excessive initiation, checking and completion attempts are limited per address and per client, with identical limited outcomes for registered and unregistered identities (FR-AUTH-019).

**Applies To:** account recovery, returning access.

**Source:** FR-AUTH-003, FR-AUTH-004, ERR-AUTH-002.

### BRULE-AUTH-006 — Separate Sessions and Revocation

**Rule:** Separate device/login sessions are logically distinct. Logout revokes the current session without automatically revoking another separate session. Successful refresh consumes the current Refresh Token and issues replacement access/refresh tokens; reuse of a rotated token revokes the affected session. Rotation cannot extend the session maximum; token/session lifetimes remain specified in SRS Section 3.1.1.

**Applies To:** session renewal, logout, multiple devices.

**Source:** FR-AUTH-007, FR-AUTH-010, FR-AUTH-013, FR-AUTH-014, DATA-AUTH-005.

### BRULE-AUTH-007 — Purpose-Limited Private Data Use

**Rule:** Personal data is used only as necessary for CapsuleAI functionality, personalization, wardrobe/history/utilization, validation/measurement, security and operational integrity within the authorized User's private experience. MVP personal images, profiles, Wear Events, feedback and corrections are not used for AI model training/improvement. Future personal-data training requires separate explicit opt-in and an approved future product/privacy change.

No public wardrobes, user-to-user wardrobe sharing, unrestricted third-party sharing or sale of personal data is introduced. External dependencies receive only purpose-minimal information: required selected weather location, email/necessary recovery-delivery information, or explicitly chosen shopping navigation without unrelated wardrobe/profile/history disclosure. Necessary active-account information and minimal history follow the retention/removal rules; this policy adds no Delete Account goal or legal certification.

**Applies To:** personal information, account/profile/wardrobe/history, validation/measurement and external exchanges.

**Source:** FR-AUTH-008, NFR-PRIV-001, NFR-PRIV-002, SI-002, SI-003, COM-003, FR-MET-006, DATA-RET-001, DATA-RET-002, DATA-RET-003.

## 3. Profile and Context Rules

### BRULE-PROF-001 — Inclusive Optional Personal Context

**Rule:** Body/gender information may be omitted, edited, or removed. It may influence ranking only softly and never prohibit a supported garment category or gate account entry. Removed information stops influencing future personalization and is represented as absent rather than replaced by an inferred profile.

**Applies To:** onboarding, profile revision, recommendation and candidate ranking.

**Source:** FR-AUTH-011, FR-PROF-005, FR-PROF-006, FR-PROF-007, FR-PERS-010, DATA-AUTH-004.

### BRULE-PROF-002 — Declared Preferences and Request Context

**Rule:** Style and occasion use the controlled vocabularies in BRULE-GAR-004. Common capsule need priorities remain distinct from the current request occasion; a request must not automatically overwrite ongoing priorities. The need scale is Not Relevant=0, Low=1, Medium=2, High=3, as used by BRULE-COV-002.

**Applies To:** profile setup/revision, initial ranking, Coverage.

**Source:** FR-PROF-002, FR-PROF-003, FR-PROF-004, DATA-AUTH-003.

### BRULE-PROF-003 — Selected Environmental Context

**Rule:** Device-location permission is optional; manual city/location selection remains available independently, including after denial. Assessments identify the location and environmental information actually used. A relevant location/environment change requires reevaluation or an outdated indication; missing context is not fabricated.

Weather is current at age ≤30 minutes from its applicable retrieval/observation timestamp. Older weather is refreshed before being used as current. An external weather request may delay an assessment at most 2 seconds; without usable current information by that boundary, environmental information is unavailable for that assessment and BRULE-OUT-006 governs fallback. A selected location alone does not prove a current forecast.

**Applies To:** context selection, recommendation, Coverage and Multiplier.

**Source:** FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-003, FR-WEATHER-004, FR-WEATHER-005, SI-002, ERR-WEATHER-001.

## 4. Garment Rules

### 4.1 Garment Ownership and Authority

### BRULE-GAR-001 — Canonical Garment Authority

**Rule:** Each owned garment belongs to the authorized user's wardrobe. Automated attribute proposals are not authoritative. Applicable User Confirmed, User Corrected, or User Entered-and-confirmed values establish the canonical profile; later automated analysis cannot silently overwrite them.

**Applies To:** ingestion, edits, all garment-dependent evaluations.

**Source:** DATA-GAR-001, FR-GAR-011, AI-REQ-001, AI-REQ-007, DATA-INT-001.

### 4.2 Saveable Garment Rules

### BRULE-GAR-002 — Saveable Minimum

**Rule:** A garment is Saveable when the user explicitly confirms its supported Primary Category and Dominant Color. Manual creation requires neither an image nor successful analysis. Missing optional rich descriptors do not prevent saving; canceled or unconfirmed work does not establish a confirmed wardrobe entry.

**Applies To:** assisted/manual creation and validation.

**Source:** FR-GAR-003, FR-AI-003, FR-AI-008, FR-AI-009, ERR-GAR-001.

### 4.3 Recommendation-Readiness Rules

### BRULE-GAR-003 — Evaluation-Specific Recommendation-Readiness

**Status: revised; old predicates superseded.**

**Rule:** Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

**Source:** FR-GAR-004/005, DATA-GAR-005, ERR-GAR-002; final Product Owner property-model decision.

### 4.4 Classification and Descriptor Rules

### BRULE-GAR-004 — Controlled Classification and Context Vocabularies

**Rule:** Category is one of the eight approved codes. Every supplied optional Subtype must belong to that Category and its complete approved source list; missing Subtype is valid. No invented fallback or silent replacement is permitted. MINI_DRESS and other overlapping names remain subtypes without automatic derivation. All Bags/Headwear choices are approved, not pending research.

The [garment-property authority](../../Initial%20files/Image%20to%20Properties%20Extraction.md) owns garment values. Profile style preferences, request Occasion and common needs keep their separate SRS Section 4.10 vocabularies; they are not garment Style or a garment Occasion property.

**Source:** FR-GAR-001/002, DATA-GAR-002; FR-PROF-002/004 for separate context.

### BRULE-GAR-005 — Bounded Garment Descriptors

**Rule:** Exactly 13 fashion properties are approved: Category, Subtype, Color, Material, Pattern, Style, Fit, Neckline, Sleeve Length, Length, Silhouette, Brand, Price. Category and one dominant Color plus explicit user confirmation form the saving minimum. All other applicable properties are optional. Material supports zero or multiple distinct selections; Pattern is single and optional. Fit, Neckline and Sleeve Length apply only to Tops; Length uses separate Tops/Pants vocabularies; Silhouette applies only to Pants. Every supplied controlled value must match the source vocabulary and category; correct or clear invalid/inapplicable input rather than silently substitute or discard it. Unknown optional information is absent, not an invented UNKNOWN/OTHER/Etc value. Preserve real listed fallbacks such as Other, Other Materials and Colorful only where defined.

| # | Property | Applicability | Required for Saving | Meaning / Value Authority |
|---|---|---|---|---|
| 1 | Category | All categories | Yes | One of eight category codes. |
| 2 | Subtype | All categories | No | Optional category-valid selection from the complete approved list. |
| 3 | Color | All categories | Yes | Exactly one dominant Color; Colorful is supported. |
| 4 | Material | All categories | No | Zero, one or multiple approved materials; no semantic duplicates or invented percentages. |
| 5 | Pattern | All categories | No | One optional approved Pattern. |
| 6 | Style | All categories | No | Optional set of zero, one or more distinct approved Style values (repeated selections do not repeat a value); Etc is explanatory, never a value. |
| 7 | Fit | Tops | No | Optional approved Fit. |
| 8 | Neckline | Tops | No | Optional approved Neckline. |
| 9 | Sleeve Length | Tops | No | Optional approved Sleeve Length. |
| 10 | Length | Tops and Pants | No | Category-specific approved Length values. |
| 11 | Silhouette | Pants | No | Optional approved Silhouette. |
| 12 | Brand | All categories | No | Optional user-entered text; no enum/catalogue. |
| 13 | Price | All categories | No | Optional amount originally paid: decimal amount and explicit ISO 4217 currency. |

Complete values are in the [source authority](../../Initial%20files/Image%20to%20Properties%20Extraction.md); no reduced local list, automatic compatibility descriptor or extra attribute is permitted.

**Source:** FR-GAR-002/006–010/012/013, DATA-GAR-002.

### BRULE-GAR-006 — Descriptor Consistency and Unknown Information

**Rule:** Unknown optional information remains absent rather than an invented value. Material selections form distinct semantic values, without percentages/proportions. Color is one approved dominant value; Pattern is one optional value; Style, like Material, is an optional set of distinct values. Validate applicability and supplied labels before confirmation; correct/clear invalid or inapplicable fields without silent substitution or discard. No cross-property inference is approved merely because a subtype overlaps length, style or occasion. Etc is not a Style value. Removed descriptors cannot be reconstructed as hidden fields or readiness gates.

**Source:** FR-GAR-002/006–010, DATA-GAR-002, AI-REQ-008.

### 4.5 Confidence and Provenance Rules

### BRULE-GAR-007 — Confidence Classification and Evidence Guards

**Rule:** Default semantic confidence is:

| Confidence | State |
|---|---|
| ≥0.85 | High Confidence |
| ≥0.60 and <0.85 | Needs Review |
| <0.60 | Uncertain |

High Confidence is prohibited when evidence is unavailable/materially conflicting, image quality prevents reliable inference, or the attribute/model has not met required validation quality. Communicate the limitation without fabricating a score. Validated attribute-specific calibration may only be stricter. Needs Review prompts checking; Uncertain prompts correction/manual information. Every proposal, including High Confidence, still requires user review/confirmation; numerical confidence is not the primary UI representation.

**Applies To:** AI proposal review and correction.

**Source:** AI-REQ-001, AI-REQ-004, AI-REQ-005, AI-REQ-012, AI-REQ-013, ERR-AI-002.

### BRULE-GAR-008 — Provenance Is Distinct from Confidence

**Rule:** AI Suggested, User Confirmed, User Corrected, and User Entered describe how a value was established. Confidence describes proposal uncertainty and cannot substitute for confirmation. User Entered values become authoritative through confirmation. Neither confidence nor user confirmation proves physical material composition or durability; uncertain material remains qualified or unknown.

**Applies To:** attribute review, explainability, candidate information.

**Source:** AI-REQ-006, AI-REQ-007, AI-REQ-009, FR-GAR-010, DATA-GAR-003.

### 4.6 Garment State Changes

### BRULE-GAR-009 — Accepted Edits and Removal

**Rule:** Only accepted edits/removal change current garment state; canceled, failed, or pending work must not be represented as accepted. Removal excludes the garment from new current outfits, current Coverage, and the current Multiplier baseline. Relevant accepted changes require refresh or an outdated indication for affected advice; historical meaning remains governed by BRULE-HIST-002. Accepted removal takes immediate effect in current ownership, recommendations, Coverage and the Multiplier baseline. Only necessary minimal historical information may remain; original removed-garment images and nonessential removed personal data must be physically deleted within 30 days.

**Applies To:** wardrobe maintenance and all dependent assessments.

**Source:** FR-WAR-003, FR-WAR-004, FR-WAR-005, FR-WAR-006, DATA-INT-002, DATA-INT-004, DATA-RET-002, DATA-HIST-001.

### 4.7 Optional Brand and Purchase Price

### BRULE-GAR-010 — Optional Brand and Purchase Price Authority

**Rule:** Brand is optional user-entered text across all eight categories, with no fixed enum or external catalogue. Preserve the confirmed value; unknown is not a fabricated brand. Price is the optional user-reported purchase price of the owned garment, represented by a decimal amount together with an explicit ISO 4217 currency code. An amount without currency, or currency without amount, is incomplete supplied information and must be corrected or cleared before confirmation; omitting both is valid and does not mean zero. Never infer currency from location or perform floating-point monetary calculations. This is not market/resale/replacement value or a retailer/affiliate price; no conversion feature is introduced. Neither field is a save or readiness requirement. Price must not be inferred from a photograph; any future Brand suggestion remains unconfirmed until user acceptance. Later analysis or image upload must not silently replace confirmed Brand or Price.

Future edits may revise or clear these properties with explicit user confirmation. Supplied valid values participate in the same accepted garment addition and safe-retry outcome; they do not create separate ownership or duplicate accepted additions. Approved validation (PRD OPQ-013, resolved 2026-11-07): Brand has at most 100 Unicode code points, a blank Brand is unspecified, and accepted Brand text is kept exactly. A Price of zero is a known amount, not a missing one; negative amounts, more than 12 integer digits and more fractional digits than the currency's ISO 4217 minor unit are rejected, never rounded; the currency is a valid upper-case ISO 4217 code. Nothing is defaulted or silently altered.

**Applies To:** manual entry, inspection, future editing and assisted-review protection across all eight categories.

**Source:** FR-GAR-012, FR-GAR-013, FR-GAR-011, DATA-GAR-002, DATA-GAR-003, DATA-INT-001, DATA-INT-004; PRD Sections 13.5/34.1.

## 5. Outfit Validity Rules

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

### 5.1 Structural Composition

### BRULE-OUT-001 — Supported MVP Outfit Composition

**Status: superseded policy; retained historical ID.**

The previous fixed composition or removed-property compatibility rule is no longer active. Future engine development must approve composition/compatibility using the current property model. No legacy role mapping, derived descriptor, readiness gate or replacement algorithm is selected here.

**Source:** supersession of FR-OUT-004 and FR-GAR-005; current FR-GAR-004 and PRD OPQ-015.

### 5.2 Garment Eligibility

### BRULE-OUT-002 — Current Daily Garment Eligibility

**Rule:** Daily constituents must be authorized, confirmed, active and currently owned; removed garments, drafts and hypothetical candidates cannot become current ownership. Recommendation eligibility has not been evaluated. Future engine policy, not accepted saving or optional information absence, determines whether a combination can be recommended. The candidate exception remains limited to hypothetical evaluation.

**Source:** FR-OUT-001, FR-WAR-005, FR-MULT-001, DATA-INT-002.

### 5.3 Layering Rules

### BRULE-OUT-003 — Compatible Layering Roles

**Status: superseded policy; retained historical ID.**

The previous fixed composition or removed-property compatibility rule is no longer active. Future engine development must approve composition/compatibility using the current property model. No legacy role mapping, derived descriptor, readiness gate or replacement algorithm is selected here.

**Source:** supersession of FR-OUT-004 and FR-GAR-005; current FR-GAR-004 and PRD OPQ-015.

### 5.4 Bulk Compatibility

### BRULE-OUT-004 — Applicable Bulk Relationship

**Status: superseded policy; retained historical ID.**

The previous fixed composition or removed-property compatibility rule is no longer active. Future engine development must approve composition/compatibility using the current property model. No legacy role mapping, derived descriptor, readiness gate or replacement algorithm is selected here.

**Source:** supersession of FR-OUT-004 and FR-GAR-005; current FR-GAR-004 and PRD OPQ-015.

### 5.5 Environmental Compatibility

### BRULE-OUT-005 — Environmental Bands and Suitability

**Status: superseded policy; retained historical ID.**

The previous fixed composition or removed-property compatibility rule is no longer active. Future engine development must approve composition/compatibility using the current property model. No legacy role mapping, derived descriptor, readiness gate or replacement algorithm is selected here.

**Source:** supersession of FR-OUT-004 and FR-GAR-005; current FR-GAR-004 and PRD OPQ-015.

### BRULE-OUT-006 — Unavailable Environmental Context

**Rule:** Preserve actual request/weather context and the existing freshness/2-second acquisition boundary. Missing weather is disclosed and not fabricated; it does not by itself label saved garments unsuitable. Future approved engine policy determines defensible reduced-context evaluation; this rule does not restore removed garment suitability attributes.

**Source:** BRULE-PROF-003, ERR-WEATHER-001, FR-OUT-012, FR-WEATHER-004.

### 5.6 Pattern / Visual Noise Compatibility

### BRULE-OUT-007 — Severe Pattern and Visual Noise Limit

**Status: superseded policy; retained historical ID.**

The previous fixed composition or removed-property compatibility rule is no longer active. Future engine development must approve composition/compatibility using the current property model. No legacy role mapping, derived descriptor, readiness gate or replacement algorithm is selected here.

**Source:** supersession of FR-OUT-004 and FR-GAR-005; current FR-GAR-004 and PRD OPQ-015.

### 5.7 Hard vs Soft Factors

### BRULE-OUT-008 — Validity Before Personalized Ranking

**Rule:** Preserve authorized confirmed ownership and valid-before-ranking intent. Actual composition/compatibility/eligibility policy is deferred to engine development; no old property predicate or category-to-role mapping is active. Soft preferences cannot override a future approved hard rule or impose body/gender category restrictions. User-profile context and feedback/history remain separate from the 13 garment properties.

**Source:** FR-OUT-003/005, FR-PERS-006/010/011; Section 3.7.1 of SRS.

### 5.8 Distinct Choices

### BRULE-OUT-009 — Canonical Outfit Identity and Available Choice

**Rule:** An outfit combination is identified by its set of constituent garment identities; permutation creates no new outfit. Use the same identity meaning for advice, feedback targets, Wear normalization, Coverage, and Multiplier. Present at least three distinct valid options when they exist; otherwise the actual one, two, or zero valid options are authoritative and must not be padded with duplicates or invalid combinations.

**Applies To:** recommendation options and every distinct-outfit count.

**Source:** DATA-OUT-001, FR-OUT-006, FR-OUT-007, FR-OUT-008, ERR-OUT-002.

## 6. Shuffle Rules

### BRULE-OUT-010 — Single-Slot Shuffle Invariants

**Rule:** A successful Shuffle replaces exactly the user-selected garment slot with an eligible compatible alternative. All other constituent identities and the assessment context remain fixed, and the resulting outfit must satisfy every applicable validity rule. If no replacement exists, retain the selection and explain the limitation. Shuffle alone creates neither Like, Dislike, nor a Wear Event.

**Applies To:** selected-slot replacement.

**Source:** FR-OUT-014, FR-OUT-015, FR-OUT-016, FR-OUT-017, ERR-OUT-003.

## 7. Feedback and Personalization Rules

### 7.1 Like / Dislike

### BRULE-PERS-001 — Effective Outfit-Targeted Feedback (founder override 2026-11-09: store-only, no weights)

**Rule:** Like and Dislike are stored as user-expressed preference state on the identified outfit/post target with no fixed numeric contribution. Like is available on AI-generated targets (Today strip, chat suggestions) and community posts; Dislike is available on AI-generated targets only (Today + chat suggestions), never on community posts. Contradictory feedback is not simultaneously effective for one user's target. State persists until changed, cleared, or superseded by user action. Dislike does not permanently ban constituent garments/categories and does not by itself change validity. Preference actions and views do not create Wear Events. No +1/−2 contribution, aging band, or decay applies; the matching engine consumes stored events under its own rules.

**Applies To:** feedback recording/revision and event storage for engine use.

**Source:** FR-PERS-001, FR-PERS-002, FR-PERS-003, FR-PERS-005, DATA-WEAR-001.

### 7.2 Wear-Based Evidence

### BRULE-PERS-002 — Wear Event Storage (founder override 2026-11-09: store-only, no weights)

**Rule:** An accepted user-reported Wear Event is stored as a historical event with its outfit, timestamp, and context. It is not verified physical wear and cannot bypass hard validity. No fixed +2 increment, strength comparison vs Like, outfit/day cap increment, or time decay applies in business rules; the matching engine consumes stored Wear events under its own rules. Duplicate/intentional-repeat identity and history preservation remain governed by BRULE-WEAR-001/002.

**Applies To:** Wear Event storage for engine use and history.

**Source:** FR-PERS-008, FR-WEAR-011, FR-PERS-011.

### 7.3 Evidence Aging

### BRULE-PERS-003 — Evidence Aging (superseded by founder override 2026-11-09: no decay)

**Rule:** No business-rule aging band or decay applies to Like/Dislike/Wear influence. `CompletedElapsedDays = floor((EvaluationInstant - EvidenceAnchorInstant) / 24 hours)` and the former 100%/50%/25%/0% bands are retained here only as a superseded definition for traceability; they are not active ranking obligations. Feedback state persists until user revision/clearing; history retention follows BRULE-WEAR-005 and BRULE-HIST-003. The matching engine defines any recency handling under its own rules.

**Applies To:** event timestamp storage; no business-rule ranking decay.

**Source:** FR-PERS-007, FR-PERS-012, DATA-WEAR-001, DATA-RET-002.

### 7.4 Recency and Diversity

### BRULE-PERS-004 — Soft Recency and Overlooked-Garment Adjustment (superseded by founder override 2026-11-09: engine-owned, no business-rule penalty)

**Rule:** No business-rule recency penalty or overlooked-garment boost applies. The matching engine consumes stored Like/Dislike/Wear events under its own rules; hard validity always precedes any engine preference. The table below is retained only for traceability and is not an active ranking obligation.

| Recency / Diversity Situation | Former Ranking Effect (superseded) |
|---|---|
| Exact-outfit recency of 0–2 completed elapsed days | Formerly strong soft diversity penalty. |
| Exact-outfit recency of 3–7 completed elapsed days | Formerly mild soft diversity penalty. |
| Exact-outfit recency of >7 completed elapsed days | Formerly no recency penalty. |
| Recommendation-Garment with no recorded wear for ≥14 completed elapsed days | Formerly possible small boost. |

The ≥14-day boundary uses the existing applicable evidence anchor. If the sources/evidence do not establish that anchor, do not invent a start date or treat garment creation as an assumed origin. This elapsed-day interpretation introduces no new garment-lifecycle event. Penalties never invalidate an outfit. The optional boost cannot override hard validity, strong explicit negative feedback, or current context relevance. No extra numerical penalty/boost magnitude is specified.

**Applies To:** personalized ordering and recorded-utilization diversity.

**Source:** FR-PERS-009, FR-PERS-011, FR-ANL-003.

### 7.5 Evidence Normalization

### BRULE-PERS-005 — Exact-Outfit / Event-Local-Day Grouping (founder override 2026-11-09: history grouping only, no increment)

**Rule:** For the same user, exact outfit identity, and original event-local calendar date, surviving accepted Wear Events form one history group for display/counting. No Wear-based preference increment, cap (+2), or decay applies. Grouping is not event identity; separate explicit reports remain separate historical events, and different outfits/dates are not collapsed.

**Applies To:** Wear history grouping only.

**Source:** FR-PERS-012, DATA-OUT-001, DATA-WEAR-003, ERR-WEAR-001.

### BRULE-PERS-006 — Recalculation of Stored Evidence (founder override 2026-11-09: no decay)

**Rule:** Accepted feedback revision/clearing, Wear correction/removal, or relevant context revision changes stored state. Reapply current feedback state and surviving events. For each affected outfit/original-local-date group, update the latest surviving accepted absolute Wear timestamp; no age/decay recalculation applies. A valid timestamp or outfit correction may change a group's membership or anchor; removing its latest event moves the anchor to the latest remaining event. Unrelated history remains unchanged.

**Applies To:** feedback/event revision and history.

**Source:** FR-PERS-013, FR-WEAR-010, FR-ANL-007, DATA-WEAR-004, DATA-RET-001.

## 8. Wear Event Rules

### 8.1 Wear Event Identity

### BRULE-WEAR-001 — Logical Reporting Intention

**Rule:** One logical Wear action is one explicit user intention to report one wearing instance. A new report requires a current valid owned outfit, remains user-reported, and defaults to the accepted current time of the action. Report identity is distinct from the outfit/day grouping used for ranking.

**Applies To:** Wear This Today and accepted report creation.

**Source:** FR-WEAR-001, FR-WEAR-011, DATA-WEAR-003.

### 8.2 Duplicate vs Intentional Repeat

### BRULE-WEAR-002 — Retry Identity and Intentional Repetition

**Rule:** Retry, repeated delivery, or reprocessing of the same logical action yields the same event. A new explicit initiation yields a separate event, including the same outfit or a different outfit within the same local day. User + outfit + local date is not sufficient duplicate identity. Failed/pending attempts are not accepted new reports.

**Applies To:** repeated reporting, uncertain communication and event counting.

**Source:** FR-WEAR-002, FR-WEAR-003, FR-WEAR-009, DATA-INT-004, ERR-WEAR-001.

### 8.3 Time and Timezone

### BRULE-WEAR-003 — Original Event-Local Time

**Rule:** Each event retains an absolute timestamp, associated timezone or UTC offset, original local date/time, and available occasion/context. Later device-timezone changes during travel must not automatically re-date the event. Missing optional context is not fabricated; present history using the original event-local basis. Original event-local date/time serves history and same-day normalization grouping; absolute timestamps serve elapsed behavioral age, decay, and elapsed-day recency. Travel changes neither the preserved grouping date nor age calculated at the same absolute EvaluationInstant.

**Applies To:** Wear reports, history grouping, time interpretation.

**Source:** FR-WEAR-004, FR-WEAR-005, FR-ANL-001, DATA-WEAR-002.

### 8.4 Correction

### BRULE-WEAR-004 — Bounded Individual Correction

**Rule:** Correction targets an existing individual report's applicable outfit, occasion/context, and local time. Corrected time must not be future and must stay within the event's original local calendar day. Accepted corrections preserve that original event-local context. Invalid, failed, or canceled corrections leave the last accepted event unchanged; no unrelated record is changed. A valid accepted timestamp correction requires recomputing the affected group's latest surviving absolute anchor and applicable aging/recency under BRULE-PERS-006.

**Applies To:** event-specific history correction.

**Source:** FR-WEAR-006, FR-WEAR-012, DATA-WEAR-004, ERR-WEAR-002.

### 8.5 Removal

### BRULE-WEAR-005 — Individual Event Removal

**Rule:** Accepted removal immediately excludes the specific report from effective history, utilization, recency and personalization. Applicable personal event data must be physically deleted within 30 days. The underlying Outfit, Garments and unrelated Wear Events remain. Canceling or known failure before acceptance does not apply removal; uncertainty is not presented as acceptance. Recompute the surviving group's latest accepted absolute anchor and age/recency; no survivors means no group Wear contribution, as defined in BRULE-PERS-006.

**Applies To:** event-specific removal and canceled mutations.

**Source:** FR-WEAR-007, FR-WEAR-008, FR-WEAR-012, DATA-RET-001, ERR-WEAR-002.

### 8.6 Effects on History and Personalization

### BRULE-WEAR-006 — Accepted Report Effects

**Rule:** Accepted creation/correction/removal updates applicable history, utilization, recency, and normalized aged evidence for the affected report/combination. Recompute surviving outfit/day contributions and their latest accepted absolute anchors, then completed elapsed-day age, decay, and exact-outfit recency under BRULE-PERS-006, without changing unrelated events. Record meanings distinguish creation, correction, and removal; retries must not inflate accepted event counts.

**Applies To:** history, personalization and interaction measurement.

**Source:** FR-WEAR-010, FR-ANL-007, FR-MET-002, FR-MET-005, DATA-WEAR-004.

## 9. Wardrobe History and Utilization Rules

### BRULE-HIST-001 — Recorded Use Is Reported Evidence

**Rule:** Use frequency, recency, variety, and overlooked-item information derives from accepted effective Wear Events. Legitimate same-day reports remain separately understandable even when ranking normalizes them. No recorded use does not prove physical nonuse; incomplete logging and no-events states must not produce fabricated statistics.

**Applies To:** wardrobe use/history and relevant personalization.

**Source:** FR-ANL-002, FR-ANL-003, FR-ANL-004, FR-ANL-005, FR-WAR-002.

### BRULE-HIST-002 — Historical Snapshots vs Current Wardrobe

**Rule:** Historical outfit/garment representations remain understandable after garment removal and are labeled Removed from wardrobe where applicable. Such snapshots do not establish active ownership or enter new current outfits, Coverage, or the Multiplier baseline. Historical views preserve original event-local time rather than travel-based re-dating. Retain only the minimal snapshot needed to understand past records. Original removed-garment images and other nonessential removed personal data are not required historical evidence and follow the 30-day physical-deletion boundary.

**Applies To:** history inspection and active inventory boundaries.

**Source:** FR-ANL-001, FR-ANL-006, FR-WAR-005, DATA-HIST-001, DATA-HIST-002, DATA-RET-002.

### BRULE-HIST-003 — Ranking Horizon Is Not Retention

**Rule:** Events older than the 90-day behavioral ranking window may remain necessary meaningful history while the account is active; zero ranking influence is not deletion. Garment removal preserves only minimal understandable historical records and applies BRULE-GAR-009's 30-day removed-image/nonessential-data deletion. Accepted individual event removal applies BRULE-WEAR-005's immediate effective exclusion and 30-day applicable personal-data deletion.

User-linked engagement/measurement information has a separate maximum retention of 90 days; then delete or aggregate/de-identify it so it is no longer maintained as user-linked personal behavior. Ordinary engagement metrics do not require raw private images/sensitive profiles. This measurement boundary neither deletes necessary unremoved Wear history/functional feedback state nor extends removed-data deletion deadlines; it does not permit indefinite user-linked measurement copies.

**Applies To:** history retention boundaries and behavioral aging.

**Source:** DATA-RET-001, DATA-RET-002, DATA-HIST-001, FR-ANL-003, DATA-RET-003, FR-MET-006.

## 10. Wardrobe Coverage Rules

### 10.1 Capsule Need Mapping

### BRULE-COV-001 — Personalized Everyday Capsule Need Mapping

**Rule:** Coverage evaluates one Personalized Everyday Capsule using relevant common needs, declared style, available climate/context, and the current confirmed eligible wardrobe. The need-to-occasion mapping is:

| Capsule Need | Relevant Occasion Codes |
|---|---|
| Everyday / Casual | EVERYDAY, DATE, SOCIAL_EVENT |
| Work / Professional | WORK |
| School / University | SCHOOL_UNIVERSITY |
| Formal / Special Event | FORMAL_EVENT |
| Travel | TRAVEL |
| Sport / Active | SPORT_ACTIVITY |

These are contextual needs, not universal mandatory wardrobe contents. A current request occasion remains distinct from common priorities.

**Applies To:** contextual Coverage and capability-based gaps.

**Source:** FR-ANL-008, FR-ANL-009, FR-PROF-003, FR-PROF-004, DATA-ANL-001.

### 10.2 Need Priorities

### BRULE-COV-002 — Need Priority Weights

**Rule:** Use exactly these priority weights:

| Need Priority | Weight |
|---|---|
| Not Relevant | 0 |
| Low | 1 |
| Medium | 2 |
| High | 3 |

Only needs with positive weight contribute to the overall weighted score. Weight 0 excludes a need from its denominator and from gap consideration; common priorities may be revised independently of request occasion.

**Applies To:** profile needs, Coverage denominator, Gap Analysis.

**Source:** FR-PROF-003, FR-ANL-010, FR-GAP-001, DATA-ANL-001.

### 10.3 Need Coverage

### BRULE-COV-003 — Valid Distinct Outfits per Need

**Rule:** Count unique valid outfit combinations for the need's mapped occasion context using current eligible garments and the applicable validity rules. The same constituent identity set counts once for that need, including repeated permutations or occurrence under multiple mapped occasions.

`NeedCoverage = min(ValidDistinctOutfitsForNeed / 3, 1)`

NeedCoverage is a ratio in [0, 1]. Per-need display is 0 valid outfits → 0%; 1 → approximately 33%; 2 → approximately 67%; ≥3 → 100%. Validity and sufficient-data conditions govern whether the count is defensible; ranking cannot add invalid combinations.

**Applies To:** per-need Coverage and underserved-need evidence.

**Source:** FR-ANL-008, FR-ANL-012, DATA-OUT-001, DATA-ANL-001.

### 10.4 Overall Coverage Score

### BRULE-COV-004 — Priority-Weighted Coverage Formula

**Rule:** When sufficient-data conditions hold, calculate:

`CoverageScore = 100 × Σ(PriorityWeight × NeedCoverage) / Σ(PriorityWeight)`

Both sums use positive-weight needs only. The result is a contextual 0–100 score, not universal wardrobe completeness. Changing relevant priorities may change the same wardrobe's score; no extra priority, threshold, or normalization formula is introduced.

**Applies To:** overall Wardrobe Coverage Score.

**Source:** FR-ANL-010, FR-ANL-011, FR-ANL-015.

### 10.5 Sufficient-Data Rules

### BRULE-COV-005 — Numeric Availability vs Insufficient Information

**Rule:** A numeric Coverage score requires positive-priority needs plus complete evaluation under a future approved composition, compatibility and sufficient-evidence policy. The old ready-role gate is superseded for all categories. Undefined policy or missing evaluation evidence yields an unevaluated/incomplete assessment, not 0% or a fabricated gap. Existing priorities, ratios and weighted formula remain future product requirements; their arithmetic alone does not prove compatibility.

**Source:** FR-ANL-011/013/015, ERR-ANL-001; SRS Sections 3.7.1/3.14.1.

### 10.6 Outdated Coverage

### BRULE-COV-006 — Relevant Changes Invalidate Current Coverage

**Rule:** Relevant changes to current wardrobe/confirmed attributes, common needs/priorities, assessment context, or applicable validity rules require reevaluation or a clear outdated indication. Removed garments are excluded from the next current calculation. Prior scores must not silently remain current after failed refresh.

**Applies To:** Coverage refresh and dependent gap evidence.

**Source:** FR-ANL-013, FR-ANL-014, FR-WAR-006, ERR-ANL-002.

## 11. Gap Analysis Rules

### BRULE-GAP-001 — Underserved Capability before Candidate Product

**Rule:** A positive-priority need with NeedCoverage <1 (below 100% displayed coverage) may prompt gap analysis. A gap identifies an evidence-supported underserved capability/bottleneck before considering candidate characteristics. A specific product is a possible response, not the definition of the gap; supported gaps may lead to hypothetical utility evaluation.

**Applies To:** Coverage-to-gap interpretation and candidate needs.

**Source:** FR-GAP-001, FR-GAP-002, FR-GAP-003, DATA-ANL-002.

**Example:** Underserved versatile neutral work-compatible footwear is a capability gap. A particular shoe is a candidate only if its attributes and utility address that context; no universal footwear purchase is implied.

### BRULE-GAP-002 — No Fabricated Gap or Purchase Obligation

**Rule:** Insufficient Coverage cannot establish a numeric zero, a specific gap, or a compulsory purchase. No important gap/no useful candidate is a valid outcome and must not create manufactured demand. Users can continue wardrobe/context/insight work without shopping.

**Applies To:** insufficient, no-gap, and no-useful-candidate outcomes.

**Source:** FR-GAP-004, FR-GAP-005, FR-SHOP-004, ERR-ANL-001.

## 12. Wardrobe Multiplier Rules

### 12.1 Baseline and Candidate Sets

### BRULE-MULT-001 — Current Baseline and Hypothetical Addition

**Rule:** W is the current confirmed active owned wardrobe; counted combinations use only garments ready for applicable checks and exclude removed inventory. The candidate is a sufficiently described hypothetical addition, not an owned garment. Evaluation/preview changes neither ownership nor Wear history. O(W) and O(W ∪ {candidate}) apply the same context, validity/business-rule version, and uniqueness meaning. Candidate descriptions require explicit user-provided evidence or an identifiable configured credible source under BRULE-SHOP-001; this does not add a candidate capture/import goal.

**Applies To:** candidate utility and hypothetical previews.

**Source:** FR-MULT-001, FR-MULT-002, FR-MULT-006, DATA-INT-002, DATA-INT-003, DATA-ANL-003, FR-SHOP-001.

### 12.2 Outfit Uniqueness

### BRULE-MULT-002 — Identity-Based Set Comparison

**Rule:** Both outfit sets use the garment-identity semantics in BRULE-OUT-009. Permutations of the same identity set count once, and outfits already in the current set do not count as new. The candidate remains separately identifiable from owned constituents; its presence is hypothetical, not a change to their ownership.

**Applies To:** baseline/expanded counting and newly enabled previews.

**Source:** FR-MULT-002, FR-MULT-003, DATA-OUT-001, FR-MULT-005.

### 12.3 Multiplier Formula

### BRULE-MULT-003 — Incremental Unique Valid Outfit Count

**Rule:** Calculate the cardinality of the newly enabled set:

`WardrobeMultiplier(candidate) = |O(W ∪ {candidate}) − O(W)|`

O produces unique valid outfit combinations under the shared context/rule basis. Multiplier is an incremental count, not a weighted score, forecast of wear, purchase guarantee, or commercial conversion measure. Personalized ordering does not redefine hard-valid set membership.

**Applies To:** candidate utility and count interpretation.

**Source:** FR-MULT-002, FR-MULT-003, FR-OUT-003, FR-SHOP-007.

### 12.4 Exact Evaluation

### BRULE-MULT-004 — Exact Evaluation Prerequisites

**Rule:** An exact +N New Outfits requires an approved future engine policy and sufficient evaluated evidence for candidate and owned combinations, required relevant context, complete current and expanded evaluations, complete uniqueness/deduplication, and the same context/rule version/identity semantics for both sets. Displayed advice subsets do not substitute for the complete evaluation. Newly enabled previews contain and identify the hypothetical candidate and share the evaluated context. Missing optional commercial fields do not defeat these prerequisites.

**Applies To:** exact counts, reasons and hypothetical previews.

**Source:** FR-MULT-004, FR-MULT-005, DATA-ANL-004, FR-SHOP-004.

### 12.5 Evaluated Zero

### BRULE-MULT-005 — Completed Empty New Set

**Rule:** Evaluated Zero means every exact-evaluation prerequisite succeeded and the newly enabled set is empty; +0 New Outfits is then permitted. Zero is not evidence of missing information, failure, timeout, or an unavailable capability. A lack of current valid combinations alone must not be substituted for a completed candidate comparison.

**Applies To:** honest zero-utility outcomes.

**Source:** FR-MULT-007, ERR-ANL-001, DATA-ANL-004.

### 12.6 Incomplete

### BRULE-MULT-006 — Insufficient Inputs or Unestablished Completion

**Rule:** Use Incomplete when required candidate/owned information is insufficient or reliable full evaluation cannot be established. Partial or timed-out work does not justify an exact count or evaluated zero. Explain the limitation; no exact +N or current newly enabled preview may be claimed without its required evaluation evidence.

**Applies To:** partial, insufficient and non-complete candidate assessments.

**Source:** FR-MULT-007, FR-SHOP-002, ERR-ANL-001, ERR-SHOP-001.

### 12.7 Unavailable

### BRULE-MULT-007 — Capability Cannot Operate

**Rule:** Use Unavailable when the evaluation capability cannot operate. Preserve understandable candidate/gap context and permitted independent use, suppress exact +N, and do not substitute +0. Capability unavailability is distinct from available evaluation with insufficient inputs or incomplete work.

**Applies To:** dependency/capability failure during candidate evaluation.

**Source:** FR-MULT-007, ERR-SHOP-001, ERR-DEP-001.

### 12.8 Outdated

### BRULE-MULT-008 — Input and Rule-Based Freshness

**Rule:** A previous result is Outdated when relevant active wardrobe composition, confirmed garment attributes, candidate attributes, assessment context, or validity/business rules no longer match its basis. Do not present its prior count/previews as current exact evidence. Freshness is input/rule based; no arbitrary fixed TTL is specified. Weather evidence follows BRULE-PROF-003; commercial currentness follows BRULE-SHOP-001. Commercial/destination unavailability alone does not change otherwise valid incremental outfit utility.

**Applies To:** assessment reuse and changed evaluation bases.

**Source:** FR-MULT-008, FR-OUT-009, DATA-ANL-004, ERR-ANL-002.

### 12.9 Re-evaluation

### BRULE-MULT-009 — Reestablishing a Current Assessment

**Rule:** Reevaluation must use the current relevant baseline and candidate with one consistent context/rule version for both sets. Establish all exact prerequisites again before replacing outdated status with Exact or Evaluated Zero. If reevaluation cannot support a current exact result, explain the applicable Incomplete/Unavailable outcome without presenting the old result as refreshed. Historical assessments remain identifiable as prior results.

**Applies To:** changed inputs, retry and current-result replacement.

**Source:** FR-MULT-002, FR-MULT-004, FR-MULT-007, FR-MULT-008, ERR-ANL-002.

## 13. Strategic Shopping Rules

### BRULE-SHOP-001 — Utility Information and Conditional Commercial Guidance

**Rule:** A usable recommendation identifies applicable candidate visual/name, category/subtype, gap, key attributes, supported utility, reasons and newly enabled hypothetical previews. Required candidate information is supported by explicit user-provided information or an identifiable configured credible source; no attribute/product claim is fabricated. Exact utility follows BRULE-MULT-004 and limitations remain explicit.

Price, material, durability/longevity, retailer and link are optional, credible/available and qualified when estimated. External commercial information preserves identifiable source and retrieval/last-checked time. Price/availability is current informational guidance up to 24 hours inclusive; older information is refreshed, explicitly stale/last checked, or omitted. No guaranteed stock, price, partnership, verified purchase/transaction or durability is implied. Missing/stale optional commercial fields do not invalidate otherwise supported wardrobe utility.

**Applies To:** candidate recommendations and details.

**Source:** FR-SHOP-001, FR-SHOP-002, FR-SHOP-003, FR-SHOP-004, ERR-SHOP-002, DATA-ANL-003.

### BRULE-SHOP-002 — External Navigation Does Not Establish Purchase

**Rule:** External shopping is an optional, explicitly external action to an available destination. Opening/returning or failed handoff establishes neither ownership nor verified purchase/sale, transaction success, automatic ingestion, or Wear. Known destination failure retains available candidate/gap/utility context. Attempts/openings and candidate views have distinct measurement meaning from sales. A known unavailable destination affects navigation only: retain otherwise valid candidate information, gap explanation, multiplier, reasons and hypothetical previews where available. It does not become +0, candidate invalidity, ownership, verified purchase failure or a transaction state. External navigation receives no unrelated private wardrobe/profile/history data.

**Applies To:** external destinations, user return and interaction interpretation.

**Source:** FR-SHOP-005, FR-SHOP-006, FR-SHOP-007, FR-MET-003, ERR-SHOP-003, DATA-INT-003, COM-003.

### BRULE-SHOP-003 — Utility Independent of Commerce

**Rule:** Core wardrobe, styling, and insight value remains available without purchase, affiliate participation, or monetization. MVP has no native cart, payment, orders, fulfillment, or verified sales attribution. Do not manufacture merchant partnerships, stock, scarcity, durability certainty, or extra vanity scores to complete advice.

**Applies To:** shopping boundaries, no-useful-candidate and optional commerce.

**Source:** FR-SHOP-003, FR-SHOP-004, FR-SHOP-008, FR-GAP-005.

## 14. Decision Tables

These tables restate current rules and explicitly mark superseded policies. Missing optional properties do not affect saving. No fixture authorizes a removed descriptor or a recommendation algorithm; future evaluation fixtures require approved policy.

### 14.1 Garment Saving and Unevaluated Eligibility

| Confirmed supported Category and Color | Supplied optional values valid/applicable | Saving | Recommendation eligibility |
|---|---|---|---|
| No | Any | Correct the minimum; no confirmed entry. | Not evaluated. |
| Yes | No | Correct or clear invalid supplied information; do not silently replace it. | Not evaluated. |
| Yes | Yes, including all omitted optional properties | Accept with every supplied valid value; image/AI are not prerequisites. | Not evaluated; no invalid/unsuitable/ineligible inference. |

**Rules:** BRULE-GAR-001–006/010. Metadata and optional media are outside the 13 fashion properties.

### 14.2 Outfit Validity

The old fixed composition and removed-property decision table is superseded (BRULE-OUT-001/003–005/007). No replacement truth table is approved. Future engine work must establish composition, compatibility and sufficient evidence before valid outfit claims; ownership and identity invariants remain.

### 14.3 Coverage Availability

| Positive needs | Approved engine policy and complete evidence | Numeric result |
|---|---|---|
| No | Any | Insufficient needs; no numeric score. |
| Yes | No | Unevaluated/incomplete; no fabricated zero or gap. |
| Yes | Yes | Apply existing ratios/weights only to the defensible complete valid-outfit counts. |

**Rules:** BRULE-COV-002–005. This table supplies no engine algorithm or garment eligibility gate.

### 14.4 Multiplier Freshness and Evaluation State

Freshness classifies a **previous assessment**; evaluation state classifies a **current attempt**. If changed inputs make an old result Outdated while a new attempt is Unavailable, neither result is a current exact assessment.

| Previous Assessment Exists | Relevant Inputs / Rules Still Match | Previous Assessment Interpretation |
|---|---|---|
| No | — | No prior result to reuse; evaluate the current basis |
| Yes | No | Outdated; previous exact count/previews cannot be presented as current |
| Yes | Yes | Freshness alone does not change its recorded completion/state; apply its actual evidence |

For a current attempt, required inputs include future candidate-evaluation evidence, applicable future owned-combination evidence, relevant context, and a consistent context/rule/identity basis. Complete evaluation includes both sets and full uniqueness/deduplication.

| Evaluation Capability Available | Required Inputs / Shared Basis Sufficient | Full Evaluation Completed | Newly Enabled Unique Count | Current Attempt State / Presentation |
|---|---|---|---|---|
| No | — | — | — | Unavailable; no exact +N or false +0 |
| Yes | No | — | — | Incomplete; no exact +N |
| Yes | Yes | No | — | Incomplete; partial work is not exact evidence |
| Yes | Yes | Yes | 0 | Evaluated Zero; +0 New Outfits |
| Yes | Yes | Yes | >0 | Exact; +N New Outfits and supported hypothetical previews |

No fixed TTL or output from incomplete work may replace the evidence predicates. Optional commercial information is outside these exactness predicates.

**Rules:** BRULE-MULT-001–BRULE-MULT-009, BRULE-SHOP-001.

### 14.5 Logical Wear Actions and Effective Ranking

These outcomes assume accepted actions unless a row states otherwise. Each original-event-local-date group contributes at most one +2 increment before aging. Its age anchor is the latest surviving accepted Wear Event's absolute timestamp; correction/removal recomputes that anchor, completed elapsed-day age, and applicable decay/recency.

| Situation | Effective History / State | Wear Preference Consequence |
|---|---|---|
| New explicit valid Wear action | One new report | Eligible for the outfit/day increment; anchor on the latest surviving accepted absolute timestamp |
| Retry/reprocessing of the same logical action | No additional report | No additional increment |
| New explicit same-outfit report in the same original local day | Separate legitimate report | Still at most one +2 increment; use the latest surviving accepted timestamp for aging |
| Different exact outfit or original local day | Separate report and group | Its own applicable increment before decay |
| Remove one of multiple surviving same-outfit/day reports | Only that report removed from effective history | Recompute the latest surviving absolute anchor and age; surviving group can still contribute once |
| Valid accepted timestamp correction | The existing report retains its original local calendar day; no extra report | Recompute the group's latest surviving absolute anchor and applicable decay/recency |
| Remove the final report for that outfit/day | No surviving effective report in that group | That group's Wear contribution is removed |
| Like/Dislike, Shuffle, preview or external navigation | No Wear report solely from that action | No Wear increment solely from that action |
| Invalid, failed, or canceled report mutation | Last accepted state preserved | No unaccepted mutation treated as effective |

**Rules:** BRULE-PERS-001, BRULE-PERS-005, BRULE-PERS-006, BRULE-WEAR-001–BRULE-WEAR-006.

### 14.6 Isolated Evidence Aging Boundaries

Evidence age means completed elapsed 24-hour periods calculated from absolute EvaluationInstant and EvidenceAnchorInstant under BRULE-PERS-003. For a controlled single effective contribution, the table gives its base contribution after the unchanged SRS influence percentage; it is not a complete ranking score. A normalized Wear contribution uses the latest surviving accepted event anchor.

| Evidence Age (CompletedElapsedDays) | Influence | Like +1 | Dislike −2 | One Wear Increment +2 |
|---|---|---|---|---|
| 30 days | 100% | +1 | −2 | +2 |
| 31 days | 50% | +0.5 | −1 | +1 |
| 60 days | 50% | +0.5 | −1 | +1 |
| 61 days | 25% | +0.25 | −0.5 | +0.5 |
| 90 days | 25% | +0.25 | −0.5 | +0.5 |
| 91 days | 0% | 0 | 0 | 0 |

Zero influence at 91 completed elapsed days does not clear feedback state or delete retained history.

**Rules:** BRULE-PERS-001–BRULE-PERS-003, BRULE-PERS-005, BRULE-HIST-003.

### 14.7 Privacy and External-Information Boundaries

| Condition | Required domain result | Rules |
|---|---|---|
| Personal garment/profile/feedback/event data supports functionality | Use only for necessary private product purposes; no MVP AI training/improvement or unrestricted external sharing. | BRULE-AUTH-001, BRULE-AUTH-007 |
| Garment removal accepted | Immediately exclude current ownership/advice; retain only necessary minimal history, with original image/nonessential removed data physically deleted within 30 days. | BRULE-GAR-009, BRULE-HIST-002 |
| Event removal accepted | Immediate effective exclusion, surviving-group recalculation and applicable personal-data deletion within 30 days; other objects/reports remain. | BRULE-WEAR-005, BRULE-PERS-006 |
| User-linked measurement reaches retention limit | Delete or aggregate/de-identify within 90 days; no older user-linked personal behavior remains in measurement. | BRULE-HIST-003 |
| Weather age ≤30 minutes and available within bounded wait | Usable current environmental evidence; enforce applicable environmental validity. | BRULE-PROF-003, BRULE-OUT-005 |
| Weather older than 30 minutes / usable refresh absent by 2 seconds | Attempt refresh; if no usable current result by the boundary, use disclosed reduced context and all remaining hard rules. | BRULE-PROF-003, BRULE-OUT-006 |
| Candidate information lacks explicit user evidence or identifiable configured credible source | Do not fabricate attributes or current exact utility; apply the relevant insufficient/unavailable state. | BRULE-SHOP-001, BRULE-MULT-004, BRULE-MULT-006, BRULE-MULT-007 |
| Price/availability older than 24 hours | Refresh, label stale/last checked, or omit; preserve otherwise valid outfit utility. | BRULE-SHOP-001 |
| Known external destination failure | Only navigation unavailable; preserve otherwise valid candidate/gap/count/reasons/previews where available, without purchase/ownership/zero inference. | BRULE-SHOP-002 |

## 15. Boundary and Edge Cases

Fixtures specify the relevant facts only; other applicable validity predicates are satisfied unless explicitly varied. Formula-only fixtures provide validated per-need counts without asserting a particular garment inventory or display-rounding policy.

| Case | Controlled Facts | Expected Domain Result | Rules |
|---|---|---|---|
| B01 — Minimum-only save | Confirmed supported Category/Color; all optional properties omitted | Owned entry saves; eligibility remains not evaluated, without unsuitable/ineligible claims | BRULE-GAR-002/003/006 |
| B02 — Superseded compatibility fixture | Former property-based layered check | Historical only; no implementation test or removed-field requirement | BRULE-OUT-003/004 superseded |
| B03 — Missing weather context | Usable weather is absent | Disclose missing request context without inventing garment properties or eligibility | BRULE-PROF-003, BRULE-OUT-006 |
| B04 — Superseded pattern-conflict fixture | Former removed-property conflict count | Historical only; future engine policy is deferred | BRULE-OUT-007 superseded |
| B05 — Permutation | Constituent identities A, B and C (illustrative, not an approved composition) is presented in another order | One identity-set combination for advice, feedback, Coverage, Wear grouping, and Multiplier; no extra unique outfit | BRULE-OUT-009, BRULE-MULT-002 |
| B06 — Shuffle invariants | Replace selected selected garment A with valid alternative D; separate variant has no compatible replacement | Only the selected garment changes under a future approved policy; other identities/context stay fixed. No-replacement variant retains selection; neither action creates Like/Dislike/Wear | BRULE-OUT-010 |
| B07 — Evaluated zero versus absent policy | Positive need, but engine policy/evidence unavailable | No numeric Coverage or fabricated gap; future completed evaluation may establish zero independently | BRULE-COV-003–005 |
| B08 — No active need | All six common priorities are 0 | No positive denominator; no numeric CoverageScore or fabricated purchase gap | BRULE-COV-002, BRULE-COV-005, BRULE-GAP-002 |
| B09 — Weighted formula | Formula-only inputs: Everyday weight 3 with 1 valid outfit; Work weight 1 with ≥3; all other weights 0 | Need ratios 1/3 and 1; CoverageScore = 50. Zero-priority needs contribute nothing | BRULE-COV-002–BRULE-COV-004 |
| B10 — Intentional same-day repeat | Two new accepted intentions for the same valid exact outfit on one original event-local day | Two historical reports; at most one +2 Wear increment before decay, not +4 | BRULE-WEAR-001, BRULE-WEAR-002, BRULE-PERS-005 |
| B11 — Retry | One accepted Wear action delivered/reprocessed again | Still one event; no inflated accepted-event metric or additional Wear increment | BRULE-WEAR-002, BRULE-WEAR-006 |
| B12 — Travel | Event originally reported at 23:50 in UTC+07:00; later device timezone changes to UTC+10:00, with the same absolute EvaluationInstant | Preserve the absolute timestamp and original local day/time; neither historical grouping nor elapsed evidence age/recency changes solely because of the device timezone | BRULE-WEAR-003, BRULE-HIST-002, BRULE-PERS-003–BRULE-PERS-004 |
| B13 — Correction limits / anchor | Attempt a next-original-day or future-time correction. Valid variant: same-group events at 08:00 and 20:00 in one original timezone; correct 20:00 to 10:00 within the original day and not in the future | Invalid attempt leaves accepted state unchanged. Valid variant keeps two reports; latest absolute group anchor becomes the corrected 10:00 event, with age/decay/recency recalculated | BRULE-WEAR-004, BRULE-WEAR-006, BRULE-PERS-005–BRULE-PERS-006 |
| B14 — Removal with survivor | Remove the latest of two same-outfit/day reports, then the last surviving report | First removal retains the single increment and moves its absolute anchor to the survivor; recalculate age/decay/recency. Last removal eliminates the group's contribution. Underlying Outfit/Garments and unrelated reports remain | BRULE-WEAR-005, BRULE-PERS-005–BRULE-PERS-006 |
| B15 — Removed garment in history | A garment referenced by old reports is removed from active wardrobe | History remains understandable as Removed from wardrobe; new current advice/Coverage/Multiplier exclude it and affected assessments refresh or become outdated | BRULE-GAR-009, BRULE-HIST-002, BRULE-COV-006, BRULE-MULT-008 |
| B16 — Complete Multiplier | Complete shared-basis sets contain 41 current and 58 expanded unique valid outfits, including all current ones. Variant has identical current/expanded sets | First gives +17 New Outfits with candidate-bearing previews; completed empty difference gives Evaluated Zero and +0 | BRULE-MULT-002–BRULE-MULT-005 |
| B17 — Non-exact Multiplier | Missing required future candidate-evaluation evidence; unavailable evaluation capability; or changed relevant wardrobe after a prior exact result | Respectively Incomplete, Unavailable, or prior result Outdated; none supports current exact +N or a fabricated +0; no arbitrary TTL restores exactness | BRULE-MULT-006–BRULE-MULT-009 |
| B18 — Confidence guard | Reliable evidence at 0.85, 0.60, or below 0.60; variant has 0.99 but materially conflicting evidence | Default High Confidence, Needs Review, Uncertain respectively; guarded variant cannot be High Confidence. All still require confirmation | BRULE-GAR-001, BRULE-GAR-007, BRULE-GAR-008 |
| B19 — Behavioral elapsed-time boundary | Anchor `2026-01-01T10:00:00Z`; evaluate at `2026-02-01T09:59:00Z` (30 days 23h59m) and `2026-02-01T10:00:00Z` (exactly 31 days) | First evaluation: 30 completed elapsed days, 100% influence. Second: 31 completed elapsed days, 50%. Local calendar-date difference does not replace absolute elapsed time | BRULE-PERS-003 |
| B20 — Normalized group anchor | Same user/outfit/original local date and UTC+07:00 timezone: accepted events at 08:00, 13:00, 20:00; remove 20:00, then 13:00, then 08:00 | Three initial reports, one +2 increment before aging, anchor 20:00. Removal gives two reports/anchor 13:00, then one/anchor 08:00, then no surviving group contribution; each step recalculates elapsed age and applicable decay/recency | BRULE-PERS-005–BRULE-PERS-006, BRULE-WEAR-005 |
| B21 — Weather freshness / bounded wait | Weather age exactly 30 minutes versus older; stale refresh supplies no usable current information within 2 seconds | Equality remains current; older weather is refreshed before current use. No usable result by boundary means disclosed unavailable environment, preserving remaining validity without blanket rejection | BRULE-PROF-003, BRULE-OUT-006 |
| B22 — Candidate evidence | Supported user-provided/identifiable configured-source descriptors; separate variant has unsupported attributes | Supported information can enter the applicable evaluation; unsupported attributes are not fabricated and cannot establish exact utility | BRULE-SHOP-001, BRULE-MULT-004, BRULE-MULT-006 |
| B23 — Commercial age | Supported candidate utility; price/availability exactly 24 hours old versus older | Equality is current informational guidance without merchant guarantees. Older fields are refreshed, explicitly stale/last checked, or omitted; utility remains valid unless its own evaluation basis changes | BRULE-SHOP-001, BRULE-MULT-008 |
| B24 — Removed garment retention | Accepted removal at t0; historical report still needs a minimal descriptor snapshot | Current ownership/advice excludes it immediately. Original image/nonessential personal data deleted by t0 +30 days; only the necessary understandable historical snapshot remains | BRULE-GAR-009, BRULE-HIST-002 |
| B25 — Removed event retention | Accepted individual removal at t0 with another same-outfit/day report surviving | Immediate effective exclusion and survivor-anchor recalculation; applicable personal event data deleted by t0 +30 days; surviving history, underlying Outfit/Garments and unrelated events remain | BRULE-WEAR-005, BRULE-PERS-006 |
| B26 — Measurement vs history | User-linked engagement reaches 90-day limit; necessary unremoved Wear history is older than the ranking window | Measurement is deleted or aggregated/de-identified so it is no longer user-linked; necessary history is not erased solely by the ranking/measurement window | BRULE-HIST-003 |
| B27 — Correction is not training consent | User confirms/corrects a garment or supplies feedback | Necessary private functionality/personalization is permitted; personal data does not enter MVP AI training/improvement. Future training requires separate explicit opt-in and approved product/privacy change | BRULE-AUTH-007 |

## 16. Rule Traceability

The matrix covers domain rules rather than every SRS requirement. SRS IDs identify defining obligations; Product/Business/Capability columns follow their existing source links. Privacy and external-information policies cite their defining data/interface/quality obligations directly. Examples and decision tables reuse these identifiers.

| Business Rule | SRS Source | Product Source | Business Source | Capability |
|---|---|---|---|---|
| BRULE-AUTH-001 | FR-AUTH-008, ERR-AUTH-001, DATA-GAR-001 | FEAT-AUTH-001, FEAT-GAR-001, FEAT-WAR-001 | BR-021, BR-003, BR-005 | CAP-01, CAP-04, CAP-03, CAP-07 |
| BRULE-AUTH-002 | FR-AUTH-002, FR-AUTH-016, DATA-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-003 | FR-AUTH-001, FR-AUTH-002, FR-AUTH-015, DATA-AUTH-001 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-004 | FR-AUTH-017, FR-AUTH-018, DATA-AUTH-005, ERR-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-005 | FR-AUTH-003, FR-AUTH-004, ERR-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-006 | FR-AUTH-007, FR-AUTH-010, FR-AUTH-013, FR-AUTH-014, DATA-AUTH-005 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-007 | FR-AUTH-008, NFR-PRIV-001, NFR-PRIV-002, SI-002, SI-003, COM-003, FR-MET-006, DATA-RET-001, DATA-RET-002, DATA-RET-003 | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002, FEAT-PERS-001, FEAT-PERS-002, FEAT-AI-002, FEAT-MET-001, FEAT-SHOP-002, FEAT-ANL-001, FEAT-WAR-002 | BR-021, BR-012, BR-013, BR-002, BR-018, BR-022, BR-006, BR-005, BR-019 | CAP-01, CAP-06, CAP-05, CAP-07, CAP-02, CAP-03, CAP-04, CAP-10, CAP-08, CAP-09 |
| BRULE-PROF-001 | FR-AUTH-011, FR-PROF-005, FR-PROF-006, FR-PROF-007, FR-PERS-010, DATA-AUTH-004 | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PERS-003 | BR-012, BR-021 | CAP-01, CAP-06, CAP-05 |
| BRULE-PROF-002 | FR-PROF-002, FR-PROF-003, FR-PROF-004, DATA-AUTH-003 | FEAT-PROF-001, FEAT-OUT-001 | BR-010, BR-014 | CAP-01, CAP-06, CAP-05 |
| BRULE-PROF-003 | FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-003, FR-WEATHER-004, FR-WEATHER-005, SI-002, ERR-WEATHER-001 | FEAT-PROF-002, FEAT-OUT-001 | BR-013, BR-007, BR-022 | CAP-01, CAP-05, CAP-07, CAP-06 |
| BRULE-GAR-001 | DATA-GAR-001, FR-GAR-011, AI-REQ-001, AI-REQ-007, DATA-INT-001 | FEAT-GAR-001, FEAT-WAR-001, FEAT-AI-002 | BR-003, BR-005, BR-002 | CAP-04, CAP-03, CAP-07, CAP-02 |
| BRULE-GAR-002 | FR-GAR-003, FR-AI-003, FR-AI-008, FR-AI-009, ERR-GAR-001 | FEAT-AI-002, FEAT-GAR-001, FEAT-AI-001 | BR-002, BR-003, BR-004, BR-005, BR-001 | CAP-02, CAP-03, CAP-04 |
| BRULE-GAR-003 | FR-GAR-004, FR-GAR-005, FR-GAR-006, DATA-GAR-005, ERR-GAR-002 | FEAT-GAR-001, FEAT-OUT-001, FEAT-WAR-002, FEAT-WAR-001 | BR-003, BR-009, BR-005, BR-007 | CAP-04, CAP-05, CAP-06, CAP-03, CAP-07 |
| BRULE-GAR-004 | FR-GAR-001, FR-GAR-002, FR-PROF-002, FR-PROF-004, DATA-GAR-002 | FEAT-GAR-001, FEAT-AI-002, FEAT-PROF-001, FEAT-OUT-001 | BR-003, BR-002, BR-010 | CAP-04, CAP-02, CAP-03, CAP-01, CAP-06, CAP-05 |
| BRULE-GAR-005 | FR-GAR-007, FR-GAR-008, FR-GAR-009, DATA-GAR-002 | FEAT-GAR-001 | BR-003 | CAP-04 |
| BRULE-GAR-006 | FR-GAR-005, FR-GAR-009, DATA-GAR-002, AI-REQ-008 | FEAT-GAR-001, FEAT-OUT-001, FEAT-AI-002 | BR-003, BR-009, BR-002 | CAP-04, CAP-05, CAP-06, CAP-02, CAP-03 |
| BRULE-GAR-007 | AI-REQ-001, AI-REQ-004, AI-REQ-005, AI-REQ-012, AI-REQ-013, ERR-AI-002 | FEAT-AI-002, FEAT-GAR-001 | BR-002, BR-003, BR-004 | CAP-02, CAP-03, CAP-04 |
| BRULE-GAR-008 | AI-REQ-006, AI-REQ-007, AI-REQ-009, FR-GAR-010, DATA-GAR-003 | FEAT-GAR-001, FEAT-AI-002 | BR-002, BR-003, BR-022 | CAP-04, CAP-02, CAP-03 |
| BRULE-GAR-009 | FR-WAR-003, FR-WAR-004, FR-WAR-005, FR-WAR-006, DATA-INT-002, DATA-INT-004, DATA-RET-002, DATA-HIST-001 | FEAT-WAR-002, FEAT-MULT-001, FEAT-ANL-002, FEAT-AI-002, FEAT-PERS-002, FEAT-ANL-001 | BR-002, BR-005, BR-014, BR-016, BR-011, BR-006 | CAP-03, CAP-04, CAP-09, CAP-08, CAP-07, CAP-02, CAP-06 |
| BRULE-GAR-010 | FR-GAR-012, FR-GAR-013, FR-GAR-011, DATA-GAR-002, DATA-GAR-003, DATA-INT-001, DATA-INT-004 | FEAT-GAR-001, FEAT-AI-002, FEAT-WAR-001, FEAT-WAR-002 | BR-002, BR-003, BR-005 | CAP-02, CAP-03, CAP-04 |
| BRULE-OUT-001 | FR-OUT-004, DATA-OUT-001 | FEAT-OUT-001, FEAT-MULT-001 | BR-007, BR-009, BR-008, BR-016 | CAP-05, CAP-06, CAP-09, CAP-08 |
| BRULE-OUT-002 | FR-OUT-001, FR-WAR-005, FR-MULT-001, DATA-INT-002 | FEAT-OUT-001, FEAT-WAR-002, FEAT-MULT-001, FEAT-ANL-002 | BR-007, BR-005, BR-015, BR-016, BR-014 | CAP-05, CAP-06, CAP-03, CAP-04, CAP-09, CAP-08, CAP-07 |
| BRULE-OUT-003 | FR-OUT-004, FR-GAR-005, FR-OUT-015 | FEAT-OUT-001, FEAT-GAR-001, FEAT-OUT-003 | BR-007, BR-009, BR-003 | CAP-05, CAP-06, CAP-04 |
| BRULE-OUT-004 | FR-OUT-004, FR-GAR-005, FR-GAR-006, FR-OUT-015 | FEAT-OUT-001, FEAT-GAR-001, FEAT-OUT-003 | BR-007, BR-009, BR-003 | CAP-05, CAP-06, CAP-04 |
| BRULE-OUT-005 | FR-OUT-002, FR-OUT-004, FR-GAR-009 | FEAT-OUT-001, FEAT-GAR-001 | BR-007, BR-010, BR-009, BR-003 | CAP-05, CAP-06, CAP-04 |
| BRULE-OUT-006 | ERR-WEATHER-001, FR-OUT-012, FR-WEATHER-004 | FEAT-PROF-002, FEAT-OUT-001, FEAT-OUT-002 | BR-007, BR-013, BR-022 | CAP-01, CAP-05, CAP-07, CAP-06, CAP-04 |
| BRULE-OUT-007 | FR-OUT-004, FR-GAR-009, FR-OUT-015 | FEAT-OUT-001, FEAT-GAR-001, FEAT-OUT-003 | BR-007, BR-009, BR-003 | CAP-05, CAP-06, CAP-04 |
| BRULE-OUT-008 | FR-OUT-003, FR-OUT-005, FR-PERS-006, FR-PERS-010, FR-PERS-011 | FEAT-OUT-001, FEAT-PERS-003, FEAT-PROF-001 | BR-009, BR-007, BR-010, BR-011, BR-012 | CAP-05, CAP-06, CAP-01 |
| BRULE-OUT-009 | DATA-OUT-001, FR-OUT-006, FR-OUT-007, FR-OUT-008, ERR-OUT-002 | FEAT-OUT-001, FEAT-MULT-001 | BR-008, BR-016, BR-022, BR-007, BR-009 | CAP-05, CAP-06, CAP-09, CAP-08 |
| BRULE-OUT-010 | FR-OUT-014, FR-OUT-015, FR-OUT-016, FR-OUT-017, ERR-OUT-003 | FEAT-OUT-003 | BR-008, BR-007, BR-009, BR-022 | CAP-05 |
| BRULE-PERS-001 | FR-PERS-001, FR-PERS-002, FR-PERS-003, FR-PERS-005, DATA-WEAR-001 | FEAT-PERS-001 | BR-011, BR-021 | CAP-06 |
| BRULE-PERS-002 | FR-PERS-008, FR-WEAR-011, FR-PERS-011 | FEAT-PERS-003, FEAT-PERS-002, FEAT-OUT-001 | BR-011, BR-009 | CAP-06, CAP-05, CAP-07 |
| BRULE-PERS-003 | FR-PERS-007, FR-PERS-012, DATA-WEAR-001, DATA-RET-002 | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002, FEAT-WAR-002, FEAT-ANL-001 | BR-011, BR-021, BR-005, BR-006 | CAP-06, CAP-05, CAP-07, CAP-03, CAP-04 |
| BRULE-PERS-004 | FR-PERS-009, FR-PERS-011, FR-ANL-003 | FEAT-PERS-003, FEAT-ANL-001, FEAT-OUT-001 | BR-011, BR-006, BR-009 | CAP-06, CAP-05, CAP-07 |
| BRULE-PERS-005 | FR-PERS-012, DATA-OUT-001, DATA-WEAR-003, ERR-WEAR-001 | FEAT-PERS-003, FEAT-PERS-002, FEAT-OUT-001, FEAT-MULT-001 | BR-011, BR-008, BR-016 | CAP-06, CAP-05, CAP-07, CAP-09, CAP-08 |
| BRULE-PERS-006 | FR-PERS-013, FR-WEAR-010, FR-ANL-007, DATA-WEAR-004, DATA-RET-001 | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002, FEAT-ANL-001 | BR-011, BR-012, BR-006, BR-021 | CAP-06, CAP-05, CAP-07 |
| BRULE-WEAR-001 | FR-WEAR-001, FR-WEAR-011, DATA-WEAR-003 | FEAT-PERS-002, FEAT-PERS-003 | BR-006, BR-011, BR-009 | CAP-06, CAP-07, CAP-05 |
| BRULE-WEAR-002 | FR-WEAR-002, FR-WEAR-003, FR-WEAR-009, DATA-INT-004, ERR-WEAR-001 | FEAT-PERS-002, FEAT-AI-002 | BR-011, BR-005 | CAP-06, CAP-07, CAP-02, CAP-03, CAP-04 |
| BRULE-WEAR-003 | FR-WEAR-004, FR-WEAR-005, FR-ANL-001, DATA-WEAR-002 | FEAT-PERS-002, FEAT-ANL-001 | BR-006, BR-011, BR-022 | CAP-06, CAP-07 |
| BRULE-WEAR-004 | FR-WEAR-006, FR-WEAR-012, DATA-WEAR-004, ERR-WEAR-002 | FEAT-PERS-002, FEAT-ANL-001 | BR-006, BR-021, BR-011, BR-022 | CAP-06, CAP-07 |
| BRULE-WEAR-005 | FR-WEAR-007, FR-WEAR-008, FR-WEAR-012, DATA-RET-001, ERR-WEAR-002 | FEAT-PERS-002, FEAT-ANL-001 | BR-006, BR-021, BR-011, BR-022 | CAP-06, CAP-07 |
| BRULE-WEAR-006 | FR-WEAR-010, FR-ANL-007, FR-MET-002, FR-MET-005, DATA-WEAR-004 | FEAT-PERS-002, FEAT-ANL-001, FEAT-PERS-003, FEAT-MET-001 | BR-006, BR-011, BR-019 | CAP-06, CAP-07, CAP-05, CAP-10, CAP-02, CAP-08, CAP-09 |
| BRULE-HIST-001 | FR-ANL-002, FR-ANL-003, FR-ANL-004, FR-ANL-005, FR-WAR-002 | FEAT-ANL-001, FEAT-PERS-002, FEAT-WAR-001 | BR-006, BR-011, BR-022, BR-005 | CAP-07, CAP-06, CAP-03 |
| BRULE-HIST-002 | FR-ANL-001, FR-ANL-006, FR-WAR-005, DATA-HIST-001, DATA-HIST-002, DATA-RET-002 | FEAT-ANL-001, FEAT-PERS-002, FEAT-WAR-002 | BR-006, BR-022, BR-005 | CAP-07, CAP-06, CAP-03, CAP-04 |
| BRULE-HIST-003 | DATA-RET-001, DATA-RET-002, DATA-HIST-001, FR-ANL-003, DATA-RET-003, FR-MET-006 | FEAT-PERS-002, FEAT-ANL-001, FEAT-WAR-002, FEAT-MET-001 | BR-006, BR-021, BR-005, BR-011, BR-019 | CAP-06, CAP-07, CAP-03, CAP-04, CAP-10, CAP-02, CAP-05, CAP-08, CAP-09 |
| BRULE-COV-001 | FR-ANL-008, FR-ANL-009, FR-PROF-003, FR-PROF-004, DATA-ANL-001 | FEAT-ANL-002, FEAT-PROF-001, FEAT-OUT-001 | BR-014, BR-010, BR-022 | CAP-07, CAP-08, CAP-01, CAP-06, CAP-05 |
| BRULE-COV-002 | FR-PROF-003, FR-ANL-010, FR-GAP-001, DATA-ANL-001 | FEAT-PROF-001, FEAT-ANL-002, FEAT-GAP-001 | BR-010, BR-014, BR-015, BR-022 | CAP-01, CAP-06, CAP-07, CAP-08 |
| BRULE-COV-003 | FR-ANL-008, FR-ANL-012, DATA-OUT-001, DATA-ANL-001 | FEAT-ANL-002, FEAT-PROF-001, FEAT-OUT-001, FEAT-MULT-001 | BR-014, BR-010, BR-022, BR-008, BR-016 | CAP-07, CAP-08, CAP-01, CAP-06, CAP-05, CAP-09 |
| BRULE-COV-004 | FR-ANL-010, FR-ANL-011, FR-ANL-015 | FEAT-ANL-002, FEAT-PROF-001 | BR-014, BR-010, BR-022, BR-024 | CAP-07, CAP-08, CAP-01, CAP-06 |
| BRULE-COV-005 | FR-ANL-011, FR-ANL-015, FR-ANL-013, ERR-ANL-001, ERR-WEATHER-001 | FEAT-ANL-002, FEAT-GAP-001, FEAT-MULT-001, FEAT-PROF-002, FEAT-OUT-001 | BR-014, BR-022, BR-024, BR-015, BR-016, BR-007, BR-013 | CAP-07, CAP-08, CAP-09, CAP-01, CAP-05, CAP-06 |
| BRULE-COV-006 | FR-ANL-013, FR-ANL-014, FR-WAR-006, ERR-ANL-002 | FEAT-ANL-002, FEAT-WAR-002, FEAT-OUT-001, FEAT-MULT-001 | BR-014, BR-022, BR-005, BR-007, BR-016 | CAP-07, CAP-08, CAP-03, CAP-04, CAP-05, CAP-06, CAP-09 |
| BRULE-GAP-001 | FR-GAP-001, FR-GAP-002, FR-GAP-003, DATA-ANL-002 | FEAT-GAP-001 | BR-015, BR-022 | CAP-08, CAP-07 |
| BRULE-GAP-002 | FR-GAP-004, FR-GAP-005, FR-SHOP-004, ERR-ANL-001 | FEAT-GAP-001, FEAT-SHOP-001, FEAT-ANL-002, FEAT-MULT-001 | BR-014, BR-022, BR-024, BR-020, BR-015, BR-016 | CAP-08, CAP-07, CAP-10, CAP-09 |
| BRULE-MULT-001 | FR-MULT-001, FR-MULT-002, FR-MULT-006, DATA-INT-002, DATA-INT-003, DATA-ANL-003, FR-SHOP-001 | FEAT-MULT-001, FEAT-WAR-002, FEAT-ANL-002, FEAT-SHOP-002, FEAT-SHOP-001 | BR-015, BR-016, BR-009, BR-017, BR-005, BR-014, BR-018, BR-020 | CAP-09, CAP-08, CAP-03, CAP-04, CAP-07, CAP-10 |
| BRULE-MULT-002 | FR-MULT-002, FR-MULT-003, DATA-OUT-001, FR-MULT-005 | FEAT-MULT-001, FEAT-OUT-001 | BR-009, BR-016, BR-008, BR-017 | CAP-09, CAP-08, CAP-05, CAP-06 |
| BRULE-MULT-003 | FR-MULT-002, FR-MULT-003, FR-OUT-003, FR-SHOP-007 | FEAT-MULT-001, FEAT-OUT-001, FEAT-SHOP-002 | BR-009, BR-016, BR-018, BR-019 | CAP-09, CAP-08, CAP-05, CAP-06, CAP-10 |
| BRULE-MULT-004 | FR-MULT-004, FR-MULT-005, DATA-ANL-004, FR-SHOP-004 | FEAT-MULT-001, FEAT-SHOP-001 | BR-016, BR-022, BR-017, BR-020, BR-024 | CAP-09, CAP-08, CAP-10 |
| BRULE-MULT-005 | FR-MULT-007, ERR-ANL-001, DATA-ANL-004 | FEAT-MULT-001, FEAT-ANL-002, FEAT-GAP-001 | BR-016, BR-022, BR-014, BR-015, BR-017 | CAP-09, CAP-08, CAP-07 |
| BRULE-MULT-006 | FR-MULT-007, FR-SHOP-002, ERR-ANL-001, ERR-SHOP-001 | FEAT-MULT-001, FEAT-SHOP-001, FEAT-ANL-002, FEAT-GAP-001 | BR-016, BR-022, BR-017, BR-018, BR-014, BR-015 | CAP-09, CAP-08, CAP-10, CAP-07 |
| BRULE-MULT-007 | FR-MULT-007, ERR-SHOP-001, ERR-DEP-001 | FEAT-MULT-001, FEAT-SHOP-001, FEAT-AI-001, FEAT-PROF-002, FEAT-AUTH-001, FEAT-SHOP-002 | BR-016, BR-022, BR-015, BR-017, BR-018, BR-001, BR-013, BR-021 | CAP-09, CAP-08, CAP-10, CAP-02, CAP-04, CAP-01, CAP-05, CAP-07 |
| BRULE-MULT-008 | FR-MULT-008, FR-OUT-009, DATA-ANL-004, ERR-ANL-002 | FEAT-MULT-001, FEAT-OUT-001, FEAT-ANL-002 | BR-016, BR-022, BR-007, BR-017, BR-014 | CAP-09, CAP-08, CAP-05, CAP-06, CAP-07 |
| BRULE-MULT-009 | FR-MULT-002, FR-MULT-004, FR-MULT-007, FR-MULT-008, ERR-ANL-002 | FEAT-MULT-001, FEAT-OUT-001, FEAT-ANL-002 | BR-009, BR-016, BR-022, BR-007, BR-014 | CAP-09, CAP-08, CAP-05, CAP-06, CAP-07 |
| BRULE-SHOP-001 | FR-SHOP-001, FR-SHOP-002, FR-SHOP-003, FR-SHOP-004, ERR-SHOP-002, DATA-ANL-003 | FEAT-SHOP-001, FEAT-MULT-001 | BR-018, BR-020, BR-016, BR-017, BR-024, BR-022, BR-015 | CAP-10, CAP-08, CAP-09 |
| BRULE-SHOP-002 | FR-SHOP-005, FR-SHOP-006, FR-SHOP-007, FR-MET-003, ERR-SHOP-003, DATA-INT-003, COM-003 | FEAT-SHOP-002, FEAT-MET-001, FEAT-SHOP-001, FEAT-MULT-001, FEAT-AUTH-001 | BR-018, BR-022, BR-019, BR-024, BR-015, BR-017, BR-021 | CAP-10, CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-01 |
| BRULE-SHOP-003 | FR-SHOP-003, FR-SHOP-004, FR-SHOP-008, FR-GAP-005 | FEAT-SHOP-001, FEAT-SHOP-002, FEAT-GAP-001 | BR-020, BR-024, BR-018 | CAP-10, CAP-08, CAP-09, CAP-07 |

| Rule Family | Defined IDs | Count |
|---|---|---|
| AUTH | BRULE-AUTH-001–BRULE-AUTH-007 | 7 |
| PROF | BRULE-PROF-001–BRULE-PROF-003 | 3 |
| GAR | BRULE-GAR-001–BRULE-GAR-009 | 9 |
| OUT | BRULE-OUT-001–BRULE-OUT-010 | 10 |
| PERS | BRULE-PERS-001–BRULE-PERS-006 | 6 |
| WEAR | BRULE-WEAR-001–BRULE-WEAR-006 | 6 |
| HIST | BRULE-HIST-001–BRULE-HIST-003 | 3 |
| COV | BRULE-COV-001–BRULE-COV-006 | 6 |
| GAP | BRULE-GAP-001–BRULE-GAP-002 | 2 |
| MULT | BRULE-MULT-001–BRULE-MULT-009 | 9 |
| SHOP | BRULE-SHOP-001–BRULE-SHOP-003 | 3 |

Coverage: **64 rules across 11 families**. Every rule has defining SRS sources and corresponding FEAT-*, BR-*, and CAP-* links. This is a rule coverage statement, not a claim that all 260 SRS requirements belong in this artifact.

## 17. Rule Validation Boundaries

Domain behavior is specified directly in these rules. Completed elapsed 24-hour aging, original event-local grouping, latest surviving accepted Wear-group anchors and recalculation after accepted correction/removal are covered by Sections 7–9, decision tables and boundary examples.

Purpose-limited data use, immediate effective removal, minimal history, physical-deletion/measurement retention, weather availability and credible commercial guidance reach the relevant SRS sources. Device/network, performance sampling, functional concurrency, recovery, usability and accessibility acceptance remain in SRS Sections 2.3, 6 and 12; these software-quality values are not separate domain rules.

Review checks consistency and traceability. Implementation mechanisms and formal workflow models remain separate artifacts; no unresolved domain-rule ambiguity currently blocks core Activity Diagram modeling.

## 18. Business Rules Exit Criteria

The Business Analyst / Product Owner, with Engineering and QA review, evaluates the following gates before accepting this rule baseline for downstream use:

- Rule identifiers are stable and source-linked; no upstream requirement, scope, vocabulary, formula, or threshold is silently changed.
- Saving and recommendation eligibility as not evaluated decisions are explicit; authoritative information and UNKNOWN behavior remain distinct.
- Ownership and identity remain defined; former composition/removed-property rules are superseded and future engine policy must be approved before valid-outfit claims.
- Shuffle preserves fixed context/constituents and never implies feedback or wear.
- Feedback strengths, aging bands, outfit/day normalization, recency, and optional diversity effects are consistent. Completed elapsed-day aging/recency and the latest surviving accepted Wear-group anchor are deterministic, including recalculation after accepted correction/removal.
- Logical Wear identity, deliberate repetition, original event-local time, bounded correction, effective removal, and resulting history/signals are consistent.
- Contextual need mapping, priorities, Coverage formulas, and insufficient-versus-zero decisions are deterministic for established inputs.
- Gaps are capabilities backed by evaluated need evidence, not manufactured purchase obligations.
- Multiplier counts, identity-set uniqueness, shared evaluation basis, completion gates, all five states, and input/rule-based freshness are explicit.
- Decision tables and representative boundary fixtures cover high-risk combinations; defined parameters can be turned into expected results without inventing a ranking algorithm.
- Every BRULE-* reaches SRS/PRD/BRD/capability sources; privacy/external-information boundaries and applicable software acceptance criteria agree.
- Private processing, no MVP personal-data training, minimal external exchange, removal/measurement deadlines and freshness fixtures match SRS acceptance; architecture mechanisms remain downstream.

No unresolved domain-rule ambiguity blocks core Activity Diagram modeling. Purpose-limited use, retention and external-currentness examples must agree with the SRS and Use Case outcomes.

Status remains Baseline Draft. Specification audits and examples do not constitute formal acceptance or executed software verification.

## 19. Next Artifact

The [master Use Case Diagram](use-cases/use-case-diagram.puml), 23 detailed Use Case Specifications and focused diagram projections describe current actors/goals and interaction outcomes.

After baseline review, the next workflow step is **core UML Activity Diagram modeling**, following Workflow Section 11. Complex use-case workflows reference SRS, BRULE-*, FEAT-*, BR-* and CAP-* sources and preserve these domain invariants. Delivery/backlog and Quality Attribute Analysis/ASR/ADD/ADR retain their separate workflow responsibilities.
