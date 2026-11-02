package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.in.security.ProblemJsonAuthenticationEntryPoint;
import com.capsuleai.identity.adapter.in.security.PublicEndpointAwareBearerTokenResolver;
import com.capsuleai.identity.adapter.in.security.SessionAwareJwtAuthenticationConverter;
import com.capsuleai.identity.api.SessionAuthority;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * The application-wide security policy. Identity owns authentication, so it
 * owns this single filter chain: public auth endpoints, everything else needs a
 * usable login session (BRULE-AUTH-001, FR-AUTH-009).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class IdentitySecurityConfiguration {

    /** Operations anyone may call. Every other request needs a usable login session. */
    static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/registrations"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/v1/auth/login"));

    @Bean
    SecurityFilterChain identitySecurityFilterChain(HttpSecurity http,
                                                    JwtDecoder accessTokenDecoder,
                                                    SessionAuthority sessionAuthority) throws Exception {
        AuthenticationEntryPoint entryPoint = new ProblemJsonAuthenticationEntryPoint();
        http
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
}