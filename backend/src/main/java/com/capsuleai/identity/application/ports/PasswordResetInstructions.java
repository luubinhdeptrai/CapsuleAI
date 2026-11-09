package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.EmailAddress;
import java.time.Instant;
import java.util.Objects;

/**
 * Everything the Email Delivery Service may receive for one recovery (SI-003,
 * BRULE-AUTH-007): the normalized recipient, the reset secret and its expiry.
 * No wardrobe, profile, history or other account data. toString() masks the secret.
 */
public record PasswordResetInstructions(EmailAddress recipient, String resetToken, Instant expiresAt) {

    public PasswordResetInstructions {
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(resetToken, "resetToken");
        Objects.requireNonNull(expiresAt, "expiresAt");
    }

    @Override
    public String toString() {
        return "PasswordResetInstructions[recipient=" + recipient.value()
                + ", resetToken=[PROTECTED], expiresAt=" + expiresAt + "]";
    }
}