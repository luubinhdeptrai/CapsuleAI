package com.capsuleai.personalcontext.configuration;

import com.capsuleai.personalcontext.adapter.out.persistence.JdbcOptionalContextRepository;
import com.capsuleai.personalcontext.adapter.out.persistence.JdbcPreferenceProfileRepository;
import com.capsuleai.personalcontext.adapter.out.persistence.SpringPersonalContextTransactions;
import com.capsuleai.personalcontext.api.CurrentPersonalContext;
import com.capsuleai.personalcontext.application.CurrentPersonalContextService;
import com.capsuleai.personalcontext.application.ReviseOptionalContextService;
import com.capsuleai.personalcontext.application.RevisePreferencesService;
import com.capsuleai.personalcontext.application.ViewOptionalContextService;
import com.capsuleai.personalcontext.application.ViewPreferencesService;
import com.capsuleai.personalcontext.application.ports.OptionalContextRepository;
import com.capsuleai.personalcontext.application.ports.PersonalContextTransactions;
import com.capsuleai.personalcontext.application.ports.PreferenceProfileRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Wires the Personal Context module's private implementations and publishes {@link CurrentPersonalContext}.
 * Reuses the application's single {@link Clock} bean.
 */
@Configuration(proxyBeanMethods = false)
public class PersonalContextConfiguration {

    @Bean
    PreferenceProfileRepository preferenceProfileRepository(JdbcClient jdbcClient) {
        return new JdbcPreferenceProfileRepository(jdbcClient);
    }

    @Bean
    PersonalContextTransactions personalContextTransactions(PlatformTransactionManager transactionManager) {
        return new SpringPersonalContextTransactions(transactionManager);
    }

    @Bean
    ViewPreferencesService viewPreferencesService(PreferenceProfileRepository profiles) {
        return new ViewPreferencesService(profiles);
    }

    @Bean
    RevisePreferencesService revisePreferencesService(PreferenceProfileRepository profiles,
                                                      PersonalContextTransactions transactions, Clock clock) {
        return new RevisePreferencesService(profiles, transactions, clock);
    }

    @Bean
    CurrentPersonalContext currentPersonalContext(PreferenceProfileRepository profiles) {
        return new CurrentPersonalContextService(profiles);
    }

    /** US-PROF-002: optional context, a separately revisioned record (S4-PROF-02). Not published (S4-PROF-04). */
    @Bean
    OptionalContextRepository optionalContextRepository(JdbcClient jdbcClient) {
        return new JdbcOptionalContextRepository(jdbcClient);
    }

    @Bean
    ViewOptionalContextService viewOptionalContextService(OptionalContextRepository optionalContextRepository) {
        return new ViewOptionalContextService(optionalContextRepository);
    }

    @Bean
    ReviseOptionalContextService reviseOptionalContextService(OptionalContextRepository optionalContextRepository,
                                                              PersonalContextTransactions personalContextTransactions,
                                                              Clock clock) {
        return new ReviseOptionalContextService(optionalContextRepository, personalContextTransactions, clock);
    }
}