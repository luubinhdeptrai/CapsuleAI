# UC-015 — View Wear History

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-015 |
| Use Case Name | View Wear History |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Review understandable chronological records of reported outfit use, including separate intentional same-day events and preserved historical garment references in their original event-local context.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Understand what was reported and identify a particular event for correction/removal. |

## 4. Preconditions

- The User has authenticated access authorized for personal Wear history. Existing events are not required to open the history.

## 5. Trigger

The User requests Wear history.

## 6. Main Success Scenario

1. The User requests personal Wear history.
2. CapsuleAI distinguishes loading/unavailable history from loaded accepted events and presents the available chronological history.
3. The User selects a specific event to inspect.
4. CapsuleAI shows its reported outfit/garment snapshot, original event-local date/time and available occasion/context, labeling the report as user-reported.
5. The User reviews the event or chooses a separate correction/removal action.

## 7. Alternative Flows

### A1 — No Wear Events

At Main Step 2:

1. CapsuleAI shows an honest no-events state without invented frequency or variety statistics.
2. The User may start an explicit Wear report through UC-014 or leave the history.

### A2 — Repeated outfit or multiple events in one day

At Main Step 2:

1. CapsuleAI shows each legitimate event separately, even when outfit and original local day match.
2. The User selects the particular event at Main Step 3; daily ranking normalization does not delete or merge the reports.

### A3 — Device timezone has changed

At Main Step 4:

1. CapsuleAI retains the event's original local date/time and associated timezone/offset interpretation.
2. The event is not automatically re-dated to the current location; continue at Main Step 5.

### A4 — Removed garment or event older than ranking horizon

At Main Step 4:

1. CapsuleAI preserves understandable historical information subject to retention policy and labels a removed item as Removed from wardrobe.
2. The snapshot does not become current owned inventory; passing the 90-day ranking horizon alone does not erase history.

## 8. Exception / Failure Flows

### E1 — History/detail cannot be retrieved

At Main Step 2:

1. CapsuleAI explains unavailable information and allows retry or return to available history/context.
2. It does not represent failed retrieval as no Wear Events and does not change accepted reports.

### E2 — Selected event is no longer effective

At Main Step 4:

1. CapsuleAI explains the changed/removed event and refreshes available history.
2. The User can select another current history event; no deleted report is silently restored.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Available accepted events are understandable and distinct, with original event-local meaning and historical/current ownership distinguished.
- Viewing history does not create/correct/remove wear, feedback or garments.

## 10. Minimal / Failure Postconditions

- Failed retrieval leaves accepted history, garments and preference state unchanged.
- Unavailable information is not an empty-history claim; historical snapshots do not restore current ownership.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-WEAR-002` | Retain distinct intentional reports even when outfit/day match. |
| `BRULE-WEAR-003` | Present original event-local interpretation after travel. |
| `BRULE-HIST-001` | History is reported evidence with incomplete-logging limitations. |
| `BRULE-HIST-002` | Preserve understandable removed-garment snapshots without current ownership. |
| `BRULE-HIST-003` | The 90-day ranking horizon is not a retention limit. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-ANL-001`, `FEAT-PERS-002` |
| [Software Requirements — Functional](../SRS.md) | `FR-ANL-001`, `FR-ANL-002`, `FR-ANL-004`, `FR-ANL-005`, `FR-ANL-006`, `FR-WEAR-004`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-HIST-001`, `DATA-HIST-002`, `DATA-RET-002`, `DATA-WEAR-002`, `DATA-RET-001` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-007`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-005`, `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-WEAR-002`, `BRULE-WEAR-003`, `BRULE-HIST-001`, `BRULE-HIST-002`, `BRULE-HIST-003`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-006`, `BR-011`, `BR-022`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-07`, `CAP-06` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- A no-report result establishes only no recorded use, never that an outfit/garment was never physically worn.
- History must keep individual events identifiable for the correct correction/removal target without requiring internal storage identifiers in the UI.

- Necessary active history may remain beyond ranking expiry. Removed-garment references retain only minimal understandable snapshots, with original images/nonessential removed data deleted within 30 days. Removed events stop effective history use immediately and applicable personal data is deleted within 30 days.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-014 — Record Wear Event](UC-014-record-wear-event.md): Create an explicit report.
- [UC-016 — Correct Wear Event](UC-016-correct-wear-event.md): Correct a selected event.
- [UC-017 — Remove Wear Event](UC-017-remove-wear-event.md): Remove a selected event.
- [UC-018 — View Wardrobe Utilization](UC-018-view-wardrobe-utilization.md): Review indicators derived from accepted reports.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-015-view-wear-history.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
