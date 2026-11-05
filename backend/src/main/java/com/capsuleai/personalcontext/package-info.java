/**
 * Personal Context business module.
 *
 * <p>Owns the user's declared preferences and needs, selected location,
 * and the meaning of current or limited contextual information, including
 * weather evidence used by other capabilities.
 *
 * <p>Other modules use published {@code api} contracts rather than importing
 * private profile models, application services, or persistence classes.
 * Context ownership and withdrawal rules remain within this module.
 *
 * <p>Persistence: when this module first persists state, its tables go into the
 * PostgreSQL schema {@code personal_context} and are named {@code personal_context_*}.
 * No other module reads, writes, joins or references them by foreign key; other
 * modules use this module's published {@code api} (architecture refinement AR-01-AR-04,
 * {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.personalcontext;
