package com.capsuleai.architecturefixture.ownera.api;

import com.capsuleai.architecturefixture.ownera.domain.OwnerAPrivateEntityFixture;

// Deliberately invalid TEST fixture: a published contract must not expose a private domain model.
public interface OwnerALeakingContractFixture {

    OwnerAPrivateEntityFixture findEntity();
}
