package com.capsuleai.identity.support;

import com.capsuleai.CapsuleAiApplication;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for Identity integration tests: real application and security
 * chain, real PostgreSQL (Testcontainers), MockMvc, controllable clock.
 */
@SpringBootTest(classes = {CapsuleAiApplication.class, IdentityTestConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IdentityIntegrationTestSupport {

    protected static final String REGISTRATIONS = "/api/v1/auth/registrations";
    protected static final String LOGIN = "/api/v1/auth/login";
    protected static final String PROTECTED_PROBE = IdentityTestConfiguration.ProtectedProbeController.PATH;
    protected static final String PASSWORD = "correct horse battery staple";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Tokens returned by a TokenResponse body. */
    public record IssuedTokens(UUID accountId, String accessToken, String refreshToken) {
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    protected MutableClock clock;

    @BeforeEach
    protected void resetIdentityState() {
        clock.setInstant(MutableClock.DEFAULT_START);
        // Every table Identity owns lives in its own schema (AR-01), so this also
        // covers tables added by later Identity migrations.
        List<String> identityTables = jdbc.sql("""
                        SELECT format('%I.%I', schemaname, tablename)
                        FROM pg_tables
                        WHERE schemaname = 'identity'
                        """)
                .query(String.class)
                .list();
        if (!identityTables.isEmpty()) {
            jdbc.sql("TRUNCATE TABLE " + String.join(", ", identityTables) + " CASCADE").update();
        }
    }

    protected ResultActions postJson(String path, Map<String, ?> body) throws Exception {
        return postJson(path, body, null);
    }

    protected ResultActions postJson(String path, Map<String, ?> body, String accessToken) throws Exception {
        MockHttpServletRequestBuilder request = post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body));
        if (accessToken != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        return mockMvc.perform(request);
    }

    protected ResultActions getProtectedProbe(String accessToken) throws Exception {
        return mockMvc.perform(get(PROTECTED_PROBE).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    protected ResultActions register(String email, String password) throws Exception {
        return postJson(REGISTRATIONS, registrationBody(email, password, password, null));
    }

    protected static Map<String, Object> registrationBody(
            String email, String password, String confirmPassword, String displayName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("confirmPassword", confirmPassword);
        body.put("displayName", displayName);
        return body;
    }

    protected UUID registerAccount(String email, String password) throws Exception {
        String body = register(email, password)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(body, "$.accountId"));
    }

    protected IssuedTokens logIn(String email, String password) throws Exception {
        return tokensFrom(postJson(LOGIN, Map.of("email", email, "password", password)));
    }

    protected IssuedTokens registerAndLogIn(String email) throws Exception {
        registerAccount(email, PASSWORD);
        return logIn(email, PASSWORD);
    }

    /** Expects HTTP 200 and reads a TokenResponse body. */
    protected static IssuedTokens tokensFrom(ResultActions response) throws Exception {
        String body = response
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return new IssuedTokens(
                UUID.fromString(JsonPath.read(body, "$.accountId")),
                JsonPath.read(body, "$.accessToken"),
                JsonPath.read(body, "$.refreshToken"));
    }

    /** The session id the real security chain resolved for this token (via the test-only probe). */
    protected UUID sessionIdOf(String accessToken) throws Exception {
        String body = getProtectedProbe(accessToken)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(body, "$.sessionId"));
    }

    protected long countRows(String table) {
        return jdbc.sql("SELECT count(*) FROM " + table).query(Long.class).single();
    }
}
