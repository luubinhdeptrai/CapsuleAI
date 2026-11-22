package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.ControlledValue;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.domain.GarmentMaterial;
import com.capsuleai.wardrobe.domain.GarmentPattern;
import com.capsuleai.wardrobe.domain.GarmentStyle;
import com.capsuleai.wardrobe.domain.GarmentSubtype;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * US-WAR-003 — the Vietnamese search labels and common aliases of the six searchable garment vocabularies
 * (S4-WAR-01; OPEN-S4-11 as refined on 2026-11-17 under the Product Owner's explicit authorization).
 *
 * <p>The first alias of a value is its Vietnamese search label; the others are common alternatives. The
 * canonical table is {@code docs/03-requirements/vocabularies/vietnamese-descriptor-search-aliases.md};
 * {@code VietnameseDescriptorAliasesTest} keeps this class identical to it. Aliases are search vocabulary
 * only: codes, English labels and stored values are unchanged, and the category and color filters still
 * accept codes only.
 *
 * <p>Every alias is lower-case NFC text, so a lower-cased NFC query is compared without further folding and
 * accents stay significant. An alias is also searchable in the other standard Vietnamese tone-mark placement
 * of its open "oa", "oe" and "uy" syllables ("họa" and "hoạ", "khóa" and "khoá"), because keyboards
 * produce either spelling. That adds a correctly accented spelling; it never removes an accent.
 */
public final class VietnameseDescriptorAliases {

    /** The two standard tone-mark placements of an open oa, oe or uy syllable, mapped in both directions. */
    private static final Map<String, String> OTHER_TONE_PLACEMENT = tonePlacementPairs();

    private static final Map<ControlledValue, List<String>> ALIASES = allAliases();

    private static final Map<ControlledValue, List<String>> SEARCH_SPELLINGS = searchSpellings(ALIASES);

    private VietnameseDescriptorAliases() {
    }

    /**
     * The documented aliases of a value, its Vietnamese search label first.
     *
     * @throws IllegalArgumentException for a value of a vocabulary that is not searched (for example Fit)
     */
    public static List<String> aliasesOf(ControlledValue value) {
        List<String> aliases = ALIASES.get(value);
        if (aliases == null) {
            throw new IllegalArgumentException("Not a value of a searchable vocabulary: " + value.name());
        }
        return aliases;
    }

    /** Every spelling the search compares for a value: each alias and, where different, its other tone placement. */
    public static List<String> searchSpellingsOf(ControlledValue value) {
        aliasesOf(value);
        return SEARCH_SPELLINGS.get(value);
    }

    /**
     * The alias with each open oa, oe or uy syllable written in its other standard tone placement, for example
     * "họa tiết hoa" and "hoạ tiết hoa". A syllable beginning with "qu" keeps its spelling, because its "u"
     * belongs to the consonant ("quý").
     */
    static String withOtherTonePlacement(String alias) {
        String[] syllables = alias.split(" ", -1);
        for (int index = 0; index < syllables.length; index++) {
            syllables[index] = withOtherTonePlacementOfSyllable(syllables[index]);
        }
        return String.join(" ", syllables);
    }

    private static String withOtherTonePlacementOfSyllable(String syllable) {
        if (syllable.length() < 2 || syllable.startsWith("qu")) {
            return syllable;
        }
        String ending = syllable.substring(syllable.length() - 2);
        String otherEnding = OTHER_TONE_PLACEMENT.get(ending);
        return otherEnding == null ? syllable : syllable.substring(0, syllable.length() - 2) + otherEnding;
    }

    private static Map<String, String> tonePlacementPairs() {
        String[][] oldAndNewPlacements = {
                {"òa", "oà"}, {"óa", "oá"}, {"ỏa", "oả"}, {"õa", "oã"}, {"ọa", "oạ"},
                {"òe", "oè"}, {"óe", "oé"}, {"ỏe", "oẻ"}, {"õe", "oẽ"}, {"ọe", "oẹ"},
                {"ùy", "uỳ"}, {"úy", "uý"}, {"ủy", "uỷ"}, {"ũy", "uỹ"}, {"ụy", "uỵ"}};
        Map<String, String> pairs = new HashMap<>();
        for (String[] pair : oldAndNewPlacements) {
            pairs.put(pair[0], pair[1]);
            pairs.put(pair[1], pair[0]);
        }
        return Map.copyOf(pairs);
    }

    private static Map<ControlledValue, List<String>> allAliases() {
        Map<ControlledValue, List<String>> aliases = new HashMap<>();
        aliases.putAll(categoryAliases());
        aliases.putAll(subtypeAliases());
        aliases.putAll(colorAliases());
        aliases.putAll(materialAliases());
        aliases.putAll(patternAliases());
        aliases.putAll(styleAliases());
        return Map.copyOf(aliases);
    }

    private static Map<ControlledValue, List<String>> searchSpellings(Map<ControlledValue, List<String>> aliases) {
        Map<ControlledValue, List<String>> spellings = new HashMap<>();
        aliases.forEach((value, valueAliases) -> {
            Set<String> valueSpellings = new LinkedHashSet<>();
            for (String alias : valueAliases) {
                valueSpellings.add(alias);
                valueSpellings.add(withOtherTonePlacement(alias));
            }
            spellings.put(value, List.copyOf(valueSpellings));
        });
        return Map.copyOf(spellings);
    }

    /** One value's aliases; fails fast on text that is not lower-case NFC without outer spaces, or on a repeat. */
    private static <E extends Enum<E> & ControlledValue> Map.Entry<E, List<String>> vietnamese(
            E value, String searchLabel, String... furtherAliases) {
        List<String> aliases = new ArrayList<>();
        aliases.add(searchLabel);
        aliases.addAll(List.of(furtherAliases));
        for (String alias : aliases) {
            if (alias.isBlank() || !alias.equals(alias.strip()) || !alias.equals(alias.toLowerCase(Locale.ROOT))
                    || !Normalizer.isNormalized(alias, Normalizer.Form.NFC)) {
                throw new IllegalStateException("An alias must be lower-case NFC text without outer spaces: " + alias);
            }
        }
        if (new LinkedHashSet<>(aliases).size() != aliases.size()) {
            throw new IllegalStateException("An alias is repeated for " + value.name());
        }
        return Map.entry(value, List.copyOf(aliases));
    }

    /** The table of one vocabulary; fails fast when a value is missing or listed twice. */
    private static <E extends Enum<E> & ControlledValue> Map<E, List<String>> complete(
            Class<E> vocabulary, List<Map.Entry<E, List<String>>> entries) {
        Map<E, List<String>> table = new EnumMap<>(vocabulary);
        for (Map.Entry<E, List<String>> entry : entries) {
            if (table.put(entry.getKey(), entry.getValue()) != null) {
                throw new IllegalStateException("Aliases are listed twice for " + entry.getKey().name());
            }
        }
        for (E value : vocabulary.getEnumConstants()) {
            if (!table.containsKey(value)) {
                throw new IllegalStateException(
                        "No Vietnamese search label for " + vocabulary.getSimpleName() + "." + value.name());
            }
        }
        return table;
    }

    /** Category: every GarmentCategory value, in vocabulary order. */
    private static Map<GarmentCategory, List<String>> categoryAliases() {
        return complete(GarmentCategory.class, List.of(
                vietnamese(GarmentCategory.TOPS, "áo"),
                vietnamese(GarmentCategory.DRESSES_JUMPSUITS, "đầm", "váy liền", "váy đầm", "đồ bay", "bộ liền thân"),
                vietnamese(GarmentCategory.PANTS, "quần"),
                vietnamese(GarmentCategory.SKIRTS, "chân váy"),
                vietnamese(GarmentCategory.OUTERWEAR, "áo khoác", "đồ khoác ngoài"),
                vietnamese(GarmentCategory.SHOES, "giày dép"),
                vietnamese(GarmentCategory.BAGS, "túi xách"),
                vietnamese(GarmentCategory.HEADWEAR, "mũ nón")));
    }

    /** Subtype: every GarmentSubtype value, in vocabulary order. */
    private static Map<GarmentSubtype, List<String>> subtypeAliases() {
        return complete(GarmentSubtype.class, List.of(
                vietnamese(GarmentSubtype.T_SHIRTS, "áo thun", "áo phông"),
                vietnamese(GarmentSubtype.LONG_SLEEVE_T_SHIRTS, "áo thun dài tay", "áo phông dài tay"),
                vietnamese(GarmentSubtype.SLEEVELESS_T_SHIRTS, "áo thun sát nách", "áo phông sát nách"),
                vietnamese(GarmentSubtype.POLO_SHIRTS, "áo polo", "áo thun có cổ"),
                vietnamese(GarmentSubtype.TANKS_AND_CAMIS, "áo ba lỗ", "áo hai dây"),
                vietnamese(GarmentSubtype.CROP_TOPS, "áo croptop", "áo lửng"),
                vietnamese(GarmentSubtype.BLOUSES, "áo kiểu", "áo blouse"),
                vietnamese(GarmentSubtype.SHIRTS, "áo sơ mi"),
                vietnamese(GarmentSubtype.SWEATSHIRTS, "áo nỉ", "áo sweatshirt"),
                vietnamese(GarmentSubtype.HOODIES, "áo hoodie", "áo nỉ có mũ"),
                vietnamese(GarmentSubtype.SWEATERS, "áo len"),
                vietnamese(GarmentSubtype.SWEATER_VESTS, "áo gile len", "áo len không tay"),
                vietnamese(GarmentSubtype.SPORTS_TOPS, "áo thể thao", "áo tập"),
                vietnamese(GarmentSubtype.BODYSUITS, "áo bodysuit", "áo liền thân"),
                vietnamese(GarmentSubtype.DAY_DRESS, "đầm thường ngày", "váy thường ngày", "đầm dạo phố"),
                vietnamese(GarmentSubtype.T_SHIRT_DRESS, "đầm thun", "váy thun"),
                vietnamese(GarmentSubtype.SHIRT_DRESS, "đầm sơ mi", "váy sơ mi"),
                vietnamese(GarmentSubtype.SWEATSHIRT_DRESS, "đầm nỉ", "váy nỉ"),
                vietnamese(GarmentSubtype.SWEATER_DRESS, "đầm len", "váy len"),
                vietnamese(GarmentSubtype.JACKET_DRESS, "đầm vest", "váy vest"),
                vietnamese(GarmentSubtype.SUSPENDER_DRESS, "đầm yếm", "váy yếm"),
                vietnamese(GarmentSubtype.JUMPSUIT, "đồ bay", "bộ liền thân"),
                vietnamese(GarmentSubtype.PARTY_DRESS, "đầm dự tiệc", "váy dự tiệc", "đầm dạ hội"),
                vietnamese(GarmentSubtype.MINI_DRESS, "đầm ngắn", "váy ngắn", "đầm mini"),
                vietnamese(GarmentSubtype.JEANS, "quần jean", "quần jeans", "quần bò"),
                vietnamese(GarmentSubtype.TROUSERS, "quần dài", "quần vải"),
                vietnamese(GarmentSubtype.DRESS_PANTS, "quần âu", "quần tây"),
                vietnamese(GarmentSubtype.TRACK_PANTS, "quần thể thao", "quần jogger"),
                vietnamese(GarmentSubtype.LEGGINGS, "quần legging", "quần bó"),
                vietnamese(GarmentSubtype.SHORTS, "quần short", "quần đùi", "quần soóc", "quần ngắn"),
                vietnamese(GarmentSubtype.MINI_SKIRTS, "chân váy ngắn", "chân váy mini"),
                vietnamese(GarmentSubtype.MIDI_SKIRTS, "chân váy midi", "chân váy qua gối"),
                vietnamese(GarmentSubtype.MAXI_SKIRTS, "chân váy maxi", "chân váy dài"),
                vietnamese(GarmentSubtype.COATS, "áo măng tô", "áo khoác dài", "áo dạ"),
                vietnamese(GarmentSubtype.TRENCH_COATS, "áo trench coat", "áo măng tô trench"),
                vietnamese(GarmentSubtype.FUR_COATS, "áo khoác lông thú", "áo lông thú"),
                vietnamese(GarmentSubtype.SHEARLING_COATS, "áo khoác lông cừu", "áo da lông cừu"),
                vietnamese(GarmentSubtype.BLAZERS, "áo blazer", "áo vest"),
                vietnamese(GarmentSubtype.JACKETS, "áo jacket", "áo khoác ngắn"),
                vietnamese(GarmentSubtype.BLOUSONS, "áo blouson", "áo khoác bo gấu"),
                vietnamese(GarmentSubtype.VARSITY_JACKETS, "áo khoác bóng chày", "áo varsity"),
                vietnamese(GarmentSubtype.TRUCKER_JACKETS, "áo khoác jean", "áo khoác bò"),
                vietnamese(GarmentSubtype.BIKER_JACKETS, "áo khoác da", "áo khoác biker"),
                vietnamese(GarmentSubtype.CARDIGANS, "áo cardigan", "áo khoác len"),
                vietnamese(GarmentSubtype.ZIP_UP_HOODIES, "áo hoodie khóa kéo", "áo khoác nỉ có mũ"),
                vietnamese(GarmentSubtype.FIELD_JACKETS, "áo khoác field", "áo khoác dã chiến"),
                vietnamese(GarmentSubtype.SPORT_JACKETS, "áo khoác thể thao", "áo khoác gió"),
                vietnamese(GarmentSubtype.FLEECE_JACKETS, "áo khoác nỉ lông", "áo fleece"),
                vietnamese(GarmentSubtype.PARKAS, "áo parka", "áo khoác parka"),
                vietnamese(GarmentSubtype.DOWN_JACKETS, "áo phao lông vũ", "áo lông vũ"),
                vietnamese(GarmentSubtype.PUFFER_JACKETS, "áo phao", "áo khoác phao"),
                vietnamese(GarmentSubtype.VESTS, "áo gile", "áo ghi lê", "áo khoác không tay"),
                vietnamese(GarmentSubtype.SNEAKERS, "giày sneaker", "giày thể thao thời trang"),
                vietnamese(GarmentSubtype.SLIP_ONS, "giày slip-on", "giày lười vải"),
                vietnamese(GarmentSubtype.SPORTS_SHOES, "giày thể thao", "giày chạy bộ"),
                vietnamese(GarmentSubtype.HIKING_SHOES, "giày leo núi", "giày trekking"),
                vietnamese(GarmentSubtype.BOOTS, "giày bốt", "giày cao cổ"),
                vietnamese(GarmentSubtype.COMBAT_BOOTS, "giày bốt combat", "giày bốt quân đội"),
                vietnamese(GarmentSubtype.UGG_BOOTS, "giày bốt lông"),
                vietnamese(GarmentSubtype.LOAFERS_AND_LOAFER_MULES, "giày lười", "giày loafer", "giày sục"),
                vietnamese(GarmentSubtype.BOAT_SHOES_AND_MOCCASINS, "giày mọi", "giày moccasin"),
                vietnamese(GarmentSubtype.FLAT_SHOES, "giày bệt", "giày búp bê"),
                vietnamese(GarmentSubtype.HEELS, "giày cao gót"),
                vietnamese(GarmentSubtype.SANDALS, "giày sandal", "dép quai hậu", "xăng đan"),
                vietnamese(GarmentSubtype.SANDAL_HEELS, "giày sandal cao gót"),
                vietnamese(GarmentSubtype.SLIDES, "dép lê", "dép quai ngang"),
                vietnamese(GarmentSubtype.MULE_HEELS, "giày sục cao gót", "dép sục cao gót"),
                vietnamese(GarmentSubtype.TOTE, "túi tote"),
                vietnamese(GarmentSubtype.SHOULDER, "túi đeo vai"),
                vietnamese(GarmentSubtype.CROSSBODY, "túi đeo chéo"),
                vietnamese(GarmentSubtype.WAIST, "túi đeo hông", "túi bao tử"),
                vietnamese(GarmentSubtype.CANVAS, "túi vải canvas", "túi vải bố"),
                vietnamese(GarmentSubtype.BACKPACKS, "ba lô", "balo"),
                vietnamese(GarmentSubtype.DUFFEL, "túi trống", "túi du lịch"),
                vietnamese(GarmentSubtype.CLUTCH, "ví cầm tay", "túi cầm tay"),
                vietnamese(GarmentSubtype.BRIEFCASES, "cặp tài liệu", "cặp công sở"),
                vietnamese(GarmentSubtype.DRAWSTRING, "túi dây rút"),
                vietnamese(GarmentSubtype.SUITCASES, "vali", "va li"),
                vietnamese(GarmentSubtype.CAP, "mũ lưỡi trai", "nón kết"),
                vietnamese(GarmentSubtype.HATS, "mũ", "nón"),
                vietnamese(GarmentSubtype.BEANIES, "mũ len", "nón len"),
                vietnamese(GarmentSubtype.BERETS, "mũ nồi", "mũ beret"),
                vietnamese(GarmentSubtype.FEDORAS, "mũ phớt", "mũ fedora"),
                vietnamese(GarmentSubtype.SUN_HATS, "mũ chống nắng", "mũ rộng vành", "nón rộng vành")));
    }

    /** Color: every GarmentColor value, in vocabulary order. */
    private static Map<GarmentColor, List<String>> colorAliases() {
        return complete(GarmentColor.class, List.of(
                vietnamese(GarmentColor.WHITE, "trắng", "màu trắng"),
                vietnamese(GarmentColor.IVORY, "trắng ngà", "màu ngà"),
                vietnamese(GarmentColor.BEIGE, "be", "màu be"),
                vietnamese(GarmentColor.LIGHT_GRAY, "xám nhạt", "ghi nhạt"),
                vietnamese(GarmentColor.DARK_GRAY, "xám đậm", "ghi đậm", "xám lông chuột"),
                vietnamese(GarmentColor.BLACK, "đen", "màu đen"),
                vietnamese(GarmentColor.LIGHT_YELLOW, "vàng nhạt"),
                vietnamese(GarmentColor.YELLOW, "vàng", "màu vàng"),
                vietnamese(GarmentColor.TURMERIC, "vàng nghệ", "màu nghệ"),
                vietnamese(GarmentColor.ORANGE, "cam", "màu cam"),
                vietnamese(GarmentColor.CORAL, "san hô", "màu san hô"),
                vietnamese(GarmentColor.RED, "đỏ", "màu đỏ"),
                vietnamese(GarmentColor.PINK, "hồng", "màu hồng"),
                vietnamese(GarmentColor.HOT_PINK, "hồng đậm", "hồng neon"),
                vietnamese(GarmentColor.LIGHT_GREEN, "xanh lá nhạt", "xanh nõn chuối"),
                vietnamese(GarmentColor.GREEN, "xanh lá", "xanh lá cây"),
                vietnamese(GarmentColor.OLIVE, "xanh ô liu", "xanh rêu"),
                vietnamese(GarmentColor.DARK_OLIVE, "xanh rêu đậm", "ô liu đậm"),
                vietnamese(GarmentColor.TEAL, "xanh mòng két", "xanh cổ vịt"),
                vietnamese(GarmentColor.KHAKI, "kaki", "màu kaki"),
                vietnamese(GarmentColor.CYAN, "xanh lơ", "xanh ngọc"),
                vietnamese(GarmentColor.SKY_BLUE, "xanh da trời", "xanh trời"),
                vietnamese(GarmentColor.BLUE, "xanh dương", "xanh lam"),
                vietnamese(GarmentColor.NAVY, "xanh navy", "xanh tím than"),
                vietnamese(GarmentColor.LAVENDER, "tím oải hương", "tím nhạt"),
                vietnamese(GarmentColor.PURPLE, "tím", "màu tím"),
                vietnamese(GarmentColor.BURGUNDY, "đỏ đô", "đỏ rượu vang", "đỏ mận"),
                vietnamese(GarmentColor.CAMEL, "nâu lạc đà", "màu lạc đà"),
                vietnamese(GarmentColor.BROWN, "nâu", "màu nâu"),
                vietnamese(GarmentColor.DARK_BROWN, "nâu đậm", "nâu sẫm"),
                vietnamese(GarmentColor.MAGENTA, "hồng cánh sen", "đỏ tía"),
                vietnamese(GarmentColor.GOLD, "vàng kim", "nhũ vàng"),
                vietnamese(GarmentColor.SILVER, "bạc", "màu bạc", "nhũ bạc"),
                vietnamese(GarmentColor.COLORFUL, "nhiều màu", "sặc sỡ")));
    }

    /** Material: every GarmentMaterial value, in vocabulary order. */
    private static Map<GarmentMaterial, List<String>> materialAliases() {
        return complete(GarmentMaterial.class, List.of(
                vietnamese(GarmentMaterial.COTTON, "vải cotton", "vải bông"),
                vietnamese(GarmentMaterial.POLYESTER, "vải polyester", "vải poly"),
                vietnamese(GarmentMaterial.NYLON, "vải nylon", "ni lông", "vải dù"),
                vietnamese(GarmentMaterial.DENIM, "vải jean", "vải bò", "vải denim"),
                vietnamese(GarmentMaterial.LEATHER, "da", "da thật"),
                vietnamese(GarmentMaterial.WOOL, "len", "vải len", "vải dạ"),
                vietnamese(GarmentMaterial.SPANDEX_ELASTANE, "vải co giãn", "sợi spandex"),
                vietnamese(GarmentMaterial.LINEN, "vải lanh", "vải đũi"),
                vietnamese(GarmentMaterial.ACRYLIC, "vải acrylic", "len acrylic"),
                vietnamese(GarmentMaterial.SILK, "lụa", "vải lụa", "tơ tằm"),
                vietnamese(GarmentMaterial.RAYON, "vải rayon", "tơ nhân tạo"),
                vietnamese(GarmentMaterial.VISCOSE, "vải viscose", "tơ visco"),
                vietnamese(GarmentMaterial.TWEED, "vải tweed", "dạ tweed"),
                vietnamese(GarmentMaterial.VELVET, "nhung", "vải nhung"),
                vietnamese(GarmentMaterial.CHIFFON, "voan", "vải voan"),
                vietnamese(GarmentMaterial.SUEDE, "da lộn", "da nỉ"),
                vietnamese(GarmentMaterial.CORDUROY, "nhung tăm", "vải nhung gân"),
                vietnamese(GarmentMaterial.LACE, "ren", "vải ren"),
                vietnamese(GarmentMaterial.FUR, "lông thú", "vải lông"),
                vietnamese(GarmentMaterial.DOWN, "lông vũ", "lông vịt"),
                vietnamese(GarmentMaterial.LYOCELL, "vải lyocell", "vải tencel"),
                vietnamese(GarmentMaterial.METAL, "kim loại"),
                vietnamese(GarmentMaterial.CUPRO, "vải cupro", "lụa cupro"),
                vietnamese(GarmentMaterial.CASHMERE, "len cashmere"),
                vietnamese(GarmentMaterial.MOHAIR, "len mohair"),
                vietnamese(GarmentMaterial.ANGORA, "len angora", "lông thỏ"),
                vietnamese(GarmentMaterial.ALPACA, "len alpaca"),
                vietnamese(GarmentMaterial.OTHER_MATERIALS, "chất liệu khác")));
    }

    /** Pattern: every GarmentPattern value, in vocabulary order. */
    private static Map<GarmentPattern, List<String>> patternAliases() {
        return complete(GarmentPattern.class, List.of(
                vietnamese(GarmentPattern.SOLID, "trơn", "màu trơn", "một màu"),
                vietnamese(GarmentPattern.STRIPED, "kẻ sọc", "họa tiết sọc"),
                vietnamese(GarmentPattern.GRAPHIC, "in hình", "hình in"),
                vietnamese(GarmentPattern.DOTTED, "chấm bi"),
                vietnamese(GarmentPattern.ANIMAL, "họa tiết da thú", "vằn ngựa", "vằn hổ"),
                vietnamese(GarmentPattern.FLORAL, "hoa", "họa tiết hoa", "hoa nhí"),
                vietnamese(GarmentPattern.TROPICAL, "họa tiết nhiệt đới", "lá nhiệt đới"),
                vietnamese(GarmentPattern.PAISLEY, "họa tiết paisley", "họa tiết giọt nước"),
                vietnamese(GarmentPattern.ARGYLE, "họa tiết quả trám", "kẻ quả trám"),
                vietnamese(GarmentPattern.CAMO, "rằn ri", "họa tiết quân đội"),
                vietnamese(GarmentPattern.COLOR_BLOCK, "khối màu", "phối màu tương phản"),
                vietnamese(GarmentPattern.REPEATED, "họa tiết lặp lại"),
                vietnamese(GarmentPattern.CHECKERBOARD, "caro bàn cờ", "kẻ ô bàn cờ"),
                vietnamese(GarmentPattern.PLAID, "kẻ caro", "kẻ ô"),
                vietnamese(GarmentPattern.GINGHAM, "caro vichy", "kẻ caro nhỏ"),
                vietnamese(GarmentPattern.HOUNDSTOOTH, "họa tiết răng chó"),
                vietnamese(GarmentPattern.HERRINGBONE, "xương cá", "họa tiết xương cá"),
                vietnamese(GarmentPattern.CHEVRON, "họa tiết chữ v", "zích zắc"),
                vietnamese(GarmentPattern.TWEED, "họa tiết tweed", "vân tweed"),
                vietnamese(GarmentPattern.ABSTRACT, "trừu tượng", "họa tiết trừu tượng"),
                vietnamese(GarmentPattern.TIE_DYE, "loang màu", "nhuộm loang"),
                vietnamese(GarmentPattern.GEOMETRIC, "hình học", "họa tiết hình học"),
                vietnamese(GarmentPattern.LACE, "ren", "họa tiết ren"),
                vietnamese(GarmentPattern.OTHER_PATTERNS, "họa tiết khác")));
    }

    /** Style: every GarmentStyle value, in vocabulary order. */
    private static Map<GarmentStyle, List<String>> styleAliases() {
        return complete(GarmentStyle.class, List.of(
                vietnamese(GarmentStyle.COMFORTABLE, "thoải mái", "dễ chịu"),
                vietnamese(GarmentStyle.CASUAL, "thường ngày", "giản dị"),
                vietnamese(GarmentStyle.BUSINESS_CASUAL, "công sở thanh lịch", "đồ công sở"),
                vietnamese(GarmentStyle.FORMAL, "trang trọng", "lịch sự"),
                vietnamese(GarmentStyle.MODERN, "hiện đại"),
                vietnamese(GarmentStyle.CLASSIC, "cổ điển"),
                vietnamese(GarmentStyle.MINIMALIST, "tối giản"),
                vietnamese(GarmentStyle.BOHEMIAN, "du mục", "boho"),
                vietnamese(GarmentStyle.LUXURY, "sang trọng", "cao cấp"),
                vietnamese(GarmentStyle.SPORTY, "thể thao", "năng động"),
                vietnamese(GarmentStyle.ATHLEISURE, "thể thao thường ngày", "đồ tập dạo phố"),
                vietnamese(GarmentStyle.TRENDY, "hợp mốt", "thời thượng", "theo xu hướng"),
                vietnamese(GarmentStyle.KIDCORE, "phong cách trẻ thơ"),
                vietnamese(GarmentStyle.ARTISTIC, "nghệ thuật", "phong cách nghệ sĩ"),
                vietnamese(GarmentStyle.DRESSY, "điệu đà", "chỉn chu"),
                vietnamese(GarmentStyle.FEMININE, "nữ tính"),
                vietnamese(GarmentStyle.STREET, "đường phố")));
    }
}