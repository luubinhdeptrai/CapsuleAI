package com.capsuleai.identity.configuration;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PasswordResetPropertiesBinding.class);

    @EnableConfigurationProperties(PasswordResetProperties.class)
    static class PasswordResetPropertiesBinding {
    }

    @Test
    void validSettingsBind() {
        contextRunner
                .withPropertyValues(
                        "capsuleai.identity.password-reset.sender-address=no-reply@capsuleai.example",
                        "capsuleai.identity.password-reset.instruction-link=https://app.capsuleai.example/reset")
                .run(context -> {
                    PasswordResetProperties properties = context.getBean(PasswordResetProperties.class);
                    assertThat(properties.senderAddress()).isEqualTo("no-reply@capsuleai.example");
                    assertThat(properties.instructionLink()).isEqualTo(URI.create("https://app.capsuleai.example/reset"));
                });
    }

    @Test
    void missingSettingsStopStartup() {
        contextRunner.run(context -> assertThat(context).hasFailed()
                .getFailure()
                .rootCause()
                .hasMessageContaining("sender-address is not configured"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"reset-password", "capsuleai://reset-password#already", "https://user@app.example/reset"})
    void aLinkThatCannotCarryTheSecretFragmentIsRejected(String instructionLink) {
        assertThatThrownBy(() -> new PasswordResetProperties("no-reply@capsuleai.example", URI.create(instructionLink), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instruction-link must be an absolute link");
    }

    /** OPEN-S2-02: production links are verified HTTPS App Links / Universal Links. */
    @Test
    void anHttpsLinkIsAcceptedWithoutTheDevelopmentFlag() {
        PasswordResetProperties properties = new PasswordResetProperties(
                "no-reply@capsuleai.example", URI.create("https://capsuleai.test/reset-password"), false);

        assertThat(properties.instructionLink().getScheme()).isEqualTo("https");
    }

    @Test
    void aCustomSchemeIsRejectedUnlessDevelopmentAllowsIt() {
        URI customScheme = URI.create("capsuleai://reset-password");

        assertThatThrownBy(() -> new PasswordResetProperties("no-reply@capsuleai.example", customScheme, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must use https");
        assertThat(new PasswordResetProperties("no-reply@capsuleai.example", customScheme, true).instructionLink())
                .isEqualTo(customScheme);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void plainHttpIsNeverAccepted(boolean developmentCustomSchemeAllowed) {
        assertThatThrownBy(() -> new PasswordResetProperties("no-reply@capsuleai.example",
                URI.create("http://capsuleai.test/reset-password"), developmentCustomSchemeAllowed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must use https");
    }

    @Test
    void aSenderThatIsNotAnEmailAddressIsRejected() {
        assertThatThrownBy(() -> new PasswordResetProperties("CapsuleAI", URI.create("https://capsuleai.test/reset"), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sender-address must be an email address");
    }
}
