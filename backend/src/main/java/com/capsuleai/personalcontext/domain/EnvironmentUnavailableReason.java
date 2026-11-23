package com.capsuleai.personalcontext.domain;

/** US-CTX-001 AC-04 (S4-CTX-05, OPEN-S4-20/22) — why no current weather is available. Codes are published. */
public enum EnvironmentUnavailableReason {

    /** Weather acquisition is switched off in this deployment. */
    NOT_CONFIGURED,

    /** The provider could not be reached or answered with an error. */
    PROVIDER_UNAVAILABLE,

    /** No usable answer within the single 2-second acquisition deadline. */
    DEADLINE_EXCEEDED,

    /** The answer could not be translated (shape, units, location, codes or a time far in the future). */
    INVALID_PROVIDER_DATA,

    /** The provider's evidence was already older than 30 minutes when it arrived. */
    STALE_PROVIDER_DATA,

    /** The selection changed or was cleared while weather was being acquired; reload the location. */
    SELECTION_CHANGED
}