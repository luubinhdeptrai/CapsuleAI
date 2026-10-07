# US-AUTH-002 — Log in to my private account

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Version | 0.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Theme | Foundation & Access |
| Product Goal Contribution | [Enabling / Protecting Product Value](../../product-goal.md#4-desired-product-outcomes) |

## 1. Story

As a registered user, I want to log in with my email and password, so that I can reach my private wardrobe and continue using my accepted information.

## 2. User Value

Credential login gives an existing user a clear route into the correct personal experience, including after loss of usable session access.

## 3. Scope

Credential-based authentication, account identification, correction and recovery entry, authorized product handoff, and safe failure or uncertain-access handling. Returning without credential entry and session renewal are refined separately.

## 4. Preconditions

- CapsuleAI's account-access interaction is reachable.
- Successful credential login requires an existing account; incomplete or incorrect credentials are handled within this story.

## 5. Acceptance Criteria

### AC-01 — Reach the correct personal experience

**Given** I have a registered account and its current valid password

**When** I submit its Email and Password

**Then** CapsuleAI confirms authorized access and opens that account's personal experience, offering the relevant onboarding path or returning to the intended product area where practical. My existing accepted wardrobe/profile/history is retained.

### AC-02 — Use the normalized email identity

**Given** my registered email is person@example.com

**When** I log in with matching credentials and vary only email case or surrounding whitespace

**Then** CapsuleAI identifies the same account. Provider-specific alias transformations do not make an otherwise different email identify that account.

### AC-03 — Compare the password exactly

**Given** my accepted password includes policy-valid spaces or Unicode

**When** I submit that exact password or a different value produced by removing spaces or changing Unicode characters

**Then** the exact value authenticates with the correct email; the changed value does not authenticate as if it were the original. No silent password trimming, normalization or transformation occurs.

### AC-04 — Correct invalid or incomplete credentials

**Given** my email/password input is incomplete or does not authenticate the account

**When** I request login

**Then** CapsuleAI refuses protected access, explains useful correction/recovery choices, and leaves accepted personal information unchanged. I can correct and resubmit or choose Forgot Password; the failure is not presented as an empty/deleted wardrobe.

### AC-05 — Enter despite incomplete optional setup

**Given** I have valid credentials but no Display Name, body/gender context, location permission or first garment

**When** login succeeds

**Then** authenticated product entry remains available without completing those optional choices or a garment quota; relevant context/inventory limitations are explained by the available destination.

### AC-06 — Isolate accounts and secrets

**Given** one user's personal experience was previously displayed on this device

**When** I authenticate as a different registered user or attempt access without authorization

**Then** only the currently authorized user's personal content is accessible. The preceding user's protected content and credential/session/recovery secrets are not exposed.

### AC-07 — Resolve interrupted login safely

**Given** communication delays, fails or leaves my login outcome uncertain

**When** authorized access cannot yet be established

**Then** CapsuleAI distinguishes the pending, failed or unconfirmed attempt from successful login, offers review/retry, and shows no protected content until usable authorized access is established. Accepted personal information is unchanged by unsuccessful access.

### AC-08 — Follow understandable access guidance

**Given** I use the Vietnamese login interaction with a screen reader or text scaled up to 200%

**When** I submit credentials and follow success, correction or Forgot Password guidance

**Then** Email/Password inputs, access state and recovery actions have meaningful accessible names/states and remain operable; success and failure are understandable without color-only cues.

## 6. Business Rules and Constraints

- BRULE-AUTH-001–BRULE-AUTH-003 enforce authorized personal access, normalized email comparison and exact password meaning.
- FR-AUTH-009 requires valid, unexpired access and authorized account/session context; lifetime and renewal behavior are refined in US-AUTH-003.
- FR-AUTH-011 preserves optional-entry boundaries. The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-002's isolation and secret protection.
- [US-AUTH-001](US-AUTH-001-register-account.md) supplies newly created accounts; an existing account can satisfy the same input without requiring registration to run again.
- [US-AUTH-003](US-AUTH-003-resume-session.md) uses credential login when returning access requires authentication again. [US-AUTH-005](US-AUTH-005-recover-account-access.md), under PBI-005, owns password recovery.
- Useful sequencing: PBI-003's common mobile entry; PBI-010/PBI-011's later setup must not block this outcome.

## 8. Out of Scope

Registration, automatic returning access/refresh and deliberate logout are sibling stories. Password-reset request/completion is PBI-005; selecting the established Forgot Password entry does not implement that journey here. Profile/preferences and environmental setup are PBI-010/PBI-011. No new authentication method, extra verification step, lockout threshold or remember-me control is introduced.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Business | [BRD](../../../01-business/BRD.md): BR-012, BR-021, BR-023; CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; FEAT-AUTH-001 (credential access) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-002, FR-AUTH-008, FR-AUTH-009, FR-AUTH-011; DATA-AUTH-001, DATA-AUTH-002; ERR-AUTH-001, COM-002; NFR-SEC-001–NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002, NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-002, BRULE-AUTH-003, BRULE-AUTH-007 |
| Use Cases | [UC-002 — Log In](../../../03-requirements/use-cases/UC-002-log-in.md), credential path |
| Activity Diagrams | None for UC-002 in the current baseline. |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

