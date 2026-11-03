package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.application.RefreshSessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** US-AUTH-003 — public renewal endpoint; the refresh token is the credential. */
@RestController
@RequestMapping("/api/v1/auth")
public class SessionRefreshController {

    private final RefreshSessionService refreshSessionService;

    public SessionRefreshController(RefreshSessionService refreshSessionService) {
        this.refreshSessionService = refreshSessionService;
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return TokenResponse.from(refreshSessionService.renew(request.toCommand()));
    }
}