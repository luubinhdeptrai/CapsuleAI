package com.capsuleai.identity.application;

/** Raw registration input, exactly as received. toString() masks the passwords. */
public record RegisterAccountCommand(String email, String password, String confirmPassword, String displayName) {

    @Override
    public String toString() {
        return "RegisterAccountCommand[email=" + email
                + ", password=[PROTECTED], confirmPassword=[PROTECTED], displayName=" + displayName + "]";
    }
}