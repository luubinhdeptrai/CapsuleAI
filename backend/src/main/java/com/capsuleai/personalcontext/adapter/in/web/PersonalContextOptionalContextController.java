package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.personalcontext.application.ReviseOptionalContextService;
import com.capsuleai.personalcontext.application.ViewOptionalContextService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authenticated user's optional gender and body-shape context (US-PROF-002). Always "my" context: the owner
 * is the authenticated account and no request names another (FR-AUTH-008). A separate resource from the
 * preferences of US-PROF-001 (S4-PROF-02). Protected by the application's default-deny filter chain.
 */
@RestController
@RequestMapping("/api/v1/personal-context/optional-context")
public class PersonalContextOptionalContextController {

    private final ViewOptionalContextService viewOptionalContextService;
    private final ReviseOptionalContextService reviseOptionalContextService;

    public PersonalContextOptionalContextController(ViewOptionalContextService viewOptionalContextService,
                                                    ReviseOptionalContextService reviseOptionalContextService) {
        this.viewOptionalContextService = viewOptionalContextService;
        this.reviseOptionalContextService = reviseOptionalContextService;
    }

    /** AC-01/03: current or absent; never 404 for a user who has declared nothing. */
    @GetMapping
    public OptionalContextResponse viewOptionalContext(@AuthenticationPrincipal AuthenticatedAccount owner) {
        return OptionalContextResponse.from(viewOptionalContextService.view(owner.accountId()));
    }

    /** AC-02/03/05: one presence-aware change against the revision the user read. */
    @PatchMapping
    public OptionalContextResponse reviseOptionalContext(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                         @RequestBody OptionalContextRevisionRequest request) {
        return OptionalContextResponse.from(
                reviseOptionalContextService.revise(request.toCommand(owner.accountId())).profile());
    }
}