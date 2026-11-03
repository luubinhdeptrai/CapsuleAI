package com.capsuleai.architecturefixture.application;

import org.springframework.context.ApplicationContext;

// Deliberately invalid TEST fixture: application code must not depend on Spring.
public final class InvalidApplicationFixture {

    ApplicationContext forbiddenDependency;
}