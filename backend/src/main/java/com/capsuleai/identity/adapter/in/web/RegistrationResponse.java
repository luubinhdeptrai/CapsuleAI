package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RegisteredAccount;
import java.util.UUID;

/** JSON body of a successful registration. */
public record RegistrationResponse(UUID accountId, String email, String displayName) {

    static RegistrationResponse from(RegisteredAccount account) {
        return new RegistrationResponse(account.accountId(), account.email(), account.displayName());
    }
}