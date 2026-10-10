# CapsuleAI — Definition of Done

## 1. Document Control

| Field | Value |
| --- | --- |
| Artifact | Project-wide Definition of Done |
| Version | 0.1.4 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Ownership | Scrum Team; Developers are accountable for adhering to the DoD |
| Process Authority | [CapsuleAI Scrum Development Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 29, 33 and 38–43 |
| Revision Note | v0.1: initial shared completion standard. v0.1.1: synchronized architecture change-propagation/navigation with ADA and explicit selection; completion criteria and acceptance thresholds are unchanged. v0.1.2 (2026-10-10): current navigation acknowledges all 18 ADA recommendations Selected and ADD next; completion criteria remain unchanged. v0.1.3 (2026-10-10): ADD v0.1 exists and Logical View is next; completion criteria remain unchanged. v0.1.4 (2026-10-10): Logical View v0.1 exists and Implementation View is next; completion criteria remain unchanged. |

## 2. Purpose

This Definition of Done (DoD) establishes the common quality bar for work contributing to a usable CapsuleAI Increment. Backend, Mobile, AI and QA contributors apply the same standard to their integrated work. Story Acceptance Criteria define the specific behavior; this document defines the completion conditions shared across that behavior.

The DoD covers completion. Product ordering, estimation, Sprint selection and preparation remain in their respective workflow activities. CapsuleAI's Markdown structure and Baseline Draft status are project conventions.

## 3. Definition

A backlog work item contributing to the CapsuleAI Increment is **Done only when all applicable Acceptance Criteria, common DoD conditions and triggered conditional criteria are satisfied, with inspectable evidence**. The resulting Increment must remain usable with previously completed work.

Apply criteria to the item's actual scope. An Enabler or Quality item demonstrates its defined delivery outcome and applicable quality evidence; it does not need to invent a user-facing feature. Code/build checks can be inapplicable to documentation-only work, with a brief reason. For software changes, a missing engineering foundation is unfinished delivery work, rather than a reason to waive build, testing or integration conditions.

## 4. Common Definition of Done

These conditions apply to every item contributing to the Increment, interpreted for its scope.

| Area | Done Condition |
| --- | --- |
| Acceptance and scope | All applicable Acceptance Criteria pass, and the defined outcome complies with its authoritative requirements and business rules. No unapproved scope or weakened requirement is introduced. |
| Implementation completeness | The scoped behavior is implemented, including required state transitions and failure paths. No placeholder, bypass or unfinished contributor task prevents that outcome from working. |
| Automated tests | Required automated unit, integration and regression tests pass. Tests cover the changed behavior, relevant boundaries and negative cases, and protect affected existing behavior. Tests do not merely repeat implementation details. |
| Integration behavior | Relevant interactions between delivered contributors and dependencies are verified, including required fallback behavior. Mocks alone do not establish that an actual required integration works. |
| Security and authorization | Affected access boundaries and secret handling satisfy the SRS. Relevant unauthorized and cross-account attempts are checked; no protected content or secrets are exposed to unauthorized parties. |
| Privacy and isolation | Affected collection, processing, storage, external exchange and evidence remain purpose-limited and private. User isolation and applicable lifecycle obligations are preserved; MVP personal data is excluded from AI training/improvement and unrestricted sharing. |
| Errors and recovery | Applicable cancellation, rejection, dependency failure and interrupted/uncertain outcomes are verified. Accepted state is protected, outcome review/retry is safe, and unavailable data is not misrepresented as success, emptiness or evaluated zero. |
| Build and CI | Changed software builds reproducibly, and all required configured CI checks pass for the version integrated into the Increment. Failing required checks are resolved; a local success alone does not establish CI success. |
| Review and defects | Code/change review is completed by the appropriate contributor, blocking findings are resolved, and no unresolved Critical defect affects the Increment. Any defect that violates required behavior or another DoD condition blocks Done regardless of its label. |
| Documentation and traceability | Change impact is reviewed. Affected authoritative requirements, rules, interaction models, story references and design documentation accurately reflect the delivered truth, preserving stable IDs. Unaffected documents need no edits. |
| Increment integration | Completed work is integrated into the shared product baseline and verified with existing completed capabilities. The scoped outcome is usable and demonstrable; an isolated backend, mobile or AI task does not establish completion of the whole story. |

Privacy, recovery, accessibility and other applicable constraints accompany each delivered capability. Later cross-MVP verification PBIs extend evidence; their backlog position does not postpone those obligations.

## 5. Conditional Completion Criteria

Review these triggers for each change. Every triggered criterion is mandatory. Record a short applicability reason in existing review/test evidence when useful; unchanged areas require neither artificial document edits nor unrelated test campaigns.

| Trigger | Additional Completion Criteria |
| --- | --- |
| HTTP API or service contract changes | Synchronize the affected OpenAPI/API contract or AI service contract and its consumers. Verify changed success, validation, failure and recovery semantics through relevant contract/integration tests; implementation and documented behavior agree. |
| Database schema changes | Include the executable migration/schema change and relevant migration/integrity tests. Verify the supported transition with existing data; synchronize affected Data View relationships/ownership and related contract documentation where changed. Migration and recovery approach follows the applicable design. |
| Architectural concern or decision changes | Synchronize only affected ASR (when requirement/driver truth changes), Architecture Decision Analysis and explicit Selected Architecture Decisions, then affected ADD, architecture views and consequential ADR records, following workflow change propagation. Relevant detailed models are updated when their underlying behavior changes. A story with no architectural change requires no architecture-document edit. |
| Authorization, sensitive-data use, retention or measurement changes | Verify the affected security/privacy boundaries, minimal disclosure, accepted removal and applicable deletion/retention behavior against the SRS. Measurement preserves core-action outcomes and uses privacy-safe evidence. No new privacy policy or training consent is inferred from user correction/feedback. |
| AI assistance, recommendation, personalization or wardrobe-intelligence behavior changes | Verify affected domain invariants and trustworthy output/state explanations. Meet applicable AI acceptance conditions; distinguish attribute quality from preview usability and preserve confirmation/manual continuity. Recognition-quality claims use the SRS locked benchmark, without training/tuning on its evaluation corpus. |
| Performance, reliability, concurrency or availability behavior is affected | Demonstrate the applicable SRS requirements under their defined validation conditions. Formal timing evidence uses the prescribed start/end boundaries, reference conditions and sampling. Functional concurrency and restart recovery retain their separate acceptance meanings; unrelated paths require no new benchmark. |
| User-facing behavior or localized content changes | Verify affected Vietnamese labels, canonical meanings, supported Android/iOS behavior, usable states and applicable accessibility criteria, including assistive technology and enlarged text. Relevant usability evidence follows SRS Section 12.4; a full representative-user study is required only for the validation scope that calls for it. |
| Runnable software, deployment, runtime configuration or operational behavior changes | Deploy and verify runnable changes in the agreed integration/staging environment when applicable; scoped technical checks use the agreed test environment. Verify dependency/fallback behavior and synchronize only affected configuration, deployment/recovery guidance and privacy-safe operational evidence. |

Existing SRS thresholds and verification conditions remain authoritative. Applicable evidence may be reused only when its tested behavior and validation basis remain valid; affected evidence must be refreshed. The later Test Strategy defines executable coverage and test selection without weakening this completion standard.

## 6. Evidence of Done

Use existing work-item/PR links, review outcomes, passing test/CI results, relevant manual or benchmark results and synchronized source documents. Record the implemented version, validation environment and relevant inputs/conditions sufficiently to reproduce the result. Fixtures and diagnostic evidence protect private data and secrets.

Evidence should connect the item and its stable source IDs to the checks that establish completion. Explain meaningful conditional exclusions briefly. A separate evidence document for every story is unnecessary, and a document audit alone does not establish working software or passed tests.

## 7. Not Done Conditions

Work remains not Done if an applicable AC fails, the integrated outcome is incomplete, a required test/build/CI check fails, required integration is broken, a Critical defect remains, or a triggered migration, contract or documentation update is missing/inconsistent. Unverified security, privacy or recovery obligations also prevent completion. A demo or partial contributor task cannot override these conditions.

## 8. Relationship to User Stories and Sprint Work

**Story Acceptance Criteria + applicable DoD conditions define completion expectations.** AC remain in their [User Stories](stories/README.md); the DoD does not repeat logout, garment, outfit or other story rules.

Refinement and Sprint selection do not mean Done. Developers can track implementation tasks separately, but a story is completed only when its integrated outcome meets the shared standard. A usable partial value-loop Increment is valid; every story need not implement the entire MVP. Work failing the DoD cannot be presented as part of the Done Increment at Sprint Review. Release/deployment decisions retain their separate workflow responsibilities.

## 9. Ownership and Evolution

The Scrum Team uses and evolves this project-wide standard. Developers ensure their work adheres to it; Product Owner, BA, QA and relevant technical contributors help interpret source obligations and review evidence within their roles. DoD satisfaction does not depend on inventing an additional Product Owner approval gate for every task.

Revise the version and rationale when the quality baseline genuinely changes, propagate affected criteria/tests, and preserve upstream authority. Do not silently weaken conditions during a Sprint to count incomplete work as Done.

## 10. Traceability

| Source | Contribution to this DoD |
| --- | --- |
| [Workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 28–29, 33–35 and 38–43 | Test strategy, common completion bar, review/CI, living documentation, ownership and change propagation. |
| [BRD v0.3](../01-business/BRD.md), Sections 17, 19 and 24; [PRD v0.2](../02-product/PRD.md), Sections 21–26 | MVP boundaries, trustworthy experience, privacy, measurement and quality expectations. |
| [SRS v0.3](../03-requirements/SRS.md), Sections 2.3, 4–9 and 12 | Precise integrity/retention, interface, security/privacy, quality, localization, AI, failure and verification obligations. |
| [Business Rules v0.1.2](../03-requirements/business-rules.md); [master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml) and [23 specifications](../03-requirements/use-cases/); [12 current Activity Diagrams](../03-requirements/activity-diagrams/) | Domain invariants, interaction outcomes and complex failure/recovery behavior. |
| [Product Goal v0.1.1](product-goal.md); [Product Backlog v0.1](product-backlog.md), Sections 2 and 7; [initial stories](stories/README.md) | Usable incremental value, quality throughout delivery, item scope and story-specific AC. |
| [Delivery Decisions](delivery-decisions.md) | Current developer handoff and workflow sequence; no replacement for requirements or this completion standard. |

## 11. Status

**Version 0.1.4 — Baseline Draft.** Prepared for Scrum Team review; formal adoption and executed software verification are not claimed. This revision updates architecture-process references without changing the completion standard.

**Original v0.1 handoff:** analysis of all 14 Quality Attributes (Workflow Section 46, Step 13). Current progression is maintained in [Delivery Decisions](delivery-decisions.md), DD-006: [Selected Architecture Decisions](../04-architecture/selected-architecture-decisions.md) retains all 18 human-selected recommendations; [ADD](../04-architecture/ADD.md) and [Logical View v0.1](../04-architecture/views/logical-view.puml) now exist. Review the Logical View proposal; **Implementation View** is next, then the ordered views and ADRs. The historical DoD handoff does not restart completed analysis.
