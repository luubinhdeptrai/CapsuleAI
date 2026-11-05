package com.capsuleai.architecturefixture.ownerb.application;

import com.capsuleai.architecturefixture.ownera.adapter.out.persistence.OwnerAPrivateRepositoryFixture;

// Deliberately invalid TEST fixture: module "ownerb" bypasses "ownera"'s published api
// and uses its private repository.
public final class OwnerBBypassingServiceFixture {

    OwnerAPrivateRepositoryFixture forbiddenDependency;
}
