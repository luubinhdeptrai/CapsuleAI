package com.capsuleai.wardrobe.adapter.in.web;

import com.capsuleai.wardrobe.domain.PurchasePrice;

/** The user-reported purchase Price, with the amount exactly as accepted (a decimal string). */
public record PriceResponse(String amount, String currency) {

    static PriceResponse from(PurchasePrice price) {
        return new PriceResponse(price.amountText(), price.currencyCode());
    }
}