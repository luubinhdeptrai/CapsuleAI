# Sprint 3 — Open Implementation Questions

| Field | Value |
| --- | --- |
| Sprint | Sprint 3 — trustworthy and recognizable private wardrobe, declared personal context |
| Stories | US-GAR-002, US-GAR-003, US-PROF-001 |
| Baseline | Branch `sprint_03`, commit `9d158fb` (working tree clean when inspected) |
| Decision source | [Sprint 3 plan](../sprint-plan.md) §9 and §13 (21-entry register) |
| Purpose | The questions that remain genuinely unresolved after the 2026-11-11 Product Owner approvals, with a recommendation and a provisional assumption the guides use |
| Package | [README](README.md) · [shared architecture](shared-architecture.md) · [US-GAR-002](US-GAR-002-guide.md) · [US-GAR-003](US-GAR-003-guide.md) · [US-PROF-001](US-PROF-001-guide.md) |

## 1. How to read this file

The 18 GAR/IMG/PROF solutions in the plan are **APPROVED**. They are not reopened here. Each entry below is a question that an approved decision deliberately left open: a policy value, a contract detail, a concrete technology, or one of the three cross-story INT decisions that still have their own status.

Every entry has sections A–I:

- **A**: question ID.
- **B**: related story and requirements.
- **C**: problem.
- **D**: context.
- **E**: options.
- **F**: recommendation.
- **G**: status.
- **H**: provisional assumption used by the guides.
- **I**: change impact if the decision differs.

Status values:

| Status | Meaning |
| --- | --- |
| `APPROVED` | Explicit Product Owner approval exists. No entry below has this status. |
| `RECOMMENDED — PENDING APPROVAL` | A product or contract choice. The guides implement the recommendation, but it is not approved. |
| `TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED` | A Developer-owned technical choice. It must be proven during JIT implementation before it is relied on. |
| `BLOCKED — REQUIRES PO DECISION` | Completion (DoD) of the affected capability needs a Product Owner decision. Code can be prepared using the provisional assumption. |

A provisional assumption is **never** an approval. Each guide labels the code that depends on an entry with its `OPEN-S3-xx` ID, so a different decision changes a known, small set of files.

## 2. Summary register

| ID | Decision ref | Story | Question | Recommendation (short) | Status |
| --- | --- | --- | --- | --- | --- |
| [OPEN-S3-01](#open-s3-01--compatible-api-evolution-and-shared-recovery-vocabulary) | S3-INT-01 | All | Compatible API evolution and shared recovery vocabulary | Option A: evolve existing resources compatibly; one error/revision/recovery convention | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-02](#open-s3-02--integration-environment-and-executable-evidence) | S3-INT-02 | All | Integration environment and executable evidence | Option A: isolated Testcontainers fixtures plus focused real-provider checks; CI runs for `sprint_03` PRs | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-03](#open-s3-03--jit-design-packaging-and-shared-file-coordination) | S3-INT-03 | All | JIT design packaging and shared-file coordination | Option A: this guide package, with living contracts and migrations remaining authoritative | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-04](#open-s3-04--revision-token-transport-and-conflict-status) | S3-GAR-02, S3-PROF-04 | GAR-002, PROF-001 | Revision token transport and conflict status | Opaque `revision` string in bodies; `expectedRevision` in PATCH body; `409 REVISION_CONFLICT` | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-05](#open-s3-05--partial-update-media-type-and-presence-encoding) | S3-GAR-01, S3-PROF-03 | GAR-002, PROF-001 | Partial-update media type and presence encoding | `application/json` with documented presence rules; Price replaced as a whole | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-06](#open-s3-06--no-op-revisions) | S3-GAR-04, S3-PROF-04 | GAR-002, PROF-001 | Does an edit that changes nothing create a revision? | No write, revision unchanged, `200` | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-07](#open-s3-07--pending-media-ttl) | S3-IMG-08 | GAR-003 | Pending-media TTL | 24 hours (`PT24H`), configurable | BLOCKED — REQUIRES PO DECISION |
| [OPEN-S3-08](#open-s3-08--heicheif-decoding-toolchain) | S3-IMG-06 | GAR-003 | HEIC/HEIF decoding toolchain | libheif `heif-dec` (libde265 plugin) as a bounded subprocess; JPEG/PNG through Java ImageIO | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-09](#open-s3-09--meaning-of-15-mb) | S3-IMG-06 | GAR-003 | Byte meaning of "15 MB" | 15,000,000 bytes (decimal), configurable | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-10](#open-s3-10--original-retention-and-the-stored-derivative) | S3-IMG-06, S3-IMG-08 | GAR-003 | Original retention and the stored derivative | Store only a metadata-free, orientation-corrected JPEG derivative (long edge ≤ 2048 px); do not keep the original | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-11](#open-s3-11--multi-image-heif-files) | S3-IMG-06 | GAR-003 | Multi-image HEIF files | Use the primary image; until decided, reject explicitly with `PHOTO_MULTIPLE_IMAGES` | BLOCKED — REQUIRES PO DECISION |
| [OPEN-S3-12](#open-s3-12--decoder-resource-ceilings) | S3-IMG-06 | GAR-003 | Decoder resource ceilings | 250 MP declared-pixel safety ceiling, 20 s decode timeout, 2 concurrent decodes | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-13](#open-s3-13--storage-sdk-and-local-s3-compatible-fixture) | S3-IMG-01, S3-INT-02 | GAR-003 | Storage SDK and local S3-compatible fixture | AWS SDK for Java v2 `2.46.13`; Adobe S3Mock `5.1.0` locally and in tests (MinIO is no longer an actively supported fixture) | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-14](#open-s3-14--photo-api-surface) | S3-IMG-02/03/05, S3-INT-01 | GAR-003 | Photo API surface | `/api/v1/wardrobe/photo-uploads` resource plus optional `photoUploadId` on the existing creation request; `photo` on garment detail | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-15](#open-s3-15--preference-contract-representation) | S3-PROF-02, S3-PROF-03 | PROF-001 | Preference contract representation | `/api/v1/personal-context/preferences`; need codes; every need present, `null` = unset; `revision: null` when nothing declared | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-16](#open-s3-16--published-current-context-contract-timing-and-shape) | S3-PROF-05 | PROF-001 | Published current-context contract timing and shape | Add a minimal `personalcontext.api.CurrentPersonalContext` now, with no consumer | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |

Count: **16 unresolved questions**. 8 are recommended and pending approval (01, 02, 04, 05, 09, 10, 14, 15), 6 are technical defaults that need JIT verification (03, 06, 08, 12, 13, 16), and 2 are blocked on a Product Owner decision (07, 11). The two blocked items (OPEN-S3-07 and OPEN-S3-11) block **US-GAR-003 Done**, not the preparation of code or the other two stories.

---

## OPEN-S3-01 — Compatible API evolution and shared recovery vocabulary

**A. Question ID:** OPEN-S3-01 (realizes S3-INT-01, status `PROPOSED — PENDING PO APPROVAL` in the plan).

**B. Related story and requirements:** All three stories. COM-001/002, FR-AUTH-008/009, NFR-SEC-001–003, ADA-006/008/009/015/017/018; S3-GAR-01/02/04, S3-PROF-03/04, S3-IMG-03/05.

**C. Problem:** New edit, media and preference operations must not break existing creation, paging, detail or security checks, and every new outcome (rejected, stale, known failure, pending media, uncertain) must be distinguishable.

**D. Context:** The current API is flat JSON with RFC 9457 Problem Details and a stable `code`. `IdentityOpenApiContractIT` checks that **every** `/api` handler is documented, that request and response record components equal the documented properties, and that documented required fields are enforced. CORS allows only `GET, POST` and the headers `Authorization, Content-Type, Accept, Idempotency-Key`. `CorsSecurityIT` asserts that a `PATCH` preflight is refused.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Evolve existing resources compatibly. Add only narrowly scoped operations (PATCH a garment, a photo-upload resource, a preferences resource). Add response fields additively. Use one convention: `VALIDATION_FAILED` (400), `REVISION_CONFLICT` (409), `TEMPORARILY_UNAVAILABLE` (503, outcome not confirmed). Extend the contract test for multipart and binary responses. Allow `PATCH` and `DELETE` in CORS. | Preserves current clients. One recovery language. The contract test keeps its all-handler strength. | Additive fields still need client review. The contract test needs careful extension. | Medium | High: matches the current small API and its strict contract test |
| B | Introduce a new versioned representation (`/api/v2/...`) wherever new behaviour appears | Old behaviour stays untouched | Duplicate routes, models and security tests; no incompatible consumer exists to justify it | High | Low |

**F. Recommendation:** Option A.

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- The guides add `revision` to the garment response (US-GAR-002) and `photo` to the garment response (US-GAR-003).
- `photoUploadId` becomes an optional field of the existing creation request.
- All three guides use the shared error codes in [shared architecture §11](shared-architecture.md#11-error-model-and-recovery-vocabulary).
- CORS gains `PATCH` (US-GAR-002) and `DELETE` (US-GAR-003).
- The 503 wording becomes operation-neutral.

**I. Change impact:** If Option B is chosen, each guide's web adapter, OpenAPI step and contract-test step move to a `/api/v2` controller. The domain, application and persistence steps are unaffected.

---

## OPEN-S3-02 — Integration environment and executable evidence

**A. Question ID:** OPEN-S3-02 (realizes S3-INT-02, status `PROPOSED — PENDING PO APPROVAL` in the plan).

**B. Related story and requirements:** All three stories. Test Strategy real-integration and failure policy; DoD integration/CI/runtime triggers; ADA-006/007/013/014; AR-08.

**C. Problem:** Real persistence, storage and decoder boundaries must be checked without relying on a developer's database and without treating an emulator as production evidence.

**D. Context:**

- Integration tests use an isolated PostgreSQL 17.11 Testcontainers database (`IdentityTestConfiguration`) and a Mailpit container.
- No storage or codec fixture exists.
- `.github/workflows/backend-ci.yml` runs only for pushes to `main` and PRs into `main` or `sprint_02`. It fails a required report that has any skipped test.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Extend the isolated Testcontainers pattern: PostgreSQL as today, an S3-compatible container for the storage adapter, the HEIF decoder installed on the CI runner. Add a separate, manual real-provider verification against a private AWS S3 bucket. Add `sprint_03` to the CI PR trigger. | Deterministic, isolated runs. Real protocol and real codec. CI runs on the actual integration path. | Extra container start-up. The real-provider check needs least-privilege credentials kept outside the repository. | Medium | High |
| B | A shared, resettable integration stack (one database, bucket and decoder host) with serialized suites | Useful for heavyweight codecs | Risk of state leakage between runs; needs scheduling | Medium–High | Low for this project size |

**F. Recommendation:** Option A.

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- **US-GAR-002** step `backend-ci.yml` adds `sprint_03` to `pull_request.branches` and adds its reports.
- **US-GAR-003** adds:
  - an S3Mock container to `compose.yaml` and to its storage integration tests;
  - a CI step that installs libheif;
  - `CAPSULEAI_REQUIRE_HEIF=true` in CI, so a missing decoder fails rather than skips.
- Real AWS evidence remains an explicit, outstanding manual activity. It is never claimed from emulator runs.

**I. Change impact:** Option B replaces the per-test containers with configuration that points at the shared stack. Test classes stay; their support classes change.

---

## OPEN-S3-03 — JIT design packaging and shared-file coordination

**A. Question ID:** OPEN-S3-03 (realizes S3-INT-03, status `DEFERRED TO JIT DESIGN` in the plan).

**B. Related story and requirements:** All stories. Workflow Sections 26/27/31–34; DD-006; DoD documentation and review conditions.

**C. Problem:** Concrete design must live somewhere without competing with the living OpenAPI and migrations, and three stories touch the same files.

**D. Context:** The Product Owner asked for this guide package. The living artifacts are `docs/05-api/openapi.yaml`, Flyway migrations, the CI workflow and the code itself.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Self-contained story guides plus a shared-convention file. The living contract and migrations stay authoritative once implemented. A shared-file ownership matrix defines the cumulative order. | Readable handoff. Clear producer/consumer ownership. | Guides drift from code after implementation unless corrected. | Medium | High |
| B | Design notes only in PRs and contracts | Least documentation | Lifecycle and concurrency decisions are hard to find | Low | Medium |

**F. Recommendation:** Option A, as realized by this package. After a story is implemented, the code, OpenAPI and migrations win over any guide text.

**G. Status:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Provisional assumption:** The guides are cumulative in the order GAR-002 → GAR-003 → PROF-001 ([shared architecture §21](shared-architecture.md#21-shared-file-ownership-and-cumulative-order)). A guide that is applied out of order uses the merge rules in that section.

**I. Change impact:** If Option B is chosen, the guides become reference material only. No code changes.

---

## OPEN-S3-04 — Revision token transport and conflict status

**A. Question ID:** OPEN-S3-04 (refines S3-GAR-02 and S3-PROF-04, both APPROVED; uses S3-INT-01).

**B. Related story and requirements:** US-GAR-002 AC-05/07/08; US-PROF-001 AC-05/06; FR-WAR-003; DATA-INT-004; COM-002.

**C. Problem:** S3-GAR-02 approves an opaque revision token and atomic expected-revision acceptance, but leaves "header versus body token and exact conflict status" to JIT design.

**D. Context:**

- **Mobile clients** are the primary consumers.
- **CORS** exposes no response headers today (`Access-Control-Expose-Headers` is absent and asserted absent in `CorsSecurityIT`).
- **The contract test** checks body fields and required request fields, but not headers.
- **RFC 9110** ties `412 Precondition Failed` to conditional request headers such as `If-Match`.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Responses carry `revision` (opaque string). PATCH bodies carry required `expectedRevision`. A stale value gives `409 Conflict`, code `REVISION_CONFLICT`. | Visible to the existing contract test. No CORS header exposure. Simple for mobile JSON clients. | Not HTTP's native conditional-request mechanism; caches cannot use it. | Low | High |
| B | `ETag` response header plus `If-Match` request header; `412` when stale, `428` when missing | Standard HTTP semantics; usable by intermediaries | Needs `Access-Control-Expose-Headers: ETag` and `If-Match` in allowed headers. The contract test must learn header requirements. More client plumbing. | Medium | Medium |
| C | Both A and B | Maximum compatibility | Two sources of truth that can disagree | High | Low |

**F. Recommendation:**

- Option A.
- The token is the decimal text of a positive counter (for example `"3"`). Clients must treat it as opaque and echo it unchanged.
- One code, `REVISION_CONFLICT`, is used by Wardrobe and Personal Context.

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- US-GAR-002 and US-PROF-001 implement Option A.
- A malformed `expectedRevision` is `400 VALIDATION_FAILED` with field code `INVALID_FORMAT`.
- A missing one is `REQUIRED`.

**I. Change impact:** Option B changes the PATCH controllers, response construction (headers), CORS configuration and tests, the OpenAPI operations and the contract test. Domain, application and persistence are unchanged, because they already use a `long` revision.

---

## OPEN-S3-05 — Partial-update media type and presence encoding

**A. Question ID:** OPEN-S3-05 (refines S3-GAR-01 and S3-PROF-03, both APPROVED).

**B. Related story and requirements:** US-GAR-002 AC-01–AC-05; US-PROF-001 AC-01/02/06; FR-WAR-003; FR-PROF-002/003; BRULE-GAR-010.

**C. Problem:** The approved presence-aware PATCH must distinguish omitted, `null` and empty values, and must treat the Price amount and currency as one value. The exact media type and encoding are not decided.

**D. Context:** RFC 7396 JSON Merge Patch merges nested objects member by member, so `"price": {"amount": "5"}` would keep the old currency, which contradicts "Price is one coherent value". `IdentityOpenApiContractIT` reads only `application/json` request bodies and requires a record request type. Jackson 3.1.5 (the version on this branch) gives a `JsonNode` record component `null` when the property is absent and a `NullNode` when it is JSON `null`. This was checked in `JsonNodeDeserializer.getAbsentValue`/`getNullValue` bytecode.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | `application/json`. Absent field: unchanged. `null`: clear an optional scalar, or the whole Price. `[]` or `null` for Materials/Styles: clear the set. `price` is always replaced as a whole object. Profile needs are cleared one by one. | Matches the approved semantics exactly. Fits the contract test. One rule set for both modules. | A project-specific convention that must be documented. | Low–Medium | High |
| B | `application/merge-patch+json` (RFC 7396) | A recognized standard | Nested merge breaks the Price pair rule; the contract test reads only `application/json` | Medium | Low |
| C | `application/json-patch+json` (RFC 6902) operations | Very explicit | Verbose for clients; path-based validation; poor fit for confirmation | High | Low |

**F. Recommendation:** Option A, documented in OpenAPI and [shared architecture §10](shared-architecture.md#10-presence-aware-partial-updates).

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- The request records use `tools.jackson.databind.JsonNode` components.
- A JSON value of the wrong type (for example a number for `subtype`, or a string for `materials`) is rejected for that field with `UNSUPPORTED_VALUE`.
- PATCH is therefore stricter than the existing POST, which lets Jackson coerce scalars.
- `commonNeeds: null` is rejected rather than clearing every need. A need must be cleared individually.

**I. Change impact:** Option B or C replaces the request records and the mapping classes in the web adapters (`GarmentRevisionRequest`, `PreferencesRevisionRequest`). The application `FieldChange` model is unchanged.

---

## OPEN-S3-06 — No-op revisions

**A. Question ID:** OPEN-S3-06 (refines S3-GAR-04 and S3-PROF-04).

**B. Related story and requirements:** US-GAR-002 AC-05/07; US-PROF-001 AC-06; FR-WAR-006; FR-ANL-014.

**C. Problem:** If a confirmed PATCH results in exactly the current values, should the revision advance?

**D. Context:**

- Advancing the revision on a no-op would make future consumers mark advice outdated for no reason.
- Not advancing it keeps the token stable.
- The stale-precondition check still applies in both cases.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | After the precondition passes, compare the resulting profile with the current one. If they are equal, write nothing and return `200` with the unchanged revision. | No spurious revisions; no wasted writes | One extra comparison | Low | High |
| B | Every accepted confirmed PATCH writes and increments the revision | Simplest mental model | Spurious "changed" signals for future consumers | Low | Medium |

**F. Recommendation:** Option A. A stale `expectedRevision` is still `409`, even when the values would not change. The client then rereads.

**G. Status:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Provisional assumption:** Both PATCH services return a `changed` flag, and the guides test both branches. For preferences, a create-if-unset that declares nothing writes nothing and leaves `revision: null`.

**I. Change impact:** Option B removes the equality short-circuit in `ReviseGarmentService` and `RevisePreferencesService` and adjusts two tests per story.

---

## OPEN-S3-07 — Pending-media TTL

**A. Question ID:** OPEN-S3-07 (S3-IMG-08 APPROVED for the lifecycle; the exact TTL is explicitly not selected).

**B. Related story and requirements:** US-GAR-003 AC-03/06/07; DATA-RET-002; BRULE-AUTH-007; NFR-PRIV-001.

**C. Problem:** How long may an unconfirmed staged photo exist before it expires and is deleted?

**D. Context:**

- **DATA-RET-002's 30-day maximum** applies to removed-garment originals. It does not set a pending TTL.
- **Product need:** the user may photograph a garment, be interrupted, and come back to confirm it later the same day.
- **Privacy:** minimization favours a short life.
- **Implementation:** the lifecycle (cancel/expire → cleanup-pending → cleaned) works the same for any TTL value.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | 1 hour | Strong minimization | An interrupted user often loses the staged photo and must upload it again | Low | Medium |
| B | 24 hours | Covers interruptions within a day; still short-lived private data | Up to a day of unconfirmed private media | Low | High |
| C | 7 days | Lets users finish over several days | Longer exposure of unconfirmed media; weak minimization | Low | Low |

**F. Recommendation:** Option B: `PT24H`, configurable through `capsuleai.wardrobe.photo-upload.pending-ttl`. Cleanup starts at expiry; deletion is retried with backoff until the object is gone.

**G. Status:** BLOCKED — REQUIRES PO DECISION. The plan states that an unresolved TTL blocks lifecycle completion and Done, not preparation.

**H. Provisional assumption:**

- The code uses `PT24H` as the default value of a property.
- Tests inject the TTL and use a controllable clock, so no test depends on the value.
- The guide and README call it "provisional, pending Product Owner approval".

**I. Change impact:** Changing the value changes only `application.properties` and README text. No code, migration or test changes.

---

## OPEN-S3-08 — HEIC/HEIF decoding toolchain

**A. Question ID:** OPEN-S3-08 (S3-IMG-06 APPROVED as the investigation direction only: a bounded local decoding process behind the Wardrobe media port).

**B. Related story and requirements:** US-GAR-003 AC-02/07; FR-AI-001/002/012; NFR-PRIV-001/002; ADA-006/007/013.

**C. Problem:** A concrete, verifiable toolchain is needed that genuinely decodes JPEG/JPG, PNG and HEIC/HEIF, safely and within bounded resources.

**D. Context:**

- Java ImageIO decodes JPEG and PNG, but not HEIC.
- No Java-only HEIC decoder is mature enough.
- **libheif** is the reference open-source HEIF library. Its `heif-dec` command-line decoder:
  - applies HEIF rotation and mirroring transformations by default;
  - writes EXIF/XMP only when asked (`--with-exif`, `--with-xmp`);
  - has a `--disable-limits` option, so its security limits are on by default;
  - decodes **every top-level image**, naming outputs `name-1.png`, `name-2.png` when there are several;
  - has no option to select only the primary image.
- These facts come from the upstream `examples/heif_dec.cc`, read on 2026-11-11.
- libheif decodes HEVC-coded HEIC through a codec plugin, usually **libde265**.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | libheif `heif-dec` + libde265 run as a **bounded subprocess**: private temp directory, wall-clock timeout, output size check. Its PNG output is then read by the same ImageIO path as JPEG/PNG. JPEG/PNG never leave Java. | Crash/memory isolation. Uses libheif's default security limits. One Java normalization path. Native code only for HEIF. | Native package needed on servers, developer machines and CI. Process-management code. | Medium | High |
| B | libvips CLI (`vips`) for all three formats | One native tool; autorotation and metadata stripping built in | Native code for every upload, including JPEG/PNG; larger dependency surface | Medium | Medium |
| C | In-process HEIF via JNI/FFM bindings to libheif | No subprocess | A native crash takes down the JVM; FFM is preview-only in Java 21 | High | Low |

**F. Recommendation:**

- **Toolchain:** Option A. Preferred packages are libheif ≥ 1.17 with the libde265 plugin, from the deployment OS (Ubuntu 24.04: `libheif-examples` and `libheif-plugin-libde265`).
- **Executable:** configurable as `capsuleai.wardrobe.photo.heif-decoder-command`, default `heif-dec`. Some distributions name the older binary `heif-convert`.
- **Verification procedure** (before relying on it; record the results in the PR):

| # | Verification step | Pass condition |
| --- | --- | --- |
| V1 | `heif-dec --version` (or `heif-convert --version`) on the target OS image and the CI runner | Prints a libheif version ≥ 1.17 |
| V2 | `heif-dec --list-decoders` (if supported by that version) | Lists an HEVC decoder (libde265) |
| V3 | Decode a real iPhone HEIC (synthetic subject, no personal content) and a real Android HEIF: `heif-dec in.heic out.png` | Exit code 0, one `out.png`, dimensions match the device's report, orientation correct |
| V4 | Encode a 2-image HEIF with `heif-enc a.png b.png -o two.heic` and decode it | Two numbered outputs appear; confirms OPEN-S3-11 handling |
| V5 | Decode a truncated HEIC (first 60% of bytes) and a JPEG renamed `.heic` | Non-zero exit or no output, within the timeout |
| V6 | Run `ImageIoPhotoInspectorIT` and `HeifDecodingIT` locally and in CI with `CAPSULEAI_REQUIRE_HEIF=true` | All pass, none skipped |
| V7 | Licence and update review: libheif (LGPL-3.0) and libde265 (LGPL-3.0) as separately installed OS packages invoked as processes; record the security-update channel | Written note in the PR; no static linking into the JAR |

**G. Status:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED. Verification is still required: nothing here has been executed against this repository.

**H. Provisional assumption:**

- US-GAR-003 implements `ImageIoPhotoInspector` with a `HeifDecoder` subprocess helper.
- Integration tests that need HEIF skip only when `CAPSULEAI_REQUIRE_HEIF` is not `true`. CI sets it, so a missing decoder fails the build.

**I. Change impact:** Option B replaces `HeifDecoder` and parts of `ImageIoPhotoInspector`. The application port `PhotoInspector` and everything above it are unchanged.

---

## OPEN-S3-09 — Meaning of "15 MB"

**A. Question ID:** OPEN-S3-09 (S3-IMG-06 lists "MB byte interpretation" as needing review).

**B. Related story and requirements:** US-GAR-003 AC-02; FR-AI-012 ("maximum 15 MB file size … at/beyond 15 MB").

**C. Problem:** Is the limit 15,000,000 bytes or 15 × 1,048,576 = 15,728,640 bytes?

**D. Context:**

- Current iOS and Android file browsers display decimal megabytes.
- A user who sees "15.3 MB" expects rejection.
- AC-02 requires explicit at-limit and above-limit cases.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | 15,000,000 bytes (decimal MB) | Matches what phones display; easy to explain | Rejects files between 15.0 and 15.7 MiB that a binary reading would accept | Low | High |
| B | 15,728,640 bytes (MiB) | Slightly more permissive | Files shown as "15.5 MB" are accepted, which contradicts the visible limit | Low | Medium |

**F. Recommendation:** Option A. The boundary tests are 15,000,000 (accepted) and 15,000,001 (rejected, `PHOTO_TOO_LARGE`). The limit is configurable as `capsuleai.wardrobe.photo.maximum-bytes`.

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- The application enforces the exact limit on counted bytes.
- The servlet multipart ceiling is set higher (`16MB`, i.e. 16 MiB) only as a transport guard.
- Anything that exceeds the transport guard is also reported as `PHOTO_TOO_LARGE`.

**I. Change impact:** Option B changes one property value, the multipart ceiling, two boundary tests and README text.

---

## OPEN-S3-10 — Original retention and the stored derivative

**A. Question ID:** OPEN-S3-10 (S3-IMG-06 and S3-IMG-08 list "original retention/normalization" as needing policy review).

**B. Related story and requirements:** US-GAR-003 AC-04/07; DATA-GAR-004; DATA-RET-002; NFR-PRIV-001/002; BRULE-AUTH-007.

**C. Problem:**

- Should CapsuleAI keep the uploaded original, which may contain EXIF/GPS data, or only a normalized derivative?
- At what resolution should the derivative be kept?

**D. Context:**

- **Privacy:** S3-IMG-06 requires orientation correction and stripping of unnecessary EXIF/GPS from served and stored presentation derivatives. Purpose limitation applies (no training use).
- **Bounded decoding:** a 200-megapixel phone image decoded at full size needs about 600 MB of memory. Subsampled decoding bounds memory without rejecting the input.
- **Future AI:** US-GAR-004 analysis is not selected. Keeping originals "for later" would be speculative.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Keep only a normalized JPEG: orientation corrected, all metadata removed, long edge ≤ 2048 px (smaller images keep their size), quality 0.9. Discard the original after validation. | Strong minimization (no GPS). Bounded memory and storage. One object per photo. | Original detail is lost for any future feature; the user must upload again. | Medium | High |
| B | Keep the original privately, plus a normalized derivative for display | Future reprocessing is possible | Retains GPS/EXIF; doubles storage and the deletion surface; speculative | High | Low |
| C | Keep a normalized derivative at full resolution | No downscaling | Memory and storage grow with sensor size; needs a pixel limit, which is a user-visible policy | Medium | Medium |

**F. Recommendation:** Option A.

**G. Status:** RECOMMENDED — PENDING APPROVAL. Product Owner/BA own the retention decision.

**H. Provisional assumption:**

- US-GAR-003 stores exactly one object per accepted photo: `image/jpeg`, metadata-free, long edge ≤ `capsuleai.wardrobe.photo.derivative-long-edge` (default `2048`).
- Validation uses the **original** dimensions (shortest side ≥ 512) read from the image header before any downscaling.
- The response reports the derivative's dimensions.

**I. Change impact:** Option B adds a second object key column, a second storage write and its cleanup. Option C changes one property and requires an OPEN-S3-12 pixel limit approved by the PO.

---

## OPEN-S3-11 — Multi-image HEIF files

**A. Question ID:** OPEN-S3-11 (S3-IMG-06 lists "multi-image HEIF selection" as needing contract/policy review).

**B. Related story and requirements:** US-GAR-003 AC-02; FR-AI-012.

**C. Problem:** A HEIF container may hold several top-level images. Which one represents the garment?

**D. Context:**

- A normal iPhone or Android photo has one top-level (primary) image. Thumbnails, depth and gain maps are auxiliary and not top-level.
- `heif-dec` decodes every top-level image and has no primary-only option (OPEN-S3-08).
- Silently picking an arbitrary image is not acceptable.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Use the file's **primary** image (what the gallery shows) | Matches user expectation | Needs primary identification (a small libheif helper or parsing `heif-info` output); more native code | Medium | High as the final policy |
| B | Reject multi-top-level files explicitly with `PHOTO_MULTIPLE_IMAGES` and offer replacement or manual entry | Simple; never shows the wrong image | Rejects a technically supported file | Low | Medium as an interim measure |
| C | Use the first decoded image | Simple | May not be the primary; silent choice | Low | Low |

**F. Recommendation:** Option A as the product policy. Option B as the explicit interim behaviour until A is approved and verified (V4 in OPEN-S3-08).

**G. Status:** BLOCKED — REQUIRES PO DECISION.

**H. Provisional assumption:** `ImageIoPhotoInspector` rejects HEIF input that decodes to more than one image with reason `MULTIPLE_IMAGES`, reported as `422 PHOTO_MULTIPLE_IMAGES` with replacement and manual-continuation guidance. This is explicit, never silent.

**I. Change impact:** Approving Option A replaces the multi-output branch in `HeifDecoder` with primary-image selection. Two tests change. The API and database are unaffected.

---

## OPEN-S3-12 — Decoder resource ceilings

**A. Question ID:** OPEN-S3-12 (S3-IMG-06: "Bound decode memory, pixel allocation, processing time and temporary resources … do not invent numerical limits or silently reject otherwise supported input").

**B. Related story and requirements:** US-GAR-003 AC-02/07; NFR-SEC; ADA-013.

**C. Problem:** Decompression bombs (for example a 15 MB PNG that declares 100,000 × 100,000 pixels) and slow decodes must be bounded without inventing a user-visible pixel rule.

**D. Context:**

- The largest current phone sensors produce about 200 MP (16,320 × 12,240).
- Subsampled ImageIO decoding (OPEN-S3-10) bounds memory, but not time.
- `ImageReader.abort()` lets a watchdog stop JPEG and PNG reads.
- libheif applies its own default limits.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Technical safety ceilings, all configurable: declared pixels ≤ 250,000,000 (above any current phone), decode wall-clock ≤ 20 s, at most 2 concurrent decodes. Exceeding a ceiling gives `PHOTO_UNREADABLE` ("could not be processed"). | Bounds every resource; no ordinary phone photo is affected | A genuine > 250 MP image is refused, which is a narrow user-visible effect | Low | High |
| B | Time limit only | No pixel rule at all | A large declared allocation can exhaust memory before the timeout | Low | Low |
| C | A user-visible maximum resolution (for example 50 MP) | Predictable resources | A new user-visible rule that rejects real 108/200 MP phone photos; needs PO approval | Low | Low |

**F. Recommendation:** Option A. The ceilings are safety bounds, not product policy. Any change that could affect ordinary phone photos goes to the PO.

**G. Status:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED. Measure decode time and peak memory with the V3 fixtures on the deployment size, then adjust.

**H. Provisional assumption:**

| Property | Default |
| --- | --- |
| `capsuleai.wardrobe.photo.maximum-declared-pixels` | `250000000` |
| `capsuleai.wardrobe.photo.decode-timeout` | `PT20S` |
| `capsuleai.wardrobe.photo.maximum-concurrent-decodes` | `2` |

When all decode slots are busy, the request waits up to the decode timeout and then answers `503 TEMPORARILY_UNAVAILABLE` (retryable). It never answers `PHOTO_UNREADABLE` for that case.

**I. Change impact:** Only property values and README text change.

---

## OPEN-S3-13 — Storage SDK and local S3-compatible fixture

**A. Question ID:** OPEN-S3-13 (S3-IMG-01 APPROVED: an S3-compatible local service for development and tests, plus private managed Amazon S3 as the proposed deployed target. It says "select an actively supported local fixture and SDK during JIT work").

**B. Related story and requirements:** US-GAR-003 AC-04/06/07; DATA-GAR-004; ADA-006/007; S3-INT-02.

**C. Problem:** Which SDK version and which local fixture should be used?

**D. Context:**

- **MinIO:** the project stopped publishing community Docker images and binaries in October 2025 (source-only distribution), so it is no longer an actively supported, pinnable fixture.
- **Adobe S3Mock:** Apache-2.0, built for tests, version `5.1.0` published June 2026. It supports AWS SDK v2 clients.
- **AWS SDK for Java v2:**
  - `PutObjectRequest.ifNoneMatch("*")` supports conditional writes (`412` if the key exists);
  - `ClientOverrideConfiguration.apiCallTimeout`/`apiCallAttemptTimeout` bound calls (timeouts are disabled by default);
  - BOM `software.amazon.awssdk:bom:2.46.13` exists on Maven Central.
- Whether S3Mock 5.1.0 honours `If-None-Match` is **not verified**.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | AWS SDK v2 `s3` (sync client) pinned through BOM `2.46.13`. `adobe/s3mock:5.1.0` in `compose.yaml` and as a Testcontainers `GenericContainer`. `forcePathStyle(true)` and an endpoint override locally. Conditional writes are configurable (`capsuleai.wardrobe.photo-storage.conditional-writes`, default `true`) and disabled for S3Mock only if V-S3 shows it unsupported. | Maintained, permissively licensed fixture. Same SDK as production. | An emulator is not S3; real-provider evidence is still required. | Medium | High |
| B | LocalStack S3 | Broad AWS emulation | Heavier; licensing and authentication changes for recent community images need review | Medium | Medium |
| C | SeaweedFS or Garage S3 gateway | Real storage engines | More operational set-up for tests | Medium–High | Medium |

**F. Recommendation:** Option A.

The verification procedure "V-S3" is a set of steps, recorded in the PR:

| # | Step |
| --- | --- |
| 1 | Put, get, head and delete against S3Mock through `S3PhotoStorageIT` |
| 2 | A conditional put (`ifNoneMatch("*")`) on an existing key returns `412` against S3Mock; if not, set `conditional-writes=false` for the fixture only |
| 3 | The same three operations plus the conditional put against a private AWS S3 test bucket, with a least-privilege IAM user: `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject` on one prefix only |
| 4 | Bucket policy blocks public access |

**G. Status:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED. AWS account, region, budget and production configuration remain outside this package.

**H. Provisional assumption:** US-GAR-003's POM, compose, configuration and test steps use Option A.

**I. Change impact:** Option B or C changes `compose.yaml`, the container definition in `S3PhotoStorageIT` and `PhotoStorageTestConfiguration`. Production code is unaffected, because only the endpoint changes.

---

## OPEN-S3-14 — Photo API surface

**A. Question ID:** OPEN-S3-14 (realizes S3-IMG-02 multipart proxy, S3-IMG-03 staged upload then explicit confirmation, S3-IMG-05 application-authorized streaming, S3-IMG-07 new garment only; all APPROVED; contract shape uses S3-INT-01).

**B. Related story and requirements:** US-GAR-003 AC-03/04/06/07; FR-AI-004/008/010; DATA-GAR-004; DATA-INT-004.

**C. Problem:** What are the routes and representations for staging, inspecting, cancelling and confirming a photo, and for reading an attached photo?

**D. Context:**

- **Creation contract:** `POST /api/v1/wardrobe/garments` takes JSON with an `Idempotency-Key`.
- **Controller convention:** one controller per cohesive base path (repository memory note).
- **Overview:** the current summary fields are exactly seven, asserted by US-WAR-001 tests and manual checks.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | A new resource `/api/v1/wardrobe/photo-uploads`: `POST` multipart (`photo` part, `Idempotency-Key`); `GET /{id}`; `GET /{id}/content`; `DELETE /{id}`. Confirmation reuses `POST /api/v1/wardrobe/garments` with an optional `photoUploadId`, included in the creation fingerprint only when present. `GET /api/v1/wardrobe/garments/{id}/photo` streams the attached photo. Garment detail gains a nullable `photo` object. The overview is unchanged. | One creation path and one idempotency model. Imageless creation is unchanged. A cohesive media controller. | Clients make two calls (stage, then confirm). Overview cards cannot show a photo indicator yet. | Medium | High |
| B | A dedicated confirmation route `POST /photo-uploads/{id}/garment` | Explicit binding of stage to garment | A second creation path with duplicated validation and idempotency | Medium | Medium |
| C | Also add `photo` to overview summaries | Overview cards can show photos | Changes the US-WAR-001 summary contract without a selected need | Low | Low until a client needs it |

**F. Recommendation:** Option A.

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- US-GAR-003 implements Option A.
- A `photoUploadId` that cannot be used (unknown, another owner's, cancelled, expired, not yet ready, already attached to another garment) never produces an imageless success. It gives `404 PHOTO_UPLOAD_NOT_FOUND` or `409 PHOTO_UPLOAD_NOT_USABLE`.

**I. Change impact:** Option B moves confirmation into `WardrobePhotoUploadController` and a new service. Option C adds one field to `GarmentSummaryResponse`, OpenAPI and the US-WAR-001 tests.

---

## OPEN-S3-15 — Preference contract representation

**A. Question ID:** OPEN-S3-15 (refines S3-PROF-02 and S3-PROF-03, APPROVED).

**B. Related story and requirements:** US-PROF-001 AC-01/02/05/06; FR-PROF-002/003; SRS §3.14.1, §4.10; BRULE-PROF-002; DATA-AUTH-003.

**C. Problem:**

- The SRS names six need dimensions ("Everyday / Casual" …) but defines no codes.
- The representation of absent, zero and empty values, and of a profile that was never set, must be fixed.

**D. Context:**

- **Style codes** come from SRS §4.10: `MINIMAL`, `CASUAL`, `SMART_CASUAL`, `CLASSIC`, `STREETWEAR`, `SPORTY`, `FORMAL`.
- **Priorities:** Not Relevant = 0, Low = 1, Medium = 2, High = 3.
- **Response convention:** the garment response keeps every field present, with `null` when absent.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Resource `/api/v1/personal-context/preferences` (`GET`, `PATCH`). Need codes `EVERYDAY_CASUAL`, `WORK_PROFESSIONAL`, `SCHOOL_UNIVERSITY`, `FORMAL_SPECIAL_EVENT`, `TRAVEL`, `SPORT_ACTIVE`. Response `commonNeeds` always lists all six, with `null` meaning unset and integers `0..3`. `styles` is an array, `[]` meaning no declared style. A never-declared profile answers `200` with `revision: null`. | Follows the garment null convention. Absent is distinct from 0. No 404 for a normal new user. | New code identifiers are introduced (documented here). | Low | High |
| B | Omit unset needs from the response object | Smaller payloads | Absence becomes implicit; inconsistent with garment responses | Low | Medium |
| C | `404` until the first declaration | REST-typical | Treats a normal state as an error; the client branches | Low | Low |

**F. Recommendation:** Option A.

**G. Status:** RECOMMENDED — PENDING APPROVAL.

**H. Provisional assumption:**

- **US-PROF-001** implements Option A.
- **Unset styles and an empty style selection are not distinguished.** Both mean "no declared style context". S3-PROF-02 requires an empty selection to round-trip as `[]`, and it does.
- **Contract change:** if the PO later wants "never set" to differ from "deliberately none", that is a contract change, described in I.

**I. Change impact:**

- **Need codes:** renaming them changes the `CommonNeed` enum, the V11 CHECK constraint (before first deployment only) and OpenAPI.
- **Unset vs. empty styles:** distinguishing them adds a `styles_declared` column and a nullable `styles` response.

---

## OPEN-S3-16 — Published current-context contract timing and shape

**A. Question ID:** OPEN-S3-16 (S3-PROF-05 APPROVED: "a narrow `personalcontext.api` current-context contract when needed by the selected implementation").

**B. Related story and requirements:** US-PROF-001 AC-03/05; FR-PROF-004; FR-ANL-014; BRULE-COV-006; ADA-017/018; AR-02–07.

**C. Problem:** Should the published contract be created in Sprint 3, when no consumer exists yet? If so, what shape should it have?

**D. Context:**

- **Consumers:** future advice and Coverage modules are skeletons.
- **Constraints:**
  - architecture tests forbid other modules from using anything except `personalcontext.api`;
  - published types must not expose `domain` or `application` classes;
  - no fake consumer may be built.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Add now: `CurrentPersonalContext.find(UUID accountId)` returns `Optional<DeclaredPersonalContext>` (revision string, style codes, need → priority map with only declared needs). It is implemented by an application service, verified by one integration test and the existing architecture tests, and has no consumer. | Fixes the published language while the owner is fresh; testable isolation | Unused until a consumer arrives | Low | High |
| B | Defer until the first consumer story | Zero speculative code | The first consumer must design the provider's contract under time pressure | Low | Medium |

**F. Recommendation:** Option A, kept minimal. No event, cache or projection.

**G. Status:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Provisional assumption:** US-PROF-001 adds the `api` package with two types and one service implementation.

**I. Change impact:** Option B deletes the two `api` types, their implementation, one test class and one configuration bean.

---

## 3. Items that are not open questions

These items were considered and are **not** open. They are recorded so nobody reopens them by accident.

| Item | Why it is settled |
| --- | --- |
| Presence-aware PATCH and explicit clearing | S3-GAR-01 and S3-PROF-03 are APPROVED; only the encoding detail is OPEN-S3-05 |
| Optimistic revision and guarded create-if-unset | S3-GAR-02 and S3-PROF-04 are APPROVED; only token transport is OPEN-S3-04 |
| Inapplicable values after a Category change | S3-GAR-03 APPROVED: reject with field errors; never auto-clear |
| Recovery after a lost edit response | S3-GAR-04 APPROVED: authenticated reread; never resend with a new token automatically |
| Staged upload, then explicit confirmation; durable pending records; application streaming | S3-IMG-02/03/04/05 APPROVED |
| New-garment photos only | S3-IMG-07 APPROVED: no attach-to-existing, no replace-confirmed |
| Personal Context owns persistence in schema `personal_context` | S3-PROF-01 APPROVED and AR-01 |
| Flyway version numbers | Not a decision: each story uses the next free version on its branch at implementation time ([shared architecture §13](shared-architecture.md#13-flyway-migration-plan)) |
| Measurement | US-MEAS-001 is removed from Sprint 3; no measurement code, tables, events or tests |
