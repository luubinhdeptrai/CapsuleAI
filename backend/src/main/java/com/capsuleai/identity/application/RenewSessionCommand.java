package com.capsuleai.identity.application;

/** Raw renewal input. toString() masks the refresh token. */
public record RenewSessionCommand(String refreshToken) {

    @Override
    public String toString() {
        return "RenewSessionCommand[refreshToken=[PROTECTED]]";
    }
}