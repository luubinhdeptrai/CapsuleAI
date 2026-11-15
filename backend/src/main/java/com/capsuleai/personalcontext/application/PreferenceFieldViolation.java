package com.capsuleai.personalcontext.application;

import com.capsuleai.personalcontext.domain.PreferenceValueProblem;

/**
 * One rejected input, named as in the request: {@code expectedRevision}, {@code styles},
 * {@code styles[1]}, {@code commonNeeds} or {@code commonNeeds.TRAVEL}. A submitted unknown need key is
 * reported as {@code commonNeeds}, so no submitted text is echoed.
 */
public record PreferenceFieldViolation(String field, PreferenceValueProblem problem) {
}