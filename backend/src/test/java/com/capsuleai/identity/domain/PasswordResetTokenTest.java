package com.capsuleai.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetTokenTest {

    private static final Instant ISSUED = Instant.parse("2026-01-05T08:00:00Z");

    private final PasswordResetToken issued = PasswordResetToken.issue(UUID.randomUUID(), UUID.randomUUID(), ISSUED);

    @Test
    void anIssuedInteractionExpiresExactlyThirtyMinutesLater() {
        assertThat(issued.expiresAt()).isEqualTo(ISSUED.plus(Duration.ofMinutes(30)));
        assertThat(PasswordResetPolicy.INTERACTION_LIFETIME).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void itIsUsableUntilTheLastSecondBeforeExpiry() {
        assertThat(issued.isUsableAt(ISSUED)).isTrue();
        assertThat(issued.isUsableAt(ISSUED.plus(Duration.ofMinutes(30)).minusSeconds(1))).isTrue();
    }

    @Test
    void itIsNotUsableAtOrAfterTheThirtyMinuteBoundary() {
        assertThat(issued.isUsableAt(ISSUED.plus(Duration.ofMinutes(30)))).isFalse();
        assertThat(issued.isUsableAt(ISSUED.plus(Duration.ofMinutes(30)).plusSeconds(1))).isFalse();
    }

    @Test
    void aConsumedInteractionIsNeverUsableAgain() {
        PasswordResetToken consumed = new PasswordResetToken(issued.id(), issued.accountId(), ISSUED,
                issued.expiresAt(), ISSUED.plusSeconds(60), null);

        assertThat(consumed.isUsableAt(ISSUED.plusSeconds(61))).isFalse();
    }

    @Test
    void aReplacedInteractionIsNotUsableEvenBeforeItsExpiry() {
        PasswordResetToken replaced = new PasswordResetToken(issued.id(), issued.accountId(), ISSUED,
                issued.expiresAt(), null, ISSUED.plusSeconds(60));

        assertThat(replaced.isUsableAt(ISSUED.plusSeconds(61))).isFalse();
    }

    @Test
    void anInteractionCannotEndBothWays() {
        assertThatThrownBy(() -> new PasswordResetToken(issued.id(), issued.accountId(), ISSUED,
                issued.expiresAt(), ISSUED.plusSeconds(1), ISSUED.plusSeconds(2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anInteractionMustExpireAfterIssue() {
        assertThatThrownBy(() -> new PasswordResetToken(issued.id(), issued.accountId(), ISSUED, ISSUED, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
