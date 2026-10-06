# UC-003 — Log Out

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-003 |
| Use Case Name | Log Out |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

End the current device/login session's personal CapsuleAI access while leaving the account's information and separate device sessions intact.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Stop access through the current session without losing the wardrobe. |

## 4. Preconditions

- The User has a current authenticated CapsuleAI session to end.

## 5. Trigger

The User explicitly requests Log Out.

## 6. Main Success Scenario

1. The User requests logout from the current account/session.
2. CapsuleAI identifies the current session affected by that request.
3. CapsuleAI completes current-session revocation, ends its personal access and confirms logout.
4. CapsuleAI presents the signed-out account-access experience; subsequent protected use requires successful authentication.

## 7. Alternative Flows

### A1 — Session already became unusable

At Main Step 2:

1. CapsuleAI identifies that the current session has expired or is already revoked and ends its protected experience.
2. CapsuleAI explains the signed-out state; another device's separate session is not targeted.

## 8. Exception / Failure Flows

### E1 — Logout cannot be confirmed

At Main Step 3:

1. CapsuleAI distinguishes an interrupted/unconfirmed revocation from completed logout and offers applicable outcome review/retry.
2. Protected content must not remain exposed through the logout experience; renewed personal access requires usable authorization.
3. The User reviews the result or resumes logout when possible; no other session is revoked merely to resolve uncertainty.

## 9. Success Postconditions

- The current session is revoked and cannot renew personal access; successful authentication is required to resume it.
- Other separate device sessions and the account's accepted wardrobe/profile/history remain intact.

## 10. Minimal / Failure Postconditions

- No wardrobe, profile, Outfit or Wear Event is deleted by logout.
- An uncertain revocation is not reported as confirmed; account switching/logout must not expose prior-user content.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information remains subject to authorized access. |
| `BRULE-AUTH-006` | Logout targets the current session and does not automatically revoke separate sessions. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-01` |
| [Product Features](../../02-product/PRD.md) | `FEAT-AUTH-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-AUTH-007`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-AUTH-005` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-SEC-002`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-AUTH-006` |
| [Business Requirements](../../01-business/BRD.md) | `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-01` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- Logout confirmation describes the current session's outcome; it is not an account-deletion or all-device-logout operation.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-002 — Log In](UC-002-log-in.md): Establish access again after logout.
- [UC-004 — Reset Password](UC-004-reset-password.md): Successful password reset has broader account-wide session-renewal revocation.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
