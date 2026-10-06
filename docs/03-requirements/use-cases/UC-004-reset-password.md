# UC-004 — Reset Password

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-004 |
| Use Case Name | Reset Password |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | Email Delivery Service |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Recover access to an existing CapsuleAI account by requesting and completing a valid email password-reset interaction, then authenticating with the new password.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Recover access while distinguishing requested instructions from completed password change. |

## 4. Preconditions

- CapsuleAI's password-recovery interaction is reachable; authenticated access is not needed to request recovery.

## 5. Trigger

The User chooses Forgot Password or opens an issued reset interaction.

## 6. Main Success Scenario

1. The User requests password recovery and supplies the account email.
2. CapsuleAI presents an account-existence-safe initiation response equivalent for registered and unregistered emails; this response does not confirm reset completion.
3. For an existing account, CapsuleAI successfully issues a 30-minute, single-success-use reset interaction, invalidating earlier unused interactions for that account, and arranges email delivery.
4. Email Delivery Service delivers the reset instructions; the User opens the received interaction.
5. CapsuleAI presents the new-password interaction while the reset remains valid.
6. The User supplies a policy-valid new password and requests completion.
7. CapsuleAI accepts the valid reset, changes the password, consumes the interaction and revokes all existing account login/renewal sessions as specified by BRULE-AUTH-005.
8. CapsuleAI confirms completed reset and directs the User to authenticate using the new password.

## 7. Alternative Flows

### A1 — Start from an already issued interaction

At Main Step 1:

1. The User opens a previously delivered interaction.
2. Resume at Main Step 5 if it remains valid; otherwise follow the invalid-interaction exception.

### A2 — Issue replacement instructions

At Main Step 3:

1. The User requests recovery again through Main Steps 1–3.
2. Successful new issuance invalidates all earlier unused interactions for that account; only a currently valid interaction can complete the reset.

### A3 — Cancel before completion

At Main Step 6:

1. The User leaves without submitting a new password.
2. No password change or reset-completion session revocation occurs; the interaction remains governed by its existing expiry/replacement/single-use rules.

## 8. Exception / Failure Flows

### E1 — Unregistered email or instructions not received

At Main Step 4:

1. The User receives no usable instructions and may check the email or request recovery again.
2. CapsuleAI preserves the equivalent initial response and does not disclose account existence or report reset completion.

### E2 — Email delivery unavailable or failed

At Main Step 4:

1. CapsuleAI keeps requested/undelivered recovery distinct from a completed reset and offers account-safe retry/restart guidance.
2. Credentials are not changed merely by requesting or attempting delivery; independent account access/recovery paths remain protected.

### E3 — Expired, consumed or replaced interaction

At Main Step 5:

1. CapsuleAI rejects an interaction at expiry (30 minutes after issuance), after successful use or after replacement by a newly issued interaction.
2. The User can restart at Main Step 1; no credential change is accepted through the rejected interaction.

### E4 — Invalid new password

At Main Step 7:

1. CapsuleAI explains the password-policy problem without completing reset.
2. The User corrects the value and resumes at Main Step 6 while the interaction remains valid.

### E5 — Failed or uncertain reset completion

At Main Step 7:

1. CapsuleAI distinguishes known failure from an outcome that cannot yet be confirmed; neither is labeled completed.
2. The User reviews the result or restarts recovery safely. A consumed interaction cannot be used for a second password change.

## 9. Success Postconditions

- The accepted new password is authoritative; the reset interaction cannot be successfully reused.
- All account login/renewal sessions are revoked for renewal, and subsequent authentication must use the new password.
- Reset completion is distinct from email request, issuance or delivery.

## 10. Minimal / Failure Postconditions

- A requested/delivered interaction alone does not change credentials or establish successful recovery.
- Rejected, canceled or known failed completion leaves the accepted password/session state unchanged; uncertain completion requires outcome review.
- Initial recovery responses do not reveal account existence.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-002` | Recovery uses the account's normalized email identity. |
| `BRULE-AUTH-003` | The new password preserves its value and satisfies the approved policy. |
| `BRULE-AUTH-004` | Interactions last 30 minutes, permit one successful use and are superseded by new issuance. |
| `BRULE-AUTH-005` | Completed reset consumes the interaction, revokes account-wide renewal sessions and preserves initiation privacy. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-01` |
| [Product Features](../../02-product/PRD.md) | `FEAT-AUTH-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-AUTH-003`, `FR-AUTH-004`, `FR-AUTH-015`, `FR-AUTH-017`, `FR-AUTH-018` |
| [Software Requirements — Data](../SRS.md) | `DATA-AUTH-001`, `DATA-AUTH-005` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-002`, `ERR-NET-001`, `ERR-DEP-001` |
| [Software Requirements — Interfaces](../SRS.md) | `SI-003`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-SEC-003`, `NFR-SEC-002`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-002`, `BRULE-AUTH-003`, `BRULE-AUTH-004`, `BRULE-AUTH-005` |
| [Business Requirements](../../01-business/BRD.md) | `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-01` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- The initial Vietnamese response conveys a conditional account-safe meaning; response wording must not confirm whether an account exists.
- No session/token storage or revocation mechanism is selected here. Revocation refers to the SRS's account-wide login/renewal-session boundary.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-002 — Log In](UC-002-log-in.md): Authenticate after a completed reset.
- [UC-003 — Log Out](UC-003-log-out.md): Ordinary logout affects only the current session, unlike completed recovery.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
