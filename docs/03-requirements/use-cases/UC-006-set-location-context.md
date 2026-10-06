# UC-006 — Set Location Context

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-006 |
| Use Case Name | Set Location Context |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | Device Location Service; Weather Information Provider |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Select or update the location/environmental context used by CapsuleAI through optional consented device location or manual city/location, with explicit limited-context recovery.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Obtain relevant context while controlling location permission. |

## 4. Preconditions

- The User has authenticated access authorized for personal context settings.

## 5. Trigger

The User chooses location setup/change or retries unavailable environmental context.

## 6. Main Success Scenario

1. The User requests location/context setup or revision.
2. CapsuleAI explains device-location purpose and offers device location, manual city/location or skipping optional setup.
3. The User chooses device location and grants the needed permission.
4. Device Location Service supplies available consented location information; CapsuleAI identifies the selected location.
5. Weather Information Provider supplies available information for the selected location; CapsuleAI presents the actual context/timestamp used, treating weather as current only at age ≤30 minutes and bounding any external-request assessment delay to 2 seconds.
6. The User reviews the selected context and continues.
7. CapsuleAI uses the accepted location/environmental context in relevant advice and reevaluates affected assessments or marks them outdated after a relevant change.

## 7. Alternative Flows

### A1 — Manual city/location

At Main Step 3:

1. The User chooses a supported city/location manually, independently of device permission.
2. CapsuleAI identifies that selected location; resume at Main Step 5 when environmental information can be retrieved.

### A2 — Decline permission

At Main Step 3:

1. The User declines device-location access.
2. CapsuleAI offers manual location or skip; continue through the manual alternative or the skip alternative without denying core authenticated entry.

### A3 — Skip optional location

At Main Step 3:

1. The User skips location setup.
2. CapsuleAI identifies absent/limited environmental context and allows other core journeys where their remaining information supports validity; no location or forecast is invented.

### A4 — Change or retry existing context

At Main Step 1:

1. The User revisits an existing selection to change location or retry unavailable context retrieval.
2. Resume at Main Step 2; changed relevant context cannot leave affected old advice labeled current.

## 8. Exception / Failure Flows

### E1 — Device location unavailable

At Main Step 4:

1. CapsuleAI explains that device location could not be obtained and offers manual city/location or skip.
2. The User takes an available alternative without a mandatory permission or device-service-success gate.

### E2 — Weather unavailable or stale

At Main Step 5:

1. CapsuleAI attempts refresh of weather older than 30 minutes; without usable current information within the 2-second boundary, it identifies unavailable environment and offers context review/retry.
2. The selected location is not confused with a known forecast; other evaluations may use disclosed reduced context while retaining remaining validity rules.

### E3 — Location update or communication outcome unconfirmed

At Main Step 7:

1. CapsuleAI distinguishes a failed/unconfirmed setting change from the last accepted selection.
2. The User reviews actual context or retries; an older assessment is not presented as refreshed for an unestablished new context.

### E4 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The available selected location and environmental context, or the explicit skipped/limited-context state, are understandable to the User.
- Relevant advice uses the identified context; affected prior assessments are refreshed or marked outdated.
- Device location is used only with permission; manual city/location remains available independently.

## 10. Minimal / Failure Postconditions

- Absent permission, missing weather or provider failure creates no fabricated location/environmental result.
- Canceled/rejected/known failed changes do not overwrite last accepted context; uncertain changes require outcome review.
- Other authorized core paths remain available subject to network/access and sufficient valid information.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-PROF-003` | Manual/device/absent context remains explicit and relevant changes invalidate affected advice. |
| `BRULE-OUT-006` | Missing weather does not automatically reject every outfit; remaining validity still applies. |
| `BRULE-COV-006` | Relevant environmental/context changes refresh or invalidate Coverage. |
| `BRULE-MULT-008` | Relevant context changes invalidate prior candidate utility. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-01`, `JRN-04` |
| [Product Features](../../02-product/PRD.md) | `FEAT-PROF-002` |
| [Software Requirements — Functional](../SRS.md) | `FR-WEATHER-001`, `FR-WEATHER-002`, `FR-WEATHER-003`, `FR-WEATHER-004`, `FR-WEATHER-005`, `FR-AUTH-008` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-WEATHER-001`, `ERR-DEP-001`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `SI-002`, `HW-003`, `COM-001`, `COM-002`, `COM-003` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PRIV-001`, `NFR-AVL-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-PROF-003`, `BRULE-OUT-006`, `BRULE-COV-006`, `BRULE-MULT-008`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-007`, `BR-013`, `BR-014`, `BR-022`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-01`, `CAP-05`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- Consent is specific to the optional device-location route; selecting a manual city does not require device access.
- Weather is current at age ≤30 minutes from its applicable retrieval/observation timestamp. Older weather is refreshed before current use; an external request may delay the assessment at most 2 seconds. Without usable current information by that boundary, disclose unavailable environment and use valid reduced context. No provider or background-tracking schedule is prescribed.

- Weather Information Provider receives only the selected location necessary for its request, not unrelated wardrobe/profile/history. Device-location permission remains optional and purpose-specific.
- The 30-minute freshness boundary is inclusive; unusable weather at the 2-second boundary affects environmental evidence, not authenticated entry or all outfits.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-005 — Update Personalization Profile](UC-005-update-personalization-profile.md): Manage preferences/common needs separately.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Use available context for current outfit advice.
- [UC-019 — View Wardrobe Coverage](UC-019-view-wardrobe-coverage.md): Use relevant context for Coverage.
- [UC-022 — Evaluate Candidate Garment](UC-022-evaluate-candidate-garment.md): Use one consistent context for current/candidate comparison.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-006-set-location-context.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.

## 17. Activity Diagram

[Activity Diagram](../activity-diagrams/AD-006-set-location-context.puml)

This diagram visualizes the established main, alternative, and failure flows of this Use Case; the textual specification remains the normative behavioral source.
