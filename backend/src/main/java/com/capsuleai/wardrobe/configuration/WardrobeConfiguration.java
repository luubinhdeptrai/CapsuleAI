package com.capsuleai.wardrobe.configuration;


import com.capsuleai.wardrobe.adapter.out.persistence.JdbcGarmentRepository;
import com.capsuleai.wardrobe.adapter.out.persistence.SpringWardrobeTransactions;
import com.capsuleai.wardrobe.application.AddGarmentService;
import com.capsuleai.wardrobe.application.BrowseWardrobeService;
import com.capsuleai.wardrobe.application.InspectGarmentService;
import com.capsuleai.wardrobe.application.ReviseGarmentService;
import com.capsuleai.wardrobe.application.ports.GarmentRepository;
import com.capsuleai.wardrobe.application.ports.WardrobeTransactions;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the Wardrobe module's private implementations. It reuses the
 * application's single {@link Clock} bean; declaring a second Clock would make
 * every Clock injection ambiguous.
 */
@Configuration(proxyBeanMethods = false)
public class WardrobeConfiguration {

    @Bean
    GarmentRepository garmentRepository(JdbcClient jdbcClient) {
        return new JdbcGarmentRepository(jdbcClient);
    }

    @Bean
    WardrobeTransactions wardrobeTransactions(PlatformTransactionManager transactionManager) {
        return new SpringWardrobeTransactions(transactionManager);
    }

    @Bean
    AddGarmentService addGarmentService(GarmentRepository garments, WardrobeTransactions transactions, Clock clock) {
        return new AddGarmentService(garments, transactions, clock);
    }

    @Bean
    BrowseWardrobeService browseWardrobeService(GarmentRepository garments) {
        return new BrowseWardrobeService(garments);
    }

    @Bean
    InspectGarmentService inspectGarmentService(GarmentRepository garments) {
        return new InspectGarmentService(garments);
    }

    @Bean
    ReviseGarmentService reviseGarmentService(GarmentRepository garments, WardrobeTransactions transactions) {
        return new ReviseGarmentService(garments, transactions);
    }
}