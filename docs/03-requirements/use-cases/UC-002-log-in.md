# UC-002 — Log In

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-002 |
| Use Case Name | Log In |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Obtain or resume authorized access to the correct personal CapsuleAI account without exposing another user's wardrobe or mistaking inaccessible data for an empty wardrobe.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Reach the correct personal wardrobe and recover from unusable credentials/access. |

## 4. Preconditions

- CapsuleAI's account-access interaction is reachable.

## 5. Trigger

The User chooses Log In or returns when CapsuleAI needs to establish usable account access.

## 6. Main Success Scenario

1. The User requests account access.
2. CapsuleAI requests Email and Password when credential-based authentication is needed.
3. The User supplies the credentials and requests login.
4. CapsuleAI establishes authorized access for the normalized email using the password exactly as supplied and confirms success.
5. CapsuleAI opens the correct personal experience, offering progressive onboarding when relevant or returning to the intended product area where practical.

## 7. Alternative Flows

### A1 — Returning with usable access

At Main Step 2:

1. CapsuleAI recognizes usable existing account access and opens that User's personal experience without requiring unnecessary credential entry.
2. The use case completes with the same ownership/access protections as the main scenario.

### A2 — Renewal within the existing session

At Main Step 2:

1. CapsuleAI restores usable access when the existing login session permits renewal within its maximum lifetime.
2. The User continues to Main Step 5; renewal does not create a new wardrobe or extend the existing session maximum.

### A3 — Optional onboarding remains incomplete

At Main Step 5:

1. The User defers optional body/gender, location or first-garment setup.
2. CapsuleAI permits authenticated entry and explains the resulting context/inventory limitations.

## 8. Exception / Failure Flows

### E1 — Invalid or incomplete credentials

At Main Step 4:

1. CapsuleAI refuses access and supplies understandable correction/recovery guidance without displaying protected data.
2. The User corrects input and resumes at Main Step 3 or requests UC-004.

### E2 — Returning access cannot be renewed

At Main Step 2:

1. CapsuleAI explains that authentication is required again for expired/revoked/unusable session access.
2. Protected use pauses; resume at Main Step 2. The wardrobe is not represented as empty or deleted.

### E3 — Communication failure or uncertain access outcome

At Main Step 4:

1. CapsuleAI distinguishes interrupted access from successful login.
2. The User reviews/retries access; no personal content is exposed until authorized access is established.

## 9. Success Postconditions

- The User has usable authorized access to the correct account's personal information.
- A returning account retains its wardrobe and accepted personal state; skipped optional setup does not block access.

## 10. Minimal / Failure Postconditions

- Unsuccessful login or renewal grants no protected access and changes no personal wardrobe/profile/history.
- An expired or revoked session cannot continue protected use; inaccessible data is not labeled empty.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Protected operations require authorized access to the affected user's information. |
| `BRULE-AUTH-002` | Login uses the same normalized email identity as registration. |
| `BRULE-AUTH-003` | The password is compared without silent transformation. |
| `BRULE-AUTH-006` | Separate sessions and renewal/revocation preserve the approved access boundaries. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-01` |
| [Product Features](../../02-product/PRD.md) | `FEAT-AUTH-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-AUTH-002`, `FR-AUTH-005`, `FR-AUTH-006`, `FR-AUTH-008`, `FR-AUTH-010`, `FR-AUTH-011`, `FR-AUTH-013`, `FR-AUTH-014` |
| [Software Requirements — Data](../SRS.md) | `DATA-AUTH-001`, `DATA-AUTH-002`, `DATA-AUTH-005` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-SEC-002`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-AUTH-002`, `BRULE-AUTH-003`, `BRULE-AUTH-006`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-012`, `BR-021`, `BR-023` |
| [Capabilities](../../01-business/BRD.md) | `CAP-01` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- A login session has a maximum 30-day lifetime; permitted renewal cannot extend that maximum. Access renewal mechanics remain outside this interaction specification.
- Account switching cannot expose the preceding user's protected content. Missing optional setup is distinct from failed authentication.

- Access exposes only the authorized User's private information; login is not consent to unrestricted sharing or personal-data AI training.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-001 — Register Account](UC-001-register-account.md): Create an account when needed.
- [UC-003 — Log Out](UC-003-log-out.md): End this session's personal access.
- [UC-004 — Reset Password](UC-004-reset-password.md): Reset credentials through email recovery.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-002-log-in.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
