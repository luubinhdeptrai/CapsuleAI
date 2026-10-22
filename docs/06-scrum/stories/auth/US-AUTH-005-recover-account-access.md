# US-AUTH-005 — Recover account access through email

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-AUTH-005 |
| Version | 0.3 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Revision note | 2026-10-21: first focused refinement; clarified source-backed acceptance/evidence and traceability; identity, scope and Baseline Draft status preserved. 2026-10-22: linked related full-backlog drafts and explicit identity/domain metadata; existing outcome and AC retained. |
| Parent PBI | [PBI-005](../../product-backlog.md#pbi-005--recover-access-through-email-password-reset) |
| Domain | Authentication |
| Theme | Foundation & Access |
| Product Goal Contribution | [Enabling / Protecting Product Value](../../product-goal.md#4-desired-product-outcomes) |

## 1. Story

As a registered user who cannot remember my password, I want to recover access through emailed reset instructions, so that I can regain my private wardrobe using a new password.

## 2. User Value

Recovery is a complete request-to-restored-access outcome. Requesting or delivering instructions alone does not restore access, so those steps and their failure paths remain in this story.

## 3. Scope

Forgot Password, account-safe initiation, purpose-minimal email delivery, valid reset completion, replacement/expiry/single-use validation, new-password policy, cancellation and failure/uncertainty, account-wide Refresh Session revocation, and return to credential login.

## 4. Preconditions

- CapsuleAI's password-recovery interaction is reachable. Authentication is not required to request recovery.
- Successful recovery requires an existing account and a valid issued interaction; missing instructions and invalid interactions are handled within the story.

## 5. Acceptance Criteria

### AC-01 — Request without disclosing account existence

**Given** the recovery interaction is reachable and I am signed out

**When** I request Forgot Password for a registered or unregistered email

**Then** the initial user-facing responses are equivalent and convey the conditional meaning that reset instructions have been sent if an account exists. Neither response reveals account existence or claims that the password has changed.

### AC-02 — Deliver instructions for the correct email identity

**Given** my account exists and I request recovery using its email with case or surrounding-whitespace variations

**When** CapsuleAI successfully issues a reset interaction and email delivery succeeds

**Then** instructions reach that account's email identity with a 30-minute, single-success-use interaction. Email comparison ignores case and trims surrounding whitespace without provider-specific alias transformations; issuance or delivery alone leaves the accepted password and account Refresh Sessions unchanged.

### AC-03 — Limit recovery disclosure

**Given** instructions are arranged for an existing account

**When** CapsuleAI exchanges information with Email Delivery Service

**Then** only email and information necessary for recovery delivery is disclosed; unrelated wardrobe, profile and history contents and unrelated secrets are not included.

### AC-04 — Complete recovery and return to login

**Given** I open an unexpired, unused interaction that has not been replaced and supply a policy-valid new password

**When** CapsuleAI accepts my reset submission while the interaction remains valid

**Then** the new password becomes authoritative, the interaction is consumed, and completed reset is confirmed. CapsuleAI directs me to authenticate again; the old password no longer authenticates and the new password permits login to the correct existing private experience.

### AC-05 — Revoke all account Refresh Sessions

**Given** my account has separate active Refresh Sessions on two or more devices

**When** password reset completes successfully

**Then** all account Refresh Sessions are revoked and cannot renew access. Protected operations through those revoked sessions are denied even if their issued Access Tokens remain otherwise valid and unexpired. Subsequent authentication requires the new password; reset does not delete my accepted wardrobe/profile/history and does not affect a different account's sessions.

### AC-06 — Start from already delivered instructions

**Given** I already have a valid issued interaction

**When** I open it directly without making another Forgot Password request

**Then** CapsuleAI offers the new-password interaction and permits the same valid completion path; another issuance is not required.

### AC-07 — Replace all earlier unused interactions

**Given** the account has one or more earlier unused reset interactions

**When** a new reset interaction is successfully issued for that account

**Then** all earlier unused interactions are invalidated and cannot change the password; the new interaction remains usable under its own expiry/single-use rules. A mere unconfirmed request does not establish successful replacement.

### AC-08 — Reject invalid, expired, used or replaced recovery

**Given** the interaction cannot establish valid recovery, has reached 30 minutes after issuance, has already succeeded, or was replaced

**When** I open or submit it to change the password

**Then** CapsuleAI rejects it, reports no completed reset, and offers a fresh account-safe recovery request. That rejected attempt changes no accepted password or session state.

### AC-09 — Recheck validity when completing

**Given** I opened a valid interaction but it expires or is replaced before submission

**When** I submit the new password

**Then** CapsuleAI rejects the now-invalid interaction without changing credentials or revoking sessions based on that rejected attempt; opening earlier does not preserve validity beyond its boundary.

### AC-10 — Validate and correct the new password

**Given** the interaction remains valid and the new password is not on any approved denylist in use

**When** I submit passwords of 11, 12, 128 or 129 characters

**Then** 11 and 129 are rejected with correction guidance; 12 and 128 satisfy the length rule. Policy-valid letters, numbers, symbols, spaces and Unicode require no mandatory class mixture. Rejection does not complete or consume the reset, and I may correct while the interaction remains valid.

### AC-11 — Preserve the accepted password exactly

**Given** I submit a policy-valid new password with spaces or Unicode

**When** reset succeeds and I later log in

**Then** that exact value authenticates without trimming, normalization or transformation; an altered value is not silently treated as the accepted password.

### AC-12 — Cancel before submitting completion

**Given** I have opened an issued interaction but have not submitted a password change

**When** I leave or cancel

**Then** no password change or completion-based session revocation occurs. The interaction retains its existing expiry, replacement and single-success-use boundaries.

### AC-13 — Recover when instructions are unavailable

**Given** no usable instructions arrive, including an unregistered-email request or delivery failure

**When** I review the recovery attempt

**Then** CapsuleAI offers account-safe email-check/retry/restart guidance, retains equivalent non-disclosing initiation behavior, and distinguishes requested or undelivered recovery from completed reset. No credential change or completion-based session revocation occurs merely from requesting or attempting delivery; independent access paths remain available.

### AC-14 — Handle known failed completion

**Given** my valid reset submission is known not to have been accepted

**When** completion fails

**Then** CapsuleAI explains failure without claiming success, leaves the accepted password/session state unchanged, and offers applicable correction/restart guidance.

### AC-15 — Resolve uncertain completion

**Given** communication is interrupted after submission and acceptance cannot be confirmed

**When** I review or restart recovery

**Then** CapsuleAI identifies the outcome as unconfirmed, offers safe outcome review or renewed recovery, and claims neither completed reset nor definite unchanged credentials. If the original submission succeeded, the consumed interaction cannot change the password a second time.

### AC-16 — Operate and understand recovery

**Given** I use the initial Vietnamese recovery interaction with a screen reader or text scaled up to 200%

**When** I request instructions, supply a new password or follow invalid/delivery-failure guidance

**Then** recovery actions and states remain operable and understandable through accessible names/states; messages keep requested, delivered, unconfirmed and completed recovery distinct without disclosing account existence.

## 6. Business Rules and Constraints

- BRULE-AUTH-002–BRULE-AUTH-005 preserve email identity, exact password policy, 30-minute/single-use/replacement validity, initiation privacy and successful-reset effects.
- Replacement occurs on successful new issuance; password change, interaction consumption and all-account Refresh Session revocation occur only on accepted reset completion.
- A small approved password denylist may apply; no provider or new recovery method is selected. The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-004's account/access capability.
- [US-AUTH-002](US-AUTH-002-log-in.md) supplies the return-to-authentication outcome; [US-AUTH-003](US-AUTH-003-resume-session.md) must honor account-wide Refresh Session revocation.
- Email Delivery Service is the established external delivery dependency. Delivery is not assumed to succeed, and this story does not select a provider.
- An existing account can satisfy recovery; repeating registration is not a prerequisite.

- Related first-private-operation evidence is [US-PRIV-001](../privacy/US-PRIV-001-protect-first-private-operations.md) and common mobile entry is [US-MOB-001](../mobile/US-MOB-001-use-vietnamese-mobile-journey-foundation.md). These drafts clarify the existing PBI-002 / useful PBI-003 support; documented stories do not prove those capabilities implemented.

## 8. Out of Scope

Ordinary account creation/login/renewal/logout belong to PBI-004. This story adds no SMS, OTP, security questions, email-verification requirement, social recovery, account deletion, delivery-provider choice, token storage or revocation design.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-005](../../product-backlog.md#pbi-005--recover-access-through-email-password-reset) |
| Business | [BRD](../../../01-business/BRD.md): BR-021, BR-023; CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; FEAT-AUTH-001 (email recovery) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-003, FR-AUTH-004, FR-AUTH-008, FR-AUTH-009, FR-AUTH-015, FR-AUTH-017, FR-AUTH-018; DATA-AUTH-001, DATA-AUTH-005; SI-003, COM-001, COM-002, COM-003; ERR-AUTH-002, ERR-AUTH-003; NFR-SEC-002, NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002, NFR-ACC-001, NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-002, BRULE-AUTH-003, BRULE-AUTH-004, BRULE-AUTH-005, BRULE-AUTH-007 |
| Use Cases | [UC-004 — Reset Password](../../../03-requirements/use-cases/UC-004-reset-password.md); [UC-002 — Log In](../../../03-requirements/use-cases/UC-002-log-in.md), return handoff only |
| Activity Diagrams | [AD-004 — Reset Password](../../../03-requirements/activity-diagrams/AD-004-reset-password.puml) |
| Architecture constraint | [Selected Architecture Decisions](../../../04-architecture/selected-architecture-decisions.md), ADA-008: usable persistent account/session authority is required in addition to token validity. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), selected-story evidence rather than foundation smoke tests alone. |

## 10. Open Refinement Notes

**2026-10-21 refinement draft.** Retain one end-to-end recovery story; an initiation-only slice would not restore access. AC-05 clarifies the existing protected-use consequence of revoked sessions, including unexpired tokens. Email delivery and opening/return feasibility remain unverified in this implementation (Q-03); Developers must also review lifecycle/concurrency testability and whether the story is selectable as written (Q-05), with shared mobile/privacy evidence in Q-02/Q-04. This requires enough feasibility evidence, not a finalized provider contract or reset design before Planning.

Question details, owners and conditional planning blockers are centralized in the [focused refinement review](../../backlog-refinement-review.md#6-open-questions-for-product-owner-and-team). These notes do not change story status, assign estimates or authorize implementation. Necessary implementation detail remains JIT for a selected story. The [full story catalogue](../README.md) now accounts for all 41 PBIs; those additional drafts change none of this story's acceptance boundaries.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

