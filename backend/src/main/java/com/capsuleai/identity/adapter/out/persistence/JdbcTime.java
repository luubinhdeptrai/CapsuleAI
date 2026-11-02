package com.capsuleai.identity.adapter.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Converts between Java {@link Instant} and PostgreSQL {@code timestamptz}. */
final class JdbcTime {

    private JdbcTime() {
    }

    static OffsetDateTime toDatabase(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    static Instant fromDatabase(ResultSet row, String column) throws SQLException {
        OffsetDateTime value = row.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}