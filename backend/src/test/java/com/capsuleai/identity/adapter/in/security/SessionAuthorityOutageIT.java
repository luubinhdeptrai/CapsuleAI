package com.capsuleai.identity.adapter.in.security;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TD-26 through the real filter chain, persistence adapters and PostgreSQL: when
 * the database cannot supply a connection, session authority cannot be checked,
 * so protected access fails closed with a sanitized 503 — not a 401 that would
 * misreport the credentials, not a 500 and never a successful response. Tokens
 * that fail verification are still 401, and access resumes once the database does.
 */
@Import(SessionAuthorityOutageIT.DatabaseConnectionOutage.class)
@ExtendWith(OutputCaptureExtension.class)
class SessionAuthorityOutageIT extends IdentityIntegrationTestSupport {

    private static final String LOGOUT = "/api/v1/auth/logout";
    private static final String REFRESH = "/api/v1/auth/refresh";

    @Autowired
    private DatabaseConnectionOutage databaseConnectionOutage;

    /**
     * TEST-ONLY. Wraps the application's real DataSource so it can refuse every
     * connection on demand with the exception HikariCP throws when it cannot
     * supply one. Everything above the connection — JdbcClient, the session
     * repository, SessionAuthority, the converter and the entry point — is real.
     */
    static class DatabaseConnectionOutage implements BeanPostProcessor {

        private final AtomicBoolean active = new AtomicBoolean();

        void begin() {
            active.set(true);
        }

        void end() {
            active.set(false);
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (!(bean instanceof DataSource dataSource)) {
                return bean;
            }
            return new DelegatingDataSource(dataSource) {
                @Override
                public Connection getConnection() throws SQLException {
                    refuseDuringOutage();
                    return super.getConnection();
                }

                @Override
                public Connection getConnection(String username, String password) throws SQLException {
                    refuseDuringOutage();
                    return super.getConnection(username, password);
                }
            };
        }

        private void refuseDuringOutage() throws SQLTransientConnectionException {
            if (active.get()) {
                throw new SQLTransientConnectionException(
                        "HikariPool-1 - Connection is not available, request timed out after 30000ms (simulated outage)");
            }
        }
    }

    @AfterEach
    void restoreDatabaseConnections() {
        databaseConnectionOutage.end();
    }

    @Test
    void usableSessionReachesTheProtectedOperationWhileTheDatabaseIsReachable() throws Exception {
        IssuedTokens tokens = registerAndLogIn("reachable@example.com");

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(tokens.accountId().toString()));
    }

    @Test
    void unreachableDatabaseFailsClosedWithASanitized503(CapturedOutput output) throws Exception {
        IssuedTokens tokens = registerAndLogIn("outage@example.com");
        databaseConnectionOutage.begin();

        String body = expectTemporarilyUnavailable(getProtectedProbe(tokens.accessToken()));

        Map<String, Object> problem = JsonPath.read(body, "$");
        assertThat(problem).containsOnlyKeys("type", "title", "status", "detail", "code");
        assertThat(problem).containsEntry("detail",
                "Your access could not be verified right now. Nothing was changed. Try again shortly.");
        assertThat(body).doesNotContain(
                tokens.accessToken(), tokens.accountId().toString(), "JDBC", "Hikari", "simulated");
        assertThat(output.getAll())
                .contains("Session authority check failed because the database is unreachable")
                .doesNotContain(tokens.accessToken())
                .doesNotContain(tokens.refreshToken());
    }

    @Test
    void accessResumesForTheSameSessionWhenTheDatabaseRecovers() throws Exception {
        IssuedTokens tokens = registerAndLogIn("recovers@example.com");
        UUID sessionId = sessionIdOf(tokens.accessToken());

        databaseConnectionOutage.begin();
        expectTemporarilyUnavailable(getProtectedProbe(tokens.accessToken()));
        databaseConnectionOutage.end();

        getProtectedProbe(tokens.accessToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(tokens.accountId().toString()))
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()));
        assertThat(countRevokedSessions()).isZero();
    }

    @Test
    void logoutDuringAnOutageIsNotConfirmedAndRevokesNothing() throws Exception {
        IssuedTokens tokens = registerAndLogIn("logout-outage@example.com");

        databaseConnectionOutage.begin();
        expectTemporarilyUnavailable(logOut(tokens.accessToken()));
        databaseConnectionOutage.end();

        assertThat(countRevokedSessions()).isZero();
        getProtectedProbe(tokens.accessToken()).andExpect(status().isOk());
    }

    @Test
    void revokedSessionIsNeverAuthorizedBeforeDuringOrAfterAnOutage() throws Exception {
        IssuedTokens tokens = registerAndLogIn("revoked@example.com");
        logOut(tokens.accessToken()).andExpect(status().isNoContent());
        expectAuthenticationRequired(getProtectedProbe(tokens.accessToken()));

        databaseConnectionOutage.begin();
        expectTemporarilyUnavailable(getProtectedProbe(tokens.accessToken()));
        databaseConnectionOutage.end();

        expectAuthenticationRequired(getProtectedProbe(tokens.accessToken()));
    }

    @Test
    void missingAccessTokenIsStillAnAuthenticationFailureDuringAnOutage() throws Exception {
        databaseConnectionOutage.begin();

        expectAuthenticationRequired(mockMvc.perform(get(PROTECTED_PROBE)));
    }

    @Test
    void malformedAccessTokenIsStillAnAuthenticationFailureDuringAnOutage() throws Exception {
        databaseConnectionOutage.begin();

        expectAuthenticationRequired(getProtectedProbe("not-a-jwt"));
    }

    @Test
    void tamperedAccessTokenIsStillAnAuthenticationFailureDuringAnOutage() throws Exception {
        IssuedTokens owner = registerAndLogIn("owner@example.com");
        IssuedTokens other = registerAndLogIn("other@example.com");
        String ownerClaimsWithAnotherTokensSignature =
                owner.accessToken().substring(0, owner.accessToken().lastIndexOf('.'))
                        + other.accessToken().substring(other.accessToken().lastIndexOf('.'));

        databaseConnectionOutage.begin();

        expectAuthenticationRequired(getProtectedProbe(ownerClaimsWithAnotherTokensSignature));
    }

    @Test
    void expiredAccessTokenIsStillAnAuthenticationFailureDuringAnOutage() throws Exception {
        IssuedTokens tokens = registerAndLogIn("expired@example.com");
        clock.advance(Duration.ofMinutes(15));

        databaseConnectionOutage.begin();

        expectAuthenticationRequired(getProtectedProbe(tokens.accessToken()));
    }

    @Test
    void loginDuringAnOutageIsUnavailableRatherThanInvalidCredentials() throws Exception {
        registerAccount("login-outage@example.com", PASSWORD);

        databaseConnectionOutage.begin();
        postJson(LOGIN, Map.of("email", "login-outage@example.com", "password", PASSWORD))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
        databaseConnectionOutage.end();

        assertThat(countRows("identity.identity_session")).isZero();
    }

    @Test
    void refreshDuringAnOutageIsUnavailableAndLeavesTheRefreshTokenCurrent() throws Exception {
        IssuedTokens tokens = registerAndLogIn("refresh-outage@example.com");

        databaseConnectionOutage.begin();
        postJson(REFRESH, Map.of("refreshToken", tokens.refreshToken()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
        databaseConnectionOutage.end();

        IssuedTokens renewed = tokensFrom(postJson(REFRESH, Map.of("refreshToken", tokens.refreshToken())));
        assertThat(renewed.accountId()).isEqualTo(tokens.accountId());
        assertThat(countRevokedSessions()).isZero();
    }

    private ResultActions logOut(String accessToken) throws Exception {
        return mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    /** Asserts the fixed outage problem; returns the body for further checks. */
    private static String expectTemporarilyUnavailable(ResultActions response) throws Exception {
        return response
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.code").value("TEMPORARILY_UNAVAILABLE"))
                .andExpect(jsonPath("$.accountId").doesNotExist())
                .andExpect(jsonPath("$.sessionId").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private static void expectAuthenticationRequired(ResultActions response) throws Exception {
        response
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, startsWith("Bearer")))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.accountId").doesNotExist());
    }

    private long countRevokedSessions() {
        return jdbc.sql("SELECT count(*) FROM identity.identity_session WHERE revoked_at IS NOT NULL")
                .query(Long.class)
                .single();
    }
}
