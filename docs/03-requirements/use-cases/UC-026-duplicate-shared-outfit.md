# UC-026 — Duplicate shared inspiration as an independent outfit draft

## 1. Use Case Summary

| Field | Value |
|---|---|
| Use Case ID | UC-026 |
| Use Case Name | Duplicate shared inspiration as an independent outfit draft |
| Scope / Level | CapsuleAI / User Goal |
| Primary Actor | User |
| Supporting Actors | None selected; Chat/preview/catalog providers and module allocation remain unresolved, not invented external actors. |
| Version / Status | 0.1 / Draft |
| Revision | 2026-11-09 — approved PR #4 / DD-007 scope propagation; no implementation or Sprint selection. |

## 2. Goal

Adapt an independent editable draft without changing the creator original or claiming ownership of its garments.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
|---|---|
| User | A truthful outcome, identifiable target, control and recovery. |
| Affected owner / creator | Private source information, confirmed authority and original content remain protected. |
| Product / Engineering / QA | Source-linked boundaries and testable accepted/failed outcomes without unapproved policies. |

## 4. Preconditions

An accessible identifiable shared outfit and authorization for the requesting user's draft. Authentication/authorization still applies if an existing session later expires. No current implementation is implied.

## 5. Trigger

The User deliberately starts the named goal from its relevant source area or selected look/item. Related goals are navigation/collaboration, not UML include/extend by default.

## 6. Main Success Scenario

1. The User opens an accessible shared outfit and selects Edit-as-duplicate.
2. CapsuleAI verifies source access and the permitted reusable representation.
3. CapsuleAI creates an independently editable draft for the requesting User.
4. The User changes the draft in their own outfit view.
5. CapsuleAI retains the original post unchanged and keeps unowned constituents distinct from confirmed owned garments.

## 7. Alternative Flows

### A1 — Independent continuation or cancellation

Before accepting a mutation, the User may decline/cancel it and continue independent supported product use. No canceled intent is represented as accepted. Each optional Like, Follow, Save, preview, duplicate, garment confirmation or Wear remains a distinct action with its own meaning; a related action is not automatically performed.

### A2 — Limited or missing information

Show available source information with its ownership/provenance and disclose what is missing. Empty/no-match data differs from retrieval failure. Where this goal includes Add Garment, allow manual correction/completion of Category and one Color with optional applicable fields; do not invent missing information, personal purchase Price or recommendation eligibility.

### A3 — Feedback or Wear handoff where applicable

Like on Community and AI Today/Chat suggestions and AI Today/Chat-only Dislike use [UC-013](UC-013-provide-outfit-feedback.md). Community Dislike is rejected without mutation. A preview's Wear Today entry uses [UC-014](UC-014-record-wear-event.md) only after current-valid-owned prerequisites; viewing, saving, following, duplicating, chatting or previewing alone creates no Wear or owned garment.

## 8. Exception / Failure Flows

### E1 — Unusable access or unavailable source target

Pause protected use; disclose no private source contents and mutate no unrelated target. Explain denied/unavailable access truthfully and allow renewed authentication or selection of an accessible source. Do not treat another user's Wardrobe or private source image as shared content.

### E2 — Dependency or retrieval failure

Show pending/failed/unavailable state separately from a completed result or genuinely empty data. Retain the identified selection and useful work where practical, and offer retry/return to independent paths. Missing AI does not justify a fabricated answer/preview or remove manual garment-entry continuity.

### E3 — Mutation outcome failed or uncertain

Known rejection/failure preserves prior accepted state. An interrupted acknowledgment alone proves neither acceptance nor definite failure. Recover by outcome review/safe retry of the same logical action, without duplicate accepted additions or applying a change to a different item. Bulk recovery uses each item's actual outcome under its refined save policy.

## 9. Success Postconditions

- The named source-linked outcome is identifiable for the authorized user and its actual target.
- Private Wardrobe authority, shared projection, catalog information, independent drafts, saved interest and reported Wear remain distinct.
- No unapproved audience, retention period, provider, model, batch limit, ranking rule or Sprint assignment is selected.

## 10. Minimal / Failure Postconditions

Previously accepted state and unrelated records remain protected. Pending/failed/uncertain work is never falsely complete; read/preview/selection/upload alone cannot silently confirm ownership or wear.

## 11. Business Rules

[Business Rules](../business-rules.md): BRULE-COM-001, BRULE-COM-002; BRULE-AUTH-001/007. The owning rule statements control; this interaction does not duplicate their policy. Garment entry, when present, follows BRULE-GAR-001/002/005/006/010 and the full source-owned 13-property model.

## 12. Requirements Traceability

| Source | References |
|---|---|
| Approved authority | [DD-007](../../06-scrum/delivery-decisions.md); merged PR #4 |
| Business | [BRD](../../01-business/BRD.md): BR-026, BR-021, BR-022; CAP-11 |
| Product | [PRD](../../02-product/PRD.md): FEAT-COM-002; JRN-08 |
| System | [SRS](../SRS.md): FR-COM-005, FR-COM-008; DATA-COM-001; FR-PERS-014 where feedback applies; DATA-INT-004; NFR-PRIV-001/002; applicable LOC/UI/ERR obligations |
| Delivery | [PBI-043](../../06-scrum/product-backlog.md) — unranked pool; [US-COM-003](../../06-scrum/stories/community/US-COM-003-duplicate-shared-inspiration-as-own-outfit-draft.md), its numbered AC and [DoD](../../06-scrum/definition-of-done.md) |
| Architecture | [ADD Section 15](../../04-architecture/ADD.md#15-approved-pr-4-capability-impact) — approved invariants versus unselected allocation |

## 13. Special Requirements / Constraints

Use Vietnamese user-facing meaning on supported Android/iOS with applicable accessible names/states and understandable recovery. Protect credentials, private media and unrelated profile/history. No MVP personal-data model training. User-linked measurement, when collected, retains the existing separate 90-day policy and cannot reverse accepted product actions; it is not functional Chat/post/saved-look retention.

## 14. Related Use Cases

[Master diagram](use-case-diagram.puml) associates this independent user goal. [UC-007](UC-007-add-garment.md) owns garment review/confirmation; [UC-013](UC-013-provide-outfit-feedback.md) owns typed preference; [UC-014](UC-014-record-wear-event.md) owns intentional reported use. Handoffs do not create formal include/extend relationships.

## 15. Open Issues

MVP-Q01, MVP-Q02, MVP-Q09 in [PRD Section 34.2](../../02-product/PRD.md#342-mvp-expansion-open-decisions) require relevant owner review before Ready. Recommendations are unapproved; JIT implementation contracts follow actual selection. No documentation-only completion/approval is claimed.

## 16. Use Case and Activity Diagrams

[Master Use Case Diagram](use-case-diagram.puml) defines actor participation; [AD-026](../activity-diagrams/AD-026-duplicate-shared-outfit.puml) models this complex flow. Source diagrams and this textual specification stay synchronized; a duplicate focused diagram is unnecessary.
