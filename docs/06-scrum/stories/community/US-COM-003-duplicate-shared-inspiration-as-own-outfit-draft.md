# US-COM-003 — Duplicate shared inspiration as an independent outfit draft

## Document Control

| Field | Value |
|---|---|
| Artifact | User Story |
| Story ID | US-COM-003 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-043](../../product-backlog.md) — unranked refinement pool, Section 6.1 |
| Domain / Theme | community / Approved MVP extension |
| Product Goal Contribution | [Product Goal](../../product-goal.md): Share selected inspiration and adapt a copy while preserving the creator original and private inventory. |
| Revision note | 2026-11-09: source-linked Draft from approved merged PR #4 / DD-007; no Ready, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated CapsuleAI user, I want to use Edit-as-duplicate on a shared outfit, so that I can adapt an independent editable draft without changing the creator original or claiming ownership of its garments.

## 2. Value

Share selected inspiration and adapt a copy while preserving the creator original and private inventory.

## 3. Scope

Copy only the approved reusable outfit representation into the requesting user's own editable draft. Editing does not mutate the source, copy private inventory, confirm garment ownership or create Wear.

## 4. Preconditions

An accessible identifiable shared outfit and authorization for the requesting user's draft. These preconditions do not imply a new provider, UI tab, algorithm or product-policy default.

## 5. Acceptance Criteria

### AC-01 — Edit copy; original and ownership unchanged

Given the authorized source and applicable approved policy, the system shall create an independently editable outfit draft for the requesting user through Edit-as-duplicate, without modifying the shared original or creating confirmed owned garments.

**Source / verification:** FR-COM-005; verify edit copy; original and ownership unchanged.

### AC-02 — Two-account allowed projection and denied private access

Given the authorized source and applicable approved policy, the system shall authorize shared-content access and each creator or affected-user operation separately from private Wardrobe authorization, applying the approved visibility/consent policy without exposing private inventory, purchase Price, profile or history.

**Source / verification:** FR-COM-008; verify two-account allowed projection and denied private access.

### AC-03 — Accepted, canceled and uncertain actions

Cancellation or known rejection/failure does not apply the requested mutation. An interrupted response does not prove success or failure: retain an unconfirmed outcome, allow outcome review/safe same-action retry and avoid duplicate accepted effects. A read, preview, Like, Follow, Save or draft copy alone never creates a confirmed Garment or Wear Event. An expired/revoked/wrong-account operation reveals no private state or unauthorized mutation. Optional measurement failure cannot reverse a valid accepted action.

## 6. Quality and Verification

Apply [shared quality/completion](../README.md#2-shared-quality-and-completion), the [Definition of Done](../../definition-of-done.md) and [Test Strategy](../../../07-testing/test-strategy.md) proportionally. Use known authorized/denied fixtures, missing information and dependency failure, plus real persistence/integration/client evidence when delivered. Vietnamese Android/iOS accessibility and truthful states apply. Protect secrets/private source data; no MVP personal-data training. New tests are planned obligations, not executed or implemented evidence.

## 7. Dependencies

Shared-post projection PBI-042/PBI-043 and approved reuse/withdrawal policy. No automatic dependency on garment ingestion or Wear is created by making an outfit draft. Collaborations do not establish priority or a serial schedule. Existing module/public-contract constraints remain authoritative.

## 8. Out of Scope

Comments, messaging, entire-wardrobe sharing, multithread chat history, personalized avatars, AR/3D, automatic ownership/wear/purchase verification and native commerce remain excluded. This story does not implement neighboring capabilities merely to expose an entry point. Full API/data/sequence/state/provider/transaction design follows actual Sprint selection only where justified by complexity and risk.

## 9. Traceability

| Source | References |
|---|---|
| Authority | [DD-007](../../delivery-decisions.md); merged PR #4 (`9e10cbb`) |
| Parent PBI | [PBI-043](../../product-backlog.md) — Section 6.1; no relative order selected |
| Business | [BRD](../../../01-business/BRD.md): BR-026, BR-021, BR-022; CAP-11; BG-02, BG-03 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-COM-002; JRN-08 |
| Software requirements | [SRS](../../../03-requirements/SRS.md): FR-COM-005, FR-COM-008; DATA-COM-001; FR-PERS-014 where feedback applies; NFR-PRIV-001/002; DATA-INT-004; relevant ERR/LOC/UI requirements |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-COM-001, BRULE-COM-002; BRULE-AUTH-001/007 |
| Use Case | [UC-026](../../../03-requirements/use-cases/UC-026-duplicate-shared-outfit.md) |
| Activity Diagram | [AD-026](../../../03-requirements/activity-diagrams/AD-026-duplicate-shared-outfit.puml) |
| Architecture / verification | [ADD Section 15](../../../04-architecture/ADD.md#15-approved-pr-4-capability-impact); [Test Strategy](../../../07-testing/test-strategy.md) — implementation/test evidence pending selection |

## 10. Open Refinement Notes

MVP-Q01, MVP-Q02, MVP-Q09 are maintained in [PRD Section 34.2](../../../02-product/PRD.md#342-mvp-expansion-open-decisions). Resolve only relevant approved-scope details and feasibility before Ready. Options/recommendations are unapproved. No new numeric rule, source agreement, provider, retention period, estimate or Sprint commitment is inferred. JIT mechanics remain with selected work.

## 11. Status

**Draft.** PO/BA, Developers and QA review remains pending. Approved MVP scope does not mean this story is Ready, selected, implemented or Done.
