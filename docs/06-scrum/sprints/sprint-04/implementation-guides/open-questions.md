# Sprint 4 — Decision Record of the Delegated Detailed Choices (OPEN-S4-01 to OPEN-S4-33)

| Field | Value |
| --- | --- |
| Sprint | Sprint 4 ([plan](../sprint-plan.md)) |
| Repository baseline | `sprint_04` at `33c579c` (merge of PR #5, the Sprint 3 branch) |
| Plan decisions | All 30 Sprint 4 recommendations (S4-GAR-01–06, S4-WAR-01–04, S4-PROF-01–04, S4-CTX-01–06, S4-BULK-01–07, S4-INT-01–03) **approved by the Product Owner on 2026-11-16** |
| This file | The 33 detailed choices that the approved decisions left open, selected by Claude under the Product Owner's explicit delegation of 2026-11-16 |
| Verification status | **Only US-GAR-005 (OPEN-S4-01–06 and its parts of OPEN-S4-31–33) is implemented in the repository, since 2026-11-21; nothing is deployed.** The other guides contain proposed code and tests. They were compiled and their tests passed only in an isolated scratch copy, most recently on 2026-11-17 ([README §11](README.md#11-status)); every decision therefore stays `VERIFICATION PENDING`. |
| Revision of 2026-11-17 | OPEN-S4-11 refined for Vietnamese descriptor search, explicitly authorized by the Product Owner; OPEN-S4-14 matching policy `m2`; OPEN-S4-09 mapped to the formal US-GAR-006 story; canonical-document references added. No other decision changed. |

## 1. Decision authority

On **2026-11-16** the Product Owner did two things:

1. **Approved all 30 recommended solutions** in [Section 9 of the Sprint 4 plan](../sprint-plan.md#9-decision-analysis--proposed-solutions). Each S4 decision is now binding. Since version 0.2 of 2026-11-17 the plan itself records the approval; its earlier `PROPOSED — PENDING PO APPROVAL` labels are kept as history.
2. **Delegated the remaining detailed questions** to Claude: "independently select practical, appropriate solutions for unresolved detailed questions". This includes the questions the plan names as blockers:
   - CAT-Q01: search semantics;
   - the supported optional body/gender representation;
   - location precision and manual resolution;
   - weather provider mapping and deadline mechanics;
   - MVP-Q08: batch limits, retention, resumption and atomicity;
   - the provisional US-GAR-006 acceptance expectations.

The two authorities are kept apart everywhere in this package:

| Label | Meaning | Applies to |
| --- | --- | --- |
| `APPROVED BY PRODUCT OWNER` | Explicit Product Owner approval on 2026-11-16 | The 30 S4 decisions only |
| `SELECTED BY CLAUDE — DELEGATED DEFAULT` | A concrete choice made by Claude under the delegation. It is binding for implementation until the Product Owner changes it, but it is **not** a Product Owner approval of the specific value. | All 33 OPEN-S4 entries |
| `IMPLEMENTATION PENDING` | The guide specifies the code; it has not been applied to the repository. | All 33 entries |
| `VERIFICATION PENDING` | Tests, measurements, provider terms, storage configuration or client evidence must still establish the claim. | Every entry that names an external, operational or client check |

On **2026-11-17** the Product Owner also **explicitly authorized Vietnamese descriptor search** for US-WAR-003, a refinement of OPEN-S4-11. That authorization covers the capability; the specific alias wording stays a delegated default.

**No delegated default is claimed as Product Owner approved.** Where an entry changes a product-visible meaning (for example the optional body/gender vocabulary or the batch limits), it is a candidate for explicit Product Owner review. The change-impact field says what a later change would touch.

Each entry has these fields:

- **A. Question and decision:** the question ID and the S4 decision it realizes.
- **B. Story and requirements:** the related story and requirement IDs.
- **C. Problem and context.**
- **D. Options:** the options considered, with their trade-offs.
- **E. Selected solution and rationale.**
- **F. Authority and status.**
- **G. Implementation steps:** the exact P/T steps in the guides.
- **H. Affected files.**
- **I. Required verification.**
- **J. Change impact.**

## 2. Summary register

| ID | Story | Realizes | Selected solution (short) | Authority | Implementation | Verification |
| --- | --- | --- | --- | --- | --- | --- |
| [OPEN-S4-01](#open-s4-01--removal-command-transport-and-outcomes) | GAR-005 | S4-GAR-01, S4-INT-02/03 | `POST …/{garmentId}/removal` with `confirmed` and `expectedRevision`. The outcome is `REMOVED` or `ALREADY_REMOVED`; a repeated removal is monotonic. | Delegated default | Implemented 2026-11-21 (US-GAR-005); tests pass locally | Pending (CI run; client evidence) |
| [OPEN-S4-02](#open-s4-02--retained-minimal-removed-representation) | GAR-005 | S4-GAR-02 | Keep id, owner, creation key and fingerprint, Category, Color, provenance, the times and the revision. Purge the rest. | Delegated default | Implemented 2026-11-21 (US-GAR-005); tests pass locally | Pending (privacy review; CI run) |
| [OPEN-S4-03](#open-s4-03--creation-replay-after-removal) | GAR-005 | S4-GAR-02, S4-INT-02 | Replaying a removed garment's creation checks the fingerprint (`422` on mismatch), then answers `410`; it never recreates the garment. | Delegated default | Implemented 2026-11-21 (US-GAR-005); tests pass locally | Pending (CI run; client evidence) |
| [OPEN-S4-04](#open-s4-04--durable-removal-cleanup-work-and-schedule) | GAR-005 | S4-GAR-03 | Cleanup table with lease and retry. Minimize at the first claim; delete the object outside the transaction. `P30D` limit, `P21D` warning. | Delegated default | Implemented 2026-11-21 (US-GAR-005); tests pass locally | Pending (CI run; client evidence) |
| [OPEN-S4-05](#open-s4-05--storage-versioning-and-physical-deletion-boundary) | GAR-005 | S4-GAR-03 | Deletion boundary: an unversioned bucket with no object lock and no replication or backup copies, proven operationally. | Delegated default | No code needed; checked by the operations step | **Pending (operations)** |
| [OPEN-S4-06](#open-s4-06--removal-concurrency-and-lock-order) | GAR-005 | S4-GAR-01, S4-INT-01 | Conditional revision `UPDATE` on the garment, then a read of the photo, then the cleanup row. | Delegated default | Implemented 2026-11-21 (US-GAR-005); tests pass locally | Pending (CI run; client evidence) |
| [OPEN-S4-07](#open-s4-07--attachment-command-transport-and-outcomes) | GAR-006 | S4-GAR-04, S4-INT-02/03 | `POST …/{garmentId}/photo-attachment` returns `GarmentResponse`. A same-pair replay answers `200` without a new revision. | Delegated default | Pending | Pending |
| [OPEN-S4-08](#open-s4-08--attachment-transaction-check-order-and-lock-order) | GAR-006 | S4-GAR-05, S4-INT-01 | Lock the garment row, then read garment and photo afresh, then lock the photo. Check order: replay, already photographed, revision, usable. | Delegated default | Pending | Pending |
| [OPEN-S4-09](#open-s4-09--provisional-attachment-acceptance-criteria) | GAR-006 | S4-GAR-04 | Provisional P-AC-01–P-AC-06, mapped to plan §6.5; since 2026-11-17 formalized one to one as AC-01–AC-06 of the formal story US-GAR-006 (parent PBI-049) | Delegated default | Pending | Pending (PO acceptance of the formalized AC) |
| [OPEN-S4-10](#open-s4-10--attachment-photo-availability-and-pending-lifecycle-reuse) | GAR-006 | S4-GAR-06 | No object check. Trust READY metadata. A missing object gives `404 GARMENT_PHOTO_UNAVAILABLE`. | Delegated default | Pending | Pending |
| [OPEN-S4-11](#open-s4-11--searchable-descriptors-and-matching-semantics-cat-q01) | WAR-003 | S4-WAR-01 | English labels, codes and **Vietnamese search aliases** of six vocabularies, plus Brand. Literal, case-insensitive, accent-sensitive, NFC; ambiguous words match every named value. Cursor policy `m2`. **Refined 2026-11-17** (Vietnamese search authorized by the PO). | Delegated default (wording); capability PO-authorized | Pending | Pending (alias wording review, PostgreSQL locale) |
| [OPEN-S4-12](#open-s4-12--search-input-limits-and-wildcard-escaping) | WAR-003 | S4-WAR-01 | `q` is trimmed and NFC-normalized; a blank `q` means no condition. 1–100 code points, no control characters. Escape `\`, `%` and `_`. | Delegated default | Pending | Pending |
| [OPEN-S4-13](#open-s4-13--category-and-color-filters-and-the-statement-catalogue) | WAR-003 | S4-WAR-02 | Repeated `category` and `color` parameters: OR within a group, AND across groups. A catalogue of 14 complete statements for the 7 restricted shapes. | Delegated default | Pending | Pending |
| [OPEN-S4-14](#open-s4-14--criteria-bound-cursor-and-result-states) | WAR-003 | S4-WAR-03 | `v2` cursor carrying a criteria fingerprint; a mismatch gives `CONDITIONS_CHANGED`. `resultState` is computed in one read snapshot. | Delegated default | Pending | Pending |
| [OPEN-S4-15](#open-s4-15--search-indexing-evidence) | WAR-003 | S4-WAR-04 | No new index in Sprint 4. Documented `EXPLAIN` procedure and the triggers for adding one. | Delegated default | Pending | Pending (measurement) |
| [OPEN-S4-16](#open-s4-16--optional-genderbody-vocabulary) | PROF-002 | S4-PROF-01 | `genderContext`: 4 values; `bodyShape`: 5 geometric values. Absence means not provided. **Newly selected values**, defined canonically in SRS §4.10.1 since 2026-11-17. | Delegated default | Pending | **Pending (inclusive-representation review)** |
| [OPEN-S4-17](#open-s4-17--optional-context-resource-persistence-and-clearing) | PROF-002 | S4-PROF-02/03/04 | `/optional-context` resource, V13 table, presence-aware PATCH. No published API. | Delegated default | Pending | Pending |
| [OPEN-S4-18](#open-s4-18--location-representation-precision-and-manual-resolution) | CTX-001 | S4-CTX-01 | 2-decimal point rounded by the server; `DEVICE` or `MANUAL`; a label. The client resolves cities from a bundled reference. | Delegated default | Pending | Pending (client, privacy) |
| [OPEN-S4-19](#open-s4-19--location-revision-clearing-and-evidence-binding) | CTX-001 | S4-CTX-02 | V14 tables. PATCH `selection`. Weather is acquired after commit, and evidence is published only while the location revision is still current. | Delegated default | Pending | Pending |
| [OPEN-S4-20](#open-s4-20--open-meteo-request-and-semantic-mapping) | CTX-001 | S4-CTX-03 | Open-Meteo `current=`, `timeformat=unixtime`, WMO code mapped to a condition, `MODELLED` basis | Delegated default | Pending | **Pending (terms, live request)** |
| [OPEN-S4-21](#open-s4-21--weather-freshness-and-latest-evidence) | CTX-001 | S4-CTX-04 | Age measured from the provider's `current.time`; inclusive 30 minutes; 5-minute future tolerance; latest evidence only | Delegated default | Pending | Pending |
| [OPEN-S4-22](#open-s4-22--one-elapsed-acquisition-deadline) | CTX-001 | S4-CTX-05 | `PT2S` from adapter entry, including capacity wait. `RestClient` on JDK client; bounded executor; 64 KiB body | Delegated default | Pending | **Pending (measured)** |
| [OPEN-S4-23](#open-s4-23--environmental-basis-and-absent-consumers) | CTX-001 | S4-CTX-06 | Basis (revision, status, evidence time) is carried in the HTTP read model; no new published API until a consumer exists | Delegated default | Pending | Pending |
| [OPEN-S4-24](#open-s4-24--weather-privacy-configuration-and-test-isolation) | CTX-001 | S4-CTX-01/03/05 | Fixed trusted base URL (HTTPS, or loopback in tests). No coordinates in logs. Test double in integration tests. | Delegated default | Pending | Pending |
| [OPEN-S4-25](#open-s4-25--bulk-admission-limits-and-retention) | BULK-001 | S4-BULK-06 | 20 items, 3 active batches, `P7D` lifetime, aggregate 300,000,000 bytes derived from the photo rule. No server-side drafts. | Delegated default | Pending | Pending (PO review advised) |
| [OPEN-S4-26](#open-s4-26--batch-and-item-states) | BULK-001 | S4-BULK-02 | Item states `AWAITING_PHOTO`, `REVIEWABLE`, `ACCEPTED` and `CANCELED`, plus derived photo states. Batch `OPEN` or `STOPPED`, plus derived `EXPIRED`. | Delegated default | Pending | Pending |
| [OPEN-S4-27](#open-s4-27--batch-and-item-identity-and-idempotency) | BULK-001 | S4-BULK-05, S4-INT-02 | Batch Idempotency-Key plus a fingerprint. Server-generated item ids and acceptance keys. Item revisions. | Delegated default | Pending | Pending |
| [OPEN-S4-28](#open-s4-28--batch-stop-item-cancellation-and-lock-order) | BULK-001 | S4-BULK-07, S4-INT-01 | Batch `FOR UPDATE` for stop, `FOR SHARE` for item commands; then item, photo, garment. Unclaimed stages are cancelled. | Delegated default | Pending | Pending |
| [OPEN-S4-29](#open-s4-29--shared-acceptance-seam-and-per-item-transaction) | BULK-001 | S4-BULK-01/04 | `GarmentAcceptance` extracted from `AddGarmentService`. The item becomes `ACCEPTED` in the same transaction as the garment and photo. | Delegated default | Pending | Pending |
| [OPEN-S4-30](#open-s4-30--sequential-processing-expired-photos-and-recovery) | BULK-001 | S4-BULK-03/06 | The client stages sequentially. An expired photo means reselecting it. The batch list and detail support resumption. No worker, no AI. | Delegated default | Pending | Pending (client) |
| [OPEN-S4-31](#open-s4-31--migrations-openapi-versions-ci-and-shared-file-order) | All | S4-INT-03 | V12/V13/V14/V15; OpenAPI 0.12.0–0.17.0; `sprint_04` CI trigger; cumulative order | Delegated default | Pending | Pending |
| [OPEN-S4-32](#open-s4-32--error-vocabulary-additions) | All | S4-INT-02/03 | New codes and field-error codes; operation-neutral 409 and 503 texts | Delegated default | Pending | Pending |
| [OPEN-S4-33](#open-s4-33--configuration-properties-and-test-profile) | All | S4-GAR-03, S4-CTX-05, S4-BULK-06 | New `capsuleai.*` properties with startup validation, and test-profile values | Delegated default | Pending | Pending |

## 3. Coverage of the plan's named blockers

| Plan blocker ([§12](../sprint-plan.md#12-risks-and-open-questions)) | Resolved by | Remaining non-delegable evidence |
| --- | --- | --- |
| R4-02: no formal attachment story | OPEN-S4-07–10; since 2026-11-17 the formal [US-GAR-006](../../../stories/garment/US-GAR-006-attach-photo-to-existing-imageless-garment.md) and [PBI-049](../../../product-backlog.md#pbi-049--attach-a-photo-to-an-existing-imageless-garment) | PO acceptance of the formalized AC before calling GAR-006 Done |
| R4-03: CAT-Q01 | OPEN-S4-11–14, with the Vietnamese refinement of 2026-11-17 | Client query/reset evidence; review of the alias wording |
| R4-04: supported optional values | OPEN-S4-16 | Inclusive-representation review of the selected values |
| R4-06: location precision and manual source | OPEN-S4-18 | Mobile permission, manual and skip evidence |
| R4-07: provider terms, access and coverage | OPEN-S4-20, OPEN-S4-24 | **Open-Meteo terms/eligibility for the actual deployment and a real authorized request** |
| R4-08: timestamp and deadline semantics | OPEN-S4-21, OPEN-S4-22 | Measured behavior of the concrete client in the agreed environment |
| R4-10 to R4-12: minimal history, attached media, delete markers | OPEN-S4-02–05 | **Bucket versioning, object-lock, replication and backup inventory** |
| R4-13 to R4-14: lock order and replay | OPEN-S4-06, 08, 28 and 03, 27 | Real concurrent PostgreSQL tests (specified) |
| R4-15 to R4-16: batch limits and outcomes | OPEN-S4-25–30 | Client multiphoto and resume evidence |

---

## OPEN-S4-01 — Removal command transport and outcomes

**A. Question and decision.** How does a client ask for a confirmed removal, and what does it receive, including after a lost response? Realizes **S4-GAR-01** (guarded monotonic removal), **S4-INT-02** (natural monotonic identity) and **S4-INT-03** (explicit command, body revision, no DELETE body).

**B. Story and requirements.** US-GAR-005 AC-01, AC-06, AC-07; FR-WAR-004–006; UC-010 A1/E1/E2.

**C. Problem and context.** Current detail already answers `410 GARMENT_NO_LONGER_CURRENT` for a removed garment. A retried removal must not create a second event, reset the removal time or disclose another owner's garment. The garment revision advances once, at removal. The client resends the revision it reviewed, which is stale after the removal it is asking about.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. `DELETE /garments/{id}` with an `If-Match` revision header | Conventional HTTP; but needs a new header, CORS and contract convention, and confirmation semantics in a header. S4-INT-03 selected the body-based convention instead. |
| 2. `POST /garments/{id}/removal` with `{confirmed, expectedRevision}`; a repeat on an already removed garment returns `ALREADY_REMOVED` regardless of the revision | Fits existing JSON, CORS and revision conventions. Recovery after a lost response is one identical request. Reveals only to the owner that their own garment is removed. |
| 3. Same as 2, but a repeat with a stale revision answers `409` | Strict, but makes the most common recovery case (lost response) look like a conflict. |

**E. Selected solution and rationale.** **Option 2.**

- `POST /api/v1/wardrobe/garments/{garmentId}/removal` with body `{"confirmed": true, "expectedRevision": "<revision>"}`.
- Validation happens before any lookup:
  - `confirmed` missing → `REQUIRED`; `false` → `NOT_CONFIRMED`;
  - `expectedRevision` missing → `REQUIRED`; not a revision token → `INVALID_FORMAT`.
- `200 OK` with `GarmentRemovalResponse {id, outcome, state, category, color, removedAt}`:
  - `outcome` is `REMOVED` when this request removed the garment, `ALREADY_REMOVED` when it was already removed;
  - `state` is always `REMOVED_FROM_WARDROBE`;
  - Category and Color are the minimal historical meaning (OPEN-S4-02).
- `ALREADY_REMOVED` keeps the original `removedAt` and writes nothing.
- `409 REVISION_CONFLICT`: the garment is current but changed after the user reviewed it.
- `404 GARMENT_NOT_FOUND`: unknown id, malformed id or another owner's garment. These are indistinguishable.
- Cancellation before submission is a client action. No request means no effect.

The monotonic answer is the natural outcome identity S4-INT-02 prefers. No receipt table is added.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-GAR-01, S4-INT-02 and S4-INT-03.
- **Implemented in the repository on 2026-11-21** (US-GAR-005; `clean verify` passed locally). A CI run on a pull request to `sprint_04` is still required.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-GAR-005:

- Production: P7, P8, P9, P13, P14, P15, P22.
- Tests: T3, T9.

**H. Affected files.**

- `RemoveGarmentCommand.java`, `GarmentRemoval.java`, `RemoveGarmentService.java`;
- `GarmentRemovalRequest.java`, `GarmentRemovalResponse.java`, `WardrobeGarmentController.java`;
- `docs/05-api/openapi.yaml`.

**I. Required verification.**

- `GarmentRemovalApiIT`: first removal, repeat removal, stale revision, wrong owner, unknown id, missing fields, unusable session.
- `IdentityOpenApiContractIT`: the documented `REQUIRED` fields.
- Client evidence of confirm/cancel and outcome review.

**J. Change impact.** Switching to DELETE would need:

- a new controller mapping, a CORS header and an OpenAPI change;
- client changes.

The application service would not change.

---

## OPEN-S4-02 — Retained minimal removed representation

**A. Question and decision.** Which facts survive removal, and which are purged? Realizes **S4-GAR-02** (minimized removed row inside Wardrobe).

**B. Story and requirements.** US-GAR-005 AC-04, AC-05; DATA-HIST-001/002, DATA-RET-002; BRULE-HIST-002/003.

**C. Problem and context.**

- A garment row holds the 13 properties, Brand and Price, provenance, the creation fingerprint, Materials and Styles child rows, and a photo linkage.
- No report or Wear consumer exists.
- The plan lists the candidate minimum: stable reference, removal label and time, Category/Color, narrow action markers.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Keep the whole row and child sets, with access excluded | Violates the 30-day nonessential-data deletion. Rejected by S4-GAR-02. |
| 2. Keep id, owner, creation key, creation fingerprint, Category, Color, provenance, created/removed/minimized times and the revision. Null every other property and delete the child sets, the photo linkage row and the object. | Smallest set that still answers "Removed: Navy Tops", refuses resurrection and supports outcome review. The fingerprint is a SHA-256 of the original confirmed values. It is kept for exact replay semantics (OPEN-S4-03). |
| 3. As 2, without Category and Color | Removed items become unrecognizable in any future history. Contradicts "understandable historical meaning". |

**E. Selected solution and rationale.** **Option 2.**

Retained columns:

- `id`, `owner_account_id`;
- `creation_key`, `creation_fingerprint`;
- `category`, `color`, `value_provenance`;
- `created_at`, `removed_at`, `revision`, `minimized_at`.

Purged (set to NULL):

- `subtype`, `pattern`, `fit`, `neckline`, `sleeve_length`, `length`, `silhouette`;
- `brand`, `price_amount`, `price_currency`.

Deleted:

- all `wardrobe_garment_material` and `wardrobe_garment_style` rows;
- the ATTACHED `wardrobe_photo_upload` row, which holds dimensions and the content hash;
- the object itself, after its key has been copied into the cleanup row (OPEN-S4-04).

A V12 CHECK enforces the minimized shape. Minimized rows still map to a valid minimum profile, so existing readers cannot fail. The removal and creation-replay paths expose only the minimal projection `RemovedGarment`.

The fingerprint is a one-way digest kept for anti-duplication. For a minimum-only garment it reveals nothing beyond the retained Category and Color.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-GAR-02.
- **Implemented in the repository on 2026-11-21** (US-GAR-005; `clean verify` passed locally). A CI run on a pull request to `sprint_04` is still required.
- `VERIFICATION PENDING`. A privacy review of the retained set is advised.

**G. Implementation steps.** US-GAR-005:

- Production: P1, P3, P12 (`minimizeRemovedGarment`).
- Tests: T8, T11.

**H. Affected files.** `V12__add_wardrobe_garment_removal.sql`, `RemovedGarment.java`, `JdbcGarmentRemovalRepository.java`.

**I. Required verification.**

- `GarmentRemovalSchemaIT`: the CHECK rejects a minimized row that still has rich data.
- `GarmentRemovalCleanupIT`: every purged column is NULL and the child rows and photo row are gone. Unrelated garments are untouched.

**J. Change impact.** Retaining more facts:

- relax the V12 CHECK in a new migration (V12 itself is immutable once applied);
- adjust the minimization statement.

Retaining fewer facts, for example dropping the fingerprint: replay can then only answer `410` without detecting key reuse.

---

## OPEN-S4-03 — Creation replay after removal

**A. Question and decision.** What does a late retry of the original creation request return after the garment was removed? Realizes **S4-GAR-02** and **S4-INT-02** ("accepted replay returns current/removed truth rather than resurrecting deleted content").

**B. Story and requirements.** US-GAR-005 AC-06; DATA-INT-004; BRULE-GAR-009.

**C. Problem and context.** Today `AddGarmentService.replay` returns the recorded garment with `200`, whatever its removal state. After removal, that would return a removed garment as if it were current. After minimization, it would return a degraded profile.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Keep returning `200` with the garment | Misrepresents a removed garment as the current result of the addition |
| 2. Fingerprint check first (`422 IDEMPOTENCY_KEY_REUSED` on mismatch), then `410 GARMENT_NO_LONGER_CURRENT` | Truthful, never recreates, discloses nothing new to the owner |
| 3. Treat the key as unused and create a new garment | Resurrects a removed item. Forbidden. |

**E. Selected solution and rationale.** **Option 2.** This applies to both imageless and photo-backed replays. The photo-backed path already locks the stage, and its replay check comes before the usability check, so a consumed stage does not mask the `410`.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-GAR-02 and S4-INT-02.
- **Implemented in the repository on 2026-11-21** (US-GAR-005; `clean verify` passed locally). A CI run on a pull request to `sprint_04` is still required.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-GAR-005:

- Production: P11.
- Tests: T3, T9.

US-BULK-001 P8 preserves this behavior inside `GarmentAcceptance`.

**H. Affected files.** `AddGarmentService.java`; later `GarmentAcceptance.java`.

**I. Required verification.** `GarmentRemovalApiIT.replayingTheOriginalAdditionAfterRemovalIsGoneAndCreatesNothing`, plus the unchanged `AddGarmentServiceTest` and `AddGarmentApiIT` suites.

**J. Change impact.** Only the replay method changes. The client must treat a `410` on a retried addition as "added earlier, removed since".

---

## OPEN-S4-04 — Durable removal-cleanup work and schedule

**A. Question and decision.** How is the photo purge and rich-data minimization performed reliably within 30 days? Realizes **S4-GAR-03** (separate durable cleanup, lease/retry, deletion outside transactions).

**B. Story and requirements.** US-GAR-005 AC-05; DATA-RET-002; BRULE-GAR-009, BRULE-HIST-003.

**C. Problem and context.**

- V10 forbids turning an ATTACHED row into DISCARDED with its garment link.
- Pending cleanup must never select attached media.
- Storage may be down.
- A process may crash between deleting the object and recording the deletion.
- The 30-day clock starts at the original removal.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Minimize and delete inside the removal request | The object call would sit inside or after the request with no durable retry. Rejected by S4-GAR-03. |
| 2. Cleanup table `wardrobe_garment_removal_cleanup`, written in the removal transaction. A scheduled worker claims due rows with `FOR UPDATE SKIP LOCKED` and a lease, minimizes the data in a short transaction, then deletes the object outside any transaction and records it. | Survives crashes; retries idempotently. The deadline is stored once (`removed_at + P30D`) and never reset. |
| 3. Generalize the pending-photo worker | Mixes the 24-hour and 30-day clocks. Rejected by S4-GAR-03. |

**E. Selected solution and rationale.** **Option 2.**

The cleanup row is keyed by `garment_id`, so there is one obligation per garment. It records:

- `removed_at` and `purge_deadline`;
- the attached `photo_upload_id` and `photo_object_key`, if any;
- `next_attempt_at` (the lease), `attempts`;
- `data_minimized_at`, `object_deleted_at`, `completed_at`.

Each run, all values from configuration:

1. Claim up to `batch-size` (50) due rows. The lease is `retry-delay` (`PT15M`).
2. For each row, minimize the data in a transaction if that is not done yet. Minimization needs no network, so it happens promptly, at the first run after removal.
3. Delete the object outside any transaction if a key is present and not yet deleted; then record it.
4. Mark the row complete when both steps are done.

A storage failure leaves the lease to expire for a retry.

Schedule: `initial-delay` `PT2M`, `interval` `PT5M`. Rows older than `overdue-warning-age` (`P21D`) and still incomplete are counted and logged at WARN, with counts only and no identifiers or keys. That leaves nine days for an operator to intervene before the 30-day limit.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-GAR-03.
- **Implemented in the repository on 2026-11-21** (US-GAR-005; `clean verify` passed locally). A CI run on a pull request to `sprint_04` is still required.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-GAR-005:

- Production: P1, P4, P5, P6, P10, P12, P17, P18, P19, P20, P21.
- Tests: T4, T5, T11.

**H. Affected files.**

- V12; `GarmentRemovalRepository.java`, `GarmentRemovalCleanupTarget.java`, `GarmentRemovalPolicy.java`;
- `GarmentRemovalCleanupService.java`, `JdbcGarmentRemovalRepository.java`, `GarmentRemovalCleanupJob.java`;
- `WardrobeRemovalProperties.java`, `WardrobeRemovalConfiguration.java`, `WardrobeRemovalCleanupConfiguration.java`;
- `application.properties`, `application-test.properties`.

**I. Required verification.** `GarmentRemovalCleanupIT`:

- deletion failure followed by a retry;
- a crash after the object delete but before the record (a repeat delete is harmless);
- duplicate claims are excluded;
- the overdue count.

Operational evidence that the job runs in the deployment.

**J. Change impact.** The values are configuration and need no code change. Changing the model (for example, minimization inside the removal transaction) affects P9, P10 and P12 only.

---

## OPEN-S4-05 — Storage versioning and physical-deletion boundary

**A. Question and decision.** When is a removed garment's photo physically deleted, given that a DELETE in a versioned bucket only adds a delete marker? Realizes **S4-GAR-03** ("establish an unversioned deletion boundary or implement verified deletion of applicable versions/copies").

**B. Story and requirements.** US-GAR-005 AC-05; DATA-RET-002; NFR-PRIV-001/002.

**C. Problem and context.**

- `S3PhotoStorage.delete` issues a key-only `DeleteObject`.
- In a versioned bucket that creates a delete marker, and noncurrent versions remain.
- S3Mock is ephemeral and proves nothing about production.
- Bucket versioning, object lock, replication and backup policy are unknown in the repository.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Require an unversioned bucket (versioning never enabled), no object lock, no replication, and no backup that keeps photo objects longer than 30 days. Keep the key-only delete. Prove the configuration operationally. | No new S3 API surface. The deletion boundary is clear and inspectable. Depends on infrastructure configuration that must be checked and monitored. |
| 2. Version-aware deletion: `ListObjectVersions` followed by `DeleteObject` with `versionId` for every version and delete marker | Works with versioning, but adds API surface, permissions (`s3:DeleteObjectVersion`) and a version enumeration that must itself be tested. Backups and replicas still need separate handling. |

**E. Selected solution and rationale.** **Option 1.** It keeps the adapter unchanged and makes the boundary explicit. The guide's operational checklist requires these AWS CLI checks against the photo bucket, run by operations without exposing credentials:

1. `aws s3api get-bucket-versioning`: no `Status`, meaning versioning was never enabled.
2. `aws s3api get-object-lock-configuration`: `ObjectLockConfigurationNotFoundError`.
3. `aws s3api get-bucket-replication`: `ReplicationConfigurationNotFoundError`.
4. Backup inventory confirms no photo-object backup outlives 30 days.

If any check fails, removal is not compliant. Option 2 must then be implemented before claiming AC-05. The guide marks this as a hard DoD item.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-GAR-03.
- No code change is needed; the US-GAR-005 code that relies on this boundary was implemented on 2026-11-21.
- **`VERIFICATION PENDING`: an operations check is required. It is not done and not claimed.**

**G. Implementation steps.** US-GAR-005:

- §14 (manual verification), steps M8–M9.
- §16 DoD item "storage deletion boundary".

The version-aware alternative is described in §17 Troubleshooting.

**H. Affected files.** None in source. This affects deployment configuration and operations evidence.

**I. Required verification.** The four checks above, recorded with the date and environment and without secrets. Re-run them after any infrastructure change.

**J. Change impact.** Choosing Option 2 later would add:

- a `deleteAllVersions` method to `PhotoStorage` and `S3PhotoStorage`;
- tests against a versioned S3-compatible fixture;
- the IAM permission `s3:DeleteObjectVersion`.

---

## OPEN-S4-06 — Removal concurrency and lock order

**A. Question and decision.** How does removal serialize with edits, attachment, repeated removal, cleanup and creation replay? Realizes **S4-GAR-01** and **S4-INT-01** (existing garment before photo, then cleanup work).

**B. Story and requirements.** US-GAR-005 AC-02, AC-06, AC-07.

**C. Problem and context.**

- Edits use a conditional `UPDATE … WHERE revision = :expected`.
- Attachment (GAR-006) locks the garment, then the photo.
- Cleanup touches the garment, the photo row and the cleanup row.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Conditional `UPDATE` of the garment (owner, current, revision), then read the attached photo while holding the garment row lock, then insert the cleanup row. Minimization takes garment, then photo, then cleanup. | One canonical order with no inversion. The READ COMMITTED re-evaluation of the conditional predicate decides the winner. |
| 2. `SELECT … FOR UPDATE` on the photo first | Inverts S4-INT-01 for existing garments. Rejected. |

**E. Selected solution and rationale.** **Option 1.** Outcomes:

- Two removals: one gets `REMOVED`. The other re-evaluates, finds `removed_at` set and answers `ALREADY_REMOVED`.
- Edit against removal: the loser gets `409` or `410`.
- Attachment against removal: removal waits on the attachment's garment lock, then fails its revision predicate and answers `409`. Or the attachment waits, then finds the garment removed and answers `410`.
- Cleanup never touches a current garment.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-GAR-01 and S4-INT-01.
- **Implemented in the repository on 2026-11-21** (US-GAR-005; `clean verify` passed locally). A CI run on a pull request to `sprint_04` is still required.
- `VERIFICATION PENDING`.

**G. Implementation steps.**

- US-GAR-005: P9, P12; T10.
- US-GAR-006 T5 covers the attachment race.

**H. Affected files.** `RemoveGarmentService.java`, `JdbcGarmentRemovalRepository.java`; tests `ConcurrentGarmentRemovalIT`, `ConcurrentGarmentPhotoAttachmentIT`.

**I. Required verification.** Barrier-based tests on real PostgreSQL:

- two removals;
- removal against an edit;
- removal against an attachment;
- no deadlock within the test timeout.

**J. Change impact.** Any new operation touching both a garment and a photo must use the same order (garment, then photo). See [shared architecture §5](shared-architecture.md#5-lock-order-and-transaction-authority).

---

## OPEN-S4-07 — Attachment command transport and outcomes

**A. Question and decision.** How is a reviewed photo attached to an existing imageless garment, and how is a lost response recovered? Realizes **S4-GAR-04**, **S4-INT-02** and **S4-INT-03**.

**B. Story and requirements.** Provisional US-GAR-006 (plan §6.5); DATA-GAR-004.

**C. Problem and context.**

- The existing garment PATCH must not gain a media field (S4-GAR-04).
- The creation endpoint creates new garments only.
- The pair (garment, photo) is the user's reviewed intent.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. `POST /garments/{garmentId}/photo-attachment` with `{confirmed, expectedRevision, photoUploadId}`. Returns `GarmentResponse`. A same-pair replay answers `200` unchanged. | Explicit command consistent with removal. Recovery is the identical request. |
| 2. `PUT /garments/{garmentId}/photo` with a multipart upload | Re-uploads and re-decodes, which S4-GAR-06 rejects. It is also replacement-shaped. |

**E. Selected solution and rationale.** **Option 1.**

- Validation happens before lookup:
  - `confirmed`: `REQUIRED` or `NOT_CONFIRMED`;
  - `expectedRevision`: `REQUIRED` or `INVALID_FORMAT`;
  - `photoUploadId`: `REQUIRED` or `INVALID_FORMAT`.
- `200` returns the garment with `photo` and the revision increased by one.
- A replay of the same pair returns `200` with the current garment and no revision change.
- Errors:
  - `404 GARMENT_NOT_FOUND`;
  - `410 GARMENT_NO_LONGER_CURRENT`;
  - `404 PHOTO_UPLOAD_NOT_FOUND`;
  - `409 GARMENT_ALREADY_HAS_PHOTO` (new);
  - `409 REVISION_CONFLICT`;
  - `409 PHOTO_UPLOAD_NOT_USABLE`.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-GAR-04, S4-INT-02 and S4-INT-03.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`. Formal story refinement is still needed (OPEN-S4-09).

**G. Implementation steps.** US-GAR-006:

- Production: P3, P4, P5, P6, P8, P9, P10, P12.
- Tests: T3, T4.

**H. Affected files.**

- `AttachGarmentPhotoCommand.java`, `GarmentPhotoAttachment.java`, `GarmentAlreadyHasPhotoException.java`, `AttachGarmentPhotoService.java`;
- `GarmentPhotoAttachmentRequest.java`, `WardrobeGarmentController.java`, `WardrobeApiExceptionHandler.java`;
- `openapi.yaml`.

**I. Required verification.**

- `GarmentPhotoAttachmentApiIT`, covering every outcome.
- The contract test.
- Client review, confirm and cancel evidence.

**J. Change impact.** The route name can change with only the controller and OpenAPI changing.

---

## OPEN-S4-08 — Attachment transaction, check order and lock order

**A. Question and decision.** In which order are the locks taken and the conditions checked? Realizes **S4-GAR-05** (one garment revision, short locks, atomic stage consumption) and **S4-INT-01**.

**B. Story and requirements.** Provisional US-GAR-006 Atomicity/authority, Retry/uncertainty and Cleanup/races.

**C. Problem and context.**

- Competing attachments, edits, removal and pending expiry can all target the same garment or photo.
- PostgreSQL rejects `FOR UPDATE` on the nullable side of an outer join. The garment select joins the photo with `LEFT JOIN`.
- Under READ COMMITTED, a locking statement that waited re-checks only the locked row. Joined rows keep the statement's original snapshot, so the photo must be read in a new statement after the lock.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Lock the garment row (`SELECT id … FOR UPDATE` on the garment table only), then read the garment and its photo in a new statement, then lock the photo with `lockOwned`. Check replay first, then "garment already has a photo", then the revision, then usability. Attach and advance the revision. | Canonical order; the read after the lock is consistent; replay answers lost responses before a stale revision is treated as new; one transaction |
| 2. Photo first, as in new-garment creation | Not allowed for existing garments (S4-INT-01) |

**E. Selected solution and rationale.** **Option 1.** The revision advances exactly once, through a conditional `UPDATE` that must affect exactly one row. Database guarantees remain:

- `uq_wardrobe_photo_upload_garment` prevents two photos on one garment;
- `attach … WHERE status = 'READY'` prevents consuming a stage twice.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-GAR-05 and S4-INT-01.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-GAR-006:

- Production: P1, P2, P6, P7.
- Tests: T1, T3, T5.

**H. Affected files.** `Garment.java`, `GarmentPhotoAttachmentRepository.java`, `AttachGarmentPhotoService.java`, `JdbcGarmentPhotoAttachmentRepository.java`.

**I. Required verification.** `ConcurrentGarmentPhotoAttachmentIT`:

- two photos competing for one garment;
- one photo competing for two garments;
- attachment against removal;
- attachment against expiry.

**J. Change impact.** A separate photo-association revision (S4-GAR-05 Option B) would need a new column and contract fields. It is not selected.

---

## OPEN-S4-09 — Provisional attachment acceptance criteria

**A. Question and decision.** Which acceptance criteria does the GAR-006 guide test, given that no formal story existed when the story was selected? (Since 2026-11-17 the formal story exists; see F.) Realizes **S4-GAR-04**.

**B. Story and requirements.** Plan §6.5 provisional expectations; supporting BR-005, DATA-GAR-004.

**C. Problem and context.** The plan forbids fabricated approved AC identifiers. Tests still need stable names.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Name them P-AC-01–P-AC-06, labelled **PROVISIONAL**, one per plan expectation. Keep the plan's wording verbatim next to each. | Testable, traceable, and clearly not approved AC |
| 2. Use AC-01… | Would look like approved story AC. Forbidden. |

**E. Selected solution and rationale.** **Option 1.** The mapping from plan §6.5 is:

| Provisional criterion | Plan expectation |
| --- | --- |
| P-AC-01 | Eligibility |
| P-AC-02 | Confirm / cancel |
| P-AC-03 | Atomicity / authority |
| P-AC-04 | Retry / uncertainty |
| P-AC-05 | Privacy / availability |
| P-AC-06 | Cleanup / races |

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING`: PO acceptance of the formalized criteria is required before Done.**
- **Update 2026-11-17:** the formal Draft story [US-GAR-006](../../../stories/garment/US-GAR-006-attach-photo-to-existing-imageless-garment.md) and its parent [PBI-049](../../../product-backlog.md#pbi-049--attach-a-photo-to-an-existing-imageless-garment) now exist. The story's AC-01–AC-06 restate P-AC-01–P-AC-06 one to one. The canonical documents gained FR-WAR-009, SRS §3.6.3, BRULE-GAR-011, PRD FEAT-WAR-002 behavior 7 with AC-08–AC-09, and UC-009 A4/E5. The AC are a delegated formalization, not a separate PO approval.

**G. Implementation steps.** US-GAR-006 §7 and §15.

**H. Affected files.** The US-GAR-006 guide only.

**I. Required verification.** PO/BA formal refinement; client evidence.

**J. Change impact.** If refinement changes an expectation, update the tests named in US-GAR-006 §15.

---

## OPEN-S4-10 — Attachment photo availability and pending-lifecycle reuse

**A. Question and decision.** Does confirmation check the object store, and what happens if the bytes disappear later? Realizes **S4-GAR-06**.

**B. Story and requirements.** Provisional Privacy/availability and Cleanup/races; DATA-GAR-004; NFR-PRIV-001/002.

**C. Problem and context.**

- READY is persisted only after successful normalization and storage.
- A normalized derivative may be smaller than the 512-pixel original minimum.
- A network check cannot be atomic with the commit.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Trust READY metadata; no object call. The existing private delivery route returns `404 GARMENT_PHOTO_UNAVAILABLE` when bytes are missing. | No extra dependency; truthful limitation |
| 2. HEAD the object first | Still racy; adds latency |

**E. Selected solution and rationale.** **Option 1.**

- The derivative is never re-validated against original-input rules.
- ATTACHED rows are never selected by pending expiry or cleanup (unchanged V10 and SQL).
- An unclaimed stage keeps the unchanged `PT24H` TTL; review or retry never extends it.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-GAR-06.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-GAR-006:

- Production: P6 (no storage dependency).
- Tests: T4 (a missing object gives `404` while detail stays readable), T5 (expiry against attachment).

**H. Affected files.** `AttachGarmentPhotoService.java`.

**I. Required verification.** As listed in G.

**J. Change impact.** A future integrity requirement would add a pre-transaction check. It would provide no atomicity guarantee.

---

## OPEN-S4-11 — Searchable descriptors and matching semantics (CAT-Q01)

> **Refined on 2026-11-17 — Vietnamese descriptor search.** The Product Owner explicitly authorized adding Vietnamese search labels and common aliases to the matching semantics of US-WAR-003, for all six searchable vocabularies. This entry records the previous delegated default, the refinement and its consequences. The refinement changes only what a text condition matches; every other rule of S4-WAR-01 and OPEN-S4-12–14 is unchanged.

**A. Question and decision.** Which fields match a free-text query, and how? Realizes **S4-WAR-01** (literal, case-insensitive, accent-sensitive substring; one phrase; OR across fields; Price excluded).

**B. Story and requirements.** US-WAR-003 AC-01, AC-03, AC-05; FR-WAR-007/008, DATA-GAR-005, LOC-002, LOC-004, LOC-006 (added 2026-11-17), [SRS §3.6.2](../../../../03-requirements/SRS.md#362-wardrobe-search-and-filter-rules); BRULE-GAR-004.

**C. Problem and context.**

- Controlled values are stored as codes. The backend vocabularies carry only the approved English labels; before this refinement no Vietnamese label table existed anywhere in the repository.
- The product's interface is Vietnamese (LOC-001). A user who types `áo thun`, `trắng` or `chân váy` expects to find T-shirts, white garments and skirts. English-only matching would make search unusable for them.
- Brand is free Unicode text, kept exactly as entered.
- PostgreSQL `lower()` and `ILIKE` depend on the database ctype.
- Vietnamese keyboards write the tone mark of an open "oa", "oe" or "uy" syllable in one of two standard positions (`họa` or `hoạ`, `khóa` or `khoá`).

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. *(previous default)* Match controlled values in Java through their English labels and codes only, lower-cased with `Locale.ROOT`, NFC, into code sets. Match Brand in SQL with `lower(normalize(brand, NFC)) LIKE lower(:pattern) ESCAPE '\'`. | Simple; never depends on the database locale. But Vietnamese descriptor words find nothing, so the search barely serves Vietnamese users. |
| 2. *(selected 2026-11-17)* Option 1 plus a centralized, reviewed table of Vietnamese search labels and common aliases for every value of the six vocabularies, held in one application class and in one canonical requirements document that a test keeps identical. An alias matches through the same substring rule. A word that names several values matches all of them (OR). Each alias is also matched in its other standard tone-mark placement. | Vietnamese queries work and stay accent-sensitive. Codes, labels and stored data are unchanged. Costs a 195-row table to maintain and a cursor-policy change. |
| 3. A database code→label table, joined in SQL | Duplicates the vocabulary authority in a second store; needs a migration and new ownership rules; locale-dependent folding |
| 4. Accent folding (`unaccent`), tokenization or full-text search | Contradicts S4-WAR-01 (literal, accent-sensitive phrase) and needs an extension or a search engine |
| 5. Leave Vietnamese to the client: it translates the query into codes and sends filters | Only works for Category and Color, which have filters; the other four vocabularies stay unsearchable in Vietnamese; every client re-implements the table |

**E. Selected solution and rationale.** **Option 2** (since 2026-11-17; Option 1 was the delegated default of 2026-11-16).

- **Fields.** Category, Subtype, Color, Material, Pattern and Style: each value's code, approved English label and Vietnamese search aliases. Brand literally. Price, Fit, Neckline, Sleeve Length, Length and Silhouette are excluded, as are personal context and images.
- **Phrase.** One literal phrase, matched when it is a substring of any participating field (OR across fields). No tokens, no wildcards. `áo thun trắng` is not split into two conditions.
- **Case and accents.** Case-insensitive; accent-sensitive; NFC. `ÁO THUN` matches `áo thun`; `ao thun` does not. `trắng` matches White; `trang` does not.
- **Aliases.** The [canonical alias table](../../../../03-requirements/vocabularies/vietnamese-descriptor-search-aliases.md) lists, for all 195 values, a Vietnamese search label and common aliases, for example `T_SHIRTS` → `áo thun`, `áo phông`; `WHITE` → `trắng`, `màu trắng`; `SKIRTS` → `chân váy`; `TOPS` → `áo`. Aliases are search vocabulary only: they are never stored, never returned as codes and never accepted by the `category` and `color` filters, which keep taking exact codes.
- **Ambiguity.** An alias substring shared by several values is not resolved to one meaning. `váy` matches Dresses/Jumpsuits and Skirts; `xanh` matches every blue and green Color; `tím` matches Purple, Lavender and Navy (`xanh tím than`). Each matched value enters its field's code set, and the fields are combined with OR. The canonical table documents these cases.
- **Tone-mark placement.** The table is written in one placement; the class also matches each alias in the other standard placement of its open "oa", "oe" and "uy" syllables. This adds a correctly accented spelling. It is not accent folding: `hoa` still never matches `họa`.
- **Brand** keeps its literal behavior, with no alias and no placement variant.
- **Cursor policy.** Because the same phrase can match more garments, the matching-policy tag in the cursor fingerprint changes from `m1` to `m2` (see OPEN-S4-14). A cursor issued under `m1` is refused with `CONDITIONS_CHANGED`.

Rationale: matching in Java against one reviewed table keeps the database locale out of descriptor matching, keeps S4-WAR-01's literal and accent-sensitive semantics, and makes the Vietnamese words reviewable in one place. The test that compares the class with the requirements document prevents the two from drifting.

**F. Authority and status.**

- The **capability** (Vietnamese descriptor search for the six vocabularies) was **explicitly authorized by the Product Owner on 2026-11-17**. It refines the approved S4-WAR-01. It does not change any of the 30 approved decisions.
- The **specific alias wording** remains `SELECTED BY CLAUDE — DELEGATED DEFAULT`. It is not a Product Owner approval of each Vietnamese word. A review by the PO or a native-speaker reviewer is recommended before release.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`. This includes the database-ctype behavior of `lower()` for Vietnamese Brand text on the deployed PostgreSQL. Descriptor aliases are folded in Java and do not depend on it.

**G. Implementation steps.** US-WAR-003:

- Production: P1 (policy `m2`), P2, P3 (`VietnameseDescriptorAliases`), P4 (`DescriptorMatcher`), P12, P20 (OpenAPI descriptions).
- Tests: T2, T3, T4, T7, T8, T10.

**H. Affected files.**

- `WardrobeSearchCriteria.java`, `WardrobeSearchQuery.java`, `VietnameseDescriptorAliases.java` (new), `DescriptorMatcher.java`, `JdbcWardrobeSearchRepository.java`;
- `docs/05-api/openapi.yaml` (descriptions of `q`, `category`, `color` and the overview, in the guide's step P20);
- the canonical table `docs/03-requirements/vocabularies/vietnamese-descriptor-search-aliases.md` (created 2026-11-17).

**I. Required verification.**

- `VietnameseDescriptorAliasesTest`: every one of the 195 values has a search label; every alias is lower-case NFC without outer spaces; the class equals the canonical document; both tone placements; unsearchable vocabularies rejected.
- `DescriptorMatcherTest`: Vietnamese labels and aliases, letter case, accent sensitivity, ambiguous words, tone placement.
- `WardrobeSearchCriteriaTest` and `WardrobeSearchRequestTest`: the `m2` fingerprint; an `m1` cursor gives `CONDITIONS_CHANGED`.
- `JdbcWardrobeSearchRepositoryIT`:
  - Vietnamese aliases find garments through their stored codes;
  - an ambiguous word returns both meanings, and a code filter still narrows them;
  - Vietnamese Brand with `Đ` and `ạ`; case; accent sensitivity; NFC versus NFD input; each descriptor field; Price excluded.
- `SearchWardrobeApiIT`: a Vietnamese alias over HTTP, echoed as typed; the unaccented phrase gives `NO_MATCH`.
- Run the integration tests on the deployed database's collation.
- A wording review of the alias table (PO or native speaker).

Scratch evidence of 2026-11-17: the six guides, including this refinement, were applied to an isolated copy together with the canonical alias document. `clean verify` passed there, including every test listed above (687 unit and 435 integration tests, none failed or skipped; [README §11](README.md#11-status)). Nothing was run in the repository, and the alias wording has not been reviewed.

**J. Change impact.**

- **Changing an alias** means editing the canonical document and `VietnameseDescriptorAliases` together (T10 fails otherwise), moving the matching-policy tag to the next value (`m3`), and updating this entry.
- **A new approved vocabulary value** needs a row; the class refuses to initialize without one.
- **Making another vocabulary searchable**, or **accent-insensitive matching**, would change S4-WAR-01 and needs a Product Owner decision.
- **A client** can reuse the Vietnamese search labels as display labels or suggestions; the client keeps final display wording (LOC-002).

---

## OPEN-S4-12 — Search input limits and wildcard escaping

**A. Question and decision.** What input is accepted, and how is user text kept literal? Realizes **S4-WAR-01**.

**B. Story and requirements.** US-WAR-003 AC-01, AC-06.

**C. Problem and context.**

- `%`, `_` and `\` are LIKE metacharacters.
- Clients may send NFD text, which iOS keyboards sometimes produce.
- Unbounded input is a denial-of-service risk.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Strip outer whitespace. A blank `q` is no condition. NFC-normalize. 1–100 code points. Reject ISO control characters. Escape `\`, `%` and `_` with `\` in the persistence adapter. | Literal semantics; bounded; reset by clearing `q` |
| 2. Treat a blank `q` as an error | Makes "clear" awkward |

**E. Selected solution and rationale.** **Option 1.**

- More than 100 code points → `400 VALIDATION_FAILED`, field `q`, `OUT_OF_RANGE`.
- A control character → `INVALID_FORMAT`.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-WAR-003:

- Production: P1, P12, P13.
- Tests: T3, T4, T7.

**H. Affected files.** `WardrobeSearchCriteria.java`, `JdbcWardrobeSearchRepository.java`, `WardrobeSearchRequest.java`.

**I. Required verification.** Literal `%`, `_` and `\` searches match only literal characters. Injection-like strings are treated as values.

**J. Change impact.** The limits are constants in `WardrobeSearchCriteria`.

---

## OPEN-S4-13 — Category and color filters and the statement catalogue

**A. Question and decision.** How are filters encoded and combined, and how is SQL kept complete and bound? Realizes **S4-WAR-02** (finite catalogue; OR within a group, AND across groups; an empty group means no restriction; EXISTS for sets).

**B. Story and requirements.** US-WAR-003 AC-02, AC-03; FR-WAR-001/007/008; AR-01–08.

**C. Problem and context.**

- The convention requires each statement to be complete.
- JDBC array binding through `JdbcClient` named parameters is not used anywhere in the repository.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Repeated query parameters `category=TOPS&category=PANTS` and `color=…`; exact codes. Bind each code set as one comma-joined text with `= ANY(string_to_array(CAST(:x AS text), ','))`. 7 restricted shapes × (first page, after cursor) = 14 compile-time-constant statements. The unrestricted overview keeps the 2 existing statements of `JdbcGarmentRepository`. | Codes never contain commas. One bound text parameter per set. No runtime SQL assembly. Each statement is complete and reviewable; a test checks the owner/current guards of all 14. |
| 2. One statement with optional `IS NULL OR` predicates | Rejected by S4-WAR-02 (generic plans) |

**E. Selected solution and rationale.** **Option 1.**

- An unknown or lower-case code → `400`, field `category` or `color`, `UNSUPPORTED_VALUE`.
- Duplicates collapse.
- More than 50 raw values in one group → `OUT_OF_RANGE`.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-WAR-02.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-WAR-003:

- Production: P11, P12, P13.
- Tests: T4, T6, T7.

**H. Affected files.** `SearchStatementCatalogue.java`, `JdbcWardrobeSearchRepository.java`, `WardrobeSearchRequest.java`.

**I. Required verification.**

- Every shape against real PostgreSQL.
- No duplicate rows when several Materials or Styles match.
- `SearchStatementCatalogueTest`.

**J. Change impact.** A new filter group doubles the catalogue. Review against S4-WAR-02 Option A if more groups are approved.

---

## OPEN-S4-14 — Criteria-bound cursor and result states

**A. Question and decision.** How is a cursor bound to its conditions, and how does the API distinguish no-match from an empty wardrobe? Realizes **S4-WAR-03** (keyset, criteria-bound cursor, live view, coherent observation).

**B. Story and requirements.** US-WAR-003 AC-02–AC-06; UI-003/004; ERR-NET-001.

**C. Problem and context.**

- The v1 cursor (`v1|createdAt|id`) carries no conditions.
- Existing clients rely on it.
- An empty filtered page needs a second fact: whether the owner has any current garment.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Unfiltered requests keep v1 cursors unchanged. Filtered requests issue `v2|createdAt|id|fp`, where `fp` is the first 16 hex characters of SHA-256 over `m2|q=<NFC lower>|c=<sorted codes>|k=<sorted codes>` (policy tag `m1` until the Vietnamese refinement of OPEN-S4-11 on 2026-11-17). A mismatched or v1-on-filtered cursor → `400`, cursor `CONDITIONS_CHANGED`. `resultState`: `HAS_RESULTS`, `NO_MATCH`, `EMPTY_WARDROBE` or `END_OF_RESULTS`. An empty first page is resolved in one REPEATABLE READ read-only transaction (the page query plus an existence query). | Backward compatible; coherent snapshot; explicit reset path |
| 2. Always v2 | Breaks clients holding v1 cursors mid-traversal |

**E. Selected solution and rationale.** **Option 1.**

- `END_OF_RESULTS` appears only on a later page that came back empty, because garments were removed meanwhile.
- The fingerprint is not authorization: every page is still owner- and current-scoped.
- **Matching-policy tag.** The tag names the matching rules the cursor was computed under. The Vietnamese aliases of OPEN-S4-11 (2026-11-17) moved it from `m1` to `m2`. A cursor issued under `m1` never matches an `m2` fingerprint, so it is refused with `CONDITIONS_CHANGED` instead of continuing a traversal under different rules. Unfiltered `v1` cursors are unaffected.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-WAR-03.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-WAR-003:

- Production: P1, P5, P6, P8, P9, P10, P14, P16, P17.
- Tests: T3, T4, T5, T8.

**H. Affected files.**

- `WardrobeSearchCriteria.java`, `WardrobeResultState.java`, `WardrobeSearchPage.java`;
- `WardrobeTransactions.java`, `SpringWardrobeTransactions.java`, `SearchWardrobeService.java`;
- `WardrobeCursor.java`, `WardrobeResponse.java`, `WardrobeGarmentController.java`.

**I. Required verification.**

- Tied timestamps;
- page boundaries;
- removal before and after the cursor;
- reordered equivalent filters giving the same cursor;
- a mismatched cursor, including an `m1` cursor for unchanged conditions;
- no-match versus empty.

**J. Change impact.** `resultState` is a new response field: compatible addition, OpenAPI 0.14.0. Any later change to the matching rules or to the Vietnamese alias table needs a new policy tag (`m3`, …).

---

## OPEN-S4-15 — Search indexing evidence

**A. Question and decision.** Which indexes ship with WAR-003? Realizes **S4-WAR-04** (measured relational indexes first; trigram only on evidence).

**B. Story and requirements.** US-WAR-003 AC-01, AC-02, AC-04, AC-06.

**C. Problem and context.**

- `ix_wardrobe_garment_owner_current` already orders the owner's current garments.
- The child sets have primary keys `(garment_id, value)` that serve the `EXISTS` probes.
- There is no measured wardrobe scale.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. No new index. Ship an `EXPLAIN (ANALYZE, BUFFERS)` procedure over representative fixtures (0, 50, 500 and 5,000 garments for one owner). Add an index migration only if a filtered first page exceeds the existing browse plan's cost class: a sequential scan of other owners' rows, or more than 50 ms on the reference machine. | Nothing speculative; evidence-driven |
| 2. Add composite and trigram indexes now | Write cost and an extension dependency without evidence |

**E. Selected solution and rationale.** **Option 1.** Candidate indexes, if later justified:

- `(owner_account_id, category, created_at DESC, id DESC) WHERE removed_at IS NULL`;
- `pg_trgm` GIN on `lower(normalize(brand, NFC))`.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-WAR-04.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING`: the measurement is required before release.**

**G. Implementation steps.** US-WAR-003 §14 steps M6–M7; §16 DoD item.

**H. Affected files.** None, unless the measurement adds a forward migration (V16 or later, owner `wardrobe`).

**I. Required verification.** The recorded EXPLAIN output and timings, with safe fixture data.

**J. Change impact.** A justified index is one forward migration. It needs no code change.

---

## OPEN-S4-16 — Optional gender/body vocabulary

**A. Question and decision.** Which optional values can a user declare? Realizes **S4-PROF-01** (a confirmed bounded choice vocabulary; the delegation lets Claude select it).

**B. Story and requirements.** US-PROF-002 AC-01, AC-02, AC-04, AC-06; FR-PROF-005–007; BRULE-PROF-001; BR-012.

**C. Problem and context.**

- No vocabulary exists in the repository.
- Values must be:
  - inclusive and conservative;
  - never inferred;
  - never a garment-category gate;
  - never measurements.
- Free text was rejected by S4-PROF-01 (Option B).

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. `genderContext`: `WOMAN`, `MAN`, `NON_BINARY`, `ANOTHER_GENDER`. `bodyShape`: geometric silhouettes `RECTANGLE`, `TRIANGLE`, `INVERTED_TRIANGLE`, `HOURGLASS`, `ROUND`. Absence (`null`) means "not provided" and is the default. | Small and inclusive (non-binary and "another gender" without collecting text). Geometric shape names avoid fruit labels that carry judgment. No numbers. |
| 2. Add `PREFER_NOT_TO_SAY` | Duplicates absence (two encodings of one meaning) |
| 3. Fashion-presentation values (feminine/masculine/androgynous) instead of gender | Avoids identity data, but does not meet BR-012's "gender information" |

**E. Selected solution and rationale.** **Option 1. These are newly selected values chosen by Claude.** They are not taken from any source document and not approved by the Product Owner.

- The API returns codes; the client provides localized, respectful labels and help text.
- "Prefer not to say" is expressed by leaving the field empty or clearing it.
- No value restricts any garment category, request or validity rule (AC-04).

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-PROF-01.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING`: an inclusive-representation and privacy review of the values and their client labels is strongly advised before release.**

**G. Implementation steps.** US-PROF-002:

- Production: P1 (CHECK constraints), P2, P3.
- Tests: T1, T6, T7.

**H. Affected files.** V13, `GenderContext.java`, `BodyShape.java`. Canonical definition since 2026-11-17: [SRS §4.10.1](../../../../03-requirements/SRS.md#4101-optional-personal-context-vocabularies), with proposed Vietnamese display labels.

**I. Required verification.**

- Every value is accepted.
- Unsupported, lower-case or mixed-case values are rejected.
- All eight garment categories remain creatable with and without context.

**J. Change impact.** A value change needs:

- a new migration altering the CHECK constraint;
- the enum;
- OpenAPI;
- the client.

Existing stored values must be migrated or kept.

---

## OPEN-S4-17 — Optional-context resource, persistence and clearing

**A. Question and decision.** Where and how is optional context stored, revised and cleared? Realizes **S4-PROF-02** (a separate revisioned record), **S4-PROF-03** (presence-aware PATCH, guarded first write) and **S4-PROF-04** (no new publication).

**B. Story and requirements.** US-PROF-002 AC-02, AC-03, AC-05, AC-06; DATA-AUTH-004; NFR-PRIV-001/002.

**C. Problem and context.** Preferences have their own revision and rewrite their child sets. A missing preferences row must not imply that optional context is missing.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. `GET`/`PATCH /api/v1/personal-context/optional-context`. V13 table `personal_context_optional_context`, one row per owner with nullable `gender_context` and `body_shape` and its own revision. Clearing sets NULL and keeps the row and revision. An empty first operation writes nothing. | Independent from preferences; absence explicit; revision never resets |
| 2. Delete the row when both fields are cleared | Revision restarts; a stale client could then pass a first-write guard |

**E. Selected solution and rationale.** **Option 1.**

- PATCH body: `{expectedRevision, genderContext?, bodyShape?}`. Omitted keeps the value, `null` clears it, a string sets it.
- `expectedRevision: null` is used only when no row exists.
- `CurrentPersonalContext` is unchanged: styles and needs only.
- Values never appear in logs.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-PROF-02/03/04.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-PROF-002:

- Production: P1, P4–P19, P21.
- Tests: T2–T8.

**H. Affected files.** V13; the optional-context domain, application, persistence and web classes; `PersonalContextApiExceptionHandler.java`; `PersonalContextConfiguration.java`; `openapi.yaml`.

**I. Required verification.**

- Concurrent first writes;
- a stale revision;
- a no-op clear;
- independence from preferences;
- `CurrentPersonalContext` unchanged.

**J. Change impact.** A future consumer adds a purpose-specific published view. The table is unchanged.

---

## OPEN-S4-18 — Location representation, precision and manual resolution

**A. Question and decision.** What location is stored and sent to the provider, at which precision, and how is a typed city resolved? Realizes **S4-CTX-01** (minimal current selection; reviewed precision; manual resolution).

**B. Story and requirements.** US-CTX-001 AC-01, AC-02, AC-06, AC-07; FR-WEATHER-001/002; HW-003; SI-002.

**C. Problem and context.**

- Device coordinates can be very precise.
- The provider needs coordinates.
- A city name alone is not weather.
- The backend cannot attest a mobile permission dialog.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. The server rounds latitude and longitude to 2 decimal places (HALF_EVEN, about 1.1 km) for both sources before storing or sending them. `source` is `DEVICE` or `MANUAL`. `label` is required for `MANUAL` and optional for `DEVICE`: 1–80 code points after NFC and trimming, no control characters. Manual city entry is resolved **on the client** from a bundled reference of Vietnamese provincial cities with pre-rounded points. | Minimizes precision; no backend geocoding dependency; one representation |
| 2. A backend geocoding proxy | A second external dependency with unverified terms |
| 3. City reference table on the server | Needs a curated dataset and its own maintenance |

**E. Selected solution and rationale.** **Option 1.**

- The raw precision of the request is discarded before persistence.
- The backend accepts any valid rounded point for `MANUAL`, so the client's reference can grow without a backend change.
- Skipping means never PATCHing; clearing means `selection: null`. Neither calls the provider.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-CTX-01.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING`: client permission, manual and skip evidence, and a privacy review of 2-decimal precision.**

**G. Implementation steps.** US-CTX-001:

- Production: P1, P2, P3, P15.
- Tests: T1, T12.

**H. Affected files.** V14, `LocationSource.java`, `LocationSelection.java`, `SubmittedLocation.java`.

**I. Required verification.**

- Rounding at the boundaries: −90, 90, −180, 180 and .005.
- Non-finite values are rejected.
- Label rules.

**J. Change impact.** Changing the precision changes:

- the `NUMERIC` scale (a new migration);
- the rounding constant;
- the privacy review.

---

## OPEN-S4-19 — Location revision, clearing and evidence binding

**A. Question and decision.** How do location changes, clearing, failed weather and late responses interact? Realizes **S4-CTX-02** (independent revision; evidence bound to it; commit first, acquire outside the transaction; conditional publication).

**B. Story and requirements.** US-CTX-001 AC-01, AC-02, AC-05, AC-06, AC-07; FR-WEATHER-001–005; COM-003.

**C. Problem and context.**

- A weather failure must not roll back an accepted selection.
- A late response for an old selection must not become current.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. V14 `personal_context_location` (one row per owner: nullable point, label, source; all or nothing; own revision) and `personal_context_weather_evidence` (latest only, with `location_revision`). PATCH `{expectedRevision, selection}`: omitted is a no-op, `null` clears, an object replaces. A change or clear deletes the evidence in the same transaction. After commit, acquire outside any transaction. Publish with an `INSERT … SELECT … FROM location WHERE revision = :rev AND latitude IS NOT NULL` upsert under a `FOR SHARE` lock of the location row. The upsert never replaces newer evidence of the same revision. | Coherent winner; no long transaction |
| 2. Store weather in the location row | Couples the user's revision to provider refreshes |

**E. Selected solution and rationale.** **Option 1.** A provider refresh never changes the location revision. The response reports the accepted selection and the environment status separately.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-CTX-02.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-CTX-001:

- Production: P1, P4, P14, P16–P22, P25–P28.
- Tests: T9–T13.

**H. Affected files.** V14; `LocationProfile.java`, `LocationRepository.java`, `ReviseLocationService.java`, `EnvironmentalContextService.java`, `JdbcLocationRepository.java`; the web classes.

**I. Required verification.** `ConcurrentLocationIT`:

- a late result after a change;
- a late result after a clear;
- an older reading after a newer one;
- refresh against clear.

**J. Change impact.** Adding location history would need a new retention policy. It is excluded.

---

## OPEN-S4-20 — Open-Meteo request and semantic mapping

**A. Question and decision.** Which provider request is made, and how is its response translated? Realizes **S4-CTX-03** (Open-Meteo first; terms, attribution and eligibility unverified).

**B. Story and requirements.** US-CTX-001 AC-02, AC-03, AC-04, AC-07; SI-002; FR-WEATHER-002–005; ERR-WEATHER-001.

**C. Problem and context.** The adapter must send only the location and must not hide a stub behind the port.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. `GET {base-url}/v1/forecast`. Parameters:<br>• `latitude`, `longitude` (rounded);<br>• `current=temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code,wind_speed_10m`;<br>• `timezone=GMT`, `timeformat=unixtime`.<br>Translation:<br>• `current.time` (UTC seconds) becomes the provider evidence time;<br>• `weather_code` (WMO) maps to `WeatherCondition`;<br>• units must be the defaults (°C, %, mm, km/h), otherwise the response is invalid;<br>• the basis is `MODELLED`, never "observed";<br>• attribution text is configured. | Small integration; keyless; explicit basis |
| 2. OpenWeather with an API key | Account and secret management. The plan's fallback if Open-Meteo terms do not fit. |

**E. Selected solution and rationale.** **Option 1.**

- The response is rejected (`INVALID_PROVIDER_DATA`) when:
  - `current` is missing;
  - a value is non-finite;
  - a unit is unexpected;
  - the reported grid point is more than 0.5° from the request in latitude or longitude.
- The raw payload is discarded after translation.
- The request sends no account, wardrobe or profile data. No API key is sent.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-CTX-03.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING`:**
  - Open-Meteo terms of use, attribution and eligibility for the actual deployment are **not verified**;
  - the request and response shape are taken from the provider's public documentation and **must be confirmed by a real authorized request**;
  - provider integration is **not claimed working**.

**G. Implementation steps.** US-CTX-001:

- Production: P5, P6, P23, P24.
- Tests: T3, T4.

§14 step M10 is the real request.

**H. Affected files.** `WeatherCondition.java`, `WeatherReading.java`, `OpenMeteoWeatherProvider.java`, `OpenMeteoResponseTranslator.java`.

**I. Required verification.**

- Translator tests over documented sample shapes.
- One real request in the agreed environment, recorded without secrets.
- Terms review by the PO and operations.

**J. Change impact.** Switching providers replaces the adapter and translator only. The port and the application are unchanged.

---

## OPEN-S4-21 — Weather freshness and latest evidence

**A. Question and decision.** Which time is aged, where is the boundary, and what is stored? Realizes **S4-CTX-04** (latest evidence only; refresh on demand; acquisition time distinct from evidence time; inclusive 30 minutes).

**B. Story and requirements.** US-CTX-001 AC-02, AC-03; FR-WEATHER-003; BRULE-OUT-006.

**C. Problem and context.** A new fetch can carry an old model time, and clocks can skew.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Age = `now − observedAt`, where `observedAt` is the provider's `current.time`.<br>• Current when `0 ≤ age ≤ PT30M`, inclusive.<br>• `observedAt` may be up to `PT5M` in the future, to allow for clock skew; beyond that the reading is invalid.<br>• `retrievedAt` is stored separately and never used for freshness.<br>• A newly fetched but stale reading is `UNAVAILABLE` (`STALE_PROVIDER_DATA`) and is not stored. | Truthful; deterministic with `Clock` |
| 2. Age by `retrievedAt` | Rejuvenates old data. Forbidden. |

**E. Selected solution and rationale.** **Option 1.** Only one evidence row exists per owner. Stale stored evidence is never shown as current; this design does not show stale weather as historical either.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-CTX-04.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-CTX-001:

- Production: P7, P19.
- Tests: T2, T10, T12.

**H. Affected files.** `WeatherFreshness.java`, `EnvironmentalContextService.java`.

**I. Required verification.** Exactly 30:00 is current; 30:00.000001 is not; a future reading beyond 5 minutes is invalid; a stale fetch is not stored.

**J. Change impact.** The boundary is approved (30 minutes) and is not a delegated choice. The skew tolerance is a constant.

---

## OPEN-S4-22 — One elapsed acquisition deadline

**A. Question and decision.** How is the 2-second boundary enforced end to end? Realizes **S4-CTX-05** (synchronous bounded adapter; one elapsed deadline; explicit unavailable outcome).

**B. Story and requirements.** US-CTX-001 AC-03, AC-04; FR-WEATHER-004/005; ERR-WEATHER-001.

**C. Problem and context.**

- A connect timeout plus a read timeout is not a total deadline.
- Queue wait, TLS, the body and parsing all count.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. The deadline starts at adapter entry. Steps:<br>• wait for one of `maximum-concurrent-requests` permits (8) for at most the remaining time;<br>• submit the Spring `RestClient` call (over `JdkClientHttpRequestFactory`, with connect and read timeouts both set to the deadline as a backstop) to a bounded executor;<br>• wait `Future.get(remaining)`;<br>• on timeout, `cancel(true)` and return `DEADLINE_EXCEEDED`;<br>• read the body through a 64 KiB limit and translate inside the task.<br>No retry. | Matches S4-CTX-05 Option A; bounded threads and memory |
| 2. JDK async client | S4-CTX-05 Option B, not selected |

**E. Selected solution and rationale.** **Option 1.**

- The deadline is configured at `PT2S`. Startup fails if it is above `PT2S` or not positive.
- Late results are discarded.
- Whether interrupting a cancelled task closes the underlying connection promptly depends on the JDK HTTP client. This must be measured (I); the design only relies on the caller returning in time and on bounded threads.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-CTX-05.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING`: measured elapsed behavior of the concrete client.**

**G. Implementation steps.** US-CTX-001:

- Production: P23, P29, P30.
- Tests: T4 (slow headers, slow body, capacity exhausted, oversized body, malformed body).

**H. Affected files.** `OpenMeteoWeatherProvider.java`, `PersonalContextWeatherProperties.java`, `PersonalContextLocationConfiguration.java`.

**I. Required verification.** `OpenMeteoWeatherProviderTest` against a local HTTP server with controlled delays. Each call returns within the deadline plus a small scheduling tolerance (`PT0.5S`, a test tolerance only). Measure in the agreed environment.

**J. Change impact.** Switching to an async client replaces the adapter internals only.

---

## OPEN-S4-23 — Environmental basis and absent consumers

**A. Question and decision.** How is the current or unavailable basis exposed now, before any advice consumer exists? Realizes **S4-CTX-06** (current/unavailable facts; basis comparison; no invalidation infrastructure).

**B. Story and requirements.** US-CTX-001 AC-02–AC-05, AC-07; COM-003; BRULE-OUT-006.

**C. Problem and context.**

- No Outfit Advice or Wardrobe Intelligence code exists.
- S4-CTX-06 says to publish "when a boundary needs it".

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. The application type `EnvironmentalContext` (no selection, current, or unavailable, with the location revision and evidence time) is the basis. The HTTP response carries it. No new `personalcontext.api` type until a consumer exists. | No speculative contract; the basis is already well defined for a later publication |
| 2. Publish `CurrentEnvironmentalContext` now | No consumer; contradicts the least-purpose reasoning shared with S4-PROF-04 |

**E. Selected solution and rationale.** **Option 1.** The AC-04 selective filtering and AC-05 reevaluation by consumers are **not demonstrated**. They are recorded as an evidence gap in the guide's §15 and §16.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-CTX-06.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-CTX-001:

- Production: P9, P10, P26.
- Tests: T12.

**H. Affected files.** `EnvironmentalContext.java`, `LocationContextView.java`, `LocationContextResponse.java`.

**I. Required verification.** HTTP tests for each basis state.

**J. Change impact.** A future consumer adds a published API translating `EnvironmentalContext`. The tables are unchanged.

---

## OPEN-S4-24 — Weather privacy, configuration and test isolation

**A. Question and decision.** How are the provider destination, logs and tests configured safely? Realizes **S4-CTX-01**, **S4-CTX-03** and **S4-CTX-05**.

**B. Story and requirements.** US-CTX-001 AC-06, AC-07.

**C. Problem and context.** Coordinates are sensitive, and request URLs can leak into logs. CI must not depend on public network availability.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. `base-url` comes from configuration only. It must be `https`, unless the host is `127.0.0.1` or `localhost`, which only tests use. Never log the URL, the query or coordinates; log only the outcome category. Integration tests replace `WeatherProvider` with a `@Primary ControllableWeatherProvider`; the adapter is tested against a local JDK `HttpServer`. | Safe defaults; reproducible CI |
| 2. Live provider in CI | Flaky; terms unverified |

**E. Selected solution and rationale.** **Option 1.**

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-CTX-001:

- Production: P23, P29, P30, P31.
- Tests: T4, T5, T6, T7, T15 (`PersonalContextWeatherPropertiesTest`).

**H. Affected files.** `OpenMeteoWeatherProvider.java`, `PersonalContextWeatherProperties.java`, `PersonalContextLocationConfiguration.java`, `application.properties`, `IdentityTestConfiguration.java`, `application-test.properties`.

**I. Required verification.**

- A startup-validation test for the base URL and the deadline (US-CTX-001 T15).
- Log inspection in the manual check.

**J. Change impact.** A keyed provider would add a secret property, read from the environment and never committed.

---

## OPEN-S4-25 — Bulk admission limits and retention

**A. Question and decision.** What numerical admission and lifecycle policy applies? Realizes **S4-BULK-06** (a composite policy; the delegation lets Claude choose the values) and resolves the MVP-Q08 limits for Sprint 4.

**B. Story and requirements.** US-BULK-001 AC-01, AC-02, AC-05, AC-06, AC-07; DATA-BULK-001; MVP-Q08.

**C. Problem and context.**

- Each photo is limited to 15,000,000 bytes, with a `PT24H` pending TTL. Both are unchanged and approved.
- Nothing bounds queued work in total or limits draft retention.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Composite policy:<br>• `maximum-items-per-batch` = **20**;<br>• `maximum-active-batches` = **3** per owner;<br>• aggregate original bytes per batch ≤ **300,000,000**, derived as 20 × 15,000,000 and enforced through the item cap plus the unchanged per-photo rule;<br>• batch lifetime **`P7D`** (after it, no new item work);<br>• **no server-side draft profiles**: the client keeps unconfirmed edits, the server keeps identifiers and states only. | Bounded load; minimal sensitive storage; resumable within a week |
| 2. Count-only limit (S4-BULK-06 Option A) | Rejected by the approved decision |

**E. Selected solution and rationale.** **Option 1.**

- The values are configuration properties.
- Expired photos need reselection.
- Batch and item rows hold no personal profile data. They are kept as minimal action identity while the account exists; a purge is deferred to account-lifecycle work.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes the approved S4-BULK-06.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`. Product Owner review of the four values is advised.

**G. Implementation steps.** US-BULK-001:

- Production: P1, P6, P19, P31, P32, P33.
- Tests: T1, T2, T7, T8, T9.

**H. Affected files.** V15, `BulkImportPolicy.java`, `BulkImportService.java`, `WardrobeBulkImportProperties.java`, `WardrobeBulkImportConfiguration.java`, `application.properties`.

**I. Required verification.**

- A 21st item and a 4th active batch are refused.
- An expired batch is refused.
- Resource behavior is checked with representative photos in the agreed environment.

**J. Change impact.** Values change through configuration only. Adding drafts would need a new table and retention rules.

---

## OPEN-S4-26 — Batch and item states

**A. Question and decision.** Which states are persisted and which are derived? Realizes **S4-BULK-02** (minimal durable model; acceptance never overwritten by uncertainty).

**B. Story and requirements.** US-BULK-001 AC-01, AC-02, AC-05.

**C. Problem and context.**

- "Uncertain" is the observer's knowledge, not a server state.
- A photo's READY or expired state lives in `wardrobe_photo_upload`.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Persisted item status:<br>• `AWAITING_PHOTO`;<br>• `REVIEWABLE` (READY stage linked);<br>• `ACCEPTED` (garment linked);<br>• `CANCELED`.<br>Derived item `photoState` on read: `NONE`, `READY` or `EXPIRED`, computed by joining the linked stage. Linking requires a READY stage, so a RECEIVING stage is never linked; `EXPIRED` covers every stage that can no longer be used for the item (expired, cancelled, attached elsewhere or purged). Persisted batch status `OPEN` or `STOPPED`; derived `EXPIRED` when `expires_at ≤ now`. | Truthful; no generic FAILED or UNCERTAIN overwrite |
| 2. Persist a FAILED status | A failure is per request (an error response), not a durable state |

**E. Selected solution and rationale.** **Option 1.**

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-BULK-001:

- Production: P1–P5, P21, P27.
- Tests: T1, T7, T8.

**H. Affected files.** V15; `BulkImportItemStatus.java`, `BulkImportBatchStatus.java`, `BulkImportItem.java`, `BulkImportBatch.java`; `JdbcBulkImportRepository.java`; `BulkImportResponse.java`.

**I. Required verification.** The state machine in the schema CHECKs and HTTP views.

**J. Change impact.** A new persisted state needs a migration that alters the CHECK constraint.

---

## OPEN-S4-27 — Batch and item identity and idempotency

**A. Question and decision.** How are retries of batch creation and item actions recognized? Realizes **S4-BULK-05** (stable item identities, per-item acceptance keys independent of order) and **S4-INT-02**.

**B. Story and requirements.** US-BULK-001 AC-05, AC-06, AC-07; FR-BULK-005/006; DATA-INT-004.

**C. Problem and context.** Positions can change. Identical photos can be separate intents. A lost response must not create a new key.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Batch creation uses the `Idempotency-Key` header and a fingerprint of `itemCount`; a replay returns the same batch, a changed count gives `422`. The server generates item ids and per-item **acceptance keys** at batch creation. Each acceptance key is used as the garment creation key, so the existing fingerprint replay applies. Item commands carry the item `expectedRevision`. | Survives reordering and lost responses; one garment per item |
| 2. Client-generated item keys | More client burden; no benefit |

**E. Selected solution and rationale.** **Option 1.**

- Replaying an accepted item returns its current garment link. A removed garment shows `ACCEPTED` with `garmentCurrent: false`; nothing is recreated.
- A different payload for an already accepted item gives `409 BULK_IMPORT_CLOSED`: the item's outcome is final, and the client never sent a key it could reuse. A changed `itemCount` under a batch `Idempotency-Key` gives `422 IDEMPOTENCY_KEY_REUSED`.
- The acceptance fingerprint covers the reviewed values only, because the item fixes the photo. A replay after US-GAR-005 removed the garment and purged its photo is therefore still recognized.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-BULK-05 and S4-INT-02.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-BULK-001:

- Production: P1, P7, P8, P19, P20, P21.
- Tests: T8, T9.

**H. Affected files.** V15, `GarmentAcceptance.java`, `AddGarmentService.java`, `BulkImportService.java`, `AcceptBulkImportItemService.java`, `JdbcBulkImportRepository.java`.

**I. Required verification.**

- A repeated acceptance;
- a changed-payload acceptance;
- a concurrent double acceptance;
- the same image on two items;
- replay after the garment was removed.

**J. Change impact.** Exposing acceptance keys to clients would be an OpenAPI change only.

---

## OPEN-S4-28 — Batch stop, item cancellation and lock order

**A. Question and decision.** How do stop and cancel serialize with acceptance? Realizes **S4-BULK-07** (authoritative stop preserving accepted items) and **S4-INT-01** (batch/item locks first).

**B. Story and requirements.** US-BULK-001 AC-02, AC-05, AC-06, AC-07.

**C. Problem and context.**

- Acceptance and stop can race.
- Cancellation must never delete an accepted item's media.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Stop takes the batch row `FOR UPDATE`, then each unaccepted item, cancelling the item and its unclaimed stage. Item commands (link, accept, cancel) take the batch row `FOR SHARE`, then the item `FOR UPDATE`, then the photo, then insert the garment. | Stop and acceptance meet at the batch row; acceptances run in parallel with each other; canonical order |
| 2. Item-only cancellation (S4-BULK-07 Option A) | Not selected |

**E. Selected solution and rationale.** **Option 1.**

- Either an acceptance commits first and stays `ACCEPTED`, or stop wins and the acceptance then sees `STOPPED` and answers `409 BULK_IMPORT_CLOSED`.
- Stop is idempotent.
- A stage is cancelled through the existing `PhotoUploadRepository.cancel`, which only affects RECEIVING and READY rows.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-BULK-07 and S4-INT-01.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-BULK-001:

- Production: P19, P20, P21.
- Tests: T9.

**H. Affected files.** `BulkImportService.java`, `AcceptBulkImportItemService.java`, `JdbcBulkImportRepository.java`.

**I. Required verification.** `ConcurrentBulkImportIT`: stop against acceptance, giving exactly one coherent winner, and no deadlock.

**J. Change impact.** None for single-item flows.

---

## OPEN-S4-29 — Shared acceptance seam and per-item transaction

**A. Question and decision.** How does bulk reuse creation without committing a garment apart from its item? Realizes **S4-BULK-01** (Wardrobe coordination; focused shared acceptance operation) and **S4-BULK-04** (per-item atomic acceptance).

**B. Story and requirements.** US-BULK-001 AC-03–AC-07; FR-BULK-003–006.

**C. Problem and context.** `AddGarmentService` opens its own transaction.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Extract `GarmentAcceptance.acceptInCurrentTransaction(owner, entry, fingerprint, now)` from `AddGarmentService`, covering the imageless and photo-backed paths and replay. `AddGarmentService` wraps it in a transaction, so its behavior is unchanged. Bulk acceptance calls it inside its own transaction after locking the batch and item, then marks the item `ACCEPTED` and clears its stage link. | One rule set; atomic item, garment and photo |
| 2. Nest `AddGarmentService` calls | Relies on transaction propagation of a separate template (S4-BULK-01 Option B), not selected |

**E. Selected solution and rationale.** **Option 1.**

- The item's photo is taken from the item row, never from the request body. This prevents cross-item association.
- After acceptance the item no longer references the stage; the garment does.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-BULK-01 and S4-BULK-04.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.** US-BULK-001:

- Production: P7, P8, P20.
- Tests: T3 (`GarmentAcceptanceTest`), T8, T9, plus the unchanged `AddGarmentServiceTest` and `PhotoBackedGarmentCreationTest`.

**H. Affected files.** `GarmentAcceptance.java`, `AddGarmentService.java`, `AcceptBulkImportItemService.java`.

**I. Required verification.**

- A rollback after each effect.
- An invalid middle item.
- The existing creation suites stay green.

**J. Change impact.** None for the single-item API.

---

## OPEN-S4-30 — Sequential processing, expired photos and recovery

**A. Question and decision.** How are photos staged, how does expiry show, and how does a client resume? Realizes **S4-BULK-03** (sequential staging) and **S4-BULK-06** (resumption).

**B. Story and requirements.** US-BULK-001 AC-01, AC-02, AC-05, AC-07.

**C. Problem and context.**

- The existing staging endpoint already validates and normalizes one photo.
- The decoder has a global ceiling of 2.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. The client uploads each photo sequentially to the existing `POST /photo-uploads`, then links the returned id to its item. An expired stage shows `photoState: EXPIRED` and needs reselection; accepted items are unaffected. Recovery: `GET /bulk-imports` (open batches) and `GET /bulk-imports/{id}`. No server worker, no AI. | Reuses proven staging; simple recovery |
| 2. Multipart batch upload | Large requests; parallel decoding (S4-BULK-03 Option B) |

**E. Selected solution and rationale.** **Option 1.**

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- **`VERIFICATION PENDING` (client multiphoto and resume evidence).**

**G. Implementation steps.** US-BULK-001:

- Production: P19, P29.
- Tests: T8.

**H. Affected files.** `BulkImportService.java`, `WardrobeBulkImportController.java`.

**I. Required verification.**

- An expired stage shows `EXPIRED` and refuses acceptance.
- Relinking a new stage works.
- Client evidence.

**J. Change impact.** Bounded parallelism would be a client change plus admission review.

---

## OPEN-S4-31 — Migrations, OpenAPI versions, CI and shared-file order

**A. Question and decision.** How are numbering and shared files coordinated across the six guides? Realizes **S4-INT-03**.

**B. Story and requirements.** All six stories; DoD; AR-01–08.

**C. Problem and context.**

- Six guides change the same controller, handler, configuration, OpenAPI and CI files.
- Migrations must be unique.
- The CI trigger lacks `sprint_04`.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Fixed order GAR-005 → GAR-006 → WAR-003 → PROF-002 → CTX-001 → BULK-001.<br>Migrations: V12 (GAR-005, `wardrobe`), V13 (PROF-002, `personal_context`), V14 (CTX-001, `personal_context`), V15 (BULK-001, `wardrobe`). GAR-006 and WAR-003 add none.<br>OpenAPI 0.12.0–0.17.0, one minor version per story.<br>GAR-005 adds `sprint_04` to the PR trigger; each guide appends its report entries.<br>Every BEFORE anchor is the text after the previous guide. | Unique numbers; reviewable cumulative diffs |
| 2. Numbers assigned at merge time | Anchors cannot be exact |

**E. Selected solution and rationale.** **Option 1.** See [shared architecture §3](shared-architecture.md#3-cumulative-delivery-order-and-shared-files) for the file-by-file order.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`; realizes S4-INT-03.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.**

| Guide | OpenAPI step | CI step |
| --- | --- | --- |
| GAR-005 | P22 | P23 |
| GAR-006 | P12 | P13 |
| WAR-003 | P20 | P21 |
| PROF-002 | P21 | P22 |
| CTX-001 | P32 | P33 |
| BULK-001 | P34 | P35 |

**H. Affected files.** `openapi.yaml`, `backend-ci.yml`, plus the shared Java files listed in the shared architecture.

**I. Required verification.**

- `PersistenceOwnershipSqlTest`: one owner schema per migration.
- `DatabaseSchemaOwnershipIT`.
- `IdentityOpenApiContractIT`.
- CI on a PR to `sprint_04`.

**J. Change impact.** A guide applied out of order must renumber its migration and rebase its anchors.

---

## OPEN-S4-32 — Error vocabulary additions

**A. Question and decision.** Which stable codes are added? Realizes **S4-INT-02** and **S4-INT-03** (non-disclosing errors, current-state guidance).

**B. Story and requirements.** All six stories.

**C. Problem and context.**

- Existing codes must keep their meaning.
- The 409 and 503 texts mention "edit" only.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Add only what has no existing equivalent, and make the shared 409 and 503 texts operation-neutral. | Small vocabulary; consistent |

**E. Selected solution and rationale.** **Option 1.** Additions:

| Code or field code | Status | Story |
| --- | --- | --- |
| `GARMENT_ALREADY_HAS_PHOTO` | 409 | GAR-006 |
| cursor field code `CONDITIONS_CHANGED`, plus `q`, `category` and `color` field errors | 400 | WAR-003 |
| `BULK_IMPORT_NOT_FOUND` | 404 | BULK-001 |
| `BULK_IMPORT_CLOSED` | 409 | BULK-001 |
| `BULK_IMPORT_LIMIT_REACHED` | 409 | BULK-001 |
| `BULK_IMPORT_ITEM_NOT_READY` | 409 | BULK-001 |

Reused codes:

- `REVISION_CONFLICT`, `GARMENT_NOT_FOUND`, `GARMENT_NO_LONGER_CURRENT`;
- `PHOTO_UPLOAD_*`, `IDEMPOTENCY_KEY_REUSED`, `VALIDATION_FAILED`;
- `TEMPORARILY_UNAVAILABLE`.

BULK-001 reuses `REVISION_CONFLICT` for a stale item revision, with item wording. Its P30 makes the `IDEMPOTENCY_KEY_REUSED` text neutral ("…used with different values…"), because a batch start also carries an `Idempotency-Key`.

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.**

- GAR-005: P16.
- GAR-006: P10.
- WAR-003: P13.
- PROF-002: P18.
- CTX-001: P28.
- BULK-001: P30.

**H. Affected files.** `WardrobeApiExceptionHandler.java`, `PersonalContextApiExceptionHandler.java`, `openapi.yaml`.

**I. Required verification.** HTTP tests assert each code.

**J. Change impact.** Codes are contract: renaming one needs an OpenAPI version change.

---

## OPEN-S4-33 — Configuration properties and test profile

**A. Question and decision.** Which new properties exist, and which values do tests use? Realizes **S4-GAR-03**, **S4-CTX-05** and **S4-BULK-06**.

**B. Story and requirements.** GAR-005, CTX-001, BULK-001.

**C. Problem and context.** Values must be visible, validated at startup and safe in tests.

**D. Options.**

| Option | Trade-off |
| --- | --- |
| 1. Typed `@ConfigurationProperties` records with startup validation. Defaults in `application.properties`. Test overrides: removal-cleanup scheduler disabled; weather provider replaced by a test double. | Explicit; fails fast |

**E. Selected solution and rationale.** **Option 1.**

| Property | Default | Test |
| --- | --- | --- |
| `capsuleai.wardrobe.removal-cleanup.enabled` | `true` | `false` |
| `capsuleai.wardrobe.removal-cleanup.initial-delay` / `interval` | `PT2M` / `PT5M` | — |
| `capsuleai.wardrobe.removal-cleanup.batch-size` / `retry-delay` | `50` / `PT15M` | — |
| `capsuleai.wardrobe.removal-cleanup.purge-limit` / `overdue-warning-age` | `P30D` / `P21D` | — |
| `capsuleai.personal-context.weather.enabled` | `true` | `false` (the `@Primary ControllableWeatherProvider` answers) |
| `capsuleai.personal-context.weather.base-url` | `https://api.open-meteo.com` | — (the adapter is not created) |
| `capsuleai.personal-context.weather.acquisition-deadline` | `PT2S` (validated ≤ `PT2S`) | — |
| `capsuleai.personal-context.weather.maximum-concurrent-requests` / `maximum-response-bytes` | `8` / `65536` | — |
| `capsuleai.personal-context.weather.attribution` | `Weather data by Open-Meteo.com` | — |
| `capsuleai.wardrobe.bulk-import.maximum-items-per-batch` / `maximum-active-batches` / `batch-lifetime` | `20` / `3` / `P7D` | — |

**F. Authority and status.**

- `SELECTED BY CLAUDE — DELEGATED DEFAULT`.
- `IMPLEMENTATION PENDING`.
- `VERIFICATION PENDING`.

**G. Implementation steps.**

- GAR-005: P18–P21, T5.
- CTX-001: P29–P31, T7, T15.
- BULK-001: P31–P33.

**H. Affected files.** `application.properties`, `application-test.properties`, the three properties records.

**I. Required verification.** Startup with the defaults; failure with an invalid deadline or a non-HTTPS URL (US-CTX-001 T15).

**J. Change impact.** Configuration only.

---

## 4. Items that are not open questions

| Item | Status | Why it is not delegated |
| --- | --- | --- |
| 15,000,000-byte photo limit, `PT24H` pending TTL, 2048-pixel normalized long edge | Approved in Sprint 3 (OPEN-S3-07/09/10) | Reused unchanged; no guide overrides them |
| Inclusive 30-minute freshness and 2-second acquisition budget | Approved product constraints (plan §4) | Implemented as given; only the mechanics are delegated (OPEN-S4-21/22) |
| Open-Meteo terms, attribution and eligibility | **Unverified** | External legal and provider facts cannot be decided by delegation |
| Bucket versioning, object lock, replication and backups | **Unverified** | Infrastructure facts (OPEN-S4-05) |
| Client (mobile) evidence for all six stories | Outstanding | Not backend work |
| Real advice, Coverage, Multiplier and history consumers for GAR-005 AC-02/03/04, PROF-002 AC-03/04 and CTX-001 AC-04/05 | Absent | Recorded as evidence gaps; no engine is added |
| Formal GAR-006 story and PBI mapping | Outstanding | Product artifact owned by the PO and BA |
