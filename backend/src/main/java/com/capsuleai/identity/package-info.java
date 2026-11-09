/**
 * Identity business module.
 *
 * <p>Owns account identity, authentication authority, persistent sessions,
 * password recovery, and related account/session lifecycle decisions.
 *
 * <p>Other modules collaborate through Identity's published {@code api}
 * contracts, not its private domain, application, or persistence classes.
 * The module retains authority over its own account and session state.
 *
 * <p>Persistence: its tables live in the PostgreSQL schema {@code identity} and are
 * named {@code identity_*}.
 * No other module reads, writes, joins or references them by foreign key; other
 * modules use this module's published {@code api} (architecture refinement AR-01-AR-04,
 * {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.identity;
