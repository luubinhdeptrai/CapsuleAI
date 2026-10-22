# US-PRIV-002 — Verify authorization, privacy and retention across the MVP

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Quality Story |
| Story ID | US-PRIV-002 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-041](../../product-backlog.md#pbi-041--verify-authorization-privacy-and-retention-across-the-mvp) |
| Domain | Privacy |
| Theme | Quality / Security / Operations |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Enabling / Protecting Product Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

Provide integrated evidence that delivered wardrobe value preserves authorized private access, user control and the existing removal/retention policies.

## 2. Value

Provide evidence that evolving product value continues to respect private access and user control.

## 3. Scope

Cross-journey access/session assurance, minimal external exchange/no personal-data training, immediate removal and distinct 30-day/90-day lifecycle evidence, reusing owning-feature checks.

## 4. Preconditions

- PBI-002/PBI-009/PBI-016/PBI-023 capabilities and the affected delivered journeys exist; first-operation protection is not deferred until this review.

## 5. Acceptance Criteria

### AC-01 — Verify integrated isolation

Agreed signed-out, unusable-session and other-account attempts across affected wardrobe/images/profile/feedback/history/assessments have zero successful unauthorized access. Account switch/logout exposes no preceding-account protected content.

### AC-02 — Verify session distinctions

Current-session logout, rotated-token reuse and completed-reset all-account revocation retain their source-specific effects. A valid unexpired token without usable account/session authority grants no bypass; credentials/token/recovery secrets are not exposed.

### AC-03 — Inspect purpose and disclosure

Processing remains necessary/private with omission/removal/permission controls. Weather receives needed selected location, recovery delivery needed email/instructions and shopping only explicit chosen navigation; no unrelated private wardrobe/profile/history, personal-data sale/public sharing or MVP personal-data AI training/improvement is introduced.

### AC-04 — Verify garment lifecycle

Accepted garment removal immediately excludes current ownership/advice/Coverage/Multiplier; original images/nonessential removed personal data are physically deleted within 30 days. Only necessary understandable minimal historical snapshots may remain, labeled removed.

### AC-05 — Verify event lifecycle

Accepted individual event removal immediately excludes its effective history/utilization/recency/personalization, recomputes survivors and physically deletes applicable personal event data within 30 days, preserving unrelated reports and underlying outfits/garments.

### AC-06 — Verify measurement lifecycle

User-linked engagement/measurement is deleted or aggregated/de-identified within 90 days, with no older user-linked measurement retained. This is distinct from the 90-day ranking window and necessary unremoved functional history/feedback; it cannot extend a 30-day removal deadline.

### AC-07 — Provide real privacy-safe evidence

Verify actual authorization, persistent effects and applicable physical deletion/resumption with the affected integrations; clock-only policy tests or mocks alone do not prove purge execution. Evidence records basis, outcomes and limitations without leaking private content; failed required behavior is not accepted as passing assurance.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-002, PBI-009, PBI-016, PBI-023.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Protection/lifecycle implementation remains [US-PRIV-001](../privacy/US-PRIV-001-protect-first-private-operations.md) and the owning garment/event/measurement stories. No account deletion/export feature, legal certification, new privacy policy or model-training consent flow is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-041](../../product-backlog.md#pbi-041--verify-authorization-privacy-and-retention-across-the-mvp); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-021, CAP-01 |
| Product | [PRD](../../../02-product/PRD.md): FEAT-AUTH-001; Section 23 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-AUTH-008–FR-AUTH-010; NFR-SEC-001–NFR-SEC-003, NFR-PRIV-001, NFR-PRIV-002; DATA-RET-001–DATA-RET-003; DATA-HIST-001, DATA-HIST-002; SI-002, SI-003; COM-003; SRS Section 12.1 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-006, BRULE-AUTH-007, BRULE-GAR-009, BRULE-WEAR-005, BRULE-HIST-002, BRULE-HIST-003 |
| Use Cases | [UC-001](../../../03-requirements/use-cases/UC-001-register-account.md); [UC-002](../../../03-requirements/use-cases/UC-002-log-in.md); [UC-003](../../../03-requirements/use-cases/UC-003-log-out.md); [UC-004](../../../03-requirements/use-cases/UC-004-reset-password.md); [UC-006](../../../03-requirements/use-cases/UC-006-set-location-context.md); [UC-007](../../../03-requirements/use-cases/UC-007-add-garment.md); [UC-010](../../../03-requirements/use-cases/UC-010-remove-garment.md); [UC-013](../../../03-requirements/use-cases/UC-013-provide-outfit-feedback.md); [UC-014](../../../03-requirements/use-cases/UC-014-record-wear-event.md); [UC-017](../../../03-requirements/use-cases/UC-017-remove-wear-event.md); [UC-023](../../../03-requirements/use-cases/UC-023-open-external-shopping-link.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-004](../../../03-requirements/activity-diagrams/AD-004-reset-password.puml); [AD-006](../../../03-requirements/activity-diagrams/AD-006-set-location-context.puml); [AD-007](../../../03-requirements/activity-diagrams/AD-007-add-garment.puml); [AD-017](../../../03-requirements/activity-diagrams/AD-017-remove-wear-event.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The privacy/retention policies are established. Developers/QA define the actual delivered-scope scenarios and applicable deletion evidence; no missing feature or test limitation can be reported as proof of compliance.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
