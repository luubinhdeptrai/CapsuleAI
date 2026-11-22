package com.capsuleai.wardrobe.adapter.out.persistence;

/**
 * US-WAR-003 (S4-WAR-02, OPEN-S4-13): the finite catalogue of complete search statements. Each of the 14
 * statements is a compile-time constant composed from the text blocks below: the owner/current guard, the
 * conditions of one shape, an optional keyset predicate, then the order and limit. Request text never
 * becomes SQL: every value is bound, and code sets are bound as one comma-joined text expanded with
 * string_to_array (approved codes never contain commas). The unrestricted overview keeps the two
 * statements of {@link JdbcGarmentRepository}.
 */
final class SearchStatementCatalogue {

    /** Every statement starts with the owner's current garments (AR-03, BRULE-GAR-009). */
    static final String CURRENT_OF_OWNER = JdbcGarmentRepository.SELECT_GARMENT + """
            WHERE g.owner_account_id = :ownerAccountId AND g.removed_at IS NULL
            """;

    /**
     * The text condition: the garment matches when any participating field does (OR). EXISTS keeps a garment
     * with several matching Materials or Styles to one row. Brand is folded with lower() on both sides
     * after NFC normalization; the pattern escapes backslash, percent and underscore with a backslash.
     */
    static final String TEXT_CONDITION = """
              AND (g.category = ANY(string_to_array(CAST(:textCategories AS text), ','))
                   OR g.subtype = ANY(string_to_array(CAST(:textSubtypes AS text), ','))
                   OR g.color = ANY(string_to_array(CAST(:textColors AS text), ','))
                   OR g.pattern = ANY(string_to_array(CAST(:textPatterns AS text), ','))
                   OR EXISTS (SELECT 1 FROM wardrobe.wardrobe_garment_material tm
                              WHERE tm.garment_id = g.id
                                AND tm.material = ANY(string_to_array(CAST(:textMaterials AS text), ',')))
                   OR EXISTS (SELECT 1 FROM wardrobe.wardrobe_garment_style ts
                              WHERE ts.garment_id = g.id
                                AND ts.style = ANY(string_to_array(CAST(:textStyles AS text), ',')))
                   OR lower(normalize(g.brand, NFC)) LIKE lower(CAST(:brandPattern AS text)) ESCAPE '\\')
            """;

    /** Any of the selected categories (OR within the group). */
    static final String CATEGORY_CONDITION = """
              AND g.category = ANY(string_to_array(CAST(:categories AS text), ','))
            """;

    /** Any of the selected colors (OR within the group). */
    static final String COLOR_CONDITION = """
              AND g.color = ANY(string_to_array(CAST(:colors AS text), ','))
            """;

    /** Strictly after the (created_at, id) position of the previous page. */
    static final String AFTER_POSITION = """
              AND (g.created_at, g.id) < (:afterCreatedAt, :afterGarmentId)
            """;

    /** The overview's stable order and the bounded page. */
    static final String NEWEST_FIRST = """
            ORDER BY g.created_at DESC, g.id DESC
            LIMIT :maximumCount
            """;

    private SearchStatementCatalogue() {
    }

    /** The seven shapes with at least one condition; conditions are ANDed. */
    enum SearchShape {
        TEXT(true, false, false,
                CURRENT_OF_OWNER + TEXT_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + TEXT_CONDITION + AFTER_POSITION + NEWEST_FIRST),
        CATEGORY(false, true, false,
                CURRENT_OF_OWNER + CATEGORY_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + CATEGORY_CONDITION + AFTER_POSITION + NEWEST_FIRST),
        COLOR(false, false, true,
                CURRENT_OF_OWNER + COLOR_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + COLOR_CONDITION + AFTER_POSITION + NEWEST_FIRST),
        TEXT_CATEGORY(true, true, false,
                CURRENT_OF_OWNER + TEXT_CONDITION + CATEGORY_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + TEXT_CONDITION + CATEGORY_CONDITION + AFTER_POSITION + NEWEST_FIRST),
        TEXT_COLOR(true, false, true,
                CURRENT_OF_OWNER + TEXT_CONDITION + COLOR_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + TEXT_CONDITION + COLOR_CONDITION + AFTER_POSITION + NEWEST_FIRST),
        CATEGORY_COLOR(false, true, true,
                CURRENT_OF_OWNER + CATEGORY_CONDITION + COLOR_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + CATEGORY_CONDITION + COLOR_CONDITION + AFTER_POSITION + NEWEST_FIRST),
        TEXT_CATEGORY_COLOR(true, true, true,
                CURRENT_OF_OWNER + TEXT_CONDITION + CATEGORY_CONDITION + COLOR_CONDITION + NEWEST_FIRST,
                CURRENT_OF_OWNER + TEXT_CONDITION + CATEGORY_CONDITION + COLOR_CONDITION + AFTER_POSITION
                        + NEWEST_FIRST);

        private final boolean text;
        private final boolean category;
        private final boolean color;
        private final String firstPage;
        private final String afterPosition;

        SearchShape(boolean text, boolean category, boolean color, String firstPage, String afterPosition) {
            this.text = text;
            this.category = category;
            this.color = color;
            this.firstPage = firstPage;
            this.afterPosition = afterPosition;
        }

        /** The shape of a query with these conditions; an unrestricted query has none. */
        static SearchShape of(boolean text, boolean category, boolean color) {
            for (SearchShape shape : values()) {
                if (shape.text == text && shape.category == category && shape.color == color) {
                    return shape;
                }
            }
            throw new IllegalArgumentException("An unrestricted overview uses the JdbcGarmentRepository statements");
        }

        String statement(boolean continuesAfterPosition) {
            return continuesAfterPosition ? afterPosition : firstPage;
        }

        boolean hasText() {
            return text;
        }

        boolean hasCategory() {
            return category;
        }

        boolean hasColor() {
            return color;
        }
    }
}