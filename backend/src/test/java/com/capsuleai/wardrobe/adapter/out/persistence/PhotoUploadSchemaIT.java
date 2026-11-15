package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003: the V10 constraints defend the staged-photo lifecycle even if code skipped a check. */
class PhotoUploadSchemaIT extends WardrobeIntegrationTestSupport {

    private static final String SHA = "e".repeat(64);

    @Test
    void aReceivingIntentIsAcceptedAndUploadKeysAreUniquePerOwner() {
        UUID owner = UUID.randomUUID();
        UUID key = UUID.randomUUID();
        insert(UUID.randomUUID(), owner, key, "RECEIVING", null, null, null);

        assertThatThrownBy(() -> insert(UUID.randomUUID(), owner, key, "RECEIVING", null, null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
        insert(UUID.randomUUID(), UUID.randomUUID(), key, "RECEIVING", null, null, null);
        assertThat(countRows("wardrobe.wardrobe_photo_upload")).isEqualTo(2);
    }

    @Test
    void anAttachedPhotoNamesExactlyOneGarmentAndAGarmentHasAtMostOnePhoto() {
        UUID owner = UUID.randomUUID();
        UUID garment = insertGarment(owner);

        assertThatThrownBy(() -> insert(UUID.randomUUID(), owner, UUID.randomUUID(), "ATTACHED", "image/jpeg", 800,
                null)).isInstanceOf(DataIntegrityViolationException.class);
        insert(UUID.randomUUID(), owner, UUID.randomUUID(), "ATTACHED", "image/jpeg", 800, garment);
        assertThatThrownBy(() -> insert(UUID.randomUUID(), owner, UUID.randomUUID(), "ATTACHED", "image/jpeg", 800,
                garment)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aStoredPhotoIsAJpegWithDimensions() {
        assertThatThrownBy(() -> insert(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "READY",
                "image/jpeg", null, null)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insert(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "READY",
                "image/png", 800, null)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyADiscardedPhotoRecordsADeletedObjectAndObjectKeysStayOpaque() {
        UUID ready = UUID.randomUUID();
        insert(ready, UUID.randomUUID(), UUID.randomUUID(), "READY", "image/jpeg", 800, null);

        assertThatThrownBy(() -> jdbc.sql("UPDATE wardrobe.wardrobe_photo_upload SET object_deleted_at = now() "
                + "WHERE id = :id").param("id", ready).update()).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("UPDATE wardrobe.wardrobe_photo_upload SET object_key = 'garments/x.png' "
                + "WHERE id = :id").param("id", ready).update()).isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insert(UUID id, UUID owner, UUID uploadKey, String status, String contentType, Integer size,
                        UUID garmentId) {
        boolean stored = status.equals("READY") || status.equals("ATTACHED");
        jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_photo_upload (id, owner_account_id, upload_key, content_sha256,
                            object_key, status, content_type, width, height, garment_id, created_at, expires_at,
                            ready_at, attached_at)
                        VALUES (:id, :owner, :uploadKey, :sha, :objectKey, :status, :contentType, :width, :height,
                            :garmentId, now(), now() + INTERVAL '1 day',
                            CASE WHEN :stored THEN now() END, CASE WHEN :garmentId::uuid IS NOT NULL THEN now() END)
                        """)
                .param("id", id)
                .param("owner", owner)
                .param("uploadKey", uploadKey)
                .param("sha", SHA)
                .param("objectKey", "photos/" + id + ".jpg")
                .param("status", status)
                .param("contentType", contentType)
                .param("width", size)
                .param("height", size)
                .param("garmentId", garmentId)
                .param("stored", stored)
                .update();
    }

    private UUID insertGarment(UUID owner) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_garment (id, owner_account_id, creation_key, creation_fingerprint,
                            category, color, value_provenance, created_at)
                        VALUES (:id, :owner, :key, :fingerprint, 'BAGS', 'BLACK', 'USER_ENTERED', now())
                        """)
                .param("id", id).param("owner", owner).param("key", UUID.randomUUID())
                .param("fingerprint", "0".repeat(64)).update();
        return id;
    }
}
