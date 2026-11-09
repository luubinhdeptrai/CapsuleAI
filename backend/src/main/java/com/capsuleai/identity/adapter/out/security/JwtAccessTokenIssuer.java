package com.capsuleai.identity.adapter.out.security;

import com.capsuleai.identity.adapter.jwt.JwtClaimNames;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

/** Signs HS256 access tokens naming the account ({@code sub}) and login session ({@code sid}). */
public final class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder encoder;
    private final String issuer;

    public JwtAccessTokenIssuer(JwtEncoder encoder, String issuer) {
        this.encoder = Objects.requireNonNull(encoder, "encoder");
        this.issuer = Objects.requireNonNull(issuer, "issuer");
    }

    @Override
    public String issue(UUID accountId, UUID sessionId, Instant issuedAt, Instant expiresAt) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(accountId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(JwtClaimNames.SESSION_ID, sessionId.toString())
                .build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}