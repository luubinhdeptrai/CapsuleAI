/**
 * Product Measurement supporting module.
 *
 * <p>Owns purpose-limited measurement intake, permitted outcome evidence,
 * and the lifecycle and retention of its own measurement information.
 * It is independent of core product-action acceptance and authority.
 *
 * <p>Other modules collaborate through published {@code api} contracts,
 * never by accessing private measurement storage or implementation classes.
 * Measurement failures must not retroactively invalidate accepted core work.
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.productmeasurement;
