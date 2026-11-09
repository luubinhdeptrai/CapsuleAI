# Sprint 2 Decision Implementation Notes

| Field | Value |
| --- | --- |
| Sprint | Sprint 2 — Recover Access and Establish a Private Wardrobe |
| Decisions | Product Owner decisions of 2026-11-07 |
| Purpose | Where each decision is realized: implementation-guide sections and synchronized documents |
| Guides | [implementation-guides/README.md](implementation-guides/README.md) |

Status markers:

- ✅ **APPROVED AND INCORPORATED INTO GUIDES**: guide and documentation changes are complete.
- ⏳ **BACKEND IMPLEMENTATION PENDING**: the proposed code exists only in the guides, not in `backend/`.
- ⚠️ **EXTERNAL VERIFICATION PENDING**: needs evidence from outside the repository.
- 🟡 **DEFERRED BY APPROVED DECISION**.

Guide abbreviations: **README** = [README](implementation-guides/README.md), **SA** = [shared-architecture](implementation-guides/shared-architecture.md), **AUTH** = [US-AUTH-005 guide](implementation-guides/US-AUTH-005-guide.md), **GAR** = [US-GAR-001 guide](implementation-guides/US-GAR-001-guide.md), **WAR1** = [US-WAR-001 guide](implementation-guides/US-WAR-001-guide.md), **WAR2** = [US-WAR-002 guide](implementation-guides/US-WAR-002-guide.md).

| ID | Decision (short) | Status | Implementation guides | Requirement and other documents |
| --- | --- | --- | --- | --- |
| OPEN-S2-01A | Price per TD-S2-11: optional; missing ≠ 0; 0 valid; negative invalid; exact decimal string, ≤ 12 integer digits; scale ≤ ISO 4217 minor unit, rejected not rounded; upper-case ISO 4217; amount and currency together; no inference or conversion | ✅ ⏳ | SA §9, §22.1 TD-S2-11; GAR §9.3 C-4, §10.8, steps `PurchasePrice`, `GarmentEntryValidator`, V8, OpenAPI; GAR §15 | [Authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md) §4; [PRD](../../../02-product/PRD.md) §13.5, OPQ-013; [SRS](../../../03-requirements/SRS.md) §3.5.2, FR-GAR-013; [Business Rules](../../../03-requirements/business-rules.md) BRULE-GAR-010 |
| OPEN-S2-01B | Brand per TD-S2-12: optional, ≤ 100 code points, blank = none, kept exactly, no catalogue | ✅ ⏳ | SA §22.1 TD-S2-12; GAR §10.8, step `Brand`, V8; GAR §15 | Authority §4; PRD §13.5; SRS §3.5.2, FR-GAR-012; BRULE-GAR-010 |
| OPEN-S2-02 | Option A: verified HTTPS link (App Links / Universal Links) in production; custom scheme only for local development; `#token=` fragment; mobile/external fallback; no domain chosen | ✅ ⏳ ⚠️ | AUTH §9.3 C-5, §10.11, steps `PasswordResetProperties`, `application*.properties`; SA §8, §17, TD-S2-05 | SRS FR-AUTH-020; [US-AUTH-005](../../stories/auth/US-AUTH-005-recover-account-access.md) §6; [sprint plan](sprint-plan.md) §12.3 |
| OPEN-S2-03 | Vietnamese email approved with minor proofreading (subject `Đặt lại mật khẩu CapsuleAI`; single use; 30 minutes; ignore if unexpected; no wardrobe data; no delivery claim) | ✅ ⏳ | AUTH §10.8, step `SmtpPasswordResetInstructionSender` and its test | US-AUTH-005 §6; sprint plan §12.3 |
| OPEN-S2-04 | Option A: configurable per-email and per-client limits on initiation; check and complete protected; identical for registered and unregistered addresses; Identity-owned; no Redis | ✅ ⏳ | AUTH §9.2 AC-17, §10.12, §15 (AC-17); SA §18, TD-S2-26; V7 attempt table | SRS FR-AUTH-019; BRULE-AUTH-005; [UC-004](../../../03-requirements/use-cases/UC-004-reset-password.md) E6; US-AUTH-005 AC-17 |
| OPEN-S2-05 | Option A: truthful 503, timing hardening with measured tests, documented residual risk, no constant-time claim | ✅ ⏳ | AUTH §9.3 C-4, §10.13, `PasswordResetTimingIT`; SA §18, TD-S2-27 | SRS NFR-SEC-003 verification; sprint plan §12.3 |
| OPEN-S2-06 | Option B: denylist deferred; Sprint 1 `PasswordPolicy` (12–128 code points) for registration and reset | ✅ 🟡 | AUTH §9.3 C-2, §18.5; SA §22.2 | US-AUTH-005 §6; sprint plan §12.3 |
| OPEN-S2-07 | Option A: 30-day retention after `COALESCE(consumed_at, replaced_at, expires_at)`; tested, concurrency-safe in-process cleanup; never deletes an active interaction | ✅ ⏳ | AUTH §10.2, §10.14, steps `PurgeEndedPasswordResetsService`, `PasswordResetCleanupJob`; SA §11.2 notes, TD-S2-28 | SRS DATA-AUTH-006; BRULE-AUTH-004; US-AUTH-005 §6 |
| OPEN-S2-08 | Option B: multiple Styles like Materials (zero, one or many; set; approved values only; all eight categories; no 14th property; `"styles": [...]`) | ✅ ⏳ | GAR §9.3 C-5, §10.2–§10.5, §10.8, steps `GarmentProfile` to OpenAPI; WAR2 §9.3 C-7, §10; SA §9, §10, TD-S2-13 | Authority §1, §3; [BRD](../../../01-business/BRD.md) property table; PRD §13.1, OPQ-016; SRS FR-GAR-009, DATA-GAR-002, property table; BRULE-GAR-006; [US-GAR-001](../../stories/garment/US-GAR-001-add-garment-manually.md) AC-06; [US-WAR-002](../../stories/wardrobe/US-WAR-002-inspect-owned-garment.md) AC-01 |
| OPEN-S2-09 | Option A: provider-neutral SMTP; Mailpit for tests only; real provider, sender domain, SPF/DKIM/DMARC, TLS and delivery evidence before release or Done | ✅ ⏳ ⚠️ | AUTH §10.8, §16, §18.3; README §16; SA §17 | US-AUTH-005 §6; backend README "Before release" (in the AUTH README step) |
| OPEN-S2-10 | Option B: cursor pagination in Sprint 2 (keyset on `created_at DESC` plus id; owner-isolated; current only; explicit continuation) | ✅ ⏳ | WAR1 §9.3 C-1, §10.3–§10.10, §15 (AC-12); SA §13, §15, TD-S2-19, TD-S2-29; GAR V8 index | PRD FEAT-WAR-001 item 7, OPQ-018; SRS FR-WAR-001; [UC-008](../../../03-requirements/use-cases/UC-008-view-wardrobe.md) A5; [US-WAR-001](../../stories/wardrobe/US-WAR-001-browse-current-wardrobe.md) AC-12, §8 |
| OPEN-S2-11 | PRD FEAT-WAR-001 compatibility-readiness wording removed; UCs, stories and SRS aligned | ✅ | GAR §9.3 C-2; WAR1 §9.3 C-3/C-4; WAR2 §9.3 C-5 | PRD FEAT-WAR-001 item 2; [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); UC-008 A4 and rules table; stories US-GAR-001/US-WAR-001/US-WAR-002; [test strategy](../../../07-testing/test-strategy.md) |
| OPEN-S2-12 | SRS DATA-GAR-002 "SOLID density" removed; Solid stays a Pattern value | ✅ | GAR §9.3 C-3 | SRS DATA-GAR-002 |
| X-1 | Explicit `recommendationEligibility: NOT_EVALUATED`; no readiness | ✅ ⏳ | SA §9, §22.3; GAR §9.2 AC-08; WAR1/WAR2 §15.3 | PRD FEAT-WAR-001; UC-007, UC-008 |
| X-2 | Revoke only active sessions; preserve earlier revocations; atomic with the password change; concurrency and regression tests | ✅ ⏳ | AUTH §9.3 C-1, §10.6, §15 (AC-05); SA TD-S2-07 | BRULE-AUTH-005 |
| X-3 | No image field | ✅ ⏳ | SA TD-S2-15; GAR, WAR1, WAR2 §15.3 | — |
| X-4 | `removed_at` plus test fixtures only; no removal endpoint | ✅ ⏳ | SA TD-S2-18; WAR1 §9.3 C-5; WAR2 | — |
| X-5 | `recordedUse.availability = UNAVAILABLE`; no `wearCount`, no `lastWornAt` | ✅ ⏳ | WAR2 §9.3 C-3, §15.3; SA TD-S2-21 | — |
| X-6 | Apply OPEN-S2-01A, 01B and 08 | ✅ ⏳ | See those rows; SA §22.3 | See those rows |
| X-7 | Preserve AR-01–AR-08 | ✅ | SA §5, §10–§12, §22.3; every guide's schema-ownership tests | [Selected architecture decisions](../../../04-architecture/selected-architecture-decisions.md) §8 (already in Stage 0) |
| REVIEW-CI | Option A: proposed workflow also runs for pull requests into `sprint_02`; `main` unchanged; guard not weakened | ✅ ⏳ | AUTH step `backend-ci.yml`; README §14; SA TD-S2-30 | The real `.github/workflows/backend-ci.yml` is unchanged until the AUTH guide is applied |
| REVIEW-DB-ROLE | Option A: single database role with build-time checks; not hard isolation | ✅ | SA §18; README §16 | Selected architecture decisions §8.5 |
| REVIEW-TAXONOMY | "Tanks & Camils" / `TANKS_AND_CAMILS` → "Tanks & Camis" / `TANKS_AND_CAMIS` everywhere; no other taxonomy change | ✅ ⏳ | GAR §9.3 C-6, steps `GarmentSubtype`, V8, OpenAPI, `GarmentSchemaIT`; SA §9, §13 | Authority §3; PRD OPQ-017 |

The executable `docs/05-api/openapi.yaml` stays at v0.4.1. Each guide specifies its staged OpenAPI change (0.5.0–0.8.0).
