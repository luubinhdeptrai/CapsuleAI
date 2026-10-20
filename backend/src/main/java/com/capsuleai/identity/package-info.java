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
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.identity;
