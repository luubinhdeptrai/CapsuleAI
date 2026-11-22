package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.GarmentPhotoAttachmentRepository;
import com.capsuleai.wardrobe.domain.Garment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link GarmentPhotoAttachmentRepository} (provisional US-GAR-006). */
public final class JdbcGarmentPhotoAttachmentRepository implements GarmentPhotoAttachmentRepository {

    /**
     * Step 1: lock only the garment row. When this statement had to wait for a competing transaction,
     * PostgreSQL (READ COMMITTED) re-checks the locked row only; rows joined from other tables would still
     * come from the statement's original snapshot. So the photo is never read in this statement.
     */
    static final String LOCK_OWNED_GARMENT_ROW = """
            SELECT id FROM wardrobe.wardrobe_garment
            WHERE owner_account_id = :ownerAccountId AND id = :garmentId
            FOR UPDATE
            """;

    /**
     * Step 2: a new statement, so a new READ COMMITTED snapshot taken after the lock was granted: the garment
     * and its attached photo as committed by any transaction that held the lock before.
     */
    static final String SELECT_OWNED_GARMENT = JdbcGarmentRepository.SELECT_GARMENT + """
            WHERE g.owner_account_id = :ownerAccountId AND g.id = :garmentId
            """;

    static final String ADVANCE_REVISION_IF_CURRENT_AT = """
            UPDATE wardrobe.wardrobe_garment
            SET revision = revision + 1
            WHERE id = :garmentId AND owner_account_id = :ownerAccountId
              AND removed_at IS NULL AND revision = :expectedRevision
            """;

    private final JdbcClient jdbcClient;

    public JdbcGarmentPhotoAttachmentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Garment> lockOwnedGarment(UUID ownerAccountId, UUID garmentId) {
        Optional<UUID> lockedGarmentId = jdbcClient.sql(LOCK_OWNED_GARMENT_ROW)
                .param("ownerAccountId", ownerAccountId)
                .param("garmentId", garmentId)
                .query(UUID.class)
                .optional();
        if (lockedGarmentId.isEmpty()) {
            return Optional.empty();
        }
        return jdbcClient.sql(SELECT_OWNED_GARMENT)
                .param("ownerAccountId", ownerAccountId)
                .param("garmentId", garmentId)
                .query((row, rowNumber) -> JdbcGarmentRepository.mapGarment(row))
                .optional();
    }

    @Override
    public boolean advanceRevisionIfCurrentAt(UUID ownerAccountId, UUID garmentId, long expectedRevision) {
        return jdbcClient.sql(ADVANCE_REVISION_IF_CURRENT_AT)
                .param("garmentId", garmentId)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .update() == 1;
    }
}
