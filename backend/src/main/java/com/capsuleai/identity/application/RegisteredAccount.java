package com.capsuleai.identity.application;

import java.util.UUID;

/** Result of a successful registration. Deliberately contains no password or hash. */
public record RegisteredAccount(UUID accountId, String email, String displayName) {
}