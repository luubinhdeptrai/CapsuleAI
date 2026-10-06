# UC-005 — Update Personalization Profile

## 1. Use Case Summary

| Field | Value |
| --- | --- |
| Use Case ID | UC-005 |
| Use Case Name | Update Personalization Profile |
| Scope | CapsuleAI |
| Level | User Goal |
| Primary Actor | User |
| Supporting Actors | None |
| Version | 0.1 |
| Status | Baseline Draft |

Source authority: [BRD](../../01-business/BRD.md) defines business intent; [PRD](../../02-product/PRD.md) defines product behavior; [SRS](../SRS.md) and [Business Rules](../business-rules.md) constrain interaction; the [master Use Case Diagram](use-case-diagram.puml) defines this goal and its actors. The [workflow](../../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md) governs artifact ownership and sequencing.

## 2. Goal

Set or revise declared style, common capsule need priorities and optional personal context so future advice reflects the User's needs while preserving omission/removal choices.

## 3. Stakeholders and Interests

| Stakeholder | Interest |
| --- | --- |
| User | Control relevant preferences and sensitive optional context. |

## 4. Preconditions

- The User has authenticated access authorized for the personal profile.

## 5. Trigger

The User reaches personalization setup or chooses to revise the profile.

## 6. Main Success Scenario

1. The User requests personalization setup or revision.
2. CapsuleAI presents current or unset style choices, common need priorities and optional body/gender information using the controlled vocabularies.
3. The User selects or revises relevant styles and common need priorities, and chooses whether to provide optional personal information.
4. CapsuleAI makes the selected information and its meaning understandable, including the distinction between ongoing needs and a current outfit request occasion.
5. The User submits the intended profile changes.
6. CapsuleAI acknowledges accepted changes, displays the current profile and uses it in subsequent relevant ranking/assessments; affected assessments are reevaluated or identified as outdated where required.

## 7. Alternative Flows

### A1 — Initial or partial setup

At Main Step 2:

1. CapsuleAI shows unset information as absent rather than an inferred personal profile.
2. The User supplies only the information they choose and resumes at Main Step 3.

### A2 — Skip or defer optional context

At Main Step 3:

1. The User omits body/gender or defers optional setup.
2. CapsuleAI permits authenticated product entry; later setup remains available and relevant limited-context explanations identify missing information.

### A3 — Remove optional personal information

At Main Step 3:

1. The User removes previously provided body/gender information and submits the revision.
2. After acceptance, CapsuleAI represents it as absent and stops using it in future personalization; resume at Main Step 6.

### A4 — Change common priorities

At Main Step 3:

1. The User selects Not Relevant, Low, Medium or High for the applicable capsule needs.
2. Accepted priorities affect contextual Coverage/gaps without overwriting a separately selected current recommendation occasion.

### A5 — Cancel an unsubmitted revision

At Main Step 5:

1. The User leaves without submitting the proposed change.
2. The last accepted profile remains current.

## 8. Exception / Failure Flows

### E1 — Unsupported or unusable profile values

At Main Step 5:

1. CapsuleAI explains the values that cannot be accepted under the controlled vocabularies/priorities.
2. The User corrects the applicable choices and resumes at Main Step 3; no invented replacement profile is saved.

### E2 — Profile update failed or uncertain

At Main Step 6:

1. CapsuleAI distinguishes rejected/failed changes from accepted updates, retaining prior accepted context on known failure.
2. The User reviews the outcome or retries the intended revision; an uncertain outcome is not reported as saved.

### E3 — Authorized access becomes unusable

At Main Step 1:

1. CapsuleAI pauses protected interaction if access expires, is revoked or is not authorized for the affected information, and explains the need to authenticate again.
2. The User can resume the appropriate goal after UC-002 establishes usable access. Protected information is not exposed or misrepresented as empty, and the interrupted request is not falsely reported as accepted.

## 9. Success Postconditions

- The accepted style/common priorities and optional personal context are current for the User.
- Removed optional information stops influencing future personalization; supported garment categories remain eligible regardless of body/gender.
- The current request occasion remains separate from persistent common need priorities.

## 10. Minimal / Failure Postconditions

- Canceled/rejected/known failed revisions do not replace the last accepted profile.
- Uncertain updates remain visibly unconfirmed until the accepted outcome is reviewed; private profile data stays authorized.
- Loss of access pauses protected use; inaccessible personal data is not exposed or labeled empty. Prior accepted information remains governed by its authorized state.

## 11. Business Rules

The following references constrain this interaction; detailed policy remains in the [Business Rules](../business-rules.md).

| Rule ID | Relevance |
| --- | --- |
| `BRULE-AUTH-001` | Personal information/actions require authorized access for the affected User. |
| `BRULE-PROF-001` | Body/gender may be omitted, revised or removed and influence ranking only softly. |
| `BRULE-PROF-002` | Declared styles/common priorities remain distinct from the request occasion. |
| `BRULE-COV-002` | Priority weights are Not Relevant=0, Low=1, Medium=2 and High=3. |
| `BRULE-GAR-004` | Styles, occasions and capsule need dimensions use the current controlled meanings. |
| `BRULE-PERS-006` | Accepted context/evidence revisions affect future effective personalization. |
| `BRULE-COV-006` | Relevant need/context changes refresh or invalidate prior Coverage. |

## 12. Requirements Traceability

| Source Type | IDs |
| --- | --- |
| [Journeys](../../02-product/PRD.md) | `JRN-01`, `JRN-05` |
| [Product Features](../../02-product/PRD.md) | `FEAT-PROF-001`, `FEAT-PERS-003` |
| [Software Requirements — Functional](../SRS.md) | `FR-PROF-001`, `FR-PROF-002`, `FR-PROF-003`, `FR-PROF-004`, `FR-PROF-005`, `FR-PROF-006`, `FR-PROF-007`, `FR-PROF-008`, `FR-PERS-010`, `FR-PERS-013`, `FR-ANL-014`, `FR-AUTH-008` |
| [Software Requirements — Data](../SRS.md) | `DATA-AUTH-003`, `DATA-AUTH-004`, `DATA-INT-004` |
| [Software Requirements — Failure / Recovery](../SRS.md) | `ERR-AUTH-001`, `ERR-AUTH-003`, `ERR-NET-001` |
| [Software Requirements — Interfaces](../SRS.md) | `UI-002`, `COM-001`, `COM-002` |
| [Software Requirements — Quality / Localization](../SRS.md) | `NFR-PRIV-001`, `LOC-001`, `LOC-002`, `NFR-USE-001`, `NFR-ACC-002` |
| [Business Rules](../business-rules.md) | `BRULE-AUTH-001`, `BRULE-PROF-001`, `BRULE-PROF-002`, `BRULE-COV-002`, `BRULE-GAR-004`, `BRULE-PERS-006`, `BRULE-COV-006` |
| [Business Requirements](../../01-business/BRD.md) | `BR-010`, `BR-012`, `BR-014`, `BR-021`, `BR-022` |
| [Capabilities](../../01-business/BRD.md) | `CAP-01`, `CAP-06` |

## 13. Special Requirements / Constraints

- Documentation is English; the initial Android/iOS product UI, explanations and recovery guidance are Vietnamese. Translated labels preserve canonical meanings. Core action outcomes and significant states must be understandable in the agreed accessibility scenarios.
- The initial capsule contains six contextual need dimensions rather than separate persona-based or universal wardrobe checklists; their mapping remains in BRULE-COV-001.
- Body/gender never prohibit a supported category or override hard validity. Omission is not replaced by inferred measurements or gender.

## 14. Related Use Cases

Related goals do not imply UML include relationships. The master diagram defines the optional UC-023 extension of UC-021; no other UML dependency is introduced here.

- [UC-006 — Set Location Context](UC-006-set-location-context.md): Choose environmental/location context separately.
- [UC-011 — Get Outfit Recommendations](UC-011-get-outfit-recommendations.md): Use declared preferences alongside request-specific choices.
- [UC-019 — View Wardrobe Coverage](UC-019-view-wardrobe-coverage.md): Inspect Coverage based on accepted common priorities.
- [UC-002 — Log In](UC-002-log-in.md): Reestablish usable authorized access if it is lost.

## 15. Open Issues

No unresolved Use Case-specific issue currently blocks this interaction. The following existing downstream acceptance gates remain governed by the [SRS](../SRS.md); they are not resolved by this specification.

| Issue | Relevant boundary |
| --- | --- |
| `OSQ-011` | Consent, access/sharing and retention/physical deletion policy for profile information remain at final privacy/data acceptance; effective omission/removal behavior is already defined. |
| `OSQ-014` | Representative-user tasks, usability criteria and Android/iOS assistive-interaction acceptance remain governed by the SRS; this specification does not select new conformance or quantified thresholds. |
