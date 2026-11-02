package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.LogInCommand;

/** JSON body of POST /api/v1/auth/login. toString() masks the password. */
public record LoginRequest(String email, String password) {

    LogInCommand toCommand() {
        return new LogInCommand(email, password);
    }

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=[PROTECTED]]";
    }
}