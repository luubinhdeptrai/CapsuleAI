# US-WAR-002 — Inspect an owned garment

**Current property-model revision — 2026-11-05:** The Product Owner approved exactly 13 garment properties and every explicitly listed vocabulary in the [garment-property authority](../../../../Initial%20files/Image%20to%20Properties%20Extraction.md). This supersedes earlier research-only vocabulary status, removed garment attributes, fixed composition assumptions and property-based readiness gates. All applicable properties are included in the selected Sprint 2 garment stories. Recommendation eligibility is not evaluated; engine composition/compatibility policy is deferred. Stable IDs below remain traceable; superseded policies are not active implementation or acceptance obligations.

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-WAR-002 |
| Version | 0.5 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Revision note | 2026-10-21: first focused refinement; clarified source-backed acceptance/evidence and traceability; identity, scope and Baseline Draft status preserved. 2026-10-22: linked related full-backlog drafts and explicit identity/domain metadata; existing outcome and AC retained. |
| Parent PBI | [PBI-007](../../product-backlog.md#pbi-007--browse-current-wardrobe-and-inspect-owned-garments) |
| Domain | Wardrobe |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Trustworthy Wardrobe Understanding](../../product-goal.md#4-desired-product-outcomes) |
| Product revision | 2026-11-05: Approved eight-category taxonomy and optional subtype/Brand/purchase Price synchronized. Existing story/AC IDs, parent PBI and Sprint 2 order retained; no implementation claimed. |

## 1. Story

As an authenticated user, I want to inspect an owned garment's accepted information and eligibility under a future approved policy, so that I can understand what CapsuleAI knows about that garment and the limits of advice based on it.

## 2. User Value

Inspection explains one possession rather than the collection as a whole. Accepted profile, unevaluated eligibility and qualified use information belong together because they establish what can be trusted about the selected garment.

## 3. Scope

Read-only detail for a selected current confirmed garment, including available image/profile/provenance/uncertainty, eligibility under a future policy and supported recorded-use information when that capability is available. Includes loading, retrieval failure, changed ownership, authorization and return to browsing.

## 4. Preconditions

- I have authenticated access authorized for the selected garment's wardrobe.
- Successful current detail requires a selected confirmed active owned garment. Missing images/rich fields do not disqualify it; a changed or no-longer-current selection is handled within the story.
- Wear reporting, utilization analytics and completed optional personalization are not prerequisites to inspect accepted garment information.

## 5. Acceptance Criteria

### AC-01 — Retrieve every available applicable accepted property

**Given** I select an authorized current confirmed garment

**When** detail retrieval succeeds

**Then** return the correct accepted Category, Subtype, Color, Material selections, Pattern, Style selections, applicable Fit/Neckline/Sleeve Length/Length/Silhouette, Brand and purchase Price with currency. Missing optional values stay missing; multiple distinct Materials and exact accepted labels/amounts are preserved. Available image and useful metadata remain separate; suggestions never replace confirmed values.

### AC-02 — Inspect an imageless or incomplete entry

**Given** the selected current garment has no image or has only the confirmed Saveable minimum

**When** detail retrieval succeeds

**Then** it remains identifiable and inspectable through accepted information. Missing image, unavailable descriptors and unevaluated eligibility retain their distinct meanings; no image, Brand, Price, default currency, material, color or other descriptor is fabricated to make the profile look complete. Missing Price is unknown, not zero; displayed Price means the user-reported purchase price, not current retailer or market value.

### AC-03 — Represent recommendation eligibility truthfully

**Given** the selected garment has valid accepted ownership

**When** detail presents evaluation information

**Then** communicate that recommendation eligibility has not been evaluated. Neither optional absence nor undefined engine policy establishes ready, unsuitable, invalid or ineligible status; no removed property or enrichment gate is imposed.

### AC-04 — Respect category applicability in detail

**Given** the selected garment belongs to any supported category

**When** accepted properties are returned

**Then** show Fit/Neckline/Sleeve Length only as applicable to Tops, Length under its Tops/Pants vocabulary, and Silhouette only for Pants. Preserve all globally applicable accepted properties, including multiple Materials, without inventing inapplicable values or inferring fields from Subtype.

### AC-05 — Keep optional omissions separate from failed retrieval

**Given** a saved garment lacks one or more optional properties

**When** available detail is retrieved

**Then** preserve the usable accepted profile, distinguish absent values from an unavailable request, and fabricate no data or evaluated eligibility. Category/Color confirmation remains sufficient for saving; detail viewing changes no accepted value or ownership.

### AC-06 — Qualify available reported-use information

**Given** supported reported-use information is available for this garment

**When** it is displayed in detail

**Then** indicators reflect accepted effective Wear Events and their reporting limits. No recorded events is described as no recorded use rather than proof of never having physically worn the garment; information is not fabricated from missing reports. This presentation consumes available evidence and does not implement reporting or utilization analytics.

### AC-07 — Keep unavailable use evidence distinct

**Given** reported-use capabilities/evidence are not yet available or cannot be retrieved, but accepted garment detail is available

**When** I inspect the garment

**Then** CapsuleAI retains usable accepted profile / unevaluated eligibility inspection and does not manufacture zero use, last-worn information or a no-recorded-events conclusion from unavailable evidence. Availability of future reporting/analytics is not a gate to viewing this garment.

### AC-08 — Distinguish detail loading and retrieval failure

**Given** detail retrieval is pending or fails

**When** I review the selected-garment outcome

**Then** CapsuleAI distinguishes loading from unavailable detail, offers applicable retry or return to usable prior context on failure, and does not call the wardrobe empty or the selected garment deleted merely because retrieval failed. Accepted wardrobe information remains unchanged.

### AC-09 — Handle a selection that is no longer current

**Given** the garment changed or was removed from current ownership after selection

**When** CapsuleAI establishes the changed current state during inspection

**Then** it explains the changed/no-longer-current item and refreshes the current wardrobe where possible, allowing return to browsing. A minimal historical snapshot stays separate and does not restore current ownership; this story does not perform removal or implement historical views.

### AC-10 — Inspect and return without mutation

**Given** usable detail is presented

**When** I inspect it, return to browsing or leave without a separate maintenance action

**Then** CapsuleAI offers the return path and inspection alone creates no addition, edit, removal, Wear Event or preference feedback. Accepted values and ownership remain unchanged by viewing.

### AC-11 — Protect detail when authorized access is lost

**Given** access expires, is revoked or is not authorized for the affected garment

**When** I open or continue inspecting it

**Then** CapsuleAI pauses protected use, explains the need for usable authorized access and exposes no other account's garment information. It does not present inaccessible detail as an empty wardrobe; inspection can resume for the correct account after valid authentication.

### AC-12 — Understand the detail and its limits

**Given** I use the Vietnamese detail interaction with a screen reader or text scaled up to 200%

**When** I inspect accepted information and unevaluated eligibility, qualified use information, failure guidance or the return action

**Then** the information, state meanings and actions remain operable and understandable through meaningful accessible names/roles/states beyond image or color alone. Saved ownership, unevaluated eligibility, uncertain descriptors and no-recorded/unavailable use evidence remain distinguishable.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-GAR-001–BRULE-GAR-003 preserve authorized ownership, authoritative accepted values and eligibility under a future policy; BRULE-GAR-006 prohibits invented compatibility information.
- BRULE-HIST-001 limits use indicators to reported evidence; BRULE-HIST-002 prevents historical snapshots from becoming current owned detail. Confirmation is distinct from verified physical material composition.
- The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-004's authenticated access; [US-AUTH-002](../auth/US-AUTH-002-log-in.md) and [US-AUTH-003](../auth/US-AUTH-003-resume-session.md) supply access/continuity where applicable.
- [US-WAR-001](US-WAR-001-browse-current-wardrobe.md) supplies the collection selection/return route. The actual detail input is a current owned garment; no single creation method is a hard dependency.
- PBI-006 / [US-GAR-001](../garment/US-GAR-001-add-garment-manually.md) supply useful minimum-only/imageless examples. PBI-008 supplies later editing; neither is a new mandatory completion gate for already available detail.
- Reported-use presentation integrates with available evidence as its owning capabilities arrive (PBI-021/PBI-025); those capabilities are not hard prerequisites to accepted profile / unevaluated eligibility inspection.

- Related [US-GAR-002](../garment/US-GAR-002-correct-and-enrich-garment-information.md) enrichment, [US-WEAR-002](../wear/US-WEAR-002-review-distinct-wear-history.md) history and [US-ANL-001](../analytics/US-ANL-001-understand-reported-wardrobe-utilization.md) utilization remain separate outcomes. Available recorded-use evidence can be consumed without making those whole capabilities prerequisites to profile inspection.

## 8. Out of Scope

Collection browsing is US-WAR-001. All 13 applicable creation properties belong to US-GAR-001; later editing is PBI-008; search/filtering is PBI-015; removal and retention enforcement are PBI-016. Photo/AI entry/proposal review remain PBI-012/PBI-013. Wear reporting/history and utilization calculations/insights remain their owning PBIs, including PBI-021/PBI-025. No new reporting action, analytics event, restore action, image generation, recommendation algorithm or implementation design is introduced.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-007](../../product-backlog.md#pbi-007--browse-current-wardrobe-and-inspect-owned-garments) |
| Business | [BRD](../../../01-business/BRD.md): BR-003, BR-005, BR-006, BR-021, BR-022; CAP-03, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-03; FEAT-WAR-001, FEAT-GAR-001 (accepted accepted profile / unevaluated eligibility); FEAT-ANL-001, available indicators only |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WAR-001, FR-WAR-002, FR-GAR-004, FR-GAR-005, FR-GAR-006, FR-AUTH-008; DATA-GAR-001, DATA-GAR-003, DATA-GAR-004, DATA-GAR-005; UI-004; ERR-GAR-002, ERR-AUTH-001, ERR-NET-001; COM-001; NFR-SEC-001, NFR-SEC-002, NFR-PRIV-001; NFR-ACC-001, NFR-ACC-002; LOC-001; FR-GAR-001, FR-GAR-002, FR-GAR-012, FR-GAR-013, DATA-GAR-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-007; BRULE-GAR-001, BRULE-GAR-002, BRULE-GAR-003, BRULE-GAR-006; BRULE-HIST-001, BRULE-HIST-002; BRULE-GAR-004, BRULE-GAR-010 |
| Use Cases | [UC-008 — View Wardrobe](../../../03-requirements/use-cases/UC-008-view-wardrobe.md), selected detail/accepted-properties/use/return and failure branches only |
| Activity Diagrams | No current dedicated Activity Diagram for UC-008; the textual specification governs this slice. |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), selected-story evidence rather than foundation smoke tests alone. |

## 10. Open Refinement Notes

**2026-11-07 Product Owner decisions.** Detail returns zero or more distinct Styles (OPEN-S2-08), Brand and Price under the approved validation (OPEN-S2-01A/01B), `recommendationEligibility: NOT_EVALUATED` (X-1), `recordedUse.availability: UNAVAILABLE` without a fabricated count or date (X-5) and no image field (X-3). Selection comes from the paged overview of US-WAR-001 (OPEN-S2-10). See the [decision implementation notes](../../sprints/sprint-02/decision-implementation-notes.md).

**2026-11-05 product revision:** [PRD Section 34.1](../../../02-product/PRD.md#341-approved-taxonomy-revision-and-open-follow-up-decisions) records the final 13-property/vocabulary approval and resolved eligibility deferral; only genuine validation/contract details remain open. Resolve applicable validation before dependent implementation; these questions do not make optional fields mandatory. Historical refinement notes below are subject to this revision. Sprint 2 selection is recorded in the [Sprint plan](../../sprints/sprint-02/sprint-plan.md); source status remains Baseline Draft.

**2026-10-21 refinement draft.** Retain detail as a read-only selected-garment outcome; its existing AC-06/AC-07 already distinguish no recorded use from unavailable evidence. Verify current/changed ownership, minimum-only imageless information, unevaluated eligibility and return to browsing with controlled examples (Q-02/Q-04/Q-05). Neither a completed Wear capability nor a particular creation method is a hard prerequisite. No new reporting, edit, removal or enrichment behavior is proposed.

Question details, owners and conditional planning blockers are centralized in the [focused refinement review](../../backlog-refinement-review.md#6-open-questions-for-product-owner-and-team). These notes do not change story status, assign estimates or authorize implementation. Necessary implementation detail remains JIT for a selected story. The [full story catalogue](../README.md) now accounts for all 41 PBIs; those additional drafts change none of this story's acceptance boundaries.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; document approval is not claimed; Sprint 2 selection is recorded separately in its plan.

