package com.capsuleai.wardrobe.support;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for Wardrobe integration tests: the same real application, filter
 * chain, PostgreSQL container and controllable clock as Identity's tests, plus
 * helpers that add garments through the real protected API.
 *
 * <p>Wardrobe tables have no foreign key into Identity (ADA-016), so Identity's
 * cleanup does not cascade into them; they are emptied here before every test.
 */
public abstract class WardrobeIntegrationTestSupport extends IdentityIntegrationTestSupport {

    protected static final String GARMENTS = "/api/v1/wardrobe/garments";
    protected static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @BeforeEach
    protected void resetWardrobeState() {
        jdbc.sql("TRUNCATE TABLE wardrobe.wardrobe_garment_material, wardrobe.wardrobe_garment_style, "
                + "wardrobe.wardrobe_garment").update();
    }

    /** POSTs one addition with the given logical-action key. */
    protected ResultActions addGarment(String accessToken, String idempotencyKey, Map<String, ?> body)
            throws Exception {
        MockHttpServletRequestBuilder request = post(GARMENTS)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(body));
        if (idempotencyKey != null) {
            request.header(IDEMPOTENCY_KEY, idempotencyKey);
        }
        return mockMvc.perform(request);
    }

    /** Adds a garment as a new logical addition, expects 201 and returns its id. */
    protected UUID addGarmentAndReturnId(String accessToken, Map<String, ?> body) throws Exception {
        String response = addGarment(accessToken, UUID.randomUUID().toString(), body)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(response, "$.id"));
    }

    /** The confirmed Saveable minimum: Category and one dominant Color. */
    protected static Map<String, Object> minimumGarment(String category, String color) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("confirmed", true);
        body.put("category", category);
        body.put("color", color);
        return body;
    }

    /** The minimum plus extra fields, given as alternating name/value pairs. */
    protected static Map<String, Object> garment(String category, String color, Object... extraNamesAndValues) {
        Map<String, Object> body = minimumGarment(category, color);
        for (int index = 0; index < extraNamesAndValues.length; index += 2) {
            body.put((String) extraNamesAndValues[index], extraNamesAndValues[index + 1]);
        }
        return body;
    }

    protected long countGarmentsOf(UUID ownerAccountId) {
        return jdbc.sql("SELECT count(*) FROM wardrobe.wardrobe_garment WHERE owner_account_id = :owner")
                .param("owner", ownerAccountId)
                .query(Long.class)
                .single();
    }
}
