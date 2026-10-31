package com.capsuleai.identity.support;

import com.capsuleai.CapsuleAiApplication;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base class for Identity integration tests: real application, real
 * PostgreSQL (Testcontainers), MockMvc, controllable clock.
 */
@SpringBootTest(classes = {CapsuleAiApplication.class, IdentityTestConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IdentityIntegrationTestSupport {

    protected static final String REGISTRATIONS = "/api/v1/auth/registrations";
    protected static final String PASSWORD = "correct horse battery staple";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcClient jdbc;

    @Autowired
    protected MutableClock clock;

    @BeforeEach
    protected void resetIdentityState() {
        clock.setInstant(MutableClock.DEFAULT_START);
        List<String> identityTables = jdbc.sql("""
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'public' AND tablename LIKE 'identity%'
                        """)
                .query(String.class)
                .list();
        if (!identityTables.isEmpty()) {
            jdbc.sql("TRUNCATE TABLE " + String.join(", ", identityTables) + " CASCADE").update();
        }
    }

    protected ResultActions postJson(String path, Map<String, ?> body) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
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

    protected long countRows(String table) {
        return jdbc.sql("SELECT count(*) FROM " + table).query(Long.class).single();
    }
}