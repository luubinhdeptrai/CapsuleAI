# UC-001 — Register Account

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-001 |
| Use Case Name | Register Account |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Create a personal CapsuleAI account using email and password so the User can begin the private wardrobe experience and progressive onboarding.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Create one usable account without unnecessary personal-data requirements. |

## 4. Preconditions

- CapsuleAI's registration interaction is reachable on the supported mobile application.

## 5. Trigger

The User chooses to register an account.

## 6. Main Success Scenario

1. The User requests registration.
2. CapsuleAI asks for Email, Password and Confirm Password and identifies Display Name as optional.
3. The User supplies the required values, optionally supplies Display Name, and submits registration.
4. CapsuleAI checks the required values, matching password confirmation and the approved password/email rules, and reports progress without claiming completion.
5. CapsuleAI confirms successful creation of one account for the normalized email and makes the new account's authorized onboarding/entry path available.
6. The User proceeds to progressive setup or returns later; optional personal context and first-garment creation do not gate authenticated product entry.

## 7. Alternative Flows

### A1 — Omit optional information

At Main Step 3:

1. The User omits Display Name and does not provide body/gender, location permission or garments during registration.
2. Resume at Main Step 4 with the required registration values only.

### A2 — Defer progressive setup

At Main Step 6:

1. The User skips optional setup or the recommended first garment.
2. CapsuleAI permits authorized Today/Wardrobe entry and explains empty-wardrobe or limited-context states as applicable; skipped setup can be completed later.

## 8. Exception / Failure Flows

### E1 — Missing, mismatched or policy-invalid input

At Main Step 4:

1. CapsuleAI identifies missing required input, a password-confirmation mismatch or a password-policy violation without reporting account creation.
2. The User corrects the values and resumes at Main Step 3; cancellation creates no account from this attempt.

### E2 — Duplicate normalized email

At Main Step 4:

1. CapsuleAI creates no second account and offers sign-in/password-recovery guidance.
2. The User may leave registration for UC-002 or UC-004; the existing account remains unchanged.

### E3 — Communication failure or uncertain creation outcome

At Main Step 4:

1. CapsuleAI distinguishes failure/pending/unknown outcome from successful registration.
2. The User reviews the actual outcome or retries safely; an already created normalized identity cannot produce a duplicate account.

## 9. Success Postconditions

- Exactly one account exists for the accepted normalized email; credential policy and confirmation have been satisfied.
- Authorized account entry is available without mandatory Display Name, body/gender, device location or a garment quota.

## 10. Minimal / Failure Postconditions

- Invalid, conflicting or canceled registration creates no new account and does not alter an existing account.
- An uncertain result is not presented as confirmed creation; outcome review and identity uniqueness protect retries.
- Personal account information remains protected.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-002` | Normalized email identifies at most one account; duplicates receive sign-in/recovery guidance. |
| `BRULE-AUTH-003` | Passwords preserve their entered value and follow the approved length/value policy. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-01` |
| [Product Features](../../02-product/PRD.md) | `FEAT-AUTH-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-AUTH-001`, `FR-AUTH-011`, `FR-AUTH-012`, `FR-AUTH-015`, `FR-AUTH-016`, `FR-PROF-001`, `FR-PROF-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-AUTH-001`, `DATA-AUTH-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-002`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-SEC-003`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-002`, `BRULE-AUTH-003` |
| [Business Requirements](../../01-business/BRD.md) | `BR-012`, `BR-021`, `BR-023` |
| [Capabilities](../../01-business/BRD.md) | `CAP-01` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- Passwords have 12–128 characters; spaces and Unicode are permitted without mandatory character-class mixtures or silent trimming/normalization. An approved small denylist may apply; no external password-checking actor is introduced.
- Email comparison trims surrounding whitespace and ignores case without provider-specific alias transformations. Registration adds no mandatory email-verification, guest or social-login journey.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-002 — Log In](UC-002-log-in.md): Access an existing or newly created account.
- [UC-004 — Reset Password](UC-004-reset-password.md): Recover access rather than create a duplicate account.
- [UC-005 — Update Personalization Profile](UC-005-update-personalization-profile.md): Set or revise personalization after account access.
- [UC-006 — Set Location Context](UC-006-set-location-context.md): Choose optional location context after account access.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
