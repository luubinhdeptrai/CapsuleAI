package com.capsuleai.personalcontext.application.ports;

import java.util.function.Supplier;

/**
 * Runs Personal Context work in one database transaction: everything commits together or nothing does.
 * The module owns this port; it does not borrow another module's private transaction port (ADA-015).
 */
public interface PersonalContextTransactions {

    <T> T inTransaction(Supplier<T> work);
}