package com.capsuleai.identity.application;

/** Input of a reset submission. toString() masks the secret and the new password. */
public record CompletePasswordResetCommand(String token, String newPassword, String clientAddress) {

    @Override
    public String toString() {
        return "CompletePasswordResetCommand[token=[PROTECTED], newPassword=[PROTECTED], clientAddress="
                + clientAddress + "]";
    }
}