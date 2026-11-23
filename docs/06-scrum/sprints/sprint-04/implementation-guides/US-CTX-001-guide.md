# US-CTX-001 — Choose Environmental Context with Safe Fallback: Implementation Guide

| Field | Value |
| --- | --- |
| Story | [US-CTX-001](../../../stories/context/US-CTX-001-choose-location-and-recover-limited-context.md) (Draft), parent PBI-011 |
| Sprint | Sprint 4, story 5 of 6 in the [recommended order](README.md#6-implementation-sequence). Same owner as US-PROF-002, with no dependency on its optional context. |
| Starting point | After US-GAR-005, US-GAR-006, US-WAR-003 and US-PROF-002 |
| Approved decisions applied | S4-CTX-01 to S4-CTX-06, S4-INT-02, S4-INT-03, all **approved by the Product Owner on 2026-11-16** |
| Delegated defaults applied | OPEN-S4-18 to 24, 31, 32, 33 (`SELECTED BY CLAUDE — DELEGATED DEFAULT`, [record](open-questions.md)) |
| External status | **The Open-Meteo terms of use, attribution and eligibility for the actual deployment are not verified, and no real provider request has been made.** The request and response shapes follow the provider's public documentation (checked through Context7 on 2026-11-16) and must be confirmed by a real authorized request (§14 M7). |
| Verification status | **Applied to the repository on 2026-11-23** (P1–P14 by the developer, reviewed against this guide; P15–P35, T1–T15 and the review corrections afterwards). `./mvnw -o clean verify` passed in the repository on 2026-11-23: 694 unit and 434 integration tests, none failed, errored or was skipped. Of the 132 required CI reports, 131 were present with at least their minimum; the missing one is the Sprint 3 `HeifDecodingIT`, which needs libheif that the local machine lacks and CI installs. Manual M1–M6 and M6a passed with the provider switched off. M8 was measured locally on 2026-11-23 against a loopback server that never answers (developer machine, not the agreed environment): 141 reads, each answered UNAVAILABLE / DEADLINE_EXCEEDED in 2,007–2,064 ms; at most 8 `weather-acquisition` threads; the JDK client's threads returned to baseline after idle. **Still required:** the Open-Meteo terms, attribution and eligibility review and an authorized real request (M7), M8 in the agreed environment, a CI run on a pull request to `sprint_04`, client evidence, and the AC-04/AC-05 consumers. |

## 1. Story Overview

**Story.** As an authenticated user, I want to choose my location by device or manually, or to skip it, so that advice can consider current weather. When weather is unavailable, I want to be told so truthfully rather than be blocked.

**Backend outcome.**

- **Location resource:** `GET`/`PATCH /api/v1/personal-context/location`.
  - One minimal current selection per owner: a point rounded to 2 decimals (about 1.1 km), an optional label (required for `MANUAL`) and its source (`DEVICE` or `MANUAL`).
  - The selection has its own revision.
  - Skipping means never sending a selection. Clearing is `selection: null`. Neither calls the provider.
- **Weather evidence:**
  - acquired on demand from **Open-Meteo**, outside any transaction, under **one elapsed 2-second deadline** that includes capacity wait, connection, body and translation;
  - only the rounded point is sent;
  - the evidence is bound to the location revision it was acquired for, and a late result for a changed or cleared selection is never published;
  - only the latest evidence is kept.
- **Freshness:**
  - weather is current when its provider evidence time is at most **30 minutes** old, inclusive;
  - older evidence is refreshed before use;
  - a newly fetched but stale reading is never current;
  - acquisition time and evidence time are distinct and both reported.
- **Truthful fallback:** the response always reports the accepted selection and an environment status separately:
  - `NO_SELECTION`;
  - `CURRENT`, with the reading;
  - `UNAVAILABLE`, with a reason (`DEADLINE_EXCEEDED`, `PROVIDER_UNAVAILABLE`, `INVALID_PROVIDER_DATA`, `STALE_PROVIDER_DATA`, `SELECTION_CHANGED` or `NOT_CONFIGURED`).

  No temperature is invented.
- **Basis for future consumers (S4-CTX-06):** the status carries the location revision it applies to. No published API is added until a consumer exists (OPEN-S4-23).

**Out of scope:**

- continuous or background tracking;
- location history;
- backend geocoding (manual cities are resolved on the client, OPEN-S4-18);
- outfit filtering or reevaluation (consumers absent; recorded in §15);
- any provider other than Open-Meteo;
- mobile permission screens.

## 2. Repository Baseline

| Item | State after US-PROF-002 | Relevance |
| --- | --- | --- |
| `personal_context` schema | V11 preferences, V13 optional context | V14 adds `personal_context_location` and `personal_context_weather_evidence` |
| Personal Context conventions | `ExpectedRevision`, `ValueChange`, `PreferenceFieldViolation`/`PreferenceValueProblem`, `PersonalContextTransactions`, guarded first write | Reused for the location lifecycle |
| HTTP clients | None in the backend | P23 adds Spring `RestClient` over the JDK `HttpClient` (`JdkClientHttpRequestFactory`), which is part of `spring-web` and needs no new dependency |
| Architecture rules | `java.net.http` banned in domain, application and api; allowed in `adapter.out` | The adapter lives in `personalcontext.adapter.out.weather` |
| Test infrastructure | `IdentityTestConfiguration` replaces photo storage with a `@Primary` fake | T6 adds a `@Primary ControllableWeatherProvider` the same way |
| `PreferencesSchemaIT` | Exact `personal_context` table list (US-PROF-002 T9) | T14 adds the two new tables |
| Migrations | V1–V13 | This guide adds **V14** |
| OpenAPI | 0.15.0 | → 0.16.0 |
| CI | 123 entries | +9 entries (132) |

## 3. Original Business Requirements — Verbatim

> Sections 3–8 reproduce the cited sources verbatim. Only relative link targets were re-based to this file's location, and heading levels were shifted to nest under this guide; no wording was changed. Sprint 4 decision rows reproduce the plan's register (version 0.2), which records the Product Owner's approval of 2026-11-16. Paragraphs and lines dated 2026-11-17 were added to the sources by the Sprint 4 documentation synchronization.

From [BRD](../../../../01-business/BRD.md):

| ID | Requirement | Business Rationale | Priority | Related Goals |
|---|---|---|---|---|
| BR-007 | Daily outfit advice must use confirmed owned garments and relevant request weather/occasion, declared preferences and approved garment information. Actual composition/compatibility policy is deferred; removed properties are not hidden requirements. | Reduce decision effort through relevant advice. | Must | BG-02, BG-03 |
| BR-013 | Users must be able to obtain location-based environmental context without granting device location permission, using manual location selection. | Support relevance without making location access a condition of participation. | Must | BG-02, BG-04 |

| ID | Capability | Business Purpose |
|---|---|---|
| CAP-01 | Account & Personalization | Maintain a private personal wardrobe context, style preferences, and optional profile information. |
| CAP-05 | Context-Aware Styling | Offer valid, personally relevant outfits and compatible item alternatives from owned clothing. |

## 4. Original Product Requirements — Verbatim

From [PRD](../../../../02-product/PRD.md):

### 8.1 JRN-01 — Account Onboarding & Personalization

**Intent:** Establish private access and relevant everyday needs through progressive onboarding. The initial interface is Vietnamese; English terms below describe product meaning and do not prescribe English UI copy.

1. **Welcome / Product Value Introduction:** Explain wardrobe intelligence, user authority, and the connected product value.
2. **Register or Login:** Register with Email, Password, and Confirm Password; Display Name is optional. Login uses Email and Password. Forgot Password provides email-based password reset.
3. **Style Preferences:** Select from the initial controlled style vocabulary in Section 11.3.
4. **Common Occasion Needs:** Set relevant common needs/priorities for the Personalized Everyday Capsule. These ongoing needs are distinct from the current occasion of an outfit request.
5. **Optional Personal Context:** Offer gender/body profile with omission, later edit, and removal.
6. **Location:** Permit device location, manually select city/location, or skip with a limited-context explanation. Manual location remains available after permission denial.
7. **Recommended Add First Garment:** Explain its value and offer the action without making it an access blocker or requiring an arbitrary garment quota.
8. **Enter Today / Wardrobe:** Open the product; an empty wardrobe explains limited recommendations and offers Add Garment. Users can return later to complete skipped optional information.

**Completion:** The authenticated user can access Today/Wardrobe without body/gender, device-location permission, or a first garment. Incomplete optional setup is distinct from failed authentication.

**Recovery:** Explain account/reset/connectivity failures and offer retry; manual location and manual garment entry preserve available alternatives. **Features:** FEAT-AUTH-001, FEAT-PROF-001, FEAT-PROF-002, FEAT-AI-002.

### 8.4 JRN-04 — Daily Outfit Recommendation

**Intent:** Choose an appropriate outfit from owned garments with useful control.

1. The user opens Today, checks the environmental context and selected occasion/style, and requests recommendations.
2. The product considers the current confirmed wardrobe and available context, excludes invalid combinations, then ranks valid options using preferences and meaningful behavior.
3. The user sees at least three distinct valid options when available, or an explanation for fewer/no options.
4. Outfit detail explains relevant context and compatibility in plain language.
5. The user may shuffle one item while keeping the other selected garments fixed; only compatible alternatives are offered.
6. The user can Like, Dislike, or use Wear This Today to create a distinct user-reported Wear Event. Multiple intentional events, including separate uses of the same outfit, may occur within the same local day. Context changes cause refreshed evaluation rather than retaining an unsupported claim of suitability.

**Completion:** The user can make an informed selection without needing a purchase. **Recovery:** Missing weather is disclosed; usable reduced-context advice remains available where validity can be established. Missing compatible garments or shuffle alternatives produce useful guidance, not fabricated choices. **Features:** FEAT-OUT-001, FEAT-OUT-002, FEAT-OUT-003, FEAT-PERS-001–FEAT-PERS-003.

### 11.2 FEAT-PROF-002 — Location and Environmental Context

**Purpose:** Supply useful environmental context without requiring device-location access.

**User value:** The user can obtain relevant weather context through permission-based or manual location choice.

**Related BRD:** BR-007, BR-013, BR-014, BR-022; CAP-01, CAP-05, CAP-07; BG-02, BG-04. **Primary users:** Especially Indecisive Professional; relevant to all personas.

**Entry points / trigger:** Onboarding, Profile, Today context review, or an unavailable-weather message. **Preconditions:** A supported manual location or consented device location for location-specific weather; connected access when weather is requested.

**Product behavior:**

1. Explain device-location purpose before requesting permission; the user can decline.
2. Offer manual city/location selection independently of permission, including after denial.
3. Show which location/environmental context informs recommendations and relevant assessments.
4. Allow the user to change the location used; refresh affected advice or mark it for reevaluation.
5. Distinguish unavailable weather from known weather; retain reduced-context recommendations where valid.

**User controls:** Grant/decline permission, select/change manual location, retry context retrieval. **Product states:** Unset, permission-based, manually selected, loading, available, unavailable.

**Failure / fallback:** Manual selection handles permission denial or device-location failure. If weather still cannot be obtained, explain the missing context; do not invent a forecast or claim weather suitability.

**Feature-level acceptance criteria:**

- **AC-01:** A user who denies device location can select a city and receive location-based weather when the weather dependency is available.
- **AC-02:** Recommendations disclose the location/context actually used.
- **AC-03:** Unavailable weather is clearly labeled; usable advice can continue with stated limitations where validity can be assessed.
- **AC-04:** Changing location cannot leave an older assessment presented as current for the new location.

**Out of scope / deferred detail:** Provider choice, retrieval frequency, location precision, and weather validity thresholds are downstream. Continuous background tracking is not an MVP requirement.

**Sprint 4 resolution (2026-11-17):** US-CTX-001 settles the details deferred above for the current increment ([SRS Section 3.3.1](../../../../03-requirements/SRS.md#331-location-and-weather-rules)): one current location from the device or a manual city list, stored only as a point rounded to two decimal places; no continuous tracking or history; Open-Meteo as the intended provider, whose terms are not yet verified; weather current at age ≤30 minutes from the provider's observation time; at most 2 seconds of waiting for weather; and an explicit unavailable state with a reason otherwise. See Section 34.3.

## 5. Original Software Requirements — Verbatim

From [SRS](../../../../03-requirements/SRS.md):

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| FR-WEATHER-001 | The system shall offer device-location access as an optional, purpose-explained choice. | MVP | FEAT-PROF-002; BR-013 | T: grant, deny, skip. |
| FR-WEATHER-002 | The system shall allow manual city/location selection independently of device permission, including after denial. | MVP | FEAT-PROF-002; BR-013 | T: manual city after denial. |
| FR-WEATHER-003 | The system shall request environmental information for the selected location when available, use it as current only at age ≤30 minutes from its applicable retrieval/observation timestamp, attempt refresh when older, and allow an external weather request to delay the assessment by no more than 2 seconds. | Conditional | FEAT-PROF-002; BR-007, BR-013 | T: age 30 minutes versus older; fresh/stale responses; usable result within 2 seconds versus unavailable fallback. |
| FR-WEATHER-004 | The system shall identify the selected location, environmental information and applicable retrieval/observation timestamp actually used, disclosing unavailable environmental context when usable current information is not obtained within the 2-second boundary. | MVP | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-022 | I/T: displayed context/timestamp matches evaluation; no invented forecast at timeout. |
| FR-WEATHER-005 | The system shall reevaluate or mark affected advice outdated when its location/environmental context changes, attempting refresh before stale weather is used as current. | MVP | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-022 | T: changed location/context and stale-weather refresh; failed refresh retains an explicit limited/outdated state. |
| SI-002 | The system shall exchange only the selected location needed for weather retrieval, apply the 30-minute currentness and 2-second assessment-delay boundary, and distinguish stale/unavailable information without sending unrelated wardrobe/profile/history. | MVP | FEAT-PROF-002; BR-013, BR-022 | T/I: minimal location exchange, timestamp ages and bounded fallback. |
| HW-003 | The system shall use device location only with permission and preserve manual city/skip choices when permission is denied. | MVP | FEAT-PROF-002; BR-013 | T: grant/deny/manual/skip. |

| ID | Failure Condition | Required System Behavior | Continuation / State Protection | Source | Verification |
|---|---|---|---|---|---|
| ERR-WEATHER-001 | Weather/location information cannot support the request. | The system shall attempt refresh of weather older than 30 minutes, treat usable information not obtained within the 2-second assessment-delay boundary as unavailable, skip only unavailable environmental hard filtering where appropriate, preserve remaining hard validity and disclose missing context without fabricated weather or blanket outfit rejection. | Missing weather alone does not reject all outfits; remaining validity and useful core context remain, without invented evidence. | FEAT-PROF-002, FEAT-OUT-001; BR-007, BR-013, BR-022 | T: freshness equality/older and availability at/after boundary; remaining validity stays enforced. |

| ID | Requirement | Scope | Source | Verification |
|---|---|---|---|---|
| COM-003 | The system shall limit external shopping interaction to explicitly chosen navigation without granting destinations unrestricted private wardrobe/profile/history access; external dependencies receive only the minimum information needed for their purpose. | MVP | FEAT-SHOP-002, FEAT-AUTH-001; BR-018, BR-021 | I/T: explicit handoff, provider-specific minimal exchanges and no unrelated personal-data disclosure. |

Added to the [SRS](../../../../03-requirements/SRS.md) on 2026-11-17 by the Sprint 4 documentation synchronization (verbatim):

#### 3.3.1 Location and Weather Rules

Added 2026-11-17 for [US-CTX-001](../../../stories/context/US-CTX-001-choose-location-and-recover-limited-context.md). The approved decisions S4-CTX-01–06 (approved 2026-11-16) govern; the concrete values are delegated defaults ([OPEN-S4-18–24](open-questions.md#open-s4-18--location-representation-precision-and-manual-resolution)). These rules refine FR-WEATHER-001–FR-WEATHER-005 without changing them.

| Rule | Requirement |
|---|---|
| One current location | Each user has at most one current location selection, from the device or chosen manually, with its own revision. Skipping sends nothing; clearing removes the selection. |
| Precision | Only a point rounded to two decimal places of latitude and longitude (about 1.1 km), its source (device or manual) and, for a manual choice, a label of at most 80 characters are stored and used. Exact device coordinates are not retained. |
| Manual choice | A manual city is chosen in the client from a bundled reference list and sent as a rounded point with its label. The backend performs no geocoding. |
| No tracking | No continuous or background location tracking and no location history: a new selection replaces the previous one. |
| Provider | The intended weather provider is Open-Meteo (current conditions). Only the rounded point is sent to it. **The provider's terms and eligibility for the actual deployment are not yet verified.** Until they are, a deployment runs with weather disabled, which reports weather as not configured. |
| Freshness | Weather is current when the provider's observation time is at most 30 minutes old, inclusive. An observation more than 5 minutes in the future is invalid. Older weather is refreshed before it is used as current; a newly fetched reading that is already older than 30 minutes is not current. Retrieval time and observation time are both kept. |
| Deadline | One acquisition takes at most 2 seconds in total, including any wait for capacity, the connection, the response and its interpretation. |
| Truthful state | The user sees the selected location and, separately, either current weather with its observation time, no selection, or weather unavailable with a reason: deadline exceeded, provider unavailable, invalid provider data, stale provider data, selection changed or not configured. No weather value is invented. |
| Revision binding | Weather is bound to the location revision it was acquired for. A late result for a changed or cleared selection is never shown as current. |
| Consumers | No advice, Coverage or Multiplier consumer exists in Sprint 4. FR-WEATHER-005's reevaluation obligation applies when such a consumer arrives. |

## 6. Original Business Rules — Verbatim

From [Business Rules](../../../../03-requirements/business-rules.md):

### BRULE-PROF-003 — Selected Environmental Context

**Rule:** Device-location permission is optional; manual city/location selection remains available independently, including after denial. Assessments identify the location and environmental information actually used. A relevant location/environment change requires reevaluation or an outdated indication; missing context is not fabricated.

Weather is current at age ≤30 minutes from its applicable retrieval/observation timestamp. Older weather is refreshed before being used as current. An external weather request may delay an assessment at most 2 seconds; without usable current information by that boundary, environmental information is unavailable for that assessment and BRULE-OUT-006 governs fallback. A selected location alone does not prove a current forecast.

**Applies To:** context selection, recommendation, Coverage and Multiplier.

**Source:** FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-003, FR-WEATHER-004, FR-WEATHER-005, SI-002, ERR-WEATHER-001.

**Sprint 4 refinement (2026-11-17):** The "applicable retrieval/observation timestamp" is the provider's observation time, and the 30-minute boundary is inclusive. An observation more than 5 minutes in the future is invalid. The 2-second limit covers the whole acquisition, including any wait for capacity. A location is a point rounded to two decimal places with its source and an optional label; there is no continuous tracking or location history, and only the rounded point is sent to the weather provider. Weather is bound to the location revision it was acquired for. Open-Meteo is the intended provider, pending verification of its terms (approved S4-CTX-01–06; delegated OPEN-S4-18–24; SRS Section 3.3.1).

### BRULE-OUT-006 — Unavailable Environmental Context

**Rule:** Preserve actual request/weather context and the existing freshness/2-second acquisition boundary. Missing weather is disclosed and not fabricated; it does not by itself label saved garments unsuitable. Future approved engine policy determines defensible reduced-context evaluation; this rule does not restore removed garment suitability attributes.

**Source:** BRULE-PROF-003, ERR-WEATHER-001, FR-OUT-012, FR-WEATHER-004.

### BRULE-COV-006 — Relevant Changes Invalidate Current Coverage

**Rule:** Relevant changes to current wardrobe/confirmed attributes, common needs/priorities, assessment context, or applicable validity rules require reevaluation or a clear outdated indication. Removed garments are excluded from the next current calculation. Prior scores must not silently remain current after failed refresh.

**Applies To:** Coverage refresh and dependent gap evidence.

**Source:** FR-ANL-013, FR-ANL-014, FR-WAR-006, ERR-ANL-002.

### BRULE-MULT-008 — Input and Rule-Based Freshness

**Rule:** A previous result is Outdated when relevant active wardrobe composition, confirmed garment attributes, candidate attributes, assessment context, or validity/business rules no longer match its basis. Do not present its prior count/previews as current exact evidence. Freshness is input/rule based; no arbitrary fixed TTL is specified. Weather evidence follows BRULE-PROF-003; commercial currentness follows BRULE-SHOP-001. Commercial/destination unavailability alone does not change otherwise valid incremental outfit utility.

**Applies To:** assessment reuse and changed evaluation bases.

**Source:** FR-MULT-008, FR-OUT-009, DATA-ANL-004, ERR-ANL-002.

## 7. Original User Story and Acceptance Criteria — Verbatim

From [US-CTX-001](../../../../06-scrum/stories/context/US-CTX-001-choose-location-and-recover-limited-context.md):

### 1. Story

As an authenticated user, I want to choose location through permission or manual selection and understand missing weather, so that advice can be relevant without mandatory device access.

### 3. Scope

Device/manual/skip selection, actual location/weather basis, freshness, bounded acquisition, context change and limited-context recovery.

### 4. Preconditions

- The user has authorized access to personal context; device permission and available weather are not prerequisites to opening setup.

### 5. Acceptance Criteria

#### AC-01 — Offer permission-independent choices

Explain location purpose and allow consented device access, independent manual city/location, or skip. Denial/device failure retains manual and skip choices and does not deny authenticated entry.

#### AC-02 — Identify actual evidence

Show the selected location and available environmental information with the applicable retrieval/observation timestamp actually used; a selected city alone is not a known forecast.

#### AC-03 — Respect weather freshness

Weather age exactly 30 minutes is current; older information is refreshed before current use. An external weather request may delay an assessment by at most 2 seconds.

#### AC-04 — Disclose bounded fallback

If usable current weather is not obtained within that boundary, disclose unavailable environmental context and skip only unavailable environmental hard filtering where appropriate. Remaining validity still applies; no invented forecast or blanket rejection of all outfits.

#### AC-05 — Handle accepted changes

Accepted location/environment changes inform later relevant advice and trigger reevaluation or an explicit outdated indication for affected results. Failed refresh never makes prior advice current for a new selection.

#### AC-06 — Protect accepted context

Known rejected/failed changes retain the last accepted selection; uncertain changes require outcome review rather than a saved claim. Wrong-account/unusable access cannot inspect or change private context.

#### AC-07 — Minimize external exchange

Weather retrieval receives only the selected location necessary for its purpose, not unrelated wardrobe/profile/history. Continuous background tracking and permission as a prerequisite are not introduced.

### 6. Quality and Verification

Apply the [shared quality and completion conditions](../../../stories/README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

### 7. Dependencies

- Parent PBI capability inputs: PBI-004.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

### 8. Out of Scope

Recommendation, Coverage and Multiplier consume this context but do not acquire weather separately. No provider, location precision, polling schedule or new timeout is selected.

### 10. Open Refinement Notes

Provider and supported-city realization details remain JIT. Developers establish relevant delivery/test access before selecting dependent work; no new weather policy is needed.

## 8. Original Use Cases, Flows and Architecture Decisions — Verbatim

### 8.1 [UC-006](../../../../03-requirements/use-cases/UC-006-set-location-context.md)

#### 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-006 |
| Use Case Name | Set Location Context |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | Device Location Service; Weather Information Provider |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../../../01-business/BRD.md) defines business intent; [PRD](../../../../02-product/PRD.md) defines product behavior; [SRS](../../../../03-requirements/SRS.md) and [Business Rules](../../../../03-requirements/business-rules.md) constrain interaction; the [master Use Case Diagram](../../../../03-requirements/use-cases/use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

#### 2. Goal

Select or update the location/environmental context used by CapsuleAI through optional consented device location or manual city/location, with explicit limited-context recovery.

#### 4. Preconditions

- The User has authenticated access authorized for personal context settings.

#### 5. Trigger

The User chooses location setup/change or retries unavailable environmental context.

#### 6. Main Success Scenario

1. The User requests location/context setup or revision.
2. CapsuleAI explains device-location purpose and offers device location, manual city/location or skipping optional setup.
3. The User chooses device location and grants the needed permission.
4. Device Location Service supplies available consented location information; CapsuleAI identifies the selected location.
5. Weather Information Provider supplies available information for the selected location; CapsuleAI presents the actual context/timestamp used, treating weather as current only at age ≤30 minutes and bounding any external-request assessment delay to 2 seconds.
6. The User reviews the selected context and continues.
7. CapsuleAI uses the accepted location/environmental context in relevant advice and reevaluates affected assessments or marks them outdated after a relevant change.

#### 7. Alternative Flows

##### A1 — Manual city/location

At Main Step 3:

1. The User chooses a supported city/location manually, independently of device permission.
2. CapsuleAI identifies that selected location; resume at Main Step 5 when environmental information can be retrieved.

##### A2 — Decline permission

At Main Step 3:

1. The User declines device-location access.
2. CapsuleAI offers manual location or skip; continue through the manual alternative or the skip alternative without denying core authenticated entry.

##### A3 — Skip optional location

At Main Step 3:

1. The User skips location setup.
2. CapsuleAI identifies absent/limited environmental context and allows other core journeys where their remaining information supports validity; no location or forecast is invented.

##### A4 — Change or retry existing context

At Main Step 1:

1. The User revisits an existing selection to change location or retry unavailable context retrieval.
2. Resume at Main Step 2; changed relevant context cannot leave affected old advice labeled current.

#### 8. Exception / Failure Flows

##### E1 — Device location unavailable

At Main Step 4:

1. CapsuleAI explains that device location could not be obtained and offers manual city/location or skip.
2. The User takes an available alternative without a mandatory permission or device-service-success gate.

##### E2 — Weather unavailable or stale

At Main Step 5:

1. CapsuleAI attempts refresh of weather older than 30 minutes; without usable current information within the 2-second boundary, it identifies unavailable environment and offers context review/retry.
2. The selected location is not confused with a known forecast; other evaluations may use disclosed reduced context while retaining remaining validity rules.

##### E3 — Location update or communication outcome unconfirmed

At Main Step 7:

1. CapsuleAI distinguishes a failed/unconfirmed setting change from the last accepted selection.
2. The User reviews actual context or retries; an older assessment is not presented as refreshed for an unestablished new context.

##### E4 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

#### 9. Success Postconditions

- The available selected location and environmental context, or the explicit skipped/limited-context state, are understandable to the User.
- Relevant advice uses the identified context; affected prior assessments are refreshed or marked outdated.
- Device location is used only with permission; manual city/location remains available independently.

#### 10. Minimal / Failure Postconditions

- Absent permission, missing weather or provider failure creates no fabricated location/environmental result.
- Canceled/rejected/known failed changes do not overwrite last accepted context; uncertain changes require outcome review.
- Other authorized core paths remain available subject to network/access and sufficient valid information.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

#### 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../../../../03-requirements/business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-PROF-003` | Manual/device/absent context remains explicit and relevant changes invalidate affected advice. |
| `BRULE-OUT-006` | Missing weather does not automatically reject every outfit; remaining validity still applies. |
| `BRULE-COV-006` | Relevant environmental/context changes refresh or invalidate Coverage. |
| `BRULE-MULT-008` | Relevant context changes invalidate prior candidate utility. |

#### 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- Consent is specific to the optional device-location route; selecting a manual city does not require device access.
- Weather is current at age ≤30 minutes from its applicable retrieval/observation timestamp. Older weather is refreshed before current use; an external request may delay the assessment at most 2 seconds. Without usable current information by that boundary, disclose unavailable environment and use valid reduced context. No provider or background-tracking schedule is prescribed.

- Weather Information Provider receives only the selected location necessary for its request, not unrelated wardrobe/profile/history. Device-location permission remains optional and purpose-specific.
- The 30-minute freshness boundary is inclusive; unusable weather at the 2-second boundary affects environmental evidence, not authenticated entry or all outfits.

#### 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

### 8.2 [AD-006](../../../../03-requirements/activity-diagrams/AD-006-set-location-context.puml)

```plantuml
@startuml
' Source Use Case: UC-006 — Set Location Context
' Primary Specification: ../use-cases/UC-006-set-location-context.md
' Supporting Authorities: ../SRS.md (v0.3); ../business-rules.md (v0.1.2)
' Business/Product: ../../01-business/BRD.md (v0.3); ../../02-product/PRD.md (v0.2)
' Master Actors/Scope: ../use-cases/use-case-diagram.puml
' Process Authority: ../../../Initial files/CapsuleAI_Scrum_Development_Workflow.md, Sections 11, 39–42, 46
' Version: 0.1; Status: Baseline Draft
' SRS trace: FR-WEATHER-001, FR-WEATHER-002, FR-WEATHER-003, FR-WEATHER-004, FR-WEATHER-005, SI-002, HW-003, ERR-WEATHER-001
' Rule trace: BRULE-AUTH-001, BRULE-PROF-003, BRULE-OUT-006, BRULE-COV-006, BRULE-MULT-008, BRULE-AUTH-007
' Flow coverage: UC Sections 6–10; alternatives and failures retain their textual meaning.
' The textual Use Case Specification remains the normative behavioral source.

title AD-006 - Set Location Context
skinparam monochrome true
skinparam shadowing false

|User|
start
:Request location setup, change or retry;
|CapsuleAI|
if (Authorized access usable?) then (Yes)
    :Explain optional location purpose and choices;
else (No)
    :Pause protected use; require renewed access (UC-002);
    stop
endif
repeat
    |User|
    :Choose device location, manual location or skip;
    if (Location route?) then (Device)
        :Grant or decline device permission;
        if (Permission granted?) then (Yes)
            |Device Location Service|
            :Provide available consented location;
            |CapsuleAI|
            if (Usable location obtained?) then (Yes)
                :Identify proposed location;
            else (No)
                :Explain unavailable device location;
            endif
        else (No)
            |CapsuleAI|
            :Keep device access declined;
        endif
        if (Device location unavailable or declined?) then (Yes)
            :Offer independent manual location or skip;
            |User|
            :Choose manual location or skip;
        endif
    endif
    |User|
    if (Manual location selected?) then (Yes)
        :Select supported city/location;
        |CapsuleAI|
        :Identify proposed manual location;
    endif
    |CapsuleAI|
    if (Setup skipped?) then (Yes)
        :Disclose absent/limited environmental context;
        :Allow remaining defensible core journeys;
        stop
    endif
    if (Usable weather for selection at age <=30 minutes?) then (Yes)
        :Identify current weather and timestamp;
    else (No)
        :Request current weather / refresh stale weather;
        note right
          Share only required selected location.
          External request delays assessment at most 2 seconds.
        end note
        |Weather Information Provider|
        :Supply available selected-location information;
        |CapsuleAI|
        if (Usable current weather obtained within boundary?) then (Yes)
            :Identify actual context and timestamp;
        else (No)
            :Disclose unavailable environment; do not invent forecast;
            :Offer context review/retry; preserve other hard rules;
        endif
    endif
    |User|
    if (Proceed with shown context?) then (Yes)
    :Review selected context and continue;
    |CapsuleAI|
    if (Setting change accepted?) then (Yes)
        :Use accepted context in relevant advice;
        :Reevaluate affected assessments or mark Outdated;
        :Confirm selected or disclosed limited context;
        stop
    else (Failed / unconfirmed)
        :Distinguish known failure from uncertain outcome;
        :Keep last accepted selection on known failure;
        |User|
        :Review actual context before retrying;
    endif
    else (Review / retry)
        :Review or change selected context;
    endif
repeat while (Change selection or retry context?) is (Yes) not (No)
|CapsuleAI|
:End without claiming an unconfirmed context change;
stop

@enduml
```

### 8.3 Selected architecture decisions — verbatim from [selected decisions](../../../../04-architecture/selected-architecture-decisions.md)

| ADA ID | Decision Problem | Selected Option | Backend Impact | Status |
| --- | --- | --- | --- | --- |
| ADA-006 | External integration boundaries | Option A — Narrow explicit ports/adapters for real dependencies | Isolate provider semantics behind narrow business ports/adapters. | Selected |
| ADA-010 | Advice and wardrobe-intelligence computation | Option A — Request-time evaluation from authoritative inputs with shared semantics | Evaluate current inputs with shared validity/identity and truthful results. | Selected |
| ADA-011 | Dedicated cache strategy | Option A — No dedicated cache initially | Start without a dedicated cache and measure complete operations. | Selected |
| ADA-014 | Protected observability and validation evidence | Option A — Minimal protected structured logs, metrics and health/recovery evidence | Expose protected outcome, timing, health and recovery evidence. | Selected |
| ADA-016 | Context Data Ownership | Option B — One PostgreSQL database initially with explicit context-owned logical persistence boundaries | Own persistence logically per context within one PostgreSQL. | Selected |
| ADA-017 | Inter-Context Communication | Option B — Explicit published module contracts, choosing synchronous local interaction or event-based interaction according to business semantics | Choose local calls/events through public contracts by business semantics. | Selected |
| ADA-018 | Context Model Isolation and Anti-Corruption | Option B — Published contracts + Anti-Corruption Layer translation at meaningful semantic boundaries | Keep models private and translate meanings at real semantic boundaries. | Selected |

### 8.4 Sprint 4 decisions — verbatim from the [plan register](../sprint-plan.md#13-consolidated-decision-register)

> **Status:** the Product Owner approved all 30 Sprint 4 recommendations on **2026-11-16**. The plan's register, version 0.2 of 2026-11-17, records that approval and is reproduced below; see [open-questions.md](open-questions.md#1-decision-authority).

| Decision ID | Related Story | Question | Recommended Solution | Decision Status | Approval Owner |
| --- | --- | --- | --- | --- | --- |
| [S4-CTX-01](../sprint-plan.md#s4-ctx-01) | US-CTX-001 | Manual and device-selected location representation | Option B — one minimal current selection with PO-reviewed precision and manual resolution. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead, Mobile and privacy reviewer |
| [S4-CTX-02](../sprint-plan.md#s4-ctx-02) | US-CTX-001 | Location persistence, revision and weather failure separation | Option B — independently revisioned current location, with provider evidence bound to that revision. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and QA |
| [S4-CTX-03](../sprint-plan.md#s4-ctx-03) | US-CTX-001 | Actual weather provider and semantic translation | Option A — evaluate Open-Meteo first, conditional on confirmed deployment eligibility, terms and actual integration evidence. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and operations owner |
| [S4-CTX-04](../sprint-plan.md#s4-ctx-04) | US-CTX-001 | Freshness, latest evidence and truthful stale handling | Option B — minimal latest evidence inside Personal Context, with on-demand refresh and no separate cache/history service. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and QA |
| [S4-CTX-05](../sprint-plan.md#s4-ctx-05) | US-CTX-001 | One acquisition deadline and explicit unavailable fallback | Option A — one bounded adapter attempt under a single elapsed deadline, with explicit unavailable outcome. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and QA |
| [S4-CTX-06](../sprint-plan.md#s4-ctx-06) | US-CTX-001 | Current environmental basis and unavailable consumer evidence | Option A — authoritative current/unavailable facts and basis comparison, without speculative result invalidation infrastructure. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and QA |
| [S4-INT-02](../sprint-plan.md#s4-int-02) | All six selected stories | Logical action outcomes and uncertainty recovery | Option A — targeted durable identities and explicit current-state review, adding a receipt only where natural state cannot prove the required outcome. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and QA |
| [S4-INT-03](../sprint-plan.md#s4-int-03) | All six selected stories | Compatible command transport and synchronized delivery contracts | Option A — compatible explicit confirmed commands, preserving existing PATCH/body-revision conventions and resource grouping. | APPROVED — 2026-11-16 | Product Owner with Backend Tech Lead and QA |

## 9. Engineering Interpretation

| Source obligation | Engineering meaning | Where |
| --- | --- | --- |
| AC-01: permission-independent choices | `DEVICE` and `MANUAL` are both accepted. Skip means no request; clearing is a `null` selection. The backend never requires, attests or stores permission state. Access is not affected. | P3, P21, T12 |
| AC-02: actual evidence, a city is not weather | The selection and the weather are separate fields. Weather has `observedAt` (provider evidence time), `retrievedAt` (acquisition time) and `basis: MODELLED`. Without usable weather the status is `UNAVAILABLE`, never a forecast. | P6, P9, P26, T12 |
| AC-03: freshness, 2-second delay | Inclusive 30 minutes on `observedAt`, re-checked at use (`WeatherFreshness`). One elapsed deadline of `PT2S` in the adapter, covering capacity wait, connection, body and translation. | P7, P19, P23, T2, T4 |
| AC-04: disclosed fallback | `UNAVAILABLE` with a reason, and the selection still returned. **Skipping only environmental hard filtering is a consumer behavior; no consumer exists.** | P19, P26; §15 |
| AC-05: accepted changes inform later advice | A change deletes the old evidence in the same transaction. The new evidence is bound to the new revision, and a late old result is never published (`SELECTION_CHANGED`). **Reevaluation of affected results: no result store exists.** | P22, T13; §15 |
| AC-06: protect accepted context | Validation or a conflict changes nothing. A provider failure after commit keeps the new selection. Wrong accounts and unusable sessions get nothing. | P21, T10, T12 |
| AC-07: minimal external exchange | The provider receives the rounded latitude and longitude plus fixed parameters only: no account, label, wardrobe or profile data, no key. No polling. | P23, T4 |
| S4-CTX-02 commit, then acquire | `ReviseLocationService` commits, then calls `EnvironmentalContextService` outside any transaction. Publication is a short transaction that locks the location row `FOR SHARE`. | P19, P21, P22 |
| S4-CTX-06 basis | `EnvironmentalContext` (`NoSelection`, `Current` or `Unavailable`) carries the location revision | P9, P26 |

## 10. Just-in-Time Detailed Design

### 10.1 Sequence: change the location

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant Revise as ReviseLocationService
    participant Repository as LocationRepository
    participant Environment as EnvironmentalContextService
    participant Provider as OpenMeteoWeatherProvider
    participant OpenMeteo as Open-Meteo (HTTPS)
    Client->>Revise: PATCH {expectedRevision, selection}
    Revise->>Revise: validate + round to 2 decimals
    Revise->>Repository: [tx] createIfAbsent / reviseIfCurrentAt (+ delete old evidence)
    Note over Revise,Repository: committed: the selection is accepted
    Revise->>Environment: currentFor(owner, new profile) (no transaction)
    Environment->>Provider: acquire(rounded point)
    Provider->>OpenMeteo: GET /v1/forecast?latitude&longitude&current=…&timezone=GMT&timeformat=unixtime
    alt response within the 2-second deadline and valid
        Provider-->>Environment: Acquired(reading)
        Environment->>Environment: observedAt within 30 minutes (inclusive)?
        Environment->>Repository: [tx] publishEvidenceIfCurrent(revision) (location FOR SHARE, upsert)
        Environment-->>Client: 200 selection + CURRENT weather
    else deadline, failure, invalid or stale
        Environment-->>Client: 200 selection + UNAVAILABLE(reason)
    end
```

### 10.2 Environment states

```mermaid
stateDiagram-v2
    [*] --> NO_SELECTION
    NO_SELECTION --> CURRENT: selection accepted + current reading published
    NO_SELECTION --> UNAVAILABLE: selection accepted + acquisition failed
    CURRENT --> CURRENT: read within 30 min (no provider call)
    CURRENT --> UNAVAILABLE: evidence older than 30 min and refresh failed
    UNAVAILABLE --> CURRENT: later read acquires a current reading
    CURRENT --> NO_SELECTION: selection cleared (evidence deleted)
    UNAVAILABLE --> NO_SELECTION: selection cleared
```

### 10.3 Deadline design (S4-CTX-05, OPEN-S4-22)

| Phase | Bounded by |
| --- | --- |
| Waiting for capacity (`maximum-concurrent-requests` = 8) | `Semaphore.tryAcquire(remaining)` from the single deadline |
| Connect and TLS | The caller waits at most the remaining time (`Future.get`); `HttpClient.connectTimeout` = deadline as a backstop |
| Headers and body | The same `Future.get(remaining)`; `JdkClientHttpRequestFactory.setReadTimeout(deadline)` as a backstop; body read through a 64 KiB limit |
| JSON translation | Inside the task, so inside the same wait |
| On timeout | `cancel(true)`, then `DEADLINE_EXCEEDED`; the late result is discarded. The permit is released only when the task really ends, so stuck calls cannot multiply threads or queues. |

The deadline is never split per phase and there is no retry.

### 10.4 Provider request and translation (S4-CTX-03, OPEN-S4-20)

`GET {base-url}/v1/forecast` with these parameters:

- `latitude=<2-decimal>`, `longitude=<2-decimal>`;
- `current=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m`;
- `timezone=GMT`, `timeformat=unixtime`.

| Provider field | Evidence field | Rule |
| --- | --- | --- |
| `current.time` (UTC seconds) | `observedAt` | Required; integral |
| `current.temperature_2m` | `temperatureCelsius` | Required; unit `°C` |
| `current.apparent_temperature` | `apparentTemperatureCelsius` | Optional; unit `°C` |
| `current.relative_humidity_2m` | `relativeHumidityPercent` | Optional; 0–100; unit `%` |
| `current.precipitation` | `precipitationMillimetres` | Optional; ≥ 0; unit `mm` |
| `current.wind_speed_10m` | `windSpeedKilometresPerHour` | Optional; ≥ 0; unit `km/h` |
| `current.weather_code` (WMO) | `weatherCode` + `condition` | Required; unknown codes are invalid |
| `latitude`, `longitude` | — | Must be within 0.5° of the request (grid snapping) |
| — | `retrievedAt` | Server clock when the response was translated |
| — | `basis` | Always `MODELLED`; never presented as an observation |

### 10.5 API contract

| Operation | Contract |
| --- | --- |
| `GET /api/v1/personal-context/location` | `200 LocationContext {revision, selection, environment}`. It may refresh weather on demand, within 2 s. |
| `PATCH /api/v1/personal-context/location` | `{expectedRevision (required), selection?}`: an omitted selection is kept (no-op), `null` clears it, an object replaces it (`latitude`, `longitude`, `source`, `label`). `200 LocationContext`. `400 VALIDATION_FAILED`. `409 REVISION_CONFLICT`. |
| `environment` | `{status, basisRevision, unavailableReason, weather}`: `status` is `NO_SELECTION`, `CURRENT` or `UNAVAILABLE`; `basisRevision` is the location revision the status applies to (null for `NO_SELECTION`) |
| `weather` | `{condition, weatherCode, temperatureCelsius, apparentTemperatureCelsius, relativeHumidityPercent, precipitationMillimetres, windSpeedKilometresPerHour, observedAt, retrievedAt, basis, provider, attribution}` |

Field errors use these codes:

| Field | Codes |
| --- | --- |
| `selection.latitude` | `REQUIRED` or `UNSUPPORTED_VALUE` (not a number, or out of range) |
| `selection.longitude` | same as latitude |
| `selection.source` | `REQUIRED` or `UNSUPPORTED_VALUE` |
| `selection.label` | `REQUIRED` (for `MANUAL`) or `INVALID_FORMAT` (more than 80 code points, control characters, wrong type) |
| `selection` | `UNSUPPORTED_VALUE` (not an object) |

### 10.6 Data (V14)

| Table | Columns | Rules |
| --- | --- | --- |
| `personal_context_location` | `owner_account_id` (PK), `latitude NUMERIC(4,2)`, `longitude NUMERIC(5,2)`, `label`, `source`, `revision`, `created_at`, `updated_at` | All selection columns are set together or are all NULL. `MANUAL` requires a label. Ranges are checked. |
| `personal_context_weather_evidence` | `owner_account_id` (PK, FK to location, `ON DELETE CASCADE`), `location_revision`, `observed_at`, `retrieved_at`, the measurements, `weather_code`, `provider`, `basis` | Latest only. Never replaced by older evidence of the same revision. |

### 10.7 Concurrency

| Race | Outcome |
| --- | --- |
| Change or clear while weather is being acquired | Acquisition holds no lock. Publication locks the location row `FOR SHARE` and checks the revision. If the selection moved on, the result is `SELECTION_CHANGED` and nothing is published. |
| Two refreshes of the same revision | The upsert keeps the newer `observed_at`. An older reading answers `KEPT_NEWER`, and the reading stays current to show. |
| Two location changes of the same revision | The conditional `UPDATE` lets one win; the other gets `409` |
| Location change and preference or optional-context change | Different rows; never blocked |

### 10.8 Security and privacy

- The base URL comes from configuration only: HTTPS, except a loopback host for tests. Redirects are not followed.
- Coordinates, labels and URLs are never logged; only the outcome category is.
- No credential exists for Open-Meteo's free tier.
- The provider never receives the account id, the label or any other data.

### 10.9 Testing plan

| Level | Classes |
| --- | --- |
| Domain | `LocationSelectionTest`, `WeatherFreshnessTest` |
| Adapter | `OpenMeteoResponseTranslatorTest`; `OpenMeteoWeatherProviderTest` (local JDK `HttpServer` with controlled delays) |
| Application | `ReviseLocationServiceTest` (with `InMemoryLocationRepository` and `ControllableWeatherProvider`) |
| Schema | `LocationSchemaIT`, updated `PreferencesSchemaIT` |
| HTTP | `LocationContextApiIT` |
| Concurrency | `ConcurrentLocationIT` |
| External (manual, not CI) | §14 M7: one real Open-Meteo request in the agreed environment |

## 11. File Change Matrix

`…/pc/` is `backend/src/main/java/com/capsuleai/personalcontext/` and `…/test/pc/` is `backend/src/test/java/com/capsuleai/personalcontext/`.

| Order | Action | Exact repository path | Purpose | Dependencies |
| --- | --- | --- | --- | --- |
| P1 | ADD | `backend/src/main/resources/db/migration/V14__create_personal_context_location.sql` | Location and evidence tables | V13 |
| P2 | ADD | `…/pc/domain/LocationSource.java` | `DEVICE`, `MANUAL` | — |
| P3 | ADD | `…/pc/domain/LocationSelection.java` | Rounded point, label, source | P2 |
| P4 | ADD | `…/pc/domain/LocationProfile.java` | Selection and revision | P3 |
| P5 | ADD | `…/pc/domain/WeatherCondition.java` | WMO code mapping | — |
| P6 | ADD | `…/pc/domain/WeatherReading.java` | Translated evidence | P5 |
| P7 | ADD | `…/pc/domain/WeatherFreshness.java` | Inclusive 30-minute rule | — |
| P8 | ADD | `…/pc/domain/EnvironmentUnavailableReason.java` | Reasons | — |
| P9 | ADD | `…/pc/application/EnvironmentalContext.java` | Basis: none, current or unavailable | P6, P8 |
| P10 | ADD | `…/pc/application/LocationContextView.java` | Profile plus environment | P4, P9 |
| P11 | ADD | `…/pc/application/ports/WeatherQuery.java` | Location-only request | — |
| P12 | ADD | `…/pc/application/ports/WeatherAcquisition.java` | Acquired or unavailable | P6, P8 |
| P13 | ADD | `…/pc/application/ports/WeatherProvider.java` | Port | P11, P12 |
| P14 | ADD | `…/pc/application/ports/LocationRepository.java` | Persistence port | P4, P6 |
| P15 | ADD | `…/pc/application/SubmittedLocation.java` | Raw selection input | — |
| P16 | ADD | `…/pc/application/ReviseLocationCommand.java` | Command | P15 |
| P17 | ADD | `…/pc/application/LocationValidationException.java` | Rejection | — |
| P18 | ADD | `…/pc/application/LocationRevisionConflictException.java` | Conflict | — |
| P19 | ADD | `…/pc/application/EnvironmentalContextService.java` | Freshness, acquisition, publication | P7, P9, P13, P14 |
| P20 | ADD | `…/pc/application/ViewLocationContextService.java` | Read | P19 |
| P21 | ADD | `…/pc/application/ReviseLocationService.java` | Change or clear | P14–P19 |
| P22 | ADD | `…/pc/adapter/out/persistence/JdbcLocationRepository.java` | SQL | P1, P14 |
| P23 | ADD | `…/pc/adapter/out/weather/OpenMeteoWeatherProvider.java` | Bounded HTTP adapter | P13, P24 |
| P24 | ADD | `…/pc/adapter/out/weather/OpenMeteoResponseTranslator.java` | Payload translation | P6 |
| P25 | ADD | `…/pc/adapter/in/web/LocationRevisionRequest.java` | Request | P16 |
| P26 | ADD | `…/pc/adapter/in/web/LocationContextResponse.java` | Response | P10 |
| P27 | ADD | `…/pc/adapter/in/web/PersonalContextLocationController.java` | Endpoints | P20, P21, P25, P26 |
| P28 | MODIFY | `…/pc/adapter/in/web/PersonalContextApiExceptionHandler.java` | Two handlers | P17, P18 |
| P29 | ADD | `…/pc/configuration/PersonalContextWeatherProperties.java` | Typed configuration | — |
| P30 | ADD | `…/pc/configuration/PersonalContextLocationConfiguration.java` | Beans | P19–P24, P29 |
| P31 | MODIFY | `backend/src/main/resources/application.properties` | Defaults | P29 |
| P32 | MODIFY | `docs/05-api/openapi.yaml` | 0.15.0 → 0.16.0 | P25–P27 |
| P33 | MODIFY | `.github/workflows/backend-ci.yml` | 9 entries | T1–T15 |
| P34 | MODIFY | `backend/src/main/resources/application-local.properties` | Local runs make no provider request (review 2026-11-23) | P31 |
| P35 | MODIFY | `backend/README.md` | Endpoint and weather switch (review 2026-11-23) | P31, P34 |
| T1 | ADD | `…/test/pc/domain/LocationSelectionTest.java` | Rounding and labels | P3 |
| T2 | ADD | `…/test/pc/domain/WeatherFreshnessTest.java` | 30-minute boundary, WMO codes | P5, P7 |
| T3 | ADD | `…/test/pc/adapter/out/weather/OpenMeteoResponseTranslatorTest.java` | Translation | P24 |
| T4 | ADD | `…/test/pc/adapter/out/weather/OpenMeteoWeatherProviderTest.java` | Deadline and failures | P23 |
| T5 | ADD | `…/test/pc/support/ControllableWeatherProvider.java` | Test double | P13 |
| T6 | MODIFY | `backend/src/test/java/com/capsuleai/identity/support/IdentityTestConfiguration.java` | `@Primary` double | T5 |
| T7 | MODIFY | `backend/src/test/resources/application-test.properties` | Disable the real adapter | P29 |
| T8 | MODIFY | `…/test/pc/support/PersonalContextIntegrationTestSupport.java` | Truncate, helpers, reset | P1, T5 |
| T9 | ADD | `…/test/pc/application/InMemoryLocationRepository.java` | Fake | P14 |
| T10 | ADD | `…/test/pc/application/ReviseLocationServiceTest.java` | Lifecycle rules | P19–P21 |
| T11 | ADD | `…/test/pc/adapter/out/persistence/LocationSchemaIT.java` | V14 | P1 |
| T12 | ADD | `…/test/pc/adapter/in/web/LocationContextApiIT.java` | HTTP | P27 |
| T13 | ADD | `…/test/pc/application/ConcurrentLocationIT.java` | Late-result fencing | P19, P22 |
| T14 | MODIFY | `…/test/pc/adapter/out/persistence/PreferencesSchemaIT.java` | Exact table list | P1 |
| T15 | ADD | `…/test/pc/configuration/PersonalContextWeatherPropertiesTest.java` | Startup validation | P29 |

## 12. Complete Production Implementation

The BEFORE blocks are the files as left by US-PROF-002. Several Personal Context files use CRLF line endings; keep them when editing. Commands run from `backend/`.

### Step P1 — [ADD] `backend/src/main/resources/db/migration/V14__create_personal_context_location.sql`

```sql
-- US-CTX-001 (S4-CTX-01/02/04, approved 2026-11-16; OPEN-S4-18/19/21): the user's one current location
-- selection and the latest weather evidence acquired for it. No history is kept.
--
-- A selection is a point rounded by the server to 2 decimals (about 1.1 km), an optional label (required for
-- MANUAL) and its source. Clearing sets every selection column to NULL and keeps the row, so the revision line
-- continues. Weather evidence is bound to the location revision it was acquired for and is deleted in the same
-- transaction as any change or clear of the selection; a provider refresh never changes the location revision.
--
-- Owner: Personal Context. Every object below is in the personal_context schema (AR-01); owner_account_id is
-- the authenticated Identity account id with no foreign key into Identity (ADA-016, AR-04).
CREATE TABLE personal_context.personal_context_location (
    owner_account_id UUID          PRIMARY KEY,
    latitude         NUMERIC(4, 2) NULL,
    longitude        NUMERIC(5, 2) NULL,
    label            TEXT          NULL,
    source           TEXT          NULL,
    revision         BIGINT        NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL,

    -- A selection is complete or absent; IS NULL comparisons are never NULL themselves.
    CONSTRAINT ck_personal_context_location_complete CHECK ((latitude IS NULL) = (longitude IS NULL)
        AND (latitude IS NULL) = (source IS NULL) AND (latitude IS NOT NULL OR label IS NULL)),
    CONSTRAINT ck_personal_context_location_latitude CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_personal_context_location_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_personal_context_location_source CHECK (source IS NULL OR source IN ('DEVICE', 'MANUAL')),
    CONSTRAINT ck_personal_context_location_manual_label CHECK (source IS DISTINCT FROM 'MANUAL' OR label IS NOT NULL),
    CONSTRAINT ck_personal_context_location_label CHECK (label IS NULL OR char_length(label) BETWEEN 1 AND 80),
    CONSTRAINT ck_personal_context_location_revision CHECK (revision >= 1),
    CONSTRAINT ck_personal_context_location_updated_after_created CHECK (updated_at >= created_at)
);

-- The latest translated weather evidence for the owner's current selection (no archive, no raw payload).
CREATE TABLE personal_context.personal_context_weather_evidence (
    owner_account_id               UUID          PRIMARY KEY
        REFERENCES personal_context.personal_context_location (owner_account_id) ON DELETE CASCADE,
    location_revision              BIGINT        NOT NULL,
    observed_at                    TIMESTAMPTZ   NOT NULL,
    retrieved_at                   TIMESTAMPTZ   NOT NULL,
    temperature_celsius            NUMERIC(6, 2) NOT NULL,
    apparent_temperature_celsius   NUMERIC(6, 2) NULL,
    relative_humidity_percent      SMALLINT      NULL,
    precipitation_millimetres      NUMERIC(7, 2) NULL,
    wind_speed_kilometres_per_hour NUMERIC(6, 2) NULL,
    weather_code                   SMALLINT      NOT NULL,
    provider                       TEXT          NOT NULL,
    basis                          TEXT          NOT NULL,

    CONSTRAINT ck_personal_context_weather_evidence_revision CHECK (location_revision >= 1),
    CONSTRAINT ck_personal_context_weather_evidence_humidity
        CHECK (relative_humidity_percent IS NULL OR relative_humidity_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_personal_context_weather_evidence_precipitation
        CHECK (precipitation_millimetres IS NULL OR precipitation_millimetres >= 0),
    CONSTRAINT ck_personal_context_weather_evidence_wind
        CHECK (wind_speed_kilometres_per_hour IS NULL OR wind_speed_kilometres_per_hour >= 0),
    CONSTRAINT ck_personal_context_weather_evidence_code CHECK (weather_code BETWEEN 0 AND 99),
    CONSTRAINT ck_personal_context_weather_evidence_provider CHECK (provider IN ('OPEN_METEO')),
    CONSTRAINT ck_personal_context_weather_evidence_basis CHECK (basis IN ('MODELLED', 'OBSERVED'))
);
```

**Explanation.** `NUMERIC(4,2)` holds −90.00 to 90.00 and `NUMERIC(5,2)` holds −180.00 to 180.00. The column scale equals the rounding precision, so the database cannot hold more precision than OPEN-S4-18 allows.

### Step P2 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/LocationSource.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * How the user chose a location (US-CTX-001 AC-01). The backend cannot attest that a permission dialog was
 * shown; DEVICE only states that the client submitted a device-derived point, rounded by the server.
 */
public enum LocationSource {
    DEVICE,
    MANUAL;

    /** The value whose code equals {@code code} exactly; no case folding or fallback. */
    public static Optional<LocationSource> fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.name().equals(code)).findFirst();
    }
}
```

### Step P3 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/LocationSelection.java`

```java
package com.capsuleai.personalcontext.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Objects;
import java.util.Optional;

/**
 * US-CTX-001 (S4-CTX-01, OPEN-S4-18) — the user's one current location: a point at the reviewed precision, a
 * display label and its source. The server rounds every submitted point to {@link #SCALE} decimals (about
 * 1.1 km) before it is stored or sent to the weather provider; raw device precision is never kept. A MANUAL
 * selection needs a label (the city the client resolved); a DEVICE selection may have none.
 */
public record LocationSelection(BigDecimal latitude, BigDecimal longitude, Optional<String> label,
                                LocationSource source) {

    /** OPEN-S4-18: two decimal places. */
    public static final int SCALE = 2;

    /** OPEN-S4-18: the longest label, in Unicode code points after normalization. */
    public static final int MAXIMUM_LABEL_CODE_POINTS = 80;

    private static final BigDecimal MAXIMUM_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MAXIMUM_LONGITUDE = new BigDecimal("180");

    public LocationSelection {
        Objects.requireNonNull(latitude, "latitude");
        Objects.requireNonNull(longitude, "longitude");
        Objects.requireNonNull(label, "label must be Optional.empty() without a label");
        Objects.requireNonNull(source, "source");
        if (latitude.scale() != SCALE || longitude.scale() != SCALE) {
            throw new IllegalArgumentException("Coordinates must be rounded to " + SCALE + " decimals");
        }
        if (!withinRange(latitude, MAXIMUM_LATITUDE) || !withinRange(longitude, MAXIMUM_LONGITUDE)) {
            throw new IllegalArgumentException("Coordinates are outside the geographic range");
        }
        if (label.isPresent() && !isAcceptableLabel(label.get())) {
            throw new IllegalArgumentException("The label must be normalized, 1-80 code points, without control characters");
        }
        if (source == LocationSource.MANUAL && label.isEmpty()) {
            throw new IllegalArgumentException("A manual selection needs a label");
        }
    }

    /** A selection from submitted values, with both coordinates rounded half-even to {@link #SCALE} decimals. */
    public static LocationSelection rounded(BigDecimal latitude, BigDecimal longitude, Optional<String> label,
                                            LocationSource source) {
        return new LocationSelection(round(latitude), round(longitude), label, source);
    }

    public static BigDecimal round(BigDecimal coordinate) {
        return coordinate.setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    /** Whether a rounded latitude lies within -90 to 90. */
    public static boolean isLatitude(BigDecimal roundedLatitude) {
        return withinRange(roundedLatitude, MAXIMUM_LATITUDE);
    }

    /** Whether a rounded longitude lies within -180 to 180. */
    public static boolean isLongitude(BigDecimal roundedLongitude) {
        return withinRange(roundedLongitude, MAXIMUM_LONGITUDE);
    }

    /** The NFC-normalized label without outer whitespace, or empty when missing or blank. */
    public static Optional<String> normalizeLabel(String rawLabel) {
        if (rawLabel == null) {
            return Optional.empty();
        }
        String normalized = Normalizer.normalize(rawLabel, Normalizer.Form.NFC).strip();
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }

    /** Whether a normalized label is short enough and free of control characters. */
    public static boolean isAcceptableLabel(String normalizedLabel) {
        return !normalizedLabel.isEmpty()
                && normalizedLabel.codePointCount(0, normalizedLabel.length()) <= MAXIMUM_LABEL_CODE_POINTS
                && normalizedLabel.codePoints().noneMatch(Character::isISOControl)
                && normalizeLabel(normalizedLabel).equals(Optional.of(normalizedLabel));
    }

    private static boolean withinRange(BigDecimal value, BigDecimal maximum) {
        return value.compareTo(maximum) <= 0 && value.compareTo(maximum.negate()) >= 0;
    }
}
```

**Explanation.**

- Rounding happens before the range check, so `90.004` becomes `90.00` and is accepted.
- `BigDecimal` keeps the exact decimal the user and the database see. `record` equality compares scale too, which is why the constructor requires `SCALE`.

### Step P4 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/LocationProfile.java`

```java
package com.capsuleai.personalcontext.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 (S4-CTX-02) — one account's location record: the current selection (empty after clearing) and its
 * own revision, independent of preferences and optional context. A provider refresh never changes it.
 */
public record LocationProfile(UUID ownerAccountId, Optional<LocationSelection> selection, long revision,
                              Instant createdAt, Instant updatedAt) {

    public static final long FIRST_REVISION = 1;

    public LocationProfile {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(selection, "selection must be Optional.empty() when cleared");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A location revision starts at " + FIRST_REVISION);
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A location cannot be updated before it was created");
        }
    }

    public static LocationProfile firstSelection(UUID ownerAccountId, LocationSelection selection, Instant selectedAt) {
        return new LocationProfile(ownerAccountId, Optional.of(selection), FIRST_REVISION, selectedAt, selectedAt);
    }

    /** The accepted change or clear: one more revision; the update time never moves before the creation time. */
    public LocationProfile revisedTo(Optional<LocationSelection> revisedSelection, Instant revisedAt) {
        Instant updated = revisedAt.isBefore(createdAt) ? createdAt : revisedAt;
        return new LocationProfile(ownerAccountId, revisedSelection, Math.addExact(revision, 1), createdAt, updated);
    }
}
```

### Step P5 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/WeatherCondition.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Optional;

/**
 * US-CTX-001 (OPEN-S4-20) — a coarse current condition translated from the WMO weather interpretation code the
 * provider reports. Unknown codes are not guessed: the reading is invalid instead.
 */
public enum WeatherCondition {
    CLEAR,
    MAINLY_CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    FREEZING_DRIZZLE,
    RAIN,
    FREEZING_RAIN,
    SNOW,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    THUNDERSTORM_WITH_HAIL;

    /** The condition of a WMO code (0, 1, 2, 3, 45, 48, 51–57, 61–67, 71–77, 80–86, 95, 96, 99). */
    public static Optional<WeatherCondition> fromWmoCode(int code) {
        return Optional.ofNullable(switch (code) {
            case 0 -> CLEAR;
            case 1 -> MAINLY_CLEAR;
            case 2 -> PARTLY_CLOUDY;
            case 3 -> OVERCAST;
            case 45, 48 -> FOG;
            case 51, 53, 55 -> DRIZZLE;
            case 56, 57 -> FREEZING_DRIZZLE;
            case 61, 63, 65 -> RAIN;
            case 66, 67 -> FREEZING_RAIN;
            case 71, 73, 75, 77 -> SNOW;
            case 80, 81, 82 -> RAIN_SHOWERS;
            case 85, 86 -> SNOW_SHOWERS;
            case 95 -> THUNDERSTORM;
            case 96, 99 -> THUNDERSTORM_WITH_HAIL;
            default -> null;
        });
    }
}
```

### Step P6 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/WeatherReading.java`

```java
package com.capsuleai.personalcontext.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * US-CTX-001 (S4-CTX-03/04) — translated weather evidence. {@code observedAt} is the provider's evidence time,
 * the time freshness is measured from; {@code retrievedAt} is when this server received it and never makes old
 * evidence current. {@code basis} tells modelled values from observations: Open-Meteo values are MODELLED.
 */
public record WeatherReading(
        Instant observedAt,
        Instant retrievedAt,
        BigDecimal temperatureCelsius,
        Optional<BigDecimal> apparentTemperatureCelsius,
        Optional<Integer> relativeHumidityPercent,
        Optional<BigDecimal> precipitationMillimetres,
        Optional<BigDecimal> windSpeedKilometresPerHour,
        int weatherCode,
        WeatherCondition condition,
        String provider,
        String basis) {

    public static final String OPEN_METEO = "OPEN_METEO";
    public static final String MODELLED = "MODELLED";

    public WeatherReading {
        Objects.requireNonNull(observedAt, "observedAt");
        Objects.requireNonNull(retrievedAt, "retrievedAt");
        Objects.requireNonNull(temperatureCelsius, "temperatureCelsius");
        Objects.requireNonNull(apparentTemperatureCelsius, "apparentTemperatureCelsius");
        Objects.requireNonNull(relativeHumidityPercent, "relativeHumidityPercent");
        Objects.requireNonNull(precipitationMillimetres, "precipitationMillimetres");
        Objects.requireNonNull(windSpeedKilometresPerHour, "windSpeedKilometresPerHour");
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(basis, "basis");
        if (WeatherCondition.fromWmoCode(weatherCode).orElse(null) != condition) {
            throw new IllegalArgumentException("The condition must be the translation of the weather code");
        }
        relativeHumidityPercent.ifPresent(humidity -> {
            if (humidity < 0 || humidity > 100) {
                throw new IllegalArgumentException("Relative humidity is a percentage");
            }
        });
    }
}
```

### Step P7 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/WeatherFreshness.java`

```java
package com.capsuleai.personalcontext.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * US-CTX-001 AC-03 (S4-CTX-04, OPEN-S4-21) — when weather evidence is current. The approved rule: evidence
 * exactly {@link #CURRENT_FOR} old is current; older evidence is refreshed before current use. Age is measured
 * from the provider's evidence time, never from the time it was fetched. A small tolerance absorbs clock skew
 * for evidence stamped slightly in the future; evidence further in the future is not plausible.
 */
public final class WeatherFreshness {

    /** Approved boundary (BR-013, FR-WEATHER-003): 30 minutes, inclusive. Not configurable. */
    public static final Duration CURRENT_FOR = Duration.ofMinutes(30);

    /** OPEN-S4-21: clock-skew tolerance for evidence stamped after the server's current time. */
    public static final Duration FUTURE_TOLERANCE = Duration.ofMinutes(5);

    private WeatherFreshness() {
    }

    /** True when evidence observed at {@code observedAt} may be used as current weather at {@code now}. */
    public static boolean isCurrentAt(Instant observedAt, Instant now) {
        Duration age = Duration.between(observedAt, now);
        if (age.isNegative()) {
            return age.negated().compareTo(FUTURE_TOLERANCE) <= 0;
        }
        return age.compareTo(CURRENT_FOR) <= 0;
    }

    /** True when evidence is stamped further in the future than the tolerance (an invalid reading). */
    public static boolean isImplausiblyFuture(Instant observedAt, Instant now) {
        return Duration.between(now, observedAt).compareTo(FUTURE_TOLERANCE) > 0;
    }
}
```

### Step P8 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/domain/EnvironmentUnavailableReason.java`

```java
package com.capsuleai.personalcontext.domain;

/** US-CTX-001 AC-04 (S4-CTX-05, OPEN-S4-20/22) — why no current weather is available. Codes are published. */
public enum EnvironmentUnavailableReason {

    /** Weather acquisition is switched off in this deployment. */
    NOT_CONFIGURED,

    /** The provider could not be reached or answered with an error. */
    PROVIDER_UNAVAILABLE,

    /** No usable answer within the single 2-second acquisition deadline. */
    DEADLINE_EXCEEDED,

    /** The answer could not be translated (shape, units, location, codes or a time far in the future). */
    INVALID_PROVIDER_DATA,

    /** The provider's evidence was already older than 30 minutes when it arrived. */
    STALE_PROVIDER_DATA,

    /** The selection changed or was cleared while weather was being acquired; reload the location. */
    SELECTION_CHANGED
}
```

### Step P9 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/EnvironmentalContext.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.util.Objects;

/**
 * US-CTX-001 (S4-CTX-06, OPEN-S4-23) — the environmental basis of the owner's current selection: none, current
 * evidence, or explicitly unavailable. {@code locationRevision} names the selection the basis applies to, so a
 * future consumer can compare bases at use. No consumer exists yet; this type is not published (no api change).
 */
public sealed interface EnvironmentalContext {

    /** No location is selected; no weather is ever requested. */
    record NoSelection() implements EnvironmentalContext {
    }

    /** Current evidence (at most 30 minutes old) for the selection at {@code locationRevision}. */
    record Current(long locationRevision, WeatherReading reading) implements EnvironmentalContext {

        public Current {
            Objects.requireNonNull(reading, "reading");
        }
    }

    /** No usable current weather for the selection at {@code locationRevision}. */
    record Unavailable(long locationRevision, EnvironmentUnavailableReason reason) implements EnvironmentalContext {

        public Unavailable {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
```

### Step P10 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/LocationContextView.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.LocationProfile;
import java.util.Objects;
import java.util.Optional;

/**
 * US-CTX-001 AC-02 — the accepted selection (empty profile: never selected) and, separately, the environmental
 * context of that selection. A selected city alone is never presented as known weather.
 */
public record LocationContextView(Optional<LocationProfile> profile, EnvironmentalContext environment) {

    public LocationContextView {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(environment, "environment");
    }
}
```

### Step P11 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ports/WeatherQuery.java`

```java
package com.capsuleai.personalcontext.application.ports;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * US-CTX-001 AC-07 (OPEN-S4-20) — everything the weather provider receives: the rounded selected point. No
 * account, label, wardrobe, profile or history information is part of a query.
 */
public record WeatherQuery(BigDecimal latitude, BigDecimal longitude) {

    public WeatherQuery {
        Objects.requireNonNull(latitude, "latitude");
        Objects.requireNonNull(longitude, "longitude");
    }
}
```

### Step P12 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ports/WeatherAcquisition.java`

```java
package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.util.Objects;

/** The outcome of one bounded weather acquisition: translated evidence, or an explicit reason it is unavailable. */
public sealed interface WeatherAcquisition {

    record Acquired(WeatherReading reading) implements WeatherAcquisition {

        public Acquired {
            Objects.requireNonNull(reading, "reading");
        }
    }

    record Unavailable(EnvironmentUnavailableReason reason) implements WeatherAcquisition {

        public Unavailable {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
```

### Step P13 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ports/WeatherProvider.java`

```java
package com.capsuleai.personalcontext.application.ports;

/**
 * US-CTX-001 (S4-CTX-03/05) — the narrow weather port. An implementation answers within the configured single
 * acquisition deadline (at most 2 seconds), including any wait for capacity, and reports every failure as
 * {@link WeatherAcquisition.Unavailable} instead of throwing. It is never called inside a database transaction.
 */
@FunctionalInterface
public interface WeatherProvider {

    WeatherAcquisition acquire(WeatherQuery query);
}
```

### Step P14 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ports/LocationRepository.java`

```java
package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Personal Context-owned persistence of the location selection and its latest weather evidence (V14). */
public interface LocationRepository {

    /** Stored evidence and the location revision it was acquired for. */
    record StoredWeatherEvidence(long locationRevision, WeatherReading reading) {

        public StoredWeatherEvidence {
            Objects.requireNonNull(reading, "reading");
        }
    }

    /** The outcome of publishing newly acquired evidence. */
    enum WeatherPublication {
        /** The evidence is now the latest for the current selection. */
        PUBLISHED,
        /** Newer evidence of the same selection was already stored; it was kept. */
        KEPT_NEWER,
        /** The selection changed or was cleared after the acquisition started; nothing was stored. */
        SELECTION_CHANGED
    }

    Optional<LocationProfile> find(UUID ownerAccountId);

    /** Stores the first selection unless a record exists (guarded). Call inside {@link PersonalContextTransactions}. */
    boolean createIfAbsent(LocationProfile profile);

    /**
     * Replaces (or clears) the selection only while the revision is still {@code expectedRevision}; the revision
     * increases by one and any stored evidence is deleted in the same transaction. Call inside
     * {@link PersonalContextTransactions}. @return false if nothing changed
     */
    boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, Optional<LocationSelection> selection,
                              Instant updatedAt);

    Optional<StoredWeatherEvidence> findEvidence(UUID ownerAccountId);

    /**
     * Publishes evidence only if the selection is still present at {@code locationRevision}, holding the location
     * row in share mode, and never replaces newer evidence of the same revision. Call inside
     * {@link PersonalContextTransactions}.
     */
    WeatherPublication publishEvidenceIfCurrent(UUID ownerAccountId, long locationRevision, WeatherReading reading);
}
```

### Step P15 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/SubmittedLocation.java`

```java
package com.capsuleai.personalcontext.application;

import java.math.BigDecimal;
import java.util.List;

/**
 * US-CTX-001 — a submitted selection exactly as received: missing values are null; {@code wrongTypeFields}
 * names the members whose JSON kind was wrong (for example a latitude given as text). Validation and rounding
 * happen in {@link ReviseLocationService}.
 */
public record SubmittedLocation(BigDecimal latitude, BigDecimal longitude, String label, String source,
                                List<String> wrongTypeFields) {

    /** Request field names, shared by the web mapping and the validation so that field errors always match. */
    public static final String LATITUDE_FIELD = "selection.latitude";
    public static final String LONGITUDE_FIELD = "selection.longitude";
    public static final String LABEL_FIELD = "selection.label";
    public static final String SOURCE_FIELD = "selection.source";

    public SubmittedLocation {
        wrongTypeFields = List.copyOf(wrongTypeFields);
    }
}
```

### Step P16 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ReviseLocationCommand.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.UUID;

/**
 * One submitted location change: keep (no selection member), clear (null), replace (an object) or malformed.
 * The owner comes from the authenticated session.
 */
public record ReviseLocationCommand(UUID ownerAccountId, ExpectedRevision expectedRevision,
                                    ValueChange<SubmittedLocation> selection) {
}
```

### Step P17 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/LocationValidationException.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.List;

/** The location change was rejected and nothing was stored; every problem is listed. No value is echoed. */
public final class LocationValidationException extends RuntimeException {

    private final List<PreferenceFieldViolation> violations;

    public LocationValidationException(List<PreferenceFieldViolation> violations) {
        super("Location change rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<PreferenceFieldViolation> violations() {
        return violations;
    }
}
```

### Step P18 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/LocationRevisionConflictException.java`

```java
package com.capsuleai.personalcontext.application;

/** The location changed (or was first selected) after the user read it (AC-06). Nothing changed. */
public final class LocationRevisionConflictException extends RuntimeException {

    public LocationRevisionConflictException() {
        super("The location changed after it was read; the change was not applied");
    }
}
```

### Step P19 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/EnvironmentalContextService.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.LocationRepository.StoredWeatherEvidence;
import com.capsuleai.personalcontext.application.ports.LocationRepository.WeatherPublication;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherFreshness;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 (S4-CTX-02/04/05/06; OPEN-S4-19/21/23) — the environmental context of the owner's current
 * selection. Stored evidence of that selection is reused while it is current (at most 30 minutes old by its
 * provider evidence time, inclusive); otherwise weather is acquired on demand through the bounded provider,
 * outside any transaction. New evidence is published only if the same selection revision is still current, so
 * a late result can never become current for a changed or cleared selection. Failures are explicit
 * UNAVAILABLE outcomes; no value is invented.
 */
public final class EnvironmentalContextService {

    private final LocationRepository locationRepository;
    private final WeatherProvider weatherProvider;
    private final PersonalContextTransactions personalContextTransactions;
    private final Clock clock;

    public EnvironmentalContextService(LocationRepository locationRepository, WeatherProvider weatherProvider,
                                       PersonalContextTransactions personalContextTransactions, Clock clock) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
        this.weatherProvider = Objects.requireNonNull(weatherProvider, "weatherProvider");
        this.personalContextTransactions =
                Objects.requireNonNull(personalContextTransactions, "personalContextTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Never call inside a transaction: it may wait up to the acquisition deadline for the provider. */
    public EnvironmentalContext currentFor(UUID ownerAccountId, Optional<LocationProfile> profile) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        if (profile.isEmpty() || profile.get().selection().isEmpty()) {
            return new EnvironmentalContext.NoSelection();
        }
        LocationProfile location = profile.get();
        Optional<StoredWeatherEvidence> stored = locationRepository.findEvidence(ownerAccountId);
        if (stored.isPresent() && stored.get().locationRevision() == location.revision()
                && WeatherFreshness.isCurrentAt(stored.get().reading().observedAt(), now())) {
            return new EnvironmentalContext.Current(location.revision(), stored.get().reading());
        }
        return acquire(ownerAccountId, location);
    }

    private EnvironmentalContext acquire(UUID ownerAccountId, LocationProfile location) {
        LocationSelection selection = location.selection().orElseThrow();
        WeatherAcquisition acquisition;
        try {
            acquisition = weatherProvider.acquire(new WeatherQuery(selection.latitude(), selection.longitude()));
        } catch (RuntimeException unexpected) {
            // The port promises not to throw; a defect must still never become a fabricated or failed request.
            acquisition = new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
        if (acquisition instanceof WeatherAcquisition.Unavailable unavailable) {
            return new EnvironmentalContext.Unavailable(location.revision(), unavailable.reason());
        }
        WeatherReading reading = ((WeatherAcquisition.Acquired) acquisition).reading();
        Instant now = now();
        if (WeatherFreshness.isImplausiblyFuture(reading.observedAt(), now)) {
            return new EnvironmentalContext.Unavailable(location.revision(),
                    EnvironmentUnavailableReason.INVALID_PROVIDER_DATA);
        }
        if (!WeatherFreshness.isCurrentAt(reading.observedAt(), now)) {
            return new EnvironmentalContext.Unavailable(location.revision(),
                    EnvironmentUnavailableReason.STALE_PROVIDER_DATA);
        }
        WeatherPublication publication = personalContextTransactions.inTransaction(() ->
                locationRepository.publishEvidenceIfCurrent(ownerAccountId, location.revision(), reading));
        if (publication == WeatherPublication.SELECTION_CHANGED) {
            return new EnvironmentalContext.Unavailable(location.revision(),
                    EnvironmentUnavailableReason.SELECTION_CHANGED);
        }
        return new EnvironmentalContext.Current(location.revision(), reading);
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
```

**Explanation.**

- Freshness is checked twice: once for stored evidence before reuse, and once for a newly fetched reading, so a new fetch never rejuvenates old evidence.
- Publication is a short transaction after the network call.
- A stale fetched reading is not stored at all (OPEN-S4-21).

### Step P20 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ViewLocationContextService.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.domain.LocationProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 AC-02/03 — the owner's current selection and its environmental context, refreshing weather on
 * demand when no current evidence exists (within the 2-second deadline). Selection data is never changed here.
 */
public final class ViewLocationContextService {

    private final LocationRepository locationRepository;
    private final EnvironmentalContextService environmentalContextService;

    public ViewLocationContextService(LocationRepository locationRepository,
                                      EnvironmentalContextService environmentalContextService) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
        this.environmentalContextService =
                Objects.requireNonNull(environmentalContextService, "environmentalContextService");
    }

    public LocationContextView view(UUID ownerAccountId) {
        Optional<LocationProfile> profile = locationRepository.find(Objects.requireNonNull(ownerAccountId, "ownerAccountId"));
        return new LocationContextView(profile, environmentalContextService.currentFor(ownerAccountId, profile));
    }
}
```

### Step P21 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/application/ReviseLocationService.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.LocationSource;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 — accepts one change of the owner's location selection (FR-WEATHER-001/002, BRULE-PROF-003;
 * S4-CTX-01/02, OPEN-S4-18/19).
 *
 * <p>The selection is validated and rounded to two decimals, then committed under the owner/revision guard
 * (guarded first selection; a change or clear deletes the old evidence in the same transaction). Only after the
 * commit is weather acquired, outside any transaction, so a provider failure never undoes the accepted
 * selection: the response reports the selection and an explicit UNAVAILABLE environment. Clearing and an
 * unchanged selection never call the provider for a new point. Preferences and optional context are untouched.
 */
public final class ReviseLocationService {

    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String SELECTION_FIELD = "selection";
    static final String LATITUDE_FIELD = SubmittedLocation.LATITUDE_FIELD;
    static final String LONGITUDE_FIELD = SubmittedLocation.LONGITUDE_FIELD;
    static final String LABEL_FIELD = SubmittedLocation.LABEL_FIELD;
    static final String SOURCE_FIELD = SubmittedLocation.SOURCE_FIELD;

    private final LocationRepository locationRepository;
    private final EnvironmentalContextService environmentalContextService;
    private final PersonalContextTransactions personalContextTransactions;
    private final Clock clock;

    public ReviseLocationService(LocationRepository locationRepository,
                                 EnvironmentalContextService environmentalContextService,
                                 PersonalContextTransactions personalContextTransactions, Clock clock) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
        this.environmentalContextService =
                Objects.requireNonNull(environmentalContextService, "environmentalContextService");
        this.personalContextTransactions =
                Objects.requireNonNull(personalContextTransactions, "personalContextTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws LocationValidationException       if the request or the selection is not acceptable (nothing changed)
     * @throws LocationRevisionConflictException if the location changed after the user read it (nothing changed)
     */
    public LocationContextView revise(ReviseLocationCommand command) {
        UUID ownerAccountId = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ValueChange<SubmittedLocation> change = Objects.requireNonNull(command.selection(), "selection");
        checkShape(command.expectedRevision(), change);

        Optional<LocationProfile> current = locationRepository.find(ownerAccountId);
        if (!preconditionHolds(command.expectedRevision(), current)) {
            throw new LocationRevisionConflictException();
        }
        Optional<LocationSelection> currentSelection = current.flatMap(LocationProfile::selection);
        Optional<LocationSelection> resulting = resulting(currentSelection, change);
        if (resulting.equals(currentSelection)) {
            return new LocationContextView(current, environmentalContextService.currentFor(ownerAccountId, current));
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        LocationProfile saved;
        if (current.isEmpty()) {
            LocationProfile created = LocationProfile.firstSelection(ownerAccountId, resulting.orElseThrow(), now);
            if (!personalContextTransactions.inTransaction(() -> locationRepository.createIfAbsent(created))) {
                throw new LocationRevisionConflictException();
            }
            saved = created;
        } else {
            LocationProfile existing = current.get();
            LocationProfile revised = existing.revisedTo(resulting, now);
            if (!personalContextTransactions.inTransaction(() -> locationRepository.reviseIfCurrentAt(
                    ownerAccountId, existing.revision(), resulting, revised.updatedAt()))) {
                throw new LocationRevisionConflictException();
            }
            saved = revised;
        }
        Optional<LocationProfile> savedProfile = Optional.of(saved);
        return new LocationContextView(savedProfile, environmentalContextService.currentFor(ownerAccountId, savedProfile));
    }

    private static void checkShape(ExpectedRevision expectedRevision, ValueChange<SubmittedLocation> change) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        if (expectedRevision == null || expectedRevision instanceof ExpectedRevision.Missing) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.REQUIRED));
        } else if (expectedRevision instanceof ExpectedRevision.Malformed) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        }
        if (change instanceof ValueChange.Malformed<?>) {
            violations.add(new PreferenceFieldViolation(SELECTION_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new LocationValidationException(violations);
        }
    }

    private static boolean preconditionHolds(ExpectedRevision expected, Optional<LocationProfile> current) {
        if (expected instanceof ExpectedRevision.Unset) {
            return current.isEmpty();
        }
        if (expected instanceof ExpectedRevision.At at) {
            return current.isPresent() && current.get().revision() == at.revision();
        }
        throw new IllegalStateException("A missing or malformed precondition was already rejected");
    }

    private static Optional<LocationSelection> resulting(Optional<LocationSelection> current,
                                                         ValueChange<SubmittedLocation> change) {
        if (change instanceof ValueChange.Clear<?>) {
            return Optional.empty();
        }
        if (change instanceof ValueChange.Replace<SubmittedLocation> replace) {
            return Optional.of(validated(replace.value()));
        }
        return current;
    }

    /** Validates every member at once and returns the rounded selection (OPEN-S4-18). */
    private static LocationSelection validated(SubmittedLocation submitted) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        BigDecimal latitude = coordinate(submitted.latitude(), LATITUDE_FIELD, submitted, violations, true);
        BigDecimal longitude = coordinate(submitted.longitude(), LONGITUDE_FIELD, submitted, violations, false);
        Optional<LocationSource> source = Optional.empty();
        if (submitted.wrongTypeFields().contains(SOURCE_FIELD)) {
            violations.add(new PreferenceFieldViolation(SOURCE_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        } else if (submitted.source() == null) {
            violations.add(new PreferenceFieldViolation(SOURCE_FIELD, PreferenceValueProblem.REQUIRED));
        } else {
            source = LocationSource.fromCode(submitted.source());
            if (source.isEmpty()) {
                violations.add(new PreferenceFieldViolation(SOURCE_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
        }
        Optional<String> label = LocationSelection.normalizeLabel(submitted.label());
        if (submitted.wrongTypeFields().contains(LABEL_FIELD)
                || (label.isPresent() && !LocationSelection.isAcceptableLabel(label.get()))) {
            violations.add(new PreferenceFieldViolation(LABEL_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        } else if (label.isEmpty() && source.equals(Optional.of(LocationSource.MANUAL))) {
            violations.add(new PreferenceFieldViolation(LABEL_FIELD, PreferenceValueProblem.REQUIRED));
        }
        if (!violations.isEmpty()) {
            throw new LocationValidationException(violations);
        }
        return new LocationSelection(latitude, longitude, label, source.orElseThrow());
    }

    private static BigDecimal coordinate(BigDecimal submitted, String field, SubmittedLocation location,
                                         List<PreferenceFieldViolation> violations, boolean isLatitude) {
        if (location.wrongTypeFields().contains(field)) {
            violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            return null;
        }
        if (submitted == null) {
            violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.REQUIRED));
            return null;
        }
        BigDecimal rounded = LocationSelection.round(submitted);
        boolean inRange = isLatitude ? LocationSelection.isLatitude(rounded) : LocationSelection.isLongitude(rounded);
        if (!inRange) {
            violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            return null;
        }
        return rounded;
    }
}
```

**Explanation.**

- An unchanged selection (same rounded point, label and source) writes nothing and keeps the revision. Its environment comes from the normal reuse-or-refresh rule.
- Clearing deletes the evidence in the revision transaction. The view then reports `NO_SELECTION` without any provider call.
- The `PreferenceValueProblem` codes are reused: an out-of-range coordinate is `UNSUPPORTED_VALUE`; a label that is too long or contains control characters is `INVALID_FORMAT`.

**Common mistakes.**

- Acquiring weather before the commit, or inside `inTransaction`.
- Treating a provider failure as a rejected change (AC-06: the accepted selection stays).

### Step P22 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/persistence/JdbcLocationRepository.java`

```java
package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.LocationSource;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link LocationRepository} (V14, schema personal_context). */
public final class JdbcLocationRepository implements LocationRepository {

    static final String SELECT_LOCATION = """
            SELECT owner_account_id, latitude, longitude, label, source, revision, created_at, updated_at
            FROM personal_context.personal_context_location
            WHERE owner_account_id = :ownerAccountId
            """;

    static final String CREATE_IF_ABSENT = """
            INSERT INTO personal_context.personal_context_location (
                owner_account_id, latitude, longitude, label, source, revision, created_at, updated_at)
            VALUES (:ownerAccountId, :latitude, :longitude, :label, :source, :revision, :createdAt, :updatedAt)
            ON CONFLICT (owner_account_id) DO NOTHING
            """;

    static final String REVISE_IF_CURRENT_AT = """
            UPDATE personal_context.personal_context_location
            SET latitude = :latitude, longitude = :longitude, label = :label, source = :source,
                revision = revision + 1, updated_at = :updatedAt
            WHERE owner_account_id = :ownerAccountId AND revision = :expectedRevision
            """;

    static final String DELETE_EVIDENCE = """
            DELETE FROM personal_context.personal_context_weather_evidence WHERE owner_account_id = :ownerAccountId
            """;

    static final String SELECT_EVIDENCE = """
            SELECT location_revision, observed_at, retrieved_at, temperature_celsius, apparent_temperature_celsius,
                   relative_humidity_percent, precipitation_millimetres, wind_speed_kilometres_per_hour,
                   weather_code, provider, basis
            FROM personal_context.personal_context_weather_evidence
            WHERE owner_account_id = :ownerAccountId
            """;

    /** Holds the selection in share mode: a change or clear waits until this publication commits, and vice versa. */
    static final String LOCK_CURRENT_SELECTION = """
            SELECT revision FROM personal_context.personal_context_location
            WHERE owner_account_id = :ownerAccountId AND revision = :locationRevision AND latitude IS NOT NULL
            FOR SHARE
            """;

    /** Latest evidence only; never replaced by evidence of an older revision or an older observation. */
    static final String UPSERT_EVIDENCE = """
            INSERT INTO personal_context.personal_context_weather_evidence AS stored (
                owner_account_id, location_revision, observed_at, retrieved_at, temperature_celsius,
                apparent_temperature_celsius, relative_humidity_percent, precipitation_millimetres,
                wind_speed_kilometres_per_hour, weather_code, provider, basis)
            VALUES (:ownerAccountId, :locationRevision, :observedAt, :retrievedAt, :temperatureCelsius,
                    :apparentTemperatureCelsius, :relativeHumidityPercent, :precipitationMillimetres,
                    :windSpeedKilometresPerHour, :weatherCode, :provider, :basis)
            ON CONFLICT (owner_account_id) DO UPDATE SET
                location_revision = EXCLUDED.location_revision, observed_at = EXCLUDED.observed_at,
                retrieved_at = EXCLUDED.retrieved_at, temperature_celsius = EXCLUDED.temperature_celsius,
                apparent_temperature_celsius = EXCLUDED.apparent_temperature_celsius,
                relative_humidity_percent = EXCLUDED.relative_humidity_percent,
                precipitation_millimetres = EXCLUDED.precipitation_millimetres,
                wind_speed_kilometres_per_hour = EXCLUDED.wind_speed_kilometres_per_hour,
                weather_code = EXCLUDED.weather_code, provider = EXCLUDED.provider, basis = EXCLUDED.basis
            WHERE stored.location_revision < EXCLUDED.location_revision
               OR (stored.location_revision = EXCLUDED.location_revision AND stored.observed_at < EXCLUDED.observed_at)
            """;

    private final JdbcClient jdbcClient;

    public JdbcLocationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<LocationProfile> find(UUID ownerAccountId) {
        return jdbcClient.sql(SELECT_LOCATION)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> mapProfile(row))
                .optional();
    }

    @Override
    public boolean createIfAbsent(LocationProfile profile) {
        return bindSelection(jdbcClient.sql(CREATE_IF_ABSENT), profile.selection())
                .param("ownerAccountId", profile.ownerAccountId())
                .param("revision", profile.revision())
                .param("createdAt", toDatabase(profile.createdAt()))
                .param("updatedAt", toDatabase(profile.updatedAt()))
                .update() == 1;
    }

    @Override
    public boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, Optional<LocationSelection> selection,
                                     Instant updatedAt) {
        int updated = bindSelection(jdbcClient.sql(REVISE_IF_CURRENT_AT), selection)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .param("updatedAt", toDatabase(updatedAt))
                .update();
        if (updated == 0) {
            return false;
        }
        // The UPDATE holds the location row lock until commit; evidence of the old selection goes with it.
        jdbcClient.sql(DELETE_EVIDENCE).param("ownerAccountId", ownerAccountId).update();
        return true;
    }

    @Override
    public Optional<StoredWeatherEvidence> findEvidence(UUID ownerAccountId) {
        return jdbcClient.sql(SELECT_EVIDENCE)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> new StoredWeatherEvidence(row.getLong("location_revision"), mapReading(row)))
                .optional();
    }

    @Override
    public WeatherPublication publishEvidenceIfCurrent(UUID ownerAccountId, long locationRevision,
                                                       WeatherReading reading) {
        Optional<Long> lockedRevision = jdbcClient.sql(LOCK_CURRENT_SELECTION)
                .param("ownerAccountId", ownerAccountId)
                .param("locationRevision", locationRevision)
                .query(Long.class)
                .optional();
        if (lockedRevision.isEmpty()) {
            return WeatherPublication.SELECTION_CHANGED;
        }
        int written = jdbcClient.sql(UPSERT_EVIDENCE)
                .param("ownerAccountId", ownerAccountId)
                .param("locationRevision", locationRevision)
                .param("observedAt", toDatabase(reading.observedAt()))
                .param("retrievedAt", toDatabase(reading.retrievedAt()))
                .param("temperatureCelsius", reading.temperatureCelsius())
                .param("apparentTemperatureCelsius", reading.apparentTemperatureCelsius().orElse(null), Types.NUMERIC)
                .param("relativeHumidityPercent", reading.relativeHumidityPercent().orElse(null), Types.SMALLINT)
                .param("precipitationMillimetres", reading.precipitationMillimetres().orElse(null), Types.NUMERIC)
                .param("windSpeedKilometresPerHour", reading.windSpeedKilometresPerHour().orElse(null), Types.NUMERIC)
                .param("weatherCode", (short) reading.weatherCode())
                .param("provider", reading.provider())
                .param("basis", reading.basis())
                .update();
        return written == 1 ? WeatherPublication.PUBLISHED : WeatherPublication.KEPT_NEWER;
    }

    private static JdbcClient.StatementSpec bindSelection(JdbcClient.StatementSpec statement,
                                                          Optional<LocationSelection> selection) {
        return statement
                .param("latitude", selection.map(LocationSelection::latitude).orElse(null), Types.NUMERIC)
                .param("longitude", selection.map(LocationSelection::longitude).orElse(null), Types.NUMERIC)
                .param("label", selection.flatMap(LocationSelection::label).orElse(null), Types.VARCHAR)
                .param("source", selection.map(chosen -> chosen.source().name()).orElse(null), Types.VARCHAR);
    }

    private static LocationProfile mapProfile(ResultSet row) throws SQLException {
        BigDecimal latitude = row.getBigDecimal("latitude");
        Optional<LocationSelection> selection = latitude == null
                ? Optional.empty()
                : Optional.of(new LocationSelection(latitude, row.getBigDecimal("longitude"),
                        Optional.ofNullable(row.getString("label")), LocationSource.valueOf(row.getString("source"))));
        return new LocationProfile(row.getObject("owner_account_id", UUID.class), selection, row.getLong("revision"),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static WeatherReading mapReading(ResultSet row) throws SQLException {
        int weatherCode = row.getInt("weather_code");
        return new WeatherReading(
                row.getObject("observed_at", OffsetDateTime.class).toInstant(),
                row.getObject("retrieved_at", OffsetDateTime.class).toInstant(),
                row.getBigDecimal("temperature_celsius"),
                Optional.ofNullable(row.getBigDecimal("apparent_temperature_celsius")),
                Optional.ofNullable(row.getObject("relative_humidity_percent", Integer.class)),
                Optional.ofNullable(row.getBigDecimal("precipitation_millimetres")),
                Optional.ofNullable(row.getBigDecimal("wind_speed_kilometres_per_hour")),
                weatherCode,
                WeatherCondition.fromWmoCode(weatherCode)
                        .orElseThrow(() -> new IllegalStateException("Stored weather code is unknown")),
                row.getString("provider"),
                row.getString("basis"));
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
```

**Explanation.**

- `INSERT … AS stored` names the existing row in the conflict predicate (PostgreSQL 9.5+).
- `update()` returns 0 when the predicate refused the replacement. That is reported as `KEPT_NEWER`.
- The `FOR SHARE` read and the upsert run in the caller's short transaction (P19).

**Common mistake.** Publishing in the same transaction as the provider call. The provider wait would hold the location row lock.

### Step P23 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/weather/OpenMeteoWeatherProvider.java`

```java
package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * US-CTX-001 (S4-CTX-03/05; OPEN-S4-20/22/24) — Open-Meteo current weather behind the narrow
 * {@link WeatherProvider} port, under one elapsed acquisition deadline.
 *
 * <p>The deadline starts on entry and covers waiting for capacity, connection and TLS, headers, the body
 * (read through a size limit) and translation. The synchronous Spring {@link RestClient} call runs on a bounded
 * executor; the caller waits only for the remaining time, then cancels and reports DEADLINE_EXCEEDED. A capacity
 * permit is released only when the call really ends, so stuck calls never multiply threads or queues. Connect
 * and read timeouts equal to the deadline are backstops, not the deadline. There is no retry.
 *
 * <p>Privacy (AC-07): the request carries only the rounded point and fixed parameters. No URL, query or
 * coordinate is logged. Open-Meteo terms, attribution and eligibility for the deployment are not verified here.
 */
public final class OpenMeteoWeatherProvider implements WeatherProvider, AutoCloseable {

    static final String FORECAST_PATH = "/v1/forecast";
    static final String CURRENT_VARIABLES =
            "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m";

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherProvider.class);

    private final HttpClient httpClient;
    private final RestClient restClient;
    private final OpenMeteoResponseTranslator responseTranslator = new OpenMeteoResponseTranslator();
    private final Duration acquisitionDeadline;
    private final Semaphore acquisitionCapacity;
    private final ExecutorService acquisitionExecutor;
    private final int maximumResponseBytes;
    private final Clock clock;

    public OpenMeteoWeatherProvider(URI baseUrl, Duration acquisitionDeadline, int maximumConcurrentRequests,
                                    int maximumResponseBytes, Clock clock) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        this.acquisitionDeadline = Objects.requireNonNull(acquisitionDeadline, "acquisitionDeadline");
        if (acquisitionDeadline.isNegative() || acquisitionDeadline.isZero() || maximumConcurrentRequests < 1
                || maximumResponseBytes < 1) {
            throw new IllegalArgumentException("Weather acquisition limits must be positive");
        }
        this.maximumResponseBytes = maximumResponseBytes;
        this.clock = Objects.requireNonNull(clock, "clock");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(acquisitionDeadline)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(acquisitionDeadline);
        this.restClient = RestClient.builder().requestFactory(requestFactory).baseUrl(baseUrl.toString()).build();
        this.acquisitionCapacity = new Semaphore(maximumConcurrentRequests);
        this.acquisitionExecutor = Executors.newFixedThreadPool(maximumConcurrentRequests, task -> {
            Thread thread = new Thread(task, "weather-acquisition");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public WeatherAcquisition acquire(WeatherQuery query) {
        long deadline = System.nanoTime() + acquisitionDeadline.toNanos();
        WeatherAcquisition outcome = acquireBefore(query, deadline);
        if (outcome instanceof WeatherAcquisition.Unavailable unavailable) {
            log.info("Weather acquisition unavailable ({})", unavailable.reason());
        }
        return outcome;
    }

    private WeatherAcquisition acquireBefore(WeatherQuery query, long deadline) {
        try {
            if (!acquisitionCapacity.tryAcquire(remainingNanos(deadline), TimeUnit.NANOSECONDS)) {
                return unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
        // Exactly one side releases the permit: the task if it started, otherwise the caller that abandoned it.
        AtomicBoolean claimed = new AtomicBoolean();
        Future<WeatherAcquisition> call;
        try {
            call = acquisitionExecutor.submit(() -> {
                if (!claimed.compareAndSet(false, true)) {
                    return unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED);
                }
                try {
                    return fetch(query);
                } finally {
                    acquisitionCapacity.release();
                }
            });
        } catch (RejectedExecutionException rejected) {
            acquisitionCapacity.release();
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
        try {
            return call.get(remainingNanos(deadline), TimeUnit.NANOSECONDS);
        } catch (TimeoutException late) {
            abandon(call, claimed);
            return unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED);
        } catch (ExecutionException failed) {
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        } catch (InterruptedException interrupted) {
            abandon(call, claimed);
            Thread.currentThread().interrupt();
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
    }

    private void abandon(Future<WeatherAcquisition> call, AtomicBoolean claimed) {
        if (claimed.compareAndSet(false, true)) {
            acquisitionCapacity.release();
        }
        call.cancel(true);
    }

    private WeatherAcquisition fetch(WeatherQuery query) {
        try {
            WeatherAcquisition acquisition = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path(FORECAST_PATH)
                            .queryParam("latitude", query.latitude().toPlainString())
                            .queryParam("longitude", query.longitude().toPlainString())
                            .queryParam("current", CURRENT_VARIABLES)
                            .queryParam("timezone", "GMT")
                            .queryParam("timeformat", "unixtime")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
                        }
                        try (InputStream body = response.getBody()) {
                            byte[] content = body.readNBytes(maximumResponseBytes + 1);
                            if (content.length > maximumResponseBytes) {
                                return unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA);
                            }
                            return responseTranslator.translate(content, query, now());
                        }
                    });
            return acquisition != null ? acquisition : unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        } catch (RestClientException | IllegalArgumentException failure) {
            return unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
    }

    /**
     * Stops the acquisition threads and the HTTP client (its selector thread and any open exchange) when the
     * application context closes.
     */
    @Override
    public void close() {
        acquisitionExecutor.shutdownNow();
        httpClient.shutdownNow();
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }

    private static long remainingNanos(long deadline) {
        return Math.max(0, deadline - System.nanoTime());
    }

    private static WeatherAcquisition unavailable(EnvironmentUnavailableReason reason) {
        return new WeatherAcquisition.Unavailable(reason);
    }
}
```

**Explanation.**

- **Deadline.** `System.nanoTime` measures the elapsed deadline, so it is immune to wall-clock changes. The application `Clock` stamps only `retrievedAt`.
- **Interruption.** An interrupted JDK `HttpClient.send` aborts the exchange in current JDKs. The design relies only on the caller returning on time and on bounded threads; prompt connection release after `cancel(true)` must be **measured** (OPEN-S4-22, §14 M8).
- **Redirects.** `HttpClient.Redirect.NEVER` keeps the request at the configured host.
- **Body limit.** `readNBytes(limit + 1)` reads at most one byte past the limit.
- **Close (review of 2026-11-23).** `close()` also shuts down the JDK `HttpClient`. Without it, the client's selector and worker threads outlived the bean: three threads remained in T4 `creatingOpensNoConnectionAndClosingStopsTheAdaptersThreads`.

**Common mistakes.**

- Setting only connect and read timeouts and calling it the deadline. Queue wait and body time escape them.
- Releasing the permit in the caller's `finally` while the task is still running. That lets stuck calls pile up.

### Step P24 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/weather/OpenMeteoResponseTranslator.java`

```java
package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * US-CTX-001 (S4-CTX-03, OPEN-S4-20) — translates an Open-Meteo forecast response (current block, unixtime,
 * default metric units) into {@link WeatherReading}, or reports it as INVALID_PROVIDER_DATA. Nothing is guessed:
 * a missing required value, an unexpected unit, an unknown weather code, an implausible value or a grid point
 * far from the requested location makes the whole response unusable. The raw payload is discarded.
 */
final class OpenMeteoResponseTranslator {

    /** Open-Meteo snaps to model grid cells; a cell further away than this is not the requested place. */
    static final BigDecimal MAXIMUM_GRID_DISTANCE_DEGREES = new BigDecimal("0.5");

    private static final List<String> NUMERIC_FIELDS = List.of("time", "temperature_2m", "apparent_temperature",
            "relative_humidity_2m", "precipitation", "weather_code", "wind_speed_10m");
    private static final BigDecimal LOWEST_TEMPERATURE = new BigDecimal("-100");
    private static final BigDecimal HIGHEST_TEMPERATURE = new BigDecimal("70");
    private static final BigDecimal HIGHEST_PRECIPITATION = new BigDecimal("1000");
    private static final BigDecimal HIGHEST_WIND_SPEED = new BigDecimal("500");

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    WeatherAcquisition translate(byte[] body, WeatherQuery query, Instant retrievedAt) {
        try {
            return translateOrReject(body, query, retrievedAt);
        } catch (JacksonException | ArithmeticException | DateTimeException | IllegalArgumentException unusable) {
            // Malformed JSON, or a number that cannot be represented (an int or long out of range, a value that
            // overflows a double, a time beyond the representable instants): unusable data, never a provider
            // failure and never an exception escaping the adapter.
            return invalid();
        }
    }

    private WeatherAcquisition translateOrReject(byte[] body, WeatherQuery query, Instant retrievedAt) {
        JsonNode root = jsonMapper.readTree(body);
        if (root == null || !root.isObject()) {
            return invalid();
        }
        JsonNode current = root.path("current");
        JsonNode units = root.path("current_units");
        if (!current.isObject() || !units.isObject()
                || !near(root.path("latitude"), query.latitude()) || !near(root.path("longitude"), query.longitude())) {
            return invalid();
        }
        for (String field : NUMERIC_FIELDS) {
            JsonNode value = current.path(field);
            if (!value.isMissingNode() && !value.isNull() && !value.isNumber()) {
                return invalid();
            }
        }
        if (!hasUnit(units, current, "temperature_2m", "°C") || !hasUnit(units, current, "apparent_temperature", "°C")
                || !hasUnit(units, current, "relative_humidity_2m", "%")
                || !hasUnit(units, current, "precipitation", "mm")
                || !hasUnit(units, current, "wind_speed_10m", "km/h")) {
            return invalid();
        }
        JsonNode time = current.path("time");
        JsonNode code = current.path("weather_code");
        Optional<BigDecimal> temperature = decimal(current.path("temperature_2m"));
        if (!time.isIntegralNumber() || !code.isIntegralNumber() || temperature.isEmpty()) {
            return invalid();
        }
        Optional<WeatherCondition> condition = WeatherCondition.fromWmoCode(code.intValue());
        Optional<BigDecimal> apparentTemperature = decimal(current.path("apparent_temperature"));
        Optional<Integer> humidity = integer(current.path("relative_humidity_2m"));
        Optional<BigDecimal> precipitation = decimal(current.path("precipitation"));
        Optional<BigDecimal> windSpeed = decimal(current.path("wind_speed_10m"));
        if (condition.isEmpty()
                || !within(temperature.get(), LOWEST_TEMPERATURE, HIGHEST_TEMPERATURE)
                || apparentTemperature.filter(value -> !within(value, LOWEST_TEMPERATURE, HIGHEST_TEMPERATURE)).isPresent()
                || humidity.filter(value -> value < 0 || value > 100).isPresent()
                || precipitation.filter(value -> !within(value, BigDecimal.ZERO, HIGHEST_PRECIPITATION)).isPresent()
                || windSpeed.filter(value -> !within(value, BigDecimal.ZERO, HIGHEST_WIND_SPEED)).isPresent()) {
            return invalid();
        }
        return new WeatherAcquisition.Acquired(new WeatherReading(Instant.ofEpochSecond(time.longValue()), retrievedAt,
                temperature.get(), apparentTemperature, humidity, precipitation, windSpeed, code.intValue(),
                condition.get(), WeatherReading.OPEN_METEO, WeatherReading.MODELLED));
    }

    /** A value present in {@code current} must declare the expected unit; an absent optional value needs none. */
    private static boolean hasUnit(JsonNode units, JsonNode current, String field, String expectedUnit) {
        JsonNode value = current.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return true;
        }
        JsonNode unit = units.path(field);
        return unit.isString() && unit.stringValue().equals(expectedUnit);
    }

    private static boolean near(JsonNode reported, BigDecimal requested) {
        return reported.isNumber()
                && reported.decimalValue().subtract(requested).abs().compareTo(MAXIMUM_GRID_DISTANCE_DEGREES) <= 0;
    }

    private static Optional<BigDecimal> decimal(JsonNode node) {
        return node.isNumber() ? Optional.of(node.decimalValue().setScale(2, RoundingMode.HALF_EVEN)) : Optional.empty();
    }

    private static Optional<Integer> integer(JsonNode node) {
        if (!node.isNumber()) {
            return Optional.empty();
        }
        return Optional.of(node.decimalValue().setScale(0, RoundingMode.HALF_EVEN).intValueExact());
    }

    private static boolean within(BigDecimal value, BigDecimal lowest, BigDecimal highest) {
        return value.compareTo(lowest) >= 0 && value.compareTo(highest) <= 0;
    }

    private static WeatherAcquisition invalid() {
        return new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA);
    }
}
```

**Explanation.**

- The plausibility bounds keep impossible values out of the evidence and inside the `NUMERIC` column precision.
- **Unrepresentable numbers (review of 2026-11-23).** Jackson 3 does not truncate: `intValue()`, `longValue()` and `decimalValue()` throw `JsonNodeException` for a value out of range or not finite. Examples: a weather code of 4294967298, a time beyond `long`, or `1e400`. Similar values make `intValueExact` and `Instant.ofEpochSecond` throw, for example humidity `1e10`. These exceptions used to escape the translator, and the adapter reported them as `PROVIDER_UNAVAILABLE`. `translate` now reports them as `INVALID_PROVIDER_DATA` (T3 `unrepresentableNumbersAreInvalidProviderData`).
- Open-Meteo labels its unit `°C` with a degree sign. The Java source is UTF-8, as Maven is configured for the project.
- Jackson 3's `JacksonException` is unchecked.

### Step P25 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/LocationRevisionRequest.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ReviseLocationCommand;
import com.capsuleai.personalcontext.application.SubmittedLocation;
import com.capsuleai.personalcontext.application.ValueChange;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of PATCH /api/v1/personal-context/location (US-CTX-001). {@code selection} omitted keeps the
 * selection, null clears it, an object replaces it. Raw {@link JsonNode} components keep omitted, null and
 * values apart; members of the wrong JSON kind are reported, never coerced. There is no owner field.
 */
public record LocationRevisionRequest(JsonNode expectedRevision, JsonNode selection) {

    ReviseLocationCommand toCommand(UUID ownerAccountId) {
        return new ReviseLocationCommand(ownerAccountId, PreferencesRevisionRequest.expectedRevision(expectedRevision),
                selection(selection));
    }

    static ValueChange<SubmittedLocation> selection(JsonNode node) {
        if (node == null) {
            return ValueChange.keep();
        }
        if (node.isNull()) {
            return ValueChange.clear();
        }
        if (!node.isObject()) {
            return ValueChange.malformed();
        }
        List<String> wrongTypeFields = new ArrayList<>();
        BigDecimal latitude = number(node.get("latitude"), SubmittedLocation.LATITUDE_FIELD, wrongTypeFields);
        BigDecimal longitude = number(node.get("longitude"), SubmittedLocation.LONGITUDE_FIELD, wrongTypeFields);
        String label = text(node.get("label"), SubmittedLocation.LABEL_FIELD, wrongTypeFields);
        String source = text(node.get("source"), SubmittedLocation.SOURCE_FIELD, wrongTypeFields);
        return ValueChange.replace(new SubmittedLocation(latitude, longitude, label, source, wrongTypeFields));
    }

    private static BigDecimal number(JsonNode value, String field, List<String> wrongTypeFields) {
        if (value == null || value.isNull()) {
            return null;
        }
        // A JSON number too large for a double (for example 1e400) is read as infinity and has no decimal value:
        // it is reported like a value of the wrong kind (UNSUPPORTED_VALUE), never left to fail as a server error.
        if (!value.isNumber() || (value.isFloatingPointNumber() && !Double.isFinite(value.doubleValue()))) {
            wrongTypeFields.add(field);
            return null;
        }
        return value.decimalValue();
    }

    private static String text(JsonNode value, String field, List<String> wrongTypeFields) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isString()) {
            wrongTypeFields.add(field);
            return null;
        }
        return value.stringValue();
    }
}
```

**Explanation (review of 2026-11-23).**

- Jackson 3 reads a JSON number too large for a double, such as `1e400`, as infinity, and its `decimalValue()` then throws `JsonNodeException`.
- The first version let that exception escape the controller, so the client got a server error instead of a field error.
- A non-finite number is now reported like a value of the wrong kind: `UNSUPPORTED_VALUE`, which the contract documents for "not a number, or out of range" (T12 `unusableValuesAreFieldErrorsNeverServerErrors`).
- Floating-point values are always doubles here, so their scale is bounded and rounding them is cheap.

### Step P26 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/LocationContextResponse.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.EnvironmentalContext;
import com.capsuleai.personalcontext.application.LocationContextView;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;

/**
 * US-CTX-001 AC-02 — the accepted selection and, separately, its environmental context. Every field is always
 * present: {@code revision} is null while nothing was ever selected, {@code selection} is null when none is
 * selected, and {@code environment.weather} is null unless the status is CURRENT. Weather carries both the
 * provider evidence time ({@code observedAt}) and the acquisition time ({@code retrievedAt}).
 */
public record LocationContextResponse(String revision, SelectionResponse selection, EnvironmentResponse environment) {

    public record SelectionResponse(BigDecimal latitude, BigDecimal longitude, String label, String source) {
    }

    /** {@code basisRevision}: the location revision the status applies to; null for NO_SELECTION. */
    public record EnvironmentResponse(String status, String basisRevision, String unavailableReason,
                                      WeatherResponse weather) {
    }

    public record WeatherResponse(String condition, int weatherCode, BigDecimal temperatureCelsius,
                                  BigDecimal apparentTemperatureCelsius, Integer relativeHumidityPercent,
                                  BigDecimal precipitationMillimetres, BigDecimal windSpeedKilometresPerHour,
                                  String observedAt, String retrievedAt, String basis, String provider,
                                  String attribution) {
    }

    static LocationContextResponse from(LocationContextView view, String weatherAttribution) {
        String revision = view.profile().map(profile -> Long.toString(profile.revision())).orElse(null);
        SelectionResponse selection = view.profile().flatMap(LocationProfile::selection)
                .map(LocationContextResponse::selection).orElse(null);
        return new LocationContextResponse(revision, selection, environment(view.environment(), weatherAttribution));
    }

    private static SelectionResponse selection(LocationSelection selection) {
        return new SelectionResponse(selection.latitude(), selection.longitude(), selection.label().orElse(null),
                selection.source().name());
    }

    private static EnvironmentResponse environment(EnvironmentalContext environment, String weatherAttribution) {
        return switch (environment) {
            case EnvironmentalContext.NoSelection none -> new EnvironmentResponse("NO_SELECTION", null, null, null);
            case EnvironmentalContext.Current current -> new EnvironmentResponse("CURRENT",
                    Long.toString(current.locationRevision()), null, weather(current.reading(), weatherAttribution));
            case EnvironmentalContext.Unavailable unavailable -> new EnvironmentResponse("UNAVAILABLE",
                    Long.toString(unavailable.locationRevision()), unavailable.reason().name(), null);
        };
    }

    private static WeatherResponse weather(WeatherReading reading, String weatherAttribution) {
        return new WeatherResponse(reading.condition().name(), reading.weatherCode(), reading.temperatureCelsius(),
                reading.apparentTemperatureCelsius().orElse(null), reading.relativeHumidityPercent().orElse(null),
                reading.precipitationMillimetres().orElse(null), reading.windSpeedKilometresPerHour().orElse(null),
                reading.observedAt().toString(), reading.retrievedAt().toString(), reading.basis(), reading.provider(),
                weatherAttribution);
    }
}
```

**Explanation.** The switch over the sealed `EnvironmentalContext` is exhaustive (Java 21 pattern matching), so a new state cannot be forgotten.

### Step P27 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PersonalContextLocationController.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.personalcontext.application.ReviseLocationService;
import com.capsuleai.personalcontext.application.ViewLocationContextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authenticated user's location selection and its environmental context (US-CTX-001). Always "my"
 * location: the owner is the authenticated account and no request names another (FR-AUTH-008). Protected by
 * the application's default-deny filter chain. Reads may refresh weather on demand within 2 seconds.
 */
@RestController
@RequestMapping("/api/v1/personal-context/location")
public class PersonalContextLocationController {

    private final ViewLocationContextService viewLocationContextService;
    private final ReviseLocationService reviseLocationService;
    private final String weatherAttribution;

    public PersonalContextLocationController(ViewLocationContextService viewLocationContextService,
                                             ReviseLocationService reviseLocationService,
                                             @Value("${capsuleai.personal-context.weather.attribution}")
                                             String weatherAttribution) {
        this.viewLocationContextService = viewLocationContextService;
        this.reviseLocationService = reviseLocationService;
        this.weatherAttribution = weatherAttribution;
    }

    /** AC-02/03: the selection and its current or unavailable weather; never 404 when nothing is selected. */
    @GetMapping
    public LocationContextResponse viewLocation(@AuthenticationPrincipal AuthenticatedAccount owner) {
        return LocationContextResponse.from(viewLocationContextService.view(owner.accountId()), weatherAttribution);
    }

    /** AC-01/05/06: one change or clear against the revision the user read; weather is acquired after commit. */
    @PatchMapping
    public LocationContextResponse reviseLocation(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                  @RequestBody LocationRevisionRequest request) {
        return LocationContextResponse.from(reviseLocationService.revise(request.toCommand(owner.accountId())),
                weatherAttribution);
    }
}
```

### Step P28 — [MODIFY] `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PersonalContextApiExceptionHandler.java`

BEFORE:

```java
import com.capsuleai.personalcontext.application.OptionalContextRevisionConflictException;
```

AFTER:

```java
import com.capsuleai.personalcontext.application.LocationRevisionConflictException;
import com.capsuleai.personalcontext.application.LocationValidationException;
import com.capsuleai.personalcontext.application.OptionalContextRevisionConflictException;
```

BEFORE (the optional-context conflict handler as left by US-PROF-002 P18):

```java
                "Your optional profile information changed after you opened it, so this change was not applied. "
                        + "Reload it, review it and submit again."));
    }
```

AFTER:

```java
                "Your optional profile information changed after you opened it, so this change was not applied. "
                        + "Reload it, review it and submit again."));
    }

    /** US-CTX-001: the same body shape; no submitted coordinate or label is echoed. */
    @ExceptionHandler(LocationValidationException.class)
    public ResponseEntity<ProblemDetail> handleLocationValidation(LocationValidationException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "This location cannot be used. Correct it and submit again; your saved location is unchanged.");
        problem.setProperty("fieldErrors", fieldErrors(exception.violations()));
        return respond(problem);
    }

    @ExceptionHandler(LocationRevisionConflictException.class)
    public ResponseEntity<ProblemDetail> handleLocationConflict(LocationRevisionConflictException exception) {
        return respond(problem(HttpStatus.CONFLICT, "REVISION_CONFLICT",
                "Your location changed after you opened it, so this change was not applied. Reload it, review it "
                        + "and submit again."));
    }
```

### Step P29 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/configuration/PersonalContextWeatherProperties.java`

```java
package com.capsuleai.personalcontext.configuration;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * US-CTX-001 configuration {@code capsuleai.personal-context.weather.*} (OPEN-S4-22/24/33). Validated at startup:
 * the acquisition deadline implements the approved 2-second boundary and may not exceed it; the provider is
 * reached only over HTTPS (plain HTTP is accepted for loopback hosts, which only tests use).
 */
@ConfigurationProperties("capsuleai.personal-context.weather")
public record PersonalContextWeatherProperties(
        boolean enabled,
        URI baseUrl,
        Duration acquisitionDeadline,
        int maximumConcurrentRequests,
        int maximumResponseBytes,
        String attribution) {

    /** Approved (FR-WEATHER-004, AC-03): an external weather request may delay an assessment by at most 2 seconds. */
    public static final Duration MAXIMUM_ACQUISITION_DEADLINE = Duration.ofSeconds(2);

    private static final Set<String> LOOPBACK_HOSTS = Set.of("127.0.0.1", "localhost");

    public PersonalContextWeatherProperties {
        Objects.requireNonNull(baseUrl, "capsuleai.personal-context.weather.base-url");
        Objects.requireNonNull(acquisitionDeadline, "capsuleai.personal-context.weather.acquisition-deadline");
        Objects.requireNonNull(attribution, "capsuleai.personal-context.weather.attribution");
        if (acquisitionDeadline.isNegative() || acquisitionDeadline.isZero()
                || acquisitionDeadline.compareTo(MAXIMUM_ACQUISITION_DEADLINE) > 0) {
            throw new IllegalArgumentException("The weather acquisition deadline must be positive and at most PT2S");
        }
        if (maximumConcurrentRequests < 1 || maximumResponseBytes < 1024) {
            throw new IllegalArgumentException("Weather concurrency must be at least 1 and the response limit at least 1024 bytes");
        }
        boolean secure = "https".equals(baseUrl.getScheme());
        boolean loopback = "http".equals(baseUrl.getScheme()) && LOOPBACK_HOSTS.contains(baseUrl.getHost());
        if (!secure && !loopback) {
            throw new IllegalArgumentException("The weather base URL must use https (http only for a loopback test host)");
        }
        if (baseUrl.getQuery() != null || baseUrl.getFragment() != null || baseUrl.getUserInfo() != null) {
            throw new IllegalArgumentException("The weather base URL must not carry a query, fragment or credentials");
        }
        if (attribution.isBlank()) {
            throw new IllegalArgumentException("The weather attribution must not be blank");
        }
    }

    /** Never print more than needed, even when debugging configuration. */
    @Override
    public String toString() {
        return "PersonalContextWeatherProperties[enabled=" + enabled + ", host=" + baseUrl.getHost()
                + ", acquisitionDeadline=" + acquisitionDeadline + ", maximumConcurrentRequests="
                + maximumConcurrentRequests + ", maximumResponseBytes=" + maximumResponseBytes + "]";
    }
}
```

### Step P30 — [ADD] `backend/src/main/java/com/capsuleai/personalcontext/configuration/PersonalContextLocationConfiguration.java`

```java
package com.capsuleai.personalcontext.configuration;

import com.capsuleai.personalcontext.adapter.out.persistence.JdbcLocationRepository;
import com.capsuleai.personalcontext.adapter.out.weather.OpenMeteoWeatherProvider;
import com.capsuleai.personalcontext.application.EnvironmentalContextService;
import com.capsuleai.personalcontext.application.ReviseLocationService;
import com.capsuleai.personalcontext.application.ViewLocationContextService;
import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Wires US-CTX-001: location persistence, the bounded weather adapter (or a NOT_CONFIGURED provider when
 * disabled) and the location use cases. Reuses the application's single Clock and the Personal Context
 * transaction port. Creating the adapter opens no connection.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PersonalContextWeatherProperties.class)
public class PersonalContextLocationConfiguration {

    @Bean
    LocationRepository locationRepository(JdbcClient jdbcClient) {
        return new JdbcLocationRepository(jdbcClient);
    }

    /** The application context closes the adapter (AutoCloseable), which stops its acquisition threads. */
    @Bean
    WeatherProvider weatherProvider(PersonalContextWeatherProperties properties, Clock clock) {
        if (!properties.enabled()) {
            return query -> new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.NOT_CONFIGURED);
        }
        return new OpenMeteoWeatherProvider(properties.baseUrl(), properties.acquisitionDeadline(),
                properties.maximumConcurrentRequests(), properties.maximumResponseBytes(), clock);
    }

    @Bean
    EnvironmentalContextService environmentalContextService(LocationRepository locationRepository,
                                                            WeatherProvider weatherProvider,
                                                            PersonalContextTransactions personalContextTransactions,
                                                            Clock clock) {
        return new EnvironmentalContextService(locationRepository, weatherProvider, personalContextTransactions, clock);
    }

    @Bean
    ViewLocationContextService viewLocationContextService(LocationRepository locationRepository,
                                                          EnvironmentalContextService environmentalContextService) {
        return new ViewLocationContextService(locationRepository, environmentalContextService);
    }

    @Bean
    ReviseLocationService reviseLocationService(LocationRepository locationRepository,
                                                EnvironmentalContextService environmentalContextService,
                                                PersonalContextTransactions personalContextTransactions, Clock clock) {
        return new ReviseLocationService(locationRepository, environmentalContextService, personalContextTransactions,
                clock);
    }
}
```

### Step P31 — [MODIFY] `backend/src/main/resources/application.properties`

BEFORE (the last line of the file, as left by US-GAR-005 P21):

```properties
capsuleai.wardrobe.removal-cleanup.overdue-warning-age=P21D
```

AFTER:

```properties
capsuleai.wardrobe.removal-cleanup.overdue-warning-age=P21D

# US-CTX-001 weather (S4-CTX-03/05; OPEN-S4-20/22/24/33 delegated defaults). Open-Meteo terms of use,
# attribution and eligibility for this deployment are NOT verified: confirm them, or set enabled=false (the API
# then reports NOT_CONFIGURED). The acquisition deadline implements the approved 2-second limit; startup fails
# above PT2S. The base URL must be https. No API key is used or stored. CAPSULE_WEATHER_ENABLED=false switches the
# provider off without a code change; the local profile defaults to off (application-local.properties).
capsuleai.personal-context.weather.enabled=${CAPSULE_WEATHER_ENABLED:true}
capsuleai.personal-context.weather.base-url=https://api.open-meteo.com
capsuleai.personal-context.weather.acquisition-deadline=PT2S
capsuleai.personal-context.weather.maximum-concurrent-requests=8
capsuleai.personal-context.weather.maximum-response-bytes=65536
capsuleai.personal-context.weather.attribution=Weather data by Open-Meteo.com
```

**Explanation (review of 2026-11-23).**

- The default stays `true`, as selected under OPEN-S4-33. Because the provider terms are unreviewed, two environment controls were added.
- `CAPSULE_WEATHER_ENABLED=false` turns the provider off in any deployment without a code change.
- P34 makes the `local` profile default to `false`, so a developer run never contacts Open-Meteo unless someone enables it deliberately.

**Common mistake.** Pointing `base-url` at a host with a path. The adapter appends `/v1/forecast`, so give only the origin.

### Step P32 — [MODIFY] `docs/05-api/openapi.yaml` (0.15.0 → 0.16.0)

**Edit 1.** Version, coverage, scope and tag.

BEFORE:

```yaml
  version: 0.15.0
```

AFTER:

```yaml
  version: 0.16.0
```

BEFORE:

```yaml
      - US-PROF-002: Control optional body and gender context
```

AFTER:

```yaml
      - US-PROF-002: Control optional body and gender context
      - US-CTX-001: Choose environmental context with safe fallback
```

BEFORE:

```yaml
    This specification does not introduce endpoints for
    session listing, all-device logout, replacing or removing a
    garment's photo, location, or public profiles.
```

AFTER:

```yaml
    This specification does not introduce endpoints for
    session listing, all-device logout, replacing or removing a
    garment's photo, or public profiles.

    Location context (US-CTX-001) is the account's one current
    selection, rounded to two decimals, and the weather acquired
    for it from Open-Meteo within a single 2-second deadline.
    Weather is current when its evidence is at most 30 minutes
    old; otherwise the environment is reported as UNAVAILABLE
    with a reason. A selected city is never presented as weather.
```

BEFORE:

```yaml
    description: The authenticated user's own declared preferences and optional context
```

AFTER:

```yaml
    description: The authenticated user's own declared preferences, optional context and location
```

**Edit 2.** The new path, before `components` (after the optional-context operations).

BEFORE:

```yaml
        "409":
          $ref: "#/components/responses/RevisionConflict"
        "503":
          $ref: "#/components/responses/Unavailable"

components:
```

AFTER:

```yaml
        "409":
          $ref: "#/components/responses/RevisionConflict"
        "503":
          $ref: "#/components/responses/Unavailable"

  /api/v1/personal-context/location:
    get:
      tags:
        - PersonalContext
      operationId: viewLocation
      summary: The account's location selection and its environmental context
      description: |
        US-CTX-001 AC-02/03 (FR-WEATHER-001–005; approved S4-CTX-01–06).
        Always 200. When no current weather evidence exists for the
        selection, weather is acquired on demand within 2 seconds;
        otherwise the stored evidence is reused while it is at most
        30 minutes old. environment.status is NO_SELECTION, CURRENT or
        UNAVAILABLE (with unavailableReason); the selection is reported
        separately and is never itself weather.
      responses:
        "200":
          description: The selection and its environmental context.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/LocationContext"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "503":
          $ref: "#/components/responses/Unavailable"
    patch:
      tags:
        - PersonalContext
      operationId: reviseLocation
      summary: Select, change or clear the account's location
      description: |
        US-CTX-001 (OPEN-S4-18/19). selection omitted keeps it; null
        clears it (no provider request); an object replaces it. The
        server rounds latitude and longitude to two decimals before
        storing or sending them. source is DEVICE or MANUAL; a MANUAL
        selection needs a label (1–80 characters). expectedRevision is
        required: null for the first selection, otherwise the revision
        last read; a mismatch is 409 and changes nothing.

        The selection is committed first; weather is acquired after
        the commit. A weather failure never undoes the accepted
        selection: the response reports it with an UNAVAILABLE
        environment.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/LocationRevisionRequest"
            examples:
              manualCity:
                summary: First selection of a city resolved on the device
                value:
                  expectedRevision: null
                  selection:
                    latitude: 10.7769
                    longitude: 106.7009
                    label: "Hồ Chí Minh"
                    source: "MANUAL"
              clear:
                summary: Clear the selection
                value:
                  expectedRevision: "2"
                  selection: null
      responses:
        "200":
          description: The change was accepted (or changed nothing); the selection and its environmental context.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/LocationContext"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "409":
          $ref: "#/components/responses/RevisionConflict"
        "503":
          $ref: "#/components/responses/Unavailable"

components:
```

**Edit 3.** Schemas, before `FieldError`.

BEFORE:

```yaml
    FieldError:
      type: object
      description: A validation failure associated with a request field.
```

AFTER:

```yaml
    LocationRevisionRequest:
      type: object
      description: A change of the account's location selection (US-CTX-001). There is no owner field.
      required:
        - expectedRevision
      properties:
        expectedRevision:
          type: [string, "null"]
          pattern: "^[1-9][0-9]{0,17}$"
          description: null for the first selection; otherwise the revision last read (opaque).
        selection:
          anyOf:
            - $ref: "#/components/schemas/LocationSelectionInput"
            - type: "null"

    LocationSelectionInput:
      type: object
      required:
        - latitude
        - longitude
        - source
      properties:
        latitude:
          type: number
          minimum: -90
          maximum: 90
          description: Rounded by the server to two decimals before storage or use.
        longitude:
          type: number
          minimum: -180
          maximum: 180
          description: Rounded by the server to two decimals before storage or use.
        label:
          type: [string, "null"]
          maxLength: 80
          description: Required for MANUAL (the city resolved on the device); optional for DEVICE.
        source:
          type: string
          enum: [DEVICE, MANUAL]

    LocationContext:
      type: object
      description: |
        The accepted selection and, separately, its environmental
        context. Every field is always present.
      required:
        - revision
        - selection
        - environment
      properties:
        revision:
          type: [string, "null"]
          pattern: "^[1-9][0-9]*$"
          description: null while nothing was ever selected.
        selection:
          anyOf:
            - $ref: "#/components/schemas/LocationSelection"
            - type: "null"
        environment:
          $ref: "#/components/schemas/EnvironmentalContext"

    LocationSelection:
      type: object
      required:
        - latitude
        - longitude
        - label
        - source
      properties:
        latitude:
          type: number
          description: Two decimals (about 1.1 km).
        longitude:
          type: number
          description: Two decimals (about 1.1 km).
        label:
          type: [string, "null"]
        source:
          type: string
          enum: [DEVICE, MANUAL]

    EnvironmentalContext:
      type: object
      required:
        - status
        - basisRevision
        - unavailableReason
        - weather
      properties:
        status:
          type: string
          enum: [NO_SELECTION, CURRENT, UNAVAILABLE]
        basisRevision:
          type: [string, "null"]
          description: The location revision this status applies to; null for NO_SELECTION.
        unavailableReason:
          type: [string, "null"]
          enum: [NOT_CONFIGURED, PROVIDER_UNAVAILABLE, DEADLINE_EXCEEDED, INVALID_PROVIDER_DATA, STALE_PROVIDER_DATA, SELECTION_CHANGED, null]
        weather:
          anyOf:
            - $ref: "#/components/schemas/Weather"
            - type: "null"

    Weather:
      type: object
      description: |
        Translated current weather. observedAt is the provider's
        evidence time (freshness is measured from it); retrievedAt is
        when the server received it. basis MODELLED means model
        output, not a station observation.
      required:
        - condition
        - weatherCode
        - temperatureCelsius
        - apparentTemperatureCelsius
        - relativeHumidityPercent
        - precipitationMillimetres
        - windSpeedKilometresPerHour
        - observedAt
        - retrievedAt
        - basis
        - provider
        - attribution
      properties:
        condition:
          type: string
          enum: [CLEAR, MAINLY_CLEAR, PARTLY_CLOUDY, OVERCAST, FOG, DRIZZLE, FREEZING_DRIZZLE, RAIN, FREEZING_RAIN, SNOW, RAIN_SHOWERS, SNOW_SHOWERS, THUNDERSTORM, THUNDERSTORM_WITH_HAIL]
        weatherCode:
          type: integer
          description: WMO weather interpretation code.
        temperatureCelsius:
          type: number
        apparentTemperatureCelsius:
          type: [number, "null"]
        relativeHumidityPercent:
          type: [integer, "null"]
        precipitationMillimetres:
          type: [number, "null"]
        windSpeedKilometresPerHour:
          type: [number, "null"]
        observedAt:
          type: string
          format: date-time
        retrievedAt:
          type: string
          format: date-time
        basis:
          type: string
          enum: [MODELLED, OBSERVED]
        provider:
          type: string
          enum: [OPEN_METEO]
        attribution:
          type: string
          description: Provider attribution to display with the weather.

    FieldError:
      type: object
      description: A validation failure associated with a request field.
```

### Step P33 — [MODIFY] `.github/workflows/backend-ci.yml`

BEFORE:

```powershell
            'failsafe-reports/TEST-com.capsuleai.personalcontext.application.ConcurrentOptionalContextIT.xml' = 3
          }
```

AFTER:

```powershell
            'failsafe-reports/TEST-com.capsuleai.personalcontext.application.ConcurrentOptionalContextIT.xml' = 3
            'surefire-reports/TEST-com.capsuleai.personalcontext.domain.LocationSelectionTest.xml' = 4
            'surefire-reports/TEST-com.capsuleai.personalcontext.domain.WeatherFreshnessTest.xml' = 4
            'surefire-reports/TEST-com.capsuleai.personalcontext.adapter.out.weather.OpenMeteoResponseTranslatorTest.xml' = 5
            'surefire-reports/TEST-com.capsuleai.personalcontext.adapter.out.weather.OpenMeteoWeatherProviderTest.xml' = 7
            'surefire-reports/TEST-com.capsuleai.personalcontext.application.ReviseLocationServiceTest.xml' = 8
            'failsafe-reports/TEST-com.capsuleai.personalcontext.adapter.out.persistence.LocationSchemaIT.xml' = 3
            'failsafe-reports/TEST-com.capsuleai.personalcontext.adapter.in.web.LocationContextApiIT.xml' = 8
            'failsafe-reports/TEST-com.capsuleai.personalcontext.application.ConcurrentLocationIT.xml' = 4
            'surefire-reports/TEST-com.capsuleai.personalcontext.configuration.PersonalContextWeatherPropertiesTest.xml' = 3
          }
```

This brings the total to 132 required entries. `OpenMeteoWeatherProviderTest` runs as a unit test (Surefire) against an in-process HTTP server and needs no Docker.

### Step P34 — [MODIFY] `backend/src/main/resources/application-local.properties`

Added by the review of 2026-11-23 (R14 deployment risk). Until the Open-Meteo terms are reviewed, a local run must not contact the provider (§14 says so for M1–M6). This step makes that the default of the `local` profile instead of a manual step. The file has no final newline; keep it that way.

BEFORE:

```properties
capsuleai.wardrobe.photo-storage.path-style=true
```

AFTER:

```properties
capsuleai.wardrobe.photo-storage.path-style=true

# US-CTX-001: the Open-Meteo terms of use, attribution and eligibility are not reviewed yet (OPEN-S4-24, guide
# section 14 M7), so a local run never contacts the provider unless CAPSULE_WEATHER_ENABLED=true is set deliberately.
# Until then the location API reports weather as UNAVAILABLE / NOT_CONFIGURED.
capsuleai.personal-context.weather.enabled=${CAPSULE_WEATHER_ENABLED:false}
```

### Step P35 — [MODIFY] `backend/README.md`

Added by the review of 2026-11-23: the developer README documents the endpoint and the weather switch.

BEFORE:

````markdown
.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PreferencesApiIT,ConcurrentPreferencesIT,PreferencesSchemaIT,CurrentPersonalContextIT,DatabaseSchemaOwnershipIT,IdentityOpenApiContractIT' verify
```

## 10. Database migrations
````

AFTER:

````markdown
.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PreferencesApiIT,ConcurrentPreferencesIT,PreferencesSchemaIT,CurrentPersonalContextIT,DatabaseSchemaOwnershipIT,IdentityOpenApiContractIT' verify
```

### Location and weather

US-CTX-001 adds `GET`/`PATCH /api/v1/personal-context/location`:

- **The selection.** One per account, `DEVICE` or `MANUAL` (a manual city needs a `label`), with its own `revision`. The server rounds it to two decimals before storing it or sending it anywhere. `selection: null` clears it.
- **The environment.** It is reported separately as `NO_SELECTION`, `CURRENT` (with the reading and both `observedAt` and `retrievedAt`), or `UNAVAILABLE` with a reason.
- **How weather is acquired.** Weather comes from Open-Meteo after the selection is committed, within one 2-second deadline. A reading is current while its `observedAt` is at most 30 minutes old.

**Weather provider switch.** The **Open-Meteo terms of use, attribution and eligibility for a deployment are not reviewed**:

- `capsuleai.personal-context.weather.enabled` defaults to `true`. Set `CAPSULE_WEATHER_ENABLED=false` to switch the provider off without a code change. The API then reports `UNAVAILABLE` / `NOT_CONFIGURED`.
- The `local` profile defaults to `false`, so a local run makes no provider request unless `CAPSULE_WEATHER_ENABLED=true` is set deliberately.
- Tests never contact the provider.

Focused tests, from `backend`:

```powershell
.\mvnw.cmd '-Dtest=LocationSelectionTest,WeatherFreshnessTest,OpenMeteoResponseTranslatorTest,OpenMeteoWeatherProviderTest,ReviseLocationServiceTest,PersonalContextWeatherPropertiesTest' test
.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=LocationSchemaIT,LocationContextApiIT,ConcurrentLocationIT,PreferencesSchemaIT,DatabaseSchemaOwnershipIT,IdentityOpenApiContractIT' verify
```

## 10. Database migrations
````

## 13. Complete Automated Test Implementation

The counts in P33 are the CI minimums. The review of 2026-11-23 added tests, so six classes now hold more than their minimum. No test calls the public internet: integration tests use `ControllableWeatherProvider`, and the adapter is tested against an in-process HTTP server.

| Class | Tests | CI minimum |
| --- | ---: | ---: |
| `OpenMeteoResponseTranslatorTest` | 8 | 5 |
| `OpenMeteoWeatherProviderTest` | 12 | 7 |
| `ReviseLocationServiceTest` | 11 | 8 |
| `LocationContextApiIT` | 10 | 8 |
| `ConcurrentLocationIT` | 8 | 4 |
| `PersonalContextWeatherPropertiesTest` | 4 | 3 |

### Step T1 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/domain/LocationSelectionTest.java`

```java
package com.capsuleai.personalcontext.domain;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-CTX-001 (OPEN-S4-18): rounding, geographic range and label rules of a selection. */
class LocationSelectionTest {

    @Test
    void coordinatesAreRoundedHalfEvenToTwoDecimals() {
        LocationSelection selection = LocationSelection.rounded(new BigDecimal("10.765"), new BigDecimal("106.6667"),
                Optional.empty(), LocationSource.DEVICE);

        assertThat(selection.latitude()).isEqualTo(new BigDecimal("10.76"));
        assertThat(selection.longitude()).isEqualTo(new BigDecimal("106.67"));
        assertThat(LocationSelection.round(new BigDecimal("10.775"))).isEqualTo(new BigDecimal("10.78"));
    }

    @Test
    void theGeographicRangeIsCheckedAfterRounding() {
        assertThat(LocationSelection.isLatitude(LocationSelection.round(new BigDecimal("90.004")))).isTrue();
        assertThat(LocationSelection.isLatitude(LocationSelection.round(new BigDecimal("-90.01")))).isFalse();
        assertThat(LocationSelection.isLongitude(LocationSelection.round(new BigDecimal("180.00")))).isTrue();
        assertThat(LocationSelection.isLongitude(LocationSelection.round(new BigDecimal("180.006")))).isFalse();
        assertThatThrownBy(() -> new LocationSelection(new BigDecimal("10.7"), new BigDecimal("106.70"),
                Optional.empty(), LocationSource.DEVICE)).as("unrounded").isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aManualSelectionNeedsALabelAndADeviceSelectionMayOmitIt() {
        assertThatThrownBy(() -> LocationSelection.rounded(BigDecimal.ONE, BigDecimal.ONE, Optional.empty(),
                LocationSource.MANUAL)).isInstanceOf(IllegalArgumentException.class);
        assertThat(LocationSelection.rounded(BigDecimal.ONE, BigDecimal.ONE, Optional.empty(), LocationSource.DEVICE)
                .label()).isEmpty();
        assertThat(LocationSelection.rounded(BigDecimal.ONE, BigDecimal.ONE, Optional.of("Huế"), LocationSource.MANUAL)
                .label()).contains("Huế");
    }

    @Test
    void labelsAreNormalizedAndBounded() {
        assertThat(LocationSelection.normalizeLabel("  Huế  ")).contains("Huế");
        assertThat(LocationSelection.normalizeLabel("   ")).isEmpty();
        assertThat(LocationSelection.isAcceptableLabel("a".repeat(80))).isTrue();
        assertThat(LocationSelection.isAcceptableLabel("a".repeat(81))).isFalse();
        assertThat(LocationSelection.isAcceptableLabel("Line\nBreak")).isFalse();
    }
}
```

### Step T2 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/domain/WeatherFreshnessTest.java`

```java
package com.capsuleai.personalcontext.domain;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-CTX-001 AC-03 (OPEN-S4-21): the inclusive 30-minute boundary and the WMO code translation. */
class WeatherFreshnessTest {

    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");

    @Test
    void evidenceExactlyThirtyMinutesOldIsCurrent() {
        assertThat(WeatherFreshness.isCurrentAt(NOW.minus(Duration.ofMinutes(30)), NOW)).isTrue();
        assertThat(WeatherFreshness.isCurrentAt(NOW, NOW)).isTrue();
    }

    @Test
    void evidenceJustOlderThanThirtyMinutesIsNotCurrent() {
        assertThat(WeatherFreshness.isCurrentAt(NOW.minus(Duration.ofMinutes(30)).minusNanos(1_000), NOW)).isFalse();
    }

    @Test
    void slightlyFutureEvidenceIsToleratedButFarFutureEvidenceIsImplausible() {
        assertThat(WeatherFreshness.isCurrentAt(NOW.plus(Duration.ofMinutes(5)), NOW)).isTrue();
        assertThat(WeatherFreshness.isImplausiblyFuture(NOW.plus(Duration.ofMinutes(5)), NOW)).isFalse();
        assertThat(WeatherFreshness.isCurrentAt(NOW.plus(Duration.ofMinutes(6)), NOW)).isFalse();
        assertThat(WeatherFreshness.isImplausiblyFuture(NOW.plus(Duration.ofMinutes(6)), NOW)).isTrue();
    }

    @Test
    void wmoCodesMapToConditionsAndUnknownCodesDoNot() {
        assertThat(WeatherCondition.fromWmoCode(0)).contains(WeatherCondition.CLEAR);
        assertThat(WeatherCondition.fromWmoCode(2)).contains(WeatherCondition.PARTLY_CLOUDY);
        assertThat(WeatherCondition.fromWmoCode(63)).contains(WeatherCondition.RAIN);
        assertThat(WeatherCondition.fromWmoCode(99)).contains(WeatherCondition.THUNDERSTORM_WITH_HAIL);
        assertThat(WeatherCondition.fromWmoCode(42)).isEmpty();
    }
}
```

### Step T3 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/adapter/out/weather/OpenMeteoResponseTranslatorTest.java`

```java
package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-CTX-001 (OPEN-S4-20): translation of the documented Open-Meteo current-weather shape. The sample follows the
 * provider's public documentation; a real response must still be checked (guide §14 M7).
 */
class OpenMeteoResponseTranslatorTest {

    static final String DOCUMENTED_RESPONSE = """
            {"latitude":10.75,"longitude":106.75,"generationtime_ms":0.05,"utc_offset_seconds":0,
             "timezone":"GMT","timezone_abbreviation":"GMT","elevation":9.0,
             "current_units":{"time":"unixtime","interval":"seconds","temperature_2m":"°C",
               "apparent_temperature":"°C","relative_humidity_2m":"%","precipitation":"mm",
               "weather_code":"wmo code","wind_speed_10m":"km/h"},
             "current":{"time":1763285400,"interval":900,"temperature_2m":31.4,"apparent_temperature":35.1,
               "relative_humidity_2m":70,"precipitation":0.0,"weather_code":2,"wind_speed_10m":12.3}}
            """;

    private static final WeatherQuery QUERY = new WeatherQuery(new BigDecimal("10.78"), new BigDecimal("106.70"));
    private static final Instant RETRIEVED_AT = Instant.parse("2026-11-16T09:31:00Z");

    private final OpenMeteoResponseTranslator translator = new OpenMeteoResponseTranslator();

    @Test
    void theDocumentedCurrentResponseIsTranslated() {
        WeatherAcquisition acquisition = translate(DOCUMENTED_RESPONSE);

        assertThat(acquisition).isInstanceOf(WeatherAcquisition.Acquired.class);
        WeatherReading reading = ((WeatherAcquisition.Acquired) acquisition).reading();
        assertThat(reading.observedAt()).isEqualTo(Instant.ofEpochSecond(1763285400));
        assertThat(reading.retrievedAt()).isEqualTo(RETRIEVED_AT);
        assertThat(reading.temperatureCelsius()).isEqualTo(new BigDecimal("31.40"));
        assertThat(reading.apparentTemperatureCelsius()).contains(new BigDecimal("35.10"));
        assertThat(reading.relativeHumidityPercent()).contains(70);
        assertThat(reading.precipitationMillimetres()).contains(new BigDecimal("0.00"));
        assertThat(reading.windSpeedKilometresPerHour()).contains(new BigDecimal("12.30"));
        assertThat(reading.weatherCode()).isEqualTo(2);
        assertThat(reading.condition()).isEqualTo(WeatherCondition.PARTLY_CLOUDY);
        assertThat(reading.basis()).isEqualTo("MODELLED");
        assertThat(reading.provider()).isEqualTo("OPEN_METEO");
    }

    @Test
    void malformedJsonOrAMissingCurrentBlockIsInvalid() {
        assertInvalid("{not json");
        assertInvalid("[]");
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"current\":{", "\"hourly\":{"));
    }

    @Test
    void anUnexpectedUnitOrANonNumericValueIsInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":\"°C\"", "\"temperature_2m\":\"°F\""));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":31.4", "\"temperature_2m\":\"31.4\""));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"time\":1763285400", "\"time\":\"2026-11-16T09:30\""));
    }

    @Test
    void aGridPointFarFromTheRequestedLocationIsInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"latitude\":10.75", "\"latitude\":12.0"));
    }

    @Test
    void anUnknownWeatherCodeOrAnImplausibleValueIsInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"weather_code\":2", "\"weather_code\":42"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"relative_humidity_2m\":70", "\"relative_humidity_2m\":140"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":31.4", "\"temperature_2m\":3140"));
    }

    /**
     * Numbers that cannot be represented are invalid provider data, never an exception or a provider failure:
     * a code beyond int, humidity beyond int, a value that overflows a double, a time beyond long or beyond the
     * representable instants, and a non-finite grid coordinate.
     */
    @Test
    void unrepresentableNumbersAreInvalidProviderData() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"weather_code\":2", "\"weather_code\":4294967298"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"relative_humidity_2m\":70", "\"relative_humidity_2m\":1e10"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"temperature_2m\":31.4", "\"temperature_2m\":1e400"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"time\":1763285400", "\"time\":99999999999999999999"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"time\":1763285400", "\"time\":9223372036854775807"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"latitude\":10.75", "\"latitude\":1e400"));
    }

    /** Negative amounts are impossible; units are never converted (km/h is required, not assumed). */
    @Test
    void negativeAmountsAndOtherUnitsAreInvalid() {
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"precipitation\":0.0", "\"precipitation\":-0.1"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"wind_speed_10m\":12.3", "\"wind_speed_10m\":-1"));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"wind_speed_10m\":\"km/h\"", "\"wind_speed_10m\":\"m/s\""));
        assertInvalid(DOCUMENTED_RESPONSE.replace("\"precipitation\":\"mm\"", "\"precipitation\":\"inch\""));
    }

    /** Only time, temperature and weather code are required; absent or null optional values stay absent. */
    @Test
    void absentOrNullOptionalValuesStayAbsentAndARequiredOneIsNeeded() {
        String minimal = """
                {"latitude":10.75,"longitude":106.75,"current_units":{"temperature_2m":"°C"},
                 "current":{"time":1763285400,"temperature_2m":31.4,"weather_code":0,"precipitation":null}}
                """;

        WeatherReading reading = ((WeatherAcquisition.Acquired) translate(minimal)).reading();

        assertThat(reading.apparentTemperatureCelsius()).isEmpty();
        assertThat(reading.relativeHumidityPercent()).isEmpty();
        assertThat(reading.precipitationMillimetres()).isEmpty();
        assertThat(reading.windSpeedKilometresPerHour()).isEmpty();
        assertThat(reading.condition()).isEqualTo(WeatherCondition.CLEAR);
        assertInvalid(minimal.replace("\"temperature_2m\":31.4,", ""));
        assertInvalid(minimal.replace("\"weather_code\":0,", ""));
    }

    private WeatherAcquisition translate(String json) {
        return translator.translate(json.getBytes(StandardCharsets.UTF_8), QUERY, RETRIEVED_AT);
    }

    private void assertInvalid(String json) {
        assertThat(translate(json)).isEqualTo(
                new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA));
    }
}
```

### Step T4 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/adapter/out/weather/OpenMeteoWeatherProviderTest.java`

```java
package com.capsuleai.personalcontext.adapter.out.weather;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-CTX-001 (S4-CTX-05, OPEN-S4-22) — the real adapter against an in-process HTTP server with controlled
 * delays: one elapsed deadline including capacity wait, slow headers and slow bodies; explicit outcomes for
 * errors, oversized bodies and unreachable providers; only the location is sent. A 500 ms deadline keeps the
 * suite fast; production uses PT2S. The tolerance is a test allowance for scheduling, not a new limit.
 */
class OpenMeteoWeatherProviderTest {

    private static final Duration DEADLINE = Duration.ofMillis(500);
    private static final Duration TOLERANCE = Duration.ofMillis(500);
    private static final WeatherQuery QUERY = new WeatherQuery(new BigDecimal("10.78"), new BigDecimal("106.70"));

    private final List<String> receivedQueries = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private ExecutorService serverThreads;
    private volatile HttpHandler behaviour;
    private OpenMeteoWeatherProvider weatherProvider;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            receivedQueries.add(exchange.getRequestURI().getRawQuery());
            behaviour.handle(exchange);
        });
        serverThreads = Executors.newCachedThreadPool();
        server.setExecutor(serverThreads);
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (weatherProvider != null) {
            weatherProvider.close();
        }
        server.stop(0);
        serverThreads.shutdownNow();
    }

    @Test
    void aValidResponseIsAcquiredAndOnlyTheLocationIsSent() {
        behaviour = exchange -> respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);

        WeatherAcquisition acquisition = provider(2).acquire(QUERY);

        assertThat(acquisition).isInstanceOf(WeatherAcquisition.Acquired.class);
        String query = receivedQueries.getFirst();
        assertThat(Arrays.stream(query.split("&")).map(parameter -> parameter.substring(0, parameter.indexOf('='))))
                .containsExactlyInAnyOrder("latitude", "longitude", "current", "timezone", "timeformat");
        assertThat(query).contains("latitude=10.78", "longitude=106.70", "timezone=GMT", "timeformat=unixtime");
    }

    @Test
    void slowResponseHeadersExceedTheDeadline() {
        behaviour = exchange -> {
            pause(3000);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };

        assertDeadlineExceededInTime(provider(2));
    }

    @Test
    void aSlowBodyExceedsTheDeadline() {
        behaviour = exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write("{\"latitude\":10.75,".getBytes(StandardCharsets.UTF_8));
                body.flush();
                pause(3000);
                body.write("\"longitude\":106.75}".getBytes(StandardCharsets.UTF_8));
            }
        };

        assertDeadlineExceededInTime(provider(2));
    }

    @Test
    void aServerErrorIsProviderUnavailable() {
        behaviour = exchange -> respond(exchange, 500, "{\"error\":true}");

        assertThat(provider(2).acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
    }

    @Test
    void anOversizedBodyIsInvalidProviderData() {
        behaviour = exchange -> respond(exchange, 200, "x".repeat(70_000));

        assertThat(provider(2).acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.INVALID_PROVIDER_DATA));
    }

    @Test
    void waitingForCapacityCountsAgainstTheSameDeadline() throws Exception {
        behaviour = exchange -> {
            pause(3000);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };
        OpenMeteoWeatherProvider singleCapacity = provider(1);

        CompletableFuture<Duration> first = CompletableFuture.supplyAsync(() -> timedDeadlineExceeded(singleCapacity));
        CompletableFuture<Duration> second = CompletableFuture.supplyAsync(() -> timedDeadlineExceeded(singleCapacity));

        assertThat(first.get()).isLessThan(DEADLINE.plus(TOLERANCE));
        assertThat(second.get()).isLessThan(DEADLINE.plus(TOLERANCE));
    }

    @Test
    void anUnreachableProviderIsUnavailableWithinTheDeadline() throws IOException {
        int closedPort;
        try (ServerSocket probe = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            closedPort = probe.getLocalPort();
        }
        OpenMeteoWeatherProvider unreachable = provider(URI.create("http://127.0.0.1:" + closedPort), 2);

        long start = System.nanoTime();
        WeatherAcquisition acquisition = unreachable.acquire(QUERY);

        assertThat(acquisition).isInstanceOf(WeatherAcquisition.Unavailable.class);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(DEADLINE.plus(TOLERANCE));
    }

    /**
     * A call abandoned at the deadline keeps its capacity permit only until the abandoned work really ends; the
     * permit is never leaked, so a later acquisition gets through once the late call has stopped.
     */
    @Test
    void anAbandonedCallReleasesItsCapacityOnceItEnds() throws Exception {
        behaviour = exchange -> {
            pause(1500);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };
        OpenMeteoWeatherProvider singleCapacity = provider(1);
        assertDeadlineExceededInTime(singleCapacity);

        behaviour = exchange -> respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        long giveUp = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        WeatherAcquisition later = singleCapacity.acquire(QUERY);
        while (!(later instanceof WeatherAcquisition.Acquired) && System.nanoTime() < giveUp) {
            later = singleCapacity.acquire(QUERY);
        }

        assertThat(later).isInstanceOf(WeatherAcquisition.Acquired.class);
    }

    /**
     * Many concurrent callers against a provider that never answers in time: every caller gets DEADLINE_EXCEEDED
     * within the deadline, and the adapter never runs more acquisition threads than its capacity.
     */
    @Test
    void concurrentTimeoutsNeverGrowAcquisitionThreadsBeyondTheCapacity() throws Exception {
        behaviour = exchange -> {
            pause(3000);
            respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        };
        Set<Long> earlierThreads = acquisitionThreadIds();
        OpenMeteoWeatherProvider twoAtATime = provider(2);
        ExecutorService callers = Executors.newFixedThreadPool(12);
        try {
            List<CompletableFuture<Duration>> calls = new ArrayList<>();
            for (int caller = 0; caller < 12; caller++) {
                calls.add(CompletableFuture.supplyAsync(() -> timedDeadlineExceeded(twoAtATime), callers));
            }
            for (CompletableFuture<Duration> call : calls) {
                assertThat(call.get()).isLessThan(DEADLINE.plus(TOLERANCE));
            }
        } finally {
            callers.shutdownNow();
        }

        Set<Long> newThreads = acquisitionThreadIds();
        newThreads.removeAll(earlierThreads);
        assertThat(newThreads).hasSizeLessThanOrEqualTo(2);
    }

    /** OPEN-S4-24: a redirect is never followed, so the request cannot leave the configured host. */
    @Test
    void aRedirectIsNotFollowed() {
        behaviour = exchange -> {
            exchange.getResponseHeaders().add("Location", "http://127.0.0.1:" + server.getAddress().getPort()
                    + "/elsewhere");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        };

        assertThat(provider(2).acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
        assertThat(receivedQueries).as("only the original request reached the server").hasSize(1);
    }

    /** AC-07, OPEN-S4-24: the only log line names the outcome category, never the point or the URL. */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void aFailureIsLoggedWithoutTheLocationOrTheUrl(CapturedOutput output) {
        behaviour = exchange -> respond(exchange, 503, "{\"error\":true}");

        provider(2).acquire(QUERY);

        assertThat(output.getAll())
                .contains("Weather acquisition unavailable (PROVIDER_UNAVAILABLE)")
                .doesNotContain("10.78")
                .doesNotContain("106.70")
                .doesNotContain("latitude")
                .doesNotContain("127.0.0.1:" + server.getAddress().getPort());
    }

    /**
     * Creating the adapter contacts nobody; closing it (the application context does on shutdown) stops its
     * acquisition threads and its HTTP client, and an acquisition after closing is unavailable, never a hang.
     */
    @Test
    void creatingOpensNoConnectionAndClosingStopsTheAdaptersThreads() {
        behaviour = exchange -> respond(exchange, 200, OpenMeteoResponseTranslatorTest.DOCUMENTED_RESPONSE);
        Set<Long> earlierThreads = adapterThreadIds();
        OpenMeteoWeatherProvider adapter = provider(2);
        assertThat(receivedQueries).as("construction sends no request").isEmpty();
        assertThat(adapter.acquire(QUERY)).isInstanceOf(WeatherAcquisition.Acquired.class);

        adapter.close();

        long giveUp = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        Set<Long> remaining = adapterThreadIds();
        remaining.removeAll(earlierThreads);
        while (!remaining.isEmpty() && System.nanoTime() < giveUp) {
            pause(20);
            remaining = adapterThreadIds();
            remaining.removeAll(earlierThreads);
        }
        assertThat(remaining).as("acquisition and HTTP client threads after close").isEmpty();
        assertThat(adapter.acquire(QUERY))
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
    }

    /** Threads owned by an adapter: its acquisition pool and the JDK HTTP client's selector and workers. */
    private static Set<Long> adapterThreadIds() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(thread -> thread.getName().equals("weather-acquisition") || thread.getName().startsWith("HttpClient-"))
                .map(Thread::threadId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Set<Long> acquisitionThreadIds() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(thread -> thread.getName().equals("weather-acquisition") && thread.isAlive())
                .map(Thread::threadId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private OpenMeteoWeatherProvider provider(int maximumConcurrentRequests) {
        return provider(URI.create("http://127.0.0.1:" + server.getAddress().getPort()), maximumConcurrentRequests);
    }

    private OpenMeteoWeatherProvider provider(URI baseUrl, int maximumConcurrentRequests) {
        weatherProvider = new OpenMeteoWeatherProvider(baseUrl, DEADLINE, maximumConcurrentRequests, 65_536,
                Clock.systemUTC());
        return weatherProvider;
    }

    private void assertDeadlineExceededInTime(OpenMeteoWeatherProvider provider) {
        assertThat(timedDeadlineExceeded(provider)).isLessThan(DEADLINE.plus(TOLERANCE));
    }

    private static Duration timedDeadlineExceeded(OpenMeteoWeatherProvider provider) {
        long start = System.nanoTime();
        WeatherAcquisition acquisition = provider.acquire(QUERY);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertThat(acquisition)
                .isEqualTo(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED));
        return elapsed;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
```

**Explanation.** `com.sun.net.httpserver` ships with the JDK (module `jdk.httpserver`), so no test dependency is added. The server binds to the loopback address on an ephemeral port.

### Step T5 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/support/ControllableWeatherProvider.java`

```java
package com.capsuleai.personalcontext.support;

import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.WeatherCondition;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * TEST-ONLY weather provider (US-CTX-001). Every integration context uses it instead of Open-Meteo: it records each
 * query, answers as told (PROVIDER_UNAVAILABLE by default) and can run an action just before answering, to make
 * races with location changes deterministic. Production never uses it.
 */
public final class ControllableWeatherProvider implements WeatherProvider {

    private final List<WeatherQuery> queries = new CopyOnWriteArrayList<>();
    private volatile Function<WeatherQuery, WeatherAcquisition> responder;
    private volatile Runnable beforeNextAcquisition;

    public ControllableWeatherProvider() {
        reset();
    }

    @Override
    public WeatherAcquisition acquire(WeatherQuery query) {
        queries.add(query);
        Runnable action = beforeNextAcquisition;
        beforeNextAcquisition = null;
        if (action != null) {
            action.run();
        }
        return responder.apply(query);
    }

    public void respondWith(WeatherAcquisition acquisition) {
        responder = query -> acquisition;
    }

    /** Answers with a typical reading whose provider evidence time is {@code observedAt}. */
    public void respondWithReadingObservedAt(Instant observedAt) {
        respondWith(new WeatherAcquisition.Acquired(reading(observedAt, observedAt.plusSeconds(1))));
    }

    public void beforeNextAcquisition(Runnable action) {
        beforeNextAcquisition = action;
    }

    public List<WeatherQuery> queries() {
        return List.copyOf(queries);
    }

    public void reset() {
        queries.clear();
        responder = query -> new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        beforeNextAcquisition = null;
    }

    public static WeatherReading reading(Instant observedAt, Instant retrievedAt) {
        return new WeatherReading(observedAt, retrievedAt, new BigDecimal("31.40"), Optional.of(new BigDecimal("35.10")),
                Optional.of(70), Optional.of(new BigDecimal("0.00")), Optional.of(new BigDecimal("12.30")), 2,
                WeatherCondition.PARTLY_CLOUDY, WeatherReading.OPEN_METEO, WeatherReading.MODELLED);
    }
}
```

### Step T6 — [MODIFY] `backend/src/test/java/com/capsuleai/identity/support/IdentityTestConfiguration.java`

BEFORE:

```java
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
```

AFTER:

```java
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.personalcontext.support.ControllableWeatherProvider;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
```

BEFORE:

```java
    FaultInjectingPhotoStorage faultInjectingPhotoStorage() {
        return new FaultInjectingPhotoStorage();
    }
```

AFTER:

```java
    FaultInjectingPhotoStorage faultInjectingPhotoStorage() {
        return new FaultInjectingPhotoStorage();
    }

    /**
     * US-CTX-001: every integration context answers weather requests from a controllable double instead of
     * contacting Open-Meteo. The real adapter is verified separately by OpenMeteoWeatherProviderTest.
     */
    @Bean
    @Primary
    ControllableWeatherProvider controllableWeatherProvider() {
        return new ControllableWeatherProvider();
    }
```

### Step T7 — [MODIFY] `backend/src/test/resources/application-test.properties`

BEFORE (the lines added by US-GAR-005 T5; the file uses CRLF):

```properties
# US-GAR-005: tests call GarmentRemovalCleanupService directly instead of waiting for the scheduler.
capsuleai.wardrobe.removal-cleanup.enabled=false
```

AFTER:

```properties
# US-GAR-005: tests call GarmentRemovalCleanupService directly instead of waiting for the scheduler.
capsuleai.wardrobe.removal-cleanup.enabled=false

# US-CTX-001: the real Open-Meteo adapter is never created in integration contexts; ControllableWeatherProvider
# (IdentityTestConfiguration) answers instead. OpenMeteoWeatherProviderTest tests the adapter against a local server.
capsuleai.personal-context.weather.enabled=false
```

### Step T8 — [MODIFY] `backend/src/test/java/com/capsuleai/personalcontext/support/PersonalContextIntegrationTestSupport.java`

BEFORE:

```java
import org.junit.jupiter.api.BeforeEach;
```

AFTER:

```java
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
```

BEFORE:

```java
    protected static final String OPTIONAL_CONTEXT = "/api/v1/personal-context/optional-context";
```

AFTER:

```java
    protected static final String OPTIONAL_CONTEXT = "/api/v1/personal-context/optional-context";
    protected static final String LOCATION = "/api/v1/personal-context/location";
```

BEFORE:

```java
    private static final JsonMapper JSON = JsonMapper.builder().build();
```

AFTER:

```java
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** US-CTX-001: the @Primary weather double of every integration context. */
    @Autowired
    protected ControllableWeatherProvider weatherProvider;
```

BEFORE (as left by US-PROF-002 T5):

```java
                + "personal_context.personal_context_optional_context")
                .update();
```

AFTER:

```java
                + "personal_context.personal_context_optional_context, "
                + "personal_context.personal_context_weather_evidence, personal_context.personal_context_location")
                .update();
        weatherProvider.reset();
```

BEFORE:

```java
    protected static Map<String, Object> json(Object... namesAndValues) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        for (int index = 0; index < namesAndValues.length; index += 2) {
            body.put((String) namesAndValues[index], namesAndValues[index + 1]);
        }
        return body;
    }
```

AFTER:

```java
    protected static Map<String, Object> json(Object... namesAndValues) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        for (int index = 0; index < namesAndValues.length; index += 2) {
            body.put((String) namesAndValues[index], namesAndValues[index + 1]);
        }
        return body;
    }

    /** US-CTX-001. */
    protected ResultActions viewLocation(String accessToken) throws Exception {
        return mockMvc.perform(get(LOCATION).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    /** US-CTX-001. Build bodies with {@link #json(Object...)} to keep JSON nulls. */
    protected ResultActions reviseLocation(String accessToken, Map<String, ?> body) throws Exception {
        return mockMvc.perform(patch(LOCATION)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }
```

**Explanation.** The evidence table is listed before its referenced location table, though `TRUNCATE` of both in one statement handles the foreign key either way. The provider double is reset before every test.

### Step T9 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/application/InMemoryLocationRepository.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link LocationRepository} with the same guard, deletion and publication rules. */
final class InMemoryLocationRepository implements LocationRepository {

    private final Map<UUID, LocationProfile> profiles = new HashMap<>();
    private final Map<UUID, StoredWeatherEvidence> evidence = new HashMap<>();
    private int selectionWrites;

    @Override
    public synchronized Optional<LocationProfile> find(UUID ownerAccountId) {
        return Optional.ofNullable(profiles.get(ownerAccountId));
    }

    @Override
    public synchronized boolean createIfAbsent(LocationProfile profile) {
        if (profiles.putIfAbsent(profile.ownerAccountId(), profile) != null) {
            return false;
        }
        selectionWrites++;
        return true;
    }

    @Override
    public synchronized boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision,
                                                  Optional<LocationSelection> selection, Instant updatedAt) {
        LocationProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        profiles.put(ownerAccountId, current.revisedTo(selection, updatedAt));
        evidence.remove(ownerAccountId);
        selectionWrites++;
        return true;
    }

    @Override
    public synchronized Optional<StoredWeatherEvidence> findEvidence(UUID ownerAccountId) {
        return Optional.ofNullable(evidence.get(ownerAccountId));
    }

    @Override
    public synchronized WeatherPublication publishEvidenceIfCurrent(UUID ownerAccountId, long locationRevision,
                                                                    WeatherReading reading) {
        LocationProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != locationRevision || current.selection().isEmpty()) {
            return WeatherPublication.SELECTION_CHANGED;
        }
        StoredWeatherEvidence stored = evidence.get(ownerAccountId);
        if (stored != null && stored.locationRevision() == locationRevision
                && !stored.reading().observedAt().isBefore(reading.observedAt())) {
            return WeatherPublication.KEPT_NEWER;
        }
        evidence.put(ownerAccountId, new StoredWeatherEvidence(locationRevision, reading));
        return WeatherPublication.PUBLISHED;
    }

    synchronized int selectionWrites() {
        return selectionWrites;
    }
}
```

### Step T10 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/application/ReviseLocationServiceTest.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import com.capsuleai.personalcontext.support.ControllableWeatherProvider;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-CTX-001 (S4-CTX-01/02/04; OPEN-S4-18/19/21): commit first, acquire after; reuse, refresh and staleness;
 * clearing; conflicts; validation; no-op.
 */
class ReviseLocationServiceTest {

    private static final UUID OWNER = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-11-16T09:30:00Z");

    private InMemoryLocationRepository locationRepository;
    private ControllableWeatherProvider weatherProvider;
    private MutableClock clock;
    private ReviseLocationService reviseLocationService;
    private ViewLocationContextService viewLocationContextService;

    @BeforeEach
    void setUp() {
        locationRepository = new InMemoryLocationRepository();
        weatherProvider = new ControllableWeatherProvider();
        clock = new MutableClock(NOW);
        ImmediatePersonalContextTransactions transactions = new ImmediatePersonalContextTransactions();
        EnvironmentalContextService environmentalContextService =
                new EnvironmentalContextService(locationRepository, weatherProvider, transactions, clock);
        reviseLocationService = new ReviseLocationService(locationRepository, environmentalContextService,
                transactions, clock);
        viewLocationContextService = new ViewLocationContextService(locationRepository, environmentalContextService);
    }

    @Test
    void aFirstSelectionIsRoundedStoredAndItsWeatherAcquiredAfterCommit() {
        weatherProvider.respondWithReadingObservedAt(NOW.minus(Duration.ofMinutes(10)));

        LocationContextView view = select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        LocationSelection selection = view.profile().orElseThrow().selection().orElseThrow();
        assertThat(view.profile().orElseThrow().revision()).isEqualTo(1);
        assertThat(selection.latitude()).isEqualTo(new BigDecimal("10.78"));
        assertThat(selection.longitude()).isEqualTo(new BigDecimal("106.70"));
        assertThat(view.environment()).isInstanceOf(EnvironmentalContext.Current.class);
        assertThat(weatherProvider.queries())
                .containsExactly(new WeatherQuery(new BigDecimal("10.78"), new BigDecimal("106.70")));
        assertThat(locationRepository.findEvidence(OWNER)).isPresent();
    }

    @Test
    void aProviderFailureKeepsTheAcceptedSelectionAndReportsUnavailable() {
        LocationContextView view = select(new ExpectedRevision.Unset(), manual("21.0285", "105.8048", "Hà Nội"));

        assertThat(view.profile().orElseThrow().selection()).isPresent();
        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
    }

    @Test
    void clearingRemovesTheSelectionAndItsEvidenceWithoutAProviderCall() {
        weatherProvider.respondWithReadingObservedAt(NOW);
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        LocationContextView cleared = reviseLocationService.revise(new ReviseLocationCommand(OWNER,
                new ExpectedRevision.At(1), ValueChange.clear()));

        assertThat(cleared.profile().orElseThrow().selection()).isEmpty();
        assertThat(cleared.profile().orElseThrow().revision()).isEqualTo(2);
        assertThat(cleared.environment()).isInstanceOf(EnvironmentalContext.NoSelection.class);
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();
        assertThat(weatherProvider.queries()).hasSize(1);
    }

    @Test
    void currentEvidenceIsReusedUpToExactlyThirtyMinutesAndRefreshedAfter() {
        weatherProvider.respondWithReadingObservedAt(NOW);
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        clock.advance(Duration.ofMinutes(30));
        assertThat(viewLocationContextService.view(OWNER).environment()).isInstanceOf(EnvironmentalContext.Current.class);
        assertThat(weatherProvider.queries()).as("exactly 30 minutes old is current: no refresh").hasSize(1);

        clock.advance(Duration.ofSeconds(1));
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        EnvironmentalContext refreshed = viewLocationContextService.view(OWNER).environment();

        assertThat(weatherProvider.queries()).hasSize(2);
        assertThat(((EnvironmentalContext.Current) refreshed).reading().observedAt()).isEqualTo(clock.instant());
    }

    @Test
    void aNewlyFetchedButStaleReadingIsNeverCurrentAndIsNotStored() {
        weatherProvider.respondWithReadingObservedAt(NOW.minus(Duration.ofMinutes(31)));

        LocationContextView view = select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.STALE_PROVIDER_DATA));
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();
    }

    @Test
    void aStaleRevisionAndASecondFirstSelectionAreConflicts() {
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(), manual("21.0285", "105.8048", "Hà Nội")))
                .isInstanceOf(LocationRevisionConflictException.class);
        assertThatThrownBy(() -> select(new ExpectedRevision.At(3), manual("21.0285", "105.8048", "Hà Nội")))
                .isInstanceOf(LocationRevisionConflictException.class);
    }

    @Test
    void invalidSelectionsAreRejectedWithEveryProblem() {
        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(),
                new SubmittedLocation(new BigDecimal("91"), null, null, "SATELLITE", List.of())))
                .isInstanceOfSatisfying(LocationValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("selection.latitude", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("selection.longitude", PreferenceValueProblem.REQUIRED),
                                new PreferenceFieldViolation("selection.source", PreferenceValueProblem.UNSUPPORTED_VALUE)));
        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", null)))
                .isInstanceOfSatisfying(LocationValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("selection.label", PreferenceValueProblem.REQUIRED)));
        assertThatThrownBy(() -> select(new ExpectedRevision.Unset(),
                new SubmittedLocation(null, null, null, null, List.of("selection.latitude"))))
                .isInstanceOfSatisfying(LocationValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("selection.latitude", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("selection.longitude", PreferenceValueProblem.REQUIRED),
                                new PreferenceFieldViolation("selection.source", PreferenceValueProblem.REQUIRED)));
        assertThat(locationRepository.find(OWNER)).isEmpty();
    }

    @Test
    void theSameSelectionAfterRoundingWritesNothing() {
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        LocationContextView unchanged = select(new ExpectedRevision.At(1), manual("10.7801", "106.6950", "Hồ Chí Minh"));

        assertThat(unchanged.profile().orElseThrow().revision()).isEqualTo(1);
        assertThat(locationRepository.selectionWrites()).isOne();
    }

    /** A provider defect that throws despite the port contract is contained: the selection stays, nothing is invented. */
    @Test
    void aProviderThatThrowsIsReportedAsUnavailableAndKeepsTheSelection() {
        ImmediatePersonalContextTransactions transactions = new ImmediatePersonalContextTransactions();
        EnvironmentalContextService failing = new EnvironmentalContextService(locationRepository, query -> {
            throw new IllegalStateException("adapter defect");
        }, transactions, clock);
        ReviseLocationService revise = new ReviseLocationService(locationRepository, failing, transactions, clock);

        LocationContextView view = revise.revise(new ReviseLocationCommand(OWNER, new ExpectedRevision.Unset(),
                ValueChange.replace(manual("21.0285", "105.8048", "Hà Nội"))));

        assertThat(view.profile().orElseThrow().selection()).isPresent();
        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE));
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();
    }

    /** OPEN-S4-21: up to 5 minutes in the future is clock skew (current); further is invalid and never stored. */
    @Test
    void evidenceFarInTheFutureIsInvalidWhileSmallClockSkewIsTolerated() {
        weatherProvider.respondWithReadingObservedAt(NOW.plus(Duration.ofMinutes(6)));

        LocationContextView view = select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));

        assertThat(view.environment()).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.INVALID_PROVIDER_DATA));
        assertThat(locationRepository.findEvidence(OWNER)).isEmpty();

        weatherProvider.respondWithReadingObservedAt(NOW.plus(Duration.ofMinutes(5)));
        assertThat(viewLocationContextService.view(OWNER).environment()).isInstanceOf(EnvironmentalContext.Current.class);
    }

    /** Evidence older than 30 minutes whose refresh fails is reported UNAVAILABLE, never presented as current. */
    @Test
    void staleStoredEvidenceWhoseRefreshFailsIsNeverPresentedAsCurrent() {
        weatherProvider.respondWithReadingObservedAt(NOW);
        select(new ExpectedRevision.Unset(), manual("10.7769", "106.7009", "Hồ Chí Minh"));
        clock.advance(Duration.ofMinutes(31));
        weatherProvider.respondWith(new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.DEADLINE_EXCEEDED));

        EnvironmentalContext environment = viewLocationContextService.view(OWNER).environment();

        assertThat(environment).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.DEADLINE_EXCEEDED));
        assertThat(weatherProvider.queries()).hasSize(2);
    }

    private LocationContextView select(ExpectedRevision expectedRevision, SubmittedLocation submitted) {
        return reviseLocationService.revise(new ReviseLocationCommand(OWNER, expectedRevision,
                ValueChange.replace(submitted)));
    }

    private static SubmittedLocation manual(String latitude, String longitude, String label) {
        return new SubmittedLocation(new BigDecimal(latitude), new BigDecimal(longitude), label, "MANUAL", List.of());
    }
}
```

**Explanation.** `10.7801`/`106.6950` round to the same `10.78`/`106.70` (half-even), so the second request is a no-op.

### Step T11 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/adapter/out/persistence/LocationSchemaIT.java`

```java
package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.foundation.IsolatedMigrationDatabase;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-CTX-001: the V14 constraints on their own, and the upgrade from V13. */
class LocationSchemaIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private PostgreSQLContainer server;

    @Test
    void anIncompleteOrInvalidSelectionIsRejectedByTheDatabase() {
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), null, null, "DEVICE"))
                .as("latitude without longitude").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), new BigDecimal("106.70"),
                null, "MANUAL")).as("manual without label").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("95.00"), new BigDecimal("106.70"),
                null, "DEVICE")).as("latitude out of range").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), new BigDecimal("106.70"),
                "x".repeat(81), "MANUAL")).as("label too long").isInstanceOf(DataIntegrityViolationException.class);

        insertLocation(UUID.randomUUID(), new BigDecimal("10.78"), new BigDecimal("106.70"), "Hồ Chí Minh", "MANUAL");
        insertLocation(UUID.randomUUID(), null, null, null, null);
        assertThat(countRows("personal_context.personal_context_location")).isEqualTo(2);
    }

    @Test
    void evidenceBelongsToALocationAndIsBounded() {
        UUID owner = UUID.randomUUID();
        insertLocation(owner, new BigDecimal("10.78"), new BigDecimal("106.70"), null, "DEVICE");

        assertThatThrownBy(() -> insertEvidence(UUID.randomUUID(), 50, "OPEN_METEO"))
                .as("evidence needs a location").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertEvidence(owner, 101, "OPEN_METEO"))
                .as("humidity is a percentage").isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertEvidence(owner, 50, "SOMEWHERE_ELSE"))
                .as("only the configured provider").isInstanceOf(DataIntegrityViolationException.class);
        insertEvidence(owner, 50, "OPEN_METEO");
        assertThat(countRows("personal_context.personal_context_weather_evidence")).isOne();
    }

    @Test
    void existingOptionalContextSurvivesTheUpgradeFromV13() {
        try (IsolatedMigrationDatabase database = IsolatedMigrationDatabase.create(server, jdbc)) {
            database.migrateTo("13");
            JdbcClient existing = database.jdbc();
            UUID owner = UUID.randomUUID();
            existing.sql("""
                            INSERT INTO personal_context.personal_context_optional_context (owner_account_id,
                                gender_context, revision, created_at, updated_at)
                            VALUES (:owner, 'NON_BINARY', 2, now(), now())
                            """)
                    .param("owner", owner).update();

            assertThat(database.migrateTo("14").migrationsExecuted).isOne();

            assertThat(existing.sql("SELECT gender_context FROM personal_context.personal_context_optional_context "
                            + "WHERE owner_account_id = :owner")
                    .param("owner", owner).query(String.class).single()).isEqualTo("NON_BINARY");
            assertThat(existing.sql("SELECT count(*) FROM personal_context.personal_context_location")
                    .query(Long.class).single()).as("no location is invented").isZero();
        }
    }

    private void insertLocation(UUID owner, BigDecimal latitude, BigDecimal longitude, String label, String source) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_location (owner_account_id, latitude, longitude,
                            label, source, revision, created_at, updated_at)
                        VALUES (:owner, :latitude, :longitude, :label, :source, 1, now(), now())
                        """)
                .param("owner", owner)
                .param("latitude", latitude, Types.NUMERIC)
                .param("longitude", longitude, Types.NUMERIC)
                .param("label", label, Types.VARCHAR)
                .param("source", source, Types.VARCHAR)
                .update();
    }

    private void insertEvidence(UUID owner, int humidity, String provider) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_weather_evidence (owner_account_id,
                            location_revision, observed_at, retrieved_at, temperature_celsius, relative_humidity_percent,
                            weather_code, provider, basis)
                        VALUES (:owner, 1, now(), now(), 30.10, :humidity, 2, :provider, 'MODELLED')
                        """)
                .param("owner", owner)
                .param("humidity", humidity)
                .param("provider", provider)
                .update();
    }
}
```

### Step T12 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/adapter/in/web/LocationContextApiIT.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US-CTX-001 through the real filter chain and PostgreSQL, with the weather provider replaced by
 * ControllableWeatherProvider: selection lifecycle, rounding, evidence times, inclusive freshness, truthful
 * fallback, clearing, conflicts, validation and access control (AC-01–AC-03, AC-06, AC-07).
 */
class LocationContextApiIT extends PersonalContextIntegrationTestSupport {

    @Test
    void anAccountWithoutASelectionHasNoEnvironmentAndCausesNoProviderCall() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-none@example.com");

        viewLocation(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.selection").value(nullValue()))
                .andExpect(jsonPath("$.environment.status").value("NO_SELECTION"))
                .andExpect(jsonPath("$.environment.basisRevision").value(nullValue()))
                .andExpect(jsonPath("$.environment.weather").value(nullValue()));
        assertThat(weatherProvider.queries()).isEmpty();
    }

    @Test
    void aManualCitySelectionReportsCurrentWeatherWithBothEvidenceTimes() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-manual@example.com");
        Instant observedAt = clock.instant().minus(Duration.ofMinutes(10));
        weatherProvider.respondWithReadingObservedAt(observedAt);

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.selection.latitude").value(10.78))
                .andExpect(jsonPath("$.selection.longitude").value(106.7))
                .andExpect(jsonPath("$.selection.label").value("Hồ Chí Minh"))
                .andExpect(jsonPath("$.selection.source").value("MANUAL"))
                .andExpect(jsonPath("$.environment.status").value("CURRENT"))
                .andExpect(jsonPath("$.environment.basisRevision").value("1"))
                .andExpect(jsonPath("$.environment.weather.observedAt").value(observedAt.toString()))
                .andExpect(jsonPath("$.environment.weather.retrievedAt").isString())
                .andExpect(jsonPath("$.environment.weather.basis").value("MODELLED"))
                .andExpect(jsonPath("$.environment.weather.attribution").value("Weather data by Open-Meteo.com"));
    }

    @Test
    void deviceCoordinatesAreRoundedBeforeStorageAndBeforeTheProvider() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-device@example.com");

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 21.028511, "longitude", 105.804817, "source", "DEVICE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selection.label").value(nullValue()));

        assertThat(weatherProvider.queries())
                .containsExactly(new WeatherQuery(new BigDecimal("21.03"), new BigDecimal("105.80")));
        assertThat(jdbc.sql("SELECT latitude::text || ',' || longitude::text FROM personal_context.personal_context_location "
                        + "WHERE owner_account_id = :owner")
                .param("owner", tokens.accountId()).query(String.class).single()).isEqualTo("21.03,105.80");
    }

    @Test
    void aProviderFailureKeepsTheSelectionAndDisclosesUnavailableWeather() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-failure@example.com");

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 16.4637, "longitude", 107.5909, "label", "Huế", "source", "MANUAL")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selection.label").value("Huế"))
                .andExpect(jsonPath("$.environment.status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.environment.unavailableReason").value("PROVIDER_UNAVAILABLE"))
                .andExpect(jsonPath("$.environment.weather").value(nullValue()));
        viewLocation(tokens.accessToken()).andExpect(jsonPath("$.selection.label").value("Huế"));
    }

    @Test
    void evidenceExactlyThirtyMinutesOldIsReusedAndOlderEvidenceIsRefreshed() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-freshness@example.com");
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")))
                .andExpect(status().isOk());

        clock.advance(Duration.ofMinutes(30));
        // Access tokens live 15 minutes (Identity), so the user signs in again after the clock has moved.
        String laterAccessToken = logIn("location-freshness@example.com", PASSWORD).accessToken();
        viewLocation(laterAccessToken).andExpect(jsonPath("$.environment.status").value("CURRENT"));
        assertThat(weatherProvider.queries()).hasSize(1);

        clock.advance(Duration.ofSeconds(1));
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        viewLocation(laterAccessToken)
                .andExpect(jsonPath("$.environment.status").value("CURRENT"))
                .andExpect(jsonPath("$.environment.weather.observedAt").value(clock.instant().toString()));
        assertThat(weatherProvider.queries()).hasSize(2);
    }

    @Test
    void clearingRemovesTheSelectionAndItsEvidence() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-clear@example.com");
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")));

        reviseLocation(tokens.accessToken(), json("expectedRevision", "1", "selection", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.selection").value(nullValue()))
                .andExpect(jsonPath("$.environment.status").value("NO_SELECTION"));

        assertThat(countRows("personal_context.personal_context_weather_evidence")).isZero();
        assertThat(weatherProvider.queries()).hasSize(1);
    }

    @Test
    void aStaleRevisionIsAConflictAndInvalidInputIsRejectedWithoutEcho() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-invalid@example.com");
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")));

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 21.0285, "longitude", 105.8048, "label", "Hà Nội", "source", "MANUAL")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        reviseLocation(tokens.accessToken(), json("expectedRevision", "1", "selection",
                Map.of("latitude", "north", "longitude", 200, "source", "MANUAL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.latitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.longitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.label')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors.length()").value(3))
                .andExpect(jsonPath("$.detail").value(not(containsString("north"))));
        viewLocation(tokens.accessToken()).andExpect(jsonPath("$.selection.label").value("Hồ Chí Minh"));
    }

    @Test
    void noOtherAccountOrSessionCanReadOrChangeIt() throws Exception {
        IssuedTokens owner = registerAndLogIn("location-owner@example.com");
        IssuedTokens other = registerAndLogIn("location-other@example.com");
        reviseLocation(owner.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "Hồ Chí Minh", "source", "MANUAL")));

        viewLocation(other.accessToken()).andExpect(jsonPath("$.environment.status").value("NO_SELECTION"));
        mockMvc.perform(get(LOCATION)).andExpect(status().isUnauthorized());
    }

    /**
     * A coordinate that overflows a double, a selection that is not an object, a source in another case and a label
     * of the wrong kind or length are field errors (400), never a server error; nothing is stored or requested.
     */
    @Test
    void unusableValuesAreFieldErrorsNeverServerErrors() throws Exception {
        IssuedTokens tokens = registerAndLogIn("location-unusable@example.com");

        reviseLocationRaw(tokens.accessToken(), """
                {"expectedRevision":null,"selection":{"latitude":1e400,"longitude":-1e400,"source":"DEVICE"}}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.latitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.longitude')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection", "Hà Nội"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("selection"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("UNSUPPORTED_VALUE"));
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", 7, "source", "manual")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.source')].code", hasItem("UNSUPPORTED_VALUE")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'selection.label')].code", hasItem("INVALID_FORMAT")));
        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 10.7769, "longitude", 106.7009, "label", "x".repeat(81), "source", "MANUAL")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("selection.label"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("INVALID_FORMAT"));

        assertThat(countRows("personal_context.personal_context_location")).isZero();
        assertThat(weatherProvider.queries()).isEmpty();
    }

    /** M6a automated: neither the submitted coordinates nor the label reach the application log. */
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void coordinatesAndLabelsNeverReachTheLog(CapturedOutput output) throws Exception {
        LoggerFactory.getLogger(LocationContextApiIT.class).info("location log capture is active");
        IssuedTokens tokens = registerAndLogIn("location-log@example.com");

        reviseLocation(tokens.accessToken(), json("expectedRevision", null, "selection",
                Map.of("latitude", 16.4637, "longitude", 107.5909, "label", "Huế Citadel", "source", "MANUAL")))
                .andExpect(status().isOk());
        reviseLocation(tokens.accessToken(), json("expectedRevision", "1", "selection",
                Map.of("latitude", 95.1234, "longitude", 107.5909, "label", "Huế Citadel", "source", "MANUAL")))
                .andExpect(status().isBadRequest());
        viewLocation(tokens.accessToken()).andExpect(status().isOk());

        assertThat(output.getAll())
                .as("the capture must see this class's own log line, or the check below proves nothing")
                .contains("location log capture is active")
                .doesNotContain("16.46").doesNotContain("107.59").doesNotContain("16.4637").doesNotContain("95.12")
                .doesNotContain("Huế Citadel");
    }

    private ResultActions reviseLocationRaw(String accessToken, String body) throws Exception {
        return mockMvc.perform(patch(LOCATION)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
```

**Explanation.** `jsonPath(...).value(106.7)` compares numerically, because Jackson writes `106.70` and JsonPath reads it as a `Double`. The database assertion uses the text form to check the stored scale.

### Step T13 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/application/ConcurrentLocationIT.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.LocationRepository.WeatherPublication;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.LocationSource;
import com.capsuleai.personalcontext.support.ControllableWeatherProvider;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-CTX-001 on real PostgreSQL (S4-CTX-02/06, OPEN-S4-19): evidence is published only for the selection revision it
 * was acquired for; a change or clear during acquisition is reported, never overwritten; older evidence never replaces
 * newer evidence of the same selection.
 */
class ConcurrentLocationIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private ReviseLocationService reviseLocationService;

    @Autowired
    private ViewLocationContextService viewLocationContextService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PersonalContextTransactions personalContextTransactions;

    @Autowired
    private ReviseOptionalContextService reviseOptionalContextService;

    @Autowired
    private RevisePreferencesService revisePreferencesService;

    @Test
    void aLateReadingForAReplacedSelectionIsNeverPublished() throws Exception {
        UUID owner = registerAndLogIn("race-location-replaced@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        select(owner, new ExpectedRevision.At(1), "21.0285", "105.8048");

        WeatherPublication publication = publish(owner, 1, clock.instant());

        assertThat(publication).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
    }

    @Test
    void aLateReadingAfterClearingIsNeverPublished() throws Exception {
        UUID owner = registerAndLogIn("race-location-cleared@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        reviseLocationService.revise(new ReviseLocationCommand(owner, new ExpectedRevision.At(1), ValueChange.clear()));

        assertThat(publish(owner, 1, clock.instant())).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        assertThat(publish(owner, 2, clock.instant())).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
    }

    @Test
    void anOlderReadingNeverReplacesANewerOneOfTheSameSelection() throws Exception {
        UUID owner = registerAndLogIn("race-location-older@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        Instant newer = clock.instant();

        assertThat(publish(owner, 1, newer)).isEqualTo(WeatherPublication.PUBLISHED);
        assertThat(publish(owner, 1, newer.minus(Duration.ofMinutes(5)))).isEqualTo(WeatherPublication.KEPT_NEWER);
        assertThat(locationRepository.findEvidence(owner).orElseThrow().reading().observedAt()).isEqualTo(newer);
    }

    @Test
    void aSelectionChangedDuringAcquisitionIsReportedAndNotPublished() throws Exception {
        UUID owner = registerAndLogIn("race-location-during@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        weatherProvider.respondWithReadingObservedAt(clock.instant());
        weatherProvider.beforeNextAcquisition(() -> personalContextTransactions.inTransaction(() ->
                locationRepository.reviseIfCurrentAt(owner, 1, Optional.of(LocationSelection.rounded(
                        new BigDecimal("16.4637"), new BigDecimal("107.5909"), Optional.of("Huế"),
                        LocationSource.MANUAL)), clock.instant())));

        EnvironmentalContext environment = viewLocationContextService.view(owner).environment();

        assertThat(environment).isEqualTo(
                new EnvironmentalContext.Unavailable(1, EnvironmentUnavailableReason.SELECTION_CHANGED));
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(2);
    }

    /** Race C: two changes of the same revision; the conditional UPDATE lets exactly one win. */
    @Test
    void ofTwoChangesOfTheSameRevisionExactlyOneIsAccepted() throws Exception {
        UUID owner = registerAndLogIn("race-location-same-revision@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");

        List<String> outcomes = race(change(owner, new ExpectedRevision.At(1), "21.0285", "105.8048"),
                change(owner, new ExpectedRevision.At(1), "16.4637", "107.5909"));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(2);
    }

    /**
     * A publication holds the selection row in share mode until it commits: a concurrent change really waits for it
     * (seen in pg_stat_activity), then commits and deletes the evidence of the old selection in its own transaction.
     */
    @Test
    void aChangeWaitsForAPublicationHoldingTheSelectionAndThenDeletesItsEvidence() throws Exception {
        UUID owner = registerAndLogIn("race-location-publication-first@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        CountDownLatch published = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<WeatherPublication> publication = threads.submit(() -> personalContextTransactions.inTransaction(() -> {
                WeatherPublication outcome = locationRepository.publishEvidenceIfCurrent(owner, 1,
                        ControllableWeatherProvider.reading(clock.instant(), clock.instant()));
                published.countDown();
                awaitQuietly(release);
                return outcome;
            }));
            assertThat(published.await(10, SECONDS)).isTrue();
            Future<String> change = threads.submit(change(owner, new ExpectedRevision.At(1), "21.0285", "105.8048"));

            awaitABackendWaitingForALock();
            assertThat(change.isDone()).as("the change waits for the publication's share lock").isFalse();
            release.countDown();

            assertThat(publication.get(10, SECONDS)).isEqualTo(WeatherPublication.PUBLISHED);
            assertThat(change.get(10, SECONDS)).isEqualTo("accepted");
        } finally {
            release.countDown();
            threads.shutdownNow();
        }
        assertThat(locationRepository.findEvidence(owner)).as("the old selection's evidence went with the change")
                .isEmpty();
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(2);
    }

    /**
     * A publication that has to wait behind an uncommitted change re-reads the row once the change commits (READ
     * COMMITTED re-check under the lock), sees the new revision and publishes nothing.
     */
    @Test
    void aPublicationWaitingBehindAnUncommittedChangeSeesTheNewRevisionAndPublishesNothing() throws Exception {
        UUID owner = registerAndLogIn("race-location-change-first@example.com").accountId();
        select(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009");
        CountDownLatch changed = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> change = threads.submit(() -> personalContextTransactions.inTransaction(() -> {
                boolean revised = locationRepository.reviseIfCurrentAt(owner, 1, Optional.of(LocationSelection.rounded(
                        new BigDecimal("16.4637"), new BigDecimal("107.5909"), Optional.of("Huế"), LocationSource.MANUAL)),
                        clock.instant());
                changed.countDown();
                awaitQuietly(release);
                return revised;
            }));
            assertThat(changed.await(10, SECONDS)).isTrue();
            Future<WeatherPublication> publication = threads.submit(() -> publish(owner, 1, clock.instant()));

            awaitABackendWaitingForALock();
            assertThat(publication.isDone()).as("the publication waits for the uncommitted change").isFalse();
            release.countDown();

            assertThat(change.get(10, SECONDS)).isTrue();
            assertThat(publication.get(10, SECONDS)).isEqualTo(WeatherPublication.SELECTION_CHANGED);
        } finally {
            release.countDown();
            threads.shutdownNow();
        }
        assertThat(locationRepository.findEvidence(owner)).isEmpty();
    }

    /** Race D: location, preferences and optional context are separate records; none blocks or overwrites another. */
    @Test
    void locationPreferenceAndOptionalContextChangesNeverConflict() throws Exception {
        UUID owner = registerAndLogIn("race-location-other-records@example.com").accountId();

        List<String> outcomes = race(change(owner, new ExpectedRevision.Unset(), "10.7769", "106.7009"), () -> {
            reviseOptionalContextService.revise(new ReviseOptionalContextCommand(owner, new ExpectedRevision.Unset(),
                    new OptionalContextChanges(ValueChange.replace("WOMAN"), ValueChange.keep())));
            return "accepted";
        }, () -> {
            revisePreferencesService.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(),
                    new PreferenceChanges(ValueChange.replace(List.of("CLASSIC")), Map.of(), false)));
            return "accepted";
        });

        assertThat(outcomes).containsExactly("accepted", "accepted", "accepted");
        assertThat(locationRepository.find(owner).orElseThrow().revision()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT gender_context || '/' || revision FROM personal_context.personal_context_optional_context "
                        + "WHERE owner_account_id = :owner")
                .param("owner", owner).query(String.class).single()).isEqualTo("WOMAN/1");
        assertThat(jdbc.sql("SELECT revision FROM personal_context.personal_context_preferences WHERE owner_account_id = :owner")
                .param("owner", owner).query(Long.class).single()).isEqualTo(1L);
    }

    private Callable<String> change(UUID owner, ExpectedRevision expectedRevision, String latitude, String longitude) {
        return () -> {
            try {
                select(owner, expectedRevision, latitude, longitude);
                return "accepted";
            } catch (LocationRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    @SafeVarargs
    private static List<String> race(Callable<String>... contenders) throws Exception {
        CyclicBarrier start = new CyclicBarrier(contenders.length);
        ExecutorService executor = Executors.newFixedThreadPool(contenders.length);
        try {
            List<Future<String>> outcomes = new ArrayList<>();
            for (Callable<String> contender : contenders) {
                outcomes.add(executor.submit(() -> {
                    start.await(10, SECONDS);
                    return contender.call();
                }));
            }
            List<String> results = new ArrayList<>();
            for (Future<String> outcome : outcomes) {
                results.add(outcome.get(20, SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    /** Polls PostgreSQL until another session of this database is blocked on a lock (at most 10 seconds). */
    private void awaitABackendWaitingForALock() throws InterruptedException {
        long giveUp = System.nanoTime() + SECONDS.toNanos(10);
        while (System.nanoTime() < giveUp) {
            long waiting = jdbc.sql("""
                            SELECT count(*) FROM pg_stat_activity
                            WHERE datname = current_database() AND wait_event_type = 'Lock'
                            """).query(Long.class).single();
            if (waiting > 0) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("No session started waiting for the row lock within 10 seconds");
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(20, SECONDS)) {
                throw new IllegalStateException("The test never released the held transaction");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while holding the transaction", interrupted);
        }
    }

    private void select(UUID owner, ExpectedRevision expectedRevision, String latitude, String longitude) {
        reviseLocationService.revise(new ReviseLocationCommand(owner, expectedRevision, ValueChange.replace(
                new SubmittedLocation(new BigDecimal(latitude), new BigDecimal(longitude), "Test city", "MANUAL",
                        List.of()))));
    }

    private WeatherPublication publish(UUID owner, long locationRevision, Instant observedAt) {
        return personalContextTransactions.inTransaction(() -> locationRepository.publishEvidenceIfCurrent(owner,
                locationRevision, ControllableWeatherProvider.reading(observedAt, observedAt.plusSeconds(1))));
    }
}
```

**Explanation.** The race in the last test is deterministic. The provider double runs the competing change in its own committed transaction while the acquisition is in progress, just as a concurrent request would, because no transaction or lock is held during acquisition.

### Step T14 — [MODIFY] `backend/src/test/java/com/capsuleai/personalcontext/adapter/out/persistence/PreferencesSchemaIT.java`

BEFORE (as left by US-PROF-002 T9):

```java
                .containsExactly("personal_context_common_need",
                        "personal_context_optional_context", // V13, US-PROF-002
                        "personal_context_preferences",
                        "personal_context_preferred_style");
```

AFTER:

```java
                .containsExactly("personal_context_common_need",
                        "personal_context_location", // V14, US-CTX-001
                        "personal_context_optional_context", // V13, US-PROF-002
                        "personal_context_preferences",
                        "personal_context_preferred_style",
                        "personal_context_weather_evidence"); // V14, US-CTX-001
```

### Step T15 — [ADD] `backend/src/test/java/com/capsuleai/personalcontext/configuration/PersonalContextWeatherPropertiesTest.java`

```java
package com.capsuleai.personalcontext.configuration;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-CTX-001 (OPEN-S4-22/24): startup validation of the weather configuration. */
class PersonalContextWeatherPropertiesTest {

    @Test
    void theShippedDefaultsAreAccepted() {
        PersonalContextWeatherProperties properties = properties(URI.create("https://api.open-meteo.com"),
                Duration.ofSeconds(2));

        assertThat(properties.acquisitionDeadline()).isEqualTo(PersonalContextWeatherProperties.MAXIMUM_ACQUISITION_DEADLINE);
        assertThat(properties.toString()).contains("host=api.open-meteo.com").doesNotContain("https://");
    }

    @Test
    void aDeadlineAboveTheApprovedTwoSecondsOrNotPositiveIsRejected() {
        URI provider = URI.create("https://api.open-meteo.com");

        assertThatThrownBy(() -> properties(provider, Duration.ofMillis(2001))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(provider, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThat(properties(provider, Duration.ofMillis(500)).acquisitionDeadline()).isEqualTo(Duration.ofMillis(500));
    }

    @Test
    void onlyAnHttpsOriginOrALoopbackTestHostIsAccepted() {
        Duration deadline = Duration.ofSeconds(2);

        assertThatThrownBy(() -> properties(URI.create("http://api.open-meteo.com"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("https://api.open-meteo.com?apikey=x"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("https://user:secret@api.open-meteo.com"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(properties(URI.create("http://127.0.0.1:18080"), deadline).baseUrl().getPort()).isEqualTo(18080);
    }

    @Test
    void theLimitsAndTheAttributionAreValidated() {
        URI provider = URI.create("https://api.open-meteo.com");
        Duration deadline = Duration.ofSeconds(2);

        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, Duration.ofSeconds(-1), 8, 65_536,
                "Weather data by Open-Meteo.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, deadline, 0, 65_536,
                "Weather data by Open-Meteo.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, deadline, 8, 1023,
                "Weather data by Open-Meteo.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PersonalContextWeatherProperties(true, provider, deadline, 8, 65_536, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("https://api.open-meteo.com#fragment"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties(URI.create("ftp://api.open-meteo.com"), deadline))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static PersonalContextWeatherProperties properties(URI baseUrl, Duration acquisitionDeadline) {
        return new PersonalContextWeatherProperties(true, baseUrl, acquisitionDeadline, 8, 65_536,
                "Weather data by Open-Meteo.com");
    }
}
```

**Explanation.** These are the checks that stop the application at startup when the configuration is unsafe. Spring Boot binds the record through its constructor, so an invalid value makes context startup fail instead of running with it.

Run:

```powershell
.\mvnw.cmd -q "-Dtest=LocationSelectionTest,WeatherFreshnessTest,OpenMeteoResponseTranslatorTest,OpenMeteoWeatherProviderTest,ReviseLocationServiceTest,PersonalContextWeatherPropertiesTest" test
.\mvnw.cmd "-Dtest=HarnessSmokeTest" "-Dit.test=LocationSchemaIT,LocationContextApiIT,ConcurrentLocationIT,PreferencesSchemaIT,OptionalContextApiIT,PreferencesApiIT,DatabaseSchemaOwnershipIT,IdentityOpenApiContractIT" verify
```

## 14. Manual Verification

**Prerequisites.**

- The setup is the same as [US-GAR-005 §14](US-GAR-005-guide.md#14-manual-verification), using `manual-helpers.ps1`.
- The account is synthetic. The coordinates are public city centres, not anyone's position.
- The script prints only PASS/FAIL.

**Run M1–M6 with the real provider switched off.** Until the Open-Meteo terms are reviewed, a local check must not contact the provider. Set the property in the PowerShell session that starts the backend (relaxed binding turns it into `capsuleai.personal-context.weather.enabled=false`), then start the backend as described in `backend/README.md`:

```powershell
$env:CAPSULEAI_PERSONALCONTEXT_WEATHER_ENABLED = 'false'
```

```powershell
# manual-ctx001.ps1: run with: powershell -ExecutionPolicy Bypass -File .\manual-ctx001.ps1
. (Join-Path $PSScriptRoot 'manual-helpers.ps1')
$owner = New-LocalAccount 'location.demo'
$other = New-LocalAccount 'location.other'
$path = '/personal-context/location'

$none = Invoke-Api GET $path $null $owner
Check 'M1 skipped -> 200, no selection, NO_SELECTION' ($none.Status -eq 200 -and $null -eq $none.Json.selection -and $none.Json.environment.status -eq 'NO_SELECTION')
$manual = Invoke-Api PATCH $path '{"expectedRevision":null,"selection":{"latitude":16.4637,"longitude":107.5909,"label":"Hue","source":"MANUAL"}}' $owner
Check 'M2 manual city kept although weather is off (rounded to 16.46/107.59)' ($manual.Status -eq 200 -and $manual.Json.revision -eq '1' -and $manual.Json.selection.latitude -eq 16.46 -and $manual.Json.selection.longitude -eq 107.59)
Check 'M3 weather disclosed as UNAVAILABLE / NOT_CONFIGURED, no values' ($manual.Json.environment.status -eq 'UNAVAILABLE' -and $manual.Json.environment.unavailableReason -eq 'NOT_CONFIGURED' -and $null -eq $manual.Json.environment.weather)
$stale = Invoke-Api PATCH $path '{"expectedRevision":null,"selection":{"latitude":21.03,"longitude":105.85,"label":"Ha Noi","source":"MANUAL"}}' $owner
Check 'M4 second first-selection -> 409 REVISION_CONFLICT' ($stale.Status -eq 409 -and $stale.Json.code -eq 'REVISION_CONFLICT')
$intruder = Invoke-Api GET $path $null $other
Check 'M5 another account sees nothing' ($null -eq $intruder.Json.selection)
$cleared = Invoke-Api PATCH $path '{"expectedRevision":"1","selection":null}' $owner
Check 'M6 clear -> revision 2, NO_SELECTION' ($cleared.Json.revision -eq '2' -and $null -eq $cleared.Json.selection -and $cleared.Json.environment.status -eq 'NO_SELECTION')
```

**M6a. Privacy check.** Search the backend log of the run for `16.46`, `107.59`, `16.4637` and `Hue`. Expect no occurrence. The adapter logs only the reason category (OPEN-S4-24).

**M7. Real provider request: NOT DONE, needs authorization.**

- **Run it only after** the team has reviewed the Open-Meteo terms of use, attribution and eligibility for the actual deployment, and authorized a request.
- **How.**
  1. Restart the backend without the variable (`Remove-Item Env:CAPSULEAI_PERSONALCONTEXT_WEATHER_ENABLED`).
  2. Repeat M2 with a fresh account.
- **Expect:**
  - `environment.status` is `CURRENT`;
  - `weather.observedAt` is at most 30 minutes before `weather.retrievedAt`;
  - `basis` is `MODELLED`;
  - `attribution` is present.
- **Record:** the date, the environment, and whether the real response matched the documented shape that `OpenMeteoResponseTranslatorTest` uses.
- **If it does not match**, the response is reported as `INVALID_PROVIDER_DATA`. Update P24 and T3 from the real shape. Never relax validation to make a test pass.

**M8. Deadline measurement in the agreed environment: NOT DONE.**

- **How.** Point `base-url` at a deliberately slow local endpoint, for example a JDK `HttpServer` that never answers (the pattern of T4) on `http://127.0.0.1:<port>`. Time 20 `GET` requests with `Measure-Command`.
- **Expect:** each request finishes within about 2 seconds plus request overhead, with `UNAVAILABLE` / `DEADLINE_EXCEEDED`.
- **Also check** that the backend thread count returns to its baseline afterwards (`jcmd <pid> Thread.print`, threads named `weather-acquisition` at most `maximum-concurrent-requests`).
- **Record** the measured distribution. OPEN-S4-22 stays `VERIFICATION PENDING` until then.

**Results (run on 2026-11-23).**

- **Setup.**
  - A throwaway stack: its own `postgres:17.11-bookworm` container and the built jar, with generated credentials. It did not use the developer's `.env`.
  - Phase 1 ran the `local` profile with no weather switch set, so P34 kept the provider off.
  - Phase 2 pointed the provider at a loopback server that never answers. No Open-Meteo request was made.
- **M1–M6.** All passed, with these extra checks:
  - the stored point has two decimals;
  - no weather evidence is stored while the provider is off;
  - clearing keeps the row with every selection column NULL;
  - a device point is rounded to `21.03`/`105.80`;
  - the same point after rounding is a no-op;
  - invalid values give `400` without echoing them, and `1e400` gives `400`, not `500`;
  - no token, or a logged-out session, gives `401` and changes nothing;
  - Flyway records V14.
- **M6a.** Neither log contains a submitted coordinate or label. The phase 2 log names only the reason category, in 41 lines.
- **M7.** Not done. The Open-Meteo terms, attribution and eligibility are not reviewed, and no request was authorized.
- **M8, measured locally on the developer machine, not the agreed environment.**

| Measurement | Result |
| --- | --- |
| First selection | Accepted; `UNAVAILABLE` / `DEADLINE_EXCEEDED` in 2,064 ms |
| 20 sequential reads | 2,009 / 2,021 / 2,026 ms (min / median / max). All 20 acquisitions reached the server, so no capacity permit leaked. |
| 20 concurrent reads | 2,014 / 2,026 / 2,029 ms. The capacity wait counted against the same deadline. |
| 5 rounds of 20 concurrent reads | 2,007–2,043 ms each |
| `weather-acquisition` threads | At most 8 throughout |
| JDK client (`HttpClient-*`) threads | 1 before any acquisition, 11–12 under load, 1 again after 75 s idle |

- **Observation.** A caller that obtains a capacity permit just before its own deadline still sends a request, which it abandons within milliseconds. Under contention, therefore, more requests reach the provider than there are permits: 16 in one round of 20 concurrent reads. The concurrency limit of 8 in-flight calls holds. Avoiding these requests would need a minimum-remaining-time rule, a new timeout that the story does not select. This is recorded for OPEN-S4-22.

## 15. Acceptance Criteria and Requirements-to-Test Matrices

### 15.1 Acceptance criteria to evidence

| AC | Evidence (run in the repository on 2026-11-23) | Remaining |
| --- | --- | --- |
| AC-01 Permission-independent choices | T1 `aManualSelectionNeedsALabel…`; T12 `aManualCitySelection…`, `deviceCoordinatesAreRounded…`, `anAccountWithoutASelection…` (skip); M1–M2 | Client permission explanation, denial and device-failure flows; manual city search on the client (OPEN-S4-18) |
| AC-02 Actual evidence; a city is not weather | T3 `theDocumentedCurrentResponseIsTranslated`; T12 `aManualCitySelection…` (`observedAt`, `retrievedAt`, `basis`, attribution), `aProviderFailureKeeps…` (no weather values); M3 | Client display. **Real response shape not confirmed (M7)**. |
| AC-03 Freshness and the 2-second delay | T2 (inclusive boundary); T10 `currentEvidenceIsReused…`, `aNewlyFetchedButStaleReading…`, `evidenceFarInTheFuture…`, `staleStoredEvidenceWhoseRefreshFails…`; T12 `evidenceExactlyThirtyMinutesOld…`; T4 (deadline, capacity wait, slow body, released capacity, bounded threads); T15 (deadline ≤ PT2S); M8 measured locally | **The deadline in the agreed environment (M8)** |
| AC-04 Bounded fallback | T4 (all failure outcomes); T10 `aProviderFailureKeeps…`; T12 `aProviderFailureKeeps…`; M3 | "Skip only environmental hard filtering" and "remaining validity still applies": **no outfit filtering or assessment exists yet**. Recorded gap, not a pass. |
| AC-05 Accepted changes | T13 (the four fencing tests plus the real lock races: same revision, a change waiting for a publication, a publication waiting behind a change, other records); T10 `clearingRemoves…`; T12 `clearingRemoves…` | **Reevaluation or outdated marking of affected results: no result store exists (consumers absent, OPEN-S4-23).** Recorded gap. |
| AC-06 Protect accepted context | T10 `aStaleRevision…`, `invalidSelections…`; T12 `aStaleRevision…` (no echo), `noOtherAccount…`; M4–M5 | Client uncertain-outcome review |
| AC-07 Minimize external exchange | T4 `aValidResponseIsAcquiredAndOnlyTheLocationIsSent`, `aRedirectIsNotFollowed`, `aFailureIsLoggedWithoutTheLocationOrTheUrl`; T12 `deviceCoordinatesAreRounded…`, `coordinatesAndLabelsNeverReachTheLog`; M6a | Provider terms review (M7) |

### 15.2 Requirements to tests

| Requirement | Tests |
| --- | --- |
| FR-WEATHER-001/002, HW-003 | T1, T10, T12 (manual and device, skip) |
| FR-WEATHER-003, BR-013 | T2, T4, T10, T12, T15; M8 |
| FR-WEATHER-004, ERR-WEATHER-001 | T3, T4, T10, T12 |
| FR-WEATHER-005 | T13 (late results never current); consumer reevaluation absent |
| SI-002 | T4 (only the location), T12 (rounded point); M6a |
| BRULE-PROF-003 | T10, T12, T13 |
| BRULE-OUT-006 | T12 (`UNAVAILABLE`, no invented values); outfit consumer absent |
| BRULE-AUTH-001/007 | T12 `noOtherAccountOrSessionCanReadOrChangeIt` |
| S4-CTX-02 separate lifecycle | T11 upgrade test, T12, T14 |

## 16. Definition of Done

- [x] P1–P35 and T1–T15 applied (2026-11-23; the repository files equal this guide's steps). `./mvnw -o clean verify` passed in the repository on 2026-11-23: 694 unit and 434 integration tests, none failed, errored or was skipped, including `PreferencesApiIT`, `OptionalContextApiIT`, `PreferencesSchemaIT`, `DatabaseSchemaOwnershipIT`, `PersistenceOwnershipSqlTest`, `ArchitectureDependencyTest` and `IdentityOpenApiContractIT`.
- [ ] CI on a PR to `sprint_04` finds 132 required reports with no failure or skip. (Locally 131 of 132; `HeifDecodingIT` needs libheif, which CI installs.)
- [ ] **The Open-Meteo terms, attribution and eligibility for the deployment are reviewed.** Either M7 is recorded, or production runs with `enabled=false` and reports `NOT_CONFIGURED`.
- [ ] **M8 deadline measurement recorded.** OPEN-S4-22 moves from `VERIFICATION PENDING` only with that evidence. (Measured locally on 2026-11-23, §14; the agreed environment is still outstanding.)
- [x] The AC-04 and AC-05 consumer gaps are recorded as outstanding (§15.1) and not reported as passing.
- [ ] Client evidence: permission explanation, grant, deny, device failure, manual city, skip, clear, uncertain outcome, Vietnamese and accessibility.

## 17. Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| Integration contexts fail with `NoUniqueBeanDefinitionException` for `WeatherProvider` | T6 is missing `@Primary` | Mark `controllableWeatherProvider()` `@Primary` |
| An integration test waits about 2 seconds, or reaches the internet | T7 not applied, so the real adapter is active | Add `capsuleai.personal-context.weather.enabled=false` to `application-test.properties` |
| Startup fails: "deadline must be positive and at most PT2S" | Deadline configured above the approved limit | Keep `PT2S` or lower; the limit is a product decision, not a tuning knob |
| Startup fails: "base URL must use https" | `http://` used for a non-loopback host | Use the `https` origin without a path |
| Every request reports `INVALID_PROVIDER_DATA` | The real response differs from the documented shape (units, `current_units`, grid distance) | Compare a recorded real response with T3 (M7). Update P24 and T3 together. |
| `OpenMeteoWeatherProviderTest` deadline tests are flaky on a slow CI runner | Scheduling delays beyond the 500 ms test tolerance | Investigate the runner first. Raise the test tolerance only with evidence; never raise the production deadline. |
| `PreferencesSchemaIT` lists two tables too many | T14 not applied | Add the two V14 tables in alphabetical order |
| `TRUNCATE` in Personal Context tests fails after V14 | T8 not applied | Add both V14 tables to the statement |
| `SELECTION_CHANGED` reported on an ordinary view | Two tabs changed the selection concurrently | Expected. Reload; the next view acquires weather for the current selection. |
| Contract IT reports a missing required field for `LocationContext` | A response component was renamed without OpenAPI | Keep P26 and P32 in step; every component is required and nullable where documented |

## 18. Final Checklist

- [x] V14 touches only `personal_context`. The coordinate column scale equals the rounding precision (2 decimals).
- [x] The selection has its own revision. Preferences and optional context are never read or written by the location services.
- [x] Rounding happens before storage and before the provider. No raw device precision is kept or logged.
- [x] Commit first, acquire after, publish only for the same revision under `FOR SHARE`. A change or clear deletes evidence in the same transaction.
- [x] Freshness is measured from the provider's evidence time; the 30-minute boundary is inclusive; a stale new reading is never current or stored.
- [x] One elapsed deadline covers capacity wait, connection, headers, body and translation; there is no retry.
- [x] Every failure is an explicit `UNAVAILABLE` reason. No invented weather.
- [x] No coordinates, labels or URLs in logs. Integration tests never call the internet.
- [x] OpenAPI 0.16.0; 9 CI entries (total 132).
- [x] Open-Meteo terms, M7 and M8 are listed as outstanding until done.
