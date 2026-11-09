package com.capsuleai.identity.adapter.out.email;

import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import com.capsuleai.identity.domain.EmailAddress;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Real SMTP delivery of reset instructions to a disposable Mailpit server
 * (US-AUTH-005 AC-02/AC-03, SI-003). This is the actual production adapter and
 * protocol; only the mail server is a local stand-in for the chosen provider.
 */
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class SmtpPasswordResetInstructionSenderIT {

    private static final int SMTP_PORT = 1025;
    private static final int API_PORT = 8025;
    private static final URI INSTRUCTION_LINK = URI.create("capsuleai://reset-password");
    private static final PasswordResetInstructions INSTRUCTIONS = new PasswordResetInstructions(
            new EmailAddress("person@example.com"), "real-smtp-reset-secret", Instant.parse("2026-01-05T08:30:00Z"));

    @Container
    private static final GenericContainer<?> MAILPIT = new GenericContainer<>("axllent/mailpit:v1.27.11")
            .withExposedPorts(SMTP_PORT, API_PORT)
            .waitingFor(Wait.forHttp("/api/v1/info").forPort(API_PORT));

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void emptyTheMailbox() throws Exception {
        http.send(HttpRequest.newBuilder(mailpitApi("/api/v1/messages")).DELETE().build(),
                HttpResponse.BodyHandlers.discarding());
    }

    @Test
    void instructionsArriveAsOnePlainTextMessageToTheAccountEmailOnly() throws Exception {
        sender(mailServerAt(MAILPIT.getMappedPort(SMTP_PORT))).sendInstructions(INSTRUCTIONS);

        String listing = get("/api/v1/messages");
        assertThat(JsonPath.<Integer>read(listing, "$.messages.length()")).isOne();
        String message = get("/api/v1/message/" + JsonPath.<String>read(listing, "$.messages[0].ID"));

        assertThat(JsonPath.<List<String>>read(message, "$.To[*].Address")).containsExactly("person@example.com");
        assertThat(JsonPath.<List<String>>read(message, "$.Cc[*].Address")).isEmpty();
        assertThat(JsonPath.<List<String>>read(message, "$.Bcc[*]")).isEmpty();
        assertThat(JsonPath.<String>read(message, "$.From.Address")).isEqualTo("no-reply@capsuleai.test");
        assertThat(JsonPath.<String>read(message, "$.Subject")).isEqualTo("Đặt lại mật khẩu CapsuleAI");
        assertThat(JsonPath.<String>read(message, "$.Text"))
                .contains("capsuleai://reset-password#token=real-smtp-reset-secret")
                .contains("30 phút");
        assertThat(JsonPath.<String>read(message, "$.HTML")).isEmpty();
    }

    @Test
    void theProductionAsyncCompositionDeliversBeforeShutdownCompletes() throws Exception {
        AsyncPasswordResetInstructionSender asyncSender = AsyncPasswordResetInstructionSender.withBoundedQueue(
                sender(mailServerAt(MAILPIT.getMappedPort(SMTP_PORT))), 10);

        asyncSender.sendInstructions(INSTRUCTIONS);
        asyncSender.close();

        assertThat(JsonPath.<Integer>read(get("/api/v1/messages"), "$.messages.length()")).isOne();
    }

    @Test
    void anUnreachableMailServerIsLoggedAndNeverThrown(CapturedOutput output) {
        SmtpPasswordResetInstructionSender unreachable = sender(mailServerAt(1));

        assertThatCode(() -> unreachable.sendInstructions(INSTRUCTIONS)).doesNotThrowAnyException();

        assertThat(output.getAll())
                .contains("Password-reset instructions could not be handed to the mail server")
                .doesNotContain("real-smtp-reset-secret")
                .doesNotContain("person@example.com");
    }

    private static SmtpPasswordResetInstructionSender sender(JavaMailSenderImpl mailServer) {
        return new SmtpPasswordResetInstructionSender(mailServer, "no-reply@capsuleai.test", INSTRUCTION_LINK);
    }

    private static JavaMailSenderImpl mailServerAt(int port) {
        JavaMailSenderImpl mailServer = new JavaMailSenderImpl();
        mailServer.setHost(MAILPIT.getHost());
        mailServer.setPort(port);
        mailServer.setDefaultEncoding(StandardCharsets.UTF_8.name());
        mailServer.getJavaMailProperties().setProperty("mail.smtp.connectiontimeout", "2000");
        mailServer.getJavaMailProperties().setProperty("mail.smtp.timeout", "2000");
        return mailServer;
    }

    private String get(String path) throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(mailpitApi(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode()).as("Mailpit %s", path).isEqualTo(200);
        return response.body();
    }

    private static URI mailpitApi(String path) {
        return URI.create("http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(API_PORT) + path);
    }
}
