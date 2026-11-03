package com.capsuleai.identity.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.identity.application.LogInService;
import com.capsuleai.identity.application.LogOutService;
import com.capsuleai.identity.application.RefreshSessionService;
import com.capsuleai.identity.application.RegisterAccountService;
import com.capsuleai.identity.application.RegisteredAccount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API: account registration and the login-session lifecycle.
 *
 * <p>Each handler only translates HTTP to its own use case. Which operations
 * are public is decided by {@code IdentitySecurityConfiguration}, not here.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterAccountService registerAccountService;
    private final LogInService logInService;
    private final LogOutService logOutService;
    private final RefreshSessionService refreshSessionService;

    public AuthController(RegisterAccountService registerAccountService,
                          LogInService logInService,
                          LogOutService logOutService,
                          RefreshSessionService refreshSessionService) {
        this.registerAccountService = registerAccountService;
        this.logInService = logInService;
        this.logOutService = logOutService;
        this.refreshSessionService = refreshSessionService;
    }

    /** US-AUTH-001 — public; creates an account without issuing tokens. */
    @PostMapping("/registrations")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        RegisteredAccount account = registerAccountService.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistrationResponse.from(account));
    }

    /** US-AUTH-002 — public credential login; starts a new login session. */
    @PostMapping("/login")
    public TokenResponse logIn(@RequestBody LoginRequest request) {
        return TokenResponse.from(logInService.logIn(request.toCommand()));
    }

    /** US-AUTH-004 — ends the current login session. Requires a usable access token. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(@AuthenticationPrincipal AuthenticatedAccount current) {
        logOutService.logOut(current);
        return ResponseEntity.noContent().build();
    }

    /** US-AUTH-003 — public renewal; the refresh token is the credential. */
    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return TokenResponse.from(refreshSessionService.renew(request.toCommand()));
    }
}
