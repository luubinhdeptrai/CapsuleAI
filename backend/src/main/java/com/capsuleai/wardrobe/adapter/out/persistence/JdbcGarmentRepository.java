package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentFit;
import com.capsuleai.wardrobe.domain.GarmentLength;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentNeckline;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentSilhouette;
import com.capsuleai.wardrobe.domain.GarmentSleeveLength;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import com.capsuleai.wardrobe.domain.PurchasePrice;
import com.capsuleai.wardrobe.domain.ValueProvenance;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Currency;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link GarmentRepository}. */
public final class JdbcGarmentRepository implements GarmentRepository {

    /** Every garment column plus its Materials and Styles, read in one statement for a consistent snapshot. */
    static final String SELECT_GARMENT = """
            SELECT g.id, g.owner_account_id, g.creation_fingerprint, g.category, g.subtype, g.color,
                   g.pattern, g.fit, g.neckline, g.sleeve_length, g.length, g.silhouette,
                   g.brand, g.price_amount, g.price_currency, g.value_provenance, g.created_at, g.removed_at,
                   ARRAY(SELECT m.material FROM wardrobe.wardrobe_garment_material m WHERE m.garment_id = g.id) AS materials,
                   ARRAY(SELECT s.style FROM wardrobe.wardrobe_garment_style s WHERE s.garment_id = g.id) AS styles
            FROM wardrobe.wardrobe_garment g
            """;

    /**
     * The first page of the owner's current wardrobe. The order matches the partial index
     * ix_wardrobe_garment_owner_current, so PostgreSQL reads one index range and stops after
     * maximumCount rows, without sorting the whole wardrobe.
     */
    static final String SELECT_CURRENT_FIRST_PAGE = SELECT_GARMENT + """
            WHERE g.owner_account_id = :ownerAccountId AND g.removed_at IS NULL
            ORDER BY g.created_at DESC, g.id DESC
            LIMIT :maximumCount
            """;

    /** A later page: strictly after the (created_at, id) position, in the same order. */
    static final String SELECT_CURRENT_PAGE_AFTER = SELECT_GARMENT + """
            WHERE g.owner_account_id = :ownerAccountId AND g.removed_at IS NULL
              AND (g.created_at, g.id) < (:afterCreatedAt, :afterGarmentId)
            ORDER BY g.created_at DESC, g.id DESC
            LIMIT :maximumCount
            """;

    private final JdbcClient jdbc;

    public JdbcGarmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addIfCreationKeyUnused(Garment garment, UUID creationKey, String creationFingerprint) {
        GarmentProfile profile = garment.profile();
        int inserted = jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_garment (
                            id, owner_account_id, creation_key, creation_fingerprint, category, subtype, color,
                            pattern, fit, neckline, sleeve_length, length, silhouette, brand,
                            price_amount, price_currency, value_provenance, created_at)
                        VALUES (
                            :id, :ownerAccountId, :creationKey, :creationFingerprint, :category, :subtype, :color,
                            :pattern, :fit, :neckline, :sleeveLength, :length, :silhouette, :brand,
                            :priceAmount, :priceCurrency, :valueProvenance, :createdAt)
                        ON CONFLICT (owner_account_id, creation_key) DO NOTHING
                        """)
                .param("id", garment.id())
                .param("ownerAccountId", garment.ownerAccountId())
                .param("creationKey", creationKey)
                .param("creationFingerprint", creationFingerprint)
                .param("category", profile.category().name())
                .param("subtype", code(profile.subtype()), Types.VARCHAR)
                .param("color", profile.color().name())
                .param("pattern", code(profile.pattern()), Types.VARCHAR)
                .param("fit", code(profile.fit()), Types.VARCHAR)
                .param("neckline", code(profile.neckline()), Types.VARCHAR)
                .param("sleeveLength", code(profile.sleeveLength()), Types.VARCHAR)
                .param("length", code(profile.length()), Types.VARCHAR)
                .param("silhouette", code(profile.silhouette()), Types.VARCHAR)
                .param("brand", profile.brand().map(Brand::value).orElse(null), Types.VARCHAR)
                .param("priceAmount", profile.price().map(PurchasePrice::amount).orElse(null), Types.NUMERIC)
                .param("priceCurrency", profile.price().map(PurchasePrice::currencyCode).orElse(null), Types.VARCHAR)
                .param("valueProvenance", garment.provenance().name())
                .param("createdAt", toDatabase(garment.createdAt()))
                .update();
        if (inserted == 0) {
            return false;
        }
        for (GarmentMaterial material : profile.materials()) {
            jdbc.sql("INSERT INTO wardrobe.wardrobe_garment_material (garment_id, material) VALUES (:garmentId, :material)")
                    .param("garmentId", garment.id())
                    .param("material", material.name())
                    .update();
        }
        for (GarmentStyle style : profile.styles()) {
            jdbc.sql("INSERT INTO wardrobe.wardrobe_garment_style (garment_id, style) VALUES (:garmentId, :style)")
                    .param("garmentId", garment.id())
                    .param("style", style.name())
                    .update();
        }
        return true;
    }

    @Override
    public Optional<RecordedGarmentCreation> findCreation(UUID ownerAccountId, UUID creationKey) {
        return jdbc.sql(SELECT_GARMENT + "WHERE g.owner_account_id = :ownerAccountId AND g.creation_key = :creationKey")
                .param("ownerAccountId", ownerAccountId)
                .param("creationKey", creationKey)
                .query((row, rowNumber) ->
                        new RecordedGarmentCreation(mapGarment(row), row.getString("creation_fingerprint")))
                .optional();
    }

    @Override
    public List<Garment> findCurrentByOwner(UUID ownerAccountId, Optional<WardrobePosition> after, int maximumCount) {
        if (after.isEmpty()) {
            return jdbc.sql(SELECT_CURRENT_FIRST_PAGE)
                    .param("ownerAccountId", ownerAccountId)
                    .param("maximumCount", maximumCount)
                    .query((row, rowNumber) -> mapGarment(row))
                    .list();
        }
        return jdbc.sql(SELECT_CURRENT_PAGE_AFTER)
                .param("ownerAccountId", ownerAccountId)
                .param("afterCreatedAt", toDatabase(after.get().createdAt()))
                .param("afterGarmentId", after.get().garmentId())
                .param("maximumCount", maximumCount)
                .query((row, rowNumber) -> mapGarment(row))
                .list();
    }

    @Override
    public Optional<Garment> findOwned(UUID ownerAccountId, UUID garmentId) {
        return jdbc.sql(SELECT_GARMENT + "WHERE g.owner_account_id = :ownerAccountId AND g.id = :garmentId")
                .param("ownerAccountId", ownerAccountId)
                .param("garmentId", garmentId)
                .query((row, rowNumber) -> mapGarment(row))
                .optional();
    }

    static Garment mapGarment(ResultSet row) throws SQLException {
        BigDecimal priceAmount = row.getBigDecimal("price_amount");
        Optional<PurchasePrice> price = priceAmount == null
                ? Optional.empty()
                : Optional.of(new PurchasePrice(priceAmount, Currency.getInstance(row.getString("price_currency"))));
        GarmentProfile profile = new GarmentProfile(
                required(GarmentCategory.class, row.getString("category")),
                optional(GarmentSubtype.class, row.getString("subtype")),
                required(GarmentColor.class, row.getString("color")),
                codes(GarmentMaterial.class, row.getArray("materials")),
                optional(GarmentPattern.class, row.getString("pattern")),
                codes(GarmentStyle.class, row.getArray("styles")),
                optional(GarmentFit.class, row.getString("fit")),
                optional(GarmentNeckline.class, row.getString("neckline")),
                optional(GarmentSleeveLength.class, row.getString("sleeve_length")),
                optional(GarmentLength.class, row.getString("length")),
                optional(GarmentSilhouette.class, row.getString("silhouette")),
                Optional.ofNullable(row.getString("brand")).map(Brand::new),
                price);
        return new Garment(
                row.getObject("id", UUID.class),
                row.getObject("owner_account_id", UUID.class),
                profile,
                ValueProvenance.valueOf(row.getString("value_provenance")),
                fromDatabase(row, "created_at"),
                fromDatabase(row, "removed_at"));
    }

    /** The distinct stored codes of a multi-valued property (Materials, Styles) as a catalogue-ordered set. */
    private static <E extends Enum<E> & ControlledValue> Set<E> codes(Class<E> vocabulary, Array column)
            throws SQLException {
        Set<E> values = EnumSet.noneOf(vocabulary);
        Arrays.stream((String[]) column.getArray())
                .map(code -> required(vocabulary, code))
                .forEach(values::add);
        return values;
    }

    private static <E extends Enum<E> & ControlledValue> E required(Class<E> vocabulary, String code) {
        return ControlledValue.fromCode(vocabulary, code)
                .orElseThrow(() -> new IllegalStateException("Stored " + vocabulary.getSimpleName() + " is unknown"));
    }

    private static <E extends Enum<E> & ControlledValue> Optional<E> optional(Class<E> vocabulary, String code) {
        return code == null ? Optional.empty() : Optional.of(required(vocabulary, code));
    }

    private static String code(Optional<? extends ControlledValue> value) {
        return value.map(ControlledValue::name).orElse(null);
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static Instant fromDatabase(ResultSet row, String column) throws SQLException {
        OffsetDateTime value = row.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
}