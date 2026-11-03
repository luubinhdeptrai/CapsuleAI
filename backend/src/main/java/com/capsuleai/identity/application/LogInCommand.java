package com.capsuleai.identity.application;

/** Raw login input, exactly as received. toString() masks the password. */
public record LogInCommand(String email, String password) {

    @Override
    public String toString() {
        return "LogInCommand[email=" + email + ", password=[PROTECTED]]";
    }
}