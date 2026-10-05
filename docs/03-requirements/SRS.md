# CapsuleAI — Software Requirements Specification

## 1. Introduction

| Field | Value |
|---|---|
| Document | CapsuleAI — Software Requirements Specification |
| Version / Status | 0.1 / Baseline Draft |
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

### 1.1 Purpose

This document specifies what CapsuleAI software shall do to implement the current business and product decisions. It provides a requirements baseline for Engineering, QA, Business Rules, and later behavioral analysis. The requirements describe observable outcomes rather than implementation tactics.

The upstream documents remain Baseline Draft, while their explicit business/product decisions are authoritative for this task. This SRS does not assert that any document has completed formal acceptance or that the software has passed the listed verification methods.

### 1.2 Scope

The initial system supports a Vietnamese mobile experience on Android and iOS: private account access; progressive context setup; assisted/manual wardrobe creation; confirmed garment intelligence; wardrobe management; validity-first personalized outfits; feedback and multiple user-reported Wear Events; utilization and contextual coverage; capability-based gaps; and candidate utility with hypothetical previews and optional external shopping.

The initial categories are TOP, BOTTOM, OUTERWEAR, and FOOTWEAR. Consumer web/desktop, extra garment categories, social/guest access, native commerce, automatic wear/purchase verification, advanced learned ranking as a prerequisite, social/community, AR/3D/video ingestion, and guaranteed offline operation are excluded. Additional interface languages are future-only.

### 1.3 Document Conventions

- **Shall** denotes a normative system obligation. Descriptive context, stimulus/response tables, glossary entries, and exit criteria explain requirements without creating separate requirement IDs.
- **MVP** denotes required initial behavior. **Conditional** denotes an obligation when its stated condition applies, not permission to omit a core capability. Credible optional shopping information remains conditional under BR-020.
- **TBD** explicitly identifies an unsettled software acceptance parameter, linked to an OSQ-* in Section 12. It does not reopen a resolved business/product decision.
- Verification codes: **T** behavioral/integration test; **A** analysis of controlled fixtures/results; **I** inspection of content, information, or constraints; **D** demonstration of an end-to-end journey. The scenario following the code states the evidence sought.
- Controlled fixtures must define their expected valid outfits, context, ownership, and confirmed information. Detailed rule-dependent expected results must be agreed in Business Rules before those tests become final acceptance evidence.

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
| OSQ-* | Open software questions, not requirements or reopened OPQ decisions. |

IDs are stable after assignment. Requirement statements may be refined through review while preserving identity and source links. No future-only requirement is added merely to fill a family.

### 1.5 Source Documents and Authority

Precedence is current approved BRD decisions → current approved PRD decisions → workflow for process/boundaries → other current CapsuleAI sources → older/exploratory sources. The Food Delivery document is used only as a structural reference.

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

The initial environment is connected Android/iOS mobile use in Vietnam with a Vietnamese interface. Camera/photo-library/location capabilities are used only with applicable permission and alternatives. No minimum OS version, hardware quota, deployment platform, simultaneous-user count, or multi-language UI obligation is approved.

Supported device/OS versions and reference test environments remain OSQ-013. A denied permission is distinct from unavailable network/service operation.

### 2.4 System Constraints

| Constraint | Required Boundary / Source |
|---|---|
| Platform and locale | Android/iOS; Vietnamese MVP UI and English documentation; localization-ready domain meaning (BR-023; OPQ-002). |
| Taxonomy | Four primary categories and bounded PRD subtypes; OTHER/UNKNOWN subtype fallback does not add primary categories (OPQ-003). |
| Authority and completeness | Confirmed category/dominant color permit saving; manual creation requires no image. Readiness is distinct and must not fabricate missing information (OPQ-004/OPQ-005). |
| Authentication | Email/password and email reset/logout; approved JWT Access Token with shorter lifetime than Refresh Token. Actual lifetimes and security policies are TBD (OPQ-008; OSQ-002/OSQ-003). |
| Recommendation and learning | Hard validity precedes soft ranking; explicit style/occasion and meaningful behavior are active. Advanced learned models are not required (BR-009–BR-012). |
| Wear and history | Multiple intentional events per local day, including repeat outfit uses; individual correction/removal, duplicate protection, and understandable removed-garment history (OPQ-006/OPQ-007). |
| Coverage and utility | One Personalized Everyday Capsule; contextual coverage; multiplier is incremental unique valid outfits under the same context (BR-014–BR-017). |
| Commerce | Required candidate utility and credible optional commercial information; external navigation only; no transaction workflow (BR-018–BR-020, BR-024). |

The approved JWT access/refresh direction constrains authentication; remaining implementation mechanisms are deferred to architecture.

### 2.5 Assumptions

BRD ASM-001–ASM-007 remain unvalidated: users will maintain useful inventory and correct predictions; compatible garments and contextual information can support relevant advice; explicit rules/lightweight ranking can provide value; explanation aids evaluation; and credible curated candidates can support initial validation. Requirements must handle failure of these assumptions through partial-data and no-useful-result states rather than inventing evidence.

### 2.6 Dependencies

| BRD Dependency | Requirement-Level Effect |
|---|---|
| DEP-001 — images | Usable images support assistance; manual entry remains available without an image. |
| DEP-002 — analysis | Availability/quality affects proposals, not user authority or manual saving. |
| DEP-003 — environmental context | Relevant location/weather supports assessment; missing/stale context is disclosed. |
| DEP-004 — confirmed inventory/context | Validity, coverage, and utility require adequate applicable information. |
| DEP-005 — candidate information/destinations | Candidate utility must be credible; optional links/commercial fields cannot gate core value. |
| DEP-006 — mobile/private handling | Authorized information and usable mobile access support participation. |

Reference conditions, provider freshness, and quantitative operating limits are explicit questions in Section 12, not assumed guarantees.

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

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-AUTH-001 | The system shall accept registration using Email, Password, and matching Confirm Password without requiring Display Name. | MVP | FEAT-AUTH-001; BR-021 | T: registration and mismatch cases. |
| FR-AUTH-002 | The system shall authenticate login using Email and Password for the corresponding account. | MVP | FEAT-AUTH-001; BR-021 | T: correct/incorrect credentials. |
| FR-AUTH-003 | The system shall provide Forgot Password initiation for email-based password reset. | MVP | FEAT-AUTH-001; BR-021 | T: reset initiation and email handoff. |
| FR-AUTH-004 | The system shall allow a valid email recovery interaction to establish a new password usable for subsequent login. | MVP | FEAT-AUTH-001; BR-021 | T: valid reset then new-password login. |
| FR-AUTH-005 | The system shall maintain access to the correct user's information through ordinary navigation/return visits while authentication remains valid. | MVP | FEAT-AUTH-001; BR-021 | T: valid-session return. |
| FR-AUTH-006 | The system shall require renewed authentication when access is no longer valid, rather than representing that user's wardrobe as empty. | MVP | FEAT-AUTH-001; BR-021 | T: expired-access scenario. |
| FR-AUTH-007 | The system shall end authenticated personal access on explicit logout. | MVP | FEAT-AUTH-001; BR-021 | T: protected action after logout. |
| FR-AUTH-008 | The system shall restrict protected personal operations to an authenticated user authorized for the affected information. | MVP | FEAT-AUTH-001; BR-021 | T: signed-out/cross-user denial. |
| FR-AUTH-009 | The system shall use a valid JWT Access Token to authenticate protected system operations. | MVP | FEAT-AUTH-001; BR-021 | T/I: absent, invalid, valid JWT access. |
| FR-AUTH-010 | The system shall support a short-lived JWT Access Token and longer-lived Refresh Token for authenticated-access renewal according to the security policy, without treating renewal as a grant of another user's permissions. | MVP | FEAT-AUTH-001; BR-021 | T/I: token roles; OSQ-002 policy. |
| FR-AUTH-011 | The system shall allow authenticated product entry without body/gender, device-location permission, first garment, or Display Name. | MVP | FEAT-AUTH-001, FEAT-PROF-001; BR-012, BR-021 | T: omitted optional information. |
| FR-AUTH-012 | The system shall explain missing registration Email/Password/Confirm Password or a password-confirmation mismatch and allow correction without reporting successful account creation. | MVP | FEAT-AUTH-001; BR-021 | T: each required field missing and mismatch. |

### 3.2 Profile & Personalization

JRN-01 and OPQ-001/OPQ-003/OPQ-009 define context. Long-term needs and the current request occasion are distinct.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-PROF-001 | The system shall provide the progressive sequence Welcome → Register/Login → Style Preferences → Common Occasion Needs → Optional Personal Context → Location → recommended First Garment → Today/Wardrobe. | MVP | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002; BR-010, BR-012, BR-013, BR-023 | D: first-use journey. |
| FR-PROF-002 | The system shall allow users to select and revise style preferences from the controlled vocabulary in Section 4.10. | MVP | FEAT-PROF-001; BR-010 | T: every approved style choice. |
| FR-PROF-003 | The system shall allow users to set and revise common occasion needs/priorities within the Personalized Everyday Capsule dimensions in Section 4.10. | MVP | FEAT-PROF-001; BR-010, BR-014 | T: common-needs priority changes. |
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

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-AI-001 | The system shall allow a permitted camera capture as garment-entry input. | MVP | FEAT-AI-001; BR-001 | T: capture to selected-image state. |
| FR-AI-002 | The system shall allow a permitted photo-library selection as garment-entry input. | MVP | FEAT-AI-001; BR-001 | T: gallery to selected-image state. |
| FR-AI-003 | The system shall provide manual garment entry without requiring an image or successful automated analysis. | MVP | FEAT-AI-002; BR-004 | T: imageless/manual completion. |
| FR-AI-004 | The system shall allow replacement or cancellation of the selected image before garment confirmation. | MVP | FEAT-AI-001; BR-001 | T: replace/cancel selected image. |
| FR-AI-005 | The system shall display a processing state while analysis is pending without reporting a completed wardrobe addition. | MVP | FEAT-AI-001; BR-001, BR-022 | T: delayed analysis state. |
| FR-AI-006 | The system shall present an available processed garment preview and proposed information for user review. | Conditional | FEAT-AI-001; BR-001, BR-003 | T: successful analysis output. |
| FR-AI-007 | The system shall allow the user to accept, correct, or manually supply proposed garment information before confirmation. | MVP | FEAT-AI-002; BR-002 | T: category/color correction. |
| FR-AI-008 | The system shall create a confirmed wardrobe entry only after explicit user confirmation of the saveable minimum. | MVP | FEAT-AI-002; BR-002, BR-005 | T: confirm versus draft. |
| FR-AI-009 | The system shall leave no confirmed entry from canceled preconfirmation work. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002 | T: canceled draft absent. |
| FR-AI-010 | The system shall acknowledge a successful addition distinctly from pending/failed work without manufacturing duplicate entries on retry. | MVP | FEAT-AI-002; BR-005 | T: successful/failed-save retry. |
| FR-AI-011 | The system shall provide capture/selection guidance about lighting, garment visibility, and avoiding confusing backgrounds. | MVP | FEAT-AI-001; BR-001, BR-022 | I/D: image-entry guidance. |

### 3.5 Garment Intelligence

OPQ-003–OPQ-005 settle vocabulary, saveability, and confidence. Exact readiness and remaining descriptor validation are OSQ-005; optional rich fields are not save blockers. AI-REQ-004–AI-REQ-007 and AI-REQ-012/AI-REQ-013 specify confidence/provenance behavior.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-GAR-001 | The system shall support primary classification limited to TOP, BOTTOM, OUTERWEAR, and FOOTWEAR. | MVP | FEAT-GAR-001; BR-003 | T: approved categories only. |
| FR-GAR-002 | The system shall offer the bounded category-specific subtypes in Section 4.10, including OTHER and UNKNOWN without blocking otherwise saveable entries. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-003, BR-002 | T: subtype fallback saves. |
| FR-GAR-003 | The system shall accept saving when the user has confirmed Primary Category and Dominant Color without requiring the remaining rich descriptors. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003 | T: minimum-only profile. |
| FR-GAR-004 | The system shall distinguish saveable ownership from recommendation readiness based on sufficient applicable compatibility information. | MVP | FEAT-GAR-001, FEAT-OUT-001; BR-003, BR-009 | T/A: incomplete versus ready fixtures. |
| FR-GAR-005 | The system shall use or justifiably derive Primary Category, Dominant Color, Pattern Type, applicable Layering Level, and Season Suitability for readiness assessment without silently inventing missing values. | MVP | FEAT-GAR-001, FEAT-OUT-001; BR-003, BR-009 | A: readiness fixtures; OSQ-005. |
| FR-GAR-006 | The system shall treat Subtype as strongly recommended, Bulk Index/Fit as useful but not universally mandatory, and Material/Silhouette/Secondary Colors/precise HEX-HSL display/Style Tags/Occasion Tags as optional for readiness. | MVP | FEAT-GAR-001; BR-003 | T: optional-descriptor cases. |
| FR-GAR-007 | The system shall provide review/correction of layering role, relative bulk, fit, and silhouette through understandable descriptors. | MVP | FEAT-GAR-001; BR-003 | T/I: shape/layer corrections. |
| FR-GAR-008 | The system shall provide review/correction of dominant/secondary color, color temperature and palette role, with conceptual HEX/HSL support without mandatory coordinate entry. | MVP | FEAT-GAR-001; BR-003 | T/I: color review. |
| FR-GAR-009 | The system shall provide review/correction of pattern type/density/visual noise and season/occasion/style associations. | MVP | FEAT-GAR-001; BR-003 | T/I: pattern/context corrections. |
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

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-OUT-001 | The system shall base current daily outfit evaluation on confirmed active owned garments and sufficient applicable compatibility information. | MVP | FEAT-OUT-001; BR-007 | T/A: owned/ready fixture. |
| FR-OUT-002 | The system shall evaluate available environmental/season context, current occasion, declared style, and relevant personalization context. | MVP | FEAT-OUT-001; BR-007, BR-010 | T/A: context variation. |
| FR-OUT-003 | The system shall exclude combinations that fail applicable hard validity constraints before personalized ranking. | MVP | FEAT-OUT-001; BR-009 | A: invalid high-preference fixture. |
| FR-OUT-004 | The system shall generate valid outfit candidates for required roles, layering, weather/season, and strong compatibility/pattern-noise constraints according to the agreed domain rules. | MVP | FEAT-OUT-001; BR-007, BR-009 | A: domain fixtures; OSQ-006. |
| FR-OUT-005 | The system shall rank only valid candidates using relevant color/style/occasion/preferences/history/diversity/recency and permitted soft personal context. | MVP | FEAT-OUT-001; BR-007, BR-009, BR-010, BR-011, BR-012 | A: controlled ranking differences. |
| FR-OUT-006 | The system shall present at least three distinct valid outfits when that many exist under the current wardrobe/context. | MVP | FEAT-OUT-001; BR-008 | T/A: three-valid fixture. |
| FR-OUT-007 | The system shall count a reordered combination of the same garment identities as the same outfit rather than a distinct option. | MVP | FEAT-OUT-001; BR-008 | A: reordered duplicates. |
| FR-OUT-008 | The system shall show the actual fewer-valid/no-valid result with limitations instead of duplicate padding or invalid alternatives. | MVP | FEAT-OUT-001; BR-008, BR-022 | T: two, one, zero valid. |
| FR-OUT-009 | The system shall reevaluate or clearly mark advice outdated after relevant wardrobe/context changes and never claim a failed refresh produced current advice. | MVP | FEAT-OUT-001; BR-007, BR-022 | T: stale/failed refresh. |

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
| FR-OUT-015 | The system shall offer a replacement only if the resulting outfit satisfies the same active validity context. | MVP | FEAT-OUT-003; BR-007, BR-009 | A: compatible replacement. |
| FR-OUT-016 | The system shall retain the current selection and explain the limitation when no compatible alternative exists. | MVP | FEAT-OUT-003; BR-008, BR-022 | T: no-alternative fixture. |
| FR-OUT-017 | The system shall avoid recording Like, Dislike, or a Wear Event solely because Shuffle was used. | MVP | FEAT-OUT-003; BR-008 | T: interaction isolation. |


### 3.10 Like / Dislike

Preference feedback is not reported wear and is reversible as specified by FEAT-PERS-001.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-PERS-001 | The system shall record Like as positive feedback on the identified outfit recommendation. | MVP | FEAT-PERS-001; BR-011 | T: Like state. |
| FR-PERS-002 | The system shall record Dislike as negative feedback on the identified recommendation without creating a permanent garment-category ban. | MVP | FEAT-PERS-001; BR-011 | T: Dislike effect and eligibility. |
| FR-PERS-003 | The system shall allow the user to change or clear feedback so contradictory Like/Dislike states do not remain effective on the same target. | MVP | FEAT-PERS-001; BR-011, BR-021 | T: revise/clear feedback. |
| FR-PERS-004 | The system shall distinguish accepted feedback from an unsuccessful recording attempt. | MVP | FEAT-PERS-001; BR-011 | T: failed versus recorded signal. |
| FR-PERS-005 | The system shall avoid creating Wear Events from Like, Dislike, recommendation views, or other preference-only interactions. | MVP | FEAT-PERS-001; BR-011 | T: history unchanged. |

### 3.11 Wear Events

New reporting requires a current valid owned outfit; historical event inspection/correction/removal targets the user's existing event. Intentional repeat use and accidental duplicate processing are different cases.

| Stimulus | Expected System Response |
|---|---|
| Wear This Today for a valid owned outfit | Create a distinct user-reported event with local date/time and available context. |
| Another intentional report within the day | Retain earlier events; permit the same or a different valid outfit. |
| Repeated tap/retry of one action | Do not manufacture another event. |
| Correct/remove one event | Update that event's relevant history/signals without deleting Outfit/Garments or unrelated events. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-WEAR-001 | The system shall create a distinct user-reported Wear Event for each successful separate intentional Wear This Today action on a valid owned outfit. | MVP | FEAT-PERS-002; BR-006, BR-011 | T: accepted report. |
| FR-WEAR-002 | The system shall permit multiple different outfit events in one local calendar day without replacing earlier events. | MVP | FEAT-PERS-002; BR-011 | T: same-day different outfits. |
| FR-WEAR-003 | The system shall permit the same outfit to be recorded again within that day for a separate intentional wear event. | MVP | FEAT-PERS-002; BR-011 | T: intentional same-outfit repeat. |
| FR-WEAR-004 | The system shall associate each event with its selected outfit, local date/time, and user-reported nature. | MVP | FEAT-PERS-002; BR-006, BR-011 | T/I: event date/time and meaning. |
| FR-WEAR-005 | The system shall retain recommendation context and occasion with an event when available without fabricating absent context. | MVP | FEAT-PERS-002; BR-011, BR-022 | T: available/missing context. |
| FR-WEAR-006 | The system shall allow the user to inspect and correct a specific accidental Wear Event. | MVP | FEAT-PERS-002; BR-006, BR-021 | T: event-specific correction. |
| FR-WEAR-007 | The system shall allow removal of an individual Wear Event without removing unrelated events. | MVP | FEAT-PERS-002; BR-006, BR-021 | T: individual removal. |
| FR-WEAR-008 | The system shall preserve the underlying Outfit and Garments when a Wear Event is removed. | MVP | FEAT-PERS-002; BR-006, BR-021 | T: underlying objects remain. |
| FR-WEAR-009 | The system shall prevent repeated taps, retries, or duplicate processing of the same action from creating accidental duplicate events while preserving separate intentional reports. | MVP | FEAT-PERS-002; BR-011 | T: duplicate versus separate intent; OSQ-008. |
| FR-WEAR-010 | The system shall reflect accepted event creation/correction/removal in relevant subsequent history, utilization, recency, and behavioral signals. | MVP | FEAT-PERS-002, FEAT-ANL-001, FEAT-PERS-003; BR-006, BR-011 | T/A: corrected/removed signal. |
| FR-WEAR-011 | The system shall avoid interpreting any event or repeated reporting as verified physical wear or as permission to bypass outfit validity. | MVP | FEAT-PERS-002, FEAT-PERS-003; BR-011, BR-009 | T/A: invalid/hypothetical attempts. |
| FR-WEAR-012 | The system shall allow cancellation of a pending Wear Event correction/removal without applying that requested change. | MVP | FEAT-PERS-002; BR-006, BR-021 | T: cancel correction/removal; original event remains. |

### 3.12 Behavioral Personalization

Explicit preferences and available history influence valid choices; no advanced learned model is mandated.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-PERS-006 | The system shall apply declared style and current occasion to relevant initial advice even without behavioral history. | MVP | FEAT-PERS-003, FEAT-PROF-001; BR-010 | A: no-history preference fixture. |
| FR-PERS-007 | The system shall use current Like and Dislike feedback as meaningful positive/negative soft ranking evidence. | MVP | FEAT-PERS-003, FEAT-PERS-001; BR-011 | A: controlled feedback comparison. |
| FR-PERS-008 | The system shall use Wear Events as stronger positive preference evidence than Like under otherwise controlled conditions. | MVP | FEAT-PERS-003, FEAT-PERS-002; BR-011 | A: signal comparison; OSQ-007. |
| FR-PERS-009 | The system shall use relevant wear history, recency, diversity, and overlooked garments to refine valid choices where alternatives exist. | MVP | FEAT-PERS-003, FEAT-ANL-001; BR-011, BR-006 | A: valid variety/recency fixture. |
| FR-PERS-010 | The system shall use optional body context only softly and avoid category restrictions or prerequisite eligibility based on body/gender. | MVP | FEAT-PERS-003, FEAT-PROF-001; BR-012 | T/A: optional context and eligibility. |
| FR-PERS-011 | The system shall keep validity dominant over every preference signal and avoid requiring a changed result when no useful valid alternative exists. | MVP | FEAT-PERS-003, FEAT-OUT-001; BR-009, BR-011 | A: constrained/no-alternative fixture. |
| FR-PERS-012 | The system shall apply the agreed event-signal normalization policy so accidental/artificial repetition cannot disproportionately amplify ranking, while retaining legitimate multiple-event history. | MVP | FEAT-PERS-003, FEAT-PERS-002; BR-011 | A: repetition cases; OSQ-007. |
| FR-PERS-013 | The system shall stop treating cleared/superseded feedback, corrected/removed events, and removed optional context as unchanged current signals. | MVP | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002; BR-011, BR-012 | T/A: signal revision. |

### 3.13 Wardrobe Utilization & History

History is user-reported and time-aware; preserved historical garments are not current ownership.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-ANL-001 | The system shall present individual Wear Events with understandable local date/time and available occasion/context. | MVP | FEAT-ANL-001, FEAT-PERS-002; BR-006 | T/I: dated event list. |
| FR-ANL-002 | The system shall display multiple same-day events separately, including intentional repeated outfit uses. | MVP | FEAT-ANL-001, FEAT-PERS-002; BR-006, BR-011 | T: multiple-event history. |
| FR-ANL-003 | The system shall provide recorded garment-use frequency/recency and relevant overlooked-item/variety indicators from accepted event information. | MVP | FEAT-ANL-001; BR-006, BR-011 | A: known event history. |
| FR-ANL-004 | The system shall explain incomplete logging and label no recorded use without claiming that the garment was never physically worn. | MVP | FEAT-ANL-001; BR-006, BR-022 | I: logging limitations. |
| FR-ANL-005 | The system shall show a no-Wear-Events state without invented usage/diversity statistics. | MVP | FEAT-ANL-001; BR-006, BR-022 | T: empty history. |
| FR-ANL-006 | The system shall preserve understandable historical outfit/garment snapshots and label a garment removed from active ownership as Removed from wardrobe. | MVP | FEAT-ANL-001; BR-006, BR-022 | T/I: history after garment removal. |
| FR-ANL-007 | The system shall update relevant history/utilization after individual event correction/removal without deleting unrelated records. | MVP | FEAT-ANL-001, FEAT-PERS-002; BR-006, BR-011 | T/A: event lifecycle effects. |

### 3.14 Wardrobe Coverage Score

The Personalized Everyday Capsule uses relevant common needs/priorities, style, climate/context, and the current confirmed wardrobe. It is one contextual model.

| Stimulus | Expected System Response |
|---|---|
| Request coverage with adequate information | Assess relevant needs and show score, covered/underserved areas, and limitations. |
| Information cannot support assessment | Explain missing information without a fabricated numeric score or proven purchase need. |
| Needs/context/wardrobe changes | Reevaluate or mark the prior assessment outdated. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-ANL-008 | The system shall evaluate coverage against the user's relevant Personalized Everyday Capsule needs rather than a universal mandatory wardrobe checklist. | MVP | FEAT-ANL-002, FEAT-PROF-001; BR-014, BR-010 | A: differing-needs fixture. |
| FR-ANL-009 | The system shall identify common need priorities, style, climate/context, and current confirmed wardrobe used by the assessment. | MVP | FEAT-ANL-002; BR-014, BR-022 | I/A: assessment context. |
| FR-ANL-010 | The system shall allow relevant needs/priorities to influence coverage so the same wardrobe can yield different contextual assessments. | MVP | FEAT-ANL-002, FEAT-PROF-001; BR-014, BR-010 | A: same wardrobe, different needs. |
| FR-ANL-011 | The system shall present an explainable Wardrobe Coverage Score only when information supports a defensible assessment. | MVP | FEAT-ANL-002; BR-014, BR-022 | T/A: sufficient/insufficient data. |
| FR-ANL-012 | The system shall identify covered and underserved needs with understandable reasons. | MVP | FEAT-ANL-002; BR-014, BR-022 | I/A: assessed need evidence. |
| FR-ANL-013 | The system shall exclude removed garments from current coverage. | MVP | FEAT-ANL-002; BR-014 | A: post-removal baseline. |
| FR-ANL-014 | The system shall reevaluate or mark coverage outdated after relevant wardrobe/needs/context changes. | MVP | FEAT-ANL-002; BR-014, BR-022 | T: changed assessment context. |
| FR-ANL-015 | The system shall avoid portraying coverage as objective completeness or insufficient information as a confirmed need to purchase. | MVP | FEAT-ANL-002; BR-014, BR-022, BR-024 | I/T: limitation language. |

### 3.15 Gap Analysis

Gap analysis follows adequate contextual coverage; a gap is an underserved capability, not absence of a named catalog product.

| Stimulus | Expected System Response |
|---|---|
| Inspect an underserved need | Describe the capability, its evidence, useful candidate characteristics, and an evaluation path. |
| No important gap or insufficient evidence | Explain the finding/limitation without manufacturing shopping demand. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-GAP-001 | The system shall identify a gap as an underserved capability related to relevant assessed needs and current wardrobe evidence. | MVP | FEAT-GAP-001; BR-015 | A: capability-based gap fixture. |
| FR-GAP-002 | The system shall explain useful candidate characteristics without making one specific product a mandatory solution. | MVP | FEAT-GAP-001; BR-015, BR-022 | I: gap versus product wording. |
| FR-GAP-003 | The system shall connect supported gaps to candidate evaluation and estimated outfit expansion when evaluable candidates exist. | MVP | FEAT-GAP-001; BR-015 | D: gap to utility evaluation. |
| FR-GAP-004 | The system shall report no important gap or insufficient evidence without manufacturing a shopping need. | MVP | FEAT-GAP-001; BR-014, BR-022, BR-024 | T: no-gap/insufficient states. |
| FR-GAP-005 | The system shall permit continued insight/context/wardrobe use without requiring external shopping. | MVP | FEAT-GAP-001; BR-024 | D: commerce-independent path. |

### 3.16 Wardrobe Multiplier

The count is incremental unique valid combinations, not a weighted score or a guarantee. Candidate evaluation never creates owned clothing.

| Stimulus | Expected System Response |
|---|---|
| Evaluate a candidate | Compare current and hypothetical expanded valid outfit sets under the same context. |
| Inspect +N | Explain newly enabled utility and show hypothetical previews containing the candidate. |
| Evaluation is zero, insufficient, or outdated | Distinguish evaluated zero from unavailable/stale evaluation; do not invent a count. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-MULT-001 | The system shall evaluate a candidate hypothetically against the current confirmed active wardrobe, excluding removed garments. | MVP | FEAT-MULT-001; BR-015, BR-016 | A: owned baseline and candidate. |
| FR-MULT-002 | The system shall apply the same assessment context and validity criteria to current and expanded wardrobe comparisons. | MVP | FEAT-MULT-001; BR-009, BR-016 | A: consistent-context comparison. |
| FR-MULT-003 | The system shall count only unique valid outfits newly enabled by adding the candidate, excluding existing combinations and reordered duplicates. | MVP | FEAT-MULT-001; BR-016 | A: known set differences. |
| FR-MULT-004 | The system shall display the incremental count as +N New Outfits with an explanation of its baseline and meaning. | MVP | FEAT-MULT-001; BR-016, BR-022 | I/A: illustrative 41 to 58 gives +17. |
| FR-MULT-005 | The system shall provide inspectable newly enabled hypothetical previews that visibly distinguish the candidate from owned garments. | MVP | FEAT-MULT-001; BR-017 | T/I: candidate in valid new previews. |
| FR-MULT-006 | The system shall leave candidate ownership and wear history unchanged by evaluation or preview viewing. | MVP | FEAT-MULT-001; BR-015, BR-017 | T: no ingestion/wear side effect. |
| FR-MULT-007 | The system shall show evaluated zero honestly and distinguish incomplete/unavailable evaluation from a proven zero or exact complete count. | MVP | FEAT-MULT-001; BR-016, BR-022 | T/A: zero/unavailable/incomplete. |
| FR-MULT-008 | The system shall reevaluate or mark the count/previews outdated after relevant wardrobe/candidate/context changes. | MVP | FEAT-MULT-001; BR-016, BR-022 | T: changed-baseline result. |

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
| FR-SHOP-002 | The system shall show +N New Outfits, why the candidate helps, and access to newly enabled hypothetical previews for usable utility advice. | MVP | FEAT-SHOP-001, FEAT-MULT-001; BR-016, BR-017, BR-018 | T/I: utility explanation and previews. |
| FR-SHOP-003 | The system shall show price range, material, longevity/durability, retailer identity, or destination only when credible and available, qualifying estimates. | Conditional | FEAT-SHOP-001; BR-020 | I: credible and missing optional fields. |
| FR-SHOP-004 | The system shall retain usable utility advice when optional commercial information is absent without inventing price, stock, merchant availability, partnerships, or vanity scores. | MVP | FEAT-SHOP-001; BR-020, BR-024 | T/I: optional-data omissions. |
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
| FR-MET-004 | The system shall support the PRD's processing/outfit timing, manual-continuation, utilization, and repeat-use measurement directions without fabricated success or targets. | MVP | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001, FEAT-ANL-001; BR-019, BR-001, BR-007, BR-006 | A/I: approved metric evidence. |
| FR-MET-005 | The system shall prevent retry/accidental-duplicate inflation while permitting distinct intentional Wear Events to be measured. | MVP | FEAT-MET-001, FEAT-PERS-002; BR-019, BR-011 | T: duplicate versus intentional activity. |
| FR-MET-006 | The system shall respect authorized privacy handling and avoid requiring raw personal images or sensitive profile details as engagement-metric content. | MVP | FEAT-MET-001; BR-021 | I: measurement information. |
| FR-MET-007 | The system shall preserve an otherwise usable core action when measurement is unavailable without falsely changing its success/failure state. | MVP | FEAT-MET-001; BR-019 | T: measurement dependency failure. |


## 4. Data Requirements

### 4.1 Logical Information Domains

These requirements specify information meaning/ownership, not tables, storage types, indexes, object persistence, or an ERD.

| Domain | Logical Information |
|---|---|
| Access and context | User Account, Personalization Profile, current request context, and authentication/recovery information. |
| Current wardrobe | Wardrobe, active/removed Garment, confirmed Garment Profile, and applicable images/proposals/readiness. |
| Advice and behavior | Outfit, Outfit Recommendation, Feedback, Wear Event, and Wear History. |
| Wardrobe intelligence | Coverage Assessment, Wardrobe Gap, Candidate Garment, Multiplier Assessment, and Shopping Recommendation. |
| Measurement | Distinguishable exposures, accepted actions/corrections/removals, failures, and qualified metric evidence. |

### 4.2 Account & Profile Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-AUTH-001 | The system shall associate registration/login/recovery information with the correct user account and protect credential/recovery information from unauthorized access. | MVP | FEAT-AUTH-001; BR-021 | T/I: account ownership and secrets. |
| DATA-AUTH-002 | The system shall represent Email as account-access information and Display Name as optional, without imposing a required naming format unsupported by the product baseline. | MVP | FEAT-AUTH-001; BR-021 | T: optional Display Name. |
| DATA-AUTH-003 | The system shall retain the user's current declared styles/common priorities and distinguish them from request-specific occasion. | MVP | FEAT-PROF-001; BR-010 | T/I: independent context values. |
| DATA-AUTH-004 | The system shall represent omitted/removed body/gender as absent current context rather than inferred replacements. | MVP | FEAT-PROF-001; BR-012, BR-021 | T/I: removed optional values. |

### 4.3 Garment & Wardrobe Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-GAR-001 | The system shall associate each current garment/profile with its owning user's wardrobe and confirmed primary category/dominant color. | MVP | FEAT-GAR-001, FEAT-WAR-001; BR-003, BR-005 | T/I: owner and saveable minimum. |
| DATA-GAR-002 | The system shall support the classification, layering/shape, color, pattern, contextual, and material information described in Section 4.10 while preserving optional/unknown states. | MVP | FEAT-GAR-001; BR-003 | I: full logical dimensions. |
| DATA-GAR-003 | The system shall associate proposed attributes with useful uncertainty/provenance information separately from authoritative user values. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | T/I: suggestion versus confirmation. |
| DATA-GAR-004 | The system shall associate available image/preview information with the appropriate draft or garment without requiring an image for manually saved entries. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002 | T: imageless and image entry. |
| DATA-GAR-005 | The system shall distinguish active ownership, removed state, and recommendation readiness without losing an otherwise saveable entry. | MVP | FEAT-GAR-001, FEAT-WAR-002; BR-003, BR-005 | T/I: independent states. |

### 4.4 Outfit & Recommendation Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-OUT-001 | The system shall identify the constituent garment identities of an outfit sufficiently to distinguish unique combinations from reordered duplicates. | MVP | FEAT-OUT-001, FEAT-MULT-001; BR-008, BR-016 | A: identity-based uniqueness. |
| DATA-OUT-002 | The system shall associate displayed advice with its evaluated wardrobe/context and information needed for explanations and outdated-state detection. | MVP | FEAT-OUT-001, FEAT-OUT-002; BR-007, BR-022 | T/I: context and freshness. |
| DATA-OUT-003 | The system shall distinguish current owned outfits, historical selections, and hypothetical candidate outfits in their logical meaning. | MVP | FEAT-OUT-002, FEAT-MULT-001; BR-017, BR-022 | I: three outfit contexts. |

### 4.5 Feedback & Wear Event Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-WEAR-001 | The system shall associate current Like/Dislike feedback with the correct user/target and preserve its accepted, changed, or cleared meaning. | MVP | FEAT-PERS-001; BR-011, BR-021 | T/I: effective feedback state. |
| DATA-WEAR-002 | The system shall represent each distinct intentional Wear Event with selected outfit, local date/time, user-reported meaning, and available occasion/context. | MVP | FEAT-PERS-002; BR-006, BR-011 | I: complete event meaning. |
| DATA-WEAR-003 | The system shall distinguish separate intentional events from duplicate processing of one action rather than collapsing all same-date/same-outfit uses. | MVP | FEAT-PERS-002; BR-011 | T: duplicate versus distinct intent. |
| DATA-WEAR-004 | The system shall reflect event correction/removal in the effective history/signal information without deleting underlying Outfit/Garments. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-011 | T/I: event-specific lifecycle. |

### 4.6 Coverage / Gap / Candidate Evaluation Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-ANL-001 | The system shall associate each coverage assessment with its Personalized Everyday Capsule needs/priorities, style/climate context, current wardrobe basis, results, and information limitations. | MVP | FEAT-ANL-002; BR-014, BR-022 | I/A: assessment basis. |
| DATA-ANL-002 | The system shall associate a gap with its underserved capability, relevant assessment evidence, and candidate characteristics rather than merely a missing product name. | MVP | FEAT-GAP-001; BR-015, BR-022 | I: capability/evidence linkage. |
| DATA-ANL-003 | The system shall represent a candidate separately from owned garments with applicable visual/identity/category/subtype/attributes and credible optional commercial information. | MVP | FEAT-SHOP-001, FEAT-MULT-001; BR-015, BR-018, BR-020 | I: candidate and optional values. |
| DATA-ANL-004 | The system shall associate multiplier results with the candidate, same-context current/expanded comparison, incremental unique-valid count, previews, and completeness/freshness state. | MVP | FEAT-MULT-001; BR-016, BR-017, BR-022 | A/I: count and context evidence. |

### 4.7 Data Integrity

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-INT-001 | The system shall prevent an AI proposal or later analysis from silently superseding a confirmed/corrected garment value. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002 | T: authority preservation. |
| DATA-INT-002 | The system shall exclude removed garments from effective current recommendation/coverage/multiplier inputs even when historical snapshots remain. | MVP | FEAT-WAR-002, FEAT-MULT-001, FEAT-ANL-002; BR-005, BR-014, BR-016 | A: current versus historical input. |
| DATA-INT-003 | The system shall avoid creating owned garments or Wear Events solely from candidate evaluation, preview, or external navigation. | MVP | FEAT-MULT-001, FEAT-SHOP-002; BR-015, BR-017, BR-018 | T: no ownership/wear side effect. |
| DATA-INT-004 | The system shall keep failed/pending mutations distinct from accepted authoritative state and protect accepted additions/events from accidental duplicate processing. | MVP | FEAT-AI-002, FEAT-PERS-002; BR-005, BR-011 | T: retry and failure integrity. |

### 4.8 Historical Data

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-HIST-001 | The system shall preserve snapshots sufficient to understand past Wear Events/outfit records after associated garments leave current ownership. | MVP | FEAT-ANL-001; BR-006 | T/I: past record after removal. |
| DATA-HIST-002 | The system shall identify removed garments in historical presentation as Removed from wardrobe without treating the snapshot as an active item. | MVP | FEAT-ANL-001; BR-006, BR-022 | I/A: removed snapshot. |

### 4.9 Retention / Deletion

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| DATA-RET-001 | The system shall remove a deleted Wear Event from effective reported-use history/signals while retaining its underlying Outfit/Garments and unrelated events. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-021 | T: effective removal. |
| DATA-RET-002 | The system shall retain meaningful historical outfit/garment information after active-garment removal rather than automatically erasing that history. | MVP | FEAT-WAR-002, FEAT-ANL-001; BR-005, BR-006 | T: active removal preserves history. |

No physical purge mechanism, legal retention duration, indefinite retention guarantee, account-export/closure workflow, or unrestricted future AI-training use is specified. Remaining retention/deletion/consent rules are OSQ-011; they must respect the settled historical and user-control semantics.

### 4.10 Logical Data Dictionary

| Object / Information | Logical Meaning / Necessary Relationships |
|---|---|
| User Account | Private access identity and authorized relationship to personal wardrobe/profile/history; Email/password recovery and optional Display Name. |
| Personalization Profile | Current style/common need priorities, optional body/gender, and selected location/context; current request occasion remains separate. |
| Wardrobe | Current confirmed active owned garments; differs from historical snapshots. |
| Garment | Individually distinguishable owned item, supported primary category, authoritative profile, active/removed state, and readiness. |
| Garment Profile | Confirmed Primary Category/Dominant Color plus supported optional classification, layering level/bulk/fit/silhouette, dominant/secondary colors/HEX/HSL/temperature/palette role, pattern type/density/noise, season/occasion/style associations, and confidence-aware material. |
| Outfit | Constituent garment identities assessed in context; reordering identities creates no new combination. |
| Outfit Recommendation | A proposed valid owned outfit with context and relevant reasons, distinct from a past report or hypothetical preview. |
| Feedback | Current accepted positive/negative preference for an identified recommendation; not a wear record. |
| Wear Event | Distinct intentional user report with outfit, local date/time, available context/occasion, and individual correction/removal semantics. |
| Wear History | Time-aware view of accepted reports and meaningful snapshots, including multiple events per day; incomplete logging is disclosed. |
| Coverage Assessment | Contextual score/covered/underserved needs and limitations for one Personalized Everyday Capsule. |
| Wardrobe Gap | Evidence-supported underserved capability related to coverage, not a compulsory named product. |
| Candidate Garment | Hypothetical potential addition with relevant characteristics; is not owned before separate confirmation. |
| Multiplier Assessment | Consistent-context incremental unique valid outfit count, evaluated zero versus unavailable state, and newly enabled previews. |
| Shopping Recommendation | Candidate/gap/utility/reason/previews and credible optional commercial details; external commerce boundary. |
| Provenance / uncertainty | How information was established versus uncertainty of a proposal; confirmation establishes authority without proving physical material composition. |

The following controlled values mirror PRD Sections 11.3 and 13.2. The PRD remains their product authority; changes require synchronized traceability, not local SRS-only taxonomy expansion.

| Vocabulary | Approved Initial Values |
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

OTHER/UNKNOWN applies to subtypes, not new primary categories. Common need dimensions and request occasion vocabulary have different purposes; detailed mappings and remaining descriptor vocabularies are OSQ-005/OSQ-009.

## 5. External Interface Requirements

### 5.1 User Interfaces

These requirements preserve navigation responsibilities and semantic states; they do not dictate screens, components, pixels, or a design system.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| UI-001 | The system shall make Today, Wardrobe, Insights, and Profile responsibilities reachable with Add Garment and applicable detail/return paths. | MVP | FEAT-WAR-001, FEAT-OUT-001, FEAT-PROF-001; BR-005, BR-007, BR-010 | D: navigation through JRN-01–JRN-07. |
| UI-002 | The system shall present the approved progressive onboarding flow with optional context and non-blocking first-garment guidance. | MVP | FEAT-AUTH-001, FEAT-PROF-001; BR-012, BR-021, BR-023 | D: onboarding skip/return. |
| UI-003 | The system shall distinguish loading, empty, no-match, pending, confirmed, unavailable, failed, and outdated states where applicable. | MVP | FEAT-WAR-001, FEAT-OUT-001; BR-005, BR-022 | T/I: state inventory. |
| UI-004 | The system shall display garment identity/category/color, readiness guidance, and available image without hiding imageless manual entries. | MVP | FEAT-WAR-001, FEAT-GAR-001; BR-005, BR-003 | I: cards and minimum-only detail. |
| UI-005 | The system shall present AI confidence as High Confidence, Needs Review, or Uncertain using understandable localized labels rather than primary raw numeric confidence. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | I/T: three semantic confidence states. |
| UI-006 | The system shall require explicit confirmation for an authoritative garment entry and expose correction/manual controls. | MVP | FEAT-AI-002; BR-002 | D: proposal to corrected confirmation. |
| UI-007 | The system shall show Wear Events with local date/time and individual inspect/correct/remove actions. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-021 | D: multiple-event lifecycle. |
| UI-008 | The system shall visibly distinguish hypothetical candidates/previews from owned outfits and removed historical garments from active ownership. | MVP | FEAT-MULT-001, FEAT-ANL-001; BR-017, BR-006, BR-022 | I: hypothetical and removed indicators. |
| UI-009 | The system shall show required candidate utility information and identify optional external navigation without requiring absent commercial data. | MVP | FEAT-SHOP-001, FEAT-SHOP-002; BR-018, BR-020, BR-024 | I/T: usable candidate without retailer. |

### 5.2 Software Interfaces

The interfaces below identify logical capabilities. Garment analysis may be delivered internally or externally; no service/module boundary, provider, transport payload, or endpoint is selected.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| SI-001 | The system shall exchange selected-image analysis input and available preview/proposals/uncertainty with the garment-analysis capability while retaining user confirmation as authority. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002 | T/I: analysis success/failure exchange. |
| SI-002 | The system shall exchange selected location and relevant environmental information with a weather capability and distinguish unavailable/stale results. | MVP | FEAT-PROF-002; BR-013, BR-022 | T: location/weather variants. |
| SI-003 | The system shall use email delivery for the approved password-recovery interaction and distinguish request/delivery/reset outcomes where determinable. | MVP | FEAT-AUTH-001; BR-021 | T: email recovery exchange. |
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
| COM-001 | The system shall protect private authentication/wardrobe/profile/history information against unauthorized disclosure or alteration during communication. | MVP | FEAT-AUTH-001; BR-021 | A/T: confidential/integrity-protected exchange. |
| COM-002 | The system shall distinguish incomplete/unavailable communications from accepted action outcomes and provide the applicable recovery in Section 9. | MVP | FEAT-AI-002, FEAT-PERS-002; BR-005, BR-011 | T: interrupted save/report. |
| COM-003 | The system shall avoid granting external destinations unrestricted access to private wardrobe/profile/history merely because a shopping link was opened. | MVP | FEAT-SHOP-002, FEAT-AUTH-001; BR-018, BR-021 | I/T: handoff data/privacy boundary. |

## 6. Quality Requirements

Quality obligations apply to the specified MVP behavior, not a selected architecture. BRD MET-Q01–MET-Q07 and PRD Section 25 supply validation directions. Approximate timings are retained as approved targets; measurement boundaries, reference conditions, aggregation, and acceptance tolerance must be resolved in OSQ-001 before final quantitative acceptance. No percentile, universal timing guarantee, service-level percentage, throughput, or recovery interval is implied.

### 6.1 Performance

Faster valid completion is acceptable.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-PERF-001 | The system shall meet the approved garment-processing target of approximately 3–5 seconds under the agreed reference image/network/device conditions; precise measurement and acceptance criteria remain OSQ-001. | MVP | FEAT-AI-001, FEAT-MET-001; BR-001, BR-019 | T/A: measured processing samples; MET-Q02; conditions TBD. |
| NFR-PERF-002 | The system shall meet the approved outfit-generation target below approximately 3 seconds under the agreed reference conditions, including a wardrobe of 100+ confirmed items; precise measurement and acceptance criteria remain OSQ-001. | MVP | FEAT-OUT-001, FEAT-MET-001; BR-007, BR-019 | T/A: reference-workload timings; MET-Q03; conditions TBD. |

### 6.2 Security

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-SEC-001 | The system shall permit zero successful unauthorized cross-user wardrobe/image accesses in the agreed validation scenarios. | MVP | FEAT-AUTH-001, FEAT-WAR-001; BR-021 | T: two-account and unauthenticated attempts; MET-Q06. |
| NFR-SEC-002 | The system shall prevent an account switch or logout from exposing the preceding user's protected personal content to the next user. | MVP | FEAT-AUTH-001; BR-021 | T: logout/switch and return-navigation scenarios. |
| NFR-SEC-003 | The system shall protect password, access/refresh, and recovery secrets from unauthorized disclosure through user-visible output and measurement information. | MVP | FEAT-AUTH-001, FEAT-MET-001; BR-021 | I/T: protected output and measurement review. |

### 6.3 Privacy

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-PRIV-001 | The system shall explain the relevant use of personal images, profile, location, and behavioral information and respect the originating feature's omission, correction, removal, and permission controls. | MVP | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002, FEAT-PERS-001, FEAT-PERS-002; BR-012, BR-013, BR-021 | I/D: purpose/control review across JRN-01–JRN-05. |
| NFR-PRIV-002 | The system shall avoid treating garment corrections, behavior recording, or external navigation as automatic unrestricted permission for future AI training or retailer data sharing. | MVP | FEAT-AUTH-001, FEAT-AI-002, FEAT-MET-001, FEAT-SHOP-002; BR-002, BR-018, BR-021 | I: usage and external-handoff boundaries; OSQ-011. |

### 6.4 Reliability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-REL-001 | The system shall preserve manual garment creation in all agreed representative AI-failure scenarios without changing confirmed information into an unconfirmed proposal. | MVP | FEAT-AI-001, FEAT-AI-002; BR-002, BR-004 | T: agreed unavailable/failed/uncertain analysis cases; MET-Q05. |
| NFR-REL-002 | The system shall maintain count/explanation integrity so controlled multiplier results match incremental unique valid outfits and coverage explanations match their assessed needs/context. | MVP | FEAT-MULT-001, FEAT-ANL-002; BR-014, BR-016, BR-022 | A: controlled fixtures; MET-Q07; OSQ-006/009/010. |

### 6.5 Availability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-AVL-001 | The system shall keep otherwise usable current-wardrobe and core advice paths independent of unavailable optional weather, shopping, or measurement dependencies, subject to available access/network and sufficient valid assessment information. | MVP | FEAT-WAR-001, FEAT-OUT-001, FEAT-SHOP-002, FEAT-MET-001; BR-005, BR-007, BR-019, BR-024 | T: isolated dependency failures and applicable reduced-context/limited-result states. |

A production availability percentage, outage window, and recovery target are not established. OSQ-013 must define any required operating targets before quality acceptance; this requirement establishes dependency independence, not guaranteed offline access.

### 6.6 Usability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-USE-001 | The system shall make the initial Android/iOS journeys usable through understandable Vietnamese guidance for setup, confirmation/correction, readiness, limited results, and recovery. | MVP | FEAT-AUTH-001, FEAT-AI-002, FEAT-WAR-001, FEAT-OUT-001; BR-002, BR-005, BR-022, BR-023 | D/I: representative users complete core journeys; evaluation criteria OSQ-014. |
| NFR-USE-002 | The system shall support inspection of analysis previews/background-removal usefulness separately from category/color accuracy, without treating an unusable preview as a successful wardrobe addition. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002, BR-022 | D/A: qualified preview-usability evaluation; MET-Q01; OSQ-001. |

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
| NFR-TEST-002 | The system shall make accepted versus attempted/failed actions and timing evidence distinguishable for the approved product validation measures without requiring sensitive personal contents as metric evidence. | MVP | FEAT-MET-001; BR-019, BR-021 | A/I: validation records and measurement definitions; OSQ-001. |

### 6.10 Interoperability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-INT-001 | The system shall preserve the logical meaning of dependency success, incomplete information, and unavailability across garment-analysis, weather, email-recovery, and external-shopping interactions. | MVP | FEAT-AI-001, FEAT-PROF-002, FEAT-AUTH-001, FEAT-SHOP-002; BR-001, BR-013, BR-018, BR-021, BR-022 | T/I: representative interface outcome contracts; OSQ-012. |

### 6.11 Scalability

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| NFR-SCA-001 | The system shall retain valid, complete core wardrobe/recommendation behavior for the approved 100+ confirmed-item reference workload rather than meeting timing by silently omitting applicable items. | MVP | FEAT-WAR-001, FEAT-OUT-001; BR-005, BR-007, BR-009 | T/A: reference inventory and expected eligible-item set; NFR-PERF-002. |

The workload is a validation reference, not a minimum onboarding quota, maximum wardrobe size, or simultaneous-user target. Capacity, concurrency, and larger-load acceptance remain OSQ-013.

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
| LOC-005 | The system shall present Wear Events with understandable local date/time while distinguishing multiple same-day reports and separate intentional repeats. | MVP | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-011, BR-022 | T/I: local date/time and multiple-event history; OSQ-008. |

Project requirements documentation remains in English. Detailed timezone/travel/date-correction policy is OSQ-008; no new scheduling/calendar feature is implied.

## 8. Other Requirements

### 8.1 AI-Specific Requirements

AI assistance reduces effort; authority is established through user action. Confidence is uncertainty of a prediction, while provenance describes how a value was established. The exact confidence mapping is OSQ-004. Advanced learned ranking, model architectures, training pipelines, and particular ML frameworks are not MVP requirements.

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| AI-REQ-001 | The system shall treat unconfirmed AI-generated garment values as suggestions rather than authoritative wardrobe information, regardless of predicted confidence. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003 | T: high-confidence proposal before confirmation. |
| AI-REQ-002 | The system shall allow users to review and correct proposed attribute information before confirming the garment profile. | MVP | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003 | T: proposed, corrected, and confirmed values. |
| AI-REQ-003 | The system shall allow users to continue with manual information when analysis is unavailable, uncertain, or unsuitable, including imageless manual creation. | MVP | FEAT-AI-001, FEAT-AI-002; BR-001, BR-004 | T: analysis failures and manual completion. |
| AI-REQ-004 | The system shall represent user-facing prediction confidence through High Confidence, Needs Review, and Uncertain semantic states using the agreed mapping. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | T/I: all states; thresholds OSQ-004. |
| AI-REQ-005 | The system shall use understandable localized confidence states as the primary presentation rather than require raw numeric confidence for user decisions. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | I: review presentation. |
| AI-REQ-006 | The system shall distinguish AI Suggested, User Confirmed, User Corrected, and User Entered provenance for applicable garment information. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | T/I: proposal, accept, change, and manual entry. |
| AI-REQ-007 | The system shall make the user's confirmed, corrected, or entered-and-confirmed values authoritative while preventing later AI output from silently replacing them. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002 | T: confirmation and subsequent analysis. |
| AI-REQ-008 | The system shall preserve unknown/missing attribute states instead of fabricating category details, colors, materials, or optional context to complete a profile. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | T/I: partial/unsupported information. |
| AI-REQ-009 | The system shall qualify material predictions/claims when uncertain rather than present inferred composition as verified physical fact. | MVP | FEAT-GAR-001; BR-003, BR-022 | I: unknown and qualified material. |
| AI-REQ-010 | The system shall achieve at least 90% primary-category recognition correctness on the agreed qualified-image evaluation set, measured separately from dominant-color recognition and preview usefulness. | MVP | FEAT-AI-001, FEAT-GAR-001, FEAT-MET-001; BR-001, BR-003, BR-019 | A: category results; MET-Q01; dataset/measurement OSQ-001. |
| AI-REQ-011 | The system shall achieve at least 90% dominant-color recognition correctness on the agreed qualified-image evaluation set, measured separately from primary-category recognition and other rich attributes. | MVP | FEAT-AI-001, FEAT-GAR-001, FEAT-MET-001; BR-001, BR-003, BR-019 | A: dominant-color results; MET-Q01; ground truth/measurement OSQ-001. |
| AI-REQ-012 | The system shall identify an attribute marked Needs Review and prompt the user to check its proposed value while retaining review/correction control. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003 | T/I: Needs Review field guidance. |
| AI-REQ-013 | The system shall explain that an Uncertain attribute could not be identified confidently and offer correction/manual information rather than imply a confirmed prediction. | MVP | FEAT-GAR-001, FEAT-AI-002; BR-002, BR-003, BR-004 | T/I: Uncertain field continuation. |

No equal accuracy target is assigned to every rich attribute. The qualified image set, ground truth, sample sufficiency, and usable-preview criteria must be agreed before final acceptance. Software tests must distinguish authority integrity from prediction correctness.

### 8.2 Explainability Requirements

FR-OUT-010–FR-OUT-013 cover relevant outfit reasoning and limitations; FR-ANL-009–FR-ANL-015 cover contextual coverage; FR-GAP-001–FR-GAP-004 cover capability evidence; FR-MULT-004/FR-MULT-005 and FR-SHOP-001–FR-SHOP-004 cover incremental candidate utility. AI-REQ-004–AI-REQ-009 govern uncertainty/provenance/material limitations.

These are the normative explanation requirements. Explanations must correspond to the actual evaluated information; no universal fashion correctness, physical wear, purchase, fit, savings, or durability guarantee is established.

### 8.3 Privacy / Data Protection Requirements

FR-AUTH-008, DATA-AUTH-001, COM-001/COM-003, NFR-SEC-001–NFR-SEC-003, and NFR-PRIV-001/NFR-PRIV-002 establish private access, protected exchange, clear purposes, and control. FR-PROF-005–FR-PROF-007 and HW-003 preserve optional context. FR-MET-006 limits engagement evidence; DATA-RET-* preserves settled effective-removal/history semantics.

Consent, retention, and broader deletion/sharing boundaries remain OSQ-011. Optional fields and permissions cannot become undisclosed access gates.

### 8.4 Auditability / Provenance Requirements

DATA-GAR-003, DATA-INT-001, and AI-REQ-006/AI-REQ-007 make suggestion versus authoritative information verifiable. DATA-WEAR-002–DATA-WEAR-004, DATA-HIST-001/DATA-HIST-002, and FR-MET-002/FR-MET-005 distinguish intentional events, effective corrections/removals, historical snapshots, and duplicate requests.

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
| ERR-AUTH-001 | Invalid or unusable login information. | The system shall deny failed authentication with understandable correction/recovery guidance without exposing private content or representing the wardrobe as empty. | User may correct input or initiate recovery; personal state is not changed. | FEAT-AUTH-001; BR-021 | T: invalid login. |
| ERR-AUTH-002 | Reset cannot complete or is not valid. | The system shall distinguish unsuccessful/invalid password recovery from a completed reset and offer appropriate retry or renewed recovery without claiming the password changed. | Existing credentials remain effective unless a reset actually completes; no provider-delivery guarantee. | FEAT-AUTH-001; BR-021 | T: unavailable delivery/invalid reset; OSQ-003. |
| ERR-AUTH-003 | Personal access no longer valid. | The system shall require renewed access when authentication/renewal fails, retaining the distinction between inaccessible and empty personal information. | Protected actions pause; user can reauthenticate; no cross-account substitution. | FEAT-AUTH-001; BR-021 | T: expired/failed renewal; OSQ-002. |

### 9.2 Analysis and Uncertainty

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-AI-001 | Selected image is unsuitable, or garment analysis fails/is unavailable. | The system shall show unsuitable-image or unavailable/failed-analysis guidance with image replacement, manual-entry continuation, or retry, preserving existing confirmed profiles. | Manual entry remains usable; no garment is added from an unconfirmed failed attempt. | FEAT-AI-001, FEAT-AI-002; BR-001, BR-002, BR-004 | T: analysis unavailable and retry. |
| ERR-AI-002 | Prediction is uncertain or low confidence. | The system shall identify uncertain/incomplete predictions and allow review, correction, or manual entry rather than silently confirming them. | User may confirm sufficient corrected information; no mandatory numeric confidence gate. | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003, BR-004 | T: uncertain/partial prediction. |

### 9.3 Garment Completeness

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-GAR-001 | Save attempted without the saveable minimum. | The system shall explain missing confirmed Primary Category or Dominant Color and retain the draft for correction rather than save it as an authoritative garment. | User supplies/changes the missing information; existing garments remain unchanged. | FEAT-AI-002, FEAT-GAR-001; BR-002, BR-003 | T: each minimum value missing. |
| ERR-GAR-002 | Saved garment lacks recommendation-ready information. | The system shall keep a minimum-confirmed garment saveable while explaining missing compatibility information and excluding it from uses requiring that information. | User can enrich the profile; no fabricated descriptor or invalid outfit. | FEAT-GAR-001, FEAT-WAR-001, FEAT-OUT-001; BR-003, BR-005, BR-007, BR-009 | T: minimum-only entry; OSQ-005. |

### 9.4 Environmental Context

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-WEATHER-001 | Weather/location information cannot support the request. | The system shall disclose unavailable/stale weather, offer applicable manual-location/context review and retry, and present only defensible reduced-context results without false weather claims. | Core use continues where valid information is sufficient; otherwise explain the limitation. Manual location does not guarantee weather availability. | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-013, BR-022 | T: denied permission, provider failure, stale result. |

### 9.5 Outfit Choice and Shuffle

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-OUT-001 | Insufficient applicable information for useful advice. | The system shall explain insufficient recommendation-ready wardrobe/context information and useful correction/addition steps without imposing an arbitrary onboarding quota. | Wardrobe/manual setup remains available; accepted inventory is not erased. | FEAT-OUT-001, FEAT-GAR-001; BR-007, BR-009, BR-022 | T: sparse/incomplete wardrobe. |
| ERR-OUT-002 | Fewer than three distinct valid outfits exist. | The system shall show the actual one, two, or zero distinct valid outfits with a limitation/no-valid-outfit explanation rather than padding with duplicates or invalid combinations. | User may revise context or wardrobe; no fake third option or fabricated wardrobe gap. | FEAT-OUT-001; BR-007, BR-008, BR-009, BR-022 | T/A: known two/one/zero valid sets. |
| ERR-OUT-003 | No compatible same-context slot replacement. | The system shall retain the existing outfit and explain no compatible alternative when the selected Shuffle slot has no valid replacement. | Other garments remain fixed; no Dislike or Wear Event is inferred. | FEAT-OUT-003; BR-007, BR-008, BR-009, BR-022 | T: no-alternative slot. |

### 9.6 Wear Event Protection

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-WEAR-001 | Duplicate processing of one reporting action. | The system shall avoid creating another Wear Event from repeated taps/retries/duplicate processing of one action, while distinguishing a later separate intentional report. | At most one logical event for that action; other intentional events remain distinct. | FEAT-PERS-002; BR-011 | T: retry versus intentional repeat; OSQ-008. |
| ERR-WEAR-002 | A Wear Event action fails or has an uncertain outcome. | The system shall show failed/pending Wear Event creation/correction/removal distinctly from success and provide recovery without deleting the underlying Outfit/Garments or unrelated events. | Previously accepted history remains meaningful; verify outcome/retry safely, avoiding accidental duplicates. | FEAT-PERS-002, FEAT-ANL-001; BR-006, BR-011, BR-021, BR-022 | T: interrupted event mutation. |

### 9.7 Assessment Integrity

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-ANL-001 | Information cannot support a defensible assessment. | The system shall show insufficient-data coverage/gap/multiplier states with missing-information guidance rather than a fabricated score, mandatory purchase need, or proven zero. | User can improve context/profile or continue other core use; +0 is reserved for evaluated zero. | FEAT-ANL-002, FEAT-GAP-001, FEAT-MULT-001; BR-014, BR-015, BR-016, BR-022 | T/A: insufficient versus evaluated-zero inputs. |
| ERR-ANL-002 | Prior result no longer corresponds to current inputs. | The system shall reevaluate affected advice or mark it outdated after relevant wardrobe/profile/context/candidate changes before presenting it as current. | Historical/past results stay distinguishable; stale counts/previews are not current evidence. | FEAT-OUT-001, FEAT-ANL-002, FEAT-MULT-001; BR-007, BR-014, BR-016, BR-022 | T: changed assessment inputs. |

### 9.8 Candidate Information and Shopping Destinations

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-SHOP-001 | Candidate unavailable, insufficient, or unhelpful. | The system shall explain no evaluable/useful candidate when required utility information cannot support advice rather than invent candidate attributes, multiplier results, or previews. | Wardrobe/gap/context work can continue; no forced purchase path. | FEAT-SHOP-001, FEAT-MULT-001; BR-015, BR-016, BR-017, BR-018, BR-022 | T: missing essential candidate information. |
| ERR-SHOP-002 | Optional commercial data unavailable or uncertain. | The system shall omit or qualify unavailable/unverified optional price/material/longevity/retailer/destination information while retaining supported candidate utility. | Usable +N/reasons/previews remain accessible when supported; no invented claim. | FEAT-SHOP-001; BR-018, BR-020, BR-022 | T/I: optional commercial information missing. |
| ERR-SHOP-003 | External shopping navigation cannot proceed. | The system shall explain a known absent/unavailable external destination and retain available candidate/wardrobe utility without claiming navigation or purchase succeeded. | User may continue core use/retry where appropriate; retailer operations are outside system control. | FEAT-SHOP-002, FEAT-SHOP-001; BR-018, BR-022, BR-024 | T: absent link and known handoff failure. |

### 9.9 Communication and Partial Dependency Failure

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-NET-001 | Network/communication fails during an action. | The system shall distinguish interrupted communication and uncertain mutation outcomes from confirmed success or empty data, providing applicable outcome review/retry without accidental duplicate actions. | Previously confirmed information is preserved; an already accepted action is not repeated as a new one. No general offline guarantee. | FEAT-AI-002, FEAT-PERS-002, FEAT-WAR-001; BR-005, BR-011, BR-022 | T: interruption before/after action acceptance. |
| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-DEP-001 | External capability unavailable. | The system shall identify the affected unavailable dependency and preserve independent authorized core paths instead of presenting a dependency failure as loss of the user's wardrobe. | Use manual/retry/recovery paths appropriate to Sections 9.1–9.8; mandatory access remains protected. | FEAT-AI-001, FEAT-PROF-002, FEAT-AUTH-001, FEAT-SHOP-002; BR-001, BR-013, BR-018, BR-021, BR-022 | T: isolated analysis/weather/email/navigation failures. |
| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-MET-001 | Measurement dependency fails. | The system shall avoid blocking or falsely reversing an otherwise usable core action solely because product measurement is unavailable. | Core result remains accurate; unavailable evidence is not reported as a successful recorded metric. | FEAT-MET-001; BR-019 | T: accepted core action with unavailable measurement. |

## 10. Requirement Traceability

### 10.1 Traceability Model

Traceability preserves BG-* → BR-* → CAP-* → FEAT-* → SRS requirement. BRD Section 26 and PRD Section 33 retain the existing goal/business/capability relationships. The functional matrix below derives capability context from the cited PRD features; it does not redefine the capability taxonomy or claim that every requirement independently delivers every capability attached to its feature.

Supporting DATA/UI/SI/HW/COM/NFR/LOC/AI/ERR requirements retain explicit feature/business sources and verification in their defining tables. A range below includes only contiguous defined IDs, and every FEAT-* has direct functional coverage. These audits establish specification coverage, not implemented or tested delivery.

### 10.2 Functional Requirement Traceability Matrix

| SRS Requirement | PRD Feature | BRD Requirement | Capability Context | Verification |
|---|---|---|---|---|
| FR-AUTH-001 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: registration and mismatch cases. |
| FR-AUTH-002 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: correct/incorrect credentials. |
| FR-AUTH-003 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: reset initiation and email handoff. |
| FR-AUTH-004 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: valid reset then new-password login. |
| FR-AUTH-005 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: valid-session return. |
| FR-AUTH-006 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: expired-access scenario. |
| FR-AUTH-007 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: protected action after logout. |
| FR-AUTH-008 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: signed-out/cross-user denial. |
| FR-AUTH-009 | FEAT-AUTH-001 | BR-021 | CAP-01 | T/I: absent, invalid, valid JWT access. |
| FR-AUTH-010 | FEAT-AUTH-001 | BR-021 | CAP-01 | T/I: token roles; OSQ-002 policy. |
| FR-AUTH-011 | FEAT-AUTH-001, FEAT-PROF-001 | BR-012, BR-021 | CAP-01, CAP-06 | T: omitted optional information. |
| FR-AUTH-012 | FEAT-AUTH-001 | BR-021 | CAP-01 | T: each required field missing and mismatch. |
| FR-PROF-001 | FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002 | BR-010, BR-012, BR-013, BR-023 | CAP-01, CAP-05, CAP-06, CAP-07 | D: first-use journey. |
| FR-PROF-002 | FEAT-PROF-001 | BR-010 | CAP-01, CAP-06 | T: every approved style choice. |
| FR-PROF-003 | FEAT-PROF-001 | BR-010, BR-014 | CAP-01, CAP-06 | T: common-needs priority changes. |
| FR-PROF-004 | FEAT-PROF-001, FEAT-OUT-001 | BR-010 | CAP-01, CAP-05, CAP-06 | T: differing current/common context. |
| FR-PROF-005 | FEAT-PROF-001 | BR-012 | CAP-01, CAP-06 | T: optional-field lifecycle. |
| FR-PROF-006 | FEAT-PROF-001 | BR-012 | CAP-01, CAP-06 | T: gender/category combinations. |
| FR-PROF-007 | FEAT-PROF-001, FEAT-PERS-003 | BR-012, BR-021 | CAP-01, CAP-05, CAP-06 | T: subsequent request after removal. |
| FR-PROF-008 | FEAT-AUTH-001, FEAT-PROF-001 | BR-012, BR-021 | CAP-01, CAP-06 | D: skip and return. |
| FR-WEATHER-001 | FEAT-PROF-002 | BR-013 | CAP-01, CAP-05, CAP-07 | T: grant, deny, skip. |
| FR-WEATHER-002 | FEAT-PROF-002 | BR-013 | CAP-01, CAP-05, CAP-07 | T: manual city after denial. |
| FR-WEATHER-003 | FEAT-PROF-002 | BR-007, BR-013 | CAP-01, CAP-05, CAP-07 | T: selected-location response. |
| FR-WEATHER-004 | FEAT-PROF-002, FEAT-OUT-001 | BR-007, BR-022 | CAP-01, CAP-05, CAP-06, CAP-07 | I/T: context matches evaluation. |
| FR-WEATHER-005 | FEAT-PROF-002, FEAT-OUT-001 | BR-007, BR-022 | CAP-01, CAP-05, CAP-06, CAP-07 | T: changed-location assessment. |
| FR-AI-001 | FEAT-AI-001 | BR-001 | CAP-02, CAP-04 | T: capture to selected-image state. |
| FR-AI-002 | FEAT-AI-001 | BR-001 | CAP-02, CAP-04 | T: gallery to selected-image state. |
| FR-AI-003 | FEAT-AI-002 | BR-004 | CAP-02, CAP-03, CAP-04 | T: imageless/manual completion. |
| FR-AI-004 | FEAT-AI-001 | BR-001 | CAP-02, CAP-04 | T: replace/cancel selected image. |
| FR-AI-005 | FEAT-AI-001 | BR-001, BR-022 | CAP-02, CAP-04 | T: delayed analysis state. |
| FR-AI-006 | FEAT-AI-001 | BR-001, BR-003 | CAP-02, CAP-04 | T: successful analysis output. |
| FR-AI-007 | FEAT-AI-002 | BR-002 | CAP-02, CAP-03, CAP-04 | T: category/color correction. |
| FR-AI-008 | FEAT-AI-002 | BR-002, BR-005 | CAP-02, CAP-03, CAP-04 | T: confirm versus draft. |
| FR-AI-009 | FEAT-AI-001, FEAT-AI-002 | BR-001, BR-002 | CAP-02, CAP-03, CAP-04 | T: canceled draft absent. |
| FR-AI-010 | FEAT-AI-002 | BR-005 | CAP-02, CAP-03, CAP-04 | T: successful/failed-save retry. |
| FR-AI-011 | FEAT-AI-001 | BR-001, BR-022 | CAP-02, CAP-04 | I/D: image-entry guidance. |
| FR-GAR-001 | FEAT-GAR-001 | BR-003 | CAP-04 | T: approved categories only. |
| FR-GAR-002 | FEAT-GAR-001, FEAT-AI-002 | BR-003, BR-002 | CAP-02, CAP-03, CAP-04 | T: subtype fallback saves. |
| FR-GAR-003 | FEAT-AI-002, FEAT-GAR-001 | BR-002, BR-003 | CAP-02, CAP-03, CAP-04 | T: minimum-only profile. |
| FR-GAR-004 | FEAT-GAR-001, FEAT-OUT-001 | BR-003, BR-009 | CAP-04, CAP-05, CAP-06 | T/A: incomplete versus ready fixtures. |
| FR-GAR-005 | FEAT-GAR-001, FEAT-OUT-001 | BR-003, BR-009 | CAP-04, CAP-05, CAP-06 | A: readiness fixtures; OSQ-005. |
| FR-GAR-006 | FEAT-GAR-001 | BR-003 | CAP-04 | T: optional-descriptor cases. |
| FR-GAR-007 | FEAT-GAR-001 | BR-003 | CAP-04 | T/I: shape/layer corrections. |
| FR-GAR-008 | FEAT-GAR-001 | BR-003 | CAP-04 | T/I: color review. |
| FR-GAR-009 | FEAT-GAR-001 | BR-003 | CAP-04 | T/I: pattern/context corrections. |
| FR-GAR-010 | FEAT-GAR-001 | BR-003 | CAP-04 | T: uncertain material case. |
| FR-GAR-011 | FEAT-GAR-001, FEAT-AI-002 | BR-002 | CAP-02, CAP-03, CAP-04 | T: analysis after correction. |
| FR-WAR-001 | FEAT-WAR-001 | BR-005 | CAP-03, CAP-07 | T: browse and imageless detail. |
| FR-WAR-002 | FEAT-WAR-001, FEAT-ANL-001 | BR-005, BR-006 | CAP-03, CAP-06, CAP-07 | I: readiness and usage labels. |
| FR-WAR-003 | FEAT-WAR-002 | BR-002, BR-005 | CAP-03, CAP-04 | T: edit/confirm/cancel. |
| FR-WAR-004 | FEAT-WAR-002 | BR-005 | CAP-03, CAP-04 | T: remove/confirm/cancel. |
| FR-WAR-005 | FEAT-WAR-002 | BR-005 | CAP-03, CAP-04 | T/A: post-removal evaluations. |
| FR-WAR-006 | FEAT-WAR-002 | BR-005 | CAP-03, CAP-04 | T: affected assessment state. |
| FR-WAR-007 | FEAT-WAR-003 | BR-005 | CAP-03 | T: match, filter, clear. |
| FR-WAR-008 | FEAT-WAR-001, FEAT-WAR-003 | BR-005 | CAP-03, CAP-07 | T: empty/no-match/failure states. |
| FR-OUT-001 | FEAT-OUT-001 | BR-007 | CAP-05, CAP-06 | T/A: owned/ready fixture. |
| FR-OUT-002 | FEAT-OUT-001 | BR-007, BR-010 | CAP-05, CAP-06 | T/A: context variation. |
| FR-OUT-003 | FEAT-OUT-001 | BR-009 | CAP-05, CAP-06 | A: invalid high-preference fixture. |
| FR-OUT-004 | FEAT-OUT-001 | BR-007, BR-009 | CAP-05, CAP-06 | A: domain fixtures; OSQ-006. |
| FR-OUT-005 | FEAT-OUT-001 | BR-007, BR-009, BR-010, BR-011, BR-012 | CAP-05, CAP-06 | A: controlled ranking differences. |
| FR-OUT-006 | FEAT-OUT-001 | BR-008 | CAP-05, CAP-06 | T/A: three-valid fixture. |
| FR-OUT-007 | FEAT-OUT-001 | BR-008 | CAP-05, CAP-06 | A: reordered duplicates. |
| FR-OUT-008 | FEAT-OUT-001 | BR-008, BR-022 | CAP-05, CAP-06 | T: two, one, zero valid. |
| FR-OUT-009 | FEAT-OUT-001 | BR-007, BR-022 | CAP-05, CAP-06 | T: stale/failed refresh. |
| FR-OUT-010 | FEAT-OUT-002 | BR-007 | CAP-04, CAP-05 | D: outfit to garment detail. |
| FR-OUT-011 | FEAT-OUT-002 | BR-022 | CAP-04, CAP-05 | I/A: explanation matches fixture. |
| FR-OUT-012 | FEAT-OUT-002 | BR-022 | CAP-04, CAP-05 | I: unavailable-context reasoning. |
| FR-OUT-013 | FEAT-OUT-002 | BR-022 | CAP-04, CAP-05 | T/I: three presentation contexts. |
| FR-OUT-014 | FEAT-OUT-003 | BR-008 | CAP-05 | T: fixed other identities. |
| FR-OUT-015 | FEAT-OUT-003 | BR-007, BR-009 | CAP-05 | A: compatible replacement. |
| FR-OUT-016 | FEAT-OUT-003 | BR-008, BR-022 | CAP-05 | T: no-alternative fixture. |
| FR-OUT-017 | FEAT-OUT-003 | BR-008 | CAP-05 | T: interaction isolation. |
| FR-PERS-001 | FEAT-PERS-001 | BR-011 | CAP-06 | T: Like state. |
| FR-PERS-002 | FEAT-PERS-001 | BR-011 | CAP-06 | T: Dislike effect and eligibility. |
| FR-PERS-003 | FEAT-PERS-001 | BR-011, BR-021 | CAP-06 | T: revise/clear feedback. |
| FR-PERS-004 | FEAT-PERS-001 | BR-011 | CAP-06 | T: failed versus recorded signal. |
| FR-PERS-005 | FEAT-PERS-001 | BR-011 | CAP-06 | T: history unchanged. |
| FR-WEAR-001 | FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | T: accepted report. |
| FR-WEAR-002 | FEAT-PERS-002 | BR-011 | CAP-06, CAP-07 | T: same-day different outfits. |
| FR-WEAR-003 | FEAT-PERS-002 | BR-011 | CAP-06, CAP-07 | T: intentional same-outfit repeat. |
| FR-WEAR-004 | FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | T/I: event date/time and meaning. |
| FR-WEAR-005 | FEAT-PERS-002 | BR-011, BR-022 | CAP-06, CAP-07 | T: available/missing context. |
| FR-WEAR-006 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | T: event-specific correction. |
| FR-WEAR-007 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | T: individual removal. |
| FR-WEAR-008 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | T: underlying objects remain. |
| FR-WEAR-009 | FEAT-PERS-002 | BR-011 | CAP-06, CAP-07 | T: duplicate versus separate intent; OSQ-008. |
| FR-WEAR-010 | FEAT-PERS-002, FEAT-ANL-001, FEAT-PERS-003 | BR-006, BR-011 | CAP-05, CAP-06, CAP-07 | T/A: corrected/removed signal. |
| FR-WEAR-011 | FEAT-PERS-002, FEAT-PERS-003 | BR-011, BR-009 | CAP-05, CAP-06, CAP-07 | T/A: invalid/hypothetical attempts. |
| FR-WEAR-012 | FEAT-PERS-002 | BR-006, BR-021 | CAP-06, CAP-07 | T: cancel correction/removal; original event remains. |
| FR-PERS-006 | FEAT-PERS-003, FEAT-PROF-001 | BR-010 | CAP-01, CAP-05, CAP-06 | A: no-history preference fixture. |
| FR-PERS-007 | FEAT-PERS-003, FEAT-PERS-001 | BR-011 | CAP-05, CAP-06 | A: controlled feedback comparison. |
| FR-PERS-008 | FEAT-PERS-003, FEAT-PERS-002 | BR-011 | CAP-05, CAP-06, CAP-07 | A: signal comparison; OSQ-007. |
| FR-PERS-009 | FEAT-PERS-003, FEAT-ANL-001 | BR-011, BR-006 | CAP-05, CAP-06, CAP-07 | A: valid variety/recency fixture. |
| FR-PERS-010 | FEAT-PERS-003, FEAT-PROF-001 | BR-012 | CAP-01, CAP-05, CAP-06 | T/A: optional context and eligibility. |
| FR-PERS-011 | FEAT-PERS-003, FEAT-OUT-001 | BR-009, BR-011 | CAP-05, CAP-06 | A: constrained/no-alternative fixture. |
| FR-PERS-012 | FEAT-PERS-003, FEAT-PERS-002 | BR-011 | CAP-05, CAP-06, CAP-07 | A: repetition cases; OSQ-007. |
| FR-PERS-013 | FEAT-PERS-003, FEAT-PERS-001, FEAT-PERS-002 | BR-011, BR-012 | CAP-05, CAP-06, CAP-07 | T/A: signal revision. |
| FR-ANL-001 | FEAT-ANL-001, FEAT-PERS-002 | BR-006 | CAP-06, CAP-07 | T/I: dated event list. |
| FR-ANL-002 | FEAT-ANL-001, FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | T: multiple-event history. |
| FR-ANL-003 | FEAT-ANL-001 | BR-006, BR-011 | CAP-06, CAP-07 | A: known event history. |
| FR-ANL-004 | FEAT-ANL-001 | BR-006, BR-022 | CAP-06, CAP-07 | I: logging limitations. |
| FR-ANL-005 | FEAT-ANL-001 | BR-006, BR-022 | CAP-06, CAP-07 | T: empty history. |
| FR-ANL-006 | FEAT-ANL-001 | BR-006, BR-022 | CAP-06, CAP-07 | T/I: history after garment removal. |
| FR-ANL-007 | FEAT-ANL-001, FEAT-PERS-002 | BR-006, BR-011 | CAP-06, CAP-07 | T/A: event lifecycle effects. |
| FR-ANL-008 | FEAT-ANL-002, FEAT-PROF-001 | BR-014, BR-010 | CAP-01, CAP-06, CAP-07, CAP-08 | A: differing-needs fixture. |
| FR-ANL-009 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | I/A: assessment context. |
| FR-ANL-010 | FEAT-ANL-002, FEAT-PROF-001 | BR-014, BR-010 | CAP-01, CAP-06, CAP-07, CAP-08 | A: same wardrobe, different needs. |
| FR-ANL-011 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | T/A: sufficient/insufficient data. |
| FR-ANL-012 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | I/A: assessed need evidence. |
| FR-ANL-013 | FEAT-ANL-002 | BR-014 | CAP-07, CAP-08 | A: post-removal baseline. |
| FR-ANL-014 | FEAT-ANL-002 | BR-014, BR-022 | CAP-07, CAP-08 | T: changed assessment context. |
| FR-ANL-015 | FEAT-ANL-002 | BR-014, BR-022, BR-024 | CAP-07, CAP-08 | I/T: limitation language. |
| FR-GAP-001 | FEAT-GAP-001 | BR-015 | CAP-07, CAP-08 | A: capability-based gap fixture. |
| FR-GAP-002 | FEAT-GAP-001 | BR-015, BR-022 | CAP-07, CAP-08 | I: gap versus product wording. |
| FR-GAP-003 | FEAT-GAP-001 | BR-015 | CAP-07, CAP-08 | D: gap to utility evaluation. |
| FR-GAP-004 | FEAT-GAP-001 | BR-014, BR-022, BR-024 | CAP-07, CAP-08 | T: no-gap/insufficient states. |
| FR-GAP-005 | FEAT-GAP-001 | BR-024 | CAP-07, CAP-08 | D: commerce-independent path. |
| FR-MULT-001 | FEAT-MULT-001 | BR-015, BR-016 | CAP-08, CAP-09 | A: owned baseline and candidate. |
| FR-MULT-002 | FEAT-MULT-001 | BR-009, BR-016 | CAP-08, CAP-09 | A: consistent-context comparison. |
| FR-MULT-003 | FEAT-MULT-001 | BR-016 | CAP-08, CAP-09 | A: known set differences. |
| FR-MULT-004 | FEAT-MULT-001 | BR-016, BR-022 | CAP-08, CAP-09 | I/A: illustrative 41 to 58 gives +17. |
| FR-MULT-005 | FEAT-MULT-001 | BR-017 | CAP-08, CAP-09 | T/I: candidate in valid new previews. |
| FR-MULT-006 | FEAT-MULT-001 | BR-015, BR-017 | CAP-08, CAP-09 | T: no ingestion/wear side effect. |
| FR-MULT-007 | FEAT-MULT-001 | BR-016, BR-022 | CAP-08, CAP-09 | T/A: zero/unavailable/incomplete. |
| FR-MULT-008 | FEAT-MULT-001 | BR-016, BR-022 | CAP-08, CAP-09 | T: changed-baseline result. |
| FR-SHOP-001 | FEAT-SHOP-001 | BR-018, BR-020 | CAP-08, CAP-09, CAP-10 | I: required candidate information. |
| FR-SHOP-002 | FEAT-SHOP-001, FEAT-MULT-001 | BR-016, BR-017, BR-018 | CAP-08, CAP-09, CAP-10 | T/I: utility explanation and previews. |
| FR-SHOP-003 | FEAT-SHOP-001 | BR-020 | CAP-08, CAP-09, CAP-10 | I: credible and missing optional fields. |
| FR-SHOP-004 | FEAT-SHOP-001 | BR-020, BR-024 | CAP-08, CAP-09, CAP-10 | T/I: optional-data omissions. |
| FR-SHOP-005 | FEAT-SHOP-002 | BR-018 | CAP-10 | T: available link handoff. |
| FR-SHOP-006 | FEAT-SHOP-002 | BR-018, BR-022 | CAP-10 | T: failed link and return. |
| FR-SHOP-007 | FEAT-SHOP-002 | BR-018, BR-019 | CAP-10 | T/I: navigation side effects. |
| FR-SHOP-008 | FEAT-SHOP-001, FEAT-SHOP-002 | BR-018, BR-024 | CAP-08, CAP-09, CAP-10 | D/I: independent core journey. |
| FR-MET-001 | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001 | BR-019, BR-001, BR-007 | CAP-02, CAP-04, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | T/I: distinct interaction evidence. |
| FR-MET-002 | FEAT-MET-001, FEAT-PERS-002 | BR-019, BR-011 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | T/I: event lifecycle evidence. |
| FR-MET-003 | FEAT-MET-001, FEAT-SHOP-002 | BR-019 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | T/I: exposure versus navigation. |
| FR-MET-004 | FEAT-MET-001, FEAT-AI-001, FEAT-OUT-001, FEAT-ANL-001 | BR-019, BR-001, BR-007, BR-006 | CAP-02, CAP-04, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | A/I: approved metric evidence. |
| FR-MET-005 | FEAT-MET-001, FEAT-PERS-002 | BR-019, BR-011 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | T: duplicate versus intentional activity. |
| FR-MET-006 | FEAT-MET-001 | BR-021 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | I: measurement information. |
| FR-MET-007 | FEAT-MET-001 | BR-019 | CAP-02, CAP-05, CAP-06, CAP-07, CAP-08, CAP-09, CAP-10 | T: measurement dependency failure. |

### 10.3 PRD Feature Coverage Audit

All 22 MVP features are covered. The functional ID set is exhaustive for each feature; the final column gives representative supporting requirements rather than duplicating every cross-cutting source row.

| PRD Feature / Name | Functional Coverage | Supporting Examples | Result |
|---|---|---|---|
| FEAT-AUTH-001 — Account access and continuity | FR-AUTH-001–FR-AUTH-012; FR-PROF-001, FR-PROF-008 | DATA-AUTH-001, UI-002, SI-003, COM-001, NFR-SEC-001 | Covered — MVP |
| FEAT-PROF-001 — Preferences and optional profile | FR-AUTH-011; FR-PROF-001–FR-PROF-008; FR-PERS-006, FR-PERS-010; FR-ANL-008, FR-ANL-010 | DATA-AUTH-003, UI-001, NFR-PRIV-001, NFR-MNT-001, LOC-002 | Covered — MVP |
| FEAT-PROF-002 — Location/environmental context | FR-PROF-001; FR-WEATHER-001–FR-WEATHER-005 | SI-002, HW-003, NFR-PRIV-001, NFR-INT-001, ERR-WEATHER-001 | Covered — MVP |
| FEAT-AI-001 — Image entry and preview | FR-AI-001–FR-AI-002, FR-AI-004–FR-AI-006, FR-AI-009, FR-AI-011; FR-MET-001, FR-MET-004 | DATA-GAR-004, SI-001, HW-001, NFR-PERF-001, NFR-REL-001 | Covered — MVP |
| FEAT-AI-002 — Confirmation/manual entry | FR-AI-003, FR-AI-007–FR-AI-010; FR-GAR-002–FR-GAR-003, FR-GAR-011 | DATA-GAR-003, DATA-INT-001, UI-005, SI-001, HW-001 | Covered — MVP |
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

BR-001–BR-024 are all accounted for by software requirements. Conditional BR-020 information remains conditional on credible supporting information; this does not defer mandatory candidate utility.

| BRD Requirement | Direct Functional Coverage | Result / Relevant Boundary |
|---|---|---|
| BR-001 | FR-AI-001–FR-AI-002, FR-AI-004–FR-AI-006, FR-AI-009, FR-AI-011; FR-MET-001, FR-MET-004 | Covered — direct software behavior. |
| BR-002 | FR-AI-007–FR-AI-009; FR-GAR-002–FR-GAR-003, FR-GAR-011; FR-WAR-003 | Covered — direct software behavior. |
| BR-003 | FR-AI-006; FR-GAR-001–FR-GAR-010 | Covered — direct software behavior. |
| BR-004 | FR-AI-003 | Covered — direct software behavior. |
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
| BR-021 | FR-AUTH-001–FR-AUTH-012; FR-PROF-007–FR-PROF-008; FR-PERS-003; FR-WEAR-006–FR-WEAR-008, FR-WEAR-012; FR-MET-006 | Covered — direct software behavior. |
| BR-022 | FR-WEATHER-004–FR-WEATHER-005; FR-AI-005, FR-AI-011; FR-OUT-008–FR-OUT-009, FR-OUT-011–FR-OUT-013, FR-OUT-016; FR-WEAR-005; FR-ANL-004–FR-ANL-006, FR-ANL-009, FR-ANL-011–FR-ANL-012, FR-ANL-014–FR-ANL-015; FR-GAP-002, FR-GAP-004; FR-MULT-004, FR-MULT-007–FR-MULT-008; FR-SHOP-006 | Covered — direct software behavior. |
| BR-023 | FR-PROF-001 | Covered — Android/iOS and Vietnamese/localization also specified by LOC-* and Section 2. |
| BR-024 | FR-ANL-015; FR-GAP-004–FR-GAP-005; FR-SHOP-004, FR-SHOP-008 | Covered — core wardrobe value remains independent of commerce. |

### 10.5 Capability Continuity Audit

The capabilities below retain their BRD identity and PRD feature links. Feature coverage above supplies the applicable functional/supporting SRS requirements; no new capability is introduced.

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

Coverage audit totals: **140 functional requirements; 22/22 features; 24/24 business requirements; 10/10 capabilities**. BG-01–BG-04 remain traceable through the unchanged BRD mappings. Verification methods are planned evidence, not recorded test passes.

## 11. Glossary

| Term | Meaning |
|---|---|
| AI Suggested | Provenance of a proposed automated value that has not become authoritative merely through prediction. |
| User Confirmed | Provenance of a value explicitly reviewed/accepted by the user. |
| User Corrected | Provenance of a value changed by the user from a suggestion/prior value and confirmed. |
| User Entered | Provenance of manually supplied information; the entry becomes authoritative through confirmation. |
| High Confidence / Needs Review / Uncertain | Approved user-facing prediction-confidence meanings; localized labels do not change these semantics. Confidence does not replace confirmation. |
| Garment | Individually distinguishable clothing item in one supported primary category; active ownership differs from removed historical representation. |
| Garment Profile | Authoritative user-confirmed garment information plus supported optional descriptors and relevant provenance/uncertainty. |
| Saveable Garment | Entry with user-confirmed Primary Category and Dominant Color; neither an image nor every rich attribute is mandatory. |
| Recommendation-Ready Garment | Entry with sufficient applicable compatibility information for a particular recommendation use; exact validation/derivation rules remain to be refined. |
| Outfit | Combination of identified garments evaluated under a context. Reordering the same identities does not create another unique outfit. |
| Outfit Recommendation | Valid outfit proposed from current owned garments with relevant context and understandable reasons. |
| Hard Validity Constraint | Domain condition that excludes an invalid combination before ranking, such as required roles, feasible layering, weather/season, or strong pattern conflict. |
| Soft Personalized Ranking | Preference-based ordering of already-valid candidates; it cannot convert an invalid outfit into a valid one. |
| Current Occasion | Occasion for a particular recommendation request, drawn from the approved occasion vocabulary. |
| Common Occasion Needs | Ongoing clothing needs/priorities informing the Personalized Everyday Capsule; distinct from current request occasion. |
| Feedback | Like/Dislike preference evidence that may be revised/cleared; it is not a Wear Event. |
| Wear Event | One intentional user-reported wearing instance with selected outfit, local date/time, and context/occasion when available. Multiple events, including repeated outfit use, may occur in a day. |
| Wear History | Time-aware accepted-event information and understandable historical snapshots; it reflects incomplete user reporting rather than verified physical wear. |
| Accidental Duplicate | Reprocessing/retry/repeated tap of one logical action, distinguished from a separate intentional Wear Event. |
| Removed from wardrobe | Historical indication that the garment is no longer current owned inventory; it is excluded from new/current calculations. |
| Personalized Everyday Capsule | One contextual model informed by common need priorities, style, climate/context, and current confirmed wardrobe. |
| Wardrobe Coverage Score | Explainable assessment of how the current wardrobe serves the relevant capsule needs, with covered/underserved areas and limitations. |
| Wardrobe Gap | Evidence-supported underserved wardrobe capability; it is not a universally compulsory missing product. |
| Candidate Garment | Potential addition evaluated hypothetically; evaluation, preview, or link opening does not establish ownership. |
| Wardrobe Multiplier | Incremental number of unique valid outfits newly enabled by a candidate under the same evaluation context, displayed as +N New Outfits. |
| Hypothetical Outfit | Preview combining the unowned candidate with current garments, clearly distinguished from a wearable owned-outfit recommendation. |
| Strategic Shopping | Utility-led candidate advice grounded in a gap, incremental outfits, reasons, and previews; commercial fields/destinations are optional when credible. |
| Outdated Assessment | Result whose relevant wardrobe, candidate, profile, or context no longer matches current inputs; distinct from fresh advice or unavailable evaluation. |

## 12. Open Software Questions

The current sources leave the items below unresolved at software acceptance/rule level. They do not reopen OPQ-001–OPQ-010 or OBQ-001/OBQ-002. Approved platform/language, email/password/JWT direction, vocabularies, saveable minimum, confidence/provenance labels, multiple Wear Events, candidate information, and Personalized Everyday Capsule remain settled.

Questions about domain invariants/formulas/policies are inputs to the next Business Rules artifact. Answers must refine the affected SRS acceptance details without changing business/product meaning. Architecture mechanisms are selected later rather than guessed here.

| ID | Question | Why It Matters / Source of Uncertainty | Responsible Decision Area | Must Be Resolved Before |
|---|---|---|---|---|
| OSQ-001 | What supported image formats/limits, image qualifications, category/color ground truth, sample sufficiency, preview-usability criteria, timing start/end boundaries, reference device/network/workload, aggregation, and tolerance make MET-Q01–MET-Q03 acceptance reproducible? | BRD Section 19 and PRD Sections 12/25 approve targets but defer conditions/methods. Preserve at least 90% separately, approximately 3–5 seconds, and below approximately 3 seconds including 100+ items; do not substitute different targets. | QA + AI/Engineering + Product Owner | Final quantitative acceptance of NFR-PERF-*, AI-REQ-010/011, and NFR-USE-002; later quality scenarios. |
| OSQ-002 | What exact access/refresh lifetimes and renewal, rotation/reuse, revocation, and logout-invalidated-access outcomes are required by security policy? | PRD Section 10.1 defers these. The short-lived JWT Access Token / longer-lived Refresh Token constraint and immediate end of user-facing personal access on logout remain settled. Storage, signing, key management, and revocation mechanisms remain design work. | Engineering/security + Product Owner | Final access-security acceptance and later security/architecture decisions. |
| OSQ-003 | What password validity policy, email identity/registration-conflict rules, reset interaction validity/expiry/reuse rules, and account-disclosure-safe recovery messages apply? | PRD Section 10.1 defines access/recovery but leaves detailed validation/security policy downstream. | Engineering/security + QA + Product Owner | Authentication/recovery acceptance scenarios and applicable Business Rules. |
| OSQ-004 | How do per-attribute prediction evidence/thresholds map to High Confidence, Needs Review, and Uncertain, including unavailable/conflicting evidence? | PRD Sections 12–13 settle labels/authority, not exact thresholds or inference mapping. No user-facing numerical threshold is invented. | AI/Engineering + QA + Product Owner | AI uncertainty acceptance and applicable Business Rules. |
| OSQ-005 | What exact applicable readiness/derived-information checks and remaining descriptor vocabularies apply to pattern, layering/bulk/fit, season, and color meaning? | PRD Sections 13.1–13.2 distinguish saving/readiness but defer exact validators/derivation/scales. Approved primary/subtype/style/occasion vocabularies and optional fields stay unchanged. | Domain/Requirements + AI/Engineering + QA | Business Rules and detailed readiness/garment acceptance fixtures. |
| OSQ-006 | What exact required-role, layering, weather/season, and strong pattern/compatibility constraints define a valid outfit under each supported context? | BRD Section 13.3 and PRD Section 15 defer detailed rules/thresholds. Hard validity before soft ranking is settled; legacy fixed heuristics are not approved defaults. | Domain/Requirements + Product Owner + QA | Business Rules; final recommendation/Shuffle/multiplier expected-result fixtures. |
| OSQ-007 | What feedback targeting, history windows, recency/diversity criteria, and signal normalization bound artificial repetition while preserving legitimate events and stronger Wear-versus-Like meaning? | PRD Sections 16–17/31 defer weights/windows/normalization. Active lightweight personalization and validity dominance remain settled. | Domain/Requirements + Product Owner + AI/Engineering + QA | Business Rules and controlled personalization acceptance. |
| OSQ-008 | How are a single logical Wear action and a separate intentional repeat distinguished; which local timezone/travel, correction, and date/time validity policies apply? | PRD Sections 16.2/17.1 defer duplicate/correction/time semantics. Same-day and same-outfit intentional events are permitted; date-plus-outfit deduplication cannot erase them. Idempotency mechanisms remain design detail. | Domain/Requirements + Engineering + QA + Product Owner | Wear-related Business Rules and duplicate/history acceptance scenarios. |
| OSQ-009 | What need-to-occasion mappings, priority/weighting, score scale/formula, evidence criteria, and sufficient-data rules determine contextual coverage and capability gaps? | PRD Sections 11.3/17.2/18 defer mappings/weights/formulas. One contextual Personalized Everyday Capsule and no manufactured purchase need are settled. | Domain/Requirements + Product Owner + QA | Business Rules and coverage/gap expected-result fixtures. |
| OSQ-010 | What evaluation sufficiency/completeness and freshness policies permit an exact +N, evaluated zero, unavailable/incomplete, or outdated multiplier result? | PRD Section 19 defers operational counting/completeness/freshness detail. Incremental unique valid outfit meaning and consistent context are settled; the implementation algorithm is later design. | Domain/Requirements + Engineering + QA | Business Rules and final multiplier completeness/freshness acceptance. |
| OSQ-011 | What consent, authorized access/deletion/sharing boundaries and retention periods apply to images, profiles, behavior, historical snapshots, and measurement information? | PRD Section 23 and BRD Section 24 defer detailed policy; no unrestricted training permission or legal period is established. Preserve effective individual Wear removal and understandable removed-garment history. | Product Owner + privacy/legal review + Engineering/security | Final privacy/data acceptance and any binding legal/security architecture constraints. |
| OSQ-012 | What weather freshness, dependency outcome/time-limit criteria, candidate-information credibility/qualification, and known destination-unavailability rules define usable versus unavailable external information? | PRD Sections 11.2/20–21/25 defer provider freshness/credible-source/operating details. No merchant partnership, guaranteed stock, or retailer-response control is assumed. | Product Owner + Engineering + QA | Interface/failure acceptance; relevant Business Rules and later provider decisions. |
| OSQ-013 | Which Android/iOS versions/devices and nominal operating conditions are supported, and what availability, recovery, capacity/concurrency, or larger-workload targets are required if any? | BRD DEP-006 and PRD Section 25 establish mobile/reliable-use direction but no OS matrix, service percentage, recovery interval, or concurrency value. | Engineering + QA + Product Owner | Final operating-quality acceptance and later Quality Attribute Analysis/ASR. |
| OSQ-014 | What representative-user tasks, usability criteria, and Android/iOS assistive-interaction scenarios define acceptance of understandable/inclusive core journeys? | BRD stakeholder usability/accessibility expectations and PRD Section 25 supply direction without a conformance level or quantified evaluation threshold. | UX/Product + QA + Product Owner | Final usability/accessibility acceptance; later applicable quality scenarios. |

Resolved answers must identify affected stable SRS IDs and verification evidence. A newly discovered change to business/product intent must follow upstream change control rather than be introduced through a local answer.

## 13. SRS Exit Criteria

This Baseline Draft is ready for baseline review and Business Rules derivation when the Product Owner, with Requirements, Engineering, QA, and relevant UX/AI/security review, confirms the following:

- All 22 MVP FEAT-* have direct functional coverage; BR-001–BR-024 and CAP-01–CAP-10 remain accounted for.
- Required core behavior and representative failures are expressed as source-linked, stable, verifiable requirements.
- Logical ownership, authority, history, effective deletion, and integrity are defined without physical schema design.
- External interfaces describe information/outcomes, permission alternatives, and failures without selecting internal topology, vendors, endpoints, or payload schemas.
- Approved recognition/timing/privacy/utility targets are retained; incomplete acceptance parameters are explicitly linked to OSQ-*.
- Settled product decisions, MVP exclusions, optional information, and commerce-independent value are preserved.
- Remaining domain rules are clearly assigned to Business Rules; there is no hidden product ambiguity preventing that artifact from being drafted.
- Requirement-level verification methods, reverse coverage audits, and source references are complete.
- Architecture, formal behavioral models, and detailed quality scenarios remain downstream.
- Review decisions and subsequent refinements are recorded through revision control without renumbering existing upstream or SRS IDs.

The document records requirement coverage and a planned verification approach, not implemented behavior, passed tests, or formal approval. Rule-dependent expected results and TBD quantitative/security/operating acceptance criteria must be resolved by their stated gates before final requirement acceptance. Those explicit refinement needs do not reopen the business/product baseline or prevent preparation of Business Rules.

## 14. Next Artifact

The next artifact is **`docs/03-requirements/business-rules.md`**, following Workflow Section 9. It will own detailed invariants, compatibility/readiness rules, formulas, counting semantics, feedback/event policies, and applicable validation policies, while referencing stable FR/DATA/AI/ERR and upstream FEAT/BR/CAP IDs.

Formal Use Cases will then refine actor–system interactions. Activity Diagrams will use PlantUML according to the workflow. Later Quality Attribute Analysis will examine measurable scenarios and architectural significance; ASR, ADD, and ADR remain downstream owners of significant requirements, views, and design decisions.

No Business Rules, Use Cases, Activity/Sequence/State diagrams, ERD, quality-analysis artifact, ASR, ADD, ADR, backlog, or User Stories are created by this SRS task.
