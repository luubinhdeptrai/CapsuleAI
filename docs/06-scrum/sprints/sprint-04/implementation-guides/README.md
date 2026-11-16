# Sprint 4 — Backend Implementation Guides

This package tells a backend developer exactly how to implement the six selected Sprint 4 stories:

- the complete production code, tests, migrations, OpenAPI and CI changes, in delivery order;
- the verbatim requirements;
- every decision behind the code.

**It is documentation only. No source file, test, migration, OpenAPI file or workflow in the repository was changed to produce it.**

## 1. Sprint Goal

From the [Sprint 4 plan](../sprint-plan.md#2-sprint-goal):

> **Enable owners to maintain a trustworthy, findable wardrobe and control their personal and environmental context, using the existing private-photo and confirmed-garment foundations, with safe removal, attachment and bulk-entry recovery.**

## 2. Stories

| Order | Story | Guide | Migration | OpenAPI | CI entries after |
| --- | --- | --- | --- | --- | --- |
| 1 | US-GAR-005 — Remove a garment while preserving minimal history | [US-GAR-005-guide.md](US-GAR-005-guide.md) | V12 (`wardrobe`) | 0.12.0 | 102 |
| 2 | US-GAR-006 (provisional) — Attach a photo to an existing garment | [US-GAR-006-guide.md](US-GAR-006-guide.md) | — | 0.13.0 | 106 |
| 3 | US-WAR-003 — Find owned garments through search and filters | [US-WAR-003-guide.md](US-WAR-003-guide.md) | — | 0.14.0 | 115 |
| 4 | US-PROF-002 — Control optional body and gender context | [US-PROF-002-guide.md](US-PROF-002-guide.md) | V13 (`personal_context`) | 0.15.0 | 121 |
| 5 | US-CTX-001 — Choose environmental context with safe fallback | [US-CTX-001-guide.md](US-CTX-001-guide.md) | V14 (`personal_context`) | 0.16.0 | 130 |
| 6 | US-BULK-001 — Review and confirm a bulk garment-photo import | [US-BULK-001-guide.md](US-BULK-001-guide.md) | V15 (`wardrobe`) | 0.17.0 | 137 |

The CI baseline before Sprint 4 is 95 required report entries.

## 3. Package Contents

Exactly nine files:

| File | Purpose |
| --- | --- |
| `README.md` | This overview |
| [shared-architecture.md](shared-architecture.md) | Cross-story rules: shared-file order, lock order, identity and recovery, errors, transport, migrations, OpenAPI, configuration, testing, SQL, privacy, naming, external obligations |
| [open-questions.md](open-questions.md) | The record of the 33 delegated detailed decisions (OPEN-S4-01 to OPEN-S4-33) |
| `US-GAR-005-guide.md` … `US-BULK-001-guide.md` | One guide per story, each with the same 18 sections (below) |

Each story guide has these sections:

1. Story Overview
2. Repository Baseline
3. Business Requirements — Verbatim
4. Product Requirements — Verbatim
5. Software Requirements — Verbatim
6. Business Rules — Verbatim
7. User Story and Acceptance Criteria — Verbatim
8. Use Cases, Flows and Architecture Decisions — Verbatim
9. Engineering Interpretation
10. Just-in-Time Detailed Design
11. File Change Matrix
12. Complete Production Implementation
13. Complete Automated Test Implementation
14. Manual Verification
15. Acceptance Criteria and Requirements-to-Test Matrices
16. Definition of Done
17. Troubleshooting
18. Final Checklist

## 4. Verified Repository Baseline

The guides are written against branch `sprint_04` at commit `33c579ce52008da8fd60ade41ce743ea6b5e002c` (merge of PR #5, Sprint 3). Its state:

- **Stack:** Java 21, Spring Boot 4.1.1 (Spring Framework 7, Spring Security 7), `JdbcClient` without JPA, Flyway, PostgreSQL 17 (Testcontainers `postgres:17.11-bookworm`), Jackson 3 (`tools.jackson`), JUnit 5, AssertJ, ArchUnit, MockMvc and the Maven Wrapper.
- **Modules:** Identity, Wardrobe and Personal Context, each in its own schema (AR-01–08). `PersistenceOwnershipSqlTest` and `DatabaseSchemaOwnershipIT` enforce this.
- **Migrations:** V1–V11.
- **OpenAPI:** 0.11.0 at `docs/05-api/openapi.yaml`, checked against the served API by `IdentityOpenApiContractIT`.
- **CI:** `.github/workflows/backend-ci.yml`, with 95 required report entries and pull-request branches `main`, `sprint_02` and `sprint_03`.

[shared-architecture.md §2](shared-architecture.md#2-verified-repository-baseline) has the details.

## 5. Decisions

**Approved by the Product Owner on 2026-11-16: all 30 Sprint 4 decisions.**

- Story decisions: S4-GAR-01–06, S4-WAR-01–04, S4-PROF-01–04, S4-CTX-01–06 and S4-BULK-01–07.
- Integration decisions: S4-INT-01–03.

The approval is binding. The plan's "PROPOSED — PENDING PO APPROVAL" cells are historical; each guide quotes them verbatim with a note.

**Delegated to Claude: 33 detailed decisions, OPEN-S4-01 to OPEN-S4-33.** Each is labeled `SELECTED BY CLAUDE — DELEGATED DEFAULT`, `IMPLEMENTATION PENDING` and `VERIFICATION PENDING` in [open-questions.md](open-questions.md). **None is Product Owner–approved.** The defaults most worth a review are:

| Area | Default | Record |
| --- | --- | --- |
| Removal command and outcomes | `POST …/garments/{id}/removal` → `REMOVED` / `ALREADY_REMOVED`; replay of a removed creation → `410` | OPEN-S4-01, 03 |
| Removal cleanup | Every `PT5M`, batch 50, retry `PT15M`, physical deletion within `P30D`, warning at `P21D` | OPEN-S4-04, 05 |
| Search | `q` of 1–100 code points over labels, codes and Brand; filtered cursor bound to its conditions (`CONDITIONS_CHANGED`) | OPEN-S4-10–14 |
| Optional context vocabularies | `GenderContext` and `BodyShape` codes: **newly selected; an inclusive-representation review is advised** | OPEN-S4-16 |
| Location and weather | Rounded to 2 decimals; Open-Meteo `current` data; inclusive 30 minutes on the provider's evidence time; one `PT2S` deadline, 8 concurrent requests, 64 KiB | OPEN-S4-18–24 |
| Bulk limits | 20 items, 3 active batches, `P7D`, derived 300,000,000 bytes, no drafts: **Product Owner review advised** | OPEN-S4-25 |

## 6. Implementation Sequence

Apply the guides **in this order**. Every BEFORE anchor is the file as left by the previous guide (OPEN-S4-31).

```mermaid
flowchart LR
    G5["1. US-GAR-005<br/>V12 · 0.12.0"] --> G6["2. US-GAR-006<br/>0.13.0"]
    G6 --> W3["3. US-WAR-003<br/>0.14.0"]
    W3 --> P2["4. US-PROF-002<br/>V13 · 0.15.0"]
    P2 --> C1["5. US-CTX-001<br/>V14 · 0.16.0"]
    C1 --> B1["6. US-BULK-001<br/>V15 · 0.17.0"]
```

The arrows are file order for this package, the plan's recommended sequence. They are not product dependencies: US-WAR-003, US-PROF-002 and US-CTX-001 are functionally independent of the media stories. A team that changes the order must rebase the BEFORE anchors of the shared files and renumber migrations and OpenAPI versions.

## 7. How to Use the Guides

1. Read [shared-architecture.md](shared-architecture.md) once.
2. For each story, in order:
   1. Read §1–§11.
   2. Apply §12 step by step:
      - `[ADD]` creates the file with the complete code shown;
      - `[MODIFY]` replaces each BEFORE block, which must match exactly once, with its AFTER block.
   3. Apply §13 the same way.
   4. Run the commands in §13, then the manual checks in §14.
3. Keep line endings when editing. Several Personal Context files and the `.properties` files use CRLF.
4. Commit per story. This package itself commits nothing.

**Shared files** changed by more than one guide are listed in [shared-architecture.md §3](shared-architecture.md#3-cumulative-delivery-order-and-shared-files). These are the busiest:

- the Wardrobe controller and exception handler;
- `application.properties` and `application-test.properties`;
- `openapi.yaml` and `backend-ci.yml`;
- the integration-test support classes, `GarmentSchemaIT` and `PreferencesSchemaIT`.

## 8. Prerequisites

- JDK 21 and Docker (Testcontainers) for integration tests.
- The local stack of `backend/README.md`: Compose PostgreSQL and S3Mock.
- `curl.exe` and Windows PowerShell 5.1 for the manual checks. The shared `manual-helpers.ps1` is in [US-GAR-005 §14](US-GAR-005-guide.md#14-manual-verification).
- Never use real credentials, personal photos or real user data in manual checks. Never run `flyway clean` against a shared database.

## 9. Migrations and API

| Version | Story | Schema | Content |
| --- | --- | --- | --- |
| V12 | GAR-005 | `wardrobe` | `minimized_at` and its CHECK; `wardrobe_garment_removal_cleanup` |
| V13 | PROF-002 | `personal_context` | `personal_context_optional_context` |
| V14 | CTX-001 | `personal_context` | `personal_context_location`, `personal_context_weather_evidence` |
| V15 | BULK-001 | `wardrobe` | `wardrobe_bulk_import_batch`, `wardrobe_bulk_import_item` |

New and changed operations:

| Operation | Story |
| --- | --- |
| `POST /api/v1/wardrobe/garments/{garmentId}/removal` | GAR-005 |
| `POST /api/v1/wardrobe/garments/{garmentId}/photo-attachment` | GAR-006 |
| `GET /api/v1/wardrobe/garments` gains `q`, `category` and `color`, plus `resultState` and `conditions` | WAR-003 |
| `GET`/`PATCH /api/v1/personal-context/optional-context` | PROF-002 |
| `GET`/`PATCH /api/v1/personal-context/location` | CTX-001 |
| `POST`/`GET /api/v1/wardrobe/bulk-imports`, `GET …/{batchId}`, `POST …/items/{itemId}/photo`, `…/acceptance`, `…/cancellation`, `POST …/{batchId}/stop` | BULK-001 |

## 10. Verification Commands

From `backend/`:

```powershell
# One unit class
.\mvnw.cmd -q "-Dtest=GarmentRemovalTest" test

# Integration classes (the smoke test satisfies Surefire when no other unit test is selected)
.\mvnw.cmd "-Dtest=HarnessSmokeTest" "-Dit.test=GarmentRemovalApiIT,GarmentSchemaIT" verify

# Everything, as CI runs it
.\mvnw.cmd clean verify
```

After each guide, CI on a pull request to `sprint_04` must find the cumulative number of required reports listed in §2, with no failure or skip.

## 11. Status

| Item | Status |
| --- | --- |
| Repository source, tests, migrations, OpenAPI, CI | **Unchanged.** Only this directory was added. |
| Guides applied in the repository | **No.** Implementation is pending. |
| Scratch verification | **Done on 2026-11-16 in an isolated copy.** All 204 steps of the six guides applied in order with every BEFORE anchor matching exactly once. `./mvnw -o clean verify` (the same Maven Wrapper, offline, run from Git Bash) gave BUILD SUCCESS: 677 unit tests (Surefire) and 433 integration tests (Failsafe, Testcontainers PostgreSQL 17.11), with 0 failures, 0 errors and 0 skipped. Of the 137 required CI reports, 136 existed with at least their minimum count. The 137th, the Sprint 3 `HeifDecodingIT`, ran 0 tests because libheif is not installed on that machine; CI installs it. |
| Delegated decisions | `VERIFICATION PENDING` until implemented in the repository and confirmed by its CI |

**What the scratch run is, and is not.**

- **It is:** the guides' ADD and MODIFY steps applied mechanically, in order, to a copy of `backend/`, `docs/05-api` and `.github/workflows`, followed by Maven on that copy.
- **It shows** that the code and tests in the guides compile, that their BEFORE anchors match cumulatively, and that the listed tests passed there.
- **It is not** a run in the repository, a CI run or a deployment. It does not cover the external obligations below.

## 12. Real-Provider and External Obligations

None of these is satisfied by this package.

- **Open-Meteo (US-CTX-001):**
  - its terms of use, attribution and eligibility for the actual deployment are **not verified**;
  - no real request was made, and the response shape follows the public documentation (§14 M7);
  - the 2-second deadline must be measured in the agreed environment (§14 M8);
  - until the terms are cleared, production can run with `capsuleai.personal-context.weather.enabled=false`, which reports `NOT_CONFIGURED`.
- **Object storage (US-GAR-005):** that deleted photos leave no recoverable version depends on the bucket's versioning and lifecycle configuration. This is an operations check (§14 M9 of that guide).
- **Resources (US-BULK-001):** 20 representative photos per batch must be measured in the agreed environment before the Product Owner reviews the limits (§14 M11).
- **Clients:** removal confirmation, search, optional-context, location-permission, multiphoto and resume screens, uncertain-outcome review, Vietnamese text and accessibility.

## 13. Known Limitations

- US-GAR-006 is **provisional**: its acceptance criteria await formal refinement (P-AC-01–06 are working criteria).
- No outfit recommendation, Coverage, Multiplier or other result consumer exists. AC parts that need one (CTX-001 AC-04/AC-05 reevaluation, PROF-002 soft influence) are recorded as gaps, never as passes.
- No AI extraction is added: bulk review values always come from the user.
- Bulk batches are kept as minimal action identity while the account exists; a purge is deferred to account-lifecycle work.
- The PROF-002 vocabularies and the BULK-001 limits are delegated defaults awaiting review.

## 14. Final Checklist

- [ ] The six guides are applied in order, each followed by `.\mvnw.cmd clean verify` and a pull request to `sprint_04`.
- [ ] CI finds 137 required reports after the sixth guide.
- [ ] Every OPEN-S4 default is either confirmed by the team or changed through its record in `open-questions.md`.
- [ ] Open-Meteo terms reviewed. CTX-001 M7 and M8 recorded.
- [ ] Bucket versioning checked (GAR-005 M9). BULK-001 M11 recorded.
- [ ] Client evidence gathered for each story's Definition of Done.
- [ ] No credential, token, personal photo or real user data was committed.
