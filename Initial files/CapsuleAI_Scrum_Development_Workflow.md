# CapsuleAI — Scrum-Based Software Development Workflow

> **Purpose**  
> Tài liệu này là **playbook chính thức** hướng dẫn team CapsuleAI triển khai đồ án theo **Scrum/Agile**, với toàn bộ tài liệu quản lý dự án, requirements và architecture được version-control trực tiếp trong codebase.
>
> Tài liệu được thiết kế để:
>
> - giúp team biết **nên làm gì trước, làm gì sau**;
> - xác định **tài liệu nào cần tạo, ai tạo, khi nào cập nhật**;
> - kết nối Business → Product → Requirements → Architecture → Scrum → Code → Test → Release;
> - mô phỏng workflow của một software team chuyên nghiệp nhưng tránh bureaucracy không cần thiết;
> - giúp AI model đọc repository và hiểu đúng workflow cần tuân theo;
> - giữ architecture của CapsuleAI có cấu trúc tương tự đồ án Food Delivery trước đây: **ASR + ADD + 14 Quality Attributes + 4 Architecture Views**;
> - yêu cầu các sơ đồ UML như **Use Case Diagram, Activity Diagram, Sequence Diagram, State Diagram, Deployment Diagram...** phải được biểu diễn bằng **UML**, ưu tiên lưu source bằng **PlantUML** trong repository.
>
> **Important:** Scrum không yêu cầu Jira. CapsuleAI **không dùng Jira**. Product Backlog, User Stories, Sprint Backlog, Bugs, Spikes, Sprint Review và Retrospective được quản lý bằng Markdown ngay trong repository.

---

# 1. Development Philosophy

CapsuleAI không triển khai theo kiểu:

```text
Viết toàn bộ tài liệu
→ khóa requirements
→ thiết kế hoàn chỉnh
→ code toàn bộ
→ test cuối cùng
```

Đó là cách làm gần Waterfall.

CapsuleAI dùng **iterative / incremental development theo Scrum**:

```text
Initial Requirements & Architecture Baseline
                    ↓
           Product Backlog
                    ↓
            Backlog Refinement
                    ↓
             Sprint Planning
                    ↓
                Sprint
   ┌────────────────────────────────┐
   │ Analyze → Design → Code → Test │
   │        → Review → Integrate    │
   └────────────────────────────────┘
                    ↓
             Working Increment
                    ↓
              Sprint Review
                    ↓
           Sprint Retrospective
                    ↓
       Update Backlog / Documents
                    ↓
               Next Sprint
```

Requirements, architecture và documentation được **evolve cùng software**.

---

# 2. Master Traceability Chain

Mọi công việc kỹ thuật quan trọng nên có khả năng trace ngược về lý do tồn tại của nó.

```text
Business Problem
      ↓
BRD
      ↓
PRD
      ↓
SRS
      ↓
Business Rules
      ↓
Use Cases
      ↓
Activity Diagrams
      ↓
Product Backlog
      ↓
User Stories + Acceptance Criteria
      ↓
14 Quality Attribute Analysis
      ↓
ASR
      ↓
ADD
      ↓
Architecture Views / ADRs
      ↓
Sequence / State / API / Data Design
      ↓
Sprint Backlog
      ↓
Implementation
      ↓
Tests
      ↓
Pull Request / Code Review
      ↓
Working Increment
      ↓
Release / Deployment
      ↓
Monitoring / Feedback / Bugs
      ↓
Product Backlog
```

Không phải mọi User Story đều cần tất cả artifact ở trên. Chỉ tạo artifact khi nó truyền đạt thông tin thực sự có giá trị.

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
│   │   ├── ADD.md
│   │   │
│   │   ├── views/
│   │   │   ├── logical-view.puml
│   │   │   ├── implementation-view.puml
│   │   │   ├── deployment-view.puml
│   │   │   └── data-view.puml
│   │   │
│   │   ├── adr/
│   │   │   ├── ADR-001-database.md
│   │   │   ├── ADR-002-jwt-authentication.md
│   │   │   └── ADR-003-python-ai-service.md
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

Data View có thể dùng:

- UML Class Diagram nếu mô hình hóa domain/data structure;
- ERD notation nếu mục tiêu là relational schema;
- PlantUML ER-style notation nếu phù hợp.

Điều quan trọng là Data View phải thể hiện **data ownership, relationships, stores và boundaries**, không chỉ danh sách field.

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
- `Spring Boot → Python AI Service → Model`;
- CV accuracy target;
- image-processing latency;
- Wardrobe Multiplier formula;
- wardrobe completion formula;
- user-preference learning scope;
- style/occasion/body-shape scope.

Không phải decision nào cũng block Sprint 1. Decision chưa cần ngay được giữ trong backlog hoặc decision log và giải quyết trước Sprint liên quan.

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
- ADD §...
- SEQ-...
- ADR-...

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

Output của Spike có thể dẫn tới ADR.

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
Independent Python inference service, image normalization,
model warm-up, timeout policy, deployment resource planning.
```

---

# 18. Phase 12 — Architecture Design Document (ADD)

Tạo:

```text
docs/04-architecture/ADD.md
```

ADD mô tả **HOW architecture addresses the ASRs and quality drivers**.

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

Với QA quan trọng, dùng measurable scenario format:

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
Mobile → Spring Boot → Python AI Service → Models

Response:
Processed garment attributes are returned

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

Example conceptual mapping:

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

Likely nodes:

```text
Mobile Device
Spring Boot Runtime
Python AI Runtime
Database
Object Storage
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

Nếu dùng relational database, có thể bổ sung ERD.

Nếu dùng document database, mô tả document ownership/embedding/reference strategy.

---

# 25. ADR — Architecture Decision Records

Tạo ADR khi có một quyết định architecture quan trọng.

Examples:

```text
ADR-001 Database Selection
ADR-002 JWT Authentication
ADR-003 Separate Python AI Service
ADR-004 Object Storage Strategy
ADR-005 AI Processing Sync vs Async
```

Template:

```text
# ADR-xxx — Title

Status:
Proposed / Accepted / Superseded

Context:
...

Decision:
...

Alternatives:
...

Consequences:
Positive:
...

Negative:
...

Related ASRs:
...

Related Views:
...
```

ADR không thay ADD.

ADD mô tả architecture tổng thể; ADR lưu history của các quyết định quan trọng.

---

# 26. Detailed Behavioral / Technical Diagrams

Không vẽ mọi diagram có thể vẽ.

## Sequence Diagram

Tạo khi interaction giữa nhiều component đủ phức tạp.

Examples:

```text
SEQ-01 Garment Processing
SEQ-02 Outfit Generation
SEQ-03 Gap Analysis
```

Phải dùng **UML Sequence Diagram** bằng PlantUML.

## State Machine Diagram

Tạo khi domain object có lifecycle quan trọng.

Example:

```text
UPLOADED
→ PROCESSING
→ AWAITING_CONFIRMATION
→ CONFIRMED
```

Failure path:

```text
PROCESSING
→ PROCESSING_FAILED
→ MANUAL_ENTRY
```

Phải dùng **UML State Machine Diagram** bằng PlantUML.

---

# 27. API and Service Contracts

Sau khi architecture baseline đủ rõ:

```text
docs/05-api/
```

## Backend API

OpenAPI nên là source of truth cho HTTP contract.

Document:

```text
api-conventions.md
```

chỉ chứa convention:

```text
versioning
error format
pagination
authentication
idempotency when relevant
naming
HTTP status behavior
```

## AI Service Contract

```text
ai-service-contract.md
```

Phải xác định rõ:

```text
request format
response format
error model
confidence
timeouts
versioning
processing status
```

Spring Boot không phụ thuộc trực tiếp vào output nội bộ của model.

---

# 28. Test Strategy

Tạo trước hoặc trong Sprint đầu:

```text
docs/07-testing/test-strategy.md
```

Cover:

```text
Unit Tests
Integration Tests
API Tests
Contract Tests
AI Evaluation Tests
End-to-End Tests
Security Tests
Performance Tests
Regression Tests
```

Traceability:

```text
Requirement
   ↓
Acceptance Criteria
   ↓
Test Case / Automated Test
```

Quality Attribute scenarios trong ADD phải dẫn tới measurable tests khi feasible.

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

Scrum Guide không định nghĩa “Sprint 0”, vì vậy CapsuleAI nên coi bước này là **Pre-Sprint Engineering Baseline**, ngắn và có timebox.

Mục tiêu:

```text
Repository
Git workflow
Build
CI
Local environments
Backend skeleton
Mobile skeleton
AI service skeleton
Database
Object storage
Configuration
Testing foundation
```

Không kéo dài thành nhiều tuần chỉ để setup.

Sau baseline, Sprint 1 phải tạo ra **usable Increment**.

---

# 31. Backlog Refinement

Backlog Refinement không phải một Scrum Event chính thức độc lập nhưng là ongoing activity.

Team chọn các upcoming stories và kiểm tra:

```text
Is the requirement clear?
Are Acceptance Criteria testable?
Are dependencies known?
Are important business rules linked?
Is architecture impact understood?
Is a Spike needed?
Can this fit within one Sprint?
```

Nếu Story có architectural impact lớn:

```text
Story
 ↓
Check ASR
 ↓
Update ASR / ADD / ADR if needed
 ↓
Ready
```

---

# 32. Sprint Planning

Mỗi Sprint nên khoảng **2 tuần** nếu phù hợp với lịch học.

Sprint Planning tạo:

```text
Sprint Goal
+
Sprint Backlog
```

Folder:

```text
docs/06-scrum/sprints/sprint-01/
```

## sprint-goal.md

Ví dụ:

```text
An authenticated user can upload a garment,
send it through the backend and AI pipeline,
and view the processed result.
```

## sprint-backlog.md

Recommended table:

| ID | Item | Owner | Estimate | Status |
|---|---|---|---:|---|

Status:

```text
Ready
In Progress
Code Review
Testing
Done
```

---

# 33. Sprint Execution

Một User Story đi qua:

```text
Ready
 ↓
In Progress
 ↓
Implementation
 ↓
Local Tests
 ↓
Commit
 ↓
Push
 ↓
Pull Request
 ↓
CI
 ↓
Code Review
 ↓
Testing
 ↓
Done
```

Recommended branch:

```text
feat/US-003-upload-garment
```

Recommended commits:

```text
feat(wardrobe): add garment upload endpoint
test(wardrobe): cover garment upload validation
docs(api): update garment upload contract
```

Không direct push vào `main`.

---

# 34. Documentation During a Sprint

Documentation không dừng lại khi Sprint bắt đầu.

Ví dụ Story phát hiện architecture change:

```text
User Story
 ↓
Design discussion
 ↓
New architectural decision
 ↓
ADR
 ↓
ADD / View update
 ↓
Implementation
```

Nếu requirement thay đổi:

```text
Requirement
 ↓
SRS
 ↓
Use Case / Activity Diagram
 ↓
Story / Acceptance Criteria
 ↓
Implementation
 ↓
Tests
```

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
ADD v0.1
4 Architecture Views v0.1
Initial ADRs
Engineering baseline
```

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

## Must Exist Before Sprint 1

```text
BRD baseline
PRD baseline
SRS baseline
Business Rules baseline
Use Case Diagram
core Use Cases
core Activity Diagrams
Product Goal
Product Backlog
Definition of Done
14 QA Analysis baseline
ASR v0.1
ADD v0.1
Logical View v0.1
Implementation View v0.1
Deployment View v0.1
Data View v0.1
major ADRs
Test Strategy baseline
```

“Baseline” nghĩa là đủ dùng để bắt đầu, không phải hoàn hảo.

## Created / Updated Per Feature

```text
User Story
Acceptance Criteria
Activity Diagram when behavior is complex
Sequence Diagram when integration is complex
State Diagram when lifecycle matters
API contract
data design
ADR when architectural decision occurs
tests
```

## Created / Updated Per Sprint

```text
Sprint Goal
Sprint Backlog
Review
Retrospective
Product Backlog
```

## Created / Updated Per Release / Operation

```text
Deployment
Rollback
Monitoring
Postmortem
```

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
| ADD | Architect / Tech Lead | Engineering team |
| Logical View | Architect / Tech Lead | Dev |
| Implementation View | Tech Lead / Senior Dev | Dev |
| Deployment View | Tech Lead / DevOps | Dev |
| Data View | Backend / Data owner / Tech Lead | Team |
| ADR | Decision proposer / Tech Lead | Engineering team |
| UML Sequence Diagram | Developer / Tech Lead | Relevant developers |
| UML State Diagram | Developer / Tech Lead / Analyst | QA, Dev |
| Test Strategy | QA/engineering lead | Dev, PO |
| Sprint Goal | Product Owner + Developers | Scrum Team |
| Sprint Backlog | Developers | Scrum Team |
| Sprint Review Notes | Scrum Team | Stakeholders |
| Retrospective | Scrum Team | Scrum Team |

Với team sinh viên nhỏ, một người có thể giữ nhiều role.

---

# 40. Source-of-Truth Rules

Tránh duplicate cùng một thông tin ở nhiều nơi.

| Information | Source of Truth |
|---|---|
| Business intent | BRD |
| Product behavior | PRD |
| Software requirements | SRS |
| Domain invariants | `business-rules.md` |
| Use-case behavior | Use Case + Activity Diagram |
| Ordered future work | `product-backlog.md` |
| Story behavior | User Story + Acceptance Criteria |
| Architecture drivers | ASR |
| Architecture design | ADD + Views |
| Architectural decision history | ADR |
| HTTP API contract | OpenAPI |
| AI service contract | `ai-service-contract.md` |
| Executable DB schema | migration/schema code |
| Data relationships/ownership | Data View |
| Current code | source code |
| Build/test state | CI |
| Sprint commitments | Sprint Backlog |
| Release state | deployment/release artifacts |

---

# 41. Change Propagation

## Requirement Change Example

Suppose image size changes from 10 MB to 20 MB:

```text
SRS
 ↓
Use Case / Activity if affected
 ↓
User Story / Acceptance Criteria
 ↓
API validation
 ↓
OpenAPI
 ↓
Implementation
 ↓
Tests
```

Architecture documents change only if architecture is affected.

## Architecture Change Example

Suppose AI processing changes:

```text
Synchronous
→ Asynchronous
```

Possible updates:

```text
ASR
ADD
ADR
Logical View
Implementation View
Deployment View
Data View
Sequence Diagram
State Diagram
API Contract
User Stories
Tests
Monitoring
```

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

Architecture is not frozen before Sprint 1.

Start with:

```text
ASR v0.1
ADD v0.1
Logical View v0.1
Implementation View v0.1
Deployment View v0.1
Data View v0.1
```

Then evolve.

Example:

```text
Sprint 3
Recommendation Engine introduces cache need
        ↓
ASR updated
        ↓
ADD updated
        ↓
ADR created
        ↓
Implementation / Deployment / Data View updated
        ↓
Code
```

This is **Agile Architecture**, not Big Design Up Front.

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

The expected traceability chain is:

```text
BRD
Business need: reduce manual wardrobe digitization effort
        ↓
PRD
Feature: AI Digital Closet
        ↓
SRS
FR-WAR-...
FR-AI-...
NFR-PERF-...
        ↓
Use Case
UC-02 Add Garment
        ↓
UML Activity Diagram
ACT-02 Garment Ingestion
        ↓
Product Backlog
US-003 Upload Garment
US-004 Process Garment
US-005 Confirm Attributes
        ↓
14 QA Analysis
Performance / Reliability / Flexibility / Interoperability
        ↓
ASR
AI latency + AI isolation + fallback
        ↓
ADD
Tactics and architecture
        ↓
Logical View
Wardrobe ↔ Garment Processing
        ↓
Implementation View
Spring Boot module ↔ AI adapter ↔ Python service
        ↓
Deployment View
Mobile → Backend → AI Runtime → Storage/DB
        ↓
Data View
Garment + image + attributes + processing status
        ↓
UML Sequence Diagram
Runtime interaction
        ↓
Sprint Backlog
        ↓
Implementation
        ↓
Tests
        ↓
PR / CI / Review
        ↓
Working Increment
```

---

# 46. What the Team Should Do From Today

Follow this order:

| # | Action | Main Output |
|---:|---|---|
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
| 11 | Create initial User Stories for top backlog | `US-*.md` |
| 12 | Define Definition of Done | `definition-of-done.md` |
| 13 | Analyze all 14 Quality Attributes | `quality-attribute-analysis.md` |
| 14 | Identify ASRs and architectural drivers | `ASR.md` |
| 15 | Create ADD baseline | `ADD.md` |
| 16 | Draw Logical View | `logical-view.puml` |
| 17 | Draw Implementation View | `implementation-view.puml` |
| 18 | Draw Deployment View | `deployment-view.puml` |
| 19 | Draw Data View | `data-view.puml` |
| 20 | Record major decisions | ADR files |
| 21 | Define initial API/service contracts | API docs/OpenAPI |
| 22 | Create Test Strategy | `test-strategy.md` |
| 23 | Prepare repository/build/CI/environments | engineering baseline |
| 24 | Refine Sprint 1 stories | Ready stories |
| 25 | Conduct Sprint Planning | Sprint Goal + Sprint Backlog |
| 26 | Start Sprint 1 implementation | Working Increment |
| 27 | Run review/test/CI continuously | quality feedback |
| 28 | Conduct Sprint Review | `review.md` |
| 29 | Conduct Sprint Retrospective | `retrospective.md` |
| 30 | Update backlog/requirements/architecture | evolved baseline |
| 31 | Start next Sprint | repeat |

---

# 47. Final Operating Model

CapsuleAI has three continuously connected tracks.

## Requirements Track

```text
BRD
 ↓
PRD
 ↓
SRS
 ↓
Business Rules
 ↓
Use Cases
 ↓
UML Activity Diagrams
```

## Architecture Track

```text
14 Quality Attribute Analysis
 ↓
ASR
 ↓
ADD
 ├─ Logical View
 ├─ Implementation View
 ├─ Deployment View
 └─ Data View
 ↓
ADR / UML Sequence / UML State / API / Data Design
```

## Scrum Delivery Track

```text
Product Goal
 ↓
Product Backlog
 ↓
Refinement
 ↓
Sprint Goal
 ↓
Sprint Backlog
 ↓
User Stories
 ↓
Implementation
 ↓
Tests / Review / CI
 ↓
Working Increment
 ↓
Sprint Review
 ↓
Retrospective
 ↓
Product Backlog
```

They interact continuously:

```text
Requirements
     ↓
Architecture
     ↓
Product Backlog
     ↓
Sprint
     ↓
Code
     ↓
Feedback
     └────────────→ Requirements / Architecture / Backlog
```

---

# 48. Core Principles

1. **Scrum is the delivery model; Jira is not required.**
2. **All project documentation is version-controlled in the codebase.**
3. **Requirements explain what must be true; architecture explains how the system satisfies the important requirements.**
4. **Evaluate all 14 Quality Attributes, but only significant ones need deep scenarios.**
5. **ASR precedes ADD logically.**
6. **ADD owns the architecture design and the four primary views.**
7. **Logical, Implementation, Deployment and Data Views are mandatory architecture views for CapsuleAI.**
8. **Use Case, Activity, Sequence and State diagrams use UML, stored preferably as PlantUML source.**
9. **Do not draw diagrams merely for documentation volume.**
10. **Product Backlog and User Stories are living artifacts, not frozen plans.**
11. **Architecture evolves during Sprints.**
12. **Every Sprint must aim to produce a usable Increment.**
13. **Acceptance Criteria must be testable.**
14. **Quality Attribute requirements must be measurable/testable.**
15. **Documentation is updated when the underlying truth changes.**
16. **Avoid duplicate sources of truth.**
17. **Prefer traceability over paperwork.**
18. **Prefer simple architecture justified by ASRs over fashionable architecture without requirements.**
19. **Tests and CI are part of development, not a final phase.**
20. **Feedback from Review, testing and production returns to the Product Backlog.**

---

# 49. Instruction for Future AI Models

When an AI model is asked to help continue CapsuleAI, it should:

1. Read this workflow first.
2. Identify the current project phase and Sprint.
3. Inspect existing documents before creating new ones.
4. Preserve IDs such as `FR-*`, `NFR-*`, `BR-*`, `UC-*`, `US-*`, `ASR-*`, `QA-*`, `ADR-*`.
5. Follow the source-of-truth rules.
6. Do not duplicate information across files unnecessarily.
7. Maintain traceability among requirements, stories, architecture and tests.
8. Use UML/PlantUML for UML diagrams.
9. Keep the four ADD architecture views synchronized with the actual system.
10. Update ASR/ADD/ADR only when the corresponding architectural concern changes.
11. Do not over-engineer the system.
12. Do not introduce a technology unless a requirement, ASR or engineering constraint justifies it.
13. Preserve Scrum's iterative nature rather than turning the workflow into Waterfall.
14. Prioritize a working Increment every Sprint.
15. Treat this document as the high-level process authority for CapsuleAI unless the team explicitly revises it.

