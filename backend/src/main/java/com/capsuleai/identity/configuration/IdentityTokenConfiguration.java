package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.in.security.AccessTokenExpiryValidator;
import com.capsuleai.identity.adapter.out.security.JwtAccessTokenIssuer;
import com.capsuleai.identity.adapter.out.security.SecureRandomRefreshTokenGenerator;
import com.capsuleai.identity.application.ports.AccessTokenIssuer;
import com.capsuleai.identity.application.ports.RefreshTokenGenerator;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** Access-token signing and verification beans. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AccessTokenProperties.class)
public class IdentityTokenConfiguration {

    static final String ACCESS_TOKEN_ISSUER = "capsuleai-identity";

    @Bean
    JwtEncoder accessTokenEncoder(AccessTokenProperties properties) {
        return NimbusJwtEncoder.withSecretKey(properties.signingKey())
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtDecoder accessTokenDecoder(AccessTokenProperties properties, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(properties.signingKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(List.<OAuth2TokenValidator<Jwt>>of(
                JwtTypeValidator.jwt(),
                new JwtIssuerValidator(ACCESS_TOKEN_ISSUER),
                new AccessTokenExpiryValidator(clock))));
        return decoder;
    }

    @Bean
    AccessTokenIssuer accessTokenIssuer(JwtEncoder accessTokenEncoder) {
        return new JwtAccessTokenIssuer(accessTokenEncoder, ACCESS_TOKEN_ISSUER);
    }

    @Bean
    RefreshTokenGenerator refreshTokenGenerator() {
        return new SecureRandomRefreshTokenGenerator();
    }
}