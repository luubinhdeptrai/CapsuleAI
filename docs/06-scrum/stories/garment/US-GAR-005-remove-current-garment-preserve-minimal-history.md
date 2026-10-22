# US-GAR-005 — Remove a garment while preserving minimal history

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-GAR-005 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-016](../../product-backlog.md#pbi-016--remove-a-garment-while-preserving-minimal-historical-meaning) |
| Domain | Garment |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Trustworthy Wardrobe Understanding; Greater Owned-Garment Value |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated owner, I want to deliberately remove a garment from my current wardrobe, so that current advice reflects my possessions while past reports remain understandable.

## 2. Value

Keep current advice aligned with actual possessions without making past reports unintelligible.

## 3. Scope

Selected-garment confirmation/cancellation, immediate ownership/assessment exclusion, minimal historical presentation and applicable 30-day physical deletion.

## 4. Preconditions

- The selected garment is currently owned and authorized for the user; changed/unavailable selections are handled rather than assumed current.

## 5. Acceptance Criteria

### AC-01 — Confirm the intended removal

Identify the selected garment and explain current-ownership removal and historical meaning. Only deliberate confirmation applies removal; cancellation preserves the accepted garment and dependent state.

### AC-02 — Exclude immediately

On accepted removal, exclude the garment from current inventory, new current outfits, current Coverage and the Multiplier baseline. Old snapshots cannot restore eligibility.

### AC-03 — Refresh dependent results

Refresh affected prior advice/assessments or clearly mark them outdated. Failed refresh is not a current result using removed inventory.

### AC-04 — Keep minimal historical meaning

When past reports reference the garment, retain only necessary understandable snapshots labeled Removed from wardrobe. Do not delete unrelated Wear Events/outfits/garments merely because this garment was removed.

### AC-05 — Delete applicable removed data

Physically delete the original removed-garment image and nonessential removed personal data within 30 days of accepted removal; necessary minimal historical meaning does not authorize indefinite original-image retention.

### AC-06 — Resolve failure or uncertainty

Known rejected/failed removal keeps the accepted current state; uncertainty does not prove either removal or unchanged ownership. Offer outcome review and safe retry without removing another item or restoring an already removed selection.

### AC-07 — Enforce ownership

Wrong-account/unusable-session attempts cannot disclose or remove the selected garment. A changed/already unavailable target yields current-state guidance rather than a different mutation.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-007.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Wear Event withdrawal is [US-WEAR-004](../wear/US-WEAR-004-withdraw-one-wear-event.md); privacy assurance is [US-PRIV-002](../privacy/US-PRIV-002-verify-mvp-authorization-privacy-and-retention.md). No archive/restore, account deletion or automatic erasure of all historical reports is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-016](../../product-backlog.md#pbi-016--remove-a-garment-while-preserving-minimal-historical-meaning); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BR-005, BR-006, BR-021, CAP-03 |
| Product | [PRD](../../../02-product/PRD.md): JRN-03, FEAT-WAR-002 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WAR-004–FR-WAR-006; DATA-INT-002; DATA-HIST-001, DATA-HIST-002; DATA-RET-002; COM-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-GAR-009, BRULE-OUT-002, BRULE-HIST-002, BRULE-HIST-003, BRULE-COV-006, BRULE-MULT-008 |
| Use Cases | [UC-010](../../../03-requirements/use-cases/UC-010-remove-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | No dedicated activity for this slice is required by the current baseline; the linked textual Use Cases govern relevant behavior. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

The removal/deletion policies are established. Detailed minimal-snapshot fields, physical purge and recoverable integration mechanisms remain selected-story JIT work and must meet the existing deadline.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
