# Sprint 3 — Decision Record of the 16 Detailed Choices (OPEN-S3-01 to OPEN-S3-16)

| Field | Value |
| --- | --- |
| Sprint | Sprint 3 — trustworthy and recognizable private wardrobe, declared personal context |
| Stories | US-GAR-002, US-GAR-003, US-PROF-001 |
| Baseline | Package written against `sprint_03` @ `9d158fb`; this approval revision recorded on `sprint_03` @ `b7644b1` (working tree clean when inspected) |
| Decision source | [Sprint 3 plan](../sprint-plan.md) §9 and §13 (21-entry register) |
| Approval | **2026-11-11: the Product Owner explicitly approved the recommended solution of all 16 entries.** OPEN-S3-11 is approved as described in its entry: Option A is the target and Option B is a temporary fail-closed fallback. |
| Purpose | Records the 16 detailed contract, policy, integration and technical choices that the plan decisions left to JIT design. For each it keeps the original analysis, the approved option, the remaining implementation and verification work, the guide sections that implement it and the change impact. The file keeps its name `open-questions.md` so existing links stay stable; **no approval question remains open in it.** |
| Package | [README](README.md) · [shared architecture](shared-architecture.md) · [US-GAR-002](US-GAR-002-guide.md) · [US-GAR-003](US-GAR-003-guide.md) · [US-PROF-001](US-PROF-001-guide.md) |

## 1. How to read this file

All 21 plan decisions are **APPROVED**:

- S3-GAR-01–05, S3-IMG-01–08 and S3-PROF-01–05 on 2026-11-11;
- S3-INT-01–03 (Option A) later the same day, together with this record.

Each entry below began as a question that an approved decision deliberately left open: a policy value, a contract detail, a concrete technology, or one of the three cross-story INT decisions. On 2026-11-11 the Product Owner approved the recommended solution of every entry.

The original analysis (C–F) is kept unchanged as the decision record. Alternatives stay for reference only; they are not permission to switch away from the approved option.

Every entry has sections A–I:

- **A**: question ID and the plan decision it realizes.
- **B**: related story and requirements.
- **C**: problem.
- **D**: context.
- **E**: options.
- **F**: recommendation (unchanged; now approved).
- **G**: decision status, implementation and verification status, and the status the entry had before approval.
- **H**: the approved direction as the guides implement it, with the guide sections.
- **I**: change impact, only if the Product Owner later revises the decision.

Status values:

| Dimension | Value | Meaning |
| --- | --- | --- |
| Decision status | `APPROVED BY PRODUCT OWNER` | Explicit Product Owner approval on 2026-11-11. It authorizes the implementation direction. All 16 entries have it. |
| Implementation / verification status | `PENDING` | The guides specify the code and tests, but nothing has been implemented, compiled, run or deployed. Where G names technical verification, that must also pass before the choice is relied on. All 16 entries have it. |
| Status before approval | (historical) | The status the entry carried before 2026-11-11, kept for traceability only. |

**Approval is not evidence.** Approval of these choices does **not** mean any of the following:

- the implementation is complete;
- the proposed Java code compiles;
- the automated tests pass;
- the deployment environment supports the selected technologies;
- the HEIC/HEIF decoder has passed real tests;
- S3Mock behaves like Amazon S3;
- the resource limits have been measured;
- production has been verified.

Each guide labels the code that implements an entry with its `OPEN-S3-xx` ID, so a later revision changes a known, small set of files.

## 2. Summary register

| ID | Decision ref | Story | Question | Approved option | Decision status | Implementation / verification status | Status before approval |
| --- | --- | --- | --- | --- | --- | --- | --- |
| [OPEN-S3-01](#open-s3-01--compatible-api-evolution-and-shared-recovery-vocabulary) | S3-INT-01 | All | Compatible API evolution and shared recovery vocabulary | A: evolve existing resources compatibly; one error, revision and recovery convention | APPROVED BY PRODUCT OWNER | PENDING: implementation; contract and CORS tests | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-02](#open-s3-02--integration-environment-and-executable-evidence) | S3-INT-02 | All | Integration environment and executable evidence | A: isolated Testcontainers fixtures with storage and decoder fixtures, real-provider checks, CI for `sprint_03` PRs | APPROVED BY PRODUCT OWNER | PENDING: fixtures, CI runs and the manual real-provider check | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-03](#open-s3-03--jit-design-packaging-and-shared-file-coordination) | S3-INT-03 | All | JIT design packaging and shared-file coordination | A: self-contained story guides, shared architecture, coordinated shared-file changes | APPROVED BY PRODUCT OWNER | PENDING: reconcile guides with the actual implementation during execution | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-04](#open-s3-04--revision-token-transport-and-conflict-status) | S3-GAR-02, S3-PROF-04 | GAR-002, PROF-001 | Revision token transport and conflict status | A: opaque `revision` string in responses; `expectedRevision` in PATCH bodies; `409 REVISION_CONFLICT` | APPROVED BY PRODUCT OWNER | PENDING: implementation and tests | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-05](#open-s3-05--partial-update-media-type-and-presence-encoding) | S3-GAR-01, S3-PROF-03 | GAR-002, PROF-001 | Partial-update media type and presence encoding | A: `application/json` with presence rules; explicit clearing; Price replaced as a whole | APPROVED BY PRODUCT OWNER | PENDING: implementation and tests (including the Jackson absent/null behaviour) | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-06](#open-s3-06--no-op-revisions) | S3-GAR-04, S3-PROF-04 | GAR-002, PROF-001 | Does an edit that changes nothing create a revision? | A: `200`, revision unchanged, nothing written | APPROVED BY PRODUCT OWNER | PENDING: both branches must be tested | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-07](#open-s3-07--pending-media-ttl) | S3-IMG-08 | GAR-003 | Pending-media TTL | B: 24 hours (`PT24H`), configurable | APPROVED BY PRODUCT OWNER | PENDING: lifecycle implementation and tests | BLOCKED — REQUIRES PO DECISION |
| [OPEN-S3-08](#open-s3-08--heicheif-decoding-toolchain) | S3-IMG-06 | GAR-003 | HEIC/HEIF decoding toolchain | A: bounded libheif subprocess (`heif-info` + `heif-dec`, libde265); Java ImageIO for JPEG/PNG | APPROVED BY PRODUCT OWNER | PENDING: verification V1–V8 on the deployment image and CI | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-09](#open-s3-09--meaning-of-15-mb) | S3-IMG-06 | GAR-003 | Byte meaning of "15 MB" | A: exactly 15,000,000 bytes | APPROVED BY PRODUCT OWNER | PENDING: implementation and boundary tests | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-10](#open-s3-10--original-retention-and-the-stored-derivative) | S3-IMG-06, S3-IMG-08 | GAR-003 | Original retention and the stored derivative | A: only a metadata-free, orientation-corrected JPEG, long edge ≤ 2048 px; original not kept | APPROVED BY PRODUCT OWNER | PENDING: implementation and tests; real-device orientation (V3) | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-11](#open-s3-11--multi-image-heif-files) | S3-IMG-06 | GAR-003 | Multi-image HEIF files | A (target): the actual primary image. B: explicit rejection, **only** as the temporary fail-closed fallback until A is verified | APPROVED BY PRODUCT OWNER | PENDING: V4/V8 on the deployment image and CI, then enable selection; until then only the fallback ships | BLOCKED — REQUIRES PO DECISION |
| [OPEN-S3-12](#open-s3-12--decoder-resource-ceilings) | S3-IMG-06 | GAR-003 | Decoder resource ceilings | A: 250 MP declared pixels, 20 s timeout, 2 concurrent decodes, all configurable | APPROVED BY PRODUCT OWNER | PENDING: measurement and load testing on the deployment size | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-13](#open-s3-13--storage-sdk-and-local-s3-compatible-fixture) | S3-IMG-01, S3-INT-02 | GAR-003 | Storage SDK and local S3-compatible fixture | A: AWS SDK for Java v2 `2.46.13`; Adobe S3Mock `5.1.0`; real-provider verification | APPROVED BY PRODUCT OWNER | PENDING: V-S3 against S3Mock and a private AWS S3 bucket | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |
| [OPEN-S3-14](#open-s3-14--photo-api-surface) | S3-IMG-02/03/05, S3-INT-01 | GAR-003 | Photo API surface | A: `/photo-uploads` resource; optional `photoUploadId` on the existing creation request | APPROVED BY PRODUCT OWNER | PENDING: implementation and tests | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-15](#open-s3-15--preference-contract-representation) | S3-PROF-02, S3-PROF-03 | PROF-001 | Preference contract representation | A: `/personal-context/preferences`; six need codes; `null` = unset; priorities 0–3; `revision: null` when never declared | APPROVED BY PRODUCT OWNER | PENDING: implementation and tests | RECOMMENDED — PENDING APPROVAL |
| [OPEN-S3-16](#open-s3-16--published-current-context-contract-timing-and-shape) | S3-PROF-05 | PROF-001 | Published current-context contract timing and shape | A: minimal `personalcontext.api.CurrentPersonalContext` now, no consumer | APPROVED BY PRODUCT OWNER | PENDING: implementation, integration test, architecture tests | TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED |

**Count: 16 entries, all `APPROVED BY PRODUCT OWNER` (2026-11-11).** No entry is pending Product Owner approval and none is blocked on a Product Owner decision.

Implementation is pending for all 16. Technical verification beyond each guide's automated tests is required for:

- **OPEN-S3-02:** CI runs and the real-provider check;
- **OPEN-S3-03:** reconciliation with the implementation;
- **OPEN-S3-06:** both branches tested;
- **OPEN-S3-08:** V1–V8;
- **OPEN-S3-11:** V4/V8 before the target is enabled;
- **OPEN-S3-12:** measurement;
- **OPEN-S3-13:** V-S3;
- **OPEN-S3-16:** integration and architecture tests.

What remains concerns implementation, verification, provider availability, deployment readiness and client evidence ([§4](#4-remaining-work-that-is-not-an-approval-question)), **not** the approval of these 16 choices.

## 3. Coverage and traceability matrix

"P" and "T" steps refer to the story guide named in the row. All steps are proposed future changes; none has been applied.

| Open Question | Approved Option | Affected Documents | Design Section | Production Steps | Test Steps | Remaining Verification |
| --- | --- | --- | --- | --- | --- | --- |
| OPEN-S3-01 | A: compatible evolution; one error, revision and recovery convention | Plan §9.4 (S3-INT-01); shared §11, §12, §20; all three guides | Shared §11–§12; GAR-002 §10.2; GAR-003 §10.2; PROF-001 §10 | GAR-002 P16 (`revision`), P17, P18 (`409`, neutral `503`), P20 (CORS `PATCH`), P21 (OpenAPI 0.9.0). GAR-003 P44 (`photo`), P45 (`photoUploadId`), P46–P48, P54 (CORS `DELETE`), P58 (0.10.0). PROF-001 P26–P28, P31 (0.11.0) | GAR-002 T7, T9; GAR-003 T16, T17, T20, T21; PROF-001 T8; `IdentityOpenApiContractIT` all-handler checks | Implementation; contract and CORS tests green; client review of additive fields |
| OPEN-S3-02 | A: isolated fixtures, real-provider checks, CI for `sprint_03` | Plan §9.4 (S3-INT-02); shared §22; README §12; GAR-002, GAR-003, PROF-001 CI steps | Shared §22; GAR-003 §10.8 | GAR-002 P22 (trigger, reports); GAR-003 P57 (S3Mock in compose), P59 (libheif, `CAPSULEAI_REQUIRE_HEIF=true`, reports); PROF-001 P32 | PostgreSQL Testcontainers ITs in all guides; GAR-003 T14 (S3Mock container), T13 (real libheif) | CI must actually run and pass on PRs into `sprint_03` (75 → 88 → 95 report entries); manual private AWS S3 check (V-S3 steps 3–4) with credentials kept outside the repository |
| OPEN-S3-03 | A: story guides, shared architecture, coordinated shared files | Plan §9.4 (S3-INT-03); this package; README §5; shared §4, §21 | Shared §21 (cumulative order and merge rules) | No code step. It is realized by the package itself: each guide's §11 file matrix, the BEFORE/AFTER anchors and the §21 order | Each guide's §18 checklist and full `clean verify` | Reconcile the guides with the actual implementation as each story lands; afterwards the code, OpenAPI and migrations win |
| OPEN-S3-04 | A: opaque `revision`, body `expectedRevision`, `409 REVISION_CONFLICT` | Shared §8, §11; GAR-002; PROF-001 | Shared §8; GAR-002 I-5, §10.1–§10.3; PROF-001 I-5, §10 | GAR-002 P1 (V9), P2, P11–P16, P18, P21. PROF-001 P1 (V11 `revision`), P9 (`ExpectedRevision`), P14, P16, P19, P23, P25–P28, P31 | GAR-002 T4, T5, T6, T7, T8; PROF-001 T4, T5, T8, T9 | Implementation and tests |
| OPEN-S3-05 | A: `application/json` presence rules, explicit clearing, whole Price | Shared §10; GAR-002; PROF-001 | Shared §10; GAR-002 I-2, §10.2; PROF-001 I-4 | GAR-002 P7 (`FieldChange`), P8 (`GarmentChanges`, `PriceInput`), P15 (`GarmentRevisionRequest`). PROF-001 P8 (`ValueChange`), P10, P25 | GAR-002 T5, T4 (`priceIsReplacedOrCleared…`, `anIncompletePrice…`); PROF-001 T5, T4 | Implementation; T5 tests re-verify the Jackson 3 absent/`null` behaviour on the actual dependency |
| OPEN-S3-06 | A: no-op → `200`, revision unchanged, nothing written | Shared §8; GAR-002; PROF-001 | Shared §8; GAR-002 I-7, §10.1; PROF-001 I-7 | GAR-002 P10 (`changed` flag), P13 (equality after the precondition). PROF-001 P15, P19 | GAR-002 T4 `anEditThatChangesNothing…`, `aStaleRevisionIsAConflict…` (a stale no-op is still `409`), T7 `anEditThatChangesNothingKeepsTheRevision`. PROF-001 T4 `anUnchangedRevision…`, `aStaleRevisionIsAConflict…` | Both branches implemented and passing |
| OPEN-S3-07 | B: `PT24H`, configurable | Plan §9.2 (S3-IMG-08); shared §17, §19; GAR-003; README | GAR-003 I-6, §10.4; shared §17 | GAR-003 P7 (`expiresAt = created + TTL`), P22, P29, P50, P55 (`pending-ttl=PT24H`), P58 (`expiresAt` text), P60 | GAR-003 T8 `aNewIntentIsReceiving…ExpiresAfterTheTtl`, T11 `anUnconfirmedPhotoExpires…`, T19 (injected TTL and clock) | Implementation; lifecycle and deletion evidence |
| OPEN-S3-08 | A: bounded libheif subprocess (`heif-info` + `heif-dec`); ImageIO for JPEG/PNG | Plan §9.2 (S3-IMG-06); shared §17, §19; GAR-003; README | GAR-003 §10.9, §10.5 | GAR-003 P38 (`HeifDecoder`), P39, P50, P51, P55, P59 (CI install), P60 | GAR-003 T13, T22, T12 (missing tools → `503`) | V1–V8 on the deployment image and the CI runner; licence and update review (V7) |
| OPEN-S3-09 | A: 15,000,000 bytes | GAR-003; shared §19 | GAR-003 I-3 | GAR-003 P6, P22, P25 (counted bytes), P48 (multipart guard → `PHOTO_TOO_LARGE`), P55 | GAR-003 T9 `theSizeLimitAcceptsExactly15000000BytesAndRejectsOneMore`, T16 `aPhotoAboveTheSizeLimitIsRejected` | Implementation and tests |
| OPEN-S3-10 | A: metadata-free, orientation-corrected JPEG ≤ 2048 px; no original | GAR-003; shared §17, §19, §20 | GAR-003 I-4 | GAR-003 P7 (`STORED_CONTENT_TYPE`), P18, P37, P39, P55 (`derivative-long-edge=2048`) | GAR-003 T12 (metadata stripping, orientation, `largePhotosAreDownscaled…2048`), T15, T16 | Implementation and tests; real-device orientation (V3) |
| OPEN-S3-11 | A target (primary image); B temporary fail-closed fallback | Plan §9.2 (S3-IMG-06); shared §17, §19; GAR-003; README | GAR-003 §10.9, I-14 | GAR-003 P6 (`MULTIPLE_IMAGES` meaning), P38 (selection), P48, P50, P51, P55 (`heif-info-command`, `heif-primary-image-selection=false`), P58, P60 | GAR-003 T13 (rejection while off; primary listed first; primary **not** the first decoded image), T22 (selection and fail-closed rules) | V4 and V8 on the deployment image and CI; then `heif-primary-image-selection=true`. The target is not Done before that |
| OPEN-S3-12 | A: 250 MP, 20 s, 2 decodes, configurable | GAR-003; shared §17, §19 | GAR-003 §10.9; P39 explanation | GAR-003 P38 (shared deadline, ceiling for every listed image), P39 (header ceiling, watchdog, decode slots), P50, P55 | GAR-003 T12 (`aPhotoDeclaringMorePixels…`, `whenEveryDecodeSlotIsBusy…`), T22 (`everyListedImageMustBeWithin…`), T13 (truncated) | Measure decode time and peak memory with the V3 fixtures on the deployment size; adjust within the approved option |
| OPEN-S3-13 | A: AWS SDK v2 `2.46.13`, S3Mock `5.1.0`, real-provider verification | Plan §9.2 (S3-IMG-01); shared §19; GAR-003; README | GAR-003 §10.5, §10.7 | GAR-003 P1 (BOM, `s3`), P36, P51, P55, P56, P57 | GAR-003 T14 (put/get/delete, missing object, conditional write) | V-S3 steps 1–4; AWS account, region, budget and production configuration |
| OPEN-S3-14 | A: `/photo-uploads` + optional `photoUploadId` | Shared §12, §16, §17; GAR-003 | GAR-003 §10.1, §10.2 | GAR-003 P30–P33, P41–P47, P58 | GAR-003 T10, T16, T17, T18, T20 | Implementation and tests; client evidence |
| OPEN-S3-15 | A: `/personal-context/preferences`, six codes, `null` = unset, 0–3, `revision: null` | Shared §18; PROF-001 | PROF-001 I-1–I-3, I-7, §10 | PROF-001 P1 (V11 CHECKs), P2–P6, P26, P31 | PROF-001 T1, T4, T7, T8 (`aNewAccountHasNoDeclaredContext…`: `revision: null`, all six needs present) | Implementation and tests; clients map the codes to localized labels |
| OPEN-S3-16 | A: minimal `CurrentPersonalContext` now, no consumer | Shared §5, §18; PROF-001 | PROF-001 §10 (published contract) | PROF-001 P20, P21, P22, P29 | PROF-001 T10 `CurrentPersonalContextIT`; `ArchitectureDependencyTest`; T11 `DatabaseSchemaOwnershipIT` | Implementation; architecture tests pass with the new `api` package; no consumer is built |

## 4. Remaining work that is not an approval question

These items stay open after the approvals. None of them reopens an approved choice, and none needs a new Product Owner approval of a choice. The exception is the AWS account and budget, which S3-IMG-01 already assigns to the project owner.

| Item | Kind | Related | Owner | What it gates |
| --- | --- | --- | --- | --- |
| Implement, compile and test all three stories | Implementation | All | Developers | Every DoD row |
| HEIF verification V1–V8 on the deployment image and the CI runner | Verification | OPEN-S3-08, OPEN-S3-11 | Backend Tech Lead | HEIC/HEIF support in US-GAR-003 Done |
| Switch `capsuleai.wardrobe.photo.heif-primary-image-selection` to `true` per environment after V4/V8 | Deployment readiness | OPEN-S3-11 | Backend Tech Lead | The approved multi-image target behaviour |
| Real device HEIC/HEIF (V3) and orientation | Verification, client evidence | OPEN-S3-08, OPEN-S3-10 | Developers / QA | US-GAR-003 Done |
| Decode time and peak memory with the V3 fixtures | Verification | OPEN-S3-12 | Backend Tech Lead; PO only if ordinary phone photos would be affected | Relying on the ceilings in production |
| V-S3 against S3Mock (including the conditional write) | Verification | OPEN-S3-13 | Developers | `conditional-writes` setting for the fixture |
| AWS account, region, budget, private bucket, least-privilege IAM, then V-S3 against real S3 | Provider availability, deployment readiness | S3-IMG-01, OPEN-S3-02, OPEN-S3-13 | Project owner / PO with Backend Tech Lead | Deployed private media |
| CI runs on PRs into `sprint_03` with all required reports and zero skips | Verification | OPEN-S3-02 | Developers | Every story's DoD |
| Reconcile the guides with the delivered code after each story | Documentation | OPEN-S3-03 | Developers | Review condition in the DoD |
| Client evidence on Android/iOS | Client evidence | All stories | Developers / QA with PO | Whole-story Done |

**No genuinely new open question** was found while synchronizing the documentation on 2026-11-11.

Implementing the approved OPEN-S3-11 Option A added two items:

- a dependency on `heif-info`, shipped in the same libheif package;
- the verification step V8.

Both are implementation details of an approved choice, not new decisions.

## OPEN-S3-01 — Compatible API evolution and shared recovery vocabulary

**A. Question ID:** OPEN-S3-01. It realizes S3-INT-01, approved as Option A by the Product Owner on 2026-11-11 together with this entry; the status before approval was `PROPOSED — PENDING PO APPROVAL`.

**B. Related story and requirements:** All three stories. COM-001/002, FR-AUTH-008/009, NFR-SEC-001–003, ADA-006/008/009/015/017/018; S3-GAR-01/02/04, S3-PROF-03/04, S3-IMG-03/05.

**C. Problem:** New edit, media and preference operations must not break existing creation, paging, detail or security checks, and every new outcome (rejected, stale, known failure, pending media, uncertain) must be distinguishable.

**D. Context:** The current API is flat JSON with RFC 9457 Problem Details and a stable `code`. `IdentityOpenApiContractIT` checks that **every** `/api` handler is documented, that request and response record components equal the documented properties, and that documented required fields are enforced. CORS allows only `GET, POST` and the headers `Authorization, Content-Type, Accept, Idempotency-Key`. `CorsSecurityIT` asserts that a `PATCH` preflight is refused.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Evolve existing resources compatibly. Add only narrowly scoped operations (PATCH a garment, a photo-upload resource, a preferences resource). Add response fields additively. Use one convention: `VALIDATION_FAILED` (400), `REVISION_CONFLICT` (409), `TEMPORARILY_UNAVAILABLE` (503, outcome not confirmed). Extend the contract test for multipart and binary responses. Allow `PATCH` and `DELETE` in CORS. | Preserves current clients. One recovery language. The contract test keeps its all-handler strength. | Additive fields still need client review. The contract test needs careful extension. | Medium | High: matches the current small API and its strict contract test |
| B | Introduce a new versioned representation (`/api/v2/...`) wherever new behaviour appears | Old behaviour stays untouched | Duplicate routes, models and security tests; no incompatible consumer exists to justify it | High | Low |

**F. Recommendation:** Option A.

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. The contract test, the CORS tests and each story's API tests must pass on the implemented code; clients review the additive fields.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- `revision` is added to the garment response (US-GAR-002) and `photo` to the garment response (US-GAR-003).
- `photoUploadId` becomes an optional field of the existing creation request.
- All three guides use the shared error codes in [shared architecture §11](shared-architecture.md#11-error-model-and-recovery-vocabulary).
- CORS gains `PATCH` (US-GAR-002) and `DELETE` (US-GAR-003).
- The 503 wording becomes operation-neutral.
- Guide sections:
  - US-GAR-002 §10.2, P16–P18, P20, P21, T7, T9;
  - US-GAR-003 §10.2, P44–P48, P54, P58, T16, T17, T20, T21;
  - US-PROF-001 §10, P26–P28, P31, T8.

**I. Change impact (only if the decision is later revised):** If Option B is chosen, each guide's web adapter, OpenAPI step and contract-test step move to a `/api/v2` controller. The domain, application and persistence steps are unaffected.

---

## OPEN-S3-02 — Integration environment and executable evidence

**A. Question ID:** OPEN-S3-02. It realizes S3-INT-02, approved as Option A by the Product Owner on 2026-11-11 together with this entry; the status before approval was `PROPOSED — PENDING PO APPROVAL`.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. The fixtures and CI changes are specified but not applied. CI must actually run on PRs into `sprint_03` and pass with zero skips. The manual real-provider check against a private AWS S3 bucket is outstanding.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- **US-GAR-002** step `backend-ci.yml` (P22) adds `sprint_03` to `pull_request.branches` and adds its reports.
- **US-GAR-003** adds:
  - an S3Mock container to `compose.yaml` (P57) and to its storage integration tests (T14);
  - a CI step that installs libheif (P59);
  - `CAPSULEAI_REQUIRE_HEIF=true` in CI, so a missing decoder fails rather than skips.
- **US-PROF-001** adds its reports (P32).
- Real AWS evidence remains an explicit, outstanding manual activity. It is never claimed from emulator runs.

**I. Change impact (only if the decision is later revised):** Option B replaces the per-test containers with configuration that points at the shared stack. Test classes stay; their support classes change.

---

## OPEN-S3-03 — JIT design packaging and shared-file coordination

**A. Question ID:** OPEN-S3-03. It realizes S3-INT-03, approved as Option A by the Product Owner on 2026-11-11 together with this entry as the documentation and JIT coordination approach; the status before approval was `DEFERRED TO JIT DESIGN`.

**B. Related story and requirements:** All stories. Workflow Sections 26/27/31–34; DD-006; DoD documentation and review conditions.

**C. Problem:** Concrete design must live somewhere without competing with the living OpenAPI and migrations, and three stories touch the same files.

**D. Context:** The Product Owner asked for this guide package. The living artifacts are `docs/05-api/openapi.yaml`, Flyway migrations, the CI workflow and the code itself.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Self-contained story guides plus a shared-convention file. The living contract and migrations stay authoritative once implemented. A shared-file ownership matrix defines the cumulative order. | Readable handoff. Clear producer/consumer ownership. | Guides drift from code after implementation unless corrected. | Medium | High |
| B | Design notes only in PRs and contracts | Least documentation | Lifecycle and concurrency decisions are hard to find | Low | Medium |

**F. Recommendation:** Option A, as realized by this package. After a story is implemented, the code, OpenAPI and migrations win over any guide text.

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. During execution, each story must:
  - recheck HEAD;
  - apply its guide in the §21 order (or use the merge rules);
  - reconcile the guide with the code it actually delivers.
- **Status before approval:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Approved direction, as implemented by the guides:** The guides are cumulative in the order GAR-002 → GAR-003 → PROF-001 ([shared architecture §21](shared-architecture.md#21-shared-file-ownership-and-cumulative-order)). A guide that is applied out of order uses the merge rules in that section. There is no code step. The approach is realized by:

- the package itself;
- each guide's §11 file matrix and BEFORE/AFTER anchors;
- the README workflow (§10).

**I. Change impact (only if the decision is later revised):** If Option B is chosen, the guides become reference material only. No code changes.

---

## OPEN-S3-04 — Revision token transport and conflict status

**A. Question ID:** OPEN-S3-04. It refines S3-GAR-02 and S3-PROF-04 (both APPROVED) and uses S3-INT-01 (APPROVED).

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. Implementation and the listed tests.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- US-GAR-002 and US-PROF-001 implement Option A.
- A malformed `expectedRevision` is `400 VALIDATION_FAILED` with field code `INVALID_FORMAT`.
- A missing one is `REQUIRED`.
- For preferences, `expectedRevision: null` means "I saw no profile" (the guarded first creation).
- Guide sections:
  - US-GAR-002 I-5, §10.1–§10.3, P1, P2, P11–P16, P18, P21, T4–T8;
  - US-PROF-001 I-5, P1, P9, P14, P16, P19, P23, P25–P28, P31, T4, T5, T8, T9.

**I. Change impact (only if the decision is later revised):** Option B changes the PATCH controllers, response construction (headers), CORS configuration and tests, the OpenAPI operations and the contract test. Domain, application and persistence are unchanged, because they already use a `long` revision.

---

## OPEN-S3-05 — Partial-update media type and presence encoding

**A. Question ID:** OPEN-S3-05. It refines S3-GAR-01 and S3-PROF-03, both APPROVED.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. Implementation and tests. The mapping tests (US-GAR-002 T5, US-PROF-001 T5) re-verify the Jackson absent/`null` behaviour on the actual dependency.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- The request records use `tools.jackson.databind.JsonNode` components.
- A JSON value of the wrong type (for example a number for `subtype`, or a string for `materials`) is rejected for that field with `UNSUPPORTED_VALUE`.
- PATCH is therefore stricter than the existing POST, which lets Jackson coerce scalars.
- A `price` object always replaces both parts. A missing part becomes `price: INCOMPLETE_PRICE` during validation; it is never merged with the old value.
- `commonNeeds: null` is rejected rather than clearing every need. A need must be cleared individually.
- Guide sections:
  - US-GAR-002 I-2, §10.2, P7, P8, P15, T4, T5;
  - US-PROF-001 I-4, P8, P10, P25, T4, T5.

**I. Change impact (only if the decision is later revised):** Option B or C replaces the request records and the mapping classes in the web adapters (`GarmentRevisionRequest`, `PreferencesRevisionRequest`). The application `FieldChange` model is unchanged.

---

## OPEN-S3-06 — No-op revisions

**A. Question ID:** OPEN-S3-06. It refines S3-GAR-04 and S3-PROF-04.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. Both branches must be implemented and proven by tests: a valid no-op writes nothing and keeps the revision, and a stale no-op is still `409`.
- **Status before approval:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Approved direction, as implemented by the guides:**

- Both PATCH services check the precondition first, then compare the result, and return a `changed` flag.
- For preferences, a create-if-unset that declares nothing writes nothing and leaves `revision: null`.
- Guide sections:
  - US-GAR-002 I-7, §10.1, P10, P13; T4 `anEditThatChangesNothingKeepsTheRevisionAndWritesNothing` and `aStaleRevisionIsAConflictAndChangesNothing` (which also asserts a stale no-op); T7 `anEditThatChangesNothingKeepsTheRevision`;
  - US-PROF-001 I-7, P15, P19; T4 `anUnchangedRevisionAndAnEmptyFirstDeclarationWriteNothing` and `aStaleRevisionIsAConflictAndChangesNothing` (also a stale no-op).

**I. Change impact (only if the decision is later revised):** Option B removes the equality short-circuit in `ReviseGarmentService` and `RevisePreferencesService` and adjusts two tests per story.

---

## OPEN-S3-07 — Pending-media TTL

**A. Question ID:** OPEN-S3-07. S3-IMG-08 was APPROVED for the lifecycle while leaving the exact TTL unselected. This entry selects it: approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option B: a configurable pending-media TTL of 24 hours (`PT24H`). This resolves the TTL that the plan said blocked lifecycle completion.
- **Implementation / verification status:** PENDING. The lifecycle implementation and its tests; operational deletion evidence.
- **Status before approval:** BLOCKED — REQUIRES PO DECISION.

**H. Approved direction, as implemented by the guides:**

- `PT24H` is the default value of `capsuleai.wardrobe.photo-upload.pending-ttl` (US-GAR-003 P55).
- Each staged photo records `expires_at = created_at + TTL` (P7).
- Tests inject the TTL and use a controllable clock, so no test depends on the value.
- The README and OpenAPI describe the 24-hour lifetime as the approved policy.
- Guide sections: US-GAR-003 I-6, §10.4, P7, P22, P29, P50, P55, P58, P60, T8, T11, T19; shared architecture §17, §19.

**I. Change impact (only if the decision is later revised):** Changing the value changes only `application.properties`, the README text and the `expiresAt` description in OpenAPI. No code, migration or test changes.

---

## OPEN-S3-08 — HEIC/HEIF decoding toolchain

**A. Question ID:** OPEN-S3-08. S3-IMG-06 was APPROVED as the investigation direction only: a bounded local decoding process behind the Wardrobe media port. This entry selects the concrete toolchain: approved by the Product Owner on 2026-11-11. The toolchain remains unverified.

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
- **Verification procedure** (before relying on it; record the results in the PR). V1, V4 and V6 were extended and V8 added on 2026-11-11, so the same procedure also verifies the approved OPEN-S3-11 primary-image behaviour. The toolchain choice itself is unchanged.

| # | Verification step | Pass condition |
| --- | --- | --- |
| V1 | `heif-dec --version` (or `heif-convert --version`) and `heif-info --version` on the target OS image and the CI runner | Both print a libheif version ≥ 1.17 from the same installation |
| V2 | `heif-dec --list-decoders` (if supported by that version) | Lists an HEVC decoder (libde265) |
| V3 | Decode a real iPhone HEIC (synthetic subject, no personal content) and a real Android HEIF: `heif-dec in.heic out.png` | Exit code 0, one `out.png`, dimensions match the device's report, orientation correct |
| V4 | Encode a 2-image HEIF with `heif-enc a.png b.png -o two.heic` (a and b of different sizes); run `heif-info two.heic` and `heif-dec two.heic out.png` | `heif-info` lists two `image: WxH (id=N)` lines in input order, the first marked `, primary`. `heif-dec` writes `out-1.png` with a's size and `out-2.png` with b's size. This confirms heif-info's order equals heif-dec's numbering (OPEN-S3-11) |
| V5 | Decode a truncated HEIC (first 60% of bytes) and a JPEG renamed `.heic` | Non-zero exit or no output, within the timeout |
| V6 | Run `HeifDecodingIT` (5 tests) locally and in CI with `CAPSULEAI_REQUIRE_HEIF=true`, and the unit tests `HeifDecoderTest` and `ImageIoPhotoInspectorTest` | All pass, none skipped |
| V7 | Licence and update review: libheif (LGPL-3.0) and libde265 (LGPL-3.0) as separately installed OS packages invoked as processes; record the security-update channel | Written note in the PR; no static linking into the JAR |
| V8 | Primary not first: rewrite the `pitm` item of `two.heic` to the second image's id, as `HeifDecodingIT.withSelectionEnabledThePrimaryImageIsUsedWhenItIsNotTheFirstDecodedImage` does, then run `heif-info` and `heif-dec` again | `heif-info` marks the **second** line `, primary` while the order is unchanged; `heif-dec` still writes `out-1.png` (a) and `out-2.png` (b); the test passes with selection enabled. Only after V4 and V8 pass in an environment may `capsuleai.wardrobe.photo.heif-primary-image-selection=true` be set there |

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A. The approval selects the toolchain direction.
- **Implementation / verification status:** PENDING. None of V1–V8 has been executed against this repository, the deployment image or the CI runner. Until they pass, the toolchain is approved but **unverified**.
- **Status before approval:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Approved direction, as implemented by the guides:**

- US-GAR-003 implements `ImageIoPhotoInspector` (P39) with a `HeifDecoder` (P38) that runs `heif-info` and then `heif-dec`.
  - Both run in a private temporary directory, with relative file names and one shared decode deadline.
  - `heif-info` is configurable as `capsuleai.wardrobe.photo.heif-info-command` (default `heif-info`, environment `CAPSULE_HEIF_INFO`) and comes from the same libheif package.
- Integration tests that need HEIF skip only when `CAPSULEAI_REQUIRE_HEIF` is not `true`. CI sets it, so a missing decoder fails the build.
- A missing tool is `503 TEMPORARILY_UNAVAILABLE`, never "unsupported format".
- Guide sections: US-GAR-003 §10.5, §10.9, P38, P39, P50, P51, P55, P59, P60, T12, T13, T22.

**I. Change impact (only if the decision is later revised):** Option B replaces `HeifDecoder` and parts of `ImageIoPhotoInspector`. The application port `PhotoInspector` and everything above it are unchanged.

---

## OPEN-S3-09 — Meaning of "15 MB"

**A. Question ID:** OPEN-S3-09. S3-IMG-06 lists "MB byte interpretation" as needing review; this entry was approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A: exactly 15,000,000 bytes.
- **Implementation / verification status:** PENDING. Implementation and the boundary tests.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- The application enforces the exact limit on counted bytes.
- The servlet multipart ceiling is set higher (`16MB`, i.e. 16 MiB) only as a transport guard.
- Anything that exceeds the transport guard is also reported as `PHOTO_TOO_LARGE`.
- Guide sections: US-GAR-003 I-3, P6, P22, P25, P48, P55; T9 `theSizeLimitAcceptsExactly15000000BytesAndRejectsOneMore`; T16 `aPhotoAboveTheSizeLimitIsRejected`.

**I. Change impact (only if the decision is later revised):** Option B changes one property value, the multipart ceiling, two boundary tests and README text.

---

## OPEN-S3-10 — Original retention and the stored derivative

**A. Question ID:** OPEN-S3-10. S3-IMG-06 and S3-IMG-08 list "original retention/normalization" as needing policy review; this entry was approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A. The Product Owner owns this retention decision.
- **Implementation / verification status:** PENDING. Implementation and tests; real-device orientation is checked in V3.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- US-GAR-003 stores exactly one object per accepted photo: `image/jpeg`, metadata-free, long edge ≤ `capsuleai.wardrobe.photo.derivative-long-edge` (default `2048`).
- Validation uses the **original** dimensions (shortest side ≥ 512) read from the image header before any downscaling.
- The response reports the derivative's dimensions.
- Guide sections: US-GAR-003 I-4, P7, P18, P37, P39, P55; T12 (metadata stripping, orientation, `largePhotosAreDownscaledSoTheLongEdgeIsAtMost2048`); T15; T16.

**I. Change impact (only if the decision is later revised):** Option B adds a second object key column, a second storage write and its cleanup. Option C changes one property and requires a PO-approved OPEN-S3-12 pixel limit.

---

## OPEN-S3-11 — Multi-image HEIF files

**A. Question ID:** OPEN-S3-11. S3-IMG-06 lists "multi-image HEIF selection" as needing contract/policy review. On 2026-11-11 the Product Owner approved:

- **Option A as the target product behaviour;**
- **Option B only as an explicitly documented temporary fail-closed fallback** until primary-image support is verified.

**B. Related story and requirements:** US-GAR-003 AC-02; FR-AI-012.

**C. Problem:** A HEIF container may hold several top-level images. Which one represents the garment?

**D. Context:**

- A normal iPhone or Android photo has one top-level (primary) image. Thumbnails, depth and gain maps are auxiliary and not top-level.
- `heif-dec` decodes every top-level image and has no primary-only option (OPEN-S3-08).
- Silently picking an arbitrary image is not acceptable.

**Additional context, recorded on 2026-11-11 for the approved Option A** (upstream libheif sources `libheif/context.cc`, `examples/heif_info.cc`, `examples/heif_dec.cc` and `examples/heif_enc.cc`, read on that date):

- libheif builds its top-level image list in **file item order** and does not move the primary image to the front. The primary image is the one the `pitm` box names.
- `heif-info` walks that list and prints `image: <w>x<h> (id=<item id>)`, followed by `, primary` for the primary image.
- `heif-dec` walks the same list in the same order and numbers several outputs from 1. It exits at the first image it cannot decode, so the numbering cannot shift.
- `heif-enc` always makes its first input the primary image and has no option to choose another.
- These are facts about upstream code, not about an installed version. V4 and V8 (OPEN-S3-08) check them per environment.

**E. Options:**

| Option | Solution | Advantages | Disadvantages / Trade-offs | Complexity | Suitability |
| --- | --- | --- | --- | --- | --- |
| A | Use the file's **primary** image (what the gallery shows) | Matches user expectation | Needs primary identification (a small libheif helper or parsing `heif-info` output); more native code | Medium | High as the final policy |
| B | Reject multi-top-level files explicitly with `PHOTO_MULTIPLE_IMAGES` and offer replacement or manual entry | Simple; never shows the wrong image | Rejects a technically supported file | Low | Medium as an interim measure |
| C | Use the first decoded image | Simple | May not be the primary; silent choice | Low | Low |

**F. Recommendation:** Option A as the product policy. Option B as the explicit interim behaviour until A is approved and verified (V4 in OPEN-S3-08).

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11).
  - **Target behaviour:** Option A.
  - **Temporary fail-closed fallback:** Option B, until primary-image selection is verified.
  - Option C is not approved.
- **Implementation / verification status:** PENDING.
  - Primary-image selection is designed, specified in code and covered by tests (US-GAR-003 §10.9, P38, T13, T22).
  - It stays **disabled** (`capsuleai.wardrobe.photo.heif-primary-image-selection=false`) until V4 and V8 pass on the deployment image and the CI runner.
  - Until then the shipped behaviour is the temporary fallback, and the target capability is **not Done**.
- **Status before approval:** BLOCKED — REQUIRES PO DECISION.

**H. Approved direction, as implemented by the guides:**

- **Identification.** `HeifDecoder` runs `heif-info`, parses the `image:` lines in order, and requires exactly one `, primary` line. The primary's 0-based position `k` names `heif-dec`'s output `decoded-(k+1).png`. The output set must be exactly `decoded-1.png` … `decoded-N.png`.
- **Ceiling first.** Every listed image must be within the declared-pixel ceiling before `heif-dec` runs.
- **Fail-closed.** Any of the following gives `422 PHOTO_MULTIPLE_IMAGES` with replacement and manual-continuation guidance, never a silent choice:
  - selection is disabled;
  - there is no primary line;
  - there are several primary lines;
  - the outputs do not match the listing.
- **Temporary fallback label.** The enum Javadoc (P6), the OpenAPI text (P58), the README (P60) and the configuration comment (P55) describe the rejection as temporary.
- **Tests.**
  - T13 `withSelectionEnabledThePrimaryImageIsUsedWhenItIsNotTheFirstDecodedImage` rewrites the `pitm` item so that the primary is the second decoded image, and expects that image. Option C would fail this test.
  - T13 also covers the primary listed first and the rejection while selection is off.
  - T22 covers the selection and fail-closed rules without libheif.
- **Guide sections:** US-GAR-003 I-14, §10.9, P6, P38, P48, P50, P51, P55, P58, P60, T13, T22; shared architecture §17, §19.

**I. Change impact (only if the decision is later revised):** If the PO later made Option B the permanent policy:

- remove the selection branch and the `heif-primary-image-selection` property (or keep it `false`);
- remove the two selection tests in `HeifDecodingIT` and the selection cases in `HeifDecoderTest`.

The API and database are unaffected.

---

## OPEN-S3-12 — Decoder resource ceilings

**A. Question ID:** OPEN-S3-12. S3-IMG-06 says: "Bound decode memory, pixel allocation, processing time and temporary resources … do not invent numerical limits or silently reject otherwise supported input". This entry was approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. Measure decode time and peak memory with the V3 fixtures on the deployment size, and load-test concurrent uploads. Adjust the values within Option A if needed. Raise any change that could affect ordinary phone photos with the PO.
- **Status before approval:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Approved direction, as implemented by the guides:**

| Property | Default |
| --- | --- |
| `capsuleai.wardrobe.photo.maximum-declared-pixels` | `250000000` |
| `capsuleai.wardrobe.photo.decode-timeout` | `PT20S` |
| `capsuleai.wardrobe.photo.maximum-concurrent-decodes` | `2` |

- When all decode slots are busy, the request waits up to the decode timeout and then answers `503 TEMPORARILY_UNAVAILABLE` (retryable). It never answers `PHOTO_UNREADABLE` for that case. US-GAR-003 T12 `whenEveryDecodeSlotIsBusyAPhotoIsTemporarilyUnavailableNotUnreadable` tests this.
- For HEIF, `heif-info` and `heif-dec` share one decode deadline, and every listed image's declared pixels are checked before `heif-dec` runs (P38, T22).
- Guide sections: US-GAR-003 §10.9, P38, P39, P50, P55, T12, T13, T22.

**I. Change impact (only if the decision is later revised):** Only property values and README text change.

---

## OPEN-S3-13 — Storage SDK and local S3-compatible fixture

**A. Question ID:** OPEN-S3-13. S3-IMG-01 was APPROVED: an S3-compatible local service for development and tests, plus private managed Amazon S3 as the proposed deployed target. It says "select an actively supported local fixture and SDK during JIT work". This entry selects them: approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A, with the documented versions and real-provider verification.
- **Implementation / verification status:** PENDING.
  - V-S3 steps 1–4 have not been executed.
  - S3Mock's `If-None-Match` support is unverified, and an S3Mock pass is not evidence about Amazon S3.
  - AWS account, region, budget and production configuration remain outside this package (S3-IMG-01).
- **Status before approval:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Approved direction, as implemented by the guides:** US-GAR-003 implements Option A:

- the POM (P1);
- the storage adapter (P36);
- the client wiring (P51);
- the properties (P55, P56);
- compose (P57);
- the storage test (T14).

The guide's README text (P60) lists the deployment settings without claiming them.

**I. Change impact (only if the decision is later revised):** Option B or C changes `compose.yaml`, `application-local.properties` and the container definition in `S3PhotoStorageIT`. Production code is unaffected, because only the endpoint changes. (Corrected on 2026-11-11: an earlier text named a `PhotoStorageTestConfiguration`, which no guide defines.)

---

## OPEN-S3-14 — Photo API surface

**A. Question ID:** OPEN-S3-14. It realizes S3-IMG-02 (multipart proxy), S3-IMG-03 (staged upload then explicit confirmation), S3-IMG-05 (application-authorized streaming) and S3-IMG-07 (new garment only), all APPROVED. The contract shape uses S3-INT-01 (APPROVED). This entry was approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. Implementation and tests; client evidence for the two-step flow.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- US-GAR-003 implements Option A.
- A `photoUploadId` that cannot be used never produces an imageless success. It gives `404 PHOTO_UPLOAD_NOT_FOUND` or `409 PHOTO_UPLOAD_NOT_USABLE`. That covers an upload that is:
  - unknown or another owner's;
  - cancelled or expired;
  - not yet ready;
  - already attached to another garment.
- Guide sections: US-GAR-003 §10.1, §10.2, P30–P33, P41–P47, P58, T10, T16, T17, T18, T20.

**I. Change impact (only if the decision is later revised):** Option B moves confirmation into `WardrobePhotoUploadController` and a new service. Option C adds one field to `GarmentSummaryResponse`, OpenAPI and the US-WAR-001 tests.

---

## OPEN-S3-15 — Preference contract representation

**A. Question ID:** OPEN-S3-15. It refines S3-PROF-02 and S3-PROF-03, both APPROVED; this entry was approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A, including the six canonical need codes, explicit unset (`null`) versus priority `0`–`3`, and `revision: null` for a never-declared profile.
- **Implementation / verification status:** PENDING. Implementation and tests; clients map the approved codes to localized labels.
- **Status before approval:** RECOMMENDED — PENDING APPROVAL.

**H. Approved direction, as implemented by the guides:**

- **US-PROF-001** implements Option A, with all seven profile style codes and the six need codes.
- **Unset styles and an empty style selection are not distinguished.** Both mean "no declared style context". S3-PROF-02 requires an empty selection to round-trip as `[]`, and it does.
- **Contract change:** if the PO later wants "never set" to differ from "deliberately none", that is a contract change, described in I.
- **Guide sections:** US-PROF-001 I-1–I-3, I-7, §10, P1–P6, P26, P31; T1, T4, T7, T8.

**I. Change impact (only if the decision is later revised):**

- **Need codes:** renaming them changes the `CommonNeed` enum, the V11 CHECK constraint (before first deployment only) and OpenAPI.
- **Unset vs. empty styles:** distinguishing them adds a `styles_declared` column and a nullable `styles` response.

---

## OPEN-S3-16 — Published current-context contract timing and shape

**A. Question ID:** OPEN-S3-16. S3-PROF-05 was APPROVED: "a narrow `personalcontext.api` current-context contract when needed by the selected implementation". This entry selects "now, minimal": approved by the Product Owner on 2026-11-11.

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

**G. Status:**

- **Decision status:** APPROVED BY PRODUCT OWNER (2026-11-11), Option A.
- **Implementation / verification status:** PENDING. Implement the two `api` types and the service, then prove them:
  - `CurrentPersonalContextIT` must pass;
  - `ArchitectureDependencyTest` must pass with the new `api` package (no `domain` or `application` type in it, and no other module reaching past it);
  - `DatabaseSchemaOwnershipIT` must pass with the new tables.
  - No consumer is built.
- **Status before approval:** TECHNICAL DEFAULT — JIT VERIFICATION REQUIRED.

**H. Approved direction, as implemented by the guides:** US-PROF-001 adds the `api` package with two types and one service implementation. Guide sections: US-PROF-001 §10, P20, P21, P22, P29, T10, T11; shared architecture §5, §18.

**I. Change impact (only if the decision is later revised):** Option B deletes the two `api` types, their implementation, one test class and one configuration bean.

---

## 5. Items that are not open questions

These items were considered and are **not** open. They are recorded so nobody reopens them by accident.

| Item | Why it is settled |
| --- | --- |
| Presence-aware PATCH and explicit clearing | S3-GAR-01 and S3-PROF-03 are APPROVED; their encoding, OPEN-S3-05, is approved too |
| Optimistic revision and guarded create-if-unset | S3-GAR-02 and S3-PROF-04 are APPROVED; token transport, OPEN-S3-04, is approved too |
| Inapplicable values after a Category change | S3-GAR-03 APPROVED: reject with field errors; never auto-clear |
| Recovery after a lost edit response | S3-GAR-04 APPROVED: authenticated reread; never resend with a new token automatically |
| Staged upload, then explicit confirmation; durable pending records; application streaming | S3-IMG-02/03/04/05 APPROVED |
| New-garment photos only | S3-IMG-07 APPROVED: no attach-to-existing, no replace-confirmed |
| Personal Context owns persistence in schema `personal_context` | S3-PROF-01 APPROVED and AR-01 |
| Flyway version numbers | Not a decision: each story uses the next free version on its branch at implementation time ([shared architecture §13](shared-architecture.md#13-flyway-migration-plan)) |
| Measurement | US-MEAS-001 is removed from Sprint 3; no measurement code, tables, events or tests |
