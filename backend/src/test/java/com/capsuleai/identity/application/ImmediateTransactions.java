package com.capsuleai.identity.application;

import com.capsuleai.identity.application.ports.IdentityTransactions;
import java.util.function.Supplier;

/** Fake transaction boundary: runs the work directly and counts how often it was used. */
final class ImmediateTransactions implements IdentityTransactions {

    private int transactions;

    @Override
    public synchronized <T> T inTransaction(Supplier<T> work) {
        transactions++;
        return work.get();
    }

    synchronized int count() {
        return transactions;
    }
}
