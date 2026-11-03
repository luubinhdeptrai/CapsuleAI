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