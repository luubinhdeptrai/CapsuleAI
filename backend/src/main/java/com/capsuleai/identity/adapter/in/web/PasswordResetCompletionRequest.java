package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.CompletePasswordResetCommand;

/** JSON body of POST /api/v1/auth/password-reset/complete. toString() masks both secrets. */
public record PasswordResetCompletionRequest(String token, String newPassword) {

    CompletePasswordResetCommand toCommand(String clientAddress) {
        return new CompletePasswordResetCommand(token, newPassword, clientAddress);
    }

    @Override
    public String toString() {
        return "PasswordResetCompletionRequest[token=[PROTECTED], newPassword=[PROTECTED]]";
    }
}