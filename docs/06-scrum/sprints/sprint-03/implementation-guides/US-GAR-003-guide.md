# US-GAR-003 — Add a Photo-Supported Garment with Manual Confirmation: Implementation Guide

| Field | Value |
| --- | --- |
| Story | [US-GAR-003](../../../stories/garment/US-GAR-003-add-photo-supported-garment.md) (Draft v0.4), parent PBI-012 |
| Sprint | Sprint 3, story 2 of 3 |
| Starting point | After every US-GAR-002 step is applied (V9, `Garment.revision`, PATCH, OpenAPI 0.9.0). No commit hash is assumed. |
| Approved decisions applied | S3-IMG-01 (strategy), S3-IMG-02, S3-IMG-03, S3-IMG-04, S3-IMG-05, S3-IMG-06 (investigation direction), S3-IMG-07, S3-IMG-08 (lifecycle) |
| Provisional choices used | OPEN-S3-01, 02, 07 (TTL 24 h, **blocked on PO**), 08 (libheif `heif-dec`), 09 (15,000,000 bytes), 10 (normalized JPEG only), 11 (multi-image HEIF rejected, **blocked on PO**), 12 (decoder ceilings), 13 (AWS SDK 2.46.13 + S3Mock 5.1.0), 14 (photo API surface) |
| Verification status | **Not compiled, not run, not tested.** The HEIF toolchain and the S3Mock fixture also need the verification procedures V1–V7 and V-S3 in [open questions](open-questions.md). |
| Shared rules | [shared-architecture.md](shared-architecture.md) §11 (errors), §15 (transactions), §16 (idempotency), §17 (media lifecycle), §19 (configuration), §21 (file order) |

## 1. Story Overview

**Story.** As an authenticated owner, I want to attach a permitted garment photo and confirm its information manually, so that the wardrobe is visually recognizable without depending on successful AI.

**Backend outcome:**

1. **Stage a photo.** The client uploads one photo to `POST /api/v1/wardrobe/photo-uploads`, using multipart with an `Idempotency-Key`. The backend:
   - checks that the bytes genuinely decode as JPEG, PNG or HEIC/HEIF;
   - checks the 15 MB and 512-pixel rules;
   - stores a private, metadata-free JPEG derivative;
   - returns a pending upload that expires.

   **No garment is created.**
2. **Review or cancel.** The client reviews the staged photo (`GET …/content`) and can cancel it (`DELETE`) or stage a replacement.
3. **Confirm.** The user explicitly confirms the garment through the **existing** `POST /api/v1/wardrobe/garments`, adding `photoUploadId`. One database transaction claims the upload, creates the garment and attaches the photo. Replay with the same `Idempotency-Key` never creates a second garment.
4. **Show the photo.** Garment detail shows `photo` metadata, and `GET /api/v1/wardrobe/garments/{id}/photo` streams it after the current owner check.
5. **Clean up.** Abandoned or cancelled uploads expire and their objects are deleted by a resumable in-process cleanup that can never delete an attached photo.

**Out of scope:**

- upload never creates a garment;
- a photo is not required for manual creation (imageless POST is unchanged);
- no attaching a photo to an existing garment;
- no replacing a confirmed photo;
- no AI analysis, background removal, bulk import, catalogue or recommendations;
- no measurement.

## 2. Repository Baseline

At the starting point, US-GAR-002 has been applied on top of `9d158fb`.

| Item | State | Relevance |
| --- | --- | --- |
| `Garment` | `(id, ownerAccountId, profile, provenance, createdAt, removedAt, revision)` + 6-argument constructor | Gains `Optional<GarmentPhoto> photo`; the 6- and 7-argument constructors stay |
| `AddGarmentService` | Creation idempotency (`addIfCreationKeyUnused`, `findCreation`, fingerprint) | Gains the photo-backed path inside one transaction |
| `AddGarmentCommand`, `GarmentCreationRequest` | 17 / 14 components, no media | + `photoUploadId` |
| `GarmentCreationFingerprint` | SHA-256 of the 13 accepted values | + `photo=<id>` **only when present**, so existing fingerprints are unchanged |
| `JdbcGarmentRepository` | `SELECT_GARMENT` reads the profile, sets and revision | + LEFT JOIN of the attached photo |
| Media infrastructure | None: no SDK, no storage, no decoder, no compose service | All new |
| Migrations | V1–V9 | V10 adds `wardrobe.wardrobe_photo_upload` |
| CORS | `GET, POST, PATCH` | + `DELETE` (cancel) |
| Contract test | Understands only JSON bodies and JSON responses | Must learn multipart requests and `image/jpeg` responses |
| CI | 75 required reports, `sprint_03` trigger | + libheif install, `CAPSULEAI_REQUIRE_HEIF=true`, new reports |

## 3. Original Business Requirements — Verbatim

From [BRD](../../../../01-business/BRD.md) (traceability BG-01, BR-001, BR-004, CAP-02, CAP-03):

| ID | Goal | Problem | Desired outcome | Capabilities |
| --- | --- | --- | --- | --- |
| BG-01 | Reduce Wardrobe Digitization Effort | Manual setup delays access to useful wardrobe advice. | Faster access to a trustworthy digital closet with less repetitive entry. | CAP-01–CAP-04 |

| ID | Requirement | Rationale | Priority | Goal |
| --- | --- | --- | --- | --- |
| BR-001 | Users must be able to create a digital representation of owned clothing with minimal manual effort through AI-assisted garment understanding. | Reduce setup friction and time to value. | Must | BG-01 |
| BR-004 | AI prediction failure or uncertainty must not prevent users from creating and maintaining wardrobe entries manually. | Preserve value when automation is unavailable or inaccurate. | Must | BG-01 |

| Capability | Name | Description |
| --- | --- | --- |
| CAP-02 | AI Digital Closet | Convert captured or selected garment images into usable wardrobe entries with less manual effort. |
| CAP-03 | Wardrobe Management | Keep the wardrobe current and discoverable through maintenance, browsing, search, and filtering. |

## 4. Original Product Requirements — Verbatim

From [PRD](../../../../02-product/PRD.md) §8.2 JRN-02:

> 1. From Today or Wardrobe, the user captures an image, selects one from the gallery, or chooses manual entry.
> 2. Image guidance helps avoid unclear garment photos; processing shows progress rather than a completed entry.
> …
> 5. With confirmed Category and one approved Color, save while omitting optional fields and preserving all supplied applicable values. Recommendation eligibility is not evaluated.
>
> **Recovery:** Poor images support replacement; AI failure supports retry or manual continuation. Low confidence never silently becomes authoritative data. Canceling a draft does not create a confirmed garment.

Steps 3–4 (processed preview and AI proposals) belong to US-GAR-004 and are not selected.

From PRD §12.1 FEAT-AI-001:

> **Product behavior:**
> 1. Offer capture and gallery selection with concise guidance about lighting, garment visibility, and avoiding confusing backgrounds.
> 2. Show the selected image and allow replacement/cancel before confirmation.
> …
> 5. Direct the user to review, retry, or manual continuation without creating a confirmed garment automatically.
>
> **Failure / fallback:** Explain capture/permission/image/analysis problems; allow another available image route or manual entry. An unusable processed preview must not block manual creation when the original image is useful.
>
> - **AC-02:** During processing, the product does not claim the garment has already been added.
> - **AC-03:** Denied camera access does not eliminate supported gallery or manual alternatives.
> - **AC-04:** An unsuitable image or processing failure offers replacement/retry and manual continuation.
> - **AC-05:** Canceling entry before confirmation leaves no confirmed wardrobe item.

From PRD §12.2 FEAT-AI-002:

> - **AC-03:** No AI proposal becomes authoritative or eligible as an owned item before confirmation.
> - **AC-06:** A failed save is not shown as a successful wardrobe addition; retry does not result in an unexplained duplicate.
> - **AC-07:** Confirmed category and dominant color allow saving without subtype, Brand, Price, material, an image for manual entry, any other optional applicable property.

## 5. Original Software Requirements — Verbatim

From [SRS](../../../../03-requirements/SRS.md):

| ID | Requirement | Verification |
| --- | --- | --- |
| FR-AI-001 | The system shall allow permitted camera garment input in JPEG/JPG, PNG, and HEIC/HEIF formats subject to Section 3.4.1 image limits. | T: supported capture inputs and size/dimension boundaries. |
| FR-AI-002 | The system shall allow permitted photo-library garment input in JPEG/JPG, PNG, and HEIC/HEIF subject to the image limits in Section 3.4.1, without requiring a plain/white background. | T: all formats; valid non-plain backgrounds and limit boundaries. |
| FR-AI-003 | The system shall provide manual garment entry without requiring an image or successful automated analysis. | T: imageless/manual completion. |
| FR-AI-004 | The system shall allow replacement or cancellation of the selected image before garment confirmation. | T: replace/cancel selected image. |
| FR-AI-008 | The system shall create a confirmed wardrobe entry only after explicit user confirmation of the saveable minimum. | T: confirm versus draft. |
| FR-AI-009 | The system shall leave no confirmed entry from canceled preconfirmation work. | T: canceled draft absent. |
| FR-AI-010 | The system shall acknowledge a successful addition distinctly from pending/failed work without manufacturing duplicate entries on retry. | T: successful/failed-save retry. |
| FR-AI-011 | The system shall provide capture/selection guidance about lighting, garment visibility, and avoiding confusing backgrounds. | I/D: image-entry guidance. |
| FR-AI-012 | The system shall validate garment-image input against supported formats, maximum 15 MB file size, and shortest dimension of at least 512 pixels, explaining unsupported/out-of-limit input with replacement or manual continuation. | T: JPEG/JPG, PNG, HEIC/HEIF; at/beyond 15 MB; 511/512 pixels; fallback. |
| DATA-GAR-004 | The system shall associate available image/preview information with the appropriate draft or garment without requiring an image for manually saved entries. | T: imageless and image entry. |
| DATA-INT-004 | The system shall keep failed/pending mutations distinct from accepted state, preventing duplicates of the same logical action without collapsing separate explicit Wear intentions. | T: interrupted additions/Wear actions. |
| DATA-RET-002 | The system shall retain required active-account profile/wardrobe/operational information and meaningful minimal history while the account is active and it remains necessary, while a removed garment's original image and nonessential personal data are physically deleted within 30 days. | T/I |
| HW-001 | The system shall request camera access for the selected capture action and offer other permitted image routes or manual entry when unavailable/denied. | T: camera denial alternatives. |
| HW-002 | The system shall use a permitted photo-library selection without requiring camera access or successful analysis for manual entry. | T: library/manual alternatives. |
| ERR-AI-001 | Input violates image limits/format support, is unsuitable, or analysis cannot complete. → The system shall explain unsupported formats, file-size/dimension violations, unsuitable images or unavailable/failed analysis with replacement/manual/retry recovery while preserving confirmed profiles. Recovery: Manual entry remains usable; no garment is added from an unconfirmed failed attempt. | |
| COM-002 | The system shall distinguish interrupted communication from accepted save, access/recovery or Wear action outcomes, applying Section 9 recovery without false completion or duplicated logical actions. | T |

Excerpts from NFR-PRIV-001 and NFR-PRIV-002:

- **NFR-PRIV-001:** "The system shall limit personal data to necessary functionality … respecting omission/correction/removal/permission controls."
- **NFR-PRIV-002:** "The MVP shall not use personal wardrobe images, profiles, Wear Events, feedback or corrections for AI model training/improvement."

FR-GAR-001–013 (FR-GAR-005 superseded) apply to the confirmed profile exactly as in creation. They are quoted in the US-GAR-002 guide §5.

## 6. Original Business Rules — Verbatim

> **BRULE-GAR-001 — Canonical Garment Authority.** Each owned garment belongs to the authorized user's wardrobe. Automated attribute proposals are not authoritative. Applicable User Confirmed, User Corrected, or User Entered-and-confirmed values establish the canonical profile; later automated analysis cannot silently overwrite them.

> **BRULE-GAR-002 — Saveable Minimum.** A garment is Saveable when the user explicitly confirms its supported Primary Category and Dominant Color. Manual creation requires neither an image nor successful analysis. Missing optional rich descriptors do not prevent saving; canceled or unconfirmed work does not establish a confirmed wardrobe entry.

> **BRULE-AUTH-001 — Authorized Personal Operations.** Personal wardrobe, profile, feedback, history, and assessment operations require authentication and authorization for the affected user's information. Unavailable authorized access is not an empty wardrobe and does not change personal state.

> **BRULE-AUTH-007 — Purpose-Limited Private Data Use** (excerpt). Personal data is used only as necessary for CapsuleAI functionality … MVP personal images, profiles, Wear Events, feedback and corrections are not used for AI model training/improvement. … This never authorizes private inventory, source-image, purchase-information, profile or history access.

> **BRULE-GAR-010** (excerpt). … Price must not be inferred from a photograph; any future Brand suggestion remains unconfirmed until user acceptance.

BRULE-GAR-003–006 apply unchanged to the confirmed profile (see the US-GAR-002 guide §6).

## 7. Original User Story and Acceptance Criteria — Verbatim

> As an authenticated owner, I want to attach a permitted garment photo and confirm its information manually, so that the wardrobe is visually recognizable without depending on successful AI.

> **AC-01 — Choose a permitted route.** Offer capture or photo-library selection with lighting/visibility guidance. Camera denial/unavailability does not remove permitted library or imageless manual entry; library input does not require camera permission.
> The optional-property and classification rules in Section 3 apply to this accepted outcome, including preservation of valid supplied values and correction/clearing of invalid category/subtype or incomplete Price pairs.

> **AC-02 — Validate supported input.** Accept supported JPEG/JPG, PNG and HEIC/HEIF at or below 15 MB with shortest dimension at least 512 pixels. Explain unsupported or out-of-limit input, including 511-pixel and above-15-MB cases, with replacement/manual continuation. A non-plain background alone does not invalidate input.

> **AC-03 — Review or cancel selection.** Show the selected input, allow replacement or cancellation before confirmation, and keep image/proposal work distinct from a confirmed wardrobe addition.

> **AC-04 — Confirm a useful garment.** After explicit confirmation of the saveable category/dominant-color minimum, associate the available image with the correct owned garment. An image alone creates no confirmed item; richer fields and AI success are not required to save.

> **AC-05 — Preserve authority and unevaluated eligibility.** The confirmed profile remains authoritative, with optional unknown information absent. Materials and Styles support distinct multiple values; all supplied category-valid properties, optional Brand and original purchase Price are preserved under the approved validation. Missing optional information cannot label an accepted garment unready, invalid, unsuitable or ineligible. Preserve the existing `recommendationEligibility: NOT_EVALUATED` contract; no recommendation engine or AI extraction is added.

> **AC-06 — Recover safely.** Canceled preconfirmation work creates no confirmed item. Known failed saves are not success; interrupted/uncertain acceptance permits review and safe same-action retry without duplicate accepted additions. Missing media does not hide otherwise available accepted garment information.

> **AC-07 — Protect private media.** Only authorized ownership/session access permits the relevant image and garment operations; denied access creates no wardrobe mutation or private disclosure. Optional measurement failure does not reverse accepted creation.

Story §8 Out of Scope: "Imageless manual entry is US-GAR-001; processed preview/proposals and confidence are US-GAR-004. No upload-only product outcome, mandatory white background, new format, AR/3D or image-delivery design is added."

## 8. Original Use Cases, Flows and Architecture Decisions — Verbatim

From [UC-007](../../../../03-requirements/use-cases/UC-007-add-garment.md) §6 Main Success Scenario (the manual-confirmation slice; step 4's analysis is not selected):

> 1. The User requests a new garment entry.
> 2. CapsuleAI offers permitted camera/gallery input or imageless manual entry, with guidance on lighting, visibility and confusing backgrounds.
> 3. The User chooses image assistance, captures or selects a permitted image, and submits it.
> 4. CapsuleAI validates the image, displays pending analysis, then presents an available usable processed preview and reviewable attribute proposals with understandable confidence/provenance.
> 5. The User reviews the proposed information, corrects it when necessary, and supplies missing information needed for the intended profile.
> 6. CapsuleAI presents all supplied applicable property values, validates source vocabulary and category applicability (including distinct Materials and one dominant Color), and checks confirmation of the Category/Color minimum. Invalid/inapplicable input or an incomplete Price pair returns for correction or clearing; optional omissions remain valid.
> 7. The User explicitly confirms the supported Primary Category and Dominant Color and submits the garment profile.
> 8. CapsuleAI acknowledges one accepted owned garment with every supplied valid applicable value preserved for rereading; recommendation eligibility is not evaluated.

> **A2 — Camera/gallery choice or image replacement.** At Main Step 3: 1. The User uses an available permitted image route, declines camera access in favor of gallery/manual entry, or replaces the selected image before confirmation. 2. CapsuleAI keeps the work unconfirmed; resume at Main Step 3 for a replacement image or take the manual alternative.
>
> **A5 — Cancel before confirmation.** At Main Step 7: 1. The User cancels preconfirmation work. 2. CapsuleAI creates no confirmed garment from the draft and leaves existing wardrobe information unchanged.
>
> **E1 — Invalid or unsuitable image.** At Main Step 4: 1. CapsuleAI explains an unsupported format, file over 15 MB, shortest dimension below 512 pixels or unsuitable image without adding a garment. 2. The User supplies a replacement and resumes at Main Step 3, or takes the imageless manual alternative.
>
> **E4 — Save failed or outcome uncertain.** At Main Step 8: 1. CapsuleAI distinguishes a known failed save from a pending/unconfirmed outcome and preserves useful entered work where practical. 2. The User reviews whether creation was accepted before retrying; one accepted addition cannot become unexplained duplicate entries.
>
> **E5 — Authorized access becomes unusable.** At Main Step 1: 1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again. 2. … Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

Architecture:

- **ADA-007:** private object storage with application-controlled delivery.
- **ADA-009/012:** database-coordinated logical actions and recoverable coordination; no broker.
- **ADA-006/013/014:** a narrow external adapter with bounded timeouts.
- **AR-01–08.**

The approved Sprint 3 decisions are quoted in full in [plan §9.2](../sprint-plan.md#92-us-gar-003-decisions):

| ID | Recommended solution (verbatim) | Status |
| --- | --- | --- |
| S3-IMG-01 | Option A — isolated S3-compatible local development/testing, with private managed Amazon S3 as the proposed deployed target. | APPROVED (strategy; AWS configuration, deployment, compatibility and budget unverified) |
| S3-IMG-02 | Option A — proxy bounded multipart photo uploads through the existing authenticated application. | APPROVED |
| S3-IMG-03 | Option B — staged upload with separately confirmed new-garment creation. | APPROVED |
| S3-IMG-04 | Option A — durable pending media coordination and resumable owner-controlled recovery. | APPROVED |
| S3-IMG-05 | Option A — authenticated application delivery, rechecking the current owner and permitted pending/accepted media association for each request. | APPROVED |
| S3-IMG-06 | Option B as the investigation direction — prove a bounded decoder adapter using a maintained HEIF-capable toolchain; finalize the concrete library/runtime in selected-story JIT design. | APPROVED (investigation direction only) |
| S3-IMG-07 | Option A — preserve new-garment-only photo support. | APPROVED |
| S3-IMG-08 | Option A — recoverable application-owned lifecycle; request PO selection of the pending retention/expiry policy before the affected lifecycle is finalized. | APPROVED (TTL not selected) |

## 9. Engineering Interpretation

| # | Interpretation | Source |
| --- | --- | --- |
| I-1 | Camera/library choice, permissions, guidance (FR-AI-011, HW-001/002) and preview display are **client** responsibilities. The backend only receives bytes and never sees whether they came from a camera. | AC-01, AC-03 |
| I-2 | Format is decided by **content sniffing and a genuine decode**, never by filename or client `Content-Type`. JPEG (`FF D8 FF`), PNG (8-byte signature), HEIF (`ftyp` box with an HEVC brand). Anything else is `PHOTO_UNSUPPORTED_FORMAT`. AVIF and other ISO-BMFF images are not approved formats. | AC-02, FR-AI-012, S3-IMG-06 |
| I-3 | Size: the received byte count must be ≤ 15,000,000 (OPEN-S3-09). Dimensions: the shortest side of the **original** image must be ≥ 512 pixels (511 rejected, 512 accepted). Background colour is never checked. | AC-02 |
| I-4 | Stored media is one normalized JPEG: orientation applied, all metadata dropped, long edge ≤ 2048 px (OPEN-S3-10). The original bytes are discarded after inspection. | S3-IMG-06, NFR-PRIV-001 |
| I-5 | A staged upload is **not** a garment. Garment creation still needs the existing confirmed POST with Category and Color. An upload that cannot be used makes the POST fail; it never falls back to an imageless success. | AC-04, S3-IMG-03 |
| I-6 | Replacement = stage another photo (new upload key) and optionally `DELETE` the old one. Unconfirmed uploads expire after the pending TTL (OPEN-S3-07, provisional 24 h). | AC-03, FR-AI-004, S3-IMG-08 |
| I-7 | Creation idempotency is unchanged for imageless POSTs. For photo POSTs, the fingerprint includes the upload id, so the same key with a different (or no) photo is `422 IDEMPOTENCY_KEY_REUSED`. Replay returns `200` with the existing garment even though the upload is now `ATTACHED`. | AC-06, S3-IMG-04, DATA-INT-004 |
| I-8 | One upload can attach to at most one garment (`UNIQUE (garment_id)` and the status transition `READY → ATTACHED` under a row lock). | S3-IMG-03 |
| I-9 | Missing media: if an attached photo's object is gone, only the photo route answers `404 GARMENT_PHOTO_UNAVAILABLE`. Garment JSON (including `photo` metadata) stays readable. | AC-06 |
| I-10 | Every media route rechecks the current session and owner. Unknown, other-owner and malformed upload ids give the same `404`. Responses are `private, no-store`. No presigned or public URL exists. | AC-07, S3-IMG-05 |
| I-11 | Upload staging is idempotent per `(owner, Idempotency-Key)` with a SHA-256 of the bytes: a retry after a lost response returns the same upload; different bytes under the same key are rejected. | S3-IMG-02 ("dropped upload response is resolved through owner-scoped pending status/retry") |
| I-12 | "Optional measurement failure does not reverse accepted creation" (AC-07) is satisfied trivially: no measurement producer exists in Sprint 3. Nothing is added. | Plan §5 |
| I-13 | Editing (US-GAR-002) never changes the photo association. There are no attach or replace routes. | S3-IMG-07 |

## 10. Just-in-Time Detailed Design

The lifecycle state diagram, staging sequence and transaction table are in [shared architecture §15 and §17](shared-architecture.md#17-private-media-architecture-us-gar-003). This section adds the story-specific detail.

### 10.1 Confirmation and recovery sequence

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant G as AddGarmentService
    participant DB as PostgreSQL
    C->>G: POST /garments {confirmed, category, color, photoUploadId} + Idempotency-Key K
    G->>G: validate entry (incl. photoUploadId format); fingerprint(profile, photo)
    G->>DB: BEGIN
    G->>DB: SELECT upload WHERE id AND owner FOR UPDATE
    alt upload missing / other owner
        G-->>C: 404 PHOTO_UPLOAD_NOT_FOUND (rollback)
    end
    G->>DB: findCreation(owner, K)
    alt K already used
        G-->>C: same fingerprint → 200 existing garment; different → 422 IDEMPOTENCY_KEY_REUSED
    end
    alt upload not READY or expired
        G-->>C: 409 PHOTO_UPLOAD_NOT_USABLE (rollback)
    end
    G->>DB: INSERT garment + sets (ON CONFLICT DO NOTHING)
    G->>DB: UPDATE upload SET status=ATTACHED, garment_id WHERE status=READY
    G->>DB: COMMIT
    G-->>C: 201 Garment {photo: {contentType, width, height}}
    Note over C,G: Lost response → repeat the same POST with K → 200 with the same garment
```

### 10.2 API contract

| Operation | Request | Success | Errors |
| --- | --- | --- | --- |
| `POST /api/v1/wardrobe/photo-uploads` | `multipart/form-data`, part `photo` (the file); header `Idempotency-Key` (UUID) | `201` new / `200` replay: `PhotoUpload` | `400 VALIDATION_FAILED` (`photo` or `Idempotency-Key` missing/invalid), `400 MALFORMED_REQUEST` (not multipart), `401`, `409 PHOTO_UPLOAD_NOT_USABLE` (cancelled or expired while uploading), `422 PHOTO_*` or `PHOTO_UPLOAD_KEY_REUSED`, `503` |
| `GET /api/v1/wardrobe/photo-uploads/{photoUploadId}` | — | `200 PhotoUpload` | `401`, `404 PHOTO_UPLOAD_NOT_FOUND`, `503` |
| `GET /api/v1/wardrobe/photo-uploads/{photoUploadId}/content` | Send `Accept: image/jpeg, application/problem+json` | `200 image/jpeg` | `401`, `404`, `409 PHOTO_UPLOAD_NOT_USABLE` (not `READY`), `503` |
| `DELETE /api/v1/wardrobe/photo-uploads/{photoUploadId}` | — | `204` (also when already cancelled or expired) | `401`, `404`, `409 PHOTO_UPLOAD_NOT_USABLE` (already attached), `503` |
| `POST /api/v1/wardrobe/garments` | Existing body + optional `photoUploadId` | `201`/`200 Garment` with `photo` | Existing errors + `400` (`photoUploadId: INVALID_FORMAT`), `404 PHOTO_UPLOAD_NOT_FOUND`, `409 PHOTO_UPLOAD_NOT_USABLE` |
| `GET /api/v1/wardrobe/garments/{garmentId}/photo` | — | `200 image/jpeg` | `401`, `404 GARMENT_NOT_FOUND`, `404 GARMENT_PHOTO_UNAVAILABLE`, `410`, `503` |

`PhotoUpload` (every field always present):

```json
{
  "id": "0f8e…",
  "status": "READY",
  "contentType": "image/jpeg",
  "width": 1536,
  "height": 2048,
  "expiresAt": "2026-11-12T08:00:00Z",
  "garmentId": null
}
```

- `status` is one of `RECEIVING`, `READY`, `ATTACHED`, `DISCARDED`.
- `contentType`, `width` and `height` are `null` until `READY`.
- `garmentId` is set only when `ATTACHED`.

`Garment.photo` is `null`, or:

```json
{ "contentType": "image/jpeg", "width": 1536, "height": 2048 }
```

The overview summary is unchanged (OPEN-S3-14).

### 10.3 SQL

The complete V10 is in Step P2. Key statements (all in `JdbcPhotoUploadRepository`):

| Purpose | Statement (abridged) |
| --- | --- |
| Intent | `INSERT … ON CONFLICT (owner_account_id, upload_key) DO NOTHING` |
| Ready | `UPDATE … SET status='READY', content_type, width, height, ready_at WHERE id=:id AND status='RECEIVING'` |
| Failed put | `UPDATE … SET status='DISCARDED', discarded_at=:now, next_cleanup_at=GREATEST(:now, created_at + :fence) WHERE id=:id AND status='RECEIVING'` |
| Late-write requeue | `UPDATE … SET object_deleted_at=NULL, next_cleanup_at=:now WHERE id=:id AND status='DISCARDED'` |
| Lock for confirmation | `SELECT … WHERE id=:id AND owner_account_id=:owner FOR UPDATE` |
| Attach | `UPDATE … SET status='ATTACHED', garment_id=:garmentId, attached_at=:now WHERE id=:id AND status='READY'` |
| Cancel | `UPDATE … SET status='DISCARDED', … WHERE id=:id AND owner_account_id=:owner AND status IN ('RECEIVING','READY')` |
| Expire batch | `UPDATE … WHERE id IN (SELECT id … WHERE status IN ('RECEIVING','READY') AND expires_at <= :now ORDER BY expires_at LIMIT :n FOR UPDATE SKIP LOCKED) AND status IN ('RECEIVING','READY')` |
| Claim cleanup | `UPDATE … SET next_cleanup_at=:retryAt, cleanup_attempts=cleanup_attempts+1 WHERE id IN (SELECT … status='DISCARDED' AND object_deleted_at IS NULL AND next_cleanup_at <= :now … FOR UPDATE SKIP LOCKED) RETURNING id, object_key` |
| Deleted | `UPDATE … SET object_deleted_at=:now WHERE id=:id AND status='DISCARDED' AND object_deleted_at IS NULL` |

The garment read gains:

```sql
LEFT JOIN wardrobe.wardrobe_photo_upload p ON p.garment_id = g.id AND p.status = 'ATTACHED'
```

### 10.4 Transactions and concurrency

| Race | Resolution |
| --- | --- |
| Two confirmations of one upload (same or different keys) | Both lock the upload row. The second sees `ATTACHED`: with the same key and fingerprint it replays; with a different key it gets `409 PHOTO_UPLOAD_NOT_USABLE`. |
| Confirmation versus expiry sweep | The sweep uses `FOR UPDATE SKIP LOCKED` and re-checks status, so it skips a row a confirmation holds. A confirmation that locks after the sweep committed sees `DISCARDED` and gets `409`. |
| Cancel versus confirmation | Same row lock. Whichever commits first wins; the other is explained (`409` for either). |
| Upload still running when cancelled or expired | `next_cleanup_at ≥ created_at + in-flight fence` (`PT5M` > storage timeout `PT30S` + decode timeout `PT20S`). If the late put lands anyway, its failing `READY` update re-queues deletion. |
| Two uploads with the same key at once | `UNIQUE (owner_account_id, upload_key)`. The loser rereads, compares the SHA-256 and replays or rejects. The loser never stored an object (it puts only after its own intent insert). |
| Two cleanup workers | `SKIP LOCKED` claims plus a lease (`next_cleanup_at = now + retry-delay`). Deleting twice is harmless (S3 delete is idempotent). |

### 10.5 Security

- **Authorization:** every route is behind the existing filter chain; ownership is in every SQL predicate.
- **Response headers:** `Cache-Control: private, no-store` and `X-Content-Type-Options: nosniff` on image responses.
- **Temporary files:** a decoder temp directory is created per decode and deleted in `finally`.
- **Process safety:** the decoder process gets fixed arguments (no shell) and its output is discarded. No filename from the client is ever used.
- **Credentials:** storage credentials come only from environment variables; the default AWS credential chain applies when they are absent. Credentials and object keys are never logged.

### 10.6 Recovery matrix

| Interruption point | State | Recovery |
| --- | --- | --- |
| Before the intent insert | Nothing durable | Retry with the same key → normal staging |
| After the intent, before the put completes (crash) | `RECEIVING`, maybe an object | A retry with the same key returns `RECEIVING` (`200`). Expiry later discards it, and cleanup deletes any object after the fence. |
| Put failed | `DISCARDED`, cleanup queued | `503`; the client stages again with a **new** key (a replay of the old key returns `DISCARDED`) |
| After `READY`, response lost | `READY` | Retry with the same key → `200` with the same upload |
| Confirmation committed, response lost | `ATTACHED`, garment exists | Retry the same POST and key → `200` with the same garment |
| Confirmation rolled back (DB failure) | `READY` unchanged | `503`; retry the same POST → `201` |
| Attached object deleted externally | `ATTACHED`, object missing | Photo route `404 GARMENT_PHOTO_UNAVAILABLE`; garment unaffected |

### 10.7 Configuration

See [shared architecture §19](shared-architecture.md#19-configuration-and-environment-variables) for every property. Local development uses the S3Mock service added to `compose.yaml` and `CAPSULE_PHOTO_*` environment variables. No real credential appears in any file.

### 10.8 Testing plan

| Level | Class | Focus |
| --- | --- | --- |
| Domain | `PhotoUploadTest` | Status invariants, usability, key format |
| Application | `StagePhotoUploadServiceTest` | Size boundary, 511/512, replay, key reuse, storage failure, cancelled while uploading |
| Application | `PhotoBackedGarmentCreationTest` | Confirm, replay, key reuse, not-usable, other owner, imageless path unchanged |
| Application | `PhotoUploadLifecycleServiceTest` | Cancel, idempotent cancel, attached not cancellable, expiry, cleanup retry, attached never deleted |
| Imaging | `ImageIoPhotoInspectorTest` | JPEG/PNG genuine decode, suffix and Content-Type mismatch, truncation, orientation, metadata stripping, long-edge bound, pixel ceiling |
| Imaging | `HeifDecodingIT` | Real libheif: HEIC decode, multi-image rejection, truncated input |
| Storage | `S3PhotoStorageIT` | Put/get/delete against S3Mock, missing object, idempotent delete, conditional write (V-S3) |
| Schema | `PhotoUploadSchemaIT` | V10 constraints |
| API | `PhotoUploadApiIT` | Staging boundaries and errors, review content, cancel, privacy, logout |
| API | `PhotoGarmentApiIT` | Confirmation, replay, detail photo, photo route, missing object, imageless regression |
| Concurrency | `ConcurrentPhotoConfirmationIT` | Same-upload confirmations, sweep versus confirmation |
| Lifecycle | `PhotoUploadCleanupIT` | Expiry, fence, delete failure and retry, attached untouched |
| Contract | `IdentityOpenApiContractIT` (modified) | Multipart and binary operations documented and protected |
| Security | `CorsSecurityIT` (modified) | `DELETE` preflight |

## 11. File Change Matrix

`…/wardrobe` = `backend/src/main/java/com/capsuleai/wardrobe`.

| Order | Action | Exact Repository Path | Purpose | Dependencies |
| --- | --- | --- | --- | --- |
| P1 | MODIFY | `backend/pom.xml` | AWS SDK v2 BOM + `s3` | — |
| P2 | ADD | `backend/src/main/resources/db/migration/V10__create_wardrobe_photo_upload.sql` | Pending media table | V9 |
| P3 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/domain/GarmentPhoto.java` | Photo metadata | — |
| P4 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/domain/Garment.java` | `photo` component | P3 |
| P5 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/domain/PhotoUploadStatus.java` | Lifecycle states | — |
| P6 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/domain/PhotoRejectionReason.java` | Rejection reasons | — |
| P7 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/domain/PhotoUpload.java` | Upload aggregate | P5 |
| P8 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ReceivedPhoto.java` | Uploaded bytes abstraction | — |
| P9 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoRejectedException.java` | `422` | P6 |
| P10 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadNotFoundException.java` | `404` | — |
| P11 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadNotUsableException.java` | `409` | — |
| P12 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadKeyReusedException.java` | `422` | — |
| P13 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentPhotoUnavailableException.java` | `404` | — |
| P14 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoStorageUnavailableException.java` | `503` | — |
| P15 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoProcessingUnavailableException.java` | `503` | — |
| P16 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/StoredPhoto.java` | Stream + length | — |
| P17 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoStorage.java` | Storage port | P14, P16 |
| P18 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/InspectedPhoto.java` | Inspection result | — |
| P19 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoInspector.java` | Decoder port | P8, P18 |
| P20 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoCleanupTarget.java` | Claimed cleanup row | — |
| P21 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoUploadRepository.java` | Persistence port | P7, P20 |
| P22 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadPolicy.java` | Policy values | — |
| P23 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/StagePhotoCommand.java` | Command | P8 |
| P24 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/StagedPhoto.java` | Outcome | P7 |
| P25 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/StagePhotoUploadService.java` | Staging use case | P8–P24 |
| P26 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/ViewPhotoUploadService.java` | Status and pending content | P21, P17 |
| P27 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/CancelPhotoUploadService.java` | Cancel | P21 |
| P28 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentPhotoContentService.java` | Attached photo stream | P21, P17 |
| P29 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadCleanupService.java` | Expiry + deletion | P21, P17 |
| P30 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/application/AddGarmentCommand.java` | `photoUploadId` | — |
| P31 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentEntryValidator.java` | `photoUploadId` format | P30 |
| P32 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentCreationFingerprint.java` | Photo in fingerprint | — |
| P33 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/application/AddGarmentService.java` | Photo-backed creation | P21, P31, P32 |
| P34 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/persistence/JdbcGarmentRepository.java` | Photo join | P2, P4 |
| P35 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/persistence/JdbcPhotoUploadRepository.java` | SQL | P2, P21 |
| P36 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/storage/S3PhotoStorage.java` | S3 adapter | P1, P17 |
| P37 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/imaging/JpegOrientation.java` | EXIF orientation | — |
| P38 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/imaging/HeifDecoder.java` | Bounded `heif-dec` process | P9, P15 |
| P39 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/imaging/ImageIoPhotoInspector.java` | Sniff, decode, normalize | P37, P38, P19 |
| P40 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/MultipartReceivedPhoto.java` | `MultipartFile` → `ReceivedPhoto` | P8 |
| P41 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/PhotoUploadResponse.java` | JSON | P7 |
| P42 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/GarmentPhotoResponse.java` | JSON | P3 |
| P43 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/PhotoResponses.java` | Image response helper | P16 |
| P44 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/GarmentResponse.java` | `photo` | P42 |
| P45 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/GarmentCreationRequest.java` | `photoUploadId` | P30 |
| P46 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/WardrobePhotoUploadController.java` | Upload routes | P25–P27, P40–P43 |
| P47 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/WardrobeGarmentController.java` | Garment photo route | P28, P43 |
| P48 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/WardrobeApiExceptionHandler.java` | Photo errors | P9–P15 |
| P49 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/scheduling/PhotoUploadCleanupJob.java` | Scheduled cleanup | P29 |
| P50 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobePhotoProperties.java` | `capsuleai.wardrobe.photo*` | — |
| P51 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobePhotoConfiguration.java` | Media beans | P35–P39, P50 |
| P52 | ADD | `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobePhotoCleanupConfiguration.java` | Scheduling switch | P49 |
| P53 | MODIFY | `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobeConfiguration.java` | `AddGarmentService` wiring | P33, P51 |
| P54 | MODIFY | `backend/src/main/java/com/capsuleai/identity/configuration/IdentitySecurityConfiguration.java` | CORS `DELETE` | — |
| P55 | MODIFY | `backend/src/main/resources/application.properties` | Multipart, photo, storage | P50 |
| P56 | MODIFY | `backend/src/main/resources/application-local.properties` | Local S3Mock | P55 |
| P57 | MODIFY | `compose.yaml` | S3Mock service | — |
| P58 | MODIFY | `docs/05-api/openapi.yaml` | 0.10.0 | P41–P48 |
| P59 | MODIFY | `.github/workflows/backend-ci.yml` | libheif, env, reports | T-steps |
| P60 | MODIFY | `backend/README.md` | Photos section | — |
| T1 | MODIFY | `backend/src/test/resources/application-test.properties` | Test storage and cleanup | P55 |
| T2 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/support/FaultInjectingPhotoStorage.java` | In-memory storage with faults | P17 |
| T3 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/support/TestImages.java` | Synthetic images | — |
| T4 | MODIFY | `backend/src/test/java/com/capsuleai/identity/support/IdentityTestConfiguration.java` | `@Primary` test storage | T2 |
| T5 | MODIFY | `backend/src/test/java/com/capsuleai/wardrobe/support/WardrobeIntegrationTestSupport.java` | Truncate, photo helpers | T2, T3 |
| T6 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/application/InMemoryPhotoUploadRepository.java` | Fake repository | P21 |
| T7 | MODIFY | `backend/src/test/java/com/capsuleai/wardrobe/application/AddGarmentServiceTest.java` | New constructor | P33, T6 |
| T8 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/domain/PhotoUploadTest.java` | Domain | P7 |
| T9 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/application/StagePhotoUploadServiceTest.java` | Staging | P25 |
| T10 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/application/PhotoBackedGarmentCreationTest.java` | Confirmation | P33 |
| T11 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/application/PhotoUploadLifecycleServiceTest.java` | Cancel, cleanup | P27, P29 |
| T12 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/imaging/ImageIoPhotoInspectorTest.java` | Imaging | P39 |
| T13 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/imaging/HeifDecodingIT.java` | Real libheif | P38, P39 |
| T14 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/storage/S3PhotoStorageIT.java` | Real S3 protocol | P36 |
| T15 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/persistence/PhotoUploadSchemaIT.java` | V10 | P2 |
| T16 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/in/web/PhotoUploadApiIT.java` | Upload API | P46 |
| T17 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/in/web/PhotoGarmentApiIT.java` | Confirmation API | P47 |
| T18 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/application/ConcurrentPhotoConfirmationIT.java` | Races | P33, P35 |
| T19 | ADD | `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/persistence/PhotoUploadCleanupIT.java` | Lifecycle on PostgreSQL | P29, P35 |
| T20 | MODIFY | `backend/src/test/java/com/capsuleai/identity/adapter/in/web/IdentityOpenApiContractIT.java` | Multipart and binary | P58 |
| T21 | MODIFY | `backend/src/test/java/com/capsuleai/identity/adapter/in/security/CorsSecurityIT.java` | `DELETE` | P54 |

## 12. Complete Production Implementation

The tree compiles again after P53. T1–T7 are needed before any test runs: they cover the test properties, the test storage bean, the support changes and the changed `AddGarmentService` constructor.

### Step P1 — [MODIFY] `backend/pom.xml`

BEFORE:

```xml
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
```

AFTER:

```xml
    <properties>
        <java.version>21</java.version>
        <!-- US-GAR-003 (OPEN-S3-13): AWS SDK for Java v2, pinned through its BOM. Re-verify on upgrade. -->
        <aws.sdk.version>2.46.13</aws.sdk.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>${aws.sdk.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
```

BEFORE:

```xml
        <dependency>
            <groupId>com.tngtech.archunit</groupId>
```

AFTER:

```xml
        <!-- US-GAR-003 (S3-IMG-01): private photo storage through the S3 protocol. Only the synchronous
             client is used, so the Netty-based asynchronous HTTP client is excluded. -->
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>s3</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>software.amazon.awssdk</groupId>
                    <artifactId>netty-nio-client</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>com.tngtech.archunit</groupId>
```

**Explanation.**

- **Version.** `2.46.13` exists on Maven Central and is older than two weeks. It includes `PutObjectRequest.ifNoneMatch` (conditional writes were added in 2024).
- **Dependency management.** Spring Boot's parent dependency management still applies; the BOM import only adds AWS versions.
- **HTTP client.** The synchronous client uses the Apache HTTP client that `s3` brings by default.

**Common mistake.** Do not add `spring-cloud-aws` or another starter; the adapter configures the client explicitly.

**Verification.** Run `.\mvnw.cmd -q dependency:tree '-Dincludes=software.amazon.awssdk'` (read-only) and confirm there is no `netty-nio-client`.

### Step P2 — [ADD] `V10__create_wardrobe_photo_upload.sql`

**Path:** `backend/src/main/resources/db/migration/V10__create_wardrobe_photo_upload.sql`

```sql
-- US-GAR-003 (S3-IMG-03/04/08): private staged garment photos and their recoverable lifecycle.
-- A row is written before any object is stored, so an object created by a crashed upload is
-- always discoverable and deletable. A staged photo is NOT a garment: only a separately confirmed
-- garment creation moves a READY upload to ATTACHED, in the same transaction that creates the garment.
--
--   RECEIVING  intent recorded; the normalized photo is being stored
--   READY      stored and reviewable; may be confirmed until expires_at
--   ATTACHED   belongs to exactly one garment (garment_id); never expired or cleaned up
--   DISCARDED  cancelled, expired or failed; its object is deleted when next_cleanup_at is due,
--              and object_deleted_at records that deletion was confirmed
--
-- object_key is opaque, generated by the server, unique and never derived from bytes, file names
-- or garment values. owner_account_id is the authenticated Identity account id, with no foreign
-- key into Identity (ADA-016, AR-04).
--
-- Owner: Wardrobe. Every object below is in the wardrobe schema (AR-01).
CREATE TABLE wardrobe.wardrobe_photo_upload (
    id                UUID        PRIMARY KEY,
    owner_account_id  UUID        NOT NULL,
    upload_key        UUID        NOT NULL,
    content_sha256    TEXT        NOT NULL,
    object_key        TEXT        NOT NULL,
    status            TEXT        NOT NULL,
    content_type      TEXT        NULL,
    width             INTEGER     NULL,
    height            INTEGER     NULL,
    garment_id        UUID        NULL REFERENCES wardrobe.wardrobe_garment (id),
    created_at        TIMESTAMPTZ NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    ready_at          TIMESTAMPTZ NULL,
    attached_at       TIMESTAMPTZ NULL,
    discarded_at      TIMESTAMPTZ NULL,
    next_cleanup_at   TIMESTAMPTZ NULL,
    cleanup_attempts  INTEGER     NOT NULL DEFAULT 0,
    object_deleted_at TIMESTAMPTZ NULL,

    -- One upload per logical staging action (S3-IMG-02 retry safety).
    CONSTRAINT uq_wardrobe_photo_upload_key UNIQUE (owner_account_id, upload_key),
    CONSTRAINT uq_wardrobe_photo_upload_object_key UNIQUE (object_key),
    -- A garment has at most one photo, and an upload belongs to at most one garment (S3-IMG-03).
    CONSTRAINT uq_wardrobe_photo_upload_garment UNIQUE (garment_id),

    CONSTRAINT ck_wardrobe_photo_upload_status
        CHECK (status IN ('RECEIVING', 'READY', 'ATTACHED', 'DISCARDED')),
    CONSTRAINT ck_wardrobe_photo_upload_sha256 CHECK (content_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_wardrobe_photo_upload_object_key CHECK (object_key ~ '^photos/[0-9a-f-]{36}\.jpg$'),
    CONSTRAINT ck_wardrobe_photo_upload_stored_metadata CHECK (status NOT IN ('READY', 'ATTACHED')
        OR (content_type = 'image/jpeg' AND width > 0 AND height > 0 AND ready_at IS NOT NULL)),
    CONSTRAINT ck_wardrobe_photo_upload_attached
        CHECK ((status = 'ATTACHED') = (garment_id IS NOT NULL AND attached_at IS NOT NULL)),
    CONSTRAINT ck_wardrobe_photo_upload_discarded
        CHECK ((status = 'DISCARDED') = (discarded_at IS NOT NULL AND next_cleanup_at IS NOT NULL)),
    CONSTRAINT ck_wardrobe_photo_upload_deleted_only_discarded
        CHECK (object_deleted_at IS NULL OR status = 'DISCARDED'),
    CONSTRAINT ck_wardrobe_photo_upload_expiry CHECK (expires_at > created_at),
    CONSTRAINT ck_wardrobe_photo_upload_attempts CHECK (cleanup_attempts >= 0)
);

-- Pending uploads in expiry order, for the sweeper.
CREATE INDEX ix_wardrobe_photo_upload_pending_expiry
    ON wardrobe.wardrobe_photo_upload (expires_at)
    WHERE status IN ('RECEIVING', 'READY');

-- Discarded uploads whose object deletion is still due.
CREATE INDEX ix_wardrobe_photo_upload_cleanup_due
    ON wardrobe.wardrobe_photo_upload (next_cleanup_at)
    WHERE status = 'DISCARDED' AND object_deleted_at IS NULL;
```

**Explanation.**

- **Integrity.** The CHECK constraints encode the lifecycle so the database refuses impossible combinations. Examples: an `ATTACHED` row without a garment, or a deleted object on an attached photo.
- **Foreign key.** The FK to `wardrobe_garment` stays inside the Wardrobe schema, which `DatabaseSchemaOwnershipIT` allows.
- **Rejected photos** never create a row: inspection happens before the intent (§10.6).

**Database implications.** Forward-only, Wardrobe-only. No routine or trigger. The partial indexes keep sweeper and cleanup queries off attached rows.

**Common mistakes.**

- Do not add `ON DELETE CASCADE` from garments; garment removal is not in scope.
- Do not store Brand, Price or any profile value here.

### Step P3 — [ADD] `GarmentPhoto.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/domain/GarmentPhoto.java`

```java
package com.capsuleai.wardrobe.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * The private photo attached to a garment at confirmation (US-GAR-003, DATA-GAR-004): media
 * metadata, not one of the 13 garment properties. The bytes live in private object storage
 * and are served only after a current owner check (S3-IMG-05).
 */
public record GarmentPhoto(UUID photoUploadId, String contentType, int width, int height) {

    public GarmentPhoto {
        Objects.requireNonNull(photoUploadId, "photoUploadId");
        Objects.requireNonNull(contentType, "contentType");
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("A stored photo has positive dimensions");
        }
    }
}
```

### Step P4 — [MODIFY] `Garment.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/domain/Garment.java`. Replace the whole file (US-GAR-002 version plus `photo`):

```java
package com.capsuleai.wardrobe.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * One confirmed garment owned by one account (DATA-GAR-001, BRULE-GAR-001).
 * Only accepted creation produces a Garment; drafts and staged photos never do.
 *
 * <p>{@code removedAt} is null while the garment is in the owner's current
 * wardrobe. Ownership is independent of optional properties and of
 * recommendation eligibility, which is not evaluated (DATA-GAR-005).
 *
 * <p>{@code revision} identifies the accepted profile version (US-GAR-002, S3-GAR-02).
 * {@code photo} is the private photo attached at confirmation, if any (US-GAR-003); it is
 * metadata, not a garment property, and edits never change it (S3-IMG-07).
 */
public record Garment(
        UUID id,
        UUID ownerAccountId,
        GarmentProfile profile,
        ValueProvenance provenance,
        Instant createdAt,
        Instant removedAt,
        long revision,
        Optional<GarmentPhoto> photo) {

    /** The revision of a newly accepted garment. */
    public static final long FIRST_REVISION = 1;

    public Garment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(provenance, "provenance");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(photo, "photo must be Optional.empty() when absent");
        if (removedAt != null && removedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("A garment cannot be removed before it was created");
        }
        if (revision < FIRST_REVISION) {
            throw new IllegalArgumentException("A garment revision starts at " + FIRST_REVISION);
        }
    }

    /** A garment without a photo. */
    public Garment(UUID id, UUID ownerAccountId, GarmentProfile profile, ValueProvenance provenance,
                   Instant createdAt, Instant removedAt, long revision) {
        this(id, ownerAccountId, profile, provenance, createdAt, removedAt, revision, Optional.empty());
    }

    /** A garment at its first revision, without a photo. */
    public Garment(UUID id, UUID ownerAccountId, GarmentProfile profile, ValueProvenance provenance,
                   Instant createdAt, Instant removedAt) {
        this(id, ownerAccountId, profile, provenance, createdAt, removedAt, FIRST_REVISION, Optional.empty());
    }

    /** A newly confirmed manual entry: user-entered values, currently owned, no photo. */
    public static Garment confirmManualEntry(UUID id, UUID ownerAccountId, GarmentProfile profile, Instant createdAt) {
        return new Garment(id, ownerAccountId, profile, ValueProvenance.USER_ENTERED, createdAt, null);
    }

    /** A newly confirmed manual entry with the reviewed photo attached (US-GAR-003 AC-04). */
    public static Garment confirmWithPhoto(UUID id, UUID ownerAccountId, GarmentProfile profile, Instant createdAt,
                                           GarmentPhoto photo) {
        return new Garment(id, ownerAccountId, profile, ValueProvenance.USER_ENTERED, createdAt, null,
                FIRST_REVISION, Optional.of(photo));
    }

    /**
     * The same garment with an accepted revised profile (US-GAR-002). Identity, owner,
     * provenance, creation time, removal state and photo are kept; the revision advances by one.
     */
    public Garment withRevisedProfile(GarmentProfile revisedProfile) {
        Objects.requireNonNull(revisedProfile, "revisedProfile");
        if (!isCurrent()) {
            throw new IllegalStateException("Only a garment in the current wardrobe can be revised");
        }
        return new Garment(id, ownerAccountId, revisedProfile, provenance, createdAt, removedAt,
                Math.addExact(revision, 1), photo);
    }

    public boolean isCurrent() {
        return removedAt == null;
    }

    public boolean isOwnedBy(UUID accountId) {
        return ownerAccountId.equals(accountId);
    }
}
```

**Explanation.** Both earlier constructor shapes (6 and 7 arguments) remain, so `JdbcGarmentRepository` (until P34), `InMemoryGarmentRepository.remove`, `GarmentTest` and older tests compile unchanged.

### Step P5 — [ADD] `PhotoUploadStatus.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/domain/PhotoUploadStatus.java`

```java
package com.capsuleai.wardrobe.domain;

/**
 * The lifecycle of a staged garment photo (US-GAR-003, S3-IMG-04/08). The constant names are
 * stored and published, so rename one only together with a migration and a contract change.
 */
public enum PhotoUploadStatus {

    /** Intent recorded; the normalized photo is being stored. Not reviewable yet. */
    RECEIVING,

    /** Stored and reviewable; can be confirmed into a new garment until it expires. */
    READY,

    /** Belongs to exactly one confirmed garment; never expires and is never cleaned up. */
    ATTACHED,

    /** Cancelled, expired or failed; unusable, and its object is deleted by cleanup. */
    DISCARDED
}
```

### Step P6 — [ADD] `PhotoRejectionReason.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/domain/PhotoRejectionReason.java`

```java
package com.capsuleai.wardrobe.domain;

/**
 * Why a submitted photo was not accepted (FR-AI-012, ERR-AI-001). Each maps to the published
 * problem code {@code PHOTO_<NAME>}; the user can replace the photo or continue manually.
 */
public enum PhotoRejectionReason {

    /** The bytes are not a JPEG, PNG or HEIC/HEIF image, whatever the file name or declared type says. */
    UNSUPPORTED_FORMAT,

    /** More than the approved maximum size (provisionally 15,000,000 bytes; OPEN-S3-09). */
    TOO_LARGE,

    /** The shortest side of the original image is below 512 pixels. */
    TOO_SMALL,

    /** A supported format that could not be decoded completely and safely within the processing limits. */
    UNREADABLE,

    /** A HEIF file with more than one top-level image (interim behaviour; OPEN-S3-11). */
    MULTIPLE_IMAGES
}
```

### Step P7 — [ADD] `PhotoUpload.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/domain/PhotoUpload.java`

```java
package com.capsuleai.wardrobe.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * One owner's staged garment photo (US-GAR-003, S3-IMG-03/04/08): private unconfirmed work that
 * never counts as owned clothing. It becomes part of a garment only through a separately
 * confirmed creation, which moves it from READY to ATTACHED.
 *
 * <p>{@code uploadKey} names the client's logical staging action, {@code contentSha256} the bytes
 * received for it, so a retry can be told apart from a different photo under the same key.
 */
public record PhotoUpload(
        UUID id,
        UUID ownerAccountId,
        UUID uploadKey,
        String contentSha256,
        String objectKey,
        PhotoUploadStatus status,
        String contentType,
        Integer width,
        Integer height,
        Instant createdAt,
        Instant expiresAt,
        UUID garmentId) {

    /** Every stored photo is a normalized, metadata-free JPEG (OPEN-S3-10). */
    public static final String STORED_CONTENT_TYPE = "image/jpeg";

    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    public PhotoUpload {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        Objects.requireNonNull(uploadKey, "uploadKey");
        Objects.requireNonNull(contentSha256, "contentSha256");
        Objects.requireNonNull(objectKey, "objectKey");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (!SHA256_HEX.matcher(contentSha256).matches()) {
            throw new IllegalArgumentException("contentSha256 must be lower-case hexadecimal SHA-256");
        }
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("A staged photo expires after it was staged");
        }
        boolean stored = status == PhotoUploadStatus.READY || status == PhotoUploadStatus.ATTACHED;
        if (stored && (contentType == null || width == null || height == null || width < 1 || height < 1)) {
            throw new IllegalArgumentException("A stored photo has a content type and positive dimensions");
        }
        if ((status == PhotoUploadStatus.ATTACHED) != (garmentId != null)) {
            throw new IllegalArgumentException("Exactly an attached photo names its garment");
        }
    }

    /** A new intent, recorded before anything is stored. */
    public static PhotoUpload receiving(UUID id, UUID ownerAccountId, UUID uploadKey, String contentSha256,
                                        Instant createdAt, Duration pendingTtl) {
        return new PhotoUpload(id, ownerAccountId, uploadKey, contentSha256, objectKeyFor(id),
                PhotoUploadStatus.RECEIVING, null, null, null, createdAt, createdAt.plus(pendingTtl), null);
    }

    /** The opaque storage key: server-generated, unique, free of any user value. */
    public static String objectKeyFor(UUID photoUploadId) {
        return "photos/" + photoUploadId + ".jpg";
    }

    public boolean isOwnedBy(UUID accountId) {
        return ownerAccountId.equals(accountId);
    }

    /** Whether this staged photo can still be reviewed or confirmed at {@code now}. */
    public boolean isUsableAt(Instant now) {
        return status == PhotoUploadStatus.READY && now.isBefore(expiresAt);
    }

    /** The garment's view of this photo; only for a stored photo. */
    public GarmentPhoto asGarmentPhoto() {
        if (status != PhotoUploadStatus.READY && status != PhotoUploadStatus.ATTACHED) {
            throw new IllegalStateException("Only a stored photo can be attached");
        }
        return new GarmentPhoto(id, contentType, width, height);
    }
}
```

### Step P8 — [ADD] `ReceivedPhoto.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ReceivedPhoto.java`

```java
package com.capsuleai.wardrobe.application;

import java.io.IOException;
import java.io.InputStream;

/**
 * The bytes of one uploaded photo as the web adapter received them (US-GAR-003). Each call to
 * {@link #openStream()} reads from the start, so the size, hash, format sniffing and decoding can
 * each read independently. Nothing about the client's file name or declared type is included.
 */
public interface ReceivedPhoto {

    /** The number of bytes the client sent. */
    long sizeBytes();

    /** A new stream over the received bytes; the caller closes it. */
    InputStream openStream() throws IOException;
}
```

### Step P9 — [ADD] `PhotoRejectedException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoRejectedException.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.util.Objects;

/**
 * The photo was not accepted and nothing was stored (FR-AI-012, ERR-AI-001). The user can
 * replace it or continue with imageless manual entry. The message never echoes photo content.
 */
public final class PhotoRejectedException extends RuntimeException {

    private final PhotoRejectionReason reason;

    public PhotoRejectedException(PhotoRejectionReason reason) {
        super("Photo not accepted: " + reason);
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public PhotoRejectionReason reason() {
        return reason;
    }
}
```

### Step P10 — [ADD] `PhotoUploadNotFoundException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadNotFoundException.java`

```java
package com.capsuleai.wardrobe.application;

/**
 * No staged photo with this id belongs to the requesting owner: unknown, another account's or a
 * malformed id. Deliberately does not say which (NFR-SEC-001, S3-IMG-05).
 */
public final class PhotoUploadNotFoundException extends RuntimeException {

    public PhotoUploadNotFoundException() {
        super("No such staged photo for the requesting owner");
    }
}
```

### Step P11 — [ADD] `PhotoUploadNotUsableException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadNotUsableException.java`

```java
package com.capsuleai.wardrobe.application;

/**
 * The owner's staged photo cannot be used for this action: it is still being received, was
 * cancelled or expired, or already belongs to a garment (US-GAR-003 AC-03/AC-06). Nothing changed.
 */
public final class PhotoUploadNotUsableException extends RuntimeException {

    public PhotoUploadNotUsableException() {
        super("The staged photo cannot be used for this action");
    }
}
```

### Step P12 — [ADD] `PhotoUploadKeyReusedException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadKeyReusedException.java`

```java
package com.capsuleai.wardrobe.application;

/** The upload Idempotency-Key was already used by this owner for a different photo. Nothing changed. */
public final class PhotoUploadKeyReusedException extends RuntimeException {

    public PhotoUploadKeyReusedException() {
        super("The upload Idempotency-Key was already used for a different photo");
    }
}
```

### Step P13 — [ADD] `GarmentPhotoUnavailableException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentPhotoUnavailableException.java`

```java
package com.capsuleai.wardrobe.application;

/**
 * The owner's current garment has no photo that can be shown: none was attached, or its stored
 * object is missing (US-GAR-003 AC-06). The garment's accepted information is unaffected.
 */
public final class GarmentPhotoUnavailableException extends RuntimeException {

    public GarmentPhotoUnavailableException() {
        super("No photo can be shown for this garment");
    }
}
```

### Step P14 — [ADD] `PhotoStorageUnavailableException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoStorageUnavailableException.java`

```java
package com.capsuleai.wardrobe.application.ports;

/**
 * Private photo storage could not complete an operation (unreachable, timed out, refused). The
 * outcome of the operation is not confirmed; callers keep their durable state recoverable.
 */
public final class PhotoStorageUnavailableException extends RuntimeException {

    public PhotoStorageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Step P15 — [ADD] `PhotoProcessingUnavailableException.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoProcessingUnavailableException.java`

```java
package com.capsuleai.wardrobe.application.ports;

/**
 * The photo could not be processed right now: no decoding capacity became free in time, or the
 * HEIF decoder is not installed. This is a temporary, truthful limitation, never "unsupported
 * format" (S3-IMG-06: a failed decoder is unresolved work, not permission to drop a format).
 */
public final class PhotoProcessingUnavailableException extends RuntimeException {

    public PhotoProcessingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### Step P16 — [ADD] `StoredPhoto.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/StoredPhoto.java`

```java
package com.capsuleai.wardrobe.application.ports;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/** An open stored photo. The receiver must close it (it holds a storage connection). */
public record StoredPhoto(InputStream content, long sizeBytes, String contentType) implements AutoCloseable {

    public StoredPhoto {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(contentType, "contentType");
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must not be negative");
        }
    }

    @Override
    public void close() throws IOException {
        content.close();
    }
}
```

### Step P17 — [ADD] `PhotoStorage.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoStorage.java`

```java
package com.capsuleai.wardrobe.application.ports;

import java.util.Optional;

/**
 * Wardrobe-owned private object storage for garment photos (ADA-007, S3-IMG-01). Never called
 * inside a database transaction. Every method may throw {@link PhotoStorageUnavailableException}.
 */
public interface PhotoStorage {

    /** Stores a new object; never overwrites an existing key. */
    void store(String objectKey, byte[] content, String contentType);

    /** The stored object, or empty if none exists under the key. */
    Optional<StoredPhoto> open(String objectKey);

    /** Deletes the object; deleting a key that does not exist succeeds (idempotent). */
    void delete(String objectKey);
}
```

### Step P18 — [ADD] `InspectedPhoto.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/InspectedPhoto.java`

```java
package com.capsuleai.wardrobe.application.ports;

import java.util.Objects;

/**
 * The result of genuinely decoding an accepted-format photo (S3-IMG-06): the detected format, the
 * original pixel dimensions (used for the 512-pixel rule) and the normalized JPEG to store, with
 * its own dimensions (orientation applied, metadata removed, long edge bounded; OPEN-S3-10).
 */
public record InspectedPhoto(
        String sourceFormat,
        int originalWidth,
        int originalHeight,
        int width,
        int height,
        byte[] jpeg) {

    public InspectedPhoto {
        Objects.requireNonNull(sourceFormat, "sourceFormat");
        Objects.requireNonNull(jpeg, "jpeg");
        if (originalWidth < 1 || originalHeight < 1 || width < 1 || height < 1 || jpeg.length == 0) {
            throw new IllegalArgumentException("An inspected photo has positive dimensions and content");
        }
    }
}
```

### Step P19 — [ADD] `PhotoInspector.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoInspector.java`

```java
package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.application.ReceivedPhoto;

/**
 * Decides, from the bytes alone, whether a photo is a genuine JPEG, PNG or HEIC/HEIF image and
 * produces the normalized JPEG to store (S3-IMG-06, OPEN-S3-08/10/12). Implementations bound
 * memory, pixels, time and temporary files.
 */
public interface PhotoInspector {

    /**
     * @throws com.capsuleai.wardrobe.application.PhotoRejectedException if the photo is not accepted
     * @throws PhotoProcessingUnavailableException                        if it cannot be processed now
     */
    InspectedPhoto inspectAndNormalize(ReceivedPhoto photo);
}
```

**Note on dependency direction.** `application.ports` refers to `application` types (`ReceivedPhoto`, and `PhotoRejectedException` in Javadoc), as the existing ports refer to domain types. Both are in the same module and framework-free, so `ArchitectureDependencyTest` allows it.

### Step P20 — [ADD] `PhotoCleanupTarget.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoCleanupTarget.java`

```java
package com.capsuleai.wardrobe.application.ports;

import java.util.Objects;
import java.util.UUID;

/** A discarded staged photo whose stored object is due for deletion, claimed by one cleanup run. */
public record PhotoCleanupTarget(UUID photoUploadId, String objectKey) {

    public PhotoCleanupTarget {
        Objects.requireNonNull(photoUploadId, "photoUploadId");
        Objects.requireNonNull(objectKey, "objectKey");
    }
}
```

### Step P21 — [ADD] `PhotoUploadRepository.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ports/PhotoUploadRepository.java`

```java
package com.capsuleai.wardrobe.application.ports;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Wardrobe-owned persistence of staged garment photos and their lifecycle (S3-IMG-04/08). Every
 * status change is a conditional statement, so concurrent confirmation, cancellation, expiry and
 * cleanup each see the outcome of the others and never act on a row in the wrong state.
 */
public interface PhotoUploadRepository {

    /** Records a RECEIVING intent unless the owner already used its upload key. @return true if recorded */
    boolean addIfUploadKeyUnused(PhotoUpload upload);

    Optional<PhotoUpload> findByUploadKey(UUID ownerAccountId, UUID uploadKey);

    /** The owner's staged photo in any status; another owner's is never returned. */
    Optional<PhotoUpload> findOwned(UUID ownerAccountId, UUID photoUploadId);

    /**
     * Like {@link #findOwned} but locks the row until the surrounding transaction ends, serializing
     * confirmation with other confirmations, cancellation and expiry. Call inside {@link WardrobeTransactions}.
     */
    Optional<PhotoUpload> lockOwned(UUID ownerAccountId, UUID photoUploadId);

    /** The photo attached to the owner's garment, if any. */
    Optional<PhotoUpload> findAttachedTo(UUID ownerAccountId, UUID garmentId);

    /** RECEIVING → READY with the stored photo's metadata. @return false if it is no longer RECEIVING */
    boolean markReady(UUID photoUploadId, String contentType, int width, int height, Instant readyAt);

    /** RECEIVING → DISCARDED after a failed store; deletion waits for the in-flight fence. */
    boolean discardReceiving(UUID photoUploadId, Instant now, Duration inFlightFence);

    /** The owner's RECEIVING or READY photo → DISCARDED (cancel). @return false if it was in another status */
    boolean cancel(UUID ownerAccountId, UUID photoUploadId, Instant now, Duration inFlightFence);

    /** A store finished after its photo was discarded: delete the object again (late-orphan fence). */
    void requeueCleanup(UUID photoUploadId, Instant now);

    /** READY → ATTACHED to the new garment. Call inside the creating transaction. @return false if not READY */
    boolean attach(UUID photoUploadId, UUID garmentId, Instant attachedAt);

    /** Discards up to {@code limit} pending photos whose expiry has passed. @return how many */
    int expireDue(Instant now, Duration inFlightFence, int limit);

    /**
     * Claims up to {@code limit} discarded photos whose deletion is due, leasing each until
     * {@code leaseUntil} so a concurrent or later run does not repeat it before then.
     */
    List<PhotoCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit);

    /** Records that the object of a discarded photo is deleted. @return false if already recorded */
    boolean markObjectDeleted(UUID photoUploadId, Instant deletedAt);
}
```

### Step P22 — [ADD] `PhotoUploadPolicy.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadPolicy.java`

```java
package com.capsuleai.wardrobe.application;

import java.time.Duration;
import java.util.Objects;

/**
 * Photo rules and lifecycle settings, supplied by configuration (US-GAR-003).
 *
 * @param maximumBytes        FR-AI-012 "15 MB"; provisionally 15,000,000 (OPEN-S3-09)
 * @param minimumShortestSide FR-AI-012: 512 pixels
 * @param pendingTtl          how long an unconfirmed photo stays usable; provisional 24 hours, pending
 *                            Product Owner approval (OPEN-S3-07, S3-IMG-08)
 * @param inFlightFence       minimum time between staging and deleting an object of a photo discarded while
 *                            still receiving; longer than any store plus decode can take
 * @param cleanupBatchSize    rows handled per expiry or deletion pass
 * @param cleanupRetryDelay   lease and retry delay for an object deletion
 */
public record PhotoUploadPolicy(
        long maximumBytes,
        int minimumShortestSide,
        Duration pendingTtl,
        Duration inFlightFence,
        int cleanupBatchSize,
        Duration cleanupRetryDelay) {

    public PhotoUploadPolicy {
        Objects.requireNonNull(pendingTtl, "pendingTtl");
        Objects.requireNonNull(inFlightFence, "inFlightFence");
        Objects.requireNonNull(cleanupRetryDelay, "cleanupRetryDelay");
        if (maximumBytes < 1 || minimumShortestSide < 1 || cleanupBatchSize < 1) {
            throw new IllegalArgumentException("Photo limits and batch size must be positive");
        }
        if (pendingTtl.isNegative() || pendingTtl.isZero() || inFlightFence.isNegative()
                || cleanupRetryDelay.isNegative() || cleanupRetryDelay.isZero()) {
            throw new IllegalArgumentException("Photo lifecycle durations must be positive");
        }
    }
}
```

### Step P23 — [ADD] `StagePhotoCommand.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/StagePhotoCommand.java`

```java
package com.capsuleai.wardrobe.application;

import java.util.UUID;

/**
 * One photo staging request as submitted (US-GAR-003). The owner comes from the authenticated
 * session; {@code uploadKey} is the raw Idempotency-Key header; {@code photo} is null when the
 * request carried no photo part.
 */
public record StagePhotoCommand(UUID ownerAccountId, String uploadKey, ReceivedPhoto photo) {
}
```

### Step P24 — [ADD] `StagedPhoto.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/StagedPhoto.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.Objects;

/** The staged photo, and whether this request staged it ({@code false}: a retry of the same staging). */
public record StagedPhoto(PhotoUpload upload, boolean newlyStaged) {

    public StagedPhoto {
        Objects.requireNonNull(upload, "upload");
    }
}
```

### Step P25 — [ADD] `StagePhotoUploadService.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/StagePhotoUploadService.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-GAR-003 — stages one private garment photo for later explicit confirmation (S3-IMG-02/03/04,
 * FR-AI-001/002/012). Staging never creates a garment.
 *
 * <p>Order of work: the size rule, then the genuine decode and normalization (no durable state
 * yet, so a rejected photo leaves nothing behind), then a durable RECEIVING intent, then the
 * object store outside any transaction, then a conditional RECEIVING → READY. A retry with the
 * same Idempotency-Key and the same bytes returns the same staged photo; different bytes under the
 * same key are refused.
 */
public final class StagePhotoUploadService {

    static final String PHOTO_FIELD = "photo";
    static final String UPLOAD_KEY_FIELD = "Idempotency-Key";

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;
    private final PhotoInspector inspector;
    private final WardrobeTransactions transactions;
    private final Clock clock;
    private final PhotoUploadPolicy policy;

    public StagePhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage, PhotoInspector inspector,
                                   WardrobeTransactions transactions, Clock clock, PhotoUploadPolicy policy) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.inspector = Objects.requireNonNull(inspector, "inspector");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * @throws GarmentValidationException    if the photo part or the Idempotency-Key is missing or malformed
     * @throws PhotoRejectedException        if the photo is not accepted (nothing stored)
     * @throws PhotoUploadKeyReusedException if the key was used for different bytes
     * @throws PhotoUploadNotUsableException if the photo was cancelled or expired while it was being stored
     */
    public StagedPhoto stage(StagePhotoCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        UUID uploadKey = validate(command);
        ReceivedPhoto photo = command.photo();
        if (photo.sizeBytes() > policy.maximumBytes()) {
            throw new PhotoRejectedException(PhotoRejectionReason.TOO_LARGE);
        }
        String contentSha256 = sha256(photo);

        Optional<PhotoUpload> earlier = uploads.findByUploadKey(owner, uploadKey);
        if (earlier.isPresent()) {
            return replay(earlier.get(), contentSha256);
        }

        InspectedPhoto inspected = inspector.inspectAndNormalize(photo);
        if (Math.min(inspected.originalWidth(), inspected.originalHeight()) < policy.minimumShortestSide()) {
            throw new PhotoRejectedException(PhotoRejectionReason.TOO_SMALL);
        }

        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), owner, uploadKey, contentSha256, now(),
                policy.pendingTtl());
        if (!transactions.inTransaction(() -> uploads.addIfUploadKeyUnused(intent))) {
            PhotoUpload concurrent = uploads.findByUploadKey(owner, uploadKey)
                    .orElseThrow(() -> new IllegalStateException("A used upload key has no staged photo"));
            return replay(concurrent, contentSha256);
        }

        try {
            storage.store(intent.objectKey(), inspected.jpeg(), PhotoUpload.STORED_CONTENT_TYPE);
        } catch (RuntimeException storeFailed) {
            transactions.inTransaction(() -> uploads.discardReceiving(intent.id(), now(), policy.inFlightFence()));
            throw storeFailed;
        }

        boolean ready = transactions.inTransaction(() -> uploads.markReady(intent.id(),
                PhotoUpload.STORED_CONTENT_TYPE, inspected.width(), inspected.height(), now()));
        if (!ready) {
            // Cancelled or expired while storing: make sure the object just written is deleted (S3-IMG-04).
            transactions.inTransaction(() -> {
                uploads.requeueCleanup(intent.id(), now());
                return null;
            });
            throw new PhotoUploadNotUsableException();
        }
        PhotoUpload staged = uploads.findOwned(owner, intent.id())
                .orElseThrow(() -> new IllegalStateException("A staged photo disappeared"));
        return new StagedPhoto(staged, true);
    }

    private UUID validate(StagePhotoCommand command) {
        List<GarmentFieldViolation> violations = new ArrayList<>();
        UUID uploadKey = null;
        String rawKey = command.uploadKey();
        if (rawKey == null || rawKey.isBlank()) {
            violations.add(new GarmentFieldViolation(UPLOAD_KEY_FIELD, GarmentValueProblem.REQUIRED));
        } else if (!CANONICAL_UUID.matcher(rawKey).matches()) {
            violations.add(new GarmentFieldViolation(UPLOAD_KEY_FIELD, GarmentValueProblem.INVALID_FORMAT));
        } else {
            uploadKey = UUID.fromString(rawKey);
        }
        if (command.photo() == null || command.photo().sizeBytes() == 0) {
            violations.add(new GarmentFieldViolation(PHOTO_FIELD, GarmentValueProblem.REQUIRED));
        }
        if (!violations.isEmpty()) {
            throw new GarmentValidationException(violations);
        }
        return uploadKey;
    }

    private static StagedPhoto replay(PhotoUpload earlier, String contentSha256) {
        if (!earlier.contentSha256().equals(contentSha256)) {
            throw new PhotoUploadKeyReusedException();
        }
        return new StagedPhoto(earlier, false);
    }

    /** SHA-256 of the received bytes, counting them too, so a misreported size cannot slip past the limit. */
    private String sha256(ReceivedPhoto photo) {
        try (InputStream content = photo.openStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            long total = 0;
            for (int read = content.read(buffer); read != -1; read = content.read(buffer)) {
                total += read;
                if (total > policy.maximumBytes()) {
                    throw new PhotoRejectedException(PhotoRejectionReason.TOO_LARGE);
                }
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException unreadable) {
            throw new UncheckedIOException("The received photo could not be read", unreadable);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required on every Java platform", impossible);
        }
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
```

**Explanation.**

- **Order of work.** Inspection runs before the intent is recorded. A rejected photo therefore leaves **no row and no object**, and retrying it reproduces the same rejection.
- **Late-orphan fence.** The object is stored only after this request won its own intent insert, so the loser of a same-key race never writes an object. `requeueCleanup` covers the case where a cancel or expiry wins the race with a still-running store (S3-IMG-04, "late orphan").
- **Timestamps.** They are truncated to microseconds because PostgreSQL stores microseconds, as in `AddGarmentService`.

**Common mistakes.**

- Do not move `storage.store` inside `transactions.inTransaction`; that would hold a database transaction open during a network transfer.
- Do not compute the object key from the hash.

### Step P26 — [ADD] `ViewPhotoUploadService.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/ViewPhotoUploadService.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * US-GAR-003 — the owner's staged photo: its status (outcome review after an interrupted upload or
 * confirmation) and, while it is READY, its bytes for review (AC-03, S3-IMG-05). Read-only.
 */
public final class ViewPhotoUploadService {

    private static final Pattern CANONICAL_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;
    private final Clock clock;

    public ViewPhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage, Clock clock) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** @throws PhotoUploadNotFoundException for an unknown, malformed or another owner's id */
    public PhotoUpload view(UUID ownerAccountId, String photoUploadId) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        return uploads.findOwned(ownerAccountId, photoUploadId(photoUploadId))
                .orElseThrow(PhotoUploadNotFoundException::new);
    }

    /**
     * The bytes of a READY, unexpired staged photo; the caller closes the result.
     *
     * @throws PhotoUploadNotUsableException if it is not reviewable (receiving, attached, cancelled, expired)
     */
    public StoredPhoto openPending(UUID ownerAccountId, String photoUploadId) {
        PhotoUpload upload = view(ownerAccountId, photoUploadId);
        if (!upload.isUsableAt(clock.instant())) {
            throw new PhotoUploadNotUsableException();
        }
        return storage.open(upload.objectKey()).orElseThrow(PhotoUploadNotUsableException::new);
    }

    /** A canonical UUID, or "not found" for anything else, so a malformed id reveals nothing. */
    static UUID photoUploadId(String photoUploadId) {
        if (photoUploadId == null || !CANONICAL_UUID.matcher(photoUploadId).matches()) {
            throw new PhotoUploadNotFoundException();
        }
        return UUID.fromString(photoUploadId);
    }
}
```

### Step P27 — [ADD] `CancelPhotoUploadService.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/CancelPhotoUploadService.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-003 — cancels the owner's unconfirmed staged photo (AC-03/AC-06, FR-AI-004/009). Cancelled
 * work creates no garment and becomes unusable at once; its object is deleted by cleanup. Cancelling
 * twice succeeds; a photo that already belongs to a garment cannot be cancelled (S3-IMG-07).
 */
public final class CancelPhotoUploadService {

    private final PhotoUploadRepository uploads;
    private final WardrobeTransactions transactions;
    private final Clock clock;
    private final PhotoUploadPolicy policy;

    public CancelPhotoUploadService(PhotoUploadRepository uploads, WardrobeTransactions transactions, Clock clock,
                                    PhotoUploadPolicy policy) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    /**
     * @throws PhotoUploadNotFoundException  for an unknown, malformed or another owner's id
     * @throws PhotoUploadNotUsableException if the photo already belongs to a garment
     */
    public void cancel(UUID ownerAccountId, String photoUploadId) {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId");
        UUID id = ViewPhotoUploadService.photoUploadId(photoUploadId);
        PhotoUpload upload = uploads.findOwned(ownerAccountId, id).orElseThrow(PhotoUploadNotFoundException::new);
        switch (upload.status()) {
            case ATTACHED -> throw new PhotoUploadNotUsableException();
            case DISCARDED -> {
                // Already cancelled or expired: cancelling again changes nothing.
            }
            case RECEIVING, READY -> {
                boolean cancelled = transactions.inTransaction(() -> uploads.cancel(ownerAccountId, id,
                        clock.instant().truncatedTo(ChronoUnit.MICROS), policy.inFlightFence()));
                if (!cancelled && uploads.findOwned(ownerAccountId, id).map(PhotoUpload::status)
                        .filter(PhotoUploadStatus.ATTACHED::equals).isPresent()) {
                    throw new PhotoUploadNotUsableException();
                }
            }
        }
    }
}
```

### Step P28 — [ADD] `GarmentPhotoContentService.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentPhotoContentService.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-003 — the photo of the owner's current garment, after the same owner and currency checks
 * as garment detail (S3-IMG-05, AC-07). A missing photo or object affects only this read; the
 * garment's information stays available (AC-06).
 */
public final class GarmentPhotoContentService {

    private final InspectGarmentService garments;
    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;

    public GarmentPhotoContentService(InspectGarmentService garments, PhotoUploadRepository uploads,
                                      PhotoStorage storage) {
        this.garments = Objects.requireNonNull(garments, "garments");
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    /**
     * @throws GarmentNotFoundException          for an unknown, malformed or another owner's garment
     * @throws GarmentNoLongerCurrentException   for the owner's removed garment
     * @throws GarmentPhotoUnavailableException  if no photo is attached or its object is missing
     */
    public StoredPhoto open(UUID ownerAccountId, String garmentId) {
        Garment garment = garments.inspect(ownerAccountId, garmentId);
        if (garment.photo().isEmpty()) {
            throw new GarmentPhotoUnavailableException();
        }
        PhotoUpload attached = uploads.findAttachedTo(ownerAccountId, garment.id())
                .orElseThrow(GarmentPhotoUnavailableException::new);
        return storage.open(attached.objectKey()).orElseThrow(GarmentPhotoUnavailableException::new);
    }
}
```

### Step P29 — [ADD] `PhotoUploadCleanupService.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/PhotoUploadCleanupService.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * US-GAR-003 — the recoverable pending-photo lifecycle (S3-IMG-04/08): expires unconfirmed photos
 * whose pending TTL has passed and deletes the objects of discarded photos, retrying failed deletions
 * on later runs. Attached photos are never selected. Object deletion runs outside any transaction;
 * a crash between deletion and its record only repeats an idempotent deletion.
 */
public final class PhotoUploadCleanupService {

    /** What one run did. */
    public record CleanupResult(int expired, int deleted, int failed) {
    }

    private final PhotoUploadRepository uploads;
    private final PhotoStorage storage;
    private final WardrobeTransactions transactions;
    private final Clock clock;
    private final PhotoUploadPolicy policy;

    public PhotoUploadCleanupService(PhotoUploadRepository uploads, PhotoStorage storage,
                                     WardrobeTransactions transactions, Clock clock, PhotoUploadPolicy policy) {
        this.uploads = Objects.requireNonNull(uploads, "uploads");
        this.storage = Objects.requireNonNull(storage, "storage");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public CleanupResult run() {
        Instant now = now();
        int expired = transactions.inTransaction(() ->
                uploads.expireDue(now, policy.inFlightFence(), policy.cleanupBatchSize()));
        List<PhotoCleanupTarget> due = transactions.inTransaction(() ->
                uploads.claimDueCleanup(now, now.plus(policy.cleanupRetryDelay()), policy.cleanupBatchSize()));
        int deleted = 0;
        int failed = 0;
        for (PhotoCleanupTarget target : due) {
            try {
                storage.delete(target.objectKey());
                transactions.inTransaction(() -> uploads.markObjectDeleted(target.photoUploadId(), now()));
                deleted++;
            } catch (PhotoStorageUnavailableException unavailable) {
                failed++; // The lease expires and a later run retries this deletion.
            }
        }
        return new CleanupResult(expired, deleted, failed);
    }

    private Instant now() {
        return clock.instant().truncatedTo(ChronoUnit.MICROS);
    }
}
```

### Step P30 — [MODIFY] `AddGarmentCommand.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/AddGarmentCommand.java`. Replace the whole file:

```java
package com.capsuleai.wardrobe.application;

import java.util.List;
import java.util.UUID;

/**
 * One manual garment addition exactly as submitted. Missing values arrive as null.
 * {@code ownerAccountId} comes from the authenticated session, never from the body;
 * {@code idempotencyKey} identifies the user's logical addition across retries;
 * {@code photoUploadId} optionally names the reviewed staged photo to attach (US-GAR-003).
 */
public record AddGarmentCommand(
        UUID ownerAccountId,
        String idempotencyKey,
        Boolean confirmed,
        String category,
        String subtype,
        String color,
        List<String> materials,
        String pattern,
        List<String> styles,
        String fit,
        String neckline,
        String sleeveLength,
        String length,
        String silhouette,
        String brand,
        String priceAmount,
        String priceCurrency,
        String photoUploadId) {

    /** An imageless manual addition (US-GAR-001). */
    public AddGarmentCommand(UUID ownerAccountId, String idempotencyKey, Boolean confirmed, String category,
                             String subtype, String color, List<String> materials, String pattern,
                             List<String> styles, String fit, String neckline, String sleeveLength, String length,
                             String silhouette, String brand, String priceAmount, String priceCurrency) {
        this(ownerAccountId, idempotencyKey, confirmed, category, subtype, color, materials, pattern, styles, fit,
                neckline, sleeveLength, length, silhouette, brand, priceAmount, priceCurrency, null);
    }

    /** The submitted garment properties, for the shared profile validation (S3-GAR-05). */
    GarmentValues values() {
        return new GarmentValues(category, subtype, color, materials, pattern, styles, fit, neckline,
                sleeveLength, length, silhouette, brand, priceAmount, priceCurrency);
    }
}
```

**Explanation.** The 17-argument constructor keeps every existing `new AddGarmentCommand(...)` call compiling: `GarmentEntryValidatorTest`, `AddGarmentServiceTest` and `ConcurrentGarmentCreationIT`.

### Step P31 — [MODIFY] `GarmentEntryValidator.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentEntryValidator.java` (the US-GAR-002 version).

BEFORE:

```java
    /** A validated logical-action key and the accepted profile. */
    record ValidatedGarmentEntry(UUID creationKey, GarmentProfile profile) {
    }
```

AFTER:

```java
    /** A validated logical-action key, the accepted profile and the staged photo to attach, if any. */
    record ValidatedGarmentEntry(UUID creationKey, GarmentProfile profile, Optional<UUID> photoUploadId) {
    }
```

BEFORE:

```java
        UUID creationKey = creationKey(command.idempotencyKey());
        requireConfirmation(command.confirmed());
        Optional<GarmentProfile> profile = profile(command.values());
        rejectIfAnyProblem();
        return new ValidatedGarmentEntry(creationKey, profile.orElseThrow());
```

AFTER:

```java
        UUID creationKey = creationKey(command.idempotencyKey());
        requireConfirmation(command.confirmed());
        Optional<UUID> photoUploadId = photoUploadId(command.photoUploadId());
        Optional<GarmentProfile> profile = profile(command.values());
        rejectIfAnyProblem();
        return new ValidatedGarmentEntry(creationKey, profile.orElseThrow(), photoUploadId);
```

BEFORE:

```java
    private <E extends Enum<E> & ControlledValue> Optional<E> required(Class<E> vocabulary, String field, String code) {
```

AFTER:

```java
    /** US-GAR-003: the optional staged photo to attach, named by its id; blank means "no photo". */
    private Optional<UUID> photoUploadId(String photoUploadId) {
        if (isBlank(photoUploadId)) {
            return Optional.empty();
        }
        if (!CANONICAL_UUID.matcher(photoUploadId).matches()) {
            reject("photoUploadId", GarmentValueProblem.INVALID_FORMAT);
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(photoUploadId));
    }

    private <E extends Enum<E> & ControlledValue> Optional<E> required(Class<E> vocabulary, String field, String code) {
```

**Explanation.** For imageless entries the reported problems and their order are unchanged, so the 117 existing validator tests stay green.

### Step P32 — [MODIFY] `GarmentCreationFingerprint.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/GarmentCreationFingerprint.java`

BEFORE:

```java
import java.util.Optional;
import java.util.stream.Collectors;
```

AFTER:

```java
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
```

BEFORE:

```java
    static String of(GarmentProfile profile) {
        String canonical = String.join("|",
```

AFTER:

```java
    static String of(GarmentProfile profile) {
        return of(profile, Optional.empty());
    }

    /**
     * US-GAR-003 (S3-IMG-04): a photo-backed addition also fingerprints the staged photo it attaches,
     * so one key cannot create garments with different photos, or with and without one. Without a
     * photo the fingerprint is exactly the Sprint 2 one, so earlier imageless additions still replay.
     */
    static String of(GarmentProfile profile, Optional<UUID> photoUploadId) {
        String canonical = String.join("|",
```

BEFORE:

```java
                "price=" + profile.price().map(price -> price.amountText() + " " + price.currencyCode()).orElse(""));
```

AFTER:

```java
                "price=" + profile.price().map(price -> price.amountText() + " " + price.currencyCode()).orElse(""))
                + photoUploadId.map(photo -> "|photo=" + photo).orElse("");
```

**Common mistake.** Do not add an empty `|photo=` segment for imageless entries. It would change every stored Sprint 2 fingerprint, and their replays would become `IDEMPOTENCY_KEY_REUSED`.

### Step P33 — [MODIFY] `AddGarmentService.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/application/AddGarmentService.java`. Replace the whole file:

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.GarmentEntryValidator.ValidatedGarmentEntry;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.RecordedGarmentCreation;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * US-GAR-001 / US-GAR-003 — accepts one explicitly confirmed manual garment for the
 * authenticated owner, optionally with the reviewed staged photo attached
 * (FR-AI-003/008/010, FR-GAR-003, DATA-GAR-004, DATA-INT-004, ADA-009, S3-IMG-03/04).
 *
 * <p>The client's Idempotency-Key names the logical addition. The garment, its sets, the key
 * and (with a photo) the photo's READY → ATTACHED transition commit together, so repeating the
 * same addition returns the garment already accepted instead of creating a second one. A photo
 * that cannot be used makes the addition fail; it never falls back to an imageless garment.
 */
public final class AddGarmentService {

    private final GarmentRepository garments;
    private final PhotoUploadRepository photoUploads;
    private final WardrobeTransactions transactions;
    private final Clock clock;

    public AddGarmentService(GarmentRepository garments, PhotoUploadRepository photoUploads,
                             WardrobeTransactions transactions, Clock clock) {
        this.garments = Objects.requireNonNull(garments, "garments");
        this.photoUploads = Objects.requireNonNull(photoUploads, "photoUploads");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @throws GarmentValidationException    if the entry is unconfirmed, incomplete or invalid (nothing stored)
     * @throws IdempotencyKeyReusedException if the key was used for different values or another photo
     * @throws PhotoUploadNotFoundException  if the named staged photo is not the owner's
     * @throws PhotoUploadNotUsableException if the staged photo is not READY, has expired or is attached
     */
    public GarmentCreation add(AddGarmentCommand command) {
        UUID owner = Objects.requireNonNull(command.ownerAccountId(), "ownerAccountId");
        ValidatedGarmentEntry entry = GarmentEntryValidator.validate(command);
        String fingerprint = GarmentCreationFingerprint.of(entry.profile(), entry.photoUploadId());
        Instant now = clock.instant().truncatedTo(ChronoUnit.MICROS);

        if (entry.photoUploadId().isPresent()) {
            return addWithPhoto(owner, entry, entry.photoUploadId().get(), fingerprint, now);
        }
        Garment candidate = Garment.confirmManualEntry(UUID.randomUUID(), owner, entry.profile(), now);
        return transactions.inTransaction(() -> {
            if (garments.addIfCreationKeyUnused(candidate, entry.creationKey(), fingerprint)) {
                return new GarmentCreation(candidate, true);
            }
            return replay(owner, entry.creationKey(), fingerprint);
        });
    }

    /**
     * One transaction: lock the staged photo (serializing confirmations, cancellation and expiry),
     * replay an earlier acceptance of the same key, otherwise require a usable photo, create the
     * garment and attach the photo.
     */
    private GarmentCreation addWithPhoto(UUID owner, ValidatedGarmentEntry entry, UUID photoUploadId,
                                         String fingerprint, Instant now) {
        return transactions.inTransaction(() -> {
            PhotoUpload upload = photoUploads.lockOwned(owner, photoUploadId)
                    .orElseThrow(PhotoUploadNotFoundException::new);
            if (garments.findCreation(owner, entry.creationKey()).isPresent()) {
                return replay(owner, entry.creationKey(), fingerprint);
            }
            if (!upload.isUsableAt(now)) {
                throw new PhotoUploadNotUsableException();
            }
            Garment candidate = Garment.confirmWithPhoto(UUID.randomUUID(), owner, entry.profile(), now,
                    upload.asGarmentPhoto());
            if (!garments.addIfCreationKeyUnused(candidate, entry.creationKey(), fingerprint)) {
                return replay(owner, entry.creationKey(), fingerprint);
            }
            if (!photoUploads.attach(upload.id(), candidate.id(), now)) {
                throw new IllegalStateException("A locked READY photo could not be attached");
            }
            return new GarmentCreation(candidate, true);
        });
    }

    private GarmentCreation replay(UUID owner, UUID creationKey, String fingerprint) {
        RecordedGarmentCreation earlier = garments.findCreation(owner, creationKey)
                .orElseThrow(() -> new IllegalStateException("A used creation key has no recorded garment"));
        if (!earlier.creationFingerprint().equals(fingerprint)) {
            throw new IdempotencyKeyReusedException();
        }
        return new GarmentCreation(earlier.garment(), false);
    }
}
```

**Explanation.**

- **Imageless path.** It is behaviourally identical to US-GAR-001.
- **Lock before replay check.** In the photo path the replay check comes **after** the lock, so a concurrent duplicate waits for the first to commit and then replays.
- **Usability check.** It uses `expires_at` directly; a photo is unusable the moment it expires, even before the sweeper runs.
- **Attach failure.** A failed `attach` throws after the garment insert, which rolls the whole transaction back.

**Common mistake.** Do not check `isUsableAt` before the replay. A lost-response retry finds the photo `ATTACHED` and must still get its `200`.

### Step P34 — [MODIFY] `JdbcGarmentRepository.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/persistence/JdbcGarmentRepository.java` (the US-GAR-002 version).

BEFORE:

```java
import com.capsuleai.wardrobe.domain.GarmentNeckline;
```

AFTER:

```java
import com.capsuleai.wardrobe.domain.GarmentNeckline;
import com.capsuleai.wardrobe.domain.GarmentPhoto;
```

BEFORE:

```java
                   g.revision,
                   ARRAY(SELECT m.material FROM wardrobe.wardrobe_garment_material m WHERE m.garment_id = g.id) AS materials,
                   ARRAY(SELECT s.style FROM wardrobe.wardrobe_garment_style s WHERE s.garment_id = g.id) AS styles
            FROM wardrobe.wardrobe_garment g
            """;
```

AFTER:

```java
                   g.revision,
                   ARRAY(SELECT m.material FROM wardrobe.wardrobe_garment_material m WHERE m.garment_id = g.id) AS materials,
                   ARRAY(SELECT s.style FROM wardrobe.wardrobe_garment_style s WHERE s.garment_id = g.id) AS styles,
                   p.id AS photo_id, p.content_type AS photo_content_type, p.width AS photo_width,
                   p.height AS photo_height
            FROM wardrobe.wardrobe_garment g
            LEFT JOIN wardrobe.wardrobe_photo_upload p ON p.garment_id = g.id AND p.status = 'ATTACHED'
            """;
```

BEFORE:

```java
                fromDatabase(row, "removed_at"),
                row.getLong("revision"));
    }
```

AFTER:

```java
                fromDatabase(row, "removed_at"),
                row.getLong("revision"),
                photo(row));
    }

    /** US-GAR-003: the attached photo's metadata, if any. */
    private static Optional<GarmentPhoto> photo(ResultSet row) throws SQLException {
        UUID photoId = row.getObject("photo_id", UUID.class);
        if (photoId == null) {
            return Optional.empty();
        }
        return Optional.of(new GarmentPhoto(photoId, row.getString("photo_content_type"),
                row.getInt("photo_width"), row.getInt("photo_height")));
    }
```

**Explanation.**

- **Join.** The join is unique on `garment_id`, so it cannot multiply rows.
- **Page order.** Page queries keep their order and partial index; the join adds one index lookup per returned garment through `uq_wardrobe_photo_upload_garment`.
- **Ownership.** Both tables are in `wardrobe`, so AR-03 holds.

### Step P35 — [ADD] `JdbcPhotoUploadRepository.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/persistence/JdbcPhotoUploadRepository.java`

```java
package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

/** PostgreSQL implementation of {@link PhotoUploadRepository} (US-GAR-003, V10). */
public final class JdbcPhotoUploadRepository implements PhotoUploadRepository {

    static final String SELECT_UPLOAD = """
            SELECT id, owner_account_id, upload_key, content_sha256, object_key, status, content_type,
                   width, height, created_at, expires_at, garment_id
            FROM wardrobe.wardrobe_photo_upload
            """;

    /** Earliest moment an object of an upload discarded now may be deleted: after the in-flight fence. */
    private static final String FENCED_CLEANUP = "GREATEST(:now, created_at + :fenceSeconds * INTERVAL '1 second')";

    private final JdbcClient jdbc;

    public JdbcPhotoUploadRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addIfUploadKeyUnused(PhotoUpload upload) {
        return jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_photo_upload (
                            id, owner_account_id, upload_key, content_sha256, object_key, status, created_at, expires_at)
                        VALUES (:id, :ownerAccountId, :uploadKey, :contentSha256, :objectKey, 'RECEIVING',
                                :createdAt, :expiresAt)
                        ON CONFLICT (owner_account_id, upload_key) DO NOTHING
                        """)
                .param("id", upload.id())
                .param("ownerAccountId", upload.ownerAccountId())
                .param("uploadKey", upload.uploadKey())
                .param("contentSha256", upload.contentSha256())
                .param("objectKey", upload.objectKey())
                .param("createdAt", toDatabase(upload.createdAt()))
                .param("expiresAt", toDatabase(upload.expiresAt()))
                .update() == 1;
    }

    @Override
    public Optional<PhotoUpload> findByUploadKey(UUID ownerAccountId, UUID uploadKey) {
        return jdbc.sql(SELECT_UPLOAD + "WHERE owner_account_id = :ownerAccountId AND upload_key = :uploadKey")
                .param("ownerAccountId", ownerAccountId)
                .param("uploadKey", uploadKey)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public Optional<PhotoUpload> findOwned(UUID ownerAccountId, UUID photoUploadId) {
        return jdbc.sql(SELECT_UPLOAD + "WHERE owner_account_id = :ownerAccountId AND id = :id")
                .param("ownerAccountId", ownerAccountId)
                .param("id", photoUploadId)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public Optional<PhotoUpload> lockOwned(UUID ownerAccountId, UUID photoUploadId) {
        return jdbc.sql(SELECT_UPLOAD + "WHERE owner_account_id = :ownerAccountId AND id = :id FOR UPDATE")
                .param("ownerAccountId", ownerAccountId)
                .param("id", photoUploadId)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public Optional<PhotoUpload> findAttachedTo(UUID ownerAccountId, UUID garmentId) {
        return jdbc.sql(SELECT_UPLOAD
                        + "WHERE owner_account_id = :ownerAccountId AND garment_id = :garmentId AND status = 'ATTACHED'")
                .param("ownerAccountId", ownerAccountId)
                .param("garmentId", garmentId)
                .query((row, rowNumber) -> mapUpload(row))
                .optional();
    }

    @Override
    public boolean markReady(UUID photoUploadId, String contentType, int width, int height, Instant readyAt) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'READY', content_type = :contentType, width = :width, height = :height,
                            ready_at = :readyAt
                        WHERE id = :id AND status = 'RECEIVING'
                        """)
                .param("id", photoUploadId)
                .param("contentType", contentType)
                .param("width", width)
                .param("height", height)
                .param("readyAt", toDatabase(readyAt))
                .update() == 1;
    }

    @Override
    public boolean discardReceiving(UUID photoUploadId, Instant now, Duration inFlightFence) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'DISCARDED', discarded_at = :now, next_cleanup_at = %s
                        WHERE id = :id AND status = 'RECEIVING'
                        """.formatted(FENCED_CLEANUP))
                .param("id", photoUploadId)
                .param("now", toDatabase(now))
                .param("fenceSeconds", inFlightFence.toSeconds())
                .update() == 1;
    }

    @Override
    public boolean cancel(UUID ownerAccountId, UUID photoUploadId, Instant now, Duration inFlightFence) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'DISCARDED', discarded_at = :now,
                            next_cleanup_at = CASE WHEN status = 'RECEIVING' THEN %s ELSE :now END
                        WHERE id = :id AND owner_account_id = :ownerAccountId AND status IN ('RECEIVING', 'READY')
                        """.formatted(FENCED_CLEANUP))
                .param("id", photoUploadId)
                .param("ownerAccountId", ownerAccountId)
                .param("now", toDatabase(now))
                .param("fenceSeconds", inFlightFence.toSeconds())
                .update() == 1;
    }

    @Override
    public void requeueCleanup(UUID photoUploadId, Instant now) {
        jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET object_deleted_at = NULL, next_cleanup_at = :now
                        WHERE id = :id AND status = 'DISCARDED'
                        """)
                .param("id", photoUploadId)
                .param("now", toDatabase(now))
                .update();
    }

    @Override
    public boolean attach(UUID photoUploadId, UUID garmentId, Instant attachedAt) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'ATTACHED', garment_id = :garmentId, attached_at = :attachedAt
                        WHERE id = :id AND status = 'READY'
                        """)
                .param("id", photoUploadId)
                .param("garmentId", garmentId)
                .param("attachedAt", toDatabase(attachedAt))
                .update() == 1;
    }

    @Override
    public int expireDue(Instant now, Duration inFlightFence, int limit) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET status = 'DISCARDED', discarded_at = :now, next_cleanup_at = %s
                        WHERE id IN (
                            SELECT p.id FROM wardrobe.wardrobe_photo_upload p
                            WHERE p.status IN ('RECEIVING', 'READY') AND p.expires_at <= :now
                            ORDER BY p.expires_at
                            LIMIT :limit
                            FOR UPDATE SKIP LOCKED)
                          AND status IN ('RECEIVING', 'READY')
                        """.formatted(FENCED_CLEANUP))
                .param("now", toDatabase(now))
                .param("fenceSeconds", inFlightFence.toSeconds())
                .param("limit", limit)
                .update();
    }

    @Override
    public List<PhotoCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET next_cleanup_at = :leaseUntil, cleanup_attempts = cleanup_attempts + 1
                        WHERE id IN (
                            SELECT p.id FROM wardrobe.wardrobe_photo_upload p
                            WHERE p.status = 'DISCARDED' AND p.object_deleted_at IS NULL AND p.next_cleanup_at <= :now
                            ORDER BY p.next_cleanup_at
                            LIMIT :limit
                            FOR UPDATE SKIP LOCKED)
                        RETURNING id, object_key
                        """)
                .param("now", toDatabase(now))
                .param("leaseUntil", toDatabase(leaseUntil))
                .param("limit", limit)
                .query((row, rowNumber) ->
                        new PhotoCleanupTarget(row.getObject("id", UUID.class), row.getString("object_key")))
                .list();
    }

    @Override
    public boolean markObjectDeleted(UUID photoUploadId, Instant deletedAt) {
        return jdbc.sql("""
                        UPDATE wardrobe.wardrobe_photo_upload
                        SET object_deleted_at = :deletedAt
                        WHERE id = :id AND status = 'DISCARDED' AND object_deleted_at IS NULL
                        """)
                .param("id", photoUploadId)
                .param("deletedAt", toDatabase(deletedAt))
                .update() == 1;
    }

    static PhotoUpload mapUpload(ResultSet row) throws SQLException {
        return new PhotoUpload(
                row.getObject("id", UUID.class),
                row.getObject("owner_account_id", UUID.class),
                row.getObject("upload_key", UUID.class),
                row.getString("content_sha256"),
                row.getString("object_key"),
                PhotoUploadStatus.valueOf(row.getString("status")),
                row.getString("content_type"),
                row.getObject("width", Integer.class),
                row.getObject("height", Integer.class),
                row.getObject("created_at", OffsetDateTime.class).toInstant(),
                row.getObject("expires_at", OffsetDateTime.class).toInstant(),
                row.getObject("garment_id", UUID.class));
    }

    private static OffsetDateTime toDatabase(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
```

**Explanation.**

- **Fence arithmetic.** `:fenceSeconds * INTERVAL '1 second'` multiplies a `bigint` by an `interval`; PostgreSQL resolves it through its `float8 * interval` operator. It uses only built-in `pg_catalog` functions, never a routine in an owner schema.
- **Status predicates.** Every `UPDATE` carries its expected status, so a transition that lost a race changes nothing and returns `false`.
- **Ownership scan.** Table names are always `wardrobe.`-qualified and every SQL text is a single literal, as `PersistenceOwnershipSqlTest` requires.

**Common mistake.** Do not drop `FOR UPDATE SKIP LOCKED` from the sweep. Without it the sweep blocks behind a confirmation or, worse, expires a row a confirmation is about to attach.

### Step P36 — [ADD] `S3PhotoStorage.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/storage/S3PhotoStorage.java`

```java
package com.capsuleai.wardrobe.adapter.out.storage;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import java.util.Objects;
import java.util.Optional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * {@link PhotoStorage} on a private S3-compatible bucket (S3-IMG-01): Amazon S3 in deployment,
 * an S3-compatible fixture locally and in tests (OPEN-S3-13). Objects carry only a Content-Type;
 * no profile value, owner id or file name is written as metadata. The client is configured with
 * bounded call timeouts, so no call can hang a request thread (ADA-013).
 */
public final class S3PhotoStorage implements PhotoStorage {

    private static final int PRECONDITION_FAILED = 412;
    private static final int NOT_FOUND = 404;

    private final S3Client s3;
    private final String bucket;
    private final boolean conditionalWrites;

    public S3PhotoStorage(S3Client s3, String bucket, boolean conditionalWrites) {
        this.s3 = Objects.requireNonNull(s3, "s3");
        this.bucket = bucket == null ? "" : bucket.strip();
        this.conditionalWrites = conditionalWrites;
    }

    @Override
    public void store(String objectKey, byte[] content, String contentType) {
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(requireBucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) content.length);
        if (conditionalWrites) {
            request.ifNoneMatch("*"); // never overwrite an existing object (S3-IMG-04)
        }
        try {
            s3.putObject(request.build(), RequestBody.fromBytes(content));
        } catch (S3Exception refused) {
            if (refused.statusCode() == PRECONDITION_FAILED) {
                throw new IllegalStateException("A new photo key already exists in storage", refused);
            }
            throw unavailable("store", refused);
        } catch (SdkException failed) {
            throw unavailable("store", failed);
        }
    }

    @Override
    public Optional<StoredPhoto> open(String objectKey) {
        try {
            ResponseInputStream<GetObjectResponse> stream = s3.getObject(GetObjectRequest.builder()
                    .bucket(requireBucket())
                    .key(objectKey)
                    .build());
            GetObjectResponse response = stream.response();
            long size = response.contentLength() == null ? -1 : response.contentLength();
            if (size < 0) {
                stream.abort();
                throw new PhotoStorageUnavailableException("Photo storage returned no content length", null);
            }
            String contentType = response.contentType() == null ? "application/octet-stream" : response.contentType();
            return Optional.of(new StoredPhoto(stream, size, contentType));
        } catch (NoSuchKeyException missing) {
            return Optional.empty();
        } catch (S3Exception refused) {
            if (refused.statusCode() == NOT_FOUND) {
                return Optional.empty();
            }
            throw unavailable("open", refused);
        } catch (SdkException failed) {
            throw unavailable("open", failed);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(requireBucket()).key(objectKey).build());
        } catch (SdkException failed) {
            throw unavailable("delete", failed);
        }
    }

    private String requireBucket() {
        if (bucket.isEmpty()) {
            throw new PhotoStorageUnavailableException(
                    "capsuleai.wardrobe.photo-storage.bucket is not configured", null);
        }
        return bucket;
    }

    /** No object key, bucket name or credential appears in the message. */
    private static PhotoStorageUnavailableException unavailable(String operation, SdkException cause) {
        return new PhotoStorageUnavailableException("Photo storage " + operation + " failed", cause);
    }
}
```

**Explanation.**

- **Exceptions.** `NoSuchKeyException` and the 404 check both map "missing" to `Optional.empty()`; providers differ in which they throw. S3 `DeleteObject` succeeds for a missing key, so delete is idempotent.
- **Conditional writes.** Use `ifNoneMatch("*")` for S3-IMG-04 overwrite protection. It can be switched off for a fixture that lacks support (V-S3).
- **Configuration timing.** The bucket is checked at first use, not at startup. The rest of the application stays usable while photo storage is unconfigured, and a missing bucket is reported as `503`.

**Common mistake.** Do not generate presigned URLs here; delivery is application streaming (S3-IMG-05).

### Step P37 — [ADD] `JpegOrientation.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/imaging/JpegOrientation.java`

```java
package com.capsuleai.wardrobe.adapter.out.imaging;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the EXIF orientation (TIFF tag 0x0112, values 1–8) of a JPEG and applies it to decoded
 * pixels, so the stored derivative shows the garment the right way up without keeping any EXIF
 * (S3-IMG-06). Anything unexpected means "normal orientation": orientation never rejects a photo.
 */
final class JpegOrientation {

    static final int NORMAL = 1;

    private static final int START_OF_IMAGE = 0xFFD8;
    private static final int APP1 = 0xFFE1;
    private static final int START_OF_SCAN = 0xFFDA;
    private static final int END_OF_IMAGE = 0xFFD9;
    private static final int ORIENTATION_TAG = 0x0112;

    private JpegOrientation() {
    }

    /** The EXIF orientation of a JPEG stream, or {@link #NORMAL}. Does not close the stream. */
    static int read(InputStream jpeg) {
        try {
            DataInputStream data = new DataInputStream(jpeg);
            if (data.readUnsignedShort() != START_OF_IMAGE) {
                return NORMAL;
            }
            while (true) {
                int marker = data.readUnsignedShort();
                if ((marker & 0xFF00) != 0xFF00 || marker == START_OF_SCAN || marker == END_OF_IMAGE) {
                    return NORMAL;
                }
                int length = data.readUnsignedShort();
                if (length < 2) {
                    return NORMAL;
                }
                if (marker == APP1) {
                    byte[] segment = new byte[length - 2];
                    data.readFully(segment);
                    int orientation = exifOrientation(segment);
                    if (orientation != 0) {
                        return orientation;
                    }
                } else {
                    skipFully(data, length - 2);
                }
            }
        } catch (IOException | RuntimeException unreadable) {
            return NORMAL;
        }
    }

    /** The orientation in one APP1 segment, or 0 if it is not EXIF or has no valid orientation. */
    static int exifOrientation(byte[] segment) {
        if (segment.length < 14 || segment[0] != 'E' || segment[1] != 'x' || segment[2] != 'i'
                || segment[3] != 'f' || segment[4] != 0 || segment[5] != 0) {
            return 0;
        }
        int tiff = 6;
        boolean littleEndian = segment[tiff] == 'I' && segment[tiff + 1] == 'I';
        boolean bigEndian = segment[tiff] == 'M' && segment[tiff + 1] == 'M';
        if (!littleEndian && !bigEndian) {
            return 0;
        }
        long firstDirectory = tiff + unsignedInt(segment, tiff + 4, littleEndian);
        if (firstDirectory < tiff || firstDirectory + 2 > segment.length) {
            return 0;
        }
        int directory = (int) firstDirectory;
        int entries = unsignedShort(segment, directory, littleEndian);
        for (int index = 0; index < entries; index++) {
            int entry = directory + 2 + index * 12;
            if (entry + 12 > segment.length) {
                return 0;
            }
            if (unsignedShort(segment, entry, littleEndian) == ORIENTATION_TAG) {
                int value = unsignedShort(segment, entry + 8, littleEndian);
                return value >= 1 && value <= 8 ? value : 0;
            }
        }
        return 0;
    }

    /** The image as it should be displayed for an EXIF orientation (2–8 transform; others return it unchanged). */
    static BufferedImage apply(BufferedImage source, int orientation) {
        if (orientation < 2 || orientation > 8) {
            return source;
        }
        int width = source.getWidth();
        int height = source.getHeight();
        boolean swapsSides = orientation >= 5;
        BufferedImage target = new BufferedImage(swapsSides ? height : width, swapsSides ? width : height,
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int targetX;
                int targetY;
                switch (orientation) {
                    case 2 -> { targetX = width - 1 - x; targetY = y; }               // mirror horizontally
                    case 3 -> { targetX = width - 1 - x; targetY = height - 1 - y; }  // rotate 180°
                    case 4 -> { targetX = x; targetY = height - 1 - y; }              // mirror vertically
                    case 5 -> { targetX = y; targetY = x; }                           // transpose
                    case 6 -> { targetX = height - 1 - y; targetY = x; }              // rotate 90° clockwise
                    case 7 -> { targetX = height - 1 - y; targetY = width - 1 - x; }  // transverse
                    default -> { targetX = y; targetY = width - 1 - x; }              // 8: rotate 90° anticlockwise
                }
                target.setRGB(targetX, targetY, source.getRGB(x, y));
            }
        }
        return target;
    }

    private static void skipFully(DataInputStream data, int count) throws IOException {
        int remaining = count;
        while (remaining > 0) {
            int skipped = data.skipBytes(remaining);
            if (skipped <= 0) {
                throw new EOFException("Truncated JPEG segment");
            }
            remaining -= skipped;
        }
    }

    private static int unsignedShort(byte[] bytes, int offset, boolean littleEndian) {
        int first = bytes[offset] & 0xFF;
        int second = bytes[offset + 1] & 0xFF;
        return littleEndian ? first | (second << 8) : (first << 8) | second;
    }

    private static long unsignedInt(byte[] bytes, int offset, boolean littleEndian) {
        long value = 0;
        for (int index = 0; index < 4; index++) {
            int shift = littleEndian ? 8 * index : 8 * (3 - index);
            value |= (long) (bytes[offset + index] & 0xFF) << shift;
        }
        return value;
    }
}
```

**Explanation.**

- **Transforms.** The mapping is the standard EXIF orientation table: 6 means "rotate 90° clockwise to display".
- **Cost.** The pixel copy runs on the already-bounded derivative (≤ 2048 px long edge).
- **PNG.** PNG `eXIf` orientation is not applied; it is rarely written and not universally honoured. This is a known limitation listed in §17.

### Step P38 — [ADD] `HeifDecoder.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/imaging/HeifDecoder.java`

```java
package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ReceivedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Decodes HEIC/HEIF with libheif's {@code heif-dec} command in a separate, time-bounded process
 * (S3-IMG-06 Option B; toolchain OPEN-S3-08, verification required). A decoder crash or runaway
 * allocation cannot take down the JVM; libheif's default security limits stay enabled (the
 * {@code --disable-limits} option is never passed). Rotation and mirroring are applied by heif-dec
 * by default; EXIF and XMP are not written unless requested, and are not requested.
 *
 * <p>heif-dec decodes every top-level image and numbers the outputs when there are several. Until the
 * Product Owner decides OPEN-S3-11, more than one output is rejected explicitly as MULTIPLE_IMAGES.
 */
public final class HeifDecoder {

    private final String command;
    private final Duration timeout;

    public HeifDecoder(String command, Duration timeout) {
        this.command = Objects.requireNonNull(command, "command").strip();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    /** One decoded HEIF image as a PNG file in a private directory, deleted on close. */
    public record DecodedHeif(Path directory, Path png) implements AutoCloseable {

        @Override
        public void close() {
            deleteRecursively(directory);
        }
    }

    /**
     * @throws PhotoRejectedException              UNREADABLE (corrupt, truncated, too slow) or MULTIPLE_IMAGES
     * @throws PhotoProcessingUnavailableException if the decoder is not installed or cannot be started
     */
    public DecodedHeif decode(ReceivedPhoto photo) {
        Path directory = privateTemporaryDirectory();
        try {
            Path input = directory.resolve("input.heif");
            try (InputStream content = photo.openStream()) {
                Files.copy(content, input, StandardCopyOption.REPLACE_EXISTING);
            }
            Path output = directory.resolve("decoded.png");
            run(List.of(command, input.toString(), output.toString()));
            List<Path> decoded;
            try (Stream<Path> files = Files.list(directory)) {
                decoded = files.filter(file -> file.getFileName().toString().endsWith(".png")).sorted().toList();
            }
            if (decoded.isEmpty()) {
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
            if (decoded.size() > 1) {
                throw new PhotoRejectedException(PhotoRejectionReason.MULTIPLE_IMAGES); // OPEN-S3-11 interim
            }
            return new DecodedHeif(directory, decoded.get(0));
        } catch (IOException ioFailure) {
            deleteRecursively(directory);
            throw new UncheckedIOException("HEIF decoding workspace failed", ioFailure);
        } catch (RuntimeException failure) {
            deleteRecursively(directory);
            throw failure;
        }
    }

    private void run(List<String> arguments) {
        Process process;
        try {
            process = new ProcessBuilder(arguments)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } catch (IOException notInstalled) {
            throw new PhotoProcessingUnavailableException("The HEIF decoder could not be started", notInstalled);
        }
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
        } catch (InterruptedException interrupted) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new PhotoProcessingUnavailableException("HEIF decoding was interrupted", interrupted);
        }
        if (process.exitValue() != 0) {
            throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
        }
    }

    /** A new directory only the application user can read (POSIX); the platform default elsewhere. */
    static Path privateTemporaryDirectory() {
        try {
            try {
                return Files.createTempDirectory("capsuleai-heif-",
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            } catch (UnsupportedOperationException notPosix) {
                return Files.createTempDirectory("capsuleai-heif-");
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("No temporary directory for HEIF decoding", failure);
        }
    }

    static void deleteRecursively(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // A leftover temporary file is removed by the OS temp cleanup; it holds no metadata.
                }
            });
        } catch (IOException ignored) {
            // Nothing to delete.
        }
    }
}
```

**Explanation.**

- **Process invocation.** No shell is involved: arguments are passed directly, so no file name can inject a command (and no client file name is used at all). Output is discarded, so a chatty decoder cannot block on a full pipe.
- **Decoder missing.** A missing decoder is `503`, not "unsupported format" (S3-IMG-06).
- **Arguments.** Positional `input output` arguments work for both `heif-dec` and the older `heif-convert` name. No version-specific flag is passed.

**Common mistake.** Do not add `--disable-limits`; libheif's own help says it is not for production.

### Step P39 — [ADD] `ImageIoPhotoInspector.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/out/imaging/ImageIoPhotoInspector.java`

```java
package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ReceivedPhoto;
import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * Genuine, bounded photo validation and normalization (S3-IMG-06; OPEN-S3-08/10/11/12).
 *
 * <ul>
 *   <li>The format comes from the bytes: JPEG and PNG signatures, or an ISO-BMFF {@code ftyp} box with
 *       an HEVC image brand. File names and declared content types are never consulted.</li>
 *   <li>JPEG and PNG are decoded by Java ImageIO; HEIC/HEIF by {@link HeifDecoder} into a PNG that is
 *       then decoded the same way.</li>
 *   <li>The declared size is read from the header first; above the technical pixel ceiling the photo
 *       is refused before any pixel is allocated. Decoding subsamples so the long edge of the result is
 *       at most the derivative bound, which bounds memory without rejecting large photos.</li>
 *   <li>A watchdog aborts a decode that exceeds the timeout; any decoder warning (for example a
 *       truncated JPEG) means the photo could not be read completely.</li>
 *   <li>The derivative is an opaque RGB JPEG written without metadata (no EXIF, no GPS), with JPEG
 *       orientation applied.</li>
 * </ul>
 */
public final class ImageIoPhotoInspector implements PhotoInspector, AutoCloseable {

    private static final int SNIFF_BYTES = 256;
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final Set<String> HEVC_IMAGE_BRANDS = Set.of("heic", "heix", "hevc", "hevx", "heim", "heis");
    private static final Set<String> STRUCTURAL_BRANDS = Set.of("mif1", "msf1");
    private static final float JPEG_QUALITY = 0.9f;

    private final HeifDecoder heifDecoder;
    private final int derivativeLongEdge;
    private final long maximumDeclaredPixels;
    private final Duration decodeTimeout;
    private final Semaphore decodeSlots;
    private final ScheduledExecutorService watchdog;

    public ImageIoPhotoInspector(HeifDecoder heifDecoder, int derivativeLongEdge, long maximumDeclaredPixels,
                                 Duration decodeTimeout, int maximumConcurrentDecodes) {
        this.heifDecoder = Objects.requireNonNull(heifDecoder, "heifDecoder");
        this.derivativeLongEdge = derivativeLongEdge;
        this.maximumDeclaredPixels = maximumDeclaredPixels;
        this.decodeTimeout = Objects.requireNonNull(decodeTimeout, "decodeTimeout");
        this.decodeSlots = new Semaphore(maximumConcurrentDecodes, true);
        this.watchdog = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "photo-decode-watchdog");
            thread.setDaemon(true);
            return thread;
        });
        if (derivativeLongEdge < 1 || maximumDeclaredPixels < 1 || maximumConcurrentDecodes < 1) {
            throw new IllegalArgumentException("Photo decoding limits must be positive");
        }
    }

    @Override
    public InspectedPhoto inspectAndNormalize(ReceivedPhoto photo) {
        acquireDecodeSlot();
        try {
            byte[] head = head(photo);
            if (isJpeg(head)) {
                int orientation = jpegOrientation(photo);
                try (InputStream content = photo.openStream()) {
                    return normalize(new MemoryCacheImageInputStream(content), "JPEG", orientation);
                }
            }
            if (isPng(head)) {
                try (InputStream content = photo.openStream()) {
                    return normalize(new MemoryCacheImageInputStream(content), "PNG", JpegOrientation.NORMAL);
                }
            }
            if (isHeif(head)) {
                try (HeifDecoder.DecodedHeif decoded = heifDecoder.decode(photo)) {
                    return normalize(new FileImageInputStream(decoded.png().toFile()), "HEIF", JpegOrientation.NORMAL);
                }
            }
            throw new PhotoRejectedException(PhotoRejectionReason.UNSUPPORTED_FORMAT);
        } catch (IOException unreadable) {
            throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
        } finally {
            decodeSlots.release();
        }
    }

    @Override
    public void close() {
        watchdog.shutdownNow();
    }

    private InspectedPhoto normalize(ImageInputStream input, String format, int orientation) throws IOException {
        try (input) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            }
            ImageReader reader = readers.next();
            AtomicBoolean timedOut = new AtomicBoolean();
            AtomicBoolean warned = new AtomicBoolean();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || (long) width * height > maximumDeclaredPixels) {
                    throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
                }
                int step = Math.max(1, (int) Math.ceil(Math.max(width, height) / (double) derivativeLongEdge));
                ImageReadParam parameters = reader.getDefaultReadParam();
                parameters.setSourceSubsampling(step, step, 0, 0);
                reader.addIIOReadWarningListener((source, warning) -> warned.set(true));
                ScheduledFuture<?> deadline = watchdog.schedule(() -> {
                    timedOut.set(true);
                    reader.abort();
                }, decodeTimeout.toMillis(), TimeUnit.MILLISECONDS);
                BufferedImage decoded;
                try {
                    decoded = reader.read(0, parameters);
                } finally {
                    deadline.cancel(false);
                }
                if (decoded == null || timedOut.get() || warned.get()) {
                    throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
                }
                BufferedImage normalized = JpegOrientation.apply(opaqueRgb(decoded), orientation);
                return new InspectedPhoto(format, width, height, normalized.getWidth(), normalized.getHeight(),
                        jpeg(normalized));
            } catch (PhotoRejectedException rejected) {
                throw rejected;
            } catch (RuntimeException malformed) {
                // ImageIO can throw unchecked exceptions for crafted input; it is still unreadable input.
                throw new PhotoRejectedException(PhotoRejectionReason.UNREADABLE);
            } finally {
                reader.dispose();
            }
        }
    }

    private void acquireDecodeSlot() {
        try {
            if (!decodeSlots.tryAcquire(decodeTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new PhotoProcessingUnavailableException("Every photo decoding slot is busy", null);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new PhotoProcessingUnavailableException("Interrupted while waiting to decode a photo", interrupted);
        }
    }

    private static byte[] head(ReceivedPhoto photo) {
        try (InputStream content = photo.openStream()) {
            return content.readNBytes(SNIFF_BYTES);
        } catch (IOException failure) {
            throw new UncheckedIOException("The received photo could not be read", failure);
        }
    }

    private static int jpegOrientation(ReceivedPhoto photo) {
        try (InputStream content = photo.openStream()) {
            return JpegOrientation.read(content);
        } catch (IOException failure) {
            return JpegOrientation.NORMAL;
        }
    }

    static boolean isJpeg(byte[] head) {
        return head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
    }

    static boolean isPng(byte[] head) {
        return head.length >= PNG_SIGNATURE.length
                && Arrays.equals(head, 0, PNG_SIGNATURE.length, PNG_SIGNATURE, 0, PNG_SIGNATURE.length);
    }

    /** An ISO-BMFF ftyp box whose major brand, or (for mif1/msf1) a compatible brand, is an HEVC image brand. */
    static boolean isHeif(byte[] head) {
        if (head.length < 16 || !"ftyp".equals(ascii(head, 4))) {
            return false;
        }
        String majorBrand = ascii(head, 8);
        if (HEVC_IMAGE_BRANDS.contains(majorBrand)) {
            return true;
        }
        if (!STRUCTURAL_BRANDS.contains(majorBrand)) {
            return false;
        }
        long boxSize = ((head[0] & 0xFFL) << 24) | ((head[1] & 0xFFL) << 16) | ((head[2] & 0xFFL) << 8) | (head[3] & 0xFFL);
        int end = (int) Math.min(boxSize, head.length);
        for (int offset = 16; offset + 4 <= end; offset += 4) {
            if (HEVC_IMAGE_BRANDS.contains(ascii(head, offset))) {
                return true;
            }
        }
        return false;
    }

    private static String ascii(byte[] bytes, int offset) {
        return new String(bytes, offset, 4, StandardCharsets.US_ASCII);
    }

    /** Flattens transparency onto white and converts every colour model to 8-bit RGB. */
    private static BufferedImage opaqueRgb(BufferedImage decoded) {
        BufferedImage rgb = new BufferedImage(decoded.getWidth(), decoded.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            graphics.drawImage(decoded, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return rgb;
    }

    /** A baseline JPEG with no metadata at all: IIOImage without metadata writes only the JFIF header. */
    private static byte[] jpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(bytes)) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(image, null, null), parameters);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }
}
```

**Explanation.**

- **Headless graphics.** `java.awt` image classes work headless. Spring Boot sets `java.awt.headless=true` by default for applications; tests run headless on CI.
- **Validation dimensions.** Dimensions come from the header before decoding (`getWidth(0)`), so the 512-pixel rule uses the original size even though the stored derivative is downscaled.
- **Timeouts.** `ImageReader.abort()` is the documented way to stop a read; the JDK JPEG and PNG readers check it while decoding. The HEIF path bounds the native decoder separately (P38), so the worst case is two timeouts.

**Common mistakes.**

- Do not use `ImageIO.read(...)`: it ignores warnings, cannot be bounded and may cache to disk.
- Do not copy EXIF into the derivative.

### Step P40 — [ADD] `MultipartReceivedPhoto.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/MultipartReceivedPhoto.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ReceivedPhoto;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

/** The uploaded photo part as {@link ReceivedPhoto}. The client file name and declared type are ignored. */
record MultipartReceivedPhoto(MultipartFile file) implements ReceivedPhoto {

    MultipartReceivedPhoto {
        Objects.requireNonNull(file, "file");
    }

    @Override
    public long sizeBytes() {
        return file.getSize();
    }

    @Override
    public InputStream openStream() throws IOException {
        return file.getInputStream();
    }
}
```

### Step P41 — [ADD] `PhotoUploadResponse.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/PhotoUploadResponse.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import java.util.UUID;

/**
 * A staged photo (US-GAR-003). Every field is always present: contentType, width and height are null
 * until the photo is READY; garmentId is set only when ATTACHED. A staged photo is never a garment.
 */
public record PhotoUploadResponse(
        UUID id,
        String status,
        String contentType,
        Integer width,
        Integer height,
        String expiresAt,
        UUID garmentId) {

    static PhotoUploadResponse from(PhotoUpload upload) {
        return new PhotoUploadResponse(upload.id(), upload.status().name(), upload.contentType(), upload.width(),
                upload.height(), upload.expiresAt().toString(), upload.garmentId());
    }
}
```

### Step P42 — [ADD] `GarmentPhotoResponse.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/GarmentPhotoResponse.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.GarmentPhoto;

/** The attached photo's metadata; its bytes are at GET /api/v1/wardrobe/garments/{garmentId}/photo. */
public record GarmentPhotoResponse(String contentType, int width, int height) {

    static GarmentPhotoResponse from(GarmentPhoto photo) {
        return new GarmentPhotoResponse(photo.contentType(), photo.width(), photo.height());
    }
}
```

### Step P43 — [ADD] `PhotoResponses.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/PhotoResponses.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Private photo responses (S3-IMG-05): streamed by the application after the owner check, never
 * cached by shared caches, never sniffed as another type. The stream is closed after it is written.
 */
final class PhotoResponses {

    private PhotoResponses() {
    }

    static ResponseEntity<Resource> image(StoredPhoto photo) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .contentLength(photo.sizeBytes())
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(photo.content()));
    }
}
```

**Explanation.** Spring's `ResourceHttpMessageConverter` does not read an `InputStreamResource` to compute its length (the explicit `Content-Length` is used) and closes the stream after copying it. Synchronous writing avoids async dispatch, which would complicate the security chain and the tests.

### Step P44 — [MODIFY] `GarmentResponse.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/GarmentResponse.java` (the US-GAR-002 version).

BEFORE:

```java
        String createdAt,
        String revision) {
```

AFTER:

```java
        String createdAt,
        String revision,
        GarmentPhotoResponse photo) {
```

BEFORE:

```java
                garment.createdAt().toString(),
                Long.toString(garment.revision()));
    }
```

AFTER:

```java
                garment.createdAt().toString(),
                Long.toString(garment.revision()),
                garment.photo().map(GarmentPhotoResponse::from).orElse(null));
    }
```

Also extend the class Javadoc with:

```java
 * <p>{@code photo} is the attached private photo's metadata (US-GAR-003) or null; the photo itself is
 * served by GET .../{garmentId}/photo after the owner check.
```

### Step P45 — [MODIFY] `GarmentCreationRequest.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/GarmentCreationRequest.java`

BEFORE:

```java
        String brand,
        PriceRequest price) {

    AddGarmentCommand toCommand(UUID ownerAccountId, String idempotencyKey) {
        return new AddGarmentCommand(ownerAccountId, idempotencyKey, confirmed, category, subtype, color,
                materials, pattern, styles, fit, neckline, sleeveLength, length, silhouette, brand,
                price == null ? null : price.amount(),
                price == null ? null : price.currency());
    }
```

AFTER:

```java
        String brand,
        PriceRequest price,
        String photoUploadId) {

    AddGarmentCommand toCommand(UUID ownerAccountId, String idempotencyKey) {
        return new AddGarmentCommand(ownerAccountId, idempotencyKey, confirmed, category, subtype, color,
                materials, pattern, styles, fit, neckline, sleeveLength, length, silhouette, brand,
                price == null ? null : price.amount(),
                price == null ? null : price.currency(),
                photoUploadId);
    }
```

Add to the Javadoc: "`photoUploadId` optionally names the reviewed staged photo to attach (US-GAR-003); omitted or null means an imageless garment."

### Step P46 — [ADD] `WardrobePhotoUploadController.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/WardrobePhotoUploadController.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.application.CancelPhotoUploadService;
import com.capsuleai.wardrobe.application.StagePhotoCommand;
import com.capsuleai.wardrobe.application.StagePhotoUploadService;
import com.capsuleai.wardrobe.application.StagedPhoto;
import com.capsuleai.wardrobe.application.ViewPhotoUploadService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Staged garment photos of the authenticated user (US-GAR-003, S3-IMG-02/03/05). A staged photo is
 * private unconfirmed work: it never creates a garment. Confirmation is the existing garment
 * creation with {@code photoUploadId}. Every route is protected by the default-deny filter chain.
 */
@RestController
@RequestMapping("/api/v1/wardrobe/photo-uploads")
public class WardrobePhotoUploadController {

    static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    static final String PHOTO_PART = "photo";

    private final StagePhotoUploadService stagePhotoUploadService;
    private final ViewPhotoUploadService viewPhotoUploadService;
    private final CancelPhotoUploadService cancelPhotoUploadService;

    public WardrobePhotoUploadController(StagePhotoUploadService stagePhotoUploadService,
                                         ViewPhotoUploadService viewPhotoUploadService,
                                         CancelPhotoUploadService cancelPhotoUploadService) {
        this.stagePhotoUploadService = stagePhotoUploadService;
        this.viewPhotoUploadService = viewPhotoUploadService;
        this.cancelPhotoUploadService = cancelPhotoUploadService;
    }

    /** 201 when this request staged the photo; 200 with the same staged photo for a retry of it. */
    @PostMapping
    public ResponseEntity<PhotoUploadResponse> stagePhoto(
            @AuthenticationPrincipal AuthenticatedAccount owner,
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String uploadKey,
            @RequestPart(name = PHOTO_PART, required = false) MultipartFile photo) {
        StagedPhoto staged = stagePhotoUploadService.stage(new StagePhotoCommand(owner.accountId(), uploadKey,
                photo == null ? null : new MultipartReceivedPhoto(photo)));
        HttpStatus status = staged.newlyStaged() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(PhotoUploadResponse.from(staged.upload()));
    }

    /** Status review, for example after an interrupted upload or confirmation. */
    @GetMapping("/{photoUploadId}")
    public PhotoUploadResponse viewPhotoUpload(@AuthenticationPrincipal AuthenticatedAccount owner,
                                               @PathVariable String photoUploadId) {
        return PhotoUploadResponse.from(viewPhotoUploadService.view(owner.accountId(), photoUploadId));
    }

    /** The staged photo itself, for review before confirmation (AC-03). Only while READY. */
    @GetMapping("/{photoUploadId}/content")
    public ResponseEntity<Resource> photoUploadContent(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                       @PathVariable String photoUploadId) {
        return PhotoResponses.image(viewPhotoUploadService.openPending(owner.accountId(), photoUploadId));
    }

    /** Cancels an unconfirmed staged photo (AC-03, FR-AI-004). Cancelling twice is fine. */
    @DeleteMapping("/{photoUploadId}")
    public ResponseEntity<Void> cancelPhotoUpload(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                  @PathVariable String photoUploadId) {
        cancelPhotoUploadService.cancel(owner.accountId(), photoUploadId);
        return ResponseEntity.noContent().build();
    }
}
```

**Explanation.**

- **No `consumes` restriction.** A non-multipart request reaches the handler, and the scoped exception handler answers `400 MALFORMED_REQUEST` instead of a framework `415`.
- **Lazy multipart parsing.** With `spring.servlet.multipart.resolve-lazily=true` (P55), parsing happens during argument resolution. Size-limit exceptions therefore reach the scoped `WardrobeApiExceptionHandler`.

### Step P47 — [MODIFY] `WardrobeGarmentController.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/WardrobeGarmentController.java` (the US-GAR-002 version).

BEFORE:

```java
import com.capsuleai.wardrobe.application.GarmentCreation;
import com.capsuleai.wardrobe.application.GarmentRevision;
```

AFTER:

```java
import com.capsuleai.wardrobe.application.GarmentCreation;
import com.capsuleai.wardrobe.application.GarmentPhotoContentService;
import com.capsuleai.wardrobe.application.GarmentRevision;
```

BEFORE:

```java
import org.springframework.http.HttpStatus;
```

AFTER:

```java
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
```

BEFORE:

```java
    private final ReviseGarmentService reviseGarmentService;

    public WardrobeGarmentController(AddGarmentService addGarmentService,
                                     BrowseWardrobeService browseWardrobeService,
                                     InspectGarmentService inspectGarmentService,
                                     ReviseGarmentService reviseGarmentService) {
        this.addGarmentService = addGarmentService;
        this.browseWardrobeService = browseWardrobeService;
        this.inspectGarmentService = inspectGarmentService;
        this.reviseGarmentService = reviseGarmentService;
    }
```

AFTER:

```java
    private final ReviseGarmentService reviseGarmentService;
    private final GarmentPhotoContentService garmentPhotoContentService;

    public WardrobeGarmentController(AddGarmentService addGarmentService,
                                     BrowseWardrobeService browseWardrobeService,
                                     InspectGarmentService inspectGarmentService,
                                     ReviseGarmentService reviseGarmentService,
                                     GarmentPhotoContentService garmentPhotoContentService) {
        this.addGarmentService = addGarmentService;
        this.browseWardrobeService = browseWardrobeService;
        this.inspectGarmentService = inspectGarmentService;
        this.reviseGarmentService = reviseGarmentService;
        this.garmentPhotoContentService = garmentPhotoContentService;
    }
```

BEFORE (the last method's closing and the class end):

```java
        GarmentRevision revision = reviseGarmentService.revise(request.toCommand(owner.accountId(), garmentId));
        return GarmentResponse.from(revision.garment());
    }
}
```

AFTER:

```java
        GarmentRevision revision = reviseGarmentService.revise(request.toCommand(owner.accountId(), garmentId));
        return GarmentResponse.from(revision.garment());
    }

    /**
     * US-GAR-003 — the garment's private photo, streamed after the current owner check (S3-IMG-05).
     * 404 GARMENT_PHOTO_UNAVAILABLE when there is none or its object is missing; the garment's
     * information stays available through GET /{garmentId} (AC-06).
     */
    @GetMapping("/{garmentId}/photo")
    public ResponseEntity<Resource> garmentPhoto(@AuthenticationPrincipal AuthenticatedAccount owner,
                                                 @PathVariable String garmentId) {
        return PhotoResponses.image(garmentPhotoContentService.open(owner.accountId(), garmentId));
    }
}
```

### Step P48 — [MODIFY] `WardrobeApiExceptionHandler.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/web/WardrobeApiExceptionHandler.java` (the US-GAR-002 version).

BEFORE:

```java
import com.capsuleai.wardrobe.application.GarmentNotFoundException;
import com.capsuleai.wardrobe.application.GarmentRevisionConflictException;
import com.capsuleai.wardrobe.application.GarmentValidationException;
import com.capsuleai.wardrobe.application.IdempotencyKeyReusedException;
```

AFTER:

```java
import com.capsuleai.wardrobe.application.GarmentNotFoundException;
import com.capsuleai.wardrobe.application.GarmentPhotoUnavailableException;
import com.capsuleai.wardrobe.application.GarmentRevisionConflictException;
import com.capsuleai.wardrobe.application.GarmentValidationException;
import com.capsuleai.wardrobe.application.IdempotencyKeyReusedException;
import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.PhotoUploadKeyReusedException;
import com.capsuleai.wardrobe.application.PhotoUploadNotFoundException;
import com.capsuleai.wardrobe.application.PhotoUploadNotUsableException;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
```

BEFORE:

```java
import org.springframework.transaction.CannotCreateTransactionException;
```

AFTER:

```java
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
```

BEFORE:

```java
    @ExceptionHandler(HttpMessageNotReadableException.class)
```

AFTER:

```java
    /** US-GAR-003 (FR-AI-012, ERR-AI-001): the photo was not accepted; replace it or continue manually. */
    @ExceptionHandler(PhotoRejectedException.class)
    public ResponseEntity<ProblemDetail> handlePhotoRejected(PhotoRejectedException exception) {
        String detail = switch (exception.reason()) {
            case UNSUPPORTED_FORMAT -> "This file is not a JPEG, PNG or HEIC/HEIF photo.";
            case TOO_LARGE -> "This photo is larger than 15 MB.";
            case TOO_SMALL -> "This photo is too small: its shorter side must be at least 512 pixels.";
            case UNREADABLE -> "This photo could not be read completely.";
            case MULTIPLE_IMAGES -> "This photo file contains several images; choose a single photo.";
        };
        return respond(problem(HttpStatus.UNPROCESSABLE_CONTENT, "PHOTO_" + exception.reason().name(),
                detail + " Choose another photo or continue without one; nothing was saved."));
    }

    /** A request larger than the multipart transport limit is the same "too large" answer (OPEN-S3-09). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleUploadTooLarge(MaxUploadSizeExceededException exception) {
        return handlePhotoRejected(new PhotoRejectedException(PhotoRejectionReason.TOO_LARGE));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ProblemDetail> handleNotMultipart(MultipartException exception) {
        return respond(problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "The request must be multipart/form-data with the photo in a part named 'photo'."));
    }

    @ExceptionHandler(PhotoUploadKeyReusedException.class)
    public ResponseEntity<ProblemDetail> handlePhotoUploadKeyReused(PhotoUploadKeyReusedException exception) {
        return respond(problem(HttpStatus.UNPROCESSABLE_CONTENT, "PHOTO_UPLOAD_KEY_REUSED",
                "This Idempotency-Key was already used for a different photo. Use a new key for a new photo; "
                        + "nothing was saved."));
    }

    /** The same answer for an unknown id and another account's staged photo (S3-IMG-05). */
    @ExceptionHandler(PhotoUploadNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePhotoUploadNotFound(PhotoUploadNotFoundException exception) {
        return respond(problem(HttpStatus.NOT_FOUND, "PHOTO_UPLOAD_NOT_FOUND",
                "This photo is not among your staged photos. Choose the photo again."));
    }

    @ExceptionHandler(PhotoUploadNotUsableException.class)
    public ResponseEntity<ProblemDetail> handlePhotoUploadNotUsable(PhotoUploadNotUsableException exception) {
        return respond(problem(HttpStatus.CONFLICT, "PHOTO_UPLOAD_NOT_USABLE",
                "This photo can no longer be used here: it was cancelled, expired, is still being received or "
                        + "already belongs to a garment. Check the photo's status or choose it again."));
    }

    /** AC-06: only the photo is unavailable; the garment's information is not affected. */
    @ExceptionHandler(GarmentPhotoUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleGarmentPhotoUnavailable(GarmentPhotoUnavailableException exception) {
        return respond(problem(HttpStatus.NOT_FOUND, "GARMENT_PHOTO_UNAVAILABLE",
                "No photo can be shown for this garment. Its information is unchanged."));
    }

    /** Storage or decoding capacity unavailable: the outcome is not confirmed, as for a database outage. */
    @ExceptionHandler({PhotoStorageUnavailableException.class, PhotoProcessingUnavailableException.class})
    public ResponseEntity<ProblemDetail> handleMediaUnavailable(RuntimeException exception) {
        log.error("Wardrobe photo operation could not be completed ({})", exception.getClass().getSimpleName());
        return outcomeNotConfirmed();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
```

**Explanation.**

- **Most specific handler.** Spring picks the most specific `@ExceptionHandler`, so `MaxUploadSizeExceededException` (a `MultipartException` subclass) gets `422 PHOTO_TOO_LARGE` and other multipart problems get `400`.
- **Logging.** The media 503 logs only the exception class: no object key, bucket or path.
- **Media types.** Problem responses on the image routes are `application/problem+json`. Clients must accept both, for example `Accept: image/jpeg, application/problem+json`; an `Accept` that excludes problem JSON makes Spring answer `406` for errors.

### Step P49 — [ADD] `PhotoUploadCleanupJob.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/adapter/in/scheduling/PhotoUploadCleanupJob.java`

```java
package com.capsuleai.wardrobe.adapter.in.scheduling;

import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService.CleanupResult;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Runs the pending-photo lifecycle periodically inside the application (S3-IMG-08); no external
 * scheduler or broker. A failed run is logged and the next run continues from durable state.
 */
public class PhotoUploadCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(PhotoUploadCleanupJob.class);

    private final PhotoUploadCleanupService cleanup;

    public PhotoUploadCleanupJob(PhotoUploadCleanupService cleanup) {
        this.cleanup = Objects.requireNonNull(cleanup, "cleanup");
    }

    @Scheduled(initialDelayString = "${capsuleai.wardrobe.photo-upload.cleanup.initial-delay}",
               fixedDelayString = "${capsuleai.wardrobe.photo-upload.cleanup.interval}")
    public void run() {
        try {
            CleanupResult result = cleanup.run();
            if (result.expired() + result.deleted() + result.failed() > 0) {
                log.info("Photo cleanup expired {} staged photos, deleted {} objects, {} deletions to retry",
                        result.expired(), result.deleted(), result.failed());
            }
        } catch (RuntimeException failure) {
            log.warn("Photo cleanup failed and will run again at its next interval ({})",
                    failure.getClass().getSimpleName());
        }
    }
}
```

### Step P50 — [ADD] `WardrobePhotoProperties.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobePhotoProperties.java`

```java
package com.capsuleai.wardrobe.configuration;

import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * US-GAR-003 configuration: {@code capsuleai.wardrobe.photo.*} (photo rules and decoding bounds),
 * {@code capsuleai.wardrobe.photo-upload.*} (pending lifecycle) and
 * {@code capsuleai.wardrobe.photo-storage.*} (private bucket). Values marked provisional in
 * application.properties await approval or verification (OPEN-S3-07/08/09/10/12/13).
 */
@ConfigurationProperties("capsuleai.wardrobe")
public record WardrobePhotoProperties(Photo photo, Upload photoUpload, Storage photoStorage) {

    public WardrobePhotoProperties {
        Objects.requireNonNull(photo, "capsuleai.wardrobe.photo.* must be configured");
        Objects.requireNonNull(photoUpload, "capsuleai.wardrobe.photo-upload.* must be configured");
        Objects.requireNonNull(photoStorage, "capsuleai.wardrobe.photo-storage.* must be configured");
    }

    public record Photo(
            long maximumBytes,
            int minimumShortestSide,
            int derivativeLongEdge,
            long maximumDeclaredPixels,
            Duration decodeTimeout,
            int maximumConcurrentDecodes,
            String heifDecoderCommand) {

        public Photo {
            Objects.requireNonNull(decodeTimeout, "capsuleai.wardrobe.photo.decode-timeout");
            Objects.requireNonNull(heifDecoderCommand, "capsuleai.wardrobe.photo.heif-decoder-command");
        }
    }

    public record Upload(Duration pendingTtl, Duration inFlightFence, Cleanup cleanup) {

        public Upload {
            Objects.requireNonNull(pendingTtl, "capsuleai.wardrobe.photo-upload.pending-ttl");
            Objects.requireNonNull(inFlightFence, "capsuleai.wardrobe.photo-upload.in-flight-fence");
            Objects.requireNonNull(cleanup, "capsuleai.wardrobe.photo-upload.cleanup.*");
        }
    }

    public record Cleanup(boolean enabled, Duration initialDelay, Duration interval, int batchSize,
                          Duration retryDelay) {

        public Cleanup {
            Objects.requireNonNull(retryDelay, "capsuleai.wardrobe.photo-upload.cleanup.retry-delay");
        }
    }

    public record Storage(
            String bucket,
            String region,
            String endpoint,
            String accessKeyId,
            String secretAccessKey,
            Duration callTimeout,
            boolean conditionalWrites,
            boolean pathStyle) {

        public Storage {
            Objects.requireNonNull(region, "capsuleai.wardrobe.photo-storage.region");
            Objects.requireNonNull(callTimeout, "capsuleai.wardrobe.photo-storage.call-timeout");
        }

        /** Never print credentials, not even when debugging configuration. */
        @Override
        public String toString() {
            return "Storage[bucket=" + bucket + ", region=" + region + ", endpoint=" + endpoint
                    + ", credentials=" + (accessKeyId == null || accessKeyId.isBlank() ? "default chain" : "static")
                    + ", callTimeout=" + callTimeout + ", conditionalWrites=" + conditionalWrites
                    + ", pathStyle=" + pathStyle + "]";
        }
    }
}
```

### Step P51 — [ADD] `WardrobePhotoConfiguration.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobePhotoConfiguration.java`

```java
package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.out.imaging.HeifDecoder;
import com.capsuleai.wardrobe.adapter.out.imaging.ImageIoPhotoInspector;
import com.capsuleai.wardrobe.adapter.out.storage.S3PhotoStorage;
import com.capsuleai.wardrobe.application.CancelPhotoUploadService;
import com.capsuleai.wardrobe.application.GarmentPhotoContentService;
import com.capsuleai.wardrobe.application.InspectGarmentService;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.application.PhotoUploadPolicy;
import com.capsuleai.wardrobe.application.StagePhotoUploadService;
import com.capsuleai.wardrobe.application.ViewPhotoUploadService;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Wires US-GAR-003's private photo capability: the S3 client and storage adapter, the bounded
 * decoder, the policy and the photo use cases. Creating the S3 client opens no connection, so the
 * application starts even when storage is not configured; photo operations then answer 503.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WardrobePhotoProperties.class)
public class WardrobePhotoConfiguration {

    @Bean
    PhotoUploadPolicy photoUploadPolicy(WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Photo photo = properties.photo();
        WardrobePhotoProperties.Upload upload = properties.photoUpload();
        Duration slowestStore = properties.photoStorage().callTimeout().plus(photo.decodeTimeout().multipliedBy(2));
        if (upload.inFlightFence().compareTo(slowestStore) <= 0) {
            throw new IllegalStateException("capsuleai.wardrobe.photo-upload.in-flight-fence must be longer than "
                    + "the storage call timeout plus twice the decode timeout (" + slowestStore + ")");
        }
        return new PhotoUploadPolicy(photo.maximumBytes(), photo.minimumShortestSide(), upload.pendingTtl(),
                upload.inFlightFence(), upload.cleanup().batchSize(), upload.cleanup().retryDelay());
    }

    @Bean
    S3Client photoStorageClient(WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Storage storage = properties.photoStorage();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(storage.region()))
                .forcePathStyle(storage.pathStyle())
                .overrideConfiguration(configuration -> configuration
                        .apiCallTimeout(storage.callTimeout())
                        .apiCallAttemptTimeout(storage.callTimeout()));
        if (hasText(storage.endpoint())) {
            builder.endpointOverride(URI.create(storage.endpoint().strip()));
        }
        if (hasText(storage.accessKeyId())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(storage.accessKeyId(), storage.secretAccessKey())));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
        }
        return builder.build();
    }

    @Bean
    PhotoStorage photoStorage(S3Client photoStorageClient, WardrobePhotoProperties properties) {
        return new S3PhotoStorage(photoStorageClient, properties.photoStorage().bucket(),
                properties.photoStorage().conditionalWrites());
    }

    @Bean
    HeifDecoder heifDecoder(WardrobePhotoProperties properties) {
        return new HeifDecoder(properties.photo().heifDecoderCommand(), properties.photo().decodeTimeout());
    }

    @Bean
    PhotoInspector photoInspector(HeifDecoder heifDecoder, WardrobePhotoProperties properties) {
        WardrobePhotoProperties.Photo photo = properties.photo();
        return new ImageIoPhotoInspector(heifDecoder, photo.derivativeLongEdge(), photo.maximumDeclaredPixels(),
                photo.decodeTimeout(), photo.maximumConcurrentDecodes());
    }

    @Bean
    StagePhotoUploadService stagePhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage,
                                                    PhotoInspector inspector, WardrobeTransactions transactions,
                                                    Clock clock, PhotoUploadPolicy policy) {
        return new StagePhotoUploadService(uploads, storage, inspector, transactions, clock, policy);
    }

    @Bean
    ViewPhotoUploadService viewPhotoUploadService(PhotoUploadRepository uploads, PhotoStorage storage, Clock clock) {
        return new ViewPhotoUploadService(uploads, storage, clock);
    }

    @Bean
    CancelPhotoUploadService cancelPhotoUploadService(PhotoUploadRepository uploads,
                                                      WardrobeTransactions transactions, Clock clock,
                                                      PhotoUploadPolicy policy) {
        return new CancelPhotoUploadService(uploads, transactions, clock, policy);
    }

    @Bean
    GarmentPhotoContentService garmentPhotoContentService(InspectGarmentService garments,
                                                          PhotoUploadRepository uploads, PhotoStorage storage) {
        return new GarmentPhotoContentService(garments, uploads, storage);
    }

    @Bean
    PhotoUploadCleanupService photoUploadCleanupService(PhotoUploadRepository uploads, PhotoStorage storage,
                                                        WardrobeTransactions transactions, Clock clock,
                                                        PhotoUploadPolicy policy) {
        return new PhotoUploadCleanupService(uploads, storage, transactions, clock, policy);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
```

**Explanation.**

- **Startup check.** The fence check makes an unsafe configuration fail at startup instead of creating late orphans.
- **Shutdown.** Spring infers `close()` as the destroy method of `S3Client` and `ImageIoPhotoInspector`, so connections and the watchdog thread end with the context.

### Step P52 — [ADD] `WardrobePhotoCleanupConfiguration.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobePhotoCleanupConfiguration.java`

```java
package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.in.scheduling.PhotoUploadCleanupJob;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Schedules the Wardrobe-owned pending-photo lifecycle (S3-IMG-08) with Spring's in-process scheduler.
 * Disabled with {@code capsuleai.wardrobe.photo-upload.cleanup.enabled=false}; the test profile does
 * this and calls {@link PhotoUploadCleanupService} directly.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "capsuleai.wardrobe.photo-upload.cleanup.enabled", havingValue = "true")
public class WardrobePhotoCleanupConfiguration {

    @Bean
    PhotoUploadCleanupJob photoUploadCleanupJob(PhotoUploadCleanupService cleanup) {
        return new PhotoUploadCleanupJob(cleanup);
    }
}
```

### Step P53 — [MODIFY] `WardrobeConfiguration.java`

**Path:** `backend/src/main/java/com/capsuleai/wardrobe/configuration/WardrobeConfiguration.java` (the US-GAR-002 version).

BEFORE:

```java
import com.capsuleai.wardrobe.adapter.out.persistence.JdbcGarmentRepository;
```

AFTER:

```java
import com.capsuleai.wardrobe.adapter.out.persistence.JdbcGarmentRepository;
import com.capsuleai.wardrobe.adapter.out.persistence.JdbcPhotoUploadRepository;
```

BEFORE:

```java
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
```

AFTER:

```java
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
```

BEFORE:

```java
    @Bean
    AddGarmentService addGarmentService(GarmentRepository garments, WardrobeTransactions transactions, Clock clock) {
        return new AddGarmentService(garments, transactions, clock);
    }
```

AFTER:

```java
    @Bean
    PhotoUploadRepository photoUploadRepository(JdbcClient jdbcClient) {
        return new JdbcPhotoUploadRepository(jdbcClient);
    }

    @Bean
    AddGarmentService addGarmentService(GarmentRepository garments, PhotoUploadRepository photoUploads,
                                        WardrobeTransactions transactions, Clock clock) {
        return new AddGarmentService(garments, photoUploads, transactions, clock);
    }
```

### Step P54 — [MODIFY] `IdentitySecurityConfiguration.java`

**Path:** `backend/src/main/java/com/capsuleai/identity/configuration/IdentitySecurityConfiguration.java` (after US-GAR-002).

BEFORE:

```java
        // PATCH: US-GAR-002 garment edits (and US-PROF-001 preference revisions); OPEN-S3-01.
        corsConfiguration.setAllowedMethods(
                List.of(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PATCH.name()));
```

AFTER:

```java
        // PATCH: US-GAR-002 garment edits (and US-PROF-001 preference revisions); DELETE: US-GAR-003
        // cancellation of a staged photo. OPEN-S3-01.
        corsConfiguration.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(),
                HttpMethod.PATCH.name(), HttpMethod.DELETE.name()));
```

**Merge rule.** If US-GAR-002 is not applied yet, the BEFORE line is the original `List.of(HttpMethod.GET.name(), HttpMethod.POST.name())`. Keep the union of methods in the order `GET, POST, PATCH, DELETE` ([shared architecture §21](shared-architecture.md#21-shared-file-ownership-and-cumulative-order)).

### Step P55 — [MODIFY] `application.properties`

**Path:** `backend/src/main/resources/application.properties`. Append at the end of the file:

```properties

# US-GAR-003: photos arrive as multipart through the application (S3-IMG-02). These servlet limits
# are only a transport guard above the photo rule; the application enforces the exact rule itself.
# resolve-lazily lets size errors reach the Wardrobe error handler; max-swallow-size lets clients
# read the 422 instead of a reset connection.
spring.servlet.multipart.max-file-size=16MB
spring.servlet.multipart.max-request-size=17MB
spring.servlet.multipart.resolve-lazily=true
server.tomcat.max-swallow-size=20MB

# US-GAR-003 photo rules (FR-AI-012). Provisional until approved or verified:
# maximum-bytes (OPEN-S3-09), derivative-long-edge (OPEN-S3-10), decoding bounds (OPEN-S3-12).
capsuleai.wardrobe.photo.maximum-bytes=15000000
capsuleai.wardrobe.photo.minimum-shortest-side=512
capsuleai.wardrobe.photo.derivative-long-edge=2048
capsuleai.wardrobe.photo.maximum-declared-pixels=250000000
capsuleai.wardrobe.photo.decode-timeout=PT20S
capsuleai.wardrobe.photo.maximum-concurrent-decodes=2
# OPEN-S3-08: libheif's HEIF decoder (verification required). Some distributions name it heif-convert.
capsuleai.wardrobe.photo.heif-decoder-command=${CAPSULE_HEIF_DECODER:heif-dec}

# US-GAR-003 pending-photo lifecycle (S3-IMG-08). The pending TTL is PROVISIONAL and awaits the
# Product Owner's decision (OPEN-S3-07). The fence must exceed the storage call timeout plus twice
# the decode timeout; startup fails otherwise.
capsuleai.wardrobe.photo-upload.pending-ttl=PT24H
capsuleai.wardrobe.photo-upload.in-flight-fence=PT5M
capsuleai.wardrobe.photo-upload.cleanup.enabled=true
capsuleai.wardrobe.photo-upload.cleanup.initial-delay=PT1M
capsuleai.wardrobe.photo-upload.cleanup.interval=PT5M
capsuleai.wardrobe.photo-upload.cleanup.batch-size=100
capsuleai.wardrobe.photo-upload.cleanup.retry-delay=PT10M

# US-GAR-003 private photo bucket (S3-IMG-01, OPEN-S3-13). Empty endpoint means Amazon S3; empty
# access key means the AWS default credential chain. Never commit real credentials.
capsuleai.wardrobe.photo-storage.bucket=${CAPSULE_PHOTO_BUCKET:}
capsuleai.wardrobe.photo-storage.region=${CAPSULE_PHOTO_REGION:ap-southeast-1}
capsuleai.wardrobe.photo-storage.endpoint=${CAPSULE_PHOTO_ENDPOINT:}
capsuleai.wardrobe.photo-storage.access-key-id=${CAPSULE_PHOTO_ACCESS_KEY_ID:}
capsuleai.wardrobe.photo-storage.secret-access-key=${CAPSULE_PHOTO_SECRET_ACCESS_KEY:}
capsuleai.wardrobe.photo-storage.call-timeout=PT30S
capsuleai.wardrobe.photo-storage.conditional-writes=true
capsuleai.wardrobe.photo-storage.path-style=false
```

**Notes.**

- **Data sizes.** Spring `DataSize` treats `MB` as 1,048,576 bytes, so `16MB` is above the 15,000,000-byte rule.
- **Region.** `ap-southeast-1` is a placeholder default; the deployed region is an S3-IMG-01 deployment decision and is not approved here.

### Step P56 — [MODIFY] `application-local.properties`

**Path:** `backend/src/main/resources/application-local.properties`. Append:

```properties

# US-GAR-003: the local S3Mock service from compose.yaml. Placeholder credentials only (S3Mock does
# not check them); this is not the production provider (OPEN-S3-13).
capsuleai.wardrobe.photo-storage.bucket=${CAPSULE_PHOTO_BUCKET:capsuleai-photos-local}
capsuleai.wardrobe.photo-storage.region=${CAPSULE_PHOTO_REGION:us-east-1}
capsuleai.wardrobe.photo-storage.endpoint=${CAPSULE_PHOTO_ENDPOINT:http://localhost:9090}
capsuleai.wardrobe.photo-storage.access-key-id=${CAPSULE_PHOTO_ACCESS_KEY_ID:local-only}
capsuleai.wardrobe.photo-storage.secret-access-key=${CAPSULE_PHOTO_SECRET_ACCESS_KEY:local-only}
capsuleai.wardrobe.photo-storage.path-style=true
```

### Step P57 — [MODIFY] `compose.yaml`

**Path:** `compose.yaml`

BEFORE:

```yaml
    ports:
      - "127.0.0.1:1025:1025"
      - "127.0.0.1:8025:8025"

volumes:
```

AFTER:

```yaml
    ports:
      - "127.0.0.1:1025:1025"
      - "127.0.0.1:8025:8025"

  # US-GAR-003 (S3-IMG-01, OPEN-S3-13): local S3-compatible private photo storage for development
  # only. Objects live inside the container; this is not the production provider. Both variable
  # spellings create the bucket; which one 5.1.0 honours is part of verification V-S3.
  s3mock:
    image: adobe/s3mock:5.1.0

    environment:
      COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS: capsuleai-photos-local
      initialBuckets: capsuleai-photos-local

    ports:
      - "127.0.0.1:9090:9090"

volumes:
```

### Step P58 — [MODIFY] `docs/05-api/openapi.yaml` (0.9.0 → 0.10.0)

Change 1. BEFORE `  version: 0.9.0`, AFTER `  version: 0.10.0`.

Change 2. BEFORE:

```yaml
      - US-GAR-002: Correct and enrich garment information
```

AFTER:

```yaml
      - US-GAR-002: Correct and enrich garment information
      - US-GAR-003: Add a photo-supported garment with manual confirmation
```

Change 3. BEFORE:

```yaml
    eligibility is not evaluated in this release, and no garment
    image is stored.
```

AFTER:

```yaml
    eligibility is not evaluated in this release. A new garment
    may carry one private photo (US-GAR-003): the photo is staged
    first and becomes part of a garment only through an explicitly
    confirmed garment creation. Photos are served only to their
    owner, through the application.
```

Change 4. BEFORE:

```yaml
    session listing, all-device logout, garment removal,
    garment images, search, filtering or user profiles.
```

AFTER:

```yaml
    session listing, all-device logout, garment removal,
    attaching a photo to an existing garment, replacing a
    confirmed photo, search, filtering or user profiles.
```

Change 5: the creation example and responses. BEFORE:

```yaml
                createdAt: "2026-11-05T09:30:00.123456Z"

        "200":
          description: |
            The same logical addition was already accepted; this is
            that garment. Nothing new was created.
```

AFTER:

```yaml
                createdAt: "2026-11-05T09:30:00.123456Z"
                revision: "1"
                photo: null

        "200":
          description: |
            The same logical addition was already accepted; this is
            that garment as it is now (it may have been edited since).
            Nothing new was created.
```

BEFORE:

```yaml
        "422":
          $ref: "#/components/responses/IdempotencyKeyReused"

        "503":
          $ref: "#/components/responses/Unavailable"
```

AFTER:

```yaml
        "404":
          $ref: "#/components/responses/PhotoUploadNotFound"

        "409":
          $ref: "#/components/responses/PhotoUploadNotUsable"

        "422":
          $ref: "#/components/responses/IdempotencyKeyReused"

        "503":
          $ref: "#/components/responses/Unavailable"
```

Change 6: four new paths, inserted before `components:`. BEFORE (end of the PATCH operation from US-GAR-002):

```yaml
        "503":
          $ref: "#/components/responses/Unavailable"

components:
```

AFTER:

```yaml
        "503":
          $ref: "#/components/responses/Unavailable"

  /api/v1/wardrobe/garments/{garmentId}/photo:
    get:
      tags:
        - Wardrobe
      operationId: getGarmentPhoto
      summary: The private photo of one owned garment
      description: |
        US-GAR-003 (AC-06/AC-07, S3-IMG-05). Streams the garment's
        photo after the current owner check. The photo is a
        normalized JPEG without metadata. Responses are private and
        not stored by caches. 404 GARMENT_PHOTO_UNAVAILABLE means
        the garment has no photo or its photo is missing; the
        garment's information is unaffected. Send
        Accept: image/jpeg, application/problem+json.
      parameters:
        - name: garmentId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        "200":
          description: The photo.
          headers:
            Cache-Control:
              schema:
                type: string
              description: "no-store, private"
          content:
            image/jpeg:
              schema:
                type: string
                format: binary
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/GarmentPhotoUnavailable"
        "410":
          $ref: "#/components/responses/GarmentNoLongerCurrent"
        "503":
          $ref: "#/components/responses/Unavailable"

  /api/v1/wardrobe/photo-uploads:
    post:
      tags:
        - Wardrobe
      operationId: stagePhoto
      summary: Stage one private garment photo for review
      description: |
        US-GAR-003 (FR-AI-001/002/012, S3-IMG-02/03/04/06). Uploads
        one photo. The bytes must genuinely be JPEG, PNG or
        HEIC/HEIF (file names and declared types are ignored), at
        most 15,000,000 bytes, with the shorter side of the
        original at least 512 pixels. A non-plain background is
        fine. The stored photo is a normalized JPEG: orientation
        applied, metadata (including location) removed, longer
        side at most 2048 pixels.

        Staging creates no garment. Confirm the garment with
        POST /api/v1/wardrobe/garments and photoUploadId before
        expiresAt; unconfirmed photos are deleted after they
        expire. Repeat a lost upload with the same Idempotency-Key
        and the same photo to get the same staged photo (200).
      parameters:
        - name: Idempotency-Key
          in: header
          required: true
          description: A UUID the client generates once per intended upload and repeats on retry.
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              required:
                - photo
              properties:
                photo:
                  type: string
                  format: binary
      responses:
        "201":
          description: The photo was staged and can be reviewed and confirmed.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PhotoUpload"
        "200":
          description: A retry of the same staging; this is that staged photo, in its current status.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PhotoUpload"
        "400":
          $ref: "#/components/responses/BadRequest"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "409":
          $ref: "#/components/responses/PhotoUploadNotUsable"
        "422":
          $ref: "#/components/responses/PhotoRejected"
        "503":
          $ref: "#/components/responses/Unavailable"

  /api/v1/wardrobe/photo-uploads/{photoUploadId}:
    get:
      tags:
        - Wardrobe
      operationId: getPhotoUpload
      summary: Review a staged photo's status
      description: |
        US-GAR-003 (AC-06). The status of one of the account's
        staged photos, for example after an interrupted upload or
        confirmation: RECEIVING, READY, ATTACHED (with garmentId)
        or DISCARDED (cancelled, expired or failed).
      parameters:
        - $ref: "#/components/parameters/PhotoUploadId"
      responses:
        "200":
          description: The staged photo.
          content:
            application/json:
              schema:
                $ref: "#/components/schemas/PhotoUpload"
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/PhotoUploadNotFound"
        "503":
          $ref: "#/components/responses/Unavailable"
    delete:
      tags:
        - Wardrobe
      operationId: cancelPhotoUpload
      summary: Cancel an unconfirmed staged photo
      description: |
        US-GAR-003 (AC-03, FR-AI-004/009). The staged photo becomes
        unusable at once and is deleted. Cancelling again succeeds.
        A photo that already belongs to a garment cannot be
        cancelled (409).
      parameters:
        - $ref: "#/components/parameters/PhotoUploadId"
      responses:
        "204":
          description: Cancelled (or already cancelled or expired).
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/PhotoUploadNotFound"
        "409":
          $ref: "#/components/responses/PhotoUploadNotUsable"
        "503":
          $ref: "#/components/responses/Unavailable"

  /api/v1/wardrobe/photo-uploads/{photoUploadId}/content:
    get:
      tags:
        - Wardrobe
      operationId: getPhotoUploadContent
      summary: The staged photo itself, for review
      description: |
        US-GAR-003 (AC-03, S3-IMG-05). Available only while the
        staged photo is READY. Private; not stored by caches. Send
        Accept: image/jpeg, application/problem+json.
      parameters:
        - $ref: "#/components/parameters/PhotoUploadId"
      responses:
        "200":
          description: The staged photo.
          content:
            image/jpeg:
              schema:
                type: string
                format: binary
        "401":
          $ref: "#/components/responses/Unauthorized"
        "404":
          $ref: "#/components/responses/PhotoUploadNotFound"
        "409":
          $ref: "#/components/responses/PhotoUploadNotUsable"
        "503":
          $ref: "#/components/responses/Unavailable"

components:
```

Change 7: a path parameter. BEFORE:

```yaml
components:
  parameters:
```

AFTER:

```yaml
components:
  parameters:
    PhotoUploadId:
      name: photoUploadId
      in: path
      required: true
      description: The id of one of the account's staged photos.
      schema:
        type: string
        format: uuid

```

Change 8: `photoUploadId` in the creation request. BEFORE:

```yaml
        price:
          anyOf:
            - $ref: "#/components/schemas/Price"
            - type: "null"

    Price:
      type: object
```

AFTER:

```yaml
        price:
          anyOf:
            - $ref: "#/components/schemas/Price"
            - type: "null"
        photoUploadId:
          type: [string, "null"]
          format: uuid
          description: |
            US-GAR-003: the reviewed staged photo to attach. Omit or
            null for a garment without a photo. A photo that cannot
            be used fails the request (404 or 409); it never falls
            back to a garment without a photo.

    Price:
      type: object
```

Change 9: `photo` in `Garment`. BEFORE:

```yaml
        - recordedUse
        - createdAt
        - revision
      properties:
```

AFTER:

```yaml
        - recordedUse
        - createdAt
        - revision
        - photo
      properties:
```

BEFORE:

```yaml
            editing. It changes only when an edit changes the profile.

    RecordedUse:
```

AFTER:

```yaml
            editing. It changes only when an edit changes the profile.
        photo:
          anyOf:
            - $ref: "#/components/schemas/GarmentPhoto"
            - type: "null"

    GarmentPhoto:
      type: object
      description: |
        The garment's private photo (US-GAR-003); its bytes are at
        GET /api/v1/wardrobe/garments/{garmentId}/photo. Media
        metadata, not a garment property.
      required:
        - contentType
        - width
        - height
      properties:
        contentType:
          type: string
          const: image/jpeg
        width:
          type: integer
          minimum: 1
        height:
          type: integer
          minimum: 1

    PhotoUpload:
      type: object
      description: |
        A staged photo (US-GAR-003). Every field is always present.
        A staged photo is never a garment.
      required:
        - id
        - status
        - contentType
        - width
        - height
        - expiresAt
        - garmentId
      properties:
        id:
          type: string
          format: uuid
        status:
          type: string
          enum: [RECEIVING, READY, ATTACHED, DISCARDED]
        contentType:
          type: [string, "null"]
          description: image/jpeg once READY; null before.
        width:
          type: [integer, "null"]
        height:
          type: [integer, "null"]
        expiresAt:
          type: string
          format: date-time
          description: |
            When an unconfirmed photo stops being usable and is
            deleted. Provisional pending lifetime 24 hours,
            awaiting Product Owner approval (OPEN-S3-07).
        garmentId:
          type: [string, "null"]
          format: uuid
          description: The garment this photo belongs to, once ATTACHED.

    RecordedUse:
```

Change 10: five new responses. BEFORE `    GarmentNotFound:` (the response, not the schema), AFTER:

```yaml
    PhotoRejected:
      description: |
        The photo was not accepted and nothing was stored. Choose
        another photo or continue without one. Codes:
        PHOTO_UNSUPPORTED_FORMAT, PHOTO_TOO_LARGE (above
        15,000,000 bytes), PHOTO_TOO_SMALL (shorter side below 512
        pixels), PHOTO_UNREADABLE, PHOTO_MULTIPLE_IMAGES, and
        PHOTO_UPLOAD_KEY_REUSED (the Idempotency-Key was used for a
        different photo).
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/Problem"
          example:
            title: "Unprocessable Content"
            status: 422
            detail: "This photo is too small: its shorter side must be at least 512 pixels. Choose another photo or continue without one; nothing was saved."
            instance: "/api/v1/wardrobe/photo-uploads"
            code: "PHOTO_TOO_SMALL"

    PhotoUploadNotFound:
      description: |
        No staged photo with this id belongs to the account (unknown,
        another account's or malformed; indistinguishable).
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/Problem"
          example:
            title: "Not Found"
            status: 404
            detail: "This photo is not among your staged photos. Choose the photo again."
            code: "PHOTO_UPLOAD_NOT_FOUND"

    PhotoUploadNotUsable:
      description: |
        The staged photo cannot be used for this action: still being
        received, cancelled, expired or already attached to a
        garment. Nothing changed. Check its status with
        GET /api/v1/wardrobe/photo-uploads/{photoUploadId}.
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/Problem"
          example:
            title: "Conflict"
            status: 409
            detail: "This photo can no longer be used here: it was cancelled, expired, is still being received or already belongs to a garment. Check the photo's status or choose it again."
            code: "PHOTO_UPLOAD_NOT_USABLE"

    GarmentPhotoUnavailable:
      description: |
        GARMENT_NOT_FOUND: no such garment in the account's wardrobe.
        GARMENT_PHOTO_UNAVAILABLE: the garment has no photo, or its
        photo is missing; the garment's information is unchanged.
      content:
        application/problem+json:
          schema:
            $ref: "#/components/schemas/Problem"
          example:
            title: "Not Found"
            status: 404
            detail: "No photo can be shown for this garment. Its information is unchanged."
            code: "GARMENT_PHOTO_UNAVAILABLE"

    GarmentNotFound:
```

**Explanation.** T20 extends `IdentityOpenApiContractIT` so that:

- multipart parts are compared with the handler's `@RequestPart` names;
- `image/jpeg` successes require a `Resource` response body;
- the new `204` is checked against `ResponseEntity<Void>`;
- all six new operations still prove a missing token is `401`.

The `400` of `stagePhoto` reuses the existing `BadRequest` response (`VALIDATION_FAILED` / `MALFORMED_REQUEST`).

**Common mistake.** The garment-photo path must be a separate path item (`/{garmentId}/photo`), not an operation inside `/{garmentId}`.

### Step P59 — [MODIFY] `.github/workflows/backend-ci.yml`

Change 1: install and verify the HEIF decoder (OPEN-S3-08). BEFORE:

```yaml
      - name: Build and run required verification
        run: bash ./mvnw --batch-mode --no-transfer-progress clean verify
```

AFTER:

```yaml
      - name: Install the HEIF decoder and encoder used by photo tests (OPEN-S3-08)
        shell: bash
        run: |
          sudo apt-get update
          sudo apt-get install --yes --no-install-recommends \
            libheif-examples libheif-plugin-libde265 libheif-plugin-x265
          (heif-dec --version || heif-convert --version) | head -n 1

      - name: Build and run required verification
        env:
          CAPSULEAI_REQUIRE_HEIF: "true"
        run: bash ./mvnw --batch-mode --no-transfer-progress clean verify
```

Change 2: required reports. BEFORE:

```yaml
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.IdentityOpenApiContractIT.xml' = 5
```

AFTER:

```yaml
            'failsafe-reports/TEST-com.capsuleai.identity.adapter.in.web.IdentityOpenApiContractIT.xml' = 6
```

BEFORE:

```yaml
            'failsafe-reports/TEST-com.capsuleai.wardrobe.application.ConcurrentGarmentRevisionIT.xml' = 3
          }
```

AFTER:

```yaml
            'failsafe-reports/TEST-com.capsuleai.wardrobe.application.ConcurrentGarmentRevisionIT.xml' = 3
            'surefire-reports/TEST-com.capsuleai.wardrobe.domain.PhotoUploadTest.xml' = 4
            'surefire-reports/TEST-com.capsuleai.wardrobe.application.StagePhotoUploadServiceTest.xml' = 9
            'surefire-reports/TEST-com.capsuleai.wardrobe.application.PhotoBackedGarmentCreationTest.xml' = 8
            'surefire-reports/TEST-com.capsuleai.wardrobe.application.PhotoUploadLifecycleServiceTest.xml' = 7
            'surefire-reports/TEST-com.capsuleai.wardrobe.adapter.out.imaging.ImageIoPhotoInspectorTest.xml' = 10
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.out.imaging.HeifDecodingIT.xml' = 3
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.out.storage.S3PhotoStorageIT.xml' = 4
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.out.persistence.PhotoUploadSchemaIT.xml' = 4
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.in.web.PhotoUploadApiIT.xml' = 9
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.in.web.PhotoGarmentApiIT.xml' = 8
            'failsafe-reports/TEST-com.capsuleai.wardrobe.application.ConcurrentPhotoConfirmationIT.xml' = 2
            'failsafe-reports/TEST-com.capsuleai.wardrobe.adapter.out.persistence.PhotoUploadCleanupIT.xml' = 4
          }
```

**Explanation.**

- **Report count.** The guard now has 87 entries.
- **No skips.** `HeifDecodingIT` is required with zero skips. With `CAPSULEAI_REQUIRE_HEIF=true` a missing decoder **fails** instead of skipping, so CI cannot pass without real HEIF evidence.
- **Encoder.** `libheif-plugin-x265` provides the encoder `heif-enc` uses to create the synthetic HEIC fixtures.
- **Package names.** They are the Ubuntu 24.04 names. Confirm them in the first CI run (verification V1/V2).

### Step P60 — [MODIFY] `backend/README.md`

Change 1: insert a new subsection before `## 10. Database migrations` (after "Editing one garment" from US-GAR-002). BEFORE:

```markdown
## 10. Database migrations
```

AFTER:

````markdown
### Photo-supported garments

US-GAR-003 adds one private photo to a **new** garment in two explicit steps:

1. `POST /api/v1/wardrobe/photo-uploads`: `multipart/form-data` with the file in the part `photo`, header `Idempotency-Key` (a new UUID per intended upload). The backend checks that the bytes are genuinely JPEG, PNG or HEIC/HEIF (the file name and declared type are ignored), at most 15,000,000 bytes, with the shorter side of the original at least 512 pixels. It stores a normalized JPEG: orientation applied, all metadata including location removed, longer side at most 2048 pixels. The answer is `201` with a staged photo (`status: READY`, `expiresAt`). **Staging creates no garment.**
2. Review it with `GET /api/v1/wardrobe/photo-uploads/{id}/content`, cancel it with `DELETE /api/v1/wardrobe/photo-uploads/{id}`, or confirm the garment with the usual `POST /api/v1/wardrobe/garments` plus `"photoUploadId": "<id>"`.

Garment detail then shows `photo` (`contentType`, `width`, `height`), and `GET /api/v1/wardrobe/garments/{id}/photo` returns the image. Unconfirmed photos expire after the pending lifetime (**provisionally 24 hours, awaiting Product Owner approval**, `capsuleai.wardrobe.photo-upload.pending-ttl`) and are deleted by an in-process cleanup. Attached photos are never deleted by it.

| Outcome | Response |
| --- | --- |
| Not JPEG/PNG/HEIC/HEIF, above 15,000,000 bytes, shorter side below 512, unreadable, several images in one HEIF | `422 PHOTO_UNSUPPORTED_FORMAT` / `PHOTO_TOO_LARGE` / `PHOTO_TOO_SMALL` / `PHOTO_UNREADABLE` / `PHOTO_MULTIPLE_IMAGES`; nothing stored |
| Same upload key, different photo | `422 PHOTO_UPLOAD_KEY_REUSED` |
| Unknown or another account's staged photo | `404 PHOTO_UPLOAD_NOT_FOUND` |
| Cancelled, expired, still receiving, or already attached | `409 PHOTO_UPLOAD_NOT_USABLE` (a garment creation with it fails; it never falls back to a garment without a photo) |
| Garment without a photo, or its photo is missing | `404 GARMENT_PHOTO_UNAVAILABLE`; garment detail still works |
| Storage, decoder or database unavailable | `503 TEMPORARILY_UNAVAILABLE`; outcome not confirmed |

Image routes answer `Cache-Control: no-store, private`. Send `Accept: image/jpeg, application/problem+json`.

**Local setup.** `docker compose --env-file .env up -d` now also starts `s3mock` (Adobe S3Mock 5.1.0, development only) on `127.0.0.1:9090`, and the `local` profile points at it. HEIC/HEIF needs libheif's decoder on the PATH as `heif-dec` (or set `CAPSULE_HEIF_DECODER`):

- Ubuntu 24.04: `sudo apt-get install libheif-examples libheif-plugin-libde265`.
- Windows: install libheif via MSYS2 (`pacman -S mingw-w64-ucrt-x86_64-libheif`) and add its `bin` to PATH.

Without the decoder, HEIC uploads answer `503`, never "unsupported". The toolchain still needs the verification procedure in the Sprint 3 open questions (OPEN-S3-08).

**Deployment.** Set `CAPSULE_PHOTO_BUCKET`, `CAPSULE_PHOTO_REGION` and either an IAM role (default credential chain) or `CAPSULE_PHOTO_ACCESS_KEY_ID` / `CAPSULE_PHOTO_SECRET_ACCESS_KEY` for a least-privilege user limited to `s3:PutObject`, `s3:GetObject` and `s3:DeleteObject` on the photo prefix. The bucket must block public access. AWS account, region, budget and production deployment are **not** established by this README.

Focused tests, from `backend`:

```powershell
.\mvnw.cmd '-Dtest=PhotoUploadTest,StagePhotoUploadServiceTest,PhotoBackedGarmentCreationTest,PhotoUploadLifecycleServiceTest,ImageIoPhotoInspectorTest' test
.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PhotoUploadApiIT,PhotoGarmentApiIT,ConcurrentPhotoConfirmationIT,PhotoUploadCleanupIT,PhotoUploadSchemaIT,S3PhotoStorageIT,HeifDecodingIT' verify
```

`HeifDecodingIT` skips locally when libheif is missing, unless `CAPSULEAI_REQUIRE_HEIF=true` (as in CI).

## 10. Database migrations
````

Change 2. BEFORE:

```markdown
adds the optimistic `revision` column (starting at 1) to `wardrobe.wardrobe_garment`. Flyway maintains
```

AFTER:

```markdown
adds the optimistic `revision` column (starting at 1) to `wardrobe.wardrobe_garment`. `V10__create_wardrobe_photo_upload.sql` (US-GAR-003) creates `wardrobe.wardrobe_photo_upload`, the staged-photo lifecycle table. Flyway maintains
```

Change 3. BEFORE ``Expect successful rows for versions `1` to `9`.``, AFTER ``Expect successful rows for versions `1` to `10`.``

Change 4. BEFORE:

```markdown
Garment removal/images and all other business endpoints
```

AFTER:

```markdown
It also adds private photos for new garments (US-GAR-003; real AWS deployment not verified). Garment removal, photos for existing garments and all other business endpoints
```

## 13. Complete Automated Test Implementation

Commands run from `backend` in Windows PowerShell 5.1. Integration tests need Docker. `HeifDecodingIT` also needs libheif (or `CAPSULEAI_REQUIRE_HEIF` unset, in which case it skips).

### Step T1 — [MODIFY] `application-test.properties`

**Path:** `backend/src/test/resources/application-test.properties`. Append:

```properties

# US-GAR-003: integration tests replace photo storage with FaultInjectingPhotoStorage (IdentityTestConfiguration);
# S3PhotoStorageIT exercises the real S3 adapter against an S3Mock container. Placeholder values only.
capsuleai.wardrobe.photo-storage.bucket=capsuleai-photos-test
capsuleai.wardrobe.photo-storage.region=us-east-1
capsuleai.wardrobe.photo-storage.endpoint=http://127.0.0.1:1
capsuleai.wardrobe.photo-storage.access-key-id=test-only
capsuleai.wardrobe.photo-storage.secret-access-key=test-only
capsuleai.wardrobe.photo-storage.path-style=true
# Tests call PhotoUploadCleanupService directly instead of waiting for the scheduler.
capsuleai.wardrobe.photo-upload.cleanup.enabled=false
```

### Step T2 — [ADD] `FaultInjectingPhotoStorage.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/support/FaultInjectingPhotoStorage.java`

```java
package com.capsuleai.wardrobe.support;

import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TEST-ONLY {@link PhotoStorage}: an in-memory store that never overwrites (like a conditional write)
 * and can be told to fail stores or deletions, run a competing action just before a store, or lose
 * an object, so lifecycle and recovery paths can be driven deterministically.
 */
public final class FaultInjectingPhotoStorage implements PhotoStorage {

    private record StoredObject(byte[] content, String contentType) {
    }

    private final Map<String, StoredObject> objects = new ConcurrentHashMap<>();
    private final AtomicBoolean failStores = new AtomicBoolean();
    private final AtomicBoolean failDeletes = new AtomicBoolean();
    private final AtomicInteger deletions = new AtomicInteger();
    private final AtomicReference<Runnable> beforeNextStore = new AtomicReference<>();

    @Override
    public void store(String objectKey, byte[] content, String contentType) {
        Runnable competing = beforeNextStore.getAndSet(null);
        if (competing != null) {
            competing.run();
        }
        if (failStores.get()) {
            throw new PhotoStorageUnavailableException("simulated store failure", null);
        }
        if (objects.putIfAbsent(objectKey, new StoredObject(content.clone(), contentType)) != null) {
            throw new IllegalStateException("A new photo key already exists in storage");
        }
    }

    @Override
    public Optional<StoredPhoto> open(String objectKey) {
        return Optional.ofNullable(objects.get(objectKey)).map(stored -> new StoredPhoto(
                new ByteArrayInputStream(stored.content()), stored.content().length, stored.contentType()));
    }

    @Override
    public void delete(String objectKey) {
        if (failDeletes.get()) {
            throw new PhotoStorageUnavailableException("simulated delete failure", null);
        }
        deletions.incrementAndGet();
        objects.remove(objectKey);
    }

    public void failStores(boolean fail) {
        failStores.set(fail);
    }

    public void failDeletes(boolean fail) {
        failDeletes.set(fail);
    }

    public void beforeNextStore(Runnable competingAction) {
        beforeNextStore.set(competingAction);
    }

    /** Simulates an object lost outside the application. */
    public void lose(String objectKey) {
        objects.remove(objectKey);
    }

    /** Places an object directly, as a store that finished late would. */
    public void put(String objectKey, byte[] content) {
        objects.put(objectKey, new StoredObject(content.clone(), "image/jpeg"));
    }

    public boolean contains(String objectKey) {
        return objects.containsKey(objectKey);
    }

    public byte[] content(String objectKey) {
        return objects.get(objectKey).content().clone();
    }

    public int objectCount() {
        return objects.size();
    }

    public int deletionCount() {
        return deletions.get();
    }

    public void reset() {
        objects.clear();
        failStores.set(false);
        failDeletes.set(false);
        deletions.set(0);
        beforeNextStore.set(null);
    }
}
```

### Step T3 — [ADD] `TestImages.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/support/TestImages.java`

```java
package com.capsuleai.wardrobe.support;

import com.capsuleai.wardrobe.application.ReceivedPhoto;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * TEST-ONLY synthetic images: no binary fixtures and no personal photos. Every image has a red
 * top-left quadrant on blue, so orientation handling can be checked by colour.
 */
public final class TestImages {

    private TestImages() {
    }

    public static BufferedImage quadrants(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.RED);
            graphics.fillRect(0, 0, width / 2, height / 2);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    public static byte[] jpeg(int width, int height) {
        return encode(quadrants(width, height), "jpeg");
    }

    public static byte[] png(int width, int height) {
        return encode(quadrants(width, height), "png");
    }

    public static byte[] gif(int width, int height) {
        return encode(quadrants(width, height), "gif");
    }

    /**
     * A JPEG whose APP1 EXIF block (placed after the JFIF header) carries an orientation tag and
     * location-like text, to check that orientation is applied and nothing of it is kept.
     */
    public static byte[] jpegWithExif(int width, int height, int orientation) {
        byte[] plain = jpeg(width, height);
        byte[] tiff = ByteBuffer.allocate(26)
                .put(new byte[] {'M', 'M', 0, 42, 0, 0, 0, 8})      // big-endian TIFF header, IFD0 at offset 8
                .putShort((short) 1)                                    // one directory entry
                .putShort((short) 0x0112).putShort((short) 3).putInt(1) // Orientation, SHORT, count 1
                .putShort((short) orientation).putShort((short) 0)      // value, padding
                .putInt(0)                                              // no next directory
                .array();
        byte[] marker = "GPSLatitude=10.7769;GPSLongitude=106.7009".getBytes(StandardCharsets.US_ASCII);
        byte[] segment = concat("Exif\0\0".getBytes(StandardCharsets.US_ASCII), tiff, marker);
        byte[] app1 = concat(new byte[] {(byte) 0xFF, (byte) 0xE1,
                (byte) ((segment.length + 2) >> 8), (byte) (segment.length + 2)}, segment);
        int afterApp0 = 4 + (((plain[4] & 0xFF) << 8) | (plain[5] & 0xFF)); // SOI + APP0 marker + APP0 length
        return concat(Arrays.copyOfRange(plain, 0, afterApp0), app1, Arrays.copyOfRange(plain, afterApp0, plain.length));
    }

    /** The bytes of an ISO-BMFF file type box with the given major brand, padded; not a decodable image. */
    public static byte[] isoMediaHeader(String majorBrand) {
        return ByteBuffer.allocate(64)
                .putInt(24).put("ftyp".getBytes(StandardCharsets.US_ASCII))
                .put(majorBrand.getBytes(StandardCharsets.US_ASCII)).putInt(0)
                .put("mif1".getBytes(StandardCharsets.US_ASCII)).put(majorBrand.getBytes(StandardCharsets.US_ASCII))
                .array();
    }

    public static byte[] truncated(byte[] image, double keptFraction) {
        return Arrays.copyOf(image, (int) (image.length * keptFraction));
    }

    public static ReceivedPhoto received(byte[] bytes) {
        return new ReceivedPhoto() {
            @Override
            public long sizeBytes() {
                return bytes.length;
            }

            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream(bytes);
            }
        };
    }

    public static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public static boolean containsAscii(byte[] haystack, String needle) {
        return new String(haystack, StandardCharsets.ISO_8859_1).contains(needle);
    }

    /** Whether the pixel is clearly red (lossy JPEG tolerance). */
    public static boolean isRed(BufferedImage image, int x, int y) {
        Color color = new Color(image.getRGB(x, y));
        return color.getRed() > 180 && color.getBlue() < 90;
    }

    private static byte[] encode(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (!ImageIO.write(image, format, bytes)) {
                throw new IllegalStateException("No ImageIO writer for " + format);
            }
            return bytes.toByteArray();
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream joined = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            joined.writeBytes(part);
        }
        return joined.toByteArray();
    }
}
```

**Explanation.** ImageIO writes JPEGs that start with `FF D8 FF E0` (JFIF APP0). The EXIF block is inserted after APP0, the conventional order, so the JDK decoder does not warn.

### Step T4 — [MODIFY] `IdentityTestConfiguration.java`

**Path:** `backend/src/test/java/com/capsuleai/identity/support/IdentityTestConfiguration.java`

BEFORE:

```java
import com.capsuleai.identity.api.AuthenticatedAccount;
```

AFTER:

```java
import com.capsuleai.identity.api.AuthenticatedAccount;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
```

BEFORE:

```java
    /**
     * TEST-ONLY protected operation. Production has no such endpoint. It returns
```

AFTER:

```java
    /**
     * US-GAR-003: every integration context stores photos in memory, with fault injection, instead of
     * contacting S3. The real S3 adapter is verified separately by S3PhotoStorageIT.
     */
    @Bean
    @Primary
    FaultInjectingPhotoStorage faultInjectingPhotoStorage() {
        return new FaultInjectingPhotoStorage();
    }

    /**
     * TEST-ONLY protected operation. Production has no such endpoint. It returns
```

**Explanation.** This is the single shared test configuration of every integration context, so the replacement applies everywhere without new contexts. Test code is excluded from `ArchitectureDependencyTest`, so this test-to-test reference crosses no production boundary.

### Step T5 — [MODIFY] `WardrobeIntegrationTestSupport.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/support/WardrobeIntegrationTestSupport.java`

BEFORE:

```java
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
```

AFTER:

```java
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
```

BEFORE:

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
```

AFTER:

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
```

BEFORE:

```java
    protected static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @BeforeEach
    protected void resetWardrobeState() {
        jdbc.sql("TRUNCATE TABLE wardrobe.wardrobe_garment_material, wardrobe.wardrobe_garment_style, "
                + "wardrobe.wardrobe_garment").update();
    }
```

AFTER:

```java
    protected static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    protected static final String PHOTO_UPLOADS = "/api/v1/wardrobe/photo-uploads";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    protected FaultInjectingPhotoStorage photoStorage;

    @BeforeEach
    protected void resetWardrobeState() {
        jdbc.sql("TRUNCATE TABLE wardrobe.wardrobe_photo_upload, wardrobe.wardrobe_garment_material, "
                + "wardrobe.wardrobe_garment_style, wardrobe.wardrobe_garment").update();
        photoStorage.reset();
    }

    /** US-GAR-003: stages one photo as a multipart upload. */
    protected ResultActions uploadPhoto(String accessToken, String uploadKey, byte[] bytes, String fileName,
                                        String declaredType) throws Exception {
        MockHttpServletRequestBuilder request = multipart(PHOTO_UPLOADS)
                .file(new MockMultipartFile("photo", fileName, declaredType, bytes))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        if (uploadKey != null) {
            request.header(IDEMPOTENCY_KEY, uploadKey);
        }
        return mockMvc.perform(request);
    }

    /** Stages a photo as a new upload, expects 201 and returns its id. */
    protected UUID stagePhotoAndReturnId(String accessToken, byte[] bytes) throws Exception {
        String response = uploadPhoto(accessToken, UUID.randomUUID().toString(), bytes, "garment.jpg", "image/jpeg")
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return UUID.fromString(JsonPath.read(response, "$.id"));
    }

    protected String photoUploadStatus(UUID photoUploadId) {
        return jdbc.sql("SELECT status FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photoUploadId).query(String.class).single();
    }
```

**Explanation.**

- **Truncate.** The photo table references garments, so it is truncated in the same statement.
- **Fake storage.** It is reset before every test, so no object leaks between tests.

### Step T6 — [ADD] `InMemoryPhotoUploadRepository.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/application/InMemoryPhotoUploadRepository.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoCleanupTarget;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** In-memory fake of {@link PhotoUploadRepository} with the same conditional transitions as the SQL. */
final class InMemoryPhotoUploadRepository implements PhotoUploadRepository {

    private static final class Row {
        PhotoUpload upload;
        Instant nextCleanupAt;
        Instant objectDeletedAt;

        Row(PhotoUpload upload) {
            this.upload = upload;
        }
    }

    private final Map<UUID, Row> rows = new LinkedHashMap<>();

    @Override
    public synchronized boolean addIfUploadKeyUnused(PhotoUpload upload) {
        boolean used = rows.values().stream().anyMatch(row -> row.upload.isOwnedBy(upload.ownerAccountId())
                && row.upload.uploadKey().equals(upload.uploadKey()));
        if (used) {
            return false;
        }
        rows.put(upload.id(), new Row(upload));
        return true;
    }

    @Override
    public synchronized Optional<PhotoUpload> findByUploadKey(UUID ownerAccountId, UUID uploadKey) {
        return rows.values().stream().map(row -> row.upload)
                .filter(upload -> upload.isOwnedBy(ownerAccountId) && upload.uploadKey().equals(uploadKey))
                .findFirst();
    }

    @Override
    public synchronized Optional<PhotoUpload> findOwned(UUID ownerAccountId, UUID photoUploadId) {
        return Optional.ofNullable(rows.get(photoUploadId)).map(row -> row.upload)
                .filter(upload -> upload.isOwnedBy(ownerAccountId));
    }

    @Override
    public synchronized Optional<PhotoUpload> lockOwned(UUID ownerAccountId, UUID photoUploadId) {
        return findOwned(ownerAccountId, photoUploadId);
    }

    @Override
    public synchronized Optional<PhotoUpload> findAttachedTo(UUID ownerAccountId, UUID garmentId) {
        return rows.values().stream().map(row -> row.upload)
                .filter(upload -> upload.isOwnedBy(ownerAccountId) && garmentId.equals(upload.garmentId())
                        && upload.status() == PhotoUploadStatus.ATTACHED)
                .findFirst();
    }

    @Override
    public synchronized boolean markReady(UUID photoUploadId, String contentType, int width, int height,
                                          Instant readyAt) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.RECEIVING) {
            return false;
        }
        row.upload = with(row.upload, PhotoUploadStatus.READY, contentType, width, height, null);
        return true;
    }

    @Override
    public synchronized boolean discardReceiving(UUID photoUploadId, Instant now, Duration inFlightFence) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.RECEIVING) {
            return false;
        }
        discard(row, fenced(row, now, inFlightFence));
        return true;
    }

    @Override
    public synchronized boolean cancel(UUID ownerAccountId, UUID photoUploadId, Instant now, Duration inFlightFence) {
        Row row = rows.get(photoUploadId);
        if (row == null || !row.upload.isOwnedBy(ownerAccountId)) {
            return false;
        }
        PhotoUploadStatus status = row.upload.status();
        if (status != PhotoUploadStatus.RECEIVING && status != PhotoUploadStatus.READY) {
            return false;
        }
        discard(row, status == PhotoUploadStatus.RECEIVING ? fenced(row, now, inFlightFence) : now);
        return true;
    }

    @Override
    public synchronized void requeueCleanup(UUID photoUploadId, Instant now) {
        Row row = rows.get(photoUploadId);
        if (row != null && row.upload.status() == PhotoUploadStatus.DISCARDED) {
            row.objectDeletedAt = null;
            row.nextCleanupAt = now;
        }
    }

    @Override
    public synchronized boolean attach(UUID photoUploadId, UUID garmentId, Instant attachedAt) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.READY) {
            return false;
        }
        row.upload = with(row.upload, PhotoUploadStatus.ATTACHED, row.upload.contentType(), row.upload.width(),
                row.upload.height(), garmentId);
        return true;
    }

    @Override
    public synchronized int expireDue(Instant now, Duration inFlightFence, int limit) {
        List<Row> due = rows.values().stream()
                .filter(row -> (row.upload.status() == PhotoUploadStatus.RECEIVING
                        || row.upload.status() == PhotoUploadStatus.READY) && !row.upload.expiresAt().isAfter(now))
                .sorted(Comparator.comparing(row -> row.upload.expiresAt()))
                .limit(limit)
                .toList();
        due.forEach(row -> discard(row, fenced(row, now, inFlightFence)));
        return due.size();
    }

    @Override
    public synchronized List<PhotoCleanupTarget> claimDueCleanup(Instant now, Instant leaseUntil, int limit) {
        List<PhotoCleanupTarget> claimed = new ArrayList<>();
        rows.values().stream()
                .filter(row -> row.upload.status() == PhotoUploadStatus.DISCARDED && row.objectDeletedAt == null
                        && !row.nextCleanupAt.isAfter(now))
                .sorted(Comparator.comparing(row -> row.nextCleanupAt))
                .limit(limit)
                .forEach(row -> {
                    row.nextCleanupAt = leaseUntil;
                    claimed.add(new PhotoCleanupTarget(row.upload.id(), row.upload.objectKey()));
                });
        return claimed;
    }

    @Override
    public synchronized boolean markObjectDeleted(UUID photoUploadId, Instant deletedAt) {
        Row row = rows.get(photoUploadId);
        if (row == null || row.upload.status() != PhotoUploadStatus.DISCARDED || row.objectDeletedAt != null) {
            return false;
        }
        row.objectDeletedAt = deletedAt;
        return true;
    }

    synchronized Optional<Instant> nextCleanupAt(UUID photoUploadId) {
        return Optional.ofNullable(rows.get(photoUploadId)).map(row -> row.nextCleanupAt);
    }

    synchronized boolean objectDeleted(UUID photoUploadId) {
        return rows.get(photoUploadId).objectDeletedAt != null;
    }

    synchronized int size() {
        return rows.size();
    }

    private static Instant fenced(Row row, Instant now, Duration inFlightFence) {
        Instant fenceEnd = row.upload.createdAt().plus(inFlightFence);
        return fenceEnd.isAfter(now) ? fenceEnd : now;
    }

    private static void discard(Row row, Instant nextCleanupAt) {
        row.upload = with(row.upload, PhotoUploadStatus.DISCARDED, row.upload.contentType(), row.upload.width(),
                row.upload.height(), null);
        row.nextCleanupAt = nextCleanupAt;
    }

    private static PhotoUpload with(PhotoUpload upload, PhotoUploadStatus status, String contentType, Integer width,
                                    Integer height, UUID garmentId) {
        return new PhotoUpload(upload.id(), upload.ownerAccountId(), upload.uploadKey(), upload.contentSha256(),
                upload.objectKey(), status, contentType, width, height, upload.createdAt(), upload.expiresAt(),
                garmentId);
    }
}
```

### Step T7 — [MODIFY] `AddGarmentServiceTest.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/application/AddGarmentServiceTest.java`

BEFORE:

```java
    private final AddGarmentService service = new AddGarmentService(garments, transactions, clock);
```

AFTER:

```java
    private final AddGarmentService service =
            new AddGarmentService(garments, new InMemoryPhotoUploadRepository(), transactions, clock);
```

BEFORE:

```java
        AddGarmentService failingCommit = new AddGarmentService(garments, new FailingCommit(), clock);
```

AFTER:

```java
        AddGarmentService failingCommit =
                new AddGarmentService(garments, new InMemoryPhotoUploadRepository(), new FailingCommit(), clock);
```

**Explanation.** These are the only two call sites of the old constructor. The 7 existing tests keep their assertions.

### Step T8 — [ADD] `PhotoUploadTest.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/domain/PhotoUploadTest.java`

```java
package com.capsuleai.wardrobe.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003: a staged photo's lifecycle invariants (S3-IMG-03/04/08). */
class PhotoUploadTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final String SHA = "a".repeat(64);

    @Test
    void aNewIntentIsReceivingWithAnOpaqueServerKeyAndExpiresAfterTheTtl() {
        UUID id = UUID.randomUUID();
        PhotoUpload intent = PhotoUpload.receiving(id, UUID.randomUUID(), UUID.randomUUID(), SHA, NOW,
                Duration.ofHours(24));

        assertThat(intent.status()).isEqualTo(PhotoUploadStatus.RECEIVING);
        assertThat(intent.objectKey()).isEqualTo("photos/" + id + ".jpg").doesNotContain(SHA);
        assertThat(intent.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(intent.garmentId()).isNull();
    }

    @Test
    void aStoredPhotoNeedsItsMetadataAndOnlyAnAttachedPhotoNamesAGarment() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> upload(id, PhotoUploadStatus.READY, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> upload(id, PhotoUploadStatus.ATTACHED, 800, 600, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> upload(id, PhotoUploadStatus.READY, 800, 600, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(upload(id, PhotoUploadStatus.ATTACHED, 800, 600, UUID.randomUUID()).asGarmentPhoto().width())
                .isEqualTo(800);
    }

    @Test
    void onlyAReadyUnexpiredPhotoIsUsable() {
        UUID id = UUID.randomUUID();
        PhotoUpload ready = upload(id, PhotoUploadStatus.READY, 800, 600, null);

        assertThat(ready.isUsableAt(NOW.plusSeconds(60))).isTrue();
        assertThat(ready.isUsableAt(ready.expiresAt())).as("expiry is exclusive").isFalse();
        assertThat(upload(id, PhotoUploadStatus.RECEIVING, null, null, null).isUsableAt(NOW)).isFalse();
        assertThat(upload(id, PhotoUploadStatus.DISCARDED, null, null, null).isUsableAt(NOW)).isFalse();
        assertThat(upload(id, PhotoUploadStatus.ATTACHED, 800, 600, UUID.randomUUID()).isUsableAt(NOW)).isFalse();
    }

    @Test
    void aMalformedHashOrANonPositiveLifetimeIsRejected() {
        assertThatThrownBy(() -> PhotoUpload.receiving(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "NOT-A-HASH", NOW, Duration.ofHours(1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PhotoUpload.receiving(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                SHA, NOW, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }

    private static PhotoUpload upload(UUID id, PhotoUploadStatus status, Integer width, Integer height,
                                      UUID garmentId) {
        String contentType = width == null ? null : PhotoUpload.STORED_CONTENT_TYPE;
        return new PhotoUpload(id, UUID.randomUUID(), UUID.randomUUID(), SHA, PhotoUpload.objectKeyFor(id), status,
                contentType, width, height, NOW, NOW.plus(Duration.ofHours(24)), garmentId);
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=PhotoUploadTest' test`, expected 4 tests.

### Step T9 — [ADD] `StagePhotoUploadServiceTest.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/application/StagePhotoUploadServiceTest.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoInspector;
import com.capsuleai.wardrobe.application.ports.PhotoStorageUnavailableException;
import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
import com.capsuleai.wardrobe.support.TestImages;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003 — staging rules, retry identity and storage recovery (S3-IMG-02/03/04, FR-AI-012). */
class StagePhotoUploadServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();
    private static final PhotoUploadPolicy POLICY = new PhotoUploadPolicy(15_000_000, 512, Duration.ofHours(24),
            Duration.ofMinutes(5), 100, Duration.ofMinutes(10));

    private final InMemoryPhotoUploadRepository uploads = new InMemoryPhotoUploadRepository();
    private final FaultInjectingPhotoStorage storage = new FaultInjectingPhotoStorage();
    private final FixedInspector inspector = new FixedInspector();
    private final StagePhotoUploadService service = new StagePhotoUploadService(uploads, storage, inspector,
            new ImmediateWardrobeTransactions(), Clock.fixed(NOW, ZoneOffset.UTC), POLICY);

    @Test
    void aValidPhotoIsStagedReadyStoredAndIsNotAGarment() {
        StagedPhoto staged = service.stage(command(UUID.randomUUID().toString(), new byte[] {1, 2, 3}));

        PhotoUpload upload = staged.upload();
        assertThat(staged.newlyStaged()).isTrue();
        assertThat(upload.status()).isEqualTo(PhotoUploadStatus.READY);
        assertThat(upload.contentType()).isEqualTo("image/jpeg");
        assertThat(upload.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
        assertThat(upload.garmentId()).isNull();
        assertThat(storage.contains(upload.objectKey())).isTrue();
    }

    @Test
    void theSizeLimitAcceptsExactly15000000BytesAndRejectsOneMore() {
        assertThat(service.stage(command(UUID.randomUUID().toString(), new byte[15_000_000])).upload().status())
                .isEqualTo(PhotoUploadStatus.READY);
        int inspectionsBefore = inspector.calls.get();

        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[15_000_001])))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.TOO_LARGE));
        assertThat(inspector.calls.get()).as("an oversized photo is never decoded").isEqualTo(inspectionsBefore);
        assertThat(uploads.size()).isOne();
    }

    @Test
    void theShortestSideOfTheOriginalMustBeAtLeast512Pixels() {
        inspector.originalSize(511, 800);
        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[] {1})))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.TOO_SMALL));
        assertThat(uploads.size()).isZero();
        assertThat(storage.objectCount()).isZero();

        inspector.originalSize(512, 800);
        assertThat(service.stage(command(UUID.randomUUID().toString(), new byte[] {1})).upload().status())
                .isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void aRetryWithTheSameKeyAndBytesReturnsTheSameStagedPhoto() {
        String key = UUID.randomUUID().toString();
        StagedPhoto first = service.stage(command(key, new byte[] {9, 9, 9}));

        StagedPhoto retry = service.stage(command(key, new byte[] {9, 9, 9}));

        assertThat(retry.newlyStaged()).isFalse();
        assertThat(retry.upload().id()).isEqualTo(first.upload().id());
        assertThat(inspector.calls.get()).isOne();
        assertThat(storage.objectCount()).isOne();
    }

    @Test
    void theSameKeyWithDifferentBytesIsRefused() {
        String key = UUID.randomUUID().toString();
        service.stage(command(key, new byte[] {1}));

        assertThatThrownBy(() -> service.stage(command(key, new byte[] {2})))
                .isInstanceOf(PhotoUploadKeyReusedException.class);
        assertThat(uploads.size()).isOne();
    }

    @Test
    void aMissingPhotoAndAMissingOrMalformedKeyAreFieldErrors() {
        assertThatThrownBy(() -> service.stage(new StagePhotoCommand(OWNER, null, null)))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("Idempotency-Key", GarmentValueProblem.REQUIRED),
                                new GarmentFieldViolation("photo", GarmentValueProblem.REQUIRED)));
        assertThatThrownBy(() -> service.stage(command("not-a-uuid", new byte[] {1})))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("Idempotency-Key", GarmentValueProblem.INVALID_FORMAT)));
        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[0])))
                .isInstanceOf(GarmentValidationException.class);
    }

    @Test
    void aRejectedPhotoLeavesNoRowAndNoObject() {
        inspector.reject(PhotoRejectionReason.UNSUPPORTED_FORMAT);

        assertThatThrownBy(() -> service.stage(command(UUID.randomUUID().toString(), new byte[] {1})))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.UNSUPPORTED_FORMAT));
        assertThat(uploads.size()).isZero();
        assertThat(storage.objectCount()).isZero();
    }

    @Test
    void aStorageFailureDiscardsTheIntentBehindTheInFlightFenceAndIsReportedUnavailable() {
        storage.failStores(true);
        String key = UUID.randomUUID().toString();

        assertThatThrownBy(() -> service.stage(command(key, new byte[] {1})))
                .isInstanceOf(PhotoStorageUnavailableException.class);

        PhotoUpload discarded = uploads.findByUploadKey(OWNER, UUID.fromString(key)).orElseThrow();
        assertThat(discarded.status()).isEqualTo(PhotoUploadStatus.DISCARDED);
        assertThat(uploads.nextCleanupAt(discarded.id())).contains(NOW.plus(Duration.ofMinutes(5)));
    }

    @Test
    void aPhotoCancelledWhileItWasBeingStoredIsNotUsableAndItsObjectIsQueuedForDeletion() {
        String key = UUID.randomUUID().toString();
        storage.beforeNextStore(() -> {
            PhotoUpload receiving = uploads.findByUploadKey(OWNER, UUID.fromString(key)).orElseThrow();
            uploads.cancel(OWNER, receiving.id(), NOW, Duration.ofMinutes(5));
        });

        assertThatThrownBy(() -> service.stage(command(key, new byte[] {1})))
                .isInstanceOf(PhotoUploadNotUsableException.class);

        PhotoUpload cancelled = uploads.findByUploadKey(OWNER, UUID.fromString(key)).orElseThrow();
        assertThat(cancelled.status()).isEqualTo(PhotoUploadStatus.DISCARDED);
        assertThat(storage.contains(cancelled.objectKey())).as("the late write landed").isTrue();
        assertThat(uploads.nextCleanupAt(cancelled.id())).as("and is due for deletion now").contains(NOW);
    }

    private static StagePhotoCommand command(String key, byte[] bytes) {
        return new StagePhotoCommand(OWNER, key, TestImages.received(bytes));
    }

    /** A decoder stand-in: a fixed result, or a configured rejection; counts calls. */
    private static final class FixedInspector implements PhotoInspector {

        private final AtomicInteger calls = new AtomicInteger();
        private int originalWidth = 1200;
        private int originalHeight = 1600;
        private PhotoRejectionReason rejection;

        void originalSize(int width, int height) {
            originalWidth = width;
            originalHeight = height;
        }

        void reject(PhotoRejectionReason reason) {
            rejection = reason;
        }

        @Override
        public InspectedPhoto inspectAndNormalize(ReceivedPhoto photo) {
            calls.incrementAndGet();
            if (rejection != null) {
                throw new PhotoRejectedException(rejection);
            }
            return new InspectedPhoto("JPEG", originalWidth, originalHeight, originalWidth, originalHeight,
                    new byte[] {(byte) 0xFF, (byte) 0xD8});
        }
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=StagePhotoUploadServiceTest' test`, expected 9 tests.

### Step T10 — [ADD] `PhotoBackedGarmentCreationTest.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/application/PhotoBackedGarmentCreationTest.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentValueProblem;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003 — explicit confirmation attaches one reviewed photo to exactly one new garment (S3-IMG-03/04). */
class PhotoBackedGarmentCreationTest {

    private static final Instant NOW = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();

    private final InMemoryGarmentRepository garments = new InMemoryGarmentRepository();
    private final InMemoryPhotoUploadRepository uploads = new InMemoryPhotoUploadRepository();
    private final AddGarmentService service = new AddGarmentService(garments, uploads,
            new ImmediateWardrobeTransactions(), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aConfirmedGarmentWithAReadyPhotoIsCreatedWithThePhotoAttached() {
        PhotoUpload photo = readyPhoto(OWNER);

        GarmentCreation creation = service.add(command(UUID.randomUUID().toString(), photo.id().toString()));

        assertThat(creation.newlyCreated()).isTrue();
        assertThat(creation.garment().photo()).hasValueSatisfying(attached -> {
            assertThat(attached.photoUploadId()).isEqualTo(photo.id());
            assertThat(attached.width()).isEqualTo(1536);
        });
        PhotoUpload attached = uploads.findOwned(OWNER, photo.id()).orElseThrow();
        assertThat(attached.status()).isEqualTo(PhotoUploadStatus.ATTACHED);
        assertThat(attached.garmentId()).isEqualTo(creation.garment().id());
    }

    @Test
    void repeatingTheSameConfirmationReturnsTheSameGarmentOnce() {
        PhotoUpload photo = readyPhoto(OWNER);
        String key = UUID.randomUUID().toString();
        GarmentCreation first = service.add(command(key, photo.id().toString()));

        GarmentCreation retry = service.add(command(key, photo.id().toString()));

        assertThat(retry.newlyCreated()).isFalse();
        assertThat(retry.garment().id()).isEqualTo(first.garment().id());
        assertThat(garments.all()).hasSize(1);
    }

    @Test
    void theSameKeyWithAnotherPhotoOrWithoutAPhotoIsAKeyReuse() {
        PhotoUpload photo = readyPhoto(OWNER);
        PhotoUpload otherPhoto = readyPhoto(OWNER);
        String key = UUID.randomUUID().toString();
        service.add(command(key, photo.id().toString()));

        assertThatThrownBy(() -> service.add(command(key, otherPhoto.id().toString())))
                .isInstanceOf(IdempotencyKeyReusedException.class);
        assertThatThrownBy(() -> service.add(command(key, null)))
                .isInstanceOf(IdempotencyKeyReusedException.class);
        assertThat(garments.all()).hasSize(1);
        assertThat(uploads.findOwned(OWNER, otherPhoto.id()).orElseThrow().status())
                .isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void anotherOwnersOrAnUnknownPhotoIsNotFoundAndCreatesNothing() {
        PhotoUpload strangersPhoto = readyPhoto(UUID.randomUUID());

        assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), strangersPhoto.id().toString())))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), UUID.randomUUID().toString())))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThat(garments.all()).isEmpty();
    }

    @Test
    void aCancelledExpiredOrAlreadyAttachedPhotoIsNotUsableAndNeverFallsBackToAnImagelessGarment() {
        PhotoUpload cancelled = readyPhoto(OWNER);
        uploads.cancel(OWNER, cancelled.id(), NOW, Duration.ofMinutes(5));
        PhotoUpload expired = photo(OWNER, NOW.minus(Duration.ofHours(25)));
        PhotoUpload attached = readyPhoto(OWNER);
        service.add(command(UUID.randomUUID().toString(), attached.id().toString()));

        for (PhotoUpload unusable : List.of(cancelled, expired, attached)) {
            assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), unusable.id().toString())))
                    .isInstanceOf(PhotoUploadNotUsableException.class);
        }
        assertThat(garments.all()).hasSize(1);
    }

    @Test
    void aMalformedPhotoUploadIdIsAFieldError() {
        assertThatThrownBy(() -> service.add(command(UUID.randomUUID().toString(), "not-a-photo")))
                .isInstanceOfSatisfying(GarmentValidationException.class, rejected ->
                        assertThat(rejected.violations()).containsExactly(
                                new GarmentFieldViolation("photoUploadId", GarmentValueProblem.INVALID_FORMAT)));
    }

    @Test
    void anImagelessAdditionStillWorksWithoutAnyPhoto() {
        GarmentCreation creation = service.add(command(UUID.randomUUID().toString(), null));

        assertThat(creation.newlyCreated()).isTrue();
        assertThat(creation.garment().photo()).isEmpty();
        assertThat(uploads.size()).isZero();
    }

    @Test
    void anUnconfirmedPhotoBackedAdditionCreatesNothingAndLeavesThePhotoReady() {
        PhotoUpload photo = readyPhoto(OWNER);
        AddGarmentCommand unconfirmed = new AddGarmentCommand(OWNER, UUID.randomUUID().toString(), false, "TOPS",
                null, "WHITE", List.of(), null, null, null, null, null, null, null, null, null, null,
                photo.id().toString());

        assertThatThrownBy(() -> service.add(unconfirmed)).isInstanceOf(GarmentValidationException.class);
        assertThat(garments.all()).isEmpty();
        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.READY);
    }

    private PhotoUpload readyPhoto(UUID owner) {
        return photo(owner, NOW);
    }

    /** A READY staged photo created at {@code stagedAt} with a 24-hour lifetime. */
    private PhotoUpload photo(UUID owner, Instant stagedAt) {
        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), owner, UUID.randomUUID(), "b".repeat(64),
                stagedAt, Duration.ofHours(24));
        uploads.addIfUploadKeyUnused(intent);
        uploads.markReady(intent.id(), PhotoUpload.STORED_CONTENT_TYPE, 1536, 2048, stagedAt);
        return uploads.findOwned(owner, intent.id()).orElseThrow();
    }

    private static AddGarmentCommand command(String key, String photoUploadId) {
        return new AddGarmentCommand(OWNER, key, true, "TOPS", "T_SHIRTS", "WHITE", List.of("COTTON"), null, null,
                null, null, null, null, null, null, null, null, photoUploadId);
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=PhotoBackedGarmentCreationTest' test`, expected 8 tests.

### Step T11 — [ADD] `PhotoUploadLifecycleServiceTest.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/application/PhotoUploadLifecycleServiceTest.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.identity.support.MutableClock;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService.CleanupResult;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.domain.PhotoUploadStatus;
import com.capsuleai.wardrobe.support.FaultInjectingPhotoStorage;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003 — cancellation, expiry and resumable object deletion (S3-IMG-04/08, AC-03/AC-06). */
class PhotoUploadLifecycleServiceTest {

    private static final Instant START = Instant.parse("2026-11-11T08:00:00Z");
    private static final UUID OWNER = UUID.randomUUID();
    private static final PhotoUploadPolicy POLICY = new PhotoUploadPolicy(15_000_000, 512, Duration.ofHours(24),
            Duration.ofMinutes(5), 100, Duration.ofMinutes(10));

    private final MutableClock clock = new MutableClock(START);
    private final InMemoryPhotoUploadRepository uploads = new InMemoryPhotoUploadRepository();
    private final FaultInjectingPhotoStorage storage = new FaultInjectingPhotoStorage();
    private final ImmediateWardrobeTransactions transactions = new ImmediateWardrobeTransactions();
    private final CancelPhotoUploadService cancel = new CancelPhotoUploadService(uploads, transactions, clock, POLICY);
    private final PhotoUploadCleanupService cleanup =
            new PhotoUploadCleanupService(uploads, storage, transactions, clock, POLICY);

    @Test
    void cancellingAReadyPhotoMakesItUnusableAndDeletesItsObjectOnTheNextRun() {
        PhotoUpload photo = storedReadyPhoto();

        cancel.cancel(OWNER, photo.id().toString());
        CleanupResult result = cleanup.run();

        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.DISCARDED);
        assertThat(result.deleted()).isOne();
        assertThat(storage.contains(photo.objectKey())).isFalse();
        assertThat(uploads.objectDeleted(photo.id())).isTrue();
    }

    @Test
    void cancellingTwiceSucceedsAndAnAttachedPhotoCannotBeCancelled() {
        PhotoUpload photo = storedReadyPhoto();
        cancel.cancel(OWNER, photo.id().toString());
        cancel.cancel(OWNER, photo.id().toString());

        PhotoUpload attached = storedReadyPhoto();
        uploads.attach(attached.id(), UUID.randomUUID(), START);
        assertThatThrownBy(() -> cancel.cancel(OWNER, attached.id().toString()))
                .isInstanceOf(PhotoUploadNotUsableException.class);
        assertThat(uploads.findOwned(OWNER, attached.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.ATTACHED);
    }

    @Test
    void anotherOwnersUnknownOrMalformedPhotoCannotBeCancelled() {
        PhotoUpload photo = storedReadyPhoto();

        assertThatThrownBy(() -> cancel.cancel(UUID.randomUUID(), photo.id().toString()))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> cancel.cancel(OWNER, UUID.randomUUID().toString()))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThatThrownBy(() -> cancel.cancel(OWNER, "x"))
                .isInstanceOf(PhotoUploadNotFoundException.class);
        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.READY);
    }

    @Test
    void anUnconfirmedPhotoExpiresAfterThePendingTtlAndItsObjectIsDeleted() {
        PhotoUpload photo = storedReadyPhoto();

        clock.advance(Duration.ofHours(23));
        assertThat(cleanup.run().expired()).as("not yet expired").isZero();
        clock.advance(Duration.ofHours(1));
        CleanupResult result = cleanup.run();

        assertThat(result.expired()).isOne();
        assertThat(result.deleted()).isOne();
        assertThat(storage.contains(photo.objectKey())).isFalse();
    }

    @Test
    void anObjectOfAPhotoDiscardedWhileReceivingIsNotDeletedBeforeTheInFlightFence() {
        PhotoUpload receiving = PhotoUpload.receiving(UUID.randomUUID(), OWNER, UUID.randomUUID(), "c".repeat(64),
                START, Duration.ofHours(24));
        uploads.addIfUploadKeyUnused(receiving);
        cancel.cancel(OWNER, receiving.id().toString());
        storage.put(receiving.objectKey(), new byte[] {1}); // a store that finished after the cancel

        assertThat(cleanup.run().deleted()).isZero();
        clock.advance(Duration.ofMinutes(5));
        assertThat(cleanup.run().deleted()).isOne();
        assertThat(storage.contains(receiving.objectKey())).isFalse();
    }

    @Test
    void aFailedDeletionIsRetriedAfterTheRetryDelay() {
        PhotoUpload photo = storedReadyPhoto();
        cancel.cancel(OWNER, photo.id().toString());
        storage.failDeletes(true);

        assertThat(cleanup.run().failed()).isOne();
        storage.failDeletes(false);
        assertThat(cleanup.run().deleted()).as("leased until the retry delay").isZero();
        clock.advance(Duration.ofMinutes(10));
        assertThat(cleanup.run().deleted()).isOne();
        assertThat(uploads.objectDeleted(photo.id())).isTrue();
    }

    @Test
    void anAttachedPhotoIsNeverExpiredOrDeleted() {
        PhotoUpload photo = storedReadyPhoto();
        uploads.attach(photo.id(), UUID.randomUUID(), START);

        clock.advance(Duration.ofDays(30));
        CleanupResult result = cleanup.run();

        assertThat(result.expired()).isZero();
        assertThat(result.deleted()).isZero();
        assertThat(storage.contains(photo.objectKey())).isTrue();
        assertThat(uploads.findOwned(OWNER, photo.id()).orElseThrow().status()).isEqualTo(PhotoUploadStatus.ATTACHED);
    }

    private PhotoUpload storedReadyPhoto() {
        PhotoUpload intent = PhotoUpload.receiving(UUID.randomUUID(), OWNER, UUID.randomUUID(), "d".repeat(64),
                clock.instant(), POLICY.pendingTtl());
        uploads.addIfUploadKeyUnused(intent);
        storage.store(intent.objectKey(), new byte[] {(byte) 0xFF, (byte) 0xD8}, PhotoUpload.STORED_CONTENT_TYPE);
        uploads.markReady(intent.id(), PhotoUpload.STORED_CONTENT_TYPE, 1024, 768, clock.instant());
        return uploads.findOwned(OWNER, intent.id()).orElseThrow();
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=PhotoUploadLifecycleServiceTest' test`, expected 7 tests.

### Step T12 — [ADD] `ImageIoPhotoInspectorTest.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/imaging/ImageIoPhotoInspectorTest.java`

```java
package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.application.ports.PhotoProcessingUnavailableException;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.support.TestImages;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-003 (S3-IMG-06, FR-AI-012): genuine decoding from the bytes, bounded resources, orientation and
 * metadata stripping. JPEG and PNG only; real HEIC/HEIF decoding is HeifDecodingIT.
 */
class ImageIoPhotoInspectorTest {

    private final ImageIoPhotoInspector inspector = inspector(250_000_000);

    @AfterEach
    void stopWatchdog() {
        inspector.close();
    }

    @Test
    void aGenuineJpegIsAcceptedWithItsOriginalDimensions() {
        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(TestImages.jpeg(1200, 900)));

        assertThat(photo.sourceFormat()).isEqualTo("JPEG");
        assertThat(photo.originalWidth()).isEqualTo(1200);
        assertThat(photo.originalHeight()).isEqualTo(900);
        assertThat(TestImages.decode(photo.jpeg()).getWidth()).isEqualTo(1200);
    }

    @Test
    void aGenuinePngIsAcceptedAndStoredAsJpeg() {
        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(TestImages.png(700, 512)));

        assertThat(photo.sourceFormat()).isEqualTo("PNG");
        assertThat(photo.originalHeight()).isEqualTo(512);
        assertThat(photo.jpeg()[0] & 0xFF).isEqualTo(0xFF);
        assertThat(photo.jpeg()[1] & 0xFF).isEqualTo(0xD8);
    }

    @Test
    void theFormatComesFromTheBytesAloneAndOtherFormatsAreUnsupported() {
        assertRejected(TestImages.gif(800, 800), PhotoRejectionReason.UNSUPPORTED_FORMAT);
        assertRejected("this is not a photo".getBytes(StandardCharsets.UTF_8), PhotoRejectionReason.UNSUPPORTED_FORMAT);
    }

    @Test
    void aTruncatedJpegOrPngIsUnreadable() {
        assertRejected(TestImages.truncated(TestImages.jpeg(900, 900), 0.5), PhotoRejectionReason.UNREADABLE);
        assertRejected(TestImages.truncated(TestImages.png(900, 900), 0.5), PhotoRejectionReason.UNREADABLE);
    }

    @Test
    void jpegOrientationIsAppliedToTheStoredPhoto() {
        InspectedPhoto photo = inspector.inspectAndNormalize(
                TestImages.received(TestImages.jpegWithExif(600, 800, 6)));

        BufferedImage stored = TestImages.decode(photo.jpeg());
        assertThat(photo.originalWidth()).as("the rule uses the original header size").isEqualTo(600);
        assertThat(stored.getWidth()).isEqualTo(800);
        assertThat(stored.getHeight()).isEqualTo(600);
        assertThat(TestImages.isRed(stored, 700, 100)).as("red quadrant rotated to the top right").isTrue();
        assertThat(TestImages.isRed(stored, 100, 100)).isFalse();
    }

    @Test
    void theStoredPhotoCarriesNoExifOrLocationMetadata() {
        byte[] withExif = TestImages.jpegWithExif(800, 600, 1);
        assertThat(TestImages.containsAscii(withExif, "GPSLatitude")).isTrue();

        byte[] stored = inspector.inspectAndNormalize(TestImages.received(withExif)).jpeg();

        assertThat(TestImages.containsAscii(stored, "Exif")).isFalse();
        assertThat(TestImages.containsAscii(stored, "GPSLatitude")).isFalse();
    }

    @Test
    void largePhotosAreDownscaledSoTheLongEdgeIsAtMost2048() {
        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(TestImages.jpeg(4100, 1000)));

        assertThat(photo.originalWidth()).isEqualTo(4100);
        assertThat(Math.max(photo.width(), photo.height())).isLessThanOrEqualTo(2048);
        assertThat(TestImages.decode(photo.jpeg()).getWidth()).isEqualTo(photo.width());
    }

    @Test
    void aPhotoDeclaringMorePixelsThanTheSafetyCeilingIsRefusedBeforeDecoding() {
        try (ImageIoPhotoInspector strict = inspector(1_000_000)) {
            assertThatThrownBy(() -> strict.inspectAndNormalize(TestImages.received(TestImages.png(1100, 1000))))
                    .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                            assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.UNREADABLE));
        }
    }

    @Test
    void aHeifPhotoWithoutAnInstalledDecoderIsTemporarilyUnavailableNotUnsupported() {
        assertThatThrownBy(() -> inspector.inspectAndNormalize(
                TestImages.received(TestImages.isoMediaHeader("heic"))))
                .isInstanceOf(PhotoProcessingUnavailableException.class);
    }

    @Test
    void anIsoMediaFileThatIsNotHeifIsUnsupported() {
        assertRejected(TestImages.isoMediaHeader("avif"), PhotoRejectionReason.UNSUPPORTED_FORMAT);
    }

    private void assertRejected(byte[] bytes, PhotoRejectionReason reason) {
        assertThatThrownBy(() -> inspector.inspectAndNormalize(TestImages.received(bytes)))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(reason));
    }

    /** The decoder command deliberately does not exist, so no test here depends on libheif. */
    private static ImageIoPhotoInspector inspector(long maximumDeclaredPixels) {
        return new ImageIoPhotoInspector(new HeifDecoder("capsuleai-missing-heif-decoder", Duration.ofSeconds(5)),
                2048, maximumDeclaredPixels, Duration.ofSeconds(20), 2);
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=ImageIoPhotoInspectorTest' test`, expected 10 tests.

### Step T13 — [ADD] `HeifDecodingIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/imaging/HeifDecodingIT.java`

```java
package com.capsuleai.wardrobe.adapter.out.imaging;

import com.capsuleai.wardrobe.application.PhotoRejectedException;
import com.capsuleai.wardrobe.application.ports.InspectedPhoto;
import com.capsuleai.wardrobe.domain.PhotoRejectionReason;
import com.capsuleai.wardrobe.support.TestImages;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * US-GAR-003 (S3-IMG-06, OPEN-S3-08/11): real HEIC/HEIF decoding through libheif. Fixtures are synthetic
 * images encoded with heif-enc at test time. With CAPSULEAI_REQUIRE_HEIF=true (CI) a missing toolchain
 * fails the build; otherwise these tests are skipped locally. Real device photos remain manual evidence (V3).
 */
class HeifDecodingIT {

    private static final boolean REQUIRED = "true".equalsIgnoreCase(System.getenv("CAPSULEAI_REQUIRE_HEIF"));
    private static final String DECODER = environment("CAPSULE_HEIF_DECODER", "heif-dec");
    private static final String ENCODER = environment("CAPSULE_HEIF_ENCODER", "heif-enc");

    private static ImageIoPhotoInspector inspector;

    @TempDir
    Path work;

    @BeforeAll
    static void requireLibheif() {
        boolean available = starts(DECODER) && starts(ENCODER);
        if (REQUIRED) {
            assertThat(available).as("libheif's %s and %s are installed (CAPSULEAI_REQUIRE_HEIF=true)", DECODER, ENCODER)
                    .isTrue();
        } else {
            assumeTrue(available, "libheif is not installed; set CAPSULEAI_REQUIRE_HEIF=true to fail instead");
        }
        inspector = new ImageIoPhotoInspector(new HeifDecoder(DECODER, Duration.ofSeconds(20)), 2048,
                250_000_000, Duration.ofSeconds(20), 2);
    }

    @AfterAll
    static void stopInspector() {
        if (inspector != null) {
            inspector.close();
        }
    }

    @Test
    void aRealHeicPhotoIsDecodedNormalizedAndMeasured() throws Exception {
        byte[] heic = encode("single.heic", TestImages.png(640, 800));

        InspectedPhoto photo = inspector.inspectAndNormalize(TestImages.received(heic));

        assertThat(photo.sourceFormat()).isEqualTo("HEIF");
        assertThat(photo.originalWidth()).isEqualTo(640);
        assertThat(photo.originalHeight()).isEqualTo(800);
        assertThat(TestImages.isRed(TestImages.decode(photo.jpeg()), 50, 50)).as("orientation kept").isTrue();
    }

    @Test
    void aHeifFileWithTwoTopLevelImagesIsRejectedExplicitly() throws Exception {
        byte[] twoImages = encode("two.heic", TestImages.png(640, 800), TestImages.png(800, 640));

        assertThatThrownBy(() -> inspector.inspectAndNormalize(TestImages.received(twoImages)))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.MULTIPLE_IMAGES));
    }

    @Test
    void aTruncatedHeicIsUnreadable() throws Exception {
        byte[] truncated = TestImages.truncated(encode("cut.heic", TestImages.png(640, 800)), 0.6);

        assertThatThrownBy(() -> inspector.inspectAndNormalize(TestImages.received(truncated)))
                .isInstanceOfSatisfying(PhotoRejectedException.class, rejected ->
                        assertThat(rejected.reason()).isEqualTo(PhotoRejectionReason.UNREADABLE));
    }

    /** heif-enc [inputs...] -o output; several inputs become several top-level images (first is primary). */
    private byte[] encode(String output, byte[]... pngs) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of(ENCODER));
        for (int index = 0; index < pngs.length; index++) {
            Path input = work.resolve("input-" + index + ".png");
            Files.write(input, pngs[index]);
            command.add(input.toString());
        }
        Path target = work.resolve(output);
        command.addAll(List.of("-o", target.toString()));
        Process process = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("heif-enc finished").isTrue();
        assertThat(process.exitValue()).as("heif-enc exit code").isZero();
        return Files.readAllBytes(target);
    }

    /** Whether the tool can be started at all (its exit code for --help varies between versions). */
    private static boolean starts(String tool) {
        try {
            Process process = new ProcessBuilder(tool, "--help").redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            process.waitFor(10, TimeUnit.SECONDS);
            process.destroyForcibly();
            return true;
        } catch (IOException | InterruptedException notAvailable) {
            return false;
        }
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
```

**Explanation.**

- **Encoding assumption.** `heif-enc` stores several inputs as separate top-level images; this is the documented libheif behaviour and is confirmed by verification V4.
- **Failing V4.** If V4 shows a different result, the second test fails. That is the intended signal for OPEN-S3-11.

**Commands:**

```powershell
.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=HeifDecodingIT' verify
$env:CAPSULEAI_REQUIRE_HEIF = 'true'; .\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=HeifDecodingIT' verify; Remove-Item Env:CAPSULEAI_REQUIRE_HEIF
```

Expected: 3 tests (skipped locally without libheif in the first command; must pass in the second).

### Step T14 — [ADD] `S3PhotoStorageIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/storage/S3PhotoStorageIT.java`

```java
package com.capsuleai.wardrobe.adapter.out.storage;

import com.capsuleai.wardrobe.application.ports.StoredPhoto;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US-GAR-003 (S3-IMG-01, OPEN-S3-13): the real S3 adapter against an S3-compatible container. An
 * emulator pass is protocol evidence only; the real-provider check in V-S3 is still required.
 */
@Testcontainers
class S3PhotoStorageIT {

    private static final int S3_PORT = 9090;
    private static final String BUCKET = "capsuleai-photos-it";

    @Container
    private static final GenericContainer<?> S3MOCK = new GenericContainer<>("adobe/s3mock:5.1.0")
            .withExposedPorts(S3_PORT)
            .withEnv("COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS", BUCKET)
            .withEnv("initialBuckets", BUCKET)
            .waitingFor(Wait.forListeningPort());

    private static S3Client client;

    @BeforeAll
    static void connect() {
        client = S3Client.builder()
                .endpointOverride(URI.create("http://" + S3MOCK.getHost() + ":" + S3MOCK.getMappedPort(S3_PORT)))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test-only", "test-only")))
                .overrideConfiguration(configuration -> configuration.apiCallTimeout(Duration.ofSeconds(30)))
                .build();
        try {
            client.createBucket(request -> request.bucket(BUCKET));
        } catch (S3Exception alreadyCreated) {
            // created by the container's initial-bucket setting
        }
    }

    @AfterAll
    static void disconnect() {
        client.close();
    }

    @Test
    void aStoredPhotoIsReadBackWithItsLengthAndType() throws Exception {
        String key = newKey();
        byte[] content = {(byte) 0xFF, (byte) 0xD8, 1, 2, 3};
        storage().store(key, content, "image/jpeg");

        try (StoredPhoto photo = storage().open(key).orElseThrow()) {
            assertThat(photo.content().readAllBytes()).isEqualTo(content);
            assertThat(photo.sizeBytes()).isEqualTo(content.length);
            assertThat(photo.contentType()).isEqualTo("image/jpeg");
        }
    }

    @Test
    void aMissingObjectIsEmptyNotAnError() {
        assertThat(storage().open(newKey())).isEmpty();
    }

    @Test
    void deletingRemovesTheObjectAndIsIdempotent() {
        String key = newKey();
        storage().store(key, new byte[] {1}, "image/jpeg");

        storage().delete(key);
        storage().delete(key);

        assertThat(storage().open(key)).isEmpty();
    }

    /** V-S3 step 2: with conditional writes, an existing key is never overwritten (S3-IMG-04). */
    @Test
    void aConditionalWriteNeverOverwritesAnExistingKey() throws Exception {
        String key = newKey();
        storage().store(key, new byte[] {1}, "image/jpeg");

        assertThatThrownBy(() -> storage().store(key, new byte[] {2}, "image/jpeg"))
                .isInstanceOf(IllegalStateException.class);
        try (StoredPhoto photo = storage().open(key).orElseThrow()) {
            assertThat(photo.content().readAllBytes()).containsExactly(1);
        }
    }

    private static S3PhotoStorage storage() {
        return new S3PhotoStorage(client, BUCKET, true);
    }

    private static String newKey() {
        return "photos/" + UUID.randomUUID() + ".jpg";
    }
}
```

**Explanation.** If `aConditionalWriteNeverOverwritesAnExistingKey` fails only against S3Mock, the fixture does not implement `If-None-Match`. Record that in the PR, keep the test, and run it against a real AWS test bucket as part of V-S3 (§17). Do **not** weaken the production default.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=S3PhotoStorageIT' verify`, expected 4 tests.

### Step T15 — [ADD] `PhotoUploadSchemaIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/persistence/PhotoUploadSchemaIT.java`

```java
package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** US-GAR-003: the V10 constraints defend the staged-photo lifecycle even if code skipped a check. */
class PhotoUploadSchemaIT extends WardrobeIntegrationTestSupport {

    private static final String SHA = "e".repeat(64);

    @Test
    void aReceivingIntentIsAcceptedAndUploadKeysAreUniquePerOwner() {
        UUID owner = UUID.randomUUID();
        UUID key = UUID.randomUUID();
        insert(UUID.randomUUID(), owner, key, "RECEIVING", null, null, null);

        assertThatThrownBy(() -> insert(UUID.randomUUID(), owner, key, "RECEIVING", null, null, null))
                .isInstanceOf(DataIntegrityViolationException.class);
        insert(UUID.randomUUID(), UUID.randomUUID(), key, "RECEIVING", null, null, null);
        assertThat(countRows("wardrobe.wardrobe_photo_upload")).isEqualTo(2);
    }

    @Test
    void anAttachedPhotoNamesExactlyOneGarmentAndAGarmentHasAtMostOnePhoto() {
        UUID owner = UUID.randomUUID();
        UUID garment = insertGarment(owner);

        assertThatThrownBy(() -> insert(UUID.randomUUID(), owner, UUID.randomUUID(), "ATTACHED", "image/jpeg", 800,
                null)).isInstanceOf(DataIntegrityViolationException.class);
        insert(UUID.randomUUID(), owner, UUID.randomUUID(), "ATTACHED", "image/jpeg", 800, garment);
        assertThatThrownBy(() -> insert(UUID.randomUUID(), owner, UUID.randomUUID(), "ATTACHED", "image/jpeg", 800,
                garment)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void aStoredPhotoIsAJpegWithDimensions() {
        assertThatThrownBy(() -> insert(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "READY",
                "image/jpeg", null, null)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> insert(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "READY",
                "image/png", 800, null)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyADiscardedPhotoRecordsADeletedObjectAndObjectKeysStayOpaque() {
        UUID ready = UUID.randomUUID();
        insert(ready, UUID.randomUUID(), UUID.randomUUID(), "READY", "image/jpeg", 800, null);

        assertThatThrownBy(() -> jdbc.sql("UPDATE wardrobe.wardrobe_photo_upload SET object_deleted_at = now() "
                + "WHERE id = :id").param("id", ready).update()).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.sql("UPDATE wardrobe.wardrobe_photo_upload SET object_key = 'garments/x.png' "
                + "WHERE id = :id").param("id", ready).update()).isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insert(UUID id, UUID owner, UUID uploadKey, String status, String contentType, Integer size,
                        UUID garmentId) {
        boolean stored = status.equals("READY") || status.equals("ATTACHED");
        jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_photo_upload (id, owner_account_id, upload_key, content_sha256,
                            object_key, status, content_type, width, height, garment_id, created_at, expires_at,
                            ready_at, attached_at)
                        VALUES (:id, :owner, :uploadKey, :sha, :objectKey, :status, :contentType, :width, :height,
                            :garmentId, now(), now() + INTERVAL '1 day',
                            CASE WHEN :stored THEN now() END, CASE WHEN :garmentId::uuid IS NOT NULL THEN now() END)
                        """)
                .param("id", id)
                .param("owner", owner)
                .param("uploadKey", uploadKey)
                .param("sha", SHA)
                .param("objectKey", "photos/" + id + ".jpg")
                .param("status", status)
                .param("contentType", contentType)
                .param("width", size)
                .param("height", size)
                .param("garmentId", garmentId)
                .param("stored", stored)
                .update();
    }

    private UUID insertGarment(UUID owner) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                        INSERT INTO wardrobe.wardrobe_garment (id, owner_account_id, creation_key, creation_fingerprint,
                            category, color, value_provenance, created_at)
                        VALUES (:id, :owner, :key, :fingerprint, 'BAGS', 'BLACK', 'USER_ENTERED', now())
                        """)
                .param("id", id).param("owner", owner).param("key", UUID.randomUUID())
                .param("fingerprint", "0".repeat(64)).update();
        return id;
    }
}
```

**Explanation.** `:garmentId::uuid` makes PostgreSQL type the parameter when it is `null`. Nulls for `contentType` and `width` are bound as SQL `NULL` by `JdbcClient` (untyped null). If your driver rejects untyped nulls, bind them with `Types.VARCHAR`/`Types.INTEGER` as `JdbcGarmentRepository` does.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PhotoUploadSchemaIT' verify`, expected 4 tests.

### Step T16 — [ADD] `PhotoUploadApiIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/in/web/PhotoUploadApiIT.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 through the real filter chain, application, PostgreSQL and real JPEG/PNG decoding. */
class PhotoUploadApiIT extends WardrobeIntegrationTestSupport {

    /** AC-02/AC-03, S3-IMG-03/05: staging is reviewable private work, not a garment. */
    @Test
    void aGenuinePhotoIsStagedAsReviewableWorkAndCreatesNoGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-stage@example.com");
        String response = uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(),
                TestImages.jpegWithExif(1200, 1600, 1), "IMG_0001.jpg", "image/jpeg")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.width").value(1200))
                .andExpect(jsonPath("$.height").value(1600))
                .andExpect(jsonPath("$.garmentId").value(org.hamcrest.Matchers.nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        UUID id = UUID.fromString(JsonPath.read(response, "$.id"));

        byte[] reviewed = mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, allOf(containsString("no-store"),
                        containsString("private"))))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(TestImages.containsAscii(reviewed, "GPSLatitude")).isFalse();
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
    }

    /** AC-02, S3-IMG-06: the bytes decide, not the file name or the declared type. */
    @Test
    void theFileNameAndDeclaredTypeAreIgnored() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-sniff@example.com");

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.jpeg(800, 800), "photo.png",
                "image/png").andExpect(status().isCreated());
        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(),
                "not a photo".getBytes(StandardCharsets.UTF_8), "photo.jpg", "image/jpeg")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_UNSUPPORTED_FORMAT"));
    }

    /** AC-02, FR-AI-012: 511 rejected, 512 accepted. */
    @Test
    void theShortestSideBoundaryIs512Pixels() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-boundary@example.com");

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.png(511, 700), "a.png", "image/png")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_TOO_SMALL"));
        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.png(512, 700), "b.png", "image/png")
                .andExpect(status().isCreated());
    }

    /** AC-02, FR-AI-012, OPEN-S3-09: above 15,000,000 bytes is rejected before decoding. */
    @Test
    void aPhotoAboveTheSizeLimitIsRejected() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-large@example.com");

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), new byte[15_000_001], "big.jpg", "image/jpeg")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_TOO_LARGE"));
        assertThat(countRows("wardrobe.wardrobe_photo_upload")).isZero();
    }

    /** S3-IMG-02: a lost upload response is recovered by repeating it; a key cannot carry two photos. */
    @Test
    void aRetryWithTheSameKeyReturnsTheSameStagedPhotoAndADifferentPhotoIsRefused() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-retry@example.com");
        String key = UUID.randomUUID().toString();
        byte[] photo = TestImages.jpeg(900, 900);
        String first = uploadPhoto(tokens.accessToken(), key, photo, "a.jpg", "image/jpeg")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        uploadPhoto(tokens.accessToken(), key, photo, "a.jpg", "image/jpeg")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(JsonPath.<String>read(first, "$.id")));
        uploadPhoto(tokens.accessToken(), key, TestImages.jpeg(901, 900), "b.jpg", "image/jpeg")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_KEY_REUSED"));
        assertThat(photoStorage.objectCount()).isOne();
    }

    @Test
    void aMissingPhotoOrKeyIsAFieldError() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-missing@example.com");

        uploadPhoto(tokens.accessToken(), null, TestImages.jpeg(800, 800), "a.jpg", "image/jpeg")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("Idempotency-Key"))
                .andExpect(jsonPath("$.fieldErrors[0].code").value("REQUIRED"));
        mockMvc.perform(post(PHOTO_UPLOADS).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .header(IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("photo"));
    }

    /** AC-03, FR-AI-004/009: cancelled work is unusable; cancelling again is harmless. */
    @Test
    void cancellingMakesThePhotoUnusableAndCancellingAgainIsFine() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-cancel@example.com");
        UUID id = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String bearer = "Bearer " + tokens.accessToken();

        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCARDED"));
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content").header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + id).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isNoContent());
    }

    /** AC-07, S3-IMG-05: another account cannot see, review or cancel the photo; it learns nothing. */
    @Test
    void anotherAccountCannotSeeReviewOrCancelAStagedPhoto() throws Exception {
        IssuedTokens owner = registerAndLogIn("photo-owner@example.com");
        IssuedTokens stranger = registerAndLogIn("photo-stranger@example.com");
        UUID id = stagePhotoAndReturnId(owner.accessToken(), TestImages.jpeg(800, 800));
        String bearer = "Bearer " + stranger.accessToken();

        for (var request : java.util.List.of(get(PHOTO_UPLOADS + "/" + id), get(PHOTO_UPLOADS + "/" + id + "/content"),
                delete(PHOTO_UPLOADS + "/" + id), get(PHOTO_UPLOADS + "/not-a-uuid"))) {
            mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, bearer))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_FOUND"));
        }
        assertThat(photoUploadStatus(id)).isEqualTo("READY");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(id))).isTrue();
    }

    /** AC-07: no usable session, no upload and no read. */
    @Test
    void aMissingTokenOrLoggedOutSessionCannotUploadOrReadPhotos() throws Exception {
        IssuedTokens tokens = registerAndLogIn("photo-logout@example.com");
        UUID id = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().is2xxSuccessful());

        uploadPhoto(tokens.accessToken(), UUID.randomUUID().toString(), TestImages.jpeg(800, 800), "a.jpg", "image/jpeg")
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(PHOTO_UPLOADS + "/" + id + "/content")).andExpect(status().isUnauthorized());
        assertThat(countRows("wardrobe.wardrobe_photo_upload")).isOne();
    }
}
```

**Explanation.**

- **Element type.** The `for (var request : List.of(...))` loop relies on `get(...)` and `delete(...)` both returning `MockHttpServletRequestBuilder`.
- **Status matcher.** `isUnprocessableContent()` is the Spring Framework 7 name for 422; the existing Wardrobe tests use the same status. If your MockMvc version only offers `isUnprocessableEntity()`, use that.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PhotoUploadApiIT' verify`, expected 9 tests.

### Step T17 — [ADD] `PhotoGarmentApiIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/in/web/PhotoGarmentApiIT.java`

```java
package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 confirmation through the existing creation route: one garment, one photo, safe retries. */
class PhotoGarmentApiIT extends WardrobeIntegrationTestSupport {

    /** AC-04/AC-05, S3-IMG-03. */
    @Test
    void confirmingWithAReadyPhotoCreatesOneGarmentWithThePhotoAttached() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-photo@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(1200, 1600));

        String created = addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(photo,
                "subtype", "T_SHIRTS", "brand", "Uniqlo"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.photo.width").value(1200))
                .andExpect(jsonPath("$.photo.height").value(1600))
                .andExpect(jsonPath("$.recommendationEligibility").value("NOT_EVALUATED"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String garmentId = JsonPath.read(created, "$.id");

        byte[] served = mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(served).isEqualTo(photoStorage.content(PhotoUpload.objectKeyFor(photo)));
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
    }

    /** AC-06, FR-AI-010: a lost confirmation response is recovered without a duplicate garment. */
    @Test
    void aLostConfirmationResponseIsRecoveredByRepeatingTheSameRequest() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-retry@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String key = UUID.randomUUID().toString();
        String first = addGarment(tokens.accessToken(), key, withPhoto(photo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        addGarment(tokens.accessToken(), key, withPhoto(photo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(JsonPath.<String>read(first, "$.id")));
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /** S3-IMG-04: one key cannot create garments with different photos, or with and without one. */
    @Test
    void reusingTheKeyWithoutThePhotoOrWithAnotherPhotoIsRefused() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-key-reuse@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        UUID otherPhoto = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(820, 800));
        String key = UUID.randomUUID().toString();
        addGarment(tokens.accessToken(), key, withPhoto(photo)).andExpect(status().isCreated());

        addGarment(tokens.accessToken(), key, minimumGarment("TOPS", "WHITE"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        addGarment(tokens.accessToken(), key, withPhoto(otherPhoto))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        assertThat(photoUploadStatus(otherPhoto)).isEqualTo("READY");
    }

    /** AC-06/AC-07: an unusable or foreign photo never yields a garment, and never an imageless one. */
    @Test
    void aCancelledOrAnotherAccountsPhotoNeverProducesAGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-unusable@example.com");
        IssuedTokens stranger = registerAndLogIn("confirm-stranger@example.com");
        UUID cancelled = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + cancelled)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())).andExpect(status().isNoContent());
        UUID strangersPhoto = stagePhotoAndReturnId(stranger.accessToken(), TestImages.jpeg(800, 800));

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(cancelled))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_USABLE"));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(strangersPhoto))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PHOTO_UPLOAD_NOT_FOUND"));
        assertThat(countGarmentsOf(tokens.accountId())).isZero();
        assertThat(photoUploadStatus(strangersPhoto)).isEqualTo("READY");
    }

    /** S3-IMG-03/07: an attached photo belongs to one garment and cannot be cancelled. */
    @Test
    void anAttachedPhotoCannotBeUsedForASecondGarmentOrCancelled() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-attached@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(photo)).andExpect(status().isCreated());

        addGarment(tokens.accessToken(), UUID.randomUUID().toString(), withPhoto(photo))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + photo)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isConflict());
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
    }

    /** AC-06: a missing stored photo hides nothing else about the garment. */
    @Test
    void aMissingStoredPhotoAffectsOnlyThePhotoRoute() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-missing-object@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String garmentId = JsonPath.read(addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                        withPhoto(photo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
        photoStorage.lose(PhotoUpload.objectKeyFor(photo));

        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_PHOTO_UNAVAILABLE"));
        mockMvc.perform(get(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("WHITE"))
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"));
    }

    /** FR-AI-003 regression: imageless creation is unchanged and needs no storage. */
    @Test
    void imagelessCreationIsUnchangedAndHasNoPhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-imageless@example.com");
        photoStorage.failStores(true);

        String garmentId = JsonPath.read(addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                        minimumGarment("SHOES", "BLACK"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photo").value(nullValue()))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
        mockMvc.perform(get(GARMENTS + "/" + garmentId + "/photo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GARMENT_PHOTO_UNAVAILABLE"));
    }

    /** S3-IMG-07 with US-GAR-002: editing never changes the photo. */
    @Test
    void editingAPhotoBackedGarmentKeepsItsPhoto() throws Exception {
        IssuedTokens tokens = registerAndLogIn("confirm-edit@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        String garmentId = JsonPath.read(addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                        withPhoto(photo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");

        mockMvc.perform(patch(GARMENTS + "/" + garmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true,\"expectedRevision\":\"1\",\"color\":\"NAVY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("NAVY"))
                .andExpect(jsonPath("$.photo.contentType").value("image/jpeg"));
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
    }

    private static Map<String, Object> withPhoto(UUID photoUploadId, Object... extraNamesAndValues) {
        Map<String, Object> body = garment("TOPS", "WHITE", extraNamesAndValues);
        body.put("photoUploadId", photoUploadId.toString());
        return body;
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PhotoGarmentApiIT' verify`, expected 8 tests.

### Step T18 — [ADD] `ConcurrentPhotoConfirmationIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/application/ConcurrentPhotoConfirmationIT.java`

```java
package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 with real transactions: the upload row lock serializes confirmations and the expiry sweep. */
class ConcurrentPhotoConfirmationIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private AddGarmentService addGarmentService;

    @Autowired
    private PhotoUploadCleanupService cleanupService;

    @Autowired
    private PhotoUploadRepository photoUploads;

    @Autowired
    private WardrobeTransactions transactions;

    @Test
    void ofTwoConfirmationsOfOnePhotoWithDifferentKeysExactlyOneCreatesAGarment() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-confirm@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(confirm(start, tokens.accountId(), photo));
            Future<String> second = executor.submit(confirm(start, tokens.accountId(), photo));

            assertThat(List.of(first.get(20, SECONDS), second.get(20, SECONDS)))
                    .containsExactlyInAnyOrder("created", "not usable");
        } finally {
            executor.shutdownNow();
        }
        assertThat(countGarmentsOf(tokens.accountId())).isOne();
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
    }

    @Test
    void theExpirySweepSkipsAPhotoBeingConfirmedAndNeverTouchesAnAttachedOne() throws Exception {
        IssuedTokens tokens = registerAndLogIn("race-sweep@example.com");
        UUID attached = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("BAGS", "BLACK", "photoUploadId", attached.toString())).andExpect(status().isCreated());
        UUID beingConfirmed = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Object> confirmation = executor.submit(() -> transactions.inTransaction(() -> {
                photoUploads.lockOwned(tokens.accountId(), beingConfirmed).orElseThrow();
                locked.countDown();
                awaitQuietly(release);
                return null;
            }));
            assertThat(locked.await(10, SECONDS)).isTrue();
            clock.advance(Duration.ofHours(25));

            assertThat(cleanupService.run().expired()).as("locked and attached photos are skipped").isZero();
            assertThat(photoUploadStatus(beingConfirmed)).isEqualTo("READY");

            release.countDown();
            confirmation.get(10, SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
        assertThat(cleanupService.run().expired()).as("once unlocked, the expired photo is discarded").isOne();
        assertThat(photoUploadStatus(attached)).isEqualTo("ATTACHED");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(attached))).isTrue();
    }

    private Callable<String> confirm(CyclicBarrier start, UUID owner, UUID photo) {
        AddGarmentCommand command = new AddGarmentCommand(owner, UUID.randomUUID().toString(), true, "TOPS", null,
                "WHITE", List.of(), null, null, null, null, null, null, null, null, null, null, photo.toString());
        return () -> {
            start.await(10, SECONDS);
            try {
                return addGarmentService.add(command).newlyCreated() ? "created" : "replayed";
            } catch (PhotoUploadNotUsableException notUsable) {
                return "not usable";
            }
        };
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(10, SECONDS)) {
                throw new IllegalStateException("The test never released the lock");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }
}
```

**Explanation.**

- **Transaction scope.** The locking transaction stands in for a confirmation that holds the upload row, and the sweep's `FOR UPDATE SKIP LOCKED` must skip it.
- **Cleanup.** Expiry and deletion run synchronously in the test because the scheduler is disabled in the test profile.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=ConcurrentPhotoConfirmationIT' verify`, expected 2 tests.

### Step T19 — [ADD] `PhotoUploadCleanupIT.java`

**Path:** `backend/src/test/java/com/capsuleai/wardrobe/adapter/out/persistence/PhotoUploadCleanupIT.java`

```java
package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import com.capsuleai.wardrobe.domain.PhotoUpload;
import com.capsuleai.wardrobe.support.TestImages;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** US-GAR-003 lifecycle on PostgreSQL (S3-IMG-04/08): expiry, in-flight fence, retried deletion, attached safety. */
class PhotoUploadCleanupIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private PhotoUploadCleanupService cleanupService;

    @Autowired
    private PhotoUploadRepository photoUploads;

    @Autowired
    private WardrobeTransactions transactions;

    @Test
    void anUnconfirmedPhotoExpiresAndItsObjectIsDeletedOnlyWhenDue() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-expiry@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));

        clock.advance(Duration.ofHours(24).minusSeconds(1));
        assertThat(cleanupService.run().expired()).isZero();
        clock.advance(Duration.ofSeconds(1));
        assertThat(cleanupService.run().deleted()).isOne();

        assertThat(photoUploadStatus(photo)).isEqualTo("DISCARDED");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(photo))).isFalse();
        assertThat(jdbc.sql("SELECT object_deleted_at IS NOT NULL FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photo).query(Boolean.class).single()).isTrue();
    }

    @Test
    void anObjectOfAPhotoCancelledWhileReceivingIsKeptUntilTheInFlightFence() {
        UUID owner = UUID.randomUUID();
        PhotoUpload receiving = PhotoUpload.receiving(UUID.randomUUID(), owner, UUID.randomUUID(), "f".repeat(64),
                clock.instant().truncatedTo(ChronoUnit.MICROS), Duration.ofHours(24));
        transactions.inTransaction(() -> photoUploads.addIfUploadKeyUnused(receiving));
        transactions.inTransaction(() -> photoUploads.cancel(owner, receiving.id(), clock.instant(),
                Duration.ofMinutes(5)));
        photoStorage.put(receiving.objectKey(), new byte[] {1}); // the store finished after the cancel

        assertThat(cleanupService.run().deleted()).isZero();
        clock.advance(Duration.ofMinutes(5));
        assertThat(cleanupService.run().deleted()).isOne();
        assertThat(photoStorage.contains(receiving.objectKey())).isFalse();
    }

    @Test
    void aFailedDeletionIsRetriedAfterTheRetryDelayAndThenRecorded() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-retry@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        mockMvc.perform(delete(PHOTO_UPLOADS + "/" + photo)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())).andExpect(status().isNoContent());
        photoStorage.failDeletes(true);

        assertThat(cleanupService.run().failed()).isOne();
        photoStorage.failDeletes(false);
        assertThat(cleanupService.run().deleted()).as("leased until the retry delay").isZero();
        clock.advance(Duration.ofMinutes(10));
        assertThat(cleanupService.run().deleted()).isOne();

        assertThat(jdbc.sql("SELECT cleanup_attempts FROM wardrobe.wardrobe_photo_upload WHERE id = :id")
                .param("id", photo).query(Integer.class).single()).isEqualTo(2);
    }

    @Test
    void attachedPhotosAreNeverExpiredOrDeleted() throws Exception {
        IssuedTokens tokens = registerAndLogIn("cleanup-attached@example.com");
        UUID photo = stagePhotoAndReturnId(tokens.accessToken(), TestImages.jpeg(800, 800));
        addGarment(tokens.accessToken(), UUID.randomUUID().toString(),
                garment("HEADWEAR", "GREEN", "photoUploadId", photo.toString())).andExpect(status().isCreated());

        clock.advance(Duration.ofDays(30));
        var result = cleanupService.run();

        assertThat(result.expired()).isZero();
        assertThat(result.deleted()).isZero();
        assertThat(photoUploadStatus(photo)).isEqualTo("ATTACHED");
        assertThat(photoStorage.contains(PhotoUpload.objectKeyFor(photo))).isTrue();
    }
}
```

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=PhotoUploadCleanupIT' verify`, expected 4 tests.

### Step T20 — [MODIFY] `IdentityOpenApiContractIT.java`

**Path:** `backend/src/test/java/com/capsuleai/identity/adapter/in/web/IdentityOpenApiContractIT.java`

Change 1: imports. Add these to the existing import block, keeping alphabetical order:

```java
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestPart;
```

Change 2: binary success responses. BEFORE:

```java
            Map<String, Object> schema = resolve(map(map(map(success, "content"), "application/json"), "schema"));
            Set<String> returnedFields = recordComponentNames((Class<?>) responseType);
```

AFTER:

```java
            Map<String, Object> content = map(success, "content");
            if (!content.containsKey("application/json")) {
                // US-GAR-003: a private photo streamed by the application.
                assertThat(content).as("%s documents a JPEG photo response", operation.key()).containsKey("image/jpeg");
                assertThat(responseType).as("%s streams the photo", operation.key()).isEqualTo(Resource.class);
                continue;
            }
            Map<String, Object> schema = resolve(map(map(content, "application/json"), "schema"));
            Set<String> returnedFields = recordComponentNames((Class<?>) responseType);
```

Change 3: a new test for multipart requests. BEFORE:

```java
    @Test
    void everyDocumentedRequiredRequestFieldIsEnforced() throws Exception {
```

AFTER:

```java
    /** US-GAR-003: a multipart operation documents exactly the parts its handler reads. */
    @Test
    void multipartRequestsListExactlyThePartsTheEndpointsRead() {
        List<DocumentedOperation> multipart = documentedOperations.values().stream()
                .filter(operation -> requestContentTypes(operation).contains("multipart/form-data"))
                .toList();
        assertThat(multipart).as("documented multipart operations").isNotEmpty();

        for (DocumentedOperation operation : multipart) {
            Map<String, Object> schema = resolve(map(map(map(map(operation.definition(), "requestBody"), "content"),
                    "multipart/form-data"), "schema"));
            Set<String> parts = Arrays.stream(handlerFor(operation).getMethodParameters())
                    .map(parameter -> parameter.getParameterAnnotation(RequestPart.class))
                    .filter(Objects::nonNull)
                    .map(RequestPart::name)
                    .collect(Collectors.toSet());

            assertThat(map(schema, "properties").keySet())
                    .as("%s multipart parts", operation.key())
                    .containsExactlyInAnyOrderElementsOf(parts);
        }
    }

    @Test
    void everyDocumentedRequiredRequestFieldIsEnforced() throws Exception {
```

Change 4: JSON-only body checks. BEFORE:

```java
        List<DocumentedOperation> withBodies = documentedOperations.values().stream()
                .filter(operation -> operation.definition().containsKey("requestBody"))
                .toList();
        assertThat(withBodies).as("documented operations with a request body").isNotEmpty();
        return withBodies;
    }
```

AFTER:

```java
        List<DocumentedOperation> withBodies = documentedOperations.values().stream()
                .filter(operation -> requestContentTypes(operation).contains("application/json"))
                .toList();
        assertThat(withBodies).as("documented operations with a JSON request body").isNotEmpty();
        return withBodies;
    }

    private static Set<String> requestContentTypes(DocumentedOperation operation) {
        if (!operation.definition().containsKey("requestBody")) {
            return Set.of();
        }
        return map(map(operation.definition(), "requestBody"), "content").keySet();
    }
```

**Explanation.**

- **Coverage kept.** Every documented operation is still served and every handler documented. Security is still proven for all of them, including the multipart, binary and `DELETE` operations.
- **What changes.** JSON request checks now cover only JSON bodies, multipart bodies get their own exact comparison, and binary responses must be streamed `Resource`s. Nothing is weakened (S3-INT-01).
- **Report count.** It goes from 5 to 6 tests.

### Step T21 — [MODIFY] `CorsSecurityIT.java`

**Path:** `backend/src/test/java/com/capsuleai/identity/adapter/in/security/CorsSecurityIT.java` (after US-GAR-002).

BEFORE:

```java
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PATCH"))
```

AFTER:

```java
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PATCH,DELETE"))
```

BEFORE:

```java
            "DELETE, authorization",
            "PUT, content-type",
```

AFTER:

```java
            "PUT, content-type",
```

BEFORE:

```java
    @Test
    void protectedOperationPreflightNeedsNoTokenButTheActualRequestStillDoes() throws Exception {
```

AFTER:

```java
    /** US-GAR-003: a browser client may cancel a staged photo; the actual cancel still needs a usable session. */
    @Test
    void photoCancellationPreflightIsGrantedButTheCancelStillNeedsAToken() throws Exception {
        String photoUpload = "/api/v1/wardrobe/photo-uploads/" + UUID.randomUUID();

        mockMvc.perform(preflight(photoUpload, WEB_CLIENT, "DELETE", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, containsString("DELETE")))
                .andExpect(header().doesNotExist(WWW_AUTHENTICATE));

        expectReadableAuthenticationFailure(
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete(photoUpload).header(ORIGIN, WEB_CLIENT)), WEB_CLIENT);
    }

    @Test
    void protectedOperationPreflightNeedsNoTokenButTheActualRequestStillDoes() throws Exception {
```

**Explanation.**

- **Report count.** The refused-preflight parameter list loses `DELETE` (now allowed) and keeps `PUT` and an unknown header. One test is added, so the report stays at 26.
- **Imports.** The fully qualified `delete` avoids clashing with any static import; add `import static …MockMvcRequestBuilders.delete;` instead if you prefer.

**Command:** `.\mvnw.cmd '-Dtest=HarnessSmokeTest' '-Dit.test=CorsSecurityIT,IdentityOpenApiContractIT' verify`, expected 26 and 6 tests.

### Full verification

```powershell
Set-Location 'D:\CapsuleAI\backend'
.\mvnw.cmd clean verify
```

Then inspect the reports. Every report listed in P59 must exist with no failures, errors or skips. With libheif installed locally, also run with `$env:CAPSULEAI_REQUIRE_HEIF='true'`.

## 14. Manual Verification

Prerequisites:

- `docker compose --env-file .env up -d` starts PostgreSQL, Mailpit and `s3mock`.
- Boot runs with the `local` profile.
- libheif's `heif-dec` is on the PATH for the HEIC part (otherwise expect `503` for HEIC).

The script uses synthetic images generated with .NET `System.Drawing` (Windows PowerShell 5.1) and never prints a token.

```powershell
# Save as manual-gar003.ps1 (UTF-8 with BOM) outside the repository; run with: powershell -File .\manual-gar003.ps1
$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8080/api/v1'
$utf8 = New-Object System.Text.UTF8Encoding($false)
$work = Join-Path $env:TEMP ("gar003-" + [guid]::NewGuid().ToString('N').Substring(0, 8))
New-Item -ItemType Directory -Path $work | Out-Null
Add-Type -AssemblyName System.Drawing

function Image($name, $width, $height, $format) {
    $bitmap = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Blue)
    $graphics.FillRectangle([System.Drawing.Brushes]::Red, 0, 0, [int]($width / 2), [int]($height / 2))
    $graphics.Dispose()
    $path = Join-Path $work $name
    $bitmap.Save($path, $format); $bitmap.Dispose()
    return $path
}
function Json($method, $uri, $body, $headers = @{}) {
    $request = [System.Net.HttpWebRequest]::Create($uri); $request.Method = $method
    foreach ($name in $headers.Keys) { $request.Headers.Add($name, $headers[$name]) }
    if ($null -ne $body) {
        $bytes = $utf8.GetBytes($body); $request.ContentType = 'application/json; charset=utf-8'
        $request.ContentLength = $bytes.Length
        $stream = $request.GetRequestStream(); $stream.Write($bytes, 0, $bytes.Length); $stream.Close()
    } elseif ($method -eq 'POST') { $request.ContentLength = 0 }
    try { $response = $request.GetResponse() } catch [System.Net.WebException] { $response = $_.Exception.Response }
    $reader = New-Object System.IO.StreamReader($response.GetResponseStream(), $utf8)
    $text = $reader.ReadToEnd(); $status = [int]$response.StatusCode; $response.Close()
    $parsed = $null; if ($text -and $text.TrimStart().StartsWith('{')) { $parsed = $text | ConvertFrom-Json }
    [pscustomobject]@{ Status = $status; Json = $parsed }
}
function Upload($token, $key, $file) {
    $out = Join-Path $work 'upload-response.json'
    $code = & curl.exe -s -o $out -w '%{http_code}' -H "Authorization: Bearer $token" -H "Idempotency-Key: $key" -F "photo=@$file" "$base/wardrobe/photo-uploads"
    $parsed = $null; if (Test-Path $out) { $parsed = Get-Content $out -Raw | ConvertFrom-Json }
    [pscustomobject]@{ Status = [int]$code; Json = $parsed }
}
function Check($label, $ok) { "{0,-90} {1}" -f $label, $(if ($ok) { 'PASS' } else { 'FAIL' }) }

$email = "photo.demo." + [guid]::NewGuid().ToString('N').Substring(0, 8) + "@example.com"
Json POST "$base/auth/registrations" ('{"email":"' + $email + '","password":"correct horse battery staple","confirmPassword":"correct horse battery staple"}') | Out-Null
$token = (Json POST "$base/auth/login" ('{"email":"' + $email + '","password":"correct horse battery staple"}')).Json.accessToken
$auth = @{ Authorization = "Bearer $token" }

$good = Image 'garment.jpg' 900 1200 ([System.Drawing.Imaging.ImageFormat]::Jpeg)
$small = Image 'small.png' 511 700 ([System.Drawing.Imaging.ImageFormat]::Png)
$edge = Image 'edge.png' 512 700 ([System.Drawing.Imaging.ImageFormat]::Png)

$key = [guid]::NewGuid().ToString()
$staged = Upload $token $key $good
Check "genuine JPEG staged -> 201 READY, no garment yet" ($staged.Status -eq 201 -and $staged.Json.status -eq 'READY')
$retry = Upload $token $key $good
Check "same key, same photo -> 200, same id" ($retry.Status -eq 200 -and $retry.Json.id -eq $staged.Json.id)
Check "511-pixel shorter side -> 422 PHOTO_TOO_SMALL" ((Upload $token ([guid]::NewGuid()) $small).Json.code -eq 'PHOTO_TOO_SMALL')
Check "512-pixel shorter side -> 201" ((Upload $token ([guid]::NewGuid()) $edge).Status -eq 201)
$text = Join-Path $work 'fake.jpg'; Set-Content -Path $text -Value 'not a photo' -Encoding ASCII
Check "text named .jpg -> 422 PHOTO_UNSUPPORTED_FORMAT" ((Upload $token ([guid]::NewGuid()) $text).Json.code -eq 'PHOTO_UNSUPPORTED_FORMAT')
$overview = Json GET "$base/wardrobe/garments" $null $auth
Check "staging created no garment" ($overview.Json.garments.Count -eq 0)

$content = Join-Path $work 'review.jpg'
$code = & curl.exe -s -D (Join-Path $work 'headers.txt') -o $content -w '%{http_code}' -H "Authorization: Bearer $token" "$base/wardrobe/photo-uploads/$($staged.Json.id)/content"
$headers = Get-Content (Join-Path $work 'headers.txt') -Raw
Check "review content -> 200 image/jpeg, Cache-Control no-store" ([int]$code -eq 200 -and $headers -match 'image/jpeg' -and $headers -match 'no-store')

$garmentKey = [guid]::NewGuid().ToString()
$body = '{"confirmed":true,"category":"TOPS","color":"BLUE","photoUploadId":"' + $staged.Json.id + '"}'
$created = Json POST "$base/wardrobe/garments" $body ($auth + @{ 'Idempotency-Key' = $garmentKey })
Check "confirmation -> 201 garment with photo" ($created.Status -eq 201 -and $created.Json.photo.contentType -eq 'image/jpeg')
$again = Json POST "$base/wardrobe/garments" $body ($auth + @{ 'Idempotency-Key' = $garmentKey })
Check "lost-response retry -> 200 same garment" ($again.Status -eq 200 -and $again.Json.id -eq $created.Json.id)
$second = Json POST "$base/wardrobe/garments" $body ($auth + @{ 'Idempotency-Key' = [guid]::NewGuid().ToString() })
Check "same photo for a second garment -> 409 PHOTO_UPLOAD_NOT_USABLE" ($second.Status -eq 409)
$photoCode = & curl.exe -s -o NUL -w '%{http_code}' -H "Authorization: Bearer $token" "$base/wardrobe/garments/$($created.Json.id)/photo"
Check "garment photo route -> 200" ([int]$photoCode -eq 200)

$toCancel = Upload $token ([guid]::NewGuid()) $edge
$cancel = Json DELETE "$base/wardrobe/photo-uploads/$($toCancel.Json.id)" $null $auth
Check "cancel -> 204, then status DISCARDED" ($cancel.Status -eq 204 -and (Json GET "$base/wardrobe/photo-uploads/$($toCancel.Json.id)" $null $auth).Json.status -eq 'DISCARDED')

Remove-Item -Recurse -Force $work
```

Run the HEIC check when `heif-enc` is available:

```powershell
heif-enc "$env:TEMP\garment.png" -o "$env:TEMP\garment.heic"
```

Upload `garment.heic` with the `Upload` function and expect `201` with `status: READY`. Repeat with a **real phone HEIC** (synthetic subject, no personal content) and record the result as evidence V3. Do not commit device photos.

S3Mock check (objects exist and disappear after cancel and cleanup), from the repository root:

```powershell
curl.exe -s "http://127.0.0.1:9090/capsuleai-photos-local?list-type=2"
```

The response lists `photos/<id>.jpg` keys only; no user value appears in any key.

## 15. Acceptance Criteria Verification Matrix

### 15.1 AC to evidence

| AC | Backend evidence (planned tests) | Client / external evidence still required |
| --- | --- | --- |
| AC-01 Permitted route | Imageless creation unchanged (T17 `imagelessCreation…`); upload accepts library or camera bytes alike | Camera and library permission flows, denial alternatives, guidance (FR-AI-011, HW-001/002) on Android/iOS |
| AC-02 Validate supported input | T12 genuine JPEG/PNG, unsupported, truncated, pixel ceiling; T13 real HEIC; T9/T16 15,000,000/15,000,001 and 511/512; T16 file-name/type mismatch | Real device HEIC/HEIF (V3); localized messages |
| AC-03 Review or cancel | T16 review content, cancel, idempotent cancel; T11 cancel lifecycle; T9 cancel while storing | Preview display; replace flow in the client |
| AC-04 Confirm a useful garment | T10 confirmation attaches; T17 confirmation and photo route; staging creates no garment (T16) | Confirmation UI |
| AC-05 Authority, NOT_EVALUATED | T17 `NOT_EVALUATED`, Brand kept; shared validator (US-GAR-002 P6) | — |
| AC-06 Recover safely | T10/T17 replay; T17 missing object affects only the photo route; T18 races; T19 lifecycle; T9 storage failure | "Unconfirmed outcome" UI and outcome review |
| AC-07 Protect private media | T16 other account → 404, logout → 401; T17 foreign photo → 404; `private, no-store`; no presigned URLs | Client cache clearing on logout or account switch |

### 15.2 Requirement to test

| Requirement / decision | Tests |
| --- | --- |
| FR-AI-001/002/012 | T12, T13, T16, T9 |
| FR-AI-003 | T17 imageless regression, existing `AddGarmentApiIT` |
| FR-AI-004/009 | T11, T16 cancel |
| FR-AI-008/010, DATA-INT-004 | T10, T17, T18 |
| DATA-GAR-004 | T10, T17 photo association |
| DATA-RET-002 | Not triggered (no removal); lifecycle deletion of unconfirmed media: T11, T19 |
| COM-002 | T17 replay; T16 upload replay; status route |
| NFR-PRIV-001/002, BRULE-AUTH-007 | T12 metadata stripping; no logging of content (code review) |
| S3-IMG-01 | T14 (protocol); V-S3 real-provider check **outstanding** |
| S3-IMG-02 | T16 multipart, size guard |
| S3-IMG-03 | T10, T16, T17 |
| S3-IMG-04 | T9, T11, T18, T19, T15 |
| S3-IMG-05 | T16, T17 |
| S3-IMG-06 | T12, T13; V1–V7 **outstanding** |
| S3-IMG-07 | No attach or replace routes; T17 edit keeps the photo |
| S3-IMG-08 | T11, T19 (TTL injected; value pending PO) |
| AR-01–08 | `PersistenceOwnershipSqlTest`, `DatabaseSchemaOwnershipIT`, `ArchitectureDependencyTest` (unchanged) |
| S3-INT-01 | T20 contract, T21 CORS |

## 16. Definition of Done

| DoD item | Status at writing |
| --- | --- |
| AC mapped; tests written and passing | Planned |
| Contract synchronized (OpenAPI 0.10.0, contract test extended) | Planned |
| V10 forward migration verified on a fresh database and on top of V9 | Planned |
| Real storage adapter evidence (S3Mock, T14) | Planned |
| **Real provider evidence (V-S3 against private AWS S3)** | **Outstanding: needs an AWS account and bucket; not established by this guide** |
| **HEIF toolchain verification V1–V7** | **Outstanding** |
| **Pending-media TTL decided by the PO (OPEN-S3-07)** | **Blocked: lifecycle completion and Done depend on it** |
| **Multi-image HEIF policy decided (OPEN-S3-11)** | **Blocked: interim explicit rejection in place** |
| OPEN-S3-09/10/12/13/14 approved or verified | Pending |
| Full `clean verify`, CI with libheif and `CAPSULEAI_REQUIRE_HEIF=true` | Planned |
| Client evidence (permissions, preview, replace and cancel, device HEIF, cache isolation) | Outstanding |
| Review with no blocker or Critical defect | Not started |

**Honesty note.** Nothing in this guide has been compiled, run or deployed. Storage, codec and deployment claims become evidence only after the steps and verification procedures are executed and recorded.

## 17. Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| Context fails: `in-flight-fence must be longer…` | Fence ≤ storage timeout + 2 × decode timeout | Raise the fence or lower the timeouts |
| All HEIC uploads → `503` | `heif-dec` not on the PATH | Install libheif (README); or set `CAPSULE_HEIF_DECODER=heif-convert` on older distributions |
| All HEIC uploads → `PHOTO_UNREADABLE` | Decoder plugin missing (no libde265) | Install `libheif-plugin-libde265`; check `heif-dec --list-decoders` (V2) |
| `HeifDecodingIT` multi-image test fails | heif-enc or heif-dec behaves differently in that libheif version | Record the V4 result and raise OPEN-S3-11 with the PO; do not delete the test |
| `S3PhotoStorageIT.aConditionalWrite…` fails only on S3Mock | Fixture ignores `If-None-Match` | Record it (V-S3). Verify against real S3. Keep `conditional-writes=true` in production. Because keys are random and unique in the database, overwrites cannot occur in normal operation |
| Bucket missing in S3Mock | Initial-bucket environment variable name differs in 5.1.0 | The test creates the bucket itself; for compose, create it with `curl.exe -X PUT http://127.0.0.1:9090/capsuleai-photos-local` |
| Large uploads get a connection reset instead of `422` | Tomcat swallow limit | Keep `server.tomcat.max-swallow-size=20MB`; a reverse proxy needs a matching body limit |
| Image routes answer `406` on errors | Client `Accept: image/jpeg` only | Send `Accept: image/jpeg, application/problem+json` |
| A CMYK or 12-bit JPEG → `PHOTO_UNREADABLE` | The JDK JPEG decoder lacks those variants | A known limitation (phones produce 8-bit YCbCr). Raise it with the PO if real users hit it |
| PNG with `eXIf` orientation shows rotated | PNG orientation is not applied (P37 note) | A known limitation; phones store JPEG/HEIC |
| `IdentityOpenApiContractIT` fails on the photo routes | Missing `image/jpeg` content or the wrong handler return type | Return `ResponseEntity<Resource>`; document `image/jpeg` |
| `PersistenceOwnershipSqlTest` flags SQL | Unqualified `wardrobe_photo_upload` | Qualify every table with `wardrobe.` |

## 18. Final Implementation Checklist

- [ ] US-GAR-002 is fully applied first (or merge rules from [shared architecture §21](shared-architecture.md#21-shared-file-ownership-and-cumulative-order) are followed).
- [ ] V10 is the next free migration version on the branch.
- [ ] P1–P60 applied; T1–T7 applied before running any test.
- [ ] Unit tests: `.\mvnw.cmd '-Dtest=PhotoUploadTest,StagePhotoUploadServiceTest,PhotoBackedGarmentCreationTest,PhotoUploadLifecycleServiceTest,ImageIoPhotoInspectorTest,AddGarmentServiceTest,GarmentEntryValidatorTest,GarmentCreationFingerprintTest' test`
- [ ] Integration tests listed in P59 pass; `HeifDecodingIT` passes with `CAPSULEAI_REQUIRE_HEIF=true`.
- [ ] `.\mvnw.cmd clean verify` passes; reports inspected; none skipped.
- [ ] Verification V1–V7 (HEIF) executed and recorded in the PR.
- [ ] V-S3 executed against S3Mock and a private AWS test bucket with least-privilege credentials; results recorded without secrets.
- [ ] OPEN-S3-07 (TTL) and OPEN-S3-11 (multi-image) decided by the PO before claiming Done; config and tests updated if they differ.
- [ ] OPEN-S3-09/10/12/13/14 statuses re-checked.
- [ ] Manual script §14 run; HEIC from a real device verified (V3).
- [ ] README, OpenAPI 0.10.0, compose and CI updated.
- [ ] No credential, token, object key list or photo is committed; `.env` untouched.
- [ ] Client evidence requested; the story is **not** marked Done without it.
