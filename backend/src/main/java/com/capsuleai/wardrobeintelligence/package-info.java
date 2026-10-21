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
 * <p>Architecture reference:
 * {@code docs/04-architecture/views/implementation-view.puml}.
 */
package com.capsuleai.wardrobeintelligence;
