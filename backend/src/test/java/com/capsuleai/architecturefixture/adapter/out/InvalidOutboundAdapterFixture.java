package com.capsuleai.architecturefixture.adapter.out;

import com.capsuleai.architecturefixture.adapter.in.InvalidInboundAdapterFixture;

// Deliberately invalid TEST fixture: an outbound adapter must not depend on an inbound adapter.
public final class InvalidOutboundAdapterFixture {

    InvalidInboundAdapterFixture forbiddenDependency;
}
