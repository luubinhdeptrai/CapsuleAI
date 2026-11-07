package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RequestPasswordResetCommand;

/** JSON body of POST /api/v1/auth/password-reset/request. */
public record PasswordResetRequest(String email) {

    RequestPasswordResetCommand toCommand(String clientAddress) {
        return new RequestPasswordResetCommand(email, clientAddress);
    }
}