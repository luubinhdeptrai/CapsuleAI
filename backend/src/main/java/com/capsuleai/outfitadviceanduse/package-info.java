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
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.outfitadviceanduse;
