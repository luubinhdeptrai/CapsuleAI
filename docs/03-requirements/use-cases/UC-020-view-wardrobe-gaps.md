# UC-020 — View Wardrobe Gaps

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-020 |
| Use Case Name | View Wardrobe Gaps |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Identify evidence-supported underserved wardrobe capabilities for the User's positive-priority needs, understand useful candidate characteristics and choose whether to explore utility without a required purchase.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Understand the capability bottleneck and avoid artificial missing-product or compulsory-shopping claims. |

## 4. Preconditions

- The User has authenticated access authorized for the current wardrobe, common need priorities and context. A completed sufficient coverage assessment or existing gap is not a prerequisite for requesting analysis.

## 5. Trigger

The User requests wardrobe gap insights.

## 6. Main Success Scenario

1. The User requests gap analysis for the current wardrobe/needs.
2. CapsuleAI identifies the applicable current coverage basis and supporting need evidence, evaluating or reusing it only when current and defensible.
3. CapsuleAI presents evidence-supported underserved capabilities for positive-priority needs whose NeedCoverage is below full coverage.
4. The User selects a gap to inspect.
5. CapsuleAI explains the bottleneck, affected need/context and useful garment characteristics, distinguishing the capability from any particular product.
6. Where an evaluable candidate exists, CapsuleAI offers related candidate/shopping utility exploration; the User chooses whether to explore it or continue using current wardrobe insights.

## 7. Alternative Flows

### A1 — No important gap

At Main Step 3:

1. CapsuleAI explains that no important gap is established by the current defensible assessment.
2. The User continues to outfits/wardrobe insights; no purchase demand or invented gap is shown.

### A2 — Supported gap without a useful/evaluable candidate

At Main Step 6:

1. CapsuleAI retains the supported capability explanation and clarifies the lack of an available useful candidate/evaluation.
2. The User can revise wardrobe/context or continue insights; the gap does not force one product or an external shopping destination.

### A3 — User declines shopping

At Main Step 6:

1. The User leaves candidate exploration or chooses to use owned wardrobe insights.
2. Core wardrobe, recommendation and gap information remain available without purchase, affiliate participation or navigation.

## 8. Exception / Failure Flows

### E1 — Insufficient assessment evidence

At Main Step 2:

1. CapsuleAI identifies the missing coverage inputs and withholds unsupported gap claims.
2. Insufficient information is not 0% coverage. The User can adjust priorities/context or enrich ready garment information and request again.

### E2 — Coverage basis changes or cannot be refreshed

At Main Step 3:

1. CapsuleAI refreshes the affected evidence or marks prior gap advice outdated with the coverage basis.
2. A failed reassessment is not current gap evidence; the User can retry or return to available wardrobe/context.

### E3 — Gap retrieval unavailable

At Main Step 3:

1. CapsuleAI states that gap information is unavailable, distinguishing it from a defensible no-important-gap result.
2. The User may retry; no specific missing product or obligatory shopping action is substituted.

### E4 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- Displayed gaps describe evidence-supported underserved capabilities and useful characteristics under the current positively prioritized needs.
- A completed no-important-gap response or a useful gap without a candidate remains meaningful; no purchase or ownership change is implied.

## 10. Minimal / Failure Postconditions

- Failed/insufficient/outdated analysis does not manufacture a capability gap, numeric zero or purchase obligation.
- Accepted wardrobe/profile/history remains intact, and insight use continues independently of shopping.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-GAP-001` | Identify the underserved capability/bottleneck before candidate products. |
| `BRULE-GAP-002` | Distinguish no important gap from insufficient assessment; never fabricate demand. |
| `BRULE-COV-001` | Use the established personalized needs/occasion mapping. |
| `BRULE-COV-002` | Only positive-priority needs can support relevant gaps. |
| `BRULE-COV-003` | NeedCoverage below full coverage supplies the evidence, with identity-based valid outfits. |
| `BRULE-COV-005` | Sufficient assessment is needed; missing evidence is not zero. |
| `BRULE-COV-006` | Relevant changes invalidate current coverage/gap advice. |
| `BRULE-SHOP-003` | Core insight utility does not depend on commerce. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-06` |
| [Product Features](../../02-product/PRD.md) | `FEAT-GAP-001`, `FEAT-ANL-002` |
| [Software Requirements — Functional](../SRS.md) | `FR-GAP-001`, `FR-GAP-002`, `FR-GAP-003`, `FR-GAP-004`, `FR-GAP-005`, `FR-ANL-014`, `FR-ANL-015`, `FR-AUTH-008`, `FR-WEATHER-003` |
| [Software Requirements — Data](../SRS.md) | `DATA-ANL-001`, `DATA-ANL-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-ANL-001`, `ERR-ANL-002`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001`, `ERR-WEATHER-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-GAP-001`, `BRULE-GAP-002`, `BRULE-COV-001`, `BRULE-COV-002`, `BRULE-COV-003`, `BRULE-COV-005`, `BRULE-COV-006`, `BRULE-SHOP-003`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-010`, `BR-014`, `BR-015`, `BR-022`, `BR-024`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-08`, `CAP-07` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- A gap represents an underserved wardrobe capability, not merely a missing specific product. Multiple possible candidates can address it; no universal product checklist is added.
- A candidate suggestion is hypothetical and separate from confirmed owned garments; viewing a gap never ingests a garment.

- Coverage/gap evidence consumes environmental context governed by ≤30-minute currentness and the 2-second acquisition boundary; unavailable weather must not manufacture a gap or numeric zero.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-019 — View Wardrobe Coverage](UC-019-view-wardrobe-coverage.md): Review the contextual coverage evidence; related goal, not a UML include.
- [UC-005 — Update Personalization Profile](UC-005-update-personalization-profile.md): Revise common need priorities.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Improve confirmed garment readiness where relevant.
- [UC-021 — View Shopping Recommendations](UC-021-view-shopping-recommendations.md): Explore useful candidate recommendations optionally.
- [UC-022 — Evaluate Candidate Garment](UC-022-evaluate-candidate-garment.md): Evaluate a supported candidate's incremental utility.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-020-view-wardrobe-gaps.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
