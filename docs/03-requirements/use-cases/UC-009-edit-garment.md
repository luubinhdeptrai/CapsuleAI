# UC-009 — Edit Garment

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-009 |
| Use Case Name | Edit Garment |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Correct or enrich supported information on a current owned garment, making explicitly accepted User changes authoritative and keeping dependent advice aligned with the new state.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Maintain trustworthy clothing information without losing accepted values on cancel/failure. |

## 4. Preconditions

- The User has authenticated access authorized for an existing current owned garment.

## 5. Trigger

The User chooses to edit that garment.

## 6. Main Success Scenario

1. The User requests editing for the selected garment.
2. CapsuleAI presents its accepted profile and supported editable information, with relevant unknown/uncertain states.
3. The User revises the intended values using the supported vocabularies.
4. CapsuleAI presents the proposed revision and identifies missing required or incompatible values and applicable readiness implications.
5. The User explicitly confirms the intended supported changes.
6. CapsuleAI acknowledges the accepted revision and shows the new authoritative profile/readiness.
7. CapsuleAI refreshes affected advice/assessments or identifies them as requiring reevaluation rather than presenting prior results as current.

## 7. Alternative Flows

### A1 — Leave optional descriptors unknown

At Main Step 3:

1. The User changes only intended fields and leaves unknown optional material/shape/color/context detail unknown.
2. Resume at Main Step 4; completing every rich descriptor is not required for a supported edit.

### A2 — Enrich readiness information

At Main Step 3:

1. The User supplies missing applicable pattern/climate/layering/bulk information.
2. CapsuleAI explains the updated rule-specific readiness without claiming that readiness alone guarantees a valid outfit; resume at Main Step 4.

### A3 — Cancel before acceptance

At Main Step 5:

1. The User cancels the proposed edit.
2. CapsuleAI retains the previously accepted profile and dependent state.

## 8. Exception / Failure Flows

### E1 — Invalid or unconfirmed required information

At Main Step 4:

1. CapsuleAI explains unsupported values, inconsistent descriptors or missing confirmed minimum information and does not accept the revision.
2. The User corrects the proposal and resumes at Main Step 3.

### E2 — Edit failed or acceptance uncertain

At Main Step 6:

1. CapsuleAI distinguishes known failure from unconfirmed acceptance, preserving useful entered work where practical.
2. The prior profile stays authoritative after known failure; the User reviews the actual outcome before retrying an uncertain update.

### E3 — Garment is no longer current/authorized

At Main Step 5:

1. CapsuleAI refuses an edit to information outside the User's current authorized ownership and explains the unavailable selection.
2. The User returns to current wardrobe inspection; no historical snapshot is silently restored as owned.

### E4 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Accepted revised values are canonical and cannot be silently replaced by later automated output.
- Current profile/readiness reflects the accepted revision; affected recommendations, Coverage and Multiplier refresh or clearly become outdated.

## 10. Minimal / Failure Postconditions

- Canceled/rejected/known failed revisions leave the accepted garment unchanged.
- An uncertain revision is not claimed saved or freshly evaluated until its actual outcome is established.
- Other garments and historical reports are not edited by this action.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-001` | User confirmation/correction establishes authoritative values. |
| `BRULE-GAR-002` | The confirmed Saveable minimum remains meaningful. |
| `BRULE-GAR-003` | Edits can change applicable readiness without universal descriptor requirements. |
| `BRULE-GAR-004` | Classification/context vocabulary is bounded. |
| `BRULE-GAR-005` | Editable descriptors retain approved values/scales. |
| `BRULE-GAR-006` | Consistency and unknown handling prevent invented compatibility. |
| `BRULE-GAR-008` | Material uncertainty/provenance is not verification. |
| `BRULE-GAR-009` | Only accepted edits change state and invalidate affected advice. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-03` |
| [Product Features](../../02-product/PRD.md) | `FEAT-WAR-002`, `FEAT-GAR-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WAR-003`, `FR-WAR-006`, `FR-GAR-002`, `FR-GAR-003`, `FR-GAR-004`, `FR-GAR-005`, `FR-GAR-006`, `FR-GAR-007`, `FR-GAR-008`, `FR-GAR-009`, `FR-GAR-010`, `FR-GAR-011`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-002`, `DATA-GAR-003`, `DATA-GAR-005`, `DATA-INT-001`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-GAR-001`, `ERR-GAR-002`, `ERR-ANL-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-004`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-001`, `BRULE-GAR-002`, `BRULE-GAR-003`, `BRULE-GAR-004`, `BRULE-GAR-005`, `BRULE-GAR-006`, `BRULE-GAR-008`, `BRULE-GAR-009`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-002`, `BR-003`, `BR-004`, `BR-005`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-03`, `CAP-04` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- The User can correct supported dimensions without re-running analysis. Automated proposals cannot silently overwrite confirmed edits.
- A failed assessment refresh is not presented as a current result for the revised profile.

- Confirmed corrections support accurate private functionality, without MVP personal-data AI training. Last accepted values remain authoritative after known failure.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-008 — View Wardrobe](UC-008-view-wardrobe.md): Inspect the current accepted profile.
- [UC-010 — Remove Garment](UC-010-remove-garment.md): Remove ownership instead of changing descriptors.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Refresh affected current advice.
- [UC-019 — View Wardrobe Coverage](UC-019-view-wardrobe-coverage.md): Refresh affected Coverage.
- [UC-022 — Evaluate Candidate Garment](UC-022-evaluate-candidate-garment.md): Reevaluate affected candidate utility.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-009-edit-garment.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
