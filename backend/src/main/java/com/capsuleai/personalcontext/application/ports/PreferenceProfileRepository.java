package com.capsuleai.personalcontext.application.ports;

import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Personal Context-owned persistence of declared preferences (S3-PROF-01, ADA-015/016). */
public interface PreferenceProfileRepository {

    /** The owner's declaration, or empty if they have declared nothing yet. One consistent snapshot. */
    Optional<PreferenceProfile> find(UUID ownerAccountId);

    /**
     * Stores the owner's first declaration unless one exists (guarded creation, S3-PROF-04). Atomic: of
     * concurrent first declarations exactly one returns {@code true}. Call inside {@link PersonalContextTransactions}.
     */
    boolean createIfAbsent(PreferenceProfile profile);

    /**
     * Replaces the declared styles and needs only while the revision is still {@code expectedRevision};
     * the revision increases by one. Atomic under concurrency. Call inside {@link PersonalContextTransactions}.
     *
     * @return {@code false} if the revision changed or there is no declaration (nothing changed)
     */
    boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, DeclaredPreferences preferences,
                              Instant updatedAt);
}