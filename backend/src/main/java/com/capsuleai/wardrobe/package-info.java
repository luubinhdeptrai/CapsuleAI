/**
 * Wardrobe business module.
 *
 * <p>Owns confirmed garment information, wardrobe membership, the truthful
 * "recommendation eligibility not evaluated" state,
 * garment confirmation/correction, and the garment analysis and media lifecycle.
 * AI-proposed information must not silently become confirmed wardrobe truth.
 *
 * <p>Other modules consume Wardrobe's published {@code api} contracts for
 * authorized garment state. Its internal aggregates, storage
 * adapters, and persistence entities remain private to Wardrobe.
 *
 * <p>Persistence: when this module first persists state, its tables go into the
 * PostgreSQL schema {@code wardrobe} and are named {@code wardrobe_*}.
 * No other module reads, writes, joins or references them by foreign key; other
 * modules use this module's published {@code api} (architecture refinement AR-01-AR-04,
 * {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.wardrobe;
