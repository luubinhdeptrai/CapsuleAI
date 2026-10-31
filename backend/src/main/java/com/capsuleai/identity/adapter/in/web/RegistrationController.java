package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RegisterAccountService;
import com.capsuleai.identity.application.RegisteredAccount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-001 — public registration endpoint. */
@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final RegisterAccountService registerAccountService;

    public RegistrationController(RegisterAccountService registerAccountService) {
        this.registerAccountService = registerAccountService;
    }

    @PostMapping("/registrations")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        RegisteredAccount account = registerAccountService.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistrationResponse.from(account));
    }
}