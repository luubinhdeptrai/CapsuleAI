package com.capsuleai.personalcontext.adapter.out.persistence;

import com.capsuleai.personalcontext.application.ports.OptionalContextRepository;
import com.capsuleai.personalcontext.domain.BodyShape;
import com.capsuleai.personalcontext.domain.GenderContext;
import com.capsuleai.personalcontext.domain.OptionalContext;
import com.capsuleai.personalcontext.domain.OptionalContextProfile;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link OptionalContextRepository} (V13, schema personal_context). */
public final class JdbcOptionalContextRepository implements OptionalContextRepository {

    static final String SELECT_OPTIONAL_CONTEXT = """
            SELECT owner_account_id, gender_context, body_shape, revision, created_at, updated_at
            FROM personal_context.personal_context_optional_context
            WHERE owner_account_id = :ownerAccountId
            """;

    static final String CREATE_IF_ABSENT = """
            INSERT INTO personal_context.personal_context_optional_context (
                owner_account_id, gender_context, body_shape, revision, created_at, updated_at)
            VALUES (:ownerAccountId, :genderContext, :bodyShape, :revision, :createdAt, :updatedAt)
            ON CONFLICT (owner_account_id) DO NOTHING
            """;

    static final String REVISE_IF_CURRENT_AT = """
            UPDATE personal_context.personal_context_optional_context
            SET gender_context = :genderContext, body_shape = :bodyShape, revision = revision + 1,
                updated_at = :updatedAt
            WHERE owner_account_id = :ownerAccountId AND revision = :expectedRevision
            """;

    private final JdbcClient jdbcClient;

    public JdbcOptionalContextRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<OptionalContextProfile> find(UUID ownerAccountId) {
        return jdbcClient.sql(SELECT_OPTIONAL_CONTEXT)
                .param("ownerAccountId", ownerAccountId)
                .query((row, rowNumber) -> mapProfile(row))
                .optional();
    }

    @Override
    public boolean createIfAbsent(OptionalContextProfile profile) {
        return jdbcClient.sql(CREATE_IF_ABSENT)
                .param("ownerAccountId", profile.ownerAccountId())
                .param("genderContext", profile.values().genderContext().map(Enum::name).orElse(null), Types.VARCHAR)
                .param("bodyShape", profile.values().bodyShape().map(Enum::name).orElse(null), Types.VARCHAR)
                .param("revision", profile.revision())
                .param("createdAt", toDatabase(profile.createdAt()))
                .param("updatedAt", toDatabase(profile.updatedAt()))
                .update() == 1;
    }

    @Override
    public boolean reviseIfCurrentAt(UUID ownerAccountId, long expectedRevision, OptionalContext values,
                                     Instant updatedAt) {
        return jdbcClient.sql(REVISE_IF_CURRENT_AT)
                .param("ownerAccountId", ownerAccountId)
                .param("expectedRevision", expectedRevision)
                .param("genderContext", values.genderContext().map(Enum::name).orElse(null), Types.VARCHAR)
                .param("bodyShape", values.bodyShape().map(Enum::name).orElse(null), Types.VARCHAR)
                .param("updatedAt", toDatabase(updatedAt))
                .update() == 1;
    }

    private static OptionalContextProfile mapProfile(ResultSet row) throws SQLException {
        return new OptionalContextProfile(
                row.getObject("owner_account_id", UUID.class),
                new OptionalContext(
                        Optional.ofNullable(row.getString("gender_context")).map(code -> GenderContext.fromCode(code)
                                .orElseThrow(() -> new IllegalStateException("Stored gender context is unknown"))),
                        Optional.ofNullable(row.getString("body_shape")).map(code -> BodyShape.fromCode(code)
                                .orElseThrow(() -> new IllegalStateException("Stored body shape is unknown")))),
                row.getLong("revision"),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("updated_at", OffsetDateTime.class).toInstant());
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}