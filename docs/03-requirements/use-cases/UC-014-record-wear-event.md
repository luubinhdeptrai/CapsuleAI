# UC-014 — Record Wear Event

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-014 |
| Use Case Name | Record Wear Event |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Explicitly report wear of a current valid owned outfit as a distinct intentional Wear Event, preserving event-local time and updating reported history and soft personalization without claiming verified physical wear.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Record each intended report accurately, retry safely and retain meaningful history. |

## 4. Preconditions

- The User has authenticated access to an identified outfit composed of owned garments. The outfit must still satisfy current eligibility/validity when the action is accepted.

## 5. Trigger

The User explicitly initiates Wear This Today for the identified owned outfit.

## 6. Main Success Scenario

1. The User selects the owned outfit and explicitly initiates Wear This Today.
2. CapsuleAI identifies the reported outfit and available occasion/recommendation context, distinguishing the action from viewing or preference feedback.
3. CapsuleAI accepts one Wear Event for this logical intention, defaulting the event time to the accepted current time and associating its absolute timestamp, event timezone/UTC offset and original local date/time.
4. CapsuleAI shows that the report was accepted and is available in history as user-reported wear, with available context only.
5. The User may review the event or continue using the wardrobe; accepted history/utilization/recency and normalized soft evidence reflect the report.

## 7. Alternative Flows

### A1 — Another intentional report for the same outfit/day

At Main Step 1:

1. The User makes a new explicit Wear This Today initiation for the same valid outfit on the same event-local calendar day.
2. CapsuleAI accepts a separate legitimate event through Main Steps 2–4; it does not collapse the reports into one history record. Same-outfit/day ranking evidence remains capped.

### A2 — Different outfit on the same local day

At Main Step 1:

1. The User intentionally reports another valid owned outfit.
2. CapsuleAI retains earlier events and creates the new distinct report through Main Steps 2–4.

### A3 — Retry of the same logical action

At Main Step 3:

1. The User retries an unconfirmed initiation or the same action is delivered again.
2. CapsuleAI recognizes the same logical intention and shows its single accepted event when accepted, without creating an extra report; continue at Main Step 4.

### A4 — Optional report context absent

At Main Step 2:

1. CapsuleAI has no occasion or recommendation context for the action.
2. It retains the available outfit/time information without fabricating missing context and continues at Main Step 3.

## 8. Exception / Failure Flows

### E1 — Outfit is no longer currently eligible/valid

At Main Step 3:

1. CapsuleAI explains the changed ownership/readiness/validity limitation and does not accept a new Wear Event for an invalid current outfit.
2. The User may refresh current recommendations or correct wardrobe/context and deliberately initiate an eligible report. Historical snapshots and candidate previews cannot be used as current owned proof.

### E2 — Known rejected creation or uncertain response

At Main Step 3:

1. CapsuleAI distinguishes a known rejected/failed action from an unconfirmed outcome.
2. Known failure creates no accepted event. If the outcome is uncertain, the User may review history or retry the same logical action; it is not presented as accepted until established and cannot create a duplicate solely because of retry.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- One accepted event exists for the logical action, with the outfit, accepted absolute time, original event-local context and user-reported nature.
- History retains distinct intentional reports; applicable normalized evidence allows at most one Wear-based preference increment for the exact outfit/original local day.

## 10. Minimal / Failure Postconditions

- Known failed creation adds no accepted event; an uncertain response does not falsely establish either acceptance or absence.
- Existing events, garment ownership and feedback remain intact; a safe retry of the same intention does not multiply history.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-WEAR-001` | One explicit logical report defaults to its accepted current time on a valid owned outfit. |
| `BRULE-WEAR-002` | Retry identity differs from a new intention, including repeated same-outfit/day reports. |
| `BRULE-WEAR-003` | Preserve original event-local context after travel; absolute elapsed time governs aging. |
| `BRULE-WEAR-006` | Accepted reports affect history/utilization and effective ranking evidence. |
| `BRULE-PERS-002` | Wear contributes conceptual +2 before decay, stronger than Like. |
| `BRULE-PERS-003` | Apply completed elapsed 24-hour aging periods; expiry of influence is not deletion. |
| `BRULE-PERS-005` | Same exact outfit/original day contributes at most one increment, anchored to the latest surviving accepted timestamp. |
| `BRULE-HIST-001` | Reports are evidence supplied by the User, not verified physical wear. |
| `BRULE-OUT-002` | Current owned eligibility excludes historical removed and hypothetical candidate items. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-04`, `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-PERS-002`, `FEAT-PERS-003`, `FEAT-ANL-001`, `FEAT-MET-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WEAR-001`, `FR-WEAR-002`, `FR-WEAR-003`, `FR-WEAR-004`, `FR-WEAR-005`, `FR-WEAR-009`, `FR-WEAR-010`, `FR-WEAR-011`, `FR-PERS-008`, `FR-PERS-012`, `FR-ANL-001`, `FR-ANL-002`, `FR-MET-002`, `FR-MET-005`, `FR-MET-007`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-OUT-001`, `DATA-WEAR-002`, `DATA-WEAR-003`, `DATA-HIST-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-WEAR-001`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-007`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-005`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-WEAR-001`, `BRULE-WEAR-002`, `BRULE-WEAR-003`, `BRULE-WEAR-006`, `BRULE-PERS-002`, `BRULE-PERS-003`, `BRULE-PERS-005`, `BRULE-HIST-001`, `BRULE-OUT-002` |
| [Business Requirements](../../01-business/BRD.md) | `BR-006`, `BR-011`, `BR-021`, `BR-022`, `BR-009`, `BR-019` |
| [Capabilities](../../01-business/BRD.md) | `CAP-06`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- Logical-action retry protection must not deduplicate all User/outfit/date reports. History identity and daily evidence normalization have different purposes.
- Time-decay/recency use the resolved absolute elapsed-time policy; original event-local date determines daily grouping. No future scheduling or backdated creation policy is added.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Choose a current valid owned outfit.
- [UC-013 — Provide Outfit Feedback](UC-013-provide-outfit-feedback.md): Give preference feedback separately.
- [UC-015 — View Wear History](UC-015-view-wear-history.md): Inspect accepted reports and resolve uncertain outcomes.
- [UC-016 — Correct Wear Event](UC-016-correct-wear-event.md): Correct a specific accepted event.
- [UC-017 — Remove Wear Event](UC-017-remove-wear-event.md): Remove a specific accepted event.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-011` | Final event privacy/retention/deletion policy remains open; ranking influence expiry does not settle retention. |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
