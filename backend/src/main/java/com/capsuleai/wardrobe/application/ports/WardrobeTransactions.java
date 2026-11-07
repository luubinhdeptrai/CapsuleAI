package com.capsuleai.wardrobe.application.ports;

import java.util.function.Supplier;

/**
 * Runs Wardrobe work in one database transaction: everything commits together,
 * or nothing does if the work throws (ADA-009, ADA-015). Wardrobe owns this port;
 * it does not borrow Identity's private transaction port.
 */
public interface WardrobeTransactions {

    <T> T inTransaction(Supplier<T> work);
}