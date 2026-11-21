package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.in.scheduling.GarmentRemovalCleanupJob;
import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Schedules the removal cleanup (US-GAR-005, S4-GAR-03) with Spring's in-process scheduler. Disabled with
 * {@code capsuleai.wardrobe.removal-cleanup.enabled=false}; the test profile does this and calls
 * {@link GarmentRemovalCleanupService#run()} directly.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "capsuleai.wardrobe.removal-cleanup.enabled", havingValue = "true")
public class WardrobeRemovalCleanupConfiguration {

    @Bean
    GarmentRemovalCleanupJob garmentRemovalCleanupJob(GarmentRemovalCleanupService garmentRemovalCleanupService) {
        return new GarmentRemovalCleanupJob(garmentRemovalCleanupService);
    }
}
