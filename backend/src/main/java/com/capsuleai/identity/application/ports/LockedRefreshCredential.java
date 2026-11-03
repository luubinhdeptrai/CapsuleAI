package com.capsuleai.identity.application.ports;

import com.capsuleai.identity.domain.LoginSession;
import com.capsuleai.identity.domain.RefreshToken;

/**
 * A presented refresh token and its login session, both row-locked until the
 * surrounding transaction ends.
 */
public record LockedRefreshCredential(LoginSession session, RefreshToken token) {
}