/**
 * Wardrobe business module.
 *
 * <p>Owns confirmed garment information, wardrobe membership and readiness,
 * garment confirmation/correction, and the garment analysis and media lifecycle.
 * AI-proposed information must not silently become confirmed wardrobe truth.
 *
 * <p>Other modules consume Wardrobe's published {@code api} contracts for
 * authorized garment state and readiness. Its internal aggregates, storage
 * adapters, and persistence entities remain private to Wardrobe.
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.wardrobe;
