package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.adapter.out.persistence.SearchStatementCatalogue.SearchShape;
import com.capsuleai.wardrobe.application.WardrobeSearchQuery;
import com.capsuleai.wardrobe.application.WardrobeSearchQuery.TextMatch;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.application.ports.WardrobeSearchRepository;
import com.capsuleai.wardrobe.domain.Garment;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link WardrobeSearchRepository} (US-WAR-003). */
public final class JdbcWardrobeSearchRepository implements WardrobeSearchRepository {

    static final String HAS_CURRENT_GARMENT = """
            SELECT EXISTS (SELECT 1 FROM wardrobe.wardrobe_garment g
                           WHERE g.owner_account_id = :ownerAccountId AND g.removed_at IS NULL)
            """;

    private final JdbcClient jdbcClient;

    public JdbcWardrobeSearchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Garment> findCurrentMatching(UUID ownerAccountId, WardrobeSearchQuery query,
                                             Optional<WardrobePosition> after, int maximumCount) {
        SearchShape shape = SearchShape.of(query.restrictsText(), query.restrictsCategories(), query.restrictsColors());
        JdbcClient.StatementSpec statement = jdbcClient.sql(shape.statement(after.isPresent()))
                .param("ownerAccountId", ownerAccountId)
                .param("maximumCount", maximumCount);
        if (shape.hasText()) {
            TextMatch textMatch = query.textMatch().orElseThrow();
            statement = statement
                    .param("textCategories", codes(textMatch.categories()))
                    .param("textSubtypes", codes(textMatch.subtypes()))
                    .param("textColors", codes(textMatch.colors()))
                    .param("textPatterns", codes(textMatch.patterns()))
                    .param("textMaterials", codes(textMatch.materials()))
                    .param("textStyles", codes(textMatch.styles()))
                    .param("brandPattern", containsPattern(textMatch.brandText()));
        }
        if (shape.hasCategory()) {
            statement = statement.param("categories", codes(query.categories()));
        }
        if (shape.hasColor()) {
            statement = statement.param("colors", codes(query.colors()));
        }
        if (after.isPresent()) {
            statement = statement
                    .param("afterCreatedAt", after.get().createdAt().atOffset(ZoneOffset.UTC))
                    .param("afterGarmentId", after.get().garmentId());
        }
        return statement.query((row, rowNumber) -> JdbcGarmentRepository.mapGarment(row)).list();
    }

    @Override
    public boolean hasCurrentGarment(UUID ownerAccountId) {
        return jdbcClient.sql(HAS_CURRENT_GARMENT)
                .param("ownerAccountId", ownerAccountId)
                .query(Boolean.class)
                .single();
    }

    /**
     * A LIKE pattern matching the text anywhere, with every LIKE metacharacter literal (OPEN-S4-12): backslash,
     * percent and underscore are escaped with a backslash, which the statement declares as ESCAPE.
     */
    static String containsPattern(String text) {
        StringBuilder pattern = new StringBuilder(text.length() + 8).append('%');
        text.codePoints().forEach(codePoint -> {
            if (codePoint == '\\' || codePoint == '%' || codePoint == '_') {
                pattern.append('\\');
            }
            pattern.appendCodePoint(codePoint);
        });
        return pattern.append('%').toString();
    }

    /** Approved codes as one comma-joined text, in a stable order (codes contain no commas). */
    private static String codes(Set<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
    }
}