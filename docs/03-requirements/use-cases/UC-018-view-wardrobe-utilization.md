# UC-018 — View Wardrobe Utilization

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-018 |
| Use Case Name | View Wardrobe Utilization |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Understand wardrobe use through recorded frequency, recency, variety and overlooked-item indicators derived from accepted Wear Events, with incomplete logging and historical ownership clearly explained.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Identify useful owned items and interpret recorded-use indicators without unsupported claims about physical behavior. |

## 4. Preconditions

- The User has authenticated access authorized for personal wardrobe/utilization information. Wear Events need not already exist.

## 5. Trigger

The User requests wardrobe utilization insights.

## 6. Main Success Scenario

1. The User requests utilization information.
2. CapsuleAI presents available reported-use frequency/recency and supported variety/overlooked-item indicators from accepted events.
3. The User selects an item or available indicator to inspect its meaning.
4. CapsuleAI explains the recorded evidence and incomplete-logging limitations, keeping current owned items and historical removed references distinct.
5. The User reviews the insight or proceeds to the relevant wardrobe/history action.

## 7. Alternative Flows

### A1 — No recorded events

At Main Step 2:

1. CapsuleAI shows a no-Wear-Events state without invented frequency, diversity or recency statistics.
2. The User can report wear separately or leave the insight.

### A2 — An item has no recorded use

At Main Step 4:

1. CapsuleAI labels the item as having no recorded use, not as never physically worn.
2. The User may inspect the current garment or decide to report an actual use separately.

### A3 — Intentional repeat reports and aging

At Main Step 2:

1. Utilization/history retains legitimate repeated reports, including same-outfit/day and retained events beyond the ranking horizon.
2. The capped/aged ranking contribution is not substituted for the recorded event count; continue at Main Step 3.

### A4 — Historical removed garment

At Main Step 4:

1. CapsuleAI labels the historical reference Removed from wardrobe where presented.
2. It does not count the removed item as current owned inventory or restore it through insight viewing.

## 8. Exception / Failure Flows

### E1 — Utilization information unavailable

At Main Step 2:

1. CapsuleAI explains the unavailable result and offers retry or return to available wardrobe/history.
2. It does not replace retrieval failure with a no-events/no-use claim or invented statistics.

### E2 — Dependent event change is not yet reflected

At Main Step 4:

1. CapsuleAI reflects accepted correction/removal in effective indicators and distinguishes information that cannot yet be refreshed.
2. The User can retry/review the relevant accepted history; an unaccepted proposed event change does not become evidence.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Displayed indicators are based on available accepted reported events, with incomplete logging and current/historical distinctions clear.
- Viewing does not create events, ownership or feedback; accepted corrections/removals govern effective evidence.

## 10. Minimal / Failure Postconditions

- Failed insight retrieval does not modify accepted wardrobe/history.
- Absent reports do not establish absence of physical wear; unavailable indicators are not fabricated.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-HIST-001` | Reported frequency/recency/variety is evidence with incomplete-logging limitations. |
| `BRULE-HIST-002` | Removed historical references remain separate from current inventory. |
| `BRULE-HIST-003` | The ranking horizon is not a history-retention cutoff. |
| `BRULE-WEAR-006` | Accepted mutations affect effective utilization. |
| `BRULE-PERS-005` | Normalized daily ranking increments do not replace distinct historical report counts. |
| `BRULE-PERS-004` | Any applicable overlooked-item ranking adjustment is soft, not proof of non-use. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-ANL-001`, `FEAT-PERS-003` |
| [Software Requirements — Functional](../SRS.md) | `FR-ANL-003`, `FR-ANL-004`, `FR-ANL-005`, `FR-ANL-006`, `FR-ANL-007`, `FR-PERS-009`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-HIST-001`, `DATA-HIST-002`, `DATA-RET-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-HIST-001`, `BRULE-HIST-002`, `BRULE-HIST-003`, `BRULE-WEAR-006`, `BRULE-PERS-005`, `BRULE-PERS-004` |
| [Business Requirements](../../01-business/BRD.md) | `BR-006`, `BR-011`, `BR-022`, `BR-024` |
| [Capabilities](../../01-business/BRD.md) | `CAP-07`, `CAP-06` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- No universal physical-wear or compulsory-purchase claim follows from an overlooked-item indicator.
- Event-local display and absolute elapsed-time aging retain their separate purposes; the 90-day influence window does not truncate retained reported-use history.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-008 — View Wardrobe](UC-008-view-wardrobe.md): Inspect current owned garments.
- [UC-014 — Record Wear Event](UC-014-record-wear-event.md): Record an explicit use.
- [UC-015 — View Wear History](UC-015-view-wear-history.md): Review the underlying reported history.
- [UC-016 — Correct Wear Event](UC-016-correct-wear-event.md): Correct inaccurate report evidence.
- [UC-017 — Remove Wear Event](UC-017-remove-wear-event.md): Remove a specific report.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-011` | Final history/privacy/retention/deletion policy constrains retained evidence but remains governed by the SRS. |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
