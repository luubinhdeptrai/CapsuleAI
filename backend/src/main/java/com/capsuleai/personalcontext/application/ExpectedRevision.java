package com.capsuleai.personalcontext.application;

import java.util.regex.Pattern;

/**
 * The precondition of a preference change (S3-PROF-04, OPEN-S3-04): missing (an error), unset ("I saw
 * no declaration yet": guarded first creation), a revision token, or malformed.
 */
public sealed interface ExpectedRevision {

    record Missing() implements ExpectedRevision {
    }

    record Unset() implements ExpectedRevision {
    }

    record At(long revision) implements ExpectedRevision {
    }

    record Malformed() implements ExpectedRevision {
    }

    /** A positive decimal that fits in a long; clients treat it as opaque. */
    Pattern TOKEN = Pattern.compile("[1-9][0-9]{0,17}");

    static ExpectedRevision ofToken(String token) {
        return token != null && TOKEN.matcher(token).matches() ? new At(Long.parseLong(token)) : new Malformed();
    }
}