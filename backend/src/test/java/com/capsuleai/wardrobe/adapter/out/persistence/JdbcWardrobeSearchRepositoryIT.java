package com.capsuleai.wardrobe.adapter.out.persistence;

import com.capsuleai.wardrobe.application.DescriptorMatcher;
import com.capsuleai.wardrobe.application.WardrobeSearchCriteria;
import com.capsuleai.wardrobe.application.WardrobeSearchQuery;
import com.capsuleai.wardrobe.application.ports.WardrobePosition;
import com.capsuleai.wardrobe.application.ports.WardrobeSearchRepository;
import com.capsuleai.wardrobe.domain.Garment;
import com.capsuleai.wardrobe.domain.GarmentCategory;
import com.capsuleai.wardrobe.domain.GarmentColor;
import com.capsuleai.wardrobe.support.WardrobeIntegrationTestSupport;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * US-WAR-003 matching on real PostgreSQL (S4-WAR-01/02, OPEN-S4-11/12/13): descriptor and Brand semantics,
 * literal LIKE metacharacters, filter combination, owner/current isolation, exclusions and keyset paging.
 * Run against the deployed-compatible database settings; a Java substring is not evidence of SQL semantics.
 */
class JdbcWardrobeSearchRepositoryIT extends WardrobeIntegrationTestSupport {

    @Autowired
    private WardrobeSearchRepository wardrobeSearchRepository;

    @Test
    void aGarmentWithSeveralMatchingMaterialsIsReturnedOnce() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-once@example.com");
        UUID matching = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "WHITE", "materials", List.of("COTTON", "CORDUROY")));
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));

        assertThat(matchingIds(tokens.accountId(), query("co", Set.of(), Set.of()))).containsExactly(matching);
    }

    @Test
    void aGarmentIsFoundThroughAStyleOrAMaterialAloneAndOnlyInTheOwnersWardrobe() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-child-sets@example.com");
        IssuedTokens other = registerAndLogIn("search-child-sets-other@example.com");
        UUID minimalist = addGarmentAndReturnId(tokens.accessToken(),
                garment("SHOES", "BLACK", "styles", List.of("CASUAL", "MINIMALIST")));
        UUID linen = addGarmentAndReturnId(tokens.accessToken(),
                garment("PANTS", "BEIGE", "materials", List.of("COTTON", "LINEN")));
        addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "RED"));
        addGarmentAndReturnId(other.accessToken(), garment("SHOES", "BLACK", "styles", List.of("MINIMALIST")));
        UUID owner = tokens.accountId();

        assertThat(matchingIds(owner, query("minimalist", Set.of(), Set.of()))).containsExactly(minimalist);
        assertThat(matchingIds(owner, query("linen", Set.of(), Set.of()))).containsExactly(linen);
        assertThat(matchingIds(owner, query("linen", Set.of(GarmentCategory.SHOES), Set.of())))
                .as("a Material match is still narrowed by the filters").isEmpty();
    }

    @Test
    void brandMatchingIsCaseInsensitiveAndAccentSensitive() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-vietnamese@example.com");
        UUID dress = addGarmentAndReturnId(tokens.accessToken(),
                garment("DRESSES_JUMPSUITS", "RED", "brand", "Đầm Xinh"));

        assertThat(matchingIds(tokens.accountId(), query("đầm", Set.of(), Set.of()))).containsExactly(dress);
        assertThat(matchingIds(tokens.accountId(), query("ĐẦM XINH", Set.of(), Set.of()))).containsExactly(dress);
        assertThat(matchingIds(tokens.accountId(), query("Đầm", Set.of(), Set.of())))
                .as("decomposed input is composed before matching").containsExactly(dress);
        assertThat(matchingIds(tokens.accountId(), query("dam", Set.of(), Set.of())))
                .as("accents are significant").isEmpty();
    }

    @Test
    void vietnameseDescriptorAliasesFindGarmentsThroughTheirStoredCodes() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-vietnamese-alias@example.com");
        UUID whiteTShirt = addGarmentAndReturnId(tokens.accessToken(),
                garment("TOPS", "WHITE", "subtype", "T_SHIRTS"));
        UUID blackSkirt = addGarmentAndReturnId(tokens.accessToken(),
                garment("SKIRTS", "BLACK", "subtype", "MINI_SKIRTS"));
        UUID redDress = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("DRESSES_JUMPSUITS", "RED"));
        UUID owner = tokens.accountId();

        assertThat(matchingIds(owner, query("áo thun", Set.of(), Set.of()))).containsExactly(whiteTShirt);
        assertThat(matchingIds(owner, query("ÁO PHÔNG", Set.of(), Set.of()))).containsExactly(whiteTShirt);
        assertThat(matchingIds(owner, query("trắng", Set.of(), Set.of()))).containsExactly(whiteTShirt);
        assertThat(matchingIds(owner, query("chân váy", Set.of(), Set.of()))).containsExactly(blackSkirt);
        assertThat(matchingIds(owner, query("váy", Set.of(), Set.of()))).as("ambiguous word: OR of both meanings")
                .containsExactlyInAnyOrder(blackSkirt, redDress);
        assertThat(matchingIds(owner, query("váy", Set.of(GarmentCategory.SKIRTS), Set.of())))
                .as("the code filter still narrows with AND").containsExactly(blackSkirt);
        assertThat(matchingIds(owner, query("ao thun", Set.of(), Set.of()))).as("accents are significant").isEmpty();
    }

    @Test
    void likeMetacharactersInTheTextAreLiteral() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-literal@example.com");
        UUID percent = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "100% Organic"));
        UUID underscore = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "A_B Studio"));
        UUID backslash = addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "Back\\Slash"));
        addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "NAVY", "brand", "Plain"));

        assertThat(matchingIds(tokens.accountId(), query("%", Set.of(), Set.of()))).containsExactly(percent);
        assertThat(matchingIds(tokens.accountId(), query("_", Set.of(), Set.of()))).containsExactly(underscore);
        assertThat(matchingIds(tokens.accountId(), query("\\", Set.of(), Set.of()))).containsExactly(backslash);
        assertThat(matchingIds(tokens.accountId(), query("0% o", Set.of(), Set.of()))).containsExactly(percent);
    }

    @Test
    void categoryAndColorFiltersAreOrWithinAGroupAndAndAcrossGroups() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-filters@example.com");
        UUID navyTop = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        UUID redTop = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "RED"));
        UUID navyPants = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "NAVY"));
        UUID blackShoes = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "BLACK"));
        UUID owner = tokens.accountId();

        assertThat(matchingIds(owner, query(null, Set.of(GarmentCategory.TOPS, GarmentCategory.PANTS),
                Set.of(GarmentColor.NAVY)))).containsExactlyInAnyOrder(navyTop, navyPants);
        assertThat(matchingIds(owner, query(null, Set.of(GarmentCategory.TOPS), Set.of())))
                .containsExactlyInAnyOrder(navyTop, redTop);
        assertThat(matchingIds(owner, query(null, Set.of(), Set.of(GarmentColor.NAVY, GarmentColor.BLACK))))
                .containsExactlyInAnyOrder(navyTop, navyPants, blackShoes);
        assertThat(matchingIds(owner, query("navy", Set.of(GarmentCategory.PANTS), Set.of())))
                .containsExactly(navyPants);
    }

    @Test
    void removedAndOtherOwnersGarmentsNeverMatch() throws Exception {
        IssuedTokens owner = registerAndLogIn("search-owner@example.com");
        IssuedTokens other = registerAndLogIn("search-other@example.com");
        UUID removed = addGarmentAndReturnId(owner.accessToken(), minimumGarment("TOPS", "NAVY"));
        addGarmentAndReturnId(other.accessToken(), minimumGarment("TOPS", "NAVY"));
        jdbc.sql("UPDATE wardrobe.wardrobe_garment SET removed_at = created_at, revision = revision + 1 WHERE id = :id")
                .param("id", removed).update();

        assertThat(matchingIds(owner.accountId(), query("navy", Set.of(), Set.of()))).isEmpty();
        assertThat(wardrobeSearchRepository.hasCurrentGarment(owner.accountId())).isFalse();
        assertThat(wardrobeSearchRepository.hasCurrentGarment(other.accountId())).isTrue();
    }

    @Test
    void priceAndOtherOptionalDetailsAreNotSearchable() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-excluded@example.com");
        addGarmentAndReturnId(tokens.accessToken(), garment("TOPS", "WHITE", "fit", "SLIM",
                "price", Map.of("amount", "299000", "currency", "VND")));

        assertThat(matchingIds(tokens.accountId(), query("299", Set.of(), Set.of()))).isEmpty();
        assertThat(matchingIds(tokens.accountId(), query("slim", Set.of(), Set.of()))).isEmpty();
    }

    @Test
    void pagesContinueStrictlyAfterThePositionNewestFirst() throws Exception {
        IssuedTokens tokens = registerAndLogIn("search-paging@example.com");
        UUID oldest = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("TOPS", "NAVY"));
        clock.advance(Duration.ofSeconds(1));
        UUID middle = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("PANTS", "NAVY"));
        clock.advance(Duration.ofSeconds(1));
        UUID newest = addGarmentAndReturnId(tokens.accessToken(), minimumGarment("SHOES", "NAVY"));
        WardrobeSearchQuery navy = query("navy", Set.of(), Set.of());

        List<Garment> firstPage = wardrobeSearchRepository.findCurrentMatching(tokens.accountId(), navy,
                Optional.empty(), 2);
        List<Garment> secondPage = wardrobeSearchRepository.findCurrentMatching(tokens.accountId(), navy,
                Optional.of(WardrobePosition.of(firstPage.getLast())), 2);

        assertThat(firstPage).extracting(Garment::id).containsExactly(newest, middle);
        assertThat(secondPage).extracting(Garment::id).containsExactly(oldest);
    }

    private static WardrobeSearchQuery query(String text, Set<GarmentCategory> categories, Set<GarmentColor> colors) {
        return DescriptorMatcher.queryFor(new WardrobeSearchCriteria(WardrobeSearchCriteria.normalizeText(text),
                categories, colors));
    }

    private List<UUID> matchingIds(UUID owner, WardrobeSearchQuery query) {
        return wardrobeSearchRepository.findCurrentMatching(owner, query, Optional.empty(), 100).stream()
                .map(Garment::id).toList();
    }
}
