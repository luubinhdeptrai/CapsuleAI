# UC-013 — Provide Outfit Feedback

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-013 |
| Use Case Name | Provide Outfit Feedback |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Express, revise or clear Like/Dislike for an identified recommendation/exact outfit combination, with accepted feedback reflected in future soft personalization and no inference of physical wear.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Control the effective preference state and see whether a change was accepted. |

## 4. Preconditions

- The User has authenticated access authorized for the identified recommendation/exact outfit feedback target.

## 5. Trigger

The User chooses Like, Dislike, a revision or clearing of feedback on the identified target.

## 6. Main Success Scenario

1. The User identifies the outfit/recommendation and chooses Like.
2. CapsuleAI shows the targeted combination and the current effective feedback state.
3. The User submits the selected preference action.
4. CapsuleAI accepts the change and shows Like as the effective state for that target, superseding any contradictory prior state.
5. The User may continue viewing recommendations; applicable future ranking uses the accepted evidence under the Business Rules.

## 7. Alternative Flows

### A1 — Dislike

At Main Step 1:

1. The User chooses Dislike for the exact target.
2. Continue at Main Step 2 with Dislike; an accepted Dislike is negative soft evidence for that target, not a permanent ban on its garments/categories.

### A2 — Revise an existing state

At Main Step 3:

1. The User replaces Like with Dislike or Dislike with Like.
2. CapsuleAI accepts and shows only the revised effective state; continue at Main Step 5.

### A3 — Clear feedback

At Main Step 3:

1. The User explicitly clears the existing feedback.
2. CapsuleAI shows no effective Like/Dislike for the target after acceptance and updates applicable evidence without removing Wear history.

### A4 — No immediate different recommendation

At Main Step 5:

1. The available valid choices may remain unchanged despite accepted feedback.
2. CapsuleAI does not bypass hard validity or promise a different result when no useful valid alternative exists.

## 8. Exception / Failure Flows

### E1 — Target is unavailable or cannot be identified reliably

At Main Step 2:

1. CapsuleAI explains that feedback cannot be safely applied to an unidentified/inaccessible target.
2. The User returns to an identifiable recommendation; no unrelated target receives the feedback.

### E2 — Rejected or uncertain feedback update

At Main Step 4:

1. CapsuleAI distinguishes known rejection/failure from an unconfirmed response.
2. Known failure retains the prior effective state. An uncertain outcome is reviewed/retried safely without claiming that the requested preference was accepted or applying simultaneously contradictory states.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The accepted Like, Dislike or cleared state is visible for the correct exact target, with contradictory states not simultaneously effective.
- Applicable future evidence reflects accepted revision/clearing; no Wear Event, garment ban or ownership change is created.

## 10. Minimal / Failure Postconditions

- A known failed/rejected feedback change does not replace the prior effective state; an uncertain outcome is not falsely reported as accepted.
- The target and accepted wardrobe/history remain intact; preference-only interactions do not create wear.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-PERS-001` | Like +1 / Dislike −2 apply to the exact target with one effective state and no permanent garment ban. |
| `BRULE-PERS-003` | Behavioral contribution ages by completed elapsed periods; feedback state is not automatically cleared at 90 days. |
| `BRULE-PERS-006` | Accepted revision/clearing changes effective evidence. |
| `BRULE-OUT-008` | Feedback influences soft ranking only after validity. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-04`, `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-PERS-001`, `FEAT-PERS-003`, `FEAT-MET-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-PERS-001`, `FR-PERS-002`, `FR-PERS-003`, `FR-PERS-004`, `FR-PERS-005`, `FR-PERS-007`, `FR-PERS-011`, `FR-PERS-013`, `FR-MET-001`, `FR-MET-007`, `FR-AUTH-008`, `FR-MET-006` |
| [Software Requirements — Data](../SRS.md) | `DATA-OUT-001`, `DATA-WEAR-001`, `DATA-RET-003` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-001`, `UI-003`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PRIV-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-PERS-001`, `BRULE-PERS-003`, `BRULE-PERS-006`, `BRULE-OUT-008`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-011`, `BR-021`, `BR-022`, `BR-009`, `BR-019` |
| [Capabilities](../../01-business/BRD.md) | `CAP-06` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- Feedback state may persist after its ranking influence expires; aging is not deletion or clearing.
- Feedback measurement distinguishes accepted changes from mere views or failed actions; measurement failure does not reverse accepted feedback.

- Feedback supports private personalization without MVP personal-data model training. User-linked engagement measurement is deleted or aggregated/de-identified within 90 days; its expiry differs from effective feedback-state/ranking semantics.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): View the recommendation that can receive feedback.
- [UC-014 — Record Wear Event](UC-014-record-wear-event.md): Report Wear separately; liking is not wearing.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-013-provide-outfit-feedback.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
