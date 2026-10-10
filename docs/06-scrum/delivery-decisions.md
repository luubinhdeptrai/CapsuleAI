# CapsuleAI — Delivery Decisions

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Delivery Decision Log |
| Version | 0.1.7 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Primary Audience | Backend Developer / Development Team |
| Ownership | Product Owner / BA, with team input within workflow responsibilities |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) |
| Revision Note | v0.1.7 links Deployment View v0.1, records both existing views as human-accepted and advances DD-006/current navigation to review → Data View. All 18 ADA selections, DD-001–005, stable IDs, product order, story boundaries and Sprint rules remain unchanged. |

| Version | Date | Revision |
| --- | --- | --- |
| 0.1 | 2026-10-08 | Initial six-entry handoff log, including DD-006's original QA preparation handoff. |
| 0.1.1 | 2026-10-08 | Extended DD-006 for ADA/human selection before ADD; current handoff and shifted Step references only. |
| 0.1.2 | 2026-10-10 | Updated the current handoff to ADA v0.2 / 18 proposed analyses; no delivery decision, story/PBI boundary, order or Sprint commitment change. |
| 0.1.3 | 2026-10-10 | Recorded the completed human-selection gate in DD-006/current navigation; Selected Architecture Decisions v0.1 records all 18 choices, ADD is next. No product/refinement/Sprint commitment change. |
| 0.1.4 | 2026-10-10 | Acknowledged ADD v0.1 and advanced DD-006/current navigation to Logical View; no selection, delivery-order, refinement or Sprint commitment change. |
| 0.1.5 | 2026-10-10 | Linked Logical View v0.1 and advanced DD-006/current navigation to Implementation View; no selection, delivery-order, refinement or Sprint commitment change. |
| 0.1.6 | 2026-10-10 | Linked Implementation View v0.1 and recorded explicit human acceptance of the Logical decomposition; review precedes Deployment View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |
| 0.1.7 | 2026-10-11 | Linked Deployment View v0.1 and recorded explicit human acceptance of both Logical and Implementation Views; Deployment View review precedes Data View. Navigation/status only; substantive baseline and historical revisions remain unchanged. |

## 2. Purpose

This lightweight CapsuleAI convention explains delivery decisions that affect developer handoff. It records what the current repository establishes and the responsibility for later refinement; it does not claim historical meetings or approvals.

Exact business/product behavior remains in BRD/PRD, software obligations in SRS, invariants in Business Rules, interactions in Use Cases/Activity Diagrams, ordering in the Product Backlog and near-term behavior in stories/AC. Architecture options/recommendations belong in ADA; explicit human choices belong in Selected Architecture Decisions; design and durable rationale belong in later ADD/views and ADRs. This log references those authorities rather than overriding them; Scrum does not prescribe this file.

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

**Authoritative Basis:** [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 31–32, 38–40 and 46, Steps 26–28; [Product Backlog](product-backlog.md), Sections 10–11; [Product Goal](product-goal.md), Section 8; all current [stories](stories/README.md), Section 11.

**Backend Developer Impact:** Participate as a Developer in feasibility/capacity and Sprint planning. At implementation time, follow the resulting Sprint Goal, selected stories/AC, DoD, relevant architecture/contracts/data design and team-agreed backend tasks. This record supplies no Sprint assignment, estimate or individual task assignment.

### DD-006 — Preserve architecture analysis and human selection before ADD

**Status:** Active

**Decision:** Preserve Workflow Section 46's preparation sequence: Quality Attribute Analysis → ASR/architectural drivers → Architecture Decision Analysis → **explicit human Selected Architecture Decisions** → ADD → Logical View → Implementation View → Deployment View → Data View → ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation.

QA, ASR and ADA drafts exist; [Selected Architecture Decisions](../04-architecture/selected-architecture-decisions.md) records explicit human acceptance of all ADA-001–018 on 2026-10-10. All remain **Selected**, without override/deferral. [ADD](../04-architecture/ADD.md) realizes them. **Deployment View review → Data View** is the current handoff. [Logical View v0.1](../04-architecture/views/logical-view.puml) and [Implementation View v0.1](../04-architecture/views/implementation-view.puml) are human-accepted baselines; [Deployment View v0.1 — Baseline Draft](../04-architecture/views/deployment-view.puml) is available for review. Then follow Data View → ADRs → Detailed API/Data/Sequence/State Design as needed → Test Strategy → Engineering Baseline → Sprint Readiness → Sprint Planning → Implementation. This records explicit human acceptance of both existing views in the Deployment View task on 2026-10-11; Deployment approval, implemented conformance and benchmark success are not claimed.

**History / Extension:** DD-006 v0.1 handed the initial stories/DoD to analysis of all 14 QAs. Version 0.1.1 naturally extends the same preparation/handoff decision with the new workflow stages and current progress. DD-006 remains Active; no prior DD entry is deleted or superseded. On 2026-10-10, v0.1.3 records the completed selection gate and advances the current handoff to ADD; the preparation sequence remains unchanged. Version 0.1.4 acknowledges ADD v0.1 creation and advances to Logical View on 2026-10-10, without changing that sequence. Version 0.1.5 acknowledges Logical View v0.1 and advances to Implementation View on 2026-10-10, preserving the same sequence. Version 0.1.6 records the human-accepted Logical decomposition and Implementation View v0.1 creation on 2026-10-10; its review precedes Deployment View, preserving the sequence. Version 0.1.7 records explicit human acceptance of both existing views and Deployment View v0.1 creation on 2026-10-11; Deployment review precedes Data View, preserving the sequence.

**Reason:** Requirements cover the whole MVP; stories/DoD supply near-term behavior and completion expectations. QA/ASR define the architectural needs, ADA explains alternatives, and the human gate prevents an AI recommendation from silently determining design. The full 41-PBI horizon remains relevant; none of these preparation artifacts commits Sprint work.

**Authoritative Basis:** [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 16–28 (especially 17.2–17.3), 30, 38 and 46, Steps 11–28; [ASR](../04-architecture/ASR.md), Section 13; [Architecture Decision Analysis](../04-architecture/architecture-decision-analysis.md), Sections 4/8/11; [Selected Architecture Decisions](../04-architecture/selected-architecture-decisions.md), Sections 2–6; [ADD](../04-architecture/ADD.md), Sections 12–14; [SRS](../03-requirements/SRS.md), Sections 6 and 12; [Definition of Done](definition-of-done.md), Sections 5 and 11; [Product Backlog](product-backlog.md), Section 11.

**Backend Developer Impact:** Read the selected record, ADD tactics/risks/traceability, accepted [Logical View v0.1](../04-architecture/views/logical-view.puml) and [Implementation View v0.1](../04-architecture/views/implementation-view.puml), plus [Deployment View v0.1 — Baseline Draft](../04-architecture/views/deployment-view.puml), before Data View; preserve all selected directions and authoritative behavior. Contribute source-boundary review, runtime/model/data feasibility and operating evidence through the ordered views under Architect/Tech Lead responsibility. Product decisions remain with PO/BA/team; detailed contracts/design and engineering follow in order. This log selects no provider, API or schema.

## 5. Superseded Decisions

None currently.

## 6. Backend Developer Handoff Summary

**Current stage:** Product Goal, the globally ordered 41-PBI backlog, eight stories and project-wide DoD remain drafts for review. QA/ASR and ADA cover the full MVP; the separate selection record retains all 18 selections. ADD and accepted [Logical View v0.1](../04-architecture/views/logical-view.puml) and [Implementation View v0.1](../04-architecture/views/implementation-view.puml) exist; [Deployment View v0.1 — Baseline Draft](../04-architecture/views/deployment-view.puml) is available for review. Data View, ADRs, Sprint Goal/Backlog and implementation commitment remain downstream.

**Next focus:** review [Deployment View v0.1 — Baseline Draft](../04-architecture/views/deployment-view.puml) → **Data View**, then ADRs. Both existing views are explicitly human-accepted in the current instruction; other review/evidence needs remain. Earlier handoffs do not restart completed preparation. Product order stays in the backlog and behavior in requirements/AC. Detailed design, Test Strategy, engineering/readiness and Sprint Planning follow DD-006/DD-005.

## 7. Status

**Version 0.1.7 — Baseline Draft.** Six Active entries, no Superseded entries. DD-006/current navigation reflects Deployment View v0.1 review and Data View next; all 18 selections and DD-001–005 remain unchanged. Active means applicable guidance, not formal approval. No product/requirement, story/PBI boundary, order, estimate or Sprint assignment changes.
