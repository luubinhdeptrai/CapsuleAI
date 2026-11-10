# US-PROF-001 — Set and Revise Style Preferences and Common Needs: Implementation Guide

| Field | Value |
| --- | --- |
| Story | [US-PROF-001](../../../stories/profile/US-PROF-001-set-and-revise-style-and-common-needs.md) (Draft v0.2), parent PBI-010 |
| Sprint | Sprint 3, story 3 of 3. **Independently preparable**: it shares no code with the two garment stories, only the cumulative files listed in §11. |
| Starting point | After US-GAR-002 and US-GAR-003 are applied (recommended order). For independent preparation from `9d158fb`, follow the merge notes in §11 and [shared architecture §21](shared-architecture.md#21-shared-file-ownership-and-cumulative-order). |
| Approved decisions applied | S3-PROF-01, S3-PROF-02, S3-PROF-03, S3-PROF-04, S3-PROF-05; S3-INT-01, S3-INT-02, S3-INT-03 |
| Approved detailed choices used | All **approved by the Product Owner on 2026-11-11** ([decision record](open-questions.md)): OPEN-S3-01 (compatible API evolution, shared error vocabulary), OPEN-S3-04 (opaque `revision` string, `expectedRevision` in the body, `409 REVISION_CONFLICT`), OPEN-S3-05 (`application/json` presence rules), OPEN-S3-06 (a no-op keeps the revision), OPEN-S3-15 (`/personal-context/preferences`, six need codes, `null` = unset, priorities 0–3, `revision: null` before the first declaration), OPEN-S3-16 (minimal `personalcontext.api.CurrentPersonalContext` now, no consumer) |
| Verification status | **Not compiled, not run, not tested.** Approval selects the direction; the tests in §13 must still be written and pass, including the architecture tests for the published contract. |

## 1. Story Overview

**Story.** As an authenticated user, I want to set and revise my styles and common clothing needs, so that advice and Coverage use my declared priorities.

**Backend outcome.** The first real capability of the `personalcontext` module:

- **Read:** `GET /api/v1/personal-context/preferences` returns the account's declared profile styles and the priority of each of the six common needs. A user who has declared nothing gets `revision: null`, no styles and every need `null` (absent context, not a persona).
- **Revise:** `PATCH /api/v1/personal-context/preferences` changes styles and needs in a presence-aware way. A need is set to `0`–`3`, cleared to unset, or left untouched. An explicit `0` (Not Relevant) stays `0`.
  - **Guarded creation:** the first declaration uses `expectedRevision: null`, and only one can win.
  - **Later revisions:** each uses the current revision.
- **Persistence:** in the new `personal_context` schema (V11), owned exclusively by the module.
- **Published contract:** a minimal `personalcontext.api.CurrentPersonalContext` for future advice and Coverage consumers. No consumer is built.

**Out of scope:**

- US-PROF-002 (body and gender);
- location and weather (US-CTX-001);
- the current request occasion (stored nowhere and never written);
- public or creator profiles;
- ranking and Coverage computation;
- measurement and analytics;
- onboarding screens (client).

## 2. Repository Baseline

| Item | State | Relevance |
| --- | --- | --- |
| `com.capsuleai.personalcontext` | Only `package-info.java` (owner of declared preferences, needs, location and context meaning; schema `personal_context`; `api`-only access) | All new code goes here |
| `BusinessModule.PERSONAL_CONTEXT` | Registered with package `personalcontext` and schema `personal_context` | Architecture and SQL-ownership tests already recognize the owner |
| Schema `personal_context` | Does not exist ("created only by the migration of the story that first needs it") | V11 creates it |
| Migrations | V1–V10 after the garment stories; V1–V8 from `9d158fb` | Next free version: V11 in the recommended order |
| Security | One filter chain; CORS includes `PATCH` once US-GAR-002 is applied | If US-GAR-002 is not applied, add `PATCH` here |
| Contract test | Checks every `/api` handler | The two new operations must be documented and match their records |
| OpenAPI | 0.10.0 after the garment stories | → 0.11.0 |

## 3. Original Business Requirements — Verbatim

From [BRD](../../../../01-business/BRD.md):

| ID | Requirement | Rationale | Priority | Goal |
| --- | --- | --- | --- | --- |
| BR-010 | Declared style preferences and selected occasion must actively influence initial advice and relevant wardrobe analysis. | Serve the user's actual clothing needs. | Must | BG-02, BG-03, BG-04 |
| BR-012 | Users must be able to omit or modify body-shape and gender information without restrictive garment eligibility rules. | Provide inclusive assistance and preserve individual choice. | Must | BG-02, BG-03, BG-04 |

BR-012 is traced by the story, but body and gender belong to US-PROF-002; nothing about them is implemented here.

| Capability | Name | Description |
| --- | --- | --- |
| CAP-01 | Account & Personalization | Maintain a private personal wardrobe context, style preferences, and optional profile information. |
| CAP-06 | Behavioral Personalization | Use explicit feedback and recorded wear choices to improve relevance and diversity. |

## 4. Original Product Requirements — Verbatim

From [PRD](../../../../02-product/PRD.md) §8.1 JRN-01:

> **Intent:** Establish private access and relevant everyday needs through progressive onboarding.
>
> 1. **Welcome / Product Value Introduction:** Explain wardrobe intelligence, user authority, and the connected product value.
> 2. **Register or Login:** …
> 3. **Style Preferences:** Select from the initial controlled style vocabulary in Section 11.3.
> 4. **Common Occasion Needs:** Set relevant common needs/priorities for the Personalized Everyday Capsule. These ongoing needs are distinct from the current occasion of an outfit request.
> 5. **Optional Personal Context:** Offer gender/body profile with omission, later edit, and removal.
> 6. **Location:** …
> 7. **Recommended Add First Garment:** Explain its value and offer the action without making it an access blocker or requiring an arbitrary garment quota.
> 8. **Enter Today / Wardrobe:** Open the product; an empty wardrobe explains limited recommendations and offers Add Garment. Users can return later to complete skipped optional information.
>
> **Completion:** The authenticated user can access Today/Wardrobe without body/gender, device-location permission, or a first garment. Incomplete optional setup is distinct from failed authentication.

From PRD §11.1 FEAT-PROF-001:

> **Product behavior:**
>
> 1. Explain and allow selection/revision of the controlled style preferences and common occasion need priorities in Section 11.3.
> 2. Use one Personalized Everyday Capsule informed by common needs, style, climate/context, and confirmed wardrobe. Distinguish these long-term/common priorities from the current occasion selected for an individual recommendation.
> 3. Offer body-profile and gender information as optional; allow omission, later edits, and removal.
> 4. Apply explicit preferences to relevant ranking and wardrobe analysis; body-profile context may softly refine ranking.
> 5. Explain that optional personal attributes do not prohibit garment categories or override the user's confirmed wardrobe and explicit preferences.
>
> **User controls:** Select/change preferences, choose the current occasion, skip optional context, edit/remove optional information. **Product states:** Unset, partially configured, configured, editing, update failed.
>
> **Failure / fallback:** Without complete context, disclose which assumptions or limited context advice uses. A failed change is not shown as saved.

From PRD §11.3:

> | Vocabulary | Initial Canonical Values |
> |---|---|
> | Style | `MINIMAL`, `CASUAL`, `SMART_CASUAL`, `CLASSIC`, `STREETWEAR`, `SPORTY`, `FORMAL` |
> | Current/common occasion vocabulary | `EVERYDAY`, `WORK`, `SCHOOL_UNIVERSITY`, `DATE`, `SOCIAL_EVENT`, `FORMAL_EVENT`, `TRAVEL`, `SPORT_ACTIVITY` |
>
> The **Personalized Everyday Capsule** is one model, not separate Student, Professional, Minimalist, or Fashion capsule profiles. Its initial need dimensions are: Everyday / Casual; Work / Professional; School / University; Formal / Special Event; Travel; Sport / Active.
>
> Users may mark some needs as more relevant than others. Common need priorities are persistent assessment context; the current occasion is the immediate context of a recommendation request. The six need dimensions and eight occasion terms are related concepts with distinct purposes, not interchangeable lists. Exact mappings, weights, and compatibility semantics belong in Business Rules/SRS/design; no universal wardrobe checklist is implied.

## 5. Original Software Requirements — Verbatim

From [SRS](../../../../03-requirements/SRS.md):

| ID | Requirement | Verification |
| --- | --- | --- |
| FR-PROF-001 | The system shall provide the progressive sequence Welcome → Register/Login → Style Preferences → Common Occasion Needs → Optional Personal Context → Location → recommended First Garment → Today/Wardrobe. | D: first-use journey. |
| FR-PROF-002 | The system shall allow users to select and revise style preferences from the controlled vocabulary in Section 4.10. | T: every defined style choice. |
| FR-PROF-003 | The system shall allow users to revise common capsule need priorities using Not Relevant=0, Low=1, Medium=2 and High=3 and the need-to-occasion mapping in Section 3.14.1. | T: every priority and need mapping; differing request/common occasion. |
| FR-PROF-004 | The system shall keep the current recommendation occasion distinct from ongoing common needs so a request occasion does not automatically overwrite those priorities. | T: differing current/common context. |
| FR-PROF-008 | The system shall allow skipped optional setup to be completed later and make first-garment creation a recommended action rather than an access or garment-quota gate. | D: skip and return. |
| FR-AUTH-011 | The system shall allow authenticated product entry without body/gender, device-location permission, first garment, or Display Name. | T: omitted optional information. |
| DATA-AUTH-003 | The system shall retain the user's current declared styles/common priorities and distinguish them from request-specific occasion. | T/I: independent context values. |
| UI-002 | The system shall present the progressive onboarding flow with optional context and non-blocking first-garment guidance. | D: onboarding skip/return. |
| FR-ANL-014 | The system shall reevaluate or mark coverage outdated after relevant wardrobe, needs, context or applicable validity-rule changes. | T: changed priority, garment attributes and applicable rule version. |

SRS §3.14.1 (excerpt):

> | Capsule Need | Relevant Occasion Codes |
> |---|---|
> | Everyday / Casual | EVERYDAY, DATE, SOCIAL_EVENT |
> | Work / Professional | WORK |
> | School / University | SCHOOL_UNIVERSITY |
> | Formal / Special Event | FORMAL_EVENT |
> | Travel | TRAVEL |
> | Sport / Active | SPORT_ACTIVITY |
>
> | Need Priority | Weight |
> |---|---|
> | Not Relevant | 0 |
> | Low | 1 |
> | Medium | 2 |
> | High | 3 |

SRS §4.10 (excerpt):

> Profile Style preferences and Occasion/request context are separate from garment Style; they do not add garment properties:
>
> | Context Vocabulary | Existing Values |
> |---|---|
> | Profile Style preference | `MINIMAL`, `CASUAL`, `SMART_CASUAL`, `CLASSIC`, `STREETWEAR`, `SPORTY`, `FORMAL` |
> | Request Occasion | `EVERYDAY`, `WORK`, `SCHOOL_UNIVERSITY`, `DATE`, `SOCIAL_EVENT`, `FORMAL_EVENT`, `TRAVEL`, `SPORT_ACTIVITY` |
> | Capsule Need Dimensions | Everyday / Casual; Work / Professional; School / University; Formal / Special Event; Travel; Sport / Active. |

## 6. Original Business Rules — Verbatim

> **BRULE-PROF-002 — Declared Preferences and Request Context.** Style and occasion use the controlled vocabularies in BRULE-GAR-004. Common capsule need priorities remain distinct from the current request occasion; a request must not automatically overwrite ongoing priorities. The need scale is Not Relevant=0, Low=1, Medium=2, High=3, as used by BRULE-COV-002.

> **BRULE-GAR-004** (excerpt). … The garment-property authority owns garment values. Profile style preferences, request Occasion and common needs keep their separate SRS Section 4.10 vocabularies; they are not garment Style or a garment Occasion property.

> **BRULE-COV-001 — Personalized Everyday Capsule Need Mapping** (excerpt). Coverage evaluates one Personalized Everyday Capsule using relevant common needs, declared style, available climate/context, and the current confirmed eligible wardrobe. The need-to-occasion mapping is … (the SRS §3.14.1 table above).

> **BRULE-COV-002 — Need Priority Weights.** Use exactly these priority weights: Not Relevant 0, Low 1, Medium 2, High 3.

> **BRULE-COV-006 — Relevant Changes Invalidate Current Coverage.** Relevant changes to current wardrobe/confirmed attributes, common needs/priorities, assessment context, or applicable validity rules require reevaluation or a clear outdated indication. Removed garments are excluded from the next current calculation. Prior scores must not silently remain current after failed refresh.

## 7. Original User Story and Acceptance Criteria — Verbatim

> As an authenticated user, I want to set and revise my styles and common clothing needs, so that advice and Coverage use my declared priorities.

> **Scope.** Initial progressive preference setup and later revision; controlled styles, six common needs, priority weights, separate current occasion and source-supported accepted/canceled/error outcomes.
>
> **PR #4 scope boundary — 2026-11-09:** This story remains private declared style/common-needs setup and revision. It neither defines a public creator profile nor publishes preferences through Community. MVP-Q06 governs the separate creator projection; no Community, Chat or Try-On implementation enters this candidate.

> **AC-01 — Set or revisit preferences.** Show current or unset styles/common needs and support submission of permitted choices in onboarding or later profile revision. Unset data is absent context, not an inferred persona or mandatory wardrobe checklist.

> **AC-02 — Use canonical choices.** Accept the controlled style vocabulary and six need dimensions/mapping in SRS Sections 4.10/3.14.1, with Not Relevant=0, Low=1, Medium=2 and High=3. Unsupported values are explained for correction without saving invented replacements.

> **AC-03 — Separate common and current context.** A current outfit request occasion may differ from ongoing needs and does not automatically overwrite their priorities; accepted common-priority changes remain available to subsequent contextual Coverage.

> **AC-04 — Preserve progressive entry.** Follow the source progression from account entry through style/common needs, optional personal context, location and recommended first garment to Today/Wardrobe. Omitted optional context or no first garment does not block authenticated entry; later completion is available.

> **AC-05 — Reflect accepted changes.** Accepted styles/priorities become the user's current context for relevant later ranking and assessment. Affected prior assessments are refreshed or marked outdated; Not Relevant needs are not silently replaced by positive defaults.

> **AC-06 — Cancel or recover safely.** Leaving an unsubmitted revision or known rejection/failure keeps the last accepted profile. An uncertain update is not called saved; the user can review actual context or retry, and unusable/wrong-account access cannot change the profile.

Story §8: "Optional body/gender lifecycle is US-PROF-002; actual location/weather acquisition is US-CTX-001. Ranking and Coverage computation remain their owning stories. No new persona capsules or category restrictions are added."

## 8. Original Use Cases, Flows and Architecture Decisions — Verbatim

From [UC-005](../../../../03-requirements/use-cases/UC-005-update-personalization-profile.md) §6 (the body/gender parts belong to US-PROF-002):

> 1. The User requests personalization setup or revision.
> 2. CapsuleAI presents current or unset style choices, common need priorities and optional body/gender information using the controlled vocabularies.
> 3. The User selects or revises relevant styles and common need priorities, and chooses whether to provide optional personal information.
> 4. CapsuleAI makes the selected information and its meaning understandable, including the distinction between ongoing needs and a current outfit request occasion.
> 5. The User submits the intended profile changes.
> 6. CapsuleAI acknowledges accepted changes, displays the current profile and uses it in subsequent relevant ranking/assessments; affected assessments are reevaluated or identified as outdated where required.

> **A1 — Initial or partial setup.** At Main Step 2: 1. CapsuleAI shows unset information as absent rather than an inferred personal profile. 2. The User supplies only the information they choose and resumes at Main Step 3.
>
> **A4 — Change common priorities.** At Main Step 3: 1. The User selects Not Relevant, Low, Medium or High for the applicable capsule needs. 2. Accepted priorities affect contextual Coverage/gaps without overwriting a separately selected current recommendation occasion.
>
> **A5 — Cancel an unsubmitted revision.** At Main Step 5: 1. The User leaves without submitting the proposed change. 2. The last accepted profile remains current.
>
> **E1 — Unsupported or unusable profile values.** At Main Step 5: 1. CapsuleAI explains the values that cannot be accepted under the controlled vocabularies/priorities. 2. The User corrects the applicable choices and resumes at Main Step 3; no invented replacement profile is saved.
>
> **E2 — Profile update failed or uncertain.** At Main Step 6: 1. CapsuleAI distinguishes rejected/failed changes from accepted updates, retaining prior accepted context on known failure. 2. The User reviews the outcome or retries the intended revision; an uncertain outcome is not reported as saved.
>
> **E3 — Authorized access becomes unusable.** At Main Step 1: 1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again. 2. … Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

UC-005 A2 (skip optional context) and A3 (remove body/gender) concern US-PROF-002. No dedicated Activity Diagram exists for this slice.

Architecture:

- ADA-015–018: module-owned persistence, owner transaction ports, published contracts and private models.
- AR-01–08: the `personal_context` schema, created by the first persisting story.
- Logical, Implementation and Data Views: Personal Context owns declared context.

The approved decisions are quoted in full in [plan §9.3](../sprint-plan.md#93-us-prof-001-decisions):

| ID | Recommended solution (verbatim) | Status |
| --- | --- | --- |
| S3-PROF-01 | Option A — `com.capsuleai.personalcontext`, owning `personal_context` state. | APPROVED |
| S3-PROF-02 | Option A — preserve absent context, canonical choices and explicit-zero semantics. | APPROVED |
| S3-PROF-03 | Option B — apply the presence-aware revision convention from S3-GAR-01 to this owner's separate profile resource. | APPROVED |
| S3-PROF-04 | Option B — reuse the optimistic revision and reread conventions of S3-GAR-02/04, including a guarded first creation. | APPROVED |
| S3-PROF-05 | Option A — a narrow `personalcontext.api` current-context contract when needed by the selected implementation, designed for later owner-authorized consumption. | APPROVED |

The cross-story decisions S3-INT-01 (compatible API evolution with one error, revision and recovery convention), S3-INT-02 (isolated integration evidence) and S3-INT-03 (story guides with shared-file coordination) are approved as Option A ([plan §9.4](../sprint-plan.md#94-cross-story-decisions)).

## 9. Engineering Interpretation

| # | Interpretation | Source |
| --- | --- | --- |
| I-1 | Need codes are `EVERYDAY_CASUAL`, `WORK_PROFESSIONAL`, `SCHOOL_UNIVERSITY`, `FORMAL_SPECIAL_EVENT`, `TRAVEL`, `SPORT_ACTIVE`. Each carries its SRS §3.14.1 occasion codes as data for future consumers; nothing computes with them now. | OPEN-S3-15, SRS §3.14.1 |
| I-2 | Priorities are JSON integers `0`–`3`, stored as `SMALLINT` with a CHECK. An absent need has **no row**. `0` has a row with priority `0`. | S3-PROF-02 |
| I-3 | Styles are a set of the seven profile codes. Garment Style codes (`STREET`, `MINIMALIST`, …) are rejected even where a name looks similar. `[]` is a valid declaration of "no particular style" and round-trips as `[]`; unset and empty are the same state (OPEN-S3-15). | S3-PROF-02, BRULE-GAR-004 |
| I-4 | PATCH semantics: `styles` absent → keep; `null` or `[]` → clear; array → replace. `commonNeeds` absent → keep all; object → per need: absent → keep, `null` → unset, integer → set. `commonNeeds: null` is rejected (no mass clearing). | S3-PROF-03, OPEN-S3-05 |
| I-5 | `expectedRevision` is required. `null` means "I saw no profile" (guarded create); a string token means "the revision I read". Mismatch either way → `409 REVISION_CONFLICT`. | S3-PROF-04, OPEN-S3-04 |
| I-6 | Submitting is the confirmation (UC-005 step 5); there is no separate `confirmed` flag. Cancel = not submitting (A5). | AC-06 |
| I-7 | A revision whose result equals the current declaration writes nothing and keeps the revision. A first declaration that declares nothing creates nothing (`revision` stays `null`). | OPEN-S3-06 |
| I-8 | The current request occasion is not part of this resource and is never written by anything in this story. | AC-03, FR-PROF-004 |
| I-9 | AC-05 "affected prior assessments are refreshed or marked outdated": no assessments exist yet. The published contract exposes `revision` so future consumers can detect a changed basis (S3-PROF-05). | AC-05, FR-ANL-014 |
| I-10 | AC-04 progressive entry: nothing in Identity or Wardrobe depends on preferences, so entry, browsing and garment creation work with an unset profile. A regression test proves it. | AC-04, FR-AUTH-011 |

## 10. Just-in-Time Detailed Design

### 10.1 Component and sequence

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant W as PersonalContextPreferencesController
    participant M as PreferencesRevisionRequest (JsonNode → changes)
    participant S as RevisePreferencesService
    participant R as JdbcPreferenceProfileRepository
    participant DB as PostgreSQL (personal_context)
    C->>W: PATCH /api/v1/personal-context/preferences
    W->>M: toCommand(owner)
    M-->>S: RevisePreferencesCommand
    S->>S: shape checks (expectedRevision, styles, commonNeeds) → 400
    S->>R: find(owner)
    S->>S: precondition (null ⇔ no profile; token ⇔ revision) → 409
    S->>S: apply + validate → 400
    alt result == current (or nothing declared)
        S-->>W: unchanged
    else no profile yet
        S->>R: tx: INSERT … ON CONFLICT DO NOTHING (+ children)
        R-->>S: false → 409
    else profile exists
        S->>R: tx: UPDATE … WHERE revision = :expected (+ replace children)
        R-->>S: false → 409
    end
    W-->>C: 200 Preferences
```

### 10.2 API contract

`GET /api/v1/personal-context/preferences` → `200 Preferences`:

```json
{
  "revision": null,
  "styles": [],
  "commonNeeds": {
    "EVERYDAY_CASUAL": null, "WORK_PROFESSIONAL": null, "SCHOOL_UNIVERSITY": null,
    "FORMAL_SPECIAL_EVENT": null, "TRAVEL": null, "SPORT_ACTIVE": null
  },
  "updatedAt": null
}
```

`PATCH /api/v1/personal-context/preferences`, body `PreferencesRevisionRequest`:

```json
{
  "expectedRevision": null,
  "styles": ["MINIMAL", "SMART_CASUAL"],
  "commonNeeds": { "WORK_PROFESSIONAL": 3, "SPORT_ACTIVE": 0 }
}
```

Response: `200 Preferences`, for example:

- `revision: "1"`;
- `styles` in catalogue order;
- `WORK_PROFESSIONAL: 3`, `SPORT_ACTIVE: 0`, the others `null`.

| Status | `code` | When |
| --- | --- | --- |
| 400 | `VALIDATION_FAILED` | `expectedRevision` missing (`REQUIRED`) or malformed (`INVALID_FORMAT`); unsupported style (`styles[i]`), unknown need (`commonNeeds`), bad priority (`commonNeeds.<NEED>`), wrong JSON kinds (`styles`, `commonNeeds`) |
| 400 | `MALFORMED_REQUEST` | Not JSON |
| 401 | `AUTHENTICATION_REQUIRED` | No usable session |
| 409 | `REVISION_CONFLICT` | Precondition failed (stale token, or a profile already exists for `null`) |
| 503 | `TEMPORARILY_UNAVAILABLE` | Database unavailable; outcome not confirmed |

### 10.3 SQL

V11 (complete in Step P1) creates schema `personal_context` and three tables:

- `personal_context_preferences` (owner PK, revision, timestamps);
- `personal_context_preferred_style` (owner, style);
- `personal_context_common_need` (owner, need, priority 0–3).

The child tables reference the parent within the schema, with `ON DELETE CASCADE`. There is no foreign key to Identity.

Guarded first creation:

```sql
INSERT INTO personal_context.personal_context_preferences (owner_account_id, revision, created_at, updated_at)
VALUES (:owner, 1, :now, :now)
ON CONFLICT (owner_account_id) DO NOTHING
```

Revision:

```sql
UPDATE personal_context.personal_context_preferences
SET revision = revision + 1, updated_at = :now
WHERE owner_account_id = :owner AND revision = :expectedRevision
```

then delete and re-insert both child sets.

### 10.4 Transactions and concurrency

- **Transaction port.** `PersonalContextTransactions` mirrors `WardrobeTransactions`. Identity's and Wardrobe's ports are private to their modules and are not borrowed.
- **Concurrent first creation.** Two first creations race on the primary key, and exactly one insert succeeds. The other returns 0 rows and gets `409`.
- **Concurrent revisions.** Two revisions race exactly as garment edits do ([shared architecture §8](shared-architecture.md#8-revision-and-optimistic-concurrency)).
- **Atomicity.** Child-set replacement happens after the winning statement, in the same transaction; any failure rolls back everything.

### 10.5 Security and privacy

- **Owner-only resource.** The resource is always "my preferences"; the owner comes from `@AuthenticationPrincipal`, and no id appears in the path.
- **Error bodies.** They never echo submitted values. An unknown need key is reported as the field `commonNeeds`, not with the submitted key.
- **Purpose limitation.** No Community or creator projection reads this data (PR #4 boundary). Nothing is used for training.

### 10.6 Recovery

| Situation | Client action |
| --- | --- |
| `400` | Keep the draft; correct the named fields |
| `409` | `GET` the preferences, show the current values, let the user re-submit with the new `revision` |
| `503` or lost response | `GET` the preferences. If they match the intent, treat the change as accepted. Otherwise retry with the **original** `expectedRevision` (a committed change makes the retry `409`, never a double application). |

### 10.7 Configuration

None.

### 10.8 Testing plan

| Level | Class | Focus |
| --- | --- | --- |
| Domain | `DeclaredPreferencesTest` | Vocabulary, need mapping, priorities, absent versus zero, immutability |
| Application | `RevisePreferencesServiceTest` | Presence semantics, zero, clearing, validation, precondition, guarded create, no-op, write-time conflict |
| Web mapping | `PreferencesRevisionRequestTest` | `JsonNode` presence mapping |
| Schema | `PreferencesSchemaIT` | V11 placement and constraints |
| API | `PreferencesApiIT` | End-to-end behaviour, privacy, conflicts, garment-style separation, progressive entry |
| Concurrency | `ConcurrentPreferencesIT` | Competing first creations and revisions, rollback |
| Published contract | `CurrentPersonalContextIT` | Absent and current semantics through `personalcontext.api` |
| Architecture | `DatabaseSchemaOwnershipIT` (modified) | The `personal_context` tables live in their schema |

## 11. File Change Matrix

`…/pc` = `backend/src/main/java/com/capsuleai/personalcontext`.

| Order | Action | Exact Repository Path | Purpose | Dependencies |
| --- | --- | --- | --- | --- |
| P1 | ADD | `backend/src/main/resources/db/migration/V11__create_personal_context_preferences.sql` | First Personal Context schema and tables (renumber to the next free version if needed) | V10 (or V8/V9 if prepared earlier) |
| P2 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/domain/ProfileStyle.java` | Seven styles | — |
| P3 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/domain/CommonNeed.java` | Six needs + occasion mapping | — |
| P4 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/domain/NeedPriority.java` | 0–3 | — |
| P5 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/domain/DeclaredPreferences.java` | Styles + declared needs | P2–P4 |
| P6 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/domain/PreferenceProfile.java` | Owner, preferences, revision | P5 |
| P7 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/domain/PreferenceValueProblem.java` | Field error codes | — |
| P8 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/ValueChange.java` | Keep/Clear/Replace/Malformed | — |
| P9 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/ExpectedRevision.java` | Missing/Unset/At/Malformed | — |
| P10 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/PreferenceChanges.java` | Requested changes | P8 |
| P11 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/RevisePreferencesCommand.java` | Command | P9, P10 |
| P12 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/PreferenceFieldViolation.java` | Field error | P7 |
| P13 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/PreferencesValidationException.java` | `400` | P12 |
| P14 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/PreferencesRevisionConflictException.java` | `409` | — |
| P15 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/PreferencesRevision.java` | Outcome | P6 |
| P16 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/ports/PreferenceProfileRepository.java` | Port | P6 |
| P17 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/ports/PersonalContextTransactions.java` | Port | — |
| P18 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/ViewPreferencesService.java` | Read | P16 |
| P19 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/RevisePreferencesService.java` | Revise | P8–P17 |
| P20 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/api/DeclaredPersonalContext.java` | Published value | — |
| P21 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/api/CurrentPersonalContext.java` | Published query | P20 |
| P22 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/application/CurrentPersonalContextService.java` | Implements P21 | P16, P21 |
| P23 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/persistence/JdbcPreferenceProfileRepository.java` | SQL | P1, P16 |
| P24 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/persistence/SpringPersonalContextTransactions.java` | `TransactionTemplate` | P17 |
| P25 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesRevisionRequest.java` | PATCH body | P10, P11 |
| P26 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesResponse.java` | JSON | P6 |
| P27 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PersonalContextPreferencesController.java` | Routes | P18, P19, P25, P26 |
| P28 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PersonalContextApiExceptionHandler.java` | Problem Details | P13, P14 |
| P29 | ADD | `backend/src/main/java/com/capsuleai/personalcontext/configuration/PersonalContextConfiguration.java` | Wiring | P18–P24 |
| P30 | MODIFY | `backend/src/main/java/com/capsuleai/personalcontext/package-info.java` | Note the persisted state | P1 |
| P31 | MODIFY | `docs/05-api/openapi.yaml` | 0.11.0 | P25–P28 |
| P32 | MODIFY | `.github/workflows/backend-ci.yml` | Reports | T-steps |
| P33 | MODIFY | `backend/README.md` | Preferences section | — |
| T1 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/domain/DeclaredPreferencesTest.java` | Domain | P2–P5 |
| T2 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/application/InMemoryPreferenceProfileRepository.java` | Fake | P16 |
| T3 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/application/ImmediatePersonalContextTransactions.java` | Fake | P17 |
| T4 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/application/RevisePreferencesServiceTest.java` | Application | P19 |
| T5 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesRevisionRequestTest.java` | Mapping | P25 |
| T6 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/support/PersonalContextIntegrationTestSupport.java` | IT base | P1 |
| T7 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/adapter/out/persistence/PreferencesSchemaIT.java` | Schema | P1 |
| T8 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesApiIT.java` | API | P27–P29 |
| T9 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/application/ConcurrentPreferencesIT.java` | Races | P19, P23 |
| T10 | ADD | `backend/src/test/java/com/capsuleai/personalcontext/application/CurrentPersonalContextIT.java` | Published contract | P22 |
| T11 | MODIFY | `backend/src/test/java/com/capsuleai/architecture/DatabaseSchemaOwnershipIT.java` | Assert the new tables | P1 |

**Independent-preparation merge notes:**

- **CORS.** If US-GAR-002 is not applied, also apply US-GAR-002 P20 (CORS `PATCH`) and T9 (`CorsSecurityIT`).
- **OpenAPI.** If US-GAR-002 is not applied, the `RevisionConflict` response is defined in this story's P31; otherwise it already exists.
- **CI.** If US-GAR-002 is not applied, add `sprint_03` to the PR trigger (US-GAR-002 P22, change 1).
- **Migration.** Use the next free migration version and the next OpenAPI minor version.

## 12. Complete Production Implementation

### Step P1 — [ADD] `V11__create_personal_context_preferences.sql`

**Path:** `backend/src/main/resources/db/migration/V11__create_personal_context_preferences.sql`

```sql
-- US-PROF-001 (S3-PROF-01/02/04): the user's declared profile styles and common-need priorities.
-- An owner without a row has declared nothing yet (absent context, not a persona). A need without a
-- row is unset; a need with priority 0 is explicitly Not Relevant (BRULE-PROF-002, BRULE-COV-002).
-- Profile styles are their own vocabulary (SRS 4.10), never garment Style values.
--
-- Owner: Personal Context. This first Personal Context migration creates the module's own schema
-- (AR-01); every object below is in it. owner_account_id is the authenticated Identity account id,
-- with no foreign key into Identity (ADA-016, AR-04).
CREATE SCHEMA IF NOT EXISTS personal_context;

CREATE TABLE personal_context.personal_context_preferences (
    owner_account_id UUID        PRIMARY KEY,
    revision         BIGINT      NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_personal_context_preferences_revision CHECK (revision >= 1),
    CONSTRAINT ck_personal_context_preferences_updated_after_created CHECK (updated_at >= created_at)
);

CREATE TABLE personal_context.personal_context_preferred_style (
    owner_account_id UUID NOT NULL
        REFERENCES personal_context.personal_context_preferences (owner_account_id) ON DELETE CASCADE,
    style            TEXT NOT NULL,

    CONSTRAINT pk_personal_context_preferred_style PRIMARY KEY (owner_account_id, style),
    CONSTRAINT ck_personal_context_preferred_style CHECK (style IN (
        'MINIMAL', 'CASUAL', 'SMART_CASUAL', 'CLASSIC', 'STREETWEAR', 'SPORTY', 'FORMAL'))
);

CREATE TABLE personal_context.personal_context_common_need (
    owner_account_id UUID     NOT NULL
        REFERENCES personal_context.personal_context_preferences (owner_account_id) ON DELETE CASCADE,
    need             TEXT     NOT NULL,
    priority         SMALLINT NOT NULL,

    CONSTRAINT pk_personal_context_common_need PRIMARY KEY (owner_account_id, need),
    CONSTRAINT ck_personal_context_common_need CHECK (need IN (
        'EVERYDAY_CASUAL', 'WORK_PROFESSIONAL', 'SCHOOL_UNIVERSITY', 'FORMAL_SPECIAL_EVENT', 'TRAVEL',
        'SPORT_ACTIVE')),
    -- Not Relevant = 0, Low = 1, Medium = 2, High = 3 (SRS 3.14.1).
    CONSTRAINT ck_personal_context_common_need_priority CHECK (priority BETWEEN 0 AND 3)
);
```

**Database implications.**

- **First Personal Context migration.** It changes only `personal_context`, as `PersistenceOwnershipSqlTest` requires. `DatabaseSchemaOwnershipIT` already allows the schema, because `BusinessModule` registers it.
- **Version number.** Use the next free version. From `9d158fb` without the garment stories, that is V9.

**Common mistakes.**

- Do not put these tables in `identity`, `wardrobe` or `public`.
- Do not add a foreign key to `identity.identity_account`.
- Do not default a missing need to `0`.

### Step P2 — [ADD] `ProfileStyle.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/domain/ProfileStyle.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Profile style preferences (SRS 4.10, PRD 11.3, FR-PROF-002). A separate vocabulary from garment
 * Style (BRULE-GAR-004): similar-looking garment codes such as STREET or MINIMALIST are not values here.
 * The constant names are stored and published.
 */
public enum ProfileStyle {
    MINIMAL,
    CASUAL,
    SMART_CASUAL,
    CLASSIC,
    STREETWEAR,
    SPORTY,
    FORMAL;

    /** The value whose code equals {@code code} exactly; no case folding, trimming or fallback. */
    public static Optional<ProfileStyle> fromCode(String code) {
        return Arrays.stream(values()).filter(style -> style.name().equals(code)).findFirst();
    }
}
```

### Step P3 — [ADD] `CommonNeed.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/domain/CommonNeed.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The six common-need dimensions of the Personalized Everyday Capsule (SRS 3.14.1/4.10, BRULE-COV-001),
 * each with the request-occasion codes it covers. Codes are defined for the API by OPEN-S3-15. The
 * mapping is data for future Coverage consumers; nothing in this story computes with it, and no request
 * occasion ever overwrites a need (FR-PROF-004).
 */
public enum CommonNeed {
    EVERYDAY_CASUAL("EVERYDAY", "DATE", "SOCIAL_EVENT"),
    WORK_PROFESSIONAL("WORK"),
    SCHOOL_UNIVERSITY("SCHOOL_UNIVERSITY"),
    FORMAL_SPECIAL_EVENT("FORMAL_EVENT"),
    TRAVEL("TRAVEL"),
    SPORT_ACTIVE("SPORT_ACTIVITY");

    private final List<String> relevantOccasionCodes;

    CommonNeed(String... relevantOccasionCodes) {
        this.relevantOccasionCodes = List.of(relevantOccasionCodes);
    }

    /** The SRS 3.14.1 request-occasion codes this need covers. */
    public List<String> relevantOccasionCodes() {
        return relevantOccasionCodes;
    }

    public static Optional<CommonNeed> fromCode(String code) {
        return Arrays.stream(values()).filter(need -> need.name().equals(code)).findFirst();
    }
}
```

### Step P4 — [ADD] `NeedPriority.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/domain/NeedPriority.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.Optional;

/** Common-need priority and its weight (FR-PROF-003, BRULE-COV-002): exactly 0, 1, 2 and 3. */
public enum NeedPriority {
    NOT_RELEVANT(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    NeedPriority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    /** The priority with this weight, or empty for anything outside 0–3 (never clamped). */
    public static Optional<NeedPriority> fromWeight(int weight) {
        return Arrays.stream(values()).filter(priority -> priority.weight == weight).findFirst();
    }
}
```

### Step P5 — [ADD] `DeclaredPreferences.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/domain/DeclaredPreferences.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * What the user has declared (DATA-AUTH-003, S3-PROF-02): a set of profile styles and a priority for
 * each declared common need. A need that is not in {@code commonNeeds} is unset (absent context); a need
 * mapped to {@link NeedPriority#NOT_RELEVANT} is explicitly not relevant. Nothing is inferred or defaulted.
 */
public record DeclaredPreferences(Set<ProfileStyle> styles, Map<CommonNeed, NeedPriority> commonNeeds) {

    public DeclaredPreferences {
        Objects.requireNonNull(styles, "styles must be empty when none are declared");
        Objects.requireNonNull(commonNeeds, "commonNeeds must be empty when none are declared");
        styles = Collections.unmodifiableSet(copyOf(styles));
        EnumMap<CommonNeed, NeedPriority> needs = new EnumMap<>(CommonNeed.class);
        commonNeeds.forEach((need, priority) -> needs.put(Objects.requireNonNull(need, "need"),
                Objects.requireNonNull(priority, "a declared need has a priority; an unset need is absent")));
        commonNeeds = Collections.unmodifiableMap(needs);
    }

    /** Nothing declared. */
    public static DeclaredPreferences none() {
        return new DeclaredPreferences(Set.of(), Map.of());
    }

    public boolean declaresNothing() {
        return styles.isEmpty() && commonNeeds.isEmpty();
    }

    /** The declared priority of a need, or empty if it is unset. */
    public Optional<NeedPriority> priorityOf(CommonNeed need) {
        return Optional.ofNullable(commonNeeds.get(need));
    }

    private static Set<ProfileStyle> copyOf(Collection<ProfileStyle> styles) {
        EnumSet<ProfileStyle> copy = EnumSet.noneOf(ProfileStyle.class);
        copy.addAll(styles);
        return copy;
    }
}
```

### Step P6 — [ADD] `PreferenceProfile.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/domain/PreferenceProfile.java`

```java
package com.capsuleai.personalcontext.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One account's accepted declaration (US-PROF-001). One per owner (S3-PROF-04). {@code revision} is
 * concurrency metadata (1 at the first declaration, one more per accepted change), not a preference.
 */
public record PreferenceProfile(
        UUID ownerAccountId,
        DeclaredPreferences preferences,
        long revision,
        Instant createdAt,
        Instant updatedAt) {

    public static final long FIRST_REVISION = 1;

    public PreferenceProfile {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(preferences, "preferences");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A preference revision starts at " + FIRST_REVISION);
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A profile cannot be updated before it was created");
        }
    }

    public static PreferenceProfile firstDeclaration(UUID ownerAccountId, DeclaredPreferences preferences,
                                                     Instant declaredAt) {
        return new PreferenceProfile(ownerAccountId, preferences, FIRST_REVISION, declaredAt, declaredAt);
    }

    /** The accepted change: one more revision; the update time never moves before the creation time. */
    public PreferenceProfile revisedTo(DeclaredPreferences revised, Instant revisedAt) {
        Instant updated = revisedAt.isBefore(createdAt) ? createdAt : revisedAt;
        return new PreferenceProfile(ownerAccountId, revised, Math.addExact(revision, 1), createdAt, updated);
    }
}
```

### Step P7 — [ADD] `PreferenceValueProblem.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/domain/PreferenceValueProblem.java`

```java
package com.capsuleai.personalcontext.domain;

/** Why a submitted preference value was rejected. Constant names are published field-error codes. */
public enum PreferenceValueProblem {

    /** expectedRevision is missing. */
    REQUIRED,

    /** expectedRevision is not null or a revision token. */
    INVALID_FORMAT,

    /** Not a profile style, not a common need, not a priority 0–3, or a value of the wrong JSON kind. */
    UNSUPPORTED_VALUE
}
```

### Step P8 — [ADD] `ValueChange.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/ValueChange.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.Objects;

/**
 * One presence-aware change (S3-PROF-03, OPEN-S3-05): keep, clear, replace, or a value of the wrong
 * kind to report. Personal Context's own type; it deliberately does not reuse Wardrobe's (AR-03).
 */
public sealed interface ValueChange<T> {

    record Keep<T>() implements ValueChange<T> {
    }

    record Clear<T>() implements ValueChange<T> {
    }

    record Replace<T>(T value) implements ValueChange<T> {

        public Replace {
            Objects.requireNonNull(value, "value");
        }
    }

    record Malformed<T>() implements ValueChange<T> {
    }

    static <T> ValueChange<T> keep() {
        return new Keep<>();
    }

    static <T> ValueChange<T> clear() {
        return new Clear<>();
    }

    static <T> ValueChange<T> replace(T value) {
        return new Replace<>(value);
    }

    static <T> ValueChange<T> malformed() {
        return new Malformed<>();
    }
}
```

### Step P9 — [ADD] `ExpectedRevision.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/ExpectedRevision.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.regex.Pattern;

/**
 * The precondition of a preference change (S3-PROF-04, OPEN-S3-04): missing (an error), unset ("I saw
 * no declaration yet": guarded first creation), a revision token, or malformed.
 */
public sealed interface ExpectedRevision {

    record Missing() implements ExpectedRevision {
    }

    record Unset() implements ExpectedRevision {
    }

    record At(long revision) implements ExpectedRevision {
    }

    record Malformed() implements ExpectedRevision {
    }

    /** A positive decimal that fits in a long; clients treat it as opaque. */
    Pattern TOKEN = Pattern.compile("[1-9][0-9]{0,17}");

    static ExpectedRevision ofToken(String token) {
        return token != null && TOKEN.matcher(token).matches() ? new At(Long.parseLong(token)) : new Malformed();
    }
}
```

### Step P10 — [ADD] `PreferenceChanges.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/PreferenceChanges.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The requested change (S3-PROF-03): the style selection as a whole, and each mentioned need on its own
 * (keys are submitted need codes, validated by the service). {@code commonNeedsMalformed} is true when
 * {@code commonNeeds} was present but not an object, including null: needs are cleared one at a time,
 * never all at once.
 */
public record PreferenceChanges(
        ValueChange<List<String>> styles,
        Map<String, ValueChange<Integer>> commonNeeds,
        boolean commonNeedsMalformed) {

    public PreferenceChanges {
        Objects.requireNonNull(styles, "styles");
        Objects.requireNonNull(commonNeeds, "commonNeeds");
        commonNeeds = Collections.unmodifiableMap(new LinkedHashMap<>(commonNeeds));
    }

    public static PreferenceChanges none() {
        return new PreferenceChanges(ValueChange.keep(), Map.of(), false);
    }
}
```

### Step P11 — [ADD] `RevisePreferencesCommand.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/RevisePreferencesCommand.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.UUID;

/** One submitted preference change. The owner comes from the authenticated session, never from the request. */
public record RevisePreferencesCommand(UUID ownerAccountId, ExpectedRevision expectedRevision,
                                       PreferenceChanges changes) {
}
```

### Step P12 — [ADD] `PreferenceFieldViolation.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/PreferenceFieldViolation.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.PreferenceValueProblem;

/**
 * One rejected input, named as in the request: {@code expectedRevision}, {@code styles},
 * {@code styles[1]}, {@code commonNeeds} or {@code commonNeeds.TRAVEL}. A submitted unknown need key is
 * reported as {@code commonNeeds}, so no submitted text is echoed.
 */
public record PreferenceFieldViolation(String field, PreferenceValueProblem problem) {
}
```

### Step P13 — [ADD] `PreferencesValidationException.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/PreferencesValidationException.java`

```java
package com.capsuleai.personalcontext.application;

import java.util.List;

/** The change was rejected and nothing was stored (UC-005 E1); every problem is listed. */
public final class PreferencesValidationException extends RuntimeException {

    private final List<PreferenceFieldViolation> violations;

    public PreferencesValidationException(List<PreferenceFieldViolation> violations) {
        super("Preference change rejected with " + violations.size() + " field violation(s)");
        this.violations = List.copyOf(violations);
    }

    public List<PreferenceFieldViolation> violations() {
        return violations;
    }
}
```

### Step P14 — [ADD] `PreferencesRevisionConflictException.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/PreferencesRevisionConflictException.java`

```java
package com.capsuleai.personalcontext.application;

/**
 * The declaration changed (or was first created) after the user read it (S3-PROF-04). Nothing changed;
 * the client rereads and lets the user review. It never resubmits automatically.
 */
public final class PreferencesRevisionConflictException extends RuntimeException {

    public PreferencesRevisionConflictException() {
        super("The preferences changed after they were read; the change was not applied");
    }
}
```

### Step P15 — [ADD] `PreferencesRevision.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/PreferencesRevision.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of an accepted change: the declaration as it now is (empty if the owner still has declared
 * nothing) and whether this change wrote anything (OPEN-S3-06).
 */
public record PreferencesRevision(Optional<PreferenceProfile> profile, boolean changed) {

    public PreferencesRevision {
        Objects.requireNonNull(profile, "profile");
    }
}
```

### Step P16 — [ADD] `PreferenceProfileRepository.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/ports/PreferenceProfileRepository.java`

```java
package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Personal Context-owned persistence of declared preferences (S3-PROF-01, ADA-015/016). */
public interface PreferenceProfileRepository {

    /** The owner's declaration, or empty if they have declared nothing yet. One consistent snapshot. */
    Optional<PreferenceProfile> find(UUID ownerAccountId);

    /**
     * Stores the owner's first declaration unless one exists (guarded creation, S3-PROF-04). Atomic: of
     * concurrent first declarations exactly one returns {@code true}. Call inside {@link PersonalContextTransactions}.
     */
    boolean createIfAbsent(PreferenceProfile profile);

    /**
     * Replaces the declared styles and needs only while the revision is still {@code expectedRevision};
     * the revision increases by one. Atomic under concurrency. Call inside {@link PersonalContextTransactions}.
     *
     * @return {@code false} if the revision changed or there is no declaration (nothing changed)
     */
    boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, DeclaredPreferences preferences,
                              Instant updatedAt);
}
```

### Step P17 — [ADD] `PersonalContextTransactions.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/ports/PersonalContextTransactions.java`

```java
package com.capsuleai.personalcontext.application.ports;

import java.util.function.Supplier;

/**
 * Runs Personal Context work in one database transaction: everything commits together or nothing does.
 * The module owns this port; it does not borrow another module's private transaction port (ADA-015).
 */
public interface PersonalContextTransactions {

    <T> T inTransaction(Supplier<T> work);
}
```

### Step P18 — [ADD] `ViewPreferencesService.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/ViewPreferencesService.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** US-PROF-001 AC-01 — the owner's current or unset declaration. Read-only. */
public final class ViewPreferencesService {

    private final PreferenceProfileRepository profiles;

    public ViewPreferencesService(PreferenceProfileRepository profiles) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
    }

    public Optional<PreferenceProfile> view(UUID ownerAccountId) {
        return profiles.find(Objects.requireNonNull(ownerAccountId, "ownerAccountId"));
    }
}
```

### Step P19 — [ADD] `RevisePreferencesService.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/RevisePreferencesService.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * US-PROF-001 — accepts one submitted change of the owner's styles and common needs (FR-PROF-002/003,
 * BRULE-PROF-002; S3-PROF-02/03/04).
 *
 * <p>Presence-aware: untouched styles and needs keep their values; a cleared need becomes unset; an
 * explicit 0 stays Not Relevant. The change is accepted only against the declaration the user read:
 * {@code expectedRevision} null for the first declaration (guarded: one wins), the current revision
 * afterwards, both enforced atomically in the database. A stale change is refused, never merged.
 */
public final class RevisePreferencesService {

    static final String EXPECTED_REVISION_FIELD = "expectedRevision";
    static final String STYLES_FIELD = "styles";
    static final String COMMON_NEEDS_FIELD = "commonNeeds";

    private final PreferenceProfileRepository profiles;
    private final PersonalContextTransactions transactions;
    private final Clock clock;

    public RevisePreferencesService(PreferenceProfileRepository profiles, PersonalContextTransactions transactions,
                                    Clock clock) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws PreferencesValidationException       if the request or a value is not acceptable (nothing changed)
     * @throws PreferencesRevisionConflictException if the declaration changed after the user read it
     */
    public PreferencesRevision revise(RevisePreferencesCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        PreferenceChanges changes = Objects.requireNonNull(command.changes(), "changes");
        checkShape(command.expectedRevision(), changes);

        Optional<PreferenceProfile> current = profiles.find(owner);
        if (!preconditionHolds(command.expectedRevision(), current)) {
            throw new PreferencesRevisionConflictException();
        }
        DeclaredPreferences base = current.map(PreferenceProfile::preferences).orElse(DeclaredPreferences.none());
        DeclaredPreferences resulting = apply(base, changes);
        if (resulting.equals(base)) {
            return new PreferencesRevision(current, false);
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);
        if (current.isEmpty()) {
            PreferenceProfile created = PreferenceProfile.firstDeclaration(owner, resulting, now);
            if (!transactions.inTransaction(() -> profiles.createIfAbsent(created))) {
                throw new PreferencesRevisionConflictException();
            }
            return new PreferencesRevision(Optional.of(created), true);
        }
        PreferenceProfile existing = current.get();
        PreferenceProfile revised = existing.revisedTo(resulting, now);
        if (!transactions.inTransaction(() -> profiles.reviseIfCurrentAt(owner, existing.revision(), resulting,
                revised.updatedAt()))) {
            throw new PreferencesRevisionConflictException();
        }
        return new PreferencesRevision(Optional.of(revised), true);
    }

    /** Problems visible without reading anything. */
    private static void checkShape(ExpectedRevision expectedRevision, PreferenceChanges changes) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        if (expectedRevision == null || expectedRevision instanceof ExpectedRevision.Missing) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.REQUIRED));
        } else if (expectedRevision instanceof ExpectedRevision.Malformed) {
            violations.add(new PreferenceFieldViolation(EXPECTED_REVISION_FIELD, PreferenceValueProblem.INVALID_FORMAT));
        }
        if (changes.styles() instanceof ValueChange.Malformed<?>) {
            violations.add(new PreferenceFieldViolation(STYLES_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (changes.commonNeedsMalformed()) {
            violations.add(new PreferenceFieldViolation(COMMON_NEEDS_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new PreferencesValidationException(violations);
        }
    }

    private static boolean preconditionHolds(ExpectedRevision expected, Optional<PreferenceProfile> current) {
        if (expected instanceof ExpectedRevision.Unset) {
            return current.isEmpty();
        }
        if (expected instanceof ExpectedRevision.At at) {
            return current.isPresent() && current.get().revision() == at.revision();
        }
        throw new IllegalStateException("A missing or malformed precondition was already rejected");
    }

    private static DeclaredPreferences apply(DeclaredPreferences base, PreferenceChanges changes) {
        List<PreferenceFieldViolation> violations = new ArrayList<>();
        Set<ProfileStyle> styles = styles(base.styles(), changes.styles(), violations);
        Map<CommonNeed, NeedPriority> needs = new EnumMap<>(CommonNeed.class);
        needs.putAll(base.commonNeeds());
        boolean unknownNeed = false;
        for (Map.Entry<String, ValueChange<Integer>> entry : changes.commonNeeds().entrySet()) {
            Optional<CommonNeed> need = CommonNeed.fromCode(entry.getKey());
            if (need.isEmpty()) {
                unknownNeed = true;
                continue;
            }
            String field = COMMON_NEEDS_FIELD + "." + need.get().name();
            ValueChange<Integer> change = entry.getValue();
            if (change instanceof ValueChange.Clear<?>) {
                needs.remove(need.get());
            } else if (change instanceof ValueChange.Replace<Integer> replace) {
                Optional<NeedPriority> priority = NeedPriority.fromWeight(replace.value());
                if (priority.isPresent()) {
                    needs.put(need.get(), priority.get());
                } else {
                    violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
                }
            } else if (change instanceof ValueChange.Malformed<?>) {
                violations.add(new PreferenceFieldViolation(field, PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
        }
        if (unknownNeed) {
            violations.add(new PreferenceFieldViolation(COMMON_NEEDS_FIELD, PreferenceValueProblem.UNSUPPORTED_VALUE));
        }
        if (!violations.isEmpty()) {
            throw new PreferencesValidationException(violations);
        }
        return new DeclaredPreferences(styles, needs);
    }

    private static Set<ProfileStyle> styles(Set<ProfileStyle> current, ValueChange<List<String>> change,
                                            List<PreferenceFieldViolation> violations) {
        if (change instanceof ValueChange.Clear<?>) {
            return Set.of();
        }
        if (!(change instanceof ValueChange.Replace<List<String>> replace)) {
            return current;
        }
        Set<ProfileStyle> styles = EnumSet.noneOf(ProfileStyle.class);
        List<String> codes = replace.value();
        for (int index = 0; index < codes.size(); index++) {
            Optional<ProfileStyle> style = ProfileStyle.fromCode(codes.get(index));
            if (style.isPresent()) {
                styles.add(style.get());
            } else {
                violations.add(new PreferenceFieldViolation(STYLES_FIELD + "[" + index + "]",
                        PreferenceValueProblem.UNSUPPORTED_VALUE));
            }
        }
        return styles;
    }
}
```

**Explanation.**

- **Order of checks.** The shape checks run first: the contract test's required-field case then gets `400 REQUIRED` with nothing read.
- **Precondition before validation.** The precondition is checked before value validation, so a stale request is answered `409` even if its values would be invalid.
- **No-op.** If nothing changes, nothing is written (OPEN-S3-06). That includes an empty first declaration: `revision` stays `null`.

**Common mistakes.**

- Do not treat `null` as `0`.
- Do not clear all needs when `commonNeeds` is omitted.
- Do not accept garment Style codes.

### Step P20 — [ADD] `DeclaredPersonalContext.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/api/DeclaredPersonalContext.java`

```java
package com.capsuleai.personalcontext.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Published language (S3-PROF-05, ADA-018): an account's currently declared context as codes. Only
 * declared needs are present; an unset need is absent and a Not Relevant need has priority 0.
 * {@code revision} lets a future consumer detect that the basis of an earlier assessment changed
 * (FR-ANL-014); it does not by itself refresh anything.
 */
public record DeclaredPersonalContext(String revision, Set<String> styleCodes, Map<String, Integer> needPriorities) {

    public DeclaredPersonalContext {
        Objects.requireNonNull(revision, "revision");
        styleCodes = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(styleCodes, "styleCodes")));
        needPriorities = Collections.unmodifiableMap(
                new LinkedHashMap<>(Objects.requireNonNull(needPriorities, "needPriorities")));
    }
}
```

### Step P21 — [ADD] `CurrentPersonalContext.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/api/CurrentPersonalContext.java`

```java
package com.capsuleai.personalcontext.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Published Personal Context query (S3-PROF-05, OPEN-S3-16): the account's current declared styles and
 * common-need priorities, read at request time from the owner, never from a copy. Future advice and
 * Coverage modules use this instead of Personal Context tables (AR-03). No consumer exists in Sprint 3.
 */
public interface CurrentPersonalContext {

    /** @return the current declaration, or empty if the account has declared nothing (absent context, not a persona) */
    Optional<DeclaredPersonalContext> find(UUID accountId);
}
```

### Step P22 — [ADD] `CurrentPersonalContextService.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/application/CurrentPersonalContextService.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.api.DeclaredPersonalContext;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Implements the published {@link CurrentPersonalContext} from the owner's own persistence. Read-only. */
public final class CurrentPersonalContextService implements CurrentPersonalContext {

    private final PreferenceProfileRepository profiles;

    public CurrentPersonalContextService(PreferenceProfileRepository profiles) {
        this.profiles = Objects.requireNonNull(profiles, "profiles");
    }

    @Override
    public Optional<DeclaredPersonalContext> find(UUID accountId) {
        return profiles.find(Objects.requireNonNull(accountId, "accountId")).map(CurrentPersonalContextService::publish);
    }

    private static DeclaredPersonalContext publish(PreferenceProfile profile) {
        Set<String> styles = new LinkedHashSet<>();
        profile.preferences().styles().forEach(style -> styles.add(style.name()));
        Map<String, Integer> needs = new LinkedHashMap<>();
        profile.preferences().commonNeeds().forEach((need, priority) -> needs.put(need.name(), priority.weight()));
        return new DeclaredPersonalContext(Long.toString(profile.revision()), styles, needs);
    }
}
```

**Explanation.** `application` implements the `api` interface, as `identity` already does with `SessionAuthority`. `ArchitectureDependencyTest` forbids only the reverse direction (`api` → `domain`/`application`).

### Step P23 — [ADD] `JdbcPreferenceProfileRepository.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/persistence/JdbcPreferenceProfileRepository.java`

```java
package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link PreferenceProfileRepository} (V11, schema personal_context). */
public final class JdbcPreferenceProfileRepository implements PreferenceProfileRepository {

    /** The declaration with its styles and needs, read in one statement for a consistent snapshot. */
    static final String SELECT_PROFILE = """
            SELECT p.owner_account_id, p.revision, p.created_at, p.updated_at,
                   ARRAY(SELECT s.style FROM personal_context.personal_context_preferred_style s
                         WHERE s.owner_account_id = p.owner_account_id) AS styles,
                   ARRAY(SELECT n.need || ':' || n.priority FROM personal_context.personal_context_common_need n
                         WHERE n.owner_account_id = p.owner_account_id) AS needs
            FROM personal_context.personal_context_preferences p
            WHERE p.owner_account_id = :ownerAccountId
            """;

    private final JdbcClient jdbc;

    public JdbcPreferenceProfileRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<PreferenceProfile> find(UUID ownerAccountId) {
        return jdbc.sql(SELECT_PROFILE)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> mapProfile(row))
                .optional();
    }

    @Override
    public boolean createIfAbsent(PreferenceProfile profile) {
        int inserted = jdbc.sql("""
                        INSERT INTO personal_context.personal_context_preferences (
                            owner_account_id, revision, created_at, updated_at)
                        VALUES (:ownerAccountId, :revision, :createdAt, :updatedAt)
                        ON CONFLICT (owner_account_id) DO NOTHING
                        """)
                .param("ownerAccountId", profile.ownerAccountId())
                .param("revision", profile.revision())
                .param("createdAt", toDatabase(profile.createdAt()))
                .param("updatedAt", toDatabase(profile.updatedAt()))
                .update();
        if (inserted == 0) {
            return false;
        }
        insertDeclarations(profile.ownerAccountId(), profile.preferences());
        return true;
    }

    @Override
    public boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, DeclaredPreferences preferences,
                                     Instant updatedAt) {
        int updated = jdbc.sql("""
                        UPDATE personal_context.personal_context_preferences
                        SET revision = revision + 1, updated_at = :updatedAt
                        WHERE owner_account_id = :ownerAccountId AND revision = :expectedRevision
                        """)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .param("updatedAt", toDatabase(updatedAt))
                .update();
        if (updated == 0) {
            return false;
        }
        // The UPDATE holds the profile row lock until commit; no other change can interleave.
        jdbc.sql("DELETE FROM personal_context.personal_context_preferred_style WHERE owner_account_id = :owner")
                .param("owner", ownerAccountId)
                .update();
        jdbc.sql("DELETE FROM personal_context.personal_context_common_need WHERE owner_account_id = :owner")
                .param("owner", ownerAccountId)
                .update();
        insertDeclarations(ownerAccountId, preferences);
        return true;
    }

    private void insertDeclarations(UUID ownerAccountId, DeclaredPreferences preferences) {
        for (ProfileStyle style : preferences.styles()) {
            jdbc.sql("""
                            INSERT INTO personal_context.personal_context_preferred_style (owner_account_id, style)
                            VALUES (:owner, :style)
                            """)
                    .param("owner", ownerAccountId)
                    .param("style", style.name())
                    .update();
        }
        for (Map.Entry<CommonNeed, NeedPriority> need : preferences.commonNeeds().entrySet()) {
            jdbc.sql("""
                            INSERT INTO personal_context.personal_context_common_need (owner_account_id, need, priority)
                            VALUES (:owner, :need, :priority)
                            """)
                    .param("owner", ownerAccountId)
                    .param("need", need.getKey().name())
                    .param("priority", (short) need.getValue().weight())
                    .update();
        }
    }

    static PreferenceProfile mapProfile(ResultSet row) throws SQLException {
        Set<ProfileStyle> styles = EnumSet.noneOf(ProfileStyle.class);
        for (String code : (String[]) row.getArray("styles").getArray()) {
            styles.add(ProfileStyle.fromCode(code)
                    .orElseThrow(() -> new IllegalStateException("Stored profile style is unknown")));
        }
        Map<CommonNeed, NeedPriority> needs = new EnumMap<>(CommonNeed.class);
        for (String entry : (String[]) row.getArray("needs").getArray()) {
            String[] parts = entry.split(":", 2);
            needs.put(CommonNeed.fromCode(parts[0])
                            .orElseThrow(() -> new IllegalStateException("Stored common need is unknown")),
                    NeedPriority.fromWeight(Integer.parseInt(parts[1]))
                            .orElseThrow(() -> new IllegalStateException("Stored need priority is unknown")));
        }
        return new PreferenceProfile(
                row.getObject("owner_account_id", UUID.class),
                new DeclaredPreferences(styles, needs),
                row.getLong("revision"),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
```

**Explanation.**

- **Single read.** `need || ':' || priority` returns each need with its priority in one array, so the whole declaration is one statement.
- **Ownership.** Every table is `personal_context.`-qualified (AR-03).

### Step P24 — [ADD] `SpringPersonalContextTransactions.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/adapter/out/persistence/SpringPersonalContextTransactions.java`

```java
package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** {@link PersonalContextTransactions} on Spring's {@link TransactionTemplate} (PostgreSQL READ COMMITTED). */
public final class SpringPersonalContextTransactions implements PersonalContextTransactions {

    private final TransactionTemplate transactionTemplate;

    public SpringPersonalContextTransactions(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
```

### Step P25 — [ADD] `PreferencesRevisionRequest.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesRevisionRequest.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ExpectedRevision;
import com.capsuleai.personalcontext.application.PreferenceChanges;
import com.capsuleai.personalcontext.application.RevisePreferencesCommand;
import com.capsuleai.personalcontext.application.ValueChange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * JSON body of PATCH /api/v1/personal-context/preferences (US-PROF-001). Each component is a raw
 * {@link JsonNode} so that omitted (Jackson leaves it null), JSON null (a NullNode) and a value can be told
 * apart (S3-PROF-03, OPEN-S3-05). There is no owner field.
 */
public record PreferencesRevisionRequest(JsonNode expectedRevision, JsonNode styles, JsonNode commonNeeds) {

    RevisePreferencesCommand toCommand(UUID ownerAccountId) {
        return new RevisePreferencesCommand(ownerAccountId, expectedRevision(expectedRevision),
                new PreferenceChanges(styles(styles), needs(commonNeeds),
                        commonNeeds != null && !commonNeeds.isObject()));
    }

    /** Omitted: missing (an error). null: no declaration seen yet. A string: a revision token. */
    static ExpectedRevision expectedRevision(JsonNode node) {
        if (node == null) {
            return new ExpectedRevision.Missing();
        }
        if (node.isNull()) {
            return new ExpectedRevision.Unset();
        }
        return node.isString() ? ExpectedRevision.ofToken(node.stringValue()) : new ExpectedRevision.Malformed();
    }

    /** Omitted keeps; null clears; an array replaces (non-string entries become null and are reported). */
    static ValueChange<List<String>> styles(JsonNode node) {
        if (node == null) {
            return ValueChange.keep();
        }
        if (node.isNull()) {
            return ValueChange.clear();
        }
        if (!node.isArray()) {
            return ValueChange.malformed();
        }
        List<String> codes = new ArrayList<>();
        for (JsonNode element : node) {
            codes.add(element.isString() ? element.stringValue() : null);
        }
        return ValueChange.replace(Collections.unmodifiableList(codes));
    }

    /** Each mentioned need: null clears it, an integer sets it, anything else is malformed. */
    static Map<String, ValueChange<Integer>> needs(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        Map<String, ValueChange<Integer>> needs = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            JsonNode value = entry.getValue();
            if (value.isNull()) {
                needs.put(entry.getKey(), ValueChange.clear());
            } else if (value.isIntegralNumber() && value.canConvertToInt()) {
                needs.put(entry.getKey(), ValueChange.replace(value.intValue()));
            } else {
                needs.put(entry.getKey(), ValueChange.malformed());
            }
        }
        return needs;
    }
}
```

**Explanation.**

- **Integer check.** `isIntegralNumber()` refuses `2.0`, `"2"` and booleans. `canConvertToInt()` refuses very large integers; anything outside 0–3 is then rejected by `NeedPriority.fromWeight`.
- **Jackson methods.** The record uses Jackson 3.1.5's `JsonNode.properties()`, `isString()` and `stringValue()`. These were checked against the jar on this machine.

### Step P26 — [ADD] `PreferencesResponse.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesResponse.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The account's declaration (OPEN-S3-15). Every field is always present. {@code revision} and
 * {@code updatedAt} are null while nothing has been declared. {@code commonNeeds} lists all six needs in
 * catalogue order: null means unset, 0 means Not Relevant, 1–3 Low/Medium/High. {@code styles} is in
 * catalogue order; [] means no declared style.
 */
public record PreferencesResponse(String revision, List<String> styles, Map<String, Integer> commonNeeds,
                                  String updatedAt) {

    static PreferencesResponse from(Optional<PreferenceProfile> profile) {
        Map<String, Integer> needs = new LinkedHashMap<>();
        for (CommonNeed need : CommonNeed.values()) {
            needs.put(need.name(), profile.flatMap(declared -> declared.preferences().priorityOf(need))
                    .map(NeedPriority::weight).orElse(null));
        }
        return new PreferencesResponse(
                profile.map(declared -> Long.toString(declared.revision())).orElse(null),
                profile.map(declared -> declared.preferences().styles().stream().map(Enum::name).toList())
                        .orElse(List.of()),
                Collections.unmodifiableMap(needs),
                profile.map(declared -> declared.updatedAt().toString()).orElse(null));
    }
}
```

### Step P27 — [ADD] `PersonalContextPreferencesController.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PersonalContextPreferencesController.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.personalcontext.application.RevisePreferencesService;
import com.capsuleai.personalcontext.application.ViewPreferencesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authenticated user's declared styles and common needs (US-PROF-001). Always "my preferences":
 * the owner is the authenticated account and no request names another (FR-AUTH-008). Protected by the
 * application's default-deny filter chain.
 */
@RestController
@RequestMapping("/api/v1/personal-context/preferences")
public class PersonalContextPreferencesController {

    private final ViewPreferencesService viewPreferencesService;
    private final RevisePreferencesService revisePreferencesService;

    public PersonalContextPreferencesController(ViewPreferencesService viewPreferencesService,
                                                RevisePreferencesService revisePreferencesService) {
        this.viewPreferencesService = viewPreferencesService;
        this.revisePreferencesService = revisePreferencesService;
    }

    /** AC-01: current or unset context; never 404 for a user who has declared nothing. */
    @GetMapping
    public PreferencesResponse viewPreferences(@AuthenticationPrincipal AuthenticatedAccount owner) {
        return PreferencesResponse.from(viewPreferencesService.view(owner.accountId()));
    }

    /** AC-01/02/05/06: one presence-aware change against the revision the user read. */
    @PatchMapping
    public PreferencesResponse revisePreferences(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                 @RequestBody PreferencesRevisionRequest request) {
        return PreferencesResponse.from(revisePreferencesService.revise(request.toCommand(owner.accountId())).profile());
    }
}
```

### Step P28 — [ADD] `PersonalContextApiExceptionHandler.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/adapter/in/web/PersonalContextApiExceptionHandler.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.PreferenceFieldViolation;
import com.capsuleai.personalcontext.application.PreferencesRevisionConflictException;
import com.capsuleai.personalcontext.application.PreferencesValidationException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RFC 9457 problem responses for Personal Context, with the same body shape and shared codes as Identity
 * and Wardrobe (S3-INT-01, OPEN-S3-01). Scoped to this package; no body echoes submitted values.
 */
@RestControllerAdvice(basePackageClasses = PersonalContextApiExceptionHandler.class)
public class PersonalContextApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PersonalContextApiExceptionHandler.class);

    @ExceptionHandler(PreferencesValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(PreferencesValidationException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Some choices are not available. Correct them and submit again; nothing was saved.");
        problem.setProperty("fieldErrors", fieldErrors(exception.violations()));
        return respond(problem);
    }

    @ExceptionHandler(PreferencesRevisionConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(PreferencesRevisionConflictException exception) {
        return respond(problem(HttpStatus.CONFLICT, "REVISION_CONFLICT",
                "Your preferences changed after you opened them, so this change was not applied. Reload them, "
                        + "review the current choices and submit again."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "The request body is missing or is not valid JSON."));
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, TransientDataAccessResourceException.class})
    public ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
        log.error("Personal Context persistence operation failed", exception);
        return outcomeNotConfirmed();
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ProblemDetail> handleTransactionNotStarted(CannotCreateTransactionException exception) {
        if (!(exception.getCause() instanceof SQLException)) {
            throw exception;
        }
        log.error("Personal Context transaction could not be started", exception);
        return outcomeNotConfirmed();
    }

    private static List<Map<String, String>> fieldErrors(List<PreferenceFieldViolation> violations) {
        return violations.stream()
                .map(violation -> {
                    Map<String, String> error = new LinkedHashMap<>();
                    error.put("field", violation.field());
                    error.put("code", violation.problem().name());
                    return error;
                })
                .toList();
    }

    private static ResponseEntity<ProblemDetail> outcomeNotConfirmed() {
        return respond(problem(HttpStatus.SERVICE_UNAVAILABLE, "TEMPORARILY_UNAVAILABLE",
                "Your preferences could not be reached, so the outcome is not confirmed. Reload them before "
                        + "retrying; a change repeated with the same expectedRevision is never applied twice."));
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        return problem;
    }

    private static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
```

### Step P29 — [ADD] `PersonalContextConfiguration.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/configuration/PersonalContextConfiguration.java`

```java
package com.capsuleai.personalcontext.configuration;

import com.capsuleai.personalcontext.adapter.out.persistence.JdbcPreferenceProfileRepository;
import com.capsuleai.personalcontext.adapter.out.persistence.SpringPersonalContextTransactions;
import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.application.CurrentPersonalContextService;
import com.capsuleai.personalcontext.application.RevisePreferencesService;
import com.capsuleai.personalcontext.application.ViewPreferencesService;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the Personal Context module's private implementations and publishes {@link CurrentPersonalContext}.
 * Reuses the application's single {@link Clock} bean.
 */
@Configuration(proxyBeanMethods = false)
public class PersonalContextConfiguration {

    @Bean
    PreferenceProfileRepository preferenceProfileRepository(JdbcClient jdbcClient) {
        return new JdbcPreferenceProfileRepository(jdbcClient);
    }

    @Bean
    PersonalContextTransactions personalContextTransactions(PlatformTransactionManager transactionManager) {
        return new SpringPersonalContextTransactions(transactionManager);
    }

    @Bean
    ViewPreferencesService viewPreferencesService(PreferenceProfileRepository profiles) {
        return new ViewPreferencesService(profiles);
    }

    @Bean
    RevisePreferencesService revisePreferencesService(PreferenceProfileRepository profiles,
                                                      PersonalContextTransactions transactions, Clock clock) {
        return new RevisePreferencesService(profiles, transactions, clock);
    }

    @Bean
    CurrentPersonalContext currentPersonalContext(PreferenceProfileRepository profiles) {
        return new CurrentPersonalContextService(profiles);
    }
}
```

### Step P30 — [MODIFY] `package-info.java`

**Path:** `backend/src/main/java/com/capsuleai/personalcontext/package-info.java`

BEFORE:

```java
 * <p>Persistence: when this module first persists state, its tables go into the
 * PostgreSQL schema {@code personal_context} and are named {@code personal_context_*}.
```

AFTER:

```java
 * <p>Persistence: the module's tables are in the PostgreSQL schema
 * {@code personal_context} (created by the first Personal Context migration,
 * US-PROF-001) and are named {@code personal_context_*}.
```

### Step P31 — [MODIFY] `docs/05-api/openapi.yaml` (0.10.0 → 0.11.0)

Change 1. BEFORE `  version: 0.10.0`, AFTER `  version: 0.11.0`.

Change 2. BEFORE:

```yaml
      - US-GAR-003: Add a photo-supported garment with manual confirmation
```

AFTER:

```yaml
      - US-GAR-003: Add a photo-supported garment with manual confirmation
      - US-PROF-001: Set and revise style preferences and common needs
```

Change 3. BEFORE:

```yaml
    confirmed photo, search, filtering or user profiles.
```

AFTER:

```yaml
    confirmed photo, search, filtering, body or gender
    profiles, location, or public profiles.

    Personal preferences (US-PROF-001) are the account's own
    declared profile styles and common-need priorities. Unset
    needs are null; 0 means Not Relevant. They are never
    derived from garments or from a request occasion.
```

Change 4. BEFORE:

```yaml
  - name: Wardrobe
    description: The authenticated user's own confirmed garments
```

AFTER:

```yaml
  - name: Wardrobe
    description: The authenticated user's own confirmed garments
  - name: PersonalContext
    description: The authenticated user's own declared preferences
```

Change 5: the path, inserted before `components:`. BEFORE (end of the last path from US-GAR-003):

```yaml
        "409":
          $ref: "#/components/responses/PhotoUploadNotUsable"
        "503":
          $ref: "#/components/responses/Unavailable"

components:
```

AFTER:

```yaml
        "409":
          $ref: "#/components/responses/PhotoUploadNotUsable"
        "503":
          $ref: "#/components/responses/Unavailable"

  /api/v1/personal-context/preferences:
    get:
      tags:
        - PersonalContext
      operationId: viewPreferences
      summary: The account's declared styles and common needs
      description: |
        US-PROF-001 AC-01 (FR-PROF-002/003, DATA-AUTH-003). Always
        200: an account that declared nothing has revision null,
        styles [] and every need null. Absent context is not a
        persona and never a default.
      responses:
        "200":
          description: The current declaration.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Preferences"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "503":
          $ref: "#/components/responses/Unavailable"
    patch:
      tags:
        - PersonalContext
      operationId: revisePreferences
      summary: Set or revise the account's styles and common needs
      description: |
        US-PROF-001 (FR-PROF-002/003, BRULE-PROF-002; approved
        S3-PROF-02/03/04 and OPEN-S3-04/05/06/15).

        - styles omitted keeps them; null or [] clears them; an
          array replaces the whole selection (profile style codes
          only, never garment Style codes).
        - commonNeeds omitted keeps every need. In the object, an
          omitted need is kept, null makes it unset, and 0–3 sets
          it (0 = Not Relevant). commonNeeds: null is rejected.
        - expectedRevision is required: null for the first
          declaration (only one can be created), otherwise the
          revision last read. A mismatch is 409 REVISION_CONFLICT
          and changes nothing.

        A change that would change nothing returns 200 with the
        same revision. A request occasion never changes these
        values.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: "#/components/schemas/PreferencesRevisionRequest"
            examples:
              firstDeclaration:
                summary: First declaration
                value:
                  expectedRevision: null
                  styles: ["MINIMAL", "SMART_CASUAL"]
                  commonNeeds:
                    WORK_PROFESSIONAL: 3
                    SPORT_ACTIVE: 0
              revision:
                summary: Raise Travel, unset School, keep everything else
                value:
                  expectedRevision: "1"
                  commonNeeds:
                    TRAVEL: 2
                    SCHOOL_UNIVERSITY: null
      responses:
        "200":
          description: The change was accepted (or changed nothing); the declaration as it now is.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/Preferences"
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

Change 6: schemas. BEFORE:

```yaml
    FieldError:
```

AFTER:

```yaml
    PreferencesRevisionRequest:
      type: object
      description: |
        A presence-aware change of the account's declared
        preferences (US-PROF-001). There is no owner field.
      required:
        - expectedRevision
      properties:
        expectedRevision:
          type: [string, "null"]
          pattern: "^[1-9][0-9]{0,17}$"
          description: null for the first declaration; otherwise the revision last read (opaque).
        styles:
          type: [array, "null"]
          items:
            $ref: "#/components/schemas/ProfileStyleCode"
        commonNeeds:
          type: object
          description: Only the needs to change. null unsets one need; 0–3 sets it.
          additionalProperties:
            type: [integer, "null"]
            minimum: 0
            maximum: 3
          propertyNames:
            $ref: "#/components/schemas/CommonNeedCode"

    Preferences:
      type: object
      description: |
        The account's declaration. Every field is always present.
        revision and updatedAt are null while nothing is declared.
      required:
        - revision
        - styles
        - commonNeeds
        - updatedAt
      properties:
        revision:
          type: [string, "null"]
          pattern: "^[1-9][0-9]*$"
          description: Opaque; send it back as expectedRevision.
        styles:
          type: array
          description: Declared profile styles in catalogue order; [] means none.
          items:
            $ref: "#/components/schemas/ProfileStyleCode"
        commonNeeds:
          type: object
          description: All six needs, always present. null = unset; 0 Not Relevant, 1 Low, 2 Medium, 3 High.
          required:
            - EVERYDAY_CASUAL
            - WORK_PROFESSIONAL
            - SCHOOL_UNIVERSITY
            - FORMAL_SPECIAL_EVENT
            - TRAVEL
            - SPORT_ACTIVE
          properties:
            EVERYDAY_CASUAL: { type: [integer, "null"], minimum: 0, maximum: 3 }
            WORK_PROFESSIONAL: { type: [integer, "null"], minimum: 0, maximum: 3 }
            SCHOOL_UNIVERSITY: { type: [integer, "null"], minimum: 0, maximum: 3 }
            FORMAL_SPECIAL_EVENT: { type: [integer, "null"], minimum: 0, maximum: 3 }
            TRAVEL: { type: [integer, "null"], minimum: 0, maximum: 3 }
            SPORT_ACTIVE: { type: [integer, "null"], minimum: 0, maximum: 3 }
        updatedAt:
          type: [string, "null"]
          format: date-time

    ProfileStyleCode:
      type: string
      description: Profile style preference (SRS 4.10); not a garment Style.
      enum: [MINIMAL, CASUAL, SMART_CASUAL, CLASSIC, STREETWEAR, SPORTY, FORMAL]

    CommonNeedCode:
      type: string
      description: |
        Common-need dimension (SRS 3.14.1; codes per OPEN-S3-15):
        EVERYDAY_CASUAL (EVERYDAY, DATE, SOCIAL_EVENT),
        WORK_PROFESSIONAL (WORK), SCHOOL_UNIVERSITY
        (SCHOOL_UNIVERSITY), FORMAL_SPECIAL_EVENT (FORMAL_EVENT),
        TRAVEL (TRAVEL), SPORT_ACTIVE (SPORT_ACTIVITY).
      enum: [EVERYDAY_CASUAL, WORK_PROFESSIONAL, SCHOOL_UNIVERSITY, FORMAL_SPECIAL_EVENT, TRAVEL, SPORT_ACTIVE]

    FieldError:
```

**Explanation.**

- **Contract test.** `IdentityOpenApiContractIT` checks that both operations are documented and protected. It also checks that the request schema's three properties equal the record components, and that `Preferences` lists and requires the four returned fields. Removing `expectedRevision` from the first example must give `400 REQUIRED`.
- **Merge rule.** If US-GAR-002 is not applied, also add its `RevisionConflict` response (US-GAR-002 P21, change 8). The `BadRequest` response already exists.

### Step P32 — [MODIFY] `.github/workflows/backend-ci.yml`

BEFORE (the last entry after US-GAR-003):

```yaml
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.out.persistence.PhotoUploadCleanupIT.xml' = 4
          }
```

AFTER:

```yaml
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.out.persistence.PhotoUploadCleanupIT.xml' = 4
            'surefire-reports/TEST-com.capsuleai.personalcontext.domain.DeclaredPreferencesTest.xml' = 5
            'surefire-reports/TEST-com.capsuleai.personalcontext.application.RevisePreferencesServiceTest.xml' = 11
            'surefire-reports/TEST-com.capsuleai.personalcontext.adapter.in.web.PreferencesRevisionRequestTest.xml' = 4
            'failsafe-reports/TEST-com.capsuleai.personalcontext.adapter.out.persistence.PreferencesSchemaIT.xml' = 3
            'failsafe-reports/TEST-com.capsuleai.personalcontext.adapter.in.web.PreferencesApiIT.xml' = 9
            'failsafe-reports/TEST-com.capsuleai.personalcontext.application.ConcurrentPreferencesIT.xml' = 3
            'failsafe-reports/TEST-com.capsuleai.personalcontext.application.CurrentPersonalContextIT.xml' = 2
          }
```

The guard now has 95 entries. If prepared before the garment stories, append after the last existing entry instead, and add `sprint_03` to the PR trigger (US-GAR-002 P22, change 1).

### Step P33 — [MODIFY] `backend/README.md`

Change 1. BEFORE:

```markdown
## 10. Database migrations
```

AFTER:

````markdown
### Personal preferences

US-PROF-001 adds the account's own declared profile styles and common-need priorities:

- `GET /api/v1/personal-context/preferences`: always `200`. Nothing declared yet means `revision: null`, `styles: []` and every need `null`. That is absent context, not a default persona.
- `PATCH /api/v1/personal-context/preferences`: send `expectedRevision` (`null` for the first declaration, otherwise the `revision` last read) and only what changes.

| In the body | Effect |
| --- | --- |
| `styles` omitted / `null` or `[]` / array | Keep / clear / replace (codes `MINIMAL`, `CASUAL`, `SMART_CASUAL`, `CLASSIC`, `STREETWEAR`, `SPORTY`, `FORMAL`) |
| `commonNeeds` omitted | Keep every need |
| `commonNeeds.<NEED>`: `null` / `0`–`3` | Unset / Not Relevant, Low, Medium, High (`EVERYDAY_CASUAL`, `WORK_PROFESSIONAL`, `SCHOOL_UNIVERSITY`, `FORMAL_SPECIAL_EVENT`, `TRAVEL`, `SPORT_ACTIVE`) |

An explicit `0` stays `0`; it is never replaced by a default. Garment Style codes are not profile styles. A stale `expectedRevision`, or a second first declaration, is `409 REVISION_CONFLICT`: reload, review and submit again. After a lost response, reload before retrying; a retry with the original `expectedRevision` is never applied twice. Nothing else (wardrobe access, garment creation) depends on preferences.

Focused tests, from `backend`:

```powershell
.\mvnw.cmd '-Dtest=DeclaredPreferencesTest,RevisePreferencesServiceTest,PreferencesRevisionRequestTest' test
.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PreferencesApiIT,ConcurrentPreferencesIT,PreferencesSchemaIT,CurrentPersonalContextIT,DatabaseSchemaOwnershipIT,IdentityOpenApiContractIT' verify
```

## 10. Database migrations
````

Change 2. BEFORE:

```markdown
creates `wardrobe.wardrobe_photo_upload`, the staged-photo lifecycle table. Flyway maintains
```

AFTER:

```markdown
creates `wardrobe.wardrobe_photo_upload`, the staged-photo lifecycle table. `V11__create_personal_context_preferences.sql` (US-PROF-001) creates the `personal_context` schema with `personal_context.personal_context_preferences` and its two set tables. Flyway maintains
```

Change 3. BEFORE ``Expect successful rows for versions `1` to `10`.``, AFTER ``Expect successful rows for versions `1` to `11`.``

Change 4. BEFORE:

```markdown
It also adds private photos for new garments (US-GAR-003; real AWS deployment not verified).
```

AFTER:

```markdown
It also adds private photos for new garments (US-GAR-003; real AWS deployment not verified) and the account's declared styles and common needs (US-PROF-001).
```

## 13. Complete Automated Test Implementation

### Step T1 — [ADD] `DeclaredPreferencesTest.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/domain/DeclaredPreferencesTest.java`

```java
package com.capsuleai.personalcontext.domain;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** US-PROF-001 (S3-PROF-02): canonical vocabularies, the need mapping, priorities and absent versus zero. */
class DeclaredPreferencesTest {

    @Test
    void theProfileStylesAreExactlyTheSevenSrsCodes() {
        assertThat(Arrays.stream(ProfileStyle.values()).map(Enum::name)).containsExactly(
                "MINIMAL", "CASUAL", "SMART_CASUAL", "CLASSIC", "STREETWEAR", "SPORTY", "FORMAL");
        assertThat(ProfileStyle.fromCode("STREET")).as("a garment Style code").isEmpty();
        assertThat(ProfileStyle.fromCode("MINIMALIST")).as("a garment Style code").isEmpty();
        assertThat(ProfileStyle.fromCode("minimal")).as("no case folding").isEmpty();
    }

    @Test
    void everyNeedMapsToItsSrsOccasionCodes() {
        assertThat(CommonNeed.EVERYDAY_CASUAL.relevantOccasionCodes()).containsExactly("EVERYDAY", "DATE", "SOCIAL_EVENT");
        assertThat(CommonNeed.WORK_PROFESSIONAL.relevantOccasionCodes()).containsExactly("WORK");
        assertThat(CommonNeed.SCHOOL_UNIVERSITY.relevantOccasionCodes()).containsExactly("SCHOOL_UNIVERSITY");
        assertThat(CommonNeed.FORMAL_SPECIAL_EVENT.relevantOccasionCodes()).containsExactly("FORMAL_EVENT");
        assertThat(CommonNeed.TRAVEL.relevantOccasionCodes()).containsExactly("TRAVEL");
        assertThat(CommonNeed.SPORT_ACTIVE.relevantOccasionCodes()).containsExactly("SPORT_ACTIVITY");
        assertThat(CommonNeed.values()).hasSize(6);
    }

    @Test
    void prioritiesAreNotRelevantZeroToHighThreeAndNothingElse() {
        assertThat(NeedPriority.fromWeight(0)).contains(NeedPriority.NOT_RELEVANT);
        assertThat(NeedPriority.fromWeight(1)).contains(NeedPriority.LOW);
        assertThat(NeedPriority.fromWeight(2)).contains(NeedPriority.MEDIUM);
        assertThat(NeedPriority.fromWeight(3)).contains(NeedPriority.HIGH);
        assertThat(NeedPriority.fromWeight(-1)).isEmpty();
        assertThat(NeedPriority.fromWeight(4)).isEmpty();
    }

    @Test
    void anUnsetNeedIsDistinctFromANotRelevantOne() {
        DeclaredPreferences preferences = new DeclaredPreferences(Set.of(),
                Map.of(CommonNeed.TRAVEL, NeedPriority.NOT_RELEVANT));

        assertThat(preferences.priorityOf(CommonNeed.TRAVEL)).contains(NeedPriority.NOT_RELEVANT);
        assertThat(preferences.priorityOf(CommonNeed.WORK_PROFESSIONAL)).isEmpty();
        assertThat(preferences.declaresNothing()).isFalse();
        assertThat(DeclaredPreferences.none().declaresNothing()).isTrue();
    }

    @Test
    void preferencesAreImmutableCopiesInCatalogueOrder() {
        EnumSet<ProfileStyle> styles = EnumSet.of(ProfileStyle.FORMAL, ProfileStyle.MINIMAL);
        EnumMap<CommonNeed, NeedPriority> needs = new EnumMap<>(Map.of(CommonNeed.WORK_PROFESSIONAL, NeedPriority.HIGH));
        DeclaredPreferences preferences = new DeclaredPreferences(styles, needs);

        styles.add(ProfileStyle.CASUAL);
        needs.put(CommonNeed.TRAVEL, NeedPriority.LOW);

        assertThat(List.copyOf(preferences.styles())).containsExactly(ProfileStyle.MINIMAL, ProfileStyle.FORMAL);
        assertThat(preferences.commonNeeds()).containsOnlyKeys(CommonNeed.WORK_PROFESSIONAL);
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=DeclaredPreferencesTest' test`, expected 5 tests.

### Step T2 — [ADD] `InMemoryPreferenceProfileRepository.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/application/InMemoryPreferenceProfileRepository.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake with the same guarded creation and conditional revision as the SQL. */
class InMemoryPreferenceProfileRepository implements PreferenceProfileRepository {

    private final Map<UUID, PreferenceProfile> profiles = new HashMap<>();
    private int writes;

    @Override
    public synchronized Optional<PreferenceProfile> find(UUID ownerAccountId) {
        return Optional.ofNullable(profiles.get(ownerAccountId));
    }

    @Override
    public synchronized boolean createIfAbsent(PreferenceProfile profile) {
        if (profiles.putIfAbsent(profile.ownerAccountId(), profile) != null) {
            return false;
        }
        writes++;
        return true;
    }

    @Override
    public synchronized boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision,
                                                  DeclaredPreferences preferences, Instant updatedAt) {
        PreferenceProfile current = profiles.get(ownerAccountId);
        if (current == null || current.revision() != expectedRevision) {
            return false;
        }
        profiles.put(ownerAccountId, current.revisedTo(preferences, updatedAt));
        writes++;
        return true;
    }

    synchronized int writes() {
        return writes;
    }
}
```

### Step T3 — [ADD] `ImmediatePersonalContextTransactions.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/application/ImmediatePersonalContextTransactions.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import java.util.function.Supplier;

/** Fake transaction boundary: runs the work directly. */
final class ImmediatePersonalContextTransactions implements PersonalContextTransactions {

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return work.get();
    }
}
```

### Step T4 — [ADD] `RevisePreferencesServiceTest.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/application/RevisePreferencesServiceTest.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import com.capsuleai.personalcontext.domain.PreferenceValueProblem;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-001 — presence-aware, validated, revision-protected preference changes (S3-PROF-02/03/04). */
class RevisePreferencesServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();

    private final InMemoryPreferenceProfileRepository profiles = new InMemoryPreferenceProfileRepository();
    private final RevisePreferencesService service = new RevisePreferencesService(profiles,
            new ImmediatePersonalContextTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aFirstDeclarationCreatesTheProfileAtRevisionOne() {
        PreferencesRevision revision = service.revise(command(new ExpectedRevision.Unset(),
                ValueChange.replace(List.of("MINIMAL", "SMART_CASUAL")), needs("WORK_PROFESSIONAL", 3)));

        PreferenceProfile profile = revision.profile().orElseThrow();
        assertThat(revision.changed()).isTrue();
        assertThat(profile.revision()).isEqualTo(1L);
        assertThat(profile.preferences().styles()).containsExactly(ProfileStyle.MINIMAL, ProfileStyle.SMART_CASUAL);
        assertThat(profile.preferences().priorityOf(CommonNeed.WORK_PROFESSIONAL)).contains(NeedPriority.HIGH);
        assertThat(profiles.find(OWNER)).contains(profile);
    }

    @Test
    void omittedStylesAndNeedsKeepTheirValuesAndNullUnsetsANeed() {
        declare(List.of("CLASSIC"), needs("WORK_PROFESSIONAL", 3, "TRAVEL", 1));

        DeclaredPreferences revised = service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("TRAVEL", null))).profile().orElseThrow().preferences();

        assertThat(revised.styles()).containsExactly(ProfileStyle.CLASSIC);
        assertThat(revised.priorityOf(CommonNeed.WORK_PROFESSIONAL)).contains(NeedPriority.HIGH);
        assertThat(revised.priorityOf(CommonNeed.TRAVEL)).as("unset, not zero").isEmpty();
    }

    @Test
    void explicitZeroIsKeptAsNotRelevantAndNeverReplacedByADefault() {
        declare(List.of(), needs("SPORT_ACTIVE", 0));

        DeclaredPreferences revised = service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("TRAVEL", 2))).profile().orElseThrow().preferences();

        assertThat(revised.priorityOf(CommonNeed.SPORT_ACTIVE)).contains(NeedPriority.NOT_RELEVANT);
        assertThat(revised.commonNeeds()).hasSize(2);
    }

    @Test
    void clearingStylesLeavesAnEmptySelection() {
        declare(List.of("STREETWEAR", "SPORTY"), Map.of());

        PreferencesRevision revision = service.revise(command(new ExpectedRevision.At(1), ValueChange.clear(), Map.of()));

        assertThat(revision.profile().orElseThrow().preferences().styles()).isEmpty();
        assertThat(revision.profile().orElseThrow().revision()).isEqualTo(2L);
    }

    @Test
    void unsupportedStylesNeedsAndPrioritiesAreRejectedWithoutChanges() {
        declare(List.of("CASUAL"), Map.of());
        Map<String, ValueChange<Integer>> needs = new LinkedHashMap<>();
        needs.put("TRAVEL", ValueChange.replace(4));
        needs.put("NIGHT_OUT", ValueChange.replace(1));

        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.At(1),
                ValueChange.replace(List.of("CASUAL", "STREET")), needs)))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("styles[1]", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("commonNeeds.TRAVEL", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("commonNeeds", PreferenceValueProblem.UNSUPPORTED_VALUE)));
        assertThat(profiles.find(OWNER).orElseThrow().revision()).isEqualTo(1L);
    }

    @Test
    void aMissingOrMalformedExpectedRevisionIsRejectedBeforeAnyLookup() {
        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.Missing(), ValueChange.keep(), Map.of())))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("expectedRevision", PreferenceValueProblem.REQUIRED)));
        assertThatThrownBy(() -> service.revise(command(ExpectedRevision.ofToken("0"), ValueChange.keep(), Map.of())))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("expectedRevision", PreferenceValueProblem.INVALID_FORMAT)));
    }

    @Test
    void aSecondFirstDeclarationIsAConflict() {
        declare(List.of("MINIMAL"), Map.of());

        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.Unset(),
                ValueChange.replace(List.of("FORMAL")), Map.of())))
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThat(profiles.find(OWNER).orElseThrow().preferences().styles()).containsExactly(ProfileStyle.MINIMAL);
    }

    @Test
    void aStaleRevisionIsAConflictAndChangesNothing() {
        declare(List.of(), needs("WORK_PROFESSIONAL", 1));
        service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(), needs("WORK_PROFESSIONAL", 2)));

        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("WORK_PROFESSIONAL", 3))))
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThatThrownBy(() -> service.revise(command(new ExpectedRevision.At(1), ValueChange.keep(),
                needs("WORK_PROFESSIONAL", 2))))
                .as("OPEN-S3-06: a stale precondition is a conflict even when nothing would change")
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThat(profiles.find(OWNER).orElseThrow().preferences().priorityOf(CommonNeed.WORK_PROFESSIONAL))
                .contains(NeedPriority.MEDIUM);
    }

    @Test
    void anUnchangedRevisionAndAnEmptyFirstDeclarationWriteNothing() {
        PreferencesRevision emptyFirst = service.revise(command(new ExpectedRevision.Unset(), ValueChange.clear(), Map.of()));
        assertThat(emptyFirst.changed()).isFalse();
        assertThat(emptyFirst.profile()).isEmpty();

        declare(List.of("CLASSIC"), needs("TRAVEL", 0));
        int writesBefore = profiles.writes();
        PreferencesRevision same = service.revise(command(new ExpectedRevision.At(1),
                ValueChange.replace(List.of("CLASSIC", "CLASSIC")), needs("TRAVEL", 0)));

        assertThat(same.changed()).isFalse();
        assertThat(same.profile().orElseThrow().revision()).isEqualTo(1L);
        assertThat(profiles.writes()).isEqualTo(writesBefore);
    }

    @Test
    void valuesOfTheWrongKindAndANullNeedObjectAreRejected() {
        assertThatThrownBy(() -> service.revise(new RevisePreferencesCommand(OWNER, new ExpectedRevision.Unset(),
                new PreferenceChanges(ValueChange.malformed(), Map.of("TRAVEL", ValueChange.malformed()), true))))
                .isInstanceOfSatisfying(PreferencesValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new PreferenceFieldViolation("styles", PreferenceValueProblem.UNSUPPORTED_VALUE),
                                new PreferenceFieldViolation("commonNeeds", PreferenceValueProblem.UNSUPPORTED_VALUE)));
    }

    @Test
    void aConcurrentFirstDeclarationDetectedAtWriteTimeIsAConflict() {
        InMemoryPreferenceProfileRepository racing = new InMemoryPreferenceProfileRepository() {
            @Override
            public synchronized boolean createIfAbsent(PreferenceProfile profile) {
                super.createIfAbsent(PreferenceProfile.firstDeclaration(OWNER,
                        new DeclaredPreferences(Set.of(ProfileStyle.FORMAL), Map.of()), NOW));
                return super.createIfAbsent(profile);
            }
        };
        RevisePreferencesService racingService = new RevisePreferencesService(racing,
                new ImmediatePersonalContextTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> racingService.revise(command(new ExpectedRevision.Unset(),
                ValueChange.replace(List.of("MINIMAL")), Map.of())))
                .isInstanceOf(PreferencesRevisionConflictException.class);
        assertThat(racing.find(OWNER).orElseThrow().preferences().styles()).containsExactly(ProfileStyle.FORMAL);
    }

    private void declare(List<String> styles, Map<String, ValueChange<Integer>> needs) {
        service.revise(command(new ExpectedRevision.Unset(), ValueChange.replace(styles), needs));
    }

    private static RevisePreferencesCommand command(ExpectedRevision expected, ValueChange<List<String>> styles,
                                                    Map<String, ValueChange<Integer>> needs) {
        return new RevisePreferencesCommand(OWNER, expected, new PreferenceChanges(styles, needs, false));
    }

    /** Alternating need code and weight; a null weight means "unset this need". */
    private static Map<String, ValueChange<Integer>> needs(Object... codesAndWeights) {
        Map<String, ValueChange<Integer>> needs = new LinkedHashMap<>();
        for (int index = 0; index < codesAndWeights.length; index += 2) {
            Integer weight = (Integer) codesAndWeights[index + 1];
            needs.put((String) codesAndWeights[index], weight == null ? ValueChange.clear() : ValueChange.replace(weight));
        }
        return needs;
    }
}
```

**Explanation.**

- **Subclassing the fake.** `InMemoryPreferenceProfileRepository` is deliberately not `final`, so the last test can subclass it to simulate a competing first declaration between the read and the write.

**Command:** `.\mvnw.cmd '-Dtest=RevisePreferencesServiceTest' test`, expected 11 tests.

### Step T5 — [ADD] `PreferencesRevisionRequestTest.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesRevisionRequestTest.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.application.ExpectedRevision;
import com.capsuleai.personalcontext.application.PreferenceChanges;
import com.capsuleai.personalcontext.application.RevisePreferencesCommand;
import com.capsuleai.personalcontext.application.ValueChange;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** US-PROF-001 (OPEN-S3-05): omitted, null and values stay distinct; pins the Jackson 3 behaviour relied on. */
class PreferencesRevisionRequestTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Test
    void anOmittedExpectedRevisionIsMissingAndNullMeansNothingDeclaredYet() {
        assertThat(command("{}").expectedRevision()).isInstanceOf(ExpectedRevision.Missing.class);
        assertThat(command("{\"expectedRevision\":null}").expectedRevision()).isInstanceOf(ExpectedRevision.Unset.class);
    }

    @Test
    void aRevisionTokenIsAStringOfDigits() {
        assertThat(command("{\"expectedRevision\":\"7\"}").expectedRevision()).isEqualTo(new ExpectedRevision.At(7));
        assertThat(command("{\"expectedRevision\":7}").expectedRevision()).isInstanceOf(ExpectedRevision.Malformed.class);
        assertThat(command("{\"expectedRevision\":\"07\"}").expectedRevision())
                .isInstanceOf(ExpectedRevision.Malformed.class);
    }

    @Test
    void stylesCanBeOmittedClearedReplacedOrMalformed() {
        assertThat(changes("{}").styles()).isInstanceOf(ValueChange.Keep.class);
        assertThat(changes("{\"styles\":null}").styles()).isInstanceOf(ValueChange.Clear.class);
        assertThat(changes("{\"styles\":[\"MINIMAL\",5]}").styles())
                .isEqualTo(ValueChange.replace(Arrays.asList("MINIMAL", null)));
        assertThat(changes("{\"styles\":\"MINIMAL\"}").styles()).isInstanceOf(ValueChange.Malformed.class);
    }

    @Test
    void eachNeedIsKeptClearedOrReplacedIndividuallyAndANullNeedObjectIsMalformed() {
        PreferenceChanges changes = changes("{\"commonNeeds\":{\"TRAVEL\":0,\"WORK_PROFESSIONAL\":null,"
                + "\"SPORT_ACTIVE\":\"HIGH\",\"SCHOOL_UNIVERSITY\":2.5}}");

        assertThat(changes.commonNeeds().get("TRAVEL")).isEqualTo(ValueChange.replace(0));
        assertThat(changes.commonNeeds().get("WORK_PROFESSIONAL")).isInstanceOf(ValueChange.Clear.class);
        assertThat(changes.commonNeeds().get("SPORT_ACTIVE")).isInstanceOf(ValueChange.Malformed.class);
        assertThat(changes.commonNeeds().get("SCHOOL_UNIVERSITY")).isInstanceOf(ValueChange.Malformed.class);
        assertThat(changes.commonNeeds()).doesNotContainKey("EVERYDAY_CASUAL");
        assertThat(changes.commonNeedsMalformed()).isFalse();
        assertThat(changes("{}").commonNeedsMalformed()).isFalse();
        assertThat(changes("{\"commonNeeds\":null}").commonNeedsMalformed()).isTrue();
        assertThat(changes("{\"commonNeeds\":[1]}").commonNeedsMalformed()).isTrue();
    }

    private static PreferenceChanges changes(String json) {
        return command(json).changes();
    }

    private static RevisePreferencesCommand command(String json) {
        return JSON.readValue(json, PreferencesRevisionRequest.class).toCommand(UUID.randomUUID());
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=PreferencesRevisionRequestTest' test`, expected 4 tests.

### Step T6 — [ADD] `PersonalContextIntegrationTestSupport.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/support/PersonalContextIntegrationTestSupport.java`

```java
package com.capsuleai.personalcontext.support;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Base class for Personal Context integration tests: the shared real application, filter chain and
 * PostgreSQL, with the personal_context tables emptied before every test (no FK into Identity, so
 * Identity's cleanup does not reach them).
 */
public abstract class PersonalContextIntegrationTestSupport extends IdentityIntegrationTestSupport {

    protected static final String PREFERENCES = "/api/v1/personal-context/preferences";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @BeforeEach
    protected void resetPersonalContextState() {
        jdbc.sql("TRUNCATE TABLE personal_context.personal_context_common_need, "
                + "personal_context.personal_context_preferred_style, personal_context.personal_context_preferences")
                .update();
    }

    protected ResultActions viewPreferences(String accessToken) throws Exception {
        return mockMvc.perform(get(PREFERENCES).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    protected ResultActions revisePreferences(String accessToken, Map<String, ?> body) throws Exception {
        return mockMvc.perform(patch(PREFERENCES)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }
}
```

### Step T7 — [ADD] `PreferencesSchemaIT.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/adapter/out/persistence/PreferencesSchemaIT.java`

```java
package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-001: the first Personal Context migration and its constraints (S3-PROF-01/02, AR-01). */
class PreferencesSchemaIT extends PersonalContextIntegrationTestSupport {

    @Test
    void theMigrationCreatesOnlyThePersonalContextSchemaAndItsThreeTables() {
        assertThat(jdbc.sql("""
                        SELECT table_name FROM information_schema.tables
                        WHERE table_schema = 'personal_context' ORDER BY table_name
                        """).query(String.class).list())
                .containsExactly("personal_context_common_need", "personal_context_preferences",
                        "personal_context_preferred_style");
    }

    @Test
    void theDatabaseRefusesUnknownStylesNeedsAndPriorities() {
        UUID owner = insertProfile(1);

        assertThatThrownBy(() -> jdbc.sql("INSERT INTO personal_context.personal_context_preferred_style "
                + "(owner_account_id, style) VALUES (:owner, 'STREET')").param("owner", owner).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("INSERT INTO personal_context.personal_context_common_need "
                + "(owner_account_id, need, priority) VALUES (:owner, 'NIGHT_OUT', 1)").param("owner", owner).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("INSERT INTO personal_context.personal_context_common_need "
                + "(owner_account_id, need, priority) VALUES (:owner, 'TRAVEL', 4)").param("owner", owner).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        jdbc.sql("INSERT INTO personal_context.personal_context_common_need (owner_account_id, need, priority) "
                + "VALUES (:owner, 'TRAVEL', 0)").param("owner", owner).update();
    }

    @Test
    void anOwnerHasOneProfileAndRevisionsArePositive() {
        UUID owner = insertProfile(1);

        assertThatThrownBy(() -> insertProfile(owner, 1)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertProfile(0)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertProfile(long revision) {
        UUID owner = UUID.randomUUID();
        insertProfile(owner, revision);
        return owner;
    }

    private void insertProfile(UUID owner, long revision) {
        jdbc.sql("""
                        INSERT INTO personal_context.personal_context_preferences (owner_account_id, revision, created_at, updated_at)
                        VALUES (:owner, :revision, now(), now())
                        """)
                .param("owner", owner).param("revision", revision).update();
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PreferencesSchemaIT' verify`, expected 3 tests.

### Step T8 — [ADD] `PreferencesApiIT.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/adapter/in/web/PreferencesApiIT.java`

```java
package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-PROF-001 through the real filter chain, application and PostgreSQL. */
class PreferencesApiIT extends PersonalContextIntegrationTestSupport {

    /** AC-01/AC-04: unset is absent context and blocks nothing else. */
    @Test
    void aNewAccountHasNoDeclaredContextAndCanStillUseTheWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-new@example.com");

        viewPreferences(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(nullValue()))
                .andExpect(jsonPath("$.styles.length()").value(0))
                .andExpect(jsonPath("$.commonNeeds.length()").value(6))
                .andExpect(jsonPath("$.commonNeeds", hasKey("TRAVEL")))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(nullValue()))
                .andExpect(jsonPath("$.updatedAt").value(nullValue()));
        mockMvc.perform(post("/api/v1/wardrobe/garments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,\"category\":\"TOPS\",\"color\":\"WHITE\"}"))
                .andExpect(status().isCreated());
    }

    /** AC-01/AC-02/AC-05. */
    @Test
    void aFirstDeclarationIsAcceptedAndReadBack() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-first@example.com");

        revisePreferences(tokens.accessToken(), body(null, List.of("SMART_CASUAL", "MINIMAL"),
                Map.of("WORK_PROFESSIONAL", 3, "SPORT_ACTIVE", 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.styles[0]").value("MINIMAL"))
                .andExpect(jsonPath("$.styles[1]").value("SMART_CASUAL"));

        viewPreferences(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("1"))
                .andExpect(jsonPath("$.commonNeeds.WORK_PROFESSIONAL").value(3))
                .andExpect(jsonPath("$.commonNeeds.SPORT_ACTIVE").value(0))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(nullValue()));
    }

    /** S3-PROF-02/03: untouched values stay; explicit zero stays zero. */
    @Test
    void partialRevisionsKeepUntouchedValuesAndExplicitZeroStaysZero() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-partial@example.com");
        revisePreferences(tokens.accessToken(), body(null, List.of("CLASSIC"), Map.of("TRAVEL", 0, "WORK_PROFESSIONAL", 2)))
                .andExpect(status().isOk());

        revisePreferences(tokens.accessToken(), body("1", null, Map.of("WORK_PROFESSIONAL", 3), "styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.styles[0]").value("CLASSIC"))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(0))
                .andExpect(jsonPath("$.commonNeeds.WORK_PROFESSIONAL").value(3));
    }

    /** S3-PROF-03: a cleared need is unset; cleared styles are none. */
    @Test
    void clearingANeedMakesItUnsetAndClearingStylesLeavesNone() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-clear@example.com");
        revisePreferences(tokens.accessToken(), body(null, List.of("FORMAL"), Map.of("SCHOOL_UNIVERSITY", 1)))
                .andExpect(status().isOk());
        Map<String, Object> clearSchool = new LinkedHashMap<>();
        clearSchool.put("SCHOOL_UNIVERSITY", null);

        revisePreferences(tokens.accessToken(), body("1", List.of(), clearSchool))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles.length()").value(0))
                .andExpect(jsonPath("$.commonNeeds.SCHOOL_UNIVERSITY").value(nullValue()));
    }

    /** AC-02, UC-005 E1. */
    @Test
    void unsupportedValuesAreExplainedAndNothingIsSaved() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-invalid@example.com");

        revisePreferences(tokens.accessToken(), body(null, List.of("MINIMAL", "BOHO"), Map.of("TRAVEL", 7)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("styles[1]"))
                .andExpect(jsonPath("$.fieldErrors[1].field").value("commonNeeds.TRAVEL"));
        viewPreferences(tokens.accessToken()).andExpect(jsonPath("$.revision").value(nullValue()));
    }

    /** S3-PROF-04: two devices, and a duplicated first declaration. */
    @Test
    void aStaleRevisionOrASecondFirstDeclarationIsAConflict() throws Exception {
        IssuedTokens phone = registerAndLogIn("prefs-devices@example.com");
        IssuedTokens laptop = logIn("prefs-devices@example.com", PASSWORD);
        revisePreferences(phone.accessToken(), body(null, List.of("CASUAL"), Map.of())).andExpect(status().isOk());

        revisePreferences(laptop.accessToken(), body(null, List.of("FORMAL"), Map.of()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVISION_CONFLICT"));
        revisePreferences(phone.accessToken(), body("1", null, Map.of("TRAVEL", 2), "styles")).andExpect(status().isOk());
        revisePreferences(laptop.accessToken(), body("1", null, Map.of("TRAVEL", 3), "styles"))
                .andExpect(status().isConflict());
        viewPreferences(laptop.accessToken())
                .andExpect(jsonPath("$.styles[0]").value("CASUAL"))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(2));
    }

    /** AC-06, UC-005 E3: private to the account; no usable session, no change. */
    @Test
    void preferencesArePrivateToTheAccountAndNeedAUsableSession() throws Exception {
        IssuedTokens alice = registerAndLogIn("prefs-alice@example.com");
        IssuedTokens bob = registerAndLogIn("prefs-bob@example.com");
        revisePreferences(alice.accessToken(), body(null, List.of("STREETWEAR"), Map.of())).andExpect(status().isOk());

        viewPreferences(bob.accessToken()).andExpect(jsonPath("$.revision").value(nullValue()));
        mockMvc.perform(get(PREFERENCES)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + alice.accessToken()))
                .andExpect(status().is2xxSuccessful());
        revisePreferences(alice.accessToken(), body("1", List.of(), Map.of())).andExpect(status().isUnauthorized());

        IssuedTokens again = logIn("prefs-alice@example.com", PASSWORD);
        viewPreferences(again.accessToken()).andExpect(jsonPath("$.styles[0]").value("STREETWEAR"));
    }

    /** BRULE-GAR-004: garment Style codes are not profile styles. */
    @Test
    void garmentStylesAreNotProfileStyles() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-garment-style@example.com");

        revisePreferences(tokens.accessToken(), body(null, List.of("STREET", "MINIMALIST"), Map.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.length()").value(2));
    }

    /** AC-06, S3-PROF-04: a lost response; the retry with its original revision cannot apply twice. */
    @Test
    void aCommittedRevisionRepeatedWithItsOriginalRevisionIsNeverAppliedTwice() throws Exception {
        IssuedTokens tokens = registerAndLogIn("prefs-lost@example.com");
        revisePreferences(tokens.accessToken(), body(null, List.of(), Map.of("TRAVEL", 1))).andExpect(status().isOk());
        Map<String, Object> intended = body("1", null, Map.of("TRAVEL", 2), "styles");

        revisePreferences(tokens.accessToken(), intended).andExpect(status().isOk());
        revisePreferences(tokens.accessToken(), intended).andExpect(status().isConflict());
        viewPreferences(tokens.accessToken())
                .andExpect(jsonPath("$.revision").value("2"))
                .andExpect(jsonPath("$.commonNeeds.TRAVEL").value(2));
    }

    /** A request body; names listed in {@code omitted} are left out entirely (not sent as null). */
    private static Map<String, Object> body(String expectedRevision, List<String> styles, Map<String, ?> needs,
                                            String... omitted) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("expectedRevision", expectedRevision);
        body.put("styles", styles);
        body.put("commonNeeds", needs);
        for (String name : omitted) {
            body.remove(name);
        }
        return body;
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PreferencesApiIT' verify`, expected 9 tests.

### Step T9 — [ADD] `ConcurrentPreferencesIT.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/application/ConcurrentPreferencesIT.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-PROF-001 with real transactions (S3-PROF-04): one first declaration and one revision win; failures roll back. */
class ConcurrentPreferencesIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private RevisePreferencesService service;

    @Autowired
    private PreferenceProfileRepository profiles;

    @Autowired
    private PersonalContextTransactions transactions;

    @Test
    void ofTwoFirstDeclarationsExactlyOneIsAccepted() throws Exception {
        UUID owner = registerAndLogIn("race-first@example.com").accountId();

        List<String> outcomes = race(change(owner, new ExpectedRevision.Unset(), "MINIMAL", null),
                change(owner, new ExpectedRevision.Unset(), "FORMAL", null));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(jdbc.sql("SELECT count(*) FROM personal_context.personal_context_preferences")
                .query(Long.class).single()).isOne();
    }

    @Test
    void ofTwoRevisionsOfOneRevisionExactlyOneWinsAndTheStoredNeedsMatchTheWinner() throws Exception {
        UUID owner = registerAndLogIn("race-revise@example.com").accountId();
        service.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(),
                new PreferenceChanges(ValueChange.replace(List.of("CASUAL")), Map.of(), false)));

        List<String> outcomes = race(change(owner, new ExpectedRevision.At(1), null, 1),
                change(owner, new ExpectedRevision.At(1), null, 3));

        assertThat(outcomes).containsExactlyInAnyOrder("accepted", "conflict");
        assertThat(jdbc.sql("SELECT revision FROM personal_context.personal_context_preferences")
                .query(Long.class).single()).isEqualTo(2L);
        assertThat(jdbc.sql("SELECT count(*) FROM personal_context.personal_context_common_need WHERE need = 'TRAVEL'")
                .query(Long.class).single()).isOne();
    }

    @Test
    void aFailureAfterTheRevisionStatementRollsBackStylesAndNeeds() {
        UUID owner = registerAndLogInQuietly("race-rollback@example.com");
        service.revise(new RevisePreferencesCommand(owner, new ExpectedRevision.Unset(), new PreferenceChanges(
                ValueChange.replace(List.of("CLASSIC")), Map.of("TRAVEL", ValueChange.replace(1)), false)));
        DeclaredPreferences replacement = new DeclaredPreferences(Set.of(ProfileStyle.SPORTY),
                Map.of(CommonNeed.SPORT_ACTIVE, NeedPriority.HIGH));

        assertThatThrownBy(() -> transactions.inTransaction(() -> {
            assertThat(profiles.reviseIfCurrentAt(owner, 1, replacement, Instant.now())).isTrue();
            throw new IllegalStateException("simulated failure after the sets were rewritten");
        })).isInstanceOf(IllegalStateException.class);

        DeclaredPreferences stored = profiles.find(owner).orElseThrow().preferences();
        assertThat(stored.styles()).containsExactly(ProfileStyle.CLASSIC);
        assertThat(stored.priorityOf(CommonNeed.TRAVEL)).contains(NeedPriority.LOW);
        assertThat(stored.priorityOf(CommonNeed.SPORT_ACTIVE)).isEmpty();
        assertThat(profiles.find(owner).orElseThrow().revision()).isEqualTo(1L);
    }

    private RevisePreferencesCommand change(UUID owner, ExpectedRevision expected, String style, Integer travel) {
        return new RevisePreferencesCommand(owner, expected, new PreferenceChanges(
                style == null ? ValueChange.keep() : ValueChange.replace(List.of(style)),
                travel == null ? Map.of() : Map.of("TRAVEL", ValueChange.replace(travel)), false));
    }

    private List<String> race(RevisePreferencesCommand first, RevisePreferencesCommand second) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var a = executor.submit(attempt(start, first));
            var b = executor.submit(attempt(start, second));
            return List.of(a.get(20, SECONDS), b.get(20, SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<String> attempt(CyclicBarrier start, RevisePreferencesCommand command) {
        return () -> {
            start.await(10, SECONDS);
            try {
                service.revise(command);
                return "accepted";
            } catch (PreferencesRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private UUID registerAndLogInQuietly(String email) {
        try {
            return registerAndLogIn(email).accountId();
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }
}
```

**Explanation.**

- **First declarations.** Two first declarations race on the primary key. Whichever inserts second gets 0 rows and a conflict, or it sees the first profile in its own pre-read and conflicts there. Either way, exactly one is accepted.
- **Rollback.** The rollback test runs the real repository inside a real transaction and fails after the child sets were rewritten.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=ConcurrentPreferencesIT' verify`, expected 3 tests.

### Step T10 — [ADD] `CurrentPersonalContextIT.java`

**Path:** `backend/src/test/java/com/capsuleai/personalcontext/application/CurrentPersonalContextIT.java`

```java
package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.api.DeclaredPersonalContext;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-PROF-001 (S3-PROF-05): the published current-context query, as a future consumer would use it. */
class CurrentPersonalContextIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private CurrentPersonalContext currentPersonalContext;

    @Test
    void nothingDeclaredIsEmptyRatherThanAnInventedPersona() throws Exception {
        IssuedTokens tokens = registerAndLogIn("context-empty@example.com");

        assertThat(currentPersonalContext.find(tokens.accountId())).isEmpty();
    }

    @Test
    void theCurrentContextIsTheLatestAcceptedDeclarationIncludingExplicitZero() throws Exception {
        IssuedTokens tokens = registerAndLogIn("context-current@example.com");
        revisePreferences(tokens.accessToken(), Map.of("expectedRevision", "", "styles", List.of()))
                .andExpect(status().isBadRequest());
        java.util.Map<String, Object> first = new java.util.LinkedHashMap<>();
        first.put("expectedRevision", null);
        first.put("styles", List.of("MINIMAL"));
        first.put("commonNeeds", Map.of("TRAVEL", 0, "WORK_PROFESSIONAL", 3));
        revisePreferences(tokens.accessToken(), first).andExpect(status().isOk());
        revisePreferences(tokens.accessToken(), Map.of("expectedRevision", "1", "commonNeeds", Map.of("WORK_PROFESSIONAL", 2)))
                .andExpect(status().isOk());

        DeclaredPersonalContext context = currentPersonalContext.find(tokens.accountId()).orElseThrow();

        assertThat(context.revision()).isEqualTo("2");
        assertThat(context.styleCodes()).containsExactly("MINIMAL");
        assertThat(context.needPriorities()).containsExactlyInAnyOrderEntriesOf(Map.of("TRAVEL", 0, "WORK_PROFESSIONAL", 2));
        assertThat(context.needPriorities()).doesNotContainKey("SCHOOL_UNIVERSITY");
    }
}
```

**Explanation.**

- **Malformed token.** The first PATCH in the second test sends `""`, a malformed token. It proves that a rejected request leaves the published context empty before the real declaration is made.
- **Published types only.** The test consumes only `personalcontext.api` types, exactly as another module would. The existing `ArchitectureDependencyTest` keeps every other package of the module private.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=CurrentPersonalContextIT' verify`, expected 2 tests.

### Step T11 — [MODIFY] `DatabaseSchemaOwnershipIT.java`

**Path:** `backend/src/test/java/com/capsuleai/architecture/DatabaseSchemaOwnershipIT.java`

BEFORE:

```java
        assertThat(schemas).isSubsetOf(allowed).contains("identity");
```

AFTER:

```java
        assertThat(schemas).isSubsetOf(allowed).contains("identity", "wardrobe", "personal_context");
```

BEFORE:

```java
                .contains("identity.identity_account", "identity.identity_session", "identity.identity_refresh_token");
```

AFTER:

```java
                .contains("identity.identity_account", "identity.identity_session", "identity.identity_refresh_token",
                        "personal_context.personal_context_preferences",
                        "personal_context.personal_context_preferred_style",
                        "personal_context.personal_context_common_need");
```

**Explanation.** The generic rules already cover the new tables (owner schema, name prefix, no cross-schema foreign key, no routine). These assertions add positive evidence that V11 put them in the right place. The test count stays 5.

## 14. Manual Verification

Prerequisites: the `local` profile is running against the Compose PostgreSQL. Synthetic account; no token is printed.

```powershell
# Save as manual-prof001.ps1 (UTF-8 with BOM) outside the repository; run with: powershell -File .\manual-prof001.ps1
$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8080/api/v1'
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Call($method, $uri, $body, $headers = @{}) {
    $request = [System.Net.HttpWebRequest]::Create($uri); $request.Method = $method
    foreach ($name in $headers.Keys) { $request.Headers.Add($name, $headers[$name]) }
    if ($null -ne $body) {
        $bytes = $utf8.GetBytes($body); $request.ContentType = 'application/json; charset=utf-8'
        $request.ContentLength = $bytes.Length
        $stream = $request.GetRequestStream(); $stream.Write($bytes, 0, $bytes.Length); $stream.Close()
    } elseif ($method -eq 'POST') { $request.ContentLength = 0 }
    try { $response = $request.GetResponse() } catch [System.Net.WebException] { $response = $_.Exception.Response }
    $reader = New-Object System.IO.StreamReader($response.GetResponseStream(), $utf8)
    $text = $reader.ReadToEnd(); $status = [int]$response.StatusCode; $response.Close()
    $json = $null; if ($text) { $json = $text | ConvertFrom-Json }
    [pscustomobject]@{ Status = $status; Json = $json; Text = $text }
}
function Check($label, $ok) { "{0,-80} {1}" -f $label, $(if ($ok) { 'PASS' } else { 'FAIL' }) }

$email = "prefs.demo." + [guid]::NewGuid().ToString('N').Substring(0, 8) + "@example.com"
Call POST "$base/auth/registrations" ('{"email":"' + $email + '","password":"correct horse battery staple","confirmPassword":"correct horse battery staple"}') | Out-Null
$h = @{ Authorization = "Bearer $((Call POST "$base/auth/login" ('{"email":"' + $email + '","password":"correct horse battery staple"}')).Json.accessToken)" }
$p = "$base/personal-context/preferences"

$empty = Call GET $p $null $h
Check "unset profile -> 200, revision null, six null needs" ($empty.Status -eq 200 -and $null -eq $empty.Json.revision -and $empty.Text -match '"TRAVEL":null')
$first = Call PATCH $p '{"expectedRevision":null,"styles":["MINIMAL","SMART_CASUAL"],"commonNeeds":{"WORK_PROFESSIONAL":3,"SPORT_ACTIVE":0}}' $h
Check "first declaration -> revision 1, SPORT_ACTIVE 0" ($first.Status -eq 200 -and $first.Json.revision -eq '1' -and $first.Json.commonNeeds.SPORT_ACTIVE -eq 0)
$dup = Call PATCH $p '{"expectedRevision":null,"styles":["FORMAL"]}' $h
Check "second first declaration -> 409 REVISION_CONFLICT" ($dup.Status -eq 409 -and $dup.Json.code -eq 'REVISION_CONFLICT')
$partial = Call PATCH $p '{"expectedRevision":"1","commonNeeds":{"TRAVEL":2}}' $h
Check "partial -> revision 2, styles and zero kept" ($partial.Json.revision -eq '2' -and $partial.Json.styles.Count -eq 2 -and $partial.Json.commonNeeds.SPORT_ACTIVE -eq 0)
$bad = Call PATCH $p '{"expectedRevision":"2","styles":["STREET"],"commonNeeds":{"TRAVEL":5}}' $h
Check "garment style and priority 5 -> 400 with two field errors" ($bad.Status -eq 400 -and $bad.Json.fieldErrors.Count -eq 2)
$unset = Call PATCH $p '{"expectedRevision":"2","commonNeeds":{"TRAVEL":null}}' $h
Check "TRAVEL null -> unset again" ($unset.Status -eq 200 -and $unset.Text -match '"TRAVEL":null')
Call POST "$base/auth/logout" $null $h | Out-Null
Check "logged-out session -> 401" ((Call GET $p $null $h).Status -eq 401)
```

Optional database check:

```powershell
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c '\dt personal_context.*'
```

Expect three tables.

## 15. Acceptance Criteria Verification Matrix

### 15.1 AC to evidence

| AC | Backend evidence | Client evidence still required |
| --- | --- | --- |
| AC-01 Set or revisit | T8 unset GET, first declaration, revision; T4 first declaration | Onboarding and profile screens |
| AC-02 Canonical choices | T1 vocabularies, mapping and priorities; T4/T8 unsupported values; T8 garment styles rejected; T7 DB checks | Localized labels |
| AC-03 Common versus current context | No request occasion exists or is written (I-8); T1 mapping kept as data | Current occasion UI (future) |
| AC-04 Progressive entry | T8 unset profile does not block garment creation; nothing else depends on preferences | Skip and return flow |
| AC-05 Reflect accepted changes | T10 published context reflects the latest revision, explicit 0 kept | Future consumers mark assessments outdated using `revision` |
| AC-06 Cancel or recover | T8 conflict, lost-response retry, privacy, logout; T9 rollback | "Not saved" and outcome review UI |

### 15.2 Requirement to test

| Requirement / decision | Tests |
| --- | --- |
| FR-PROF-002 | T1, T4, T8 |
| FR-PROF-003, BRULE-COV-002 | T1 priorities, T4 zero/unset, T8 |
| FR-PROF-004, BRULE-PROF-002 | No occasion write path (design); T1 mapping |
| FR-PROF-008, FR-AUTH-011 | T8 progressive entry |
| DATA-AUTH-003 | T7, T8, T10 |
| FR-ANL-014, BRULE-COV-006 | `revision` in the published contract (T10) |
| BRULE-GAR-004 | T1, T8 garment styles rejected |
| S3-PROF-01 | V11 schema; T7; T11; `PersistenceOwnershipSqlTest` |
| S3-PROF-02 | T1, T4, T8 |
| S3-PROF-03 | T4, T5, T8 |
| S3-PROF-04 | T4, T8, T9 |
| S3-PROF-05 | T10; `ArchitectureDependencyTest` |
| S3-INT-01 / OPEN-S3-01 | `IdentityOpenApiContractIT` (two new operations); shared `REVISION_CONFLICT`, `VALIDATION_FAILED` and neutral `503` codes (P28) |
| OPEN-S3-04 (revision token) | T4 `aMissingOrMalformedExpectedRevision…`, `aStaleRevisionIsAConflict…`; T5 `aRevisionTokenIsAStringOfDigits`; T8 conflict; T9 races |
| OPEN-S3-05 (presence rules) | T5 (omitted, `null`, values, wrong kinds, `commonNeeds: null` rejected); T4 `omittedStylesAndNeedsKeep…`, `clearingStyles…` |
| OPEN-S3-06 (no-op) | T4 `anUnchangedRevisionAndAnEmptyFirstDeclarationWriteNothing`, `aStaleRevisionIsAConflict…` (a stale no-op is still `409`) |
| OPEN-S3-15 (representation) | T1 seven styles, six needs and their mappings, priorities 0–3; T4 explicit zero versus unset, empty styles; T8 `aNewAccountHasNoDeclaredContext…` (`revision: null`), all six needs present; T7 database CHECKs |
| OPEN-S3-16 (published contract) | T10 `CurrentPersonalContextIT`; `ArchitectureDependencyTest` (no `domain`/`application` types in `api`, no other module reads Personal Context internals); T11 ownership |

## 16. Definition of Done

| DoD item | Status at writing |
| --- | --- |
| AC mapped; tests written and passing | Planned |
| First owner migration verified (fresh and on top of V10) | Planned |
| Contract synchronized (OpenAPI 0.11.0) | Planned |
| Concurrency and rollback evidence | Planned |
| Architecture and ownership tests pass with the new module code | Planned |
| Approved choices OPEN-S3-01/04/05/06/15/16 implemented as specified | Decisions approved by the PO on 2026-11-11 ([decision record](open-questions.md)); proven only by the tests in §15.2 once they pass: Planned |
| Full `clean verify`; CI reports | Planned |
| Client evidence (onboarding, skip/return, labels, accessibility, conflict/uncertain UI) | Outstanding |

**Honesty note.** Nothing here has been compiled or executed.

## 17. Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| `PersistenceOwnershipSqlTest`: V11 changes several schemas | A table outside `personal_context` | Keep every object in `personal_context` |
| Flyway: version already used | A garment migration took V11 | Use the next free version; never renumber an applied migration |
| `ArchitectureDependencyTest` fails for `personalcontext.api` | An `api` type imports `domain`/`application` | Keep `DeclaredPersonalContext` as codes and primitives only |
| `IdentityOpenApiContractIT`: response fields differ | `Preferences` schema versus `PreferencesResponse` components | Exactly `revision`, `styles`, `commonNeeds`, `updatedAt` |
| `RevisionConflict` response missing from OpenAPI | US-GAR-002 not applied | Add it (US-GAR-002 P21, change 8) |
| A browser PATCH preflight is refused | CORS without `PATCH` | Apply US-GAR-002 P20 and T9 |
| `PreferencesRevisionRequestTest` first test fails | Jackson absent/null behaviour changed | See US-GAR-002 §17 (same fallback) |
| `"commonNeeds":{"TRAVEL":2.0}` rejected | Only integers are priorities | Send `2`, not `2.0` |

## 18. Final Implementation Checklist

- [ ] The starting point is confirmed (after the garment stories, or independent with the merge notes in §11).
- [ ] V11 (or the next free version) creates only `personal_context`.
- [ ] P1–P33 applied; T1–T11 applied.
- [ ] Unit tests: `.\mvnw.cmd '-Dtest=DeclaredPreferencesTest,RevisePreferencesServiceTest,PreferencesRevisionRequestTest' test`
- [ ] Architecture rules: `.\mvnw.cmd '-Dtest=ArchitectureDependencyTest,PersistenceOwnershipSqlTest' test`
- [ ] Integration tests: `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PreferencesApiIT,ConcurrentPreferencesIT,PreferencesSchemaIT,CurrentPersonalContextIT,DatabaseSchemaOwnershipIT,IdentityOpenApiContractIT' verify`
- [ ] `.\mvnw.cmd clean verify` passes; reports inspected.
- [ ] Manual script §14 run.
- [ ] The approved representation is implemented exactly (OPEN-S3-15: six need codes, `null` = unset, `0`–`3`, `[]` styles, `revision: null`; OPEN-S3-16: minimal `CurrentPersonalContext`, no consumer). Clients map the approved codes to localized labels; if the PO later revises a code, apply its change impact before the first deployment.
- [ ] README and OpenAPI 0.11.0 updated; CI reports added.
- [ ] Client evidence requested; the story is **not** marked Done without it.
