package com.capsuleai.identity.application.ports;

import java.util.function.Supplier;

/**
 * Runs work in one database transaction: everything commits together, or
 * nothing does if the work throws. Lets use cases own their transaction
 * boundary without depending on Spring (ADA-015).
 */
public interface IdentityTransactions {

    <T> T inTransaction(Supplier<T> work);

    default void inTransactionWithoutResult(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }
}