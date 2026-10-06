# UC-012 — Shuffle Garment Slot

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-012 |
| Use Case Name | Shuffle Garment Slot |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Replace one selected garment slot in a current owned outfit while retaining every other constituent and the assessment context, or retain the original selection with an explanation when no compatible alternative exists.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Explore a compatible alternative without losing the other chosen garments or implicitly expressing feedback/wear. |

## 4. Preconditions

- The User has authenticated access to an identified current owned outfit and its assessment context.
- The selected slot is one of that outfit's supported garment roles; an available replacement is not a precondition.

## 5. Trigger

The User requests Shuffle for one selected garment slot.

## 6. Main Success Scenario

1. The User selects a slot and requests Shuffle.
2. CapsuleAI identifies the current selected outfit and fixed assessment context for the request.
3. CapsuleAI presents a different eligible owned garment for only that slot, with the resulting full outfit satisfying all applicable validity rules.
4. The User reviews the changed garment and resulting outfit.
5. CapsuleAI retains every other constituent and the context and provides applicable garment/reason information.

## 7. Alternative Flows

### A1 — No compatible replacement

At Main Step 3:

1. CapsuleAI retains the current selection and explains that no compatible owned alternative exists under the fixed constituents/context.
2. The User may keep it or separately change wardrobe/context and obtain a new assessment; the limitation creates no Dislike or Wear Event.

### A2 — Another explicit slot exploration

At Main Step 4:

1. The User initiates a separate Shuffle for another supported slot.
2. Apply this use case again using that action's selected outfit, unchanged other constituents and fixed context; no multi-slot replacement is implied.

## 8. Exception / Failure Flows

### E1 — Selected outfit/context is no longer current

At Main Step 2:

1. CapsuleAI identifies the outdated/changed advice rather than presenting a replacement as valid for a stale basis.
2. The User can refresh through UC-011 and then request a new Shuffle.

### E2 — Replacement request fails

At Main Step 3:

1. CapsuleAI explains the unavailable result and retains the prior selection where available.
2. The User may retry; no replacement, feedback or wear is represented as accepted solely from the failed request.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- A successful replacement changes only the selected slot and remains fully valid under the same context; a completed no-alternative response retains the selection.
- Shuffle alone creates neither Like, Dislike nor a Wear Event.

## 10. Minimal / Failure Postconditions

- Failed Shuffle does not alter accepted garment profiles or history and does not infer feedback.
- The prior selection is not represented as a successful replacement; stale advice is distinguishable.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-OUT-010` | One selected slot may change; all other garment identities and context remain fixed. |
| `BRULE-OUT-002` | Only eligible current owned replacement garments are considered. |
| `BRULE-GAR-003` | Replacement inputs must be ready for the applicable evaluation. |
| `BRULE-OUT-001` | The full resulting outfit retains supported composition. |
| `BRULE-OUT-003` | The replacement must preserve valid layering. |
| `BRULE-OUT-004` | The full outfit must preserve applicable bulk validity. |
| `BRULE-OUT-005` | Keep applicable environmental validity under the same context. |
| `BRULE-OUT-006` | An existing disclosed reduced-context basis remains fixed. |
| `BRULE-OUT-007` | The resulting outfit must satisfy the pattern/noise limit. |
| `BRULE-OUT-008` | Soft preference cannot rescue an invalid replacement. |
| `BRULE-OUT-009` | A permutation of the same garment set is not a distinct alternative. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-04` |
| [Product Features](../../02-product/PRD.md) | `FEAT-OUT-003`, `FEAT-OUT-002` |
| [Software Requirements — Functional](../SRS.md) | `FR-OUT-009`, `FR-OUT-010`, `FR-OUT-011`, `FR-OUT-014`, `FR-OUT-015`, `FR-OUT-016`, `FR-OUT-017`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-OUT-001`, `DATA-OUT-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-OUT-003`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-OUT-010`, `BRULE-OUT-002`, `BRULE-GAR-003`, `BRULE-OUT-001`, `BRULE-OUT-003`, `BRULE-OUT-004`, `BRULE-OUT-005`, `BRULE-OUT-006`, `BRULE-OUT-007`, `BRULE-OUT-008`, `BRULE-OUT-009`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-007`, `BR-008`, `BR-009`, `BR-022`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-04`, `CAP-05` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- No compatible replacement is a meaningful completed alternative, not a reason to change fixed slots, relax hard rules or invent a replacement.
- A new context request belongs to UC-011; this use case does not acquire weather directly.

- Fixed assessment context preserves the established disclosed environmental basis; a relevant context change requires new current recommendations rather than quietly changing Shuffle's basis.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Obtain/refresh the source owned-outfit assessment.
- [UC-013 — Provide Outfit Feedback](UC-013-provide-outfit-feedback.md): Record feedback through a separate explicit action.
- [UC-014 — Record Wear Event](UC-014-record-wear-event.md): Record wear through a separate explicit action.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-012-shuffle-garment-slot.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
