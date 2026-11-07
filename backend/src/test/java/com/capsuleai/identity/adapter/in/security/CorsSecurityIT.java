package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_MAX_AGE;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpHeaders.VARY;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS through the real filter chain, persistence adapters and PostgreSQL. A
 * configured browser origin (the Swagger Editor, a web client) gets granted
 * preflights and readable responses; any other origin gets no grant. Neither
 * changes who is authenticated: every actual request still needs a valid token
 * and a usable session, exactly as without CORS.
 */
@TestPropertySource(properties = "capsuleai.cors.allowed-origins="
        + CorsSecurityIT.SWAGGER_EDITOR + "," + CorsSecurityIT.WEB_CLIENT)
class CorsSecurityIT extends IdentityIntegrationTestSupport {

    static final String SWAGGER_EDITOR = "https://editor.swagger.io";
    static final String WEB_CLIENT = "https://web-client.example";
    private static final String UNLISTED_ORIGIN = "https://attacker.example";

    private static final String LOGOUT = "/api/v1/auth/logout";
    private static final String REFRESH = "/api/v1/auth/refresh";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @ParameterizedTest
    @ValueSource(strings = {REGISTRATIONS, LOGIN, REFRESH})
    void swaggerEditorPreflightForAPublicOperationIsGranted(String path) throws Exception {
        mockMvc.perform(preflight(path, SWAGGER_EDITOR, "POST", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, SWAGGER_EDITOR))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST"))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_HEADERS, "content-type"))
                .andExpect(header().string(ACCESS_CONTROL_MAX_AGE, "1800"))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_CREDENTIALS))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_EXPOSE_HEADERS))
                .andExpect(header().doesNotExist(WWW_AUTHENTICATE))
                .andExpect(content().string(""));
    }

    /** US-GAR-001: a browser client must be allowed to send the Idempotency-Key of a garment addition. */
    @Test
    void garmentAdditionPreflightMayCarryTheIdempotencyKey() throws Exception {
        mockMvc.perform(preflight("/api/v1/wardrobe/garments", WEB_CLIENT, "POST",
                        "authorization,content-type,idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, WEB_CLIENT))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_HEADERS, containsString("idempotency-key")))
                .andExpect(header().doesNotExist(WWW_AUTHENTICATE));
    }

    @Test
    void protectedOperationPreflightNeedsNoTokenButTheActualRequestStillDoes() throws Exception {
        mockMvc.perform(preflight(PROTECTED_PROBE, SWAGGER_EDITOR, "GET", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, SWAGGER_EDITOR))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_HEADERS, "authorization"))
                .andExpect(header().doesNotExist(WWW_AUTHENTICATE));

        expectReadableAuthenticationFailure(
                mockMvc.perform(get(PROTECTED_PROBE).header(ORIGIN, SWAGGER_EDITOR)), SWAGGER_EDITOR);
    }

    @Test
    void logoutPreflightIsGrantedButLogoutWithoutATokenStillRevokesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("logout-preflight@example.com");

        mockMvc.perform(preflight(LOGOUT, WEB_CLIENT, "POST", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, WEB_CLIENT));
        expectReadableAuthenticationFailure(mockMvc.perform(post(LOGOUT).header(ORIGIN, WEB_CLIENT)), WEB_CLIENT);

        assertThat(countRevokedSessions()).isZero();
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());
    }

    @Test
    void optionsRequestThatIsNotAPreflightStillRequiresAuthentication() throws Exception {
        mockMvc.perform(options(PROTECTED_PROBE))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            UNLISTED_ORIGIN,
            "http://editor.swagger.io",
            "https://editor.swagger.io:8443",
            "https://editor.swagger.io.attacker.example",
            "https://sub.editor.swagger.io",
            "null"})
    void preflightFromAnyOtherOriginIsRefused(String origin) throws Exception {
        expectNoCorsGrant(mockMvc.perform(preflight(LOGIN, origin, "POST", "content-type")))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({
            "DELETE, authorization",
            "PUT, content-type",
            "PATCH, content-type",
            "POST, x-requested-with"})
    void preflightForAMethodOrHeaderTheApiDoesNotAcceptIsRefused(String method, String requestHeaders)
            throws Exception {
        expectNoCorsGrant(mockMvc.perform(preflight(LOGIN, SWAGGER_EDITOR, method, requestHeaders)))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualRequestFromAnUnlistedOriginIsRefusedBeforeItTakesEffect() throws Exception {
        expectNoCorsGrant(mockMvc.perform(crossOriginPost(REGISTRATIONS, UNLISTED_ORIGIN,
                        registrationBody("unlisted@example.com", PASSWORD, PASSWORD, null))))
                .andExpect(status().isForbidden());

        assertThat(countRows("identity.identity_account")).isZero();
    }

    @Test
    void unlistedOriginGetsNoAccessEvenWithAUsableToken() throws Exception {
        IssuedTokens tokens = registerAndLogIn("unlisted-with-token@example.com");

        expectNoCorsGrant(mockMvc.perform(crossOriginProbe(UNLISTED_ORIGIN, tokens.accessToken())))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString(tokens.accountId().toString()))));
    }

    @Test
    void usableSessionReachesTheProtectedOperationFromAnAllowedOrigin() throws Exception {
        registerAccount("cross-origin@example.com", PASSWORD);
        IssuedTokens tokens = tokensFrom(expectCorsGranted(mockMvc.perform(crossOriginPost(LOGIN, SWAGGER_EDITOR,
                Map.of("email", "cross-origin@example.com", "password", PASSWORD))), SWAGGER_EDITOR));

        expectCorsGranted(mockMvc.perform(crossOriginProbe(SWAGGER_EDITOR, tokens.accessToken())), SWAGGER_EDITOR)
                .andExpect(status().isOk())
                .andExpect(header().stringValues(VARY, hasItem(ORIGIN)))
                .andExpect(jsonPath("$.accountId").value(tokens.accountId().toString()));
    }

    @Test
    void malformedAccessTokenIsStillRejectedFromAnAllowedOrigin() throws Exception {
        expectReadableAuthenticationFailure(
                mockMvc.perform(crossOriginProbe(SWAGGER_EDITOR, "not-a-jwt")), SWAGGER_EDITOR);
    }

    @Test
    void expiredAccessTokenIsStillRejectedFromAnAllowedOrigin() throws Exception {
        IssuedTokens tokens = registerAndLogIn("expired-cross-origin@example.com");
        clock.advance(Duration.ofMinutes(15));

        expectReadableAuthenticationFailure(
                mockMvc.perform(crossOriginProbe(SWAGGER_EDITOR, tokens.accessToken())), SWAGGER_EDITOR);
    }

    @Test
    void logoutFromAnAllowedOriginRevokesTheSessionForEveryLaterRequest() throws Exception {
        IssuedTokens tokens = registerAndLogIn("logout-cross-origin@example.com");

        expectCorsGranted(mockMvc.perform(post(LOGOUT)
                        .header(ORIGIN, SWAGGER_EDITOR)
                        .header(AUTHORIZATION, "Bearer " + tokens.accessToken())), SWAGGER_EDITOR)
                .andExpect(status().isNoContent());

        assertThat(countRevokedSessions()).isOne();
        expectReadableAuthenticationFailure(
                mockMvc.perform(crossOriginProbe(SWAGGER_EDITOR, tokens.accessToken())), SWAGGER_EDITOR);
    }

    @Test
    void registrationAndItsErrorsAreReadableFromAnAllowedOrigin() throws Exception {
        expectCorsGranted(mockMvc.perform(crossOriginPost(REGISTRATIONS, WEB_CLIENT,
                        registrationBody("browser@example.com", PASSWORD, PASSWORD, null))), WEB_CLIENT)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("browser@example.com"));

        expectCorsGranted(mockMvc.perform(crossOriginPost(REGISTRATIONS, WEB_CLIENT,
                        registrationBody("browser@example.com", PASSWORD, PASSWORD, null))), WEB_CLIENT)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));

        expectCorsGranted(mockMvc.perform(crossOriginPost(REGISTRATIONS, WEB_CLIENT,
                        registrationBody("other@example.com", PASSWORD, "a different password", null))), WEB_CLIENT)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        assertThat(countRows("identity.identity_account")).isOne();
    }

    @Test
    void invalidCredentialsAreReadableFromAnAllowedOriginAndIssueNothing() throws Exception {
        registerAccount("wrong-password@example.com", PASSWORD);

        expectCorsGranted(mockMvc.perform(crossOriginPost(LOGIN, SWAGGER_EDITOR,
                        Map.of("email", "wrong-password@example.com", "password", "not the password"))), SWAGGER_EDITOR)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        assertThat(countRows("identity.identity_session")).isZero();
    }

    @Test
    void refreshRotationAndReuseDetectionWorkFromAnAllowedOrigin() throws Exception {
        IssuedTokens tokens = registerAndLogIn("rotation-cross-origin@example.com");

        IssuedTokens renewed = tokensFrom(expectCorsGranted(mockMvc.perform(crossOriginPost(REFRESH, WEB_CLIENT,
                Map.of("refreshToken", tokens.refreshToken()))), WEB_CLIENT));
        assertThat(renewed.accountId()).isEqualTo(tokens.accountId());
        assertThat(renewed.refreshToken()).isNotEqualTo(tokens.refreshToken());

        expectCorsGranted(mockMvc.perform(crossOriginPost(REFRESH, WEB_CLIENT,
                        Map.of("refreshToken", tokens.refreshToken()))), WEB_CLIENT)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        expectReadableAuthenticationFailure(
                mockMvc.perform(crossOriginProbe(WEB_CLIENT, renewed.accessToken())), WEB_CLIENT);
    }

    private static MockHttpServletRequestBuilder preflight(
            String path, String origin, String requestMethod, String requestHeaders) {
        return options(path)
                .header(ORIGIN, origin)
                .header(ACCESS_CONTROL_REQUEST_METHOD, requestMethod)
                .header(ACCESS_CONTROL_REQUEST_HEADERS, requestHeaders);
    }

    private static MockHttpServletRequestBuilder crossOriginPost(String path, String origin, Map<String, ?> body) {
        return post(path)
                .header(ORIGIN, origin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body));
    }

    private static MockHttpServletRequestBuilder crossOriginProbe(String origin, String accessToken) {
        return get(PROTECTED_PROBE)
                .header(ORIGIN, origin)
                .header(AUTHORIZATION, "Bearer " + accessToken);
    }

    /** The page on that origin may read the response; nothing else is granted. */
    private static ResultActions expectCorsGranted(ResultActions response, String origin) throws Exception {
        return response
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_CREDENTIALS))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_EXPOSE_HEADERS));
    }

    private static ResultActions expectNoCorsGrant(ResultActions response) throws Exception {
        return response
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().doesNotExist(ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    /** A 401 the page can read, so a browser client can send the user to log in again. */
    private static void expectReadableAuthenticationFailure(ResultActions response, String origin) throws Exception {
        expectCorsGranted(response, origin)
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.accountId").doesNotExist());
    }

    private long countRevokedSessions() {
        return jdbc.sql("SELECT count(*) FROM identity.identity_session WHERE revoked_at IS NOT NULL")
                .query(Long.class)
                .single();
    }
}
