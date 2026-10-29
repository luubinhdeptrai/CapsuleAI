# US-AUTH-002 — Log In to My Private Account: Implementation Guide

| Field | Value |
| --- | --- |
| Story | [US-AUTH-002 — Log in to my private account](../../../stories/auth/US-AUTH-002-log-in.md) (v0.3, Baseline Draft) |
| Parent PBI | [PBI-004](../../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions); hard dependency [PBI-002](../../../product-backlog.md#pbi-002--protect-private-user-data-from-the-first-usable-increment) |
| Implementation order | **2 of 4** — after [US-AUTH-001](US-AUTH-001-guide.md); next: [US-AUTH-004](US-AUTH-004-guide.md) |
| Starts from | Stage 1 (registration merged, all Stage 1 tests green) |
| Stage produced | **Stage 2** — registration + login + JWT + persistent session authority on every protected request |
| Read first | [README](README.md) → [Shared Architecture §4–§6](shared-architecture.md#4-authentication-lifecycle) |
| Status | Reference code for manual implementation. **Not compiled or executed by the author.** |

---

## 1. Story Overview

**User story.** As a registered user, I want to log in with my email and password, so that I can reach my private wardrobe and continue using my accepted information.

| Item | Description |
| --- | --- |
| User value | A clear, safe route into **the correct** personal account. |
| Backend objective | `POST /api/v1/auth/login` checks the normalized email and the **exact** password, creates a persistent login session in PostgreSQL, and issues a 15-minute JWT access token plus a refresh token. Every other route becomes protected: a request needs a valid JWT **and** a usable session. |
| In scope (backend) | Credential verification; uniform rejection of wrong/unknown credentials; session creation; token issuance; the application-wide security filter chain; account isolation; secrets kept out of output and logs. |
| Out of scope | Renewal with the refresh token (US-AUTH-003), logout (US-AUTH-004), password reset (PBI-005), lockout, remember-me, social login. Mobile navigation and cache clearing. |
| Dependencies | `Account`, `EmailAddress`, `PasswordHasher`, `AccountRepository`, `IdentityApiExceptionHandler`, `IdentityConfiguration` and the test support from Stage 1. |
| Expected outcome | Stage 2: registration still works and stays public; login works; any non-public route answers 401 without a usable session; all Stage 1 and Stage 2 tests pass. |
| Relationship | US-AUTH-004 adds revocation to the session and a protected logout endpoint. US-AUTH-003 consumes the refresh token issued here. |

**Why the refresh token is issued now.** The SRS defines the login session as the "Refresh Session" (FR-AUTH-010). Issuing its first refresh token at login makes the login contract final in this stage, and gives US-AUTH-004 a real session to revoke. The renewal endpoint itself arrives in US-AUTH-003. Until then, the refresh token is stored (hashed) and returned, but nothing accepts it.

---

## 2. Business Requirements

### 2.1 Scenarios

| Scenario | Backend behaviour | Source |
| --- | --- | --- |
| **Normal** — correct email + current password | 200 with tokens for that account; account data untouched. | [UC-002 Main](../../../../03-requirements/use-cases/UC-002-log-in.md#6-main-success-scenario), FR-AUTH-002 |
| **Alternative** — email typed with other case/spaces | Same account. | FR-AUTH-002, BRULE-AUTH-002 |
| **Alternative** — optional setup never done | Login still succeeds. | UC-002 A3, FR-AUTH-011 |
| **Alternative** — logging in on a second device | A **separate** session; the first keeps working. | BRULE-AUTH-006 |
| **Exception** — missing email/password | 400 explaining which is missing. | UC-002 E1 |
| **Exception** — wrong password or unknown email | 401 `INVALID_CREDENTIALS`, identical for both; no session; account unchanged. | UC-002 E1, ERR-AUTH-001 |
| **Exception** — password with spaces trimmed / different Unicode form | Rejected — not the same password. | FR-AUTH-002, BRULE-AUTH-003 |
| **Security** — request to any protected route without usable authority | 401 `AUTHENTICATION_REQUIRED`; no content. | FR-AUTH-008/009, ADA-008 |
| **Infrastructure** — PostgreSQL unreachable while the session is being checked | Fail closed: no access, no content; 503 `TEMPORARILY_UNAVAILABLE` (not 401, because the credentials were not found invalid). | FR-AUTH-009, COM-002, ERR-DEP-001 ("do not present a dependency failure as loss of the wardrobe") |
| **Security** — token for another account's session, forged, wrong issuer, expired | 401. | FR-AUTH-009, NFR-SEC-001 |
| **Recovery** — response lost | Retry is safe; no protected content exposed until tokens arrive. | UC-002 E3, COM-002 |

### 2.2 Requirements traceability

| Requirement | Short meaning | Relevant AC | Source |
| --- | --- | --- | --- |
| FR-AUTH-002 | Email trimmed + case-insensitive; password untransformed. | AC-02, AC-03 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-008 | Protected operations only for the authorized account. | AC-06 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-009 | Valid unexpired JWT **and** authorized session/account context. | AC-01, AC-06 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-010 | 15-minute access token; 30-day session maximum. | AC-01 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-011 | No optional setup gates entry. | AC-05 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| DATA-AUTH-001/002/005 | Protected credentials; normalized email; separate sessions with account, lifetime, refresh validity. | AC-02, AC-03, AC-06 | [SRS §4.2](../../../../03-requirements/SRS.md#42-account--profile-data) |
| ERR-AUTH-001 | Deny invalid credentials with guidance; never "empty wardrobe"; no state change. | AC-04 | [SRS §9.1](../../../../03-requirements/SRS.md#91-access-and-recovery) |
| COM-001 / COM-002 | Protect secrets; interrupted ≠ success. | AC-06, AC-07 | [SRS §5.4](../../../../03-requirements/SRS.md#54-communication-interfaces) |
| NFR-SEC-001/002/003 | No cross-user access; account-switch isolation; no secrets in output/logs. | AC-06 | [SRS §6.2](../../../../03-requirements/SRS.md#62-security) |
| ADA-008 | PostgreSQL is the persistent session authority; JWT validity alone is not enough. | AC-06 | [Selected Decisions ADA-008](../../../../04-architecture/selected-architecture-decisions.md#ada-008--session-refresh-and-reset-authority), [ADD §9.1](../../../../04-architecture/ADD.md#91-authentication-authorization-and-session-lifecycle) |
| US-PRIV-001 AC-01/02/03 | Ownership, usable authority, secrets for the first protected operations. | AC-06 | [US-PRIV-001](../../../stories/privacy/US-PRIV-001-protect-first-private-operations.md#5-acceptance-criteria) |

---

## 3. Business Rules

| Rule | Simple meaning | Example | Backend implication |
| --- | --- | --- | --- |
| [BRULE-AUTH-001](../../../../03-requirements/business-rules.md#brule-auth-001--authorized-personal-operations) | Personal operations need authentication **and** authorization; "no access" is never "empty". | A request without a token to a future `/api/v1/wardrobe/garments` must get 401, not `[]`. | Default-deny filter chain; 401 body says `AUTHENTICATION_REQUIRED`. |
| [BRULE-AUTH-002](../../../../03-requirements/business-rules.md#brule-auth-002--normalized-email-identity) | Same normalized email as registration. | `" PERSON@example.com "` logs into `person@example.com`. | Reuse `EmailAddress.normalize`. |
| [BRULE-AUTH-003](../../../../03-requirements/business-rules.md#brule-auth-003--password-value-preservation) | Compare the password exactly. | Registered `"  Mật khẩu rất dài  "`; `"Mật khẩu rất dài"` (no spaces) fails. | Pass the raw string to `PasswordHasher.matches`. No length policy at login (a wrong value just fails). |
| [BRULE-AUTH-006](../../../../03-requirements/business-rules.md#brule-auth-006--separate-sessions-and-revocation) | Separate device sessions are distinct. | Phone and tablet logins create two sessions. | One `identity_session` row per login. |
| [BRULE-AUTH-007](../../../../03-requirements/business-rules.md#brule-auth-007--purpose-limited-private-data-use) | Purpose-limited data. | Tokens contain only ids and times — no email, no name. | Minimal JWT claims. |

---

## 4. Acceptance Criteria

| AC | Summary | Backend effect | Verification type |
| --- | --- | --- | --- |
| [AC-01](../../../stories/auth/US-AUTH-002-log-in.md#ac-01--reach-the-correct-personal-experience) Reach the correct personal experience | Valid credentials → authorized access to **that** account; data retained. | 200 + tokens + `accountId`; protected request resolves to that account; account row unchanged. | **Backend**; **External** (navigation to onboarding/intended area). |
| [AC-02](../../../stories/auth/US-AUTH-002-log-in.md#ac-02--use-the-normalized-email-identity) Normalized email | Case/space variants → same account; no alias merging. | `EmailAddress.normalize`. | **Backend**. |
| [AC-03](../../../stories/auth/US-AUTH-002-log-in.md#ac-03--compare-the-password-exactly) Exact password | Exact value works; trimmed/renormalized value fails. | PBKDF2 exact comparison. | **Backend**. |
| [AC-04](../../../stories/auth/US-AUTH-002-log-in.md#ac-04--correct-invalid-or-incomplete-credentials) Invalid/incomplete credentials | Refuse access, explain, change nothing. | 400 / 401 `INVALID_CREDENTIALS`; no session; account unchanged. | **Backend**; **External** (Forgot Password link, wording). |
| [AC-05](../../../stories/auth/US-AUTH-002-log-in.md#ac-05--enter-despite-incomplete-optional-setup) Optional setup incomplete | Login works without name/profile/location/garment. | No optional data read. | **Backend**; **External** (destination explains limits). |
| [AC-06](../../../stories/auth/US-AUTH-002-log-in.md#ac-06--isolate-accounts-and-secrets) Isolate accounts and secrets | Only the current account's content; no secrets exposed. | Session-aware JWT check; tokens/passwords never logged. | **Backend** (security chain); **External** (client clears previous user's screens/cache). |
| [AC-07](../../../stories/auth/US-AUTH-002-log-in.md#ac-07--resolve-interrupted-login-safely) Interrupted login | Distinguish pending/failed; no protected content until access. | Tokens returned only after commit; failures write nothing. | **Backend**; **External** (pending/retry UI). |
| [AC-08](../../../stories/auth/US-AUTH-002-log-in.md#ac-08--follow-understandable-access-guidance) Accessible guidance | Vietnamese, screen reader, 200%. | Stable codes. | **External only**. |

---

## 5. Just-in-Time Detailed Design

### 5.1 New and changed classes

| Layer | Class | Responsibility | Why it exists |
| --- | --- | --- | --- |
| domain | `SessionPolicy` | 15 min / 30 days; access expiry capped at session expiry. | Policy numbers in one place. |
| domain | `LoginSession` | Session record; `establish`, `isUsableAt`, `belongsTo`. | The persistent authority (DATA-AUTH-005). |
| domain | `RefreshTokenHash` | SHA-256 hex of a refresh token. | Only hashes are stored (TD-11). |
| api | `AuthenticatedAccount` | (accountId, sessionId) of a verified request. | Published principal for every module. |
| api | `SessionAuthority` | "Is this session usable for this account now?" | Published authority contract (Implementation View: Wiring → Identity API). |
| application | `LogInService` | Verify credentials, create session, issue tokens. | The use case. |
| application | `SessionAuthorityService` | Implements `SessionAuthority` from stored sessions. | Keeps the rule in application/domain, not in a filter. |
| application | `LogInCommand`, `SessionTokens`, `InvalidCredentialsException` | Input, output, rejection. | — |
| ports | `SessionRepository`, `IdentityTransactions`, `AccessTokenIssuer`, `RefreshTokenGenerator` | Session persistence, transaction boundary, token creation. | See [Shared Architecture §3.3](shared-architecture.md#33-why-each-abstraction-exists-and-what-was-deliberately-not-added). |
| ports | `AccountRepository` **(modified)** | + `findByEmail`. | Login looks accounts up. |
| adapter.out | `JdbcSessionRepository`, `SpringIdentityTransactions`, `JdbcTime` | SQL, `TransactionTemplate`, time mapping. | — |
| adapter.out | `JdbcAccountRepository` **(modified)** | + `findByEmail`. | — |
| adapter.jwt | `JwtClaimNames` | The `sid` claim name, shared by token issuing and token reading. | Inbound and outbound adapters must not import each other (TD-25). |
| adapter.out | `JwtAccessTokenIssuer`, `SecureRandomRefreshTokenGenerator` | Sign HS256 JWT; 256-bit random tokens. | — |
| adapter.in.security | `AccessTokenExpiryValidator` | `now ≥ exp` → invalid, zero skew, application clock. | Exact 15-minute boundary (TD-09). |
| adapter.in.security | `SessionAwareJwtAuthenticationConverter` | JWT → `AuthenticatedAccountToken` **only** if the session is usable; an unreachable database becomes `SessionAuthorityUnavailableException`. | ADA-008 on every request; TD-26. |
| adapter.in.security | `SessionAuthorityUnavailableException` | "Authority could not be checked" — an `AuthenticationException` the entry point turns into 503. | Fail closed without telling the client its credentials are bad. |
| adapter.in.security | `AuthenticatedAccountToken` | Spring `Authentication` with `AuthenticatedAccount` principal. | Enables `@AuthenticationPrincipal AuthenticatedAccount`. |
| adapter.in.security | `ProblemJsonAuthenticationEntryPoint` | 401 + `WWW-Authenticate` + problem JSON; 503 problem JSON for an authority outage. | Consistent error contract. |
| adapter.in.security | `PublicEndpointAwareBearerTokenResolver` | Ignore `Authorization` on public endpoints. | TD-17. |
| configuration | `AccessTokenProperties` | Binds and validates the Base64 signing secret. | Fail fast on missing/weak secret. |
| configuration | `IdentityTokenConfiguration` | `JwtEncoder`, `JwtDecoder` (+ validators), issuer, generator beans. | — |
| configuration | `IdentitySecurityConfiguration` | The single default-deny `SecurityFilterChain`. | TD-19. |
| configuration | `IdentityConfiguration` **(modified)** | + session repository, transactions, login service, authority service. | — |
| adapter.in.web | `LoginController`, `LoginRequest`, `TokenResponse` | HTTP. | — |
| adapter.in.web | `IdentityApiExceptionHandler` **(modified)** | + 401 `INVALID_CREDENTIALS`. (503 stays limited to unreachable-database exceptions, as in Stage 1.) | — |

### 5.2 HTTP contract

`POST /api/v1/auth/login` — public.

```json
{ "email": " Person@Example.com ", "password": "correct horse battery staple" }
```

| Outcome | Status | Body |
| --- | --- | --- |
| Success | 200 | `TokenResponse` — see [Shared Architecture §7](shared-architecture.md#7-http-api-contract-final-stage-4). `Cache-Control: no-store` is added by Spring Security. |
| Missing field | 400 | `VALIDATION_FAILED`, `fieldErrors` with `REQUIRED` for `email` and/or `password` |
| Wrong password **or** unknown email | 401 | `INVALID_CREDENTIALS` (byte-identical bodies) |
| Not JSON | 400 | `MALFORMED_REQUEST` |
| Database unreachable | 503 | `TEMPORARILY_UNAVAILABLE` |

Every other request without a usable session:

```http
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer error="invalid_token", error_description="…", error_uri="…"
Content-Type: application/problem+json

{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Authentication is required. Log in again to continue.","code":"AUTHENTICATION_REQUIRED"}
```

(Without any token the header is just `WWW-Authenticate: Bearer`.)

A protected request whose session **cannot be checked** because PostgreSQL is unreachable:

```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/problem+json

{"type":"about:blank","title":"Service Unavailable","status":503,"detail":"Your access could not be verified right now. Nothing was changed. Try again shortly.","code":"TEMPORARILY_UNAVAILABLE"}
```

No `WWW-Authenticate` header is sent: the client is not being asked to authenticate again. The body is a fixed string, so it never contains exception text, SQL, host names or credentials.

### 5.3 Token and session rules

| Item | Rule |
| --- | --- |
| Session | `established_at = now` (seconds), `expires_at = now + 30 days`. Never updated. |
| Access token | HS256, `iss=capsuleai-identity`, `sub=accountId`, `sid=sessionId`, `iat=now`, `exp=min(now + 15 min, expires_at)`, header `typ=JWT`. |
| Valid when | Signature OK, `typ` is `JWT` (or absent), `iss` matches, **`now < exp`**, and the session row exists, belongs to `sub`, and `now < expires_at`. |
| Refresh token | 32 bytes from `SecureRandom`, Base64url without padding. Only `SHA-256(token)` (lower-case hex) is stored. |
| Signing secret | `capsuleai.identity.access-token.signing-secret`: Base64, ≥ 32 decoded bytes. Startup fails otherwise. |

### 5.4 Transaction

The two inserts (session + first refresh token) run in one `IdentityTransactions.inTransactionWithoutResult(...)`. Tokens are generated **before** and returned **after** the commit. If the commit fails, the client receives an error and no tokens.

### 5.5 How a protected request is authorized

See the sequence diagram in [Shared Architecture §4.5](shared-architecture.md#45-every-protected-request-us-auth-002-onward). In short:

1. `BearerTokenAuthenticationFilter` (Spring Security) reads `Authorization: Bearer …` (skipped on public endpoints).
2. `NimbusJwtDecoder` checks the HS256 signature, then our validators (`typ`, `iss`, expiry with the application clock).
3. `SessionAwareJwtAuthenticationConverter` asks `SessionAuthority` → `SessionAuthorityService` → `SELECT` the session.
4. Usable → principal `AuthenticatedAccount`; otherwise `InvalidBearerTokenException` → entry point → 401.
5. If step 3 cannot reach PostgreSQL (`DataAccessResourceFailureException`, including `CannotGetJdbcConnectionException`, or `TransientDataAccessResourceException`), the converter throws `SessionAuthorityUnavailableException` → entry point → **503**. Any other exception (a programming error) is not caught → 500. In **all** of these cases no authentication is established and the controller never runs (fail closed, [TD-26](shared-architecture.md#14-technical-decision-log)).

> **Concept — why a special exception type, and why it extends `AuthenticationException`.** Spring Security's `BearerTokenAuthenticationFilter` catches `AuthenticationException` and passes it to its failure handler, which calls our entry point — **except** for `AuthenticationServiceException`, which it rethrows as a server error. A plain `DataAccessException` is not an `AuthenticationException` at all, so it would escape the filter as a 500. `SessionAuthorityUnavailableException` extends `AuthenticationException` directly, so it reaches our entry point, which can then choose 503 for it and 401 for everything else. This reuses the existing error path; no extra filter or handler is needed.

Stage 2 has **no production protected endpoint yet** (logout arrives in US-AUTH-004). The tests therefore use a **test-only** probe endpoint defined in test sources ([TD-21](shared-architecture.md#14-technical-decision-log)). Inventing a production endpoint just to demonstrate security is explicitly discouraged by the [Sprint plan](../sprint-plan.md#10-sprint-review-evidence).

### 5.6 Database change

`V3__create_identity_session.sql`:

- `identity_session` — FK to `identity_account`; CHECK `expires_at > established_at AND expires_at ≤ established_at + 720 hours` (the 30-day maximum is enforced by the database too; `720 hours` avoids daylight-saving surprises of `30 days`).
- `identity_refresh_token` — FK to session; `token_hash` unique and must match `^[0-9a-f]{64}$`.

---

## 6. File Implementation Plan

`J` = `backend/src/main/java/com/capsuleai/identity`.

| Order | File Path | Action | Responsibility | Depends On |
| --- | --- | --- | --- | --- |
| 1 | `backend/pom.xml` | MODIFY | + `spring-boot-starter-security-oauth2-resource-server`. | Stage 1 |
| 2 | `backend/src/main/resources/application-local.properties` | MODIFY | Local signing secret from environment. | — |
| 3 | `backend/src/main/resources/db/migration/V3__create_identity_session.sql` | NEW | Session + refresh-token tables. | V2 |
| 4 | `J/domain/SessionPolicy.java` | NEW | Lifetimes. | — |
| 5 | `J/domain/LoginSession.java` | NEW | Session model. | 4 |
| 6 | `J/domain/RefreshTokenHash.java` | NEW | Token hashing. | — |
| 7 | `J/api/AuthenticatedAccount.java` | NEW | Published principal. | — |
| 8 | `J/api/SessionAuthority.java` | NEW | Published authority contract. | 7 |
| 9 | `J/application/LogInCommand.java` | NEW | Input. | — |
| 10 | `J/application/SessionTokens.java` | NEW | Output. | — |
| 11 | `J/application/InvalidCredentialsException.java` | NEW | Rejection. | — |
| 12 | `J/application/ports/AccountRepository.java` | MODIFY | + `findByEmail`. | Stage 1 |
| 13 | `J/application/ports/SessionRepository.java` | NEW | Session port. | 5, 6 |
| 14 | `J/application/ports/IdentityTransactions.java` | NEW | Transaction port. | — |
| 15 | `J/application/ports/AccessTokenIssuer.java` | NEW | Signing port. | — |
| 16 | `J/application/ports/RefreshTokenGenerator.java` | NEW | Randomness port. | — |
| 17 | `J/application/LogInService.java` | NEW | Login use case. | 4–16 |
| 18 | `J/application/SessionAuthorityService.java` | NEW | Authority use case. | 8, 13 |
| 19 | `J/adapter/out/persistence/JdbcTime.java` | NEW | `Instant` ↔ `timestamptz`. | — |
| 20 | `J/adapter/out/persistence/JdbcAccountRepository.java` | MODIFY | + `findByEmail`; uses `JdbcTime`. | 12, 19 |
| 21 | `J/adapter/out/persistence/JdbcSessionRepository.java` | NEW | Session SQL. | 3, 13, 19 |
| 22 | `J/adapter/out/persistence/SpringIdentityTransactions.java` | NEW | `TransactionTemplate`. | 14 |
| 22a | `J/adapter/jwt/JwtClaimNames.java` | NEW | Shared JWT claim names (no in↔out adapter import). | — |
| 23 | `J/adapter/out/security/JwtAccessTokenIssuer.java` | NEW | JWT signing. | 1, 15, 22a |
| 24 | `J/adapter/out/security/SecureRandomRefreshTokenGenerator.java` | NEW | Random tokens. | 16 |
| 25 | `J/adapter/in/security/AccessTokenExpiryValidator.java` | NEW | Exact expiry. | 1 |
| 26 | `J/adapter/in/security/AuthenticatedAccountToken.java` | NEW | Authentication object. | 7 |
| 27 | `J/adapter/in/security/SessionAwareJwtAuthenticationConverter.java` | NEW | JWT → usable session; outage classification. | 8, 22a, 26, 29a |
| 28 | `J/adapter/in/security/ProblemJsonAuthenticationEntryPoint.java` | NEW | 401 body; 503 body for an authority outage. | 1, 29a |
| 29 | `J/adapter/in/security/PublicEndpointAwareBearerTokenResolver.java` | NEW | Public-endpoint token handling. | 1 |
| 29a | `J/adapter/in/security/SessionAuthorityUnavailableException.java` | NEW | "Authority could not be checked" (→ 503). | 1 |
| 30 | `J/configuration/AccessTokenProperties.java` | NEW | Secret binding. | 2 |
| 31 | `J/configuration/IdentityTokenConfiguration.java` | NEW | JWT beans. | 23–25, 30 |
| 32 | `J/configuration/IdentityConfiguration.java` | MODIFY | + login wiring. | 17–22 |
| 33 | `J/configuration/IdentitySecurityConfiguration.java` | NEW | Filter chain. | 27–29, 31 |
| 34 | `J/adapter/in/web/LoginRequest.java` | NEW | Request JSON. | 9 |
| 35 | `J/adapter/in/web/TokenResponse.java` | NEW | Response JSON. | 10 |
| 36 | `J/adapter/in/web/LoginController.java` | NEW | Endpoint. | 17, 34, 35 |
| 37 | `J/adapter/in/web/IdentityApiExceptionHandler.java` | MODIFY | + 401 `INVALID_CREDENTIALS`. | 11 |
| 38 | `docs/05-api/openapi.yaml` | MODIFY | + login, bearer scheme, `TokenResponse`. | 36 |
| — | All other Stage 1 files | REUSE | Unchanged. | — |

---

## 7. Complete Production Code

### 7.1 `backend/pom.xml` — MODIFY

Add this block directly **after** the `spring-security-crypto` dependency from Stage 1. Nothing else changes.

```xml
        <!-- US-AUTH-002: Spring Security web filter chain + JWT (Nimbus) resource-server support -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-oauth2-resource-server</artifactId>
        </dependency>
```

Notes:

- In Spring Boot 4 this is the current starter name; `spring-boot-starter-oauth2-resource-server` still exists but is **deprecated** in 4.1.1.
- It brings Spring Security (web, config), `spring-security-oauth2-jose` and Nimbus JOSE+JWT, all at Boot-managed versions (Security 7.1.1).
- Keep `spring-security-crypto` declared explicitly; Identity uses it directly.
- As soon as Spring Security is on the classpath, **every** endpoint is protected by default. That is why `IdentitySecurityConfiguration` (7.33) must be added in the same step.

### 7.2 `backend/src/main/resources/application-local.properties` — MODIFY

Complete resulting file:

```properties
# Local datasource properties are added in Checkpoint 6.
server.port=8080

spring.datasource.url=${CAPSULE_DB_URL:jdbc:postgresql://localhost:5433/capsuleai_dev}
spring.datasource.username=${CAPSULE_DB_USER:capsuleai_dev}
spring.datasource.password=${CAPSULE_DB_PASSWORD:YOUR_PRIVATE_LOCAL_PASSWORD}

# US-AUTH-002: access-token signing key (Base64, at least 32 random bytes).
# Deliberately no default value: startup fails until CAPSULE_JWT_SIGNING_SECRET is set.
capsuleai.identity.access-token.signing-secret=${CAPSULE_JWT_SIGNING_SECRET}
```

No secret is committed. The test profile gets its own disposable key (Section 9, T1).

### 7.3 `backend/src/main/resources/db/migration/V3__create_identity_session.sql` — NEW

```sql
-- US-AUTH-002: persistent login-session authority (ADA-008) and refresh credentials.
-- One row per successful login. expires_at is fixed at login and never extended (FR-AUTH-010).
CREATE TABLE identity_session (
    id             UUID        PRIMARY KEY,
    account_id     UUID        NOT NULL REFERENCES identity_account (id),
    established_at TIMESTAMPTZ NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_identity_session_lifetime
        CHECK (expires_at > established_at
               AND expires_at <= established_at + INTERVAL '720 hours')
);

CREATE INDEX ix_identity_session_account ON identity_session (account_id);

-- Only the SHA-256 hash of each refresh token is stored, never the token itself.
CREATE TABLE identity_refresh_token (
    id         UUID        PRIMARY KEY,
    session_id UUID        NOT NULL REFERENCES identity_session (id),
    token_hash TEXT        NOT NULL,
    issued_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_identity_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_identity_refresh_token_hash_format CHECK (token_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX ix_identity_refresh_token_session ON identity_refresh_token (session_id);
```

- `720 hours` = exactly 30 × 24 h. Adding `INTERVAL '30 days'` to a `timestamptz` follows calendar days in the session time zone, which can be 719 or 721 hours across a daylight-saving change.
- The unique index on `token_hash` makes the lookup by hash fast and guarantees one row per token.
- Revocation columns are **not** here: they belong to US-AUTH-004 (V4).

### 7.4 `J/domain/SessionPolicy.java` — NEW

```java
package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;

/** Fixed session policy (SRS §3.1.1, FR-AUTH-010). */
public final class SessionPolicy {

    public static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    public static final Duration SESSION_MAXIMUM_LIFETIME = Duration.ofDays(30);

    private SessionPolicy() {
    }

    /** An access token lives 15 minutes, but never longer than its login session. */
    public static Instant accessTokenExpiresAt(Instant issuedAt, Instant sessionExpiresAt) {
        Instant byLifetime = issuedAt.plus(ACCESS_TOKEN_LIFETIME);
        return byLifetime.isBefore(sessionExpiresAt) ? byLifetime : sessionExpiresAt;
    }
}
```

`Duration.ofDays(30)` is exactly 720 hours (a `Duration` day is always 24 h), matching the database CHECK.

### 7.5 `J/domain/LoginSession.java` — NEW

**Purpose.** The persistent login session. A record (immutable). `isUsableAt` uses `isBefore`, so at exactly `expiresAt` the session is **not** usable. US-AUTH-004 adds revocation fields to this record.

```java
package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One login session (the SRS "Refresh Session"), created by a successful login.
 * Its maximum lifetime is fixed when it is established and never extended.
 */
public record LoginSession(UUID id, UUID accountId, Instant establishedAt, Instant expiresAt) {

    public LoginSession {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(establishedAt, "establishedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(establishedAt)) {
            throw new IllegalArgumentException("A session must expire after it is established");
        }
    }

    public static LoginSession establish(UUID id, UUID accountId, Instant establishedAt) {
        return new LoginSession(id, accountId, establishedAt,
                establishedAt.plus(SessionPolicy.SESSION_MAXIMUM_LIFETIME));
    }

    /** Usable strictly before the absolute maximum lifetime ends. */
    public boolean isUsableAt(Instant now) {
        return now.isBefore(expiresAt);
    }

    public boolean belongsTo(UUID candidateAccountId) {
        return accountId.equals(candidateAccountId);
    }
}
```

### 7.6 `J/domain/RefreshTokenHash.java` — NEW

**Purpose.** Refresh tokens are 256-bit random values, so a fast SHA-256 is sufficient (slow hashing protects *guessable* secrets like passwords). If the database leaks, the hashes cannot be used as tokens. `MessageDigest` and `HexFormat` are JDK classes, so this stays in the domain.

```java
package com.capsuleai.identity.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Lower-case hex SHA-256 of a refresh token. Only this value is stored (TD-11).
 */
public record RefreshTokenHash(String value) {

    public RefreshTokenHash {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("A lower-case hex SHA-256 digest is required");
        }
    }

    public static RefreshTokenHash of(String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            throw new IllegalArgumentException("A refresh token is required");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return new RefreshTokenHash(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", impossible);
        }
    }

    @Override
    public String toString() {
        return "RefreshTokenHash[PROTECTED]";
    }
}
```

### 7.7 `J/api/AuthenticatedAccount.java` — NEW

**Purpose.** The only Identity type other modules need: who is calling and through which session. It lives in `api` because the [Implementation View](../../../../04-architecture/views/implementation-view.puml) says other owners may import only a module's published `api`.

```java
package com.capsuleai.identity.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Published Identity contract: the account and login session a protected
 * request was verified against.
 *
 * <p>Other modules obtain the current account only through this type
 * (as {@code @AuthenticationPrincipal AuthenticatedAccount account} in their
 * controllers), never through Identity's private domain or persistence classes.
 */
public record AuthenticatedAccount(UUID accountId, UUID sessionId) {

    public AuthenticatedAccount {
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(sessionId, "sessionId");
    }
}
```

### 7.8 `J/api/SessionAuthority.java` — NEW

```java
package com.capsuleai.identity.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Published Identity contract: "may this account use this login session now?"
 * (FR-AUTH-009, ADA-008).
 *
 * <p>Token signature and expiry are checked before this is asked; this contract
 * adds the persistent session authority that a token alone cannot prove.
 */
public interface SessionAuthority {

    /**
     * @return the authenticated account if the session exists, belongs to the
     *         account and is currently usable; otherwise empty
     */
    Optional<AuthenticatedAccount> findUsableSession(UUID accountId, UUID sessionId);
}
```

### 7.9 `J/application/LogInCommand.java` — NEW

```java
package com.capsuleai.identity.application;

/** Raw login input, exactly as received. toString() masks the password. */
public record LogInCommand(String email, String password) {

    @Override
    public String toString() {
        return "LogInCommand[email=" + email + ", password=[PROTECTED]]";
    }
}
```

### 7.10 `J/application/SessionTokens.java` — NEW

```java
package com.capsuleai.identity.application;

import java.time.Instant;
import java.util.UUID;

/** Credentials issued for a login session. toString() masks both tokens. */
public record SessionTokens(
        UUID accountId,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant sessionExpiresAt) {

    @Override
    public String toString() {
        return "SessionTokens[accountId=" + accountId
                + ", accessToken=[PROTECTED], accessTokenExpiresAt=" + accessTokenExpiresAt
                + ", refreshToken=[PROTECTED], sessionExpiresAt=" + sessionExpiresAt + "]";
    }
}
```

### 7.11 `J/application/InvalidCredentialsException.java` — NEW

```java
package com.capsuleai.identity.application;

/**
 * The email/password pair did not authenticate an account.
 * Deliberately does not say whether the email or the password was wrong.
 */
public final class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
```

### 7.12 `J/application/ports/AccountRepository.java` — MODIFY

Complete resulting file (adds `findByEmail`):

```java
package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import java.util.Optional;

/** Identity-owned account persistence. */
public interface AccountRepository {

    /**
     * Stores the account only if no account already uses its normalized email.
     *
     * <p>Must be atomic under concurrency: of several equivalent registrations
     * running at the same time, exactly one may return {@code true}.
     *
     * @return {@code true} if stored; {@code false} if the email identity is
     *         already registered (nothing was changed)
     */
    boolean addIfEmailAvailable(Account account);

    /** @return the account registered for this normalized email, if any */
    Optional<Account> findByEmail(EmailAddress email);
}
```

### 7.13 `J/application/ports/SessionRepository.java` — NEW

US-AUTH-004 and US-AUTH-003 add methods to this port.

```java
package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import java.util.Optional;
import java.util.UUID;

/** Identity-owned persistence of login sessions and their refresh tokens. */
public interface SessionRepository {

    /**
     * Stores a new session together with the hash of its first refresh token.
     * Call inside {@link IdentityTransactions} so both rows commit together.
     */
    void create(LoginSession session, RefreshTokenHash firstRefreshToken);

    Optional<LoginSession> findById(UUID sessionId);
}
```

### 7.14 `J/application/ports/IdentityTransactions.java` — NEW

**Purpose.** A use case says "these steps belong together" without importing Spring. The adapter (7.22) implements it with Spring's `TransactionTemplate`; tests implement it with a fake that simply runs the work.

```java
package com.capsuleai.identity.application.ports;

import java.util.function.Supplier;

/**
 * Runs work in one database transaction: everything commits together, or
 * nothing does if the work throws. Lets use cases own their transaction
 * boundary without depending on Spring (ADA-015).
 */
public interface IdentityTransactions {

    <T> T inTransaction(Supplier<T> work);

    default void inTransactionWithoutResult(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }
}
```

### 7.15 `J/application/ports/AccessTokenIssuer.java` — NEW

```java
package com.capsuleai.identity.application.ports;

import java.time.Instant;
import java.util.UUID;

/** Creates signed access tokens. */
public interface AccessTokenIssuer {

    /** @return a signed token naming the account and session, valid until {@code expiresAt} */
    String issue(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt);
}
```

### 7.16 `J/application/ports/RefreshTokenGenerator.java` — NEW

```java
package com.capsuleai.identity.application.ports;

/** Creates new, unguessable refresh-token values. */
public interface RefreshTokenGenerator {

    String newToken();
}
```

### 7.17 `J/application/LogInService.java` — NEW

**Purpose.** The login use case.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.InvalidValueException;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.SessionPolicy;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-AUTH-002 — verifies email and exact password, then establishes a new,
 * separate login session and issues its credentials.
 */
public final class LogInService {

    private final AccountRepository accounts;
    private final PasswordHasher passwordHasher;
    private final SessionRepository sessions;
    private final RefreshTokenGenerator refreshTokens;
    private final AccessTokenIssuer accessTokens;
    private final IdentityTransactions transactions;
    private final Clock clock;

    public LogInService(AccountRepository accounts,
                        PasswordHasher passwordHasher,
                        SessionRepository sessions,
                        RefreshTokenGenerator refreshTokens,
                        AccessTokenIssuer accessTokens,
                        IdentityTransactions transactions,
                        Clock clock) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.refreshTokens = Objects.requireNonNull(refreshTokens, "refreshTokens");
        this.accessTokens = Objects.requireNonNull(accessTokens, "accessTokens");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws RequestValidationException  if the email or the password is missing
     * @throws InvalidCredentialsException if the pair does not authenticate an account
     */
    public SessionTokens logIn(LogInCommand command) {
        requireCredentials(command);

        Account account = findAccount(command.email())
                .filter(candidate -> passwordHasher.matches(command.password(), candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        LoginSession session = LoginSession.establish(UUID.randomUUID(), account.id(), now);
        String refreshToken = refreshTokens.newToken();
        Instant accessTokenExpiresAt = SessionPolicy.accessTokenExpiresAt(now, session.expiresAt());
        String accessToken = accessTokens.issue(account.id(), session.id(), now, accessTokenExpiresAt);

        // Both rows commit together; tokens leave this method only after the commit.
        transactions.inTransactionWithoutResult(
                () -> sessions.create(session, RefreshTokenHash.of(refreshToken)));

        return new SessionTokens(account.id(), accessToken, accessTokenExpiresAt, refreshToken, session.expiresAt());
    }

    private static void requireCredentials(LogInCommand command) {
        List<FieldViolation> violations = new ArrayList<>();
        if (command.email() == null || command.email().isBlank()) {
            violations.add(new FieldViolation("email", ValueProblem.REQUIRED));
        }
        if (command.password() == null || command.password().isEmpty()) {
            violations.add(new FieldViolation("password", ValueProblem.REQUIRED));
        }
        if (!violations.isEmpty()) {
            throw new RequestValidationException(violations);
        }
    }

    /** A malformed email cannot identify any account, so it is treated like an unknown one. */
    private Optional<Account> findAccount(String rawEmail) {
        try {
            return accounts.findByEmail(EmailAddress.normalize(rawEmail));
        } catch (InvalidValueException malformedEmail) {
            return Optional.empty();
        }
    }
}
```

Design points:

- The password is **not** checked against the 12–128 policy here. Policy belongs to creating passwords; at login a wrong value simply does not match. The emptiness check only separates "missing" (400) from "wrong" (401).
- `Optional.filter(...)` keeps "unknown email" and "wrong password" on **the same path**, so both raise the same exception and produce byte-identical responses.
- Account enumeration through timing (unknown emails skip the slow hash) is not equalized, because registration intentionally reveals existence ([A-09](shared-architecture.md#15-ambiguities-limitations-and-open-questions)).

### 7.18 `J/application/SessionAuthorityService.java` — NEW

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import com.capsuleai.identity.application.ports.SessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Answers the published {@link SessionAuthority} question from persistent session state. */
public final class SessionAuthorityService implements SessionAuthority {

    private final SessionRepository sessions;
    private final Clock clock;

    public SessionAuthorityService(SessionRepository sessions, Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public Optional<AuthenticatedAccount> findUsableSession(UUID accountId, UUID sessionId) {
        Instant now = clock.instant();
        return sessions.findById(sessionId)
                .filter(session -> session.belongsTo(accountId))
                .filter(session -> session.isUsableAt(now))
                .map(session -> new AuthenticatedAccount(session.accountId(), session.id()));
    }
}
```

The rule "usable" lives in `LoginSession.isUsableAt`. When US-AUTH-004 adds revocation to that method, this class needs **no change** — that is the payoff of keeping the rule in the domain.

### 7.19 `J/adapter/out/persistence/JdbcTime.java` — NEW

```java
package com.capsuleai.identity.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Converts between Java {@link Instant} and PostgreSQL {@code timestamptz}. */
final class JdbcTime {

    private JdbcTime() {
    }

    static OffsetDateTime toDatabase(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    static Instant fromDatabase(ResultSet row, String column) throws SQLException {
        OffsetDateTime value = row.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}
```

### 7.20 `J/adapter/out/persistence/JdbcAccountRepository.java` — MODIFY

Complete resulting file. `addIfEmailAvailable` is unchanged in behaviour (it now uses `JdbcTime`); `findByEmail` is new.

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.DisplayName;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.PasswordHash;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link AccountRepository}. */
public final class JdbcAccountRepository implements AccountRepository {

    private final JdbcClient jdbc;

    public JdbcAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addIfEmailAvailable(Account account) {
        int inserted = jdbc.sql("""
                        INSERT INTO identity_account (id, email, password_hash, display_name, created_at)
                        VALUES (:id, :email, :passwordHash, :displayName, :createdAt)
                        ON CONFLICT (email) DO NOTHING
                        """)
                .param("id", account.id())
                .param("email", account.email().value())
                .param("passwordHash", account.passwordHash().value())
                .param("displayName", account.displayName().map(DisplayName::value).orElse(null), Types.VARCHAR)
                .param("createdAt", JdbcTime.toDatabase(account.createdAt()))
                .update();
        return inserted == 1;
    }

    @Override
    public Optional<Account> findByEmail(EmailAddress email) {
        return jdbc.sql("""
                        SELECT id, email, password_hash, display_name, created_at
                        FROM identity_account
                        WHERE email = :email
                        """)
                .param("email", email.value())
                .query(JdbcAccountRepository::mapAccount)
                .optional();
    }

    private static Account mapAccount(ResultSet row, int rowNumber) throws SQLException {
        return new Account(
                row.getObject("id", UUID.class),
                new EmailAddress(row.getString("email")),
                new PasswordHash(row.getString("password_hash")),
                Optional.ofNullable(row.getString("display_name")).map(DisplayName::new),
                JdbcTime.fromDatabase(row, "created_at"));
    }
}
```

`WHERE email = :email` uses the unique index, and because the stored value is already normalized, a plain equality is the case-insensitive comparison BRULE-AUTH-002 needs.

### 7.21 `J/adapter/out/persistence/JdbcSessionRepository.java` — NEW

US-AUTH-004 and US-AUTH-003 extend this class; their guides print the complete new version.

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link SessionRepository}. */
public final class JdbcSessionRepository implements SessionRepository {

    private final JdbcClient jdbc;

    public JdbcSessionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
        jdbc.sql("""
                        INSERT INTO identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", session.id())
                .param("accountId", session.accountId())
                .param("establishedAt", JdbcTime.toDatabase(session.establishedAt()))
                .param("expiresAt", JdbcTime.toDatabase(session.expiresAt()))
                .update();
        insertRefreshToken(session.id(), firstRefreshToken, session.establishedAt());
    }

    @Override
    public Optional<LoginSession> findById(UUID sessionId) {
        return jdbc.sql("""
                        SELECT id, account_id, established_at, expires_at
                        FROM identity_session
                        WHERE id = :id
                        """)
                .param("id", sessionId)
                .query(JdbcSessionRepository::mapSession)
                .optional();
    }

    private void insertRefreshToken(UUID sessionId, RefreshTokenHash tokenHash, Instant issuedAt) {
        jdbc.sql("""
                        INSERT INTO identity_refresh_token (id, session_id, token_hash, issued_at)
                        VALUES (:id, :sessionId, :tokenHash, :issuedAt)
                        """)
                .param("id", UUID.randomUUID())
                .param("sessionId", sessionId)
                .param("tokenHash", tokenHash.value())
                .param("issuedAt", JdbcTime.toDatabase(issuedAt))
                .update();
    }

    private static LoginSession mapSession(ResultSet row, int rowNumber) throws SQLException {
        return new LoginSession(
                row.getObject("id", UUID.class),
                row.getObject("account_id", UUID.class),
                JdbcTime.fromDatabase(row, "established_at"),
                JdbcTime.fromDatabase(row, "expires_at"));
    }
}
```

> **Concept — how `JdbcClient` joins a transaction.** `JdbcClient` uses the Boot-configured `DataSource` through Spring's `DataSourceUtils`. Inside `TransactionTemplate.execute(...)`, Spring binds one connection to the current thread, and both inserts above use **that** connection; the commit happens when the callback returns. Outside a transaction, each statement autocommits on its own.

### 7.22 `J/adapter/out/persistence/SpringIdentityTransactions.java` — NEW

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.IdentityTransactions;
import java.util.function.Supplier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link IdentityTransactions} backed by Spring's {@link TransactionTemplate}.
 * Uses PostgreSQL's default READ COMMITTED isolation. A RuntimeException thrown
 * by the work rolls everything back.
 */
public final class SpringIdentityTransactions implements IdentityTransactions {

    private final TransactionTemplate transactionTemplate;

    public SpringIdentityTransactions(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
```

### 7.22a `J/adapter/jwt/JwtClaimNames.java` — NEW

**Purpose.** The token **issuer** (outbound adapter, 7.23) writes the session id into the JWT, and the token **reader** (inbound adapter, 7.27) reads it back. Both must use the same claim name. If the reader imported the issuer class just to reuse its constant, an inbound adapter would depend on an outbound adapter — two independent edges of the hexagon would be glued together, and changing token *signing* could break token *reading* at compile time. This tiny, framework-free class is the shared definition both depend on instead ([TD-25](shared-architecture.md#14-technical-decision-log)).

Why this package: it is a technical token-format detail, so it does not belong in `domain` (business rules) or in `api` (other modules receive `AuthenticatedAccount`, never raw claims). `adapter.jwt` sits beside `adapter.in` and `adapter.out` and contains only this constant holder.

```java
package com.capsuleai.identity.adapter.jwt;

/**
 * Names of Identity's custom access-token claims: the shared wire format of
 * the token issuer (adapter.out.security) and the token reader
 * (adapter.in.security). Neither adapter imports the other; both use this.
 *
 * <p>Internal to Identity and deliberately not part of the published
 * {@code api}: other modules receive {@code AuthenticatedAccount}, never JWT claims.
 */
public final class JwtClaimNames {

    /** Claim carrying the login-session id. */
    public static final String SESSION_ID = "sid";

    private JwtClaimNames() {
    }
}
```

### 7.23 `J/adapter/out/security/JwtAccessTokenIssuer.java` — NEW

```java
package com.capsuleai.identity.adapter.out.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/** Signs HS256 access tokens naming the account ({@code sub}) and login session ({@code sid}). */
public final class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder encoder;
    private final String issuer;

    public JwtAccessTokenIssuer(JwtEncoder encoder, String issuer) {
        this.encoder = Objects.requireNonNull(encoder, "encoder");
        this.issuer = Objects.requireNonNull(issuer, "issuer");
    }

    @Override
    public String issue(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(accountId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtClaimNames.SESSION_ID, sessionId.toString())
                .build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
```

The token holds **no** email or name (purpose limitation). Anyone can Base64-decode a JWT payload, so it must never contain anything secret.

### 7.24 `J/adapter/out/security/SecureRandomRefreshTokenGenerator.java` — NEW

```java
package com.capsuleai.identity.adapter.out.security;

import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;

/** 256-bit random refresh tokens, Base64url-encoded without padding (43 characters). */
public final class SecureRandomRefreshTokenGenerator implements RefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
```

### 7.25 `J/adapter/in/security/AccessTokenExpiryValidator.java` — NEW

**Purpose.** Spring's default `JwtTimestampValidator` accepts tokens up to **60 seconds** after `exp` (clock skew) and accepts a token at exactly `exp`. The SRS says 15 minutes, and RFC 7519 says a JWT must not be accepted "on or after" `exp`. This validator enforces exactly that, using the injected application `Clock` so tests can move time.

```java
package com.capsuleai.identity.adapter.in.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Rejects an access token at and after its {@code exp} instant, with zero clock
 * skew, using the application's {@link Clock} (FR-AUTH-010, TD-09).
 */
public final class AccessTokenExpiryValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error EXPIRED =
            new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "The access token has expired", null);

    private final Clock clock;

    public AccessTokenExpiryValidator(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Instant expiresAt = token.getExpiresAt();
        if (expiresAt == null || !clock.instant().isBefore(expiresAt)) {
            return OAuth2TokenValidatorResult.failure(EXPIRED);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
```

### 7.26 `J/adapter/in/security/AuthenticatedAccountToken.java` — NEW

```java
package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.api.AuthenticatedAccount;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Spring Security authentication whose principal is the published
 * {@link AuthenticatedAccount}. The bearer token itself is not retained.
 */
public final class AuthenticatedAccountToken extends AbstractAuthenticationToken {

    private final AuthenticatedAccount account;

    public AuthenticatedAccountToken(AuthenticatedAccount account) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.account = account;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public AuthenticatedAccount getPrincipal() {
        return account;
    }

    @Override
    public String getName() {
        return account.accountId().toString();
    }
}
```

No roles/authorities exist in the requirements, so the token carries none. Authorization in CapsuleAI is **ownership** ("only your own data"), which each owning module enforces using `accountId`.

### 7.27 `J/adapter/in/security/SessionAwareJwtAuthenticationConverter.java` — NEW

**Purpose.** The heart of ADA-008. Spring Security calls this **after** the signature and our validators have accepted the JWT. It asks the published `SessionAuthority` whether the session is usable. Throwing `InvalidBearerTokenException` (an `AuthenticationException`) makes the filter call the entry point → 401. If the question cannot be answered because PostgreSQL is unreachable, it throws `SessionAuthorityUnavailableException` instead → 503 (7.29a, TD-26).

It reads the claim name from `JwtClaimNames` (7.22a), **not** from `JwtAccessTokenIssuer`: an inbound adapter must not import an outbound adapter.

```java
package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Turns an already signature- and expiry-checked JWT into an authenticated
 * account, but ONLY if persistent session authority says the session is usable
 * now (FR-AUTH-009, ADA-008). A valid signature alone is never enough.
 *
 * <p>Fail closed in every outcome other than "usable": an unusable session is an
 * invalid token (401); an unreachable database is an authority outage (503); any
 * other exception propagates (500).
 */
public final class SessionAwareJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(SessionAwareJwtAuthenticationConverter.class);

    private final SessionAuthority sessionAuthority;

    public SessionAwareJwtAuthenticationConverter(SessionAuthority sessionAuthority) {
        this.sessionAuthority = Objects.requireNonNull(sessionAuthority, "sessionAuthority");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID accountId = requiredUuid(jwt.getSubject());
        UUID sessionId = requiredUuid(jwt.getClaimAsString(JwtClaimNames.SESSION_ID));
        AuthenticatedAccount account = findUsableSession(accountId, sessionId)
                .orElseThrow(() -> new InvalidBearerTokenException("The login session is no longer usable"));
        return new AuthenticatedAccountToken(account);
    }

    /**
     * Only a confirmed "database cannot be reached" failure is translated. Any
     * other exception (for example invalid SQL) is a programming error and is
     * left to propagate as a 500. In both cases no authentication is created.
     */
    private Optional<AuthenticatedAccount> findUsableSession(UUID accountId, UUID sessionId) {
        try {
            return sessionAuthority.findUsableSession(accountId, sessionId);
        } catch (DataAccessResourceFailureException | TransientDataAccessResourceException unavailable) {
            log.error("Session authority check failed because the database is unreachable", unavailable);
            throw new SessionAuthorityUnavailableException(unavailable);
        }
    }

    private static UUID requiredUuid(String claimValue) {
        if (claimValue == null) {
            throw new InvalidBearerTokenException("The access token is missing a required claim");
        }
        try {
            return UUID.fromString(claimValue);
        } catch (IllegalArgumentException malformed) {
            throw new InvalidBearerTokenException("The access token has a malformed claim");
        }
    }
}
```

**What happens when PostgreSQL is down.** `SessionAuthorityService` → `JdbcSessionRepository.findById` → `JdbcClient` asks for a connection; HikariCP cannot supply one, so Spring's `DataSourceUtils` throws `CannotGetJdbcConnectionException` (a `DataAccessResourceFailureException`). The converter catches exactly that family, logs it server-side, and throws `SessionAuthorityUnavailableException`. The request is **not** authenticated and never reaches a controller; the client receives a fixed 503 body. There is no fallback, cache or "assume valid" branch: a session that cannot be checked is never treated as usable ([TD-26](shared-architecture.md#14-technical-decision-log); this replaces the earlier "500" behaviour recorded as A-10).

Why catch here and not inside `SessionAuthorityService`: classifying Spring DAO exceptions is a technical concern of the security adapter. The application layer stays Spring-free (ArchUnit rule), and `SessionAuthority` keeps its simple "usable or not" contract.

### 7.28 `J/adapter/in/security/ProblemJsonAuthenticationEntryPoint.java` — NEW

```java
package com.capsuleai.identity.adapter.in.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Answers authentication failures with a problem body:
 * <ul>
 *   <li>credentials not acceptable → 401, the standard {@code WWW-Authenticate:
 *       Bearer} header and "authenticate again" — never an "empty wardrobe"
 *       (BRULE-AUTH-001);</li>
 *   <li>session authority could not be checked → 503 "temporarily unavailable",
 *       without a challenge header, because the credentials were not found invalid.</li>
 * </ul>
 * Both bodies are fixed strings, so no exception text or request data can leak.
 */
public final class ProblemJsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String AUTHENTICATION_REQUIRED_BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,\
            "detail":"Authentication is required. Log in again to continue.",\
            "code":"AUTHENTICATION_REQUIRED"}""";

    private static final String TEMPORARILY_UNAVAILABLE_BODY = """
            {"type":"about:blank","title":"Service Unavailable","status":503,\
            "detail":"Your access could not be verified right now. Nothing was changed. Try again shortly.",\
            "code":"TEMPORARILY_UNAVAILABLE"}""";

    private final AuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof SessionAuthorityUnavailableException) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            writeProblemBody(response, TEMPORARILY_UNAVAILABLE_BODY);
            return;
        }
        bearerEntryPoint.commence(request, response, authException);   // 401 + WWW-Authenticate
        writeProblemBody(response, AUTHENTICATION_REQUIRED_BODY);
    }

    private static void writeProblemBody(HttpServletResponse response, String body) throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
    }
}
```

The bodies are fixed strings (they contain no request data), so they are written directly without a JSON library. The `\` at a line end inside a text block joins lines without inserting a newline. The 503 body reuses the existing `TEMPORARILY_UNAVAILABLE` code, so clients handle it the same way as a 503 from a controller.

### 7.29 `J/adapter/in/security/PublicEndpointAwareBearerTokenResolver.java` — NEW

**Purpose.** Spring's resource server tries to authenticate **any** request that carries `Authorization: Bearer …`, even on `permitAll()` routes; an expired token would turn a login attempt into a 401. Mobile HTTP clients often attach the last token to every call, so this resolver ignores the header on public endpoints (TD-17).

```java
package com.capsuleai.identity.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Ignores the Authorization header on public endpoints, so a stale token that a
 * client attaches to every request cannot block public operations such as login.
 */
public final class PublicEndpointAwareBearerTokenResolver implements BearerTokenResolver {

    private final RequestMatcher publicEndpoints;
    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    public PublicEndpointAwareBearerTokenResolver(RequestMatcher publicEndpoints) {
        this.publicEndpoints = Objects.requireNonNull(publicEndpoints, "publicEndpoints");
    }

    @Override
    public String resolve(HttpServletRequest request) {
        return publicEndpoints.matches(request) ? null : delegate.resolve(request);
    }
}
```

### 7.29a `J/adapter/in/security/SessionAuthorityUnavailableException.java` — NEW

**Purpose.** Carries "the session could not be checked" from the converter (7.27) to the entry point (7.28). It must extend `AuthenticationException` so Spring Security delivers it to the entry point, and must **not** extend `AuthenticationServiceException`, which Spring Security rethrows as a 500 instead. The message is generic; the original database exception is kept only as the `cause` for server-side logs.

```java
package com.capsuleai.identity.adapter.in.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Persistent session authority could not be checked because the database is
 * unreachable. Authentication fails (fail closed); the entry point answers 503
 * rather than 401, because the client's credentials were not found invalid.
 *
 * <p>Deliberately not an {@code AuthenticationServiceException}: Spring
 * Security rethrows that type as a server error instead of calling the entry point.
 */
public final class SessionAuthorityUnavailableException extends AuthenticationException {

    public SessionAuthorityUnavailableException(Throwable cause) {
        super("Session authority is temporarily unavailable", cause);
    }
}
```

### 7.30 `J/configuration/AccessTokenProperties.java` — NEW

**Purpose.** Binds `capsuleai.identity.access-token.signing-secret` and **fails at startup** if it is missing, not Base64, or shorter than 32 bytes (HS256 needs a 256-bit key). Error messages never contain the secret. `toString()` is masked.

```java
package com.capsuleai.identity.configuration;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Access-token signing configuration ({@code capsuleai.identity.access-token.*}).
 * The secret is Base64 and must decode to at least 32 bytes (HS256).
 */
@ConfigurationProperties("capsuleai.identity.access-token")
public record AccessTokenProperties(String signingSecret) {

    static final int MINIMUM_SECRET_BYTES = 32;

    public AccessTokenProperties {
        if (signingSecret == null || signingSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.access-token.signing-secret is not configured");
        }
        if (decode(signingSecret).length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.access-token.signing-secret must decode to at least 32 bytes");
        }
    }

    public SecretKey signingKey() {
        return new SecretKeySpec(decode(signingSecret), "HmacSHA256");
    }

    private static byte[] decode(String base64) {
        try {
            return Base64.getDecoder().decode(base64.strip());
        } catch (IllegalArgumentException notBase64) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.access-token.signing-secret must be Base64-encoded");
        }
    }

    @Override
    public String toString() {
        return "AccessTokenProperties[signingSecret=PROTECTED]";
    }
}
```

> **Concept — configuration records.** Spring Boot binds properties to a record through its constructor ("constructor binding"). Validation in the compact constructor runs during binding, so a bad value stops the application before it accepts requests.

### 7.31 `J/configuration/IdentityTokenConfiguration.java` — NEW

**Purpose.** Creates the JWT encoder/decoder from the same secret. `setJwtValidator` **replaces** Spring's default validators, so the list re-adds the type check and adds the issuer check and our exact expiry validator. The decoder uses the application `Clock` bean — in tests that is the `MutableClock`.

```java
package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.in.security.AccessTokenExpiryValidator;
import com.capsuleai.identity.adapter.out.security.JwtAccessTokenIssuer;
import com.capsuleai.identity.adapter.out.security.SecureRandomRefreshTokenGenerator;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** Access-token signing and verification beans. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AccessTokenProperties.class)
public class IdentityTokenConfiguration {

    static final String ACCESS_TOKEN_ISSUER = "capsuleai-identity";

    @Bean
    JwtEncoder accessTokenEncoder(AccessTokenProperties properties) {
        return NimbusJwtEncoder.withSecretKey(properties.signingKey())
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtDecoder accessTokenDecoder(AccessTokenProperties properties, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(properties.signingKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.<OAuth2TokenValidator<Jwt>>of(
                JwtTypeValidator.jwt(),
                new JwtIssuerValidator(ACCESS_TOKEN_ISSUER),
                new AccessTokenExpiryValidator(clock))));
        return decoder;
    }

    @Bean
    AccessTokenIssuer accessTokenIssuer(JwtEncoder accessTokenEncoder) {
        return new JwtAccessTokenIssuer(accessTokenEncoder, ACCESS_TOKEN_ISSUER);
    }

    @Bean
    RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRandomRefreshTokenGenerator();
    }
}
```

Because a `JwtDecoder` bean now exists, Spring Boot does **not** create its default in-memory user (no "Using generated security password" log line).

### 7.32 `J/configuration/IdentityConfiguration.java` — MODIFY

Complete resulting file (Stage 2):

```java
package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.out.persistence.JdbcAccountRepository;
import com.capsuleai.identity.adapter.out.persistence.JdbcSessionRepository;
import com.capsuleai.identity.adapter.out.persistence.SpringIdentityTransactions;
import com.capsuleai.identity.adapter.out.security.Pbkdf2PasswordHasher;
import com.capsuleai.identity.application.LogInService;
import com.capsuleai.identity.application.RegisterAccountService;
import com.capsuleai.identity.application.SessionAuthorityService;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.PasswordHasher;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import com.capsuleai.identity.application.ports.SessionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

/** Wires the Identity module's private implementations. */
@Configuration(proxyBeanMethods = false)
public class IdentityConfiguration {

    @Bean
    Clock identityClock() {
        return Clock.systemUTC();
    }

    @Bean
    PasswordHasher passwordHasher() {
        return new Pbkdf2PasswordHasher();
    }

    @Bean
    AccountRepository accountRepository(JdbcClient jdbcClient) {
        return new JdbcAccountRepository(jdbcClient);
    }

    @Bean
    SessionRepository sessionRepository(JdbcClient jdbcClient) {
        return new JdbcSessionRepository(jdbcClient);
    }

    @Bean
    IdentityTransactions identityTransactions(PlatformTransactionManager transactionManager) {
        return new SpringIdentityTransactions(transactionManager);
    }

    @Bean
    RegisterAccountService registerAccountService(
            AccountRepository accounts, PasswordHasher passwordHasher, Clock clock) {
        return new RegisterAccountService(accounts, passwordHasher, clock);
    }

    @Bean
    LogInService logInService(AccountRepository accounts,
                              PasswordHasher passwordHasher,
                              SessionRepository sessions,
                              RefreshTokenGenerator refreshTokens,
                              AccessTokenIssuer accessTokens,
                              IdentityTransactions transactions,
                              Clock clock) {
        return new LogInService(accounts, passwordHasher, sessions, refreshTokens, accessTokens, transactions, clock);
    }

    @Bean
    SessionAuthorityService sessionAuthorityService(SessionRepository sessions, Clock clock) {
        return new SessionAuthorityService(sessions, clock);
    }
}
```

`SessionAuthorityService` implements the `SessionAuthority` interface, so the security configuration can inject it by the interface type.

### 7.33 `J/configuration/IdentitySecurityConfiguration.java` — NEW

**Purpose.** The single application-wide filter chain (TD-19). Read it top to bottom as a policy:

1. No CSRF, HTTP Basic, form login or default logout filter — this is a token API without cookies.
2. Stateless — no server HTTP session is ever created.
3. Error dispatches are allowed (so real 404/500 pages render), public endpoints are allowed, **everything else needs authentication**.
4. Authentication = JWT bearer token, verified by our decoder and converted only for a usable session.

`@ConditionalOnWebApplication` keeps this configuration out of non-web contexts such as the baseline `DatabaseSmokeIT` (`webEnvironment = NONE`), where Spring Security's `HttpSecurity` builder does not exist.

```java
package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.in.security.ProblemJsonAuthenticationEntryPoint;
import com.capsuleai.identity.adapter.in.security.PublicEndpointAwareBearerTokenResolver;
import com.capsuleai.identity.adapter.in.security.SessionAwareJwtAuthenticationConverter;
import com.capsuleai.identity.api.SessionAuthority;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * The application-wide security policy. Identity owns authentication, so it
 * owns this single filter chain: public auth endpoints, everything else needs a
 * usable login session (BRULE-AUTH-001, FR-AUTH-009).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class IdentitySecurityConfiguration {

    /** Operations anyone may call. Every other request needs a usable login session. */
    static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/registrations"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/login"));

    @Bean
    SecurityFilterChain identitySecurityFilterChain(HttpSecurity http,
                                                    JwtDecoder accessTokenDecoder,
                                                    SessionAuthority sessionAuthority) throws Exception {
        AuthenticationEntryPoint entryPoint = new ProblemJsonAuthenticationEntryPoint();
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(new PublicEndpointAwareBearerTokenResolver(PUBLIC_ENDPOINTS))
                        .authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> jwt
                                .decoder(accessTokenDecoder)
                                .jwtAuthenticationConverter(
                                        new SessionAwareJwtAuthenticationConverter(sessionAuthority))));
        return http.build();
    }
}
```

> **Why CSRF can be disabled here.** CSRF attacks abuse credentials that browsers attach automatically (cookies). This API never uses cookies; a bearer token is attached only by code that already holds it, so there is nothing for a forged cross-site request to borrow.

### 7.34 `J/adapter/in/web/LoginRequest.java` — NEW

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.LogInCommand;

/** JSON body of POST /api/v1/auth/login. toString() masks the password. */
public record LoginRequest(String email, String password) {

    LogInCommand toCommand() {
        return new LogInCommand(email, password);
    }

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=[PROTECTED]]";
    }
}
```

### 7.35 `J/adapter/in/web/TokenResponse.java` — NEW

Timestamps are ISO-8601 strings produced by `Instant.toString()` (for example `2026-01-05T08:15:00Z`), so the wire format does not depend on Jackson date settings. US-AUTH-003 reuses this class unchanged.

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.SessionTokens;
import java.util.UUID;

/** JSON body returned by login (and later by renewal). toString() masks the tokens. */
public record TokenResponse(
        String tokenType,
        String accessToken,
        String accessTokenExpiresAt,
        String refreshToken,
        String sessionExpiresAt,
        UUID accountId) {

    static TokenResponse from(SessionTokens tokens) {
        return new TokenResponse(
                "Bearer",
                tokens.accessToken(),
                tokens.accessTokenExpiresAt().toString(),
                tokens.refreshToken(),
                tokens.sessionExpiresAt().toString(),
                tokens.accountId());
    }

    @Override
    public String toString() {
        return "TokenResponse[tokenType=" + tokenType + ", accessToken=[PROTECTED], accessTokenExpiresAt="
                + accessTokenExpiresAt + ", refreshToken=[PROTECTED], sessionExpiresAt=" + sessionExpiresAt
                + ", accountId=" + accountId + "]";
    }
}
```

`accountId` lets the mobile client detect an account switch and clear the previous user's cached screens (AC-06, external evidence).

### 7.36 `J/adapter/in/web/LoginController.java` — NEW

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.LogInService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-002 — public credential login endpoint. */
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LogInService logInService;

    public LoginController(LogInService logInService) {
        this.logInService = logInService;
    }

    @PostMapping("/login")
    public TokenResponse logIn(@RequestBody LoginRequest request) {
        return TokenResponse.from(logInService.logIn(request.toCommand()));
    }
}
```

### 7.37 `J/adapter/in/web/IdentityApiExceptionHandler.java` — MODIFY

Complete resulting file (adds `handleInvalidCredentials`):

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.EmailAlreadyRegisteredException;
import com.capsuleai.identity.application.FieldViolation;
import com.capsuleai.identity.application.InvalidCredentialsException;
import com.capsuleai.identity.application.RequestValidationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates Identity outcomes into RFC 9457 problem responses.
 *
 * <p>Every body carries a stable machine-readable {@code code}; the mobile
 * client maps codes to Vietnamese guidance. No body echoes submitted values.
 */
@RestControllerAdvice(basePackageClasses = IdentityApiExceptionHandler.class)
public class IdentityApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(IdentityApiExceptionHandler.class);

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(RequestValidationException exception) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Some submitted values are missing or invalid. Correct them and submit again.");
        problem.setProperty("fieldErrors", fieldErrors(exception.violations()));
        return respond(problem);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return respond(problem(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                "An account already exists for this email. Log in, or use Forgot Password to recover access."));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception) {
        return respond(problem(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "The email or password is incorrect. Check them and try again, or use Forgot Password."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "The request body is missing or is not valid JSON."));
    }

    /**
     * Only an unreachable database becomes 503. Other data-access errors (for
     * example invalid SQL) are programming errors and stay 500.
     */
    @ExceptionHandler({DataAccessResourceFailureException.class, TransientDataAccessResourceException.class})
    public ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
        log.error("Identity persistence operation failed", exception);
        return respond(problem(HttpStatus.SERVICE_UNAVAILABLE, "TEMPORARILY_UNAVAILABLE",
                "The request could not be completed and its outcome is not confirmed. Check the result or retry."));
    }

    private static List<Map<String, String>> fieldErrors(List<FieldViolation> violations) {
        return violations.stream()
                .map(violation -> {
                    Map<String, String> error = new LinkedHashMap<>();
                    error.put("field", violation.field());
                    error.put("code", violation.problem().name());
                    return error;
                })
                .toList();
    }

    private static ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        return problem;
    }

    private static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
```

### 7.38 `docs/05-api/openapi.yaml` — MODIFY

Complete resulting file for Stage 2 (adds the bearer scheme, the default security requirement, the login path and `TokenResponse`; registration is marked public with `security: []`).

```yaml
openapi: 3.1.0
info:
  title: CapsuleAI Backend API
  version: 0.2.0
  description: |
    Living HTTP contract (Scrum Workflow Section 27).
    Sprint 1 adds the Identity authentication endpoints.
    Error bodies are RFC 9457 problem details with a stable `code` property;
    clients map each code to Vietnamese guidance.
    Every operation requires a bearer access token unless it declares `security: []`.
servers:
  - url: http://127.0.0.1:8080
    description: Local development
security:
  - bearerAuth: []
paths:
  /api/v1/auth/registrations:
    post:
      tags: [Identity]
      operationId: registerAccount
      summary: Register a private account (US-AUTH-001)
      description: |
        Creates exactly one account per normalized email (surrounding whitespace
        removed, compared case-insensitively). Does not log the user in; the
        client calls the login operation next.
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegistrationRequest'
      responses:
        '201':
          description: Account created.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RegistrationResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '409':
          description: EMAIL_ALREADY_REGISTERED. Offer Log in and Forgot Password. The existing account is unchanged.
          content:
            application/problem+json:
              schema:
                $ref: '#/components/schemas/Problem'
        '503':
          $ref: '#/components/responses/Unavailable'
  /api/v1/auth/login:
    post:
      tags: [Identity]
      operationId: logIn
      summary: Log in with email and password (US-AUTH-002)
      description: |
        Verifies the normalized email and the exact password, creates a new
        separate login session (maximum 30 days) and returns a 15-minute access
        token plus a refresh token. An Authorization header, if sent, is ignored.
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: Logged in.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TokenResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          description: INVALID_CREDENTIALS. Same response for an unknown email and a wrong password. No session created.
          content:
            application/problem+json:
              schema:
                $ref: '#/components/schemas/Problem'
        '503':
          $ref: '#/components/responses/Unavailable'
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: Access token from login. Valid for 15 minutes and only while its login session is usable.
  schemas:
    RegistrationRequest:
      type: object
      required: [email, password, confirmPassword]
      properties:
        email:
          type: string
          maxLength: 254
          example: person@example.com
        password:
          type: string
          description: 12 to 128 Unicode code points, used exactly as entered.
        confirmPassword:
          type: string
          description: Must equal password exactly.
        displayName:
          type: [string, 'null']
          description: Optional. Stored exactly. At most 100 code points.
    RegistrationResponse:
      type: object
      required: [accountId, email, displayName]
      properties:
        accountId:
          type: string
          format: uuid
        email:
          type: string
          description: Normalized email identity.
        displayName:
          type: [string, 'null']
    LoginRequest:
      type: object
      required: [email, password]
      properties:
        email:
          type: string
        password:
          type: string
          description: Compared exactly; never trimmed or normalized.
    TokenResponse:
      type: object
      required: [tokenType, accessToken, accessTokenExpiresAt, refreshToken, sessionExpiresAt, accountId]
      properties:
        tokenType:
          type: string
          enum: [Bearer]
        accessToken:
          type: string
          description: JWT for the Authorization header.
        accessTokenExpiresAt:
          type: string
          format: date-time
        refreshToken:
          type: string
          description: Opaque credential for renewing access. Store securely; never log it.
        sessionExpiresAt:
          type: string
          format: date-time
          description: Absolute end of the login session. Never extended.
        accountId:
          type: string
          format: uuid
    FieldError:
      type: object
      required: [field, code]
      properties:
        field:
          type: string
          example: password
        code:
          type: string
          enum: [REQUIRED, INVALID_FORMAT, TOO_SHORT, TOO_LONG, INVALID_CHARACTERS, MISMATCH]
    Problem:
      type: object
      required: [status, code]
      properties:
        type:
          type: string
        title:
          type: string
        status:
          type: integer
        detail:
          type: string
        instance:
          type: string
        code:
          type: string
          enum:
            - VALIDATION_FAILED
            - MALFORMED_REQUEST
            - EMAIL_ALREADY_REGISTERED
            - INVALID_CREDENTIALS
            - AUTHENTICATION_REQUIRED
            - TEMPORARILY_UNAVAILABLE
        fieldErrors:
          type: array
          items:
            $ref: '#/components/schemas/FieldError'
  responses:
    BadRequest:
      description: VALIDATION_FAILED (see fieldErrors) or MALFORMED_REQUEST. Nothing was saved.
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/Problem'
    Unauthorized:
      description: AUTHENTICATION_REQUIRED. The login session is unusable; authenticate again. Not an empty result.
      headers:
        WWW-Authenticate:
          schema:
            type: string
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/Problem'
    Unavailable:
      description: TEMPORARILY_UNAVAILABLE. The outcome is not confirmed; review or retry.
      content:
        application/problem+json:
          schema:
            $ref: '#/components/schemas/Problem'
```

---

## 8. Manual Implementation Walkthrough

Create the story branch (for example `feature/US-AUTH-002-log-in`) from `main` **after** the US-AUTH-001 PR is merged.

### Step 1 — Domain, api, application, ports (files 4–18)

Start with code that needs no new dependency: `SessionPolicy`, `LoginSession`, `RefreshTokenHash`, the two `api` types, the command/result/exception, the modified `AccountRepository`, the four new ports and the two services.

*Check:* `.\mvnw.cmd -q compile` fails only because `JdbcAccountRepository` does not implement `findByEmail` yet — that is expected and tells you the port change worked. Continue.

Login flow as executed, for a correct password:

```text
LoginController.logIn({email:" Person@Example.com ", password:"…"})
 └─ LogInService.logIn
     ├─ requireCredentials → both present
     ├─ EmailAddress.normalize → person@example.com
     ├─ accounts.findByEmail → Account(id=A, hash={pbkdf2-sha256-v1}…)
     ├─ passwordHasher.matches(exact password, hash) → true     (~0.5 s)
     ├─ now = 08:00:00Z; session S = establish(A, now) → expires 2026-02-04T08:00:00Z
     ├─ refreshToken = "q8lC…" (43 chars)
     ├─ accessToken = JWT{sub=A, sid=S, iat=08:00:00, exp=08:15:00}
     └─ inTransaction: INSERT session S; INSERT refresh_token(sha256("q8lC…")) → COMMIT
 └─ 200 TokenResponse
```

### Step 2 — Persistence adapters (files 19–22)

Create `JdbcTime`, update `JdbcAccountRepository`, create `JdbcSessionRepository` and `SpringIdentityTransactions`. Then the migration `V3__create_identity_session.sql` (file 3).

*Check:* `.\mvnw.cmd -q compile` succeeds.

### Step 3 — The "everything locks" moment: dependency + security together (files 1, 23–33)

Adding `spring-boot-starter-security-oauth2-resource-server` turns on Spring Security for **every** endpoint. If you add the dependency and run the app before `IdentitySecurityConfiguration` exists, registration answers **401** and Boot prints a generated password. Therefore do these files together, in this order:

1. `pom.xml` (7.1) — accept the classpath refresh in VS Code.
2. `JwtClaimNames` (7.22a), token adapters (7.23, 7.24) and security adapters (7.25–7.29a).
3. `AccessTokenProperties`, `IdentityTokenConfiguration`, updated `IdentityConfiguration`, `IdentitySecurityConfiguration` (7.30–7.33).
4. `application-local.properties` (7.2).

> **Concept — the filter chain.** Before a request reaches a controller it passes through a chain of servlet filters. Spring Security inserts one filter (`FilterChainProxy`) that runs *your* `SecurityFilterChain`. `BearerTokenAuthenticationFilter` finds the token, the `JwtDecoder` checks it, the converter builds the `Authentication`, and the authorization filter checks `permitAll()` / `authenticated()`. Failure at any point → the entry point writes the 401.

*Common mistakes:*

- Using `NimbusJwtDecoder.withSecretKey(key).build()` without `setJwtValidator(...)`: Spring's default validator allows 60 s of clock skew and uses the system clock; the exact-expiry test fails.
- Calling `decoder.setJwtValidator(new AccessTokenExpiryValidator(clock))` **alone**: this *replaces* the defaults, so the issuer and type checks would disappear. Use the full list in 7.31.
- Forgetting the `ERROR` dispatcher permit: real 404/500 errors would turn into confusing 401s.
- Importing `JwtAccessTokenIssuer` into the converter to reuse a constant. Use `JwtClaimNames`; inbound and outbound adapters never import each other.
- Catching `DataAccessException` (all of it) in the converter, or making `SessionAuthorityUnavailableException` extend `AuthenticationServiceException`. The first disguises programming errors as outages; the second makes Spring Security rethrow it as a 500 instead of calling the entry point.

### Step 4 — Web layer and contract (files 34–38)

Create `LoginRequest`, `TokenResponse`, `LoginController`; update `IdentityApiExceptionHandler`; replace `openapi.yaml`.

### Step 5 — Run locally with a signing secret

The app now refuses to start without a signing secret. In the terminal that starts Boot (after the database variables from the backend README §9):

```powershell
Set-Location 'D:\CapsuleAI\backend'
$secretBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($secretBytes)
$env:CAPSULE_JWT_SIGNING_SECRET = [Convert]::ToBase64String($secretBytes)
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

This works in Windows PowerShell 5.1. A new secret on every start simply invalidates older access tokens — fine for development. Never commit or share the value.

In a second terminal:

```powershell
$base = 'http://127.0.0.1:8080/api/v1/auth'
function Send-Json($uri, $object) {
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($object | ConvertTo-Json))
    Invoke-RestMethod -Method Post -Uri $uri -ContentType 'application/json; charset=utf-8' -Body $bytes
}

# Registration is still public
Send-Json "$base/registrations" @{ email = 'person@example.com'; password = 'correct horse battery staple'; confirmPassword = 'correct horse battery staple' }

# Login (variant email) - do not paste the tokens into chats or screenshots
$tokens = Send-Json "$base/login" @{ email = ' PERSON@example.com '; password = 'correct horse battery staple' }
$tokens.accessTokenExpiresAt
$tokens.sessionExpiresAt

# Any non-public route without a token -> 401 problem JSON
try { Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/anything' } catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}

# Wrong password -> 401 INVALID_CREDENTIALS
try { Send-Json "$base/login" @{ email = 'person@example.com'; password = 'wrong password 123' } } catch {
    $_.ErrorDetails.Message
}
```

Inspect the stored session (no raw token appears anywhere):

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT s.id, s.account_id, s.established_at, s.expires_at, left(t.token_hash, 12) AS hash_prefix FROM identity_session s JOIN identity_refresh_token t ON t.session_id = s.id;'
```

Production has no protected endpoint yet, so the "valid token → 200" path is verified by the automated test-only probe (Section 9). US-AUTH-004 adds logout, the first real protected operation.

**Optional manual outage check (fail closed → 503).** With the app running and `$tokens` from above, stop only the database, call any non-public route with the still-valid access token, then start the database again:

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env stop postgres
try {
    Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/v1/anything' -Headers @{ Authorization = "Bearer $($tokens.accessToken)" }
} catch {
    $_.Exception.Response.StatusCode.value__     # expect 503 (may take ~30 s: Hikari's connection timeout)
    $_.ErrorDetails.Message                      # expect code TEMPORARILY_UNAVAILABLE, no SQL or host details
}
docker compose --env-file .env up -d --wait --wait-timeout 60 postgres
```

With the database running, the same request authenticates and then returns 404 (the route does not exist) — proof that the 503 came from the authority check, not from routing. The application log shows one `Session authority check failed because the database is unreachable` entry with the cause; the HTTP body shows none of it.

---

## 9. Complete Automated Test Code

Paths relative to `backend/src/test/`.

| # | File | Kind | Runner |
| --- | --- | --- | --- |
| T1 | `resources/application-test.properties` | Test configuration — **NEW** | — |
| T2 | `java/com/capsuleai/identity/support/IdentityTestConfiguration.java` | Support — **MODIFY** (probe) | — |
| T3 | `java/com/capsuleai/identity/support/IdentityIntegrationTestSupport.java` | Support — **MODIFY** (login helpers) | — |
| T4 | `java/com/capsuleai/identity/domain/SessionPolicyTest.java` | Domain | Surefire |
| T5 | `java/com/capsuleai/identity/domain/LoginSessionTest.java` | Domain | Surefire |
| T6 | `java/com/capsuleai/identity/domain/RefreshTokenHashTest.java` | Domain | Surefire |
| T7 | `java/com/capsuleai/identity/application/InMemoryAccountRepository.java` | Fake — **MODIFY** | — |
| T8 | `java/com/capsuleai/identity/application/InMemorySessionRepository.java` | Fake | — |
| T9 | `java/com/capsuleai/identity/application/ImmediateTransactions.java` | Fake | — |
| T10 | `java/com/capsuleai/identity/application/FakeAccessTokenIssuer.java` | Fake | — |
| T11 | `java/com/capsuleai/identity/application/SequenceRefreshTokenGenerator.java` | Fake | — |
| T12 | `java/com/capsuleai/identity/application/LogInServiceTest.java` | Application | Surefire |
| T13 | `java/com/capsuleai/identity/application/SessionAuthorityServiceTest.java` | Application | Surefire |
| T14 | `java/com/capsuleai/identity/adapter/out/persistence/SessionSchemaIT.java` | PostgreSQL / migration | Failsafe |
| T15 | `java/com/capsuleai/identity/adapter/in/web/LoginApiIT.java` | HTTP + security + PostgreSQL | Failsafe |
| T16 | `java/com/capsuleai/identity/adapter/in/security/AccessTokenSecurityIT.java` | Real security chain | Failsafe |
| T17 | `java/com/capsuleai/identity/adapter/in/security/SessionAwareJwtAuthenticationConverterTest.java` | Adapter unit (401 / 503 / 500 classification) | Surefire |
| T18 | `java/com/capsuleai/identity/adapter/in/security/SessionAuthorityOutageIT.java` | Real security chain with an unreachable authority | Failsafe |

All Stage 1 tests remain and must still pass. In particular `RegistrationApiIT` now runs **through** the security chain, proving registration stayed public.

### T1 — `resources/application-test.properties` — NEW

**Objective.** Every Spring context started with profile `test` (all Identity ITs **and** the baseline `DatabaseSmokeIT`) now needs a signing secret, otherwise `AccessTokenProperties` stops startup. This disposable key exists only in test sources.

```properties
# Disposable, test-only access-token signing key (Base64 of a fixed 43-byte phrase).
# It protects nothing and must never be used outside automated tests.
capsuleai.identity.access-token.signing-secret=Y2Fwc3VsZWFpLWlkZW50aXR5LXRlc3Qtb25seS1zaWduaW5nLWtleS12MQ==
```

### T2 — `java/com/capsuleai/identity/support/IdentityTestConfiguration.java` — MODIFY

**Objective.** Adds a **test-only** protected endpoint (TD-21) that returns the principal resolved by the real security chain, so tests can prove *which* account and session were authenticated.

How it is registered: it is a `public static` nested class annotated with `@RestController` inside this `@TestConfiguration`. Spring registers nested component classes of a configuration class as beans automatically, so **no `@Bean` method is needed** (adding one would register it twice and fail with "Ambiguous mapping"). Component scanning ignores it because its enclosing class is a `@TestConfiguration`, so it never appears in production or in `DatabaseSmokeIT`.

Complete resulting file:

```java
package com.capsuleai.identity.support;

import com.capsuleai.identity.api.AuthenticatedAccount;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** Test infrastructure shared by all Identity integration tests. */
@TestConfiguration(proxyBeanMethods = false)
public class IdentityTestConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer identityPostgres() {
        return new PostgreSQLContainer("postgres:17.11-bookworm")
                .withDatabaseName("capsuleai_identity_test");
    }

    @Bean
    @Primary
    MutableClock mutableClock() {
        return new MutableClock(MutableClock.DEFAULT_START);
    }

    /**
     * TEST-ONLY protected operation. Production has no such endpoint. It returns
     * the principal resolved by the real security chain.
     */
    @RestController
    public static class ProtectedProbeController {

        public static final String PATH = "/test/identity/whoami";

        @GetMapping(PATH)
        public Map<String, String> whoAmI(@AuthenticationPrincipal AuthenticatedAccount account) {
            return Map.of(
                    "accountId", account.accountId().toString(),
                    "sessionId", account.sessionId().toString());
        }
    }
}
```

### T3 — `java/com/capsuleai/identity/support/IdentityIntegrationTestSupport.java` — MODIFY

**Objective.** Adds helpers for login, bearer requests, the probe and reading tokens. Stage 1 helpers are unchanged. `IssuedTokens` is a `public` record so subclasses in other packages can use it.

Complete resulting file:

```java
package com.capsuleai.identity.support;

import com.capsuleai.CapsuleAiApplication;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for Identity integration tests: real application and security
 * chain, real PostgreSQL (Testcontainers), MockMvc, controllable clock.
 */
@SpringBootTest(classes = {CapsuleAiApplication.class, IdentityTestConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IdentityIntegrationTestSupport {

    protected static final String REGISTRATIONS = "/api/v1/auth/registrations";
    protected static final String LOGIN = "/api/v1/auth/login";
    protected static final String PROTECTED_PROBE = IdentityTestConfiguration.ProtectedProbeController.PATH;
    protected static final String PASSWORD = "correct horse battery staple";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Tokens returned by a TokenResponse body. */
    public record IssuedTokens(UUID accountId, String accessToken, String refreshToken) {
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    protected MutableClock clock;

    @BeforeEach
    protected void resetIdentityState() {
        clock.setInstant(MutableClock.DEFAULT_START);
        List<String> identityTables = jdbc.sql("""
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public' AND tablename LIKE 'identity%'
                        """)
                .query(String.class)
                .list();
        if (!identityTables.isEmpty()) {
            jdbc.sql("TRUNCATE TABLE " + String.join(", ", identityTables) + " CASCADE").update();
        }
    }

    protected ResultActions postJson(String path, Map<String, ?> body) throws Exception {
        return postJson(path, body, null);
    }

    protected ResultActions postJson(String path, Map<String, ?> body, String accessToken) throws Exception {
        MockHttpServletRequestBuilder request = post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body));
        if (accessToken != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return mockMvc.perform(request);
    }

    protected ResultActions getProtectedProbe(String accessToken) throws Exception {
        return mockMvc.perform(get(PROTECTED_PROBE).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    protected ResultActions register(String email, String password) throws Exception {
        return postJson(REGISTRATIONS, registrationBody(email, password, password, null));
    }

    protected static Map<String, Object> registrationBody(
            String email, String password, String confirmPassword, String displayName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("confirmPassword", confirmPassword);
        body.put("displayName", displayName);
        return body;
    }

    protected UUID registerAccount(String email, String password) throws Exception {
        String body = register(email, password)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(body, "$.accountId"));
    }

    protected IssuedTokens logIn(String email, String password) throws Exception {
        return tokensFrom(postJson(LOGIN, Map.of("email", email, "password", password)));
    }

    protected IssuedTokens registerAndLogIn(String email) throws Exception {
        registerAccount(email, PASSWORD);
        return logIn(email, PASSWORD);
    }

    /** Expects HTTP 200 and reads a TokenResponse body. */
    protected static IssuedTokens tokensFrom(ResultActions response) throws Exception {
        String body = response
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return new IssuedTokens(
                UUID.fromString(JsonPath.read(body, "$.accountId")),
                JsonPath.read(body, "$.accessToken"),
                JsonPath.read(body, "$.refreshToken"));
    }

    /** The session id the real security chain resolved for this token (via the test-only probe). */
    protected UUID sessionIdOf(String accessToken) throws Exception {
        String body = getProtectedProbe(accessToken)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(body, "$.sessionId"));
    }

    protected long countRows(String table) {
        return jdbc.sql("SELECT count(*) FROM " + table).query(Long.class).single();
    }
}
```

### T4 — `java/com/capsuleai/identity/domain/SessionPolicyTest.java`

**Covers:** FR-AUTH-010 numbers; TD-10 cap.

```java
package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionPolicyTest {

    private static final Instant ISSUED = Instant.parse("2026-01-05T08:00:00Z");

    @Test
    void accessTokensLastFifteenMinutes() {
        assertThat(SessionPolicy.ACCESS_TOKEN_LIFETIME).isEqualTo(Duration.ofMinutes(15));
        assertThat(SessionPolicy.accessTokenExpiresAt(ISSUED, ISSUED.plus(Duration.ofDays(30))))
                .isEqualTo(Instant.parse("2026-01-05T08:15:00Z"));
    }

    @Test
    void loginSessionsLastAtMostThirtyTimesTwentyFourHours() {
        assertThat(SessionPolicy.SESSION_MAXIMUM_LIFETIME).isEqualTo(Duration.ofHours(720));
    }

    @Test
    void anAccessTokenNeverOutlivesItsSession() {
        Instant sessionEnd = ISSUED.plus(Duration.ofMinutes(10));

        assertThat(SessionPolicy.accessTokenExpiresAt(ISSUED, sessionEnd)).isEqualTo(sessionEnd);
    }
}
```

### T5 — `java/com/capsuleai/identity/domain/LoginSessionTest.java`

**Covers:** FR-AUTH-010 session boundary (just before / at / after), account ownership. US-AUTH-004 replaces this file with a version that also covers revocation.

```java
package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginSessionTest {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void establishedSessionEndsExactlyThirtyDaysLater() {
        assertThat(established().expiresAt()).isEqualTo(Instant.parse("2026-02-04T08:00:00Z"));
    }

    @Test
    void isUsableUntilJustBeforeItsMaximumLifetime() {
        LoginSession session = established();

        assertThat(session.isUsableAt(ESTABLISHED)).isTrue();
        assertThat(session.isUsableAt(session.expiresAt().minusSeconds(1))).isTrue();
        assertThat(session.isUsableAt(session.expiresAt())).isFalse();
        assertThat(session.isUsableAt(session.expiresAt().plusSeconds(1))).isFalse();
    }

    @Test
    void belongsOnlyToItsAccount() {
        LoginSession session = established();

        assertThat(session.belongsTo(ACCOUNT_ID)).isTrue();
        assertThat(session.belongsTo(UUID.randomUUID())).isFalse();
    }

    @Test
    void rejectsASessionThatDoesNotExpireAfterItStarts() {
        assertThatThrownBy(() -> new LoginSession(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED, ESTABLISHED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static LoginSession established() {
        return LoginSession.establish(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED);
    }
}
```

### T6 — `java/com/capsuleai/identity/domain/RefreshTokenHashTest.java`

**Covers:** TD-11. The expected value for `"abc"` is the published SHA-256 test vector, derived independently of the production code.

```java
package com.capsuleai.identity.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenHashTest {

    @Test
    void isTheLowerCaseHexSha256OfTheToken() {
        assertThat(RefreshTokenHash.of("abc").value())
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void equalTokensHashEquallyAndDifferentTokensDiffer() {
        assertThat(RefreshTokenHash.of("token-1")).isEqualTo(RefreshTokenHash.of("token-1"));
        assertThat(RefreshTokenHash.of("token-1")).isNotEqualTo(RefreshTokenHash.of("token-2"));
    }

    @Test
    void neverPrintsItsValue() {
        assertThat(RefreshTokenHash.of("abc").toString()).doesNotContain("ba7816");
    }

    @Test
    void rejectsAMissingToken() {
        assertThatThrownBy(() -> RefreshTokenHash.of("")).isInstanceOf(IllegalArgumentException.class);
    }
}
```

### T7 — `java/com/capsuleai/identity/application/InMemoryAccountRepository.java` — MODIFY

Complete resulting file (adds `findByEmail`):

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccountRepository;
import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory fake of {@link AccountRepository} for application tests. */
final class InMemoryAccountRepository implements AccountRepository {

    private final Map<EmailAddress, Account> accountsByEmail = new LinkedHashMap<>();

    @Override
    public synchronized boolean addIfEmailAvailable(Account account) {
        return accountsByEmail.putIfAbsent(account.email(), account) == null;
    }

    @Override
    public synchronized Optional<Account> findByEmail(EmailAddress email) {
        return Optional.ofNullable(accountsByEmail.get(email));
    }

    synchronized List<Account> all() {
        return List.copyOf(accountsByEmail.values());
    }
}
```

### T8 — `java/com/capsuleai/identity/application/InMemorySessionRepository.java`

Fake of the session port. US-AUTH-004 and US-AUTH-003 replace it with extended versions that keep the same helper methods, so these tests keep compiling.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link SessionRepository} for application tests. */
final class InMemorySessionRepository implements SessionRepository {

    private final Map<UUID, LoginSession> sessionsById = new LinkedHashMap<>();
    private final Map<RefreshTokenHash, UUID> sessionIdsByRefreshToken = new LinkedHashMap<>();

    @Override
    public synchronized void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
        sessionsById.put(session.id(), session);
        sessionIdsByRefreshToken.put(firstRefreshToken, session.id());
    }

    @Override
    public synchronized Optional<LoginSession> findById(UUID sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    synchronized List<LoginSession> all() {
        return List.copyOf(sessionsById.values());
    }

    synchronized Optional<UUID> sessionIdForRefreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(sessionIdsByRefreshToken.get(hash));
    }
}
```

### T9 — `java/com/capsuleai/identity/application/ImmediateTransactions.java`

Runs work immediately and counts boundaries, so a test can assert "the session was written inside a transaction". It proves nothing about real commits.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.IdentityTransactions;
import java.util.function.Supplier;

/** Fake transaction boundary: runs the work directly and counts how often it was used. */
final class ImmediateTransactions implements IdentityTransactions {

    private int transactions;

    @Override
    public synchronized <T> T inTransaction(Supplier<T> work) {
        transactions++;
        return work.get();
    }

    synchronized int count() {
        return transactions;
    }
}
```

### T10 — `java/com/capsuleai/identity/application/FakeAccessTokenIssuer.java`

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fake issuer that records what it was asked to sign. */
final class FakeAccessTokenIssuer implements AccessTokenIssuer {

    record Issued(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt) {
    }

    private final List<Issued> issued = new ArrayList<>();

    @Override
    public synchronized String issue(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt) {
        issued.add(new Issued(accountId, sessionId, issuedAt, expiresAt));
        return "access-token-" + issued.size();
    }

    synchronized List<Issued> issued() {
        return List.copyOf(issued);
    }
}
```

### T11 — `java/com/capsuleai/identity/application/SequenceRefreshTokenGenerator.java`

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.RefreshTokenGenerator;

/** Deterministic, predictable refresh tokens for application tests (never use outside tests). */
final class SequenceRefreshTokenGenerator implements RefreshTokenGenerator {

    private int next;

    @Override
    public synchronized String newToken() {
        next++;
        return "refresh-token-" + next;
    }
}
```

### T12 — `java/com/capsuleai/identity/application/LogInServiceTest.java`

**Covers:** AC-01 (session for the right account), AC-02, AC-03, AC-04, AC-05, AC-07 (failed attempts create nothing; tokens tied to one committed transaction); BRULE-AUTH-006 (separate sessions). **Does not prove:** SQL, real signing, the security chain.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogInServiceTest {

    private static final String PASSWORD = "correct horse battery staple";
    /** "  Mật khẩu rất dài  " */
    private static final String UNICODE_PASSWORD = "  M\u1EADt kh\u1EA9u r\u1EA5t d\u00E0i  ";

    private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    private final RecordingPasswordHasher hasher = new RecordingPasswordHasher();
    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final SequenceRefreshTokenGenerator refreshTokens = new SequenceRefreshTokenGenerator();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final ImmediateTransactions transactions = new ImmediateTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);

    private final LogInService service =
            new LogInService(accounts, hasher, sessions, refreshTokens, accessTokens, transactions, clock);

    @Test
    void establishesASessionForTheNormalizedEmailAndExactPassword() {
        Account account = registered("person@example.com", PASSWORD);

        SessionTokens tokens = service.logIn(new LogInCommand("  PERSON@example.com ", PASSWORD));

        assertThat(sessions.all()).hasSize(1);
        LoginSession session = sessions.all().get(0);
        assertThat(tokens.accountId()).isEqualTo(account.id());
        assertThat(session.accountId()).isEqualTo(account.id());
        assertThat(session.establishedAt()).isEqualTo(MutableClock.DEFAULT_START);
        assertThat(tokens.sessionExpiresAt())
                .isEqualTo(session.expiresAt())
                .isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofDays(30)));
        assertThat(tokens.accessTokenExpiresAt())
                .isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofMinutes(15)));
        assertThat(sessions.sessionIdForRefreshToken(RefreshTokenHash.of(tokens.refreshToken())))
                .contains(session.id());
        assertThat(accessTokens.issued()).singleElement().satisfies(issued -> {
            assertThat(issued.accountId()).isEqualTo(account.id());
            assertThat(issued.sessionId()).isEqualTo(session.id());
            assertThat(issued.expiresAt()).isEqualTo(tokens.accessTokenExpiresAt());
        });
        assertThat(transactions.count()).isOne();
    }

    @Test
    void everySuccessfulLoginCreatesASeparateSession() {
        registered("person@example.com", PASSWORD);

        SessionTokens first = service.logIn(new LogInCommand("person@example.com", PASSWORD));
        SessionTokens second = service.logIn(new LogInCommand("person@example.com", PASSWORD));

        assertThat(sessions.all()).hasSize(2);
        assertThat(sessions.all().get(0).id()).isNotEqualTo(sessions.all().get(1).id());
        assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
    }

    @Test
    void wrongPasswordGrantsNoSessionAndNoToken() {
        registered("person@example.com", PASSWORD);

        assertThatThrownBy(() -> service.logIn(new LogInCommand("person@example.com", "not the password")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(sessions.all()).isEmpty();
        assertThat(accessTokens.issued()).isEmpty();
        assertThat(transactions.count()).isZero();
    }

    @Test
    void unknownEmailIsRejectedExactlyLikeAWrongPassword() {
        assertThatThrownBy(() -> service.logIn(new LogInCommand("nobody@example.com", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(sessions.all()).isEmpty();
    }

    @Test
    void exactPasswordWithSpacesAndUnicodeAuthenticates() {
        registered("unicode@example.com", UNICODE_PASSWORD);

        assertThat(service.logIn(new LogInCommand("unicode@example.com", UNICODE_PASSWORD)).accessToken())
                .isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("changedVariantsOfTheUnicodePassword")
    void changedPasswordVariantsDoNotAuthenticate(String variant) {
        registered("unicode@example.com", UNICODE_PASSWORD);

        assertThatThrownBy(() -> service.logIn(new LogInCommand("unicode@example.com", variant)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    static Stream<String> changedVariantsOfTheUnicodePassword() {
        return Stream.of(
                UNICODE_PASSWORD.strip(),
                UNICODE_PASSWORD.replace(" ", ""),
                Normalizer.normalize(UNICODE_PASSWORD, Normalizer.Form.NFD));
    }

    @Test
    void incompleteCredentialsAreValidationErrors() {
        assertThatThrownBy(() -> service.logIn(new LogInCommand(" ", "")))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new FieldViolation("email", ValueProblem.REQUIRED),
                                new FieldViolation("password", ValueProblem.REQUIRED)));
    }

    @Test
    void malformedEmailIsTreatedAsInvalidCredentials() {
        assertThatThrownBy(() -> service.logIn(new LogInCommand("not-an-email", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginNeedsNoOptionalProfileInformation() {
        Account account = registered("minimal@example.com", PASSWORD);
        assertThat(account.displayName()).isEmpty();

        assertThat(service.logIn(new LogInCommand("minimal@example.com", PASSWORD)).accountId())
                .isEqualTo(account.id());
    }

    private Account registered(String email, String password) {
        Account account = new Account(UUID.randomUUID(), EmailAddress.normalize(email),
                hasher.hash(password), Optional.empty(), clock.instant());
        accounts.addIfEmailAvailable(account);
        return account;
    }
}
```

### T13 — `java/com/capsuleai/identity/application/SessionAuthorityServiceTest.java`

**Covers:** FR-AUTH-009 authority decision; US-PRIV-001 AC-02 (another account's session is refused).

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.MutableClock;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionAuthorityServiceTest {

    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final SessionAuthorityService authority = new SessionAuthorityService(sessions, clock);
    private final UUID accountId = UUID.randomUUID();

    @Test
    void usableSessionAuthenticatesItsAccount() {
        LoginSession session = storedSession();

        assertThat(authority.findUsableSession(accountId, session.id()))
                .contains(new AuthenticatedAccount(accountId, session.id()));
    }

    @Test
    void sessionPresentedForAnotherAccountIsNotAccepted() {
        LoginSession session = storedSession();

        assertThat(authority.findUsableSession(UUID.randomUUID(), session.id())).isEmpty();
    }

    @Test
    void unknownSessionIsNotAccepted() {
        assertThat(authority.findUsableSession(accountId, UUID.randomUUID())).isEmpty();
    }

    @Test
    void sessionAtItsMaximumLifetimeIsNotAccepted() {
        LoginSession session = storedSession();

        clock.setInstant(session.expiresAt().minusSeconds(1));
        assertThat(authority.findUsableSession(accountId, session.id())).isPresent();

        clock.setInstant(session.expiresAt());
        assertThat(authority.findUsableSession(accountId, session.id())).isEmpty();
    }

    private LoginSession storedSession() {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), accountId, clock.instant());
        sessions.create(session, RefreshTokenHash.of("refresh-" + session.id()));
        return session;
    }
}
```

### T14 — `java/com/capsuleai/identity/adapter/out/persistence/SessionSchemaIT.java`

**Covers:** V3 migration; DATA-AUTH-005 integrity (account ownership FK, 30-day maximum, unique well-formed hashes). Uses two different sessions for the duplicate-hash check so the test stays correct after US-AUTH-003 adds a per-session index.

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");

    @Autowired
    private Flyway flyway;

    @Test
    void sessionMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("3");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void sessionMustBelongToAnExistingAccount() {
        assertThatThrownBy(() -> insertSession(UUID.randomUUID(), ESTABLISHED, ESTABLISHED.plus(Duration.ofDays(30))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseEnforcesTheThirtyDayMaximum() {
        UUID accountId = insertAccount();

        insertSession(accountId, ESTABLISHED, ESTABLISHED.plus(Duration.ofHours(720)));

        assertThatThrownBy(() -> insertSession(accountId, ESTABLISHED,
                ESTABLISHED.plus(Duration.ofHours(720)).plusSeconds(1)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertSession(accountId, ESTABLISHED, ESTABLISHED))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void refreshTokenHashesAreUniqueAndWellFormed() {
        UUID accountId = insertAccount();
        UUID firstSession = insertSession(accountId, ESTABLISHED, ESTABLISHED.plus(Duration.ofDays(30)));
        UUID secondSession = insertSession(accountId, ESTABLISHED, ESTABLISHED.plus(Duration.ofDays(30)));
        String hash = RefreshTokenHash.of("some-refresh-token").value();

        insertRefreshToken(firstSession, hash);

        assertThatThrownBy(() -> insertRefreshToken(secondSession, hash))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insertRefreshToken(secondSession, "NOT-A-SHA-256-HASH"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertAccount() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_account (id, email, password_hash, created_at)
                        VALUES (:id, :email, '{test}hash', now())
                        """)
                .param("id", id)
                .param("email", id + "@example.com")
                .update();
        return id;
    }

    private UUID insertSession(UUID accountId, Instant establishedAt, Instant expiresAt) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", id)
                .param("accountId", accountId)
                .param("establishedAt", establishedAt.atOffset(ZoneOffset.UTC))
                .param("expiresAt", expiresAt.atOffset(ZoneOffset.UTC))
                .update();
        return id;
    }

    private void insertRefreshToken(UUID sessionId, String tokenHash) {
        jdbc.sql("""
                        INSERT INTO identity_refresh_token (id, session_id, token_hash, issued_at)
                        VALUES (:id, :sessionId, :tokenHash, now())
                        """)
                .param("id", UUID.randomUUID())
                .param("sessionId", sessionId)
                .param("tokenHash", tokenHash)
                .update();
    }
}
```

### T15 — `java/com/capsuleai/identity/adapter/in/web/LoginApiIT.java`

**Covers:** AC-01, AC-02, AC-03 (and US-AUTH-001 AC-05 end-to-end), AC-04, AC-06 (secrets), AC-07 (failures write nothing); registration still public (regression); TD-17. **Proves:** HTTP → real PBKDF2 → PostgreSQL → real JWT, through the configured filter chain. **Does not prove:** mobile behaviour.

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class LoginApiIT extends IdentityIntegrationTestSupport {

    @Test
    void loginIssuesBearerTokensForTheRegisteredAccount() throws Exception {
        UUID accountId = registerAccount("person@example.com", PASSWORD);

        postJson(LOGIN, Map.of("email", "  Person@EXAMPLE.com ", "password", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.accessTokenExpiresAt").value("2026-01-05T08:15:00Z"))
                .andExpect(jsonPath("$.sessionExpiresAt").value("2026-02-04T08:00:00Z"));
    }

    @Test
    void loginReachesTheCorrectAccountThroughTheProtectedPath() throws Exception {
        UUID accountId = registerAccount("owner@example.com", PASSWORD);

        IssuedTokens tokens = logIn("owner@example.com", PASSWORD);

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()));
    }

    @Test
    void storesSessionAuthorityAndOnlyAHashOfTheRefreshToken() throws Exception {
        IssuedTokens tokens = registerAndLogIn("hash@example.com");

        Map<String, Object> session = jdbc.sql("SELECT account_id FROM identity_session").query().singleRow();
        assertThat(session.get("account_id")).isEqualTo(tokens.accountId());
        String storedHash = jdbc.sql("SELECT token_hash FROM identity_refresh_token").query(String.class).single();
        assertThat(storedHash)
                .isEqualTo(RefreshTokenHash.of(tokens.refreshToken()).value())
                .isNotEqualTo(tokens.refreshToken());
    }

    @Test
    void exactPasswordWithSpacesAndUnicodeAuthenticatesButChangedVariantsDoNot() throws Exception {
        String exact = "  M\u1EADt kh\u1EA9u r\u1EA5t d\u00E0i  ";
        registerAccount("unicode@example.com", exact);

        logIn("unicode@example.com", exact);

        for (String variant : List.of(
                exact.strip(),
                exact.replace(" ", ""),
                Normalizer.normalize(exact, Normalizer.Form.NFD))) {
            postJson(LOGIN, Map.of("email", "unicode@example.com", "password", variant))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        assertThat(countRows("identity_session")).isOne();
    }

    @Test
    void wrongPasswordAndUnknownEmailAreIndistinguishable() throws Exception {
        registerAccount("known@example.com", PASSWORD);

        String wrongPassword = postJson(LOGIN, Map.of("email", "known@example.com", "password", "not the password!"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String unknownEmail = postJson(LOGIN, Map.of("email", "unknown@example.com", "password", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(JsonPath.<String>read(wrongPassword, "$.code")).isEqualTo("INVALID_CREDENTIALS");
        assertThat(wrongPassword).isEqualTo(unknownEmail);
        assertThat(countRows("identity_session")).isZero();
    }

    @Test
    void incompleteCredentialsAreValidationErrors() throws Exception {
        postJson(LOGIN, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].code", hasItem("REQUIRED")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')].code", hasItem("REQUIRED")));
    }

    @Test
    void loginAttemptsDoNotChangeTheAccount() throws Exception {
        registerAccount("stable@example.com", PASSWORD);
        Map<String, Object> before = jdbc.sql("SELECT * FROM identity_account").query().singleRow();

        postJson(LOGIN, Map.of("email", "stable@example.com", "password", "wrong password 123"))
                .andExpect(status().isUnauthorized());
        logIn("stable@example.com", PASSWORD);

        assertThat(jdbc.sql("SELECT * FROM identity_account").query().singleRow()).isEqualTo(before);
    }

    @Test
    void passwordsAndTokensNeverAppearInLogs(CapturedOutput output) throws Exception {
        String password = "Distinctive-Login-Secret-9902";
        registerAccount("logs@example.com", password);

        IssuedTokens tokens = logIn("logs@example.com", password);

        assertThat(output.getAll())
                .doesNotContain(password)
                .doesNotContain(tokens.accessToken())
                .doesNotContain(tokens.refreshToken());
    }

    @Test
    void staleAuthorizationHeaderDoesNotBlockLogin() throws Exception {
        registerAccount("stale@example.com", PASSWORD);

        postJson(LOGIN, Map.of("email", "stale@example.com", "password", PASSWORD), "expired-or-garbage-token")
                .andExpect(status().isOk());
    }

    @Test
    void registrationRemainsPublicNowThatSecurityIsActive() throws Exception {
        register("public@example.com", PASSWORD).andExpect(status().isCreated());
    }
}
```

### T16 — `java/com/capsuleai/identity/adapter/in/security/AccessTokenSecurityIT.java`

**Covers:** FR-AUTH-009 (absent, malformed, invalid-signature, wrong-issuer, expired, valid tokens), FR-AUTH-010 (exact 15-minute boundary), ADA-008 (signed token without usable session), AC-06 / NFR-SEC-001 (account isolation, mixed account/session), US-AUTH-001 AC-10 and BRULE-AUTH-001 (default deny), US-PRIV-001 AC-01/02. **Proves:** the configured `SecurityFilterChain` with real HS256 tokens and real PostgreSQL lookups — exactly what [Test Strategy §9.3](../../../../07-testing/test-strategy.md#93-evidence-across-layers) asks for. **Does not prove:** Tomcat-level behaviour or client token storage.

```java
package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.configuration.AccessTokenProperties;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccessTokenSecurityIT extends IdentityIntegrationTestSupport {

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Autowired
    private AccessTokenProperties accessTokenProperties;

    @Test
    void usableAccessTokenReachesAProtectedOperationAsTheCorrectAccount() throws Exception {
        IssuedTokens tokens = registerAndLogIn("owner@example.com");

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(tokens.accountId().toString()));
    }

    @Test
    void eachAccountIsResolvedOnlyToItself() throws Exception {
        IssuedTokens first = registerAndLogIn("first@example.com");
        IssuedTokens second = registerAndLogIn("second@example.com");

        getProtectedProbe(first.accessToken())
                .andExpect(jsonPath("$.accountId").value(first.accountId().toString()));
        getProtectedProbe(second.accessToken())
                .andExpect(jsonPath("$.accountId").value(second.accountId().toString()));
    }

    @Test
    void requestWithoutAnAccessTokenIsRejectedWithoutContent() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.accountId").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-jwt", "eyJhbGciOiJIUzI1NiJ9.e30.not-a-valid-signature"})
    void malformedAccessTokensAreRejected(String token) throws Exception {
        getProtectedProbe(token)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        IssuedTokens tokens = registerAndLogIn("forged@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        String forged = sign(randomKey(), "capsuleai-identity", tokens.accountId(), sessionId);

        getProtectedProbe(forged).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFromAnotherIssuerIsRejectedEvenWithTheRealKey() throws Exception {
        IssuedTokens tokens = registerAndLogIn("issuer@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        String foreign = sign(accessTokenProperties.signingKey(), "someone-else", tokens.accountId(), sessionId);

        getProtectedProbe(foreign).andExpect(status().isUnauthorized());
    }

    @Test
    void accessTokenExpiresExactlyFifteenMinutesAfterIssue() throws Exception {
        IssuedTokens tokens = registerAndLogIn("expiry@example.com");

        clock.advance(Duration.ofMinutes(15).minusSeconds(1));
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());

        clock.advance(Duration.ofSeconds(1));
        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void validlySignedTokenWithoutPersistedSessionIsRejected() throws Exception {
        UUID accountId = registerAccount("no-session@example.com", PASSWORD);
        Instant now = clock.instant();

        String token = accessTokenIssuer.issue(accountId, UUID.randomUUID(), now, now.plus(Duration.ofMinutes(15)));

        getProtectedProbe(token).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionPresentedWithAnotherAccountIsRejected() throws Exception {
        IssuedTokens victim = registerAndLogIn("victim@example.com");
        IssuedTokens attacker = registerAndLogIn("attacker@example.com");
        UUID victimSession = sessionIdOf(victim.accessToken());
        Instant now = clock.instant();

        String mixed = accessTokenIssuer.issue(attacker.accountId(), victimSession, now, now.plus(Duration.ofMinutes(15)));

        getProtectedProbe(mixed).andExpect(status().isUnauthorized());
    }

    @Test
    void sessionAuthorityIsReadFromPostgreSqlOnEveryRequest() throws Exception {
        IssuedTokens tokens = registerAndLogIn("db-authority@example.com");
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());

        // Shorten the stored session to one second and move past it.
        jdbc.sql("UPDATE identity_session SET expires_at = established_at + INTERVAL '1 second'").update();
        clock.advance(Duration.ofSeconds(1));

        // The JWT alone is still valid for ~15 minutes, but persistent authority says no.
        getProtectedProbe(tokens.accessToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownRoutesAreProtectedByDefault() throws Exception {
        mockMvc.perform(get("/api/v1/wardrobe/garments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    private static SecretKey randomKey() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    private String sign(SecretKey key, String issuer, UUID accountId, UUID sessionId) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(accountId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(15)))
                .claim(JwtClaimNames.SESSION_ID, sessionId.toString())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return NimbusJwtEncoder.withSecretKey(key)
                .algorithm(MacAlgorithm.HS256)
                .build()
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
```

`sessionAuthorityIsReadFromPostgreSqlOnEveryRequest` is also the backend's restart-durability argument: authority lives only in PostgreSQL, never in application memory, so an application restart cannot forget a session or a later revocation.

### T17 — `java/com/capsuleai/identity/adapter/in/security/SessionAwareJwtAuthenticationConverterTest.java`

**Covers:** TD-26 classification and TD-25 claim name. A usable session becomes the principal; an unusable session and a missing claim stay **invalid-token** failures (401 path); an unreachable database becomes `SessionAuthorityUnavailableException` (503 path); a programming error (`BadSqlGrammarException`) is **not** disguised as an outage. **Proves:** the converter's decisions with plain lambda stubs of the published `SessionAuthority`. **Does not prove:** what the filter chain sends to the client (T18) or a real database outage (manual check in Step 5).

```java
package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.api.SessionAuthority;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionAwareJwtAuthenticationConverterTest {

    private final UUID accountId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    @Test
    void usableSessionBecomesTheAuthenticatedPrincipal() {
        AuthenticatedAccount expected = new AuthenticatedAccount(accountId, sessionId);

        AbstractAuthenticationToken authentication =
                converter((account, session) -> Optional.of(expected)).convert(jwt());

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(expected);
    }

    @Test
    void unusableSessionIsAnInvalidTokenNotAnOutage() {
        assertThatThrownBy(() -> converter((account, session) -> Optional.empty()).convert(jwt()))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void tokenWithoutTheSessionClaimIsAnInvalidToken() {
        Jwt withoutSessionClaim = Jwt.withTokenValue("token-value")
                .header("alg", "HS256")
                .subject(accountId.toString())
                .build();

        assertThatThrownBy(() -> converter((account, session) -> Optional.empty()).convert(withoutSessionClaim))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void unobtainableDatabaseConnectionIsReportedAsUnavailableAuthority() {
        SessionAuthority unreachable = (account, session) -> {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection");
        };

        assertThatThrownBy(() -> converter(unreachable).convert(jwt()))
                .isInstanceOf(SessionAuthorityUnavailableException.class)
                .hasCauseInstanceOf(CannotGetJdbcConnectionException.class);
    }

    @Test
    void transientResourceFailureIsAlsoReportedAsUnavailableAuthority() {
        SessionAuthority flaky = (account, session) -> {
            throw new TransientDataAccessResourceException("Connection reset");
        };

        assertThatThrownBy(() -> converter(flaky).convert(jwt()))
                .isInstanceOf(SessionAuthorityUnavailableException.class);
    }

    @Test
    void programmingErrorsAreNotDisguisedAsOutages() {
        SessionAuthority broken = (account, session) -> {
            throw new BadSqlGrammarException("find session", "SELECT nonsense", new SQLException("syntax error"));
        };

        assertThatThrownBy(() -> converter(broken).convert(jwt()))
                .isInstanceOf(BadSqlGrammarException.class);
    }

    private static SessionAwareJwtAuthenticationConverter converter(SessionAuthority authority) {
        return new SessionAwareJwtAuthenticationConverter(authority);
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token-value")
                .header("alg", "HS256")
                .subject(accountId.toString())
                .claim(JwtClaimNames.SESSION_ID, sessionId.toString())
                .issuedAt(Instant.parse("2026-01-05T08:00:00Z"))
                .expiresAt(Instant.parse("2026-01-05T08:15:00Z"))
                .build();
    }
}
```

`SessionAuthority` has a single method, so a lambda is a complete stub. The two outage tests log an error line each — that is the converter's server-side diagnostic and is expected.

### T18 — `java/com/capsuleai/identity/adapter/in/security/SessionAuthorityOutageIT.java`

**Covers:** TD-26 through the **real configured `SecurityFilterChain`**: a validly signed, unexpired token whose session cannot be checked gets **503** with the fixed problem body, no `WWW-Authenticate` challenge, no principal data, no exception text and no token echo, and the protected controller is not reached (fail closed). Invalid, expired and missing tokens still get **401** — they fail before the authority is consulted.

**How the outage is simulated.** The test replaces `SessionAuthority` with a `@Primary` stub that throws `CannotGetJdbcConnectionException` — exactly what Spring throws when HikariCP cannot supply a connection. Replacing a bean changes the Spring configuration, so this class gets **its own** application context and therefore its own PostgreSQL container (a few extra seconds); it does **not** extend `IdentityIntegrationTestSupport`, so the shared context used by every other IT stays untouched. **Does not prove:** a real database stop (manual check in Step 5).

```java
package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.CapsuleAiApplication;
import com.capsuleai.identity.api.SessionAuthority;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.support.IdentityTestConfiguration;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
        CapsuleAiApplication.class,
        IdentityTestConfiguration.class,
        SessionAuthorityOutageIT.UnreachableSessionAuthority.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionAuthorityOutageIT {

    private static final String PROTECTED_PROBE = IdentityTestConfiguration.ProtectedProbeController.PATH;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Autowired
    private MutableClock clock;

    /** Simulates a database outage at the published authority boundary. */
    @TestConfiguration(proxyBeanMethods = false)
    static class UnreachableSessionAuthority {

        @Bean
        @Primary
        SessionAuthority unreachableSessionAuthority() {
            return (accountId, sessionId) -> {
                throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection (simulated outage)");
            };
        }
    }

    @BeforeEach
    void resetClock() {
        clock.setInstant(MutableClock.DEFAULT_START);
    }

    @Test
    void confirmedDatabaseOutageFailsClosedWithASanitized503() throws Exception {
        String token = validlySignedToken();

        mockMvc.perform(get(PROTECTED_PROBE).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.accountId").doesNotExist())
                .andExpect(jsonPath("$.sessionId").doesNotExist())
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(content().string(not(containsString("JDBC"))))
                .andExpect(content().string(not(containsString("simulated"))))
                .andExpect(content().string(not(containsString(token))));
    }

    @Test
    void malformedTokenIsStillRejectedAs401() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void expiredTokenIsStillRejectedAs401() throws Exception {
        String token = validlySignedToken();
        clock.advance(Duration.ofMinutes(15));

        mockMvc.perform(get(PROTECTED_PROBE).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void missingTokenIsStillRejectedAs401() throws Exception {
        mockMvc.perform(get(PROTECTED_PROBE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    private String validlySignedToken() {
        Instant now = clock.instant();
        return accessTokenIssuer.issue(UUID.randomUUID(), UUID.randomUUID(), now, now.plus(Duration.ofMinutes(15)));
    }
}
```

Why the 401 cases matter here: they prove the classification order. Signature, issuer and expiry are checked by the decoder **before** the converter asks `SessionAuthority`, so a database outage can never turn a bad or expired token into anything other than 401. A revoked session (401) is covered by US-AUTH-004 `LogoutApiIT`, because revocation does not exist yet in this stage.

### 9.1 Test traceability

| AC | Scenario | Test Class | Test Method | Verification Level |
| --- | --- | --- | --- | --- |
| AC-01 | Correct account receives tokens | `LoginApiIT` / `LogInServiceTest` | `loginIssuesBearerTokensForTheRegisteredAccount` / `establishesASessionForTheNormalizedEmailAndExactPassword` | HTTP + DB / Application |
| AC-01 | Token reaches protected path as that account | `LoginApiIT` / `AccessTokenSecurityIT` | `loginReachesTheCorrectAccountThroughTheProtectedPath` / `usableAccessTokenReachesAProtectedOperationAsTheCorrectAccount` | Security chain |
| AC-01 | Accepted data retained | `LoginApiIT` | `loginAttemptsDoNotChangeTheAccount` | HTTP + DB |
| AC-01 | Onboarding / intended area | — | — | **External** |
| AC-02 | Email case/space variant | `LoginApiIT` / `LogInServiceTest` | `loginIssuesBearerTokensForTheRegisteredAccount` / `establishesASessionForTheNormalizedEmailAndExactPassword` | HTTP / Application |
| AC-02 | No alias merging | US-AUTH-001 `EmailAddressTest` | `providerAliasesAreNotMerged` | Domain (shared rule) |
| AC-03 | Exact value works, variants fail | `LoginApiIT` / `LogInServiceTest` | `exactPasswordWithSpacesAndUnicodeAuthenticatesButChangedVariantsDoNot` / `exactPasswordWithSpacesAndUnicodeAuthenticates`, `changedPasswordVariantsDoNotAuthenticate` | HTTP + real PBKDF2 / Application |
| AC-04 | Wrong/unknown → same 401, nothing written | `LoginApiIT` / `LogInServiceTest` | `wrongPasswordAndUnknownEmailAreIndistinguishable` / `wrongPasswordGrantsNoSessionAndNoToken`, `unknownEmailIsRejectedExactlyLikeAWrongPassword` | HTTP / Application |
| AC-04 | Incomplete → 400 | `LoginApiIT` / `LogInServiceTest` | `incompleteCredentialsAreValidationErrors` (both) | HTTP / Application |
| AC-04 | Account unchanged | `LoginApiIT` | `loginAttemptsDoNotChangeTheAccount` | HTTP + DB |
| AC-04 | Forgot Password route, wording | — | — | **External** |
| AC-05 | No optional setup needed | `LogInServiceTest` | `loginNeedsNoOptionalProfileInformation` | Application |
| AC-06 | Two accounts isolated | `AccessTokenSecurityIT` | `eachAccountIsResolvedOnlyToItself`, `sessionPresentedWithAnotherAccountIsRejected` | Security chain |
| AC-06 | No access without usable authority | `AccessTokenSecurityIT` | `requestWithoutAnAccessTokenIsRejectedWithoutContent`, `malformedAccessTokensAreRejected`, `tokenSignedWithAnotherKeyIsRejected`, `tokenFromAnotherIssuerIsRejectedEvenWithTheRealKey`, `validlySignedTokenWithoutPersistedSessionIsRejected`, `sessionAuthorityIsReadFromPostgreSqlOnEveryRequest` | Security chain + DB |
| AC-06 | Secrets not in logs / storage | `LoginApiIT` | `passwordsAndTokensNeverAppearInLogs`, `storesSessionAuthorityAndOnlyAHashOfTheRefreshToken` | HTTP + logs + DB |
| AC-06 | Client hides previous user's content | — | — | **External** |
| AC-07 | Failed attempt writes nothing; one transaction per success | `LogInServiceTest` | `wrongPasswordGrantsNoSessionAndNoToken`, `establishesASessionForTheNormalizedEmailAndExactPassword` | Application |
| AC-07 | Retry after lost response creates a separate usable session | `LogInServiceTest` | `everySuccessfulLoginCreatesASeparateSession` | Application |
| AC-07 | Pending/failed presentation | — | — | **External** |
| AC-08 | Accessible Vietnamese guidance | — | — | **External only** |
| FR-AUTH-010 | Exact 15-minute access boundary | `AccessTokenSecurityIT` / `SessionPolicyTest` | `accessTokenExpiresExactlyFifteenMinutesAfterIssue` / `accessTokensLastFifteenMinutes` | Security chain / Domain |
| FR-AUTH-010 | 30-day session boundary | `LoginSessionTest` / `SessionAuthorityServiceTest` / `SessionSchemaIT` | `isUsableUntilJustBeforeItsMaximumLifetime` / `sessionAtItsMaximumLifetimeIsNotAccepted` / `databaseEnforcesTheThirtyDayMaximum` | Domain / Application / DB |
| 001 AC-10 | Default deny | `AccessTokenSecurityIT` | `unknownRoutesAreProtectedByDefault` | Security chain |
| FR-AUTH-009 / TD-26 | Unreachable database during the authority check → fail closed, sanitized 503, no challenge, no leak | `SessionAuthorityOutageIT` / `SessionAwareJwtAuthenticationConverterTest` | `confirmedDatabaseOutageFailsClosedWithASanitized503` / `unobtainableDatabaseConnectionIsReportedAsUnavailableAuthority`, `transientResourceFailureIsAlsoReportedAsUnavailableAuthority` | Security chain (stubbed authority) / Adapter unit |
| FR-AUTH-009 / TD-26 | Invalid, expired, missing token or unusable session still 401 | `SessionAuthorityOutageIT` / `SessionAwareJwtAuthenticationConverterTest` | `malformedTokenIsStillRejectedAs401`, `expiredTokenIsStillRejectedAs401`, `missingTokenIsStillRejectedAs401` / `unusableSessionIsAnInvalidTokenNotAnOutage`, `tokenWithoutTheSessionClaimIsAnInvalidToken` | Security chain / Adapter unit |
| TD-26 | Programming errors are not reported as 503 | `SessionAwareJwtAuthenticationConverterTest` | `programmingErrorsAreNotDisguisedAsOutages` | Adapter unit |
| TD-26 | Real PostgreSQL stop → 503 | — | Manual check (Section 8, Step 5) | **Manual evidence** |
| Regression | Registration public; Stage 1 suite | `LoginApiIT` + all Stage 1 tests | `registrationRemainsPublicNowThatSecurityIsActive` | HTTP |

---

## 10. Run and Verify

```powershell
Set-Location 'D:\CapsuleAI\backend'

.\mvnw.cmd compile
.\mvnw.cmd test
.\mvnw.cmd '-Dtest=LogInServiceTest,SessionAuthorityServiceTest,SessionAwareJwtAuthenticationConverterTest' test
.\mvnw.cmd '-Dit.test=AccessTokenSecurityIT,SessionAuthorityOutageIT' verify
.\mvnw.cmd '-Dit.test=LoginApiIT,RegistrationApiIT' verify

.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
if ($LASTEXITCODE -ne 0) { throw 'Verification failed; read the first error above.' }

Get-ChildItem .\target\surefire-reports\*.txt | Get-Content
Get-ChildItem .\target\failsafe-reports\*.txt | Get-Content
```

**Expected:** `BUILD SUCCESS`; Failsafe now also lists `SessionSchemaIT`, `LoginApiIT`, `AccessTokenSecurityIT`, `SessionAuthorityOutageIT`; all Stage 1 classes still pass, including `DatabaseSmokeIT` (it needs T1 to start). `SessionAuthorityOutageIT` starts a second PostgreSQL container because it has its own context. Record the actual counts.

| Symptom | Likely cause / fix |
| --- | --- |
| Every IT fails with `signing-secret is not configured` | T1 missing or in the wrong folder (`src/test/resources`). |
| `DatabaseSmokeIT` fails with `No qualifying bean of type HttpSecurity` | `@ConditionalOnWebApplication` missing on `IdentitySecurityConfiguration`. |
| Registration tests now return 401 | `PUBLIC_ENDPOINTS` path/method typo, or the bearer resolver is not configured. |
| `Ambiguous mapping … ProtectedProbeController` | An extra `@Bean` method for the probe — remove it (see T2). |
| Probe returns 404 with a valid token | Probe class not `public static` or not nested in `IdentityTestConfiguration`. |
| Expiry test passes at +15 min (should fail) | Default `JwtTimestampValidator` still in use, or decoder not using the injected `Clock`. |
| Log shows "Using generated security password" | No `JwtDecoder` bean (check `IdentityTokenConfiguration` is in `identity.configuration`). |
| ArchUnit failure in `application` | A Spring type leaked into a service (e.g. `@Transactional`). |
| Outage test gets 500 instead of 503 | `SessionAuthorityUnavailableException` extends `AuthenticationServiceException` (rethrown by Spring Security) or the converter does not catch `DataAccessResourceFailureException`. |
| Outage test gets 401 instead of 503 | The entry point checks for the wrong type, or the converter wraps the outage in `InvalidBearerTokenException`. |
| Outage test: `expected single matching bean but found 2` for `SessionAuthority` | `@Primary` missing on the stub bean in `UnreachableSessionAuthority`. |

---

## 11. CI Integration

No workflow change is required for discovery: Surefire/Failsafe pick up the new classes; the new dependency is resolved and cached automatically; Docker is available for Testcontainers. A failing security test fails the build like any other test.

`application-test.properties` is part of the repository, so CI uses the same disposable test key. No GitHub secret is needed for tests.

**Recommended guard update** — replace the `$required` block (cumulative with Stage 1):

```powershell
          $required = @{
            'surefire-reports/TEST-com.capsuleai.foundation.HarnessSmokeTest.xml' = 2
            'surefire-reports/TEST-com.capsuleai.architecture.ArchitectureDependencyTest.xml' = 4
            'failsafe-reports/TEST-com.capsuleai.foundation.DatabaseSmokeIT.xml' = 1
            'surefire-reports/TEST-com.capsuleai.identity.application.RegisterAccountServiceTest.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.RegistrationApiIT.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.application.ConcurrentRegistrationIT.xml' = 1
            'surefire-reports/TEST-com.capsuleai.identity.application.LogInServiceTest.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.LoginApiIT.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.security.AccessTokenSecurityIT.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.security.SessionAuthorityOutageIT.xml' = 1
          }
```

After the PR run, open the job log for "Require expected test reports" and download `backend-test-reports` to confirm `AccessTokenSecurityIT` and `SessionAuthorityOutageIT` ran with zero skips.

---

## 12. Completion Checklist

**Production implementation**
- [ ] Files 1–38 (including 22a and 29a) done; `pom.xml` uses `spring-boot-starter-security-oauth2-resource-server`.
- [ ] No class in `adapter.in` imports `adapter.out` (and vice versa); the `sid` claim name comes only from `JwtClaimNames`.
- [ ] `V3` applied; `identity_session` and `identity_refresh_token` exist with their constraints.
- [ ] No signing secret committed; local runs use `CAPSULE_JWT_SIGNING_SECRET`.
- [ ] `openapi.yaml` updated to version 0.2.0 with login and bearer scheme.

**Business rules and AC**
- [ ] BRULE-AUTH-002/003 at login (variant email works; changed password fails).
- [ ] Wrong password and unknown email give byte-identical 401 bodies.
- [ ] Each login creates a separate session (BRULE-AUTH-006).
- [ ] AC-01…AC-07 backend evidence per Section 9.1.

**Security and privacy**
- [ ] Default deny: unknown routes → 401 `AUTHENTICATION_REQUIRED`.
- [ ] Signed but session-less, cross-account, wrong-issuer, wrong-key, expired and malformed tokens → 401.
- [ ] Exact 15-minute boundary verified with the test clock.
- [ ] Passwords/tokens absent from logs; only the refresh-token hash is stored.
- [ ] Unreachable database during the authority check → 503 `TEMPORARILY_UNAVAILABLE`, fail closed, fixed body, no challenge header; programming errors stay 500.

**Tests and verification**
- [ ] New unit/application tests pass (incl. `SessionAwareJwtAuthenticationConverterTest`); `SessionSchemaIT`, `LoginApiIT`, `AccessTokenSecurityIT`, `SessionAuthorityOutageIT` pass.
- [ ] Optional manual outage check (Section 8, Step 5) recorded if performed.
- [ ] All Stage 1 tests still pass (registration through the security chain).
- [ ] `clean verify` green locally; CI green on the PR; report guard updated (optional).
- [ ] Review done; `git diff --check` clean; no `.env`/`target`/secrets staged.

**Remaining external evidence**
- [ ] Mobile login, onboarding/intended-area handoff, Forgot Password entry (AC-01, AC-04).
- [ ] Client clears the previous user's content on account switch (AC-06).
- [ ] Pending/failed/uncertain presentation (AC-07); accessibility (AC-08).

Next: [US-AUTH-004 — Log out](US-AUTH-004-guide.md).
