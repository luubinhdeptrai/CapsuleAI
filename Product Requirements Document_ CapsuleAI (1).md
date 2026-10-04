# **Product Requirements Document: CapsuleAI**

## **1. Summary & Objective**

CapsuleAI resolves morning decision fatigue and prevents uncoordinated clothing purchases by serving as a personalized digital stylist and strategic shopping advisor. The system operates on a mobile-first architecture that digitizes wardrobes, executes deterministic styling heuristics, and identifies wardrobe gaps using mathematical utility scores.

## **2. User Personas & Core Journeys**

### **User Personas**

* **The Indecisive Professional:** Needs rapid, weather-appropriate daily outfit recommendations to eliminate morning friction and save time.
* **The Fashion-Conscious Minimalist:** Wants to build a functional capsule wardrobe, maximize garment utilization, and minimize clutter.
* **The Smart Shopper:** Requires quantitative validation before purchasing new clothes to guarantee pairing compatibility with existing items.

### **Core User Flows**

* **Wardrobe Ingestion:** User captures photos of clothing items through the mobile app, views auto-segmented assets with tags, verifies attributes, and commits items to their digital closet.
* **Daily Outfit Generation:** User views localized weather data alongside three pre-calculated outfits, shuffles individual item slots as needed, and confirms the chosen daily outfit.
* **Gap Analysis & Strategic Shopping:** User navigates to the recommendations screen, inspects suggested catalog items with high multiplier scores, and reviews simulated outfits linking to external purchase pages.

## **3. System Architecture & Tech Stack**

| **Layer** | **Technology** | **Primary Responsibilities** |
| --- | --- | --- |
| **Mobile Client** | React Native | Camera integration, local image caching, optimistic UI updates, carousel gestures. |
| **Application Backend** | Java (Spring Boot) | RESTful API routing, business logic, CV vendor orchestration, deterministic recommendation engine. |
| **AI Layer** | Python |  |
| **Data Persistence** | MongoDB | Document store for unstructured garment tags, outfit permutations, user preferences, and static catalog items. |
| **Asset Storage** | AWS S3 | Object store for raw camera uploads and transparent PNG segmented garment assets. |
| **External Services** | Vision API & Weather API | Third-party background removal and tagging (e.g., Photoroom / Google Cloud Vision), OpenWeatherMap API for local telemetry. |

##

## **4. Functional Specifications**

### **Feature 1: AI Digital Closet (Smart Upload)**

* **Input Pipeline:** The React Native client captures garment images via the camera API and streams raw image payloads directly to the Java backend.
* **Storage & Processing:** The backend persists the raw asset to AWS S3, dispatches the URL to external Vision APIs to isolate the garment and strip backgrounds, and returns a transparent PNG saved back to S3.
* **Automated Tagging:** The API extracts primary attributes: category (top, bottom, footwear, outerwear), dominant color, sub-color, pattern (solid, striped, plaid), and seasonality (summer, winter, all-season).
* **User Confirmation:** The UI renders the segmented garment and allows manual override of category and color tags before writing the final record to MongoDB.
* **Out of Scope:** 3D garment rendering, AR try-on, and real-time video capture.
* **Acceptance Criteria:** Background removal and category/color tagging succeed on at least 90% of well-lit uploads.

### **Feature 2: Heuristic Outfit Recommendation Engine**

* **Input Vector:** Local weather conditions (temperature, precipitation) combined with the user's active closet inventory.
* **Deterministic Rule Set:**
  + *Slot Requirement:* Every outfit requires 1 Top + 1 Bottom + 1 Footwear. Outerwear is conditionally injected based on temperature (< 18°C).
  + *Thermal Suitability:* Garment season tags must align with real-time temperature thresholds.
  + *Color Harmonization:* Enforces color wheel rules (Monochromatic, Analogous, Complementary) while treating Neutrals (White, Black, Navy, Gray, Beige) as universal wildcards.
  + *Pattern Balancing:* Max 1 patterned garment allowed per outfit; remaining slots must be solid.
* **Interaction Mechanics:**
  + Generates 3 cohesive combinations per query.
  + "Shuffle Item" swaps out an individual component (e.g., swapping a top) while locking the remaining items.
  + "Wear This Today" writes the chosen outfit ID to the user history log in MongoDB.
* **Acceptance Criteria:** Recommendation response latency remains below 3 seconds on an inventory of 100+ user items.

### **Feature 3: Strategic Purchase Recommender ("Wardrobe Multiplier")**

* **Catalog Ingestion:** Utilizes a static MongoDB catalog of standard foundational wardrobe essentials (e.g., white leather sneakers, navy trench coat, beige chinos).
* **Combinatorial Gap Algorithm:**
  + Iterates across catalog items not currently matched in the user's closet.
  + Runs the deterministic styling engine across hypothetical combinations containing the candidate item.
  + Computes the **Wardrobe Multiplier**:
* **Presentation Layer:** Renders recommendation cards displaying estimated price ranges, material durability ratings, multiplier metrics, and simulated outfit previews.
* **Out of Scope:** Native in-app checkout, payment gateways, and automated cart synchronization.
* **Acceptance Criteria:** Every recommended item card displays a valid multiplier calculation and links out via an external URL.

##

## **6. Non-Functional Requirements**

* **API Performance:** Read operations for cached wardrobes must resolve in <200ms. Background removal and tagging pipelines must complete within 6 seconds.
* **Resilience:** Graceful fallback to manual tag entry forms if third-party computer vision APIs fail or return low-confidence scores.
* **Security & Isolation:** Enforce JWT token verification across all Java backend endpoints. S3 read/write policies must enforce pre-signed URL constraints.
* **Data Hygiene:** Hard deletions of garments cascade to remove the corresponding image asset from AWS S3 and invalidate cached outfit pairings.

## **7. Phase roadmap**

| **Phase** | **Milestone Objective** | **Key Deliverables** |
| --- | --- | --- |
| **Sprint 1** | Foundation & Asset Pipeline | React Native camera integration, Java API scaffolding, AWS S3 image upload, third-party CV background stripping. |
| **Sprint 2** | Wardrobe Management & UI | Tag confirmation screens, MongoDB wardrobe CRUD, closet filter/search views. |
| **Sprint 3** | Deterministic Outfit Engine | OpenWeatherMap integration, color theory/layering rules matrix implementation, carousel UI with item shuffling. |
| **Sprint 4** | Gap Analysis & Static Catalog | Mock catalog database setup, combo multiplier scoring algorithm, recommendation card UI with affiliate links. |
| **Sprint 5** | Production Hardening & Polish | End-to-end regression testing, latency optimization, API error handling, project submission documentation. |