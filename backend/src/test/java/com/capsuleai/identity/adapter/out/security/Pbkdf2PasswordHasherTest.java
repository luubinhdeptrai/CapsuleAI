package com.capsuleai.identity.adapter.out.security;

import com.capsuleai.identity.domain.PasswordHash;
import java.text.Normalizer;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

    @Test
    void producesASaltedOneWayEncodingThatIsNotThePassword() {
        String password = "correct horse battery staple";

        PasswordHash first = hasher.hash(password);
        PasswordHash second = hasher.hash(password);

        assertThat(first.value()).startsWith("{pbkdf2-sha256-v1}").doesNotContain(password);
        assertThat(first.value()).isNotEqualTo(second.value());
        assertThat(hasher.matches(password, first)).isTrue();
        assertThat(hasher.matches(password, second)).isTrue();
    }

    @Test
    void acceptsOnlyTheExactPasswordIncludingSpacesAndUnicode() {
        // "  Mật khẩu rất dài  "
        String exact = "  M\u1EADt kh\u1EA9u r\u1EA5t d\u00E0i  ";
        PasswordHash stored = hasher.hash(exact);

        assertThat(hasher.matches(exact, stored)).isTrue();
        assertThat(hasher.matches(exact.strip(), stored)).isFalse();
        assertThat(hasher.matches(Normalizer.normalize(exact, Normalizer.Form.NFD), stored)).isFalse();
        assertThat(hasher.matches(exact.toUpperCase(Locale.ROOT), stored)).isFalse();
    }

    @Test
    void distinguishesLongPasswordsThatDifferOnlyAtTheEnd() {
        // 127 × "ậ" = 381 UTF-8 bytes, far beyond bcrypt's 72-byte limit.
        String commonPrefix = "\u1EAD".repeat(127);
        PasswordHash stored = hasher.hash(commonPrefix + "a");

        assertThat(hasher.matches(commonPrefix + "a", stored)).isTrue();
        assertThat(hasher.matches(commonPrefix + "b", stored)).isFalse();
    }
}