# CapsuleAI — Software Requirements Specification

## 1. Introduction

| Field | Value |
|---|---|
| Document | CapsuleAI — Software Requirements Specification |
| Version / Status | 0.2.1 / Baseline Draft |
| Last Updated | 2026-10-05 |
| Primary Owner | Requirements Engineer / Business Analyst / System Analyst |
| Decision Authority | Product Owner, with Engineering and QA review |
| Business Baseline | [BRD v0.3](../01-business/BRD.md) |
| Product Baseline | [PRD v0.2](../02-product/PRD.md) |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) |
| Authoritative Location | `docs/03-requirements/SRS.md` |
| Development Model | Scrum / Agile |

| Version | Date | Revision |
|---|---|---|
| 0.1 | 2026-10-05 | Initial SRS derived from BRD v0.3 and PRD v0.2; specifies MVP behavior, logical information, interfaces, quality directions, failures, verification, and traceability. |
| 0.2 | 2026-10-05 | Resolved OSQ-001–OSQ-010 and propagated acceptance targets, access/recovery policy, confidence/readiness vocabularies, outfit validity, personalization, Wear Event time/identity, Coverage formulas and Multiplier exactness/freshness. Preserved upstream scope and stable existing requirement IDs. |
| 0.2.1 | 2026-10-05 | Editorial normalization of SRS terminology and section headings; removed decision-log style wording, standardized normative language, and made FR-AUTH-018 mandatory. No other requirement semantics or scope changed. |

### 1.1 Purpose

This Software Requirements Specification defines CapsuleAI's required software behavior, logical information, interfaces, quality criteria, and failure handling. It supports implementation planning, verification, Business Rules derivation, and subsequent behavioral analysis by specifying observable outcomes.

The BRD and PRD provide the business and product sources of truth. OSQ-001–OSQ-010 were resolved during requirements refinement and are reflected in the corresponding requirements and criteria; Section 12.1 records those decisions. This SRS and its upstream documents remain Baseline Draft; formal acceptance and software verification are not recorded here.

### 1.2 Scope

The initial system supports a Vietnamese mobile experience on Android and iOS: private account access; progressive context setup; assisted/manual wardrobe creation; confirmed garment intelligence; wardrobe management; validity-first personalized outfits; feedback and multiple user-reported Wear Events; utilization and contextual coverage; capability-based gaps; and candidate utility with hypothetical previews and optional external shopping.

The initial categories are TOP, BOTTOM, OUTERWEAR, and FOOTWEAR. Consumer web/desktop, extra garment categories, social/guest access, native commerce, automatic wear/purchase verification, advanced learned ranking as a prerequisite, social/community, AR/3D/video ingestion, and guaranteed offline operation are excluded. Additional interface languages are future-only.

### 1.3 Document Conventions

- **Shall** denotes a mandatory system obligation; **may** denotes an explicitly permitted option. Referenced rules, criteria, and formulas define requirement interpretation and acceptance.
- **MVP** denotes required initial behavior. **Conditional** denotes an obligation when its stated condition applies, not permission to omit a core capability. Credible optional shopping information remains conditional under BR-020.
- **TBD** identifies an unsettled parameter associated with OSQ-011–OSQ-014 in Section 12.2.
- Verification codes: **T** behavioral/integration test; **A** analysis of controlled fixtures/results; **I** inspection of content, information, or constraints; **D** demonstration of an end-to-end journey. The scenario following the code states the evidence sought.
- Controlled fixtures identify context, ownership, confirmed inputs, and expected results from the requirements defined in this SRS. Business Rules will formalize reusable rule IDs and executable edge-case examples; fixture refinement preserves the specified rules.

### 1.4 Requirement Identification Scheme

| Family | Meaning |
|---|---|
| FR-AUTH / PROF / WEATHER / AI / GAR / WAR / OUT / PERS / WEAR / ANL / GAP / MULT / SHOP / MET | Functional behavior by domain; suffix `-001`, `-002`, and so on. |
| DATA-* | Logical information, integrity, and history requirements. |
| UI-* / SI-* / HW-* / COM-* | User, software, hardware, and communication interfaces. |
| NFR-PERF / SEC / PRIV / REL / AVL / USE / ACC / MNT / TEST / INT / SCA / SUP | Quality requirements. |
| LOC-* | Localization behavior. |
| AI-REQ-* | AI-specific authority, uncertainty, and correctness obligations. |
| ERR-* | Condition-specific failure and recovery requirements. |
| OSQ-* | Software questions and decision records; Section 12 records their status. These identifiers do not define a separate requirement family or reopen resolved product questions. |

IDs are stable after assignment. Requirement statements may be refined through review while preserving identity and source links. No future-only requirement is added merely to fill a family.

### 1.5 Source Documents and Authority

Source precedence is current approved BRD decisions → current approved PRD decisions → workflow for process and artifact boundaries → other current CapsuleAI sources → older or exploratory sources. The Food Delivery document is used only as a structural reference. Resolved software decisions in Section 12.1 supplement affected requirements' feature and business sources while preserving BRD/PRD intent.

BRD BG-01–BG-04, BR-001–BR-024, and CAP-01–CAP-10 remain unchanged. PRD FEAT-* and JRN-* remain upstream identifiers. OPQ-001–OPQ-010 and OBQ-001/OBQ-002 are resolved; they are not SRS blockers. No upstream contradiction requiring a business/product change was identified.

Legacy fixed weather/pattern heuristics, combined recognition claims, implementation stacks, cached-operation timings, and Sprint plans do not override the normalized baselines. Workflow examples illustrate artifact types rather than establish additional CapsuleAI domain rules.

### 1.6 Traceability Policy

Each normative row identifies its PRD feature and relevant BRD requirement. Capability and goal meaning follows the existing upstream traceability. Section 10 supplies the functional matrix and reverse coverage audits; supporting requirements retain their own source/verification columns.

The chain is BG-* → BR-* → CAP-* → FEAT-* → SRS requirement. It is a relationship among existing artifacts, not a new hierarchy of business intent. Later Business Rules, Use Cases, tests, quality analysis, and architecture must retain applicable SRS/feature/business links.

### 1.7 References

| Reference | Use |
|---|---|
| [BRD](../01-business/BRD.md), especially Sections 13–24 and 27 | Business meaning, scope, validation directions, trust, and resolved business questions. |
| [PRD](../02-product/PRD.md), especially Sections 8–26 and 33–37 | Journeys, 22 features, acceptance criteria, states, and ten resolved product decisions. |
| [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 8–11 and 39–41 | SRS ownership, next artifacts, traceability, and change propagation. |
| [SRS structural reference](../../docs%20tham%20kh%E1%BA%A3o/SRS_FoodDelivery.md) | Introduction, feature sequences, logical information, interfaces, quality, localization, and glossary discipline. No domain, technology, legal claim, or numerical target is copied. |
| Resolved software decisions, 2026-10-05 — Section 12.1 | OSQ-001–OSQ-010 record software acceptance, security, and domain-rule decisions; the applicable SRS sections specify the requirements and criteria. |
| [Legacy PRD](../../Product%20Requirements%20Document_%20CapsuleAI.md) and [Implementation Summary](../../Initial%20files/CapsuleAI_Implementation_Summary.md) | Historical context only where consistent with current baselines. |

## 2. Overall Description

### 2.1 Product Perspective

CapsuleAI connects AI Digital Closet, Context-Aware Styling, and Wardrobe Intelligence & Strategic Shopping. AI proposes information; user confirmation establishes authority. Daily advice uses current owned garments; candidates remain hypothetical. External destinations handle purchasing independently.

The primary human actor is the CapsuleAI User. Garment analysis, weather information, email delivery, and external shopping destinations are logical dependencies, not prescribed internal services. This document contains no architecture topology or formal analysis model.

### 2.2 User Classes and Characteristics

| Persona | Principal System Needs |
|---|---|
| Indecisive Professional — primary | Low setup friction, timely contextual outfit choices, one-item substitution, and simple wear reporting. |
| Fashion-Conscious Minimalist — secondary | Current wardrobe visibility, recorded utilization, relevant coverage, and improvement without forced purchases. |
| Smart Shopper — secondary | Explainable gaps, candidate attributes, defensible incremental utility, previews, and optional external navigation. |

These overlapping personas receive the same core permissions; they are not separate access roles.

### 2.3 Operating Environment

The initial environment is connected Android/iOS mobile use in Vietnam with a Vietnamese interface. Camera/photo-library/location capabilities are used only with applicable permission and alternatives. No minimum OS version, hardware quota, deployment platform, simultaneous-user count, or multi-language UI obligation is specified for the MVP.

Supported device/OS versions and reference test environments remain OSQ-013. A denied permission is distinct from unavailable network/service operation.

### 2.4 System Constraints

| Constraint | Required Boundary / Source |
|---|---|
| Platform and locale | Android/iOS; Vietnamese MVP UI and English documentation; localization-ready domain meaning (BR-023; OPQ-002). |
| Taxonomy | Four primary categories and unchanged bounded PRD subtypes/styles/occasions; additional descriptor values are specified in Section 4.10, with no new primary category. |
| Authority and completeness | Confirmed category/dominant color permit saving; manual creation needs no image. Bounded descriptors and applicable common/layering/bulk readiness are defined in Sections 3.5.1/4.10 (OPQ-004/OPQ-005; resolved OSQ-005). |
| Authentication | 15-minute JWT Access Token; maximum 30-day Refresh Session; rotation/reuse detection, session-scoped logout and account-wide reset revocation; 12–128-character untransformed passwords; normalized unique email; single-use 30-minute recovery (OPQ-008; resolved OSQ-002/OSQ-003). |
| Recommendation and learning | Slot/layer/bulk/environment/severe-pattern checks precede soft ranking. Base evidence +1/−2/+2 uses the 90-day decay window and same-outfit/day Wear normalization; recency penalties stay soft (BR-009–BR-012). |
| Wear and history | Multiple intentional events per local day; logical-action duplicate protection; absolute timestamp and original local/timezone context; no-future/same-original-day correction; individual removal and removed-garment snapshots (OPQ-006/OPQ-007; resolved OSQ-008). |
| Coverage and utility | One Personalized Everyday Capsule uses the specified priority weights, need mapping, formula, and sufficient-data gate; multiplier is the same-context/rule-version unique-valid set difference with five exactness/freshness states (BR-014–BR-017). |
| Commerce | Required candidate utility and credible optional commercial information; external navigation only; no transaction workflow (BR-018–BR-020, BR-024). |

JWT access/refresh authentication is a system constraint; remaining implementation mechanisms are deferred to architecture.

### 2.5 Assumptions

BRD ASM-001–ASM-007 remain unvalidated: users will maintain useful inventory and correct predictions; compatible garments and contextual information can support relevant advice; explicit rules/lightweight ranking can provide value; explanation aids evaluation; and credible curated candidates can support initial validation. Requirements must handle failure of these assumptions through partial-data and no-useful-result states rather than inventing evidence.

### 2.6 Dependencies

| BRD Dependency | Requirement-Level Effect |
|---|---|
| DEP-001 — images | JPEG/JPG, PNG, HEIC/HEIF inputs within 15 MB and ≥512-pixel shortest side support assistance; qualified recognition/preview criteria are defined, and imageless manual entry remains available. |
| DEP-002 — analysis | Availability/quality affects proposals, not user authority or manual saving. |
| DEP-003 — environmental context | Relevant location/weather supports assessment; missing/stale context is disclosed. |
| DEP-004 — confirmed inventory/context | Validity, coverage, and utility require adequate applicable information. |
| DEP-005 — candidate information/destinations | Candidate utility must be credible; optional links/commercial fields cannot gate core value. |
| DEP-006 — mobile/private handling | Authorized information and usable mobile access support participation. |

The following sections specify the image benchmark, percentiles, timing boundaries, and domain/security rules. Reference device/network/operating configuration (OSQ-013), provider freshness (OSQ-012), retention/privacy (OSQ-011), and usability/accessibility acceptance (OSQ-014) require further refinement.

## 3. Functional Requirements

Requirements below apply to the correct authenticated user's information unless they govern welcome/access/recovery. Common postcondition: an accepted mutation is distinguishable from pending or failed work; failures do not silently replace authoritative information. Error variants are specified in Section 9.


### 3.1 Account & Authentication

JRN-01 governs progressive access. Registration/login/recovery are available before authentication; personal operations require authorized access.

| Stimulus | Expected System Response |
|---|---|
| Register with Email, Password, Confirm Password | Validate required input and confirmation; create access only on success. Display Name is optional. |
| Login or return with usable access | Open the correct personal experience; explain renewal when required. |
| Forgot Password / complete email reset | Distinguish requested, completed, and failed recovery; enable login with the new password after success. |
| Logout | End personal access; require successful authentication before protected use resumes. |

#### 3.1.1 Authentication and Recovery Rules

The following rules define authentication, session renewal, password handling, and account recovery behavior for the MVP. FR-AUTH-* and DATA-AUTH-* specify the required outcomes; signing, key management, token/session storage, and revocation mechanisms remain downstream design.

| Rule | Requirement |
|---|---|
| JWT Access Token | 15-minute lifetime. |
| Refresh Session | Maximum 30-day login-session/Refresh Token lifetime; rotating a token does not extend the session maximum. |
| Successful refresh | Consume the current Refresh Token; issue a new Access Token and Refresh Token. A consumed token is invalid for ordinary reuse. |
| Rotated-token reuse | Revoke the affected login session and require renewed authentication for it. |
| Separate devices | Separate login sessions are permitted. Logout revokes only the current session and ends that device/session's authenticated use; another separate session is not automatically revoked. |
| Successful password reset | Consume the reset interaction, revoke all active Refresh Sessions for the account and require renewed authentication with the new password. |
| Password | 12–128 characters; letters, numbers, symbols, spaces and Unicode are allowed. No mandatory character-class mixture or silent trimming/transformation/normalization. |
| Common-password protection | A small approved denylist may be used; an external breached-password service is not an MVP dependency. |
| Email identity | Trim surrounding whitespace and compare case-insensitively; at most one account per normalized email. Do not apply provider-specific alias transformations. |
| Registration conflict | No duplicate account; provide sign-in/recovery guidance. |
| Forgot Password privacy | Equivalent initial responses do not reveal account existence; Vietnamese wording conveys the conditional meaning “If an account exists for this email, password-reset instructions have been sent.” An initiation response does not confirm reset completion. |
| Reset validity | 30-minute lifetime and single successful use; expired/consumed interactions cannot reset credentials. |
| Reset replacement | Successfully issuing a new password-reset interaction invalidates all earlier unused password-reset interactions for the account (FR-AUTH-018). |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-AUTH-001 | The system shall accept Email/Password/Confirm Password registration with matching confirmation and the password/email policy in Section 3.1.1, without requiring Display Name. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: optional name; password limits and email identity/conflict fixtures. |
| FR-AUTH-002 | The system shall authenticate Email/Password login using case-insensitive, surrounding-whitespace-trimmed email identity while comparing the user's untransformed password value. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: email case/space variants; exact Unicode/space-containing passwords. |
| FR-AUTH-003 | The system shall provide email-based Forgot Password initiation with equivalent user-facing responses for registered and unregistered email addresses. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: paired known/unknown email responses and recovery handoff. |
| FR-AUTH-004 | The system shall allow a valid single-use email recovery interaction to set a policy-valid new password, consume that interaction, revoke all account Refresh Sessions, and require subsequent authentication using the new password. | MVP | FEAT-AUTH-001; BR-021; OSQ-002, OSQ-003 (Resolved) | T: successful reset; old password/refresh sessions denied; new-password login. |
| FR-AUTH-005 | The system shall preserve the correct user's returning access while the login session remains usable, supporting access renewal within its maximum 30-day lifetime. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: ordinary return, valid renewal, expired session, separate-device context. |
| FR-AUTH-006 | The system shall renew expired Access Token access only through a usable Refresh Session or successful authentication, explaining when reauthentication is required instead of showing an empty wardrobe. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: 15-minute expiry with usable/unusable renewal; 30-day session limit. |
| FR-AUTH-007 | The system shall revoke the current Refresh Session on logout and end that session/device's personal access until authentication succeeds again, without automatically revoking another device's separate session. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: logout Session A; protected A access denied; Session B unaffected. |
| FR-AUTH-008 | The system shall restrict protected personal operations to an authenticated user authorized for the affected information. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: signed-out/cross-user denial. |
| FR-AUTH-009 | The system shall authenticate protected operations with a valid, unexpired JWT Access Token and the authorized session/account context. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T/I: absent, invalid, expired and valid JWT access; session authorization. |
| FR-AUTH-010 | The system shall use a 15-minute JWT Access Token lifetime and a maximum 30-day Refresh Token/login-session lifetime; rotation shall not extend that session beyond its maximum lifetime. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: access expiry and absolute session-lifetime boundaries across refreshes. |
| FR-AUTH-011 | The system shall allow authenticated product entry without body/gender, device-location permission, first garment, or Display Name. | MVP | FEAT-AUTH-001, FEAT-PROF-001; BR-012, BR-021 | T: omitted optional information. |
| FR-AUTH-012 | The system shall explain missing registration Email/Password/Confirm Password or a password-confirmation mismatch and allow correction without reporting successful account creation. | MVP | FEAT-AUTH-001; BR-021 | T: each required field missing and mismatch. |
| FR-AUTH-013 | The system shall consume the current Refresh Token on every successful refresh and issue a new Access Token and Refresh Token, making the consumed token invalid for ordinary reuse. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: successful rotation and attempted reuse. |
| FR-AUTH-014 | The system shall revoke the affected login session and require authentication again when a previously rotated Refresh Token is reused. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: rotated-token replay; affected session revoked. |
| FR-AUTH-015 | The system shall accept policy-valid passwords of 12–128 characters including letters, numbers, symbols, spaces, and Unicode without mandatory character-class mixtures or silent trimming, normalization, or transformation. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: 11/12/128/129-character inputs; spaces/Unicode; no required class mixture. |
| FR-AUTH-016 | The system shall map each normalized email to at most one account, trimming surrounding whitespace and comparing case-insensitively; duplicate registration shall offer sign-in/recovery guidance without creating another account or applying provider-specific alias rules. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: duplicate case/space variants and distinct provider-alias strings. |
| FR-AUTH-017 | The system shall limit each password-reset interaction to 30 minutes and one successful use, rejecting expired or already-used interactions without changing credentials. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: valid, expiry-boundary, used-interaction and repeated-reset cases. |
| FR-AUTH-018 | The system shall invalidate any earlier unused password-reset interaction for the account when a new password-reset interaction is successfully issued. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: successfully issue a new password-reset interaction; verify all earlier unused interactions for the account are rejected and the new interaction remains usable. |

### 3.2 Profile & Personalization

JRN-01 and OPQ-001/OPQ-003/OPQ-009 define context. Long-term needs and the current request occasion are distinct.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-PROF-001 | The system shall provide the progressive sequence Welcome → Register/Login → Style Preferences → Common Occasion Needs → Optional Personal Context → Location → recommended First Garment → Today/Wardrobe. | MVP | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002; BR-010, BR-012, BR-013, BR-023 | D: first-use journey. |
| FR-PROF-002 | The system shall allow users to select and revise style preferences from the controlled vocabulary in Section 4.10. | MVP | FEAT-PROF-001; BR-010 | T: every defined style choice. |
| FR-PROF-003 | The system shall allow users to revise common capsule need priorities using Not Relevant=0, Low=1, Medium=2 and High=3 and the need-to-occasion mapping in Section 3.14.1. | MVP | FEAT-PROF-001; BR-010, BR-014; OSQ-009 (Resolved) | T: every priority and need mapping; differing request/common occasion. |
| FR-PROF-004 | The system shall keep the current recommendation occasion distinct from ongoing common needs so a request occasion does not automatically overwrite those priorities. | MVP | FEAT-PROF-001, FEAT-OUT-001; BR-010 | T: differing current/common context. |
| FR-PROF-005 | The system shall allow body-profile information to be omitted, edited, or removed. | MVP | FEAT-PROF-001; BR-012 | T: optional-field lifecycle. |
| FR-PROF-006 | The system shall allow gender information to be omitted, edited, or removed without prohibiting supported garment categories. | MVP | FEAT-PROF-001; BR-012 | T: gender/category combinations. |
| FR-PROF-007 | The system shall stop using removed optional personal information as current personalization input. | MVP | FEAT-PROF-001, FEAT-PERS-003; BR-012, BR-021 | T: subsequent request after removal. |
| FR-PROF-008 | The system shall allow skipped optional setup to be completed later and make first-garment creation a recommended action rather than an access or garment-quota gate. | MVP | FEAT-AUTH-001, FEAT-PROF-001; BR-012, BR-021 | D: skip and return. |

### 3.3 Location & Environmental Context

Environmental context may be consented device location or manual city/location. Unavailable weather is distinct from a denied permission.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-WEATHER-001 | The system shall offer device-location access as an optional, purpose-explained choice. | MVP | FEAT-PROF-002; BR-013 | T: grant, deny, skip. |
| FR-WEATHER-002 | The system shall allow manual city/location selection independently of device permission, including after denial. | MVP | FEAT-PROF-002; BR-013 | T: manual city after denial. |
| FR-WEATHER-003 | The system shall request environmental information for the selected location when the dependency is available. | Conditional | FEAT-PROF-002; BR-007, BR-013 | T: selected-location response. |
| FR-WEATHER-004 | The system shall identify the location and available environmental context actually used by an assessment. | MVP | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-022 | I/T: context matches evaluation. |
| FR-WEATHER-005 | The system shall reevaluate or mark affected advice outdated when its location/environmental context changes. | MVP | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-022 | T: changed-location assessment. |

### 3.4 Garment Ingestion

JRN-02 starts from Add Garment. Before confirmation, image/proposal work is a draft, not an owned confirmed item.

| Stimulus | Expected System Response |
|---|---|
| Capture/select image | Show selected input, processing, and any usable preview/proposals. |
| Review/correct or choose manual entry | Retain user authority; permit manual saving without AI success or an image. |
| Confirm category and dominant color | Create the authoritative owned entry and show readiness guidance. |
| Cancel or processing fails | Do not create a confirmed item; allow appropriate replacement/retry/manual continuation. |

#### 3.4.1 Image Input and Preview Criteria

The following criteria define supported garment-image input and preview usability. Input format/size/dimension validation is separate from qualification for recognition benchmarking; a natural background alone does not make an input invalid.

| Criterion | Requirement |
|---|---|
| Supported input | JPEG/JPG, PNG, HEIC/HEIF. |
| Input limits | Maximum 15 MB; shortest image dimension ≥512 pixels. |
| Qualified benchmark image | One clearly identifiable main garment; most of it visible; no severe occlusion or blur; sufficient lighting for major color information; no ambiguity from competing garments. |
| Background | A plain or white background is not mandatory. |
| Usable processed preview | Garment identifiable; major garment regions not incorrectly removed; remaining background does not materially prevent review; sufficient for correction/confirmation. |

Section 8.1 specifies the locked benchmark/accuracy protocol. NFR-PERF-001 measures accepted analysis after transfer through availability of both usable preview and reviewable proposals; a preview alone does not finish that interval. Unusable processing does not remove manual-entry value.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-AI-001 | The system shall allow permitted camera garment input in JPEG/JPG, PNG, and HEIC/HEIF formats subject to Section 3.4.1 image limits. | MVP | FEAT-AI-001; BR-001; OSQ-001 (Resolved) | T: supported capture inputs and size/dimension boundaries. |
| FR-AI-002 | The system shall allow permitted photo-library garment input in JPEG/JPG, PNG, and HEIC/HEIF subject to the image limits in Section 3.4.1, without requiring a plain/white background. | MVP | FEAT-AI-001; BR-001; OSQ-001 (Resolved) | T: all formats; valid non-plain backgrounds and limit boundaries. |
| FR-AI-003 | The system shall provide manual garment entry without requiring an image or successful automated analysis. | MVP | FEAT-AI-002; BR-004 | T: imageless/manual completion. |
| FR-AI-004 | The system shall allow replacement or cancellation of the selected image before garment confirmation. | MVP | FEAT-AI-001; BR-001 | T: replace/cancel selected image. |
| FR-AI-005 | The system shall display a processing state while analysis is pending without reporting a completed wardrobe addition. | MVP | FEAT-AI-001; BR-001, BR-022 | T: delayed analysis state. |
| FR-AI-006 | The system shall present an available usable processed preview and reviewable proposals under the preview-usability criteria in Section 3.4.1, with prediction certainty distinct from preview quality. | Conditional | FEAT-AI-001; BR-001, BR-003; OSQ-001 (Resolved) | T/I: identifiable garment, retained major regions, reviewable background/proposals. |
| FR-AI-007 | The system shall allow the user to accept, correct, or manually supply proposed garment information before confirmation. | MVP | FEAT-AI-002; BR-002 | T: category/color correction. |
| FR-AI-008 | The system shall create a confirmed wardrobe entry only after explicit user confirmation of the saveable minimum. | MVP | FEAT-AI-002; BR-002, BR-005 | T: confirm versus draft. |
| FR-AI-009 | The system shall leave no confirmed entry from canceled preconfirmation work. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002 | T: canceled draft absent. |
| FR-AI-010 | The system shall acknowledge a successful addition distinctly from pending/failed work without manufacturing duplicate entries on retry. | MVP | FEAT-AI-002; BR-005 | T: successful/failed-save retry. |
| FR-AI-011 | The system shall provide capture/selection guidance about lighting, garment visibility, and avoiding confusing backgrounds. | MVP | FEAT-AI-001; BR-001, BR-022 | I/D: image-entry guidance. |
| FR-AI-012 | The system shall validate garment-image input against supported formats, maximum 15 MB file size, and shortest dimension of at least 512 pixels, explaining unsupported/out-of-limit input with replacement or manual continuation. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-004, BR-022; OSQ-001 (Resolved) | T: JPEG/JPG, PNG, HEIC/HEIF; at/beyond 15 MB; 511/512 pixels; fallback. |

### 3.5 Garment Intelligence

Garment intelligence uses bounded descriptor vocabularies, confidence criteria, and readiness checks. Optional rich fields are not universal save/readiness gates. AI-REQ-004–AI-REQ-007 and AI-REQ-012/AI-REQ-013 specify confidence/provenance behavior.

#### 3.5.1 Garment Readiness Criteria

A garment is Saveable when the user confirms its Primary Category and Dominant Color; manual saving requires no image. Recommendation readiness additionally requires the information needed for the applicable compatibility checks below.

| Garment / Use | Required Compatibility Information |
|---|---|
| All primary categories | Primary Category, Dominant Color, Pattern Type and Climate/Season Suitability. |
| TOP and OUTERWEAR | Applicable Layering Level in addition to the common fields. |
| Layered compatibility where bulk matters | Applicable Bulk Index; unavailable/UNKNOWN required information cannot pass that decision. |
| Strongly recommended | Subtype, including the defined OTHER/UNKNOWN fallback; not a universal readiness blocker. |
| Not universally required | Material, Silhouette, Secondary Colors, exact HEX/HSL display, Fit, Style Tags and Occasion Tags. |

Justified derived information can satisfy an applicable requirement without inventing a field or overwriting user authority. A required field that is UNKNOWN/unavailable prevents readiness for decisions needing it, while an entry meeting the saveable minimum remains owned/saveable. Bulk is conditional on layered compatibility, so it is not a universal save or readiness gate.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-GAR-001 | The system shall support primary classification limited to TOP, BOTTOM, OUTERWEAR, and FOOTWEAR. | MVP | FEAT-GAR-001; BR-003 | T: defined categories only. |
| FR-GAR-002 | The system shall offer the unchanged bounded category subtypes plus the descriptor vocabularies in Section 4.10, permitting OTHER/UNKNOWN where defined without blocking otherwise saveable entries. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-003, BR-002; OSQ-005 (Resolved) | T: defined vocabularies and subtype fallback saving. |
| FR-GAR-003 | The system shall accept saving when the user has confirmed Primary Category and Dominant Color without requiring the remaining rich descriptors. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003 | T: minimum-only profile. |
| FR-GAR-004 | The system shall distinguish saveable ownership from rule-specific readiness, keeping unknown required compatibility fields ineligible for the decisions requiring them while retaining saveable entries. | MVP | FEAT-GAR-001, FEAT-OUT-001; BR-003, BR-009; OSQ-005 (Resolved) | T/A: UNKNOWN versus known required fields; saveable/ready separation. |
| FR-GAR-005 | The system shall require Primary Category, Dominant Color, Pattern Type and Climate/Season Suitability for every category's readiness, applicable Layering Level for TOP/OUTERWEAR, and Bulk Index when layered compatibility requires it, using only justified derivation and no fabricated fields. | MVP | FEAT-GAR-001, FEAT-OUT-001; BR-003, BR-009; OSQ-005 (Resolved) | T/A: per-category readiness; unknown fields; layering/bulk applicability. |
| FR-GAR-006 | The system shall treat Subtype as strongly recommended and Fit/Material/Silhouette/Secondary Colors/precise HEX-HSL display/Style Tags/Occasion Tags as non-universal readiness requirements; Bulk Index is required only for applicable layered compatibility. | MVP | FEAT-GAR-001; BR-003; OSQ-005 (Resolved) | T: omitted optional fields versus required applicable bulk. |
| FR-GAR-007 | The system shall provide review/correction of bounded Layering Level, Bulk Index and Fit and optional silhouette descriptors using Section 4.10 meanings. | MVP | FEAT-GAR-001; BR-003; OSQ-005 (Resolved) | T/I: every layer/fit value and 1–5 bulk bounds. |
| FR-GAR-008 | The system shall provide review/correction of dominant color family, secondary color, Color Temperature and Palette Role using the bounded vocabulary, with optional conceptual HEX/HSL support. | MVP | FEAT-GAR-001; BR-003; OSQ-005 (Resolved) | T/I: all color-family/temperature/role values and unknown handling. |
| FR-GAR-009 | The system shall provide review/correction of bounded Pattern Type/Density/Visual Noise and climate/occasion/style associations, applying SOLID → NONE and the Vietnam-first environmental taxonomy. | MVP | FEAT-GAR-001; BR-003; OSQ-005 (Resolved) | T: SOLID/NONE; noise 1–5; climate values; preserved style/occasion codes. |
| FR-GAR-010 | The system shall permit uncertain material information to be corrected or left unknown without claiming verified composition or durability. | MVP | FEAT-GAR-001; BR-003 | T: uncertain material case. |
| FR-GAR-011 | The system shall make user-confirmed/corrected/entered values authoritative and prevent later automated output from silently replacing them. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002 | T: analysis after correction. |

### 3.6 Wardrobe Management

JRN-03 operates on the current owned wardrobe; historical information remains distinct.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-WAR-001 | The system shall present the current confirmed wardrobe and identifiable garment details, including manually saved entries without images. | MVP | FEAT-WAR-001; BR-005 | T: browse and imageless detail. |
| FR-WAR-002 | The system shall show applicable garment readiness and recorded-use information without equating no recorded use with proven nonuse. | MVP | FEAT-WAR-001, FEAT-ANL-001; BR-005, BR-006 | I: readiness and usage labels. |
| FR-WAR-003 | The system shall allow explicit confirmation/cancellation of supported garment-profile edits. | MVP | FEAT-WAR-002; BR-002, BR-005 | T: edit/confirm/cancel. |
| FR-WAR-004 | The system shall allow deliberate confirmation/cancellation of garment removal from active ownership. | MVP | FEAT-WAR-002; BR-005 | T: remove/confirm/cancel. |
| FR-WAR-005 | The system shall exclude removed garments from new current outfits, current coverage, and the current multiplier baseline. | MVP | FEAT-WAR-002; BR-005 | T/A: post-removal evaluations. |
| FR-WAR-006 | The system shall refresh or mark affected prior advice for reevaluation after relevant edits/removal. | MVP | FEAT-WAR-002; BR-005 | T: affected assessment state. |
| FR-WAR-007 | The system shall provide descriptor search and category/color filtering of confirmed garments with visible conditions and reset. | MVP | FEAT-WAR-003; BR-005 | T: match, filter, clear. |
| FR-WAR-008 | The system shall distinguish an empty wardrobe, no matching results, loading, and unavailable data with applicable Add Garment or reset/retry actions. | MVP | FEAT-WAR-001, FEAT-WAR-003; BR-005 | T: empty/no-match/failure states. |

### 3.7 Outfit Recommendation

JRN-04 requires sufficient applicable confirmed wardrobe/context information, not a fixed inventory quota.

| Stimulus | Expected System Response |
|---|---|
| Request outfits for active context | Evaluate owned garments, filter invalid candidates, then rank valid candidates. |
| At least three distinct valid combinations exist | Present at least three distinct options. |
| Only one/two or no valid combinations exist | Present the actual valid set or no-valid state with useful limitations; do not pad. |
| Context/wardrobe changes | Refresh evaluation or explicitly mark affected advice outdated. |

#### 3.7.1 Outfit Validity Rules

An outfit is valid for MVP recommendation only when the applicable rules below are satisfied. Composition is necessary but not sufficient: eligible garments must also satisfy applicable readiness, layering, bulk, environmental, and severe-pattern rules.

| Dimension | Required Validity |
|---|---|
| Composition | Exactly 1 TOP + 1 BOTTOM + 1 FOOTWEAR, with 0 or 1 OUTERWEAR; no unsupported category or additional slot item. |
| Current daily eligibility | Confirmed, active, owned and Recommendation-Ready for applicable rules; hypothetical candidates are excluded. |
| Layered roles | With OUTERWEAR, the inner TOP has BASE/MID and the outer garment has OUTER/SHELL where applicable. |
| Applicable bulk | `outerwear.bulkIndex >= innerTop.bulkIndex` when bulk applies to both; missing required bulk fails readiness rather than being invented. |
| Environmental compatibility | Confirmed suitability supports the active band when available environmental context is used for hard filtering. |
| Missing environment | Skip unavailable environmental hard filtering where appropriate, retain other validity checks and explain missing context; missing weather alone does not reject all outfits. |
| Severe pattern/noise | Among TOP/BOTTOM/OUTERWEAR, at most one garment has Visual Noise ≥4 **or** Pattern Density HIGH. A garment meeting both still counts as one severe item. FOOTWEAR is outside this specific conflict count. |

| Active Environmental Band | Temperature |
|---|---|
| HOT | ≥30°C |
| WARM | ≥24°C and <30°C |
| MILD | ≥18°C and <24°C |
| COOL | ≥12°C and <18°C |
| COLD | <12°C |

Environmental suitability uses HOT/WARM/MILD/COOL/COLD/ALL_SEASON/UNKNOWN rather than a mandatory calendar-season taxonomy. ALL_SEASON expresses suitability across environmental bands; UNKNOWN cannot establish a required compatibility claim. Provider freshness remains OSQ-012.

Moderate patterns may coexist and are considered through soft ranking. Color harmony, declared style, current occasion, Like/Dislike, Wear/history, recency, diversity and optional body/gender are soft factors. Gender/body/stereotypes cannot become hard category restrictions. These rules apply equally to Shuffle's resulting outfit and to valid hypothetical combinations; candidate ownership remains hypothetical.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-OUT-001 | The system shall use only confirmed, active, owned garments that satisfy applicable readiness for current daily recommendations, excluding removed items and hypothetical candidates. | MVP | FEAT-OUT-001; BR-007; OSQ-005, OSQ-006 (Resolved) | T/A: ownership/confirmation/readiness/candidate eligibility fixtures. |
| FR-OUT-002 | The system shall evaluate available environmental/season context, current occasion, declared style, and relevant personalization context. | MVP | FEAT-OUT-001; BR-007, BR-010 | T/A: context variation. |
| FR-OUT-003 | The system shall exclude combinations that fail applicable hard validity constraints before personalized ranking. | MVP | FEAT-OUT-001; BR-009 | A: invalid high-preference fixture. |
| FR-OUT-004 | The system shall enforce exactly one TOP, one BOTTOM and one FOOTWEAR with zero or one OUTERWEAR, plus the applicable layering/bulk/environment/severe-pattern validity policies in Section 3.7.1 before ranking. | MVP | FEAT-OUT-001; BR-007, BR-009; OSQ-005, OSQ-006 (Resolved) | A: allowed 3/4-item composition; invalid slots/layers/bulk/pattern boundaries. |
| FR-OUT-005 | The system shall rank valid candidates through soft color harmony, style, current occasion, Like/Dislike, Wear/history, recency/diversity and optional body/gender, never using body/gender/stereotypes as hard category restrictions. | MVP | FEAT-OUT-001; BR-007, BR-009, BR-010, BR-011, BR-012; OSQ-006, OSQ-007 (Resolved) | A: preference differences among valid sets; no identity-based category rejection. |
| FR-OUT-006 | The system shall present at least three distinct valid outfits when that many exist under the current wardrobe/context. | MVP | FEAT-OUT-001; BR-008 | T/A: three-valid fixture. |
| FR-OUT-007 | The system shall count a reordered combination of the same garment identities as the same outfit rather than a distinct option. | MVP | FEAT-OUT-001; BR-008 | A: reordered duplicates. |
| FR-OUT-008 | The system shall show the actual fewer-valid/no-valid result with limitations instead of duplicate padding or invalid alternatives. | MVP | FEAT-OUT-001; BR-008, BR-022 | T: two, one, zero valid. |
| FR-OUT-009 | The system shall reevaluate or clearly mark advice outdated when relevant wardrobe/context/validity-rule inputs change, never presenting a failed refresh as current advice. | MVP | FEAT-OUT-001; BR-007, BR-022; OSQ-006, OSQ-010 (Resolved) | T: attribute/context and applicable rule-version changes. |

### 3.8 Outfit Explanation

Explanations reflect the context/information actually evaluated; internal weights or execution traces are not required UI.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-OUT-010 | The system shall identify the garments in an outfit and provide access to applicable garment information. | MVP | FEAT-OUT-002; BR-007 | D: outfit to garment detail. |
| FR-OUT-011 | The system shall explain relevant context and compatibility/preference reasons for recommending an outfit. | MVP | FEAT-OUT-002; BR-022 | I/A: explanation matches fixture. |
| FR-OUT-012 | The system shall disclose missing environmental/personal context that limits the recommendation's interpretation. | MVP | FEAT-OUT-002; BR-022 | I: unavailable-context reasoning. |
| FR-OUT-013 | The system shall distinguish current owned-outfit advice, historical reports, and hypothetical candidate previews. | MVP | FEAT-OUT-002; BR-022 | T/I: three presentation contexts. |

### 3.9 Shuffle

Only the selected item slot changes; the remaining garment identities and assessment context stay fixed.

| Stimulus | Expected System Response |
|---|---|
| Shuffle a selected item | Find a valid owned replacement compatible with fixed garments/context. |
| No compatible replacement or failure | Retain the selected outfit and explain no alternative or recovery. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-OUT-014 | The system shall replace only the user-selected garment slot during a successful Shuffle action. | MVP | FEAT-OUT-003; BR-008 | T: fixed other identities. |
| FR-OUT-015 | The system shall offer a selected-slot replacement only when the resulting outfit satisfies Section 3.7.1 validity with unchanged other garment identities and assessment context. | MVP | FEAT-OUT-003; BR-007, BR-009; OSQ-006 (Resolved) | A: fixed-slot replacements at layer/bulk/environment/pattern limits. |
| FR-OUT-016 | The system shall retain the current selection and explain the limitation when no compatible alternative exists. | MVP | FEAT-OUT-003; BR-008, BR-022 | T: no-alternative fixture. |
| FR-OUT-017 | The system shall avoid recording Like, Dislike, or a Wear Event solely because Shuffle was used. | MVP | FEAT-OUT-003; BR-008 | T: interaction isolation. |


### 3.10 Like / Dislike

Preference feedback is not reported wear and is reversible as specified by FEAT-PERS-001.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-PERS-001 | The system shall record Like as current positive feedback on the identified recommendation/exact outfit combination with conceptual base contribution +1, subject to Section 3.12.1 aging. | MVP | FEAT-PERS-001; BR-011; OSQ-007 (Resolved) | T/A: exact target, Like +1 and age-dependent influence. |
| FR-PERS-002 | The system shall record Dislike as current negative evidence for the identified recommendation/exact outfit combination with conceptual base contribution −2, without a permanent ban on its garments/categories. | MVP | FEAT-PERS-001; BR-011; OSQ-007 (Resolved) | T/A: Dislike −2; no automatic per-garment/category ban. |
| FR-PERS-003 | The system shall keep Like/Dislike state until changed, cleared, or superseded through user action and prevent simultaneously effective contradictory feedback for the same target. | MVP | FEAT-PERS-001; BR-011, BR-021; OSQ-007 (Resolved) | T: persisted state beyond 90 days, revise/clear; ranking aging separate. |
| FR-PERS-004 | The system shall distinguish accepted feedback from an unsuccessful recording attempt. | MVP | FEAT-PERS-001; BR-011 | T: failed versus recorded signal. |
| FR-PERS-005 | The system shall avoid creating Wear Events from Like, Dislike, recommendation views, or other preference-only interactions. | MVP | FEAT-PERS-001; BR-011 | T: history unchanged. |

### 3.11 Wear Events

New reporting requires a current valid owned outfit; event inspection/correction/removal targets an existing individual report. Logical-action retries preserve one event, while new explicit intentions create separate events. Each event retains its original event-local time.

| Stimulus | Expected System Response |
|---|---|
| Wear This Today for a valid owned outfit | Create a distinct user-reported event with local date/time and available context. |
| Another intentional report within the day | Retain earlier events; permit the same or a different valid outfit. |
| Repeated tap/retry of one action | Do not manufacture another event. |
| Correct/remove one event | Update that event's relevant history/signals without deleting Outfit/Garments or unrelated events. |

#### 3.11.1 Wear Event Identity and Time Rules

One logical Wear action is one explicit intention to report a wearing instance. Retry/delivery/reprocessing of that action yields one event; a new explicit initiation yields a separate event. User + outfit + calendar date is not sufficient duplicate identity.

Each event preserves absolute timestamp, associated timezone or UTC offset, original local date/time and user-reported meaning, with available occasion/context. Default event time is the accepted current time of the action. Device-timezone changes during later travel do not automatically re-date history.

The user can correct applicable outfit, occasion/context and local event time. A corrected time must not be future and must remain in the event's original local calendar day. Accepted corrections stay associated with the original event-local context; invalid corrections leave the prior accepted event unchanged. Individual removal affects effective history/utilization/recency/signals without deleting Outfit/Garments or unrelated events.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-WEAR-001 | The system shall create one distinct user-reported Wear Event for each successful explicit logical Wear action on a valid owned outfit, defaulting event time to the accepted current time of that action. | MVP | FEAT-PERS-002; BR-006, BR-011; OSQ-008 (Resolved) | T: new explicit initiation versus retries; accepted-action default time. |
| FR-WEAR-002 | The system shall permit multiple different outfit events in one local calendar day without replacing earlier events. | MVP | FEAT-PERS-002; BR-011; OSQ-008 (Resolved) | T: same-day different outfits. |
| FR-WEAR-003 | The system shall allow a new explicit Wear This Today initiation for the same valid outfit within the same local day to create a separate intentional event, without using user+outfit+date alone as duplicate identity. | MVP | FEAT-PERS-002; BR-011; OSQ-008 (Resolved) | T: separate same-outfit/same-day initiation creates two events. |
| FR-WEAR-004 | The system shall associate each event with outfit, absolute timestamp, event-associated timezone or UTC offset, original local date/time and user-reported nature, preserving that original event-local context after travel. | MVP | FEAT-PERS-002; BR-006, BR-011; OSQ-008 (Resolved) | T/I: absolute/local correspondence and later timezone change. |
| FR-WEAR-005 | The system shall retain recommendation context and occasion with an event when available without fabricating absent context. | MVP | FEAT-PERS-002; BR-011, BR-022; OSQ-008 (Resolved) | T: available/missing context. |
| FR-WEAR-006 | The system shall allow correction of an individual event's applicable outfit, occasion/context and local time, rejecting a future corrected time or one outside its original local calendar day. | MVP | FEAT-PERS-002; BR-006, BR-021; OSQ-008 (Resolved) | T: outfit/context correction; future/cross-day rejection in original timezone. |
| FR-WEAR-007 | The system shall allow removal of an individual Wear Event without removing unrelated events. | MVP | FEAT-PERS-002; BR-006, BR-021; OSQ-008 (Resolved) | T: individual removal. |
| FR-WEAR-008 | The system shall preserve the underlying Outfit and Garments when a Wear Event is removed. | MVP | FEAT-PERS-002; BR-006, BR-021; OSQ-008 (Resolved) | T: underlying objects remain. |
| FR-WEAR-009 | The system shall treat retry, repeated delivery or reprocessing of one logical Wear action as the same event, while treating a new explicit user initiation as a separate event even for the same outfit/day. | MVP | FEAT-PERS-002; BR-011; OSQ-008 (Resolved) | T: one logical action retried; separate explicit repeat; no date/outfit-only deduplication. |
| FR-WEAR-010 | The system shall reflect accepted creation/correction/removal in effective history/utilization/recency and normalized, age-weighted behavioral evidence without deleting other legitimate reports. | MVP | FEAT-PERS-002, FEAT-ANL-001, FEAT-PERS-003; BR-006, BR-011; OSQ-007, OSQ-008 (Resolved) | T/A: corrected outfit/time and removal change effective signals/history appropriately. |
| FR-WEAR-011 | The system shall avoid interpreting any event or repeated reporting as verified physical wear or as permission to bypass outfit validity. | MVP | FEAT-PERS-002, FEAT-PERS-003; BR-011, BR-009 | T/A: invalid/hypothetical attempts. |
| FR-WEAR-012 | The system shall allow cancellation of a pending Wear Event correction/removal without applying that requested change. | MVP | FEAT-PERS-002; BR-006, BR-021; OSQ-008 (Resolved) | T: cancel correction/removal; original event remains. |

### 3.12 Behavioral Personalization

The MVP uses explicit preferences and lightweight behavioral signals to rank already-valid outfit candidates. Advanced learned ranking is not required.

#### 3.12.1 Behavioral Personalization Rules

The following rules define behavioral contributions, aging, normalization, and recency for lightweight rule/score-based personalization. They specify ranking effects without defining a complete ranking algorithm; Business Rules will formalize reusable rules and examples. Hard validity always precedes all contributions/penalties.

| Evidence | Conceptual Base Contribution |
|---|---|
| Like on the identified recommendation/exact outfit combination | +1 |
| Dislike on that target | −2 |
| Wear-based preference increment | +2 |

| Behavioral Evidence Age | Ranking Influence |
|---|---|
| 0–30 days | 100% |
| 31–60 days | 50% |
| 61–90 days | 25% |
| >90 days | 0% |

The behavioral window is the most recent 90 days. Like/Dislike state remains until changed, cleared or superseded through user action; aging its ranking influence does not automatically clear that state. History can retain older events subject to OSQ-011 retention. Dislike targets the outfit and does not automatically ban every constituent garment.

All legitimate Wear Events remain separate in history. For ranking, the same exact outfit within the same event-local calendar day supplies at most one Wear-based preference increment, before applicable time decay. This normalization does not deduplicate separate intentional actions or collapse different outfits/days.

| Recency / Diversity Situation | Soft Ranking Effect |
|---|---|
| Exact outfit reported worn within the last 2 days | Strong diversity penalty. |
| Exact outfit reported worn 3–7 days ago | Mild diversity penalty. |
| Exact outfit last reported worn >7 days ago | No recency penalty. |
| Ready garment with no recorded wear for ≥14 days | May receive a small soft utilization/diversity boost. |

Strong/mild penalties remain ranking effects and never invalidate an outfit. The optional overlooked-item boost cannot override hard validity, strong explicit negative feedback or current context relevance. No additional numeric ranking weights or mandatory learned model are specified.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-PERS-006 | The system shall apply declared style and current occasion to relevant initial advice even without behavioral history. | MVP | FEAT-PERS-003, FEAT-PROF-001; BR-010 | A: no-history preference fixture. |
| FR-PERS-007 | The system shall apply the +1 Like/−2 Dislike base contributions and 90-day time-decay policy in Section 3.12.1 to effective outfit-targeted evidence, preserving feedback state until user revision. | MVP | FEAT-PERS-003, FEAT-PERS-001; BR-011; OSQ-007 (Resolved) | A: weights and 30/31/60/61/90/91-day aging; persisted state. |
| FR-PERS-008 | The system shall use a Wear-based preference increment of +2 before decay, stronger than Like's +1 under controlled conditions, subject to exact-outfit/day normalization. | MVP | FEAT-PERS-003, FEAT-PERS-002; BR-011; OSQ-007 (Resolved) | A: equal-age +2 versus +1; normalized intentional repeats. |
| FR-PERS-009 | The system shall apply strong soft exact-outfit diversity penalty within the last 2 days, mild penalty at 3–7 days and none beyond 7 days, permitting a small 14-day-overlooked ready-garment boost only within Section 3.12.1 limits. | MVP | FEAT-PERS-003, FEAT-ANL-001; BR-011, BR-006; OSQ-007 (Resolved) | A: 2/3/7/8-day recency boundaries; optional ≥14-day boost safeguards. |
| FR-PERS-010 | The system shall use optional body context only softly and avoid category restrictions or prerequisite eligibility based on body/gender. | MVP | FEAT-PERS-003, FEAT-PROF-001; BR-012 | T/A: optional context and eligibility. |
| FR-PERS-011 | The system shall keep validity dominant over every preference signal and avoid requiring a changed result when no useful valid alternative exists. | MVP | FEAT-PERS-003, FEAT-OUT-001; BR-009, BR-011 | A: constrained/no-alternative fixture. |
| FR-PERS-012 | The system shall limit multiple same-exact-outfit Wear Events in one event-local calendar day to at most one Wear-based preference increment and apply the 0–30/31–60/61–90/>90-day behavioral influence policy while retaining legitimate event history. | MVP | FEAT-PERS-003, FEAT-PERS-002; BR-011; OSQ-007, OSQ-008 (Resolved) | A: normalized outfit/day versus distinct outfits/days; 100%/50%/25%/0% decay. |
| FR-PERS-013 | The system shall recompute effective evidence after feedback/event/context revision or removal, applying decay/normalization without treating expired influence as deleted history or automatically cleared feedback. | MVP | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002; BR-011, BR-012; OSQ-007, OSQ-008 (Resolved) | T/A: clear/revise/remove; surviving same-day events; >90-day state retained. |

### 3.13 Wardrobe Utilization & History

History is user-reported and time-aware; preserved historical garments are not current ownership.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-ANL-001 | The system shall present events using their original event-local date/time and available context rather than automatically re-dating them when the current device timezone changes. | MVP | FEAT-ANL-001, FEAT-PERS-002; BR-006; OSQ-008 (Resolved) | T/I: original-local history after travel; accepted in-day correction. |
| FR-ANL-002 | The system shall display multiple same-day events separately, including intentional repeated outfit uses. | MVP | FEAT-ANL-001, FEAT-PERS-002; BR-006, BR-011 | T: multiple-event history. |
| FR-ANL-003 | The system shall derive reported garment-use frequency/recency and overlooked-item/variety indicators from accepted events, preserving distinct history beyond the 90-day ranking window subject to retention policy. | MVP | FEAT-ANL-001; BR-006, BR-011; OSQ-007, OSQ-008 (Resolved) | A: all legitimate reports versus normalized/expired ranking evidence; OSQ-011. |
| FR-ANL-004 | The system shall explain incomplete logging and label no recorded use without claiming that the garment was never physically worn. | MVP | FEAT-ANL-001; BR-006, BR-022 | I: logging limitations. |
| FR-ANL-005 | The system shall show a no-Wear-Events state without invented usage/diversity statistics. | MVP | FEAT-ANL-001; BR-006, BR-022 | T: empty history. |
| FR-ANL-006 | The system shall preserve understandable historical outfit/garment snapshots and label a garment removed from active ownership as Removed from wardrobe. | MVP | FEAT-ANL-001; BR-006, BR-022 | T/I: history after garment removal. |
| FR-ANL-007 | The system shall update event-specific history/utilization and relevant normalized ranking evidence after valid correction/removal, preserving unrelated records and original event-local context. | MVP | FEAT-ANL-001, FEAT-PERS-002; BR-006, BR-011; OSQ-007, OSQ-008 (Resolved) | T/A: correction/removal with another same-outfit/day event and historical snapshots. |

### 3.14 Wardrobe Coverage Score

The Personalized Everyday Capsule uses relevant common needs/priorities, style, climate/context, and the current confirmed wardrobe. It is one contextual model.

| Stimulus | Expected System Response |
|---|---|
| Request coverage with adequate information | Assess relevant needs and show score, covered/underserved areas, and limitations. |
| Information cannot support assessment | Explain missing information without a fabricated numeric score or proven purchase need. |
| Needs/context/wardrobe changes | Reevaluate or mark the prior assessment outdated. |

#### 3.14.1 Wardrobe Coverage Rules

Wardrobe Coverage for the Personalized Everyday Capsule uses the following need mapping, priority weights, formulas, and sufficient-data criteria. Valid distinct combinations for a need use the mapped occasion context and outfit validity rules; the same garment-identity combination is not counted twice by permutation.

| Capsule Need | Relevant Occasion Codes |
|---|---|
| Everyday / Casual | EVERYDAY, DATE, SOCIAL_EVENT |
| Work / Professional | WORK |
| School / University | SCHOOL_UNIVERSITY |
| Formal / Special Event | FORMAL_EVENT |
| Travel | TRAVEL |
| Sport / Active | SPORT_ACTIVITY |

| Need Priority | Weight |
|---|---|
| Not Relevant | 0 |
| Low | 1 |
| Medium | 2 |
| High | 3 |

`NeedCoverage = min(ValidDistinctOutfitsForNeed / 3, 1)`

Per-need presentation: 0 valid outfits → 0%; 1 → approximately 33%; 2 → approximately 67%; ≥3 → 100%. NeedCoverage is a ratio in [0, 1]; percentage presentation multiplies that ratio by 100.

`CoverageScore = 100 × Σ(PriorityWeight × NeedCoverage) / Σ(PriorityWeight)`

Only needs with weight >0 participate in the denominator. A numeric score requires at least one positive-weight need and at least one Recommendation-Ready TOP, BOTTOM and FOOTWEAR. OUTERWEAR is additionally required only where the evaluated need/environment requires it. Failure of these gates produces insufficient-information/incomplete assessment, not a false 0%.

A relevant need with NeedCoverage <1 (below 100% displayed coverage) may prompt gap analysis; the system identifies an underserved capability/bottleneck before evaluating a specific garment. Sufficiently described but incompatible garments can yield defensible zero coverage; incompletely digitized information cannot be mislabeled as evaluated zero. This score measures contextual wardrobe coverage rather than universal wardrobe completeness.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-ANL-008 | The system shall evaluate the Personalized Everyday Capsule's relevant needs through the Section 3.14.1 occasion mapping and valid-distinct-outfit rules rather than a universal checklist. | MVP | FEAT-ANL-002, FEAT-PROF-001; BR-014, BR-010; OSQ-006, OSQ-009 (Resolved) | A: six need mappings and unique outfit counts per need. |
| FR-ANL-009 | The system shall identify active need priorities, mapped occasions, style, environmental context and current confirmed/eligible wardrobe used in coverage. | MVP | FEAT-ANL-002; BR-014, BR-022; OSQ-009 (Resolved) | I/A: visible assessment basis and disabled priority-zero needs. |
| FR-ANL-010 | The system shall use only positive priorities as weights in the CoverageScore denominator so changing need priorities can change the same wardrobe's contextual score. | MVP | FEAT-ANL-002, FEAT-PROF-001; BR-014, BR-010; OSQ-009 (Resolved) | A: weights 0/1/2/3; zero excluded; controlled weighted comparison. |
| FR-ANL-011 | The system shall show numeric CoverageScore only when Section 3.14.1 sufficient-data conditions hold and calculate it as 100 × Σ(PriorityWeight × NeedCoverage) / Σ(PriorityWeight). | MVP | FEAT-ANL-002; BR-014, BR-022; OSQ-009 (Resolved) | A/T: exact weighted fixtures; no-active-need/missing-ready-role cases suppress number. |
| FR-ANL-012 | The system shall calculate NeedCoverage as min(ValidDistinctOutfitsForNeed / 3, 1), presenting covered/underserved need evidence and its mapped context. | MVP | FEAT-ANL-002; BR-014, BR-022; OSQ-006, OSQ-009 (Resolved) | A: 0/1/2/3/4 valid outfits yield 0%/≈33%/≈67%/100%/100%. |
| FR-ANL-013 | The system shall exclude removed garments from current coverage. | MVP | FEAT-ANL-002; BR-014 | A: post-removal baseline. |
| FR-ANL-014 | The system shall reevaluate or mark coverage outdated after relevant wardrobe, needs, context or applicable validity-rule changes. | MVP | FEAT-ANL-002; BR-014, BR-022; OSQ-006, OSQ-009 (Resolved) | T: changed priority, garment attributes and applicable rule version. |
| FR-ANL-015 | The system shall distinguish insufficient information from defensible evaluated coverage, withholding a false 0% while avoiding universal-completeness or compulsory-purchase claims. | MVP | FEAT-ANL-002; BR-014, BR-022, BR-024; OSQ-009 (Resolved) | T/I: insufficient versus valid zero; no universal completion claim. |

### 3.15 Gap Analysis

Gap analysis follows adequate contextual coverage; a gap is an underserved capability, not absence of a named catalog product.

| Stimulus | Expected System Response |
|---|---|
| Inspect an underserved need | Describe the capability, its evidence, useful candidate characteristics, and an evaluation path. |
| No important gap or insufficient evidence | Explain the finding/limitation without manufacturing shopping demand. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-GAP-001 | The system shall consider a positive-priority need with NeedCoverage below 100% for gap analysis and identify its evidence-supported underserved capability/bottleneck before evaluating a specific candidate. | MVP | FEAT-GAP-001; BR-015; OSQ-009 (Resolved) | A: 0/1/2 versus ≥3 valid need outfits, priority-zero exclusion, capability-first evidence. |
| FR-GAP-002 | The system shall explain useful candidate characteristics without making one specific product a mandatory solution. | MVP | FEAT-GAP-001; BR-015, BR-022 | I: gap versus product wording. |
| FR-GAP-003 | The system shall connect supported gaps to candidate evaluation and estimated outfit expansion when evaluable candidates exist. | MVP | FEAT-GAP-001; BR-015 | D: gap to utility evaluation. |
| FR-GAP-004 | The system shall explain no important gap or insufficient assessment without manufacturing demand, never treating lack of sufficient coverage data as a numeric 0% or a compulsory purchase. | MVP | FEAT-GAP-001; BR-014, BR-022, BR-024; OSQ-009 (Resolved) | T: no-gap, incomplete and valid-zero distinction. |
| FR-GAP-005 | The system shall permit continued insight/context/wardrobe use without requiring external shopping. | MVP | FEAT-GAP-001; BR-024 | D: commerce-independent path. |

### 3.16 Wardrobe Multiplier

The count is incremental unique valid combinations, not a weighted score or a guarantee. Candidate evaluation never creates owned clothing.

| Stimulus | Expected System Response |
|---|---|
| Evaluate a candidate | Compare current and hypothetical expanded valid outfit sets under the same context. |
| Inspect +N | Explain newly enabled utility and show hypothetical previews containing the candidate. |
| Evaluation is zero, insufficient, or outdated | Distinguish evaluated zero from unavailable/stale evaluation; do not invent a count. |

#### 3.16.1 Wardrobe Multiplier Evaluation Rules

Wardrobe Multiplier is calculated as:

`WardrobeMultiplier(candidate) = |O(W ∪ {candidate}) − O(W)|`

W is the current confirmed active wardrobe; O produces unique valid outfit sets under one consistent relevant context/rule version. The formula specifies required meaning, not enumeration or persistence design.

| State | Required Meaning / Presentation |
|---|---|
| Exact | Current complete evaluation with the prerequisites below; an exact +N may be shown. |
| Evaluated Zero | Every exact-evaluation prerequisite succeeds and the newly enabled set is empty; show +0 New Outfits. |
| Incomplete | Required candidate/owned information is insufficient or full evaluation cannot reliably complete; suppress exact +N. |
| Unavailable | The evaluation capability cannot operate; suppress exact +N and do not substitute zero. |
| Outdated | Relevant inputs/rules changed since evaluation; a prior result cannot be current until reevaluated. |

Exact evaluation requires sufficient candidate readiness; applicable readiness for all owned garments used in relevant combinations; required relevant context; completed current and expanded evaluation; completed uniqueness/deduplication; and the same context, validity rules, relevant rule version and garment-identity uniqueness semantics for both sets. These conditions also govern Evaluated Zero.

Garment-identity sets define canonical outfit uniqueness; permutation creates no additional combination. Failures, timeouts, missing information and unavailable evaluation do not establish +0 or a complete exact count.

Changes to active wardrobe composition, confirmed garment attributes, candidate attributes, context or relevant validity/business rules make the prior result Outdated. Freshness is input/rule based; no arbitrary time-based TTL is specified. Weather freshness affecting context remains OSQ-012.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-MULT-001 | The system shall evaluate a sufficiently described hypothetical candidate against the current confirmed active wardrobe, using only applicable ready owned garments in counted combinations and excluding removed inventory. | MVP | FEAT-MULT-001; BR-015, BR-016; OSQ-005, OSQ-010 (Resolved) | A: candidate/owned readiness, removed baseline and hypothetical ownership. |
| FR-MULT-002 | The system shall compare current and expanded valid outfit sets under the same context, validity/business-rule version and identity-based uniqueness semantics. | MVP | FEAT-MULT-001; BR-009, BR-016; OSQ-006, OSQ-010 (Resolved) | A: matched context/rule-version versus mismatched comparison. |
| FR-MULT-003 | The system shall calculate WardrobeMultiplier(candidate) as the cardinality of O(W ∪ {candidate}) − O(W), using garment-identity sets so existing or permuted combinations do not inflate new valid outfits. | MVP | FEAT-MULT-001; BR-016; OSQ-010 (Resolved) | A: exact set-difference fixtures and identity permutations. |
| FR-MULT-004 | The system shall show an exact +N New Outfits only when Section 3.16.1 completeness/readiness/context/version/deduplication conditions hold, explaining the evaluated baseline and count. | MVP | FEAT-MULT-001; BR-016, BR-022; OSQ-010 (Resolved) | T/A: complete 41→58 gives +17; fail each exact-result prerequisite. |
| FR-MULT-005 | The system shall provide newly enabled hypothetical previews for an exact completed evaluation, visibly identifying the unowned candidate and associating previews with the same evaluated context/rule basis. | MVP | FEAT-MULT-001; BR-017; OSQ-010 (Resolved) | T/I: newly enabled candidate previews; stale/incomplete previews not current exact evidence. |
| FR-MULT-006 | The system shall leave candidate ownership and wear history unchanged by evaluation or preview viewing. | MVP | FEAT-MULT-001; BR-015, BR-017 | T: no ingestion/wear side effect. |
| FR-MULT-007 | The system shall distinguish Exact, Evaluated Zero, Incomplete, Unavailable and Outdated, showing +0 only for a fully completed exact evaluation with no newly enabled outfits and suppressing exact +N for non-exact states. | MVP | FEAT-MULT-001; BR-016, BR-022; OSQ-010 (Resolved) | T/A: all five states; failures/timeouts/missing data never become zero. |
| FR-MULT-008 | The system shall mark a prior multiplier Outdated when relevant wardrobe composition, confirmed/candidate attributes, context or validity/business rules change, withholding current exact status until reevaluated without arbitrary time-based TTL. | MVP | FEAT-MULT-001; BR-016, BR-022; OSQ-010 (Resolved) | T: each input/rule trigger; unchanged inputs do not expire under an invented TTL. |

### 3.17 Strategic Shopping

Usable candidate utility must satisfy PRD display rules; optional credible commercial data cannot gate it.

| Stimulus | Expected System Response |
|---|---|
| Open a usable candidate | Show applicable visual/identity/category/subtype/gap/attributes, +N, why it helps, and new hypothetical previews. |
| Optional information is credible or missing | Show qualified information when credible; retain utility when absent. |
| Open/decline an external destination | Treat navigation as optional; returning is not purchase verification or automatic ingestion. |
| No useful candidate or unavailable destination | Explain and retain useful wardrobe insight. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-SHOP-001 | The system shall show applicable candidate image/visual, human-readable identity, primary category, subtype, gap addressed, and relevant attributes for each usable recommendation. | MVP | FEAT-SHOP-001; BR-018, BR-020 | I: required candidate information. |
| FR-SHOP-002 | The system shall show exact +N, reasons and newly enabled previews for usable completed candidate utility, explicitly identifying incomplete/unavailable/outdated evaluation instead of presenting it as current exact advice. | MVP | FEAT-SHOP-001, FEAT-MULT-001; BR-016, BR-017, BR-018; OSQ-010 (Resolved) | T/I: exact usable utility versus all non-exact evaluation states. |
| FR-SHOP-003 | The system shall show price range, material, longevity/durability, retailer identity, or destination only when credible and available, qualifying estimates. | Conditional | FEAT-SHOP-001; BR-020 | I: credible and missing optional fields. |
| FR-SHOP-004 | The system shall retain supported candidate utility without optional commercial fields, while avoiding invented merchant claims or exact +N/previews unsupported by evaluation completeness. | MVP | FEAT-SHOP-001; BR-020, BR-024; OSQ-010 (Resolved) | T/I: missing commercial data versus missing required utility data. |
| FR-SHOP-005 | The system shall provide optional navigation to an available external destination with an explicit external-shopping indication. | Conditional | FEAT-SHOP-002; BR-018 | T: available link handoff. |
| FR-SHOP-006 | The system shall preserve candidate/gap/utility context after known destination failure or normal return where available. | MVP | FEAT-SHOP-002; BR-018, BR-022 | T: failed link and return. |
| FR-SHOP-007 | The system shall avoid equating candidate/link interaction with verified sale, purchase, automatic garment ingestion, or reported wear. | MVP | FEAT-SHOP-002; BR-018, BR-019 | T/I: navigation side effects. |
| FR-SHOP-008 | The system shall keep core advice available without native cart, payment, orders, fulfillment, purchase, or affiliate participation. | MVP | FEAT-SHOP-001, FEAT-SHOP-002; BR-018, BR-024 | D/I: independent core journey. |

### 3.18 Product Measurement

This specifies conceptual measurable outcomes, not an analytics schema/vendor or an administrative product.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-MET-001 | The system shall make confirmed garment additions, attribute corrections, outfit requests/views, Shuffle outcomes, and Like/Dislike changes distinguishable for measurement. | MVP | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001; BR-019, BR-001, BR-007 | T/I: distinct interaction evidence. |
| FR-MET-002 | The system shall distinguish Wear Event creation, correction, and removal as separate measurement meanings. | MVP | FEAT-MET-001, FEAT-PERS-002; BR-019, BR-011 | T/I: event lifecycle evidence. |
| FR-MET-003 | The system shall make coverage/gap/candidate/previews views and external-link attempts/openings distinguishable without interpreting them as sales. | MVP | FEAT-MET-001, FEAT-SHOP-002; BR-019 | T/I: exposure versus navigation. |
| FR-MET-004 | The system shall support recognition/preview validation and processing/recommendation timing evidence under OSQ-001's locked benchmark, timing boundaries and p95 targets, alongside manual continuity/utilization/repeat-use measurement. | MVP | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001, FEAT-ANL-001; BR-019, BR-001, BR-007, BR-006; OSQ-001 (Resolved) | A/I: separate accuracy reports, sample durations/start-end boundaries and honest event outcomes. |
| FR-MET-005 | The system shall avoid inflation from one logical Wear action's retries, distinguishing intentional event counts from normalized same-outfit/day ranking evidence. | MVP | FEAT-MET-001, FEAT-PERS-002; BR-019, BR-011; OSQ-007, OSQ-008 (Resolved) | T/A: two intentional history events versus one preference increment; retry counted once. |
| FR-MET-006 | The system shall respect authorized privacy handling and avoid requiring raw personal images or sensitive profile details as engagement-metric content. | MVP | FEAT-MET-001; BR-021 | I: measurement information. |
| FR-MET-007 | The system shall preserve an otherwise usable core action when measurement is unavailable without falsely changing its success/failure state. | MVP | FEAT-MET-001; BR-019 | T: measurement dependency failure. |


## 4. Data Requirements

### 4.1 Logical Information Domains

These requirements specify information meaning/ownership, not tables, storage types, indexes, object persistence, or an ERD.

| Domain | Logical Information |
|---|---|
| Access and context | User Account, Personalization Profile, request context, Refresh Sessions and single-use reset validity; logical policy information, not session-store design. |
| Current wardrobe | Wardrobe, active/removed Garment, confirmed Garment Profile, and applicable images/proposals/readiness. |
| Advice and behavior | Outfit, Outfit Recommendation, Feedback, Wear Event, and Wear History. |
| Wardrobe intelligence | Coverage Assessment, Wardrobe Gap, Candidate Garment, Multiplier Assessment, and Shopping Recommendation. |
| Measurement | Distinguishable exposures, accepted actions/corrections/removals, failures, and qualified metric evidence. |

### 4.2 Account & Profile Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-AUTH-001 | The system shall associate credentials/recovery/session information with the correct account, protect those secrets, and preserve the password value and policy-governed validity outcomes in Section 3.1.1. | MVP | FEAT-AUTH-001; BR-021; OSQ-002, OSQ-003 (Resolved) | T/I: account ownership; exact password meaning; protected recovery/session state. |
| DATA-AUTH-002 | The system shall represent Email through the normalized account identity defined in Section 3.1.1 and Display Name as optional, preserving one account per identity without provider-specific alias transformations. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: uniqueness across email case/whitespace variants; optional Display Name. |
| DATA-AUTH-003 | The system shall retain the user's current declared styles/common priorities and distinguish them from request-specific occasion. | MVP | FEAT-PROF-001; BR-010 | T/I: independent context values. |
| DATA-AUTH-004 | The system shall represent omitted/removed body/gender as absent current context rather than inferred replacements. | MVP | FEAT-PROF-001; BR-012, BR-021 | T/I: removed optional values. |
| DATA-AUTH-005 | The system shall represent separate login sessions with account association, maximum lifetime, current/consumed refresh validity and revocation state, and reset interactions with account association, expiry and single-use validity, sufficient to enforce Section 3.1.1. | MVP | FEAT-AUTH-001; BR-021; OSQ-002, OSQ-003 (Resolved) | T/I: independent sessions, rotated-token reuse, current/all-session revocation, reset expiry/use. |

### 4.3 Garment & Wardrobe Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-GAR-001 | The system shall associate each current garment/profile with its owning user's wardrobe and confirmed primary category/dominant color. | MVP | FEAT-GAR-001, FEAT-WAR-001; BR-003, BR-005 | T/I: owner and saveable minimum. |
| DATA-GAR-002 | The system shall represent Section 4.10 garment descriptors with bounded values/scales, SOLID → NONE density, environmental rather than calendar-season primary taxonomy, and applicable unknown/optional states. | MVP | FEAT-GAR-001; BR-003; OSQ-005 (Resolved) | I/T: descriptor sets, scales, SOLID density and non-mandatory rich information. |
| DATA-GAR-003 | The system shall associate proposals with evidence-based confidence/provenance under Section 8.1 separately from authoritative user values, without treating numeric confidence as confirmation. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003; OSQ-004, OSQ-005 (Resolved) | T/I: default thresholds, evidence guards, calibration and confirmation. |
| DATA-GAR-004 | The system shall associate available image/preview information with the appropriate draft or garment without requiring an image for manually saved entries. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002 | T: imageless and image entry. |
| DATA-GAR-005 | The system shall represent active ownership, removed state and applicable readiness separately, retaining saveable entries with UNKNOWN/unavailable required compatibility fields. | MVP | FEAT-GAR-001, FEAT-WAR-002; BR-003, BR-005; OSQ-005 (Resolved) | T/I: conditional layering/bulk readiness and retained minimum-only entries. |

### 4.4 Outfit & Recommendation Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-OUT-001 | The system shall identify an outfit through its set of constituent garment identities, treating every permutation of that set as one unique combination for advice, feedback, daily Wear normalization and multiplier comparison. | MVP | FEAT-OUT-001, FEAT-MULT-001; BR-008, BR-016; OSQ-006, OSQ-007, OSQ-010 (Resolved) | A: identity permutations and exact-combination targeting. |
| DATA-OUT-002 | The system shall associate advice with its evaluated wardrobe/context and relevant validity-rule version, sufficient for explanations and input/rule-based outdated-state detection. | MVP | FEAT-OUT-001, FEAT-OUT-002; BR-007, BR-022; OSQ-006, OSQ-010 (Resolved) | T/I: context/rule version and changed-input detection. |
| DATA-OUT-003 | The system shall distinguish current owned outfits, historical selections, and hypothetical candidate outfits in their logical meaning. | MVP | FEAT-OUT-002, FEAT-MULT-001; BR-017, BR-022 | I: three outfit contexts. |

### 4.5 Feedback & Wear Event Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-WEAR-001 | The system shall associate current outfit-targeted Like/Dislike state and applicable action-time information with the correct user/combination, sufficient to preserve state and apply the contribution/decay rules in Section 3.12.1. | MVP | FEAT-PERS-001; BR-011, BR-021; OSQ-007 (Resolved) | T/I: target, user revision, age and persistent feedback versus expired influence. |
| DATA-WEAR-002 | The system shall represent each intentional Wear Event with outfit, absolute timestamp, associated timezone or UTC offset, original local date/time context, available occasion/context, user-reported meaning and applicable corrected information. | MVP | FEAT-PERS-002; BR-006, BR-011; OSQ-008 (Resolved) | I/T: time information and valid same-original-day correction. |
| DATA-WEAR-003 | The system shall distinguish logical-action identity from exact-outfit/local-day grouping, preserving separate intentional events while deduplicating one action and normalizing only its applicable ranking contribution. | MVP | FEAT-PERS-002; BR-011; OSQ-007, OSQ-008 (Resolved) | T/A: retries versus explicit repeats; two history records, one day preference increment. |
| DATA-WEAR-004 | The system shall reflect valid event corrections/removals in effective history and normalized/age-weighted signals without deleting underlying Outfit/Garments or unrelated events. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-011; OSQ-007, OSQ-008 (Resolved) | T/A: surviving reports and recalculated contribution after removal/correction. |

### 4.6 Coverage / Gap / Candidate Evaluation Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-ANL-001 | The system shall associate coverage with mapped needs, 0/1/2/3 priorities, sufficient-data state, relevant context/rule basis, valid distinct outfit counts, per-need ratios and the weighted score/limitations. | MVP | FEAT-ANL-002; BR-014, BR-022; OSQ-006, OSQ-009 (Resolved) | I/A: formula inputs/results and sufficient-versus-incomplete state. |
| DATA-ANL-002 | The system shall associate each proposed gap with a relevant positive-weight underserved need, its capability/bottleneck evidence and candidate characteristics rather than a mandatory product name. | MVP | FEAT-GAP-001; BR-015, BR-022; OSQ-009 (Resolved) | I/A: <100% need evidence and capability-before-candidate linkage. |
| DATA-ANL-003 | The system shall represent the hypothetical candidate separately from owned garments with required identity/visual/category/subtype/gap/attribute information, applicable readiness and credible optional commercial values. | MVP | FEAT-SHOP-001, FEAT-MULT-001; BR-015, BR-018, BR-020; OSQ-005, OSQ-010 (Resolved) | I/T: usable versus incomplete candidate and unowned distinction. |
| DATA-ANL-004 | The system shall associate multiplier state/count/previews with candidate, wardrobe/context, relevant rule version, readiness/completion/deduplication evidence and input/rule-based freshness. | MVP | FEAT-MULT-001; BR-016, BR-017, BR-022; OSQ-010 (Resolved) | A/I: five states and exact-result evidence; no arbitrary TTL. |

### 4.7 Data Integrity

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-INT-001 | The system shall prevent an AI proposal or later analysis from silently superseding a confirmed/corrected garment value. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002 | T: authority preservation. |
| DATA-INT-002 | The system shall exclude removed garments from effective current recommendation/coverage/multiplier inputs even when historical snapshots remain. | MVP | FEAT-WAR-002, FEAT-MULT-001, FEAT-ANL-002; BR-005, BR-014, BR-016 | A: current versus historical input. |
| DATA-INT-003 | The system shall avoid creating owned garments or Wear Events solely from candidate evaluation, preview, or external navigation. | MVP | FEAT-MULT-001, FEAT-SHOP-002; BR-015, BR-017, BR-018 | T: no ownership/wear side effect. |
| DATA-INT-004 | The system shall keep failed/pending mutations distinct from accepted state, preventing duplicates of the same logical action without collapsing separate explicit Wear intentions. | MVP | FEAT-AI-002, FEAT-PERS-002; BR-005, BR-011; OSQ-008 (Resolved) | T: interrupted additions/Wear actions; retry versus explicit same-outfit/day repeat. |

### 4.8 Historical Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-HIST-001 | The system shall retain meaningful past event/outfit/garment snapshots and original event-local time context after garment removal, timezone travel or behavioral-ranking expiry, subject to the unresolved retention policy. | MVP | FEAT-ANL-001; BR-006; OSQ-007, OSQ-008 (Resolved) | T/I: removed garment, >90-day event and device timezone change; OSQ-011. |
| DATA-HIST-002 | The system shall identify removed garments in historical presentation as Removed from wardrobe without treating the snapshot as an active item. | MVP | FEAT-ANL-001; BR-006, BR-022; OSQ-008 (Resolved) | I/A: removed snapshot. |

### 4.9 Retention / Deletion

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-RET-001 | The system shall remove a deleted event from effective history/utilization/recency/signals while retaining Outfit/Garments and unrelated events, recomputing any surviving outfit/day preference increment. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-021; OSQ-007, OSQ-008 (Resolved) | T/A: individual removal versus surviving same-day report. |
| DATA-RET-002 | The system shall retain meaningful history after active-garment removal and avoid deleting events merely because they exceed the 90-day behavioral ranking window, subject to approved retention policy. | MVP | FEAT-WAR-002, FEAT-ANL-001; BR-005, BR-006; OSQ-007, OSQ-008 (Resolved) | T: removed garment and aging history; OSQ-011 retention remains open. |

No physical purge mechanism, legal retention duration, indefinite retention guarantee, account-export/closure workflow, or unrestricted future AI-training use is specified. Remaining retention/deletion/consent rules are OSQ-011; they must respect the settled historical and user-control semantics.

### 4.10 Logical Data Dictionary

| Object / Information | Logical Meaning / Necessary Relationships |
|---|---|
| User Account | Private access identity and authorized relationship to personal wardrobe/profile/history; Email/password recovery and optional Display Name. |
| Personalization Profile | Current style/common need priorities, optional body/gender, and selected location/context; current request occasion remains separate. |
| Wardrobe | Current confirmed active owned garments; differs from historical snapshots. |
| Garment | Individually distinguishable owned item, supported primary category, authoritative profile, active/removed state, and readiness. |
| Garment Profile | Confirmed category/dominant color plus bounded pattern/density/noise, layering/bulk/fit, climate suitability/color family/temperature/palette role and supported optional silhouette/material/secondary coordinates/style/occasion information; readiness applies conditional required fields. |
| Outfit | Constituent garment identities assessed in context; reordering identities creates no new combination. |
| Outfit Recommendation | A proposed valid owned outfit with context and relevant reasons, distinct from a past report or hypothetical preview. |
| Feedback | Current +1/−2 outfit-targeted preference, revision state and applicable evidence time for aging; persists until user action and never implies reported wear. |
| Wear Event | Report for a logical Wear intention, with absolute timestamp, event timezone/offset and original local date/time plus available occasion/context; bounded event-specific correction/removal and separate ranking normalization. |
| Wear History | Time-aware view of accepted reports and meaningful snapshots, including multiple events per day; incomplete logging is disclosed. |
| Coverage Assessment | Positive-priority mapped capsule needs, valid distinct outfit counts/Need Coverage, weighted score and sufficient/incomplete context state for one Personalized Everyday Capsule. |
| Wardrobe Gap | Evidence-supported underserved capability related to coverage, not a compulsory named product. |
| Candidate Garment | Hypothetical potential addition with relevant characteristics; is not owned before separate confirmation. |
| Multiplier Assessment | Same-context/rule-version unique-valid set comparison with Exact/Evaluated Zero/Incomplete/Unavailable/Outdated meaning, completeness evidence and appropriate hypothetical previews. |
| Shopping Recommendation | Candidate/gap/utility/reason/previews and credible optional commercial details; external commerce boundary. |
| Provenance / uncertainty | How information was established versus uncertainty of a proposal; confirmation establishes authority without proving physical material composition. |
| Refresh Session | Account-associated separate login context, maximum lifetime, refresh consumption/revocation state and access-renewal meaning under Section 3.1.1; no physical storage structure. |

The category/subtype/style/occasion values below mirror PRD Sections 11.3 and 13.2. Additional descriptors use the bounded values in the second table. Further vocabulary changes require approval and source traceability.

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

OTHER/UNKNOWN does not introduce a primary category. Common need dimensions and request occasion codes have different purposes; the resolved mapping is in Section 3.14.1.

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

Unknown/unavailable information remains distinct from an actual scale value. Descriptor support does not make every field mandatory to save. TOP/OUTERWEAR layering and applicable layered Bulk Index are readiness checks only under Section 3.5.1.

## 5. External Interface Requirements

### 5.1 User Interfaces

These requirements preserve navigation responsibilities and semantic states; they do not dictate screens, components, pixels, or a design system.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| UI-001 | The system shall make Today, Wardrobe, Insights, and Profile responsibilities reachable with Add Garment and applicable detail/return paths. | MVP | FEAT-WAR-001, FEAT-OUT-001, FEAT-PROF-001; BR-005, BR-007, BR-010 | D: navigation through JRN-01–JRN-07. |
| UI-002 | The system shall present the progressive onboarding flow with optional context and non-blocking first-garment guidance. | MVP | FEAT-AUTH-001, FEAT-PROF-001; BR-012, BR-021, BR-023 | D: onboarding skip/return. |
| UI-003 | The system shall distinguish loading, empty, no-match, pending, confirmed and failed states plus Exact/Evaluated Zero/Incomplete/Unavailable/Outdated multiplier states where applicable. | MVP | FEAT-WAR-001, FEAT-OUT-001, FEAT-MULT-001; BR-005, BR-016, BR-022; OSQ-010 (Resolved) | T/I: generic state inventory and five multiplier result states. |
| UI-004 | The system shall display garment identity/category/color, readiness guidance, and available image without hiding imageless manual entries. | MVP | FEAT-WAR-001, FEAT-GAR-001; BR-005, BR-003 | I: cards and minimum-only detail. |
| UI-005 | The system shall display High Confidence, Needs Review and Uncertain through understandable Vietnamese semantic labels under the Section 8.1 mapping, retaining confirmation authority and avoiding primary raw-score presentation. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003; OSQ-004 (Resolved) | I/T: threshold boundaries, evidence guards and localized field guidance. |
| UI-006 | The system shall require explicit confirmation for an authoritative garment entry and expose correction/manual controls. | MVP | FEAT-AI-002; BR-002 | D: proposal to corrected confirmation. |
| UI-007 | The system shall show Wear Events with original local date/time and event-specific inspect/correct/remove controls governed by the no-future/same-original-day correction bounds. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-021; OSQ-008 (Resolved) | D/T: multiple-event lifecycle and invalid correction guidance. |
| UI-008 | The system shall visibly distinguish hypothetical candidates/previews from owned outfits and removed historical garments from active ownership. | MVP | FEAT-MULT-001, FEAT-ANL-001; BR-017, BR-006, BR-022; OSQ-008, OSQ-010 (Resolved) | I: hypothetical and removed indicators. |
| UI-009 | The system shall show required candidate utility information and identify optional external navigation without requiring absent commercial data. | MVP | FEAT-SHOP-001, FEAT-SHOP-002; BR-018, BR-020, BR-024 | I/T: usable candidate without retailer. |

### 5.2 Software Interfaces

The interfaces below identify logical capabilities. Garment analysis may be delivered internally or externally; no service/module boundary, provider, transport payload, or endpoint is selected.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| SI-001 | The system shall exchange selected-image analysis input and available preview/proposals/uncertainty with the garment-analysis capability while retaining user confirmation as authority. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002 | T/I: analysis success/failure exchange. |
| SI-002 | The system shall exchange selected location and relevant environmental information with a weather capability and distinguish unavailable/stale results. | MVP | FEAT-PROF-002; BR-013, BR-022 | T: location/weather variants. |
| SI-003 | The system shall use email delivery for password recovery while keeping initiation responses account-existence-safe and distinguishing a valid completed reset from requested, failed, expired, or consumed interactions. | MVP | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: email recovery outcomes; paired known/unknown account responses. |
| SI-004 | The system shall support optional external-destination handoff without treating retailer operations as CapsuleAI transactions or guaranteed outcomes. | MVP | FEAT-SHOP-002; BR-018, BR-024 | T/I: external navigation boundary. |

### 5.3 Hardware Interfaces

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| HW-001 | The system shall request camera access for the selected capture action and offer other permitted image routes or manual entry when unavailable/denied. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-004 | T: camera denial alternatives. |
| HW-002 | The system shall use a permitted photo-library selection without requiring camera access or successful analysis for manual entry. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-004 | T: library/manual alternatives. |
| HW-003 | The system shall use device location only with permission and preserve manual city/skip choices when permission is denied. | MVP | FEAT-PROF-002; BR-013 | T: grant/deny/manual/skip. |

### 5.4 Communication Interfaces

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| COM-001 | The system shall protect private access/wardrobe/profile/history exchanges against unauthorized disclosure or alteration, preserving password values and the session/account authority defined in Section 3.1.1. | MVP | FEAT-AUTH-001; BR-021; OSQ-002, OSQ-003 (Resolved) | A/T: protected exchange; Unicode/spaces preserved; session ownership. |
| COM-002 | The system shall distinguish interrupted communication from accepted save, access/recovery or Wear action outcomes, applying Section 9 recovery without false completion or duplicated logical actions. | MVP | FEAT-AI-002, FEAT-PERS-002, FEAT-AUTH-001; BR-005, BR-011, BR-021; OSQ-002, OSQ-003, OSQ-008 (Resolved) | T: interrupted save/reset/refresh/Wear actions and outcome review. |
| COM-003 | The system shall avoid granting external destinations unrestricted access to private wardrobe/profile/history merely because a shopping link was opened. | MVP | FEAT-SHOP-002, FEAT-AUTH-001; BR-018, BR-021 | I/T: handoff data/privacy boundary. |

## 6. Quality Requirements

Quality requirements apply to MVP behavior and derive from BRD MET-Q01–MET-Q03 and PRD Section 25. They cover locked-set recognition acceptance, criterion-based preview review, garment-processing p95 ≤5 seconds, and outfit-result p95 <3 seconds. The following sections define timing boundaries and workload; reference device/network and operating configuration remain OSQ-013. Availability, throughput, concurrency, and recovery targets are not quantified here; architecture decisions remain downstream.

### 6.1 Performance

Faster valid completion is acceptable.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-PERF-001 | The system shall complete garment processing at p95 ≤5 seconds from accepted analysis request after image transfer completes to available usable processed preview and reviewable proposals, under reference conditions governed by OSQ-013. | MVP | FEAT-AI-001, FEAT-MET-001; BR-001, BR-019; OSQ-001 (Resolved) | T/A: accepted-request-to-preview/proposals durations, p95 and recorded reference configuration. |
| NFR-PERF-002 | The system shall provide the first complete outfit recommendation result set at p95 <3 seconds from accepted request with required context available, for the specified 100-confirmed-garment nominal workload. | MVP | FEAT-OUT-001, FEAT-MET-001; BR-007, BR-019; OSQ-001 (Resolved) | T/A: p95 with exactly 100 confirmed garments; reference device/network OSQ-013. |

### 6.2 Security

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-SEC-001 | The system shall permit zero successful unauthorized cross-user wardrobe/image accesses in the agreed validation scenarios. | MVP | FEAT-AUTH-001, FEAT-WAR-001; BR-021 | T: two-account and unauthenticated attempts; MET-Q06. |
| NFR-SEC-002 | The system shall prevent account switch or logout from exposing the preceding user's protected content, enforcing current-session logout isolation and all-Refresh-Session revocation after successful password reset. | MVP | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: two sessions/accounts; current logout versus account-wide reset. |
| NFR-SEC-003 | The system shall protect credential/token/recovery secrets from unauthorized output/measurement disclosure and preserve account-existence-safe Forgot Password responses. | MVP | FEAT-AUTH-001, FEAT-MET-001; BR-021; OSQ-002, OSQ-003 (Resolved) | I/T: secret-output review and known/unknown email response comparison. |

### 6.3 Privacy

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-PRIV-001 | The system shall explain the relevant use of personal images, profile, location, and behavioral information and respect the originating feature's omission, correction, removal, and permission controls. | MVP | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002, FEAT-PERS-001, FEAT-PERS-002; BR-012, BR-013, BR-021 | I/D: purpose/control review across JRN-01–JRN-05. |
| NFR-PRIV-002 | The system shall avoid treating garment corrections, behavior recording, or external navigation as automatic unrestricted permission for future AI training or retailer data sharing. | MVP | FEAT-AUTH-001, FEAT-AI-002, FEAT-MET-001, FEAT-SHOP-002; BR-002, BR-018, BR-021 | I: usage and external-handoff boundaries; OSQ-011. |

### 6.4 Reliability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-REL-001 | The system shall preserve manual garment creation in all agreed representative AI-failure scenarios without changing confirmed information into an unconfirmed proposal. | MVP | FEAT-AI-001, FEAT-AI-002; BR-002, BR-004 | T: agreed unavailable/failed/uncertain analysis cases; MET-Q05. |
| NFR-REL-002 | The system shall maintain controlled coverage/formula/explanation and unique-valid multiplier integrity under the validity, completeness, and same-context/rule-version rules. | MVP | FEAT-MULT-001, FEAT-ANL-002; BR-014, BR-016, BR-022; OSQ-006, OSQ-009, OSQ-010 (Resolved) | A: exact coverage fixtures and complete/zero/incomplete/outdated multiplier cases; MET-Q07. |

### 6.5 Availability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-AVL-001 | The system shall keep otherwise usable current-wardrobe and core advice paths independent of unavailable optional weather, shopping, or measurement dependencies, subject to available access/network and sufficient valid assessment information. | MVP | FEAT-WAR-001, FEAT-OUT-001, FEAT-SHOP-002, FEAT-MET-001; BR-005, BR-007, BR-019, BR-024 | T: isolated dependency failures and applicable reduced-context/limited-result states. |

A production availability percentage, outage window, and recovery target are not established. OSQ-013 must define any required operating targets before quality acceptance; this requirement establishes dependency independence, not guaranteed offline access.

### 6.6 Usability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-USE-001 | The system shall make the initial Android/iOS journeys usable through understandable Vietnamese guidance for setup, confirmation/correction, readiness, limited results, and recovery. | MVP | FEAT-AUTH-001, FEAT-AI-002, FEAT-WAR-001, FEAT-OUT-001; BR-002, BR-005, BR-022, BR-023 | D/I: representative users complete core journeys; evaluation criteria OSQ-014. |
| NFR-USE-002 | The system shall evaluate preview usability by identifiable garment, preserved major regions, non-obstructive remaining background and sufficient correction/confirmation information, independently of recognition accuracy and without an invented preview percentage. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002, BR-022; OSQ-001 (Resolved) | D/A: each usable-preview criterion, acceptable background and manual fallback. |

### 6.7 Accessibility

These are requirement-level interpretations of BRD stakeholder usability/accessibility expectations and explainable product states, not a claim of certification against an unselected standard.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-ACC-001 | The system shall communicate meaningful confidence, readiness, unavailable/outdated, hypothetical, and removed states with understandable labels rather than relying solely on color or a garment image. | MVP | FEAT-GAR-001, FEAT-WAR-001, FEAT-OUT-001, FEAT-MULT-001, FEAT-ANL-001; BR-003, BR-005, BR-006, BR-022 | I/D: labels remain meaningful without color/image cues. |
| NFR-ACC-002 | The system shall make core action meanings and outcomes understandable in the agreed Android/iOS accessibility validation scenarios; supported assistive interactions and acceptance criteria remain OSQ-014. | MVP | FEAT-AUTH-001, FEAT-AI-002, FEAT-OUT-001, FEAT-PERS-002; BR-002, BR-011, BR-022, BR-023 | D: accessibility scenarios TBD; no unsupported conformance level. |

### 6.8 Maintainability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-MNT-001 | The system shall preserve the controlled semantic meaning of categories/subtypes, styles, occasions, confidence, and provenance across label/localization changes without silently changing confirmed data or domain behavior. | MVP | FEAT-GAR-001, FEAT-PROF-001, FEAT-AUTH-001; BR-003, BR-010, BR-023 | I/T: semantic vocabulary and changed-label regression. |

### 6.9 Testability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-TEST-001 | The system shall make the evaluated context, relevant confirmed inputs, limitations, and displayed results inspectable through authorized product behavior so controlled validity, coverage, and multiplier cases can be verified. | MVP | FEAT-OUT-001, FEAT-ANL-002, FEAT-MULT-001; BR-009, BR-014, BR-016, BR-022 | A/I: known fixtures and explained results; no test-only public interface required. |
| NFR-TEST-002 | The system shall make validation inputs/results, accepted versus attempted/failed actions and specified timing start/end evidence inspectable without requiring sensitive personal contents in engagement metrics. | MVP | FEAT-MET-001; BR-019, BR-021; OSQ-001 (Resolved) | A/I: locked benchmark and p95 evidence; measurement privacy. |

### 6.10 Interoperability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-INT-001 | The system shall preserve the logical meaning of dependency success, incomplete information, and unavailability across garment-analysis, weather, email-recovery, and external-shopping interactions. | MVP | FEAT-AI-001, FEAT-PROF-002, FEAT-AUTH-001, FEAT-SHOP-002; BR-001, BR-013, BR-018, BR-021, BR-022 | T/I: representative interface outcome contracts; OSQ-012. |

### 6.11 Scalability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-SCA-001 | The system shall retain valid core wardrobe/recommendation behavior for the 100-confirmed-garment timing benchmark without silently omitting eligible inputs to meet latency, while preserving growth direction subject to OSQ-013 capacity refinement. | MVP | FEAT-WAR-001, FEAT-OUT-001; BR-005, BR-007, BR-009; OSQ-001 (Resolved) | T/A: 100-item reference manifest, applicable eligible inputs and result integrity. |

The nominal timing workload contains 100 confirmed garments, refining the upstream growing-wardrobe/100+ direction without setting an onboarding quota or a maximum. Larger-load/capacity/concurrency acceptance remains OSQ-013.

### 6.12 Supportability / Manageability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-SUP-001 | The system shall make failed/unavailable/pending operation states and applicable recovery choices distinguishable so users can act without interpreting a technical failure as empty inventory or successful completion. | MVP | FEAT-AUTH-001, FEAT-AI-002, FEAT-OUT-001, FEAT-SHOP-002; BR-002, BR-007, BR-018, BR-021, BR-022 | T/I: failure-state and recovery inspection. |
| NFR-SUP-002 | The system shall support agreed validation of processing, recommendation, manual continuation, privacy, and utility integrity without exposing credential secrets or requiring raw private images in engagement diagnostics. | MVP | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001, FEAT-MULT-001; BR-001, BR-007, BR-016, BR-019, BR-021 | A/I: evidence review; MET-Q01–MET-Q07. |

## 7. Localization Requirements

The English canonical terms in this document describe meaning; equivalent understandable Vietnamese labels are required in the initial UI. English documentation does not imply English MVP screens.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| LOC-001 | The system shall provide the initial user interface, explanations, and applicable recovery guidance in Vietnamese on Android and iOS. | MVP | FEAT-AUTH-001, FEAT-OUT-001, FEAT-AI-002; BR-002, BR-022, BR-023 | I/D: initial Vietnamese journeys. |
| LOC-002 | The system shall keep canonical category/subtype, style/occasion, confidence, and provenance semantics independent of translated display labels. | MVP | FEAT-PROF-001, FEAT-GAR-001, FEAT-AUTH-001; BR-003, BR-010, BR-023 | I/T: vocabulary-to-label mapping. |
| LOC-003 | The system shall allow later label-language additions without redefining those established domain meanings or changing existing confirmed information. | MVP | FEAT-PROF-001, FEAT-GAR-001, FEAT-AUTH-001; BR-003, BR-010, BR-023 | I: localization readiness; no second MVP language. |
| LOC-004 | The system shall preserve Vietnamese text and diacritics accurately in supported user entry and display. | MVP | FEAT-AUTH-001, FEAT-PROF-001; BR-010, BR-021, BR-023 | T: Vietnamese names and applicable text round-trip. |
| LOC-005 | The system shall present event-local date/time in understandable Vietnamese while preserving the original event timezone/offset/day through travel, in-day correction and multiple intentional same-day reports. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-011, BR-022; OSQ-008 (Resolved) | T/I: original-local date after travel; future/cross-day correction rejected. |

Project documentation remains in English. Event-local time, travel behavior, and correction limits follow Section 3.11.1; scheduling/calendar capabilities are outside MVP scope.

## 8. Other Requirements

### 8.1 AI-Specific Requirements

AI assistance reduces effort while user confirmation establishes authority. The following criteria define confidence presentation and independent recognition validation. Provenance records how a value was established. Model architecture and training infrastructure remain design matters; advanced learned ranking is not an MVP prerequisite.

#### 8.1.1 Confidence and Recognition Validation

The default confidence mapping below communicates prediction uncertainty independently of provenance. High Confidence proposals still require user review/confirmation.

| Default Confidence | Semantic State |
|---|---|
| ≥0.85 | High Confidence |
| ≥0.60 and <0.85 | Needs Review |
| <0.60 | Uncertain |

High Confidence is prohibited when prediction evidence is unavailable/materially conflicting, image quality prevents reliable inference, or the attribute/model has not met its required High Confidence validation quality. A non-high state must communicate the limitation without fabricating a score. Validation may justify stricter attribute-calibrated thresholds; criteria cannot be weakened merely to increase High Confidence frequency. Raw numerical confidence is not the primary UI representation.

Independent recognition validation uses the locked benchmark below. Section 3.4.1 defines image qualification and preview usability.

| Benchmark Element | Acceptance Criteria |
|---|---|
| Locked set | ≥200 qualified images; TOP ≥50, BOTTOM ≥50, OUTERWEAR ≥50, FOOTWEAR ≥50. |
| Category ground truth | Human-labeled supported Primary Category. |
| Color ground truth | Controlled dominant-color-family labels from Section 4.10. |
| Labeling protocol | Two independent human annotators should label the benchmark; disagreement is resolved through adjudication. |
| Evaluation isolation | The locked set is not used for model training/tuning after lock. |
| Category accuracy | ≥90% overall; each primary-category group's recognition accuracy ≥85%. |
| Dominant-color accuracy | ≥90% overall, reported separately. |
| Preview usability | Criterion-based review under Section 3.4.1; no percentage threshold is invented. |

These targets do not automatically apply to every rich attribute. Dataset/label manifests, qualification decisions and evaluation-isolation evidence support reproducible acceptance. Reference devices/network and nominal operating configuration remain OSQ-013; no training infrastructure or model architecture is selected.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| AI-REQ-001 | The system shall treat unconfirmed AI-generated garment values as suggestions rather than authoritative wardrobe information, regardless of predicted confidence. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003; OSQ-004 (Resolved) | T: high-confidence proposal before confirmation. |
| AI-REQ-002 | The system shall allow users to review and correct proposed attribute information before confirming the garment profile. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003; OSQ-004 (Resolved) | T: proposed, corrected, and confirmed values. |
| AI-REQ-003 | The system shall allow users to continue with manual information when analysis is unavailable, uncertain, or unsuitable, including imageless manual creation. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-004 | T: analysis failures and manual completion. |
| AI-REQ-004 | The system shall apply default confidence mapping High Confidence ≥0.85, Needs Review ≥0.60 and <0.85, and Uncertain <0.60, subject to Section 8.1 evidence/validation guards and stricter calibrated criteria. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003; OSQ-004 (Resolved) | T/A: 0.60/0.85 boundaries; missing/conflicting evidence; stricter calibration. |
| AI-REQ-005 | The system shall use understandable localized confidence states as the primary presentation rather than require raw numeric confidence for user decisions. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003; OSQ-004 (Resolved) | I: review presentation. |
| AI-REQ-006 | The system shall distinguish AI Suggested, User Confirmed, User Corrected, and User Entered provenance for applicable garment information. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003; OSQ-004 (Resolved) | T/I: proposal, accept, change, and manual entry. |
| AI-REQ-007 | The system shall make the user's confirmed, corrected, or entered-and-confirmed values authoritative while preventing later AI output from silently replacing them. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002; OSQ-004 (Resolved) | T: confirmation and subsequent analysis. |
| AI-REQ-008 | The system shall preserve unknown/missing attribute states instead of fabricating category details, colors, materials, or optional context to complete a profile. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | T/I: partial/unsupported information. |
| AI-REQ-009 | The system shall qualify material predictions/claims when uncertain rather than present inferred composition as verified physical fact. | MVP | FEAT-GAR-001; BR-003, BR-022 | I: unknown and qualified material. |
| AI-REQ-010 | The system shall achieve ≥90% overall primary-category recognition accuracy and ≥85% accuracy within each TOP/BOTTOM/OUTERWEAR/FOOTWEAR benchmark group on the locked qualified-image set in Section 8.1. | MVP | FEAT-AI-001, FEAT-GAR-001, FEAT-MET-001; BR-001, BR-003, BR-019; OSQ-001 (Resolved) | A: overall category result and four per-category floors; locked benchmark. |
| AI-REQ-011 | The system shall achieve ≥90% overall dominant-color recognition accuracy against controlled color-family ground truth on the same locked qualified-image benchmark, measured separately from category/preview quality. | MVP | FEAT-AI-001, FEAT-GAR-001, FEAT-MET-001; BR-001, BR-003, BR-019; OSQ-001, OSQ-005 (Resolved) | A: independent color-family accuracy report; adjudicated labels. |
| AI-REQ-012 | The system shall identify an attribute marked Needs Review and prompt the user to check its proposed value while retaining review/correction control. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003; OSQ-004 (Resolved) | T/I: Needs Review field guidance. |
| AI-REQ-013 | The system shall explain that an Uncertain attribute could not be identified confidently and offer correction/manual information rather than imply a confirmed prediction. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003, BR-004; OSQ-004 (Resolved) | T/I: Uncertain field continuation. |
| AI-REQ-014 | The system shall support recognition validation against a locked set of at least 200 qualified images, including at least 50 per primary category, and exclude the locked evaluation set from model training/tuning. | MVP | FEAT-AI-001, FEAT-GAR-001, FEAT-MET-001; BR-001, BR-003, BR-019; OSQ-001 (Resolved) | A/I: benchmark manifest/counts, qualification, label adjudication and training/tuning exclusion evidence. |

The ≥90% category/color targets are separate from rich-attribute expectations and preview criteria. The tables above define qualification, ground truth, locked-set minimum/balance, and accuracy floors; reference operating configuration remains OSQ-013. Verification distinguishes user-authority integrity from prediction accuracy.

### 8.2 Explainability Requirements

FR-OUT-010–FR-OUT-013 cover relevant outfit reasoning and limitations; FR-ANL-009–FR-ANL-015 cover contextual coverage; FR-GAP-001–FR-GAP-004 cover capability evidence; FR-MULT-004/FR-MULT-005 and FR-SHOP-001–FR-SHOP-004 cover incremental candidate utility. AI-REQ-004–AI-REQ-009 govern uncertainty/provenance/material limitations.

These are the normative explanation requirements. Explanations must correspond to the actual evaluated information; no universal fashion correctness, physical wear, purchase, fit, savings, or durability guarantee is established.

### 8.3 Privacy / Data Protection Requirements

FR-AUTH-008, DATA-AUTH-001, COM-001/COM-003, NFR-SEC-001–NFR-SEC-003, and NFR-PRIV-001/NFR-PRIV-002 establish private access, protected exchange, clear purposes, and control. FR-PROF-005–FR-PROF-007 and HW-003 preserve optional context. FR-MET-006 limits engagement evidence; DATA-RET-* preserves settled effective-removal/history semantics.

Consent, retention, and broader deletion/sharing boundaries remain OSQ-011. Optional fields and permissions cannot become undisclosed access gates.

### 8.4 Auditability / Provenance Requirements

DATA-GAR-003, DATA-INT-001, and AI-REQ-006/AI-REQ-007 make suggestion versus authoritative information verifiable. DATA-WEAR-002–DATA-WEAR-004, DATA-HIST-001/DATA-HIST-002, and FR-MET-002/FR-MET-005 distinguish logical actions, original event-local timestamps, bounded corrections/removals, historical snapshots and duplicate processing. History counts remain distinct from same-outfit/day ranking normalization.

This does not require an immutable audit store, indefinite retention, an administrator console, or a particular event architecture. The observable accepted state and its meaning are the requirement.

### 8.5 Legal / Regulatory Considerations

The baselines establish Vietnam-first scope and privacy/trust commitments, not certification or a named legal-compliance regime. This SRS makes no additional legal claim or statutory retention rule. Product/legal review must resolve applicable handling policy through OSQ-011 before final privacy acceptance; any newly binding obligation must be approved and traced through the source documents.

### 8.6 Operational Requirements

FR-MET-001–FR-MET-007, NFR-SUP-001/NFR-SUP-002, and Section 9 define interpretable measurement, protected validation evidence, failure distinction, and recoverability. No production results or uptime claim is asserted.

Supported operating conditions/capacity remain OSQ-013. Operational targets and architectural significance will be refined later; CI/CD products, deployment topology, monitoring vendors, backup mechanisms, and recovery tactics are outside this document.

## 9. Error and Failure Requirements

These requirements specify recoverable outcomes, not exception types or infrastructure retry algorithms. Previously confirmed information remains authoritative. An interrupted response is not proof that an action failed; uncertain outcomes must remain distinguishable from accepted results, and recovery must not create accidental duplicate logical actions.

### 9.1 Access and Recovery

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-AUTH-001 | Invalid or unusable login information. | The system shall deny invalid credentials or unavailable authorized session access with useful correction/recovery guidance without exposing private content or representing the wardrobe as empty. | User may correct input or initiate recovery; personal state is not changed. | FEAT-AUTH-001; BR-021; OSQ-002, OSQ-003 (Resolved) | T: incorrect credentials, token expiry and revoked session. |
| ERR-AUTH-002 | Reset cannot complete, is expired, or has already been used. | The system shall reject failed, expired, or already-used recovery interactions without claiming reset completion, offering renewed recovery with account-existence-safe initiation wording. | Credentials and session state change only on an actually completed reset; successful reset revokes all Refresh Sessions and requires login. | FEAT-AUTH-001; BR-021; OSQ-003 (Resolved) | T: 30-minute expiry, consumed interaction, delivery failure, known/unknown email. |
| ERR-AUTH-003 | Renewal is unusable, the session is revoked/expired, or rotated Refresh Token reuse is detected. | The system shall require renewed authentication for expired/revoked Refresh Sessions or detected rotated-token reuse, retaining the inaccessible-versus-empty distinction. | Protected session use pauses; reauthenticate. Reuse revokes the affected session, and logout does not automatically revoke other sessions. | FEAT-AUTH-001; BR-021; OSQ-002 (Resolved) | T: maximum 30-day expiry and consumed refresh-token replay. |

### 9.2 Analysis and Uncertainty

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-AI-001 | Input violates image limits/format support, is unsuitable, or analysis cannot complete. | The system shall explain unsupported formats, file-size/dimension violations, unsuitable images or unavailable/failed analysis with replacement/manual/retry recovery while preserving confirmed profiles. | Manual entry remains usable; no garment is added from an unconfirmed failed attempt. | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002, BR-004; OSQ-001 (Resolved) | T: unsupported input, >15 MB, shortest side <512, failed analysis. |
| ERR-AI-002 | Prediction is uncertain or low confidence. | The system shall identify guarded uncertain/incomplete predictions under Section 8.1 and allow correction/manual continuation without silently confirming them or fabricating missing evidence. | User may confirm sufficient corrected/manual information; confidence never gates saving the confirmed minimum. | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003, BR-004; OSQ-004 (Resolved) | T: <0.60, materially conflicting/unavailable evidence and user correction. |

### 9.3 Garment Completeness

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-GAR-001 | Save attempted without the saveable minimum. | The system shall explain missing confirmed Primary Category or Dominant Color and retain the draft for correction rather than save it as an authoritative garment. | User supplies/changes the missing information; existing garments remain unchanged. | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003 | T: each minimum value missing. |
| ERR-GAR-002 | Saved garment lacks recommendation-ready information. | The system shall retain a minimum-confirmed saveable garment while explaining required UNKNOWN/unavailable pattern/climate/layering/bulk fields and excluding only decisions requiring those missing fields. | User can enrich the profile; no fabricated descriptor or invalid outfit. | FEAT-GAR-001, FEAT-WAR-001, FEAT-OUT-001; BR-003, BR-005, BR-007, BR-009; OSQ-005 (Resolved) | T: category-specific and layered readiness cases. |

### 9.4 Environmental Context

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-WEATHER-001 | Weather/location information cannot support the request. | The system shall disclose unavailable/stale weather, offer applicable context review/retry, and skip unavailable environmental hard filtering where appropriate while enforcing remaining validity without false weather claims. | Missing weather alone does not reject every outfit; use remaining valid information and disclose reduced context. Provider freshness remains OSQ-012. | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-013, BR-022; OSQ-006 (Resolved) | T: missing weather retains structurally/layer/pattern-valid options; stale policy OSQ-012. |

### 9.5 Outfit Choice and Shuffle

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-OUT-001 | Insufficient applicable information for useful advice. | The system shall explain insufficient applicable ready garments/context under Sections 3.5.1/3.7.1 and useful corrections/additions without a fixed onboarding quota. | Wardrobe/manual setup remains available; accepted inventory is not erased. | FEAT-OUT-001, FEAT-GAR-001; BR-007, BR-009, BR-022; OSQ-005, OSQ-006 (Resolved) | T: no ready role, unknown required field and conditional layering/bulk failure. |
| ERR-OUT-002 | Fewer than three distinct valid outfits exist. | The system shall show the actual one, two, or zero distinct valid outfits with a limitation/no-valid-outfit explanation rather than padding with duplicates or invalid combinations. | User may revise context or wardrobe; no fake third option or fabricated wardrobe gap. | FEAT-OUT-001; BR-007, BR-008, BR-009, BR-022 | T/A: known two/one/zero valid sets. |
| ERR-OUT-003 | No compatible same-context slot replacement. | The system shall retain the fixed outfit and explain no replacement when no owned candidate satisfies the same slot/context and full validity rules. | Other garments remain fixed; no Dislike or Wear Event is inferred. | FEAT-OUT-003; BR-007, BR-008, BR-009, BR-022; OSQ-006 (Resolved) | T: no valid same-slot substitute; incompatible/high-noise replacements rejected. |

### 9.6 Wear Event Protection

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-WEAR-001 | Duplicate processing of one reporting action. | The system shall deduplicate retry/delivery/reprocessing of the same logical Wear action without collapsing a new explicit same-outfit/same-day intention or confusing ranking normalization with event deletion. | One event per logical action; distinct intentional reports remain in history, with at most one Wear preference increment per exact outfit/event-local day. | FEAT-PERS-002; BR-011; OSQ-007, OSQ-008 (Resolved) | T: logical-action retries versus new explicit repeat and daily contribution cap. |
| ERR-WEAR-002 | Event mutation fails/is uncertain, or corrected time is future/outside the original local day. | The system shall reject future or cross-original-day time corrections and distinguish failed/pending event mutations from accepted changes without deleting Outfit/Garments or unrelated events. | Invalid corrections leave the accepted event unchanged; uncertain outcomes require review/safe retry. Valid corrections/removals update effective history/signals. | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-011, BR-021, BR-022; OSQ-008 (Resolved) | T: future/cross-day corrections, interrupted mutation, original event retained. |

### 9.7 Assessment Integrity

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-ANL-001 | Coverage lacks positive-priority/required ready-role data, or multiplier inputs/completion/capability cannot support an exact result. | The system shall show incomplete/insufficient coverage or gap assessment when sufficient-data conditions fail and distinguish Incomplete/Unavailable multiplier evaluation from exact zero, without fabricated scores or purchase needs. | Improve required context/readiness or retry available evaluation; numeric 0% and +0 require adequate completed assessments. | FEAT-ANL-002, FEAT-GAP-001, FEAT-MULT-001; BR-014, BR-015, BR-016, BR-022; OSQ-009, OSQ-010 (Resolved) | T/A: no positive need; missing ready role; incomplete/unavailable versus full exact zero. |
| ERR-ANL-002 | Prior advice/assessment no longer matches current relevant inputs or rules. | The system shall reevaluate affected assessments or mark them Outdated after relevant wardrobe/profile/context/candidate or validity-rule changes, withholding current exact multiplier status until reevaluation. | Historical results remain identifiable; prior counts/previews are not current exact evidence. Weather freshness remains OSQ-012. | FEAT-OUT-001, FEAT-ANL-002, FEAT-MULT-001; BR-007, BR-014, BR-016, BR-022; OSQ-009, OSQ-010 (Resolved) | T: changes to all relevant input/rule triggers and context/version mismatch. |

### 9.8 Candidate Information and Shopping Destinations

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-SHOP-001 | Candidate unavailable, insufficient, or unhelpful. | The system shall explain unavailable/insufficient candidate information through the applicable Incomplete or Unavailable evaluation state without inventing attributes, exact counts or current previews. | Wardrobe/gap/context work can continue; no forced purchase path. | FEAT-SHOP-001, FEAT-MULT-001; BR-015, BR-016, BR-017, BR-018, BR-022; OSQ-005, OSQ-010 (Resolved) | T: candidate missing readiness fields versus unavailable capability. |
| ERR-SHOP-002 | Optional commercial data unavailable or uncertain. | The system shall omit/qualify unverified optional commercial data while retaining supported exact candidate utility or clearly labeled evaluation limitations. | Usable +N/reasons/previews remain accessible when supported; no invented claim. | FEAT-SHOP-001; BR-018, BR-020, BR-022; OSQ-010 (Resolved) | T/I: optional commercial omissions with valid exact versus incomplete multiplier. |
| ERR-SHOP-003 | External shopping navigation cannot proceed. | The system shall explain a known absent/unavailable external destination and retain available candidate/wardrobe utility without claiming navigation or purchase succeeded. | User may continue core use/retry where appropriate; retailer operations are outside system control. | FEAT-SHOP-002, FEAT-SHOP-001; BR-018, BR-022, BR-024 | T: absent link and known handoff failure. |

### 9.9 Communication and Partial Dependency Failure

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-NET-001 | Network/communication fails during an action. | The system shall distinguish interrupted communication and uncertain mutation outcomes from confirmed success or empty data, providing applicable outcome review/retry without accidental duplicate actions. | Previously confirmed information is preserved; an already accepted action is not repeated as a new one. No general offline guarantee. | FEAT-AI-002, FEAT-PERS-002, FEAT-WAR-001; BR-005, BR-011, BR-022 | T: interruption before/after action acceptance. |
| ERR-DEP-001 | External capability unavailable. | The system shall identify the affected unavailable dependency and preserve independent authorized core paths instead of presenting a dependency failure as loss of the user's wardrobe. | Use manual/retry/recovery paths appropriate to Sections 9.1–9.8; mandatory access remains protected. | FEAT-AI-001, FEAT-PROF-002, FEAT-AUTH-001, FEAT-SHOP-002; BR-001, BR-013, BR-018, BR-021, BR-022 | T: isolated analysis/weather/email/navigation failures. |
| ERR-MET-001 | Measurement dependency fails. | The system shall avoid blocking or falsely reversing an otherwise usable core action solely because product measurement is unavailable. | Core result remains accurate; unavailable evidence is not reported as a successful recorded metric. | FEAT-MET-001; BR-019 | T: accepted core action with unavailable measurement. |

## 10. Requirement Traceability

### 10.1 Traceability Model

The unchanged chain is BG-* → BR-* → CAP-* → FEAT-* → SRS requirement. BRD Section 26 and PRD Section 33 retain goal/business/capability meaning. Capability context below derives from each cited feature; it does not assert that an individual requirement independently delivers every feature capability.

Links to resolved OSQ-001–OSQ-010 in requirement source columns and the matrix record software decisions alongside the existing feature and business sources. Supporting DATA/UI/SI/HW/COM/NFR/LOC/AI/ERR rows retain source and verification information. ID ranges include only contiguous defined requirements.

These audits establish specification coverage; implementation and successful verification require separate evidence.

### 10.2 Functional Requirement Traceability Matrix

| SRS Requirement | PRD Feature | BRD Requirement | Capability Context | Resolved Software Decision | Verification |
|---|---|---|---|---|---|
| FR-AUTH-001 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: optional name; password limits and email identity/conflict fixtures. |
| FR-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: email case/space variants; exact Unicode/space-containing passwords. |
| FR-AUTH-003 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: paired known/unknown email responses and recovery handoff. |
| FR-AUTH-004 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002, OSQ-003 (Resolved) | T: successful reset; old password/refresh sessions denied; new-password login. |
| FR-AUTH-005 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: ordinary return, valid renewal, expired session, separate-device context. |
| FR-AUTH-006 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: 15-minute expiry with usable/unusable renewal; 30-day session limit. |
| FR-AUTH-007 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: logout Session A; protected A access denied; Session B unaffected. |
| FR-AUTH-008 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: signed-out/cross-user denial. |
| FR-AUTH-009 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T/I: absent, invalid, expired and valid JWT access; session authorization. |
| FR-AUTH-010 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: access expiry and absolute session-lifetime boundaries across refreshes. |
| FR-AUTH-011 | FEAT-AUTH-001, FEAT-PROF-001 | BR-012, BR-021 | CAP-01, CAP-06 | — | T: omitted optional information. |
| FR-AUTH-012 | FEAT-AUTH-001 | BR-021 | CAP-01 | — | T: each required field missing and mismatch. |
| FR-AUTH-013 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: successful rotation and attempted reuse. |
| FR-AUTH-014 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-002 (Resolved) | T: rotated-token replay; affected session revoked. |
| FR-AUTH-015 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: 11/12/128/129-character inputs; spaces/Unicode; no required class mixture. |
| FR-AUTH-016 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: duplicate case/space variants and distinct provider-alias strings. |
| FR-AUTH-017 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: valid, expiry-boundary, used-interaction and repeated-reset cases. |
| FR-AUTH-018 | FEAT-AUTH-001 | BR-021 | CAP-01 | OSQ-003 (Resolved) | T: successfully issue a new password-reset interaction; verify all earlier unused interactions for the account are rejected and the new interaction remains usable. |
| FR-PROF-001 | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002 | BR-010, BR-012, BR-013, BR-023 | CAP-01, CAP-05, CAP-06, CAP-07 | — | D: first-use journey. |
| FR-PROF-002 | FEAT-PROF-001 | BR-010 | CAP-01, CAP-06 | — | T: every defined style choice. |
| FR-PROF-003 | FEAT-PROF-001 | BR-010, BR-014 | CAP-01, CAP-06 | OSQ-009 (Resolved) | T: every priority and need mapping; differing request/common occasion. |
| FR-PROF-004 | FEAT-PROF-001, FEAT-OUT-001 | BR-010 | CAP-01, CAP-05, CAP-06 | — | T: differing current/common context. |
| FR-PROF-005 | FEAT-PROF-001 | BR-012 | CAP-01, CAP-06 | — | T: optional-field lifecycle. |
| FR-PROF-006 | FEAT-PROF-001 | BR-012 | CAP-01, CAP-06 | — | T: gender/category combinations. |
| FR-PROF-007 | FEAT-PROF-001, FEAT-PERS-003 | BR-012, BR-021 | CAP-01, CAP-05, CAP-06 | — | T: subsequent request after removal. |
| FR-PROF-008 | FEAT-AUTH-001, FEAT-PROF-001 | BR-012, BR-021 | CAP-01, CAP-06 | — | D: skip and return. |
| FR-WEATHER-001 | FEAT-PROF-002 | BR-013 | CAP-01, CAP-05, CAP-07 | — | T: grant, deny, skip. |
| FR-WEATHER-002 | FEAT-PROF-002 | BR-013 | CAP-01, CAP-05, CAP-07 | — | T: manual city after denial. |
| FR-WEATHER-003 | FEAT-PROF-002 | BR-007, BR-013 | CAP-01, CAP-05, CAP-07 | — | T: selected-location response. |
| FR-WEATHER-004 | FEAT-PROF-002, FEAT-OUT-001 | BR-007, BR-022 | CAP-01, CAP-05, CAP-06, CAP-07 | — | I/T: context matches evaluation. |
| FR-WEATHER-005 | FEAT-PROF-002, FEAT-OUT-001 | BR-007, BR-022 | CAP-01, CAP-05, CAP-06, CAP-07 | — | T: changed-location assessment. |
| FR-AI-001 | FEAT-AI-001 | BR-001 | CAP-02, CAP-04 | OSQ-001 (Resolved) | T: supported capture inputs and size/dimension boundaries. |
| FR-AI-002 | FEAT-AI-001 | BR-001 | CAP-02, CAP-04 | OSQ-001 (Resolved) | T: all formats; valid non-plain backgrounds and limit boundaries. |
| FR-AI-003 | FEAT-AI-002 | BR-004 | CAP-02, CAP-03, CAP-04 | — | T: imageless/manual completion. |
| FR-AI-004 | FEAT-AI-001 | BR-001 | CAP-02, CAP-04 | — | T: replace/cancel selected image. |
| FR-AI-005 | FEAT-AI-001 | BR-001, BR-022 | CAP-02, CAP-04 | — | T: delayed analysis state. |
| FR-AI-006 | FEAT-AI-001 | BR-001, BR-003 | CAP-02, CAP-04 | OSQ-001 (Resolved) | T/I: identifiable garment, retained major regions, reviewable background/proposals. |
| FR-AI-007 | FEAT-AI-002 | BR-002 | CAP-02, CAP-03, CAP-04 | — | T: category/color correction. |
| FR-AI-008 | FEAT-AI-002 | BR-002, BR-005 | CAP-02, CAP-03, CAP-04 | — | T: confirm versus draft. |
| FR-AI-009 | FEAT-AI-001, FEAT-AI-002 | BR-001, BR-002 | CAP-02, CAP-03, CAP-04 | — | T: canceled draft absent. |
| FR-AI-010 | FEAT-AI-002 | BR-005 | CAP-02, CAP-03, CAP-04 | — | T: successful/failed-save retry. |
| FR-AI-011 | FEAT-AI-001 | BR-001, BR-022 | CAP-02, CAP-04 | — | I/D: image-entry guidance. |
| FR-AI-012 | FEAT-AI-001, FEAT-AI-002 | BR-001, BR-004, BR-022 | CAP-02, CAP-03, CAP-04 | OSQ-001 (Resolved) | T: JPEG/JPG, PNG, HEIC/HEIF; at/beyond 15 MB; 511/512 pixels; fallback. |
| FR-GAR-001 | FEAT-GAR-001 | BR-003 | CAP-04 | — | T: defined categories only. |
| FR-GAR-002 | FEAT-GAR-001, FEAT-AI-002 | BR-003, BR-002 | CAP-02, CAP-03, CAP-04 | OSQ-005 (Resolved) | T: defined vocabularies and subtype fallback saving. |
| FR-GAR-003 | FEAT-AI-002, FEAT-GAR-001 | BR-002, BR-003 | CAP-02, CAP-03, CAP-04 | — | T: minimum-only profile. |
| FR-GAR-004 | FEAT-GAR-001, FEAT-OUT-001 | BR-003, BR-009 | CAP-04, CAP-05, CAP-06 | OSQ-005 (Resolved) | T/A: UNKNOWN versus known required fields; saveable/ready separation. |
| FR-GAR-005 | FEAT-GAR-001, FEAT-OUT-001 | BR-003, BR-009 | CAP-04, CAP-05, CAP-06 | OSQ-005 (Resolved) | T/A: per-category readiness; unknown fields; layering/bulk applicability. |
| FR-GAR-006 | FEAT-GAR-001 | BR-003 | CAP-04 | OSQ-005 (Resolved) | T: omitted optional fields versus required applicable bulk. |
| FR-GAR-007 | FEAT-GAR-001 | BR-003 | CAP-04 | OSQ-005 (Resolved) | T/I: every layer/fit value and 1–5 bulk bounds. |
| FR-GAR-008 | FEAT-GAR-001 | BR-003 | CAP-04 | OSQ-005 (Resolved) | T/I: all color-family/temperature/role values and unknown handling. |
| FR-GAR-009 | FEAT-GAR-001 | BR-003 | CAP-04 | OSQ-005 (Resolved) | T: SOLID/NONE; noise 1–5; climate values; preserved style/occasion codes. |
| FR-GAR-010 | FEAT-GAR-001 | BR-003 | CAP-04 | — | T: uncertain material case. |
| FR-GAR-011 | FEAT-GAR-001, FEAT-AI-002 | BR-002 | CAP-02, CAP-03, CAP-04 | — | T: analysis after correction. |
| FR-WAR-001 | FEAT-WAR-001 | BR-005 | CAP-03, CAP-07 | — | T: browse and imageless detail. |
| FR-WAR-002 | FEAT-WAR-001, FEAT-ANL-001 | BR-005, BR-006 | CAP-03, CAP-06, CAP-07 | — | I: readiness and usage labels. |
| FR-WAR-003 | FEAT-WAR-002 | BR-002, BR-005 | CAP-03, CAP-04 | — | T: edit/confirm/cancel. |
| FR-WAR-004 | FEAT-WAR-002 | BR-005 | CAP-03, CAP-04 | — | T: remove/confirm/cancel. |
| FR-WAR-005 | FEAT-WAR-002 | BR-005 | CAP-03, CAP-04 | — | T/A: post-removal evaluations. |
| FR-WAR-006 | FEAT-WAR-002 | BR-005 | CAP-03, CAP-04 | — | T: affected assessment state. |
| FR-WAR-007 | FEAT-WAR-003 | BR-005 | CAP-03 | — | T: match, filter, clear. |
| FR-WAR-008 | FEAT-WAR-001, FEAT-WAR-003 | BR-005 | CAP-03, CAP-07 | — | T: empty/no-match/failure states. |
| FR-OUT-001 | FEAT-OUT-001 | BR-007 | CAP-05, CAP-06 | OSQ-005, OSQ-006 (Resolved) | T/A: ownership/confirmation/readiness/candidate eligibility fixtures. |
| FR-OUT-002 | FEAT-OUT-001 | BR-007, BR-010 | CAP-05, CAP-06 | — | T/A: context variation. |
| FR-OUT-003 | FEAT-OUT-001 | BR-009 | CAP-05, CAP-06 | — | A: invalid high-preference fixture. |
| FR-OUT-004 | FEAT-OUT-001 | BR-007, BR-009 | CAP-05, CAP-06 | OSQ-005, OSQ-006 (Resolved) | A: allowed 3/4-item composition; invalid slots/layers/bulk/pattern boundaries. |
| FR-OUT-005 | FEAT-OUT-001 | BR-007, BR-009, BR-010, BR-011, BR-012 | CAP-05, CAP-06 | OSQ-006, OSQ-007 (Resolved) | A: preference differences among valid sets; no identity-based category rejection. |
| FR-OUT-006 | FEAT-OUT-001 | BR-008 | CAP-05, CAP-06 | — | T/A: three-valid fixture. |
| FR-OUT-007 | FEAT-OUT-001 | BR-008 | CAP-05, CAP-06 | — | A: reordered duplicates. |
| FR-OUT-008 | FEAT-OUT-001 | BR-008, BR-022 | CAP-05, CAP-06 | — | T: two, one, zero valid. |
| FR-OUT-009 | FEAT-OUT-001 | BR-007, BR-022 | CAP-05, CAP-06 | OSQ-006, OSQ-010 (Resolved) | T: attribute/context and applicable rule-version changes. |
| FR-OUT-010 | FEAT-OUT-002 | BR-007 | CAP-04, CAP-05 | — | D: outfit to garment detail. |
| FR-OUT-011 | FEAT-OUT-002 | BR-022 | CAP-04, CAP-05 | — | I/A: explanation matches fixture. |
| FR-OUT-012 | FEAT-OUT-002 | BR-022 | CAP-04, CAP-05 | — | I: unavailable-context reasoning. |
| FR-OUT-013 | FEAT-OUT-002 | BR-022 | CAP-04, CAP-05 | — | T/I: three presentation contexts. |
| FR-OUT-014 | FEAT-OUT-003 | BR-008 | CAP-05 | — | T: fixed other identities. |
| FR-OUT-015 | FEAT-OUT-003 | BR-007, BR-009 | CAP-05 | OSQ-006 (Resolved) | A: fixed-slot replacements at layer/bulk/environment/pattern limits. |
| FR-OUT-016 | FEAT-OUT-003 | BR-008, BR-022 | CAP-05 | — | T: no-alternative fixture. |
| FR-OUT-017 | FEAT-OUT-003 | BR-008 | CAP-05 | — | T: interaction isolation. |
| FR-PERS-001 | FEAT-PERS-001 | BR-011 | CAP-06 | OSQ-007 (Resolved) | T/A: exact target, Like +1 and age-dependent influence. |
| FR-PERS-002 | FEAT-PERS-001 | BR-011 | CAP-06 | OSQ-007 (Resolved) | T/A: Dislike −2; no automatic per-garment/category ban. |
| FR-PERS-003 | FEAT-PERS-001 | BR-011, BR-021 | CAP-06 | OSQ-007 (Resolved) | T: persisted state beyond 90 days, revise/clear; ranking aging separate. |
| FR-PERS-004 | FEAT-PERS-001 | BR-011 | CAP-06 | — | T: failed versus recorded signal. |
| FR-PERS-005 | FEAT-PERS-001 | BR-011 | CAP-06 | — | T: history unchanged. |
| FR-WEAR-001 | FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: new explicit initiation versus retries; accepted-action default time. |
| FR-WEAR-002 | FEAT-PERS-002 | BR-011 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: same-day different outfits. |
| FR-WEAR-003 | FEAT-PERS-002 | BR-011 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: separate same-outfit/same-day initiation creates two events. |
| FR-WEAR-004 | FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T/I: absolute/local correspondence and later timezone change. |
| FR-WEAR-005 | FEAT-PERS-002 | BR-011, BR-022 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: available/missing context. |
| FR-WEAR-006 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: outfit/context correction; future/cross-day rejection in original timezone. |
| FR-WEAR-007 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: individual removal. |
| FR-WEAR-008 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: underlying objects remain. |
| FR-WEAR-009 | FEAT-PERS-002 | BR-011 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: one logical action retried; separate explicit repeat; no date/outfit-only deduplication. |
| FR-WEAR-010 | FEAT-PERS-002, FEAT-ANL-001, FEAT-PERS-003 | BR-006, BR-011 | CAP-05, CAP-06, CAP-07 | OSQ-007, OSQ-008 (Resolved) | T/A: corrected outfit/time and removal change effective signals/history appropriately. |
| FR-WEAR-011 | FEAT-PERS-002, FEAT-PERS-003 | BR-011, BR-009 | CAP-05, CAP-06, CAP-07 | — | T/A: invalid/hypothetical attempts. |
| FR-WEAR-012 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T: cancel correction/removal; original event remains. |
| FR-PERS-006 | FEAT-PERS-003, FEAT-PROF-001 | BR-010 | CAP-01, CAP-05, CAP-06 | — | A: no-history preference fixture. |
| FR-PERS-007 | FEAT-PERS-003, FEAT-PERS-001 | BR-011 | CAP-05, CAP-06 | OSQ-007 (Resolved) | A: weights and 30/31/60/61/90/91-day aging; persisted state. |
| FR-PERS-008 | FEAT-PERS-003, FEAT-PERS-002 | BR-011 | CAP-05, CAP-06, CAP-07 | OSQ-007 (Resolved) | A: equal-age +2 versus +1; normalized intentional repeats. |
| FR-PERS-009 | FEAT-PERS-003, FEAT-ANL-001 | BR-011, BR-006 | CAP-05, CAP-06, CAP-07 | OSQ-007 (Resolved) | A: 2/3/7/8-day recency boundaries; optional ≥14-day boost safeguards. |
| FR-PERS-010 | FEAT-PERS-003, FEAT-PROF-001 | BR-012 | CAP-01, CAP-05, CAP-06 | — | T/A: optional context and eligibility. |
| FR-PERS-011 | FEAT-PERS-003, FEAT-OUT-001 | BR-009, BR-011 | CAP-05, CAP-06 | — | A: constrained/no-alternative fixture. |
| FR-PERS-012 | FEAT-PERS-003, FEAT-PERS-002 | BR-011 | CAP-05, CAP-06, CAP-07 | OSQ-007, OSQ-008 (Resolved) | A: normalized outfit/day versus distinct outfits/days; 100%/50%/25%/0% decay. |
| FR-PERS-013 | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002 | BR-011, BR-012 | CAP-05, CAP-06, CAP-07 | OSQ-007, OSQ-008 (Resolved) | T/A: clear/revise/remove; surviving same-day events; >90-day state retained. |
| FR-ANL-001 | FEAT-ANL-001, FEAT-PERS-002 | BR-006 | CAP-06, CAP-07 | OSQ-008 (Resolved) | T/I: original-local history after travel; accepted in-day correction. |
| FR-ANL-002 | FEAT-ANL-001, FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | — | T: multiple-event history. |
| FR-ANL-003 | FEAT-ANL-001 | BR-006, BR-011 | CAP-06, CAP-07 | OSQ-007, OSQ-008 (Resolved) | A: all legitimate reports versus normalized/expired ranking evidence; OSQ-011. |
| FR-ANL-004 | FEAT-ANL-001 | BR-006, BR-022 | CAP-06, CAP-07 | — | I: logging limitations. |
| FR-ANL-005 | FEAT-ANL-001 | BR-006, BR-022 | CAP-06, CAP-07 | — | T: empty history. |
| FR-ANL-006 | FEAT-ANL-001 | BR-006, BR-022 | CAP-06, CAP-07 | — | T/I: history after garment removal. |
| FR-ANL-007 | FEAT-ANL-001, FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | OSQ-007, OSQ-008 (Resolved) | T/A: correction/removal with another same-outfit/day event and historical snapshots. |
| FR-ANL-008 | FEAT-ANL-002, FEAT-PROF-001 | BR-014, BR-010 | CAP-01, CAP-06, CAP-07, CAP-08 | OSQ-006, OSQ-009 (Resolved) | A: six need mappings and unique outfit counts per need. |
| FR-ANL-009 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | OSQ-009 (Resolved) | I/A: visible assessment basis and disabled priority-zero needs. |
| FR-ANL-010 | FEAT-ANL-002, FEAT-PROF-001 | BR-014, BR-010 | CAP-01, CAP-06, CAP-07, CAP-08 | OSQ-009 (Resolved) | A: weights 0/1/2/3; zero excluded; controlled weighted comparison. |
| FR-ANL-011 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | OSQ-009 (Resolved) | A/T: exact weighted fixtures; no-active-need/missing-ready-role cases suppress number. |
| FR-ANL-012 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | OSQ-006, OSQ-009 (Resolved) | A: 0/1/2/3/4 valid outfits yield 0%/≈33%/≈67%/100%/100%. |
| FR-ANL-013 | FEAT-ANL-002 | BR-014 | CAP-07, CAP-08 | — | A: post-removal baseline. |
| FR-ANL-014 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | OSQ-006, OSQ-009 (Resolved) | T: changed priority, garment attributes and applicable rule version. |
| FR-ANL-015 | FEAT-ANL-002 | BR-014, BR-022, BR-024 | CAP-07, CAP-08 | OSQ-009 (Resolved) | T/I: insufficient versus valid zero; no universal completion claim. |
| FR-GAP-001 | FEAT-GAP-001 | BR-015 | CAP-07, CAP-08 | OSQ-009 (Resolved) | A: 0/1/2 versus ≥3 valid need outfits, priority-zero exclusion, capability-first evidence. |
| FR-GAP-002 | FEAT-GAP-001 | BR-015, BR-022 | CAP-07, CAP-08 | — | I: gap versus product wording. |
| FR-GAP-003 | FEAT-GAP-001 | BR-015 | CAP-07, CAP-08 | — | D: gap to utility evaluation. |
| FR-GAP-004 | FEAT-GAP-001 | BR-014, BR-022, BR-024 | CAP-07, CAP-08 | OSQ-009 (Resolved) | T: no-gap, incomplete and valid-zero distinction. |
| FR-GAP-005 | FEAT-GAP-001 | BR-024 | CAP-07, CAP-08 | — | D: commerce-independent path. |
| FR-MULT-001 | FEAT-MULT-001 | BR-015, BR-016 | CAP-08, CAP-09 | OSQ-005, OSQ-010 (Resolved) | A: candidate/owned readiness, removed baseline and hypothetical ownership. |
| FR-MULT-002 | FEAT-MULT-001 | BR-009, BR-016 | CAP-08, CAP-09 | OSQ-006, OSQ-010 (Resolved) | A: matched context/rule-version versus mismatched comparison. |
| FR-MULT-003 | FEAT-MULT-001 | BR-016 | CAP-08, CAP-09 | OSQ-010 (Resolved) | A: exact set-difference fixtures and identity permutations. |
| FR-MULT-004 | FEAT-MULT-001 | BR-016, BR-022 | CAP-08, CAP-09 | OSQ-010 (Resolved) | T/A: complete 41→58 gives +17; fail each exact-result prerequisite. |
| FR-MULT-005 | FEAT-MULT-001 | BR-017 | CAP-08, CAP-09 | OSQ-010 (Resolved) | T/I: newly enabled candidate previews; stale/incomplete previews not current exact evidence. |
| FR-MULT-006 | FEAT-MULT-001 | BR-015, BR-017 | CAP-08, CAP-09 | — | T: no ingestion/wear side effect. |
| FR-MULT-007 | FEAT-MULT-001 | BR-016, BR-022 | CAP-08, CAP-09 | OSQ-010 (Resolved) | T/A: all five states; failures/timeouts/missing data never become zero. |
| FR-MULT-008 | FEAT-MULT-001 | BR-016, BR-022 | CAP-08, CAP-09 | OSQ-010 (Resolved) | T: each input/rule trigger; unchanged inputs do not expire under an invented TTL. |
| FR-SHOP-001 | FEAT-SHOP-001 | BR-018, BR-020 | CAP-08, CAP-09, CAP-10 | — | I: required candidate information. |
| FR-SHOP-002 | FEAT-SHOP-001, FEAT-MULT-001 | BR-016, BR-017, BR-018 | CAP-08, CAP-09, CAP-10 | OSQ-010 (Resolved) | T/I: exact usable utility versus all non-exact evaluation states. |
| FR-SHOP-003 | FEAT-SHOP-001 | BR-020 | CAP-08, CAP-09, CAP-10 | — | I: credible and missing optional fields. |
| FR-SHOP-004 | FEAT-SHOP-001 | BR-020, BR-024 | CAP-08, CAP-09, CAP-10 | OSQ-010 (Resolved) | T/I: missing commercial data versus missing required utility data. |
| FR-SHOP-005 | FEAT-SHOP-002 | BR-018 | CAP-10 | — | T: available link handoff. |
| FR-SHOP-006 | FEAT-SHOP-002 | BR-018, BR-022 | CAP-10 | — | T: failed link and return. |
| FR-SHOP-007 | FEAT-SHOP-002 | BR-018, BR-019 | CAP-10 | — | T/I: navigation side effects. |
| FR-SHOP-008 | FEAT-SHOP-001, FEAT-SHOP-002 | BR-018, BR-024 | CAP-08, CAP-09, CAP-10 | — | D/I: independent core journey. |
| FR-MET-001 | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001 | BR-019, BR-001, BR-007 | CAP-02, CAP-04, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | — | T/I: distinct interaction evidence. |
| FR-MET-002 | FEAT-MET-001, FEAT-PERS-002 | BR-019, BR-011 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | — | T/I: event lifecycle evidence. |
| FR-MET-003 | FEAT-MET-001, FEAT-SHOP-002 | BR-019 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | — | T/I: exposure versus navigation. |
| FR-MET-004 | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001, FEAT-ANL-001 | BR-019, BR-001, BR-007, BR-006 | CAP-02, CAP-04, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | OSQ-001 (Resolved) | A/I: separate accuracy reports, sample durations/start-end boundaries and honest event outcomes. |
| FR-MET-005 | FEAT-MET-001, FEAT-PERS-002 | BR-019, BR-011 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | OSQ-007, OSQ-008 (Resolved) | T/A: two intentional history events versus one preference increment; retry counted once. |
| FR-MET-006 | FEAT-MET-001 | BR-021 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | — | I: measurement information. |
| FR-MET-007 | FEAT-MET-001 | BR-019 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | — | T: measurement dependency failure. |

### 10.3 PRD Feature Coverage Audit

All 22 MVP features remain covered. Functional coverage is exhaustive; supporting examples point to representative cross-cutting requirements.

| PRD Feature / Name | Functional Coverage | Supporting Examples | Result |
|---|---|---|---|
| FEAT-AUTH-001 — Account access and continuity | FR-AUTH-001–FR-AUTH-018; FR-PROF-001, FR-PROF-008 | DATA-AUTH-001, UI-002, SI-003, COM-001, NFR-SEC-001 | Covered — MVP |
| FEAT-PROF-001 — Preferences and optional profile | FR-AUTH-011; FR-PROF-001–FR-PROF-008; FR-PERS-006, FR-PERS-010; FR-ANL-008, FR-ANL-010 | DATA-AUTH-003, UI-001, NFR-PRIV-001, NFR-MNT-001, LOC-002 | Covered — MVP |
| FEAT-PROF-002 — Location/environmental context | FR-PROF-001; FR-WEATHER-001–FR-WEATHER-005 | SI-002, HW-003, NFR-PRIV-001, NFR-INT-001, ERR-WEATHER-001 | Covered — MVP |
| FEAT-AI-001 — Image entry and preview | FR-AI-001–FR-AI-002, FR-AI-004–FR-AI-006, FR-AI-009, FR-AI-011–FR-AI-012; FR-MET-001, FR-MET-004 | DATA-GAR-004, SI-001, HW-001, NFR-PERF-001, NFR-REL-001 | Covered — MVP |
| FEAT-AI-002 — Confirmation/manual entry | FR-AI-003, FR-AI-007–FR-AI-010, FR-AI-012; FR-GAR-002–FR-GAR-003, FR-GAR-011 | DATA-GAR-003, DATA-INT-001, UI-005, SI-001, HW-001 | Covered — MVP |
| FEAT-GAR-001 — Rich garment intelligence | FR-GAR-001–FR-GAR-011 | DATA-GAR-001, DATA-INT-001, UI-004, NFR-ACC-001, NFR-MNT-001 | Covered — MVP |
| FEAT-WAR-001 — Wardrobe overview/detail | FR-WAR-001–FR-WAR-002, FR-WAR-008 | DATA-GAR-001, UI-001, NFR-SEC-001, NFR-AVL-001, NFR-USE-001 | Covered — MVP |
| FEAT-WAR-002 — Edit/remove garments | FR-WAR-003–FR-WAR-006 | DATA-GAR-005, DATA-INT-002, DATA-RET-002 | Covered — MVP |
| FEAT-WAR-003 — Search/filter | FR-WAR-007–FR-WAR-008 | See defining FR rows. | Covered — MVP |
| FEAT-OUT-001 — Valid personalized choices | FR-PROF-004; FR-WEATHER-004–FR-WEATHER-005; FR-GAR-004–FR-GAR-005; FR-OUT-001–FR-OUT-009; FR-PERS-011; FR-MET-001, FR-MET-004 | DATA-OUT-001, UI-001, NFR-PERF-002, NFR-AVL-001, NFR-USE-001 | Covered — MVP |
| FEAT-OUT-002 — Outfit detail/reasoning | FR-OUT-010–FR-OUT-013 | DATA-OUT-002 | Covered — MVP |
| FEAT-OUT-003 — Single-item shuffle | FR-OUT-014–FR-OUT-017 | ERR-OUT-003 | Covered — MVP |
| FEAT-PERS-001 — Like/Dislike | FR-PERS-001–FR-PERS-005, FR-PERS-007, FR-PERS-013 | DATA-WEAR-001, NFR-PRIV-001 | Covered — MVP |
| FEAT-PERS-002 — Wear This Today / Wear Events | FR-WEAR-001–FR-WEAR-012; FR-PERS-008, FR-PERS-012–FR-PERS-013; FR-ANL-001–FR-ANL-002, FR-ANL-007; FR-MET-002, FR-MET-005 | DATA-WEAR-002, DATA-INT-004, DATA-RET-001, UI-007, COM-002 | Covered — MVP |
| FEAT-PERS-003 — Active personalization | FR-PROF-007; FR-WEAR-010–FR-WEAR-011; FR-PERS-006–FR-PERS-013 | See defining FR rows. | Covered — MVP |
| FEAT-ANL-001 — History/utilization | FR-WAR-002; FR-WEAR-010; FR-PERS-009; FR-ANL-001–FR-ANL-007; FR-MET-004 | DATA-WEAR-004, DATA-HIST-001, DATA-RET-001, UI-007, NFR-ACC-001 | Covered — MVP |
| FEAT-ANL-002 — Coverage Score | FR-ANL-008–FR-ANL-015 | DATA-ANL-001, DATA-INT-002, NFR-REL-002, NFR-TEST-001, ERR-ANL-001 | Covered — MVP |
| FEAT-GAP-001 — Capability-based gaps | FR-GAP-001–FR-GAP-005 | DATA-ANL-002, ERR-ANL-001 | Covered — MVP |
| FEAT-MULT-001 — Multiplier/previews | FR-MULT-001–FR-MULT-008; FR-SHOP-002 | DATA-OUT-001, DATA-ANL-003, DATA-INT-002, UI-008, NFR-REL-002 | Covered — MVP |
| FEAT-SHOP-001 — Utility-led candidate advice | FR-SHOP-001–FR-SHOP-004, FR-SHOP-008 | DATA-ANL-003, UI-009, ERR-SHOP-001 | Covered — MVP |
| FEAT-SHOP-002 — External shopping navigation | FR-SHOP-005–FR-SHOP-008; FR-MET-003 | DATA-INT-003, UI-009, SI-004, COM-003, NFR-PRIV-002 | Covered — MVP |
| FEAT-MET-001 — Product measurement | FR-MET-001–FR-MET-007 | NFR-PERF-001, NFR-SEC-003, NFR-PRIV-002, NFR-AVL-001, NFR-TEST-002 | Covered — MVP |

### 10.4 BRD Requirement Coverage Audit

BR-001–BR-024 remain accounted for. Conditional BR-020 information depends on credible sources and does not defer required candidate utility or weaken exact-evaluation prerequisites.

| BRD Requirement | Direct Functional Coverage | Result / Relevant Boundary |
|---|---|---|
| BR-001 | FR-AI-001–FR-AI-002, FR-AI-004–FR-AI-006, FR-AI-009, FR-AI-011–FR-AI-012; FR-MET-001, FR-MET-004 | Covered — direct software behavior. |
| BR-002 | FR-AI-007–FR-AI-009; FR-GAR-002–FR-GAR-003, FR-GAR-011; FR-WAR-003 | Covered — direct software behavior. |
| BR-003 | FR-AI-006; FR-GAR-001–FR-GAR-010 | Covered — direct software behavior. |
| BR-004 | FR-AI-003, FR-AI-012 | Covered — direct software behavior. |
| BR-005 | FR-AI-008, FR-AI-010; FR-WAR-001–FR-WAR-008 | Covered — direct software behavior. |
| BR-006 | FR-WAR-002; FR-WEAR-001, FR-WEAR-004, FR-WEAR-006–FR-WEAR-008, FR-WEAR-010, FR-WEAR-012; FR-PERS-009; FR-ANL-001–FR-ANL-007; FR-MET-004 | Covered — direct software behavior. |
| BR-007 | FR-WEATHER-003–FR-WEATHER-005; FR-OUT-001–FR-OUT-002, FR-OUT-004–FR-OUT-005, FR-OUT-009–FR-OUT-010, FR-OUT-015; FR-MET-001, FR-MET-004 | Covered — direct software behavior. |
| BR-008 | FR-OUT-006–FR-OUT-008, FR-OUT-014, FR-OUT-016–FR-OUT-017 | Covered — direct software behavior. |
| BR-009 | FR-GAR-004–FR-GAR-005; FR-OUT-003–FR-OUT-005, FR-OUT-015; FR-WEAR-011; FR-PERS-011; FR-MULT-002 | Covered — direct software behavior. |
| BR-010 | FR-PROF-001–FR-PROF-004; FR-OUT-002, FR-OUT-005; FR-PERS-006; FR-ANL-008, FR-ANL-010 | Covered — direct software behavior. |
| BR-011 | FR-OUT-005; FR-PERS-001–FR-PERS-005, FR-PERS-007–FR-PERS-009, FR-PERS-011–FR-PERS-013; FR-WEAR-001–FR-WEAR-005, FR-WEAR-009–FR-WEAR-011; FR-ANL-002–FR-ANL-003, FR-ANL-007; FR-MET-002, FR-MET-005 | Covered — direct software behavior. |
| BR-012 | FR-AUTH-011; FR-PROF-001, FR-PROF-005–FR-PROF-008; FR-OUT-005; FR-PERS-010, FR-PERS-013 | Covered — direct software behavior. |
| BR-013 | FR-PROF-001; FR-WEATHER-001–FR-WEATHER-003 | Covered — direct software behavior. |
| BR-014 | FR-PROF-003; FR-ANL-008–FR-ANL-015; FR-GAP-004 | Covered — direct software behavior. |
| BR-015 | FR-GAP-001–FR-GAP-003; FR-MULT-001, FR-MULT-006 | Covered — direct software behavior. |
| BR-016 | FR-MULT-001–FR-MULT-004, FR-MULT-007–FR-MULT-008; FR-SHOP-002 | Covered — direct software behavior. |
| BR-017 | FR-MULT-005–FR-MULT-006; FR-SHOP-002 | Covered — direct software behavior. |
| BR-018 | FR-SHOP-001–FR-SHOP-002, FR-SHOP-005–FR-SHOP-008 | Covered — direct software behavior. |
| BR-019 | FR-SHOP-007; FR-MET-001–FR-MET-005, FR-MET-007 | Covered — direct software behavior. |
| BR-020 | FR-SHOP-001, FR-SHOP-003–FR-SHOP-004 | Covered — credible optional information only. |
| BR-021 | FR-AUTH-001–FR-AUTH-018; FR-PROF-007–FR-PROF-008; FR-PERS-003; FR-WEAR-006–FR-WEAR-008, FR-WEAR-012; FR-MET-006 | Covered — direct software behavior. |
| BR-022 | FR-WEATHER-004–FR-WEATHER-005; FR-AI-005, FR-AI-011–FR-AI-012; FR-OUT-008–FR-OUT-009, FR-OUT-011–FR-OUT-013, FR-OUT-016; FR-WEAR-005; FR-ANL-004–FR-ANL-006, FR-ANL-009, FR-ANL-011–FR-ANL-012, FR-ANL-014–FR-ANL-015; FR-GAP-002, FR-GAP-004; FR-MULT-004, FR-MULT-007–FR-MULT-008; FR-SHOP-006 | Covered — direct software behavior. |
| BR-023 | FR-PROF-001 | Covered — Android/iOS and Vietnamese/localization also specified by LOC-* and Section 2. |
| BR-024 | FR-ANL-015; FR-GAP-004–FR-GAP-005; FR-SHOP-004, FR-SHOP-008 | Covered — core wardrobe value remains independent of commerce. |

### 10.5 Capability Continuity Audit

Capabilities retain BRD identity and current PRD feature links; feature coverage above supplies the related stable SRS requirements.

| Capability | Covered PRD Features | Result |
|---|---|---|
| CAP-01 — Account & Personalization | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002 | Covered |
| CAP-02 — AI Digital Closet | FEAT-AI-001, FEAT-AI-002, FEAT-MET-001 | Covered |
| CAP-03 — Wardrobe Management | FEAT-AI-002, FEAT-WAR-001, FEAT-WAR-002, FEAT-WAR-003 | Covered |
| CAP-04 — Garment Intelligence | FEAT-AI-001, FEAT-AI-002, FEAT-GAR-001, FEAT-WAR-002, FEAT-OUT-002 | Covered |
| CAP-05 — Context-Aware Styling | FEAT-PROF-002, FEAT-OUT-001, FEAT-OUT-002, FEAT-OUT-003, FEAT-PERS-003, FEAT-MET-001 | Covered |
| CAP-06 — Behavioral Personalization | FEAT-PROF-001, FEAT-OUT-001, FEAT-PERS-001, FEAT-PERS-002, FEAT-PERS-003, FEAT-ANL-001, FEAT-MET-001 | Covered |
| CAP-07 — Wardrobe Analytics | FEAT-PROF-002, FEAT-WAR-001, FEAT-PERS-002, FEAT-ANL-001, FEAT-ANL-002, FEAT-GAP-001, FEAT-MET-001 | Covered |
| CAP-08 — Gap Analysis | FEAT-ANL-002, FEAT-GAP-001, FEAT-MULT-001, FEAT-SHOP-001, FEAT-MET-001 | Covered |
| CAP-09 — Wardrobe Multiplier | FEAT-MULT-001, FEAT-SHOP-001, FEAT-MET-001 | Covered |
| CAP-10 — Strategic Shopping | FEAT-SHOP-001, FEAT-SHOP-002, FEAT-MET-001 | Covered |

Coverage totals: **147 functional requirements; 22/22 features; 24/24 business requirements; 10/10 capabilities**. The SRS contains 256 stable requirement IDs. BG-01–BG-04 retain the BRD mappings; verification remains planned evidence.

## 11. Glossary

| Term | Meaning |
|---|---|
| AI Suggested | Provenance of a proposed automated value that has not become authoritative merely through prediction. |
| User Confirmed | Provenance of a value explicitly reviewed/accepted by the user. |
| User Corrected | Provenance of a value changed by the user from a suggestion/prior value and confirmed. |
| User Entered | Provenance of manually supplied information; the entry becomes authoritative through confirmation. |
| High Confidence / Needs Review / Uncertain | User-facing prediction-confidence meanings; localized labels do not change these semantics. Confidence does not replace confirmation. |
| Garment | Individually distinguishable clothing item in one supported primary category; active ownership differs from removed historical representation. |
| Garment Profile | Authoritative user-confirmed category/color plus bounded classification, pattern/noise, layering/bulk/fit, environmental and color descriptors, optional rich information, and applicable provenance/uncertainty. |
| Saveable Garment | Entry with user-confirmed Primary Category and Dominant Color; neither an image nor every rich attribute is mandatory. |
| Recommendation-Ready Garment | Entry with known/justifiably derived required category/color/pattern/climate information, applicable TOP/OUTERWEAR layering and conditional layered bulk under Section 3.5.1. UNKNOWN required fields do not satisfy the affected decision; saving remains separate. |
| Outfit | Combination of identified garments evaluated under a context. Reordering the same identities does not create another unique outfit. |
| Outfit Recommendation | Valid outfit proposed from current owned garments with relevant context and understandable reasons. |
| Hard Validity Constraint | Prerequisite in Section 3.7.1: exact slot composition, active confirmed ownership/readiness, applicable layer/bulk/environment suitability and severe pattern/noise limits; enforced before ranking. |
| Soft Personalized Ranking | Preference-based ordering of already-valid candidates; it cannot convert an invalid outfit into a valid one. |
| Current Occasion | Occasion for a particular recommendation request, drawn from the defined occasion vocabulary. |
| Common Occasion Needs | Ongoing clothing needs/priorities informing the Personalized Everyday Capsule; distinct from current request occasion. |
| Feedback | Current outfit-targeted Like (+1) or Dislike (−2) state until changed/cleared/superseded, with age-weighted ranking influence under the 90-day policy; not a Wear Event or automatic garment ban. |
| Wear Event | One logical intentional wearing report with outfit, absolute timestamp, event timezone/offset and original local date/time, plus available context. Separate explicit repeats can coexist; correction stays non-future/in the original local day. |
| Wear History | Distinct accepted reports and historical snapshots in original event-local context, unaffected by later device timezone or expiry of ranking influence, subject to retention policy. Logging remains user-reported/incomplete. |
| Accidental Duplicate | Retry/delivery/reprocessing of the same logical Wear intention; it does not create another event. User+outfit+date alone does not identify duplicates, and preference normalization is separate. |
| Removed from wardrobe | Historical indication that the garment is no longer current owned inventory; it is excluded from new/current calculations. |
| Personalized Everyday Capsule | One contextual model informed by common need priorities, style, climate/context, and current confirmed wardrobe. |
| Wardrobe Coverage Score | Weighted percentage from Section 3.14.1, using positive priorities and per-need ratios only after sufficient ready wardrobe/context information; contextual, not universal completeness. |
| Wardrobe Gap | Capability/bottleneck identified from a relevant positive-priority need below full Need Coverage, with evidence before a specific candidate; no compulsory purchase is implied. |
| Candidate Garment | Potential addition evaluated hypothetically; evaluation, preview, or link opening does not establish ownership. |
| Wardrobe Multiplier | Cardinality of the expanded unique-valid outfit set minus existing combinations under identical context/rules/version; exact +N depends on completed ready-input evaluation. |
| Hypothetical Outfit | Preview combining the unowned candidate with current garments, clearly distinguished from a wearable owned-outfit recommendation. |
| Strategic Shopping | Utility-led candidate advice grounded in a gap, incremental outfits, reasons, and previews; commercial fields/destinations are optional when credible. |
| Outdated Assessment | Result whose relevant wardrobe/garment/candidate/profile/context or validity rules changed since evaluation; prior results cannot be current without reevaluation. |
| Refresh Session | A login-session authorization context with maximum 30-day lifetime and rotating Refresh Token; independent devices may have separate sessions. Reuse revokes the affected session; password reset revokes all account Refresh Sessions. |
| Logical Wear Action | One explicit intention to report a wearing instance, unchanged by retry/delivery/reprocessing. A new explicit initiation is a new action. |
| Environmental Band | Active temperature category: HOT ≥30°C; WARM 24–<30°C; MILD 18–<24°C; COOL 12–<18°C; COLD <12°C. Missing weather is disclosed and handled through remaining validity. |
| Visual Noise | The 1–5 visual-complexity scale; ≥4 or HIGH density makes a TOP/BOTTOM/OUTERWEAR item severe for the at-most-one conflict count. |
| Need Coverage | Ratio min(ValidDistinctOutfitsForNeed / 3, 1), displayed as a percentage for a mapped relevant capsule need. |
| Coverage Score | Synonym for the Wardrobe Coverage Score: 100 × priority-weighted sum of Need Coverage divided by total positive priority weight. |
| Exact Multiplier | Completed current same-context/rule-version comparison satisfying all readiness/context/deduplication prerequisites; may show exact +N. |
| Evaluated Zero | Complete exact evaluation with an empty newly enabled outfit set; only this establishes +0 New Outfits. |
| Incomplete Multiplier | Required attributes/owned readiness or reliable full evaluation are insufficient; no exact +N. |
| Unavailable Multiplier | Evaluation capability cannot operate; no exact +N or false +0. |
| Outdated Multiplier | Relevant inputs/rules changed after assessment; reevaluation is required for current exact presentation, without an arbitrary TTL. |

## 12. Software Decisions and Open Questions

### 12.1 Resolved Software Decisions

OSQ-001–OSQ-010 were resolved on 2026-10-05 and are reflected in the applicable requirements and criteria throughout this SRS. The following table records their resolution summaries and affected requirements. Sections 3, 4, 6, and 8 contain the normative details; the SRS remains Baseline Draft.

| ID | Resolved Decision Summary | Status | Affected Requirements |
|---|---|---|---|
| OSQ-001 | JPEG/JPG, PNG and HEIC/HEIF; ≤15 MB and shortest side ≥512 pixels; locked ≥200 qualified images (≥50/category), separate ≥90% overall category/color and ≥85% category floor; usable-preview criteria; processing p95 ≤5 seconds and first outfit result p95 <3 seconds on 100 confirmed garments. Reference operating configuration remains OSQ-013. | Resolved | FR-AI-001–FR-AI-002, FR-AI-006, FR-AI-012; FR-MET-004; NFR-PERF-001–NFR-PERF-002; NFR-USE-002; NFR-TEST-002; NFR-SCA-001; AI-REQ-010–AI-REQ-011, AI-REQ-014; ERR-AI-001 |
| OSQ-002 | 15-minute JWT Access Token; maximum 30-day Refresh Session; consume/rotate refresh, session-revoking reuse detection, current-session logout, separate-session isolation and all-Refresh-Session revocation/reauthentication after reset. | Resolved | FR-AUTH-004–FR-AUTH-010, FR-AUTH-013–FR-AUTH-014; DATA-AUTH-001, DATA-AUTH-005; COM-001–COM-002; NFR-SEC-002–NFR-SEC-003; ERR-AUTH-001, ERR-AUTH-003 |
| OSQ-003 | 12–128-character untransformed passwords without mandatory class mixtures; normalized unique email and useful conflict guidance; account-safe recovery wording; single-use 30-minute reset; successfully issuing a new reset interaction invalidates earlier unused interactions (FR-AUTH-018). | Resolved | FR-AUTH-001–FR-AUTH-004, FR-AUTH-015–FR-AUTH-018; DATA-AUTH-001–DATA-AUTH-002, DATA-AUTH-005; SI-003; COM-001–COM-002; NFR-SEC-003; ERR-AUTH-001–ERR-AUTH-002 |
| OSQ-004 | Default ≥0.85 High / ≥0.60–<0.85 Needs Review / <0.60 Uncertain; evidence/quality guards; only stricter validated calibration; user authority and non-primary numeric presentation preserved. | Resolved | DATA-GAR-003; UI-005; AI-REQ-001–AI-REQ-002, AI-REQ-004–AI-REQ-007, AI-REQ-012–AI-REQ-013; ERR-AI-002 |
| OSQ-005 | Bounded pattern/density/noise/layer/bulk/fit/climate/color descriptors; all-category common readiness, TOP/OUTERWEAR layering and conditional layered bulk; saveability and non-universal rich fields preserved. | Resolved | FR-GAR-002, FR-GAR-004–FR-GAR-009; FR-OUT-001, FR-OUT-004; FR-MULT-001; DATA-GAR-002–DATA-GAR-003, DATA-GAR-005; DATA-ANL-003; AI-REQ-011; ERR-GAR-002; ERR-OUT-001; ERR-SHOP-001 |
| OSQ-006 | Exactly TOP+BOTTOM+FOOTWEAR, optional one OUTERWEAR; active confirmed ready ownership; layer/bulk/environment/severe-pattern validity and missing-weather fallback before soft color/style/occasion/behavior/body/gender ranking. | Resolved | FR-OUT-001, FR-OUT-004–FR-OUT-005, FR-OUT-009, FR-OUT-015; FR-ANL-008, FR-ANL-012, FR-ANL-014; FR-MULT-002; DATA-OUT-001–DATA-OUT-002; DATA-ANL-001; NFR-REL-002; ERR-WEATHER-001; ERR-OUT-001, ERR-OUT-003 |
| OSQ-007 | Like +1, Dislike −2, Wear +2; 90-day 100%/50%/25%/0% decay; one Wear increment per exact outfit/local day; strong ≤2-day/mild 3–7-day/no >7-day recency penalty; optional ≥14-day overlooked boost; feedback state persists until user action. | Resolved | FR-OUT-005; FR-PERS-001–FR-PERS-003, FR-PERS-007–FR-PERS-009, FR-PERS-012–FR-PERS-013; FR-WEAR-010; FR-ANL-003, FR-ANL-007; FR-MET-005; DATA-OUT-001; DATA-WEAR-001, DATA-WEAR-003–DATA-WEAR-004; DATA-HIST-001; DATA-RET-001–DATA-RET-002; ERR-WEAR-001 |
| OSQ-008 | One event per logical intention; new explicit initiation remains separate; absolute timestamp, original timezone/offset and local date/time; no travel re-dating; correction of applicable outfit/context/time is non-future and within original local day; individual removal preserves underlying objects. | Resolved | FR-WEAR-001–FR-WEAR-010, FR-WEAR-012; FR-PERS-012–FR-PERS-013; FR-ANL-001, FR-ANL-003, FR-ANL-007; FR-MET-005; DATA-WEAR-002–DATA-WEAR-004; DATA-INT-004; DATA-HIST-001–DATA-HIST-002; DATA-RET-001–DATA-RET-002; UI-007–UI-008; COM-002; LOC-005; ERR-WEAR-001–ERR-WEAR-002 |
| OSQ-009 | Six-need occasion mapping and priorities 0/1/2/3; per-need valid-count/3 capped at 1; weighted 0–100 score after positive-need/ready TOP+BOTTOM+FOOTWEAR sufficiency, plus OUTERWEAR only when required; incomplete is not false zero; capability gap precedes product. | Resolved | FR-PROF-003; FR-ANL-008–FR-ANL-012, FR-ANL-014–FR-ANL-015; FR-GAP-001, FR-GAP-004; DATA-ANL-001–DATA-ANL-002; NFR-REL-002; ERR-ANL-001–ERR-ANL-002 |
| OSQ-010 | Same-context/rule/version identity-based set difference; Exact/Evaluated Zero/Incomplete/Unavailable/Outdated; exact completeness gates; zero only for completed empty new set; freshness follows changed inputs/rules, without arbitrary TTL. | Resolved | FR-OUT-009; FR-MULT-001–FR-MULT-005, FR-MULT-007–FR-MULT-008; FR-SHOP-002, FR-SHOP-004; DATA-OUT-001–DATA-OUT-002; DATA-ANL-003–DATA-ANL-004; UI-003, UI-008; NFR-REL-002; ERR-ANL-001–ERR-ANL-002; ERR-SHOP-001–ERR-SHOP-002 |

### 12.2 Remaining Open Software Questions

Only OSQ-011–OSQ-014 remain open. Retention/consent, external-information freshness/credibility, operating/test configuration and usability/accessibility criteria do not reopen settled formulas, lifetimes, vocabularies or event/domain policies.

| ID | Question | Why It Matters | Responsible Decision Area | Must Be Resolved Before |
|---|---|---|---|---|
| OSQ-011 | What consent, authorized access/deletion/sharing boundaries and retention periods apply to images, profiles, behavior, historical snapshots, and measurement information? | PRD Section 23 and BRD Section 24 defer detailed policy; no unrestricted training permission or legal period is established. Preserve effective individual Wear removal and understandable removed-garment history. | Product Owner + privacy/legal review + Engineering/security | Final privacy/data acceptance and any binding legal/security architecture constraints. |
| OSQ-012 | What weather freshness, dependency outcome/time-limit criteria, candidate-information credibility/qualification, and known destination-unavailability rules define usable versus unavailable external information? | PRD Sections 11.2/20–21/25 defer provider freshness/credible-source/operating details. No merchant partnership, guaranteed stock, or retailer-response control is assumed. | Product Owner + Engineering + QA | Interface/failure acceptance; relevant Business Rules and later provider decisions. |
| OSQ-013 | Which Android/iOS versions/devices and nominal operating conditions are supported, and what availability, recovery, capacity/concurrency, or larger-workload targets are required if any? | BRD DEP-006 and PRD Section 25 establish mobile/reliable-use direction but no OS matrix, service percentage, recovery interval, or concurrency value. | Engineering + QA + Product Owner | Final operating-quality acceptance and later Quality Attribute Analysis/ASR. |
| OSQ-014 | What representative-user tasks, usability criteria, and Android/iOS assistive-interaction scenarios define acceptance of understandable/inclusive core journeys? | BRD stakeholder usability/accessibility expectations and PRD Section 25 supply direction without a conformance level or quantified evaluation threshold. | UX/Product + QA + Product Owner | Final usability/accessibility acceptance; later applicable quality scenarios. |

OSQ-013 reference configuration includes the devices/network/nominal conditions for the specified percentile timings; it must not substitute different thresholds or a different benchmark workload. Privacy retention, weather freshness and accessibility acceptance remain governed by their own open IDs. Signing, storage and revocation implementation remain downstream architecture decisions.

Answers must preserve stable requirement IDs and source intent. Any newly discovered business/product change follows upstream change control.

## 13. SRS Exit Criteria

This v0.2.1 Baseline Draft is ready for baseline review and Business Rules derivation when the Product Owner, with Requirements, Engineering, QA and relevant UX/AI/security review, confirms:

- All 22 MVP features, BR-001–BR-024 and CAP-01–CAP-10 remain covered and source-linked; existing stable SRS IDs are preserved.
- OSQ-001–OSQ-010 are Resolved and consistently propagated into functional/data/interface/quality/AI/error requirements, glossary and verification.
- Recognition qualification/locked-set/accuracy, preview criteria, timing boundaries/p95 targets and workload are clear; reference device/network configuration remains OSQ-013.
- Access/session/password/email/reset policy is defined at requirement level without selecting signing, storage or revocation implementation.
- Readiness/vocabularies, hard validity, personalization, logical Wear actions/time/correction, contextual Coverage formulas/gaps and Multiplier completeness/freshness are sufficiently defined to derive Business Rules.
- Major failure/recovery and ownership/history/effective-removal outcomes remain explicit; optional shopping information/permissions do not become core access gates.
- Specified rules and formulas refine previously deferred software details without changing BRD/PRD scope, personas, feature IDs, or product meaning.
- OSQ-011–OSQ-014 remain explicit privacy/external/operating/usability refinement items with their acceptance gates; no unresolved domain-policy question blocks `business-rules.md`.
- Traceability/reverse audits and planned verification are complete; mandatory requirements and optional behavior use consistent normative language.
- Physical schemas, implementation topology, formal behavioral models and architecture tactics remain downstream.

This document remains Baseline Draft and does not record formal document approval or passed software acceptance. Business Rules will formalize named invariants, edge cases, and executable examples for the specified rules. Remaining open acceptance criteria must be addressed by their recorded gates, with revisions preserving identity and traceability.

## 14. Next Artifact

The next artifact remains **`docs/03-requirements/business-rules.md`**, following Workflow Section 9. It will formalize the resolved garment-readiness/descriptor invariants, outfit validity, personalization contributions/decay/normalization, logical Wear/time/correction/removal, Coverage formulas/need mappings/gaps and Multiplier uniqueness/completeness/freshness into named reusable rules, decision tables, edge cases, examples and precedence with stable SRS/FEAT/BR/CAP references.

The SRS defines required software behavior and acceptance criteria. Business Rules will formalize reusable domain rules, invariants, formulas, rule precedence, decision tables, edge cases, and examples. Formal Use Cases then refine actor–system interactions; Activity Diagrams model workflow behavior using PlantUML. Quality Attribute Analysis later examines measurable scenarios and architectural significance, with ASR/ADD/ADR downstream.

Business Rules, formal behavioral models, quality analysis, architecture artifacts, the backlog, and User Stories are separate artifacts produced in workflow order.
