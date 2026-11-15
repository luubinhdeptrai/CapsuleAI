package com.capsuleai.wardrobe.support;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    protected static final String PHOTO_UPLOADS = "/api/v1/wardrobe/photo-uploads";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    protected FaultInjectingPhotoStorage photoStorage;

    @BeforeEach
    protected void resetWardrobeState() {
        jdbc.sql("TRUNCATE TABLE wardrobe.wardrobe_photo_upload, wardrobe.wardrobe_garment_material, "
                + "wardrobe.wardrobe_garment_style, wardrobe.wardrobe_garment").update();
        photoStorage.reset();
    }

    /** US-GAR-003: stages one photo as a multipart upload. */
    protected ResultActions uploadPhoto(String accessToken, String uploadKey, byte[] bytes, String fileName,
                                        String declaredType) throws Exception {
        // Spring Framework 7: the multipart builder is not a MockHttpServletRequestBuilder subtype.
        MockMultipartHttpServletRequestBuilder request = multipart(PHOTO_UPLOADS)
                .file(new MockMultipartFile("photo", fileName, declaredType, bytes))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        if (uploadKey != null) {
            request.header(IDEMPOTENCY_KEY, uploadKey);
        }
        return mockMvc.perform(request);
    }

    /** Stages a photo as a new upload, expects 201 and returns its id. */
    protected UUID stagePhotoAndReturnId(String accessToken, byte[] bytes) throws Exception {
        String response = uploadPhoto(accessToken, UUID.randomUUID().toString(), bytes, "garment.jpg", "image/jpeg")
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(response, "$.id"));
    }

    protected String photoUploadStatus(UUID photoUploadId) {
        return jdbc.sql("SELECT status FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(String.class).single();
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
