/**
 * Outfit Advice and Use business module.
 *
 * <p>Owns outfit advice and shuffle coordination, feedback, recorded Wear,
 * wear history, and outfit evaluation rules. Its published {@code api}
 * includes the shared outfit-evaluation semantics consumed by other owners.
 *
 * <p>Current garment information must come through Wardrobe's published
 * contracts. Other modules must not depend on this module's private domain,
 * use-case implementations, or persistence models.
 *
 * <p>Persistence: when this module first persists state, its tables go into the
 * PostgreSQL schema {@code outfit_advice_and_use} and are named {@code outfit_advice_and_use_*}.
 * No other module reads, writes, joins or references them by foreign key; other
 * modules use this module's published {@code api} (architecture refinement AR-01-AR-04,
 * {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.outfitadviceanduse;
