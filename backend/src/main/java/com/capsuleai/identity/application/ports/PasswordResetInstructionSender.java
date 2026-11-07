package com.capsuleai.identity.application.ports;

/**
 * Hands reset instructions to the Email Delivery Service (SI-003, ADA-006).
 *
 * <p>Called only after the issued interaction has been committed. Implementations
 * must not throw for delivery problems: a provider failure is reported in protected
 * logs, while the initiation response stays equivalent for every email (FR-AUTH-003).
 */
public interface PasswordResetInstructionSender {

    void sendInstructions(PasswordResetInstructions instructions);
}