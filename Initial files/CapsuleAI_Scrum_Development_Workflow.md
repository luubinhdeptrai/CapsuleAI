# CapsuleAI — Scrum-Based Software Development Workflow

> **Purpose**  
> Tài liệu này là **playbook chính thức** hướng dẫn team CapsuleAI triển khai đồ án theo **Scrum/Agile**, với toàn bộ tài liệu quản lý dự án, requirements và architecture được version-control trực tiếp trong codebase.
>
> Tài liệu được thiết kế để:
>
> - giúp team biết **nên làm gì trước, làm gì sau**;
> - xác định **tài liệu nào cần tạo, ai tạo, khi nào cập nhật**;
> - connect Business → Product → Requirements → Architecture → Scrum → Automated Test + Implementation → Release;
> - mô phỏng workflow của một software team chuyên nghiệp nhưng tránh bureaucracy không cần thiết;
> - giúp AI model đọc repository và hiểu đúng workflow cần tuân theo;
> - maintain a source-driven architecture baseline: **14 Quality Attributes → ASR → Architecture Decision Analysis → Selected Architecture Decisions → ADD + 4 Architecture Views**, followed by engineering preparation and iterative Sprint delivery;
> - yêu cầu các sơ đồ UML như **Use Case Diagram, Activity Diagram, Sequence Diagram, State Diagram, Deployment Diagram...** phải được biểu diễn bằng **UML**, ưu tiên lưu source bằng **PlantUML** trong repository.
>
> **Important:** Scrum không yêu cầu Jira. CapsuleAI **không dùng Jira**. Product Backlog, User Stories, Sprint Backlog, Bugs, Spikes, Sprint Review và Retrospective được quản lý bằng Markdown ngay trong repository.
>
> **Process revision — 2026-10-08:** Added comparative Architecture Decision Analysis and explicit human selection before ADD; synchronized reusable order, ownership, traceability and change propagation. Existing numbered Sections 1–49 remain stable; Section 46 action numbers from 15 onward shift by two. This process revision does not approve architecture or change product requirements.
>
> **Process revision — 2026-10-20:** Removed the mandatory standalone ADR phase; ADA and Selected Architecture Decisions retain current decision authority/history. Test Strategy and Engineering Baseline precede Sprint 1 implementation. Refinement prepares candidates, Sprint Planning selects work, and one story-specific JIT / Just-Enough Detailed Design loop occurs inside Sprint execution with automated testing. Sections 1–49 remain stable; Section 46 actions 22–29 now reflect this order. No product, selected architecture, backlog order or Sprint scope changes.
>
> Numbered sections describe artifact guidance as well as execution order: Sections 25–27 are not mandatory pre-Sprint phases. Sections 1, 38 and 46–47 define the preparation/delivery timing.
>
> Examples, sample IDs, stack names and Sprint outlines in this playbook illustrate the method. Current canonical requirements and the ordered Product Backlog govern CapsuleAI behavior/delivery; selected architecture comes only from the explicit selection record. Reusable workflow guidance never mandates a database, runtime, AI topology or cloud provider.

---

# 1. Development Philosophy

CapsuleAI uses iterative, incremental Scrum delivery with enough system-wide architecture to guide implementation.

> **Architecture early, Detailed Design late.**
>
> Stable system boundaries are decided early; implementation details are decided as late as responsibly possible, when the Sprint actually needs them.

The system-wide baseline covers requirements, Business Rules, Use Cases/Activities, the Product Backlog and initial stories/AC, QA/ASR, architecture decision analysis and selection, ADD and the four architecture views. It establishes responsibilities, authority, constraints and traceability without freezing implementation details for the whole MVP.

```text
System-wide Requirements / Backlog / Architecture Baseline
        ↓
Test Strategy
        ↓
Pre-Sprint Engineering Baseline
        ↓
Backlog Refinement
        ↓
Sprint Planning: Goal + selected PBIs + initial Sprint Backlog
        ↓
Sprint Execution: for each selected Story
  JIT Detailed Design → Automated Test + Implementation
  → Refactor → Integration Verification → PR / CI / Review → Done
        ↓
Working Increment
        ↓
Sprint Review → Sprint Retrospective
        ↓
Update Backlog / Requirements / Architecture where truth changes
        ↓
Next Sprint
```

Refinement continues as an ongoing activity; the sequence identifies the preparation needed before selection, not a once-only refinement phase. Sprint Planning initiates the Sprint. Detailed Design belongs inside execution, after selection, and repeats in small steps with tests and implementation.

Avoid both designing and freezing the whole MVP before coding and starting feature implementation without sufficient architecture and story clarity. Requirements, architecture, contracts, schemas and diagrams evolve with working software.

---

# 2. Master Traceability Chain

Important engineering work must trace back to its purpose. This chain connects authorities and delivery evidence; it does not require a new document for every link or every Story.

```text
Business Problem → BRD → PRD → SRS → Business Rules
        ↓
Use Cases / Activity Diagrams
        ↓
Product Goal → Product Backlog → Initial User Stories / Acceptance Criteria
        ↓
14 Quality Attribute Analysis → ASR
        ↓
Architecture Decision Analysis → Selected Architecture Decisions
        ↓
ADD → Logical View → Implementation View → Deployment View → Data View
        ↓
Test Strategy → Engineering Baseline
        ↓
Backlog Refinement → Sprint Planning / Sprint Backlog
        ↓
Sprint Execution, per selected Story:
JIT Detailed Design, only where necessary
→ Automated Test + Implementation → Refactor / Integration Verification
→ Pull Request / CI / Code Review → Done
        ↓
Working Increment → Release / Deployment when appropriate
        ↓
Monitoring / Review / Retrospective / Feedback / Bugs
        ↓
Product Backlog and affected authoritative documents
```

ADA records decision problems, alternatives, trade-offs, recommendations and confidence. Selected Architecture Decisions records authoritative selections, rationale, developer impact and supersession history. ADD and the four views realize the selected architecture. These artifacts already preserve the current decision baseline; separate ADR files are optional for future consequential evolution, not a mandatory link before implementation.

Detailed API/Data/Sequence/State artifacts are conditional, story-specific outputs of Sprint execution (Section 26). They are not a whole-MVP prerequisite for Test Strategy, Engineering Baseline, refinement or Sprint Planning.

---

# 3. Recommended Repository Structure

```text
CapsuleAI/
│
├── README.md
│
├── docs/
│   │
│   ├── 01-business/
│   │   └── BRD.md
│   │
│   ├── 02-product/
│   │   └── PRD.md
│   │
│   ├── 03-requirements/
│   │   ├── SRS.md
│   │   ├── business-rules.md
│   │   │
│   │   ├── use-cases/
│   │   │   ├── use-case-diagram.puml
│   │   │   ├── UC-01-authentication.md
│   │   │   ├── UC-02-add-garment.md
│   │   │   ├── UC-03-manage-wardrobe.md
│   │   │   ├── UC-04-generate-outfit.md
│   │   │   └── UC-05-gap-analysis.md
│   │   │
│   │   └── activity-diagrams/
│   │       ├── ACT-01-onboarding.puml
│   │       ├── ACT-02-garment-ingestion.puml
│   │       ├── ACT-03-outfit-generation.puml
│   │       └── ACT-04-gap-analysis.puml
│   │
│   ├── 04-architecture/
│   │   ├── quality-attribute-analysis.md
│   │   ├── ASR.md
│   │   ├── architecture-decision-analysis.md
│   │   ├── selected-architecture-decisions.md
│   │   ├── ADD.md
│   │   │
│   │   ├── views/
│   │   │   ├── logical-view.puml
│   │   │   ├── implementation-view.puml
│   │   │   ├── deployment-view.puml
│   │   │   └── data-view.puml
│   │   │
│   │   ├── adr/                         # optional future consequential changes only
│   │   │   └── ADR-xxx-consequential-change.md
│   │   │
│   │   ├── sequence-diagrams/
│   │   │   └── SEQ-01-garment-processing.puml
│   │   │
│   │   └── state-diagrams/
│   │       └── STATE-01-garment-processing.puml
│   │
│   ├── 05-api/
│   │   ├── api-conventions.md
│   │   └── ai-service-contract.md
│   │
│   ├── 06-scrum/
│   │   ├── product-goal.md
│   │   ├── product-backlog.md
│   │   ├── definition-of-done.md
│   │   │
│   │   ├── stories/
│   │   │   ├── US-001-register.md
│   │   │   ├── US-002-login.md
│   │   │   ├── US-003-upload-garment.md
│   │   │   └── ...
│   │   │
│   │   ├── bugs/
│   │   │   └── BUG-001-*.md
│   │   │
│   │   ├── spikes/
│   │   │   └── SPIKE-001-*.md
│   │   │
│   │   └── sprints/
│   │       ├── sprint-01/
│   │       │   ├── sprint-goal.md
│   │       │   ├── sprint-backlog.md
│   │       │   ├── review.md
│   │       │   └── retrospective.md
│   │       └── ...
│   │
│   ├── 07-testing/
│   │   ├── test-strategy.md
│   │   └── regression-checklist.md
│   │
│   └── 08-operations/
│       ├── deployment.md
│       ├── rollback.md
│       ├── monitoring.md
│       └── postmortems/
│
├── backend/
├── mobile/
└── ai-service/
```

Không tạo toàn bộ file ngay ngày đầu. Các file được tạo khi workflow thực sự cần chúng.

The tree is a target structure, not evidence that every artifact exists. The selected-decision record follows actual human selection; ADD and views follow that gate. No ADR directory/files are needed for the current baseline. Detailed API/data/Sequence/State artifacts are conditional Sprint outputs for selected Stories, not documents to create for the whole MVP in advance. Code directories do not establish an implemented baseline.

---

# 4. Diagram Standard

## 4.1 General Rule

Các diagram kỹ thuật và behavioral diagram trong CapsuleAI phải được lưu dưới dạng **source text**, ưu tiên **PlantUML**, để:

- version-control bằng Git;
- dễ review thay đổi;
- AI model có thể đọc và chỉnh sửa;
- không phụ thuộc file ảnh thủ công;
- có thể render tự động trong CI hoặc editor.

## 4.2 UML Diagrams

Các diagram sau phải dùng **UML notation**:

- Use Case Diagram;
- Activity Diagram;
- Sequence Diagram;
- State Machine Diagram;
- Deployment Diagram;
- Package/Component Diagram khi dùng cho Logical hoặc Implementation View.

Ví dụ file:

```text
ACT-02-garment-ingestion.puml
```

Không dùng flowchart tùy ý nếu mục đích là một UML Activity Diagram.

## 4.3 Data View

The baseline Data View shows logical information concepts, accountable owners, major relationships, stores and boundaries. Use a conceptual UML/Class-style or other suitable PlantUML representation without requiring physical tables/columns.

Physical data detail and an ERD, where useful, belong to the selected Story's JIT Detailed Design during Sprint execution (Section 26). A logical Data View is not a requirement to complete the whole MVP's relational schema before Sprint Planning.

---

# 5. Phase 0 — Consolidate Existing Sources

CapsuleAI hiện đã có Proposal, Project Outline, PRD và Implementation Summary.

Không viết lại từ số 0.

Mục tiêu của Phase 0:

1. xác định tài liệu nào là source;
2. loại bỏ mâu thuẫn;
3. đánh dấu quyết định chưa chốt;
4. chuẩn hóa terminology;
5. đưa tài liệu cần duy trì lâu dài vào repository.

## Output

```text
docs/01-business/BRD.md
docs/02-product/PRD.md
docs/03-requirements/SRS.md
```

## Important Decisions to Resolve or Track

Ví dụ:

- database cuối cùng;
- JWT authentication approach;
- core runtime, AI/CV execution boundary and interaction model;
- CV accuracy target;
- image-processing latency;
- Wardrobe Multiplier formula;
- wardrobe completion formula;
- user-preference learning scope;
- style/occasion/body-shape scope.

Product/domain decisions are resolved in their owning requirements artifacts. Historical technology suggestions remain candidates until current constraints establish otherwise. Architecture problems go through ASR → Architecture Decision Analysis → explicit selection (Sections 17.2–17.3). Critical choices block ADD; safe Important deferrals need a stable boundary, owner and resolution point before dependent design. Later implementation details need not block the initial baseline.

---

# 6. Phase 1 — Business Requirements Document (BRD)

## Purpose

BRD trả lời:

```text
WHY are we building CapsuleAI?
WHAT business/user problem must be solved?
WHO are the stakeholders?
WHAT is in scope?
WHAT is out of scope?
```

## Primary Owner

BA / Product-oriented team member.

## Reviewers

- Product Owner;
- Stakeholders;
- Tech Lead chỉ review feasibility ở mức cao.

## Recommended Contents

```text
1. Business Context
2. Problem Statement
3. Product Vision
4. Stakeholders
5. Target Users / Personas
6. Business Goals
7. Success Metrics
8. High-Level Business Requirements
9. Scope
10. Out of Scope
11. Assumptions
12. Constraints
13. Business Risks
```

BRD không chứa controller, database schema hoặc technology decisions.

---

# 7. Phase 2 — Product Requirements Document (PRD)

## Purpose

PRD chuyển business needs thành product capabilities.

```text
BRD
 ↓
PRD
```

PRD trả lời:

```text
WHAT product experiences/features will solve the problem?
WHAT value does each feature provide?
WHAT are the main user journeys?
```

## CapsuleAI Core Product Pillars

```text
1. AI Digital Closet
2. Outfit Recommendation Engine
3. Strategic Shopping / Wardrobe Multiplier
```

## Recommended Contents

```text
1. Product Summary
2. Product Goal
3. Personas
4. Core User Journeys
5. Feature Definitions
6. Feature-level Acceptance Criteria
7. MVP
8. Out of Scope
9. Product Assumptions
10. Product Roadmap
```

PRD không thay thế SRS.

---

# 8. Phase 3 — Software Requirements Specification (SRS)

## Purpose

SRS chuyển product expectations thành **precise, testable software requirements**.

```text
PRD
 ↓
SRS
```

## Requirement IDs

Dùng stable IDs:

```text
FR-AUTH-001
FR-WAR-001
FR-AI-001
FR-OUT-001
FR-GAP-001

NFR-PERF-001
NFR-SEC-001
NFR-REL-001
...
```

## Recommended SRS Contents

```text
1. Introduction
2. System Scope
3. Actors
4. Functional Requirements
5. Data Requirements
6. External Interface Requirements
7. Non-Functional Requirements
8. Constraints
9. Assumptions
10. Dependencies
11. Traceability
```

## Example

```text
FR-WAR-001
An authenticated user shall be able to upload a garment image.

FR-AI-001
The system shall extract garment attributes from a qualified garment image.

NFR-PERF-001
The garment-processing pipeline shall complete within the approved latency target.

NFR-SEC-001
Protected backend endpoints shall require valid JWT authentication.
```

---

# 9. Phase 4 — Business Rules

Tạo:

```text
docs/03-requirements/business-rules.md
```

Business Rules chứa những invariant hoặc domain rule không nên bị chôn trong code.

Ví dụ:

```text
BR-OUT-001
Every generated outfit must contain exactly one Top.

BR-OUT-002
Every generated outfit must contain exactly one Bottom.

BR-OUT-003
Every generated outfit must contain exactly one Footwear.

BR-OUT-004
Outerwear is optional and weather-dependent.

BR-OUT-005
Maximum one patterned garment is allowed per outfit.
```

Business Rules sẽ được dùng bởi:

```text
Use Cases
User Stories
Acceptance Criteria
Domain Logic
Automated Tests
```

---

# 10. Phase 5 — Use Case Analysis

## 10.1 Use Case Diagram

Tạo:

```text
docs/03-requirements/use-cases/use-case-diagram.puml
```

Phải sử dụng **UML Use Case Diagram**.

Use Case Diagram trả lời:

```text
WHO interacts with CapsuleAI?
WHAT goals can each actor achieve?
```

Không thể hiện controller, database hoặc API endpoint.

## 10.2 Core Use Case Specifications

Chỉ viết chi tiết cho core use cases.

Recommended:

```text
UC-01 Authentication
UC-02 Add Garment
UC-03 Manage Wardrobe
UC-04 Generate Outfit
UC-05 Shuffle Outfit
UC-06 Wear Outfit Today
UC-07 Gap Analysis / Strategic Shopping
```

Template:

```text
# UC-02 — Add Garment

Actor:
Authenticated User

Goal:
Digitize a physical garment.

Preconditions:
...

Trigger:
...

Main Flow:
1.
2.
3.

Alternative Flows:
...

Exception Flows:
...

Postconditions:
...

Related Requirements:
FR-...

Related Business Rules:
BR-...
```

---

# 11. Phase 6 — Activity Diagrams

Activity Diagram mô tả **workflow / behavior / decisions** của một process.

Phải sử dụng **UML Activity Diagram**, ưu tiên PlantUML.

Recommended diagrams:

```text
ACT-01 Onboarding
ACT-02 Garment Ingestion
ACT-03 Outfit Generation
ACT-04 Gap Analysis
```

Không cần Activity Diagram cho mọi CRUD đơn giản.

## Example Logical Flow — Garment Ingestion

```text
Start
 ↓
Select garment image
 ↓
Validate image
 ↓
[Invalid] → Show validation error → End
 ↓ [Valid]
Upload image
 ↓
Store original image
 ↓
Invoke AI processing
 ↓
AI successful?
 ├─ No → Manual entry / fallback
 └─ Yes → Extract attributes
            ↓
        Show attributes
            ↓
        User confirms/edits
            ↓
          Save garment
            ↓
            End
```

Khi implement, AI model phải chuyển flow này thành **UML Activity Diagram syntax**, không phải ASCII flowchart.

---

# 12. Phase 7 — Product Goal

Tạo:

```text
docs/06-scrum/product-goal.md
```

Product Goal là long-term objective của Scrum Product.

Ví dụ:

```text
Enable users to digitize their wardrobe, receive context-aware
outfit recommendations, and identify high-utility clothing purchases
through one integrated mobile experience.
```

Product Goal không phải Sprint Goal.

---

# 13. Phase 8 — Product Backlog

Tạo:

```text
docs/06-scrum/product-backlog.md
```

Product Backlog là **ordered, evolving list of work**.

Không dùng Jira.

Recommended columns:

| ID | Type | Title | Priority | Estimate | Status |
|---|---|---|---|---:|---|

Types:

```text
Story
Bug
Spike
Technical Work
Technical Debt
```

Example:

```text
US-001 Register account
US-002 Login with JWT
US-003 Upload garment
US-004 Analyze garment with AI
US-005 Confirm AI attributes
US-006 View wardrobe
SPIKE-001 Evaluate segmentation model
```

Product Backlog được update xuyên suốt project.

---

# 14. Phase 9 — User Stories

Mỗi User Story quan trọng có file riêng:

```text
docs/06-scrum/stories/US-003-upload-garment.md
```

Template:

```text
# US-003 — Upload Garment Image

## Epic / Capability
Digital Wardrobe

## User Story

As an authenticated user,
I want to upload a garment image,
so that I can digitize it into my wardrobe.

## Related Requirements

- FR-WAR-001
- FR-AI-001

## Related Use Cases

- UC-02 Add Garment

## Related Activity Diagram

- ACT-02 Garment Ingestion

## Acceptance Criteria

### AC-01
Given ...
When ...
Then ...

### AC-02
Given ...
When ...
Then ...

## Relevant Architecture

- ASR-...
- ADA-... (when relevant and available)
- Selected Architecture Decisions: ADA-... selection
- ADD §...
- Existing relevant SEQ-/STATE-... only when needed for selected Sprint work
- Optional future ADR-... only when justified under Section 25

## Implementation Checklist

- [ ] ...
- [ ] ...

## Definition of Done

Uses project-wide Definition of Done.
```

Acceptance Criteria nằm trong User Story file.

Không cần `acceptance-criteria.md` riêng cho từng Story.

---

# 15. Bugs and Spikes

## Bugs

```text
docs/06-scrum/bugs/BUG-xxx.md
```

Template:

```text
Title
Related Story
Environment
Severity
Steps to Reproduce
Expected Result
Actual Result
Evidence
Resolution
```

## Spikes

```text
docs/06-scrum/spikes/SPIKE-xxx.md
```

Spike dùng khi team cần nghiên cứu để giảm uncertainty.

Example:

```text
SPIKE-001 — Evaluate RMBG vs U2-Net
```

A bounded Spike can inform feasibility or an ADA comparison/re-analysis. Explicit human selection precedes affected ADD/views if an architecture choice changes; an optional future ADR is justified only under Section 25. A Spike result is not automatic selection or a full production-design packet.

---

# 16. Phase 10 — 14 Quality Attribute Analysis

Tạo:

```text
docs/04-architecture/quality-attribute-analysis.md
```

Team phải xem xét đủ 14 Quality Attributes:

1. Performance
2. Availability
3. Reliability
4. Security
5. Scalability
6. Maintainability
7. Flexibility
8. Reusability
9. Interoperability
10. Conceptual Integrity
11. Usability
12. Testability
13. Supportability
14. Manageability

Không cần tạo nhiều scenario cho tất cả 14.

Mục tiêu:

```text
Consider all 14
      ↓
Assess relevance
      ↓
Prioritize
      ↓
Select architecturally significant ones
      ↓
Promote into ASRs / QA scenarios
```

Recommended table:

| Quality Attribute | Relevance | Evidence | Priority | ASR? |
|---|---|---|---|---|

Example initial CapsuleAI candidates:

```text
Performance          High
Reliability          High
Security             High
Maintainability      High
Flexibility          High
Interoperability     High
Usability            High
Testability          High
Reusability          High/Medium
Availability         Medium
Scalability          Medium
Conceptual Integrity Medium
Supportability       Medium
Manageability        Medium
```

Priority phải được xác nhận từ actual requirements, không chọn chỉ vì muốn architecture phức tạp.

---

# 17. Phase 11 — Architecturally Significant Requirements (ASR)

Tạo:

```text
docs/04-architecture/ASR.md
```

## Purpose

ASR chứa **subset của Functional + Non-Functional Requirements** có ảnh hưởng đáng kể tới architecture.

Một requirement là ASR nếu nó gây pressure lên một hoặc nhiều yếu tố:

```text
module boundaries
runtime topology
data ownership
integration style
deployment
security model
failure handling
quality-attribute tactics
```

## Example

```text
NFR:
Garment processing must finish within the accepted latency target.

Architectural significance:
Affects image size limits, inference service, model runtime,
timeout strategy, deployment resources and sync/async behavior.

→ ASR
```

Trong khi:

```text
FR:
User can rename a garment.

→ usually NOT ASR
```

## Recommended ASR Structure

```text
1. Purpose
2. Architectural Drivers
3. Architecturally Significant Functional Areas
4. Significant Quality Attributes
5. Architectural Constraints
6. Cross-Cutting Concerns
7. Traceability
8. Out-of-Scope Architectural Concerns
```

## Architectural Driver Table

| ID | Driver | Source | Architectural Impact |
|---|---|---|---|

Example:

```text
AD-01
Driver:
Garment-processing latency

Source:
NFR-PERF-...

Architectural Impact:
Requires analysis of execution/resource boundaries, complete-result
latency, dependency failure and reviewable outcomes.
Runtime/service, sync/async and deployment choices remain open.
```

---

## 17.1 ASR Handoff and Boundaries

ASR answers **what architecture must be capable of satisfying**. Requirements, driver evidence and established constraints are retained; possible databases, frameworks, services, caches and topology are not requirements merely because they appear in historical proposals.

The next stage is comparative analysis, followed by explicit human selection. ADD does not silently convert ASR pressure or an AI recommendation into a chosen solution.

## 17.2 Phase 11a — Architecture Decision Analysis

Future/reusable location:

```text
docs/04-architecture/architecture-decision-analysis.md
```

**Owner:** Architect / Tech Lead with Developer input. Backend Developers contribute feasibility, implementation and operating implications, and actual relevant experience. They do not own product scope or backlog ordering.

**Purpose:** expose major architecture problems arising from ASRs, quality trade-offs and current constraints; compare realistic alternatives; recommend the strongest supported option while keeping it non-binding. It answers **what problems need solutions, what credible options exist, and why an option is recommended**.

Use stable **ADA-001, ADA-002, ...** identities, distinct from ADR IDs. Each analysis records:

- Problem and why a decision is needed now.
- Related ASRs, actual QA significance and established constraints.
- Source-derived evaluation criteria; no unsupported numerical weights.
- Credible options, benefits, disadvantages, risks and development/operating implications.
- Compact comparative matrix.
- Exactly one recommendation where evidence suffices, rationale and High/Medium/Low confidence with material uncertainty.
- **Decision Status: Proposed — Awaiting Selection**.
- Conditional consequences if selected and deferred details.

Classify importance as **Critical Before ADD**, **Important Before ADD**, or **Defer to Just-In-Time Detailed Design**. Critical choices determine a coherent major structure/authority. Important choices can be bounded only where stable abstractions permit safe progress. Low-level implementation details stay downstream; do not invent alternatives or infrastructure merely to fill a checklist.

Historical stack preferences and documentation references are candidate context unless canonical current sources impose them. Prefer sufficient simplicity against the actual ASRs, supported workload and team evidence. Recommendation confidence is not approval, test success or human preference.

**Exit to review:** traceable comparisons, explicit uncertainties/dependencies and a compact proposed set. No ADD, view, selected record or ADR is generated just because analysis is complete.

## 17.3 Phase 11b — Selected Architecture Decisions

Future/reusable location:

```text
docs/04-architecture/selected-architecture-decisions.md
```

**Purpose:** provide the explicit human decision gate between analysis and ADD. It answers **which analyzed directions have actually been chosen, overridden or safely deferred**.

**Owner:** responsible human Architect / Tech Lead, with relevant engineering review and Product/BA consultation for requirement implications. An AI can prepare analysis/record wording but cannot invent human approval or selection.

Selection may accept the recommendation, choose another analyzed option with reasons, or defer an Important choice safely. A material unexamined alternative returns to ADA analysis first. Record the selecting human/review date and any uncertainty relevant to the choice.

Lightweight future format:

| ADA ID | Selected Option | Selection Basis | Backend Impact | Status |
| --- | --- | --- | --- | --- |
| ADA-... | An explicitly chosen analyzed option, or bounded deferral | Human selection/override rationale and source evidence | Responsibilities, authority, integration or operation affected | Selected / Deferred / Superseded |

For a deferral, record the stable boundary, impact/risk, responsible owner, needed evidence and resolution point before dependent design. For supersession, retain prior selection/history and identify its replacement. Preserve ADA IDs and link to the analysis rather than duplicating every alternative.

**ADD entry gate:** all Critical Before ADD choices are explicitly Selected, and Important choices are either Selected or safely bounded with documented deferrals. A Critical unresolved choice blocks the ADD baseline. Selection does not claim software verification or approval of the upstream draft documents.

Together with ADA, this is the authoritative current architecture decision record: preserve selected options, rationale, developer impact and supersession history without duplicating the baseline in separate ADR files. Standalone ADRs are optional only for significant future evolution (Section 25), never one per ADA or a prerequisite to implementation. Create/update selected rows only after actual human selection.

---

# 18. Phase 12 — Architecture Description Document (ADD)

Tạo:

```text
docs/04-architecture/ADD.md
```

ADD answers **how the architecture is structured given the ASRs and explicitly selected architecture directions**. Its inputs are requirements/QA/ASR evidence, ADA comparisons and the human Selected Architecture Decisions record.

Before creating an ADD baseline, satisfy Section 17.3's entry gate. Link each consequential design direction to its ASR(s), ADA ID and selection record. Represent safe Important deferrals as bounded open concerns; do not fill them with an assumed runtime, database or topology. ADD owns structure, responsibilities, runtime collaboration and tactics, not the pre-ADD selection gate.

Recommended structure:

```text
1. Introduction
2. Architecture Scope
3. Design Constraints
4. Architectural Drivers
5. Quality Attribute Requirements
6. Architectural Decisions
7. Architectural Representation
   7.1 Logical View
   7.2 Implementation View
   7.3 Deployment View
   7.4 Data View
8. Cross-Cutting Concerns
9. Architecture Risks
10. Trade-offs
11. Traceability
```

---

# 19. Quality Attribute Scenarios in ADD

QA analysis owns the source-linked six-part scenario bodies and existing acceptance measures. ADD references those scenarios and explains selected architectural tactics/realization; it does not create competing thresholds. For an important QA, the combined presentation may show:

```text
Stimulus Source
Stimulus
Environment
Artifact
Response
Response Measure
Architectural Tactics
```

Example:

```text
QA-P-01 — Garment Processing Latency

Stimulus Source:
Authenticated mobile user

Stimulus:
Uploads a qualified garment image

Environment:
Normal operational load

Artifact:
Mobile → selected business runtime → selected analysis capability

Response:
Both usable processed preview and reviewable proposals are available

Response Measure:
p95 within approved latency target

Architectural Tactics:
- bounded upload size
- image preprocessing
- model warm-up
- timeout policy
- AI service isolation
```

Quality attribute phải **measurable/testable**, không dùng statement mơ hồ như:

```text
The system should be fast.
```

---

# 20. The Four Required Architecture Views

CapsuleAI bắt buộc duy trì bốn architecture views chính như Food Delivery:

```text
1. Logical View
2. Implementation View
3. Deployment View
4. Data View
```

Chúng nằm trong:

```text
docs/04-architecture/views/
```

và được tham chiếu từ `ADD.md`.

---

# 21. Logical View

File:

```text
logical-view.puml
```

## Purpose

Trả lời:

```text
What are the major logical/domain responsibilities?
How are they separated?
Who depends on whom?
```

Không tập trung vào deployment hoặc physical database.

Possible CapsuleAI logical areas:

```text
Identity
User Profile
Wardrobe
Garment Processing
Weather
Outfit Recommendation
Outfit History / Feedback
Catalog
Gap Analysis / Wardrobe Multiplier
```

## Diagram Rule

Dùng UML Package Diagram hoặc Component Diagram bằng PlantUML.

Logical View nên ở mức domain/capability.

---

# 22. Implementation View

File:

```text
implementation-view.puml
```

## Purpose

Trả lời:

```text
How are logical responsibilities implemented in source code?
What modules/packages/components exist?
How do modules depend on each other?
Where are adapters/repositories/interfaces?
```

Illustrative mapping only, conditional on explicit runtime/boundary selections:

```text
Spring Boot Backend
├── auth
├── user
├── wardrobe
├── ai
├── weather
├── outfit
├── history
├── catalog
└── recommendation

Python AI Service
├── api
├── preprocessing
├── segmentation
├── classification
├── color
├── pattern
└── model
```

## Diagram Rule

Dùng UML Component / Package notation.

Không biến diagram thành class diagram chứa mọi class.

---

# 23. Deployment View

File:

```text
deployment-view.puml
```

## Purpose

Trả lời:

```text
Where does each runtime component execute?
How do runtime nodes communicate?
Which infrastructure/external services exist?
```

Candidate node kinds, conditional on the selected runtime/persistence/storage direction:

```text
Mobile Device
Selected Business Runtime
Selected Analysis Runtime (if separate)
Selected Primary Store
Private Media Resource (if selected)
Weather Provider
Reverse Proxy / Cloud Platform if applicable
```

## Diagram Rule

Dùng **UML Deployment Diagram** bằng PlantUML.

Deployment View phải phân biệt:

```text
software component
runtime process/container
deployment node
external service
data store
network protocol
```

---

# 24. Data View

File:

```text
data-view.puml
```

## Purpose

Trả lời:

```text
What data exists?
Who owns it?
How are entities/documents related?
Where is data stored?
How does data flow across boundaries?
```

Possible data concepts:

```text
User
Profile
Garment
GarmentImage
GarmentAttributes
Outfit
OutfitItem
OutfitHistory
Feedback
StapleCatalogItem
Recommendation
```

Object storage:

```text
Original Garment Images
Processed Transparent Images
```

Data View phải thể hiện ownership/boundary chứ không chỉ field.

Keep this system-wide view at logical ownership/relationship/store level. A relational ERD or document embedding/reference detail may be added only where selected Sprint work needs JIT physical data design (Section 26); a complete MVP schema is not a baseline gate.

---

# 25. Optional ADRs for Future Architecture Evolution

The current architecture decision records are already authoritative:

| Artifact | Responsibility |
| --- | --- |
| `architecture-decision-analysis.md` | Decision problems, alternatives, trade-offs, recommendations, confidence and evidence gaps. |
| `selected-architecture-decisions.md` | Selected options, human selection rationale, developer impact, bounded deferrals and retained supersession history. |
| ADD + Logical / Implementation / Deployment / Data Views | Realization of the selected architecture. |

**No separate ADR phase is required for the current baseline or before implementation. There is no one-to-one ADA-to-ADR requirement.** Do not duplicate the 18 selected decisions into new files merely to complete a checklist.

A standalone ADR is optional and may be introduced only when a future consequential architectural evolution/change benefits from a dedicated durable historical record. For example, measured recommendation latency might justify reconsidering the selected no-cache direction; if a cache is later analyzed and explicitly selected, an ADR may help preserve that transition. This example selects no cache or other technology.

When justified, keep an ADR concise: Status; Context and evidence; Decision; Alternatives; Consequences; related ASR/ADA IDs; selection-record link; affected ADD/views; and supersession links where applicable. Preserve existing history and update the current selection authority and affected realization. An optional ADR neither becomes a competing current selection record nor replaces explicit human selection.

---

# 26. Just-In-Time / Just-Enough Detailed Design

Detailed Design is **part of Sprint execution**, performed story by story **only after the PBI has actually been selected into the Sprint Backlog**. Sprint Planning comes first. Design only the details needed for the selected outcome and its complexity/risk; do not batch-design all Sprint stories or the whole MVP before implementation.

| Conditional design work | When it adds value |
| --- | --- |
| API Design | The selected Story introduces or changes a consumer/provider contract; define only the affected request, response, validation, failure and recovery behavior. |
| Detailed Data Design | Persisted state or integrity changes; resolve necessary owner-scoped representation, migrations and relationships. The baseline Data View remains a logical ownership overview. |
| UML Sequence Diagram | Multi-component interaction, coordination or failure behavior is complex enough to require a reviewable model. |
| UML State Machine Diagram | Lifecycle/state transitions materially affect correctness or security. |
| Transaction / concurrency design | Acceptance, retries, revocation, currentness or concurrent effects require explicit coordination. |
| Algorithm / policy detail | The selected behavior needs precise implementation reasoning while preserving the authoritative Business Rules. |
| External integration contract detail | A real dependency requires agreed payload, meaning, disclosure, failure or compatibility behavior. |

These are conditional artifacts, not a four-document checklist. A simple profile read may need no Sequence or State Diagram. Refresh Token Rotation may need API and persistence design, a Sequence Diagram, session lifecycle reasoning and transaction/security analysis. Neither example selects Sprint scope.

Use UML Sequence/State notation, preferably PlantUML source, when those diagrams are needed. Keep the smallest useful explanation in an existing story, contract, diagram, migration or PR where appropriate; a separate design document per Story is unnecessary.

Resolve the necessary decisions before dependent tests/code, then iterate design, automated tests, implementation and refactoring in small steps. There is no second generic implementation-level “Design” phase afterward. A newly selected item added through Sprint adaptation follows the same rule.

---

# 27. Living API and Service Contracts

Create or update implementation-level contracts during the selected Story's JIT Detailed Design (Sections 26 and 33). Architecture-level boundary agreements can exist early; final endpoints, DTOs and complete future-feature contracts are not refinement or pre-Sprint gates.

## Backend API

OpenAPI is the HTTP contract source of truth when an API is introduced or changed. Keep any `docs/05-api/api-conventions.md` focused on shared conventions: versioning, errors, pagination, authentication, naming, HTTP outcomes and idempotency where relevant. Shared conventions do not require designing every MVP endpoint.

## AI Service Contract

When a selected Story requires the analysis boundary, update `docs/05-api/ai-service-contract.md` for the necessary request/response meaning, confidence, errors, timeouts, compatibility and completion/status behavior. The business runtime consumes that agreed contract, not ungoverned internal model output.

Keep contracts, consumers, implementation and contract/integration tests synchronized as understanding changes. Use the selected architecture and authoritative requirements; this guidance selects no new provider, protocol, payload or tool.

---

# 28. Test Strategy

Test Strategy is **project-wide engineering preparation after the architecture baseline and before Engineering Baseline and Sprint 1 implementation**. It does not wait for ADRs, complete API contracts, physical schemas or all Sequence/State Diagrams.

The initial artifact, when its creation is authorized, is:

```text
docs/07-testing/test-strategy.md
```

Use requirements, Business Rules, Use Cases/Activities, existing stories/AC, QA scenarios, ASRs, selected decisions, ADD/views and the Definition of Done to establish proportionate coverage and evidence responsibilities.

Cover the applicable unit/domain, integration, API/contract, AI evaluation, end-to-end, security, performance and regression testing approaches, including environments, fixtures, privacy-safe evidence, automation and CI expectations. Preserve source thresholds and complete-operation measurement boundaries. Exact story-level cases and fixtures evolve during Sprint delivery as necessary contracts and data details emerge.

```text
Requirement / Business Rule / QA obligation
        ↓
Story Acceptance Criteria + applicable DoD
        ↓
Automated tests and other required verification evidence
```

Testing starts with implementation and continues through review/CI and integration. Selective TDD may use RED → GREEN → REFACTOR for suitable domain/business logic; strict TDD is not required for every code change. A test strategy is a living approach to verification, not a claim that tests or benchmarks have passed.

---

# 29. Definition of Done

Tạo:

```text
docs/06-scrum/definition-of-done.md
```

Recommended baseline:

```text
A Product Backlog Item is Done when:

- Acceptance Criteria satisfied
- implementation complete
- required automated tests pass
- integration behavior verified
- CI passes
- code reviewed
- no unresolved critical defects
- API contract updated if changed
- relevant requirement/design docs updated
- architecture docs updated if architecture changed
- deployed to agreed integration/staging environment when applicable
```

Definition of Done áp dụng cho toàn project.

---

# 30. Pre-Sprint Engineering Baseline

Prepare a short, timeboxed **project-wide Engineering Baseline after Test Strategy and before Sprint 1 implementation**. This is preparation, not a Scrum “Sprint 0” or implementation of product features.

Typical readiness work includes:

- Repository structure, Git/PR workflow, build tooling and supported runtime versions.
- Spring Boot Modular Monolith skeleton consistent with the selected architecture; only necessary module/wiring boundaries.
- Local PostgreSQL development environment and a versioned migration foundation.
- Docker development setup where adopted, configuration/environment conventions and secret handling.
- Mobile and private-analysis skeletons where needed for the agreed foundation.
- Automated-test infrastructure, representative smoke checks and a CI baseline.

Flyway may supply the migration baseline if selected; Docker/tooling/version choices are engineering preparation decisions under the existing architecture, not new product scope mandated by this playbook. Use current selected directions rather than historical stack examples.

Establish enough reproducibility to begin integrated delivery, then improve the foundation as increments require. Do not implement authentication, garment or other product outcomes here, predesign all tables/endpoints, or grow a prolonged setup phase. Existing Enabler PBIs retain their ordering and scope; preparation does not mark them Done or select Sprint work.

After preparation, refine candidate work and conduct Sprint Planning. Sprint 1 must aim for a usable Increment.

---

# 31. Backlog Refinement

Backlog Refinement is an ongoing activity, not a separate formal Scrum event. It prepares candidate PBIs before Sprint Planning and continues as learning changes the backlog.

The objective is to make a candidate Story **sufficiently understood, feasible, testable and estimable/selectable to be considered Ready**. “Ready” is a lightweight project working agreement, not an additional Scrum artifact or a guarantee of selection.

Refinement may:

- Clarify the user/delivery outcome, Story boundaries and testable Acceptance Criteria.
- Verify relevant requirements and Business Rules.
- Identify dependencies, architecture constraints and technical risks.
- Identify testability concerns and unresolved unknowns.
- Perform lightweight technical investigation sufficient for feasibility and selection.
- Create a timeboxed Spike when a major unknown must be resolved before responsible selection; keep the dependent Story unselected until there is enough evidence.

Refinement does **not normally finalize** endpoint/DTO contracts, complete physical schemas, transaction implementation, full Sequence/State Diagrams or class-level design. A Spike investigates a bounded unknown; it is not permission to design the entire future feature or MVP.

If evidence reveals a consequential architecture change, review requirement/QA/ASR impact, analyze the affected ADA problem, obtain explicit human selection and synchronize only affected ADD/views. A future standalone ADR is optional under Section 25. Resolve necessary stable-boundary uncertainty before selecting dependent work; leave implementation details to the selected Story's JIT design.

Refinement creates no Sprint Goal, Sprint assignment or capacity commitment. Product Owner accountability for backlog ordering and PO/BA ownership of Story/AC behavior remain unchanged.

---

# 32. Sprint Planning

Sprint Planning initiates the Sprint. Use a consistent Sprint length (for example, two weeks when appropriate to the team's schedule); this playbook fixes no capacity or scope.

Planning establishes:

1. **Why:** a coherent Sprint Goal shaped collaboratively by the Scrum Team.
2. **What:** Product Backlog Items selected by Developers through discussion with the Product Owner, considering order, readiness, dependencies, capacity and the DoD.
3. **How:** an initial Sprint Backlog and enough implementation thinking to understand how the selected work may proceed. Developers own and adapt this plan.

For a possible Refresh Token Story, initial thinking might identify the Identity module, persistent refresh-session authority, JWT operations and integration tests. It need not finalize DTOs, database columns, transaction mechanics, complete sequences, all state transitions or class-level design.

**Selection precedes full JIT Detailed Design.** Do not require the API/Data/Sequence/State artifact set for every candidate before Planning, or design the whole Sprint before starting a Story.

Use the existing Sprint artifact locations when Planning is authorized:

```text
docs/06-scrum/sprints/sprint-01/sprint-goal.md
docs/06-scrum/sprints/sprint-01/sprint-backlog.md
```

A Sprint Backlog may track Item ID, work, responsible contributor, estimate and status. Status labels such as Ready, In Progress, In Review and Done describe work visibility; testing accompanies development throughout. No extra generic Design phase or final-only Testing phase is implied.

The selected work and initial plan evolve with learning. Developers and Product Owner renegotiate scope where necessary without endangering the Sprint Goal; current draft stories and illustrative roadmap examples are not automatic Sprint 1 selections.

---

# 33. Sprint Execution

For each selected Story, use one implementation-level design concept:

```text
Selected Story in Sprint Backlog
        ↓
Just-In-Time / Just-Enough Detailed Design
        ↓
Automated Test + Implementation
        ↓
Refactor
        ↓
Integration / API Verification where applicable
        ↓
Pull Request → CI → Code Review
        ↓
Done: Story AC + applicable Definition of Done
        ↓
Integrated, usable Working Increment
```

These are short feedback loops, not isolated handoff phases. Write and run relevant automated tests alongside implementation from the start. Use selective RED → GREEN → REFACTOR where helpful; strict TDD is not universal. Address review/CI failures and repeat affected checks before Done.

Design only what the selected Story's complexity/risk needs (Section 26). Do not finish all Sprint design before any implementation, introduce a second “Design → Code → Test” phase, or delay testing until code review. Integrate completed work throughout the Sprint.

Use small reviewable commits and branches linked to existing Story/PBI IDs, with implementation, tests and affected contracts/design documentation evolving together. Open a Draft PR when useful for early feedback. Do not push directly to `main`.

The Sprint Backlog is adapted by Developers as work becomes understood. New or changed work follows the same selection/JIT rule and preserves the Sprint Goal and authoritative requirements. Incomplete work does not become part of the Done Increment merely because the Sprint ends.

---

# 34. Documentation During a Sprint

API contracts, physical schemas/migrations, diagrams and other necessary detailed designs are living artifacts created or updated with selected Sprint work. Keep them synchronized when implementation or validated understanding changes; create documentation for useful communication and traceability.

- Update requirements/rules and affected Use Cases/Activities/Story AC only when requirement truth changes.
- Update ADA and the human selection record only when a consequential choice changes; update ADD/views only when architectural truth changes.
- Update affected contracts, data detail, Sequence/State models and tests when their implementation-level meaning changes.
- Preserve stable IDs and links. Unaffected artifacts require no edits.

A newly discovered architecture concern follows analysis → explicit human selection → affected ADD/views; an optional future ADR may preserve significant evolution under Section 25. Resume the selected Story's same JIT/test/implementation loop after resolving the affected boundary; this is not another full design phase.

A requirement change may update the backlog and selected work through PO/BA/Developer collaboration. Keep the Sprint Goal and DoD intact. Documentation volume and formal approval claims are not evidence of working software.

---

# 35. Sprint Review

File:

```text
review.md
```

Record:

```text
Sprint Goal outcome
Completed Increment
Demo summary
Stakeholder feedback
Accepted / rejected behavior
New backlog items
Scope changes
```

Sprint Review tập trung vào **working software**, không phải số lượng commit.

---

# 36. Sprint Retrospective

File:

```text
retrospective.md
```

Recommended format:

```text
What went well?
What did not go well?
Why?
What will we change next Sprint?
Who owns the improvement?
```

Example:

```text
Problem:
QA received all stories in the final two days.

Root Cause:
PRs were opened too late.

Action:
Open Draft PR after first meaningful implementation.
```

Action item phải được áp dụng Sprint sau.

---

# 37. Recommended CapsuleAI Sprint Roadmap

Roadmap chỉ là initial plan. Product Backlog có thể thay đổi.

The Sprint outlines below are illustrative historical planning examples, not current commitment or backlog ordering. DD-001 and the current ordered Product Backlog govern CapsuleAI; technical examples apply only if later explicitly selected. Quality accompanies each increment, not only a final hardening Sprint.

## Preparation / Baseline

```text
BRD
PRD
SRS
Core Use Cases
Core Activity Diagrams
Product Goal
Initial Product Backlog
14 QA Analysis
ASR v0.1
Architecture Decision Analysis v0.1
Selected Architecture Decisions (human gate)
ADD v0.1
Logical → Implementation → Deployment → Data Views v0.1
Test Strategy baseline
Pre-Sprint Engineering Baseline
```

After this shared preparation, repeat Backlog Refinement → Sprint Planning → Sprint execution, with JIT design and automated tests/implementation per selected Story. The historical outlines below remain examples, not current Sprint scope or design prerequisites.

## Sprint 1 — Authentication + Garment Pipeline

Goal:

```text
Authenticated user can upload a garment and receive a processed result.
```

Possible scope:

```text
JWT authentication
mobile image selection/camera
Spring Boot upload path
object storage
Python AI integration
background removal
basic result display
```

## Sprint 2 — Digital Wardrobe

```text
attribute extraction
manual correction
garment persistence
wardrobe CRUD
search/filter
data cleanup
```

## Sprint 3 — Outfit Recommendation

```text
weather integration
slot rules
season rules
color harmony
pattern rules
compatibility score
generate >= 3 outfits
shuffle
wear today
history
```

## Sprint 4 — Gap Analysis / Strategic Shopping

```text
staple catalog
gap detection
hypothetical outfit simulation
Wardrobe Multiplier
ranking
recommendation preview
external links
```

## Sprint 5 — Hardening

```text
E2E
security
authorization
failure handling
performance
observability
deployment
regression
polish
```

---

# 38. Document Creation Order

## System-Wide Baseline Before Sprint 1

Maintain sufficient baselines for BRD, PRD, SRS, Business Rules, the master Use Case Diagram/core specifications and Activities, Product Goal, ordered Product Backlog, initial near-term stories/AC, DoD, all 14 QA assessments, ASR, ADA, explicit Selected Architecture Decisions, ADD and all four architecture views.

Critical architecture choices must be selected and Important choices selected or safely bounded under Section 17.3. “Baseline” means sufficient for responsible delivery, not frozen or perfect.

## Engineering Preparation Before Sprint 1 Implementation

```text
Architecture Baseline → Test Strategy → Engineering Baseline
```

Separate ADR files, complete MVP API/Data design, and all Sequence/State Diagrams are **not** entry requirements. No separate full-design or Sprint Readiness documentation phase is added.

## Every Sprint: Refinement and Selection

Refine candidate Story/AC, dependencies, constraints, risk and testability enough for readiness. Sprint Planning establishes the Sprint Goal, selected PBIs and initial Sprint Backlog; it precedes full implementation-level design.

## During Sprint Execution, Per Selected Story

Create/update only necessary API contracts, data detail/migrations, UML Sequence/State Diagrams, transaction/concurrency, algorithms or integration detail through JIT design, alongside automated tests and implementation. Reuse existing artifact locations; no separate design packet is required for every Story.

Activity models, requirements and architecture records change only where their owning truth changes. A standalone ADR remains optional for significant future evolution.

## Per Sprint / Release

Maintain Sprint Goal, evolving Sprint Backlog, Review, Retrospective and Product Backlog. Maintain deployment, rollback, monitoring and postmortem material when release/operation needs it. A usable Increment and its evidence remain the focus.

---

# 39. Artifact Ownership

| Artifact | Primary Owner | Reviewers |
|---|---|---|
| BRD | BA / Product | PO, Stakeholder |
| PRD | PO / Product | BA, UX, Tech Lead |
| SRS | BA / System Analyst | PO, Tech Lead, Dev, QA |
| Business Rules | BA / PO | Dev, QA |
| Use Case | BA / System Analyst | PO, QA, Dev |
| UML Activity Diagram | BA/System Analyst or Dev for technical flows | PO, QA, Tech Lead |
| Product Backlog | Product Owner | Team |
| User Story | PO / BA | Dev, QA |
| Acceptance Criteria | PO / BA | Dev, QA |
| 14 QA Analysis | Architect / Tech Lead | Dev, QA, Product |
| ASR | Architect / Tech Lead | Engineering team |
| Architecture Decision Analysis | Architect / Tech Lead with Developer input | Dev, QA and relevant AI/Mobile/Product contributors |
| Selected Architecture Decisions | Responsible human Architect / Tech Lead | Relevant engineering reviewers; Product/BA for requirement implications |
| ADD | Architect / Tech Lead | Engineering team |
| Logical View | Architect / Tech Lead | Dev |
| Implementation View | Tech Lead / Senior Dev | Dev |
| Deployment View | Tech Lead / DevOps | Dev |
| Data View | Backend / Data owner / Tech Lead | Team |
| Optional future ADR | Decision proposer / Tech Lead | Relevant engineering reviewers when a significant evolution warrants the record |
| Story-specific JIT Detailed Design | Developers / Tech Lead | Relevant Developers, QA and data/integration contributors, proportional to risk |
| UML Sequence Diagram, if needed | Developer / Tech Lead | Relevant developers |
| UML State Diagram, if needed | Developer / Tech Lead / Analyst | QA, Dev |
| Test Strategy | QA/engineering lead | Dev, PO |
| Engineering Baseline | Developers / Tech Lead | Relevant engineering contributors |
| Sprint Goal | Product Owner + Developers | Scrum Team |
| Sprint Backlog | Developers | Scrum Team |
| Sprint Review Notes | Scrum Team | Stakeholders |
| Retrospective | Scrum Team | Scrum Team |

Với team sinh viên nhỏ, một người có thể giữ nhiều role.

---

# 40. Source-of-Truth Rules

Avoid duplicate authorities. Later artifacts link to their sources rather than restate them.

| Information | Source of Truth |
| --- | --- |
| Business intent | BRD |
| Product behavior | PRD |
| Software requirements | SRS |
| Domain invariants | `business-rules.md` |
| Use-case behavior | Use Case + Activity Diagram |
| Ordered future work | `product-backlog.md` |
| Story behavior | User Story + Acceptance Criteria |
| QA classifications/scenario evidence | Quality Attribute Analysis; normative measures remain in SRS |
| Architecture drivers and significant requirements | ASR |
| Architecture problems/options/trade-offs/recommendations/confidence | Architecture Decision Analysis |
| Authoritative selections/rationale/developer impact and retained selection history | Selected Architecture Decisions, linked to ADA |
| Architecture realization | ADD + four architecture views |
| Optional dedicated history of a significant future architecture change | Standalone ADR, only when justified; links to ADA/selection, not a competing current authority |
| HTTP API contract | OpenAPI |
| AI service contract | `ai-service-contract.md` |
| Executable DB schema | Migration/schema code |
| Logical data relationships/ownership/stores | Data View |
| Necessary implementation-level design explanation | Living story-scoped contract/data/Sequence/State/design artifacts created during Sprint execution |
| Current code | Source code |
| Build/test state | CI and applicable verification evidence |
| Sprint Goal, selected work and evolving implementation plan | Sprint Goal + Sprint Backlog |
| Release state | Deployment/release artifacts |

ADA and Selected Architecture Decisions already preserve the current decision baseline. New ADR files are neither a prerequisite to implementation nor a requirement for each ADA ID.

---

# 41. Change Propagation

## Requirement Change

When a requirement changes, update its owning authority, then only affected rules, Use Cases/Activities, Story/AC, contracts, implementation and verification evidence. Update architecture only if architectural truth changes.

A changed image-size limit, for example, affects the SRS, relevant user behavior, validation/API contract and tests. It does not automatically require a new ASR or architecture decision. If the implementing Story is not selected, retain the work in the Product Backlog rather than completing its detailed design in advance.

## Architecture Change

A hypothetical change from synchronous to asynchronous analysis would require evidence and renewed selection; this example changes no current CapsuleAI decision.

```text
Review requirement / QA / ASR impact; update only changed truth
        ↓
Affected ADA analysis / re-analysis
        ↓
Explicit human selection / Selected Architecture Decisions
        ↓
Affected ADD and architecture views
        ↓
Selected Sprint Story's JIT Detailed Design
        ↓
Automated Test + Implementation / verification
```

For significant future evolution, a standalone ADR may optionally preserve the historical transition, linked to the same ADA/selection and affected views. It is not an additional compulsory stage.

Update Test Strategy or engineering conventions when the changed verification/delivery approach warrants it. User Stories/AC change only when their behavior or source references change; a tactic alone creates no product scope. Preserve decision history and supersession without duplicating current authority.

---

# 42. UML Rules for AI Models Working on CapsuleAI

Whenever an AI model is asked to create or update diagrams for this repository, follow these rules:

1. **Use Case Diagram must be UML Use Case Diagram.**
2. **Activity Diagram must be UML Activity Diagram.**
3. **Sequence Diagram must be UML Sequence Diagram.**
4. **State Diagram must be UML State Machine Diagram.**
5. **Deployment View must use UML Deployment semantics.**
6. Prefer **PlantUML source files (`.puml`)** rather than static images.
7. Do not replace UML with generic Mermaid flowcharts when the requested artifact is explicitly UML.
8. Keep diagrams at the correct abstraction level.
9. Do not put implementation details into a business-level diagram unless necessary.
10. Every diagram must be traceable to relevant requirements/use cases/ASRs where applicable.
11. Update existing diagrams when the model changes; do not create duplicate competing versions.
12. Diagram source is authoritative; rendered images are derived artifacts.

---

# 43. Architecture Evolution Rule

Architecture evolves with evidence. The initial sufficient baseline is QA → ASR → ADA → explicit human Selected Architecture Decisions → ADD + four views. It does not require standalone ADR files or all implementation detail.

For example, the current selected direction starts without a dedicated cache. If measured representative recommendation latency later no longer satisfies requirements:

1. Review the evidence and requirement/QA/ASR impact.
2. Re-analyze the relevant ADA alternatives against correctness, freshness, privacy and operating cost.
3. Obtain explicit human selection and retain the superseded choice/history.
4. Update only affected ADD/views and the selected Sprint work's JIT design, tests and implementation.
5. Optionally create a standalone ADR if this consequential transition benefits from a dedicated historical record.

Introducing Redis is an example of a possible future choice, not a decision made by this workflow. The existing no-cache selection remains in force unless explicitly changed through that process.

Stable boundaries guide delivery early; details are resolved as late as responsibly possible. Neither a frozen whole-MVP design nor ungoverned code-first development replaces the agreed baseline.

---

# 44. Practical Decision Rule: Do We Need a Diagram?

Create a diagram only when it helps answer a non-trivial question.

## Create

```text
Use Case Diagram
→ system capabilities / actors

Activity Diagram
→ branching workflow / business process

Sequence Diagram
→ complex runtime collaboration

State Diagram
→ important lifecycle

Logical View
→ domain/module responsibilities

Implementation View
→ source/module structure

Deployment View
→ runtime topology

Data View
→ ownership / relationships / storage
```

## Usually Skip

```text
Activity Diagram for trivial CRUD
Sequence Diagram for GET /profile
Class Diagram containing every DTO/entity/service
Diagram that duplicates code without adding understanding
```

---

# 45. End-to-End Example — Add Garment

This is a traceability and timing example, not a Sprint selection or a change to backlog order. The existing manual-add Story (US-GAR-001 / PBI-006) can use the following path **if later selected**:

```text
BRD / PRD: trustworthy wardrobe understanding and manual continuity
        ↓
SRS / Business Rules: confirmed minimum, authority, readiness and safe acceptance
        ↓
UC-007 / AD-007: manual-entry branch and shared confirmation/recovery
        ↓
PBI-006 → US-GAR-001 / Acceptance Criteria
        ↓
QA / ASR → ADA → Selected Architecture Decisions
        ↓
ADD + Logical / Implementation / Deployment / Data Views
        ↓
Project-wide Test Strategy → Engineering Baseline
        ↓
Backlog Refinement: clarify outcome, constraints, feasibility and testability
        ↓
Sprint Planning: select the Story within a coherent Sprint Goal
        ↓
Sprint Execution:
JIT API/Data/interaction/transaction detail only where needed
→ Automated Test + Implementation → Refactor / Integration Verification
→ PR / CI / Review → Done → Working Increment
```

Existing AC and DoD govern completion; no image or AI success is introduced as a manual-entry prerequisite. Photo/AI assistance retains its own existing backlog boundaries and receives JIT detail only when its work is selected. The architecture decision baseline is reused rather than copied into ADR files.

---

# 46. What the Team Should Do From Today

**Current position — 2026-10-20:** the system-wide requirements, Business Rules, Use Cases/Activities, Product Backlog/initial stories, QA/ASR, ADA, Selected Architecture Decisions, ADD and four architecture views already exist. All 18 recorded architecture selections remain unchanged. The next project-wide preparation activity is **Test Strategy**, followed by **Engineering Baseline**. Do not restart completed baseline work, create baseline ADRs or design the whole MVP.

This process revision changes neither artifact approval statuses nor claims of implementation or verification. Existing source-specific historical handoffs remain history; this section and DD-006 provide current process navigation.

| # | Action | Main Output |
| ---: | --- | --- |
| 1 | Consolidate existing source documents | source baseline |
| 2 | Create/normalize BRD | `BRD.md` |
| 3 | Normalize existing PRD | `PRD.md` |
| 4 | Create SRS | `SRS.md` |
| 5 | Extract business rules | `business-rules.md` |
| 6 | Identify actors and use cases | UML Use Case Diagram |
| 7 | Write core Use Case Specifications | `UC-*.md` |
| 8 | Draw core workflows | UML Activity Diagrams |
| 9 | Define Product Goal | `product-goal.md` |
| 10 | Create initial Product Backlog | `product-backlog.md` |
| 11 | Create initial User Stories for top backlog | `US-*.md` + Acceptance Criteria |
| 12 | Define Definition of Done | `definition-of-done.md` |
| 13 | Analyze all 14 Quality Attributes | `quality-attribute-analysis.md` |
| 14 | Identify ASRs and architectural drivers | `ASR.md` |
| 15 | Compare major choices; recommend without selecting | `architecture-decision-analysis.md` |
| 16 | Human review and explicit selection / safe bounded deferral | `selected-architecture-decisions.md` |
| 17 | Realize selected directions in ADD | `ADD.md` |
| 18 | Maintain Logical View | `logical-view.puml` |
| 19 | Maintain Implementation View | `implementation-view.puml` |
| 20 | Maintain Deployment View | `deployment-view.puml` |
| 21 | Maintain Data View; system-wide baseline now exists | `data-view.puml` |
| 22 | Define project-wide Test Strategy | `test-strategy.md` when authorized |
| 23 | Prepare repository/build/CI/environments/testing foundation | timeboxed Engineering Baseline |
| 24 | Refine candidate work enough for feasibility, estimation/selection and readiness | understandable candidate Stories/AC; bounded Spikes where necessary |
| 25 | Conduct Sprint Planning | Sprint Goal + selected PBIs + initial Sprint Backlog |
| 26 | During Sprint execution, design one selected Story Just-In-Time | only necessary API/Data/Sequence/State/transaction/algorithm/integration detail |
| 27 | Develop with automated tests and implementation together; refactor and verify integration | tested integrated behavior |
| 28 | Use PR, CI and code review continuously; resolve findings | review/verification evidence |
| 29 | Meet applicable Story AC + DoD | Done work contributing to a usable Working Increment |
| 30 | Conduct Sprint Review | Increment inspection, feedback and `review.md` |
| 31 | Conduct Sprint Retrospective | improvement actions and `retrospective.md` |
| 32 | Update backlog/requirements/architecture only where truth changes | evolved authoritative artifacts and traceability |
| 33 | Continue refinement and the next Sprint | repeat iterative delivery |

Steps 1–21 describe the existing system-wide baseline, not work to repeat. Steps 22–23 are shared preparation before Sprint 1 implementation. Steps 24–33 form the ongoing delivery loop; Steps 26–29 repeat per selected Story within the Sprint, not as batches for the whole Sprint/MVP. Refinement continues throughout delivery.

```text
Architecture Baseline
        ↓
Test Strategy → Engineering Baseline
        ↓
Backlog Refinement → Sprint Planning
        ↓
Sprint Execution, per selected Story:
JIT Detailed Design → Automated Test + Implementation
→ Refactor / Integration Verification → PR / CI / Review → Done
        ↓
Working Increment → Sprint Review → Sprint Retrospective
        ↓
Update affected Backlog / Requirements / Architecture → Next Sprint
```

Standalone ADRs are optional future evolution records under Section 25; they are outside this mandatory execution sequence.

---

# 47. Final Operating Model

Requirements, architecture and Scrum delivery remain continuously connected. Project-wide engineering preparation connects the existing baseline to Sprint 1 implementation.

## Requirements Track

```text
BRD → PRD → SRS → Business Rules → Use Cases / UML Activities
```

## Architecture Track

```text
14 QA Analysis → ASR → Architecture Decision Analysis
→ Selected Architecture Decisions (explicit human gate)
→ ADD → Logical / Implementation / Deployment / Data Views
```

ADA and the selected record preserve decision reasoning and history. Optional ADRs may support significant future evolution; they do not add a baseline phase.

## Shared Engineering Preparation

```text
Architecture Baseline → Test Strategy → Engineering Baseline
```

## Iterative Scrum Delivery Track

```text
Product Goal / evolving Product Backlog
        ↓
Ongoing Backlog Refinement
        ↓
Sprint Planning: Sprint Goal + selected PBIs + initial plan
        ↓
Sprint Execution, story by story:
JIT Detailed Design → Automated Test + Implementation
→ Refactor / Integration Verification → PR / CI / Review → Done
        ↓
Working Increment
        ↓
Sprint Review → Sprint Retrospective
        ↓
Update affected Backlog / Requirements / Architecture
        ↓
Next Sprint
```

Learning flows back continuously, not only at the end of a Sprint. Change only affected authorities, preserve traceability and keep the Sprint Backlog adaptable. There is one implementation-level design concept: selected-story JIT design inside Sprint execution.

---

# 48. Core Principles

1. **Scrum is the delivery model; Jira is not required.**
2. **All project documentation is version-controlled in the codebase.**
3. **Requirements explain what must be true; architecture explains how the system satisfies important requirements.**
4. **Evaluate all 14 Quality Attributes; only significant ones need deep scenarios.**
5. **ASR precedes ADA; explicit human selection precedes ADD. Recommendation is not selection.**
6. **ADA and Selected Architecture Decisions already preserve the current decision baseline; ADD and four views realize it. Standalone ADRs are optional for consequential future evolution.**
7. **Logical, Implementation, Deployment and Data Views remain mandatory architecture views for CapsuleAI.**
8. **Use Case, Activity, Sequence and State diagrams use UML, preferably PlantUML source; create conditional diagrams only when useful.**
9. **Do not create documentation merely for volume.**
10. **Product Backlog and User Stories remain living artifacts.**
11. **Architecture evolves during Sprints when evidence warrants change.**
12. **Every Sprint aims for a usable Increment.**
13. **Acceptance Criteria must be testable.**
14. **Quality Attribute requirements must be measurable/testable.**
15. **Update documentation when its underlying truth changes.**
16. **Avoid duplicate sources of truth.**
17. **Prefer traceability over paperwork.**
18. **Prefer sufficient architecture justified by ASRs and constraints.**
19. **Automated tests, integration verification, review and CI are part of development; selective TDD is optional.**
20. **Review, testing and operating feedback return to the Product Backlog.**
21. **Architecture early, Detailed Design late: stable boundaries early; implementation details Just-In-Time.**
22. **Refinement prepares candidate Stories; it does not fully design implementation.**
23. **Sprint Planning selects work before its full JIT Detailed Design.**
24. **JIT design happens inside execution, story by story, proportional to complexity and risk.**
25. **Test Strategy and Engineering Baseline are shared preparation before Sprint 1 implementation; neither requires complete MVP detailed design.**

---

# 49. Instruction for Future AI Models

When continuing CapsuleAI:

1. Read this workflow and current relevant repository artifacts first; repository truth overrides memory.
2. Identify the current baseline, engineering-preparation state and actual Sprint Backlog before proposing next work.
3. Inspect existing files before creating new ones; do not repeat completed baseline work.
4. Preserve stable `FR-*`, `NFR-*`, `BR-*`, `UC-*`, `PBI-*`, `US-*`, `ASR-*`, `QA-*`, `ADA-*` and any existing `ADR-*` IDs.
5. Follow source ownership and maintain traceability among requirements, Stories, architecture and tests.
6. Use ADA for analysis and Selected Architecture Decisions for authoritative selections/history. Do not create one ADR per ADA or invent approval/selection.
7. Keep ADD and the four views synchronized only when architectural truth changes; update QA/ASR only when requirements/drivers change.
8. Follow analysis → explicit human selection → affected ADD/views for consequential architecture changes. A future standalone ADR is optional under Section 25.
9. Preserve UML/PlantUML standards and the abstraction boundaries of each artifact.
10. After the existing architecture baseline, proceed to authorized Test Strategy and Engineering Baseline work; do not require full API/Data/Sequence/State design first.
11. Refine candidate Stories only enough for clarity, feasibility, testability and estimation/selection; investigate major unknowns with bounded Spikes.
12. Sprint Planning establishes the Sprint Goal, selected PBIs and initial Sprint Backlog. Draft Stories, backlog order and examples do not authorize Sprint selection.
13. Perform JIT / Just-Enough Detailed Design only for work actually selected into the Sprint, during execution. Create only artifacts justified by complexity/risk.
14. Use the same JIT → Automated Test + Implementation → Refactor / Integration Verification → PR / CI / Review → Done loop. Do not add a second generic Design phase or final-only testing stage.
15. Apply Story AC and the project DoD; preserve a usable integrated Increment rather than counting documents or isolated tasks as Done.
16. Update contracts, schemas/migrations, diagrams and tests with delivered truth; requirements and architecture change only when their respective truth changes.
17. Preserve Product Owner ordering, PO/BA Story/AC responsibilities and Developer ownership of the evolving Sprint Backlog. Do not invent Sprint Goals, scope, estimates or capacity.
18. Avoid over-engineering, duplicate authorities and unjustified technologies; historical examples are not selections.
19. Keep Scrum iterative and incremental, with Review/Retrospective feedback informing subsequent work.
20. Treat this file as process authority unless the team explicitly revises it; process navigation does not claim artifact approval or software verification.
