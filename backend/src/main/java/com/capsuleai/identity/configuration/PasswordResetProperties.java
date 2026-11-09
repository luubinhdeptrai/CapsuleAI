package com.capsuleai.identity.configuration;

import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.InvalidValueException;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Password-reset email settings ({@code capsuleai.identity.password-reset.*}).
 *
 * <p>{@code senderAddress} is the From address. {@code instructionLink} is the
 * absolute link the mobile client opens; the reset secret is appended as the
 * fragment {@code #token=...}, so the configured link must not have a fragment.
 * Startup fails if either is missing or invalid.
 */
@ConfigurationProperties("capsuleai.identity.password-reset")
public record PasswordResetProperties(String senderAddress,
                                      URI instructionLink,
                                      boolean developmentCustomSchemeAllowed) {

    public PasswordResetProperties {
        if (senderAddress == null || senderAddress.isBlank()) {
            throw new IllegalArgumentException("capsuleai.identity.password-reset.sender-address is not configured");
        }
        try {
            EmailAddress.normalize(senderAddress);
        } catch (InvalidValueException notAnEmailAddress) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.password-reset.sender-address must be an email address");
        }
        if (instructionLink == null) {
            throw new IllegalArgumentException("capsuleai.identity.password-reset.instruction-link is not configured");
        }
        if (!instructionLink.isAbsolute() || instructionLink.getRawFragment() != null
                || instructionLink.getRawUserInfo() != null) {
            throw new IllegalArgumentException("capsuleai.identity.password-reset.instruction-link must be an absolute "
                    + "link without user information or fragment, such as https://<verified-domain>/reset-password");
        }
        // OPEN-S2-02: production links are verified HTTPS App Links / Universal Links; a private
        // custom scheme is accepted only where the development flag allows it, and plain http never.
        String scheme = instructionLink.getScheme().toLowerCase(java.util.Locale.ROOT);
        if (scheme.equals("https")) {
            if (instructionLink.getHost() == null || instructionLink.getHost().isBlank()) {
                throw new IllegalArgumentException("capsuleai.identity.password-reset.instruction-link must name the "
                        + "verified HTTPS domain the mobile app is associated with");
            }
        } else if (scheme.equals("http") || !developmentCustomSchemeAllowed) {
            throw new IllegalArgumentException("capsuleai.identity.password-reset.instruction-link must use https; a "
                    + "custom scheme is allowed only with development-custom-scheme-allowed=true (local development)");
        }
    }
}