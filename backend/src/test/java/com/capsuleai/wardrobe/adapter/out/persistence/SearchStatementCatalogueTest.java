package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.adapter.out.persistence.SearchStatementCatalogue.SearchShape;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-WAR-003 (S4-WAR-02): consistency of the finite statement catalogue, which duplicates parts of its
 * statements by design. No variant may drop the owner/current guard, the order or the bound limit.
 */
class SearchStatementCatalogueTest {

    private static final String GUARD = "g.owner_account_id = :ownerAccountId AND g.removed_at IS NULL";
    private static final String ORDER = "ORDER BY g.created_at DESC, g.id DESC";
    private static final String KEYSET = "(g.created_at, g.id) < (:afterCreatedAt, :afterGarmentId)";

    @Test
    void everyStatementIsGuardedOrderedAndBounded() {
        Set<String> statements = new HashSet<>();
        for (SearchShape shape : SearchShape.values()) {
            for (boolean continuesAfterPosition : new boolean[] {false, true}) {
                String statement = shape.statement(continuesAfterPosition);
                assertThat(statement).as("%s after=%s", shape, continuesAfterPosition)
                        .contains(GUARD).contains(ORDER).contains("LIMIT :maximumCount");
                statements.add(statement);
            }
        }
        assertThat(statements).as("14 distinct complete statements").hasSize(14);
    }

    @Test
    void eachShapeContainsExactlyItsConditions() {
        for (SearchShape shape : SearchShape.values()) {
            String statement = shape.statement(false);
            assertThat(statement.contains(":brandPattern")).as("%s text", shape).isEqualTo(shape.hasText());
            assertThat(statement.contains("CAST(:categories AS text)")).as("%s category", shape)
                    .isEqualTo(shape.hasCategory());
            assertThat(statement.contains("CAST(:colors AS text)")).as("%s color", shape).isEqualTo(shape.hasColor());
        }
        assertThatThrownBy(() -> SearchShape.of(false, false, false)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void materialsAndStylesAreComparedPerGarmentOnlyWhenTheTextNamesSuchValues() {
        // OPEN-S4-15, measured 2026-11-22: an EXISTS over a child table was planned as a hashed subplan that
        // read every account's rows on each text search. The guarded per-garment ARRAY form is never hashed.
        assertThat(SearchStatementCatalogue.TEXT_CONDITION)
                .doesNotContain("EXISTS")
                .contains("CAST(:textMaterials AS text) <> ''")
                .contains("FROM wardrobe.wardrobe_garment_material tm")
                .contains("WHERE tm.garment_id = g.id")
                .contains("CAST(:textStyles AS text) <> ''")
                .contains("FROM wardrobe.wardrobe_garment_style ts")
                .contains("WHERE ts.garment_id = g.id");
    }

    @Test
    void onlyContinuationStatementsUseTheKeysetPosition() {
        for (SearchShape shape : SearchShape.values()) {
            assertThat(shape.statement(true)).contains(KEYSET);
            assertThat(shape.statement(false)).doesNotContain(KEYSET);
        }
    }
}
