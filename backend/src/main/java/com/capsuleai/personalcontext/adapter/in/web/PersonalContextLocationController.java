package com.capsuleai.personalcontext.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.personalcontext.application.ReviseLocationService;
import com.capsuleai.personalcontext.application.ViewLocationContextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The authenticated user's location selection and its environmental context (US-CTX-001). Always "my"
 * location: the owner is the authenticated account and no request names another (FR-AUTH-008). Protected by
 * the application's default-deny filter chain. Reads may refresh weather on demand within 2 seconds.
 */
@RestController
@RequestMapping("/api/v1/personal-context/location")
public class PersonalContextLocationController {

    private final ViewLocationContextService viewLocationContextService;
    private final ReviseLocationService reviseLocationService;
    private final String weatherAttribution;

    public PersonalContextLocationController(ViewLocationContextService viewLocationContextService,
                                             ReviseLocationService reviseLocationService,
                                             @Value("${capsuleai.personal-context.weather.attribution}")
                                             String weatherAttribution) {
        this.viewLocationContextService = viewLocationContextService;
        this.reviseLocationService = reviseLocationService;
        this.weatherAttribution = weatherAttribution;
    }

    /** AC-02/03: the selection and its current or unavailable weather; never 404 when nothing is selected. */
    @GetMapping
    public LocationContextResponse viewLocation(@AuthenticationPrincipal AuthenticatedAccount owner) {
        return LocationContextResponse.from(viewLocationContextService.view(owner.accountId()), weatherAttribution);
    }

    /** AC-01/05/06: one change or clear against the revision the user read; weather is acquired after commit. */
    @PatchMapping
    public LocationContextResponse reviseLocation(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                  @RequestBody LocationRevisionRequest request) {
        return LocationContextResponse.from(reviseLocationService.revise(request.toCommand(owner.accountId())),
                weatherAttribution);
    }
}
