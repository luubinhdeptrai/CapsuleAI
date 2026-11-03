package com.capsuleai.identity.configuration;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Access-token signing configuration ({@code capsuleai.identity.access-token.*}).
 * The secret is Base64 and must decode to at least 32 bytes (HS256).
 */
@ConfigurationProperties("capsuleai.identity.access-token")
public record AccessTokenProperties(String signingSecret) {

    static final int MINIMUM_SECRET_BYTES = 32;

    public AccessTokenProperties {
        if (signingSecret == null || signingSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.access-token.signing-secret is not configured");
        }
        if (decode(signingSecret).length < MINIMUM_SECRET_BYTES) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.access-token.signing-secret must decode to at least 32 bytes");
        }
    }

    public SecretKey signingKey() {
        return new SecretKeySpec(decode(signingSecret), "HmacSHA256");
    }

    private static byte[] decode(String base64) {
        try {
            return Base64.getDecoder().decode(base64.strip());
        } catch (IllegalArgumentException notBase64) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.access-token.signing-secret must be Base64-encoded");
        }
    }

    @Override
    public String toString() {
        return "AccessTokenProperties[signingSecret=PROTECTED]";
    }
}