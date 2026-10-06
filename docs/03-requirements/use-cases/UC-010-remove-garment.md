# UC-010 — Remove Garment

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-010 |
| Use Case Name | Remove Garment |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Deliberately remove a garment from current wardrobe ownership so future current advice excludes it while past reported outfits remain understandable.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Keep current possessions accurate while retaining the meaning of past reports. |

## 4. Preconditions

- The User has authenticated access authorized for an existing current owned garment.

## 5. Trigger

The User chooses to remove the selected garment.

## 6. Main Success Scenario

1. The User requests removal of the selected current garment.
2. CapsuleAI identifies the affected garment and explains removal from current ownership and the preservation of understandable history.
3. The User deliberately confirms removal.
4. CapsuleAI acknowledges accepted removal and refreshes the current wardrobe without that garment.
5. CapsuleAI refreshes affected current recommendations/Coverage/Multiplier or marks them for reevaluation; new current evaluations exclude the garment.
6. When historical reports reference it, CapsuleAI retains understandable snapshots labeled Removed from wardrobe.

## 7. Alternative Flows

### A1 — Cancel removal

At Main Step 3:

1. The User cancels rather than confirms.
2. CapsuleAI leaves current ownership, accepted profile and dependent state unchanged.

## 8. Exception / Failure Flows

### E1 — Removal failed or outcome uncertain

At Main Step 4:

1. CapsuleAI distinguishes known failure from an unconfirmed outcome and does not claim completed removal.
2. On known failure the garment remains current; after uncertainty the User reviews current ownership before safe retry.

### E2 — Selected garment is already unavailable/current state changed

At Main Step 3:

1. CapsuleAI explains the actual current state rather than applying removal to another item or restoring old ownership.
2. The User returns to the current wardrobe; historical references remain governed separately.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The selected garment is excluded from active inventory, new current outfits, current Coverage and the current Multiplier baseline.
- Affected prior advice is refreshed or visibly outdated; past Wear Events/outfit/garment snapshots remain understandable with removed labeling.

## 10. Minimal / Failure Postconditions

- Canceled/rejected/known failed removal leaves the accepted current garment state intact.
- Uncertain removal is not labeled accepted before outcome review.
- No Wear Event, underlying historical Outfit or unrelated garment is deleted solely because ownership changed.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-009` | Accepted removal changes active state and affected advice; failed/canceled work does not. |
| `BRULE-OUT-002` | Removed items cannot enter new current daily outfits. |
| `BRULE-HIST-002` | Removed historical garments remain understandable without restoring current eligibility. |
| `BRULE-HIST-003` | Ownership removal is not history-retention expiry. |
| `BRULE-COV-006` | Current Coverage must reflect ownership changes. |
| `BRULE-MULT-008` | Changed current inventory invalidates prior candidate comparisons. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-03` |
| [Product Features](../../02-product/PRD.md) | `FEAT-WAR-002`, `FEAT-ANL-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WAR-004`, `FR-WAR-005`, `FR-WAR-006`, `FR-ANL-006`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-005`, `DATA-INT-002`, `DATA-INT-004`, `DATA-HIST-001`, `DATA-HIST-002`, `DATA-RET-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-ANL-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-009`, `BRULE-OUT-002`, `BRULE-HIST-002`, `BRULE-HIST-003`, `BRULE-COV-006`, `BRULE-MULT-008` |
| [Business Requirements](../../01-business/BRD.md) | `BR-005`, `BR-006`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-03`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- This is active-ownership removal, not automatic erasure of historical reports or an archive/restore feature.
- Removed from wardrobe labeling must be understandable without relying only on the garment image/color.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-008 — View Wardrobe](UC-008-view-wardrobe.md): Confirm the current inventory after removal.
- [UC-015 — View Wear History](UC-015-view-wear-history.md): Inspect preserved historical reports.
- [UC-017 — Remove Wear Event](UC-017-remove-wear-event.md): Remove a specific Wear Event as a separate intention.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-011` | Retention, physical deletion and privacy handling of garment images/historical snapshots remain unresolved at final privacy/data acceptance; effective current exclusion is already required. |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
