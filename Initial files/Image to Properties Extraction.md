# Garment Properties and Image-Assistance Research

**Project alignment, 2026-11-05.** The eight categories and ten Dresses/Jumpsuits subtypes below are approved product vocabulary. [PRD Sections 13.2/13.5](../docs/02-product/PRD.md#132-controlled-initial-garment-subtypes), [SRS Sections 3.5/4.10](../docs/03-requirements/SRS.md) and [BRULE-GAR-001–010](../docs/03-requirements/business-rules.md) govern implementation. The broader fashion lists retained below are research candidates, not additional approved enums or Sprint 2 obligations. Category is garment classification; it is not an outfit slot.

| Display Name | Canonical Identifier |
|---|---|
| Tops | `TOPS` |
| Dresses/Jumpsuits | `DRESSES_JUMPSUITS` |
| Pants | `PANTS` |
| Skirts | `SKIRTS` |
| Outerwear | `OUTERWEAR` |
| Shoes | `SHOES` |
| Bags | `BAGS` |
| Headwear | `HEADWEAR` |

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

Subtype is optional and must belong to its category when supplied. Exactly these ten Dresses/Jumpsuits values are approved; omission handles unknown subtype without adding OTHER/UNKNOWN here. MINI_DRESS may overlap length and PARTY_DRESS occasion, but neither automatically fills those attributes. JACKET_DRESS remains distinct from OUTERWEAR; JUMPSUIT stays in the combined category. User choice remains authoritative; further disambiguation and Bags/Headwear bounded vocabularies remain PRD OPQ-014.

- **Brand:** optional user-entered text for every category. No fixed enum or brand catalogue; it may be unknown. A future image-based suggestion is not guaranteed recognition and needs explicit user acceptance.
- **Price:** optional user-reported purchase price for every category, represented by a decimal amount and explicit ISO 4217 currency. Never infer it from appearance or infer currency from location; unknown does not mean zero. It is distinct from retailer, resale, market or replacement values. Monetary validation details remain PRD OPQ-013.

Saveable minimum remains supported category + supported dominant color + explicit user confirmation. Subtype, image, Brand, Price and every other rich attribute remain optional for saving. Image assistance is not required. Confirmed values are never silently overwritten by upload or later AI output; suggested, unknown and confirmed information stay distinct. Applicable readiness follows SRS Section 3.5.1; new category policies remain OPQ-012. Brand and Price are not readiness gates or mandatory compatibility-model inputs.

## Preserved Fashion Attribute Research

# **Properties:**

- Primary Category: Tops, Dresses/Jumpsuits, Pants, Skirts, Outerwear, Shoes, Bags, Headwear
- Subtype research candidates by category (approved values are in the PRD):
  - Tops: T-shirts, Long Sleeve T-shirts, Sleeveless T-shirts, Polo Shirts, Tanks & Camils, Crop Tops, Blouses, Shirts, Sweatshirts, Hoodies, Sweaters, Sweater Vests, Sports Tops, Bodysuits
  - Dresses/Jumpsuits (approved): Day Dress, T-Shirt Dress, Shirt Dress, Sweatshirt Dress, Sweater Dress, Jacket Dress, Suspender Dress, Jumpsuit, Party Dress, Mini Dress
  - Pants: Jeans, Trousers, Dress Pants, Track Pants, Leggings, Shorts
  - Skirts: Mini Skirts, Midi Skirts, Maxi Skirts
  - Outerwear: Coats, Trench Coats, Fur Coats, Shearling Coats, Blazers, Jackets, Blousons, Varsity Jackets, Trucker Jackets, Biker Jackets, Cardigans, Zip Up Hoodies, Field Jackets, Sport Jackets, Fleece Jackets, Parkas, Down Jackets, Puffer Jackets, Vests
  - Shoes: Sneakers, Slip Ons, Sports Shoes, Hiking Shoes, Boots, Combat Boots, Ugg Boots, Loafers & Loafer Mules, Boat Shoes & Moccasins, Flat Shoes, Heels, Sandals, Sandal Heels, Slides, Mule Heels
  - Bags: Tote, Shoulder, Crossbody, Waist, Canvas, Backpacks, Duffel, Clutch, Briefcases, Drawstring, Suitcases
  - Headwear: Cap, Hats, Beanies, Berets, Fedoras, Sun Hats
- Color: White, Ivory, Beige, Light-Gray, Dark-Gray, Black, Light-Yellow, Yellow, Turmeric, Orange, Coral, Red, Pink, Hot-Pink, Light-Green, Green, Olive, Dark-Olive, Teal, Khaki, Cyan, Sky-Blue, Blue, Navy, Lavender, Purple, Burgundy, Camel, Brown, Dark-Brown, Magenta, Gold, Silver, Colorful
- Material: Cotton, Polyester, Nylon, Denim, Leather, Wool, Spandex (elastane), Linen, Acrylic, Silk, Rayon, Viscose, Tweed, Velvet, Chiffon, Suede, Corduroy, Lace, Fur, Down, Lyocell, Metal, Cupro, Cashmere, Mohair, Angora, Alpaca, Other Materials
- Pattern: Solid, Striped, Graphic, Dotted, Animal, Floral, Tropical, Paisley, Argyle, Camo, Color-Block, Repeated, Checkerboard, Plaid, Gingham, Houndstooth, Herringbone, Chevron, Tweed, Abstract, Tie Dye, Geometric, Lace, Other Patterns
- Details
  - Tops:
    - **Style**: Comfortable, Casual, Business Casual, Formal, Modern, Classic, Minimalist, Bohemian, Luxury, Sporty, Athleisure, Trendy, Kidcore, Artistic, Dressy, Feminine, Street, Etc
    - **Fit**: Slim, Regular, Loose, Oversized
    - **Neckline**: Round, Scoop (U), Boat, V-Neck, Deep-V, Square, Surplice, Shirt Collar, Standup-Collar, Wide-Collar, Mockneck, Turtleneck, Strapless, Thick-Strap, Thin-Strap, Sweetheart, Off-The-Shoulder, Asymmetric, Halter, Illusion, Keyhole, Suit-Collar, Other
    - **Sleeve length**: Sleeveless, Cap Sleeve, Short Sleeve, 3/4 Sleeve, Long Sleeve
    - **Length**: Cropped, Waist, Hip, Knee
  - Pants:
    - **Style**: Comfortable, Casual, Business Casual, Formal, Modern, Classic, Minimalist, Bohemian, Luxury, Sporty, Athleisure, Trendy, Kidcore, Artistic, Dressy, Feminine, Street, Etc
    - Silhouette: Flare, Slim, Tapered, Baggy, Skinny, Bootcut, Wide-leg
    - Length: Short, Long
  - Other Categories:
    - **Style**: Comfortable, Casual, Business Casual, Formal, Modern, Classic, Minimalist, Bohemian, Luxury, Sporty, Athleisure, Trendy, Kidcore, Artistic, Dressy, Feminine, Street, Etc
