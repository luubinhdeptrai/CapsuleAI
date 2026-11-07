package com.capsuleai.identity.adapter.out.email;

import com.capsuleai.identity.application.ports.PasswordResetInstructionSender;
import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import com.capsuleai.identity.domain.PasswordResetPolicy;
import java.net.URI;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Delivers reset instructions as a plain-text Vietnamese email through SMTP
 * (SI-003, ADA-006). The message carries only what recovery needs: the recipient,
 * a link holding the reset secret, and its validity. The secret travels in the
 * link fragment, which browsers never send to a web server.
 *
 * <p>A delivery failure is logged without the secret or the recipient and is not
 * rethrown, so the initiation response stays equivalent (FR-AUTH-003, ERR-DEP-001).
 */
public final class SmtpPasswordResetInstructionSender implements PasswordResetInstructionSender {

    static final String SUBJECT = "Đặt lại mật khẩu CapsuleAI";

    private static final Logger log = LoggerFactory.getLogger(SmtpPasswordResetInstructionSender.class);

    private final JavaMailSender mailSender;
    private final String senderAddress;
    private final URI instructionLink;

    public SmtpPasswordResetInstructionSender(JavaMailSender mailSender, String senderAddress, URI instructionLink) {
        this.mailSender = Objects.requireNonNull(mailSender, "mailSender");
        this.senderAddress = Objects.requireNonNull(senderAddress, "senderAddress");
        this.instructionLink = Objects.requireNonNull(instructionLink, "instructionLink");
    }

    @Override
    public void sendInstructions(PasswordResetInstructions instructions) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(instructions.recipient().value());
        message.setSubject(SUBJECT);
        message.setText(body(resetLink(instructions.resetToken())));
        try {
            mailSender.send(message);
        } catch (MailException deliveryFailure) {
            log.warn("Password-reset instructions could not be handed to the mail server ({})",
                    deliveryFailure.getClass().getSimpleName());
        }
    }

    String resetLink(String resetToken) {
        return instructionLink + "#token=" + resetToken;
    }

    private static String body(String resetLink) {
        long validMinutes = PasswordResetPolicy.INTERACTION_LIFETIME.toMinutes();
        return """
                Xin chào,

                CapsuleAI đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản sử dụng địa chỉ email này.

                Mở liên kết dưới đây để đặt mật khẩu mới. Liên kết chỉ dùng được một lần và hết hạn sau %d phút:
                %s

                Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. Mật khẩu hiện tại của bạn sẽ không thay đổi.

                CapsuleAI
                """.formatted(validMinutes, resetLink);
    }
}