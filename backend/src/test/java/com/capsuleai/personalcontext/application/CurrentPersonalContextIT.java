package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.api.DeclaredPersonalContext;
import com.capsuleai.personalcontext.support.PersonalContextIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-PROF-001 (S3-PROF-05): the published current-context query, as a future consumer would use it. */
class CurrentPersonalContextIT extends PersonalContextIntegrationTestSupport {

    @Autowired
    private CurrentPersonalContext currentPersonalContext;

    @Test
    void nothingDeclaredIsEmptyRatherThanAnInventedPersona() throws Exception {
        IssuedTokens tokens = registerAndLogIn("context-empty@example.com");

        assertThat(currentPersonalContext.find(tokens.accountId())).isEmpty();
    }

    @Test
    void theCurrentContextIsTheLatestAcceptedDeclarationIncludingExplicitZero() throws Exception {
        IssuedTokens tokens = registerAndLogIn("context-current@example.com");
        revisePreferences(tokens.accessToken(), Map.of("expectedRevision", "", "styles", List.of()))
                .andExpect(status().isBadRequest());
        java.util.Map<String, Object> first = new java.util.LinkedHashMap<>();
        first.put("expectedRevision", null);
        first.put("styles", List.of("MINIMAL"));
        first.put("commonNeeds", Map.of("TRAVEL", 0, "WORK_PROFESSIONAL", 3));
        revisePreferences(tokens.accessToken(), first).andExpect(status().isOk());
        revisePreferences(tokens.accessToken(), Map.of("expectedRevision", "1", "commonNeeds", Map.of("WORK_PROFESSIONAL", 2)))
                .andExpect(status().isOk());

        DeclaredPersonalContext context = currentPersonalContext.find(tokens.accountId()).orElseThrow();

        assertThat(context.revision()).isEqualTo("2");
        assertThat(context.styleCodes()).containsExactly("MINIMAL");
        assertThat(context.needPriorities()).containsExactlyInAnyOrderEntriesOf(Map.of("TRAVEL", 0, "WORK_PROFESSIONAL", 2));
        assertThat(context.needPriorities()).doesNotContainKey("SCHOOL_UNIVERSITY");
    }
}
