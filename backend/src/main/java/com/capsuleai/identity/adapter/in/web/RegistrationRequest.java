package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RegisterAccountCommand;

/** JSON body of POST /api/v1/auth/registrations. Missing fields arrive as null. */
public record RegistrationRequest(String email, String password, String confirmPassword, String displayName) {

    RegisterAccountCommand toCommand() {
        return new RegisterAccountCommand(email, password, confirmPassword, displayName);
    }

    @Override
    public String toString() {
        return "RegistrationRequest[email=" + email
                + ", password=[PROTECTED], confirmPassword=[PROTECTED], displayName=" + displayName + "]";
    }
}