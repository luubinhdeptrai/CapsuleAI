package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.application.WardrobeSearchCriteria;
import java.util.List;

/**
 * US-WAR-003 AC-03 — the active, normalized conditions of a page, so the client can show, change and clear
 * them. {@code q} is null without a text condition; the code lists are in catalogue order and [] when absent.
 */
public record WardrobeConditionsResponse(String q, List<String> categories, List<String> colors) {

    static WardrobeConditionsResponse from(WardrobeSearchCriteria criteria) {
        return new WardrobeConditionsResponse(
                criteria.text().orElse(null),
                criteria.categories().stream().map(Enum::name).toList(),
                criteria.colors().stream().map(Enum::name).toList());
    }
}