package com.capsuleai.identity.adapter.out.email;

import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import com.capsuleai.identity.domain.EmailAddress;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(OutputCaptureExtension.class)
class SmtpPasswordResetInstructionSenderTest {

    private static final PasswordResetInstructions INSTRUCTIONS = new PasswordResetInstructions(
            new EmailAddress("person@example.com"), "secret-reset-token", Instant.parse("2026-01-05T08:30:00Z"));

    @Test
    void theMessageCarriesOnlyTheRecipientAndAFragmentLinkWithTheSecret() {
        CapturingMailSender mailServer = new CapturingMailSender();
        SmtpPasswordResetInstructionSender sender = new SmtpPasswordResetInstructionSender(
                mailServer, "no-reply@capsuleai.test", URI.create("capsuleai://reset-password"));

        sender.sendInstructions(INSTRUCTIONS);

        SimpleMailMessage message = mailServer.sent.get(0);
        assertThat(mailServer.sent).hasSize(1);
        assertThat(message.getFrom()).isEqualTo("no-reply@capsuleai.test");
        assertThat(message.getTo()).containsExactly("person@example.com");
        assertThat(message.getCc()).isNull();
        assertThat(message.getBcc()).isNull();
        assertThat(message.getSubject()).isEqualTo("Đặt lại mật khẩu CapsuleAI");
        assertThat(message.getText())
                .contains("capsuleai://reset-password#token=secret-reset-token")
                .contains("30 phút")
                .contains("một lần")
                .contains("hết hạn sau 30 phút")
                .contains("hãy bỏ qua email này");
    }

    @Test
    void aMailServerFailureIsLoggedWithoutTheSecretOrTheRecipientAndNotRethrown(CapturedOutput output) {
        JavaMailSenderImpl unreachableMailServer = new JavaMailSenderImpl() {
            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                throw new MailSendException("Mail server connection failed for person@example.com");
            }
        };
        SmtpPasswordResetInstructionSender sender = new SmtpPasswordResetInstructionSender(
                unreachableMailServer, "no-reply@capsuleai.test", URI.create("capsuleai://reset-password"));

        assertThatCode(() -> sender.sendInstructions(INSTRUCTIONS)).doesNotThrowAnyException();

        assertThat(output.getAll())
                .contains("Password-reset instructions could not be handed to the mail server (MailSendException)")
                .doesNotContain("secret-reset-token")
                .doesNotContain("person@example.com");
    }

    /** Records messages instead of contacting a mail server. */
    private static final class CapturingMailSender extends JavaMailSenderImpl {

        private final List<SimpleMailMessage> sent = new ArrayList<>();

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            sent.addAll(List.of(simpleMessages));
        }
    }
}
