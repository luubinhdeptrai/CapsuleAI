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
