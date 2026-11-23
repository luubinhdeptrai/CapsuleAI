package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.application.ports.LocationRepository;
import com.capsuleai.personalcontext.application.ports.LocationRepository.StoredWeatherEvidence;
import com.capsuleai.personalcontext.application.ports.LocationRepository.WeatherPublication;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.WeatherAcquisition;
import com.capsuleai.personalcontext.application.ports.WeatherProvider;
import com.capsuleai.personalcontext.application.ports.WeatherQuery;
import com.capsuleai.personalcontext.domain.EnvironmentUnavailableReason;
import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherFreshness;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * US-CTX-001 (S4-CTX-02/04/05/06; OPEN-S4-19/21/23) — the environmental context of the owner's current
 * selection. Stored evidence of that selection is reused while it is current (at most 30 minutes old by its
 * provider evidence time, inclusive); otherwise weather is acquired on demand through the bounded provider,
 * outside any transaction. New evidence is published only if the same selection revision is still current, so
 * a late result can never become current for a changed or cleared selection. Failures are explicit
 * UNAVAILABLE outcomes; no value is invented.
 */
public final class EnvironmentalContextService {

    private final LocationRepository locationRepository;
    private final WeatherProvider weatherProvider;
    private final PersonalContextTransactions personalContextTransactions;
    private final Clock clock;

    public EnvironmentalContextService(LocationRepository locationRepository, WeatherProvider weatherProvider,
                                       PersonalContextTransactions personalContextTransactions, Clock clock) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
        this.weatherProvider = Objects.requireNonNull(weatherProvider, "weatherProvider");
        this.personalContextTransactions =
                Objects.requireNonNull(personalContextTransactions, "personalContextTransactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** Never call inside a transaction: it may wait up to the acquisition deadline for the provider. */
    public EnvironmentalContext currentFor(UUID ownerAccountId, Optional<LocationProfile> profile) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        if (profile.isEmpty() || profile.get().selection().isEmpty()) {
            return new EnvironmentalContext.NoSelection();
        }
        LocationProfile location = profile.get();
        Optional<StoredWeatherEvidence> stored = locationRepository.findEvidence(ownerAccountId);
        if (stored.isPresent() && stored.get().locationRevision() == location.revision()
                && WeatherFreshness.isCurrentAt(stored.get().reading().observedAt(), now())) {
            return new EnvironmentalContext.Current(location.revision(), stored.get().reading());
        }
        return acquire(ownerAccountId, location);
    }

    private EnvironmentalContext acquire(UUID ownerAccountId, LocationProfile location) {
        LocationSelection selection = location.selection().orElseThrow();
        WeatherAcquisition acquisition;
        try {
            acquisition = weatherProvider.acquire(new WeatherQuery(selection.latitude(), selection.longitude()));
        } catch (RuntimeException unexpected) {
            // The port promises not to throw; a defect must still never become a fabricated or failed request.
            acquisition = new WeatherAcquisition.Unavailable(EnvironmentUnavailableReason.PROVIDER_UNAVAILABLE);
        }
        if (acquisition instanceof WeatherAcquisition.Unavailable unavailable) {
            return new EnvironmentalContext.Unavailable(location.revision(), unavailable.reason());
        }
        WeatherReading reading = ((WeatherAcquisition.Acquired) acquisition).reading();
        Instant now = now();
        if (WeatherFreshness.isImplausiblyFuture(reading.observedAt(), now)) {
            return new EnvironmentalContext.Unavailable(location.revision(),
                    EnvironmentUnavailableReason.INVALID_PROVIDER_DATA);
        }
        if (!WeatherFreshness.isCurrentAt(reading.observedAt(), now)) {
            return new EnvironmentalContext.Unavailable(location.revision(),
                    EnvironmentUnavailableReason.STALE_PROVIDER_DATA);
        }
        WeatherPublication publication = personalContextTransactions.inTransaction(() ->
                locationRepository.publishEvidenceIfCurrent(ownerAccountId, location.revision(), reading));
        if (publication == WeatherPublication.SELECTION_CHANGED) {
            return new EnvironmentalContext.Unavailable(location.revision(),
                    EnvironmentUnavailableReason.SELECTION_CHANGED);
        }
        return new EnvironmentalContext.Current(location.revision(), reading);
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
