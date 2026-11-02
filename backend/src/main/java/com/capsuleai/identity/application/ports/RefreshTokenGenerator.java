package com.capsuleai.identity.application.ports;

/** Creates new, unguessable refresh-token values. */
public interface RefreshTokenGenerator {

    String newToken();
}