# CapsuleAI — Sprint 1 Planning Draft

## 1. Sprint Overview

| Field | Value |
| --- | --- |
| Sprint identifier | Sprint 1 — Secure Account Access — Backend Only |
| Version / status | 0.1 / Planning Draft |
| Prepared | 2026-10-22 |
| Repository inspected | Branch `main`; commit `ea0b2a76f3272eb407aa0d80c121cb757fccb018`; working tree clean before this draft |
| Sprint dates / duration | Pending team decision |
| Capacity / estimates / assignees | Not established; no allocations or estimates inferred |
| Decision status | Recommended candidates for PO/Developers review; no formal selection, commitment or team consensus recorded |
| Accountability | Scrum Team shapes the Goal; Developers select feasible work with the PO and own the resulting Sprint Backlog |

**Proposed Sprint Goal**

> Deliver a working Spring Boot authentication backend supporting account registration, login, authenticated-session continuity, and logout, with applicable security protections and automated verification.

The Goal is clear, cohesive and testable as a backend outcome. It enables private participation in the [Product Goal](../../product-goal.md); it does not yet deliver wardrobe value or a complete mobile journey. Technical readiness supports starting selected work, but duration and capacity are unknown, so completing the preferred scope in one Sprint is not established.

**Delivery boundary:** backend behavior, persistence, security, affected HTTP contracts and automated verification for the four candidate stories. Their original AC remain authoritative. Mobile screens, navigation, styling, components and frontend tests are external responsibilities; none is assigned to the backend developer. Backend evidence cannot by itself establish all four user-facing stories as Done.

Password recovery (US-AUTH-005/PBI-005), email delivery, profile/location features, garments, AI, shopping, account deletion, session-management screens and all-device logout are outside this proposal. Existing recovery guidance references do not select recovery implementation.

### 1.1 Inspected engineering baseline

| Observed evidence | Planning implication |
| --- | --- |
| [Backend README](../../../../backend/README.md), [POM](../../../../backend/pom.xml) and committed Wrapper | Java 21, Spring Boot 4.1.1 servlet MVC and Maven 3.9.16 foundation exist. Reuse them. Spring Security/JWT implementation and explicit security/JWT dependencies are not yet present. Add necessary compatible support with selected-story work. |
| [Identity package](../../../../backend/src/main/java/com/capsuleai/identity/package-info.java) and production source | Bootstrap and six ownership package descriptions exist; account/session use cases, product APIs and security enforcement do not. Identity owns authentication; other business modules need no feature implementation for this Goal. |
| [Compose](../../../../compose.yaml), application configuration and [foundation migration](../../../../backend/src/main/resources/db/migration/V1__foundation.sql) | PostgreSQL development configuration and Flyway plumbing exist. V1 runs `SELECT 1` and creates no business tables. Necessary account/session migrations remain selected-story work; ORM, physical schemas and concurrency mechanisms are not chosen by this plan. |
| Existing [harness](../../../../backend/src/test/java/com/capsuleai/foundation/HarnessSmokeTest.java), [architecture](../../../../backend/src/test/java/com/capsuleai/architecture/ArchitectureDependencyTest.java) and [database](../../../../backend/src/test/java/com/capsuleai/foundation/DatabaseSmokeIT.java) tests | Two harness, two architecture and one real-PostgreSQL foundation case provide infrastructure. They establish no authentication AC coverage. Initial architecture rules tolerate empty roles and need meaningful application coverage as code appears. |
| [Backend CI](../../../../.github/workflows/backend-ci.yml) | Java/Docker build verification, Surefire/Failsafe report checks and report retention exist. The named foundation-report guard does not prove new authentication tests were discovered; inspect their actual reports too. |
| README Sections 7–13 | Local private configuration and recorded merged-baseline CI evidence exist. This planning inspection did not run builds, start services or rerun CI. Verify the actual implementation revision later; do not treat historical results as its proof. |

### 1.2 Governing sources and artifact convention

| Authority | Planning use |
| --- | --- |
| [Canonical workflow](../../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 26–34, 38–40 and 46 | Planning precedes selected-story JIT design; tests accompany implementation; no mandatory baseline ADR phase. |
| [Product Backlog](../../product-backlog.md), PBI-001–PBI-005; [Story Catalogue](../../stories/README.md); [Delivery Decisions](../../delivery-decisions.md), DD-001–DD-006 | Preserve order, story identities, hard capability dependencies and human selection authority. |
| [Focused refinement review](../../backlog-refinement-review.md), Q-01–Q-05 | Carry relevant feasibility and completion questions forward; distinguish backend scope from outstanding full-story/client evidence. |
| [BRD](../../../01-business/BRD.md), BR-021/BR-023 and CAP-01; [PRD](../../../02-product/PRD.md), JRN-01 and FEAT-AUTH-001 | Private access, optional setup and progressive product entry. |
| [SRS](../../../03-requirements/SRS.md), Sections 3.1, 4.2, 5.4, 6.2–6.3 and 9.1; [Business Rules](../../../03-requirements/business-rules.md), AUTH family | Exact identity, password, access, renewal, revocation, privacy and failure policies. |
| [UC-001](../../../03-requirements/use-cases/UC-001-register-account.md), [UC-002](../../../03-requirements/use-cases/UC-002-log-in.md), [UC-003](../../../03-requirements/use-cases/UC-003-log-out.md) | Successful, denied, interrupted and already-unusable interaction outcomes. |
| [Selected Architecture Decisions](../../../04-architecture/selected-architecture-decisions.md), especially ADA-003/008/009/015–018; [ADD](../../../04-architecture/ADD.md), Sections 9.1–9.4; [Logical](../../../04-architecture/views/logical-view.puml), [Implementation](../../../04-architecture/views/implementation-view.puml), [Deployment](../../../04-architecture/views/deployment-view.puml) and [Data](../../../04-architecture/views/data-view.puml) Views | Persistent PostgreSQL authority, Identity ownership, inward dependencies and deliberate transaction boundaries. No architecture reselection. |
| [Test Strategy](../../../07-testing/test-strategy.md), Sections 8–9, 13–18 and 21–22; [Definition of Done](../../definition-of-done.md), Sections 3–8 | Proportional automated evidence, real security/persistence integration and honest completion claims. |

The workflow supplies the `sprints/sprint-01/` directory convention and example Goal/Backlog locations. No existing Sprint files or separate canonical planning template were found. This single primary draft consolidates the proposed Goal and candidate backlog as requested; it creates no competing committed Sprint record. Historical preparation handoffs in older documents do not restart the already merged engineering baseline.

## 2. Candidate Sprint Backlog

All four authentication stories remain **Baseline Draft** in their source files. The privacy story remains **Draft**. Assessments below are recommendations for discussion, not Ready approvals.

| Story ID | Story Title | Backend Outcome | Dependencies | Planning Assessment |
| --- | --- | --- | --- | --- |
| [US-AUTH-001](../../stories/auth/US-AUTH-001-register-account.md) | Register a private account | Create one policy-valid private account, preserve optional input and safely handle validation/conflict/uncertainty. | PBI-002 protection; account persistence and credential handling. A later credential login can consume the created account. | Preferred candidate; sufficiently specified for backend planning discussion. Registration-to-authorized-entry contract remains JIT detail. |
| [US-AUTH-002](../../stories/auth/US-AUTH-002-log-in.md) | Log in to my private account | Verify exact credentials and establish usable account/session authority with required token issuance. | PBI-002; an existing account, normally supplied by US-AUTH-001. Existing-account fixtures can independently support development. | Preferred candidate; source policies are clear. Security/session support must be implemented, not assumed from the skeleton. |
| [US-AUTH-003](../../stories/auth/US-AUTH-003-resume-session.md) | Resume access within my login session | Resume/renew correct-account access, enforcing rotation, absolute expiry, reuse revocation and safe failure. | US-AUTH-002 supplies a login session; shared persistent authority. Must honor US-AUTH-004 revocation when integrated. | Preferred candidate with the greatest lifecycle/concurrency risk; Developers must establish selectability. |
| [US-AUTH-004](../../stories/auth/US-AUTH-004-log-out.md) | Log out of my current session | Revoke the intended session, deny its further protected access/renewal and preserve separate sessions and accepted data. | A usable session from US-AUTH-002 or an existing-session fixture; continuity must respect revocation. | Preferred candidate; implement revocation early alongside issued authority. No dependency on completing all renewal behavior first. |
| [US-PRIV-001](../../stories/privacy/US-PRIV-001-protect-first-private-operations.md) | Protect the first delivered private operations — applicable obligations | Verify the delivered authentication/session boundaries, secret protection, necessary processing and bounded evidence. | Applied with every affected operation. PBI-002 is PBI-004's hard capability dependency. | Mandatory applicable work across the four candidates; neither whole-story selection nor completion of US-PRIV-001/PBI-002 is inferred. |

PBI-003's mobile foundation is useful sequencing, not a new hard dependency for backend implementation. Its missing client evidence still prevents affected full-story completion. No Product Backlog order or parent mapping changes.

## 3. Story-by-Story Scope

AC references are local to the linked story. Tables identify backend contributions and verification limits; they do not replace or relax the original criteria.

### 3.1 US-AUTH-001 — Registration

**Outcome and deliverables:** accept required registration input, securely handle credentials, persist one normalized identity and optional Display Name, and expose truthful success/validation/conflict/failure outcomes. Optional profile, location and wardrobe setup cannot become account-access gates.

| Relevant AC | Backend deliverables and verification | External / end-to-end evidence remaining |
| --- | --- | --- |
| AC-01, AC-02 | Account creation and correct-account entry handoff; successful operation with all optional setup absent. Application, PostgreSQL and HTTP integration checks. | Actual onboarding/Today/Wardrobe entry, skip behavior and empty/limited-context presentation. |
| AC-03, AC-04, AC-05, AC-07 | Missing/mismatched input rejection; 11/12/128/129 password boundaries; exact spaces/Unicode preservation through credential verification; optional name/diacritics round-trip. Unit, persistence and API evidence. | Input correction, visible retained name and understandable interaction. |
| AC-06, AC-08, AC-09 | Concurrent equivalent emails create at most one account; duplicate attempts preserve the existing account. Cancellation before acceptance creates none; interrupted responses do not undo an already accepted creation. Verify committed results, failure boundaries and safe retry without duplicates. | Client cancellation before submission/acceptance, progress states and user-directed outcome review/retry. |
| AC-10 | No protected disclosure before usable authority; successful handoff targets the created account; credentials stay out of unauthorized outputs/logs. Security/API checks. | Client presentation throughout entry. |
| AC-11 | Supply contract semantics that can support accessible guidance; backend verification alone does not satisfy this AC. | Vietnamese registration, TalkBack/VoiceOver and operability at 200% text scaling. |

**Risks / JIT questions:** resolve only the required creation/entry contract, credential protection and persistence integrity. The sources require an authorized entry path but do not prescribe automatic login; do not invent it or assume registration issues credentials. Cancellation is not a new rollback/delete-account feature. Sign-in/recovery guidance does not include implementing password reset.

### 3.2 US-AUTH-002 — Login

**Outcome and deliverables:** verify normalized email and the untransformed password, establish correct-account persistent session authority and issue credentials under the existing policy. Invalid access grants no protected use and does not alter accepted personal information.

| Relevant AC | Backend deliverables and verification | External / end-to-end evidence remaining |
| --- | --- | --- |
| AC-01, AC-05 | Correct-account login and usable authority without optional setup. Application, credential-adapter, PostgreSQL and HTTP/security integration checks. | Personal-area/onboarding navigation and intended-area restoration. |
| AC-02, AC-03 | Same email identity rules as registration; exact password comparison including spaces/Unicode. Unit and real adapter/API evidence. | Client entry/preservation of those values. |
| AC-04, AC-07 | Reject incomplete/wrong credentials; distinguish unavailable or uncertain access from success/empty inventory; no protected response without usable authority. Negative/failure integration tests. | Understandable correction/recovery route, pending/failed presentation and user retry. |
| AC-06 | Two-account access and responses remain isolated; denied attempts disclose no prior-account content or mutate accepted state. Real security/owner checks. | Device cache clearing, preceding-content hiding and account-switch presentation. |
| AC-08 | Preserve guidance semantics in the backend contract. | Vietnamese accessible login and 200% text-scaling behavior. |

**Risks / JIT questions:** security dependencies, credential verification, JWT validation/issuance, session association, signing/key handling and the affected contract are necessary selected-story design work. No social login, guest access, mandatory email verification, lockout policy or recovery implementation is introduced. Existing account input is a capability dependency; production registration need not run inside every login test.

### 3.3 US-AUTH-003 — Session continuity

**Outcome and deliverables:** retain usable authorized access and renew it only within the established session policy, with durable consumption/revocation and correct account association.

| Relevant AC | Backend deliverables and verification | External / end-to-end evidence remaining |
| --- | --- | --- |
| AC-01, AC-02 | Usable return and renewal after access expiry preserve account identity and accepted state. Policy and full security/persistence/API integration checks. | Returning personal experience, intended area and no unnecessary credential prompt. |
| AC-03, AC-05 | Successful rotation consumes the old refresh credential and replaces both credentials; consumed-token reuse revokes the affected session, including newer refresh authority, while another session remains distinct. Real committed/concurrent PostgreSQL and API checks. | Client handling of renewed or rejected access. |
| AC-04, AC-06, AC-07 | Enforce absolute lifetime and separate sessions; reject revoked/unusable authority even with an unexpired signed JWT; credential login can restore correct-account access. Controlled-clock boundary and security integration evidence. | Login handoff, preserved visible personal experience and inaccessible-versus-empty guidance. |
| AC-08 | Exercise failure before/after renewal acceptance; no false completion, unauthorized continuation or damage to accepted data. Inspect actual committed authority. | Pending/uncertain presentation and usable review/retry/reauthentication interaction. |
| AC-09 | Contract conveys authentication-required rather than fabricated inventory results. | Vietnamese accessible guidance and 200% text-scaling behavior. |

**Risks / JIT questions:** transaction/competition behavior, durable refresh representation and interrupted-response recovery need deliberate reasoning. Do not add a replay grace exception or relax consumed-token reuse policy to simplify retries. A bounded feasibility investigation is appropriate before selection only if a major unknown prevents responsible selection; full API/schema/sequence/state design follows selection.

### 3.4 US-AUTH-004 — Logout

**Outcome and deliverables:** terminate the intended current session without deleting personal data or revoking another separate session.

| Relevant AC | Backend deliverables and verification | External / end-to-end evidence remaining |
| --- | --- | --- |
| AC-01, AC-02, AC-05 | Completed revocation denies current-session protected access and renewal, including an unexpired JWT; session B remains usable. Already-expired/revoked handling stays scoped to A. PostgreSQL and real security/API evidence. | Completed/already-unusable signed-out experience. |
| AC-03, AC-04 | No deletion/alteration of accepted personal state; signed-out and wrong-account requests receive no preceding-account content. Application and security/persistence checks. | Normal/back navigation, local cache isolation and account-switch display. |
| AC-06 | Fault/interruption evidence distinguishes confirmed revocation from unconfirmed outcome; review/retry cannot target a different session or falsely report success. | Protected content stays hidden during uncertain logout; usable user recovery controls. |
| AC-07 | Accurate outcome semantics in the contract. | Vietnamese accessible logout controls/states and 200% text scaling. |

**Risks / JIT questions:** determine supported repeated/already-unusable and unconfirmed-request behavior from UC-003 without inventing unconditional success or a new all-device operation. Integrate denial with continuity, not only token deletion in a client.

For all four stories, fixtures and checks can establish preservation of delivered account/session state. Absent wardrobe/profile/history features must be recorded as not yet exercised; do not implement those product capabilities solely to manufacture auth evidence or claim they have been verified.

## 4. Security and Privacy Requirements

The following policies are already fixed by the [SRS](../../../03-requirements/SRS.md) and [Business Rules](../../../03-requirements/business-rules.md). Their implementation mechanisms remain JIT decisions.

| Required boundary | Sprint application | Source anchors |
| --- | --- | --- |
| Identity and credentials | Trim surrounding email whitespace and compare case-insensitively; no provider alias merging; one account per normalized identity. Passwords are 12–128 characters, permit spaces/Unicode, require no character-class mix and are not silently transformed. Protect credentials in storage, processing and outputs. An optional small approved denylist is not assumed adopted. | FR-AUTH-001, FR-AUTH-002, FR-AUTH-012, FR-AUTH-015, FR-AUTH-016; DATA-AUTH-001, DATA-AUTH-002; BRULE-AUTH-002, BRULE-AUTH-003 |
| Authorized protected use | Valid, unexpired JWT **and** usable authorized account/session context, followed by affected-information ownership checks. Authority unavailable/unusable is no bypass. | FR-AUTH-008, FR-AUTH-009; ADA-008; ADD Section 9.1 |
| Lifetime and refresh | Access Token lifetime: **15 minutes**. Login/Refresh Session maximum: **30 days from its original establishment**; rotation never extends it. Each successful refresh consumes its current token and issues replacement Access and Refresh Tokens. Reuse revokes that affected session; a newer refresh credential cannot renew it. | FR-AUTH-005, FR-AUTH-006, FR-AUTH-010, FR-AUTH-013, FR-AUTH-014; DATA-AUTH-005; BRULE-AUTH-006 |
| Revocation scope | Logout affects the current session; refresh reuse affects its session. Another separate session is preserved. Account-wide revocation after completed password reset remains a distinct future capability; it is not implemented by this proposal. | FR-AUTH-007; BRULE-AUTH-005, BRULE-AUTH-006 |
| Integrity and failures | Coordinate accepted credential/session effects durably. Preserve accepted state on denied/known-failed operations and truthful uncertainty after interruption. Do not confuse unavailable access with empty inventory or let retry bypass refresh rules. | COM-001, COM-002; ERR-AUTH-001, ERR-AUTH-003, ERR-NET-001; ADA-008, ADA-009 |
| Secrets and purpose | Protect private exchanges and unauthorized outputs; keep passwords/tokens out of logs, reports and measurement. Process only necessary private information. No MVP personal-data training/improvement, sale, public wardrobe or unrestricted sharing. | NFR-SEC-001–NFR-SEC-003; NFR-PRIV-001, NFR-PRIV-002; COM-003; BRULE-AUTH-007 |

Public registration/login remain available as specified. Renewal establishes authority through the usable refresh credential; requiring a still-unexpired Access Token for renewal would contradict the expired-access recovery outcome. The protected-personal-operation rule does not invent a new access gate for these public/recovery entry points.

### US-PRIV-001 applicability

| Privacy AC | Required bounded evidence / limit |
| --- | --- |
| AC-01, AC-02 | Enumerate protected authentication/session operations actually delivered; exercise two accounts, separate sessions, signed-out/unusable authority, ownership denial and no unauthorized mutation/disclosure. Test the actual security path. Client hiding remains external evidence. |
| AC-03 | Inspect affected contracts, credentials, token handling, exchange protection and safe diagnostics. Recovery-initiation privacy is not claimed tested when recovery is absent. |
| AC-04 | Confirm necessary account/session processing and optional-input boundaries; no new provider exchange or training/sharing purpose is introduced. |
| AC-05 | Preserve applicable lifecycle responsibility for delivered data. Images, Wear history, optional-profile removal and measurement retention remain unexercised where absent, with their owning future work; absence is not a passing check. |
| AC-06 | Retain operation/scenario, tested revision/environment, expected/forbidden outcomes and limitations in existing PR/test evidence without exposing secrets. PO/Developers still need the finite PBI-002 completion boundary. |

Apply these protections as each operation is delivered. A separate full-MVP privacy campaign is not a prerequisite, and neither US-PRIV-001 nor PBI-002 is automatically Done through authentication alone.

## 5. Dependencies and Recommended Sequence

The capability dependencies above do not require a serial layer-by-layer project. The recommended order below minimizes rework and exposes revocation risk early. **Each selected story uses its own JIT design → automated tests and implementation → refactor → integration verification → PR/CI/review loop.** This is not a whole-Sprint design phase followed by coding and final testing.

| Order | Recommended work | Dependency / purpose |
| --- | --- | --- |
| 1 | Begin US-AUTH-001 with only its necessary Identity contract, credential/persistence design and tests; deliver registration behavior. | Reuse the foundation. Resolve shared details only as the first selected outcome actually needs them. |
| 2 | Implement US-AUTH-002 with persistent session authority, required issuance and real security-path checks. | Consume an accepted account; expose correct-account access, invalid credentials and authority failures. |
| 3 | Implement US-AUTH-004 current-session logout and immediate revocation checks. | A usable session is now available; establish that issued authority is controllable before expanding continuity. |
| 4 | Implement US-AUTH-003 renewal/rotation/reuse and integrate with login/logout. | Extend the same authority; preserve expiry, affected-session revocation and separate-session behavior. |
| Throughout / completion | Integrate privacy, failure, migration and regression evidence with each story; complete cross-story verification and resolve PR/CI/review findings. | Privacy/testing are not deferred to the end. Keep affected contracts and implementation evidence current. |

US-AUTH-004 does not need all of US-AUTH-003 implemented before its normal logout path can be developed. Final combined evidence must nevertheless show that renewal cannot revive logged-out authority. Likewise, login tests may use controlled existing-account fixtures without making registration a universal execution prerequisite.

Stable boundaries remain unchanged: Identity owns account/session state in one PostgreSQL; domain/application dependencies point inward; framework security/HTTP/persistence integration stays at the edge. Use published contracts where another owner is genuinely needed. No unrelated module implementation, cache, broker, internal HTTP or new deployment topology is required.

## 6. Backend Task Breakdown

These are proposed engineering tasks beneath the existing candidates, not new PBIs or replacement stories. Developers may refine them after selection. No estimates or assignments are recorded.

| Story / grouping | Practical backend task | Verification / acceptance connection |
| --- | --- | --- |
| US-AUTH-001 | Resolve the necessary registration/entry contract and Identity-owned persistence/credential protection during JIT work; introduce only required dependencies and forward migrations. | AC-01–AC-07, AC-10; synchronized contract, real migration transition and secret handling. |
| US-AUTH-001 | Implement account creation and policy validation, including optional name and exact password meaning; add fast behavior and persistence/API checks. | AC-01–AC-07; boundary and diacritic fixtures. |
| US-AUTH-001 | Cover equivalent-email contention, cancellation-before-acceptance and failed/uncertain creation with committed outcomes and safe retry. | AC-06, AC-08, AC-09; no duplicate or alteration of an existing account. |
| US-AUTH-002 | Resolve required credential verification, security/JWT integration, initial session authority and issuance detail; implement usable login through the affected API. | AC-01–AC-05; fixed session policy and real security path. |
| US-AUTH-002 | Verify incorrect/incomplete login, two-account isolation, optional-setup independence and interrupted-access outcomes. | AC-04–AC-07; no unauthorized content or accepted-state damage. |
| US-AUTH-004 | Design and implement current-session revocation with completed, already-unusable and unconfirmed outcomes; integrate access/renewal denial. | AC-01, AC-02, AC-05, AC-06; real persisted revocation and unexpired-JWT denial. |
| US-AUTH-004 | Verify separate-session preservation, no personal-data deletion and backend account-switch isolation. | AC-02–AC-04; client hiding remains separately unverified. |
| US-AUTH-003 | Resolve and implement the necessary renewal contract, durable consumption/replacement and absolute-lifetime enforcement; use lifecycle/interaction models only if risk justifies them. | AC-01–AC-04, AC-06, AC-07; policy and API/persistence integration. |
| US-AUTH-003 | Verify competing renewals, consumed-token replay, denial of newer authority after revocation, restart durability and interrupted-response outcomes. | AC-03, AC-05–AC-08; committed PostgreSQL state plus configured security enforcement. |
| All four + US-PRIV-001 | Maintain the actual protected-operation/scenario set, two-account/separate-session fixtures, controlled clock and privacy-safe evidence; exercise authentication/authorization failures and exchanges. | Applicable privacy AC-01–AC-06 and the story AC above. No artificial production endpoint or unrelated wardrobe feature just for a demo. |
| All four | Keep necessary APIs, migrations and design explanations synchronized; extend meaningful dependency checks and ensure changed tests are discovered by the existing verification entry point. | Triggered DoD; foundation smoke counts alone are insufficient. |
| All four | Integrate results, run affected/full required backend regression, inspect current CI/reports and resolve review findings; retain AC-to-evidence and external-residual mapping. | Applicable common DoD; record backend verification separately from full-story Done. |

## 7. Testing and Definition of Done

Use the existing [Test Strategy](../../../07-testing/test-strategy.md) and [DoD](../../definition-of-done.md); this section introduces no alternative completion standard.

| Verification category | Required focus for this proposal |
| --- | --- |
| Unit / application | Identity/password policies, validation, correct-account effects, lifecycle/expiry decisions and denied/failed operations using real policies and controlled boundary doubles. Selective TDD is useful; strict TDD is not required for every change. |
| PostgreSQL / Testcontainers | Actual forward migrations, supported transition from the foundation, persistence round-trips, normalized-email uniqueness under contention, atomic refresh consumption/replacement and persisted revocation/absolute lifetime. Use real commits, independent transactions and applicable restart checks; mocks or in-memory substitutes do not prove these properties. |
| Spring Security / HTTP API | Actual configured security chain, token validation and persistent-authority/owner checks. Include missing, malformed, invalid-signature and expired token cases plus valid signed tokens with revoked/unusable authority. Mock-user/mock-JWT helpers are not the sole evidence. |
| Failure / concurrency / boundaries | Explicitly coordinate registration/renewal competitors and inspect final committed results; test time immediately before, at and after the fixed deadlines. Control interruption before/after acceptance; no arbitrary sleeps or replay exception. |
| Regression / architecture / CI | Preserve existing valid checks and add source-linked auth coverage. Use the existing full verification entry point documented in the backend README; inspect actual discovered tests, failures/errors/skips and reports on the integrated revision. Required checks must pass, with review findings resolved. |
| Integration / operational evidence | Demonstrate the backend with real required persistence/security in the agreed environment. Record revision, environment and inputs; protect secrets. Backend HTTP evidence is not mobile E2E or accessibility proof. |

Affected API contracts and migrations are expected triggers: synchronize the necessary OpenAPI/consumer meaning and verify real migration transitions. Privacy/security, failure/recovery and runnable-software conditions apply. Architecture documents change only if architectural truth changes; existing selections are reused. No full-MVP benchmark, unrelated feature tests or mandatory document set is introduced.

**Completion reporting**

- A backend contribution may be reported as **implemented and verified within the stated backend scope** only with its applicable AC evidence, tests, contracts/migrations, integration, CI and review complete. Missing required backend protection or failed checks prevent even that report.
- A **whole user story is Done** only when all its original applicable AC and common/triggered DoD conditions pass, including outstanding client/integrated evidence. Backend task completion or a Postman demo does not establish that.
- Retain explicit residuals from Section 3 against each original story. These four stories remain incomplete wherever required mobile/end-to-end evidence is missing; US-PRIV-001/PBI-002 also retain their broader reviewed boundary.
- Sprint Review may inspect backend progress transparently. Incomplete stories cannot be counted in the **Done Increment**. Planning must address this delivery limitation without weakening the DoD or assigning frontend work to the backend developer.

## 8. Risks and Open Questions

The textual Use Cases report no blocking interaction-policy issue. The canonical password/session rules are established. Endpoint, schema, key, transaction and detailed retry mechanisms are implementation decisions, not automatically missing product requirements.

| Question / risk | Classification and effect | Responsible role / resolution point |
| --- | --- | --- |
| What duration, dates and real contributor capacity support the preferred four-story backend scope? | **Blocking formal selection/forecast:** no capacity evidence exists. No invented points, velocity or deadline. | PO/Developers during actual Sprint Planning; related focused-review Q-05. |
| How will the backend Goal and remaining full-story AC be represented honestly at Sprint end? | **Blocking an agreed completion forecast:** backend scope is explicit, but absent mobile evidence prevents affected whole-story Done claims. It does not block preparing or implementing selected backend contributions. | PO/Developers review the Goal/scope and residual tracking before selection. Future client delivery/verification remains external to this developer; related Q-02. |
| Can Developers responsibly bound the continuity/security/concurrency work within available capacity? | **Conditional selection blocker:** greatest technical risk is US-AUTH-003. If a major unknown prevents selection, use bounded feasibility investigation before committing affected work; do not perform full design in refinement. | Developers with Tech Lead/QA before selection; Q-05. |
| What exact registration-to-authorized-entry contract, security/credential support, persistence representation and concurrency/recovery mechanics are needed? | **JIT decisions before dependent tests/code:** resolve only for selected work. Preserve fixed policies, including uncertain refresh versus consumed-token replay. Escalate to PO/BA only if a real user-outcome ambiguity or source contradiction emerges. | Developers/Tech Lead during story execution; no policy answer invented here. |
| Which delivered auth/session operations and forbidden outcomes establish the bounded first-private-operation evidence? | **Conditional protection blocker; whole privacy/PBI closure unresolved:** agree relevant scenarios and inspectable evidence. No material untested security boundary can be waived. | PO/Developers with QA for Q-04; scenario detail alongside selected work, finite PBI-002 boundary before its completion claim. |
| Are local Java/Docker/PostgreSQL and CI execution available for the implementation revision, with safe credentials and necessary security support? | **Conditional execution/verification blocker:** foundation exists, but current execution was not revalidated by this planning task. Resolve an actual environment gap before the affected check; do not recreate the baseline. | Developers before dependent work/testing; follow backend README, including its explicit local-password configuration caveat. |
| Can PBI-001 or the broader mobile foundation be declared complete? | **Nonblocking for this backend draft:** their broader completion remains Q-01/Q-02; do not close them automatically or transfer client work to the backend developer. | PO/Developers in the existing refinement process. |
| Do recovery email/provider questions need resolution now? | **Nonblocking here:** Q-03 concerns excluded US-AUTH-005. Preserve future reset/revocation compatibility; do not select a provider or implement reset to answer it. | Developers/PO/BA when recovery becomes a candidate. |

No question above records a stakeholder answer. Minor JIT choices do not require a new approval gate. A discovered requirement or architecture change must use the existing owning artifacts and decision process.

## 9. Feasibility and Scope Adjustment

**Preferred recommendation:** retain the backend contributions of US-AUTH-001–US-AUTH-004 plus applicable US-PRIV-001 obligations. Together they cover creation, usable access, continuity and deliberate termination. Existing build/database/test/CI infrastructure makes a start credible; there is no evidence requiring a product or architecture redesign.

This remains substantial new product engineering: secure credentials, account/session persistence, real security enforcement, migrations and lifecycle/race/failure evidence are all absent from the current implementation. The learning objective also requires capacity for investigation and review. Infrastructure readiness therefore does not prove one-Sprint feasibility.

| Readiness category | Current assessment |
| --- | --- |
| Can begin after selection | Registration and existing-account login behavior under clear policies, using the merged foundation. Applicable protection/tests start with the first operation. |
| Requires JIT design | Necessary API, credential/security, account/session representation, migration, transaction and interruption/retry detail. No whole-Sprint or whole-MVP design gate. |
| Requires planning/product discussion | Capacity-based selection/Goal and handling of incomplete full-story/client acceptance; finite privacy completion boundary if a whole-story/PBI completion claim is proposed. |
| Depends on another capability | Continuity/logout need a usable login session; normal new-account login consumes registration. Synthetic existing accounts/sessions support focused tests. |
| May move later | A whole candidate's backend contribution if actual capacity is insufficient; any remaining full-story client/integration evidence remains visible. Do not remove required security behavior from a retained capability. |

**Reduced fallback for team consideration:** defer US-AUTH-001 self-service registration and retain US-AUTH-002, US-AUTH-003 and US-AUTH-004 backend access for existing accounts, with applicable privacy and the complete session policy. Controlled synthetic existing-account fixtures can support development/review; they are not a new production provisioning method or a claim that real users can onboard.

A corresponding narrower Goal would be: “Deliver verified backend login, session continuity and current-session logout for existing CapsuleAI accounts.” This loses new-user self-service entry and may save limited work because continuity remains the main risk. It still requires a real capacity assessment and explicit team selection; it is not an applied scope change.

If the complete existing-account lifecycle also cannot fit, return to PO/Developers discussion with a bounded, coherent alternative or a justified feasibility investigation. Do not obtain apparent fit by omitting usable session authority, rotation/reuse enforcement, revocation, mandatory tests or full-story residuals. No fallback is selected by this draft.

## 10. Sprint Review Evidence

The following are possible evidence targets, conditional on actual selection and successful implementation.

| Backend demonstration / evidence | What it can establish |
| --- | --- |
| Synthetic registration, normalized-email duplicate rejection and exact-password login | Accepted account identity, required/optional input rules and backend creation/access outcomes. |
| Real HTTP/security path with controlled signed credentials and PostgreSQL authority | Authorized correct-account behavior; missing/invalid/expired/revoked access denied without leakage or accepted-state mutation. |
| Controlled-clock renewal and competing/replayed refresh scenarios | Rotation, fixed lifetime, affected-session/newer-credential denial and separate-session preservation. |
| Logout A while B remains usable; retry/already-unusable/interrupted outcomes | Current-session scope, immediate protected-access denial and truthful failure semantics. |
| Automated policy/API/PostgreSQL reports, real migration evidence, passing current CI and resolved review findings | Repeatable source-linked backend verification rather than foundation smoke or manual requests alone. |
| Per-story AC/evidence and residual mapping, linked to tested revision/environment | Exactly what passed, failed, was not run or remains external; no whole-story Done claim without all required evidence. |

Use synthetic data and sanitized outcomes; do not display raw tokens, passwords or private configuration in review evidence. Label any test-only fixture boundary. Do not add a product endpoint solely for the demo, promise a working mobile journey, or present absent wardrobe/history features as verified. Show incomplete backend work as progress, separately from any work that actually meets the DoD.

## 11. Next Activity

Review this Planning Draft with PO/Developers to establish duration/capacity, confirm a coherent Goal and select feasible work. Preserve the preferred four-story proposal unless that discussion justifies a change. Agree how backend evidence and external full-story residuals will be tracked; no story status changes are made here.

**After actual selection:** start the first selected story's **Just-In-Time / Just-Enough Detailed Design**, resolving only the contracts, data/security and transaction detail it needs. Create a sequence/state model only if complexity or lifecycle risk warrants it. Then use:

**Selected Story → JIT Detailed Design → Automated Tests + Implementation → Refactor → Integration Verification → Pull Request / CI / Review → applicable AC + DoD.**

Repeat and integrate in small steps; adapt the Sprint Backlog as Developers learn. No detailed design, code, tests, migrations, configuration changes, commit, push or PR is performed by this planning task.
