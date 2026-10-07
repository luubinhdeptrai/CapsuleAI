# US-AUTH-003 — Resume access within my login session

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

As a returning user, I want to resume my private experience while my login session is usable, so that I can continue wardrobe use without unnecessary credential entry and recognize when I must log in again.

## 2. User Value

Returning access preserves continuity with accepted personal information while keeping the established expiry, renewal and revocation boundaries visible through usable or renewed access.

## 3. Scope

Existing-session return, access renewal, maximum session lifetime, refresh rotation/reuse outcomes, interrupted renewal and reauthentication handoff. These are one continuity outcome rather than standalone token tasks.

## 4. Preconditions

- CapsuleAI's account-access interaction is reachable.
- I previously established a login session; expired, revoked or unusable sessions are handled within the story.

## 5. Acceptance Criteria

### AC-01 — Return with usable access

**Given** my account/session authorization is usable and my Access Token is valid and unexpired

**When** I return to CapsuleAI

**Then** the correct private experience opens without unnecessary credential entry, retaining my accepted wardrobe/profile/history and returning to the intended area where practical.

### AC-02 — Renew at access expiry

**Given** my 15-minute Access Token has expired but my Refresh Session remains usable within its maximum lifetime

**When** I attempt to resume protected use

**Then** the expired token alone does not grant access; successful renewal restores access to the same account and its accepted information without requiring credentials or creating a new wardrobe.

### AC-03 — Rotate renewal credentials

**Given** my current Refresh Token is usable and the session has not reached its maximum lifetime

**When** access renewal succeeds

**Then** the current Refresh Token is consumed and replacement Access and Refresh Tokens are issued. The consumed token cannot perform another ordinary renewal; my continued authorized access uses the renewed validity.

### AC-04 — Enforce the absolute session maximum

**Given** my login session has reached its maximum 30-day lifetime, including after earlier successful rotations

**When** I attempt to renew or continue protected use

**Then** renewal does not extend that original maximum, protected session use pauses, and CapsuleAI explains that authentication is required again. This does not delete or label my wardrobe empty.

### AC-05 — Handle rotated-token reuse

**Given** a Refresh Token has already been consumed by a successful rotation

**When** that rotated token is reused

**Then** the affected login session is revoked and requires renewed authentication; its newer refresh credential cannot continue renewing that revoked session. Another separate login session is not automatically revoked by this affected-session response.

### AC-06 — Reauthenticate after unusable access

**Given** the returning session is expired, revoked or otherwise unusable

**When** I try to open protected wardrobe or another personal area

**Then** CapsuleAI pauses protected use, explains renewed authentication, and offers the login route. After valid credential login, the correct accepted personal state is available and the intended area is restored where practical.

### AC-07 — Keep separate sessions distinct

**Given** I have separate usable sessions on two devices

**When** I resume or renew access in one session

**Then** the action remains associated with that session and account; it does not replace the other session or expose another account's personal content. Current-session logout and account-wide password-reset revocation retain their separate established boundaries.

### AC-08 — Resolve interrupted renewal

**Given** a renewal response is pending, fails or cannot confirm its outcome

**When** usable authorized access has not yet been established

**Then** CapsuleAI distinguishes that access problem from success or empty inventory and offers applicable review/retry or reauthentication. It does not continue protected use on expired/unusable authorization or claim a refresh completed solely because it was attempted; accepted personal information remains intact.

### AC-09 — Understand renewed-access guidance

**Given** my session cannot resume usable protected access

**When** I encounter the authentication-required state using the Vietnamese interface and applicable assistive technology

**Then** the state and login/recovery actions are understandable through accessible labels/states, remain operable at text scaling up to 200%, and distinguish inaccessible information from a genuinely empty wardrobe.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-AUTH-006 require authorized account/session use, rotation and affected-session revocation.
- The existing SRS constraint is a 15-minute JWT Access Token and maximum 30-day Refresh Token/login session. Rotation never restarts the session maximum; expired access requires usable renewal or successful authentication.
- Revocation mechanisms and token storage remain downstream choices. The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-002's private-data protection.
- [US-AUTH-002](US-AUTH-002-log-in.md) establishes a credential-based login session and restores access after expiry/revocation. Existing sessions are valid inputs; registration need not run again.
- Useful sequencing: PBI-003 supplies the common mobile access/recovery experience. [US-AUTH-004](US-AUTH-004-log-out.md) and PBI-005 supply revocation scenarios, not extra gates for every return.

## 8. Out of Scope

Credential-login validation is US-AUTH-002; account creation is US-AUTH-001; explicit logout is US-AUTH-004. Password-reset completion and all-account Refresh Session revocation are PBI-005. No session-management dashboard, all-device-logout control, remember-me setting, new access method, storage mechanism or API contract is introduced.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Business | [BRD](../../../01-business/BRD.md): BR-021, BR-023; CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; FEAT-AUTH-001 (returning access) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-005, FR-AUTH-006, FR-AUTH-008–FR-AUTH-010, FR-AUTH-013, FR-AUTH-014; DATA-AUTH-005; ERR-AUTH-003, COM-002; NFR-SEC-002, NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002, NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-006, BRULE-AUTH-007 |
| Use Cases | [UC-002 — Log In](../../../03-requirements/use-cases/UC-002-log-in.md), returning-access alternatives and failure flows |
| Activity Diagrams | None for UC-002 in the current baseline. |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

