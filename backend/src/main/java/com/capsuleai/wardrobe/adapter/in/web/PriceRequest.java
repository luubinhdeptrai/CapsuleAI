package com.capsuleai.wardrobe.adapter.in.web;

/**
 * Optional purchase Price in a request: the amount as a decimal STRING (for
 * example {@code "1250000"} or {@code "12.50"}) so no client parses it as a
 * floating-point number, and an ISO 4217 currency code.
 */
public record PriceRequest(String amount, String currency) {
}