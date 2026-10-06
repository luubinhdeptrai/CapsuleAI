# UC-008 — View Wardrobe

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-008 |
| Use Case Name | View Wardrobe |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Browse and find current owned garments, inspect their confirmed details/readiness and understand available reported-use information without changing wardrobe ownership.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Find accurate possessions, including imageless/incomplete entries. |

## 4. Preconditions

- The User has authenticated access authorized for the wardrobe; an empty wardrobe is permitted.

## 5. Trigger

The User opens the wardrobe or requests garment search/detail.

## 6. Main Success Scenario

1. The User requests the current wardrobe.
2. CapsuleAI distinguishes loading from loaded data and presents identifiable current confirmed garments, including entries without images.
3. The User browses or enters descriptor search and category/color filter conditions.
4. CapsuleAI shows active conditions and the matching current garments.
5. The User selects a garment to inspect.
6. CapsuleAI presents its confirmed profile, available image/provenance/uncertainty, applicable readiness guidance and supported recorded-use information.
7. The User returns to browsing or chooses a separate supported maintenance/insight action.

## 7. Alternative Flows

### A1 — Unfiltered browsing/reset

At Main Step 3:

1. The User supplies no conditions or clears existing search/filter conditions.
2. CapsuleAI restores the unfiltered current wardrobe; continue at Main Step 5 if the User selects an item.

### A2 — No matching results

At Main Step 4:

1. CapsuleAI explains that the current conditions match no garments, without calling the wardrobe empty.
2. The User adjusts or resets conditions and resumes at Main Step 3.

### A3 — Genuinely empty wardrobe

At Main Step 2:

1. CapsuleAI explains the confirmed empty state and offers Add Garment.
2. The User may start UC-007 or leave the wardrobe without a fixed garment-quota requirement.

### A4 — Imageless, unready or no-recorded-use entry

At Main Step 6:

1. CapsuleAI keeps the entry identifiable without an image, explains missing applicable readiness fields and labels missing reports as no recorded use.
2. The User can inspect/enrich the garment without an inference that it has never been physically worn.

## 8. Exception / Failure Flows

### E1 — Wardrobe/search/detail retrieval fails

At Main Step 2:

1. CapsuleAI identifies unavailable data and offers retry or return to usable prior context.
2. It does not display an empty wardrobe/no matches as a substitute for a failed retrieval; accepted inventory is unchanged.

### E2 — Selected garment is no longer current

At Main Step 6:

1. CapsuleAI explains the changed/removed item and refreshes the current wardrobe where possible.
2. The User returns to current browsing; historical snapshots remain separate and do not restore ownership.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Displayed inventory/details match the available current confirmed wardrobe and visible search/filter conditions.
- Readiness and recorded-use limitations are understandable, and no item is added/edited/removed by viewing.

## 10. Minimal / Failure Postconditions

- Failed browsing/search/detail does not delete or change accepted garments.
- Unavailable access/data is distinct from a true empty or no-match result; removed items are not restored as current.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-001` | Confirmed garment values govern displayed current information. |
| `BRULE-GAR-003` | Show applicable readiness without hiding Saveable but unready entries. |
| `BRULE-HIST-001` | Usage indicators are reported evidence, not verified physical behavior. |
| `BRULE-HIST-002` | Historical removed snapshots do not enter current inventory. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-03` |
| [Product Features](../../02-product/PRD.md) | `FEAT-WAR-001`, `FEAT-WAR-003`, `FEAT-GAR-001`, `FEAT-ANL-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-WAR-001`, `FR-WAR-002`, `FR-WAR-007`, `FR-WAR-008`, `FR-GAR-004`, `FR-ANL-004`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-GAR-001`, `DATA-GAR-004`, `DATA-GAR-005`, `DATA-HIST-002`, `DATA-HIST-001`, `DATA-RET-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-001`, `UI-003`, `UI-004`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-001`, `BRULE-GAR-003`, `BRULE-HIST-001`, `BRULE-HIST-002`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-003`, `BR-005`, `BR-006`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-03`, `CAP-04`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- Search/filtering is embedded in View Wardrobe rather than a new use case. Supported descriptor search and category/color filters have visible conditions and reset.
- No image, no recorded use, missing readiness, loading and unavailable data remain distinct meanings.

- Viewing is confined to the current authorized wardrobe. Historical references retain only necessary meaning and do not expose indefinitely retained original removed-garment images.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-007 — Add Garment](UC-007-add-garment.md): Add an owned garment.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Confirm edits to a selected current garment.
- [UC-010 — Remove Garment](UC-010-remove-garment.md): Deliberately remove a current garment.
- [UC-018 — View Wardrobe Utilization](UC-018-view-wardrobe-utilization.md): Inspect reported utilization in more detail.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-008-view-wardrobe.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
