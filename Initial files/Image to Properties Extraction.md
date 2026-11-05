# CapsuleAI — Approved Garment Properties

## 1. Authority and Scope

**Status: Product Owner-approved property model, 2026-11-05.** This existing file is the source of truth for the current garment properties, category applicability and explicitly listed vocabulary labels. The Product Owner approved all lists below; their previous research-candidate status is superseded. BRD/PRD/SRS, Business Rules, stories, plans and future contracts must follow this catalogue. This approval changes requirements; it does not claim implemented Wardrobe functionality or document-wide approval of downstream drafts.

The current fashion-property model contains exactly the following 13 properties. Technical metadata and optional media are separate (Section 5). Category and Color require explicit user confirmation when the garment is saved; all other applicable properties are optional. All 13 applicable properties belong to the selected Sprint 2 creation/inspection scope.

## 2. Property Catalogue and Applicability

| # | Property | Applicability | Required for Saving | Meaning / Value Authority |
|---|---|---|---|---|
| 1 | Category | All categories | Yes | One of eight category codes. |
| 2 | Subtype | All categories | No | Optional category-valid selection from the complete approved list. |
| 3 | Color | All categories | Yes | Exactly one dominant Color; Colorful is supported. |
| 4 | Material | All categories | No | Zero, one or multiple approved materials; no semantic duplicates or invented percentages. |
| 5 | Pattern | All categories | No | One optional approved Pattern. |
| 6 | Style | All categories | No | Optional named Style selection; Etc is explanatory, never a value. |
| 7 | Fit | Tops | No | Optional approved Fit. |
| 8 | Neckline | Tops | No | Optional approved Neckline. |
| 9 | Sleeve Length | Tops | No | Optional approved Sleeve Length. |
| 10 | Length | Tops and Pants | No | Category-specific approved Length values. |
| 11 | Silhouette | Pants | No | Optional approved Silhouette. |
| 12 | Brand | All categories | No | Optional user-entered text; no enum/catalogue. |
| 13 | Price | All categories | No | Optional amount originally paid: decimal amount and explicit ISO 4217 currency. |

A supplied controlled value must match its approved list and category. An absent optional value is valid and does not mean an invented UNKNOWN/OTHER choice. Preserve explicitly listed actual fallback values only where defined. Do not silently substitute an invalid subtype or discard an inapplicable supplied field: identify it for correction or clearing. Subtype names that overlap another dimension do not automatically derive that dimension. MINI_DRESS and PARTY_DRESS remain subtypes; JACKET_DRESS stays distinct from OUTERWEAR and JUMPSUIT stays in the combined category.

## 3. Approved Controlled Vocabularies

Labels and spelling below are authoritative product values. Category codes and the ten Dresses/Jumpsuits codes are already approved. Do not invent additional subtype identifiers or fallback values. Technical serialization/storage representations for other labels belong to selected-story JIT design and must preserve every approved label and its meaning.

### 3.1 Category and Subtype

| Category Display Name | Category Identifier | Approved Subtypes |
|---|---|---|
| Tops | `TOPS` | T-shirts, Long Sleeve T-shirts, Sleeveless T-shirts, Polo Shirts, Tanks & Camils, Crop Tops, Blouses, Shirts, Sweatshirts, Hoodies, Sweaters, Sweater Vests, Sports Tops, Bodysuits |
| Dresses/Jumpsuits | `DRESSES_JUMPSUITS` | Day Dress, T-Shirt Dress, Shirt Dress, Sweatshirt Dress, Sweater Dress, Jacket Dress, Suspender Dress, Jumpsuit, Party Dress, Mini Dress |
| Pants | `PANTS` | Jeans, Trousers, Dress Pants, Track Pants, Leggings, Shorts |
| Skirts | `SKIRTS` | Mini Skirts, Midi Skirts, Maxi Skirts |
| Outerwear | `OUTERWEAR` | Coats, Trench Coats, Fur Coats, Shearling Coats, Blazers, Jackets, Blousons, Varsity Jackets, Trucker Jackets, Biker Jackets, Cardigans, Zip Up Hoodies, Field Jackets, Sport Jackets, Fleece Jackets, Parkas, Down Jackets, Puffer Jackets, Vests |
| Shoes | `SHOES` | Sneakers, Slip Ons, Sports Shoes, Hiking Shoes, Boots, Combat Boots, Ugg Boots, Loafers & Loafer Mules, Boat Shoes & Moccasins, Flat Shoes, Heels, Sandals, Sandal Heels, Slides, Mule Heels |
| Bags | `BAGS` | Tote, Shoulder, Crossbody, Waist, Canvas, Backpacks, Duffel, Clutch, Briefcases, Drawstring, Suitcases |
| Headwear | `HEADWEAR` | Cap, Hats, Beanies, Berets, Fedoras, Sun Hats |

| Dresses/Jumpsuits Subtype | Canonical Identifier |
|---|---|
| Day Dress | `DAY_DRESS` |
| T-Shirt Dress | `T_SHIRT_DRESS` |
| Shirt Dress | `SHIRT_DRESS` |
| Sweatshirt Dress | `SWEATSHIRT_DRESS` |
| Sweater Dress | `SWEATER_DRESS` |
| Jacket Dress | `JACKET_DRESS` |
| Suspender Dress | `SUSPENDER_DRESS` |
| Jumpsuit | `JUMPSUIT` |
| Party Dress | `PARTY_DRESS` |
| Mini Dress | `MINI_DRESS` |

### 3.2 Globally Applicable Controlled Properties

| Property | Approved Values |
|---|---|
| Color | White, Ivory, Beige, Light-Gray, Dark-Gray, Black, Light-Yellow, Yellow, Turmeric, Orange, Coral, Red, Pink, Hot-Pink, Light-Green, Green, Olive, Dark-Olive, Teal, Khaki, Cyan, Sky-Blue, Blue, Navy, Lavender, Purple, Burgundy, Camel, Brown, Dark-Brown, Magenta, Gold, Silver, Colorful |
| Material | Cotton, Polyester, Nylon, Denim, Leather, Wool, Spandex (elastane), Linen, Acrylic, Silk, Rayon, Viscose, Tweed, Velvet, Chiffon, Suede, Corduroy, Lace, Fur, Down, Lyocell, Metal, Cupro, Cashmere, Mohair, Angora, Alpaca, Other Materials |
| Pattern | Solid, Striped, Graphic, Dotted, Animal, Floral, Tropical, Paisley, Argyle, Camo, Color-Block, Repeated, Checkerboard, Plaid, Gingham, Houndstooth, Herringbone, Chevron, Tweed, Abstract, Tie Dye, Geometric, Lace, Other Patterns |
| Style | Comfortable, Casual, Business Casual, Formal, Modern, Classic, Minimalist, Bohemian, Luxury, Sporty, Athleisure, Trendy, Kidcore, Artistic, Dressy, Feminine, Street |

Color is one dominant color; Colorful is the listed value for multicolored garments. It adds no secondary-color or coordinate field. Material is an optional collection of distinct approved values; repeated selections do not create repeated semantic materials. Material percentages/proportions are neither required nor fabricated. Pattern is one optional value. Style applies to every category; the same named list was repeated for Tops, Pants and other categories in the former layout. The former word Etc was explanatory, not a fashion style, wildcard or enum member. New styles require Product Owner approval.

### 3.3 Category-Specific Controlled Properties

| Property | Applicable Category | Approved Values |
|---|---|---|
| Fit | Tops | Slim, Regular, Loose, Oversized |
| Neckline | Tops | Round, Scoop (U), Boat, V-Neck, Deep-V, Square, Surplice, Shirt Collar, Standup-Collar, Wide-Collar, Mockneck, Turtleneck, Strapless, Thick-Strap, Thin-Strap, Sweetheart, Off-The-Shoulder, Asymmetric, Halter, Illusion, Keyhole, Suit-Collar, Other |
| Sleeve Length | Tops | Sleeveless, Cap Sleeve, Short Sleeve, 3/4 Sleeve, Long Sleeve |
| Length | Tops | Cropped, Waist, Hip, Knee |
| Length | Pants | Short, Long |
| Silhouette | Pants | Flare, Slim, Tapered, Baggy, Skinny, Bootcut, Wide-leg |

Do not apply Tops/Pants-specific fields to Dresses/Jumpsuits, Skirts, Outerwear, Shoes, Bags or Headwear. All global properties remain available for every category. Explicit labels such as Tanks & Camils are preserved as supplied; no semantic rename is introduced by this revision.

## 4. Brand, Price, Confirmation and Eligibility

Brand is optional user-entered text across all categories, without a fixed enum, external catalogue or brand-recognition feature. Preserve accepted values, and represent unspecified Brand truthfully.

Price is the optional user-reported amount originally paid to purchase the owned garment. When supplied it consists of a decimal amount and explicit ISO 4217 currency code. Both amount and currency must be supplied together or cleared; missing Price is unknown, not zero. Never infer currency from location, infer Price from appearance, use floating-point monetary values, or introduce conversion. This meaning is separate from market/resale/replacement/catalogue/affiliate values. Amount bounds, zero/negative handling, precision/rounding and currency-list policy remain unresolved where not otherwise approved; Brand length/normalization and Style selection cardinality need the applicable story-specific contract decision without adding vocabulary values.

User-confirmed information is authoritative. Later image upload or AI suggestions cannot silently overwrite accepted properties. Future analysis is suggestion-only and must stay within this model; a Brand suggestion requires acceptance and Price never comes from the photograph alone. Missing images or AI failure never block manual creation. Preserve ownership, privacy, provenance, failed/uncertain-action distinctions and safe retries with at most one accepted garment per logical addition.

Recommendation eligibility has not been evaluated. A saved garment is not invalid, unsuitable or ineligible because an engine policy is undefined or an optional property is absent. No ready/unready result, required compatibility field, fixed outfit composition or category-to-role mapping is approved for the current phase. Define composition, compatibility and sufficient-evidence policy during recommendation-engine development, using only approved properties unless the Product Owner approves a future model change. Sprint 2 exposes truthful accepted information and missing values; the smallest contract representation is selected during story-specific design, without mandating a NOT_EVALUATED enum or database field.

## 5. Technical Metadata, Images and Supersession

Garment/Owner identity, timestamps, record version, ownership/confirmation state, provenance and idempotency/action-outcome information are system metadata, outside the 13 fashion properties. Keep what security, persistence, concurrency and integrity require; this catalogue does not prescribe physical columns or add a generic taxonomy framework. Images are optional supporting media, not a required fashion property.

**Superseded fashion attributes:** Secondary Colors, HEX/HSL, Color Temperature, Palette Role, Pattern Density, Visual Noise, Layering Level, Bulk Index, Climate/Season Suitability and Occasion Tags are removed from the current approved garment model. Earlier conflicting subtype, color, pattern, fit and garment-style vocabularies are superseded by Section 3. They must not return as hidden fields, readiness gates or required derived attributes. User-profile style preferences, common needs, request occasion, weather context and necessary metadata remain distinct and are not removed by this garment decision.

The earlier recommendation-readiness predicates and fixed TOP + BOTTOM + FOOTWEAR template/category mappings are superseded. Future outfit/purchase research remains research, not current property vocabulary or an approved compatibility algorithm. Historical findings may retain their original text when clearly marked.

## 6. Traceability and Delivery

[BRD](../docs/01-business/BRD.md) BR-002/003/005 → [PRD](../docs/02-product/PRD.md) FEAT-AI-002/FEAT-GAR-001/FEAT-WAR-001 → [SRS](../docs/03-requirements/SRS.md) FR-GAR-001–013, DATA-GAR-001–005 → [Business Rules](../docs/03-requirements/business-rules.md) BRULE-GAR-001–010 → US-GAR-001, US-WAR-001/002 and UC-007/008/009 → [Sprint 2 plan](../docs/06-scrum/sprints/sprint-02/sprint-plan.md) and [Test Strategy](../docs/07-testing/test-strategy.md).

Retained superseded requirement/rule IDs preserve historical references; they do not require removed fields or readiness tests. Wardrobe remains the owning module. Exact API paths, DTOs, physical schema, normalization mechanics and transaction realization are selected-story JIT work. This documentation alignment implements no product code, migrations, AI extraction or recommendation engine.
