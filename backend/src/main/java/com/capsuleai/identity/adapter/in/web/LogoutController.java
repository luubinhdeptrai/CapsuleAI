package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.application.LogOutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-004 — ends the current login session. Requires a usable access token. */
@RestController
@RequestMapping("/api/v1/auth")
public class LogoutController {

    private final LogOutService logOutService;

    public LogoutController(LogOutService logOutService) {
        this.logOutService = logOutService;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(@AuthenticationPrincipal AuthenticatedAccount current) {
        logOutService.logOut(current);
        return ResponseEntity.noContent().build();
    }
}