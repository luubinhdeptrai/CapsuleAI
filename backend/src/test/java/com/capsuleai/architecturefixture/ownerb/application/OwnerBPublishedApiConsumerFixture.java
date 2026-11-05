package com.capsuleai.architecturefixture.ownerb.application;

import com.capsuleai.architecturefixture.ownera.api.OwnerAPublishedContractFixture;

// Valid TEST fixture: module "ownerb" uses only "ownera"'s published api.
public final class OwnerBPublishedApiConsumerFixture {

    OwnerAPublishedContractFixture allowedDependency;
}
