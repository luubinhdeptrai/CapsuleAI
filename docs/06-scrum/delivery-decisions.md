# CapsuleAI — Delivery Decisions

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Delivery Decision Log |
| Version | 0.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Primary Audience | Backend Developer / Development Team |
| Ownership | Product Owner / BA, with team input within workflow responsibilities |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) |
| Revision Note | Initial handoff record of existing ordering, refinement and story boundaries, role responsibilities and the next workflow activity. |

## 2. Purpose

This lightweight CapsuleAI convention explains delivery decisions that affect developer handoff. It records what the current repository establishes and the responsibility for later refinement; it does not claim historical meetings or approvals.

Exact business/product behavior remains in BRD/PRD, software obligations in SRS, invariants in Business Rules, interactions in Use Cases/Activity Diagrams, ordering in the Product Backlog and near-term behavior in stories/AC. Architecture design and technical decision history belong in the later ADD/views and ADRs. This log references those authorities rather than overriding them; Scrum does not prescribe this file.

## 3. How to Use This File

Read active decisions for their Backend Developer impact, then follow the linked sources for exact behavior and work boundaries. Product ordering and decomposition are handled through the PO/BA/team workflow; contribute feasibility, integration risks and capacity without taking over Product Owner accountability.

Keep DD IDs stable. When guidance changes, update its authoritative source first where required, mark replaced entries Superseded and link the replacement. Add an entry only when the delivery implication merits a handoff record.

## 4. Active Delivery Decisions

### DD-001 — Preserve the single ordered Product Backlog

**Status:** Active

**Decision:** Use the existing 41-PBI Product Backlog as the delivery-order authority. The Product Owner remains accountable for ordering, with team input. Preserve current Order fields and stable PBI IDs; themes and illustrative workflow Sprint examples do not supply competing priorities.

**Reason:** The order connects private wardrobe understanding to useful styling, learning and informed improvement, with necessary enabling/quality work. Separate theme orders or backend-driven priorities would fragment that value and dependency logic.

**Authoritative Basis:** [Product Backlog](product-backlog.md), Sections 2–7; [Product Goal](product-goal.md), Sections 3–8; [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 13, 37 and 39–40.

**Backend Developer Impact:** Consult the backlog for actual ordering and hard capability dependencies. Identify technical constraints for team refinement; do not infer a serial implementation schedule or reorder work yourself. All PBIs remain Not Estimated.

### DD-002 — Keep the established first refinement horizon

**Status:** Active

**Decision:** The first refinement horizon remains **PBI-001–PBI-007**. Initial story-level refinement covers PBI-004–PBI-007; PBI-001–PBI-003 remain the related engineering, privacy and mobile-foundation outcomes to clarify. PBI-008–PBI-011 are later refinement candidates when review/learning justifies extending the horizon.

**Reason:** A small horizon gives early private manual-wardrobe value useful detail while preserving progressive refinement. It keeps trust and usability alongside that value without prematurely decomposing the entire MVP.

**Authoritative Basis:** [Product Backlog](product-backlog.md), Sections 5–6 and 10; [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 14 and 31.

**Backend Developer Impact:** The current eight stories are the initial behavioral handoff, not the whole preparation horizon. Foundation items and source quality constraints still matter; refinement states do not establish Sprint readiness. No story for PBI-008 onward is created or selected by this log.

### DD-003 — Retain the actual eight-story decomposition

**Status:** Active

**Decision:** Preserve the current story batch and parent mapping below. This records existing decomposition; it performs no split, merge or new refinement.

| Parent PBI | Existing Stories | Boundary to Preserve |
| --- | --- | --- |
| PBI-004 | [US-AUTH-001 — Register](stories/auth/US-AUTH-001-register-account.md); [US-AUTH-002 — Log in](stories/auth/US-AUTH-002-log-in.md); [US-AUTH-003 — Resume session](stories/auth/US-AUTH-003-resume-session.md); [US-AUTH-004 — Log out](stories/auth/US-AUTH-004-log-out.md) | Four usable access outcomes within one access capability. Session continuity remains one outcome rather than token/layer tasks. |
| PBI-005 | [US-AUTH-005 — Recover access](stories/auth/US-AUTH-005-recover-account-access.md) | Recovery request, instructions, valid completion and return to authentication remain one end-to-end outcome, including failure/recovery behavior. |
| PBI-006 | [US-GAR-001 — Add manually](stories/garment/US-GAR-001-add-garment-manually.md) | Minimum manual entry, review, confirmation and safe saving; rich enrichment and photo/AI routes retain their later PBI boundaries. |
| PBI-007 | [US-WAR-001 — Browse wardrobe](stories/wardrobe/US-WAR-001-browse-current-wardrobe.md); [US-WAR-002 — Inspect garment](stories/wardrobe/US-WAR-002-inspect-owned-garment.md) | Collection overview and selected-item detail are distinct read-only outcomes. Detail consumes available use evidence without implementing future reporting/analytics. |

**Reason:** These slices expose coherent user outcomes while retaining their validation, cancellation and failed/uncertain states. Grouping access in one PBI does not require one story; neither recovery steps nor persistence layers become separate product stories.

**Authoritative Basis:** [Story Index](stories/README.md) and the eight linked stories, Sections 3, 5 and 7–9; [Product Backlog](product-backlog.md), PBI-004–PBI-007 and Sections 9–10; [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Section 14.

**Backend Developer Impact:** Read each selected story's complete AC and source links. Keep required failure/authorization behavior inside its outcome. Backend implementation tasks may support these stories but cannot redefine their product boundaries or prove the whole story Done alone.

### DD-004 — Keep product refinement with PO/BA and team input

**Status:** Active

**Decision:** Future product ordering, story split/merge and refinement-boundary decisions are handled through the PO/BA/team workflow, with AI assistance for source analysis and drafting. The Product Owner retains backlog accountability; PO/BA maintain story behavior and AC. AI assistance does not confer approval authority. Record consequential delivery implications here after the relevant source is updated.

**Reason:** The Backend Developer needs a coherent implementation handoff, while the responsible product roles preserve value, scope and traceability. Technical feasibility should inform those decisions without transferring product ownership to the developer.

**Authoritative Basis:** [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 31, 39–41 and 49; [Product Backlog](product-backlog.md), Sections 2 and 10–11; current [stories](stories/README.md), their scope, traceability and pending-review status.

**Backend Developer Impact:** Raise feasibility, dependency, ambiguity and integration concerns; contribute technical decomposition and capacity information. PO/BA-supported refinement resolves product boundaries. Requirements and architecture changes still use their own authoritative artifacts; this log cannot authorize an implementation shortcut.

### DD-005 — Defer Sprint commitment to the established planning stage

**Status:** Active

**Decision:** None of the current stories is automatically Sprint 1 work. Future selection happens through appropriate refinement and Sprint Planning, using Product Goal, backlog order, dependencies, story readiness, architecture, team capacity and a coherent Sprint Goal. PO and Developers shape the Sprint Goal; Developers select feasible work with the PO and own the Sprint Backlog and implementation plan.

**Reason:** Refined Baseline Draft stories are potential delivery slices, not a capacity-based Sprint commitment. Planning follows the intervening preparation stages and must reflect the actual team and technical baseline.

**Authoritative Basis:** [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 31–32, 38–40 and 46, Steps 24–26; [Product Backlog](product-backlog.md), Sections 10–11; [Product Goal](product-goal.md), Section 8; all current [stories](stories/README.md), Section 11.

**Backend Developer Impact:** Participate as a Developer in feasibility/capacity and Sprint planning. At implementation time, follow the resulting Sprint Goal, selected stories/AC, DoD, relevant architecture/contracts/data design and team-agreed backend tasks. This record supplies no Sprint assignment, estimate or individual task assignment.

### DD-006 — Proceed next to Quality Attribute Analysis

**Status:** Active

**Decision:** After review of the initial stories and this DoD baseline, the next activity is **analysis of all 14 Quality Attributes**. Preserve Workflow Section 46's subsequent order: ASR/architectural drivers; ADD baseline; Logical, Implementation, Deployment and Data Views; major ADRs; initial API/service contracts; Test Strategy; repository/build/CI/environment engineering baseline; Sprint-story readiness refinement; then Sprint Planning and implementation. Relevant detailed sequence/state models follow the workflow's feature-level need criteria.

**Reason:** Requirements already describe the full MVP, while the initial stories and DoD provide near-term outcomes and shared completion expectations. Quality drivers must inform architecture and engineering preparation; a story batch alone cannot select those designs or start a committed Sprint.

**Authoritative Basis:** [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 16–28, 30, 38 and 46, Steps 11–26; [SRS](../03-requirements/SRS.md), Sections 6 and 12; [Definition of Done](definition-of-done.md), Sections 5 and 11; [Product Backlog](product-backlog.md), Section 11.

**Backend Developer Impact:** Next, contribute requirement-linked risks and measurable scenarios to Quality Attribute Analysis under Tech Lead/Architect ownership. Later contribute to contracts, Data View and implementation feasibility within their respective stages. No architecture, API, schema or provider choice is made here; current documents remain drafts for review.

## 5. Superseded Decisions

None currently.

## 6. Backend Developer Handoff Summary

**Current stage:** An initial Product Goal and globally ordered backlog exist, with eight Baseline Draft stories from four PBIs. The project-wide DoD is now drafted for team review. No Sprint Goal, Sprint Backlog or implementation commitment is recorded.

**Next focus:** Review the story boundaries and [DoD](definition-of-done.md), then support Quality Attribute Analysis with source-linked backend risks and validation needs. Product order stays in the backlog; exact behavior stays in requirements and AC. Architecture/contracts, engineering preparation and Sprint selection follow DD-006 and DD-005 at their appropriate stages.

## 7. Status

**Baseline Draft.** Six Active entries summarize supported current guidance, with no Superseded entries. Active means applicable handoff guidance, not formal artifact approval. No upstream artifact, existing story, PBI order, estimate or Sprint assignment is changed by this log.
