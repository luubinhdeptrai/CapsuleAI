# CapsuleAI — Sprint 2 Backend Sprint Planning

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## 1. Sprint Overview

| Field | Value |
| --- | --- |
| Sprint | Sprint 2 — Recover Access and Establish a Private Wardrobe |
| Version / status | 0.3 / Planning Draft — four-story scope selected by the Product Owner |
| Prepared | 2026-11-03; taxonomy/property revision 2026-11-05 |
| Revision inspection | Branch `sprint_02`; commit `57d5d9def0b985d94c0703337dd1f4b36e90c754`; initial working tree clean. Documentation alignment only; Wardrobe remains unimplemented. |
| Inspected repository | Branch `sprint_02`; commit `4709d6f7d0c718129405732cfe824ccf6c6b2f61` |
| Initial working tree | Clean |
| Selected scope | US-AUTH-005, US-GAR-001, US-WAR-001 and US-WAR-002, exactly |
| Sprint dates / duration | Not supplied; pending team scheduling |
| Capacity / velocity / estimates | Not supplied; no forecast, Story Points or assignments invented |
| Delivery responsibility | Backend implementation, affected contracts, persistence, security and backend verification |
| Planning accountability | Developers maintain the implementation plan; PO/BA clarify product behavior; relevant engineering/QA contributors supply verification |
| Current task boundary | Synchronize approved taxonomy/property requirements and this plan; no implementation, new guides, API/schema artifacts, commit, push or PR |

**Scope authority:** the current Product Owner instruction explicitly selects all four stories. Earlier catalogue/story statements that no Sprint selection was recorded describe their authoring snapshots; they do not make these four items optional now. Their source-file statuses and AC identities remain unchanged. The 2026-11-05 approved taxonomy/property revision updates the three garment/wardrobe stories; US-AUTH-005 remains unchanged. Selection establishes scope, not demonstrated feasibility, implementation success or full-story Done.

**Canonical location:** `docs/06-scrum/sprints/sprint-02/sprint-plan.md` follows the existing [Sprint 1 plan](../sprint-01/sprint-plan.md) and [workflow](../../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 3/32/38. The requested example used `docs/06-delivery/`; the repository uses `docs/06-scrum/`. One primary planning artifact is created under that established convention.

Frontend/mobile coding, screens, navigation, styling, components and frontend test implementation are outside this developer's assignment. Their required acceptance evidence remains visible in this plan. Applicable US-PRIV-001 obligations accompany the four selected stories without becoming a fifth selected story.

## 2. Sprint Goal

> Enable secure recovery of access to a trustworthy private wardrobe by delivering email password reset, confirmed manual garment creation, current-wardrobe browsing and garment inspection through verified backend APIs.

The Goal connects restored private participation with the first useful **Understand My Wardrobe** capability in the [Product Goal](../../product-goal.md), Sections 3–5. Recovery preserves access to the same account; manual confirmation establishes trustworthy possessions; browsing/detail make that information inspectable.

**[REQ]** Backend success means correct-account recovery and authorized creation/read outcomes, truthful failure/unevaluated-eligibility states, preserved accepted data and inspectable automated integration evidence. Full mobile journeys and whole-story Done require the additional evidence identified in Sections 6/11. All four selected stories contribute to this Goal; none is optional, deferred or a stretch item.

## 3. Current Repository and Sprint 1 Baseline Assessment

**2026-11-05 reinspection:** The detailed inventory/findings below were recorded at `4709d6f7d0c718129405732cfe824ccf6c6b2f61` and are historical. At `ae632fd115329cb66560781090b711b85f00cdb3`, `SessionAuthorityOutageIT`, `JwtClaimNames`, converter dependency-outage translation and `IdentityOpenApiContractIT` exist; OpenAPI v0.4.1 includes the registration confirmation field and corrected Identity contract. Earlier missing-file/contract findings must not be treated as current unresolved defects. No runtime/CI pass is inferred from this static reinspection. Wardrobe still contains only its package marker; no garment enum, DTO, API, persistence or migration exists. Migrations remain V1–V5 for the foundation/Identity. The current OpenAPI explicitly excludes Wardrobe. This revision creates no API or physical schema.


### 3.1 Verified implementation, rather than historical descriptions

Inspection covered the complete tracked-file inventory, production packages, test classes/scenarios, dependencies/configuration, all migrations, API specification, CI and relevant canonical documentation/guides. Generated output, caches, binaries and private environment contents were excluded. No build, test suite, service startup, provider integration or hosted CI run was executed for this planning task.

Local `main` and the existing `origin/main` tracking reference point to the inspected commit. Local history contains merge `afe3dcd`, “Merge pull request #2 … sprint_01_security.” Thus Sprint 1 implementation is present in the local main history; this is not a claim about current remote CI, deployed behavior or full-story completion.

| Area | Evidence observed at the inspected commit | Sprint 2 implication |
| --- | --- | --- |
| Runtime/build | [POM](../../../../backend/pom.xml): Java 21, Boot 4.1.1 servlet MVC, Security crypto/resource-server support, web testing, Flyway, PostgreSQL and existing test libraries. [Wrapper](../../../../backend/.mvn/wrapper/maven-wrapper.properties): Maven 3.9.16. Cached Boot 4.1.1 dependency BOM confirms managed Spring Security 7.1.1. [Compose](../../../../compose.yaml)/test configuration use PostgreSQL 17.11. | Reuse the installed conventions. Spring JDBC is already used; do not introduce JPA, another web stack or a replacement build/security foundation. The BOM inspection is version evidence, not runtime verification. |
| Account persistence | [JdbcAccountRepository](../../../../backend/src/main/java/com/capsuleai/identity/adapter/out/persistence/JdbcAccountRepository.java) inserts normalized unique identities and reads accounts by email; [AccountRepository](../../../../backend/src/main/java/com/capsuleai/identity/application/ports/AccountRepository.java) has no credential-update operation. | Reuse account identity; add only the Identity-owned capabilities reset actually needs. |
| Password protection | [PasswordPolicy](../../../../backend/src/main/java/com/capsuleai/identity/domain/PasswordPolicy.java) counts Unicode code points, validates 12–128 and preserves values; [Pbkdf2PasswordHasher](../../../../backend/src/main/java/com/capsuleai/identity/adapter/out/security/Pbkdf2PasswordHasher.java) supplies salted PBKDF2-HMAC-SHA256 through `PasswordHasher`. | Reset must reuse exact-value policy and the existing hash/verification convention. Do not introduce a different password interpretation or assume a denylist exists. |
| HTTP access | [AuthController](../../../../backend/src/main/java/com/capsuleai/identity/adapter/in/web/AuthController.java) groups registration, login, logout and refresh under `/api/v1/auth`, with separate application services. | Extend cohesive APIs; do not create a controller per story. No recovery or wardrobe handler currently exists. |
| JWT/security | [IdentityTokenConfiguration](../../../../backend/src/main/java/com/capsuleai/identity/configuration/IdentityTokenConfiguration.java) configures HS256 verification; [security configuration](../../../../backend/src/main/java/com/capsuleai/identity/configuration/IdentitySecurityConfiguration.java) supplies one default-deny chain. The converter consults published `SessionAuthority` and resolves `AuthenticatedAccount`. | Reuse JWT plus persistent session authority for wardrobe requests. Recovery entry/completion must be reachable without an existing login; its own reset credential governs completion. |
| Persistent sessions | [JdbcSessionRepository](../../../../backend/src/main/java/com/capsuleai/identity/adapter/out/persistence/JdbcSessionRepository.java) supports session creation/read, current-session revocation, locked refresh consumption/replacement. [RefreshSessionService](../../../../backend/src/main/java/com/capsuleai/identity/application/RefreshSessionService.java) commits reuse revocation before returning rejection. | Add account-wide reset effects coherently. Existing current-session revoke is not an account-wide reset implementation. Preserve rotation/reuse and other-account isolation. |
| Database baseline | [V2 account](../../../../backend/src/main/resources/db/migration/V2__create_identity_account.sql), [V3 sessions](../../../../backend/src/main/resources/db/migration/V3__create_identity_session.sql), [V4 revocation](../../../../backend/src/main/resources/db/migration/V4__add_identity_session_revocation.sql), [V5 rotation](../../../../backend/src/main/resources/db/migration/V5__add_refresh_token_rotation.sql), following V1. | No reset or wardrobe schema exists. V5/`RevocationReason` support only logout/reuse reasons; reset requires compatible evolution. Add forward migrations after V5, without rewriting applied files. |
| Ownership/modules | [Identity package](../../../../backend/src/main/java/com/capsuleai/identity/package-info.java) owns access; [Wardrobe package](../../../../backend/src/main/java/com/capsuleai/wardrobe/package-info.java) currently contains only ownership documentation. | Garment/create/browse/detail behavior belongs in `com.capsuleai.wardrobe`. No existing garment implementation can be credited or recreated. |
| Tests/CI | Domain/application, password, schema, API/security, registration and refresh concurrency tests exist. [IdentityIntegrationTestSupport](../../../../backend/src/test/java/com/capsuleai/identity/support/IdentityIntegrationTestSupport.java) combines real application/security, MockMvc, PostgreSQL and controlled time. [ArchUnit](../../../../backend/src/test/java/com/capsuleai/architecture/ArchitectureDependencyTest.java) checks inward and cross-owner dependencies. | Reuse infrastructure and maintain regressions; new private wardrobe paths need real owner-isolation evidence, beyond the existing test-only identity probe. Existing test source is not evidence that tests passed. |

### 3.2 Confirmed static inconsistencies and limitations

| Finding | Exact evidence / distinction | Planning treatment |
| --- | --- | --- |
| Required CI suite is absent | [CI workflow](../../../../.github/workflows/backend-ci.yml) requires `TEST-com.capsuleai.identity.adapter.in.security.SessionAuthorityOutageIT.xml`. No corresponding test class exists in current test sources. | The declared clean-source test discovery cannot supply that suite. Resolve the source/report mismatch through subsequent engineering work before claiming CI/DoD satisfaction; do not count an upload or unrelated passing suite as success. No CI change is made here. |
| Registration contract disagrees with implementation/requirements | [OpenAPI](../../../05-api/openapi.yaml) v0.4.0 documents `POST /api/v1/auth/register` and omits `confirmPassword` from its registration schema. Controller, security matcher and integration tests use `POST /api/v1/auth/registrations`; registration requires confirmation under FR-AUTH-001/FR-AUTH-012. | Reconcile the existing contract before extending it. Do not rename working routes or remove required confirmation to imitate the stale specification. Final synchronized contract/tests must reflect authoritative requirements and actual supported behavior. |
| Authority-outage guide change is absent from code | Sprint 1 shared guide TD-26 describes a sanitized filter-path 503 and `SessionAuthorityUnavailableException`. The actual [converter](../../../../backend/src/main/java/com/capsuleai/identity/adapter/in/security/SessionAwareJwtAuthenticationConverter.java) has no outage translation; the [entry point](../../../../backend/src/main/java/com/capsuleai/identity/adapter/in/security/ProblemJsonAuthenticationEntryPoint.java) writes a 401 body. | The documented outage behavior is unverified and not implemented by the described classes. Verify/resolve this affected security/error boundary during the first integration work. No runtime status is claimed from this static inspection alone. |
| JWT-constant guide change is absent | Shared guide TD-25 describes `identity.adapter.jwt.JwtClaimNames`; that class is absent. The inbound converter imports the outbound issuer for its claim constant. | Record guide/code drift; consult actual paths and evaluate the established adapter dependency convention when touching those files. Do not assume the guide's final map exists or turn this into architectural restructuring. |
| README/plan snapshots are historical | [Backend README](../../../../backend/README.md) and Sprint 1 planning still describe an infrastructure-only product baseline. Guide README/shared design use an earlier analysed commit and explicitly say their reference code was not executed by the author. | Use source/history above for implementation facts. Reuse guide structure and valid decisions, not obsolete “not implemented” or “resolved” claims. |
| External and integrated evidence is missing | No recovery email adapter, reset model, wardrobe feature code, mobile client or Sprint 1 Review/Retrospective artifact is present in the inspected inventory. | Do not infer delivery/provider readiness, client acceptance or a fully Done Sprint 1 Increment from merged code. Sprint 2 retains the corresponding evidence obligations. |

The baseline can be extended; it cannot yet be described as fully validated. These findings remain bounded integration/readiness work supporting the selected stories, not new PBIs or a replacement architecture phase.

## 4. Governing Sources and Traceability

| Label | Meaning in this plan |
| --- | --- |
| **[REQ]** | Existing business/product/software requirement, Business Rule or source Story AC. Summaries here do not replace the original wording. |
| **[ARCH]** | Established selected architecture, owning views and module boundaries. |
| **[TECH]** | Proposed engineering task or JIT implementation direction; exact mechanisms are not selected by this plan. |
| **[OPEN]** | Unresolved decision, evidence gap or feasibility question, with resolution responsibility in Section 12. |

| Governing source | Application to Sprint 2 |
| --- | --- |
| [Canonical workflow](../../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 26–34/38–41/46 | Selection precedes story-specific JIT design; automated tests accompany implementation; documentation evolves with truth. No mandatory baseline ADR or complete Sprint-wide design packet. |
| [Product Goal](../../product-goal.md); [Product Backlog](../../product-backlog.md), PBI-004–PBI-007; [Story Catalogue](../../stories/README.md) | Product value, parent mapping and hard capability dependencies. No backlog reordering or new stories. |
| [Delivery Decisions](../../delivery-decisions.md), DD-001/DD-003/DD-005/DD-006; [focused refinement review](../../backlog-refinement-review.md), Q-02–Q-05 | Preserve coherent outcomes, technical ownership, evidence limits and unresolved delivery questions. The present PO selection supersedes earlier absence-of-selection statements, not substantive AC. |
| [BRD](../../../01-business/BRD.md), BG-01, BR-002–BR-006, BR-021–BR-023, CAP-01/CAP-03/CAP-04; [PRD](../../../02-product/PRD.md), JRN-01–JRN-03, FEAT-AUTH-001/FEAT-AI-002/FEAT-GAR-001/FEAT-WAR-001 | Private participation, manual confirmation, trustworthy inventory and understandable limitations. Reported-use presentation is conditional, not authority to implement analytics. |
| [SRS](../../../03-requirements/SRS.md), Sections 3.1/3.4–3.6/4/5.4/6/9; [Business Rules](../../../03-requirements/business-rules.md), AUTH/GAR/HIST families | Precise reset, garment, data-integrity, ownership, unevaluated eligibility, privacy and failure behavior. Story-specific anchors follow below. |
| [UC-004](../../../03-requirements/use-cases/UC-004-reset-password.md) / [AD-004](../../../03-requirements/activity-diagrams/AD-004-reset-password.puml); [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md) / [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); [UC-008](../../../03-requirements/use-cases/UC-008-view-wardrobe.md) | Reset interaction; manual branch/shared confirmation only; browsing/detail branches only. UC-008 search/filtering is outside these selected slices. No dedicated UC-008 activity is present. |
| [Selected Architecture Decisions](../../../04-architecture/selected-architecture-decisions.md), ADA-001/003/006/008/009/015–018; [ADD](../../../04-architecture/ADD.md), Sections 9.1–9.6/9.8–9.9; [Logical](../../../04-architecture/views/logical-view.puml), [Implementation](../../../04-architecture/views/implementation-view.puml), [Data](../../../04-architecture/views/data-view.puml), [Deployment](../../../04-architecture/views/deployment-view.puml) | One modular monolith/PostgreSQL, Identity and Wardrobe ownership, explicit ports, local transactions, private models and purpose-minimal provider exchange. |
| [Backend conventions](../../../engineering/backend-conventions.md); [OpenAPI](../../../05-api/openapi.yaml); current production/test/configuration paths in Section 3 | Cohesive thin controllers, scoped error translation, actual persistence/security style and living contracts. OpenAPI disagreements are exposed, not silently selected over requirements. |
| [Test Strategy](../../../07-testing/test-strategy.md), Sections 8–9/11/13–18/21–22; [DoD](../../definition-of-done.md), Sections 3–8 | Real persistence/security/provider evidence, source-linked tests, integrated completion and remaining client criteria. |
| [Sprint 1 plan](../sprint-01/sprint-plan.md), [guide README](../sprint-01/implementation-guides/README.md), [shared architecture](../sprint-01/implementation-guides/shared-architecture.md), [registration guide](../sprint-01/implementation-guides/US-AUTH-001-guide.md), [login guide](../sprint-01/implementation-guides/US-AUTH-002-guide.md), [continuity guide](../sprint-01/implementation-guides/US-AUTH-003-guide.md), [logout guide](../sprint-01/implementation-guides/US-AUTH-004-guide.md) | Structural reference and reusable implementation conventions, checked against current code. TD-01/TD-02/TD-03/TD-04/TD-09/TD-19/TD-27 inform reuse; TD-25/TD-26 illustrate the drift recorded in Section 3. |

**[REQ] Traceability practice:** source requirement/rule → selected Story/AC → task and verification → tested revision/environment/results. Use existing work-item/PR/report evidence later; create no competing requirement catalogue. The future guide's complete source quotations are source snapshots, not a new authority.

## 5. Selected Sprint 2 Backlog

Implementation priority below is the recommended sequence within the selected scope, not a change to the Product Backlog's global order.

| Priority | Story / exact title | Parent PBI | User value / backend objective | Expected deliverables | Dependencies | Verification / backend acceptance |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | [US-AUTH-005 — Recover account access through email](../../stories/auth/US-AUTH-005-recover-account-access.md) | PBI-005 | Restore correct-account access through completed email recovery, not merely instruction delivery. | Account-safe initiation, real delivery boundary, reset validity/replacement/consumption, accepted password change and account-wide session revocation; affected API/migrations/tests. | Existing account/login/session authority from PBI-004; Email Delivery Service and usable instruction-opening contract. | AC-01–AC-16 mapping below; atomic reset effects, old authority denied, new-password login, actual delivery evidence and no disclosure. |
| 2 | [US-GAR-001 — Add an owned garment manually](../../stories/garment/US-GAR-001-add-garment-manually.md) | PBI-006 | Establish one confirmed owned garment without image/AI dependency. | Eight-category confirmed minimum with optional valid subtype/Brand/purchase Price, owner-associated persistence, authority/provenance and explanation that eligibility has not been evaluated, durable safe retry, protected API/migrations/tests. | PBI-004 usable access; Wardrobe ownership and private-data protections. Recovery completion is not a technical prerequisite. | AC-01–AC-14; accepted minimum-only entry survives reread, no unconfirmed/duplicate effect, bounded values and owner isolation. |
| 3 | [US-WAR-001 — Browse my current wardrobe](../../stories/wardrobe/US-WAR-001-browse-current-wardrobe.md) | PBI-007 | Recognize current possessions and distinguish genuine emptiness from unavailable access/data. | Authorized read-only collection, identifiable imageless/minimum-only summaries, current-ownership filtering, truthful empty/failure contract and tests. | PBI-004. Manual creation provides useful populated fixtures, not a hard gate to empty browsing. | AC-01–AC-11; only the current account's confirmed active entries; successful empty result distinct from failure; no read side effects. |
| 4 | [US-WAR-002 — Inspect an owned garment](../../stories/wardrobe/US-WAR-002-inspect-owned-garment.md) | PBI-007 | Understand the accepted profile, eligibility under a future approved policy and limits of use evidence. | Owner-scoped detail, available accepted information/provenance, conditional readiness, explicit unavailable use evidence, changed-current-state/failure semantics and tests. | PBI-004 and an actual current owned garment. Browse/add are useful routes; Wear/analytics are not prerequisites. | AC-01–AC-12; correct current garment, retained imageless/incomplete detail, no fabricated usage/readiness or read mutation. |

**[REQ]** Keep account recovery one coherent request-to-restored-access story and keep browsing/detail as distinct outcomes (DD-003). Applicable privacy is included as work within these four rows; it is not a fifth backlog item.

## 6. Story-by-Story Backend Scope

The following are **planning summaries** of every original AC, with proposed verification and external limits. Read the linked complete current story for normative wording; the approved taxonomy/property revision is synchronized here while all AC IDs are retained. All evidence described is required/planned, not already passed.

### 6.1 US-AUTH-005 — Recover account access through email

**Outcome / deliverables:** extend Identity from the existing account and session model to complete secure email recovery. Reuse normalized identity, `PasswordPolicy`, `PasswordHasher` and existing login/authority verification. Add necessary recovery persistence, delivery port/adapter, application coordination, contract and tests; exact interfaces and representations remain JIT.

| Original AC | [REQ] Backend acceptance condition | [TECH] Verification / external residual |
| --- | --- | --- |
| AC-01 | Equivalent account-safe initiation responses for known/unknown normalized identities; no implication of password change. | Paired HTTP/application tests including delivery-failure paths; Vietnamese conditional wording/presentation remains client evidence. |
| AC-02 | Instructions target the correct normalized email; 30-minute/single-success validity; issuance/delivery changes neither password nor sessions. | Real persistence plus controlled delivery/actual adapter compatibility; inbox receipt and opening evidence must identify the real/substituted boundary. |
| AC-03 | Only email and necessary recovery information crosses the delivery boundary. | Adapter request and diagnostic inspection; no wardrobe/profile/history payload or unrelated secret. |
| AC-04 | Valid accepted completion changes the password, consumes the interaction and requires new authentication; old password fails, exact new password reaches the existing account. | Application, real PostgreSQL and HTTP login integration; actual client return-to-login/private-area journey remains external. |
| AC-05 | Revoke all affected-account Refresh Sessions; their unexpired JWTs and newer refresh credentials cannot bypass revocation. Retain garments/other accepted data and another account's sessions. | Two accounts, multiple sessions, real security/refresh checks and before/after accepted-state assertions. |
| AC-06 | A valid already-issued interaction can enter recovery without another issuance. | Direct-open/validation contract checks; actual mobile interaction opening remains external. |
| AC-07 | Successfully issuing a replacement invalidates all earlier unused interactions for that account. An unconfirmed request is not proof of replacement. | Committed multi-interaction tests and competing issuance/completion cases. |
| AC-08 | Invalid, expired, consumed and replaced interactions cannot change credentials or sessions; reject at the 30-minute boundary. | Controlled-time/open/submission and replay cases, with unchanged-state assertions and safe errors. |
| AC-09 | Recheck validity at submission; earlier opening does not preserve validity after expiry/replacement. | Advance clock or replace between opening/submission; verify no consumption/password/revocation on rejection. |
| AC-10 | Enforce 11/12/128/129 boundaries; permit policy-valid Unicode/spaces without class-mixture rules. Invalid new password does not consume a still-valid interaction. | Domain/API cases and corrected resubmission; no denylist assumed adopted. |
| AC-11 | Preserve the exact accepted new password. | Real existing hasher and login integration, including transformed variants rejected. |
| AC-12 | Leaving before completion submission causes no password change or completion-based revocation. | Backend read/validation operations have no completion effects; cancellation controls/navigation remain external. |
| AC-13 | Missing instructions, unknown email and failed delivery retain safe initiation semantics; no credential change/revocation merely from request/delivery. Independent access remains usable. | Failure doubles plus real adapter checks; mailbox check/retry/restart UI remains external. |
| AC-14 | Known-unaccepted completion leaves prior credentials/session state intact; no false success. | Application and real transaction rollback/failure checks. |
| AC-15 | Lost response is unconfirmed, not proof of success or unchanged credentials; consumed interactions cannot perform a second change. | Faults before/after commit and replay/restart evidence; user-facing outcome review/recovery remains client evidence. |
| AC-16 | Contract retains distinct requested/delivered/unconfirmed/completed meanings without disclosure. | Stable safe semantics support integration; Vietnamese screen-reader/200% text operation requires client/device evidence. |

**Primary source anchors:** BR-021/BR-023, CAP-01; FR-AUTH-003/FR-AUTH-004/FR-AUTH-008/FR-AUTH-009/FR-AUTH-015/FR-AUTH-017/FR-AUTH-018; DATA-AUTH-001/DATA-AUTH-005; SI-003, COM-001–COM-003, ERR-AUTH-002/ERR-AUTH-003/ERR-NET-001/ERR-DEP-001; BRULE-AUTH-002–BRULE-AUTH-007; UC-004/AD-004 and UC-002 login handoff.

**Resolved 2026-11-07 (Product Owner):** instruction link, email wording, rate limiting, timing/partial-failure handling, denylist deferral, 30-day retention and provider-neutral SMTP with real delivery evidence before Done; see Section 12.3. **[Historical OPEN, resolved]** Required JIT decisions: delivery provider/access and instruction-opening contract; reset-secret representation/protection; durable issuance/replacement and accepted-completion coordination; public request/error semantics; safe uncertain-outcome recovery. Do not select SMTP/provider SDKs, token lengths, retention periods or new recovery methods here.

### 6.2 US-GAR-001 — Add an owned garment manually

**Final approved scope — Option B:** implement every applicable property below for manual input, validation, confirmation, PostgreSQL persistence, safe retry and rereading. Exact source values come from the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md).

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

The prior minimum/subtype/Brand/Price-only boundary and readiness algorithm are superseded. Optional means the user may omit a field, not that the developer may omit its support. Source FR-GAR-001–013 retains FR-GAR-005 as superseded; DATA-GAR-001–005, BRULE-GAR-001–010 and current Story AC govern behavior.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

**Outcome / deliverables:** introduce the selected Wardrobe behavior around explicit confirmation, owner-associated accepted information and safe retry. Category/color confirmation establishes Saveable ownership; recommendation eligibility is not evaluated; all applicable optional properties are supported and preserved.

| Original AC | [REQ] Backend acceptance condition | [TECH] Verification / external residual |
| --- | --- | --- |
| AC-01 | exactly one owned garment is acknowledged, all accepted applicable values are durably preserved and available for rereading, and recommendation eligibility remains not evaluated. | Application, persistence round-trip and authenticated HTTP evidence; human review/confirmation presentation remains external. |
| AC-02 | the garment is saved through the same manual outcome without requiring image capture, upload, preview, AI proposals or successful analysis. | Omitted-image fixtures and no analysis/media dependency on the manual path; no artificial provider setup. |
| AC-03 | the review reflects the latest supported values and permits explicit confirmation of that revised minimum. Merely entering or changing a draft does not establish a confirmed owned garment. | Command/acceptance validation and retained accepted values; pre-submit draft editing/display remains client responsibility. |
| AC-04 | use the complete source lists: every category subtype, exactly ten Dresses/Jumpsuits choices, all Colors/Materials/Patterns/named Styles and category details. Validate parent/applicability; reject unsupported selections with correction/clear guidance, without silent replacement, invented fallback or Etc value. Color is exactly one dominant selection, including Colorful. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-05 | CapsuleAI identifies the missing confirmation/information and retains the draft for correction. No authoritative garment is created, and existing wardrobe information remains unchanged until accepted creation. | Unit/API invalid-input and no-write checks; draft retention/correction interaction remains external where client-held. |
| AC-06 | omission does not block saving. Support all 13 properties in this Sprint: optional Subtype, zero or multiple distinct Materials, one optional Pattern, Style, applicable Fit/Neckline/Sleeve Length/Length/Silhouette, Brand text and purchase Price. Material duplicates add no semantic material; no percentages/proportions are fabricated. Price uses decimal amount plus explicit ISO 4217 currency together or is cleared; missing Price is unknown, not zero. Preserve all accepted values without float calculations, image/location inference or currency conversion. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-07 | Fit, Neckline and Sleeve Length apply only to Tops; Length uses the separate Tops/Pants lists and Silhouette applies only to Pants. Other categories receive no such fields. Invalid/inapplicable supplied information is identified for correction or clearing rather than silently discarded; applicable optional omission remains valid. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-08 | no readiness predicate is evaluated or required. Do not label the garment ready, invalid, unsuitable or ineligible because a future engine policy is undefined or optional values are missing. Preserve normal wardrobe access and truthful missing-value information; no mandatory readiness enum/column is prescribed. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-09 | the accepted values remain authoritative for my garment. Provenance reflects how those values were established where relevant, and automated output cannot silently replace them; this includes Brand and Price. Price is never inferred from a photograph and a suggested Brand is never accepted automatically; this story does not require automated analysis. | Create/read model tests; future real AI overwrite regression remains unexercised while AI is absent, not a reason to implement it now. |
| AC-10 | no confirmed wardrobe entry is created from that draft and my previously accepted garments remain unchanged. | No creation from an unaccepted command; actual cancellation/navigation evidence is external. |
| AC-11 | CapsuleAI distinguishes failure from successful creation, retains useful entered work where practical, and offers a safe continuation/retry. No confirmed garment is claimed and existing accepted garments remain unchanged. | Fault/rollback/API cases; useful client draft preservation and continuation remain external. |
| AC-12 | CapsuleAI distinguishes pending/unconfirmed acceptance from known failure and success, preserves useful work where practical, and lets me establish whether creation was accepted before safe retry. Repeated delivery or retry of the same intended addition has at most one accepted wardrobe effect; uncertainty does not establish definite unchanged inventory. | Durable PostgreSQL logical-action/outcome and restart/response-loss cases; user review/pending presentation remains external. |
| AC-13 | CapsuleAI pauses protected use, explains the need for usable authorized access, and exposes no other user's wardrobe. It does not label inaccessible data empty or an interrupted submission accepted; the goal can resume after valid authentication with outcome review where acceptance is uncertain. | Actual signed-token/security/owner integration and no unauthorized mutation; renewed-login presentation remains external. |
| AC-14 | the fields, correction and confirmation actions and result states remain operable and understandable through meaningful labels/states beyond color alone. Guidance distinguishes draft, saved ownership, failed/unconfirmed save and eligibility under a future approved policy. | Vietnamese accessible fields/confirmation/guidance and 200% scaling remain client/device evidence. |

**Primary source anchors:** BG-01, BR-002–BR-005, CAP-03/CAP-04; FR-AI-003/FR-AI-008–FR-AI-010, FR-GAR-001–FR-GAR-006/FR-GAR-011–FR-GAR-013, FR-AUTH-008; DATA-GAR-001–DATA-GAR-005, DATA-INT-001/DATA-INT-004, ERR-GAR-001/ERR-GAR-002/ERR-NET-001, COM-001/COM-002; BRULE-AUTH-001/BRULE-AUTH-007 and BRULE-GAR-001–BRULE-GAR-006; UC-007/AD-007 manual/shared confirmation branches.

**[OPEN] Required JIT decisions:** confirmation representation, owner-scoped logical-action identity/outcome review, minimum persistence and accepted property/provenance representation, error/response contract. Do not deduplicate by category/color alone: two separate intended additions may describe distinct similar garments. A timeout alone does not prove no save occurred.

### 6.3 US-WAR-001 — Browse my current wardrobe

**Outcome / deliverables:** an authorized, read-only current collection with identifiable minimum-only entries and honest retrieval outcomes. Reuse Wardrobe ownership and accepted-property meanings established during manual creation without making all creation work a prerequisite to an empty collection.

| Original AC | [REQ] Backend acceptance condition | [TECH] Verification / external residual |
| --- | --- | --- |
| AC-01 | summaries identify the correct possessions using accepted Category, Color and available identifying information such as Subtype/image. Missing optional properties or images never hide a saved item. Detail can retrieve all available applicable accepted properties; the collection need not return every detailed property. | Owner-scoped JDBC/API mapping; cards/visual recognition remain client evidence. |
| AC-02 | it remains identifiable and selectable. Eligibility is not evaluated; do not infer ready, invalid, unsuitable or ineligible from missing optional information or undefined future engine policy. | Minimum-only fixtures and no evaluated-eligibility assertions; accessible selection remains external. |
| AC-03 | CapsuleAI identifies loading and does not claim that I own no garments. A pending request is not a completed empty result. | No fabricated empty success for incomplete/failed retrieval; loading state itself is client evidence. |
| AC-04 | CapsuleAI explains the confirmed empty state and the value of adding garments, offers Add Garment, and lets me leave without a fixed garment-quota requirement. | Real empty/populated account cases; explanatory text/Add action/leave behavior remain external. |
| AC-05 | CapsuleAI identifies unavailable data and offers retry or return to usable prior context. It does not replace the failure with an empty wardrobe/no-match claim or delete/change accepted garments; retry can present the actual inventory when retrieval succeeds. | Repository/application/API failure and recovery cases; retry/return UI remains external. |
| AC-06 | CapsuleAI hands off to the established garment-entry goal, including the available manual route. The handoff itself creates no garment and does not impose a quota or optional-profile completion gate; manual creation is covered by US-GAR-001. | No read-side mutation; actual Add navigation belongs to the client and manual creation to US-GAR-001. |
| AC-07 | CapsuleAI hands off that garment to US-WAR-002's detail outcome and provides a return path to browsing. Selection alone changes no accepted garment information or ownership. | Collection/detail identity integration; navigation and return path remain external. |
| AC-08 | only confirmed active owned garments are presented as current possessions. A retained historical snapshot does not restore ownership, and hypothetical or unconfirmed information is not treated as an accepted addition; creating/removing those other representations is outside this story. | Controlled current/noncurrent owner fixtures; no new removal/history/candidate product features. |
| AC-09 | CapsuleAI pauses protected use, gives renewed-authentication guidance and does not expose another account's entries or label inaccessible inventory empty. After usable authorized access is established, I may resume browsing the correct account. | Real security/ownership cases, including reset-revoked sessions; client reauthentication/resumption remains external. |
| AC-10 | browsing creates no addition, edit, removal, Wear Event or preference feedback and does not change accepted inventory or use evidence. | Before/after accepted-state assertions; no implicit behavioral event implementation. |
| AC-11 | these entries, actions and states remain operable and understandable through meaningful accessible names/roles/states and information beyond image or color alone; missing image, unevaluated eligibility and unavailable retrieval retain their distinct meanings. | Vietnamese accessible browsing/Add/detail controls and 200% scaling remain client/device evidence. |

**Primary source anchors:** BR-003/BR-005/BR-021/BR-022, CAP-03/CAP-04; FR-WAR-001 and FR-WAR-008 empty/loading/unavailable slice only; FR-GAR-004/FR-AUTH-008/FR-AUTH-009; DATA-GAR-001/DATA-GAR-004/DATA-GAR-005, ERR-AUTH-001/ERR-NET-001, COM-001; BRULE-AUTH-001/BRULE-AUTH-007, BRULE-GAR-001–BRULE-GAR-003, BRULE-HIST-002; UC-008 browsing and UC-007 Add handoff only.

**Resolved 2026-11-07 (Product Owner):** the overview uses cursor pagination, newest first (OPEN-S2-10, US-WAR-001 AC-12). **[Historical OPEN, resolved]** Required JIT decisions: collection contract and current-state query/projection/error semantics. No user-selectable sorting, search/filtering, analytics or removal implementation is selected by this story.

### 6.4 US-WAR-002 — Inspect an owned garment

**Outcome / deliverables:** read the selected current garment's accepted information, eligibility under a future approved policy and actual use-evidence availability. Missing descriptors/images do not make a saved garment inaccessible.

| Original AC | [REQ] Backend acceptance condition | [TECH] Verification / external residual |
| --- | --- | --- |
| AC-01 | return the correct accepted Category, Subtype, Color, Material selections, Pattern, Style, applicable Fit/Neckline/Sleeve Length/Length/Silhouette, Brand and purchase Price with currency. Missing optional values stay missing; multiple distinct Materials and exact accepted labels/amounts are preserved. Available image and useful metadata remain separate; suggestions never replace confirmed values. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-02 | it remains identifiable and inspectable through accepted information. Missing image, unavailable descriptors and unevaluated eligibility retain their distinct meanings; no image, Brand, Price, default currency, material, color or other descriptor is fabricated to make the profile look complete. Missing Price is unknown, not zero; displayed Price means the user-reported purchase price, not current retailer or market value. | Explicit missing-data fixtures and response assertions. |
| AC-03 | communicate that recommendation eligibility has not been evaluated. Neither optional absence nor undefined engine policy establishes ready, unsuitable, invalid or ineligible status; no removed property or enrichment gate is imposed. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-04 | show Fit/Neckline/Sleeve Length only as applicable to Tops, Length under its Tops/Pants vocabulary, and Silhouette only for Pants. Preserve all globally applicable accepted properties, including multiple Materials, without inventing inapplicable values or inferring fields from Subtype. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-05 | preserve the usable accepted profile, distinguish absent values from an unavailable request, and fabricate no data or evaluated eligibility. Category/Color confirmation remains sufficient for saving; detail viewing changes no accepted value or ownership. | Source-vocabulary/applicability and omission/preservation cases, real persistence/reread and owner isolation; verify no evaluated eligibility claim. Client presentation remains separate evidence. |
| AC-06 | indicators reflect accepted effective Wear Events and their reporting limits. No recorded events is described as no recorded use rather than proof of never having physically worn the garment; information is not fabricated from missing reports. This presentation consumes available evidence and does not implement reporting or utilization analytics. | Conditional source/contract fixtures when that presentation exists; do not build reporting/analytics or claim an absent real integration was exercised. |
| AC-07 | CapsuleAI retains usable accepted profile / unevaluated eligibility inspection and does not manufacture zero use, last-worn information or a no-recorded-events conclusion from unavailable evidence. Availability of future reporting/analytics is not a gate to viewing this garment. | Profile/readiness succeeds with explicit unavailable use meaning. No production Wear capability currently exists. |
| AC-08 | CapsuleAI distinguishes loading from unavailable detail, offers applicable retry or return to usable prior context on failure, and does not call the wardrobe empty or the selected garment deleted merely because retrieval failed. Accepted wardrobe information remains unchanged. | Failure/retry/no-mutation tests; loading and recovery navigation remain external. |
| AC-09 | it explains the changed/no-longer-current item and refreshes the current wardrobe where possible, allowing return to browsing. A minimal historical snapshot stays separate and does not restore current ownership; this story does not perform removal or implement historical views. | Controlled stale-selection/noncurrent fixtures; exact safe error contract JIT, refresh/return UI external. |
| AC-10 | CapsuleAI offers the return path and inspection alone creates no addition, edit, removal, Wear Event or preference feedback. Accepted values and ownership remain unchanged by viewing. | Read-only state assertions; visible return path remains external. |
| AC-11 | CapsuleAI pauses protected use, explains the need for usable authorized access and exposes no other account's garment information. It does not present inaccessible detail as an empty wardrobe; inspection can resume for the correct account after valid authentication. | Real security/owner checks, guessed identifiers and reset-revoked credentials; client resumption remains external. |
| AC-12 | the information, state meanings and actions remain operable and understandable through meaningful accessible names/roles/states beyond image or color alone. Saved ownership, unevaluated eligibility, uncertain descriptors and no-recorded/unavailable use evidence remain distinguishable. | Contract tests support client integration; Vietnamese assistive-technology/200% evidence remains external. |

**Primary source anchors:** BR-003/BR-005/BR-006/BR-021/BR-022, CAP-03/CAP-04; FR-WAR-001/FR-WAR-002, FR-GAR-004–FR-GAR-006, FR-AUTH-008/FR-AUTH-009; DATA-GAR-001/DATA-GAR-003–DATA-GAR-005, ERR-GAR-002/ERR-AUTH-001/ERR-NET-001, COM-001; BRULE-AUTH-001/BRULE-AUTH-007, BRULE-GAR-001–BRULE-GAR-003/BRULE-GAR-006, BRULE-HIST-001/BRULE-HIST-002; UC-008 detail/readiness/use/return branches.

**[ARCH]** If a real reported-use contributor later exists, detail composition consumes its published contract, not another owner's repositories or private models. For this Sprint the actual missing capability must be represented honestly; no Outfit Advice and Use implementation is added to satisfy this conditional presentation (Implementation View; US-WAR-002 AC-06/AC-07).

## 7. Applicable Business Rules, Security, and Privacy

**[REQ] Taxonomy/property trace:** BR-002/003/005 → FEAT-AI-002/FEAT-GAR-001/FEAT-WAR-001 → FR-GAR-001–006/011–013, DATA-GAR-002 and BRULE-GAR-004/010 → US-GAR-001 AC-01/03/04/06–09, US-WAR-001 AC-01/02, US-WAR-002 AC-01–05 → selected-story JIT representation and the verification in Section 11. UC-007/008 and AD-007 retain authority, correction, failed/uncertain acceptance and owner access. Future editing remains PBI-008/UC-009.

| Classification / source | Required treatment |
| --- | --- |
| **[REQ]** BRULE-AUTH-002/BRULE-AUTH-003; FR-AUTH-015 | Reuse trim/case-insensitive identity without alias transformations and exact password values. Existing code-point counting/PBKDF2 are implementation conventions; no new composition rule or denylist is inferred. |
| **[REQ]** BRULE-AUTH-004/BRULE-AUTH-005; FR-AUTH-003/FR-AUTH-004/FR-AUTH-017/FR-AUTH-018 | Reset lasts 30 minutes and permits one successful use. Successful new issuance replaces all earlier unused interactions. Only accepted completion changes password, consumes the interaction and revokes all account sessions; request/opening/delivery is not completion. |
| **[REQ]** BRULE-AUTH-001/BRULE-AUTH-006; FR-AUTH-007–FR-AUTH-010/FR-AUTH-013/FR-AUTH-014 | Preserve JWT plus usable persistent authority, 15-minute access/absolute 30-day session policy and distinct logout/reuse/reset revocation scopes. New wardrobe APIs cannot trust a caller-supplied owner identifier or JWT validity alone. |
| **[REQ]** BRULE-GAR-001–010; FR-GAR-001–013; DATA-GAR-001–005 | All 13 applicable properties and complete source lists are supported/preserved; Category/one Color plus confirmation save, other values are optional, and eligibility is not evaluated. Superseded FR-GAR-005 creates no active gate. |
| **[REQ]** BRULE-HIST-001/BRULE-HIST-002; FR-WAR-001/FR-WAR-002 | Reads show current possessions; historical snapshots and hypothetical/draft data are different. No recorded use is not physical nonuse; unavailable reporting is not a zero. |
| **[REQ]** DATA-INT-001/DATA-INT-004, COM-002, ERR-NET-001 | Preserve accepted state, distinguish known failure from uncertain acceptance, coordinate safe retries durably and prevent duplicate effects of one intended creation. No general offline or unconditional retry-success guarantee. |
| **[REQ]** NFR-SEC-001–NFR-SEC-003, NFR-PRIV-001/NFR-PRIV-002, COM-001/COM-003, SI-003, BRULE-AUTH-007 | Zero successful unauthorized cross-account access in agreed scenarios; protect exchanges, passwords and reset/token secrets. Purpose-minimal email delivery only; no personal-data training/improvement, sale, public wardrobe or unrestricted sharing. |

**[REQ] Privacy work inside the four stories:** apply [US-PRIV-001](../../stories/privacy/US-PRIV-001-protect-first-private-operations.md) AC-01/AC-02 to the new create/list/detail and reset-revocation boundaries; AC-03 to reset secrets, email exchange and diagnostics; AC-04 to necessary processing and optional information; AC-05 to applicable delivered data lifecycle; AC-06 to bounded, revision-specific evidence. Do not declare the whole privacy story/PBI-002 complete or count absent image/history/measurement features as passing checks.

Necessary active-account/wardrobe data remains purpose-limited (DATA-RET-002). This scope adds no account-deletion/export, garment-removal, image-purge, measurement pipeline or arbitrary reset-record retention period. Future owning lifecycle obligations remain in their sources, not waived.

**[TECH] Security evidence must extend beyond the Identity probe:** authenticate real wardrobe operations through the existing filter chain, then verify affected owner isolation in Wardrobe. After accepted reset, exercise those same operations using old unexpired JWTs and refreshed credentials and verify denial; new-password login restores access to the unchanged wardrobe. Keep secrets out of API errors, logs, test reports and review captures.

## 8. Dependencies and Recommended Implementation Sequence

Retain the requested order: **US-AUTH-005 → US-GAR-001 → US-WAR-001 → US-WAR-002**.

| Step | Implementation focus | Actual dependency / reuse |
| --- | --- | --- |
| Initial bounded checks, with the first story | Reproduce the relevant baseline behavior later, reconcile the contract/CI/security findings in Section 3 and establish recovery delivery/opening feasibility. | Supporting work for affected integration, not a new preparation phase or a whole-Sprint design gate. No claim that all issues are already fixed. |
| 1 — Recovery | Story-specific JIT design, automated tests and implementation of initiation/delivery/valid completion/revocation. | Existing account/login/session/password capabilities; no garment prerequisite. Verify revocation with the existing security probe initially, then real wardrobe operations as they arrive. |
| 2 — Manual addition | Establish confirmed garment ownership, all 13 applicable properties and durable safe acceptance. | Existing usable access and Wardrobe module boundary; recovery is an independent capability, not a technical gate. |
| 3 — Browsing | Reuse accepted Wardrobe state and accepted properties for owner-scoped current summaries. | Authentication is hard input. Empty collection behavior can be developed independently; manual creation supplies real populated cross-story evidence. |
| 4 — Detail | Reuse the same accepted model and stable garment identification. | A current owned garment is the actual input. Browse provides selection/return; a particular creation method and future Wear/analytics do not gate detail. |
| Continuous integration | Connect accepted creation → list → detail; reset → old authority denied → new login → same wardrobe retained. | Required combined evidence; testing/review begin with each story, not only at this final cross-story check. |

Each story uses **Selected Story → JIT Detailed Design → Automated Tests + Implementation → Refactor → Integration Verification → PR / CI / Review**. Finish only the necessary detail for the current story before dependent code; do not first design all four APIs/schemas/sequences.

An email dependency problem does not create a technical dependency blocking independent Wardrobe work. It remains a visible recovery risk with an owner; all four selected stories remain in scope. This plan creates neither replacement work nor a reduced-scope fallback.

## 9. Backend Engineering Task Breakdown

**[TECH]** These are proposed engineering tasks beneath the four selected stories. They introduce no new PBI/Story, estimate or assignee. Source AC in Section 6 supplies the acceptance boundary.

| Selected story / task grouping | Practical engineering work | Required output / verification |
| --- | --- | --- |
| US-AUTH-005 — current integration and JIT contract | Review actual Identity paths and baseline gaps; define only recovery's public request/open/completion/failure semantics, provider boundary and necessary persistence/lifecycle/concurrency detail. | Source-linked decisions; affected OpenAPI aligned with current required registration behavior; CI/outage mismatch tracked to resolution without weakening protection. |
| US-AUTH-005 — interaction authority | Model issued/expired/consumed/replaced outcomes with the existing clock and normalized identity; persist secure account-associated reset authority and coherent replacement. | Policy tests, fresh/upgrade migration and real competing-issuance/single-use/expiry evidence. No password/session mutation from issuance/opening. |
| US-AUTH-005 — email adapter | Define a narrow recovery-delivery port and the adapter for the chosen real delivery mechanism, necessary private configuration and safe failure translation. | Deterministic adapter/protocol tests plus actual configured delivery compatibility/receipt evidence. Controlled failure substitutes are explicitly labelled. |
| US-AUTH-005 — accepted completion | Reuse password policy/hasher; extend owned account/session operations and supported revocation reasons; coordinate password change, interaction consumption and account-wide revocation in the existing transaction style. | Commit/rollback, double completion, replacement/completion, reset/refresh and reset/login concurrency evidence; other accounts and accepted garments preserved. |
| US-AUTH-005 — web/security/regression | Extend the cohesive auth API with thin handlers and scoped errors; update the common public-endpoint matcher/resolver consistently; retain protected defaults. | Known/unknown equivalence, malformed/expired/replaced inputs, secret-safe outputs/logs, stale bearer headers on recovery, old/new-password and old-token behavior. |
| US-GAR-001 — full applicable property model | Implement input/validation/persistence for all 13 applicable source properties, Category/Color confirmation and safe-retry metadata; eligibility is not evaluated. | Every source vocabulary/category, optional omission, multiple Materials, applicability, exact reread and authorized single-effect retry. |
| US-GAR-001 — durable acceptance | Introduce needed Wardrobe-owned persistence and transaction/action-outcome ports/adapters using current JDBC/Flyway conventions. Coordinate accepted garment and logical-action outcome. | Real committed save/reread, same-intention retry/contention/response-loss/restart evidence and valid separate additions of similar garments; existing state preserved on failure. |
| US-GAR-001 — protected creation | Add the necessary handler in a cohesive Wardrobe API, request/response validation and module-scoped error translation; derive owner from authenticated authority. | Real HTTP/security tests, explicit confirmation/missing-minimum/no-write evidence, cross-account and expired/revoked denial. |
| US-WAR-001 — current collection | Implement owner-scoped current-confirmed queries/projections and reuse the future owned-combination evidence meaning; expose stable garment identification and truthful empty/unavailable semantics. | Empty/populated/imageless/minimum-only/current/noncurrent persistence/API fixtures; another account's entries never appear. |
| US-WAR-001 — read integration | Integrate collection behavior with creation and corresponding detail identity without adding maintenance, search or use-recording side effects. | No mutation on read, retry after retrieval failure, protected access and created-item visibility tests; client handoffs listed as residuals. |
| US-WAR-002 — complete accepted detail | Retrieve every applicable accepted property, multiple Materials and optional Brand/Price, with metadata/media separate and no evaluated eligibility predicate. | Complete/missing values, category applicability, owner/current-state isolation, truthful unavailable use evidence and no read mutation. |
| US-WAR-002 — use-evidence and read safety | Represent unavailable reporting honestly; consume only published actual evidence if available; avoid private cross-owner models and invented zero/last-worn fields. | No-reporting/unavailable-input tests; conditional available-evidence contract fixtures where applicable; failure/retry/no-mutation and guessed-id/cross-account tests. |
| All four — integration/evidence | Maintain affected OpenAPI, forward migrations, necessary JIT explanations, architecture regression and sanitized AC-to-result mapping. | Existing auth regressions plus new real wardrobe/reset checks; inspect discovered suites/current CI, resolve review findings and keep external acceptance residuals explicit. |

No complete Java classes, SQL, endpoint payload definitions or executable code is generated by this plan.

## 10. Architecture and API/Persistence Considerations

### 10.1 Module ownership and reuse

**[ARCH]** Follow ADA-001/ADA-003/ADA-006/ADA-008/ADA-009/ADA-015–ADA-018 and the Implementation/Data Views.

| Verified location / convention | Application to selected work |
| --- | --- |
| `backend/src/main/java/com/capsuleai/identity/` | Recovery is Identity-owned. Extend existing services/ports/adapters only as needed; Wardrobe must not update Identity tables or import private Identity repositories/domain/application classes. |
| `identity/api/AuthenticatedAccount.java` and `SessionAuthority.java` | Published account/session meaning is the authorized integration boundary. Spring principal extraction stays at the web/security edge; owner-scoped use cases enforce affected-information access. |
| `backend/src/main/java/com/capsuleai/wardrobe/package-info.java` | Existing module root, not an implemented feature. Add useful `domain`, `application`/needed ports, adapters and configuration only as selected work needs them. |
| `IdentityTransactions` / `SpringIdentityTransactions` | Existing application-owned transaction boundary with outer Spring adapter. Keep domain/application/framework-free contracts; Wardrobe follows this style without importing Identity's private transaction port. |
| `AuthController` and backend conventions | Group related auth/recovery handlers by cohesive API. Group garment create/list/detail where resource/base-path responsibility is shared; separate application use cases remain separate. Exact new class/base-path names are JIT decisions, not one controller per story. |
| Scoped `IdentityApiExceptionHandler` | Its advice covers Identity's web package. New Wardrobe translation must cover its own web surface; do not assume Identity advice catches Wardrobe or security-filter exceptions. |
| One PostgreSQL, owner-controlled JDBC | Persist accountable owned state and use published owner boundaries. Owner association/FK/query choices are JIT; one database grants no unrestricted cross-owner SQL access. No generic repository/framework or new service boundary is required. |

**[TECH]** Reuse established password/JWT/session behavior. Do not introduce a new hashing algorithm, cache, broker, internal HTTP, microservice or infrastructure platform for these stories. Exact reset/email contract and logical-action representation need small owner-scoped design, not a new generalized framework.

### 10.2 Necessary JIT data and transaction decisions

**Wardrobe properties:** design source-value membership and category applicability, one Color, optional distinct Materials and every other applicable accepted property. Persist all accepted values and owner/confirmation/action-outcome metadata using PostgreSQL, Spring JDBC and forward-only Flyway changes during selected-story work. Preserve accepted labels and decimal/currency values through create/detail and safe replay. Collection projections may be narrower. A separate material percentage, removed attribute or readiness column is not authorized. Physical relations/columns, field names and transaction mechanics remain JIT; no speculative migration is created now.

- **Recovery:** determine protected reset verification representation, account association, expiry/consumption/replacement coordination and authoritative password-update/account-wide revoke operations. Existing SQL/enum constraints permit only logout/reuse reasons and must evolve coherently. Test supported upgrades and old rows; do not delete sessions merely to avoid implementing revocation.
- **Concurrent authority:** current `LogInService` verifies credentials before entering its session-creation transaction. Reset JIT design must examine concurrent login as well as refresh and reset completion so accepted reset cannot be bypassed by stale credential/session establishment. This is a risk requiring design/test evidence, not a confirmed exploit from an executed test. Choose the coordination mechanism JIT.
- **Email:** successful issuance/replacement and provider delivery are different outcomes. PostgreSQL cannot make an external email atomic. Define necessary safe dispatch/failure/recovery behavior while preserving equivalent initiation responses; do not infer a broker, universal Outbox or guarantee that provider acceptance means inbox delivery.
- **Manual creation:** preserve the eight-category code, optional valid subtype, confirmed Brand text and optional purchase Price amount/currency alongside accepted ownership, with enough durable logical-action/outcome coordination to resolve uncertain creation. Replaying the same action and deliberately adding another similar garment are distinct. Choose conflict and outcome-review semantics without inventing a mandatory server-side draft product.
- **Reads:** keep current ownership, unevaluated eligibility and historical meaning separate. A controlled inactive/stale fixture can verify changed selection without adding removal/history APIs. Missing Wear evidence remains unavailable, not zero.
- **Migrations:** add new versioned migrations after current V5, allocate actual versions during story work and verify clean initialization plus supported transition from V1–V5 with representative account/session data. No whole-MVP physical schema is required.

These tasks realize source behavior already traced in Section 6 and ADD Sections 9.1/9.2/9.4. They do not select physical tables, columns, indexes, isolation/locking mechanisms or a universal retention period now.

**[REQ] Optional-property constraints:** no fixed Brand enum or brand subsystem; decimal monetary representation with explicit ISO 4217 currency and no location default, floating-point calculation or currency conversion. A supplied incomplete Price pair must be corrected or cleared; absence is unknown, not zero. Preserve authority and same-action retry behavior for optional values. Resolve PRD OPQ-013 before dependent validation design. Exact field names, columns and endpoint shapes remain JIT.

### 10.3 API compatibility and security

**[REQ]** Workflow Section 27 makes OpenAPI the maintained HTTP contract authority; any actual contradictions must be resolved against SRS, code and tests; the original Section 3 findings are historical, not treated as permission to change product behavior. Preserve actual existing registration/login/logout/refresh behavior while adding only selected recovery and wardrobe operations.

**[TECH]** New recovery route names, open-versus-submit behavior, DTOs, status/error codes, duplicate-action handling and wardrobe collection/detail shapes remain selected-story JIT decisions. Existing convention examples and the test-only unknown-route string are not approved Wardrobe contracts. Retain safe Problem JSON/machine-readable meanings and distinguish unauthorized, unavailable, genuinely empty and no-longer-current outcomes; exact disclosure-safe status selection remains JIT.

The existing security configuration uses one public matcher for both authorization and bearer-header resolution. Recovery additions must preserve that consistency so stale access credentials cannot prevent unauthenticated recovery; all new personal wardrobe routes retain the protected default. Spring Security 7.1.1 documents bearer authentication as a filter-stage operation with authentication-entry-point error handling, separate from request authorization ([versioned resource-server documentation](https://github.com/spring-projects/spring-security/blob/7.1.1/docs/modules/ROOT/pages/servlet/oauth2/resource-server/index.adoc)). Framework documentation explains mechanics; CapsuleAI requirements and architecture remain policy authority.

## 11. Testing Strategy and Definition of Done

### 11.1 Required verification portfolio

**Final catalogue coverage:** minimum-only and full/partial entries for all eight categories; every approved Subtype and invalid parent pair; all 34 Colors (exactly one), 28 Materials (zero/one/multiple/duplicate selection), 24 Patterns (single optional), 17 named Styles (Etc rejected), every Tops/Pants-specific field/value and inapplicable-input correction. Verify optional omission, exact accepted-value persistence/reread, Brand text, decimal Price/currency pair, missing rather than zero, no appearance/location inference, no image/AI prerequisite, owner isolation and single accepted effect on retry. Detail returns all applicable accepted fields; collection remains identifiable. No obsolete attribute or ready/ineligible assertion is accepted. These are planned tests, not executed evidence.

**[REQ] Catalogue traceability:** the complete 13-property/vocabulary portfolio above supersedes the earlier subtype/Brand/Price-only examples. Link each applicable test to current Story/AC, FR-GAR-001–013, DATA-GAR-001–005 and BRULE-GAR-001–010; superseded IDs impose no old property or readiness test.

**[REQ]** Apply the existing Test Strategy/DoD. Section 6 maps all 53 selected-story AC to backend checks and external residuals.

| Level | Sprint 2 verification focus |
| --- | --- |
| Domain unit | Reset deadlines/state transitions and exact password policy; bounded classification/color, saved ownership versus unevaluated eligibility, optional/absent-value meanings. Independent expected outcomes, not implementation-mirroring assertions. |
| Application/use case | Safe initiation, minimal delivery disclosure, failed/rejected/no-write/reset coordination; confirmed creation/retry; owner-scoped collection/detail, unavailable use evidence and truthful errors. Boundary doubles do not prove persistence. |
| PostgreSQL/Testcontainers | Actual migrations/adapters, supported V5 upgrade, account/garment association, durable reset replacement/consumption and atomic completion/revocation, logical-action single accepted effect and restart/interruption outcomes. Include real commits/independent connections. |
| HTTP/Spring Security | Actual configured filter chain with real signed tokens and PostgreSQL authority. Known/unknown recovery equivalence, stale bearer on public recovery, malformed/invalid/expired/reset-revoked credentials, wrong account/guessed garment IDs and zero unauthorized disclosure/mutation. |
| Concurrency/boundaries | Competing reset completions; issuance/replacement versus submission; reset versus refresh/login; repeated manual creation for one intention. Coordinate barriers/signals and inspect committed outcomes; control time before/at/after 30 minutes without sleeps. |
| Email integration/contract | Real adapter serialization and purpose-minimal payload, controlled delivery failure/delay and actual selected mechanism compatibility/receipt. A fake mail sender alone cannot establish delivery. Keep uncontrolled Internet dependencies out of ordinary regression. |
| Architecture/regression/CI | Reuse and extend meaningful ArchUnit checks for new production classes and owner isolation; preserve Sprint 1 registration/login/logout/rotation/expiry regressions. Existing dependency rules do not alone establish SQL ownership or authorization. Resolve missing required suite/report and inspect fresh new-story test discovery/reports. |

**[TECH] Fixture reuse:** use existing MockMvc/Testcontainers/`MutableClock` patterns and synthetic two-account/multi-session data. Current Identity test support resets `identity*` tables using cascading test cleanup. Review fixture isolation as Wardrobe relationships appear; do not reset away the data whose preservation a reset/logout test is intended to prove. No test setup may target a developer's persistent or production database.

**[REQ] Evidence cadence:** focused checks during each story, affected integration/regression before review, and the required full verification/CI on the integrated revision. Existing Maven `*Test`/`*IT` discovery and the backend README's full `clean verify` entry point remain the convention. A bare push to `sprint_02` does not match current CI triggers; current workflow runs on `main` pushes and PRs targeting `main`. No PR is created by this task.

### 11.2 Completion states and outstanding client evidence

| Reporting state | Required meaning |
| --- | --- |
| Backend implementation completed | Necessary selected backend behavior and required failure/security paths are implemented; affected contract/migration/design truth is synchronized. This is not a full-story Done label. |
| Backend tests and integration verified | Applicable automated checks, real persistence/security/provider boundaries and required current CI/review evidence pass on the identified revision/environment. Tests that are absent, not run, failed or skipped are not passing evidence. |
| Outstanding frontend/mobile acceptance | Preserve the Section 6 residuals: recovery instruction opening/forms/return, manual review/cancellation, Add/detail/return navigation, account-switch/cache hiding, loading/recovery presentation, Vietnamese accessibility and 200% text behavior. No frontend coding/testing tasks are assigned to this backend developer. |
| Full User Story Done | All original applicable AC plus common/triggered DoD pass with the integrated supported experience. Backend completion alone cannot establish it; incomplete stories remain outside the Done Increment. |

API, schema, authorization/privacy, failure/recovery and runnable-software DoD triggers apply. Verify actual provider/integration environments where required; ordinary protocol substitutes are labelled, not presented as proof of the substituted real boundary. Architecture/requirements documents change later only when their own truth changes; stable selections do not need ceremonial edits.

**[REQ]** Do not weaken AC/DoD, declare all of US-PRIV-001 Done, or claim a full-MVP benchmark from these checks. Required formal performance/recovery evidence follows its actual source-defined scope and conditions; no new endpoint-latency target, coverage percentage or capacity estimate is introduced.

## 12. Risks, Open Questions, and Feasibility

### 12.1 Confirmed conditions versus unverified risks

| Condition / question | Evidence and effect | Owner / resolution point |
| --- | --- | --- |
| Historical missing outage suite | Present at the 2026-11-05 inspected revision; Section 3.2 records the older snapshot. Current execution evidence remains required. | Developers; verify actual affected CI/test results during implementation. |
| Historical contract/guide drift | Current registration contract, shared JWT constant and outage handling now exist as noted in Section 3. Earlier findings are not current defect claims. | Developers/Tech Lead; use actual source and verify affected contracts during selected work. |
| **[OPEN]** Email delivery mechanism and controlled destination access | No adapter/provider is implemented or selected by the inspected files. Actual delivery/receipt evidence cannot yet be produced. This blocks the affected recovery integration/completion claim, not independent Wardrobe development. | Developers with project owner for service/account access; resolve necessary feasibility/contract during recovery JIT work, without inventing a provider. |
| **[OPEN]** Instruction opening and reset outcome review | Supported request/open/submit/return transport and uncertain-result behavior need a safe client/backend contract. Account-safe initiation must survive failure translation. | Developers/Tech Lead with PO/BA for a genuine user-outcome ambiguity and the external client contributor for integration; Q-03. Backend policy design can proceed without assigning UI work here. |
| **[OPEN]** Reset concurrency/security | New persistence/credential-update/account-wide revoke capability is absent. Login verifies outside its creation transaction; refresh has existing row locking. Race correctness across these paths is unverified. | Developers/Tech Lead during reset JIT, before dependent implementation; demonstrate committed race/rollback outcomes rather than assume existing logout/refresh logic is sufficient. |
| **[OPEN]** Garment confirmation / unevaluated eligibility and safe retry representation | No feature implementation exists. Exact command/action identity, current-state representation and truthful unevaluated-eligibility disclosure are JIT work; sources already define their business meaning. | Developers; resolve only needed detail during manual/create/read design. PO/BA only if an actual source ambiguity emerges; no fabricated rules or default ready values. |
| **[OPEN]** Schema evolution and integration compatibility | Existing account/session data and reason constraints must remain usable. New owner relationships may affect fixture cleanup and migration tests. | Developers; forward migrations plus fresh/upgrade/integrity tests with actual preceding data. No destructive reset or editing V1–V5. |
| **[OPEN]** Full-story client evidence and finite privacy boundary | No mobile implementation/evidence is established here; focused review Q-02/Q-04 remains relevant. Backend assignment is fixed, but whole-story Done needs more than backend tests. | PO/Developers coordinate residual evidence responsibility with relevant client/QA contributors. No task allocation to this backend developer and no quality waiver. |
| **[OPEN]** Duration, availability and implementation forecast | Scope is selected; duration/capacity/estimates and current execution results are not available. Feasibility within a single timebox cannot be established from file inspection. | PO/Developers during planning review/scheduling. Preserve all four selected stories and make forecast uncertainty explicit; no unrecorded deferral/substitution. |

Previously recorded Display Name bounds, broader foundation completion and unrelated catalogue questions are not new Sprint 2 policy decisions. Reuse accepted current behavior where relevant; escalate a real conflict rather than reopening the entire baseline.

**[OPEN] Approved taxonomy follow-ups:**

| Question | Effect | Owner / resolution point |
| --- | --- | --- |
| PRD OPQ-012: eligibility/composition deferral | Resolved deferral for every category; no Sprint 2 engine or readiness gate. | Future engine design; no reapproval of the current property model. |
| PRD OPQ-013: monetary bounds, zero/negative handling, precision/rounding, currency-list and Brand validation/normalization | **Resolved 2026-11-07:** zero valid, negative invalid, at most 12 integer digits, currency minor-unit precision without rounding, valid upper-case ISO 4217 currency; Brand ≤ 100 code points, blank unspecified, exact text. | Closed; see Section 12.3. |
| PRD OPQ-014: vocabulary approval | Resolved: every source-listed subtype/value is approved now, with no Etc Style. | Implement complete lists; technical representations remain JIT. |

### 12.2 Feasibility assessment

**Scope/effort impact:** Option B expands manual-entry validation, persistence mapping, fixtures and detail retrieval from the former small optional subset to all 13 applicable properties and complete vocabularies. Category applicability and material collection semantics add implementation/test work. Preserve the four selected stories and order; no duration, velocity or estimate is invented, and fit within a Sprint is not asserted. Resolve actual forecast concerns with the team using the selected scope. Engine eligibility/compatibility is future work and supplies no Sprint 2 blocker.

The work has a credible architectural base: existing account/session security, JDBC transactions, Flyway, real-PostgreSQL/API test support and CI are reusable. Creation/list/detail share Wardrobe ownership and accepted-property semantics. This reduces setup duplication.

The scope is still substantial: recovery adds a real external boundary and security-sensitive concurrent authority transitions; manual entry introduces durable acceptance/idempotency; list/detail require current owner isolation and honest conditional evidence. Missing current runtime and integrated verification increases uncertainty; Section 3 distinguishes the historical static findings from the inspected revision. A completion date or “fits one Sprint” claim would be unsupported.

All four stories remain selected. Resolve affected blockers with bounded engineering work and responsible PO/Developer coordination while progressing independent selected work. This document provides no reduced scope, stretch category or permission to omit security, recovery or verification obligations.

### 12.3 Product Owner decisions of 2026-11-07

The Product Owner finalized every open Sprint 2 question; the selected stories and Sprint Goal are unchanged. Decisions: OPEN-S2-01A (Price validation), OPEN-S2-01B (Brand), OPEN-S2-02 (HTTPS App/Universal Links; custom scheme for development only), OPEN-S2-03 (Vietnamese email wording), OPEN-S2-04 (configurable per-address and per-client rate limits), OPEN-S2-05 (truthful 503, timing hardening, documented residual risk), OPEN-S2-06 (denylist deferred), OPEN-S2-07 (30-day reset-record retention with automated cleanup), OPEN-S2-08 (multiple Styles), OPEN-S2-09 (provider-neutral SMTP now; real provider and delivery evidence before release/Done), OPEN-S2-10 (cursor pagination), OPEN-S2-11 (PRD readiness wording removed), OPEN-S2-12 (SOLID-density verification removed), X-1–X-7, REVIEW-CI (CI on pull requests to `sprint_02`), REVIEW-DB-ROLE (single database role with build-time ownership checks) and REVIEW-TAXONOMY (Tanks & Camis). Where each one is incorporated: [decision implementation notes](decision-implementation-notes.md) and the [implementation guides](implementation-guides/README.md).

## 13. Sprint Review Evidence

The following evidence is a target for later delivery, not a report of executed functionality.

| Demonstration / evidence | What should be observable |
| --- | --- |
| Account-safe recovery request and actual delivery | Paired known/unknown initiation semantics, purpose-minimal recipient delivery, usable issued instructions and separate delivery/completion outcomes. Sanitize secrets in review captures. |
| Accepted reset across multiple sessions | Old password rejected; exact new password logs in to the same account; all prior affected-account sessions/tokens denied; another account unaffected. Show expired/replaced/reused/invalid/uncertain outcomes. |
| Confirmed imageless manual creation | All eight categories save once with the supported minimum; optional subtype/Brand/purchase Price are retained when supplied and remain optional when omitted; unevaluated eligibility is explained without reporting save failure or creating fabricated data. |
| Creation → collection → corresponding detail | Correct owner sees the accepted entry and accepted profile / unevaluated eligibility; another account/guessed identifier does not. Genuine emptiness differs from unavailable access/data and stale selection. |
| Reset after wardrobe creation | Existing wardrobe remains intact; old credentials cannot access it; new-password login restores authorized list/detail. This makes previously unexercised accepted-wardrobe preservation inspectable. |
| Automated/real-integration evidence | Relevant unit/application, PostgreSQL, security/API, migration, concurrency, email compatibility, architecture and regression results; current CI reports and resolved review findings tied to revision/environment. |
| Completion/residual account | State backend implementation status, executed verification, outstanding client evidence and actual full-story DoD status separately. No implemented mobile journey or unavailable Wear/image integration is promised. |

**[REQ]** Use synthetic data, protect reset credentials/tokens/passwords and label simulated boundaries. Incomplete work may be discussed as progress but cannot be represented as a Done Increment.

## 14. Next Development Activities

### 14.1 Planning review and first selected-story JIT work

Review the proposed Goal, scheduling/capacity information, responsibility for external evidence and the concrete blockers in Section 12. All four selected stories remain in the Sprint 2 plan; this is not a request to reselect them.

The next engineering activity is **US-AUTH-005 JIT Detailed Design**, grounded in the current Identity implementation: necessary recovery contract/delivery feasibility, reset authority/lifecycle, atomic accepted effects, concurrency and evidence. Verify current contract/CI/security evidence alongside the first affected integration work; Section 3 distinguishes historical findings from current source. A sequence/state model is conditional on the reset risk, not a mandatory diagram set for every story.

Then repeat the established **JIT Design → Automated Tests + Implementation → Refactor → Integration Verification → PR / CI / Review** loop for recovery, manual creation, browsing and detail. Keep selected work and affected living artifacts synchronized; do not complete a Sprint-wide implementation design before starting the first story.

### 14.2 Standard for subsequent self-contained implementation guides

**[REQ — current documentation instruction]** Future story guides must contain the **complete original relevant** business requirements, SRS requirements, Business Rules, all original Story AC, constraints and exceptional/alternative flows. Reproduce original wording faithfully, including identifiers, headings, Given/When/Then conditions, exceptions, references and conditional applicability. Link each source and record its version/inspected revision.

Use the Sprint 1 guide structure for orientation, but its compact scenario/AC summary tables cannot replace the complete original text required for Sprint 2. Present verbatim source material separately from explanations, examples, backend implications and proposed technical choices. Include enough context to understand the full relevant behavior from the guide alone; synchronized source snapshots do not supersede canonical authorities.

**[TECH] Guide instructions must be concrete and verifiable:**

- **[ADD]** State the exact newly verified destination path, purpose and full necessary file content when implementation is authorized.
- **[MODIFY]** Verify the current file, identify an unambiguous insertion/replacement anchor and show accurate BEFORE/AFTER examples; provide the complete resulting file where the manual implementation convention requires it. Do not treat a previous guide's proposed file as current source.
- **[REMOVE]** Identify the precise file/element and removal boundaries, explain why removal is necessary and preserve unrelated content. Do not invent removals merely to populate a checklist.
- Preserve **[REQ] / [ARCH] / [TECH] / [OPEN]** distinctions, use descriptive Java class/method/variable names, keep controllers cohesive and thin, and trace meaningful tests back to source AC.
- Include failure, secret handling, ownership, migration/compatibility, real integration and current CI discovery considerations; distinguish backend evidence from full-story/client completion. Do not publish credentials or claim unexecuted checks passed.

Potential future guide names, under the existing per-Sprint implementation-guide convention, are `US-AUTH-005-guide.md`, `US-GAR-001-guide.md`, `US-WAR-001-guide.md` and `US-WAR-002-guide.md`. **None is created by this task.** Prepare only the guide needed for the current selected story when that follow-up work is authorized; a complete four-guide packet is not an implementation gate.

The original planning task created this artifact only. The 2026-11-05 revision synchronizes the approved taxonomy/properties with affected requirements, stories and supporting documents. It retains the four selected stories and their order, US-AUTH-005, architecture selections and the implementation-guide standards above. No production code, tests, migrations, API contracts, configuration, CI or new guide is created or changed by this revision.
