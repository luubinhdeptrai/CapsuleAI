# US-CHAT-001 — Consult the single-thread AI Chat Stylist

## Document Control

| Field | Value |
|---|---|
| Artifact | User Story |
| Story ID | US-CHAT-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-044](../../product-backlog.md) — unranked refinement pool, Section 6.1 |
| Domain / Theme | chat / Approved MVP extension |
| Product Goal Contribution | [Product Goal](../../product-goal.md): Ask for relevant styling guidance and continue into explicit product actions. |
| Revision note | 2026-11-09: source-linked Draft from approved merged PR #4 / DD-007; no Ready, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated CapsuleAI user, I want to ask a stylist in one text thread, inspect guidance cards/chips and route garment photos to Add, so that I can get understandable guidance and choose a separate deliberate wardrobe or feedback action.

## 2. Value

Ask for relevant styling guidance and continue into explicit product actions.

## 3. Scope

Single-thread text, outfit/item cards, suggestion chips, photo attachment into Add Garment and Like/Dislike on AI outfit suggestions. No multithread history, automatic add, personal-data training or invented provider.

## 4. Preconditions

Authorized access to the single conversation; only relevant approved personal context may be used. These preconditions do not imply a new provider, UI tab, algorithm or product-policy default.

## 5. Acceptance Criteria

### AC-01 — Text interaction in one thread; no multithread claim

Given the authorized source and applicable approved policy, the system shall provide one AI stylist conversation thread supporting user text and corresponding AI replies, without requiring or introducing multi-thread history.

**Source / verification:** FR-CHAT-001; verify text interaction in one thread; no multithread claim.

### AC-02 — Card source identity and ownership meaning

Given the authorized source and applicable approved policy, the system shall present supported outfit/item cards in Chat with identifiable referenced looks/items and their current owned, catalog or inspiration meaning, without fabricating ownership or verified suitability.

**Source / verification:** FR-CHAT-002; verify card source identity and ownership meaning.

### AC-03 — Chip action meaning and no implied mutation

Given the authorized source and applicable approved policy, the system shall offer suggestion chips for continuing the conversation or an identified product action, without treating chip selection alone as Like, Dislike, Wear or confirmed ownership.

**Source / verification:** FR-CHAT-003; verify chip action meaning and no implied mutation.

### AC-04 — Photo route, correction/confirmation/cancel; no auto-add

Given the authorized source and applicable approved policy, the system shall route an attached garment photo into Add Garment review and explicit confirmation, preserving the same property, authority, media-authorization and failure rules rather than silently adding a garment from Chat.

**Source / verification:** FR-CHAT-004; verify photo route, correction/confirmation/cancel; no auto-add.

### AC-05 — AI Chat Like/Dislike/clear; Community target rejected

Given the authorized source and applicable approved policy, the system shall support Like and Dislike on AI-generated Chat outfit suggestions through FR-PERS-001–005/014, without applying that Dislike to a Community post or an unrelated target.

**Source / verification:** FR-CHAT-005; verify AI Chat Like/Dislike/clear; Community target rejected.

### AC-06 — Account isolation, absent context and provider payload inspection

Given the authorized source and applicable approved policy, the system shall use only the current user's authorized relevant context for Chat, identify missing context, and restrict any external exchange to the approved purpose-minimal contract; conversation retention and disclosure require MVP-Q03/MVP-Q09 refinement before Ready.

**Source / verification:** FR-CHAT-006; verify account isolation, absent context and provider payload inspection.

### AC-07 — AI unavailable and uncertain reply/action recovery

Given the authorized source and applicable approved policy, the system shall show pending, unavailable or failed AI replies truthfully and allow appropriate retry or return to independent wardrobe actions, without claiming a fabricated answer or completed product mutation.

**Source / verification:** FR-CHAT-007; verify AI unavailable and uncertain reply/action recovery.

### AC-08 — Accepted, canceled and uncertain actions

Cancellation or known rejection/failure does not apply the requested mutation. An interrupted response does not prove success or failure: retain an unconfirmed outcome, allow outcome review/safe same-action retry and avoid duplicate accepted effects. A read, preview, Like, Follow, Save or draft copy alone never creates a confirmed Garment or Wear Event. An expired/revoked/wrong-account operation reveals no private state or unauthorized mutation. Optional measurement failure cannot reverse a valid accepted action.

## 6. Quality and Verification

Apply [shared quality/completion](../README.md#2-shared-quality-and-completion), the [Definition of Done](../../definition-of-done.md) and [Test Strategy](../../../07-testing/test-strategy.md) proportionally. Use known authorized/denied fixtures, missing information and dependency failure, plus real persistence/integration/client evidence when delivered. Vietnamese Android/iOS accessibility and truthful states apply. Protect secrets/private source data; no MVP personal-data training. New tests are planned obligations, not executed or implemented evidence.

## 7. Dependencies

Private access; approved Chat feasibility/retention/context contract. Existing Add review PBI-012/PBI-006 for photo routing and PBI-020 for suggestion feedback are collaborating outcomes, not implementation authorization. Collaborations do not establish priority or a serial schedule. Existing module/public-contract constraints remain authoritative.

## 8. Out of Scope

Comments, messaging, entire-wardrobe sharing, multithread chat history, personalized avatars, AR/3D, automatic ownership/wear/purchase verification and native commerce remain excluded. This story does not implement neighboring capabilities merely to expose an entry point. Full API/data/sequence/state/provider/transaction design follows actual Sprint selection only where justified by complexity and risk.

## 9. Traceability

| Source | References |
|---|---|
| Authority | [DD-007](../../delivery-decisions.md); merged PR #4 (`9e10cbb`) |
| Parent PBI | [PBI-044](../../product-backlog.md) — Section 6.1; no relative order selected |
| Business | [BRD](../../../01-business/BRD.md): BR-027, BR-021, BR-022; CAP-12; BG-02, BG-03 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-CHAT-001; JRN-09 |
| Software requirements | [SRS](../../../03-requirements/SRS.md): FR-CHAT-001, FR-CHAT-002, FR-CHAT-003, FR-CHAT-004, FR-CHAT-005, FR-CHAT-006, FR-CHAT-007; DATA-CHAT-001; FR-PERS-014 where feedback applies; NFR-PRIV-001/002; DATA-INT-004; relevant ERR/LOC/UI requirements |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-CHAT-001/002; BRULE-PERS-001; BRULE-GAR-001/002; BRULE-AUTH-001/007 |
| Use Case | [UC-027](../../../03-requirements/use-cases/UC-027-consult-ai-chat-stylist.md) |
| Activity Diagram | [AD-027](../../../03-requirements/activity-diagrams/AD-027-consult-ai-chat-stylist.puml) |
| Architecture / verification | [ADD Section 15](../../../04-architecture/ADD.md#15-approved-pr-4-capability-impact); [Test Strategy](../../../07-testing/test-strategy.md) — implementation/test evidence pending selection |

## 10. Open Refinement Notes

MVP-Q03, MVP-Q09 are maintained in [PRD Section 34.2](../../../02-product/PRD.md#342-mvp-expansion-open-decisions). Resolve only relevant approved-scope details and feasibility before Ready. Options/recommendations are unapproved. No new numeric rule, source agreement, provider, retention period, estimate or Sprint commitment is inferred. JIT mechanics remain with selected work.

## 11. Status

**Draft.** PO/BA, Developers and QA review remains pending. Approved MVP scope does not mean this story is Ready, selected, implemented or Done.
