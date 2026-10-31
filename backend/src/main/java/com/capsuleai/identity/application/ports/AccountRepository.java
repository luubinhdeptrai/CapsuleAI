package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.Account;

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
}