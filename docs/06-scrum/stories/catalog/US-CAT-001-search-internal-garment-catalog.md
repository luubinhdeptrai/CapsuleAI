# US-CAT-001 — Search the internal garment catalog

## Document Control

| Field | Value |
|---|---|
| Artifact | User Story |
| Story ID | US-CAT-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-046](../../product-backlog.md) — unranked refinement pool, Section 6.1 |
| Domain / Theme | catalog / Approved MVP extension |
| Product Goal Contribution | [Product Goal](../../product-goal.md): Find relevant catalog garment information separately from owned wardrobe discovery. |
| Revision note | 2026-11-09: source-linked Draft from approved merged PR #4 / DD-007; no Ready, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated CapsuleAI user, I want to search and inspect relevant catalog items separately from my owned wardrobe, so that I can discover usable garment information without mistaking catalog results for possessions.

## 2. Value

Find relevant catalog garment information separately from owned wardrobe discovery.

## 3. Scope

Catalog search, identifiable results/unknown data and selection for an explicit Add-to-Closet intent. Actual owned addition belongs to US-CAT-002; no merchant agreement or live inventory is assumed.

## 4. Preconditions

Authorized catalog access and an approved usable catalog source/search scope. These preconditions do not imply a new provider, UI tab, algorithm or product-policy default.

## 5. Acceptance Criteria

### AC-01 — Same query in separate catalog and owned domains

Given the authorized source and applicable approved policy, the system shall search the internal garment catalog as a source distinct from the authenticated user's owned-wardrobe search governed by FR-WAR-007.

**Source / verification:** FR-CAT-001; verify same query in separate catalog and owned domains.

### AC-02 — Result identity, known/unknown fields and provenance

Given the authorized source and applicable approved policy, the system shall present identifiable catalog results and available descriptive information while distinguishing missing, uncertain or unavailable information from confirmed personal garment values.

**Source / verification:** FR-CAT-002; verify result identity, known/unknown fields and provenance.

### AC-03 — All result states; wardrobe unchanged

Given the authorized source and applicable approved policy, the system shall distinguish catalog loading, no matches, unavailable retrieval and denied access, offering change/reset/retry where applicable without implying deletion of owned garments.

**Source / verification:** FR-CAT-003; verify all result states; wardrobe unchanged.

### AC-04 — Search/select has no ownership side effect

Given the authorized source and applicable approved policy, the system shall allow selection of a catalog result for an explicit Add-to-Closet intent without treating viewing, searching or selecting alone as confirmed ownership.

**Source / verification:** FR-CAT-004; verify search/select has no ownership side effect.

### AC-05 — Accepted, canceled and uncertain actions

Cancellation or known rejection/failure does not apply the requested mutation. An interrupted response does not prove success or failure: retain an unconfirmed outcome, allow outcome review/safe same-action retry and avoid duplicate accepted effects. A read, preview, Like, Follow, Save or draft copy alone never creates a confirmed Garment or Wear Event. An expired/revoked/wrong-account operation reveals no private state or unauthorized mutation. Optional measurement failure cannot reverse a valid accepted action.

## 6. Quality and Verification

Apply [shared quality/completion](../README.md#2-shared-quality-and-completion), the [Definition of Done](../../definition-of-done.md) and [Test Strategy](../../../07-testing/test-strategy.md) proportionally. Use known authorized/denied fixtures, missing information and dependency failure, plus real persistence/integration/client evidence when delivered. Vietnamese Android/iOS accessibility and truthful states apply. Protect secrets/private source data; no MVP personal-data training. New tests are planned obligations, not executed or implemented evidence.

## 7. Dependencies

Catalog source/rights and search feasibility. US-WAR-003 remains owned search and is not replaced or a populated-wardrobe prerequisite. Collaborations do not establish priority or a serial schedule. Existing module/public-contract constraints remain authoritative.

## 8. Out of Scope

Comments, messaging, entire-wardrobe sharing, multithread chat history, personalized avatars, AR/3D, automatic ownership/wear/purchase verification and native commerce remain excluded. This story does not implement neighboring capabilities merely to expose an entry point. Full API/data/sequence/state/provider/transaction design follows actual Sprint selection only where justified by complexity and risk.

## 9. Traceability

| Source | References |
|---|---|
| Authority | [DD-007](../../delivery-decisions.md); merged PR #4 (`9e10cbb`) |
| Parent PBI | [PBI-046](../../product-backlog.md) — Section 6.1; no relative order selected |
| Business | [BRD](../../../01-business/BRD.md): BR-029, BR-021, BR-022; CAP-14; BG-01 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-CAT-001; JRN-11 |
| Software requirements | [SRS](../../../03-requirements/SRS.md): FR-CAT-001, FR-CAT-002, FR-CAT-003, FR-CAT-004; DATA-CAT-001; FR-PERS-014 where feedback applies; NFR-PRIV-001/002; DATA-INT-004; relevant ERR/LOC/UI requirements |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-CAT-001; BRULE-AUTH-001/007 |
| Use Case | [UC-029](../../../03-requirements/use-cases/UC-029-search-internal-garment-catalog.md) |
| Activity Diagram | [AD-029](../../../03-requirements/activity-diagrams/AD-029-search-internal-garment-catalog.puml) |
| Architecture / verification | [ADD Section 15](../../../04-architecture/ADD.md#15-approved-pr-4-capability-impact); [Test Strategy](../../../07-testing/test-strategy.md) — implementation/test evidence pending selection |

## 10. Open Refinement Notes

MVP-Q07, MVP-Q09 are maintained in [PRD Section 34.2](../../../02-product/PRD.md#342-mvp-expansion-open-decisions). Resolve only relevant approved-scope details and feasibility before Ready. Options/recommendations are unapproved. No new numeric rule, source agreement, provider, retention period, estimate or Sprint commitment is inferred. JIT mechanics remain with selected work.

## 11. Status

**Draft.** PO/BA, Developers and QA review remains pending. Approved MVP scope does not mean this story is Ready, selected, implemented or Done.
