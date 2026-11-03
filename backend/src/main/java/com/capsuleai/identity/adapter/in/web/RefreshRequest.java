package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RenewSessionCommand;

/** JSON body of POST /api/v1/auth/refresh. toString() masks the token. */
public record RefreshRequest(String refreshToken) {

    RenewSessionCommand toCommand() {
        return new RenewSessionCommand(refreshToken);
    }

    @Override
    public String toString() {
        return "RefreshRequest[refreshToken=[PROTECTED]]";
    }
}