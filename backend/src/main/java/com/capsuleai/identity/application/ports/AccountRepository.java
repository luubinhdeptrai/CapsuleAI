package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.Account;
import com.capsuleai.identity.domain.EmailAddress;
import com.capsuleai.identity.domain.PasswordHash;
import java.util.Optional;
import java.util.UUID;

/** Identity-owned account persistence. */
public interface AccountRepository {

    /**
     * Stores the account only if no account already uses its normalized email.
     *
     * <p>Must be atomic under concurrency: of several equivalent registrations
     * running at the same time, exactly one may return {@code true}.
     *
     * @return {@code true} if stored; {@code false} if the email identity is
     *         already registered (nothing was changed)
     */
    boolean addIfEmailAvailable(Account account);

    /** @return the account registered for this normalized email, if any */
    Optional<Account> findByEmail(EmailAddress email);

    /**
     * Reads the account's current password hash and holds a SHARED lock on the
     * account until the surrounding transaction ends. A concurrent password reset
     * waits, so a login cannot create a session from a credential that a reset has
     * just replaced. Must be called inside {@link IdentityTransactions}.
     *
     * @return the current hash, or empty if the account does not exist
     */
    Optional<PasswordHash> lockCredentialAgainstChange(UUID accountId);

    /**
     * Holds an EXCLUSIVE lock on the account until the surrounding transaction ends.
     * Reset issuance and reset completion take it first, so they run one after
     * another per account and always lock account, then reset, then session rows.
     * Must be called inside {@link IdentityTransactions}.
     *
     * @return {@code true} if the account exists (and is now locked)
     */
    boolean lockForCredentialChange(UUID accountId);

    /** Replaces the password hash of a locked account (FR-AUTH-004). */
    void replacePasswordHash(UUID accountId, PasswordHash newPasswordHash);
}