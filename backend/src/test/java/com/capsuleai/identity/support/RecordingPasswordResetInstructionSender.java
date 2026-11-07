package com.capsuleai.identity.support;

import com.capsuleai.identity.application.ports.PasswordResetInstructionSender;
import com.capsuleai.identity.application.ports.PasswordResetInstructions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TEST-ONLY substitute for the Email Delivery Service. It records the instructions
 * the application handed over, synchronously, so tests can follow the emailed link.
 * It proves what CapsuleAI sends, not that a mail server delivered it; real SMTP
 * delivery is verified separately in {@code SmtpPasswordResetInstructionSenderIT}.
 */
public final class RecordingPasswordResetInstructionSender implements PasswordResetInstructionSender {

    private final List<PasswordResetInstructions> sent = new ArrayList<>();

    @Override
    public synchronized void sendInstructions(PasswordResetInstructions instructions) {
        sent.add(instructions);
    }

    public synchronized List<PasswordResetInstructions> sent() {
        return List.copyOf(sent);
    }

    /** The reset secret of the most recent instructions sent to this normalized email. */
    public synchronized Optional<String> latestTokenFor(String normalizedEmail) {
        for (int index = sent.size() - 1; index >= 0; index--) {
            if (sent.get(index).recipient().value().equals(normalizedEmail)) {
                return Optional.of(sent.get(index).resetToken());
            }
        }
        return Optional.empty();
    }

    public synchronized void clear() {
        sent.clear();
    }
}
