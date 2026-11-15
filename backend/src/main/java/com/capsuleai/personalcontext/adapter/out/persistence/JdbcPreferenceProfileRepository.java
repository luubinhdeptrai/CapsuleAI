package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import com.capsuleai.personalcontext.domain.CommonNeed;
import com.capsuleai.personalcontext.domain.DeclaredPreferences;
import com.capsuleai.personalcontext.domain.NeedPriority;
import com.capsuleai.personalcontext.domain.PreferenceProfile;
import com.capsuleai.personalcontext.domain.ProfileStyle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link PreferenceProfileRepository} (V11, schema personal_context). */
public final class JdbcPreferenceProfileRepository implements PreferenceProfileRepository {

    /** The declaration with its styles and needs, read in one statement for a consistent snapshot. */
    static final String SELECT_PROFILE = """
            SELECT p.owner_account_id, p.revision, p.created_at, p.updated_at,
                   ARRAY(SELECT s.style FROM personal_context.personal_context_preferred_style s
                         WHERE s.owner_account_id = p.owner_account_id) AS styles,
                   ARRAY(SELECT n.need || ':' || n.priority FROM personal_context.personal_context_common_need n
                         WHERE n.owner_account_id = p.owner_account_id) AS needs
            FROM personal_context.personal_context_preferences p
            WHERE p.owner_account_id = :ownerAccountId
            """;

    private final JdbcClient jdbc;

    public JdbcPreferenceProfileRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<PreferenceProfile> find(UUID ownerAccountId) {
        return jdbc.sql(SELECT_PROFILE)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> mapProfile(row))
                .optional();
    }

    @Override
    public boolean createIfAbsent(PreferenceProfile profile) {
        int inserted = jdbc.sql("""
                        INSERT INTO personal_context.personal_context_preferences (
                            owner_account_id, revision, created_at, updated_at)
                        VALUES (:ownerAccountId, :revision, :createdAt, :updatedAt)
                        ON CONFLICT (owner_account_id) DO NOTHING
                        """)
                .param("ownerAccountId", profile.ownerAccountId())
                .param("revision", profile.revision())
                .param("createdAt", toDatabase(profile.createdAt()))
                .param("updatedAt", toDatabase(profile.updatedAt()))
                .update();
        if (inserted == 0) {
            return false;
        }
        insertDeclarations(profile.ownerAccountId(), profile.preferences());
        return true;
    }

    @Override
    public boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, DeclaredPreferences preferences,
                                     Instant updatedAt) {
        int updated = jdbc.sql("""
                        UPDATE personal_context.personal_context_preferences
                        SET revision = revision + 1, updated_at = :updatedAt
                        WHERE owner_account_id = :ownerAccountId AND revision = :expectedRevision
                        """)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .param("updatedAt", toDatabase(updatedAt))
                .update();
        if (updated == 0) {
            return false;
        }
        // The UPDATE holds the profile row lock until commit; no other change can interleave.
        jdbc.sql("DELETE FROM personal_context.personal_context_preferred_style WHERE owner_account_id = :owner")
                .param("owner", ownerAccountId)
                .update();
        jdbc.sql("DELETE FROM personal_context.personal_context_common_need WHERE owner_account_id = :owner")
                .param("owner", ownerAccountId)
                .update();
        insertDeclarations(ownerAccountId, preferences);
        return true;
    }

    private void insertDeclarations(UUID ownerAccountId, DeclaredPreferences preferences) {
        for (ProfileStyle style : preferences.styles()) {
            jdbc.sql("""
                            INSERT INTO personal_context.personal_context_preferred_style (owner_account_id, style)
                            VALUES (:owner, :style)
                            """)
                    .param("owner", ownerAccountId)
                    .param("style", style.name())
                    .update();
        }
        for (Map.Entry<CommonNeed, NeedPriority> need : preferences.commonNeeds().entrySet()) {
            jdbc.sql("""
                            INSERT INTO personal_context.personal_context_common_need (owner_account_id, need, priority)
                            VALUES (:owner, :need, :priority)
                            """)
                    .param("owner", ownerAccountId)
                    .param("need", need.getKey().name())
                    .param("priority", (short) need.getValue().weight())
                    .update();
        }
    }

    static PreferenceProfile mapProfile(ResultSet row) throws SQLException {
        Set<ProfileStyle> styles = EnumSet.noneOf(ProfileStyle.class);
        for (String code : (String[]) row.getArray("styles").getArray()) {
            styles.add(ProfileStyle.fromCode(code)
                    .orElseThrow(() -> new IllegalStateException("Stored profile style is unknown")));
        }
        Map<CommonNeed, NeedPriority> needs = new EnumMap<>(CommonNeed.class);
        for (String entry : (String[]) row.getArray("needs").getArray()) {
            String[] parts = entry.split(":", 2);
            needs.put(CommonNeed.fromCode(parts[0])
                            .orElseThrow(() -> new IllegalStateException("Stored common need is unknown")),
                    NeedPriority.fromWeight(Integer.parseInt(parts[1]))
                            .orElseThrow(() -> new IllegalStateException("Stored need priority is unknown")));
        }
        return new PreferenceProfile(
                row.getObject("owner_account_id", UUID.class),
                new DeclaredPreferences(styles, needs),
                row.getLong("revision"),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}