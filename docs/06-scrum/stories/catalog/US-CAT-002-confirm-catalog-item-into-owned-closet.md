# US-CAT-002 — Confirm a catalog item into the owned closet

## Document Control

| Field | Value |
|---|---|
| Artifact | User Story |
| Story ID | US-CAT-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-047](../../product-backlog.md) — unranked refinement pool, Section 6.1 |
| Domain / Theme | catalog / Approved MVP extension |
| Product Goal Contribution | [Product Goal](../../product-goal.md): Reuse supported catalog information while personally confirming the actual owned garment. |
| Revision note | 2026-11-09: source-linked Draft from approved merged PR #4 / DD-007; no Ready, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated CapsuleAI user, I want to review and confirm the actual garment represented by a selected catalog item, so that I can save a trustworthy owned profile while retaining personal purchase information and confirmation authority.

## 2. Value

Reuse supported catalog information while personally confirming the actual owned garment.

## 3. Scope

Review/correct all 13 applicable properties, confirm Category and one Color, preserve optional distinct Materials/Styles and optional Brand/purchase Price, then safely save a distinct owned garment. Catalog asking price is not purchase Price; no automatic source overwrite.

## 4. Preconditions

An accessible selected catalog item and explicit Add-to-Closet intent; accepted confirmation is still required. These preconditions do not imply a new provider, UI tab, algorithm or product-policy default.

## 5. Acceptance Criteria

### AC-01 — Full/partial/minimum profiles and invalid applicability

Given the authorized source and applicable approved policy, the system shall allow review/correction of all 13 applicable garment properties before catalog-to-closet confirmation, preserving complete source vocabularies, distinct Materials and Styles, category applicability and the confirmed Category/one Color minimum.

**Source / verification:** FR-CAT-005; verify full/partial/minimum profiles and invalid applicability.

### AC-02 — Confirmed addition versus later source change

Given the authorized source and applicable approved policy, the system shall create a distinct owner-controlled garment only on accepted user confirmation, retain source-versus-user-confirmed meaning, and prevent later catalog updates from silently overwriting its confirmed profile.

**Source / verification:** FR-CAT-006; verify confirmed addition versus later source change.

### AC-03 — Missing minimum, missing optional data and no price inference

Given the authorized source and applicable approved policy, the system shall permit correction/manual completion or cancellation when catalog information cannot satisfy the saving minimum; it shall not infer personal purchase Price from catalog asking price or impose an optional-property/eligibility gate.

**Source / verification:** FR-CAT-007; verify missing minimum, missing optional data and no price inference.

### AC-04 — Same-action retry, separate intent and owner isolation

Given the authorized source and applicable approved policy, the system shall distinguish canceled, pending, known failed, uncertain and accepted catalog additions and recover one logical addition without duplicate accepted garments or another user's data.

**Source / verification:** FR-CAT-008; verify same-action retry, separate intent and owner isolation.

### AC-05 — Accepted, canceled and uncertain actions

Cancellation or known rejection/failure does not apply the requested mutation. An interrupted response does not prove success or failure: retain an unconfirmed outcome, allow outcome review/safe same-action retry and avoid duplicate accepted effects. A read, preview, Like, Follow, Save or draft copy alone never creates a confirmed Garment or Wear Event. An expired/revoked/wrong-account operation reveals no private state or unauthorized mutation. Optional measurement failure cannot reverse a valid accepted action.

## 6. Quality and Verification

Apply [shared quality/completion](../README.md#2-shared-quality-and-completion), the [Definition of Done](../../definition-of-done.md) and [Test Strategy](../../../07-testing/test-strategy.md) proportionally. Use known authorized/denied fixtures, missing information and dependency failure, plus real persistence/integration/client evidence when delivered. Vietnamese Android/iOS accessibility and truthful states apply. Protect secrets/private source data; no MVP personal-data training. New tests are planned obligations, not executed or implemented evidence.

## 7. Dependencies

PBI-046 catalog selection and PBI-006 confirmed garment authority; existing property/validation/safe-retry contracts are reused where applicable, not redesigned here. Collaborations do not establish priority or a serial schedule. Existing module/public-contract constraints remain authoritative.

## 8. Out of Scope

Comments, messaging, entire-wardrobe sharing, multithread chat history, personalized avatars, AR/3D, automatic ownership/wear/purchase verification and native commerce remain excluded. This story does not implement neighboring capabilities merely to expose an entry point. Full API/data/sequence/state/provider/transaction design follows actual Sprint selection only where justified by complexity and risk.

## 9. Traceability

| Source | References |
|---|---|
| Authority | [DD-007](../../delivery-decisions.md); merged PR #4 (`9e10cbb`) |
| Parent PBI | [PBI-047](../../product-backlog.md) — Section 6.1; no relative order selected |
| Business | [BRD](../../../01-business/BRD.md): BR-030, BR-021, BR-022; CAP-14; BG-01 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-CAT-002; JRN-11 |
| Software requirements | [SRS](../../../03-requirements/SRS.md): FR-CAT-005, FR-CAT-006, FR-CAT-007, FR-CAT-008; DATA-CAT-001; FR-PERS-014 where feedback applies; NFR-PRIV-001/002; DATA-INT-004; relevant ERR/LOC/UI requirements |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-CAT-001/002; BRULE-GAR-001/002/005/006/010; BRULE-AUTH-001/007 |
| Use Case | [UC-030](../../../03-requirements/use-cases/UC-030-confirm-catalog-item-into-closet.md) |
| Activity Diagram | [AD-030](../../../03-requirements/activity-diagrams/AD-030-confirm-catalog-item-into-closet.puml) |
| Architecture / verification | [ADD Section 15](../../../04-architecture/ADD.md#15-approved-pr-4-capability-impact); [Test Strategy](../../../07-testing/test-strategy.md) — implementation/test evidence pending selection |

## 10. Open Refinement Notes

MVP-Q07, MVP-Q09 are maintained in [PRD Section 34.2](../../../02-product/PRD.md#342-mvp-expansion-open-decisions). Resolve only relevant approved-scope details and feasibility before Ready. Options/recommendations are unapproved. No new numeric rule, source agreement, provider, retention period, estimate or Sprint commitment is inferred. JIT mechanics remain with selected work.

## 11. Status

**Draft.** PO/BA, Developers and QA review remains pending. Approved MVP scope does not mean this story is Ready, selected, implemented or Done.
