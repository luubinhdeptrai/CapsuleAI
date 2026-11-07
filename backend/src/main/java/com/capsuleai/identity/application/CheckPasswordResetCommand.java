package com.capsuleai.identity.application;

/** Input of opening a reset interaction from its instructions. toString() masks the secret. */
public record CheckPasswordResetCommand(String token, String clientAddress) {

    @Override
    public String toString() {
        return "CheckPasswordResetCommand[token=[PROTECTED], clientAddress=" + clientAddress + "]";
    }
}