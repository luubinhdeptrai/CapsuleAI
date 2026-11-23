package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.out.persistence.JdbcBulkImportRepository;
import com.capsuleai.wardrobe.application.AcceptBulkImportItemService;
import com.capsuleai.wardrobe.application.BulkImportPolicy;
import com.capsuleai.wardrobe.application.BulkImportService;
import com.capsuleai.wardrobe.application.PhotoUploadPolicy;
import com.capsuleai.wardrobe.application.ports.BulkImportRepository;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.PhotoUploadRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Wires US-BULK-001: batch persistence, the admission policy and the two bulk use cases. Reuses the
 * application's single Clock, the Wardrobe transaction port, the garment and staged-photo repositories and the
 * photo policy (its in-flight fence applies when a stage is cancelled).
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WardrobeBulkImportProperties.class)
public class WardrobeBulkImportConfiguration {

    @Bean
    BulkImportPolicy bulkImportPolicy(WardrobeBulkImportProperties properties) {
        return new BulkImportPolicy(properties.maximumItemsPerBatch(), properties.maximumActiveBatches(),
                properties.batchLifetime());
    }

    @Bean
    BulkImportRepository bulkImportRepository(JdbcClient jdbcClient) {
        return new JdbcBulkImportRepository(jdbcClient);
    }

    @Bean
    BulkImportService bulkImportService(BulkImportRepository bulkImportRepository,
                                        PhotoUploadRepository photoUploadRepository,
                                        WardrobeTransactions wardrobeTransactions, Clock clock,
                                        BulkImportPolicy bulkImportPolicy, PhotoUploadPolicy photoUploadPolicy) {
        return new BulkImportService(bulkImportRepository, photoUploadRepository, wardrobeTransactions, clock,
                bulkImportPolicy, photoUploadPolicy);
    }

    @Bean
    AcceptBulkImportItemService acceptBulkImportItemService(BulkImportRepository bulkImportRepository,
                                                            GarmentRepository garmentRepository,
                                                            PhotoUploadRepository photoUploadRepository,
                                                            WardrobeTransactions wardrobeTransactions, Clock clock) {
        return new AcceptBulkImportItemService(bulkImportRepository, garmentRepository, photoUploadRepository,
                wardrobeTransactions, clock);
    }
}
