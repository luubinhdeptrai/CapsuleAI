# UC-023 — Open External Shopping Link

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-023 |
| Use Case Name | Open External Shopping Link |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | External Shopping Destination |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Explicitly navigate from supported candidate advice to an available external shopping destination, with clear boundary/recovery and no inference of purchase, ownership or wear.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Know navigation is external and retain useful CapsuleAI advice after return or known failure. |
| External Shopping Destination | Receive the optional navigation request; its own availability/actions are outside CapsuleAI's transaction responsibility. |

## 4. Preconditions

- The User has authenticated access to the identified candidate advice in UC-021.
- An external-link action is offered only for available credible destination information; destination opening/success is not guaranteed.

## 5. Trigger

At UC-021's optional external-link action, the User explicitly chooses an offered available shopping link.

## 6. Main Success Scenario

1. The User chooses the available external shopping link for the identified candidate.
2. CapsuleAI identifies the navigation as external and retains the available candidate/gap/utility context.
3. CapsuleAI hands off the chosen navigation to the External Shopping Destination.
4. The External Shopping Destination opens when the handoff/destination is available; CapsuleAI does not represent this as purchase or transaction completion.

## 7. Alternative Flows

### A1 — Normal return

At Main Step 4:

1. The User returns to CapsuleAI after external navigation.
2. CapsuleAI restores available candidate/gap/utility context. Relevant changed advice is refreshed or marked outdated rather than shown as current solely because of return.

### A2 — User stays with CapsuleAI advice

At Main Step 1:

1. Before initiating handoff, the User declines or cancels the external navigation.
2. The User remains with candidate/core advice and no external action or purchase is reported as completed.

## 8. Exception / Failure Flows

### E1 — Destination becomes absent/unavailable

At Main Step 3:

1. CapsuleAI explains unavailable external navigation and retains otherwise valid candidate information, gap, multiplier, utility reasons and hypothetical previews where available; destination failure changes no utility/ownership/transaction state.
2. The User may continue core advice or retry an available action where appropriate; no substitute merchant claim or purchase is invented.

### E2 — Handoff fails or outcome cannot be established

At Main Step 4:

1. CapsuleAI distinguishes known failed navigation from an unconfirmed opening outcome.
2. The User may return/retry where appropriate while useful candidate/gap context remains available; CapsuleAI does not claim confirmed opening, purchase or ownership from an uncertain response.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- For successful navigation, the User has been directed to the explicitly selected external destination; available CapsuleAI candidate/context information remains recoverable on normal return.
- No purchase/sale, transaction success, wardrobe addition or Wear Event is established by opening or returning.

## 10. Minimal / Failure Postconditions

- Known failed/uncertain navigation is not represented as confirmed opening or purchase; available candidate utility remains usable where possible.
- Owned wardrobe, Wear history and profile are not changed by navigation; external destinations receive no unrestricted private-data access.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-SHOP-002` | External navigation is optional and not purchase/ownership/wear proof; known failure retains available context. |
| `BRULE-SHOP-001` | Only credible available destination information supports the optional action. |
| `BRULE-SHOP-003` | Core utility remains independent of merchant/affiliate participation. |
| `BRULE-MULT-008` | Changed inputs/rules invalidate current candidate utility even after return. |
| `BRULE-AUTH-001` | External navigation does not relax private-account authorization. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-07` |
| [Product Features](../../02-product/PRD.md) | `FEAT-SHOP-002`, `FEAT-MET-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-SHOP-005`, `FR-SHOP-006`, `FR-SHOP-007`, `FR-SHOP-008`, `FR-MET-003`, `FR-MET-007`, `FR-AUTH-008`, `FR-SHOP-003`, `FR-MET-006` |
| [Software Requirements — Data](../SRS.md) | `DATA-INT-003`, `DATA-ANL-003`, `DATA-RET-003` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-SHOP-003`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `SI-004`, `COM-003`, `UI-003`, `UI-009`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PRIV-002`, `NFR-AVL-001`, `NFR-INT-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001` |
| [Business Rules](../business-rules.md) | `BRULE-SHOP-002`, `BRULE-SHOP-001`, `BRULE-SHOP-003`, `BRULE-MULT-008`, `BRULE-AUTH-001`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-018`, `BR-019`, `BR-021`, `BR-022`, `BR-024` |
| [Capabilities](../../01-business/BRD.md) | `CAP-10` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- UC-023 extends UC-021 only when an available external shopping link is offered and the User explicitly chooses it. There is no include or required commerce dependency.
- No cart, checkout, payment, order, fulfillment, purchase verification or automatic garment ingestion is part of this goal.
- Measurement separates link attempts/known opening from verified sales; measurement failure cannot block otherwise usable navigation/core advice.

- Destination/link information is supported by explicit user evidence or an identifiable configured credible source. Applicable external commercial guidance retains source/time and follows 24-hour price/availability currentness; no retailer/stock/price/transaction/durability guarantee is made.
- Only explicitly chosen navigation is handed off; unrelated private wardrobe/profile/history is not disclosed. Personal data is not sold or used for MVP model training. User-linked link-engagement measurement is deleted or aggregated/de-identified within 90 days.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-021 — View Shopping Recommendations](UC-021-view-shopping-recommendations.md): Base use case extended at its optional available external-link action; this is the master diagram's sole extend relationship.
- [UC-022 — Evaluate Candidate Garment](UC-022-evaluate-candidate-garment.md): Understand the independently evaluated candidate utility; related goal only.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-023-open-external-shopping-link.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
