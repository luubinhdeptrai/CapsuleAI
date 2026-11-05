package com.capsuleai.architecture;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * The business modules of the Modular Monolith and the PostgreSQL schema each one owns.
 *
 * <p>Five are the logical Bounded Contexts of the accepted Logical View; Product
 * Measurement is the supporting capability that owns its own measurement lifecycle
 * (Implementation View). Each module owns exactly one schema, named after the module,
 * and every table in that schema starts with the schema name and an underscore
 * (architecture refinement AR-01, {@code docs/04-architecture/selected-architecture-decisions.md}
 * Section 8, and {@code docs/engineering/backend-conventions.md} Section 2).
 *
 * <p>This enum is the single list used by the architecture tests: Java module
 * boundaries, SQL ownership and PostgreSQL schema metadata. A schema is listed here
 * before its module persists anything, but it is created only by the migration of the
 * story that first needs it.
 */
public enum BusinessModule {

    IDENTITY("identity", "identity"),
    PERSONAL_CONTEXT("personalcontext", "personal_context"),
    WARDROBE("wardrobe", "wardrobe"),
    OUTFIT_ADVICE_AND_USE("outfitadviceanduse", "outfit_advice_and_use"),
    WARDROBE_INTELLIGENCE("wardrobeintelligence", "wardrobe_intelligence"),
    PRODUCT_MEASUREMENT("productmeasurement", "product_measurement");

    /** Package of all production code. */
    public static final String ROOT_PACKAGE = "com.capsuleai";

    private final String packageName;
    private final String schema;

    BusinessModule(String packageName, String schema) {
        this.packageName = packageName;
        this.schema = schema;
    }

    /** Last package segment below {@link #ROOT_PACKAGE}, for example {@code wardrobeintelligence}. */
    public String packageName() {
        return packageName;
    }

    /** Fully qualified base package, for example {@code com.capsuleai.wardrobe}. */
    public String basePackage() {
        return ROOT_PACKAGE + "." + packageName;
    }

    /** The PostgreSQL schema this module owns, for example {@code wardrobe_intelligence}. */
    public String schema() {
        return schema;
    }

    /** Every table of this module starts with this prefix, for example {@code wardrobe_}. */
    public String tablePrefix() {
        return schema + "_";
    }

    public static Optional<BusinessModule> ofPackageName(String packageName) {
        return Arrays.stream(values()).filter(module -> module.packageName.equals(packageName)).findFirst();
    }

    public static Optional<BusinessModule> ofSchema(String schema) {
        return Arrays.stream(values()).filter(module -> module.schema.equals(schema)).findFirst();
    }

    /**
     * The module a table name belongs to: the module with the longest matching prefix,
     * so {@code wardrobe_intelligence_gap} belongs to Wardrobe Intelligence, not Wardrobe.
     */
    public static Optional<BusinessModule> ofTableName(String tableName) {
        return Arrays.stream(values())
                .filter(module -> tableName.startsWith(module.tablePrefix()))
                .max(Comparator.comparingInt(module -> module.tablePrefix().length()));
    }
}
