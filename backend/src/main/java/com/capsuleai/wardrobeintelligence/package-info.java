/**
 * Wardrobe Intelligence business module.
 *
 * <p>Owns wardrobe coverage and gap assessments, hypothetical item utility,
 * strategic shopping support, and the meaning of evaluation result states.
 * Assessments use authoritative published inputs rather than directly
 * accessing another module's private data models.
 *
 * <p>Other modules collaborate through its published {@code api} contracts.
 * Derived assessment results are not assumed to require a permanent result
 * store; detailed persistence choices belong to selected future work.
 *
 * <p>Persistence: when this module first persists state, its tables go into the
 * PostgreSQL schema {@code wardrobe_intelligence} and are named {@code wardrobe_intelligence_*}.
 * No other module reads, writes, joins or references them by foreign key; other
 * modules use this module's published {@code api} (architecture refinement AR-01-AR-04,
 * {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.wardrobeintelligence;
