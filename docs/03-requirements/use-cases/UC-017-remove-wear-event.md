# UC-017 — Remove Wear Event

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-017 |
| Use Case Name | Remove Wear Event |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Remove one identified Wear Event from effective history, with corresponding utilization/recency/personalization updates while retaining unrelated events and the underlying outfit and garments.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Withdraw one report without losing other reports or owned/historical garment information. |

## 4. Preconditions

- The User has authenticated access authorized for a particular existing effective Wear Event.

## 5. Trigger

The User explicitly chooses removal of the identified Wear Event.

## 6. Main Success Scenario

1. The User identifies the specific event and requests its removal.
2. CapsuleAI identifies the targeted report and explains that removal concerns that report, with the outfit/garments and other reports retained.
3. The User proceeds with the explicit individual removal intention.
4. CapsuleAI accepts removal and shows that the event is no longer effective in history.
5. The User reviews available history/utilization; relevant recency and normalized ranking evidence reflect surviving reports.

## 7. Alternative Flows

### A1 — Cancel pending removal

At Main Step 3:

1. The User cancels before removal is accepted.
2. CapsuleAI keeps the accepted event and its effects unchanged; the use case ends without removal.

### A2 — Other same-outfit/day reports survive

At Main Step 5:

1. CapsuleAI keeps the remaining legitimate reports separately in history.
2. The same exact outfit/original-day evidence group remains subject to one increment, using the latest surviving accepted timestamp; removing the latest event updates that anchor.

### A3 — Last effective report for the evidence group

At Main Step 5:

1. No effective report remains for that exact outfit/original local day.
2. That group's Wear-based evidence ceases; unrelated groups, events, feedback, outfit and garments remain.

## 8. Exception / Failure Flows

### E1 — Known failed or uncertain removal

At Main Step 4:

1. CapsuleAI distinguishes known failure/rejection from an unconfirmed response.
2. Known failure retains the event and effects. An uncertain outcome is reviewed against the selected event/history or retried safely; it is not asserted as accepted removal or as definitely unchanged.

### E2 — Selected event is already removed or has changed

At Main Step 2:

1. CapsuleAI shows the available current history/state rather than removing a different report.
2. The User verifies the intended target; no other same-day or same-outfit report is removed automatically.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The accepted removal makes only the selected event ineffective in history and derived utilization/recency/evidence.
- Underlying outfits/garments and unrelated reports remain; affected daily normalized evidence and latest surviving accepted timestamp reflect survivors.

## 10. Minimal / Failure Postconditions

- Canceled or known failed removal leaves the accepted event/effects intact; uncertain outcome is not falsely reported as accepted.
- No underlying garment/outfit or unrelated report is deleted by this action; no removed event is counted as effective after accepted removal.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-WEAR-005` | Remove an individual event from effective history; physical deletion mechanics remain downstream. |
| `BRULE-WEAR-006` | Accepted removal updates effective report-derived effects. |
| `BRULE-PERS-005` | Surviving group evidence uses the latest surviving accepted event anchor; no survivors means no group Wear increment. |
| `BRULE-PERS-006` | Recompute relevant evidence after removal. |
| `BRULE-HIST-001` | Historical report counts are not collapsed into normalized ranking increments. |
| `BRULE-HIST-003` | Ranking aging does not define physical retention/deletion policy. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-PERS-002`, `FEAT-ANL-001`, `FEAT-PERS-003`, `FEAT-MET-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WEAR-007`, `FR-WEAR-008`, `FR-WEAR-010`, `FR-WEAR-012`, `FR-ANL-007`, `FR-PERS-012`, `FR-PERS-013`, `FR-MET-002`, `FR-MET-005`, `FR-MET-007`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-WEAR-003`, `DATA-WEAR-004`, `DATA-RET-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-007`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-WEAR-005`, `BRULE-WEAR-006`, `BRULE-PERS-005`, `BRULE-PERS-006`, `BRULE-HIST-001`, `BRULE-HIST-003` |
| [Business Requirements](../../01-business/BRD.md) | `BR-006`, `BR-011`, `BR-021`, `BR-022`, `BR-019` |
| [Capabilities](../../01-business/BRD.md) | `CAP-06`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- Removal is an effective-history outcome; this use case does not prescribe physical erasure, audit retention or persistence design.
- The withdrawal of one report must not remove all reports sharing its outfit/local day.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-015 — View Wear History](UC-015-view-wear-history.md): Identify/review the specific event and surviving history.
- [UC-016 — Correct Wear Event](UC-016-correct-wear-event.md): Correct a report instead of withdrawing it.
- [UC-018 — View Wardrobe Utilization](UC-018-view-wardrobe-utilization.md): Review utilization after accepted removal.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-011` | Final event retention/privacy/deletion policy remains at its SRS gate; effective removal behavior is specified without selecting physical deletion mechanics. |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
