package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.util.function.Supplier;

/** Fake transaction boundary: runs the work directly and counts how often it was used. */
final class ImmediateWardrobeTransactions implements WardrobeTransactions {

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
