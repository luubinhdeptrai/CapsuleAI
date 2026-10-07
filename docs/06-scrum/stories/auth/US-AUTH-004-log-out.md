# US-AUTH-004 — Log out of my current session

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

As an authenticated user, I want to log out of my current session, so that I can end access on this device while keeping my wardrobe and other separate sessions intact.

## 2. User Value

Logout provides deliberate control over the current device's personal access without deleting accepted information or unexpectedly signing out other devices.

## 3. Scope

Explicit current-session logout, signed-out handoff, prevention of resumed protected use, separate-session preservation, and already-unusable or unconfirmed revocation outcomes.

## 4. Preconditions

- I have a current authenticated CapsuleAI session to end. A session that becomes unusable during the interaction follows the established alternative.

## 5. Acceptance Criteria

### AC-01 — End the current session

**Given** I have a usable authenticated session

**When** I explicitly request logout and current-session revocation completes

**Then** CapsuleAI confirms logout and presents the signed-out account-access experience. That session cannot renew access, and protected use on it requires successful authentication again.

### AC-02 — Preserve another separate session

**Given** my account has separate sessions A and B on different devices

**When** I successfully log out of session A

**Then** session A loses protected access and renewal; session B is not automatically revoked and can continue under its own validity conditions.

### AC-03 — Retain accepted personal information

**Given** my account has accepted wardrobe, profile, Outfits or Wear Events

**When** logout succeeds

**Then** those records are not deleted or altered by logout and remain available through subsequent authorized access.

### AC-04 — Keep protected content hidden

**Given** logout has ended the current session's personal access

**When** I return through normal navigation or another user logs in on this device

**Then** the preceding user's protected content is not exposed. Signed-out access cannot reopen it without successful authentication, and another account sees only its authorized information.

### AC-05 — Handle an already unusable session

**Given** my current session expires or has already been revoked before logout completes

**When** CapsuleAI identifies that state during logout

**Then** the protected experience ends with an understandable signed-out state; another device's separate session is not targeted.

### AC-06 — Resolve unconfirmed logout

**Given** communication interrupts the logout request and current-session revocation cannot be confirmed

**When** I encounter the unconfirmed result

**Then** CapsuleAI distinguishes it from completed logout and offers applicable outcome review/retry. Protected content does not remain exposed through the logout experience; any renewed personal use requires usable authorization, and no other session is revoked to resolve the uncertainty.

### AC-07 — Operate and understand logout

**Given** I use the Vietnamese interface with a screen reader or text scaled up to 200%

**When** I initiate logout and receive a completed, already-unusable or unconfirmed outcome

**Then** the logout action, access state and applicable recovery/login actions have understandable accessible names/states and remain operable; the message accurately describes the current session's outcome.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-AUTH-006 preserve authorization and current-session-only revocation; ordinary logout has a narrower boundary than completed password reset.
- NFR-SEC-002 protects preceding-user content throughout logout and account switching. The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-002's private-data protection.
- A login session supplied by [US-AUTH-002](US-AUTH-002-log-in.md) or usable existing access is required for the normal outcome. US-AUTH-002 supplies later reauthentication; [US-AUTH-003](US-AUTH-003-resume-session.md) must honor completed revocation.
- Useful sequencing: PBI-003's common mobile entry. Other device sessions need not exist to perform logout.

## 8. Out of Scope

Account deletion/export, all-device logout or a session-management screen. Account-wide Refresh Session revocation from a successful password reset is PBI-005. Registration, credential login and returning-session continuity remain sibling PBI-004 stories.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Business | [BRD](../../../01-business/BRD.md): BR-021, BR-023; CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; FEAT-AUTH-001 (logout) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-007, FR-AUTH-008; DATA-AUTH-005; COM-002; ERR-NET-001; NFR-SEC-002, NFR-PRIV-001, NFR-PRIV-002, NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-006, BRULE-AUTH-007 |
| Use Cases | [UC-003 — Log Out](../../../03-requirements/use-cases/UC-003-log-out.md) |
| Activity Diagrams | None for UC-003 in the current baseline. |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

