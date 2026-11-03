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
