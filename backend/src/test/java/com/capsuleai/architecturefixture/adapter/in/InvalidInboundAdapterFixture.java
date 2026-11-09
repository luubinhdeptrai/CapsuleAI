package com.capsuleai.architecturefixture.adapter.in;

import com.capsuleai.architecturefixture.adapter.out.InvalidOutboundAdapterFixture;

// Deliberately invalid TEST fixture: an inbound adapter must not depend on an outbound adapter.
public final class InvalidInboundAdapterFixture {

    InvalidOutboundAdapterFixture forbiddenDependency;
}
