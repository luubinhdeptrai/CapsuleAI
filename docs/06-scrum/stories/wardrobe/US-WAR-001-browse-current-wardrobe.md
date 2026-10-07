# US-WAR-001 — Browse my current wardrobe

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Version | 0.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-007](../../product-backlog.md#pbi-007--browse-current-wardrobe-and-inspect-owned-garments) |
| Theme | Digital Wardrobe |
| Product Goal Contribution | [Trustworthy Wardrobe Understanding](../../product-goal.md#4-desired-product-outcomes) |

## 1. Story

As an authenticated user, I want to browse my current confirmed wardrobe, so that I can recognize what I own and choose a garment to inspect or start adding one.

## 2. User Value

The collection overview helps me recognize my possessions even before any outfit or reported-use capability is available. Genuine emptiness, loading and unavailable retrieval are outcomes of the same browsing goal, rather than separate stories.

## 3. Scope

Read-only current wardrobe browsing with identifiable summaries, visible imageless/incomplete entries, applicable readiness meaning, loading/empty/unavailable distinctions, Add Garment access and selection of an owned garment for detail.

## 4. Preconditions

- I have authenticated access authorized for my wardrobe.
- An empty wardrobe is permitted; saved garments, completed onboarding, images and reported-use capabilities are not prerequisites.

## 5. Acceptance Criteria

### AC-01 — Recognize current confirmed possessions

**Given** my current wardrobe contains confirmed active garments

**When** I open Wardrobe and retrieval succeeds

**Then** CapsuleAI presents identifiable current owned entries with their confirmed category, available subtype, dominant color and available image/visual. Summaries correspond to the correct garments; missing subtype or image does not conceal an otherwise saved entry.

### AC-02 — Keep imageless and unready garments visible

**Given** my current wardrobe contains an imageless, minimum-confirmed garment that lacks applicable compatibility information

**When** the overview loads successfully

**Then** the garment remains identifiable and selectable through its accepted information. Applicable guidance distinguishes Saveable ownership from Recommendation-Readiness without calling the entry unsaved or requiring an image/rich profile to browse.

### AC-03 — Distinguish loading from loaded emptiness

**Given** authorized wardrobe retrieval has not yet established the available current inventory

**When** I wait for the requested overview

**Then** CapsuleAI identifies loading and does not claim that I own no garments. A pending request is not a completed empty result.

### AC-04 — Understand a genuinely empty wardrobe

**Given** authorized retrieval succeeds and my current confirmed wardrobe has no active garments

**When** the overview is presented

**Then** CapsuleAI explains the confirmed empty state and the value of adding garments, offers Add Garment, and lets me leave without a fixed garment-quota requirement.

### AC-05 — Recover from unavailable retrieval

**Given** wardrobe retrieval fails or usable data cannot be established

**When** I review the overview outcome

**Then** CapsuleAI identifies unavailable data and offers retry or return to usable prior context. It does not replace the failure with an empty wardrobe/no-match claim or delete/change accepted garments; retry can present the actual inventory when retrieval succeeds.

### AC-06 — Start adding without a browsing quota

**Given** I am in a usable empty or populated overview

**When** I choose Add Garment

**Then** CapsuleAI hands off to the established garment-entry goal, including the available manual route. The handoff itself creates no garment and does not impose a quota or optional-profile completion gate; manual creation is covered by US-GAR-001.

### AC-07 — Select the corresponding detail

**Given** a current confirmed entry is presented

**When** I choose it for inspection

**Then** CapsuleAI hands off that garment to US-WAR-002's detail outcome and provides a return path to browsing. Selection alone changes no accepted garment information or ownership.

### AC-08 — Separate current ownership from other representations

**Given** unconfirmed drafts, hypothetical candidates or removed historical snapshots exist alongside accepted inventory

**When** the current overview is loaded

**Then** only confirmed active owned garments are presented as current possessions. A retained historical snapshot does not restore ownership, and hypothetical or unconfirmed information is not treated as an accepted addition; creating/removing those other representations is outside this story.

### AC-09 — Pause browsing when access is unusable

**Given** my access expires, is revoked or is not authorized for the requested wardrobe

**When** I open or continue browsing

**Then** CapsuleAI pauses protected use, gives renewed-authentication guidance and does not expose another account's entries or label inaccessible inventory empty. After usable authorized access is established, I may resume browsing the correct account.

### AC-10 — Preserve accepted state while viewing

**Given** my current wardrobe is available

**When** I browse entries, return from detail or leave the overview without a separate mutating action

**Then** browsing creates no addition, edit, removal, Wear Event or preference feedback and does not change accepted inventory or use evidence.

### AC-11 — Operate and understand the wardrobe overview

**Given** I use the Vietnamese overview with a screen reader or text scaled up to 200%

**When** I identify an entry, open detail, choose Add Garment or follow empty/loading/unavailable guidance

**Then** these entries, actions and states remain operable and understandable through meaningful accessible names/roles/states and information beyond image or color alone; no image, incomplete readiness and unavailable retrieval retain their distinct meanings.

## 6. Business Rules and Constraints

- BRULE-AUTH-001 and BRULE-GAR-001–BRULE-GAR-003 constrain authorized current ownership, confirmed information and retention of Saveable entries despite missing readiness fields.
- BRULE-HIST-002 keeps removed historical meaning separate from active inventory. Where reported-use information is displayed, BRULE-HIST-001 prohibits interpreting no recorded use as physical nonuse; detailed conditional use presentation is covered by US-WAR-002.
- The initial interface and guidance are Vietnamese on Android 10+ and iOS 15+; this specification is English. Story actions and states retain meaningful accessible names, roles and labels beyond color, operability at text scaling up to 200%, and the SRS primary touch-target criteria (approximately 48 dp on Android / 44 pt on iOS). Private information is used only for necessary authorized product purposes; MVP personal data is excluded from AI training/improvement and unrestricted sharing.

## 7. Dependencies

- Parent PBI hard capability dependency: PBI-004's authenticated access. [US-AUTH-002](../auth/US-AUTH-002-log-in.md) and [US-AUTH-003](../auth/US-AUTH-003-resume-session.md) provide entry/continuity where applicable.
- [US-WAR-002](US-WAR-002-inspect-owned-garment.md) supplies selected-garment detail. The overview and empty-state value do not require an existing garment or completion of detail first.
- PBI-006 / [US-GAR-001](../garment/US-GAR-001-add-garment-manually.md) provide useful populated examples and the manual Add Garment destination; PBI-006 is useful sequencing rather than an additional hard dependency for browsing.
- PBI-003 supplies useful common navigation/accessibility support. PBI-008/PBI-015/PBI-016/PBI-025 add separate future capabilities; their completion does not gate basic browsing.

## 8. Out of Scope

Garment creation is PBI-006; the selected profile/detail is US-WAR-002. Rich entry and editing are PBI-008, measurement PBI-009, photo/AI entry PBI-012/PBI-013, search/filter conditions and no-match/reset behavior PBI-015, removal/retention enforcement PBI-016, and utilization analytics PBI-025. This story introduces no sorting, pagination rule, archive/restore, wear-reporting action, new event or universal offline guarantee.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-007](../../product-backlog.md#pbi-007--browse-current-wardrobe-and-inspect-owned-garments) |
| Business | [BRD](../../../01-business/BRD.md): BR-003, BR-005, BR-021, BR-022; CAP-03, CAP-04 |
| Product | [PRD](../../../02-product/PRD.md): JRN-03; FEAT-WAR-001 (overview and entry/return paths) |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-WAR-001, FR-WAR-008 (empty/loading/unavailable only), FR-GAR-004, FR-AUTH-008; DATA-GAR-001, DATA-GAR-004, DATA-GAR-005; UI-001, UI-003, UI-004; ERR-AUTH-001, ERR-NET-001; NFR-ACC-002; LOC-001 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-AUTH-001, BRULE-AUTH-007; BRULE-GAR-001, BRULE-GAR-002, BRULE-GAR-003; BRULE-HIST-002 |
| Use Cases | [UC-008 — View Wardrobe](../../../03-requirements/use-cases/UC-008-view-wardrobe.md), browsing/empty/retrieval/access branches; [UC-007 — Add Garment](../../../03-requirements/use-cases/UC-007-add-garment.md), entry handoff only |
| Activity Diagrams | No current dedicated Activity Diagram for UC-008; the textual specification governs this slice. |

## 10. Open Refinement Notes

None currently identified from the authoritative baseline.

## 11. Status

**Baseline Draft.** Product Owner and Scrum Team review is pending; no approval or Sprint commitment is recorded.

