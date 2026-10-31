package com.capsuleai.identity.domain;

/**
 * Why a submitted value was rejected.
 *
 * <p>The constant names are part of the published API contract (field error
 * codes), so rename one only together with a contract change.
 */
public enum ValueProblem {
    REQUIRED,
    INVALID_FORMAT,
    TOO_SHORT,
    TOO_LONG,
    INVALID_CHARACTERS,
    MISMATCH
}