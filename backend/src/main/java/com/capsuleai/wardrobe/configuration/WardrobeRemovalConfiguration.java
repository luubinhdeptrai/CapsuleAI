package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.out.persistence.JdbcGarmentRemovalRepository;
import com.capsuleai.wardrobe.application.GarmentRemovalCleanupService;
import com.capsuleai.wardrobe.application.GarmentRemovalPolicy;
import com.capsuleai.wardrobe.application.RemoveGarmentService;
import com.capsuleai.wardrobe.application.ports.GarmentRemovalRepository;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoStorage;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Wires US-GAR-005: the removal use case, its persistence and the cleanup service. Reuses the application's
 * single Clock, the Wardrobe transaction port and the private photo storage.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WardrobeRemovalProperties.class)
public class WardrobeRemovalConfiguration {

    @Bean
    GarmentRemovalPolicy garmentRemovalPolicy(WardrobeRemovalProperties properties) {
        return new GarmentRemovalPolicy(properties.purgeLimit(), properties.overdueWarningAge(),
                properties.batchSize(), properties.retryDelay());
    }

    @Bean
    GarmentRemovalRepository garmentRemovalRepository(JdbcClient jdbcClient) {
        return new JdbcGarmentRemovalRepository(jdbcClient);
    }

    @Bean
    RemoveGarmentService removeGarmentService(GarmentRepository garmentRepository,
                                              GarmentRemovalRepository garmentRemovalRepository,
                                              WardrobeTransactions wardrobeTransactions, Clock clock,
                                              GarmentRemovalPolicy garmentRemovalPolicy) {
        return new RemoveGarmentService(garmentRepository, garmentRemovalRepository, wardrobeTransactions, clock,
                garmentRemovalPolicy);
    }

    @Bean
    GarmentRemovalCleanupService garmentRemovalCleanupService(GarmentRemovalRepository garmentRemovalRepository,
                                                              PhotoStorage photoStorage,
                                                              WardrobeTransactions wardrobeTransactions, Clock clock,
                                                              GarmentRemovalPolicy garmentRemovalPolicy) {
        return new GarmentRemovalCleanupService(garmentRemovalRepository, photoStorage, wardrobeTransactions, clock,
                garmentRemovalPolicy);
    }
}