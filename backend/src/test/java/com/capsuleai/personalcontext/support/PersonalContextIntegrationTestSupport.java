package com.capsuleai.personalcontext.support;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Base class for Personal Context integration tests: the shared real application, filter chain and
 * PostgreSQL, with the personal_context tables emptied before every test (no FK into Identity, so
 * Identity's cleanup does not reach them).
 */
public abstract class PersonalContextIntegrationTestSupport extends IdentityIntegrationTestSupport {

    protected static final String PREFERENCES = "/api/v1/personal-context/preferences";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @BeforeEach
    protected void resetPersonalContextState() {
        jdbc.sql("TRUNCATE TABLE personal_context.personal_context_common_need, "
                + "personal_context.personal_context_preferred_style, personal_context.personal_context_preferences")
                .update();
    }

    protected ResultActions viewPreferences(String accessToken) throws Exception {
        return mockMvc.perform(get(PREFERENCES).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    protected ResultActions revisePreferences(String accessToken, Map<String, ?> body) throws Exception {
        return mockMvc.perform(patch(PREFERENCES)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body)));
    }
}
