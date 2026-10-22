# US-AUTH-004 — Log out of my current session

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-AUTH-004 |
| Version | 0.3 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Revision note | 2026-10-21: first focused refinement; clarified source-backed acceptance/evidence and traceability; identity, scope and Baseline Draft status preserved. 2026-10-22: linked related full-backlog drafts and explicit identity/domain metadata; existing outcome and AC retained. |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Domain | Authentication |
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

**Then** CapsuleAI confirms logout and presents the signed-out account-access experience. That session cannot renew access or perform protected operations, including with an otherwise valid, unexpired Access Token; protected use requires successful authentication again.

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

- Related first-private-operation evidence is [US-PRIV-001](../privacy/US-PRIV-001-protect-first-private-operations.md) and common mobile entry is [US-MOB-001](../mobile/US-MOB-001-use-vietnamese-mobile-journey-foundation.md). These drafts clarify the existing PBI-002 / useful PBI-003 support; documented stories do not prove those capabilities implemented.

## 8. Out of Scope

Account deletion/export, all-device logout or a session-management screen. Account-wide Refresh Session revocation from a successful password reset is PBI-005. Registration, credential login and returning-session continuity remain sibling PBI-004 stories.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Business | [BRD](../../../01-business/BRD.md): BR-021, BR-023; CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; FEAT-AUTH-001 (logout) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-007, FR-AUTH-008, FR-AUTH-009; DATA-AUTH-005; COM-001, COM-002; ERR-AUTH-003, ERR-NET-001; NFR-SEC-002, NFR-PRIV-001, NFR-PRIV-002, NFR-ACC-001, NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-006, BRULE-AUTH-007 |
| Use Cases | [UC-003 — Log Out](../../../03-requirements/use-cases/UC-003-log-out.md) |
| Activity Diagrams | None for UC-003 in the current baseline. |
| Architecture constraint | [Selected Architecture Decisions](../../../04-architecture/selected-architecture-decisions.md), ADA-008: usable persistent account/session authority is required in addition to token validity. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), selected-story evidence rather than foundation smoke tests alone. |

## 10. Open Refinement Notes

**2026-10-21 refinement draft.** Current-session logout is independently meaningful and stays separate from continuity and account-wide reset effects. AC-01 clarifies that completed revocation denies protected use even before the issued token expires. Verify session A is denied, separate session B remains usable, private client content is hidden and an uncertain logout is not claimed complete. Review shared mobile/privacy feasibility and Developers' sizing (Q-02/Q-04/Q-05); no all-device logout or session-management feature is added.

Question details, owners and conditional planning blockers are centralized in the [focused refinement review](../../backlog-refinement-review.md#6-open-questions-for-product-owner-and-team). These notes do not change story status, assign estimates or authorize implementation. Necessary implementation detail remains JIT for a selected story. The [full story catalogue](../README.md) now accounts for all 41 PBIs; those additional drafts change none of this story's acceptance boundaries.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

