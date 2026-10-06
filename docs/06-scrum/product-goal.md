# CapsuleAI Product Goal

## Document Control

| Field | Value |
| --- | --- |
| Artifact | Scrum Product Goal |
| Version | 0.1.1 |
| Status | Baseline Draft |
| Product | CapsuleAI |
| Owner / Accountability | Product Owner |

## 1. Purpose

This artifact defines one current Product Goal to guide Product Backlog creation and ordering, User Story refinement, Sprint planning, and product trade-offs. It spans multiple Sprints toward the connected MVP and derives from the existing business, product, and requirements baseline. The [Scrum development workflow](../../Initial%20files/CapsuleAI_Scrum_Development_Workflow.md), Sections 12–14 and 46, governs its place in the delivery sequence.

## 2. Product Context

CapsuleAI primarily serves the **Indecisive Professional**, whose outfit choices require effort despite owning suitable clothing. The **Fashion-Conscious Minimalist** and **Smart Shopper** share the need to understand wardrobe usefulness and assess additions. Its Vietnam-first mobile experience connects **AI Digital Closet**, **Context-Aware Styling**, and **Wardrobe Intelligence & Strategic Shopping**: trustworthy wardrobe information supports daily decisions, reported use informs personalization, and wardrobe needs guide potential improvements.

## 3. Product Goal

Enable Indecisive Professionals in Vietnam to understand their wardrobe with confidence, make daily outfit decisions with less effort, and gain more useful wear from owned garments. CapsuleAI connects trustworthy wardrobe understanding, personalized guidance that learns from use, and explainable wardrobe intelligence in one mobile experience so users can recognize underserved needs and judge potential additions by the new valid outfit combinations they unlock.

## 4. Desired Product Outcomes

- **Easier wardrobe understanding:** Users establish and maintain a useful digital representation of their clothing with less repetitive entry and fewer avoidable corrections, while retaining authority over garment information.
- **Easier daily decisions:** Users choose personally relevant, context-appropriate outfits from valid combinations of owned garments with less selection effort.
- **More value from owned clothing:** Users discover useful combinations and overlooked garments; recorded use and meaningful feedback improve visibility, relevance, and variety over time.
- **Actionable wardrobe needs:** Users understand how their wardrobe supports their Personalized Everyday Capsule and which capabilities are underserved.
- **Better-informed additions:** Users judge hypothetical candidates by newly enabled unique valid outfits and understandable previews, without pressure to purchase.

## 5. Product Value Loop

The experience follows **Understand My Wardrobe → Use My Wardrobe Better → Improve My Wardrobe**, with feedback returning into personalization.

| Stage | Product value |
| --- | --- |
| Understand My Wardrobe | AI-assisted entry, review, correction, and ongoing management establish trustworthy knowledge of owned garments. |
| Use My Wardrobe Better | Valid personalized choices help users dress for their context; reported wear and utilization insights reveal useful combinations and overlooked clothing. |
| Improve My Wardrobe | Coverage and Gaps make underserved needs understandable; candidate evaluation and Wardrobe Multiplier show whether an addition would expand useful outfit options. Improvement can also mean rediscovering owned clothing. |

Explicit Like/Dislike feedback and user-reported Wear Events inform lightweight personalization and future wardrobe-use insights. A newly acquired garment enters the loop through a separate user-confirmed wardrobe addition.

## 6. Goal Boundaries

- The current horizon is a private, mobile-first wardrobe-intelligence MVP for Vietnam, centered on the user's owned wardrobe, with existing privacy and user-control protections.
- AI assists wardrobe understanding; user-confirmed or corrected information remains authoritative, and manual entry preserves continuity.
- Outfit recommendations and wardrobe insights explain their basis and limitations, disclosing insufficient evidence without fabricated certainty.
- Strategic Shopping remains advisory, with optional external navigation. Core wardrobe value is independent of purchases, shopping links, retailer contracts, and affiliates; native commerce is outside this goal.
- Social networking, public wardrobe sharing, and AR/3D remain outside the current horizon.

## 7. Evidence of Progress

Evaluate progress through the existing directions in BRD Section 19 and PRD Section 24, at their established measurement stages. These are intended validation directions; observed benefits have not been established. Numerical targets and quality acceptance conditions remain in their upstream documents.

| Evidence area | Existing evidence direction | BRD references |
| --- | --- | --- |
| Wardrobe digitization | Confirmed additions, time to a usable wardrobe, and avoidable correction burden; preserve manual continuity and user authority. | MET-E01, MET-E05, MET-Q01, MET-Q05 |
| Recommendation usefulness | Recommendation use and reported time/effort to selection, supported by distinct valid choices. | MET-E02, MET-Q04 |
| Wear and feedback | Meaningful Wear This Today participation and reported events, with Like/Dislike analyzed separately and retries excluded from event inflation. | MET-E03 |
| Owned-garment utilization | Recorded use across owned garments and outfit diversity, interpreted with incomplete logging in mind. | MET-E04 |
| Coverage, Gaps, and candidates | Explanation/detail engagement, hypothetical-preview use, and defensible Coverage and incremental-outfit explanations. | MET-E06, MET-Q07 |
| Repeat engagement | Cohort baselines and return behavior across the loop, without introducing a growth target. | MET-E07 |

Existing privacy, reliability, usability, accessibility, and performance expectations remain necessary to deliver these outcomes. Optional shopping-link engagement is supplemental evidence under MET-C01; a click does not establish purchase or wardrobe improvement.

## 8. Product Backlog Alignment

The Product Owner should order and refine future Product Backlog Items by their contribution to trustworthy wardrobe understanding, better wardrobe use, meaningful personalization, actionable gaps, or higher-utility additions. Necessary quality, security, and reliability work should identify the outcome it enables or protects; its contribution may be indirect.

Refinement should make that contribution and the relevant upstream evidence clear. Items without a defensible contribution should be challenged or deprioritized, and proposed scope changes must follow upstream change control. Sprint Goals can select coherent steps toward this Product Goal as the ordered backlog evolves.

## 9. Traceability

Business identifiers refer to [BRD v0.3](../01-business/BRD.md); journey and feature identifiers refer to [PRD v0.2](../02-product/PRD.md). Representative links below connect the single goal to existing intent and product behavior.

| Goal contribution | Business goals / requirements | Capabilities | Major PRD journeys / features |
| --- | --- | --- | --- |
| Trustworthy wardrobe understanding | BG-01; BR-001, BR-002, BR-004, BR-005 | CAP-02–CAP-04 | JRN-02, JRN-03; FEAT-AI-002, FEAT-WAR-001 |
| Easier valid outfit decisions | BG-02; BR-007, BR-009, BR-010 | CAP-05 | JRN-04; FEAT-OUT-001 |
| Learning and greater owned-clothing value | BG-03; BR-006, BR-011 | CAP-06, CAP-07 | JRN-05; FEAT-PERS-003, FEAT-ANL-001 |
| Understand needs and assess additions | BG-03, BG-04; BR-014–BR-018, BR-024 | CAP-07–CAP-10 | JRN-06, JRN-07; FEAT-ANL-002, FEAT-GAP-001, FEAT-MULT-001, FEAT-SHOP-001 |
| Personal context and trust across the loop | BG-01–BG-04; BR-012, BR-013, BR-021–BR-023 | CAP-01 | JRN-01; FEAT-PROF-001, FEAT-PROF-002; Sections 21–25 |

[SRS v0.3](../03-requirements/SRS.md), [Business Rules v0.1.2](../03-requirements/business-rules.md), the [master Use Case Diagram](../03-requirements/use-cases/use-case-diagram.puml), all 23 [Use Case specifications](../03-requirements/use-cases/), and all 12 current [Activity Diagrams](../03-requirements/activity-diagrams/) provide the behavioral and quality constraints behind this summary. They retain their source authority; this goal does not replace their detailed requirements or rules.

## 10. Current Status

**Baseline Draft** for Product Owner and Scrum Team review. This goal will guide initial Product Backlog creation and refinement. The next intended Scrum artifact is the initial Product Backlog after Product Goal review, following the workflow sequence. Formal approval is not claimed.
