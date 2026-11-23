package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.LocationProfile;
import com.capsuleai.personalcontext.domain.LocationSelection;
import com.capsuleai.personalcontext.domain.WeatherReading;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Personal Context-owned persistence of the location selection and its latest weather evidence (V14). */
public interface LocationRepository {

    /** Stored evidence and the location revision it was acquired for. */
    record StoredWeatherEvidence(long locationRevision, WeatherReading reading) {

        public StoredWeatherEvidence {
            Objects.requireNonNull(reading, "reading");
        }
    }

    /** The outcome of publishing newly acquired evidence. */
    enum WeatherPublication {
        /** The evidence is now the latest for the current selection. */
        PUBLISHED,
        /** Newer evidence of the same selection was already stored; it was kept. */
        KEPT_NEWER,
        /** The selection changed or was cleared after the acquisition started; nothing was stored. */
        SELECTION_CHANGED
    }

    Optional<LocationProfile> find(UUID ownerAccountId);

    /** Stores the first selection unless a record exists (guarded). Call inside {@link PersonalContextTransactions}. */
    boolean createIfAbsent(LocationProfile profile);

    /**
     * Replaces (or clears) the selection only while the revision is still {@code expectedRevision}; the revision
     * increases by one and any stored evidence is deleted in the same transaction. Call inside
     * {@link PersonalContextTransactions}. @return false if nothing changed
     */
    boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, Optional<LocationSelection> selection,
                              Instant updatedAt);

    Optional<StoredWeatherEvidence> findEvidence(UUID ownerAccountId);

    /**
     * Publishes evidence only if the selection is still present at {@code locationRevision}, holding the location
     * row in share mode, and never replaces newer evidence of the same revision. Call inside
     * {@link PersonalContextTransactions}.
     */
    WeatherPublication publishEvidenceIfCurrent(UUID ownerAccountId, long locationRevision, WeatherReading reading);
}