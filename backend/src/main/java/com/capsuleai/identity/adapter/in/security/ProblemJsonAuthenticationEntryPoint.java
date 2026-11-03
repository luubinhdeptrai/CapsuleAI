package com.capsuleai.identity.adapter.in.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Answers authentication failures with a problem body:
 * <ul>
 *   <li>credentials not acceptable → 401, the standard {@code WWW-Authenticate:
 *       Bearer} header and "authenticate again" — never an "empty wardrobe"
 *       (BRULE-AUTH-001);</li>
 *   <li>session authority could not be checked → 503 "temporarily unavailable",
 *       without a challenge header, because the credentials were not found invalid.</li>
 * </ul>
 * Both bodies are fixed strings, so no exception text or request data can leak.
 */
public final class ProblemJsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String AUTHENTICATION_REQUIRED_BODY = """
            {"type":"about:blank","title":"Unauthorized","status":401,\
            "detail":"Authentication is required. Log in again to continue.",\
            "code":"AUTHENTICATION_REQUIRED"}""";

    private static final String TEMPORARILY_UNAVAILABLE_BODY = """
            {"type":"about:blank","title":"Service Unavailable","status":503,\
            "detail":"Your access could not be verified right now. Nothing was changed. Try again shortly.",\
            "code":"TEMPORARILY_UNAVAILABLE"}""";

    private final AuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException, ServletException {
        if (authenticationException instanceof SessionAuthorityUnavailableException) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            writeProblemBody(response, TEMPORARILY_UNAVAILABLE_BODY);
            return;
        }
        bearerEntryPoint.commence(request, response, authenticationException);
        writeProblemBody(response, AUTHENTICATION_REQUIRED_BODY);
    }

    private static void writeProblemBody(HttpServletResponse response, String body) throws IOException {
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
    }
}