# US-AUTH-004 — Log Out of My Current Session: Implementation Guide

| Field | Value |
| --- | --- |
| Story | [US-AUTH-004 — Log out of my current session](../../../stories/auth/US-AUTH-004-log-out.md) (v0.3, Baseline Draft) |
| Parent PBI | [PBI-004](../../../product-backlog.md#pbi-004--emailpassword-access-and-controllable-sessions); hard dependency [PBI-002](../../../product-backlog.md#pbi-002--protect-private-user-data-from-the-first-usable-increment) |
| Implementation order | **3 of 4** — after [US-AUTH-002](US-AUTH-002-guide.md); next: [US-AUTH-003](US-AUTH-003-guide.md) |
| Starts from | Stage 2 (login and security chain merged, all tests green) |
| Stage produced | **Stage 3** — registration + login + current-session logout with immediate revocation |
| Read first | [Shared Architecture §4.2 and §4.6](shared-architecture.md#42-session-state-machine) |
| Status | Reference code for manual implementation. **Not compiled or executed by the author.** |

---

## 1. Story Overview

**User story.** As an authenticated user, I want to log out of my current session, so that I can end access on this device while keeping my wardrobe and other separate sessions intact.

| Item | Description |
| --- | --- |
| User value | Deliberate control over this device's access, without losing data or signing out other devices. |
| Backend objective | `POST /api/v1/auth/logout` (authenticated) revokes **only** the session named by the access token. From that moment the session's access token — still cryptographically valid and unexpired — is rejected everywhere, and (from US-AUTH-003) its refresh token cannot renew. |
| In scope (backend) | Revocation state in PostgreSQL; protected logout endpoint; separate-session preservation; already-unusable and repeated-logout outcomes; no data deletion. |
| Out of scope | All-device logout, session-management screen, account deletion (story §8); password-reset revocation (PBI-005); client-side cache clearing. |
| Dependencies | Stage 2: `LoginSession`, `SessionRepository`, `JdbcSessionRepository`, `SessionAuthorityService`, the filter chain and the test probe. |
| Expected outcome | Stage 3: logout works; the revoked session's token fails immediately at the security filter; all earlier tests pass. |
| Relationship | US-AUTH-003 reuses `RevocationReason`, `SessionRepository.revoke` and the revocation columns for reuse detection, and must refuse to renew a logged-out session. |

**Why logout comes before renewal.** The Sprint plan orders it this way to "establish that issued authority is controllable before expanding continuity" ([Sprint plan §5](../sprint-plan.md#5-dependencies-and-recommended-sequence)). Logout needs only a session and the authority check, both of which exist after Stage 2.

---

## 2. Business Requirements

### 2.1 Scenarios

| Scenario | Backend behaviour | Source |
| --- | --- | --- |
| **Normal** — usable session, user taps Log Out | Revoke that session; 204; its access token is rejected from now on. | [UC-003 Main](../../../../03-requirements/use-cases/UC-003-log-out.md#6-main-success-scenario), FR-AUTH-007 |
| **Separate session** — same account on device B | B keeps working. | FR-AUTH-007, BRULE-AUTH-006 |
| **Alternative** — session already revoked | Request is rejected at the filter with 401 `AUTHENTICATION_REQUIRED` → client shows the signed-out state. | UC-003 A1 |
| **Alternative** — access token already expired | 401; no session is targeted. Client contract: renew first (US-AUTH-003), or treat 401 as signed out. | UC-003 A1, [A-07](shared-architecture.md#15-ambiguities-limitations-and-open-questions) |
| **Exception** — logout response lost | Retry with the same token → 401 = already signed out; no other session revoked. | UC-003 E1, COM-002, ERR-NET-001 |
| **Data** — wardrobe, profile, history | Never touched by logout. | UC-003 §10, DATA-RET-002 |
| **Security** — another account | Unaffected; previous account's content not reachable with the revoked token. | NFR-SEC-002 |

### 2.2 Requirements traceability

| Requirement | Short meaning | Relevant AC | Source |
| --- | --- | --- | --- |
| FR-AUTH-007 | Revoke current Refresh Session; end this session's access; other sessions continue. | AC-01, AC-02, AC-05 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| FR-AUTH-008/009 | Protected operations need usable session/account authority. | AC-01, AC-04 | [SRS §3.1.1](../../../../03-requirements/SRS.md#311-authentication-and-recovery-rules) |
| DATA-AUTH-005 | Sessions represent revocation state. | AC-01 | [SRS §4.2](../../../../03-requirements/SRS.md#42-account--profile-data) |
| DATA-RET-002 | Necessary account data retained while active. | AC-03 | [SRS §4.9](../../../../03-requirements/SRS.md#49-retention--deletion) |
| ERR-AUTH-003 | Revoked session → authenticate again; logout does not revoke other sessions. | AC-05 | [SRS §9.1](../../../../03-requirements/SRS.md#91-access-and-recovery) |
| COM-002 / ERR-NET-001 | Unconfirmed ≠ completed; safe retry. | AC-06 | [SRS §5.4](../../../../03-requirements/SRS.md#54-communication-interfaces), [§9.9](../../../../03-requirements/SRS.md#99-communication-and-partial-dependency-failure) |
| NFR-SEC-002 | Logout/account switch never exposes the previous user's content. | AC-04 | [SRS §6.2](../../../../03-requirements/SRS.md#62-security) |
| ADA-008 | Persistent authority beats token validity. | AC-01 | [ADA-008](../../../../04-architecture/selected-architecture-decisions.md#ada-008--session-refresh-and-reset-authority) |
| US-PRIV-001 AC-01/02 | First delivered protected operation; usable authority; ownership. | AC-01, AC-04 | [US-PRIV-001](../../../stories/privacy/US-PRIV-001-protect-first-private-operations.md#5-acceptance-criteria) |

---

## 3. Business Rules

| Rule | Simple meaning | Example | Backend implication |
| --- | --- | --- | --- |
| [BRULE-AUTH-006](../../../../03-requirements/business-rules.md#brule-auth-006--separate-sessions-and-revocation) | Logout revokes the **current** session only. | Phone logs out; tablet stays signed in. | `UPDATE … WHERE id = :sessionFromToken`. Never by account. |
| [BRULE-AUTH-001](../../../../03-requirements/business-rules.md#brule-auth-001--authorized-personal-operations) | Unavailable access is not an empty wardrobe and changes nothing. | After logout, a request with the old token gets 401, not `[]`. | Same 401 problem body as every other authentication failure. |
| [BRULE-AUTH-007](../../../../03-requirements/business-rules.md#brule-auth-007--purpose-limited-private-data-use) | Logout is not deletion. | Account row byte-identical after logout. | Logout touches only `identity_session`. |
| BRULE-AUTH-005 (future) | Completed password reset revokes **all** sessions. | — | **Not in Sprint 1.** Kept distinct: logout never revokes by account. |

---

## 4. Acceptance Criteria

| AC | Summary | Backend effect | Verification type |
| --- | --- | --- | --- |
| [AC-01](../../../stories/auth/US-AUTH-004-log-out.md#ac-01--end-the-current-session) End the current session | Revoked session can't do protected work, even with an unexpired token; can't renew. | `revoked_at` set; filter rejects; renewal rejects (US-AUTH-003). | **Backend**; **Cross-story** (renewal denial verified in US-AUTH-003); **External** (signed-out screen). |
| [AC-02](../../../stories/auth/US-AUTH-004-log-out.md#ac-02--preserve-another-separate-session) Preserve another session | B keeps working. | Revocation targets one row. | **Backend**. |
| [AC-03](../../../stories/auth/US-AUTH-004-log-out.md#ac-03--retain-accepted-personal-information) Retain personal information | Nothing deleted/altered. | Only `identity_session` updated. | **Backend** for account data; wardrobe/profile/history **not yet exercised** (not delivered). |
| [AC-04](../../../stories/auth/US-AUTH-004-log-out.md#ac-04--keep-protected-content-hidden) Keep protected content hidden | Old token reopens nothing; another account sees only its own. | 401 without content; other account unaffected. | **Backend**; **External** (back navigation, local cache). |
| [AC-05](../../../stories/auth/US-AUTH-004-log-out.md#ac-05--handle-an-already-unusable-session) Already unusable session | Signed-out outcome; no other session targeted. | 401 `AUTHENTICATION_REQUIRED`; nothing revoked. | **Backend**; **External** (message). |
| [AC-06](../../../stories/auth/US-AUTH-004-log-out.md#ac-06--resolve-unconfirmed-logout) Unconfirmed logout | Distinguish from completed; review/retry; no other session revoked. | Retry answers 401 = already signed out; idempotent end state. | **Backend** (retry semantics); **External** (unconfirmed UI, content hidden). |
| [AC-07](../../../stories/auth/US-AUTH-004-log-out.md#ac-07--operate-and-understand-logout) Accessible logout | Vietnamese, screen reader, 200%. | — | **External only**. |

---

## 5. Just-in-Time Detailed Design

### 5.1 Classes

| Layer | Class | Action | Responsibility |
| --- | --- | --- | --- |
| domain | `RevocationReason` | NEW | `LOGOUT` (US-AUTH-003 adds `REFRESH_TOKEN_REUSE`). |
| domain | `LoginSession` | MODIFY | + `revokedAt`, `revocationReason`, `isRevoked()`; `isUsableAt` = not revoked **and** before expiry. |
| application | `LogoutOutcome` | NEW | `REVOKED` / `NOTHING_TO_REVOKE`. |
| application | `LogOutService` | NEW | Revoke the principal's session at "now". |
| ports | `SessionRepository` | MODIFY | + `revoke(sessionId, accountId, reason, revokedAt)`. |
| adapter.out | `JdbcSessionRepository` | MODIFY | Conditional `UPDATE`; map new columns. |
| adapter.in.web | `LogoutController` | NEW | `POST /logout` → 204. |
| configuration | `IdentityConfiguration` | MODIFY | + `LogOutService` bean. |
| — | `SessionAuthorityService`, security adapters, `IdentitySecurityConfiguration` | REUSE | **No change**: they already call `isUsableAt`, which now includes revocation. |

### 5.2 HTTP contract

```http
POST /api/v1/auth/logout
Authorization: Bearer <access token of the session to end>
```

| Outcome | Status | Body |
| --- | --- | --- |
| Session revoked (or found already revoked by a concurrent request) | 204 | none |
| Missing/invalid/expired token, or session already unusable | 401 | `AUTHENTICATION_REQUIRED` problem (from the filter) |

The endpoint needs no permit rule: everything outside `PUBLIC_ENDPOINTS` already requires authentication.

**Client contract** (to share with the mobile developer): on 204 **or** 401, clear the stored tokens and all cached personal content and show the signed-out screen; 401 means "already signed out". On a timeout, show "logout not confirmed", keep personal content hidden, and offer retry (retry is safe and only ever targets this session). If the access token has expired, renew it first (US-AUTH-003) and then log out; if renewal is refused, the session is already unusable.

### 5.3 Revocation in the database

```sql
UPDATE identity_session
SET revoked_at = :revokedAt, revocation_reason = 'LOGOUT'
WHERE id = :sessionId AND account_id = :accountId AND revoked_at IS NULL
```

- `id = :sessionId` → only the current session (BRULE-AUTH-006).
- `account_id = :accountId` → defence in depth: even a bug that passed a foreign session id could not revoke another account's session.
- `revoked_at IS NULL` → the first revocation wins and its reason/time are preserved; a second request changes nothing.
- One statement → atomic without an explicit transaction. If a renewal (US-AUTH-003) holds the row lock, this `UPDATE` waits for it and then applies.

### 5.4 Why the access token stops working immediately

Nothing in the token changes — it is still signed and unexpired. But every request goes through `SessionAwareJwtAuthenticationConverter` → `SessionAuthorityService` → `LoginSession.isUsableAt(now)`, which now returns `false` for a revoked session. This is ADA-008 in practice: **persistent authority beats token validity**.

### 5.5 Database change

`V4__add_identity_session_revocation.sql` adds two nullable columns and three CHECKs: time and reason set together; reason in (`LOGOUT`); revocation not before establishment. Existing Stage 2 rows get `NULL` (= not revoked) and stay usable — verified by a real migration-transition test (T5).

---

## 6. File Implementation Plan

`J` = `backend/src/main/java/com/capsuleai/identity`.

| Order | File Path | Action | Responsibility | Depends On |
| --- | --- | --- | --- | --- |
| 1 | `backend/src/main/resources/db/migration/V4__add_identity_session_revocation.sql` | NEW | Revocation columns. | V3 |
| 2 | `J/domain/RevocationReason.java` | NEW | Revocation vocabulary. | — |
| 3 | `J/domain/LoginSession.java` | MODIFY | Revocation-aware usability. | 2 |
| 4 | `J/application/LogoutOutcome.java` | NEW | Outcome. | — |
| 5 | `J/application/ports/SessionRepository.java` | MODIFY | + `revoke`. | 2 |
| 6 | `J/application/LogOutService.java` | NEW | Use case. | 4, 5 |
| 7 | `J/adapter/out/persistence/JdbcSessionRepository.java` | MODIFY | SQL. | 1, 3, 5 |
| 8 | `J/configuration/IdentityConfiguration.java` | MODIFY | Wiring. | 6 |
| 9 | `J/adapter/in/web/LogoutController.java` | NEW | Endpoint. | 6 |
| 10 | `docs/05-api/openapi.yaml` | MODIFY | + logout. | 9 |
| — | `J/application/SessionAuthorityService.java`, `J/adapter/in/security/*`, `J/configuration/IdentitySecurityConfiguration.java` | REUSE | Unchanged. | — |

---

## 7. Complete Production Code

### 7.1 `backend/src/main/resources/db/migration/V4__add_identity_session_revocation.sql` — NEW

```sql
-- US-AUTH-004: current-session revocation (FR-AUTH-007, DATA-AUTH-005).
-- NULL revoked_at = not revoked. Existing sessions therefore stay usable.
ALTER TABLE identity_session
    ADD COLUMN revoked_at        TIMESTAMPTZ NULL,
    ADD COLUMN revocation_reason TEXT        NULL;

ALTER TABLE identity_session
    ADD CONSTRAINT ck_identity_session_revocation_recorded
        CHECK ((revoked_at IS NULL) = (revocation_reason IS NULL)),
    ADD CONSTRAINT ck_identity_session_revocation_reason
        CHECK (revocation_reason IN ('LOGOUT')),
    ADD CONSTRAINT ck_identity_session_revoked_after_established
        CHECK (revoked_at IS NULL OR revoked_at >= established_at);
```

A `CHECK` whose expression is `NULL` passes, so `revocation_reason IN ('LOGOUT')` accepts unrevoked rows. US-AUTH-003's V5 replaces the reason constraint to add `REFRESH_TOKEN_REUSE` — a forward migration, never an edit of V4.

### 7.2 `J/domain/RevocationReason.java` — NEW

```java
package com.capsuleai.identity.domain;

/** Why a login session stopped being usable. Stored by name in the database. */
public enum RevocationReason {

    /** The user ended this session (US-AUTH-004, FR-AUTH-007). */
    LOGOUT
}
```

### 7.3 `J/domain/LoginSession.java` — MODIFY

Complete resulting file. `establish` and `belongsTo` keep their behaviour; `isUsableAt` now also requires "not revoked".

```java
package com.capsuleai.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One login session (the SRS "Refresh Session"), created by a successful login.
 * Its maximum lifetime is fixed when it is established and never extended.
 * A revoked session never becomes usable again.
 *
 * <p>{@code revokedAt} and {@code revocationReason} are both null (not revoked)
 * or both set.
 */
public record LoginSession(
        UUID id,
        UUID accountId,
        Instant establishedAt,
        Instant expiresAt,
        Instant revokedAt,
        RevocationReason revocationReason) {

    public LoginSession {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Objects.requireNonNull(establishedAt, "establishedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!expiresAt.isAfter(establishedAt)) {
            throw new IllegalArgumentException("A session must expire after it is established");
        }
        if ((revokedAt == null) != (revocationReason == null)) {
            throw new IllegalArgumentException("revokedAt and revocationReason must be set together");
        }
    }

    public static LoginSession establish(UUID id, UUID accountId, Instant establishedAt) {
        return new LoginSession(id, accountId, establishedAt,
                establishedAt.plus(SessionPolicy.SESSION_MAXIMUM_LIFETIME), null, null);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    /** Usable = not revoked and strictly before the absolute maximum lifetime ends. */
    public boolean isUsableAt(Instant now) {
        return !isRevoked() && now.isBefore(expiresAt);
    }

    public boolean belongsTo(UUID candidateAccountId) {
        return accountId.equals(candidateAccountId);
    }
}
```

**Compile impact.** The canonical constructor gained two parameters. Production callers: `establish` (updated here) and `JdbcSessionRepository.mapSession` (7.7). Test callers: only `LoginSessionTest` (replaced in Section 9). Stage 2 tests that use `LoginSession.establish(...)` compile unchanged.

### 7.4 `J/application/LogoutOutcome.java` — NEW

```java
package com.capsuleai.identity.application;

/** Result of a logout request for the current session. */
public enum LogoutOutcome {

    /** This request revoked the session. */
    REVOKED,

    /**
     * Nothing was revoked: the session was already revoked (for example by a
     * concurrent request) or is not this account's session.
     */
    NOTHING_TO_REVOKE
}
```

### 7.5 `J/application/ports/SessionRepository.java` — MODIFY

Complete resulting file:

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
}
```

### 7.6 `J/application/LogOutService.java` — NEW

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * US-AUTH-004 — revokes ONLY the session the request authenticated with
 * (FR-AUTH-007, BRULE-AUTH-006). Other sessions and all account data are untouched.
 */
public final class LogOutService {

    private final SessionRepository sessions;
    private final Clock clock;

    public LogOutService(SessionRepository sessions, Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public LogoutOutcome logOut(AuthenticatedAccount current) {
        boolean revoked = sessions.revoke(
                current.sessionId(),
                current.accountId(),
                RevocationReason.LOGOUT,
                clock.instant().truncatedTo(ChronoUnit.SECONDS));
        return revoked ? LogoutOutcome.REVOKED : LogoutOutcome.NOTHING_TO_REVOKE;
    }
}
```

The service receives the **published** `AuthenticatedAccount` principal, never a session id from the request body, so a client cannot ask to revoke somebody else's session.

### 7.7 `J/adapter/out/persistence/JdbcSessionRepository.java` — MODIFY

Complete resulting file (Stage 3): `findById` reads the revocation columns, `mapSession` builds the 6-field record, `revoke` is new; `create` is unchanged.

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
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

### 7.8 `J/configuration/IdentityConfiguration.java` — MODIFY

Complete resulting file (Stage 3) — adds the `LogOutService` bean and its import:

```java
package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.out.persistence.JdbcAccountRepository;
import com.capsuleai.identity.adapter.out.persistence.JdbcSessionRepository;
import com.capsuleai.identity.adapter.out.persistence.SpringIdentityTransactions;
import com.capsuleai.identity.adapter.out.security.Pbkdf2PasswordHasher;
import com.capsuleai.identity.application.LogInService;
import com.capsuleai.identity.application.LogOutService;
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
}
```

### 7.9 `J/adapter/in/web/LogoutController.java` — NEW

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.application.LogOutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-004 — ends the current login session. Requires a usable access token. */
@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final LogOutService logOutService;

    public LogoutController(LogOutService logOutService) {
        this.logOutService = logOutService;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(@AuthenticationPrincipal AuthenticatedAccount current) {
        logOutService.logOut(current);
        return ResponseEntity.noContent().build();
    }
}
```

Both outcomes return 204 because in both cases the session is unusable afterwards — which is what the user asked for. `NOTHING_TO_REVOKE` can only happen if another request revoked the same session in the few milliseconds after the filter checked it.

> **Concept — `@AuthenticationPrincipal`.** Spring Security registers an argument resolver that injects `Authentication.getPrincipal()` into a controller parameter. Our `AuthenticatedAccountToken` returns the published `AuthenticatedAccount`, so this controller (and any future module's controller) receives a plain Identity API record — not a Spring or JWT type.

### 7.10 `docs/05-api/openapi.yaml` — MODIFY

Two edits:

1. Change `info.version` from `0.2.0` to `0.3.0`.
2. Add this path under `paths:` directly after the `/api/v1/auth/login:` block (same indentation as `/api/v1/auth/login:`, two spaces):

```yaml
  /api/v1/auth/logout:
    post:
      tags: [Identity]
      operationId: logOut
      summary: Log out of the current session (US-AUTH-004)
      description: |
        Revokes only the login session the access token belongs to. Other
        sessions of the account and all account data are unchanged. Afterwards
        this session's access token (and refresh token) are rejected.
        A 401 means the session was already unusable, i.e. already signed out.
        Uses the default bearer security requirement.
      responses:
        '204':
          description: The current session is revoked.
        '401':
          $ref: '#/components/responses/Unauthorized'
```

---

## 8. Manual Implementation Walkthrough

Branch: `feature/US-AUTH-004-log-out` from `main` after US-AUTH-002 is merged.

### Step 1 — Migration and domain (files 1–3)

Create V4, `RevocationReason`, and replace `LoginSession`.

*Check:* `.\mvnw.cmd -q compile` fails in `JdbcSessionRepository.mapSession` (four arguments). Good — it shows every place that must know about revocation. Fix it in Step 3.

### Step 2 — Application (files 4–6)

`LogoutOutcome`, the extended port, `LogOutService`.

### Step 3 — Adapter, wiring, endpoint, contract (files 7–10)

Replace `JdbcSessionRepository` and `IdentityConfiguration`; create `LogoutController`; edit `openapi.yaml`.

*Check:* `.\mvnw.cmd -q compile` succeeds.

Request walkthrough:

```text
POST /api/v1/auth/logout  (Authorization: Bearer JWT{sub=A, sid=S1})
 └─ Security filter: signature ✓, iss ✓, now < exp ✓
     └─ SessionAuthorityService.findUsableSession(A, S1) → session row: revoked_at NULL, not expired → usable ✓
 └─ LogoutController.logOut(principal = AuthenticatedAccount(A, S1))
     └─ LogOutService.logOut → UPDATE identity_session SET revoked_at=08:05:00Z, reason='LOGOUT'
                                WHERE id=S1 AND account_id=A AND revoked_at IS NULL   → 1 row
 └─ 204

Next request with the same JWT:
 └─ Security filter: signature ✓, iss ✓, now < exp ✓
     └─ findUsableSession(A, S1) → revoked_at set → isUsableAt = false → empty
     └─ InvalidBearerTokenException → 401 AUTHENTICATION_REQUIRED
```

*Common mistakes:* revoking by `account_id` only (logs out every device — violates AC-02); deleting the session row (loses the history needed by US-AUTH-003's reuse detection and makes "already revoked" indistinguishable from "never existed"); reading the session id from the request body instead of the principal.

### Step 4 — Manual check

With the app running (Stage 2 instructions), log in twice as the same user to simulate two devices:

```powershell
$base = 'http://127.0.0.1:8080/api/v1/auth'
function Send-Json($uri, $object) {
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($object | ConvertTo-Json))
    Invoke-RestMethod -Method Post -Uri $uri -ContentType 'application/json; charset=utf-8' -Body $bytes
}
$deviceA = Send-Json "$base/login" @{ email = 'person@example.com'; password = 'correct horse battery staple' }
$deviceB = Send-Json "$base/login" @{ email = 'person@example.com'; password = 'correct horse battery staple' }

# Log out device A -> 204 (Invoke-WebRequest shows the status code)
(Invoke-WebRequest -Method Post -Uri "$base/logout" -Headers @{ Authorization = "Bearer $($deviceA.accessToken)" } -UseBasicParsing).StatusCode

# Same request again -> 401 AUTHENTICATION_REQUIRED (already signed out)
try { Invoke-WebRequest -Method Post -Uri "$base/logout" -Headers @{ Authorization = "Bearer $($deviceA.accessToken)" } -UseBasicParsing } catch {
    $_.Exception.Response.StatusCode.value__
    $_.ErrorDetails.Message
}

# Device B can still log out (it was still usable) -> 204
(Invoke-WebRequest -Method Post -Uri "$base/logout" -Headers @{ Authorization = "Bearer $($deviceB.accessToken)" } -UseBasicParsing).StatusCode
```

```powershell
Set-Location 'D:\CapsuleAI'
docker compose --env-file .env exec postgres psql -U capsuleai_dev -d capsuleai_dev -c 'SELECT id, established_at, revoked_at, revocation_reason FROM identity_session ORDER BY established_at;'
```

---

## 9. Complete Automated Test Code

Paths relative to `backend/src/test/java/com/capsuleai/`.

| # | File | Kind | Runner |
| --- | --- | --- | --- |
| T1 | `identity/domain/LoginSessionTest.java` | Domain — **MODIFY** (replace) | Surefire |
| T2 | `identity/application/InMemorySessionRepository.java` | Fake — **MODIFY** | — |
| T3 | `identity/application/LogOutServiceTest.java` | Application | Surefire |
| T4 | `identity/adapter/in/web/LogoutApiIT.java` | HTTP + security + PostgreSQL | Failsafe |
| T5 | `identity/adapter/out/persistence/SessionRevocationSchemaIT.java` | Migration / integrity | Failsafe |

### T1 — `identity/domain/LoginSessionTest.java` — MODIFY (complete replacement)

**Covers:** revocation makes a session unusable even before expiry (AC-01); consistency of revocation fields; Stage 2 boundaries kept.

```java
package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginSessionTest {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final Instant EXPIRES = ESTABLISHED.plus(Duration.ofDays(30));
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void establishedSessionEndsExactlyThirtyDaysLaterAndIsNotRevoked() {
        LoginSession session = established();

        assertThat(session.expiresAt()).isEqualTo(Instant.parse("2026-02-04T08:00:00Z"));
        assertThat(session.isRevoked()).isFalse();
        assertThat(session.revocationReason()).isNull();
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
    void revokedSessionIsUnusableEvenBeforeItsMaximumLifetime() {
        LoginSession revoked = new LoginSession(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED, EXPIRES,
                ESTABLISHED.plusSeconds(60), RevocationReason.LOGOUT);

        assertThat(revoked.isRevoked()).isTrue();
        assertThat(revoked.isUsableAt(ESTABLISHED.plusSeconds(61))).isFalse();
    }

    @Test
    void belongsOnlyToItsAccount() {
        LoginSession session = established();

        assertThat(session.belongsTo(ACCOUNT_ID)).isTrue();
        assertThat(session.belongsTo(UUID.randomUUID())).isFalse();
    }

    @Test
    void rejectsASessionThatDoesNotExpireAfterItStarts() {
        assertThatThrownBy(() -> new LoginSession(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED, ESTABLISHED, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void revocationTimeAndReasonAreRecordedTogether() {
        assertThatThrownBy(() -> new LoginSession(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED, EXPIRES,
                ESTABLISHED, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LoginSession(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED, EXPIRES,
                null, RevocationReason.LOGOUT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static LoginSession established() {
        return LoginSession.establish(UUID.randomUUID(), ACCOUNT_ID, ESTABLISHED);
    }
}
```

### T2 — `identity/application/InMemorySessionRepository.java` — MODIFY

Complete resulting file: adds `revoke` with the same semantics as the SQL (`id` + `account_id` + not yet revoked). Helper methods from Stage 2 are kept so `LogInServiceTest` still compiles.

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.SessionRepository;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import java.time.Instant;
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

    synchronized List<LoginSession> all() {
        return List.copyOf(sessionsById.values());
    }

    synchronized Optional<UUID> sessionIdForRefreshToken(RefreshTokenHash hash) {
        return Optional.ofNullable(sessionIdsByRefreshToken.get(hash));
    }
}
```

### T3 — `identity/application/LogOutServiceTest.java`

**Covers:** AC-01 (revoked now, reason LOGOUT), AC-02 (other session usable), AC-05/AC-06 (repeat → nothing more revoked; first revocation preserved), defence in depth against foreign sessions. **Does not prove:** SQL or the security chain (T4).

```java
package com.capsuleai.identity.application;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshTokenHash;
import com.capsuleai.identity.domain.RevocationReason;
import com.capsuleai.identity.support.MutableClock;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogOutServiceTest {

    private final InMemorySessionRepository sessions = new InMemorySessionRepository();
    private final MutableClock clock = new MutableClock(MutableClock.DEFAULT_START);
    private final LogOutService service = new LogOutService(sessions, clock);
    private final UUID accountId = UUID.randomUUID();

    @Test
    void revokesTheCurrentSessionAtTheCurrentTime() {
        LoginSession current = storedSession(accountId);
        clock.advance(Duration.ofMinutes(5));

        LogoutOutcome outcome = service.logOut(new AuthenticatedAccount(accountId, current.id()));

        assertThat(outcome).isEqualTo(LogoutOutcome.REVOKED);
        LoginSession stored = sessions.findById(current.id()).orElseThrow();
        assertThat(stored.revokedAt()).isEqualTo(MutableClock.DEFAULT_START.plus(Duration.ofMinutes(5)));
        assertThat(stored.revocationReason()).isEqualTo(RevocationReason.LOGOUT);
        assertThat(stored.isUsableAt(clock.instant())).isFalse();
    }

    @Test
    void leavesAnotherSessionOfTheSameAccountUsable() {
        LoginSession deviceA = storedSession(accountId);
        LoginSession deviceB = storedSession(accountId);

        service.logOut(new AuthenticatedAccount(accountId, deviceA.id()));

        assertThat(sessions.findById(deviceB.id()).orElseThrow().isUsableAt(clock.instant())).isTrue();
    }

    @Test
    void repeatedLogoutFindsNothingToRevokeAndKeepsTheFirstRevocation() {
        LoginSession current = storedSession(accountId);
        AuthenticatedAccount principal = new AuthenticatedAccount(accountId, current.id());
        service.logOut(principal);
        clock.advance(Duration.ofMinutes(1));

        assertThat(service.logOut(principal)).isEqualTo(LogoutOutcome.NOTHING_TO_REVOKE);
        assertThat(sessions.findById(current.id()).orElseThrow().revokedAt()).isEqualTo(MutableClock.DEFAULT_START);
    }

    @Test
    void neverRevokesASessionOfAnotherAccount() {
        LoginSession someoneElses = storedSession(accountId);

        LogoutOutcome outcome = service.logOut(new AuthenticatedAccount(UUID.randomUUID(), someoneElses.id()));

        assertThat(outcome).isEqualTo(LogoutOutcome.NOTHING_TO_REVOKE);
        assertThat(sessions.findById(someoneElses.id()).orElseThrow().isRevoked()).isFalse();
    }

    private LoginSession storedSession(UUID owner) {
        LoginSession session = LoginSession.establish(UUID.randomUUID(), owner, clock.instant());
        sessions.create(session, RefreshTokenHash.of("refresh-" + session.id()));
        return session;
    }
}
```

### T4 — `identity/adapter/in/web/LogoutApiIT.java`

**Covers:** AC-01 (unexpired token rejected after logout; persisted revocation), AC-02, AC-03 (account unchanged; re-login restores the same account), AC-04 (other account unaffected; old token returns no content), AC-05 (already revoked; expired token), AC-06 (retry is safe and targets nothing else), US-PRIV-001 AC-01/02 for the first production protected operation. **Proves:** real filter chain + PostgreSQL. **Does not prove:** client cache clearing or renewal denial (US-AUTH-003).

```java
package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogoutApiIT extends IdentityIntegrationTestSupport {

    private static final String LOGOUT = "/api/v1/auth/logout";

    @Test
    void logoutEndsTheCurrentSessionEvenThoughItsAccessTokenIsUnexpired() throws Exception {
        IssuedTokens tokens = registerAndLogIn("person@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        logOut(tokens.accessToken()).andExpect(status().isNoContent());

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        Map<String, Object> row = jdbc.sql("""
                        SELECT revoked_at, revocation_reason FROM identity_session WHERE id = :id
                        """)
                .param("id", sessionId)
                .query()
                .singleRow();
        assertThat(row.get("revoked_at")).isNotNull();
        assertThat(row.get("revocation_reason")).isEqualTo("LOGOUT");
    }

    @Test
    void logoutLeavesAnotherSessionOfTheSameAccountUsable() throws Exception {
        registerAccount("two-devices@example.com", PASSWORD);
        IssuedTokens deviceA = logIn("two-devices@example.com", PASSWORD);
        IssuedTokens deviceB = logIn("two-devices@example.com", PASSWORD);

        logOut(deviceA.accessToken()).andExpect(status().isNoContent());

        getProtectedProbe(deviceA.accessToken()).andExpect(status().isUnauthorized());
        getProtectedProbe(deviceB.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(deviceB.accountId().toString()));
        assertThat(jdbc.sql("SELECT count(*) FROM identity_session WHERE revoked_at IS NULL")
                .query(Long.class).single()).isOne();
    }

    @Test
    void repeatingLogoutReportsTheSessionAsAlreadyUnusable() throws Exception {
        IssuedTokens tokens = registerAndLogIn("retry@example.com");
        logOut(tokens.accessToken()).andExpect(status().isNoContent());

        logOut(tokens.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        assertThat(countRevokedSessions()).isOne();
    }

    @Test
    void logoutWithAnExpiredAccessTokenIsRejectedAndTargetsNoSession() throws Exception {
        IssuedTokens tokens = registerAndLogIn("expired@example.com");
        clock.advance(Duration.ofMinutes(15));

        logOut(tokens.accessToken()).andExpect(status().isUnauthorized());

        assertThat(countRevokedSessions()).isZero();
    }

    @Test
    void logoutKeepsTheAccountAndAllowsLoggingInAgain() throws Exception {
        IssuedTokens first = registerAndLogIn("keep@example.com");
        Map<String, Object> accountBefore = jdbc.sql("SELECT * FROM identity_account").query().singleRow();

        logOut(first.accessToken()).andExpect(status().isNoContent());

        assertThat(jdbc.sql("SELECT * FROM identity_account").query().singleRow()).isEqualTo(accountBefore);
        IssuedTokens again = logIn("keep@example.com", PASSWORD);
        assertThat(again.accountId()).isEqualTo(first.accountId());
        getProtectedProbe(again.accessToken()).andExpect(status().isOk());
    }

    @Test
    void logoutWithoutAuthenticationIsRejectedAndChangesNothing() throws Exception {
        registerAndLogIn("anonymous@example.com");

        mockMvc.perform(post(LOGOUT)).andExpect(status().isUnauthorized());

        assertThat(countRevokedSessions()).isZero();
    }

    @Test
    void oneAccountsLogoutDoesNotAffectAnotherAccount() throws Exception {
        IssuedTokens first = registerAndLogIn("first@example.com");
        IssuedTokens second = registerAndLogIn("second@example.com");

        logOut(first.accessToken()).andExpect(status().isNoContent());

        getProtectedProbe(first.accessToken())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accountId").doesNotExist());
        getProtectedProbe(second.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(second.accountId().toString()));
    }

    private ResultActions logOut(String accessToken) throws Exception {
        return mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private long countRevokedSessions() {
        return jdbc.sql("SELECT count(*) FROM identity_session WHERE revoked_at IS NOT NULL")
                .query(Long.class)
                .single();
    }
}
```

### T5 — `identity/adapter/out/persistence/SessionRevocationSchemaIT.java`

**Covers:** V4 migration; revocation integrity; **supported transition with existing data** (DoD schema trigger). The transition test runs the real migration files into a separate scratch schema: first up to V3, then inserts a Stage 2 style session, then migrates to the latest version and checks the old session is still "not revoked". **Proves:** the forward migration on real PostgreSQL. **Does not prove:** a production data migration at scale.

```java
package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionRevocationSchemaIT extends IdentityIntegrationTestSupport {

    private static final Instant ESTABLISHED = Instant.parse("2026-01-05T08:00:00Z");
    private static final String TRANSITION_SCHEMA = "identity_transition_v4";

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    void revocationMigrationIsApplied() {
        assertThat(flyway.info().applied())
                .extracting(migration -> migration.getVersion().getVersion())
                .contains("4");
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void revocationTimeAndReasonMustBeRecordedTogether() {
        UUID sessionId = insertSession();

        assertThatThrownBy(() -> jdbc.sql("UPDATE identity_session SET revoked_at = now() WHERE id = :id")
                .param("id", sessionId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("UPDATE identity_session SET revocation_reason = 'LOGOUT' WHERE id = :id")
                .param("id", sessionId).update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyKnownRevocationReasonsAreAccepted() {
        UUID sessionId = insertSession();

        assertThatThrownBy(() -> revoke(sessionId, "DELETED_BY_ADMIN"))
                .isInstanceOf(DataIntegrityViolationException.class);

        revoke(sessionId, "LOGOUT");
        assertThat(jdbc.sql("SELECT revocation_reason FROM identity_session WHERE id = :id")
                .param("id", sessionId).query(String.class).single()).isEqualTo("LOGOUT");
    }

    @Test
    void sessionsCreatedBeforeTheMigrationRemainUnrevoked() {
        try {
            migrateTransitionSchema("3");
            UUID accountId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_account (id, email, password_hash, created_at) "
                            + "VALUES (:id, 'existing@example.com', '{test}hash', now())")
                    .param("id", accountId)
                    .update();
            jdbc.sql("INSERT INTO " + TRANSITION_SCHEMA + ".identity_session (id, account_id, established_at, expires_at) "
                            + "VALUES (:id, :accountId, now(), now() + INTERVAL '1 day')")
                    .param("id", sessionId)
                    .param("accountId", accountId)
                    .update();

            migrateTransitionSchema(null);

            Map<String, Object> row = jdbc.sql("SELECT revoked_at, revocation_reason FROM "
                            + TRANSITION_SCHEMA + ".identity_session WHERE id = :id")
                    .param("id", sessionId)
                    .query()
                    .singleRow();
            assertThat(row.get("revoked_at")).isNull();
            assertThat(row.get("revocation_reason")).isNull();
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

    private void revoke(UUID sessionId, String reason) {
        jdbc.sql("UPDATE identity_session SET revoked_at = :at, revocation_reason = :reason WHERE id = :id")
                .param("at", ESTABLISHED.plusSeconds(60).atOffset(ZoneOffset.UTC))
                .param("reason", reason)
                .param("id", sessionId)
                .update();
    }
}
```

How the transition test works: `Flyway.configure().schemas(X)` creates schema `X`, keeps its own history table there, and sets PostgreSQL's `search_path` to `X` while migrating, so the unqualified `CREATE TABLE identity_session …` statements land in `X`. The test then uses schema-qualified SQL to insert and read. The schema is dropped afterwards; the normal `public` tables are not touched. `var` is used for Flyway's fluent configuration type to keep the code short.

### 9.1 Test traceability

| AC | Scenario | Test Class | Test Method | Verification Level |
| --- | --- | --- | --- | --- |
| AC-01 | Unexpired token rejected after logout; revocation persisted | `LogoutApiIT` | `logoutEndsTheCurrentSessionEvenThoughItsAccessTokenIsUnexpired` | Security chain + DB |
| AC-01 | Revoked = unusable before expiry | `LoginSessionTest` | `revokedSessionIsUnusableEvenBeforeItsMaximumLifetime` | Domain |
| AC-01 | Revoked at "now", reason LOGOUT | `LogOutServiceTest` | `revokesTheCurrentSessionAtTheCurrentTime` | Application |
| AC-01 | Logged-out session cannot renew | US-AUTH-003 `SessionRefreshApiIT` | `loggedOutSessionCannotBeRenewed` | **Cross-story** |
| AC-02 | Other session of same account usable | `LogoutApiIT` / `LogOutServiceTest` | `logoutLeavesAnotherSessionOfTheSameAccountUsable` (both) | Security chain / Application |
| AC-03 | Account unchanged; re-login same account | `LogoutApiIT` | `logoutKeepsTheAccountAndAllowsLoggingInAgain` | HTTP + DB |
| AC-03 | Wardrobe/profile/Outfit/Wear retained | — | — | **Not yet exercised** (capabilities not delivered) |
| AC-04 | Other account unaffected; old token yields no content | `LogoutApiIT` | `oneAccountsLogoutDoesNotAffectAnotherAccount` | Security chain |
| AC-04 | Client navigation / cache hidden | — | — | **External** |
| AC-05 | Already revoked → signed-out answer, nothing else revoked | `LogoutApiIT` / `LogOutServiceTest` | `repeatingLogoutReportsTheSessionAsAlreadyUnusable` / `repeatedLogoutFindsNothingToRevokeAndKeepsTheFirstRevocation` | Security chain / Application |
| AC-05 | Expired access token → 401, no session targeted | `LogoutApiIT` | `logoutWithAnExpiredAccessTokenIsRejectedAndTargetsNoSession` | Security chain |
| AC-06 | Retry after unconfirmed logout is safe | `LogoutApiIT` | `repeatingLogoutReportsTheSessionAsAlreadyUnusable`, `logoutLeavesAnotherSessionOfTheSameAccountUsable` | Security chain |
| AC-06 | Unconfirmed UI, content hidden | — | — | **External** |
| AC-07 | Accessible logout | — | — | **External only** |
| PRIV AC-01/02 | Unauthenticated logout rejected, no mutation | `LogoutApiIT` | `logoutWithoutAuthenticationIsRejectedAndChangesNothing` | Security chain |
| PRIV AC-02 | Foreign session never revoked | `LogOutServiceTest` | `neverRevokesASessionOfAnotherAccount` | Application |
| DoD schema | V4 constraints; transition with existing data | `SessionRevocationSchemaIT` | all four | PostgreSQL migration |

---

## 10. Run and Verify

```powershell
Set-Location 'D:\CapsuleAI\backend'

.\mvnw.cmd compile
.\mvnw.cmd '-Dtest=LoginSessionTest,LogOutServiceTest' test
.\mvnw.cmd '-Dit.test=LogoutApiIT,SessionRevocationSchemaIT' verify
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
if ($LASTEXITCODE -ne 0) { throw 'Verification failed; read the first error above.' }

Get-ChildItem .\target\failsafe-reports\*.txt | Get-Content
```

**Expected:** `BUILD SUCCESS`; `LogoutApiIT` and `SessionRevocationSchemaIT` appear; every Stage 1–2 class still passes (notably `AccessTokenSecurityIT`, because `isUsableAt` changed).

| Symptom | Likely cause / fix |
| --- | --- |
| Compile error `constructor LoginSession … cannot be applied` | A caller still uses 4 arguments (`mapSession`, a test). |
| Logout returns 401 for a fresh token | Logout path accidentally added to `PUBLIC_ENDPOINTS` (then no principal exists) — it must **not** be public. |
| Old token still works after logout | `findById` does not select `revoked_at`, or `mapSession` ignores it. |
| `ck_identity_session_revoked_after_established` violation | Revocation time earlier than establishment (check clock usage in tests). |
| Transition test: `relation … does not exist` | Migration location or schema name typo; Flyway must target the scratch schema. |

---

## 11. CI Integration

No workflow change is required: the new classes are discovered by naming, and Docker/Testcontainers are already available. Recommended guard additions (append to the Stage 2 `$required` block):

```powershell
            'surefire-reports/TEST-com.capsuleai.identity.application.LogOutServiceTest.xml' = 1
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.LogoutApiIT.xml' = 1
```

Confirm in the PR run that both print `… tests, none failed/skipped`.

---

## 12. Completion Checklist

**Production implementation**
- [ ] V4 applied; columns and three CHECKs exist.
- [ ] `LoginSession` revocation-aware; `JdbcSessionRepository` maps and revokes; `LogOutService` wired; `LogoutController` added; logout **not** public.
- [ ] `openapi.yaml` 0.3.0 with `/api/v1/auth/logout`.

**Rules and AC**
- [ ] Only the current session is revoked (BRULE-AUTH-006); other device and other accounts unaffected.
- [ ] Unexpired token rejected immediately after logout (ADA-008).
- [ ] Account row unchanged; re-login works.
- [ ] Repeated/expired/unauthenticated logout → 401, nothing else changes.

**Tests and verification**
- [ ] `LoginSessionTest` (replaced), `LogOutServiceTest`, `LogoutApiIT`, `SessionRevocationSchemaIT` pass.
- [ ] All Stage 1–2 tests pass.
- [ ] `clean verify` green locally and in CI; guard updated (optional).
- [ ] Review done; PR references `US-AUTH-004`, `PBI-004`.

**Remaining external evidence**
- [ ] Signed-out screen; local tokens and cached personal content cleared (AC-01, AC-04).
- [ ] Unconfirmed-logout presentation with content hidden (AC-06); accessibility (AC-07).
- [ ] Wardrobe/profile/history retention re-verified once those features exist (AC-03).

Next: [US-AUTH-003 — Resume access within my login session](US-AUTH-003-guide.md).
