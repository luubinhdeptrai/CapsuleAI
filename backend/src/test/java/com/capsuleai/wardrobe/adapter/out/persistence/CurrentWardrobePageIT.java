package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The keyset query at the SQL level (OPEN-S2-10): PostgreSQL's own uuid order breaks
 * creation-time ties, and a page is read through the current-wardrobe index in index
 * order, so the database never sorts the whole wardrobe to return one page.
 */
class CurrentWardrobePageIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private GarmentRepository garments;

    @Autowired
    private WardrobeTransactions transactions;

    private final UUID owner = UUID.randomUUID();

    @Test
    void creationTimeTiesFollowTheDatabaseIdOrderAndEveryGarmentIsVisitedOnce() {
        Instant sameInstant = Instant.parse("2026-01-05T08:00:00.123456Z");
        for (int count = 0; count < 5; count++) {
            store(sameInstant);
        }
        List<UUID> databaseOrder = jdbc.sql("""
                        SELECT id::text FROM wardrobe.wardrobe_garment
                        WHERE owner_account_id = :owner ORDER BY created_at DESC, id DESC
                        """)
                .param("owner", owner).query(String.class).list().stream().map(UUID::fromString).toList();

        List<UUID> visited = new ArrayList<>();
        Optional<WardrobePosition> after = Optional.empty();
        List<Garment> page;
        do {
            page = garments.findCurrentByOwner(owner, after, 2);
            page.forEach(garment -> visited.add(garment.id()));
            after = page.isEmpty() ? after : Optional.of(WardrobePosition.of(page.getLast()));
        } while (page.size() == 2);

        assertThat(visited).containsExactlyElementsOf(databaseOrder);
    }

    @Test
    void pagesHoldOnlyTheOwnersCurrentGarmentsEvenAcrossSharedCreationTimes() {
        Instant sameInstant = Instant.parse("2026-01-05T08:00:00.123456Z");
        UUID someoneElse = UUID.randomUUID();
        List<UUID> current = new ArrayList<>();
        for (int count = 0; count < 5; count++) {
            current.add(store(owner, sameInstant));
            store(someoneElse, sameInstant);
        }
        UUID removed = current.removeFirst();
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at WHERE id = :id")
                .param("id", removed).update();

        List<UUID> visited = new ArrayList<>();
        Optional<WardrobePosition> after = Optional.empty();
        List<Garment> page;
        do {
            page = garments.findCurrentByOwner(owner, after, 2);
            page.forEach(garment -> visited.add(garment.id()));
            after = page.isEmpty() ? after : Optional.of(WardrobePosition.of(page.getLast()));
        } while (page.size() == 2);

        assertThat(visited).hasSize(4).containsExactlyInAnyOrderElementsOf(current);
    }

    @Test
    void aLaterPageIsReadInIndexOrderWithoutSorting() {
        Instant start = Instant.parse("2026-01-05T08:00:00Z");
        for (int second = 0; second < 50; second++) {
            store(start.plusSeconds(second));
        }
        jdbc.sql("ANALYZE wardrobe.wardrobe_garment").update();

        List<String> plan = transactions.inTransaction(() -> {
            // Rule out the sequential scan a 50-row test table would otherwise invite.
            jdbc.sql("SET LOCAL enable_seqscan = off").update();
            return jdbc.sql("EXPLAIN " + JdbcGarmentRepository.SELECT_CURRENT_PAGE_AFTER)
                    .param("ownerAccountId", owner)
                    .param("afterCreatedAt", OffsetDateTime.parse("2026-01-05T08:00:30Z"))
                    .param("afterGarmentId", UUID.randomUUID())
                    .param("maximumCount", 21)
                    .query(String.class)
                    .list();
        });

        assertThat(String.join("\n", plan))
                .contains("ix_wardrobe_garment_owner_current")
                .doesNotContain("Sort");
    }

    private void store(Instant createdAt) {
        store(owner, createdAt);
    }

    private UUID store(UUID ownerAccountId, Instant createdAt) {
        Garment garment = Garment.confirmManualEntry(UUID.randomUUID(), ownerAccountId,
                GarmentProfile.minimum(GarmentCategory.TOPS, GarmentColor.RED), createdAt);
        transactions.inTransaction(() -> garments.addIfCreationKeyUnused(garment, UUID.randomUUID(), "a".repeat(64)));
        return garment.id();
    }
}
