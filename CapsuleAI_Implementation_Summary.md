# CapsuleAI — Implementation Summary

> **Purpose of this file:** Summarize the three project documents into an implementation-oriented plan so the development team can quickly understand **what must be built**, **how the system is expected to work**, **what is out of scope**, and **which requirements still need clarification**.
>
> This summary is grounded in:
> 1. `DA 2 Proposal.docx`
> 2. `ĐỀ CƯƠNG ĐỒ ÁN 2_ HỆ THỐNG QUẢN LÝ VÀ GỢI Ý PHỐI ĐỒ THÔNG MINH (2).docx`
> 3. `Product Requirements Document_ CapsuleAI.docx`
>
> Items explicitly labeled **Suggested implementation mapping** or **Open question** are synthesis/engineering guidance derived from the documents, not additional requirements stated verbatim in them.

---

## 1. Project in One Sentence

**CapsuleAI** is a cross-platform mobile application that digitizes a user's wardrobe from photos, automatically extracts clothing attributes, recommends weather-appropriate outfits from clothes the user already owns, and identifies strategic new purchases that would unlock the largest number of additional outfit combinations.

The product is intended to act as both:

- a **personal digital stylist**, and
- a **smart shopping advisor**.

The main problems being addressed are:

- morning outfit decision fatigue;
- repeated use of only a few familiar outfits;
- poor visibility into the full wardrobe;
- impulse purchases that do not combine well with existing clothes;
- clothing waste and low wardrobe utilization.

---

# 2. Core Product Scope

The documents consistently describe **three central product pillars**.

## 2.1 AI Digital Closet

The user should be able to photograph or upload clothing and have the system automatically build a structured digital wardrobe.

### Required behavior

1. User takes a photo with the mobile camera or selects one from the device gallery.
2. The image is uploaded to the backend.
3. The original image is stored in object storage.
4. Computer Vision processing:
   - removes the background;
   - produces a transparent PNG;
   - identifies clothing attributes.
5. The app displays the processed item and predicted attributes.
6. The user can correct the AI-generated tags.
7. The confirmed item is saved to the user's wardrobe.

### Main attributes mentioned in the documents

At minimum:

- `category`
  - Top
  - Bottom
  - Footwear
  - Outerwear
- `sub_category`
- dominant / primary color
- sub-color / color palette
- pattern
  - solid
  - striped
  - plaid
  - etc.
- seasonality
  - summer
  - winter
  - all-season
- image URLs
  - original image
  - background-removed image
- `wear_count`
- creation date

The proposal also mentions extracting or researching attributes such as **fabric/material**, although the more detailed MVP specifications concentrate mainly on category, color, pattern, and season.

### Important UI behavior

The upload flow should be fast and simple:

`Capture/Upload -> AI Processing -> Tag Confirmation -> Save`

The proposal expects this flow to require no more than approximately **three user interactions/taps** after capture.

---

## 2.2 Outfit Recommendation Engine

The application must generate outfits using **items already present in the user's wardrobe**.

For the daily mix-and-match feature, the documents explicitly exclude inserting unowned items into normal outfit recommendations.

### Minimum outfit structure

The detailed PRD defines this base structure:

```text
1 Top
+ 1 Bottom
+ 1 Footwear
+ optional Outerwear
```

Outerwear is conditionally added in the PRD when:

```text
temperature < 18°C
```

The broader proposal states that an outfit can consist of approximately **2–4 garments**, depending on context.

### Recommendation inputs

The engine should consider:

- current wardrobe inventory;
- temperature;
- precipitation / weather condition;
- garment season;
- color compatibility;
- pattern compatibility;
- user preferences/history.

The Vietnamese proposal additionally discusses:

- style preference;
- work / occasion context;
- clothing form/shape;
- personal preferences.

However, the detailed PRD gives precise deterministic rules mainly for weather, slots, color, and patterns.

### Deterministic styling rules explicitly defined

#### Rule 1 — Required slots

Every generated outfit should contain:

- exactly one Top;
- exactly one Bottom;
- exactly one Footwear item;
- optional Outerwear.

#### Rule 2 — Thermal suitability

The selected garments' season tags must be compatible with real-time weather/temperature.

#### Rule 3 — Color harmony

The engine should apply fashion color relationships such as:

- Monochromatic;
- Analogous;
- Complementary.

The PRD defines these neutral colors as broadly compatible wildcard colors:

- White
- Black
- Navy
- Gray
- Beige

#### Rule 4 — Pattern balance

Maximum:

```text
1 patterned garment per outfit
```

The other garments should be solid.

### Recommendation output

When the user requests recommendations, the system should return at least:

```text
3 distinct outfit combinations
```

### Required interactions

#### Shuffle Item

The user can lock most of an outfit and replace only one slot.

Example:

```text
Keep:
- Bottom
- Footwear
- Outerwear

Replace:
- Top
```

The recommendation engine then finds a replacement that remains compatible with the locked items and weather.

#### Wear This Today

When the user confirms an outfit:

- save the selected outfit to history;
- update the user's wearing log;
- use the interaction as behavioral feedback for future recommendations.

The Vietnamese proposal also specifies a `Like/Dislike` status in outfit history.

---

# 3. Strategic Shopping — Wardrobe Multiplier / Gap Analysis

This is the main differentiating feature emphasized across the documents.

Instead of recommending arbitrary products, CapsuleAI should identify **missing wardrobe staples that maximize the number of useful new outfit combinations**.

## 3.1 Static staple catalog

The system should maintain a catalog of common foundational wardrobe items such as:

- white T-shirt;
- blue jeans;
- white leather sneakers;
- blazer;
- beige chinos;
- trench coat;
- similar capsule-wardrobe staples.

A catalog item may contain information such as:

- category;
- color;
- pattern;
- season;
- material;
- estimated price range;
- durability/longevity rating;
- external purchasing URL.

## 3.2 Gap Analysis

For candidate catalog items that are not adequately represented in the user's wardrobe:

1. Temporarily treat the candidate as if the user owned it.
2. Run the same outfit compatibility engine.
3. Count the additional valid outfits enabled by that item.
4. Compare candidate items.
5. Recommend items with high utility.

Conceptually:

```text
Current wardrobe
      |
      v
Find missing staple candidate
      |
      v
Temporarily add candidate
      |
      v
Generate all/new valid outfits
      |
      v
Count newly unlocked combinations
      |
      v
Wardrobe Multiplier
```

Example product message from the documents:

```text
Classic White Sneaker
Estimated Cost: $50–$120
Longevity: High
Wardrobe Multiplier: +14 New Outfits
```

## 3.3 Recommendation screen

Each shopping recommendation should show:

- recommended item;
- ideal attributes;
- estimated price range;
- durability/longevity;
- Wardrobe Multiplier / Combo Multiplier;
- examples of newly possible outfits;
- external shopping/affiliate link.

### Important scope boundary

CapsuleAI does **not** implement:

- in-app payment;
- checkout;
- shopping cart synchronization;
- a complete e-commerce marketplace.

The application only redirects users to an external purchasing page.

---

# 4. Main User Journeys

## 4.1 Onboarding and Wardrobe Setup

```text
Register / Login
    |
    v
Set profile
- Gender
- Style preference
- Location permission
    |
    v
Photograph 5–10 favorite garments
    |
    v
AI processes each image
    |
    v
User confirms/corrects tags
    |
    v
Items are added to Digital Closet
```

The product proposal recommends prompting a new user to upload approximately **5–10 favorite items** during onboarding.

---

## 4.2 Daily Outfit Flow

```text
Open app
    |
    v
Fetch current weather
    |
    v
Load wardrobe
    |
    v
Generate at least 3 compatible outfits
    |
    v
Display carousel/cards
    |
    +--> Shuffle one garment
    |
    +--> Choose another outfit
    |
    v
Wear This Today
    |
    v
Save to outfit history
```

The home screen is expected to prominently show:

- current/local weather;
- outfit recommendation carousel;
- quick interaction with generated outfits.

---

## 4.3 Smart Shopping Flow

```text
Open "Shop Smart"
    |
    v
Analyze wardrobe completeness/gaps
    |
    v
Rank useful missing staple items
    |
    v
Display recommendation card
    |
    v
Show multiplier + simulated outfits
    |
    v
Open external shopping link
```

The product description uses a UX concept such as:

> "Your wardrobe is 80% complete. Here is what's missing..."

However, the exact formula for the **wardrobe completeness percentage** is not specified in the documents.

---

# 5. Required Functional Modules

A practical decomposition of the documented requirements is below.

## 5.1 Account and Personalization Module

Must support:

- registration;
- login;
- user profile management;
- gender;
- style preferences;
- location permission/location information;
- authentication.

Documented authentication options:

- JWT;
- OAuth 2.0.

The detailed PRD explicitly requires JWT verification across Java backend endpoints.

---

## 5.2 Wardrobe Management Module

Must support:

- create garment;
- view garment;
- update garment;
- delete garment;
- search/filter closet;
- edit AI-generated tags;
- store raw and processed image information;
- track `wear_count`.

When an item is deleted, the detailed PRD requires:

- deleting the associated S3 asset(s);
- invalidating cached outfit combinations involving the item.

---

## 5.3 Image/CV Processing Module

Must support:

- image upload;
- raw image storage;
- background removal;
- transparent PNG generation;
- category recognition;
- color extraction;
- pattern recognition;
- season prediction/tagging;
- low-confidence/error fallback to manual entry.

The research proposal mentions possible approaches/models such as:

- U2-Net;
- RMBG;
- multi-label fashion classifiers;
- CLIP zero-shot or fine-tuned models.

The detailed PRD also allows third-party CV providers such as:

- Photoroom;
- Google Cloud Vision;
- similar Vision APIs.

---

## 5.4 Weather Module

Use a weather provider such as:

- OpenWeatherMap API;
- equivalent weather service.

Weather data mentioned includes:

- temperature;
- precipitation;
- rain/sunny state;
- UV index.

The outfit engine consumes this information.

---

## 5.5 Outfit Engine Module

Responsibilities:

- validate item-slot requirements;
- filter thermally unsuitable clothing;
- calculate color compatibility;
- enforce pattern rules;
- generate valid combinations;
- score compatibility;
- return at least three outfits;
- support single-slot shuffle;
- preserve locked outfit components.

The Vietnamese proposal additionally mentions moving toward **content-based filtering from user interaction history**, while the detailed PRD defines the first engine mainly as deterministic heuristics.

---

## 5.6 Outfit History / Feedback Module

Must support:

- logging a selected daily outfit;
- recording its garment IDs;
- storing compatibility score;
- date/time;
- Like/Dislike feedback;
- updating wear-related information.

This history is intended to help the system learn user preferences.

---

## 5.7 Gap Analysis / Wardrobe Multiplier Module

Responsibilities:

- load static wardrobe-staple catalog;
- detect candidate missing items;
- simulate outfit generation with each candidate;
- count newly unlocked outfits;
- calculate multiplier metric;
- rank/display useful candidates;
- generate preview combinations;
- provide external shopping link.

---

# 6. System Architecture

The documents converge on the following architecture:

```text
┌─────────────────────────────────────┐
│ React Native Mobile App             │
│                                     │
│ - Authentication UI                 │
│ - Camera / Gallery                  │
│ - Closet UI                         │
│ - Outfit Carousel                   │
│ - Shuffle / Wear Today              │
│ - Gap Analysis / Shopping UI        │
└──────────────────┬──────────────────┘
                   │ HTTPS / REST
                   v
┌─────────────────────────────────────┐
│ Java Spring Boot Backend            │
│                                     │
│ - Authentication / Access Control   │
│ - User/Profile Logic                │
│ - Wardrobe CRUD                     │
│ - S3 orchestration                  │
│ - Weather orchestration             │
│ - CV orchestration                  │
│ - Outfit recommendation rules       │
│ - Wardrobe Multiplier               │
│ - Outfit history                    │
└───────┬────────────┬────────────┬────┘
        │            │            │
        │            │            │
        v            v            v
┌─────────────┐ ┌──────────┐ ┌────────────────────┐
│ MongoDB     │ │ AWS S3   │ │ External Services  │
│             │ │          │ │                    │
│ Users       │ │ Raw      │ │ Weather API        │
│ Garments    │ │ images   │ │ Vision API         │
│ Outfits     │ │ PNGs     │ │ or Python AI/CV    │
│ History     │ │          │ │ service            │
│ Catalog     │ │          │ │                    │
└─────────────┘ └──────────┘ └────────────────────┘
```

## Technology stack explicitly proposed

| Layer | Technology |
|---|---|
| Mobile client | React Native |
| Core backend | Java + Spring Boot |
| AI/CV | Python and/or external Vision APIs |
| Database | MongoDB |
| Object storage | AWS S3 |
| Weather | OpenWeatherMap or equivalent |
| API style | RESTful APIs |
| Transport security | HTTPS/TLS |

---

# 7. Suggested Backend Domain Structure

> **Suggested implementation mapping:** The exact Java package/module structure is not prescribed in the documents. The following structure maps the documented responsibilities into implementation units.

```text
backend/
└── src/main/java/.../
    ├── auth/
    ├── user/
    ├── wardrobe/
    ├── image/
    ├── weather/
    ├── outfit/
    ├── recommendation/
    ├── catalog/
    ├── history/
    ├── storage/
    └── common/
```

Possible responsibilities:

### `auth`
- register/login;
- JWT creation/validation;
- authorization.

### `user`
- profile;
- gender;
- style preferences;
- location settings.

### `wardrobe`
- garment CRUD;
- search/filter;
- garment metadata.

### `image`
- CV processing orchestration;
- prediction confidence;
- manual fallback.

### `storage`
- S3 upload;
- pre-signed URLs;
- deletion.

### `weather`
- weather-provider client;
- normalized weather model.

### `outfit`
- compatibility rules;
- combination generation;
- shuffle.

### `history`
- daily outfit logging;
- likes/dislikes;
- wear tracking.

### `catalog`
- capsule staple catalog.

### `recommendation`
- gap analysis;
- multiplier calculation;
- shopping recommendations.

---

# 8. Data You Need to Store

## 8.1 Wardrobe Item

The source documents explicitly mention data equivalent to:

```json
{
  "id": "...",
  "userId": "...",
  "category": "TOP",
  "subCategory": "...",
  "hexColors": ["#..."],
  "pattern": "SOLID",
  "seasons": ["ALL_SEASON"],
  "wearCount": 0,
  "originalImageUrl": "...",
  "processedImageUrl": "...",
  "createdAt": "..."
}
```

Additional source-mentioned attributes may include:

- sub-color;
- material/fabric.

The exact final MongoDB schema is not specified.

---

## 8.2 Outfit

The proposal explicitly expects fields equivalent to:

```json
{
  "id": "...",
  "userId": "...",
  "topId": "...",
  "bottomId": "...",
  "footwearId": "...",
  "outerwearId": "...",
  "compatibilityScore": 0.0
}
```

`outerwearId` can be optional.

---

## 8.3 Outfit Log

Store information such as:

```json
{
  "id": "...",
  "userId": "...",
  "outfitId": "...",
  "date": "...",
  "feedback": "LIKE"
}
```

The documents explicitly mention:

- outfit history;
- `Like/Dislike`;
- "Wear This Today".

---

## 8.4 Catalog Item

> **Suggested implementation mapping:** The documents specify the information to display and the existence of a static catalog, but do not prescribe its exact MongoDB document.

A useful representation would contain:

```json
{
  "id": "...",
  "name": "Classic White Sneaker",
  "category": "FOOTWEAR",
  "color": "WHITE",
  "pattern": "SOLID",
  "season": ["ALL_SEASON"],
  "material": "LEATHER",
  "estimatedPriceMin": 50,
  "estimatedPriceMax": 120,
  "durability": "HIGH",
  "externalUrl": "..."
}
```

---

# 9. Suggested REST API Surface

> **Suggested implementation mapping:** The documents require RESTful APIs but do not define exact endpoint paths. These endpoints are a practical mapping of the stated features.

```text
/auth
POST   /auth/register
POST   /auth/login

/users
GET    /users/me
PATCH  /users/me

/wardrobe
POST   /wardrobe/items
GET    /wardrobe/items
GET    /wardrobe/items/{id}
PATCH  /wardrobe/items/{id}
DELETE /wardrobe/items/{id}

/image-processing
POST   /wardrobe/analyze

/weather
GET    /weather/current

/outfits
GET    /outfits/recommendations
POST   /outfits/{id}/shuffle
POST   /outfits/{id}/wear
POST   /outfits/{id}/feedback

/history
GET    /outfits/history

/shopping
GET    /shopping/gaps
GET    /shopping/recommendations
GET    /shopping/recommendations/{id}
```

The API should return normalized data to the React Native client rather than exposing provider-specific Vision/Weather responses directly.

---

# 10. Mobile Screens to Implement

Based on the documented user flows, the mobile application needs approximately the following screens.

## Authentication / Onboarding

- Register
- Login
- Initial profile setup
  - gender
  - style preferences
  - location permission

## Wardrobe

- Digital Closet
- Camera / Upload
- AI Processing state
- Tag Confirmation / Edit
- Garment Detail
- Closet Search / Filter

## Daily Recommendations

- Home
  - weather summary
  - three recommended outfits
- Outfit Detail
- Shuffle Item interaction
- Wear This Today
- Like / Dislike

## Smart Shopping

- Gap Analysis
- Recommendation List
- Recommendation Detail
- Multiplier result
- Newly unlocked outfit previews
- External purchase link

---

# 11. Non-Functional Requirements

## Performance

The documents state several related targets:

| Operation | Requirement appearing in documents |
|---|---|
| Outfit generation | `< 3 seconds` |
| Outfit generation at 100+ wardrobe items | `< 3 seconds` |
| Image background removal + tagging | `3–5 seconds` in proposal |
| Image processing | `< 6 seconds` in detailed PRD |
| Normal API read/write | average `< 500 ms` in proposal |
| Cached wardrobe reads | `< 200 ms` in detailed PRD |

These should be converted into measurable integration/performance tests.

---

## Accuracy

Two thresholds appear in the documents:

- proposal: at least **85%** correct primary category + dominant color for well-lit images with limited occlusion;
- product/PRD documents: approximately **90%** successful background removal/category/color tagging for well-lit uploads.

This discrepancy should be resolved before defining the final acceptance test.

---

## Resilience

If a Vision API fails or returns low-confidence predictions:

```text
Do not block wardrobe creation.
```

Instead:

1. retain the uploaded image if appropriate;
2. show a manual tag form;
3. allow the user to enter/correct the item's data;
4. continue saving the wardrobe item.

---

## Security & Privacy

Required controls include:

- authenticated access to user data;
- JWT or OAuth-based authentication;
- JWT verification on protected backend endpoints;
- HTTPS/TLS;
- strict user-level access control;
- S3 access using constrained/pre-signed URLs;
- wardrobe images inaccessible to unrelated users.

---

## Data Hygiene

Hard deletion of a garment should also:

1. delete corresponding S3 object(s);
2. remove/invalidate generated or cached combinations using the deleted item.

---

## Maintainability

The proposal explicitly asks for separation between:

```text
Core Backend
```

and

```text
AI / Computer Vision processing
```

so that the AI implementation can later be changed without rewriting the business backend.

---

# 12. Out of Scope

Do **not** spend the project timeline implementing these unless the team intentionally changes scope:

- AR virtual try-on;
- 3D garment rendering;
- real-time video garment processing;
- direct payment gateway;
- native in-app checkout;
- shopping cart synchronization;
- full e-commerce marketplace;
- complex accessories such as:
  - jewelry;
  - scarves;
  - hair accessories;
  - watches.

The MVP focuses primarily on:

```text
Top
Bottom
Outerwear
Footwear
```

---

# 13. Recommended Implementation Order

The detailed PRD already defines a five-sprint roadmap. It is the clearest implementation sequence across the documents.

## Sprint 1 — Foundation & Asset Pipeline

Implement:

- React Native project setup;
- Spring Boot API scaffolding;
- MongoDB connection;
- authentication skeleton;
- AWS S3 integration;
- camera/image upload;
- CV/background-removal integration;
- basic end-to-end image flow.

### Sprint 1 success condition

A user can send a garment image from the mobile app and receive/store a processed version.

---

## Sprint 2 — Wardrobe Management & UI

Implement:

- garment metadata;
- AI tag extraction;
- tag confirmation/editing;
- wardrobe CRUD;
- closet list/grid;
- filter/search;
- image deletion/data cleanup.

### Sprint 2 success condition

A user can build and manage a useful digital wardrobe.

---

## Sprint 3 — Deterministic Outfit Engine

Implement:

- OpenWeatherMap integration;
- temperature/season filtering;
- slot rules;
- color harmony matrix;
- neutral colors;
- pattern balancing;
- compatibility scoring;
- generate 3 outfits;
- carousel/card UI;
- Shuffle Item;
- Wear This Today;
- outfit history.

### Sprint 3 success condition

A user can open the application and obtain at least three weather-compatible outfit recommendations from their existing wardrobe.

---

## Sprint 4 — Gap Analysis & Strategic Shopping

Implement:

- static capsule-staple catalog;
- missing-item candidate detection;
- hypothetical outfit simulation;
- Wardrobe Multiplier;
- recommendation ranking;
- recommendation cards;
- simulated outfit previews;
- external links.

### Sprint 4 success condition

For a recommended catalog item, the system can display a valid number of additional outfits that the purchase would unlock.

---

## Sprint 5 — Hardening & Project Polish

Implement:

- end-to-end testing;
- security checks;
- access-control tests;
- failure/fallback behavior;
- performance testing;
- latency optimization;
- CV confidence/error handling;
- UI polish;
- submission/demo documentation.

---

# 14. What the MVP Should Demonstrate

For a university project/demo, the complete system should be able to demonstrate this end-to-end scenario:

```text
1. User registers/logs in.
2. User configures basic style/location preferences.
3. User photographs a shirt.
4. Background is removed automatically.
5. System predicts category/color/pattern/season.
6. User corrects or confirms the prediction.
7. Item appears in Digital Closet.
8. User repeats this for enough garments to form outfits.
9. App retrieves current weather.
10. Engine returns at least 3 outfits.
11. User shuffles one garment while preserving the others.
12. User selects "Wear This Today".
13. Outfit is written to history.
14. App analyzes wardrobe gaps.
15. App recommends a missing staple.
16. App shows "+N New Outfits".
17. User previews those new hypothetical outfits.
18. User can follow an external purchase link.
```

If this workflow works reliably, the project demonstrates all three main technical contributions:

1. **Computer Vision wardrobe digitization**
2. **Context-aware outfit recommendation**
3. **Wardrobe utility / gap-analysis recommendation**

---

# 15. Important Decisions to Clarify Before Coding

The three documents are largely consistent, but some details are either different or underspecified.

## 15.1 CV architecture: Python model vs third-party Vision API

One proposal describes an independent:

```text
Python AI/CV Service
```

and discusses U2-Net/RMBG/CLIP.

The PRD describes:

```text
Spring Boot -> third-party Vision API
```

such as Photoroom or Google Cloud Vision.

### Decision needed

Choose one of these architectures for the MVP:

```text
A. Java Backend -> External Vision API
```

or

```text
B. Java Backend -> Python AI Service -> Model
```

or a deliberately designed hybrid.

Do not accidentally build both unless the project specifically requires comparison/research.

---

## 15.2 Final image-processing accuracy target

Conflicting source values:

```text
85%
```

vs

```text
90%
```

Choose one official acceptance criterion.

---

## 15.3 Final image-processing latency target

Source values:

```text
3–5 seconds
```

and

```text
under 6 seconds
```

Define one measurable target for testing.

---

## 15.4 API latency target

The proposal says average normal API requests should be:

```text
< 500 ms
```

The detailed PRD specifically asks cached wardrobe reads to be:

```text
< 200 ms
```

These can coexist if treated as separate metrics, but the team should document that interpretation.

---

## 15.5 Exact Wardrobe Multiplier formula

The documents describe the algorithm conceptually:

```text
new valid outfits enabled by candidate item
```

but do not provide a complete mathematical formula or normalization rule.

The team must define:

- what counts as a unique outfit;
- whether duplicate/color-near-equivalent combinations count separately;
- whether the score is simply a count or weighted;
- whether weather/style suitability affects the score;
- how candidate items are ranked when scores tie.

---

## 15.6 Wardrobe completion percentage

The UX example displays:

```text
"Your wardrobe is 80% complete"
```

but the documents do not define how that percentage is computed.

A formal rule is needed before implementing this indicator.

---

## 15.7 User preference learning

The proposal mentions:

- content-based filtering;
- user interaction history;
- learning from "Wear This Today";
- Like/Dislike.

The PRD's core outfit engine is deterministic.

The team should decide whether the first version will:

```text
A. only log feedback for future use
```

or

```text
B. actually modify recommendation scores using that feedback.
```

---

## 15.8 Style/occasion/body-shape scope

The broad research motivation mentions:

- event/work context;
- body shape;
- clothing shape/form.

The detailed functional PRD does not define algorithms or required fields for all of these.

Treat them as **not fully specified** until the team confirms whether they are MVP requirements.

---

# 16. Suggested Team Work Split

> **Suggested implementation mapping:** This is not prescribed by the documents, but follows their architecture.

### Mobile Developer

Own:

- React Native;
- camera/gallery;
- upload flow;
- tag confirmation;
- wardrobe UI;
- outfit carousel;
- shuffle interactions;
- Shop Smart UI.

### Java Backend Developer

Own:

- Spring Boot REST API;
- authentication;
- access control;
- user/profile;
- wardrobe CRUD;
- S3 orchestration;
- weather integration;
- outfit rules;
- history;
- multiplier service;
- MongoDB integration.

### AI/CV Developer

Own:

- background removal;
- fashion attribute extraction;
- confidence values;
- model/API evaluation;
- Python inference service if the team chooses that architecture.

### Shared Work

- MongoDB schema;
- API contract;
- recommendation-rule definitions;
- integration tests;
- deployment;
- performance tests;
- project report/demo.

---

# 17. Practical Development Checklist

## Foundation

- [ ] React Native repository created
- [ ] Spring Boot repository created
- [ ] MongoDB configured
- [ ] AWS S3 bucket configured
- [ ] environment configuration strategy defined
- [ ] local/dev deployment configuration created
- [ ] HTTPS/deployment plan identified

## Authentication

- [ ] register
- [ ] login
- [ ] JWT/OAuth decision
- [ ] protected endpoints
- [ ] user-level authorization

## Digital Closet

- [ ] camera/gallery upload
- [ ] raw-image S3 upload
- [ ] background removal
- [ ] transparent PNG storage
- [ ] category extraction
- [ ] color extraction
- [ ] pattern extraction
- [ ] season tagging
- [ ] confidence/error handling
- [ ] manual tag correction
- [ ] garment CRUD
- [ ] filter/search
- [ ] delete S3 assets with garment deletion

## Weather

- [ ] location permission
- [ ] current-weather API
- [ ] normalized temperature
- [ ] precipitation state
- [ ] weather service error handling

## Outfit Engine

- [ ] Top slot
- [ ] Bottom slot
- [ ] Footwear slot
- [ ] conditional Outerwear
- [ ] season/temperature validation
- [ ] monochromatic rule
- [ ] analogous rule
- [ ] complementary rule
- [ ] neutral wildcard behavior
- [ ] max-one-pattern rule
- [ ] compatibility scoring
- [ ] at least 3 recommendations
- [ ] response under target latency
- [ ] Shuffle Item
- [ ] Wear This Today
- [ ] Like/Dislike
- [ ] outfit history

## Gap Analysis

- [ ] static staple catalog
- [ ] gap detection
- [ ] candidate simulation
- [ ] unique-new-outfit counting
- [ ] multiplier calculation
- [ ] recommendation ranking
- [ ] estimated price
- [ ] durability
- [ ] simulated outfit previews
- [ ] external URL

## Quality

- [ ] unit tests
- [ ] integration tests
- [ ] end-to-end tests
- [ ] authorization tests
- [ ] image-pipeline fallback tests
- [ ] outfit-rule tests
- [ ] multiplier tests
- [ ] performance tests
- [ ] data cleanup tests

---

# 18. Final Priority View

If development time becomes limited, prioritize the project in this order:

```text
P0 — Required foundation
Authentication
Wardrobe CRUD
Image upload/storage

P0 — Core project contribution
Background removal + attribute extraction
Deterministic outfit recommendation
Weather-aware generation
3-outfit result
Shuffle
Wear This Today

P0 — Main differentiating feature
Static staple catalog
Gap Analysis
Wardrobe Multiplier

P1 — Important supporting capability
History
Like/Dislike
Search/filter
Simulated outfit preview
External shopping links

P2 — Only if time permits / once clearly specified
Advanced personalization from behavior
More complex form/body-shape rules
Occasion-aware ranking
More sophisticated AI models
Additional categories/accessories
```

> `P0/P1/P2` above is an implementation-planning interpretation of the documented MVP, not a priority notation explicitly used in the source files.

---

# 19. Bottom Line

For implementation purposes, think of CapsuleAI as **four connected systems**:

```text
[1] Digital Wardrobe
    Photo -> CV -> Structured garment data

[2] Styling Engine
    Wardrobe + Weather + Rules -> 3 outfits

[3] Preference/History Loop
    Shuffle + Wear + Like/Dislike -> interaction history

[4] Smart Shopping Engine
    Wardrobe + Static Catalog + Styling Engine
        -> Gap Analysis
        -> Wardrobe Multiplier
        -> Strategic purchase recommendation
```

The **most important architectural idea** is that the outfit compatibility logic should be reusable.

The same compatibility engine should support both:

```text
Daily Outfit Generation
```

and:

```text
Hypothetical Outfit Generation for Wardrobe Multiplier
```

That avoids building two unrelated recommendation systems and directly matches the relationship between the two features described in the PRD.

Before implementation begins, the team should first resolve the open decisions in Section 15—especially the **CV architecture**, **acceptance thresholds**, and the exact **Wardrobe Multiplier definition**.
