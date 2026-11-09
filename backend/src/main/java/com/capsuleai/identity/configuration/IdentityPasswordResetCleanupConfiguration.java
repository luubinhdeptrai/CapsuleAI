package com.capsuleai.identity.configuration;

import com.capsuleai.identity.adapter.in.scheduling.PasswordResetCleanupJob;
import com.capsuleai.identity.application.PurgeEndedPasswordResetsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Schedules the Identity-owned password-reset cleanup (OPEN-S2-07) with Spring's in-process
 * scheduler. Disabled with {@code capsuleai.identity.password-reset.cleanup.enabled=false}
 * (the test profile does this and calls the service directly).
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "capsuleai.identity.password-reset.cleanup.enabled", havingValue = "true")
public class IdentityPasswordResetCleanupConfiguration {

    @Bean
    PasswordResetCleanupJob passwordResetCleanupJob(PurgeEndedPasswordResetsService purge) {
        return new PasswordResetCleanupJob(purge);
    }
}