# UC-019 — View Wardrobe Coverage

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-019 |
| Use Case Name | View Wardrobe Coverage |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.2 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Understand how the current confirmed wardrobe supports the User's positively prioritized needs through an explainable Personalized Everyday Capsule coverage assessment, with a numeric score only when the assessment is defensible.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Understand contextual strengths/underserved needs and assessment limitations without a universal-completeness or purchase claim. |

## 4. Preconditions

- The User has authenticated access authorized for current wardrobe/profile/context. Positive priorities, complete context and ready garment roles are assessment conditions, not prerequisites for opening coverage.

## 5. Trigger

The User requests wardrobe coverage.

## 6. Main Success Scenario

1. The User requests the Personalized Everyday Capsule coverage assessment.
2. CapsuleAI identifies the current confirmed eligible wardrobe, common need priorities, mapped occasions, style and available environmental context used.
3. CapsuleAI presents a completed coverage assessment when sufficient-data conditions hold, identifying the score's contextual basis.
4. CapsuleAI shows the priority-weighted Wardrobe Coverage Score and supporting per-need valid-distinct-outfit coverage, separating relevant positively weighted needs from Not Relevant needs.
5. The User inspects an underserved/covered need and its explanation.
6. CapsuleAI explains the evidence, limitations and available next actions without making a specific purchase compulsory.

## 7. Alternative Flows

### A1 — Defensible evaluated zero

At Main Step 4:

1. The sufficient-data gate is met, but no hard-valid combinations support the relevant needs.
2. CapsuleAI shows a genuinely evaluated 0% with explanatory evidence; it is not substituted for missing priorities, ready roles or required information.

### A2 — All relevant needs covered / no important gap

At Main Step 5:

1. CapsuleAI shows the evaluated covered needs and explains that no important gap is established.
2. The User can continue using owned outfits without manufactured demand.

### A3 — User revises need/context/wardrobe

At Main Step 6:

1. The User chooses a separate profile, location or garment-maintenance action.
2. Relevant accepted input changes require a refreshed assessment or an explicit outdated state; a new request resumes at Main Step 1.

### A4 — Some needs are Not Relevant

At Main Step 4:

1. CapsuleAI excludes zero-priority needs from the weighted denominator and explains their status.
2. Coverage is interpreted against the User's positive priorities rather than a universal checklist.

## 8. Exception / Failure Flows

### E1 — Insufficient priorities, ready roles or required information

At Main Step 3:

1. CapsuleAI explains the missing assessment conditions and withholds a false numeric 0%.
2. A numeric score requires at least one positive priority and applicable ready TOP, BOTTOM and FOOTWEAR, with OUTERWEAR only when required by the evaluated need/environment, plus defensible required inputs.
3. The User may update the relevant profile, garment or context information and request again; no missing-product demand is fabricated.

### E2 — Environmental information unavailable/stale

At Main Step 2:

1. CapsuleAI discloses the missing environmental context.
2. Continue to Main Step 3 only where a valid reduced-context assessment is defensible under the rules; otherwise explain the specific insufficient-information state and allow context revision/retry.

### E3 — Changed basis or failed reassessment

At Main Step 3:

1. CapsuleAI refreshes coverage or marks the prior assessment outdated after relevant wardrobe/attributes, needs, context or applicable rule changes.
2. Failed refresh does not establish a current score; the User may retry while prior accepted inventory/profile remains intact.

### E4 — Coverage retrieval unavailable

At Main Step 4:

1. CapsuleAI distinguishes unavailable information from evaluated zero and insufficient data.
2. The User can retry or continue to usable wardrobe/context; no score or need evidence is invented.

### E5 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- A completed assessment identifies its current contextual basis, positive priorities, supporting need coverage and priority-weighted score, including defensible zero when applicable.
- No universal wardrobe-completeness, ownership change or compulsory-purchase outcome is established.

## 10. Minimal / Failure Postconditions

- Insufficient/unavailable/outdated assessments are not current numeric zero scores.
- Accepted wardrobe/profile/history remains intact; no unsupported gap or purchase obligation is inferred.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-007` | Personal information is purpose-limited/private; MVP personal data is not used for AI training/improvement or unrestricted external sharing. |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-COV-001` | Use the six established needs and their mapped occasions. |
| `BRULE-COV-002` | Priorities 0/1/2/3 mean Not Relevant/Low/Medium/High; only positive weights contribute. |
| `BRULE-COV-003` | NeedCoverage is min(valid distinct outfits for the need / 3, 1), with identity-based counting across mapped occasions. |
| `BRULE-COV-004` | CoverageScore is 100 × weighted NeedCoverage sum / positive-priority sum. |
| `BRULE-COV-005` | Separate sufficient-data evaluated zero from insufficient information. |
| `BRULE-COV-006` | Relevant input/rule changes require refresh or outdated status. |
| `BRULE-GAR-003` | Use applicable ready confirmed garments; Saveable alone is insufficient. |
| `BRULE-OUT-002` | Removed garments and unowned candidates are excluded from current coverage. |
| `BRULE-OUT-008` | Hard validity constrains the evidence before any soft preferences. |
| `BRULE-OUT-006` | Missing weather uses disclosed reduced context only when defensible. |
| `BRULE-GAP-002` | Coverage limitations do not manufacture gaps or purchase obligations. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-06` |
| [Product Features](../../02-product/PRD.md) | `FEAT-ANL-002`, `FEAT-PROF-001` |
| [Software Requirements — Functional](../SRS.md) | `FR-ANL-008`, `FR-ANL-009`, `FR-ANL-010`, `FR-ANL-011`, `FR-ANL-012`, `FR-ANL-013`, `FR-ANL-014`, `FR-ANL-015`, `FR-PROF-003`, `FR-PROF-004`, `FR-AUTH-008`, `FR-WEATHER-003` |
| [Software Requirements — Data](../SRS.md) | `DATA-ANL-001`, `DATA-OUT-001`, `DATA-OUT-002` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-ANL-001`, `ERR-ANL-002`, `ERR-WEATHER-001`, `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-003`, `UI-004`, `UI-008`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-ACC-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002`, `NFR-REL-002`, `NFR-TEST-001`, `NFR-PRIV-001`, `NFR-PRIV-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-COV-001`, `BRULE-COV-002`, `BRULE-COV-003`, `BRULE-COV-004`, `BRULE-COV-005`, `BRULE-COV-006`, `BRULE-GAR-003`, `BRULE-OUT-002`, `BRULE-OUT-008`, `BRULE-OUT-006`, `BRULE-GAP-002`, `BRULE-AUTH-007` |
| [Business Requirements](../../01-business/BRD.md) | `BR-010`, `BR-014`, `BR-022`, `BR-024`, `BR-021` |
| [Capabilities](../../01-business/BRD.md) | `CAP-07`, `CAP-08` |

## 13. Special Requirements / Constraints

- Documentation is English; the Vietnamese MVP UI supports Android 10+ and iOS 15+. Translated labels preserve canonical meanings. Applicable actions/states have meaningful accessible names/roles/states and understandable labels beyond color, and remain operable with primary actions accessible at text scaling up to 200%. TalkBack/VoiceOver validation and platform primary touch-target criteria follow SRS Sections 6.7/12.4.
- The six needs are Everyday/Casual, Work, School/University, Formal, Travel and Sport. Current request occasion and common need priorities serve different purposes.
- Numeric coverage and per-need evidence follow the Business Rules, not the small displayed daily recommendation subset. Details of calculation implementation remain downstream.
- No new outerwear-by-temperature mandate or fixed wardrobe quota is introduced. Available context is consumed here; external acquisition is UC-006.

- Environmental evidence follows UC-006's ≤30-minute weather currentness and ≤2-second external-request delay boundary. Unavailable weather permits disclosed reduced-context coverage only when remaining sufficient-data/validity conditions hold.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-005 — Update Personalization Profile](UC-005-update-personalization-profile.md): Change common need priorities and preferences.
- [UC-006 — Set Location Context](UC-006-set-location-context.md): Set available environmental context.
- [UC-009 — Edit Garment](UC-009-edit-garment.md): Enrich applicable confirmed garment information.
- [UC-020 — View Wardrobe Gaps](UC-020-view-wardrobe-gaps.md): Inspect evidence-supported underserved capabilities.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Use current owned outfits independently of shopping.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction.

## 16. Focused Use Case Diagram

[Focused Use Case Diagram](diagrams/UC-019-view-wardrobe-coverage.puml)

This focused diagram is a local projection of the master Use Case Diagram. Detailed workflow behavior is defined by this specification and by later Activity Diagrams.
