package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.GarmentPhotoAttachmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-006 with real transactions on PostgreSQL READ COMMITTED (S4-GAR-05, S4-INT-01, OPEN-S4-08):
 * competing photos for one garment, one photo for competing garments, and attachment against removal or an
 * edit each have exactly one coherent winner and no half-association; a failure after the photo is claimed
 * rolls back the whole attachment.
 */
class ConcurrentGarmentPhotoAttachmentIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private AttachGarmentPhotoService attachGarmentPhotoService;

    @Autowired
    private RemoveGarmentService removeGarmentService;

    @Autowired
    private ReviseGarmentService reviseGarmentService;

    @Autowired
    private GarmentPhotoAttachmentRepository garmentPhotoAttachmentRepository;

    @Autowired
    private PhotoUploadRepository photoUploadRepository;

    @Autowired
    private WardrobeTransactions wardrobeTransactions;

    @Test
    void ofTwoPhotosCompetingForOneGarmentExactlyOneIsAttached() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-attach-two-photos@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        UUID firstPhotoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        UUID secondPhotoId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(640, 640));

        List<String> outcomes = race(attachment(tokens.accountId(), garmentId, firstPhotoId),
                attachment(tokens.accountId(), garmentId, secondPhotoId));

        assertThat(outcomes).containsExactlyInAnyOrder("attached", "already-has-photo");
        assertThat(jdbc.sql("SELECT count(*) FROM wardrobe.wardrobe_photo_upload WHERE garment_id = :id")
                .param("id", garmentId).query(Long.class).single()).isOne();
        assertThat(List.of(photoUploadStatus(firstPhotoId), photoUploadStatus(secondPhotoId)))
                .containsExactlyInAnyOrder("ATTACHED", "READY");
    }

    @Test
    void ofOnePhotoCompetingForTwoGarmentsExactlyOneReceivesIt() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-attach-two-garments@example.com");
        UUID firstGarmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        UUID secondGarmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "BLACK"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));

        List<String> outcomes = race(attachment(tokens.accountId(), firstGarmentId, photoUploadId),
                attachment(tokens.accountId(), secondGarmentId, photoUploadId));

        assertThat(outcomes).containsExactlyInAnyOrder("attached", "not-usable");
        assertThat(jdbc.sql("""
                        SELECT count(*) FROM wardrobe.wardrobe_garment
                        WHERE id IN (:first, :second) AND revision = 2
                        """)
                .param("first", firstGarmentId).param("second", secondGarmentId)
                .query(Long.class).single()).as("only the receiving garment advanced its revision").isOne();
    }

    @Test
    void anAttachmentAndARemovalOfTheSameRevisionHaveOneCoherentWinner() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-attach-removal@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BROWN"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));

        List<String> outcomes = race(attachment(tokens.accountId(), garmentId, photoUploadId),
                removal(tokens.accountId(), garmentId));

        assertThat(outcomes)
                .as("attachment wins (removal conflicts) or removal wins (attachment finds it gone)")
                .isIn(List.of("attached", "conflict"), List.of("gone", "removed"));
        assertThat(photoUploadStatus(photoUploadId))
                .isEqualTo(outcomes.getFirst().equals("attached") ? "ATTACHED" : "READY");
    }

    @Test
    void anAttachmentAndAnEditOfTheSameRevisionHaveOneCoherentWinner() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-attach-edit@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));

        List<String> outcomes = race(attachment(tokens.accountId(), garmentId, photoUploadId),
                edit(tokens.accountId(), garmentId));

        assertThat(outcomes)
                .as("attachment wins (the edit conflicts) or the edit wins (the attachment conflicts)")
                .isIn(List.of("attached", "conflict"), List.of("conflict", "edited"));
        boolean attachmentWon = outcomes.getFirst().equals("attached");
        Map<String, Object> garmentRow = jdbc.sql("SELECT revision, color FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query().singleRow();
        assertThat(((Number) garmentRow.get("revision")).longValue()).as("exactly one change applied").isEqualTo(2L);
        assertThat(garmentRow.get("color")).isEqualTo(attachmentWon ? "WHITE" : "RED");
        assertThat(photoUploadStatus(photoUploadId)).isEqualTo(attachmentWon ? "ATTACHED" : "READY");
    }

    @Test
    void aFailureAfterThePhotoIsClaimedRollsBackTheWholeAttachment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("attach-rollback@example.com");
        UUID garmentId = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "WHITE"));
        UUID photoUploadId = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 600));
        GarmentPhotoAttachmentRepository failingAfterThePhotoIsClaimed = new GarmentPhotoAttachmentRepository() {
            @Override
            public Optional<Garment> lockOwnedGarment(UUID ownerAccountId, UUID lockedGarmentId) {
                return garmentPhotoAttachmentRepository.lockOwnedGarment(ownerAccountId, lockedGarmentId);
            }

            @Override
            public boolean advanceRevisionIfCurrentAt(UUID ownerAccountId, UUID revisedGarmentId, long expectedRevision) {
                throw new IllegalStateException("simulated failure after the photo was claimed");
            }
        };
        AttachGarmentPhotoService attachmentFailingMidway = new AttachGarmentPhotoService(failingAfterThePhotoIsClaimed,
                photoUploadRepository, wardrobeTransactions, clock);

        assertThatThrownBy(() -> attachmentFailingMidway.attach(new AttachGarmentPhotoCommand(tokens.accountId(),
                garmentId.toString(), true, "1", photoUploadId.toString())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(photoUploadStatus(photoUploadId)).as("the photo claim was rolled back").isEqualTo("READY");
        assertThat(jdbc.sql("SELECT garment_id FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(UUID.class).optional()).as("no half-association").isEmpty();
        assertThat(jdbc.sql("SELECT revision FROM wardrobe.wardrobe_garment WHERE id = :id")
                .param("id", garmentId).query(Long.class).single()).isEqualTo(1L);

        attachGarmentPhotoService.attach(new AttachGarmentPhotoCommand(tokens.accountId(), garmentId.toString(), true,
                "1", photoUploadId.toString()));
        assertThat(photoUploadStatus(photoUploadId)).as("the same reviewed pair still attaches afterwards")
                .isEqualTo("ATTACHED");
    }

    private List<String> race(Callable<String> first, Callable<String> second) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> firstOutcome = executor.submit(() -> {
                start.await(10, SECONDS);
                return first.call();
            });
            Future<String> secondOutcome = executor.submit(() -> {
                start.await(10, SECONDS);
                return second.call();
            });
            return List.of(firstOutcome.get(20, SECONDS), secondOutcome.get(20, SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<String> attachment(UUID owner, UUID garmentId, UUID photoUploadId) {
        return () -> {
            try {
                attachGarmentPhotoService.attach(new AttachGarmentPhotoCommand(owner, garmentId.toString(), true, "1",
                        photoUploadId.toString()));
                return "attached";
            } catch (GarmentAlreadyHasPhotoException alreadyHasPhoto) {
                return "already-has-photo";
            } catch (PhotoUploadNotUsableException notUsable) {
                return "not-usable";
            } catch (GarmentNoLongerCurrentException gone) {
                return "gone";
            } catch (GarmentRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private Callable<String> edit(UUID owner, UUID garmentId) {
        GarmentChanges recolor = new GarmentChanges(FieldChange.keep(), FieldChange.keep(), FieldChange.replace("RED"),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(),
                FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep(), FieldChange.keep());
        return () -> {
            try {
                reviseGarmentService.revise(new ReviseGarmentCommand(owner, garmentId.toString(), true, "1", recolor));
                return "edited";
            } catch (GarmentRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }

    private Callable<String> removal(UUID owner, UUID garmentId) {
        return () -> {
            try {
                GarmentRemoval removal = removeGarmentService.remove(
                        new RemoveGarmentCommand(owner, garmentId.toString(), true, "1"));
                return removal.newlyRemoved() ? "removed" : "already-removed";
            } catch (GarmentRevisionConflictException conflict) {
                return "conflict";
            }
        };
    }
}
