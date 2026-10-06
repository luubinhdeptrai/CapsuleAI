# UC-021 — View Shopping Recommendations

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-021 |
| Use Case Name | View Shopping Recommendations |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Review gap-related hypothetical garment recommendations and their supported incremental outfit utility, interpreting optional commercial information cautiously and deciding whether to evaluate or navigate externally.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Understand utility and limitations before deciding whether a candidate is useful, independent of purchase. |

## 4. Preconditions

- The User has authenticated access authorized for wardrobe/gap/candidate advice. Available useful candidates and completed exact utility are not prerequisites for requesting advice.

## 5. Trigger

The User requests shopping recommendations from a supported gap/advice context.

## 6. Main Success Scenario

1. The User requests candidate recommendations for the supported wardrobe gap/advice context.
2. CapsuleAI presents available usable recommendations with candidate visual/identity, category/subtype, gap and relevant attributes supported by explicit user-provided information or an identifiable configured credible source.
3. The User selects a candidate to inspect.
4. CapsuleAI shows supported utility reasons, current evaluation state and exact incremental count/newly enabled hypothetical previews only when justified by the completed assessment.
5. CapsuleAI presents credible optional commercial information with identifiable external source/retrieval or last-checked time. Price/availability up to 24 hours old is current informational guidance; older values are refreshed, clearly stale/last checked, or omitted. Estimates are qualified and candidates/previews remain unowned.
6. The User evaluates the advice and may continue browsing, request UC-022 evaluation or explicitly choose an offered external shopping link.

## 7. Alternative Flows

### A1 — No useful candidate

At Main Step 2:

1. CapsuleAI explains that no useful/evaluable candidate recommendation is available for the supported context.
2. The User can continue gap/wardrobe/context work; no product or purchase requirement is fabricated.

### A2 — Optional commercial information absent

At Main Step 5:

1. CapsuleAI omits unavailable commercial fields and retains supported candidate utility/reasons.
2. A missing price, retailer or link does not turn a completed utility evaluation into +0 or prevent core advice.

### A3 — User dismisses/continues browsing

At Main Step 6:

1. The User declines further candidate exploration or leaves shopping advice.
2. Owned wardrobe, recommendations and insight use remain available without purchase or affiliate participation.

### A4 — Optional external-link action

At Main Step 6:

1. If an available external shopping link is offered and the User explicitly chooses it, UC-023 extends this use case at this point.
2. The User may instead stay with candidate information; viewing the recommendation alone causes no external navigation.

## 8. Exception / Failure Flows

### E1 — Candidate information/evaluation is insufficient or unavailable

At Main Step 4:

1. CapsuleAI identifies Incomplete or Unavailable evaluation as applicable and explains the limitation without unsupported attributes, exact counts or current new-outfit previews.
2. Available gap/wardrobe/context information remains usable; the User can request reevaluation when applicable.

### E2 — Assessment is outdated or refresh fails

At Main Step 4:

1. CapsuleAI refreshes the assessment or labels the prior basis Outdated when relevant inputs/rules change.
2. A failed refresh does not restore current exact status. The User can continue advice with clear limitations or request UC-022 again.

### E3 — Optional commercial claims cannot be verified

At Main Step 5:

1. CapsuleAI omits unsupported claims; price/availability older than 24 hours is refreshed, clearly stale/last checked, or omitted. Credible estimates remain qualified.
2. Supported utility remains available; merchant/stock/durability assertions are not invented.

### E4 — Advice retrieval fails

At Main Step 2:

1. CapsuleAI explains unavailable recommendations rather than claiming no supported gap or no useful candidate based solely on retrieval failure.
2. The User can retry or return to available gap/wardrobe context.

### E5 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The User can understand available candidate identity, supported gap utility and evaluation limitations; optional commercial information is credible/qualified when shown.
- Viewing/dismissing does not create ownership, Wear history, purchase or verified sales.

## 10. Minimal / Failure Postconditions

- Unavailable/incomplete/outdated utility is not shown as current exact +N or +0; missing commercial fields do not fabricate utility.
- Accepted wardrobe/history remains intact, and candidate information is not silently ingested as an owned garment.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-SHOP-001` | Separate required candidate utility information from credible optional commercial guidance. |
| `BRULE-SHOP-002` | External navigation is explicit, optional and not purchase proof. |
| `BRULE-SHOP-003` | Core utility remains independent of commerce/affiliate participation. |
| `BRULE-GAP-001` | The recommendation addresses an underserved capability, not a compulsory product. |
| `BRULE-GAP-002` | Do not manufacture demand when gap/candidate evidence is lacking. |
| `BRULE-MULT-003` | Utility is incremental unique valid outfits, not a weighted value or sales promise. |
| `BRULE-MULT-004` | Exact counts/previews need a current complete defensible assessment. |
| `BRULE-MULT-005` | Only a completed empty difference establishes +0. |
| `BRULE-MULT-006` | Incomplete is not exact or evaluated zero. |
| `BRULE-MULT-007` | Unavailable capability is not evaluated zero. |
| `BRULE-MULT-008` | Changed input/rule basis makes prior exact advice outdated. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-07` |
| [Product Features](../../02-product/PRD.md) | `FEAT-SHOP-001`, `FEAT-MULT-001`, `FEAT-SHOP-002` |
| [Software Requirements — Functional](../SRS.md) | `FR-SHOP-001`, `FR-SHOP-002`, `FR-SHOP-003`, `FR-SHOP-004`, `FR-SHOP-005`, `FR-SHOP-007`, `FR-SHOP-008`, `FR-MULT-004`, `FR-MULT-005`, `FR-MULT-006`, `FR-MULT-007`, `FR-MULT-008`, `FR-AUTH-008`, `FR-SHOP-006` |
| [Software Requirements — Data](../SRS.md) | `DATA-ANL-002`, `DATA-ANL-003`, `DATA-ANL-004`, `DATA-INT-003` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-SHOP-001`, `ERR-SHOP-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001`, `ERR-SHOP-003` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-008`, `UI-009`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-ACC-001`, `NFR-AVL-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-SHOP-001`, `BRULE-SHOP-002`, `BRULE-SHOP-003`, `BRULE-GAP-001`, `BRULE-GAP-002`, `BRULE-MULT-003`, `BRULE-MULT-004`, `BRULE-MULT-005`, `BRULE-MULT-006`, `BRULE-MULT-007`, `BRULE-MULT-008`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-015`, `BR-016`, `BR-017`, `BR-018`, `BR-020`, `BR-022`, `BR-024`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-10`, `CAP-08`, `CAP-09` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- This use case consumes available candidate information; it does not add catalog search/import, user-uploaded candidate ingestion or new retailer actors.
- The extension point is the explicit optional external-link action. No mandatory evaluation/include relationship or purchase flow is introduced.

- No stock, price, partnership, purchase/transaction or durability guarantee follows from informational guidance. Destination unavailability affects navigation alone, preserving otherwise valid candidate/gap/multiplier/reasons/previews where available.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-020 — View Wardrobe Gaps](UC-020-view-wardrobe-gaps.md): Understand the supported wardrobe capability gap.
- [UC-022 — Evaluate Candidate Garment](UC-022-evaluate-candidate-garment.md): Request/review candidate utility as a related goal, not a UML include.
- [UC-023 — Open External Shopping Link](UC-023-open-external-shopping-link.md): The master diagram's sole extend relationship: optional explicit navigation at an available external-link action.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-021-view-shopping-recommendations.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
