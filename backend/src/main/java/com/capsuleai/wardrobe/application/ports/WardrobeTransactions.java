package com.capsuleai.wardrobe.application.ports;

import java.util.function.Supplier;

/**
 * Runs Wardrobe work in one database transaction: everything commits together,
 * or nothing does if the work throws (ADA-009, ADA-015). Wardrobe owns this port;
 * it does not borrow Identity's private transaction port.
 */
public interface WardrobeTransactions {

    <T> T inTransaction(Supplier<T> work);

    /**
     * US-WAR-003 (S4-WAR-03): runs read-only work on one consistent snapshot, so that several statements
     * observe the same committed state (for example a search page and the "owner has garments" fact).
     * Implementations backed by a database must give the whole transaction one snapshot (PostgreSQL
     * REPEATABLE READ). The default, an ordinary transaction, is enough for in-memory test doubles.
     */
    default <T> T inReadOnlySnapshot(Supplier<T> work) {
        return inTransaction(work);
    }
}