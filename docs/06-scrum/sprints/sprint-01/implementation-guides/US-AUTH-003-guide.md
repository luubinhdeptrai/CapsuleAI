# US-AUTH-003 — Resume Access Within My Login Session: Implementation Guide

| Field | Value |
| --- | --- |
| Story | [US-AUTH-003 — Resume access within my login session](../../../stories/auth/US-AUTH-003-resume-session.md) (v0.3, Baseline Draft) |
| Parent PBI | [PBI-004](../../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions); hard dependency [PBI-002](../../../product-backlog.md#pbi-002--protect-private-user-data-from-the-first-usable-increment) |
| Implementation order | **4 of 4** — after [US-AUTH-004](US-AUTH-004-guide.md) |
| Starts from | Stage 3 (logout merged, all tests green) |
| Stage produced | **Stage 4** — the complete Sprint 1 authentication backend |
| Read first | [Shared Architecture §4.7, §5.3, §5.4](shared-architecture.md#47-refresh-rotation-and-reuse-detection-us-auth-003) |
| Status | Reference code for manual implementation. **Not compiled or executed by the author.** |

---

## 1. Story Overview

**User story.** As a returning user, I want to resume my private experience while my login session is usable, so that I can continue wardrobe use without unnecessary credential entry and recognize when I must log in again.

| Item | Description |
| --- | --- |
| User value | Continuity without retyping the password, within clear security limits. |
| Backend objective | `POST /api/v1/auth/refresh` exchanges the current refresh token for a **new** access token and a **new** refresh token in the same session. The old refresh token is consumed. A consumed token presented again revokes that session. Nothing ever extends the session beyond 30 days after login. |
| In scope (backend) | Rotation; reuse detection and affected-session revocation; absolute lifetime; refusal for logged-out sessions; competing renewals; interrupted-renewal semantics; separate sessions. |
| Out of scope | Session dashboard, all-device logout, remember-me, password-reset revocation (PBI-005); mobile retry/guidance UI. |
| Dependencies | Stage 3: `LoginSession` (with revocation), `RevocationReason`, `SessionRepository.revoke`, `IdentityTransactions`, `AccessTokenIssuer`, `RefreshTokenGenerator`, `TokenResponse`, the filter chain and test support. |
| Expected outcome | Stage 4: full lifecycle — register → login → protected use → renew/rotate → logout or reuse revocation → re-login. All tests of all stages pass. |

This story is the one the Sprint plan calls "the greatest lifecycle/concurrency risk". The design is deliberately strict: **no grace period** for a replayed token ([Sprint plan §3.3](../sprint-plan.md#33-us-auth-003--session-continuity), [Test Strategy §9.2](../../../../07-testing/test-strategy.md#92-required-authentication-and-authorization-scenarios)).

---

## 2. Business Requirements

### 2.1 Scenarios

| Scenario | Backend behaviour | Source |
| --- | --- | --- |
| **Return with usable access** | Access token still valid → protected requests work (Stage 2 behaviour). | AC-01, FR-AUTH-005 |
| **Renew at access expiry** | Expired access token rejected; refresh with a current token → new tokens for the **same** account and session. | AC-02, FR-AUTH-006, [UC-002 A2](../../../../03-requirements/use-cases/UC-002-log-in.md#7-alternative-flows) |
| **Rotation** | Old refresh token consumed; replacements issued; old one can't renew again. | AC-03, FR-AUTH-013 |
| **Absolute maximum** | At 30 days after login, renewal is refused and protected use stops, whatever happened before. Nothing deleted. | AC-04, FR-AUTH-010 |
| **Reuse** | A consumed token presented again → revoke **that** session (also kills its newest token); other sessions untouched. | AC-05, FR-AUTH-014, BRULE-AUTH-006 |
| **Unusable session** | Expired/revoked session → 401; a still-unexpired access token of that session is also refused; login restores the same account. | AC-06, ERR-AUTH-003, [UC-002 E2](../../../../03-requirements/use-cases/UC-002-log-in.md#8-exception--failure-flows) |
| **Separate sessions** | Renewal on device A never touches device B. | AC-07 |
| **Competing renewals** | Same token twice at once → exactly one rotation; the other is reuse → session revoked. | AC-03/05, [Test Strategy §8.2](../../../../07-testing/test-strategy.md#82-what-database-tests-must-establish) |
| **Interrupted renewal** | Response lost after commit → retry presents a consumed token → session revoked → log in again. Response lost before commit → nothing changed → retry succeeds. | AC-08, COM-002 |
| **Logout then renew** | Refused; the logout reason is kept. | US-AUTH-004 AC-01 |

### 2.2 Requirements traceability

| Requirement | Short meaning | Relevant AC | Source |
| --- | --- | --- | --- |
| FR-AUTH-005 | Returning access within the 30-day session. | AC-01, AC-02 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-006 | Expired access renewed only via usable Refresh Session or login. | AC-02, AC-06 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-008/009 | Usable session/account context for protected use. | AC-06, AC-07 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-010 | 15 min / 30 days; rotation never extends. | AC-02, AC-04 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-013 | Consume current token, issue new access + refresh. | AC-03 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-014 | Rotated-token reuse revokes the affected session. | AC-05 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| DATA-AUTH-005 | Separate sessions with current/consumed refresh validity and revocation. | AC-03, AC-05, AC-07 | [SRS §4.2](../../../../03-requirements/SRS.md#42-account--profile-data) |
| ERR-AUTH-003 | Unusable/revoked/reuse → reauthenticate; inaccessible ≠ empty. | AC-04, AC-06 | [SRS §9.1](../../../../03-requirements/SRS.md#91-access-and-recovery) |
| COM-002 | Interrupted ≠ completed; no false refresh claim. | AC-08 | [SRS §5.4](../../../../03-requirements/SRS.md#54-communication-interfaces) |
| NFR-SEC-002/003 | Session isolation; secrets out of logs. | AC-07 | [SRS §6.2](../../../../03-requirements/SRS.md#62-security) |
| NFR-REL-003 | Authority survives restart. | AC-01 | [SRS §6.4](../../../../03-requirements/SRS.md#64-reliability) |
| ADA-008 | PostgreSQL coordinates single-success consumption and revocation. | AC-03, AC-05 | [ADA-008](../../../../04-architecture/selected-architecture-decisions.md#ada-008--session-refresh-and-reset-authority), [ADD §6.8](../../../../04-architecture/ADD.md#68-qa-sec-02--session-and-recovery-boundaries) |

---

## 3. Business Rules

| Rule | Simple meaning | Example | Backend implication |
| --- | --- | --- | --- |
| [BRULE-AUTH-006](../../../../03-requirements/business-rules.md#brule-auth-006--separate-sessions-and-revocation) — rotation | Each successful refresh consumes the token and returns new ones. | R1 → (R2, new access). R1 is now dead. | `consumed_at` set on R1; R2 inserted; one current token per session. |
| BRULE-AUTH-006 — reuse | Presenting a consumed token revokes the affected session. | Attacker replays stolen R1 after the user rotated → session revoked; user's R2 also stops working. | `revocation_reason = 'REFRESH_TOKEN_REUSE'`. |
| BRULE-AUTH-006 — maximum | Rotation cannot extend the session maximum. | Login 5 Jan 08:00; renewals on day 10, 20, 29 → session still ends 4 Feb 08:00. | `expires_at` never updated; access `exp` capped at `expires_at`. |
| BRULE-AUTH-006 — separate sessions | Sessions are independent. | Reuse on the phone does not sign out the tablet. | Revocation by session id only. |
| [BRULE-AUTH-001](../../../../03-requirements/business-rules.md#brule-auth-001--authorized-personal-operations) | Unavailable access ≠ empty wardrobe. | Refused renewal → 401 `AUTHENTICATION_REQUIRED`, never `200 []`. | Same code as the filter's 401. |

---

## 4. Acceptance Criteria

| AC | Summary | Backend effect | Verification type |
| --- | --- | --- | --- |
| [AC-01](../../../stories/auth/US-AUTH-003-resume-session.md#ac-01--return-with-usable-access) Return with usable access | Valid token + usable session → correct private experience. | Stage 2 behaviour, durable in PostgreSQL. | **Backend**; **External** (intended area). |
| [AC-02](../../../stories/auth/US-AUTH-003-resume-session.md#ac-02--renew-at-access-expiry) Renew at access expiry | Expired token alone grants nothing; renewal restores same account. | Filter rejects; `/refresh` renews. | **Backend**. |
| [AC-03](../../../stories/auth/US-AUTH-003-resume-session.md#ac-03--rotate-renewal-credentials) Rotate | Old consumed; replacements issued; old can't renew again. | Locked consume + insert. | **Backend** (incl. concurrency). |
| [AC-04](../../../stories/auth/US-AUTH-003-resume-session.md#ac-04--enforce-the-absolute-session-maximum) Absolute maximum | At 30 days renewal fails and protected use pauses; nothing deleted. | `now ≥ expires_at` → reject; access `exp` capped. | **Backend**; **External** (explanation). |
| [AC-05](../../../stories/auth/US-AUTH-003-resume-session.md#ac-05--handle-rotated-token-reuse) Reuse | Reuse revokes the affected session including newer token; other session unaffected. | `REVOKE_SESSION_FOR_REUSE`. | **Backend**. |
| [AC-06](../../../stories/auth/US-AUTH-003-resume-session.md#ac-06--reauthenticate-after-unusable-access) Reauthenticate | Unusable session refuses even an unexpired token; login restores correct state. | Filter + renewal refuse; login creates a new session. | **Backend**; **External** (login route, intended area). |
| [AC-07](../../../stories/auth/US-AUTH-003-resume-session.md#ac-07--keep-separate-sessions-distinct) Separate sessions | Renewal stays in its session/account. | Same `sid` after renewal; other sessions untouched. | **Backend**. |
| [AC-08](../../../stories/auth/US-AUTH-003-resume-session.md#ac-08--resolve-interrupted-renewal) Interrupted renewal | No false success; no continued use on unusable authority. | Commit-then-respond; rollback on failure; strict reuse. | **Backend** (state semantics); **External** (pending/retry UI). |
| [AC-09](../../../stories/auth/US-AUTH-003-resume-session.md#ac-09--understand-renewed-access-guidance) Guidance | Accessible authentication-required state. | Stable `AUTHENTICATION_REQUIRED` code. | **External only**. |

---

## 5. Just-in-Time Detailed Design

### 5.1 Classes

| Layer | Class | Action | Responsibility |
| --- | --- | --- | --- |
| domain | `RevocationReason` | MODIFY | + `REFRESH_TOKEN_REUSE`. |
| domain | `RefreshToken` | NEW | (id, sessionId, issuedAt, consumedAt); `isConsumed()`. |
| domain | `RenewalDecision` | NEW | `ROTATE`, `REVOKE_SESSION_FOR_REUSE`, `REJECT`. |
| domain | `RefreshRotationPolicy` | NEW | Pure decision from (session, token, now). |
| ports | `LockedRefreshCredential` | NEW | Session + token returned **locked**. |
| ports | `SessionRepository` | MODIFY | + `lockByRefreshToken`, `consumeRefreshToken`, `addRefreshToken`. |
| application | `RenewSessionCommand`, `AuthenticationRequiredException` | NEW | Input; uniform refusal. |
| application | `RefreshSessionService` | NEW | Transaction: lock → decide → rotate / revoke / reject. |
| adapter.out | `JdbcSessionRepository` | MODIFY | `SELECT … FOR UPDATE OF t, s`; consume; add. |
| adapter.in.web | `RefreshRequest`, `SessionRefreshController` | NEW | `POST /refresh` → `TokenResponse` (reused). |
| adapter.in.web | `IdentityApiExceptionHandler` | MODIFY | + 401 `AUTHENTICATION_REQUIRED`. |
| configuration | `IdentitySecurityConfiguration` | MODIFY | + public `/refresh`. |
| configuration | `IdentityConfiguration` | MODIFY | + `RefreshSessionService` bean. |

### 5.2 Decision table (`RefreshRotationPolicy`)

Checked in this order:

| Session revoked? | `now ≥ expires_at`? | Token consumed? | Decision | State change |
| --- | --- | --- | --- | --- |
| Yes | — | — | `REJECT` | none (keeps the first reason, e.g. LOGOUT) |
| No | Yes | — | `REJECT` | none |
| No | No | Yes | `REVOKE_SESSION_FOR_REUSE` | session revoked, reason `REFRESH_TOKEN_REUSE` |
| No | No | No | `ROTATE` | token consumed; new current token; new access token |

Unknown token (no row) → refused, nothing changed. All refusals answer the same 401 `AUTHENTICATION_REQUIRED`; the exact reason is recorded in the database for diagnosis, not disclosed to the caller.

### 5.3 Transaction and locking

Inside one `IdentityTransactions.inTransaction(...)`:

1. `SELECT … FROM identity_refresh_token t JOIN identity_session s … WHERE t.token_hash = ? FOR UPDATE OF t, s` — locks the token row **and** its session row.
2. Decide with the policy.
3. `ROTATE`: `UPDATE … SET consumed_at` (old) → `INSERT` new token → sign access token. `REVOKE…`: `UPDATE identity_session SET revoked_at…`. `REJECT`: nothing.
4. Return a result; the template **commits**. Only after commit does the service throw `AuthenticationRequiredException` for refusals.

> **Key lesson — never throw inside a transaction whose work must persist.** A reuse revocation must be saved *and* the caller must get 401. If the code threw the exception inside the transaction, Spring would roll back and the revocation would vanish. So the transactional method returns `Optional.empty()` and the exception is thrown afterwards.

Why the locks give exactly one success: see the worked race in [Shared Architecture §5.4](shared-architecture.md#54-why-the-refresh-locking-is-correct-worked-race). Logout uses a conditional `UPDATE` on the same session row, so logout and renewal are serialized too: whichever commits first, the session ends revoked and no newly issued token is usable.

### 5.4 Interrupted renewal — what the client sees

| Where it broke | Database after the failure | Client retry with the same refresh token |
| --- | --- | --- |
| Before the request reached the server | unchanged | succeeds |
| Inside the transaction (error, crash) | rolled back, unchanged | succeeds |
| After commit, response lost | old token consumed, new token stored | **reuse → session revoked → 401**; user logs in again |

The third row is the strict consequence of FR-AUTH-014 with no grace window; a client must never present a refresh token twice. **Client contract:** perform at most one renewal at a time per session (single-flight), and replace the stored refresh token atomically with the new one before using the new access token. Record this for the mobile developer; it is external evidence for AC-08.

### 5.5 Database change

`V5__add_refresh_token_rotation.sql`:

- `identity_refresh_token.consumed_at` (NULL = current) with `consumed_at ≥ issued_at`.
- Partial unique index `ux_identity_refresh_token_current (session_id) WHERE consumed_at IS NULL` — the database itself guarantees **at most one current refresh token per session**. Rotation therefore consumes first, then inserts.
- Replaces the revocation-reason CHECK to allow `REFRESH_TOKEN_REUSE`.

Before V5 no rotation existed, so every session had exactly one unconsumed token and the new index can be created safely on existing data (verified by T6's transition test).

### 5.6 HTTP contract

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{ "refreshToken": "q8lC2v…" }
```

| Outcome | Status | Body |
| --- | --- | --- |
| Renewed | 200 | `TokenResponse` — new `accessToken`, new `refreshToken`, **unchanged** `sessionExpiresAt` |
| `refreshToken` missing/blank | 400 | `VALIDATION_FAILED`, field `refreshToken` `REQUIRED` |
| Unknown, consumed (reuse), revoked or expired session | 401 | `AUTHENTICATION_REQUIRED` |
| Database failure | 503 | `TEMPORARILY_UNAVAILABLE` (transaction rolled back) |

Public endpoint: the refresh token is the credential. An `Authorization` header (for example an expired access token) is ignored.

---

## 6. File Implementation Plan

`J` = `backend/src/main/java/com/capsuleai/identity`.

| Order | File Path | Action | Responsibility | Depends On |
| --- | --- | --- | --- | --- |
| 1 | `backend/src/main/resources/db/migration/V5__add_refresh_token_rotation.sql` | NEW | Consumption column, current-token index, reason. | V4 |
| 2 | `J/domain/RevocationReason.java` | MODIFY | + `REFRESH_TOKEN_REUSE`. | Stage 3 |
| 3 | `J/domain/RefreshToken.java` | NEW | Token model. | — |
| 4 | `J/domain/RenewalDecision.java` | NEW | Decisions. | — |
| 5 | `J/domain/RefreshRotationPolicy.java` | NEW | Decision rule. | 3, 4 |
| 6 | `J/application/ports/LockedRefreshCredential.java` | NEW | Locked pair. | 3 |
| 7 | `J/application/ports/SessionRepository.java` | MODIFY | + 3 methods. | 6 |
| 8 | `J/application/RenewSessionCommand.java` | NEW | Input. | — |
| 9 | `J/application/AuthenticationRequiredException.java` | NEW | Refusal. | — |
| 10 | `J/application/RefreshSessionService.java` | NEW | Use case. | 2–9 |
| 11 | `J/adapter/out/persistence/JdbcSessionRepository.java` | MODIFY | Locking SQL. | 1, 7 |
| 12 | `J/configuration/IdentityConfiguration.java` | MODIFY | Wiring. | 10 |
| 13 | `J/configuration/IdentitySecurityConfiguration.java` | MODIFY | Public `/refresh`. | — |
| 14 | `J/adapter/in/web/RefreshRequest.java` | NEW | Request JSON. | 8 |
| 15 | `J/adapter/in/web/SessionRefreshController.java` | NEW | Endpoint. | 10, 14 |
| 16 | `J/adapter/in/web/IdentityApiExceptionHandler.java` | MODIFY | + 401. | 9 |
| 17 | `docs/05-api/openapi.yaml` | MODIFY | + refresh. | 15 |
| — | `J/adapter/in/web/TokenResponse.java`, `J/application/SessionTokens.java`, token adapters | REUSE | Unchanged. | — |

---

## 7. Complete Production Code

### 7.1 `backend/src/main/resources/db/migration/V5__add_refresh_token_rotation.sql` — NEW

```sql
-- US-AUTH-003: refresh-token rotation and reuse detection (FR-AUTH-013, FR-AUTH-014).
-- consumed_at NULL = the session's current refresh token.
ALTER TABLE identity_refresh_token
    ADD COLUMN consumed_at TIMESTAMPTZ NULL;

ALTER TABLE identity_refresh_token
    ADD CONSTRAINT ck_identity_refresh_token_consumed_after_issue
        CHECK (consumed_at IS NULL OR consumed_at >= issued_at);

-- A login session has at most one current (unconsumed) refresh token.
CREATE UNIQUE INDEX ux_identity_refresh_token_current
    ON identity_refresh_token (session_id)
    WHERE consumed_at IS NULL;

-- Reuse detection revokes the affected session with its own reason.
ALTER TABLE identity_session
    DROP CONSTRAINT ck_identity_session_revocation_reason;

ALTER TABLE identity_session
    ADD CONSTRAINT ck_identity_session_revocation_reason
        CHECK (revocation_reason IN ('LOGOUT', 'REFRESH_TOKEN_REUSE'));
```

This is the forward-only way to change a constraint that V4 created: V4 is never edited.

### 7.2 `J/domain/RevocationReason.java` — MODIFY

```java
package com.capsuleai.identity.domain;

/** Why a login session stopped being usable. Stored by name in the database. */
public enum RevocationReason {

    /** The user ended this session (US-AUTH-004, FR-AUTH-007). */
    LOGOUT,

    /** A consumed refresh token was presented again (US-AUTH-003, FR-AUTH-014). */
    REFRESH_TOKEN_REUSE
}
```

### 7.3 `J/domain/RefreshToken.java` — NEW

```java
package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** One issued refresh token of a login session. Only its hash is ever stored. */
public record RefreshToken(UUID id, UUID sessionId, Instant issuedAt, Instant consumedAt) {

    public RefreshToken {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(issuedAt, "issuedAt");
    }

    /** Consumed by an earlier successful rotation; presenting it again is reuse. */
    public boolean isConsumed() {
        return consumedAt != null;
    }
}
```

### 7.4 `J/domain/RenewalDecision.java` — NEW

```java
package com.capsuleai.identity.domain;

/** What a renewal attempt must do (FR-AUTH-013, FR-AUTH-014, BRULE-AUTH-006). */
public enum RenewalDecision {

    /** Consume the presented token and issue replacements. */
    ROTATE,

    /** The presented token was already consumed: revoke the affected session; renew nothing. */
    REVOKE_SESSION_FOR_REUSE,

    /** The session is revoked or past its maximum lifetime: renew nothing, change nothing. */
    REJECT
}
```

### 7.5 `J/domain/RefreshRotationPolicy.java` — NEW

**Purpose.** All renewal rules in one pure function — no database, no clock reading, no Spring — so every branch is unit-testable with plain values. The order matters: an unusable session is checked **before** reuse, so replaying an old token of an already logged-out session does not overwrite the `LOGOUT` reason.

```java
package com.capsuleai.identity.domain;

import java.time.Instant;

/**
 * Decides what a renewal with a presented refresh token must do
 * (FR-AUTH-010, FR-AUTH-013, FR-AUTH-014, BRULE-AUTH-006).
 */
public final class RefreshRotationPolicy {

    private RefreshRotationPolicy() {
    }

    public static RenewalDecision decide(LoginSession session, RefreshToken presented, Instant now) {
        if (!presented.sessionId().equals(session.id())) {
            throw new IllegalArgumentException("The refresh token does not belong to this session");
        }
        if (!session.isUsableAt(now)) {
            return RenewalDecision.REJECT;
        }
        if (presented.isConsumed()) {
            return RenewalDecision.REVOKE_SESSION_FOR_REUSE;
        }
        return RenewalDecision.ROTATE;
    }
}
```

`session.isUsableAt(now)` already means "not revoked and `now < expires_at`" (from US-AUTH-004), so the 30-day maximum and logout are both honoured here without new code.

### 7.6 `J/application/ports/LockedRefreshCredential.java` — NEW

```java
package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshToken;

/**
 * A presented refresh token and its login session, both row-locked until the
 * surrounding transaction ends.
 */
public record LockedRefreshCredential(LoginSession session, RefreshToken token) {
}
```

### 7.7 `J/application/ports/SessionRepository.java` — MODIFY

Complete resulting file (final):

```java
package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Instant;
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

    /**
     * Revokes the session if it belongs to the account and is not yet revoked.
     * A single conditional update, so concurrent revocations cannot both succeed.
     *
     * @return {@code true} if this call revoked it; {@code false} if it was
     *         already revoked or is not that account's session
     */
    boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt);

    /**
     * Finds a refresh token by hash together with its session and LOCKS both rows
     * until the surrounding transaction ends, so competing renewals and logouts
     * of the same session run one after another.
     * Must be called inside {@link IdentityTransactions}.
     */
    Optional<LockedRefreshCredential> lockByRefreshToken(RefreshTokenHash hash);

    /** Marks a locked, current refresh token as consumed. */
    void consumeRefreshToken(UUID refreshTokenId, Instant consumedAt);

    /** Stores the session's new current refresh token. */
    void addRefreshToken(UUID sessionId, RefreshTokenHash hash, Instant issuedAt);
}
```

### 7.8 `J/application/RenewSessionCommand.java` — NEW

```java
package com.capsuleai.identity.application;

/** Raw renewal input. toString() masks the refresh token. */
public record RenewSessionCommand(String refreshToken) {

    @Override
    public String toString() {
        return "RenewSessionCommand[refreshToken=[PROTECTED]]";
    }
}
```

### 7.9 `J/application/AuthenticationRequiredException.java` — NEW

```java
package com.capsuleai.identity.application;

/**
 * The login session cannot be renewed: unknown token, revoked or expired session,
 * or reuse detected. The caller must authenticate again. Deliberately does not
 * say which case applied.
 */
public final class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException() {
        super("Authentication is required again");
    }
}
```

### 7.10 `J/application/RefreshSessionService.java` — NEW

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.IdentityTransactions;
import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshRotationPolicy;
import com.capsuleai.identity.domain.RefreshToken;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.domain.SessionPolicy;
import com.capsuleai.identity.domain.ValueProblem;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * US-AUTH-003 — renews access with a rotating refresh token inside the login
 * session's absolute 30-day lifetime. A reused (consumed) token revokes the
 * affected session (FR-AUTH-005/006/010/013/014, BRULE-AUTH-006).
 */
public final class RefreshSessionService {

    private final SessionRepository sessions;
    private final RefreshTokenGenerator refreshTokens;
    private final AccessTokenIssuer accessTokens;
    private final IdentityTransactions transactions;
    private final Clock clock;

    public RefreshSessionService(SessionRepository sessions,
                                 RefreshTokenGenerator refreshTokens,
                                 AccessTokenIssuer accessTokens,
                                 IdentityTransactions transactions,
                                 Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.refreshTokens = Objects.requireNonNull(refreshTokens, "refreshTokens");
        this.accessTokens = Objects.requireNonNull(accessTokens, "accessTokens");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws RequestValidationException      if no refresh token was supplied
     * @throws AuthenticationRequiredException if the session cannot be renewed; a
     *         detected reuse has already been committed as a revocation when thrown
     */
    public SessionTokens renew(RenewSessionCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            throw new RequestValidationException(
                    List.of(new FieldViolation("refreshToken", ValueProblem.REQUIRED)));
        }
        RefreshTokenHash presented = RefreshTokenHash.of(command.refreshToken());
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        String replacementToken = refreshTokens.newToken();

        // The transaction commits successful rotations AND reuse revocations.
        // A refusal is signalled by an empty result, never by throwing inside the
        // transaction, because an exception would roll the revocation back.
        Optional<SessionTokens> renewed =
                transactions.inTransaction(() -> renewInsideTransaction(presented, replacementToken, now));

        return renewed.orElseThrow(AuthenticationRequiredException::new);
    }

    private Optional<SessionTokens> renewInsideTransaction(
            RefreshTokenHash presented, String replacementToken, Instant now) {
        Optional<LockedRefreshCredential> locked = sessions.lockByRefreshToken(presented);
        if (locked.isEmpty()) {
            return Optional.empty();
        }
        LoginSession session = locked.get().session();
        RefreshToken token = locked.get().token();

        return switch (RefreshRotationPolicy.decide(session, token, now)) {
            case ROTATE -> Optional.of(rotate(session, token, replacementToken, now));
            case REVOKE_SESSION_FOR_REUSE -> {
                sessions.revoke(session.id(), session.accountId(), RevocationReason.REFRESH_TOKEN_REUSE, now);
                yield Optional.empty();
            }
            case REJECT -> Optional.empty();
        };
    }

    private SessionTokens rotate(LoginSession session, RefreshToken current, String replacementToken, Instant now) {
        // Consume first: the database allows only one current token per session.
        sessions.consumeRefreshToken(current.id(), now);
        sessions.addRefreshToken(session.id(), RefreshTokenHash.of(replacementToken), now);

        Instant accessTokenExpiresAt = SessionPolicy.accessTokenExpiresAt(now, session.expiresAt());
        String accessToken = accessTokens.issue(session.accountId(), session.id(), now, accessTokenExpiresAt);

        return new SessionTokens(
                session.accountId(), accessToken, accessTokenExpiresAt, replacementToken, session.expiresAt());
    }
}
```

Points to understand:

- The replacement token is generated **before** the transaction (cheap, no database), so the lock is held for the shortest possible time.
- The access token is signed **inside** the transaction: if signing failed, the consumption would roll back and the client could simply retry.
- `sessionExpiresAt` comes from the stored session — it is impossible for this code to extend it.
- The `switch` is an exhaustive switch **expression** over the enum: adding a new `RenewalDecision` constant later makes this code fail to compile until it is handled.

### 7.11 `J/adapter/out/persistence/JdbcSessionRepository.java` — MODIFY

Complete resulting file (final).

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshToken;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
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
                        SELECT id, account_id, established_at, expires_at, revoked_at, revocation_reason
                        FROM identity_session
                        WHERE id = :id
                        """)
                .param("id", sessionId)
                .query(JdbcSessionRepository::mapSession)
                .optional();
    }

    @Override
    public boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt) {
        int updated = jdbc.sql("""
                        UPDATE identity_session
                        SET revoked_at = :revokedAt, revocation_reason = :reason
                        WHERE id = :id AND account_id = :accountId AND revoked_at IS NULL
                        """)
                .param("revokedAt", JdbcTime.toDatabase(revokedAt))
                .param("reason", reason.name())
                .param("id", sessionId)
                .param("accountId", accountId)
                .update();
        return updated == 1;
    }

    @Override
    public Optional<LockedRefreshCredential> lockByRefreshToken(RefreshTokenHash hash) {
        return jdbc.sql("""
                        SELECT t.id          AS token_id,
                               t.issued_at   AS token_issued_at,
                               t.consumed_at AS token_consumed_at,
                               s.id, s.account_id, s.established_at, s.expires_at,
                               s.revoked_at, s.revocation_reason
                        FROM identity_refresh_token t
                        JOIN identity_session s ON s.id = t.session_id
                        WHERE t.token_hash = :tokenHash
                        FOR UPDATE OF t, s
                        """)
                .param("tokenHash", hash.value())
                .query((row, rowNumber) -> new LockedRefreshCredential(
                        mapSession(row, rowNumber),
                        new RefreshToken(
                                row.getObject("token_id", UUID.class),
                                row.getObject("id", UUID.class),
                                JdbcTime.fromDatabase(row, "token_issued_at"),
                                JdbcTime.fromDatabase(row, "token_consumed_at"))))
                .optional();
    }

    @Override
    public void consumeRefreshToken(UUID refreshTokenId, Instant consumedAt) {
        int updated = jdbc.sql("""
                        UPDATE identity_refresh_token
                        SET consumed_at = :consumedAt
                        WHERE id = :id AND consumed_at IS NULL
                        """)
                .param("consumedAt", JdbcTime.toDatabase(consumedAt))
                .param("id", refreshTokenId)
                .update();
        if (updated != 1) {
            throw new IllegalStateException(
                    "Refresh token " + refreshTokenId + " was not current; lock it with lockByRefreshToken first");
        }
    }

    @Override
    public void addRefreshToken(UUID sessionId, RefreshTokenHash hash, Instant issuedAt) {
        insertRefreshToken(sessionId, hash, issuedAt);
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
        String reason = row.getString("revocation_reason");
        return new LoginSession(
                row.getObject("id", UUID.class),
                row.getObject("account_id", UUID.class),
                JdbcTime.fromDatabase(row, "established_at"),
                JdbcTime.fromDatabase(row, "expires_at"),
                JdbcTime.fromDatabase(row, "revoked_at"),
                reason == null ? null : RevocationReason.valueOf(reason));
    }
}
```

> **Concept — `SELECT … FOR UPDATE` in READ COMMITTED.** `FOR UPDATE OF t, s` takes a row lock on the matching token row and its session row. A second transaction running the same query **waits** at that line. When the first commits, PostgreSQL re-reads the locked rows and gives the waiting transaction their **new committed values** — so it sees `consumed_at` already set. The lock only lasts as long as the transaction, which is why the port insists on `IdentityTransactions`; outside a transaction each statement autocommits and the lock disappears immediately.

### 7.12 `J/configuration/IdentityConfiguration.java` — MODIFY

Complete resulting file (final):

```java
package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.out.persistence.JdbcAccountRepository;
import com.capsuleai.identity.adapter.out.persistence.JdbcSessionRepository;
import com.capsuleai.identity.adapter.out.persistence.SpringIdentityTransactions;
import com.capsuleai.identity.adapter.out.security.Pbkdf2PasswordHasher;
import com.capsuleai.identity.application.LogInService;
import com.capsuleai.identity.application.LogOutService;
import com.capsuleai.identity.application.RefreshSessionService;
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

    @Bean
    LogOutService logOutService(SessionRepository sessions, Clock clock) {
        return new LogOutService(sessions, clock);
    }

    @Bean
    RefreshSessionService refreshSessionService(SessionRepository sessions,
                                                RefreshTokenGenerator refreshTokens,
                                                AccessTokenIssuer accessTokens,
                                                IdentityTransactions transactions,
                                                Clock clock) {
        return new RefreshSessionService(sessions, refreshTokens, accessTokens, transactions, clock);
    }
}
```

### 7.13 `J/configuration/IdentitySecurityConfiguration.java` — MODIFY

Complete resulting file (final). The only change is the third public endpoint.

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

    /**
     * Operations anyone may call. Renewal is public because the refresh token in
     * the body is its credential; requiring a still-valid access token would make
     * renewal at access expiry impossible (Sprint plan §4).
     */
    static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/registrations"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/login"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/refresh"));

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

### 7.14 `J/adapter/in/web/RefreshRequest.java` — NEW

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RenewSessionCommand;

/** JSON body of POST /api/v1/auth/refresh. toString() masks the token. */
public record RefreshRequest(String refreshToken) {

    RenewSessionCommand toCommand() {
        return new RenewSessionCommand(refreshToken);
    }

    @Override
    public String toString() {
        return "RefreshRequest[refreshToken=[PROTECTED]]";
    }
}
```

### 7.15 `J/adapter/in/web/SessionRefreshController.java` — NEW

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RefreshSessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-003 — public renewal endpoint; the refresh token is the credential. */
@RestController
@RequestMapping("/api/v1/auth")
public class SessionRefreshController {

    private final RefreshSessionService refreshSessionService;

    public SessionRefreshController(RefreshSessionService refreshSessionService) {
        this.refreshSessionService = refreshSessionService;
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return TokenResponse.from(refreshSessionService.renew(request.toCommand()));
    }
}
```

### 7.16 `J/adapter/in/web/IdentityApiExceptionHandler.java` — MODIFY

Complete resulting file (final; adds `handleAuthenticationRequired`):

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.AuthenticationRequiredException;
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

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationRequired(AuthenticationRequiredException exception) {
        return respond(problem(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED",
                "Your session can no longer be renewed. Log in again to continue."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "The request body is missing or is not valid JSON."));
    }

    @ExceptionHandler(DataAccessException.class)
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

### 7.17 `docs/05-api/openapi.yaml` — MODIFY

1. Change `info.version` from `0.3.0` to `0.4.0`.
2. Add under `paths:` after the `/api/v1/auth/logout:` block:

```yaml
  /api/v1/auth/refresh:
    post:
      tags: [Identity]
      operationId: refreshSession
      summary: Renew access with the refresh token (US-AUTH-003)
      description: |
        Consumes the presented refresh token and returns a new access token and a
        new refresh token for the same login session. sessionExpiresAt never
        changes. Presenting an already consumed refresh token revokes that login
        session (reuse detection). There is no grace period: the client must
        replace its stored refresh token after every successful renewal and must
        not run two renewals at once. An Authorization header, if sent, is ignored.
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshRequest'
      responses:
        '200':
          description: Renewed.
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TokenResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '503':
          $ref: '#/components/responses/Unavailable'
```

3. Add under `components.schemas:` (after `LoginRequest`):

```yaml
    RefreshRequest:
      type: object
      required: [refreshToken]
      properties:
        refreshToken:
          type: string
          description: The current refresh token from the latest login or renewal.
```

---

## 8. Manual Implementation Walkthrough

Branch: `feature/US-AUTH-003-resume-session` from `main` after US-AUTH-004 is merged.

### Step 1 — Migration and domain (files 1–5)

Create V5, extend `RevocationReason`, add `RefreshToken`, `RenewalDecision`, `RefreshRotationPolicy`. Write `RefreshRotationPolicyTest` (Section 9, T2) right away if you like — it needs nothing else and gives fast feedback on the decision table.

### Step 2 — Ports and application (files 6–10)

*Check:* `.\mvnw.cmd -q compile` fails because `JdbcSessionRepository` lacks the three new methods. Expected.

Execution of a successful renewal:

```text
POST /api/v1/auth/refresh {"refreshToken":"R1"}            (public; no Authorization needed)
 └─ RefreshSessionService.renew
     ├─ hash = sha256("R1"); now = 08:20:00Z; replacement = "R2"
     └─ BEGIN
         ├─ SELECT … t JOIN s … WHERE t.token_hash = hash FOR UPDATE OF t, s   → locks R1 row + session S
         ├─ decide(S usable, R1 current) → ROTATE
         ├─ UPDATE identity_refresh_token SET consumed_at = 08:20:00 WHERE id = R1 AND consumed_at IS NULL
         ├─ INSERT identity_refresh_token (S, sha256("R2"), issued 08:20:00)
         ├─ sign JWT{sub=A, sid=S, exp=08:35:00}
         └─ COMMIT
 └─ 200 {accessToken, refreshToken:"R2", sessionExpiresAt:"2026-02-04T08:00:00Z"}
```

And a replay of R1 a minute later:

```text
 BEGIN; SELECT … FOR UPDATE → R1 consumed, S usable → REVOKE_SESSION_FOR_REUSE
        UPDATE identity_session SET revoked_at, reason='REFRESH_TOKEN_REUSE'; COMMIT
 └─ throw AuthenticationRequiredException → 401 AUTHENTICATION_REQUIRED
 Later: R2 → session revoked → REJECT → 401;   access token of S → filter → 401
```

### Step 3 — Adapter, wiring, security, web, contract (files 11–17)

Replace `JdbcSessionRepository`, `IdentityConfiguration`, `IdentitySecurityConfiguration`, `IdentityApiExceptionHandler`; create the request and controller; edit `openapi.yaml`.

*Common mistakes:*

- Throwing `AuthenticationRequiredException` **inside** the transaction lambda → the reuse revocation is rolled back (the reuse test catches this).
- Inserting the new token **before** consuming the old one → `ux_identity_refresh_token_current` violation.
- Forgetting `OF t, s` or the transaction → competing renewals can both succeed (the concurrency test catches this).
- Updating `expires_at` during rotation → violates FR-AUTH-010 (the maximum test catches this).
- Adding a "grace window" for recently consumed tokens → forbidden by the Test Strategy.

### Step 4 — Manual check

```powershell
$base = 'http://127.0.0.1:8080/api/v1/auth'
function Send-Json($uri, $object) {
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($object | ConvertTo-Json))
    Invoke-RestMethod -Method Post -Uri $uri -ContentType 'application/json; charset=utf-8' -Body $bytes
}
$login = Send-Json "$base/login" @{ email = 'person@example.com'; password = 'correct horse battery staple' }

$renewed = Send-Json "$base/refresh" @{ refreshToken = $login.refreshToken }
$renewed.sessionExpiresAt -eq $login.sessionExpiresAt      # True: the maximum never moves

# Replay the consumed token -> 401, and the session is now revoked
try { Send-Json "$base/refresh" @{ refreshToken = $login.refreshToken } } catch { $_.ErrorDetails.Message }
try { Send-Json "$base/refresh" @{ refreshToken = $renewed.refreshToken } } catch { $_.ErrorDetails.Message }
```

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT s.id, s.revocation_reason, t.issued_at, t.consumed_at FROM identity_session s JOIN identity_refresh_token t ON t.session_id = s.id ORDER BY t.issued_at;'
```

**Restart durability (manual, NFR-REL-003).** Log in, stop the application with Ctrl+C, start it again with the **same** `CAPSULE_JWT_SIGNING_SECRET`, then renew with the refresh token: it succeeds, because the session lives only in PostgreSQL. Log out, restart again, and confirm the old access token is still refused. Record the result in the PR. (The automated `AccessTokenSecurityIT.sessionAuthorityIsReadFromPostgreSqlOnEveryRequest` shows there is no in-memory authority to lose.)

---

## 9. Complete Automated Test Code

Paths relative to `backend/src/test/java/com/capsuleai/`.

| # | File | Kind | Runner |
| --- | --- | --- | --- |
| T1 | `identity/application/InMemorySessionRepository.java` | Fake — **MODIFY** (final) | — |
| T2 | `identity/domain/RefreshRotationPolicyTest.java` | Domain | Surefire |
| T3 | `identity/application/RefreshSessionServiceTest.java` | Application | Surefire |
| T4 | `identity/adapter/in/web/SessionRefreshApiIT.java` | HTTP + security + PostgreSQL | Failsafe |
| T5 | `identity/application/ConcurrentRefreshIT.java` | Concurrency + PostgreSQL | Failsafe |
| T6 | `identity/adapter/out/persistence/RefreshTokenRotationSchemaIT.java` | Migration / integrity | Failsafe |

### T1 — `identity/application/InMemorySessionRepository.java` — MODIFY (final)

Stores `RefreshToken` records so the fake can model consumption. Earlier helper methods (`all`, `sessionIdForRefreshToken`) keep their signatures, so `LogInServiceTest`, `SessionAuthorityServiceTest` and `LogOutServiceTest` compile unchanged. It cannot model row locks — concurrency is proven only by T5.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.LockedRefreshCredential;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshToken;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link SessionRepository} for application tests (no real locking). */
final class InMemorySessionRepository implements SessionRepository {

    private final Map<UUID, LoginSession> sessionsById = new LinkedHashMap<>();
    private final Map<RefreshTokenHash, RefreshToken> tokensByHash = new LinkedHashMap<>();

    @Override
    public synchronized void create(LoginSession session, RefreshTokenHash firstRefreshToken) {
        sessionsById.put(session.id(), session);
        tokensByHash.put(firstRefreshToken,
                new RefreshToken(UUID.randomUUID(), session.id(), session.establishedAt(), null));
    }

    @Override
    public synchronized Optional<LoginSession> findById(UUID sessionId) {
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    @Override
    public synchronized boolean revoke(UUID sessionId, UUID accountId, RevocationReason reason, Instant revokedAt) {
        LoginSession session = sessionsById.get(sessionId);
        if (session == null || !session.belongsTo(accountId) || session.isRevoked()) {
            return false;
        }
        sessionsById.put(sessionId, new LoginSession(session.id(), session.accountId(),
                session.establishedAt(), session.expiresAt(), revokedAt, reason));
        return true;
    }

    @Override
    public synchronized Optional<LockedRefreshCredential> lockByRefreshToken(RefreshTokenHash hash) {
        RefreshToken token = tokensByHash.get(hash);
        if (token == null) {
            return Optional.empty();
        }
        return Optional.of(new LockedRefreshCredential(sessionsById.get(token.sessionId()), token));
    }

    @Override
    public synchronized void consumeRefreshToken(UUID refreshTokenId, Instant consumedAt) {
        for (Map.Entry<RefreshTokenHash, RefreshToken> entry : tokensByHash.entrySet()) {
            RefreshToken token = entry.getValue();
            if (token.id().equals(refreshTokenId)) {
                if (token.isConsumed()) {
                    throw new IllegalStateException("Refresh token already consumed");
                }
                entry.setValue(new RefreshToken(token.id(), token.sessionId(), token.issuedAt(), consumedAt));
                return;
            }
        }
        throw new IllegalStateException("Unknown refresh token");
    }

    @Override
    public synchronized void addRefreshToken(UUID sessionId, RefreshTokenHash hash, Instant issuedAt) {
        tokensByHash.put(hash, new RefreshToken(UUID.randomUUID(), sessionId, issuedAt, null));
    }

    synchronized List<LoginSession> all() {
        return List.copyOf(sessionsById.values());
    }

    synchronized Optional<UUID> sessionIdForRefreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(tokensByHash.get(hash)).map(RefreshToken::sessionId);
    }

    synchronized Optional<RefreshToken> refreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(tokensByHash.get(hash));
    }
}
```

### T2 — `identity/domain/RefreshRotationPolicyTest.java`

**Covers:** the full decision table of Section 5.2 including the 30-day boundary (one second before / at / after), reuse, revoked and expired sessions. Expected results come from the rules, not from calling production code twice.

```java
package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static com.capsuleai.identity.domain.RenewalDecision.REJECT;
import static com.capsuleai.identity.domain.RenewalDecision.REVOKE_SESSION_FOR_REUSE;
import static com.capsuleai.identity.domain.RenewalDecision.ROTATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshRotationPolicyTest {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final LoginSession session = LoginSession.establish(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED);
    private final RefreshToken current = new RefreshToken(UUID.randomUUID(), session.id(), ESTABLISHED, null);
    private final RefreshToken consumed =
            new RefreshToken(UUID.randomUUID(), session.id(), ESTABLISHED, ESTABLISHED.plusSeconds(60));

    @Test
    void currentTokenOfAUsableSessionIsRotated() {
        assertThat(decide(session, current, ESTABLISHED.plus(Duration.ofDays(1)))).isEqualTo(ROTATE);
    }

    @Test
    void rotationIsAllowedUntilOneSecondBeforeTheSessionMaximum() {
        assertThat(decide(session, current, session.expiresAt().minusSeconds(1))).isEqualTo(ROTATE);
    }

    @Test
    void renewalIsRejectedAtAndAfterTheSessionMaximum() {
        assertThat(decide(session, current, session.expiresAt())).isEqualTo(REJECT);
        assertThat(decide(session, current, session.expiresAt().plusSeconds(1))).isEqualTo(REJECT);
    }

    @Test
    void consumedTokenOfAUsableSessionTriggersRevocation() {
        assertThat(decide(session, consumed, ESTABLISHED.plus(Duration.ofHours(1))))
                .isEqualTo(REVOKE_SESSION_FOR_REUSE);
    }

    @Test
    void revokedSessionIsNeverRenewedOrRevokedAgain() {
        LoginSession loggedOut = new LoginSession(session.id(), ACCOUNT_ID, ESTABLISHED, session.expiresAt(),
                ESTABLISHED.plusSeconds(120), RevocationReason.LOGOUT);

        assertThat(decide(loggedOut, current, ESTABLISHED.plusSeconds(180))).isEqualTo(REJECT);
        assertThat(decide(loggedOut, consumed, ESTABLISHED.plusSeconds(180))).isEqualTo(REJECT);
    }

    @Test
    void expiredSessionIsRejectedEvenForAConsumedToken() {
        assertThat(decide(session, consumed, session.expiresAt())).isEqualTo(REJECT);
    }

    @Test
    void tokenFromAnotherSessionIsAProgrammingError() {
        RefreshToken foreign = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), ESTABLISHED, null);

        assertThatThrownBy(() -> RefreshRotationPolicy.decide(session, foreign, ESTABLISHED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static RenewalDecision decide(LoginSession candidate, RefreshToken token, Instant now) {
        return RefreshRotationPolicy.decide(candidate, token, now);
    }
}
```

### T3 — `identity/application/RefreshSessionServiceTest.java`

**Covers:** AC-02/03 (rotation, same session/account), AC-04 (maximum never extended; capped access expiry; refusal at maximum changes nothing), AC-05 (reuse revokes, newer token dead), AC-07 (other session unaffected), US-AUTH-004 AC-01 (logged-out session not renewable; reason kept). **Does not prove:** locking/concurrency or HTTP.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.domain.ValueProblem;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshSessionServiceTest {

    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final SequenceRefreshTokenGenerator refreshTokens = new SequenceRefreshTokenGenerator();
    private final FakeAccessTokenIssuer accessTokens = new FakeAccessTokenIssuer();
    private final ImmediateTransactions transactions = new ImmediateTransactions();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final RefreshSessionService service =
            new RefreshSessionService(sessions, refreshTokens, accessTokens, transactions, clock);
    private final UUID accountId = UUID.randomUUID();

    private record StoredSession(LoginSession session, String refreshToken) {
    }

    @Test
    void renewalRotatesTheRefreshTokenAndKeepsTheSessionMaximum() {
        StoredSession stored = loggedIn();
        clock.advance(Duration.ofMinutes(20));

        SessionTokens renewed = renew(stored.refreshToken());

        assertThat(renewed.accountId()).isEqualTo(accountId);
        assertThat(renewed.refreshToken()).isNotEqualTo(stored.refreshToken());
        assertThat(renewed.sessionExpiresAt()).isEqualTo(stored.session().expiresAt());
        assertThat(renewed.accessTokenExpiresAt()).isEqualTo(clock.instant().plus(Duration.ofMinutes(15)));
        assertThat(sessions.refreshToken(RefreshTokenHash.of(stored.refreshToken())))
                .hasValueSatisfying(old -> assertThat(old.consumedAt()).isEqualTo(clock.instant()));
        assertThat(sessions.refreshToken(RefreshTokenHash.of(renewed.refreshToken())))
                .hasValueSatisfying(replacement -> {
                    assertThat(replacement.sessionId()).isEqualTo(stored.session().id());
                    assertThat(replacement.isConsumed()).isFalse();
                });
        assertThat(accessTokens.issued()).singleElement()
                .satisfies(issued -> assertThat(issued.sessionId()).isEqualTo(stored.session().id()));
    }

    @Test
    void consumedTokenReuseRevokesTheSessionIncludingItsNewerToken() {
        StoredSession stored = loggedIn();
        SessionTokens renewed = renew(stored.refreshToken());

        assertThatThrownBy(() -> renew(stored.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().revocationReason())
                .isEqualTo(RevocationReason.REFRESH_TOKEN_REUSE);
        assertThatThrownBy(() -> renew(renewed.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void reuseRevokesOnlyTheAffectedSession() {
        StoredSession deviceA = loggedIn();
        StoredSession deviceB = loggedIn();
        renew(deviceA.refreshToken());

        assertThatThrownBy(() -> renew(deviceA.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(deviceB.session().id()).orElseThrow().isRevoked()).isFalse();
        assertThat(renew(deviceB.refreshToken()).accountId()).isEqualTo(accountId);
    }

    @Test
    void unknownRefreshTokenChangesNothing() {
        StoredSession stored = loggedIn();

        assertThatThrownBy(() -> renew("never-issued"))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().isRevoked()).isFalse();
    }

    @Test
    void sessionCannotBeRenewedAtItsMaximumLifetimeAndNothingChanges() {
        StoredSession stored = loggedIn();
        clock.setInstant(stored.session().expiresAt());

        assertThatThrownBy(() -> renew(stored.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().isRevoked()).isFalse();
        assertThat(sessions.refreshToken(RefreshTokenHash.of(stored.refreshToken())).orElseThrow().isConsumed())
                .isFalse();
    }

    @Test
    void renewalJustBeforeTheMaximumCapsTheAccessTokenAtTheSessionEnd() {
        StoredSession stored = loggedIn();
        clock.setInstant(stored.session().expiresAt().minus(Duration.ofMinutes(5)));

        SessionTokens renewed = renew(stored.refreshToken());

        assertThat(renewed.accessTokenExpiresAt()).isEqualTo(stored.session().expiresAt());
    }

    @Test
    void rotationsNeverExtendTheSessionMaximum() {
        StoredSession stored = loggedIn();
        String token = stored.refreshToken();
        for (int day : new int[] {9, 18, 27}) {
            clock.setInstant(stored.session().establishedAt().plus(Duration.ofDays(day)));
            SessionTokens renewed = renew(token);
            assertThat(renewed.sessionExpiresAt()).isEqualTo(stored.session().expiresAt());
            token = renewed.refreshToken();
        }

        clock.setInstant(stored.session().expiresAt());
        String lastToken = token;
        assertThatThrownBy(() -> renew(lastToken)).isInstanceOf(AuthenticationRequiredException.class);
    }

    @Test
    void loggedOutSessionCannotBeRenewedAndKeepsItsLogoutReason() {
        StoredSession stored = loggedIn();
        sessions.revoke(stored.session().id(), accountId, RevocationReason.LOGOUT, clock.instant());

        assertThatThrownBy(() -> renew(stored.refreshToken()))
                .isInstanceOf(AuthenticationRequiredException.class);

        assertThat(sessions.findById(stored.session().id()).orElseThrow().revocationReason())
                .isEqualTo(RevocationReason.LOGOUT);
    }

    @Test
    void missingRefreshTokenIsAValidationError() {
        assertThatThrownBy(() -> renew(" "))
                .isInstanceOfSatisfying(RequestValidationException.class, rejected ->
                        assertThat(rejected.violations())
                                .containsExactly(new FieldViolation("refreshToken", ValueProblem.REQUIRED)));
    }

    private StoredSession loggedIn() {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), accountId, clock.instant());
        String token = "initial-" + session.id();
        sessions.create(session, RefreshTokenHash.of(token));
        return new StoredSession(session, token);
    }

    private SessionTokens renew(String refreshToken) {
        return service.renew(new RenewSessionCommand(refreshToken));
    }
}
```

### T4 — `identity/adapter/in/web/SessionRefreshApiIT.java`

**Covers:** AC-01/02 (renew after real 15-minute expiry, same account), AC-03 (database consumption, one current token, same `sid`), AC-04 (absolute maximum across rotations; capped access expiry; nothing deleted), AC-05/AC-07 (reuse revokes A including its newest tokens; B unaffected), AC-06 (unusable session → login restores the same account), US-AUTH-004 AC-01 (logout blocks renewal), TD-17, NFR-SEC-003. **Proves:** HTTP + configured security + PostgreSQL. **Does not prove:** competing requests (T5) or mobile retry UI.

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
class SessionRefreshApiIT extends IdentityIntegrationTestSupport {

    private static final String REFRESH = "/api/v1/auth/refresh";
    private static final String LOGOUT = "/api/v1/auth/logout";

    @Test
    void expiredAccessIsRenewedForTheSameAccountWithoutCredentials() throws Exception {
        IssuedTokens login = registerAndLogIn("person@example.com");
        getProtectedProbe(login.accessToken()).andExpect(status().isOk());

        clock.advance(Duration.ofMinutes(15));
        getProtectedProbe(login.accessToken()).andExpect(status().isUnauthorized());

        IssuedTokens renewed = renew(login.refreshToken());

        assertThat(renewed.accountId()).isEqualTo(login.accountId());
        getProtectedProbe(renewed.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(login.accountId().toString()));
        assertThat(countRows("identity_account")).isOne();
    }

    @Test
    void renewalConsumesTheOldRefreshTokenAndIssuesReplacements() throws Exception {
        IssuedTokens login = registerAndLogIn("rotate@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        clock.advance(Duration.ofMinutes(20));

        IssuedTokens renewed = tokensFrom(refresh(login.refreshToken())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessTokenExpiresAt").value("2026-01-05T08:35:00Z"))
                .andExpect(jsonPath("$.sessionExpiresAt").value("2026-02-04T08:00:00Z")));

        assertThat(renewed.refreshToken()).isNotEqualTo(login.refreshToken());
        assertThat(renewed.accessToken()).isNotEqualTo(login.accessToken());
        assertThat(isConsumed(login.refreshToken())).isTrue();
        assertThat(isConsumed(renewed.refreshToken())).isFalse();
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM identity_refresh_token
                        WHERE session_id = :id AND consumed_at IS NULL
                        """)
                .param("id", sessionId).query(Long.class).single()).isOne();
        assertThat(sessionIdOf(renewed.accessToken())).isEqualTo(sessionId);
    }

    @Test
    void consumedTokenReuseRevokesOnlyThatSessionIncludingItsNewestTokens() throws Exception {
        registerAccount("reuse@example.com", PASSWORD);
        IssuedTokens deviceA = logIn("reuse@example.com", PASSWORD);
        IssuedTokens deviceB = logIn("reuse@example.com", PASSWORD);
        UUID sessionA = sessionIdOf(deviceA.accessToken());
        IssuedTokens renewedA = renew(deviceA.refreshToken());

        refresh(deviceA.refreshToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        assertThat(revocationReason(sessionA)).isEqualTo("REFRESH_TOKEN_REUSE");
        refresh(renewedA.refreshToken()).andExpect(status().isUnauthorized());
        getProtectedProbe(renewedA.accessToken()).andExpect(status().isUnauthorized());

        IssuedTokens renewedB = renew(deviceB.refreshToken());
        getProtectedProbe(renewedB.accessToken()).andExpect(status().isOk());
    }

    @Test
    void sessionMaximumIsAbsoluteAcrossRotations() throws Exception {
        IssuedTokens login = registerAndLogIn("maximum@example.com");
        Instant established = MutableClock.DEFAULT_START;
        Instant sessionEnd = established.plus(Duration.ofDays(30));

        String refreshToken = login.refreshToken();
        for (int day : new int[] {10, 20}) {
            clock.setInstant(established.plus(Duration.ofDays(day)));
            IssuedTokens renewed = tokensFrom(refresh(refreshToken)
                    .andExpect(jsonPath("$.sessionExpiresAt").value(sessionEnd.toString())));
            refreshToken = renewed.refreshToken();
        }

        clock.setInstant(sessionEnd.minusSeconds(1));
        IssuedTokens lastRenewal = tokensFrom(refresh(refreshToken)
                .andExpect(jsonPath("$.accessTokenExpiresAt").value(sessionEnd.toString())));
        getProtectedProbe(lastRenewal.accessToken()).andExpect(status().isOk());

        clock.setInstant(sessionEnd);
        refresh(lastRenewal.refreshToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        getProtectedProbe(lastRenewal.accessToken()).andExpect(status().isUnauthorized());
        assertThat(countRows("identity_account")).isOne();
    }

    @Test
    void loggedOutSessionCannotBeRenewed() throws Exception {
        IssuedTokens login = registerAndLogIn("logged-out@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + login.accessToken()))
                .andExpect(status().isNoContent());

        refresh(login.refreshToken()).andExpect(status().isUnauthorized());

        assertThat(revocationReason(sessionId)).isEqualTo("LOGOUT");
        assertThat(isConsumed(login.refreshToken())).isFalse();
    }

    @Test
    void loginAfterAnUnusableSessionRestoresTheSameAccount() throws Exception {
        IssuedTokens login = registerAndLogIn("again@example.com");
        renew(login.refreshToken());
        refresh(login.refreshToken()).andExpect(status().isUnauthorized());

        IssuedTokens fresh = logIn("again@example.com", PASSWORD);

        assertThat(fresh.accountId()).isEqualTo(login.accountId());
        getProtectedProbe(fresh.accessToken()).andExpect(status().isOk());
    }

    @Test
    void renewalIsPublicAndIgnoresAStaleAuthorizationHeader() throws Exception {
        IssuedTokens login = registerAndLogIn("stale@example.com");
        clock.advance(Duration.ofMinutes(16));

        tokensFrom(postJson(REFRESH, Map.of("refreshToken", login.refreshToken()), login.accessToken()));
    }

    @Test
    void unknownRefreshTokenIsRejectedWithoutRevokingAnything() throws Exception {
        registerAndLogIn("unknown-token@example.com");

        refresh("this-token-was-never-issued")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        assertThat(jdbc.sql("SELECT count(*) FROM identity_session WHERE revoked_at IS NOT NULL")
                .query(Long.class).single()).isZero();
    }

    @Test
    void missingRefreshTokenIsAValidationError() throws Exception {
        postJson(REFRESH, Map.of())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("refreshToken"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
    }

    @Test
    void tokensNeverAppearInLogs(CapturedOutput output) throws Exception {
        IssuedTokens login = registerAndLogIn("logs@example.com");
        IssuedTokens renewed = renew(login.refreshToken());
        refresh(login.refreshToken()).andExpect(status().isUnauthorized());

        assertThat(output.getAll())
                .doesNotContain(login.refreshToken())
                .doesNotContain(renewed.refreshToken())
                .doesNotContain(renewed.accessToken());
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return postJson(REFRESH, Map.of("refreshToken", refreshToken));
    }

    private IssuedTokens renew(String refreshToken) throws Exception {
        return tokensFrom(refresh(refreshToken));
    }

    private boolean isConsumed(String refreshToken) {
        return jdbc.sql("SELECT consumed_at IS NOT NULL FROM identity_refresh_token WHERE token_hash = :hash")
                .param("hash", RefreshTokenHash.of(refreshToken).value())
                .query(Boolean.class)
                .single();
    }

    private String revocationReason(UUID sessionId) {
        return jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId)
                .query(String.class)
                .single();
    }
}
```

### T5 — `identity/application/ConcurrentRefreshIT.java`

**Covers:** AC-03/AC-05 under real contention (single success, reuse revocation, no usable newer token), logout racing renewal (US-AUTH-004 AC-01 + this story), [Test Strategy §8.2](../../../../07-testing/test-strategy.md#82-what-database-tests-must-establish) "refresh single success" with independent real transactions. All competitors are released together by a latch; the assertions hold for **every** possible interleaving, so the test needs no sleeps and is not timing-dependent. **Does not prove:** a particular interleaving happened (it proves the outcome is correct whichever one did).

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConcurrentRefreshIT extends IdentityIntegrationTestSupport {

    @Autowired
    private RefreshSessionService refreshSessionService;

    @Autowired
    private LogOutService logOutService;

    @Test
    void competingRenewalsWithOneTokenAllowExactlyOneSuccessThenRevokeTheSession() throws Exception {
        IssuedTokens login = registerAndLogIn("race@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        Callable<Object> renewal = () -> refreshSessionService.renew(new RenewSessionCommand(login.refreshToken()));

        List<Object> outcomes = runConcurrently(List.of(renewal, renewal, renewal, renewal));

        List<SessionTokens> successes = outcomes.stream()
                .filter(SessionTokens.class::isInstance)
                .map(SessionTokens.class::cast)
                .toList();
        assertThat(successes).hasSize(1);
        assertThat(outcomes).filteredOn(AuthenticationRequiredException.class::isInstance).hasSize(3);
        assertThat(revocationReason(sessionId)).isEqualTo("REFRESH_TOKEN_REUSE");
        assertThatThrownBy(() -> refreshSessionService.renew(new RenewSessionCommand(successes.get(0).refreshToken())))
                .isInstanceOf(AuthenticationRequiredException.class);
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM identity_refresh_token
                        WHERE session_id = :id AND consumed_at IS NULL
                        """)
                .param("id", sessionId).query(Long.class).single()).isOne();
    }

    @Test
    void concurrentLogoutAndRenewalLeaveNoUsableAuthority() throws Exception {
        IssuedTokens login = registerAndLogIn("logout-race@example.com");
        UUID sessionId = sessionIdOf(login.accessToken());
        AuthenticatedAccount current = new AuthenticatedAccount(login.accountId(), sessionId);

        List<Object> outcomes = runConcurrently(List.<Callable<Object>>of(
                () -> logOutService.logOut(current),
                () -> refreshSessionService.renew(new RenewSessionCommand(login.refreshToken()))));

        assertThat(outcomes.get(0)).isEqualTo(LogoutOutcome.REVOKED);
        assertThat(revocationReason(sessionId)).isEqualTo("LOGOUT");
        if (outcomes.get(1) instanceof SessionTokens renewed) {
            // Renewal committed first; logout then revoked the session the new tokens belong to.
            assertThatThrownBy(() -> refreshSessionService.renew(new RenewSessionCommand(renewed.refreshToken())))
                    .isInstanceOf(AuthenticationRequiredException.class);
            getProtectedProbe(renewed.accessToken()).andExpect(status().isUnauthorized());
        } else {
            assertThat(outcomes.get(1)).isInstanceOf(AuthenticationRequiredException.class);
        }
        getProtectedProbe(login.accessToken()).andExpect(status().isUnauthorized());
    }

    /** Starts all actions together; returns each result or the RuntimeException it threw. */
    private static List<Object> runConcurrently(List<? extends Callable<?>> actions) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(actions.size());
        CountDownLatch ready = new CountDownLatch(actions.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<?> action : actions) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        return action.call();
                    } catch (RuntimeException failure) {
                        return failure;
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) {
                outcomes.add(future.get(30, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
        }
    }

    private String revocationReason(UUID sessionId) {
        return jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId)
                .query(String.class)
                .single();
    }
}
```

### T6 — `identity/adapter/out/persistence/RefreshTokenRotationSchemaIT.java`

**Covers:** V5 integrity (one current token per session; consumption not before issue; new reason value) and the **transition with existing data** from V4 (existing current token stays current; the new index then enforces the rule).

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenRotationSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final String TRANSITION_SCHEMA = "identity_transition_v5";

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    void rotationMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("5");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void aSessionHasAtMostOneCurrentRefreshToken() {
        UUID sessionId = insertSession();
        UUID first = insertRefreshToken(sessionId, RefreshTokenHash.of("first").value());

        assertThatThrownBy(() -> insertRefreshToken(sessionId, RefreshTokenHash.of("second").value()))
                .isInstanceOf(DataIntegrityViolationException.class);

        jdbc.sql("UPDATE identity_refresh_token SET consumed_at = issued_at WHERE id = :id")
                .param("id", first).update();
        insertRefreshToken(sessionId, RefreshTokenHash.of("second").value());

        assertThat(jdbc.sql("SELECT count(*) FROM identity_refresh_token WHERE session_id = :id")
                .param("id", sessionId).query(Long.class).single()).isEqualTo(2);
    }

    @Test
    void consumptionCannotPrecedeIssue() {
        UUID token = insertRefreshToken(insertSession(), RefreshTokenHash.of("early").value());

        assertThatThrownBy(() -> jdbc.sql("""
                        UPDATE identity_refresh_token
                        SET consumed_at = issued_at - INTERVAL '1 second'
                        WHERE id = :id
                        """)
                .param("id", token).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void reuseRevocationReasonIsAcceptedAndUnknownReasonsAreNot() {
        UUID sessionId = insertSession();

        assertThatThrownBy(() -> revoke(sessionId, "SOMETHING_ELSE"))
                .isInstanceOf(DataIntegrityViolationException.class);
        revoke(sessionId, "REFRESH_TOKEN_REUSE");

        assertThat(jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId).query(String.class).single()).isEqualTo("REFRESH_TOKEN_REUSE");
    }

    @Test
    void existingCurrentTokensRemainCurrentAfterTheMigration() {
        try {
            migrateTransitionSchema("4");
            UUID accountId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_account (id, email, password_hash, created_at) "
                            + "VALUES (:id, 'existing@example.com', '{test}hash', now())")
                    .param("id", accountId).update();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_session (id, account_id, established_at, expires_at) "
                            + "VALUES (:id, :accountId, now(), now() + INTERVAL '1 day')")
                    .param("id", sessionId).param("accountId", accountId).update();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_refresh_token (id, session_id, token_hash, issued_at) "
                            + "VALUES (:id, :sessionId, :hash, now())")
                    .param("id", UUID.randomUUID()).param("sessionId", sessionId)
                    .param("hash", RefreshTokenHash.of("existing").value()).update();

            migrateTransitionSchema(null);

            assertThat(jdbc.sql("SELECT consumed_at IS NULL FROM " + TRANSITION_SCHEMA
                            + ".identity_refresh_token WHERE session_id = :id")
                    .param("id", sessionId).query(Boolean.class).single()).isTrue();
            assertThatThrownBy(() -> jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA
                            + ".identity_refresh_token (id, session_id, token_hash, issued_at) "
                            + "VALUES (:id, :sessionId, :hash, now())")
                    .param("id", UUID.randomUUID()).param("sessionId", sessionId)
                    .param("hash", RefreshTokenHash.of("second-current").value()).update())
                    .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbc.sql("DROP SCHEMA IF EXISTS " + TRANSITION_SCHEMA + " CASCADE").update();
        }
    }

    /** Runs the real migration files into a scratch schema, up to {@code target} (null = latest). */
    private void migrateTransitionSchema(String target) {
        var configuration = Flyway.configure()
                .dataSource(dataSource)
                .schemas(TRANSITION_SCHEMA)
                .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        configuration.load().migrate();
    }

    private UUID insertSession() {
        UUID accountId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_account (id, email, password_hash, created_at)
                        VALUES (:id, :email, '{test}hash', now())
                        """)
                .param("id", accountId)
                .param("email", accountId + "@example.com")
                .update();
        UUID sessionId = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_session (id, account_id, established_at, expires_at)
                        VALUES (:id, :accountId, :establishedAt, :expiresAt)
                        """)
                .param("id", sessionId)
                .param("accountId", accountId)
                .param("establishedAt", ESTABLISHED.atOffset(ZoneOffset.UTC))
                .param("expiresAt", ESTABLISHED.plus(Duration.ofDays(30)).atOffset(ZoneOffset.UTC))
                .update();
        return sessionId;
    }

    private UUID insertRefreshToken(UUID sessionId, String tokenHash) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO identity_refresh_token (id, session_id, token_hash, issued_at)
                        VALUES (:id, :sessionId, :tokenHash, :issuedAt)
                        """)
                .param("id", id)
                .param("sessionId", sessionId)
                .param("tokenHash", tokenHash)
                .param("issuedAt", ESTABLISHED.atOffset(ZoneOffset.UTC))
                .update();
        return id;
    }

    private void revoke(UUID sessionId, String reason) {
        jdbc.sql("UPDATE identity_session SET revoked_at = :at, revocation_reason = :reason WHERE id = :id")
                .param("at", ESTABLISHED.plusSeconds(60).atOffset(ZoneOffset.UTC))
                .param("reason", reason)
                .param("id", sessionId)
                .update();
    }
}
```

### 9.1 Test traceability

| AC | Scenario | Test Class | Test Method | Verification Level |
| --- | --- | --- | --- | --- |
| AC-01 | Valid token + usable session works; authority durable in PostgreSQL | US-AUTH-002 `AccessTokenSecurityIT` | `usableAccessTokenReachesAProtectedOperationAsTheCorrectAccount`, `sessionAuthorityIsReadFromPostgreSqlOnEveryRequest` | Security chain + DB |
| AC-01 | Survives application restart | — | Manual restart check (Section 8, Step 4) | **Manual evidence** |
| AC-02 | Expired token refused; renewal restores same account | `SessionRefreshApiIT` | `expiredAccessIsRenewedForTheSameAccountWithoutCredentials` | HTTP + security + DB |
| AC-03 | Old consumed, replacements issued, same session | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | `renewalConsumesTheOldRefreshTokenAndIssuesReplacements` / `renewalRotatesTheRefreshTokenAndKeepsTheSessionMaximum` | HTTP + DB / Application |
| AC-03 | Exactly one success under contention | `ConcurrentRefreshIT` | `competingRenewalsWithOneTokenAllowExactlyOneSuccessThenRevokeTheSession` | PostgreSQL concurrency |
| AC-03 | One current token per session (DB) | `RefreshTokenRotationSchemaIT` | `aSessionHasAtMostOneCurrentRefreshToken` | PostgreSQL |
| AC-04 | Maximum absolute across rotations | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | `sessionMaximumIsAbsoluteAcrossRotations` / `rotationsNeverExtendTheSessionMaximum` | HTTP + DB / Application |
| AC-04 | Boundary just before / at / after | `RefreshRotationPolicyTest` | `rotationIsAllowedUntilOneSecondBeforeTheSessionMaximum`, `renewalIsRejectedAtAndAfterTheSessionMaximum` | Domain |
| AC-04 | Access expiry capped; refusal changes nothing | `RefreshSessionServiceTest` | `renewalJustBeforeTheMaximumCapsTheAccessTokenAtTheSessionEnd`, `sessionCannotBeRenewedAtItsMaximumLifetimeAndNothingChanges` | Application |
| AC-04 | Explanation, not empty wardrobe | — | — | **External** (code `AUTHENTICATION_REQUIRED` provided) |
| AC-05 | Reuse revokes session incl. newer refresh + access token | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | `consumedTokenReuseRevokesOnlyThatSessionIncludingItsNewestTokens` / `consumedTokenReuseRevokesTheSessionIncludingItsNewerToken` | HTTP + DB / Application |
| AC-05 | Other session not revoked | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | same as above / `reuseRevokesOnlyTheAffectedSession` | HTTP / Application |
| AC-05 | Decision rule | `RefreshRotationPolicyTest` | `consumedTokenOfAUsableSessionTriggersRevocation`, `revokedSessionIsNeverRenewedOrRevokedAgain`, `expiredSessionIsRejectedEvenForAConsumedToken` | Domain |
| AC-06 | Unusable session refuses unexpired token | `SessionRefreshApiIT` | `consumedTokenReuseRevokesOnlyThatSessionIncludingItsNewestTokens`, `sessionMaximumIsAbsoluteAcrossRotations` | Security chain |
| AC-06 | Login restores the same account | `SessionRefreshApiIT` | `loginAfterAnUnusableSessionRestoresTheSameAccount` | HTTP + DB |
| AC-06 | Login route / intended area | — | — | **External** |
| AC-07 | Separate sessions distinct | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | `consumedTokenReuseRevokesOnlyThatSessionIncludingItsNewestTokens`, `renewalConsumesTheOldRefreshTokenAndIssuesReplacements` / `reuseRevokesOnlyTheAffectedSession` | HTTP / Application |
| AC-08 | No false success; strict reuse on retry | `SessionRefreshApiIT` / `ConcurrentRefreshIT` | `consumedTokenReuseRevokesOnlyThatSessionIncludingItsNewestTokens` / `competingRenewalsWithOneTokenAllowExactlyOneSuccessThenRevokeTheSession` | HTTP / PostgreSQL |
| AC-08 | Unknown token changes nothing | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | `unknownRefreshTokenIsRejectedWithoutRevokingAnything` / `unknownRefreshTokenChangesNothing` | HTTP / Application |
| AC-08 | Pending/uncertain UI; single-flight client renewal | — | — | **External** |
| AC-09 | Accessible guidance | — | — | **External only** |
| 004 AC-01 | Logout blocks renewal; reason kept | `SessionRefreshApiIT` / `RefreshSessionServiceTest` | `loggedOutSessionCannotBeRenewed` / `loggedOutSessionCannotBeRenewedAndKeepsItsLogoutReason` | HTTP / Application |
| 004 AC-01 | Logout racing renewal | `ConcurrentRefreshIT` | `concurrentLogoutAndRenewalLeaveNoUsableAuthority` | PostgreSQL concurrency |
| TD-17 | Stale header ignored on renewal | `SessionRefreshApiIT` | `renewalIsPublicAndIgnoresAStaleAuthorizationHeader` | Security chain |
| NFR-SEC-003 | Tokens not logged | `SessionRefreshApiIT` | `tokensNeverAppearInLogs` | Logs |
| DoD schema | V5 integrity and transition | `RefreshTokenRotationSchemaIT` | all five | PostgreSQL migration |

---

## 10. Run and Verify

```powershell
Set-Location 'D:\CapsuleAI\backend'

.\mvnw.cmd compile
.\mvnw.cmd '-Dtest=RefreshRotationPolicyTest,RefreshSessionServiceTest' test
.\mvnw.cmd '-Dit.test=SessionRefreshApiIT,ConcurrentRefreshIT,RefreshTokenRotationSchemaIT' verify

# Full Sprint 1 regression, exactly as CI runs it
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
if ($LASTEXITCODE -ne 0) { throw 'Verification failed; read the first error above.' }

Get-ChildItem .\target\surefire-reports\*.txt | Get-Content
Get-ChildItem .\target\failsafe-reports\*.txt | Get-Content
```

**Expected:** `BUILD SUCCESS`; every test class from all four stories present with zero failures/errors/skips. Run `ConcurrentRefreshIT` several times (`'-Dit.test=ConcurrentRefreshIT' verify`) — it must pass every time; an intermittent failure means a real concurrency bug, not a flaky test.

| Symptom | Likely cause / fix |
| --- | --- |
| Reuse test: session not revoked | Exception thrown inside the transaction lambda (rollback), or `revoke` not called for `REVOKE_SESSION_FOR_REUSE`. |
| `duplicate key value violates unique constraint "ux_identity_refresh_token_current"` | Insert before consume in `rotate`. |
| Concurrency test: 2 successes | Missing `FOR UPDATE OF t, s`, or `lockByRefreshToken` called outside `inTransaction`. |
| Concurrency test hangs then times out | A transaction never ends (e.g. a lambda that blocks); check the 30 s futures and thread dumps. |
| Refresh returns 401 with a valid token while sending an `Authorization` header | `/api/v1/auth/refresh` missing from `PUBLIC_ENDPOINTS`. |
| `ck_identity_session_revocation_reason` violation on reuse | V5 not applied or constraint not replaced. |
| `sessionExpiresAt` changes after renewal | Code computes expiry from `now` instead of the stored session. |

---

## 11. CI Integration

No workflow change is required for discovery or execution. The concurrency test uses ordinary threads and the shared Testcontainers database; it runs in the existing 15-minute job (most of the time is PBKDF2 and container start-up). Recommended final guard (complete Sprint 1 `$required` block):

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
            'surefire-reports/TEST-com.capsuleai.identity.application.LogOutServiceTest.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.LogoutApiIT.xml' = 1
            'surefire-reports/TEST-com.capsuleai.identity.application.RefreshSessionServiceTest.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.SessionRefreshApiIT.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.application.ConcurrentRefreshIT.xml' = 1
          }
```

If the job ever approaches its 15-minute timeout, investigate before raising it (Docker image pulls and PBKDF2 are the usual costs); do not lower the hashing cost or skip integration tests.

---

## 12. Completion Checklist

**Production implementation**
- [ ] V5 applied: `consumed_at`, current-token partial unique index, new reason constraint.
- [ ] `RefreshRotationPolicy` implements the decision table; `RefreshSessionService` commits reuse revocations and throws only after commit.
- [ ] `lockByRefreshToken` uses `FOR UPDATE OF t, s` inside `IdentityTransactions`.
- [ ] `/api/v1/auth/refresh` public; stale `Authorization` ignored.
- [ ] `openapi.yaml` 0.4.0 with refresh and `RefreshRequest`.

**Rules and AC**
- [ ] Rotation consumes and replaces both credentials (FR-AUTH-013).
- [ ] Reuse revokes only the affected session, killing its newer tokens (FR-AUTH-014, BRULE-AUTH-006).
- [ ] 30-day maximum absolute; access `exp` capped; refusal deletes nothing (FR-AUTH-010).
- [ ] Logged-out sessions cannot renew (US-AUTH-004 AC-01).
- [ ] No grace window for replayed tokens.

**Tests and verification**
- [ ] `RefreshRotationPolicyTest`, `RefreshSessionServiceTest`, `SessionRefreshApiIT`, `ConcurrentRefreshIT`, `RefreshTokenRotationSchemaIT` pass; concurrency test passes repeatedly.
- [ ] Complete Sprint 1 regression green locally and in CI; guard updated (optional).
- [ ] Manual restart-durability check recorded in the PR.
- [ ] Review done; PR references `US-AUTH-003`, `PBI-004`.

**Remaining external evidence**
- [ ] Mobile single-flight renewal and atomic refresh-token replacement (AC-08 client contract).
- [ ] Authentication-required state distinct from an empty wardrobe; login route; intended-area restoration (AC-04, AC-06, AC-09).
- [ ] Vietnamese accessible guidance at 200% text (AC-09).

**Sprint-level wrap-up**
- [ ] All four story PRs merged with green CI; AC→evidence tables attached.
- [ ] US-PRIV-001 bounded evidence summarised per [Shared Architecture §2.5](shared-architecture.md#25-privacy-and-us-priv-001-applicability); unexercised areas listed as such.
- [ ] Sprint Review shows backend progress separately from whole-story Done (mobile evidence outstanding).
