# UC-011 — Get Outfit Recommendations

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-011 |
| Use Case Name | Get Outfit Recommendations |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Obtain understandable, context-appropriate outfit choices from the current confirmed owned wardrobe, with hard validity established before soft personalized ranking and honest disclosure when fewer choices or limited context are available.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Choose wearable owned combinations and understand reasons, limitations and currentness. |

## 4. Preconditions

- The User has authenticated access authorized for the wardrobe and personalization context. A useful wardrobe or complete context is not a precondition; limitations are handled below.

## 5. Trigger

The User requests outfit recommendations for the current occasion/context.

## 6. Main Success Scenario

1. The User requests recommendations and provides or accepts the current occasion/style context.
2. CapsuleAI identifies the available wardrobe, declared preferences, relevant personalization and environmental context used for the request, disclosing unavailable context.
3. CapsuleAI presents recommendations based on current confirmed, active, owned, applicable Recommendation-Ready garments, with hard-validity constraints satisfied before soft ranking.
4. CapsuleAI offers at least three distinct valid outfits when that many exist, identifies their garments and shows relevant compatibility/context reasons.
5. The User selects an outfit to inspect.
6. CapsuleAI shows its constituent garment information, explanation and current owned-outfit status, with separate actions for Shuffle, feedback or Wear This Today.
7. The User chooses an outfit or continues browsing; viewing alone records neither feedback nor wear.

## 7. Alternative Flows

### A1 — Only one or two valid choices

At Main Step 4:

1. CapsuleAI presents the actual one or two distinct valid outfits and explains the limited choice.
2. The User inspects one at Main Step 5 or revises wardrobe/context through a related use case; no duplicate or invalid option pads the result.

### A2 — Completed assessment finds no valid outfit

At Main Step 4:

1. CapsuleAI states that no valid combination exists under the evaluated context and explains useful readiness/context/wardrobe adjustments supported by the evidence.
2. The User may revise inputs and request again or return to the wardrobe; this completed zero-choice result does not manufacture a wardrobe gap.

### A3 — No prior behavioral history or omitted optional context

At Main Step 2:

1. CapsuleAI uses available declared style/current occasion and applicable owned-garment information.
2. Omitted body/gender or absent behavioral history does not block recommendations; continue at Main Step 3.

### A4 — User changes occasion or selects another supported action

At Main Step 6:

1. The User may request under a changed occasion/context or start UC-012, UC-013 or UC-014 for the identified outfit.
2. Changed-context recommendations are a new assessment; feedback and Wear require their own explicit actions.

## 8. Exception / Failure Flows

### E1 — Insufficient applicable garments or information

At Main Step 3:

1. CapsuleAI explains the missing applicable ready roles/attributes or assessment context rather than asserting a completed count.
2. The User can enrich confirmed garments, add items or adjust available context; accepted wardrobe information remains intact and no fixed onboarding quota is imposed.

### E2 — Weather unavailable or stale

At Main Step 2:

1. CapsuleAI consumes context under UC-006's acquisition boundary: weather older than 30 minutes needs refresh before current use; without usable current information within the 2-second external-request delay boundary, disclose unavailable environment and use remaining context where validity is defensible.
2. Resume at Main Step 3 only if remaining information supports validity; otherwise explain the specific limitation and allow manual context/retry through UC-006.

### E3 — Assessment inputs change or refresh fails

At Main Step 4:

1. CapsuleAI refreshes the advice or clearly marks the prior assessment outdated when wardrobe, context or relevant rules change.
2. A failed refresh is not current advice; the User may retry or return to available wardrobe/context.

### E4 — Recommendation retrieval fails

At Main Step 3:

1. CapsuleAI distinguishes unavailable recommendations from a completed zero-choice result and offers retry.
2. No invalid, duplicated or hypothetical candidate outfit is substituted as current owned advice.

### E5 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The displayed assessment identifies its available context and current owned garments, with valid distinct choices or an honest completed fewer/no-valid outcome.
- Soft preference/diversity/recency signals rank only valid combinations; no feedback, Wear Event or ownership change is created by requesting/viewing.

## 10. Minimal / Failure Postconditions

- Accepted garments, feedback and Wear history remain unchanged by a failed request.
- Missing information, unavailable results and outdated advice are not misrepresented as a current complete zero or as valid recommendations.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAR-003` | Only applicable ready confirmed inputs enter current recommendations. |
| `BRULE-OUT-001` | An outfit has exactly one TOP, BOTTOM and FOOTWEAR, with zero or one OUTERWEAR. |
| `BRULE-OUT-002` | Exclude removed, unowned and hypothetical items from daily advice. |
| `BRULE-OUT-003` | Apply compatible known layering roles when relevant. |
| `BRULE-OUT-004` | Enforce the applicable outer/inner bulk relationship. |
| `BRULE-OUT-005` | Use the established environmental suitability policy when context is available. |
| `BRULE-OUT-006` | Missing weather permits disclosed reduced-context advice where remaining validity is defensible. |
| `BRULE-OUT-007` | Apply the severe-pattern/visual-noise limit rather than a blanket patterned-item ban. |
| `BRULE-OUT-008` | Hard validity precedes all soft preferences. |
| `BRULE-OUT-009` | Garment-identity sets define distinct choices; show actual available counts. |
| `BRULE-PROF-001` | Optional body/gender influence is soft and does not prohibit categories. |
| `BRULE-PERS-003` | Use the elapsed-time aging policy without deleting historical state. |
| `BRULE-PERS-004` | Recency/diversity remain soft adjustments, not exclusion rules. |
| `BRULE-PERS-005` | Legitimate repeated reports do not multiply same-outfit/event-local-day preference increments. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-04` |
| [Product Features](../../02-product/PRD.md) | `FEAT-OUT-001`, `FEAT-OUT-002`, `FEAT-PERS-003` |
| [Software Requirements — Functional](../SRS.md) | `FR-OUT-001`, `FR-OUT-002`, `FR-OUT-003`, `FR-OUT-004`, `FR-OUT-005`, `FR-OUT-006`, `FR-OUT-007`, `FR-OUT-008`, `FR-OUT-009`, `FR-OUT-010`, `FR-OUT-011`, `FR-OUT-012`, `FR-OUT-013`, `FR-PERS-006`, `FR-PERS-007`, `FR-PERS-008`, `FR-PERS-009`, `FR-PERS-010`, `FR-PERS-011`, `FR-PERS-012`, `FR-AUTH-008`, `FR-WEATHER-003` |
| [Software Requirements — Data](../SRS.md) | `DATA-OUT-001`, `DATA-OUT-002`, `DATA-OUT-003`, `DATA-GAR-005` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-OUT-001`, `ERR-OUT-002`, `ERR-WEATHER-001`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-004`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PERF-002`, `NFR-SCA-001`, `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-TEST-003`, `NFR-SCA-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAR-003`, `BRULE-OUT-001`, `BRULE-OUT-002`, `BRULE-OUT-003`, `BRULE-OUT-004`, `BRULE-OUT-005`, `BRULE-OUT-006`, `BRULE-OUT-007`, `BRULE-OUT-008`, `BRULE-OUT-009`, `BRULE-PROF-001`, `BRULE-PERS-003`, `BRULE-PERS-004`, `BRULE-PERS-005`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-007`, `BR-008`, `BR-009`, `BR-010`, `BR-011`, `BR-012`, `BR-013`, `BR-022`, `BR-024`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-04`, `CAP-05`, `CAP-06` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- The first complete recommendation result targets p95 <3 seconds from accepted request with required context available and exactly 100 confirmed garments (NFR-PERF-002). SRS Sections 2.3/12.3 define reference devices/network, 5 warm-ups and ≥100 measured runs with legitimate slow runs retained. Eligible inputs cannot be omitted; 20-user functional integrity is separate from this p95 workload.
- An exact garment-identity set is one outfit regardless of ordering. Soft personalization need not visibly change the result when no useful valid alternative exists.
- Only UC-006 acquires external location/weather context. This use case consumes available context and has no separate supporting external actor.

- Available weather obeys the inclusive 30-minute freshness and 2-second acquisition boundary. Missing weather skips only unavailable environmental filtering, preserving remaining hard rules; no direct external actor is added here.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-005 — Update Personalization Profile](UC-005-update-personalization-profile.md): Maintain declared preferences and optional context.
- [UC-006 — Set Location Context](UC-006-set-location-context.md): Set or recover environmental context.
- [UC-007 — Add Garment](UC-007-add-garment.md): Add confirmed owned garments when useful.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Enrich applicable garment readiness.
- [UC-012 — Shuffle Garment Slot](UC-012-shuffle-garment-slot.md): Replace one selected slot under fixed context.
- [UC-013 — Provide Outfit Feedback](UC-013-provide-outfit-feedback.md): Explicitly provide/revise feedback.
- [UC-014 — Record Wear Event](UC-014-record-wear-event.md): Explicitly report wear of a valid owned outfit.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-011-get-outfit-recommendations.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
