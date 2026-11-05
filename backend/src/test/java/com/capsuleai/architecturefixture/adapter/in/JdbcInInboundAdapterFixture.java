package com.capsuleai.architecturefixture.adapter.in;

import org.springframework.jdbc.core.simple.JdbcClient;

// Deliberately invalid TEST fixture: only persistence adapters and configuration use Spring JDBC.
public final class JdbcInInboundAdapterFixture {

    JdbcClient forbiddenDependency;
}
