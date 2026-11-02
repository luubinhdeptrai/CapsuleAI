package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.LogInService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-002 — public credential login endpoint. */
@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LogInService logInService;

    public LoginController(LogInService logInService) {
        this.logInService = logInService;
    }

    @PostMapping("/login")
    public TokenResponse logIn(@RequestBody LoginRequest request) {
        return TokenResponse.from(logInService.logIn(request.toCommand()));
    }
}