package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.CheckPasswordResetCommand;

/** JSON body of POST /api/v1/auth/password-reset/check. toString() masks the secret. */
public record PasswordResetCheckRequest(String token) {

    CheckPasswordResetCommand toCommand(String clientAddress) {
        return new CheckPasswordResetCommand(token, clientAddress);
    }

    @Override
    public String toString() {
        return "PasswordResetCheckRequest[token=[PROTECTED]]";
    }
}