# UC-028 — Preview a selected look on a generic model

## 1. Use Case Summary

| Field | Value |
|---|---|
| Use Case ID | UC-028 |
| Use Case Name | Preview a selected look on a generic model |
| Scope / Level | CapsuleAI / User Goal |
| Primary Actor | User |
| Supporting Actors | None selected; Chat/preview/catalog providers and module allocation remain unresolved, not invented external actors. |
| Version / Status | 0.1 / Draft |
| Revision | 2026-11-09 — approved PR #4 / DD-007 scope propagation; no implementation or Sprint selection. |

## 2. Goal

Inspect inspiration before a separate action while keeping ownership and reported wear truthful.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
|---|---|
| User | A truthful outcome, identifiable target, control and recovery. |
| Affected owner / creator | Private source information, confirmed authority and original content remain protected. |
| Product / Engineering / QA | Source-linked boundaries and testable accepted/failed outcomes without unapproved policies. |

## 4. Preconditions

An accessible identified selected outfit; preview method, acceptable fidelity and purpose-minimal inputs are approved before Ready. Authentication/authorization still applies if an existing session later expires. No current implementation is implied.

## 5. Trigger

The User deliberately starts the named goal from its relevant source area or selected look/item. Related goals are navigation/collaboration, not UML include/extend by default.

## 6. Main Success Scenario

1. The User selects an accessible outfit and requests generic-model Try-On.
2. CapsuleAI retains the identified selected-look basis and requests a preview using approved minimal input.
3. When available, CapsuleAI shows the generic-model result associated with that selection and its limitations.
4. The User may explicitly Save look, return, or initiate Wear Today.
5. Save retains interest only. Wear proceeds through UC-014 only if existing current-valid-owned prerequisites hold; unowned inspiration cannot bypass them.

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

[Business Rules](../business-rules.md): BRULE-TRY-001/002; BRULE-COM-002; BRULE-WEAR-001/002; BRULE-AUTH-001/007. The owning rule statements control; this interaction does not duplicate their policy. Garment entry, when present, follows BRULE-GAR-001/002/005/006/010 and the full source-owned 13-property model.

## 12. Requirements Traceability

| Source | References |
|---|---|
| Approved authority | [DD-007](../../06-scrum/delivery-decisions.md); merged PR #4 |
| Business | [BRD](../../01-business/BRD.md): BR-028, BR-021, BR-022; CAP-13 |
| Product | [PRD](../../02-product/PRD.md): FEAT-TRY-001; JRN-10 |
| System | [SRS](../SRS.md): FR-TRY-001, FR-TRY-002, FR-TRY-003, FR-TRY-004, FR-TRY-005; DATA-TRY-001; FR-PERS-014 where feedback applies; DATA-INT-004; NFR-PRIV-001/002; applicable LOC/UI/ERR obligations |
| Delivery | [PBI-045](../../06-scrum/product-backlog.md) — unranked pool; [US-TRY-001](../../06-scrum/stories/try-on/US-TRY-001-preview-selected-look-on-generic-model.md), its numbered AC and [DoD](../../06-scrum/definition-of-done.md) |
| Architecture | [ADD Section 15](../../04-architecture/ADD.md#15-approved-pr-4-capability-impact) — approved invariants versus unselected allocation |

## 13. Special Requirements / Constraints

Use Vietnamese user-facing meaning on supported Android/iOS with applicable accessible names/states and understandable recovery. Protect credentials, private media and unrelated profile/history. No MVP personal-data model training. User-linked measurement, when collected, retains the existing separate 90-day policy and cannot reverse accepted product actions; it is not functional Chat/post/saved-look retention.

## 14. Related Use Cases

[Master diagram](use-case-diagram.puml) associates this independent user goal. [UC-007](UC-007-add-garment.md) owns garment review/confirmation; [UC-013](UC-013-provide-outfit-feedback.md) owns typed preference; [UC-014](UC-014-record-wear-event.md) owns intentional reported use. Handoffs do not create formal include/extend relationships.

## 15. Open Issues

MVP-Q02, MVP-Q04, MVP-Q05, MVP-Q09 in [PRD Section 34.2](../../02-product/PRD.md#342-mvp-expansion-open-decisions) require relevant owner review before Ready. Recommendations are unapproved; JIT implementation contracts follow actual selection. No documentation-only completion/approval is claimed.

## 16. Use Case and Activity Diagrams

[Master Use Case Diagram](use-case-diagram.puml) defines actor participation; [AD-028](../activity-diagrams/AD-028-preview-generic-model-try-on.puml) models this complex flow. Source diagrams and this textual specification stay synchronized; a duplicate focused diagram is unnecessary.
