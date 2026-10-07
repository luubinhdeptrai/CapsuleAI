# US-AUTH-001 — Register a private account

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

As a new user, I want to register a private account with my email and password, so that I can begin building a trustworthy personal wardrobe.

## 2. User Value

Registration establishes one personal identity for the wardrobe value loop without requiring optional personal information or clothing entry before access.

## 3. Scope

Email/password account creation, required-input and identity validation, explicit creation outcome, correction/cancellation, and the authorized entry handoff. Progressive preference and location setup remain separate capabilities.

## 4. Preconditions

- CapsuleAI's registration interaction is reachable on the supported mobile application.

## 5. Acceptance Criteria

### AC-01 — Create one account

**Given** registration is reachable and the email does not identify an existing account

**When** I submit Email, Password and matching Confirm Password under the established policies

**Then** CapsuleAI confirms successful creation of one account for that email identity and makes the new account's authorized onboarding/entry path available.

### AC-02 — Optional information does not gate entry

**Given** I provide the required registration values but omit Display Name, body/gender information, location permission and a first garment

**When** registration succeeds and I defer optional setup

**Then** authorized Today/Wardrobe entry remains available; no optional field, permission or garment quota prevents access. The handoff identifies available limited-context or empty-wardrobe states rather than demanding completion.

### AC-03 — Correct missing or mismatched input

**Given** Email, Password or Confirm Password is missing, or the supplied passwords differ

**When** I submit registration

**Then** CapsuleAI explains the missing value or mismatch, allows correction, and creates no account or success claim from that rejected attempt. Resubmitting corrected valid values can complete registration.

### AC-04 — Enforce password length without added composition rules

**Given** the other registration values are valid and the password is not on any approved denylist in use

**When** I submit matching passwords of 11, 12, 128 or 129 characters

**Then** 11 and 129 are rejected with correction guidance; 12 and 128 are accepted. A password within those limits is not rejected solely for lacking a mixture of character classes; letters, numbers, symbols, spaces and Unicode are allowed.

### AC-05 — Preserve the exact password value

**Given** I register with a policy-valid password containing spaces or Unicode

**When** I later authenticate with that exact password through the login capability

**Then** the entered value remains valid without silent trimming, normalization or transformation; a different value obtained by changing those characters is not treated as the same password.

### AC-06 — Prevent a duplicate normalized identity

**Given** an account exists for person@example.com

**When** I submit registration using surrounding whitespace and different email case, such as Person@Example.com with surrounding spaces

**Then** no second account is created and the existing account is unchanged; CapsuleAI offers sign-in/password-recovery guidance. Provider-specific alias transformations are not used to merge otherwise distinct email strings.

### AC-07 — Keep an optional name intact

**Given** I choose to supply a Display Name, including supported text with diacritics

**When** registration is accepted and the supplied name is displayed

**Then** the optional name is retained accurately; omitting it would also allow registration.

### AC-08 — Cancel unaccepted registration

**Given** my registration has not been accepted

**When** I cancel or leave before creation

**Then** no new account is created by that canceled attempt and no existing account is changed.

### AC-09 — Distinguish pending, failed and uncertain creation

**Given** I submit registration and communication is delayed, fails or leaves the creation outcome unknown

**When** CapsuleAI cannot yet confirm successful creation

**Then** it identifies pending, known failure or uncertainty appropriately, offers outcome review or safe retry, and does not claim success. Reviewing or retrying an already accepted normalized identity cannot create a duplicate account.

### AC-10 — Protect information during entry

**Given** registration has not established authorized account access

**When** I attempt to reach protected personal content or inspect an access failure

**Then** CapsuleAI grants no protected access and discloses no credential or recovery secrets. Successful entry exposes only the new account's authorized experience.

### AC-11 — Understand and operate registration

**Given** I use the initial mobile interface with TalkBack or VoiceOver, or text scaled up to 200%

**When** I identify the registration inputs, submit valid or invalid values, and follow correction or entry guidance

**Then** the required/optional meanings, progress and creation outcome are understandable through Vietnamese labels and accessible names/states; primary actions remain operable without relying on color alone.

## 6. Business Rules and Constraints

- BRULE-AUTH-002 and BRULE-AUTH-003 preserve normalized email uniqueness and the exact password policy; an approved small denylist is permitted, not required by this story.
- FR-AUTH-011 keeps optional personal context, Display Name and the first garment from becoming access gates.
- BRULE-AUTH-007 preserves purpose-limited private use. The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-002's private-data protection applies from account creation.
- Existing-account access is refined in [US-AUTH-002](US-AUTH-002-log-in.md); registration supplies an account for its new-user path, while existing accounts remain valid inputs.
- Useful sequencing: PBI-003 supplies common mobile entry. PBI-010/PBI-011 own subsequent preferences/location; completing them is not a prerequisite for authenticated entry.

## 8. Out of Scope

Credential login, returning-session renewal and logout are the other PBI-004 stories. Password recovery is PBI-005. Preference editing/progressive setup and location behavior are PBI-010/PBI-011. This story adds no email verification, social/guest access, username authentication, CAPTCHA, OTP, remember-me control, lockout threshold or password-strength meter.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-004](../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions) |
| Business | [BRD](../../../01-business/BRD.md): BR-012, BR-021, BR-023; CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): JRN-01; FEAT-AUTH-001 (registration/entry only) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-001, FR-AUTH-011, FR-AUTH-012, FR-AUTH-015, FR-AUTH-016; DATA-AUTH-001, DATA-AUTH-002; COM-002; ERR-NET-001; NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002, NFR-ACC-002; LOC-001, LOC-004 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-002, BRULE-AUTH-003, BRULE-AUTH-007 |
| Use Cases | [UC-001 — Register Account](../../../03-requirements/use-cases/UC-001-register-account.md) |
| Activity Diagrams | None for UC-001 in the current baseline. |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

