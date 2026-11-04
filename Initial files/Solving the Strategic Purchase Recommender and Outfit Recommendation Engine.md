# Solving the Strategic Purchase Recommender and Outfit Recommendation Engine	

## 1\. Summary

The app needs two related engines. The **Outfit Recommendation Engine** ranks combinations of garments the user already owns. The **Strategic Purchase Recommender** chooses which new garments to buy so that the number and quality of good outfits grows the most. Both depend on the same core component: a function that scores how well a set of garments goes together.

Recommended approach:

- Build one shared compatibility scorer, c(outfit), behind a stable interface, and run both engines on top of it.  
- **Purchase engine:** follow Hsiao & Grauman’s capsule-wardrobe formulation (compatibility plus versatility, chosen by greedy marginal gain). It is the only one of the five papers that treats “which items to add” as the problem.  
- **Outfit engine:** score and rank closet combinations with an outfit-level model. OCPHN has the strongest reported outfit-level results of the five, so it is the leading candidate scorer. Test it against Hsiao’s attribute-based scorer in a bake-off before committing.  
- **Personalization:** start with the closet-derived style profile from Hsiao, then add a PCE-Net-style learned preference term once users produce feedback (saves, wears, dismissals).  
- Use Yang (2022) and Abugabah (2020) sparingly: Yang for attribute-vocabulary ideas and simple rule filters, and Abugabah’s lineage (VBPR) only once you have purchase or click logs.

Three evidence caveats shape the plan. First, Hsiao’s compatibility scorer is only modestly above chance by its own metric. Second, OCPHN’s headline numbers use randomly built negative outfits. Third, Polyvore-style training data was released for research, and its commercial terms are unverified. Section 6 covers these.

**How to read this report:** statements marked “(paper)” are reported by the cited paper. Statements marked “(inference)” or “(assumption)” are my own reasoning, arithmetic, or guesses, and should be tested rather than trusted.

## 2\. Problem definitions and app context

### 2.1 Outfit Recommendation Engine

Input: the user’s tagged closet, plus optional context such as weather or occasion. Output: a ranked list of outfits (sets of closet items, for example top \+ bottom \+ shoes, optionally with outerwear or an accessory), each with a score and a short reason. A useful variant is completion: given a partial outfit, suggest the closet item that finishes it best.

### 2.2 Strategic Purchase Recommender

Input: the closet, a candidate catalog (for example affiliate products), and constraints such as budget, category, and size. Output: a short ranked list of items to buy, each with the new good outfits it unlocks and the wardrobe gap it fills. The stated goal, maximizing outfit combinations and looks, is the capsule-wardrobe objective: more compatible and more varied outfits from fewer pieces.

### 2.3 App context assumed in this report

- Garments are captured by photo, background-removed, and tagged with type, color, pattern, and season. The user can confirm or edit tags.  
- Inference cost per garment must stay low.  
- At launch there is little or no interaction history, so personalization starts cold. (assumption)  
- Per-user closets are small, tens of items, so enumerating outfits is cheap. (assumption)  
- Catalog items for purchase can be run through the same tagger. (assumption)

Because tags feed the scorer, tagging errors flow straight into recommendations. The tag-confirmation screen is therefore part of recommendation quality, not only UX.

## 3\. What each paper contributes

### 3.1 Hsiao & Grauman, “Creating Capsule Wardrobes from Fashion Images” (CVPR 2018\)

**Role:** purchase-engine core; attribute-based scorer candidate.

What it does (paper):

- Casts capsule creation as subset selection. Pieces are picked per layer (outerwear, upper body, lower body, one-piece), and the outfits are the Cartesian product of the chosen pieces. The goal is a set that is maximally compatible and versatile.  
- Objective: y\* \= argmax C(y) \+ V(y). C(y) is the sum of outfit compatibility scores. V(y) is a coverage term over K styles: for each style, 1 minus the product of (1 − P(style \| outfit)) over the outfits. A personalized version weights each style by the user’s preference.  
- Greedy maximization of a submodular function is near-optimal (at least about 63% of the optimum). Adding one garment adds many outfits, so the objective is not submodular per garment. The paper shows it is submodular when the other layers are fixed, so it selects one layer at a time and iterates until convergence.  
- Compatibility is learned without labeled pairs: a Correlated Topic Model over predicted attributes of 3,957 “in the wild” full-body outfit photos from Chictopia. An outfit’s likelihood is its compatibility, and topics act as styles. A user’s style profile is the average topic distribution of the user’s own outfits.  
- Attributes come from a ResNet-50 recognizer over a 195-attribute vocabulary, trained in two stages from catalog images to street images (a 15% mAP gain on the outfit-image validation split).

Evidence (paper):

- Toy scale (10 candidates, 3 picks per layer): iterative greedy reaches 87% of the optimal objective versus 76% for naive greedy (Table 3).  
- At 150 candidates per layer, brute force is estimated at about a billion hours per capsule, versus about 200 seconds for their method on one 2.66 GHz Xeon. Most of that time is topic-model inference.  
- Capsules from the iterative method come closest to human-curated Polyvore outfits on compatibility. MMR baselines score better on versatility but ignore the seed wardrobe (Table 2 and text).  
- Human study: 59% preferred iterative over naive-greedy capsules. There were 14 subjects, all female, and 50 pairs.  
- Compatibility accuracy, average precision: 0.199 for the proposed scorer versus 0.184 BiLSTM, 0.177 PolyLDA, 0.157 Monomer, and 0.148 LDA (Fig. 5).

Caveats:

- (inference) The compatibility test set has 2,574 positives and 12,870 negatives, so a random ranker would score about 0.167 average precision. The scorer’s margin over chance is small, and two baselines fall below chance. The selection algorithm’s evidence is stronger than the scorer’s.  
- Women’s clothing only; candidates come from Polyvore; personalization is a qualitative proof of concept on two users.  
- The scorer works on predicted attributes, so it can only be as good as the attributes. Its own qualitative finding is that top-scoring outfits favor neutral staples and the worst ones show clashing palettes or mismatched fit and cut.

### 3.2 Li et al., OCPHN: Outfit Compatibility Prediction with Hypergraph Networks (Mathematics, 2022\)

**Role:** outfit-scorer candidate; completion (“fill in the blank”).

What it does (paper): Item categories are nodes and each outfit is a hyperedge. Each hyperedge is reduced to a simple graph built around its two most different items. A graph convolution propagates messages, and an attention mechanism produces the compatibility score. Node inputs are CNN visual features plus a learned category feature. Training uses a BPR loss against randomly generated negative outfits.

Evidence (paper): On Polyvore-N (20,871 outfits, 130,901 items, 120 categories), fill-in-the-blank accuracy is 79.24% and compatibility AUC is 97.89%. Baselines: NGNN 75.30% / 96.03%, GGNN 74.19% / 94.77%, VCP 60.59% / 93.82%, Bi-LSTM 46.26% / 77.11% (Table 2). The ablation without both the weight-matrix and hypergraph components gives 76.71% / 96.42% (Table 3). The best reported embedding dimension is 16\.

Caveats:

- Negative outfits are randomly assembled (Sec. 5.3.2), and fill-in-the-blank distractors are random items from other outfits. (inference) The scores show it separates real outfits from random mixes, not that it ranks two plausible outfits correctly.  
- It is not personalized. The authors list user-specific compatibility as future work.  
- The released code is dated: per the repository page, it is TensorFlow 1.15 on Python 3.7, with five commits and no releases. (inference) Plan to reimplement it; the model is small.  
- It is trained on Polyvore data, whose commercial use is unverified (Section 6).

### 3.3 Nie et al., PCE-Net (Applied Sciences, 2023\)

**Role:** personalization layer; later phase.

What it does (paper): It jointly learns item–item compatibility embeddings (with visual and text attention branches) and user–item preference (matrix factorization plus content-based features), trained with BPR on quadruplets: a user, a top, the bottom the user preferred, and another bottom. The dataset is IQON3000: 216,791 outfits, 3,568 users, 650,373 items.

Evidence (paper): AUC 0.8534, versus 0.8502 for PAI-BPR and 0.8314 for GP-BPR (Table 1). Ablation: the personal-preference component alone reaches 0.8376, and the compatibility component alone reaches 0.6982 (Table 4). A 100-participant user study is also reported.

Caveats:

- The margin over PAI-BPR is small, and I found no variance reporting in the text.  
- It covers tops and bottoms only, and the authors state they model preference only for bottoms.  
- It needs user histories. The IQON site has closed, so the authors reused text features supplied by an earlier paper.  
- (inference) The ablation suggests personal history can matter more than generic compatibility, but that is one dataset where the positive item is the user’s own pick.

### 3.4 Abugabah et al., “Learning Context-Aware Outfit Recommendation” (Symmetry, 2020\)

**Role:** later-stage purchase ranking from logs; low priority now.

What it does (paper): It extends visual BPR (VBPR) with a sequential, dynamic-preference term and EfficientNet-B7 image features to predict items users will interact with. It is evaluated by AUC@10 and AUC@20 on Amazon (women, men, phones) and Epinions data, 267,683 users and 790,438 items in total.

Evidence (paper): Amazon Women AUC@10 is 0.7516 versus 0.7284 for the best baseline (CIGAR). Convergence on the women’s dress data takes about 5 hours, longer than BPR-MF and CIGAR. Outfit generation is shown only through example figures.

Caveats:

- It needs large interaction logs, which the app will not have at launch.  
- The equations and Algorithm 1 are hard to follow. For example, Algorithm 1 refers to compatibility between a feature vector and itself.  
- “AUC@k” is a nonstandard metric, and one of the four datasets is phones.  
- (inference) If you adopt this lineage, implement from the original VBPR paper (He & McAuley, AAAI 2016\) rather than from this paper.

### 3.5 Yang, “Clothing Design Style Recommendation Using Decision Tree Algorithm Combined with Deep Learning” (2022)

**Role:** attribute-vocabulary reference; optional rule layer.

What it does (paper): A multilabel neural classifier recognizes clothing style and attributes (35 labels across basic and advanced clothing type, season, collar, looseness, and color). A three-layer decision tree maps user → gender → six preference attributes (age, collar, profile, sleeve, color, fabric) for an initial recommendation.

Evidence (paper): Classifier precision 0.73, recall 0.43, F1 0.55. Twenty users rated 30 recommendations each, for an average satisfaction of 86.25%.

Caveats: The satisfaction test has no baseline, and recall is low. The tree filters by user attributes; it does not score whether garments go together. The paper itself notes that style definitions are subjective. It is useful as a label-taxonomy cross-check and for simple cold-start rules such as gender and season filters, not as the compatibility engine.

## 4\. Recommended solution

### 4.1 Architecture

Pipeline: photo → tagger (type, color, pattern, season, plus an image embedding) → item record → compatibility scorer c(outfit) → the two engines → personalization → UI with explanations.

Keep the scorer behind one interface. Input: a set of item records with layers or categories. Output: a score in a fixed range. Both engines call only this interface, so scorers can be swapped without touching engine logic.

### 4.2 Item representation

Store, for each garment: category or layer, tagged attributes with confidence, an image embedding, and whether the user confirmed the tags. (inference) Carry tag confidence into the scorer, or down-weight unconfirmed items, so tagger mistakes do not dominate recommendations.

### 4.3 Compatibility scorer: two candidates and a bake-off

**Option A, attribute topic model (Hsiao).** It needs no labeled outfit pairs, works directly on tagger’s attributes, and yields style distributions that power the versatility term and user style profiles. Weakness: modest measured accuracy.

**Option B, OCPHN-style graph scorer.** It has the strongest reported outfit-level results, uses image features and categories, but needs labeled outfit data to train and has no personalization.

Bake-off procedure (inference):

1. Score the same held-out outfits with both options.  
2. Use hard negatives: swap one item within the same layer for a plausible but incongruent one, as Hsiao does with meta-labels, instead of random items.  
3. Report AUC and AP next to the chance-level AP.  
4. Add a human check on a few hundred pairs.

### 4.4 Outfit Recommendation Engine

1. **Generate candidates** from layer templates (top \+ bottom \+ shoes; dress \+ shoes; optional outerwear or accessory). Closets are small: 10 tops × 8 bottoms × 4 shoes × 3 outerwear is 960 combinations, cheap to score (illustrative arithmetic).  
2. **Apply hard rules first:** season tag versus weather, occasion filter if given, no duplicate categories.  
3. **Score** with c(outfit), and add the personal term once available: s(u, o) \= c(o) \+ λ · pref(u, o).  
4. **Re-rank for variety** so the top results do not all share the same hero piece. (inference)  
5. **Explain** with attribute-level reasons such as a neutral staple anchoring the look or a harmonious palette. Hsiao reports these patterns in its best-scoring outfits.  
6. **Completion mode:** given a partial outfit, rank closet items by the score of the resulting outfit. This is the fill-in-the-blank task OCPHN evaluates.

### 4.5 Strategic Purchase Recommender

Use Hsiao’s objective with the user’s closet as the fixed seed. A candidate’s value has three parts:

- **Compatibility gain:** the scores of the new outfits formed by the candidate with owned items.  
- **Versatility gain:** new coverage of styles or, as a simpler substitute, occasion, season, and category slots the closet lacks. (inference)  
- **Personalization:** weight styles by the user’s profile (Hsiao’s per-style weights).

Selection for k items, with k small:

1. Build the candidate pool from the catalog after hard filters: budget, category, size, availability.  
2. For each candidate, form only its new outfits (the candidate combined with the other layers of the closet), score them with c, and compute the marginal gain δ(x \| closet \+ items already chosen).  
3. Pick the highest-gain candidate, add it to the working closet, and repeat k times.  
4. Report each pick with the number of acceptable new outfits it unlocks and the gap it fills.

Notes:

- Hsiao’s near-optimality guarantee applies to submodular objectives. Because adding a garment adds outfits superlinearly, the paper selects one layer at a time with the others fixed. (inference) For k of 3 or fewer against a fixed closet, direct greedy over items is simple and cheap. For larger joint recommendations across layers, use the layer-wise iterative scheme.  
- Summing raw scores rewards many mediocre outfits. Counting only the score above a threshold, the sum of max(0, c(o) − τ), avoids padding. This is my variant (inference); tune τ in the bake-off.  
- Cost: only new outfits containing the candidate need scoring, and c(o) can be cached for repeated closet combinations. Hsiao reports about 200 seconds for a full capsule at 150 candidates per layer. Your problem is smaller, but benchmark it.  
- Cold-start baseline: recommend neutral staples that fill empty layers or slots, since Hsiao finds staples dominate compatible outfits.  
- Rank by wardrobe gain before applying any commercial weighting such as affiliate payout, and keep any such weighting visible to the user.

### 4.6 Personalization in phases

- **Phase 0, cold start:** a style profile from the closet (average style distribution) plus an optional onboarding choice of liked styles. Simple rule filters in the spirit of Yang (gender, season, age) can be added as filters, not as scores.  
- **Phase 1, feedback:** log saved, worn, and dismissed outfits. Train a PCE-Net-style preference term (user latent factors plus content-based features) with BPR on pairs of the form “user preferred outfit A over outfit B for the same anchor item.” PCE-Net’s ablation suggests this term can carry much of the signal.  
- **Phase 2, purchase signals:** once you have clicks and purchases, add a VBPR-style visual ranking to pre-rank catalog candidates before the marginal-gain step.

## 5\. Evaluation plan

- **Scorer:** held-out outfits with hard negatives; AUC and AP with the chance baseline shown; completion accuracy with same-category distractors, which test taste rather than category completion (inference).  
- **Outfit engine:** human pairwise ratings of top-k outputs against a staples-and-rules baseline; diversity of the top-k; latency per request.  
- **Purchase engine:** a leave-out simulation (inference): take curated outfit sets, hide items, and check whether the recommended items restore compatible outfits. Add stylist or user ratings, and compare against baselines: neutral staples, popularity, and an MMR-style diversity baseline as in Hsiao.  
- **Cold start:** evaluate at closet sizes of about 5, 10, 20, and 40 items.  
- **Online:** tag-confirmation rate, outfit save and wear rates, dismissals, purchase-recommendation click-through, and post-purchase wear.  
- **Human-study design:** prior studies are small and narrow (14 female subjects in Hsiao, 20 users in Yang). Recruit across genders, ages, and style preferences, and report sample sizes and confidence intervals.

## 6\. Risks and open issues

- **Scorer quality.** Hsiao’s scorer is near chance by my arithmetic, and OCPHN’s numbers come from easy random negatives. Do not treat either paper’s headline number as the quality you will get.  
- **Data licensing.** Han et al. (2017), whose Bi-LSTM OCPHN compares against, describe their Polyvore dataset as released for research purposes. I could not confirm commercial terms, and IQON3000’s source site has closed. Check licenses before training any shipped model on these datasets, or plan to collect your own labeled outfits.  
- **Code age.** OCPHN’s reference code targets TensorFlow 1.15 and Python 3.7. Budget for a reimplementation.  
- **Tagger error propagation.** Hsiao’s scorer runs on predicted attributes, so tag errors change scores. Use confirmed tags and confidence.  
- **Subjectivity and drift.** Yang notes there is no unified definition of clothing style, and Abugabah argues preferences change with context and time. Plan for the feedback loop and context features such as weather and occasion.  
- **Coverage gaps.** Hsiao’s data and study cover women’s clothing, and PCE-Net covers tops and bottoms only. Body type and fit are not modeled in these papers; PCE-Net’s related-work section points to a personalized capsule framework (PCW-DC) that includes body type, which is a lead worth reading.  
- **Domain shift.** Phone photos differ from catalog images. Hsiao’s cross-domain training suggests this matters, so test the scorer on both.  
- **Commercial incentives.** Affiliate revenue can bias purchase ranking. Keep the gain-based ranking independent and disclose any commercial weighting.

## 7\. Roadmap

**Phase A, foundations.** Fix the attribute vocabulary and layer templates, define the scorer interface, build the evaluation harness with hard negatives and chance baselines, and ship a rules-and-staples baseline. Exit: baseline numbers recorded.

**Phase B, scorer bake-off.** Compare Option A and Option B (retrained on data you are allowed to use) and pick one. Exit: the chosen scorer beats the baseline on hard negatives and on human pairs.

**Phase C, engines v1.** Outfit ranking with explanations, and the purchase engine with marginal-gain selection. Exit: human ratings beat the baselines and latency is acceptable.

**Phase D, personalization.** Feedback logging and a PCE-Net-style preference term. Exit: personalized ranking beats unpersonalized on held-out feedback.

**Phase E, purchase signals.** VBPR-style pre-ranking of catalog candidates once click and purchase logs exist.

## 8\. References

1. W.-L. Hsiao and K. Grauman, “Creating Capsule Wardrobes from Fashion Images,” CVPR 2018\. [https\://arxiv.org/abs/1712.02662](https://arxiv.org/abs/1712.02662)  
2. Z. Li, J. Li, T. Wang, X. Gong, Y. Wei, P. Luo, “OCPHN: Outfit Compatibility Prediction with Hypergraph Networks,” Mathematics 10(20):3913, 2022\. [https\://doi.org/10.3390/math10203913](https://doi.org/10.3390/math10203913) — code: [https\://github.com/outfit-net/outfit-commpatibility](https://github.com/outfit-net/outfit-commpatibility)  
3. X. Nie, Z. Xu, J. Zhang, Y. Tian, “Attention-Based Personalized Compatibility Learning for Fashion Matching,” Applied Sciences 13(17):9638, 2023\. [https\://doi.org/10.3390/app13179638](https://doi.org/10.3390/app13179638)  
4. A. Abugabah, X. Cheng, J. Wang, “Learning Context-Aware Outfit Recommendation,” Symmetry 12(6):873, 2020\. [https\://doi.org/10.3390/sym12060873](https://doi.org/10.3390/sym12060873)  
5. B. Yang, “Clothing Design Style Recommendation Using Decision Tree Algorithm Combined with Deep Learning,” Computational Intelligence and Neuroscience, 2022, Article ID 5745457\. [https\://doi.org/10.1155/2022/5745457](https://doi.org/10.1155/2022/5745457)  
6. X. Han, Z. Wu, Y.-G. Jiang, L. S. Davis, “Learning Fashion Compatibility with Bidirectional LSTMs,” ACM Multimedia 2017\. [https\://arxiv.org/pdf/1707.05691](https://arxiv.org/pdf/1707.05691)  
7. R. He and J. McAuley, “VBPR: Visual Bayesian Personalized Ranking from Implicit Feedback,” AAAI 2016, pp. 144–150 (cited as reference 18 in Abugabah et al.; not independently checked).

