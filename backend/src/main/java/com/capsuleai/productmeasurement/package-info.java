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
 * <p>Persistence: when this module first persists state, its tables go into the
 * PostgreSQL schema {@code product_measurement} and are named {@code product_measurement_*}.
 * No other module reads, writes, joins or references them by foreign key; other
 * modules use this module's published {@code api} (architecture refinement AR-01-AR-04,
 * {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.productmeasurement;
