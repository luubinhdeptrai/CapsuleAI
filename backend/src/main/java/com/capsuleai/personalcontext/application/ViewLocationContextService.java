package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.domain.LocationProfile;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 AC-02/03 — the owner's current selection and its environmental context, refreshing weather on
 * demand when no current evidence exists (within the 2-second deadline). Selection data is never changed here.
 */
public final class ViewLocationContextService {

    private final LocationRepository locationRepository;
    private final EnvironmentalContextService environmentalContextService;

    public ViewLocationContextService(LocationRepository locationRepository,
                                      EnvironmentalContextService environmentalContextService) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
        this.environmentalContextService =
                Objects.requireNonNull(environmentalContextService, "environmentalContextService");
    }

    public LocationContextView view(UUID ownerAccountId) {
        Optional<LocationProfile> profile = locationRepository.find(Objects.requireNonNull(ownerAccountId, "ownerAccountId"));
        return new LocationContextView(profile, environmentalContextService.currentFor(ownerAccountId, profile));
    }
}
