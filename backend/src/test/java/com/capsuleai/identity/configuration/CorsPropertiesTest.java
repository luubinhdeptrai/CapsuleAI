package com.capsuleai.identity.configuration;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CorsPropertiesBinding.class);

    @EnableConfigurationProperties(CorsProperties.class)
    static class CorsPropertiesBinding {
    }

    @Test
    void commaSeparatedOriginsBindInOrder() {
        contextRunner
                .withPropertyValues("capsuleai.cors.allowed-origins=https://editor.swagger.io, https://web-client.example")
                .run(context -> assertThat(context.getBean(CorsProperties.class).allowedOrigins())
                        .containsExactly("https://editor.swagger.io", "https://web-client.example"));
    }

    /** What application.properties resolves to when CAPSULE_CORS_ALLOWED_ORIGINS is not set. */
    @Test
    void emptyValueAllowsNoOrigin() {
        contextRunner
                .withPropertyValues("capsuleai.cors.allowed-origins=")
                .run(context -> assertThat(context.getBean(CorsProperties.class).allowedOrigins()).isEmpty());
    }

    @Test
    void absentPropertyAllowsNoOrigin() {
        contextRunner.run(context -> assertThat(context.getBean(CorsProperties.class).allowedOrigins()).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://editor.swagger.io",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "https://app.example.com:8443",
            "http://[::1]:5173"})
    void exactOriginsAreAccepted(String allowedOrigin) {
        assertThat(new CorsProperties(List.of(allowedOrigin)).allowedOrigins()).containsExactly(allowedOrigin);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "*",
            "https://*.swagger.io",
            "http://localhost:*",
            "null",
            "editor.swagger.io",
            "https://editor.swagger.io/",
            "https://editor.swagger.io/api",
            "https://editor.swagger.io?debug",
            "https://editor.swagger.io#top",
            "https://user@editor.swagger.io",
            "ftp://editor.swagger.io",
            "https://editor.swagger.io:443",
            "http://localhost:80",
            ""})
    void anythingOtherThanAnExactOriginIsRejected(String allowedOrigin) {
        assertThatThrownBy(() -> new CorsProperties(List.of(allowedOrigin)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not an exact origin");
    }

    @Test
    void invalidOriginStopsStartupInsteadOfBeingIgnored() {
        contextRunner
                .withPropertyValues("capsuleai.cors.allowed-origins=https://editor.swagger.io,*")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .rootCause()
                        .hasMessageContaining("entry '*' is not an exact origin"));
    }
}
