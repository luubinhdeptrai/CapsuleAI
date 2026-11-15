package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.personalcontext.application.RevisePreferencesService;
import com.capsuleai.personalcontext.application.ViewPreferencesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authenticated user's declared styles and common needs (US-PROF-001). Always "my preferences":
 * the owner is the authenticated account and no request names another (FR-AUTH-008). Protected by the
 * application's default-deny filter chain.
 */
@RestController
@RequestMapping("/api/v1/personal-context/preferences")
public class PersonalContextPreferencesController {

    private final ViewPreferencesService viewPreferencesService;
    private final RevisePreferencesService revisePreferencesService;

    public PersonalContextPreferencesController(ViewPreferencesService viewPreferencesService,
                                                RevisePreferencesService revisePreferencesService) {
        this.viewPreferencesService = viewPreferencesService;
        this.revisePreferencesService = revisePreferencesService;
    }

    /** AC-01: current or unset context; never 404 for a user who has declared nothing. */
    @GetMapping
    public PreferencesResponse viewPreferences(@AuthenticationPrincipal AuthenticatedAccount owner) {
        return PreferencesResponse.from(viewPreferencesService.view(owner.accountId()));
    }

    /** AC-01/02/05/06: one presence-aware change against the revision the user read. */
    @PatchMapping
    public PreferencesResponse revisePreferences(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                 @RequestBody PreferencesRevisionRequest request) {
        return PreferencesResponse.from(revisePreferencesService.revise(request.toCommand(owner.accountId())).profile());
    }
}