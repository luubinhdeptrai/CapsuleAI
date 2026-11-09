package com.capsuleai.identity.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetTokenHashTest {

    @Test
    void theSameSecretAlwaysHashesToTheSameLowerCaseHexDigest() {
        PasswordResetTokenHash hash = PasswordResetTokenHash.of("reset-secret");

        assertThat(hash.value()).matches("[0-9a-f]{64}");
        assertThat(PasswordResetTokenHash.of("reset-secret")).isEqualTo(hash);
        assertThat(PasswordResetTokenHash.of("reset-secret-2")).isNotEqualTo(hash);
    }

    @Test
    void theDigestIsSha256OfTheUtf8Secret() {
        // SHA-256("abc"), a published test vector.
        assertThat(PasswordResetTokenHash.of("abc").value())
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void toStringNeverShowsTheDigest() {
        PasswordResetTokenHash hash = PasswordResetTokenHash.of("reset-secret");

        assertThat(hash.toString()).doesNotContain(hash.value()).contains("PROTECTED");
    }

    @Test
    void anEmptySecretIsRejected() {
        assertThatThrownBy(() -> PasswordResetTokenHash.of("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PasswordResetTokenHash("ABC")).isInstanceOf(IllegalArgumentException.class);
    }
}
