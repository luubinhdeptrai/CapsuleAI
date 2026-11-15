package com.capsuleai.wardrobe.configuration;

import com.capsuleai.wardrobe.adapter.in.scheduling.PhotoUploadCleanupJob;
import com.capsuleai.wardrobe.application.PhotoUploadCleanupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Schedules the Wardrobe-owned pending-photo lifecycle (S3-IMG-08) with Spring's in-process scheduler.
 * Disabled with {@code capsuleai.wardrobe.photo-upload.cleanup.enabled=false}; the test profile does
 * this and calls {@link PhotoUploadCleanupService} directly.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "capsuleai.wardrobe.photo-upload.cleanup.enabled", havingValue = "true")
public class WardrobePhotoCleanupConfiguration {

    @Bean
    PhotoUploadCleanupJob photoUploadCleanupJob(PhotoUploadCleanupService cleanup) {
        return new PhotoUploadCleanupJob(cleanup);
    }
}