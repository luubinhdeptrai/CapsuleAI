# US-MULT-001 — Evaluate incremental candidate utility with honest result states

## Document Control

| Field | Value |
| --- | --- |
| Artifact | User Story |
| Story ID | US-MULT-001 |
| Version | 0.1 |
| Status | Draft |
| Product | CapsuleAI |
| Parent PBI | [PBI-030](../../product-backlog.md#pbi-030--evaluate-candidate-wardrobe-multiplier-with-honest-result-states) |
| Domain | Wardrobe Multiplier |
| Theme | Strategic Shopping |
| Product Goal Contribution | [Product Goal](../../product-goal.md#4-desired-product-outcomes): Better-Informed Additions |
| Revision note | 2026-10-22: initial source-linked draft; no approval, estimate, Sprint selection or implementation claim. |

## 1. Story

As an authenticated user, I want a defensible count of newly enabled outfits for a hypothetical candidate, so that I can judge its wardrobe utility.

## 2. Value

Give users defensible evidence of the extra outfit utility a hypothetical addition enables.

## 3. Scope

Complete same-basis baseline/expanded valid-set comparison, exact incremental count/reasons and Exact/Evaluated Zero/Incomplete/Unavailable/Outdated handling with reevaluation.

## 4. Preconditions

- An authorized candidate is identified through existing gap/advice context; required credible information, readiness and evaluation availability are checked during the interaction.

## 5. Acceptance Criteria

### AC-01 — Identify the hypothetical basis

Identify current confirmed active owned W, the unowned candidate, applicable readiness and relevant context/rule basis; exclude removed inventory from current inputs. Candidate descriptors require supported source evidence, not invented attributes.

### AC-02 — Compare complete consistent sets

Evaluate O(W) and O(W plus candidate) with the same context, validity/business-rule version and garment-identity uniqueness meaning. Both sets and deduplication must be complete; a daily displayed subset or reordered combination does not establish exact utility.

### AC-03 — Calculate the incremental count

Wardrobe Multiplier is the number of combinations in O(W plus candidate) that are not in O(W), not a weighted score or permutation count. Complete controlled sets with 41 current and 58 expanded unique valid outfits, including the current ones, give +17 New Outfits.

### AC-04 — Show exact only with evidence

Exact +N requires sufficient credible candidate/applicable owned readiness, required context and complete matched evaluations/uniqueness. Explain the evaluated baseline and count without claiming future wear, savings, purchase satisfaction or guaranteed commercial value.

### AC-05 — Reserve zero for completed comparison

Evaluated Zero and +0 are permitted only when every exact prerequisite succeeds and the new-outfit set is empty. No valid current combinations alone does not prove zero candidate utility.

### AC-06 — Distinguish incomplete and unavailable

Missing required information or partial/timed-out/unestablished full evaluation is Incomplete; a capability that cannot operate is Unavailable. Both suppress current exact +N/+0 and unsupported current new-outfit previews while retaining useful candidate/gap context.

### AC-07 — Track changed basis

Relevant owned composition/confirmed attributes, candidate attributes, context or validity/business-rule changes make a prior count Outdated. Do not impose an invented fixed TTL; weather and optional commercial information retain their separate currentness policies.

### AC-08 — Reestablish current truth

Reevaluation uses one current consistent basis and must establish all exact prerequisites again. A failed/incomplete/unavailable reevaluation does not make old counts current; result retrieval failure retains only established status/basis and permits appropriate retry.

### AC-09 — Preserve independent user value

Missing optional commercial fields or a failed destination does not by itself invalidate supported outfit utility. Evaluation/viewing changes no ownership, Wear or purchase state and grants no unauthorized access.

## 6. Quality and Verification

Apply the [shared quality and completion conditions](../README.md#2-shared-quality-and-completion) to this item's actual scope, together with its source-specific criteria below. Relevant automated, real integration, client and other triggered evidence must establish the outcome; drafted AC or backend smoke checks alone do not establish Done.

## 7. Dependencies

- Parent PBI capability inputs: PBI-029, PBI-017.
- Source-linked related stories describe collaborating outcomes; they do not change the Product Backlog's hard-dependency semantics or imply a serial development schedule.

## 8. Out of Scope

Preview inspection is [US-MULT-002](../multiplier/US-MULT-002-inspect-newly-enabled-hypothetical-outfits.md). No enumeration algorithm, numerical runtime limit, new Multiplier latency threshold, candidate import/edit or arbitrary expiry policy is added.

## 9. Traceability

| Source | References |
| --- | --- |
| Parent PBI | [PBI-030](../../product-backlog.md#pbi-030--evaluate-candidate-wardrobe-multiplier-with-honest-result-states); scoped outcome and dependencies |
| Business | [BRD](../../../01-business/BRD.md): BG-04, BR-009, BR-016, BR-022, CAP-09 |
| Product | [PRD](../../../02-product/PRD.md): JRN-07, FEAT-MULT-001 |
| SRS | [SRS](../../../03-requirements/SRS.md): FR-MULT-001–FR-MULT-008; DATA-ANL-003, DATA-ANL-004; DATA-OUT-001; DATA-INT-002, DATA-INT-003; ERR-ANL-001, ERR-ANL-002; ERR-SHOP-001; NFR-REL-002 |
| Business Rules | [Business Rules](../../../03-requirements/business-rules.md): BRULE-MULT-001–BRULE-MULT-009, BRULE-OUT-009, BRULE-SHOP-001 |
| Use Cases | [UC-022](../../../03-requirements/use-cases/UC-022-evaluate-candidate-garment.md); applicable interaction slices / verification anchors only |
| Activity Diagrams | [AD-022](../../../03-requirements/activity-diagrams/AD-022-evaluate-candidate-garment.puml); applicable branches only |
| Completion / Verification | [Definition of Done](../../definition-of-done.md), applicable common/triggered criteria; [Test Strategy](../../../07-testing/test-strategy.md), proportional source-linked evidence. |

## 10. Open Refinement Notes

CAT-Q04 applies to candidate evidence availability. Exactness is a requirement; enumeration, bounded execution and completion evidence mechanisms are JIT design, with no new numerical feasibility limit invented here.

Question ownership and impact: [catalogue questions CAT-Q04](../README.md#4-open-questions-and-readiness). These are unresolved refinements, not new requirement identifiers.

## 11. Status

**Draft.** PO/BA and Developers/QA review is pending. No approval, formal readiness, estimate, Sprint assignment or implemented outcome is claimed. Necessary implementation-level design follows selection and remains Just-In-Time during Sprint execution.
