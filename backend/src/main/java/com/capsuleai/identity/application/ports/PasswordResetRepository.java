package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.PasswordResetToken;
import com.capsuleai.identity.domain.PasswordResetTokenHash;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Identity-owned persistence of password-reset interactions (DATA-AUTH-005, ADA-008). */
public interface PasswordResetRepository {

    /**
     * Marks every open (neither consumed nor replaced) interaction of the account as
     * replaced (FR-AUTH-018). Call inside {@link IdentityTransactions} after
     * {@link AccountRepository#lockForCredentialChange(UUID)}.
     *
     * @return how many interactions were replaced
     */
    int replaceOpenTokens(UUID accountId, Instant replacedAt);

    /** Stores a newly issued interaction with the hash of its secret. */
    void add(PasswordResetToken token, PasswordResetTokenHash hash);

    /** Reads an interaction by the hash of its secret, without locking. */
    Optional<PasswordResetToken> findByTokenHash(PasswordResetTokenHash hash);

    /**
     * Reads an interaction by the hash of its secret and LOCKS its row until the
     * surrounding transaction ends, so competing completions and a competing
     * replacement run one after another. Must be called inside {@link IdentityTransactions}.
     */
    Optional<PasswordResetToken> lockByTokenHash(PasswordResetTokenHash hash);

    /**
     * Marks a locked, open interaction as consumed by an accepted reset.
     *
     * @throws IllegalStateException if the interaction was not open
     */
    void consume(UUID tokenId, Instant consumedAt);

    /**
     * Deletes at most {@code batchSize} interactions that ended (were consumed, replaced or
     * expired) at or before {@code cutoff}, skipping rows another transaction holds locked.
     * An interaction that is still usable never qualifies.
     *
     * @return the number of deleted interactions
     */
    int deleteEndedBefore(Instant cutoff, int batchSize);
}