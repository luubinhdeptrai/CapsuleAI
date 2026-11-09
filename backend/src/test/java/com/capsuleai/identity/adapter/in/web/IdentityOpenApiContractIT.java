package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.support.IdentityIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Keeps {@code docs/05-api/openapi.yaml} — the maintained HTTP contract — in
 * step with what the running application actually serves: the same operations,
 * the same public/protected split through the real filter chain, the same
 * request and response fields, and the documented required fields enforced.
 */
@SuppressWarnings("unchecked")
class IdentityOpenApiContractIT extends IdentityIntegrationTestSupport {

    private static final Path OPENAPI_SPECIFICATION = Path.of("..", "docs", "05-api", "openapi.yaml");
    private static final String SCHEMA_REFERENCE_PREFIX = "#/components/schemas/";
    private static final Set<String> HTTP_METHODS = Set.of("get", "put", "post", "delete", "patch");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private Map<String, Object> specification;
    private Map<String, DocumentedOperation> documentedOperations;
    private Map<String, HandlerMethod> implementedOperations;
    private String contractAccessToken;

    /** One documented path + method, keyed like {@code "POST /api/v1/auth/login"}. */
    private record DocumentedOperation(String method, String path, Map<String, Object> definition) {

        String key() {
            return method.toUpperCase(Locale.ROOT) + " " + path;
        }

        boolean isPublic() {
            return definition.containsKey("security") && ((List<?>) definition.get("security")).isEmpty();
        }
    }

    @BeforeEach
    void loadContractAndHandlers() throws IOException {
        assertThat(OPENAPI_SPECIFICATION).as("OpenAPI specification location").isRegularFile();
        try (Reader reader = Files.newBufferedReader(OPENAPI_SPECIFICATION, StandardCharsets.UTF_8)) {
            specification = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
        }

        documentedOperations = new LinkedHashMap<>();
        map(specification, "paths").forEach((path, pathItem) -> ((Map<String, Object>) pathItem)
                .forEach((method, definition) -> {
                    if (HTTP_METHODS.contains(method)) {
                        DocumentedOperation operation =
                                new DocumentedOperation(method, path, (Map<String, Object>) definition);
                        documentedOperations.put(operation.key(), operation);
                    }
                }));

        implementedOperations = new LinkedHashMap<>();
        requestMappingHandlerMapping.getHandlerMethods().forEach((mapping, handlerMethod) -> {
            for (String pattern : patternsOf(mapping)) {
                if (pattern.startsWith("/api/")) {
                    mapping.getMethodsCondition().getMethods().forEach(method ->
                            implementedOperations.put(method.name() + " " + pattern, handlerMethod));
                }
            }
        });
    }

    @Test
    void everyDocumentedOperationIsServedAndEveryApiHandlerIsDocumented() {
        assertThat(implementedOperations.keySet())
                .as("operations served under /api/ versus operations documented in openapi.yaml")
                .containsExactlyInAnyOrderElementsOf(documentedOperations.keySet());
    }

    @Test
    void documentedSecurityMatchesTheConfiguredFilterChain() throws Exception {
        for (DocumentedOperation operation : documentedOperations.values()) {
            MockHttpServletResponse response = perform(operation, Map.of());

            if (operation.isPublic()) {
                assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE))
                        .as("%s is documented as public, so no bearer challenge", operation.key())
                        .isNull();
                assertThat(map(operation.definition(), "responses"))
                        .as("%s answered without a token with a documented status", operation.key())
                        .containsKey(String.valueOf(response.getStatus()));
            } else {
                assertThat(response.getStatus())
                        .as("%s is documented as protected, so a missing token is rejected", operation.key())
                        .isEqualTo(401);
                assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer");
                assertThat(map(operation.definition(), "responses")).containsKey("401");
            }
        }
    }

    @Test
    void requestSchemasListExactlyTheFieldsTheEndpointsRead() {
        for (DocumentedOperation operation : operationsWithRequestBodies()) {
            Map<String, Object> schema = requestSchemaOf(operation);
            Class<?> requestType = requestBodyTypeOf(handlerFor(operation));

            assertThat(map(schema, "properties").keySet())
                    .as("%s request schema versus %s", operation.key(), requestType.getSimpleName())
                    .containsExactlyInAnyOrderElementsOf(recordComponentNames(requestType));
            assertThat(map(schema, "properties").keySet()).containsAll(requiredFields(schema));
        }
    }

    @Test
    void successResponseSchemasListAndRequireExactlyTheFieldsTheEndpointsReturn() {
        for (DocumentedOperation operation : documentedOperations.values()) {
            Map<String, Object> success = successResponseOf(operation);
            Type responseType = responseBodyTypeOf(handlerFor(operation));

            if (!success.containsKey("content")) {
                assertThat(responseType).as("%s documents no response body", operation.key()).isEqualTo(Void.class);
                continue;
            }
            Map<String, Object> schema = resolve(map(map(map(success, "content"), "application/json"), "schema"));
            Set<String> returnedFields = recordComponentNames((Class<?>) responseType);

            assertThat(map(schema, "properties").keySet())
                    .as("%s response schema versus %s", operation.key(), responseType.getTypeName())
                    .containsExactlyInAnyOrderElementsOf(returnedFields);
            assertThat(requiredFields(schema))
                    .as("%s: every returned field is always present (null when empty)", operation.key())
                    .containsExactlyInAnyOrderElementsOf(returnedFields);
        }
    }

    @Test
    void everyDocumentedRequiredRequestFieldIsEnforced() throws Exception {
        for (DocumentedOperation operation : operationsWithRequestBodies()) {
            Map<String, Object> documentedExample = requestExampleOf(operation);

            for (String requiredField : requiredFields(requestSchemaOf(operation))) {
                Map<String, Object> withoutRequiredField = new LinkedHashMap<>(documentedExample);
                withoutRequiredField.remove(requiredField);

                MockHttpServletResponse response = operation.isPublic()
                        ? perform(operation, withoutRequiredField)
                        : performAuthenticated(operation, withoutRequiredField);
                String body = response.getContentAsString(StandardCharsets.UTF_8);

                assertThat(response.getStatus())
                        .as("%s without required %s", operation.key(), requiredField)
                        .isEqualTo(400);
                assertThat(JsonPath.<String>read(body, "$.code")).isEqualTo("VALIDATION_FAILED");
                assertThat(JsonPath.<List<String>>read(body,
                        "$.fieldErrors[?(@.field == '" + requiredField + "')].code"))
                        .as("%s reports %s as REQUIRED", operation.key(), requiredField)
                        .containsExactly("REQUIRED");
            }
        }
    }

    private MockHttpServletResponse perform(DocumentedOperation operation, Map<String, Object> body) throws Exception {
        return mockMvc.perform(request(HttpMethod.valueOf(operation.method().toUpperCase(Locale.ROOT)),
                                concretePath(operation))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(body)))
                .andReturn()
                .getResponse();
    }

    /**
     * Calls a protected operation as a freshly registered user with a new
     * Idempotency-Key, so that only the request body is under test.
     */
    private MockHttpServletResponse performAuthenticated(DocumentedOperation operation, Map<String, Object> body)
            throws Exception {
        if (contractAccessToken == null) {
            contractAccessToken = registerAndLogIn("openapi-contract@example.com").accessToken();
        }
        return mockMvc.perform(request(HttpMethod.valueOf(operation.method().toUpperCase(Locale.ROOT)),
                                concretePath(operation))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + contractAccessToken)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(body)))
                .andReturn()
                .getResponse();
    }

    /** Fills each path parameter such as {garmentId} with a random UUID that names no resource. */
    private static String concretePath(DocumentedOperation operation) {
        return operation.path().replaceAll("\\{[^}]+}", UUID.randomUUID().toString());
    }

    private HandlerMethod handlerFor(DocumentedOperation operation) {
        HandlerMethod handlerMethod = implementedOperations.get(operation.key());
        assertThat(handlerMethod).as("%s is documented but not served", operation.key()).isNotNull();
        return handlerMethod;
    }

    private List<DocumentedOperation> operationsWithRequestBodies() {
        List<DocumentedOperation> withBodies = documentedOperations.values().stream()
                .filter(operation -> operation.definition().containsKey("requestBody"))
                .toList();
        assertThat(withBodies).as("documented operations with a request body").isNotEmpty();
        return withBodies;
    }

    private Map<String, Object> requestSchemaOf(DocumentedOperation operation) {
        return resolve(map(requestJsonContentOf(operation), "schema"));
    }

    private static Map<String, Object> requestExampleOf(DocumentedOperation operation) {
        Map<String, Object> json = requestJsonContentOf(operation);
        if (json.containsKey("example")) {
            return map(json, "example");
        }
        Map<String, Object> firstNamedExample = (Map<String, Object>) map(json, "examples").values().iterator().next();
        return map(firstNamedExample, "value");
    }

    private static Map<String, Object> requestJsonContentOf(DocumentedOperation operation) {
        return map(map(map(operation.definition(), "requestBody"), "content"), "application/json");
    }

    private static Map<String, Object> successResponseOf(DocumentedOperation operation) {
        return map(operation.definition(), "responses").entrySet().stream()
                .filter(response -> response.getKey().startsWith("2"))
                .map(response -> (Map<String, Object>) response.getValue())
                .findFirst()
                .orElseThrow(() -> new AssertionError(operation.key() + " documents no success response"));
    }

    private Map<String, Object> resolve(Map<String, Object> schemaOrReference) {
        Object reference = schemaOrReference.get("$ref");
        if (reference == null) {
            return schemaOrReference;
        }
        String schemaName = ((String) reference).substring(SCHEMA_REFERENCE_PREFIX.length());
        return map(map(map(specification, "components"), "schemas"), schemaName);
    }

    private static Class<?> requestBodyTypeOf(HandlerMethod handlerMethod) {
        return Arrays.stream(handlerMethod.getMethodParameters())
                .filter(parameter -> parameter.hasParameterAnnotation(RequestBody.class))
                .map(MethodParameter::getParameterType)
                .findFirst()
                .orElseThrow(() -> new AssertionError(handlerMethod + " reads no request body"));
    }

    private static Type responseBodyTypeOf(HandlerMethod handlerMethod) {
        Type returnType = handlerMethod.getMethod().getGenericReturnType();
        if (returnType instanceof ParameterizedType parameterized && parameterized.getRawType() == ResponseEntity.class) {
            return parameterized.getActualTypeArguments()[0];
        }
        return returnType;
    }

    private static Set<String> recordComponentNames(Class<?> type) {
        assertThat(type.isRecord()).as("%s is a JSON record", type.getSimpleName()).isTrue();
        return Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());
    }

    private static List<String> requiredFields(Map<String, Object> schema) {
        return schema.containsKey("required") ? (List<String>) schema.get("required") : List.of();
    }

    private static Set<String> patternsOf(RequestMappingInfo mapping) {
        return mapping.getPathPatternsCondition() != null
                ? mapping.getPathPatternsCondition().getPatternValues()
                : mapping.getPatternValues();
    }

    private static Map<String, Object> map(Map<String, Object> parent, String key) {
        Object child = parent.get(key);
        assertThat(child).as("OpenAPI element '%s'", key).isInstanceOf(Map.class);
        return (Map<String, Object>) child;
    }
}
