package com.capsuleai.identity.adapter.in.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Rejects an access token at and after its {@code exp} instant, with zero clock
 * skew, using the application's {@link Clock} (FR-AUTH-010, TD-09).
 */
public final class AccessTokenExpiryValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error EXPIRED =
            new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "The access token has expired", null);

    private final Clock clock;

    public AccessTokenExpiryValidator(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Instant expiresAt = token.getExpiresAt();
        if (expiresAt == null || !clock.instant().isBefore(expiresAt)) {
            return OAuth2TokenValidatorResult.failure(EXPIRED);
        }
        return OAuth2TokenValidatorResult.success();
    }
}