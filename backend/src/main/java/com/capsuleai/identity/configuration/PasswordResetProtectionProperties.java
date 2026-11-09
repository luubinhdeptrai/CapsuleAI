package com.capsuleai.identity.configuration;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Password-reset abuse protection (OPEN-S2-04, OPEN-S2-05). The defaults live in
 * {@code application.properties}; they are conservative technical choices (TD-S2-26), not
 * product rules, and each deployment may tune them.
 *
 * @param requestPerEmailMaxAttempts      Forgot Password requests allowed per normalized email per window
 * @param requestPerClientMaxAttempts     Forgot Password requests allowed per client address per window
 * @param verificationPerClientMaxAttempts link checks and completions allowed per client address per window
 * @param window                          length of one fixed counting window
 * @param minimumRequestResponseTime      every successful Forgot Password request lasts at least this long
 */
@ConfigurationProperties("capsuleai.identity.password-reset.protection")
public record PasswordResetProtectionProperties(int requestPerEmailMaxAttempts,
                                                int requestPerClientMaxAttempts,
                                                int verificationPerClientMaxAttempts,
                                                Duration window,
                                                Duration minimumRequestResponseTime) {

    public PasswordResetProtectionProperties {
        Objects.requireNonNull(window, "capsuleai.identity.password-reset.protection.window is not configured");
        Objects.requireNonNull(minimumRequestResponseTime,
                "capsuleai.identity.password-reset.protection.minimum-request-response-time is not configured");
        if (requestPerEmailMaxAttempts < 1 || requestPerClientMaxAttempts < 1 || verificationPerClientMaxAttempts < 1) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.password-reset.protection limits must each allow at least one attempt");
        }
        if (window.toSeconds() < 1) {
            throw new IllegalArgumentException("capsuleai.identity.password-reset.protection.window must be at least 1s");
        }
        if (minimumRequestResponseTime.isNegative() || minimumRequestResponseTime.compareTo(Duration.ofSeconds(2)) > 0) {
            throw new IllegalArgumentException(
                    "capsuleai.identity.password-reset.protection.minimum-request-response-time must be between 0 and 2s");
        }
    }
}