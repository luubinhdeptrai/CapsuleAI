package com.capsuleai.identity.adapter.out.persistence;

import com.capsuleai.identity.application.PurgeEndedPasswordResetsService;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OPEN-S2-07, DATA-AUTH-006: an interaction is deleted 30 days after it ended (consumed,
 * replaced or expired); a usable interaction never is, and the cleanup never waits for a
 * row another transaction holds.
 */
class PasswordResetCleanupIT extends IdentityIntegrationTestSupport {

    private static final Duration RETENTION = Duration.ofDays(30);

    @Autowired
    private PurgeEndedPasswordResetsService purge;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Instant now;

    @BeforeEach
    void theCurrentInstant() {
        now = clock.instant();
    }

    @Test
    void eachEndedInteractionIsDeletedExactlyThirtyDaysAfterItEnded() {
        UUID consumedLongAgo = consumedAt(now.minus(RETENTION).minusSeconds(1));
        UUID consumedExactlyThirtyDaysAgo = consumedAt(now.minus(RETENTION));
        UUID consumedRecently = consumedAt(now.minus(RETENTION).plusSeconds(1));
        UUID replacedLongAgo = replacedAt(now.minus(RETENTION).minusSeconds(1));
        UUID expiredLongAgo = expiredAt(now.minus(RETENTION).minusSeconds(1));
        UUID expiredRecently = expiredAt(now.minus(RETENTION).plusSeconds(1));

        PurgeEndedPasswordResetsService.PurgeResult result = purge.purge();

        assertThat(result.deletedInteractions()).isEqualTo(4);
        assertThat(remaining()).containsExactlyInAnyOrder(consumedRecently, expiredRecently)
                .doesNotContain(consumedLongAgo, consumedExactlyThirtyDaysAgo, replacedLongAgo, expiredLongAgo);
    }

    @Test
    void aUsableInteractionIsNeverDeleted() {
        UUID usable = insert(now.minusSeconds(60), null, null);
        clock.setInstant(now.plusSeconds(60));

        purge.purge();

        assertThat(remaining()).containsExactly(usable);
    }

    @Test
    void theCleanupSkipsARowAnotherTransactionHoldsAndDeletesItOnTheNextRun() throws Exception {
        UUID held = consumedAt(now.minus(RETENTION).minus(Duration.ofDays(1)));
        UUID free = consumedAt(now.minus(RETENTION).minus(Duration.ofDays(2)));
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Void> holder = CompletableFuture.runAsync(() -> new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> {
                    jdbc.sql("SELECT id FROM identity.identity_password_reset WHERE id = :id FOR UPDATE")
                            .param("id", held).query(UUID.class).single();
                    locked.countDown();
                    try {
                        release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }));
        try {
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();

            assertThat(purge.purge().deletedInteractions()).isOne();
            assertThat(remaining()).containsExactly(held).doesNotContain(free);
        } finally {
            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
        }

        assertThat(purge.purge().deletedInteractions()).isOne();
        assertThat(remaining()).isEmpty();
    }

    @Test
    void attemptCountersAreRemovedADayAfterTheirWindowStarted() {
        jdbc.sql("INSERT INTO identity.identity_password_reset_attempt (scope, subject_hash, window_start, attempts) "
                        + "VALUES ('REQUEST_PER_CLIENT', :old, :oldWindow, 3), ('REQUEST_PER_CLIENT', :recent, :recentWindow, 1)")
                .param("old", "a".repeat(64))
                .param("oldWindow", now.minus(Duration.ofDays(1)).minusSeconds(1).atOffset(ZoneOffset.UTC))
                .param("recent", "b".repeat(64))
                .param("recentWindow", now.minus(Duration.ofHours(23)).atOffset(ZoneOffset.UTC))
                .update();

        assertThat(purge.purge().deletedAttemptCounters()).isOne();
        assertThat(countRows("identity.identity_password_reset_attempt")).isOne();
    }

    // ---------------------------------------------------------------- rows

    private UUID consumedAt(Instant consumed) {
        return insert(consumed.minusSeconds(60), consumed, null);
    }

    private UUID replacedAt(Instant replaced) {
        return insert(replaced.minusSeconds(60), null, replaced);
    }

    private UUID expiredAt(Instant expiry) {
        return insert(expiry.minus(Duration.ofMinutes(30)), null, null);
    }

    /** Each row gets its own account: an account may hold only one open interaction. */
    private UUID insert(Instant issued, Instant consumed, Instant replaced) {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        jdbc.sql("INSERT INTO identity.identity_account (id, email, password_hash, created_at) "
                        + "VALUES (:id, :email, '{test}hash', now())")
                .param("id", accountId).param("email", accountId + "@example.com").update();
        jdbc.sql("""
                        INSERT INTO identity.identity_password_reset
                               (id, account_id, token_hash, issued_at, expires_at, consumed_at, replaced_at)
                        VALUES (:id, :accountId, :hash, :issued, :expires, :consumed, :replaced)
                        """)
                .param("id", id)
                .param("accountId", accountId)
                .param("hash", PasswordResetTokenHash.of(id.toString()).value())
                .param("issued", issued.atOffset(ZoneOffset.UTC))
                .param("expires", issued.plus(Duration.ofMinutes(30)).atOffset(ZoneOffset.UTC))
                .param("consumed", consumed == null ? null : consumed.atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .param("replaced", replaced == null ? null : replaced.atOffset(ZoneOffset.UTC), java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
                .update();
        return id;
    }

    private List<UUID> remaining() {
        return jdbc.sql("SELECT id FROM identity.identity_password_reset").query(UUID.class).list();
    }
}
