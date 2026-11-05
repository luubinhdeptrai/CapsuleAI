package com.capsuleai.architecture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Focused SQL ownership checks for architecture refinements AR-01 to AR-04: a module's SQL
 * names only tables of its own schema, always schema-qualified; a migration after the
 * Identity relocation changes exactly one owner schema, so it cannot create a
 * cross-context foreign key, view or join.
 *
 * <p>This is deliberately not a SQL parser. It finds references to tables of the
 * {@link BusinessModule registered owners}: {@code schema.table} where {@code schema} is an
 * owner schema, a known table name (one created by any migration) used without a schema,
 * and an owner-prefixed name following a table keyword such as {@code FROM} or
 * {@code REFERENCES}. Other identifiers (columns, aliases, constraint names, English text)
 * are ignored. Conventions that keep it reliable: SQL is written in one string literal or
 * text block per statement, identifiers are lower case and never double-quoted, and a
 * table name is never assembled from fragments in production code.
 *
 * <p>The schema-metadata test ({@code DatabaseSchemaOwnershipIT}) checks the migrated
 * database itself, so a reference this scanner cannot see still cannot produce a
 * cross-schema foreign key or a table in the wrong schema.
 */
final class SqlOwnershipRules {

    /** Migrations written before AR-01 (immutable) and the one administrative relocation. */
    static final Map<Integer, String> EXEMPT_MIGRATIONS = Map.of(
            1, "Pre-sprint foundation; applied before AR-01 and never edited.",
            2, "Sprint 1 Identity table created in Flyway's default schema before AR-01; applied migrations are never edited.",
            3, "Sprint 1 Identity tables created in Flyway's default schema before AR-01; applied migrations are never edited.",
            4, "Sprint 1 Identity change applied before AR-01; never edited.",
            5, "Sprint 1 Identity change applied before AR-01; never edited.",
            6, "Administrative relocation of the legacy Identity tables from Flyway's default schema into the identity schema (AR-01).");

    record SqlText(String text, int firstLine) {
    }

    record TableReference(String schema, String table, int line) {

        boolean qualified() {
            return schema != null;
        }

        @Override
        public String toString() {
            return (qualified() ? schema + "." : "") + table + " (line " + line + ")";
        }
    }

    private static final Set<String> OWNER_SCHEMAS = schemas();
    private static final Pattern QUALIFIED = Pattern.compile("(?<![\\w.$])([a-z][a-z0-9_]*)\\.([a-z][a-z0-9_]*)\\b");
    private static final Pattern IDENTIFIER = Pattern.compile("(?<![\\w.$\"])([a-z][a-z0-9_]*)\\b(?!\\s*\\.)");
    private static final Pattern AFTER_TABLE_KEYWORD = Pattern.compile(
            "(?i:\\b(?:FROM|JOIN|INTO|UPDATE|TABLE|REFERENCES|ON|TRUNCATE)\\s+(?:ONLY\\s+)?(?:IF\\s+(?:NOT\\s+)?EXISTS\\s+)?)"
                    + "([a-z][a-z0-9_]*)\\b(?!\\s*\\.)");
    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?i:\\bCREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?)(?:([a-z][a-z0-9_]*)\\.)?([a-z][a-z0-9_]*)");
    private static final Pattern CREATE_SCHEMA = Pattern.compile(
            "(?i:\\bCREATE\\s+SCHEMA\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?)([a-z][a-z0-9_]*)");
    private static final Pattern MIGRATION_VERSION = Pattern.compile("^V(\\d+)(?:[._]\\d+)*__.+\\.sql$");

    private SqlOwnershipRules() {
    }

    // ----------------------------------------------------------------- rules

    /**
     * Violations in one production Java source file.
     *
     * @param owner the module the file belongs to, or empty for code outside every module
     */
    static List<String> javaSourceViolations(Optional<BusinessModule> owner, String fileLabel, String source,
                                             Set<String> knownTables) {
        List<String> violations = new ArrayList<>();
        for (SqlText literal : javaStringLiterals(source)) {
            for (TableReference reference : tableReferences(literal, knownTables)) {
                if (!reference.qualified()) {
                    violations.add(fileLabel + ": unqualified table " + reference
                            + "; write " + expectedQualification(reference.table()));
                    continue;
                }
                BusinessModule schemaOwner = BusinessModule.ofSchema(reference.schema()).orElseThrow();
                if (owner.isEmpty()) {
                    violations.add(fileLabel + ": code outside every module uses " + reference
                            + "; only the owning module " + schemaOwner.packageName() + " may");
                } else if (owner.get() != schemaOwner) {
                    violations.add(fileLabel + ": module " + owner.get().packageName() + " uses " + reference
                            + ", which module " + schemaOwner.packageName()
                            + " owns; call its published api instead");
                } else if (!reference.table().startsWith(schemaOwner.tablePrefix())) {
                    violations.add(fileLabel + ": table " + reference + " must start with "
                            + schemaOwner.tablePrefix());
                }
            }
        }
        return violations;
    }

    /** Violations in one Flyway migration; legacy and relocation migrations are exempt. */
    static List<String> migrationViolations(String fileName, String sql, Set<String> knownTables) {
        int version = migrationVersion(fileName);
        if (EXEMPT_MIGRATIONS.containsKey(version)) {
            return List.of();
        }
        String text = withoutSqlComments(sql);
        List<String> violations = new ArrayList<>();
        if (text.contains("${flyway:defaultSchema}")) {
            violations.add(fileName + ": only the V6 relocation may address Flyway's default schema");
        }
        Set<String> schemasUsed = new TreeSet<>();
        for (TableReference reference : tableReferences(new SqlText(text, 1), knownTables)) {
            if (!reference.qualified()) {
                violations.add(fileName + ": unqualified table " + reference
                        + "; write " + expectedQualification(reference.table()));
            } else {
                schemasUsed.add(reference.schema());
            }
        }
        Matcher createSchema = CREATE_SCHEMA.matcher(text);
        while (createSchema.find()) {
            String schema = createSchema.group(1);
            if (!OWNER_SCHEMAS.contains(schema)) {
                violations.add(fileName + ": schema " + schema + " is not a registered owner schema");
            }
            schemasUsed.add(schema);
        }
        Matcher createTable = CREATE_TABLE.matcher(text);
        while (createTable.find()) {
            String schema = createTable.group(1);
            String table = createTable.group(2);
            if (schema != null && OWNER_SCHEMAS.contains(schema) && !table.startsWith(schema + "_")) {
                violations.add(fileName + ": table " + schema + "." + table + " must start with " + schema + "_");
            }
        }
        if (schemasUsed.size() > 1) {
            violations.add(fileName + ": changes several owner schemas " + schemasUsed
                    + "; one migration belongs to one owner, and no foreign key, view or join may cross owners");
        }
        return violations;
    }

    /** Every table name created by any migration, with or without a schema. */
    static Set<String> knownTables(Collection<String> migrationTexts) {
        Set<String> tables = new TreeSet<>();
        for (String migration : migrationTexts) {
            Matcher createTable = CREATE_TABLE.matcher(withoutSqlComments(migration));
            while (createTable.find()) {
                tables.add(createTable.group(2));
            }
        }
        return tables;
    }

    static int migrationVersion(String fileName) {
        Matcher matcher = MIGRATION_VERSION.matcher(fileName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a versioned Flyway migration name: " + fileName);
        }
        return Integer.parseInt(matcher.group(1));
    }

    // ------------------------------------------------------------- scanning

    /** References to owner tables in one SQL text, each at most once per position. */
    static List<TableReference> tableReferences(SqlText sql, Set<String> knownTables) {
        String text = sql.text();
        List<TableReference> references = new ArrayList<>();
        Set<Integer> positions = new LinkedHashSet<>();
        Matcher qualified = QUALIFIED.matcher(text);
        while (qualified.find()) {
            if (OWNER_SCHEMAS.contains(qualified.group(1)) && positions.add(qualified.start())) {
                references.add(new TableReference(qualified.group(1), qualified.group(2),
                        lineOf(sql, qualified.start())));
            }
        }
        Matcher identifier = IDENTIFIER.matcher(text);
        while (identifier.find()) {
            if (knownTables.contains(identifier.group(1)) && positions.add(identifier.start(1))) {
                references.add(new TableReference(null, identifier.group(1), lineOf(sql, identifier.start(1))));
            }
        }
        Matcher afterKeyword = AFTER_TABLE_KEYWORD.matcher(text);
        while (afterKeyword.find()) {
            String name = afterKeyword.group(1);
            if (BusinessModule.ofTableName(name).isPresent() && positions.add(afterKeyword.start(1))) {
                references.add(new TableReference(null, name, lineOf(sql, afterKeyword.start(1))));
            }
        }
        return references;
    }

    /** String literals and text blocks of a Java source, skipping comments and char literals. */
    static List<SqlText> javaStringLiterals(String source) {
        List<SqlText> literals = new ArrayList<>();
        int line = 1;
        int index = 0;
        int length = source.length();
        while (index < length) {
            char current = source.charAt(index);
            if (current == '\n') {
                line++;
                index++;
            } else if (source.startsWith("//", index)) {
                while (index < length && source.charAt(index) != '\n') {
                    index++;
                }
            } else if (source.startsWith("/*", index)) {
                int end = source.indexOf("*/", index + 2);
                end = end < 0 ? length : end + 2;
                line += newlines(source, index, end);
                index = end;
            } else if (current == '\'') {
                index++;
                while (index < length && source.charAt(index) != '\'') {
                    index += source.charAt(index) == '\\' ? 2 : 1;
                }
                index++;
            } else if (source.startsWith("\"\"\"", index)) {
                int start = index + 3;
                int end = start;
                while (end < length && !(source.startsWith("\"\"\"", end) && !escaped(source, end))) {
                    end++;
                }
                literals.add(new SqlText(source.substring(start, Math.min(end, length)), line));
                line += newlines(source, start, Math.min(end, length));
                index = end + 3;
            } else if (current == '"') {
                StringBuilder content = new StringBuilder();
                int end = index + 1;
                while (end < length && source.charAt(end) != '"' && source.charAt(end) != '\n') {
                    if (source.charAt(end) == '\\' && end + 1 < length) {
                        content.append(source.charAt(end + 1));
                        end += 2;
                    } else {
                        content.append(source.charAt(end));
                        end++;
                    }
                }
                literals.add(new SqlText(content.toString(), line));
                index = end + 1;
            } else {
                index++;
            }
        }
        return literals;
    }

    /** Removes {@code --} and block comments, keeping line breaks so line numbers stay true. */
    static String withoutSqlComments(String sql) {
        StringBuilder result = new StringBuilder(sql.length());
        int index = 0;
        boolean inQuote = false;
        while (index < sql.length()) {
            char current = sql.charAt(index);
            if (current == '\'') {
                inQuote = !inQuote;
                result.append(current);
                index++;
            } else if (!inQuote && sql.startsWith("--", index)) {
                while (index < sql.length() && sql.charAt(index) != '\n') {
                    index++;
                }
            } else if (!inQuote && sql.startsWith("/*", index)) {
                int end = sql.indexOf("*/", index + 2);
                end = end < 0 ? sql.length() : end + 2;
                result.append("\n".repeat(newlines(sql, index, end)));
                index = end;
            } else {
                result.append(current);
                index++;
            }
        }
        return result.toString();
    }

    // -------------------------------------------------------------- helpers

    private static String expectedQualification(String table) {
        return BusinessModule.ofTableName(table)
                .map(owner -> owner.schema() + "." + table)
                .orElse("<owner schema>." + table);
    }

    private static int lineOf(SqlText sql, int offset) {
        return sql.firstLine() + newlines(sql.text(), 0, offset);
    }

    private static int newlines(String text, int from, int to) {
        int count = 0;
        for (int index = from; index < to; index++) {
            if (text.charAt(index) == '\n') {
                count++;
            }
        }
        return count;
    }

    private static boolean escaped(String text, int index) {
        int backslashes = 0;
        for (int position = index - 1; position >= 0 && text.charAt(position) == '\\'; position--) {
            backslashes++;
        }
        return backslashes % 2 == 1;
    }

    private static Set<String> schemas() {
        Set<String> schemas = new TreeSet<>();
        for (BusinessModule module : BusinessModule.values()) {
            schemas.add(module.schema());
        }
        return Set.copyOf(schemas);
    }
}
