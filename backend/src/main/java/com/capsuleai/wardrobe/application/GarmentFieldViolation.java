package com.capsuleai.wardrobe.application;

import com.capsuleai.wardrobe.domain.GarmentValueProblem;

/**
 * One rejected input, named as in the HTTP request: a body field such as
 * {@code subtype}, {@code materials[1]} or {@code price.amount}, or the
 * {@code Idempotency-Key} header.
 */
public record GarmentFieldViolation(String field, GarmentValueProblem problem) {
}