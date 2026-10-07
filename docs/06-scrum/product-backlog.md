# CapsuleAI Product Backlog

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Initial ordered Scrum Product Backlog |
| Version | 0.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Owner / Accountability | Product Owner |
| Product Goal reference | [Product Goal](product-goal.md), Version 0.1.1, Baseline Draft |
| Estimates | Not Estimated — applies to every PBI |
| Revision note | Initial globally ordered MVP backlog derived from the current canonical baseline; no upstream scope or decision change. |

## 1. Purpose

This artifact defines the initial ordered work needed to achieve CapsuleAI's current Product Goal. It represents useful product increments and the enabling and quality work that protects them. It is an evolving Product Backlog: ordering, scope boundaries and refinement depth will change with evidence and team review.

The canonical artifacts below were read before deriving this backlog. Their requirements and decisions remain authoritative; the backlog describes delivery outcomes rather than restating their detailed rules.

| Authority | Canonical source and role |
| --- | --- |
| Process | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), especially Sections 12–14, 28–31, 39–41 and 46: ownership, ordering, refinement and downstream sequence. |
| Business | [BRD](../01-business/BRD.md), v0.3: intent, BG/BR/CAP, personas, value and MVP boundaries. |
| Product | [PRD](../02-product/PRD.md), v0.2: JRN/FEAT, user experience, product behavior and measurement directions. |
| Software requirements | [SRS](../03-requirements/SRS.md), v0.3: functional, data, interface, quality, localization, AI and recovery constraints. |
| Domain invariants | [Business Rules](../03-requirements/business-rules.md), v0.1.2: all 11 rule families and precedence. |
| User goals | [Master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml) and all 23 [Use Case Specifications](../03-requirements/use-cases/), UC-001 through UC-023. |
| Complex flows | All 12 current [Activity Diagrams](../03-requirements/activity-diagrams/): AD-004, AD-006, AD-007, AD-011, AD-012, AD-014, AD-016, AD-017, AD-019, AD-020, AD-021 and AD-022. |
| Scrum ordering anchor | [Product Goal](product-goal.md), v0.1.1: outcomes, value loop, boundaries and existing evidence directions. |

The other repository sources were inspected: the Implementation Summary, proposal, official outline and Business Analysis notes are historical context. Their older quotas, rules, targets, technology descriptions, speculative features and Sprint roadmap do not override the current canonical baseline. No additional current implementation, architecture, testing or operations artifact was present to supply further approved decisions.

## 2. Backlog Management Principles

- **One global order:** Order 1 is currently highest. Section 5 is authoritative across all themes; no separate theme priority or invented prioritization score applies.
- **Stable identity:** PBI IDs identify work permanently. Initial IDs happen to follow initial allocation order; future reordering changes the Order field, never the IDs. Preserve references when splitting, merging or retiring work through refinement.
- **Goal-led value:** Each PBI identifies a Product Goal contribution. Enablers and quality work state which product value or delivery capability they support.
- **Progressive refinement:** Top items have clearer outcomes and boundaries. Refinement states are Candidate, Needs Refinement, Refinable Soon and Ready for Story Refinement. Story readiness does not establish Sprint readiness, approval or commitment.
- **Controlled types:** Product Increment, Enabler, Quality and Research / Validation. This baseline contains no Research / Validation item because no unresolved product uncertainty justifies a separate research outcome.
- **No invented estimates:** Every item is Not Estimated. Delivery-team estimation follows later refinement/planning; existing software latency and retention constraints are not work estimates.
- **Meaningful dependencies:** Section 5 lists hard capability dependencies only. Useful sequencing appears in detail notes and Section 7. A dependency means the named capability is needed to realize or verify the outcome, not that all development must be serial.
- **Source-linked boundaries:** Primary Traceability provides selected anchors in the linked sources. Related UC specifications carry fuller requirement mappings; this document does not replace them. Scope changes require upstream change control and synchronized traceability before they enter delivery.
- **Quality throughout:** Applicable privacy, security, accessibility, localization, integrity and recovery constraints accompany each refined increment. Later verification PBIs extend evidence; their position does not defer required behavior. Measurement failure cannot reverse a core action's actual outcome.
- **Architecture and Scrum timing:** Enabler PBIs reserve necessary delivery capability without selecting providers, data technology, runtime collaboration or topology. Architecture preparation and Sprint commitment follow the workflow's own stages.

## 3. Product Goal Alignment

[Product Goal v0.1.1](product-goal.md) aims to help Indecisive Professionals in Vietnam understand their wardrobe confidently, make daily outfit decisions with less effort and gain useful wear from owned garments. Explainable needs and incremental candidate utility support informed additions. The backlog builds **Understand My Wardrobe → Use My Wardrobe Better → Improve My Wardrobe**, with explicit feedback and reported wear returning into personalization.

| Canonical Goal Contribution | Meaning in this backlog |
| --- | --- |
| Trustworthy Wardrobe Understanding | Establish and maintain confirmed, understandable owned-garment information with manual continuity and assisted entry. |
| Easier Outfit Decisions | Provide distinct valid choices, relevant context, inspectable reasons and controlled alternatives. |
| Greater Owned-Garment Value | Support meaningful feedback, reported use, personalization and visibility into useful/overlooked clothing. |
| Actionable Wardrobe Needs | Explain personalized Coverage and evidence-supported capability gaps. |
| Better-Informed Additions | Explain credible hypothetical candidates, incremental valid outfits, previews and optional guidance/navigation. |
| Enabling / Protecting Product Value | Provide private access, usable delivery, validation, measurement and dependable operation supporting the loop. |

Product increments can expose useful parts of the loop before the complete MVP exists. A partial increment does not redefine the MVP. Candidates remain hypothetical until a separate confirmed owned-garment addition; improvement can also mean better use of existing clothing.

## 4. Backlog Themes

| Theme | Purpose |
| --- | --- |
| Foundation & Access | Reproducible delivery, coherent mobile entry and safe account access/recovery. |
| Personalization & Context | Progressive preferences, inclusive optional profile information and controlled environmental context. |
| Digital Wardrobe | Manual/photo/AI-assisted confirmed entries and accurate, discoverable current possessions. |
| Outfit Decision Support | Hard-valid, context-relevant choices, inspectable detail and single-slot Shuffle. |
| Wear & Learning | Explicit preference state, intentional reported events, repair/withdrawal and active personalization. |
| Wardrobe Intelligence | Reported utilization, contextual Coverage and actionable capability gaps. |
| Strategic Shopping | Credible candidates, exact incremental utility, hypothetical previews and optional external guidance. |
| Quality / Security / Operations | Privacy, measurement, AI validation, performance, accessibility, usability, regression and operability. |

Themes aid reading only. The list below contains one global order: 41 PBIs comprising 28 Product Increments, 5 Enablers and 8 Quality items.

## 5. Initial Ordered Product Backlog

Dependencies in this table are hard capability dependencies; None means no specific prior PBI is required. Cross-cutting source constraints still apply. Runtime availability/sufficiency is checked inside the relevant product outcome, so AI success, weather permission, prior Wear history, a useful candidate and an external destination are not universal participation gates.

| Order | PBI ID | Title | Type | Theme | Goal Contribution | Dependencies | Refinement State |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 1 | PBI-001 | Reproducible engineering and automated-check foundation | Enabler | Foundation & Access | Enabling / Protecting Product Value | None | Refinable Soon |
| 2 | PBI-002 | Protect private user data from the first usable increment | Quality | Quality / Security / Operations | Enabling / Protecting Product Value | None | Refinable Soon |
| 3 | PBI-003 | Usable Vietnamese mobile journey foundation | Enabler | Foundation & Access | Enabling / Protecting Product Value | None | Refinable Soon |
| 4 | PBI-004 | Email/password access and controllable sessions | Product Increment | Foundation & Access | Enabling / Protecting Product Value | PBI-002 | Ready for Story Refinement |
| 5 | PBI-005 | Recover access through email password reset | Product Increment | Foundation & Access | Enabling / Protecting Product Value | PBI-004 | Ready for Story Refinement |
| 6 | PBI-006 | Add a confirmed garment manually | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding | PBI-004 | Ready for Story Refinement |
| 7 | PBI-007 | Browse current wardrobe and inspect owned garments | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding | PBI-004 | Ready for Story Refinement |
| 8 | PBI-008 | Correct and enrich garment information with readiness guidance | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding | PBI-006 | Ready for Story Refinement |
| 9 | PBI-009 | Minimum necessary measurement from first wardrobe use | Enabler | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-002 | Refinable Soon |
| 10 | PBI-010 | Set and revise preferences through progressive onboarding | Product Increment | Personalization & Context | Easier Outfit Decisions; Actionable Wardrobe Needs | PBI-004 | Ready for Story Refinement |
| 11 | PBI-011 | Choose environmental context with manual and limited-context recovery | Product Increment | Personalization & Context | Easier Outfit Decisions; Actionable Wardrobe Needs | PBI-004 | Refinable Soon |
| 12 | PBI-012 | Add a photo-supported garment with manual confirmation | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding | PBI-006 | Refinable Soon |
| 13 | PBI-013 | Review AI-assisted garment proposals before confirmation | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding | PBI-012 | Refinable Soon |
| 14 | PBI-014 | Validate garment-assistance quality on a locked benchmark | Quality | Quality / Security / Operations | Trustworthy Wardrobe Understanding; Enabling / Protecting Product Value | PBI-013 | Refinable Soon |
| 15 | PBI-015 | Find garments through wardrobe search and filters | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding; Greater Owned-Garment Value | PBI-007 | Refinable Soon |
| 16 | PBI-016 | Remove a garment while preserving minimal historical meaning | Product Increment | Digital Wardrobe | Trustworthy Wardrobe Understanding; Greater Owned-Garment Value | PBI-007 | Refinable Soon |
| 17 | PBI-017 | Receive valid daily outfits relevant to declared context | Product Increment | Outfit Decision Support | Easier Outfit Decisions; Greater Owned-Garment Value | PBI-008, PBI-010 | Needs Refinement |
| 18 | PBI-018 | Inspect outfit constituents and explanation | Product Increment | Outfit Decision Support | Easier Outfit Decisions | PBI-017, PBI-007 | Needs Refinement |
| 19 | PBI-019 | Shuffle one garment slot while retaining other choices | Product Increment | Outfit Decision Support | Easier Outfit Decisions; Greater Owned-Garment Value | PBI-017 | Needs Refinement |
| 20 | PBI-020 | Express, revise or clear outfit Like/Dislike | Product Increment | Wear & Learning | Easier Outfit Decisions; Greater Owned-Garment Value | PBI-017 | Needs Refinement |
| 21 | PBI-021 | Report intentional wear and review distinct Wear Events | Product Increment | Wear & Learning | Greater Owned-Garment Value | PBI-017 | Needs Refinement |
| 22 | PBI-022 | Correct one reported Wear Event within its original local day | Product Increment | Wear & Learning | Greater Owned-Garment Value | PBI-021 | Needs Refinement |
| 23 | PBI-023 | Withdraw one Wear Event and remove its effective influence | Product Increment | Wear & Learning | Greater Owned-Garment Value | PBI-021 | Needs Refinement |
| 24 | PBI-024 | Adapt future valid-outfit ranking to feedback and reported use | Product Increment | Wear & Learning | Easier Outfit Decisions; Greater Owned-Garment Value | PBI-020, PBI-021 | Needs Refinement |
| 25 | PBI-025 | Understand reported wardrobe utilization and overlooked items | Product Increment | Wardrobe Intelligence | Greater Owned-Garment Value | PBI-021, PBI-007 | Needs Refinement |
| 26 | PBI-026 | See a defensible personalized Wardrobe Coverage Score | Product Increment | Wardrobe Intelligence | Actionable Wardrobe Needs | PBI-017, PBI-010 | Needs Refinement |
| 27 | PBI-027 | Explore per-need Coverage evidence and useful input revisions | Product Increment | Wardrobe Intelligence | Actionable Wardrobe Needs; Greater Owned-Garment Value | PBI-026 | Needs Refinement |
| 28 | PBI-028 | Explain evidence-supported wardrobe capability gaps | Product Increment | Wardrobe Intelligence | Actionable Wardrobe Needs | PBI-026 | Needs Refinement |
| 29 | PBI-029 | Explore credible hypothetical candidates for supported gaps | Product Increment | Strategic Shopping | Actionable Wardrobe Needs; Better-Informed Additions | PBI-028 | Needs Refinement |
| 30 | PBI-030 | Evaluate candidate Wardrobe Multiplier with honest result states | Product Increment | Strategic Shopping | Better-Informed Additions | PBI-029, PBI-017 | Needs Refinement |
| 31 | PBI-031 | Preview newly enabled hypothetical outfits | Product Increment | Strategic Shopping | Better-Informed Additions | PBI-030 | Needs Refinement |
| 32 | PBI-032 | Show qualified commercial guidance when credible | Product Increment | Strategic Shopping | Better-Informed Additions | PBI-029 | Needs Refinement |
| 33 | PBI-033 | Navigate optionally to an external shopping destination | Product Increment | Strategic Shopping | Better-Informed Additions | PBI-029 | Needs Refinement |
| 34 | PBI-034 | Complete Product Goal evidence across the value loop | Enabler | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-009 | Needs Refinement |
| 35 | PBI-035 | Validate image and daily-outfit performance against the SRS | Quality | Quality / Security / Operations | Trustworthy Wardrobe Understanding; Easier Outfit Decisions; Enabling / Protecting Product Value | PBI-013, PBI-017 | Needs Refinement |
| 36 | PBI-036 | Verify accessible Vietnamese journeys on supported devices | Quality | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-003 | Needs Refinement |
| 37 | PBI-037 | Validate core-task usability with representative users | Quality | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-006, PBI-019, PBI-022, PBI-023, PBI-026, PBI-030 | Needs Refinement |
| 38 | PBI-038 | Prove concurrency and recoverable operation without state corruption | Quality | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-004, PBI-006, PBI-017 | Needs Refinement |
| 39 | PBI-039 | Verify domain invariants and the complete MVP value loop | Quality | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-024, PBI-031, PBI-033 | Needs Refinement |
| 40 | PBI-040 | Deliver repeatable deployment and inspectable operation | Enabler | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-001 | Needs Refinement |
| 41 | PBI-041 | Verify authorization, privacy and retention across the MVP | Quality | Quality / Security / Operations | Enabling / Protecting Product Value | PBI-002, PBI-009, PBI-016, PBI-023 | Needs Refinement |

## 6. Product Backlog Item Details

### PBI-001 — Reproducible engineering and automated-check foundation

**ID:** PBI-001; **Order:** 1; **Type:** Enabler

**Title:** Reproducible engineering and automated-check foundation

**Theme:** Foundation & Access; **Refinement State:** Refinable Soon

**Outcome / Value**

Enable repeatable delivery and early detection of regressions across the connected mobile product.

**Scope Summary**

Establish reproducible builds, controlled configuration, automated checks and a repeatable validation environment for the supported mobile experience and its connected capabilities. Add representative checks as each increment arrives; protect secrets and make results inspectable. Technology choices and implementation begin only after the workflow's intervening architecture and engineering preparation.

**Key Dependencies**

None.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-023; NFR-MNT-001, NFR-TEST-001, NFR-TEST-002, NFR-TEST-003, NFR-SUP-001; Workflow Sections 28, 30 and 46.

**Refinement Notes**

Refine delivery outcomes and evidence needed for the first private wardrobe increment. Separate this ongoing foundation from PBI-039's complete-loop regression evidence and PBI-040's operational readiness; do not select frameworks or deployment topology here.

### PBI-002 — Protect private user data from the first usable increment

**ID:** PBI-002; **Order:** 2; **Type:** Quality

**Title:** Protect private user data from the first usable increment

**Theme:** Quality / Security / Operations; **Refinement State:** Refinable Soon

**Outcome / Value**

Make trustworthy wardrobe participation possible through authorized isolation and purpose-limited use.

**Scope Summary**

Establish private access to each user's images, profile, wardrobe, feedback and history; protect credentials, sessions, transport and secrets. Apply minimum necessary external disclosures and the approved retention distinctions from first collection. MVP personal data is excluded from AI training/improvement, sale, public access and unrestricted sharing.

**Key Dependencies**

None.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-021; CAP-01; FR-AUTH-008; NFR-SEC-001, NFR-SEC-002, NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002; DATA-RET-001, DATA-RET-002, DATA-RET-003; BRULE-AUTH-001, BRULE-AUTH-007.

**Refinement Notes**

Refine the first authenticated-data boundary and its failure behavior. Garment/Wear removal is delivered within PBI-016/PBI-023; measurement lifecycle within PBI-009/PBI-034. PBI-041 provides broader verification evidence rather than postponing these protections.

### PBI-003 — Usable Vietnamese mobile journey foundation

**ID:** PBI-003; **Order:** 3; **Type:** Enabler

**Title:** Usable Vietnamese mobile journey foundation

**Theme:** Foundation & Access; **Refinement State:** Refinable Soon

**Outcome / Value**

Give the Vietnam-first audience a coherent, understandable route through wardrobe value.

**Scope Summary**

Provide the common Vietnamese mobile experience on Android 10+ and iOS 15+, with Today, Wardrobe, Insights and Profile navigation and prominent Add Garment access. Establish meaningful accessible labels/states, scalable text, understandable empty/loading/unavailable messages and localization support that preserves canonical meanings. Integrate each destination as its product capability arrives.

**Key Dependencies**

None.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-023, BR-022; UI-001, UI-002, UI-003, UI-004, UI-005, UI-006, UI-007, UI-008, UI-009; LOC-001, LOC-002, LOC-003, LOC-004, LOC-005; NFR-USE-001, NFR-ACC-001, NFR-ACC-002.

**Refinement Notes**

Refine the first entry and wardrobe routes with authentication/manual entry. This establishes shared usability and accessibility behavior; PBI-036/PBI-037 validate the delivered journeys. Future localization compatibility does not add another MVP UI language.

### PBI-004 — Email/password access and controllable sessions

**ID:** PBI-004; **Order:** 4; **Type:** Product Increment

**Title:** Email/password access and controllable sessions

**Theme:** Foundation & Access; **Refinement State:** Ready for Story Refinement

**Outcome / Value**

Let users establish and resume private access and deliberately end their current session.

**Scope Summary**

Deliver email/password registration, login, session continuity and logout using the established access/refresh policy. Preserve account-safe errors, supported email/password rules, expiry/revocation and reauthentication of interrupted protected journeys. Logout affects the intended current session, with other devices governed by the existing baseline.

**Key Dependencies**

Hard capability dependencies: PBI-002. Useful sequencing: PBI-003; this does not add a hard dependency.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

CAP-01; JRN-01, FEAT-AUTH-001; FR-AUTH-001, FR-AUTH-002, FR-AUTH-006, FR-AUTH-007, FR-AUTH-010; UC-001, UC-002, UC-003; BRULE-AUTH-002, BRULE-AUTH-003, BRULE-AUTH-004, BRULE-AUTH-006.

**Refinement Notes**

Registration, login and logout stay together as one access capability rather than token/layer tasks. Refine normal access, invalid credentials, loss of access and deliberate logout; exact JWT/session constraints remain in the SRS and rules.

### PBI-005 — Recover access through email password reset

**ID:** PBI-005; **Order:** 5; **Type:** Product Increment

**Title:** Recover access through email password reset

**Theme:** Foundation & Access; **Refinement State:** Ready for Story Refinement

**Outcome / Value**

Restore private access safely when a user cannot remember the password.

**Scope Summary**

Deliver Forgot Password, purpose-minimal email instructions, valid reset interaction and return to login. Preserve equivalent non-disclosing initial responses, replacement/expiry/single-use behavior, safe recovery from delivery or uncertain completion, and revocation of all account Refresh Sessions only on completed reset.

**Key Dependencies**

Hard capability dependencies: PBI-004.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

CAP-01; JRN-01, FEAT-AUTH-001; FR-AUTH-003, FR-AUTH-004, FR-AUTH-017, FR-AUTH-018, SI-003, ERR-AUTH-002; UC-004, AD-004; BRULE-AUTH-003, BRULE-AUTH-004, BRULE-AUTH-005.

**Refinement Notes**

Use UC-004 and AD-004 to refine recovery as an end-to-end outcome. An email-delivery provider is a later engineering choice; requesting instructions is not a completed reset.

### PBI-006 — Add a confirmed garment manually

**ID:** PBI-006; **Order:** 6; **Type:** Product Increment

**Title:** Add a confirmed garment manually

**Theme:** Digital Wardrobe; **Refinement State:** Ready for Story Refinement

**Outcome / Value**

Provide useful wardrobe entry immediately, including when no image or AI service is available.

**Scope Summary**

Create an owned garment through imageless manual entry with explicit confirmation of Primary Category and Dominant Color. Support review, correction, cancellation and safe resolution of uncertain saves. Keep Saveable ownership separate from applicable Recommendation-Readiness and explain missing compatibility information without fabricating it.

**Key Dependencies**

Hard capability dependencies: PBI-004.

**Goal Contribution**

Trustworthy Wardrobe Understanding.

**Primary Traceability**

BG-01; BR-002, BR-004; CAP-03, CAP-04; JRN-02, FEAT-AI-002, FEAT-GAR-001; FR-AI-003, FR-GAR-001, FR-GAR-002, FR-GAR-003, FR-GAR-004, FR-GAR-011; UC-007, AD-007; BRULE-GAR-001, BRULE-GAR-002, BRULE-GAR-003.

**Refinement Notes**

The confirmed minimum and manual continuity are already explicit. Refine a minimum-only entry and its inspection after acceptance; rich descriptor enrichment is PBI-008, photo input PBI-012 and AI assistance PBI-013. AI success is never a prerequisite.

### PBI-007 — Browse current wardrobe and inspect owned garments

**ID:** PBI-007; **Order:** 7; **Type:** Product Increment

**Title:** Browse current wardrobe and inspect owned garments

**Theme:** Digital Wardrobe; **Refinement State:** Ready for Story Refinement

**Outcome / Value**

Let users understand their possessions and distinguish accepted inventory from inaccessible or historical data.

**Scope Summary**

Show the current wardrobe, garment detail, accepted profile and applicable readiness guidance, including imageless and incomplete entries. Distinguish a genuinely empty wardrobe, loading and unavailable access/data; offer Add Garment without a quota. Display supported use indicators when reported-use capabilities are available, without claiming physical non-use.

**Key Dependencies**

Hard capability dependencies: PBI-004. Useful sequencing: PBI-006; this does not add a hard dependency.

**Goal Contribution**

Trustworthy Wardrobe Understanding.

**Primary Traceability**

BR-005, BR-006; CAP-03, CAP-04; JRN-03, FEAT-WAR-001; FR-WAR-001, FR-WAR-002, FR-GAR-004; UC-008; BRULE-HIST-001, BRULE-HIST-002.

**Refinement Notes**

Empty browsing does not require existing garments. PBI-006 supplies useful populated inspection examples; search/filtering is PBI-015 and detailed reported-use insight is PBI-025.

### PBI-008 — Correct and enrich garment information with readiness guidance

**ID:** PBI-008; **Order:** 8; **Type:** Product Increment

**Title:** Correct and enrich garment information with readiness guidance

**Theme:** Digital Wardrobe; **Refinement State:** Ready for Story Refinement

**Outcome / Value**

Keep garment knowledge trustworthy and help users supply the information needed for defensible advice.

**Scope Summary**

Review, correct and confirm supported rich descriptors during entry or later editing: bounded subtype, layering/bulk/fit/silhouette, color/palette, pattern/noise, climate, style/occasion and uncertain material. Explain rule-specific readiness and leave optional or unsupported values unknown. Accepted edits preserve user authority and refresh affected advice or mark it outdated.

**Key Dependencies**

Hard capability dependencies: PBI-006.

**Goal Contribution**

Trustworthy Wardrobe Understanding.

**Primary Traceability**

BR-003, BR-002; CAP-04; JRN-02, JRN-03, FEAT-GAR-001, FEAT-WAR-002; FR-GAR-005, FR-GAR-008, FR-GAR-010, FR-GAR-011, FR-WAR-003, FR-WAR-006; UC-007, UC-009; BRULE-GAR-003, BRULE-GAR-005, BRULE-GAR-006, BRULE-GAR-009.

**Refinement Notes**

Keep initial review and later edit aligned to the same vocabularies. Readiness alone does not guarantee outfit validity; not all descriptors become mandatory saving fields. Refine normal edits, unknowns, cancellation and accepted-versus-uncertain outcomes.

### PBI-009 — Minimum necessary measurement from first wardrobe use

**ID:** PBI-009; **Order:** 9; **Type:** Enabler

**Title:** Minimum necessary measurement from first wardrobe use

**Theme:** Quality / Security / Operations; **Refinement State:** Refinable Soon

**Outcome / Value**

Provide early evidence of useful digitization and reliable user actions without creating an analytics product.

**Scope Summary**

Establish privacy-respecting evidence capture for confirmed additions/corrections and progressively arriving recommendation, feedback, Wear and insight interactions. Distinguish exposure, attempt and established accepted outcome. Minimize user-linked content, apply the separate 90-day measurement retention/de-identification limit and preserve core actions when measurement is unavailable.

**Key Dependencies**

Hard capability dependencies: PBI-002. Useful sequencing: PBI-006; this does not add a hard dependency.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-019, BR-021; FEAT-MET-001; FR-MET-001, FR-MET-002, FR-MET-003, FR-MET-005, FR-MET-006, FR-MET-007; DATA-RET-003; Product Goal Section 7.

**Refinement Notes**

Begin with garment entry evidence, then integrate event producers with their owning product PBIs. PBI-034 assembles the complete-loop evidence and interpretation; ordinary engagement evidence does not require raw images or sensitive profile details.

### PBI-010 — Set and revise preferences through progressive onboarding

**ID:** PBI-010; **Order:** 10; **Type:** Product Increment

**Title:** Set and revise preferences through progressive onboarding

**Theme:** Personalization & Context; **Refinement State:** Ready for Story Refinement

**Outcome / Value**

Give first advice a relevant personal basis while keeping optional information and wardrobe setup under user control.

**Scope Summary**

Deliver progressive setup and later profile revision for style, common need priorities and optional body-shape/gender. Separate common occasion needs from the current recommendation occasion; optional information can be skipped, edited or removed and has soft influence only. Offer location choice and first garment entry as those routes become available, without a garment quota or optional-field access gate.

**Key Dependencies**

Hard capability dependencies: PBI-004. Useful sequencing: PBI-003, PBI-006; this does not add a hard dependency.

**Goal Contribution**

Easier Outfit Decisions; Actionable Wardrobe Needs.

**Primary Traceability**

BR-010, BR-012; CAP-01, CAP-06; JRN-01, FEAT-PROF-001; FR-PROF-001, FR-PROF-003, FR-PROF-005, FR-PROF-007; UC-005; BRULE-PROF-001, BRULE-PROF-002.

**Refinement Notes**

Keep initial setup and subsequent profile maintenance in one capability. Refine the six canonical need priorities and optional-field lifecycle from the SRS; missing body/gender never prohibits a garment category. Useful entry routes are PBI-003/PBI-006, rather than hard completion gates.

### PBI-011 — Choose environmental context with manual and limited-context recovery

**ID:** PBI-011; **Order:** 11; **Type:** Product Increment

**Title:** Choose environmental context with manual and limited-context recovery

**Theme:** Personalization & Context; **Refinement State:** Refinable Soon

**Outcome / Value**

Improve context relevance without requiring device-location permission or a successful weather provider.

**Scope Summary**

Set/change location through optional consented device access, independent manual city/location or skip. Identify actual weather/context and timestamps; apply the established freshness and bounded acquisition rules. Disclose missing/stale environment, retain defensible reduced-context journeys and refresh or invalidate affected advice after accepted context changes.

**Key Dependencies**

Hard capability dependencies: PBI-004.

**Goal Contribution**

Easier Outfit Decisions; Actionable Wardrobe Needs.

**Primary Traceability**

BR-007, BR-013; CAP-01, CAP-05; JRN-01, JRN-04, FEAT-PROF-002; FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-003, FR-WEATHER-004, FR-WEATHER-005, SI-002, HW-003; UC-006, AD-006; BRULE-PROF-003, BRULE-OUT-006.

**Refinement Notes**

Refine manual/permission-denied/skip routes and the weather-unavailable boundary. Provider selection and background tracking are not established here; location disclosure is limited to the context request's purpose.

### PBI-012 — Add a photo-supported garment with manual confirmation

**ID:** PBI-012; **Order:** 12; **Type:** Product Increment

**Title:** Add a photo-supported garment with manual confirmation

**Theme:** Digital Wardrobe; **Refinement State:** Refinable Soon

**Outcome / Value**

Make owned clothing recognizable visually while preserving manual entry independently of automated analysis.

**Scope Summary**

Capture or select a garment image through permitted camera/gallery routes, show guidance and validate supported format/size/dimensions. Allow replacement, cancellation or manual continuation and save a photo-supported garment only after user-confirmed minimum information. Retain intelligible image/input failures without adding an unconfirmed item.

**Key Dependencies**

Hard capability dependencies: PBI-006.

**Goal Contribution**

Trustworthy Wardrobe Understanding.

**Primary Traceability**

BG-01; BR-001, BR-004; CAP-02, CAP-03; JRN-02, FEAT-AI-001, FEAT-AI-002; FR-AI-001, FR-AI-002, FR-AI-004, FR-AI-011, FR-AI-012, HW-001, HW-002; UC-007, AD-007.

**Refinement Notes**

This slice adds a useful image-backed manual entry, not an upload-only task. PBI-013 adds processed preview and AI proposals. Refine permission alternatives and image limits from the SRS without requiring a white background.

### PBI-013 — Review AI-assisted garment proposals before confirmation

**ID:** PBI-013; **Order:** 13; **Type:** Product Increment

**Title:** Review AI-assisted garment proposals before confirmation

**Theme:** Digital Wardrobe; **Refinement State:** Refinable Soon

**Outcome / Value**

Reduce repetitive garment entry while keeping the final wardrobe representation under user authority.

**Scope Summary**

From a supported image, present pending analysis, usable processed preview and reviewable supported attribute proposals with confidence and provenance. Let users correct uncertain information and explicitly confirm before saving; never overwrite accepted corrections later. Provide retry/replacement/manual continuation for unavailable analysis or unusable preview.

**Key Dependencies**

Hard capability dependencies: PBI-012. Useful sequencing: PBI-008; this does not add a hard dependency.

**Goal Contribution**

Trustworthy Wardrobe Understanding.

**Primary Traceability**

BG-01; BR-001, BR-002, BR-004; CAP-02, CAP-04; JRN-02, FEAT-AI-001, FEAT-AI-002; FR-AI-005, FR-AI-006, FR-AI-007, FR-AI-008, FR-AI-010, AI-REQ-001, AI-REQ-006, AI-REQ-007; UC-007, AD-007; BRULE-GAR-007, BRULE-GAR-008.

**Refinement Notes**

Review/correction and fallback belong inside this usable assisted-entry slice, not separate AI layer tasks. Preview usability and attribute certainty are separate. PBI-014 supplies validation evidence before assisted quality claims are accepted; PBI-008 supplies rich descriptor enrichment.

### PBI-014 — Validate garment-assistance quality on a locked benchmark

**ID:** PBI-014; **Order:** 14; **Type:** Quality

**Title:** Validate garment-assistance quality on a locked benchmark

**Theme:** Quality / Security / Operations; **Refinement State:** Refinable Soon

**Outcome / Value**

Establish defensible assistance quality and reduce misleading confidence or burdensome corrections.

**Scope Summary**

Prepare and run the SRS-locked non-personal garment benchmark with independent annotation/adjudication and separate category, dominant-color and usable-preview evidence. Apply the established aggregate/per-category targets and confidence evidence guards; preserve manual continuity. Protect the locked evaluation from training/tuning use and retain inspectable results.

**Key Dependencies**

Hard capability dependencies: PBI-013.

**Goal Contribution**

Trustworthy Wardrobe Understanding; Enabling / Protecting Product Value.

**Primary Traceability**

BR-001, BR-002; FEAT-AI-001; AI-REQ-004, AI-REQ-010, AI-REQ-011, AI-REQ-014, NFR-USE-002, FR-MET-004; BRULE-GAR-007; SRS Sections 8.1 and 12.3; BRD MET-Q01, MET-Q05.

**Refinement Notes**

Benchmark preparation can start independently; execution needs PBI-013's behavior. The SRS already defines targets and corpus boundaries, so this is Quality work, not open-ended model research. Processing latency is validated separately in PBI-035.

### PBI-015 — Find garments through wardrobe search and filters

**ID:** PBI-015; **Order:** 15; **Type:** Product Increment

**Title:** Find garments through wardrobe search and filters

**Theme:** Digital Wardrobe; **Refinement State:** Refinable Soon

**Outcome / Value**

Reduce effort to discover relevant clothing already owned.

**Scope Summary**

Provide supported descriptor search, category/color filters, visible active conditions and reset over the current wardrobe. Separate no matches from an empty wardrobe or retrieval/access failure and preserve identifiable imageless entries and detail navigation.

**Key Dependencies**

Hard capability dependencies: PBI-007.

**Goal Contribution**

Trustworthy Wardrobe Understanding; Greater Owned-Garment Value.

**Primary Traceability**

BR-005; CAP-03; JRN-03, FEAT-WAR-003; FR-WAR-007, FR-WAR-008; UC-008.

**Refinement Notes**

Keep search and filtering together as discovery inside View Wardrobe. Do not add catalog search or new garment categories.

### PBI-016 — Remove a garment while preserving minimal historical meaning

**ID:** PBI-016; **Order:** 16; **Type:** Product Increment

**Title:** Remove a garment while preserving minimal historical meaning

**Theme:** Digital Wardrobe; **Refinement State:** Refinable Soon

**Outcome / Value**

Keep current advice aligned with actual possessions without making past reports unintelligible.

**Scope Summary**

Confirm removal from current ownership, exclude the garment from new daily outfits/current Coverage/Multiplier baseline, and refresh affected advice or mark it outdated. Preserve necessary historical snapshots with Removed from wardrobe labeling, while deleting original images and nonessential removed personal data within 30 days. Distinguish cancellation, known failure and uncertain outcome.

**Key Dependencies**

Hard capability dependencies: PBI-007.

**Goal Contribution**

Trustworthy Wardrobe Understanding; Greater Owned-Garment Value.

**Primary Traceability**

BR-005, BR-006, BR-021; CAP-03; JRN-03, FEAT-WAR-002; FR-WAR-004, FR-WAR-005, FR-WAR-006, DATA-HIST-002, DATA-RET-002; UC-010; BRULE-GAR-009, BRULE-HIST-002, BRULE-HIST-003.

**Refinement Notes**

Later history/assessment consumers must honor removal as they arrive. Removal does not delete Wear Events, restore old ownership or add an archive/restore feature. Retention enforcement is part of this increment, with broader assurance in PBI-041.

### PBI-017 — Receive valid daily outfits relevant to declared context

**ID:** PBI-017; **Order:** 17; **Type:** Product Increment

**Title:** Receive valid daily outfits relevant to declared context

**Theme:** Outfit Decision Support; **Refinement State:** Needs Refinement

**Outcome / Value**

Reduce daily decision effort with distinct wearable combinations of current owned garments.

**Scope Summary**

Use applicable Recommendation-Ready confirmed garments and declared style/current occasion. Apply all hard composition, layering, bulk, pattern and available environmental constraints before soft ranking. Present at least three distinct choices when available, actual one/two/none otherwise, with concise reasons and identified context. Distinguish completed zero from insufficient, unavailable or outdated advice and support disclosed missing-weather fallback.

**Key Dependencies**

Hard capability dependencies: PBI-008, PBI-010. Useful sequencing: PBI-011; this does not add a hard dependency.

**Goal Contribution**

Easier Outfit Decisions; Greater Owned-Garment Value.

**Primary Traceability**

BG-02; BR-007, BR-008, BR-009, BR-010, BR-012, BR-022; CAP-05; JRN-04, FEAT-OUT-001; FR-OUT-001, FR-OUT-003, FR-OUT-005, FR-OUT-009, FR-OUT-012, FR-OUT-013; UC-011, AD-011; BRULE-OUT-001, BRULE-OUT-008, BRULE-OUT-009.

**Refinement Notes**

Refine an end-to-end owned-outfit result, including true limited/zero and changed-basis outcomes. PBI-011 improves environmental evidence but permission/provider success never gates all advice. PBI-018 adds detailed inspection, and PBI-024 adds active behavioral ranking; no historical feedback is required for initial advice.

### PBI-018 — Inspect outfit constituents and explanation

**ID:** PBI-018; **Order:** 18; **Type:** Product Increment

**Title:** Inspect outfit constituents and explanation

**Theme:** Outfit Decision Support; **Refinement State:** Needs Refinement

**Outcome / Value**

Help users judge why a proposed outfit is suitable before choosing a separate action.

**Scope Summary**

Open a recommended outfit's full detail, constituent accepted garment information and supported compatibility/context explanation, maintaining current owned-outfit status and limitations. Offer distinct entry points to Shuffle, feedback and Wear as those capabilities arrive; inspection itself changes none of those states.

**Key Dependencies**

Hard capability dependencies: PBI-017, PBI-007.

**Goal Contribution**

Easier Outfit Decisions.

**Primary Traceability**

BR-007, BR-022; CAP-04, CAP-05; JRN-04, FEAT-OUT-002; FR-OUT-010, FR-OUT-011; UC-011; BRULE-OUT-002.

**Refinement Notes**

Keep the distinction from PBI-017 clear: concise reasons in the choice list versus inspectable garment/context detail. Refinement should connect to wardrobe detail without exposing hypothetical candidates as owned.

### PBI-019 — Shuffle one garment slot while retaining other choices

**ID:** PBI-019; **Order:** 19; **Type:** Product Increment

**Title:** Shuffle one garment slot while retaining other choices

**Theme:** Outfit Decision Support; **Refinement State:** Needs Refinement

**Outcome / Value**

Let users explore a compatible alternative without restarting the whole outfit decision.

**Scope Summary**

Replace only the selected slot with a different eligible owned garment, keeping every other garment identity and the assessment context fixed. Preserve full-outfit validity. Explain no compatible alternative, outdated basis or failure while retaining the prior selection where available; Shuffle alone creates no feedback or Wear Event.

**Key Dependencies**

Hard capability dependencies: PBI-017.

**Goal Contribution**

Easier Outfit Decisions; Greater Owned-Garment Value.

**Primary Traceability**

BR-008, BR-009; CAP-05; JRN-04, FEAT-OUT-003; FR-OUT-014, FR-OUT-015, FR-OUT-016, FR-OUT-017, ERR-OUT-003; UC-012, AD-012; BRULE-OUT-010.

**Refinement Notes**

Refine one supported slot and fixed-context recovery before broadening examples. Do not interpret Shuffle as Dislike or change other slots to manufacture a result.

### PBI-020 — Express, revise or clear outfit Like/Dislike

**ID:** PBI-020; **Order:** 20; **Type:** Product Increment

**Title:** Express, revise or clear outfit Like/Dislike

**Theme:** Wear & Learning; **Refinement State:** Needs Refinement

**Outcome / Value**

Give users control over meaningful preference evidence separately from reported wear.

**Scope Summary**

Accept Like/Dislike for the identified exact outfit/recommendation target, showing one effective state and allowing revision or clearing. Preserve prior state on known failure and resolve uncertain updates safely. Make accepted changes available for future soft personalization without permanent garment bans or Wear creation.

**Key Dependencies**

Hard capability dependencies: PBI-017.

**Goal Contribution**

Easier Outfit Decisions; Greater Owned-Garment Value.

**Primary Traceability**

BR-011, BR-021; CAP-06; JRN-05, FEAT-PERS-001; FR-PERS-001, FR-PERS-002, FR-PERS-003, FR-PERS-004, FR-PERS-005, FR-PERS-013; UC-013; BRULE-PERS-001, BRULE-PERS-006.

**Refinement Notes**

This owns the feedback interaction and effective state; PBI-024 owns its age-aware effect on future ranking. Refine target identity and reversal/clearing, without promising visibly different advice when valid alternatives are limited.

### PBI-021 — Report intentional wear and review distinct Wear Events

**ID:** PBI-021; **Order:** 21; **Type:** Product Increment

**Title:** Report intentional wear and review distinct Wear Events

**Theme:** Wear & Learning; **Refinement State:** Needs Refinement

**Outcome / Value**

Make reported use visible and trustworthy enough to support greater value from owned clothing.

**Scope Summary**

Accept Wear This Today only for a currently valid owned outfit, creating one event per logical intention at accepted current time. Preserve absolute time, original event-local date/time and timezone/offset, available context and user-reported meaning. Show chronological history and event detail, including legitimate repeated same-outfit/day reports; retries of one intention do not add events. Keep no reports, unavailable data and historical removed snapshots distinct.

**Key Dependencies**

Hard capability dependencies: PBI-017.

**Goal Contribution**

Greater Owned-Garment Value.

**Primary Traceability**

BG-03; BR-006, BR-011; CAP-06, CAP-07; JRN-05, FEAT-PERS-002, FEAT-ANL-001; FR-WEAR-001, FR-WEAR-002, FR-WEAR-003, FR-WEAR-011, FR-ANL-001, FR-ANL-002, DATA-WEAR-002; UC-014, UC-015, AD-014; BRULE-WEAR-001, BRULE-WEAR-002, BRULE-WEAR-003, BRULE-HIST-001.

**Refinement Notes**

Creation and inspectable history form one useful reporting increment, including resolution of uncertain acceptance. PBI-022/PBI-023 provide repair/withdrawal, PBI-024 ranking effects and PBI-025 utilization. History identity and daily preference normalization remain different concepts.

### PBI-022 — Correct one reported Wear Event within its original local day

**ID:** PBI-022; **Order:** 22; **Type:** Product Increment

**Title:** Correct one reported Wear Event within its original local day

**Theme:** Wear & Learning; **Refinement State:** Needs Refinement

**Outcome / Value**

Repair inaccurate reported use without changing unrelated history.

**Scope Summary**

Select one effective event and correct supported outfit, occasion/context or local time. Accept only non-future time within the original event-local day; preserve that meaning after travel. Accepted corrections update effective history/utilization and affected normalized ranking groups/anchors; cancellation or known failure preserves prior state, while uncertainty requires outcome review.

**Key Dependencies**

Hard capability dependencies: PBI-021.

**Goal Contribution**

Greater Owned-Garment Value.

**Primary Traceability**

BR-006, BR-011; CAP-06, CAP-07; JRN-05, FEAT-PERS-002, FEAT-ANL-001; FR-WEAR-006, FR-WEAR-010, FR-WEAR-012, FR-ANL-007; UC-016, AD-016; BRULE-WEAR-004, BRULE-WEAR-006, BRULE-PERS-005, BRULE-PERS-006.

**Refinement Notes**

Correction creates no new event or ownership. Refine original-day/timezone constraints and downstream consumer updates together, preserving unrelated reports; no backdated creation or cross-day relocation is added.

### PBI-023 — Withdraw one Wear Event and remove its effective influence

**ID:** PBI-023; **Order:** 23; **Type:** Product Increment

**Title:** Withdraw one Wear Event and remove its effective influence

**Theme:** Wear & Learning; **Refinement State:** Needs Refinement

**Outcome / Value**

Let users withdraw a report while preserving other events, outfits and garments.

**Scope Summary**

Explicitly remove the selected event from effective history, utilization, recency and personalization immediately on acceptance. Recompute effects from survivors, including the latest surviving same-outfit/original-day anchor, and physically delete applicable personal event data within 30 days. Retain underlying outfits/garments, independent feedback and unrelated reports; support cancel, failed and uncertain outcomes.

**Key Dependencies**

Hard capability dependencies: PBI-021.

**Goal Contribution**

Greater Owned-Garment Value.

**Primary Traceability**

BR-006, BR-011, BR-021; CAP-06, CAP-07; JRN-05, FEAT-PERS-002, FEAT-ANL-001; FR-WEAR-007, FR-WEAR-008, FR-WEAR-010, FR-WEAR-012, DATA-RET-001; UC-017, AD-017; BRULE-WEAR-005, BRULE-WEAR-006, BRULE-PERS-005.

**Refinement Notes**

Individual withdrawal is distinct from wardrobe removal and ranking expiry. Refine survivor and last-event consequences without removing every report for the same outfit/day; broader retention proof is PBI-041.

### PBI-024 — Adapt future valid-outfit ranking to feedback and reported use

**ID:** PBI-024; **Order:** 24; **Type:** Product Increment

**Title:** Adapt future valid-outfit ranking to feedback and reported use

**Theme:** Wear & Learning; **Refinement State:** Needs Refinement

**Outcome / Value**

Make repeated use of CapsuleAI more personally relevant and help rediscover owned clothing.

**Scope Summary**

Use accepted Like/Dislike and Wear evidence for lightweight age-aware personalization, diversity and soft recency adjustments only after hard validity. Preserve distinct Wear history while capping same-exact-outfit/original-day preference influence and using the latest surviving accepted timestamp. Apply the established elapsed-time aging policy and reflect revisions, corrections and removals without clearing history merely at ranking expiry.

**Key Dependencies**

Hard capability dependencies: PBI-020, PBI-021. Useful sequencing: PBI-022, PBI-023; this does not add a hard dependency.

**Goal Contribution**

Easier Outfit Decisions; Greater Owned-Garment Value.

**Primary Traceability**

BR-009, BR-011, BR-012; CAP-06; JRN-04, JRN-05, FEAT-PERS-003; FR-PERS-007, FR-PERS-009, FR-PERS-010, FR-PERS-012, FR-PERS-013; UC-011, UC-014; BRULE-PERS-002, BRULE-PERS-003, BRULE-PERS-004, BRULE-PERS-005, BRULE-PERS-006.

**Refinement Notes**

Initial advice is already usable through PBI-017. Refine observable personalization effects and survivor/time boundaries from the rules, without designing a ranking algorithm or training on personal data. PBI-022/PBI-023 provide mutation examples to validate.

### PBI-025 — Understand reported wardrobe utilization and overlooked items

**ID:** PBI-025; **Order:** 25; **Type:** Product Increment

**Title:** Understand reported wardrobe utilization and overlooked items

**Theme:** Wardrobe Intelligence; **Refinement State:** Needs Refinement

**Outcome / Value**

Show useful patterns in owned-clothing use while acknowledging incomplete reporting.

**Scope Summary**

Present reported frequency, recency, variety and supported overlooked-item indicators from effective accepted Wear Events, with explainable evidence and paths back to wardrobe/history. Count legitimate intentional reports independently of normalized ranking increments. Reflect corrections/removals, retain necessary history beyond ranking influence and label no recorded use without claiming never worn.

**Key Dependencies**

Hard capability dependencies: PBI-021, PBI-007. Useful sequencing: PBI-022, PBI-023; this does not add a hard dependency.

**Goal Contribution**

Greater Owned-Garment Value.

**Primary Traceability**

BG-03; BR-006, BR-024; CAP-07; JRN-05, FEAT-ANL-001; FR-ANL-003, FR-ANL-004, FR-ANL-006, FR-ANL-007; UC-018; BRULE-HIST-001, BRULE-HIST-002, BRULE-HIST-003.

**Refinement Notes**

History review remains PBI-021; this adds derived insight. Refine no-report/unavailable states and trustworthy interpretation, not a verified physical-wear score or an obligation to buy.

### PBI-026 — See a defensible personalized Wardrobe Coverage Score

**ID:** PBI-026; **Order:** 26; **Type:** Product Increment

**Title:** See a defensible personalized Wardrobe Coverage Score

**Theme:** Wardrobe Intelligence; **Refinement State:** Needs Refinement

**Outcome / Value**

Help users understand how their owned wardrobe supports their relevant everyday needs.

**Scope Summary**

Assess the Personalized Everyday Capsule using current confirmed eligible garments, six mapped common needs, positive priority weights, style and defensible context. Show contextual weighted Coverage with concise per-need support, only when sufficient-information conditions hold. Distinguish evaluated zero, insufficient information, unavailable and outdated results; changes require refresh or explicit outdated status.

**Key Dependencies**

Hard capability dependencies: PBI-017, PBI-010.

**Goal Contribution**

Actionable Wardrobe Needs.

**Primary Traceability**

BG-03, BG-04; BR-014, BR-010, BR-022; CAP-07; JRN-06, FEAT-ANL-002; FR-ANL-008, FR-ANL-011, FR-ANL-012, FR-ANL-014, FR-ANL-015; UC-019, AD-019; BRULE-COV-002, BRULE-COV-004, BRULE-COV-005, BRULE-COV-006.

**Refinement Notes**

Numeric sufficiency requires positive priorities and applicable ready garment roles, not a universal wardrobe quota. Count complete valid distinct support, not the small daily displayed subset. Coverage needs no prior Wear history and makes no purchase claim; detailed need inspection is PBI-027.

### PBI-027 — Explore per-need Coverage evidence and useful input revisions

**ID:** PBI-027; **Order:** 27; **Type:** Product Increment

**Title:** Explore per-need Coverage evidence and useful input revisions

**Theme:** Wardrobe Intelligence; **Refinement State:** Needs Refinement

**Outcome / Value**

Turn the contextual score into understandable information about covered and underserved needs.

**Scope Summary**

Inspect a need's mapped occasions, priority, valid-distinct-outfit support and limitations, including Not Relevant needs and covered outcomes. Offer relevant existing profile, garment-readiness or context revision routes. Preserve the score's current basis and honest limited/outdated status; this inspection does not identify a retail product or force shopping.

**Key Dependencies**

Hard capability dependencies: PBI-026.

**Goal Contribution**

Actionable Wardrobe Needs; Greater Owned-Garment Value.

**Primary Traceability**

BR-014, BR-022, BR-024; CAP-07, CAP-08; JRN-06, FEAT-ANL-002; FR-ANL-009, FR-ANL-012, FR-ANL-014, FR-ANL-015; UC-019, AD-019; BRULE-COV-001, BRULE-COV-003, BRULE-COV-006.

**Refinement Notes**

PBI-026 owns the completed assessment and score; this adds detailed inspection and actionable revision. Gap bottleneck/characteristic advice is PBI-028. Avoid duplicating Coverage calculation or adding a new profile-edit capability.

### PBI-028 — Explain evidence-supported wardrobe capability gaps

**ID:** PBI-028; **Order:** 28; **Type:** Product Increment

**Title:** Explain evidence-supported wardrobe capability gaps

**Theme:** Wardrobe Intelligence; **Refinement State:** Needs Refinement

**Outcome / Value**

Make underserved needs actionable without manufacturing product demand.

**Scope Summary**

Derive relevant underserved capabilities from current defensible Coverage evidence for positive-priority needs. Explain the bottleneck, need/context and useful garment characteristics. Support no-important-gap, insufficient, unavailable and outdated outcomes, and keep a supported gap useful even without an evaluable candidate or a user's interest in shopping.

**Key Dependencies**

Hard capability dependencies: PBI-026. Useful sequencing: PBI-027; this does not add a hard dependency.

**Goal Contribution**

Actionable Wardrobe Needs.

**Primary Traceability**

BR-015, BR-022, BR-024; CAP-08; JRN-06, FEAT-GAP-001; FR-GAP-001, FR-GAP-002, FR-GAP-003, FR-GAP-004, FR-GAP-005; UC-020, AD-020; BRULE-GAP-001, BRULE-GAP-002.

**Refinement Notes**

Refine capability-level explanations from established need evidence; a gap is not a missing specific product. PBI-029 connects a gap to optional credible candidates. PBI-027 provides useful need inspection but is not a hard computational prerequisite.

### PBI-029 — Explore credible hypothetical candidates for supported gaps

**ID:** PBI-029; **Order:** 29; **Type:** Product Increment

**Title:** Explore credible hypothetical candidates for supported gaps

**Theme:** Strategic Shopping; **Refinement State:** Needs Refinement

**Outcome / Value**

Help users understand possible additions before asking for outfit utility or choosing a shopping destination.

**Scope Summary**

From existing gap/advice context, offer available candidates with supported human-readable identity/visual, category/subtype, relevant attributes, source and gap rationale. Keep candidates hypothetical and unowned; clearly identify unavailable or insufficient candidate information and honest no-useful-candidate outcomes. Let users select, dismiss or return to core insights without external navigation.

**Key Dependencies**

Hard capability dependencies: PBI-028.

**Goal Contribution**

Actionable Wardrobe Needs; Better-Informed Additions.

**Primary Traceability**

BR-015, BR-018, BR-020, BR-024; CAP-08, CAP-10; JRN-06, JRN-07, FEAT-SHOP-001; FR-GAP-004, FR-SHOP-001, FR-SHOP-004; UC-020, UC-021, AD-020, AD-021; BRULE-GAP-001, BRULE-SHOP-001, BRULE-SHOP-003.

**Refinement Notes**

Candidate descriptors need explicit user evidence or an identifiable configured credible source. Refine available candidate/source examples and honest absence without prescribing a supplier or adding catalog search, import, upload or candidate editing. PBI-030 provides evaluation; unavailable utility must be disclosed until it exists.

### PBI-030 — Evaluate candidate Wardrobe Multiplier with honest result states

**ID:** PBI-030; **Order:** 30; **Type:** Product Increment

**Title:** Evaluate candidate Wardrobe Multiplier with honest result states

**Theme:** Strategic Shopping; **Refinement State:** Needs Refinement

**Outcome / Value**

Give users defensible evidence of the extra outfit utility a hypothetical addition enables.

**Scope Summary**

Compare complete current and hypothetical expanded valid outfit sets under the same context/rule and garment-identity uniqueness basis. Present incremental unique newly enabled outfit count, supported reasons and current basis. Distinguish Exact, Evaluated Zero, Incomplete, Unavailable and Outdated, including safe reevaluation after changes. Partial or timed-out work is not +0, and optional commercial fields do not gate utility.

**Key Dependencies**

Hard capability dependencies: PBI-029, PBI-017.

**Goal Contribution**

Better-Informed Additions.

**Primary Traceability**

BG-04; BR-009, BR-016, BR-022; CAP-09; JRN-07, FEAT-MULT-001; FR-MULT-003, FR-MULT-004, FR-MULT-006, FR-MULT-007, FR-MULT-008, DATA-ANL-004; UC-022, AD-022; BRULE-MULT-002, BRULE-MULT-003, BRULE-MULT-004, BRULE-MULT-005, BRULE-MULT-006, BRULE-MULT-007, BRULE-MULT-008, BRULE-MULT-009.

**Refinement Notes**

Refine completion, uniqueness and changed-basis explanations without selecting an enumeration algorithm, runtime strategy or arbitrary TTL. The entire defensible comparison is one utility outcome; PBI-031 adds inspectable previews. Candidate evaluation never adds ownership or Wear.

### PBI-031 — Preview newly enabled hypothetical outfits

**ID:** PBI-031; **Order:** 31; **Type:** Product Increment

**Title:** Preview newly enabled hypothetical outfits

**Theme:** Strategic Shopping; **Refinement State:** Needs Refinement

**Outcome / Value**

Make incremental candidate utility understandable through concrete combinations with owned clothing.

**Scope Summary**

Show candidate-bearing newly enabled outfits from the same current completed assessment as the Multiplier, distinctly labeling the unowned candidate. Do not fabricate new previews for evaluated zero or claim current previews for incomplete/unavailable/outdated evaluations. Preserve an established count appropriately when preview retrieval alone fails and allow return without purchase.

**Key Dependencies**

Hard capability dependencies: PBI-030.

**Goal Contribution**

Better-Informed Additions.

**Primary Traceability**

BR-017, BR-022; CAP-09, CAP-10; JRN-07, FEAT-MULT-001, FEAT-SHOP-001; FR-MULT-005, FR-SHOP-002, DATA-OUT-003; UC-022, AD-022; BRULE-MULT-004, BRULE-MULT-005, BRULE-OUT-002.

**Refinement Notes**

Evaluation status/count remains PBI-030's responsibility. Refine same-basis preview inspection and retry; hypothetical outfits are ineligible for daily owned advice and Wear This Today.

### PBI-032 — Show qualified commercial guidance when credible

**ID:** PBI-032; **Order:** 32; **Type:** Product Increment

**Title:** Show qualified commercial guidance when credible

**Theme:** Strategic Shopping; **Refinement State:** Needs Refinement

**Outcome / Value**

Support informed consideration of additions without inventing merchant facts or making commerce necessary.

**Scope Summary**

Enrich supported candidate advice with optional credible price ranges, material, durability/longevity, retailer or destination information. Preserve identifiable external source and retrieval/last-checked time, qualify estimates and refresh, label stale or omit price/availability older than the established 24-hour boundary. Keep supported candidate utility usable when commercial information is absent.

**Key Dependencies**

Hard capability dependencies: PBI-029.

**Goal Contribution**

Better-Informed Additions.

**Primary Traceability**

BR-020, BR-024; CAP-10; JRN-07, FEAT-SHOP-001; FR-SHOP-003, FR-SHOP-004, DATA-ANL-003; UC-021, AD-021; BRULE-SHOP-001, BRULE-SHOP-003.

**Refinement Notes**

This implements conditional BR-020 where credible information exists; it neither mandates a retailer contract nor guarantees price, stock, composition or longevity. Confirm data credibility during refinement without adding a research item for already settled scope. PBI-030/PBI-031 supply utility independently.

### PBI-033 — Navigate optionally to an external shopping destination

**ID:** PBI-033; **Order:** 33; **Type:** Product Increment

**Title:** Navigate optionally to an external shopping destination

**Theme:** Strategic Shopping; **Refinement State:** Needs Refinement

**Outcome / Value**

Let interested users act on credible candidate advice while retaining wardrobe value on return or failure.

**Scope Summary**

Offer an available credible destination only as an explicit external action. Preserve otherwise valid candidate/gap/Multiplier/reason/preview context after known handoff failure or normal return where available, refreshing or marking changed advice outdated. Distinguish attempts, established opening and uncertain outcome; no purchase, ownership or Wear is inferred and unrelated private data is not disclosed.

**Key Dependencies**

Hard capability dependencies: PBI-029. Useful sequencing: PBI-030, PBI-031, PBI-032; this does not add a hard dependency.

**Goal Contribution**

Better-Informed Additions.

**Primary Traceability**

BR-018, BR-019, BR-024; CAP-10; JRN-07, FEAT-SHOP-002; FR-SHOP-005, FR-SHOP-006, FR-SHOP-007, FR-SHOP-008, SI-004, ERR-SHOP-003; UC-023; BRULE-SHOP-002, BRULE-AUTH-007.

**Refinement Notes**

UC-023 is an optional extension of UC-021, not a mandatory commerce step. A credible available link is a runtime condition; commercial guidance or an exact Multiplier is not an additional mandatory link gate. Refine failure/return without cart, checkout, orders, fulfillment or sales verification.

### PBI-034 — Complete Product Goal evidence across the value loop

**ID:** PBI-034; **Order:** 34; **Type:** Enabler

**Title:** Complete Product Goal evidence across the value loop

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Allow the team to evaluate the documented outcome directions rather than assume product usefulness.

**Scope Summary**

Extend the established measurement foundation to Coverage/Gaps/candidates/previews and optional link interactions, and assemble the existing digitization, selection effort, feedback/Wear, utilization and repeat-engagement evidence. Distinguish exposures from accepted actions, corrections/removals from creation, intentional Wear from retries and navigation from sales. Apply minimal content, retention and core-action continuity consistently.

**Key Dependencies**

Hard capability dependencies: PBI-009.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-019, BR-021; FEAT-MET-001; FR-MET-001, FR-MET-002, FR-MET-003, FR-MET-004, FR-MET-005, FR-MET-006, FR-MET-007; Product Goal Section 7; BRD Section 19; PRD Section 24.

**Refinement Notes**

Build on PBI-009 rather than recreate event capture. Evidence integration advances with the corresponding product PBIs and combines captured data with established validation/user-research evidence; it adds no KPI target, analytics administrator or purchase attribution product.

### PBI-035 — Validate image and daily-outfit performance against the SRS

**ID:** PBI-035; **Order:** 35; **Type:** Quality

**Title:** Validate image and daily-outfit performance against the SRS

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Protect low-effort digitization and fast daily decisions with repeatable measured evidence.

**Scope Summary**

Validate garment-processing p95 at most 5 seconds after transfer/accepted analysis through usable preview plus reviewable proposals, and first complete outfit-result p95 below 3 seconds at exactly 100 confirmed garments. Use the established reference devices/network, warm-ups, measured sample and timing boundaries; retain legitimate slow runs and diagnose failures without omitting eligible inputs.

**Key Dependencies**

Hard capability dependencies: PBI-013, PBI-017.

**Goal Contribution**

Trustworthy Wardrobe Understanding; Easier Outfit Decisions; Enabling / Protecting Product Value.

**Primary Traceability**

BR-001, BR-007; NFR-PERF-001, NFR-PERF-002, NFR-SCA-001, NFR-TEST-003, FR-MET-004; SRS Sections 2.3 and 12.3; BRD MET-Q02, MET-Q03.

**Refinement Notes**

These are existing product performance targets, not delivery time estimates. Benchmark preparation may begin earlier; complete runs need the two paths. Do not add a Multiplier latency threshold, arbitrary larger-wardrobe gate or use the 20-user functional workload as this p95 workload.

### PBI-036 — Verify accessible Vietnamese journeys on supported devices

**ID:** PBI-036; **Order:** 36; **Type:** Quality

**Title:** Verify accessible Vietnamese journeys on supported devices

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Make the connected experience operable for users relying on assistive technology or enlarged text.

**Scope Summary**

Validate delivered actions/states across representative current journeys with TalkBack/VoiceOver, meaningful labels/roles, information beyond color/images, primary touch targets and up to 200% text scaling. Check Vietnamese labels preserve domain meanings and original event-local time presentation. Correct barriers as capabilities arrive on supported Android/iOS.

**Key Dependencies**

Hard capability dependencies: PBI-003.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-023, BR-022; NFR-ACC-001, NFR-ACC-002, LOC-001, LOC-002, LOC-003, LOC-004, LOC-005; SRS Sections 6.7 and 12.4.

**Refinement Notes**

Accessibility starts in PBI-003 and each increment, not at this later verification position. Evidence follows delivered journeys progressively; preserve platform targets rather than invent certification or a new language scope.

### PBI-037 — Validate core-task usability with representative users

**ID:** PBI-037; **Order:** 37; **Type:** Quality

**Title:** Validate core-task usability with representative users

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Check that the target audience can understand wardrobe value and make decisions without avoidable assistance.

**Scope Summary**

Evaluate the established six core task groups with the SRS participant and completion conditions, critical-error boundary and supporting SUS direction: account access, garment entry, outfit choices/Shuffle, Wear reporting/correction/removal, Coverage/Gaps and candidate/Multiplier evaluation. Include the primary persona and secondary personas where practical; retain findings for backlog refinement and correct evidenced barriers.

**Key Dependencies**

Hard capability dependencies: PBI-006, PBI-019, PBI-022, PBI-023, PBI-026, PBI-030.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BG-01, BG-02; BR-022, BR-023; NFR-USE-001, NFR-USE-002; SRS Section 12.4; Product Goal Sections 4 and 7.

**Refinement Notes**

This is validation of established behavior, not discovery of a replacement product. Prepare research materials earlier; execution needs representative implemented outcomes. Exact sample/task/threshold policy stays upstream, without new growth or conversion targets.

### PBI-038 — Prove concurrency and recoverable operation without state corruption

**ID:** PBI-038; **Order:** 38; **Type:** Quality

**Title:** Prove concurrency and recoverable operation without state corruption

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Keep accepted personal wardrobe and recommendation actions trustworthy during failures and concurrent use.

**Scope Summary**

Verify 20 active authenticated users retain access isolation and functional integrity under the established reference conditions. Demonstrate restart/failure recovery of core journeys within the SRS boundary while preserving accepted state and usable fallbacks. Distinguish known failure, uncertain acceptance and genuine empty/zero states across implemented actions; external failure must not create false success.

**Key Dependencies**

Hard capability dependencies: PBI-004, PBI-006, PBI-017.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-021, BR-022; NFR-REL-001, NFR-REL-002, NFR-REL-003, NFR-AVL-001, NFR-SCA-002; ERR-AUTH-001, ERR-NET-001, ERR-DEP-001; SRS Sections 12.2 and 12.3.

**Refinement Notes**

Use the existing recovery target and workload rather than invent uptime or scale commitments. Each owning product PBI already includes its recovery behavior; this provides cross-user/failure evidence rather than a separate implementation of all exception paths.

### PBI-039 — Verify domain invariants and the complete MVP value loop

**ID:** PBI-039; **Order:** 39; **Type:** Quality

**Title:** Verify domain invariants and the complete MVP value loop

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Demonstrate that connected capabilities preserve trustworthy meaning from entry through wardrobe improvement.

**Scope Summary**

Extend incremental automated evidence to complete connected scenarios: confirmed authority/readiness, owned hard-valid outfits, explicit feedback, intentional Wear identity/time and correction/removal effects, explainable Coverage/Gaps, complete same-basis Multiplier states and hypothetical previews. Verify optional commercial/link failures leave core utility intact, with mutation and unavailable-state regressions.

**Key Dependencies**

Hard capability dependencies: PBI-024, PBI-031, PBI-033.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-002, BR-009, BR-011, BR-014, BR-015, BR-016, BR-017, BR-024; NFR-TEST-001, NFR-TEST-002, NFR-TEST-003, NFR-REL-002; DATA-INT-001, DATA-INT-002, DATA-INT-003, DATA-INT-004; Workflow Sections 28 and 46.

**Refinement Notes**

Use the existing Use Cases and all 11 Business Rule families as evidence anchors. PBI-001 enables automated checks from the outset; this adds integrated coverage as the loop becomes available, without creating a test-plan document or duplicating behavioral requirements.

### PBI-040 — Deliver repeatable deployment and inspectable operation

**ID:** PBI-040; **Order:** 40; **Type:** Enabler

**Title:** Deliver repeatable deployment and inspectable operation

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Make validated increments usable and diagnosable in a controlled operating environment.

**Scope Summary**

Provide repeatable deployment, protected environment configuration, necessary operational visibility and recovery evidence for the current supported product. Verify connected dependency availability/fallback and maintenance support without disclosing private data or secrets. Complete operational readiness in the environment selected later through the workflow's architecture process.

**Key Dependencies**

Hard capability dependencies: PBI-001.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-023, BR-021; NFR-SUP-001, NFR-SUP-002, NFR-MNT-001, NFR-INT-001, NFR-AVL-001; ERR-DEP-001; Workflow Sections 28, 30 and 46.

**Refinement Notes**

PBI-001 enables early builds/checks and validation environments; this finishes usable deployment and operating evidence. Initial deployment can proceed with early increments without waiting for every feature. Cloud provider, services, topology and runtime boundaries remain later architecture decisions.

### PBI-041 — Verify authorization, privacy and retention across the MVP

**ID:** PBI-041; **Order:** 41; **Type:** Quality

**Title:** Verify authorization, privacy and retention across the MVP

**Theme:** Quality / Security / Operations; **Refinement State:** Needs Refinement

**Outcome / Value**

Provide evidence that evolving product value continues to respect private access and user control.

**Scope Summary**

Verify isolation and session/security behavior across delivered journeys; inspect purpose-minimal external disclosure and absence of personal-data AI training. Verify immediate effective removal, 30-day physical deletion for applicable removed garment/event data, necessary minimal retained history and the separate 90-day user-linked measurement limit. Protect secrets and personal data in validation evidence.

**Key Dependencies**

Hard capability dependencies: PBI-002, PBI-009, PBI-016, PBI-023.

**Goal Contribution**

Enabling / Protecting Product Value.

**Primary Traceability**

BR-021; NFR-SEC-001, NFR-SEC-002, NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002; DATA-RET-001, DATA-RET-002, DATA-RET-003; BRULE-AUTH-001, BRULE-AUTH-007, BRULE-HIST-003.

**Refinement Notes**

Foundational protection is PBI-002; action-specific lifecycle behavior remains in PBI-016/PBI-023 and measurement PBIs. This is evidence and remediation across the integrated product, not deferred privacy implementation or a new account deletion/export feature.

## 7. Dependency and Sequencing View

The hard relationships below summarize the master table. They identify needed product capabilities, not implementation layers or a requirement to finish all work serially. The Product Owner may reorder independent work or refine related items together while keeping stable IDs.

| Major relationship | Hard capability chain | Sequencing interpretation |
| --- | --- | --- |
| Private wardrobe value | PBI-002 → PBI-004 → PBI-006 → PBI-008; PBI-004 → PBI-007 | Manual creation, populated inspection and readiness enrichment establish early useful wardrobe value. Empty browsing remains valid without existing garments. |
| Assisted entry | PBI-006 → PBI-012 → PBI-013 → PBI-014 | Photo-supported manual entry precedes useful automated proposals and quality evidence. Neither photo input nor successful AI blocks PBI-006. Benchmark preparation can proceed earlier. |
| Daily choices and learning | PBI-008 + PBI-010 → PBI-017; PBI-017 → PBI-019 / PBI-020 / PBI-021; PBI-020 + PBI-021 → PBI-024 | Initial declared-context advice is useful without behavioral history. PBI-011 is useful environmental sequencing, with denied permission/missing weather handled explicitly. |
| Trustworthy reported-use insight | PBI-021 → PBI-022 / PBI-023; PBI-021 + PBI-007 → PBI-025 | Repair and withdrawal affect the selected report and all existing consumers. Same-day history identity is separate from daily ranking normalization. Utilization does not gate Coverage. |
| Needs and candidate utility | PBI-017 + PBI-010 → PBI-026; PBI-026 → PBI-027 / PBI-028; PBI-028 → PBI-029; PBI-029 + PBI-017 → PBI-030 → PBI-031 | Coverage/candidate work needs defensible validity behavior and confirmed inputs, not a three-choice UI subset or prior Wear history. Detailed need inspection and gap explanation are distinct outcomes. |
| Optional commerce guidance | PBI-029 → PBI-032 / PBI-033 | Credible commercial fields and explicit navigation can advance independently of exact utility. Their absence/failure must preserve otherwise valid advice; no retail partnership or purchase gates the loop. |
| Measurement and operation | PBI-002 → PBI-009 → PBI-034; PBI-001 → PBI-040 | Measurement begins with early wardrobe outcomes and expands with feature availability. Deployable validation and operating capability can advance with early increments rather than waiting for every feature. |

Quality work accompanies its owning increments. PBI-035–PBI-041 provide focused or integrated evidence as representative capabilities become available. Their key dependencies express the behavior needed for those checks, without creating a separate final-only testing phase.

## 8. MVP Coverage Check

Coverage here means a defined implementing/enabling backlog path, not delivered or accepted software. Conditional commercial information and available-link navigation retain their upstream conditions.

| MVP Area | Covered By |
| --- | --- |
| Authentication and Recovery | PBI-004, PBI-005; isolation/verification PBI-002, PBI-041 |
| Personalization and Context | PBI-010, PBI-011, PBI-024 |
| Garment Digitization | PBI-006, PBI-008, PBI-012, PBI-013 |
| Manual Garment Entry | PBI-006; manual continuity in PBI-012/PBI-013, validated through PBI-014/PBI-038 |
| AI-Assisted Garment Analysis | PBI-013, PBI-014; performance PBI-035 |
| Wardrobe Management | PBI-007, PBI-008, PBI-015, PBI-016 |
| Outfit Recommendation | PBI-017, PBI-018, PBI-024 |
| Shuffle | PBI-019 |
| Like / Dislike | PBI-020, PBI-024 |
| Wear Events | PBI-021, PBI-022, PBI-023; ranking effects PBI-024 |
| Wear History | PBI-021, PBI-022, PBI-023; minimal removed-garment meaning PBI-016 |
| Wardrobe Utilization | PBI-025; current garment use indicators in PBI-007 |
| Wardrobe Coverage | PBI-026, PBI-027 |
| Gap Analysis | PBI-028; optional candidate bridge PBI-029 |
| Candidate Evaluation | PBI-029, PBI-030 |
| Wardrobe Multiplier | PBI-030, PBI-031 |
| Strategic Shopping | PBI-029, PBI-030, PBI-031, PBI-032 |
| External Shopping Navigation | PBI-033 |
| Privacy / User Data Protection | PBI-002, PBI-009, PBI-016, PBI-023, PBI-034, PBI-041 |
| Accessibility / Usability | PBI-003, PBI-014, PBI-036, PBI-037 |
| Performance / Reliability | Recovery in owning increments; focused evidence PBI-035, PBI-038 |
| Testing / Validation | PBI-001, PBI-014, PBI-035–PBI-039, PBI-041 |
| Deployment / Operability | PBI-001, PBI-040; recovery evidence PBI-038 |
| Product Measurement | PBI-009, PBI-034; existing validation evidence PBI-014/PBI-035/PBI-037/PBI-039 |
| Vietnamese localization and supported mobile experience | PBI-003, PBI-036; applied to every user-facing increment |

No backlog item adds native commerce, social/public wardrobe features, AR/3D, social/OTP/guest access, personal-data AI training or an administrator product. Candidate capture/import, account export/deletion and speculative reward/advertising features are also absent from the current scope.

## 9. Traceability Summary

Primary Traceability in Section 6 uses selected stable identifiers from the linked canonical sources. Full behavioral detail remains in the SRS, rules and UC specifications; the backlog does not duplicate the requirements catalogue.

| Goal / journey anchor | Major capability anchors | Principal PBI routes |
| --- | --- | --- |
| Enable private participation — JRN-01 | CAP-01; FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002 | PBI-002–PBI-005, PBI-010, PBI-011 |
| Understand My Wardrobe — BG-01; JRN-02 | CAP-02, CAP-04; FEAT-AI-001, FEAT-AI-002, FEAT-GAR-001 | PBI-006, PBI-008, PBI-012–PBI-014 |
| Maintain current understanding — BG-01/BG-03; JRN-03 | CAP-03; FEAT-WAR-001–FEAT-WAR-003 | PBI-007, PBI-008, PBI-015, PBI-016 |
| Use My Wardrobe Better — BG-02; JRN-04 | CAP-05; FEAT-OUT-001–FEAT-OUT-003 | PBI-010, PBI-011, PBI-017–PBI-019, PBI-024 |
| Learn from explicit use — BG-03; JRN-05 | CAP-06, CAP-07; FEAT-PERS-001–FEAT-PERS-003, FEAT-ANL-001 | PBI-020–PBI-025 |
| Improve My Wardrobe through needs — BG-03/BG-04; JRN-06 | CAP-07, CAP-08; FEAT-ANL-002, FEAT-GAP-001 | PBI-026–PBI-029 |
| Judge informed additions — BG-04; JRN-07 | CAP-09, CAP-10; FEAT-MULT-001, FEAT-SHOP-001, FEAT-SHOP-002 | PBI-029–PBI-033 |
| Enable/protect every loop stage | BR-021–BR-024; FEAT-MET-001; applicable NFR/LOC/AI-REQ/DATA/ERR and interface constraints | PBI-001–PBI-003, PBI-009, PBI-014, PBI-034–PBI-041 plus owning product increments |

The coverage audit accounts for BG-01–BG-04, BR-001–BR-024, CAP-01–CAP-10, all 22 FEAT identifiers, all 23 UC goals, all 14 FR families (AUTH, PROF, WEATHER, AI, GAR, WAR, OUT, PERS, WEAR, ANL, GAP, MULT, SHOP and MET) and all 11 Business Rule families (AUTH, PROF, GAR, OUT, PERS, WEAR, HIST, COV, GAP, MULT and SHOP). Relevant data integrity/retention, interfaces, localization, AI obligations, recovery and quality constraints follow the owning increments and the evidence PBIs.

The following decomposition is intentional rather than a one-to-one UC mapping:

| Complex Use Case | Principal decomposition | Value boundary |
| --- | --- | --- |
| UC-007 — Add Garment | PBI-006, PBI-008, PBI-012, PBI-013; supporting PBI-014 | Minimum manual ownership; descriptor enrichment; photo-supported manual entry; assisted preview/proposals; separate quality evidence. Confirmation/fallback remains inside each entry slice. |
| UC-011 — Get Outfit Recommendations | PBI-017, PBI-018, PBI-024 | Initial valid declared-context choices; inspectable detail; active behavioral ranking once explicit evidence exists. Shuffle remains its separate UC-012 outcome. |
| UC-014 — Record Wear Event | PBI-021, PBI-024; related UC-016/UC-017 in PBI-022/PBI-023 | Intentional reporting with inspectable history differs from normalized future-ranking effects and individual repair/withdrawal. |
| UC-019 — View Wardrobe Coverage | PBI-026, PBI-027 | Defensible score/summary versus detailed need evidence and useful input-revision routes. Sufficiency/currentness is preserved in both. |
| UC-020 — View Wardrobe Gaps | PBI-028, PBI-029 | Supported capability bottleneck versus optional exploration of credible hypothetical candidates. No candidate does not erase a supported gap. |
| UC-021 — View Shopping Recommendations | PBI-029, PBI-032; evaluated utility/previews from PBI-030/PBI-031 and optional UC-023 in PBI-033 | Required supported candidate information differs from conditional commercial guidance, defensible utility and optional external navigation. |
| UC-022 — Evaluate Candidate Garment | PBI-030, PBI-031 | Complete same-basis incremental utility and honest states versus inspection of newly enabled hypothetical outfits. |

Simple behaviors remain grouped where they form one useful capability: UC-001/UC-002/UC-003 in PBI-004; initial/later profile maintenance from UC-005 in PBI-010; UC-009's confirmed edits with garment enrichment in PBI-008; UC-015's history inspection with intentional reporting in PBI-021. UC-008's browsing/detail is PBI-007 and its discovery outcome PBI-015. Like/Dislike/revise/clear stay together in PBI-020; all single-slot Shuffle outcomes stay in PBI-019; external handoff/failure/return stay in PBI-033. Individual Wear correction/removal remain distinct outcomes in PBI-022/PBI-023, without splitting their validation steps into task PBIs.

## 10. Refinement Guidance

Use a small first refinement horizon: **PBI-001–PBI-007**. Begin story-level refinement with PBI-004–PBI-007 (private account access, recovery, confirmed manual entry and wardrobe inspection). Clarify the enabling/quality outcomes of PBI-001–PBI-003 within that horizon so technical preparation and trust/usability constraints accompany early value. PBI-008–PBI-011 are the next candidates when review and learning justify extending the horizon.

For each selected PBI, confirm the user/delivery outcome, smallest coherent slice, source constraints, dependencies, failed/uncertain states and evidence needed. Then derive traceable User Stories and testable Acceptance Criteria in the next authorized workflow stage. Enabler refinement must expose decisions needed from the later architecture stages rather than resolve them inside this backlog.

Top PBI states indicate suitability to begin that refinement, not completed stories. Lower items remain Needs Refinement because delivery slices, evidence and integration boundaries need team discussion. Revisit Order after refinement and observed learning; do not infer team capacity, estimates or Sprint allocation.

## 11. Current Status

**Baseline Draft.** The backlog contains an initial proposed order, not formal approval, implemented capability or a Sprint commitment. All items are Not Estimated. Review may change ordering and decomposition while preserving stable identities and upstream traceability.

The audit found no blocking contradiction in current MVP scope or behavior and no uncovered major MVP area. One non-blocking source editorial issue remains: FR-MET-004 contains an incomplete benchmark cross-reference. PBI-014/PBI-035 use the explicit SRS Sections 8.1 and 12.3 to anchor recognition and timing validation. The SRS and every other input artifact remain unchanged.

After Product Backlog review, the next expected artifact is **initial User Stories for the highest-ordered backlog**, followed by **Definition of Done**, according to Workflow Section 46, Steps 11–12. Later quality-attribute analysis, ASR/ADD/ADR, test strategy, engineering preparation and Sprint Planning retain their separate workflow responsibilities. This task creates only product-backlog.md; it creates no stories, detailed Acceptance Criteria, Definition of Done, Sprint artifacts, architecture/design artifacts or implementation.

