package com.capsuleai.wardrobe.application;

/**
 * The Idempotency-Key was already used by this owner for an addition with
 * different values. A different intended addition needs a new key. Nothing changed.
 */
public final class IdempotencyKeyReusedException extends RuntimeException {

    public IdempotencyKeyReusedException() {
        super("The Idempotency-Key was already used for a different garment addition");
    }
}