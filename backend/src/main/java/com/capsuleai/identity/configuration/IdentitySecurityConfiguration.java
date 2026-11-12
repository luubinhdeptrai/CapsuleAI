package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.in.security.ProblemJsonAuthenticationEntryPoint;
import com.capsuleai.identity.adapter.in.security.PublicEndpointAwareBearerTokenResolver;
import com.capsuleai.identity.adapter.in.security.SessionAwareJwtAuthenticationConverter;
import com.capsuleai.identity.api.SessionAuthority;
import jakarta.servlet.DispatcherType;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * The application-wide security policy. Identity owns authentication, so it
 * owns this single filter chain: public auth endpoints, everything else needs a
 * usable login session (BRULE-AUTH-001, FR-AUTH-009).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(CorsProperties.class)
public class IdentitySecurityConfiguration {

    /** How long a browser may reuse a granted preflight before asking again. */
    static final Duration CORS_PREFLIGHT_MAX_AGE = Duration.ofMinutes(30);

    /** Request header naming one logical garment addition (US-GAR-001); browsers must be allowed to send it. */
    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

 /**
     * Operations anyone may call. Renewal is public because the refresh token in
     * the body is its credential; requiring a still-valid access token would make
     * renewal at access expiry impossible (Sprint plan §4). Password recovery is
     * public because the user cannot log in; the reset secret is its credential.
     */
    static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/registrations"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/login"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/refresh"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/password-reset/request"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/password-reset/check"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/password-reset/complete"));

    @Bean
    SecurityFilterChain identitySecurityFilterChain(HttpSecurity http,
                                                    JwtDecoder accessTokenDecoder,
                                                    SessionAuthority sessionAuthority,
                                                    CorsConfigurationSource corsConfigurationSource) throws Exception {
        AuthenticationEntryPoint entryPoint = new ProblemJsonAuthenticationEntryPoint();
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(new PublicEndpointAwareBearerTokenResolver(PUBLIC_ENDPOINTS))
                        .authenticationEntryPoint(entryPoint)
                        .jwt(jwt -> jwt
                                .decoder(accessTokenDecoder)
                                .jwtAuthenticationConverter(
                                        new SessionAwareJwtAuthenticationConverter(sessionAuthority))));
        return http.build();
    }

    /**
     * Browser access from the configured exact origins only (for example the
     * Swagger Editor or a web client); none by default. Native clients send no
     * Origin and are unaffected. The CORS filter answers a granted preflight
     * before authentication, but that authorizes nothing: the actual request
     * still passes every rule above. No credentials mode, because the API reads
     * bearer tokens from the Authorization header and never uses cookies.
     */
    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsProperties.allowedOrigins());
        // PATCH: US-GAR-002 garment edits (and US-PROF-001 preference revisions); OPEN-S3-01.
        corsConfiguration.setAllowedMethods(
                List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PATCH.name()));        corsConfiguration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, IDEMPOTENCY_KEY_HEADER));
        corsConfiguration.setAllowCredentials(false);
        corsConfiguration.setMaxAge(CORS_PREFLIGHT_MAX_AGE);
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return corsConfigurationSource;
    }
}