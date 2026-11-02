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
