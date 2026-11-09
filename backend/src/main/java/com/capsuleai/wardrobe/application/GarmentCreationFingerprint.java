package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.Brand;
import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentProfile;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SHA-256 of the accepted values of one addition. A retry with the same
 * Idempotency-Key must carry the same values; a different fingerprint means the
 * client reused a key for a different garment, which is rejected (ADA-009).
 * Materials and Styles are sets in catalogue order, so the selection order and
 * repeated selections do not change the fingerprint.
 */
final class GarmentCreationFingerprint {

    private GarmentCreationFingerprint() {
    }

    static String of(GarmentProfile profile) {
        String canonical = String.join("|",
                "category=" + profile.category().name(),
                "subtype=" + code(profile.subtype()),
                "color=" + profile.color().name(),
                "materials=" + profile.materials().stream().map(GarmentMaterial::name).collect(Collectors.joining(",")),
                "pattern=" + code(profile.pattern()),
                "styles=" + profile.styles().stream().map(GarmentStyle::name).collect(Collectors.joining(",")),
                "fit=" + code(profile.fit()),
                "neckline=" + code(profile.neckline()),
                "sleeveLength=" + code(profile.sleeveLength()),
                "length=" + code(profile.length()),
                "silhouette=" + code(profile.silhouette()),
                // Length-prefixed, so brand text containing '|' cannot be confused with another field.
                "brand=" + profile.brand().map(Brand::value).map(value -> value.length() + ":" + value).orElse(""),
                "price=" + profile.price().map(price -> price.amountText() + " " + price.currencyCode()).orElse(""));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", impossible);
        }
    }

    private static String code(Optional<? extends ControlledValue> value) {
        return value.map(ControlledValue::name).orElse("");
    }
}