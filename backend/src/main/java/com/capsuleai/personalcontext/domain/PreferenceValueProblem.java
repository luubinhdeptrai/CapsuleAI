package com.capsuleai.personalcontext.domain;

/** Why a submitted preference value was rejected. Constant names are published field-error codes. */
public enum PreferenceValueProblem {

    /** expectedRevision is missing. */
    REQUIRED,

    /** expectedRevision is not null or a revision token. */
    INVALID_FORMAT,

    /** Not a profile style, not a common need, not a priority 0–3, or a value of the wrong JSON kind. */
    UNSUPPORTED_VALUE
}