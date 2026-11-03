package com.capsuleai.identity.application;

import com.capsuleai.identity.domain.ValueProblem;

/** One rejected input field, named exactly as in the HTTP request body. */
public record FieldViolation(String field, ValueProblem problem) {
}