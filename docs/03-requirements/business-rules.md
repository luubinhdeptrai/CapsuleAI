# CapsuleAI — Business Rules

## 1. Introduction

| Field | Value |
|---|---|
| Version / Status | 0.1 / Baseline Draft |
| Last Updated | 2026-10-06 |
| Primary Owner | Business Analyst / Product Owner |
| Reviewers | Engineering / QA |
| Business Source | [BRD v0.3](../01-business/BRD.md) |
| Product Source | [PRD v0.2](../02-product/PRD.md) |
| Software Requirements Source | [SRS v0.2.1](SRS.md) |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) |
| Authoritative Location | `docs/03-requirements/business-rules.md` |

| Version | Date | Revision |
|---|---|---|
| 0.1 | 2026-10-06 | Initial extraction of reusable MVP domain rules, vocabularies, formulas, precedence, decision tables, boundary cases, and source traceability. One temporal-precision clarification is recorded in Section 17; upstream documents are unchanged. |

### 1.1 Purpose

This specification defines reusable CapsuleAI domain invariants and decisions for Product, Engineering, QA, and subsequent behavioral modeling. It consolidates the rules needed to determine garment readiness, outfit validity, effective behavioral evidence, contextual coverage, candidate utility, and assessment states.

### 1.2 Scope

Rules cover private account identity, user-controlled context, confirmed garment information, current owned outfits, Shuffle, feedback, user-reported Wear Events, history/utilization, the Personalized Everyday Capsule, capability-based gaps, and hypothetical strategic shopping. The MVP supports TOP, BOTTOM, OUTERWEAR, and FOOTWEAR.

Observable interaction sequences and quality acceptance remain in the SRS. This document does not specify implementation mechanisms, physical data structures, interfaces, model design, ranking algorithms, or downstream artifacts. SRS recognition benchmarks, image limits, password/token lifetimes, and performance criteria retain their existing authority; only reusable domain policies are extracted here.

### 1.3 Source Documents and Authority

All four current source documents listed in document control were read. Business intent follows the BRD; product behavior follows the PRD; software behavior and resolved software decisions follow the SRS. The workflow governs artifact ownership, order, and change propagation. Other current material and historical proposals provide context only when consistent with these sources; illustrative workflow rules do not override current CapsuleAI domain decisions.

Each rule cites defining SRS requirements. Section 16 maps those sources to FEAT-*, BR-*, and relevant CAP-* identifiers. OSQ-001–OSQ-010 remain resolved; OSQ-011–OSQ-014 remain open in SRS Section 12.2. No BRD/PRD/SRS contradiction was identified. A narrow missing temporal interpretation is recorded rather than assigned a new policy.

### 1.4 Rule Identification Scheme

Identifiers use `BRULE-<family>-<three digits>`. Families are AUTH, PROF, GAR, OUT, PERS, WEAR, HIST, COV, GAP, MULT, and SHOP. Shuffle uses the OUT family. Identifiers remain stable after assignment; refinements preserve source links. BRULE-* identifies domain rules and must not be confused with upstream BR-* business requirements.

ORQ-* identifies a local rule clarification, not a new requirement or a replacement for an SRS OSQ. Decision tables and fixtures reference BRULE-* without creating competing rule IDs.

### 1.5 Rule Conventions

- **Must / only when** expresses a mandatory domain condition; **may** identifies permitted optional behavior.
- **Applicable** means required by the particular evaluation; it does not make every supported descriptor universally mandatory.
- **Unknown / unavailable** is absence of usable evidence, not a numeric zero or a fabricated compatibility value. UNKNOWN is an explicit vocabulary value only where defined.
- **Current** denotes the active assessment basis. Historical snapshots and hypothetical candidates do not establish current ownership.
- Decision tables use **Yes**, **No**, and **—** (irrelevant to that row). Required predicates must be established from authoritative or justified information; an unestablished required check cannot be treated as a successful check.
- Examples are controlled fixtures, not universal wardrobe targets or product claims. Integer-day aging examples use the SRS's stated bands; fractional-day classification and normalized-group age interpretation remain ORQ-001.
- Source columns identify specification coverage, not implemented behavior or passed verification. Local rule sources and the consolidated traceability matrix must stay synchronized.

### 1.6 Rule Precedence

Apply the following order within the relevant domain operation. Steps 1–6 constrain validity; ranking starts only after all applicable hard checks succeed.

| Priority | Governing Boundary | Consequence |
|---|---|---|
| 1 | User authorization / ownership boundaries | Only the authorized user's information may be used or changed. |
| 2 | Current ownership and active state | Removed items and unowned candidates cannot enter current daily outfits. |
| 3 | Authoritative confirmed information | User-confirmed/corrected/entered-and-confirmed values take precedence over automated proposals. |
| 4 | Garment Recommendation-Readiness | Missing required compatibility information prevents the evaluation needing it. |
| 5 | Outfit hard validity | Enforce composition, eligibility, layering, applicable bulk, and severe-pattern/noise limits. |
| 6 | Available environmental/contextual hard validity | Enforce applicable environmental compatibility; unavailable weather follows the explicit fallback. |
| 7 | Soft personalization / ranking | Rank already-valid choices using available preferences and evidence. |
| 8 | Diversity / recency adjustments | Apply soft penalties or the permitted boost within the preceding boundaries. |

Soft ranking SHALL NEVER make a hard-invalid outfit valid. Positive personalization SHALL NEVER override unauthorized ownership, removed state, missing required readiness, structural invalidity, layering/bulk incompatibility, applicable hard environmental incompatibility, or severe pattern/noise invalidity.

Candidate evaluation is a separately identified hypothetical scope: the candidate may join expanded hypothetical sets, while every owned constituent remains subject to the same applicable rules. History preserves prior meaning without restoring current eligibility. See BRULE-OUT-002, BRULE-OUT-008, BRULE-HIST-002, and BRULE-MULT-001.

## 2. Account and Identity Rules

### BRULE-AUTH-001 — Authorized Personal Operations

**Rule:** Personal wardrobe, profile, feedback, history, and assessment operations require authentication and authorization for the affected user's information. Unavailable authorized access is not an empty wardrobe and does not change personal state.

**Applies To:** all personal domain operations.

**Source:** FR-AUTH-008, ERR-AUTH-001, DATA-GAR-001; OSQ-002, OSQ-003 (Resolved).

### BRULE-AUTH-002 — Normalized Email Identity

**Rule:** Email identity is compared case-insensitively after trimming surrounding whitespace. Each normalized identity maps to at most one account. Provider-specific alias transformations are not applied; duplicate registration creates no second account and offers sign-in/recovery guidance.

**Applies To:** registration, login, account recovery.

**Source:** FR-AUTH-002, FR-AUTH-016, DATA-AUTH-002; OSQ-003 (Resolved).

**Boundary:** ` Person@Example.com ` and `person@example.com` identify the same account; provider alias strings are not automatically merged.

### BRULE-AUTH-003 — Password Value Preservation

**Rule:** A password has 12–128 characters and may contain letters, numbers, symbols, spaces, and Unicode without mandatory character-class mixtures. Its value must not be silently trimmed, transformed, or normalized. A small approved denylist is permitted; an external breached-password service is not an MVP dependency.

**Applies To:** registration, login, password reset.

**Source:** FR-AUTH-001, FR-AUTH-002, FR-AUTH-015, DATA-AUTH-001; OSQ-002, OSQ-003 (Resolved).

### BRULE-AUTH-004 — Password-Reset Interaction Validity

**Rule:** A password-reset interaction is associated with its account, has a 30-minute lifetime, and permits one successful use. Expired or consumed interactions cannot change credentials. Successfully issuing a new password-reset interaction invalidates all earlier unused password-reset interactions for that account.

**Applies To:** password recovery and repeated recovery requests.

**Source:** FR-AUTH-017, FR-AUTH-018, DATA-AUTH-005, ERR-AUTH-002; OSQ-002, OSQ-003 (Resolved).

### BRULE-AUTH-005 — Completed Reset and Recovery Privacy

**Rule:** A successful reset sets a policy-valid password, consumes the reset interaction, revokes all account Refresh Sessions, and requires renewed authentication with the new password. Failed recovery must not be reported as completed. Initial recovery responses are equivalent for registered and unregistered email identities and do not disclose account existence.

**Applies To:** account recovery, returning access.

**Source:** FR-AUTH-003, FR-AUTH-004, ERR-AUTH-002; OSQ-002, OSQ-003 (Resolved).

### BRULE-AUTH-006 — Separate Sessions and Revocation

**Rule:** Separate device/login sessions are logically distinct. Logout revokes the current session without automatically revoking another separate session. Successful refresh consumes the current Refresh Token and issues replacement access/refresh tokens; reuse of a rotated token revokes the affected session. Rotation cannot extend the session maximum; token/session lifetimes remain specified in SRS Section 3.1.1.

**Applies To:** session renewal, logout, multiple devices.

**Source:** FR-AUTH-007, FR-AUTH-010, FR-AUTH-013, FR-AUTH-014, DATA-AUTH-005; OSQ-002, OSQ-003 (Resolved).

## 3. Profile and Context Rules

### BRULE-PROF-001 — Inclusive Optional Personal Context

**Rule:** Body/gender information may be omitted, edited, or removed. It may influence ranking only softly and never prohibit a supported garment category or gate account entry. Removed information stops influencing future personalization and is represented as absent rather than replaced by an inferred profile.

**Applies To:** onboarding, profile revision, recommendation and candidate ranking.

**Source:** FR-AUTH-011, FR-PROF-005, FR-PROF-006, FR-PROF-007, FR-PERS-010, DATA-AUTH-004.

### BRULE-PROF-002 — Declared Preferences and Request Context

**Rule:** Style and occasion use the controlled vocabularies in BRULE-GAR-004. Common capsule need priorities remain distinct from the current request occasion; a request must not automatically overwrite ongoing priorities. The need scale is Not Relevant=0, Low=1, Medium=2, High=3, as used by BRULE-COV-002.

**Applies To:** profile setup/revision, initial ranking, Coverage.

**Source:** FR-PROF-002, FR-PROF-003, FR-PROF-004, DATA-AUTH-003; OSQ-009 (Resolved).

### BRULE-PROF-003 — Selected Environmental Context

**Rule:** Device-location permission is optional; manual city/location selection remains available independently, including after denial. Assessments identify the location and environmental information actually used. A relevant location/environment change requires reevaluation or an outdated indication; missing context is not fabricated. Weather/external-information freshness remains OSQ-012.

**Applies To:** context selection, recommendation, Coverage and Multiplier.

**Source:** FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-004, FR-WEATHER-005, ERR-WEATHER-001; OSQ-006 (Resolved).

## 4. Garment Rules

### 4.1 Garment Ownership and Authority

### BRULE-GAR-001 — Canonical Garment Authority

**Rule:** Each owned garment belongs to the authorized user's wardrobe. Automated attribute proposals are not authoritative. Applicable User Confirmed, User Corrected, or User Entered-and-confirmed values establish the canonical profile; later automated analysis cannot silently overwrite them.

**Applies To:** ingestion, edits, all garment-dependent evaluations.

**Source:** DATA-GAR-001, FR-GAR-011, AI-REQ-001, AI-REQ-007, DATA-INT-001; OSQ-004 (Resolved).

### 4.2 Saveable Garment Rules

### BRULE-GAR-002 — Saveable Minimum

**Rule:** A garment is Saveable when the user explicitly confirms its supported Primary Category and Dominant Color. Manual creation requires neither an image nor successful analysis. Missing optional rich descriptors do not prevent saving; canceled or unconfirmed work does not establish a confirmed wardrobe entry.

**Applies To:** assisted/manual creation and validation.

**Source:** FR-GAR-003, FR-AI-003, FR-AI-008, FR-AI-009, ERR-GAR-001.

### 4.3 Recommendation-Readiness Rules

### BRULE-GAR-003 — Evaluation-Specific Recommendation-Readiness

**Rule:** Readiness requires the applicable compatibility information below, supplied authoritatively or derived from justified evidence without overwriting user values. A Saveable garment may remain unready for a decision requiring an UNKNOWN/unavailable field; retain the garment and identify the missing information.

| Garment / Evaluation | Required Information |
|---|---|
| All primary categories | Primary Category, Dominant Color, Pattern Type, Climate/Season Suitability. |
| TOP / OUTERWEAR | Applicable Layering Level in addition to common information. |
| Layered compatibility where bulk matters | Applicable Bulk Index; missing required bulk cannot pass that check. |
| Strongly recommended | Subtype, including defined OTHER/UNKNOWN fallback; not a universal blocker. |
| Not universally required | Fit, Material, Silhouette, Secondary Colors, precise HEX/HSL display, Style Tags, Occasion Tags. |

Bulk is conditional on layered compatibility, not a universal saving or readiness gate. Supporting a rich descriptor does not make it universally mandatory.

**Applies To:** Daily Outfit, Shuffle, Coverage, hypothetical Multiplier combinations.

**Source:** FR-GAR-004, FR-GAR-005, FR-GAR-006, DATA-GAR-005, ERR-GAR-002; OSQ-005 (Resolved).

### 4.4 Classification and Descriptor Rules

### BRULE-GAR-004 — Controlled Classification and Context Vocabularies

**Rule:** Primary Category is restricted to the four values below. Subtypes are bounded within their parent category; OTHER/UNKNOWN is a subtype fallback, not a fifth primary category. Style and Occasion use the same controlled values across profile and assessment operations.

| Vocabulary | Initial Values |
|---|---|
| Primary Category | `TOP`, `BOTTOM`, `OUTERWEAR`, `FOOTWEAR` |
| Style | `MINIMAL`, `CASUAL`, `SMART_CASUAL`, `CLASSIC`, `STREETWEAR`, `SPORTY`, `FORMAL` |
| Occasion | `EVERYDAY`, `WORK`, `SCHOOL_UNIVERSITY`, `DATE`, `SOCIAL_EVENT`, `FORMAL_EVENT`, `TRAVEL`, `SPORT_ACTIVITY` |
| TOP Subtype | `T_SHIRT`, `SHIRT`, `POLO`, `BLOUSE`, `SWEATER`, `HOODIE`, `TANK_TOP`, `OTHER`, `UNKNOWN` |
| BOTTOM Subtype | `JEANS`, `TROUSERS`, `CHINOS`, `SHORTS`, `SKIRT`, `LEGGINGS`, `OTHER`, `UNKNOWN` |
| OUTERWEAR Subtype | `JACKET`, `BLAZER`, `COAT`, `CARDIGAN`, `OVERSHIRT`, `OTHER`, `UNKNOWN` |
| FOOTWEAR Subtype | `SNEAKERS`, `LOAFERS`, `DRESS_SHOES`, `BOOTS`, `SANDALS`, `FLATS`, `HEELS`, `OTHER`, `UNKNOWN` |
| Capsule Need Dimensions | Everyday / Casual; Work / Professional; School / University; Formal / Special Event; Travel; Sport / Active. |
| AI Confidence | High Confidence; Needs Review; Uncertain. |
| Provenance | AI Suggested; User Confirmed; User Corrected; User Entered. |

**Applies To:** garment classification, profile choices, recommendation and analytics context.

**Source:** FR-GAR-001, FR-GAR-002, FR-PROF-002, FR-PROF-004, DATA-GAR-002; OSQ-005 (Resolved).

### BRULE-GAR-005 — Bounded Garment Descriptors

**Rule:** Descriptors use exactly the following values/scales. Unknown or unavailable information is distinct from a real scale value. Descriptor support does not change the saving and applicable readiness rules.

| Additional Descriptor | Values / Scale |
|---|---|
| Pattern Type | `SOLID`, `STRIPE`, `PLAID`, `FLORAL`, `GRAPHIC`, `POLKA_DOT`, `ABSTRACT`, `ANIMAL_PRINT`, `OTHER`, `UNKNOWN` |
| Pattern Density | `NONE`, `LOW`, `MEDIUM`, `HIGH`; SOLID → NONE. |
| Visual Noise | 1 Very Low; 2 Low; 3 Medium; 4 High; 5 Very High. |
| Layering Level | `BASE`, `MID`, `OUTER`, `SHELL`, `NOT_APPLICABLE`, `UNKNOWN` |
| Bulk Index | 1 Very Light; 2 Light; 3 Medium; 4 Bulky; 5 Very Bulky. |
| Fit | `SLIM`, `REGULAR`, `RELAXED`, `OVERSIZED`, `UNKNOWN` |
| Climate / Environmental Suitability | `HOT`, `WARM`, `MILD`, `COOL`, `COLD`, `ALL_SEASON`, `UNKNOWN`; this is the primary MVP interpretation of Climate/Season Suitability. |
| Dominant Color Family | `BLACK`, `WHITE`, `GRAY`, `BEIGE`, `BROWN`, `RED`, `ORANGE`, `YELLOW`, `GREEN`, `BLUE`, `PURPLE`, `PINK`, `MULTICOLOR`, `OTHER`, `UNKNOWN` |
| Color Temperature | `WARM`, `COOL`, `NEUTRAL`, `UNKNOWN` |
| Palette Role | `ANCHOR`, `ACCENT`, `UNKNOWN` |

**Applies To:** garment review/editing, compatibility, confidence ground truth.

**Source:** FR-GAR-007, FR-GAR-008, FR-GAR-009, DATA-GAR-002; OSQ-005 (Resolved).

### BRULE-GAR-006 — Descriptor Consistency and Unknown Information

**Rule:** Pattern Type SOLID implies Pattern Density NONE. Climate/Season Suitability uses environmental bands rather than a mandatory calendar-season taxonomy. UNKNOWN/unavailable information cannot establish compatibility that requires it. Derivation requires supporting evidence; missing descriptors, colors, material, or optional context must not be fabricated to pass a check.

**Applies To:** profile confirmation, readiness, outfit and hypothetical validity.

**Source:** FR-GAR-005, FR-GAR-009, DATA-GAR-002, AI-REQ-008; OSQ-005 (Resolved).

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

**Source:** AI-REQ-001, AI-REQ-004, AI-REQ-005, AI-REQ-012, AI-REQ-013, ERR-AI-002; OSQ-004 (Resolved).

### BRULE-GAR-008 — Provenance Is Distinct from Confidence

**Rule:** AI Suggested, User Confirmed, User Corrected, and User Entered describe how a value was established. Confidence describes proposal uncertainty and cannot substitute for confirmation. User Entered values become authoritative through confirmation. Neither confidence nor user confirmation proves physical material composition or durability; uncertain material remains qualified or unknown.

**Applies To:** attribute review, explainability, candidate information.

**Source:** AI-REQ-006, AI-REQ-007, AI-REQ-009, FR-GAR-010, DATA-GAR-003; OSQ-004, OSQ-005 (Resolved).

### 4.6 Garment State Changes

### BRULE-GAR-009 — Accepted Edits and Removal

**Rule:** Only accepted edits/removal change current garment state; canceled, failed, or pending work must not be represented as accepted. Removal excludes the garment from new current outfits, current Coverage, and the current Multiplier baseline. Relevant accepted changes require refresh or an outdated indication for affected advice; historical meaning remains governed by BRULE-HIST-002.

**Applies To:** wardrobe maintenance and all dependent assessments.

**Source:** FR-WAR-003, FR-WAR-004, FR-WAR-005, FR-WAR-006, DATA-INT-002, DATA-INT-004; OSQ-008 (Resolved).

## 5. Outfit Validity Rules

### 5.1 Structural Composition

### BRULE-OUT-001 — Supported MVP Outfit Composition

**Rule:** A valid MVP outfit contains exactly 1 TOP + 1 BOTTOM + 1 FOOTWEAR, with 0 or 1 OUTERWEAR. No additional slot, repeated item in a slot, or unsupported category forms an additional MVP composition. Composition alone does not establish full validity.

**Applies To:** Daily Outfit, Shuffle, Coverage counts, Multiplier sets.

**Source:** FR-OUT-004, DATA-OUT-001; OSQ-005, OSQ-006, OSQ-007, OSQ-010 (Resolved).

### 5.2 Garment Eligibility

### BRULE-OUT-002 — Current Daily Garment Eligibility

**Rule:** Every current daily constituent must be confirmed, active, currently owned by the authorized user, and Recommendation-Ready for applicable checks. Removed garments and hypothetical candidates are excluded. Historical snapshots do not restore eligibility. The isolated candidate exception applies only to hypothetical evaluation/previews under BRULE-MULT-001.

**Applies To:** Daily Outfit, Shuffle, current Coverage and Multiplier owned baseline.

**Source:** FR-OUT-001, FR-WAR-005, FR-MULT-001, DATA-INT-002; OSQ-005, OSQ-006, OSQ-010 (Resolved).

### 5.3 Layering Rules

### BRULE-OUT-003 — Compatible Layering Roles

**Rule:** When OUTERWEAR is present, the inner TOP has BASE or MID and the outer garment has OUTER or SHELL where applicable. Required unknown/unavailable roles cannot establish compatible layering. A role used for another evaluation does not relax this layered-outfit condition.

**Applies To:** layered Daily Outfit, Shuffle, Coverage and candidate combinations.

**Source:** FR-OUT-004, FR-GAR-005, FR-OUT-015; OSQ-005, OSQ-006 (Resolved).

### 5.4 Bulk Compatibility

### BRULE-OUT-004 — Applicable Bulk Relationship

**Rule:** Where bulk applies to both layered constituents, `outerwear.bulkIndex >= innerTop.bulkIndex` is required. Equality satisfies the relationship. Missing required bulk fails the applicable readiness/compatibility decision and must not be invented; this does not introduce a universal bulk requirement for saving or every unlayered outfit.

**Applies To:** applicable layered combinations.

**Source:** FR-OUT-004, FR-GAR-005, FR-GAR-006, FR-OUT-015; OSQ-005, OSQ-006 (Resolved).

### 5.5 Environmental Compatibility

### BRULE-OUT-005 — Environmental Bands and Suitability

**Rule:** Available temperature belongs to the following band:

| Active Band | Temperature |
|---|---|
| HOT | ≥30°C |
| WARM | ≥24°C and <30°C |
| MILD | ≥18°C and <24°C |
| COOL | ≥12°C and <18°C |
| COLD | <12°C |

When environmental hard filtering applies, confirmed suitability must support the active band. ALL_SEASON supports the environmental bands; UNKNOWN does not establish required compatibility. No automatic outerwear temperature threshold is added to the zero-or-one composition rule.

**Applies To:** environment-aware Daily Outfit, Shuffle, Coverage, Multiplier.

**Source:** FR-OUT-002, FR-OUT-004, FR-GAR-009; OSQ-005, OSQ-006 (Resolved).

### BRULE-OUT-006 — Unavailable Environmental Context

**Rule:** Missing weather alone must not reject every outfit. Skip unavailable environmental hard filtering where appropriate, enforce the remaining readiness and hard validity rules, and disclose the context limitation. Do not substitute invented weather. Whether external information is fresh/usable remains OSQ-012.

**Applies To:** recommendation and related evaluations with unavailable weather.

**Source:** ERR-WEATHER-001, FR-OUT-012, FR-WEATHER-004; OSQ-006 (Resolved).

### 5.6 Pattern / Visual Noise Compatibility

### BRULE-OUT-007 — Severe Pattern and Visual Noise Limit

**Rule:** Among TOP, BOTTOM, and OUTERWEAR, at most one garment may have Visual Noise ≥4 OR Pattern Density HIGH. A garment satisfying both counts once. FOOTWEAR is excluded from this specific count. Moderate patterns may coexist and remain soft ranking considerations; a generic one-patterned-garment limit does not replace this rule.

**Applies To:** all current and hypothetical valid outfit combinations.

**Source:** FR-OUT-004, FR-GAR-009, FR-OUT-015; OSQ-005, OSQ-006 (Resolved).

### 5.7 Hard vs Soft Factors

### BRULE-OUT-008 — Validity Before Personalized Ranking

**Rule:** Authorization, active ownership, confirmed information, applicable readiness, composition, layering, applicable bulk/environment, and severe-pattern/noise compatibility constrain validity. Color harmony, style, current occasion, Like/Dislike, Wear/history, recency/diversity, and optional body/gender are soft ranking factors. Soft scores cannot admit invalid combinations or impose body/gender category restrictions. Declared style/current occasion influence initial valid choices without a behavioral-history quota; no changed result is required when useful valid alternatives do not exist.

**Applies To:** candidate generation, ranking, Shuffle, analytics and shopping utility.

**Source:** FR-OUT-003, FR-OUT-005, FR-PERS-006, FR-PERS-010, FR-PERS-011; OSQ-006, OSQ-007 (Resolved).

### 5.8 Distinct Choices

### BRULE-OUT-009 — Canonical Outfit Identity and Available Choice

**Rule:** An outfit combination is identified by its set of constituent garment identities; permutation creates no new outfit. Use the same identity meaning for advice, feedback targets, Wear normalization, Coverage, and Multiplier. Present at least three distinct valid options when they exist; otherwise the actual one, two, or zero valid options are authoritative and must not be padded with duplicates or invalid combinations.

**Applies To:** recommendation options and every distinct-outfit count.

**Source:** DATA-OUT-001, FR-OUT-006, FR-OUT-007, FR-OUT-008, ERR-OUT-002; OSQ-006, OSQ-007, OSQ-010 (Resolved).

## 6. Shuffle Rules

### BRULE-OUT-010 — Single-Slot Shuffle Invariants

**Rule:** A successful Shuffle replaces exactly the user-selected garment slot with an eligible compatible alternative. All other constituent identities and the assessment context remain fixed, and the resulting outfit must satisfy every applicable validity rule. If no replacement exists, retain the selection and explain the limitation. Shuffle alone creates neither Like, Dislike, nor a Wear Event.

**Applies To:** selected-slot replacement.

**Source:** FR-OUT-014, FR-OUT-015, FR-OUT-016, FR-OUT-017, ERR-OUT-003; OSQ-006 (Resolved).

## 7. Feedback and Personalization Rules

### 7.1 Like / Dislike

### BRULE-PERS-001 — Effective Outfit-Targeted Feedback

**Rule:** Like has conceptual base contribution +1 and Dislike −2 on the identified recommendation/exact outfit combination. Contradictory feedback is not simultaneously effective for one user's target. State persists until changed, cleared, or superseded by user action; aging influence does not clear it. Dislike does not permanently ban constituent garments/categories. Preference actions and views do not create Wear Events.

**Applies To:** feedback recording/revision and future valid ranking.

**Source:** FR-PERS-001, FR-PERS-002, FR-PERS-003, FR-PERS-005, DATA-WEAR-001; OSQ-007 (Resolved).

### 7.2 Wear-Based Evidence

### BRULE-PERS-002 — Wear Preference Strength

**Rule:** An effective user-reported Wear contribution has base increment +2 before time decay, stronger than Like +1 under otherwise equal controlled conditions. Wear evidence is subject to the outfit/day cap in BRULE-PERS-005. It is not verified physical wear and cannot bypass hard validity.

**Applies To:** behavioral personalization from accepted Wear Events.

**Source:** FR-PERS-008, FR-WEAR-011, FR-PERS-011; OSQ-007 (Resolved).

### 7.3 Evidence Aging

### BRULE-PERS-003 — Ninety-Day Ranking Influence

**Rule:** Effective behavioral evidence uses the most recent 90-day ranking window and the following influence:

| Behavioral Evidence Age | Ranking Influence |
|---|---|
| 0–30 days | 100% |
| 31–60 days | 50% |
| 61–90 days | 25% |
| >90 days | 0% |

Apply aging to effective contributions without deleting historical events or automatically clearing Like/Dislike state. Historical retention remains OSQ-011. ORQ-001 records the missing fractional-day interpretation and normalized Wear-group age anchor; it does not change these bands or percentages.

**Applies To:** feedback and normalized Wear evidence used in ranking.

**Source:** FR-PERS-007, FR-PERS-012, DATA-WEAR-001, DATA-RET-002; OSQ-007, OSQ-008 (Resolved).

### 7.4 Recency and Diversity

### BRULE-PERS-004 — Soft Recency and Overlooked-Garment Adjustment

**Rule:** Use the latest effective reported wear of the exact outfit for the stated recency situation:

| Recency / Diversity Situation | Ranking Effect |
|---|---|
| Exact outfit reported worn within the last 2 days | Strong soft diversity penalty. |
| Exact outfit reported worn 3–7 days ago | Mild soft diversity penalty. |
| Exact outfit last reported worn >7 days ago | No recency penalty. |
| Recommendation-Ready garment with no recorded wear for ≥14 days | May receive a small soft utilization/diversity boost. |

Penalties never invalidate an outfit. The optional boost cannot override hard validity, strong explicit negative feedback, or current context relevance. No extra numerical penalty/boost magnitude is specified; the fractional-day convention is part of ORQ-001.

**Applies To:** personalized ordering and recorded-utilization diversity.

**Source:** FR-PERS-009, FR-PERS-011, FR-ANL-003; OSQ-007, OSQ-008 (Resolved).

### 7.5 Evidence Normalization

### BRULE-PERS-005 — Exact-Outfit / Event-Local-Day Wear Cap

**Rule:** For the same user, exact outfit identity, and original event-local calendar day, legitimate Wear Events supply at most one Wear-based preference increment before time decay. This is a ranking normalization boundary, not event identity: separate explicit reports remain separate historical events. Different outfits/days are not collapsed. Do not assign an unsupported timestamp-selection convention to the normalized increment; see ORQ-001.

**Applies To:** Wear-based ranking and prevention of repetition inflation.

**Source:** FR-PERS-012, DATA-OUT-001, DATA-WEAR-003, ERR-WEAR-001; OSQ-006, OSQ-007, OSQ-008, OSQ-010 (Resolved).

### BRULE-PERS-006 — Recalculation of Effective Evidence

**Rule:** Accepted feedback revision/clearing, Wear correction/removal, or relevant context revision changes effective evidence. Reapply current feedback state, surviving events, outfit/day normalization, aging, and recency as applicable. Removing one report does not remove a surviving same-outfit/day contribution; removing the last removes that group's Wear contribution. Historical event count and normalized ranking evidence remain different quantities.

**Applies To:** feedback/event revision, utilization and subsequent recommendations.

**Source:** FR-PERS-013, FR-WEAR-010, FR-ANL-007, DATA-WEAR-004, DATA-RET-001; OSQ-007, OSQ-008 (Resolved).

## 8. Wear Event Rules

### 8.1 Wear Event Identity

### BRULE-WEAR-001 — Logical Reporting Intention

**Rule:** One logical Wear action is one explicit user intention to report one wearing instance. A new report requires a current valid owned outfit, remains user-reported, and defaults to the accepted current time of the action. Report identity is distinct from the outfit/day grouping used for ranking.

**Applies To:** Wear This Today and accepted report creation.

**Source:** FR-WEAR-001, FR-WEAR-011, DATA-WEAR-003; OSQ-007, OSQ-008 (Resolved).

### 8.2 Duplicate vs Intentional Repeat

### BRULE-WEAR-002 — Retry Identity and Intentional Repetition

**Rule:** Retry, repeated delivery, or reprocessing of the same logical action yields the same event. A new explicit initiation yields a separate event, including the same outfit or a different outfit within the same local day. User + outfit + local date is not sufficient duplicate identity. Failed/pending attempts are not accepted new reports.

**Applies To:** repeated reporting, uncertain communication and event counting.

**Source:** FR-WEAR-002, FR-WEAR-003, FR-WEAR-009, DATA-INT-004, ERR-WEAR-001; OSQ-007, OSQ-008 (Resolved).

### 8.3 Time and Timezone

### BRULE-WEAR-003 — Original Event-Local Time

**Rule:** Each event retains an absolute timestamp, associated timezone or UTC offset, original local date/time, and available occasion/context. Later device-timezone changes during travel must not automatically re-date the event. Missing optional context is not fabricated; present history using the original event-local basis.

**Applies To:** Wear reports, history grouping, time interpretation.

**Source:** FR-WEAR-004, FR-WEAR-005, FR-ANL-001, DATA-WEAR-002; OSQ-008 (Resolved).

### 8.4 Correction

### BRULE-WEAR-004 — Bounded Individual Correction

**Rule:** Correction targets an existing individual report's applicable outfit, occasion/context, and local time. Corrected time must not be future and must stay within the event's original local calendar day. Accepted corrections preserve that original event-local context. Invalid, failed, or canceled corrections leave the last accepted event unchanged; no unrelated record is changed.

**Applies To:** event-specific history correction.

**Source:** FR-WEAR-006, FR-WEAR-012, DATA-WEAR-004, ERR-WEAR-002; OSQ-007, OSQ-008 (Resolved).

### 8.5 Removal

### BRULE-WEAR-005 — Individual Event Removal

**Rule:** Accepted removal excludes the specific report from effective history, utilization, recency, and signals. It does not delete the underlying Outfit, Garments, or unrelated Wear Events. Canceling or failing a requested removal does not apply it. Physical retention/deletion handling remains governed by OSQ-011, without restoring the removed event to effective use.

**Applies To:** event-specific removal and canceled mutations.

**Source:** FR-WEAR-007, FR-WEAR-008, FR-WEAR-012, DATA-RET-001, ERR-WEAR-002; OSQ-007, OSQ-008 (Resolved).

### 8.6 Effects on History and Personalization

### BRULE-WEAR-006 — Accepted Report Effects

**Rule:** Accepted creation/correction/removal updates applicable history, utilization, recency, and normalized aged evidence for the affected report/combination. Recompute surviving outfit/day contributions without changing unrelated events. Record meanings distinguish creation, correction, and removal; retries must not inflate accepted event counts.

**Applies To:** history, personalization and interaction measurement.

**Source:** FR-WEAR-010, FR-ANL-007, FR-MET-002, FR-MET-005, DATA-WEAR-004; OSQ-007, OSQ-008 (Resolved).

## 9. Wardrobe History and Utilization Rules

### BRULE-HIST-001 — Recorded Use Is Reported Evidence

**Rule:** Use frequency, recency, variety, and overlooked-item information derives from accepted effective Wear Events. Legitimate same-day reports remain separately understandable even when ranking normalizes them. No recorded use does not prove physical nonuse; incomplete logging and no-events states must not produce fabricated statistics.

**Applies To:** wardrobe use/history and relevant personalization.

**Source:** FR-ANL-002, FR-ANL-003, FR-ANL-004, FR-ANL-005, FR-WAR-002; OSQ-007, OSQ-008 (Resolved).

### BRULE-HIST-002 — Historical Snapshots vs Current Wardrobe

**Rule:** Historical outfit/garment representations remain understandable after garment removal and are labeled Removed from wardrobe where applicable. Such snapshots do not establish active ownership or enter new current outfits, Coverage, or the Multiplier baseline. Historical views preserve original event-local time rather than travel-based re-dating.

**Applies To:** history inspection and active inventory boundaries.

**Source:** FR-ANL-001, FR-ANL-006, FR-WAR-005, DATA-HIST-001, DATA-HIST-002; OSQ-007, OSQ-008 (Resolved).

### BRULE-HIST-003 — Ranking Horizon Is Not Retention

**Rule:** Events older than the 90-day behavioral window may remain meaningful history; zero ranking influence is not deletion. Garment removal does not erase understandable historical records. Retention periods, consent, access/sharing, and physical deletion handling remain OSQ-011; effective removal under BRULE-WEAR-005 still applies.

**Applies To:** history retention boundaries and behavioral aging.

**Source:** DATA-RET-001, DATA-RET-002, DATA-HIST-001, FR-ANL-003; OSQ-007, OSQ-008 (Resolved).

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

**Source:** FR-ANL-008, FR-ANL-009, FR-PROF-003, FR-PROF-004, DATA-ANL-001; OSQ-006, OSQ-009 (Resolved).

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

**Source:** FR-PROF-003, FR-ANL-010, FR-GAP-001, DATA-ANL-001; OSQ-006, OSQ-009 (Resolved).

### 10.3 Need Coverage

### BRULE-COV-003 — Valid Distinct Outfits per Need

**Rule:** Count unique valid outfit combinations for the need's mapped occasion context using current eligible garments and the applicable validity rules. The same constituent identity set counts once for that need, including repeated permutations or occurrence under multiple mapped occasions.

`NeedCoverage = min(ValidDistinctOutfitsForNeed / 3, 1)`

NeedCoverage is a ratio in [0, 1]. Per-need display is 0 valid outfits → 0%; 1 → approximately 33%; 2 → approximately 67%; ≥3 → 100%. Validity and sufficient-data conditions govern whether the count is defensible; ranking cannot add invalid combinations.

**Applies To:** per-need Coverage and underserved-need evidence.

**Source:** FR-ANL-008, FR-ANL-012, DATA-OUT-001, DATA-ANL-001; OSQ-006, OSQ-007, OSQ-009, OSQ-010 (Resolved).

### 10.4 Overall Coverage Score

### BRULE-COV-004 — Priority-Weighted Coverage Formula

**Rule:** When sufficient-data conditions hold, calculate:

`CoverageScore = 100 × Σ(PriorityWeight × NeedCoverage) / Σ(PriorityWeight)`

Both sums use positive-weight needs only. The result is a contextual 0–100 score, not universal wardrobe completeness. Changing relevant priorities may change the same wardrobe's score; no extra priority, threshold, or normalization formula is introduced.

**Applies To:** overall Wardrobe Coverage Score.

**Source:** FR-ANL-010, FR-ANL-011, FR-ANL-015; OSQ-009 (Resolved).

### 10.5 Sufficient-Data Rules

### BRULE-COV-005 — Numeric Availability vs Insufficient Information

**Rule:** A numeric Coverage score requires at least one positive-priority need and at least one Recommendation-Ready TOP, BOTTOM, and FOOTWEAR. Recommendation-Ready OUTERWEAR is additionally required only when the evaluated need/environment requires it. Required information must support a defensible assessment. Failed sufficiency produces insufficient/incomplete information, not 0%. With sufficient described inputs but no valid combinations, evaluated zero is legitimate. Missing weather follows BRULE-OUT-006 rather than invented environmental context.

**Applies To:** Coverage availability, zero interpretation, Gap Analysis prerequisites.

**Source:** FR-ANL-011, FR-ANL-015, FR-ANL-013, ERR-ANL-001, ERR-WEATHER-001; OSQ-006, OSQ-009, OSQ-010 (Resolved).

### 10.6 Outdated Coverage

### BRULE-COV-006 — Relevant Changes Invalidate Current Coverage

**Rule:** Relevant changes to current wardrobe/confirmed attributes, common needs/priorities, assessment context, or applicable validity rules require reevaluation or a clear outdated indication. Removed garments are excluded from the next current calculation. Prior scores must not silently remain current after failed refresh.

**Applies To:** Coverage refresh and dependent gap evidence.

**Source:** FR-ANL-013, FR-ANL-014, FR-WAR-006, ERR-ANL-002; OSQ-006, OSQ-009, OSQ-010 (Resolved).

## 11. Gap Analysis Rules

### BRULE-GAP-001 — Underserved Capability before Candidate Product

**Rule:** A positive-priority need with NeedCoverage <1 (below 100% displayed coverage) may prompt gap analysis. A gap identifies an evidence-supported underserved capability/bottleneck before considering candidate characteristics. A specific product is a possible response, not the definition of the gap; supported gaps may lead to hypothetical utility evaluation.

**Applies To:** Coverage-to-gap interpretation and candidate needs.

**Source:** FR-GAP-001, FR-GAP-002, FR-GAP-003, DATA-ANL-002; OSQ-009 (Resolved).

**Example:** Underserved versatile neutral work-compatible footwear is a capability gap. A particular shoe is a candidate only if its attributes and utility address that context; no universal footwear purchase is implied.

### BRULE-GAP-002 — No Fabricated Gap or Purchase Obligation

**Rule:** Insufficient Coverage cannot establish a numeric zero, a specific gap, or a compulsory purchase. No important gap/no useful candidate is a valid outcome and must not create manufactured demand. Users can continue wardrobe/context/insight work without shopping.

**Applies To:** insufficient, no-gap, and no-useful-candidate outcomes.

**Source:** FR-GAP-004, FR-GAP-005, FR-SHOP-004, ERR-ANL-001; OSQ-009, OSQ-010 (Resolved).

## 12. Wardrobe Multiplier Rules

### 12.1 Baseline and Candidate Sets

### BRULE-MULT-001 — Current Baseline and Hypothetical Addition

**Rule:** W is the current confirmed active owned wardrobe; counted combinations use only garments ready for applicable checks and exclude removed inventory. The candidate is a sufficiently described hypothetical addition, not an owned garment. Evaluation/preview changes neither ownership nor Wear history. O(W) and O(W ∪ {candidate}) apply the same context, validity/business-rule version, and uniqueness meaning.

**Applies To:** candidate utility and hypothetical previews.

**Source:** FR-MULT-001, FR-MULT-002, FR-MULT-006, DATA-INT-002, DATA-INT-003; OSQ-005, OSQ-006, OSQ-010 (Resolved).

### 12.2 Outfit Uniqueness

### BRULE-MULT-002 — Identity-Based Set Comparison

**Rule:** Both outfit sets use the garment-identity semantics in BRULE-OUT-009. Permutations of the same identity set count once, and outfits already in the current set do not count as new. The candidate remains separately identifiable from owned constituents; its presence is hypothetical, not a change to their ownership.

**Applies To:** baseline/expanded counting and newly enabled previews.

**Source:** FR-MULT-002, FR-MULT-003, DATA-OUT-001, FR-MULT-005; OSQ-006, OSQ-007, OSQ-010 (Resolved).

### 12.3 Multiplier Formula

### BRULE-MULT-003 — Incremental Unique Valid Outfit Count

**Rule:** Calculate the cardinality of the newly enabled set:

`WardrobeMultiplier(candidate) = |O(W ∪ {candidate}) − O(W)|`

O produces unique valid outfit combinations under the shared context/rule basis. Multiplier is an incremental count, not a weighted score, forecast of wear, purchase guarantee, or commercial conversion measure. Personalized ordering does not redefine hard-valid set membership.

**Applies To:** candidate utility and count interpretation.

**Source:** FR-MULT-002, FR-MULT-003, FR-OUT-003, FR-SHOP-007; OSQ-006, OSQ-010 (Resolved).

### 12.4 Exact Evaluation

### BRULE-MULT-004 — Exact Evaluation Prerequisites

**Rule:** An exact +N New Outfits requires sufficient candidate readiness, applicable readiness for owned garments used in relevant combinations, required relevant context, complete current and expanded evaluations, complete uniqueness/deduplication, and the same context/rule version/identity semantics for both sets. Displayed advice subsets do not substitute for the complete evaluation. Newly enabled previews contain and identify the hypothetical candidate and share the evaluated context. Missing optional commercial fields do not defeat these prerequisites.

**Applies To:** exact counts, reasons and hypothetical previews.

**Source:** FR-MULT-004, FR-MULT-005, DATA-ANL-004, FR-SHOP-004; OSQ-010 (Resolved).

### 12.5 Evaluated Zero

### BRULE-MULT-005 — Completed Empty New Set

**Rule:** Evaluated Zero means every exact-evaluation prerequisite succeeded and the newly enabled set is empty; +0 New Outfits is then permitted. Zero is not evidence of missing information, failure, timeout, or an unavailable capability. A lack of current valid combinations alone must not be substituted for a completed candidate comparison.

**Applies To:** honest zero-utility outcomes.

**Source:** FR-MULT-007, ERR-ANL-001, DATA-ANL-004; OSQ-009, OSQ-010 (Resolved).

### 12.6 Incomplete

### BRULE-MULT-006 — Insufficient Inputs or Unestablished Completion

**Rule:** Use Incomplete when required candidate/owned information is insufficient or reliable full evaluation cannot be established. Partial or timed-out work does not justify an exact count or evaluated zero. Explain the limitation; no exact +N or current newly enabled preview may be claimed without its required evaluation evidence.

**Applies To:** partial, insufficient and non-complete candidate assessments.

**Source:** FR-MULT-007, FR-SHOP-002, ERR-ANL-001, ERR-SHOP-001; OSQ-005, OSQ-009, OSQ-010 (Resolved).

### 12.7 Unavailable

### BRULE-MULT-007 — Capability Cannot Operate

**Rule:** Use Unavailable when the evaluation capability cannot operate. Preserve understandable candidate/gap context and permitted independent use, suppress exact +N, and do not substitute +0. Capability unavailability is distinct from available evaluation with insufficient inputs or incomplete work.

**Applies To:** dependency/capability failure during candidate evaluation.

**Source:** FR-MULT-007, ERR-SHOP-001, ERR-DEP-001; OSQ-005, OSQ-010 (Resolved).

### 12.8 Outdated

### BRULE-MULT-008 — Input and Rule-Based Freshness

**Rule:** A previous result is Outdated when relevant active wardrobe composition, confirmed garment attributes, candidate attributes, assessment context, or validity/business rules no longer match its basis. Do not present its prior count/previews as current exact evidence. Freshness is input/rule based; no arbitrary fixed TTL is specified. External weather/information freshness remains OSQ-012.

**Applies To:** assessment reuse and changed evaluation bases.

**Source:** FR-MULT-008, FR-OUT-009, DATA-ANL-004, ERR-ANL-002; OSQ-006, OSQ-009, OSQ-010 (Resolved).

### 12.9 Re-evaluation

### BRULE-MULT-009 — Reestablishing a Current Assessment

**Rule:** Reevaluation must use the current relevant baseline and candidate with one consistent context/rule version for both sets. Establish all exact prerequisites again before replacing outdated status with Exact or Evaluated Zero. If reevaluation cannot support a current exact result, explain the applicable Incomplete/Unavailable outcome without presenting the old result as refreshed. Historical assessments remain identifiable as prior results.

**Applies To:** changed inputs, retry and current-result replacement.

**Source:** FR-MULT-002, FR-MULT-004, FR-MULT-007, FR-MULT-008, ERR-ANL-002; OSQ-006, OSQ-009, OSQ-010 (Resolved).

## 13. Strategic Shopping Rules

### BRULE-SHOP-001 — Utility Information and Conditional Commercial Guidance

**Rule:** A usable recommendation identifies the applicable candidate visual/name, category/subtype, gap, key attributes, supported utility, reasons, and newly enabled hypothetical previews. Exact utility follows BRULE-MULT-004; limitations remain explicit. Price, material, durability/longevity, retailer, and link are optional and shown only when credible/available, with estimates qualified. Their absence alone does not invalidate supported wardrobe utility; credibility parameters remain OSQ-012.

**Applies To:** candidate recommendations and details.

**Source:** FR-SHOP-001, FR-SHOP-002, FR-SHOP-003, FR-SHOP-004, ERR-SHOP-002; OSQ-010 (Resolved).

### BRULE-SHOP-002 — External Navigation Does Not Establish Purchase

**Rule:** External shopping is an optional, explicitly external action to an available destination. Opening/returning or failed handoff establishes neither ownership nor verified purchase/sale, transaction success, automatic ingestion, or Wear. Known destination failure retains available candidate/gap/utility context. Attempts/openings and candidate views have distinct measurement meaning from sales.

**Applies To:** external destinations, user return and interaction interpretation.

**Source:** FR-SHOP-005, FR-SHOP-006, FR-SHOP-007, FR-MET-003, ERR-SHOP-003, DATA-INT-003.

### BRULE-SHOP-003 — Utility Independent of Commerce

**Rule:** Core wardrobe, styling, and insight value remains available without purchase, affiliate participation, or monetization. MVP has no native cart, payment, orders, fulfillment, or verified sales attribution. Do not manufacture merchant partnerships, stock, scarcity, durability certainty, or extra vanity scores to complete advice.

**Applies To:** shopping boundaries, no-useful-candidate and optional commerce.

**Source:** FR-SHOP-003, FR-SHOP-004, FR-SHOP-008, FR-GAP-005; OSQ-010 (Resolved).

## 14. Decision Tables

These tables restate the named rules for fixture construction. They do not introduce new policy. A check marked Yes is established from the relevant confirmed/justified information; a non-applicable layering, bulk, or OUTERWEAR check is satisfied without demanding that field. Missing required evidence cannot be treated as Yes.

### 14.1 Garment Saveability / Readiness

The confirmed minimum is Primary Category and Dominant Color. Common readiness information includes those fields plus usable Pattern Type and Climate/Season Suitability. Applicable checks include required TOP/OUTERWEAR layering and conditional layered bulk.

| Confirmed Minimum | Common Readiness Information Usable | Applicable Layering Satisfied | Applicable Bulk Satisfied | Saveable | Recommendation-Ready for Evaluation |
|---|---|---|---|---|---|
| No | — | — | — | No | No |
| Yes | No | — | — | Yes | No |
| Yes | Yes | No | — | Yes | No |
| Yes | Yes | Yes | No | Yes | No |
| Yes | Yes | Yes | Yes | Yes | Yes |

Manual absence of an image and missing optional rich descriptors do not change a Yes saving result. A required UNKNOWN value makes its corresponding readiness predicate No. Readiness is not full outfit validity.

**Rules:** BRULE-GAR-002, BRULE-GAR-003, BRULE-GAR-006.

### 14.2 Outfit Validity

Eligibility includes authorization, current confirmed active ownership, and applicable readiness; the candidate exception is confined to hypothetical sets. Layering/bulk must satisfy any applicable checks. Environmental No means available applicable context is incompatible; Unavailable uses the explicit fallback while preserving every other hard rule.

| Composition | Eligible Constituents | Layering | Bulk | Environmental Check | Severe-Pattern Limit | Outcome |
|---|---|---|---|---|---|---|
| No | — | — | — | — | — | Invalid |
| Yes | No | — | — | — | — | Invalid |
| Yes | Yes | No | — | — | — | Invalid |
| Yes | Yes | Yes | No | — | — | Invalid |
| Yes | Yes | Yes | Yes | No | — | Invalid |
| Yes | Yes | Yes | Yes | Yes or Unavailable | No | Invalid |
| Yes | Yes | Yes | Yes | Yes | Yes | Valid |
| Yes | Yes | Yes | Yes | Unavailable | Yes | Valid with disclosed missing context |

Positive feedback cannot alter an Invalid outcome. An unestablished required check cannot support a Valid claim; retain the applicable insufficient-information explanation.

**Rules:** BRULE-OUT-001–BRULE-OUT-008, BRULE-GAR-003.

### 14.3 Coverage Availability

The ready-role predicates use current eligible wardrobe items. The OUTERWEAR predicate is Yes when not required by the evaluation, or when the additional required ready role exists. Defensible counting still uses the mapped context and all hard rules.

| Positive Need Exists | Ready TOP | Ready BOTTOM | Ready FOOTWEAR | Applicable OUTERWEAR Requirement Satisfied | Outcome |
|---|---|---|---|---|---|
| No | — | — | — | — | Insufficient Information; no denominator-based score |
| Yes | No | — | — | — | Insufficient Information |
| Yes | Yes | No | — | — | Insufficient Information |
| Yes | Yes | Yes | No | — | Insufficient Information |
| Yes | Yes | Yes | Yes | No | Insufficient Information |
| Yes | Yes | Yes | Yes | Yes | Numeric Coverage available from defensible valid counts |

Zero valid combinations after sufficient evaluation may produce numeric 0%; absence of a ready role cannot. Missing weather alone follows the environmental fallback rather than becoming a fabricated weather value or automatic zero.

**Rules:** BRULE-COV-001–BRULE-COV-005, BRULE-OUT-006.

### 14.4 Multiplier Freshness and Evaluation State

Freshness classifies a **previous assessment**; evaluation state classifies a **current attempt**. If changed inputs make an old result Outdated while a new attempt is Unavailable, neither result is a current exact assessment.

| Previous Assessment Exists | Relevant Inputs / Rules Still Match | Previous Assessment Interpretation |
|---|---|---|
| No | — | No prior result to reuse; evaluate the current basis |
| Yes | No | Outdated; previous exact count/previews cannot be presented as current |
| Yes | Yes | Freshness alone does not change its recorded completion/state; apply its actual evidence |

For a current attempt, required inputs include candidate readiness, applicable owned readiness, relevant context, and a consistent context/rule/identity basis. Complete evaluation includes both sets and full uniqueness/deduplication.

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

These outcomes assume accepted actions unless a row states otherwise. The +2 cap is before aging; its age anchor remains ORQ-001.

| Situation | Effective History / State | Wear Preference Consequence |
|---|---|---|
| New explicit valid Wear action | One new report | Eligible for the outfit/day increment |
| Retry/reprocessing of the same logical action | No additional report | No additional increment |
| New explicit same-outfit report in the same original local day | Separate legitimate report | Still at most one +2 increment for that outfit/day |
| Different exact outfit or original local day | Separate report and group | Its own applicable increment before decay |
| Remove one of multiple surviving same-outfit/day reports | Only that report removed from effective history | Recompute; surviving group can still contribute once |
| Remove the final report for that outfit/day | No surviving effective report in that group | That group's Wear contribution is removed |
| Like/Dislike, Shuffle, preview or external navigation | No Wear report solely from that action | No Wear increment solely from that action |
| Invalid, failed, or canceled report mutation | Last accepted state preserved | No unaccepted mutation treated as effective |

**Rules:** BRULE-PERS-001, BRULE-PERS-005, BRULE-PERS-006, BRULE-WEAR-001–BRULE-WEAR-006.

### 14.6 Isolated Evidence Aging Boundaries

For a controlled single effective contribution at the stated integer-day age, the table gives base contribution after the SRS influence percentage. It is not a complete ranking score or an interpretation of fractional days.

| Evidence Age | Influence | Like +1 | Dislike −2 | One Wear Increment +2 |
|---|---|---|---|---|
| 30 days | 100% | +1 | −2 | +2 |
| 31 days | 50% | +0.5 | −1 | +1 |
| 60 days | 50% | +0.5 | −1 | +1 |
| 61 days | 25% | +0.25 | −0.5 | +0.5 |
| 90 days | 25% | +0.25 | −0.5 | +0.5 |
| 91 days | 0% | 0 | 0 | 0 |

Zero influence at 91 days does not clear feedback state or delete retained history.

**Rules:** BRULE-PERS-001–BRULE-PERS-003, BRULE-HIST-003; ORQ-001 limits sub-day interpretation.

## 15. Boundary and Edge Cases

Fixtures specify the relevant facts only; other applicable validity predicates are satisfied unless explicitly varied. Formula-only fixtures provide validated per-need counts without asserting a particular garment inventory or display-rounding policy.

| Case | Controlled Facts | Expected Domain Result | Rules |
|---|---|---|---|
| B01 — Saveable but unready | Category/color confirmed; Pattern Type UNKNOWN or required climate information unavailable | Saveable entry remains owned; decisions requiring the missing field cannot use it as ready | BRULE-GAR-002, BRULE-GAR-003, BRULE-GAR-006 |
| B02 — Layering / bulk unknown | Layered combination needs a TOP role or applicable Bulk Index that is unavailable | Do not fabricate BASE or bulk; no valid layered combination from that unsupported check, while saving remains permitted | BRULE-GAR-003, BRULE-OUT-003, BRULE-OUT-004 |
| B03 — Environmental edges / absence | Temperature exactly 30°C, 24°C, 18°C, or 12°C; separately, no usable weather but remaining hard checks pass | HOT, WARM, MILD, COOL respectively; unavailable-weather variant retains remaining valid choices with disclosed limitation | BRULE-OUT-005, BRULE-OUT-006 |
| B04 — Severe pattern count | One TOP has both noise 4 and HIGH density; BOTTOM noise 3/MEDIUM. Variant: BOTTOM also noise 4. FOOTWEAR severity varied independently | First pair contributes one severe counted item; variant contributes two and is invalid; FOOTWEAR does not alter this specific count | BRULE-OUT-007 |
| B05 — Permutation | TOP A + BOTTOM B + FOOTWEAR C is presented in another order | One identity-set combination for advice, feedback, Coverage, Wear grouping, and Multiplier; no extra unique outfit | BRULE-OUT-009, BRULE-MULT-002 |
| B06 — Shuffle invariants | Replace selected TOP A with valid TOP D; separate variant has no compatible replacement | Only TOP changes; other identities/context stay fixed. No-replacement variant retains selection; neither action creates Like/Dislike/Wear | BRULE-OUT-010 |
| B07 — Defensible zero vs missing FOOTWEAR | Positive need and ready TOP/BOTTOM/FOOTWEAR exist, but every combination violates the severe-pattern limit. Variant has no ready FOOTWEAR | Complete valid count can be zero and Coverage 0%; missing-ready-role variant is Insufficient Information, not 0% | BRULE-COV-003–BRULE-COV-005, BRULE-OUT-007 |
| B08 — No active need | All six common priorities are 0 | No positive denominator; no numeric CoverageScore or fabricated purchase gap | BRULE-COV-002, BRULE-COV-005, BRULE-GAP-002 |
| B09 — Weighted formula | Formula-only inputs: Everyday weight 3 with 1 valid outfit; Work weight 1 with ≥3; all other weights 0 | Need ratios 1/3 and 1; CoverageScore = 50. Zero-priority needs contribute nothing | BRULE-COV-002–BRULE-COV-004 |
| B10 — Intentional same-day repeat | Two new accepted intentions for the same valid exact outfit on one original event-local day | Two historical reports; at most one +2 Wear increment before decay, not +4 | BRULE-WEAR-001, BRULE-WEAR-002, BRULE-PERS-005 |
| B11 — Retry | One accepted Wear action delivered/reprocessed again | Still one event; no inflated accepted-event metric or additional Wear increment | BRULE-WEAR-002, BRULE-WEAR-006 |
| B12 — Travel | Event originally reported at 23:50 in UTC+07:00; later device timezone changes to UTC+10:00 | Preserve the absolute timestamp and original local day/time; current device date does not re-date the historical report | BRULE-WEAR-003, BRULE-HIST-002 |
| B13 — Correction limits | Attempt a next-original-day or future-time correction; separate valid same-original-day, non-future correction | Invalid attempt leaves accepted event unchanged; valid correction updates only the targeted report and relevant effective evidence | BRULE-WEAR-004, BRULE-WEAR-006 |
| B14 — Removal with survivor | Remove one of two same-outfit/day reports, then the last surviving report | First removal retains the surviving group's cap; last removal eliminates its Wear evidence. Underlying Outfit/Garments and unrelated reports remain | BRULE-WEAR-005, BRULE-PERS-006 |
| B15 — Removed garment in history | A garment referenced by old reports is removed from active wardrobe | History remains understandable as Removed from wardrobe; new current advice/Coverage/Multiplier exclude it and affected assessments refresh or become outdated | BRULE-GAR-009, BRULE-HIST-002, BRULE-COV-006, BRULE-MULT-008 |
| B16 — Complete Multiplier | Complete shared-basis sets contain 41 current and 58 expanded unique valid outfits, including all current ones. Variant has identical current/expanded sets | First gives +17 New Outfits with candidate-bearing previews; completed empty difference gives Evaluated Zero and +0 | BRULE-MULT-002–BRULE-MULT-005 |
| B17 — Non-exact Multiplier | Missing required candidate readiness; unavailable evaluation capability; or changed relevant wardrobe after a prior exact result | Respectively Incomplete, Unavailable, or prior result Outdated; none supports current exact +N or a fabricated +0; no arbitrary TTL restores exactness | BRULE-MULT-006–BRULE-MULT-009 |
| B18 — Confidence guard | Reliable evidence at 0.85, 0.60, or below 0.60; variant has 0.99 but materially conflicting evidence | Default High Confidence, Needs Review, Uncertain respectively; guarded variant cannot be High Confidence. All still require confirmation | BRULE-GAR-001, BRULE-GAR-007, BRULE-GAR-008 |
| B19 — Temporal precision gap | Evidence age lies between stated day bands, or multiple reports in one outfit/day group have different timestamps | Do not silently invent rounding or choose the group's earliest/latest time for decay. Record the unresolved expected-result interpretation under ORQ-001 | BRULE-PERS-003–BRULE-PERS-005, ORQ-001 |

## 16. Rule Traceability

The matrix covers domain rules rather than every SRS requirement. SRS IDs identify defining obligations; Product/Business/Capability columns follow their existing source links. Relevant resolved OSQs are shown with each rule's Source. Examples and decision tables reuse these identifiers.

| Business Rule | SRS Source | Product Source | Business Source | Capability |
|---|---|---|---|---|
| BRULE-AUTH-001 | FR-AUTH-008, ERR-AUTH-001, DATA-GAR-001 | FEAT-AUTH-001, FEAT-GAR-001, FEAT-WAR-001 | BR-021, BR-003, BR-005 | CAP-01, CAP-03, CAP-04, CAP-07 |
| BRULE-AUTH-002 | FR-AUTH-002, FR-AUTH-016, DATA-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-003 | FR-AUTH-001, FR-AUTH-002, FR-AUTH-015, DATA-AUTH-001 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-004 | FR-AUTH-017, FR-AUTH-018, DATA-AUTH-005, ERR-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-005 | FR-AUTH-003, FR-AUTH-004, ERR-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-AUTH-006 | FR-AUTH-007, FR-AUTH-010, FR-AUTH-013, FR-AUTH-014, DATA-AUTH-005 | FEAT-AUTH-001 | BR-021 | CAP-01 |
| BRULE-PROF-001 | FR-AUTH-011, FR-PROF-005, FR-PROF-006, FR-PROF-007, FR-PERS-010, DATA-AUTH-004 | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PERS-003 | BR-012, BR-021 | CAP-01, CAP-05, CAP-06 |
| BRULE-PROF-002 | FR-PROF-002, FR-PROF-003, FR-PROF-004, DATA-AUTH-003 | FEAT-PROF-001, FEAT-OUT-001 | BR-010, BR-014 | CAP-01, CAP-05, CAP-06 |
| BRULE-PROF-003 | FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-004, FR-WEATHER-005, ERR-WEATHER-001 | FEAT-PROF-002, FEAT-OUT-001 | BR-013, BR-007, BR-022 | CAP-01, CAP-05, CAP-06, CAP-07 |
| BRULE-GAR-001 | DATA-GAR-001, FR-GAR-011, AI-REQ-001, AI-REQ-007, DATA-INT-001 | FEAT-GAR-001, FEAT-WAR-001, FEAT-AI-002 | BR-003, BR-005, BR-002 | CAP-02, CAP-03, CAP-04, CAP-07 |
| BRULE-GAR-002 | FR-GAR-003, FR-AI-003, FR-AI-008, FR-AI-009, ERR-GAR-001 | FEAT-AI-002, FEAT-GAR-001, FEAT-AI-001 | BR-002, BR-003, BR-004, BR-005, BR-001 | CAP-02, CAP-03, CAP-04 |
| BRULE-GAR-003 | FR-GAR-004, FR-GAR-005, FR-GAR-006, DATA-GAR-005, ERR-GAR-002 | FEAT-GAR-001, FEAT-OUT-001, FEAT-WAR-002, FEAT-WAR-001 | BR-003, BR-009, BR-005, BR-007 | CAP-03, CAP-04, CAP-05, CAP-06, CAP-07 |
| BRULE-GAR-004 | FR-GAR-001, FR-GAR-002, FR-PROF-002, FR-PROF-004, DATA-GAR-002 | FEAT-GAR-001, FEAT-AI-002, FEAT-PROF-001, FEAT-OUT-001 | BR-003, BR-002, BR-010 | CAP-01, CAP-02, CAP-03, CAP-04, CAP-05, CAP-06 |
| BRULE-GAR-005 | FR-GAR-007, FR-GAR-008, FR-GAR-009, DATA-GAR-002 | FEAT-GAR-001 | BR-003 | CAP-04 |
| BRULE-GAR-006 | FR-GAR-005, FR-GAR-009, DATA-GAR-002, AI-REQ-008 | FEAT-GAR-001, FEAT-OUT-001, FEAT-AI-002 | BR-003, BR-009, BR-002 | CAP-02, CAP-03, CAP-04, CAP-05, CAP-06 |
| BRULE-GAR-007 | AI-REQ-001, AI-REQ-004, AI-REQ-005, AI-REQ-012, AI-REQ-013, ERR-AI-002 | FEAT-AI-002, FEAT-GAR-001 | BR-002, BR-003, BR-004 | CAP-02, CAP-03, CAP-04 |
| BRULE-GAR-008 | AI-REQ-006, AI-REQ-007, AI-REQ-009, FR-GAR-010, DATA-GAR-003 | FEAT-GAR-001, FEAT-AI-002 | BR-002, BR-003, BR-022 | CAP-02, CAP-03, CAP-04 |
| BRULE-GAR-009 | FR-WAR-003, FR-WAR-004, FR-WAR-005, FR-WAR-006, DATA-INT-002, DATA-INT-004 | FEAT-WAR-002, FEAT-MULT-001, FEAT-ANL-002, FEAT-AI-002, FEAT-PERS-002 | BR-002, BR-005, BR-014, BR-016, BR-011 | CAP-02, CAP-03, CAP-04, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-OUT-001 | FR-OUT-004, DATA-OUT-001 | FEAT-OUT-001, FEAT-MULT-001 | BR-007, BR-009, BR-008, BR-016 | CAP-05, CAP-06, CAP-08, CAP-09 |
| BRULE-OUT-002 | FR-OUT-001, FR-WAR-005, FR-MULT-001, DATA-INT-002 | FEAT-OUT-001, FEAT-WAR-002, FEAT-MULT-001, FEAT-ANL-002 | BR-007, BR-005, BR-015, BR-016, BR-014 | CAP-03, CAP-04, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-OUT-003 | FR-OUT-004, FR-GAR-005, FR-OUT-015 | FEAT-OUT-001, FEAT-GAR-001, FEAT-OUT-003 | BR-007, BR-009, BR-003 | CAP-04, CAP-05, CAP-06 |
| BRULE-OUT-004 | FR-OUT-004, FR-GAR-005, FR-GAR-006, FR-OUT-015 | FEAT-OUT-001, FEAT-GAR-001, FEAT-OUT-003 | BR-007, BR-009, BR-003 | CAP-04, CAP-05, CAP-06 |
| BRULE-OUT-005 | FR-OUT-002, FR-OUT-004, FR-GAR-009 | FEAT-OUT-001, FEAT-GAR-001 | BR-007, BR-010, BR-009, BR-003 | CAP-04, CAP-05, CAP-06 |
| BRULE-OUT-006 | ERR-WEATHER-001, FR-OUT-012, FR-WEATHER-004 | FEAT-PROF-002, FEAT-OUT-001, FEAT-OUT-002 | BR-007, BR-013, BR-022 | CAP-01, CAP-04, CAP-05, CAP-06, CAP-07 |
| BRULE-OUT-007 | FR-OUT-004, FR-GAR-009, FR-OUT-015 | FEAT-OUT-001, FEAT-GAR-001, FEAT-OUT-003 | BR-007, BR-009, BR-003 | CAP-04, CAP-05, CAP-06 |
| BRULE-OUT-008 | FR-OUT-003, FR-OUT-005, FR-PERS-006, FR-PERS-010, FR-PERS-011 | FEAT-OUT-001, FEAT-PERS-003, FEAT-PROF-001 | BR-009, BR-007, BR-010, BR-011, BR-012 | CAP-01, CAP-05, CAP-06 |
| BRULE-OUT-009 | DATA-OUT-001, FR-OUT-006, FR-OUT-007, FR-OUT-008, ERR-OUT-002 | FEAT-OUT-001, FEAT-MULT-001 | BR-008, BR-016, BR-022, BR-007, BR-009 | CAP-05, CAP-06, CAP-08, CAP-09 |
| BRULE-OUT-010 | FR-OUT-014, FR-OUT-015, FR-OUT-016, FR-OUT-017, ERR-OUT-003 | FEAT-OUT-003 | BR-008, BR-007, BR-009, BR-022 | CAP-05 |
| BRULE-PERS-001 | FR-PERS-001, FR-PERS-002, FR-PERS-003, FR-PERS-005, DATA-WEAR-001 | FEAT-PERS-001 | BR-011, BR-021 | CAP-06 |
| BRULE-PERS-002 | FR-PERS-008, FR-WEAR-011, FR-PERS-011 | FEAT-PERS-003, FEAT-PERS-002, FEAT-OUT-001 | BR-011, BR-009 | CAP-05, CAP-06, CAP-07 |
| BRULE-PERS-003 | FR-PERS-007, FR-PERS-012, DATA-WEAR-001, DATA-RET-002 | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002, FEAT-WAR-002, FEAT-ANL-001 | BR-011, BR-021, BR-005, BR-006 | CAP-03, CAP-04, CAP-05, CAP-06, CAP-07 |
| BRULE-PERS-004 | FR-PERS-009, FR-PERS-011, FR-ANL-003 | FEAT-PERS-003, FEAT-ANL-001, FEAT-OUT-001 | BR-011, BR-006, BR-009 | CAP-05, CAP-06, CAP-07 |
| BRULE-PERS-005 | FR-PERS-012, DATA-OUT-001, DATA-WEAR-003, ERR-WEAR-001 | FEAT-PERS-003, FEAT-PERS-002, FEAT-OUT-001, FEAT-MULT-001 | BR-011, BR-008, BR-016 | CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-PERS-006 | FR-PERS-013, FR-WEAR-010, FR-ANL-007, DATA-WEAR-004, DATA-RET-001 | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002, FEAT-ANL-001 | BR-011, BR-012, BR-006, BR-021 | CAP-05, CAP-06, CAP-07 |
| BRULE-WEAR-001 | FR-WEAR-001, FR-WEAR-011, DATA-WEAR-003 | FEAT-PERS-002, FEAT-PERS-003 | BR-006, BR-011, BR-009 | CAP-05, CAP-06, CAP-07 |
| BRULE-WEAR-002 | FR-WEAR-002, FR-WEAR-003, FR-WEAR-009, DATA-INT-004, ERR-WEAR-001 | FEAT-PERS-002, FEAT-AI-002 | BR-011, BR-005 | CAP-02, CAP-03, CAP-04, CAP-06, CAP-07 |
| BRULE-WEAR-003 | FR-WEAR-004, FR-WEAR-005, FR-ANL-001, DATA-WEAR-002 | FEAT-PERS-002, FEAT-ANL-001 | BR-006, BR-011, BR-022 | CAP-06, CAP-07 |
| BRULE-WEAR-004 | FR-WEAR-006, FR-WEAR-012, DATA-WEAR-004, ERR-WEAR-002 | FEAT-PERS-002, FEAT-ANL-001 | BR-006, BR-021, BR-011, BR-022 | CAP-06, CAP-07 |
| BRULE-WEAR-005 | FR-WEAR-007, FR-WEAR-008, FR-WEAR-012, DATA-RET-001, ERR-WEAR-002 | FEAT-PERS-002, FEAT-ANL-001 | BR-006, BR-021, BR-011, BR-022 | CAP-06, CAP-07 |
| BRULE-WEAR-006 | FR-WEAR-010, FR-ANL-007, FR-MET-002, FR-MET-005, DATA-WEAR-004 | FEAT-PERS-002, FEAT-ANL-001, FEAT-PERS-003, FEAT-MET-001 | BR-006, BR-011, BR-019 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 |
| BRULE-HIST-001 | FR-ANL-002, FR-ANL-003, FR-ANL-004, FR-ANL-005, FR-WAR-002 | FEAT-ANL-001, FEAT-PERS-002, FEAT-WAR-001 | BR-006, BR-011, BR-022, BR-005 | CAP-03, CAP-06, CAP-07 |
| BRULE-HIST-002 | FR-ANL-001, FR-ANL-006, FR-WAR-005, DATA-HIST-001, DATA-HIST-002 | FEAT-ANL-001, FEAT-PERS-002, FEAT-WAR-002 | BR-006, BR-022, BR-005 | CAP-03, CAP-04, CAP-06, CAP-07 |
| BRULE-HIST-003 | DATA-RET-001, DATA-RET-002, DATA-HIST-001, FR-ANL-003 | FEAT-PERS-002, FEAT-ANL-001, FEAT-WAR-002 | BR-006, BR-021, BR-005, BR-011 | CAP-03, CAP-04, CAP-06, CAP-07 |
| BRULE-COV-001 | FR-ANL-008, FR-ANL-009, FR-PROF-003, FR-PROF-004, DATA-ANL-001 | FEAT-ANL-002, FEAT-PROF-001, FEAT-OUT-001 | BR-014, BR-010, BR-022 | CAP-01, CAP-05, CAP-06, CAP-07, CAP-08 |
| BRULE-COV-002 | FR-PROF-003, FR-ANL-010, FR-GAP-001, DATA-ANL-001 | FEAT-PROF-001, FEAT-ANL-002, FEAT-GAP-001 | BR-010, BR-014, BR-015, BR-022 | CAP-01, CAP-06, CAP-07, CAP-08 |
| BRULE-COV-003 | FR-ANL-008, FR-ANL-012, DATA-OUT-001, DATA-ANL-001 | FEAT-ANL-002, FEAT-PROF-001, FEAT-OUT-001, FEAT-MULT-001 | BR-014, BR-010, BR-022, BR-008, BR-016 | CAP-01, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-COV-004 | FR-ANL-010, FR-ANL-011, FR-ANL-015 | FEAT-ANL-002, FEAT-PROF-001 | BR-014, BR-010, BR-022, BR-024 | CAP-01, CAP-06, CAP-07, CAP-08 |
| BRULE-COV-005 | FR-ANL-011, FR-ANL-015, FR-ANL-013, ERR-ANL-001, ERR-WEATHER-001 | FEAT-ANL-002, FEAT-GAP-001, FEAT-MULT-001, FEAT-PROF-002, FEAT-OUT-001 | BR-014, BR-022, BR-024, BR-015, BR-016, BR-007, BR-013 | CAP-01, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-COV-006 | FR-ANL-013, FR-ANL-014, FR-WAR-006, ERR-ANL-002 | FEAT-ANL-002, FEAT-WAR-002, FEAT-OUT-001, FEAT-MULT-001 | BR-014, BR-022, BR-005, BR-007, BR-016 | CAP-03, CAP-04, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-GAP-001 | FR-GAP-001, FR-GAP-002, FR-GAP-003, DATA-ANL-002 | FEAT-GAP-001 | BR-015, BR-022 | CAP-07, CAP-08 |
| BRULE-GAP-002 | FR-GAP-004, FR-GAP-005, FR-SHOP-004, ERR-ANL-001 | FEAT-GAP-001, FEAT-SHOP-001, FEAT-ANL-002, FEAT-MULT-001 | BR-014, BR-022, BR-024, BR-020, BR-015, BR-016 | CAP-07, CAP-08, CAP-09, CAP-10 |
| BRULE-MULT-001 | FR-MULT-001, FR-MULT-002, FR-MULT-006, DATA-INT-002, DATA-INT-003 | FEAT-MULT-001, FEAT-WAR-002, FEAT-ANL-002, FEAT-SHOP-002 | BR-015, BR-016, BR-009, BR-017, BR-005, BR-014, BR-018 | CAP-03, CAP-04, CAP-07, CAP-08, CAP-09, CAP-10 |
| BRULE-MULT-002 | FR-MULT-002, FR-MULT-003, DATA-OUT-001, FR-MULT-005 | FEAT-MULT-001, FEAT-OUT-001 | BR-009, BR-016, BR-008, BR-017 | CAP-05, CAP-06, CAP-08, CAP-09 |
| BRULE-MULT-003 | FR-MULT-002, FR-MULT-003, FR-OUT-003, FR-SHOP-007 | FEAT-MULT-001, FEAT-OUT-001, FEAT-SHOP-002 | BR-009, BR-016, BR-018, BR-019 | CAP-05, CAP-06, CAP-08, CAP-09, CAP-10 |
| BRULE-MULT-004 | FR-MULT-004, FR-MULT-005, DATA-ANL-004, FR-SHOP-004 | FEAT-MULT-001, FEAT-SHOP-001 | BR-016, BR-022, BR-017, BR-020, BR-024 | CAP-08, CAP-09, CAP-10 |
| BRULE-MULT-005 | FR-MULT-007, ERR-ANL-001, DATA-ANL-004 | FEAT-MULT-001, FEAT-ANL-002, FEAT-GAP-001 | BR-016, BR-022, BR-014, BR-015, BR-017 | CAP-07, CAP-08, CAP-09 |
| BRULE-MULT-006 | FR-MULT-007, FR-SHOP-002, ERR-ANL-001, ERR-SHOP-001 | FEAT-MULT-001, FEAT-SHOP-001, FEAT-ANL-002, FEAT-GAP-001 | BR-016, BR-022, BR-017, BR-018, BR-014, BR-015 | CAP-07, CAP-08, CAP-09, CAP-10 |
| BRULE-MULT-007 | FR-MULT-007, ERR-SHOP-001, ERR-DEP-001 | FEAT-MULT-001, FEAT-SHOP-001, FEAT-AI-001, FEAT-PROF-002, FEAT-AUTH-001, FEAT-SHOP-002 | BR-016, BR-022, BR-015, BR-017, BR-018, BR-001, BR-013, BR-021 | CAP-01, CAP-02, CAP-04, CAP-05, CAP-07, CAP-08, CAP-09, CAP-10 |
| BRULE-MULT-008 | FR-MULT-008, FR-OUT-009, DATA-ANL-004, ERR-ANL-002 | FEAT-MULT-001, FEAT-OUT-001, FEAT-ANL-002 | BR-016, BR-022, BR-007, BR-017, BR-014 | CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-MULT-009 | FR-MULT-002, FR-MULT-004, FR-MULT-007, FR-MULT-008, ERR-ANL-002 | FEAT-MULT-001, FEAT-OUT-001, FEAT-ANL-002 | BR-009, BR-016, BR-022, BR-007, BR-014 | CAP-05, CAP-06, CAP-07, CAP-08, CAP-09 |
| BRULE-SHOP-001 | FR-SHOP-001, FR-SHOP-002, FR-SHOP-003, FR-SHOP-004, ERR-SHOP-002 | FEAT-SHOP-001, FEAT-MULT-001 | BR-018, BR-020, BR-016, BR-017, BR-024, BR-022 | CAP-08, CAP-09, CAP-10 |
| BRULE-SHOP-002 | FR-SHOP-005, FR-SHOP-006, FR-SHOP-007, FR-MET-003, ERR-SHOP-003, DATA-INT-003 | FEAT-SHOP-002, FEAT-MET-001, FEAT-SHOP-001, FEAT-MULT-001 | BR-018, BR-022, BR-019, BR-024, BR-015, BR-017 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 |
| BRULE-SHOP-003 | FR-SHOP-003, FR-SHOP-004, FR-SHOP-008, FR-GAP-005 | FEAT-SHOP-001, FEAT-SHOP-002, FEAT-GAP-001 | BR-020, BR-024, BR-018 | CAP-07, CAP-08, CAP-09, CAP-10 |

| Rule Family | Defined IDs | Count |
|---|---|---|
| AUTH | BRULE-AUTH-001–BRULE-AUTH-006 | 6 |
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

Coverage: **63 rules across 11 families**. Every rule has defining SRS sources and corresponding FEAT-*, BR-*, and CAP-* links. This is a rule coverage statement, not a claim that all 256 SRS requirements belong in this artifact.

## 17. Open Rule Questions

One temporal-precision clarification remains. It does not reopen the resolved contributions, day bands, normalization cap, or Wear Event identity/time rules.

| ID | Domain Clarification | Existing Rules Preserved | Affected Rules / Source | Owner / Resolution Gate |
|---|---|---|---|---|
| ORQ-001 | How is evidence age classified at sub-day boundaries: completed elapsed days or calendar-day age, and on which time basis? For multiple surviving events in one exact-outfit/event-local-day group, what age anchor applies to its single Wear increment? The SRS supplies day bands and the cap but does not specify these interpretations. | +1/−2/+2; 90-day window; 0–30/31–60/61–90/>90 influence; 2/3–7/>7 recency; optional ≥14-day boost; original event-local history and one increment per group | BRULE-PERS-003–BRULE-PERS-005, BRULE-PERS-006; SRS Section 3.12.1, FR-PERS-007, FR-PERS-009, FR-PERS-012, DATA-WEAR-001, DATA-WEAR-003 | Product Owner + Business Analyst + QA; resolve before final fractional-day/normalized-group decay fixtures and acceptance |

Integer-band examples and rule invariants remain usable. Behavioral modeling can reference the known constraints and carry ORQ-001 at its acceptance gate; exact sub-day expected results must not be invented. A resolved interpretation requires normal source/change propagation rather than a silent local amendment.

The SRS software questions remain at their existing gates:

| SRS Question | Relevance to This Artifact | Deferred Detail / Gate |
|---|---|---|
| OSQ-011 | BRULE-HIST-003 and BRULE-WEAR-005 preserve effective removal and meaningful history | Consent/access/sharing, retention periods, and physical deletion handling; final privacy/data acceptance. No retention interval is introduced here. |
| OSQ-012 | BRULE-PROF-003, BRULE-OUT-006, BRULE-MULT-008, BRULE-SHOP-001–BRULE-SHOP-002 | Weather/external-information freshness, credibility, qualification, time limits, and known destination unavailability; interface/failure acceptance and relevant rule refinement. No fresh-data duration or merchant guarantee is introduced. |
| OSQ-013 | No new domain policy or evaluation-count limit | Supported devices/OS, operating/test configuration, capacity and recovery targets; operating-quality acceptance and later quality analysis. No computation cutoff is substituted for exactness. |
| OSQ-014 | No change to rule values or semantic confidence states | Usability/accessibility acceptance; its existing UX/QA gate. This artifact does not prescribe conformance or an evaluation threshold. |

OSQ-001–OSQ-010 remain resolved; OSQ-011–OSQ-014 remain open. No architecture question is converted into an ORQ.

## 18. Business Rules Exit Criteria

The Business Analyst / Product Owner, with Engineering and QA review, evaluates the following gates before accepting this rule baseline for downstream use:

- Rule identifiers are stable and source-linked; no upstream requirement, scope, vocabulary, formula, or threshold is silently changed.
- Saving and applicable Recommendation-Readiness decisions are explicit; authoritative information and UNKNOWN behavior remain distinct.
- Composition, ownership, layering, bulk, environment, severe-pattern rules, and hard-versus-soft precedence determine valid outcomes.
- Shuffle preserves fixed context/constituents and never implies feedback or wear.
- Feedback strengths, aging bands, outfit/day normalization, recency, and optional diversity effects are consistent; ORQ-001 is visible with its owner and acceptance gate. Final sub-day aging fixtures require its resolution.
- Logical Wear identity, deliberate repetition, original event-local time, bounded correction, effective removal, and resulting history/signals are consistent.
- Contextual need mapping, priorities, Coverage formulas, and insufficient-versus-zero decisions are deterministic for established inputs.
- Gaps are capabilities backed by evaluated need evidence, not manufactured purchase obligations.
- Multiplier counts, identity-set uniqueness, shared evaluation basis, completion gates, all five states, and input/rule-based freshness are explicit.
- Decision tables and representative boundary fixtures cover high-risk combinations; resolved parameters can be turned into expected results without inventing a ranking algorithm.
- Every BRULE-* reaches SRS/PRD/BRD/capability sources; open software acceptance questions retain their existing gates.
- Rule clarifications and source inconsistencies are exposed rather than hidden; architecture and implementation choices remain downstream.

Status remains Baseline Draft. Specification audits and examples do not constitute formal acceptance or executed software verification.

## 19. Next Artifact

Workflow Section 10 defines **Phase 5 — Use Case Analysis** as the next step: the formal UML Use Case Diagram at `docs/03-requirements/use-cases/use-case-diagram.puml`, followed by core `UC-*.md` Use Case Specifications. They define actors, goals, interactions, alternatives, and outcomes while referencing the applicable SRS, BRULE-*, FEAT-*, BR-*, and CAP-* sources.

Workflow Section 11 then defines core UML Activity Diagrams, preferably PlantUML, for workflow behavior. Product Goal/backlog/stories and Quality Attribute Analysis/ASR/ADD/ADR follow their workflow responsibilities. Use Case, Activity, delivery, and architecture artifacts are separate from this domain-rule specification.
