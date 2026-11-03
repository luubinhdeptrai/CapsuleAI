package com.capsuleai.identity.adapter.in.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Ignores the Authorization header on public endpoints, so a stale token that a
 * client attaches to every request cannot block public operations such as login.
 */
public final class PublicEndpointAwareBearerTokenResolver implements BearerTokenResolver {

    private final RequestMatcher publicEndpoints;
    private final BearerTokenResolver delegate = new DefaultBearerTokenResolver();

    public PublicEndpointAwareBearerTokenResolver(RequestMatcher publicEndpoints) {
        this.publicEndpoints = Objects.requireNonNull(publicEndpoints, "publicEndpoints");
    }

    @Override
    public String resolve(HttpServletRequest request) {
        return publicEndpoints.matches(request) ? null : delegate.resolve(request);
    }
}