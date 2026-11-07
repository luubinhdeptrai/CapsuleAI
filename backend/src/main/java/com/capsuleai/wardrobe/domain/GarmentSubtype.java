package com.capsuleai.wardrobe.domain;

/**
 * Optional subtype; each belongs to exactly one category (FR-GAR-002,
 * BRULE-GAR-004). All 84 approved subtypes are listed. Overlapping names such as
 * MINI_DRESS or PARTY_DRESS do not derive any other property.
 *
 * <p>Labels are the approved values of the garment-property authority
 * ({@code Initial files/Image to Properties Extraction.md}, Section 3). The ten
 * Dresses/Jumpsuits constant names are the approved canonical identifiers.
 */
public enum GarmentSubtype implements ControlledValue {

    // Tops
    T_SHIRTS(GarmentCategory.TOPS, "T-shirts"),
    LONG_SLEEVE_T_SHIRTS(GarmentCategory.TOPS, "Long Sleeve T-shirts"),
    SLEEVELESS_T_SHIRTS(GarmentCategory.TOPS, "Sleeveless T-shirts"),
    POLO_SHIRTS(GarmentCategory.TOPS, "Polo Shirts"),
    TANKS_AND_CAMIS(GarmentCategory.TOPS, "Tanks & Camis"),
    CROP_TOPS(GarmentCategory.TOPS, "Crop Tops"),
    BLOUSES(GarmentCategory.TOPS, "Blouses"),
    SHIRTS(GarmentCategory.TOPS, "Shirts"),
    SWEATSHIRTS(GarmentCategory.TOPS, "Sweatshirts"),
    HOODIES(GarmentCategory.TOPS, "Hoodies"),
    SWEATERS(GarmentCategory.TOPS, "Sweaters"),
    SWEATER_VESTS(GarmentCategory.TOPS, "Sweater Vests"),
    SPORTS_TOPS(GarmentCategory.TOPS, "Sports Tops"),
    BODYSUITS(GarmentCategory.TOPS, "Bodysuits"),

    // Dresses/Jumpsuits
    DAY_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Day Dress"),
    T_SHIRT_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "T-Shirt Dress"),
    SHIRT_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Shirt Dress"),
    SWEATSHIRT_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Sweatshirt Dress"),
    SWEATER_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Sweater Dress"),
    JACKET_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Jacket Dress"),
    SUSPENDER_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Suspender Dress"),
    JUMPSUIT(GarmentCategory.DRESSES_JUMPSUITS, "Jumpsuit"),
    PARTY_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Party Dress"),
    MINI_DRESS(GarmentCategory.DRESSES_JUMPSUITS, "Mini Dress"),

    // Pants
    JEANS(GarmentCategory.PANTS, "Jeans"),
    TROUSERS(GarmentCategory.PANTS, "Trousers"),
    DRESS_PANTS(GarmentCategory.PANTS, "Dress Pants"),
    TRACK_PANTS(GarmentCategory.PANTS, "Track Pants"),
    LEGGINGS(GarmentCategory.PANTS, "Leggings"),
    SHORTS(GarmentCategory.PANTS, "Shorts"),

    // Skirts
    MINI_SKIRTS(GarmentCategory.SKIRTS, "Mini Skirts"),
    MIDI_SKIRTS(GarmentCategory.SKIRTS, "Midi Skirts"),
    MAXI_SKIRTS(GarmentCategory.SKIRTS, "Maxi Skirts"),

    // Outerwear
    COATS(GarmentCategory.OUTERWEAR, "Coats"),
    TRENCH_COATS(GarmentCategory.OUTERWEAR, "Trench Coats"),
    FUR_COATS(GarmentCategory.OUTERWEAR, "Fur Coats"),
    SHEARLING_COATS(GarmentCategory.OUTERWEAR, "Shearling Coats"),
    BLAZERS(GarmentCategory.OUTERWEAR, "Blazers"),
    JACKETS(GarmentCategory.OUTERWEAR, "Jackets"),
    BLOUSONS(GarmentCategory.OUTERWEAR, "Blousons"),
    VARSITY_JACKETS(GarmentCategory.OUTERWEAR, "Varsity Jackets"),
    TRUCKER_JACKETS(GarmentCategory.OUTERWEAR, "Trucker Jackets"),
    BIKER_JACKETS(GarmentCategory.OUTERWEAR, "Biker Jackets"),
    CARDIGANS(GarmentCategory.OUTERWEAR, "Cardigans"),
    ZIP_UP_HOODIES(GarmentCategory.OUTERWEAR, "Zip Up Hoodies"),
    FIELD_JACKETS(GarmentCategory.OUTERWEAR, "Field Jackets"),
    SPORT_JACKETS(GarmentCategory.OUTERWEAR, "Sport Jackets"),
    FLEECE_JACKETS(GarmentCategory.OUTERWEAR, "Fleece Jackets"),
    PARKAS(GarmentCategory.OUTERWEAR, "Parkas"),
    DOWN_JACKETS(GarmentCategory.OUTERWEAR, "Down Jackets"),
    PUFFER_JACKETS(GarmentCategory.OUTERWEAR, "Puffer Jackets"),
    VESTS(GarmentCategory.OUTERWEAR, "Vests"),

    // Shoes
    SNEAKERS(GarmentCategory.SHOES, "Sneakers"),
    SLIP_ONS(GarmentCategory.SHOES, "Slip Ons"),
    SPORTS_SHOES(GarmentCategory.SHOES, "Sports Shoes"),
    HIKING_SHOES(GarmentCategory.SHOES, "Hiking Shoes"),
    BOOTS(GarmentCategory.SHOES, "Boots"),
    COMBAT_BOOTS(GarmentCategory.SHOES, "Combat Boots"),
    UGG_BOOTS(GarmentCategory.SHOES, "Ugg Boots"),
    LOAFERS_AND_LOAFER_MULES(GarmentCategory.SHOES, "Loafers & Loafer Mules"),
    BOAT_SHOES_AND_MOCCASINS(GarmentCategory.SHOES, "Boat Shoes & Moccasins"),
    FLAT_SHOES(GarmentCategory.SHOES, "Flat Shoes"),
    HEELS(GarmentCategory.SHOES, "Heels"),
    SANDALS(GarmentCategory.SHOES, "Sandals"),
    SANDAL_HEELS(GarmentCategory.SHOES, "Sandal Heels"),
    SLIDES(GarmentCategory.SHOES, "Slides"),
    MULE_HEELS(GarmentCategory.SHOES, "Mule Heels"),

    // Bags
    TOTE(GarmentCategory.BAGS, "Tote"),
    SHOULDER(GarmentCategory.BAGS, "Shoulder"),
    CROSSBODY(GarmentCategory.BAGS, "Crossbody"),
    WAIST(GarmentCategory.BAGS, "Waist"),
    CANVAS(GarmentCategory.BAGS, "Canvas"),
    BACKPACKS(GarmentCategory.BAGS, "Backpacks"),
    DUFFEL(GarmentCategory.BAGS, "Duffel"),
    CLUTCH(GarmentCategory.BAGS, "Clutch"),
    BRIEFCASES(GarmentCategory.BAGS, "Briefcases"),
    DRAWSTRING(GarmentCategory.BAGS, "Drawstring"),
    SUITCASES(GarmentCategory.BAGS, "Suitcases"),

    // Headwear
    CAP(GarmentCategory.HEADWEAR, "Cap"),
    HATS(GarmentCategory.HEADWEAR, "Hats"),
    BEANIES(GarmentCategory.HEADWEAR, "Beanies"),
    BERETS(GarmentCategory.HEADWEAR, "Berets"),
    FEDORAS(GarmentCategory.HEADWEAR, "Fedoras"),
    SUN_HATS(GarmentCategory.HEADWEAR, "Sun Hats");

    private final GarmentCategory category;
    private final String label;

    GarmentSubtype(GarmentCategory category, String label) {
        this.category = category;
        this.label = label;
    }

    public GarmentCategory category() {
        return category;
    }

    @Override
    public String label() {
        return label;
    }
}