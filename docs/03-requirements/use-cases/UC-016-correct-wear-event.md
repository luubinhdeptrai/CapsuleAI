# UC-016 — Correct Wear Event

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-016 |
| Use Case Name | Correct Wear Event |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Correct a specific accepted Wear Event's applicable outfit, occasion/context or local time, preserving the original local calendar day and updating effective history/evidence without altering unrelated reports.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Repair an inaccurate report with clear acceptance or rejection and no loss of unrelated history. |

## 4. Preconditions

- The User has authenticated access authorized for a particular existing effective Wear Event.

## 5. Trigger

The User chooses to correct the identified Wear Event.

## 6. Main Success Scenario

1. The User selects the particular event and requests correction.
2. CapsuleAI shows the existing outfit, available context and original event-local date/time meaning.
3. The User changes the applicable outfit, occasion/context and/or local time and submits the correction.
4. CapsuleAI confirms that the submitted correction meets the supported-field, non-future-time and original-local-day conditions; invalid proposals follow E1.
5. CapsuleAI accepts the valid correction and shows the revised event while preserving its original event-local calendar-day meaning.
6. The User reviews the accepted result; effective history/utilization/recency and normalized age-weighted evidence reflect the correction.

## 7. Alternative Flows

### A1 — Context-only or outfit-only correction

At Main Step 3:

1. The User changes only applicable outfit/context information.
2. CapsuleAI preserves unchanged event-local time/day and applies Main Steps 4–6 to the supported changes; the correction does not restore garment ownership.

### A2 — Valid local-time correction

At Main Step 3:

1. The User supplies a non-future local time within the event's original local calendar day.
2. After acceptance, the event's corrected absolute timestamp is reflected in elapsed-time aging and the applicable latest-surviving-event evidence anchor.

### A3 — Cancel before acceptance

At Main Step 3:

1. The User cancels the pending correction before it is accepted.
2. CapsuleAI leaves the previous accepted event and effective evidence unchanged; the use case ends without correction.

## 8. Exception / Failure Flows

### E1 — Invalid future or cross-day time / unsupported correction

At Main Step 4:

1. CapsuleAI explains the invalid correction, including the original event-local-day boundary where relevant.
2. The previous accepted event remains authoritative. The User can revise at Main Step 3 or cancel; current device timezone does not permit moving the event to another original day.

### E2 — Rejected or uncertain correction

At Main Step 5:

1. CapsuleAI distinguishes known failure/rejection from an unconfirmed response.
2. Known failure leaves the prior event/evidence intact. For an uncertain outcome, the User reviews the identified event or retries safely; an unconfirmed proposal is not shown as accepted and no new event is created.

### E3 — Selected event is no longer effective

At Main Step 2:

1. CapsuleAI explains that the selected event has changed/been removed and shows available history.
2. The User selects the applicable existing event again; no unrelated event receives the correction.

### E4 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The accepted correction affects only the selected event's supported fields, with non-future corrected time within the original event-local day.
- Effective history/utilization and normalized ranking evidence reflect the corrected event. Changed outfit/time updates affected group membership and latest surviving accepted absolute timestamp; unrelated reports remain.

## 10. Minimal / Failure Postconditions

- Known invalid, canceled or failed correction leaves the previous accepted event/evidence unchanged; an uncertain response does not prove unchanged or accepted state.
- Correction does not create another Wear Event, restore removed ownership or delete underlying outfits/garments.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-WEAR-004` | Correct an individual event within its original local day and reject future time. |
| `BRULE-WEAR-003` | Original local meaning persists; the valid corrected absolute timestamp governs elapsed time. |
| `BRULE-WEAR-006` | Only accepted corrections alter effective history/utilization/evidence. |
| `BRULE-PERS-005` | Update affected exact-outfit/original-day groups and latest surviving accepted event anchor. |
| `BRULE-PERS-006` | Recompute effective normalized/aged evidence after correction. |
| `BRULE-PERS-003` | Use completed elapsed 24-hour periods, not current-timezone calendar subtraction. |
| `BRULE-HIST-002` | Historical snapshots remain distinct from current owned inventory. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-PERS-002`, `FEAT-ANL-001`, `FEAT-PERS-003`, `FEAT-MET-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WEAR-004`, `FR-WEAR-006`, `FR-WEAR-010`, `FR-WEAR-012`, `FR-ANL-007`, `FR-PERS-012`, `FR-PERS-013`, `FR-MET-002`, `FR-MET-005`, `FR-MET-007`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-WEAR-002`, `DATA-WEAR-003`, `DATA-WEAR-004`, `DATA-HIST-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-WEAR-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-007`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-005`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-WEAR-004`, `BRULE-WEAR-003`, `BRULE-WEAR-006`, `BRULE-PERS-005`, `BRULE-PERS-006`, `BRULE-PERS-003`, `BRULE-HIST-002` |
| [Business Requirements](../../01-business/BRD.md) | `BR-006`, `BR-011`, `BR-021`, `BR-022`, `BR-019` |
| [Capabilities](../../01-business/BRD.md) | `CAP-06`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- The original event-local calendar day is a correction boundary; travel does not reinterpret it. The resolved aging/day-group policy is not an open issue.
- Only supported applicable corrections are offered. No new backdated-creation, future scheduling or cross-day relocation feature is introduced.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-015 — View Wear History](UC-015-view-wear-history.md): Identify/review the specific event.
- [UC-017 — Remove Wear Event](UC-017-remove-wear-event.md): Remove the event instead of correcting it.
- [UC-018 — View Wardrobe Utilization](UC-018-view-wardrobe-utilization.md): Review utilization reflecting accepted corrections.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-011` | Event privacy and final retention/deletion policy remain open; no physical data-deletion method is prescribed. |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
