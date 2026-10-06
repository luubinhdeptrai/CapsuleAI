# UC-022 — Evaluate Candidate Garment

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-022 |
| Use Case Name | Evaluate Candidate Garment |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Evaluate a hypothetical candidate's incremental unique valid outfit utility against the current owned wardrobe under a consistent context/rule basis, with honest exact, zero, incomplete, unavailable or outdated status.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Understand defensible newly enabled outfit utility without mistaking a candidate for ownership or an estimate for a completed count. |

## 4. Preconditions

- The User has authenticated access authorized for the wardrobe and a candidate identified through existing gap/advice context.
- The candidate remains hypothetical. Sufficient attributes/context and operating evaluation capability are checked during the interaction, not assumed as preconditions.

## 5. Trigger

The User requests evaluation or reevaluation of the identified candidate garment.

## 6. Main Success Scenario

1. The User selects the candidate from supported gap/shopping advice and requests evaluation.
2. CapsuleAI identifies the hypothetical candidate, applicable current confirmed active owned baseline and assessment context/rule basis used, exposing relevant limitations.
3. CapsuleAI shows the evaluation as pending until a result with established status is available.
4. CapsuleAI presents a completed Exact evaluation with the incremental +N New Outfits count, current baseline/context explanation and supported utility reasons.
5. The User requests newly enabled outfit previews.
6. CapsuleAI shows candidate-bearing hypothetical outfits from that same completed assessment, clearly distinguishing the unowned candidate from owned garments.
7. The User reviews the utility and may continue candidate advice; evaluation/preview viewing creates no ownership or Wear Event.

## 7. Alternative Flows

### A1 — Completed Evaluated Zero

At Main Step 4:

1. Both outfit-set assessments are complete under the same valid basis and their newly enabled difference is empty.
2. CapsuleAI shows Evaluated Zero and +0 New Outfits with reasons. It does not invent newly enabled previews; the User may continue at Main Step 7.

### A2 — Reevaluate after relevant changes

At Main Step 1:

1. The User requests a fresh assessment after a changed wardrobe, candidate attribute, context or applicable rule basis.
2. CapsuleAI uses the actual current basis; resume at Main Step 2. Exact status is reestablished only if current completeness conditions hold.

### A3 — Optional commercial fields absent

At Main Step 2:

1. The candidate has sufficient evaluative descriptors but lacks credible optional price/retailer/destination information.
2. Utility evaluation continues at Main Step 3 without invented commercial values; absent optional fields are not a zero-count or evaluation-failure condition.

### A4 — User declines preview or further shopping

At Main Step 5:

1. The User stops after reviewing the established evaluation state/count.
2. The User can return to core wardrobe/gap/advice; no purchase or navigation is required.

## 8. Exception / Failure Flows

### E1 — Incomplete inputs or unestablished completion

At Main Step 4:

1. CapsuleAI shows Incomplete when required inputs are insufficient or complete evaluation cannot be established, including partial/timed-out work.
2. It explains the relevant limitation and withholds current exact +N and unsupported current newly enabled previews; Incomplete is not +0.
3. The User may correct relevant owned/context inputs through existing use cases or retry when useful; no new candidate-upload/edit workflow is invented.

### E2 — Evaluation capability unavailable

At Main Step 3:

1. CapsuleAI shows Unavailable when the evaluation capability cannot operate and explains recovery/retry where appropriate.
2. Unavailable is not a completed zero. Available candidate/gap information remains accessible where possible.

### E3 — Prior result is outdated

At Main Step 4:

1. CapsuleAI labels the prior count/previews Outdated after a relevant wardrobe, confirmed/candidate descriptor, context or validity/business-rule change.
2. The old count cannot claim current exact status. Reevaluation may itself remain Incomplete or Unavailable; those outcomes do not make the old result current.

### E4 — Result or preview retrieval fails

At Main Step 6:

1. CapsuleAI explains the unavailable information and retains only information whose established status/basis is clear.
2. The User may retry. Missing previews do not invent newly enabled outfits or turn an unestablished count into +0; no ownership/wear change occurs.

### E5 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- A completed Exact or Evaluated Zero result uses current complete baseline/expanded outfit sets under the same context/rules and garment-identity uniqueness basis.
- Newly enabled previews, when present, contain the hypothetical candidate and share the evaluated basis. Candidate ownership and Wear history remain unchanged.

## 10. Minimal / Failure Postconditions

- Incomplete, Unavailable and Outdated are explicitly distinguished and never represented as current exact +N/+0.
- Known unavailable/partial results do not fabricate current previews. Evaluation does not add a garment, create wear or establish purchase.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-MULT-001` | Use a current owned ready baseline and a separately identified hypothetical candidate. |
| `BRULE-MULT-002` | Compare identity-based sets under the same context/rule basis; permutations are not new outfits. |
| `BRULE-MULT-003` | Wardrobe Multiplier is the count of O(W ∪ {candidate}) − O(W), the unique valid outfits enabled by the addition. |
| `BRULE-MULT-004` | Exact status requires sufficient inputs and complete current set evaluations, not only displayed daily recommendations. |
| `BRULE-MULT-005` | A completed empty difference alone establishes Evaluated Zero / +0. |
| `BRULE-MULT-006` | Insufficient/partial/unestablished completion is Incomplete. |
| `BRULE-MULT-007` | A capability that cannot operate is Unavailable. |
| `BRULE-MULT-008` | Freshness changes with relevant inputs/rules, not an invented time-based TTL. |
| `BRULE-MULT-009` | Reevaluation must reestablish the actual current basis; failure leaves old advice outdated. |
| `BRULE-GAR-003` | Applicable readiness governs owned inputs and required candidate evaluation descriptors. |
| `BRULE-OUT-008` | Hard validity precedes preferences in both assessed sets. |
| `BRULE-SHOP-001` | Optional commercial information is not an evaluative descriptor/completeness gate. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-07` |
| [Product Features](../../02-product/PRD.md) | `FEAT-MULT-001`, `FEAT-SHOP-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-MULT-001`, `FR-MULT-002`, `FR-MULT-003`, `FR-MULT-004`, `FR-MULT-005`, `FR-MULT-006`, `FR-MULT-007`, `FR-MULT-008`, `FR-SHOP-002`, `FR-SHOP-004`, `FR-AUTH-008`, `FR-SHOP-001`, `FR-SHOP-003`, `FR-WEATHER-003` |
| [Software Requirements — Data](../SRS.md) | `DATA-OUT-001`, `DATA-OUT-002`, `DATA-OUT-003`, `DATA-ANL-003`, `DATA-ANL-004`, `DATA-INT-003` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-SHOP-001`, `ERR-SHOP-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001`, `ERR-ANL-001`, `ERR-ANL-002`, `ERR-WEATHER-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-008`, `UI-009`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-REL-002`, `NFR-TEST-001`, `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-MULT-001`, `BRULE-MULT-002`, `BRULE-MULT-003`, `BRULE-MULT-004`, `BRULE-MULT-005`, `BRULE-MULT-006`, `BRULE-MULT-007`, `BRULE-MULT-008`, `BRULE-MULT-009`, `BRULE-GAR-003`, `BRULE-OUT-008`, `BRULE-SHOP-001`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-009`, `BR-015`, `BR-016`, `BR-017`, `BR-022`, `BR-024`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-09`, `CAP-08`, `CAP-10` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- The multiplier is incremental outfit utility, not a weighted score, forecast, purchase guarantee or number of permutations. No internal enumeration/ranking design is specified.
- Baseline and expanded assessments must share context/rules; candidate evaluation is the documented hypothetical exception to daily owned-garment eligibility.
- No separate multiplier latency or mandatory larger-wardrobe quantitative threshold is specified. SRS Section 12.3 defines supported validation conditions; exactness requires complete evidence rather than an arbitrary truncated subset.

- Candidate descriptors require explicit user-provided evidence or an identifiable configured credible source. No candidate import/capture interaction is added. Weather context follows the 30-minute/2-second acquisition boundary; optional external price/availability follows source/time and 24-hour currentness. Missing/stale commercial values or destination failure do not turn valid utility into +0.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-020 — View Wardrobe Gaps](UC-020-view-wardrobe-gaps.md): Understand the need/capability addressed by the candidate.
- [UC-021 — View Shopping Recommendations](UC-021-view-shopping-recommendations.md): Review the source candidate advice and return to browsing.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Correct applicable owned-garment inputs.
- [UC-005 — Update Personalization Profile](UC-005-update-personalization-profile.md): Revise relevant preference/need inputs.
- [UC-006 — Set Location Context](UC-006-set-location-context.md): Revise relevant environmental context.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-022-evaluate-candidate-garment.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.

## 17. Activity Diagram

[Activity Diagram](../activity-diagrams/AD-022-evaluate-candidate-garment.puml)

This diagram visualizes the established main, alternative, and failure flows of this Use Case; the textual specification remains the normative behavioral source.
