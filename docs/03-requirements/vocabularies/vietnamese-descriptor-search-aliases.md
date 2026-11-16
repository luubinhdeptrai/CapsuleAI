# Vietnamese Descriptor Search Aliases

| Field | Value |
| --- | --- |
| Document | Vietnamese search labels and aliases of the six searchable garment vocabularies |
| Version / Status | 0.1 / Baseline Draft |
| Last updated | 2026-11-17 |
| Owner | Product Owner, with the Business Analyst |
| Capability authority | The Product Owner explicitly authorized Vietnamese descriptor search for US-WAR-003 on 2026-11-17. It refines the approved decision [S4-WAR-01](../../06-scrum/sprints/sprint-04/sprint-plan.md#s4-war-01) (approved 2026-11-16). |
| Wording authority | `SELECTED BY CLAUDE — DELEGATED DEFAULT` under [OPEN-S4-11](../../06-scrum/sprints/sprint-04/implementation-guides/open-questions.md#open-s4-11--searchable-descriptors-and-matching-semantics-cat-q01). The specific Vietnamese words are not individually approved by the Product Owner. A review by the Product Owner or a native-speaker reviewer is recommended before release. |
| Vocabulary authority | The approved codes and English labels come from the [garment-property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md) and [SRS §4.10](../SRS.md#410-controlled-vocabularies-and-descriptor-scales). This document adds search aliases only. It changes no code, label, meaning, applicability rule or stored value. |
| Implementation status | **Not implemented.** The table is proposed in the [US-WAR-003 guide](../../06-scrum/sprints/sprint-04/implementation-guides/US-WAR-003-guide.md), step P3 (`VietnameseDescriptorAliases`). Test T10 (`VietnameseDescriptorAliasesTest`) will compare the code with this document cell by cell. |

## 1. Purpose and Authority

CapsuleAI stores each garment's controlled values as codes, such as `T_SHIRTS` or `WHITE`. The approved labels are English. Users of the Vietnamese mobile app search in Vietnamese, so the wardrobe search ([FR-WAR-007](../SRS.md#36-wardrobe-management), [LOC-006](../SRS.md#7-localization-requirements)) also matches the Vietnamese labels and common aliases listed here.

This is the single canonical source of those aliases:

- The proposed Java class `VietnameseDescriptorAliases` must contain exactly these rows. Test T10 reads this file and fails on any difference.
- The mobile client can use the **Vietnamese search label** column as a starting point for display labels. The client still owns its final display wording ([LOC-002](../SRS.md#7-localization-requirements)).
- **Category and color filters keep using codes.** An alias is never accepted as a filter value and is never stored.

The table covers the six vocabularies that wardrobe search matches: Category, Subtype, Color, Material, Pattern and Style. Brand is free text and is matched literally. Price, Fit, Neckline, Sleeve Length, Length and Silhouette are never searched.

## 2. Matching Semantics

These rules come from the approved [S4-WAR-01](../../06-scrum/sprints/sprint-04/sprint-plan.md#s4-war-01) and the delegated OPEN-S4-11 and OPEN-S4-12. They are summarized here; [SRS §3.6.2](../SRS.md#362-wardrobe-search-and-filter-rules) is the normative statement.

| Rule | Meaning |
| --- | --- |
| One literal phrase | The query is one phrase. It is not split into words, and it has no wildcards. |
| Substring | A controlled value matches when the phrase is contained in its code, its approved English label or any spelling of its Vietnamese aliases. |
| Case-insensitive | `ÁO THUN` matches `áo thun`. Both sides are lower-cased. |
| Accent-sensitive | `ao thun` does **not** match `áo thun`. No accent is removed. |
| NFC | The query is converted to Unicode NFC before matching. Every alias here is stored in NFC. |
| Tone-mark placement | Vietnamese keyboards place the tone mark of an open "oa", "oe" or "uy" syllable in one of two standard positions. Each alias is therefore also matched in its other standard spelling: `họa tiết hoa` and `hoạ tiết hoa` both match Floral. This adds a correctly accented spelling. It is not accent folding. |
| OR across fields | A garment matches when its Category, Subtype, Color, any Material, Pattern, any Style or its Brand matches. |
| Filters | The `category` and `color` filters take exact codes and are combined with the phrase by AND (unchanged). |
| Brand | Brand is compared literally, ignoring case, with no alias and no tone-placement variant. |

Examples:

| Query | Matches |
| --- | --- |
| `áo thun` | Subtypes T-shirts, Long Sleeve T-shirts, Sleeveless T-shirts and Polo Shirts (through `áo thun có cổ`) |
| `áo phông` | T-shirts, Long Sleeve T-shirts and Sleeveless T-shirts |
| `ÁO THUN` | The same as `áo thun` |
| `trắng` | Colors White and Ivory (through `trắng ngà`) |
| `chân váy` | Category Skirts and its three Subtypes |
| `ao thun` | Nothing through the descriptors (accents are significant) |

## 3. Ambiguous Words and OR Semantics

A Vietnamese word can name several controlled values. The table does not pick one of them. A phrase matches **every** value whose alias contains it, and those values are combined with OR, like every other field. The examples below were computed from the table and are covered by tests T2 and T7.

| Query | Values matched | Why |
| --- | --- | --- |
| `áo` | Categories Tops and Outerwear; every Subtype whose alias starts with `áo` | `áo` is the Vietnamese word for a top and is part of `áo khoác` (outerwear) |
| `váy` | Categories Dresses/Jumpsuits and Skirts, with their Subtypes | `váy liền` is a dress; `chân váy` is a skirt |
| `xanh` | Light-Green, Green, Olive, Dark-Olive, Teal, Cyan, Sky-Blue, Blue, Navy | Vietnamese `xanh` covers both blue and green |
| `tím` | Lavender, Purple, Navy | Navy is commonly called `xanh tím than` |
| `hồng` | Pink, Hot-Pink, Magenta | Magenta is commonly called `hồng cánh sen` |
| `đỏ` | Red, Burgundy, Magenta | `đỏ đô` and `đỏ tía` |
| `caro` | Checkerboard, Plaid, Gingham | All three are check patterns |
| `len` | Material Wool and the wool-family Materials; Sweaters, Sweater Vests, Sweater Dress, Cardigans, Beanies | `len` means wool and is part of `áo len`, `mũ len` |
| `da` | Leather, Suede, Animal, Sky-Blue (`xanh da trời`), Biker Jackets, Shearling Coats, and values whose English label or code contains "da", for example Day Dress, Dark-Gray and Sandals | Substring matching also applies to English labels and codes |
| `ren` | Material and Pattern Lace; Style Trendy and Subtype Trench Coats through their English labels | The same English-label effect |

Consequences:

- A short or generic phrase can return many garments. Users narrow the result with a longer phrase or with the category and color filters.
- A phrase that combines two descriptors, such as `áo thun trắng`, matches only a field that contains the whole phrase. Usually nothing does. The phrase is never split into words (approved S4-WAR-01). The client can suggest the color filter instead.
- An alias that was a substring of a very common query was removed when it caused misleading results. For example, `da báo` (leopard print) contains `áo` and is not listed.

## 4. Maintenance Rules

| Change | Required work |
| --- | --- |
| Add, remove or reword an alias | Update this document and `VietnameseDescriptorAliases` together; T10 fails otherwise. In the proposed code, change the matching-policy tag in `WardrobeSearchCriteria` (for example `m2` to `m3`). Search results change, so cursors issued under the old policy must answer `CONDITIONS_CHANGED`. Record the change in OPEN-S4-11. |
| A new approved vocabulary value | First update the [garment-property authority](../../../Initial%20files/Image%20to%20Properties%20Extraction.md) and the enum (Product Owner approval). Then add a row here. The class refuses to start without a label for every value, and T10 fails. |
| Rename a code or English label | Not allowed through this document. Codes and labels belong to the garment-property authority. |
| Make another vocabulary searchable | Product Owner decision. Change S4-WAR-01 or OPEN-S4-11 first. |

Format rules, which T10 relies on:

- One row per value, in the order of the approved vocabulary.
- Code in backticks in the first column.
- Every alias in lower-case Unicode NFC, without leading or trailing spaces.
- Further aliases separated by `; `. Use `—` when there is none.
- Write the old-style tone placement (`họa`, `khóa`). The code adds the other spelling.

## 5. Category

8 values, in the order of the approved vocabulary.

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `TOPS` | Tops | áo | — |
| `DRESSES_JUMPSUITS` | Dresses/Jumpsuits | đầm | váy liền; váy đầm; đồ bay; bộ liền thân |
| `PANTS` | Pants | quần | — |
| `SKIRTS` | Skirts | chân váy | — |
| `OUTERWEAR` | Outerwear | áo khoác | đồ khoác ngoài |
| `SHOES` | Shoes | giày dép | — |
| `BAGS` | Bags | túi xách | — |
| `HEADWEAR` | Headwear | mũ nón | — |

## 6. Subtype

84 values, in the order of the approved vocabulary.

### Subtypes of Tops

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `T_SHIRTS` | T-shirts | áo thun | áo phông |
| `LONG_SLEEVE_T_SHIRTS` | Long Sleeve T-shirts | áo thun dài tay | áo phông dài tay |
| `SLEEVELESS_T_SHIRTS` | Sleeveless T-shirts | áo thun sát nách | áo phông sát nách |
| `POLO_SHIRTS` | Polo Shirts | áo polo | áo thun có cổ |
| `TANKS_AND_CAMIS` | Tanks & Camis | áo ba lỗ | áo hai dây |
| `CROP_TOPS` | Crop Tops | áo croptop | áo lửng |
| `BLOUSES` | Blouses | áo kiểu | áo blouse |
| `SHIRTS` | Shirts | áo sơ mi | — |
| `SWEATSHIRTS` | Sweatshirts | áo nỉ | áo sweatshirt |
| `HOODIES` | Hoodies | áo hoodie | áo nỉ có mũ |
| `SWEATERS` | Sweaters | áo len | — |
| `SWEATER_VESTS` | Sweater Vests | áo gile len | áo len không tay |
| `SPORTS_TOPS` | Sports Tops | áo thể thao | áo tập |
| `BODYSUITS` | Bodysuits | áo bodysuit | áo liền thân |

### Subtypes of Dresses/Jumpsuits

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `DAY_DRESS` | Day Dress | đầm thường ngày | váy thường ngày; đầm dạo phố |
| `T_SHIRT_DRESS` | T-Shirt Dress | đầm thun | váy thun |
| `SHIRT_DRESS` | Shirt Dress | đầm sơ mi | váy sơ mi |
| `SWEATSHIRT_DRESS` | Sweatshirt Dress | đầm nỉ | váy nỉ |
| `SWEATER_DRESS` | Sweater Dress | đầm len | váy len |
| `JACKET_DRESS` | Jacket Dress | đầm vest | váy vest |
| `SUSPENDER_DRESS` | Suspender Dress | đầm yếm | váy yếm |
| `JUMPSUIT` | Jumpsuit | đồ bay | bộ liền thân |
| `PARTY_DRESS` | Party Dress | đầm dự tiệc | váy dự tiệc; đầm dạ hội |
| `MINI_DRESS` | Mini Dress | đầm ngắn | váy ngắn; đầm mini |

### Subtypes of Pants

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `JEANS` | Jeans | quần jean | quần jeans; quần bò |
| `TROUSERS` | Trousers | quần dài | quần vải |
| `DRESS_PANTS` | Dress Pants | quần âu | quần tây |
| `TRACK_PANTS` | Track Pants | quần thể thao | quần jogger |
| `LEGGINGS` | Leggings | quần legging | quần bó |
| `SHORTS` | Shorts | quần short | quần đùi; quần soóc; quần ngắn |

### Subtypes of Skirts

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `MINI_SKIRTS` | Mini Skirts | chân váy ngắn | chân váy mini |
| `MIDI_SKIRTS` | Midi Skirts | chân váy midi | chân váy qua gối |
| `MAXI_SKIRTS` | Maxi Skirts | chân váy maxi | chân váy dài |

### Subtypes of Outerwear

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `COATS` | Coats | áo măng tô | áo khoác dài; áo dạ |
| `TRENCH_COATS` | Trench Coats | áo trench coat | áo măng tô trench |
| `FUR_COATS` | Fur Coats | áo khoác lông thú | áo lông thú |
| `SHEARLING_COATS` | Shearling Coats | áo khoác lông cừu | áo da lông cừu |
| `BLAZERS` | Blazers | áo blazer | áo vest |
| `JACKETS` | Jackets | áo jacket | áo khoác ngắn |
| `BLOUSONS` | Blousons | áo blouson | áo khoác bo gấu |
| `VARSITY_JACKETS` | Varsity Jackets | áo khoác bóng chày | áo varsity |
| `TRUCKER_JACKETS` | Trucker Jackets | áo khoác jean | áo khoác bò |
| `BIKER_JACKETS` | Biker Jackets | áo khoác da | áo khoác biker |
| `CARDIGANS` | Cardigans | áo cardigan | áo khoác len |
| `ZIP_UP_HOODIES` | Zip Up Hoodies | áo hoodie khóa kéo | áo khoác nỉ có mũ |
| `FIELD_JACKETS` | Field Jackets | áo khoác field | áo khoác dã chiến |
| `SPORT_JACKETS` | Sport Jackets | áo khoác thể thao | áo khoác gió |
| `FLEECE_JACKETS` | Fleece Jackets | áo khoác nỉ lông | áo fleece |
| `PARKAS` | Parkas | áo parka | áo khoác parka |
| `DOWN_JACKETS` | Down Jackets | áo phao lông vũ | áo lông vũ |
| `PUFFER_JACKETS` | Puffer Jackets | áo phao | áo khoác phao |
| `VESTS` | Vests | áo gile | áo ghi lê; áo khoác không tay |

### Subtypes of Shoes

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `SNEAKERS` | Sneakers | giày sneaker | giày thể thao thời trang |
| `SLIP_ONS` | Slip Ons | giày slip-on | giày lười vải |
| `SPORTS_SHOES` | Sports Shoes | giày thể thao | giày chạy bộ |
| `HIKING_SHOES` | Hiking Shoes | giày leo núi | giày trekking |
| `BOOTS` | Boots | giày bốt | giày cao cổ |
| `COMBAT_BOOTS` | Combat Boots | giày bốt combat | giày bốt quân đội |
| `UGG_BOOTS` | Ugg Boots | giày bốt lông | — |
| `LOAFERS_AND_LOAFER_MULES` | Loafers & Loafer Mules | giày lười | giày loafer; giày sục |
| `BOAT_SHOES_AND_MOCCASINS` | Boat Shoes & Moccasins | giày mọi | giày moccasin |
| `FLAT_SHOES` | Flat Shoes | giày bệt | giày búp bê |
| `HEELS` | Heels | giày cao gót | — |
| `SANDALS` | Sandals | giày sandal | dép quai hậu; xăng đan |
| `SANDAL_HEELS` | Sandal Heels | giày sandal cao gót | — |
| `SLIDES` | Slides | dép lê | dép quai ngang |
| `MULE_HEELS` | Mule Heels | giày sục cao gót | dép sục cao gót |

### Subtypes of Bags

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `TOTE` | Tote | túi tote | — |
| `SHOULDER` | Shoulder | túi đeo vai | — |
| `CROSSBODY` | Crossbody | túi đeo chéo | — |
| `WAIST` | Waist | túi đeo hông | túi bao tử |
| `CANVAS` | Canvas | túi vải canvas | túi vải bố |
| `BACKPACKS` | Backpacks | ba lô | balo |
| `DUFFEL` | Duffel | túi trống | túi du lịch |
| `CLUTCH` | Clutch | ví cầm tay | túi cầm tay |
| `BRIEFCASES` | Briefcases | cặp tài liệu | cặp công sở |
| `DRAWSTRING` | Drawstring | túi dây rút | — |
| `SUITCASES` | Suitcases | vali | va li |

### Subtypes of Headwear

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `CAP` | Cap | mũ lưỡi trai | nón kết |
| `HATS` | Hats | mũ | nón |
| `BEANIES` | Beanies | mũ len | nón len |
| `BERETS` | Berets | mũ nồi | mũ beret |
| `FEDORAS` | Fedoras | mũ phớt | mũ fedora |
| `SUN_HATS` | Sun Hats | mũ chống nắng | mũ rộng vành; nón rộng vành |

## 7. Color

34 values, in the order of the approved vocabulary.

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `WHITE` | White | trắng | màu trắng |
| `IVORY` | Ivory | trắng ngà | màu ngà |
| `BEIGE` | Beige | be | màu be |
| `LIGHT_GRAY` | Light-Gray | xám nhạt | ghi nhạt |
| `DARK_GRAY` | Dark-Gray | xám đậm | ghi đậm; xám lông chuột |
| `BLACK` | Black | đen | màu đen |
| `LIGHT_YELLOW` | Light-Yellow | vàng nhạt | — |
| `YELLOW` | Yellow | vàng | màu vàng |
| `TURMERIC` | Turmeric | vàng nghệ | màu nghệ |
| `ORANGE` | Orange | cam | màu cam |
| `CORAL` | Coral | san hô | màu san hô |
| `RED` | Red | đỏ | màu đỏ |
| `PINK` | Pink | hồng | màu hồng |
| `HOT_PINK` | Hot-Pink | hồng đậm | hồng neon |
| `LIGHT_GREEN` | Light-Green | xanh lá nhạt | xanh nõn chuối |
| `GREEN` | Green | xanh lá | xanh lá cây |
| `OLIVE` | Olive | xanh ô liu | xanh rêu |
| `DARK_OLIVE` | Dark-Olive | xanh rêu đậm | ô liu đậm |
| `TEAL` | Teal | xanh mòng két | xanh cổ vịt |
| `KHAKI` | Khaki | kaki | màu kaki |
| `CYAN` | Cyan | xanh lơ | xanh ngọc |
| `SKY_BLUE` | Sky-Blue | xanh da trời | xanh trời |
| `BLUE` | Blue | xanh dương | xanh lam |
| `NAVY` | Navy | xanh navy | xanh tím than |
| `LAVENDER` | Lavender | tím oải hương | tím nhạt |
| `PURPLE` | Purple | tím | màu tím |
| `BURGUNDY` | Burgundy | đỏ đô | đỏ rượu vang; đỏ mận |
| `CAMEL` | Camel | nâu lạc đà | màu lạc đà |
| `BROWN` | Brown | nâu | màu nâu |
| `DARK_BROWN` | Dark-Brown | nâu đậm | nâu sẫm |
| `MAGENTA` | Magenta | hồng cánh sen | đỏ tía |
| `GOLD` | Gold | vàng kim | nhũ vàng |
| `SILVER` | Silver | bạc | màu bạc; nhũ bạc |
| `COLORFUL` | Colorful | nhiều màu | sặc sỡ |

## 8. Material

28 values, in the order of the approved vocabulary.

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `COTTON` | Cotton | vải cotton | vải bông |
| `POLYESTER` | Polyester | vải polyester | vải poly |
| `NYLON` | Nylon | vải nylon | ni lông; vải dù |
| `DENIM` | Denim | vải jean | vải bò; vải denim |
| `LEATHER` | Leather | da | da thật |
| `WOOL` | Wool | len | vải len; vải dạ |
| `SPANDEX_ELASTANE` | Spandex (elastane) | vải co giãn | sợi spandex |
| `LINEN` | Linen | vải lanh | vải đũi |
| `ACRYLIC` | Acrylic | vải acrylic | len acrylic |
| `SILK` | Silk | lụa | vải lụa; tơ tằm |
| `RAYON` | Rayon | vải rayon | tơ nhân tạo |
| `VISCOSE` | Viscose | vải viscose | tơ visco |
| `TWEED` | Tweed | vải tweed | dạ tweed |
| `VELVET` | Velvet | nhung | vải nhung |
| `CHIFFON` | Chiffon | voan | vải voan |
| `SUEDE` | Suede | da lộn | da nỉ |
| `CORDUROY` | Corduroy | nhung tăm | vải nhung gân |
| `LACE` | Lace | ren | vải ren |
| `FUR` | Fur | lông thú | vải lông |
| `DOWN` | Down | lông vũ | lông vịt |
| `LYOCELL` | Lyocell | vải lyocell | vải tencel |
| `METAL` | Metal | kim loại | — |
| `CUPRO` | Cupro | vải cupro | lụa cupro |
| `CASHMERE` | Cashmere | len cashmere | — |
| `MOHAIR` | Mohair | len mohair | — |
| `ANGORA` | Angora | len angora | lông thỏ |
| `ALPACA` | Alpaca | len alpaca | — |
| `OTHER_MATERIALS` | Other Materials | chất liệu khác | — |

## 9. Pattern

24 values, in the order of the approved vocabulary.

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `SOLID` | Solid | trơn | màu trơn; một màu |
| `STRIPED` | Striped | kẻ sọc | họa tiết sọc |
| `GRAPHIC` | Graphic | in hình | hình in |
| `DOTTED` | Dotted | chấm bi | — |
| `ANIMAL` | Animal | họa tiết da thú | vằn ngựa; vằn hổ |
| `FLORAL` | Floral | hoa | họa tiết hoa; hoa nhí |
| `TROPICAL` | Tropical | họa tiết nhiệt đới | lá nhiệt đới |
| `PAISLEY` | Paisley | họa tiết paisley | họa tiết giọt nước |
| `ARGYLE` | Argyle | họa tiết quả trám | kẻ quả trám |
| `CAMO` | Camo | rằn ri | họa tiết quân đội |
| `COLOR_BLOCK` | Color-Block | khối màu | phối màu tương phản |
| `REPEATED` | Repeated | họa tiết lặp lại | — |
| `CHECKERBOARD` | Checkerboard | caro bàn cờ | kẻ ô bàn cờ |
| `PLAID` | Plaid | kẻ caro | kẻ ô |
| `GINGHAM` | Gingham | caro vichy | kẻ caro nhỏ |
| `HOUNDSTOOTH` | Houndstooth | họa tiết răng chó | — |
| `HERRINGBONE` | Herringbone | xương cá | họa tiết xương cá |
| `CHEVRON` | Chevron | họa tiết chữ v | zích zắc |
| `TWEED` | Tweed | họa tiết tweed | vân tweed |
| `ABSTRACT` | Abstract | trừu tượng | họa tiết trừu tượng |
| `TIE_DYE` | Tie Dye | loang màu | nhuộm loang |
| `GEOMETRIC` | Geometric | hình học | họa tiết hình học |
| `LACE` | Lace | ren | họa tiết ren |
| `OTHER_PATTERNS` | Other Patterns | họa tiết khác | — |

## 10. Style

17 values, in the order of the approved vocabulary.

| Code | Approved English label | Vietnamese search label | Further Vietnamese aliases |
| --- | --- | --- | --- |
| `COMFORTABLE` | Comfortable | thoải mái | dễ chịu |
| `CASUAL` | Casual | thường ngày | giản dị |
| `BUSINESS_CASUAL` | Business Casual | công sở thanh lịch | đồ công sở |
| `FORMAL` | Formal | trang trọng | lịch sự |
| `MODERN` | Modern | hiện đại | — |
| `CLASSIC` | Classic | cổ điển | — |
| `MINIMALIST` | Minimalist | tối giản | — |
| `BOHEMIAN` | Bohemian | du mục | boho |
| `LUXURY` | Luxury | sang trọng | cao cấp |
| `SPORTY` | Sporty | thể thao | năng động |
| `ATHLEISURE` | Athleisure | thể thao thường ngày | đồ tập dạo phố |
| `TRENDY` | Trendy | hợp mốt | thời thượng; theo xu hướng |
| `KIDCORE` | Kidcore | phong cách trẻ thơ | — |
| `ARTISTIC` | Artistic | nghệ thuật | phong cách nghệ sĩ |
| `DRESSY` | Dressy | điệu đà | chỉn chu |
| `FEMININE` | Feminine | nữ tính | — |
| `STREET` | Street | đường phố | — |

## 11. Traceability

| Item | Reference |
| --- | --- |
| Approved decision | [S4-WAR-01](../../06-scrum/sprints/sprint-04/sprint-plan.md#s4-war-01): searchable descriptors and Vietnamese matching semantics (approved 2026-11-16) |
| Delegated detail and its 2026-11-17 refinement | [OPEN-S4-11](../../06-scrum/sprints/sprint-04/implementation-guides/open-questions.md#open-s4-11--searchable-descriptors-and-matching-semantics-cat-q01) |
| Requirements | [FR-WAR-007](../SRS.md#36-wardrobe-management), [SRS §3.6.2](../SRS.md#362-wardrobe-search-and-filter-rules), [LOC-002](../SRS.md#7-localization-requirements), [LOC-004](../SRS.md#7-localization-requirements), [LOC-006](../SRS.md#7-localization-requirements), [BRULE-GAR-004](../business-rules.md#brule-gar-004--controlled-classification-and-context-vocabularies) |
| Product | [PRD FEAT-WAR-003](../../02-product/PRD.md#143-feat-war-003--wardrobe-search-and-filtering), [PBI-015](../../06-scrum/product-backlog.md#pbi-015--find-garments-through-wardrobe-search-and-filters) |
| Story | [US-WAR-003](../../06-scrum/stories/wardrobe/US-WAR-003-find-garments-with-search-and-filters.md) AC-01, AC-03 |
| Proposed implementation and tests | [US-WAR-003 guide](../../06-scrum/sprints/sprint-04/implementation-guides/US-WAR-003-guide.md): P3 `VietnameseDescriptorAliases`, P4 `DescriptorMatcher`, P1 matching policy `m2`; tests T2, T3, T4, T7, T8, T10 |
